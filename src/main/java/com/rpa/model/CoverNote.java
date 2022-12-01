package com.rpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "rs_covernote_log")
public class CoverNote {

	private String intermediaryNo;

	private String intermediaryPhNo;

	private String rsMode;

	private String action;

	private String referenceNo;

	private String token;

	private String customerNo;

	private String capturedDate;

	private String capturedTime;

	private String smsText;

	private String custName;

	private String smsDate;

	private String lastupdate;

	@Id
	public String getIntermediaryNo() {
		return intermediaryNo;
	}

	public void setIntermediaryNo(String intermediaryNo) {
		this.intermediaryNo = intermediaryNo;
	}

	public String getIntermediaryPhNo() {
		return intermediaryPhNo;
	}

	public void setIntermediaryPhNo(String intermediaryPhNo) {
		this.intermediaryPhNo = intermediaryPhNo;
	}

	public String getRsMode() {
		return rsMode;
	}

	public void setRsMode(String rsMode) {
		this.rsMode = rsMode;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getCustomerNo() {
		return customerNo;
	}

	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}

	public String getCapturedDate() {
		return capturedDate;
	}

	public void setCapturedDate(String capturedDate) {
		this.capturedDate = capturedDate;
	}

	public String getCapturedTime() {
		return capturedTime;
	}

	public void setCapturedTime(String capturedTime) {
		this.capturedTime = capturedTime;
	}

	public String getSmsText() {
		return smsText;
	}

	public void setSmsText(String smsText) {
		this.smsText = smsText;
	}

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public String getSmsDate() {
		return smsDate;
	}

	public void setSmsDate(String smsDate) {
		this.smsDate = smsDate;
	}

	public String getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(String lastupdate) {
		this.lastupdate = lastupdate;
	}
}
