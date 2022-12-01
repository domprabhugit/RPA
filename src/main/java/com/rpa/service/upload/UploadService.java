/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.upload;

import java.util.List;

import com.rpa.model.FileUpload;

public interface UploadService {
	List<FileUpload> getUniqueRecords();
	List<FileUpload> findByBankName(String bankName);
	List<FileUpload> findByBankNameAndProcessName(String bankName,String processName);
	List<FileUpload> findByBankNameAndProcessNameAndFolderDesc(String bankName,String processName,String folderDesc);	
}

