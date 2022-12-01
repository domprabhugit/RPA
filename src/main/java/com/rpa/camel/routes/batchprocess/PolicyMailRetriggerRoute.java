
/* * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com*/
 

package com.rpa.camel.routes.batchprocess;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.PolicyPdfExcelUploader;
import com.rpa.camel.processors.exception.FirstgenPolicyDownloadException;
import com.rpa.constants.RPAConstants;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class PolicyMailRetriggerRoute extends RouteBuilder {

	private String routeId = "policyMailRetrigger";
	
	private String routeIdPolicyPDFMailRetrigger = "policyPDFMailRetrigger";
	
	@Override
	public void configure() throws Exception {

	onException(Exception.class).handled(false).process(new FirstgenPolicyDownloadException()).bean(TransactionInfoServiceImpl.class,"updatePolicyPDFRetriggerTransactionInfo");
	
	from("direct:policyPdfMailRetrigger_process")
	.routeId(routeId)
	.routeDescription("Policy PDF Mail ReTrigger - Common")
	.bean(TransactionInfoServiceImpl.class,"insertPolicyPDFRetriggerTransactionInfo")
	.process(new PolicyPdfExcelUploader())
	.bean(TransactionInfoServiceImpl.class,"updatePolicyPDFRetriggerTransactionInfo")
	.end();
	
	from("quartz2:"+routeIdPolicyPDFMailRetrigger+"?stateful=true&cron=0+0+0/1+1/1+*+?+*").autoStartup(false)
	/*from("quartz2:"+routeIdPolicyPDFMailRetrigger+"?stateful=true&cron=0+0/2+7-23+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdPolicyPDFMailRetrigger+"?stateful=true&cron=0+0+2,3+?+*+*+*").autoStartup(true)
	from("quartz2:"+routeIdPolicyPDFMailRetrigger+"?stateful=true&cron=0+0+20,21,2,3+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdPolicyPDFMailRetrigger)
	.routeDescription("Policy PDF Mail Retrigger")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:policyPdfMailRetrigger_process")
	.end();
	

}	


	
	
}
