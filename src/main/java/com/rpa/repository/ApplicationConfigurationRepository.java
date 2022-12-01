/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.ApplicationConfiguration;

public interface ApplicationConfigurationRepository extends JpaRepository<ApplicationConfiguration, Long>{

	@Query("SELECT s from ApplicationConfiguration s WHERE s.processId=?1 and s.appId=?2 ")
	List<ApplicationConfiguration> findByProcessIdAndApp(Long id, Long valueOf);

	ApplicationConfiguration findById(Long id);

	@Query("SELECT s from ApplicationConfiguration s WHERE s.processId=?1 and s.appId=?2 ")
	ApplicationConfiguration findByProcessAndAppId(Long processId, Long appId);
	
	List<ApplicationConfiguration> findByProcessId(Long processId);
	
	ApplicationConfiguration findByAppId(Long appId);

	@Query("Select s from ApplicationConfiguration s where s.processId in (?1) ")
	List<ApplicationConfiguration> finAppConfigurationDetailsByUserProcessList(List<Long> userProcessList);
}
