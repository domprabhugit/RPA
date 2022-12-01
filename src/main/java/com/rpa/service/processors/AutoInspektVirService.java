/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import com.rpa.model.processors.AutoInspektVIR;
import com.rpa.model.processors.CommonReportTakenStatus;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;

public interface AutoInspektVirService {
	
    List<AutoInspektVIR> findAll();
    
    List<String> findYesterdayPolicyNo();

    List<AutoInspektVIR> save(List<AutoInspektVIR> newMarutiPolicyList);

    List<AutoInspektVIR> findPdfUnUploadedPolicies(Long thresholdCount);

	AutoInspektVIR findByPolicyNo(String policyNo);
	
	AutoInspektVIR save(AutoInspektVIR AutoInspektVIR);
	
	String getLastId(String paramValue);

	CarDailyStatusResponse getMarutiDailyStatus(String policyDate) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<String> getMarutiPolicyDateList(String monthYear, String statusFlag);

	List<String> findBacklogPolicyNo(String startDate, String endDate);

	List<AutoInspektVIR> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	List<String> getCarTypeList();

	List<AutoInspektVIR> getCarPolicyExtractionDetails(String startDate, String endDate, String carType);

	List<AutoInspektVIR> findPdfToBeDownloaded(Long thresholdCount);

	List<AutoInspektVIR> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	CarDailyStatusResponse getMarutiYearlyStatus(String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<CommonReportTakenStatus> getPendingPolicyDateListAutoInspekt(String string);

	CommonReportTakenStatus save(CommonReportTakenStatus marutiPolicyTakenStatus);

	List<String> findExistingPolicyList(String policyDate);

	MarutiUploadReferenceResponse getUploadReference(String policyNo, String flag);

	List<String> findExistingBacklogPolicyList(String policyDate);

	List<CommonReportTakenStatus> getPendingBacklogPolicyDateListAutoInspekt(String string, String string2);

}
