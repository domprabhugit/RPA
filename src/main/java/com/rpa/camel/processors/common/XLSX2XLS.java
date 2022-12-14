package com.rpa.camel.processors.common;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLSX2XLS{
    private String outFn;
    private File inpFn;

    public XLSX2XLS(File inpFn){
        this.outFn = inpFn + ".xls";
        this.inpFn = inpFn;
    }

    public void xlsx2xls_progress() throws InvalidFormatException,IOException {
        InputStream in = new FileInputStream(inpFn);
        try {
            XSSFWorkbook wbIn = new XSSFWorkbook(in);
            File outF = new File(outFn);
            if (outF.exists()) {
                outF.delete();
            }

            Workbook wbOut = new HSSFWorkbook();
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
                        case Cell.CELL_TYPE_BLANK: break;

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
                        	 if(DateUtil.isCellDateFormatted(cellIn)){
    							 SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    							 cellOut.setCellValue(sdf.format(cellIn.getDateCellValue()));
    						}else{
                            cellOut.setCellValue(cellIn.getNumericCellValue());
    						}
                            break;
                        case Cell.CELL_TYPE_STRING:
                            cellOut.setCellValue(cellIn.getStringCellValue());
                            break;
                        }

                        {
                            CellStyle styleIn = cellIn.getCellStyle();
                            CellStyle styleOut = cellOut.getCellStyle();
                            styleOut.setDataFormat(styleIn.getDataFormat());
                        }cellOut.setCellComment(cellIn.getCellComment());

                        }
                }
            }
            OutputStream out = new BufferedOutputStream(new FileOutputStream(outF));
            try {
                wbOut.write(out);
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
    
    public static void main(String args[] ) throws InvalidFormatException, IOException{
    	
    	XLSX2XLS xlsx2xls = new XLSX2XLS(new File("D:\\64vb\\ROYALHONDA.xlsx"));
    	
    	xlsx2xls.xlsx2xls_progress();
    	
    }
}