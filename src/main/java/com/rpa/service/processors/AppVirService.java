/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import com.rpa.model.processors.APPVIR;
import com.rpa.model.processors.CommonReportTakenStatus;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;

public interface AppVirService {
	
    List<APPVIR> findAll();
    
    List<String> findYesterdayPolicyNo();

    List<APPVIR> save(List<APPVIR> newMarutiPolicyList);

    List<APPVIR> findPdfUnUploadedPolicies(Long thresholdCount);

	APPVIR findByPolicyNo(String policyNo);
	
	APPVIR save(APPVIR APPVIR);
	
	String getLastId(String paramValue);

	CarDailyStatusResponse getMarutiDailyStatus(String policyDate) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<String> getMarutiPolicyDateList(String monthYear, String statusFlag);

	List<String> findBacklogPolicyNo(String startDate, String endDate);

	List<APPVIR> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	List<String> getCarTypeList();

	List<APPVIR> getCarPolicyExtractionDetails(String startDate, String endDate, String carType);

	List<APPVIR> findPdfToBeDownloaded(Long thresholdCount);

	List<APPVIR> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	CarDailyStatusResponse getMarutiYearlyStatus(String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<CommonReportTakenStatus> getPendingPolicyDateListVirApp(String string);

	CommonReportTakenStatus save(CommonReportTakenStatus marutiPolicyTakenStatus);

	List<String> findExistingPolicyList(String policyDate);

	MarutiUploadReferenceResponse getUploadReference(String policyNo, String flag);

	List<String> findExistingBacklogPolicyList(String policyDate);

	List<CommonReportTakenStatus> getPendingBacklogPolicyDateListVirApp(String string, String string2);

}
