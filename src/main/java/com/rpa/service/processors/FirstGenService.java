package com.rpa.service.processors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.rpa.model.processors.GLBatchProcessor;

@Component
@Service
public class FirstGenService {
	
	@Autowired
	FirstGenProcessorService batchProcessorService;


	public void save(GLBatchProcessor glBatchProcessor)
	{
		batchProcessorService.save(glBatchProcessor);
	}
	
}
