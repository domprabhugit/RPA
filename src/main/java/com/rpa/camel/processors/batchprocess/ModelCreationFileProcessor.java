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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.ModelCodeCreation;
import com.rpa.repository.TransactionInfoRepository;
import com.rpa.service.processors.ModelCodeCreationService;
import com.rpa.util.UtilityFile;

public class ModelCreationFileProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(ModelCreationFileProcessor.class.getName());

	@Autowired
	ApplicationContext applicationContext;
	@Autowired
	TransactionInfoRepository transactionInfoRepository;

	@Autowired
	ModelCodeCreationService modelCodeCreationService;

	private String vehicleModelTable = UtilityFile.getGridUploadProperty("modelcreation.vehiclemodel.Table.Name");
	private String vehicleModelSublineTable = UtilityFile
			.getGridUploadProperty("modelcreation.vehiclemodelsubline.Table.Name");
	private String vehicleModelIDVDiscountTable = UtilityFile
			.getGridUploadProperty("modelcreation.vehicleidvdiscount.Table.Name");
	private String vehicleModelIDVTable = UtilityFile.getGridUploadProperty("modelcreation.vehicleidv.Table.Name");
	private String rolePlayerTableName = UtilityFile.getGridUploadProperty("Grid.RolePlayer.Table.Name"),
			partyGroupTableName = UtilityFile.getGridUploadProperty("Grid.PartyGroup.Table.Name");
	private String ModelCodeTableName = UtilityFile.getGridUploadProperty("modelcreation.Grid.Model.Table.Name"),
			baseGridTableName = UtilityFile.getGridUploadProperty("modelcreation.Grid.BaseGrid.Table.Name"),
			gridTempTableName = UtilityFile.getGridUploadProperty("modelcreation.Grid.TempGrid.Table.Name"),
			gridUploadMasterTableName = UtilityFile.getGridUploadProperty("modelcreation.Grid.MainGrid.Table.Name");

	private List<String> errorList = new ArrayList<String>(), errorSheetNameList = new ArrayList<String>();
	List<Map<String, String>> finalmapList = new ArrayList<Map<String, String>>();
	int remarksColumnNo = 0, uploadCount = 0, uploadSuccesCount = 0;
	// private Map<String,String > sheetCountDetails=new LinkedHashMap<>();

	private void AutoWiringBeanPropertiesSetMethod() {

		logger.info("AutoWiringBeanPropertiesSetMethod is Called in ModelCreationFileProcessor Class");
		applicationContext = SpringContext.getAppContext();
		transactionInfoRepository = applicationContext.getBean(TransactionInfoRepository.class);
		modelCodeCreationService = applicationContext.getBean(ModelCodeCreationService.class);
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in ModelCreationFileProcessor Class");

	}

	private Boolean gridStatus = true, modelValidationStatus = true, invalidHeaderGroup = false,
			isHeaderValidatedForThisRow = false,sheetStatus = true;
	private int validationCount = 0, validationSuccesCount = 0, count = 0;

	@Override
	public void process(Exchange exchange) throws Exception {

		ModelCreationFileProcessor fileValidatorProcessor = new ModelCreationFileProcessor();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty("TRANSACTION_INFO_REQ");
		fileValidatorProcessor.doProcess(transactionInfo, exchange);

	}

	private void doProcess(TransactionInfo transactionInfo, Exchange exchange) throws Exception {
		AutoWiringBeanPropertiesSetMethod();
		logger.info("BEGIN - ModelCreationFileProcessor---doProcess() Method Called Here");
		transactionInfo.setProcessPhase(RPAConstants.MODEL_VALIDATOR_PHASE);
		transactionInfo.setTransactionStatus(RPAConstants.InProgress);
		AutoWiringBeanPropertiesSetMethod();

		List<ModelCodeCreation> automationFileList = modelCodeCreationService
				.FindValidateFilesList(RPAConstants.Completed);
		if (!automationFileList.isEmpty()) {
			ModelCreationGridFileProcessMethod(transactionInfo, automationFileList);
			automationFileList.clear();
		}
		automationFileList = getFilesFromBaseLocation(transactionInfo);
		validationCount = automationFileList.size();
		uploadCount = automationFileList.size();
		if (!automationFileList.isEmpty()) {
			ModelCreationGridFileProcessMethod(transactionInfo, automationFileList);
		} else {
			transactionInfo.setTransactionStatus("No File");
		}

		// transactionInfo.setProcessStatus(RPAConstants.VALIDATION);
		transactionInfo.setTotalRecords(String.valueOf(validationCount));
		transactionInfo.setTotalSuccessRecords(String.valueOf(validationSuccesCount));
		transactionInfo.setTotalErrorRecords(String.valueOf(validationCount - validationSuccesCount));
		transactionInfo.setTotalUploadRecords(String.valueOf(uploadCount));
		transactionInfo.setTotalSuccessUploads(String.valueOf(uploadSuccesCount));
		transactionInfo.setTotalErrorUploads(String.valueOf(uploadCount - uploadSuccesCount));
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		logger.info("ModelCreationFileProcessor---doProcess() Method Ended Here");

	}

	private void insertGridModelDetails() throws Exception {
		logger.info("BEGIN - ModelCreationFileProcessor---insertGridModelDetails() Method Called Here");
		Connection connection = UtilityFile.getGridDbConnection();
		tableDeleteMethod(connection, getListOfTablesToTruncate(RPAConstants.MC_GRID_NON_MODEL_Tables));
		Boolean status = insertBasicGridDetails(connection, finalmapList);
		if (status) {
			createTempGridValuesWithoutAgentCodes(connection);
		}
		gridUploadMasterMethod(connection);
		// sheetCountDetails.put(SheetName+"-"+GridSheetName,
		// modalCodes.size()+"*"+FinalmapList.size()+"="+modalCodes.size()*FinalmapList.size());
		// totalRecordsCount=totalRecordsCount+(modalCodes.size()*FinalmapList.size());
		tableDeleteMethod(connection, getListOfTablesToTruncate(RPAConstants.MC_GRID_Truncate_Table));
		connection.close();
		logger.info("ModelCreationFileProcessor---insertGridModelDetails() Method Ends Here");
	}

	private void ModelCreationGridFileProcessMethod(TransactionInfo transactionInfo,
			List<ModelCodeCreation> modelFileList) throws Exception {
		logger.info("BEGIN - ModelCreationFileProcessor---ModelCreationGridFileProcessMethod() Method Called Here");
		List<Map<String, String>> listMap = new ArrayList<>();
		if (!modelFileList.isEmpty()) {

			for (ModelCodeCreation modelCodeCreation : modelFileList) {
				gridStatus = true;
				modelValidationStatus = true;
				// validationCount++;
				errorList.clear();
				errorSheetNameList.clear();
				Workbook workbook = UtilityFile.workBookCreation(modelCodeCreation.getFilePath());
				modelCodeCreation.setSuccessNo(String.valueOf(0));

				int SheetIndex = 0;
				modelCodeCreation.setFileType("ModelCode");
				modelCodeCreation.setFileStatus("ModelCodeValidation");
				workbook = modelCodeValidation(workbook, SheetIndex, listMap, modelCodeCreation);
				FileOutputStream out = new FileOutputStream(modelCodeCreation.getFilePath());
				workbook.write(out);
				out.close();

				logger.info(" finalmapList size :: " + finalmapList.size());

				if (modelValidationStatus == false || gridStatus == false) {
					if (!modelValidationStatus)
						modelCodeCreation.setIsModelValidated(RPAConstants.F);
					if (!gridStatus)
						modelCodeCreation.setIsGridValidated(RPAConstants.F);
					transactionInfo.setProcessStatus(RPAConstants.Failed);
					String newFilePath = UtilityFile.FileMovementMethod(
							UtilityFile.getCodeBasePath()
									+ UtilityFile.getGridUploadProperty("modelcreation.File.Location.BasePath"),
							new File(modelCodeCreation.getFilePath()),
							UtilityFile.getGridUploadProperty("modelcreation.File.Processed.Path")
									+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY)
											.toString()
									+ UtilityFile.getGridUploadProperty("modelcreation.File.Location.ErrorPath"));
					modelCodeCreation.setFilePath(newFilePath);
					modelCodeCreation.setEndDate(new Date());
				} else {
					transactionInfo.setProcessPhase(RPAConstants.GRID_UPLOADER_PHASE);
					validationSuccesCount++;
					insertModelValues(listMap, modelCodeCreation);

					String newFilePath = UtilityFile.FileMovementMethod(
							UtilityFile.getCodeBasePath()
									+ UtilityFile.getGridUploadProperty("modelcreation.File.Location.BasePath"),
							new File(modelCodeCreation.getFilePath()),
							UtilityFile.getGridUploadProperty("modelcreation.File.Processed.Path")
									+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY)
											.toString()
									+ UtilityFile.getGridUploadProperty("modelcreation.File.Location.ValidatedPath"));
					modelCodeCreation.setFilePath(newFilePath);
					modelCodeCreation.setEndDate(new Date());
					insertGridModelDetails();
					uploadSuccesCount++;
					modelCodeCreation.setSuccessNo(modelCodeCreation.getValidateNo());
					modelCodeCreation.setIsProcessed(RPAConstants.Y);
				}
				modelCodeCreation.setErrorSheetList(String.join(RPAConstants.COMMA, errorList));

				modelCodeCreationService.save(modelCodeCreation);

				// }

			}
		}
		logger.info("ModelCreationFileProcessor---ModelCreationGridFileProcessMethod() Method Ends Here");
	}

	@SuppressWarnings("deprecation")
	private Workbook modelCodeValidation(Workbook workbook, int sheetIndex, List<Map<String, String>> listMap,
			ModelCodeCreation modelCodeCreation) throws Exception {
		CellStyle cellstyle = null;
		List<String> currentSheetModelcolumnNames = new ArrayList<>();
		DataFormatter df = new DataFormatter();

		boolean proceedFurther = true;
		String currentColumnName = "";
		boolean ismodeValidationCompleted = false, isModelCodeGenerated = false, isAtleaseOneModelRowAvailable = false;
		List<String> missingHeaders = new ArrayList<>();
		List<String> modelProductList = new ArrayList<>();
		List<String> modelProductNameList = new ArrayList<>();
		;
		List<Map<String, String>> validatorList = new ArrayList<Map<String, String>>();
		validatorList = getProductListFromProperties(validatorList);
		List<String> gridTotalProducts = new ArrayList<String>(validatorList.get(0).keySet());
		List<String> gridTotalProductsNames = new ArrayList<String>(validatorList.get(0).values());
		List<String> columnHeaders = new LinkedList<>(Arrays
				.asList(new String[] { "PRODUCT", "Y_AXIS", "VALUE", "GRID_ID", "GRID_NAME", "EFFECTIVE_START_DATE",
						"EFFECTIVE_END_DATE", "VERSION", "TRANSACTIONTYPE", "START_AGE", "END_AGE","STATE" }));
		int modelHeaderRow = 0;
		String errorRemarks = "";
		Connection conn = UtilityFile.getGridDbConnection();

		int sheetCount = workbook.getNumberOfSheets();
		modelCodeCreation.setTotalSheetCount(String.valueOf(sheetCount));

		for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
			 sheetStatus = true;
			modelCodeCreation.setValidateNo(String.valueOf(s));
			String SheetName = workbook.getSheetName(s);
			/* if (SheetName.toUpperCase().contains("MODEL")) { */

			Sheet agentSheet = workbook.getSheet(SheetName);
			for (Row row : agentSheet) {
				if (!ismodeValidationCompleted) {
					if (!checkIfRowIsEmpty(row) && s == 0) {
						errorRemarks = "";

						Map<String, String> map = new HashMap<>();
						if (modelHeaderRow == 0) {
							cellstyle = row.getCell(row.getFirstCellNum()).getCellStyle();
							creatCell(row, row.getPhysicalNumberOfCells(), cellstyle, RPAConstants.Remarks);
							for (Cell cell : row) {

								if (cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK
										|| (cell.getCellType() == Cell.CELL_TYPE_STRING
												&& !cell.getStringCellValue().isEmpty()))) {
									currentSheetModelcolumnNames.add(cell.toString().toUpperCase().trim());
								} else {
									currentSheetModelcolumnNames.add("");
								}
								
								modelHeaderRow++;
							}

							if(containsCaseInsensitive(("Product"),currentSheetModelcolumnNames)){
								if (!containsCaseInsensitive("Make", currentSheetModelcolumnNames))
									missingHeaders.add("Make");
								if (!exactMatch("Model", currentSheetModelcolumnNames) && !exactMatch("Model Name", currentSheetModelcolumnNames))
									missingHeaders.add("Model");
								if (!containsCaseInsensitive("Effective start date", currentSheetModelcolumnNames)
										&& !containsCaseInsensitive("Effective date from", currentSheetModelcolumnNames)
										&& !containsCaseInsensitive("Effective date", currentSheetModelcolumnNames)
										&& !containsCaseInsensitive("Start Date", currentSheetModelcolumnNames))
									missingHeaders.add("Effective start date");
								/*if (!containsCaseInsensitive("Class Code", currentSheetModelcolumnNames))
									missingHeaders.add("Class Code");*/
	
								if (missingHeaders.size() > 0) {
									modelValidationStatus = false;
									String headers = "";
									for (String header : missingHeaders) {
										headers += header + ",";
									}
	
									headers = headers.replaceAll(",$", "");
									errorList.add(SheetName + "-" + " Missing headers : " + headers);
									return workbook;
								}
							}else{
								if(row.getRowNum()<=5){
									currentSheetModelcolumnNames.clear();
									modelHeaderRow = 0;
								}
								else
									throw new Exception("Model code Mandatory Headers Not available in the sheet"); 
							}
						} else {
							int totColumns = row.getPhysicalNumberOfCells();
							for (int i = 0; i < row.getLastCellNum(); i++) {
								Cell currentCell = row.getCell(i);

								if (currentSheetModelcolumnNames.get(i).equalsIgnoreCase("Product")) {

									if (gridTotalProducts.contains(currentCell.getStringCellValue())) {
										proceedFurther = true;
									} else {
										proceedFurther = false;
									}

								}
								if (proceedFurther) {
									isAtleaseOneModelRowAvailable = true;
									currentColumnName = currentSheetModelcolumnNames.get(i).trim();
									if (currentColumnName.equalsIgnoreCase("Product")) {
										map.put("Product", currentCell.getStringCellValue());
										modelProductList.add(currentCell.getStringCellValue());
										modelProductNameList
												.add(validatorList.get(0).get(currentCell.getStringCellValue()));
										if (currentCell.getStringCellValue().equalsIgnoreCase("VPC")) {
											map.put("prefix", "CMH");
											map.put("SUBLINES", "privatePassengerCar");
											map.put("ENGINE_CAPACITY_UNIT", "CC");

										} else if (currentCell.getStringCellValue().equalsIgnoreCase("VPCV")) {
											map.put("prefix", "PV");
											map.put("SUBLINES", "VPCV");
											map.put("ENGINE_CAPACITY_UNIT", "CC");
										} else if (currentCell.getStringCellValue().equalsIgnoreCase("VGC")) {
											map.put("prefix", "GC");
											map.put("SUBLINES", "commercialVehicle");
											map.put("ENGINE_CAPACITY_UNIT", "TONNS");
										} else if (currentCell.getStringCellValue().equalsIgnoreCase("VOC")) {
											map.put("prefix", "MCW");
											map.put("SUBLINES", "VOC");
											map.put("ENGINE_CAPACITY_UNIT", "HP");
										} else if (currentCell.getStringCellValue().equalsIgnoreCase("VMC")) {
											map.put("prefix", "ZWTV");
											map.put("SUBLINES", "motorCycle");
											map.put("ENGINE_CAPACITY_UNIT", "CC");
										} else {
											errorRemarks += "Invalid Product,";
										}

										if (!isModelCodeGenerated) {
											map.put("MODEL_CODE", generateModelCode(map.get("prefix")));
										}

									} else if (currentColumnName.equalsIgnoreCase("Fuel")) {
										map.put("FUEL_TYPE", currentCell.getStringCellValue());
									} else if (currentColumnName.equalsIgnoreCase("Type of body")
											|| currentColumnName.equalsIgnoreCase("Body Type")) {
										map.put("BODY_STYLE", currentCell.getStringCellValue());
									} else if (currentColumnName.equalsIgnoreCase("SC in Xgen & Emotor")
											|| currentColumnName.equalsIgnoreCase("SC")) {
										if (currentCell.getCellType() == Cell.CELL_TYPE_STRING)
											map.put("SEATING_CAPACITY",
													String.valueOf(currentCell.getStringCellValue()));
										else
											map.put("SEATING_CAPACITY",
													String.valueOf(currentCell.getNumericCellValue()));
									} else if (currentColumnName.equalsIgnoreCase("Make")) {
										map.put("MAKE", currentCell.getStringCellValue());
										map.put("MAKEID", getMakeId(currentCell.getStringCellValue()));
									} else if (currentColumnName.equalsIgnoreCase("Model Name")) {
										map.put("MODEL_NAME", currentCell.getStringCellValue());
									} else if (currentColumnName.equalsIgnoreCase("Effective start date")
											|| currentColumnName.equalsIgnoreCase("Effective date from")
											|| currentColumnName.equalsIgnoreCase("Start Date")) {
										if (checkModelCreationDateFormat(df.formatCellValue(currentCell),
												RPAConstants.mm_slash_dd_slash_yyyy)) {
											map.put("MODEL_YEAR",
													UtilityFile.dateToSting(currentCell.getDateCellValue(), "yyyy"));
											map.put("EFFECTIVE_START_DATE",
													String.valueOf(
															UtilityFile.dateToSting(currentCell.getDateCellValue(),
																	RPAConstants.dd_slash_MM_slash_yyyy)));
										} else {
											errorRemarks += "Invalid Date format -" + currentColumnName + ",";
										}
									} else if (currentColumnName.equalsIgnoreCase("Effective date")) {
										if (checkModelCreationDateFormat(df.formatCellValue(currentCell),
												RPAConstants.mm_slash_dd_slash_yyyy)) {
											map.put("MODEL_YEAR",
													UtilityFile.dateToSting(currentCell.getDateCellValue(), "yyyy"));
											map.put("EFFECTIVE_START_DATE",
													String.valueOf(
															UtilityFile.dateToSting(currentCell.getDateCellValue(),
																	RPAConstants.dd_slash_MM_slash_yyyy)));
										} else {
											errorRemarks += "Invalid Date format -" + currentColumnName + ",";
										}
									} else if (currentColumnName.equalsIgnoreCase("Effective end date")) {
										if (checkModelCreationDateFormat(df.formatCellValue(currentCell),
												RPAConstants.mm_slash_dd_slash_yyyy)) {
											map.put("EFFECTIVE_END_DATE",
													String.valueOf(
															UtilityFile.dateToSting(currentCell.getDateCellValue(),
																	RPAConstants.dd_slash_MM_slash_yyyy)));
										} else {
											errorRemarks += "Invalid Date format -" + currentColumnName + ",";
										}
									} else if (currentColumnName.equalsIgnoreCase("Class Code")) {
										map.put("VEHICLE_CLASS", currentCell.getStringCellValue());
									} else if (currentColumnName.equalsIgnoreCase("GVW")) {
										if (map.get("ENGINE_CAPACITY_UNIT") != null
												&& map.get("ENGINE_CAPACITY_UNIT").equalsIgnoreCase("TONNS")) {
											if (currentCell.getCellType() == Cell.CELL_TYPE_STRING)
												map.put("VEHICLEWEIGHT",
														String.valueOf(currentCell.getStringCellValue()));
											else
												map.put("VEHICLEWEIGHT",
														String.valueOf(currentCell.getNumericCellValue()));
										}
									} else if (currentColumnName.equalsIgnoreCase("cc")) {
										if (map.get("ENGINE_CAPACITY_UNIT") != null
												&& map.get("ENGINE_CAPACITY_UNIT").equalsIgnoreCase("cc")) {
											if (currentCell.getCellType() == Cell.CELL_TYPE_STRING)
												map.put("ENGINE_CAPACITY_AMOUNT",
														String.valueOf(currentCell.getStringCellValue()));
											else
												map.put("ENGINE_CAPACITY_AMOUNT",
														String.valueOf(currentCell.getNumericCellValue()));
										}
									} else if (currentColumnName.equalsIgnoreCase("hp")) {
										if (map.get("ENGINE_CAPACITY_UNIT") != null
												&& map.get("ENGINE_CAPACITY_UNIT").equalsIgnoreCase("hp")) {
											if (currentCell.getCellType() == Cell.CELL_TYPE_STRING)
												map.put("ENGINE_CAPACITY_AMOUNT",
														String.valueOf(currentCell.getStringCellValue()));
											else
												map.put("ENGINE_CAPACITY_AMOUNT",
														String.valueOf(currentCell.getNumericCellValue()));
										}
									} else if (currentColumnName.equalsIgnoreCase("Min")) {
										if (currentCell.getCellType() == Cell.CELL_TYPE_STRING)
											map.put("MIN_SEATINGCAPACITY",
													String.valueOf(currentCell.getStringCellValue()));
										else
											map.put("MIN_SEATINGCAPACITY",
													String.valueOf(currentCell.getNumericCellValue()));
									} else if (currentColumnName.equalsIgnoreCase("Max")) {
										if (currentCell.getCellType() == Cell.CELL_TYPE_STRING)
											map.put("MAX_SEATINGCAPACITY",
													String.valueOf(currentCell.getStringCellValue()));
										else
											map.put("MAX_SEATINGCAPACITY",
													String.valueOf(currentCell.getNumericCellValue()));
									} else if (currentColumnName.equalsIgnoreCase("LIP")) {
										if (currentCell.getCellType() == Cell.CELL_TYPE_STRING)
											map.put("LIP", String.valueOf(currentCell.getStringCellValue()));
										else
											map.put("LIP", String.valueOf(currentCell.getNumericCellValue()));
									}

									/* set default values if null */
									map = setDefaultValues(map);

									if (errorRemarks != "")
										modelValidationStatus = false;

								} else {
									ismodeValidationCompleted = true;
									break;
								}
							}
							if (!errorRemarks.equalsIgnoreCase(""))
								creatCell(row, currentSheetModelcolumnNames.size() - 1, cellstyle, errorRemarks);
						}
						if (map.size() > 0)
							listMap.add(map);
					} else {
						logger.info("modelCodeValidation() - Skipped Empty Row ");
					}
				} else {
					// break;
					if (!checkIfRowIsEmpty(row))
						finalmapList = getGridDataWorkBookValidation(SheetName, row, modelProductList,
								gridTotalProducts, finalmapList, columnHeaders, conn, gridTotalProductsNames,
								modelProductNameList);
				}
			}

			if (!isAtleaseOneModelRowAvailable) {
				errorList.add(SheetName + "-" + " No Model Rows available");
				modelValidationStatus = false;

			}
			/* } */
		}

		if (conn != null)
			conn.close();

		return workbook;
	}

	@SuppressWarnings({ "deprecation", "static-access" })
	private List<Map<String, String>> getGridDataWorkBookValidation(String sheetName, Row columnRow,
			List<String> modalproductList, List<String> gridTotalProducts, List<Map<String, String>> finalmapList,
			List<String> columnNames, Connection conn, List<String> gridTotalProductsNames,
			List<String> modelProductNameList) throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException {
		DataFormatter df = new DataFormatter();
		// gridStatus = true;
		
		if(columnNames.size()==0)
			columnNames = new LinkedList<>(Arrays
					.asList(new String[] { "PRODUCT", "Y_AXIS", "VALUE", "GRID_ID", "GRID_NAME", "EFFECTIVE_START_DATE",
							"EFFECTIVE_END_DATE", "VERSION", "TRANSACTIONTYPE", "START_AGE", "END_AGE","STATE" }));

		String remarksValue = "";
		boolean processThisRow = true,skipThisRow=false;
		isHeaderValidatedForThisRow = false;
		

		logger.info("EXCEl model-GRID DETAIlS Validating Method is Started Here");
		if (columnRow == null) {

		} else {

			String Grid_Name = "";

			int currentRowColumnLength = 11;

			CellStyle cellstyle = columnRow.getCell(columnRow.getFirstCellNum()).getCellStyle();

			for (int i = 0; i <= currentRowColumnLength; i++) {

				if(!skipThisRow){
				Cell cell = columnRow.getCell(i);
				remarksValue = "";

				if (cell != null && (cell.getCellType() != Cell.CELL_TYPE_BLANK
						|| (cell.getCellType() == Cell.CELL_TYPE_STRING && !cell.getStringCellValue().isEmpty()))) {
					if (Arrays.asList(new String[] { "ID", "PRODUCT", "Y_AXIS", "VALUE", "GRID_ID", "GRID_NAME",
							"EFFECTIVE_START_DATE", "EFFECTIVE_END_DATE", "VERSION", "TRANSACTIONTYPE", "START_AGE",
							"END_AGE", "STATE" }).contains(columnNames.get(i))) {
						if (cell.getCellType() == Cell.CELL_TYPE_STRING
								&& (Arrays
										.asList(new String[] { "ID", "PRODUCT", "Y_AXIS", "VALUE", "GRID_ID",
												"GRID_NAME", "EFFECTIVE_START_DATE", "EFFECTIVE_END_DATE", "VERSION",
												"TRANSACTIONTYPE", "START_AGE", "END_AGE", "STATE" })
										.contains(cell.getStringCellValue()))
								&& isHeaderValidatedForThisRow == false) {
							invalidHeaderGroup = false;
							isHeaderValidatedForThisRow = true;
							remarksColumnNo = columnRow.getPhysicalNumberOfCells();
							columnNames.clear();
							for (Cell headercell : columnRow) {
								if (headercell != null && (headercell.getCellType() != headercell.CELL_TYPE_BLANK
										|| (headercell.getCellType() == headercell.CELL_TYPE_STRING
												&& !headercell.getStringCellValue().isEmpty()))) {
									columnNames.add(Arrays
											.asList(new String[] { "ID", "PRODUCT", "X_AXIS", "Y_AXIS", "VALUE",
													"GRID_ID", "GRID_NAME", "EFFECTIVE_START_DATE",
													"EFFECTIVE_END_DATE", "VERSION", "TRANSACTIONTYPE", "START_AGE",
													"END_AGE", "STATE", "REMARKS" })
											.contains(headercell.toString().toUpperCase().trim()) == true
													? headercell.toString().toUpperCase().trim() : "wrongcolumName");
								}

							}
							currentRowColumnLength = columnNames.size()-1;
							if (columnNames.contains("wrongcolumName")) {
								gridStatus = sheetStatus = false;
								errorList.add(sheetName + "-" + " ColumnNames Not in the Template Format in Row :: "
										+ String.valueOf(columnRow.getRowNum()));
								invalidHeaderGroup = true;
								skipThisRow = true;
								columnNames.clear();
							}
							List<String> mandatoryHeaders = Arrays.asList(new String[] { "PRODUCT", "Y_AXIS", "VALUE",
									"GRID_ID", "GRID_NAME", "EFFECTIVE_START_DATE", "EFFECTIVE_END_DATE", "VERSION",
									"TRANSACTIONTYPE", "START_AGE", "END_AGE" });
							for (String header : mandatoryHeaders) {
								if (!columnNames.contains(header)) {
									gridStatus = sheetStatus = false;
									errorList.add(sheetName + "-" + " Mandatory Headers Not available in Row :: "
											+ String.valueOf(columnRow.getRowNum()));
									invalidHeaderGroup = true;
									break;
								}
							}

							if (!columnNames.contains(RPAConstants.Remarks)) {
								creatCell(columnRow, remarksColumnNo, cellstyle, RPAConstants.Remarks);
							} else {
								remarksColumnNo = remarksColumnNo - 1;
							}

						}
						if (!invalidHeaderGroup) {
							if(columnNames.get(i).equals("PRODUCT")){
							if (cell.getCellType() != Cell.CELL_TYPE_STRING
									|| cell.getStringCellValue().equalsIgnoreCase("")
									|| cell.getStringCellValue().equalsIgnoreCase("PRODUCT")
									|| (!gridTotalProducts.contains(cell.getStringCellValue())
											&& !gridTotalProductsNames.contains(cell.getStringCellValue()))) {
								processThisRow = false;
								break;
							} else if (modalproductList.contains(cell.getStringCellValue())
									|| modelProductNameList.contains(cell.getStringCellValue())) {
								cell.setCellValue(cell.getStringCellValue());
							} else {
								remarksValue = " Invalid Product ";
							}
							}
						}
					}
					if (!invalidHeaderGroup) {
						if (columnNames.get(i).equals("PRODUCT") && cell.getStringCellValue().equalsIgnoreCase("")) {
							processThisRow = false;
							break;
						}
						if (columnNames.get(i).equals("Y_AXIS")) {
							/* if (columnRow.getRowNum() == 1) { */
							Grid_Name = getGridName(cell.getStringCellValue(), conn);
							/* } */

							if (cell.getStringCellValue().indexOf("_") != -1) {
								String[] yaxis = cell.getStringCellValue().split("_");
								cell.setCellValue(yaxis[0].trim() + "_" + yaxis[1].trim());
							} else {
								cell.setCellValue(cell.getStringCellValue());
							}
						}
						if (columnNames.get(i).equals("GRID_NAME")) {
							if (Grid_Name.isEmpty()) {
								// remarksValue = " Not Available in the
								// DATABASE ";
								remarksValue = "";
							} else {
								cell.setCellValue(Grid_Name);
							}
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
								remarksValue = checkModelCreationDateFormat(df.formatCellValue(cell),
										RPAConstants.GRID_DATE_FORMAT) == false ? "invalid date Format" : "";
							}

						}

						if (columnNames.get(i).equalsIgnoreCase("TRANSACTIONTYPE")) {
							remarksValue = Arrays.asList(new String[] { "NB", "RN", "RNE" })
									.contains(cell.toString()) == false ? "invalid TransactionType" : "";
						}
					}
				} else if (cell != null && (cell.getCellType() == Cell.CELL_TYPE_BLANK
						|| (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().isEmpty()))) {
					if (!invalidHeaderGroup) {
						if (columnNames.get(i).equals("PRODUCT") && cell.getStringCellValue().equalsIgnoreCase("")) {
							processThisRow = false;
							break;
						}
						if (columnNames.get(i).equals("GRID_NAME")) {
							if (Grid_Name.isEmpty()) {
								remarksValue = " Not Available in the DATABASE ";
							} else {
								cell.setCellValue(Grid_Name);
							}
						}
						if (!(columnNames.get(i).equals("EFFECTIVE_END_DATE") || columnNames.get(i).equals("STATE")
								|| columnNames.get(i).equals("GRID_NAME") || columnNames.get(i).equals("PRODUCT")))

							remarksValue = RPAConstants.Empty;

					}
				}

				if (!remarksValue.equalsIgnoreCase("")) {
					gridStatus = sheetStatus = false;
					logger.info("Row No:::::  " + (Integer.valueOf(columnRow.getRowNum()) + 1) + "::ColumnName===="
							+ columnNames.get(i) + "has   " + remarksValue + "");

					creatCell(columnRow, remarksColumnNo, columnRow.getCell(columnRow.getFirstCellNum()).getCellStyle(),
							columnNames.get(i) + " has  " + remarksValue + "  ");
				}
			}
			}

			if (gridStatus == true && processThisRow == true && invalidHeaderGroup == false) {
				Map<String, String> rowmap = new HashMap<>();
				for (int k = 0; k <= 12; k++) {
					Cell currentCell = columnRow.getCell(k);

					if (currentCell != null && (currentCell.getCellType() != Cell.CELL_TYPE_BLANK
							|| currentCell.getCellType() == currentCell.CELL_TYPE_STRING
									&& !currentCell.getStringCellValue().isEmpty())) {
						rowmap.put(columnNames.get(k), df.formatCellValue(columnRow.getCell(k)));
					}
				}
				if (!rowmap.isEmpty())
					finalmapList.add(rowmap);

			}

			if (!gridStatus)
				finalmapList.clear();

			count++;
			logger.info("Grid Details Count::" + count +" Row number :: "+columnRow.getRowNum());

			/*if(columnRow.getRowNum()==7600){
				logger.info("iteration before error ");
			}*/
		}
		if (sheetStatus == false && !errorList.contains(sheetName))
			errorList.add(sheetName);
		if (count == 0 && !errorSheetNameList.contains(sheetName))
			errorSheetNameList.add(sheetName);

		return finalmapList;
	}

	private Map<String, String> setDefaultValues(Map<String, String> map) {

		if (map.get("FUEL_TYPE") == null)
			map.put("FUEL_TYPE", "diesel");
		if (map.get("BODY_STYLE") == null)
			map.put("BODY_STYLE", "open");
		if (map.get("SEATING_CAPACITY") == null)
			map.put("SEATING_CAPACITY", String.valueOf("2"));
		if (map.get("MIN_SEATINGCAPACITY") == null)
			map.put("MIN_SEATINGCAPACITY", map.get("SEATING_CAPACITY"));
		if (map.get("MAX_SEATINGCAPACITY") == null)
			map.put("MAX_SEATINGCAPACITY", map.get("SEATING_CAPACITY"));
		if (map.get("VEHICLEWEIGHT") == null)
			map.put("VEHICLEWEIGHT", String.valueOf(0));
		if (map.get("ENGINE_CAPACITY_AMOUNT") == null)
			map.put("ENGINE_CAPACITY_AMOUNT", String.valueOf(0));
		if (map.get("LIP") == null)
			map.put("LIP", String.valueOf(0));

		return map;
	}

	private String generateModelCode(String prefix) throws SQLException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, URISyntaxException {
		logger.info("BEGIN - ModelCreationFileProcessor---generateModelCode() Method Called Here");
		Connection connection = UtilityFile.getGridDbConnection();

		int prefixLength = prefix.length() + 1;

		Integer newModeCode = 0;
		String sql = "select * from (select SUBSTR(model_code, " + prefixLength
				+ ", length(model_code))+1  from vehiclemodel where model_code like ('%" + prefix
				+ "%') order by SUBSTR(model_code, " + prefixLength + ", length(model_code)) desc) where rownum=1";
		PreparedStatement statement = connection.prepareStatement(sql);
		ResultSet rs = statement.executeQuery();
		while (rs.next()) {
			newModeCode = rs.getInt(1);
		}
		if (rs != null)
			rs.close();
		if (statement != null)
			statement.close();
		if (connection != null)
			connection.close();

		String modeCode = prefix + String.valueOf(newModeCode);
		logger.info("ModelCreationFileProcessor---generateModelCode() Method Ends Here - modeCode "+modeCode);
		return modeCode;

	}

	private List<ModelCodeCreation> getFilesFromBaseLocation(TransactionInfo transactionInfo)
			throws UnsupportedEncodingException, URISyntaxException {

		logger.info("ModelCodecCreation - Getting Files From Base Location Path Method Called Here");
		String FilePath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getGridUploadProperty("modelcreation.File.Location.BasePath");
		logger.info(" Base Location Path   ::::  " + FilePath);
		File files = new File(FilePath);

		File[] directoryList = files.listFiles();
		int fileCount = 0;
		List<ModelCodeCreation> gridList = new ArrayList<ModelCodeCreation>();
		if (directoryList != null) {
			for (File file : directoryList) {
				if (!file.isDirectory()) {
					if (Arrays
							.asList(UtilityFile.getGridUploadProperty("Grid.File.Extension").split(RPAConstants.COMMA))
							.contains(FilenameUtils.getExtension(file.getName()))) {
						ModelCodeCreation gridAutoMationObject = new ModelCodeCreation();
						gridAutoMationObject.setStartDate(transactionInfo.getTransactionStartDate());
						gridAutoMationObject.setFileName(file.getName());
						gridAutoMationObject.setFilePath(file.getAbsolutePath());
						gridAutoMationObject.setIsModelCreated(RPAConstants.N);
						gridAutoMationObject.setIsProcessed(RPAConstants.N);
						gridAutoMationObject.setTransactionInfoId(transactionInfo.getId());
						gridAutoMationObject.setIsGridValidated(RPAConstants.N);
						modelCodeCreationService.save(gridAutoMationObject);
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

	public static boolean checkModelCreationDateFormat(String dateValue, String dateFormat) {
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

	private boolean checkIfRowIsEmpty(Row row) {
		if (row == null) {
			return true;
		}
		if (row.getLastCellNum() <= 0) {
			return true;
		}
		for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
			Cell cell = row.getCell(cellNum);
			if (cell != null && cell.getCellTypeEnum() != CellType.BLANK && StringUtils.isNotBlank(cell.toString())) {
				return false;
			}
		}
		return true;
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

	public String getMakeId(String make) throws InstantiationException, IllegalAccessException, ClassNotFoundException,
			SQLException, URISyntaxException {
		logger.info("BEGIN - ModelCreationFileProcessor---getMakeId() Method Called Here");
		Connection connection = UtilityFile.getGridDbConnection();
		Integer makeId = 0;
		String sql = "select distinct makeid from  " + vehicleModelTable + " where make= upper('" + make + "')";
		PreparedStatement statement = connection.prepareStatement(sql);
		ResultSet rs = statement.executeQuery();
		while (rs.next()) {
			makeId = rs.getInt(1);
		}
		if (rs != null)
			rs.close();
		if (statement != null)
			statement.close();
		if (connection != null)
			connection.close();
		
		logger.info(" ModelCreationFileProcessor---getMakeId() Method Ends Here - makeId :: "+makeId);
		return String.valueOf(makeId);
	}

	public boolean insertModelValues(List<Map<String, String>> listMap, ModelCodeCreation modelCodeCreation)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException,
			URISyntaxException, NumberFormatException, ParseException {
		Connection connection = UtilityFile.getGridDbConnection();
		logger.info("BEGIN - ModelCreationFileProcessor---insertModelValues() Method Called Here");
		for (Map<String, String> map : listMap) {
			if (modelCodeCreation.getIsModelCreated().equalsIgnoreCase(RPAConstants.N)) {
				Integer nextId = getNextId(connection, vehicleModelTable);
				if (vehicleModelInsert(map, connection, nextId)) {
					Integer nextSublineId = getNextId(connection, vehicleModelSublineTable);
					vehicleModelSublineInsert(map, connection, nextId, nextSublineId);
					Integer nextIdvDiscounteId = getNextId(connection, vehicleModelIDVDiscountTable);
					vehicleModelIDVDiscountInsert(map, connection, nextIdvDiscounteId);
					Integer nextIdvId = getNextId(connection, vehicleModelIDVTable);
					vehicleModelIDVInsert(map, connection, nextIdvId);
					modelCodeCreation.setIsModelCreated(RPAConstants.Y);
				}
			} else {
				logger.info("Already model code created & inserted");
			}

			String sql = "truncate  table  RPA_MC_MODELCODE ";
			PreparedStatement statementTruncate = connection.prepareStatement(sql);
			statementTruncate.execute();
			statementTruncate.close();

			PreparedStatement statement = connection.prepareStatement("insert into  RPA_MC_MODELCODE values(?,?)");
			logger.info("Base Grid Table Name::: RPA_MC_MODELCODE ");
			statement.setString(1, map.get("MODEL_CODE"));
			statement.setString(2, map.get("Product"));

			statement.execute();
			statement.close();
		}

		if (connection != null)
			connection.close();

		logger.info("BEGIN - ModelCreationFileProcessor---insertModelValues() Method Ends Here");
		return true;

	}

	private Integer getNextId(Connection connection, String tableName) throws SQLException {
		Integer nextId = 0;
		String sql1 = "select nvl(max(id),0)+1 from " + tableName;
		PreparedStatement statement1 = connection.prepareStatement(sql1);
		ResultSet rs1 = statement1.executeQuery();
		while (rs1.next()) {
			nextId = rs1.getInt(1);
		}
		if (rs1 != null)
			rs1.close();
		if (statement1 != null)
			statement1.close();

		return nextId;
	}

	private void vehicleModelIDVDiscountInsert(Map<String, String> map, Connection connection,
			Integer nextIdvDiscounteId) throws SQLException, ParseException {
		logger.info("BEGIN - ModelCreationFileProcessor---vehicleModelIDVDiscountInsert() Method Called Here");
		List<String> ageList = getListData("ModelIDVAge");
		List<String> discountList = getListData("ModelIDVDiscount");
		Date effectiveStartDate = null, effectiveEndDate = null;
		SimpleDateFormat originalFormat = new SimpleDateFormat("d/m/yyy");
		if (map.get("EFFECTIVE_START_DATE") != null)
			effectiveStartDate = originalFormat.parse(map.get("EFFECTIVE_START_DATE"));
		if (map.get("EFFECTIVE_END_DATE") != null)
			effectiveEndDate = originalFormat.parse(map.get("EFFECTIVE_END_DATE"));

		PreparedStatement statement = connection
				.prepareStatement("insert into  " + vehicleModelIDVDiscountTable + " values(?,?,?,?,?,?,?,?)");
		logger.info("vehicleModelIDVDiscountInsert Table Name:::" + vehicleModelIDVDiscountTable);

		for (int i = 0; i < 7; i++) {

			/* Id */
			statement.setInt(1, nextIdvDiscounteId);
			/* vehicle model */
			statement.setString(2, map.get("MODEL_CODE"));
			/* vehicle make */
			statement.setString(3, map.get("MAKE"));
			/* age */
			statement.setInt(4, Integer.valueOf(ageList.get(i)));
			/* EFFECTIVE_START_DATE */
			if (effectiveStartDate != null)
				statement.setDate(5, new java.sql.Date(effectiveStartDate.getTime()));
			else
				statement.setDate(5, null);
			/* EFFECTIVE_END_DATE */
			if (effectiveEndDate != null)
				statement.setDate(6, new java.sql.Date(effectiveEndDate.getTime()));
			else
				statement.setDate(6, null);
			/* discount */
			statement.setFloat(7, Float.valueOf(discountList.get(i).replace(",", "")));
			/* Product */
			statement.setString(8, map.get("SUBLINES"));

			statement.execute();
			nextIdvDiscounteId++;
		}
		statement.close();
		
		logger.info("ModelCreationFileProcessor---vehicleModelIDVDiscountInsert() Method Ends Here");
	}

	private void vehicleModelIDVInsert(Map<String, String> map, Connection connection, Integer nextIdvId)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException,
			URISyntaxException, ParseException {
		logger.info("BEGIN - ModelCreationFileProcessor---vehicleModelIDVInsert() Method Called Here");
		List<String> regionList = getListData("ModelIDVRegion");
		Date effectiveStartDate = null, effectiveEndDate = null;
		SimpleDateFormat originalFormat = new SimpleDateFormat("d/m/yyy");
		if (map.get("EFFECTIVE_START_DATE") != null)
			effectiveStartDate = originalFormat.parse(map.get("EFFECTIVE_START_DATE"));
		if (map.get("EFFECTIVE_START_DATE") != null)
			effectiveEndDate = originalFormat.parse(map.get("EFFECTIVE_START_DATE"));

		PreparedStatement statement = connection
				.prepareStatement("insert into  " + vehicleModelIDVTable + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		logger.info("vehicleModelIDVInsert Table Name:::" + vehicleModelIDVTable);
		for (int i = 0; i < 6; i++) {

			/* Id */
			statement.setInt(1, nextIdvId);
			/* vehicle model */
			statement.setString(2, map.get("MODEL_CODE"));
			/* vehicle make */
			statement.setString(3, map.get("MAKE"));
			/* region */
			statement.setString(4, regionList.get(i));
			/* CITY */
			statement.setString(5, null);
			/* RTA */
			statement.setString(6, null);
			/* AC */
			statement.setString(7, null);
			/* FUEL */
			statement.setString(8, null);
			/* EFFECTIVE_START_DATE */
			if (effectiveStartDate != null)
				statement.setDate(9, new java.sql.Date(effectiveStartDate.getTime()));
			else
				statement.setString(9, null);
			/* EFFECTIVE_END_DATE */
			if (effectiveEndDate != null)
				statement.setDate(10, new java.sql.Date(effectiveEndDate.getTime()));
			else
				statement.setString(10, null);
			/* LIP */
			if (map.get("LIP") != null)
				statement.setFloat(11, Float.valueOf(map.get("LIP").replace(",", "")));
			else
				statement.setFloat(11, 0);
			/* Subline */
			statement.setString(12, map.get("SUBLINES"));
			/* CHASSISVALUE */
			if (map.get("LIP") != null)
				statement.setFloat(13, Float.valueOf(map.get("LIP").replace(",", "")));
			/* BODYVALUE */
			statement.setFloat(14, 0);
			/* LAST_UPDATED_ON */
			statement.setDate(15, new java.sql.Date(new Date().getTime()));
			/* UPLOADID */
			statement.setString(16, null);

			statement.execute();
			nextIdvId++;
		}
		statement.close();
		logger.info("ModelCreationFileProcessor---vehicleModelIDVInsert() Method Ends Here");
	}

	private void vehicleModelSublineInsert(Map<String, String> map, Connection connection, Integer parentId,
			Integer nextSublineId) throws InstantiationException, IllegalAccessException, ClassNotFoundException,
			SQLException, URISyntaxException {
		logger.info("BEGIN - ModelCreationFileProcessor---vehicleModelSublineInsert() Method Called Here");
		PreparedStatement statement = connection
				.prepareStatement("insert into  " + vehicleModelSublineTable + " values(?,?,?,?)");
		logger.info("vehicleModelSublineInsert Table Name:::" + vehicleModelSublineTable);

		/* Id */
		statement.setInt(1, nextSublineId);
		/* Sublines */
		statement.setString(2, map.get("SUBLINES"));
		/* COMPULSORYDEDUCTIBLE */
		statement.setFloat(3, 0);
		/* Id */
		statement.setInt(4, parentId);
		statement.execute();
		statement.close();
		
		logger.info("ModelCreationFileProcessor---vehicleModelSublineInsert() Method Ends Here");
	}

	private boolean vehicleModelInsert(Map<String, String> map, Connection connection, Integer nextId)
			throws NumberFormatException, SQLException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, URISyntaxException, ParseException {
		logger.info("BEGIN - ModelCreationFileProcessor---vehicleModelInsert() Method called Here");
		PreparedStatement statement = connection.prepareStatement("insert into  " + vehicleModelTable
				+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		logger.info("vehicleModel Table Name:::" + vehicleModelTable);
		Date effectiveStartDate = null, effectiveEndDate = null;
		SimpleDateFormat originalFormat = new SimpleDateFormat("d/m/yyy");
		if (map.get("EFFECTIVE_START_DATE") != null)
			effectiveStartDate = originalFormat.parse(map.get("EFFECTIVE_START_DATE"));
		if (map.get("EFFECTIVE_START_DATE") != null)
			effectiveEndDate = originalFormat.parse(map.get("EFFECTIVE_START_DATE"));

		/* ticket Id */
		statement.setInt(1, nextId);
		/* Insurance Rating Group */
		statement.setString(2, null);
		/* Sublines */
		statement.setString(3, map.get("SUBLINES"));
		/* Fuel Type */
		statement.setString(4, map.get("FUEL_TYPE"));
		/* BODY_STYLE */
		statement.setString(5, map.get("BODY_STYLE"));
		/* SEATING_CAPACITY */
		statement.setInt(6, (int) Double.parseDouble(map.get("SEATING_CAPACITY")));
		/* DESCRIPTION */
		statement.setString(7, null);
		/* MAKEID */
		statement.setInt(8, Integer.valueOf(map.get("MAKEID")));
		/* MAKE */
		statement.setString(9, map.get("MAKE"));
		/* MODEL_CODE */
		statement.setString(10, map.get("MODEL_CODE"));
		/* MODEL_NAME */
		statement.setString(11, map.get("MODEL_NAME"));
		/* MODEL_YEAR */
		statement.setString(12, map.get("MODEL_YEAR"));
		/* OBSOLETE_YEARS */
		statement.setInt(13, 5);
		/* LEGACY_CODE */
		statement.setString(14, map.get("MODEL_CODE"));
		/* STATUS */
		statement.setString(15, map.get(""));
		/* BUSINESS_STATUS */
		statement.setString(16, map.get("active"));
		/* EFFECTIVE_START_DATE */
		if (effectiveStartDate != null)
			statement.setDate(17, new java.sql.Date(effectiveStartDate.getTime()));
		else
			statement.setDate(17, null);
		/* EFFECTIVE_END_DATE */
		if (effectiveEndDate != null)
			statement.setDate(18, new java.sql.Date(effectiveEndDate.getTime()));
		else
			statement.setDate(18, null);
		/* CREATED_BY */
		statement.setString(19, null);
		/* CREATED_DATE */
		statement.setDate(20, null);
		/* LAST_MODIFIED_DATE */
		statement.setDate(21, null);
		/* LAST_MODIFIED_BY */
		statement.setString(22, null);
		/* VEHICLE_CLASS */
		statement.setString(23, map.get("VEHICLE_CLASS"));
		/* SAFETYFEATURES */
		statement.setString(24, null);
		/* VINTAGECar */
		statement.setInt(25, 0);
		/* VEHICLEWEIGHT */
		statement.setFloat(26, Float.valueOf(map.get("VEHICLEWEIGHT").replace(",", "")));
		/* MINIMUMPRICE */
		statement.setFloat(27, 0);
		/* ENGINE_CAPACITY_AMOUNT */
		statement.setFloat(28, Float.valueOf(map.get("ENGINE_CAPACITY_AMOUNT").replace(",", "")));
		/* ENGINE_CAPACITY_UNIT */
		statement.setString(29, map.get("ENGINE_CAPACITY_UNIT"));
		/* VEHICLECLASSDESCRIPTION */
		statement.setString(30, null);
		/* HIGH_END_MODEL */
		statement.setString(31, null);
		/* MIN_SEATINGCAPACITY */
		statement.setInt(32, (int) Double.parseDouble(map.get("MIN_SEATINGCAPACITY")));
		/* MAX_SEATINGCAPACITY */
		statement.setInt(33, (int) Double.parseDouble(map.get("MAX_SEATINGCAPACITY")));
		/* ADD_ON_COVERAGE_VALUE */
		statement.setString(34, null);
		/* UPLOADID */
		statement.setString(35, null);

		statement.execute();
		statement.close();
		logger.info("ModelCreationFileProcessor---vehicleModelInsert() Method Ends Here");
		return true;
	}

	public List<String> getListData(String paramName) {

		List<String> modelList = new ArrayList<String>();
		if (paramName.equalsIgnoreCase("ModelIDVRegion")) {
			modelList.add("East Region");
			modelList.add("North Region");
			modelList.add("NorthRegion");
			modelList.add("South Region");
			modelList.add("West Region");
			modelList.add("WestRegion");
		} else if (paramName.equalsIgnoreCase("ModelIDVAge")) {
			modelList.add("-1");
			modelList.add("0");
			modelList.add("1");
			modelList.add("2");
			modelList.add("3");
			modelList.add("4");
			modelList.add("5");
		} else if (paramName.equalsIgnoreCase("ModelIDVDiscount")) {
			modelList.add("5");
			modelList.add("20");
			modelList.add("20");
			modelList.add("20");
			modelList.add("30");
			modelList.add("40");
			modelList.add("50");
		}
		return modelList;
	}

	public boolean containsCaseInsensitive(String s, List<String> l) {
		return l.stream().anyMatch(x -> x.equalsIgnoreCase(s));
	}
	
	public boolean exactMatch(String s, List<String> l) {
		boolean isMatched = false;
		for(String c : l){
			if(c.equalsIgnoreCase(s))
				isMatched = true;
				
		}
		return isMatched;
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
			} else {
				cell.setCellValue(value);
			}
		}
		if (cellstyle != null) {

			cellstyle.setFillBackgroundColor(IndexedColors.ROSE.getIndex());
			cell.setCellStyle(cellstyle);
		}
		return cell;
	}

	private String getGridName(String Product, Connection connection) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("BEGIN - ModelCreationFileProcessor---getGridName() Method called Here");
		/* Connection connection = UtilityFile.getGridDbConnection(); */
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
			if (rs != null)
				rs.close();
			if (statement != null)
				statement.close();
			if (!cnt.equals("0")) {
				grid_name = RPAConstants.Agent_Region;
			} else {
				String sql1 = "select count(*) from " + partyGroupTableName + " where PARTYGROUPCODE='"
						+ grid_names[0].trim() + "'";
				PreparedStatement statement1 = connection.prepareStatement(sql1);
				ResultSet rs1 = statement1.executeQuery();
				while (rs1.next()) {
					cnt = rs1.getString(1);
				}
				if (rs1 != null)
					rs1.close();
				if (statement1 != null)
					statement1.close();
				if (!cnt.equals("0")) {
					grid_name = RPAConstants.Model_Agent;
				}

			}

		} else {
			grid_name = RPAConstants.Model_Region;
		}

		// connection.close();
		return grid_name;

	}

	private Boolean insertBasicGridDetails(Connection conn, List<Map<String, String>> finalmapList)
			throws Exception {
		logger.info("BEGIN - ModelCreationFileProcessor---insertBasicGridDetails() Method called Here");

		Boolean status = false;
		for (Map<String, String> map : finalmapList) {
			//SimpleDateFormat originalFormat = new SimpleDateFormat("MMMM/dd/yyyy hh:mm:ss");
			Date effectiveStartDate = UtilityFile.parseAnyDateFormat(map.get("EFFECTIVE_START_DATE")),effectiveEndDate=null;
					//originalFormat.parse(map.get("EFFECTIVE_START_DATE"));
			String effectiveEndDateMap = map.get("EFFECTIVE_END_DATE");

			if (effectiveEndDateMap != null && !effectiveEndDateMap.equals("") && effectiveEndDateMap.length() >= 10) {
				logger.info(" start date length ::" + map.get("EFFECTIVE_END_DATE").length());

				//effectiveEndDate = originalFormat.parse(effectiveEndDateMap);
				effectiveEndDate = UtilityFile.parseAnyDateFormat(map.get("EFFECTIVE_END_DATE"));
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
			if (map.get("END_AGE") != null)
				statement.setInt(11, Integer.valueOf(map.get("END_AGE").trim()));
			else
				statement.setInt(11, Integer.valueOf(0));
			statement.setString(12, map.get("STATE"));

			statement.execute();
			status = true;
			statement.close();
		}
		
		logger.info("ModelCreationFileProcessor---insertBasicGridDetails() Method ends Here");
		return status;

	}

	private void createTempGridValuesWithoutAgentCodes(Connection conn) throws SQLException {
		logger.info("BEGIN - ModelCreationFileProcessor---createTempGridValuesWithoutAgentCodes() Method called Here");
		String sql = "select PRODUCT from " + ModelCodeTableName + " ";
		PreparedStatement statement1 = conn.prepareStatement(sql);
		ResultSet rs1 = statement1.executeQuery();
		while (rs1.next()) {
			sql = " insert into " + gridTempTableName + "  " + " (select (select nvl(max(id),0) from "
					+ gridUploadMasterTableName + ")+rownum as ID , "
					+ "a.PRODUCT,MODELCODE as X_AXIS,Y_AXIS,VALUE,GRID_ID,GRID_NAME,EFFECTIVE_START_DATE, "
					+ " EFFECTIVE_END_DATE,VERSION,TRANSACTIONTYPE,START_AGE,END_AGE,STATE " + " from "
					+ ModelCodeTableName + " a cross join " + baseGridTableName + " b where b.PRODUCT='"
					+ rs1.getString(1) + "') ";

			logger.info(sql);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.execute();
			statement.close();
		}
		if (rs1 != null)
			rs1.close();
		statement1.execute();
		statement1.close();

		logger.info("ModelCreationFileProcessor---createTempGridValuesWithoutAgentCodes() Method ends Here");
	}

	private void gridUploadMasterMethod(Connection conn) throws SQLException {

		logger.info("BEGIN - ModelCreationFileProcessor---gridUploadMasterMethod() Method called Here");

		/*
		 * String Sql = " update ( select * from " + gridUploadMasterTableName +
		 * " GR " +
		 * " where (GR.X_AXIS, GR.Y_AXIS )in(select GT.X_AXIS,GT.Y_AXIS  from "
		 * + gridTempTableName + " GT ))GR " +
		 * "  set GR.EFFECTIVE_END_DATE=(select distinct(GT.EFFECTIVE_START_DATE-1)  from "
		 * + gridTempTableName + " GT " +
		 * " where GT.TRANSACTIONTYPE = GR.TRANSACTIONTYPE and GT.EFFECTIVE_START_DATE <> GR.EFFECTIVE_START_DATE )"
		 * ; logger.info(Sql); PreparedStatement updateStatement =
		 * conn.prepareStatement(Sql);
		 * 
		 * 
		 * 
		 * updateStatement.execute(); updateStatement.close(); logger.info(
		 * " Grid upload Master Update Method Ended here");
		 */

		logger.info(" Grid upload Master Insert Method Called here");
		PreparedStatement statement = conn.prepareStatement(
				"insert into " + gridUploadMasterTableName + " (SELECT * FROM " + gridTempTableName + ")");

		statement.execute();

		statement.close();
		logger.info("ModelCreationFileProcessor---gridUploadMasterMethod() Method ends Here");
		conn.commit();

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
}
