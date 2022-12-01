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
import com.rpa.model.processors.AutoInspektVIR;
import com.rpa.model.processors.CommonReportTakenStatus;
import com.rpa.repository.processors.AutoInspektVIRRepository;
import com.rpa.repository.processors.CommonReportTakenStatusRepository;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;
import com.rpa.util.UtilityFile;

@Service
public class AutoInspektVirServiceImpl implements AutoInspektVirService {

	@Autowired
	private AutoInspektVIRRepository autoInspektVIRRepository;
	
	@Autowired
	private CommonReportTakenStatusRepository commonReportTakenStatusRepository;

	@Override
	public List<AutoInspektVIR> findAll() {
		return autoInspektVIRRepository.findAll();
	}

	@Override
	public List<String> findYesterdayPolicyNo() {
		return autoInspektVIRRepository.findYesterdayPolicyNo();
	}

	@Override
	public List<AutoInspektVIR> save(List<AutoInspektVIR> newMarutiPolicyList) {
		return autoInspektVIRRepository.save(newMarutiPolicyList);
	}

	@Override
	public List<AutoInspektVIR> findPdfUnUploadedPolicies(Long thresholdCount) {
		return autoInspektVIRRepository.findPdfUnUploadedPolicies(thresholdCount);
	}

	@Override
	public AutoInspektVIR findByPolicyNo(String policyNo) {
		return autoInspektVIRRepository.findByPolicyNo(policyNo);
	}

	@Override
	public AutoInspektVIR save(AutoInspektVIR hondaPolicy) {
		return autoInspektVIRRepository.save(hondaPolicy);
	}

	@Override
	public String getLastId(String paramValue) {
		return autoInspektVIRRepository.UPDATE_MAX_ID(paramValue);
	}

