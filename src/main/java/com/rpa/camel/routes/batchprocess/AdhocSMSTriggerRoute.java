/*package com.rpa.camel.routes.batchprocess;

 *//** Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com*//*

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.AdhocSMSTrigger;
import com.rpa.camel.processors.exception.SmsTriggerException;
import com.rpa.constants.RPAConstants;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class AdhocSMSTriggerRoute extends RouteBuilder {

	private String routeId = "smsTrigger";
	
	private String routeIdAdhocSmsTrigger = "adhocSmsTrigger";
	
	@Override
	public void configure() throws Exception {

	onException(Exception.class).handled(false).process(new SmsTriggerException()).bean(TransactionInfoServiceImpl.class,"updatePolicyPDFRetriggerTransactionInfo");
	
	from("direct:adhocsmstrigger_process")
	.routeId(routeId)
	.routeDescription("Adhoc SMS Trigger - Common")
	.bean(TransactionInfoServiceImpl.class,"insertAdhocSMSTransactionInfo")
	.process(new AdhocSMSTrigger())
	.bean(TransactionInfoServiceImpl.class,"updateAdhocSMSTransactionInfo")
	.end();
	
	from("quartz2:"+routeIdAdhocSmsTrigger+"?stateful=true&cron=0+0/2+3-2+?+*+*+*").autoStartup(false)
	.routeId(routeIdAdhocSmsTrigger)
	.routeDescription("Adhoc SMS Trigger")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:adhocsmstrigger_process")
	.end();
	

}	


	
	
}
*/