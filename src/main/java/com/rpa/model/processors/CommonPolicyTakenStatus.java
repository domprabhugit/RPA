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
@Table(name = "COMMON_POLICY_TAKEN_STATUS")
public class CommonPolicyTakenStatus  implements Serializable   {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	private String policyDate;
	
	private String hondaStatus;
	
	private String fordStatus;
	
	private String tataPvStatus;
	
	private String tataCvStatus;
	
	private String abiblStatus;
	
	private String miblStatus;
	
	private String volvoStatus;
	
	private String tafeStatus;
	
	private String piaggioStatus;

	public String getPolicyDate() {
		return policyDate;
	}

	public void setPolicyDate(String policyDate) {
		this.policyDate = policyDate;
	}

	public String getHondaStatus() {
		return hondaStatus;
	}

	public void setHondaStatus(String hondaStatus) {
		this.hondaStatus = hondaStatus;
	}

	public String getFordStatus() {
		return fordStatus;
	}

	public void setFordStatus(String fordStatus) {
		this.fordStatus = fordStatus;
	}

	public String getTataPVStatus() {
		return tataPvStatus;
	}

	public void setTataPVStatus(String tataPVStatus) {
		this.tataPvStatus = tataPVStatus;
	}

	public String getTataCVStatus() {
		return tataCvStatus;
	}

	public void setTataCVStatus(String tataCVStatus) {
		this.tataCvStatus = tataCVStatus;
	}

	public String getAbiblStatus() {
		return abiblStatus;
	}

	public void setAbiblStatus(String abiblStatus) {
		this.abiblStatus = abiblStatus;
	}

	public String getMiblStatus() {
		return miblStatus;
	}

	public void setMiblStatus(String miblStatus) {
		this.miblStatus = miblStatus;
	}
	
	public String getVolvoStatus() {
		return volvoStatus;
	}

	public void setVolvoStatus(String volvoStatus) {
		this.volvoStatus = volvoStatus;
	}

	public String getTataPvStatus() {
		return tataPvStatus;
	}

	public void setTataPvStatus(String tataPvStatus) {
		this.tataPvStatus = tataPvStatus;
	}

	public String getTataCvStatus() {
		return tataCvStatus;
	}

	public void setTataCvStatus(String tataCvStatus) {
		this.tataCvStatus = tataCvStatus;
	}

	public String getTafeStatus() {
		return tafeStatus;
	}

	public void setTafeStatus(String tafeStatus) {
		this.tafeStatus = tafeStatus;
	}

	public String getPiaggioStatus() {
		return piaggioStatus;
	}

	public void setPiaggioStatus(String piaggioStatus) {
		this.piaggioStatus = piaggioStatus;
	}
	
	
	
}
