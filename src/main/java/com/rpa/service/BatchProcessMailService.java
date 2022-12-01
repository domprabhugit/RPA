/*
 * Robotic Process Automation
 * @originalAuthor Vijayananth.M
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.service;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.rpa.util.UtilityFile;

@Configuration
@EnableScheduling
public class BatchProcessMailService {

	
	@Autowired
	MailService mailService;
	
	@Autowired
	private Environment environment;
	
	private static final Logger logger=LoggerFactory.getLogger(BatchProcessMailService.class.getName());
		
	@Scheduled(cron = "0 0 11,19 ? * *")
	public void BatchProcessMailSend()
	{
		
		logger.info("BatchProcess Mail Service Started");
		/*if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {*/
			try {
				mailService.sendEmailForGlBatchProcess(UtilityFile.getCodeBasePath()+UtilityFile.getBatchProperty("FirstGen.File.Process.Location")+UtilityFile.dateToSting(UtilityFile.yesterday(), "dd-MM-YYYY"));
			 
			} catch (Exception e) {
				
				logger.error("Error in Batch Process Mail Sending Method"+e.getMessage(),e);
			}	
		/*}else{
			logger.info("BatchProcess - Scheduler is not applicable for uat ");
		}*/
	}
}
