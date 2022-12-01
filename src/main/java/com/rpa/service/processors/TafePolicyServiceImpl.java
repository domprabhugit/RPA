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

import com.rpa.constants.RPAConstants;
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.model.processors.TafePolicy;
import com.rpa.repository.processors.CommonPolicyTakenStatusRepository;
import com.rpa.repository.processors.TafePolicyRepository;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;
import com.rpa.util.UtilityFile;

@Service
public class TafePolicyServiceImpl implements TafePolicyService {

	@Autowired
	private TafePolicyRepository tafePolicyRepository;
	
	@Autowired
	private CommonPolicyTakenStatusRepository commonPolicyTakenStatusRepository;

	@Override
	public List<TafePolicy> findAll() {
		return tafePolicyRepository.findAll();
	}

	@Override
	public List<String> findYesterdayPolicyNo() {
		return tafePolicyRepository.findYesterdayPolicyNo();
	}

	@Override
	public List<TafePolicy> save(List<TafePolicy> newMarutiPolicyList) {
		return tafePolicyRepository.save(newMarutiPolicyList);
	}

	@Override
	public List<TafePolicy> findPdfUnUploadedPolicies(Long thresholdCount) {
		return tafePolicyRepository.findPdfUnUploadedPolicies(thresholdCount);
	}

	@Override
	public TafePolicy findByPolicyNo(String policyNo) {
		return tafePolicyRepository.findByPolicyNo(policyNo);
	}

	@Override
	public TafePolicy save(TafePolicy hondaPolicy) {
		return tafePolicyRepository.save(hondaPolicy);
	}

	@Override
	public String getLastId(String paramValue) {
		return tafePolicyRepository.UPDATE_MAX_ID(paramValue);
	}

	@Override
	public CarDailyStatusResponse getMarutiDailyStatus(String policyDate)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String totalPolicies = getTotalPolicies("D", policyDate);
		/*
		 * String totalPolicies =
		 * tafePolicyRepository.getTotalPolicies(policyDate);
		 */
		String extractedPolicies = tafePolicyRepository.getExtractedPolicies(policyDate);
		String extractedProposals = tafePolicyRepository.getExtractedProposals(policyDate);
		String uploadedPolicies = tafePolicyRepository.getUploadedPolicies(policyDate);
		String uploadedProposals = tafePolicyRepository.getUploadedProposals(policyDate);
		String policyPdfErrorCount = tafePolicyRepository.getErrorPolicies(policyDate);
		String proposalPdfErrorCount = tafePolicyRepository.getErrorProposals(policyDate);
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

	private String getTotalPolicies(String flag, String policyDate)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String totalPoliciesCount = "0", sql = "";
		Connection conn = null;
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.CARPOLICY_DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		if (flag.equalsIgnoreCase("D")) {
			
			sql = "select  count(*) from TTRN_abiblt_NEWRENEWAL_SERVICE where POLICYNO not like 'DUM%' and TO_CHAR(TO_DATE(REQUEST_DATE,'DD/MM/YYYY'),'MM/DD/YYYY')= to_date('"
					+ policyDate + "','" + RPAConstants.mm_slash_dd_slash_yyyy + "') and payment_yn='Y' ";
		} else if (flag.equalsIgnoreCase("M")) {
			sql = "select count(*) from TTRN_abiblt_NEWRENEWAL_SERVICE  v where POLICYNO not like 'DUM%' and TO_CHAR(TO_DATE(REQUEST_DATE,'DD/MM/YYYY'),'MM/YYYY')= '"
					+ policyDate + "' and payment_yn='Y' ";
		} else if (flag.equalsIgnoreCase("Y")) {
			sql = "select count(*) from TTRN_abiblt_NEWRENEWAL_SERVICE  v where POLICYNO not like 'DUM%' and TO_CHAR(TO_DATE(REQUEST_DATE,'DD/MM/YYYY'),'YYYY')= '"
					+ policyDate + "' and payment_yn='Y' ";
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
	public List<String> getMarutiPolicyDateList(String monthYear, String flag) {
		List<String> dateList = new ArrayList<String>();
		if (flag.equalsIgnoreCase("P")) {
			dateList = tafePolicyRepository.getMarutiPendingPolicyDateList(monthYear);
		} else {
			dateList = tafePolicyRepository.getMarutiUploadedPoliyDateList(monthYear);
		}
		return dateList;
	}

	@Override
	public List<String> findBacklogPolicyNo(String startDate, String endDate) {
		return tafePolicyRepository.findBacklogPolicyNo(startDate, endDate);
	}

	@Override
	public List<TafePolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return tafePolicyRepository.findPdfUnUploadedBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public List<String> getCarTypeList() {
		return tafePolicyRepository.getCarTypeList();
	}

	@Override
	public List<TafePolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType) {
		return tafePolicyRepository.getCarPolicyExtractionDetails(startDate, endDate, carType);
	}

