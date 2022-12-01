/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpa.constants.RPAConstants;
import com.rpa.util.UtilityFile;

public class Splitter {

	XSSFWorkbook validationErrorWorkBook = null;
	XSSFSheet validationErrorSheet = null;
	XSSFRow validationErrorRow = null;
	int errorrowNum = 0;

	private final String fileName;
	private int maxRows;
	private final String path;
	public static int filecount;
	public static int rowcounter;

	private static final Logger logger = LoggerFactory.getLogger(Splitter.class.getName());

	public Splitter(String fileName, int maxRows, String filepath, String[] headers, Integer[] dateColumns)
			throws InvalidFormatException, IOException {

		logger.info("Splitting Excel Process started for the file :: " + fileName);

		path = filepath;
		ZipSecureFile.setMinInflateRatio(0);
		this.fileName = fileName;
		this.maxRows = maxRows;
		Workbook workbook = null;

		try {
			
			workbook = WorkbookFactory.create(new File(fileName));

			Sheet sheet = workbook.getSheetAt(0);

			Row headerRow = sheet.getRow(0);
			for (int c = 0; c < headers.length; c++) {
				Cell headerCells = headerRow.getCell(c);
				headerCells.setCellValue(headers[c]);
			}

			logger.info("Splitting Excel Process - sheet row count :: " + sheet.getPhysicalNumberOfRows());

			if (sheet.getPhysicalNumberOfRows() >= maxRows) {
				logger.info("Splitting Excel Process max rows crossed splitWorkbook called to write  ");
				splitWorkbook(workbook, headers, dateColumns);
				// writeWorkBook(wb, path);
			} else {
				maxRows = sheet.getPhysicalNumberOfRows();
				splitWorkbook(workbook, headers, dateColumns);
				//FileUtils.copyFileToDirectory(new File(fileName), new File(filepath));
				logger.info("Splitting Excel Process - sheet is not having enough row to split :: "
						+ sheet.getPhysicalNumberOfRows());
			}
			
			logger.info("Splitting Excel Process ends");
		} catch (IOException e) {
			logger.error("IOException in Splitting Process constructor  ::  " + e.getMessage());
		} catch (Exception e) {
			logger.error("Error in Splitting Process constructor ::  " + e.getMessage());
		}finally{
			if(workbook!=null){
				workbook.close();
			}
		}
	}

	@SuppressWarnings("deprecation")
	private boolean splitWorkbook(Workbook workbook, String[] headers, Integer[] dateColumns) throws IOException {
		logger.info("Splitting excel - split workbook method called");
		// List<SXSSFWorkbook> workbooks = new ArrayList<SXSSFWorkbook>();

		SXSSFWorkbook wb = new SXSSFWorkbook(100);

		CellStyle clone = wb.createCellStyle();

		SXSSFSheet sh = wb.createSheet();

		SXSSFRow newRow;
		SXSSFCell newCell;

		int rowCount = 0;
		int colCount = 0;
		int headflag = 0;

		Sheet sheet = workbook.getSheetAt(0);
		rowcounter++;

		int fileCounter = 0;

		for (Row row : sheet) {
			/* Time to create a new workbook? */
			if (rowCount == maxRows) {
				logger.info("Splitting excel - Rolling to another file since Maximum limit of rows :: " + maxRows
						+ " is reached.");
				headflag = 1;
				// workbooks.add(wb);

				writeWorkBook(wb, path, fileCounter);
				fileCounter++;
				wb = new SXSSFWorkbook();
				clone = wb.createCellStyle();
				sh = wb.createSheet();
				rowCount = 0;
			}

			if (headflag == 1) {
				newRow = sh.createRow(rowCount++);
				headflag = 0;
				for (int k = 0; k < headers.length; k++) {
					newCell = newRow.createCell(colCount++);
					newCell.setCellValue(headers[k]);
				}
				colCount = 0;

			}

			newRow = sh.createRow(rowCount++);
			for (int cn = 0; cn < row.getLastCellNum(); cn++) {
				Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				newCell = newRow.createCell(cn);
				if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
					if (ArrayUtils.contains(dateColumns, cn)) {
						newCell.setCellValue(
								UtilityFile.dateToSting(cell.getDateCellValue(), RPAConstants.dd_slash_MM_slash_yyyy));
					} else {
						newCell.setCellValue(cell.getNumericCellValue());
					}
				} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell)) {
					clone.cloneStyleFrom(cell.getCellStyle());
					newCell.setCellStyle(clone);
					newCell.setCellValue(cell.getDateCellValue());
				} else {
					newCell.setCellValue(cell.toString());
				}
			}
			/* } */

			rowcounter++;
		}

		/* Only add the last workbook if it has content */
		if (wb.getSheetAt(0).getPhysicalNumberOfRows() > 0) {
			writeWorkBook(wb, path, fileCounter);
		}
		if(wb!=null){
			wb.close();
		}
		return true;
	}

	/* Write the workbooks to disk. */
	private void writeWorkBook(SXSSFWorkbook wb, String path, int fileCounter) throws IOException {
		FileOutputStream out = null;

		try {
			String newFileName = fileName.substring(fileName.lastIndexOf("/") + 1);
			newFileName = newFileName.substring(0, newFileName.lastIndexOf("."));
			File newFileFolder = new File(path);
			if (!newFileFolder.exists()) {
				newFileFolder.mkdirs();
			}
			out = new FileOutputStream(
					new File(newFileFolder + "\\" + newFileName + "-" + (fileCounter + 1) + ".xlsx"));
			wb.write(out);
			//out.close();
			logger.info("Splitting excel - content Written on :: " + newFileFolder + "\\" + newFileName + "-"
					+ (fileCounter + 1) + ".xlsx");
			filecount++;
			wb.close();
			wb.dispose();
		} catch (IOException e) {
			logger.error("Error in WriteWorkBooks method of Splitting Process ::  " + e.getMessage());
		}finally{
			if(out!=null)
				out.close();
		}

	}

	public int sendtotalrows() {
		return rowcounter;
	}

}
