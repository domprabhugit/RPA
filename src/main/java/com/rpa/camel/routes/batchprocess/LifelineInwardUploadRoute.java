/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
/*
package com.rpa.camel.routes.batchprocess;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.rpa.camel.processors.batchprocess.LifeLineInwardUploadConverter;
import com.rpa.camel.processors.batchprocess.LifelineInwardUploadMailReader;
import com.rpa.camel.processors.batchprocess.LifelineInwardUploadSplitter;
import com.rpa.camel.processors.batchprocess.LifelineInwardUploadValidator;
import com.rpa.camel.processors.batchprocess.LifelineInwardUploader;
import com.rpa.camel.processors.common.ExceptionProcessor;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.service.TransactionInfoServiceImpl;
import com.rpa.util.UtilityFile;

@Component
public class LifelineInwardUploadRoute extends RouteBuilder {

	private String routeId = "IlongueLifelineInwardUpload";

	@Override
	public void configure() throws Exception {

		onException(Exception.class).handled(true).process(new ExceptionProcessor());
		from("quartz2:IlongueLifelineInwardUpload?stateful=true&cron=0+0/02+*+1/1+*+?").autoStartup(false)
				
				 from(
				 "quartz2:IlongueLifelineInwardUpload?stateful=true&cron=0+0/30+9-18+?+*+*+*"
				 ).autoStartup(false)
				 
				.routeId(routeId).routeDescription("Ilongue - Lifeline Inward Upload")
				.bean(TransactionInfoServiceImpl.class, "insertLifelineUploadTransactionInfo")
				.process(new LifelineInwardUploadMailReader()).process(new LifelineInwardUploadPreProcessor()).choice()
				.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
				.bean(TransactionInfoServiceImpl.class, "updateLifelineUploadTransactionInfo(true)").otherwise()
				.bean(TransactionInfoServiceImpl.class, "updateLifelineUploadTransactionInfo(false)")
				.process(new LifelineInwardUploadSplitter()).process(new LifelineInwardUploadPreProcessor()).choice()
				.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
				.bean(TransactionInfoServiceImpl.class, "updateLifelineUploadTransactionInfo(true)").otherwise()
				.bean(TransactionInfoServiceImpl.class, "updateLifelineUploadTransactionInfo(false)")
				.process(new LifelineInwardUploadValidator()).process(new LifelineInwardUploadPreProcessor()).choice()
				.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
				.bean(TransactionInfoServiceImpl.class, "updateLifelineUploadTransactionInfo(true)").otherwise()
				.bean(TransactionInfoServiceImpl.class, "updateLifelineUploadTransactionInfo(false)")
				.process(new LifeLineInwardUploadConverter()).process(new LifelineInwardUploadPreProcessor()).choice()
				.when(header(RPAConstants.PROCEED_FURTHER).isEqualTo(RPAConstants.N))
				.bean(TransactionInfoServiceImpl.class, "updateLifelineUploadTransactionInfo(true)").otherwise()
				.bean(TransactionInfoServiceImpl.class, "updateLifelineUploadTransactionInfo(false)")
				.process(new LifelineInwardUploader())
				.bean(TransactionInfoServiceImpl.class, "updateLifelineUploadTransactionInfo(true)").end();

	}

	public class LifelineInwardUploadPreProcessor implements Processor {

		public void process(Exchange exchange) {

			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
			if (transactionInfo.getProcessPhase().equalsIgnoreCase(RPAConstants.MAIL_READER)) {
				if (exchange.getIn().getHeader(RPAConstants.NO_MAIL) == null
						|| exchange.getIn().getHeader(RPAConstants.NO_MAIL).equals(RPAConstants.Y))
					exchange.getIn().setHeader(RPAConstants.PROCEED_FURTHER, RPAConstants.N);
				else
					exchange.getIn().setHeader(RPAConstants.PROCEED_FURTHER, RPAConstants.Y);
			} else {
				if (exchange.getIn().getHeader(RPAConstants.NO_FILE) == null
						|| exchange.getIn().getHeader(RPAConstants.NO_FILE).equals(RPAConstants.Y))
					exchange.getIn().setHeader(RPAConstants.PROCEED_FURTHER, RPAConstants.N);
				else
					exchange.getIn().setHeader(RPAConstants.PROCEED_FURTHER, RPAConstants.Y);
			}

			UtilityFile.printHeapDetails(transactionInfo.getProcessPhase());
		}

	}
}
*/