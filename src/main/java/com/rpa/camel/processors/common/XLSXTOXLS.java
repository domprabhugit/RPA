package com.rpa.camel.processors.common;

import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class XLSXTOXLS {

	@SuppressWarnings("deprecation")
	public static HSSFWorkbook convertWorkbookXSSFToHSSF(SXSSFWorkbook wbIn) throws InvalidFormatException,IOException {

		HSSFWorkbook wbOut = new HSSFWorkbook();
		int sheetCnt = wbIn.getNumberOfSheets();
		for (int i = 0; i < sheetCnt; i++) {
			Sheet sIn = wbIn.getSheetAt(0);
			Sheet sOut = wbOut.createSheet(sIn.getSheetName());
			Iterator<Row> rowIt = sIn.rowIterator();
			while (rowIt.hasNext()) {
				Row rowIn = rowIt.next();
				Row rowOut = sOut.createRow(rowIn.getRowNum());

				Iterator<Cell> cellIt = rowIn.cellIterator();
				while (cellIt.hasNext()) {
					Cell cellIn = cellIt.next();
					Cell cellOut = rowOut.createCell(cellIn.getColumnIndex(), cellIn.getCellType());

					switch (cellIn.getCellType()) {
					case Cell.CELL_TYPE_BLANK: 
						break;

					case Cell.CELL_TYPE_BOOLEAN:
						cellOut.setCellValue(cellIn.getBooleanCellValue());
						break;

					case Cell.CELL_TYPE_ERROR:
						cellOut.setCellValue(cellIn.getErrorCellValue());
						break;

					case Cell.CELL_TYPE_FORMULA:
						cellOut.setCellFormula(cellIn.getCellFormula());
						break;

					case Cell.CELL_TYPE_NUMERIC:
						cellOut.setCellValue(cellIn.getNumericCellValue());
						break;

					case Cell.CELL_TYPE_STRING:
						cellOut.setCellValue(cellIn.getStringCellValue());
						break;
					}
					CellStyle styleIn = cellIn.getCellStyle();
					CellStyle styleOut = cellOut.getCellStyle();
					styleOut.setDataFormat(styleIn.getDataFormat());
					cellOut.setCellComment(cellIn.getCellComment());
				}
			}
		}
		return wbOut;
	}
}