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
import com.rpa.model.processors.AbiblPolicy;
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.response.CarResponse;
import com.rpa.service.processors.AbiblPolicyService;
import com.rpa.util.UtilityFile;

public class AbiblPolicyNumberFetcher implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(AbiblPolicyNumberFetcher.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	AbiblPolicyService abiblPolicyService;

	@Override
	public void process(Exchange exchange) throws URISyntaxException, MalformedURLException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		logger.info("*********** inside Camel Process of abiblPolicyNumberFetcher Called ************");
		logger.info("BEGIN : abiblPolicyNumberFetcher Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.FETCH_NUMBER);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		abiblPolicyService = applicationContext.getBean(AbiblPolicyService.class);
		AbiblPolicyNumberFetcher abiblPolicyNumberFetcher = new AbiblPolicyNumberFetcher();
		abiblPolicyNumberFetcher.doProcess(abiblPolicyNumberFetcher, exchange, transactionInfo, abiblPolicyService);
		logger.info("*********** inside Camel Process of abiblPolicyNumberFetcher Processor Ended ************");
	}

	public void doProcess(AbiblPolicyNumberFetcher abiblPolicyNumberFetcher, Exchange exchange, TransactionInfo transactionInfo,
			AbiblPolicyService abiblPolicyService) throws MalformedURLException, IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("BEGIN : abiblPolicyNumberFetcher Processor - doProcess Method Called  ");

		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		logger.info("abiblPolicyNumberFetcher - connection object created :: " + conn);
		List<CommonPolicyTakenStatus> policyDateList = new ArrayList<CommonPolicyTakenStatus>();
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			 policyDateList = abiblPolicyService.getPendingPolicyDateListAbibl(UtilityFile.getCarPolicyProperty(RPAConstants.ABIBL_NORMAL_STARTDATE_YYFORMAT));
		}else{
			String startDate = UtilityFile.getCarPolicyProperty(RPAConstants.ABIBL_BACKLOG_STARTDATE_YYFORMAT);
			String endDate = UtilityFile.getCarPolicyProperty(RPAConstants.ABIBL_BACKLOG_ENDDATE_YYFORMAT);
			policyDateList = abiblPolicyService.getPendingBacklogPolicyDateListAbibl(startDate,endDate);
		}
			
			for (CommonPolicyTakenStatus commonPolicyTakenStatus : policyDateList) {
				logger.info("abiblPolicyNumberFetcher - policy to be fetched from maruti db for policy date :: " + commonPolicyTakenStatus.getPolicyDate());
				getPolicyNumberListFromAbiblDB(driver, transactionInfo, abiblPolicyService, exchange, commonPolicyTakenStatus, conn);
				break;
			}
		
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		if (conn != null)
			conn.close();

		logger.info("BEGIN : abiblPolicyNumberFetcher Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListFromAbiblDB(WebDriver driver, TransactionInfo transactionInfo,
			AbiblPolicyService abiblPolicyService, Exchange exchange, CommonPolicyTakenStatus commonPolicyTakenStatus, Connection conn) throws InstantiationException,
					IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("abiblPolicyNumberFetcher - BEGIN getPolicyNumberListFromAbiblDB()  called ");

		List<String> abiblPolicyNoLocalList = new ArrayList<String>();

		List<CarResponse> carReponseList = new ArrayList<CarResponse>();
		String sql = "";
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			abiblPolicyNoLocalList = abiblPolicyService.findExistingPolicyList(commonPolicyTakenStatus.getPolicyDate());
			sql = "select POLICY_NO,TRANSACTIONID proposal_no,to_char(TO_DATE(SUBSTR(RES_TIME,1,10),'DD/MM/YYYY'),'MM/DD/YYYY') from TTRN_ADITYA_NEWRENEWAL_SERVICE where POLICY_NO not like 'DUM%' and make ='AL' and TO_CHAR(TO_DATE(SUBSTR(RES_TIME,1,10),'DD-MM-YY'),'DD/MM/YYYY')= "
					+ "to_char(TO_DATE('"+commonPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'DD/MM/YYYY') and payment_yn='Y' ";
			logger.info("abiblPolicyNumberFetcher - sql query to fetch policies dated - "+commonPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
			
		} else if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			abiblPolicyNoLocalList = abiblPolicyService.findExistingBacklogPolicyList(commonPolicyTakenStatus.getPolicyDate());
			sql = "select POLICY_NO,TRANSACTIONID proposal_no,to_char(TO_DATE(SUBSTR(RES_TIME,1,10),'DD/MM/YYYY'),'MM/DD/YYYY') from TTRN_ADITYA_NEWRENEWAL_SERVICE where POLICY_NO not like 'DUM%' and make ='AL' and TO_CHAR(TO_DATE(SUBSTR(RES_TIME,1,10),'DD-MM-YY'),'DD/MM/YYYY')= "
					+ "to_char(TO_DATE('"+commonPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'DD/MM/YYYY') and payment_yn='Y' ";
			logger.info("abiblPolicyNumberFetcher - sql query to fetch backlog policies dated - "+commonPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
		}
		
		insertPoliciesInLocalDb(carReponseList,conn,sql,abiblPolicyNoLocalList,exchange,commonPolicyTakenStatus,abiblPolicyService);

		if (conn != null) {
			conn.close();
		}
		
		logger.info("abiblPolicyNumberFetcher - getPolicyNumberListFromAbiblDB()  END ");
		return true;
	}

	private void insertPoliciesInLocalDb(List<CarResponse> carReponseList, Connection conn, String sql, List<String> abiblPolicyNoLocalList,
			Exchange exchange, CommonPolicyTakenStatus commonPolicyTakenStatus, AbiblPolicyService abiblPolicyService) throws SQLException {
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

		logger.info("abiblPolicyNumberFetcher - policies count :: " + carReponseList.size());

		List<AbiblPolicy> newMarutiPolicyList = new ArrayList<AbiblPolicy>();
		AbiblPolicy fPolicy = null;
		for (CarResponse carResponse : carReponseList) {
			if (!abiblPolicyNoLocalList.contains(carResponse.getPolicyNo())) {
				fPolicy = new AbiblPolicy();
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
				fPolicy.setCarType(RPAConstants.OMNI_ABIBL_PREFIX);
				newMarutiPolicyList.add(fPolicy);
				fPolicy = null;
			}
		}
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			logger.info("abiblPolicyNumberFetcher - Newly fetched Policies for the date "+commonPolicyTakenStatus.getPolicyDate()+" :: " + newMarutiPolicyList.size());
		} else {
			logger.info("abiblPolicyNumberFetcher - Newly fetched Policies  count as backlog :: " + newMarutiPolicyList.size());
		}
		if(abiblPolicyService.save(newMarutiPolicyList)!=null){
			commonPolicyTakenStatus.setAbiblStatus(RPAConstants.Y);
			abiblPolicyService.save(commonPolicyTakenStatus);
		}
		
	}

}
