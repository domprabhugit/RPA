/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.routes.batchprocess;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.FirstgenApplicationAutomater;
import com.rpa.camel.processors.batchprocess.FirstgenNumberFetcher;
import com.rpa.camel.processors.batchprocess.FirstgenPdfExtracter;
import com.rpa.camel.processors.exception.FirstgenPolicyDownloadException;
import com.rpa.constants.RPAConstants;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class FirstgenPolicyExtracterRoute extends RouteBuilder {

	private String routeId = "firstgenPolicyExtracter";
	private String routeIdFirstgenPolicy = "PolicyExtracterFirstgen";
	
	@Override
	public void configure() throws Exception {

	onException(Exception.class).handled(false).process(new FirstgenPolicyDownloadException()).bean(TransactionInfoServiceImpl.class,"updateFirstgenPolicyExtractionTransactionInfo");
	
	from("direct:firstgenpolicyextractor_process")
	.routeId(routeId)
	.routeDescription("Firstgen Policy Extractor - Common")
	.bean(TransactionInfoServiceImpl.class,"insertFirstgenPolicyExtractionTransactionInfo")
	.process(new FirstgenNumberFetcher())
	.process(new FirstgenApplicationAutomater())
	.process(new FirstgenPdfExtracter())
	.bean(TransactionInfoServiceImpl.class,"updateFirstgenPolicyExtractionTransactionInfo")
	.end();
	
	from("quartz2:"+routeIdFirstgenPolicy+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdFirstgenPolicy+"?stateful=true&cron=0+0+19-23,4-7+?+*+*+*").autoStartup(false)*/
	/*from("quartz2:"+routeIdFirstgenPolicy+"?stateful=true&cron=0+0+2,3+?+*+*+*").autoStartup(true)*/
	/*from("quartz2:"+routeIdFirstgenPolicy+"?stateful=true&cron=0+0+20,21,2,3+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdFirstgenPolicy)
	.routeDescription("Firstgen Policy Extractor")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:firstgenpolicyextractor_process")
	.end();
	

}	


	
	
}
