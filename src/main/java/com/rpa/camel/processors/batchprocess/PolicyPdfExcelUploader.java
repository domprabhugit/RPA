/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.camel.processors.common.ChromeDriverHeadless;
import com.rpa.camel.processors.common.HashMapToExcelLoader;
import com.rpa.constants.RPAConstants;
import com.rpa.model.ApplicationConfiguration;
import com.rpa.model.BusinessProcess;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.PolicyPdfMailRetrigger;
import com.rpa.repository.PolicyPdfMailRetriggerRepository;
import com.rpa.service.CommonService;
import com.rpa.service.EmailService;
import com.rpa.service.process.ProcessService;
import com.rpa.util.UtilityFile;

public class PolicyPdfExcelUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(PolicyPdfExcelUploader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	HashMapToExcelLoader hashMapToExcelLoader;

	@Autowired
	ChromeDriverHeadless chromeDriverHeadless;

	@Autowired
	PolicyPdfMailRetriggerRepository policyPdfMailRetriggerRepository;

	@SuppressWarnings("static-access")
	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("*********** inside Camel Process of policyPdfExcelUploader Called ************");
		logger.info("BEGIN : policyPdfExcelUploader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		hashMapToExcelLoader = applicationContext.getBean(HashMapToExcelLoader.class);
		policyPdfMailRetriggerRepository = applicationContext.getBean(PolicyPdfMailRetriggerRepository.class);
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase("Excel_Generator");
		transactionInfo.setProcessStatus(RPAConstants.Success);
		driver = chromeDriverHeadless.getNewMarutiChromeDriver();
		exchange.getIn().setHeader("chromeDriver_Dtc", driver);
		PolicyPdfExcelUploader policyPdfExcelUploader = new PolicyPdfExcelUploader();
		policyPdfExcelUploader.doProcess(policyPdfExcelUploader, exchange, transactionInfo, hashMapToExcelLoader,
				driver, policyPdfMailRetriggerRepository);
		logger.info("*********** inside Camel Process of policyPdfExcelUploader Processor Ended ************");
	}

	public void doProcess(PolicyPdfExcelUploader policyPdfExcelUploader, Exchange exchange,
			TransactionInfo transactionInfo, HashMapToExcelLoader hashMapToExcelLoader, WebDriver driver,
			PolicyPdfMailRetriggerRepository policyPdfMailRetriggerRepository) throws Exception {
		logger.info("BEGIN : policyPdfExcelUploader Processor - doProcess Method Called  ");
		transactionInfo.setProcessPhase("Quote_fetcher");
		logger.info("BEGIN : policyPdfExcelUploader Processor - Current Phase :: Quote_fetcher");
		PolicyPdfMailRetrigger policyPdfMailRetrigger = new PolicyPdfMailRetrigger();
		logger.info("BEGIN : policyPdfExcelUploader Processor - policyPdfMailRetrigger object created  ");
		policyPdfMailRetrigger.setTransactionRefNo(transactionInfo.getId());
		policyPdfMailRetrigger.setIsExcelCreated(RPAConstants.N);
		policyPdfMailRetrigger.setIsExcelUploaded(RPAConstants.N);
		policyPdfMailRetrigger.setCreatedTime(new Date());
		policyPdfMailRetrigger.setQuoteCount("0");
		policyPdfMailRetriggerRepository.save(policyPdfMailRetrigger);
		logger.info("BEGIN : policyPdfExcelUploader Processor - entry made in  policyPdfMailRetrigger table ");

		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DATASOURCE_PASSWORD);
		try {
			conn = DriverManager.getConnection(connInstance, username, password);
			logger.info("policyPdfExcelUploader - connection object created :: " + conn);

			getQuoteIdToBeRetriggered(driver, transactionInfo, exchange, conn, hashMapToExcelLoader,
					policyPdfMailRetrigger, policyPdfMailRetriggerRepository);

			exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
			logger.info(
					"BEGIN : policyPdfExcelUploader Processor - before set trnasaction info object in exchange object ");
		} finally {
			if (conn != null)
				conn.close();
		}
		logger.info("BEGIN : policyPdfExcelUploader Processor - doProcess Method Ended  ");
	}

	public boolean getQuoteIdToBeRetriggered(WebDriver driver, TransactionInfo transactionInfo, Exchange exchange,
			Connection conn, HashMapToExcelLoader hashMapToExcelLoader, PolicyPdfMailRetrigger policyPdfMailRetrigger,
			PolicyPdfMailRetriggerRepository policyPdfMailRetriggerRepository) throws Exception {
		logger.info("policyPdfExcelUploader - BEGIN getQuoteIdToBeRetriggered()  called ");

		Calendar currTime = Calendar.getInstance();
		int hour = currTime.get(Calendar.HOUR_OF_DAY);
		logger.info("policyPdfExcelUploader hour of the day - -> " + hour);

		String sql = "";

		if (hour >= 0 && hour < 2) {
			sql = "SELECT quote_id quoteid FROM d_policy_purchased_details WHERE product IN ('MOTORSHIELDONLINE', 'TWOWHEELER') AND buy_date BETWEEN TO_DATE (to_char(sysdate-1,'mm/dd/yyyy')|| '00:00:00', 'MM/DD/YYYY HH24:MI:SS') AND TO_DATE (to_char(sysdate,'mm/dd/yyyy')|| '23:59:59','MM/DD/YYYY HH24:MI:SS') AND quote_id NOT IN (SELECT quote_id FROM mailstatus b WHERE mail_type = 'PolicyPurchasedPDFMail') AND quote_id NOT IN (SELECT quote_id FROM  D_PURCHASED_EMAIL_TRIGGER where status='Success') ";
		} else {
			sql = "SELECT quote_id quoteid FROM d_policy_purchased_details WHERE product IN ('MOTORSHIELDONLINE', 'TWOWHEELER') AND buy_date BETWEEN TO_DATE (to_char(sysdate,'mm/dd/yyyy')|| '00:00:00', 'MM/DD/YYYY HH24:MI:SS') AND TO_DATE (to_char(sysdate,'mm/dd/yyyy')|| '23:59:59','MM/DD/YYYY HH24:MI:SS') AND quote_id NOT IN (SELECT quote_id FROM mailstatus b WHERE mail_type = 'PolicyPurchasedPDFMail') AND quote_id NOT IN (SELECT quote_id FROM  D_PURCHASED_EMAIL_TRIGGER where status='Success') ";
		}

		logger.info("policyPdfExcelUploader - sql query to quotes policies for today's date :: " + sql);

		/* Create Map for Excel Data */
		HashMap<String, Object[]> excel_data = new HashMap<String, Object[]>(); 
		
		int row_counter = 0;
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					row_counter = row_counter + 1;
					excel_data.put(Integer.toString(row_counter), new Object[] { rs.getString(1) });
				}
				if (rs != null)
					rs.close();
			}
			if (ps != null)
				ps.close();
		}

		logger.info("policyPdfExcelUploader - quuoteid count :: " + row_counter);
		policyPdfMailRetrigger.setQuoteCount(String.valueOf(row_counter));

		if (row_counter == 0) {
			logger.info("policyPdfExcelUploader - No quote fetched from Table");
			transactionInfo.setTransactionStatus("No Quote");
			transactionInfo.setTotalRecords("0");
			transactionInfo.setTotalSuccessRecords("0");
		} else {
			logger.info("policyPdfExcelUploader - Some quote fetched from Table");
			transactionInfo.setTotalRecords("1");
			transactionInfo.setTotalSuccessRecords("1");
		}

		transactionInfo.setProcessPhase("excel_generator");
		logger.info("BEGIN : policyPdfExcelUploader Processor - Current Phase :: excel_generator");

		if (row_counter > 0) {
			logger.info("policyPdfExcelUploader - In the process of creating Quote excel file");
			String path = UtilityFile.getCodeBasePath()
					+ UtilityFile.getCarPolicyProperty(RPAConstants.POLICY_PDFMAIL_PATH)
					+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY);
			
			logger.info("policyPdfExcelUploader - Path were excel file is to be created ::" + path);
			File folder = new File(path);
			folder.mkdirs();

			String fileName = new SimpleDateFormat("yyyyMMddHHmm'_quote.xls'").format(new Date());
			logger.info("policyPdfExcelUploader - filename ::" + fileName);
			policyPdfMailRetrigger.setFileName(fileName);
			policyPdfMailRetrigger.setFilePath(path);
			transactionInfo.setExternalTransactionRefNo(fileName);
			
			if (hashMapToExcelLoader.convertLoadHashMapValueInExcel(path + "/" + fileName, excel_data, "quote")) {
				logger.info("policyPdfExcelUploader - Excel File created with name ::" + fileName);
				policyPdfMailRetrigger.setIsExcelCreated(RPAConstants.Y);
			} else {
				logger.info("policyPdfExcelUploader - Excel File " + fileName + " not created with name ");
			}

			policyPdfMailRetrigger.setModifiedTime(new Date());
			policyPdfMailRetriggerRepository.save(policyPdfMailRetrigger);

			transactionInfo.setProcessPhase("excel_uploader");
			logger.info("BEGIN : policyPdfExcelUploader Processor - Current Phase :: excel_uploader");

			applicationContext = SpringContext.getAppContext();
			ProcessService processService = applicationContext.getBean(ProcessService.class);
			CommonService commonService = applicationContext.getBean(CommonService.class);

			BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
			ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
					RPAConstants.APPID_DTC);

			if (applicationDetails == null) {
				logger.error("Error in dtc login  :: Application details not configured for process name :: "
						+ transactionInfo.getProcessName());
				return false;
			} else {

				driver.get(applicationDetails.getUrl());
				logger.info("policyPdfExcelUploader - " + exchange.getProperty(RPAConstants.VEHICLE_TYPE)
						+ " current url is ::" + driver.getCurrentUrl());
				String userName = applicationDetails.getUsername();
				String password = applicationDetails.getPassword();

				WebElement userNameElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='userName']"),
						40);
				WebElement passwordElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='passWord']"),
						40);
				WebElement submit1Element = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='dvbody']/input[3]"), 40);
				if (userNameElement != null) {
					userNameElement.sendKeys(userName);
					passwordElement.sendKeys(password);
					submit1Element.click();

					try {

						// Menu link
						WebElement menuElement = UtilityFile.waitForElementPresent(driver,
								By.xpath("/html/body/div[1]/div/div/div[5]/div/div/ul/li[17]/a"), 40);
						menuElement.click();

						WebElement uploadElement = driver.findElement(By.id("PolicyPurchasedEmailTrigger"));

						uploadElement.sendKeys(removeFirstLetterIfStartsWithSlash(path + "//" + fileName));

						WebElement submitBtn = UtilityFile.waitForElementPresent(driver,
								By.xpath("//*[@id='submitbtn']"), 40);
						try {
							submitBtn.click();
						} catch (Exception e) {
							logger.error("Error after submit btn click :: " + e);
							WebDriverWait wait = new WebDriverWait(driver, 2700);
							wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loading")));
						}

						String result = "";
						if (isAlertPresent(driver)) {
							result = driver.switchTo().alert().getText();
							logger.info("alert text :: "+result);
							driver.switchTo().alert().accept();
						}

						if (result.equalsIgnoreCase("success")) {
							logger.info("Quote successfully uploaded");
							policyPdfMailRetrigger.setIsExcelUploaded(RPAConstants.Y);
						} else {
							policyPdfMailRetrigger.setIsExcelUploaded(RPAConstants.E);
							logger.info("Quote not uploaded");
						}
						policyPdfMailRetrigger.setModifiedTime(new Date());
						policyPdfMailRetriggerRepository.save(policyPdfMailRetrigger);

					} finally {

						// Logout link
						WebElement logoutBtn = UtilityFile.waitForElementPresent(driver,
								By.xpath("/html/body/div/div/div/div[2]/div[3]/div/ul/li/a"), 40);
						logoutBtn.click();

					}

				} else {
					EmailService emailService = (EmailService) applicationContext.getBean("emailService");
					emailService.carPolicyExtractionNotification(transactionInfo,
							"Unable to locate element - Please check whether the site is reachable", "DTC");
					transactionInfo.setProcessFailureReason("Unable to locate element");
					throw new Exception("Unable to locate element - Please check whether the site is reachable");
				}

			}
		}

		logger.info("policyPdfExcelUploader - getQuoteIdToBeRetriggered()  END ");
		return true;
	}

	public String removeFirstLetterIfStartsWithSlash(String letter) {
		if (letter.startsWith("/")) {
			letter = letter.substring(1);
		}
		if (letter.contains("/")) {
			letter = letter.replace("//", "/");
		}
		return letter;
	}

	public boolean isAlertPresent(WebDriver driver) {
		boolean foundAlert = false;
		WebDriverWait wait = new WebDriverWait(driver,
				0 /* timeout in seconds */);
		try {
			wait.until(ExpectedConditions.alertIsPresent());
			foundAlert = true;
		} catch (TimeoutException eTO) {
			foundAlert = false;
		}
		return foundAlert;
	}
}
