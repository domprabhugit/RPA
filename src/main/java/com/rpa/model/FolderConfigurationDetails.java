/*
 * Robotic Process Automation
 * @originalAuthor S.Mohamed Ismaiel
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

@Entity
@Table(name = "folder_configuration_Details")
public class FolderConfigurationDetails implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private String processName;
	private String customerName;
	private String fileType;
	private String folderPath;
	private String status;
	private String isRestrictedFolder;
	private String username;
	private String password;
	
	@Id
	@SequenceGenerator(name = "folder_configuration_seq", sequenceName = "folder_configuration_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="folder_configuration_seq")
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

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIsRestrictedFolder() {
		return isRestrictedFolder;
	}

	public void setIsRestrictedFolder(String isRestrictedFolder) {
		this.isRestrictedFolder = isRestrictedFolder;
	}
	
	
}
