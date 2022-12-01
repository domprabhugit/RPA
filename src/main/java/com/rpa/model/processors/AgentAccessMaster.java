package com.rpa.model.processors;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="AGENT_ACCESS_MASTER")
public class AgentAccessMaster implements  Serializable {

	@Id
	@SequenceGenerator(name = "agent_access_master_seq", sequenceName = "agent_access_master_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="agent_access_master_seq")
	long id;
	String agentName;
	String leadAccess;
	String lateLogin;
	String monthYear;
	public String getAgentName() {
		return agentName;
	}
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	public String getLeadAccess() {
		return leadAccess;
	}
	public void setLeadAccess(String leadAccess) {
		this.leadAccess = leadAccess;
	}
	public String getLateLogin() {
		return lateLogin;
	}
	public void setLateLogin(String lateLogin) {
		this.lateLogin = lateLogin;
	}
	public String getMonthYear() {
		return monthYear;
	}
	public void setMonthYear(String monthYear) {
		this.monthYear = monthYear;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	
}
