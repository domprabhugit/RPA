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
import com.rpa.model.processors.MiblPolicy;
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.response.CarResponse;
import com.rpa.service.processors.MiblPolicyService;
import com.rpa.util.UtilityFile;

public class MiblPolicyNumberFetcher implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(MiblPolicyNumberFetcher.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	MiblPolicyService miblPolicyService;

	@Override
	public void process(Exchange exchange) throws URISyntaxException, MalformedURLException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		logger.info("*********** inside Camel Process of miblPolicyNumberFetcher Called ************");
		logger.info("BEGIN : miblPolicyNumberFetcher Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.FETCH_NUMBER);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		miblPolicyService = applicationContext.getBean(MiblPolicyService.class);
		MiblPolicyNumberFetcher miblPolicyNumberFetcher = new MiblPolicyNumberFetcher();
		miblPolicyNumberFetcher.doProcess(miblPolicyNumberFetcher, exchange, transactionInfo, miblPolicyService);
		logger.info("*********** inside Camel Process of miblPolicyNumberFetcher Processor Ended ************");
	}

	public void doProcess(MiblPolicyNumberFetcher miblPolicyNumberFetcher, Exchange exchange, TransactionInfo transactionInfo,
			MiblPolicyService miblPolicyService) throws MalformedURLException, IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("BEGIN : miblPolicyNumberFetcher Processor - doProcess Method Called  ");

		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		logger.info("miblPolicyNumberFetcher - connection object created :: " + conn);
		List<CommonPolicyTakenStatus> policyDateList = new ArrayList<CommonPolicyTakenStatus>();
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			 policyDateList = miblPolicyService.getPendingPolicyDateListMibl(UtilityFile.getCarPolicyProperty(RPAConstants.MIBL_NORMAL_STARTDATE_YYFORMAT));
		}else{
			String startDate = UtilityFile.getCarPolicyProperty(RPAConstants.MIBL_BACKLOG_STARTDATE_YYFORMAT);
			String endDate = UtilityFile.getCarPolicyProperty(RPAConstants.MIBL_BACKLOG_ENDDATE_YYFORMAT);
			policyDateList = miblPolicyService.getPendingBacklogPolicyDateListMibl(startDate,endDate);
		}
			
			for (CommonPolicyTakenStatus commonPolicyTakenStatus : policyDateList) {
				logger.info("miblPolicyNumberFetcher - policy to be fetched from maruti db for policy date :: " + commonPolicyTakenStatus.getPolicyDate());
				getPolicyNumberListFromMiblDB(driver, transactionInfo, miblPolicyService, exchange, commonPolicyTakenStatus, conn);
				break;
			}
		
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		if (conn != null)
			conn.close();

		logger.info("BEGIN : miblPolicyNumberFetcher Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListFromMiblDB(WebDriver driver, TransactionInfo transactionInfo,
			MiblPolicyService miblPolicyService, Exchange exchange, CommonPolicyTakenStatus commonPolicyTakenStatus, Connection conn) throws InstantiationException,
					IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("miblPolicyNumberFetcher - BEGIN getPolicyNumberListFromMiblDB()  called ");

		List<String> miblPolicyNoLocalList = new ArrayList<String>();

		List<CarResponse> carReponseList = new ArrayList<CarResponse>();
		String sql = "";
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			miblPolicyNoLocalList = miblPolicyService.findExistingPolicyList(commonPolicyTakenStatus.getPolicyDate());
			sql = "select policyno POLICY_NO,TRANSACTIONID proposal_no,to_char(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'DD/MM/YYYY'),'MM/DD/YYYY') from TTRN_MIBL_NEWRENEWAL_SERVICE where policyno not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'DD-MM-YY'),'DD/MM/YYYY')= "
					+ "to_char(TO_DATE('"+commonPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'DD/MM/YYYY') and payment_yn='Y' ";
			logger.info("miblPolicyNumberFetcher - sql query to fetch policies dated - "+commonPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
			
		} else if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			miblPolicyNoLocalList = miblPolicyService.findExistingBacklogPolicyList(commonPolicyTakenStatus.getPolicyDate());
			sql = "select POLICY_NO,TRANSACTIONID proposal_no,to_char(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'DD/MM/YYYY'),'MM/DD/YYYY') from TTRN_MIBL_NEWRENEWAL_SERVICE where policyno not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'DD-MM-YY'),'DD/MM/YYYY')= "
					+ "to_char(TO_DATE('"+commonPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'DD/MM/YYYY') and payment_yn='Y' ";
			logger.info("miblPolicyNumberFetcher - sql query to fetch backlog policies dated - "+commonPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
		}
		
		insertPoliciesInLocalDb(carReponseList,conn,sql,miblPolicyNoLocalList,exchange,commonPolicyTakenStatus,miblPolicyService);

		if (conn != null) {
			conn.close();
		}
		
		logger.info("miblPolicyNumberFetcher - getPolicyNumberListFromMiblDB()  END ");
		return true;
	}

	private void insertPoliciesInLocalDb(List<CarResponse> carReponseList, Connection conn, String sql, List<String> miblPolicyNoLocalList,
			Exchange exchange, CommonPolicyTakenStatus commonPolicyTakenStatus, MiblPolicyService miblPolicyService) throws SQLException {
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

		logger.info("miblPolicyNumberFetcher - policies count :: " + carReponseList.size());

		List<MiblPolicy> newMarutiPolicyList = new ArrayList<MiblPolicy>();
		MiblPolicy fPolicy = null;
		for (CarResponse carResponse : carReponseList) {
			if (!miblPolicyNoLocalList.contains(carResponse.getPolicyNo())) {
				fPolicy = new MiblPolicy();
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
				fPolicy.setCarType(RPAConstants.OMNI_MIBL_PREFIX);
				newMarutiPolicyList.add(fPolicy);
				fPolicy = null;
			}
		}
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			logger.info("miblPolicyNumberFetcher - Newly fetched Policies for the date "+commonPolicyTakenStatus.getPolicyDate()+" :: " + newMarutiPolicyList.size());
		} else {
			logger.info("miblPolicyNumberFetcher - Newly fetched Policies  count as backlog :: " + newMarutiPolicyList.size());
		}
		if(miblPolicyService.save(newMarutiPolicyList)!=null){
			commonPolicyTakenStatus.setMiblStatus(RPAConstants.Y);
			miblPolicyService.save(commonPolicyTakenStatus);
		}
		
	}

}
