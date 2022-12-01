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
import com.rpa.model.processors.HondaPolicy;
import com.rpa.repository.processors.CommonPolicyTakenStatusRepository;
import com.rpa.repository.processors.HondaPolicyRepository;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;
import com.rpa.util.UtilityFile;

@Service
@Transactional
public class HondaPolicyServiceImpl implements HondaPolicyService {

	@Autowired
	private HondaPolicyRepository hondaPolicyRepository;
	
	@Autowired
	private CommonPolicyTakenStatusRepository commonPolicyTakenStatusRepository;

	@Override
	public List<HondaPolicy> findAll() {
		return hondaPolicyRepository.findAll();
	}

	@Override
	public List<String> findYesterdayPolicyNo() {
		return hondaPolicyRepository.findYesterdayPolicyNo();
	}

	@Override
	public List<HondaPolicy> save(List<HondaPolicy> newMarutiPolicyList) {
		return hondaPolicyRepository.save(newMarutiPolicyList);
	}

	@Override
	public List<HondaPolicy> findPdfUnUploadedPolicies(Long thresholdCount) {
		return hondaPolicyRepository.findPdfUnUploadedPolicies(thresholdCount);
	}

	@Override
	public HondaPolicy findByPolicyNo(String policyNo) {
		return hondaPolicyRepository.findByPolicyNo(policyNo);
	}

	@Override
	public HondaPolicy save(HondaPolicy hondaPolicy) {
		return hondaPolicyRepository.save(hondaPolicy);
	}

	@Override
	public String getLastId(String paramValue) {
		return hondaPolicyRepository.UPDATE_MAX_ID(paramValue);
	}

