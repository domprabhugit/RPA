package com.rpa.camel.processors.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class HashMapToExcelLoader {
	
	public boolean convertLoadHashMapValueInExcel(String fileName,HashMap<String, Object[]> excel_data,String sheetName) throws IOException{
		 FileOutputStream output_file = null;	
		 HSSFWorkbook new_workbook = null;
		try{
		/* Create 2007 format Workbook and Worksheet objects */
        new_workbook = new HSSFWorkbook(); //create a blank workbook object
        HSSFSheet  sheet = new_workbook.createSheet(sheetName);  //create a worksheet with caption score_details
        
		/* Load data into logical worksheet */           
	    Set<String> keyset = excel_data.keySet();
	    int rownum = 0;
	    for (String key : keyset) { //loop through the data and add them to the cell
	            Row row = sheet.createRow(rownum++);
	            Object [] objArr = excel_data.get(key);
	            int cellnum = 0;
	            for (Object obj : objArr) {
	                    Cell cell = row.createCell(cellnum++);
	                    if(obj instanceof Double)
	                            cell.setCellValue((Double)obj);
	                    else
	                            cell.setCellValue((String)obj);
	                    }
	    }

	     output_file = new FileOutputStream(new File(fileName)); //create XLS file
	    new_workbook.write(output_file);//write excel document to output stream
	    
	    return true;
		}catch(Exception e){
			throw e;
			
		}finally{
			if(output_file!=null)
				output_file.close(); //close the file
			if(new_workbook!=null)
				new_workbook.close();
		}
	    
	}

	
}
