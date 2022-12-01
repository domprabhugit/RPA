/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.File;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.camel.processors.common.Splitter;
import com.rpa.util.UtilityFile;

public class LifelineInwardUploadSplitter implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(LifelineInwardUploadSplitter.class.getName());

	@SuppressWarnings("unused")
	private static final int BUFFER_SIZE = 4096;

	@Override
	public void process(Exchange exchange) {
		logger.info("*********** inside Camel Process of LifelineInwardUploadSplitter Called ************");
		logger.info("BEGIN : LifelineInwardUploadSplitter Processor - Started ");
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.SPLITTER);
		LifelineInwardUploadSplitter lifeLineUploadProcessorObj = new LifelineInwardUploadSplitter();
		lifeLineUploadProcessorObj.doProcess(lifeLineUploadProcessorObj, exchange, transactionInfo);
		logger.info("*********** inside Camel Process of LifelineInwardUploadSplitter Processor Ended ************");
	}

	public void doProcess(LifelineInwardUploadSplitter lifelineInwardMailReader, Exchange exchange,
			TransactionInfo transactionInfo) {
		logger.info("BEGIN : LifelineInwardUploadSplitter Processor - doProcess Method Called  ");
		if (!getFilesToSplit(exchange, transactionInfo)) {
			transactionInfo.setProcessStatus(RPAConstants.SPLIT_ERROR);
		}
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		logger.info("END : LifelineInwardUploadSplitter Processor - doProcess Method Ended  ");
	}

	private boolean getFilesToSplit(Exchange exchange, TransactionInfo transactionInfo) {
		logger.info("BEGIN : LifelineInwardUploadSplitter Processor - getFilesToSplit Method Called  ");
		try {

			boolean isFileAvailableToSplit = false;
			Integer[] dateCoumnIndex = { 16, 18, 20, 25, 26, 32, 34, 39, 47 };
			String[] headers = { "PROPOSARCODE", "FIRSTNAME", "LASTNAME", "CITY", "STATE", "BRANCH",
					"AGENTCODE", "AGENTNAME", "SUBLINE", "PRODUCTNAME", "TRANSACTIONTYPE", "POLICYCODE",
					"QUOTENUMBER", "INWARDCODE", "INWARDPREMIUM", "CURRENT_USER", "CREATIONDATE",
					"CREATEDBY", "COMMENTSDATE", "CHEQUE_NUMBER", "CHEQUE_DATE", "CHEQUE_AMOUNT",
					"CHEQUE_BRANCH", "CHEQUE_BANK", "CASHAMT", "POLICY_START_DATE", "POLICY_END_DATE",
					"PROPOSALFORMNUMBER", "SMNAME", "SMCODE", "BRANCH_NAME", "OACODE", "POLICYPOSTEDDATE",
					"NETPREMIUM", "RECEIPT_DATE", "PAYMENTMODE", "ROWN", "CONS_RECEIPT", "RECP_AMOUNT",
					"INWARDDATE", "INFO_REASON", "COMMENTS", "STATUS", "SHORTFALL", "Channel_New",
					"REFERRAL_TO", "REFERRAL_ACTIONED", "Referral Date ( UW )", "UW STATUS", "Decision",
					"Remarks", "Actionable", "Contactable/ Non- Contactable of PPMC",
					"Sub Remarks of PPMC" };

			File SplitFolder = new File(UtilityFile.getCodeBasePath()
					+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_TO_B_SPLITTED)
					+ exchange.getProperty("fileName"));

			File processFolder = new File(UtilityFile.getCodeBasePath()
					+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_PROCESS)
					+ exchange.getProperty("fileName"));

			if (!SplitFolder.exists())
				SplitFolder.mkdirs();

			if (!processFolder.exists())
				processFolder.mkdirs();

			File[] directoryList = SplitFolder.listFiles();
			if (directoryList != null) {

				for (File file : directoryList) {

					if (file.canRead() && !file.isDirectory()) {
						logger.info("LifelineInwardUploadSplitter Processor - getFilesToSplit() file name ::  "
								+ file.getName());
						isFileAvailableToSplit = true;

						new Splitter(
								UtilityFile.getCodeBasePath()
										+ UtilityFile
												.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_TO_B_SPLITTED)
										+ exchange.getProperty("fileName") + RPAConstants.SLASH + file.getName(),
								2500,
								UtilityFile.getCodeBasePath()
										+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_PROCESS)
										+ exchange.getProperty("fileName"),
								headers, dateCoumnIndex);

						logger.info(
								"LifelineInwardUploadSplitter Processor - getFilesToSplit() split done fot the file ::  "
										+ file.getName());
					}

				}

				if (!isFileAvailableToSplit) {
					transactionInfo.setProcessStatus(RPAConstants.NO_FILE);
					logger.info("LifelineInwardUploadValidator  - No Files in split folder to split");
					exchange.getIn().setHeader(RPAConstants.NO_FILE, RPAConstants.Y);
				} else {
					transactionInfo.setProcessStatus(RPAConstants.FILE_SPLITTED);
					exchange.getIn().setHeader(RPAConstants.NO_FILE, RPAConstants.N);
					logger.info("LifelineInwardUploadValidator  - File Split done");
				}
			}
			logger.info("END - LifelineInwardUploadSplitter Processor - getFilesToSplit() method ended");
			return true;
		} catch (Exception e) {
			logger.info("Error in LifelineInwardUploadSplitter Processor - doProcess Method Ended  ", e);
			return false;
		}
	}

}
