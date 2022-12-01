package com.rpa.model.processors;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="AGENT_RESPONSE")
public class AgentResponse implements  Serializable  {

private String name;

@Id
@SequenceGenerator(name = "agent_response_seq", sequenceName = "agent_response_seq", allocationSize=1)
@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="agent_response_seq")
	private int id;
	
	private String totOdPremium;
	
	private String accessType;
	
	private String slab;
	
	private String searchSlab;
	
	private String wsiSlab;
	
	private String searchInsentive;
	
	private String wsiIncentive;
	
	private String totalIncentive;
	
	private String lateLogin;
	
	private String finalIncentive;
	
	private String motorOdIncentive;
	
	private String noOfTwoWheelerPolicies;
	
	private String twoWheelerIncentive;
	
	private String monthYear;
	
	private String flagType;
	
	private String search;
	
	private String wsi;
	
	private String perPolicy;
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMotorOdIncentive() {
		return motorOdIncentive;
	}

	public void setMotorOdIncentive(String motorOdIncentive) {
		this.motorOdIncentive = motorOdIncentive;
	}

	public String getTwoWheelerIncentive() {
		return twoWheelerIncentive;
	}

	public void setTwoWheelerIncentive(String twoWheelerIncentive) {
		this.twoWheelerIncentive = twoWheelerIncentive;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAccessType() {
		return accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public String getSlab() {
		return slab;
	}

	public void setSlab(String slab) {
		this.slab = slab;
	}

	public String getTotOdPremium() {
		return totOdPremium;
	}

	public void setTotOdPremium(String totOdPremium) {
		this.totOdPremium = totOdPremium;
	}

	public String getNoOfTwoWheelerPolicies() {
		return noOfTwoWheelerPolicies;
	}

	public void setNoOfTwoWheelerPolicies(String noOfTwoWheelerPolicies) {
		this.noOfTwoWheelerPolicies = noOfTwoWheelerPolicies;
	}

	public String getMonthYear() {
		return monthYear;
	}

	public void setMonthYear(String monthYear) {
		this.monthYear = monthYear;
	}

	public String getFlagType() {
		return flagType;
	}

	public void setFlagType(String flagType) {
		this.flagType = flagType;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public String getWsi() {
		return wsi;
	}

	public void setWsi(String wsi) {
		this.wsi = wsi;
	}

	public String getSearchSlab() {
		return searchSlab;
	}

	public void setSearchSlab(String searchSlab) {
		this.searchSlab = searchSlab;
	}

	public String getWsiSlab() {
		return wsiSlab;
	}

	public void setWsiSlab(String wsiSlab) {
		this.wsiSlab = wsiSlab;
	}

	public String getSearchInsentive() {
		return searchInsentive;
	}

	public void setSearchInsentive(String searchInsentive) {
		this.searchInsentive = searchInsentive;
	}

	public String getWsiIncentive() {
		return wsiIncentive;
	}

	public void setWsiIncentive(String wsiIncentive) {
		this.wsiIncentive = wsiIncentive;
	}

	public String getTotalIncentive() {
		return totalIncentive;
	}

	public void setTotalIncentive(String totalIncentive) {
		this.totalIncentive = totalIncentive;
	}

	public String getLateLogin() {
		return lateLogin;
	}

	public void setLateLogin(String lateLogin) {
		this.lateLogin = lateLogin;
	}

	public String getFinalIncentive() {
		return finalIncentive;
	}

	public void setFinalIncentive(String finalIncentive) {
		this.finalIncentive = finalIncentive;
	}

	public String getPerPolicy() {
		return perPolicy;
	}

	public void setPerPolicy(String perPolicy) {
		this.perPolicy = perPolicy;
	}

	
	
	
}
