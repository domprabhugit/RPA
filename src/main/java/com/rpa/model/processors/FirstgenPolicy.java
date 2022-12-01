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
@Table(name = "firstgen_policy")
public class FirstgenPolicy  implements Serializable   {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	private String policyNo;
	
	private String policyDate;
	
	private String isPolicyUploaded;
	
	private String inwardCode;
	
	private String isInvoiceUploaded;
	
	private String isPolicyDownloaded;
	
	private String isInvoiceDownloaded;
	
	private String policyPdfPath;
	
	private String invoicePdfPath;
	
	 @Temporal(TemporalType.TIMESTAMP)
	 private Date policyPdfUploadedTime;
	 
	 @Temporal(TemporalType.TIMESTAMP)
	 private Date invoicePdfUploadedTime;
	 
	private byte[] policyRequest;
	
	private byte[] policyResponse;
	
	private byte[] invoiceRequest;
	
	private byte[] invoiceResponse;
	
	private String inwardFolderIndex;
	
	private String invoiceFolderIndex;
	
	private String backLogFlag;
	
	private String carType;
	
	private String isPdfMerged;
	
	private String fileMergedPath;

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

	public String getIsInvoiceUploaded() {
		return isInvoiceUploaded;
	}

	public void setIsInvoiceUploaded(String isInvoiceUploaded) {
		this.isInvoiceUploaded = isInvoiceUploaded;
	}

	public String getIsPolicyDownloaded() {
		return isPolicyDownloaded;
	}

	public void setIsPolicyDownloaded(String isPolicyDownloaded) {
		this.isPolicyDownloaded = isPolicyDownloaded;
	}

	public String getIsInvoiceDownloaded() {
		return isInvoiceDownloaded;
	}

	public void setIsInvoiceDownloaded(String isInvoiceDownloaded) {
		this.isInvoiceDownloaded = isInvoiceDownloaded;
	}

	public String getPolicyPdfPath() {
		return policyPdfPath;
	}

	public void setPolicyPdfPath(String policyPdfPath) {
		this.policyPdfPath = policyPdfPath;
	}

	public String getInvoicePdfPath() {
		return invoicePdfPath;
	}

	public void setInvoicePdfPath(String invoicePdfPath) {
		this.invoicePdfPath = invoicePdfPath;
	}

	public Date getPolicyPdfUploadedTime() {
		return policyPdfUploadedTime;
	}

	public void setPolicyPdfUploadedTime(Date policyPdfUploadedTime) {
		this.policyPdfUploadedTime = policyPdfUploadedTime;
	}

	public Date getInvoicePdfUploadedTime() {
		return invoicePdfUploadedTime;
	}

	public void setInvoicePdfUploadedTime(Date invoicePdfUploadedTime) {
		this.invoicePdfUploadedTime = invoicePdfUploadedTime;
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

	public byte[] getInvoiceRequest() {
		return invoiceRequest;
	}

	public void setInvoiceRequest(byte[] invoiceRequest) {
		this.invoiceRequest = invoiceRequest;
	}

	public byte[] getInvoiceResponse() {
		return invoiceResponse;
	}

	public void setInvoiceResponse(byte[] invoiceResponse) {
		this.invoiceResponse = invoiceResponse;
	}

	public String getInwardFolderIndex() {
		return inwardFolderIndex;
	}

	public void setInwardFolderIndex(String inwardFolderIndex) {
		this.inwardFolderIndex = inwardFolderIndex;
	}

	public String getInvoiceFolderIndex() {
		return invoiceFolderIndex;
	}

	public void setInvoiceFolderIndex(String invoiceFolderIndex) {
		this.invoiceFolderIndex = invoiceFolderIndex;
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

	public String getIsPdfMerged() {
		return isPdfMerged;
	}

	public void setIsPdfMerged(String isPdfMerged) {
		this.isPdfMerged = isPdfMerged;
	}

	public String getFileMergedPath() {
		return fileMergedPath;
	}

	public void setFileMergedPath(String fileMergedPath) {
		this.fileMergedPath = fileMergedPath;
	}
	
	
	
	
}
