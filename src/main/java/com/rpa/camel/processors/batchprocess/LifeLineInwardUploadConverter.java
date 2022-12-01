/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.camel.processors.common.ExcelToCsvConverter;
import com.rpa.camel.processors.common.XlsxConverter;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.response.LongueUploadResponse;
import com.rpa.service.EmailService;
import com.rpa.util.UtilityFile;

public class LifeLineInwardUploadConverter implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(LifeLineInwardUploadConverter.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	private XlsxConverter xlsxConverter = new XlsxConverter();

	@Autowired
	EmailService emailService;

	private void AutoWiringBeanPropertiesSetMethod() {

		logger.info("AutoWiringBeanPropertiesSetMethod is Called in LifeLineInwardUploadConverter Class");
		applicationContext = SpringContext.getAppContext();
		emailService = applicationContext.getBean(EmailService.class);
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in LifeLineInwardUploadConverter Class");

	}

	@Override
	public void process(Exchange exchange) {
		logger.info("*********** inside Camel Process of LifeLineInwardUploadConverter Called ************");
		AutoWiringBeanPropertiesSetMethod();
		logger.info("BEGIN : LifeLineInwardUploadConverter - Started ");
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.CONVERSION);
		LifeLineInwardUploadConverter lifelineInwardFileProcess = new LifeLineInwardUploadConverter();
		lifelineInwardFileProcess.doProcess(exchange, transactionInfo, emailService);
		logger.info("*********** inside Camel Process of LifeLineInwardUploadConverter Ended ************");
	}

	public void doProcess(Exchange exchange, TransactionInfo transactionInfo, EmailService emailService) {
		logger.info("BEGIN : LifeLineInwardUploadConverter - doProcess Method Called  ");
		if (!getFilesToProcess(exchange, transactionInfo, RPAConstants.S, emailService)) {
			transactionInfo.setProcessStatus(RPAConstants.CONVERSION_ERROR);
		}
		getFilesToProcess(exchange, transactionInfo, RPAConstants.E, emailService);

		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		logger.info("BEGIN : LifeLineInwardUploadConverter - doProcess Method Ended  ");
	}

	private boolean getFilesToProcess(Exchange exchange, TransactionInfo transactionInfo, String flag,
			EmailService emailService) {
		logger.info("BEGIN : LifeLineInwardUploadConverter - getFilesToSplit Method Called  ");
		try {

			File sourceFolder = null, destFolder = null, errorExcelFolder = null, successExcelFolder = null;

			if (flag.equalsIgnoreCase(RPAConstants.S)) {
				sourceFolder = new File(UtilityFile.getCodeBasePath()
						+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_PROCESS)
						+ exchange.getProperty("fileName") + RPAConstants.SLASH + "Success");
				destFolder = new File(UtilityFile.getCodeBasePath()
						+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_PROCESS)
						+ exchange.getProperty("fileName") + RPAConstants.SLASH + "Success" + RPAConstants.SLASH
						+ "Csv");

				successExcelFolder = new File(UtilityFile.getCodeBasePath()
						+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_PROCESS)
						+ exchange.getProperty("fileName") + RPAConstants.SLASH + "Success" + RPAConstants.SLASH
						+ "Excel");

			} else {
				sourceFolder = new File(UtilityFile.getCodeBasePath()
						+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_PROCESS)
						+ exchange.getProperty("fileName") + RPAConstants.SLASH + "Error");
				destFolder = new File(UtilityFile.getCodeBasePath()
						+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_PROCESS)
						+ exchange.getProperty("fileName") + RPAConstants.SLASH + "Error" + RPAConstants.SLASH + "Csv");

				errorExcelFolder = new File(UtilityFile.getCodeBasePath()
						+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_PROCESS)
						+ exchange.getProperty("fileName") + RPAConstants.SLASH + "Error" + RPAConstants.SLASH
						+ "Excel");
			}

			File uploadCsvFolder = new File(UtilityFile.getCodeBasePath()
					+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_UPLOAD)
					+ exchange.getProperty("fileName"));

			logger.info("LifeLineInwardUploadConverter - sourceFolder path :: " + sourceFolder.getPath());

			logger.info("LifeLineInwardUploadConverter - destFolder path :: " + destFolder.getPath());

			if (successExcelFolder != null) {
				logger.info(
						"LifeLineInwardUploadConverter - successExcelFolder path :: " + successExcelFolder.getPath());
			}

			if (errorExcelFolder != null) {
				logger.info("LifeLineInwardUploadConverter - errorExcelFolder path :: " + errorExcelFolder.getPath());
			}

			if (errorExcelFolder != null) {
				logger.info("LifeLineInwardUploadConverter - uploadCsvFolder path :: " + uploadCsvFolder.getPath());
			}

			if (!sourceFolder.exists())
				sourceFolder.mkdirs();

			if (!uploadCsvFolder.exists())
				uploadCsvFolder.mkdirs();

			if (!destFolder.exists())
				destFolder.mkdirs();

			if (errorExcelFolder != null && errorExcelFolder.exists() != true)
				errorExcelFolder.mkdirs();

			if (successExcelFolder != null && successExcelFolder.exists() != true)
				successExcelFolder.mkdirs();

			boolean isFileAvailableToConvert = false;

			File[] directoryList = UtilityFile.getTheFilesInOlderOrder(sourceFolder.getPath(), "xlsx");

			logger.info("LifeLineInwardUploadConverter - directoryList length :: " + directoryList.length);

			int totalSuccessCount = 0, totalErrorRowCount = 0;
			StringBuffer csvBuffer = new StringBuffer();

			int fileCounter = 0;int currentRowCount = 0;
			boolean skipFirstRow = false;
			for (File file : directoryList) {

				if (file.canRead() && !file.isDirectory()) {

					logger.info("LifeLineInwardUploadConverter - file name :: " + file.getName());
					if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("xlsx")) {

						isFileAvailableToConvert = true;
						if (fileCounter > 0) {
							skipFirstRow = true;
						}
						LongueUploadResponse longueUploadResponse = ExcelToCsvConverter.getAsCsvString(
								new File(sourceFolder + RPAConstants.SLASH + file.getName()),
								RPAConstants.ILONGUE_FIELD_TERMINATER, RPAConstants.ILONGUE_LINE_TERMINATER,
								skipFirstRow);

						fileCounter++;
						currentRowCount = longueUploadResponse.getRowCount();

						if (flag.equalsIgnoreCase(RPAConstants.S)) {
							if (currentRowCount > 0) {
								currentRowCount = currentRowCount - 1;
								logger.info("LifeLineInwardUploadConverter - current success row count --"
										+ currentRowCount);
								totalSuccessCount = totalSuccessCount + currentRowCount;
							}
						} else {
							currentRowCount = currentRowCount - 1;
							logger.info("LifeLineInwardUploadConverter - current error row count --" + currentRowCount);
							totalErrorRowCount = totalErrorRowCount + currentRowCount;
						}

						csvBuffer.append(longueUploadResponse.getCsvBuffer());

						/* } */
					} else {
						logger.error(
								"LifeLineInwardUploadConverter - Invalid file extension expected xlsx but found :: "
										+ FilenameUtils.getExtension(file.getName()));
					}

				}

			}

			if (flag.equalsIgnoreCase(RPAConstants.S)) {
				logger.info(
						"LifeLineInwardUploadConverter - converted to csv format - no. of success lines converted :: "
								+ totalSuccessCount);
				transactionInfo.setTotalUploadRecords(String.valueOf(totalSuccessCount));
			} else {
				logger.info("LifeLineInwardUploadConverter - converted to csv format - no. of error lines converted :: "
						+ totalErrorRowCount);
			}

			FileOutputStream fos = null;

			if (flag.equalsIgnoreCase(RPAConstants.S)) {
				fos = new FileOutputStream(
						uploadCsvFolder + RPAConstants.SLASH + exchange.getProperty("fileName") + ".csv");
				fos.write(csvBuffer.toString().getBytes());
				fos.close();
				FileUtils.copyFileToDirectory(
						new File(uploadCsvFolder + RPAConstants.SLASH + exchange.getProperty("fileName") + ".csv"),
						destFolder);

				xlsxConverter.convertToXlsx(destFolder + RPAConstants.SLASH + exchange.getProperty("fileName") + ".csv",
						successExcelFolder.getPath() + RPAConstants.SLASH + exchange.getProperty("fileName") + ".xlsx",
						RPAConstants.ILONGUE_FIELD_TERMINATER, RPAConstants.ILONGUE_LINE_TERMINATER);

				transactionInfo.setSuccessFileDownload(UtilityFile.getCodeBasePath()
						+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_PROCESS)
						+ exchange.getProperty("fileName") + RPAConstants.SLASH + "Success" + RPAConstants.SLASH
						+ "Excel" + RPAConstants.SLASH + exchange.getProperty("fileName") + ".xlsx");
			} else {
				fos = new FileOutputStream(destFolder + RPAConstants.SLASH + exchange.getProperty("fileName") + ".csv");
				fos.write(csvBuffer.toString().getBytes());
				fos.close();

				if (totalErrorRowCount > 0) {
					/*
					 * conversion of single csv to xlsx and sent the same as
					 * attachment to ilongue user
					 */
					xlsxConverter.convertToXlsx(
							destFolder + RPAConstants.SLASH + exchange.getProperty("fileName") + ".csv",
							errorExcelFolder + RPAConstants.SLASH + exchange.getProperty("fileName") + ".xlsx",
							RPAConstants.ILONGUE_FIELD_TERMINATER, RPAConstants.ILONGUE_LINE_TERMINATER);

					transactionInfo.setErrorFileDownload(UtilityFile.getCodeBasePath()
							+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_PROCESS)
							+ exchange.getProperty("fileName") + RPAConstants.SLASH + "Error" + RPAConstants.SLASH
							+ "Excel" + RPAConstants.SLASH + exchange.getProperty("fileName") + ".xlsx");

					emailService.lifeLineInwardUplaodNotification(UtilityFile.getCodeBasePath()
							+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_PROCESS)
							+ exchange.getProperty("fileName") + RPAConstants.SLASH + "Error" + RPAConstants.SLASH
							+ "Excel" + RPAConstants.SLASH + exchange.getProperty("fileName") + ".xlsx", exchange,
							transactionInfo);
				}
			}

			if (flag.equalsIgnoreCase(RPAConstants.S)) {
				if (!isFileAvailableToConvert) {
					transactionInfo.setProcessStatus(RPAConstants.NO_FILE);
					logger.info("LifelineInwardUploadValidator  - No Files in process folder to convert");
					exchange.getIn().setHeader(RPAConstants.NO_FILE, RPAConstants.Y);
				} else {
					transactionInfo.setProcessStatus(RPAConstants.FILE_CONVERTED);
					exchange.getIn().setHeader(RPAConstants.NO_FILE, RPAConstants.N);
				}
			}
			logger.info("END : LifeLineInwardUploadConverter - getFilesToSplit Method end");
			return true;
		} catch (Exception e) {
			logger.info("Error in LifeLineInwardUploadConverter - doProcess Method Ended  " , e);
			return false;
		}
	}
}
