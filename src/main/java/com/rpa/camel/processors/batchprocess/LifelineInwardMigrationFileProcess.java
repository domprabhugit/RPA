/*
 * Robotic Process Automation
 * @originalAuthor Vijayananth.M
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.LifeLineMigration;
import com.rpa.service.TransactionInfoService;
import com.rpa.service.processors.LifelineMigrationService;
import com.rpa.util.UtilityFile;

public class LifelineInwardMigrationFileProcess implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(LifelineInwardMigrationFileProcess.class.getName());

	private DataFormatter formatter = new DataFormatter();

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	LifelineMigrationService lifelineMigrationService;

	@Autowired
	private TransactionInfoService transactionInfoService;

	private void AutoWiringBeanPropertiesSetMethod() {

		logger.info("AutoWiringBeanPropertiesSetMethod is Called in LifelineInwardMigrationFileProcess Class");
		applicationContext = SpringContext.getAppContext();
		lifelineMigrationService = applicationContext.getBean(LifelineMigrationService.class);
		transactionInfoService = applicationContext.getBean(TransactionInfoService.class);
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in LifelineInwardMigrationFileProcess Class");

	}

	@Override
	public void process(Exchange exchange) {
		if (exchange.getIn().getHeader(RPAConstants.NO_MAIL) != null
				&& !exchange.getIn().getHeader(RPAConstants.NO_MAIL).equals(RPAConstants.Y)) {
			AutoWiringBeanPropertiesSetMethod();
			logger.info("*********** inside Camel Process of LifelineInwardMigrationFile Called ************");
			logger.info("BEGIN : LifelineInwardMigrationFile Processor - Started ");
			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
			LifelineInwardMigrationFileProcess lifeLineUploadProcessorObj = new LifelineInwardMigrationFileProcess();
			lifeLineUploadProcessorObj.doProcess(lifeLineUploadProcessorObj, lifelineMigrationService, exchange,
					transactionInfoService, transactionInfo);
			logger.info("*********** inside Camel Process of LifelineInwardMigrationFile Processor Ended ************");
		}
	}

	public void doProcess(LifelineInwardMigrationFileProcess lifelineInwardMailReader,
			LifelineMigrationService lifelineMigrationService, Exchange exchange,
			TransactionInfoService transactionInfoService, TransactionInfo transactionInfo) {
		logger.info("BEGIN : LifelineInwardMigrationFile Processor - doProcess Method Called  ");
		transactionInfo.setProcessPhase(RPAConstants.FILE_PROCESSOR);
		if (getFilesToProcess(lifelineMigrationService, exchange, transactionInfoService, transactionInfo)) {

		} else {
			transactionInfo.setProcessStatus(RPAConstants.FILE_PROCESSOR_ERROR);
		}
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		logger.info("BEGIN : LifelineInwardMigrationFile Processor - doProcess Method Ended  ");
	}

	private boolean getFilesToProcess(LifelineMigrationService lifelineMigrationService, Exchange exchange,
			TransactionInfoService transactionInfoService, TransactionInfo transactionInfo) {
		logger.info("BEGIN : LifelineInwardMigrationFile Processor - getFilesToSplit Method Called  ");
		try {

			File processFolder = new File(UtilityFile.getCodeBasePath()
					+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_MIGRRATION_FOLDER_PROCESS));

			File processedFolder = new File(UtilityFile.getCodeBasePath()
					+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_MIGRRATION_FOLDER_PROCESSED));

			if (!processFolder.exists())
				processFolder.mkdirs();

			if (!processedFolder.exists())
				processedFolder.mkdirs();
			boolean isFileAvailableToProcess = false;
			File[] directoryList = processFolder.listFiles();
			if (directoryList != null) {
				for (File file : directoryList) {

					if (file.canRead() && !file.isDirectory()
							&& (file.getName().equalsIgnoreCase(transactionInfo.getFirstgenFileName())
									|| file.getName().equalsIgnoreCase(transactionInfo.getIlongueFileName()))) {
						isFileAvailableToProcess = true;
						if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("xls")) {
							logger.info(
									"LifelineInwardMigrationFile Processor - Processing file --> " + file.getName());
							HSSFWorkbook input_workbook;
							HSSFSheet input_workbook_sheet;
							HSSFRow row_1_index;
							input_workbook = new HSSFWorkbook(new FileInputStream(file));

							input_workbook_sheet = input_workbook.getSheetAt(0);
							int input_workbook_sheet_total_row = input_workbook_sheet.getLastRowNum();
							LifeLineMigration lifelineMigrationObj = new LifeLineMigration();
							String appType = "";
							for (int input_workbook_sheet_row_1 = input_workbook_sheet_total_row; input_workbook_sheet_row_1 >= 1; input_workbook_sheet_row_1--) {
								row_1_index = input_workbook_sheet.getRow(input_workbook_sheet_row_1);

								int input_workbook_sheet_total_column = row_1_index.getLastCellNum();
								String countDate = "", fileType = "";
								for (int input_workbook_sheet_column = 0; input_workbook_sheet_column < input_workbook_sheet_total_column; input_workbook_sheet_column++) {
									row_1_index.getCell(0);

									if (input_workbook_sheet_column == 0) {
										countDate = formatter
												.formatCellValue(row_1_index.getCell(input_workbook_sheet_column));
										if (input_workbook_sheet_row_1 == input_workbook_sheet_total_row) {
											Date dateObj = null;
											if (file.getName().startsWith(RPAConstants.FIRSTGEN)) {
												appType = RPAConstants.F;
												dateObj = new SimpleDateFormat("dd-MMM-yy").parse(countDate);
											} else {
												appType = RPAConstants.I;
												dateObj = new SimpleDateFormat("MM/dd/yy").parse(countDate);
											}
											lifelineMigrationObj.setCountDate(dateObj);

											transactionInfoService.inactiveOldTransactions(exchange, dateObj);
											transactionInfo.setMigrationDate(dateObj);
											transactionInfo.setMigrationStatus(RPAConstants.P);
											transactionInfo.setStatus(RPAConstants.Y);

											List<LifeLineMigration> list = lifelineMigrationService
													.findByCountDateAndType(dateObj, appType);
											if (list.size() > 0) {
												logger.info(
														"LifelineInwardMigrationFile Processor - Already Migration data available in DB for  --> "
																+ countDate + " for Application type --> " + appType);
												for (LifeLineMigration obj : list) {
													lifelineMigrationObj = obj;
												}
											}
										}
									}
									if (input_workbook_sheet_column == 1) {
										fileType = formatter
												.formatCellValue(row_1_index.getCell(input_workbook_sheet_column));
									}
									if (input_workbook_sheet_column == 2) {
										int currnetCountValue = Integer.valueOf(formatter
												.formatCellValue(row_1_index.getCell(input_workbook_sheet_column)));

										if (file.getName().startsWith(RPAConstants.FIRSTGEN)) {
											appType = RPAConstants.F;
											lifelineMigrationObj = setFirstgenCounts(fileType, currnetCountValue,
													appType, lifelineMigrationObj);
										} else if (file.getName().startsWith(RPAConstants.ILONGUE)) {
											appType = RPAConstants.I;
											lifelineMigrationObj = setILongueCounts(fileType, currnetCountValue,
													appType, lifelineMigrationObj);
										}
									}

								}

							}
							lifelineMigrationObj.setAppType(appType);
							lifelineMigrationObj.setIsCompared(RPAConstants.N);
							lifelineMigrationObj.setTransactionInfoId(transactionInfo.getId());
							LifeLineMigration savedObj = lifelineMigrationService.save(lifelineMigrationObj);
							if (savedObj != null) {
								logger.info(
										"LifelineInwardMigrationFile Processor - Saved Migration data in DB Dated on :: "
												+ savedObj.getCountDate() + " for Application type :: " + appType);
							}

							input_workbook.close();

						} else {
							XSSFWorkbook input_workbook;
							input_workbook = new XSSFWorkbook(new FileInputStream(file));
							XSSFSheet input_workbook_sheet;
							XSSFRow row_1_index;

							input_workbook_sheet = input_workbook.getSheetAt(0);
							int input_workbook_sheet_total_row = input_workbook_sheet.getLastRowNum();
							LifeLineMigration lifelineMigrationObj = new LifeLineMigration();
							String appType = "";
							for (int input_workbook_sheet_row_1 = input_workbook_sheet_total_row; input_workbook_sheet_row_1 >= 1; input_workbook_sheet_row_1--) {
								row_1_index = input_workbook_sheet.getRow(input_workbook_sheet_row_1);

								int input_workbook_sheet_total_column = row_1_index.getLastCellNum();
								String countDate = "", fileType = "";
								for (int input_workbook_sheet_column = 0; input_workbook_sheet_column < input_workbook_sheet_total_column; input_workbook_sheet_column++) {
									// row_1_index.getCell(3);

									if (input_workbook_sheet_column == 3) {
										countDate = formatter
												.formatCellValue(row_1_index.getCell(input_workbook_sheet_column));
										if (input_workbook_sheet_row_1 == input_workbook_sheet_total_row) {
											Date dateObj = null;
											if (file.getName().startsWith(RPAConstants.FIRSTGEN)) {
												appType = RPAConstants.F;
												dateObj = new SimpleDateFormat("dd-MMM-yy").parse(countDate);

											} else {
												appType = RPAConstants.I;
												dateObj = new SimpleDateFormat("MM/dd/yy").parse(countDate);
											}

											lifelineMigrationObj.setCountDate(dateObj);
											List<LifeLineMigration> list = lifelineMigrationService
													.findByCountDateAndType(dateObj, appType);
											if (list.size() > 0) {
												logger.info(
														"LifelineInwardMigrationFile Processor - Already Migration data available in DB for  --> "
																+ countDate + " for Application type --> " + appType);
												for (LifeLineMigration obj : list) {
													lifelineMigrationObj = obj;
												}
											}
										}
									}

									if (input_workbook_sheet_column == 1) {
										fileType = formatter
												.formatCellValue(row_1_index.getCell(input_workbook_sheet_column));
									}
									if (input_workbook_sheet_column == 2) {

										int currnetCountValue = Integer.valueOf(formatter
												.formatCellValue(row_1_index.getCell(input_workbook_sheet_column)));
										if (file.getName().startsWith(RPAConstants.FIRSTGEN)) {
											appType = RPAConstants.F;
											lifelineMigrationObj = setFirstgenCounts(fileType, currnetCountValue,
													appType, lifelineMigrationObj);
										} else if (file.getName().startsWith(RPAConstants.ILONGUE)) {
											appType = RPAConstants.I;
											lifelineMigrationObj = setILongueCounts(fileType, currnetCountValue,
													appType, lifelineMigrationObj);
										}
									}

								}

							}
							lifelineMigrationObj.setAppType(appType);
							lifelineMigrationObj.setIsCompared(RPAConstants.N);
							lifelineMigrationObj.setTransactionInfoId(transactionInfo.getId());
							LifeLineMigration savedObj = lifelineMigrationService.save(lifelineMigrationObj);
							if (savedObj != null) {
								logger.info(
										"LifelineInwardMigrationFile Processor - Saved Migration data in DB Dated on :: "
												+ savedObj.getCountDate() + " for Application type :: " + appType);
							}

							input_workbook.close();
						}

						file.renameTo(
								new File(UtilityFile.getCodeBasePath()
										+ UtilityFile.getIlongueProperty(
												RPAConstants.ILONGUE_LIFELINE_MIGRRATION_FOLDER_PROCESSED)
										+ file.getName()));
						logger.info("LifelineInwardMigrationFile Processor - " + file.getName()
								+ " moved to processed folder ");
					}

				}
			}
			if (!isFileAvailableToProcess) {
				transactionInfo.setProcessStatus(RPAConstants.NO_FILE);
				logger.info("LifelineInwardMigrationFile Processor - No Files in process folder to process");
				exchange.getIn().setHeader(RPAConstants.NO_FILE, RPAConstants.Y);
			} else {
				transactionInfo.setProcessStatus(RPAConstants.FILE_PROCESSED);
				exchange.getIn().setHeader(RPAConstants.NO_FILE, RPAConstants.N);
			}
			return true;
		} catch (Exception e) {
			logger.info("Error in LifelineInwardMigrationFile Processor - doProcess Method Ended  " + e.getMessage());
			return false;
		}
	}

	private LifeLineMigration setFirstgenCounts(String fileType, int currnetCountValue, String appType,
			LifeLineMigration lifelineMigrationObj) {

		switch (fileType) {
		case "Approved":
			lifelineMigrationObj.setApprovedCount(currnetCountValue);
			break;
		case "RECEIPT":
			lifelineMigrationObj.setReceiptCount(currnetCountValue);
			break;
		case "Motor Inward":
			lifelineMigrationObj.setMobileInwardCount(currnetCountValue);
			break;
		case "Pipe Line":
			lifelineMigrationObj.setPipelineCount(currnetCountValue);
			break;
		case "Renewal Policy":
			lifelineMigrationObj.setRenewalPolicyCount(currnetCountValue);
			break;
		case "Health Claims":
			lifelineMigrationObj.setHealthclaims(currnetCountValue);
			break;
		case "MOTOR CLAIMS":
			lifelineMigrationObj.setMotorClaims(currnetCountValue);
			break;
		case "Cancelled":
			lifelineMigrationObj.setCancelledCount(currnetCountValue);
			break;
		case "Life Line Inward":
			lifelineMigrationObj.setLifelineInward(currnetCountValue);
			break;
		case "Health Inward":
			lifelineMigrationObj.setHealthInwardCount(currnetCountValue);
			break;
		case "Xgen Health Claims":
			lifelineMigrationObj.setXgenHealthclaims(currnetCountValue);
			break;
		case "Licence Agent":
			lifelineMigrationObj.setLicenseAgent(currnetCountValue);
			break;
		}
		return lifelineMigrationObj;
	}

	private LifeLineMigration setILongueCounts(String fileType, int currnetCountValue, String appType,
			LifeLineMigration lifelineMigrationObj) {

		switch (fileType) {
		case "AP":
			lifelineMigrationObj.setApprovedCount(currnetCountValue);
			break;
		case "RB":
			lifelineMigrationObj.setReceiptCount(currnetCountValue);
			break;
		case "MI":
			lifelineMigrationObj.setMobileInwardCount(currnetCountValue);
			break;
		case "MP":
			lifelineMigrationObj.setPipelineCount(currnetCountValue);
			break;
		case "MR":
			lifelineMigrationObj.setRenewalPolicyCount(currnetCountValue);
			break;
		case "MA":
			lifelineMigrationObj.setHealthclaims(currnetCountValue);
			break;
		case "MC":
			lifelineMigrationObj.setMotorClaims(currnetCountValue);
			break;
		case "CAN":
			lifelineMigrationObj.setCancelledCount(currnetCountValue);
			break;
		case "HLLI":
			lifelineMigrationObj.setLifelineInward(currnetCountValue);
			break;
		case "HI":
			lifelineMigrationObj.setHealthInwardCount(currnetCountValue);
			break;
		case "FC":
			lifelineMigrationObj.setXgenHealthclaims(currnetCountValue);
			break;
		case "NL":
			lifelineMigrationObj.setLicenseAgent(currnetCountValue);
			break;
		}
		return lifelineMigrationObj;
	}

}
