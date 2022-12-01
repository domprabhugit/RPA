/*
 * Robotic Process Automation
 * @originalAuthor Vijayananth.M
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.rpa.camel.config.SpringContext;
import com.rpa.camel.processors.common.ChromeDriverHeadless;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.GLBatchProcessor;
import com.rpa.repository.TransactionInfoRepository;
import com.rpa.service.processors.FirstGenProcessorService;
import com.rpa.util.UtilityFile;

public class FirstGenReprocessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(FirstGenReprocessor.class.getName());

	@Autowired
	FirstGenProcessorService batchProcessorService;

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	ChromeDriverHeadless chromeDriverHeadless;

	@Autowired
	WebDriver driver;

	@Autowired
	private TransactionInfoRepository transactionInfoRepository;
	
	@Autowired
	private Environment environment;

	private void AutoWiringBeanPropertiesSetMethod() {
		logger.info("AutoWiringBeanPropertiesSetMethod is Called in FirstGenReprocessor Class");
		applicationContext = SpringContext.getAppContext();
		batchProcessorService = applicationContext.getBean(FirstGenProcessorService.class);
		chromeDriverHeadless = applicationContext.getBean(ChromeDriverHeadless.class);
		environment = applicationContext.getBean(Environment.class);
		transactionInfoRepository = applicationContext.getBean(TransactionInfoRepository.class);
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in FirstGenReprocessor Class");
	}

	@SuppressWarnings("static-access")
	@Override
	public void process(Exchange exchange) throws IOException, URISyntaxException, InterruptedException, ParseException {
		logger.info("*********** inside Camel Process of FirstGenReProcessor Called ************");
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty("TRANSACTION_INFO_REQ");
		transactionInfo.setProcessPhase(RPAConstants.REPROCESSOR);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		driver = chromeDriverHeadless.getNewChromeDriver();
		exchange.getIn().setHeader("chromeDriver", driver);
		FirstGenReprocessor batchReprocessor = new FirstGenReprocessor();
		batchReprocessor.doProcess(batchReprocessor, driver, transactionInfo);
		exchange.setProperty("TRANSACTION_INFO_REQ", transactionInfo);
		logger.info("*********** inside Camel Process of FirstGenReprocessor Ended ************");
	}

	public void doProcess(FirstGenReprocessor batchReprocessor, WebDriver driver, TransactionInfo transactionInfo)
			throws IOException, URISyntaxException, InterruptedException, ParseException {
		logger.info("BEGIN : Processor - FirstGen GL POST - Batch Reprocess for ALM resolved issue ");

		AutoWiringBeanPropertiesSetMethod();
		FirstGenProcessor trackIssue = new FirstGenProcessor();
		if (trackIssue.doAlmLogin(driver, transactionInfo)) {
			if (trackIssue.accessSupportDeskDashBaord(driver)) {
				/*WebElement searchElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='level_2']/ul/li[4]/a"), 60);
				searchElement = driver.findElement(By.xpath("//*[@id='level_2']/ul/li[4]/a"));*/
				
				WebElement searchElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='main-container']/aside/div[1]/div[1]/nav/a[3]"), 60);
				searchElement = driver.findElement(By.xpath("//*[@id='main-container']/aside/div[1]/div[1]/nav/a[3]"));
				
				searchElement.click();
				/* filtering Requested By */
				FluentWait<WebDriver> fluentWaitForRequestedBySelect = new FluentWait<>(driver).withTimeout(60, TimeUnit.SECONDS)
						.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
				/*fluentWaitForRequestedBySelect
						.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_report_criteria_adv_9435']")));
				Select requestedBySelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_report_criteria_adv_9435']")));
				requestedBySelect.selectByValue("3862");*/
				fluentWaitForRequestedBySelect
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_report_criteria_9435']")));
				Select requestedBySelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_report_criteria_9435']")));
				requestedBySelect.selectByValue("3862");
				
				/* filtering by status */
				FluentWait<WebDriver> fluentWaitForStatusSelect = new FluentWait<>(driver).withTimeout(60, TimeUnit.SECONDS)
						.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
				fluentWaitForStatusSelect.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_report_criteria_adv_9459']")));
				Select statusSelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_report_criteria_adv_9459']")));
				statusSelect.deselectAll();
				statusSelect.selectByValue("12490");
				
				
				/* filtering by transferTeam */
				FluentWait<WebDriver> fluentWaitforTransferTeam = new FluentWait<>(driver).withTimeout(60, TimeUnit.SECONDS)
						.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
				fluentWaitforTransferTeam.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_report_criteria_adv_9468']")));
				Select transferTeamSelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_report_criteria_adv_9468']")));
				transferTeamSelect.deselectAll();
				transferTeamSelect.selectByValue("");
				
				if (Arrays.stream(environment.getActiveProfiles())
						.anyMatch(env -> (env.equalsIgnoreCase("uat") || env.equalsIgnoreCase("dev")))) {
					/* filtering by category */
					FluentWait<WebDriver> fluentWaitforCategory = new FluentWait<>(driver).withTimeout(60, TimeUnit.SECONDS)
							.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
					fluentWaitforCategory.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_report_criteria_adv_9437']")));
					Select catgorySelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_report_criteria_adv_9437']")));
					catgorySelect.deselectAll();
					catgorySelect.selectByValue("11803");
				}else{
					/* filtering by category */
					FluentWait<WebDriver> fluentWaitforCategory = new FluentWait<>(driver).withTimeout(60, TimeUnit.SECONDS)
							.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
					fluentWaitforCategory.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_report_criteria_adv_9437']")));
					Select catgorySelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_report_criteria_adv_9437']")));
					catgorySelect.deselectAll();
					catgorySelect.selectByValue("13505");
				}
				
				
				/*WebElement submitElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='tracker_report_query_form']/div/button"), 60);
				submitElement = driver.findElement(By.xpath("//*[@id='tracker_report_query_form']/div/button"));*/
				WebElement submitElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@name='tracker_query_submit']"), 60);
				submitElement = driver.findElement(By.xpath("//*[@name='tracker_query_submit']"));
				submitElement.click();
				batchReprocessor.getResolvedList(driver, trackIssue, batchReprocessor, transactionInfo);
			}
		}
	}

	private void getResolvedList(WebDriver driver, FirstGenProcessor batchProcessor, FirstGenReprocessor batchReprocessor,
			TransactionInfo transactionInfo2) throws IOException, URISyntaxException, InterruptedException, ParseException {
		logger.info("FirstGenReprocessor  getResolvedList to reprocess Method  called");
		ArrayList<String> al = new ArrayList<String>();

		List<WebElement> elements = driver
				.findElements(By.xpath("//*[@id='tracker_report_table'] /tbody/tr/td[contains(text(), 'Resolved')]/ancestor::tr/td[2]"));
		for (WebElement element : elements) {
			// System.out.println("w-->" + element);
			String ticketId = element.getText();
			al.add(ticketId);
		}
		logger.info("getResolvedList list size -->"+al.size());
		batchProcessor.doAlmLogout(driver, transactionInfo2);
		if (al.size() != 0) {
			List<GLBatchProcessor> glBatchProcessors = batchProcessorService.findByTicketId(al);
			if (glBatchProcessors != null && !glBatchProcessors.isEmpty()) {
				for (GLBatchProcessor glBatch : glBatchProcessors) {
					if (!glBatch.getProcessedFlag().equalsIgnoreCase("Y")) {
						logger.info("getResolvedList run number to be processed -->"+glBatch.getRunNo());
						List<TransactionInfo> transactionInfo = transactionInfoRepository.findByRunNo(glBatch.getRunNo());
						batchProcessor.doProcess(batchProcessor, driver, glBatch, batchReprocessor, transactionInfo.get(0));
						logger.info("getResolvedList - Run Number after batchProcessor.doProcess -->"+transactionInfo.get(0).getRunNo());
					}
				}
			}
		}
		logger.info("FirstGenReprocessor  getResolvedList to reprocess Method  end");
	}

}
