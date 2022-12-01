/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.routes.batchprocess;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.FirstgenErrorPdfUploader;
import com.rpa.camel.processors.batchprocess.FirstgenPdfUploader;
import com.rpa.camel.processors.exception.FirstgenPolicyDownloadException;
import com.rpa.constants.RPAConstants;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class FirstgenPolicyUploaderRoute extends RouteBuilder {

	private String routeId = "firstgenPolicyUpload";
	private String routeIdFirstgenPolicy = "policyUploadFirstgen";
	private String routeIdFirstgenErrorPolicy = "policyErrorUploadFirstgen";
	
	@Override
	public void configure() throws Exception {

	onException(Exception.class).handled(false).process(new FirstgenPolicyDownloadException()).bean(TransactionInfoServiceImpl.class,"updateFirstgenPolicyDocUploadTransactionInfo").end();
	
	from("direct:firstgen_policyuploader_process")
	.routeId(routeId)
	.routeDescription("FirstGen Policy Uploader ")
	.bean(TransactionInfoServiceImpl.class,"insertFirstgenPolicyDocUploadTransactionInfo")
	.process(new FirstgenPdfUploader())
	.bean(TransactionInfoServiceImpl.class,"updateFirstgenPolicyDocUploadTransactionInfo")
	.end();
	
	from("quartz2:"+routeIdFirstgenPolicy+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdFirstgenPolicy+"?stateful=true&cron=0+0+19-23,4-8+?+*+*+*").autoStartup(false)*/
	/*from("quartz2:"+routeIdFirstgenPolicy+"?stateful=true&cron=0+0+3,4+?+*+*+*").autoStartup(false)*/
	/*from("quartz2:"+routeIdFirstgenPolicy+"?stateful=true&cron=0+0+21,22,3,4+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdFirstgenPolicy)
	.routeDescription("Policy Uploader For FirstGen")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:firstgen_policyuploader_process")
	.end();
	
	/*from("quartz2:"+routeIdFirstgenErrorPolicy+"?stateful=true&cron=0+0/1+9-8+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdFirstgenErrorPolicy+"?stateful=true&cron=0+0+4,5+?+*+*+*").autoStartup(false)
	.routeId(routeIdFirstgenErrorPolicy)
	.routeDescription("Policy Error Uploader For FirstGen")
	.bean(TransactionInfoServiceImpl.class,"insertFirstgenPolicyDocUploadTransactionInfo")
	.process(new FirstgenErrorPdfUploader())
	.bean(TransactionInfoServiceImpl.class,"updateFirstgenPolicyDocUploadTransactionInfo")
	.end();
	

}	


}
