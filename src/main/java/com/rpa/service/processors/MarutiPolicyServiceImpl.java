/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rpa.constants.RPAConstants;
import com.rpa.model.processors.MarutiPolicy;
import com.rpa.model.processors.MarutiPolicyTakenStatus;
import com.rpa.repository.processors.MarutiPolicyRepository;
import com.rpa.repository.processors.MarutiPolicyTakenStatusRepository;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;
import com.rpa.util.UtilityFile;

@Service
public class MarutiPolicyServiceImpl implements MarutiPolicyService {

	/*@Autowired
	private MarutiPolicyRepository marutiPolicyRepository;*/
	
	/*@Autowired
	private MarutiPolicyTakenStatusRepository marutiPolicyTakenStatusRepository;*/

	@Override
	public List<String> findYesterdayPolicyNo() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		//return marutiPolicyRepository.findYesterdayPolicyNo();
		Connection conn = UtilityFile.getLocalRPAConnection();
		List<String> marutilPolicies = new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try{
			 statement = conn.prepareStatement("SELECT s.POLICY_NO from maruti_policy s where POLICY_DATE = to_char(sysdate-1,'mm/dd/yyyy') and s.BACK_LOG_FLAG <> 'Y'");
			 rs = statement.executeQuery();
			while (rs.next()) {
				marutilPolicies.add(rs.getString(1));
			}
			
		}finally{
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		return marutilPolicies;
	}

	/*@Override
	public List<MarutiPolicy> save(List<MarutiPolicy> newMarutiPolicyList) {
		return marutiPolicyRepository.save(newMarutiPolicyList);
	}*/

	@Override
	public List<MarutiPolicy> findPdfUnUploadedPolicies(Long thresholdCount) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		//return marutiPolicyRepository.findPdfUnUploadedPolicies(thresholdCount);
		
		Connection conn = UtilityFile.getLocalRPAConnection();
		List<MarutiPolicy> marutilPolicies = new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try{
 
			 statement = conn.prepareStatement("select * from (SELECT * from Maruti_Policy s where s.back_Log_Flag <> 'Y' and (s.is_Policy_Uploaded in ('N','D') or s.is_Proposal_Uploaded in ('N','D')) and (s.is_Policy_Downloaded in ('Y') or s.is_Proposal_Downloaded in ('Y')) order by to_date(policy_Date,'MM/DD/YYYY') ) a where rownum<='"+thresholdCount+"' ");
			 rs = statement.executeQuery();
			while (rs.next()) {
				MarutiPolicy mpolicy = new MarutiPolicy();
				mpolicy.setPolicyNo(rs.getString("POLICY_NO"));
				mpolicy.setPolicyDate(rs.getString("POLICY_DATE"));
				mpolicy.setProposalNumber(rs.getString("PROPOSAL_NUMBER"));
				mpolicy.setInwardCode(rs.getString("INWARD_CODE"));
				mpolicy.setIsPolicyDownloaded(rs.getString("IS_POLICY_DOWNLOADED"));
				mpolicy.setIsProposalDownloaded(rs.getString("IS_PROPOSAL_DOWNLOADED"));
				mpolicy.setIsPolicyUploaded(rs.getString("IS_POLICY_UPLOADED"));
				mpolicy.setIsProposalUploaded(rs.getString("IS_PROPOSAL_UPLOADED"));
				mpolicy.setBackLogFlag(rs.getString("BACK_LOG_FLAG"));
				mpolicy.setCarType(rs.getString("CAR_TYPE"));
				mpolicy.setPolicyPdfPath(rs.getString("POLICY_PDF_PATH"));
				mpolicy.setProposalPdfPath(rs.getString("PROPOSAL_PDF_PATH"));
				mpolicy.setInwardFolderIndex(rs.getString("INWARD_FOLDER_INDEX"));
				mpolicy.setProposalFolderIndex(rs.getString("PROPOSAL_FOLDER_INDEX"));
				marutilPolicies.add(mpolicy);
			}
			
		}finally{
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		
		return marutilPolicies;
	}

	@Override
	public MarutiPolicy findByPolicyNo(String policyNo) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		//return marutiPolicyRepository.findByPolicyNo(policyNo);
		
		Connection conn = UtilityFile.getLocalRPAConnection();
		PreparedStatement statement = null;
		ResultSet rs = null;
		MarutiPolicy mpolicy = new MarutiPolicy();
		try{
 
			 statement = conn.prepareStatement("select * from maruti_policy s where s.POLICY_NO='"+policyNo+"' ");
			 rs = statement.executeQuery();
			while (rs.next()) {
				mpolicy.setPolicyNo(rs.getString("POLICY_NO"));
				mpolicy.setPolicyDate(rs.getString("POLICY_DATE"));
				mpolicy.setProposalNumber(rs.getString("PROPOSAL_NUMBER"));
				mpolicy.setInwardCode(rs.getString("INWARD_CODE"));
				mpolicy.setIsPolicyDownloaded(rs.getString("IS_POLICY_DOWNLOADED"));
				mpolicy.setIsProposalDownloaded(rs.getString("IS_PROPOSAL_DOWNLOADED"));
				mpolicy.setIsPolicyUploaded(rs.getString("IS_POLICY_UPLOADED"));
				mpolicy.setIsProposalUploaded(rs.getString("IS_PROPOSAL_UPLOADED"));
				mpolicy.setBackLogFlag(rs.getString("BACK_LOG_FLAG"));
				mpolicy.setCarType(rs.getString("CAR_TYPE"));
				mpolicy.setPolicyPdfPath(rs.getString("POLICY_PDF_PATH"));
				mpolicy.setProposalPdfPath(rs.getString("PROPOSAL_PDF_PATH"));
				mpolicy.setInwardFolderIndex(rs.getString("INWARD_FOLDER_INDEX"));
				mpolicy.setProposalFolderIndex(rs.getString("PROPOSAL_FOLDER_INDEX"));
			}
			
			
		}finally{
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		
		return mpolicy;
	}

