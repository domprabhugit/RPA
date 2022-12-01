package com.rpa.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "rpa_user")
public class User extends Auditable{
	private Long id;
	private String username;
	private String password;
	private String passwordConfirm;
	private String emailId;
	private String phoneNo;
	private String active;
	private String firstTimePassword;
	
	@Transient
	private String roleName;
	
	@Transient
	private String processName;

	//@JsonIgnore
	private Set<Role> roles;
	
	@JsonIgnore
	private Set<BusinessProcess> businessProcesses;

	@Id
	@SequenceGenerator(name = "rpa_user_seq", sequenceName = "rpa_user_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="rpa_user_seq")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}
	
	@Transient
	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	@ManyToMany
	@JoinTable(name = "rpa_user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	@ManyToMany
	@JoinTable(name = "rpa_user_process", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "process_id"))
		public Set<BusinessProcess> getBusinessProcesses() {
		return businessProcesses;
	}

	public void setBusinessProcesses(Set<BusinessProcess> businessProcesses) {
		this.businessProcesses = businessProcesses;
	}
	
	public String getFirstTimePassword() {
		return firstTimePassword;
	}

	public void setFirstTimePassword(String firstTimePassword) {
		this.firstTimePassword = firstTimePassword;
	}
}
