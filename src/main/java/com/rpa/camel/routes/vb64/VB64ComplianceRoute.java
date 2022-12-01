/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.routes.vb64;  

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.exception.VB64ComplianceException;
import com.rpa.camel.processors.uploader.VB64ComplianceUploader;
import com.rpa.camel.processors.vb64.VB64ComplianceTransformer;
import com.rpa.camel.processors.vb64.VB64ComplianceValidator;
import com.rpa.constants.RPAConstants;
import com.rpa.service.CommonService;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class VB64ComplianceRoute extends RouteBuilder {

	private String routeIdForHDFC = "HDFC_64VB_Compliance";
	private String routeIdForHSBC = "HSBC_64VB_Compliance";
	private String routeIdForCITI = "CITI_64VB_Compliance";
	private String routeIdForSCB = "SCB_64VB_Compliance";

	@Override
	public void configure() throws Exception {

		onException(Exception.class).handled(false).process(new VB64ComplianceException()).bean(TransactionInfoServiceImpl.class,"updateVB64TransactionInfo");
		
		from("direct:vb64_process")
		.routeId("VB64ComplianceUploadCommon")
		.routeDescription("64VB Compliance Upload Process - Common")
		.process(new VB64CompliancePreProcessor())
		.choice()
			.when(header(RPAConstants.FILE_NOT_FOUND).isEqualTo(RPAConstants.Y))
				.bean(TransactionInfoServiceImpl.class,"insertVB64TransactionInfo")
				.bean(TransactionInfoServiceImpl.class,"updateVB64TransactionInfo")
		.end()
		.split(body()).parallelProcessing(false)
		.bean(TransactionInfoServiceImpl.class,"insertVB64TransactionInfo")
		.process(new VB64ComplianceValidator())
		.process(new VB64ComplianceTransformer())
		.process(new VB64ComplianceUploader())
		.bean(TransactionInfoServiceImpl.class,"updateVB64TransactionInfo")
		.end();
		
		/** HDFC BANK **/
		from("quartz2:VB64HDFCRoute?stateful=true&cron=0+0+11,17+*+*+?").autoStartup(false)
		/*from("quartz2:VB64HDFCRoute?stateful=true&cron=0+0/15+9-19+?+*+*+*").autoStartup(false)*/
		/*from("quartz2:VB64HDFCRoute?stateful=true&cron=0+0/2+9-19+?+*+*+*").autoStartup(false)*/
		.routeId(routeIdForHDFC)
		.routeDescription("64VB Compliance for HDFC")
		.setProperty(RPAConstants.BANK_NAME, simple(RPAConstants.HDFC))
		.setProperty(RPAConstants.SUCCESS_BASE_FOLDER_PATH, simple(RPAConstants.VB64_HDFC_FOLDER_SUCCESS_FILE))
		.setProperty(RPAConstants.ERROR_BASE_FOLDER_PATH, simple(RPAConstants.VB64_HDFC_FOLDER_ERROR_FILE))
		.setProperty(RPAConstants.UPLOAD_BASE_FOLDER_PATH, simple(RPAConstants.VB64_HDFC_FOLDER_UPLOAD_FILE))
		.bean(CommonService.class,"readAllFilesInsideAFolderWithExtension('VB64_HDFC_FOLDER_BANK_FILE','.xls','.XLS')")
		.to("direct:vb64_process")
		.end();
		
		/** CITI BANK **/
		from("quartz2:VB64CITIRoute?stateful=true&cron=0+0+11,17+*+*+?").autoStartup(false)
		/*from("quartz2:VB64CITIRoute?stateful=true&cron=0+0/15+9-19+?+*+*+*").autoStartup(false)*/
		/*from("quartz2:VB64CITIRoute?stateful=true&cron=0+0/2+9-19+?+*+*+*").autoStartup(false)*/
		.routeId(routeIdForCITI)
		.routeDescription("64VB Compliance for CITI")
		.setProperty(RPAConstants.BANK_NAME, simple(RPAConstants.CITI))
		.setProperty(RPAConstants.SUCCESS_BASE_FOLDER_PATH, simple(RPAConstants.VB64_CITI_FOLDER_SUCCESS_FILE))
		.setProperty(RPAConstants.ERROR_BASE_FOLDER_PATH, simple(RPAConstants.VB64_CITI_FOLDER_ERROR_FILE))
		.setProperty(RPAConstants.UPLOAD_BASE_FOLDER_PATH, simple(RPAConstants.VB64_CITI_FOLDER_UPLOAD_FILE))
		.bean(CommonService.class,"readAllFilesInsideAFolderWithExtension('VB64_CITI_FOLDER_BANK_FILE','.xls','.XLS')")
		.to("direct:vb64_process")
		.end();
		
		/** HSBC BANK **/
		from("quartz2:VB64HSBCRoute?stateful=true&cron=0+0+11,17+*+*+?").autoStartup(false)
		/*from("quartz2:VB64HSBCRoute?stateful=true&cron=0+0/15+9-19+?+*+*+*").autoStartup(false)*/
		.routeId(routeIdForHSBC)
		.routeDescription("64VB Compliance for HSBC")
		.setProperty(RPAConstants.BANK_NAME, simple(RPAConstants.HSBC))
		.setProperty(RPAConstants.SUCCESS_BASE_FOLDER_PATH, simple(RPAConstants.VB64_HSBC_FOLDER_SUCCESS_FILE))
		.setProperty(RPAConstants.ERROR_BASE_FOLDER_PATH, simple(RPAConstants.VB64_HSBC_FOLDER_ERROR_FILE))
		.setProperty(RPAConstants.UPLOAD_BASE_FOLDER_PATH, simple(RPAConstants.VB64_HSBC_FOLDER_UPLOAD_FILE))
		.bean(CommonService.class,"readAllFilesInsideAFolderWithExtension('VB64_HSBC_FOLDER_BANK_FILE','.xls','.XLS')")
		.to("direct:vb64_process")
		.end();
		
		/** SCB BANK **/
		from("quartz2:VB64SCBRoute?stateful=true&cron=0+0+11,17+*+*+?").autoStartup(false)
		/*from("quartz2:VB64SCBRoute?stateful=true&cron=0+0/15+9-19+?+*+*+*").autoStartup(false)*/
		/*from("quartz2:VB64SCBRoute?stateful=true&cron=0+0/2+9-19+?+*+*+*").autoStartup(false)*/
		.routeId(routeIdForSCB)
		.routeDescription("64VB Compliance for SCB")
		.setProperty(RPAConstants.BANK_NAME, simple(RPAConstants.SCB))
		.setProperty(RPAConstants.SUCCESS_BASE_FOLDER_PATH, simple(RPAConstants.VB64_SCB_FOLDER_SUCCESS_FILE))
		.setProperty(RPAConstants.ERROR_BASE_FOLDER_PATH, simple(RPAConstants.VB64_SCB_FOLDER_ERROR_FILE))
		.setProperty(RPAConstants.UPLOAD_BASE_FOLDER_PATH, simple(RPAConstants.VB64_SCB_FOLDER_UPLOAD_FILE))
		.bean(CommonService.class,"readAllFilesInsideAFolderWithExtension('VB64_SCB_FOLDER_BANK_FILE','.xls','.XLS')")
		.to("direct:vb64_process")
		.end();
	}	

	public class VB64CompliancePreProcessor implements Processor {

		public void process(Exchange exchange) throws IOException, InvalidFormatException, URISyntaxException{
			@SuppressWarnings("unchecked")
			ArrayList<String> fileArrayList = (ArrayList<String>) exchange.getIn().getBody();
			/*TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.setProcessStatus(RPAConstants.Initialize);
			transactionInfo.setProcessFailureReason(ErrorConstants.ERROR_CODE_00013);*/
			if(fileArrayList.size()==0){
				/*transactionInfo.setProcessStatus(RPAConstants.Failed);
				transactionInfo.setProcessFailureReason(ErrorConstants.ERROR_CODE_0007);*/
				exchange.getIn().setHeader(RPAConstants.FILE_NOT_FOUND, RPAConstants.Y);
				exchange.setProperty("NO_FILE",RPAConstants.Y);
			}else{
				exchange.setProperty("NO_FILE",RPAConstants.N);
			}
			//exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		}
	}
}