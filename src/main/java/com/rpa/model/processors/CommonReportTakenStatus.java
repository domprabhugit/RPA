/*
 * Robotic Process Automation
 * @originalAuthor Mohamed ismaiel.s
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.model.processors;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "COMMON_REPORT_TAKEN_STATUS")
public class CommonReportTakenStatus  implements Serializable   {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	private String inspectionDate;
	
	private String autoinspektStatus;
	
	private String adroitStatus;
	
	private String virappStatus;

	public String getInspectionDate() {
		return inspectionDate;
	}

	public void setInspectionDate(String inspectionDate) {
		this.inspectionDate = inspectionDate;
	}

	public String getAutoinspektStatus() {
		return autoinspektStatus;
	}

	public void setAutoinspektStatus(String autoinspektStatus) {
		this.autoinspektStatus = autoinspektStatus;
	}

	public String getAdroitStatus() {
		return adroitStatus;
	}

	public void setAdroitStatus(String adroitStatus) {
		this.adroitStatus = adroitStatus;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getVirappStatus() {
		return virappStatus;
	}

	public void setVirappStatus(String virappStatus) {
		this.virappStatus = virappStatus;
	}
	
	
	
	
}
