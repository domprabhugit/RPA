/*
 * Robotic Process Automation
 * @originalAuthor S.Mohamed Ismaiel
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.FolderConfigurationMaster;

public interface FolderConfigurationMasterRepository extends JpaRepository<FolderConfigurationMaster, Long>{

	@Query("SELECT s from FolderConfigurationMaster s WHERE s.processName=?1")
	List<FolderConfigurationMaster> findBanksByProcessName(String processName);

	@Query("SELECT distinct s.processName from FolderConfigurationMaster s ")
	List<String> findUniqueProcessList();

	@Query("SELECT s from FolderConfigurationMaster s WHERE s.processName=?1 and s.customerName=?2")
	List<FolderConfigurationMaster> getFileTypesByBankAndProcessName(String processName, String custName);

	@Query("SELECT distinct s from FolderConfigurationMaster s")
	List<FolderConfigurationMaster> findUniqueBankList();

	
}
