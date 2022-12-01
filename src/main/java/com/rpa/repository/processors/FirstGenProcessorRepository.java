/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.repository.processors;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.rpa.model.processors.GLBatchProcessor;

public interface FirstGenProcessorRepository extends JpaRepository<GLBatchProcessor, Integer>  {
	List<GLBatchProcessor> findAll();
	 GLBatchProcessor findByRunNo(@Param("runNo") String id);
	// List<GLBatchProcessor> save(List<GLBatchProcessor> gLBatchProcessors);
	 List<GLBatchProcessor> findByTicketId(@Param("ticketId") List<String> ticket_id);
	List<GLBatchProcessor> findByDateFrom(@Param("dateFrom") Date dateFrom);
}