	@Override
	public CarDailyStatusResponse getMarutiDailyStatus(String policyDate)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String totalPolicies = getTotalPolicies("D", policyDate);
		/*
		 * String totalPolicies =
		 * autoInspektVIRRepository.getTotalPolicies(policyDate);
		 */
		String extractedPolicies = autoInspektVIRRepository.getExtractedPolicies(policyDate);
		/*String extractedProposals = autoInspektVIRRepository.getExtractedProposals(policyDate);*/
		String uploadedPolicies = autoInspektVIRRepository.getUploadedPolicies(policyDate);
		/*String uploadedProposals = autoInspektVIRRepository.getUploadedProposals(policyDate);*/
		String policyPdfErrorCount = autoInspektVIRRepository.getErrorPolicies(policyDate);
		/*String proposalPdfErrorCount = autoInspektVIRRepository.getErrorProposals(policyDate);*/
		CarDailyStatusResponse marutiDailyStatusResponse = new CarDailyStatusResponse();
		marutiDailyStatusResponse.setTotalPolciesCount(totalPolicies);
		marutiDailyStatusResponse.setPolicyPdfExtractedCount(extractedPolicies);
		//marutiDailyStatusResponse.setProposalPdfExtractedCount(extractedProposals);
		marutiDailyStatusResponse.setPolicyPdfUploadedCount(uploadedPolicies);
		//marutiDailyStatusResponse.setProposalPdfUploadedCount(uploadedProposals);
		marutiDailyStatusResponse.setPolicyPdfErrorCount(policyPdfErrorCount);
		//marutiDailyStatusResponse.setProposalPdfErrorCount(proposalPdfErrorCount);
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
			sql = "select count(*) from insurancepolicy ins, motorcycle mc,privatepassengercar ppc,commercialvehicle cv, vir_agency v "
					+ "where ins.id = ppc.id(+) and ins.id = cv.id(+) and ins.id = mc.id(+)  and islatest =1 and ins.businessstatus != 'garbage' and nvl(nvl(ppc.SERVICE_PROVIDERNAME,nvl(cv.SERVICE_PROVIDERNAME,mc.SERVICE_PROVIDERNAME)),'NO VIR DONE')=v.MAIL AND TRUNC(TO_DATE(SUBSTR(INSPECTIONDATE,1,10),'MM/DD/YYYY'))= to_date('"
					+ policyDate + "','" + RPAConstants.mm_slash_dd_slash_yyyy + "') and SERVICE_PROVIDERNAME like '%Mahindra First Choice%' ";
		} else if (flag.equalsIgnoreCase("M")) {
			sql = "select count(*) from insurancepolicy ins, motorcycle mc,privatepassengercar ppc,commercialvehicle cv, vir_agency v "
					+ "where ins.id = ppc.id(+) and ins.id = cv.id(+) and ins.id = mc.id(+)  and islatest =1 and ins.businessstatus != 'garbage' and nvl(nvl(ppc.SERVICE_PROVIDERNAME,nvl(cv.SERVICE_PROVIDERNAME,mc.SERVICE_PROVIDERNAME)),'NO VIR DONE')=v.MAIL AND TO_CHAR(TO_DATE(SUBSTR(INSPECTIONDATE,1,10),'MM/DD/YYYY'),'MM/YYYY')= to_date('"
					+ policyDate + "' and SERVICE_PROVIDERNAME like '%Mahindra First Choice%' ";
		} else if (flag.equalsIgnoreCase("Y")) {
			sql = "select count(*) from insurancepolicy ins, motorcycle mc,privatepassengercar ppc,commercialvehicle cv, vir_agency v "
					+ "where ins.id = ppc.id(+) and ins.id = cv.id(+) and ins.id = mc.id(+)  and islatest =1 and ins.businessstatus != 'garbage' and nvl(nvl(ppc.SERVICE_PROVIDERNAME,nvl(cv.SERVICE_PROVIDERNAME,mc.SERVICE_PROVIDERNAME)),'NO VIR DONE')=v.MAIL AND TO_CHAR(TO_DATE(SUBSTR(INSPECTIONDATE,1,10),'MM/DD/YYYY'),'YYYY')= to_date('"
					+ policyDate + "' and SERVICE_PROVIDERNAME like '%Mahindra First Choice%' ";
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
			dateList = autoInspektVIRRepository.getMarutiPendingPolicyDateList(monthYear);
		} else {
			dateList = autoInspektVIRRepository.getMarutiUploadedPoliyDateList(monthYear);
		}
		return dateList;
	}

	@Override
	public List<String> findBacklogPolicyNo(String startDate, String endDate) {
		return autoInspektVIRRepository.findBacklogPolicyNo(startDate, endDate);
	}

	@Override
	public List<AutoInspektVIR> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return autoInspektVIRRepository.findPdfUnUploadedBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public List<String> getCarTypeList() {
		return autoInspektVIRRepository.getCarTypeList();
	}

	@Override
	public List<AutoInspektVIR> getCarPolicyExtractionDetails(String startDate, String endDate, String carType) {
		return autoInspektVIRRepository.getCarPolicyExtractionDetails(startDate, endDate, carType);
	}

	@Override
	public List<AutoInspektVIR> findPdfToBeDownloaded(Long thresholdCount) {
		return autoInspektVIRRepository.findPdfToBeDownloaded(thresholdCount);
	}

	@Override
	public List<AutoInspektVIR> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return autoInspektVIRRepository.findPdfToBeDownloadedForBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String monthYear = policyMonth + "/" + policyYear;
		String totalPolicies = getTotalPolicies("M", monthYear);
		/*
		 * String totalPolicies =
		 * autoInspektVIRRepository.getTotalMonthlyPolicies(monthYear);
		 */
		String extractedPolicies = autoInspektVIRRepository.getMonthlyExtractedPolicies(monthYear);
		//String extractedProposals = autoInspektVIRRepository.geMonthlytExtractedProposals(monthYear);
		String uploadedPolicies = autoInspektVIRRepository.getMonthlyUploadedPolicies(monthYear);
		//String uploadedProposals = autoInspektVIRRepository.getMonthlyUploadedProposals(monthYear);
		String policyPdfErrorCount = autoInspektVIRRepository.getMonthlyErrorPolicies(monthYear);
		//String proposalPdfErrorCount = autoInspektVIRRepository.getMonthlyErrorProposals(monthYear);
		CarDailyStatusResponse marutiDailyStatusResponse = new CarDailyStatusResponse();
		marutiDailyStatusResponse.setTotalPolciesCount(totalPolicies);
		marutiDailyStatusResponse.setPolicyPdfExtractedCount(extractedPolicies);
		//marutiDailyStatusResponse.setProposalPdfExtractedCount(extractedProposals);
		marutiDailyStatusResponse.setPolicyPdfUploadedCount(uploadedPolicies);
		//marutiDailyStatusResponse.setProposalPdfUploadedCount(uploadedProposals);
		marutiDailyStatusResponse.setPolicyPdfErrorCount(policyPdfErrorCount);
		//marutiDailyStatusResponse.setProposalPdfErrorCount(proposalPdfErrorCount);
		return marutiDailyStatusResponse;
	}

	@Override
	public CarDailyStatusResponse getMarutiYearlyStatus(String policyYear)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String totalPolicies = getTotalPolicies("Y", policyYear);
		/*
		 * String totalPolicies =
		 * autoInspektVIRRepository.getTotalYearlyPolicies(policyYear);
		 */
		String extractedPolicies = autoInspektVIRRepository.getYearlyExtractedPolicies(policyYear);
		//String extractedProposals = autoInspektVIRRepository.geYearlytExtractedProposals(policyYear);
		String uploadedPolicies = autoInspektVIRRepository.getYearlyUploadedPolicies(policyYear);
		//String uploadedProposals = autoInspektVIRRepository.getYearlyUploadedProposals(policyYear);
		String policyPdfErrorCount = autoInspektVIRRepository.getYearlyErrorPolicies(policyYear);
		//String proposalPdfErrorCount = autoInspektVIRRepository.getYearlyErrorProposals(policyYear);
		CarDailyStatusResponse marutiDailyStatusResponse = new CarDailyStatusResponse();
		marutiDailyStatusResponse.setTotalPolciesCount(totalPolicies);
		marutiDailyStatusResponse.setPolicyPdfExtractedCount(extractedPolicies);
		//marutiDailyStatusResponse.setProposalPdfExtractedCount(extractedProposals);
		marutiDailyStatusResponse.setPolicyPdfUploadedCount(uploadedPolicies);
		//marutiDailyStatusResponse.setProposalPdfUploadedCount(uploadedProposals);
		marutiDailyStatusResponse.setPolicyPdfErrorCount(policyPdfErrorCount);
		//marutiDailyStatusResponse.setProposalPdfErrorCount(proposalPdfErrorCount);
		return marutiDailyStatusResponse;
	}

	@Override
	public List<CommonReportTakenStatus> getPendingPolicyDateListAutoInspekt(String startDate) {
		return commonReportTakenStatusRepository.getPendingPolicyDateListAutoInspekt(startDate);
	}

	@Override
	public CommonReportTakenStatus save(CommonReportTakenStatus commonPolicyTakenStatus) {
		return commonReportTakenStatusRepository.save(commonPolicyTakenStatus);
	}

	@Override
	public List<String> findExistingPolicyList(String policyDate) {
		return autoInspektVIRRepository.findExistingPolicyList(policyDate);
	}

	@Override
	public MarutiUploadReferenceResponse getUploadReference(String policyNo,String flag) {
		MarutiUploadReferenceResponse marutiUploadReferenceResponse = new MarutiUploadReferenceResponse();
		AutoInspektVIR marutiPolcy = autoInspektVIRRepository.getUploadReference(policyNo);
		
		marutiUploadReferenceResponse.setUploadTime(marutiPolcy.getReportUploadedTime());
		marutiUploadReferenceResponse.setRequestXml(new String(marutiPolcy.getReportRequest()));
		marutiUploadReferenceResponse.setResponseXml(new String(marutiPolcy.getReportResponse()));
		return marutiUploadReferenceResponse;
	}

	@Override
	public List<CommonReportTakenStatus> getPendingBacklogPolicyDateListAutoInspekt(String startDate, String endDate) {
		return commonReportTakenStatusRepository.getPendingBacklogPolicyDateListAutoInspekt(startDate,endDate);
	}
	
	@Override
	public List<String> findExistingBacklogPolicyList(String policyDate) {
		return autoInspektVIRRepository.findExistingBacklogPolicyList(policyDate);
	}

}
