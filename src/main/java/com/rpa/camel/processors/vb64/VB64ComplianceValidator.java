/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.processors.vb64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.camel.processors.common.ExcelCopyRowWithRemarks;
import com.rpa.constants.ErrorConstants;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.service.EmailService;
import com.rpa.util.UtilityFile;

public class VB64ComplianceValidator implements Processor {

	private DataFormatter formatter = new DataFormatter();

	private SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("dd/MM/yyyy");

	private ExcelCopyRowWithRemarks excelCopyRowWithRemarks = new ExcelCopyRowWithRemarks();

	private static final Logger logger = LoggerFactory.getLogger(VB64ComplianceValidator.class.getName());

	ApplicationContext applicationContext = SpringContext.getAppContext();

	public void process(Exchange exchange) throws Exception {

		logger.info("BEGIN - VB64ComplianceValidator");
		
		 String fileSuffix = new SimpleDateFormat("yyyy-MM-dd-HHmmss-SSS").format(new Date());

		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		String bankName = (String) exchange.getProperty(RPAConstants.BANK_NAME);
		String successBaseFolderPath = (String) exchange.getProperty(RPAConstants.SUCCESS_BASE_FOLDER_PATH);
		String errorBaseFolderPath = (String) exchange.getProperty(RPAConstants.ERROR_BASE_FOLDER_PATH);
		transactionInfo.setProcessPhase(RPAConstants.VALIDATION);

		String bankFilePath = transactionInfo.getInputFilePath_1();

		logger.info("VB64ComplianceValidator - Bank File Path::"+bankFilePath);

		File bank_file = new File(bankFilePath);

		if(bank_file != null) {

			logger.info("VB64ComplianceValidator - File Name::"+bank_file.getName());

			HSSFWorkbook input_workbook;
			HSSFSheet input_workbook_sheet;
			HSSFSheet error_workbook_sheet;
			HSSFSheet success_workbook_sheet;

			input_workbook = new HSSFWorkbook(new FileInputStream(bank_file));
			HSSFWorkbook  error_workbook = new HSSFWorkbook();
			error_workbook_sheet = error_workbook.createSheet("Error Records");
			HSSFWorkbook  success_workbook = new HSSFWorkbook();
			success_workbook_sheet = success_workbook.createSheet("Success Records");
			Hashtable<Integer, Integer> hashtable = new Hashtable<Integer, Integer>();

			HSSFCellStyle cloneCellStyleSuccess = success_workbook.createCellStyle();
			
			Integer[] dateCoumnIndex = new Integer[]{};
			if(RPAConstants.HDFC.equals(bankName)) {
			dateCoumnIndex = new Integer[]{Integer.valueOf(5), Integer.valueOf(21)};
			} else if(RPAConstants.CITI.equals(bankName)) {
			dateCoumnIndex = new Integer[]{Integer.valueOf(3),Integer.valueOf(6),Integer.valueOf(12)};
			} else if(RPAConstants.AXIS.equals(bankName)) {
			dateCoumnIndex = new Integer[]{};
			} else if(RPAConstants.HSBC.equals(bankName)) {
			dateCoumnIndex = new Integer[]{Integer.valueOf(5),Integer.valueOf(20)};
			} else if(RPAConstants.SCB.equals(bankName)) {
			dateCoumnIndex = new Integer[]{Integer.valueOf(4),Integer.valueOf(14)};
			}
			

			for (int sheetnum = 0; sheetnum == 0; sheetnum++) {

				input_workbook_sheet = input_workbook.getSheetAt(sheetnum);

				int input_workbook_sheet_total_row = input_workbook_sheet.getLastRowNum(); 

				transactionInfo.setTotalRecords(String.valueOf(input_workbook_sheet_total_row));

				validations(input_workbook, input_workbook_sheet, error_workbook, error_workbook_sheet, hashtable, bankName,dateCoumnIndex);

				int count=0;
				Map<Integer, HSSFCellStyle> successStyleMap = new HashMap<Integer,HSSFCellStyle>();
				for (int input_workbook_sheet_row_1 = input_workbook_sheet_total_row; input_workbook_sheet_row_1 >=1; input_workbook_sheet_row_1--) { // traversing

					if(hashtable.size()>0 && !hashtable.contains(input_workbook_sheet_row_1)){
						count++;
						excelCopyRowWithRemarks.copyRow(input_workbook, input_workbook_sheet, success_workbook, 
								success_workbook_sheet, input_workbook_sheet_row_1, count, cloneCellStyleSuccess, (short) 0, RPAConstants.NA,dateCoumnIndex,successStyleMap);
					} else if(hashtable.size()==0){
						count++;
						excelCopyRowWithRemarks.copyRow(input_workbook, input_workbook_sheet, success_workbook,
								success_workbook_sheet, input_workbook_sheet_row_1, count, cloneCellStyleSuccess, (short) 0, RPAConstants.NA,dateCoumnIndex,successStyleMap);
					}
				}
			}
			input_workbook.close();

			logger.info("VB64ComplianceValidator - Validation Completed::"+bank_file.getName());

			if(UtilityFile.isSheetEmpty(error_workbook.getSheetAt(0))){
				
				String directoryName = UtilityFile.getCodeBasePath() + UtilityFile.getUploadProperty(errorBaseFolderPath);
				
				String errorFilePath = directoryName +"/"+transactionInfo.getProcessName()+"_ERROR_"+fileSuffix+".xls";
				
				UtilityFile.writeExcelFileByPath(directoryName, errorFilePath, error_workbook);

				logger.info("VB64ComplianceValidator - Error File Path::"+errorFilePath);

				transactionInfo.setErrorFileDownload(errorFilePath);

				EmailService emailService = (EmailService) applicationContext.getBean("emailService");
				emailService.vb64ComplianceNotification(bankName, errorFilePath, transactionInfo);
			}

			for (int sheetnum = 0; sheetnum < error_workbook.getNumberOfSheets(); sheetnum++) {
				error_workbook_sheet = error_workbook.getSheetAt(sheetnum);
				int error_workbook_sheet_total_row = error_workbook_sheet.getLastRowNum(); 
				transactionInfo.setTotalErrorRecords(String.valueOf(error_workbook_sheet_total_row));
			}
			error_workbook.close();

			if(UtilityFile.isSheetEmpty(success_workbook.getSheetAt(0))){
				
				String directoryName = UtilityFile.getCodeBasePath() + UtilityFile.getUploadProperty(successBaseFolderPath);
				
				String successFilePath = directoryName +"/"+transactionInfo.getProcessName()+"_SUCCESS_"+fileSuffix+".xls";
				
				UtilityFile.writeExcelFileByPath(directoryName, successFilePath, success_workbook);

				exchange.setProperty(RPAConstants.SUCCESS_FILE_PATH, successFilePath);
				transactionInfo.setSuccessFileDownload(successFilePath);
			}
			for (int sheetnum = 0; sheetnum < success_workbook.getNumberOfSheets(); sheetnum++) {
				success_workbook_sheet = success_workbook.getSheetAt(sheetnum);
				int success_workbook_sheet_total_row = success_workbook_sheet.getLastRowNum(); 
				transactionInfo.setTotalSuccessRecords(String.valueOf(success_workbook_sheet_total_row));
			}
			success_workbook.close();
		} 

		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		boolean renamed = bank_file.renameTo(new File(bank_file.getPath()+"_"+fileSuffix+".DONE"));			

		logger.info("Bank File Renamed?::"+renamed);

		logger.info("END - VB64ComplianceValidator");
	}

