/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
import com.rpa.constants.RPAConstants;
import com.rpa.model.ApplicationConfiguration;
import com.rpa.model.BusinessProcess;
import com.rpa.model.TransactionInfo;
import com.rpa.repository.ApplicationConfigurationRepository;
import com.rpa.service.CommonService;
import com.rpa.service.EmailService;
import com.rpa.service.process.ProcessService;
import com.rpa.util.UtilityFile;

public class VolvoApplicationAutomater implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(VolvoApplicationAutomater.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	ChromeDriverHeadless chromeDriverHeadless;

	@Autowired
	WebDriver driver;
	
	@Autowired
	ApplicationConfigurationRepository applicationConfigurationRepository;

	@Override
	public void process(Exchange exchange)
			throws Exception {
		logger.info("*********** inside Camel Process of vovloApplicationAutomater Called ************");
		logger.info("BEGIN : vovloApplicationAutomater Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		chromeDriverHeadless = applicationContext.getBean(ChromeDriverHeadless.class);
		driver = chromeDriverHeadless.getNewHeadlessDriverWithPdfExternalOpen(UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("volvo.policy.temppdf.location"));
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.AUTOMATER);
		VolvoApplicationAutomater vovloApplicationAutomater = new VolvoApplicationAutomater();
		exchange.getIn().setHeader("chromeDriver_volvo", driver);
		applicationConfigurationRepository = applicationContext.getBean(ApplicationConfigurationRepository.class);
		vovloApplicationAutomater.doProcess(vovloApplicationAutomater, exchange, transactionInfo, driver,applicationConfigurationRepository);
		logger.info("*********** inside Camel Process of vovloApplicationAutomater Processor Ended ************");
	}

	public void doProcess(VolvoApplicationAutomater vovloApplicationAutomater, Exchange exchange,
			TransactionInfo transactionInfo, WebDriver driver, ApplicationConfigurationRepository applicationConfigurationRepository)
			throws Exception {
		logger.info("BEGIN : vovloApplicationAutomater Processor - doProcess Method Called  ");

		dovolvoLogin(driver, transactionInfo,applicationConfigurationRepository);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : vovloApplicationAutomater Processor - doProcess Method Ended  ");
	}

	public boolean dovolvoLogin(WebDriver driver, TransactionInfo transactionInfo, ApplicationConfigurationRepository applicationConfigurationRepository) throws Exception {
		logger.info("Processor - vovloApplicationAutomater - BEGIN dovolvoLogin()  called ");
		applicationContext = SpringContext.getAppContext();
		ProcessService processService = applicationContext.getBean(ProcessService.class);
		CommonService commonService = applicationContext.getBean(CommonService.class);

		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
				RPAConstants.APPID_VOLVO);

		if (applicationDetails == null) {
			logger.error("Error in dovolvoLogin :: Application details not configured for process name :: "
					+ transactionInfo.getProcessName());
			return false;
		} else {

			driver.get(applicationDetails.getUrl());
			String userName = applicationDetails.getUsername();
			String password = applicationDetails.getPassword();

			WebElement userNameElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='txtUsrsName']"), 40);
			WebElement passwordElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='txtPWD']"), 40);
			WebElement submit1Element = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='btnLogin']"),
					40);
			if(userNameElement!=null){
			userNameElement.sendKeys(userName);
			passwordElement.sendKeys(password);
			JavascriptExecutor executor = (JavascriptExecutor)driver;
			executor.executeScript("arguments[0].click();", submit1Element);
			}else{
				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.carPolicyExtractionNotification(transactionInfo,"Unable to locate element - Please check whether the site is reachable","volvo");
				transactionInfo.setProcessFailureReason("Unable to locate element");
				throw new Exception("Unable to locate element - Please check whether the site is reachable");
			}
			Thread.sleep(3000);
			
			if(isAlertPresent(driver)){
				Alert alert = driver.switchTo().alert();
				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.carPolicyExtractionNotification(transactionInfo,alert.getText(),"volvo");
				transactionInfo.setProcessFailureReason(alert.getText());
				applicationDetails.setIsPasswordExpired(RPAConstants.Y);
				applicationConfigurationRepository.save(applicationDetails);
				throw new Exception("Invalid Credentials - Unable to Login into volvo Application");
			}else if(driver.findElements(By.xpath("//*[@id='btnOkExpire']")).size()!=0){
				
				WebElement expireBtnElem = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='btnOkExpire']"), 40);
				JavascriptExecutor executor = (JavascriptExecutor)driver;
				executor.executeScript("arguments[0].click();", expireBtnElem);
				logger.info("vovloApplicationAutomater - Logged Into volvo Application Successfully::" + driver.getCurrentUrl());
				
				
				
			} else if(driver.findElements(By.xpath("//*[@id='divExpiry']")).size()!=0){
				WebElement expireBtnElem = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='btnOkExpire']"), 40);
				JavascriptExecutor executor = (JavascriptExecutor)driver;
				executor.executeScript("arguments[0].click();", expireBtnElem);
				logger.info("vovloApplicationAutomater - Logged Into volvo Application Successfully::" + driver.getCurrentUrl());
				//*[@id="btnOkExpire"]
			} else{
				logger.info("vovloApplicationAutomater - Logged Into volvo Application Successfully::" + driver.getCurrentUrl());
			}
			
			/*if(driver.findElements(By.xpath("//*[@id='btnOkExpire']")).size()!=0){
				
				WebElement expireBtnElem = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='btnOkExpire']"), 40);
				JavascriptExecutor executor = (JavascriptExecutor)driver;
				executor.executeScript("arguments[0].click();", expireBtnElem);
			}
			
			if(isAlertPresent(driver)){
				Alert alert = driver.switchTo().alert();
				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.carPolicyExtractionNotification(transactionInfo,alert.getText(),"volvo");
				transactionInfo.setProcessFailureReason(alert.getText());
				throw new Exception("Invalid Credentials - Unable to Login into volvo Application");
			}else{
				logger.info("vovloApplicationAutomater - Logged Into volvo Application Successfully::" + driver.getCurrentUrl());
			}*/
			
			((JavascriptExecutor)driver).executeScript("$('#smoothmenu1 > ul > li:nth-child(2) > ul > li:nth-child(1) > a')[0].click();");
			
			((JavascriptExecutor)driver).executeScript("$('#smoothmenu1 > ul > li:nth-child(2) > ul > li:nth-child(1) > a')[0].click();");

			logger.info("vovloApplicationAutomater - clicked on policyIssuance Element ::" + driver.getCurrentUrl());
			
			logger.info("Processor - vovloApplicationAutomater - END dovolvoLogin() ");
			return true;

		}

	}

	
	public boolean isAlertPresent(WebDriver driver){
	    boolean foundAlert = false;
	    WebDriverWait wait = new WebDriverWait(driver, 0 /*timeout in seconds*/);
	    try {
	        wait.until(ExpectedConditions.alertIsPresent());
	        foundAlert = true;
	    } catch (TimeoutException eTO) {
	        foundAlert = false;
	    }
	    return foundAlert;
	}
}