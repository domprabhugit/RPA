/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.camel.processors.common.OracleSqlLoader;
import com.rpa.camel.processors.common.OracleSqlLoader.ExitCode;
import com.rpa.camel.processors.common.OracleSqlLoader.Results;
import com.rpa.camel.processors.common.XlsxConverter;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.util.UtilityFile;

public class LifelineInwardUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(LifelineInwardUploader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	private void AutoWiringBeanPropertiesSetMethod() {

		logger.info("AutoWiringBeanPropertiesSetMethod is Called in LifelineInwardUploader Class");
		applicationContext = SpringContext.getAppContext();
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in LifelineInwardUploader Class");

	}

	@Override
	public void process(Exchange exchange) throws SQLException {
		logger.info("*********** inside Camel Process of LifelineInwardUploader Called ************");
		AutoWiringBeanPropertiesSetMethod();
		logger.info("BEGIN : LifelineInwardUploader - Started ");
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		LifelineInwardUploader lifelineInwardFileProcess = new LifelineInwardUploader();
		transactionInfo.setProcessPhase(RPAConstants.UPLOAD);
		lifelineInwardFileProcess.doProcess(exchange, transactionInfo);
		logger.info("*********** inside Camel Process of LifelineInwardUploader Ended ************");
	}

	public void doProcess(Exchange exchange, TransactionInfo transactionInfo) throws SQLException {

		logger.info("BEGIN : LifelineInwardUploader - doProcess Method Called  ");
		if (!getFilesToProcess(exchange, transactionInfo)) {
			transactionInfo.setProcessStatus(RPAConstants.UPLOAD_ERROR);
		}
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("LifelineInwardUploader - doProcess Method Ended  ");
	}

	private boolean getFilesToProcess(Exchange exchange, TransactionInfo transactionInfo) throws SQLException {
		Connection conn = null;
		logger.info("BEGIN : LifelineInwardUploader - getFilesToSplit Method Called  ");
		try {

			File directory = new File(UtilityFile.getCodeBasePath()
					+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_UPLOAD)
					+ exchange.getProperty("fileName"));

			if (!directory.exists())
				directory.mkdirs();

			File[] directoryList = directory.listFiles();
			boolean isCsvFileAvailableToUpload = false;

			for (File file : directoryList) {

				if (file.canRead() && !file.isDirectory()) {

					if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("csv")) {
						isCsvFileAvailableToUpload = true;
						logger.info("LifelineInwardUploader - isCsvFileAvailableToUpload  :: "
								+ isCsvFileAvailableToUpload);
						DriverManager.registerDriver(
								(Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
						String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.ILONGUE_DATASOURCE_URL);
						String instance = connInstance.substring(connInstance.indexOf("@") + 1);
						String username = UtilityFile.getDatabaseProperty(RPAConstants.ILONGUE_DATASOURCE_USERNAME);
						String password = UtilityFile.getDatabaseProperty(RPAConstants.ILONGUE_DATASOURCE_PASSWORD);
						conn = DriverManager.getConnection(connInstance, username, password);

						int uploadId = getNextUploadId(conn, RPAConstants.ILONGUE_TEMP_TABLE_NAME);
						String quotes = "\"";
						String ilongueTempTableColumns = "PROPOSARCODE, FIRSTNAME, LASTNAME, CITY, STATE, BRANCH, AGENTCODE, AGENTNAME, SUBLINE, PRODUCTNAME, TRANSACTIONTYPE, POLICYCODE, QUOTENUMBER, INWARDCODE, INWARDPREMIUM, CURRENT_USER, CREATIONDATE, CREATEDBY, COMMENTSDATE, CHEQUE_NUMBER, CHEQUE_DATE, CHEQUE_AMOUNT, CHEQUE_BRANCH, CHEQUE_BANK, CASHAMT, POLICY_START_DATE, POLICY_END_DATE, PROPOSALFORMNUMBER, SMNAME, SMCODE, BRANCH_NAME, OACODE, POLICYPOSTEDDATE, NETPREMIUM, RECEIPT_DATE, PAYMENTMODE, ROWN, CONS_RECEIPT, RECP_AMOUNT, INWARDDATE, INFO_REASON, COMMENTS CHAR(2000), STATUS, SHORTFALL, CHANNEL_NEW,REFERRAL_TO,REFERRAL_ACTIONED, REFERRAL_DATE_UW,UW_STATUS , DECISION,REMARKS,ACTIONABLE,CONT_NONCONT_OF_PPMC,SUBREMARKS_OF_PPMC,UPLOADED_DATE sysdate,UPLOAD_ID EXPRESSION"
								+ quotes + "(select " + uploadId + " from dual) " + quotes + "";
						logger.info("LifelineInwardUploader - next upload id  :: " + uploadId);
						
						
						transactionInfo.setExternalTransactionRefNo(String.valueOf(uploadId));
						
						String sqlldrPath = UtilityFile.getCodeBasePath().replace("/", "\\")+"\\rpa_apps\\sql_loader_client\\sqlldr";
						
						logger.info(" LifelineInwardUploader - sqlldrPath :: "+sqlldrPath);
						
						final Results results = OracleSqlLoader.bulkLoad(conn, username, password, instance,
								RPAConstants.ILONGUE_TEMP_TABLE_NAME, new File(directory + "\\" + file.getName()),
								directory, RPAConstants.ILONGUE_LINE_TERMINATER, RPAConstants.ILONGUE_FIELD_TERMINATER,
								ilongueTempTableColumns,sqlldrPath);
						logger.info("LifelineInwardUploader - sql loader result :: " + results.exitCode);
						if (results.exitCode != ExitCode.SUCCESS) {
							transactionInfo.setProcessStatus(RPAConstants.UPLOAD_ERROR);

						} else {
							logger.info("uploaded successfully  :: " + uploadId);
						}

						File[] uploadDirectoryList = directory.listFiles();
						for (File uploadedfile : uploadDirectoryList) {

							if (uploadedfile.canRead() && !uploadedfile.isDirectory()
									&& FilenameUtils.getExtension(uploadedfile.getName()).equalsIgnoreCase("bad")) {
								XlsxConverter xlsxConverter = new XlsxConverter();
								xlsxConverter.convertToXlsx(uploadedfile.getPath(),
										uploadedfile.getParent() + RPAConstants.SLASH
												+ (exchange.getProperty("fileName") + "_ERROR.XLSX"),
										RPAConstants.ILONGUE_FIELD_TERMINATER, RPAConstants.ILONGUE_LINE_TERMINATER);
								transactionInfo.setErrorFileDownload(uploadedfile.getParent() + RPAConstants.SLASH
										+ (exchange.getProperty("fileName") + "_ERROR.XLSX"));
							} else if (uploadedfile.canRead() && !uploadedfile.isDirectory()
									&& FilenameUtils.getExtension(uploadedfile.getName()).equalsIgnoreCase("log")
									&& !uploadedfile.getName().contains("stderr")
									&& !uploadedfile.getName().contains("stdout")) {
								if (checkLog(UtilityFile.getCodeBasePath()
										+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_UPLOAD)
										+ RPAConstants.SLASH + (exchange.getProperty("fileName") + RPAConstants.SLASH)
										+ (exchange.getProperty("fileName") + ".log"), transactionInfo)) {
									logger.info("LifelineInwardUploader - log file read ");
									callProcedure(conn, String.valueOf(uploadId), RPAConstants.TYPE_CODE);
									logger.info("LifelineInwardUploader - procedure called ");
								} else {
									logger.info("LifelineInwardUploader -  procedure not called this time ");
								}
							}

						}

						transactionInfo.setLogFileDownload(UtilityFile.getCodeBasePath()
								+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_UPLOAD)
								+ RPAConstants.SLASH + (exchange.getProperty("fileName") + RPAConstants.SLASH)
								+ (exchange.getProperty("fileName") + ".log"));
					} else {
						logger.error("LifelineInwardUploader - Invalid file extension expected csv but found :: "
								+ FilenameUtils.getExtension(file.getName()));
					}

				}

			}

			if (!isCsvFileAvailableToUpload) {
				transactionInfo.setProcessStatus(RPAConstants.NO_FILE);
				logger.info("LifelineInwardUploadValidator  - No Files in process folder to upload");
			} else {
				transactionInfo.setProcessStatus(RPAConstants.FILE_UPLOADED);
				exchange.getIn().setHeader(RPAConstants.NO_FILE, RPAConstants.N);
				logger.info("LifelineInwardUploadValidator  - File uploaded");
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Error in LifelineInwardUploader - doProcess Method Ended  " , e);
			return false;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	private boolean checkLog(String logFile, TransactionInfo transactionInfo) {
		logger.info("LifelineInwardUploader - inside checkLog() ");
		boolean isSomeDataGotInserted = false;
		boolean isLogEmpty = true;
		try {
			FileInputStream fstream = new FileInputStream(logFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			// read log line by line
			while ((strLine = br.readLine()) != null) {
				isLogEmpty = false;
				// parse strLine to obtain what you want

				String successRows = "0";
				if (strLine.contains("Rows successfully loaded")) {
					System.out.println(strLine);
					strLine = strLine.replaceAll("\\s+", "");
					if (strLine.indexOf("Rows") != -1) {
						successRows = strLine.split("Rows")[0];
						if (Integer.valueOf(successRows) > 0) {
							isSomeDataGotInserted = true;
						} else {
							isSomeDataGotInserted = false;
						}
						transactionInfo.setTotalSuccessUploads(successRows);
						logger.info("LifelineInwardUploader - successfully  loaded rows ::" + successRows);
					}
					int errorRecords = Integer.valueOf(transactionInfo.getTotalUploadRecords())
							- Integer.valueOf(transactionInfo.getTotalSuccessUploads());
					transactionInfo.setTotalErrorUploads(String.valueOf(errorRecords));
					logger.info("LifelineInwardUploader - error record count ::" + errorRecords);
				}
			}
			if (isLogEmpty) {
				transactionInfo.setTotalSuccessRecords(transactionInfo.getTotalSuccessRecords());
				logger.info("LifelineInwardUploader - successfully  loaded rows ::"
						+ transactionInfo.getTotalSuccessRecords());
				transactionInfo.setTotalErrorRecords("0");
			}
			fstream.close();
			return isSomeDataGotInserted;
		} catch (Exception e) {
			logger.info("LifelineInwardUploader - error in checkLog() ::", e);
			return false;
		}

	}

	public static Integer getNextUploadId(final Connection conn, final String tableName) throws SQLException {
		logger.info("LifelineInwardUploader - Begin getNextUploadId() ");
		Integer nextUploadId = 0;
		try (PreparedStatement ps = conn.prepareStatement("select GET_NEXT_UPLOADID from dual")) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					nextUploadId = rs.getInt(1);
				}
			}
		}
		logger.info("LifelineInwardUploader - End getNextUploadId() ");
		return nextUploadId;
	}

	public static Integer callProcedure(final Connection conn, final String uploadId, String typeCode)
			throws SQLException {
		logger.info("LifelineInwardUploader - Begin callProcedure() ");
		Integer nextUploadId = 0;
		try (PreparedStatement ps = conn.prepareStatement("{call Approved_Validation(?,?)}")) {
			ps.setString(1, uploadId);
			ps.setString(2, typeCode);
			try (ResultSet rs = ps.executeQuery()) {
				/*
				 * while (rs.next()) { nextUploadId= rs.getInt(1); }
				 */
			}
		}
		logger.info("LifelineInwardUploader - End callProcedure() ");
		return nextUploadId;
	}

}
