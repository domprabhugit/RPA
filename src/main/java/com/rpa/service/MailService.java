/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service;

import java.util.Date;
import java.util.List;

import com.rpa.model.processors.LifeLineMigration;

public interface MailService {    
    
    public void sendEmailForGlBatchProcess(String filePath);

	public void sendMigrationStatus(boolean isMismatchAvailable, List<LifeLineMigration> list,Date date, String toMailid);
	
	public void getPolicyPDFMailPendingstatus(String filePath, int row_counter);
}