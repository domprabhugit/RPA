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
@Table(name = "piaggio_policy")
public class PiaggioPolicy  implements Serializable   {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	private String policyNo;
	
	private String policyDate;
	
	private String proposalNumber;
	
	private String isPolicyUploaded;
	
	private String inwardCode;
	
	private String isProposalUploaded;
	
	private String backLogFlag;
	
	private String carType;
	
	private String isPolicyDownloaded;
	
	private String isProposalDownloaded;
	
	private String policyPdfPath;
	
	private String proposalPdfPath;
	
	 @Temporal(TemporalType.TIMESTAMP)
	 private Date policyPdfUploadedTime;
	 
	 @Temporal(TemporalType.TIMESTAMP)
	 private Date proposalPdfUploadedTime;
	 
	private byte[] policyRequest;
	
	private byte[] policyResponse;
	
	private byte[] proposalRequest;
	
	private byte[] proposalResponse;
	
	private String inwardFolderIndex;
	
	private String proposalFolderIndex;
	
	public String getPolicyNo() {
		return policyNo;
	}

	public void setPolicyNo(String policyNo) {
		this.policyNo = policyNo;
	}

	public String getPolicyDate() {
		return policyDate;
	}

	public void setPolicyDate(String policyDate) {
		this.policyDate = policyDate;
	}
	
	public String getProposalNumber() {
		return proposalNumber;
	}

	public void setProposalNumber(String proposalNumber) {
		this.proposalNumber = proposalNumber;
	}

	public String getIsPolicyUploaded() {
		return isPolicyUploaded;
	}

	public void setIsPolicyUploaded(String isPolicyUploaded) {
		this.isPolicyUploaded = isPolicyUploaded;
	}

	public String getInwardCode() {
		return inwardCode;
	}

	public void setInwardCode(String inwardCode) {
		this.inwardCode = inwardCode;
	}

	public String getIsProposalUploaded() {
		return isProposalUploaded;
	}

	public void setIsProposalUploaded(String isProposalUploaded) {
		this.isProposalUploaded = isProposalUploaded;
	}

	public String getBackLogFlag() {
		return backLogFlag;
	}

	public void setBackLogFlag(String backLogFlag) {
		this.backLogFlag = backLogFlag;
	}

	public String getCarType() {
		return carType;
	}

	public void setCarType(String carType) {
		this.carType = carType;
	}

	public String getIsPolicyDownloaded() {
		return isPolicyDownloaded;
	}

	public void setIsPolicyDownloaded(String isPolicyDownloaded) {
		this.isPolicyDownloaded = isPolicyDownloaded;
	}

	public String getIsProposalDownloaded() {
		return isProposalDownloaded;
	}

	public void setIsProposalDownloaded(String isProposalDownloaded) {
		this.isProposalDownloaded = isProposalDownloaded;
	}

	public String getPolicyPdfPath() {
		return policyPdfPath;
	}

	public void setPolicyPdfPath(String policyPdfPath) {
		this.policyPdfPath = policyPdfPath;
	}

	public String getProposalPdfPath() {
		return proposalPdfPath;
	}

	public void setProposalPdfPath(String proposalPdfPath) {
		this.proposalPdfPath = proposalPdfPath;
	}

	public Date getPolicyPdfUploadedTime() {
		return policyPdfUploadedTime;
	}

	public void setPolicyPdfUploadedTime(Date policyPdfUploadedTime) {
		this.policyPdfUploadedTime = policyPdfUploadedTime;
	}

	public Date getProposalPdfUploadedTime() {
		return proposalPdfUploadedTime;
	}

	public void setProposalPdfUploadedTime(Date proposalPdfUploadedTime) {
		this.proposalPdfUploadedTime = proposalPdfUploadedTime;
	}

	public byte[] getPolicyRequest() {
		return policyRequest;
	}

	public void setPolicyRequest(byte[] policyRequest) {
		this.policyRequest = policyRequest;
	}

	public byte[] getPolicyResponse() {
		return policyResponse;
	}

	public void setPolicyResponse(byte[] policyResponse) {
		this.policyResponse = policyResponse;
	}

	public byte[] getProposalRequest() {
		return proposalRequest;
	}

	public void setProposalRequest(byte[] proposalRequest) {
		this.proposalRequest = proposalRequest;
	}

	public byte[] getProposalResponse() {
		return proposalResponse;
	}

	public void setProposalResponse(byte[] proposalResponse) {
		this.proposalResponse = proposalResponse;
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

	
	
}
