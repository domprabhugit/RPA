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
import com.rpa.model.processors.VolvoPolicy;
import com.rpa.service.CommonService;
import com.rpa.service.process.ProcessService;
import com.rpa.service.processors.VolvoPolicyService;
import com.rpa.util.UtilityFile;

public class VolvoPolicyPdfExtracter implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(VolvoPolicyPdfExtracter.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	VolvoPolicyService volvoPolicyService;

	@Autowired
	private CommonService commonService;

	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("*********** inside Camel Process of volvoPolicyPdfExtracter Called ************");
		logger.info("BEGIN : volvoPolicyPdfExtracter Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		driver = (WebDriver) exchange.getIn().getHeader("chromeDriver_volvo");
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.EXTRACTION);
		VolvoPolicyPdfExtracter volvoPolicyPdfExtracter = new VolvoPolicyPdfExtracter();
		volvoPolicyService = applicationContext.getBean(VolvoPolicyService.class);
		commonService = applicationContext.getBean(CommonService.class);
		volvoPolicyPdfExtracter.doProcess(volvoPolicyPdfExtracter, exchange, transactionInfo, driver, volvoPolicyService,
				commonService);
		logger.info("*********** inside Camel Process of volvoPolicyPdfExtracter Processor Ended ************");
	}

	public void doProcess(VolvoPolicyPdfExtracter volvoPolicyPdfExtracter, Exchange exchange,
			TransactionInfo transactionInfo, WebDriver driver, VolvoPolicyService volvoPolicyService,
			CommonService commonService) throws Exception {
		logger.info("BEGIN : volvoPolicyPdfExtracter Processor - doProcess Method Called  ");

		getPolicyNumberListTobeExtractedFromLocalDb(driver, transactionInfo, volvoPolicyService, exchange,
				commonService);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : volvoPolicyPdfExtracter Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeExtractedFromLocalDb(WebDriver driver, TransactionInfo transactionInfo,
			VolvoPolicyService volvoPolicyService, Exchange exchange, CommonService commonService) throws Exception {
		logger.info(
				"Processor - volvoPolicyPdfExtracter - BEGIN getPolicyNumberListTobeExtractedFromLocalDb()  called ");
		
		applicationContext = SpringContext.getAppContext();
		ProcessService processService = applicationContext.getBean(ProcessService.class);

		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
				RPAConstants.APPID_VOLVO);
		

		String hondaFolderPath = "",volvoTempPDFPath="";
		String phantomjsPath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getCarPolicyProperty(RPAConstants.PHANTOMJS_DRIVER_FOLDER) + "/phantomjs";
		phantomjsPath = removeFirstLetterIfStartsWithSlash(phantomjsPath);

		String rasterizeJsPath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getCarPolicyProperty(RPAConstants.CAR_POLICY_JS_FOLDER) + "/rasterize.js";
		rasterizeJsPath = removeFirstLetterIfStartsWithSlash(rasterizeJsPath);

		List<VolvoPolicy> volvoPolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			volvoPolicyNoList = volvoPolicyService.findPdfToBeDownloaded(
					commonService.getThresholdFrequencyLevel(RPAConstants.VOLVO_DOWNLOAD_THRESHOLD));
		} else {
			String startDate = UtilityFile.getCarPolicyProperty(RPAConstants.VOLVO_BACKLOG_STARTDATE);
			String endDate = UtilityFile.getCarPolicyProperty(RPAConstants.VOLVO_BACKLOG_ENDDATE);
			volvoPolicyNoList = volvoPolicyService.findPdfToBeDownloadedForBackLogPolicies(startDate, endDate,
					commonService.getThresholdFrequencyLevel(RPAConstants.VOLVO_DOWNLOAD_THRESHOLD));
		}
		logger.info("volvoPolicyPdfExtracter - Unextracted Policies count ::" + volvoPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(volvoPolicyNoList.size()));

		int extractedPolicyCount = 0, extractedProposalCount = 0;
		String policyNo = "0", proposalNumber = "0", pdfPath = "";

		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			hondaFolderPath = UtilityFile.getCodeBasePath()
					+ UtilityFile.getCarPolicyProperty(RPAConstants.VOLVO_POLICY_FOLDER)
					+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY) + RPAConstants.UNDERSCORE
					+ RPAConstants.BACK_LOG;

		} else {
			hondaFolderPath = UtilityFile.getCodeBasePath()
					+ UtilityFile.getCarPolicyProperty(RPAConstants.VOLVO_POLICY_FOLDER)
					+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY);

		}

		volvoTempPDFPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("volvo.policy.temppdf.location");
		File volvoTempPDFPathFolder = new File(volvoTempPDFPath);

		if (!volvoTempPDFPathFolder.exists())
			volvoTempPDFPathFolder.mkdirs();

		File hondaFolder = new File(hondaFolderPath);

		if (!hondaFolder.exists())
			hondaFolder.mkdirs();

		String policyWindowHandle = "", proposalWindowHandle = "", switchingURL = "", mainWindowHandle = "",
				cookies = "";

		for (Cookie ck : driver.manage().getCookies()) {
			if (ck.getName().contains("ASP")) {
				cookies = ck.getName() + ";" + ck.getValue() + ";" + ck.getDomain() + ";" + ck.getPath() + ";"
						+ ck.getExpiry() + ";" + ck.isSecure();
				logger.info(ck.getName() + ";" + ck.getValue() + ";" + ck.getDomain() + ";" + ck.getPath() + ";"
						+ ck.getExpiry() + ";" + ck.isSecure());
			}
		}

		mainWindowHandle = driver.getWindowHandle();
		logger.info("mainWindowHandle --------0 "+ mainWindowHandle);
		
		
		String downloadFilePath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("volvo.policy.temppdf.location");
		
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
		 
		 exchange.getIn().setHeader("chromeDriver_volvo_fileDownload", newDriver);
		 

		for (VolvoPolicy volvoPolicyObj : volvoPolicyNoList) {

			driver.switchTo().window(mainWindowHandle);

			logger.info("volvoPolicyPdfExtracter - after switched to main window " + driver.getCurrentUrl());

			policyNo = volvoPolicyObj.getPolicyNo();
			proposalNumber = volvoPolicyObj.getProposalNumber();

			WebElement claimNumberElement = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='ContentPlaceHolder1_txtPolNo']"), 40);
			claimNumberElement.clear();
			claimNumberElement.sendKeys(policyNo);

			logger.info("volvoPolicyPdfExtracter - after policyNo input set " + driver.getCurrentUrl());

			logger.info("volvoPolicyPdfExtracter - policyNo ::  " + policyNo);

			WebElement policysearchElement = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='ContentPlaceHolder1_btnSearch']"), 40);
			FluentWait<WebDriver> fluentwait = UtilityFile.getFluentWaitObject(driver, 30, 200);
			fluentwait.until(ExpectedConditions.visibilityOf(policysearchElement));
			fluentwait.until(ExpectedConditions.elementToBeClickable(policysearchElement));
			policysearchElement.click();
			
			WebElement noRecord = null,contentTbl=null;
			try{
			 noRecord = waitForElementPresentWithOutErrorThrows(driver,
					By.xpath("//*[@id='ContentPlaceHolder1_lblMessage']"),
					2);
			 
			 if(noRecord==null){
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
						 logger.error("Table Not Loaded even after waited for 60 secs");
						 	volvoPolicyObj.setIsPolicyDownloaded(RPAConstants.E);
					 		volvoPolicyObj.setIsProposalDownloaded(RPAConstants.E);
					 }else{
						 logger.info("No record Found for -"+policyNo);
					 		volvoPolicyObj.setIsPolicyDownloaded(RPAConstants.R);
					 		volvoPolicyObj.setIsProposalDownloaded(RPAConstants.R);
					 }
				 }else{

			if (driver.getCurrentUrl().contains("Login.aspx")) {
				logger.info("Volvo Application Logged out automatically");
				setExtractionCount(extractedPolicyCount, extractedProposalCount, transactionInfo, exchange);
				throw new Exception("Volvo Application Logged out automatically");
			}

			if (volvoPolicyObj.getIsPolicyDownloaded() == null)
				volvoPolicyObj.setIsPolicyDownloaded(RPAConstants.N);

			/* policy Extraction */
			if (volvoPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {

				System.out.println("<<<<<<<<<<<<<<<<No. of current tabs before policy click: "
						+ new ArrayList<String>(driver.getWindowHandles()).size());
				// try{

				if (driver.getCurrentUrl().contains("Login.aspx")) {
					logger.info("Volvo Application Logged out automatically");
					setExtractionCount(extractedPolicyCount, extractedProposalCount, transactionInfo, exchange);
					throw new Exception("Volvo Application Logged out automatically");
				}

				new WebDriverWait(driver, 40).ignoring(StaleElementReferenceException.class).until(ExpectedConditions
						.elementToBeClickable(By.id("ContentPlaceHolder1_gvPrintPolicy_btnPrePrint_0")));
				driver.findElement(By.id("ContentPlaceHolder1_gvPrintPolicy_btnPrePrint_0")).click();

				if (driver.getCurrentUrl().contains("Login.aspx")) {
					logger.info("Volvo Application Logged out automatically");
					setExtractionCount(extractedPolicyCount, extractedProposalCount, transactionInfo, exchange);
					throw new Exception("Volvo Application Logged out automatically");
				}

				(new WebDriverWait(driver, 30)).until(ExpectedConditions.numberOfWindowsToBe(2));

				logger.info("<<<<<<<<<<<<<<<No. of current tabs after policy click: "
						+ new ArrayList<String>(driver.getWindowHandles()).size());

				policyWindowHandle = "";
				switchingURL = "";
				//driver.switchTo().defaultContent();
				for (String winHandle : driver.getWindowHandles()) {
					driver.switchTo().window(winHandle);
				}
				
				logger.info("current url -- " + driver.getCurrentUrl());
				
					if (driver.getCurrentUrl().contains("Login.aspx")) {
						logger.info("Volvo Application Logged out automatically");
						setExtractionCount(extractedPolicyCount, extractedProposalCount, transactionInfo, exchange);
						throw new Exception("Volvo Application Logged out automatically");
					}

				pdfPath = hondaFolderPath + "/policy_" + policyNo + ".pdf";
				pdfPath = removeFirstLetterIfStartsWithSlash(pdfPath);

				/* To Download Policy PDF */
				if (saveAsPadf(driver, pdfPath,newDriver)) {
					extractedPolicyCount++;
					logger.info("volvoPolicyPdfExtracter -policy PDF Downloaded for policyNo :: " + policyNo);

					volvoPolicyObj.setIsPolicyDownloaded(RPAConstants.Y);
					volvoPolicyObj.setPolicyPdfPath(pdfPath);
					volvoPolicyObj.setIsPolicyUploaded(RPAConstants.N);
				}
				if(driver!=null && !driver.getCurrentUrl().contains("VSPrintPolicy"))
					driver.close();
			} else {
				logger.info("volvoPolicyPdfExtracter - Policy pdf already extracted for policy code :: " + policyNo);
			}

			if (volvoPolicyObj.getIsProposalDownloaded() == null)
				volvoPolicyObj.setIsProposalDownloaded(RPAConstants.N);

			/* proposal form extraction */
			if (volvoPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.N)) {
				
				driver.switchTo().window(mainWindowHandle);
				logger.info(" before url -- " + driver.getCurrentUrl());

				logger.info("current url -- " + driver.getCurrentUrl());

				logger.info("<<<<<<<<<<<<<<<<No. of current tabs before proposal click: "
						+ new ArrayList<String>(driver.getWindowHandles()).size());

				// to open proposal pdf
				WebElement proposalpdflement = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='ContentPlaceHolder1_gvPrintPolicy_btnPrintProposal_0']"), 40);
				logger.info(" proposalpdflement ---->"+proposalpdflement);
				FluentWait<WebDriver> fluentwaitProposal = UtilityFile.getFluentWaitObject(driver, 30, 200);
				fluentwaitProposal.until(ExpectedConditions.visibilityOf(proposalpdflement));
				fluentwaitProposal.until(ExpectedConditions.elementToBeClickable(proposalpdflement));
				 proposalpdflement.click();
				/*((JavascriptExecutor) driver)
						.executeScript("$('#ContentPlaceHolder1_gvPrintPolicy_btnPrintProposal_0')[0].click();");*/
				logger.info("Volvo - proposalpdflement button clicled");
				pdfPath = hondaFolderPath + "/proposal_" + proposalNumber + ".pdf";
				pdfPath = removeFirstLetterIfStartsWithSlash(pdfPath);

				if (driver.getCurrentUrl().contains("Login.aspx")) {
					logger.info("Volvo Application Logged out automatically");
					setExtractionCount(extractedPolicyCount, extractedProposalCount, transactionInfo, exchange);
					throw new Exception("Volvo Application Logged out automatically");
				}

				(new WebDriverWait(driver, 30)).until(ExpectedConditions.numberOfWindowsToBe(2));

				logger.info("<<<<<<<<<<<<<<<<No. of current tabs after proposal click: "
						+ new ArrayList<String>(driver.getWindowHandles()).size());

				if (driver.getCurrentUrl().contains("Login.aspx")) {
					logger.info("Volvo Application Logged out automatically");
					setExtractionCount(extractedPolicyCount, extractedProposalCount, transactionInfo, exchange);
					throw new Exception("Volvo Application Logged out automatically");
				}

				proposalWindowHandle = "";
				switchingURL = "";
				//driver.switchTo().defaultContent();
				for (String winHandle : driver.getWindowHandles()) {
					driver.switchTo().window(winHandle);
				}

				logger.info("bfroe propsal pdf download url -- " + driver.getCurrentUrl());

				/* To Download proposal PDF */
				if (downloadPdf(driver, phantomjsPath, rasterizeJsPath, policyNo, pdfPath, driver.getCurrentUrl(),
						cookies)) {
					extractedProposalCount++;
					logger.info(
							"volvoPolicyPdfExtracter -propsal form PDF Downloaded for proposalNo :: " + proposalNumber);

					volvoPolicyObj.setIsProposalDownloaded(RPAConstants.Y);
					volvoPolicyObj.setProposalPdfPath(pdfPath);
					volvoPolicyObj.setIsProposalUploaded(RPAConstants.N);
				}
				
				if(driver!=null && !driver.getCurrentUrl().contains("VSPrintPolicy"))
					driver.close();
			} else {
				logger.info("volvoPolicyPdfExtracter - Proposal pdf already extracted for policy code :: " + policyNo);
			}
		}
			 }
			 
			else{
		 		logger.info("No record Found for -"+policyNo);
		 		volvoPolicyObj.setIsPolicyDownloaded(RPAConstants.R);
		 		volvoPolicyObj.setIsProposalDownloaded(RPAConstants.R);
		 	}
		}catch(TimeoutException e){
			logger.info("Element not found -->"+e);
		}
			// volvoPolicyObj.setInwardCode(inwardCode);
			volvoPolicyService.save(volvoPolicyObj);

		}
		/* Total policy pdf extratced */
		transactionInfo.setTotalSuccessRecords(String.valueOf(extractedPolicyCount));
		logger.info("volvoPolicyPdfExtracter - Extracted policy count :: " + extractedPolicyCount);

		/* Total proposal pdf extratced */
		transactionInfo.setTotalSuccessUploads((String.valueOf(extractedProposalCount)));
		logger.info("volvoPolicyPdfExtracter - Extracted Proposal count :: " + extractedProposalCount);

		// At the end, come back to the main window
		driver.switchTo().window(mainWindowHandle);

		logger.info("volvoPolicyPdfExtracter - Current URL before logout try ::" + driver.getCurrentUrl());
		WebElement logout = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='hrefLogOut']"), 40);
		logout.click();
		logger.info("volvoPolicyPdfExtracter - END getPolicyNumberListTobeExtractedFromLocalDb()");
		return true;

	}

	private void setExtractionCount(int extractedPolicyCount, int extractedProposalCount,
			TransactionInfo transactionInfo, Exchange exchange) {
		/* Total policy pdf extratced */
		transactionInfo.setTotalSuccessRecords(String.valueOf(extractedPolicyCount));
		logger.info("setExtractionCount - setting Extracted policy count :: " + extractedPolicyCount);

		/* Total proposal pdf extratced */
		transactionInfo.setTotalSuccessUploads((String.valueOf(extractedProposalCount)));
		logger.info("setExtractionCount - setting Extracted Proposal count :: " + extractedProposalCount);

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
		logger.info("volvoPolicyPdfExtracter - is File ::" + file.getName() + " Deleted ?? :: " + isFileDeleted);
		file = null;
		return true;
	}

	public boolean saveAsPadf(WebDriver driver, String pdfPath, ChromeDriver newDriver)
			throws URISyntaxException, InterruptedException, IOException {

		boolean exist = false, isDeleted = false;

		isDeleted = UtilityFile.isFileDeleted(
				UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("volvo.policy.temppdf.location"),
				".pdf");
		if (isDeleted) {
			logger.info(" volvoPolicyPdfExtracter : saveAsPadf - cleared temp_pdf folder for new Download");
		}else{
			logger.info(" volvoPolicyPdfExtracter : saveAsPadf - no file available in temp folder to delete");
		}

		 newDriver.get(driver.getCurrentUrl()); 
		 
		int i = 0;
		do {

			logger.info("volvoPolicyPdfExtracter : saveAsPadf - isFileDownload_ExistAndRename method called here -- download try number :: "
					+ (i + 1));
			
			WebElement proposalpdflement = UtilityFile.waitForElementPresent(newDriver,
					By.xpath("//*[@id='ContentPlaceHolder1_btnPrint']"), 60);
			FluentWait<WebDriver> fluentwaitProposal = UtilityFile.getFluentWaitObject(newDriver, 60, 200);
			/*fluentwaitProposal.until(ExpectedConditions.visibilityOf(proposalpdflement));*/
			fluentwaitProposal.until(ExpectedConditions.elementToBeClickable(proposalpdflement));
			proposalpdflement.click();
			
			logger.info(" volvoPolicyPdfExtracter : saveAsPadf - after clicking proposal export pdf  button download try number "+ (i + 1));
			
			Thread.sleep(3000);

			logger.info("volvoPolicyPdfExtracter : saveAsPadf - isFileDownload_ExistAndRename method called here -- download try number :: "
					+ (i + 1));
			
			exist = UtilityFile.isFileDownload_ExistAndRename(
					UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("volvo.policy.temppdf.location"),
					".pdf", true, pdfPath);
			i++;
			if (i == 10) {
				logger.info("volvoPolicyPdfExtracter : saveAsPadf - file not download after 30 secs of wait ");
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
}
