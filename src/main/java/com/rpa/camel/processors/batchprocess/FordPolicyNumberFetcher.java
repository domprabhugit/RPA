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
import com.rpa.model.processors.FordPolicy;
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.response.CarResponse;
import com.rpa.service.processors.FordPolicyService;
import com.rpa.util.UtilityFile;

public class FordPolicyNumberFetcher implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(FordPolicyNumberFetcher.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	FordPolicyService fordPolicyService;

	@Override
	public void process(Exchange exchange) throws URISyntaxException, MalformedURLException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		logger.info("*********** inside Camel Process of fordPolicyNumberFetcher Called ************");
		logger.info("BEGIN : fordPolicyNumberFetcher Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.FETCH_NUMBER);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		fordPolicyService = applicationContext.getBean(FordPolicyService.class);
		FordPolicyNumberFetcher fordPolicyNumberFetcher = new FordPolicyNumberFetcher();
		fordPolicyNumberFetcher.doProcess(fordPolicyNumberFetcher, exchange, transactionInfo, fordPolicyService);
		logger.info("*********** inside Camel Process of fordPolicyNumberFetcher Processor Ended ************");
	}

	public void doProcess(FordPolicyNumberFetcher fordPolicyNumberFetcher, Exchange exchange, TransactionInfo transactionInfo,
			FordPolicyService fordPolicyService) throws MalformedURLException, IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("BEGIN : fordPolicyNumberFetcher Processor - doProcess Method Called  ");

		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		logger.info("fordPolicyNumberFetcher - connection object created :: " + conn);
		List<CommonPolicyTakenStatus> policyDateList = new ArrayList<CommonPolicyTakenStatus>();
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			 policyDateList = fordPolicyService.getPendingPolicyDateListFord(UtilityFile.getCarPolicyProperty(RPAConstants.FORD_NORMAL_STARTDATE_YYFORMAT));
		}else{
			String startDate = UtilityFile.getCarPolicyProperty(RPAConstants.FORD_BACKLOG_STARTDATE_YYFORMAT);
			String endDate = UtilityFile.getCarPolicyProperty(RPAConstants.FORD_BACKLOG_ENDDATE_YYFORMAT);
			policyDateList = fordPolicyService.getPendingBacklogPolicyDateListFord(startDate,endDate);
		}
			
			for (CommonPolicyTakenStatus marutiPolicyTakenStatus : policyDateList) {
				logger.info("fordPolicyNumberFetcher - policy to be fetched from maruti db for policy date :: " + marutiPolicyTakenStatus.getPolicyDate());
				getPolicyNumberListFromFordDB(driver, transactionInfo, fordPolicyService, exchange, marutiPolicyTakenStatus, conn);
				break;
			}
		
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		if (conn != null)
			conn.close();

		logger.info("BEGIN : fordPolicyNumberFetcher Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListFromFordDB(WebDriver driver, TransactionInfo transactionInfo,
			FordPolicyService fordPolicyService, Exchange exchange, CommonPolicyTakenStatus marutiPolicyTakenStatus, Connection conn) throws InstantiationException,
					IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("fordPolicyNumberFetcher - BEGIN getPolicyNumberListFromFordDB()  called ");

		List<String> fordPolicyNoLocalList = new ArrayList<String>();

		List<CarResponse> carReponseList = new ArrayList<CarResponse>();
		String sql = "";
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			fordPolicyNoLocalList = fordPolicyService.findExistingPolicyList(marutiPolicyTakenStatus.getPolicyDate());
			sql = "select POLICYNO,TRANSACTIONID proposal_no,to_char(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/DD/YYYY') from ttrn_ford_new_service where POLICYNO not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM-DD-YY'),'MM/DD/YYYY')= "
					+ "to_char(TO_DATE('"+marutiPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'MM/DD/YYYY') and payment_yn='Y' ";
			logger.info("fordPolicyNumberFetcher - sql query to fetch policies dated - "+marutiPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
			
		} else if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			fordPolicyNoLocalList = fordPolicyService.findExistingBacklogPolicyList(marutiPolicyTakenStatus.getPolicyDate());
			sql = "select POLICYNO,TRANSACTIONID proposal_no,to_char(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/DD/YYYY') from ttrn_ford_new_service where POLICYNO not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM-DD-YY'),'MM/DD/YYYY')= "
					+ "to_char(TO_DATE('"+marutiPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'MM/DD/YYYY') and payment_yn='Y' ";
			logger.info("fordPolicyNumberFetcher - sql query to fetch backlog policies dated - "+marutiPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
		}
		
		insertPoliciesInLocalDb(carReponseList,conn,sql,fordPolicyNoLocalList,exchange,marutiPolicyTakenStatus,fordPolicyService);

		if (conn != null) {
			conn.close();
		}
		
		logger.info("fordPolicyNumberFetcher - getPolicyNumberListFromFordDB()  END ");
		return true;
	}

	private void insertPoliciesInLocalDb(List<CarResponse> carReponseList, Connection conn, String sql, List<String> fordPolicyNoLocalList,
			Exchange exchange, CommonPolicyTakenStatus marutiPolicyTakenStatus, FordPolicyService fordPolicyService) throws SQLException {
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

		logger.info("fordPolicyNumberFetcher - policies count :: " + carReponseList.size());

		List<FordPolicy> newMarutiPolicyList = new ArrayList<FordPolicy>();
		FordPolicy fPolicy = null;
		for (CarResponse carResponse : carReponseList) {
			if (!fordPolicyNoLocalList.contains(carResponse.getPolicyNo())) {
				fPolicy = new FordPolicy();
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
				fPolicy.setCarType(RPAConstants.OMNI_FORD_PREFIX);
				newMarutiPolicyList.add(fPolicy);
				fPolicy = null;
			}
		}
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			logger.info("fordPolicyNumberFetcher - Newly fetched Policies for the date "+marutiPolicyTakenStatus.getPolicyDate()+" :: " + newMarutiPolicyList.size());
		} else {
			logger.info("fordPolicyNumberFetcher - Newly fetched Policies  count as backlog :: " + newMarutiPolicyList.size());
		}
		if(fordPolicyService.save(newMarutiPolicyList)!=null){
			marutiPolicyTakenStatus.setFordStatus(RPAConstants.Y);
			fordPolicyService.save(marutiPolicyTakenStatus);
		}
		
	}

}
