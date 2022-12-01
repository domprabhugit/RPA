/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import com.rpa.model.processors.MarutiPolicy;
import com.rpa.model.processors.MarutiPolicyTakenStatus;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;

public interface MarutiPolicyService {
	
    
    List<String> findYesterdayPolicyNo() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException;

   /* List<MarutiPolicy> save(List<MarutiPolicy> newMarutiPolicyList);*/

    List<MarutiPolicy> findPdfUnUploadedPolicies(Long thresholdCount) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException;

	MarutiPolicy findByPolicyNo(String policyNo) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException;
	
	/*MarutiPolicy save(MarutiPolicy marutiPolicy);*/
	
	/*String getLastId(String paramValue);*/

	CarDailyStatusResponse getMarutiDailyStatus(String policyDate) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<String> getMarutiPolicyDateList(String monthYear, String statusFlag) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<String> findBacklogPolicyNo(String startDate, String endDate) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException;

	List<MarutiPolicy> findPdfUnUploadedBackLogPolicies(String startDate, String endDate,Long thresholdCount) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<String> getCarTypeList() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException;

	List<MarutiPolicy> getCarPolicyExtractionDetails(String startDate, String endDate, String carType) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException;

	List<MarutiPolicy> findPdfToBeDownloaded(Long thresholdCount) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException;

	List<MarutiPolicy> findPdfToBeDownloadedForBackLogPolicies(String startDate, String endDate,Long thresholdCount) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	CarDailyStatusResponse getMarutiMonthlyStatus(String policyMonth, String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	CarDailyStatusResponse getMarutiYearlyStatus(String policyYear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<MarutiPolicyTakenStatus> getPendingPolicyDateList(String string) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	/*MarutiPolicyTakenStatus save(MarutiPolicyTakenStatus marutiPolicyTakenStatus);*/

	List<String> findExistingPolicyList(String policyDate) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException;

	MarutiUploadReferenceResponse getUploadReference(String policyNo, String flag) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<String> findExistingBacklogPolicyList(String policyDate) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException;

	List<MarutiPolicyTakenStatus> getPendingBacklogPolicyDateList(String string, String string2) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;

	List<MarutiPolicy> saveNewObject(List<MarutiPolicy> newMarutiPolicyList) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException;

	boolean updateNonByteMarutiData(MarutiPolicy marutiPolicyObj) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException;

	boolean updateWithByteMarutiData(MarutiPolicy marutiPolicyObj) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException;

	boolean update(MarutiPolicyTakenStatus marutiPolicyTakenStatus) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException;

	String getTotalPolicies(String flag, String policyDate) throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException;

}
