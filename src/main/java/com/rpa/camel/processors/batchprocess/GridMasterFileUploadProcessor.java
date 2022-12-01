package com.rpa.camel.processors.batchprocess;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
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
import com.rpa.model.processors.GridFilesCountDetails;
import com.rpa.repository.TransactionInfoRepository;
import com.rpa.service.processors.GridAutomationService;
import com.rpa.util.UtilityFile;

public class GridMasterFileUploadProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(GridMasterFileUploadProcessor.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	TransactionInfoRepository transactionInfoRepository;

	@Autowired
	GridAutomationService gridAutomationService;

	private String AgentCodeTableName = UtilityFile.getGridUploadProperty("Grid.Agent.Table.Name"),
			ModelCodeTableName = UtilityFile.getGridUploadProperty("Grid.Model.Table.Name"),
			ModelAgentMapTableName = UtilityFile.getGridUploadProperty("Grid.ModelAgent.Table.Name"),
			baseGridTableName = UtilityFile.getGridUploadProperty("Grid.BaseGrid.Table.Name"),
			gridTempTableName = UtilityFile.getGridUploadProperty("Grid.TempGrid.Table.Name"),
			gridUploadMasterTableName = UtilityFile.getGridUploadProperty("Grid.MainGrid.Table.Name");

	private int uploadCount = 0, uploadSuccesCount = 0,totalRecordsCount=0;
	
	private Map<String,String > sheetCountDetails=new LinkedHashMap<>();

	private void AutoWiringBeanPropertiesSetMethod() {

		logger.info("AutoWiringBeanPropertiesSetMethod is Called in GridMasterUploadProcessor Class");
		applicationContext = SpringContext.getAppContext();
		transactionInfoRepository = applicationContext.getBean(TransactionInfoRepository.class);

		gridAutomationService = applicationContext.getBean(GridAutomationService.class);
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in GridMasterUploadProcessor Class");

	}

	@Override
	public void process(Exchange exchange) throws Exception {

		GridMasterFileUploadProcessor grid = new GridMasterFileUploadProcessor();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty("TRANSACTION_INFO_REQ");
		grid.doProcess(transactionInfo, exchange);

	}

	public void doProcess(TransactionInfo transactionInfo, Exchange exchange)
			throws EncryptedDocumentException, InvalidFormatException, URISyntaxException, IOException,
			InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, ParseException {
		// To get AutowiredBeans
		AutoWiringBeanPropertiesSetMethod();
		// To clear all the tempTable before starting the process
		logger.info("BEGIN - GridMasterFileUploadProcessor---doProcess() Method Called Here");
		List<GridAutomationModel> gridErrorFileList = gridAutomationService
				.FindErrorFileList(transactionInfo.getTransactionStartDate(), RPAConstants.Completed);
		if (!gridErrorFileList.isEmpty()) {
			logger.info("Files count For Error::::" + gridErrorFileList.size());
			for (GridAutomationModel grid : gridErrorFileList) {

				String newFilePath = UtilityFile.FileMovementMethod(
						UtilityFile.getCodeBasePath()
								+ UtilityFile.getGridUploadProperty("Grid.File.Location.BasePath"),
						new File(grid.getFilePath()),
						UtilityFile.getGridUploadProperty("Grid.File.Processed.Path")
								+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY).toString()
								+ UtilityFile.getGridUploadProperty("Grid.File.Location.ErrorPath"));
				grid.setEndDate(new Date());
				grid.setFilePath(newFilePath);
				grid.setFileStatus("ValidationFailed");
				gridAutomationService.save(grid);

			}

			

		}
		List<GridAutomationModel> gridUploadFileList = gridAutomationService
				.FindUploadFilesList(transactionInfo.getTransactionStartDate(), RPAConstants.Completed);

		if (gridUploadFileList.isEmpty()) {
			// fileReaderMethodForRegular(transactionInfo);
			logger.info("No Files To Upload");
			if(!transactionInfo.getProcessStatus().equalsIgnoreCase(RPAConstants.Failed)){
				transactionInfo.setProcessStatus("No File");
				transactionInfo.setTransactionStatus("No File");
			}
				
				
				

			// transactionInfoRepository.save(transactionInfo);
		} else {
			logger.info("tableDeleteMethod called Here To Clear Temp Table");
			tableDeleteMethod(UtilityFile.getGridDbConnection(),
					getListOfTablesToTruncate(RPAConstants.GRID_Truncate_All_Table));
			logger.info("Files count For upload::::" + gridUploadFileList.size());
			uploadCount = gridUploadFileList.size();
			transactionInfo.setProcessPhase(RPAConstants.GRID_UPLOADER_PHASE);
			for (GridAutomationModel grid : gridUploadFileList) {
				grid.setGridInsertId(getNextGridInsertId(RPAConstants.GRID_INSERT_PREFIX));
				gridAutomationService.save(grid);
				transactionInfo.setTransactionStatus(RPAConstants.InProgress);
				FileUploaderProcessMethod(transactionInfo, grid);
			}
			transactionInfo.setProcessStatus(RPAConstants.Success);
			transactionInfo.setTotalUploadRecords(String.valueOf(uploadCount));
			transactionInfo.setTotalSuccessUploads(String.valueOf(uploadSuccesCount));
			transactionInfo.setTotalErrorUploads(String.valueOf(uploadCount - uploadSuccesCount));

			

		}
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
	}
	

	private void FileUploaderProcessMethod(TransactionInfo transactionInfo, GridAutomationModel gridAutoMationObject)
			throws URISyntaxException, EncryptedDocumentException, InvalidFormatException, IOException,
			InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, ParseException {
		logger.info(" FileUploaderProcessMethod  called Here ");
		gridAutoMationObject.setIsProcessed("S");
		gridAutomationService.save(gridAutoMationObject);
		
		List<String> agentCodes = new ArrayList<String>();
		sheetCountDetails.clear();
		totalRecordsCount=0;
		Connection connection = UtilityFile.getGridDbConnection();
		Workbook workbook = null;
		
		String FilePath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getGridUploadProperty("Grid.File.Location.BasePath");
		
		gridAutoMationObject.setStartDate(transactionInfo.getTransactionStartDate());

		File file = new File(gridAutoMationObject.getFilePath());
		gridAutoMationObject.setFileName(file.getName());
		
		workbook = UtilityFile.workBookCreation(file.getAbsolutePath());
		
	try{
		transactionInfo.setInputFilePath_1(file.getAbsolutePath());
		
		gridAutomationService.save(gridAutoMationObject);
		
		
		
		if(gridAutoMationObject.getFileType().equals("GridWiseAgent")){
				int i = 0,p=0;
				for (i = 0; i < Integer
						.valueOf(gridAutoMationObject.getTotalSheetCount()) - 1; i = i + 3) {
					p++;
					gridAutoMationObject.setSuccessNo(String.valueOf(i + 1));
					gridAutoMationObject.setFileStatus("GridUploadProcess");
					processgridWiseAgentCodes(workbook, i, connection, agentCodes,gridAutoMationObject.getGridInsertId(),gridAutoMationObject.getId(),p);
					gridAutomationService.save(gridAutoMationObject);
				}
				if (i == Integer.valueOf(gridAutoMationObject.getTotalSheetCount())) {
					gridAutoMationObject.setFileStatus(RPAConstants.Success);
					gridAutoMationObject.setSuccessNo(gridAutoMationObject.getTotalSheetCount());
				}
				logger.info("To delete AgentCode  After the Excel File Complete");
				tableDeleteMethod(connection, new ArrayList<String>(Arrays.asList(UtilityFile.getGridUploadProperty("Grid.truncate.Table.Agent"))));
		}else if(gridAutoMationObject.getFileType().equals("FileWiseAgent")){
			String SheetName = workbook.getSheetName(Integer.valueOf(gridAutoMationObject.getTotalSheetCount()) - 1);
			agentCodes = getAgentCodesFromWorkBook(SheetName, workbook, agentCodes);
				int i = 0,p=0;
				for (i = 0; i < Integer
						.valueOf(gridAutoMationObject.getTotalSheetCount()) - 1; i = i + 2) {
					p++;
					gridAutoMationObject.setSuccessNo(String.valueOf(i + 1));
					gridAutoMationObject.setFileStatus("GridUploadProcess");
					processFileWiseAgentCodes(workbook, i, connection, agentCodes,gridAutoMationObject.getGridInsertId(),gridAutoMationObject.getId(),p);
					gridAutomationService.save(gridAutoMationObject);
				}
				if (i == Integer.valueOf(gridAutoMationObject.getTotalSheetCount()) - 1) {
					gridAutoMationObject.setFileStatus(RPAConstants.Success);
					gridAutoMationObject.setSuccessNo(gridAutoMationObject.getTotalSheetCount());
				}
				logger.info("To delete AgentCode  After the Excel File Complete");
				tableDeleteMethod(connection, new ArrayList<String>(Arrays.asList(UtilityFile.getGridUploadProperty("Grid.truncate.Table.Agent"))));
		}else if(gridAutoMationObject.getFileType().equals("GridAndModel")){
			int i = 0,p=0;
			for (i = 0; i < Integer.valueOf(gridAutoMationObject.getTotalSheetCount())
							- 1; i = i + 2) {
				p++;
				gridAutoMationObject.setSuccessNo(String.valueOf(i + 1));
				gridAutoMationObject.setFileStatus("GridUploadProcess");
				gridProcessWithoutAgentCodes(workbook, i, connection,gridAutoMationObject.getGridInsertId(),gridAutoMationObject.getId(),p);
				gridAutomationService.save(gridAutoMationObject);
			}
			if (i == Integer.valueOf(gridAutoMationObject.getTotalSheetCount())) {
				gridAutoMationObject.setFileStatus(RPAConstants.Success);
				gridAutoMationObject.setSuccessNo(gridAutoMationObject.getTotalSheetCount());
			}
		}
		

		if (gridAutoMationObject.getFileStatus().equalsIgnoreCase(RPAConstants.Success)) {
			String newFileName = UtilityFile.FileMovementMethod(FilePath, new File(gridAutoMationObject.getFilePath()),
					UtilityFile.getGridUploadProperty("Grid.File.Processed.Path")
							+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY).toString()
							+ UtilityFile.getGridUploadProperty("Grid.File.Location.UploadedPath"));
			gridAutoMationObject.setEndDate(new Date());
			gridAutoMationObject.setFilePath(newFileName);
			gridAutoMationObject.setIsProcessed(RPAConstants.Y);
			
			gridAutoMationObject.setTotalRecordsCount(String.valueOf(totalRecordsCount));
			gridAutomationService.save(gridAutoMationObject);
			List<GridFilesCountDetails> fileDetails=GridFileCountDetailsMethod(gridAutomationService.findByGridId(gridAutoMationObject.getTransactionInfoId(),gridAutoMationObject.getId()).get(0),sheetCountDetails);
	       gridAutomationService.save(fileDetails);
			
			uploadSuccesCount++;
			logger.info("File SuccessFully  Completed");
		}
		logger.info(" FileUploaderProcessMethod  Ended Here ");
		
		if(connection!=null)
			connection.close();
	}finally{
		if(workbook!=null){
			workbook.close();
		}
	}
}

	private List<GridFilesCountDetails> GridFileCountDetailsMethod(GridAutomationModel gridAutoMationObject, Map<String, String> sheetCountDetails) {
		
		List<GridFilesCountDetails> fileDetails=new ArrayList<GridFilesCountDetails>();
		// Map<String, String> treeMap = new TreeMap<String, String>(sheetCountDetails);
		if(!sheetCountDetails.isEmpty())
		{
			for (Map.Entry<String, String> sheetDetails : sheetCountDetails.entrySet())
			{
			GridFilesCountDetails filesCount=new GridFilesCountDetails();
			
			filesCount.setFileName(gridAutoMationObject.getFileName());
			filesCount.setGridId(gridAutoMationObject.getId());
			filesCount.setTransactionInfoId(gridAutoMationObject.getTransactionInfoId());
			filesCount.setSheetsName(sheetDetails.getKey());
			filesCount.setSheetsCount(sheetDetails.getValue());
			fileDetails.add(filesCount);
			}
		
		}
		
		return fileDetails;
	}

	private void gridProcessWithoutAgentCodes(Workbook workbook, int SheetNo, Connection connection, String gridInsertId, long gridId, int sheetPairNo)
			throws SQLException, ParseException {

		List<String> modalCodes = new ArrayList<String>();
		List<String> columnNames = new ArrayList<String>();
		List<Map<String, String>> FinalmapList = new ArrayList<Map<String, String>>();
		
		String firstSheetName ="",secondSheetName = "",gridSheetName="",modalSheetName="";
		
		firstSheetName = workbook.getSheetName(SheetNo);
		
		if(workbook.getNumberOfSheets()>SheetNo+1){
			secondSheetName = workbook.getSheetName(SheetNo + 1);
		}
		
		if (firstSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))){
			logger.info("Fetching Grids from the sheet :::::" + firstSheetName);
			FinalmapList = getBasicGridDetailsFromWorkBook(firstSheetName, workbook, columnNames, FinalmapList,null,1);
			gridSheetName = firstSheetName;
		}else if(secondSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))){
			logger.info("Fetching Grids from the sheet :::::" + secondSheetName);
			FinalmapList = getBasicGridDetailsFromWorkBook(secondSheetName, workbook, columnNames, FinalmapList,null,1);
			gridSheetName = secondSheetName;
		}else{
			logger.error("gridProcessWithAgentCodes - Grid sheet not available in Sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2));
		}
		
		
		if (firstSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Modal.Sheet.Name"))){
			logger.info("Fetching Model from the Sheet :::::" + firstSheetName);
			modalCodes = getModelCodesFromWorkBook(firstSheetName, workbook, modalCodes);
			modalSheetName = firstSheetName;
		}else if(secondSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Modal.Sheet.Name"))){
			logger.info("Fetching Model from the Sheet :::::" + secondSheetName);
			modalCodes = getModelCodesFromWorkBook(secondSheetName, workbook, modalCodes);
			modalSheetName = secondSheetName;
		}else{
			logger.error("gridProcessWithoutAgentCodes - Model sheet not available in Sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2));
		}
		
		insertGridWithRowStatus(connection,gridId,String.valueOf(sheetPairNo));
		
		if (!modalCodes.isEmpty()) {
			modalCodes = UtilityFile.removeDuplicates(modalCodes);
			modalCodeInsertionMethod(connection, modalCodes);
			
			Boolean status = insertBasicGridDetails(connection, FinalmapList);
			if (status) {
				createTempGridValuesWithoutAgentCodes(connection,gridInsertId);
			}
			try{
				gridUploadMasterMethod(connection,gridInsertId,gridId,sheetPairNo);
			}catch(Exception e){
				logger.error("gridProcessWithoutAgentCodes - Error while inserting records in main table from the sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2)+"::::::"+e);
				
				logger.info("gridProcessWithoutAgentCodes - Reverting records inserted in main table");
				
				/*String Sql = " Delete from " + gridUploadMasterTableName + " where  REMARKS='"+gridInsertId+"' ";
				logger.info("gridProcessWithoutAgentCodes - deleting inserted records with grid inserted id "+gridInsertId+" after exception - query :: "+Sql);
				PreparedStatement updateStatement = connection.prepareStatement(Sql);
				
				updateStatement.execute();
				updateStatement.close();
				
				 Sql = " update " + gridUploadMasterTableName + " set EFFECTIVE_END_DATE = null where  REMARKS='"+gridInsertId+"' ";
				logger.info("gridProcessWithoutAgentCodes - reverting updated  EFFECTIVE_END_DATE with grid inserted id "+gridInsertId+" after exception - query :: "+Sql);
				 updateStatement = connection.prepareStatement(Sql);
				
				updateStatement.execute();
				updateStatement.close();*/
				
				throw e;
			}
			sheetCountDetails.put(modalSheetName+"-"+gridSheetName, modalCodes.size()+"*"+FinalmapList.size()+"="+modalCodes.size()*FinalmapList.size());
			totalRecordsCount=totalRecordsCount+(modalCodes.size()*FinalmapList.size());
			tableDeleteMethod(connection, getListOfTablesToTruncate(RPAConstants.GRID_Truncate_Table));
			
		}
		
	}

	@SuppressWarnings("deprecation")
	private List<String> getAgentCodesFromWorkBook(String SheetName, Workbook wb, List<String> agentCodes)

	{
		if (SheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Agent.Sheet.Name")))

		{
			logger.info("EXCEl Agent Code Reading Method is Started Here For Sheet::" + SheetName);
			Sheet agentSheet = wb.getSheet(SheetName);
			for (Row row : agentSheet) {
				if (row.getRowNum() != 0) {
					Cell cell = row.getCell(0);
						if (cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK
							|| (cell.getCellType() == Cell.CELL_TYPE_STRING && !cell.getStringCellValue().isEmpty()))) {

						{
							agentCodes.add(cell.getStringCellValue());

						}

					}

				}

			}
		}
		logger.info("EXCEl Agent Code Reading Method is Ended Here For Sheet::" + SheetName);
		logger.info("Agent Code Count::" + agentCodes.size());
		return agentCodes;
	}

	private Boolean agentCodeInsertionMethod(Connection conn, List<String> agentCodes) throws SQLException {
		Boolean status = false;
		 
		logger.info(" Agent Table Name:::" + AgentCodeTableName);
		for (String agentCode : agentCodes) {
			System.out.println("agentCode -->"+agentCode);
			String Sql = "insert into " + AgentCodeTableName + " values('" + agentCode.replace('\u00A0',' ').trim() + "')";
			System.out.println("Sql -->"+Sql);
			PreparedStatement statement = conn.prepareStatement(Sql);
			statement.execute();
			statement.close();
			status = true;
		}
		logger.info("No of AgentCode ::: " + agentCodes.size());
		return status;

	}

	private void processgridWiseAgentCodes(Workbook workbook, int SheetNo, Connection connection,
			List<String> agentCodes, String gridInsertId, long gridId, int sheetPairNo) throws SQLException, ParseException {
		
		
		//logger.info("Grid Upload Process For the Sheets ::" + SheetName);
		agentCodes = new ArrayList<String>();
		List<String> modalCodes = new ArrayList<String>();
		List<String> columnNames = new ArrayList<String>();
		List<Map<String, String>> FinalmapList = new ArrayList<Map<String, String>>();
		String firstSheetName ="",secondSheetName = "", thridSheetName="",gridSheetName="",modalSheetName="",agentSheetName="";
		
		firstSheetName = workbook.getSheetName(SheetNo);
		if(workbook.getNumberOfSheets()>SheetNo+1){
			secondSheetName = workbook.getSheetName(SheetNo + 1);
		}
		
		if(workbook.getNumberOfSheets()>SheetNo+2){
			thridSheetName = workbook.getSheetName(SheetNo + 2);
		}
		
		if (firstSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Agent.Sheet.Name"))){
			logger.info("Fetching Agent codes from the Sheet :::::" + firstSheetName);
			agentCodes = getAgentCodesFromWorkBook(firstSheetName, workbook, agentCodes);
			agentSheetName = firstSheetName;
		}else if(secondSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Agent.Sheet.Name"))){
			logger.info("Fetching Agent codes from the Sheet :::::" + secondSheetName);
			agentCodes = getAgentCodesFromWorkBook(secondSheetName, workbook, agentCodes);
			agentSheetName = secondSheetName;
		}else if(thridSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Agent.Sheet.Name"))){
			logger.info("Agent Validation Process For the Sheet :::::" + thridSheetName);
			agentCodes = getAgentCodesFromWorkBook(thridSheetName, workbook, agentCodes);
			agentSheetName = thridSheetName;
		}else{
			logger.error("gridProcessWithAgentCodes - Agent sheet not available in Sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2));
		}
		
		
		logger.info("GgridProcessWithAgentCodes agencode size after addition of sheet agent code::" +agentCodes.size());
		
		//String GridSheetName = workbook.getSheetName(SheetNo + 1);
		//logger.info("Grid Upload Process For the Sheets ::" + GridSheetName);
		
		insertGridWithRowStatus(connection,gridId,String.valueOf(sheetPairNo));
		
		if (firstSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))){
			logger.info("Fetching Grids from the sheet :::::" + firstSheetName);
			FinalmapList = getBasicGridDetailsFromWorkBook(firstSheetName, workbook, columnNames, FinalmapList,agentCodes,SheetNo);
			gridSheetName = firstSheetName;
		}else if(secondSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))){
			logger.info("Fetching Grids from the sheet :::::" + secondSheetName);
			FinalmapList = getBasicGridDetailsFromWorkBook(secondSheetName, workbook, columnNames, FinalmapList,agentCodes,SheetNo);
			gridSheetName = secondSheetName;
		}else if(thridSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))){
			logger.info("Fetching Grids from the sheet :::::" + thridSheetName);
			FinalmapList = getBasicGridDetailsFromWorkBook(thridSheetName, workbook, columnNames, FinalmapList,agentCodes,SheetNo);
			gridSheetName = thridSheetName;
		}else{
			logger.error("gridProcessWithAgentCodes - Grid sheet not available in Sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2));
		}
		
		
		if (firstSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Modal.Sheet.Name"))){
			logger.info("Fetching Model from the Sheet :::::" + firstSheetName);
			modalCodes = getModelCodesFromWorkBook(firstSheetName, workbook, modalCodes);
			modalSheetName = firstSheetName;
		}else if(secondSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Modal.Sheet.Name"))){
			logger.info("Fetching Model from the Sheet :::::" + secondSheetName);
			modalCodes = getModelCodesFromWorkBook(secondSheetName, workbook, modalCodes);
			modalSheetName = secondSheetName;
		}else if(thridSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Modal.Sheet.Name"))){
			logger.info("Fetching Model from the Sheet :::::" + thridSheetName);
			modalCodes = getModelCodesFromWorkBook(thridSheetName, workbook, modalCodes);
			modalSheetName = thridSheetName;
		}else{
			logger.error("gridProcessWithAgentCodes - Model sheet not available in Sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2));
		}
		modalCodes = UtilityFile.removeDuplicates(modalCodes);
		
		
		if(agentCodes.size()>0){
			agentCodes = UtilityFile.removeDuplicates(agentCodes);
			agentCodeInsertionMethod(connection, agentCodes);
		}
		
		if (!modalCodes.isEmpty()) {
			Boolean modalStatus = modalCodeInsertionMethod(connection, modalCodes);
			if (modalStatus) {
				ModalAgentCodeMappingMethod(connection);
			}
			
			Boolean status = insertBasicGridDetails(connection, FinalmapList);
			if (status) {
				createTempGridValues(connection,gridInsertId);
			}
			
			try{
				gridUploadMasterMethod(connection,gridInsertId,gridId,sheetPairNo);
			}catch(Exception e){
				logger.error("GgridProcessWithAgentCodes - Error while inserting records in main table from the sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2)+"::::::"+e);
				
				logger.info("GgridProcessWithAgentCodes - Reverting records inserted in main table");
				
				/*String Sql = " Delete from " + gridUploadMasterTableName + " where  REMARKS='"+gridInsertId+"' ";
				logger.info("GgridProcessWithAgentCodes - deleting inserted records with grid inserted id "+gridInsertId+" after exception - query :: "+Sql);
				PreparedStatement updateStatement = connection.prepareStatement(Sql);
				
				updateStatement.execute();
				updateStatement.close();*/
				
				throw e;
			}
			
			agentCodes = UtilityFile.removeDuplicates(agentCodes);
			sheetCountDetails.put(modalSheetName+"-"+gridSheetName+"-"+agentSheetName, modalCodes.size()+"*"+FinalmapList.size()+"*"+agentCodes.size()+"="+modalCodes.size()*FinalmapList.size()*agentCodes.size());
			totalRecordsCount=totalRecordsCount+(modalCodes.size()*FinalmapList.size()*agentCodes.size());
			tableDeleteMethod(connection, getListOfTablesToTruncate(RPAConstants.GRIDWISEAGENT_Truncate_Table));

		}

	}
	
	private void processFileWiseAgentCodes(Workbook workbook, int SheetNo, Connection connection,
			List<String> agentCodes, String gridInsertId, long gridId, int sheetPairNo) throws SQLException, ParseException {
		
		
		//logger.info("Grid Upload Process For the Sheets ::" + SheetName);
		List<String> modalCodes = new ArrayList<String>();
		List<String> columnNames = new ArrayList<String>();
		List<Map<String, String>> FinalmapList = new ArrayList<Map<String, String>>();
		String firstSheetName ="",secondSheetName = "",gridSheetName="",modalSheetName="";
		
		firstSheetName = workbook.getSheetName(SheetNo);
		if(workbook.getNumberOfSheets()>SheetNo+1){
			secondSheetName = workbook.getSheetName(SheetNo + 1);
		}
		
		if (firstSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))){
			logger.info("Fetching Grids from the sheet :::::" + firstSheetName);
			FinalmapList = getBasicGridDetailsFromWorkBook(firstSheetName, workbook, columnNames, FinalmapList,agentCodes,SheetNo);
			gridSheetName = firstSheetName;
		}else if(secondSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))){
			logger.info("Fetching Grids from the sheet :::::" + secondSheetName);
			FinalmapList = getBasicGridDetailsFromWorkBook(secondSheetName, workbook, columnNames, FinalmapList,agentCodes,SheetNo);
			gridSheetName = secondSheetName;
		}else{
			logger.error("gridProcessWithAgentCodes - Grid sheet not available in Sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2));
		}
		
		
		if (firstSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Modal.Sheet.Name"))){
			logger.info("Fetching Model from the Sheet :::::" + firstSheetName);
			modalCodes = getModelCodesFromWorkBook(firstSheetName, workbook, modalCodes);
			modalSheetName = firstSheetName;
		}else if(secondSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Modal.Sheet.Name"))){
			logger.info("Fetching Model from the Sheet :::::" + secondSheetName);
			modalCodes = getModelCodesFromWorkBook(secondSheetName, workbook, modalCodes);
			modalSheetName = secondSheetName;
		}else{
			logger.error("gridProcessWithAgentCodes - Model sheet not available in Sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2));
		}
		
		if(SheetNo==0){
			agentCodes = UtilityFile.removeDuplicates(agentCodes);
			logger.info("processFileWiseAgentCodes agencode size ::" +agentCodes.size());
			agentCodeInsertionMethod(connection, agentCodes);
		}
		
		insertGridWithRowStatus(connection,gridId,String.valueOf(sheetPairNo));
		
		modalCodes = UtilityFile.removeDuplicates(modalCodes);
		
		if (!modalCodes.isEmpty()) {
			Boolean modalStatus = modalCodeInsertionMethod(connection, modalCodes);
			if (modalStatus) {
				ModalAgentCodeMappingMethod(connection);
			}
			
			Boolean status = insertBasicGridDetails(connection, FinalmapList);
			if (status) {
				createTempGridValues(connection,gridInsertId);
			}
			
			
			try{
				gridUploadMasterMethod(connection,gridInsertId,gridId,sheetPairNo);
			}catch(Exception e){
				logger.error("gridProcessWithAgentCodes - Error while inserting records in main table from the sheet "+SheetNo+"&"+(SheetNo+1)+"&"+(SheetNo+2)+"::::::"+e);
				
				logger.info("gridProcessWithAgentCodes - Reverting records inserted in main table");
				
				/*String Sql = " Delete from " + gridUploadMasterTableName + " where  REMARKS='"+gridInsertId+"' ";
				logger.info("gridProcessWithAgentCodes - deleting inserted records with grid inserted id "+gridInsertId+" after exception - query :: "+Sql);
				PreparedStatement updateStatement = connection.prepareStatement(Sql);
				
				updateStatement.execute();
				updateStatement.close();*/
				
				throw e;
			}
			
			agentCodes = UtilityFile.removeDuplicates(agentCodes);
			sheetCountDetails.put(modalSheetName+"-"+gridSheetName+"-"+workbook.getSheetName(workbook.getNumberOfSheets()-1), modalCodes.size()+"*"+FinalmapList.size()+"*"+agentCodes.size()+"="+modalCodes.size()*FinalmapList.size()*agentCodes.size());
			totalRecordsCount=totalRecordsCount+(modalCodes.size()*FinalmapList.size()*agentCodes.size());
			tableDeleteMethod(connection, getListOfTablesToTruncate(RPAConstants.GRID_Truncate_Table));

		}

	}

	@SuppressWarnings("deprecation")
	private List<Map<String, String>> getBasicGridDetailsFromWorkBook(String gridSheetName, Workbook workbook,
			List<String> columnNames, List<Map<String, String>> finalmapList, List<String> agentCodes, int sheetNo) {
		DataFormatter df = new DataFormatter();

		if (gridSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name")))

		{
			logger.info("EXCEl GRID DETAIlS Reading Method is Started Here");
			Sheet gridSheet = workbook.getSheet(gridSheetName);
			// GetColumn Names
			Row columnRow = gridSheet.getRow(0);
			for (Cell cell : columnRow) {
				if (cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK
						|| (cell.getCellType() == Cell.CELL_TYPE_STRING && !cell.getStringCellValue().isEmpty()))) {
					columnNames.add(cell.toString().toUpperCase());
				}

			}
			for (Row row : gridSheet) {
				if (row.getRowNum() != 0 && row != null) {
					Map<String, String> map = new HashMap<>();
					for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
						Cell cell = row.getCell(i);
						
						if (cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK
								|| cell.getCellType() == Cell.CELL_TYPE_STRING
										&& !cell.getStringCellValue().isEmpty())) {
							if(agentCodes!=null && sheetNo==0){
								if(columnNames.get(i)!=null &&  columnNames.get(i).equals("Y_AXIS") && row.getRowNum()==1 ){
									agentCodes.add(df.formatCellValue(row.getCell(i)).split("_")[0]) ;
									
								}
							}
							
							map.put(columnNames.get(i), df.formatCellValue(row.getCell(i)));
						}
					}
					if (!map.isEmpty())
						finalmapList.add(map);

				}
			}
		}
		logger.info("Grid Details Count::" + finalmapList.size());
		return finalmapList;

	}

	@SuppressWarnings("deprecation")
	private List<String> getModelCodesFromWorkBook(String sheetName, Workbook workbook, List<String> modalCodes) {
		if (sheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.Modal.Sheet.Name")))

		{
			logger.info("EXCEl getModelCodesFromWorkBook Method is Started Here For ::" + sheetName);
			Sheet modalSheet = workbook.getSheet(sheetName);
			for (Row row : modalSheet) {
				if (row.getRowNum() != 0 && row != null && !UtilityFile.isRowEmpty(row)) {
					Cell cell = row.getCell(0);
					if (cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK
							|| (cell.getCellType() == Cell.CELL_TYPE_STRING && !cell.getStringCellValue().isEmpty()))) {
						modalCodes.add(cell.getStringCellValue());
					}
				}
			}
		}
		logger.info("EXCEl getModelCodesFromWorkBook  Method is Ended Here For ::" + sheetName);
		logger.info(" Model Code Count ::" + modalCodes.size());
		return modalCodes;
	}

	private Boolean modalCodeInsertionMethod(Connection conn, List<String> modelCodes) throws SQLException {
		Boolean status = false;
		logger.info("modalCodeInsertionMethod:::: ");
		logger.info(" Modal Table Name:::" + ModelCodeTableName);
		for (String modelCode : modelCodes) {
			String Sql = "insert into " + ModelCodeTableName + " values('" + modelCode.trim() + "')";

			PreparedStatement statement = conn.prepareStatement(Sql);
			statement.execute();
			statement.close();
			status = true;
		}
		logger.info("No of ModelCode ::: " + modelCodes.size());
		return status;

	}

	private List<String> getListOfTablesToTruncate(String key) {

		List<String> tableList = new ArrayList<String>();
		String[] tableNames = UtilityFile.getGridUploadProperty(key).split(RPAConstants.GRID_VALUE_SEPARATOR);
		for (String table : tableNames) {
			tableList.add(table);
		}
		return tableList;
	}

	private void tableDeleteMethod(Connection conn, List<String> tableName) throws SQLException {

		for (String table : tableName) {

			String sql = "truncate  table  " + table;
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.execute();
			statement.close();
		}

	}

	private void createTempGridValues(Connection conn, String gridInsertId) throws SQLException {

		/*With NCB Value column*/
		/*String sql = "insert into  " + gridTempTableName + " (select (select nvl(max(id),0) from "
				+ gridUploadMasterTableName + ")+rownum as ID , "
				+ " PRODUCT,MODELCODE as X_AXIS,(AGENTCODE ||'_'||REGEXP_SUBSTR(Y_AXIS,'[^_]+',1,2)) AS Y_AXIS, "
				+ " VALUE,GRID_ID,GRID_NAME,EFFECTIVE_START_DATE,EFFECTIVE_END_DATE,VERSION,TRANSACTIONTYPE,START_AGE, "
				+ " END_AGE,STATE,'0,1,2,3,4,5' " + " from " + ModelAgentMapTableName + " cross join "
				+ baseGridTableName + " )";*/
		
		String sql = "insert into  " + gridTempTableName + " (select (select nvl(max(id),0) from "
				+ gridUploadMasterTableName + ")+rownum as ID , "
				+ " PRODUCT,MODELCODE as X_AXIS,(AGENTCODE ||'_'||REGEXP_SUBSTR(Y_AXIS,'[^_]+',1,2)) AS Y_AXIS, "
				+ " VALUE,GRID_ID,GRID_NAME,EFFECTIVE_START_DATE,EFFECTIVE_END_DATE,VERSION,TRANSACTIONTYPE,START_AGE, "
				+ " END_AGE,STATE,sysdate,null,'"+gridInsertId+"','RPA_USER',null " + " from " + ModelAgentMapTableName + " cross join "
				+ baseGridTableName + " )";

		PreparedStatement statement = conn.prepareStatement(sql);
		statement.execute();
		statement.close();

	}

	private void createTempGridValuesWithoutAgentCodes(Connection conn,String gridInsertId) throws SQLException {
		/*With NCB Value column*/
		/*String sql = " insert into " + gridTempTableName + "  " + " (select (select nvl(max(id),0) from "
				+ gridUploadMasterTableName + ")+rownum as ID , "
				+ " PRODUCT,MODELCODE as X_AXIS,Y_AXIS,VALUE,GRID_ID,GRID_NAME,EFFECTIVE_START_DATE, "
				+ " EFFECTIVE_END_DATE,VERSION,TRANSACTIONTYPE,START_AGE,END_AGE,STATE,'0,1,2,3,4,5' " + " from "
				+ ModelCodeTableName + " cross join " + baseGridTableName + ")";*/
		
		String sql = " insert into " + gridTempTableName + "  " + " (select (select nvl(max(id),0) from "
				+ gridUploadMasterTableName + ")+rownum as ID , "
				+ " PRODUCT,MODELCODE as X_AXIS,Y_AXIS,VALUE,GRID_ID,GRID_NAME,EFFECTIVE_START_DATE, "
				+ " EFFECTIVE_END_DATE,VERSION,TRANSACTIONTYPE,START_AGE,END_AGE,STATE,sysdate,null,'"+gridInsertId+"','RPA_USER',null " + " from "
				+ ModelCodeTableName + " cross join " + baseGridTableName + ")";
		
		logger.info(sql);
		PreparedStatement statement = conn.prepareStatement(sql);
		statement.execute();
		statement.close();

	}

	private void gridUploadMasterMethod(Connection conn, String gridInsertId, long gridId,int sheetPairNo) throws SQLException {

		logger.info(" Grid upload Master Update Method Called here");
		PreparedStatement updateStatement = null;
		
		String RN_EffectiveStartDate = "",RNE_EffectiveStartDate="",sql="";
		
		 sql = "select distinct to_char(effective_start_date-1,'dd-mm-yy'),transactiontype from rpa_gridtemp group by effective_start_date,transactiontype";
		PreparedStatement statement = conn.prepareStatement(sql);
		ResultSet rs = statement.executeQuery();
		while (rs.next()) {
			if(rs.getString(2).equals("RN") || rs.getString(2).equals("NB")){
				RN_EffectiveStartDate = rs.getString(1);
				logger.info("Grid upload Master - RN_EffectiveStartDate  "+RN_EffectiveStartDate);
			}else if (rs.getString(2).equals("RNE")){
				RNE_EffectiveStartDate = rs.getString(1);
				logger.info("Grid upload Master - RNE_EffectiveStartDate  "+RNE_EffectiveStartDate);
			}
		}
		if(rs!=null)
			rs.close();
		if(statement!=null)
			statement.close();
		

		/*String Sql = " update ( select * from " + gridUploadMasterTableName + " GR "
				+ " where (GR.X_AXIS, GR.Y_AXIS, GR.TRANSACTIONTYPE ) in (select distinct GT.X_AXIS,GT.Y_AXIS,GT.TRANSACTIONTYPE  from " + gridTempTableName + " GT ))GR "
				+ "  set LAST_MODIFIED_DATE=sysdate,LASTMODIFIED_BY='RPA_USER',REMARKS='"+gridInsertId+"',GR.EFFECTIVE_END_DATE=(select distinct(GT.EFFECTIVE_START_DATE-1)  from " + gridTempTableName
				+ " GT "
				+ " where GT.TRANSACTIONTYPE = GR.TRANSACTIONTYPE and GT.EFFECTIVE_START_DATE <> GR.EFFECTIVE_START_DATE )";*/
		
		
		/*String Sql = "UPDATE (SELECT * FROM " + gridUploadMasterTableName + " GR WHERE (GR.X_AXIS, GR.Y_AXIS ) IN ( SELECT DISTINCT GT.X_AXIS, GT.Y_AXIS FROM " + gridTempTableName + " GT WHERE GT.TRANSACTIONTYPE <> 'RNE' "
				+ "AND NVL(GT.STATE,0) LIKE NVL(GR.STATE,0) )"
				+ " AND GR.TRANSACTIONTYPE <> 'RNE' AND (GR.EFFECTIVE_END_DATE IS NULL OR GR.EFFECTIVE_END_DATE  > (SELECT DISTINCT effective_start_date-1 FROM " + gridTempTableName + " WHERE transactiontype <> 'RNE') )) "
				+ "GR SET LAST_MODIFIED_DATE =sysdate,LASTMODIFIED_BY ='RPA_USER',REMARKS='11111',GR.EFFECTIVE_END_DATE=( SELECT DISTINCT effective_start_date-1 FROM " + gridTempTableName + " WHERE transactiontype <> 'RNE')";*/
		
		if(!RN_EffectiveStartDate.equals("")){
		logger.info(" Grid upload Master Update started for NB & RN :::::::::::");
		
		/*String Sql = "UPDATE (SELECT * FROM " + gridUploadMasterTableName + " GR WHERE (GR.X_AXIS, GR.Y_AXIS ) IN ( SELECT DISTINCT GT.X_AXIS, GT.Y_AXIS FROM " + gridTempTableName + " GT WHERE GT.TRANSACTIONTYPE <> 'RNE' "
				+ "AND NVL(GT.STATE,0) LIKE NVL(GR.STATE,0) )"
				+ " AND GR.TRANSACTIONTYPE <> 'RNE' AND (GR.EFFECTIVE_END_DATE IS NULL OR GR.EFFECTIVE_END_DATE  > (TO_DATE('"+RN_EffectiveStartDate+"','DD-MM-YY') ) ) ) "
				+ "GR SET LAST_MODIFIED_DATE =sysdate,LASTMODIFIED_BY ='RPA_USER',REMARKS='"+gridInsertId+"',GR.EFFECTIVE_END_DATE=(TO_DATE('"+RN_EffectiveStartDate+"','DD-MM-YY') ) ";
		
		logger.info("Grid upload Master- UPDATE query for RNE "+Sql);
		 updateStatement = conn.prepareStatement(Sql);
		
		updateStatement.setQueryTimeout(3600);

		updateStatement.execute();
		updateStatement.close();
		conn.commit();*/
		
		callUpdateProcedure(conn,gridId,String.valueOf(sheetPairNo),gridInsertId,RN_EffectiveStartDate,"NB");
		
		logger.info(" Grid upload Master Update ended for NB & RN :::::::::::");
		}else{
			logger.info(" Grid upload Master  NB or RN not availble in grid sheet :::::::::::");
		}

		if(!RNE_EffectiveStartDate.equals("")){
		/*logger.info(" Grid upload Master Update started for RNE :::::::::::");
		 sql = "UPDATE (SELECT * FROM " + gridUploadMasterTableName + " GR WHERE (GR.X_AXIS, GR.Y_AXIS ) IN ( SELECT DISTINCT GT.X_AXIS, GT.Y_AXIS FROM " + gridTempTableName + " GT WHERE GT.TRANSACTIONTYPE <> 'RNE' "
				+ "AND NVL(GT.STATE,0) LIKE NVL(GR.STATE,0) )"
				+ " AND GR.TRANSACTIONTYPE <> 'RNE' AND (GR.EFFECTIVE_END_DATE IS NULL OR GR.EFFECTIVE_END_DATE  > (TO_DATE('"+RNE_EffectiveStartDate+"','DD-MM-YY') ) ) ) "
				+ "GR SET LAST_MODIFIED_DATE =sysdate,LASTMODIFIED_BY ='RPA_USER',REMARKS='"+gridInsertId+"',GR.EFFECTIVE_END_DATE=(TO_DATE('"+RNE_EffectiveStartDate+"','DD-MM-YY') ) ";
		
		logger.info("UPDATE query for RNE "+sql);
		 updateStatement = conn.prepareStatement(sql);
		
		updateStatement.setQueryTimeout(3600);

		updateStatement.execute();
		updateStatement.close();
		conn.commit();*/
		
		callUpdateProcedure(conn,gridId,String.valueOf(sheetPairNo),gridInsertId,RNE_EffectiveStartDate,"RNE");
		
		logger.info(" Grid upload Master Update ended for RNE :::::::::::");
		}else{
			logger.info(" Grid upload Master RNE not availble in grid sheet :::::::::::");
		}
		
		
		logger.info(" Grid upload Master Insert Method Called here");
		/* statement = conn.prepareStatement(
				"insert into " + gridUploadMasterTableName + " (SELECT * FROM " + gridTempTableName + ")");

		statement.execute();
		
		statement.close();
		conn.commit();
		*/
		String result="";
		CallableStatement callStmt = null;
		try {
				callStmt = conn.prepareCall("CALL gridAutomation_insert(?,?,?,?)");
			
			callStmt.setLong(1, gridId);
			callStmt.setString(2, String.valueOf(sheetPairNo));
			callStmt.registerOutParameter(3, Types.VARCHAR);
			callStmt.setString(4, gridInsertId);
			callStmt.execute();
			result = callStmt.getString(3);
			
			
			logger.info("GridMasterFileUploadProcessor - END gridAutomation_insert() result ::" + result);
		}finally {
			
			if (callStmt != null) {
				callStmt.close();
			}
		}
		
		
		
		/*catch(Exception e){
			logger.error("GridMasterFileUploadProcessor  - error from callUpdateProcedure SP gridautomation update :: "+e);
			
			logger.info("GridMasterFileUploadProcessor - Reverting records updated in main table");
			
			String Sql = " update " + gridUploadMasterTableName + " set EFFECTIVE_END_DATE=null  where  REMARKS='"+gridInsertId+"' ";
			logger.info("gridProcessWithoutAgentCodes - reverting updated records records with grid inserted id "+gridInsertId+" after exception - query :: "+Sql);
			PreparedStatement updateStatement = conn.prepareStatement(Sql);
			
			updateStatement.execute();
			updateStatement.close();
		}*/
		
		logger.info(" Grid upload Master Insert Method Ended here");
		


	}

	private Boolean insertBasicGridDetails(Connection conn, List<Map<String, String>> finalmapList)
			throws SQLException, ParseException {
		logger.info("insertBasicGridDetails ::");

		Boolean status = false;
		for (Map<String, String> map : finalmapList) {
			SimpleDateFormat originalFormat = new SimpleDateFormat("MMMM/dd/yyyy hh:mm:ss");
			Date effectiveStartDate = originalFormat.parse(map.get("EFFECTIVE_START_DATE")), effectiveEndDate = null;
			Date current = new Date();
			if(effectiveStartDate.before(current)){
				logger.info("The effectiveStartDate "+effectiveStartDate+" is older than current day");
				effectiveStartDate = new Date();
	        }
			
			String effectiveEndDateMap = map.get("EFFECTIVE_END_DATE");

			if (effectiveEndDateMap != null && effectiveEndDateMap != "" && effectiveEndDateMap.length() >= 10) {
				logger.info(" start date length ::" + map.get("EFFECTIVE_END_DATE").length());

				effectiveEndDate = originalFormat.parse(effectiveEndDateMap);
			}

			
			PreparedStatement statement = conn
					.prepareStatement("insert into  " + baseGridTableName + " values(?,?,?,?,?,?,?,?,?,?,?,?)");
			logger.info("Base Grid Table Name:::" + baseGridTableName);
			statement.setString(1, map.get("PRODUCT").trim());
			statement.setString(2, map.get("Y_AXIS").trim());
			statement.setFloat(3, Float.valueOf(map.get("VALUE").trim()));
			statement.setInt(4, Integer.valueOf(map.get("GRID_ID").trim()));
			statement.setString(5, map.get("GRID_NAME").trim());
			statement.setDate(6, new java.sql.Date(effectiveStartDate.getTime()));
			if (effectiveEndDateMap != null && effectiveEndDateMap != "" && effectiveEndDateMap.length() >= 10) {
				statement.setDate(7, new java.sql.Date(effectiveEndDate.getTime()));
			} else {
				statement.setDate(7, null);
			}
			statement.setInt(8, Integer.valueOf(map.get("VERSION").trim()));
			statement.setString(9, map.get("TRANSACTIONTYPE").trim());
			statement.setInt(10, Integer.valueOf(map.get("START_AGE").trim()));
			statement.setInt(11, Integer.valueOf(map.get("END_AGE").trim()));
			statement.setString(12, map.get("STATE"));

			statement.execute();
			status = true;
			statement.close();
		}
		return status;

	}

	private void ModalAgentCodeMappingMethod(Connection conn) throws SQLException {
		logger.info("ModalAgentCodeMappingMethod ::");
		String Sql = "insert into " + ModelAgentMapTableName + " (select  MODELCODE,AGENTCODE FROM   "
				+ ModelCodeTableName + "  CROSS JOIN  " + AgentCodeTableName + ") ";
		logger.info(" ModalAgentCode Table Name:::" + ModelAgentMapTableName);
		PreparedStatement statement = conn.prepareStatement(Sql);
		statement.execute();
		statement.close();

	}
	
	private String getNextGridInsertId(String paramValue) throws InstantiationException, IllegalAccessException,
	ClassNotFoundException, SQLException, URISyntaxException {
logger.info("abiblPolicyPdfUploader - BEGIN getNextGridInsertId() called");
Connection conn = UtilityFile.getLocalRPAConnection();
String nextGridInsertId = "";
CallableStatement callStmt = null;
try {
	callStmt = conn.prepareCall("CALL UPDATE_MAX_ID(?,?)");
	callStmt.setString(1, paramValue);
	callStmt.registerOutParameter(2, Types.VARCHAR);
	callStmt.execute();
	nextGridInsertId = callStmt.getString(2);
} finally {
	if (conn != null) {
		conn.close();
	}
	if (callStmt != null) {
		callStmt.close();
	}
}

if(nextGridInsertId==null){
	logger.error("GridMasterFileUploadProcessor -  getNextGridInsertId - Param value not available in rpa_num_ctrl for key  ::" + paramValue);
}

logger.info("GridMasterFileUploadProcessor - getNextGridInsertId ::" + nextGridInsertId);
return nextGridInsertId;
}
	
	
	private boolean callUpdateProcedure(Connection conn,long gridId,String sheetPairNo, String gridInsertId,String effectDate,String flag) throws SQLException {

		
		logger.info("GridMasterFileUploadProcessor - BEGIN callUpdateProcedure() called flag :: "+flag);
		String result = "",errorMsg="";
		CallableStatement callStmt = null;
		try {
			if(flag.equals("RNE")){
				callStmt = conn.prepareCall("CALL gridautomation_update_RNE(?,?,?,?,?)");
			}else {
				callStmt = conn.prepareCall("CALL gridautomation_update_Non_RNE(?,?,?,?,?)");
			}
			callStmt.setLong(1, gridId);
			callStmt.setString(2, sheetPairNo);
			callStmt.registerOutParameter(3, Types.VARCHAR);
			callStmt.setString(4, gridInsertId);
			callStmt.setString(5, effectDate);
			callStmt.execute();
			errorMsg = callStmt.getString(3);
		}
		/*catch(Exception e){
			logger.error("GridMasterFileUploadProcessor  - error from callUpdateProcedure SP gridautomation update :: "+e);
			
			logger.info("GridMasterFileUploadProcessor - Reverting records updated in main table");
			
			String Sql = " update " + gridUploadMasterTableName + " set EFFECTIVE_END_DATE=null  where  REMARKS='"+gridInsertId+"' ";
			logger.info("gridProcessWithoutAgentCodes - reverting updated records records with grid inserted id "+gridInsertId+" after exception - query :: "+Sql);
			PreparedStatement updateStatement = conn.prepareStatement(Sql);
			
			updateStatement.execute();
			updateStatement.close();
		}*/
		finally {
			
			if (callStmt != null) {
				callStmt.close();
			}
		}
		if(errorMsg!=null && !errorMsg.equals("")){
			logger.error("GridMasterFileUploadProcessor - END callUpdateProcedure() error from proecure gridautomation update ::" + result);
		}
		
		logger.info("GridMasterFileUploadProcessor - END callUpdateProcedure() result ::" + result);
		
		if(result.equals("") || result.equals("0")){
			return false;
		}else{
			return true;
		}


	}
	
	
	private Boolean insertGridWithRowStatus(Connection conn,long gridId,String sheetNo) throws SQLException {
		Boolean status = false;
		String Sql = "";
		PreparedStatement statement = null;
		logger.info("insertGridWithModelRowStatus:::: ");
		if(sheetNo.equals("1")){
			 Sql = "delete from GRID_MODEL_ROW_STATUS where GRID_ID = " + gridId;
	
			 statement = conn.prepareStatement(Sql);
			statement.execute();
			statement.close();
		}
			 Sql = "insert into GRID_MODEL_ROW_STATUS (GRID_ID,SHEET_PAIR_NO) values('" + gridId + "','" + sheetNo + "')";

			 statement = conn.prepareStatement(Sql);
			statement.execute();
			statement.close();
			status = true;
		return status;

	}
	
}
