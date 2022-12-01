/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
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
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.FirstgenPolicy;
import com.rpa.service.CommonService;
import com.rpa.service.processors.FirstGenDownloadPolicyService;
import com.rpa.util.UtilityFile;

public class FirstgenPdfExtracter implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(FirstgenPdfExtracter.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	FirstGenDownloadPolicyService firstGenDownloadPolicyService;

	@Autowired
	private CommonService commonService;

	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("*********** inside Camel Process of firstgenPdfExtracter Called ************");
		logger.info("BEGIN : firstgenPdfExtracter Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		driver = (WebDriver) exchange.getIn().getHeader("chromeDriver_firstgen");
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.EXTRACTION);
		FirstgenPdfExtracter firstgenPdfExtracter = new FirstgenPdfExtracter();
		firstGenDownloadPolicyService = applicationContext.getBean(FirstGenDownloadPolicyService.class);
		commonService = applicationContext.getBean(CommonService.class);
		firstgenPdfExtracter.doProcess(firstgenPdfExtracter, exchange, transactionInfo, driver,
				firstGenDownloadPolicyService, commonService);
		logger.info("*********** inside Camel Process of firstgenPdfExtracter Processor Ended ************");
	}

	public void doProcess(FirstgenPdfExtracter firstgenPdfExtracter, Exchange exchange, TransactionInfo transactionInfo,
			WebDriver driver, FirstGenDownloadPolicyService firstGenDownloadPolicyService, CommonService commonService)
			throws Exception {
		logger.info("BEGIN : firstgenPdfExtracter Processor - doProcess Method Called  ");

		getPolicyNumberListTobeExtractedFromLocalDb(driver, transactionInfo, firstGenDownloadPolicyService, exchange,
				commonService);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : firstgenPdfExtracter Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeExtractedFromLocalDb(WebDriver driver, TransactionInfo transactionInfo,
			FirstGenDownloadPolicyService firstGenDownloadPolicyService, Exchange exchange, CommonService commonService)
			throws Exception {
		logger.info("Processor - firstgenPdfExtracter - BEGIN getPolicyNumberListTobeExtractedFromLocalDb()  called ");

		List<FirstgenPolicy> fordPolicyNoList = null;
		fordPolicyNoList = firstGenDownloadPolicyService.findPdfToBeDownloaded(
				commonService.getThresholdFrequencyLevel(RPAConstants.FIRSTGEN_DOWNLOAD_THRESHOLD));
		logger.info("firstgenPdfExtracter - Unextracted Policies count ::" + fordPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(fordPolicyNoList.size()));

		int extractedPolicyCount = 0, extractedProposalCount = 0;

		String mainWindowHandle = "", policyNo = "", pdfPath = "";

		String phantomjsPath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getCarPolicyProperty(RPAConstants.PHANTOMJS_DRIVER_FOLDER) + "/phantomjs";
		phantomjsPath = removeFirstLetterIfStartsWithSlash(phantomjsPath);

		String rasterizeJsPath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getCarPolicyProperty(RPAConstants.CAR_POLICY_JS_FOLDER) + "/rasterize.js";
		rasterizeJsPath = removeFirstLetterIfStartsWithSlash(rasterizeJsPath);

		String hondaFolderPath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getCarPolicyProperty(RPAConstants.FIRSTGEN_DOWNLOAD_POLICY_FOLDER)
				+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY);
		
		File hondaFolder = new File(hondaFolderPath);

		if (!hondaFolder.exists())
			hondaFolder.mkdirs();
		
		String tempFolderPath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getCarPolicyProperty("firstgen.download.policy.temppdf.location");
		
		File tempFolder = new File(tempFolderPath);

		if (!tempFolder.exists())
			tempFolder.mkdirs();

		mainWindowHandle = driver.getWindowHandle();

		for (FirstgenPolicy fordPolicyObj : fordPolicyNoList) {
			
			driver.switchTo().window(mainWindowHandle);
			
			driver.switchTo().defaultContent();
			WebElement menuFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='Bottom']/frame[1]"), 60);
			driver.switchTo().frame(menuFrameElement);
			logger.info("firstgenApplicationAutomater - after switch to frame, current frame name :::" + getCurrentFrameName(driver));
			WebElement enquiryMenu = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='m0']/div/div[1]/a[2]"), 60);
			//WebElement enquiryMenu = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='m0']/div[1]/div[1]/a[7]"), 60);
			Actions action2 = new Actions(driver);

			action2.moveToElement(enquiryMenu).build().perform();

			enquiryMenu.click();

			policyNo = fordPolicyObj.getPolicyNo();
			if (fordPolicyObj.getIsPolicyDownloaded() == null)
				fordPolicyObj.setIsPolicyDownloaded(RPAConstants.N);
			if (fordPolicyObj.getIsInvoiceDownloaded() == null)
				fordPolicyObj.setIsInvoiceDownloaded(RPAConstants.N);

			

			driver.switchTo().defaultContent();
			logger.info(
					"firstgenApplicationAutomater - detailFrameElement - before switch to frame, current frame name :::"
							+ getCurrentFrameName(driver));

			WebElement detailFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='fs2']"), 60);
			driver.switchTo().frame(detailFrameElement);

			logger.info(
					"firstgenApplicationAutomater - detailFrameElement - after switch to frame, current frame name :::"
							+ getCurrentFrameName(driver));

			logger.info("firstgenApplicationAutomater - switched frame to detail");

			WebElement runNoFieldAbandon = UtilityFile.waitForElementPresent(driver,
					By.xpath("/html/body/form/div/table[2]/tbody/tr[2]/td[2]/input[1]"), 60);
			runNoFieldAbandon.sendKeys(policyNo);

			WebElement policySearchBtn = UtilityFile.waitForElementPresent(driver,
					By.xpath("/html/body/form/div/table[1]/tbody/tr/td/p/input[2]"), 60);
			policySearchBtn.click();

			logger.info("BEGIN : firstgenApplicationAutomater  - Policy search button clicked :: url -- "
					+ driver.getCurrentUrl());

			Actions policyAction = new Actions(driver);
			WebElement policyNumberElement = UtilityFile.waitForElementPresent(driver,
					By.xpath("/html/body/form/div/div/table/tbody/tr[1]"), 60);
			policyAction.moveToElement(policyNumberElement).doubleClick().build().perform();

			/* movement */
			WebElement movementBtn = UtilityFile.waitForElementPresent(driver,
					By.xpath("/html/body/form/table[2]/tbody/tr/td/p/input[2]"), 60);
			movementBtn.click();
			
			logger.info(
					"firstgenApplicationAutomater - movment btn clicked");

			/* documents */
			/**/
			WebElement documentsBtn = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='LinkTd']/center/font[contains(text(), 'Documents')]"), 60);
			documentsBtn.click();
			
			logger.info(
					"firstgenApplicationAutomater - document btn clicked");
			
			WebElement deleteBtn = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='DivTd']/div/table[1]/tbody/tr/td[3]/input[2]"), 60);
			deleteBtn.click();
			
			/*Policy download*/
			
			logger.info(
					"firstgenApplicationAutomater - Policy download part");
			
			/*((JavascriptExecutor) driver)
			.executeScript("document.querySelector('#DivTd > div > table:nth-child(2) > tbody > tr > td:nth-child(2) > input.Det_Picklist').click();");*/
			
			/*WebElement popupArrowBtn = UtilityFile.waitForElementPresent(driver,
					By.xpath("//input[@type='button' and @value='>']"), 60);
			popupArrowBtn.click();*/
			
			new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='button' and @value='>']"))).click();
			
			logger.info("<<<<<<<<<<<<<<<<No. of current tabs before no. of windows check : "
					+ new ArrayList<String>(driver.getWindowHandles()).size());
			
			Thread.sleep(3000);
			//(new WebDriverWait(driver, 30)).until(ExpectedConditions.numberOfWindowsToBe(2));
			
			//switch to second window
			
			for (String handle : driver.getWindowHandles()) {
				if (!handle.equals(mainWindowHandle)) {
					driver.switchTo().window(handle);
				}
			}
			
			WebElement popupSearchDesc = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='DF_TEMPLATE_DESC']"), 60);
			if(policyNo.startsWith("DBS"))
				popupSearchDesc.sendKeys("DC_RNDBSGMC_DS07_LOGO");
			else
				popupSearchDesc.sendKeys("DC_RNDBSGPA_DS06_LOGO");
			
			Thread.sleep(1000);
			
			WebElement searchBtn = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='searchAjaxButton']"), 60);
			searchBtn.click();
			
			logger.info(
					"firstgenApplicationAutomater - search popup with policy logo entered and clicked");
			
			Thread.sleep(1000);
			
			/*WebElement selectSearchedValue = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='headerTable']/tbody/tr[2]"), 60);
			selectSearchedValue.click();*/
			((JavascriptExecutor) driver)
			.executeScript("document.querySelector('#headerTable > tbody > tr.Det_TableRowOdd').click()");
			
			driver.switchTo().window(mainWindowHandle);
			
			driver.switchTo().defaultContent();
			logger.info(
					"firstgenApplicationAutomater - detailFrameElement - before switch to frame, current frame name :::"
							+ getCurrentFrameName(driver));

			WebElement detailFrameElementPolicy = waitForElementVisible(driver, By.xpath("//*[@id='fs2']"), 60);
			driver.switchTo().frame(detailFrameElementPolicy);
			
			
			WebElement addBtn = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='DivTd']/div/table[1]/tbody/tr/td[3]/input[1]"), 60);
			addBtn.click();
			
			logger.info(
					"firstgenApplicationAutomater - add btn clicked");
			
			/*gst invoice*/
			
			logger.info(
					"firstgenApplicationAutomater - Gst invoice part");
			
			/*((JavascriptExecutor) driver)
			.executeScript("document.querySelector('#DivTd > div > table:nth-child(2) > tbody > tr > td:nth-child(2) > input.Det_Picklist').click();");
			
			(new WebDriverWait(driver, 30)).until(ExpectedConditions.numberOfWindowsToBe(2));*/
			
			new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='button' and @value='>']"))).click();
			
			logger.info("<<<<<<<<<<<<<<<<No. of current tabs before no. of windows check before gst invoice : "
					+ new ArrayList<String>(driver.getWindowHandles()).size());
			
			Thread.sleep(3000);
			
			for (String handle : driver.getWindowHandles()) {
				if (!handle.equals(mainWindowHandle)) {
					driver.switchTo().window(handle);
				}
			}
			
			WebElement popupSearchDescGstInvoice = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='DF_TEMPLATE_DESC']"), 60);
			popupSearchDescGstInvoice.sendKeys("DC_GSTINVOICE_HEALTH_LOGO");
			
			Thread.sleep(1000);
			
			WebElement searchBtnInvoice = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='searchAjaxButton']"), 60);
			searchBtnInvoice.click();
			
			logger.info(
					"firstgenApplicationAutomater - search popup with invoice logo entered and clicked");
			
			Thread.sleep(1000);
			
			/*WebElement selectSearchedGstInvoice = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='headerTable']/tbody/tr[2]"), 60);
			selectSearchedGstInvoice.click();*/
			((JavascriptExecutor) driver)
			.executeScript("document.querySelector('#headerTable > tbody > tr.Det_TableRowOdd').click()");
			
			driver.switchTo().window(mainWindowHandle);
			
			driver.switchTo().defaultContent();
			logger.info(
					"firstgenApplicationAutomater - detailFrameElement - before switch to frame, current frame name :::"
							+ getCurrentFrameName(driver));

			WebElement detailFrameElementInvoice = waitForElementVisible(driver, By.xpath("//*[@id='fs2']"), 60);
			driver.switchTo().frame(detailFrameElementInvoice);
			
			WebElement addBtnGstInvoice = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='DivTd']/div/table[1]/tbody/tr/td[3]/input[1]"), 60);
			addBtnGstInvoice.click();
			
			logger.info(
					"firstgenApplicationAutomater - add button clicked for adding gst with logo");
			
			FluentWait<WebDriver> fluentWait = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
					.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);

			FluentWait<WebDriver> fluentWaitForDispatchType = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
					.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
			fluentWaitForDispatchType.until(ExpectedConditions.presenceOfElementLocated(
					By.xpath("//*[@id='DocumentID']/tbody/tr[2]//*[@id='optDocDispatchType']")));
			fluentWait.until(ExpectedConditions.presenceOfElementLocated((By.xpath(
					"//*[@id='DocumentID']/tbody/tr[2]//*[@id='optDocDispatchType']/option[@value='PREVIEW']"))));
			Select dispatchSelect = new Select(
					driver.findElement(By.xpath("//*[@id='DocumentID']/tbody/tr[2]//*[@id='optDocDispatchType']")));
			dispatchSelect.selectByValue("PREVIEW");

			FluentWait<WebDriver> fluentWaitForRequestType = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
					.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
			fluentWaitForRequestType.until(ExpectedConditions.presenceOfElementLocated(
					By.xpath("//*[@id='DocumentID']/tbody/tr[2]//*[@id='optDocRequestType']")));
			fluentWait.until(ExpectedConditions.presenceOfElementLocated((By
					.xpath("//*[@id='DocumentID']/tbody/tr[2]//*[@id='optDocRequestType']/option[@value='ONLINE']"))));
			Select requestSelect = new Select(
					driver.findElement(By.xpath("//*[@id='DocumentID']/tbody/tr[2]//*[@id='optDocRequestType']")));
			requestSelect.selectByValue("ONLINE");
			
			logger.info(
					"firstgenApplicationAutomater - priview online changed for policy");

			/* Gst Invoice */
			FluentWait<WebDriver> fluentWaitForDispatchTypeGst = new FluentWait<>(driver)
					.withTimeout(30, TimeUnit.SECONDS).pollingEvery(200, TimeUnit.MILLISECONDS)
					.ignoring(NoSuchElementException.class);
			fluentWaitForDispatchTypeGst.until(ExpectedConditions.presenceOfElementLocated(
					By.xpath("//*[@id='DocumentID']/tbody/tr[3]//*[@id='optDocDispatchType']")));
			fluentWait.until(ExpectedConditions.presenceOfElementLocated((By.xpath(
					"//*[@id='DocumentID']/tbody/tr[3]//*[@id='optDocDispatchType']/option[@value='PREVIEW']"))));
			Select dispatchSelectGst = new Select(
					driver.findElement(By.xpath("//*[@id='DocumentID']/tbody/tr[3]//*[@id='optDocDispatchType']")));
			dispatchSelectGst.selectByValue("PREVIEW");

			FluentWait<WebDriver> fluentWaitForRequestTypeGst = new FluentWait<>(driver)
					.withTimeout(30, TimeUnit.SECONDS).pollingEvery(200, TimeUnit.MILLISECONDS)
					.ignoring(NoSuchElementException.class);
			fluentWaitForRequestTypeGst.until(ExpectedConditions.presenceOfElementLocated(
					By.xpath("//*[@id='DocumentID']/tbody/tr[3]//*[@id='optDocRequestType']")));
			fluentWait.until(ExpectedConditions.presenceOfElementLocated((By
					.xpath("//*[@id='DocumentID']/tbody/tr[3]//*[@id='optDocRequestType']/option[@value='ONLINE']"))));
			Select requestSelectGst = new Select(
					driver.findElement(By.xpath("//*[@id='DocumentID']/tbody/tr[3]//*[@id='optDocRequestType']")));
			requestSelectGst.selectByValue("ONLINE");
			
			logger.info(
					"firstgenApplicationAutomater - priview online changed for gst invoice");

			pdfPath = removeFirstLetterIfStartsWithSlash(pdfPath);

			String oddDocName = "", evenDocName = "";

			WebElement evenRowCheckBox = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='DocumentID']/tbody/tr[@class='Det_TableRowEven']//*[@id='optSelectDocID']"), 60);
			WebElement oddCheckBox = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='DocumentID']/tbody/tr[@class='Det_TableRowOdd']//*[@id='optSelectDocID']"), 60);

			/* CHECK even */
			/*evenRowCheckBox.click();*/
			oddCheckBox.click();

			/* get value of odd row text box */
			WebElement oddRowDoc = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='DocumentID']/tbody/tr[@class='Det_TableRowOdd']//*[@id='txtDocId']"), 60);
			oddDocName = oddRowDoc.getAttribute("value");
			
			logger.info(
					"firstgenApplicationAutomater - DocumentID --"+oddDocName);
			
			if (oddDocName.contains("GSTInvoice")) {
				/* policy Extraction */
				if (fordPolicyObj.getIsInvoiceDownloaded().equalsIgnoreCase(RPAConstants.N)) {
					pdfPath = hondaFolder + "\\GstInvoice_" + policyNo.replaceAll("(\\r\\n|\\n|\\r)", "") + ".pdf";
					/* To Download Policy PDF */
					if (saveInLocalPath(driver, pdfPath)) {
						extractedProposalCount++;
						logger.info("firstgenPdfExtracter -gst invoice PDF Downloaded for policyNo :: " + policyNo);

						fordPolicyObj.setIsInvoiceDownloaded(RPAConstants.Y);
						fordPolicyObj.setInvoicePdfPath(pdfPath);
						fordPolicyObj.setIsInvoiceUploaded(RPAConstants.N);
						
						(new WebDriverWait(driver, 30)).until(ExpectedConditions.numberOfWindowsToBe(2));

						for (String handle : driver.getWindowHandles()) {
							if (!handle.equals(mainWindowHandle)) {
								driver.switchTo().window(handle);
								driver.close();
							}
						}

						driver.switchTo().window(mainWindowHandle);

						driver.switchTo().defaultContent();
						logger.info(
								"firstgenApplicationAutomater after pdf1 download - detailFrameElement - before switch to frame, current frame name :::"
										+ getCurrentFrameName(driver));

						detailFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='fs2']"), 60);
						driver.switchTo().frame(detailFrameElement);

						logger.info(
								"firstgenApplicationAutomater after pdf1 download - detailFrameElement - after switch to frame, current frame name :::"
										+ getCurrentFrameName(driver));
					}
				} else {
					logger.info(
							"firstgenPdfExtracter - GstInvoice pdf already extracted for policy code :: " + policyNo);
				}
			} else {
				if (fordPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
					pdfPath = hondaFolder + "\\Policy_" + policyNo.replaceAll("(\\r\\n|\\n|\\r)", "") + ".pdf";
					if (saveInLocalPath(driver, pdfPath)) {
						extractedPolicyCount++;
						logger.info("firstgenPdfExtracter -policy PDF Downloaded for policyNo :: " + policyNo);

						fordPolicyObj.setIsPolicyDownloaded(RPAConstants.Y);
						fordPolicyObj.setPolicyPdfPath(pdfPath);
						fordPolicyObj.setIsPolicyUploaded(RPAConstants.N);
						
						ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
						logger.info("firstgenPdfExtracter -policy PDF Downloaded current window count ::"
								+  tabs.size());
						(new WebDriverWait(driver, 30)).until(ExpectedConditions.numberOfWindowsToBe(2));

						for (String handle : driver.getWindowHandles()) {
							if (!handle.equals(mainWindowHandle)) {
								driver.switchTo().window(handle);
								driver.close();
							}
						}

						driver.switchTo().window(mainWindowHandle);

						driver.switchTo().defaultContent();
						logger.info(
								"firstgenApplicationAutomater after pdf1 download - detailFrameElement - before switch to frame, current frame name :::"
										+ getCurrentFrameName(driver));

						detailFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='fs2']"), 60);
						driver.switchTo().frame(detailFrameElement);

						logger.info(
								"firstgenApplicationAutomater after pdf1 download - detailFrameElement - after switch to frame, current frame name :::"
										+ getCurrentFrameName(driver));
					}
				} else {
					logger.info("firstgenPdfExtracter - Policy pdf already extracted for policy code :: " + policyNo);
				}
			}


			// getCurrentFrameName(driver);

			/* uncheck odd */
			oddCheckBox = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='DocumentID']/tbody/tr[@class='Det_TableRowOdd']//*[@id='optSelectDocID']"), 60);
			oddCheckBox.click();

			/* check even */
			evenRowCheckBox = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='DocumentID']/tbody/tr[@class='Det_TableRowEven']//*[@id='optSelectDocID']"), 60);
			evenRowCheckBox.click();

			/* get value of even row text box */
			WebElement evenRowDoc = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='DocumentID']/tbody/tr[@class='Det_TableRowEven']//*[@id='txtDocId']"), 60);
			evenDocName = evenRowDoc.getAttribute("value");

			logger.info(
					"firstgenApplicationAutomater - even DocumentID --"+evenDocName);
			
			if (evenDocName.contains("GSTInvoice")) {
				/* policy Extraction */
				if (fordPolicyObj.getIsInvoiceDownloaded().equalsIgnoreCase(RPAConstants.N)) {
					pdfPath = hondaFolder + "\\GstInvoice_" + policyNo.replaceAll("(\\r\\n|\\n|\\r)", "") + ".pdf";
					if (saveInLocalPath(driver, pdfPath)) {
						extractedProposalCount++;
						logger.info("firstgenPdfExtracter -policy PDF Downloaded for policyNo :: " + policyNo);

						fordPolicyObj.setIsInvoiceDownloaded(RPAConstants.Y);
						fordPolicyObj.setInvoicePdfPath(pdfPath);
						fordPolicyObj.setIsInvoiceUploaded(RPAConstants.N);
						
						(new WebDriverWait(driver, 30)).until(ExpectedConditions.numberOfWindowsToBe(2));

						for (String handle : driver.getWindowHandles()) {
							if (!handle.equals(mainWindowHandle)) {
								driver.switchTo().window(handle);
								driver.close();
							}
						}
					}
				} else {
					logger.info(
							"firstgenPdfExtracter - GstInvoice pdf already extracted for policy code :: " + policyNo);
				}
			} else {
				if (fordPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
					pdfPath = hondaFolder + "\\Policy_" + policyNo.replaceAll("(\\r\\n|\\n|\\r)", "") + ".pdf";
					if (saveInLocalPath(driver, pdfPath)) {
						extractedPolicyCount++;
						logger.info("firstgenPdfExtracter -policy PDF Downloaded for policyNo :: " + policyNo);

						fordPolicyObj.setIsPolicyDownloaded(RPAConstants.Y);
						fordPolicyObj.setPolicyPdfPath(pdfPath);
						fordPolicyObj.setIsPolicyUploaded(RPAConstants.N);
						
						(new WebDriverWait(driver, 30)).until(ExpectedConditions.numberOfWindowsToBe(2));

						for (String handle : driver.getWindowHandles()) {
							if (!handle.equals(mainWindowHandle)) {
								driver.switchTo().window(handle);
								driver.close();
							}
						}
					}
				} else {
					logger.info("firstgenPdfExtracter - Policy pdf already extracted for policy code :: " + policyNo);
				}

				
			}

			firstGenDownloadPolicyService.save(fordPolicyObj);

		}
		/* Total policy pdf extratced */
		transactionInfo.setTotalSuccessRecords(String.valueOf(extractedPolicyCount));
		logger.info("firstgenPdfExtracter - Extracted policy count :: " + extractedPolicyCount);

		/* Total proposal pdf extratced */
		transactionInfo.setTotalSuccessUploads((String.valueOf(extractedProposalCount)));
		logger.info("firstgenPdfExtracter - Extracted Proposal count :: " + extractedProposalCount);

		// At the end, come back to the main window
		driver.switchTo().window(mainWindowHandle);

		driver.switchTo().defaultContent();
		logger.info("firstGenLogout() topFrameElement - before switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		WebElement topFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='TopFrame']"), 60);
		driver.switchTo().frame(topFrameElement);

		logger.info("firstGenLogout() topFrameElement - after switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		WebElement logoutElement = waitForElementVisible(driver,
				By.xpath("//form[@name='Frm1']/table/tbody/tr/td[4]/a[2]"), 60);
		logoutElement = driver.findElement(By.xpath("//form[@name='Frm1']/table/tbody/tr/td[4]/a[2]"));
		logoutElement.click();
		
		logger.info("firstgenPdfExtracter - END getPolicyNumberListTobeExtractedFromLocalDb()");

		return true;

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

	public boolean saveInLocalPath(WebDriver driver, String pdfPath)
			throws URISyntaxException, InterruptedException, IOException {

		boolean exist = false, isDeleted = false;

		isDeleted = UtilityFile.isFileDeleted(UtilityFile.getCodeBasePath()
				+ UtilityFile.getCarPolicyProperty("firstgen.download.policy.temppdf.location"), ".action");
		if (isDeleted) {
			logger.info(" firstgendownloadPdfExtracter : saveInLocalPath - cleared temp_pdf folder for new Download");
		} else {
			logger.info(" firstgendownloadPdfExtracter : saveInLocalPath - no file available in temp folder to delete");
		}

		int i = 0;
		do {
			
			/* Execute */
			WebElement btnExecute = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@name='btnExecute']"), 60);
			btnExecute.click();

			logger.info(
					"firstgendownloadPdfExtracter : saveInLocalPath - execute btn clicked");
			
			/* prevew */
			WebElement btnPreview = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@name='btnPreview']"), 60);
			btnPreview.click();
			
			logger.info(
					"firstgendownloadPdfExtracter : saveInLocalPath - preview btn clicked");
			
			Thread.sleep(8000);

			logger.info(
					"firstgendownloadPdfExtracter : saveInLocalPath - isFileDownload_ExistAndRename method called here -- download try number :: "
							+ (i + 1));
			exist = UtilityFile.isFileDownload_ExistAndRenameWithMultiExtensionCheck(
					UtilityFile.getCodeBasePath()
					+ UtilityFile.getCarPolicyProperty("firstgen.download.policy.temppdf.location"),
					".action",".pdf", true, pdfPath);

			i++;
			if (i == 3) {
				logger.info(
						"firstgendownloadPdfExtracter : saveInLocalPath - file not download after 24 secs of wait ");
				return false;
			}

		} while (exist != true);
		return true;

	}

	public String getCurrentFrameName(WebDriver driver) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		String CurrentFrame = js.executeScript("return self.name").toString();
		return CurrentFrame;
	}

	public WebElement waitForElementVisible(WebDriver driver, final By selector, int timeOutInSeconds) {
		WebElement element = null;
		WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));

		return element;
	}
}
