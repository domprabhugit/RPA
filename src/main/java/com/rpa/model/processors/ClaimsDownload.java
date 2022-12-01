package com.rpa.model.processors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "claims_download")
public class ClaimsDownload {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	private String claimNumber;
	
	private String isProcessed;
	
	private String isFileAvailable;

	public String getClaimNumber() {
		return claimNumber;
	}

	public void setClaimNumber(String claimNumber) {
		this.claimNumber = claimNumber;
	}

	public String getIsProcessed() {
		return isProcessed;
	}

	public void setIsProcessed(String isProcessed) {
		this.isProcessed = isProcessed;
	}

	public String getIsFileAvailable() {
		return isFileAvailable;
	}

	public void setIsFileAvailable(String isFileAvailable) {
		this.isFileAvailable = isFileAvailable;
	}
	
	
}
