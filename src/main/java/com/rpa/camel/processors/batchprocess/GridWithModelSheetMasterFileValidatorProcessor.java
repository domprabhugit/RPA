package com.rpa.camel.processors.batchprocess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
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
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.monitorjbl.xlsx.StreamingReader;
import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.GridWithModelSheetAutomationModel;
import com.rpa.repository.TransactionInfoRepository;
import com.rpa.service.processors.GridWithModelSheetAutomationService;
import com.rpa.util.UtilityFile;

public class GridWithModelSheetMasterFileValidatorProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(GridWithModelSheetMasterFileValidatorProcessor.class.getName());

	@Autowired
	ApplicationContext applicationContext;
	@Autowired
	TransactionInfoRepository transactionInfoRepository;

	@Autowired
	GridWithModelSheetAutomationService gridAutomationService;

	private List<String> errorList = new ArrayList<String>(),errorSheetNameList = new ArrayList<String>();
	
	private String rolePlayerTableName = UtilityFile.getGridUploadProperty("Grid.RolePlayer.Table.Name"),
			partyGroupTableName = UtilityFile.getGridUploadProperty("Grid.PartyGroup.Table.Name");

	private void AutoWiringBeanPropertiesSetMethod() {

		logger.info("AutoWiringBeanPropertiesSetMethod is Called in GridWithModelSheetMasterFileValidatorProcessor Class");
		applicationContext = SpringContext.getAppContext();
		transactionInfoRepository = applicationContext.getBean(TransactionInfoRepository.class);
		gridAutomationService = applicationContext.getBean(GridWithModelSheetAutomationService.class);
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in GridWithModelSheetMasterFileValidatorProcessor Class");

	}

	private Boolean validationStatus = true, agentStatus = true, gridStatus = true;
	private int validationCount = 0, validationSuccesCount = 0;

	@Override
	public void process(Exchange exchange) throws Exception {

		GridWithModelSheetMasterFileValidatorProcessor fileValidatorProcessor = new GridWithModelSheetMasterFileValidatorProcessor();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty("TRANSACTION_INFO_REQ");
		fileValidatorProcessor.doProcess(transactionInfo, exchange);

	}

	private void doProcess(TransactionInfo transactionInfo, Exchange exchange) throws Exception {
		AutoWiringBeanPropertiesSetMethod();
		logger.info("BEGIN - GridWithModelSheetMasterFileValidatorProcessor---doProcess() Method Called Here");
		transactionInfo.setProcessPhase(RPAConstants.GRID_VALIDATOR_PHASE);
		transactionInfo.setTransactionStatus(RPAConstants.InProgress);
		AutoWiringBeanPropertiesSetMethod();
		List<GridWithModelSheetAutomationModel> automationFileList = gridAutomationService
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
		logger.info("BEGIN - GridWithModelSheetMasterFileValidatorProcessor---doProcess() Method Ended Here");

	}

	private void GridProcessFileValidatorMethod(TransactionInfo transactionInfo, List<GridWithModelSheetAutomationModel> automationFileList,Exchange exchange)
			throws Exception {
		validationStatus = true;
		if (!automationFileList.isEmpty()) {

			for (GridWithModelSheetAutomationModel grid : automationFileList) {
				if (grid.getIsValidated().equals(RPAConstants.N) ) {
					exchange.setProperty("grid_obj", grid);
				/*try{*/
					validationCount++;
					errorList.clear();
					errorSheetNameList.clear();
					grid.setFileType("GridWithModelProcess");
					try (
							  InputStream is = new FileInputStream(new File(grid.getFilePath()));
							  Workbook workbook = StreamingReader.builder()
							          .rowCacheSize(100)
							          .bufferSize(4096)
							          .open(is)) {
						logger.info("GridProcessFileValidatorMethod -- Sheet count --->"+workbook.getNumberOfSheets());

					int sheetCount = workbook.getNumberOfSheets();
					grid.setTotalSheetCount(String.valueOf(sheetCount));
					grid.setSuccessNo(String.valueOf(0));
					grid.setFileStatus("GridModelAndAgentValidation");
					
					int i = 0; String SheetName = "", GridSheetName="",remarksValue="", agentCode = "";
					
					String fileType = agentOrGroupCodeDecider(workbook.getSheetName(0),workbook);
					if(fileType.equals("")){
						throw new Exception("Invalid Grid Name - Expected "+RPAConstants.Model_Agent+" or "+RPAConstants.Agent_Region);
					}else{
						grid.setFileType(fileType);
						gridAutomationService.save(grid);
					}
					if(fileType.equals("GridWithModelAgentRegionProcess")){
					for (i = 0; i < sheetCount; i = i + 2) {
						grid.setValidateNo(String.valueOf(i));
						excelValidationProcessWithoutAgentCodes(workbook, i, grid,SheetName,GridSheetName,remarksValue,agentCode);
						if (agentStatus == false ||  gridStatus == false) {
							logger.info("GridProcessFileValidatorMethod - Validation failed ");
							grid.setIsValidated(RPAConstants.F);
							validationStatus = false;

							transactionInfo.setProcessStatus(RPAConstants.Failed);
							transactionInfo.setTransactionStatus(RPAConstants.Failed);
						}
						gridAutomationService.save(grid);
					}
					}else if(fileType.equals("GridWithModelAgentProcess") || fileType.equals("GridWithModelRegionProcess")){
						for (i = 0; i < sheetCount; i = i + 1) {
							grid.setValidateNo(String.valueOf(i));
							logger.info("Grid Sheet :: "+workbook.getSheetName(i)+" available for Grid ");
							if(i>0){
							List<String> columnNames = new ArrayList<String>();
							getGridDataWorkBookValidation(workbook.getSheetName(i), workbook, columnNames);
							}
							if (agentStatus == false ||  gridStatus == false) {
								logger.info("GridProcessFileValidatorMethod - Validation failed ");
								grid.setIsValidated(RPAConstants.F);
								validationStatus = false;

								transactionInfo.setProcessStatus(RPAConstants.Failed);
								transactionInfo.setTransactionStatus(RPAConstants.Failed);
							}
							gridAutomationService.save(grid);
						}
					}
					
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
										+ UtilityFile.getGridUploadProperty("GridModel.File.Location.BasePath"),
								new File(grid.getFilePath()),
								UtilityFile.getGridUploadProperty("GridModel.File.Processed.Path")
										+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY)
												.toString()
										+ UtilityFile.getGridUploadProperty("GridModel.File.Location.ValidatedPath"));

						grid.setFilePath(newFilePath);
						gridAutomationService.save(grid);
						transactionInfo.setProcessStatus(RPAConstants.Success);
					} else {
						grid.setErrorSheetList(String.join(RPAConstants.COMMA, errorList));
						gridAutomationService.save(grid);
					}
					
				}
					
				/*}catch(Exception e){
					logger.error(" Exception occured while validating "+ grid.getFileName()+" Grid id :: "+grid.getId()+" Excption is -- "+e);
					String newFilePath = UtilityFile.FileMovementMethod(
							UtilityFile.getCodeBasePath()
									+ UtilityFile.getGridUploadProperty("GridModel.File.Location.BasePath"),
							new File(grid.getFilePath()),
							UtilityFile.getGridUploadProperty("GridModel.File.Processed.Path")
									+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY)
											.toString()
									+ UtilityFile.getGridUploadProperty("GridModel.File.Location.ErrorPath"));
					grid.setFilePath(newFilePath);
					gridAutomationService.save(grid);
					throw e;
				}*/
			}
				
			}
		}
		
	}



	private Boolean getAgentDataWorkBookValidation( Workbook workbook, String sheetName,String agentCode) {
		 agentCode = "";
		agentStatus = true;
		if (sheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("GridModel.Agent.Sheet.Name")))
		{
		
		}else{
			agentStatus = false;
			errorSheetNameList.add(sheetName);
		}
		return agentStatus;

	}

	private List<GridWithModelSheetAutomationModel> getFilesFromBaseLocation(TransactionInfo transactionInfo)
			throws UnsupportedEncodingException, URISyntaxException {

		logger.info("Getting Files From Base Location Path Method Called Here");
		String FilePath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getGridUploadProperty("GridModel.File.Location.BasePath");
		logger.info(" Base Location Path   ::::  "+FilePath);
		File files = new File(FilePath);
		
		File[] directoryList = files.listFiles();
		int fileCount = 0;
		List<GridWithModelSheetAutomationModel> gridList = new ArrayList<GridWithModelSheetAutomationModel>();
		if (directoryList != null) {
			for (File file : directoryList) {
				if (!file.isDirectory()) {
					if (Arrays
							.asList(UtilityFile.getGridUploadProperty("Grid.File.Extension").split(RPAConstants.COMMA))
							.contains(FilenameUtils.getExtension(file.getName()))) {
						GridWithModelSheetAutomationModel gridAutoMationObject = new GridWithModelSheetAutomationModel();
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

	private Boolean excelValidationProcessWithoutAgentCodes(Workbook workbook, int SheetNo, GridWithModelSheetAutomationModel grid, String SheetName, String GridSheetName,String remarksValue,String agentCode)
			throws Exception {
		SheetName="";
		String gridName="";
		grid.setFileStatus("Model and GridValidation");
		
		gridName = workbook.getSheetName(SheetNo);
		if(!gridName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))){
			errorSheetNameList.add("Grid Sheet not available for Grid ::"+gridName);
			logger.error("Grid Sheet not available for Grid ::"+gridName);
			gridStatus = false;
		}else{
			if(SheetNo>0){
			logger.info("Grid Sheet :: "+gridName+" available for Grid ");
			List<String> columnNames = new ArrayList<String>();
			getGridDataWorkBookValidation(gridName, workbook, columnNames);
			}
		}
		
		
		if(workbook.getNumberOfSheets()>SheetNo+1){
			SheetName = workbook.getSheetName(SheetNo+1);
		}
		
		if(SheetName.equals("")){
			errorSheetNameList.add("Agent Sheet not available for Grid ::"+workbook.getSheetName(SheetNo));
			logger.error("Agent Sheet not available for Grid ::"+workbook.getSheetName(SheetNo));
			agentStatus = false;
		}else{
			logger.info("Agent Sheet :: "+SheetName+" available for Grid ::"+workbook.getSheetName(SheetNo));
			logger.info("Agent Validation For the Sheet ::::::" + SheetName);
		getAgentDataWorkBookValidation( workbook,SheetName,agentCode);
		}
		
		logger.info(" Excel File Validated and Modified   ::");
		return validationStatus;

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
	
	
	private String agentOrGroupCodeDecider(String gridSheetName, Workbook workbook
			) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		DataFormatter df = new DataFormatter();
		String gridType="",gridName="";
		List<String> columnNames = new ArrayList<>();
		
		if (gridSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("GridModel.BaseGrid.Sheet.Name")))

		{
			logger.info("EXCEl GRID DETAIlS Reading Method is Started Here");
			Sheet gridSheet = workbook.getSheet(gridSheetName);
			// GetColumn Names
			//Row columnRow = gridSheet.getRow(0);
			for (Row row : gridSheet) {
				if(row.getRowNum() == 0){
					for (Cell cell : row) {
						
						if (cell != null && (cell.getCellTypeEnum() != CellType.BLANK
								|| (cell.getCellTypeEnum() == CellType.STRING && !cell.getStringCellValue().isEmpty()))) {
							//columnNames.add(cell.getStringCellValue().toUpperCase());
							columnNames.add(Arrays.asList(new String[] {"PRODUCT","MODEL CODE","Y_AXIS","VALUE","GRID_ID","GRID_NAME","EFFECTIVE_START_DATE","EFFECTIVE_END_DATE","VERSION","TRANSACTIONTYPE","START_AGE","END_AGE","STATE","REMARKS" })
									.contains(cell.getStringCellValue().toUpperCase().trim())==true?cell.getStringCellValue().toUpperCase().trim(): "wrongcolumName");
						}

					}
					
					List<String> mandatoryHeaders = Arrays.asList(new String[] {"PRODUCT","MODEL CODE","Y_AXIS","VALUE","GRID_ID","GRID_NAME","EFFECTIVE_START_DATE","EFFECTIVE_END_DATE","VERSION","TRANSACTIONTYPE","START_AGE","END_AGE"});
					for (String header : mandatoryHeaders) {
						   if (!columnNames.contains(header)) {
							   gridStatus = false;
							   errorList.add(gridSheetName+"-"+" Mandatory Headers Not available");
						   }
						}
					
				}else if ( row != null && row.getRowNum() != 0 && row.getRowNum() <=1 ) {
					for (int i = 0; i < columnNames.size(); i++) {
						Cell cell = row.getCell(i);
						
						if (cell != null && (cell.getCellTypeEnum() != CellType.BLANK
								|| cell.getCellTypeEnum() == CellType.STRING
										&& !cell.getStringCellValue().isEmpty())) {
							
							if(columnNames.get(i).equals("Y_AXIS")){
								gridName = getGridName(df.formatCellValue(row.getCell(i)));
								if(gridName.equals(RPAConstants.Agent_Region)){
									gridType = "GridWithModelAgentRegionProcess";
								}else if(gridName.equals(RPAConstants.Model_Agent) ){
									gridType = "GridWithModelAgentProcess";
								}else if(gridName.equals(RPAConstants.Model_Region)){
									gridType = "GridWithModelRegionProcess";
								}
							}
							
							
						}
					}

				}else if (  row.getRowNum() > 2 ){
					break;
				}
			}
		}
		return gridType;

	}
	
	
	
	@SuppressWarnings("deprecation")
	private Workbook getGridDataWorkBookValidation(String gridSheetName, Workbook workbook, List<String> columnNames
) throws Exception {
		gridStatus = true;
		if (gridSheetName.toUpperCase().contains(UtilityFile.getGridUploadProperty("Grid.BaseGrid.Sheet.Name"))) {
		
			logger.info("EXCEl GRID DETAIlS Validating Method is Started Here");
			Sheet gridSheet = workbook.getSheet(gridSheetName);
			// GetColumn Names
			//Row columnRow = gridSheet.getRow(0);
			int c = 0;
			
			//Row firstRow = gridSheet.rowIterator().next();
			
			for (Row row : gridSheet) {
				if(c==0){
					c++;
			if(row==null){
				logger.info("getGridDataWorkBookValidation :: "+gridSheetName+"-"+" Headers Not available in First Row " );
				gridStatus = false;
				errorList.add(gridSheetName+"-"+" Headers Not available in First Row " );
				return workbook;
			}else{
			for (Cell cell : row) {
				/*if (cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK
						|| (cell.getCellType() == Cell.CELL_TYPE_STRING && !cell.getStringCellValue().isEmpty()))) {*/
					
					if (cell != null && (cell.getCellTypeEnum() != CellType.BLANK
							|| (cell.getCellTypeEnum() == CellType.STRING && !cell.getStringCellValue().isEmpty()))) {
					
					columnNames.add(Arrays.asList(new String[] {"PRODUCT","MODEL CODE","Y_AXIS","VALUE","GRID_ID","GRID_NAME","EFFECTIVE_START_DATE","EFFECTIVE_END_DATE","VERSION","TRANSACTIONTYPE","START_AGE","END_AGE","STATE","REMARKS" })
							.contains(cell.getStringCellValue().toUpperCase().trim())==true?cell.getStringCellValue().toUpperCase().trim(): "wrongcolumName");
				}

			}	
			
			List<String> mandatoryHeaders = Arrays.asList(new String[] {"PRODUCT","MODEL CODE","Y_AXIS","VALUE","GRID_ID","GRID_NAME","EFFECTIVE_START_DATE","EFFECTIVE_END_DATE","VERSION","TRANSACTIONTYPE","START_AGE","END_AGE"});
			for (String header : mandatoryHeaders) {
				   if (!columnNames.contains(header)) {
					   gridStatus = false;
					   errorList.add(gridSheetName+"-"+" Mandatory Headers Not available");
					   return workbook;
				   }
				}
				if(!columnNames.contains("wrongcolumName") &&  columnNames.size()>=12)
				{	
				
			}
		
			
			else
				{
				gridStatus = false;
				   errorList.add(gridSheetName+"-"+" ColumnNames Not in the Template Format  " );
				   return workbook;
				}
		}
				}else{
					break;
				}
			}
		}
		if (!gridStatus)
			errorList.add(gridSheetName);
		
		
		return workbook;
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
}
