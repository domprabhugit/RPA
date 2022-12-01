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
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.model.processors.FordPolicy;
import com.rpa.repository.processors.CommonPolicyTakenStatusRepository;
import com.rpa.repository.processors.FordPolicyRepository;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;
import com.rpa.util.UtilityFile;

@Service
@Transactional
public class FordPolicyServiceImpl implements FordPolicyService {

	@Autowired
	private FordPolicyRepository fordPolicyRepository;
	
	@Autowired
	private CommonPolicyTakenStatusRepository commonPolicyTakenStatusRepository;

	@Override
	public List<FordPolicy> findAll() {
		return fordPolicyRepository.findAll();
	}

	@Override
	public List<String> findYesterdayPolicyNo() {
		return fordPolicyRepository.findYesterdayPolicyNo();
	}

	@Override
	public List<FordPolicy> save(List<FordPolicy> newMarutiPolicyList) {
		return fordPolicyRepository.save(newMarutiPolicyList);
	}

	@Override
	public List<FordPolicy> findPdfUnUploadedPolicies(Long thresholdCount) {
		return fordPolicyRepository.findPdfUnUploadedPolicies(thresholdCount);
	}

	@Override
	public FordPolicy findByPolicyNo(String policyNo) {
		return fordPolicyRepository.findByPolicyNo(policyNo);
	}

	@Override
	public FordPolicy save(FordPolicy hondaPolicy) {
		return fordPolicyRepository.save(hondaPolicy);
	}

	@Override
	public String getLastId(String paramValue) {
		return fordPolicyRepository.UPDATE_MAX_ID(paramValue);
	}

	@Override
	public CarDailyStatusResponse getMarutiDailyStatus(String policyDate)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String totalPolicies = getTotalPolicies("D", policyDate);
		/*
		 * String totalPolicies =
		 * fordPolicyRepository.getTotalPolicies(policyDate);
		 */
		String extractedPolicies = fordPolicyRepository.getExtractedPolicies(policyDate);
		String extractedProposals = fordPolicyRepository.getExtractedProposals(policyDate);
		String uploadedPolicies = fordPolicyRepository.getUploadedPolicies(policyDate);
		String uploadedProposals = fordPolicyRepository.getUploadedProposals(policyDate);
		String policyPdfErrorCount = fordPolicyRepository.getErrorPolicies(policyDate);
		String proposalPdfErrorCount = fordPolicyRepository.getErrorProposals(policyDate);
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
			
			sql = "select  count(*) from ttrn_ford_new_service where POLICYNO not like 'DUM%' and TRUNC(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'))= to_date('"
					+ policyDate + "','" + RPAConstants.mm_slash_dd_slash_yyyy + "') and payment_yn='Y' ";
		} else if (flag.equalsIgnoreCase("M")) {
			sql = "select count(*) from ttrn_ford_new_service  v where policyno not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/YYYY')= '"
					+ policyDate + "' and payment_yn='Y' ";
		} else if (flag.equalsIgnoreCase("Y")) {
			sql = "select count(*) from ttrn_ford_new_service  v where policyno not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'YYYY')= '"
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
			dateList = fordPolicyRepository.getMarutiPendingPolicyDateList(monthYear);
		} else {
			dateList = fordPolicyRepository.getMarutiUploadedPoliyDateList(monthYear);
		}
		return dateList;
	}

	@Override
	public List<String> findBacklogPolicyNo(String startDate, String endDate) {
		return fordPolicyRepository.findBacklogPolicyNo(startDate, endDate);
	}

	@Override
	public List<FordPolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return fordPolicyRepository.findPdfUnUploadedBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public List<String> getCarTypeList() {
		return fordPolicyRepository.getCarTypeList();
	}

	@Override
	public List<FordPolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType) {
		return fordPolicyRepository.getCarPolicyExtractionDetails(startDate, endDate, carType);
	}

	@Override
	public List<FordPolicy> findPdfToBeDownloaded(Long thresholdCount) {
		return fordPolicyRepository.findPdfToBeDownloaded(thresholdCount);
	}

	@Override
	public List<FordPolicy> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return fordPolicyRepository.findPdfToBeDownloadedForBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String monthYear = policyMonth + "/" + policyYear;
		String totalPolicies = getTotalPolicies("M", monthYear);
		/*
		 * String totalPolicies =
		 * fordPolicyRepository.getTotalMonthlyPolicies(monthYear);
		 */
		String extractedPolicies = fordPolicyRepository.getMonthlyExtractedPolicies(monthYear);
		String extractedProposals = fordPolicyRepository.geMonthlytExtractedProposals(monthYear);
		String uploadedPolicies = fordPolicyRepository.getMonthlyUploadedPolicies(monthYear);
		String uploadedProposals = fordPolicyRepository.getMonthlyUploadedProposals(monthYear);
		String policyPdfErrorCount = fordPolicyRepository.getMonthlyErrorPolicies(monthYear);
		String proposalPdfErrorCount = fordPolicyRepository.getMonthlyErrorProposals(monthYear);
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
		 * fordPolicyRepository.getTotalYearlyPolicies(policyYear);
		 */
		String extractedPolicies = fordPolicyRepository.getYearlyExtractedPolicies(policyYear);
		String extractedProposals = fordPolicyRepository.geYearlytExtractedProposals(policyYear);
		String uploadedPolicies = fordPolicyRepository.getYearlyUploadedPolicies(policyYear);
		String uploadedProposals = fordPolicyRepository.getYearlyUploadedProposals(policyYear);
		String policyPdfErrorCount = fordPolicyRepository.getYearlyErrorPolicies(policyYear);
		String proposalPdfErrorCount = fordPolicyRepository.getYearlyErrorProposals(policyYear);
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
	public List<CommonPolicyTakenStatus> getPendingPolicyDateListFord(String startDate) {
		return commonPolicyTakenStatusRepository.getPendingPolicyDateListFord(startDate);
	}

	@Override
	public CommonPolicyTakenStatus save(CommonPolicyTakenStatus commonPolicyTakenStatus) {
		return commonPolicyTakenStatusRepository.save(commonPolicyTakenStatus);
	}

	@Override
	public List<String> findExistingPolicyList(String policyDate) {
		return fordPolicyRepository.findExistingPolicyList(policyDate);
	}

	@Override
	public MarutiUploadReferenceResponse getUploadReference(String policyNo,String flag) {
		MarutiUploadReferenceResponse marutiUploadReferenceResponse = new MarutiUploadReferenceResponse();
		FordPolicy marutiPolcy = fordPolicyRepository.getUploadReference(policyNo);

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
	public List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListFord(String startDate, String endDate) {
		return commonPolicyTakenStatusRepository.getPendingBacklogPolicyDateListFord(startDate,endDate);
	}
	
	@Override
	public List<String> findExistingBacklogPolicyList(String policyDate) {
		return fordPolicyRepository.findExistingBacklogPolicyList(policyDate);
	}

}
