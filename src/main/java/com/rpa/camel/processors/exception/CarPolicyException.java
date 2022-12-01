/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.processors.exception;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.ErrorInfo;
import com.rpa.model.TransactionInfo;
import com.rpa.service.ErrorInfoService;
import com.rpa.util.UtilityFile;

public class CarPolicyException implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(CarPolicyException.class.getName());
	
	@Autowired
	ErrorInfoService errorInfoService;

	@Autowired
	ApplicationContext applicationContext;
	
	public void process(Exchange exchange)
			throws URISyntaxException, IOException, InterruptedException, ParseException {

		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		applicationContext = SpringContext.getAppContext();
		errorInfoService = applicationContext.getBean(ErrorInfoService.class);
		
		logger.info("Inside CarPolicyException::Process Phase::" + transactionInfo.getProcessPhase());

		transactionInfo.setTransactionStatus(RPAConstants.Failed);
		if (transactionInfo.getProcessPhase() != null) {
			transactionInfo.setProcessStatus(RPAConstants.Failed);
			if ((RPAConstants.FETCH_NUMBER).equals(transactionInfo.getProcessPhase())) {
				transactionInfo.setProcessFailureReason(RPAConstants.FETCH_NUMBER_ERROR);
			} else if ((RPAConstants.AUTOMATER).equals(transactionInfo.getProcessPhase())) {
				transactionInfo.setProcessFailureReason(RPAConstants.AUTOMATER_ERROR);
			} else if ((RPAConstants.EXTRACTION).equals(transactionInfo.getProcessPhase())) {
				transactionInfo.setProcessFailureReason(RPAConstants.EXTRACTION_ERROR);
			}
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
