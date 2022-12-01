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
import com.rpa.model.processors.FirstgenPolicy;
import com.rpa.repository.processors.CommonPolicyTakenStatusRepository;
import com.rpa.repository.processors.FirstgenDownloadPolicyRepository;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;
import com.rpa.util.UtilityFile;

@Service
public class FirstGenDownloadPolicyServiceImpl implements FirstGenDownloadPolicyService {

	@Autowired
	private FirstgenDownloadPolicyRepository firstgenDownloadPolicyRepository;
	
	@Autowired
	private CommonPolicyTakenStatusRepository commonPolicyTakenStatusRepository;

	@Override
	public List<FirstgenPolicy> findAll() {
		return firstgenDownloadPolicyRepository.findAll();
	}

	@Override
	public List<String> findYesterdayPolicyNo() {
		return firstgenDownloadPolicyRepository.findYesterdayPolicyNo();
	}

	@Override
	public List<FirstgenPolicy> save(List<FirstgenPolicy> newMarutiPolicyList) {
		return firstgenDownloadPolicyRepository.save(newMarutiPolicyList);
	}

	@Override
	public List<FirstgenPolicy> findPdfUnUploadedPolicies(Long thresholdCount) {
		return firstgenDownloadPolicyRepository.findPdfUnUploadedPolicies(thresholdCount);
	}

	@Override
	public FirstgenPolicy findByPolicyNo(String policyNo) {
		return firstgenDownloadPolicyRepository.findByPolicyNo(policyNo);
	}

	@Override
	public FirstgenPolicy save(FirstgenPolicy hondaPolicy) {
		return firstgenDownloadPolicyRepository.save(hondaPolicy);
	}

	@Override
	public String getLastId(String paramValue) {
		return firstgenDownloadPolicyRepository.UPDATE_MAX_ID(paramValue);
	}

