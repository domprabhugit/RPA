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
import com.rpa.model.processors.HondaPolicy;
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.response.CarResponse;
import com.rpa.service.processors.HondaPolicyService;
import com.rpa.util.UtilityFile;

public class HondaPolicyNumberFetcher implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(HondaPolicyNumberFetcher.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	HondaPolicyService hondaPolicyService;

	@Override
	public void process(Exchange exchange) throws URISyntaxException, MalformedURLException, IOException,
			InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		logger.info("*********** inside Camel Process of hondaPolicyNumberFetcher Called ************");
		logger.info("BEGIN : hondaPolicyNumberFetcher Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.FETCH_NUMBER);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		hondaPolicyService = applicationContext.getBean(HondaPolicyService.class);
		HondaPolicyNumberFetcher hondaPolicyNumberFetcher = new HondaPolicyNumberFetcher();
		hondaPolicyNumberFetcher.doProcess(hondaPolicyNumberFetcher, exchange, transactionInfo, hondaPolicyService);
		logger.info("*********** inside Camel Process of hondaPolicyNumberFetcher Processor Ended ************");
	}

	public void doProcess(HondaPolicyNumberFetcher hondaPolicyNumberFetcher, Exchange exchange,
			TransactionInfo transactionInfo, HondaPolicyService hondaPolicyService)
			throws MalformedURLException, IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("BEGIN : hondaPolicyNumberFetcher Processor - doProcess Method Called  ");

		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		logger.info("hondaPolicyNumberFetcher - connection object created :: " + conn);
		List<CommonPolicyTakenStatus> policyDateList = new ArrayList<CommonPolicyTakenStatus>();
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			policyDateList = hondaPolicyService.getPendingPolicyDateListHonda(
					UtilityFile.getCarPolicyProperty(RPAConstants.HONDA_NORMAL_STARTDATE_YYFORMAT));
		} else {
			String startDate = UtilityFile.getCarPolicyProperty(RPAConstants.HONDA_BACKLOG_STARTDATE_YYFORMAT);
			String endDate = UtilityFile.getCarPolicyProperty(RPAConstants.HONDA_BACKLOG_ENDDATE_YYFORMAT);
			policyDateList = hondaPolicyService.getPendingBacklogPolicyDateListHonda(startDate, endDate);
		}

		for (CommonPolicyTakenStatus marutiPolicyTakenStatus : policyDateList) {
			logger.info("hondaPolicyNumberFetcher - policy to be fetched from maruti db for policy date :: "
					+ marutiPolicyTakenStatus.getPolicyDate());
			getPolicyNumberListFromMarutiDB(driver, transactionInfo, hondaPolicyService, exchange,
					marutiPolicyTakenStatus, conn);
			break;
		}
		/*
		 * } else { getPolicyNumberListFromMarutiDB(driver, transactionInfo,
		 * hondaPolicyService, exchange, null, conn); }
		 */
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		if (conn != null)
			conn.close();

		logger.info("BEGIN : hondaPolicyNumberFetcher Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListFromMarutiDB(WebDriver driver, TransactionInfo transactionInfo,
			HondaPolicyService hondaPolicyService, Exchange exchange, CommonPolicyTakenStatus marutiPolicyTakenStatus,
			Connection conn) throws InstantiationException, IllegalAccessException, ClassNotFoundException,
			SQLException, URISyntaxException {
		logger.info("hondaPolicyNumberFetcher - BEGIN getPolicyNumberListFromMarutiDB()  called ");

		List<String> hondaPolicyNoLocalList = new ArrayList<String>();

		/*
		 * logger.info(
		 * "hondaPolicyNumberFetcher - already fetched Policy number count dated yesterday :: "
		 * + hondaPolicyNoLocalList.size());
		 */

		List<CarResponse> carReponseList = new ArrayList<CarResponse>();
		String sql = "";
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			hondaPolicyNoLocalList = hondaPolicyService.findExistingPolicyList(marutiPolicyTakenStatus.getPolicyDate());
			/*sql = "select vv.POLICYNO,vv.HAPROPOSALNO proposal_no,to_char(TO_DATE(SUBSTR(jj.REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/DD/YYYY') from ttrn_newrenewal vv,ttrn_newrenewal_service jj where vv.POLICYNO not like 'DUM%' "
					+ "and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM-DD-YY'),'MM/DD/YYYY')= to_char(TO_DATE('"
					+ marutiPolicyTakenStatus.getPolicyDate()
					+ "','DD-MM-YY'),'MM/DD/YYYY') and vv.POLICYNO=JJ.ICPOLICYNO "
					+ "union select vv.POLICYNO,vv.HAPROPOSALNO proposal_no,to_char(TO_DATE(SUBSTR(jj.REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/DD/YYYY') from ttrn_newrenewal_raw vv,ttrn_newrenewal_service jj "
					+ "where vv.POLICYNO not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM-DD-YY'),'MM/DD/YYYY')= to_char(TO_DATE('"
					+ marutiPolicyTakenStatus.getPolicyDate()
					+ "','DD-MM-YY'),'MM/DD/YYYY') and vv.POLICYNO=JJ.ICPOLICYNO";*/
			
			sql = "select icpolicyno,HAPROPOSALNO proposal_no,to_char(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/DD/YYYY') from ttrn_newrenewal_service a where ICPOLICYNO not like 'DUM%' "
					+ "and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM-DD-YY'),'MM/DD/YYYY')= to_char(TO_DATE('"
					+ marutiPolicyTakenStatus.getPolicyDate()
					+ "','DD-MM-YY'),'MM/DD/YYYY')";

			logger.info("hondaPolicyNumberFetcher - sql query to fetch policies dated - "
					+ marutiPolicyTakenStatus.getPolicyDate() + " :: " + sql);

		} else if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			hondaPolicyNoLocalList = hondaPolicyService
					.findExistingBacklogPolicyList(marutiPolicyTakenStatus.getPolicyDate());
			sql = "select icpolicyno,HAPROPOSALNO proposal_no,to_char(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/DD/YYYY') from ttrn_newrenewal_service a where ICPOLICYNO not like 'DUM%' "
					+ "and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM-DD-YY'),'MM/DD/YYYY')= to_char(TO_DATE('"
					+ marutiPolicyTakenStatus.getPolicyDate()
					+ "','DD-MM-YY'),'MM/DD/YYYY') ";
			logger.info("hondaPolicyNumberFetcher - sql query to fetch backlog policies dated - "
					+ marutiPolicyTakenStatus.getPolicyDate() + " :: " + sql);
		}

		insertPoliciesInLocalDb(carReponseList, conn, sql, hondaPolicyNoLocalList, exchange, marutiPolicyTakenStatus,
				hondaPolicyService);

		if (conn != null) {
			conn.close();
		}

		logger.info("hondaPolicyNumberFetcher - getPolicyNumberListFromMarutiDB()  END ");
		return true;
	}

	private void insertPoliciesInLocalDb(List<CarResponse> carReponseList, Connection conn, String sql,
			List<String> hondaPolicyNoLocalList, Exchange exchange, CommonPolicyTakenStatus marutiPolicyTakenStatus,
			HondaPolicyService hondaPolicyService) throws SQLException {
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

		logger.info("hondaPolicyNumberFetcher - policies count :: " + carReponseList.size());

		List<HondaPolicy> newMarutiPolicyList = new ArrayList<HondaPolicy>();
		HondaPolicy hPolicy = null;
		for (CarResponse carResponse : carReponseList) {
			if (!hondaPolicyNoLocalList.contains(carResponse.getPolicyNo())) {
				hPolicy = new HondaPolicy();
				hPolicy.setIsPolicyUploaded(RPAConstants.N);
				hPolicy.setIsProposalUploaded(RPAConstants.N);
				hPolicy.setIsPolicyDownloaded(RPAConstants.N);
				hPolicy.setIsProposalDownloaded(RPAConstants.N);
				hPolicy.setPolicyNo(carResponse.getPolicyNo());
				hPolicy.setProposalNumber(carResponse.getProposalNumber());

				hPolicy.setPolicyDate(carResponse.getPolicyDate());
				if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
					hPolicy.setBackLogFlag(RPAConstants.Y);
				} else {
					hPolicy.setBackLogFlag(RPAConstants.N);
				}
				hPolicy.setCarType(RPAConstants.OMNI_HONDA_PREFIX);
				newMarutiPolicyList.add(hPolicy);
				hPolicy = null;
			}
		}
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			logger.info("hondaPolicyNumberFetcher - Newly fetched Policies for the date "
					+ marutiPolicyTakenStatus.getPolicyDate() + " :: " + newMarutiPolicyList.size());
		} else {
			logger.info("hondaPolicyNumberFetcher - Newly fetched Policies  count as backlog :: "
					+ newMarutiPolicyList.size());
		}
		if (hondaPolicyService.save(newMarutiPolicyList) != null) {
			marutiPolicyTakenStatus.setHondaStatus(RPAConstants.Y);
			hondaPolicyService.save(marutiPolicyTakenStatus);
		}

	}

}
