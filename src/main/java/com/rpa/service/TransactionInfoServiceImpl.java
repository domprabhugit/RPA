package com.rpa.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

import org.apache.camel.Exchange;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rpa.constants.ErrorConstants;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.repository.TransactionInfoRepository;
import com.rpa.util.UtilityFile;

@Service
public class TransactionInfoServiceImpl implements TransactionInfoService {

	private static final Logger logger = LoggerFactory.getLogger(TransactionInfoServiceImpl.class.getName());

	@Autowired
	private TransactionInfoRepository transactionInfoRepository;

	@Override
	public List<TransactionInfo> filterTransactionDetails(String startDate, String endDate, String processName)
			throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SSS");
		Date parsedStartDate = (Date) dateFormat.parse(startDate + " 00:00:00:000");
		Date parsedEndDate = (Date) dateFormat.parse(endDate + " 23:59:59:999");

		Timestamp timestampStartDate = new java.sql.Timestamp(parsedStartDate.getTime());
		Timestamp timestampEndDate = new java.sql.Timestamp(parsedEndDate.getTime());
		List<TransactionInfo> list = transactionInfoRepository.filterTransactionDetails(timestampStartDate,
				timestampEndDate, processName);
		String timeStamp = "";
		for (TransactionInfo transaction : list) {
			Date date = new Date();
			if (transaction.getTransactionStartDate() != null) {
				date.setTime(transaction.getTransactionStartDate().getTime());
				timeStamp = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SSS").format(date);
				transaction.setStartDate(timeStamp);
			}
			if (transaction.getTransactionEndDate() != null) {
				date.setTime(transaction.getTransactionEndDate().getTime());
				timeStamp = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SSS").format(date);
				transaction.setEndDate(timeStamp);
			}
		}
		return list;
	}

	public void insertVB64TransactionInfo(Exchange exchange) {

		logger.info("insertIntoTransactionInfo - File Path::" + exchange.getIn().getBody());

		String filePath = exchange.getIn().getBody().toString();

		//TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		TransactionInfo transactionInfo =  new TransactionInfo(); 
		transactionInfo.setProcessStatus(RPAConstants.Initialize);
		transactionInfo.setProcessFailureReason(ErrorConstants.ERROR_CODE_00013);
		
		if(exchange.getProperty("NO_FILE").equals(RPAConstants.Y)){
			transactionInfo.setProcessStatus(RPAConstants.Failed);
			transactionInfo.setProcessFailureReason(ErrorConstants.ERROR_CODE_0007);
		}

		if (filePath != null && !filePath.isEmpty()) {
			transactionInfo.setInputFilePath_1(filePath);
		}
		transactionInfo.setProcessName(exchange.getFromRouteId().toString());
		transactionInfo.setTransactionStatus(RPAConstants.InProgress);
		transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
		transactionInfo.setCreatedBy(RPAConstants.ADMIN);

		transactionInfoRepository.save(transactionInfo);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		logger.info("insertIntoTransactionInfo - Transaction ID::" + transactionInfo.getId());
	}

	public void updateVB64TransactionInfo(Exchange exchange)
			throws IOException, InvalidFormatException, URISyntaxException {

		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
		transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
		transactionInfo.setTransactionStatus(RPAConstants.Completed);
		transactionInfoRepository.save(transactionInfo);

		logger.info("updateVB64TransactionInfo - Transaction ID::" + transactionInfo.getId());
	}

	public void insertTransactionInfo(Exchange exchange)
			throws IOException, InvalidFormatException, URISyntaxException {

		TransactionInfo transactionInfo = new TransactionInfo();
		transactionInfo.setProcessName(exchange.getFromRouteId().toString());
		transactionInfo.setTransactionStatus(RPAConstants.InProgress);
		transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
		System.out.println("setTransactionStartDate::" + transactionInfo.getTransactionStartDate());

		transactionInfoRepository.save(transactionInfo);
		exchange.setProperty("TRANSACTION_INFO_REQ", transactionInfo);

		logger.info("insertIntoTransactionInfo - Transaction ID::" + transactionInfo.getId());
	}

	public void updateFirstGenTransactionInfo(Exchange exchange)
			throws IOException, InvalidFormatException, URISyntaxException {

		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty("TRANSACTION_INFO_REQ");
		transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
		transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
		WebDriver chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver");
		logger.info("updateLifelineUploadTransactionInfo - chromeDriver object ::" + chromeDriver);
		if (chromeDriver != null) {
			chromeDriver.close();
			chromeDriver.quit();
			logger.info("updateLifelineUploadTransactionInfo - chromeDriver's quit method called ::");
		}
		transactionInfo.setTransactionStatus(RPAConstants.Completed);
		transactionInfo.setRunNo("");
		transactionInfoRepository.save(transactionInfo);

		logger.info("updateLifelineUploadTransactionInfo - Transaction ID::" + transactionInfo.getId());
	}

	public boolean inactiveOldTransactions(Exchange exchange, Date countDate) {

		List<TransactionInfo> transactionInfoList = transactionInfoRepository.findByMigrationDate(countDate);
		for (TransactionInfo transactionInfo : transactionInfoList) {
			transactionInfo.setStatus(RPAConstants.N);
			transactionInfoRepository.save(transactionInfo);
		}
		logger.info(
				"inactiveOldTransactions - Transactions before updation of migrated data was made inactive for the date ::"
						+ countDate);
		return true;
	}

	@Override
	public TransactionInfo findById(Long Id) {
		return transactionInfoRepository.findById(Id);
	}

	public void insertMigrationTransactionInfo(Exchange exchange) {

		TransactionInfo transactionInfo = new TransactionInfo();
		transactionInfo.setProcessName(exchange.getFromRouteId().toString());
		transactionInfo.setTransactionStatus(RPAConstants.InProgress);
		transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
		transactionInfo.setCreatedBy(RPAConstants.ADMIN);
		transactionInfo.setStatus(RPAConstants.Y);

		transactionInfoRepository.save(transactionInfo);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("insertMigrationTransactionInfo - Transaction ID::" + transactionInfo.getId());
	}

	public void updateMigrationTransactionInfo(Exchange exchange)
			throws IOException, InvalidFormatException, URISyntaxException {

		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
		transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
		transactionInfo.setTransactionStatus(RPAConstants.Completed);
		transactionInfoRepository.save(transactionInfo);

		logger.info("updateMigrationTransactionInfo - Transaction ID::" + transactionInfo.getId());
	}

	public void insertLifelineUploadTransactionInfo(Exchange exchange) {

		TransactionInfo transactionInfo = new TransactionInfo();
		transactionInfo.setProcessName(exchange.getFromRouteId().toString());
		transactionInfo.setTransactionStatus(RPAConstants.InProgress);
		transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
		transactionInfo.setCreatedBy(RPAConstants.ADMIN);
		transactionInfo.setStatus(RPAConstants.Y);

		transactionInfoRepository.save(transactionInfo);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("insertLifelineUploadTransactionInfo - Transaction ID::" + transactionInfo.getId());
	}

	public void updateLifelineUploadTransactionInfo(boolean isTransactionCompleted, Exchange exchange)
			throws IOException, InvalidFormatException, URISyntaxException, MessagingException {

		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
		if (isTransactionCompleted) {
			Message message = (Message) exchange.getIn().getHeader("messageObject");
			Folder emailFolder = (Folder) exchange.getIn().getHeader("emailFolderObject");
			Store store = (Store) exchange.getIn().getHeader("storeObject");
			if (message != null)
				message.setFlag(Flags.Flag.DELETED, true);
			if (emailFolder != null)
				emailFolder.close(true);
			if (store != null)
				store.close();
			transactionInfo.setTransactionStatus(RPAConstants.Completed);
			transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
		} else {
			transactionInfo.setTransactionStatus(RPAConstants.InProgress);
		}
		transactionInfoRepository.save(transactionInfo);

		logger.info("updateLifelineUploadTransactionInfo - Transaction ID::" + transactionInfo.getId());
	}
	
	public void insertCarPolicyExtractionTransactionInfo(Exchange exchange) {

		TransactionInfo transactionInfo = new TransactionInfo();
		transactionInfo.setProcessName(exchange.getFromRouteId().toString());
		transactionInfo.setTransactionStatus(RPAConstants.InProgress);
		transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
		transactionInfo.setCreatedBy(RPAConstants.ADMIN);
		transactionInfo.setStatus(RPAConstants.Y);

		transactionInfoRepository.save(transactionInfo);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("insertCarPolicyExtractionTransactionInfo - Transaction ID::"+transactionInfo.getId());
	}
	
	public void updateCarPolicyExtractionTransactionInfo(Exchange exchange) throws IOException, InvalidFormatException, URISyntaxException{

		WebDriver chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver_maruti");
		logger.info("updateCarPolicyExtractionTransactionInfo - chromeDriver object ::" + chromeDriver);
		if (chromeDriver != null) {
			if(exchange.getProperty("IS_LOGGED_OUT")!=null && exchange.getProperty("IS_LOGGED_IN")!=null && exchange.getProperty("IS_LOGGED_IN").equals(1) && exchange.getProperty("IS_LOGGED_OUT").equals(0)){
				
				ArrayList<String> tabs = new ArrayList<String>(chromeDriver.getWindowHandles());
				chromeDriver.switchTo().window(tabs.get(0));
				
				WebElement logoutMaruti = UtilityFile.waitForElementPresent(chromeDriver,
						By.xpath("//*[@id='imgNavSignOut']"), 40);
				logoutMaruti.click();
			}
			
			//chromeDriver.close();
			for (String handle : chromeDriver.getWindowHandles()) {
				chromeDriver.switchTo().window(handle);
				chromeDriver.close();
			}
			chromeDriver.quit();
			logger.info("updateCarPolicyExtractionTransactionInfo - chromeDriver's quit method called ::");
			
		}else{
			if(exchange.getFromRouteId().contains("Honda"))
				chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver_honda");
			else if(exchange.getFromRouteId().contains("Ford")){
				chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver_ford");
				WebDriver chromeFordDriverDownload = (WebDriver) exchange.getIn().getHeader("chromeDriver_ford_fileDownload");
				if(chromeFordDriverDownload!=null){
					for (String handle : chromeFordDriverDownload.getWindowHandles()) {
						chromeFordDriverDownload.switchTo().window(handle);
						chromeFordDriverDownload.close();
					}
					chromeFordDriverDownload.quit();
					logger.info("updateCarPolicyExtractionTransactionInfo - closed ford chrome download Driver "+exchange.getFromRouteId()+" object ::" + chromeFordDriverDownload);
				}
			}
			else if(exchange.getFromRouteId().contains("TataCV")){
				chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver_tatacv");
				WebDriver chromeTataCvDriverDownload = (WebDriver) exchange.getIn().getHeader("chromeDriver_tatacv_fileDownload");
				if(chromeTataCvDriverDownload!=null){
					//chromeTataCvDriverDownload.close();
					for (String handle : chromeTataCvDriverDownload.getWindowHandles()) {
						chromeTataCvDriverDownload.switchTo().window(handle);
						chromeTataCvDriverDownload.close();
					}
					chromeTataCvDriverDownload.quit();
					logger.info("updateCarPolicyExtractionTransactionInfo - closed tatapv chrome download Driver "+exchange.getFromRouteId()+" object ::" + chromeTataCvDriverDownload);
				}
				
			}else if(exchange.getFromRouteId().contains("TataPV")){
				chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver_tatapv");
				WebDriver chromeTataPvDriverDownload = (WebDriver) exchange.getIn().getHeader("chromeDriver_tatapv_fileDownload");
				if(chromeTataPvDriverDownload!=null){
					//chromeTataPvDriverDownload.close();
					for (String handle : chromeTataPvDriverDownload.getWindowHandles()) {
						chromeTataPvDriverDownload.switchTo().window(handle);
						chromeTataPvDriverDownload.close();
					}
					chromeTataPvDriverDownload.quit();
					logger.info("updateCarPolicyExtractionTransactionInfo - closed tatapv chrome download Driver "+exchange.getFromRouteId()+" object ::" + chromeTataPvDriverDownload);
				}
			}else if(exchange.getFromRouteId().contains("Abibl")){
				chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver_abibl");
				WebDriver chromeAbiblDriverDownload = (WebDriver) exchange.getIn().getHeader("chromeDriver_abibl_fileDownload");
				if(chromeAbiblDriverDownload!=null){
					for (String handle : chromeAbiblDriverDownload.getWindowHandles()) {
						chromeAbiblDriverDownload.switchTo().window(handle);
						chromeAbiblDriverDownload.close();
					}
					chromeAbiblDriverDownload.quit();
					logger.info("updateCarPolicyExtractionTransactionInfo - closed abibl chrome download Driver "+exchange.getFromRouteId()+" object ::" + chromeAbiblDriverDownload);
				}
			}else if(exchange.getFromRouteId().contains("Mibl")){
				chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver_mibl");
				WebDriver chromemiblDriverDownload = (WebDriver) exchange.getIn().getHeader("chromeDriver_mibl_fileDownload");
				if(chromemiblDriverDownload!=null){
					for (String handle : chromemiblDriverDownload.getWindowHandles()) {
						chromemiblDriverDownload.switchTo().window(handle);
						chromemiblDriverDownload.close();
					}
					chromemiblDriverDownload.quit();
					logger.info("updateCarPolicyExtractionTransactionInfo - closed mibl chrome download Driver "+exchange.getFromRouteId()+" object ::" + chromemiblDriverDownload);
				}
			}else if(exchange.getFromRouteId().contains("Volvo")){
				chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver_volvo");
				WebDriver chromevolvoDriverDownload = (WebDriver) exchange.getIn().getHeader("chromeDriver_volvo_fileDownload");
				if(chromevolvoDriverDownload!=null){
					for (String handle : chromevolvoDriverDownload.getWindowHandles()) {
						chromevolvoDriverDownload.switchTo().window(handle);
						chromevolvoDriverDownload.close();
					}
					chromevolvoDriverDownload.quit();
					logger.info("updateCarPolicyExtractionTransactionInfo - closed volvo chrome download Driver "+exchange.getFromRouteId()+" object ::" + chromevolvoDriverDownload);
				}
			}else if(exchange.getFromRouteId().contains("Tafe")){
				chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver_tafe");
				WebDriver chrometafeDriverDownload = (WebDriver) exchange.getIn().getHeader("chromeDriver_volvo_fileDownload");
				if(chrometafeDriverDownload!=null){
					for (String handle : chrometafeDriverDownload.getWindowHandles()) {
						chrometafeDriverDownload.switchTo().window(handle);
						chrometafeDriverDownload.close();
					}
					chrometafeDriverDownload.quit();
					logger.info("updateCarPolicyExtractionTransactionInfo - closed tafe chrome download Driver "+exchange.getFromRouteId()+" object ::" + chrometafeDriverDownload);
				}
			}else if(exchange.getFromRouteId().contains("Piaggio")){
				chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver_piaggio");
				WebDriver chromepiaggioDriverDownload = (WebDriver) exchange.getIn().getHeader("chromeDriver_piaggio_fileDownload");
				if(chromepiaggioDriverDownload!=null){
					for (String handle : chromepiaggioDriverDownload.getWindowHandles()) {
						chromepiaggioDriverDownload.switchTo().window(handle);
						chromepiaggioDriverDownload.close();
					}
					chromepiaggioDriverDownload.quit();
					logger.info("updateCarPolicyExtractionTransactionInfo - closed piaggio chrome download Driver "+exchange.getFromRouteId()+" object ::" + chromepiaggioDriverDownload);
				}
			}
			
			logger.info("updateCarPolicyExtractionTransactionInfo - chromeDriver "+exchange.getFromRouteId()+" object ::" + chromeDriver);
			if (chromeDriver != null) {
				//chromeDriver.close();
				for (String handle : chromeDriver.getWindowHandles()) {
					chromeDriver.switchTo().window(handle);
					chromeDriver.close();
				}
				chromeDriver.quit();
				logger.info("updateCarPolicyExtractionTransactionInfo - chromeDriver's quit method called ::");
			}
		}
		
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);	
		transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
		transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
		if(transactionInfo.getStatus()!=null && transactionInfo.getStatus().equals("D")){
			transactionInfo.setProcessPhase(RPAConstants.AUTOMATER);
			transactionInfo.setTransactionStatus("Credential Expired");	
			transactionInfo.setProcessStatus("Failed");
		}else{
			transactionInfo.setTransactionStatus(RPAConstants.Completed);
		}
		transactionInfoRepository.save(transactionInfo);
		logger.info("updateCarPolicyExtractionTransactionInfo - Transaction ID::"+transactionInfo.getId());
	}
	
	
	public void insertGridTransactionInfo(Exchange exchange)
			throws IOException, InvalidFormatException, URISyntaxException {

		TransactionInfo transactionInfo = new TransactionInfo();
		transactionInfo.setProcessName(exchange.getFromRouteId().toString());
		transactionInfo.setTransactionStatus(RPAConstants.InProgress);
		transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
		//System.out.println("setTransactionStartDate::" + transactionInfo.getTransactionStartDate());

		 transactionInfoRepository.save(transactionInfo);
		exchange.setProperty("TRANSACTION_INFO_REQ", transactionInfo);

		logger.info("insertGridTransactionInfo - Transaction ID::" + transactionInfo.getId());
	}
	
	
		public void updateGridTransactionInfo(Exchange exchange)
			throws IOException, InvalidFormatException, URISyntaxException {

		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
		
		if(!transactionInfo.getTransactionStatus().equalsIgnoreCase("Error")){
			if(transactionInfo.getProcessStatus().equalsIgnoreCase(RPAConstants.Failed)){
				transactionInfo.setTransactionStatus(RPAConstants.Failed);
			}else if(!transactionInfo.getTransactionStatus().equalsIgnoreCase("No File") ){
				transactionInfo.setTransactionStatus(transactionInfo.getProcessPhase().equalsIgnoreCase(RPAConstants.GRID_VALIDATOR_PHASE)?RPAConstants.VALIDATION:RPAConstants.Completed);
			}
		}
		
		if(transactionInfo.getTransactionStatus().equalsIgnoreCase(RPAConstants.Failed) || transactionInfo.getTransactionStatus().equalsIgnoreCase(RPAConstants.Completed) ||
				transactionInfo.getTransactionStatus().equalsIgnoreCase("No File") || transactionInfo.getTransactionStatus().equalsIgnoreCase("Error")){
			transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
		}
		transactionInfoRepository.save(transactionInfo);
        logger.info("updateGridTransactionInfo - Transaction ID::" + transactionInfo.getId());
	}	
	
		public void insertOmniDocUploadTransactionInfo(Exchange exchange){
			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.setProcessName(exchange.getFromRouteId().toString());
			transactionInfo.setTransactionStatus(RPAConstants.InProgress);
			transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setCreatedBy(RPAConstants.ADMIN);
			transactionInfo.setStatus(RPAConstants.Y);

			transactionInfoRepository.save(transactionInfo);
			exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

			logger.info("insertOmniDocUploadTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		public void updateOmniDocUploadTransactionInfo(Exchange exchange) throws IOException, InvalidFormatException, URISyntaxException{

			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);	
			transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
			transactionInfo.setTransactionStatus(RPAConstants.Completed);
			transactionInfoRepository.save(transactionInfo);
			logger.info("updateOmniDocUploadTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		
		public void insertClaimsDownloadTransactionInfo(Exchange exchange)
				throws IOException, InvalidFormatException, URISyntaxException {

			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.setProcessName(exchange.getFromRouteId().toString());
			transactionInfo.setTransactionStatus(RPAConstants.InProgress);
			transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
			System.out.println("setTransactionStartDate::" + transactionInfo.getTransactionStartDate());

			transactionInfoRepository.save(transactionInfo);
			exchange.setProperty("TRANSACTION_INFO", transactionInfo);

			logger.info("insertClaimsDownloadTransactionInfo - Transaction ID::" + transactionInfo.getId());
		}
		
		public void updateClaimsDownloadTransactionInfo(Exchange exchange)
				throws IOException, InvalidFormatException, URISyntaxException {

			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty("TRANSACTION_INFO");
			transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
			WebDriver chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver_claimsdownload");
			logger.info("updateClaimsDownloadTransactionInfo - chromeDriver object ::" + chromeDriver);
			if (chromeDriver != null) {
				chromeDriver.close();
				chromeDriver.quit();
				logger.info("updateClaimsDownloadTransactionInfo - chromeDriver's quit method called ::");
			}
			transactionInfo.setTransactionStatus(RPAConstants.Completed);
			transactionInfoRepository.save(transactionInfo);

			logger.info("updateClaimsDownloadTransactionInfo - Transaction ID::" + transactionInfo.getId());
		}

		public void insertModelCreationTransactionInfo(Exchange exchange)
				throws IOException, InvalidFormatException, URISyntaxException {

			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.setProcessName(exchange.getFromRouteId().toString());
			transactionInfo.setTransactionStatus(RPAConstants.InProgress);
			transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
			//System.out.println("setTransactionStartDate::" + transactionInfo.getTransactionStartDate());

			 transactionInfoRepository.save(transactionInfo);
			exchange.setProperty("TRANSACTION_INFO_REQ", transactionInfo);

			logger.info("insertModelCreationTransactionInfo - Transaction ID::" + transactionInfo.getId());
		}
		
		
			public void updateModelCreationTransactionInfo(Exchange exchange)
				throws IOException, InvalidFormatException, URISyntaxException {

			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
			transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
			/*if(!transactionInfo.getProcessPhase().equalsIgnoreCase(RPAConstants.GRID_READER_PHASE))*/
			if(transactionInfo.getProcessStatus().equalsIgnoreCase(RPAConstants.Failed)){
				transactionInfo.setTransactionStatus(RPAConstants.Failed);
			}else if(!transactionInfo.getTransactionStatus().equalsIgnoreCase("No File")){
				transactionInfo.setTransactionStatus(transactionInfo.getProcessPhase().equalsIgnoreCase(RPAConstants.MODEL_VALIDATOR_PHASE)?RPAConstants.VALIDATION:RPAConstants.Completed);
			}
			if(transactionInfo.getTransactionStatus().equalsIgnoreCase(RPAConstants.Failed) || transactionInfo.getTransactionStatus().equalsIgnoreCase(RPAConstants.Completed) ||
					transactionInfo.getTransactionStatus().equalsIgnoreCase("No File")){
				transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
			}
			transactionInfoRepository.save(transactionInfo);
	        logger.info("updateModelCreationTransactionInfo - Transaction ID::" + transactionInfo.getId());
		}
			
		public void insertSecuredFileTransferTransactionInfo(Exchange exchange){
			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.setProcessName(exchange.getFromRouteId().toString());
			transactionInfo.setTransactionStatus(RPAConstants.InProgress);
			transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setCreatedBy(RPAConstants.ADMIN);
			transactionInfo.setStatus(RPAConstants.Y);

			transactionInfoRepository.save(transactionInfo);
			exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

			logger.info("insertSecuredFileTransferTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		public void updateSecuredFileTransferTransactionInfo(Exchange exchange) throws IOException, InvalidFormatException, URISyntaxException{

			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);	
			transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
			transactionInfo.setTransactionStatus(RPAConstants.Completed);
			transactionInfoRepository.save(transactionInfo);
			logger.info("updateSecuredFileTransferTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		public void insertFirstgenPolicyDocUploadTransactionInfo(Exchange exchange){
			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.setProcessName(exchange.getFromRouteId().toString());
			transactionInfo.setTransactionStatus(RPAConstants.InProgress);
			transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setCreatedBy(RPAConstants.ADMIN);
			transactionInfo.setStatus(RPAConstants.Y);

			transactionInfoRepository.save(transactionInfo);
			exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

			logger.info("insertFirstgenPolicyDocUploadTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		public void updateFirstgenPolicyDocUploadTransactionInfo(Exchange exchange) throws IOException, InvalidFormatException, URISyntaxException{

			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);	
			transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
			transactionInfo.setTransactionStatus(RPAConstants.Completed);
			transactionInfoRepository.save(transactionInfo);
			logger.info("updateFirstgenPolicyDocUploadTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		public void insertFirstgenPolicyExtractionTransactionInfo(Exchange exchange) {

			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.setProcessName(exchange.getFromRouteId().toString());
			transactionInfo.setTransactionStatus(RPAConstants.InProgress);
			transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setCreatedBy(RPAConstants.ADMIN);
			transactionInfo.setStatus(RPAConstants.Y);

			transactionInfoRepository.save(transactionInfo);
			exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

			logger.info("insertFirstgenPolicyExtractionTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		public void updateFirstgenPolicyExtractionTransactionInfo(Exchange exchange) throws IOException, InvalidFormatException, URISyntaxException{

			WebDriver chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver_firstgen");
			logger.info("updateFirstgenPolicyExtractionTransactionInfo - chromeDriver object ::" + chromeDriver);
			if (chromeDriver != null) {
				//chromeDriver.close();
				for (String handle : chromeDriver.getWindowHandles()) {
					chromeDriver.switchTo().window(handle);
					chromeDriver.close();
				}
				chromeDriver.quit();
				logger.info("updateFirstgenPolicyExtractionTransactionInfo - chromeDriver's quit method called ::");
			}
			
			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);	
			transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
			transactionInfo.setTransactionStatus(RPAConstants.Completed);
			transactionInfoRepository.save(transactionInfo);
			logger.info("updateFirstgenPolicyExtractionTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		
		public void insertPolicyPDFRetriggerTransactionInfo(Exchange exchange) {

			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.setProcessName(exchange.getFromRouteId().toString());
			transactionInfo.setTransactionStatus(RPAConstants.InProgress);
			transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setCreatedBy(RPAConstants.ADMIN);
			transactionInfo.setStatus(RPAConstants.Y);

			transactionInfoRepository.save(transactionInfo);
			exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

			logger.info("insertPolicyPDFRetriggerTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		public void updatePolicyPDFRetriggerTransactionInfo(Exchange exchange) throws IOException, InvalidFormatException, URISyntaxException{

			WebDriver chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver_Dtc");
			logger.info("updatePolicyPDFRetriggerTransactionInfo - chromeDriver object ::" + chromeDriver);
			if (chromeDriver != null) {
				//chromeDriver.close();
				for (String handle : chromeDriver.getWindowHandles()) {
					chromeDriver.switchTo().window(handle);
					chromeDriver.close();
				}
				chromeDriver.quit();
				logger.info("updatePolicyPDFRetriggerTransactionInfo - chromeDriver's quit method called ::");
			}
			
			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);	
			transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
			if(!transactionInfo.getTransactionStatus().equals("No Quote"))
				transactionInfo.setTransactionStatus(RPAConstants.Completed);
			transactionInfoRepository.save(transactionInfo);
			logger.info("updatePolicyPDFRetriggerTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		
		public void insertVIRPolicyExtractionTransactionInfo(Exchange exchange) {

			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.setProcessName(exchange.getFromRouteId().toString());
			transactionInfo.setTransactionStatus(RPAConstants.InProgress);
			transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setCreatedBy(RPAConstants.ADMIN);
			transactionInfo.setStatus(RPAConstants.Y);

			transactionInfoRepository.save(transactionInfo);
			exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

			logger.info("insertVIRPolicyExtractionTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		public void updateVIRPolicyExtractionTransactionInfo(Exchange exchange) throws IOException, InvalidFormatException, URISyntaxException{

			WebDriver chromeDriver = (WebDriver) exchange.getIn().getHeader("chromeDriver_autoInspekt");
			logger.info("updateVIRPolicyExtractionTransactionInfo - chromeDriver object ::" + chromeDriver);
			if (chromeDriver != null) {
				
				
				//chromeDriver.close();
				for (String handle : chromeDriver.getWindowHandles()) {
					chromeDriver.switchTo().window(handle);
					chromeDriver.close();
				}
				chromeDriver.quit();
				logger.info("updateCarPolicyExtractionTransactionInfo - chromeDriver's quit method called ::");
				
			}
			
			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);	
			transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
			if(transactionInfo.getStatus()!=null && transactionInfo.getStatus().equals("D")){
				transactionInfo.setProcessPhase(RPAConstants.AUTOMATER);
				transactionInfo.setTransactionStatus("Credential Expired");	
				transactionInfo.setProcessStatus("Failed");
			}else{
				transactionInfo.setTransactionStatus(RPAConstants.Completed);
			}
			transactionInfoRepository.save(transactionInfo);
			logger.info("updateVIRPolicyExtractionTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		
		public void insertVirDocUploadTransactionInfo(Exchange exchange){
			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.setProcessName(exchange.getFromRouteId().toString());
			transactionInfo.setTransactionStatus(RPAConstants.InProgress);
			transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setCreatedBy(RPAConstants.ADMIN);
			transactionInfo.setStatus(RPAConstants.Y);

			transactionInfoRepository.save(transactionInfo);
			exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

			logger.info("insertVirDocUploadTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		public void updateVirDocUploadTransactionInfo(Exchange exchange) throws IOException, InvalidFormatException, URISyntaxException{

			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);	
			transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
			transactionInfo.setTransactionStatus(RPAConstants.Completed);
			transactionInfoRepository.save(transactionInfo);
			logger.info("updateVirDocUploadTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		public void insertAdhocSMSTransactionInfo(Exchange exchange){
			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.setProcessName(exchange.getFromRouteId().toString());
			transactionInfo.setTransactionStatus(RPAConstants.InProgress);
			transactionInfo.setTransactionStartDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setCreatedBy(RPAConstants.ADMIN);
			transactionInfo.setStatus(RPAConstants.Y);

			transactionInfoRepository.save(transactionInfo);
			exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

			logger.info("insertAdhocSMSTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
		
		public void updateAdhocSMSTransactionInfo(Exchange exchange) throws IOException, InvalidFormatException, URISyntaxException{

			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);	
			transactionInfo.setTransactionEndDate(new Timestamp(System.currentTimeMillis()));
			transactionInfo.setUpdatedBy(RPAConstants.ADMIN);
			transactionInfo.setTransactionStatus(RPAConstants.Completed);
			transactionInfoRepository.save(transactionInfo);
			logger.info("updateAdhocSMSTransactionInfo - Transaction ID::"+transactionInfo.getId());
		}
}
