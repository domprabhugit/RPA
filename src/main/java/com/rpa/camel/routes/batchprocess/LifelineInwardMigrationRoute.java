/*
 * Robotic Process Automation
 * @originalAuthor S.Mohamed Ismaiel
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.routes.batchprocess;  

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.common.ExceptionProcessor;

@Component
public class LifelineInwardMigrationRoute extends RouteBuilder {

	@SuppressWarnings("unused")
	private String routeId = "LifelineMigrationProcess";

	@Override
	public void configure() throws Exception {
		
		onException(Exception.class).handled(true).process(new ExceptionProcessor());

	/*	from("quartz2:LifelineInwardMigrationRoute?cron=0+0/05+*+1/1+*+?")
		.routeId(routeId)
		.routeDescription("Ilongue - Lifeline Migration")
		.bean(TransactionInfoServiceImpl.class,"insertMigrationTransactionInfo")
		.process(new LifelineInwardMigrationMailReader())
		.process(new LifelineInwardMigrationFileProcess())
		.process(new LifelineInwardMigrationComparator())
		.bean(TransactionInfoServiceImpl.class,"updateMigrationTransactionInfo")
		.end();*/
		
	}	
		
}