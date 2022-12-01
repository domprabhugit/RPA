/*package com.rpa.camel.routes.batchprocess;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.GridWithModelSheetMasterFileReaderProcessor;
import com.rpa.camel.processors.batchprocess.GridWithModelSheetMasterFileUploadProcessor;
import com.rpa.camel.processors.batchprocess.GridWithModelSheetMasterFileValidatorProcessor;
import com.rpa.camel.processors.exception.GridUploadException;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class GridWithModelSheetMasterUploadRoute extends RouteBuilder {
	
	
	private String routeId = "GridWithModelSheetMasterUploadProcess";

	@Override
	public void configure() throws Exception {
		onException(Exception.class).handled(false).process(new GridUploadException()).bean(TransactionInfoServiceImpl.class,"updateGridTransactionInfo").end();
		from("quartz2:GridWithModelSheetMasterUploadRoute?stateful=true&cron=0+0/5+10-23+?+*+*+*").autoStartup(true)
		.routeId(routeId)
		.routeDescription("Grid with model sheet Master  Data Upload")
		.bean(TransactionInfoServiceImpl.class, "insertGridTransactionInfo")
		.process(new GridWithModelSheetMasterFileReaderProcessor())
		.process(new GridWithModelSheetMasterFileValidatorProcessor())
		.bean(TransactionInfoServiceImpl.class, "updateGridTransactionInfo")
		.process(new GridWithModelSheetMasterFileUploadProcessor())
		.bean(TransactionInfoServiceImpl.class, "updateGridTransactionInfo")
		.end();
		
	}

}
*/