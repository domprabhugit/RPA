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
import com.rpa.model.processors.TataPolicy;
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.response.CarResponse;
import com.rpa.service.processors.TataPolicyService;
import com.rpa.util.UtilityFile;

public class TataPolicyNumberFetcher implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(TataPolicyNumberFetcher.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	TataPolicyService tataPolicyService;

	@Override
	public void process(Exchange exchange) throws URISyntaxException, MalformedURLException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		logger.info("*********** inside Camel Process of tataPolicyNumberFetcher Called ************");
		logger.info("BEGIN : tataPolicyNumberFetcher Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.FETCH_NUMBER);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		tataPolicyService = applicationContext.getBean(TataPolicyService.class);
		TataPolicyNumberFetcher tataPolicyNumberFetcher = new TataPolicyNumberFetcher();
		tataPolicyNumberFetcher.doProcess(tataPolicyNumberFetcher, exchange, transactionInfo, tataPolicyService);
		logger.info("*********** inside Camel Process of tataPolicyNumberFetcher Processor Ended ************");
	}

	public void doProcess(TataPolicyNumberFetcher tataPolicyNumberFetcher, Exchange exchange, TransactionInfo transactionInfo,
			TataPolicyService tataPolicyService) throws MalformedURLException, IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("BEGIN : tataPolicyNumberFetcher Processor - doProcess Method Called  ");

		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		logger.info("tataPolicyNumberFetcher - connection object created :: " + conn);
		List<CommonPolicyTakenStatus> policyDateList = new ArrayList<CommonPolicyTakenStatus>();
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
				policyDateList = tataPolicyService.getPendingPolicyDateListTataPV(UtilityFile.getCarPolicyProperty(RPAConstants.TATA_NORMAL_STARTDATE_YYFORMAT));
			else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
				policyDateList = tataPolicyService.getPendingPolicyDateListTataCV(UtilityFile.getCarPolicyProperty(RPAConstants.TATA_NORMAL_STARTDATE_YYFORMAT));
		}else{
			String startDate = UtilityFile.getCarPolicyProperty(RPAConstants.TATA_BACKLOG_STARTDATE_YYFORMAT);
			String endDate = UtilityFile.getCarPolicyProperty(RPAConstants.TATA_BACKLOG_ENDDATE_YYFORMAT);
			if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV))
				policyDateList = tataPolicyService.getPendingBacklogPolicyDateListTataPV(startDate,endDate);
			else if(exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV))
				policyDateList = tataPolicyService.getPendingBacklogPolicyDateListTataCV(startDate,endDate);
		}
			
			for (CommonPolicyTakenStatus marutiPolicyTakenStatus : policyDateList) {
				logger.info("tataPolicyNumberFetcher - policy to be fetched from maruti db for policy date :: " + marutiPolicyTakenStatus.getPolicyDate());
				getPolicyNumberListFromTataDB(driver, transactionInfo, tataPolicyService, exchange, marutiPolicyTakenStatus, conn);
				break;
			}
		/*} else {
				getPolicyNumberListFromTataDB(driver, transactionInfo, tataPolicyService, exchange, null, conn);
		}*/
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		if (conn != null)
			conn.close();

		logger.info("BEGIN : tataPolicyNumberFetcher Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListFromTataDB(WebDriver driver, TransactionInfo transactionInfo,
			TataPolicyService tataPolicyService, Exchange exchange, CommonPolicyTakenStatus marutiPolicyTakenStatus, Connection conn) throws InstantiationException,
					IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("tataPolicyNumberFetcher - BEGIN getPolicyNumberListFromTataDB()  called ");

		List<String> tataPolicyNoLocalList = new ArrayList<String>();

		List<CarResponse> carReponseList = new ArrayList<CarResponse>();
		String sql = "";
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			if (exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)) {
				tataPolicyNoLocalList = tataPolicyService.findExistingPolicyListPV(marutiPolicyTakenStatus.getPolicyDate());
				sql = "select POLICYNO,TRANSACTIONID proposal_no,to_char(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/DD/YYYY'),vehicletype from ttrn_TATA_newservice  where payment_yn='Y' and POLICYNO not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM-DD-YY'),'MM/DD/YYYY')= "
						+ "to_char(TO_DATE('"+marutiPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'MM/DD/YYYY') and VEHICLETYPE in('PCP') ";
				insertPVPoliciesInLocalDb(carReponseList,conn,sql,tataPolicyNoLocalList,exchange,marutiPolicyTakenStatus,tataPolicyService);
			}else if (exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)) {
				tataPolicyNoLocalList = tataPolicyService.findExistingPolicyListCV(marutiPolicyTakenStatus.getPolicyDate());
				sql = "select POLICYNO,TRANSACTIONID proposal_no,to_char(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/DD/YYYY'),vehicletype from ttrn_TATA_newservice  where payment_yn='Y' and POLICYNO not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM-DD-YY'),'MM/DD/YYYY')= "
						+ "to_char(TO_DATE('"+marutiPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'MM/DD/YYYY') and VEHICLETYPE in('GCV','PCV','MIS') ";
				insertCVPoliciesInLocalDb(carReponseList,conn,sql,tataPolicyNoLocalList,exchange,marutiPolicyTakenStatus,tataPolicyService);
			}
			logger.info("tataPolicyNumberFetcher - sql query to fetch policies dated - "+marutiPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
			
		} else if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			if (exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)) {
				tataPolicyNoLocalList = tataPolicyService.findExistingBacklogPolicyListPV(marutiPolicyTakenStatus.getPolicyDate());
				sql = "select POLICYNO,TRANSACTIONID proposal_no,to_char(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/DD/YYYY'),vehicletype from ttrn_TATA_newservice  where payment_yn='Y' and POLICYNO not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM-DD-YY'),'MM/DD/YYYY')= "
						+ "to_char(TO_DATE('"+marutiPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'MM/DD/YYYY') and VEHICLETYPE in('PCP') ";
				insertPVPoliciesInLocalDb(carReponseList,conn,sql,tataPolicyNoLocalList,exchange,marutiPolicyTakenStatus,tataPolicyService);
			}else if (exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)) {
				tataPolicyNoLocalList = tataPolicyService.findExistingBacklogPolicyListCV(marutiPolicyTakenStatus.getPolicyDate());
				sql = "select POLICYNO,TRANSACTIONID proposal_no,to_char(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/DD/YYYY'),vehicletype from ttrn_TATA_newservice  where payment_yn='Y' and POLICYNO not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM-DD-YY'),'MM/DD/YYYY')= "
						+ "to_char(TO_DATE('"+marutiPolicyTakenStatus.getPolicyDate()+"','DD-MM-YY'),'MM/DD/YYYY') and VEHICLETYPE in('GCV','PCV','MIS') ";
				insertCVPoliciesInLocalDb(carReponseList,conn,sql,tataPolicyNoLocalList,exchange,marutiPolicyTakenStatus,tataPolicyService);
			}
			logger.info("tataPolicyNumberFetcher - sql query to fetch backlog policies dated - "+marutiPolicyTakenStatus.getPolicyDate()+" :: "+ sql);
		}

		if (conn != null) {
			conn.close();
		}
		
		logger.info("tataPolicyNumberFetcher - getPolicyNumberListFromTataDB()  END ");
		return true;
	}

	
	private void insertPVPoliciesInLocalDb(List<CarResponse> carReponseList, Connection conn, String sql, List<String> tataPolicyNoLocalList,
			Exchange exchange, CommonPolicyTakenStatus marutiPolicyTakenStatus, TataPolicyService tataPolicyService) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					CarResponse carReponse = new CarResponse();
					carReponse.setPolicyNo(rs.getString(1));
					carReponse.setProposalNumber(rs.getString(2));
					carReponse.setPolicyDate(rs.getString(3));
					carReponse.setVehicleType(rs.getString(4));
					carReponseList.add(carReponse);
					carReponse = null;
				}
				if (rs != null)
					rs.close();
			}
		}

		logger.info("tataPolicyNumberFetcher - policies count :: " + carReponseList.size());

		List<TataPolicy> newTaataPolicyList = new ArrayList<TataPolicy>();
		TataPolicy tPolicy = null;
		for (CarResponse carResponse : carReponseList) {
			if (!tataPolicyNoLocalList.contains(carResponse.getPolicyNo())) {
				tPolicy = new TataPolicy();
				tPolicy.setIsPolicyUploaded(RPAConstants.N);
				tPolicy.setIsProposalUploaded(RPAConstants.N);
				tPolicy.setIsPolicyDownloaded(RPAConstants.N);
				tPolicy.setIsProposalDownloaded(RPAConstants.N);
				tPolicy.setPolicyNo(carResponse.getPolicyNo());
				tPolicy.setProposalNumber(carResponse.getProposalNumber());
				tPolicy.setVehicletype(carResponse.getVehicleType());

				tPolicy.setPolicyDate(carResponse.getPolicyDate());
				if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
					tPolicy.setBackLogFlag(RPAConstants.Y);
				} else {
					tPolicy.setBackLogFlag(RPAConstants.N);
				}
				tPolicy.setCarType(RPAConstants.OMNI_TATA_PREFIX);
				newTaataPolicyList.add(tPolicy);
				tPolicy = null;
			}
		}
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			logger.info("tataPolicyNumberFetcher - Newly fetched Policies for the date "+marutiPolicyTakenStatus.getPolicyDate()+" :: " + newTaataPolicyList.size());
		} else {
			logger.info("tataPolicyNumberFetcher - Newly fetched Policies  count as backlog :: " + newTaataPolicyList.size());
		}
		if(tataPolicyService.save(newTaataPolicyList)!=null){
			if (exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)) 
				marutiPolicyTakenStatus.setTataPVStatus(RPAConstants.Y);
			else if (exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)) 
				marutiPolicyTakenStatus.setTataCVStatus(RPAConstants.Y);
			tataPolicyService.save(marutiPolicyTakenStatus);
		}
		
	}
	
	private void insertCVPoliciesInLocalDb(List<CarResponse> carReponseList, Connection conn, String sql, List<String> tataPolicyNoLocalList,
			Exchange exchange, CommonPolicyTakenStatus marutiPolicyTakenStatus, TataPolicyService tataPolicyService) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					CarResponse carReponse = new CarResponse();
					carReponse.setPolicyNo(rs.getString(1));
					carReponse.setProposalNumber(rs.getString(2));
					carReponse.setPolicyDate(rs.getString(3));
					carReponse.setVehicleType(rs.getString(4));
					carReponseList.add(carReponse);
					carReponse = null;
				}
				if (rs != null)
					rs.close();
			}
		}

		logger.info("tataPolicyNumberFetcher - policies count :: " + carReponseList.size());

		List<TataPolicy> newTaataPolicyList = new ArrayList<TataPolicy>();
		TataPolicy tPolicy = null;
		for (CarResponse carResponse : carReponseList) {
			if (!tataPolicyNoLocalList.contains(carResponse.getPolicyNo())) {
				tPolicy = new TataPolicy();
				tPolicy.setIsPolicyUploaded(RPAConstants.N);
				tPolicy.setIsProposalUploaded(RPAConstants.N);
				tPolicy.setIsPolicyDownloaded(RPAConstants.N);
				tPolicy.setIsProposalDownloaded(RPAConstants.N);
				tPolicy.setPolicyNo(carResponse.getPolicyNo());
				tPolicy.setProposalNumber(carResponse.getProposalNumber());
				tPolicy.setVehicletype(carResponse.getVehicleType());

				tPolicy.setPolicyDate(carResponse.getPolicyDate());
				if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
					tPolicy.setBackLogFlag(RPAConstants.Y);
				} else {
					tPolicy.setBackLogFlag(RPAConstants.N);
				}
				tPolicy.setCarType(RPAConstants.OMNI_TATA_PREFIX);
				newTaataPolicyList.add(tPolicy);
				tPolicy = null;
			}
		}
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			logger.info("tataPolicyNumberFetcher - Newly fetched Policies for the date "+marutiPolicyTakenStatus.getPolicyDate()+" :: " + newTaataPolicyList.size());
		} else {
			logger.info("tataPolicyNumberFetcher - Newly fetched Policies  count as backlog :: " + newTaataPolicyList.size());
		}
		if(tataPolicyService.save(newTaataPolicyList)!=null){
			if (exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.PV)) 
				marutiPolicyTakenStatus.setTataPVStatus(RPAConstants.Y);
			else if (exchange.getProperty(RPAConstants.VEHICLE_TYPE).equals(RPAConstants.CV)) 
				marutiPolicyTakenStatus.setTataCVStatus(RPAConstants.Y);
			tataPolicyService.save(marutiPolicyTakenStatus);
		}
		
	}

}
