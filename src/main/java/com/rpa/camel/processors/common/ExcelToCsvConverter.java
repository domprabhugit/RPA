/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpa.response.LongueUploadResponse;

public class ExcelToCsvConverter {

	private static final Logger logger = LoggerFactory.getLogger(ExcelToCsvConverter.class.getName());

	@SuppressWarnings("deprecation")
	public static int convertToCsv(File inputFile, File outputFile, String columnDelimitter, String lineDelimitter)
			throws IOException {
		logger.info("ExcelToCsvConverter -  convertToCsv() called filename:: " + inputFile.getName());
		StringBuffer bf = new StringBuffer();
		FileOutputStream fos = null;
		String strGetValue = "";
		XSSFWorkbook wb = null;
		OPCPackage opcPackage = null;
		try {
			fos = new FileOutputStream(outputFile);
			logger.info("start time before excelfile read by OPCPackage :: " + System.currentTimeMillis());
			opcPackage = OPCPackage.open(inputFile);
			wb = new XSSFWorkbook(opcPackage);
			logger.info("end time after excelfile read by OPCPackage :: " + System.currentTimeMillis());
			XSSFSheet sheet = wb.getSheetAt(0);
			Row row;
			Cell cell;
			int intRowCounter = 0;
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {

				StringBuffer cellDData = new StringBuffer();
				row = rowIterator.next();
				int maxNumOfCells = sheet.getRow(0).getLastCellNum();
				int cellCounter = 0;
				while ((cellCounter) < maxNumOfCells) {
					if (sheet.getRow(row.getRowNum()) != null
							&& sheet.getRow(row.getRowNum()).getCell(cellCounter) != null) {
						cell = sheet.getRow(row.getRowNum()).getCell(cellCounter);
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_BOOLEAN:
							strGetValue = cell.getBooleanCellValue() + columnDelimitter;
							cellDData.append(removeSpace(strGetValue));
							break;
						case Cell.CELL_TYPE_NUMERIC:
							strGetValue = new BigDecimal(cell.getNumericCellValue()).toPlainString();
							if (DateUtil.isCellDateFormatted(cell)) {
								strGetValue = new DataFormatter().formatCellValue(cell);
							} else {
								strGetValue = new BigDecimal(cell.getNumericCellValue()).toPlainString();
							}
							String tempStrGetValue = removeSpace(strGetValue);
							if (tempStrGetValue.length() == 0) {
								strGetValue = " " + columnDelimitter;
								cellDData.append(strGetValue);
							} else {
								strGetValue = strGetValue + columnDelimitter;
								cellDData.append(removeSpace(strGetValue));
							}
							break;
						case Cell.CELL_TYPE_STRING:

							strGetValue = cell.getStringCellValue();
							String tempStrGetValue1 = removeSpace(strGetValue);
							if (tempStrGetValue1.length() == 0) {
								strGetValue = " " + columnDelimitter;
								cellDData.append(strGetValue);
							} else {
								if (DateUtil.isCellDateFormatted(cell)) {
									strGetValue = new DataFormatter().formatCellValue(cell) + columnDelimitter;
								} else {
									strGetValue = strGetValue + columnDelimitter;
								}
								cellDData.append(removeSpace(strGetValue));
							}
							break;
						case Cell.CELL_TYPE_BLANK:
							strGetValue = "" + columnDelimitter;
							cellDData.append(removeSpace(strGetValue));
							break;
						default:
							strGetValue = cell + columnDelimitter;
							cellDData.append(removeSpace(strGetValue));
						}
					} else {
						strGetValue = " " + columnDelimitter;
						cellDData.append(strGetValue);
					}
					cellCounter++;
				}
				String temp = cellDData.toString();
				bf.append(temp.trim());
				bf.append(lineDelimitter);
				bf.append("\n");
				intRowCounter++;
			}

			fos.write(bf.toString().getBytes());
			fos.close();
			logger.info("ExcelToCsvConverter -  convertToCsv() end");
			return intRowCounter;
		} catch (Exception ex) {
			logger.error("error in convertToCsv() ::", ex);
			return 0;
		} finally {
			try {
				if (fos != null)
					fos.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (wb != null) {
				wb.close();
			}
			if(opcPackage!=null){
				opcPackage.revert();
			}
		}
	}

