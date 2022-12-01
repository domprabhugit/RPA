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
import com.rpa.model.processors.MiblPolicy;
import com.rpa.repository.processors.CommonPolicyTakenStatusRepository;
import com.rpa.repository.processors.MiblPolicyRepository;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;
import com.rpa.util.UtilityFile;

@Service
public class MiblPolicyServiceImpl implements MiblPolicyService {

	@Autowired
	private MiblPolicyRepository miblPolicyRepository;
	
	@Autowired
	private CommonPolicyTakenStatusRepository commonPolicyTakenStatusRepository;

	@Override
	public List<MiblPolicy> findAll() {
		return miblPolicyRepository.findAll();
	}

	@Override
	public List<String> findYesterdayPolicyNo() {
		return miblPolicyRepository.findYesterdayPolicyNo();
	}

	@Override
	public List<MiblPolicy> save(List<MiblPolicy> newMarutiPolicyList) {
		return miblPolicyRepository.save(newMarutiPolicyList);
	}

	@Override
	public List<MiblPolicy> findPdfUnUploadedPolicies(Long thresholdCount) {
		return miblPolicyRepository.findPdfUnUploadedPolicies(thresholdCount);
	}

	@Override
	public MiblPolicy findByPolicyNo(String policyNo) {
		return miblPolicyRepository.findByPolicyNo(policyNo);
	}

	@Override
	public MiblPolicy save(MiblPolicy hondaPolicy) {
		return miblPolicyRepository.save(hondaPolicy);
	}

	@Override
	public String getLastId(String paramValue) {
		return miblPolicyRepository.UPDATE_MAX_ID(paramValue);
	}

	@Override
	public CarDailyStatusResponse getMarutiDailyStatus(String policyDate)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String totalPolicies = getTotalPolicies("D", policyDate);
		/*
		 * String totalPolicies =
		 * miblPolicyRepository.getTotalPolicies(policyDate);
		 */
		String extractedPolicies = miblPolicyRepository.getExtractedPolicies(policyDate);
		String extractedProposals = miblPolicyRepository.getExtractedProposals(policyDate);
		String uploadedPolicies = miblPolicyRepository.getUploadedPolicies(policyDate);
		String uploadedProposals = miblPolicyRepository.getUploadedProposals(policyDate);
		String policyPdfErrorCount = miblPolicyRepository.getErrorPolicies(policyDate);
		String proposalPdfErrorCount = miblPolicyRepository.getErrorProposals(policyDate);
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
			
			sql = "select  count(*) from TTRN_MIBL_NEWRENEWAL_SERVICE where POLICYNO not like 'DUM%' and  TRUNC(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'DD/MM/YYYY')) = to_date('"
					+ policyDate + "','" + RPAConstants.mm_slash_dd_slash_yyyy + "') and payment_yn='Y' ";
		} else if (flag.equalsIgnoreCase("M")) {
			sql = "select count(*) from TTRN_MIBL_NEWRENEWAL_SERVICE  v where policyno not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'DD/MM/YYYY'),'MM/YYYY')= '"
					+ policyDate + "' and payment_yn='Y' ";
		} else if (flag.equalsIgnoreCase("Y")) {
			sql = "select count(*) from TTRN_MIBL_NEWRENEWAL_SERVICE  v where policyno not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'DD/MM/YYYY'),'YYYY')= '"
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
			dateList = miblPolicyRepository.getMarutiPendingPolicyDateList(monthYear);
		} else {
			dateList = miblPolicyRepository.getMarutiUploadedPoliyDateList(monthYear);
		}
		return dateList;
	}

	@Override
	public List<String> findBacklogPolicyNo(String startDate, String endDate) {
		return miblPolicyRepository.findBacklogPolicyNo(startDate, endDate);
	}

	@Override
	public List<MiblPolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return miblPolicyRepository.findPdfUnUploadedBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public List<String> getCarTypeList() {
		return miblPolicyRepository.getCarTypeList();
	}

	@Override
	public List<MiblPolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType) {
		return miblPolicyRepository.getCarPolicyExtractionDetails(startDate, endDate, carType);
	}

	@Override
	public List<MiblPolicy> findPdfToBeDownloaded(Long thresholdCount) {
		return miblPolicyRepository.findPdfToBeDownloaded(thresholdCount);
	}

	@Override
	public List<MiblPolicy> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return miblPolicyRepository.findPdfToBeDownloadedForBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String monthYear = policyMonth + "/" + policyYear;
		String totalPolicies = getTotalPolicies("M", monthYear);
		/*
		 * String totalPolicies =
		 * miblPolicyRepository.getTotalMonthlyPolicies(monthYear);
		 */
		String extractedPolicies = miblPolicyRepository.getMonthlyExtractedPolicies(monthYear);
		String extractedProposals = miblPolicyRepository.geMonthlytExtractedProposals(monthYear);
		String uploadedPolicies = miblPolicyRepository.getMonthlyUploadedPolicies(monthYear);
		String uploadedProposals = miblPolicyRepository.getMonthlyUploadedProposals(monthYear);
		String policyPdfErrorCount = miblPolicyRepository.getMonthlyErrorPolicies(monthYear);
		String proposalPdfErrorCount = miblPolicyRepository.getMonthlyErrorProposals(monthYear);
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
		 * miblPolicyRepository.getTotalYearlyPolicies(policyYear);
		 */
		String extractedPolicies = miblPolicyRepository.getYearlyExtractedPolicies(policyYear);
		String extractedProposals = miblPolicyRepository.geYearlytExtractedProposals(policyYear);
		String uploadedPolicies = miblPolicyRepository.getYearlyUploadedPolicies(policyYear);
		String uploadedProposals = miblPolicyRepository.getYearlyUploadedProposals(policyYear);
		String policyPdfErrorCount = miblPolicyRepository.getYearlyErrorPolicies(policyYear);
		String proposalPdfErrorCount = miblPolicyRepository.getYearlyErrorProposals(policyYear);
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
	public List<CommonPolicyTakenStatus> getPendingPolicyDateListMibl(String startDate) {
		return commonPolicyTakenStatusRepository.getPendingPolicyDateListMibl(startDate);
	}

	@Override
	public CommonPolicyTakenStatus save(CommonPolicyTakenStatus commonPolicyTakenStatus) {
		return commonPolicyTakenStatusRepository.save(commonPolicyTakenStatus);
	}

	@Override
	public List<String> findExistingPolicyList(String policyDate) {
		return miblPolicyRepository.findExistingPolicyList(policyDate);
	}

	@Override
	public MarutiUploadReferenceResponse getUploadReference(String policyNo,String flag) {
		MarutiUploadReferenceResponse marutiUploadReferenceResponse = new MarutiUploadReferenceResponse();
		MiblPolicy marutiPolcy = miblPolicyRepository.getUploadReference(policyNo);

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
	public List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListMibl(String startDate, String endDate) {
		return commonPolicyTakenStatusRepository.getPendingBacklogPolicyDateListMibl(startDate,endDate);
	}
	
	@Override
	public List<String> findExistingBacklogPolicyList(String policyDate) {
		return miblPolicyRepository.findExistingBacklogPolicyList(policyDate);
	}

}
