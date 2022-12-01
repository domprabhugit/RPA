/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import com.rpa.model.processors.AdroitVIR;
import com.rpa.model.processors.CommonReportTakenStatus;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;

public interface AdroitVirService {
	
    List<AdroitVIR> findAll();
    
    List<String> findYesterdayPolicyNo();

    List<AdroitVIR> save(List<AdroitVIR> newMarutiPolicyList);

    List<AdroitVIR> findPdfUnUploadedPolicies(Long thresholdCount);

	AdroitVIR findByPolicyNo(String policyNo);
	
	AdroitVIR save(AdroitVIR AdroitVIR);
	
	String getLastId(String paramValue);

	CarDailyStatusResponse getMarutiDailyStatus(String policyDate) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<String> getMarutiPolicyDateList(String monthYear, String statusFlag);

	List<String> findBacklogPolicyNo(String startDate, String endDate);

	List<AdroitVIR> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	List<String> getCarTypeList();

	List<AdroitVIR> getCarPolicyExtractionDetails(String startDate, String endDate, String carType);

	List<AdroitVIR> findPdfToBeDownloaded(Long thresholdCount);

	List<AdroitVIR> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	CarDailyStatusResponse getMarutiYearlyStatus(String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<CommonReportTakenStatus> getPendingPolicyDateListAdroit(String string);

	CommonReportTakenStatus save(CommonReportTakenStatus marutiPolicyTakenStatus);

	List<String> findExistingPolicyList(String policyDate);

	MarutiUploadReferenceResponse getUploadReference(String policyNo, String flag);

	List<String> findExistingBacklogPolicyList(String policyDate);

	List<CommonReportTakenStatus> getPendingBacklogPolicyDateListAdroit(String string, String string2);

}
