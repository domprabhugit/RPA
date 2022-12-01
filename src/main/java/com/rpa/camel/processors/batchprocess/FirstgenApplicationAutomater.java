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
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
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
import com.rpa.service.CommonService;
import com.rpa.service.process.ProcessService;
import com.rpa.util.UtilityFile;

public class FirstgenApplicationAutomater implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(FirstgenApplicationAutomater.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	ChromeDriverHeadless chromeDriverHeadless;

	@Autowired
	WebDriver driver;

	@SuppressWarnings("static-access")
	@Override
	public void process(Exchange exchange)
			throws Exception {
		logger.info("*********** inside Camel Process of firstgenApplicationAutomater Called ************");
		logger.info("BEGIN : firstgenApplicationAutomater Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		chromeDriverHeadless = applicationContext.getBean(ChromeDriverHeadless.class);
		driver = chromeDriverHeadless.getNewChromeDriverWithCustomDownloadPath(UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("firstgen.download.policy.temppdf.location"));
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.AUTOMATER);
		FirstgenApplicationAutomater firstgenApplicationAutomater = new FirstgenApplicationAutomater();
		exchange.getIn().setHeader("chromeDriver_firstgen", driver);
		firstgenApplicationAutomater.doProcess(firstgenApplicationAutomater, exchange, transactionInfo, driver);
		logger.info("*********** inside Camel Process of firstgenApplicationAutomater Processor Ended ************");
	}

	public void doProcess(FirstgenApplicationAutomater firstgenApplicationAutomater, Exchange exchange,
			TransactionInfo transactionInfo, WebDriver driver)
			throws Exception {
		logger.info("BEGIN : firstgenApplicationAutomater Processor - doProcess Method Called  ");

		doFirstgenAssureLogin(driver, transactionInfo);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : firstgenApplicationAutomater Processor - doProcess Method Ended  ");
	}

	public boolean doFirstgenAssureLogin(WebDriver driver, TransactionInfo transactionInfo) throws Exception {
		logger.info("Processor - doFirstgenAssureLogin()  called ");
		applicationContext = SpringContext.getAppContext();
		ProcessService processService = applicationContext.getBean(ProcessService.class);
		CommonService commonService = applicationContext.getBean(CommonService.class);

		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
				RPAConstants.APPID_FIRSTGRN);

		if (applicationDetails == null) {
			logger.error("Error in doFirstgenAssureLogin :: Application details not configured for process name :: "
					+ transactionInfo.getProcessName());
			return false;
		} else {

			driver.get(applicationDetails.getUrl());
			logger.info("firstgenApplicationAutomater  - doFirstgenAssureLogin curent url after alm get url :: " + driver.getCurrentUrl());
			String userName = applicationDetails.getUsername();
			String password = applicationDetails.getPassword();
			String FirstGenUrl = applicationDetails.getUrl();
			if (driver.getCurrentUrl().equals(FirstGenUrl)) {
				logger.info("FirstGenProcessor  - GL POST FirstGenUrl :: " + driver.getCurrentUrl());
				WebElement user = UtilityFile.waitForElementPresent(driver, By.id("userName"), 120);
				user.sendKeys(userName);
				WebElement pass = UtilityFile.waitForElementPresent(driver, By.id("password"), 120);
				pass.sendKeys(password);
				
				((JavascriptExecutor) driver)
				.executeScript("document.querySelector('body > div > center > table > tbody > tr > td > div > table > tbody > tr:nth-child(2) > td > div > center > form > table > tbody > tr:nth-child(3) > td:nth-child(2) > input').click();");
				
				logger.info("FirstGen application logged in successfully");
			}
			logger.info("firstgenApplicationAutomater - Logged Into Application Successfully::" + driver.getCurrentUrl());

			
			logger.info("firstgenApplicationAutomater - FirstGen MenuSelection Method Started ");

			logger.info("firstgenApplicationAutomater - current Frame before menu selection :::" + getCurrentFrameName(driver));
			driver.switchTo().defaultContent();
			WebElement menuFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='Bottom']/frame[1]"), 60);
			driver.switchTo().frame(menuFrameElement);
			logger.info("firstgenApplicationAutomater - after switch to frame, current frame name :::" + getCurrentFrameName(driver));

			WebElement accounts = null;

			accounts = waitForElementVisible(driver, By.xpath("//*[@id='m0']/a"), 60);

			Actions action = new Actions(driver);

			action.moveToElement(accounts).build().perform();
			accounts.click();
			
			WebElement underwritingMenu = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='m0']/div/a[1]"), 60);
			Actions action1 = new Actions(driver);

			action1.moveToElement(underwritingMenu).build().perform();

			underwritingMenu.click();
			
			Thread.sleep(1000);
		    
			logger.info("Processor - firstgenApplicationAutomater - END doFirstgenAssureLogin() ");
			return true;

		}

	}

	public WebElement waitForElementVisible(WebDriver driver, final By selector, int timeOutInSeconds) {
		WebElement element = null;
		WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));

		return element;
	}
	
	public String getCurrentFrameName(WebDriver driver) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		String CurrentFrame = js.executeScript("return self.name").toString();
		return CurrentFrame;
	}
	

}