	private static String removeSpace(String strString) {
		if (strString != null && !strString.equals("")) {
			return strString.trim();
		}
		return strString;
	}

	@SuppressWarnings("deprecation")
	public static LongueUploadResponse getAsCsvString(File inputFile, String columnDelimitter, String lineDelimitter,
			boolean firstRow) throws IOException {
		logger.info(" ExcelToCsvConverter getAsCsvString() called ");
		StringBuffer bf = new StringBuffer();
		String strGetValue = "";
		XSSFWorkbook wb = null;
		OPCPackage opcPackage = null;
		LongueUploadResponse longueUploadResponse = new LongueUploadResponse();
		try {
			logger.info("start time before excelfile read by OPCPackage :: " + System.currentTimeMillis());
			 opcPackage = OPCPackage.open(inputFile);
			wb = new XSSFWorkbook(opcPackage);
			logger.info("end time after excelfile read by OPCPackage :: " + System.currentTimeMillis());
			XSSFSheet sheet = wb.getSheetAt(0);
			logger.info(" ExcelToCsvConverter getAsCsvString() - current sheet last row number :: "+sheet.getLastRowNum());
			Row row;
			Cell cell;
			int intRowCounter = 0;
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				if (firstRow && intRowCounter == 0) {
					rowIterator.next();
					logger.info("Header Skipped");
					intRowCounter++;
				} else {
					StringBuffer cellDData = new StringBuffer();
					row = rowIterator.next();
					int maxNumOfCells = sheet.getRow(0).getLastCellNum();
					int cellCounter = 0;
					while ((cellCounter) < maxNumOfCells) {
						if (sheet.getRow(row.getRowNum()) != null
								&& sheet.getRow(row.getRowNum()).getCell(cellCounter) != null) {
							cell = sheet.getRow(row.getRowNum()).getCell(cellCounter);
							switch (cell.getCellType()) {
							case Cell.CELL_TYPE_BOOLEAN:
								strGetValue = cell.getBooleanCellValue() + columnDelimitter;
								cellDData.append(removeSpace(strGetValue));
								break;
							case Cell.CELL_TYPE_NUMERIC:
								strGetValue = new BigDecimal(cell.getNumericCellValue()).toPlainString();
								if (DateUtil.isCellDateFormatted(cell)) {
									strGetValue = new DataFormatter().formatCellValue(cell);
								} else {
									strGetValue = new BigDecimal(cell.getNumericCellValue()).toPlainString();
								}
								String tempStrGetValue = removeSpace(strGetValue);
								if (tempStrGetValue.length() == 0) {
									strGetValue = " " + columnDelimitter;
									cellDData.append(strGetValue);
								} else {
									strGetValue = strGetValue + columnDelimitter;
									cellDData.append(removeSpace(strGetValue));
								}
								break;
							case Cell.CELL_TYPE_STRING:
								strGetValue = cell.getStringCellValue();
								String tempStrGetValue1 = removeSpace(strGetValue);
								if (tempStrGetValue1.length() == 0) {
									strGetValue = " " + columnDelimitter;
									cellDData.append(strGetValue);
								} else {
									strGetValue = strGetValue + columnDelimitter;
									cellDData.append(removeSpace(strGetValue));
								}
								break;
							case Cell.CELL_TYPE_BLANK:
								strGetValue = "" + columnDelimitter;
								cellDData.append(removeSpace(strGetValue));
								break;
							default:
								strGetValue = cell + columnDelimitter;
								cellDData.append(removeSpace(strGetValue));
							}
						} else {
							strGetValue = " " + columnDelimitter;
							cellDData.append(strGetValue);
						}
						cellCounter++;
					}
					String temp = cellDData.toString();
					bf.append(temp.trim());
					bf.append(lineDelimitter);
					bf.append("\n");
					intRowCounter++;
				}
			}

		/*	if (intRowCounter > 0) {
				intRowCounter = intRowCounter;
			}*/
			longueUploadResponse.setRowCount(intRowCounter);
			longueUploadResponse.setCsvBuffer(bf);
			return longueUploadResponse;
		} catch (Exception ex) {
			logger.error("error in getAsCsvString() ::", ex);
			return null;
		} finally {
			if (wb != null) {
				wb.close();
			}
			if(opcPackage!=null){
				opcPackage.revert();
			}
		}
	}

}
