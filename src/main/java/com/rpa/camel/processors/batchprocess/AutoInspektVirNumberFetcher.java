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
import java.util.Arrays;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.AutoInspektVIR;
import com.rpa.model.processors.CommonReportTakenStatus;
import com.rpa.response.VirResponse;
import com.rpa.service.processors.AutoInspektVirService;
import com.rpa.util.UtilityFile;

public class AutoInspektVirNumberFetcher implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(AutoInspektVirNumberFetcher.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;

	@Autowired
	AutoInspektVirService autoInspektVirService;
	
	@Autowired
	private Environment environment;

	@Override
	public void process(Exchange exchange) throws URISyntaxException, MalformedURLException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		logger.info("*********** inside Camel Process of virAutoInspektNumberFetcher Called ************");
		logger.info("BEGIN : virAutoInspektNumberFetcher Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.FETCH_NUMBER);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		autoInspektVirService = applicationContext.getBean(AutoInspektVirService.class);
		AutoInspektVirNumberFetcher virAutoInspektNumberFetcher = new AutoInspektVirNumberFetcher();
		environment = applicationContext.getBean(Environment.class);
		virAutoInspektNumberFetcher.doProcess(virAutoInspektNumberFetcher, exchange, transactionInfo, autoInspektVirService,environment);
		logger.info("*********** inside Camel Process of virAutoInspektNumberFetcher Processor Ended ************");
	}

	public void doProcess(AutoInspektVirNumberFetcher virAutoInspektNumberFetcher, Exchange exchange, TransactionInfo transactionInfo,
			AutoInspektVirService autoInspektVirService, Environment environment) throws MalformedURLException, IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("BEGIN : virAutoInspektNumberFetcher Processor - doProcess Method Called  ");

		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		logger.info("virAutoInspektNumberFetcher - connection object created :: " + conn);
		List<CommonReportTakenStatus> policyDateList = new ArrayList<CommonReportTakenStatus>();
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			 policyDateList = autoInspektVirService.getPendingPolicyDateListAutoInspekt(UtilityFile.getVIRProperty(RPAConstants.AUTOINSPEKT_NORMAL_STARTDATE_YYFORMAT));
		}else{
			String startDate = UtilityFile.getVIRProperty(RPAConstants.AUTOINSPEKT_BACKLOG_STARTDATE_YYFORMAT);
			String endDate = UtilityFile.getVIRProperty(RPAConstants.AUTOINSPEKT_BACKLOG_ENDDATE_YYFORMAT);
			policyDateList = autoInspektVirService.getPendingBacklogPolicyDateListAutoInspekt(startDate,endDate);
		}
			
			for (CommonReportTakenStatus commonReportTakenStatus : policyDateList) {
				logger.info("virAutoInspektNumberFetcher - policy to be fetched from maruti db for policy date :: " + commonReportTakenStatus.getInspectionDate());
				getPolicyNumberListFromPiaggioDB(driver, transactionInfo, autoInspektVirService, exchange, commonReportTakenStatus, conn,environment);
				break;
			}
		
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		if (conn != null)
			conn.close();

		logger.info("BEGIN : virAutoInspektNumberFetcher Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListFromPiaggioDB(WebDriver driver, TransactionInfo transactionInfo,
			AutoInspektVirService autoInspektVirService, Exchange exchange, CommonReportTakenStatus commonReportTakenStatus, Connection conn, Environment environment) throws InstantiationException,
					IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("virAutoInspektNumberFetcher - BEGIN getPolicyNumberListFromPiaggioDB()  called ");

		List<String> autoInspectLocalList = new ArrayList<String>();

		List<VirResponse> carReponseList = new ArrayList<VirResponse>();
		String sql = "";
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			autoInspectLocalList = autoInspektVirService.findExistingPolicyList(commonReportTakenStatus.getInspectionDate());
			if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("local")))) {
				sql = "select policycode,PROPOSARCODE,INWARDCODE, REGISTRATIONNUMBER , CHASSISNO, ENGINENO, SERVICE_PROVIDERNAME , VIR_NUMBER , to_char(TO_DATE(SUBSTR(INSPECTIONDATE,1,10),'DD/MM/YYYY'),'MM/DD/YYYY') INSPECTIONDATE,dt.PROPOSARCODE,dm.INWARDCODE  from VIR_SERVICE "
						+ "where to_char(TO_DATE(SUBSTR(INSPECTIONDATE,1,10),'DD/MM/YYYY'),'DD/MM/YYYY') = to_char(TO_DATE('"+commonReportTakenStatus.getInspectionDate()+"','DD-MM-YY'),'DD/MM/YYYY') and SERVICE_PROVIDERNAME like '%Mahindra First Choice%' ";
			}else{
				sql = "select ins.policycode,dt.PROPOSARCODE,dm.INWARDCODE, nvl(ppc.REGISTRATIONNUMBER ,nvl(cv.REGISTRATIONNUMBER ,mc.REGISTRATIONNO ))REGISTRATIONNUMBER, nvl(ppc.CHASSISNO,nvl(cv.CHASSISNO,mc.CHASSISNO))CHASSISNO, nvl(ppc.ENGINENO,nvl(cv.ENGINENO,mc.ENGINENO))ENGINENO, nvl(ppc.SERVICE_PROVIDERNAME,nvl(cv.SERVICE_PROVIDERNAME,mc.SERVICE_PROVIDERNAME))SERVICE_PROVIDERNAME, nvl(ppc.VIR_NUMBER,nvl(cv.VIR_NUMBER,mc.VIR_NUMBER))VIR_NUMBER, to_char(TO_DATE(SUBSTR( nvl(ppc.INSPECTIONDATE,nvl(cv.INSPECTIONDATE,mc.INSPECTIONDATE)) )),1,10),'DD/MM/YYYY'),'MM/DD/YYYY') INSPECTIONDATE from insurancepolicy ins, motorcycle mc,privatepassengercar ppc,commercialvehicle cv, vir_agency v "
						+ "where ins.id = ppc.id(+) and ins.id = cv.id(+) and ins.id = mc.id(+)  and islatest =1 and ins.businessstatus != 'garbage' and nvl(nvl(ppc.SERVICE_PROVIDERNAME,nvl(cv.SERVICE_PROVIDERNAME,mc.SERVICE_PROVIDERNAME)),'NO VIR DONE')=v.MAIL AND to_char(TO_DATE(SUBSTR(nvl(ppc.INSPECTIONDATE,nvl(cv.INSPECTIONDATE,mc.INSPECTIONDATE)),1,10),'DD/MM/YYYY'),'DD/MM/YYYY') = to_char(TO_DATE('"+commonReportTakenStatus.getInspectionDate()+"','DD-MM-YY'),'DD/MM/YYYY') and SERVICE_PROVIDERNAME like '%Mahindra First Choice%' ";
			}
			
			logger.info("virAutoInspektNumberFetcher - sql query to fetch policies dated - "+commonReportTakenStatus.getInspectionDate()+" :: "+ sql);
			
		} else if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
			autoInspectLocalList = autoInspektVirService.findExistingBacklogPolicyList(commonReportTakenStatus.getInspectionDate());
			if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("local")))) {
				sql = "select policycode,PROPOSARCODE,INWARDCODE, REGISTRATIONNUMBER , CHASSISNO, ENGINENO, SERVICE_PROVIDERNAME , VIR_NUMBER , to_char(TO_DATE(SUBSTR(INSPECTIONDATE,1,10),'DD/MM/YYYY'),'MM/DD/YYYY') INSPECTIONDATE  from VIR_SERVICE "
						+ "where to_char(TO_DATE(SUBSTR(INSPECTIONDATE,1,10),'DD/MM/YYYY'),'DD/MM/YYYY') = to_char(TO_DATE('"+commonReportTakenStatus.getInspectionDate()+"','DD-MM-YY'),'DD/MM/YYYY') ";
			}else{
				sql = "select ins.policycode,ins.PROPOSARCODE,INWARDCODE nvl(ppc.REGISTRATIONNUMBER ,nvl(cv.REGISTRATIONNUMBER ,mc.REGISTRATIONNO ))REGISTRATIONNUMBER, nvl(ppc.CHASSISNO,nvl(cv.CHASSISNO,mc.CHASSISNO))CHASSISNO, nvl(ppc.ENGINENO,nvl(cv.ENGINENO,mc.ENGINENO))ENGINENO, nvl(ppc.SERVICE_PROVIDERNAME,nvl(cv.SERVICE_PROVIDERNAME,mc.SERVICE_PROVIDERNAME))SERVICE_PROVIDERNAME, nvl(ppc.VIR_NUMBER,nvl(cv.VIR_NUMBER,mc.VIR_NUMBER))VIR_NUMBER, nvl(ppc.INSPECTIONDATE,nvl(cv.INSPECTIONDATE,mc.INSPECTIONDATE)) INSPECTIONDATE from insurancepolicy ins, motorcycle mc,privatepassengercar ppc,commercialvehicle cv, vir_agency v "
						+ "where ins.id = ppc.id(+) and ins.id = cv.id(+) and ins.id = mc.id(+)  and islatest =1 and ins.businessstatus != 'garbage' and nvl(nvl(ppc.SERVICE_PROVIDERNAME,nvl(cv.SERVICE_PROVIDERNAME,mc.SERVICE_PROVIDERNAME)),'NO VIR DONE')=v.MAIL AND to_char(TO_DATE(SUBSTR(nvl(ppc.INSPECTIONDATE,nvl(cv.INSPECTIONDATE,mc.INSPECTIONDATE)),1,10),'DD/MM/YYYY'),'DD/MM/YYYY') = to_char(TO_DATE('"+commonReportTakenStatus.getInspectionDate()+"','DD-MM-YY'),'DD/MM/YYYY') ";	
			}
			
			logger.info("virAutoInspektNumberFetcher - sql query to fetch backlog policies dated - "+commonReportTakenStatus.getInspectionDate()+" :: "+ sql);
		}
		
		insertPoliciesInLocalDb(carReponseList,conn,sql,autoInspectLocalList,exchange,commonReportTakenStatus,autoInspektVirService);

		if (conn != null) {
			conn.close();
		}
		
		logger.info("virAutoInspektNumberFetcher - getPolicyNumberListFromPiaggioDB()  END ");
		return true;
	}

	private void insertPoliciesInLocalDb(List<VirResponse> carReponseList, Connection conn, String sql, List<String> autoInspectLocalList,
			Exchange exchange, CommonReportTakenStatus commonReportTakenStatus, AutoInspektVirService autoInspektVirService) throws SQLException {
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					VirResponse carReponse = new VirResponse();
					carReponse.setPolicyNo(rs.getString(1));
					carReponse.setProposalNumber(rs.getString(2));
					carReponse.setInwardCode(rs.getString(3));
					carReponse.setRegisterNumnber(rs.getString(4));
					carReponse.setChasisNo(rs.getString(5));
					carReponse.setEngineNo(rs.getString(6));
					carReponse.setServiceProvideName(rs.getString(7));
					carReponse.setVirNo(rs.getString(8));
					carReponse.setInspectionDate(rs.getString(9));
					carReponseList.add(carReponse);
					carReponse = null;
				}
				if (rs != null)
					rs.close();
			}
		}

		logger.info("virAutoInspektNumberFetcher - policies count :: " + carReponseList.size());

		List<AutoInspektVIR> newMarutiPolicyList = new ArrayList<AutoInspektVIR>();
		AutoInspektVIR fPolicy = null;
		for (VirResponse carResponse : carReponseList) {
			if (!autoInspectLocalList.contains(carResponse.getPolicyNo())) {
				fPolicy = new AutoInspektVIR();
				fPolicy.setIsReportUploaded(RPAConstants.N);
				fPolicy.setIsReportDownloaded(RPAConstants.N);
				fPolicy.setPolicyNo(carResponse.getPolicyNo());
				fPolicy.setProposalNumber(carResponse.getProposalNumber());
				fPolicy.setRegistrationNumber(carResponse.getRegisterNumnber());
				fPolicy.setInwardCode(carResponse.getInwardCode());
				fPolicy.setChassisno(carResponse.getChasisNo());
				fPolicy.setEngineno(carResponse.getEngineNo());
				fPolicy.setVirNumber(carResponse.getVirNo());
				fPolicy.setServiceProvidername(carResponse.getServiceProvideName());
				fPolicy.setInspectionDate(carResponse.getInspectionDate());

				if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.BACK_LOG)) {
					fPolicy.setBackLogFlag(RPAConstants.Y);
				} else {
					fPolicy.setBackLogFlag(RPAConstants.N);
				}
				fPolicy.setVirType(RPAConstants.OMNI_AUTOINSPECT_PREFIX);
				newMarutiPolicyList.add(fPolicy);
				fPolicy = null;
			}
		}
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			logger.info("virAutoInspektNumberFetcher - Newly fetched Policies for the date "+commonReportTakenStatus.getInspectionDate()+" :: " + newMarutiPolicyList.size());
		} else {
			logger.info("virAutoInspektNumberFetcher - Newly fetched Policies  count as backlog :: " + newMarutiPolicyList.size());
		}
		if(autoInspektVirService.save(newMarutiPolicyList)!=null){
			commonReportTakenStatus.setAutoinspektStatus(RPAConstants.Y);
			autoInspektVirService.save(commonReportTakenStatus);
		}
		
	}

}
