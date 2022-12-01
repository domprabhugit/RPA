package com.rpa.camel.routes.batchprocess;
/*
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.ModelCreationFileReaderProcessor;
import com.rpa.camel.processors.batchprocess.ModelCreationFileProcessor;
import com.rpa.camel.processors.exception.GridUploadException;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class ModelCodeCreationRoute extends RouteBuilder {
	
	
	private String routeId = "ModelCodeCreationProcess";

	@Override
	public void configure() throws Exception {
		onException(Exception.class).handled(false).process(new GridUploadException()).bean(TransactionInfoServiceImpl.class,"updateModelCreationTransactionInfo").end();
		from("quartz2:"+routeId+"?stateful=true&cron=0+0/02+*+1/1+*+?").autoStartup(false)
		from("quartz2:"+routeId+"?stateful=true&cron=0+0/5+7-22+?+*+*+*").autoStartup(false)
		from("quartz2:"+routeId+"?stateful=true&cron=0+0/2+0-23+?+*+*+*").autoStartup(true)
		.routeId(routeId)
		.routeDescription("Model Code Creation")
		.bean(TransactionInfoServiceImpl.class, "insertModelCreationTransactionInfo")
		.process(new ModelCreationFileReaderProcessor())
		.process(new ModelCreationFileProcessor())
		.bean(TransactionInfoServiceImpl.class, "updateModelCreationTransactionInfo")
		.end();
		
	}

}
*/