/*
 * Robotic Process Automation
 * @originalAuthor S.Mohamed Ismaiel
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "folder_configuration_master")
public class FolderConfigurationMaster implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private String processName;
	private String customerName;
	private String fileType;

	@Id
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

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

}
