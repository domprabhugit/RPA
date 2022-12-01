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
import com.rpa.model.processors.PiaggioPolicy;
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.response.CarResponse;
import com.rpa.service.processors.PiaggioPolicyService;
import com.rpa.util.UtilityFile;

public class PiaggioPolicyNumberFetcher implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(PiaggioPolicyNumberFetcher.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	PiaggioPolicyService piaggioPolicyService;

	@Override
	public void process(Exchange exchange) throws URISyntaxException, MalformedURLException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		logger.info("*********** inside Camel Process of piaggioPolicyNumberFetcher Called ************");
		logger.info("BEGIN : piaggioPolicyNumberFetcher Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.FETCH_NUMBER);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		piaggioPolicyService = applicationContext.getBean(PiaggioPolicyService.class);
		PiaggioPolicyNumberFetcher piaggioPolicyNumberFetcher = new PiaggioPolicyNumberFetcher();
		piaggioPolicyNumberFetcher.doProcess(piaggioPolicyNumberFetcher, exchange, transactionInfo, piaggioPolicyService);
		logger.info("*********** inside Camel Process of piaggioPolicyNumberFetcher Processor Ended ************");
	}

	public void doProcess(PiaggioPolicyNumberFetcher piaggioPolicyNumberFetcher, Exchange exchange, TransactionInfo transactionInfo,
			PiaggioPolicyService piaggioPolicyService) throws MalformedURLException, IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("BEGIN : piaggioPolicyNumberFetcher Processor - doProcess Method Called  ");

		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		logger.info("piaggioPolicyNumberFetcher - connection object created :: " + conn);
		List<CommonPolicyTakenStatus> policyDateList = new ArrayList<CommonPolicyTakenStatus>();
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			 policyDateList = piaggioPolicyService.getPendingPolicyDateListPiaggio(UtilityFile.getCarPolicyProperty(RPAConstants.PIAGGIO_NORMAL_STARTDATE_YYFORMAT));
		}else{
			String startDate = UtilityFile.getCarPolicyProperty(RPAConstants.PIAGGIO_BACKLOG_STARTDATE_YYFORMAT);
			String endDate = UtilityFile.getCarPolicyProperty(RPAConstants.PIAGGIO_BACKLOG_ENDDATE_YYFORMAT);
			policyDateList = piaggioPolicyService.getPendingBacklogPolicyDateListPiaggio(startDate,endDate);
		}
			
			for (CommonPolicyTakenStatus commonPolicyTakenStatus : policyDateList) {
				logger.info("piaggioPolicyNumberFetcher - policy to be fetched from maruti db for policy date :: " + commonPolicyTakenStatus.getPolicyDate());
				getPolicyNumberListFromPiaggioDB(driver, transactionInfo, piaggioPolicyService, exchange, commonPolicyTakenStatus, conn);
				break;
			}
		
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		if (conn != null)
			conn.close();

		logger.info("BEGIN : piaggioPolicyNumberFetcher Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListFromPiaggioDB(WebDriver driver, TransactionInfo transactionInfo,
			PiaggioPolicyService piaggioPolicyService, Exchange exchange, CommonPolicyTakenStatus commonPolicyTakenStatus, Connection conn) throws InstantiationException,
					IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("piaggioPolicyNumberFetcher - BEGIN getPolicyNumberListFromPiaggioDB()  called ");

		List<String> piaggioPolicyNoLocalList = new ArrayList<String>();

		List<CarResponse> carReponseList = new ArrayList<CarResponse>();
		String sql = "";
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			piaggioPolicyNoLocalList = piaggioPolicyService.findExistingPolicyList(commonPolicyTakenStatus.getPolicyDate());
			sql = "select POLICY_NO,TRANSACTIONID proposal_no,to_char(TO_DATE(SUBSTR(RES_TIME,1,10),'DD/MM/YYYY'),'MM/DD/YYYY') from TTRN_ADITYA_NEWRENEWAL_SERVICE where POLICY_NO not like 'DUM%' and make='PI' and TO_CHAR(TO_DATE(SUBSTR(RES_TIME,1,10),'DD-MM-YY'),'DD/MM/YYYY')= "
					+ "to_char(TO_DATE('"+commonPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'DD/MM/YYYY') and payment_yn='Y' ";
			logger.info("piaggioPolicyNumberFetcher - sql query to fetch policies dated - "+commonPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
			
		} else if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			piaggioPolicyNoLocalList = piaggioPolicyService.findExistingBacklogPolicyList(commonPolicyTakenStatus.getPolicyDate());
			sql = "select POLICY_NO,TRANSACTIONID proposal_no,to_char(TO_DATE(SUBSTR(RES_TIME,1,10),'DD/MM/YYYY'),'MM/DD/YYYY') from TTRN_ADITYA_NEWRENEWAL_SERVICE where POLICY_NO not like 'DUM%' and make='PI' and TO_CHAR(TO_DATE(SUBSTR(RES_TIME,1,10),'DD-MM-YY'),'DD/MM/YYYY')= "
					+ "to_char(TO_DATE('"+commonPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'DD/MM/YYYY') and payment_yn='Y' ";
			logger.info("piaggioPolicyNumberFetcher - sql query to fetch backlog policies dated - "+commonPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
		}
		
		insertPoliciesInLocalDb(carReponseList,conn,sql,piaggioPolicyNoLocalList,exchange,commonPolicyTakenStatus,piaggioPolicyService);

		if (conn != null) {
			conn.close();
		}
		
		logger.info("piaggioPolicyNumberFetcher - getPolicyNumberListFromPiaggioDB()  END ");
		return true;
	}

	private void insertPoliciesInLocalDb(List<CarResponse> carReponseList, Connection conn, String sql, List<String> piaggioPolicyNoLocalList,
			Exchange exchange, CommonPolicyTakenStatus commonPolicyTakenStatus, PiaggioPolicyService piaggioPolicyService) throws SQLException {
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

		logger.info("piaggioPolicyNumberFetcher - policies count :: " + carReponseList.size());

		List<PiaggioPolicy> newMarutiPolicyList = new ArrayList<PiaggioPolicy>();
		PiaggioPolicy fPolicy = null;
		for (CarResponse carResponse : carReponseList) {
			if (!piaggioPolicyNoLocalList.contains(carResponse.getPolicyNo())) {
				fPolicy = new PiaggioPolicy();
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
				fPolicy.setCarType(RPAConstants.OMNI_PIAGGIO_PREFIX);
				newMarutiPolicyList.add(fPolicy);
				fPolicy = null;
			}
		}
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			logger.info("piaggioPolicyNumberFetcher - Newly fetched Policies for the date "+commonPolicyTakenStatus.getPolicyDate()+" :: " + newMarutiPolicyList.size());
		} else {
			logger.info("piaggioPolicyNumberFetcher - Newly fetched Policies  count as backlog :: " + newMarutiPolicyList.size());
		}
		if(piaggioPolicyService.save(newMarutiPolicyList)!=null){
			commonPolicyTakenStatus.setPiaggioStatus(RPAConstants.Y);
			piaggioPolicyService.save(commonPolicyTakenStatus);
		}
		
	}

}