	@Override
	public CarDailyStatusResponse getMarutiDailyStatus(String policyDate)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String totalPolicies = getTotalPolicies("D", policyDate);
		/*
		 * String totalPolicies =
		 * firstgenDownloadPolicyRepository.getTotalPolicies(policyDate);
		 */
		String extractedPolicies = firstgenDownloadPolicyRepository.getExtractedPolicies(policyDate);
		String extractedProposals = firstgenDownloadPolicyRepository.getExtractedProposals(policyDate);
		String uploadedPolicies = firstgenDownloadPolicyRepository.getUploadedPolicies(policyDate);
		String uploadedProposals = firstgenDownloadPolicyRepository.getUploadedProposals(policyDate);
		String policyPdfErrorCount = firstgenDownloadPolicyRepository.getErrorPolicies(policyDate);
		String proposalPdfErrorCount = firstgenDownloadPolicyRepository.getErrorProposals(policyDate);
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
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		if (flag.equalsIgnoreCase("D")) {
			sql="select count(*) from IUMPL1A@portal h where h.IUWP1_CAMP_CD in ('DS06','DS07') and h.IUWP1_ACC_CD='AG020227' and h.IUWP1_POL_ENDT_TXT='Renewal' AND to_char(to_date (h.IUWP1_ISS_DT + 2378496, 'j' ),'dd-mm-yyyy')=to_char(to_date('"+policyDate+"','mm/dd/yyyy'),'dd-mm-yyyy')";
		} else if (flag.equalsIgnoreCase("M")) {
			sql = "select count(*) from IUMPL1A@portal h where h.IUWP1_CAMP_CD in ('DS06','DS07') and h.IUWP1_ACC_CD='AG020227' and h.IUWP1_POL_ENDT_TXT='Renewal' AND to_char(to_date (h.IUWP1_ISS_DT + 2378496, 'j' ),'mm/yyyy')='"+policyDate+"' ";
		} else if (flag.equalsIgnoreCase("Y")) {
			sql = "select count(*) from IUMPL1A@portal h where h.IUWP1_CAMP_CD in ('DS06','DS07') and h.IUWP1_ACC_CD='AG020227' and h.IUWP1_POL_ENDT_TXT='Renewal' AND to_char(to_date (h.IUWP1_ISS_DT + 2378496, 'j' ),'yyyy')='"+policyDate+"' ";
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
			dateList = firstgenDownloadPolicyRepository.getMarutiPendingPolicyDateList(monthYear);
		} else {
			dateList = firstgenDownloadPolicyRepository.getMarutiUploadedPoliyDateList(monthYear);
		}
		return dateList;
	}

	@Override
	public List<String> findBacklogPolicyNo(String startDate, String endDate) {
		return firstgenDownloadPolicyRepository.findBacklogPolicyNo(startDate, endDate);
	}

	@Override
	public List<FirstgenPolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return firstgenDownloadPolicyRepository.findPdfUnUploadedBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public List<String> getCarTypeList() {
		return firstgenDownloadPolicyRepository.getCarTypeList();
	}

	@Override
	public List<FirstgenPolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType) {
		return firstgenDownloadPolicyRepository.getCarPolicyExtractionDetails(startDate, endDate, carType);
	}

	@Override
	public List<FirstgenPolicy> findPdfToBeDownloaded(Long thresholdCount) {
		return firstgenDownloadPolicyRepository.findPdfToBeDownloaded(thresholdCount);
	}

	@Override
	public List<FirstgenPolicy> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return firstgenDownloadPolicyRepository.findPdfToBeDownloadedForBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String monthYear = policyMonth + "/" + policyYear;
		String totalPolicies = getTotalPolicies("M", monthYear);
		/*
		 * String totalPolicies =
		 * firstgenDownloadPolicyRepository.getTotalMonthlyPolicies(monthYear);
		 */
		String extractedPolicies = firstgenDownloadPolicyRepository.getMonthlyExtractedPolicies(monthYear);
		String extractedProposals = firstgenDownloadPolicyRepository.geMonthlytExtractedProposals(monthYear);
		String uploadedPolicies = firstgenDownloadPolicyRepository.getMonthlyUploadedPolicies(monthYear);
		String uploadedProposals = firstgenDownloadPolicyRepository.getMonthlyUploadedProposals(monthYear);
		String policyPdfErrorCount = firstgenDownloadPolicyRepository.getMonthlyErrorPolicies(monthYear);
		String proposalPdfErrorCount = firstgenDownloadPolicyRepository.getMonthlyErrorProposals(monthYear);
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
		 * firstgenDownloadPolicyRepository.getTotalYearlyPolicies(policyYear);
		 */
		String extractedPolicies = firstgenDownloadPolicyRepository.getYearlyExtractedPolicies(policyYear);
		String extractedProposals = firstgenDownloadPolicyRepository.geYearlytExtractedProposals(policyYear);
		String uploadedPolicies = firstgenDownloadPolicyRepository.getYearlyUploadedPolicies(policyYear);
		String uploadedProposals = firstgenDownloadPolicyRepository.getYearlyUploadedProposals(policyYear);
		String policyPdfErrorCount = firstgenDownloadPolicyRepository.getYearlyErrorPolicies(policyYear);
		String proposalPdfErrorCount = firstgenDownloadPolicyRepository.getYearlyErrorProposals(policyYear);
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
	public List<CommonPolicyTakenStatus> getPendingPolicyDateListFirstGen(String startDate) {
		return commonPolicyTakenStatusRepository.getPendingPolicyDateListFord(startDate);
	}

	@Override
	public CommonPolicyTakenStatus save(CommonPolicyTakenStatus commonPolicyTakenStatus) {
		return commonPolicyTakenStatusRepository.save(commonPolicyTakenStatus);
	}

	@Override
	public List<String> findExistingPolicyList(String policyDate) {
		return firstgenDownloadPolicyRepository.findExistingPolicyList(policyDate);
	}

	@Override
	public MarutiUploadReferenceResponse getUploadReference(String policyNo,String flag) {
		MarutiUploadReferenceResponse marutiUploadReferenceResponse = new MarutiUploadReferenceResponse();
		FirstgenPolicy marutiPolcy = firstgenDownloadPolicyRepository.getUploadReference(policyNo);

		if(flag.equalsIgnoreCase("PO")){
			marutiUploadReferenceResponse.setUploadTime(marutiPolcy.getPolicyPdfUploadedTime());
			marutiUploadReferenceResponse.setRequestXml(new String(marutiPolcy.getPolicyRequest()));
			marutiUploadReferenceResponse.setResponseXml(new String(marutiPolcy.getPolicyResponse()));
		}else{
			marutiUploadReferenceResponse.setUploadTime(marutiPolcy.getInvoicePdfUploadedTime());
			marutiUploadReferenceResponse.setRequestXml(new String(marutiPolcy.getInvoiceRequest()));
			marutiUploadReferenceResponse.setResponseXml(new String(marutiPolcy.getInvoiceResponse()));
		}

		return marutiUploadReferenceResponse;
	}

	@Override
	public List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListFirstGen(String startDate, String endDate) {
		return commonPolicyTakenStatusRepository.getPendingBacklogPolicyDateListFord(startDate,endDate);
	}
	
	@Override
	public List<String> findExistingBacklogPolicyList(String policyDate) {
		return firstgenDownloadPolicyRepository.findExistingBacklogPolicyList(policyDate);
	}

	@Override
	public List<FirstgenPolicy> findErrorPdfUploadedPolicies(Long thresholdFrequencyLevel) {
		return firstgenDownloadPolicyRepository.findErrorPdfUploadedPolicies(thresholdFrequencyLevel);
	}

}
