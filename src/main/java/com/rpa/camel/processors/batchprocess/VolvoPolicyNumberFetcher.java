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
import com.rpa.model.processors.VolvoPolicy;
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.response.CarResponse;
import com.rpa.service.processors.VolvoPolicyService;
import com.rpa.util.UtilityFile;

public class VolvoPolicyNumberFetcher implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(VolvoPolicyNumberFetcher.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	VolvoPolicyService volvoPolicyService;

	@Override
	public void process(Exchange exchange) throws URISyntaxException, MalformedURLException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		logger.info("*********** inside Camel Process of volvoPolicyNumberFetcher Called ************");
		logger.info("BEGIN : volvoPolicyNumberFetcher Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.FETCH_NUMBER);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		volvoPolicyService = applicationContext.getBean(VolvoPolicyService.class);
		VolvoPolicyNumberFetcher volvoPolicyNumberFetcher = new VolvoPolicyNumberFetcher();
		volvoPolicyNumberFetcher.doProcess(volvoPolicyNumberFetcher, exchange, transactionInfo, volvoPolicyService);
		logger.info("*********** inside Camel Process of volvoPolicyNumberFetcher Processor Ended ************");
	}

	public void doProcess(VolvoPolicyNumberFetcher volvoPolicyNumberFetcher, Exchange exchange, TransactionInfo transactionInfo,
			VolvoPolicyService volvoPolicyService) throws MalformedURLException, IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("BEGIN : volvoPolicyNumberFetcher Processor - doProcess Method Called  ");

		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		logger.info("volvoPolicyNumberFetcher - connection object created :: " + conn);
		List<CommonPolicyTakenStatus> policyDateList = new ArrayList<CommonPolicyTakenStatus>();
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			 policyDateList = volvoPolicyService.getPendingPolicyDateListVolvo(UtilityFile.getCarPolicyProperty(RPAConstants.VOLVO_NORMAL_STARTDATE_YYFORMAT));
		}else{
			String startDate = UtilityFile.getCarPolicyProperty(RPAConstants.VOLVO_BACKLOG_STARTDATE_YYFORMAT);
			String endDate = UtilityFile.getCarPolicyProperty(RPAConstants.VOLVO_BACKLOG_ENDDATE_YYFORMAT);
			policyDateList = volvoPolicyService.getPendingBacklogPolicyDateListVolvo(startDate,endDate);
		}
			
			for (CommonPolicyTakenStatus commonPolicyTakenStatus : policyDateList) {
				logger.info("volvoPolicyNumberFetcher - policy to be fetched from maruti db for policy date :: " + commonPolicyTakenStatus.getPolicyDate());
				getPolicyNumberListFromVolvoDB(driver, transactionInfo, volvoPolicyService, exchange, commonPolicyTakenStatus, conn);
				break;
			}
		
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		if (conn != null)
			conn.close();

		logger.info("BEGIN : volvoPolicyNumberFetcher Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListFromVolvoDB(WebDriver driver, TransactionInfo transactionInfo,
			VolvoPolicyService volvoPolicyService, Exchange exchange, CommonPolicyTakenStatus commonPolicyTakenStatus, Connection conn) throws InstantiationException,
					IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("volvoPolicyNumberFetcher - BEGIN getPolicyNumberListFromVolvoDB()  called ");

		List<String> volvoPolicyNoLocalList = new ArrayList<String>();

		List<CarResponse> carReponseList = new ArrayList<CarResponse>();
		String sql = "";
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			volvoPolicyNoLocalList = volvoPolicyService.findExistingPolicyList(commonPolicyTakenStatus.getPolicyDate());
			sql = "select POLICY_NO,PROPOSAL_NO proposal_no,to_char(TO_DATE(REQUEST_DATE,'DD/MM/YYYY'),'MM/DD/YYYY') from TTRN_volvo_NEWRENEWAL_SERVICE where POLICY_NO not like 'DUM%' and TO_CHAR(TO_DATE(REQUEST_DATE,'DD-MM-YY'),'DD/MM/YYYY')= "
			+ "to_char(TO_DATE('"+commonPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'DD/MM/YYYY') ";
			logger.info("volvoPolicyNumberFetcher - sql query to fetch policies dated - "+commonPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
		} else if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			volvoPolicyNoLocalList = volvoPolicyService.findExistingBacklogPolicyList(commonPolicyTakenStatus.getPolicyDate());
			sql = "select POLICY_NO,PROPOSAL_NO proposal_no,to_char(TO_DATE(REQUEST_DATE,'DD/MM/YYYY'),'MM/DD/YYYY') from TTRN_volvo_NEWRENEWAL_SERVICE where POLICY_NO not like 'DUM%' and TO_CHAR(TO_DATE(REQUEST_DATE,'DD-MM-YY'),'DD/MM/YYYY')= "
			+ "to_char(TO_DATE('"+commonPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'DD/MM/YYYY')";
			logger.info("volvoPolicyNumberFetcher - sql query to fetch backlog policies dated - "+commonPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
		}
		
		insertPoliciesInLocalDb(carReponseList,conn,sql,volvoPolicyNoLocalList,exchange,commonPolicyTakenStatus,volvoPolicyService);

		if (conn != null) {
			conn.close();
		}
		
		logger.info("volvoPolicyNumberFetcher - getPolicyNumberListFromVolvoDB()  END ");
		return true;
	}

	private void insertPoliciesInLocalDb(List<CarResponse> carReponseList, Connection conn, String sql, List<String> volvoPolicyNoLocalList,
			Exchange exchange, CommonPolicyTakenStatus commonPolicyTakenStatus, VolvoPolicyService volvoPolicyService) throws SQLException {
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

		logger.info("volvoPolicyNumberFetcher - policies count :: " + carReponseList.size());

		List<VolvoPolicy> newMarutiPolicyList = new ArrayList<VolvoPolicy>();
		VolvoPolicy fPolicy = null;
		for (CarResponse carResponse : carReponseList) {
			if (!volvoPolicyNoLocalList.contains(carResponse.getPolicyNo())) {
				fPolicy = new VolvoPolicy();
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
				fPolicy.setCarType(RPAConstants.OMNI_VOLVO_PREFIX);
				newMarutiPolicyList.add(fPolicy);
				fPolicy = null;
			}
		}
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			logger.info("volvoPolicyNumberFetcher - Newly fetched Policies for the date "+commonPolicyTakenStatus.getPolicyDate()+" :: " + newMarutiPolicyList.size());
		} else {
			logger.info("volvoPolicyNumberFetcher - Newly fetched Policies  count as backlog :: " + newMarutiPolicyList.size());
		}
		if(volvoPolicyService.save(newMarutiPolicyList)!=null){
			commonPolicyTakenStatus.setVolvoStatus(RPAConstants.Y);
			volvoPolicyService.save(commonPolicyTakenStatus);
		}
		
	}

}
