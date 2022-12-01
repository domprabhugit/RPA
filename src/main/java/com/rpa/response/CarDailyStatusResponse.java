package com.rpa.response;

public class CarDailyStatusResponse {

	private String totalPolciesCount;
	
	private String policyPdfExtractedCount;

	private String proposalPdfExtractedCount;
	
	private String policyPdfErrorCount;
	
	private String proposalPdfErrorCount;
	
	private String policyPdfUploadedCount;

	private String proposalPdfUploadedCount;

	public String getTotalPolciesCount() {
		return totalPolciesCount;
	}

	public void setTotalPolciesCount(String totalPolciesCount) {
		this.totalPolciesCount = totalPolciesCount;
	}

	public String getPolicyPdfExtractedCount() {
		return policyPdfExtractedCount;
	}

	public void setPolicyPdfExtractedCount(String policyPdfExtractedCount) {
		this.policyPdfExtractedCount = policyPdfExtractedCount;
	}

	public String getProposalPdfExtractedCount() {
		return proposalPdfExtractedCount;
	}

	public void setProposalPdfExtractedCount(String proposalPdfExtractedCount) {
		this.proposalPdfExtractedCount = proposalPdfExtractedCount;
	}

	public String getPolicyPdfErrorCount() {
		return policyPdfErrorCount;
	}

	public void setPolicyPdfErrorCount(String policyPdfErrorCount) {
		this.policyPdfErrorCount = policyPdfErrorCount;
	}

	public String getProposalPdfErrorCount() {
		return proposalPdfErrorCount;
	}

	public void setProposalPdfErrorCount(String proposalPdfErrorCount) {
		this.proposalPdfErrorCount = proposalPdfErrorCount;
	}

	public String getPolicyPdfUploadedCount() {
		return policyPdfUploadedCount;
	}

	public void setPolicyPdfUploadedCount(String policyPdfUploadedCount) {
		this.policyPdfUploadedCount = policyPdfUploadedCount;
	}

	public String getProposalPdfUploadedCount() {
		return proposalPdfUploadedCount;
	}

	public void setProposalPdfUploadedCount(String proposalPdfUploadedCount) {
		this.proposalPdfUploadedCount = proposalPdfUploadedCount;
	}
	
	
}
