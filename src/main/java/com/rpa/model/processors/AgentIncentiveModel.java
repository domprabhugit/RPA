package com.rpa.model.processors;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="AGENT_INCENTIVE")
public class AgentIncentiveModel  implements  Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Id
	@SequenceGenerator(name = "agent_incentive_seq", sequenceName = "agent_incentive_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="agent_incentive_seq")
	private long id;
	
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	
	private String monthYear;
	
	private String isXgenFileAvailable;
	
	private String isDtwocFileAvailable;
	
	private String isCrmFileAvailable;

	private String isDataUploadedInDb;
	
	private String isIncentiveCalculated;
	
	private Long transactionRefNo;
	
	private String xGenFilePath;
	
	private String d2cFilePath;
	
	private String crmFilePath;
	
	private String errorValidationList;
	
	private String isValidationSucceeded;

	public String getMonthYear() {
		return monthYear;
	}

	public void setMonthYear(String monthYear) {
		this.monthYear = monthYear;
	}

	public String getIsXgenFileAvailable() {
		return isXgenFileAvailable;
	}

	public void setIsXgenFileAvailable(String isXgenFileAvailable) {
		this.isXgenFileAvailable = isXgenFileAvailable;
	}

	public String getIsCrmFileAvailable() {
		return isCrmFileAvailable;
	}

	public void setIsCrmFileAvailable(String isCrmFileAvailable) {
		this.isCrmFileAvailable = isCrmFileAvailable;
	}

	public Long getTransactionRefNo() {
		return transactionRefNo;
	}

	public void setTransactionRefNo(Long transactionRefNo) {
		this.transactionRefNo = transactionRefNo;
	}

	public String getxGenFilePath() {
		return xGenFilePath;
	}

	public void setxGenFilePath(String xGenFilePath) {
		this.xGenFilePath = xGenFilePath;
	}

	public String getD2cFilePath() {
		return d2cFilePath;
	}

	public void setD2cFilePath(String d2cFilePath) {
		this.d2cFilePath = d2cFilePath;
	}

	public String getCrmFilePath() {
		return crmFilePath;
	}

	public void setCrmFilePath(String crmFilePath) {
		this.crmFilePath = crmFilePath;
	}

	public String getErrorValidationList() {
		return errorValidationList;
	}

	public void setErrorValidationList(String errorValidationList) {
		this.errorValidationList = errorValidationList;
	}

	public String getIsValidationSucceeded() {
		return isValidationSucceeded;
	}

	public void setIsValidationSucceeded(String isValidationSucceeded) {
		this.isValidationSucceeded = isValidationSucceeded;
	}

	public String getIsIncentiveCalculated() {
		return isIncentiveCalculated;
	}

	public void setIsIncentiveCalculated(String isIncentiveCalculated) {
		this.isIncentiveCalculated = isIncentiveCalculated;
	}

	public String getIsDataUploadedInDb() {
		return isDataUploadedInDb;
	}

	public void setIsDataUploadedInDb(String isDataUploadedInDb) {
		this.isDataUploadedInDb = isDataUploadedInDb;
	}

	public String getIsDtwocFileAvailable() {
		return isDtwocFileAvailable;
	}

	public void setIsDtwocFileAvailable(String isDtwocFileAvailable) {
		this.isDtwocFileAvailable = isDtwocFileAvailable;
	}

	
}
