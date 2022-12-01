/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.repository.upload;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.FileUpload;

public interface UploadRepository extends JpaRepository<FileUpload, Long> {
	@Query(value = "SELECT distinct fu FROM FileUpload fu")
	List<FileUpload> getUniqueRecords();
	List<FileUpload> findByBankName(String bankName);
	List<FileUpload> findByBankNameAndProcessName(String bankName,String processName);	
	List<FileUpload> findByBankNameAndProcessNameAndFolderDesc(String bankName,String processName,String folderDesc);	
}
