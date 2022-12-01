package com.rpa.camel.processors.batchprocess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.GridAutomationModel;
import com.rpa.repository.TransactionInfoRepository;
import com.rpa.service.processors.GridAutomationService;
import com.rpa.util.UtilityFile;

public class GridMasterFileValidatorProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(GridMasterFileValidatorProcessor.class.getName());

	@Autowired
	ApplicationContext applicationContext;
	@Autowired
	TransactionInfoRepository transactionInfoRepository;

	@Autowired
	GridAutomationService gridAutomationService;

	private String rolePlayerTableName = UtilityFile.getGridUploadProperty("Grid.RolePlayer.Table.Name"),
			partyGroupTableName = UtilityFile.getGridUploadProperty("Grid.PartyGroup.Table.Name");

	private List<String> errorList = new ArrayList<String>(),errorSheetNameList = new ArrayList<String>();

	@SuppressWarnings("unused")
	private void AutoWiringBeanPropertiesSetMethod() {

		logger.info("AutoWiringBeanPropertiesSetMethod is Called in GridMasterFileValidatorProcessor Class");
		applicationContext = SpringContext.getAppContext();
		transactionInfoRepository = applicationContext.getBean(TransactionInfoRepository.class);
		gridAutomationService = applicationContext.getBean(GridAutomationService.class);
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in GridMasterFileValidatorProcessor Class");

	}

	private Boolean validationStatus = true, modalStatus = true, agentStatus = true, gridStatus = true;
	private int validationCount = 0, validationSuccesCount = 0;

	@Override
	public void process(Exchange exchange) throws Exception {

		GridMasterFileValidatorProcessor fileValidatorProcessor = new GridMasterFileValidatorProcessor();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty("TRANSACTION_INFO_REQ");
		fileValidatorProcessor.doProcess(transactionInfo, exchange);

	}

	private void doProcess(TransactionInfo transactionInfo, Exchange exchange) throws Exception {
		AutoWiringBeanPropertiesSetMethod();
		logger.info("BEGIN - GridMasterFileValidatorProcessor---doProcess() Method Called Here");
		transactionInfo.setProcessPhase(RPAConstants.GRID_VALIDATOR_PHASE);
		//transactionInfo.setProcessStatus(RPAConstants.VALIDATION);
		transactionInfo.setTransactionStatus(RPAConstants.InProgress);
		AutoWiringBeanPropertiesSetMethod();
		List<GridAutomationModel> automationFileList = gridAutomationService
				.FindValidateFilesList(RPAConstants.Completed);
		if (!automationFileList.isEmpty()) {
			GridProcessFileValidatorMethod(transactionInfo, automationFileList,exchange);
			automationFileList.clear();
		}
		automationFileList = getFilesFromBaseLocation(transactionInfo);
		GridProcessFileValidatorMethod(transactionInfo, automationFileList,exchange);

		transactionInfo.setTotalRecords(String.valueOf(validationCount));
		transactionInfo.setTotalSuccessRecords(String.valueOf(validationSuccesCount));
		transactionInfo.setTotalErrorRecords(String.valueOf(validationCount - validationSuccesCount));
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		logger.info("BEGIN - GridMasterFileValidatorProcessor---doProcess() Method Ended Here");

	}

	private void GridProcessFileValidatorMethod(TransactionInfo transactionInfo, List<GridAutomationModel> automationFileList, Exchange exchange)
			throws Exception {
		validationStatus = true;

		if (!automationFileList.isEmpty()) {

			for (GridAutomationModel grid : automationFileList) {
				if (grid.getIsValidated() == RPAConstants.N) {
					exchange.setProperty("grid_obj", grid);
					validationCount++;
					errorList.clear();
					errorSheetNameList.clear();
					Workbook workbook = UtilityFile.workBookCreation(grid.getFilePath());
					
			try{		
					int sheetCount = workbook.getNumberOfSheets();
					grid.setTotalSheetCount(String.valueOf(sheetCount));
					String SheetName = workbook.getSheetName(sheetCount - 1);
					//grid.setTotalSheetCount(String.valueOf(sheetCount));
					grid.setSuccessNo(String.valueOf(0));
					
					if(sheetCount>2){
						if (workbook.getSheetName(2).toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Agent.Sheet.Name"))) {
							//Gridwiseagent
							grid.setFileType("GridWiseAgent");
							int i = 0;
							for (i = 0; i < sheetCount - 1; i = i + 3) {
								grid.setValidateNo(String.valueOf(i + 1));
								grid.setFileStatus("GridProcess");
								excelGridWiseAgentModelValidation(workbook, i, grid);
								if (!(modalStatus == agentStatus == gridStatus == true)) {
									grid.setIsValidated(RPAConstants.F);
									validationStatus = false;

									transactionInfo.setProcessStatus(RPAConstants.Failed);
									transactionInfo.setTransactionStatus(RPAConstants.Failed);
								}
								gridAutomationService.save(grid);
							}
							
						}else if(workbook.getSheetName(sheetCount - 1).toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Agent.Sheet.Name"))){
							//FileWiseCommonAgent
							
							//grid.setFileType("ModelAndAgent");
							grid.setFileType("FileWiseAgent");
							grid.setFileStatus("AgentSheetValidation");
							agentStatus = agentCodeSheetValidation(workbook, workbook.getSheetName(sheetCount - 1));
							if (!agentStatus) {
								grid.setIsValidated(RPAConstants.F);
								validationStatus = false;
								errorList.add(SheetName);
								transactionInfo.setProcessStatus(RPAConstants.Failed);
								transactionInfo.setTransactionStatus(RPAConstants.Failed);
							}
							gridAutomationService.save(grid);
							
							int i = 0;
							for (i = 0; i < sheetCount - 1; i = i + 2) {
								grid.setValidateNo(String.valueOf(i + 1));
								grid.setFileStatus("GridProcess");
								excelValidationProcessWithoutAgentCodes(workbook, i, grid);
								if (!(modalStatus == agentStatus == gridStatus == true)) {
									grid.setIsValidated(RPAConstants.F);
									validationStatus = false;

									transactionInfo.setProcessStatus(RPAConstants.Failed);
									transactionInfo.setTransactionStatus(RPAConstants.Failed);
								}
								gridAutomationService.save(grid);
							}
							
							
						}else if(!workbook.getSheetName(sheetCount - 1).toUpperCase().contains("GRID") && !workbook.getSheetName(sheetCount - 1).toUpperCase().contains("MODEL")){
							errorSheetNameList.add("Last Sheet Name - "+workbook.getSheetName(sheetCount - 1)+" is invalid  (Expected name is Agent or Model or Grid) ");
							validationStatus = false;
							transactionInfo.setProcessStatus(RPAConstants.Failed);
							transactionInfo.setTransactionStatus(RPAConstants.Failed);
						}else{
							grid.setFileType("GridAndModel");
							int i = 0;
							for (i = 0; i < sheetCount - 1; i = i + 2) {
								grid.setValidateNo(String.valueOf(i + 1));
								grid.setFileStatus("GridProcess");
								excelValidationProcessWithoutAgentCodes(workbook, i, grid);
								if (!(modalStatus == agentStatus == gridStatus == true)) {
									grid.setIsValidated(RPAConstants.F);
									validationStatus = false;

									transactionInfo.setProcessStatus(RPAConstants.Failed);
									transactionInfo.setTransactionStatus(RPAConstants.Failed);
								}
								gridAutomationService.save(grid);
							}
						}
					}else{
						grid.setFileType("GridAndModel");
						int i = 0;
						for (i = 0; i < sheetCount - 1; i = i + 2) {
							grid.setValidateNo(String.valueOf(i + 1));
							grid.setFileStatus("GridProcess");
							excelValidationProcessWithoutAgentCodes(workbook, i, grid);
							if (!(modalStatus == agentStatus == gridStatus == true)) {
								grid.setIsValidated(RPAConstants.F);
								validationStatus = false;

								transactionInfo.setProcessStatus(RPAConstants.Failed);
								transactionInfo.setTransactionStatus(RPAConstants.Failed);
							}
							gridAutomationService.save(grid);
						}
					}
					
					
					/*if (SheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Agent.Sheet.Name"))) {
						
					} else {
						
					}*/
					
					if(!errorSheetNameList.isEmpty())
					{
						grid.setIsValidated(RPAConstants.F);
						grid.setValidationSheetList(String.join(RPAConstants.COMMA, errorSheetNameList));
						validationStatus = false;
						gridAutomationService.save(grid);
					}
					grid.setFileStatus("FileValidated");
					if (validationStatus) {
						grid.setIsValidated(RPAConstants.Y);
						validationSuccesCount++;
						String newFilePath = UtilityFile.FileMovementMethod(
								UtilityFile.getCodeBasePath()
										+ UtilityFile.getGridUploadProperty("Grid.File.Location.BasePath"),
								new File(grid.getFilePath()),
								UtilityFile.getGridUploadProperty("Grid.File.Processed.Path")
										+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY)
												.toString()
										+ UtilityFile.getGridUploadProperty("Grid.File.Location.ValidatedPath"));

						grid.setFilePath(newFilePath);
						gridAutomationService.save(grid);
						transactionInfo.setProcessStatus(RPAConstants.Success);
					} else {
						grid.setErrorSheetList(String.join(RPAConstants.COMMA, errorList));
						gridAutomationService.save(grid);
					}
					
			}finally{	
					if(workbook!=null){
						workbook.close();
					}
			}
				}

				
			}
		}
	}

	private Boolean agentCodeSheetValidation(Workbook workbook, String sheetName) {

		agentStatus = true;
		Sheet agentSheet = workbook.getSheet(sheetName);
		for (Row row : agentSheet) {
		
			if (row.getRowNum() == 0) {
				row.createCell(1).setCellValue(RPAConstants.Remarks);
				String agentCode = row.getCell(0).getStringCellValue();
				if (agentCode != null && (agentCode.contains("AGENT") || agentCode.contains("Agent") || agentCode.contains("ggent") ) ) {
					
				}else{
					agentStatus = false;
					logger.info("agentCodeSheetValidation :: "+sheetName+"-"+" Column name should be AGENT " );
					 errorList.add(sheetName+"-"+" Column name should be AGENT " );
				}
			} else {
				String agentCode = row.getCell(0).getStringCellValue();
				if (agentCode != null && !agentCode.equals("")) {
				}

				else {
					row.createCell(1).setCellValue("not a Valid AgentCode");
					agentStatus = false;
				}
			}

			
		}
	
		return agentStatus;
	}

	@SuppressWarnings("deprecation")
	private Workbook getGridDataWorkBookValidation(String gridSheetName, Workbook workbook, List<String> columnNames,
			List<Map<String, String>> validatorList, List<Map<String, String>> modalCodeMap) throws Exception {
		DataFormatter df = new DataFormatter();
		gridStatus = true;
		int count = 0;
		String remarksValue = "", Product = "";
		if (gridSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))) {
		
			logger.info("EXCEl GRID DETAIlS Validating Method is Started Here");
			Sheet gridSheet = workbook.getSheet(gridSheetName);
			// GetColumn Names
			Row columnRow = gridSheet.getRow(0);
			if(columnRow==null){
				logger.info("getGridDataWorkBookValidation :: "+gridSheetName+"-"+" Headers Not available in First Row " );
				gridStatus = false;
				errorList.add(gridSheetName+"-"+" Headers Not available in First Row " );
				return workbook;
			}else{
			for (Cell cell : columnRow) {
				if (cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK
						|| (cell.getCellType() == Cell.CELL_TYPE_STRING && !cell.getStringCellValue().isEmpty()))) {
					columnNames.add(Arrays.asList(new String[] {"PRODUCT","Y_AXIS","VALUE","GRID_ID","GRID_NAME","EFFECTIVE_START_DATE","EFFECTIVE_END_DATE","VERSION","TRANSACTIONTYPE","START_AGE","END_AGE","STATE","REMARKS" })
							.contains(cell.toString().toUpperCase().trim())==true?cell.toString().toUpperCase().trim(): "wrongcolumName");
				}

			}	
			
			List<String> mandatoryHeaders = Arrays.asList(new String[] {"PRODUCT","Y_AXIS","VALUE","GRID_ID","GRID_NAME","EFFECTIVE_START_DATE","EFFECTIVE_END_DATE","VERSION","TRANSACTIONTYPE","START_AGE","END_AGE"});
			for (String header : mandatoryHeaders) {
				   if (!columnNames.contains(header)) {
					   gridStatus = false;
					   errorList.add(gridSheetName+"-"+" Mandatory Headers Not available");
					   return workbook;
				   }
				}
				if(!columnNames.contains("wrongcolumName") &&  columnNames.size()>=11)
				{	
				
				Map<String, String> map = modalCodeMap.get(0);
				String[] columnName = UtilityFile.getGridUploadProperty("Grid.Model.Column.Name").split(",");
				
				Product = validatorList.get(0)
						.get(map.get(columnName[0]) == null ? map.get(columnName[1]) : map.get(columnName[0]));
				String Grid_Name = "";
				
				
				
				int remarksRowNo = columnRow.getPhysicalNumberOfCells();
				CellStyle cellstyle = columnRow.getCell(columnRow.getFirstCellNum()).getCellStyle();
				
				if (!columnNames.contains(RPAConstants.Remarks)) {
					creatCell(columnRow, remarksRowNo, cellstyle, RPAConstants.Remarks);
				} else {
					remarksRowNo = remarksRowNo - 1;
				}
	
				for (Row row : gridSheet) {
					if (row.getRowNum() != 0 && row != null && !UtilityFile.isRowEmpty(row)) {
						for (int i = 0; i < remarksRowNo; i++) {
							Cell cell = row.getCell(i);
							remarksValue = "";
	
							
							if (cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK
									|| (cell.getCellType() == Cell.CELL_TYPE_STRING
											&& !cell.getStringCellValue().isEmpty()))) {
								if(gridSheet.getSheetName().equals("Grid Q")){
									System.out.println("grid heet q");
								}
								if (columnNames.get(i).equals("PRODUCT")) {
									if (Product != null && Product != "") {
									cell.setCellValue(Product);
									} else {
										remarksValue = " Invalid Value ";
									}
								}
								if (columnNames.get(i).equals("Y_AXIS")) {
									if (row.getRowNum() == 1) {
										Grid_Name = getGridName(cell.getStringCellValue());
									}
									
									if(cell.getStringCellValue().indexOf("_")!=-1){
									String[] yaxis = cell.getStringCellValue().split("_");
									cell.setCellValue(yaxis[0].trim() + "_" + yaxis[1].trim());
									}else{
										cell.setCellValue(cell.getStringCellValue());
									}
								}
								if (columnNames.get(i).equals("GRID_NAME"))
									if (Grid_Name.isEmpty()) {
										remarksValue = " Not Available in the DATABASE ";
									} else {
										cell.setCellValue(Grid_Name);
									}
								if (columnNames.get(i).equalsIgnoreCase("VALUE")
										|| columnNames.get(i).equalsIgnoreCase("START_AGE")
										|| columnNames.get(i).equalsIgnoreCase("END_AGE")
										|| columnNames.get(i).equalsIgnoreCase("GRID_ID")
										|| columnNames.get(i).equalsIgnoreCase("VERSION"))
	
								{
									remarksValue = UtilityFile.isNumeric(cell.toString()) == false ? "invalid numeric value"
											: "";
	
								}
								if (columnNames.get(i).equalsIgnoreCase("EFFECTIVE_START_DATE")
										|| columnNames.get(i).equalsIgnoreCase("EFFECTIVE_END_DATE")) {
									logger.info("invalid::::" + df.formatCellValue(cell));
									if (df.formatCellValue(cell).length() >= 10) {
										remarksValue = checkGridDateFormat(df.formatCellValue(cell),
												RPAConstants.GRID_DATE_FORMAT) == false ? "invalid date Format" : "";
										/*remarksValue = checkGridMonth(df.formatCellValue(cell),
												RPAConstants.GRID_DATE_FORMAT,
												UtilityFile.getGridUploadProperty(RPAConstants.MONTH_RANGE)) == true
														? "month  not with in range" : "";*/
									}
	
								}
	
								if (columnNames.get(i).equalsIgnoreCase("TRANSACTIONTYPE")) {
									remarksValue = Arrays.asList(new String[] { "NB", "RN", "RNE" })
											.contains(cell.toString()) == false ? "invalid TransactionType" : "";
								}
							} else if (cell != null && (cell.getCellType() == Cell.CELL_TYPE_BLANK
									|| (cell.getCellType() == Cell.CELL_TYPE_STRING
											&& cell.getStringCellValue().isEmpty()))) {
								
								if (columnNames.get(i).equals("GRID_NAME"))
									if (Grid_Name.isEmpty()) {
										remarksValue = " Not Available in the DATABASE ";
									} else {
										cell.setCellValue(Grid_Name);
									}
								if (!(columnNames.get(i).equals("EFFECTIVE_END_DATE") || columnNames.get(i).equals("STATE")
										|| columnNames.get(i).equals("GRID_NAME") || columnNames.get(i).equals("PRODUCT")))
	
									remarksValue = RPAConstants.Empty;
								
							}
						
							if (!remarksValue.equalsIgnoreCase("")) {
								gridStatus = false;
								logger.info("Row No:::::  " + (Integer.valueOf(row.getRowNum()) + 1) + "::ColumnName===="
										+ columnNames.get(i) + "has   " + remarksValue + "");
	
								creatCell(row, remarksRowNo, row.getCell(row.getFirstCellNum()).getCellStyle(),
										columnNames.get(i) + " has  " + remarksValue + "  ");
							}
	
						}
					}
					count++;
					logger.info("Grid Details Count::" + count);
				}
	
			}
		
			
			else
				{
				gridStatus = false;
				   errorList.add(gridSheetName+"-"+" ColumnNames Not in the Template Format  " );
				   return workbook;
				}
		}
		}
		if (!gridStatus)
			errorList.add(gridSheetName);
		if (count == 0)
			errorSheetNameList.add(gridSheetName);
		
		
		return workbook;
	}

	@SuppressWarnings("deprecation")
	private List<Map<String, String>> getModalDataWorkBookValidation(String modalSheetName, Workbook workbook,
			List<String> columnNames, List<Map<String, String>> finalmapList) {
		DataFormatter df = new DataFormatter();
		modalStatus = true;
		if (modalSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Modal.Sheet.Name")))

		{
			logger.info("EXCEl Model DETAILS Validating Method is Started Here");
			Sheet gridSheet = workbook.getSheet(modalSheetName);
			Row columnRow = gridSheet.getRow(0);
			int v = 0;
			if(columnRow!=null){
			for (Cell cell : columnRow) {
				if (cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK
						|| (cell.getCellType() == Cell.CELL_TYPE_STRING && !cell.getStringCellValue().isEmpty()))) {
					if (v == 3) {
						if(cell.toString().trim().toUpperCase().contains("SUB") || cell.toString().trim().toUpperCase().contains("PRODUCT")){
							columnNames.add(RPAConstants.product.toUpperCase());
						}else{
							logger.info("getModalDataWorkBookValidation :: "+modalSheetName+"-"+" 4th column should be product/subline " );
							modalStatus = false;
							 errorList.add(modalSheetName+"-"+" 4th column should be product/subline " );
							 return finalmapList;
						}
						
					} else {
						columnNames.add(cell.toString().trim().toUpperCase());
					}
					v++;
				}

			}

			if(columnNames.size()<3){
				logger.info("getModalDataWorkBookValidation :: "+modalSheetName+"-"+" 4 Columns should be available " );
				modalStatus = false;
				 errorList.add(modalSheetName+"-"+" ColumnNames Not in the Template Format " );
				 return finalmapList;
			}else{
					
			int remarksRowNo = columnRow.getPhysicalNumberOfCells();
			CellStyle cellstyle = columnRow.getCell(columnRow.getFirstCellNum()).getCellStyle();
			String remarksValue = "";
			Map<String, String> map = new HashMap<>();
			if (!columnNames.contains(RPAConstants.Remarks)) {
				creatCell(columnRow, remarksRowNo, cellstyle, RPAConstants.Remarks);
			} else {
				remarksRowNo = remarksRowNo - 1;
			}

			for (Row row : gridSheet) {

				if (row.getRowNum() != 0 && row != null && !UtilityFile.isRowEmpty(row)) {
					//map.clear();
					map = new HashMap<>();

					for (int i = 0; i < remarksRowNo; i++) {
						Cell cell = row.getCell(i);
					
						if (cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK
								|| (cell.getCellType() == Cell.CELL_TYPE_STRING
										&& !cell.getStringCellValue().isEmpty())))

						{
							map.put(columnNames.get(i), df.formatCellValue(row.getCell(i)).trim());
						} else {
							if (cell != null) {
								logger.info("Row No:::::" + (Integer.valueOf(row.getRowNum()) + 1) + "columnName===="
										+ columnNames.get(i) + "has a" + remarksValue + "");
								if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
									remarksValue = RPAConstants.Empty;
								}
								
								creatCell(row, remarksRowNo, row.getCell(row.getFirstCellNum()).getCellStyle(),
										columnNames.get(i) + " has a   " + remarksValue + "  ");

								modalStatus = false;
							}
						}
					}
					if (!map.isEmpty())
						finalmapList.add(map);

				}
			}
		  }
		}else{
			logger.info("getModalDataWorkBookValidation :: "+modalSheetName+"-"+" Headers Not available in First Row " );
			modalStatus = false;
			 errorList.add(modalSheetName+"-"+" Headers Not available in First Row " );
			 return finalmapList;
		}
			
		}
		
		logger.info("Model Details Count::" + finalmapList.size());
		if (!modalStatus)
			errorList.add(modalSheetName);
		if (finalmapList.isEmpty())
		errorSheetNameList.add(modalSheetName);
			return finalmapList;

	}

	private Cell creatCell(Row row, int rowNo, CellStyle cellstyle, String value) {
		Cell cell = null;
		String oldValue = "";
		cell = row.getCell(rowNo);
		if (cell == null) {
			cell = row.createCell(rowNo);
			cell.setCellValue(value);
		} else {
			oldValue = cell.getStringCellValue();
			if (!oldValue.isEmpty()) {
				cell.setCellValue(oldValue + "," + value);
			}
		}
		if (cellstyle != null) {
			
			cellstyle.setFillBackgroundColor(IndexedColors.ROSE.getIndex());
			cell.setCellStyle(cellstyle);
		}
		return cell;
	}

	private List<GridAutomationModel> getFilesFromBaseLocation(TransactionInfo transactionInfo)
			throws UnsupportedEncodingException, URISyntaxException {

		logger.info("Getting Files From Base Location Path Method Called Here");
		String FilePath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getGridUploadProperty("Grid.File.Location.BasePath");
		logger.info(" Base Location Path   ::::  "+FilePath);
		File files = new File(FilePath);
		
		File[] directoryList = files.listFiles();
		int fileCount = 0;
		List<GridAutomationModel> gridList = new ArrayList<GridAutomationModel>();
		if (directoryList != null) {
			for (File file : directoryList) {
				if (!file.isDirectory()) {
					if (Arrays
							.asList(UtilityFile.getGridUploadProperty("Grid.File.Extension").split(RPAConstants.COMMA))
							.contains(FilenameUtils.getExtension(file.getName()))) {
						GridAutomationModel gridAutoMationObject = new GridAutomationModel();
						gridAutoMationObject.setStartDate(transactionInfo.getTransactionStartDate());
						gridAutoMationObject.setFileName(file.getName());
						gridAutoMationObject.setFilePath(file.getAbsolutePath());
						gridAutoMationObject.setIsValidated(RPAConstants.N);
						gridAutoMationObject.setIsProcessed(RPAConstants.N);
						gridAutoMationObject.setTransactionInfoId(transactionInfo.getId());
						gridAutomationService.save(gridAutoMationObject);
						gridList.add(gridAutoMationObject);
						fileCount++;
					}
				}
			}
			transactionInfo.setProcessStatus("FileReadingFromLocation");
			transactionInfo.setTotalRecords(String.valueOf(fileCount));
			transactionInfoRepository.save(transactionInfo);
		}
		logger.info("Getting Files From Base Location Path Method ended Here");
		return gridList;

	}

	private Boolean excelValidationProcessWithoutAgentCodes(Workbook workbook, int SheetNo, GridAutomationModel grid)
			throws Exception {
		List<Map<String, String>> modalCodeMap = new ArrayList<Map<String, String>>();
		List<Map<String, String>> validatorList = new ArrayList<Map<String, String>>();
		List<String> columnNames = new ArrayList<String>();
		
		grid.setFileStatus("Model and GridValidation");
		String firstSheetName = workbook.getSheetName(SheetNo);
		String secondSheetName = workbook.getSheetName(SheetNo + 1);
		if (firstSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Modal.Sheet.Name"))){
			logger.info("Model Validation Process For the Sheet :::::" + firstSheetName);
			modalCodeMap = getModalDataWorkBookValidation(firstSheetName, workbook, columnNames, modalCodeMap);
		}else if(secondSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Modal.Sheet.Name"))){
			logger.info("Model Validation Process For the Sheet :::::" + secondSheetName);
			modalCodeMap = getModalDataWorkBookValidation(secondSheetName, workbook, columnNames, modalCodeMap);
		}else{
			logger.error("excelValidationProcessWithoutAgentCodes - Model sheet not available in Sheet "+SheetNo+"&"+(SheetNo+1));
			errorSheetNameList.add("Model Not available in Sheet "+SheetNo+"&"+(SheetNo+1));
			modalStatus = false;
		}
		
			
		if (!modalCodeMap.isEmpty()) {
			columnNames = new ArrayList<String>();
			validatorList = getProductListFromProperties(validatorList);
			if (firstSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))){
				logger.info("Grid Validation Process For the Sheet :::::" + firstSheetName);
				workbook = getGridDataWorkBookValidation(firstSheetName, workbook, columnNames, validatorList, modalCodeMap);
			}else if(secondSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))){
				logger.info("Grid Validation Process For the Sheet :::::" + secondSheetName);
				workbook = getGridDataWorkBookValidation(secondSheetName, workbook, columnNames, validatorList, modalCodeMap);
			}else{
				logger.error("excelValidationProcessWithoutAgentCodes - Grid sheet not available in Sheet "+SheetNo+"&"+(SheetNo+1));
				errorSheetNameList.add("Grid Not available in Sheet "+SheetNo+"&"+(SheetNo+1));
				gridStatus = false;
			}
			
			FileOutputStream out = new FileOutputStream(grid.getFilePath());
			workbook.write(out);
			out.close();

		}
		
		logger.info(" Excel File Validated and Modified   ::");
		return validationStatus;

	}
	
	
	private Boolean excelGridWiseAgentModelValidation(Workbook workbook, int SheetNo, GridAutomationModel grid)
			throws Exception {
		List<Map<String, String>> modalCodeMap = new ArrayList<Map<String, String>>();
		List<Map<String, String>> validatorList = new ArrayList<Map<String, String>>();
		List<String> columnNames = new ArrayList<String>();
		String firstSheetName ="",secondSheetName = "", thridSheetName="";
		grid.setFileStatus("Model and GridValidation");
		firstSheetName = workbook.getSheetName(SheetNo);
		if(workbook.getNumberOfSheets()>SheetNo+1){
			secondSheetName = workbook.getSheetName(SheetNo + 1);
		}
		
		if(workbook.getNumberOfSheets()>SheetNo+2){
			thridSheetName = workbook.getSheetName(SheetNo + 2);
		}
		 
		if (firstSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Agent.Sheet.Name"))){
			logger.info("Agent Validation Process For the Sheet :::::" + firstSheetName);
			agentStatus = agentCodeSheetValidation(workbook, firstSheetName);
		}else if(secondSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Agent.Sheet.Name"))){
			logger.info("Agent Validation Process For the Sheet :::::" + secondSheetName);
			agentStatus = agentCodeSheetValidation(workbook, secondSheetName);
		}else if(thridSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Agent.Sheet.Name"))){
			logger.info("Agent Validation Process For the Sheet :::::" + thridSheetName);
			agentStatus = agentCodeSheetValidation(workbook, thridSheetName);
		}else{
			logger.error("excelValidationProcessWithoutAgentCodes - Agent sheet not available in Sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2));
			errorSheetNameList.add("Agent Not available in Sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2));
			agentStatus = false;
		}
		 
		if (firstSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Modal.Sheet.Name"))){
			logger.info("Model Validation Process For the Sheet :::::" + firstSheetName);
			modalCodeMap = getModalDataWorkBookValidation(firstSheetName, workbook, columnNames, modalCodeMap);
		}else if(secondSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Modal.Sheet.Name"))){
			logger.info("Model Validation Process For the Sheet :::::" + secondSheetName);
			modalCodeMap = getModalDataWorkBookValidation(secondSheetName, workbook, columnNames, modalCodeMap);
		}else if(thridSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Modal.Sheet.Name"))){
			logger.info("Model Validation Process For the Sheet :::::" + thridSheetName);
			modalCodeMap = getModalDataWorkBookValidation(thridSheetName, workbook, columnNames, modalCodeMap);
		}else{
			logger.error("excelValidationProcessWithoutAgentCodes - Model sheet not available in Sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2));
			errorSheetNameList.add("Model Not available in Sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2));
			modalStatus = false;
		}
		
			
		if (!modalCodeMap.isEmpty()) {
			columnNames = new ArrayList<String>();
			validatorList = getProductListFromProperties(validatorList);
			if (firstSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))){
				logger.info("Grid Validation Process For the Sheet :::::" + firstSheetName);
				workbook = getGridDataWorkBookValidation(firstSheetName, workbook, columnNames, validatorList, modalCodeMap);
			}else if(secondSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))){
				logger.info("Grid Validation Process For the Sheet :::::" + secondSheetName);
				workbook = getGridDataWorkBookValidation(secondSheetName, workbook, columnNames, validatorList, modalCodeMap);
			}else if(thridSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))){
				logger.info("Grid Validation Process For the Sheet :::::" + thridSheetName);
				workbook = getGridDataWorkBookValidation(thridSheetName, workbook, columnNames, validatorList, modalCodeMap);
			}else{
				logger.error("excelValidationProcessWithoutAgentCodes - Grid sheet not available in Sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2));
				errorSheetNameList.add("Grid Not available in Sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2));
				gridStatus = false;
			}
			
			validatorList = getProductListFromProperties(validatorList);
			
			FileOutputStream out = new FileOutputStream(grid.getFilePath());
			workbook.write(out);
			out.close();

		}
		
		logger.info(" Excel File Validated and Modified   ::");
		return validationStatus;

	}
	
	

	private String getGridName(String Product) throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("Validating Grid Names Method Called here");
		Connection connection = UtilityFile.getGridDbConnection();
		String grid_name = "", cnt = "0";

		if (Product.indexOf("_") != -1) {
			String[] grid_names = Product.split("_");
			String sql = "select count(*) from  " + rolePlayerTableName + " where ROLE_PLAYER_CODE='"
					+ grid_names[0].trim() + "'";
			PreparedStatement statement = connection.prepareStatement(sql);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				cnt = rs.getString(1);
			}
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if (!cnt.equals("0")) {
				/*Agent code based*/
				grid_name = RPAConstants.Agent_Region;
			} else {
				String sql1 = "select count(*) from " + partyGroupTableName + " where PARTYGROUPCODE='"
						+ grid_names[0].trim() + "'";
				PreparedStatement statement1 = connection.prepareStatement(sql1);
				ResultSet rs1 = statement1.executeQuery();
				while (rs1.next()) {
					cnt = rs1.getString(1);
				}
				if(rs1!=null)
					rs1.close();
				if(statement1!=null)
					statement1.close();
				if (!cnt.equals("0")) {
					/*party group code based*/
					grid_name = RPAConstants.Model_Agent;
				}

			}

		} else {
			/*Region based*/
			grid_name = RPAConstants.Model_Region;
		}

		connection.close();
		return grid_name;

	}

	private List<Map<String, String>> getProductListFromProperties(List<Map<String, String>> validatorList) {
		Map<String, String> productList = new HashMap<>();
		logger.info(" Getting Produc tList For Grid Process:::");
		productList.put(RPAConstants.VGC, RPAConstants.VGC_Product);
		productList.put(RPAConstants.VPC, RPAConstants.VPC_Product);
		productList.put(RPAConstants.VPCV, RPAConstants.VPCV_Product);
		productList.put(RPAConstants.VMC, RPAConstants.VMC_Product);
		productList.put(RPAConstants.VOC, RPAConstants.VOC_Product);
		validatorList.add(productList);

		return validatorList;
	}

	public static boolean checkGridDateFormat(String dateValue, String dateFormat) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			sdf.parse(dateValue);
		
			return true;
		} catch (ParseException ex) {
			logger.error("Error in CheckDate Format Method" + ex.getMessage(), ex);
			return false;
		}

	}

	public static boolean checkGridMonth(String dateValue, String dateFormat, String month) {
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			date = sdf.parse(dateValue);
			Calendar startCalendar = Calendar.getInstance();
			startCalendar.setTime(date);
			Calendar endCalendar = Calendar.getInstance();
			endCalendar.setTime(new Date());
			startCalendar.add(Calendar.MONTH, Integer.valueOf(month) - 1);
			return date.after(new Date()) ? false : startCalendar.before(endCalendar);

		} catch (ParseException ex) {
			logger.error("Error in Month" + ex.getMessage(), ex);
			return false;
		}

	}

}
