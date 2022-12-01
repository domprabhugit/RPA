/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.process;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.rpa.model.ApplicationConfiguration;
import com.rpa.model.ApplicationMaster;
import com.rpa.model.BusinessProcess;
import com.rpa.model.EmailConfiguration;
import com.rpa.model.FolderConfigurationDetails;
import com.rpa.model.FolderConfigurationMaster;

public interface ProcessService {
	List<BusinessProcess> getAllBusinessProcesses();
	BusinessProcess findByProcessName(String processName);
	List<EmailConfiguration> checkProcessMailExist(Long id);
	List<EmailConfiguration> saveAllProcessMail(List<EmailConfiguration> businessProcessList);
	EmailConfiguration saveProcessMail(EmailConfiguration EmailConfiguration);
	List<EmailConfiguration> findAll();
	BusinessProcess getOne(Long id);
	EmailConfiguration findById(String id);
	boolean mailProcessDelete(ArrayList<BigDecimal> decArray);
	boolean buttonMailProcessActivate(ArrayList<BigDecimal> decArray);
	List<ApplicationConfiguration> checkAppProcessDetailsExist(Long id, String appId);
	ApplicationConfiguration save(ApplicationConfiguration appMasterDetails);
	List<ApplicationConfiguration> getProcessApps();
	ApplicationMaster findByAppId(Long appId);
	ApplicationConfiguration findById(Long id);
	boolean appProcessDelete(ArrayList<BigDecimal> decArray);
	boolean appProcessActivate(ArrayList<BigDecimal> decArray);
	List<ApplicationConfiguration> findByProcessId(Long processId);
	List<FolderConfigurationMaster> findBanksByProcessName(String processName);
	List<String> findUniqueProcessList();
	List<FolderConfigurationMaster> getFileTypesByBankAndProcessName(String processName, String custName);
	List<FolderConfigurationDetails> getFolderConfigDetails();
	List<FolderConfigurationDetails> checkFolderConfigDetailsExist(String processName, String customerName,
			String fileType);
	com.rpa.model.FolderConfigurationDetails save(FolderConfigurationDetails folderConfigurationDetails);
	FolderConfigurationDetails findByFolderConfigId(Long valueOf);
	boolean deleteFolderConfig(ArrayList<BigDecimal> decArray);
	boolean activateFolderConfig(ArrayList<BigDecimal> decArray);
	List<FolderConfigurationMaster> getUniqueBankList();
}


