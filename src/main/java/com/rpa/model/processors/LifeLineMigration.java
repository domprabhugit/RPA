/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.model.processors;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.rpa.model.BaseEntity;

@Entity
@Table(name = "lifeline_migration")
public class LifeLineMigration extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Temporal(TemporalType.DATE)
	private Date countDate;

	private int approvedCount;

	private int receiptCount;

	private int mobileInwardCount;

	private int pipelineCount;

	private int renewalPolicyCount;

	private int motorClaims;

	private int healthclaims;

	private int cancelledCount;

	private int lifelineInward;

	private int healthInwardCount;

	private int xgenHealthclaims;

	private int licenseAgent;

	private String appType;

	private String isCompared;

	private Long transactionInfoId;

	public Long getTransactionInfoId() {
		return transactionInfoId;
	}

	public void setTransactionInfoId(Long transactionInfoId) {
		this.transactionInfoId = transactionInfoId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCountDate() {
		return countDate;
	}

	public void setCountDate(Date countDate) {
		this.countDate = countDate;
	}

	public int getApprovedCount() {
		return approvedCount;
	}

	public void setApprovedCount(int approvedCount) {
		this.approvedCount = approvedCount;
	}

	public int getReceiptCount() {
		return receiptCount;
	}

	public void setReceiptCount(int receiptCount) {
		this.receiptCount = receiptCount;
	}

	public int getMobileInwardCount() {
		return mobileInwardCount;
	}

	public void setMobileInwardCount(int mobileInwardCount) {
		this.mobileInwardCount = mobileInwardCount;
	}

	public int getPipelineCount() {
		return pipelineCount;
	}

	public void setPipelineCount(int pipelineCount) {
		this.pipelineCount = pipelineCount;
	}

	public int getRenewalPolicyCount() {
		return renewalPolicyCount;
	}

	public void setRenewalPolicyCount(int renewalPolicyCount) {
		this.renewalPolicyCount = renewalPolicyCount;
	}

	public int getMotorClaims() {
		return motorClaims;
	}

	public void setMotorClaims(int motorClaims) {
		this.motorClaims = motorClaims;
	}

	public int getHealthclaims() {
		return healthclaims;
	}

	public void setHealthclaims(int healthclaims) {
		this.healthclaims = healthclaims;
	}

	public int getCancelledCount() {
		return cancelledCount;
	}

	public void setCancelledCount(int cancelledCount) {
		this.cancelledCount = cancelledCount;
	}

	public int getLifelineInward() {
		return lifelineInward;
	}

	public void setLifelineInward(int lifelineInward) {
		this.lifelineInward = lifelineInward;
	}

	public int getHealthInwardCount() {
		return healthInwardCount;
	}

	public void setHealthInwardCount(int healthInwardCount) {
		this.healthInwardCount = healthInwardCount;
	}

	public int getXgenHealthclaims() {
		return xgenHealthclaims;
	}

	public void setXgenHealthclaims(int xgenHealthclaims) {
		this.xgenHealthclaims = xgenHealthclaims;
	}

	public int getLicenseAgent() {
		return licenseAgent;
	}

	public void setLicenseAgent(int licenseAgent) {
		this.licenseAgent = licenseAgent;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public String getIsCompared() {
		return isCompared;
	}

	public void setIsCompared(String isCompared) {
		this.isCompared = isCompared;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
