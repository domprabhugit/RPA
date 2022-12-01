package com.rpa.camel.processors.exception;

import java.io.File;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.ErrorInfo;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.GridAutomationModel;
import com.rpa.model.processors.GridWithModelSheetAutomationModel;
import com.rpa.service.ErrorInfoService;
import com.rpa.service.processors.GridAutomationService;
import com.rpa.service.processors.GridWithModelSheetAutomationService;
import com.rpa.util.UtilityFile;

public class GridUploadException implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(GridUploadException.class.getName());


	@Autowired
	ErrorInfoService errorInfoService;

	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	GridWithModelSheetAutomationService gridWithModelAutomationService;
	
	@Autowired
	GridAutomationService gridAutomationService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty("TRANSACTION_INFO_REQ");
		logger.info("Inside GridUploadException::Process Phase::" + transactionInfo.getProcessPhase());
		
		applicationContext = SpringContext.getAppContext();
		errorInfoService = applicationContext.getBean(ErrorInfoService.class);
		
		transactionInfo.setTransactionStatus("Error");
		
		logger.info("Caught Error :: "+exchange.getProperty(Exchange.EXCEPTION_CAUGHT));
		
		
		if (transactionInfo.getId() != null) {
			
			if(transactionInfo.getProcessName().equalsIgnoreCase("GridWithModelSheetMasterUploadProcess")){
				GridWithModelSheetAutomationModel gridModelObj = (GridWithModelSheetAutomationModel) exchange.getProperty("grid_obj");
				if(gridModelObj!=null){
				gridWithModelAutomationService = applicationContext.getBean(GridWithModelSheetAutomationService.class);
				
				
				String newFilePath = UtilityFile.FileMovementMethod(
						UtilityFile.getCodeBasePath()
								+ UtilityFile.getGridUploadProperty("GridModel.File.Location.BasePath"),
						new File(gridModelObj.getFilePath()),
						UtilityFile.getGridUploadProperty("GridModel.File.Processed.Path")
								+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY)
										.toString()
								+ UtilityFile.getGridUploadProperty("GridModel.File.Location.ErrorPath"));
				gridModelObj.setFilePath(newFilePath);
				gridModelObj.setIsValidated("Y");
				gridModelObj.setIsProcessed("Y");
				gridWithModelAutomationService.save(gridModelObj);
				}
			}else if(transactionInfo.getProcessName().equalsIgnoreCase("GridMasterUploadProcess")){
				GridAutomationModel gridModelObj = (GridAutomationModel) exchange.getProperty("grid_obj");
				if(gridModelObj!=null){
				gridAutomationService = applicationContext.getBean(GridAutomationService.class);
				
				String newFilePath = UtilityFile.FileMovementMethod(
						UtilityFile.getCodeBasePath()
								+ UtilityFile.getGridUploadProperty("Grid.File.Location.BasePath"),
						new File(gridModelObj.getFilePath()),
						UtilityFile.getGridUploadProperty("Grid.File.Processed.Path")
								+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY)
										.toString()
								+ UtilityFile.getGridUploadProperty("Grid.File.Location.ErrorPath"));
				gridModelObj.setFilePath(newFilePath);
				gridModelObj.setIsValidated("Y");
				gridModelObj.setIsProcessed("Y");
				gridAutomationService.save(gridModelObj);
				}
			}
			
			ErrorInfo errInfo = new ErrorInfo();
			errInfo.setTransactionId(transactionInfo.getId());
			if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT).toString().length() > 1000)
				errInfo.setErrorMessage(exchange.getProperty(Exchange.EXCEPTION_CAUGHT).toString().substring(0, 1000));
			else
				errInfo.setErrorMessage(exchange.getProperty(Exchange.EXCEPTION_CAUGHT).toString());
			errorInfoService.save(errInfo);
		}
		
	}

}
