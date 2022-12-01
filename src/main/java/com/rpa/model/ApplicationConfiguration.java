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
@Table(name = "application_configuration")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ApplicationConfiguration implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Long id;
    private Long processId;
    private Long appId;
    private String url;
    private String username;
    private String password;
    private String status;
    private String isPasswordExpired;

    @Id
	@SequenceGenerator(name = "application_configuration_seq", sequenceName = "application_configuration_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="application_configuration_seq")
    public Long getId() {
        return id;
    }
    
	private String processName;
	
	private String appName;
	
	@Transient
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

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

	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIsPasswordExpired() {
		return isPasswordExpired;
	}

	public void setIsPasswordExpired(String isPasswordExpired) {
		this.isPasswordExpired = isPasswordExpired;
	}
	
	
	
    
}
