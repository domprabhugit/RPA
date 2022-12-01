/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 

package com.rpa.camel.routes.batchprocess;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.AdroitDocUploader;
import com.rpa.camel.processors.batchprocess.AutoInspektDocUploader;
import com.rpa.camel.processors.batchprocess.VirDocUploader;
import com.rpa.camel.processors.exception.FirstgenPolicyDownloadException;
import com.rpa.constants.RPAConstants;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class VIRDocumentUploaderRoute extends RouteBuilder {

	private String routeId = "virDocUpload";
	private String routeIdAutoInspektVirPolicy = "docUploadAutoInspektVir";
	private String routeIdAdroit = "virDocUploadAdroit";
	private String routeIdAdroitVirPolicy = "docUploadAdroitVir";
	private String routeIdVirApp = "virDocUploadVirApp";
	private String routeIdAppVirPolicy = "docUploadVirApp";
	//private String routeIdFirstgenErrorPolicy = "policyErrorUploadFirstgen";
	
	@Override
	public void configure() throws Exception {

	onException(Exception.class).handled(false).process(new FirstgenPolicyDownloadException()).bean(TransactionInfoServiceImpl.class,"updateVirDocUploadTransactionInfo").end();
	
	from("direct:autoinspecktvir_docuploader_process")
	.routeId(routeId)
	.routeDescription("Autoinspekt Vir Doc Uploader ")
	.bean(TransactionInfoServiceImpl.class,"insertVirDocUploadTransactionInfo")
	.process(new AutoInspektDocUploader())
	.bean(TransactionInfoServiceImpl.class,"updateVirDocUploadTransactionInfo")
	.end();
	
	from("quartz2:"+routeIdAutoInspektVirPolicy+"?stateful=true&cron=0+0/2+10-23+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdVirPolicy+"?stateful=true&cron=0+0+19-23,4-8+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdVirPolicy+"?stateful=true&cron=0+0+3,4+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdVirPolicy+"?stateful=true&cron=0+0+21,22,3,4+?+*+*+*").autoStartup(false)
	.routeId(routeIdAutoInspektVirPolicy)
	.routeDescription("Doc Uploader For Autoinspekt")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:autoinspecktvir_docuploader_process")
	.end();
	
	from("direct:adroit_docuploader_process")
	.routeId(routeIdAdroit)
	.routeDescription("adroit Vir Doc Uploader ")
	.bean(TransactionInfoServiceImpl.class,"insertVirDocUploadTransactionInfo")
	.process(new AdroitDocUploader())
	.bean(TransactionInfoServiceImpl.class,"updateVirDocUploadTransactionInfo")
	.end();
	
	from("quartz2:"+routeIdAdroitVirPolicy+"?stateful=true&cron=0+0/2+10-23+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdAdroitVirPolicy+"?stateful=true&cron=0+0+19-23,4-8+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdAdroitVirPolicy+"?stateful=true&cron=0+0+3,4+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdAdroitVirPolicy+"?stateful=true&cron=0+0+21,22,3,4+?+*+*+*").autoStartup(false)
	.routeId(routeIdAdroitVirPolicy)
	.routeDescription("Doc Uploader For Adroit")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:adroit_docuploader_process")
	.end();
	
	from("direct:virapp_docuploader_process")
	.routeId(routeIdVirApp)
	.routeDescription("App Vir Doc Uploader ")
	.bean(TransactionInfoServiceImpl.class,"insertVirDocUploadTransactionInfo")
	.process(new VirDocUploader())
	.bean(TransactionInfoServiceImpl.class,"updateVirDocUploadTransactionInfo")
	.end();
	
	from("quartz2:"+routeIdAppVirPolicy+"?stateful=true&cron=0+0/2+10-9+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdAppVirPolicy+"?stateful=true&cron=0+0+19-23,4-8+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdAppVirPolicy+"?stateful=true&cron=0+0+3,4+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdAppVirPolicy+"?stateful=true&cron=0+0+21,22,3,4+?+*+*+*").autoStartup(false)
	.routeId(routeIdAppVirPolicy)
	.routeDescription("Doc Uploader For App Vir")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:virapp_docuploader_process")
	.end();

}	


}
*/