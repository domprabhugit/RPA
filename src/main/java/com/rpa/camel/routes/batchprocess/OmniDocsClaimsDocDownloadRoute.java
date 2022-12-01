/*
 * Robotic Process Automation
 * @originalAuthor Vijayananth M
 * Copyright(c) 2017, www.tekplay.com
 */
/*
package com.rpa.camel.routes.batchprocess;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.OmniDocsClaimsDocDownloader;
import com.rpa.camel.processors.exception.ClaimsDownloadException;
import com.rpa.service.TransactionInfoServiceImpl;

@Component
public class OmniDocsClaimsDocDownloadRoute extends RouteBuilder {

	private String routeId = "OmniDocClaimsDocDownload";

	@Override
	public void configure() throws Exception {
		onException(Exception.class).handled(false).process(new ClaimsDownloadException()).bean(TransactionInfoServiceImpl.class,"updateClaimsDownloadTransactionInfo").end();
		from("quartz2:"+routeId+"?stateful=true&cron=0+0+9-18+?+*+*+*").autoStartup(false)
		from("quartz2:"+routeId+"?stateful=true&cron=0+0/2+*+1/1+*+?").autoStartup(false)
		.routeId(routeId)
		.routeDescription("Omni Doc Claims Download")
		.bean(TransactionInfoServiceImpl.class, "insertClaimsDownloadTransactionInfo")
		.process(new OmniDocsClaimsDocDownloader())
		.bean(TransactionInfoServiceImpl.class, "updateClaimsDownloadTransactionInfo")
		.end();
	}
}*/