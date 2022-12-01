/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.processors.exception;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.ErrorInfo;
import com.rpa.model.TransactionInfo;
import com.rpa.service.ErrorInfoService;

public class ClaimsDownloadException implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(ClaimsDownloadException.class.getName());
	
	@Autowired
	ErrorInfoService errorInfoService;

	@Autowired
	ApplicationContext applicationContext;
	
	public void process(Exchange exchange)
			throws URISyntaxException, IOException, InterruptedException, ParseException {

		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		applicationContext = SpringContext.getAppContext();
		errorInfoService = applicationContext.getBean(ErrorInfoService.class);
		
		logger.info("Inside ClaimsDownloadException::Process Phase::" + transactionInfo.getProcessPhase());

		transactionInfo.setTransactionStatus(RPAConstants.Failed);
		if (transactionInfo.getProcessPhase() != null) {
			transactionInfo.setProcessStatus(RPAConstants.Failed);
		}
		
		if(transactionInfo.getId()!=null){
		ErrorInfo errInfo = new ErrorInfo();
		errInfo.setTransactionId(transactionInfo.getId());
		if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT).toString().length() > 1000)
			errInfo.setErrorMessage(exchange.getProperty(Exchange.EXCEPTION_CAUGHT).toString().substring(0, 1000));
		else
			errInfo.setErrorMessage(exchange.getProperty(Exchange.EXCEPTION_CAUGHT).toString());
		errorInfoService.save(errInfo);
		}
	}
}
