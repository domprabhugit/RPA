/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.DailyFileTransferStatus;
import com.rpa.service.CommonService;
import com.rpa.util.UtilityFile;

public class SecuredFileTransfer implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(SecuredFileTransfer.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	CommonService commonService;
	
	@Autowired
	WebDriver driver;

	@Override
	public void process(Exchange exchange)
			throws Exception {
		logger.info("*********** inside Camel Process of SecuredFileTransfer Called ************");
		logger.info("BEGIN : SecuredFileTransfer Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.FILE_TRANSFER);
		SecuredFileTransfer securedFileTransfer = new SecuredFileTransfer();
		commonService = applicationContext.getBean(CommonService.class);
		securedFileTransfer.doProcess(securedFileTransfer, exchange, transactionInfo, driver,commonService);
		logger.info("*********** inside Camel Process of securedFileTransfer Processor Ended ************");
	}

	public void doProcess(SecuredFileTransfer securedFileTransfer, Exchange exchange,
			TransactionInfo transactionInfo, WebDriver driver,CommonService commonService)
			throws Exception {
		logger.info("BEGIN : securedFileTransfer Processor - doProcess Method Called  ");
		String extractedFilePrefix = "",renewedFilePrefix="";
		
		DailyFileTransferStatus dailyFileTransferStatus =  commonService.getDailyTransferStatus(UtilityFile.createSpecifiedDateFormat(RPAConstants.yyyy_MM_dd));
		
		
		/*SBS*/
		if(dailyFileTransferStatus.getIsSbsExtractionTransferred().equalsIgnoreCase(RPAConstants.N) ||
				dailyFileTransferStatus.getIsSbsRenewalTransferred().equalsIgnoreCase(RPAConstants.N) ){
		extractedFilePrefix="PREVIOUS_DAY_EXTRACTED_"+UtilityFile.createSpecifiedDateFormat(RPAConstants.yyyy_MM_dd)+".xls";
		renewedFilePrefix="PREVIOUS_DAY_RENEWED_"+UtilityFile.createSpecifiedDateFormat(RPAConstants.yyyy_MM_dd)+".xls";
		commonService.transferFileBewtweenServers(transactionInfo,RPAConstants.SBS, extractedFilePrefix,renewedFilePrefix, exchange, dailyFileTransferStatus);
		}
		
		/*SF*/
		/*extractedFilePrefix="SF_PREVIOUS_DAY_EXTRACTED_"+UtilityFile.createSpecifiedDateFormat(RPAConstants.yyyy_MM_dd);
		renewedFilePrefix="SF_PREVIOUS_DAY_RENEWED_"+UtilityFile.createSpecifiedDateFormat(RPAConstants.yyyy_MM_dd);
		commonService.transferFileBewtweenServers(transactionInfo,RPAConstants.SF, extractedFilePrefix,renewedFilePrefix, exchange, dailyFileTransferStatus);*/
		
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		
		logger.info("BEGIN : securedFileTransfer Processor - doProcess Method Ended  ");
	}

}
