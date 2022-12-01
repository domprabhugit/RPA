/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.processors.common;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelCopyRowWithRemarks {

	private static final Logger logger = LoggerFactory.getLogger(ExcelCopyRowWithRemarks.class.getName());
	
	
	@SuppressWarnings("deprecation")
	public void copyRow(HSSFWorkbook workbook, HSSFSheet worksheet, HSSFWorkbook output_workbook, 
			HSSFSheet output_workbook_sheet, int sourceRowNum, int destinationRowNum, HSSFCellStyle clone, short totalColumnCount, String remarks, Integer[] dateCoumnIndex, Map<Integer, HSSFCellStyle> styleMap) {
		
		SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("dd/MM/yyyy");

		// Get the source / new row
		HSSFRow newRow = null;
		HSSFRow sourceRow = null;
		// copy headers
		if(destinationRowNum==1){
			newRow = output_workbook_sheet.createRow(0);
			sourceRow = worksheet.getRow(0);

			for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
				HSSFCell oldCell = sourceRow.getCell(i);
				HSSFCell newCell = newRow.createCell(i);

				// Copy style from old cell and apply to new cell
				clone.cloneStyleFrom(oldCell.getCellStyle());
				newCell.setCellStyle(clone);

				// If there is a cell comment, copy
				if (oldCell.getCellComment() != null) {
					newCell.setCellComment(oldCell.getCellComment());
				}

				// If there is a cell hyperlink, copy
				if (oldCell.getHyperlink() != null) {		
					newCell.setHyperlink(oldCell.getHyperlink());
				}

				// Set the cell data type
				newCell.setCellType(oldCell.getCellType());

				// Set the cell data value
				switch (oldCell.getCellType()) {
				case Cell.CELL_TYPE_BLANK:
					newCell.setCellValue(oldCell.getStringCellValue());
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					newCell.setCellValue(oldCell.getBooleanCellValue());
					break;
				case Cell.CELL_TYPE_ERROR:
					newCell.setCellErrorValue(oldCell.getErrorCellValue());
					break;
				case Cell.CELL_TYPE_FORMULA:
					newCell.setCellFormula(oldCell.getCellFormula());
					break;
				case Cell.CELL_TYPE_NUMERIC:
					newCell.setCellValue(oldCell.getNumericCellValue());
					break;
				case Cell.CELL_TYPE_STRING:
					newCell.setCellValue(oldCell.getRichStringCellValue());
					break;
				}
			} 
			if(remarks!="NA"){
				HSSFCell newCell = newRow.createCell(sourceRow.getLastCellNum());
				newCell.setCellValue("Remarks");
			}
		}

		newRow = output_workbook_sheet.createRow(destinationRowNum);
		sourceRow = worksheet.getRow(sourceRowNum);

		// Loop through source columns to add to new row
		for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
			// Grab a copy of the old/new cell
			HSSFCell oldCell = sourceRow.getCell(i);
			HSSFCell newCell = newRow.createCell(i);

			// If the old cell is null jump to next cell
			if (oldCell == null) {
				newCell = null;
				continue;
			}

			if(styleMap != null) {
	            if(oldCell.getSheet().getWorkbook() == newCell.getSheet().getWorkbook()){
	                newCell.setCellStyle(oldCell.getCellStyle());
	            } else{
	                int stHashCode = oldCell.getCellStyle().hashCode();
	                HSSFCellStyle newCellStyle = styleMap.get(stHashCode);
	                if(newCellStyle == null){
	                    newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();
	                    newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
	                    styleMap.put(stHashCode, newCellStyle);
	                }
	                newCell.setCellStyle(newCellStyle);
	                
	            }
	        }

			// If there is a cell comment, copy
			if (oldCell.getCellComment() != null) {
				newCell.setCellComment(oldCell.getCellComment());
			}

			// If there is a cell hyperlink, copy
			if (oldCell.getHyperlink() != null) {
				newCell.setHyperlink(oldCell.getHyperlink());
			}

			// Set the cell data type
			newCell.setCellType(oldCell.getCellType());

			// Set the cell data value
			switch (oldCell.getCellType()) {
			case Cell.CELL_TYPE_BLANK:
				newCell.setCellValue(oldCell.getStringCellValue());
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				newCell.setCellValue(oldCell.getBooleanCellValue());
				break;
			case Cell.CELL_TYPE_ERROR:
				newCell.setCellErrorValue(oldCell.getErrorCellValue());
				break;
			case Cell.CELL_TYPE_FORMULA:
				newCell.setCellFormula(oldCell.getCellFormula());
				break;
			case Cell.CELL_TYPE_NUMERIC:
				if(DateUtil.isCellDateFormatted(oldCell)){
					newCell.setCellValue(simpleDateFormatter.format(oldCell.getDateCellValue()).toString());
				} else {
				/*	newCell.setCellValue(NumberToTextConverter.toText(oldCell.getNumericCellValue()));*/
					newCell.setCellValue(oldCell.getNumericCellValue());
				}
				break;
			case Cell.CELL_TYPE_STRING:
				newCell.setCellValue(oldCell.getRichStringCellValue());
				break;
			}
		}

		if(remarks!="NA"){
			HSSFCell newCell = newRow.createCell(totalColumnCount+1);
			newCell.setCellValue(remarks);
		}
	}

	@SuppressWarnings("deprecation")
	public void copyRow(XSSFWorkbook workbook, XSSFSheet worksheet, SXSSFWorkbook output_workbook,
			SXSSFSheet output_workbook_sheet, int sourceRowNum, int destinationRowNum, CellStyle clone,
			short totalColumnCount, String remarks) {

		logger.info("ExcelCopyRowWithRemarks - inside copyRow() method ");

		SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("dd/MM/yyyy");

		// Get the source / new row
		SXSSFRow newRow = null;
		XSSFRow sourceRow = null;
		// copy headers
		if (destinationRowNum == 1) {
			newRow = output_workbook_sheet.createRow(0);
			sourceRow = worksheet.getRow(0);

			for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
				XSSFCell oldCell = sourceRow.getCell(i);
				SXSSFCell newCell = newRow.createCell(i);

				// Copy style from old cell and apply to new cell
				clone.cloneStyleFrom(oldCell.getCellStyle());
				newCell.setCellStyle(clone);

				// If there is a cell comment, copy
				if (oldCell.getCellComment() != null) {
					newCell.setCellComment(oldCell.getCellComment());
				}

				// If there is a cell hyperlink, copy
				if (oldCell.getHyperlink() != null) {
					newCell.setHyperlink(oldCell.getHyperlink());
				}

				// Set the cell data type
				newCell.setCellType(oldCell.getCellType());

				// Set the cell data value
				switch (oldCell.getCellType()) {
				case Cell.CELL_TYPE_BLANK:
					newCell.setCellValue(oldCell.getStringCellValue());
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					newCell.setCellValue(oldCell.getBooleanCellValue());
					break;
				case Cell.CELL_TYPE_ERROR:
					newCell.setCellErrorValue(oldCell.getErrorCellValue());
					break;
				case Cell.CELL_TYPE_FORMULA:
					newCell.setCellFormula(oldCell.getCellFormula());
					break;
				case Cell.CELL_TYPE_NUMERIC:
					newCell.setCellValue(oldCell.getNumericCellValue());
					break;
				case Cell.CELL_TYPE_STRING:
					newCell.setCellValue(oldCell.getRichStringCellValue());
					break;
				}
			}
			if (remarks != "NA") {
				SXSSFCell newCell = newRow.createCell(sourceRow.getLastCellNum());
				newCell.setCellValue("Remarks");
			}
		}

		newRow = output_workbook_sheet.createRow(destinationRowNum);
		sourceRow = worksheet.getRow(sourceRowNum);

		// Loop through source columns to add to new row
		for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
			// Grab a copy of the old/new cell
			XSSFCell oldCell = sourceRow.getCell(i);
			SXSSFCell newCell = newRow.createCell(i);

			// If the old cell is null jump to next cell
			if (oldCell == null) {
				newCell = null;
				continue;
			}

			// Copy style from old cell and apply to new cell
			clone.cloneStyleFrom(oldCell.getCellStyle());
			newCell.setCellStyle(clone);

			// If there is a cell comment, copy
			if (oldCell.getCellComment() != null) {
				newCell.setCellComment(oldCell.getCellComment());
			}

			// If there is a cell hyperlink, copy
			if (oldCell.getHyperlink() != null) {
				newCell.setHyperlink(oldCell.getHyperlink());
			}

			// Set the cell data type
			newCell.setCellType(oldCell.getCellType());

			// Set the cell data value
			switch (oldCell.getCellType()) {
			case Cell.CELL_TYPE_BLANK:
				newCell.setCellValue(oldCell.getStringCellValue());
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				newCell.setCellValue(oldCell.getBooleanCellValue());
				break;
			case Cell.CELL_TYPE_ERROR:
				newCell.setCellErrorValue(oldCell.getErrorCellValue());
				break;
			case Cell.CELL_TYPE_FORMULA:
				newCell.setCellFormula(oldCell.getCellFormula());
				break;
			case Cell.CELL_TYPE_NUMERIC:
				if (DateUtil.isCellDateFormatted(oldCell)) {
					newCell.setCellValue(simpleDateFormatter.format(oldCell.getDateCellValue()).toString());
				} else {
					newCell.setCellValue(NumberToTextConverter.toText(oldCell.getNumericCellValue()));
				}
				break;
			case Cell.CELL_TYPE_STRING:
				newCell.setCellValue(oldCell.getRichStringCellValue());
				break;
			}
		}

		if (remarks != "NA") {
			SXSSFCell newCell = newRow.createCell(totalColumnCount + 1);
			newCell.setCellValue(remarks);
		}

		logger.info("ExcelCopyRowWithRemarks - copyRow() end ");
	}

	@SuppressWarnings("deprecation")
	public int copyMultipleRow(XSSFWorkbook input_workbook, XSSFSheet input_workbook_sheet,
			SXSSFWorkbook output_workbook, SXSSFSheet output_workbook_sheet, CellStyle clone, short totalColumnCount,
			Map<Integer, String> map) throws URISyntaxException, InvalidFormatException, IOException {

		logger.info("ExcelCopyRowWithRemarks - inside copyMultipleRow() method ");

		SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("dd/MM/yyyy");

		// Get the source / new row
		SXSSFRow newRow = null;
		XSSFRow sourceRow = null;

		int destinationRowNum = 0;
		for (Map.Entry<Integer, String> entry : map.entrySet()) {
			// copy headers
			if (destinationRowNum == 0) {

				newRow = output_workbook_sheet.createRow(0);
				sourceRow = input_workbook_sheet.getRow(0);

				for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
					XSSFCell oldCell = sourceRow.getCell(i);
					SXSSFCell newCell = newRow.createCell(i);

					// Copy style from old cell and apply to new cell
					clone.cloneStyleFrom(oldCell.getCellStyle());
					newCell.setCellStyle(clone);

					// If there is a cell comment, copy
					if (oldCell.getCellComment() != null) {
						newCell.setCellComment(oldCell.getCellComment());
					}

					// If there is a cell hyperlink, copy
					if (oldCell.getHyperlink() != null) {
						newCell.setHyperlink(oldCell.getHyperlink());
					}

					// Set the cell data type
					newCell.setCellType(oldCell.getCellType());

					// Set the cell data value
					switch (oldCell.getCellType()) {
					case Cell.CELL_TYPE_BLANK:
						newCell.setCellValue(oldCell.getStringCellValue());
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						newCell.setCellValue(oldCell.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_ERROR:
						newCell.setCellErrorValue(oldCell.getErrorCellValue());
						break;
					case Cell.CELL_TYPE_FORMULA:
						newCell.setCellFormula(oldCell.getCellFormula());
						break;
					case Cell.CELL_TYPE_NUMERIC:
						newCell.setCellValue(oldCell.getNumericCellValue());
						break;
					case Cell.CELL_TYPE_STRING:
						newCell.setCellValue(oldCell.getRichStringCellValue());
						break;
					}
				}
				if (entry.getValue() != "NA") {
					SXSSFCell newCell = newRow.createCell(sourceRow.getLastCellNum());
					newCell.setCellValue("Error Remarks");
				}
			}
			destinationRowNum++;
			
				newRow = output_workbook_sheet.createRow(destinationRowNum);
				sourceRow = input_workbook_sheet.getRow(entry.getKey());

				// Loop through source columns to add to new row
				for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
					// Grab a copy of the old/new cell
					XSSFCell oldCell = sourceRow.getCell(i);
					SXSSFCell newCell = newRow.createCell(i);

					// If the old cell is null jump to next cell
					if (oldCell == null) {
						newCell = null;
						continue;
					}

					// If there is a cell comment, copy
					if (oldCell.getCellComment() != null) {
						newCell.setCellComment(oldCell.getCellComment());
					}

					// If there is a cell hyperlink, copy
					if (oldCell.getHyperlink() != null) {
						newCell.setHyperlink(oldCell.getHyperlink());
					}

					// Set the cell data type
					newCell.setCellType(oldCell.getCellType());

					// Set the cell data value
					switch (oldCell.getCellType()) {
					case Cell.CELL_TYPE_BLANK:
						newCell.setCellValue(oldCell.getStringCellValue());
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						newCell.setCellValue(oldCell.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_ERROR:
						newCell.setCellErrorValue(oldCell.getErrorCellValue());
						break;
					case Cell.CELL_TYPE_FORMULA:
						newCell.setCellFormula(oldCell.getCellFormula());
						break;
					case Cell.CELL_TYPE_NUMERIC:
						if (DateUtil.isCellDateFormatted(oldCell)) {
							newCell.setCellValue(simpleDateFormatter.format(oldCell.getDateCellValue()).toString());
						} else {
							newCell.setCellValue(NumberToTextConverter.toText(oldCell.getNumericCellValue()));
						}
						break;
					case Cell.CELL_TYPE_STRING:
						newCell.setCellValue(oldCell.getRichStringCellValue());
						break;
					}
				}

				if (entry.getValue() != "NA" && !entry.getValue().equals("") && totalColumnCount>0) {
					SXSSFCell newCell = newRow.createCell(totalColumnCount + 1);
					newCell.setCellValue(entry.getValue());
				}
			
		}
		logger.info("ExcelCopyRowWithRemarks - copyMultipleRow() end, last row number  :: " + destinationRowNum);
		return destinationRowNum;
	}

}