/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.util;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
//import java.util.function.Function;
import com.google.common.base.Function;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpa.Application;
import com.rpa.constants.RPAConstants;
import com.rpa.model.CustomerInfo;
import com.rpa.model.VB64EmailPojo;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

public class UtilityFile {
	private static final Logger logger = LoggerFactory.getLogger(UtilityFile.class);

	private static final Pattern DOUBLE_PATTERN = Pattern
			.compile("[\\x00-\\x20]*[+-]?(NaN|Infinity|((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)"
					+ "([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|"
					+ "(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))"
					+ "[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*");

	public static boolean isFloat(String s) {
		return DOUBLE_PATTERN.matcher(s).matches();
	}

	public static boolean checkDateFormat(String dateValue, String dateFormat) {
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			date = sdf.parse(dateValue);
			if (!dateValue.equals(sdf.format(date))) {
				date = null;
			}
		} catch (ParseException ex) {
			logger.error("Error in CheckDate Format Method" + ex.getMessage(), ex);
		}
		if (date == null) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean onlyLettersSpaces(String s) {
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (Character.isLetter(ch) || ch == ' ') {
				continue;
			}
			return false;
		}
		return true;
	}

	public static String convertToDateFormat(String dateValue, String originalDateFormat, String newDateFormat) {
		String formatedDate = null;
		try {
			SimpleDateFormat originalFormat = new SimpleDateFormat(originalDateFormat);
			Date date = originalFormat.parse(dateValue);
			SimpleDateFormat newFormat = new SimpleDateFormat(newDateFormat);
			formatedDate = newFormat.format(date);
		} catch (ParseException ex) {
			logger.error("Error in convertToDateFormat  Method" + ex.getMessage(), ex);
		}
		return formatedDate;
	}

	public static String createSpecifiedDateFormat(String dateFormat) {
		String formatedDate = null;
		Date date = new Date();
		formatedDate = new SimpleDateFormat(dateFormat).format(date);
		return formatedDate;
	}

	public static String getUploadProperty(String key) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			URL imageURL = UtilityFile.class.getResource("/properties/upload.properties");
			prop.load(imageURL.openStream());

		} catch (IOException ex) {
			logger.error("Error in getUploadProperty  Method" + ex.getMessage(), ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.error("Error in getUploadProperty  Method" + e.getMessage(), e);
				}
			}
		}
		return prop.getProperty(key);
	}

	public static String getSMTPDetails(String key) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			URL imageURL = UtilityFile.class.getResource("/properties/smtp.properties");
			prop.load(imageURL.openStream());

		} catch (IOException ex) {
			logger.error("Error in getSMTPDetails  Method" + ex.getMessage(), ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.error("Error in getSMTPDetails  Method" + e.getMessage(), e);
				}
			}
		}
		return prop.getProperty(key);
	}

	public static String getCodeBasePath() throws UnsupportedEncodingException, URISyntaxException {
		String path = Application.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		logger.info("getCodeBasePath::" + path);
		String reponsePath = "";

		if (path.contains("/target/classes/")) {
			reponsePath = path.replace("/target/classes/", "");
		} else if (path.endsWith("RPA.war")) {
			reponsePath = path.replace("RPA.war", "");
		} else {
			reponsePath = path.replace("/RPA.war!/BOOT-INF/classes!/", "");
			reponsePath = path.replace("/RPA.war!/WEB-INF/classes!/", "");
		}

		reponsePath = reponsePath.replace("file:/", "");
		reponsePath = reponsePath.replace("/rpa_deploy", "");

		logger.info("FINAL getCodeBasePath::" + reponsePath);
		return reponsePath;
	}

	public static String removeJunkCharacters(String inputString) {
		final String[] metaCharacters = { "©", "®", "¯", "³", "´", "¸", "¹", "¾", "À", "Á", "Â", "Ã", "Ä", "Å", "È",
				"É", "Ê", "Ë", "Ì", "Í", "Î", "Ï", "Ð", "Ò", "Ó", "Ô", "Õ", "Ö", "×", "Ø", "Ù", "Ú", "Û", "Ü", "Ý", "Þ",
				"ã", "ð", "õ", "÷", "ø", "ü", "ý", "þ", "¤", "¶", "§", "?", "`", "{", "|", "}", "~", "", "Ç", "ü", "é",
				"â", "ä", "à", "å", "ç", "ê", "ë", "è", "ï", "î", "ì", "æ", "Æ", "ô", "ö", "ò", "û", "ù", "ÿ", "¢", "£",
				"¥", "P", "á", "í", "ó", "ú", "ñ", "Ñ", "¿", "¬", "½", "¼", "¡", "«", "»", "¦", "ß", "µ", "±", "°", "•",
				"·", "²", "€", "„", "…", "†", "‡", "ˆ", "‰", "Š", "‹", "Œ", "‘", "’", "“", "”", "–", "—", "˜", "™", "š",
				"›", "œ", "Ÿ", "¨"

		};
		String outputString = "";
		for (int i = 0; i < metaCharacters.length; i++) {
			if (inputString.contains(metaCharacters[i])) {
				outputString = inputString.replace(metaCharacters[i], "");
				inputString = outputString;
			} else {
				outputString = inputString;
			}
		}
		return outputString;
	}

	@SuppressWarnings("rawtypes")
	public static boolean isSheetEmpty(SXSSFSheet sheet) {
		Iterator rows = sheet.rowIterator();
		while (rows.hasNext()) {
			SXSSFRow row = (SXSSFRow) rows.next();
			Iterator cells = row.cellIterator();
			while (cells.hasNext()) {
				SXSSFCell cell = (SXSSFCell) cells.next();
				if (!cell.getStringCellValue().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean isSheetEmpty(HSSFSheet sheet) {
		Iterator rows = sheet.rowIterator();
		while (rows.hasNext()) {
			HSSFRow row = (HSSFRow) rows.next();
			Iterator cells = row.cellIterator();
			while (cells.hasNext()) {
				HSSFCell cell = (HSSFCell) cells.next();
				if (!cell.getStringCellValue().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	public static List<File> getSubdirs(File file) {
		List<File> subdirs = Arrays.asList(file.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory();
			}
		}));
		subdirs = new ArrayList<File>(subdirs);
		List<File> deepSubdirs = new ArrayList<File>();
		for (File subdir : subdirs) {
			deepSubdirs.addAll(getSubdirs(subdir));
		}
		subdirs.addAll(deepSubdirs);
		return subdirs;
	}

	public static VB64EmailPojo getEmailDetails(String bankName, String filePath) {
		VB64EmailPojo emailPojo = new VB64EmailPojo();
		emailPojo.setBankName(bankName);
		emailPojo.setFileName(filePath);
		CustomerInfo customerInfo = new CustomerInfo();
		customerInfo.setName("User");
		customerInfo.setAddress("RoyalSundaram");
		customerInfo.setEmail(UtilityFile.getSMTPDetails(RPAConstants.VB64_EMAIL_TO));
		emailPojo.setCustomerInfo(customerInfo);
		return emailPojo;
	}

	public static String dateToSting(Date date, String format) {
		String sDate = "";
		if (date != null && format != null) {
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			try {
				sDate = formatter.format(date);
				return sDate;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return sDate;
	}

	public static String getBatchProperty(String key) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			URL imageURL = UtilityFile.class.getResource("/properties/batch.properties");
			prop.load(imageURL.openStream());

		} catch (IOException ex) {
			logger.error("Error in getBatchProperty  Method" + ex.getMessage(), ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.error("Error in getBatchProperty  Method" + e.getMessage(), e);
				}
			}
		}
		return prop.getProperty(key);
	}

	public static boolean isFileDownload_Exist(String dirPath, String ext) {
		logger.info("isFileDownload_Exist() Is File Exist Or Not Method Called");
		boolean flag = false;
		try {

			File dir = new File(dirPath);
			File[] files = dir.listFiles();
			if (files == null || files.length == 0) {
				logger.info("isFileDownload_Exist() No File avilable in "+dirPath);
				flag = false;

			}else{
				logger.info("isFileDownload_Exist() "+files.length+" file available in path");
			}

			for (int i = 0; i < files.length; i++) {
				logger.info("isFileDownload_Exist() comparting file "+files[i].getName()+" with extension :: "+ ext);
				if (files[i].getName().contains(ext)) {
					logger.info("isFileDownload_Exist() file - "+files[i].getName()+" conatins extension :: "+ ext);
					flag = true;
				}else{
					logger.info("isFileDownload_Exist() file - "+files[i].getName()+" not conatins extension :: "+ ext);
				}

			}

		} catch (Exception e) {
			logger.error("Error in isFileDownload_Exist  Method" + e.getMessage(), e);
		}
		return flag;
	}

	public static boolean isFileDeleted(String dirPath, String ext) {

		logger.info("Is File Deleted Or Not Method Called");
		boolean flag = false;
		try {

			File dir = new File(dirPath);
			File[] files = dir.listFiles();
			logger.info("File count available in "+dirPath+" is : "+files.length);
			if (files == null || files.length == 0) {
				flag = false;
			}
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().contains(ext)) {
					flag = true;
					files[i].delete();
				}
			}

		}

		catch (Exception e) {
			logger.error("Error in isFileDownload_Exist  Method" + e.getMessage(), e);
		}
		return flag;

	}

	public static String generateInitialPassword(int length) {
		return RandomStringUtils.randomAlphabetic(length);
	}

	public static String getIlongueProperty(String key) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			URL propURL = UtilityFile.class.getResource("/properties/ilongue.properties");
			prop.load(propURL.openStream());

		} catch (IOException ex) {
			logger.error("Error in getBatchProperty  Method" + ex.getMessage(), ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.error("Error in getBatchProperty  Method" + e.getMessage(), e);
				}
			}
		}
		return prop.getProperty(key);
	}

	public static File getTheOldestFile(String filePath, String ext) {
		File theNewestFile = null;
		File dir = new File(filePath);
		FileFilter fileFilter = new WildcardFileFilter("*." + ext);
		File[] files = dir.listFiles(fileFilter);

		if (files.length > 0) {
			/** The oldest file comes first **/
			Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
			theNewestFile = files[0];
		}

		return theNewestFile;
	}

	public static void writeExcelFileByPath(String directoryName, String filePath, HSSFWorkbook hssfWorkBook) {

		File directory = new File(directoryName);
		if (!directory.exists()) {
			directory.mkdirs();
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(filePath);
			hssfWorkBook.write(fileOut);
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static String getDatabaseProperty(String key) throws URISyntaxException {
		Properties prop = new Properties();
		InputStream input = null;
		try {
		
			String jarBasePath = UtilityFile.getCodeBasePath();
			input = new FileInputStream(jarBasePath+"/rpa_deploy/config/database.properties");
			// load a properties file
			prop.load(input);
			
		} catch (IOException ex) {
			logger.error("Error in getDatabaseProperty  Method" + ex.getMessage(), ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.error("Error in getDatabaseProperty  Method" + e.getMessage(), e);
				}
			}
		}
		return prop.getProperty(key);
	}

	public static String getFileNameWithoutExtension(File fileWithExtension) {
		String fileNameWithoutExtension = "";
		String fileName = fileWithExtension.getName();
		if (fileName.indexOf(".") != -1) {
			int index = fileName.lastIndexOf(".");
			fileNameWithoutExtension = fileName.substring(0, index);
		} else {
			fileNameWithoutExtension = fileName;
		}
		return fileNameWithoutExtension;
	}

	public static void writeSxssfFileByPath(String directoryName, String filePath, SXSSFWorkbook sxssfWorkBook) {

		File directory = new File(directoryName);
		if (!directory.exists()) {
			directory.mkdirs();
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(filePath);
			sxssfWorkBook.write(fileOut);
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static File[] getTheFilesInOlderOrder(String filePath, String ext) {
		File dir = new File(filePath);
		FileFilter fileFilter = new WildcardFileFilter("*." + ext);
		File[] files = dir.listFiles(fileFilter);

		if (files.length > 0) {
			/** The oldest file comes first **/
			Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
		}

		return files;
	}
	
	public static Date yesterday() {
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, -1);
	    return cal.getTime();
	}
	
	public static WebElement waitForElementPresent(WebDriver driver, final By selector, int timeOutInSeconds) {
		WebElement element = null;
		try {
			WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
			element = wait.until(ExpectedConditions.presenceOfElementLocated(selector));

		} catch (Exception e) {
			logger.error("waitForElementVisible method::" + e.getMessage(), e);
		}
		return element;
	}
	
	public static FluentWait<WebDriver> getFluentWaitObject(WebDriver driver, int timeOutInSeconds, int pollingTimeInSeconds){
		FluentWait<WebDriver> fluentwait = new FluentWait<>(driver).withTimeout(timeOutInSeconds, TimeUnit.SECONDS)
				.pollingEvery(pollingTimeInSeconds, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
		return fluentwait;
	}
	
	public static String getCarPolicyProperty(String key) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			URL imageURL = UtilityFile.class.getResource("/properties/carpolicy.properties");
			prop.load(imageURL.openStream());

		} catch (IOException ex) {
			logger.error("Error in getUploadProperty  Method" + ex.getMessage(), ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.error("Error in getUploadProperty  Method" + e.getMessage(), e);
				}
			}
		}
		return prop.getProperty(key);
	}
	
	public static String getOmniDocProperty(String key) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			URL propURL = UtilityFile.class.getResource("/properties/omnidoc.properties");
			prop.load(propURL.openStream());

		} catch (IOException ex) {
			logger.error("Error in getOmniDocProperty  Method" + ex.getMessage(), ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.error("Error in getOmniDocProperty  Method" + e.getMessage(), e);
				}
			}
		}
		return prop.getProperty(key);
	}
	
	
	public static Connection getLocalRPAConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException{
		Connection conn = null;
		DriverManager.registerDriver(
				(Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		return conn;
	}
	
	public static void printHeapDetails(String phase){
		int mb = 1024*1024;

		//Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();

		logger.info("##### Heap utilization statistics [MB] ##### before the start of pahse ::"+phase);

		//Print used memory
		logger.info("Used Memory:"
			+ (runtime.totalMemory() - runtime.freeMemory()) / mb);

		//Print free memory
		logger.info("Free Memory:"
			+ runtime.freeMemory() / mb);

		//Print total available memory
		logger.info("Total Memory:" + runtime.totalMemory() / mb);

		//Print Maximum available memory
		logger.info("Max Memory:" + runtime.maxMemory() / mb);
	}
	
	

	public static String getGridUploadProperty(String key) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			URL propURL = UtilityFile.class.getResource("/properties/grid.properties");
			prop.load(propURL.openStream());

		} catch (IOException ex) {
			logger.error("Error in getGridUploadProperty  Method" + ex.getMessage(), ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.error("Error in getGridUploadProperty  Method" + e.getMessage(), e);
				}
			}
		}
		return prop.getProperty(key);
	}
	
	public static Workbook workBookCreation(String FileName) throws EncryptedDocumentException, InvalidFormatException, IOException
	{
		Workbook workbook=null;
		InputStream excelFileToRead = new FileInputStream(FileName);
		workbook = WorkbookFactory.create(excelFileToRead);
		return workbook;
	}
	
	
	public static Connection getGridDbConnection()
			throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.GRID_DATASOURCE_URL);

		String username = UtilityFile.getDatabaseProperty(RPAConstants.GRID_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.GRID_DATASOURCE_PASSWORD);
		return conn = DriverManager.getConnection(connInstance, username, password);

	}
	public static boolean isRowEmpty(Row row) {
		for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
			Cell cell = row.getCell(c);
			if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
				return false;
			}
		}
		return true;
	}
	public static boolean isNumeric(String str)
	{
	  return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
	

	public static List<String> removeDuplicates(List<String> datalist) {
		List<String> al = datalist;
		// add elements to al, including duplicates
		Set<String> hs = new HashSet<>();
		hs.addAll(al);
		al.clear();
		al.addAll(hs);
		return al;
	}
	
	
	public static Date getDateObjWithFrequency(int frequency ) {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -frequency);
		Date date = cal.getTime();
		return date;
		}
	
	
