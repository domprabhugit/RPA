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
@Table(name="POLICY_PDF_MAIL_RETRIGGER")
public class PolicyPdfMailRetrigger  implements  Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Id
	@SequenceGenerator(name = "policy_pdf_mail_retrigger_seq", sequenceName = "policy_pdf_mail_retrigger_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="policy_pdf_mail_retrigger_seq")
	private long id;
	
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Temporal(TemporalType.TIMESTAMP)
	private  Date  createdTime;
	
	@Temporal(TemporalType.TIMESTAMP)
	private  Date  modifiedTime;
	
	private String fileName;
	
	private String isExcelCreated;
	
	private String isExcelUploaded;
	
	private String quoteCount;
	
	private String remarks;
	
	private long transactionRefNo;
	
	private String filePath;


	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Date getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(Date modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getIsExcelCreated() {
		return isExcelCreated;
	}

	public void setIsExcelCreated(String isExcelCreated) {
		this.isExcelCreated = isExcelCreated;
	}

	public String getIsExcelUploaded() {
		return isExcelUploaded;
	}

	public void setIsExcelUploaded(String isExcelUploaded) {
		this.isExcelUploaded = isExcelUploaded;
	}

	public String getQuoteCount() {
		return quoteCount;
	}

	public void setQuoteCount(String quoteCount) {
		this.quoteCount = quoteCount;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public long getTransactionRefNo() {
		return transactionRefNo;
	}

	public void setTransactionRefNo(long transactionRefNo) {
		this.transactionRefNo = transactionRefNo;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	
	
}
