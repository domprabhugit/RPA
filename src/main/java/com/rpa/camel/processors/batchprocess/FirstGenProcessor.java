/*
 * Robotic Process Automation
 * @originalAuthor Vijayananth.M
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.processors.batchprocess;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
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
import org.springframework.core.env.Environment;

import com.rpa.camel.config.SpringContext;
import com.rpa.camel.processors.common.ChromeDriverHeadless;
import com.rpa.constants.RPAConstants;
import com.rpa.model.ApplicationConfiguration;
import com.rpa.model.BusinessProcess;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.GLBatchProcessor;
import com.rpa.repository.TransactionInfoRepository;
import com.rpa.service.CommonService;
import com.rpa.service.process.ProcessService;
import com.rpa.service.processors.FirstGenProcessorService;
import com.rpa.util.UtilityFile;

public class FirstGenProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(FirstGenProcessor.class.getName());

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

		logger.info("AutoWiringBeanPropertiesSetMethod is Called in FirstGenProcessor Class");
		applicationContext = SpringContext.getAppContext();
		batchProcessorService = applicationContext.getBean(FirstGenProcessorService.class);
		chromeDriverHeadless = applicationContext.getBean(ChromeDriverHeadless.class);
		transactionInfoRepository = applicationContext.getBean(TransactionInfoRepository.class);
		environment = applicationContext.getBean(Environment.class);
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in FirstGenProcessor Class");

	}

	@Override
	public void process(Exchange exchange)
			throws URISyntaxException, IOException, InterruptedException, ParseException {

		logger.info("*********** inside Camel Process of FirstGenProcessor Called ************");
		logger.info("BEGIN : FirstGenProcessor - GL POST - Started ");
		// driver = chromeDriverHeadless.getDriverInstance();
		driver = (WebDriver) exchange.getIn().getHeader("chromeDriver");
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty("TRANSACTION_INFO_REQ");
		transactionInfo.setProcessPhase(RPAConstants.PROCESSOR);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		FirstGenProcessor firstGenProcessor = new FirstGenProcessor();
		firstGenProcessor.doProcess(firstGenProcessor, driver, new GLBatchProcessor(), null, transactionInfo);
		exchange.setProperty("TRANSACTION_INFO_REQ", transactionInfo);
		logger.info("*********** inside Camel Process of FirstGenProcessor Ended ************");
	}

	public void doProcess(FirstGenProcessor firstGenProcessor, WebDriver driver, GLBatchProcessor GlModel,
			FirstGenReprocessor batchReprocessor, TransactionInfo transactionInfo)
			throws IOException, URISyntaxException, InterruptedException, ParseException {
		logger.info("BEGIN : FirstGenProcessor Processor - GL POST - doProcess Method Called  ");

		// AutoWiring Objects
		AutoWiringBeanPropertiesSetMethod();
		// Regular process for day to day
		if (batchReprocessor != null) {
			firstGenProcessor.dailyReprocess(firstGenProcessor, driver, GlModel, transactionInfo);
		} else {
			String[] moduleCode = getModuleCodeToProcess(transactionInfo);
			for (String code : moduleCode) {
				logger.info("FistGenProcessor Processor - GL POST - Module Code To Be Processed :: " + code);
				GlModel.setModuleCode(code);
				GlModel.setDateFrom(UtilityFile.yesterday());
				GlModel.setDateTo(UtilityFile.yesterday());
				GlModel.setRunNo("");
				firstGenProcessor.dailyProcess(firstGenProcessor, driver, GlModel, transactionInfo);
			}
		}
		logger.info("BEGIN : FirstGenProcessor Processor - GL POST - doProcess Method Ended  ");
	}

	private String[] getModuleCodeToProcess(TransactionInfo transactionInfo) throws ParseException {

		logger.info("BEGIN : FirstGenProcessor Processor - getModuleCodeToProcess Method Called  ");

		String[] moduleCode = {};

		SimpleDateFormat df = new SimpleDateFormat(RPAConstants.yyyy_slash_MM_slash_dd);
		Date currentDate = df.parse(df.format(UtilityFile.yesterday()));

		List<GLBatchProcessor> Gl = batchProcessorService.findByDateFrom(currentDate);
		
		/*Below Line should be uncommented only for testing*/
		//Gl.clear();
		
		transactionInfo.setExternalTransactionRefNo("Status" + RPAConstants.UNDERSCORE
				+ UtilityFile.dateToSting(currentDate, RPAConstants.dd_slash_MM_slash_YYYY));
		List<String> Module = new ArrayList<String>(Arrays.asList("AC", "UW", "CL"));
		List<String> DbModule = new ArrayList<String>();
		if (Gl != null && !Gl.isEmpty()) {

			for (GLBatchProcessor g : Gl) {
				DbModule.add(g.getModuleCode());
				logger.info("FirstGenProcessor Processor - GL POST - Module Code Already process for the day :: "
						+ currentDate + " is :: " + g.getModuleCode());
			}
			if (DbModule.containsAll(Module)) {
				logger.info("FirstGenProcessor Processor - GL POST - All module already processed for the date ::"
						+ currentDate);
				transactionInfo.setProcessStatus(RPAConstants.ALREADY_PROCESSED);
				return moduleCode;
			} else {
				Module.removeAll(DbModule);
			}
		}

		moduleCode = (String[]) Module.toArray(new String[0]);
		logger.info("BEGIN : FirstGenProcessor Processor - getModuleCodeToProcess Method Ended  ");
		return moduleCode;

	}

	private void dailyReprocess(FirstGenProcessor firstGenProcessor, WebDriver driver, GLBatchProcessor GlModel,
			TransactionInfo transactionInfo) throws IOException, URISyntaxException, InterruptedException {
		logger.info("BEGIN : FirstGenProcessor  - GL POST - dailyReprocess Method Called  ");
		transactionInfo.setProcessPhase(RPAConstants.RE + GlModel.getModuleCode() + "-" + RPAConstants.LOGIN);
		transactionInfo.setExternalTransactionRefNo("Status" + RPAConstants.UNDERSCORE
				+ UtilityFile.dateToSting(GlModel.getDateFrom(), RPAConstants.dd_slash_MM_slash_YYYY));

		if (firstGenProcessor.FirstGenLogin(firstGenProcessor, driver, transactionInfo)) {
			if (firstGenProcessor.MenuSelection(driver,transactionInfo))

			{
				transactionInfo.setTransactionStatus(RPAConstants.Abandon);
				if (firstGenProcessor.glAbandon(driver, GlModel, transactionInfo)) {
					GlModel.setProcessedFlag(RPAConstants.Y);
					batchProcessorService.save(GlModel);
					// to maintain the abandon status
					String OldRunNo = transactionInfo.getRunNo().toString();
					transactionInfoRepository.save(transactionInfo);
					logger.info("Gl abandon Status Updated sucessfully");

					GlModel.setRunNo("");// to generate new RunNo.
					GLBatchProcessor GlBatchObject = firstGenProcessor.glExtraction(driver, GlModel, transactionInfo);
					if (GlBatchObject.getRunNo().isEmpty()) {
						transactionInfo.setProcessPhase(
								RPAConstants.RE + GlModel.getModuleCode() + "-" + RPAConstants.EXTRACTION);
						GlBatchObject = firstGenProcessor.glExtraction(driver, GlBatchObject, transactionInfo);
					}

					transactionInfo
							.setProcessPhase(RPAConstants.RE + GlModel.getModuleCode() + "-" + RPAConstants.DOWNLOAD);
					Boolean sc = firstGenProcessor.glBatchSummary(driver, GlBatchObject);
					if (!sc) {
						glBatchSummary(driver, GlBatchObject);
					}

					Boolean success = firstGenProcessor.getDownloadedReport(driver, GlBatchObject);
					if (success) {
						TransactionInfo trnInfo = getNewTransactionObject(transactionInfo);
						transactionInfo.setProcessPhase(
								RPAConstants.RE + GlModel.getModuleCode() + "-" + RPAConstants.VALIDATION);
						Boolean flag = firstGenProcessor.checkReportStatus(driver, GlBatchObject, trnInfo);
						if (flag) {
							GlBatchObject.setTicketId("");
							GlBatchObject.setProcessedFlag(RPAConstants.Y);
							transactionInfo.setProcessPhase(
									RPAConstants.RE + GlModel.getModuleCode() + "-" + RPAConstants.POST);
							if (firstGenProcessor.glPost(driver, GlBatchObject)) {
								batchProcessorService.save(GlBatchObject);
								trnInfo.setReprocessedFlag(RPAConstants.Yes);
								trnInfo.setTransactionStatus(RPAConstants.Success);
								trnInfo.setRunNo(GlBatchObject.getRunNo());
								trnInfo.setOldRunNo(OldRunNo);
								trnInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
								transactionInfoRepository.save(trnInfo);
								logger.info("BatchProcessObject  inserted in the table");
								firstGenProcessor.firstGenLogout(driver, GlBatchObject, transactionInfo);
							}

						} else {

							GlBatchObject.setProcessedFlag(RPAConstants.N);
							GlBatchObject.setTicketId("");
							batchProcessorService.save(GlBatchObject);

							firstGenProcessor.firstGenLogout(driver, GlBatchObject, transactionInfo);
							transactionInfo.setProcessPhase(
									RPAConstants.RE + GlModel.getModuleCode() + "-" + RPAConstants.ALM);
							Boolean t = raiseNewTicket(firstGenProcessor, driver, GlBatchObject, transactionInfo);
							if (t) {
								logger.info("ALM ticket Raised for the moduleCode::" + GlBatchObject.getModuleCode());
							}
							trnInfo.setReprocessedFlag(RPAConstants.Yes);
							trnInfo.setTransactionStatus(RPAConstants.Error);
							trnInfo.setOldRunNo(OldRunNo);
							trnInfo.setRunNo(GlBatchObject.getRunNo());
							trnInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
							transactionInfoRepository.save(trnInfo);
						}

					}
				}
			}
		}

		logger.info("BEGIN : FirstGenProcessor  - GL POST - dailyReprocess ended SuccessFully ");
	}

	private void dailyProcess(FirstGenProcessor firstGenProcessor, WebDriver driver, GLBatchProcessor GlModel,
			TransactionInfo transactionInfo) throws IOException, URISyntaxException, InterruptedException {
		logger.info("BEGIN : FirstGenProcessor  - GL POST - dailyProcess Method Called  ");
		transactionInfo.setProcessPhase(GlModel.getModuleCode() + "-" + RPAConstants.LOGIN);
		if (firstGenProcessor.FirstGenLogin(firstGenProcessor, driver, transactionInfo)) {
			if (firstGenProcessor.MenuSelection(driver,transactionInfo))

			{

				transactionInfo.setProcessPhase(GlModel.getModuleCode() + "-" + RPAConstants.EXTRACTION);
				GLBatchProcessor GlBatchObject = firstGenProcessor.glExtraction(driver, GlModel, transactionInfo);

				if (GlBatchObject.getRunNo().isEmpty()) {
					GlBatchObject = firstGenProcessor.glExtraction(driver, GlBatchObject, transactionInfo);
				}
				transactionInfo.setRunNo(GlBatchObject.getRunNo());
				transactionInfo.setProcessPhase(GlModel.getModuleCode() + "-" + RPAConstants.DOWNLOAD);
				Boolean sc = firstGenProcessor.glBatchSummary(driver, GlBatchObject);
				if (!sc) {
					glBatchSummary(driver, GlBatchObject);
				}
				Boolean success = firstGenProcessor.getDownloadedReport(driver, GlBatchObject);
				if (success) {
					transactionInfo.setProcessPhase(GlModel.getModuleCode() + "-" + RPAConstants.VALIDATION);
					TransactionInfo trnInfo = getNewTransactionObject(transactionInfo);

					Boolean flag = firstGenProcessor.checkReportStatus(driver, GlBatchObject, trnInfo);
					if (flag) {
						GlBatchObject.setTicketId("");
						GlBatchObject.setProcessedFlag(RPAConstants.Y);
						transactionInfo.setProcessPhase(GlModel.getModuleCode() + "-" + RPAConstants.POST);
						if (firstGenProcessor.glPost(driver, GlBatchObject)) {
							batchProcessorService.save(GlBatchObject);
							trnInfo.setReprocessedFlag(RPAConstants.No);
							trnInfo.setTransactionStatus(RPAConstants.Success);
							trnInfo.setRunNo(GlBatchObject.getRunNo());
							trnInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
							transactionInfoRepository.save(trnInfo);
							firstGenProcessor.firstGenLogout(driver, GlBatchObject, transactionInfo);
						}

					} else {

						GlBatchObject.setProcessedFlag(RPAConstants.N);
						GlBatchObject.setTicketId("");
						batchProcessorService.save(GlBatchObject);

						firstGenProcessor.firstGenLogout(driver, GlBatchObject, transactionInfo);
						transactionInfo.setProcessPhase(GlModel.getModuleCode() + "-" + RPAConstants.ALM);
						Boolean t = raiseNewTicket(firstGenProcessor, driver, GlBatchObject, transactionInfo);
						if (t) {
							logger.info("ALM ticket Raised for the moduleCode::" + GlBatchObject.getModuleCode());
						}
						trnInfo.setReprocessedFlag(RPAConstants.No);
						trnInfo.setTransactionStatus(RPAConstants.Error);
						trnInfo.setRunNo(GlBatchObject.getRunNo());
						trnInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
						transactionInfoRepository.save(trnInfo);
					}

				}
			}
		}
		logger.info("BEGIN : FirstGenProcessor  - GL POST - dailyProcess ended SuccessFully ");

	}

	private TransactionInfo getNewTransactionObject(TransactionInfo transactionInfo) {
		logger.info("BEGIN : FirstGenProcessor  - GL POST - getNewTransactionObject() called ");
		TransactionInfo trn = new TransactionInfo();
		trn.setProcessName(transactionInfo.getProcessName());
		trn.setTransactionStatus(transactionInfo.getTransactionStatus());
		trn.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
		logger.info("END : FirstGenProcessor  - GL POST - getNewTransactionObject()");
		return trn;
	}

	private void firstGenLogout(WebDriver driver, GLBatchProcessor glBatchObject, TransactionInfo transactionInfo) {
		logger.info("BEGIN : FirstGenProcessor  - GL POST - firstGenLogout Method Started ");

		ProcessService processService = applicationContext.getBean(ProcessService.class);
		CommonService commonService = applicationContext.getBean(CommonService.class);

		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
				RPAConstants.APPID_FIRSTGRN);

		if (applicationDetails == null) {
			logger.error("Error in firstGenLogout :: Application details not configured for process name :: "
					+ transactionInfo.getProcessName());
		} else {
			String applicationURL = applicationDetails.getUrl();

			driver.get(applicationURL);

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

			logger.info("END : FirstGenProcessor  - GL POST - firstGenLogoout SuccessFully ");
		}

	}

	private Boolean getDownloadedReport(WebDriver driver, GLBatchProcessor glBatchObject)
			throws UnsupportedEncodingException, URISyntaxException {
		logger.info("BEGIN : FirstGenProcessor  - GL POST - getDownloadedReport called() ");

		File files = new File(
				UtilityFile.getCodeBasePath() + UtilityFile.getBatchProperty("FirstGen.File.DownLoad.Location"));

		logger.info("getDownloadedReport File Path::" + files.getPath());

		File[] directoryList = files.listFiles();
		if (directoryList != null) {
			// create Directory process if not exists
			File newFile = new File(UtilityFile.getCodeBasePath()
					+ UtilityFile.getBatchProperty("FirstGen.File.Process.Location") + UtilityFile
							.dateToSting(glBatchObject.getDateFrom(), RPAConstants.dd_slash_MM_slash_YYYY).toString());
			if (!newFile.exists()) {
				newFile.mkdirs();
			}

			if (directoryList.length > 1) // if more than one file in downloads
											// folder
			{
				logger.info("Sort out the Latest DownloadedReport  from the List of Files method Starts Here ");

				Arrays.sort(directoryList, new Comparator<File>() {

					@Override
					public int compare(File One, File two) {
						return Long.valueOf(One.lastModified()).compareTo(two.lastModified());
					}

				});
				logger.info("Sort out the Latest DownloadedReport  from the List of Files method ends Here ");
			}

			for (File file : directoryList) {

				if (file.canRead() && !file.isDirectory()) {

					String fileExtension = getFileExtension(file);

					if(fileExtension.equals(UtilityFile.getBatchProperty("Batch.File.extension"))) {

						logger.info("fileExtension " + fileExtension);

						boolean isRenamed = file.renameTo(new File(newFile + RPAConstants.SLASH + glBatchObject.getRunNo()
						+ RPAConstants.UNDERSCORE + glBatchObject.getModuleCode() + RPAConstants.UNDERSCORE
						+ UtilityFile.dateToSting(glBatchObject.getDateFrom(), RPAConstants.dd_slash_MM_slash_YYYY)
						+ ".pdf"));

						logger.info("is downloaded file Renamed " + isRenamed);

						break;
					}
				}
			}
		}

		return true;

	}
	
	private String getFileExtension(File file) {
	    String name = file.getName();
	    int lastIndexOf = name.lastIndexOf(".");
	    if (lastIndexOf == -1) {
	        return ""; // empty extension
	    }
	    return name.substring(lastIndexOf);
	}

	public boolean FirstGenLogin(FirstGenProcessor batch, WebDriver driver, TransactionInfo transactionInfo)

	{
		logger.info("BEGIN : FirstGenProcessor  - GL POST - FirstGenLogin Method Started ");

		ProcessService processService = applicationContext.getBean(ProcessService.class);
		CommonService commonService = applicationContext.getBean(CommonService.class);

		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
				RPAConstants.APPID_FIRSTGRN);

		if (applicationDetails == null) {
			logger.error("Error in firstGen Login Screen :: Application details not configured for process name :: "
					+ transactionInfo.getProcessName());
			return false;
		} else {

			String FirstGenUrl = applicationDetails.getUrl();
			String userName = applicationDetails.getUsername();
			String password = applicationDetails.getPassword();
			logger.info("current URL" + driver.getCurrentUrl());
			/*if (!driver.getCurrentUrl().equals(FirstGenUrl)) {*/
				driver.get(FirstGenUrl);
				logger.error("FirstGenProcessor  - GL POST FirstGenUrl :: " + driver.getCurrentUrl());
				WebElement user = waitForElementPresent(driver, By.id("userName"), 120);
				user.sendKeys(userName);
				WebElement pass = waitForElementPresent(driver, By.id("password"), 120);
				pass.sendKeys(password);
				//user.submit();
				waitForElementVisible(driver, By.xpath("/html/body/div/center/table/tbody/tr/td/div/table/tbody/tr[2]/td/div/center/form/table/tbody/tr[3]/td[2]/input"), 60).click();
				logger.info("FirstGen application logged in successfully");
			/*}*/
			return true;

		}

	}

	public boolean MenuSelection(WebDriver driver,TransactionInfo transactionInfo) {
		logger.info("BEGIN : FirstGenProcessor  - GL POST - FirstGen MenuSelection Method Started ");

		if(driver.getCurrentUrl().contains("WuiLoginScreen.action")){
			logger.info("BEGIN : FirstGenProcessor  - GL POST - FirstGenLogin Method Started ");

			ProcessService processService = applicationContext.getBean(ProcessService.class);
			CommonService commonService = applicationContext.getBean(CommonService.class);

			BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
			ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
					RPAConstants.APPID_FIRSTGRN);

			if (applicationDetails == null) {
				logger.error("Error in firstGen Login Screen :: Application details not configured for process name :: "
						+ transactionInfo.getProcessName());
			
			} else {

				String FirstGenUrl = applicationDetails.getUrl();
				String userName = applicationDetails.getUsername();
				String password = applicationDetails.getPassword();
				logger.info("current URL" + driver.getCurrentUrl());
				/*if (!driver.getCurrentUrl().equals(FirstGenUrl)) {*/
					driver.get(FirstGenUrl);
					logger.error("FirstGenProcessor  - GL POST FirstGenUrl :: " + driver.getCurrentUrl());
					WebElement user = waitForElementPresent(driver, By.id("userName"), 120);
					user.sendKeys(userName);
					WebElement pass = waitForElementPresent(driver, By.id("password"), 120);
					pass.sendKeys(password);
					/*user.submit();*/
					waitForElementVisible(driver, By.xpath("/html/body/div/center/table/tbody/tr/td/div/table/tbody/tr[2]/td/div/center/form/table/tbody/tr[3]/td[2]/input"), 60).click();
					logger.info("FirstGen application logged in successfully");
				/*}*/

			}
		}
		
		logger.info("MenuSelection() before switch defaultcontent :::" + getCurrentFrameName(driver));
		driver.switchTo().defaultContent();
		logger.info("MenuSelection() before switch to frame, current frame name :::" + getCurrentFrameName(driver));
		WebElement menuFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='Bottom']/frame[1]"), 60);
		driver.switchTo().frame(menuFrameElement);
		logger.info("MenuSelection() after switch to frame, current frame name :::" + getCurrentFrameName(driver));

		WebElement accounts = null;

		// accounts = waitForElementVisible(driver,
		// By.xpath("//*[@id='m0']/a[5]"), 60);
		accounts = waitForElementVisible(driver, By.xpath("//*[@id='m0']/a"), 60);

		Actions action = new Actions(driver);

		action.moveToElement(accounts).build().perform();
		accounts.click();

		/*
		 * WebElement Acnt_Gl_Link = waitForElementVisible(driver,
		 * By.xpath("//*[@id='m0']/div[3]/a[9]"), 60);
		 * action.moveToElement(Acnt_Gl_Link).build().perform();
		 * Acnt_Gl_Link.click();
		 */
		logger.info("BEGIN : FirstGenProcessor  - GL POST - FirstGen MenuSelection Method Ended ");
		return true;

	}

	public GLBatchProcessor glExtraction(WebDriver driver, GLBatchProcessor model, TransactionInfo transactionInfo) throws InterruptedException {
		logger.info("BEGIN : glExtraction()  - GL POST - FirstGen glExtraction Method Started ");
		
		String runNumber = ""; 
		SimpleDateFormat pattern = new SimpleDateFormat("dd/MM/yyyy");
		String ProcessDate = pattern.format(model.getDateFrom()).toString();
		String desc = model.getModuleCode() + " " + ProcessDate + " TO " + ProcessDate;
		desc = desc.replace("/", "");
		WebElement menuFrameElement = null;
		
		if (model.getModuleCode() != null) {
			logger.info("glExtraction() For Module Code:: " + model.getModuleCode());
		} else {
			logger.info("glExtraction() Module Code is null");
		}

		if(driver.getCurrentUrl().contains("WuiLoginScreen.action")){
			logger.info("glExtraction()  - GL POST - FirstGenLogin Method Started ");

			ProcessService processService = applicationContext.getBean(ProcessService.class);
			CommonService commonService = applicationContext.getBean(CommonService.class);

			BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
			ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
					RPAConstants.APPID_FIRSTGRN);

			if (applicationDetails == null) {
				logger.error("Error in glExtraction() Firstgen Login Screen :: Application details not configured for process name :: "
						+ transactionInfo.getProcessName());
				//return false;
			} else {

				String FirstGenUrl = applicationDetails.getUrl();
				String userName = applicationDetails.getUsername();
				String password = applicationDetails.getPassword();
				logger.info("glExtraction() current URL" + driver.getCurrentUrl());
				/*if (!driver.getCurrentUrl().equals(FirstGenUrl)) {*/
					driver.get(FirstGenUrl);
					logger.error("glExtraction()  - GL POST FirstGenUrl :: " + driver.getCurrentUrl());
					WebElement user = waitForElementPresent(driver, By.id("userName"), 120);
					user.sendKeys(userName);
					WebElement pass = waitForElementPresent(driver, By.id("password"), 120);
					pass.sendKeys(password);
					/*user.submit();*/
					waitForElementVisible(driver, By.xpath("/html/body/div/center/table/tbody/tr/td/div/table/tbody/tr[2]/td/div/center/form/table/tbody/tr[3]/td[2]/input"), 60).click();
					logger.info("glExtraction() Firstgen application logged in successfully");
				/*}*/
				//return true;

			}
		}
		
		WebElement accounts = null;
		/*if(driver.findElement( By.xpath("//*[@class='m1']")).isDisplayed() ){
			
		}else{
			accounts = waitForElementVisible(driver, By.xpath("//*[@id='m0']/a"), 60);
			Actions action = new Actions(driver);
			action.moveToElement(accounts).build().perform();
			accounts.click();	
		}*/
		
		

		
		
		logger.info("glExtraction() RunNo Exist Pre-check starts here :::");
		
		/*Run Number Existance Check Starts here*/
		
		driver.switchTo().defaultContent();
		logger.info("glExtraction() RunNo Exist check - menuFrameElement - before switch to frame, current frame name :::"
		+ getCurrentFrameName(driver));
		WebElement menuFrameElementNew = waitForElementVisible(driver, By.xpath("//*[@id='Bottom']/frame[1]"), 60);
		driver.switchTo().frame(menuFrameElementNew);
		logger.info("glExtraction() RunNo Exist check - menuFrameElement - after switch to frame, current frame name :::"
		+ getCurrentFrameName(driver));

		// WebElement glReportsMenu = waitForElementVisible(driver,
		// By.xpath("//*[@id='m0']/div[3]/div[9]/a[3]"), 60);
		
		if(driver.findElement( By.xpath("//*[@class='m1']")).isDisplayed() ){
			logger.info("glExtraction() class=m1 is displayed----------");
		}else{
			logger.info("glExtraction() class=m1 is not displayed----------");
			accounts = waitForElementVisible(driver, By.xpath("//*[@id='m0']/a"), 60);
			logger.info("glExtraction() accounts1----------"+accounts);
			//accounts.click();
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", accounts);
			
			/*JavascriptExecutor executor = (JavascriptExecutor)driver;
			executor.executeScript("arguments[0].click();", accounts);*/
			
			
			/*Actions action = new Actions(driver);
			logger.info("glExtraction() accounts----------"+action);
			action.moveToElement(accounts).build().perform();
			logger.info("glExtraction() accounts----------moved to elem");
			accounts.click();	*/
			logger.info("glExtraction() accounts cliked----------");
		}

		WebElement glReportsMenu = waitForElementVisible(driver, By.xpath("//*[@id='m0']/div/a[5]"), 60);
		Actions actionNew = new Actions(driver);
		actionNew.moveToElement(glReportsMenu).build().perform();
		glReportsMenu.click();
		/*
		* WebElement glBatchSummaryMenu = waitForElementVisible(driver,
		* By.xpath("//*[@id='m0']/div[3]/div[9]/div[1]/a[3]"), 60);
		*/
		WebElement glBatchSummaryMenu = waitForElementVisible(driver, By.xpath("//*[@id='m0']/div/div/a[3]"), 60);
		Actions action = new Actions(driver);
		action.moveToElement(glBatchSummaryMenu).build().perform();
		glBatchSummaryMenu.click();

		driver.switchTo().defaultContent();
		logger.info("glExtraction() RunNo Exist check - detailFrameElement - before switch to frame, current frame name :::"
		+ getCurrentFrameName(driver));

		WebElement detailFrameElementNew = waitForElementVisible(driver, By.xpath("//*[@id='fs2']"), 60);
		driver.switchTo().frame(detailFrameElementNew);

		WebElement runNoHelp = waitForElementPresent(driver, By.xpath("/html/body/form/div/table/tbody/tr[1]/td[2]/input[2]"),
		60);
		runNoHelp.click();

		(new WebDriverWait(driver, 30)).until(ExpectedConditions.numberOfWindowsToBe(2));

		String mainWindowHandle = "";
		for (String handle : driver.getWindowHandles()) {
		if (!handle.equals(mainWindowHandle)) {
		driver.switchTo().window(handle);
		}
		}
		//Thread.sleep(1000);
		
		WebElement runDateElem = waitForElementPresent(driver, By.xpath("//*[@id='RUN_DATE']"),
		60);
		runDateElem.sendKeys(UtilityFile
		.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_yyyy).toString());
		
		//Thread.sleep(1000);

		WebElement moduleElem = waitForElementPresent(driver, By.xpath("//*[@id='MODULE']"),
		60);
		moduleElem.sendKeys(model.getModuleCode());
		
		WebElement statusElem = waitForElementPresent(driver, By.xpath("//*[@id='STATUS']"),
				60);
		statusElem.sendKeys("R");
		
		//Thread.sleep(1000);

		WebElement searchElem = waitForElementPresent(driver, By.xpath("//*[@id='searchAjaxButton']"),
		60);
		searchElem.click();

		// Now get all the TR elements from the table
		List<WebElement> allRows = driver.findElements(By.xpath("//*[@id='headerTable']/tbody/tr"));
		boolean isRunNoAlreadyAvailable = false;

		Thread.sleep(1000);
		// And iterate over them, getting the cells
		int trCount  = 0;
		//for (WebElement row : allRows) {
		for(trCount=0;trCount<allRows.size();trCount++){
			List<WebElement> cells = driver.findElements(By.xpath("//*[@id='headerTable']/tbody/tr["+trCount+"]/td"));
		   // List<WebElement> cells = row.findElements(By.tagName("td"));

		    // Print the contents of each cell
		    int columnNo = 0;
		    String fromDateCellvalue = "",toDateCellValue=""; runNumber= "";
		    Thread.sleep(1000);
		    for (WebElement cell : cells) {
		    columnNo ++;
		   
		    if(columnNo==2){
		    runNumber = cell.getText();
		    }else if(columnNo==5){
		    fromDateCellvalue = cell.getText();
		    }else if (columnNo==6){
		    toDateCellValue = cell.getText();
		    }
		      
		    }
		    
		    if(fromDateCellvalue.equals( UtilityFile
					.dateToSting(model.getDateFrom(), RPAConstants.dd_slash_MM_slash_yyyy).toString()) && toDateCellValue.equals(UtilityFile
							.dateToSting(model.getDateTo(), RPAConstants.dd_slash_MM_slash_yyyy).toString()) ){
			    isRunNoAlreadyAvailable = true;
			    model.setRunNo(runNumber);
			    model.setDescription(desc);
			    logger.info("GlExtraction runNo :: " + runNumber);
			    logger.info("GlExtraction Screen success");
			   
			    break;
			    }
		    
		}
		
		driver.close();
		
		logger.info("glExtraction() RunNo Exist Pre-check Ends here :::");
		
	    for (String handle : driver.getWindowHandles()) {
			if (!handle.equals(mainWindowHandle)) {
			driver.switchTo().window(handle);
			}
			}
	    
		logger.info("glExtraction() RunNo Exist check - detailFrameElement - after switch to frame, current frame name :::"
		+ getCurrentFrameName(driver));
		
		driver.switchTo().defaultContent();
		logger.info("glExtraction() RunNo Exist check - menuFrameElement - before switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));
		menuFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='Bottom']/frame[1]"), 60);
		driver.switchTo().frame(menuFrameElement);

		logger.info("glExtraction() RunNo Exist check - menuFrameElement - after switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));
		
		glReportsMenu = waitForElementVisible(driver, By.xpath("//*[@id='m0']/div/a[5]"), 60);

		action.moveToElement(glReportsMenu).build().perform();

		glReportsMenu.click();
		
		/*Run Number Existance Check Starts here*/
		
		/*GL Extraction Starts here*/
		
		if(!isRunNoAlreadyAvailable){
			logger.info("Gl Extraction - Run Number not created already ");
			
			driver.switchTo().defaultContent();
			logger.info("glExtraction() - menuFrameElement before switch to frame, current frame name :::"
					+ getCurrentFrameName(driver));
			 menuFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='Bottom']/frame[1]"), 60);
			driver.switchTo().frame(menuFrameElement);
			logger.info("glExtraction() -menuFrameElement after switch to frame, current frame name :::"
					+ getCurrentFrameName(driver));

			/*
			 * WebElement glExtractionMenu = waitForElementVisible(driver,
			 * By.xpath("//*[@id='m0']/div[3]/div[9]/a[1]"), 60);
			 */
			
			if(driver.findElement( By.xpath("//*[@class='m1']")).isDisplayed() ){
				
			}else{
				accounts = waitForElementVisible(driver, By.xpath("//*[@id='m0']/a"), 60);
				logger.info("glExtraction() accounts2----------"+accounts);
				 /*action = new Actions(driver);
				action.moveToElement(accounts).build().perform();
				accounts.click();*/	
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", accounts);
			}
			
			WebElement glExtractionMenu = waitForElementVisible(driver, By.xpath("//*[@id='m0']/div/a[1]"), 60);

			logger.info("GlExtraction Screen");
			 action = new Actions(driver);

			action.moveToElement(glExtractionMenu).build().perform();
			glExtractionMenu.click();

			driver.switchTo().defaultContent();
			logger.info("glExtraction()  detailframe before switch to frame, current frame name :::"
					+ getCurrentFrameName(driver));
			WebElement detailFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='fs2']"), 60);
			driver.switchTo().frame(detailFrameElement);
			logger.info("glExtraction() detailframe after switch to frame, current frame name :::"
					+ getCurrentFrameName(driver));

			WebElement moduleSelectWait = waitForElementPresent(driver, By.xpath("//*[@id='selmodule']"), 60);
			Select moduleSelect = new Select(moduleSelectWait);
			moduleSelect.selectByValue(model.getModuleCode());
			
			
			WebElement FromDate = waitForElementVisible(driver, By.id("txtFromDate"), 60);
			FromDate.sendKeys(ProcessDate.toString());

			WebElement toDate = waitForElementVisible(driver, By.id("txtToDate"), 60);

			toDate.sendKeys(ProcessDate.toString());
			
			WebElement description = driver.findElement(By.id("txtDesc"));
			logger.info("GlExtraction Description :: " + desc);
			description.sendKeys(desc);
			String[] runNo;

			WebElement ExtractButton = driver.findElement(By.xpath("//*[@class='Det_Button']"));

			ExtractButton.click();
			WebElement runningNo = null;
			
			try{
			runningNo = waitForElementPresent(driver, By.xpath("//*[@class='block']/table[2]/tbody/tr/td/font"),
					180);
			runningNo = waitForElementVisible(driver, By.xpath("//*[@class='block']/table[2]/tbody/tr/td/font"), 180);
			logger.info("GlExtraction catched run no :: " + runningNo);
			}catch(Exception e){
				logger.info("glExtraction() RunNo Exist Post-check Starts here :::");
				/*Run Number Existance Check Starts here*/
				runningNo = null;
				driver.switchTo().defaultContent();
				logger.info("glExtraction() RunNo Exist check after Exception - menuFrameElement - before switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));
				 menuFrameElementNew = waitForElementVisible(driver, By.xpath("//*[@id='Bottom']/frame[1]"), 60);
				driver.switchTo().frame(menuFrameElementNew);
				logger.info("glExtraction() RunNo Exist check after Exception - menuFrameElement - after switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

				// WebElement glReportsMenu = waitForElementVisible(driver,
				// By.xpath("//*[@id='m0']/div[3]/div[9]/a[3]"), 60);
				
				if(driver.findElement( By.xpath("//*[@class='m1']")).isDisplayed() ){
					
				}else{
					accounts = waitForElementVisible(driver, By.xpath("//*[@id='m0']/a"), 60);
					logger.info("glExtraction() accounts3----------"+accounts);
					 /*action = new Actions(driver);
					action.moveToElement(accounts).build().perform();
					accounts.click();*/	
					((JavascriptExecutor) driver).executeScript("arguments[0].click();", accounts);
				}
				
				 glReportsMenu = waitForElementVisible(driver, By.xpath("//*[@id='m0']/div/a[5]"), 60);

				 actionNew = new Actions(driver);

				actionNew.moveToElement(glReportsMenu).build().perform();

				glReportsMenu.click();
				/*
				* WebElement glBatchSummaryMenu = waitForElementVisible(driver,
				* By.xpath("//*[@id='m0']/div[3]/div[9]/div[1]/a[3]"), 60);
				*/
				 glBatchSummaryMenu = waitForElementVisible(driver, By.xpath("//*[@id='m0']/div/div/a[3]"), 60);
				 action = new Actions(driver);
				action.moveToElement(glBatchSummaryMenu).build().perform();
				glBatchSummaryMenu.click();

				driver.switchTo().defaultContent();
				logger.info("glExtraction() RunNo Exist check after Exception - detailFrameElement - before switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

				 detailFrameElementNew = waitForElementVisible(driver, By.xpath("//*[@id='fs2']"), 60);
				driver.switchTo().frame(detailFrameElementNew);

				 runNoHelp = waitForElementPresent(driver, By.xpath("/html/body/form/div/table/tbody/tr[1]/td[2]/input[2]"),
				60);
				runNoHelp.click();

				(new WebDriverWait(driver, 30)).until(ExpectedConditions.numberOfWindowsToBe(2));

				 mainWindowHandle = "";
				for (String handle : driver.getWindowHandles()) {
				if (!handle.equals(mainWindowHandle)) {
				driver.switchTo().window(handle);
				}
				}
				//Thread.sleep(1000);
				
				 runDateElem = waitForElementPresent(driver, By.xpath("//*[@id='RUN_DATE']"),
				60);
				runDateElem.sendKeys(UtilityFile
				.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_yyyy).toString());
				
				//Thread.sleep(1000);

				 moduleElem = waitForElementPresent(driver, By.xpath("//*[@id='MODULE']"),
				60);
				moduleElem.sendKeys(model.getModuleCode());
				
				 statusElem = waitForElementPresent(driver, By.xpath("//*[@id='STATUS']"),
						60);
				statusElem.sendKeys("R");
				
				//Thread.sleep(1000);

				 searchElem = waitForElementPresent(driver, By.xpath("//*[@id='searchAjaxButton']"),
				60);
				searchElem.click();

				// Now get all the TR elements from the table
				 allRows = driver.findElements(By.xpath("//*[@id='headerTable']/tbody/tr"));
				 isRunNoAlreadyAvailable = false;

				Thread.sleep(1000);
				// And iterate over them, getting the cells
				 trCount  = 0;
				//for (WebElement row : allRows) {
				for(trCount=0;trCount<allRows.size();trCount++){
					trCount++;
					List<WebElement> cells = driver.findElements(By.xpath("//*[@id='headerTable']/tbody/tr["+trCount+"]/td"));
				   // List<WebElement> cells = row.findElements(By.tagName("td"));

				    // Print the contents of each cell
				    int columnNo = 0;
				    String fromDateCellvalue = "",toDateCellValue=""; runNumber= "";
				    Thread.sleep(1000);
				    for (WebElement cell : cells) {
				    columnNo ++;
				   
				    if(columnNo==2){
				    runNumber = cell.getText();
				    }else if(columnNo==5){
				    fromDateCellvalue = cell.getText();
				    }else if (columnNo==6){
				    toDateCellValue = cell.getText();
				    }
				      
				    }
				    
				    if(fromDateCellvalue.equals( UtilityFile
							.dateToSting(model.getDateFrom(), RPAConstants.dd_slash_MM_slash_yyyy).toString()) && toDateCellValue.equals(UtilityFile
									.dateToSting(model.getDateTo(), RPAConstants.dd_slash_MM_slash_yyyy).toString()) ){
					    isRunNoAlreadyAvailable = true;
					    model.setRunNo(runNumber);
					    model.setDescription(desc);
					    logger.info("GlExtraction runNo :: " + runNumber);
					    logger.info("GlExtraction Screen success");
					   
					    break;
					    }
				    
				}
				
				driver.close();
				logger.info("glExtraction() RunNo Exist Post-check ends here :::");
			    for (String handle : driver.getWindowHandles()) {
					if (!handle.equals(mainWindowHandle)) {
					driver.switchTo().window(handle);
					}
					}
			    
				logger.info("glExtraction() RunNo Exist check after Exception - detailFrameElement - after switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));
				
				driver.switchTo().defaultContent();
				logger.info("glExtraction() RunNo Exist check after Exception - menuFrameElement - before switch to frame, current frame name :::"
						+ getCurrentFrameName(driver));

				driver.switchTo().frame(menuFrameElement);

				logger.info("glExtraction() RunNo Exist check after Exception - menuFrameElement - after switch to frame, current frame name :::"
						+ getCurrentFrameName(driver));

				action.moveToElement(glReportsMenu).build().perform();

				glReportsMenu.click();
				
				/*Run Number Existance Check Ends here*/
				
			}
			
			
			if(runningNo!=null){
				runNo = runningNo.getText().split("   ");
				logger.info("GlExtraction runNo :: " + runningNo.getText());
		
				if (!runNo[1].equalsIgnoreCase("null") || !runNo[1].equalsIgnoreCase("")) {
					model.setRunNo(String.valueOf(runNo[1]));
					model.setDescription(desc);
		
					logger.info("GlExtraction Screen success");
				} else {
					logger.info("GlExtraction Screen Error Creating RunNo");
					model.setRunNo("");
				}
				
			}
		}else{
			logger.info("Gl Extraction - Run Number created already - Skipped extraction process");
		}
		

		
		
		return model;

	}

	private Boolean glBatchSummary(WebDriver driver, GLBatchProcessor model)
			throws InterruptedException, UnsupportedEncodingException, URISyntaxException {

		logger.info("glBatchSummary Screen Method is Started");
		Boolean exist, delete = false;

		driver.switchTo().defaultContent();
		logger.info("glBatchSummary() menuFrameElement - before switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));
		WebElement menuFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='Bottom']/frame[1]"), 60);
		driver.switchTo().frame(menuFrameElement);
		logger.info("glBatchSummary() menuFrameElement - after switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		// WebElement glReportsMenu = waitForElementVisible(driver,
		// By.xpath("//*[@id='m0']/div[3]/div[9]/a[3]"), 60);
		WebElement accounts = null;
		if(driver.findElement( By.xpath("//*[@class='m1']")).isDisplayed() ){
			
		}else{
			accounts = waitForElementVisible(driver, By.xpath("//*[@id='m0']/a"), 60);
			logger.info("glExtraction() accounts4----------"+accounts);
			/*Actions action = new Actions(driver);
			action.moveToElement(accounts).build().perform();
			accounts.click();*/	
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", accounts);
		}

		WebElement glReportsMenu = waitForElementVisible(driver, By.xpath("//*[@id='m0']/div/a[5]"), 60);

		Actions action = new Actions(driver);

		action.moveToElement(glReportsMenu).build().perform();

		glReportsMenu.click();
		
		logger.info("glBatchSummary() Report Menu clicked :::");
		
		/*
		 * WebElement glBatchSummaryMenu = waitForElementVisible(driver,
		 * By.xpath("//*[@id='m0']/div[3]/div[9]/div[1]/a[3]"), 60);
		 */
		WebElement glBatchSummaryMenu = waitForElementVisible(driver, By.xpath("//*[@id='m0']/div/div/a[3]"), 60);

		action.moveToElement(glBatchSummaryMenu).build().perform();
		glBatchSummaryMenu.click();
		
		logger.info("glBatchSummary() glBatch summary Menu clicked :::");

		driver.switchTo().defaultContent();
		logger.info("glBatchSummary() detailFrameElement - before switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		WebElement detailFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='fs2']"), 60);
		driver.switchTo().frame(detailFrameElement);
		logger.info("glBatchSummary() detailFrameElement - after switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		WebElement runNoField = waitForElementVisible(driver,
				By.xpath("//*[@class='block']/table/tbody/tr/td[2]/input[1]"), 60);

		runNoField.sendKeys(String.valueOf(model.getRunNo()));
		
		logger.info("glBatchSummary() Setting run number :::"+String.valueOf(model.getRunNo()));

		FluentWait<WebDriver> reportWait = new FluentWait<WebDriver>(driver).withTimeout(120, TimeUnit.SECONDS)
				.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);

		reportWait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//form[@name='GLLinkBatchSummary']/table[2]/tbody/tr/td/input[3]")));

		String downloadFilePath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getBatchProperty("FirstGen.File.DownLoad.Location");
		// create a download folder in the deployed path
		File file = new File(downloadFilePath);
		if (!file.exists()) {
			file.mkdirs();
		}

		// Method to Delete existing Files in downloadpath

		delete = UtilityFile.isFileDeleted(downloadFilePath, UtilityFile.getBatchProperty("Batch.File.extension"));
		if (delete) {
			logger.info("DownloadPath cleared for new Download");
		}else{
			logger.info("DownloadPath No File available to delete");
		}

		int i = 0;
		do {
			WebElement report = driver
					.findElement(By.xpath("//form[@name='GLLinkBatchSummary']/table[2]/tbody/tr/td/input[3]"));
			report.click();
			Thread.sleep(10000);

			logger.info("glBatchSummary() Screen File Exist or not check method called here");
			exist = UtilityFile.isFileDownload_Exist(downloadFilePath,
					UtilityFile.getBatchProperty("Batch.File.extension"));
			i++;
			if (i == 10) {
				logger.info("glBatchSummary Screen  Report not Able To download after multiple Tries ");
				break;
			}else{
				logger.info("glBatchSummary Screen  Report download try :: "+i);
			}

		} while (exist != true);

		if (exist) {
			logger.info("  glBatchSummary Screen  Report Download successfully ");

			logger.info("  glBatchSummary Screen successfully Completed ");
			driver.switchTo().defaultContent();
			logger.info("glBatchSummary() menuFrameElement - before switch to frame, current frame name :::"
					+ getCurrentFrameName(driver));

			driver.switchTo().frame(menuFrameElement);

			logger.info("glBatchSummary() menuFrameElement - after switch to frame, current frame name :::"
					+ getCurrentFrameName(driver));

			action.moveToElement(glReportsMenu).build().perform();

			glReportsMenu.click();
		}
		return exist;

	}

	private Boolean checkReportStatus(WebDriver driver, GLBatchProcessor glBatchObject, TransactionInfo transactionInfo)
			throws IOException, URISyntaxException {

		logger.info("PDF Error Check Method is Started::");
		Boolean status = false;
		int glBatchProcess_pdfLineNo = 19;
		String refNo = glBatchObject.getRunNo() + RPAConstants.UNDERSCORE + glBatchObject.getModuleCode()
				+ RPAConstants.UNDERSCORE
				+ UtilityFile.dateToSting(glBatchObject.getDateFrom(), RPAConstants.dd_slash_MM_slash_YYYY);

		File file = new File(
				UtilityFile.getCodeBasePath() + UtilityFile.getBatchProperty("FirstGen.File.Process.Location")
						+ UtilityFile.dateToSting(glBatchObject.getDateFrom(), RPAConstants.dd_slash_MM_slash_YYYY)
						+ RPAConstants.SLASH + refNo + ".pdf");

		logger.info("Batch Process Path::" + file.getPath());
		if (file.canRead()) {
			PDFTextStripper pdfTextStripper = new PDFTextStripper();

			PDDocument pdDocument = PDDocument.load(file);
			int totalPages = pdDocument.getNumberOfPages();

			pdfTextStripper.setStartPage(1);
			pdfTextStripper.setEndPage(totalPages);
			String pdfFileInText = pdfTextStripper.getText(pdDocument);
			String lines[] = pdfFileInText.split("\\r?\\n");
			int i = 0;
			for (String line : lines) {
				if (i == glBatchProcess_pdfLineNo) {
					if (line.equals("0")) {

						status = true;

						logger.info("pdf with No Error");
					}
				}
				i++;
			}
			pdDocument.close();
		}
		
		transactionInfo.setExternalTransactionRefNo(refNo);
		File newFile = file;
		if (status) {
			File successFile = new File(
					UtilityFile.getCodeBasePath() + UtilityFile.getBatchProperty("FirstGen.File.Process.Location")
							+ UtilityFile.dateToSting(glBatchObject.getDateFrom(), RPAConstants.dd_slash_MM_slash_YYYY)
							+ UtilityFile.getBatchProperty("FirstGen.File.Success.Location"));
			if (!successFile.exists()) {
				successFile.mkdir();
			}
			FileUtils.copyFileToDirectory(newFile, successFile);
			logger.info("File SuccessFully Moved To Success Folder");
			transactionInfo.setSuccessFileDownload(successFile + "\\" + newFile.getName());
			logger.info("Success File Path::" + successFile + "\\" + newFile.getName());

		} else {
			File errorFile = new File(
					UtilityFile.getCodeBasePath() + UtilityFile.getBatchProperty("FirstGen.File.Process.Location")
							+ UtilityFile.dateToSting(glBatchObject.getDateFrom(), RPAConstants.dd_slash_MM_slash_YYYY)
							+ UtilityFile.getBatchProperty("FirstGen.File.Error.Location"));
			if (!errorFile.exists()) {
				errorFile.mkdir();
			}

			FileUtils.copyFileToDirectory(newFile, errorFile);
			logger.info("File SuccessFully Moved To Error Folder");
			transactionInfo.setErrorFileDownload(errorFile + "\\" + newFile.getName());
			logger.info("File Path::" + errorFile + "\\" + newFile.getName());
		}

		return status;

	}

	private boolean glAbandon(WebDriver driver, GLBatchProcessor model, TransactionInfo transactionInfo) throws UnsupportedEncodingException, IOException, URISyntaxException {
		Boolean success = false;

		transactionInfo.setProcessPhase(RPAConstants.ABANDON);
		logger.info("glAbandon Screen start");

		driver.switchTo().defaultContent();
		logger.info("glAbandon() menuFrameElement - before switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		WebElement menuFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='Bottom']/frame[1]"), 60);

		driver.switchTo().frame(menuFrameElement);

		logger.info("glAbandon() menuFrameElement - after switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		logger.info("glAbandon - switched frame to menu");

		/*
		 * WebElement glAbandonMenu = waitForElementPresent(driver,
		 * By.xpath("//*[@id='m0']/div[3]/div[9]/a[5]"), 60);
		 */
		
		WebElement accounts = null;
		if(driver.findElement( By.xpath("//*[@class='m1']")).isDisplayed() ){
			
		}else{
			accounts = waitForElementVisible(driver, By.xpath("//*[@id='m0']/a"), 60);
			logger.info("glExtraction() accounts5----------"+accounts);
			/*Actions action = new Actions(driver);
			action.moveToElement(accounts).build().perform();
			accounts.click();*/	
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", accounts);
		}

		WebElement glAbandonMenu = waitForElementPresent(driver, By.xpath("//*[@id='m0']/div/a[2]"), 60);
		Actions action = new Actions(driver);

		action.moveToElement(glAbandonMenu).build().perform();

		glAbandonMenu.click();

		driver.switchTo().defaultContent();
		logger.info("glAbandon() detailFrameElement - before switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		WebElement detailFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='fs2']"), 60);
		driver.switchTo().frame(detailFrameElement);

		logger.info("glAbandon() detailFrameElement - after switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		logger.info("glAbandon - switched frame to detail");

		WebElement runNoFieldAbandon = waitForElementPresent(driver,
				By.xpath("//*[@class='block']/table/tbody/tr/td[2]/input[1]"), 60);

		runNoFieldAbandon.sendKeys(String.valueOf(model.getRunNo()));

		logger.info("glAbandon - run number entered");

		WebElement glAbandonButton = waitForElementPresent(driver,
				By.xpath("//form[@name='GLLinkAbandon']/table[2]/tbody/tr/td/input[2]"), 60);
		logger.info("glAbandon - found gl link button element");
		glAbandonButton.click();

		logger.info("glAbandon process Ended Successfully");
		success = true;
		
		if(success)
		{
			transactionInfo.setErrorFileDownload(ErrorFileMovementMethod( UtilityFile.getCodeBasePath() + UtilityFile.getBatchProperty("FirstGen.File.Process.Location")
			+ UtilityFile.dateToSting(model.getDateFrom(), RPAConstants.dd_slash_MM_slash_YYYY),
			new File(transactionInfo.getErrorFileDownload()),  UtilityFile.getBatchProperty("FirstGen.File.Abandon.Location")));
		}
		
		return success;

	}

	private boolean glPost(WebDriver driver, GLBatchProcessor model) {

		Boolean success = false;

		logger.info("glPost Screen Method Starts Here");

		driver.switchTo().defaultContent();
		logger.info("glPost() menuFrameElement - before switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		WebElement menuFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='Bottom']/frame[1]"), 60);
		driver.switchTo().frame(menuFrameElement);

		logger.info("glPost() menuFrameElement - after switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		logger.info("glPost - Switched to menu frame");

		Actions action = new Actions(driver);
		logger.info("glPost - Clicked on GL Report menu");

		// WebElement Acnt_Gl_Link = waitForElementVisible(driver,
		// By.xpath("//*[@id='m0']/div[3]/a[9]"), 40);
		WebElement Acnt_Gl_Link = waitForElementVisible(driver, By.xpath("//*[@id='m0']/div/a[3]"), 40);

		action.moveToElement(Acnt_Gl_Link).build().perform();

		logger.info("glPost - after Acnt_Gl_Link");

		driver.switchTo().defaultContent();
		logger.info("glPost() menuFrameElement - before switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		WebElement menuFrameElem = waitForElementVisible(driver, By.xpath("//*[@id='Bottom']/frame[1]"), 60);
		driver.switchTo().frame(menuFrameElem);
		logger.info("glPost() menuFrameElement - after switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		logger.info("glPost - switched to menu frame");

		logger.info("glPost - waiting for GL Post menu anchor element");

		WebElement glPostMenu = waitForElementPresent(driver, By.linkText("GL Post"), 60);
		glPostMenu = waitForElementVisible(driver, By.linkText("GL Post"), 60);

		glPostMenu.click();

		logger.info("glPost - Clicked on GL Post menu");
		driver.switchTo().defaultContent();
		logger.info("glPost() detailFrameElement - before switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		WebElement detailFrameElement = waitForElementVisible(driver, By.xpath("//*[@id='fs2']"), 60);
		driver.switchTo().frame(detailFrameElement);

		logger.info("glPost() detailFrameElement - after switch to frame, current frame name :::"
				+ getCurrentFrameName(driver));

		WebElement runNoFieldPost = waitForElementVisible(driver,
				By.xpath("//*[@class='block']/table/tbody/tr/td[2]/input[1]"), 60);

		runNoFieldPost.sendKeys(String.valueOf(model.getRunNo()));

		WebElement GlPostButton = waitForElementPresent(driver,
				By.xpath("//form[@name='GLLinkPostForm']/table[2]/tbody/tr/td/input[2]"), 60);
		GlPostButton = driver.findElement(By.xpath("//form[@name='GLLinkPostForm']/table[2]/tbody/tr/td/input[2]"));
		GlPostButton.click();

		success = true;
		logger.info("glPost Screen Method Ends Here successfully");
		return success;

	}

	// ALM Related Method Here

	public boolean doAlmLogin(WebDriver driver, TransactionInfo transactionInfo) {
		logger.info("Processor - GL POST - BEGIN doAlmLogin()  called ");
		applicationContext = SpringContext.getAppContext();
		ProcessService processService = applicationContext.getBean(ProcessService.class);
		CommonService commonService = applicationContext.getBean(CommonService.class);

		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
				RPAConstants.APPID_ALM);

		if (applicationDetails == null) {
			logger.error("Error in doAlmLogin :: Application details not configured for process name :: "
					+ transactionInfo.getProcessName());
			return false;
		} else {



			logger.error("FirstGenProcessor  - doAlmLogin curent url after alm get url :: " + driver.getCurrentUrl());
			String userName = applicationDetails.getUsername();
			String password = applicationDetails.getPassword();
			
			driver.get(applicationDetails.getUrl());
			
			if(driver.findElements(By.xpath("//*[@id='details-button']")).size()>0){
				driver.findElement(By.xpath("//*[@id='details-button']")).click();
			}
			
			if(driver.findElements(By.xpath("//*[@id='proceed-link']")).size()>0){
				driver.findElement(By.xpath("//*[@id='proceed-link']")).click();
			}
			
			
			WebElement userNameElement = waitForElementPresent(driver, By.name("form_loginname"), 40);
			WebElement passwordElement = waitForElementPresent(driver, By.name("form_pw"), 40);
			WebElement submit1Element = waitForElementPresent(driver, By.name("login"), 40);
			userNameElement.sendKeys(userName);
			passwordElement.sendKeys(password);
			submit1Element.submit();
			logger.info("Processor - GL POST - Logged Into Application Successfully::" + driver.getCurrentUrl());

			// after login to select Support Desk
//			WebElement FirstSupportDeskLinkElement = driver
//					.findElement(By.xpath("//*[@id='widget_myprojects-0']/div[2]/table/tbody/tr[1]/td[2]/a"));
			
			/*WebElement FirstSupportDeskLinkElement = driver
					.findElement(By.xpath("//*[@id='main-container']/main/div[2]/div[2]/div[2]/div[1]/section[1]/div/section/table/tbody/tr[1]/td[2]/a"));
			FluentWait<WebDriver> fluentwait = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
					.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
			fluentwait.until(ExpectedConditions.visibilityOf(FirstSupportDeskLinkElement));
			fluentwait.until(ExpectedConditions.elementToBeClickable(FirstSupportDeskLinkElement));
			FirstSupportDeskLinkElement.click();*/
			logger.info("Processor - GL POST - End doAlmLogin() ");
			return true;

		}

	}

	public boolean raiseNewTicket(FirstGenProcessor trackIssue, WebDriver driver, GLBatchProcessor glBatchObject,
			TransactionInfo transactionInfo) {
		logger.info("BEGIN - raiseNewTicket method called() ");
		String runNo = glBatchObject.getRunNo();

		logger.info("Navigated to correct webpage::" + driver.getCurrentUrl());
		if (trackIssue.doAlmLogin(driver, transactionInfo)) {

			if (trackIssue.accessSupportDeskDashBaord(driver)) {
				logger.info("Processor - GL POST - accessed supportdesk alm dashboard first time");
				if (trackIssue.accessNewTicketPage(driver)) {
					logger.info("Processor - GL POST - accessed Ticket page");
					if (trackIssue.setTicketValues(driver, runNo)) {
						logger.info("Processor - GL POST - ALM ticket values entered");
						//if (trackIssue.accessSupportDeskDashBaord(driver)) {
							//logger.info("Processor - GL POST - accessed supportdesk alm dashboard second time");
							if (trackIssue.updateTicketId(driver, runNo)) {
								logger.info("Processor - GL POST - updated ticket id");
								trackIssue.doAlmLogout(driver, transactionInfo);

							}
						//}
					}
				}
			}
		}
		logger.info("END - raiseNewTicket method called() ");
		return false;
	}

	public void doAlmLogout(WebDriver driver, TransactionInfo transactionInfo) {

		logger.info("BEGIN - ALM logout Method called()");

		ProcessService processService = applicationContext.getBean(ProcessService.class);
		CommonService commonService = applicationContext.getBean(CommonService.class);

		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		ApplicationConfiguration applicationDetails = commonService.getApplicationDetails(businessProcess.getId(),
				RPAConstants.APPID_ALM);

		if (applicationDetails == null) {
			logger.error("Error in doAlmLogout :: Application details not configured for process name :: "
					+ transactionInfo.getProcessName());
		} else {

			driver.get(applicationDetails.getUrl());

			//WebElement logout = driver.findElement(By.xpath("//*[@id='header']/table/tbody/tr/td[2]/ul/li[2]/a"));
			WebElement logout = driver.findElement(By.xpath("//*[@id='user-nav']/form/button"));
			
			logout.click();
		}
		logger.info("END - ALM logout Method");
	}

	public boolean accessSupportDeskDashBaord(WebDriver driver) {
		logger.info("Processor - GL POST - BEGIN accessSupportDeskDashBaord()  called ");

		//WebElement supportDeskLinkElement = driver.findElement(By.xpath("//*[@id='level_2']/ul/li[1]/a"));
		WebElement supportDeskLinkElement = driver.findElement(By.xpath("//*[@id='main-container']/main/div[2]/div[2]/div[2]/div[1]/section[1]/div/section/table/tbody/tr[2]/td[2]/a"));
		
		FluentWait<WebDriver> fluentWait = new FluentWait<>(driver).withTimeout(40, TimeUnit.SECONDS)
				.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
		fluentWait.until(ExpectedConditions.visibilityOf(supportDeskLinkElement));
		fluentWait.until(ExpectedConditions.elementToBeClickable(supportDeskLinkElement));
		supportDeskLinkElement.click();
		/*if(!driver.getCurrentUrl().contains("desktop")){
			driver.get("https://10.46.192.193/projects/desktop/");
		}*/
		if(!driver.getCurrentUrl().contains("supportdeskv2")){
			driver.get("https://10.46.192.193/projects/supportdeskv2/");
		}
		logger.info("Processor - GL POST - Accessed Support Desk Dashboard Successfully::" + driver.getCurrentUrl());
		return true;
	}

	private boolean accessNewTicketPage(WebDriver driver) {
		logger.info("Processor - GL POST - BEGIN accessNewTicketPage()  called ");
//		WebElement NewTicketLinkElement = driver.findElement(By.xpath("//*[@id='level_2']/ul/li[3]/a"));
		WebElement NewTicketLinkElement = driver.findElement(By.xpath("//*[@id='main-container']/aside/div[1]/div[1]/nav/a[2]"));
		FluentWait<WebDriver> fluentWait = new FluentWait<>(driver).withTimeout(40, TimeUnit.SECONDS)
				.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
		fluentWait.until(ExpectedConditions.visibilityOf(NewTicketLinkElement));
		fluentWait.until(ExpectedConditions.elementToBeClickable(NewTicketLinkElement));
		NewTicketLinkElement.click();
		logger.info("Processor - GL POST - Accessed Support Desk Dashboard Successfully::" + driver.getCurrentUrl());
		return true;
	}

	private boolean setTicketValues(WebDriver driver, String runNo) {
		logger.info("Processor - GL POST - BEGIN setTicketValues()  called ");

		String requestType = "", serviceType = "", categoryType = "", subCategory = "", classification = "",
				priority = "", severity = "", region = "", location = "", extension = "", subject = "";

		if (Arrays.stream(environment.getActiveProfiles())
				.anyMatch(env -> (env.equalsIgnoreCase("uat") || env.equalsIgnoreCase("dev")))) {
			/* UAT parameters */
			requestType = "11708";
			serviceType = "11848";
			categoryType = "11803";
			subCategory = "11856";
			classification = "12168";
			priority = "15665";
			severity = "12160";
			region = "12360";
			location = "12367";
			extension = "7169";
			subject = "GL Batch upload Error For  Run No.:" + runNo;
		}
		// Check if Active profiles contains "prod"
		else if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
			/* Live parameters */
			requestType = "11709";
			serviceType = "11849";
			categoryType = "13505";
			subCategory = "12053";
			classification = "12170";
			priority = "15665";
			severity = "12160";
			region = "12360";
			location = "12367";
			extension = "7268";
			subject = "GL Batch upload Error For  Run No.:" + runNo;
		}

		String description = "Dr Team,\n Account Batch posting Error, Kindly find this Run No.: " + runNo;

		FluentWait<WebDriver> fluentWait = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
				.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);

		/* Setting Request Type */
		FluentWait<WebDriver> fluentWaitForReuestSelect = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
				.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
		fluentWaitForReuestSelect
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_field_9436']")));
		fluentWait.until(ExpectedConditions.presenceOfElementLocated(
				(By.xpath("//*[@id='tracker_field_9436']/option[@value='" + requestType + "']"))));
		Select requestTypeSelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_field_9436']")));
		requestTypeSelect.selectByValue(requestType);

		/* Setting Service */
		FluentWait<WebDriver> fluentWaitForServiceSelect = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
				.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
		fluentWaitForServiceSelect
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_field_9440']")));
		fluentWait.until(ExpectedConditions.presenceOfElementLocated(
				(By.xpath("//*[@id='tracker_field_9440']/option[@value='" + serviceType + "']"))));
		Select serviceSelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_field_9440']")));
		serviceSelect.selectByValue(serviceType);

		/* Setting Category */
		FluentWait<WebDriver> fluentWaitForCategorySelect = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
				.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
		fluentWaitForCategorySelect
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_field_9437']")));
		fluentWait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//*[@id='tracker_field_9437']/option[@value='" + categoryType + "']")));
		Select categorySelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_field_9437']")));
		categorySelect.selectByValue(categoryType);

		/* Setting SubCategory */
		FluentWait<WebDriver> fluentWaitForSubCategorySelect = new FluentWait<>(driver)
				.withTimeout(30, TimeUnit.SECONDS).pollingEvery(200, TimeUnit.MILLISECONDS)
				.ignoring(NoSuchElementException.class);
		fluentWaitForSubCategorySelect
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_field_9441']")));
		fluentWait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//*[@id='tracker_field_9441']/option[@value='" + subCategory + "']")));
		Select subCategorySelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_field_9441']")));
		subCategorySelect.selectByValue(subCategory);

		/* Setting classification */
		FluentWait<WebDriver> fluentWaitForClassificationSelect = new FluentWait<>(driver)
				.withTimeout(30, TimeUnit.SECONDS).pollingEvery(200, TimeUnit.MILLISECONDS)
				.ignoring(NoSuchElementException.class);
		fluentWaitForClassificationSelect
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_field_9445']")));
		fluentWait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//*[@id='tracker_field_9445']/option[@value='" + classification + "']")));
		Select ClassificationSelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_field_9445']")));
		ClassificationSelect.selectByValue(classification);

		/* Setting priority */
		FluentWait<WebDriver> fluentWaitForPrioritySelect = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
				.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
		fluentWaitForPrioritySelect
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_field_11965']")));
		Select prioritySelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_field_11965']")));
		prioritySelect.selectByValue(priority);

		/* Setting severity */
		FluentWait<WebDriver> fluentWaitForServeritySelect = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
				.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
		fluentWaitForServeritySelect
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_field_9444']")));
		Select severitySelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_field_9444']")));
		severitySelect.selectByValue(severity);

		/* Setting Region */
		FluentWait<WebDriver> fluentWaitForRegionSelect = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
				.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
		fluentWaitForRegionSelect
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_field_9448']")));
		Select regionSelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_field_9448']")));
		regionSelect.selectByValue(region);

		/* Setting location */
		FluentWait<WebDriver> fluentWaitForLocationSelect = new FluentWait<>(driver).withTimeout(30, TimeUnit.SECONDS)
				.pollingEvery(200, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);
		fluentWaitForLocationSelect
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tracker_field_9451']")));
		fluentWait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='tracker_field_9451']/option[@value='" + location + "']")));
		Select locationSelect = new Select(driver.findElement(By.xpath("//*[@id='tracker_field_9451']")));
		locationSelect.selectByValue(location);

		/* Setting Extention */
