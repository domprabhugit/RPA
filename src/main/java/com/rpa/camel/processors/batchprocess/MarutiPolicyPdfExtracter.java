/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.ApplicationConfiguration;
import com.rpa.model.BusinessProcess;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.MarutiPolicy;
import com.rpa.service.CommonService;
import com.rpa.service.EmailService;
import com.rpa.service.process.ProcessService;
import com.rpa.service.processors.MarutiPolicyService;
import com.rpa.util.UtilityFile;

public class MarutiPolicyPdfExtracter implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(MarutiPolicyPdfExtracter.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	MarutiPolicyService marutiPolicyService;
	
	@Autowired
	private CommonService commonService;

	int reLogincount = 0;
	
	@SuppressWarnings("static-access")
	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("*********** inside Camel Process of marutipolicyPdfExtracter Called ************");
		logger.info("BEGIN : marutipolicyPdfExtracter Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		driver = (WebDriver) exchange.getIn().getHeader("chromeDriver_maruti");
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.EXTRACTION);
		MarutiPolicyPdfExtracter marutipolicyPdfExtracter = new MarutiPolicyPdfExtracter();
		marutiPolicyService = applicationContext.getBean(MarutiPolicyService.class);
		commonService = applicationContext.getBean(CommonService.class);
		exchange.setProperty("IS_LOGGED_OUT", 0);
		marutipolicyPdfExtracter.doProcess(marutipolicyPdfExtracter, exchange, transactionInfo, driver, marutiPolicyService,commonService);
		logger.info("*********** inside Camel Process of marutipolicyPdfExtracter Processor Ended ************");
	}

	public void doProcess(MarutiPolicyPdfExtracter marutipolicyPdfExtracter, Exchange exchange, TransactionInfo transactionInfo, WebDriver driver,
			MarutiPolicyService marutiPolicyService, CommonService commonService) throws Exception {
		logger.info("BEGIN : marutipolicyPdfExtracter Processor - doProcess Method Called  ");
		/*for(int i=0;i<300;i++)
				{		
			logger.info("i-->"+i);
			commonService.getThresholdFrequencyLevel(RPAConstants.MARUTI_DOWNLOAD_THRESHOLD);
			
				}*/
		try{
		getPolicyNumberListTobeExtractedFromLocalDb(driver, transactionInfo, marutiPolicyService, exchange,commonService);
		}finally{
			//terminateProcess("dsSam%.exe");
		}
		
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : marutipolicyPdfExtracter Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeExtractedFromLocalDb(WebDriver driver, TransactionInfo transactionInfo, MarutiPolicyService marutiPolicyService,
			Exchange exchange, CommonService commonService) throws Exception {
		logger.info("Processor - marutipolicyPdfExtracter - BEGIN getPolicyNumberListTobeExtractedFromLocalDb()  called ");

		String marutiFolderPath = "", marutiHtmlFolderPath = "";
		String phantomjsPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty(RPAConstants.PHANTOMJS_DRIVER_FOLDER) + "/phantomjs";
		
		phantomjsPath = removeFirstLetterIfStartsWithSlash(phantomjsPath);

		String rasterizeJsPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_POLICY_JS_FOLDER)
				+ "/rasterize.js";
		
		rasterizeJsPath = removeFirstLetterIfStartsWithSlash(rasterizeJsPath);

		List<MarutiPolicy> marutiPolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			Calendar currTime = Calendar.getInstance();
			int hour = currTime.get(Calendar.HOUR_OF_DAY);
			if(hour<18){
				marutiPolicyNoList = marutiPolicyService.findPdfToBeDownloaded(Long.valueOf("5"));
			}else{
				marutiPolicyNoList = marutiPolicyService.findPdfToBeDownloaded(commonService.getThresholdFrequencyLevel(RPAConstants.MARUTI_DOWNLOAD_THRESHOLD));
			}
			
		} else {
			String startDate = UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_BACKLOG_STARTDATE);
			String endDate = UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_BACKLOG_ENDDATE);
			marutiPolicyNoList = marutiPolicyService.findPdfToBeDownloadedForBackLogPolicies(startDate, endDate,commonService.getThresholdFrequencyLevel(RPAConstants.MARUTI_DOWNLOAD_THRESHOLD));
		}
		logger.info("marutipolicyPdfExtracter - Unextracted Policies count ::" + marutiPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(marutiPolicyNoList.size()));

		int extractedPolicyCount = 0, extractedProposalCount = 0;
		String policyNo = "0", proposalNumber = "0";

		applicationContext = SpringContext.getAppContext();
		ProcessService processService = applicationContext.getBean(ProcessService.class);
		 commonService = applicationContext.getBean(CommonService.class);
		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());

		ApplicationConfiguration marutiInsuranceApplicationDetails = null,marutiInsuranceApplicationCredentials=null;
		String policyURL = "", proposalURL = "", htmlPath = "", pdfPath = "", url = "";

		marutiInsuranceApplicationDetails = commonService.getApplicationDetails(businessProcess.getId(), RPAConstants.APPID_MARUTI_POLICY);
		
		marutiInsuranceApplicationCredentials = commonService.getApplicationDetails(businessProcess.getId(), RPAConstants.APPID_MARUTI_INSURANCE);

		if (marutiInsuranceApplicationDetails == null) {
			logger.error("Error in getPolicyNumberListTobeExtractedFromLocalDb :: Maruti policy url Application details not configured for process name :: "
					+ transactionInfo.getProcessName());
			return false;
		} else {
			policyURL = marutiInsuranceApplicationDetails.getUrl();
		}

		marutiInsuranceApplicationDetails = commonService.getApplicationDetails(businessProcess.getId(), RPAConstants.APPID_MARUTI_PROPOSAL);
		if (marutiInsuranceApplicationDetails == null) {
			logger.error("Error in getPolicyNumberListTobeExtractedFromLocalDb :: Maruti propsal url Application details not configured for process name :: "
					+ transactionInfo.getProcessName());
			return false;
		} else {
			proposalURL = marutiInsuranceApplicationDetails.getUrl();
		}

		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			marutiFolderPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_POLICY_FOLDER)
					+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY) + RPAConstants.UNDERSCORE + RPAConstants.BACK_LOG;

			marutiHtmlFolderPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_BACKLOGPOLICY_HTML_FOLDER);
		} else {
			marutiFolderPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_POLICY_FOLDER)
					+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY);

			marutiHtmlFolderPath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_POLICY_HTML_FOLDER);
		}
		marutiFolderPath = removeFirstLetterIfStartsWithSlash(marutiFolderPath);

		marutiHtmlFolderPath = removeFirstLetterIfStartsWithSlash(marutiHtmlFolderPath);

		File marutiHtmlFolder = new File(marutiHtmlFolderPath);

		if (!marutiHtmlFolder.exists())
			marutiHtmlFolder.mkdirs();

		File marutiFolder = new File(marutiFolderPath);

		if (!marutiFolder.exists())
			marutiFolder.mkdirs();
		
		for (MarutiPolicy marutiPolicyObj : marutiPolicyNoList) {

			policyNo = marutiPolicyObj.getPolicyNo();
			proposalNumber = marutiPolicyObj.getProposalNumber();

			logger.info("marutipolicyPdfExtracter - traverserred to print policy screen :: URL " + driver.getCurrentUrl());

			if(marutiPolicyObj.getIsPolicyDownloaded()==null)
				marutiPolicyObj.setIsPolicyDownloaded(RPAConstants.N);
			
			/* policy Extraction */
			if (marutiPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
				htmlPath = marutiHtmlFolderPath + "/policy_" + UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY) + "_"
						+ policyNo + ".html";
				htmlPath = removeFirstLetterIfStartsWithSlash(htmlPath);
				pdfPath = marutiFolderPath + "/policy_" + policyNo + ".pdf";
				pdfPath = removeFirstLetterIfStartsWithSlash(pdfPath);
				url = policyURL + policyNo;
				//driver.get(url);
				/* To Download Policy PDF */
				if (downloadPdf(driver, phantomjsPath, rasterizeJsPath, policyNo, htmlPath, pdfPath, url,"A",transactionInfo,marutiInsuranceApplicationCredentials)) {
				
					extractedPolicyCount++;
					logger.info("marutipolicyPdfExtracter -policy PDF Downloaded for policyNo :: " + policyNo);
					
					/* To Delete Policy HTML file */
					logger.info("marutipolicyPdfExtracter - is "+policyNo+"'s html File Deleted ? "+deleteFile(htmlPath));
					
						marutiPolicyObj.setIsPolicyDownloaded(RPAConstants.Y);
						marutiPolicyObj.setPolicyPdfPath(pdfPath);
						marutiPolicyObj.setIsPolicyUploaded(RPAConstants.N);
				}
			} else {
				logger.info("marutipolicyPdfExtracter - Policy pdf already extracted for policy code :: " + policyNo);
			}
			
			if(marutiPolicyObj.getIsProposalDownloaded()==null)
				marutiPolicyObj.setIsProposalDownloaded(RPAConstants.N);

			/* proposal form extraction */
			if (marutiPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.N)) {

				htmlPath = marutiHtmlFolderPath + "/proposal_" + UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY) + "_"
						+ proposalNumber + ".html";
				htmlPath = removeFirstLetterIfStartsWithSlash(htmlPath);
				pdfPath = marutiFolderPath + "/proposal_" + proposalNumber + ".pdf";
				pdfPath = removeFirstLetterIfStartsWithSlash(pdfPath);
				url = proposalURL + policyNo;

				//driver.get(url);
				
				/* To Download proposal PDF */
				if (downloadPdf(driver, phantomjsPath, rasterizeJsPath, policyNo, htmlPath, pdfPath, url,"B",transactionInfo,marutiInsuranceApplicationCredentials)) {
				
					extractedProposalCount++;
					logger.info("marutipolicyPdfExtracter -propsal form PDF Downloaded for proposalNo :: " + proposalNumber);

					/* To Delete Policy HTML file */
					logger.info("marutipolicyPdfExtracter - is "+proposalNumber+"'s html File Deleted ? "+deleteFile(htmlPath));
					marutiPolicyObj.setIsProposalDownloaded(RPAConstants.Y);
					marutiPolicyObj.setProposalPdfPath(pdfPath);
					marutiPolicyObj.setIsProposalUploaded(RPAConstants.N);
				}
			} else {
				logger.info("marutipolicyPdfExtracter - Proposal pdf already extracted for policy code :: " + policyNo);
			}

			//marutiPolicyService.save(marutiPolicyObj);
			marutiPolicyService.updateNonByteMarutiData(marutiPolicyObj);

		}
		/* Total policy pdf extratced */
		transactionInfo.setTotalSuccessRecords(String.valueOf(extractedPolicyCount));
		logger.info("marutipolicyPdfExtracter - Extracted policy count :: " + extractedPolicyCount);

		/* Total proposal pdf extratced */
		transactionInfo.setTotalSuccessUploads((String.valueOf(extractedProposalCount)));
		logger.info("marutipolicyPdfExtracter - Extracted Proposal count :: " + extractedProposalCount);

		//driver.get("https://connect.corp.maruti.co.in/MarutiInsurance/login/,DanaInfo=192.168.1.47+index.aspx");
		driver.get("http://192.168.1.47/MarutiInsurance/login/index.aspx");
		logger.info("marutipolicyPdfExtracter - Current URL before logout try ::" + driver.getCurrentUrl());
		WebElement logoutInsurance = UtilityFile.waitForElementPresent(driver,
				By.xpath("//*[@id='aspnetForm']/table/tbody/tr[1]/td/table/tbody/tr/td[2]/table/tbody/tr[1]/td[2]/a"), 40);
		if(logoutInsurance!=null)
			logoutInsurance.click();
		
		ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
		driver.switchTo().window(tabs.get(0));
		
		WebElement logoutMaruti = UtilityFile.waitForElementPresent(driver,
				By.xpath("//*[@id='imgNavSignOut']"), 40);
		if(logoutMaruti!=null)
		logoutMaruti.click();
		
		
		exchange.setProperty("IS_LOGGED_OUT", 1);
		
		logger.info("marutipolicyPdfExtracter - url after logout maruti --"+driver.getCurrentUrl());
		
		logger.info("marutipolicyPdfExtracter - END getPolicyNumberListTobeExtractedFromLocalDb()");
		return true;

	}


	public boolean downloadPdf(WebDriver driver, String phantomjsPath, String rasterizeJsPath, String policyNo, String htmlPath,
			String pdfPath, String url,String flag,TransactionInfo transactionInfo, ApplicationConfiguration marutiInsuranceApplicationCredentials) throws Exception {
		logger.info("marutipolicyPdfExtracter - BEGIN downloadPdf() called");
		logger.info("marutipolicyPdfExtracter - downloadPdf() driver object ::" + driver);
		boolean result = false;
		logger.info("marutipolicyPdfExtracter - bfore current URL ::" + driver.getCurrentUrl());
		driver.get(url);
		logger.info("marutipolicyPdfExtracter - after current URL ::" + driver.getCurrentUrl());
		
		if(driver.getPageSource().contains("User ID") && driver.getPageSource().contains("Password")){
			reLogincount= reLogincount+1;
			logger.error("marutipolicyPdfExtracter - application logged out automatically - current url :: "+driver.getCurrentUrl() );
			if(reLogincount<=5){
			
			logger.info("maruti - current url------------------ "+driver.getCurrentUrl());
				
				logger.info("marutiApplicationAutomater - before userid xpath check");
			WebElement userElementMarutiInsurance = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='txtUser']"), 40);
			WebElement passwordElementMarutiInsurance = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='txtPassword']"), 40);
			WebElement submitMarutiInsurance = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='Button1']"), 40);
			userElementMarutiInsurance.sendKeys(marutiInsuranceApplicationCredentials.getUsername());
			passwordElementMarutiInsurance.sendKeys(marutiInsuranceApplicationCredentials.getPassword());
			submitMarutiInsurance.click();

			logger.info("marutiApplicationAutomater - after submit click ::"
					+ driver.getCurrentUrl());
			
			if(driver.getCurrentUrl().endsWith("login.aspx")){
				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.carPolicyExtractionNotification(transactionInfo,"Invalid Credentials - Unable to Login into Insurance Maruti Application, Please change credentials if Expired.","Maruti");
				transactionInfo.setProcessFailureReason("Invalid Credentials - Unable to Login into Insurance Maruti Application");
				//marutiInsuranceApplicationDetails.setIsPasswordExpired(RPAConstants.Y);
				throw new Exception("Invalid Credentials - Unable to Login into Maruti Insurance Application");
			}else{
				logger.info("marutiApplicationAutomater - Logged Into Maruti Insurance Application Successfully ::"
						+ driver.getCurrentUrl());
				
				driver.get(url);
				logger.info("marutipolicyPdfExtracter - after current URL ::" + driver.getCurrentUrl());
				
			}
			}else{
				logger.error("marutipolicyPdfExtracter - exceeded re login 5 count attempt");
				throw new Exception("marutiApplicationAutomater - exceeded re login 5 count attempt - Unable to login");
			}
		}
		
		if( (flag.equalsIgnoreCase("A") && driver.getPageSource().contains("../Images/NewRSAsignature.PNG"))
				|| (flag.equalsIgnoreCase("B") && driver.getPageSource().contains("../images/RSACert.gif")) ){
			FileWriter fstream = new FileWriter(htmlPath);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(driver.getPageSource());
			out.close();
			fstream.close();
		}else{
			logger.error("marutipolicyPdfExtracter - Important : Image Src Changed in webpage / cancelled policy  for :: "+policyNo+"- Please change the image name under rpa_upload/maruti/images/ or contact admin");
			FileWriter fstream = new FileWriter(htmlPath);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(driver.getPageSource());
			out.close();
			fstream.close();
			try{
			//EmailService emailService = (EmailService) applicationContext.getBean("emailService");
			//emailService.carPolicyMarutiSrcNotification("Important : Image Src Changed in webpage / cancelled policy  for :: "+policyNo+"- Please change the image name under rpa_upload/maruti/images/ or contact admin","Maruti");
			//transactionInfo.setProcessFailureReason("Image Src Changed in webpage");
			}catch(Exception e){
				logger.error("marutipolicyPdfExtracter - eror while preparing mail");
			}
			//throw new Exception("Important : Image Src Changed in maruti webpage - Please change the image name under rpa_upload/maruti/images/ or contact admin");
		}

		String cmd = phantomjsPath + " " + rasterizeJsPath + " file:///" + htmlPath + " " + pdfPath;
		logger.info("cmd to be executed ::" + cmd);
		Process process = Runtime.getRuntime().exec(cmd);
		int exitStatus = process.waitFor();
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
		logger.info("marutipolicyPdfExtracter - END downloadPdf()");
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
		logger.info("marutipolicyPdfExtracter - is File ::" + file.getName() + " Deleted ?? :: " + isFileDeleted);
		file = null;
		return true;
	}
	
	public static boolean downloadPdfWithOnlineURL(WebDriver driver, String phantomjsPath, String rasterizeJsPath, String policyNo,
			String pdfPath, String pageURL, String cookies) throws IOException, InterruptedException {
		logger.info("tataPolicyPdfExtracter - BEGIN downloadPdfWithOnlineURL() called");
		boolean result = false;

		//pdfPath="D:/Royal_Sundaram/ws_rpa_latest/RPA_WEB/rpa_upload/maruti/20-02-2019/policy_MOP5074687.pdf";
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
		logger.info("marutipolicyPdfExtracter - END downloadPdfWithOnlineURL()");
		return result;
	}
	
	public void terminateProcess(String processName){
		try{
			logger.info("marutipolicyPdfExtracter - terminateProcess called() :: "+processName);
			Process p4Exit = Runtime.getRuntime().exec("wmic process where (Name like '%"+processName+"%') call terminate");
			p4Exit.waitFor();
			/*Process process = Runtime.getRuntime().exec("wmic process where (Name like '%pulseapplicationlauncher.exe%') call terminate");
			process.waitFor();*/
			logger.info("marutipolicyPdfExtracter - Exit value-->"+p4Exit.exitValue());
		} catch (Exception e1) {
			logger.error("marutipolicyPdfExtracter - Error while killing process::"+e1);
			
		}
	}

}