public static String FileMovementMethod(String BaseFilePath, File file, String NewPath) throws IOException {
		
		logger.info("File FileMovementMethod :::::" + file.getName());
		String fileSuffix = new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date());
		String ext = FilenameUtils.getExtension(file.getName());
		File Path = new File(BaseFilePath + NewPath);

		if (!Path.exists()) {
			Path.mkdirs();
		}
		File newFile=new File(Path.getAbsolutePath() + RPAConstants.SLASH
				+ file.getName().concat("__").replace("." + ext, "").concat(fileSuffix).concat("." + ext));
		file.renameTo(newFile);
		// FileUtils.moveFileToDirectory(file, Path,true);
		logger.info("File Successfully Moved to the  Location" + newFile.getAbsolutePath());
		return newFile.getAbsolutePath();

	}

public static Workbook autoSizeColumns(Workbook workbook) {
    int numberOfSheets = workbook.getNumberOfSheets();
    for (int i = 0; i < numberOfSheets; i++) {
        Sheet sheet = workbook.getSheetAt(i);
        if (sheet.getPhysicalNumberOfRows() > 0) {
            Row row = sheet.getRow(0);
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                int columnIndex = cell.getColumnIndex();
                sheet.autoSizeColumn(columnIndex);
            }
        }
    }
    return workbook;
}


