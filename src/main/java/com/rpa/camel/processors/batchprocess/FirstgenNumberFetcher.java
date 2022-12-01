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
import com.rpa.model.processors.FirstgenPolicy;
import com.rpa.response.CarResponse;
import com.rpa.service.processors.FirstGenDownloadPolicyService;
import com.rpa.util.UtilityFile;

public class FirstgenNumberFetcher implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(FirstgenNumberFetcher.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	FirstGenDownloadPolicyService firstGenDownloadService;

	@Override
	public void process(Exchange exchange) throws URISyntaxException, MalformedURLException, IOException,
			InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		logger.info("*********** inside Camel Process of firstgenNumberFetcher Called ************");
		logger.info("BEGIN : firstgenNumberFetcher Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.FETCH_NUMBER);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		firstGenDownloadService = applicationContext.getBean(FirstGenDownloadPolicyService.class);
		FirstgenNumberFetcher firstgenNumberFetcher = new FirstgenNumberFetcher();
		firstgenNumberFetcher.doProcess(firstgenNumberFetcher, exchange, transactionInfo, firstGenDownloadService);
		logger.info("*********** inside Camel Process of firstgenNumberFetcher Processor Ended ************");
	}

	public void doProcess(FirstgenNumberFetcher firstgenNumberFetcher, Exchange exchange,
			TransactionInfo transactionInfo, FirstGenDownloadPolicyService firstGenDownloadService)
			throws MalformedURLException, IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("BEGIN : firstgenNumberFetcher Processor - doProcess Method Called  ");

		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DATASOURCE_PASSWORD);
		try{
		conn = DriverManager.getConnection(connInstance, username, password);
		logger.info("firstgenNumberFetcher - connection object created :: " + conn);

			getPolicyNumberListFromMarutiDB( driver,  transactionInfo,
					 firstGenDownloadService, exchange,
					 conn);
			
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		}finally{
			if (conn != null)
			conn.close();
		}
		logger.info("BEGIN : firstgenNumberFetcher Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListFromMarutiDB(WebDriver driver, TransactionInfo transactionInfo,
			FirstGenDownloadPolicyService firstGenDownloadService, Exchange exchange,
			Connection conn) throws InstantiationException, IllegalAccessException, ClassNotFoundException,
			SQLException, URISyntaxException {
		logger.info("firstgenNumberFetcher - BEGIN getPolicyNumberListFromMarutiDB()  called ");

		List<String> hondaPolicyNoLocalList = new ArrayList<String>();
		hondaPolicyNoLocalList = firstGenDownloadService.findExistingPolicyList(UtilityFile.createSpecifiedDateFormat(RPAConstants.dd_slash_MM_slash_yyyy));

		List<CarResponse> carReponseList = new ArrayList<CarResponse>();
		String sql = "select IUWP1_POL_NO ,to_char(to_date (h.IUWP1_ISS_DT + 2378496, 'j' ),'mm/dd/yyyy') AS policy_issue_date from IUMPL1A@portal h where h.IUWP1_CAMP_CD in ('DS06','DS07') and h.IUWP1_ACC_CD='AG020227' and h.IUWP1_POL_ENDT_TXT='Renewal' AND to_char(to_date (h.IUWP1_ISS_DT + 2378496, 'j' ),'dd-mm-yyyy')=to_char(to_date(sysdate-1,'dd-mm-yy'),'dd-mm-yyyy') ";

			logger.info("firstgenNumberFetcher - sql query to fetch policies for today's date :: " + sql);


		insertPoliciesInLocalDb(carReponseList, conn, sql, hondaPolicyNoLocalList, exchange,
				firstGenDownloadService);

		logger.info("firstgenNumberFetcher - getPolicyNumberListFromMarutiDB()  END ");
		return true;
	}

	private void insertPoliciesInLocalDb(List<CarResponse> carReponseList, Connection conn, String sql,
			List<String> hondaPolicyNoLocalList, Exchange exchange,
			FirstGenDownloadPolicyService firstGenDownloadService) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					CarResponse carReponse = new CarResponse();
					carReponse.setPolicyNo(rs.getString(1));
					carReponse.setPolicyDate(rs.getString(2));
					carReponseList.add(carReponse);
					carReponse = null;
				}
				if (rs != null)
					rs.close();
			}
		}

		logger.info("firstgenNumberFetcher - policies count :: " + carReponseList.size());

		List<FirstgenPolicy> newMarutiPolicyList = new ArrayList<FirstgenPolicy>();
		FirstgenPolicy hPolicy = null;
		for (CarResponse carResponse : carReponseList) {
			if (!hondaPolicyNoLocalList.contains(carResponse.getPolicyNo())) {
				hPolicy = new FirstgenPolicy();
				hPolicy.setIsPolicyUploaded(RPAConstants.N);
				hPolicy.setIsInvoiceUploaded(RPAConstants.N);
				hPolicy.setIsPolicyDownloaded(RPAConstants.N);
				hPolicy.setIsInvoiceDownloaded(RPAConstants.N);
				hPolicy.setBackLogFlag(RPAConstants.N);
				hPolicy.setIsPdfMerged(RPAConstants.N);
				hPolicy.setPolicyNo(carResponse.getPolicyNo());
				hPolicy.setCarType(RPAConstants.OMNI_FIRSTGEN_PREFIX);
				hPolicy.setPolicyDate(carResponse.getPolicyDate());
				newMarutiPolicyList.add(hPolicy);
				hPolicy = null;
			}
		}
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			logger.info("firstgenNumberFetcher - Newly fetched Policies for today's date :: " + newMarutiPolicyList.size());
		} 
		firstGenDownloadService.save(newMarutiPolicyList);

	}

}
