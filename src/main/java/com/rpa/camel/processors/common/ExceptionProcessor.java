/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.processors.common;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(ExceptionProcessor.class.getName());

	public void process(Exchange exchange) throws IOException, InvalidFormatException, URISyntaxException{

		if(exchange!=null && exchange.getException()!=null){
		logger.info("Caught Exception::"+exchange.getException().getMessage());
		}
	}
}