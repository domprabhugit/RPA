/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.model;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "transaction_info")
public class TransactionInfo extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "transaction_info_seq", sequenceName = "transaction_info_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="transaction_info_seq")
	private Long id;

	private String processName;

	private String transactionStatus;

	@Temporal(TemporalType.TIMESTAMP)
	private Date transactionStartDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date transactionEndDate;

	@Transient
	private String startDate;

	@Transient
	private String endDate;

	private String externalTransactionRefNo;

	private String totalRecords;

	private String totalSuccessRecords;

	private String totalErrorRecords;
	
	private String inputFilePath_1;

	private String uploadFileDownload;

	private String errorFileDownload;

	private String successFileDownload;
	
	private String reprocessedFlag;
	
	private String oldRunNo;
	
	private String runNo;
	
	 @Temporal(TemporalType.DATE)
	private Date migrationDate;
	
	private String migrationStatus;
	
	private String processPhase;

	private String processStatus;
	
	private String processSuccessReason;
	
	private String processFailureReason;
	
	private String migrationMailId;
	
	private String status;
	
	private String ilongueFileName;
	
	private String firstgenFileName;
	
	private String logFileDownload;
	
	private String totalUploadRecords;

	private String totalSuccessUploads;

	private String totalErrorUploads;
	
	public String getTotalUploadRecords() {
		return totalUploadRecords;
	}

	public void setTotalUploadRecords(String totalUploadRecords) {
		this.totalUploadRecords = totalUploadRecords;
	}

	public String getTotalSuccessUploads() {
		return totalSuccessUploads;
	}

	public void setTotalSuccessUploads(String totalSuccessUploads) {
		this.totalSuccessUploads = totalSuccessUploads;
	}

	public String getTotalErrorUploads() {
		return totalErrorUploads;
	}

	public void setTotalErrorUploads(String totalErrorUploads) {
		this.totalErrorUploads = totalErrorUploads;
	}

	public String getLogFileDownload() {
		return logFileDownload;
	}

	public void setLogFileDownload(String logFileDownload) {
		this.logFileDownload = logFileDownload;
	}

	public String getIlongueFileName() {
		return ilongueFileName;
	}

	public void setIlongueFileName(String ilongueFileName) {
		this.ilongueFileName = ilongueFileName;
	}

	public String getFirstgenFileName() {
		return firstgenFileName;
	}

	public void setFirstgenFileName(String firstgenFileName) {
		this.firstgenFileName = firstgenFileName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMigrationMailId() {
		return migrationMailId;
	}

	public void setMigrationMailId(String migrationMailId) {
		this.migrationMailId = migrationMailId;
	}

	public String getMigrationStatus() {
		return migrationStatus;
	}

	public void setMigrationStatus(String migrationStatus) {
		this.migrationStatus = migrationStatus;
	}


	public Date getMigrationDate() {
		return migrationDate;
	}

	public void setMigrationDate(Date migrationDate) {
		this.migrationDate = migrationDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public Date getTransactionStartDate() {
		return transactionStartDate;
	}

	public void setTransactionStartDate(Date transactionStartDate) {
		this.transactionStartDate = transactionStartDate;
	}

	public Date getTransactionEndDate() {
		return transactionEndDate;
	}

	public void setTransactionEndDate(Date transactionEndDate) {
		this.transactionEndDate = transactionEndDate;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getExternalTransactionRefNo() {
		return externalTransactionRefNo;
	}

	public void setExternalTransactionRefNo(String externalTransactionRefNo) {
		this.externalTransactionRefNo = externalTransactionRefNo;
	}

	public String getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(String totalRecords) {
		this.totalRecords = totalRecords;
	}

	public String getTotalSuccessRecords() {
		return totalSuccessRecords;
	}

	public void setTotalSuccessRecords(String totalSuccessRecords) {
		this.totalSuccessRecords = totalSuccessRecords;
	}

	public String getTotalErrorRecords() {
		return totalErrorRecords;
	}

	public void setTotalErrorRecords(String totalErrorRecords) {
		this.totalErrorRecords = totalErrorRecords;
	}

	public String getInputFilePath_1() {
		return inputFilePath_1;
	}

	public void setInputFilePath_1(String inputFilePath_1) {
		this.inputFilePath_1 = inputFilePath_1;
	}

	public String getUploadFileDownload() {
		return uploadFileDownload;
	}

	public void setUploadFileDownload(String uploadFileDownload) {
		this.uploadFileDownload = uploadFileDownload;
	}

	public String getErrorFileDownload() {
		return errorFileDownload;
	}

	public void setErrorFileDownload(String errorFileDownload) {
		this.errorFileDownload = errorFileDownload;
	}

	public String getSuccessFileDownload() {
		return successFileDownload;
	}

	public void setSuccessFileDownload(String successFileDownload) {
		this.successFileDownload = successFileDownload;
	}

	public String getReprocessedFlag() {
		return reprocessedFlag;
	}

	public void setReprocessedFlag(String reprocessedFlag) {
		this.reprocessedFlag = reprocessedFlag;
	}

	public String getOldRunNo() {
		return oldRunNo;
	}

	public void setOldRunNo(String oldRunNo) {
		this.oldRunNo = oldRunNo;
	}

	public String getRunNo() {
		return runNo;
	}

	public void setRunNo(String runNo) {
		this.runNo = runNo;
	}

	public String getProcessPhase() {
		return processPhase;
	}

	public void setProcessPhase(String processPhase) {
		this.processPhase = processPhase;
	}

	public String getProcessStatus() {
		return processStatus;
	}

	public void setProcessStatus(String processStatus) {
		this.processStatus = processStatus;
	}

	public String getProcessSuccessReason() {
		return processSuccessReason;
	}

	public void setProcessSuccessReason(String processSuccessReason) {
		this.processSuccessReason = processSuccessReason;
	}

	public String getProcessFailureReason() {
		return processFailureReason;
	}

	public void setProcessFailureReason(String processFailureReason) {
		this.processFailureReason = processFailureReason;
	}
}
