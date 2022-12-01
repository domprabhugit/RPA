package com.rpa.camel.processors.common;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XlsxConverter {
	
	private static final Logger logger = LoggerFactory.getLogger(XlsxConverter.class.getName());
	
	public void convertToXlsx(String csvFileName,String xlsxFileName,String fieldTerminater, String lineTerminater) throws IOException{
		logger.info("Inside convertToXlsx()  conversion started from csv file :: "+csvFileName); 
		XSSFWorkbook workBook = new XSSFWorkbook();
		BufferedReader br = null;
		try {
			XSSFSheet sheet = workBook.createSheet("sheet1");
			String currentLine = null;
			int RowNum = 0;
			StringBuilder csvString = new StringBuilder();
			br = new BufferedReader(new FileReader(csvFileName));
			while ((currentLine = br.readLine()) != null) {
				csvString .append(currentLine);
			}
			currentLine = null;
			String[] rowArr = csvString.toString().split(lineTerminater);
			for(int i = 0 ; i <rowArr.length ; i++ ){
				currentLine = rowArr[i];
				String str[] = currentLine.split(fieldTerminater);
				XSSFRow currentRow = sheet.createRow(RowNum);
				for (int k = 0; k < str.length; k++) {
					currentRow.createCell(k).setCellValue(str[k]);
				}
				RowNum++;
			}
			
			
			FileOutputStream fileOutputStream = new FileOutputStream(xlsxFileName);
			workBook.write(fileOutputStream);
			fileOutputStream.close();
			logger.info("End convertToXlsx() Total rows of conversion :: "+RowNum); 
		} catch (Exception ex) {
			logger.error("Error in  convertToXlsx()  :: ",ex); 
		}finally{
			if(br!=null){
				br.close();
			}
			if(workBook!=null){
				workBook.close();
			}
		}
	}

}