/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import com.rpa.model.processors.HondaPolicy;
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;

public interface HondaPolicyService {
	
    List<HondaPolicy> findAll();
    
    List<String> findYesterdayPolicyNo();

    List<HondaPolicy> save(List<HondaPolicy> newMarutiPolicyList);

    List<HondaPolicy> findPdfUnUploadedPolicies(Long thresholdCount);

	HondaPolicy findByPolicyNo(String policyNo);
	
	HondaPolicy save(HondaPolicy HondaPolicy);
	
	String getLastId(String paramValue);

	CarDailyStatusResponse getMarutiDailyStatus(String policyDate) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<String> getMarutiPolicyDateList(String monthYear, String statusFlag);

	List<String> findBacklogPolicyNo(String startDate, String endDate);

	List<HondaPolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	List<String> getCarTypeList();

	List<HondaPolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType);

	List<HondaPolicy> findPdfToBeDownloaded(Long thresholdCount);

	List<HondaPolicy> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	CarDailyStatusResponse getMarutiYearlyStatus(String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<CommonPolicyTakenStatus> getPendingPolicyDateListHonda(String string);

	CommonPolicyTakenStatus save(CommonPolicyTakenStatus marutiPolicyTakenStatus);

	List<String> findExistingPolicyList(String policyDate);

	MarutiUploadReferenceResponse getUploadReference(String policyNo, String flag);

	List<String> findExistingBacklogPolicyList(String policyDate);

	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListHonda(String string, String string2);

}
