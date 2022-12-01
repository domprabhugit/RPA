/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.processors.vb64;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.util.UtilityFile;

public class VB64ComplianceTransformer implements Processor {

	private SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("dd/MM/yyyy");

	private static final Logger logger = LoggerFactory.getLogger(VB64ComplianceTransformer.class.getName());

	@SuppressWarnings("deprecation")
	public void process(Exchange exchange) throws Exception{

		logger.info("BEGIN - VB64ComplianceTransformer");
		
		String fileSuffix = new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date());

		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		String bankName = (String) exchange.getProperty(RPAConstants.BANK_NAME);
		String uploadBaseFolderPath = (String) exchange.getProperty(RPAConstants.UPLOAD_BASE_FOLDER_PATH);
		transactionInfo.setProcessPhase(RPAConstants.TRANSFORMATION);

		String successOutputFile =  (String) exchange.getProperty(RPAConstants.SUCCESS_FILE_PATH);

		logger.info("VB64ComplianceTransformer - Success Output File::"+successOutputFile);

		if (successOutputFile != null) {

			File success_file = new File(successOutputFile);

			logger.info("VB64ComplianceTransformer - File Name::"+success_file.getName());

			HSSFWorkbook success_workbook = new HSSFWorkbook(new FileInputStream(success_file));

			HSSFWorkbook upload_workbook = new HSSFWorkbook();
			HSSFSheet sheet = upload_workbook.createSheet("64VB Upload Sheet");

			Object[][] datatypes = null;

			if(RPAConstants.HDFC.equals(bankName)) {
				datatypes = new Object[][] {{"Month", "row_type", "entry_id", "type_of_en", "dr_cr", "entry_amt", "val_dt", "post_dt", "prod_code", "pkup_loc", 
					"pkup_pt", "pkup_dt", "dept_slip", "dept_dt", "dept_amt", "no_of_inst", "dept_rmk", "inst_no", "drawee_bk", "cl_loc", 
					"inst_amt", "inst_dt", "drawer_nam", "deal_code", "deal_name", "drawer", "policy_no", "return_rsn"}};
			} else if(RPAConstants.CITI.equals(bankName)) {
				datatypes = new Object[][] {{"CLIENT CODE", "DEPOSIT DATE", "PRODUCT", "CREDIT/DEBIT DATE", "LOCATION", "CHEQUE NO.", "CHEQUE AMT.", "TYPE(CR/DR)", "NARRATION", 
					"CBP. NO.", "DEP.SLIP NO.", "CUSTOMER REF.", "DEPOSIT AMT.", "DWE BANK CODE", "CHECK DATA", "COVER NOTE NO.", "BANK NAME", "PICK POINT NAME", "PKUP POINT CODE", 
				"REMARKS"}};			
			} else if(RPAConstants.AXIS.equals(bankName)) {
				// AXIS Dropped from Requirement - Future Inclusion
			} else if(RPAConstants.HSBC.equals(bankName)) {
				datatypes = new Object[][] {{"Month", "Record Identifier", "Txn. Journal No.", "Type of Entry", "Debit / Credit", "Entry Amount", "Date of Entry", 
					"Post Date", "Product Code", "Pickup Location", "Pickup Point", "Pickup Date", "Deposit Slip No.", "Date of Deposit Slip", "Deposit Amount", "No. of Instruments", 
					"Deposit Remarks", "Instrument No.", "Drawee Bank", "Clearing Loc.", "Instrument Amount", "Instrument Date", "Drawer Name", "MI Policy no", "return reason", "Correct chq no", 
					"Correct chq amt", "RSA Policy no"}};
			} else if(RPAConstants.SCB.equals(bankName)) {
				datatypes = new Object[][] {{"Customer Name", "Ent_type", "Cr/Dr", "Ent_Amount", "Credit_Debit Dt", "Product", "PickupLoc", "Pickup Point", "Pickupdt", 
					"Depost#", "Dep_date", "Dep_Amount", "Pay Order No", "Cheque no", "Cheque Dt", "Drawee Bank", "Drawnon", "Chq Amount", "Drawer", "Reason", 
					"Enrichment No", "Enrichment Remark"}};	
			} 

			int rowNum = 0;

			for (Object[] datatype : datatypes) {
				HSSFRow row = sheet.createRow(rowNum++);
				int colNum = 0;
				for (Object field : datatype) {
					HSSFCell cell = row.createCell(colNum++);
					if (field instanceof String) {
						cell.setCellValue((String) field);
					} else if (field instanceof Integer) {
						cell.setCellValue((Integer) field);
					}
				}
			}

			for (int sheetnum = 0; sheetnum == 0; sheetnum++) {
				
				

				HSSFSheet success_workbook_sheet = success_workbook.getSheetAt(sheetnum);

				int success_workbook_sheet_total_row = success_workbook_sheet.getLastRowNum(); 
				
				//binds the style you need to the cell.
				HSSFCellStyle dateCellStyle = upload_workbook.createCellStyle();
				short df = upload_workbook.createDataFormat().getFormat("dd/MM/yyyy");
				dateCellStyle.setDataFormat(df);
				boolean applyDateStyle= false;

				if(RPAConstants.HDFC.equals(bankName)) {
					for (int success_workbook_sheet_row_1 = success_workbook_sheet_total_row; success_workbook_sheet_row_1 >=1; success_workbook_sheet_row_1--) { // traversing
						HSSFRow row_1_index = success_workbook_sheet.getRow(success_workbook_sheet_row_1);
						int row_1_max = row_1_index.getLastCellNum();
						HSSFRow row = sheet.createRow(rowNum++);
						int colNum = 0;
						HSSFCell cell1 = row.createCell(colNum++);
						cell1.setCellValue(UtilityFile.createSpecifiedDateFormat(RPAConstants.dd_slash_MM_slash_yyyy));
						for (int row_1_index_cell = 0; row_1_index_cell < row_1_max; row_1_index_cell++) {
							applyDateStyle = false;
							String value = "";
							if(row_1_index_cell!=16){
								if(row_1_index_cell==5 || row_1_index_cell==6 || row_1_index_cell==10 || row_1_index_cell==12 || row_1_index_cell==21){
									if (HSSFCell.CELL_TYPE_NUMERIC == row_1_index.getCell(row_1_index_cell).getCellType()) {
										if(DateUtil.isCellDateFormatted(row_1_index.getCell(row_1_index_cell))){
											value = simpleDateFormatter.format(row_1_index.getCell(row_1_index_cell).getDateCellValue()).toString();
										} else {
											String dateValue = NumberToTextConverter.toText(row_1_index.getCell(row_1_index_cell).getNumericCellValue());
											value = UtilityFile.convertToDateFormat(dateValue, RPAConstants.ddMMyyyy, RPAConstants.dd_slash_MM_slash_yyyy);
										}
									}else{
										applyDateStyle = true;
										value = row_1_index.getCell(row_1_index_cell).toString();
									}
								} else {
									if (HSSFCell.CELL_TYPE_NUMERIC == row_1_index.getCell(row_1_index_cell).getCellType()) { 
										if(row_1_index_cell==4 || row_1_index_cell==13 || row_1_index_cell==20){
											value = Double.toString(row_1_index.getCell(row_1_index_cell).getNumericCellValue());
										} else {
											value = NumberToTextConverter.toText(row_1_index.getCell(row_1_index_cell).getNumericCellValue());
										}
									} else if(HSSFCell.CELL_TYPE_STRING == row_1_index.getCell(row_1_index_cell).getCellType()) { 
										value = row_1_index.getCell(row_1_index_cell).getStringCellValue();
									}
								}
								if(!StringUtils.isEmpty(value) && value!=null){
									UtilityFile.removeJunkCharacters(value);
								}
								HSSFCell celln = row.createCell(colNum++);
								if(applyDateStyle){
									celln.setCellStyle(dateCellStyle);
								}
								celln.setCellValue(value);
							}
						}
					}
				} else if(RPAConstants.CITI.equals(bankName)) {
					for (int success_workbook_sheet_row_1 = success_workbook_sheet_total_row; success_workbook_sheet_row_1 >=1; success_workbook_sheet_row_1--) { // traversing
						HSSFRow row_1_index = success_workbook_sheet.getRow(success_workbook_sheet_row_1);
						int row_1_max = row_1_index.getLastCellNum();
						HSSFRow row = sheet.createRow(rowNum++);
						int colNum = 0;
						for (int row_1_index_cell = 0; row_1_index_cell < row_1_max; row_1_index_cell++) {
							String value = "";
							if(row_1_index_cell==1 || row_1_index_cell==3 || row_1_index_cell==14){
								if (HSSFCell.CELL_TYPE_NUMERIC == row_1_index.getCell(row_1_index_cell).getCellType()) { 
									if(DateUtil.isCellDateFormatted(row_1_index.getCell(row_1_index_cell))){
										value = simpleDateFormatter.format(row_1_index.getCell(row_1_index_cell).getDateCellValue()).toString();
									} else {
										value = NumberToTextConverter.toText(row_1_index.getCell(row_1_index_cell).getNumericCellValue());
									}
								}
							} else {
								if (HSSFCell.CELL_TYPE_NUMERIC == row_1_index.getCell(row_1_index_cell).getCellType()) { 
									if(row_1_index_cell==6 || row_1_index_cell==12){
										value = Double.toString(row_1_index.getCell(row_1_index_cell).getNumericCellValue());
									} else {
										value = NumberToTextConverter.toText(row_1_index.getCell(row_1_index_cell).getNumericCellValue());
									}
								} else if(HSSFCell.CELL_TYPE_STRING == row_1_index.getCell(row_1_index_cell).getCellType()) { 
									value = row_1_index.getCell(row_1_index_cell).getStringCellValue();
								} 
							}
							if(!StringUtils.isEmpty(value) && value!=null){
								UtilityFile.removeJunkCharacters(value);
							}
							HSSFCell celln = row.createCell(colNum++);
							celln.setCellValue(value);
						}
					}		
				} else if(RPAConstants.AXIS.equals(bankName)) {
					// AXIS Dropped from Requirement - Future Inclusion
				} else if(RPAConstants.HSBC.equals(bankName)) {
					for (int success_workbook_sheet_row_1 = success_workbook_sheet_total_row; success_workbook_sheet_row_1 >=1; success_workbook_sheet_row_1--) { // traversing
						HSSFRow row_1_index = success_workbook_sheet.getRow(success_workbook_sheet_row_1);
						int row_1_max = row_1_index.getLastCellNum();
						HSSFRow row = sheet.createRow(rowNum++);
						int colNum = 0;
						HSSFCell cell1 = row.createCell(colNum++);
						cell1.setCellValue(UtilityFile.createSpecifiedDateFormat(RPAConstants.dd_slash_MM_slash_yyyy));
						for (int row_1_index_cell = 0; row_1_index_cell < row_1_max; row_1_index_cell++) {
							String value = "";
							if(row_1_index_cell==5 || row_1_index_cell==6 || row_1_index_cell==10 || row_1_index_cell==12 || row_1_index_cell==20){
								if (HSSFCell.CELL_TYPE_NUMERIC == row_1_index.getCell(row_1_index_cell).getCellType()) { 
									if(DateUtil.isCellDateFormatted(row_1_index.getCell(row_1_index_cell))){
										value = simpleDateFormatter.format(row_1_index.getCell(row_1_index_cell).getDateCellValue()).toString();
									} else {
										value = NumberToTextConverter.toText(row_1_index.getCell(row_1_index_cell).getNumericCellValue());
									}
								}
							} else {
								if (HSSFCell.CELL_TYPE_NUMERIC == row_1_index.getCell(row_1_index_cell).getCellType()) { 
									if(row_1_index_cell==4 || row_1_index_cell==13 || row_1_index_cell==19){
										value = Double.toString(row_1_index.getCell(row_1_index_cell).getNumericCellValue());
									} else {
										value = NumberToTextConverter.toText(row_1_index.getCell(row_1_index_cell).getNumericCellValue());
									}
								} else if(HSSFCell.CELL_TYPE_STRING == row_1_index.getCell(row_1_index_cell).getCellType()) { 
									value = row_1_index.getCell(row_1_index_cell).getStringCellValue();
								} 
							}
							if(!StringUtils.isEmpty(value) && value!=null){
								UtilityFile.removeJunkCharacters(value);
							}
							HSSFCell celln = row.createCell(colNum++);
							celln.setCellValue(value);
						}
					}
				} else if(RPAConstants.SCB.equals(bankName)) {
					for (int success_workbook_sheet_row_1 = success_workbook_sheet_total_row; success_workbook_sheet_row_1 >=1; success_workbook_sheet_row_1--) { // traversing
						HSSFRow row_1_index = success_workbook_sheet.getRow(success_workbook_sheet_row_1);
						int row_1_max = row_1_index.getLastCellNum();
						HSSFRow row = sheet.createRow(rowNum++);
						int colNum = 0;
						for (int row_1_index_cell = 0; row_1_index_cell < row_1_max; row_1_index_cell++) {
							String value = "";
							if(row_1_index_cell==4 || row_1_index_cell==8 || row_1_index_cell==10 || row_1_index_cell==14){
								if (HSSFCell.CELL_TYPE_NUMERIC == row_1_index.getCell(row_1_index_cell).getCellType()) { 
									if(DateUtil.isCellDateFormatted(row_1_index.getCell(row_1_index_cell))){
										value = simpleDateFormatter.format(row_1_index.getCell(row_1_index_cell).getDateCellValue()).toString();
									} else {
										value = NumberToTextConverter.toText(row_1_index.getCell(row_1_index_cell).getNumericCellValue());
									}
								}
							} else {
								if (HSSFCell.CELL_TYPE_NUMERIC == row_1_index.getCell(row_1_index_cell).getCellType()) { 
									if(row_1_index_cell==3 || row_1_index_cell==11 || row_1_index_cell==17){
										value = Double.toString(row_1_index.getCell(row_1_index_cell).getNumericCellValue());
									} else {
										value = NumberToTextConverter.toText(row_1_index.getCell(row_1_index_cell).getNumericCellValue());
									}
								} else if(HSSFCell.CELL_TYPE_STRING == row_1_index.getCell(row_1_index_cell).getCellType()) { 
									value = row_1_index.getCell(row_1_index_cell).getStringCellValue();
								}
							}
							if(!StringUtils.isEmpty(value) && value!=null){
								UtilityFile.removeJunkCharacters(value);
							}
							HSSFCell celln = row.createCell(colNum++);
							celln.setCellValue(value);
						}
					}
				} 
			}
			success_workbook.close();

			String directoryName = UtilityFile.getCodeBasePath() + UtilityFile.getUploadProperty(uploadBaseFolderPath);
			
			String uploadFilePath = directoryName +"/"+transactionInfo.getProcessName()+"_UPLOAD_"+fileSuffix+".xls";
			
			UtilityFile.writeExcelFileByPath(directoryName, uploadFilePath, upload_workbook);
			upload_workbook.close();

			exchange.setProperty(RPAConstants.UPLOAD_FILE_PATH, uploadFilePath);
			transactionInfo.setUploadFileDownload(uploadFilePath);
		} 

		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("END - VB64ComplianceTransformer");
	}
}