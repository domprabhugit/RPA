/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import com.rpa.model.processors.FirstgenPolicy;
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;

public interface FirstGenDownloadPolicyService {
	
    List<FirstgenPolicy> findAll();
    
    List<String> findYesterdayPolicyNo();

    List<FirstgenPolicy> save(List<FirstgenPolicy> newMarutiPolicyList);

    List<FirstgenPolicy> findPdfUnUploadedPolicies(Long thresholdCount);

	FirstgenPolicy findByPolicyNo(String policyNo);
	
	FirstgenPolicy save(FirstgenPolicy FirstgenPolicy);
	
	String getLastId(String paramValue);

	CarDailyStatusResponse getMarutiDailyStatus(String policyDate) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<String> getMarutiPolicyDateList(String monthYear, String statusFlag);

	List<String> findBacklogPolicyNo(String startDate, String endDate);

	List<FirstgenPolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	List<String> getCarTypeList();

	List<FirstgenPolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType);

	List<FirstgenPolicy> findPdfToBeDownloaded(Long thresholdCount);

	List<FirstgenPolicy> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	CarDailyStatusResponse getMarutiYearlyStatus(String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<CommonPolicyTakenStatus> getPendingPolicyDateListFirstGen(String string);

	CommonPolicyTakenStatus save(CommonPolicyTakenStatus marutiPolicyTakenStatus);

	List<String> findExistingPolicyList(String policyDate);

	MarutiUploadReferenceResponse getUploadReference(String policyNo, String flag);

	List<String> findExistingBacklogPolicyList(String policyDate);

	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListFirstGen(String string, String string2);

	List<FirstgenPolicy> findErrorPdfUploadedPolicies(Long thresholdFrequencyLevel);

}
