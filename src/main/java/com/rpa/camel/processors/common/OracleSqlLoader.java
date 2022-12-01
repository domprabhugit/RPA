package com.rpa.camel.processors.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpa.util.UtilityFile;

public class OracleSqlLoader {
	
	private static final Logger logger = LoggerFactory.getLogger(OracleSqlLoader.class.getName());

	 /**
     * SQL*Loader process exit code. Operating-system independent.
     * The process itself returns an operating-system specific exit code, so we normalize it.
     * <p/>
     */
    public enum ExitCode {SUCCESS, FAIL, WARN, FATAL, UNKNOWN}

    /**
     * Return value from our high level API method.
     * It contains SQL*Loader process exit code File objects referring generated files.
     * This class is immutable, so no need for getters and setters.
     */
    public static class Results {
        public final ExitCode exitCode;
        public final File controlFile;
        public final File logFile;
        public final File badFile;
        public final File discardFile;

        public Results(ExitCode exitCode, File controlFile, File logFile, File badFile, File discardFile) {
            this.exitCode = exitCode;
            this.controlFile = controlFile;
            this.logFile = logFile;
            this.badFile = badFile;
            this.discardFile = discardFile;
        }
    }

    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    /**
     * Helper method. Get list of table columns, to be inserted in control file.
     * TSV data file must match this column order.
     */
    public static List<String> getTableColumns(final Connection conn, final String tableName) throws SQLException {
        final List<String> ret = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("select COLUMN_NAME from USER_TAB_COLUMNS where TABLE_NAME = ? order by COLUMN_ID")) {
            ps.setObject(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ret.add(rs.getString(1));
                }
            }
        }
        return ret;
    }
    

    /**
     * Helper method. Generate intermediate control file.
     * @param columns 
     * @param fieldTerminater 
     * @param lineTerminater 
     */
    public static String createControlFile(
            final String dataFileName,
            final String badFileName,
            final String discardFileName,
            final String tableName, String lineTerminater, String fieldTerminater, String columns
    ) {
    	logger.info(" Oracle sql loader - createControlFile method called ");
        return "OPTIONS(skip=1) \n" +
                "load data infile '" + dataFileName + ".csv' \n" +
                "badfile '" + badFileName + "'\n" +
                "discardfile '" + discardFileName + "'\n" +
                "append\n" +
                "continueif last <> '"+lineTerminater+"'"+
                "into table " + tableName + "\n" +
             "fields terminated by '"+fieldTerminater+"' OPTIONALLY ENCLOSED BY '\"' " + 
              "TRAILING NULLCOLS( "+columns+" )";
    }

    /**
     * Run SQL*Loader process.
     * @param sqlldrPath 
     */
    public static ExitCode runSqlLdrProcess(
            final File initialDir,
            final String stdoutLogFile,
            final String stderrLogFile,
            final String controlFile,
            final String logFile,
            final String username,
            final String password,
            final String instance, String sqlldrPath
    ) throws IOException {
    	
    	logger.info(" Oracle sql loader - runSqlLdrProcess called ");
    	
        final ProcessBuilder pb = new ProcessBuilder(
        		sqlldrPath,
                "control=" + controlFile,
                "log=" + logFile,
                "userid=" + username + "/" + password + "@" + instance,
                "silent=header"
        );
        logger.info(" Oracle sql loader - sqlldrPath inside  Oraclesqlloader :: "+sqlldrPath);
        
        logger.info(" Oracle sql loader - before initialize directory :: "+initialDir);
        pb.directory(initialDir);
        if (stdoutLogFile != null) pb.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(initialDir, stdoutLogFile)));
        if (stderrLogFile != null) pb.redirectError(ProcessBuilder.Redirect.appendTo(new File(initialDir, stderrLogFile)));
        final Process process = pb.start();
        logger.info(" Oracle sql loader - process start called");
        try {
        	logger.info(" Oracle sql loader - before wait for");
           // process.waitFor(); // TODO may implement here timeout mechanism and progress monitor instead of just blocking the caller thread.
            
        	process.waitFor(60, TimeUnit.SECONDS);
        	if(process!=null)
        		process.destroy();
        	
        	logger.info(" Oracle sql loader - after wait for called");
        } catch (InterruptedException e) {
        	 logger.info("Ignored interrrupted excpetion :: "+ e.getMessage());
        }

        final int exitCode = process.exitValue();
        logger.info(" Oracle sql loader - exitCode :: "+exitCode);
        // Exit codes are OS dependent. Convert them to our OS independent.
        // See: https://docs.oracle.com/cd/B19306_01/server.102/b14215/ldr_params.htm#i1005019
        switch (exitCode) {
            case 0:
                return ExitCode.SUCCESS;
            case 1:
                return ExitCode.FAIL;
            case 2:
                return ExitCode.WARN;
            case 3:
                return IS_WINDOWS ? ExitCode.FAIL : ExitCode.FATAL;
            case 4:
                return ExitCode.FATAL;
            default:
                return ExitCode.UNKNOWN;
        }
    }

    /**
     * High level API.
     * Wraps the logic of SQL*Loader tool.
     *
     * @param conn JDBC connection matching username, password and instance arguments. Used to read the column list of the table.
     * @param username to be fed to SQL*Loader process, should match JDBC connection details.
     * @param password to be fed to SQL*Loader process, should match JDBC connection details.
     * @param instance to be fed to SQL*Loader process, should match JDBC connection details.
     * @param tableName table to be populated
     * @param dataFile
     *      tab-separated values file to be inserted to the table.
     *      Column order must match table's column order.
     *      Check by running this SQL command:
     *      <pre>
     *      select * from USER_TAB_COLUMNS where table_name = '[your-table-name]' order by COLUMN_ID
     *      </pre>
     * @param destinationDirectory 
     * @param sqlldrPath 
     * @param uploadId 
     */
    public static Results bulkLoad(
            final Connection conn,
            final String username,
            final String password,
            final String instance,
            final String tableName,
            final File dataFile, File destinationDirectory,
            String lineTerminater, String fieldTerminater, String columns, String sqlldrPath
    ) throws IOException, SQLException {
    	logger.info(" Oracle sql loader - bulkLoad method called ");
        final String dataFileName = UtilityFile.getFileNameWithoutExtension(dataFile);
        final String controlFileName = dataFileName + ".ctl";
        final String logFileName = dataFileName + ".log";
        final String badFileName = dataFileName + ".bad";
        final String discardFileName = dataFileName + ".discard";

        final File controlFile = new File(destinationDirectory, controlFileName);
        
        final String controlFileContents = createControlFile(dataFileName, badFileName, discardFileName, tableName,lineTerminater,fieldTerminater,columns);
        logger.info(" Oracle sql loader - Control file created ");
        Files.write(controlFile.toPath(), controlFileContents.getBytes(), StandardOpenOption.CREATE_NEW);
        logger.info(" Oracle sql loader - Contents written on Control file ");

        final ExitCode exitCode = runSqlLdrProcess(
        		destinationDirectory,
                dataFileName + ".stdout.log",
                dataFileName + ".stderr.log",
                controlFileName,
                logFileName,
                username,
                password,
                instance,
                sqlldrPath
        );

        logger.info(" Oracle sql loader - exitCode as status :: "+exitCode);
        
        // Return to the caller names of files generated inside this method.
        Results ret = new Results(
                exitCode,
                controlFile,
                new File(destinationDirectory, logFileName),
                new File(destinationDirectory, badFileName),
                new File(destinationDirectory, discardFileName)
        );
        return ret;
    }

    // TODO may add method to parse log file if required


}
