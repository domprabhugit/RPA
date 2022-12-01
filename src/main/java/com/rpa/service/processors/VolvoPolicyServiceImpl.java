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
import com.rpa.model.processors.VolvoPolicy;
import com.rpa.repository.processors.CommonPolicyTakenStatusRepository;
import com.rpa.repository.processors.VolvoPolicyRepository;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;
import com.rpa.util.UtilityFile;

@Service
public class VolvoPolicyServiceImpl implements VolvoPolicyService {

	@Autowired
	private VolvoPolicyRepository volvoPolicyRepository;
	
	@Autowired
	private CommonPolicyTakenStatusRepository commonPolicyTakenStatusRepository;

	@Override
	public List<VolvoPolicy> findAll() {
		return volvoPolicyRepository.findAll();
	}

	@Override
	public List<String> findYesterdayPolicyNo() {
		return volvoPolicyRepository.findYesterdayPolicyNo();
	}

	@Override
	public List<VolvoPolicy> save(List<VolvoPolicy> newMarutiPolicyList) {
		return volvoPolicyRepository.save(newMarutiPolicyList);
	}

	@Override
	public List<VolvoPolicy> findPdfUnUploadedPolicies(Long thresholdCount) {
		return volvoPolicyRepository.findPdfUnUploadedPolicies(thresholdCount);
	}

	@Override
	public VolvoPolicy findByPolicyNo(String policyNo) {
		return volvoPolicyRepository.findByPolicyNo(policyNo);
	}

	@Override
	public VolvoPolicy save(VolvoPolicy hondaPolicy) {
		return volvoPolicyRepository.save(hondaPolicy);
	}

	@Override
	public String getLastId(String paramValue) {
		return volvoPolicyRepository.UPDATE_MAX_ID(paramValue);
	}

	@Override
	public CarDailyStatusResponse getMarutiDailyStatus(String policyDate)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String totalPolicies = getTotalPolicies("D", policyDate);
		/*
		 * String totalPolicies =
		 * volvoPolicyRepository.getTotalPolicies(policyDate);
		 */
		String extractedPolicies = volvoPolicyRepository.getExtractedPolicies(policyDate);
		String extractedProposals = volvoPolicyRepository.getExtractedProposals(policyDate);
		String uploadedPolicies = volvoPolicyRepository.getUploadedPolicies(policyDate);
		String uploadedProposals = volvoPolicyRepository.getUploadedProposals(policyDate);
		String policyPdfErrorCount = volvoPolicyRepository.getErrorPolicies(policyDate);
		String proposalPdfErrorCount = volvoPolicyRepository.getErrorProposals(policyDate);
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
			
			sql = "select  count(*) from TTRN_volvo_NEWRENEWAL_SERVICE where POLICY_NO not like 'DUM%' and TO_DATE(REQUEST_DATE,'DD/MM/YYYY')= to_date('"
					+ policyDate + "','" + RPAConstants.mm_slash_dd_slash_yyyy + "') and payment_yn='Y' ";
		} else if (flag.equalsIgnoreCase("M")) {
			sql = "select count(*) from TTRN_volvo_NEWRENEWAL_SERVICE  v where POLICY_NO not like 'DUM%' and TO_CHAR(TO_DATE(REQUEST_DATE,'DD/MM/YYYY'),'MM/YYYY')= '"
					+ policyDate + "' and payment_yn='Y' ";
		} else if (flag.equalsIgnoreCase("Y")) {
			sql = "select count(*) from TTRN_volvo_NEWRENEWAL_SERVICE  v where POLICY_NO not like 'DUM%' and TO_CHAR(TO_DATE(REQUEST_DATE,'DD/MM/YYYY'),'YYYY')= '"
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
			dateList = volvoPolicyRepository.getMarutiPendingPolicyDateList(monthYear);
		} else {
			dateList = volvoPolicyRepository.getMarutiUploadedPoliyDateList(monthYear);
		}
		return dateList;
	}

	@Override
	public List<String> findBacklogPolicyNo(String startDate, String endDate) {
		return volvoPolicyRepository.findBacklogPolicyNo(startDate, endDate);
	}

	@Override
	public List<VolvoPolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return volvoPolicyRepository.findPdfUnUploadedBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public List<String> getCarTypeList() {
		return volvoPolicyRepository.getCarTypeList();
	}

	@Override
	public List<VolvoPolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType) {
		return volvoPolicyRepository.getCarPolicyExtractionDetails(startDate, endDate, carType);
	}

	@Override
	public List<VolvoPolicy> findPdfToBeDownloaded(Long thresholdCount) {
		return volvoPolicyRepository.findPdfToBeDownloaded(thresholdCount);
	}

	@Override
	public List<VolvoPolicy> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return volvoPolicyRepository.findPdfToBeDownloadedForBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String monthYear = policyMonth + "/" + policyYear;
		String totalPolicies = getTotalPolicies("M", monthYear);
		/*
		 * String totalPolicies =
		 * volvoPolicyRepository.getTotalMonthlyPolicies(monthYear);
		 */
		String extractedPolicies = volvoPolicyRepository.getMonthlyExtractedPolicies(monthYear);
		String extractedProposals = volvoPolicyRepository.geMonthlytExtractedProposals(monthYear);
		String uploadedPolicies = volvoPolicyRepository.getMonthlyUploadedPolicies(monthYear);
		String uploadedProposals = volvoPolicyRepository.getMonthlyUploadedProposals(monthYear);
		String policyPdfErrorCount = volvoPolicyRepository.getMonthlyErrorPolicies(monthYear);
		String proposalPdfErrorCount = volvoPolicyRepository.getMonthlyErrorProposals(monthYear);
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
		 * volvoPolicyRepository.getTotalYearlyPolicies(policyYear);
		 */
		String extractedPolicies = volvoPolicyRepository.getYearlyExtractedPolicies(policyYear);
		String extractedProposals = volvoPolicyRepository.geYearlytExtractedProposals(policyYear);
		String uploadedPolicies = volvoPolicyRepository.getYearlyUploadedPolicies(policyYear);
		String uploadedProposals = volvoPolicyRepository.getYearlyUploadedProposals(policyYear);
		String policyPdfErrorCount = volvoPolicyRepository.getYearlyErrorPolicies(policyYear);
		String proposalPdfErrorCount = volvoPolicyRepository.getYearlyErrorProposals(policyYear);
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
	public List<CommonPolicyTakenStatus> getPendingPolicyDateListVolvo(String startDate) {
		return commonPolicyTakenStatusRepository.getPendingPolicyDateListVolvo(startDate);
	}

	@Override
	public CommonPolicyTakenStatus save(CommonPolicyTakenStatus commonPolicyTakenStatus) {
		return commonPolicyTakenStatusRepository.save(commonPolicyTakenStatus);
	}

	@Override
	public List<String> findExistingPolicyList(String policyDate) {
		return volvoPolicyRepository.findExistingPolicyList(policyDate);
	}

	@Override
	public MarutiUploadReferenceResponse getUploadReference(String policyNo,String flag) {
		MarutiUploadReferenceResponse marutiUploadReferenceResponse = new MarutiUploadReferenceResponse();
		VolvoPolicy marutiPolcy = volvoPolicyRepository.getUploadReference(policyNo);

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
	public List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListVolvo(String startDate, String endDate) {
		return commonPolicyTakenStatusRepository.getPendingBacklogPolicyDateListVolvo(startDate,endDate);
	}
	
	@Override
	public List<String> findExistingBacklogPolicyList(String policyDate) {
		return volvoPolicyRepository.findExistingBacklogPolicyList(policyDate);
	}

}
