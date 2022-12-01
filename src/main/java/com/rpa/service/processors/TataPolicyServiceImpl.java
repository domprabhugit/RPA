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
import com.rpa.model.processors.TataPolicy;
import com.rpa.repository.processors.CommonPolicyTakenStatusRepository;
import com.rpa.repository.processors.TataPolicyRepository;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;
import com.rpa.util.UtilityFile;

@Service
public class TataPolicyServiceImpl implements TataPolicyService {

	@Autowired
	private TataPolicyRepository tataPolicyRepository;
	
	@Autowired
	private CommonPolicyTakenStatusRepository commonPolicyTakenStatusRepository;

	@Override
	public List<TataPolicy> findAll() {
		return tataPolicyRepository.findAll();
	}

	@Override
	public List<String> findYesterdayPolicyNo() {
		return tataPolicyRepository.findYesterdayPolicyNo();
	}

	@Override
	public List<TataPolicy> save(List<TataPolicy> newMarutiPolicyList) {
		return tataPolicyRepository.save(newMarutiPolicyList);
	}

	@Override
	public List<TataPolicy> findPdfUnUploadedPolicies(Long thresholdCount) {
		return tataPolicyRepository.findPdfUnUploadedPolicies(thresholdCount);
	}

	@Override
	public TataPolicy findByPolicyNo(String policyNo) {
		return tataPolicyRepository.findByPolicyNo(policyNo);
	}

	@Override
	public TataPolicy save(TataPolicy hondaPolicy) {
		return tataPolicyRepository.save(hondaPolicy);
	}

	@Override
	public String getLastId(String paramValue) {
		return tataPolicyRepository.UPDATE_MAX_ID(paramValue);
	}

