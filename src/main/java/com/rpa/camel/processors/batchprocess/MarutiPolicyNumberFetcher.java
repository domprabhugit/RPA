/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.MarutiPolicy;
import com.rpa.model.processors.MarutiPolicyTakenStatus;
import com.rpa.response.CarResponse;
import com.rpa.service.processors.MarutiPolicyService;
import com.rpa.util.UtilityFile;

public class MarutiPolicyNumberFetcher implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(MarutiPolicyNumberFetcher.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	MarutiPolicyService marutiPolicyService;

	@Override
	public void process(Exchange exchange) throws URISyntaxException, MalformedURLException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		logger.info("*********** inside Camel Process of MarutiPolicyNumberFetcher Called ************");
		logger.info("BEGIN : MarutiPolicyNumberFetcher Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.FETCH_NUMBER);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		marutiPolicyService = applicationContext.getBean(MarutiPolicyService.class);
		MarutiPolicyNumberFetcher MarutiPolicyNumberFetcher = new MarutiPolicyNumberFetcher();
		MarutiPolicyNumberFetcher.doProcess(MarutiPolicyNumberFetcher, exchange, transactionInfo, marutiPolicyService);
		logger.info("*********** inside Camel Process of MarutiPolicyNumberFetcher Processor Ended ************");
	}

	public void doProcess(MarutiPolicyNumberFetcher MarutiPolicyNumberFetcher, Exchange exchange, TransactionInfo transactionInfo,
			MarutiPolicyService marutiPolicyService) throws MalformedURLException, IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("BEGIN : MarutiPolicyNumberFetcher Processor - doProcess Method Called  ");

		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		logger.info("MarutiPolicyNumberFetcher - connection object created :: " + conn);
		List<MarutiPolicyTakenStatus> policyDateList = new ArrayList<MarutiPolicyTakenStatus>();
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			 policyDateList = marutiPolicyService.getPendingPolicyDateList(UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_NORMAL_STARTDATE_YYFORMAT));
		}else{
			String startDate = UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_BACKLOG_STARTDATE_YYFORMAT);
			String endDate = UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_BACKLOG_ENDDATE_YYFORMAT);
			policyDateList = marutiPolicyService.getPendingBacklogPolicyDateList(startDate,endDate);
		}
			
			for (MarutiPolicyTakenStatus marutiPolicyTakenStatus : policyDateList) {
				logger.info("MarutiPolicyNumberFetcher - policy to be fetched from maruti db for policy date :: " + marutiPolicyTakenStatus.getPolicyDate());
				getPolicyNumberListFromMarutiDB(driver, transactionInfo, marutiPolicyService, exchange, marutiPolicyTakenStatus, conn);
				break;
			}
		
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		if (conn != null)
			conn.close();

		logger.info("MarutiPolicyNumberFetcher Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListFromMarutiDB(WebDriver driver, TransactionInfo transactionInfo,
			MarutiPolicyService marutiPolicyService, Exchange exchange, MarutiPolicyTakenStatus marutiPolicyTakenStatus, Connection conn) throws InstantiationException,
					IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("MarutiPolicyNumberFetcher - BEGIN getPolicyNumberListFromMarutiDB()  called ");

		List<String> marutiPolicyNoLocalList = new ArrayList<String>();

		List<CarResponse> MarutiResponseList = new ArrayList<CarResponse>();
		String sql = "";
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			marutiPolicyNoLocalList = marutiPolicyService.findExistingPolicyList(marutiPolicyTakenStatus.getPolicyDate());
			sql = "select policyno,proposalno,to_char(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/DD/YYYY') from ttrn_maruti_newrenewal_service  v where policyno not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM-DD-YY'),'MM/DD/YYYY')= "
					+ "to_char(TO_DATE('"+marutiPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'MM/DD/YYYY') ";
			logger.info("MarutiPolicyNumberFetcher - sql query to fetch policies dated - "+marutiPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
		} else if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			marutiPolicyNoLocalList = marutiPolicyService.findExistingBacklogPolicyList(marutiPolicyTakenStatus.getPolicyDate());
			sql = "select policyno,proposalno,to_char(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/DD/YYYY') from ttrn_maruti_newrenewal_service  v where policyno not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM-DD-YY'),'MM/DD/YYYY')= "
					+ "to_char(TO_DATE('"+marutiPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'MM/DD/YYYY') ";
			logger.info("MarutiPolicyNumberFetcher - sql query to fetch backlog policies dated - "+marutiPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
		}
		
		insertPoliciesInLocalDb(MarutiResponseList,conn,sql,marutiPolicyNoLocalList,exchange,marutiPolicyTakenStatus,marutiPolicyService);

		if (conn != null) {
			conn.close();
		}
		
		logger.info("MarutiPolicyNumberFetcher - getPolicyNumberListFromMarutiDB()  END ");
		return true;
	}

	private void insertPoliciesInLocalDb(List<CarResponse> marutiResponseList, Connection conn, String sql, List<String> marutiPolicyNoLocalList,
			Exchange exchange, MarutiPolicyTakenStatus marutiPolicyTakenStatus, MarutiPolicyService marutiPolicyService) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					CarResponse marutiResponse = new CarResponse();
					marutiResponse.setPolicyNo(rs.getString(1));
					marutiResponse.setProposalNumber(rs.getString(2));
					marutiResponse.setPolicyDate(rs.getString(3));
					marutiResponseList.add(marutiResponse);
					marutiResponse = null;
				}
				if (ps!=null)
					ps.close();
				if (rs != null)
					rs.close();
			}
		}

		logger.info("MarutiPolicyNumberFetcher - policies count :: " + marutiResponseList.size());

		List<MarutiPolicy> newMarutiPolicyList = new ArrayList<MarutiPolicy>();
		MarutiPolicy mpolicy = null;
		for (CarResponse marutiResponse : marutiResponseList) {
			if (!marutiPolicyNoLocalList.contains(marutiResponse.getPolicyNo())) {
				mpolicy = new MarutiPolicy();
				mpolicy.setIsPolicyUploaded(RPAConstants.N);
				mpolicy.setIsProposalUploaded(RPAConstants.N);
				mpolicy.setIsPolicyDownloaded(RPAConstants.N);
				mpolicy.setIsProposalDownloaded(RPAConstants.N);
				mpolicy.setPolicyNo(marutiResponse.getPolicyNo());
				mpolicy.setProposalNumber(marutiResponse.getProposalNumber());

				mpolicy.setPolicyDate(marutiResponse.getPolicyDate());
				if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
					mpolicy.setBackLogFlag(RPAConstants.Y);
				} else {
					mpolicy.setBackLogFlag(RPAConstants.N);
				}
				mpolicy.setCarType(RPAConstants.OMNI_MARUTI_PREFIX);
				newMarutiPolicyList.add(mpolicy);
				mpolicy = null;
			}
		}
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			logger.info("MarutiPolicyNumberFetcher - Newly fetched Policies for the date "+marutiPolicyTakenStatus.getPolicyDate()+" :: " + newMarutiPolicyList.size());
		} else {
			logger.info("MarutiPolicyNumberFetcher - Newly fetched Policies  count as backlog :: " + newMarutiPolicyList.size());
		}
		if(marutiPolicyService.saveNewObject(newMarutiPolicyList)!=null){
			marutiPolicyTakenStatus.setStatus(RPAConstants.Y);
			marutiPolicyService.update(marutiPolicyTakenStatus);
		}
		
	}

}