//		WebElement extElement = driver.findElement(
//				By.xpath("//*[@id='wrapper']/div[3]/div/form/div[1]/table[3]/tbody/tr/td[1]/div/div[2]/input"));
		
		WebElement extElement = driver.findElement(
				By.xpath("/html/body/div[1]/div[4]/div/div/div[3]/form/div[1]/table[3]/tbody/tr/td[1]/div/div[2]/input"));
		
		
		extElement.sendKeys(extension);

		/* Setting Subject */
		/*WebElement subjectElement = driver
				.findElement(By.xpath("//*[@id='wrapper']/div[3]/div/form/div[1]/div[2]/input"));*/
		
		WebElement subjectElement = driver
				.findElement(By.xpath("/html/body/div[1]/div[4]/div/div/div[3]/form/div[1]/div[2]/input"));
		
		subjectElement.sendKeys(subject);

		/* Setting Description */
		description = description.replace("\n", Keys.chord(Keys.SHIFT, Keys.ENTER));
		WebElement descriptionElement = driver.findElement(By.xpath("//*[@id='field_9457']"));
		FluentWait<WebDriver> fluentWaitForDescriptionElement = new FluentWait<>(driver)
				.withTimeout(30, TimeUnit.SECONDS).pollingEvery(200, TimeUnit.MILLISECONDS)
				.ignoring(NoSuchElementException.class);
		fluentWaitForDescriptionElement
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='field_9457']")));
		descriptionElement.sendKeys(description);

		/*WebElement submitElement = driver
				.findElement(By.xpath("//*[@id='wrapper']/div[3]/div/form/div[2]/div/button[1]"));*/
		WebElement submitElement = driver
				.findElement(By.xpath("/html/body/div[1]/div[4]/div/div/div[3]/form/div[2]/div/button[1]"));
		
		submitElement.submit();

		return true;
	}

	private Boolean updateTicketId(WebDriver driver, String runNo) {
		logger.info("Processor - GL POST - BEGIN updateTicketId()  called ");
//		List<WebElement> elements = driver
//				.findElements(By.xpath(".//*[@class='widget_content']/table/tbody/tr/td[contains(text(), '" + runNo
//						+ "')]/ancestor::tr/td[2]"));
//		for (WebElement element : elements) {
//			String ticketId = element.getText();
//			GLBatchProcessor glBatchProcessor = batchProcessorService.findByRunNo(runNo);
//			glBatchProcessor.setTicketId(ticketId);
//			batchProcessorService.save(glBatchProcessor);
//		}
		
		WebElement ticketIdelement = waitForElementVisible(driver, By.xpath("//*[@id='feedback']/ul/li/a"), 30);
		String ticketId = ticketIdelement.getText();
		GLBatchProcessor glBatchProcessor = batchProcessorService.findByRunNo(runNo);
		glBatchProcessor.setTicketId(ticketId.split("#")[1]);
		batchProcessorService.save(glBatchProcessor);
		return true;

	}

	// common method
	public WebElement waitForElementVisible(WebDriver driver, final By selector, int timeOutInSeconds) {
		WebElement element = null;
		WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));

		return element;
	}

	public static WebElement waitForElementPresent(WebDriver driver, final By selector, int timeOutInSeconds) {
		WebElement element = null;

		WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
		element = wait.until(ExpectedConditions.presenceOfElementLocated(selector));

		return element;
	}

	public String getCurrentFrameName(WebDriver driver) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		String CurrentFrame = js.executeScript("return self.name").toString();
		return CurrentFrame;
	}
	
	public static String ErrorFileMovementMethod(String BaseFilePath, File file, String NewPath) throws IOException {
		
		logger.info("File FileMovementMethod :::::" + file.getName());
		File Path = new File(BaseFilePath + NewPath);
        if (!Path.exists()) {
			Path.mkdirs();
		}
        File newFile=new File(Path+RPAConstants.SLASH+file.getName());
	   file.renameTo(newFile);
		// FileUtils.moveFileToDirectory(file, Path,true);
		logger.info("File Successfully Moved to the  Location" + newFile.getAbsolutePath());
		return newFile.getAbsolutePath();

	}
}