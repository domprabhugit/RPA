package com.rpa.model.processors;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="Grid_Automation")
public class GridAutomationModel  implements  Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Id
	@SequenceGenerator(name = "grid_automation_seq", sequenceName = "grid_automation_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="grid_automation_seq")
	private long id;
	
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Temporal(TemporalType.DATE)
	private  Date  startDate;
	
	private String fileName;
	
	@Temporal(TemporalType.DATE)
	private  Date  endDate;
	
	
	private String fileStatus;
	private String fileType;
	
	private String FilePath;
	
	private String totalSheetCount;
	
	private String successNo;
	
	private String validateNo;

	private String isValidated;
	
	private String isProcessed;
	
	private Long transactionInfoId;
	
	private String errorSheetList;
	
	private String validationSheetList;
	
	private String totalRecordsCount;
	
	private String gridInsertId;
	
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileStatus() {
		return fileStatus;
	}

	public void setFileStatus(String fileStatus) {
		this.fileStatus = fileStatus;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	
	

	public String getFilePath() {
		return FilePath;
	}

	public void setFilePath(String filePath) {
		FilePath = filePath;
	}

	public String getTotalSheetCount() {
		return totalSheetCount;
	}

	public void setTotalSheetCount(String totalSheetCount) {
		this.totalSheetCount = totalSheetCount;
	}

	public String getSuccessNo() {
		return successNo;
	}

	public void setSuccessNo(String successNo) {
		this.successNo = successNo;
	}

	public String getValidateNo() {
		return validateNo;
	}

	public void setValidateNo(String validateNo) {
		this.validateNo = validateNo;
	}

	public String getIsValidated() {
		return isValidated;
	}

	public void setIsValidated(String isValidated) {
		this.isValidated = isValidated;
	}

	public String getIsProcessed() {
		return isProcessed;
	}

	public void setIsProcessed(String isProcessed) {
		this.isProcessed = isProcessed;
	}

	public Long getTransactionInfoId() {
		return transactionInfoId;
	}

	public void setTransactionInfoId(Long transactionInfoId) {
		this.transactionInfoId = transactionInfoId;
	}


	public String getErrorSheetList() {
		return errorSheetList;
	}

	public void setErrorSheetList(String errorSheetList) {
		this.errorSheetList = errorSheetList;
	}

	public String getValidationSheetList() {
		return validationSheetList;
	}

	public void setValidationSheetList(String validationSheetList) {
		this.validationSheetList = validationSheetList;
	}

	public String getTotalRecordsCount() {
		return totalRecordsCount;
	}

	public void setTotalRecordsCount(String totalRecordsCount) {
		this.totalRecordsCount = totalRecordsCount;
	}

	public String getGridInsertId() {
		return gridInsertId;
	}

	public void setGridInsertId(String gridInsertId) {
		this.gridInsertId = gridInsertId;
	}

	
	
}