public static String getOmniDocLiveProperty(String key) {
	Properties prop = new Properties();
	InputStream input = null;
	try {
		URL propURL = UtilityFile.class.getResource("/properties/omnidocLive.properties");
		prop.load(propURL.openStream());

	} catch (IOException ex) {
		logger.error("Error in getOmniDocProperty  Method" + ex.getMessage(), ex);
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				logger.error("Error in getOmniDocProperty  Method" + e.getMessage(), e);
			}
		}
	}
	return prop.getProperty(key);
}

/*public CellStyle createCellStyle(Workbook workbook)
{
HSSFCellStyle style=(HSSFCellStyle) workbook.createCellStyle();
style.setBorderBottom();
style.setBorderTop(HSSFCellStyle.BORDER_THIN);
style.setBorderRight(HSSFCellStyle.BORDER_THIN);
style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
return style;
}*/


		/**
		 * Waits for a new file to be downloaded with a file watcher
		 */
		@SuppressWarnings("unchecked")
		public static File WaitForNewFile(Path folder, String extension, int timeout_sec) throws InterruptedException, IOException {
		    long end_time = System.currentTimeMillis() + timeout_sec * 1000;
		    try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
		        folder.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
		        for (WatchKey key; null != (key = watcher.poll(end_time - System.currentTimeMillis(), TimeUnit.MILLISECONDS)); key.reset()) {
		            for (WatchEvent<?> event : key.pollEvents()) {
		                File file = folder.resolve(((WatchEvent<Path>)event).context()).toFile();
		                if (file.toString().toLowerCase().endsWith(extension.toLowerCase()))
		                    return file;
		            }
		        }
		    }
		    return null;
		}
		
		public static boolean isFileDownload_ExistAndRename(String dirPath, String ext,boolean isRenameNeeded,String destinationPathWithName) {
			logger.info("isFileDownload_ExistAndRename - source directory :: "+dirPath);
			boolean flag = false;
			int counter  = 0;
			String currentFileName = "";
			try {

				File dir = new File(dirPath);
				File[] files = dir.listFiles();
				if (files == null || files.length == 0) {
					flag = false;

				}
				logger.info("isFileDownload_ExistAndRename - files.length :: "+files.length);

				for (int i = 0; i < files.length; i++) {
					logger.info("isFileDownload_ExistAndRename - File availeble in folder : "+files[i].getName());
					logger.info("isFileDownload_ExistAndRename - contains extension  : "+files[i].getName().contains(ext));
					if (files[i].getName().contains(ext)) {
						do {
							counter++;
							Thread.sleep(3000);
					    } while( !org.apache.commons.io.FileUtils.listFiles(dir, new String[]{"crdownload"}, false).isEmpty() && counter<20 );
						
						if(isRenameNeeded){
							if(files[i].getName().contains("crdownload")){
								if (files[i].getAbsolutePath().indexOf(".") != -1) {
									int index = files[i].getAbsolutePath().lastIndexOf(".");
									currentFileName = files[i].getAbsolutePath().substring(0, index);
								} 
								logger.info("is File "+files[i].getName()+" Renamed ??"+new File(currentFileName).renameTo(new File(destinationPathWithName)));
							}else
								logger.info("is File "+files[i].getName()+" Renamed ??"+files[i].renameTo(new File(destinationPathWithName)));
							
							
						}
						flag = true;
					}

				}

			} catch (Exception e) {
				logger.error("Error in isFileDownload_Exist  Method" + e.getMessage(), e);
			}
			return flag;
		}
		
		private static final String[] formats = { 
                "MMMM/dd/yyyy hh:mm:ss","yyyy-MM-dd'T'HH:mm:ss'Z'",   "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ss",      "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mm:ss", 
                "MM/dd/yyyy HH:mm:ss",        "MM/dd/yyyy'T'HH:mm:ss.SSS'Z'", 
                "MM/dd/yyyy'T'HH:mm:ss.SSSZ", "MM/dd/yyyy'T'HH:mm:ss.SSS", 
                "MM/dd/yyyy'T'HH:mm:ssZ",     "MM/dd/yyyy'T'HH:mm:ss", 
                "yyyy:MM:dd HH:mm:ss",        "yyyyMMdd",
                "dd/MM/yyyy"};
		
		public static Date parseAnyDateFormat(String d) throws Exception {
			Date date = null;
			 if (d != null) {
		            for (String parse : formats) {
		                SimpleDateFormat sdf = new SimpleDateFormat(parse);
		                try {
		                	date = sdf.parse(d);
		                    return date;
		                } catch (ParseException e) {

		                }
		            }
		            if(date==null)
						 throw new Exception("Not Able to Parse date String ::" + d +" Please add date format suitable for this date " );  
		        }
			 
			return date;
			 
		}
		
		
		public static void acceptSecurityAlert(WebDriver driver) {
			 { /* works fine! ! */
			      System.setProperty("java.awt.headless", "false");
			      System.out.println(java.awt.GraphicsEnvironment.isHeadless());
			      /* ---> prints true */
			    }
		    //Keep checking every 7 seconds for the 'Security Warning'
		   Wait<WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(1000, TimeUnit.SECONDS).pollingEvery(10, TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
		//	Wait<WebDriver> wait = new WebDriverWait(driver, 30);
			
		    //Wait until the 'Security Warning' appears
		   // boolean isTrue = wait.until(new Function<WebDriver, Boolean>(){
			wait.until(new Function<WebDriver, Boolean>(){
		        //implement interface method
		        public Boolean apply(WebDriver driver) {
		            try {                                       
		                char[] buffer = new char[1024 * 2];
		                
		                HWND hwnd = User32.INSTANCE.GetForegroundWindow();
		                
		                hwnd = User32.INSTANCE.FindWindow(null, "User Account Control");
		                User32.INSTANCE.SetForegroundWindow(hwnd);  
		                
		                User32.INSTANCE.GetWindowText(hwnd, buffer, 1024);
		                //System.out.println("Active window title: " + Native.toString(buffer));                    
System.out.println("title  ::"+Native.toString(buffer));
		                //Check for 'Security Warning' window
		                if(Native.toString(buffer).equalsIgnoreCase("User Account Control")){

		                    //After 'Security Warning' window appears, use TAB key to go to 'Continue' button and press ENTER key.
		                    //System.out.println("Pressing keys...");       
		                	 User32.INSTANCE.GetWindowText(hwnd, buffer, 1024);
		                	 System.out.println("isheadless before try::"+java.awt.GraphicsEnvironment.isHeadless());
		                    Robot robot = new Robot();
		                    
		                   // System.setProperty("java.awt.headless", "true");
		                   
		                    User32.INSTANCE.SetForegroundWindow(hwnd);
		                    System.out.println("title 2222 ::"+Native.toString(buffer));
		                   robot.delay(5000);    
		                   robot.keyPress(KeyEvent.VK_LEFT);
		                   robot.delay(100);
		                   robot.keyRelease(KeyEvent.VK_LEFT);
		                   robot.delay(100);
		                   robot.keyPress(KeyEvent.VK_ENTER);
		                   robot.delay(100);
		                   robot.keyRelease(KeyEvent.VK_ENTER);
		                   robot.delay(100);
		                    
		                   robot.keyPress(KeyEvent.VK_TAB); robot.delay(200);
		                    robot.keyRelease(KeyEvent.VK_TAB); robot.delay(200);

		                    robot.keyPress(KeyEvent.VK_TAB); robot.delay(200);
		                    robot.keyRelease(KeyEvent.VK_TAB); robot.delay(200);

		                    robot.keyPress(KeyEvent.VK_ENTER); robot.delay(200);
		                    robot.keyRelease(KeyEvent.VK_ENTER); robot.delay(200);
		                    
		                   // System.setProperty("java.awt.headless", "false");
		                    return true;
		                }
		                return null;
		            }catch(Exception e) {
		                System.out.println("Exception!");
		                e.printStackTrace();
		            } 
		            return null;
		        }
		    });
		}
		
		
		/*public static void waitForPageLoad(WebDriver driver) {

			Wait<WebDriver> wait = new FluentWait<>(driver)
				    .withTimeout(60, TimeUnit.SECONDS)
				    .pollingEvery(5, TimeUnit.SECONDS)
				    .ignoring(NoSuchElementException.class);

				wait.until(new Function<WebDriver, Boolean>() {
				  @Override
				  public Boolean apply(WebDriver driver) {
				    return driver.findElement(By.cssSelector("my-css-selector")).getText().contains("name");
				  }
				});
		}*/
		
		public static String getDTCDocProperty(String key) {
			Properties prop = new Properties();
			InputStream input = null;
			try {
				URL propURL = UtilityFile.class.getResource("/properties/omnidocDTC.properties");
				prop.load(propURL.openStream());

			} catch (IOException ex) {
				logger.error("Error in getOmniDocProperty  Method" + ex.getMessage(), ex);
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						logger.error("Error in getOmniDocProperty  Method" + e.getMessage(), e);
					}
				}
			}
			return prop.getProperty(key);
		}
		
		public static String getDTCDocLiveProperty(String key) {
			Properties prop = new Properties();
			InputStream input = null;
			try {
				URL propURL = UtilityFile.class.getResource("/properties/omnidocDTCLive.properties");
				prop.load(propURL.openStream());

			} catch (IOException ex) {
				logger.error("Error in getOmniDocProperty  Method" + ex.getMessage(), ex);
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						logger.error("Error in getOmniDocProperty  Method" + e.getMessage(), e);
					}
				}
			}
			return prop.getProperty(key);
		}
		
		
		public static boolean isFileDownload_ExistAndRenameWithMultiExtensionCheck(String dirPath, String ext,String ext1,boolean isRenameNeeded,String destinationPathWithName) {
			logger.info("isFileDownload_ExistAndRename - source directory :: "+dirPath +" - destination directory :: "+destinationPathWithName);
			boolean flag = false;
			try {

			File dir = new File(dirPath);
			File[] files = dir.listFiles();
			if (files == null || files.length == 0) {
			flag = false;

			}
			logger.info("isFileDownload_ExistAndRename - files.length :: "+files.length);

			for (int i = 0; i < files.length; i++) {
			logger.info("isFileDownload_ExistAndRename - File availeble in folder : "+files[i].getName());
			logger.info("isFileDownload_ExistAndRename - contains extension  : "+files[i].getName().contains(ext));
			if (files[i].getName().contains(ext) || files[i].getName().contains(ext1) ) {
			if(isRenameNeeded){
			logger.info("is File "+files[i].getName()+" Renamed ??"+files[i].renameTo(new File(destinationPathWithName)));
			}
			flag = true;
			}

			}

			} catch (Exception e) {
			logger.error("Error in isFileDownload_Exist  Method" + e.getMessage(), e);
			}
			return flag;
			}
		
		public static String getAgentProperty(String key) {
			Properties prop = new Properties();
			InputStream input = null;
			try {
				URL propURL = UtilityFile.class.getResource("/properties/agent.properties");
				prop.load(propURL.openStream());

			} catch (IOException ex) {
				logger.error("Error in getAgentProperty  Method" + ex.getMessage(), ex);
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						logger.error("Error in getAgentProperty  Method" + e.getMessage(), e);
					}
				}
			}
			return prop.getProperty(key);
		}
		
		
		public static String getVIRProperty(String key) {
			Properties prop = new Properties();
			InputStream input = null;
			try {
				URL propURL = UtilityFile.class.getResource("/properties/vir.properties");
				prop.load(propURL.openStream());

			} catch (IOException ex) {
				logger.error("Error in getBatchProperty  Method" + ex.getMessage(), ex);
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						logger.error("Error in getBatchProperty  Method" + e.getMessage(), e);
					}
				}
			}
			return prop.getProperty(key);
		}
}
