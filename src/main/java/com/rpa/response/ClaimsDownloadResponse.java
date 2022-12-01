package com.rpa.response;

public class ClaimsDownloadResponse {

	private String totalClaims;
	
	private String processedClaims;
	
	private String claimsWithFiles;
	
	private String claimsWithoutFiles;

	public String getTotalClaims() {
		return totalClaims;
	}

	public void setTotalClaims(String totalClaims) {
		this.totalClaims = totalClaims;
	}

	public String getProcessedClaims() {
		return processedClaims;
	}

	public void setProcessedClaims(String processedClaims) {
		this.processedClaims = processedClaims;
	}

	public String getClaimsWithFiles() {
		return claimsWithFiles;
	}

	public void setClaimsWithFiles(String claimsWithFiles) {
		this.claimsWithFiles = claimsWithFiles;
	}

	public String getClaimsWithoutFiles() {
		return claimsWithoutFiles;
	}

	public void setClaimsWithoutFiles(String claimsWithoutFiles) {
		this.claimsWithoutFiles = claimsWithoutFiles;
	}

	
}