	@Override
	public List<TafePolicy> findPdfToBeDownloaded(Long thresholdCount) {
		return tafePolicyRepository.findPdfToBeDownloaded(thresholdCount);
	}

	@Override
	public List<TafePolicy> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return tafePolicyRepository.findPdfToBeDownloadedForBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String monthYear = policyMonth + "/" + policyYear;
		String totalPolicies = getTotalPolicies("M", monthYear);
		/*
		 * String totalPolicies =
		 * tafePolicyRepository.getTotalMonthlyPolicies(monthYear);
		 */
		String extractedPolicies = tafePolicyRepository.getMonthlyExtractedPolicies(monthYear);
		String extractedProposals = tafePolicyRepository.geMonthlytExtractedProposals(monthYear);
		String uploadedPolicies = tafePolicyRepository.getMonthlyUploadedPolicies(monthYear);
		String uploadedProposals = tafePolicyRepository.getMonthlyUploadedProposals(monthYear);
		String policyPdfErrorCount = tafePolicyRepository.getMonthlyErrorPolicies(monthYear);
		String proposalPdfErrorCount = tafePolicyRepository.getMonthlyErrorProposals(monthYear);
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
		/*
		 * String totalPolicies =
		 * tafePolicyRepository.getTotalYearlyPolicies(policyYear);
		 */
		String extractedPolicies = tafePolicyRepository.getYearlyExtractedPolicies(policyYear);
		String extractedProposals = tafePolicyRepository.geYearlytExtractedProposals(policyYear);
		String uploadedPolicies = tafePolicyRepository.getYearlyUploadedPolicies(policyYear);
		String uploadedProposals = tafePolicyRepository.getYearlyUploadedProposals(policyYear);
		String policyPdfErrorCount = tafePolicyRepository.getYearlyErrorPolicies(policyYear);
		String proposalPdfErrorCount = tafePolicyRepository.getYearlyErrorProposals(policyYear);
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
	public List<CommonPolicyTakenStatus> getPendingPolicyDateListTafe(String startDate) {
		return commonPolicyTakenStatusRepository.getPendingPolicyDateListTafe(startDate);
	}

	@Override
	public CommonPolicyTakenStatus save(CommonPolicyTakenStatus commonPolicyTakenStatus) {
		return commonPolicyTakenStatusRepository.save(commonPolicyTakenStatus);
	}

	@Override
	public List<String> findExistingPolicyList(String policyDate) {
		return tafePolicyRepository.findExistingPolicyList(policyDate);
	}

	@Override
	public MarutiUploadReferenceResponse getUploadReference(String policyNo,String flag) {
		MarutiUploadReferenceResponse marutiUploadReferenceResponse = new MarutiUploadReferenceResponse();
		TafePolicy marutiPolcy = tafePolicyRepository.getUploadReference(policyNo);

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
	public List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListTafe(String startDate, String endDate) {
		return commonPolicyTakenStatusRepository.getPendingBacklogPolicyDateListTafe(startDate,endDate);
	}
	
	@Override
	public List<String> findExistingBacklogPolicyList(String policyDate) {
		return tafePolicyRepository.findExistingBacklogPolicyList(policyDate);
	}

}
