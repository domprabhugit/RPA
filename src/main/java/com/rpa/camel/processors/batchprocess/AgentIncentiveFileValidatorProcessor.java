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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.IndexedColors;
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
import com.rpa.model.processors.AgentIncentiveModel;
import com.rpa.model.processors.GridAutomationModel;
import com.rpa.repository.TransactionInfoRepository;
import com.rpa.service.processors.AgentIncentiveService;
import com.rpa.service.processors.GridAutomationService;
import com.rpa.util.UtilityFile;

public class AgentIncentiveFileValidatorProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(AgentIncentiveFileValidatorProcessor.class.getName());

	@Autowired
	ApplicationContext applicationContext;
	@Autowired
	TransactionInfoRepository transactionInfoRepository;

	@Autowired
	AgentIncentiveService agentIncentiveService;

	private List<String> errorList = new ArrayList<String>();

	@SuppressWarnings("unused")
	private void AutoWiringBeanPropertiesSetMethod() {

		logger.info("AutoWiringBeanPropertiesSetMethod is Called in AgentIncentiveFileValidatorProcessor Class");
		applicationContext = SpringContext.getAppContext();
		transactionInfoRepository = applicationContext.getBean(TransactionInfoRepository.class);
		agentIncentiveService = applicationContext.getBean(AgentIncentiveService.class);
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in AgentIncentiveFileValidatorProcessor Class");

	}

	private Boolean  xGenStatus = true, d2cStatus = true, crmStatus = true;

	@Override
	public void process(Exchange exchange) throws Exception {

		AgentIncentiveFileValidatorProcessor fileValidatorProcessor = new AgentIncentiveFileValidatorProcessor();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty("TRANSACTION_INFO_REQ");
		fileValidatorProcessor.doProcess(transactionInfo, exchange);

	}

	private void doProcess(TransactionInfo transactionInfo, Exchange exchange) throws Exception {
		AutoWiringBeanPropertiesSetMethod();
		logger.info("BEGIN - AgentIncentiveFileValidatorProcessor---doProcess() Method Called Here");
		transactionInfo.setProcessPhase("FILE_VALIDATOR");
		//transactionInfo.setProcessStatus(RPAConstants.VALIDATION);
		transactionInfo.setTransactionStatus(RPAConstants.InProgress);
		AutoWiringBeanPropertiesSetMethod();
		
		AgentIncentiveModel agentIncentiveModel =  new AgentIncentiveModel();
		//agentIncentiveModel.setMonthYear("04/2019");
		agentIncentiveModel.setIsXgenFileAvailable("N");
		agentIncentiveModel.setIsDtwocFileAvailable("N");
		agentIncentiveModel.setIsCrmFileAvailable("N");
		agentIncentiveModel.setTransactionRefNo(transactionInfo.getId());
		agentIncentiveService.save(agentIncentiveModel);
		logger.info(" AgentIncentiveFileValidatorProcessor Agenbt incentive object created ::"+agentIncentiveModel);
		
		getFilesFromBaseLocation(transactionInfo,agentIncentiveModel);
		agentIncentiveFileValidatorMethod(transactionInfo, agentIncentiveModel,exchange);

		/*transactionInfo.setTotalRecords(String.valueOf(validationCount));
		transactionInfo.setTotalSuccessRecords(String.valueOf(validationSuccesCount));
		transactionInfo.setTotalErrorRecords(String.valueOf(validationCount - validationSuccesCount));*/
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		logger.info("BEGIN - AgentIncentiveFileValidatorProcessor---doProcess() Method Ended Here");

	}

	private void agentIncentiveFileValidatorMethod(TransactionInfo transactionInfo,AgentIncentiveModel  agentIncentiveModel, Exchange exchange)
			throws Exception {
		List<Map<String, String>> d2cDataList = new ArrayList<>(); 
		List<Map<String, String>> xgenDataList = new ArrayList<>(); 
		List<Map<String, String>> crmDataList = new ArrayList<>(); 
		List<String> policyList = new ArrayList<>();
		List<String> columnNames = new ArrayList<>();
		boolean skipThisRow = false;
		DataFormatter df = new DataFormatter();
		errorList.clear();
		if (agentIncentiveModel!=null) {
			/*if(agentIncentiveModel.getIsDtwocFileAvailable().equals("Y")){
				try (
				InputStream is = new FileInputStream(new File(agentIncentiveModel.getD2cFilePath()));
						Workbook workbook = StreamingReader.builder()
				          .rowCacheSize(100)
				          .bufferSize(4096)
				          .open(is)) {
					String sheetName = workbook.getSheetName(0);
					Sheet gridSheet = workbook.getSheet(sheetName);
				    int c = 0;
					for (Row row : gridSheet) {
						if(c==0){
							columnNames.clear();
							c++;
					if(row==null){
						logger.info("agentIncentiveFileValidatorMethod :: D2C File - "+sheetName+"-"+" Headers Not available in First Row " );
						d2cStatus = false;
						errorList.add("D2C File -"+sheetName+"-"+" Headers Not available in First Row " );
					}else{
					for (Cell cell : row) {
						if (cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK
								|| (cell.getCellType() == Cell.CELL_TYPE_STRING && !cell.getStringCellValue().isEmpty()))) {
							
							if (cell != null && (cell.getCellTypeEnum() != CellType.BLANK
									|| (cell.getCellTypeEnum() == CellType.STRING && !cell.getStringCellValue().isEmpty()))) {
							
							columnNames.add(Arrays.asList(new String[] {"QUOTE_ID","POLICYCODE","CUSTOMER NAME","MOBILE","BUY_DATE" })
									.contains(cell.getStringCellValue().toUpperCase().trim())==true?cell.getStringCellValue().toUpperCase().trim(): "wrongcolumName");
						}

					}	
					
					List<String> mandatoryHeaders = Arrays.asList(new String[] {"QUOTE_ID","POLICYCODE","CUSTOMER NAME","MOBILE","BUY_DATE"});
					for (String header : mandatoryHeaders) {
						   if (!columnNames.contains(header)) {
							   d2cStatus = false;
							   errorList.add(sheetName+"-"+" Mandatory Headers Not available");
							   break;
						   }
						}
						
				}
					
					
					
						}else{
							if(d2cStatus){
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
										d2cDataList.add(map);

								}
						}
					}
					
				}
				
				
				          }
			}else{
				throw new Exception("D2C File Not available in Path");
			}*/
			
			
			
			if(agentIncentiveModel.getIsCrmFileAvailable().equals("Y")){
				try (
				InputStream is = new FileInputStream(new File(agentIncentiveModel.getCrmFilePath()));
						Workbook workbook = StreamingReader.builder()
				          .rowCacheSize(100)
				          .bufferSize(4096)
				          .open(is)) {
					int sheetCount = workbook.getNumberOfSheets();
					int k =0;
					for (k = 0; k < sheetCount; k = k + 1) {
					String sheetName = workbook.getSheetName(k);
					Sheet gridSheet = workbook.getSheet(sheetName);
				    int c = 0;
					for (Row row : gridSheet) {
						if(c==0){
							columnNames.clear();
							c++;
					if(row==null){
						logger.info("agentIncentiveFileValidatorMethod :: CRM File - "+sheetName+"-"+" Headers Not available in First Row " );
						crmStatus = false;
						errorList.add("CRM File -"+sheetName+"-"+" Headers Not available in First Row " );
					}else{
					for (Cell cell : row) {
						/*if (cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK
								|| (cell.getCellType() == Cell.CELL_TYPE_STRING && !cell.getStringCellValue().isEmpty()))) {*/
							
							if (cell != null && (cell.getCellTypeEnum() != CellType.BLANK
									|| (cell.getCellTypeEnum() == CellType.STRING && !cell.getStringCellValue().isEmpty()))) {
							
							columnNames.add(Arrays.asList(new String[] {"AGENT NAME","CUSTOMER NAME","POLICY NO","TEAM","PROCESS TL","DATE","LEAD SOURCE" })
									.contains(cell.getStringCellValue().toUpperCase().trim())==true?cell.getStringCellValue().toUpperCase().trim(): "wrongcolumName");
						}

					}	
					
					List<String> mandatoryHeaders = Arrays.asList(new String[] {"AGENT NAME","CUSTOMER NAME","POLICY NO","TEAM","PROCESS TL","DATE","LEAD SOURCE"});
					for (String header : mandatoryHeaders) {
						   if (!columnNames.contains(header)) {
							   crmStatus = false;
							   errorList.add("CRM File -"+sheetName+"-"+" Mandatory Headers Not available");
							   logger.info("agentIncentiveFileValidatorMethod :: CRM File - in sheet -- "+sheetName+" header - "+header+" Not available " );
							   break;
						   }
						}
				}
					
					
					
						}else{
							if(crmStatus){
								if (row.getRowNum() != 0 && row != null) {
									Map<String, String> map = new HashMap<>();
									for (int i = 0; i < columnNames.size(); i++) {
										Cell cell = row.getCell(i);
										
										if (cell != null && (cell.getCellTypeEnum() != CellType.BLANK
												|| cell.getCellTypeEnum() == CellType.STRING
														&& !cell.getStringCellValue().isEmpty())) {
											map.put(columnNames.get(i), df.formatCellValue(row.getCell(i)));
											if(columnNames.get(i).equals("POLICY NO")){
												policyList.add(df.formatCellValue(row.getCell(i)));
											}

										}
									}
									if (!map.isEmpty())
										crmDataList.add(map);

								}
						}else{
							   logger.info("agentIncentiveFileValidatorMethod :: CRM File Failed the validation " );
						}
					}
					
				}
					}
			}
			}else{
				throw new Exception("CRM File Not available in Path");
			}
			
			if(agentIncentiveModel.getIsXgenFileAvailable().equals("Y")){
				try (
				InputStream is = new FileInputStream(new File(agentIncentiveModel.getxGenFilePath()));
						Workbook workbook = StreamingReader.builder()
				          .rowCacheSize(100)
				          .bufferSize(4096)
				          .open(is)) {
					//String sheetName = workbook.getSheetName(0);
					int sheetCount = workbook.getNumberOfSheets();
					int k =0;
					for (k = 0; k < sheetCount; k = k + 1) {
					String sheetName = workbook.getSheetName(k);
					Sheet gridSheet = workbook.getSheet(sheetName);
				    int c = 0;
					for (Row row : gridSheet) {
						if(c==0){
							columnNames.clear();
							c++;
					if(row==null){
						logger.info("agentIncentiveFileValidatorMethod :: XGEN File - "+sheetName+"-"+" Headers Not available in First Row " );
						xGenStatus = false;
						errorList.add("XGEN File -"+sheetName+"-"+" Headers Not available in First Row " );
					}else{
					for (Cell cell : row) {
						/*if (cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK
								|| (cell.getCellType() == Cell.CELL_TYPE_STRING && !cell.getStringCellValue().isEmpty()))) {*/
							
							if (cell != null && (cell.getCellTypeEnum() != CellType.BLANK
									|| (cell.getCellTypeEnum() == CellType.STRING && !cell.getStringCellValue().isEmpty()))) {
							
							columnNames.add(Arrays.asList(new String[] {"POLICY NO.","OUR SHARE OF PREMIUM","TPPREMIUM","POLICY_COUNT","PRODUCT","ENTRY DATE" })
									.contains(cell.getStringCellValue().toUpperCase().trim())==true?cell.getStringCellValue().toUpperCase().trim(): "wrongcolumName");
						}

					}	
					
					List<String> mandatoryHeaders = Arrays.asList(new String[] {"POLICY NO.","OUR SHARE OF PREMIUM","TPPREMIUM","POLICY_COUNT","PRODUCT","ENTRY DATE"});
					for (String header : mandatoryHeaders) {
						   if (!columnNames.contains(header)) {
							   xGenStatus = false;
							   errorList.add("XGEN File -"+sheetName+"-"+" Mandatory Headers Not available");
							   logger.info("agentIncentiveFileValidatorMethod :: XGEN File - in sheet -- "+sheetName+" header - "+header+" Not available " );
							   break;
						   }
						}
				}
					
					
					
						}else{
							if(xGenStatus){
								if (row.getRowNum() != 0 && row != null) {
									skipThisRow = false;
									Map<String, String> map = new HashMap<>();
									for (int i = 0; i < columnNames.size(); i++) {
										Cell cell = row.getCell(i);
										
										if (cell != null && (cell.getCellTypeEnum() != CellType.BLANK
												|| cell.getCellTypeEnum() == CellType.STRING
														&& !cell.getStringCellValue().isEmpty())) {
											map.put(columnNames.get(i), df.formatCellValue(row.getCell(i)));
											
											/*if(columnNames.get(i).equals("POLICY NO.")){
												if(!policyList.contains(df.formatCellValue(row.getCell(i)))){
													skipThisRow = true;	
													break;
												}
											}*/
										}
									}
									if (!map.isEmpty() && skipThisRow==false){
										xgenDataList.add(map);
									}

								}
						}else{
							   logger.info("agentIncentiveFileValidatorMethod :: XGEN File Failed the validation " );
						}
					}
					
				}
			  }
			}
			}else{
				throw new Exception("XGEN File Not available in Path");
			}
		}


		if(!errorList.isEmpty())
		{
			agentIncentiveModel.setIsValidationSucceeded(RPAConstants.N);
			agentIncentiveModel.setErrorValidationList(String.join(RPAConstants.COMMA, errorList));
			
		}else{
			agentIncentiveModel.setIsValidationSucceeded(RPAConstants.Y);
			exchange.setProperty("incentive_xgenList", xgenDataList);
			 logger.info("agentIncentiveFileValidatorMethod XGEN Policy record size :: "+xgenDataList.size() );
			exchange.setProperty("incentive_d2cList", d2cDataList);
			exchange.setProperty("incentive_crmList", crmDataList);
			logger.info("agentIncentiveFileValidatorMethod CRM Policy record size :: "+crmDataList.size() );
		}
		exchange.setProperty("incentive_obj", agentIncentiveModel);
		agentIncentiveService.save(agentIncentiveModel);
		
			
	}

	


	private void getFilesFromBaseLocation(TransactionInfo transactionInfo,AgentIncentiveModel agentIncentiveModel)
			throws UnsupportedEncodingException, URISyntaxException {

		logger.info("Getting Files From Base Location Path Method Called Here");
		String FilePath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getAgentProperty("Agent.File.Location.BasePath");
		logger.info(" Base Location Path   ::::  "+FilePath);
		File files = new File(FilePath);
		
		File[] directoryList = files.listFiles();
		int fileCount = 0;
		if (directoryList != null) {
			for (File file : directoryList) {
				if (!file.isDirectory()) {
					if (Arrays
							.asList(UtilityFile.getAgentProperty("Agent.File.Extension").split(RPAConstants.COMMA))
							.contains(FilenameUtils.getExtension(file.getName()))) {
						/*if(file.getName().toUpperCase().contains("D2C")){
							agentIncentiveModel.setD2cFilePath(file.getAbsolutePath());
							logger.info(" getFilesFromBaseLocation  :: "+agentIncentiveModel);
							agentIncentiveModel.setIsDtwocFileAvailable("Y");
						}else*/ if(file.getName().toUpperCase().contains("XGEN")){
							logger.info(" getFilesFromBaseLocation  XGEN file is available with file name :: "+file.getName());
							agentIncentiveModel.setxGenFilePath(file.getAbsolutePath());
							agentIncentiveModel.setIsXgenFileAvailable("Y");
						}else if(file.getName().toUpperCase().contains("CRM")){
							logger.info(" getFilesFromBaseLocation  CRM file is available with file name :: "+file.getName());
							agentIncentiveModel.setCrmFilePath(file.getAbsolutePath());
							agentIncentiveModel.setIsCrmFileAvailable("Y");
						}
						
						agentIncentiveService.save(agentIncentiveModel);
						fileCount++;
					}
				}
			}
			transactionInfo.setProcessStatus("FileReadingFromLocation");
			transactionInfo.setTotalRecords(String.valueOf(fileCount));
			transactionInfoRepository.save(transactionInfo);
		}
		logger.info("Getting Files From Base Location Path Method ended Here");

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
