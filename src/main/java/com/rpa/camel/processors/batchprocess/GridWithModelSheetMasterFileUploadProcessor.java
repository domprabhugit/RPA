package com.rpa.camel.processors.batchprocess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.monitorjbl.xlsx.StreamingReader;
import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.GridFilesCountDetails;
import com.rpa.model.processors.GridWithModelSheetAutomationModel;
import com.rpa.repository.TransactionInfoRepository;
import com.rpa.service.processors.GridWithModelSheetAutomationService;
import com.rpa.util.UtilityFile;

public class GridWithModelSheetMasterFileUploadProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(GridWithModelSheetMasterFileUploadProcessor.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	TransactionInfoRepository transactionInfoRepository;

	@Autowired
	GridWithModelSheetAutomationService gridAutomationService;

	private String AgentCodeTableName = UtilityFile.getGridUploadProperty("GridModel.Agent.Table.Name"),
			baseGridTableName = UtilityFile.getGridUploadProperty("GridModel.BaseGrid.Table.Name"),
			gridUploadMasterTableName = UtilityFile.getGridUploadProperty("GridModel.MainGrid.Table.Name");

	private int uploadCount = 0, uploadSuccesCount = 0,totalRecordsCount=0;
	
	private Map<String,String > sheetCountDetails=new LinkedHashMap<>();

	private void AutoWiringBeanPropertiesSetMethod() {

		logger.info("AutoWiringBeanPropertiesSetMethod is Called in GridMasterUploadProcessor Class");
		applicationContext = SpringContext.getAppContext();
		transactionInfoRepository = applicationContext.getBean(TransactionInfoRepository.class);

		gridAutomationService = applicationContext.getBean(GridWithModelSheetAutomationService.class);
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in GridMasterUploadProcessor Class");

	}

	@Override
	public void process(Exchange exchange) throws Exception {

		GridWithModelSheetMasterFileUploadProcessor grid = new GridWithModelSheetMasterFileUploadProcessor();
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
		List<GridWithModelSheetAutomationModel> gridErrorFileList = gridAutomationService
				.FindErrorFileList(transactionInfo.getTransactionStartDate(), RPAConstants.Completed);
		if (!gridErrorFileList.isEmpty()) {
			logger.info("Files count For Error::::" + gridErrorFileList.size());
			for (GridWithModelSheetAutomationModel grid : gridErrorFileList) {

				String newFilePath = UtilityFile.FileMovementMethod(
						UtilityFile.getCodeBasePath()
								+ UtilityFile.getGridUploadProperty("GridModel.File.Location.BasePath"),
						new File(grid.getFilePath()),
						UtilityFile.getGridUploadProperty("GridModel.File.Processed.Path")
								+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY).toString()
								+ UtilityFile.getGridUploadProperty("GridModel.File.Location.ErrorPath"));
				grid.setEndDate(new Date());
				grid.setFilePath(newFilePath);
				grid.setFileStatus("ValidationFailed");
				gridAutomationService.save(grid);

			}

			

		}
		List<GridWithModelSheetAutomationModel> gridUploadFileList = gridAutomationService
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
					getListOfTablesToTruncate(RPAConstants.GRIDMODEL_Truncate_All_Table));
			logger.info("Files count For upload::::" + gridUploadFileList.size());
			uploadCount = gridUploadFileList.size();
			transactionInfo.setProcessPhase(RPAConstants.GRID_UPLOADER_PHASE);
			for (GridWithModelSheetAutomationModel grid : gridUploadFileList) {
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
	

	private void FileUploaderProcessMethod(TransactionInfo transactionInfo, GridWithModelSheetAutomationModel gridAutoMationObject)
			throws URISyntaxException, EncryptedDocumentException, InvalidFormatException, IOException,
			InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, ParseException {
		logger.info(" FileUploaderProcessMethod  called Here ");
		gridAutoMationObject.setIsProcessed("S");
		 gridAutomationService.save(gridAutoMationObject);
		
		/*List<String> agentCodes = new ArrayList<String>();*/
		sheetCountDetails.clear();
		totalRecordsCount=0;
		Connection connection = UtilityFile.getGridDbConnection();
	//	Workbook workbook = null;
		
		String FilePath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getGridUploadProperty("GridModel.File.Location.BasePath");
		
		gridAutoMationObject.setStartDate(transactionInfo.getTransactionStartDate());

		File file = new File(gridAutoMationObject.getFilePath());
		gridAutoMationObject.setFileName(file.getName());
		
		//workbook = UtilityFile.workBookCreation(file.getAbsolutePath());
		try (
				  InputStream is = new FileInputStream(new File(file.getAbsolutePath()));
				Workbook workbook = StreamingReader.builder()
				          .rowCacheSize(100)
				          .bufferSize(4096)
				          .open(is)) {
			
			//String SheetName = workbook.getSheetName(Integer.valueOf(gridAutoMationObject.getTotalSheetCount()) - 1);
			transactionInfo.setInputFilePath_1(file.getAbsolutePath());
			if (gridAutoMationObject.getFileType().equals("GridWithModelAgentRegionProcess")) {
					int i = 0,p=0;
					
					for (i = Integer.valueOf(gridAutoMationObject.getSuccessNo()); i < Integer
							.valueOf(gridAutoMationObject.getTotalSheetCount())-1; i = i + 2) {
						p++;
						gridAutoMationObject.setSuccessNo(String.valueOf(i + 1));
						gridAutoMationObject.setFileStatus("GridUploadProcess");
						if(!gridProcessWithAgentCodes(workbook, i, connection,gridAutoMationObject.getId(),p,gridAutoMationObject.getGridInsertId(),gridAutoMationObject.getFileType())){
							gridAutoMationObject.setFileStatus("Failed");
							break;
						}
						gridAutomationService.save(gridAutoMationObject);
					}
					if (i == Integer.valueOf(gridAutoMationObject.getTotalSheetCount())) {
						gridAutoMationObject.setSuccessNo(gridAutoMationObject.getTotalSheetCount());
						gridAutoMationObject.setFileStatus(RPAConstants.Success);
					}else{
						String newFilePath = UtilityFile.FileMovementMethod(
								UtilityFile.getCodeBasePath()
										+ UtilityFile.getGridUploadProperty("GridModel.File.Location.BasePath"),
								new File(gridAutoMationObject.getFilePath()),
								UtilityFile.getGridUploadProperty("GridModel.File.Processed.Path")
										+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY).toString()
										+ UtilityFile.getGridUploadProperty("GridModel.File.Location.ErrorPath"));
						gridAutoMationObject.setEndDate(new Date());
						gridAutoMationObject.setFilePath(newFilePath);
						gridAutoMationObject.setFileStatus("Error");
						gridAutoMationObject.setIsProcessed(RPAConstants.Y);
						gridAutomationService.save(gridAutoMationObject);
					}
					logger.info("To delete AgentCode  After the Excel File Complete");
					tableDeleteMethod(connection, new ArrayList<String>(Arrays.asList(UtilityFile.getGridUploadProperty("GridModel.truncate.Table.Agent"))));


			} else if (gridAutoMationObject.getFileType().equals("GridWithModelAgentProcess") || gridAutoMationObject.getFileType().equals("GridWithModelRegionProcess") ) {
				int i = 0,p=0;
				i=1;
				for (i = 0 ; i < Integer
						.valueOf(gridAutoMationObject.getTotalSheetCount()); i = i + 1) {
					p++;
					gridAutoMationObject.setSuccessNo(String.valueOf(i + 1));
					gridAutoMationObject.setFileStatus("GridUploadProcess");
					if(!gridProcessWithAgentRegion(workbook, i, connection,gridAutoMationObject.getId(),p,gridAutoMationObject.getGridInsertId(),gridAutoMationObject.getFileType() )){
						gridAutoMationObject.setFileStatus("Failed");
						break;
					}
					gridAutomationService.save(gridAutoMationObject);
				}
				if (i == Integer.valueOf(gridAutoMationObject.getTotalSheetCount())) {
					gridAutoMationObject.setSuccessNo(gridAutoMationObject.getTotalSheetCount());
					gridAutoMationObject.setFileStatus(RPAConstants.Success);
				}else{
					String newFilePath = UtilityFile.FileMovementMethod(
							UtilityFile.getCodeBasePath()
									+ UtilityFile.getGridUploadProperty("GridModel.File.Location.BasePath"),
							new File(gridAutoMationObject.getFilePath()),
							UtilityFile.getGridUploadProperty("GridModel.File.Processed.Path")
									+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY).toString()
									+ UtilityFile.getGridUploadProperty("GridModel.File.Location.ErrorPath"));
					gridAutoMationObject.setEndDate(new Date());
					gridAutoMationObject.setFilePath(newFilePath);
					gridAutoMationObject.setFileStatus("Error");
					gridAutoMationObject.setIsProcessed(RPAConstants.Y);
					gridAutomationService.save(gridAutoMationObject);
				}
				logger.info("To delete AgentCode  After the Excel File Complete");
				tableDeleteMethod(connection, new ArrayList<String>(Arrays.asList(UtilityFile.getGridUploadProperty("GridModel.truncate.Table.Agent"))));


		} 
			
		}
		/*try{*/
		
		/*}finally{
			if (workbook != null) {
				workbook.close();
			}
		}*/

		if (gridAutoMationObject.getFileStatus().equalsIgnoreCase(RPAConstants.Success)) {
			String newFileName = UtilityFile.FileMovementMethod(FilePath, new File(gridAutoMationObject.getFilePath()),
					UtilityFile.getGridUploadProperty("GridModel.File.Processed.Path")
							+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY).toString()
							+ UtilityFile.getGridUploadProperty("GridModel.File.Location.UploadedPath"));
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
		
	}

	private List<GridFilesCountDetails> GridFileCountDetailsMethod(GridWithModelSheetAutomationModel gridAutoMationObject, Map<String, String> sheetCountDetails) {
		
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

	private List<String> getAgentCodesFromWorkBook(String SheetName, Workbook wb, List<String> agentCodes)

	{
		if (SheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("GridModel.Agent.Sheet.Name")))

		{
			logger.info("EXCEl Agent Code Reading Method is Started Here For Sheet::" + SheetName);
			Sheet agentSheet = wb.getSheet(SheetName);
			for (Row row : agentSheet) {
				if (row.getRowNum() != 0) {
					Cell cell = row.getCell(0);
						if (cell != null && (cell.getCellTypeEnum() != CellType.BLANK
							|| (cell.getCellTypeEnum() == CellType.STRING && !cell.getStringCellValue().isEmpty()))) {

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

	private boolean gridProcessWithAgentCodes(Workbook workbook, int SheetNo, Connection connection, long gridId, int sheetPairNo, String gridInsertId, String fileType) throws SQLException, ParseException {
		boolean isCurrentSheetSetIsInserted = false;
		String SheetName = workbook.getSheetName(SheetNo+1);
		String GridSheetName = workbook.getSheetName(SheetNo);
		logger.info("Grid Upload Process For the Sheets ::" + SheetName);
		List<String> agentCodes = new ArrayList<String>();
		List<String> columnNames = new ArrayList<String>();
		List<Map<String, String>> FinalmapList = new ArrayList<Map<String, String>>();
		
		agentCodes = getAgentCodesFromWorkBook(SheetName, workbook, agentCodes);
		agentCodes = UtilityFile.removeDuplicates(agentCodes);
		if (!agentCodes.isEmpty()) {
		agentCodeInsertionMethod(connection, agentCodes);
		}

		logger.info("Grid Upload Process For the Sheets ::" + GridSheetName);
		FinalmapList = getBasicGridDetailsFromWorkBook(GridSheetName, workbook, columnNames, FinalmapList);
		insertGridWithModelRowStatus(connection,gridId,String.valueOf(sheetPairNo));

			Boolean status = insertBasicGridDetails(connection, FinalmapList,gridInsertId,fileType);
			if (status) {
				/*if(createTempGridValues(connection,gridId,String.valueOf(sheetPairNo))){*/
				try{
					isCurrentSheetSetIsInserted = gridUploadMasterMethod(connection,gridId,String.valueOf(sheetPairNo),gridInsertId,"AG-C");
				}catch(Exception e){
					logger.error("GridWithModelSheetMasterFileUploadProcessor  - error from gridUploadMasterMethod  :: "+e);
					
					logger.info("GridWithModelSheetMasterFileUploadProcessor - Reverting records inserted in main table");
					
					/*String Sql = " Delete from " + gridUploadMasterTableName + " where  REMARKS='"+gridInsertId+"' ";
					logger.info("gridProcessWithoutAgentCodes - deleting inserted records with grid inserted id "+gridInsertId+" after exception - query :: "+Sql);
					PreparedStatement updateStatement = connection.prepareStatement(Sql);
					
					updateStatement.execute();
					updateStatement.close();
					
					 Sql = " update " + gridUploadMasterTableName + " set EFFECTIVE_END_DATE=null  where  REMARKS='"+gridInsertId+"' ";
					logger.info("gridProcessWithoutAgentCodes - reverting updated records records with grid inserted id "+gridInsertId+" after exception - query :: "+Sql);
					 updateStatement = connection.prepareStatement(Sql);
					updateStatement.execute();
					updateStatement.close();*/
					
					throw e;
				}
					/*}*/
			}
			//isCurrentSheetSetIsInserted = true;
			tableDeleteMethod(connection, getListOfTablesToTruncate(RPAConstants.GRIDMODEL_Truncate_All_Table));

		
		
			sheetCountDetails.put(SheetName+"-"+GridSheetName, FinalmapList.size()+"*"+agentCodes.size()+"="+FinalmapList.size()*agentCodes.size());
			//sheetCountDetails.put(SheetName+"-"+GridSheetName, agentCodes.size()+"*"+FinalmapList.size()+"*"+agentCodes.size());
			totalRecordsCount=totalRecordsCount+(FinalmapList.size()*agentCodes.size());

			return isCurrentSheetSetIsInserted;
		}

	private List<Map<String, String>> getBasicGridDetailsFromWorkBook(String gridSheetName, Workbook workbook,
			List<String> columnNames, List<Map<String, String>> finalmapList) {
		DataFormatter df = new DataFormatter();

		if (gridSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("GridModel.BaseGrid.Sheet.Name")))

		{
			logger.info("EXCEl GRID DETAIlS Reading Method is Started Here");
			Sheet gridSheet = workbook.getSheet(gridSheetName);
			// GetColumn Names
			//Row columnRow = gridSheet.getRow(0);
			int c = 0;
			for (Row row : gridSheet) {
				if(c==0){
					for (Cell cell : row) {
						
						if (cell != null && (cell.getCellTypeEnum() != CellType.BLANK
								|| (cell.getCellTypeEnum() == CellType.STRING && !cell.getStringCellValue().isEmpty()))) {
							columnNames.add(cell.getStringCellValue().toUpperCase());
						}

					}
				}
				c++;
				
				if (row.getRowNum() != 0 && row != null) {
					Map<String, String> map = new HashMap<>();
					for (int i = 0; i < columnNames.size(); i++) {
						Cell cell = row.getCell(i);
						
						if (cell != null && (cell.getCellTypeEnum() != CellType.BLANK
								|| cell.getCellTypeEnum() == CellType.STRING
										&& !cell.getStringCellValue().isEmpty())) {
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

	private boolean gridUploadMasterMethod(Connection conn,long gridId,String sheetPairNo, String gridInsertId,String flag) throws SQLException {

		String RN_EffectiveStartDate = "",RNE_EffectiveStartDate="",sql="";
		
		 sql = "select distinct to_char(effective_start_date-1,'dd-mm-yy'),transactiontype from rpa_grid_model group by effective_start_date,transactiontype";
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
		
		if(!RN_EffectiveStartDate.equals("")){
			callUpdateProcedure(conn,gridId,String.valueOf(sheetPairNo),gridInsertId,RN_EffectiveStartDate,"NB",flag);
		}else{
			logger.info(" Grid upload Master  NB or RN not availble in grid sheet :::::::::::");
		}
		
		if(!RNE_EffectiveStartDate.equals("")){
			callUpdateProcedure(conn,gridId,String.valueOf(sheetPairNo),gridInsertId,RNE_EffectiveStartDate,"RNE",flag);
		}else{
			logger.info(" Grid upload Master RNE not availble in grid sheet :::::::::::");
		}
		
		
		String result = "",errorMsg="";
		CallableStatement callStmt = null;
		try {
		
		logger.info("GridWithModelSheetMasterFileUploadProcessor - BEGIN gridUploadMasterMethod() called flag :: "+flag);
			if(flag.equals("AG-R")){
				callStmt = conn.prepareCall("CALL gridwithmodel_insert_region(?,?,?,?,?)");
			}else if(flag.equals("AG-C")){
				callStmt = conn.prepareCall("CALL gridwithmodel_insert(?,?,?,?,?)");
			}
			
			callStmt.setLong(1, gridId);
			callStmt.setString(2, sheetPairNo);
			callStmt.registerOutParameter(3, Types.VARCHAR);
			callStmt.registerOutParameter(4, Types.VARCHAR);
			callStmt.setString(5, gridInsertId);
			callStmt.execute();
			result = callStmt.getString(3);
			errorMsg = callStmt.getString(4);
		
		/*if(errorMsg!=null && !errorMsg.equals("")){
			logger.error("GridWithModelSheetMasterFileUploadProcessor - END gridUploadMasterMethod() error from proecure gridwithmodel INSERT ::" + result);
		}*/
		/*if(insertedRows!=null && !insertedRows.equals("")){
			logger.error("GridWithModelSheetMasterFileUploadProcessor - END gridUploadMasterMethod() inserted row count ::" + insertedRows);
		}*/
		
		logger.info("GridWithModelSheetMasterFileUploadProcessor - END gridUploadMasterMethod() result ::" + result);
		
		if(result.equals("") || result.equals("0")){
			return false;
		}else{
			return true;
		}
		}
		finally {
			
			if (callStmt != null) {
				callStmt.close();
			}
		}


	}

	private Boolean insertBasicGridDetails(Connection conn, List<Map<String, String>> finalmapList, String gridInsertId, String fileTpye)
			throws SQLException, ParseException {
		logger.info("insertBasicGridDetails ::");

		Boolean status = false;
		int c = 0 ;
		String gridName = "";
		if(fileTpye.equalsIgnoreCase("GridWithModelAgentRegionProcess")){
			gridName = RPAConstants.Agent_Region;
		}else if(fileTpye.equalsIgnoreCase("GridWithModelAgentProcess")){
			gridName = RPAConstants.Model_Agent;
		}else if(fileTpye.equalsIgnoreCase("GridWithModelRegionProcess")){
			gridName = RPAConstants.Model_Region;
		}
		
		for (Map<String, String> map : finalmapList) {
			SimpleDateFormat originalFormat = new SimpleDateFormat("MMMM/dd/yyyy hh:mm:ss");
			String effectiveEndDateMap = map.get("EFFECTIVE_END_DATE");
			
			Date effectiveStartDate = originalFormat.parse(map.get("EFFECTIVE_START_DATE")), effectiveEndDate = null;
			Date current = new Date();
			if(effectiveStartDate.before(current)){
				logger.info("The effectiveStartDate "+effectiveStartDate+" is older than current day");
				effectiveStartDate = new Date();
	        }

			if (effectiveEndDateMap != null && effectiveEndDateMap != "" && effectiveEndDateMap.length() >= 10) {
				logger.info(" start date length ::" + map.get("EFFECTIVE_END_DATE").length());

				effectiveEndDate = originalFormat.parse(effectiveEndDateMap);
			}

			c++;
			PreparedStatement statement = conn
					.prepareStatement("insert into  " + baseGridTableName + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			logger.info("Base Grid Table Name::: c --> "+ c +" -- " + baseGridTableName);

			if(map.get("PRODUCT")!=null && map.get("PRODUCT").indexOf("_")!=-1){
				if(map.get("PRODUCT").split("_")[0].equalsIgnoreCase(RPAConstants.VGC)){
					statement.setString(1, RPAConstants.VGC_Product);
				}else if(map.get("PRODUCT").split("_")[0].equalsIgnoreCase(RPAConstants.VPC)){
					statement.setString(1, RPAConstants.VPC_Product);
				}else if(map.get("PRODUCT").split("_")[0].equalsIgnoreCase(RPAConstants.VPCV)){
					statement.setString(1, RPAConstants.VPCV_Product);
				}else if(map.get("PRODUCT").split("_")[0].equalsIgnoreCase(RPAConstants.VMC)){
					statement.setString(1, RPAConstants.VMC_Product);
				}else if(map.get("PRODUCT").split("_")[0].equalsIgnoreCase(RPAConstants.VOC)){
					statement.setString(1, RPAConstants.VOC_Product);
				}
				
			}else{
				statement.setString(1, map.get("PRODUCT").trim());
			}
			
			statement.setString(2, map.get("Y_AXIS").trim());
			statement.setFloat(3, Float.valueOf(map.get("VALUE").trim()));
			statement.setInt(4, Integer.valueOf(map.get("GRID_ID").trim()));
			statement.setString(5, gridName);
			statement.setDate(6, new java.sql.Date(effectiveStartDate.getTime()));
			if (effectiveEndDateMap != null && effectiveEndDateMap != "" && effectiveEndDateMap.length() >= 10) {
				statement.setDate(7, new java.sql.Date(effectiveEndDate.getTime()));
			} else {
				statement.setDate(7, null);
			}
			statement.setInt(8, Integer.valueOf(map.get("VERSION").trim()));
			statement.setString(9, map.get("TRANSACTIONTYPE").trim());
			statement.setInt(10, Integer.valueOf(map.get("START_AGE").trim()));
			/*if(map.get("END_AGE")!=null)*/
				statement.setInt(11, Integer.valueOf(map.get("END_AGE").trim()));
			/*else
				statement.setInt(11, Integer.valueOf("0"));*/
			statement.setString(12, map.get("STATE"));
			statement.setString(13, map.get("MODEL CODE").trim());
			
			statement.setDate(14, new java.sql.Date(new Date().getTime()));
			statement.setDate(15,null);
			statement.setString(16,gridInsertId);
			statement.setString(17,"RPA_USER");
			statement.setString(18,null);

			statement.execute();
			status = true;
			statement.close();
		}
		return status;

	}

	private Boolean insertGridWithModelRowStatus(Connection conn,long gridId,String sheetNo) throws SQLException {
		Boolean status = false;
		String Sql = "";
		PreparedStatement statement = null;
		logger.info("insertGridWithModelRowStatus:::: ");
		if(sheetNo.equals("1")){
			 Sql = "delete from GRID_MODEL_ROW_STATUS where GRID_MODEL_ID = " + gridId;
	
			 statement = conn.prepareStatement(Sql);
			statement.execute();
			statement.close();
		}
			 Sql = "insert into GRID_MODEL_ROW_STATUS (GRID_MODEL_ID,SHEET_PAIR_NO) values('" + gridId + "','" + sheetNo + "')";

			 statement = conn.prepareStatement(Sql);
			statement.execute();
			statement.close();
			status = true;
		return status;

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
	
	
	private boolean gridProcessWithAgentRegion(Workbook workbook, int SheetNo, Connection connection, long gridId, int sheetPairNo, String gridInsertId, String fileTpye) throws SQLException, ParseException {
		boolean isCurrentSheetSetIsInserted = false;
		/*String SheetName = workbook.getSheetName(SheetNo+1);*/
		String GridSheetName = workbook.getSheetName(SheetNo);
		logger.info("Grid Upload Process For the grid Sheets ::" + GridSheetName);
		/*List<String> agentCodes = new ArrayList<String>();*/
		List<String> columnNames = new ArrayList<String>();
		List<Map<String, String>> FinalmapList = new ArrayList<Map<String, String>>();
		
		/*agentCodes = getAgentCodesFromWorkBook(SheetName, workbook, agentCodes);
		agentCodes = UtilityFile.removeDuplicates(agentCodes);
		if (!agentCodes.isEmpty()) {
		agentCodeInsertionMethod(connection, agentCodes);
		}*/

		logger.info("Grid Upload Process For the Sheets ::" + GridSheetName);
		FinalmapList = getBasicGridDetailsFromWorkBook(GridSheetName, workbook, columnNames, FinalmapList);
		insertGridWithModelRowStatus(connection,gridId,String.valueOf(sheetPairNo));

			Boolean status = insertBasicGridDetails(connection, FinalmapList,gridInsertId,fileTpye);
			if (status) {
				/*if(createTempGridValues(connection,gridId,String.valueOf(sheetPairNo))){*/
				try{	
					isCurrentSheetSetIsInserted = gridUploadMasterMethod(connection,gridId,String.valueOf(sheetPairNo),gridInsertId,"AG-R");
				}catch(Exception e){
					logger.error("GridWithModelSheetMasterFileUploadProcessor  - error from gridUploadMasterMethod :: "+e);
					
					/*logger.info("GridWithModelSheetMasterFileUploadProcessor - Reverting records inserted in main table");
					
					String Sql = " Delete from " + gridUploadMasterTableName + " where  REMARKS='"+gridInsertId+"' ";
					logger.info("gridProcessWithoutAgentCodes - deleting inserted records with grid inserted id "+gridInsertId+" after exception - query :: "+Sql);
					PreparedStatement updateStatement = connection.prepareStatement(Sql);
					
					updateStatement.execute();
					updateStatement.close();
					
					 Sql = " update " + gridUploadMasterTableName + " set EFFECTIVE_END_DATE=null  where  REMARKS='"+gridInsertId+"' ";
					logger.info("gridProcessWithoutAgentCodes - reverting updated records records with grid inserted id "+gridInsertId+" after exception - query :: "+Sql);
					 updateStatement = connection.prepareStatement(Sql);
					updateStatement.execute();
					updateStatement.close();*/
					
					throw e;
				}
				/*}*/
			}
			//isCurrentSheetSetIsInserted = true;
			tableDeleteMethod(connection, getListOfTablesToTruncate(RPAConstants.GRIDMODEL_Truncate_All_Table));

		
		
			sheetCountDetails.put(GridSheetName, FinalmapList.size()+"="+FinalmapList.size());
			totalRecordsCount=totalRecordsCount+(FinalmapList.size());

			return isCurrentSheetSetIsInserted;
		}
	
	
private boolean callUpdateProcedure(Connection conn,long gridId,String sheetPairNo, String gridInsertId,String effectDate,String trnflag, String flag) throws SQLException {
		logger.info("GridMasterFileUploadProcessor - BEGIN callUpdateProcedure() called flag :: "+flag);
		String result = "";
		CallableStatement callStmt = null;
		
		try {
			if(trnflag.equals("RNE")){
				if(flag.equalsIgnoreCase("AG-R")){
					callStmt = conn.prepareCall("CALL GridwithmodelR_Update_RNE(?,?,?,?,?)");
					callStmt.setLong(1, gridId);
					callStmt.setString(2, sheetPairNo);
					callStmt.registerOutParameter(3, Types.VARCHAR);
					callStmt.setString(4, gridInsertId);
					callStmt.setString(5, effectDate);
					callStmt.execute();
					result = callStmt.getString(3);
				}else if(flag.equalsIgnoreCase("AG-C")){
					callStmt = conn.prepareCall("CALL Gridwithmodel_Update_RNE(?,?,?,?,?)");
					callStmt.setLong(1, gridId);
					callStmt.setString(2, sheetPairNo);
					callStmt.registerOutParameter(3, Types.VARCHAR);
					callStmt.setString(4, gridInsertId);
					callStmt.setString(5, effectDate);
					callStmt.execute();
					result = callStmt.getString(3);
				}
				
			}else {
				if(flag.equalsIgnoreCase("AG-R")){
					callStmt = conn.prepareCall("CALL GridwithmodelR_Update_Non_RNE(?,?,?,?,?)");
					callStmt.setLong(1, gridId);
					callStmt.setString(2, sheetPairNo);
					callStmt.registerOutParameter(3, Types.VARCHAR);
					callStmt.setString(4, gridInsertId);
					callStmt.setString(5, effectDate);
					callStmt.execute();
					result = callStmt.getString(3);
				}else if(flag.equalsIgnoreCase("AG-C")){
					callStmt = conn.prepareCall("CALL Gridwithmodel_Update_Non_RNE(?,?,?,?,?)");
					callStmt.setLong(1, gridId);
					callStmt.setString(2, sheetPairNo);
					callStmt.registerOutParameter(3, Types.VARCHAR);
					callStmt.setString(4, gridInsertId);
					callStmt.setString(5, effectDate);
					callStmt.execute();
					result = callStmt.getString(3);
				}
				
			}
			
		}
		/*catch(Exception e){
			logger.error("GridMasterFileUploadProcessor  - error from callUpdateProcedure SP Bulk_update_Non_RNE :: "+e);
			
			logger.info("GridMasterFileUploadProcessor - Reverting records updated in main table");
			
			String Sql = " update " + gridUploadMasterTableName + " set EFFECTIVE_END_DATE=null  where  REMARKS='"+gridInsertId+"' ";
			logger.info("gridProcessWithoutAgentCodes - reverting updated records records with grid inserted id "+gridInsertId+" after exception - query :: "+Sql);
			PreparedStatement updateStatement = conn.prepareStatement(Sql);
			
			updateStatement.execute();
			updateStatement.close();
			
			throw e;
		}*/
		finally {
			
			if (callStmt != null) {
				callStmt.close();
			}
		}
		/*if(errorMsg!=null && !errorMsg.equals("")){
			logger.error("GridMasterFileUploadProcessor - END callUpdateProcedure() error from proecure Bulk_update_Non_RNE ::" + result);
		}*/
		
		/*logger.info("GridMasterFileUploadProcessor - END callUpdateProcedure() result ::" + result);
		
		if(result.equals("") || result.equals("0")){
			return false;
		}else{
			return true;
		}*/

		return true;

	}
}
