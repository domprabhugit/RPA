/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.routes.batchprocess;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.AbiblPolicyPdfUploader;
import com.rpa.camel.processors.batchprocess.FordPolicyPdfUploader;
import com.rpa.camel.processors.batchprocess.HondaPolicyPdfUploader;
import com.rpa.camel.processors.batchprocess.MarutiPolicyPdfUploader;
import com.rpa.camel.processors.batchprocess.MiblPolicyPdfUploader;
import com.rpa.camel.processors.batchprocess.PiaggioPolicyPdfUploader;
import com.rpa.camel.processors.batchprocess.TafePolicyPdfUploader;
import com.rpa.camel.processors.batchprocess.TataPolicyPdfUploader;
import com.rpa.camel.processors.batchprocess.VolvoPolicyPdfUploader;
import com.rpa.camel.processors.exception.CarPolicyException;
import com.rpa.constants.RPAConstants;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class CarPolicyUploaderRoute extends RouteBuilder {

	private String routeId = "carPolicyUpload";
	private String routeIdHonda = "carPolicyUploadHonda";
	private String routeIdFord = "carPolicyUploadFord";
	private String routeIdTata = "carPolicyUploadTata";
	private String routeIdAbibl = "carPolicyUploadAbibl";
	private String routeIdMibl = "carPolicyUploadMibl";
	private String routeIdVolvo = "carPolicyUploadVolvo";
	private String routeIdTafe = "carPolicyUploadTafe";
	private String routeIdPiaggio = "carPolicyUploadPiaggio";
	
	private String routeIdForMarutiUpload = "policyUploadMaruti";
	private String routeIdForMarutiUploadBacklog = "policyUploadMarutiBacklog";
	
	private String routeIdForHondaUpload = "policyUploadHonda";
	private String routeIdForHondaUploadBacklog = "policyUploadHondaBacklog";
	
	private String routeIdForFordUpload = "policyUploadFord";
	private String routeIdForFordUploadBacklog = "policyUploadFordBacklog";
	
	private String routeIdForTataUpload = "policyUploadTata";
	private String routeIdForTataUploadBacklog = "policyUploadTataBacklog";
	
	private String routeIdForAbiblUpload = "policyUploadAbibl";
	private String routeIdForAbiblUploadBacklog = "policyUploadAbiblBacklog";

	private String routeIdForMiblUpload = "policyUploadMibl";
	private String routeIdForMiblUploadBacklog = "policyUploadMiblBacklog";
	
	private String routeIdForVolvoUpload = "policyUploadVolvo";
	private String routeIdForVolvoUploadBacklog = "policyUploadVolvoBacklog";
	
	private String routeIdForTafeUpload = "policyUploadTafe";
	private String routeIdForTafeUploadBacklog = "policyUploadTafeBacklog";
	
	private String routeIdForPiaggioUpload = "policyUploadPiaggio";
	private String routeIdForPiaggioUploadBacklog = "policyUploadPiaggioBacklog";
	
	@Override
	public void configure() throws Exception {

	onException(Exception.class).handled(false).process(new CarPolicyException()).bean(TransactionInfoServiceImpl.class,"updateOmniDocUploadTransactionInfo").end();
	
	from("direct:carpolicyuploader_process")
	.routeId(routeId)
	.routeDescription("Car Policy Uploader ")
	.bean(TransactionInfoServiceImpl.class,"insertOmniDocUploadTransactionInfo")
	.process(new MarutiPolicyPdfUploader())
	.bean(TransactionInfoServiceImpl.class,"updateOmniDocUploadTransactionInfo")
	.end();
	
	/*from("quartz2:"+routeIdForMarutiUpload+"?stateful=true&cron=0+0/2+9-2+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForMarutiUpload+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForMarutiUpload+"?stateful=true&cron=0+0+18-23+?+*+*+*").autoStartup(false)*/
	/*from("quartz2:"+routeIdForMarutiUpload+"?stateful=true&cron=0+0+14-23+?+*+*+*").autoStartup(false)*/
	/*from("quartz2:"+routeIdForMarutiUpload+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForMarutiUpload)
	.routeDescription("Car Policy Uploader For Maruti")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:carpolicyuploader_process")
	.end();
	

	/*from("quartz2:"+routeIdForMarutiUploadBacklog+"?stateful=true&cron=0+0/1+9-2+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForMarutiUploadBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForMarutiUploadBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForMarutiUploadBacklog)
	.routeDescription("Car Policy Uploader For Maruti Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:carpolicyuploader_process")
	.end();
	
	from("direct:hondacarpolicyuploader_process")
	.routeId(routeIdHonda)
	.routeDescription("Honda Car Policy Uploader ")
	.bean(TransactionInfoServiceImpl.class,"insertOmniDocUploadTransactionInfo")
	.process(new HondaPolicyPdfUploader())
	.bean(TransactionInfoServiceImpl.class,"updateOmniDocUploadTransactionInfo")
	.end();
	
	from("quartz2:"+routeIdForHondaUpload+"?stateful=true&cron=0+0/2+9-2+?+*+*+*").autoStartup(false)
	//from("quartz2:"+routeIdForHondaUpload+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForHondaUpload+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForHondaUpload)
	.routeDescription("Car Policy Uploader For Honda")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:hondacarpolicyuploader_process")
	.end();
	

	/*from("quartz2:"+routeIdForHondaUploadBacklog+"?stateful=true&cron=0+0/1+9-2+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForHondaUploadBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForHondaUploadBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForHondaUploadBacklog)
	.routeDescription("Car Policy Uploader For Honda Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:hondacarpolicyuploader_process")
	.end();
	
	from("direct:fordcarpolicyuploader_process")
	.routeId(routeIdFord)
	.routeDescription("Ford Car Policy Uploader ")
	.bean(TransactionInfoServiceImpl.class,"insertOmniDocUploadTransactionInfo")
	.process(new FordPolicyPdfUploader())
	.bean(TransactionInfoServiceImpl.class,"updateOmniDocUploadTransactionInfo")
	.end();
	
	/*from("quartz2:"+routeIdForFordUpload+"?stateful=true&cron=0+0/2+9-2+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForFordUpload+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForFordUpload+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForFordUpload)
	.routeDescription("Car Policy Uploader For Ford")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:fordcarpolicyuploader_process")
	.end();
	

	/*from("quartz2:"+routeIdForFordUploadBacklog+"?stateful=true&cron=0+0/1+9-2+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForFordUploadBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForFordUploadBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForFordUploadBacklog)
	.routeDescription("Car Policy Uploader For Ford Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:fordcarpolicyuploader_process")
	.end();
	
	
	from("direct:tatacarpolicyuploader_process")
	.routeId(routeIdTata)
	.routeDescription("Tata Car Policy Uploader ")
	.bean(TransactionInfoServiceImpl.class,"insertOmniDocUploadTransactionInfo")
	.process(new TataPolicyPdfUploader())
	.bean(TransactionInfoServiceImpl.class,"updateOmniDocUploadTransactionInfo")
	.end();
	
	/*from("quartz2:"+routeIdForTataUpload+"?stateful=true&cron=0+0/2+9-2+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForTataUpload+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForTataUpload+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForTataUpload)
	.routeDescription("Car Policy Uploader For Tata")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:tatacarpolicyuploader_process")
	.end();
	

	/*from("quartz2:"+routeIdForTataUploadBacklog+"?stateful=true&cron=0+0/1+9-2+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForTataUploadBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForTataUploadBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForTataUploadBacklog)
	.routeDescription("Car Policy Uploader For Tata Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:tatacarpolicyuploader_process")
	.end();
	
	
	from("direct:abiblcarpolicyuploader_process")
	.routeId(routeIdAbibl)
	.routeDescription("Abibl Car Policy Uploader ")
	.bean(TransactionInfoServiceImpl.class,"insertOmniDocUploadTransactionInfo")
	.process(new AbiblPolicyPdfUploader())
	.bean(TransactionInfoServiceImpl.class,"updateOmniDocUploadTransactionInfo")
	.end();
	
	/*from("quartz2:"+routeIdForAbiblUpload+"?stateful=true&cron=0+0/2+9-2+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForAbiblUpload+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForAbiblUpload+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForAbiblUpload)
	.routeDescription("Car Policy Uploader For Abibl")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:abiblcarpolicyuploader_process")
	.end();
	

	/*from("quartz2:"+routeIdForAbiblUploadBacklog+"?stateful=true&cron=0+0/1+9-2+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForAbiblUploadBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForAbiblUploadBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForAbiblUploadBacklog)
	.routeDescription("Car Policy Uploader For Abibl Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:abiblcarpolicyuploader_process")
	.end();
	
	
	from("direct:miblcarpolicyuploader_process")
	.routeId(routeIdMibl)
	.routeDescription("Mibl Car Policy Uploader ")
	.bean(TransactionInfoServiceImpl.class,"insertOmniDocUploadTransactionInfo")
	.process(new MiblPolicyPdfUploader())
	.bean(TransactionInfoServiceImpl.class,"updateOmniDocUploadTransactionInfo")
	.end();
	
	/*from("quartz2:"+routeIdForMiblUpload+"?stateful=true&cron=0+0/1+9-2+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForMiblUpload+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForMiblUpload+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForMiblUpload)
	.routeDescription("Car Policy Uploader For Mibl")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:miblcarpolicyuploader_process")
	.end();
	

	/*from("quartz2:"+routeIdForMiblUploadBacklog+"?stateful=true&cron=0+0/1+9-2+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForMiblUploadBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForMiblUploadBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForMiblUploadBacklog)
	.routeDescription("Car Policy Uploader For Mibl Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:miblcarpolicyuploader_process")
	.end();
	
	
	from("direct:volvocarpolicyuploader_process")
	.routeId(routeIdVolvo)
	.routeDescription("Volvo Car Policy Uploader ")
	.bean(TransactionInfoServiceImpl.class,"insertOmniDocUploadTransactionInfo")
	.process(new VolvoPolicyPdfUploader())
	.bean(TransactionInfoServiceImpl.class,"updateOmniDocUploadTransactionInfo")
	.end();
	
	/*from("quartz2:"+routeIdForVolvoUpload+"?stateful=true&cron=0+0/1+9-2+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForVolvoUpload+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForVolvoUpload+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForVolvoUpload)
	.routeDescription("Car Policy Uploader For Volvo")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:volvocarpolicyuploader_process")
	.end();
	

	/*from("quartz2:"+routeIdForVolvoUploadBacklog+"?stateful=true&cron=0+0/1+9-2+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForVolvoUploadBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForVolvoUploadBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForVolvoUploadBacklog)
	.routeDescription("Car Policy Uploader For Volvo Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:volvocarpolicyuploader_process")
	.end();
	
	
	from("direct:tafecarpolicyuploader_process")
	.routeId(routeIdTafe)
	.routeDescription("Tafe Car Policy Uploader ")
	.bean(TransactionInfoServiceImpl.class,"insertOmniDocUploadTransactionInfo")
	.process(new TafePolicyPdfUploader())
	.bean(TransactionInfoServiceImpl.class,"updateOmniDocUploadTransactionInfo")
	.end();
	
	/*from("quartz2:"+routeIdForTafeUpload+"?stateful=true&cron=0+0/2+9-2+?+*+*+*").autoStartup(false)*/
	/*from("quartz2:"+routeIdForTafeUpload+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	from("quartz2:"+routeIdForTafeUpload+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	.routeId(routeIdForTafeUpload)
	.routeDescription("Car Policy Uploader For Tafe")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:tafecarpolicyuploader_process")
	.end();

	from("quartz2:"+routeIdForTafeUploadBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForTafeUploadBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForTafeUploadBacklog)
	.routeDescription("Car Policy Uploader For Tafe Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:tafecarpolicyuploader_process")
	.end();
	
	
	from("direct:piaggiocarpolicyuploader_process")
	.routeId(routeIdPiaggio)
	.routeDescription("Piaggio Car Policy Uploader ")
	.bean(TransactionInfoServiceImpl.class,"insertOmniDocUploadTransactionInfo")
	.process(new PiaggioPolicyPdfUploader())
	.bean(TransactionInfoServiceImpl.class,"updateOmniDocUploadTransactionInfo")
	.end();
	
	from("quartz2:"+routeIdForPiaggioUpload+"?stateful=true&cron=0+0+4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForPiaggioUpload+"?stateful=true&cron=0+0/2+9-2+?+*+*+*").autoStartup(false)*/
	/*from("quartz2:"+routeIdForPiaggioUpload+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForPiaggioUpload)
	.routeDescription("Car Policy Uploader For Piaggio")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.NORMAL))
	.to("direct:piaggiocarpolicyuploader_process")
	.end();
	
	from("quartz2:"+routeIdForPiaggioUploadBacklog+"?stateful=true&cron=0+0+18-23,4-7+?+*+*+*").autoStartup(false)
	/*from("quartz2:"+routeIdForPiaggioUploadBacklog+"?stateful=true&cron=0+0/10+10-23+?+*+*+*").autoStartup(false)*/
	.routeId(routeIdForPiaggioUploadBacklog)
	.routeDescription("Car Policy Uploader For Piaggio Backlog")
	.setProperty(RPAConstants.EXTRACTION_TYPE, simple(RPAConstants.BACK_LOG))
	.to("direct:piaggiocarpolicyuploader_process")
	.end();
}	


}
