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
import org.openqa.selenium.TimeoutException;
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

public class HondaApplicationAutomater implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(HondaApplicationAutomater.class.getName());

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
		logger.info("*********** inside Camel Process of hondaApplicationAutomater Called ************");
		logger.info("BEGIN : hondaApplicationAutomater Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		chromeDriverHeadless = applicationContext.getBean(ChromeDriverHeadless.class);
		driver = chromeDriverHeadless.getNewChromeDriver();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.AUTOMATER);
		HondaApplicationAutomater hondaApplicationAutomater = new HondaApplicationAutomater();
		exchange.getIn().setHeader("chromeDriver_honda", driver);
		applicationConfigurationRepository = applicationContext.getBean(ApplicationConfigurationRepository.class);
		hondaApplicationAutomater.doProcess(hondaApplicationAutomater, exchange, transactionInfo, driver,applicationConfigurationRepository);
		logger.info("*********** inside Camel Process of hondaApplicationAutomater Processor Ended ************");
	}

	public void doProcess(HondaApplicationAutomater hondaApplicationAutomater, Exchange exchange,
			TransactionInfo transactionInfo, WebDriver driver, ApplicationConfigurationRepository applicationConfigurationRepository)
			throws Exception {
		logger.info("BEGIN : hondaApplicationAutomater Processor - doProcess Method Called  ");

		doHondaAssureLogin(driver, transactionInfo,applicationConfigurationRepository);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : hondaApplicationAutomater Processor - doProcess Method Ended  ");
	}

	public boolean doHondaAssureLogin(WebDriver driver, TransactionInfo transactionInfo, ApplicationConfigurationRepository applicationConfigurationRepository) throws Exception {
		logger.info("Processor - hondaApplicationAutomater - BEGIN doHondaAssureLogin()  called ");
		applicationContext = SpringContext.getAppContext();
		ProcessService processService = applicationContext.getBean(ProcessService.class);
		CommonService commonService = applicationContext.getBean(CommonService.class);

		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
				RPAConstants.APPID_HONDA);

		if (applicationDetails == null) {
			logger.error("Error in doHondaAssureLogin :: Application details not configured for process name :: "
					+ transactionInfo.getProcessName());
			return false;
		} else {

			driver.get(applicationDetails.getUrl());
			String userName = applicationDetails.getUsername();
			String password = applicationDetails.getPassword();

			WebElement userNameElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='txtUserName']"), 40);
			WebElement passwordElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='txtPassword']"), 40);
			WebElement submit1Element = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='btnLogin']"),
					40);
			if(userNameElement!=null){
			userNameElement.sendKeys(userName);
			passwordElement.sendKeys(password);
			submit1Element.click();
			}else{
				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.carPolicyExtractionNotification(transactionInfo,"Unable to locate element - Please check whether the site is reachable","Honda");
				transactionInfo.setProcessFailureReason("Unable to locate element");
				throw new Exception("Unable to locate element - Please check whether the site is reachable");
			}
			Thread.sleep(3000);
			
			if(isAlertPresent(driver)){
				Alert alert = driver.switchTo().alert();
				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.carPolicyExtractionNotification(transactionInfo,alert.getText(),"Honda");
				transactionInfo.setProcessFailureReason(alert.getText());
				applicationDetails.setIsPasswordExpired(RPAConstants.Y);
				applicationConfigurationRepository.save(applicationDetails);
				throw new Exception("Invalid Credentials - Unable to Login into Honda Application");
			}else{
				if(driver.findElements(By.xpath("//*[@id='btnOkExpire']")).size()!=0){
					UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='btnOkExpire']"), 40).click();
				}
				logger.info("hondaApplicationAutomater - Logged Into Honda Application Successfully::" + driver.getCurrentUrl());
			}
			
			// after login to click policy Issuance menu
			WebElement policyIssuanceElement = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='ctl00_divForCopyBlock']/table/tbody/tr[2]/td/table/tbody/tr/td[2]/a[1]"), 40);
			FluentWait<WebDriver> fluentwait = UtilityFile.getFluentWaitObject(driver, 30, 200);
			fluentwait.until(ExpectedConditions.visibilityOf(policyIssuanceElement));
			fluentwait.until(ExpectedConditions.elementToBeClickable(policyIssuanceElement));
			policyIssuanceElement.click();

			logger.info("hondaApplicationAutomater - clicked on policyIssuance Element ::" + driver.getCurrentUrl());
			
			
			
			// after login to click Print Policy Documents
						WebElement printPolicyDocsElement = UtilityFile.waitForElementPresent(driver,
								By.xpath("//*[@id='ctl00_tblChild']/table/tbody/tr/td/ul[1]/li/a"), 80);
						FluentWait<WebDriver> printPolicyDocsElementFluentwait = UtilityFile.getFluentWaitObject(driver, 30, 200);
						printPolicyDocsElementFluentwait.until(ExpectedConditions.visibilityOf(printPolicyDocsElement));
						printPolicyDocsElementFluentwait.until(ExpectedConditions.elementToBeClickable(printPolicyDocsElement));
						printPolicyDocsElement.click();

			logger.info("Processor - hondaApplicationAutomater - END doHondaAssureLogin() ");
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
