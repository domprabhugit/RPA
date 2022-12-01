/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rpa.model.processors.GLBatchProcessor;
import com.rpa.repository.processors.FirstGenProcessorRepository;

@Service
public class FirstGenProcessorServiceImpl implements FirstGenProcessorService {
    
  @Autowired
   private FirstGenProcessorRepository batchProcessorRepository;
   
	@Override
	public void save(GLBatchProcessor glBatchProcessor) {
		batchProcessorRepository.save(glBatchProcessor);
	}

	@Override
	public List<GLBatchProcessor> findAll() {
		return batchProcessorRepository.findAll();
	}
	
	@Override
	public GLBatchProcessor findByRunNo(String id){
		return batchProcessorRepository.findByRunNo(id);
	}
	
	@Override
	public List<GLBatchProcessor> findByTicketId(List<String> ticketId){
		return batchProcessorRepository.findByTicketId(ticketId);
	}

	@Override
	public List<GLBatchProcessor> findByDateFrom(Date dateFrom){
		return batchProcessorRepository.findByDateFrom(dateFrom);
	}
}
