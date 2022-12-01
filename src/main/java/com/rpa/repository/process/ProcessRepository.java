/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.repository.process;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.BusinessProcess;

public interface ProcessRepository extends JpaRepository<BusinessProcess, Long> {
	List<BusinessProcess> findAll();
	BusinessProcess findByProcessName(String processName);
	BusinessProcess getOne(Long id);
}
