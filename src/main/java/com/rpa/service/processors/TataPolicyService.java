/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import com.rpa.model.processors.TataPolicy;
import com.rpa.model.processors.CommonPolicyTakenStatus;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;

public interface TataPolicyService {
	
    List<TataPolicy> findAll();
    
    List<String> findYesterdayPolicyNo();

    List<TataPolicy> save(List<TataPolicy> newMarutiPolicyList);

    List<TataPolicy> findPdfUnUploadedPolicies(Long thresholdCount);

	TataPolicy findByPolicyNo(String policyNo);
	
	TataPolicy save(TataPolicy TataPolicy);
	
	String getLastId(String paramValue);

	CarDailyStatusResponse getMarutiDailyStatus(String policyDate) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<String> getMarutiPolicyDateList(String monthYear, String statusFlag);

	List<String> findBacklogPolicyNo(String startDate, String endDate);

	List<TataPolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount);

	List<String> getCarTypeList();

	List<TataPolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType);

	List<TataPolicy> findPdfToBeDownloadedPV(Long thresholdCount);
	
	List<TataPolicy> findPdfToBeDownloadedCV(Long thresholdCount);

	List<TataPolicy> findPdfToBeDownloadedForBackLogPoliciesPV(String startDate, String endDate,Long thresholdCount);
	
	List<TataPolicy> findPdfToBeDownloadedForBackLogPoliciesCV(String startDate, String endDate,Long thresholdCount);

	CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	CarDailyStatusResponse getMarutiYearlyStatus(String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<CommonPolicyTakenStatus> getPendingPolicyDateListTataPV(String string);
	
	List<CommonPolicyTakenStatus> getPendingPolicyDateListTataCV(String string);

	CommonPolicyTakenStatus save(CommonPolicyTakenStatus marutiPolicyTakenStatus);

	List<String> findExistingPolicyListPV(String policyDate);
	
	List<String> findExistingPolicyListCV(String policyDate);

	MarutiUploadReferenceResponse getUploadReference(String policyNo, String flag);

	List<String> findExistingBacklogPolicyListPV(String policyDate);
	
	List<String> findExistingBacklogPolicyListCV(String policyDate);

	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListTataPV(String string, String string2);
	
	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListTataCV(String string, String string2);

}
