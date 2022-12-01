/*
 * Robotic Process Automation
 * @originalAuthor S.Mohamed Ismaiel
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.FolderConfigurationDetails;

public interface FolderConfigurationDetailsRepository extends JpaRepository<FolderConfigurationDetails, Long>{

	List<FolderConfigurationDetails> findAll();

	@Query("SELECT s from FolderConfigurationDetails s WHERE s.processName=?1 and s.customerName=?2 and fileType=?3 ")
	List<FolderConfigurationDetails> checkFolderConfigDetailsExist(String processName, String customerName,
			String fileType);

	FolderConfigurationDetails findById(Long id);

	@Query("SELECT s from FolderConfigurationDetails s WHERE s.processName=?1 and s.customerName=?2 and s.fileType=?3 and s.status='Y'")
	List<FolderConfigurationDetails> getActiveFolderConfigDetails(String processName, String customerName,
			String fileType);
	
}
