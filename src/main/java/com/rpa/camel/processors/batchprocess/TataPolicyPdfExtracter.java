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
import org.openqa.selenium.JavascriptExecutor;
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
import com.rpa.model.processors.TataPolicy;
import com.rpa.service.CommonService;
import com.rpa.service.process.ProcessService;
import com.rpa.service.processors.TataPolicyService;
import com.rpa.util.UtilityFile;

public class TataPolicyPdfExtracter implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(TataPolicyPdfExtracter.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	TataPolicyService tataPolicyService;

	@Autowired
	private CommonService commonService;
	
	int extractedPVPolicyCount = 0, extractedPVProposalCount=0, extractedCVPolicyCount=0, extractedCVProposalCount=0;
	ApplicationConfiguration pvApplicationDetails = null,cvApplicationDetails = null;
	List<TataPolicy> tataPVPolicyNoList = null;List<TataPolicy> tataCVPolicyNoList = null;

	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("*********** inside Camel Process of tataPolicyPdfExtracter Called ************");
		logger.info("BEGIN : tataPolicyPdfExtracter Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
			driver = (WebDriver) exchange.getIn().getHeader("chromeDriver_tatapv");
		else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
			driver = (WebDriver) exchange.getIn().getHeader("chromeDriver_tatacv");
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.EXTRACTION);
		TataPolicyPdfExtracter tataPolicyPdfExtracter = new TataPolicyPdfExtracter();
		tataPolicyService = applicationContext.getBean(TataPolicyService.class);
		commonService = applicationContext.getBean(CommonService.class);
		tataPolicyPdfExtracter.doProcess(tataPolicyPdfExtracter, exchange, transactionInfo, driver, tataPolicyService,
				commonService);
		logger.info("*********** inside Camel Process of tataPolicyPdfExtracter Processor Ended ************");
	}

	public void doProcess(TataPolicyPdfExtracter tataPolicyPdfExtracter, Exchange exchange,
			TransactionInfo transactionInfo, WebDriver driver, TataPolicyService tataPolicyService,
			CommonService commonService) throws Exception {
		logger.info("BEGIN : tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" Processor - doProcess Method Called  ");

		getPolicyNumberListTobeExtractedFromLocalDb(driver, transactionInfo, tataPolicyService, exchange,
				commonService);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeExtractedFromLocalDb(WebDriver driver, TransactionInfo transactionInfo,
			TataPolicyService tataPolicyService, Exchange exchange, CommonService commonService) throws Exception {
		logger.info(
				"Processor - tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - BEGIN getPolicyNumberListTobeExtractedFromLocalDb()  called ");

		
		if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
			extractedPVPolicyCount = 0; extractedPVProposalCount = 0;
		}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
			 extractedCVPolicyCount = 0; extractedCVProposalCount = 0;
		}
		
		applicationContext = SpringContext.getAppContext();
		ProcessService processService = applicationContext.getBean(ProcessService.class);

		if (exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)) {
			BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
			pvApplicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
					RPAConstants.APPID_TATA);
		}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){
			BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
			cvApplicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
					RPAConstants.APPID_TATA);
		}
			
		
		
		String mainWindowHandle = "";
		mainWindowHandle = driver.getWindowHandle();

		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
				tataPVPolicyNoList = tataPolicyService.findPdfToBeDownloadedPV(
						commonService.getThresholdFrequencyLevel(RPAConstants.TATA_DOWNLOAD_THRESHOLD));
			else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
				tataCVPolicyNoList = tataPolicyService.findPdfToBeDownloadedCV(
						commonService.getThresholdFrequencyLevel(RPAConstants.TATA_DOWNLOAD_THRESHOLD));
				
		} else {
			String startDate = UtilityFile.getCarPolicyProperty(RPAConstants.TATA_BACKLOG_STARTDATE);
			String endDate = UtilityFile.getCarPolicyProperty(RPAConstants.TATA_BACKLOG_ENDDATE);
			if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
				tataPVPolicyNoList = tataPolicyService.findPdfToBeDownloadedForBackLogPoliciesPV(startDate, endDate,
					commonService.getThresholdFrequencyLevel(RPAConstants.TATA_DOWNLOAD_THRESHOLD));
			else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
				tataCVPolicyNoList = tataPolicyService.findPdfToBeDownloadedForBackLogPoliciesCV(startDate, endDate,
						commonService.getThresholdFrequencyLevel(RPAConstants.TATA_DOWNLOAD_THRESHOLD));
		}
		
		if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
			logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Unextracted Policies count ::" + tataPVPolicyNoList.size());
			/* Total policies taken for extraction */
			transactionInfo.setTotalRecords(String.valueOf(tataPVPolicyNoList.size()));
			 
			tataPVPDFExtractionProcess(tataPVPolicyNoList,exchange,transactionInfo,pvApplicationDetails,mainWindowHandle,driver,tataPolicyService);
		}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){
			logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Unextracted Policies count ::" + tataCVPolicyNoList.size());
			/* Total policies taken for extraction */
			transactionInfo.setTotalRecords(String.valueOf(tataCVPolicyNoList.size()));
			 
			tataCVPDFExtractionProcess(tataCVPolicyNoList,exchange,transactionInfo,cvApplicationDetails,mainWindowHandle,driver,tataPolicyService);
		}
		
		
		/* Total policy pdf extratced */
		if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
			transactionInfo.setTotalSuccessRecords(String.valueOf(extractedPVPolicyCount));
			logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Extracted policy count :: " + extractedPVPolicyCount);
		}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
			transactionInfo.setTotalSuccessRecords(String.valueOf(extractedCVPolicyCount));
			logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Extracted policy count :: " + extractedCVPolicyCount);
		}
		

		/* Total proposal pdf extratced */
		if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
			transactionInfo.setTotalSuccessUploads((String.valueOf(extractedPVProposalCount)));
			logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Extracted Proposal count :: " + extractedPVProposalCount);
		}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
			transactionInfo.setTotalSuccessUploads((String.valueOf(extractedCVProposalCount)));
			logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Extracted Proposal count :: " + extractedCVProposalCount);
		}
		

		// At the end, come back to the main window
		driver.switchTo().window(mainWindowHandle);

		logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Current URL before logout try ::" + driver.getCurrentUrl());
		WebElement logout = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='hrefLogOut']"), 40);
		logout.click();
		logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - END getPolicyNumberListTobeExtractedFromLocalDb()");
		return true;

	}

	private void setExtractionCount(int extractedPolicyCount, int extractedProposalCount,
			TransactionInfo transactionInfo, Exchange exchange) {
		/* Total policy pdf extratced */
		transactionInfo.setTotalSuccessRecords(String.valueOf(extractedPolicyCount));
		logger.info("setExtractionCount "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - setting Extracted policy count :: " + extractedPolicyCount);

		/* Total proposal pdf extratced */
		transactionInfo.setTotalSuccessUploads((String.valueOf(extractedProposalCount)));
		logger.info("setExtractionCount "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - setting Extracted Proposal count :: " + extractedProposalCount);

		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
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
		logger.info("tataPolicyPdfExtracter - is File ::" + file.getName() + " Deleted ?? :: " + isFileDeleted);
		file = null;
		return true;
	}

	public boolean saveAsPadf(WebDriver driver, String pdfPath, ChromeDriver newDriver, Exchange exchange)
			throws URISyntaxException, InterruptedException, IOException {

		boolean exist = false, isDeleted = false;

		if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
			isDeleted = UtilityFile.isFileDeleted(
				UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("tatapv.policy.temppdf.location"),
				".pdf");
		else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
			isDeleted = UtilityFile.isFileDeleted(
					UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("tatacv.policy.temppdf.location"),
					".pdf");
					
		if (isDeleted) {
			logger.info("tataPolicyPdfExtracter : saveAsPadf - cleared temp_pdf folder for new Download");
		}
		
		newDriver.get(driver.getCurrentUrl()); 
	
			int i = 0;
			do {
				WebElement proposalpdflement = UtilityFile.waitForElementPresent(newDriver,
						By.xpath("//*[@id='ContentPlaceHolder1_btnPrint']"), 40);
				if(proposalpdflement!=null){
					FluentWait<WebDriver> fluentwaitProposal = UtilityFile.getFluentWaitObject(newDriver, 30, 200);
					fluentwaitProposal.until(ExpectedConditions.visibilityOf(proposalpdflement));
					fluentwaitProposal.until(ExpectedConditions.elementToBeClickable(proposalpdflement));
					proposalpdflement.click();
					
					Thread.sleep(3000);
	
				logger.info("tataPolicyPdfExtracter : saveAsPadf - isFileDownload_ExistAndRename method called here -- download try number :: "
						+ (i + 1));
				if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
					exist = UtilityFile.isFileDownload_ExistAndRename(
						UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("tatapv.policy.temppdf.location"),
						".pdf", true, pdfPath);
				else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
					exist = UtilityFile.isFileDownload_ExistAndRename(
							UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("tatacv.policy.temppdf.location"),
							".pdf", true, pdfPath);
					
				i++;
				if (i == 10) {
					logger.info("tataPolicyPdfExtracter : saveAsPadf - file not download after 30 secs of wait ");
					return false;
				}
			}else{
				logger.error("tataPolicyPdfExtracter : saveAsPadf - policy pdf element issue  ");
				return false;
			}

		} while (exist != true);
		return true;

	}

	public static boolean downloadPdf(WebDriver driver, String phantomjsPath, String rasterizeJsPath, String policyNo,
			String pdfPath, String pageURL, String cookies) throws IOException, InterruptedException {
		logger.info("tataPolicyPdfExtracter - BEGIN downloadPdf() called");
		boolean result = false;

		String cmd = phantomjsPath + " " + rasterizeJsPath + " \"" + pageURL + "\" \"" + pdfPath + "\" \"" + cookies
				+ "\"";
		logger.info("cmd to be executed ::" + cmd);
		Process process = Runtime.getRuntime().exec(cmd);
		//int exitStatus = process.waitFor();
		process.waitFor(120, TimeUnit.SECONDS);
    	if(process!=null)
    		process.destroy();
    	int exitStatus = process.exitValue();
		logger.info("Execution status ::" + exitStatus);

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		String currentLine = null;
		StringBuilder stringBuilder = new StringBuilder(exitStatus == 0 ? "SUCCESS:" : "ERROR:");
		currentLine = bufferedReader.readLine();
		while (currentLine != null) {
			logger.info("currentLine ::" + currentLine);
			stringBuilder.append(currentLine);
			currentLine = bufferedReader.readLine();
		}

		if (exitStatus != 0) {
			if (stringBuilder != null)
				logger.info("Error in html to pdf conversion :: " + stringBuilder.toString());
		}

		if (exitStatus == 0) {
			result = true;
		} else {
			result = false;
		}
		currentLine = null;
		bufferedReader.close();
		bufferedReader = null;
		stringBuilder = null;
		logger.info("tataPolicyPdfExtracter - END downloadPdf()");
		return result;
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
	
	public void tataPVPDFExtractionProcess(List<TataPolicy> tataPolicyNoList,Exchange exchange,TransactionInfo transactionInfo,ApplicationConfiguration applicationDetails, String mainWindowHandle, WebDriver driver, TataPolicyService tataPolicyService ) throws Exception{
		String policyNo = "0", proposalNumber = "0", pdfPath = "";
		
		String policyWindowHandle = "",proposalWindowHandle = "", switchingURL = "",
				cookies = "";

		for (Cookie ck : driver.manage().getCookies()) {
			if (ck.getName().contains("ASP")) {
				cookies = ck.getName() + ";" + ck.getValue() + ";" + ck.getDomain() + ";" + ck.getPath() + ";"
						+ ck.getExpiry() + ";" + ck.isSecure();
				logger.info(ck.getName() + ";" + ck.getValue() + ";" + ck.getDomain() + ";" + ck.getPath() + ";"
						+ ck.getExpiry() + ";" + ck.isSecure());
			}
		}
		
		String tataFolderPath = "", tataTempPdfPath = "";
		String phantomjsPath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getCarPolicyProperty(RPAConstants.PHANTOMJS_DRIVER_FOLDER) + "/phantomjs";
		phantomjsPath = removeFirstLetterIfStartsWithSlash(phantomjsPath);

		String rasterizeJsPath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getCarPolicyProperty(RPAConstants.CAR_POLICY_JS_FOLDER) + "/rasterize.js";
		rasterizeJsPath = removeFirstLetterIfStartsWithSlash(rasterizeJsPath);
		
		//, noRecordCount = 0

		applicationContext = SpringContext.getAppContext();
		commonService = applicationContext.getBean(CommonService.class);

		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			tataFolderPath = UtilityFile.getCodeBasePath()
					+ UtilityFile.getCarPolicyProperty(RPAConstants.TATA_POLICY_FOLDER)
					+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY) + RPAConstants.UNDERSCORE
					+ RPAConstants.BACK_LOG;

		} else {
			tataFolderPath = UtilityFile.getCodeBasePath()
					+ UtilityFile.getCarPolicyProperty(RPAConstants.TATA_POLICY_FOLDER)
					+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY);

		}
		if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
			tataTempPdfPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("tatapv.policy.temppdf.location");
		else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
			tataTempPdfPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("tatacv.policy.temppdf.location");
		
		File tataTempPdfFolder = new File(tataTempPdfPath);

		if (!tataTempPdfFolder.exists())
			tataTempPdfFolder.mkdirs();

		File tataFolder = new File(tataFolderPath);

		if (!tataFolder.exists())
			tataFolder.mkdirs();

		String downloadFilePath = "";
		if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
		 downloadFilePath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("tatapv.policy.temppdf.location");
		else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
			downloadFilePath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("tatacv.policy.temppdf.location");
		
		logger.info("before Replacement"+downloadFilePath);
		downloadFilePath=downloadFilePath.replace("/", "\\");

		logger.info("after Replacement"+downloadFilePath);
		if(downloadFilePath.startsWith("\\"))
		{
			downloadFilePath=downloadFilePath.substring(1);
			logger.info("after removing the slash"+downloadFilePath);
		}

		logger.info("newHeadlessDriverWithPdfExternalOpen - final download path for  chrome:"+downloadFilePath);
		logger.info(downloadFilePath);
		
			System.setProperty("webdriver.chrome.driver", UtilityFile.getCodeBasePath() +"/rpa_drivers/chrome/chromedriver.exe");
			ChromeOptions options = new ChromeOptions();
               options.addArguments("--test-type");
               options.addArguments("--headless");
               options.addArguments("--disable-extensions"); //to disable browser extension popup

               ChromeDriverService driverService = ChromeDriverService.createDefaultService();
               ChromeDriver newDriver = new ChromeDriver(driverService, options);

               Map<String, Object> commandParams = new HashMap<>();
               commandParams.put("cmd", "Page.setDownloadBehavior");
               Map<String, String> params = new HashMap<>();
               params.put("behavior", "allow");
               params.put("downloadPath", downloadFilePath);
               commandParams.put("params", params);
               ObjectMapper objectMapper = new ObjectMapper();
               HttpClient httpClient = HttpClientBuilder.create().build();
               String command = objectMapper.writeValueAsString(commandParams);
               String u = driverService.getUrl().toString() + "/session/" + newDriver.getSessionId() + "/chromium/send_command";
               HttpPost request = new HttpPost(u);
               request.addHeader("content-type", "application/json");
               request.setEntity(new StringEntity(command));
               httpClient.execute(request);
              
               newDriver.get(applicationDetails.getUrl());
		 Set<Cookie> allCookies = driver.manage().getCookies();
		 
		 for(Cookie cookie : allCookies) {
	            newDriver.manage().addCookie(cookie);
	        }
		 
		 if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
			 exchange.getIn().setHeader("chromeDriver_tatapv_fileDownload", newDriver);
		 if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
			 exchange.getIn().setHeader("chromeDriver_tatacv_fileDownload", newDriver);
		 
		for (TataPolicy tataPolicyObj : tataPolicyNoList) {

			for (String winHandle : driver.getWindowHandles()) {
				if (!winHandle.equalsIgnoreCase(mainWindowHandle)) {
					driver.switchTo().window(winHandle);
					driver.close();
				}
			}

			driver.switchTo().window(mainWindowHandle);

			logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - after switched to main window " + driver.getCurrentUrl());

			policyNo = tataPolicyObj.getPolicyNo();
			proposalNumber = tataPolicyObj.getProposalNumber();

			WebElement claimNumberElement = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='ContentPlaceHolder1_txtPolNo']"), 40);
			claimNumberElement.clear();
			claimNumberElement.sendKeys(policyNo);

			logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - after policyNo input set " + driver.getCurrentUrl());

			logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - policyNo ::  " + policyNo);

			WebElement policysearchElement = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='ContentPlaceHolder1_btnSearch']"), 40);
			FluentWait<WebDriver> fluentwait = UtilityFile.getFluentWaitObject(driver, 30, 200);
			fluentwait.until(ExpectedConditions.visibilityOf(policysearchElement));
			fluentwait.until(ExpectedConditions.elementToBeClickable(policysearchElement));
			policysearchElement.click();
			
			//WebElement policyResultTable = null;
			WebElement noRecord = null,contentTbl=null;
			try{
				
				/*noRecord = waitForElementPresentWithOutErrorThrows(driver,
						By.xpath("//*[@id='ContentPlaceHolder1_lblMessage']"),
						2);
				 
				 if(noRecord==null){*/
				
			 contentTbl =  waitForElementPresentWithOutErrorThrows(driver,
						By.xpath("//*[@id='ContentPlaceHolder1_gvPrintPolicy']/tbody/tr[2]/td[1]/span[contains(text(),'"
								+ policyNo + "')]"),
						60);
			 if(contentTbl==null){
				 noRecord=waitForElementPresentWithOutErrorThrows(driver,
							By.xpath("//*[@id='ContentPlaceHolder1_lblMessage']"),
							10);
				 if(noRecord==null){
					 //throw new Exception("Table Not Loaded even after waited for 60 secs");
					 logger.error("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Table Not Loaded even after waited for 60 secs");
					 tataPolicyObj.setIsPolicyDownloaded(RPAConstants.E);
					 tataPolicyObj.setIsProposalDownloaded(RPAConstants.E);
				 }else{
					 logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - No record Found for -"+policyNo);
					 tataPolicyObj.setIsPolicyDownloaded(RPAConstants.R);
					 tataPolicyObj.setIsProposalDownloaded(RPAConstants.R);
				 }
			 }else{

			if (driver.getCurrentUrl().contains("login.aspx")) {
				logger.info("Tata Application Logged out automatically");
				if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
					setExtractionCount(extractedPVPolicyCount, extractedPVProposalCount, transactionInfo, exchange);
				}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
					 setExtractionCount(extractedCVPolicyCount, extractedCVProposalCount, transactionInfo, exchange);
				}
				
				throw new Exception("Tata Application Logged out automatically");
			}

			if (tataPolicyObj.getIsPolicyDownloaded() == null)
				tataPolicyObj.setIsPolicyDownloaded(RPAConstants.N);

			/* policy Extraction */
			if (tataPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {

				System.out.println("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" <<<<<<<<<<<<<<<<No. of current tabs before policy click: "
						+ new ArrayList<String>(driver.getWindowHandles()).size());
				// try{

				if (driver.getCurrentUrl().contains("login.aspx")) {
					logger.info("Tata Application Logged out automatically");
					if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
						setExtractionCount(extractedPVPolicyCount, extractedPVProposalCount, transactionInfo, exchange);
					}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
						 setExtractionCount(extractedCVPolicyCount, extractedCVProposalCount, transactionInfo, exchange);
					}
					throw new Exception("Tata Application Logged out automatically");
				}

				new WebDriverWait(driver, 40).ignoring(StaleElementReferenceException.class).until(ExpectedConditions
						.elementToBeClickable(By.id("ContentPlaceHolder1_gvPrintPolicy_btnPrePrint_0")));
				driver.findElement(By.id("ContentPlaceHolder1_gvPrintPolicy_btnPrePrint_0")).click();

				if (driver.getCurrentUrl().contains("login.aspx")) {
					logger.info("Tata Application Logged out automatically");
					if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
						setExtractionCount(extractedPVPolicyCount, extractedPVProposalCount, transactionInfo, exchange);
					}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
						 setExtractionCount(extractedCVPolicyCount, extractedCVProposalCount, transactionInfo, exchange);
					}
					throw new Exception("Tata Application Logged out automatically");
				}

				(new WebDriverWait(driver, 30)).until(ExpectedConditions.or(ExpectedConditions.numberOfWindowsToBe(2),
						ExpectedConditions.numberOfWindowsToBe(2)));

				logger.info("<<<<<<<<<<<<<<<No. of current tabs after policy click: "
						+ new ArrayList<String>(driver.getWindowHandles()).size());

				policyWindowHandle = "";
				switchingURL = "";
				for (String winHandle : driver.getWindowHandles()) {
					driver.switchTo().window(winHandle);
					switchingURL = driver.getCurrentUrl();
					logger.info("switchingURL bfr policy download --" + switchingURL);
					if (switchingURL.contains("VSPrintScheduleRpt.aspx")) {
						policyWindowHandle = winHandle;
					} else if (switchingURL.contains("VSViewProposal.aspx")) {
						if (!policyWindowHandle.equalsIgnoreCase("")) {
							driver.switchTo().window(policyWindowHandle);
						}
					}

				}
				logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" current url -- " + driver.getCurrentUrl());

				pdfPath = tataFolderPath + "/policy_" + policyNo + ".pdf";
				pdfPath = removeFirstLetterIfStartsWithSlash(pdfPath);

				/* To Download Policy PDF */
				if (saveAsPadf(driver, pdfPath,newDriver,exchange)) {
					
					if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
						extractedPVPolicyCount++;
					}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
						 extractedCVPolicyCount++;
					}
					logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" -policy PDF Downloaded for policyNo :: " + policyNo);

					tataPolicyObj.setIsPolicyDownloaded(RPAConstants.Y);
					tataPolicyObj.setPolicyPdfPath(pdfPath);
					tataPolicyObj.setIsPolicyUploaded(RPAConstants.N);
				}
			} else {
				logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Policy pdf already extracted for policy code :: " + policyNo);
			}

			if (tataPolicyObj.getIsProposalDownloaded() == null)
				tataPolicyObj.setIsProposalDownloaded(RPAConstants.N);

			/* proposal form extraction */
			if (tataPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.N)) {
				logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" before url -- " + driver.getCurrentUrl());
				// driver.switchTo().window(mainWindowHandle);

				for (String winHandle : driver.getWindowHandles()) {
					if (!winHandle.equalsIgnoreCase(mainWindowHandle)) {
						driver.switchTo().window(winHandle);
						driver.close();
					}
				}

				driver.switchTo().window(mainWindowHandle);

				logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" current url -- " + driver.getCurrentUrl());

				logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" <<<<<<<<<<<<<<<<No. of current tabs before proposal click: "
						+ new ArrayList<String>(driver.getWindowHandles()).size());

				// to open proposal pdf
				WebElement proposalpdflement = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='ContentPlaceHolder1_gvPrintPolicy_btnPrintProposal_0']"), 40);
				FluentWait<WebDriver> fluentwaitProposal = UtilityFile.getFluentWaitObject(driver, 30, 200);
				fluentwaitProposal.until(ExpectedConditions.visibilityOf(proposalpdflement));
				fluentwaitProposal.until(ExpectedConditions.elementToBeClickable(proposalpdflement));
				// proposalpdflement.click();
				((JavascriptExecutor) driver)
						.executeScript("$('#ContentPlaceHolder1_gvPrintPolicy_btnPrintProposal_0')[0].click();");

				pdfPath = tataFolderPath + "/proposal_" + proposalNumber + ".pdf";
				pdfPath = removeFirstLetterIfStartsWithSlash(pdfPath);

				if (driver.getCurrentUrl().contains("login.aspx")) {
					logger.info("Tata Application Logged out automatically");
					if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
						setExtractionCount(extractedPVPolicyCount, extractedPVProposalCount, transactionInfo, exchange);
					}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
						 setExtractionCount(extractedCVPolicyCount, extractedCVProposalCount, transactionInfo, exchange);
					}
					throw new Exception("Tata Application Logged out automatically");
				}

				if (driver.getCurrentUrl().contains("login.aspx")) {
					logger.info("Tata Application Logged out automatically");
					if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
						setExtractionCount(extractedPVPolicyCount, extractedPVProposalCount, transactionInfo, exchange);
					}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
						 setExtractionCount(extractedCVPolicyCount, extractedCVProposalCount, transactionInfo, exchange);
					}
					throw new Exception("Tata Application Logged out automatically");
				}

				(new WebDriverWait(driver, 30)).until(ExpectedConditions.or(ExpectedConditions.numberOfWindowsToBe(2),
						ExpectedConditions.numberOfWindowsToBe(2)));

				logger.info("<<<<<<<<<<<<<<<<No. of current tabs after proposal click: "
						+ new ArrayList<String>(driver.getWindowHandles()).size());

				if (driver.getCurrentUrl().contains("login.aspx")) {
					logger.info("Tata Application Logged out automatically");
					if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
						setExtractionCount(extractedPVPolicyCount, extractedPVProposalCount, transactionInfo, exchange);
					}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
						 setExtractionCount(extractedCVPolicyCount, extractedCVProposalCount, transactionInfo, exchange);
					}
					throw new Exception("Tata Application Logged out automatically");
				}

				proposalWindowHandle = "";
				switchingURL = "";
				for (String winHandle : driver.getWindowHandles()) {
					driver.switchTo().window(winHandle);
					switchingURL = driver.getCurrentUrl();
					logger.info("switchingURL bfr propsal download --" + switchingURL);
					if (switchingURL.contains("VSViewProposal.aspx")) {
						proposalWindowHandle = winHandle;
					} else if (switchingURL.contains("VSPrintScheduleRpt.aspx")) {
						if (!proposalWindowHandle.equalsIgnoreCase("")) {
							driver.switchTo().window(proposalWindowHandle);
						}
					}
				}

				logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" bfroe propsal pdf download url -- " + driver.getCurrentUrl());

				/* To Download proposal PDF */
				if (downloadPdf(driver, phantomjsPath, rasterizeJsPath, policyNo, pdfPath, driver.getCurrentUrl(),
						cookies)) {
					if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
						extractedPVProposalCount++;
					}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
						extractedCVProposalCount++;
					}
					logger.info(
							"tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" -propsal form PDF Downloaded for proposalNo :: " + proposalNumber);

					tataPolicyObj.setIsProposalDownloaded(RPAConstants.Y);
					tataPolicyObj.setProposalPdfPath(pdfPath);
					tataPolicyObj.setIsProposalUploaded(RPAConstants.N);
				}
			} else {
				logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Proposal pdf already extracted for policy code :: " + policyNo);
			}
		}
			
			/*}else{
		 		logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - No record Found for -"+policyNo);
		 		tataPolicyObj.setIsPolicyDownloaded(RPAConstants.R);
		 		tataPolicyObj.setIsProposalDownloaded(RPAConstants.R);
		 	}*/
			
			}catch(TimeoutException e){
				logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" TIMEOUT - Element not found -->"+e);
			}

			// tataPolicyObj.setInwardCode(inwardCode);
			tataPolicyService.save(tataPolicyObj);

		}
	}

		public void tataCVPDFExtractionProcess(List<TataPolicy> tataPolicyNoList,Exchange exchange,TransactionInfo transactionInfo,ApplicationConfiguration applicationDetails, String mainWindowHandle, WebDriver driver, TataPolicyService tataPolicyService ) throws Exception{
			String policyNo = "0", proposalNumber = "0", pdfPath = "";
			
			String policyWindowHandle = "",proposalWindowHandle = "", switchingURL = "",
					cookies = "";
		
			for (Cookie ck : driver.manage().getCookies()) {
				if (ck.getName().contains("ASP")) {
					cookies = ck.getName() + ";" + ck.getValue() + ";" + ck.getDomain() + ";" + ck.getPath() + ";"
							+ ck.getExpiry() + ";" + ck.isSecure();
					logger.info(ck.getName() + ";" + ck.getValue() + ";" + ck.getDomain() + ";" + ck.getPath() + ";"
							+ ck.getExpiry() + ";" + ck.isSecure());
				}
			}
			
			String tataFolderPath = "", tataTempPdfPath = "";
			String phantomjsPath = UtilityFile.getCodeBasePath()
					+ UtilityFile.getCarPolicyProperty(RPAConstants.PHANTOMJS_DRIVER_FOLDER) + "/phantomjs";
			phantomjsPath = removeFirstLetterIfStartsWithSlash(phantomjsPath);
		
			String rasterizeJsPath = UtilityFile.getCodeBasePath()
					+ UtilityFile.getCarPolicyProperty(RPAConstants.CAR_POLICY_JS_FOLDER) + "/rasterize.js";
			rasterizeJsPath = removeFirstLetterIfStartsWithSlash(rasterizeJsPath);
			
			//, noRecordCount = 0
		
			applicationContext = SpringContext.getAppContext();
			commonService = applicationContext.getBean(CommonService.class);
		
			if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
				tataFolderPath = UtilityFile.getCodeBasePath()
						+ UtilityFile.getCarPolicyProperty(RPAConstants.TATA_POLICY_FOLDER)
						+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY) + RPAConstants.UNDERSCORE
						+ RPAConstants.BACK_LOG;
		
			} else {
				tataFolderPath = UtilityFile.getCodeBasePath()
						+ UtilityFile.getCarPolicyProperty(RPAConstants.TATA_POLICY_FOLDER)
						+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY);
		
			}
			if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
				tataTempPdfPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("tatapv.policy.temppdf.location");
			else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
				tataTempPdfPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("tatacv.policy.temppdf.location");
			
			File tataTempPdfFolder = new File(tataTempPdfPath);
		
			if (!tataTempPdfFolder.exists())
				tataTempPdfFolder.mkdirs();
		
			File tataFolder = new File(tataFolderPath);
		
			if (!tataFolder.exists())
				tataFolder.mkdirs();
		
			String downloadFilePath = "";
			if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
			 downloadFilePath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("tatapv.policy.temppdf.location");
			else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
				downloadFilePath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("tatacv.policy.temppdf.location");
			
			logger.info("before Replacement"+downloadFilePath);
			downloadFilePath=downloadFilePath.replace("/", "\\");
		
			logger.info("after Replacement"+downloadFilePath);
			if(downloadFilePath.startsWith("\\"))
			{
				downloadFilePath=downloadFilePath.substring(1);
				logger.info("after removing the slash"+downloadFilePath);
			}
		
			logger.info("newHeadlessDriverWithPdfExternalOpen - final download path for  chrome:"+downloadFilePath);
			logger.info(downloadFilePath);
			
				System.setProperty("webdriver.chrome.driver", UtilityFile.getCodeBasePath() +"/rpa_drivers/chrome/chromedriver.exe");
				ChromeOptions options = new ChromeOptions();
		           options.addArguments("--test-type");
		           options.addArguments("--headless");
		           options.addArguments("--disable-extensions"); //to disable browser extension popup
		
		           ChromeDriverService driverService = ChromeDriverService.createDefaultService();
		           ChromeDriver newDriver = new ChromeDriver(driverService, options);
		
		           Map<String, Object> commandParams = new HashMap<>();
		           commandParams.put("cmd", "Page.setDownloadBehavior");
		           Map<String, String> params = new HashMap<>();
		           params.put("behavior", "allow");
		           params.put("downloadPath", downloadFilePath);
		           commandParams.put("params", params);
		           ObjectMapper objectMapper = new ObjectMapper();
		           HttpClient httpClient = HttpClientBuilder.create().build();
		           String command = objectMapper.writeValueAsString(commandParams);
		           String u = driverService.getUrl().toString() + "/session/" + newDriver.getSessionId() + "/chromium/send_command";
		           HttpPost request = new HttpPost(u);
		           request.addHeader("content-type", "application/json");
		           request.setEntity(new StringEntity(command));
		           httpClient.execute(request);
		          
		           newDriver.get(applicationDetails.getUrl());
			 Set<Cookie> allCookies = driver.manage().getCookies();
			 
			 for(Cookie cookie : allCookies) {
		            newDriver.manage().addCookie(cookie);
		        }
			 
			 if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
				 exchange.getIn().setHeader("chromeDriver_tatapv_fileDownload", newDriver);
			 if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
				 exchange.getIn().setHeader("chromeDriver_tatacv_fileDownload", newDriver);
			 
			for (TataPolicy tataPolicyObj : tataPolicyNoList) {
		
				for (String winHandle : driver.getWindowHandles()) {
					if (!winHandle.equalsIgnoreCase(mainWindowHandle)) {
						driver.switchTo().window(winHandle);
						driver.close();
					}
				}
		
				driver.switchTo().window(mainWindowHandle);
		
				logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - after switched to main window " + driver.getCurrentUrl());
		
				policyNo = tataPolicyObj.getPolicyNo();
				proposalNumber = tataPolicyObj.getProposalNumber();
		
				WebElement claimNumberElement = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='ContentPlaceHolder1_txtPolNo']"), 40);
				claimNumberElement.clear();
				claimNumberElement.sendKeys(policyNo);
		
				logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - after policyNo input set " + driver.getCurrentUrl());
		
				logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - policyNo ::  " + policyNo);
		
				WebElement policysearchElement = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='ContentPlaceHolder1_btnSearch']"), 40);
				FluentWait<WebDriver> fluentwait = UtilityFile.getFluentWaitObject(driver, 30, 200);
				fluentwait.until(ExpectedConditions.visibilityOf(policysearchElement));
				fluentwait.until(ExpectedConditions.elementToBeClickable(policysearchElement));
				policysearchElement.click();
				
				//WebElement policyResultTable = null;
				WebElement noRecord = null,contentTbl=null;
				try{
					
					/*noRecord = waitForElementPresentWithOutErrorThrows(driver,
							By.xpath("//*[@id='ContentPlaceHolder1_lblMessage']"),
							2);
					 
					 if(noRecord==null){*/
					
				 contentTbl =  waitForElementPresentWithOutErrorThrows(driver,
							By.xpath("//*[@id='ContentPlaceHolder1_gvPrintPolicy']/tbody/tr[2]/td[1]/span[contains(text(),'"
									+ policyNo + "')]"),
							60);
				 if(contentTbl==null){
					 noRecord=waitForElementPresentWithOutErrorThrows(driver,
								By.xpath("//*[@id='ContentPlaceHolder1_lblMessage']"),
								10);
					 if(noRecord==null){
						 //throw new Exception("Table Not Loaded even after waited for 60 secs");
						 logger.error("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Table Not Loaded even after waited for 60 secs");
						 tataPolicyObj.setIsPolicyDownloaded(RPAConstants.E);
						 tataPolicyObj.setIsProposalDownloaded(RPAConstants.E);
					 }else{
						 logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - No record Found for -"+policyNo);
						 tataPolicyObj.setIsPolicyDownloaded(RPAConstants.R);
						 tataPolicyObj.setIsProposalDownloaded(RPAConstants.R);
					 }
				 }else{
		
				if (driver.getCurrentUrl().contains("login.aspx")) {
					logger.info("Tata Application Logged out automatically");
					if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
						setExtractionCount(extractedPVPolicyCount, extractedPVProposalCount, transactionInfo, exchange);
					}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
						 setExtractionCount(extractedCVPolicyCount, extractedCVProposalCount, transactionInfo, exchange);
					}
					
					throw new Exception("Tata Application Logged out automatically");
				}
		
				if (tataPolicyObj.getIsPolicyDownloaded() == null)
					tataPolicyObj.setIsPolicyDownloaded(RPAConstants.N);
		
				/* policy Extraction */
				if (tataPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
		
					System.out.println("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" <<<<<<<<<<<<<<<<No. of current tabs before policy click: "
							+ new ArrayList<String>(driver.getWindowHandles()).size());
					// try{
		
					if (driver.getCurrentUrl().contains("login.aspx")) {
						logger.info("Tata Application Logged out automatically");
						if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
							setExtractionCount(extractedPVPolicyCount, extractedPVProposalCount, transactionInfo, exchange);
						}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
							 setExtractionCount(extractedCVPolicyCount, extractedCVProposalCount, transactionInfo, exchange);
						}
						throw new Exception("Tata Application Logged out automatically");
					}
		
					new WebDriverWait(driver, 40).ignoring(StaleElementReferenceException.class).until(ExpectedConditions
							.elementToBeClickable(By.id("ContentPlaceHolder1_gvPrintPolicy_btnPrePrint_0")));
					driver.findElement(By.id("ContentPlaceHolder1_gvPrintPolicy_btnPrePrint_0")).click();
		
					if (driver.getCurrentUrl().contains("login.aspx")) {
						logger.info("Tata Application Logged out automatically");
						if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
							setExtractionCount(extractedPVPolicyCount, extractedPVProposalCount, transactionInfo, exchange);
						}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
							 setExtractionCount(extractedCVPolicyCount, extractedCVProposalCount, transactionInfo, exchange);
						}
						throw new Exception("Tata Application Logged out automatically");
					}
		
					(new WebDriverWait(driver, 30)).until(ExpectedConditions.or(ExpectedConditions.numberOfWindowsToBe(2),
							ExpectedConditions.numberOfWindowsToBe(2)));
		
					logger.info("<<<<<<<<<<<<<<<No. of current tabs after policy click: "
							+ new ArrayList<String>(driver.getWindowHandles()).size());
		
					policyWindowHandle = "";
					switchingURL = "";
					for (String winHandle : driver.getWindowHandles()) {
						driver.switchTo().window(winHandle);
						switchingURL = driver.getCurrentUrl();
						logger.info("switchingURL bfr policy download --" + switchingURL);
						if (switchingURL.contains("VSPrintScheduleRpt.aspx")) {
							policyWindowHandle = winHandle;
						} else if (switchingURL.contains("VSViewProposal.aspx")) {
							if (!policyWindowHandle.equalsIgnoreCase("")) {
								driver.switchTo().window(policyWindowHandle);
							}
						}
		
					}
					logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" current url -- " + driver.getCurrentUrl());
		
					pdfPath = tataFolderPath + "/policy_" + policyNo + ".pdf";
					pdfPath = removeFirstLetterIfStartsWithSlash(pdfPath);
		
					/* To Download Policy PDF */
					if (saveAsPadf(driver, pdfPath,newDriver,exchange)) {
						
						if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
							extractedPVPolicyCount++;
						}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
							 extractedCVPolicyCount++;
						}
						logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" -policy PDF Downloaded for policyNo :: " + policyNo);
		
						tataPolicyObj.setIsPolicyDownloaded(RPAConstants.Y);
						tataPolicyObj.setPolicyPdfPath(pdfPath);
						tataPolicyObj.setIsPolicyUploaded(RPAConstants.N);
					}
				} else {
					logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Policy pdf already extracted for policy code :: " + policyNo);
				}
		
				if (tataPolicyObj.getIsProposalDownloaded() == null)
					tataPolicyObj.setIsProposalDownloaded(RPAConstants.N);
		
				/* proposal form extraction */
				if (tataPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.N)) {
					logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" before url -- " + driver.getCurrentUrl());
					// driver.switchTo().window(mainWindowHandle);
		
					for (String winHandle : driver.getWindowHandles()) {
						if (!winHandle.equalsIgnoreCase(mainWindowHandle)) {
							driver.switchTo().window(winHandle);
							driver.close();
						}
					}
		
					driver.switchTo().window(mainWindowHandle);
		
					logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" current url -- " + driver.getCurrentUrl());
		
					logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" <<<<<<<<<<<<<<<<No. of current tabs before proposal click: "
							+ new ArrayList<String>(driver.getWindowHandles()).size());
		
					// to open proposal pdf
					WebElement proposalpdflement = UtilityFile.waitForElementPresent(driver,
							By.xpath("//*[@id='ContentPlaceHolder1_gvPrintPolicy_btnPrintProposal_0']"), 40);
					FluentWait<WebDriver> fluentwaitProposal = UtilityFile.getFluentWaitObject(driver, 30, 200);
					fluentwaitProposal.until(ExpectedConditions.visibilityOf(proposalpdflement));
					fluentwaitProposal.until(ExpectedConditions.elementToBeClickable(proposalpdflement));
					// proposalpdflement.click();
					((JavascriptExecutor) driver)
							.executeScript("$('#ContentPlaceHolder1_gvPrintPolicy_btnPrintProposal_0')[0].click();");
		
					pdfPath = tataFolderPath + "/proposal_" + proposalNumber + ".pdf";
					pdfPath = removeFirstLetterIfStartsWithSlash(pdfPath);
		
					if (driver.getCurrentUrl().contains("login.aspx")) {
						logger.info("Tata Application Logged out automatically");
						if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
							setExtractionCount(extractedPVPolicyCount, extractedPVProposalCount, transactionInfo, exchange);
						}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
							 setExtractionCount(extractedCVPolicyCount, extractedCVProposalCount, transactionInfo, exchange);
						}
						throw new Exception("Tata Application Logged out automatically");
					}
		
					if (driver.getCurrentUrl().contains("login.aspx")) {
						logger.info("Tata Application Logged out automatically");
						if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
							setExtractionCount(extractedPVPolicyCount, extractedPVProposalCount, transactionInfo, exchange);
						}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
							 setExtractionCount(extractedCVPolicyCount, extractedCVProposalCount, transactionInfo, exchange);
						}
						throw new Exception("Tata Application Logged out automatically");
					}
		
					(new WebDriverWait(driver, 30)).until(ExpectedConditions.or(ExpectedConditions.numberOfWindowsToBe(2),
							ExpectedConditions.numberOfWindowsToBe(2)));
		
					logger.info("<<<<<<<<<<<<<<<<No. of current tabs after proposal click: "
							+ new ArrayList<String>(driver.getWindowHandles()).size());
		
					if (driver.getCurrentUrl().contains("login.aspx")) {
						logger.info("Tata Application Logged out automatically");
						if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
							setExtractionCount(extractedPVPolicyCount, extractedPVProposalCount, transactionInfo, exchange);
						}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
							 setExtractionCount(extractedCVPolicyCount, extractedCVProposalCount, transactionInfo, exchange);
						}
						throw new Exception("Tata Application Logged out automatically");
					}
		
					proposalWindowHandle = "";
					switchingURL = "";
					for (String winHandle : driver.getWindowHandles()) {
						driver.switchTo().window(winHandle);
						switchingURL = driver.getCurrentUrl();
						logger.info("switchingURL bfr propsal download --" + switchingURL);
						if (switchingURL.contains("VSViewProposal.aspx")) {
							proposalWindowHandle = winHandle;
						} else if (switchingURL.contains("VSPrintScheduleRpt.aspx")) {
							if (!proposalWindowHandle.equalsIgnoreCase("")) {
								driver.switchTo().window(proposalWindowHandle);
							}
						}
					}
		
					logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" bfroe propsal pdf download url -- " + driver.getCurrentUrl());
		
					/* To Download proposal PDF */
					if (downloadPdf(driver, phantomjsPath, rasterizeJsPath, policyNo, pdfPath, driver.getCurrentUrl(),
							cookies)) {
						if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
							extractedPVProposalCount++;
						}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){ 
							extractedCVProposalCount++;
						}
						logger.info(
								"tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" -propsal form PDF Downloaded for proposalNo :: " + proposalNumber);
		
						tataPolicyObj.setIsProposalDownloaded(RPAConstants.Y);
						tataPolicyObj.setProposalPdfPath(pdfPath);
						tataPolicyObj.setIsProposalUploaded(RPAConstants.N);
					}
				} else {
					logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Proposal pdf already extracted for policy code :: " + policyNo);
				}
			}
				
				/*}else{
			 		logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - No record Found for -"+policyNo);
			 		tataPolicyObj.setIsPolicyDownloaded(RPAConstants.R);
			 		tataPolicyObj.setIsProposalDownloaded(RPAConstants.R);
			 	}*/
				
				}catch(TimeoutException e){
					logger.info("tataPolicyPdfExtracter "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" TIMEOUT - Element not found -->"+e);
				}
		
				// tataPolicyObj.setInwardCode(inwardCode);
				tataPolicyService.save(tataPolicyObj);
		
			}
		}

}