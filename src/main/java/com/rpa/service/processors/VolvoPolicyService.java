/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import com.rpa.model.processors.VolvoPolicy;
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;

public interface VolvoPolicyService {
	
    List<VolvoPolicy> findAll();
    
    List<String> findYesterdayPolicyNo();

    List<VolvoPolicy> save(List<VolvoPolicy> newMarutiPolicyList);

    List<VolvoPolicy> findPdfUnUploadedPolicies(Long thresholdCount);

	VolvoPolicy findByPolicyNo(String policyNo);
	
	VolvoPolicy save(VolvoPolicy VolvoPolicy);
	
	String getLastId(String paramValue);

	CarDailyStatusResponse getMarutiDailyStatus(String policyDate) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<String> getMarutiPolicyDateList(String monthYear, String statusFlag);

	List<String> findBacklogPolicyNo(String startDate, String endDate);

	List<VolvoPolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	List<String> getCarTypeList();

	List<VolvoPolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType);

	List<VolvoPolicy> findPdfToBeDownloaded(Long thresholdCount);

	List<VolvoPolicy> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	CarDailyStatusResponse getMarutiYearlyStatus(String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<CommonPolicyTakenStatus> getPendingPolicyDateListVolvo(String string);

	CommonPolicyTakenStatus save(CommonPolicyTakenStatus marutiPolicyTakenStatus);

	List<String> findExistingPolicyList(String policyDate);

	MarutiUploadReferenceResponse getUploadReference(String policyNo, String flag);

	List<String> findExistingBacklogPolicyList(String policyDate);

	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListVolvo(String string, String string2);

}
