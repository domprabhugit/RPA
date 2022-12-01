/*
 * Robotic Process Automation
 * @originalAuthor Mohamed ismaiel.s
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.model.processors;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "AUTOINSPEKT_VIR")
public class AutoInspektVIR  implements Serializable   {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	private String registrationNumber;
	
	private String policyNo;
	
	private String proposalNumber;
	
	private String inspectionDate;
	
	private String isReportDownloaded;
	
	private String isReportUploaded;
	
	private String inwardCode;
	
	private String backLogFlag;
	
	private String virType;
	
	private String reportPdfPath;
	
	 @Temporal(TemporalType.TIMESTAMP)
	 private Date reportUploadedTime;
	 
	private byte[] reportRequest;
	
	private byte[] reportResponse;
	
	private String inwardFolderIndex;
	
	private String proposalFolderIndex;
	
	private String chassisno;
	
	private String engineno;
	
	private String virNumber;
	
	private String serviceProvidername;

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public String getPolicyNo() {
		return policyNo;
	}

	public void setPolicyNo(String policyNo) {
		this.policyNo = policyNo;
	}

	public String getProposalNumber() {
		return proposalNumber;
	}

	public void setProposalNumber(String proposalNumber) {
		this.proposalNumber = proposalNumber;
	}

	public String getInspectionDate() {
		return inspectionDate;
	}

	public void setInspectionDate(String inspectionDate) {
		this.inspectionDate = inspectionDate;
	}

	public String getIsReportDownloaded() {
		return isReportDownloaded;
	}

	public void setIsReportDownloaded(String isReportDownloaded) {
		this.isReportDownloaded = isReportDownloaded;
	}

	public String getIsReportUploaded() {
		return isReportUploaded;
	}

	public void setIsReportUploaded(String isReportUploaded) {
		this.isReportUploaded = isReportUploaded;
	}

	public String getInwardCode() {
		return inwardCode;
	}

	public void setInwardCode(String inwardCode) {
		this.inwardCode = inwardCode;
	}

	public String getBackLogFlag() {
		return backLogFlag;
	}

	public void setBackLogFlag(String backLogFlag) {
		this.backLogFlag = backLogFlag;
	}

	public String getVirType() {
		return virType;
	}

	public void setVirType(String virType) {
		this.virType = virType;
	}

	public String getReportPdfPath() {
		return reportPdfPath;
	}

	public void setReportPdfPath(String reportPdfPath) {
		this.reportPdfPath = reportPdfPath;
	}

	public Date getReportUploadedTime() {
		return reportUploadedTime;
	}

	public void setReportUploadedTime(Date reportUploadedTime) {
		this.reportUploadedTime = reportUploadedTime;
	}

	public byte[] getReportRequest() {
		return reportRequest;
	}

	public void setReportRequest(byte[] reportRequest) {
		this.reportRequest = reportRequest;
	}

	public byte[] getReportResponse() {
		return reportResponse;
	}

	public void setReportResponse(byte[] reportResponse) {
		this.reportResponse = reportResponse;
	}

	public String getInwardFolderIndex() {
		return inwardFolderIndex;
	}

	public void setInwardFolderIndex(String inwardFolderIndex) {
		this.inwardFolderIndex = inwardFolderIndex;
	}

	public String getProposalFolderIndex() {
		return proposalFolderIndex;
	}

	public void setProposalFolderIndex(String proposalFolderIndex) {
		this.proposalFolderIndex = proposalFolderIndex;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getChassisno() {
		return chassisno;
	}

	public void setChassisno(String chassisno) {
		this.chassisno = chassisno;
	}

	public String getEngineno() {
		return engineno;
	}

	public void setEngineno(String engineno) {
		this.engineno = engineno;
	}

	public String getVirNumber() {
		return virNumber;
	}

	public void setVirNumber(String virNumber) {
		this.virNumber = virNumber;
	}

	public String getServiceProvidername() {
		return serviceProvidername;
	}

	public void setServiceProvidername(String serviceProvidername) {
		this.serviceProvidername = serviceProvidername;
	}

	
	
}
