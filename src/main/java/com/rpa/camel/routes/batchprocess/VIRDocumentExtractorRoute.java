/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 

package com.rpa.camel.routes.batchprocess;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.AdroitApplicationAutomater;
import com.rpa.camel.processors.batchprocess.AdroitVirDocExtracter;
import com.rpa.camel.processors.batchprocess.AdroitVirNumberFetcher;
import com.rpa.camel.processors.batchprocess.AutoInspektApplicationAutomater;
import com.rpa.camel.processors.batchprocess.AutoInspektVirDocExtracter;
import com.rpa.camel.processors.batchprocess.AutoInspektVirNumberFetcher;
import com.rpa.camel.processors.batchprocess.VirDocumentMailReader;
import com.rpa.camel.processors.exception.FirstgenPolicyDownloadException;
import com.rpa.constants.RPAConstants;
import com.rpa.service.CommonService;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class VIRDocumentExtractorRoute extends RouteBuilder {

	private String routeId = "virDocumentExtractorProcess";
	private String routeIdVir = "virDocumentExtractor";
	
	private String routeIdAutoInspekt = "autoInskeptExtractorProcess";
	private String routeIdForAutoInspekt = "autoInskeptExtractor";
	
	private String routeIdAdroit = "adroitExtractorProcess";
	private String routeIdForAdroit = "adroitExtractor";
	
	@Override
	public void configure() throws Exception {

	onException(Exception.class).handled(false).process(new FirstgenPolicyDownloadException()).bean(TransactionInfoServiceImpl.class,"updateVIRPolicyExtractionTransactionInfo");
	
	from("direct:virDocumentExtractor_process")
	.routeId(routeId)
	.routeDescription("VIR Document from Mail Extraction - Common")
	.bean(TransactionInfoServiceImpl.class,"insertVIRPolicyExtractionTransactionInfo")
	.process(new VirDocumentMailReader())
	.bean(TransactionInfoServiceImpl.class,"updateVIRPolicyExtractionTransactionInfo")
	.end();
	
	from("quartz2:"+routeIdVir+"?stateful=true&cron=0+0+0/1+1/1+*+?+*").autoStartup(false)
	from("quartz2:"+routeIdVir+"?stateful=true&cron=0+0/2+7-6+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdVir+"?stateful=true&cron=0+0+2,3+?+*+*+*").autoStartup(true)
	from("quartz2:"+routeIdVir+"?stateful=true&cron=0+0+20,21,2,3+?+*+*+*").autoStartup(false)
	.routeId(routeIdVir)
	.routeDescription("VIR Document From Mail Extraction")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:virDocumentExtractor_process")
	.end();
	
	
	from("direct:autoInspektDocumentExtractor_process")
	.routeId(routeIdAutoInspekt)
	.routeDescription("Auto Inspekt VIR Extraction - Common")
	.bean(TransactionInfoServiceImpl.class,"insertVIRPolicyExtractionTransactionInfo")
	.process(new AutoInspektVirNumberFetcher())
	.bean(CommonService.class,"isCredentialsExpired('P')").choice()
	.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
	.bean(TransactionInfoServiceImpl.class,"updateVIRPolicyExtractionTransactionInfo").otherwise()
	.process(new AutoInspektApplicationAutomater())
	.process(new AutoInspektVirDocExtracter())
	.bean(TransactionInfoServiceImpl.class,"updateVIRPolicyExtractionTransactionInfo")
	
	.end();
	
	from("quartz2:"+routeIdForAutoInspekt+"?stateful=true&cron=0+0+0/1+1/1+*+?+*").autoStartup(false)
	from("quartz2:"+routeIdForAutoInspekt+"?stateful=true&cron=0+0/2+7-6+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdForAutoInspekt+"?stateful=true&cron=0+0/2+7-23+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdForAutoInspekt+"?stateful=true&cron=0+0+2,3+?+*+*+*").autoStartup(true)
	from("quartz2:"+routeIdForAutoInspekt+"?stateful=true&cron=0+0+20,21,2,3+?+*+*+*").autoStartup(false)
	.routeId(routeIdForAutoInspekt)
	.routeDescription("Autoinspekt VIR Extraction")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:autoInspektDocumentExtractor_process")
	.end();
	
	from("direct:adroitDocumentExtractor_process")
	.routeId(routeIdAdroit)
	.routeDescription("Adroit VIR Extraction - Common")
	.bean(TransactionInfoServiceImpl.class,"insertVIRPolicyExtractionTransactionInfo")
	.process(new AdroitVirNumberFetcher())
	.bean(CommonService.class,"isCredentialsExpired('P')").choice()
	.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
	.bean(TransactionInfoServiceImpl.class,"updateVIRPolicyExtractionTransactionInfo").otherwise()
	.process(new AdroitApplicationAutomater())
	.process(new AdroitVirDocExtracter())
	.bean(TransactionInfoServiceImpl.class,"updateVIRPolicyExtractionTransactionInfo")
	
	.end();
	
	from("quartz2:"+routeIdForAdroit+"?stateful=true&cron=0+0+0/1+1/1+*+?+*").autoStartup(false)
	from("quartz2:"+routeIdForAdroit+"?stateful=true&cron=0+0/2+7-6+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdForAdroit+"?stateful=true&cron=0+0/2+7-23+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdForAdroit+"?stateful=true&cron=0+0+2,3+?+*+*+*").autoStartup(true)
	from("quartz2:"+routeIdForAdroit+"?stateful=true&cron=0+0+20,21,2,3+?+*+*+*").autoStartup(false)
	.routeId(routeIdForAdroit)
	.routeDescription("Adroit VIR Extraction")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:adroitDocumentExtractor_process")
	.end();
	

}	


	
	
}
*/