	@Override
	public CarDailyStatusResponse getMarutiDailyStatus(String policyDate)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String totalPolicies = getTotalPolicies("D", policyDate);
		/*
		 * String totalPolicies =
		 * tataPolicyRepository.getTotalPolicies(policyDate);
		 */
		String extractedPolicies = tataPolicyRepository.getExtractedPolicies(policyDate);
		String extractedProposals = tataPolicyRepository.getExtractedProposals(policyDate);
		String uploadedPolicies = tataPolicyRepository.getUploadedPolicies(policyDate);
		String uploadedProposals = tataPolicyRepository.getUploadedProposals(policyDate);
		String policyPdfErrorCount = tataPolicyRepository.getErrorPolicies(policyDate);
		String proposalPdfErrorCount = tataPolicyRepository.getErrorProposals(policyDate);
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
			sql = "select count(*) from ttrn_TATA_newservice  v where policyno not like 'DUM%' and TRUNC(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'))= to_date('"
					+ policyDate + "','" + RPAConstants.mm_slash_dd_slash_yyyy + "') and payment_yn='Y' ";
		} else if (flag.equalsIgnoreCase("M")) {
			sql = "select count(*) from ttrn_TATA_newservice  v where policyno not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/YYYY')= '"
					+ policyDate + "' and payment_yn='Y' ";
		} else if (flag.equalsIgnoreCase("Y")) {
			sql = "select count(*) from ttrn_TATA_newservice  v where policyno not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'YYYY')= '"
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
			dateList = tataPolicyRepository.getMarutiPendingPolicyDateList(monthYear);
		} else {
			dateList = tataPolicyRepository.getMarutiUploadedPoliyDateList(monthYear);
		}
		return dateList;
	}

	@Override
	public List<String> findBacklogPolicyNo(String startDate, String endDate) {
		return tataPolicyRepository.findBacklogPolicyNo(startDate, endDate);
	}

	@Override
	public List<TataPolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return tataPolicyRepository.findPdfUnUploadedBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public List<String> getCarTypeList() {
		return tataPolicyRepository.getCarTypeList();
	}

	@Override
	public List<TataPolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType) {
		return tataPolicyRepository.getCarPolicyExtractionDetails(startDate, endDate, carType);
	}

	@Override
	public List<TataPolicy> findPdfToBeDownloadedPV(Long thresholdCount) {
		return tataPolicyRepository.findPdfToBeDownloadedPV(thresholdCount);
	}
	
	@Override
	public List<TataPolicy> findPdfToBeDownloadedCV(Long thresholdCount) {
		return tataPolicyRepository.findPdfToBeDownloadedCV(thresholdCount);
	}

	@Override
	public List<TataPolicy> findPdfToBeDownloadedForBackLogPoliciesPV(String startDate, String endDate,Long thresholdCount) {
		return tataPolicyRepository.findPdfToBeDownloadedForBackLogPoliciesPV(startDate, endDate,thresholdCount);
	}
	
	@Override
	public List<TataPolicy> findPdfToBeDownloadedForBackLogPoliciesCV(String startDate, String endDate,Long thresholdCount) {
		return tataPolicyRepository.findPdfToBeDownloadedForBackLogPoliciesCV(startDate, endDate,thresholdCount);
	}

	@Override
	public CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String monthYear = policyMonth + "/" + policyYear;
		String totalPolicies = getTotalPolicies("M", monthYear);
		/*
		 * String totalPolicies =
		 * tataPolicyRepository.getTotalMonthlyPolicies(monthYear);
		 */
		String extractedPolicies = tataPolicyRepository.getMonthlyExtractedPolicies(monthYear);
		String extractedProposals = tataPolicyRepository.geMonthlytExtractedProposals(monthYear);
		String uploadedPolicies = tataPolicyRepository.getMonthlyUploadedPolicies(monthYear);
		String uploadedProposals = tataPolicyRepository.getMonthlyUploadedProposals(monthYear);
		String policyPdfErrorCount = tataPolicyRepository.getMonthlyErrorPolicies(monthYear);
		String proposalPdfErrorCount = tataPolicyRepository.getMonthlyErrorProposals(monthYear);
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
		 * tataPolicyRepository.getTotalYearlyPolicies(policyYear);
		 */
		String extractedPolicies = tataPolicyRepository.getYearlyExtractedPolicies(policyYear);
		String extractedProposals = tataPolicyRepository.geYearlytExtractedProposals(policyYear);
		String uploadedPolicies = tataPolicyRepository.getYearlyUploadedPolicies(policyYear);
		String uploadedProposals = tataPolicyRepository.getYearlyUploadedProposals(policyYear);
		String policyPdfErrorCount = tataPolicyRepository.getYearlyErrorPolicies(policyYear);
		String proposalPdfErrorCount = tataPolicyRepository.getYearlyErrorProposals(policyYear);
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
	public List<CommonPolicyTakenStatus> getPendingPolicyDateListTataPV(String startDate) {
		return commonPolicyTakenStatusRepository.getPendingPolicyDateListTataPV(startDate);
	}
	
	@Override
	public List<CommonPolicyTakenStatus> getPendingPolicyDateListTataCV(String startDate) {
		return commonPolicyTakenStatusRepository.getPendingPolicyDateListTataCV(startDate);
	}

	@Override
	public CommonPolicyTakenStatus save(CommonPolicyTakenStatus commonPolicyTakenStatus) {
		return commonPolicyTakenStatusRepository.save(commonPolicyTakenStatus);
	}

	@Override
	public List<String> findExistingPolicyListPV(String policyDate) {
		return tataPolicyRepository.findExistingPolicyListPV(policyDate);
	}
	
	@Override
	public List<String> findExistingPolicyListCV(String policyDate) {
		return tataPolicyRepository.findExistingPolicyListCV(policyDate);
	}

	@Override
	public MarutiUploadReferenceResponse getUploadReference(String policyNo,String flag) {
		MarutiUploadReferenceResponse marutiUploadReferenceResponse = new MarutiUploadReferenceResponse();
		TataPolicy marutiPolcy = tataPolicyRepository.getUploadReference(policyNo);

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
	public List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListTataPV(String startDate, String endDate) {
		return commonPolicyTakenStatusRepository.getPendingBacklogPolicyDateListTataPV(startDate,endDate);
	}
	
	@Override
	public List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListTataCV(String startDate, String endDate) {
		return commonPolicyTakenStatusRepository.getPendingBacklogPolicyDateListTataCV(startDate,endDate);
	}
	
	@Override
	public List<String> findExistingBacklogPolicyListPV(String policyDate) {
		return tataPolicyRepository.findExistingBacklogPolicyListPV(policyDate);
	}
	
	@Override
	public List<String> findExistingBacklogPolicyListCV(String policyDate) {
		return tataPolicyRepository.findExistingBacklogPolicyListCV(policyDate);
	}

}
