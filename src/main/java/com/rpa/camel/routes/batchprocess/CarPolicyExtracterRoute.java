/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.routes.batchprocess;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.AbiblApplicationAutomater;
import com.rpa.camel.processors.batchprocess.AbiblPolicyNumberFetcher;
import com.rpa.camel.processors.batchprocess.AbiblPolicyPdfExtracter;
import com.rpa.camel.processors.batchprocess.FordApplicationAutomater;
import com.rpa.camel.processors.batchprocess.FordPolicyNumberFetcher;
import com.rpa.camel.processors.batchprocess.FordPolicyPdfExtracter;
import com.rpa.camel.processors.batchprocess.HondaApplicationAutomater;
import com.rpa.camel.processors.batchprocess.HondaPolicyNumberFetcher;
import com.rpa.camel.processors.batchprocess.HondaPolicyPdfExtracter;
import com.rpa.camel.processors.batchprocess.MarutiApplicationAutomater;
import com.rpa.camel.processors.batchprocess.MarutiPolicyNumberFetcher;
import com.rpa.camel.processors.batchprocess.MarutiPolicyPdfExtracter;
import com.rpa.camel.processors.batchprocess.MiblApplicationAutomater;
import com.rpa.camel.processors.batchprocess.MiblPolicyNumberFetcher;
import com.rpa.camel.processors.batchprocess.MiblPolicyPdfExtracter;
import com.rpa.camel.processors.batchprocess.PiaggioApplicationAutomater;
import com.rpa.camel.processors.batchprocess.PiaggioPolicyNumberFetcher;
import com.rpa.camel.processors.batchprocess.PiaggioPolicyPdfExtracter;
import com.rpa.camel.processors.batchprocess.TafeApplicationAutomater;
import com.rpa.camel.processors.batchprocess.TafePolicyNumberFetcher;
import com.rpa.camel.processors.batchprocess.TafePolicyPdfExtracter;
import com.rpa.camel.processors.batchprocess.TataApplicationAutomater;
import com.rpa.camel.processors.batchprocess.TataPolicyNumberFetcher;
import com.rpa.camel.processors.batchprocess.TataPolicyPdfExtracter;
import com.rpa.camel.processors.batchprocess.VolvoApplicationAutomater;
import com.rpa.camel.processors.batchprocess.VolvoPolicyNumberFetcher;
import com.rpa.camel.processors.batchprocess.VolvoPolicyPdfExtracter;
import com.rpa.camel.processors.exception.CarPolicyException;
import com.rpa.constants.RPAConstants;
import com.rpa.service.CommonService;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class CarPolicyExtracterRoute extends RouteBuilder {

	private String routeId = "CarPolicyExtracter";
	private String routeIdHonda = "CarPolicyExtracterHonda";
	private String routeIdFord = "CarPolicyExtracterFord";
	private String routeIdTata = "CarPolicyExtracterTata";
	private String routeIdAbibl = "CarPolicyExtracterAbibl";
	private String routeIdMibl = "CarPolicyExtracterMibl";
	private String routeIdVolvo = "CarPolicyExtracterVolvo";
	private String routeIdTafe = "CarPolicyExtracterTafe";
	private String routeIdPiaggio = "CarPolicyExtracterPiaggio";
	
	private String routeIdForMaruti = "policyExtractionMaruti";
	private String routeIdForMarutiBacklog = "policyExtractionMarutiBacklog";
	
	private String routeIdForHonda = "policyExtractionHonda";
	private String routeIdForHondaBacklog = "policyExtractionHondaBacklog";
	
	private String routeIdForFord = "policyExtractionFord";
	private String routeIdForFordBacklog = "policyExtractionFordBacklog";
	
	private String routeIdForTataPV = "policyExtractionTataPV";
	private String routeIdForTataBacklogPV = "policyExtractionTataBacklogPV";
	
	private String routeIdForTataCV = "policyExtractionTataCV";
	private String routeIdForTataBacklogCV = "policyExtractionTataBacklogCV";
	
	private String routeIdForAbibl = "policyExtractionAbibl";
	private String routeIdForAbiblBacklog = "policyExtractionAbiblBacklog";
	
	private String routeIdForMibl = "policyExtractionMibl";
	private String routeIdForMiblBacklog = "policyExtractionMiblBacklog";
	
	private String routeIdForVolvo = "policyExtractionVolvo";
	private String routeIdForVolvoBacklog = "policyExtractionVolvoBacklog";
	
	private String routeIdForTafe = "policyExtractionTafe";
	private String routeIdForTafeBacklog = "policyExtractionTafeBacklog";
	
	private String routeIdForPiaggio = "policyExtractionPiaggio";
	private String routeIdForPiaggioBacklog = "policyExtractionPiaggioBacklog";

	@Override
	public void configure() throws Exception {

	onException(Exception.class).handled(false).process(new CarPolicyException()).bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo");
	
	from("direct:carpolicyextractor_process")
	.routeId(routeId)
	.routeDescription("Car Policy Extractor - Common")
	.bean(TransactionInfoServiceImpl.class,"insertCarPolicyExtractionTransactionInfo")
	.process(new MarutiPolicyNumberFetcher())
	.bean(CommonService.class,"isCredentialsExpired('M')").choice()
	.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo").otherwise()
	.process(new MarutiApplicationAutomater())
	.process(new MarutiPolicyPdfExtracter())
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo")
	.end();
	
	/*from("quartz2:"+routeIdForMaruti+"?stateful=true&cron=0+0/2+9-7+?+*+*+*").autoStartup(false)*/
	/*from("quartz2:"+routeIdForMaruti+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForMaruti+"?stateful=true&cron=0+0+17,18,21+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForMaruti+"?stateful=true&cron=0+0+16-23+?+*+*+*").autoStartup(false)*/
	/*from("quartz2:"+routeIdForMaruti+"?stateful=true&cron=0+0/5+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForMaruti)
	.routeDescription("Car Policy Extractor For Maruti")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:carpolicyextractor_process")
	.end();
	

	/*from("quartz2:"+routeIdForMarutiBacklog+"?stateful=true&cron=0+0/2+9-2+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForMarutiBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForMarutiBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForMarutiBacklog)
	.routeDescription("Car Policy Extractor For Maruti Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:carpolicyextractor_process")
	.end();
	
	
	
	from("direct:hondacarpolicyextractor_process")
	.routeId(routeIdHonda)
	.routeDescription("Honda Car Policy Extractor - Common")
	.bean(TransactionInfoServiceImpl.class,"insertCarPolicyExtractionTransactionInfo")
	/*.process(new HondaPolicyNumberFetcher())*/
	.bean(CommonService.class,"isCredentialsExpired('H')").choice()
	.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo").otherwise()
	.process(new HondaApplicationAutomater())
	.process(new HondaPolicyPdfExtracter())
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo")
	.end();
	
	from("quartz2:"+routeIdForHonda+"?stateful=true&cron=0+0/2+9-8+?+*+*+*").autoStartup(false)
	 /*from("quartz2:"+routeIdForHonda+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)*/
	/*from("quartz2:"+routeIdForHonda+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForHonda)
	.routeDescription("Car Policy Extractor For Honda")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:hondacarpolicyextractor_process")
	.end();
	
	from("quartz2:"+routeIdForHondaBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForHondaBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForHondaBacklog)
	.routeDescription("Car Policy Extractor For Honda Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:hondacarpolicyextractor_process")
	.end();
	
	
	from("direct:fordcarpolicyextractor_process")
	.routeId(routeIdFord)
	.routeDescription("Ford Car Policy Extractor - Common")
	.bean(TransactionInfoServiceImpl.class,"insertCarPolicyExtractionTransactionInfo")
	.process(new FordPolicyNumberFetcher())
	.bean(CommonService.class,"isCredentialsExpired('F')").choice()
	.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo").otherwise()
	.process(new FordApplicationAutomater())
	.process(new FordPolicyPdfExtracter())
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo")
	.end();
	
	//from("quartz2:"+routeIdForFord+"?stateful=true&cron=0+0/2+9-8+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdForFord+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForFord+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForFord)
	.routeDescription("Car Policy Extractor For Ford")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:fordcarpolicyextractor_process")
	.end();
	
	from("quartz2:"+routeIdForFordBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForFordBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForFordBacklog)
	.routeDescription("Car Policy Extractor For Ford Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:fordcarpolicyextractor_process")
	.end();
	
	
	from("direct:tatacarpolicyextractor_process")
	.routeId(routeIdTata)
	.routeDescription("Tata Car Policy Extractor - Common")
	.bean(TransactionInfoServiceImpl.class,"insertCarPolicyExtractionTransactionInfo")
	.process(new TataPolicyNumberFetcher())
	.bean(CommonService.class,"isCredentialsExpired('T')").choice()
	.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo").otherwise()
	.process(new TataApplicationAutomater())
	.process(new TataPolicyPdfExtracter())
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo")
	.end();
	
	//from("quartz2:"+routeIdForTataPV+"?stateful=true&cron=0+0/2+9-8+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdForTataPV+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForTataPV+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForTataPV)
	.routeDescription("Car Policy Extractor For Tata PV")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.setProperty(RPAConstants.VEHICLE_TYPE, simple(RPAConstants.PV))
	.to("direct:tatacarpolicyextractor_process")
	.end();
	
	from("quartz2:"+routeIdForTataBacklogPV+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForTataBacklogPV+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForTataBacklogPV)
	.routeDescription("Car Policy Extractor For Tata Backlog PV")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.setProperty(RPAConstants.VEHICLE_TYPE, simple(RPAConstants.PV))
	.to("direct:tatacarpolicyextractor_process")
	.end();
	
	
    //from("quartz2:"+routeIdForTataCV+"?stateful=true&cron=0+0/2+9-8+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdForTataCV+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForTataCV+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForTataCV)
	.routeDescription("Car Policy Extractor For Tata CV")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.setProperty(RPAConstants.VEHICLE_TYPE, simple(RPAConstants.CV))
	.to("direct:tatacarpolicyextractor_process")
	.end();
	
	from("quartz2:"+routeIdForTataBacklogCV+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForTataBacklogCV+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForTataBacklogCV)
	.routeDescription("Car Policy Extractor For Tata Backlog CV")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.setProperty(RPAConstants.VEHICLE_TYPE, simple(RPAConstants.CV))
	.to("direct:tatacarpolicyextractor_process")
	.end();
	
	
	from("direct:abiblcarpolicyextractor_process")
	.routeId(routeIdAbibl)
	.routeDescription("Abibl Car Policy Extractor - Common")
	.bean(TransactionInfoServiceImpl.class,"insertCarPolicyExtractionTransactionInfo")
	.process(new AbiblPolicyNumberFetcher())
	.bean(CommonService.class,"isCredentialsExpired('A')").choice()
	.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo").otherwise()
	.process(new AbiblApplicationAutomater())
	.process(new AbiblPolicyPdfExtracter())
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo")
	.end();
	
	//from("quartz2:"+routeIdForAbibl+"?stateful=true&cron=0+0/2+9-8+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdForAbibl+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForAbibl+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForAbibl)
	.routeDescription("Car Policy Extractor For Abibl")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:abiblcarpolicyextractor_process")
	.end();
	
	from("quartz2:"+routeIdForAbiblBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForAbiblBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForAbiblBacklog)
	.routeDescription("Car Policy Extractor For Abibl Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:abiblcarpolicyextractor_process")
	.end();
	
	
	from("direct:miblcarpolicyextractor_process")
	.routeId(routeIdMibl)
	.routeDescription("Mibl Car Policy Extractor - Common")
	.bean(TransactionInfoServiceImpl.class,"insertCarPolicyExtractionTransactionInfo")
	.process(new MiblPolicyNumberFetcher())
	.bean(CommonService.class,"isCredentialsExpired('B')").choice()
	.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo").otherwise()
	.process(new MiblApplicationAutomater())
	.process(new MiblPolicyPdfExtracter())
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo")
	.end();
	
	//from("quartz2:"+routeIdForMibl+"?stateful=true&cron=0+0/2+9-8+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdForMibl+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForMibl+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForMibl)
	.routeDescription("Car Policy Extractor For Mibl")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:miblcarpolicyextractor_process")
	.end();
	
	from("quartz2:"+routeIdForMiblBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForMiblBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForMiblBacklog)
	.routeDescription("Car Policy Extractor For Mibl Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:miblcarpolicyextractor_process")
	.end();
	
	
	from("direct:volvocarpolicyextractor_process")
	.routeId(routeIdVolvo)
	.routeDescription("Volvo Car Policy Extractor - Common")
	.bean(TransactionInfoServiceImpl.class,"insertCarPolicyExtractionTransactionInfo")
	//.process(new VolvoPolicyNumberFetcher())
	.bean(CommonService.class,"isCredentialsExpired('V')").choice()
	.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo").otherwise()
	.process(new VolvoApplicationAutomater())
	.process(new VolvoPolicyPdfExtracter())
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo")
	.end();
	
	from("quartz2:"+routeIdForVolvo+"?stateful=true&cron=0+0/2+9-8+?+*+*+*").autoStartup(false)
	//from("quartz2:"+routeIdForVolvo+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForVolvo+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForVolvo)
	.routeDescription("Car Policy Extractor For Volvo")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:volvocarpolicyextractor_process")
	.end();
	
	from("quartz2:"+routeIdForVolvoBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForVolvoBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForVolvoBacklog)
	.routeDescription("Car Policy Extractor For Volvo Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:volvocarpolicyextractor_process")
	.end();
	
	
	from("direct:tafecarpolicyextractor_process")
	.routeId(routeIdTafe)
	.routeDescription("Tafe Car Policy Extractor - Common")
	.bean(TransactionInfoServiceImpl.class,"insertCarPolicyExtractionTransactionInfo")
	.process(new TafePolicyNumberFetcher())
	.bean(CommonService.class,"isCredentialsExpired('E')").choice()
	.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo").otherwise()
	.process(new TafeApplicationAutomater())
	.process(new TafePolicyPdfExtracter())
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo")
	.end();
	
	/*from("quartz2:"+routeIdForTafe+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	//from("quartz2:"+routeIdForTafe+"?stateful=true&cron=0+0/2+9-8+?+*+*+*").autoStartup(false)
	from("quartz2:"+routeIdForTafe+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	.routeId(routeIdForTafe)
	.routeDescription("Car Policy Extractor For Tafe")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:tafecarpolicyextractor_process")
	.end();
	
	from("quartz2:"+routeIdForTafeBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForTafeBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForTafeBacklog)
	.routeDescription("Car Policy Extractor For Tafe Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:tafecarpolicyextractor_process")
	.end();
	
	
	from("direct:piaggiocarpolicyextractor_process")
	.routeId(routeIdPiaggio)
	.routeDescription("Piaggio Car Policy Extractor - Common")
	.bean(TransactionInfoServiceImpl.class,"insertCarPolicyExtractionTransactionInfo")
	//.process(new PiaggioPolicyNumberFetcher())
	.bean(CommonService.class,"isCredentialsExpired('P')").choice()
	.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo").otherwise()
	.process(new PiaggioApplicationAutomater())
	.process(new PiaggioPolicyPdfExtracter())
	.bean(TransactionInfoServiceImpl.class,"updateCarPolicyExtractionTransactionInfo")
	.end();
	
	//from("quartz2:"+routeIdForPiaggio+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForPiaggio+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForPiaggio+"?stateful=true&cron=0+0/2+9-8+?+*+*+*").autoStartup(false)
	.routeId(routeIdForPiaggio)
	.routeDescription("Car Policy Extractor For Piaggio")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:piaggiocarpolicyextractor_process")
	.end();
	
	from("quartz2:"+routeIdForPiaggioBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForPiaggioBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForPiaggioBacklog)
	.routeDescription("Car Policy Extractor For Piaggio Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:piaggiocarpolicyextractor_process")
	.end();
}	


	
	
}