	/*@Override
	public MarutiPolicy save(MarutiPolicy marutiPolicy) {
		return marutiPolicyRepository.save(marutiPolicy);
	}*/

	/*@Override
	public String getLastId(String paramValue) {
		return marutiPolicyRepository.UPDATE_MAX_ID(paramValue);
	}*/

	@Override
	public CarDailyStatusResponse getMarutiDailyStatus(String policyDate)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String totalPolicies = getTotalPolicies("D", policyDate);
		
		/*String extractedPolicies = marutiPolicyRepository.getExtractedPolicies(policyDate);
		String extractedProposals = marutiPolicyRepository.getExtractedProposals(policyDate);
		String uploadedPolicies = marutiPolicyRepository.getUploadedPolicies(policyDate);
		String uploadedProposals = marutiPolicyRepository.getUploadedProposals(policyDate);
		String policyPdfErrorCount = marutiPolicyRepository.getErrorPolicies(policyDate);
		String proposalPdfErrorCount = marutiPolicyRepository.getErrorProposals(policyDate);*/
		
		String extractedPolicies = getStringDetailByQry("SELECT count(*) from Maruti_Policy s where to_date(POLICY_DATE,'mm/dd/yyyy') = to_date('"+policyDate+"','mm/dd/yyyy') and s.is_policy_downloaded='Y'");
		String extractedProposals = getStringDetailByQry("SELECT count(*) from Maruti_Policy s where to_date(POLICY_DATE,'mm/dd/yyyy') = to_date('"+policyDate+"','mm/dd/yyyy') and s.is_proposal_downloaded='Y'");
		String uploadedPolicies = getStringDetailByQry("SELECT count(*) from Maruti_Policy s where to_date(POLICY_DATE,'mm/dd/yyyy') = to_date('"+policyDate+"','mm/dd/yyyy') and s.is_policy_uploaded='Y'");
		String uploadedProposals = getStringDetailByQry("SELECT count(*) from Maruti_Policy s where to_date(POLICY_DATE,'mm/dd/yyyy') = to_date('"+policyDate+"','mm/dd/yyyy') and s.is_proposal_uploaded='Y'");
		String policyPdfErrorCount =getStringDetailByQry("SELECT count(*) from Maruti_Policy s where to_date(POLICY_DATE,'mm/dd/yyyy') = to_date('"+policyDate+"','mm/dd/yyyy') and s.is_policy_uploaded='E'");
		String proposalPdfErrorCount = getStringDetailByQry("SELECT count(*) from Maruti_Policy s where to_date(POLICY_DATE,'mm/dd/yyyy') = to_date('"+policyDate+"','mm/dd/yyyy') and s.is_proposal_uploaded='E'");
		
		
		CarDailyStatusResponse marutiDailyStatusResponse = new CarDailyStatusResponse();
		marutiDailyStatusResponse.setTotalPolciesCount(totalPolicies);
		marutiDailyStatusResponse.setPolicyPdfExtractedCount(extractedPolicies);
		marutiDailyStatusResponse.setProposalPdfExtractedCount(extractedProposals);
		marutiDailyStatusResponse.setPolicyPdfUploadedCount(uploadedPolicies);
		marutiDailyStatusResponse.setProposalPdfUploadedCount(uploadedProposals);
		marutiDailyStatusResponse.setPolicyPdfErrorCount(policyPdfErrorCount);
		marutiDailyStatusResponse.setProposalPdfErrorCount(proposalPdfErrorCount);
		return marutiDailyStatusResponse;
	}

	@Override
	public String getTotalPolicies(String flag, String policyDate)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String totalPoliciesCount = "0", sql = "";
		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		if (flag.equalsIgnoreCase("D")) {
			sql = "select count(*) from ttrn_maruti_newrenewal_service  v where policyno not like 'DUM%' and TRUNC(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'))= to_date('"
					+ policyDate + "','" + RPAConstants.mm_slash_dd_slash_yyyy + "') ";
		} else if (flag.equalsIgnoreCase("M")) {
			sql = "select count(*) from ttrn_maruti_newrenewal_service  v where policyno not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/YYYY')= '"
					+ policyDate + "' ";
		} else if (flag.equalsIgnoreCase("Y")) {
			sql = "select count(*) from ttrn_maruti_newrenewal_service  v where policyno not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'YYYY')= '"
					+ policyDate + "' ";
		}

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					totalPoliciesCount = rs.getString(1);

				}
				if (rs != null)
					rs.close();
			}
		}
		if (conn != null)
			conn.close();

		return totalPoliciesCount;
	}

	@Override
	public List<String> getMarutiPolicyDateList(String monthYear, String flag) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		List<String> dateList = new ArrayList<String>();
		/*if (flag.equalsIgnoreCase("P")) {
			dateList = marutiPolicyRepository.getMarutiPendingPolicyDateList(monthYear);
		} else {
			dateList = marutiPolicyRepository.getMarutiUploadedPoliyDateList(monthYear);
		}*/
		String sql = "";
		if (flag.equalsIgnoreCase("P")) {
			sql = "SELECT distinct s.POLICY_DATE from  maruti_policy s where to_char(to_date(POLICY_DATE,'mm/dd/yyyy'),'mm/yyyy') = '"+monthYear+"' and s.IS_POLICY_UPLOADED='N' and  s.IS_PROPOSAL_UPLOADED='N' order by to_date(POLICY_DATE,'MM/DD/YYYY')";
			dateList = getMarutiPolicyDates(monthYear,sql);
		} else {
			sql = "SELECT distinct s.POLICY_DATE from  maruti_policy s where to_char(to_date(POLICY_DATE,'mm/dd/yyyy'),'mm/yyyy') = '"+monthYear+"' and s.POLICY_DATE not in (SELECT distinct s.POLICY_DATE from  maruti_policy s where to_char(to_date(POLICY_DATE,'mm/dd/yyyy'),'mm/yyyy') = '"+monthYear+"' and s.IS_POLICY_UPLOADED='N' and  s.IS_PROPOSAL_UPLOADED='N') order by to_date(POLICY_DATE,'MM/DD/YYYY')";
			dateList = getMarutiPolicyDates(monthYear,sql);
		}
		
		return dateList;
	}

	private List<String> getMarutiPolicyDates(String monthYear, String sql) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		Connection conn = UtilityFile.getLocalRPAConnection();
		List<String> dates = new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try{
 
			 statement = conn.prepareStatement(sql);
			 rs = statement.executeQuery();
			while (rs.next()) {
				dates.add(rs.getString(1));
			}
			
		}finally{
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		return dates;
	}

	@Override
	public List<String> findBacklogPolicyNo(String startDate, String endDate) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		//return marutiPolicyRepository.findBacklogPolicyNo(startDate, endDate);
		Connection conn = UtilityFile.getLocalRPAConnection();
		List<String> marutilPolicies = new ArrayList<>();
		PreparedStatement statement =  null;
		ResultSet rs = null;
		try{
 
			 statement = conn.prepareStatement("SELECT s.POLICY_NO from maruti_policy s where (to_date(s.POLICY_DATE,'mm/dd/yyyy') between to_date('"+startDate+"','mm/dd/yyyy') and to_date('"+endDate+"','mm/dd/yyyy')) and s.BACK_LOG_FLAG='Y'");
			 rs = statement.executeQuery();
			while (rs.next()) {
				marutilPolicies.add(rs.getString(1));
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		return marutilPolicies;
	}

	@Override
	public List<MarutiPolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		//return marutiPolicyRepository.findPdfUnUploadedBackLogPolicies(startDate, endDate,thresholdCount);
		
		Connection conn = UtilityFile.getLocalRPAConnection();
		List<MarutiPolicy> marutilPolicies = new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try{
 
			 statement = conn.prepareStatement("select * from (SELECT  from Maruti_Policy s where s.back_Log_Flag='Y' and (to_date(s.policy_Date,'mm/dd/yyyy') between to_date('"+startDate+"','mm/dd/yyyy') and to_date('"+endDate+"','mm/dd/yyyy')) and (s.is_Policy_Uploaded in ('N','D') or s.is_Proposal_Uploaded in ('N','D')) and ( s.is_Policy_Downloaded in ('Y') or s.is_Proposal_Downloaded in ('Y') ) order by to_date(policy_Date,'MM/DD/YYYY') ) a where rownum<='"+thresholdCount+"'");
			 rs = statement.executeQuery();
			while (rs.next()) {
				MarutiPolicy mpolicy = new MarutiPolicy();
				mpolicy.setPolicyNo(rs.getString("POLICY_NO"));
				mpolicy.setPolicyDate(rs.getString("POLICY_DATE"));
				mpolicy.setProposalNumber(rs.getString("PROPOSAL_NUMBER"));
				mpolicy.setInwardCode(rs.getString("INWARD_CODE"));
				mpolicy.setIsPolicyDownloaded(rs.getString("IS_POLICY_DOWNLOADED"));
				mpolicy.setIsProposalDownloaded(rs.getString("IS_PROPOSAL_DOWNLOADED"));
				mpolicy.setIsPolicyUploaded(rs.getString("IS_POLICY_UPLOADED"));
				mpolicy.setIsProposalUploaded(rs.getString("IS_PROPOSAL_UPLOADED"));
				mpolicy.setBackLogFlag(rs.getString("BACK_LOG_FLAG"));
				mpolicy.setCarType(rs.getString("CAR_TYPE"));
				mpolicy.setPolicyPdfPath(rs.getString("POLICY_PDF_PATH"));
				mpolicy.setProposalPdfPath(rs.getString("PROPOSAL_PDF_PATH"));
				mpolicy.setInwardFolderIndex(rs.getString("INWARD_FOLDER_INDEX"));
				mpolicy.setProposalFolderIndex(rs.getString("PROPOSAL_FOLDER_INDEX"));
				marutilPolicies.add(mpolicy);
			}
			
		}finally{
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		
		return marutilPolicies;
	}

	@Override
	public List<String> getCarTypeList() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		
		//return marutiPolicyRepository.getCarTypeList();
		Connection conn = UtilityFile.getLocalRPAConnection();
		List<String> carTypes = new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try{
 
			statement = conn.prepareStatement("SELECT distinct s.CAR_TYPE from maruti_policy s");
			rs = statement.executeQuery();
			while (rs.next()) {
				carTypes.add(rs.getString(1));
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		
		return carTypes;
	}

	@Override
	public List<MarutiPolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		//return marutiPolicyRepository.getCarPolicyExtractionDetails(startDate, endDate, carType);
		Connection conn = UtilityFile.getLocalRPAConnection();
		List<MarutiPolicy> marutiPolicies = new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try{
 
			statement = conn.prepareStatement("SELECT * from maruti_policy s where (to_date(s.POLICY_DATE,'mm/dd/yyyy') between to_date('"+startDate+"','mm/dd/yyyy') and to_date('"+endDate+"','mm/dd/yyyy')) and s.CAR_TYPE='"+carType+"'");
			rs = statement.executeQuery();
			while (rs.next()) {
				MarutiPolicy mpolicy = new MarutiPolicy();
				mpolicy.setPolicyNo(rs.getString("POLICY_NO"));
				mpolicy.setPolicyDate(rs.getString("POLICY_DATE"));
				mpolicy.setProposalNumber(rs.getString("PROPOSAL_NUMBER"));
				mpolicy.setInwardCode(rs.getString("INWARD_CODE"));
				mpolicy.setIsPolicyDownloaded(rs.getString("IS_POLICY_DOWNLOADED"));
				mpolicy.setIsProposalDownloaded(rs.getString("IS_PROPOSAL_DOWNLOADED"));
				mpolicy.setIsPolicyUploaded(rs.getString("IS_POLICY_UPLOADED"));
				mpolicy.setIsProposalUploaded(rs.getString("IS_PROPOSAL_UPLOADED"));
				mpolicy.setBackLogFlag(rs.getString("BACK_LOG_FLAG"));
				mpolicy.setCarType(rs.getString("CAR_TYPE"));
				mpolicy.setPolicyPdfPath(rs.getString("POLICY_PDF_PATH"));
				mpolicy.setProposalPdfPath(rs.getString("PROPOSAL_PDF_PATH"));
				mpolicy.setInwardFolderIndex(rs.getString("INWARD_FOLDER_INDEX"));
				mpolicy.setProposalFolderIndex(rs.getString("PROPOSAL_FOLDER_INDEX"));
				marutiPolicies.add(mpolicy);
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		return marutiPolicies;
	}

	@Override
	public List<MarutiPolicy> findPdfToBeDownloaded(Long thresholdCount) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		//return marutiPolicyRepository.findPdfToBeDownloaded(thresholdCount);
		
		Connection conn = UtilityFile.getLocalRPAConnection();
		List<MarutiPolicy> marutilPolicies = new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try{
 
			 statement = conn.prepareStatement("select * from (SELECT * from Maruti_Policy s where s.back_Log_Flag <> 'Y' and (s.is_Policy_Downloaded in ('N') or s.is_Proposal_Downloaded in ('N')) order by to_date(policy_Date,'MM/DD/YYYY') )a where rownum<='"+thresholdCount+"'");
			 rs = statement.executeQuery();
			while (rs.next()) {
				MarutiPolicy mpolicy = new MarutiPolicy();
				mpolicy.setPolicyNo(rs.getString("POLICY_NO"));
				mpolicy.setPolicyDate(rs.getString("POLICY_DATE"));
				mpolicy.setProposalNumber(rs.getString("PROPOSAL_NUMBER"));
				mpolicy.setInwardCode(rs.getString("INWARD_CODE"));
				mpolicy.setIsPolicyDownloaded(rs.getString("IS_POLICY_DOWNLOADED"));
				mpolicy.setIsProposalDownloaded(rs.getString("IS_PROPOSAL_DOWNLOADED"));
				mpolicy.setIsPolicyUploaded(rs.getString("IS_POLICY_UPLOADED"));
				mpolicy.setIsProposalUploaded(rs.getString("IS_PROPOSAL_UPLOADED"));
				mpolicy.setBackLogFlag(rs.getString("BACK_LOG_FLAG"));
				mpolicy.setCarType(rs.getString("CAR_TYPE"));
				mpolicy.setPolicyPdfPath(rs.getString("POLICY_PDF_PATH"));
				mpolicy.setProposalPdfPath(rs.getString("PROPOSAL_PDF_PATH"));
				mpolicy.setInwardFolderIndex(rs.getString("INWARD_FOLDER_INDEX"));
				mpolicy.setProposalFolderIndex(rs.getString("PROPOSAL_FOLDER_INDEX"));
				marutilPolicies.add(mpolicy);
			}
			
		}finally{
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		
		return marutilPolicies;
	}

	@Override
	public List<MarutiPolicy> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		//return marutiPolicyRepository.findPdfToBeDownloadedForBackLogPolicies(startDate, endDate,thresholdCount);
		
		Connection conn = UtilityFile.getLocalRPAConnection();
		List<MarutiPolicy> marutilPolicies = new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try{
 
			 statement = conn.prepareStatement("select * from (SELECT * from Maruti_Policy s where s.back_Log_Flag='Y' and (to_date(s.policy_Date,'mm/dd/yyyy') between to_date('"+startDate+"','mm/dd/yyyy') and to_date('"+endDate+"','mm/dd/yyyy')) and (s.is_Policy_Downloaded in ('N') or s.is_Proposal_Downloaded in ('N')) order by to_date(policy_Date,'MM/DD/YYYY') )a where rownum<='"+thresholdCount+"'");
			 rs = statement.executeQuery();
			while (rs.next()) {
				MarutiPolicy mpolicy = new MarutiPolicy();
				mpolicy.setPolicyNo(rs.getString("POLICY_NO"));
				mpolicy.setPolicyDate(rs.getString("POLICY_DATE"));
				mpolicy.setProposalNumber(rs.getString("PROPOSAL_NUMBER"));
				mpolicy.setInwardCode(rs.getString("INWARD_CODE"));
				mpolicy.setIsPolicyDownloaded(rs.getString("IS_POLICY_DOWNLOADED"));
				mpolicy.setIsProposalDownloaded(rs.getString("IS_PROPOSAL_DOWNLOADED"));
				mpolicy.setIsPolicyUploaded(rs.getString("IS_POLICY_UPLOADED"));
				mpolicy.setIsProposalUploaded(rs.getString("IS_PROPOSAL_UPLOADED"));
				mpolicy.setBackLogFlag(rs.getString("BACK_LOG_FLAG"));
				mpolicy.setCarType(rs.getString("CAR_TYPE"));
				mpolicy.setPolicyPdfPath(rs.getString("POLICY_PDF_PATH"));
				mpolicy.setProposalPdfPath(rs.getString("PROPOSAL_PDF_PATH"));
				mpolicy.setInwardFolderIndex(rs.getString("INWARD_FOLDER_INDEX"));
				mpolicy.setProposalFolderIndex(rs.getString("PROPOSAL_FOLDER_INDEX"));
				marutilPolicies.add(mpolicy);
			}
			
		}finally{
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		
		return marutilPolicies;
	}

	@Override
	public CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String monthYear = policyMonth + "/" + policyYear;
		String totalPolicies = getTotalPolicies("M", monthYear);
		
		/*String extractedPolicies = marutiPolicyRepository.getMonthlyExtractedPolicies(monthYear);
		String extractedProposals = marutiPolicyRepository.geMonthlytExtractedProposals(monthYear);
		String uploadedPolicies = marutiPolicyRepository.getMonthlyUploadedPolicies(monthYear);
		String uploadedProposals = marutiPolicyRepository.getMonthlyUploadedProposals(monthYear);
		String policyPdfErrorCount = marutiPolicyRepository.getMonthlyErrorPolicies(monthYear);
		String proposalPdfErrorCount = marutiPolicyRepository.getMonthlyErrorProposals(monthYear);*/
		
		String extractedPolicies = getStringDetailByQry("SELECT count(*) from maruti_policy s where to_char(to_date(policy_date,'mm/dd/yyyy'),'mm/yyyy') = '"+monthYear+"' and s.is_policy_downloaded='Y'");
		String extractedProposals = getStringDetailByQry("SELECT count(*) from maruti_policy s where to_char(to_date(policy_date,'mm/dd/yyyy'),'mm/yyyy') = '"+monthYear+"' and s.is_proposal_downloaded='Y'");
		String uploadedPolicies = getStringDetailByQry("SELECT count(*) from maruti_policy s where to_char(to_date(policy_date,'mm/dd/yyyy'),'mm/yyyy') = '"+monthYear+"' and s.is_policy_uploaded='Y'");
		String uploadedProposals = getStringDetailByQry("SELECT count(*) from maruti_policy s where to_char(to_date(policy_date,'mm/dd/yyyy'),'mm/yyyy') = '"+monthYear+"' and s.is_proposal_uploaded='Y'");
		String policyPdfErrorCount = getStringDetailByQry("SELECT count(*) from maruti_policy s where to_char(to_date(policy_date,'mm/dd/yyyy'),'mm/yyyy') = '"+monthYear+"' and s.is_policy_uploaded='E'");
		String proposalPdfErrorCount = getStringDetailByQry("SELECT count(*) from maruti_policy s where to_char(to_date(policy_date,'mm/dd/yyyy'),'mm/yyyy') = '"+monthYear+"' and s.is_policy_uploaded='E'");
		
		CarDailyStatusResponse marutiDailyStatusResponse = new CarDailyStatusResponse();
		marutiDailyStatusResponse.setTotalPolciesCount(totalPolicies);
		marutiDailyStatusResponse.setPolicyPdfExtractedCount(extractedPolicies);
		marutiDailyStatusResponse.setProposalPdfExtractedCount(extractedProposals);
		marutiDailyStatusResponse.setPolicyPdfUploadedCount(uploadedPolicies);
		marutiDailyStatusResponse.setProposalPdfUploadedCount(uploadedProposals);
		marutiDailyStatusResponse.setPolicyPdfErrorCount(policyPdfErrorCount);
		marutiDailyStatusResponse.setProposalPdfErrorCount(proposalPdfErrorCount);
		return marutiDailyStatusResponse;
	}

	@Override
	public CarDailyStatusResponse getMarutiYearlyStatus(String policyYear)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String totalPolicies = getTotalPolicies("Y", policyYear);
		/*String extractedPolicies = marutiPolicyRepository.getYearlyExtractedPolicies(policyYear);
		String extractedProposals = marutiPolicyRepository.geYearlytExtractedProposals(policyYear);
		String uploadedPolicies = marutiPolicyRepository.getYearlyUploadedPolicies(policyYear);
		String uploadedProposals = marutiPolicyRepository.getYearlyUploadedProposals(policyYear);
		String policyPdfErrorCount = marutiPolicyRepository.getYearlyErrorPolicies(policyYear);
		String proposalPdfErrorCount = marutiPolicyRepository.getYearlyErrorProposals(policyYear);*/
		
		String extractedPolicies = getStringDetailByQry("SELECT count(*) from maruti_policy s where to_char(to_date(policy_date,'mm/dd/yyyy'),'yyyy') = '"+policyYear+"' and s.is_policy_downloaded='Y'");
		String extractedProposals = getStringDetailByQry("SELECT count(*) from maruti_policy s where to_char(to_date(policy_date,'mm/dd/yyyy'),'yyyy') = '"+policyYear+"' and s.is_proposal_downloaded='Y' ");
		String uploadedPolicies = getStringDetailByQry("SELECT count(*) from maruti_policy s where to_char(to_date(policy_date,'mm/dd/yyyy'),'yyyy') = '"+policyYear+"' and s.is_policy_uploaded='Y'");
		String uploadedProposals = getStringDetailByQry("SELECT count(*) from maruti_policy s where to_char(to_date(policy_date,'mm/dd/yyyy'),'yyyy') = '"+policyYear+"' and s.is_proposal_uploaded='Y'");
		String policyPdfErrorCount = getStringDetailByQry("SELECT count(*) from maruti_policy s where to_char(to_date(policy_date,'mm/dd/yyyy'),'yyyy') = '"+policyYear+"' and s.is_policy_uploaded='E'");
		String proposalPdfErrorCount = getStringDetailByQry("SELECT count(*) from maruti_policy s where to_char(to_date(policy_date,'mm/dd/yyyy'),'yyyy') = '"+policyYear+"' and s.is_proposal_uploaded='E'");
		
		CarDailyStatusResponse marutiDailyStatusResponse = new CarDailyStatusResponse();
		marutiDailyStatusResponse.setTotalPolciesCount(totalPolicies);
		marutiDailyStatusResponse.setPolicyPdfExtractedCount(extractedPolicies);
		marutiDailyStatusResponse.setProposalPdfExtractedCount(extractedProposals);
		marutiDailyStatusResponse.setPolicyPdfUploadedCount(uploadedPolicies);
		marutiDailyStatusResponse.setProposalPdfUploadedCount(uploadedProposals);
		marutiDailyStatusResponse.setPolicyPdfErrorCount(policyPdfErrorCount);
		marutiDailyStatusResponse.setProposalPdfErrorCount(proposalPdfErrorCount);
		return marutiDailyStatusResponse;
	}

	@Override
	public List<MarutiPolicyTakenStatus> getPendingPolicyDateList(String startDate) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		//return marutiPolicyTakenStatusRepository.getPendingPolicyDateList(startDate);
		return getMarutiPolicyTakenStatusList("select * from MARUTI_POLICY_TAKEN_STATUS s where s.STATUS='N' AND s.policy_date BETWEEN to_date('"+startDate+"','dd/mm/yy') AND SYSDATE-1 order by to_date(s.policy_date,'dd/mm/yyyy')");
	}

	/*@Override
	public MarutiPolicyTakenStatus save(MarutiPolicyTakenStatus marutiPolicyTakenStatus) {
		return marutiPolicyTakenStatusRepository.save(marutiPolicyTakenStatus);
	}*/

	@Override
	public List<String> findExistingPolicyList(String policyDate) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		//return marutiPolicyRepository.findExistingPolicyList(policyDate);
		Connection conn = UtilityFile.getLocalRPAConnection();
		List<String> marutilPolicies = new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try{
 
			 statement = conn.prepareStatement("SELECT s.POLICY_NO from maruti_policy s where to_char(to_date(POLICY_DATE,'mm/dd/yy'),'DD/MM/YYYY') = to_char(to_date('"+policyDate+"','dd/mm/yyyy'),'DD/MM/YYYY') and s.BACK_LOG_FLAG <> 'Y'");
			 rs = statement.executeQuery();
			while (rs.next()) {
				marutilPolicies.add(rs.getString(1));
			}
			
		}finally{
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		return marutilPolicies;
	}

	@Override
	public MarutiUploadReferenceResponse getUploadReference(String policyNo,String flag) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		MarutiUploadReferenceResponse marutiUploadReferenceResponse = new MarutiUploadReferenceResponse();
		//MarutiPolicy marutiPolcy = marutiPolicyRepository.getUploadReference(policyNo);
		MarutiPolicy marutiPolcy = findByPolicyNo(policyNo);

		if(flag.equalsIgnoreCase("PO")){
			marutiUploadReferenceResponse.setUploadTime(marutiPolcy.getPolicyPdfUploadedTime());
			marutiUploadReferenceResponse.setRequestXml(new String(marutiPolcy.getPolicyRequest()));
			marutiUploadReferenceResponse.setResponseXml(new String(marutiPolcy.getPolicyResponse()));
		}else{
			marutiUploadReferenceResponse.setUploadTime(marutiPolcy.getProposalPdfUploadedTime());
			marutiUploadReferenceResponse.setRequestXml(new String(marutiPolcy.getProposalRequest()));
			marutiUploadReferenceResponse.setResponseXml(new String(marutiPolcy.getProposalResponse()));
		}

		return marutiUploadReferenceResponse;
	}

	@Override
	public List<MarutiPolicyTakenStatus> getPendingBacklogPolicyDateList(String startDate, String endDate) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		//return marutiPolicyTakenStatusRepository.getPendingBacklogPolicyDateList(startDate,endDate);
		return getMarutiPolicyTakenStatusList("select * from MARUTI_POLICY_TAKEN_STATUS s where s.STATUS='N' AND s.POLICY_DATE BETWEEN to_date('"+startDate+"','dd/mm/yy') AND to_date('"+endDate+"','dd/mm/yy') order by to_date(s.POLICY_DATE,'dd/mm/yyyy')");
	}
	
	@Override
	public List<String> findExistingBacklogPolicyList(String policyDate) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		//return marutiPolicyRepository.findExistingBacklogPolicyList(policyDate);
		Connection conn = UtilityFile.getLocalRPAConnection();
		List<String> marutilPolicies = new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try{
 
			 statement = conn.prepareStatement("SELECT s.POLICY_NO from maruti_policy s where to_char(to_date(POLICY_DATE,'mm/dd/yy'),'DD/MM/YYYY') = to_char(to_date('"+policyDate+"','dd/mm/yyyy'),'DD/MM/YYYY') and s.BACK_LOG_FLAG = 'Y'");
			 rs = statement.executeQuery();
			while (rs.next()) {
				marutilPolicies.add(rs.getString(1));
			}
			
		}finally{
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		return marutilPolicies;
	}

	@Override
	public List<MarutiPolicy> saveNewObject(List<MarutiPolicy> newMarutiPolicyList) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		Connection conn = UtilityFile.getLocalRPAConnection();
		PreparedStatement statement = null;
		List<MarutiPolicy> result = new ArrayList<MarutiPolicy>();
		try{
		for(MarutiPolicy marutiObj : newMarutiPolicyList){
			statement = conn
					.prepareStatement("insert into maruti_policy (POLICY_NO,POLICY_DATE,PROPOSAL_NUMBER,BACK_LOG_FLAG,CAR_TYPE,IS_POLICY_DOWNLOADED,IS_PROPOSAL_DOWNLOADED,IS_POLICY_UPLOADED,IS_PROPOSAL_UPLOADED) values(?,?,?,?,?,?,?,?,?)");
			statement.setString(1, marutiObj.getPolicyNo());
			statement.setString(2,marutiObj.getPolicyDate());
			statement.setString(3, marutiObj.getProposalNumber());
			statement.setString(4, marutiObj.getBackLogFlag());
			statement.setString(5, marutiObj.getCarType());
			statement.setString(6, marutiObj.getIsPolicyDownloaded());
			statement.setString(7, marutiObj.getIsProposalDownloaded());
			statement.setString(8, marutiObj.getIsPolicyUploaded());
			statement.setString(9, marutiObj.getIsProposalUploaded());
			statement.execute();
			result.add(marutiObj);
		}
			
		}catch(Exception e){
			return null;
		}finally{
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		
		return result;
	}
	
	
	public String getStringDetailByQry(String query) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException{
		
		Connection conn = UtilityFile.getLocalRPAConnection();
		String returnString = "";
		PreparedStatement statement = null;
		ResultSet rs = null;
		try{
 
			statement = conn.prepareStatement(query);
			rs = statement.executeQuery();
			while (rs.next()) {
				returnString = rs.getString(1);
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		
		return returnString;
		
	}

	@Override
	public boolean updateNonByteMarutiData(MarutiPolicy marutiPolicyObj) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		Connection conn = UtilityFile.getLocalRPAConnection();
		PreparedStatement updateStatement = null;
		try{
		if(marutiPolicyObj!=null){
		String Sql = " update maruti_policy set POLICY_DATE=?,PROPOSAL_NUMBER=?,INWARD_CODE=?,IS_POLICY_DOWNLOADED=?,IS_PROPOSAL_DOWNLOADED=?,IS_POLICY_UPLOADED=?,IS_PROPOSAL_UPLOADED=?,POLICY_PDF_PATH=?,PROPOSAL_PDF_PATH=?,POLICY_PDF_UPLOADED_TIME=?,PROPOSAL_PDF_UPLOADED_TIME=?,INWARD_FOLDER_INDEX=?,PROPOSAL_FOLDER_INDEX=? WHERE POLICY_NO='"+marutiPolicyObj.getPolicyNo()+"' ";
		
		 updateStatement = conn.prepareStatement(Sql);
		updateStatement.setString(1, marutiPolicyObj.getPolicyDate());
		updateStatement.setString(2, marutiPolicyObj.getProposalNumber());
		updateStatement.setString(3, marutiPolicyObj.getInwardCode());
		updateStatement.setString(4, marutiPolicyObj.getIsPolicyDownloaded());
		updateStatement.setString(5, marutiPolicyObj.getIsProposalDownloaded());
		updateStatement.setString(6, marutiPolicyObj.getIsPolicyUploaded());
		updateStatement.setString(7, marutiPolicyObj.getIsProposalUploaded());
		updateStatement.setString(8, marutiPolicyObj.getPolicyPdfPath());
		updateStatement.setString(9, marutiPolicyObj.getProposalPdfPath());
		if(marutiPolicyObj.getPolicyPdfUploadedTime()!=null)
			updateStatement.setDate(10, new java.sql.Date((marutiPolicyObj.getPolicyPdfUploadedTime().getTime())));
		else
			updateStatement.setDate(10,null);
		if(marutiPolicyObj.getProposalPdfUploadedTime()!=null)
			updateStatement.setDate(11, new java.sql.Date((marutiPolicyObj.getProposalPdfUploadedTime().getTime())));
		else
			updateStatement.setDate(11, null);
		updateStatement.setString(12, marutiPolicyObj.getInwardCode());
		updateStatement.setString(13, marutiPolicyObj.getProposalFolderIndex());
		
		updateStatement.setQueryTimeout(3600);
		updateStatement.execute();
			return true;
		}else{
			return false;
		}
		}finally{
			if(updateStatement!=null)
				updateStatement.close();
			if(conn!=null)
				conn.close();
		}
	}

	@Override
	public boolean updateWithByteMarutiData(MarutiPolicy marutiPolicyObj) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		Connection conn = UtilityFile.getLocalRPAConnection();
		PreparedStatement updateStatement = null;
		try{
		if(marutiPolicyObj!=null){
		String Sql = " update maruti_policy set POLICY_DATE=?,PROPOSAL_NUMBER=?,INWARD_CODE=?,IS_POLICY_DOWNLOADED=?,IS_PROPOSAL_DOWNLOADED=?,IS_POLICY_UPLOADED=?,IS_PROPOSAL_UPLOADED=?,POLICY_PDF_PATH=?,PROPOSAL_PDF_PATH=?,POLICY_PDF_UPLOADED_TIME=?,PROPOSAL_PDF_UPLOADED_TIME=?,INWARD_FOLDER_INDEX=?,PROPOSAL_FOLDER_INDEX=?,POLICY_REQUEST=?,POLICY_RESPONSE=?,PROPOSAL_REQUEST=?,PROPOSAL_RESPONSE=? where POLICY_NO='"+marutiPolicyObj.getPolicyNo()+"' ";
		
		 updateStatement = conn.prepareStatement(Sql);
		updateStatement.setString(1, marutiPolicyObj.getPolicyDate());
		updateStatement.setString(2, marutiPolicyObj.getProposalNumber());
		updateStatement.setString(3, marutiPolicyObj.getInwardCode());
		updateStatement.setString(4, marutiPolicyObj.getIsPolicyDownloaded());
		updateStatement.setString(5, marutiPolicyObj.getIsProposalDownloaded());
		updateStatement.setString(6, marutiPolicyObj.getIsPolicyUploaded());
		updateStatement.setString(7, marutiPolicyObj.getIsProposalUploaded());
		updateStatement.setString(8, marutiPolicyObj.getPolicyPdfPath());
		updateStatement.setString(9, marutiPolicyObj.getProposalPdfPath());
		if(marutiPolicyObj.getPolicyPdfUploadedTime()!=null)
			updateStatement.setDate(10, new java.sql.Date((marutiPolicyObj.getPolicyPdfUploadedTime().getTime())));
		else
			updateStatement.setDate(10, null);
		if(marutiPolicyObj.getProposalPdfUploadedTime()!=null)
			updateStatement.setDate(11, new java.sql.Date((marutiPolicyObj.getProposalPdfUploadedTime().getTime())));
		else
			updateStatement.setDate(11, null);
		updateStatement.setString(12, marutiPolicyObj.getInwardCode());
		updateStatement.setString(13, marutiPolicyObj.getProposalFolderIndex());
		updateStatement.setBytes(14, marutiPolicyObj.getPolicyRequest());
		updateStatement.setBytes(15, marutiPolicyObj.getPolicyResponse());
		updateStatement.setBytes(16, marutiPolicyObj.getProposalRequest());
		updateStatement.setBytes(17, marutiPolicyObj.getProposalResponse());
	
		updateStatement.setQueryTimeout(3600);
		updateStatement.execute();
			return true;
		}else{
			return false;
		}
		}finally{
			if(updateStatement!=null)
				updateStatement.close();
			if(conn!=null)
				conn.close();
		}
	}

	public List<MarutiPolicyTakenStatus> getMarutiPolicyTakenStatusList(String sql) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException{
		Connection conn = UtilityFile.getLocalRPAConnection();
		List<MarutiPolicyTakenStatus> marutilPolicies = new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try{
 
			 statement = conn.prepareStatement(sql);
			 rs = statement.executeQuery();
			while (rs.next()) {
				MarutiPolicyTakenStatus mpolicy = new MarutiPolicyTakenStatus();
				mpolicy.setPolicyDate(rs.getString("POLICY_DATE"));
				mpolicy.setStatus(rs.getString("STATUS"));
				marutilPolicies.add(mpolicy);
			}
			
		}finally{
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
			if(conn!=null){
				conn.close();
			}
		}
		return  marutilPolicies;
	}


	@Override
	public boolean update(MarutiPolicyTakenStatus marutiPolicyTakenStatus) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		Connection conn = UtilityFile.getLocalRPAConnection();
		PreparedStatement updateStatement = null;
		try{
		if(marutiPolicyTakenStatus!=null){
		String Sql = " update MARUTI_POLICY_TAKEN_STATUS set STATUS=? where POLICY_DATE='"+marutiPolicyTakenStatus.getPolicyDate()+"' ";
		
		 updateStatement = conn.prepareStatement(Sql);
		updateStatement.setString(1, marutiPolicyTakenStatus.getStatus());
		
		updateStatement.setQueryTimeout(3600);
		updateStatement.execute();
			return true;
		}else{
			return false;
		}
		}finally{
			if(updateStatement!=null)
				updateStatement.close();
			if(conn!=null)
				conn.close();
		}
	}
}
