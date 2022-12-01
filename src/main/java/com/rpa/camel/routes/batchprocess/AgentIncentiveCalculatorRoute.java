/*package com.rpa.camel.routes.batchprocess;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.AgentIncentiveCalculator;
import com.rpa.camel.processors.batchprocess.AgentIncentiveFileReaderProcessor;
import com.rpa.camel.processors.batchprocess.AgentIncentiveFileUploadProcessor;
import com.rpa.camel.processors.batchprocess.AgentIncentiveFileValidatorProcessor;
import com.rpa.camel.processors.exception.GridUploadException;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class AgentIncentiveCalculatorRoute extends RouteBuilder {
	
	
	private String routeId = "AgentIncentiveCalculatorProcess";

	@Override
	public void configure() throws Exception {
		onException(Exception.class).handled(false).process(new GridUploadException()).bean(TransactionInfoServiceImpl.class,"updateGridTransactionInfo").end();
		from("quartz2:"+routeId+"?stateful=true&cron=0+0/2+7-6+?+*+*+*").autoStartup(false)
		.routeId(routeId)
		.routeDescription("Agent Incentive Calculation")
		.bean(TransactionInfoServiceImpl.class, "insertGridTransactionInfo")
		.process(new AgentIncentiveFileReaderProcessor())
		.process(new AgentIncentiveFileValidatorProcessor())
		.bean(TransactionInfoServiceImpl.class, "updateGridTransactionInfo")
		.process(new AgentIncentiveFileUploadProcessor())
		.bean(TransactionInfoServiceImpl.class, "updateGridTransactionInfo")
		.process(new AgentIncentiveCalculator())
		.bean(TransactionInfoServiceImpl.class, "updateGridTransactionInfo")
		.end();
		
	}

}
*/