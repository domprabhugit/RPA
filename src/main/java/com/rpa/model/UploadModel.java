/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.model;

import org.springframework.web.multipart.MultipartFile;

public class UploadModel {
    private MultipartFile fileInput;
    private String bankName;
    private String processName;
    private String folderDesc;
    private String folderPath;
	public MultipartFile getFileInput() {
		return fileInput;
	}
	public void setFileInput(MultipartFile fileInput) {
		this.fileInput = fileInput;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public String getFolderDesc() {
		return folderDesc;
	}
	public void setFolderDesc(String folderDesc) {
		this.folderDesc = folderDesc;
	}
	public String getFolderPath() {
		return folderPath;
	}
	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}
}
