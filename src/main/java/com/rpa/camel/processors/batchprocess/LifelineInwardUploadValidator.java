/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.camel.processors.common.ExcelCopyRowWithRemarks;
import com.rpa.constants.ErrorConstants;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.service.EmailService;
import com.rpa.util.UtilityFile;

public class LifelineInwardUploadValidator implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(LifelineInwardUploadValidator.class.getName());

	private DataFormatter formatter = new DataFormatter();

	private ExcelCopyRowWithRemarks excelCopyRowWithRemarks = new ExcelCopyRowWithRemarks();

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	EmailService emailService;

	private void AutoWiringBeanPropertiesSetMethod() {

		logger.info("AutoWiringBeanPropertiesSetMethod is Called in LifelineInwardUploadValidator Class");
		applicationContext = SpringContext.getAppContext();
		emailService = applicationContext.getBean(EmailService.class);
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in LifelineInwardUploadValidator Class");

	}

	@Override
	public void process(Exchange exchange) throws IOException {
		logger.info("*********** inside Camel Process of LifelineInwardUploadValidator Called ************");
			AutoWiringBeanPropertiesSetMethod();
			logger.info("*********** inside Camel Process of LifelineInwardUploadValidator Called ************");
			logger.info("BEGIN : LifelineInwardUploadValidator - Started ");
			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
			transactionInfo.setProcessPhase(RPAConstants.VALIDATION);
			LifelineInwardUploadValidator lifelineInwardFileProcess = new LifelineInwardUploadValidator();
			lifelineInwardFileProcess.doProcess(transactionInfo, exchange, emailService);
			logger.info("*********** inside Camel Process of LifelineInwardUploadValidator Ended ************");
	}

	public void doProcess(TransactionInfo transactionInfo, Exchange exchange, EmailService emailService) throws IOException {
		logger.info("BEGIN : LifelineInwardUploadValidator - doProcess Method Called  ");
		if (!getFilesToProcess(transactionInfo, exchange, emailService)) {
			transactionInfo.setProcessStatus(RPAConstants.VALIDATOR_ERROR);
		}
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		logger.info("BEGIN : LifelineInwardUploadValidator - doProcess Method Ended  ");
	}

	private boolean getFilesToProcess(TransactionInfo transactionInfo, Exchange exchange, EmailService emailService) throws IOException {
		logger.info("BEGIN : LifelineInwardUploadValidator - getFilesToSplit Method Called  ");
		
		try {

			transactionInfo.setProcessPhase(RPAConstants.VALIDATION);
			
			Map<Integer, String> errorMap = new HashMap<Integer, String>();
			Map<Integer, String> successMap = new HashMap<Integer, String>();
			int input_workbook_sheet_total_row = 0;short totalColumnCount = 53;

			File processFolder = new File(UtilityFile.getCodeBasePath()
					+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_PROCESS)
					+ exchange.getProperty("fileName"));
			if (!processFolder.exists())
				processFolder.mkdirs();

			File successFolder = new File(UtilityFile.getCodeBasePath()
					+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_PROCESS)
					+ exchange.getProperty("fileName") + RPAConstants.SLASH + "Success");
			if (!successFolder.exists())
				successFolder.mkdirs();

			File errorFolder = new File(UtilityFile.getCodeBasePath()
					+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_PROCESS)
					+ exchange.getProperty("fileName") + RPAConstants.SLASH + "Error");
			if (!errorFolder.exists())
				errorFolder.mkdirs();

			boolean isFileAvailableToValidate = false;

			File[] directoryList = UtilityFile.getTheFilesInOlderOrder(processFolder.getPath(), "xlsx");

			logger.info("LifelineInwardUploadValidator - directoryList length :: " + directoryList.length);

			if (directoryList != null) {
				if (directoryList.length == 0) {
					exchange.getIn().setHeader(RPAConstants.NO_FILE, RPAConstants.Y);
					logger.info("LifelineInwardUploadValidator - No Files in process folder to process");
				}

				int successRowCount = 0, errorRowCount = 0, currentSuccessRowCount = 0, currentErrorRowCount = 0, totalRecords = 0;
				for (File file : directoryList) {

					if (file.canRead() && !file.isDirectory()) {

						if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("xlsx")) {
							
							errorMap.clear();
							successMap.clear();

							isFileAvailableToValidate = true;
							logger.info("LifelineInwardUploadValidator - File Name::" + file.getName());

							logger.info("LifelineInwardUploadValidator - start time::" + System.currentTimeMillis());

							XSSFWorkbook input_workbook;
							XSSFSheet input_workbook_sheet;

							OPCPackage opcPackage = OPCPackage.open(file);
							input_workbook = new XSSFWorkbook(opcPackage);
							logger.info("LifelineInwardUploadValidator - input workbook read ");

							SXSSFWorkbook error_workbook = new SXSSFWorkbook(100);
							SXSSFSheet error_workbook_sheet = error_workbook.createSheet("Error Records");

							SXSSFWorkbook success_workbook = new SXSSFWorkbook(100);
							SXSSFSheet success_workbook_sheet = success_workbook.createSheet("Success Records");

							CellStyle cloneCellStyleSuccess = success_workbook.createCellStyle();

							for (int sheetnum = 0; sheetnum == 0; sheetnum++) {
								logger.info("sheetnum-->" + sheetnum);
								input_workbook_sheet = input_workbook.getSheetAt(sheetnum);

								 input_workbook_sheet_total_row = input_workbook_sheet.getLastRowNum();
								totalRecords = totalRecords + input_workbook_sheet_total_row;
								logger.info("input_workbook_sheet_total_row of current workbook-->" + input_workbook_sheet_total_row);

								logger.info(" start time before validation andstarts ::" + System.currentTimeMillis());
								validations(input_workbook, input_workbook_sheet, error_workbook, error_workbook_sheet,
										totalColumnCount, errorMap, successMap);
								logger.info(" end time after validation copy ::" + System.currentTimeMillis());

								logger.info("Error map size -->" + errorMap.size());
								logger.info("success map size -->" + successMap.size());
								CellStyle cloneCellStyleError = error_workbook.createCellStyle();

								if (errorMap.size() > 0) {
									currentErrorRowCount = 0;
									logger.info(" start time before error row copy  for the file ::" + file.getName()
											+ " ----- >" + System.currentTimeMillis());
									currentErrorRowCount = excelCopyRowWithRemarks.copyMultipleRow(input_workbook,
											input_workbook_sheet, error_workbook, error_workbook_sheet,
											cloneCellStyleError, totalColumnCount, errorMap);
									logger.info(" end time before error row copy  for the file ::" + file.getName()
											+ " ----- >" + System.currentTimeMillis());
									errorRowCount = errorRowCount + currentErrorRowCount;
								} else {
									logger.info(" No Error rows to copy this time ");
								}

								if (successMap.size() > 0) {
									currentSuccessRowCount = 0;
									logger.info(" start time before success row copy  for the file ::" + file.getName()
											+ " ----- >" + System.currentTimeMillis());
									currentSuccessRowCount = excelCopyRowWithRemarks.copyMultipleRow(input_workbook,
											input_workbook_sheet, success_workbook, success_workbook_sheet,
											cloneCellStyleSuccess, (short) 0, successMap);
									successRowCount = successRowCount + currentSuccessRowCount;
									logger.info(" end time before success row copy  for the file ::" + file.getName()
											+ " ----- >" + System.currentTimeMillis());
								} else {
									logger.info(" No Success rows to copy this time ");
								}

							}
							if (input_workbook != null) {
								input_workbook.close();
							}

							logger.info("LifelineInwardUploadValidator - Validation Completed for the file ::"
									+ file.getName());

							if (UtilityFile.isSheetEmpty(error_workbook.getSheetAt(0))) {

								logger.info("LifelineInwardUploadValidator - Error available in the file ::"
										+ file.getName());

								String errorFilePath = errorFolder.getPath() + "/"
										+ UtilityFile.getFileNameWithoutExtension(file) + "_ERROR.xlsx";

								UtilityFile.writeSxssfFileByPath(errorFolder.getPath(), errorFilePath, error_workbook);

								logger.info("LifelineInwardUploadValidator - Error File Path::" + errorFilePath);

							} else {
								logger.info("LifelineInwardUploadValidator - No Error in the file");
							}

							if (UtilityFile.isSheetEmpty(success_workbook.getSheetAt(0))) {

								String successFilePath = successFolder.getPath() + "/"
										+ UtilityFile.getFileNameWithoutExtension(file) + "_SUCCESS.xlsx";

								logger.info("LifelineInwardUploadValidator - success file path ::" + successFilePath);

								UtilityFile.writeSxssfFileByPath(successFolder.getPath(), successFilePath,
										success_workbook);

								//exchange.setProperty(RPAConstants.SUCCESS_FILE_PATH, successFilePath);

							}

							if (success_workbook != null) {
								success_workbook.close();
							}
							if (error_workbook != null) {
								error_workbook.close();
							}

							logger.info("LifelineInwardUploadValidator - end time::" + System.currentTimeMillis());

							transactionInfo.setProcessStatus(RPAConstants.FILE_PROCESSED);

							
							if(opcPackage!=null){
								opcPackage.revert();
							}
						}
					}
				}

				errorMap=null;
				successMap=null;
				
				logger.info("LifelineInwardUploadValidator - Total validation success records ::" + String.valueOf(successRowCount));
				transactionInfo.setTotalSuccessRecords(String.valueOf(successRowCount));
				logger.info("LifelineInwardUploadValidator - Total validation error records ::" + String.valueOf(errorRowCount));
				transactionInfo.setTotalErrorRecords(String.valueOf(errorRowCount));
				logger.info("LifelineInwardUploadValidator - Total records ::" + totalRecords);
				transactionInfo.setTotalRecords(String.valueOf(totalRecords));
				
				if (!isFileAvailableToValidate) {
					transactionInfo.setProcessStatus(RPAConstants.NO_FILE);
					logger.info("LifelineInwardUploadValidator  - No Files in process folder to validate");
					exchange.getIn().setHeader(RPAConstants.NO_FILE, RPAConstants.Y);
				} else {
					transactionInfo.setProcessStatus(RPAConstants.FILE_VALIDATED);
					exchange.getIn().setHeader(RPAConstants.NO_FILE, RPAConstants.N);
					logger.info("LifelineInwardUploadValidator  - File validated");
				}
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Error in LifelineInwardUploadValidator - doProcess Method Ended  " , e);
			return false;
		}
	}

	public void validations(XSSFWorkbook input_workbook, XSSFSheet input_workbook_sheet, SXSSFWorkbook error_workbook,
			SXSSFSheet error_workbook_sheet, short totalColumnCount, Map<Integer, String> errorMap,
			Map<Integer, String> successMap) throws IOException {

		logger.info("BEGIN - validations()");

		String[] headers = { "PROPOSARCODE", "FIRSTNAME", "LASTNAME", "CITY", "STATE", "BRANCH", "AGENTCODE",
				"AGENTNAME", "SUBLINE", "PRODUCTNAME", "TRANSACTIONTYPE", "POLICYCODE", "QUOTENUMBER", "INWARDCODE",
				"INWARDPREMIUM", "CURRENT_USER", "CREATIONDATE", "CREATEDBY", "COMMENTSDATE", "CHEQUE_NUMBER",
				"CHEQUE_DATE", "CHEQUE_AMOUNT", "CHEQUE_BRANCH", "CHEQUE_BANK", "CASHAMT", "POLICY_START_DATE",
				"POLICY_END_DATE", "PROPOSALFORMNUMBER", "SMNAME", "SMCODE", "BRANCH_NAME", "OACODE",
				"POLICYPOSTEDDATE", "NETPREMIUM", "RECEIPT_DATE", "PAYMENTMODE", "ROWN", "CONS_RECEIPT", "RECP_AMOUNT",
				"INWARDDATE", "INFO_REASON", "COMMENTS", "STATUS", "SHORTFALL", "Channel_New", "REFERRAL_TO",
				"REFERRAL_ACTIONED", "Referral Date ( UW )", "UW STATUS", "Decision", "Remarks", "Actionable",
				"Contactable/ Non- Contactable of PPMC", "Sub Remarks of PPMC" };
		boolean validationFailed = false;int row_1_max = 0;String errorRemarks = "";StringBuffer sb =null;

		for (int sheetnum = 0; sheetnum == 0; sheetnum++) {
			int input_workbook_sheet_total_row = input_workbook_sheet.getLastRowNum();
			for (int input_workbook_sheet_row_1 = input_workbook_sheet_total_row; input_workbook_sheet_row_1 >= 1; input_workbook_sheet_row_1--) { // traversing
				XSSFRow row_1_index = input_workbook_sheet.getRow(input_workbook_sheet_row_1);
				row_1_max = row_1_index.getLastCellNum();
				validationFailed = false;
				errorRemarks = "";
				sb = new StringBuffer();
				for (int row_1_index_cell = 0; row_1_index_cell <= row_1_max; row_1_index_cell++) {
					String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
					
					/*date format check*/
					if(value != null && !StringUtils.isEmpty(value)){
						if(row_1_index_cell == 16 || row_1_index_cell == 18 || row_1_index_cell == 20
								|| row_1_index_cell == 25 || row_1_index_cell == 26 || row_1_index_cell == 32
								|| row_1_index_cell == 34 || row_1_index_cell == 39 || row_1_index_cell == 47){
							
							if(value.contains(" :.")){
								value = value.replaceAll(":.", "").trim();
							}
							
							 if(!UtilityFile.checkDateFormat(value, RPAConstants.dd_slash_MM_slash_yyyy)){
								validationFailed = true;
								sb.append(errorRemarks.concat(headers[row_1_index_cell] +"-"+ErrorConstants.ERROR_CODE_0002));
							}
							
						}
					}
					
					if (row_1_index_cell == 1 || row_1_index_cell == 2 || row_1_index_cell == 7
							|| row_1_index_cell == 11 || row_1_index_cell == 12 || row_1_index_cell == 13
							|| row_1_index_cell == 15 || row_1_index_cell == 21 || row_1_index_cell == 30
							|| row_1_index_cell == 40 || row_1_index_cell == 42 || row_1_index_cell == 43
							|| row_1_index_cell == 45 || row_1_index_cell == 46 || row_1_index_cell == 48
							|| row_1_index_cell == 49 || row_1_index_cell == 51) { // header
						
						if (value != null && value.length() > 1000) {
							validationFailed = true;
							sb.append(errorRemarks.concat(headers[row_1_index_cell] +"-"+ ErrorConstants.ERROR_CODE_00014 + " 1000."));
							break;
						}
					} else if (row_1_index_cell == 41 || row_1_index_cell == 50) {
						if (value != null && value.length() > 2000) {
							validationFailed = true;
							sb.append(errorRemarks.concat(headers[row_1_index_cell] +"-"+ ErrorConstants.ERROR_CODE_00014 + " 2000."));
							break;
						}
					} else if (row_1_index_cell == 36) {
						if (value != null && value.length() > 5) {
							validationFailed = true;
							sb.append(errorRemarks.concat(headers[row_1_index_cell] +"-"+ ErrorConstants.ERROR_CODE_00014 + " 5."));
							break;
						}
					} else if (row_1_index_cell == 53) {
						if (value != null && value.length() > 200) {
							validationFailed = true;
							sb.append(errorRemarks.concat(headers[row_1_index_cell] +"-"+ ErrorConstants.ERROR_CODE_00014 + " 200."));
							break;
						}
					} else {
						if (value != null && value.length() > 100) {
							validationFailed = true;
							sb.append(errorRemarks.concat(headers[row_1_index_cell] +"-"+ ErrorConstants.ERROR_CODE_00014 + " 100."));
							break;
						}
					}
					
				}
				if (validationFailed) {
					errorMap.put(input_workbook_sheet_row_1, sb.toString());
				} else {
					successMap.put(input_workbook_sheet_row_1, sb.toString());
				}
				sb = null;
			}
		}
		logger.info("END - validations()");
	}

}
