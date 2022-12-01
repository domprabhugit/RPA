/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.camel.processors.common.ChromeDriverHeadless;
import com.rpa.constants.RPAConstants;
import com.rpa.model.ApplicationConfiguration;
import com.rpa.model.BusinessProcess;
import com.rpa.model.TransactionInfo;
import com.rpa.service.CommonService;
import com.rpa.service.process.ProcessService;
import com.rpa.util.UtilityFile;

public class DTCPolicyExtracter implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(DTCPolicyExtracter.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	ChromeDriverHeadless chromeDriverHeadless;

	@Autowired
	WebDriver driver;

	@SuppressWarnings("static-access")
	@Override
	public void process(Exchange exchange)
			throws Exception {
		logger.info("*********** inside Camel Process of dtcPolicyExtracter Called ************");
		logger.info("BEGIN : dtcPolicyExtracter Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		chromeDriverHeadless = applicationContext.getBean(ChromeDriverHeadless.class);
		driver = chromeDriverHeadless.getNewChromeDriverWithCustomDownloadPath(UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("dtc.download.policy.temppdf.location"));
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.AUTOMATER);
		DTCPolicyExtracter dtcPolicyExtracter = new DTCPolicyExtracter();
		exchange.getIn().setHeader("chromeDriver_firstgen", driver);
		dtcPolicyExtracter.doProcess(dtcPolicyExtracter, exchange, transactionInfo, driver);
		logger.info("*********** inside Camel Process of dtcPolicyExtracter Processor Ended ************");
	}

	public void doProcess(DTCPolicyExtracter dtcPolicyExtracter, Exchange exchange,
			TransactionInfo transactionInfo, WebDriver driver)
			throws Exception {
		logger.info("BEGIN : dtcPolicyExtracter Processor - doProcess Method Called  ");

		doDtcPdfExtraction(driver, transactionInfo);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : dtcPolicyExtracter Processor - doProcess Method Ended  ");
	}

	public boolean doDtcPdfExtraction(WebDriver driver, TransactionInfo transactionInfo) throws Exception {
		logger.info("Processor - doDtcPdfExtraction()  called ");
		applicationContext = SpringContext.getAppContext();
		ProcessService processService = applicationContext.getBean(ProcessService.class);
		CommonService commonService = applicationContext.getBean(CommonService.class);

		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
				RPAConstants.APPID_DTC);

		if (applicationDetails == null) {
			logger.error("Error in doDtcPdfExtraction :: Application details not configured for process name :: "
					+ transactionInfo.getProcessName());
			return false;
		} else {

			driver.get(applicationDetails.getUrl());
			logger.info("dtcPolicyExtracter  - doDtcPdfExtraction curent url after alm get url :: " + driver.getCurrentUrl());
			String userName = applicationDetails.getUsername();
			String password = applicationDetails.getPassword();
			String FirstGenUrl = applicationDetails.getUrl();
			if (driver.getCurrentUrl().equals(FirstGenUrl)) {
				logger.info("dtcPolicyExtracter  - DTC current url :: " + driver.getCurrentUrl());
				
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
					logger.info("dtcPolicyExtracter DTC application logged in successfully");
				}
				
				List<String> list = new ArrayList<>();
				
				Map<String,String> map = new HashMap<>();
				
				map.put("0","carInsuranceXgenDownloadV5");
				map.put("1","carInsuranceBundledXgenDownloadV5");
				map.put("2","carStandAloneXgenDownloadV5");
				map.put("3","carLiabilityOnlyXgenDownloadV5");
				map.put("4","TwowheelerXgenDownload");
				map.put("5","TwowheelerBundledXgenDownload");
				map.put("6","TwowheelerXgenDownloadStandalone");
				map.put("7","TwowheelerXgenDownloadLiabilityOnly");
				map.put("8","TravelPoliciesXGenDownload");
				map.put("9","TravelSecureXGenDownload-Asia");
				map.put("10","TravelSecureXGenDownload-Leisure Trip");
				map.put("11","TravelSecureXGenDownload-Student");
				map.put("12","TravelSecureXGenDownload-Senior Citizen");
				map.put("13","TravelSecureXGenDownload-Multi Trip");
				
				
				((JavascriptExecutor) driver).executeScript("document.getElementsById('from'[0].removeAttribute('readonly');");
			    WebElement dateFrom = driver.findElement(By.id("from"));
			    dateFrom.clear();
			    dateFrom.sendKeys(UtilityFile.createSpecifiedDateFormat(RPAConstants.dd_slash_MM_slash_yyyy));
			    
			    ((JavascriptExecutor) driver).executeScript("document.getElementsById('from'[0].removeAttribute('readonly');");
			    WebElement dateTo = driver.findElement(By.id("to"));
			    dateTo.clear();
			    dateTo.sendKeys(UtilityFile.createSpecifiedDateFormat(RPAConstants.dd_slash_MM_slash_yyyy));
				
			    FluentWait<WebDriver> fluentWaitForSelect = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
						.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
			    fluentWaitForSelect
						.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='partner']")));
			    FluentWait<WebDriver> fluentWait = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
						.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
				fluentWait.until(ExpectedConditions.presenceOfElementLocated(
						(By.xpath("//*[@id='partner']/option[@value='All']"))));
				Select requestTypeSelect = new Select(driver.findElement(By.xpath("//*[@id='partner']")));
				requestTypeSelect.selectByValue("All");
			    
				for (Map.Entry<String,String> entry : map.entrySet()){
					
					downloadReport(entry,driver);
					
				}
				
			}
		    
			logger.info("Processor - dtcPolicyExtracter - END doDtcPdfExtraction() ");
			return true;

		}

	}

	private void downloadReport(Entry<String, String> entry, WebDriver driver) throws UnsupportedEncodingException, URISyntaxException, InterruptedException, IOException {
		
		 FluentWait<WebDriver> fluentWaitForSelect = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
					.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
		    fluentWaitForSelect
					.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='reportTypeId']")));
		    FluentWait<WebDriver> fluentWait = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
					.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
			fluentWait.until(ExpectedConditions.presenceOfElementLocated(
					(By.xpath("//*[@id='partner']/option[@value='"+entry.getValue()+"']"))));
			Select requestTypeSelect = new Select(driver.findElement(By.xpath("//*[@id='reportTypeId']")));
			requestTypeSelect.selectByValue(entry.getValue());
			
			
			File newFile = new File(UtilityFile.getCodeBasePath()
					+ UtilityFile.getBatchProperty("dtc.download.policy.upload.location") + UtilityFile
							.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY).toString());
			if (!newFile.exists()) {
				newFile.mkdirs();
			}
			
			saveInLocalPath(driver,UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("dtc.download.policy.upload.location"));
			
		
		/*switch(entry.getKey())
		{
		   // case statements
		   // values must be of same type of expression
		   case "0" :
		      // Statements
		      break; // break is optional
		   
		   case "1" :
		      // Statements
		      break; // break is optional
		      
		   case "2" :
			      // Statements
			      break;
		   case "3" :
			      // Statements
			      break;
		
		   case "4" :
			      // Statements
			      break;

		   case "5" :
			      // Statements
			      break;

		   case "6" :
			      // Statements
			      break;

		   case "7" :
			      // Statements
			      break;

		   case "8" :
			      // Statements
			      break;

		   case "9" :
			      // Statements
			      break;

		   case "10" :
			      // Statements
			      break;
			      
		   case "11" :
			      // Statements
			      break;

		   case "12" :
			      // Statements
			      break;
			      
		   case "13" :
			      // Statements
			      break;
		   
		   // We can have any number of case statements
		   // below is default statement, used when none of the cases is true. 
		   // No break is needed in the default case.
		   default : 
		      // Statements
		}*/
		
		
	}

	public WebElement waitForElementVisible(WebDriver driver, final By selector, int timeOutInSeconds) {
		WebElement element = null;
		WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));

		return element;
	}
	
	public String getCurrentFrameName(WebDriver driver) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		String CurrentFrame = js.executeScript("return self.name").toString();
		return CurrentFrame;
	}
	
	
	public boolean saveInLocalPath(WebDriver driver, String pdfPath)
			throws URISyntaxException, InterruptedException, IOException {

		boolean exist = false, isDeleted = false;

		isDeleted = UtilityFile.isFileDeleted(UtilityFile.getCodeBasePath()
				+ UtilityFile.getCarPolicyProperty("dtc.download.policy.temppdf.location"), ".xls");
		if (isDeleted) {
			logger.info(" dtcPolicyExtracter : saveInLocalPath - cleared temp_pdf folder for new Download");
		} else {
			logger.info(" dtcPolicyExtracter : saveInLocalPath - no file available in temp folder to delete");
		}

		int i = 0;
		do {
			
			/* Execute */
			WebElement submit = UtilityFile.waitForElementPresent(driver, By.xpath("/html/body/div[1]/div/div/form/div/div/div/a"), 60);
			submit.click();

			logger.info(
					"dtcPolicyExtracter : saveInLocalPath - submit btn clicked");
			
			
			
			//Thread.sleep(8000);

			logger.info(
					"dtcPolicyExtracter : saveInLocalPath - isFileDownload_ExistAndRename method called here -- download try number :: "
							+ (i + 1));
			exist = UtilityFile.isFileDownload_ExistAndRenameWithMultiExtensionCheck(
					UtilityFile.getCodeBasePath()
					+ UtilityFile.getCarPolicyProperty("dtc.download.policy.temppdf.location"),
					".xls",".xlsx", true, pdfPath);

			i++;
			if (i == 3) {
				logger.info(
						"dtcPolicyExtracter : saveInLocalPath - file not download after 24 secs of wait ");
				return false;
			}

		} while (exist != true);
		return true;

	}
	

}
