/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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

import com.rpa.camel.config.SpringContext;
import com.rpa.camel.processors.common.ChromeDriverHeadless;
import com.rpa.constants.RPAConstants;
import com.rpa.model.ApplicationConfiguration;
import com.rpa.model.BusinessProcess;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.ClaimsDownload;
import com.rpa.service.CommonService;
import com.rpa.service.process.ProcessService;
import com.rpa.service.processors.OmniDocsClaimsDocDownloaderService;
import com.rpa.util.UtilityFile;

public class OmniDocsClaimsDocDownloader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(OmniDocsClaimsDocDownloader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	ChromeDriverHeadless chromeDriverHeadless;

	@Autowired
	OmniDocsClaimsDocDownloaderService omniDocsClimsDocDownloaderService;

	@Autowired
	WebDriver driver;

	@SuppressWarnings("static-access")
	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("*********** inside Camel Process of OmniDocsClaimsDocDownloader Called ************");
		logger.info("BEGIN : OmniDocsClaimsDocDownloader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		chromeDriverHeadless = applicationContext.getBean(ChromeDriverHeadless.class);
		omniDocsClimsDocDownloaderService = applicationContext.getBean(OmniDocsClaimsDocDownloaderService.class);
		driver = chromeDriverHeadless.getNewChromeDriverWithCustomDownloadPath(
				UtilityFile.getCodeBasePath() + UtilityFile.getOmniDocProperty("Omnidoc.File.DownLoad.Location"));
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.DOWNLOAD);
		OmniDocsClaimsDocDownloader OmniDocsClaimsDocDownloader = new OmniDocsClaimsDocDownloader();
		exchange.getIn().setHeader("chromeDriver_claimsdownload", driver);
		OmniDocsClaimsDocDownloader.doProcess(OmniDocsClaimsDocDownloader, exchange, transactionInfo, driver,
				omniDocsClimsDocDownloaderService);
		logger.info("*********** inside Camel Process of OmniDocsClaimsDocDownloader Processor Ended ************");
	}

	public void doProcess(OmniDocsClaimsDocDownloader OmniDocsClaimsDocDownloader, Exchange exchange,
			TransactionInfo transactionInfo, WebDriver driver,
			OmniDocsClaimsDocDownloaderService omniDocsClimsDocDownloaderService) throws Exception {
		logger.info("BEGIN : OmniDocsClaimsDocDownloader Processor - doProcess Method Called  ");

		doOmniDoscLogin(driver, transactionInfo, omniDocsClimsDocDownloaderService);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : OmniDocsClaimsDocDownloader Processor - doProcess Method Ended  ");
	}

	public boolean doOmniDoscLogin(WebDriver driver, TransactionInfo transactionInfo,
			OmniDocsClaimsDocDownloaderService omniDocsClimsDocDownloaderService) throws Exception {
		logger.info("Processor - OmniDocsClaimsDocDownloader - BEGIN doMarutiSuzukiLogin()  called ");
		applicationContext = SpringContext.getAppContext();
		ProcessService processService = applicationContext.getBean(ProcessService.class);
		CommonService commonService = applicationContext.getBean(CommonService.class);

		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
				Long.valueOf(10));

		if (applicationDetails == null) {
			logger.error("Error in doOmniDoscLogin :: Application details not configured for process name :: "
					+ transactionInfo.getProcessName());
			throw new Exception("Applicationn details not configured");
		} else {

			driver.get(applicationDetails.getUrl());
			String userName = applicationDetails.getUsername();
			String password = applicationDetails.getPassword();

			WebElement userNameElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='userName']"), 40);
			WebElement passwordElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='password']"), 40);
			WebElement submit1Element = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='login'][1]//*[@id='login']"), 40);
			if (userNameElement != null) {
				userNameElement.sendKeys(userName);
				passwordElement.sendKeys(password);
				submit1Element.click();
			} else {
				/*
				 * EmailService emailService = (EmailService)
				 * applicationContext.getBean("emailService");
				 * emailService.carPolicyExtractionNotification(transactionInfo,
				 * "Unable to locate element - Please check whether the site is reachable"
				 * );
				 */
				transactionInfo.setProcessFailureReason("Unable to locate element");
				throw new Exception("Unable to locate element - Please check whether the site is reachable");
			}

			logger.info("Processor - OmniDocsClaimsDocDownloader - current url after login submit :: "+driver.getCurrentUrl());

			if (driver.getCurrentUrl().contains("timeoutWeb.jsp")) {
				WebElement clickHereElement = UtilityFile.waitForElementPresent(driver, By.xpath("/html/body/p[4]/a"),
						40);
				clickHereElement.click();
				userNameElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='userName']"), 40);
				passwordElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='password']"), 40);
				submit1Element = UtilityFile.waitForElementPresent(driver,
						By.xpath("//*[@id='login'][1]//*[@id='login']"), 40);

				if (userNameElement != null) {
					userNameElement.sendKeys(userName);
					passwordElement.sendKeys(password);
					submit1Element.click();

				}
			}
			logger.info("OmniDocsClaimsDocDownloader - current url before master desktop click try :: "
					+ driver.getCurrentUrl());
			// after login to click Master desktop
			WebElement masterDesktopElement = UtilityFile.waitForElementPresent(driver,
					By.xpath("//*[@id='TopHeaderDiv']/table/tbody/tr[1]/td[3]/a"), 60);
			FluentWait<WebDriver> fluentwait = UtilityFile.getFluentWaitObject(driver, 30, 200);
			fluentwait.until(ExpectedConditions.visibilityOf(masterDesktopElement));
			fluentwait.until(ExpectedConditions.elementToBeClickable(masterDesktopElement));
			masterDesktopElement.click();

			logger.info("OmniDocsClaimsDocDownloader - clicked on masterDesktop Element ::" + driver.getCurrentUrl());

			// Perform the click operation that opens new window
			// To click Search Element
			driver.switchTo().defaultContent();
			driver.switchTo().frame(UtilityFile.waitForElementPresent(driver, By.xpath("/html/frameset/frame"), 60));

			WebElement searcElement = UtilityFile.waitForElementPresent(driver,
					By.xpath("/html/body/div[1]/table/tbody/tr/td[2]/div/center/table/tbody/tr/td[8]/div/center/p/a"),
					60);
			FluentWait<WebDriver> fluentwaitSearch = UtilityFile.getFluentWaitObject(driver, 30, 200);
			fluentwaitSearch.until(ExpectedConditions.visibilityOf(searcElement));
			fluentwaitSearch.until(ExpectedConditions.elementToBeClickable(searcElement));
			searcElement.click();
			
			logger.info("OmniDocsClaimsDocDownloader - url after searchelemnt click ::" + driver.getCurrentUrl());

			// Switch to new window opened
			for (String winHandle : driver.getWindowHandles()) {
				driver.switchTo().window(winHandle);
			}

			logger.info("current url before frame switch for dataclass click :: " + driver.getCurrentUrl());
			// To click dataclass Element
			driver.switchTo().defaultContent();
			driver.switchTo().frame(1).switchTo().frame(0);
			
			logger.info("frame name after frmSearchToolbar switch :::" + getCurrentFrameName(driver));
			WebElement dataClassElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='a3']"), 60);
			FluentWait<WebDriver> fluentwaitDataClass = UtilityFile.getFluentWaitObject(driver, 30, 200);
			fluentwaitDataClass.until(ExpectedConditions.visibilityOf(dataClassElement));
			fluentwaitDataClass.until(ExpectedConditions.elementToBeClickable(dataClassElement));
			dataClassElement.click();
			
			logger.info("dataclass element clicked :: ");

			// To Choose data class
			driver.switchTo().defaultContent();
			driver.switchTo().frame(1).switchTo().frame(1).switchTo().frame(0);
			
			logger.info("frame name after frmSearchMain switch :::" + getCurrentFrameName(driver));
			FluentWait<WebDriver> fluentWaitForDataClassSelect = new FluentWait<>(driver)
					.withTimeout(30, TimeUnit.SECONDS).pollingEvery(200, TimeUnit.MILLISECONDS)
					.ignoring(NoSuchElementException.class);
			fluentWaitForDataClassSelect
					.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='cboDataDef']")));
			Select severitySelect = new Select(driver.findElement(By.xpath("//*[@id='cboDataDef']")));
			severitySelect.selectByValue("EcmsClaims");
			logger.info("Choosed EcmsClaims :: ");

			List<ClaimsDownload> claimsDowloadList = getClaimsListFromDb(omniDocsClimsDocDownloaderService);
			transactionInfo.setTotalRecords(String.valueOf(claimsDowloadList.size()));
			int processedCount = 0,claimsWithFiles=0,claimsWithoutFiles=0;

			logger.info("Claims to be download for the current schedule :: "+claimsDowloadList.size());
			for (ClaimsDownload claimsDowload : claimsDowloadList) {
				logger.info("Claim number for which docs to be downloaded :: "+claimsDowload.getClaimNumber());
				if(downloadClaimsDocs(claimsDowload.getClaimNumber(), driver)){
					logger.info("Files are available to downloaded for claim number :: "+claimsDowload.getClaimNumber());
					claimsDowload.setIsFileAvailable(RPAConstants.Y);
					claimsWithFiles++;
					
				}else{
					logger.info("No Files available to downloaded for claim number :: "+claimsDowload.getClaimNumber());
					claimsDowload.setIsFileAvailable(RPAConstants.N);
					claimsWithoutFiles++;
				}
				claimsDowload.setIsProcessed(RPAConstants.Y);
				omniDocsClimsDocDownloaderService.save(claimsDowload);
				processedCount++;
			}
			transactionInfo.setTotalSuccessUploads(String.valueOf(claimsWithFiles));
			transactionInfo.setTotalErrorUploads(String.valueOf(claimsWithoutFiles));
			transactionInfo.setTotalUploadRecords(String.valueOf(processedCount));

			for (String winHandle : driver.getWindowHandles()) {
				driver.switchTo().window(winHandle);
				break;
			}
			driver.getCurrentUrl();
			driver.switchTo().defaultContent();
			driver.switchTo().frame(UtilityFile.waitForElementPresent(driver,
					By.xpath("/html/frameset/frame[@name='frmTop']"), 60));
			logger.info("frame name after frmSearchToolbar switch :::" + getCurrentFrameName(driver));

			WebElement logoutElement = UtilityFile.waitForElementPresent(driver,
					By.xpath("/html/body/div[1]/table/tbody/tr/td[2]/div/center/table/tbody/tr/td[10]/div/center/p/a"),
					60);
			FluentWait<WebDriver> fluentwaitLogout = UtilityFile.getFluentWaitObject(driver, 30, 200);
			fluentwaitLogout.until(ExpectedConditions.visibilityOf(logoutElement));
			fluentwaitLogout.until(ExpectedConditions.elementToBeClickable(logoutElement));
			logoutElement.click();
			logger.info("Logged out :: "+driver.getCurrentUrl());
			
			logger.info("Processor - OmniDocsClaimsDocDownloader - END doMarutiSuzukiLogin() ");
			return true;

		}

	}

	private boolean downloadClaimsDocs(String claimNumber, WebDriver driver)
			throws URISyntaxException, InterruptedException, IOException {
		boolean status  = true;
		String errorMsg = "";
		driver.switchTo().defaultContent();
		driver.switchTo().frame(1).switchTo().frame(1).switchTo().frame(1);
		logger.info("frame name before T63 element :::" + getCurrentFrameName(driver));
		// Set claim number
		WebElement claimNumberElement = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='T63']"), 40);
		claimNumberElement.clear();
		claimNumberElement.sendKeys(claimNumber);

		// Set keyword
		FluentWait<WebDriver> fluentWaitForDataClassSelect = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
				.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
		fluentWaitForDataClassSelect.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("/html/body/form/table/tbody/tr[2]/td[3]/select")));
		Select severitySelect = new Select(
				driver.findElement(By.xpath("/html/body/form/table/tbody/tr[2]/td[3]/select")));
		severitySelect.selectByVisibleText("Contains     ");
		logger.info("Contains choosed :::");

		// Set Documnet Type
		WebElement docType = UtilityFile.waitForElementPresent(driver, By.xpath("//*[@id='T66']"), 40);
		docType.clear();
		docType.sendKeys("Bills");

		// search Submit
		driver.switchTo().defaultContent();
		driver.switchTo().frame(1).switchTo().frame(2);
		logger.info("frame name before search filter button click :::" + getCurrentFrameName(driver));

		WebElement searchSubmit1Element = UtilityFile.waitForElementPresent(driver,
				By.xpath("/html/body/form[1]/table/tbody/tr/td/input[1][@title='Search']"), 40);
		searchSubmit1Element.click();
		
		driver.switchTo().defaultContent();
		driver.switchTo().frame(1).switchTo().frame(3).switchTo().frame(1);
		logger.info("frame name before checking file availability to download :::" + getCurrentFrameName(driver));
		List<WebElement> rows = driver.findElements(By.xpath("/html/body/form[1][@name='mainForm']/table/tbody/tr"));
		System.out.println(rows.size());
		
		if(rows.size()<=1){
			if (errorMsg != null && errorMsg.contains("No Document")) {
				logger.info(" No Documets available to download for Claim ::" + claimNumber);
				status = false;
			}
		} else {
			status = true;
		// check all check box
		WebElement checkAllCheckbox = UtilityFile.waitForElementPresent(driver,
				By.xpath("/html/body/form[1]/table/tbody/tr[1]/td[2]/input[@name='selAll']"), 40);
			/*errorMsg = driver.findElement(By.xpath("/html/body/form[1]/table/tbody/tr/td[2][@class='EWMessage']"))
					.getText();*/
			checkAllCheckbox.click();

			// download click
			driver.switchTo().defaultContent();
			driver.switchTo().frame(1).switchTo().frame(3).switchTo().frame(2);
			logger.info("frame name before file download link click :::" + getCurrentFrameName(driver));
			WebElement downloadElement = UtilityFile.waitForElementPresent(driver,
					By.xpath("/html/body/table/tbody/tr[2]/td[8]"), 40);
			downloadElement.click();
			Thread.sleep(5000);

			File srcDirectory = new File(
					UtilityFile.getCodeBasePath() + UtilityFile.getOmniDocProperty("Omnidoc.File.DownLoad.Location"));

			if (!srcDirectory.exists())
				srcDirectory.mkdirs();

			File[] directoryList = srcDirectory.listFiles();

			for (File file : directoryList) {

				if (file.canRead() && !file.isDirectory()) {
					File destinationFolder = new File(UtilityFile.getCodeBasePath()
							+ UtilityFile.getOmniDocProperty("Omnidoc.File.Claims.Location") + claimNumber.trim());
					if (!destinationFolder.exists()) {
						destinationFolder.mkdirs();
					}
					boolean isFileRenamed = file.renameTo(new File(UtilityFile.getCodeBasePath()
							+ UtilityFile.getOmniDocProperty("Omnidoc.File.Claims.Location") + claimNumber.trim() + "/"
							+ file.getName()));
					logger.info(" isFileRenamed ? :: " + isFileRenamed);
				}
			}

		}
		return status;
	}

	private List<ClaimsDownload> getClaimsListFromDb(
			OmniDocsClaimsDocDownloaderService omniDocsClimsDocDownloaderService) {
		return omniDocsClimsDocDownloaderService.getClaimsListFromDb();

	}

	public String getCurrentFrameName(WebDriver driver) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		String CurrentFrame = js.executeScript("return self.name").toString();
		return CurrentFrame;
	}

}
