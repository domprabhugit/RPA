package com.rpa.camel.routes.batchprocess;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.GridMasterFileReaderProcessor;
import com.rpa.camel.processors.batchprocess.GridMasterFileUploadProcessor;
import com.rpa.camel.processors.batchprocess.GridMasterFileValidatorProcessor;
import com.rpa.camel.processors.exception.GridUploadException;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class GridMasterUploadRoute extends RouteBuilder {
	
	
	private String routeId = "GridMasterUploadProcess";

	@Override
	public void configure() throws Exception {
		onException(Exception.class).handled(false).process(new GridUploadException()).bean(TransactionInfoServiceImpl.class,"updateGridTransactionInfo").end();
		from("quartz2:GridMasterUploadProcessRoute?stateful=true&cron=0+0/5+10-23+?+*+*+*").autoStartup(false)
		.routeId(routeId)
		.routeDescription("Grid Master  Data Upload")
		.bean(TransactionInfoServiceImpl.class, "insertGridTransactionInfo")
		.process(new GridMasterFileReaderProcessor())
		.process(new GridMasterFileValidatorProcessor())
		.bean(TransactionInfoServiceImpl.class, "updateGridTransactionInfo")
		.process(new GridMasterFileUploadProcessor())
		.bean(TransactionInfoServiceImpl.class, "updateGridTransactionInfo")
		.end();
		
	}

}
