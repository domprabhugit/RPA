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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "gl_batch_process")
@NamedQueries({
	@NamedQuery(name = GLBatchProcessor.FIND_BY_TICKET,
	query = "SELECT p FROM GLBatchProcessor p WHERE p.ticketId in (:ticketId) "),
	@NamedQuery(name = GLBatchProcessor.FIND_BY_RUN_NO,
	query = "SELECT p FROM GLBatchProcessor p WHERE p.runNo = :runNo"),
	@NamedQuery(name = GLBatchProcessor.FIND_BY_DATE,
	query = "SELECT p FROM GLBatchProcessor p WHERE p.dateFrom = :dateFrom")
})
public class GLBatchProcessor  implements Serializable   {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String FIND_BY_TICKET = "GLBatchProcessor.findByTicketId";
	public static final String FIND_BY_RUN_NO = "GLBatchProcessor.findByRunNo";
	public static final String FIND_BY_DATE = "GLBatchProcessor.findByDateFrom";
	
	@Id
	private String runNo;
	
	public String getRunNo() {
		return runNo;
	}

	public void setRunNo(String runNo) {
		this.runNo = runNo;
	}

	private String moduleCode;
	
	@Temporal(TemporalType.DATE)
	private Date dateFrom;
	
	@Temporal(TemporalType.DATE)
	private Date dateTo;
	
	private String processedFlag;
	
	private String ticketId;
	
	private String description;
	
	public String getModuleCode() {
		return moduleCode;
	}

	public void setModuleCode(String moduleCode) {
		this.moduleCode = moduleCode;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public String getProcessedFlag() {
		return processedFlag;
	}

	public void setProcessedFlag(String processedFlag) {
		this.processedFlag = processedFlag;
	}

	public String getTicketId() {
		return ticketId;
	}

	public void setTicketId(String ticketId) {
		this.ticketId = ticketId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
