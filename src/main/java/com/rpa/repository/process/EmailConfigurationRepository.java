/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.repository.process;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.EmailConfiguration;

public interface EmailConfigurationRepository extends JpaRepository<EmailConfiguration, Long> {
	List<EmailConfiguration> findByProcessId(Long id);
	List<EmailConfiguration> save(List<EmailConfiguration> businessProcessList);
	@Query("Select s from EmailConfiguration s where s.processId=?1 and status='Y' ")
	List<EmailConfiguration> findByActiveProcessId(Long id);
	@Query("Select s from EmailConfiguration s where s.processId in (?1) ")
	List<EmailConfiguration> findEamilConfigsByProcessList(List<Long> userProcessList);
}
