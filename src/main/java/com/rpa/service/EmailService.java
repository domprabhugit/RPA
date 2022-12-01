/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service;

import java.util.Date;
import java.util.List;

import org.apache.camel.Exchange;

import com.rpa.model.EmailConfiguration;
import com.rpa.model.TransactionInfo;
import com.rpa.model.User;
import com.rpa.model.processors.LifeLineMigration;

public interface EmailService {
 
    public void vb64ComplianceNotification(String bankName, String filePath, TransactionInfo transactionInfo);
     
    public void sendEmailForUserCreation(User user);   
    
    public void sendMigrationStatus(boolean isMismatchAvailable, List<LifeLineMigration> list, Date date, String toMailId);

	public String[] getToMailIds(String processName);

	public String[] getCcMailIds(String processName);

	public List<EmailConfiguration> findByProcessId(Long processId);

	public void lifeLineInwardUplaodNotification(String errorFilePath, Exchange exchange, TransactionInfo transactionInfo);

	void carPolicyExtractionNotification(TransactionInfo transactionInfo, String errorMsg,String appName);
	
	void carPolicyMarutiSrcNotification(String errorMsg,String appName);
	
	void fileTransferNotification(TransactionInfo transactionInfo, String errorMsg);   
}