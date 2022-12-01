/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.util.Date;
import java.util.List;

import com.rpa.model.processors.GLBatchProcessor;

public interface FirstGenProcessorService {
    void save(GLBatchProcessor glBatchProcessor);

    List<GLBatchProcessor> findAll();

	GLBatchProcessor findByRunNo(String runNo);

	List<GLBatchProcessor> findByTicketId(List<String> ticketId);

	List<GLBatchProcessor> findByDateFrom(Date dateFrom);
}
