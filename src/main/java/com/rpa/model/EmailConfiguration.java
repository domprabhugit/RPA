/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "email_configuration")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EmailConfiguration implements Serializable {

	private static final long serialVersionUID = -3611603915087508235L;
	
	private Long id;
	private Long processId;
	private String toEmailIds;
	private String ccEmailIds;
	private String status;

	@Id
	@SequenceGenerator(name = "email_configuration_seq", sequenceName = "email_configuration_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="email_configuration_seq")
	public Long getId() {
		return id;
	}

	private String processName;

	@Transient
	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public Long getProcessId() {
		return processId;
	}

	public void setProcessId(Long processId) {
		this.processId = processId;
	}

	public String getToEmailIds() {
		return toEmailIds;
	}

	public void setToEmailIds(String toEmailIds) {
		this.toEmailIds = toEmailIds;
	}

	public String getCcEmailIds() {
		return ccEmailIds;
	}

	public void setCcEmailIds(String ccEmailIds) {
		this.ccEmailIds = ccEmailIds;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "EmailProcessor [id=" + id + ", processId=" + processId + ", toEmailIds=" + toEmailIds + ", ccEmailIds="
				+ ccEmailIds + ", status=" + status + ", processName=" + processName + "]";
	}
}
