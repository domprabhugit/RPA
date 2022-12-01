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
import com.rpa.model.processors.AgentIncentiveModel;
import com.rpa.model.processors.GridAutomationModel;
import com.rpa.model.processors.GridFilesCountDetails;
import com.rpa.repository.TransactionInfoRepository;
import com.rpa.service.processors.AgentIncentiveService;
import com.rpa.service.processors.GridAutomationService;
import com.rpa.util.UtilityFile;

public class AgentIncentiveFileUploadProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(AgentIncentiveFileUploadProcessor.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	TransactionInfoRepository transactionInfoRepository;

	@Autowired
	AgentIncentiveService agentIncentiveService;

	private void AutoWiringBeanPropertiesSetMethod() {

		logger.info("AutoWiringBeanPropertiesSetMethod is Called in AgentIncentiveFileUploadProcessor Class");
		applicationContext = SpringContext.getAppContext();
		transactionInfoRepository = applicationContext.getBean(TransactionInfoRepository.class);

		agentIncentiveService = applicationContext.getBean(AgentIncentiveService.class);
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in AgentIncentiveFileUploadProcessor Class");

	}

	@Override
	public void process(Exchange exchange) throws Exception {

		AgentIncentiveFileUploadProcessor grid = new AgentIncentiveFileUploadProcessor();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty("TRANSACTION_INFO_REQ");
		grid.doProcess(transactionInfo, exchange);

	}

	@SuppressWarnings("unchecked")
	public void doProcess(TransactionInfo transactionInfo, Exchange exchange)
			throws Exception {
		// To get AutowiredBeans
		AutoWiringBeanPropertiesSetMethod();
		// To clear all the tempTable before starting the process
		logger.info("BEGIN - AgentIncentiveFileUploadProcessor---doProcess() Method Called Here");
		
		AgentIncentiveModel AgentIncentiveModel = (AgentIncentiveModel) exchange.getProperty("incentive_obj");
		
		if(AgentIncentiveModel!=null){
			if(AgentIncentiveModel.getIsValidationSucceeded().equals("Y")){
				
				Connection connection = UtilityFile.getLocalRPAConnection();
				
				try{
					
				tableDeleteMethod(connection,getListOfTablesToTruncate(RPAConstants.AGENT_TRUNCATE_TABLES));
				
				String newXgenFilePath = UtilityFile.FileMovementMethod(
						UtilityFile.getCodeBasePath()
								+ UtilityFile.getAgentProperty("Agent.File.Location.BasePath"),
						new File(AgentIncentiveModel.getxGenFilePath()),
						UtilityFile.getAgentProperty("Agent.File.Processed.Path")
								+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY).toString()
								+ UtilityFile.getAgentProperty("Agent.File.Location.ValidatedPath"));
				AgentIncentiveModel.setD2cFilePath(newXgenFilePath);
				String newCrmFilePath = UtilityFile.FileMovementMethod(
						UtilityFile.getCodeBasePath()
								+ UtilityFile.getAgentProperty("Agent.File.Location.BasePath"),
						new File(AgentIncentiveModel.getCrmFilePath()),
						UtilityFile.getAgentProperty("Agent.File.Processed.Path")
								+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY).toString()
								+ UtilityFile.getAgentProperty("Agent.File.Location.ValidatedPath"));
				AgentIncentiveModel.setCrmFilePath(newCrmFilePath);
				
				List<Map<String, String>> xgenList = (List<Map<String, String>>) exchange.getProperty("incentive_xgenList");
					insertXgenDataInDB(connection,xgenList);
				List<Map<String, String>> crmList = (List<Map<String, String>>) exchange.getProperty("incentive_crmList");
					insertCrmDataInDB(connection,crmList);
					
					
				String sql =	"select distinct TO_CHAR(ENTRY_DATE,'MM/YYYY') from RPA_ADMIN.XGEN_TEMP A WHERE TO_CHAR(A.ENTRY_DATE,'MM/YYYY') IN (select distinct MONTH_YEAR from RPA_ADMIN.AGENT_RESPONSE) ORDER BY TO_CHAR(ENTRY_DATE,'MM/YYYY')" ;
				String duplicateCaluldatedMonthYear="";
				try (PreparedStatement ps = connection.prepareStatement(sql)) {
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							duplicateCaluldatedMonthYear +=rs.getString(1)+",";
						}
						if (rs != null)
							rs.close();
					}
				}	
				duplicateCaluldatedMonthYear="";
				if(!duplicateCaluldatedMonthYear.equals("")){
					throw new Exception("Incentive Calculation Already Exists for the month/year :: "+duplicateCaluldatedMonthYear);
				}
					
				 sql =	"select distinct TO_CHAR(CONVERSION_DATE,'MM/YYYY') from RPA_ADMIN.CRM_TEMP A WHERE TO_CHAR(A.CONVERSION_DATE,'MM/YYYY')  IN (select distinct TO_CHAR(CONVERSION_DATE,'MM/YYYY') from RPA_ADMIN.CONVERSION_DATA) ORDER BY TO_CHAR(CONVERSION_DATE,'MM/YYYY')" ;
				String duplicateMonthYear="";
				try (PreparedStatement ps = connection.prepareStatement(sql)) {
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							duplicateMonthYear+=rs.getString(1)+",";
						}
						if (rs != null)
							rs.close();
					}
				}
				duplicateMonthYear="";
				if(duplicateMonthYear.equals("")){
					insertConversionDataInDB(connection,crmList);
				}else{
					throw new Exception("CRM data Already Exists for the month/year :: "+duplicateMonthYear);
				}
					
					
				}finally{
					if(connection!=null){
						connection.close();
					}
				}
				
				
				
			}else{
				String newXgenFilePath = UtilityFile.FileMovementMethod(
						UtilityFile.getCodeBasePath()
								+ UtilityFile.getAgentProperty("Agent.File.Location.BasePath"),
						new File(AgentIncentiveModel.getxGenFilePath()),
						UtilityFile.getAgentProperty("Agent.File.Processed.Path")
								+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY).toString()
								+ UtilityFile.getAgentProperty("Agent.File.Location.ErrorPath"));
				AgentIncentiveModel.setD2cFilePath(newXgenFilePath);
				String newCrmFilePath = UtilityFile.FileMovementMethod(
						UtilityFile.getCodeBasePath()
								+ UtilityFile.getAgentProperty("Agent.File.Location.BasePath"),
						new File(AgentIncentiveModel.getCrmFilePath()),
						UtilityFile.getAgentProperty("Agent.File.Processed.Path")
								+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY).toString()
								+ UtilityFile.getAgentProperty("Agent.File.Location.ErrorPath"));
				AgentIncentiveModel.setCrmFilePath(newCrmFilePath);
			}
		}
		
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
	}
	

	private boolean insertCrmDataInDB(Connection conn, List<Map<String, String>> crmList) throws ParseException, NumberFormatException, SQLException {
		Boolean status = false;
		int  i = 0;
		
		String qry = "select max(id) from crm_temp";
		
		try (PreparedStatement ps = conn.prepareStatement(qry)) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					i = rs.getInt(1);
				}
			}
		}
		
		for (Map<String, String> map : crmList) {
			i++;
			//UtilityFile.checkDateFormat(map.get("DATE"), "d-MMM-yy");
			
			SimpleDateFormat originalFormat = new SimpleDateFormat("d-MMM-yy");
			Date conversionDate = originalFormat.parse(map.get("DATE"));
			conn.setAutoCommit(true);
			PreparedStatement statement = conn
					.prepareStatement("insert into crm_temp (id,AGENT_NAME,TL_NAME,PROCESS_TL,ACCESS_TYPE,CONVERSION_DATE,POLICY_NO,lead_source) values(?,?,?,?,?,?,?,?)");
			logger.info(" Table Name crm_temp row number :::  "+i);
			statement.setInt(1, i);
			statement.setString(2, map.get("AGENT NAME").trim());
			statement.setString(3, map.get("TEAM").trim());
			if(map.get("PROCESS TL")!=null)
				statement.setString(4, map.get("PROCESS TL").trim());
			else
				statement.setString(4, "");
			
			/*statement.setString(6, map.get("ACCESS").trim());*/
			statement.setString(5, "");
			statement.setDate(6, new java.sql.Date(conversionDate.getTime()));
			statement.setString(7, map.get("POLICY NO").trim());
			//statement.setString(7, "");
			
			statement.setString(8, map.get("LEAD SOURCE").trim());

			statement.execute();
			status = true;
			statement.close();
		}
		return status;
		
	}

	
	private boolean insertConversionDataInDB(Connection conn, List<Map<String, String>> crmList) throws ParseException, NumberFormatException, SQLException {
		Boolean status = false;
		int  i = 0;
		
		String qry = "select max(id) from conversion_data";
		
		try (PreparedStatement ps = conn.prepareStatement(qry)) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					i = rs.getInt(1);
				}
			}
		}
		
		for (Map<String, String> map : crmList) {
			i++;
			//UtilityFile.checkDateFormat(map.get("DATE"), "d-MMM-yy");
			
			SimpleDateFormat originalFormat = new SimpleDateFormat("d-MMM-yy");
			Date conversionDate = null;
			if(UtilityFile.checkDateFormat(map.get("DATE"), "d-MMM-yy")){
				 conversionDate = originalFormat.parse(map.get("DATE"));
			}else if(UtilityFile.checkDateFormat(map.get("DATE"), "dd/MMM/yy")){
				 conversionDate = new SimpleDateFormat("dd/MMM/yy").parse(map.get("DATE"));
			}
			
			
			
			conn.setAutoCommit(true);
			PreparedStatement statement = conn
					.prepareStatement("insert into conversion_data (id,AGENT_NAME,TL_NAME,PROCESS_TL,ACCESS_TYPE,CONVERSION_DATE,POLICY_NO,lead_source) values(?,?,?,?,?,?,?,?)");
			logger.info(" Table Name crm_temp row number :::  "+i);
			statement.setInt(1, i);
			statement.setString(2, map.get("AGENT NAME").trim());
			statement.setString(3, map.get("TEAM").trim());
			if(map.get("PROCESS TL")!=null)
				statement.setString(4, map.get("PROCESS TL").trim());
			else
				statement.setString(4, "");
			
			/*statement.setString(6, map.get("ACCESS").trim());*/
			statement.setString(5, "");
			statement.setDate(6, new java.sql.Date(conversionDate.getTime()));
			statement.setString(7, map.get("POLICY NO").trim());
			//statement.setString(7, "");
			
			statement.setString(8, map.get("LEAD SOURCE").trim());

			statement.execute();
			status = true;
			statement.close();
		}
		return status;
		
	}
	
	private boolean insertD2cDataInDB(Connection conn, List<Map<String, String>> d2cList) throws ParseException, SQLException {
		Boolean status = false;
		Date conversionDate = null;
		for (Map<String, String> map : d2cList) {
			/*SimpleDateFormat originalFormat = new SimpleDateFormat("dd-mm-yyyy");
			Date conversionDate = originalFormat.parse(map.get("BUY_DATE"));*/
			
			if(map.get("BUY_DATE")!=null){
				//if(UtilityFile.checkDateFormat(map.get("BUY_DATE").split(" ")[0], "M/dd/yy"))
				SimpleDateFormat originalFormat = new SimpleDateFormat("M/dd/yy");
				if(map.get("BUY_DATE").indexOf(" ")!=-1){
					conversionDate = originalFormat.parse(map.get("BUY_DATE").split(" ")[0]);
				}else{
					conversionDate = originalFormat.parse(map.get("BUY_DATE"));
				}
				
			}else{
				break;
			}
			
			PreparedStatement statement = conn
					.prepareStatement("insert into d2c_temp (QUOTE_ID,POLICY_CODE,CUSTOMER_NAME,MOBILE,BUY_DATE) values(?,?,?,?,?)");
			logger.info("Base Grid Table Name::: d2c_temp " );
			statement.setString(1, map.get("QUOTE_ID").trim());
			statement.setString(2, map.get("POLICYCODE").trim());
			statement.setString(3, map.get("CUSTOMER NAME").trim());
			statement.setString(4, map.get("MOBILE").trim());
			statement.setDate(5, new java.sql.Date(conversionDate.getTime()));

			statement.execute();
			status = true;
			statement.close();
		}
		return status;
		
	}

	private boolean insertXgenDataInDB(Connection conn, List<Map<String, String>> xgenList) throws ParseException, SQLException {
		Boolean status = false;
		Date entryDate = null;
		int i = 0;
		for (Map<String, String> map : xgenList) {
			i++;
			SimpleDateFormat originalFormat = null;
			/*SimpleDateFormat originalFormat = new SimpleDateFormat("d/m/yy");
			Date date = originalFormat.parse(map.get("CONVERSION DATE"));
			SimpleDateFormat newFormat = new SimpleDateFormat("mm/dd/yyyy");
			String formatedDate = newFormat.format(date);*/
			if(map.get("ENTRY DATE")!=null){
				if(UtilityFile.checkDateFormat(map.get("ENTRY DATE").split(" ")[0], "d/M/yy")){
					 originalFormat = new SimpleDateFormat("d/M/yy");
				}else{
					if(UtilityFile.checkDateFormat(map.get("ENTRY DATE").split(" ")[0], "M/d/yyyy")){
						 originalFormat = new SimpleDateFormat("M/d/yyyy");
					}
					
				}
				
				if(map.get("ENTRY DATE").indexOf(" ")!=-1){
					 entryDate = originalFormat.parse(map.get("ENTRY DATE").split(" ")[0]);
				}else{
					 entryDate = originalFormat.parse(map.get("ENTRY DATE"));
				}
				
			}else{
				break;
			}
			PreparedStatement statement = conn
					.prepareStatement("insert into xgen_temp (ID,POLICY_NO,OUR_SHARE_OF_PREMIUM,TP_PREMIUM,POLICY_COUNT,PRODUCT,ENTRY_DATE) values(?,?,?,?,?,?,?)");
			logger.info("Base Grid Table Name::: xgen_temp ROW NO --> "+i );
			statement.setInt(1, i);
			statement.setString(2, map.get("POLICY NO.").trim());
			statement.setString(3, map.get("OUR SHARE OF PREMIUM").trim());
			if(map.get("TPPREMIUM")!=null)
				statement.setString(4, map.get("TPPREMIUM").trim());
			else
				statement.setString(4, "0");
			statement.setInt(5, Integer.valueOf(map.get("POLICY_COUNT").trim()));
			statement.setString(6, map.get("PRODUCT").trim());
			statement.setDate(7, new java.sql.Date(entryDate.getTime()));

			statement.execute();
			status = true;
			statement.close();
		}
		return status;
		
	}


	private List<String> getListOfTablesToTruncate(String key) {

		List<String> tableList = new ArrayList<String>();
		String[] tableNames = UtilityFile.getAgentProperty(key).split(RPAConstants.GRID_VALUE_SEPARATOR);
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
