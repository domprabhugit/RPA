/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
/*
package com.rpa.camel.routes.batchprocess;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.DTCPolicyExtracter;
import com.rpa.camel.processors.batchprocess.FirstgenApplicationAutomater;
import com.rpa.camel.processors.batchprocess.FirstgenNumberFetcher;
import com.rpa.camel.processors.batchprocess.FirstgenPdfExtracter;
import com.rpa.camel.processors.exception.FirstgenPolicyDownloadException;
import com.rpa.constants.RPAConstants;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class DToCPolicyExtracterRoute extends RouteBuilder {

	private String routeId = "dtcPolicyExtracter";
	private String routeIdDtccPolicy = "PolicyExtracterDTC";
	
	@Override
	public void configure() throws Exception {

	onException(Exception.class).handled(false).process(new FirstgenPolicyDownloadException()).bean(TransactionInfoServiceImpl.class,"updateFirstgenPolicyExtractionTransactionInfo");
	
	from("direct:dtcgenpolicyextractor_process")
	.routeId(routeId)
	.routeDescription("DTC Policy Extractor - Common")
	.bean(TransactionInfoServiceImpl.class,"insertFirstgenPolicyExtractionTransactionInfo")
	.process(new DTCPolicyExtracter())
	.bean(TransactionInfoServiceImpl.class,"updateFirstgenPolicyExtractionTransactionInfo")
	.end();
	
	from("quartz2:"+routeIdDtccPolicy+"?stateful=true&cron=0+0/5+10-23+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdFirstgenPolicy+"?stateful=true&cron=0+0+19-23,4-7+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdFirstgenPolicy+"?stateful=true&cron=0+0+2,3+?+*+*+*").autoStartup(true)
	from("quartz2:"+routeIdFirstgenPolicy+"?stateful=true&cron=0+0+20,21,2,3+?+*+*+*").autoStartup(false)
	.routeId(routeIdDtccPolicy)
	.routeDescription("DTC Policy Extractor")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:dtcgenpolicyextractor_process")
	.end();
	

}	


	
	
}
*/