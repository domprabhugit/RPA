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
import com.rpa.model.processors.APPVIR;
import com.rpa.model.processors.CommonReportTakenStatus;
import com.rpa.repository.processors.APPVIRRepository;
import com.rpa.repository.processors.CommonReportTakenStatusRepository;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;
import com.rpa.util.UtilityFile;

@Service
public class AppVirServiceImpl implements AppVirService {

	@Autowired
	private APPVIRRepository appVIRRepository;
	
	@Autowired
	private CommonReportTakenStatusRepository commonReportTakenStatusRepository;

	@Override
	public List<APPVIR> findAll() {
		return appVIRRepository.findAll();
	}

	@Override
	public List<String> findYesterdayPolicyNo() {
		return appVIRRepository.findYesterdayPolicyNo();
	}

	@Override
	public List<APPVIR> save(List<APPVIR> newMarutiPolicyList) {
		return appVIRRepository.save(newMarutiPolicyList);
	}

	@Override
	public List<APPVIR> findPdfUnUploadedPolicies(Long thresholdCount) {
		return appVIRRepository.findPdfUnUploadedPolicies(thresholdCount);
	}

	@Override
	public APPVIR findByPolicyNo(String policyNo) {
		return appVIRRepository.findByPolicyNo(policyNo);
	}

	@Override
	public APPVIR save(APPVIR hondaPolicy) {
		return appVIRRepository.save(hondaPolicy);
	}

	@Override
	public String getLastId(String paramValue) {
		return appVIRRepository.UPDATE_MAX_ID(paramValue);
	}

	@Override
	public CarDailyStatusResponse getMarutiDailyStatus(String policyDate)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String totalPolicies = getTotalPolicies("D", policyDate);
		/*
		 * String totalPolicies =
		 * appVIRRepository.getTotalPolicies(policyDate);
		 */
		String extractedPolicies = appVIRRepository.getExtractedPolicies(policyDate);
		/*String extractedProposals = appVIRRepository.getExtractedProposals(policyDate);*/
		String uploadedPolicies = appVIRRepository.getUploadedPolicies(policyDate);
		/*String uploadedProposals = appVIRRepository.getUploadedProposals(policyDate);*/
		String policyPdfErrorCount = appVIRRepository.getErrorPolicies(policyDate);
		/*String proposalPdfErrorCount = appVIRRepository.getErrorProposals(policyDate);*/
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
			dateList = appVIRRepository.getMarutiPendingPolicyDateList(monthYear);
		} else {
			dateList = appVIRRepository.getMarutiUploadedPoliyDateList(monthYear);
		}
		return dateList;
	}

	@Override
	public List<String> findBacklogPolicyNo(String startDate, String endDate) {
		return appVIRRepository.findBacklogPolicyNo(startDate, endDate);
	}

	@Override
	public List<APPVIR> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return appVIRRepository.findPdfUnUploadedBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public List<String> getCarTypeList() {
		return appVIRRepository.getCarTypeList();
	}

	@Override
	public List<APPVIR> getCarPolicyExtractionDetails(String startDate, String endDate, String carType) {
		return appVIRRepository.getCarPolicyExtractionDetails(startDate, endDate, carType);
	}

	@Override
	public List<APPVIR> findPdfToBeDownloaded(Long thresholdCount) {
		return appVIRRepository.findPdfToBeDownloaded(thresholdCount);
	}

	@Override
	public List<APPVIR> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount) {
		return appVIRRepository.findPdfToBeDownloadedForBackLogPolicies(startDate, endDate,thresholdCount);
	}

	@Override
	public CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		String monthYear = policyMonth + "/" + policyYear;
		String totalPolicies = getTotalPolicies("M", monthYear);
		/*
		 * String totalPolicies =
		 * appVIRRepository.getTotalMonthlyPolicies(monthYear);
		 */
		String extractedPolicies = appVIRRepository.getMonthlyExtractedPolicies(monthYear);
		//String extractedProposals = appVIRRepository.geMonthlytExtractedProposals(monthYear);
		String uploadedPolicies = appVIRRepository.getMonthlyUploadedPolicies(monthYear);
		//String uploadedProposals = appVIRRepository.getMonthlyUploadedProposals(monthYear);
		String policyPdfErrorCount = appVIRRepository.getMonthlyErrorPolicies(monthYear);
		//String proposalPdfErrorCount = appVIRRepository.getMonthlyErrorProposals(monthYear);
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
		 * appVIRRepository.getTotalYearlyPolicies(policyYear);
		 */
		String extractedPolicies = appVIRRepository.getYearlyExtractedPolicies(policyYear);
		//String extractedProposals = appVIRRepository.geYearlytExtractedProposals(policyYear);
		String uploadedPolicies = appVIRRepository.getYearlyUploadedPolicies(policyYear);
		//String uploadedProposals = appVIRRepository.getYearlyUploadedProposals(policyYear);
		String policyPdfErrorCount = appVIRRepository.getYearlyErrorPolicies(policyYear);
		//String proposalPdfErrorCount = appVIRRepository.getYearlyErrorProposals(policyYear);
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
	public List<CommonReportTakenStatus> getPendingPolicyDateListVirApp(String startDate) {
		return commonReportTakenStatusRepository.getPendingPolicyDateListVirApp(startDate);
	}

	@Override
	public CommonReportTakenStatus save(CommonReportTakenStatus commonPolicyTakenStatus) {
		return commonReportTakenStatusRepository.save(commonPolicyTakenStatus);
	}

	@Override
	public List<String> findExistingPolicyList(String policyDate) {
		return appVIRRepository.findExistingPolicyList(policyDate);
	}

	@Override
	public MarutiUploadReferenceResponse getUploadReference(String policyNo,String flag) {
		MarutiUploadReferenceResponse marutiUploadReferenceResponse = new MarutiUploadReferenceResponse();
		APPVIR marutiPolcy = appVIRRepository.getUploadReference(policyNo);
		
		marutiUploadReferenceResponse.setUploadTime(marutiPolcy.getReportUploadedTime());
		marutiUploadReferenceResponse.setRequestXml(new String(marutiPolcy.getReportRequest()));
		marutiUploadReferenceResponse.setResponseXml(new String(marutiPolcy.getReportResponse()));
		return marutiUploadReferenceResponse;
	}

	@Override
	public List<CommonReportTakenStatus> getPendingBacklogPolicyDateListVirApp(String startDate, String endDate) {
		return commonReportTakenStatusRepository.getPendingBacklogPolicyDateListVirApp(startDate,endDate);
	}
	
	@Override
	public List<String> findExistingBacklogPolicyList(String policyDate) {
		return appVIRRepository.findExistingBacklogPolicyList(policyDate);
	}

}
