/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.ApplicationConfiguration;
import com.rpa.model.BusinessProcess;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.AutoInspektVIR;
import com.rpa.service.CommonService;
import com.rpa.service.process.ProcessService;
import com.rpa.service.processors.AutoInspektVirService;
import com.rpa.util.UtilityFile;

public class AutoInspektVirDocExtracter implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(AutoInspektVirDocExtracter.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	AutoInspektVirService autoInspektPolicyService;

	@Autowired
	private CommonService commonService;

	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("*********** inside Camel Process of autoInspektPolicyPdfExtracter Called ************");
		logger.info("BEGIN : autoInspektPolicyPdfExtracter Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		driver = (WebDriver) exchange.getIn().getHeader("chromeDriver_autoInspekt");
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.EXTRACTION);
		AutoInspektVirDocExtracter autoInspektPolicyPdfExtracter = new AutoInspektVirDocExtracter();
		autoInspektPolicyService = applicationContext.getBean(AutoInspektVirService.class);
		commonService = applicationContext.getBean(CommonService.class);
		autoInspektPolicyPdfExtracter.doProcess(autoInspektPolicyPdfExtracter, exchange, transactionInfo, driver, autoInspektPolicyService,
				commonService);
		logger.info("*********** inside Camel Process of autoInspektPolicyPdfExtracter Processor Ended ************");
	}

	public void doProcess(AutoInspektVirDocExtracter autoInspektPolicyPdfExtracter, Exchange exchange,
			TransactionInfo transactionInfo, WebDriver driver, AutoInspektVirService autoInspektPolicyService,
			CommonService commonService) throws Exception {
		logger.info("BEGIN : autoInspektPolicyPdfExtracter Processor - doProcess Method Called  ");

		getPolicyNumberListTobeExtractedFromLocalDb(driver, transactionInfo, autoInspektPolicyService, exchange,
				commonService);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : autoInspektPolicyPdfExtracter Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeExtractedFromLocalDb(WebDriver driver, TransactionInfo transactionInfo,
			AutoInspektVirService autoInspektPolicyService, Exchange exchange, CommonService commonService) throws Exception {
		logger.info(
				"Processor - autoInspektPolicyPdfExtracter - BEGIN getPolicyNumberListTobeExtractedFromLocalDb()  called ");
		
		WebDriverWait wait = new WebDriverWait(driver, 60);
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[@id='datatable-fixed-header_processing']/span[text()='Loading. Please wait...']")));
		
		applicationContext = SpringContext.getAppContext();
		ProcessService processService = applicationContext.getBean(ProcessService.class);

		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
				RPAConstants.APPID_AUTOINSPEKT);
		

		String hondaFolderPath = "",autoInspektTempPDFPath="";
		String phantomjsPath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getVIRProperty(RPAConstants.PHANTOMJS_DRIVER_FOLDER) + "/phantomjs";
		phantomjsPath = removeFirstLetterIfStartsWithSlash(phantomjsPath);

		String rasterizeJsPath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getVIRProperty(RPAConstants.CAR_POLICY_JS_FOLDER) + "/rasterize.js";
		rasterizeJsPath = removeFirstLetterIfStartsWithSlash(rasterizeJsPath);

		List<AutoInspektVIR> autoInspektPolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			autoInspektPolicyNoList = autoInspektPolicyService.findPdfToBeDownloaded(
					commonService.getThresholdFrequencyLevel(RPAConstants.AUTOINSPEKT_DOWNLOAD_THRESHOLD));
		} else {
			String startDate = UtilityFile.getVIRProperty(RPAConstants.AUTOINSPEKT_BACKLOG_STARTDATE);
			String endDate = UtilityFile.getVIRProperty(RPAConstants.AUTOINSPEKT_BACKLOG_ENDDATE);
			autoInspektPolicyNoList = autoInspektPolicyService.findPdfToBeDownloadedForBackLogPolicies(startDate, endDate,
					commonService.getThresholdFrequencyLevel(RPAConstants.AUTOINSPEKT_DOWNLOAD_THRESHOLD));
		}
		logger.info("autoInspektPolicyPdfExtracter - Unextracted Policies count ::" + autoInspektPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(autoInspektPolicyNoList.size()));

		int extractedPolicyCount = 0, extractedProposalCount = 0;
		String policyNo = "0", proposalNumber = "0", pdfPath = "", regNo ="",virNumber="";

		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			hondaFolderPath = UtilityFile.getCodeBasePath()
					+ UtilityFile.getVIRProperty(RPAConstants.AUTOINSPEKT_POLICY_FOLDER)
					+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY) + RPAConstants.UNDERSCORE
					+ RPAConstants.BACK_LOG;

		} else {
			hondaFolderPath = UtilityFile.getCodeBasePath()
					+ UtilityFile.getVIRProperty(RPAConstants.AUTOINSPEKT_POLICY_FOLDER)
					+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY);

		}

		autoInspektTempPDFPath = UtilityFile.getCodeBasePath() + UtilityFile.getVIRProperty("autoinspekt.doc.temppdf.location");
		File autoInspektTempPDFPathFolder = new File(autoInspektTempPDFPath);

		if (!autoInspektTempPDFPathFolder.exists())
			autoInspektTempPDFPathFolder.mkdirs();

		File hondaFolder = new File(hondaFolderPath);

		if (!hondaFolder.exists())
			hondaFolder.mkdirs();


		 
		for (AutoInspektVIR autoInspektPolicyObj : autoInspektPolicyNoList) {

			logger.info("autoInspektPolicyPdfExtracter - after switched to main window " + driver.getCurrentUrl());

			policyNo = autoInspektPolicyObj.getPolicyNo();
			proposalNumber = autoInspektPolicyObj.getProposalNumber();
			regNo = autoInspektPolicyObj.getRegistrationNumber();
			virNumber = autoInspektPolicyObj.getVirNumber();
			
			
			WebElement searchBox = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='global-search-query']"), 60);
			searchBox.clear();
			//searchBox.sendKeys("MH49AT0588");
			searchBox.sendKeys(regNo);
			
			WebElement searchSubmit = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='kick-global-search']"), 40);
			searchSubmit.click();
			
			Thread.sleep(2000);
			
			logger.info("autoInspektPolicyPdfExtracter - after policyNo input set " + driver.getCurrentUrl());

			logger.info("autoInspektPolicyPdfExtracter - reg No ::  " + regNo);

			try{

			if (autoInspektPolicyObj.getIsReportDownloaded() == null)
				autoInspektPolicyObj.setIsReportDownloaded(RPAConstants.N);

			/* policy Extraction */
			if (autoInspektPolicyObj.getIsReportDownloaded().equalsIgnoreCase(RPAConstants.N)) {
				
				logger.info("current url -- " + driver.getCurrentUrl());
				

				pdfPath = hondaFolderPath + "/report_" + virNumber + ".pdf";
				pdfPath = removeFirstLetterIfStartsWithSlash(pdfPath);
				
				if( driver.findElements(By.xpath("//*[@data-original-title='Download Report Report']")).size() > 0 ){
					if (saveAsPadf(driver, pdfPath)) {
						extractedPolicyCount++;
						logger.info("autoInspektPolicyPdfExtracter - Report PDF Downloaded for regNo :: " + regNo);

						autoInspektPolicyObj.setIsReportDownloaded(RPAConstants.Y);
						autoInspektPolicyObj.setReportPdfPath(pdfPath);
						autoInspektPolicyObj.setIsReportUploaded(RPAConstants.N);
					}
				}else{
					if( driver.findElements(By.xpath("//*[@id='datatable-fixed-header']/tbody/tr/td[contains(.,'No data')]")).size() > 0 ){
					String result = driver.findElement(By.xpath("//*[@id='datatable-fixed-header']/tbody/tr/td[contains(.,'No data')]")).getText();
						if(result.contains("No data")){
							logger.info("autoInspektPolicyPdfExtracter - No roecord for regNo :: " + regNo);
	
							autoInspektPolicyObj.setIsReportDownloaded(RPAConstants.R);
							//autoInspektPolicyObj.setReportPdfPath(pdfPath);
							autoInspektPolicyObj.setIsReportUploaded(RPAConstants.N);
						}
					}else{
						logger.info("autoInspektPolicyPdfExtracter - no record element not available ");
					}
				}
				
				/* To Download Policy PDF */
				/*if(driver!=null && !driver.getCurrentUrl().contains("VSPrintPolicy"))
					driver.close();*/
			} else {
				logger.info("autoInspektPolicyPdfExtracter - Report pdf already extracted for registration number :: " + regNo);
			}

		}catch(TimeoutException e){
			logger.info("Element not found -->"+e);
		}
			// autoInspektPolicyObj.setInwardCode(inwardCode);
			autoInspektPolicyService.save(autoInspektPolicyObj);

		}
		/* Total policy pdf extratced */
		transactionInfo.setTotalSuccessRecords(String.valueOf(extractedPolicyCount));
		logger.info("autoInspektPolicyPdfExtracter - Extracted policy count :: " + extractedPolicyCount);

		/* Total proposal pdf extratced */
		transactionInfo.setTotalSuccessUploads((String.valueOf(extractedProposalCount)));
		logger.info("autoInspektPolicyPdfExtracter - Extracted Proposal count :: " + extractedProposalCount);

		logger.info("autoInspektPolicyPdfExtracter - Current URL before logout try ::" + driver.getCurrentUrl());
		WebElement logoutMenu = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='wrapper']/div[1]/div[2]/div/ul[2]/li/a"), 40);
		logoutMenu.click();
		
		WebElement logout = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='wrapper']/div[1]/div[2]/div/ul[2]/li/ul/li[4]/a"), 40);
		logout.click();
		
		logger.info("autoInspektPolicyPdfExtracter - END getPolicyNumberListTobeExtractedFromLocalDb()");
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

	public boolean deleteFile(String filePath) {
		boolean isFileDeleted = false;
		File file = null;
		file = new File(filePath);
		isFileDeleted = file.delete();
		logger.info("autoInspektPolicyPdfExtracter - is File ::" + file.getName() + " Deleted ?? :: " + isFileDeleted);
		file = null;
		return true;
	}

	public boolean saveAsPadf(WebDriver driver, String pdfPath)
			throws URISyntaxException, InterruptedException, IOException {

		boolean exist = false, isDeleted = false;

		isDeleted = UtilityFile.isFileDeleted(
				UtilityFile.getCodeBasePath() + UtilityFile.getVIRProperty("autoinspekt.doc.temppdf.location"),
				".pdf");
		if (isDeleted) {
			logger.info(" autoInspektPolicyPdfExtracter : saveAsPadf - cleared temp_pdf folder for new Download");
		}else{
			logger.info(" autoInspektPolicyPdfExtracter : saveAsPadf - no file available in temp folder to delete");
		}

		int i = 0;
		do {

			logger.info("autoInspektPolicyPdfExtracter : saveAsPadf - isFileDownload_ExistAndRename method called here -- download try number :: "
					+ (i + 1));
			WebElement reportpdflement = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@data-original-title='Download Report Report']"), 40);
			if(reportpdflement!=null){
			FluentWait<WebDriver> fluentwaitProposal = UtilityFile.getFluentWaitObject(driver, 30, 200);
			fluentwaitProposal.until(ExpectedConditions.visibilityOf(reportpdflement));
			fluentwaitProposal.until(ExpectedConditions.elementToBeClickable(reportpdflement));
			reportpdflement.click();
			
			logger.info(" autoInspektPolicyPdfExtracter : saveAsPadf - after clicking proposal export pdf  button download try number "+ (i + 1));
			
			Thread.sleep(3000);

			logger.info("autoInspektPolicyPdfExtracter : saveAsPadf - isFileDownload_ExistAndRename method called here -- download try number :: "
					+ (i + 1));
			
			exist = UtilityFile.isFileDownload_ExistAndRename(
					UtilityFile.getCodeBasePath() + UtilityFile.getVIRProperty("autoinspekt.doc.temppdf.location"),
					".pdf", true, pdfPath);
			i++;
			if (i == 10) {
				logger.info("autoInspektPolicyPdfExtracter : saveAsPadf - file not download after 30 secs of wait ");
				return false;
			}

			}else{
				logger.error("autoInspektPolicyPdfExtracter : saveAsPadf - policy pdf element issue  ");
				return false;
			}
		} while (exist != true);
		return true;

	}
	
	public static WebElement waitForElementPresentWithOutErrorThrows(WebDriver driver, final By selector, int timeOutInSeconds) {
		WebElement element = null;
		try {
			WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
			element = wait.until(ExpectedConditions.presenceOfElementLocated(selector));

		} catch (Exception e) {
			//logger.error("waitForElementVisible method::" + e.getMessage(), e);
		}
		return element;
	}
	
	
}