	@SuppressWarnings("deprecation")
	public void validations(HSSFWorkbook input_workbook, HSSFSheet input_workbook_sheet, HSSFWorkbook error_workbook, HSSFSheet error_workbook_sheet,
			Hashtable<Integer, Integer> hashtable, String bankName, Integer[] dateCoumnIndex) throws IOException, ParseException{

		logger.info("BEGIN - validations()");
		Map<Integer, HSSFCellStyle> errorStyleMap = new HashMap<Integer,HSSFCellStyle>();

		HSSFCellStyle cloneCellStyleError = error_workbook.createCellStyle();
		
		//binds the style you need to the cell.
		HSSFCellStyle dateCellStyle = input_workbook.createCellStyle();
		short df = input_workbook.createDataFormat().getFormat("dd/MM/yyyy");
		dateCellStyle.setDataFormat(df);
		/*short df1 = input_workbook.createDataFormat().getFormat("ddMMyyyy");
		dateCellStyle.setDataFormat(df1);*/

		for (int sheetnum = 0; sheetnum ==0 ; sheetnum++) {
			int input_workbook_sheet_total_row = input_workbook_sheet.getLastRowNum(); 
			int count=0;
			for (int input_workbook_sheet_row_1 = input_workbook_sheet_total_row; input_workbook_sheet_row_1 >=1; input_workbook_sheet_row_1--) { // traversing
				HSSFRow row_1_index = input_workbook_sheet.getRow(input_workbook_sheet_row_1);
				int row_1_max = row_1_index.getLastCellNum();
				boolean validationFailed = false;
				boolean skipRecordFlag = false;
				short totalColumnCount = 0;
				String remarks = RPAConstants.NA;
				for (int row_1_index_cell = 0; row_1_index_cell <=row_1_max; row_1_index_cell++) {
					if(RPAConstants.HDFC.equals(bankName)) {
						totalColumnCount = 28;
						if(row_1_index_cell==0){ //header
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if("H".equals(value)){
								skipRecordFlag = true;
								break;
							}
						} else if(row_1_index_cell==3){ //dr_cr
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !UtilityFile.onlyLettersSpaces(value)){
								validationFailed = true;
								remarks = "Debit / Credit -" + ErrorConstants.ERROR_CODE_0003;
								break;
							}
						} else if(row_1_index_cell==5){ //val_dt
							//String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).replaceAll("[/-]", "");
							String value ="";
							
							if((row_1_index.getCell(row_1_index_cell).getCellType()==Cell.CELL_TYPE_NUMERIC)){
								if(DateUtil.isCellDateFormatted(row_1_index.getCell(row_1_index_cell))){
									 SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
									 value = sdf.format(row_1_index.getCell(row_1_index_cell).getDateCellValue());
								}else{
									if(!formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).equals("") && formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).indexOf("/")==-1 && 
											formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).indexOf("-")==-1){
										value = UtilityFile.convertToDateFormat(formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)), "ddMMyyyy", "dd/MM/yyyy");	
									}else{
										value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
									}
								}
							}else{
								if(!formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).equals("") && formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).indexOf("/")==-1 && 
										formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).indexOf("-")==-1){
									value = UtilityFile.convertToDateFormat(formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)), "ddMMyyyy", "dd/MM/yyyy");	
								}else{
									value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
								}
							}
							if(StringUtils.isEmpty(value) || !UtilityFile.checkDateFormat(value, "dd/MM/yyyy")){
								validationFailed = true;
								remarks = "Value Date -" + ErrorConstants.ERROR_CODE_0002;
								break;
							}
							row_1_index.getCell(row_1_index_cell).setCellStyle(dateCellStyle);
							row_1_index.getCell(row_1_index_cell).setCellValue(value);
							
							    
						} else if(row_1_index_cell==17){ //inst_no
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !StringUtils.isNumeric(value.trim())){
								validationFailed = true;
								remarks = "Instrument Number -" + ErrorConstants.ERROR_CODE_0004;
								break;
							}
						} else if(row_1_index_cell==18){ //drawee_bk
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).replaceAll("[^a-zA-Z0-9\\s]", "");
							if(StringUtils.isEmpty(value) || !UtilityFile.onlyLettersSpaces(value)){
								validationFailed = true;
								remarks = "Drawee Bank -" + ErrorConstants.ERROR_CODE_0003;
								break;
							}
							row_1_index.getCell(row_1_index_cell).setCellValue(value);
						} else if(row_1_index_cell==20){ //inst_amt
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !UtilityFile.isFloat(value)){
								validationFailed = true;
								remarks = "Instrument Amount -" + ErrorConstants.ERROR_CODE_0005;
								break;
							}
						} else if(row_1_index_cell==21){ //inst_dt
							/*String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !UtilityFile.checkDateFormat(value, RPAConstants.ddMMyyyy)){
								validationFailed = true;
								remarks = "Instrument Date -" + ErrorConstants.ERROR_CODE_0002;
								break;
							}*/
							String value = "";
							if((row_1_index.getCell(row_1_index_cell).getCellType()==Cell.CELL_TYPE_NUMERIC)){
								if(DateUtil.isCellDateFormatted(row_1_index.getCell(row_1_index_cell))){
									 SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
									 value = sdf.format(row_1_index.getCell(row_1_index_cell).getDateCellValue());
								}else{
									if(!formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).equals("") && formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).indexOf("/")==-1 && 
											formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).indexOf("-")==-1){
										value = UtilityFile.convertToDateFormat(formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)), "ddMMyyyy", "dd/MM/yyyy");	
									}else{
										value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
									}
								}
							}else{
								if(!formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).equals("") && formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).indexOf("/")==-1 && 
										formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).indexOf("-")==-1){
									value = UtilityFile.convertToDateFormat(formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)), "ddMMyyyy", "dd/MM/yyyy");	
								}else{
									value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
								}
							}
							if(StringUtils.isEmpty(value) || !UtilityFile.checkDateFormat(value, "dd/MM/yyyy")){
								validationFailed = true;
								remarks = "Instrument Date -" + ErrorConstants.ERROR_CODE_0002;
								break;
							}
							row_1_index.getCell(row_1_index_cell).setCellStyle(dateCellStyle);
							row_1_index.getCell(row_1_index_cell).setCellValue(value);
						} else if(row_1_index_cell==26){//policy_no
							/*String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !StringUtils.isAlphanumeric(value.trim())){
								validationFailed = true;
								remarks = "Policy No -" + ErrorConstants.ERROR_CODE_0006;
								break;
							}*/
							/*Removed above policy number Validation as requested by Rs Team*/
						} 
					} else if(RPAConstants.CITI.equals(bankName)) {
						totalColumnCount = 19;
						if(row_1_index_cell==3){ //Credit_Debit Dt
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !UtilityFile.checkDateFormat(value, RPAConstants.dd_slash_MM_slash_yyyy)){
								validationFailed = true;
								remarks = "Credit / Debit Date - " + ErrorConstants.ERROR_CODE_0002;
								break;
							}
						} else if(row_1_index_cell==5){ //Cheque no
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || StringUtils.isBlank(value) || Integer.valueOf(value) < 0){
								validationFailed = true;
								remarks = "Cheque Number - " + ErrorConstants.ERROR_CODE_0004;
								break;
							}
						} else if(row_1_index_cell==6){ //Chq Amount
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !UtilityFile.isFloat(value)){
								validationFailed = true;
								remarks = "Cheque Amount - " + ErrorConstants.ERROR_CODE_0005;
								break;
							}
						} else if(row_1_index_cell==16){ //Bank Name
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).replaceAll("[^a-zA-Z0-9\\s]", "");
							if(StringUtils.isEmpty(value) || !UtilityFile.onlyLettersSpaces(value)){
								validationFailed = true;
								remarks = "Bank Name - " + ErrorConstants.ERROR_CODE_0003;
								break;
							}
							row_1_index.getCell(row_1_index_cell).setCellValue(value);
						} 
					} else if(RPAConstants.AXIS.equals(bankName)) {
						// AXIS Dropped from Requirement - Future Inclusion
					} else if(RPAConstants.HSBC.equals(bankName)) {
						totalColumnCount = 22;
						if(row_1_index_cell==3){ //Debit / Credit
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !UtilityFile.onlyLettersSpaces(value)){
								validationFailed = true;
								remarks = "Debit / Credit -" + ErrorConstants.ERROR_CODE_0003;
								break;
							}
						} else if(row_1_index_cell==5){ //Date of Entry
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !UtilityFile.checkDateFormat(value, RPAConstants.dd_slash_MM_slash_yyyy)){
								validationFailed = true;
								remarks = "Date of Entry -" + ErrorConstants.ERROR_CODE_0002;
								break;
							}
						} else if(row_1_index_cell==16){ //Instrument No.
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !StringUtils.isNumeric(value.trim())){
								validationFailed = true;
								remarks = "Instrument Number -" + ErrorConstants.ERROR_CODE_0004;
								break;
							}	
						} else if(row_1_index_cell==17){ //Drawee Bank
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).replaceAll("[^a-zA-Z0-9\\s]", "");
							if(StringUtils.isEmpty(value) || !UtilityFile.onlyLettersSpaces(value)){
								validationFailed = true;
								remarks = "Drawee Bank -" + ErrorConstants.ERROR_CODE_0003;
								break;
							}
							row_1_index.getCell(row_1_index_cell).setCellValue(value);
						} else if(row_1_index_cell==19){ //Instrument Amount
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !UtilityFile.isFloat(value.replace(",", ""))){
								validationFailed = true;
								remarks = "Instrument Amount -" + ErrorConstants.ERROR_CODE_0005;
								break;
							}
						} else if(row_1_index_cell==20){ //Instrument Date
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !UtilityFile.checkDateFormat(value, RPAConstants.dd_slash_MM_slash_yyyy)){
								validationFailed = true;
								remarks = "Instrument Date -" + ErrorConstants.ERROR_CODE_0002;
								break;
							}
						} 
					} else if(RPAConstants.SCB.equals(bankName)) {
						totalColumnCount = 21;
						if(row_1_index_cell==2){ //Cr/Dr
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !UtilityFile.onlyLettersSpaces(value)){
								validationFailed = true;
								remarks = "Debit / Credit -" + ErrorConstants.ERROR_CODE_0003;
								break;
							}
						} else if(row_1_index_cell==4){ //Credit_Debit Dt
							Date date = row_1_index.getCell(row_1_index_cell).getDateCellValue();
							String value = simpleDateFormatter.format(date).toString();
							if(StringUtils.isEmpty(value) || !UtilityFile.checkDateFormat(value, RPAConstants.dd_slash_MM_slash_yyyy)){
								validationFailed = true;
								remarks = "Credit / Debit Date -" + ErrorConstants.ERROR_CODE_0002;
								break;
							}
						} else if(row_1_index_cell==9){ //Depost#
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !StringUtils.isAlphanumeric(value.trim())){
								validationFailed = true;
								remarks = "Depost Number -" + ErrorConstants.ERROR_CODE_0004;
								break;
							}	
						} else if(row_1_index_cell==13){ //Cheque no
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || StringUtils.isBlank(value) || Integer.valueOf(value) < 0){
								validationFailed = true;
								remarks = "Cheque Number -" + ErrorConstants.ERROR_CODE_0004;
								break;
							}
						} else if(row_1_index_cell==14){ //Cheque Dt
							Date date = row_1_index.getCell(row_1_index_cell).getDateCellValue();
							String value = simpleDateFormatter.format(date).toString();
							if(StringUtils.isEmpty(value) || !UtilityFile.checkDateFormat(value, RPAConstants.dd_slash_MM_slash_yyyy)){
								validationFailed = true;
								remarks = "Cheque Date -" + ErrorConstants.ERROR_CODE_0002;
								break;
							}
						} else if(row_1_index_cell==15){ //Drawee Bank
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell)).replaceAll("[^a-zA-Z0-9\\s]", "");
							if(StringUtils.isEmpty(value) || !UtilityFile.onlyLettersSpaces(value)){
								validationFailed = true;
								remarks = "Drawee Bank -" + ErrorConstants.ERROR_CODE_0003;
								break;
							}
							row_1_index.getCell(row_1_index_cell).setCellValue(value);
						} else if(row_1_index_cell==17){ //Chq Amount
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(StringUtils.isEmpty(value) || !UtilityFile.isFloat(value)){
								validationFailed = true;
								remarks = "Cheque Amount -" + ErrorConstants.ERROR_CODE_0005;
								break;
							}
						} if(row_1_index_cell==19){ //Reason
							String value = formatter.formatCellValue(row_1_index.getCell(row_1_index_cell));
							if(!UtilityFile.onlyLettersSpaces(value)){
								validationFailed = true;
								remarks = "Reason -" + ErrorConstants.ERROR_CODE_0003;
								break;
							}
						}
					} 
				}
				if(skipRecordFlag){
					hashtable.put(input_workbook_sheet_row_1, input_workbook_sheet_row_1);
				} else if(validationFailed){
					if(hashtable.size()>0 && !hashtable.contains(input_workbook_sheet_row_1)){
						count++;
						excelCopyRowWithRemarks.copyRow(input_workbook, input_workbook_sheet, error_workbook, error_workbook_sheet, 
								input_workbook_sheet_row_1, count, cloneCellStyleError, totalColumnCount, remarks,dateCoumnIndex,errorStyleMap);
					} else if(hashtable.size()==0){
						count++;
						excelCopyRowWithRemarks.copyRow(input_workbook, input_workbook_sheet, error_workbook, error_workbook_sheet, 
								input_workbook_sheet_row_1, count, cloneCellStyleError, totalColumnCount, remarks,dateCoumnIndex,errorStyleMap);
					}
					hashtable.put(input_workbook_sheet_row_1, input_workbook_sheet_row_1);
				}
			}
		}
		logger.info("END - validations()");
	}
}