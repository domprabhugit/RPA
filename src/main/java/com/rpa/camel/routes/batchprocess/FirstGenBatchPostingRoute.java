/*
 * Robotic Process Automation
 * @originalAuthor Vijayananth M
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.routes.batchprocess;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.FirstGenProcessor;
import com.rpa.camel.processors.batchprocess.FirstGenReprocessor;
import com.rpa.camel.processors.exception.FirstGenException;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class FirstGenBatchPostingRoute extends RouteBuilder {

	private String routeId = "FirstGenBatchProcess";

	@Override
	public void configure() throws Exception {

		onException(Exception.class).handled(false).process(new FirstGenException()).bean(TransactionInfoServiceImpl.class,"updateFirstGenTransactionInfo").end();
		/*from("quartz2:BatchProcessRoute?stateful=true&cron=0+0/15+9-20+?+*+*+*").autoStartup(false)*/
		from("quartz2:BatchProcessRoute?stateful=true&cron=0+0/15+9-20+?+*+*+*").autoStartup(true)
		//from("quartz2:BatchProcessRoute?stateful=true&cron=0+0/5+9-8+?+*+*+*").autoStartup(false)
		/*from("quartz2:BatchProcessRoute?stateful=true&cron=0+0/2+*+1/1+*+?").autoStartup(false)*/
		.routeId(routeId)
		.routeDescription("Batch Process For GL Posting")
		.bean(TransactionInfoServiceImpl.class, "insertTransactionInfo")
		.process(new FirstGenReprocessor())
		.process(new FirstGenProcessor())
		.bean(TransactionInfoServiceImpl.class, "updateFirstGenTransactionInfo")
		.end();
	}
}