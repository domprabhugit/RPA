package com.rpa.response;

public class GridAutomationStatus {
	private String fileType;
	private String successFiles;
	private String totalFiles;
	private String validationFailed;
	private String errorFiles;
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getSuccessFiles() {
		return successFiles;
	}
	public void setSuccessFiles(String successFiles) {
		this.successFiles = successFiles;
	}
	public String getTotalFiles() {
		return totalFiles;
	}
	public void setTotalFiles(String totalFiles) {
		this.totalFiles = totalFiles;
	}
	public String getValidationFailed() {
		return validationFailed;
	}
	public void setValidationFailed(String validationFailed) {
		this.validationFailed = validationFailed;
	}
	public String getErrorFiles() {
		return errorFiles;
	}
	public void setErrorFiles(String errorFiles) {
		this.errorFiles = errorFiles;
	}
	
	

}