	@Override
	public CarDailyStatusResponse getMarutiDailyStatus(String policyDate)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String totalPolicies = getTotalPolicies("D", policyDate);
		/*
		 * String totalPolicies =
		 * hondaPolicyRepository.getTotalPolicies(policyDate);
		 */
		String extractedPolicies = hondaPolicyRepository.getExtractedPolicies(policyDate);
		String extractedProposals = hondaPolicyRepository.getExtractedProposals(policyDate);
		String uploadedPolicies = hondaPolicyRepository.getUploadedPolicies(policyDate);
		String uploadedProposals = hondaPolicyRepository.getUploadedProposals(policyDate);
		String policyPdfErrorCount = hondaPolicyRepository.getErrorPolicies(policyDate);
		String proposalPdfErrorCount = hondaPolicyRepository.getErrorProposals(policyDate);
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
			/*sql = "select count(*) from ttrn_newrenewal vv,ttrn_newrenewal_service jj where vv.POLICYNO not like 'DUM%' and TRUNC(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'))= to_date('"
					+ policyDate + "','" + RPAConstants.mm_slash_dd_slash_yyyy + "') and vv.POLICYNO=JJ.ICPOLICYNO ";*/
			sql = "select count(*) from ttrn_newrenewal_service where icpolicyno not like 'DUM%' and TRUNC(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'))= to_date('"
					+ policyDate + "','" + RPAConstants.mm_slash_dd_slash_yyyy + "') ";
		} else if (flag.equalsIgnoreCase("M")) {
			/*sql = "select count(*) from ttrn_newrenewal vv,ttrn_newrenewal_service jj where vv.POLICYNO not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/YYYY')= '"
					+ policyDate + "' and vv.POLICYNO=JJ.ICPOLICYNO ";*/
			sql = "select count(*) from ttrn_newrenewal_service where icpolicyno not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'MM/YYYY')= '"
					+ policyDate + "'";
		} else if (flag.equalsIgnoreCase("Y")) {
			/*sql = "select count(*) from ttrn_newrenewal vv,ttrn_newrenewal_service jj where vv.POLICYNO not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'YYYY')= '"
					+ policyDate + "' and vv.POLICYNO=JJ.ICPOLICYNO ";*/
			sql = "select count(*) from ttrn_newrenewal_service where icpolicyno not like 'DUM%' and TO_CHAR(TO_DATE(SUBSTR(REQUEST_TIME,1,10),'MM/DD/YYYY'),'YYYY')= '"
					+ policyDate + "'";
			
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
			dateList = hondaPolicyRepository.getMarutiPendingPolicyDateList(monthYear);
		} else {
			dateList = hondaPolicyRepository.getMarutiUploadedPoliyDateList(monthYear);
		}
		return dateList;
	}

	@Override
	public List<String> findBacklogPolicyNo(String startDate, String endDate) {
		return hondaPolicyRepository.findBacklogPolicyNo(startDate, endDate);
	}

	@Override
	public List<HondaPolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return hondaPolicyRepository.findPdfUnUploadedBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public List<String> getCarTypeList() {
		return hondaPolicyRepository.getCarTypeList();
	}

	@Override
	public List<HondaPolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType) {
		return hondaPolicyRepository.getCarPolicyExtractionDetails(startDate, endDate, carType);
	}

	@Override
	public List<HondaPolicy> findPdfToBeDownloaded(Long thresholdCount) {
		return hondaPolicyRepository.findPdfToBeDownloaded(thresholdCount);
	}

	@Override
	public List<HondaPolicy> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return hondaPolicyRepository.findPdfToBeDownloadedForBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String monthYear = policyMonth + "/" + policyYear;
		String totalPolicies = getTotalPolicies("M", monthYear);
		/*
		 * String totalPolicies =
		 * hondaPolicyRepository.getTotalMonthlyPolicies(monthYear);
		 */
		String extractedPolicies = hondaPolicyRepository.getMonthlyExtractedPolicies(monthYear);
		String extractedProposals = hondaPolicyRepository.geMonthlytExtractedProposals(monthYear);
		String uploadedPolicies = hondaPolicyRepository.getMonthlyUploadedPolicies(monthYear);
		String uploadedProposals = hondaPolicyRepository.getMonthlyUploadedProposals(monthYear);
		String policyPdfErrorCount = hondaPolicyRepository.getMonthlyErrorPolicies(monthYear);
		String proposalPdfErrorCount = hondaPolicyRepository.getMonthlyErrorProposals(monthYear);
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
		 * hondaPolicyRepository.getTotalYearlyPolicies(policyYear);
		 */
		String extractedPolicies = hondaPolicyRepository.getYearlyExtractedPolicies(policyYear);
		String extractedProposals = hondaPolicyRepository.geYearlytExtractedProposals(policyYear);
		String uploadedPolicies = hondaPolicyRepository.getYearlyUploadedPolicies(policyYear);
		String uploadedProposals = hondaPolicyRepository.getYearlyUploadedProposals(policyYear);
		String policyPdfErrorCount = hondaPolicyRepository.getYearlyErrorPolicies(policyYear);
		String proposalPdfErrorCount = hondaPolicyRepository.getYearlyErrorProposals(policyYear);
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
	public List<CommonPolicyTakenStatus> getPendingPolicyDateListHonda(String startDate) {
		return commonPolicyTakenStatusRepository.getPendingPolicyDateListHonda(startDate);
	}

	@Override
	public CommonPolicyTakenStatus save(CommonPolicyTakenStatus commonPolicyTakenStatus) {
		return commonPolicyTakenStatusRepository.save(commonPolicyTakenStatus);
	}

	@Override
	public List<String> findExistingPolicyList(String policyDate) {
		return hondaPolicyRepository.findExistingPolicyList(policyDate);
	}

	@Override
	public MarutiUploadReferenceResponse getUploadReference(String policyNo,String flag) {
		MarutiUploadReferenceResponse marutiUploadReferenceResponse = new MarutiUploadReferenceResponse();
		HondaPolicy marutiPolcy = hondaPolicyRepository.getUploadReference(policyNo);

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
	public List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListHonda(String startDate, String endDate) {
		return commonPolicyTakenStatusRepository.getPendingBacklogPolicyDateListHonda(startDate,endDate);
	}
	
	@Override
	public List<String> findExistingBacklogPolicyList(String policyDate) {
		return hondaPolicyRepository.findExistingBacklogPolicyList(policyDate);
	}

}
