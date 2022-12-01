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

public class TataApplicationAutomater implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(TataApplicationAutomater.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	ChromeDriverHeadless chromeDriverHeadless;

	@Autowired
	WebDriver driver;
	
	@Autowired
	ApplicationConfigurationRepository applicationConfigurationRepository;

	@SuppressWarnings("static-access")
	@Override
	public void process(Exchange exchange)
			throws Exception {
		logger.info("*********** inside Camel Process of tataApplicationAutomater Called ************");
		logger.info("BEGIN : tataApplicationAutomater Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		chromeDriverHeadless = applicationContext.getBean(ChromeDriverHeadless.class);
		driver = chromeDriverHeadless.getNewHeadlessDriverWithPdfExternalOpen(UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("tata.policy.temppdf.location"));
		applicationConfigurationRepository = applicationContext.getBean(ApplicationConfigurationRepository.class);
		
		TataApplicationAutomater tataApplicationAutomater = new TataApplicationAutomater();
		if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)){
			exchange.getIn().setHeader("chromeDriver_tatapv", driver);
			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
			transactionInfo.setProcessPhase(RPAConstants.AUTOMATER);
			tataApplicationAutomater.doProcess(tataApplicationAutomater, exchange, transactionInfo, driver,applicationConfigurationRepository);
		}
		else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){
			exchange.getIn().setHeader("chromeDriver_tatacv", driver);
			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
			transactionInfo.setProcessPhase(RPAConstants.AUTOMATER);
			tataApplicationAutomater.doProcess(tataApplicationAutomater, exchange, transactionInfo, driver,applicationConfigurationRepository);
		}
		logger.info("*********** inside Camel Process of tataApplicationAutomater Processor Ended ************");
	}

	public void doProcess(TataApplicationAutomater tataApplicationAutomater, Exchange exchange,
			TransactionInfo transactionInfo, WebDriver driver, ApplicationConfigurationRepository applicationConfigurationRepository)
			throws Exception {
		logger.info("BEGIN : tataApplicationAutomater Processor - doProcess Method Called  ");

		if (exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)) {
			doTataPVLogin(driver, transactionInfo,exchange,applicationConfigurationRepository);
		}else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)){
			doTataCVLogin(driver, transactionInfo,exchange,applicationConfigurationRepository);
		}
		
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : tataApplicationAutomater Processor - doProcess Method Ended  ");
	}

	public boolean doTataPVLogin(WebDriver driver, TransactionInfo transactionInfo, Exchange exchange, ApplicationConfigurationRepository applicationConfigurationRepository) throws Exception {
		logger.info("Processor - tataApplicationAutomater - BEGIN doTataPVLogin()  called ");
		applicationContext = SpringContext.getAppContext();
		ProcessService processService = applicationContext.getBean(ProcessService.class);
		CommonService commonService = applicationContext.getBean(CommonService.class);

		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
				RPAConstants.APPID_TATA);

		if (applicationDetails == null) {
			logger.error("Error in doTataPVLogin :: Application details not configured for process name :: "
					+ transactionInfo.getProcessName());
			return false;
		} else {

			driver.get(applicationDetails.getUrl());

			logger.info("tataApplicationAutomater - "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" current url is ::"+ driver.getCurrentUrl());
			
			String userName = applicationDetails.getUsername();
			String password = applicationDetails.getPassword();

			WebElement userNameElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='txtUserName']"), 40);
			WebElement passwordElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='txtPassword']"), 40);
			WebElement submit1Element = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='btnLogin']"),
					40);
			if(userNameElement!=null){
			userNameElement.sendKeys(userName);
			passwordElement.sendKeys(password);
			JavascriptExecutor executor = (JavascriptExecutor)driver;
			executor.executeScript("arguments[0].click();", submit1Element);
			}else{
				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.carPolicyExtractionNotification(transactionInfo,"Unable to locate element - Please check whether the site is reachable","Tata");
				transactionInfo.setProcessFailureReason("Unable to locate element");
				throw new Exception("Unable to locate element - Please check whether the site is reachable "+exchange.getProperty(RPAConstants.VEHICLE_TYPE));
			}
			Thread.sleep(3000);
			
			if(isAlertPresent(driver)){
				Alert alert = driver.switchTo().alert();
				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.carPolicyExtractionNotification(transactionInfo,alert.getText(),"Tata - "+exchange.getProperty(RPAConstants.VEHICLE_TYPE));
				transactionInfo.setProcessFailureReason(alert.getText());
				applicationDetails.setIsPasswordExpired(RPAConstants.Y);
				applicationConfigurationRepository.save(applicationDetails);
				driver.switchTo().alert().accept();
				throw new Exception("Invalid Credentials - Unable to Login into Tata Application"+exchange.getProperty(RPAConstants.VEHICLE_TYPE));
			}else{
				logger.info("tataApplicationAutomater - Logged Into "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" Tata Application Successfully :: " + driver.getCurrentUrl());
			}
			
			if(driver.findElements(By.xpath("//*[@id='btnOkExpire']")).size()!=0){
				
				WebElement expireBtnElem = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='btnOkExpire']"), 40);
				JavascriptExecutor executor = (JavascriptExecutor)driver;
				executor.executeScript("arguments[0].click();", expireBtnElem);
			}
			
			if(isAlertPresent(driver)){
				Alert alert = driver.switchTo().alert();
				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.carPolicyExtractionNotification(transactionInfo,alert.getText(),"Tata - "+exchange.getProperty(RPAConstants.VEHICLE_TYPE));
				transactionInfo.setProcessFailureReason(alert.getText());
				applicationDetails.setIsPasswordExpired(RPAConstants.Y);
				applicationConfigurationRepository.save(applicationDetails);
				driver.switchTo().alert().accept();
				throw new Exception("Invalid Credentials - Unable to Login into Tata Application"+exchange.getProperty(RPAConstants.VEHICLE_TYPE));
			}else{
				logger.info("tataApplicationAutomater - Logged Into "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" Tata Application Successfully::" + driver.getCurrentUrl());
			}
			
			if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
				((JavascriptExecutor)driver).executeScript("$('#myslidemenu > ul > li:nth-child(2) > ul > li:nth-child(1) > a')[0].click();");
			else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
				((JavascriptExecutor)driver).executeScript("$('#myslidemenu > ul > li:nth-child(3) > ul > li:nth-child(1) > a')[0].click();");

			logger.info("tataApplicationAutomater "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - clicked on policyIssuance Element ::" + driver.getCurrentUrl());
			
			logger.info("Processor - tataApplicationAutomater - END doTataPVLogin() ");
			return true;

		}

	}

	
	public boolean doTataCVLogin(WebDriver driver, TransactionInfo transactionInfo, Exchange exchange,ApplicationConfigurationRepository applicationConfigurationRepository) throws Exception {
		logger.info("Processor - tataApplicationAutomater - BEGIN doTataCVLogin()  called ");
		applicationContext = SpringContext.getAppContext();
		ProcessService processService = applicationContext.getBean(ProcessService.class);
		CommonService commonService = applicationContext.getBean(CommonService.class);

		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
				RPAConstants.APPID_TATA);

		if (applicationDetails == null) {
			logger.error("Error in doTataCVLogin :: Application details not configured for process name :: "
					+ transactionInfo.getProcessName());
			return false;
		} else {

			driver.get(applicationDetails.getUrl());
			logger.info("tataApplicationAutomater - "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" current url is ::"+ driver.getCurrentUrl());
			String userName = applicationDetails.getUsername();
			String password = applicationDetails.getPassword();

			WebElement userNameElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='txtUserName']"), 40);
			WebElement passwordElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='txtPassword']"), 40);
			WebElement submit1Element = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='btnLogin']"),
					40);
			if(userNameElement!=null){
			userNameElement.sendKeys(userName);
			passwordElement.sendKeys(password);
			JavascriptExecutor executor = (JavascriptExecutor)driver;
			executor.executeScript("arguments[0].click();", submit1Element);
			
			}else{
				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.carPolicyExtractionNotification(transactionInfo,"Unable to locate element - Please check whether the site is reachable","Tata");
				transactionInfo.setProcessFailureReason("Unable to locate element");
				throw new Exception("Unable to locate element "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Please check whether the site is reachable");
			}
			Thread.sleep(3000);
			
			if(isAlertPresent(driver)){
				Alert alert = driver.switchTo().alert();
				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.carPolicyExtractionNotification(transactionInfo,alert.getText(),"Tata - "+exchange.getProperty(RPAConstants.VEHICLE_TYPE));
				transactionInfo.setProcessFailureReason(alert.getText());
				applicationDetails.setIsPasswordExpired(RPAConstants.Y);
				applicationConfigurationRepository.save(applicationDetails);
				driver.switchTo().alert().accept();
				throw new Exception("Invalid Credentials "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Unable to Login into Tata Application");
			}else{
				logger.info("tataApplicationAutomater "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Logged Into Tata Application Successfully::" + driver.getCurrentUrl());
			}
			
			if(driver.findElements(By.xpath("//*[@id='btnOkExpire']")).size()!=0){
				
				WebElement expireBtnElem = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='btnOkExpire']"), 40);
				JavascriptExecutor executor = (JavascriptExecutor)driver;
				executor.executeScript("arguments[0].click();", expireBtnElem);
			}
			
			if(isAlertPresent(driver)){
				Alert alert = driver.switchTo().alert();
				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.carPolicyExtractionNotification(transactionInfo,alert.getText(),"Tata - "+exchange.getProperty(RPAConstants.VEHICLE_TYPE));
				transactionInfo.setProcessFailureReason(alert.getText());
				applicationDetails.setIsPasswordExpired(RPAConstants.Y);
				applicationConfigurationRepository.save(applicationDetails);
				driver.switchTo().alert().accept();
				throw new Exception("Invalid Credentials "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Unable to Login into Tata Application");
			}else{
				logger.info("tataApplicationAutomater "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - Logged Into Tata Application Successfully::" + driver.getCurrentUrl());
			}
			
			if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
				((JavascriptExecutor)driver).executeScript("$('#myslidemenu > ul > li:nth-child(2) > ul > li:nth-child(1) > a')[0].click();");
			else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
				((JavascriptExecutor)driver).executeScript("$('#myslidemenu > ul > li:nth-child(3) > ul > li:nth-child(1) > a')[0].click();");

			logger.info("tataApplicationAutomater "+exchange.getProperty(RPAConstants.VEHICLE_TYPE)+" - clicked on policyIssuance Element ::" + driver.getCurrentUrl());
			
			logger.info("Processor - tataApplicationAutomater - END doTataCVLogin() ");
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
