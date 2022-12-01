/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.processors.uploader;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.ErrorConstants;
import com.rpa.constants.RPAConstants;
import com.rpa.model.ApplicationConfiguration;
import com.rpa.model.BusinessProcess;
import com.rpa.model.TransactionInfo;
import com.rpa.service.CommonService;
import com.rpa.service.process.ProcessService;
import com.rpa.util.UtilityFile;

public class VB64ComplianceUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(VB64ComplianceUploader.class.getName());

	ApplicationContext applicationContext = SpringContext.getAppContext();
	
	public void process(Exchange exchange) throws Exception{

		logger.info("BEGIN : Processor - VB64UploaderHDFCProcessor - BANK NAME::"+exchange.getProperty(RPAConstants.BANK_NAME));

		String bankName = (String) exchange.getProperty(RPAConstants.BANK_NAME);

		String uploadOutputFile =  (String) exchange.getProperty(RPAConstants.UPLOAD_FILE_PATH);

		logger.info("VB64UploaderHDFCProcessor - Upload File Path::"+uploadOutputFile);

		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.UPLOAD);
		boolean uploadFileSuccess = false;
		
		if (uploadOutputFile != null) {
			
			File upload_file = new File(uploadOutputFile);
			
			ProcessService processService = applicationContext.getBean(ProcessService.class);
			CommonService commonService = applicationContext.getBean(CommonService.class);
			
			BusinessProcess businessProcess = processService.findByProcessName(exchange.getFromRouteId());
			ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),RPAConstants.APPID_64VB);

			if(applicationDetails==null){
				logger.error("Error in VB64UploaderHDFCProcessor :: Application details not configured for process name :: "+transactionInfo.getProcessName() );
			}else{
			String applicationURL = applicationDetails.getUrl();
			String userName = applicationDetails.getUsername();
			String password = applicationDetails.getPassword();

			DesiredCapabilities capabilities = new DesiredCapabilities();
			capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, UtilityFile.getCodeBasePath() + "/rpa_drivers/headless/phantomjs.exe");

			WebDriver driver = new PhantomJSDriver(capabilities);	
			driver.get(applicationURL);         
			logger.info("Driver URL::"+driver.getCurrentUrl());

			if(applicationURL.equals(driver.getCurrentUrl())){

				logger.info("Navigated to correct webpage::"+applicationURL);

				WebElement userNameElement = driver.findElement(By.name("enteredLoginID"));
				WebElement passwordElement = driver.findElement(By.name("enteredPassword"));
				WebElement submit1Element = driver.findElement(By.name("Submit"));
				userNameElement.sendKeys(userName);					
				passwordElement.sendKeys(password);					
				submit1Element.submit();

				logger.info("Processor - VB64UploaderHDFCProcessor - Logged Into Application Successfully::"+applicationURL);

				logger.info("VB64UploaderHDFCProcessor - File Name::"+upload_file.getName());

				if(!upload_file.getName().contains("_done")){

					FluentWait<WebDriver> fluentWait = new FluentWait<>(driver)
							.withTimeout(30, TimeUnit.SECONDS)
							.pollingEvery(200, TimeUnit.MILLISECONDS)
							.ignoring(NoSuchElementException.class);

					// Get all of the options
					List<WebElement> options1 = driver.findElements(By.xpath("//ul/li"));
					// Loop through the options and select the one that matches
					for (WebElement opt : options1) {
						if (opt.getText().contains("UPLOAD")) {
							opt.click();
							logger.info("Processor - VB64UploaderHDFCProcessor - Upload Menu Option Clicked::"+opt.getText());
							break;
						}
					}

					// Get all of the options
					List<WebElement> options2 = driver.findElements(By.xpath("//ul/li/ul/li/a"));
					// Loop through the options and select the one that matches
					for (WebElement opt : options2) {
						if(bankName.equals("HDFC") 
								&& opt.getAttribute("href").contains("fileUpload.do?method=init&typeid=103")){
							opt.click();
							break;
						} else if(bankName.equals("CITI") 
								&& opt.getAttribute("href").contains("fileUpload.do?method=init&typeid=102")){
							opt.click();
							break;
						} else if(bankName.equals("AXIS") 
								&& opt.getAttribute("href").contains("fileUpload.do?method=init&typeid=106")){
							opt.click();
							break;
						} else if(bankName.equals("SCB") 
								&& opt.getAttribute("href").contains("fileUpload.do?method=init&typeid=105")){
							opt.click();
							break;
						} else if(bankName.equals("HSBC") 
								&& opt.getAttribute("href").contains("fileUpload.do?method=init&typeid=108")){
							opt.click();
							break;
						}
					}

					WebElement uploadFileElement = driver.findElement(By.xpath("//input[@type='file']"));
					WebElement submit2Element = driver.findElement(By.name("Submit2"));
					uploadFileElement.sendKeys(upload_file.getPath());
					submit2Element.submit();      			

					logger.info("Processor - VB64UploaderHDFCProcessor - File Upload Button Clicked::"+upload_file.getName());
					
					FluentWait<WebDriver> uploadWait = new FluentWait<WebDriver>(driver).withTimeout(60, TimeUnit.MINUTES)
							.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
					uploadWait.until(ExpectedConditions.invisibilityOfElementLocated(
							By.xpath("//*[@id='uploadsts1']")));
					
					logger.info("Processor - VB64UploaderHDFCProcessor - error element size :: "+driver.findElements(By.xpath("//*[@id='mainForm']/li")).size());
					
					if(driver.findElements(By.xpath("//*[@id='mainForm']/li")).size()>0)
					
					{
						logger.info("Processor - VB64UploaderHDFCProcessor - error Text :: "+driver.findElement(By.xpath("//*[@id='mainForm']/li")).getText());
						transactionInfo.setProcessFailureReason(driver.findElement(By.xpath("//*[@id='mainForm']/li")).getText());
					}
					else
					{	

					WebElement processElement = driver.findElement(By.xpath("//input[@property='Process']"));

					fluentWait.until(ExpectedConditions.visibilityOf(processElement));

					String transactionID = driver.findElement(By.xpath("//table//tr//td//table//tr//td//table//tr//td[contains(text(),'Transaction ID')]")).getText();

					logger.info("Processor - VB64UploaderHDFCProcessor - Transaction ID::"+transactionID);

					transactionInfo.setExternalTransactionRefNo(transactionID);
					exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

					processElement.click();

					logger.info("Processor - VB64UploaderHDFCProcessor - Process Button Clicked::"+upload_file.getName());

					/*if(bankName.equals("HDFC")) {
						upload_file.renameTo(new File(UtilityFile.getCodeBasePath()
								+ UtilityFile.getUploadProperty(RPAConstants.VB64_HDFC_FOLDER_PROCESSED_FILE)+"\\" + upload_file.getName()));
					} else if(bankName.equals("CITI")) {
						upload_file.renameTo(new File(UtilityFile.getCodeBasePath()
								+ UtilityFile.getUploadProperty(RPAConstants.VB64_CITI_FOLDER_PROCESSED_FILE)+"\\" + upload_file.getName()));
					} else if(bankName.equals("AXIS")) {
						upload_file.renameTo(new File(UtilityFile.getCodeBasePath()
								+ UtilityFile.getUploadProperty(RPAConstants.VB64_AXIS_FOLDER_PROCESSED_FILE)+"\\" + upload_file.getName()));
					} else if(bankName.equals("SCB")) {
						upload_file.renameTo(new File(UtilityFile.getCodeBasePath()
								+ UtilityFile.getUploadProperty(RPAConstants.VB64_SCB_FOLDER_PROCESSED_FILE)+"\\" + upload_file.getName()));
					} else if(bankName.equals("HSBC")) {
						upload_file.renameTo(new File(UtilityFile.getCodeBasePath()
								+ UtilityFile.getUploadProperty(RPAConstants.VB64_HSBC_FOLDER_PROCESSED_FILE)+"\\" + upload_file.getName()));
					} */
					if(bankName.equals("HDFC")) {
						 FileUtils.copyFile(upload_file, new File(UtilityFile.getCodeBasePath()
									+ UtilityFile.getUploadProperty(RPAConstants.VB64_HDFC_FOLDER_PROCESSED_FILE)+"\\" + upload_file.getName()));
					} else if(bankName.equals("CITI")) {
						 FileUtils.copyFile(upload_file, new File(UtilityFile.getCodeBasePath()
									+ UtilityFile.getUploadProperty(RPAConstants.VB64_CITI_FOLDER_PROCESSED_FILE)+"\\" + upload_file.getName()));
					} else if(bankName.equals("AXIS")) {
						 FileUtils.copyFile(upload_file, new File(UtilityFile.getCodeBasePath()
									+ UtilityFile.getUploadProperty(RPAConstants.VB64_AXIS_FOLDER_PROCESSED_FILE)+"\\" + upload_file.getName()));
					} else if(bankName.equals("SCB")) {
						 FileUtils.copyFile(upload_file, new File(UtilityFile.getCodeBasePath()
									+ UtilityFile.getUploadProperty(RPAConstants.VB64_SCB_FOLDER_PROCESSED_FILE)+"\\" + upload_file.getName()));
					} else if(bankName.equals("HSBC")) {
						 FileUtils.copyFile(upload_file, new File(UtilityFile.getCodeBasePath()
									+ UtilityFile.getUploadProperty(RPAConstants.VB64_HSBC_FOLDER_PROCESSED_FILE)+"\\" + upload_file.getName()));
					} 
					uploadFileSuccess = true;	
				}
					}
				else {
					logger.info("VB64UploaderHDFCProcessor - Upload folder contains only the completed files.");
				}
				List<WebElement> options3 = driver.findElements(By.xpath("//ul/li"));

				for (WebElement opt : options3) {
					if (opt.getText().contains("LOGOUT")) {
						opt.click();
						logger.info("Processor - VB64UploaderHDFCProcessor - Logged out successfully from the appliation::"+applicationURL);
						break;
					}
				}
			} else {
				logger.info("URL does not match::"+applicationURL);
			} 

			driver.quit();	
			
			if(!uploadFileSuccess){
				throw new Exception("Upload Failed::Transaction ID::"+transactionInfo.getId());
			} else {
				transactionInfo.setProcessStatus(RPAConstants.Success);
				transactionInfo.setProcessPhase(RPAConstants.PROCESSED);
				transactionInfo.setProcessSuccessReason(ErrorConstants.ERROR_CODE_00011);
			}
		} 
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
	}
		logger.info("END : Processor - VB64UploaderHDFCProcessor");
	}
}