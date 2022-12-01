/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
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

public class MarutiApplicationAutomater implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(MarutiApplicationAutomater.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	ChromeDriverHeadless chromeDriverHeadless;
	
	@Autowired
	ApplicationConfigurationRepository applicationConfigurationRepository;
	
	@Autowired
	WebDriver driver;
	
	int retryCount = 0;

	@SuppressWarnings("static-access")
	@Override
	public void process(Exchange exchange)
			throws Exception {
		logger.info("*********** inside Camel Process of marutiApplicationAutomater Called ************");
		logger.info("BEGIN : marutiApplicationAutomater Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		chromeDriverHeadless = applicationContext.getBean(ChromeDriverHeadless.class);
		driver = chromeDriverHeadless.getNewMarutiChromeDriver();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.AUTOMATER);
		exchange.setProperty("IS_LOGGED_OUT", 0);
		exchange.setProperty("IS_LOGGED_IN", 0);
		MarutiApplicationAutomater marutiApplicationAutomater = new MarutiApplicationAutomater();
		//marutiApplicationAutomater.terminateProcess("dsSam%.exe");
		exchange.getIn().setHeader("chromeDriver_maruti", driver);
		applicationConfigurationRepository = applicationContext.getBean(ApplicationConfigurationRepository.class);
		marutiApplicationAutomater.doProcess(marutiApplicationAutomater, exchange, transactionInfo, driver,applicationConfigurationRepository);
		logger.info("*********** inside Camel Process of marutiApplicationAutomater Processor Ended ************");
	}

	public void doProcess(MarutiApplicationAutomater marutiApplicationAutomater, Exchange exchange,
			TransactionInfo transactionInfo, WebDriver driver, ApplicationConfigurationRepository applicationConfigurationRepository)
			throws Exception {
		logger.info("BEGIN : marutiApplicationAutomater Processor - doProcess Method Called  ");

		doMarutiSuzukiLogin(driver, transactionInfo,exchange,applicationConfigurationRepository);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : marutiApplicationAutomater Processor - doProcess Method Ended  ");
	}

	public boolean doMarutiSuzukiLogin(WebDriver driver, TransactionInfo transactionInfo, Exchange exchange, ApplicationConfigurationRepository applicationConfigurationRepository) throws Exception {
		logger.info("Processor - marutiApplicationAutomater - BEGIN doMarutiSuzukiLogin()  called ");
		applicationContext = SpringContext.getAppContext();
		ProcessService processService = applicationContext.getBean(ProcessService.class);
		CommonService commonService = applicationContext.getBean(CommonService.class);

		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
				RPAConstants.APPID_MARUTI);

		if (applicationDetails == null) {
			logger.error("Error in doMarutiSuzukiLogin :: Application details not configured for process name :: "
					+ transactionInfo.getProcessName());
			return false;
		} else {

			driver.get(applicationDetails.getUrl());
			String userName = applicationDetails.getUsername();
			String password = applicationDetails.getPassword();

			WebElement userNameElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='username']"), 40);
			WebElement passwordElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='password']"), 40);
			WebElement submit1Element = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='btnSubmit_6']"),
					40);
			if(userNameElement!=null){
			userNameElement.sendKeys(userName);
			passwordElement.sendKeys(password);
			submit1Element.submit();
			}else{
				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.carPolicyExtractionNotification(transactionInfo,"Unable to locate element - Please check whether the site is reachable","Maruti");
				transactionInfo.setProcessFailureReason("Unable to locate element");
				throw new Exception("Unable to locate element - Please check whether the site is reachable");
			}
			
			if(driver.getCurrentUrl().endsWith("failed")){
				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.carPolicyExtractionNotification(transactionInfo,"Invalid Credentials - Unable to Login into Maruti Application, Please change credentials if Expired.","Maruti");
				transactionInfo.setProcessFailureReason("Invalid Credentials - Unable to Login into Maruti Application");
				applicationDetails.setIsPasswordExpired(RPAConstants.Y);
				applicationConfigurationRepository.save(applicationDetails);
				throw new Exception("Invalid Credentials - Unable to Login into Maruti Application");
			}else{
				logger.info("marutiApplicationAutomater - Logged Into Maruti Application Successfully::" + driver.getCurrentUrl());
			}
			
			logger.info("marutiApplicationAutomater - current url before wait for url call ::" + driver.getCurrentUrl());

			if(driver.getCurrentUrl().contains("passwordExpiration")){
				FluentWait<WebDriver> fluentwaitObj = UtilityFile.getFluentWaitObject(driver, 180, 200);
				fluentwaitObj.until(ExpectedConditions.visibilityOf(
						driver.findElement(By.xpath("/html/body/a"))));
				fluentwaitObj.until(ExpectedConditions.elementToBeClickable(
						driver.findElement(By.xpath("/html/body/a"))));
				WebElement continueElem = driver
						.findElement(By.xpath("/html/body/a"));
				continueElem.click();
			}
			
			waitForUrl("https://connect.corp.maruti.co.in/dana/home/index.cgi", driver, 120);
			
			// after login to click continue
			FluentWait<WebDriver> fluentwaitObj = UtilityFile.getFluentWaitObject(driver, 180, 200);
			fluentwaitObj.until(ExpectedConditions.visibilityOf(
					driver.findElement(By.xpath("//*[@id='table_webbookmarkline_1'][1]//*[@id='table_webbookmarkline_2']/tbody/tr/td[2]/a"))));
			fluentwaitObj.until(ExpectedConditions.elementToBeClickable(
					driver.findElement(By.xpath("//*[@id='table_webbookmarkline_1'][1]//*[@id='table_webbookmarkline_2']/tbody/tr/td[2]/a"))));
			WebElement marutiInsurance = driver
					.findElement(By.xpath("//*[@id='table_webbookmarkline_1'][1]//*[@id='table_webbookmarkline_2']/tbody/tr/td[2]/a"));
			marutiInsurance.click();

			logger.info("marutiApplicationAutomater - clicked on maruti insurance Element ::" + driver.getCurrentUrl());

			ApplicationConfiguration marutiInsuranceApplicationDetails = commonService
					.getApplicationDetails(businessProcess.getId(), RPAConstants.APPID_MARUTI_INSURANCE);
			
			if (marutiInsuranceApplicationDetails == null) {
				logger.error(
						"Error in doMarutiSuzukiLogin :: Maruti insurance Application details not configured for process name :: "
								+ transactionInfo.getProcessName());
				return false;
			} else {
				String userNameMarutiInsurance = marutiInsuranceApplicationDetails.getUsername();
				String passwordMarutiInsurance = marutiInsuranceApplicationDetails.getPassword();
				ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
				logger.info("tab size ----------------->"+tabs.size());
				if(tabs.size()>1){
					logger.info("before tab switch");
					driver.switchTo().window(tabs.get(1));
					logger.info("after tab switch");
				}
				exchange.setProperty("IS_LOGGED_IN", 1);

				try{
				driver.navigate().to("http://192.168.1.47/MarutiInsurance/login/login.aspx");
				}catch(WebDriverException e){
					driver.close();
					driver.quit();
					logger.error("marutiApplicationAutomater- error while redirecting to same page :: "+e);
					//process(exchange);
					throw e;
				}
				
				logger.info("maruti - current url------------------ "+driver.getCurrentUrl());
					
					logger.info("marutiApplicationAutomater - before userid xpath check");
					WebElement userElementMarutiInsurance = null;
				/* userElementMarutiInsurance = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='txtUser']"), 120);*/
				 
					try {
						WebDriverWait wait = new WebDriverWait(driver, 120);
						userElementMarutiInsurance = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='txtUser']")));

					} catch (Exception e) {
						logger.error("waitForElement txtUser ::" + e.getMessage(), e);
						driver.close();
						 tabs = new ArrayList<String>(driver.getWindowHandles());
							logger.info("current tab size tab size ----------------->"+tabs.size());
							/*if(tabs.size()>1){*/
								logger.info("before tab switch to previous window");
								driver.switchTo().window(tabs.get(0));
								logger.info("after tab switched to previous window ");
							/*}*/
							 marutiInsurance = driver
									.findElement(By.xpath("//*[@id='table_webbookmarkline_1'][1]//*[@id='table_webbookmarkline_2']/tbody/tr/td[2]/a"));
							marutiInsurance.click();
							
							tabs = new ArrayList<String>(driver.getWindowHandles());
							logger.info("tab size ----------------->"+tabs.size());
							if(tabs.size()>1){
								logger.info("before tab switch to maruti insurance");
								driver.switchTo().window(tabs.get(1));
								logger.info("after tab switch");
							}
							try{
							userElementMarutiInsurance = UtilityFile.waitForElementPresent(driver,
									By.xpath("//*[@id='txtUser']"), 80);
							
							} catch (Exception e1) {
								logger.error("waitForElement txtUser ::" + e1.getMessage(), e1);
								driver.close();
								 tabs = new ArrayList<String>(driver.getWindowHandles());
									logger.info("retry - current tab size tab size ----------------->"+tabs.size());
									/*if(tabs.size()>1){*/
										logger.info("retry - before tab switch to previous window");
										driver.switchTo().window(tabs.get(0));
										logger.info("retry - after tab switched to previous window ");
									/*}*/
									 marutiInsurance = driver
											.findElement(By.xpath("//*[@id='table_webbookmarkline_1'][1]//*[@id='table_webbookmarkline_2']/tbody/tr/td[2]/a"));
									marutiInsurance.click();
									
									tabs = new ArrayList<String>(driver.getWindowHandles());
									logger.info("retry - tab size ----------------->"+tabs.size());
									if(tabs.size()>1){
										logger.info("before tab switch to maruti insurance");
										driver.switchTo().window(tabs.get(1));
										logger.info("after tab switch- current url -->"+driver.getCurrentUrl());
									}
									userElementMarutiInsurance = UtilityFile.waitForElementPresent(driver,
											By.xpath("//*[@id='txtUser']"), 80);
							}
					}
				 
				if(userElementMarutiInsurance==null){
					throw new Exception("Unable to read User maruti insurance elemnt");
				}
				WebElement passwordElementMarutiInsurance = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='txtPassword']"), 40);
				WebElement submitMarutiInsurance = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='Button1']"), 40);
				userElementMarutiInsurance.sendKeys(userNameMarutiInsurance);
				passwordElementMarutiInsurance.sendKeys(passwordMarutiInsurance);
				submitMarutiInsurance.click();

				logger.info("marutiApplicationAutomater - after submit click ::"
						+ driver.getCurrentUrl());
				
				if(driver.getCurrentUrl().endsWith("login.aspx")){
					EmailService emailService = (EmailService) applicationContext.getBean("emailService");
					emailService.carPolicyExtractionNotification(transactionInfo,"Invalid Credentials - Unable to Login into Insurance Maruti Application, Please change credentials if Expired.","Maruti");
					transactionInfo.setProcessFailureReason("Invalid Credentials - Unable to Login into Insurance Maruti Application");
					marutiInsuranceApplicationDetails.setIsPasswordExpired(RPAConstants.Y);
					applicationConfigurationRepository.save(marutiInsuranceApplicationDetails);
					throw new Exception("Invalid Credentials - Unable to Login into Maruti Insurance Application");
				}else{
					logger.info("marutiApplicationAutomater - Logged Into Maruti Insurance Application Successfully ::"
							+ driver.getCurrentUrl());
				}
				
				
			}
			logger.info("Processor - marutiApplicationAutomater - END doMarutiSuzukiLogin() ");
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
	
	public void waitForUrl(String url, WebDriver driver, int specifiedTimeout) {
		logger.info("waitForUrl - current url ::" + driver.getCurrentUrl());
		logger.info("waitForUrl - expected url ::" + url);
		   WebDriverWait wait = new WebDriverWait(driver,    specifiedTimeout);
		   ExpectedCondition<Boolean> urlIsCorrect = arg0 -> driver.getCurrentUrl().equals(url);
		   wait.until(urlIsCorrect);
		}
	
	public void terminateProcess(String processName){
		try{
			logger.info("marutiApplicationAutomater - terminateProcess called() :: "+processName);
			Process p4Exit = Runtime.getRuntime().exec("wmic process where (Name like '%"+processName+"%') call terminate");
			p4Exit.waitFor();
			/*Process process = Runtime.getRuntime().exec("wmic process where (Name like '%pulseapplicationlauncher.exe%') call terminate");
			process.waitFor();*/
			logger.info("marutiApplicationAutomater - Exit value-->"+p4Exit.exitValue());
		} catch (Exception e1) {
			logger.error("marutiApplicationAutomater - Error while killing process::"+e1);
			
		}
	}
}
