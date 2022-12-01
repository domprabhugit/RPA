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
import com.rpa.model.processors.TafePolicy;
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.response.CarResponse;
import com.rpa.service.processors.TafePolicyService;
import com.rpa.util.UtilityFile;

public class TafePolicyNumberFetcher implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(TafePolicyNumberFetcher.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	TafePolicyService tafePolicyService;

	@Override
	public void process(Exchange exchange) throws URISyntaxException, MalformedURLException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		logger.info("*********** inside Camel Process of tafePolicyNumberFetcher Called ************");
		logger.info("BEGIN : tafePolicyNumberFetcher Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.FETCH_NUMBER);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		tafePolicyService = applicationContext.getBean(TafePolicyService.class);
		TafePolicyNumberFetcher tafePolicyNumberFetcher = new TafePolicyNumberFetcher();
		tafePolicyNumberFetcher.doProcess(tafePolicyNumberFetcher, exchange, transactionInfo, tafePolicyService);
		logger.info("*********** inside Camel Process of tafePolicyNumberFetcher Processor Ended ************");
	}

	public void doProcess(TafePolicyNumberFetcher tafePolicyNumberFetcher, Exchange exchange, TransactionInfo transactionInfo,
			TafePolicyService tafePolicyService) throws MalformedURLException, IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("BEGIN : tafePolicyNumberFetcher Processor - doProcess Method Called  ");

		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		logger.info("tafePolicyNumberFetcher - connection object created :: " + conn);
		List<CommonPolicyTakenStatus> policyDateList = new ArrayList<CommonPolicyTakenStatus>();
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			 policyDateList = tafePolicyService.getPendingPolicyDateListTafe(UtilityFile.getCarPolicyProperty(RPAConstants.TAFE_NORMAL_STARTDATE_YYFORMAT));
		}else{
			String startDate = UtilityFile.getCarPolicyProperty(RPAConstants.TAFE_BACKLOG_STARTDATE_YYFORMAT);
			String endDate = UtilityFile.getCarPolicyProperty(RPAConstants.TAFE_BACKLOG_ENDDATE_YYFORMAT);
			policyDateList = tafePolicyService.getPendingBacklogPolicyDateListTafe(startDate,endDate);
		}
			
			for (CommonPolicyTakenStatus commonPolicyTakenStatus : policyDateList) {
				logger.info("tafePolicyNumberFetcher - policy to be fetched from maruti db for policy date :: " + commonPolicyTakenStatus.getPolicyDate());
				getPolicyNumberListFromTafeDB(driver, transactionInfo, tafePolicyService, exchange, commonPolicyTakenStatus, conn);
				break;
			}
		
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		if (conn != null)
			conn.close();

		logger.info("BEGIN : tafePolicyNumberFetcher Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListFromTafeDB(WebDriver driver, TransactionInfo transactionInfo,
			TafePolicyService tafePolicyService, Exchange exchange, CommonPolicyTakenStatus commonPolicyTakenStatus, Connection conn) throws InstantiationException,
					IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("tafePolicyNumberFetcher - BEGIN getPolicyNumberListFromTafeDB()  called ");

		List<String> tafePolicyNoLocalList = new ArrayList<String>();

		List<CarResponse> carReponseList = new ArrayList<CarResponse>();
		String sql = "";
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			tafePolicyNoLocalList = tafePolicyService.findExistingPolicyList(commonPolicyTakenStatus.getPolicyDate());
			sql = "select POLICYNO,PROPOSAL_NO proposal_no,to_char(TO_DATE(REQUEST_DATE,'DD/MM/YYYY'),'MM/DD/YYYY') from TTRN_abiblt_NEWRENEWAL_SERVICE where POLICYNO not like 'DUM%' and TO_CHAR(TO_DATE(REQUEST_DATE,'DD-MM-YY'),'DD/MM/YYYY')= "
			+ "to_char(TO_DATE('"+commonPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'DD/MM/YYYY') ";
			logger.info("tafePolicyNumberFetcher - sql query to fetch policies dated - "+commonPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
		} else if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			tafePolicyNoLocalList = tafePolicyService.findExistingBacklogPolicyList(commonPolicyTakenStatus.getPolicyDate());
			sql = "select POLICYNO,PROPOSAL_NO proposal_no,to_char(TO_DATE(REQUEST_DATE,'DD/MM/YYYY'),'MM/DD/YYYY') from TTRN_abiblt_NEWRENEWAL_SERVICE where POLICYNO not like 'DUM%' and TO_CHAR(TO_DATE(REQUEST_DATE,'DD-MM-YY'),'DD/MM/YYYY')= "
			+ "to_char(TO_DATE('"+commonPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'DD/MM/YYYY')";
			logger.info("tafePolicyNumberFetcher - sql query to fetch backlog policies dated - "+commonPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
		}
		
		insertPoliciesInLocalDb(carReponseList,conn,sql,tafePolicyNoLocalList,exchange,commonPolicyTakenStatus,tafePolicyService);

		if (conn != null) {
			conn.close();
		}
		
		logger.info("tafePolicyNumberFetcher - getPolicyNumberListFromTafeDB()  END ");
		return true;
	}

	private void insertPoliciesInLocalDb(List<CarResponse> carReponseList, Connection conn, String sql, List<String> tafePolicyNoLocalList,
			Exchange exchange, CommonPolicyTakenStatus commonPolicyTakenStatus, TafePolicyService tafePolicyService) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					CarResponse carReponse = new CarResponse();
					carReponse.setPolicyNo(rs.getString(1));
					carReponse.setProposalNumber(rs.getString(2));
					carReponse.setPolicyDate(rs.getString(3));
					carReponseList.add(carReponse);
					carReponse = null;
				}
				if (rs != null)
					rs.close();
			}
		}

		logger.info("tafePolicyNumberFetcher - policies count :: " + carReponseList.size());

		List<TafePolicy> newMarutiPolicyList = new ArrayList<TafePolicy>();
		TafePolicy fPolicy = null;
		for (CarResponse carResponse : carReponseList) {
			if (!tafePolicyNoLocalList.contains(carResponse.getPolicyNo())) {
				fPolicy = new TafePolicy();
				fPolicy.setIsPolicyUploaded(RPAConstants.N);
				fPolicy.setIsProposalUploaded(RPAConstants.N);
				fPolicy.setIsPolicyDownloaded(RPAConstants.N);
				fPolicy.setIsProposalDownloaded(RPAConstants.N);
				fPolicy.setPolicyNo(carResponse.getPolicyNo());
				fPolicy.setProposalNumber(carResponse.getProposalNumber());

				fPolicy.setPolicyDate(carResponse.getPolicyDate());
				if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
					fPolicy.setBackLogFlag(RPAConstants.Y);
				} else {
					fPolicy.setBackLogFlag(RPAConstants.N);
				}
				fPolicy.setCarType(RPAConstants.OMNI_TAFE_PREFIX);
				newMarutiPolicyList.add(fPolicy);
				fPolicy = null;
			}
		}
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			logger.info("tafePolicyNumberFetcher - Newly fetched Policies for the date "+commonPolicyTakenStatus.getPolicyDate()+" :: " + newMarutiPolicyList.size());
		} else {
			logger.info("tafePolicyNumberFetcher - Newly fetched Policies  count as backlog :: " + newMarutiPolicyList.size());
		}
		if(tafePolicyService.save(newMarutiPolicyList)!=null){
			commonPolicyTakenStatus.setTafeStatus(RPAConstants.Y);
			tafePolicyService.save(commonPolicyTakenStatus);
		}
		
	}

}
