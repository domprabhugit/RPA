/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import com.rpa.model.processors.FordPolicy;
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;

public interface FordPolicyService {
	
    List<FordPolicy> findAll();
    
    List<String> findYesterdayPolicyNo();

    List<FordPolicy> save(List<FordPolicy> newMarutiPolicyList);

    List<FordPolicy> findPdfUnUploadedPolicies(Long thresholdCount);

	FordPolicy findByPolicyNo(String policyNo);
	
	FordPolicy save(FordPolicy FordPolicy);
	
	String getLastId(String paramValue);

	CarDailyStatusResponse getMarutiDailyStatus(String policyDate) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<String> getMarutiPolicyDateList(String monthYear, String statusFlag);

	List<String> findBacklogPolicyNo(String startDate, String endDate);

	List<FordPolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	List<String> getCarTypeList();

	List<FordPolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType);

	List<FordPolicy> findPdfToBeDownloaded(Long thresholdCount);

	List<FordPolicy> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	CarDailyStatusResponse getMarutiYearlyStatus(String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<CommonPolicyTakenStatus> getPendingPolicyDateListFord(String string);

	CommonPolicyTakenStatus save(CommonPolicyTakenStatus marutiPolicyTakenStatus);

	List<String> findExistingPolicyList(String policyDate);

	MarutiUploadReferenceResponse getUploadReference(String policyNo, String flag);

	List<String> findExistingBacklogPolicyList(String policyDate);

	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListFord(String string, String string2);

}
