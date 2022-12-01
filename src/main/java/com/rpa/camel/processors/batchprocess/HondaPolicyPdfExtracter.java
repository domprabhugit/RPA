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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.HondaPolicy;
import com.rpa.service.CommonService;
import com.rpa.service.processors.HondaPolicyService;
import com.rpa.util.UtilityFile;

public class HondaPolicyPdfExtracter implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(HondaPolicyPdfExtracter.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	HondaPolicyService hondaPolicyService;
	
	@Autowired
	private CommonService commonService;

	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("*********** inside Camel Process of hondaPolicyPdfExtracter Called ************");
		logger.info("BEGIN : hondaPolicyPdfExtracter Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		driver = (WebDriver) exchange.getIn().getHeader("chromeDriver_honda");
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.EXTRACTION);
		HondaPolicyPdfExtracter hondaPolicyPdfExtracter = new HondaPolicyPdfExtracter();
		hondaPolicyService = applicationContext.getBean(HondaPolicyService.class);
		commonService = applicationContext.getBean(CommonService.class);
		hondaPolicyPdfExtracter.doProcess(hondaPolicyPdfExtracter, exchange, transactionInfo, driver, hondaPolicyService,commonService);
		logger.info("*********** inside Camel Process of hondaPolicyPdfExtracter Processor Ended ************");
	}

	public void doProcess(HondaPolicyPdfExtracter hondaPolicyPdfExtracter, Exchange exchange, TransactionInfo transactionInfo, WebDriver driver,
			HondaPolicyService hondaPolicyService, CommonService commonService) throws Exception {
		logger.info("BEGIN : hondaPolicyPdfExtracter Processor - doProcess Method Called  ");

		getPolicyNumberListTobeExtractedFromLocalDb(driver, transactionInfo, hondaPolicyService, exchange,commonService);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : hondaPolicyPdfExtracter Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeExtractedFromLocalDb(WebDriver driver, TransactionInfo transactionInfo, HondaPolicyService hondaPolicyService,
			Exchange exchange, CommonService commonService) throws Exception {
		logger.info("Processor - hondaPolicyPdfExtracter - BEGIN getPolicyNumberListTobeExtractedFromLocalDb()  called ");

		String hondaFolderPath = "", hondaHtmlFolderPath = "";
		String phantomjsPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty(RPAConstants.PHANTOMJS_DRIVER_FOLDER) + "/phantomjs";
		phantomjsPath = removeFirstLetterIfStartsWithSlash(phantomjsPath);

		String rasterizeJsPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty(RPAConstants.CAR_POLICY_JS_FOLDER)
				+ "/rasterize.js";
		rasterizeJsPath = removeFirstLetterIfStartsWithSlash(rasterizeJsPath);

		List<HondaPolicy> hondaPolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			hondaPolicyNoList = hondaPolicyService.findPdfToBeDownloaded(commonService.getThresholdFrequencyLevel(RPAConstants.HONDA_DOWNLOAD_THRESHOLD));
		} else {
			String startDate = UtilityFile.getCarPolicyProperty(RPAConstants.HONDA_BACKLOG_STARTDATE);
			String endDate = UtilityFile.getCarPolicyProperty(RPAConstants.HONDA_BACKLOG_ENDDATE);
			hondaPolicyNoList = hondaPolicyService.findPdfToBeDownloadedForBackLogPolicies(startDate, endDate,commonService.getThresholdFrequencyLevel(RPAConstants.HONDA_DOWNLOAD_THRESHOLD));
		}
		logger.info("hondaPolicyPdfExtracter - Unextracted Policies count ::" + hondaPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(hondaPolicyNoList.size()));

		int extractedPolicyCount = 0, extractedProposalCount = 0;
		String policyNo = "0", proposalNumber = "0";

		applicationContext = SpringContext.getAppContext();
		 commonService = applicationContext.getBean(CommonService.class);

		String policyURL = "", proposalURL = "", htmlPath = "", pdfPath = "", url = "";

		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			hondaFolderPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty(RPAConstants.HONDA_POLICY_FOLDER)
					+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY) + RPAConstants.UNDERSCORE + RPAConstants.BACK_LOG;

			hondaHtmlFolderPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty(RPAConstants.HONDA_BACKLOGPOLICY_HTML_FOLDER);
		} else {
			hondaFolderPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty(RPAConstants.HONDA_POLICY_FOLDER)
					+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY);

			hondaHtmlFolderPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty(RPAConstants.HONDA_POLICY_HTML_FOLDER);
		}
		hondaFolderPath = removeFirstLetterIfStartsWithSlash(hondaFolderPath);

		hondaHtmlFolderPath = removeFirstLetterIfStartsWithSlash(hondaHtmlFolderPath);

		File hondaHtmlFolder = new File(hondaHtmlFolderPath);

		if (!hondaHtmlFolder.exists())
			hondaHtmlFolder.mkdirs();

		File hondaFolder = new File(hondaFolderPath);

		if (!hondaFolder.exists())
			hondaFolder.mkdirs();

		String cookies = "", policyWindowHandle = "",proposalWindowHandle = "",switchingURL="",mainWindowHandle="";
		for(Cookie ck : driver.manage().getCookies()){						
			cookies = ck.getName()+";"+ck.getValue()+";"+ck.getDomain()+";"+ck.getPath()+";"+ck.getExpiry()+";"+ck.isSecure();
            logger.info(ck.getName()+";"+ck.getValue()+";"+ck.getDomain()+";"+ck.getPath()+";"+ck.getExpiry()+";"+ck.isSecure());																									
        }	
		
		mainWindowHandle = driver.getWindowHandle();
		
		for (HondaPolicy hondaPolicyObj : hondaPolicyNoList) {
			
			for (String winHandle : driver.getWindowHandles()) {
				if(!winHandle.equalsIgnoreCase(mainWindowHandle)){
					 driver.switchTo().window(winHandle);
					driver.close();
				}
			}
			
			driver.switchTo().window(mainWindowHandle);
			
			/*for (String winHandle : driver.getWindowHandles()) {
				driver.switchTo().window(winHandle);
				break;
			}*/
			
			policyNo = hondaPolicyObj.getPolicyNo();
			proposalNumber = hondaPolicyObj.getProposalNumber();

			
			WebElement claimNumberElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='ctl00_ContentPlaceHolder1_txtPolNo']"), 40);
			claimNumberElement.clear();
			claimNumberElement.sendKeys(policyNo);
			
			logger.info("hondaPolicyPdfExtracter - policyNo ::  " + policyNo);
			
			// after login to click policy search element 
			WebElement policysearchElement = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='ctl00_ContentPlaceHolder1_btnSearch']"), 40);
			FluentWait<WebDriver> fluentwait = UtilityFile.getFluentWaitObject(driver, 30, 200);
			fluentwait.until(ExpectedConditions.visibilityOf(policysearchElement));
			fluentwait.until(ExpectedConditions.elementToBeClickable(policysearchElement));
			policysearchElement.click();

			//Thread.sleep(2000);
			
			if(waitForElementPresentWithOutErrorThrows(driver,
			By.xpath("//*[@id='ctl00_ContentPlaceHolder1_gvPrintPolicy_ctl02_lblPolicyNo']"),
			2)==null){
				hondaPolicyObj.setIsPolicyDownloaded(RPAConstants.R);
				hondaPolicyObj.setIsProposalDownloaded(RPAConstants.R);
			}else{
			
			UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='ctl00_ContentPlaceHolder1_gvPrintPolicy']/tbody/tr[2]/td[1]/span[contains(text(),'"
					+ policyNo + "')]"),
					30);
			
			if(hondaPolicyObj.getIsPolicyDownloaded()==null)
				hondaPolicyObj.setIsPolicyDownloaded(RPAConstants.N);
			
			/* policy Extraction */
			if (hondaPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {

				logger.info("<<<<<<<<<<<<<<<<No. of current tabs before policy click: " + new ArrayList<String>(driver.getWindowHandles()).size());
				WebElement policypdflement = null;
				try{
				 policypdflement =UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='ctl00_ContentPlaceHolder1_gvPrintPolicy']/tbody/tr[2]/td[7]/input"),20);
				}catch(Exception e){
					logger.error(" hondaPolicyPdfExtracter() - Unable to get element after waiting for 420 secs, could be No record scenario ::",e);
				}
				if(policypdflement==null){
					hondaPolicyObj.setIsPolicyDownloaded("R");
					hondaPolicyObj.setIsProposalDownloaded("R");
				}else{
					FluentWait<WebDriver> fluentwaitPolicy = UtilityFile.getFluentWaitObject(driver, 30, 200);
					fluentwaitPolicy.until(ExpectedConditions.visibilityOf(policypdflement));
					fluentwaitPolicy.until(ExpectedConditions.elementToBeClickable(policypdflement));
					policypdflement.click();
					
					//Thread.sleep(2000);
					(new WebDriverWait(driver, 30)).until(ExpectedConditions.or(ExpectedConditions.numberOfWindowsToBe(2),ExpectedConditions.numberOfWindowsToBe(3)));
					
					logger.info("<<<<<<<<<<<<<<<No. of current tabs after policy click: " + new ArrayList<String>(driver.getWindowHandles()).size());
					
					policyWindowHandle="";switchingURL="";
					for (String winHandle : driver.getWindowHandles()) {
						driver.switchTo().window(winHandle);
						switchingURL = driver.getCurrentUrl();
						logger.info("switchingURL bfr policy download --"+switchingURL);
						if(switchingURL.contains("PolicyViewSchedule.aspx")){
							policyWindowHandle = winHandle;
						}else if(switchingURL.contains("ROYALAddOn_Docs.aspx")){
							if(!policyWindowHandle.equalsIgnoreCase("")){
								driver.switchTo().window(policyWindowHandle);
							}
						}
						
					}
					logger.info("current url -- "+driver.getCurrentUrl() );
					
					htmlPath = hondaHtmlFolderPath + "/policy_" + UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY) + "_"
							+ policyNo + ".html";
					htmlPath = removeFirstLetterIfStartsWithSlash(htmlPath);
					pdfPath = hondaFolderPath + "/policy_" + policyNo + ".pdf";
					pdfPath = removeFirstLetterIfStartsWithSlash(pdfPath);
					url = policyURL + policyNo;
					/* To Download Policy PDF */
					if (downloadPdf(driver, phantomjsPath, rasterizeJsPath, policyNo, htmlPath, pdfPath, url,driver.getCurrentUrl(),cookies)) {
						extractedPolicyCount++;
						logger.info("hondaPolicyPdfExtracter -policy PDF Downloaded for policyNo :: " + policyNo);
						
						/* To Delete Policy HTML file */
						logger.info("hondaPolicyPdfExtracter - is "+policyNo+"'s html File Deleted ? "+deleteFile(htmlPath));
							hondaPolicyObj.setIsPolicyDownloaded(RPAConstants.Y);
							hondaPolicyObj.setPolicyPdfPath(pdfPath);
							hondaPolicyObj.setIsPolicyUploaded(RPAConstants.N);
					}
				}
				
				
				//driver.close();
			} else {
				logger.info("hondaPolicyPdfExtracter - Policy pdf already extracted for policy code :: " + policyNo);
			}
			
			if(hondaPolicyObj.getIsProposalDownloaded()==null)
				hondaPolicyObj.setIsProposalDownloaded(RPAConstants.N);

			/* proposal form extraction */
			if (hondaPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.N)) {
				logger.info(" before url -- "+driver.getCurrentUrl() );
				
				//driver.switchTo().window(mainWindowHandle);
				
				for (String winHandle : driver.getWindowHandles()) {
					if(!winHandle.equalsIgnoreCase(mainWindowHandle)){
						 driver.switchTo().window(winHandle);
						driver.close();
					}
				}
				
				driver.switchTo().window(mainWindowHandle);

				logger.info("current url -- "+driver.getCurrentUrl() );
				
				logger.info("<<<<<<<<<<<<<<<<No. of current tabs before proposal click: " + new ArrayList<String>(driver.getWindowHandles()).size());
				
				// to open proposal pdf
				WebElement proposalpdflement = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='ctl00_ContentPlaceHolder1_gvPrintPolicy_ctl02_btnProposal']"), 40);
				FluentWait<WebDriver> fluentwaitProposal = UtilityFile.getFluentWaitObject(driver, 30, 200);
				fluentwaitProposal.until(ExpectedConditions.visibilityOf(proposalpdflement));
				fluentwaitProposal.until(ExpectedConditions.elementToBeClickable(proposalpdflement));
				proposalpdflement.click();
				
				htmlPath = hondaHtmlFolderPath + "/proposal_" + UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY) + "_"
						+ proposalNumber + ".html";
				htmlPath = removeFirstLetterIfStartsWithSlash(htmlPath);
				pdfPath = hondaFolderPath + "/proposal_" + proposalNumber + ".pdf";
				pdfPath = removeFirstLetterIfStartsWithSlash(pdfPath);
				url = proposalURL + policyNo;
				
				//Thread.sleep(2000);
				(new WebDriverWait(driver, 30)).until(ExpectedConditions.or(ExpectedConditions.numberOfWindowsToBe(2),ExpectedConditions.numberOfWindowsToBe(3)));
				
				logger.info("<<<<<<<<<<<<<<<<No. of current tabs after proposal click: " + new ArrayList<String>(driver.getWindowHandles()).size());
				
				proposalWindowHandle="";switchingURL="";
				for (String winHandle : driver.getWindowHandles()) {
					//driver.switchTo().window(winHandle);
					driver.switchTo().window(winHandle);
					switchingURL = driver.getCurrentUrl();
					/*switchingURL = driver.switchTo().window(winHandle).getCurrentUrl();*/
					logger.info("switchingURL bfr propsal download --"+switchingURL);
					if(switchingURL.contains("PrintProposalPreview.aspx")){
						proposalWindowHandle = winHandle;
					}else if(switchingURL.contains("PolicyViewSchedule.aspx") || switchingURL.contains("ROYALAddOn_Docs.aspx")){
						//driver.switchTo().window(winHandle);
						//driver.close();
						if(!proposalWindowHandle.equalsIgnoreCase("")){
							driver.switchTo().window(proposalWindowHandle);
						}
					}
					}

				logger.info("bfroe propsal pdf download url -- "+driver.getCurrentUrl() );
				
				/* To Download proposal PDF */
				if (downloadPdf(driver, phantomjsPath, rasterizeJsPath, policyNo, htmlPath, pdfPath, url,driver.getCurrentUrl(),cookies)) {
					extractedProposalCount++;
					logger.info("hondaPolicyPdfExtracter -propsal form PDF Downloaded for proposalNo :: " + proposalNumber);

					/* To Delete Policy HTML file */
					logger.info("hondaPolicyPdfExtracter - is "+proposalNumber+"'s html File Deleted ? "+deleteFile(htmlPath));
					hondaPolicyObj.setIsProposalDownloaded(RPAConstants.Y);
					hondaPolicyObj.setProposalPdfPath(pdfPath);
					hondaPolicyObj.setIsProposalUploaded(RPAConstants.N);
				}
			} else {
				logger.info("hondaPolicyPdfExtracter - Proposal pdf already extracted/ no record for policy code :: " + policyNo);
			}
		}
			//hondaPolicyObj.setInwardCode(inwardCode);
			hondaPolicyService.save(hondaPolicyObj);

		}
		/* Total policy pdf extratced */
		transactionInfo.setTotalSuccessRecords(String.valueOf(extractedPolicyCount));
		logger.info("hondaPolicyPdfExtracter - Extracted policy count :: " + extractedPolicyCount);

		/* Total proposal pdf extratced */
		transactionInfo.setTotalSuccessUploads((String.valueOf(extractedProposalCount)));
		logger.info("hondaPolicyPdfExtracter - Extracted Proposal count :: " + extractedProposalCount);

	// At the end, come back to the main window
		driver.switchTo().window(mainWindowHandle);
		
		logger.info("hondaPolicyPdfExtracter - Current URL before logout try ::" + driver.getCurrentUrl());
		WebElement logout = UtilityFile.waitForElementPresent(driver,
				By.xpath("//*[@id='ctl00_divForCopyBlock']/table/tbody/tr[1]/td/table/tbody/tr/td[2]/table/tbody/tr/td[11]/a"), 40);
		logout.click();
		logger.info("hondaPolicyPdfExtracter - END getPolicyNumberListTobeExtractedFromLocalDb()");
		return true;

	}


	public static boolean downloadPdf(WebDriver driver, String phantomjsPath, String rasterizeJsPath, String policyNo, String htmlPath,
			String pdfPath, String url, String pageURL, String cookies) throws IOException, InterruptedException {
		logger.info("hondaPolicyPdfExtracter - BEGIN downloadPdf() called");
		boolean result = false;

		String cmd = phantomjsPath + " " + rasterizeJsPath + " " +   pageURL + " " + pdfPath +" "+cookies;
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
		logger.info("hondaPolicyPdfExtracter - END downloadPdf()");
		return result;
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
		logger.info("hondaPolicyPdfExtracter - is File ::" + file.getName() + " Deleted ?? :: " + isFileDeleted);
		file = null;
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
