/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.process;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.rpa.constants.RPAConstants;
import com.rpa.model.ApplicationConfiguration;
import com.rpa.model.ApplicationMaster;
import com.rpa.model.BusinessProcess;
import com.rpa.model.EmailConfiguration;
import com.rpa.model.FolderConfigurationDetails;
import com.rpa.model.FolderConfigurationMaster;
import com.rpa.model.User;
import com.rpa.repository.ApplicationConfigurationRepository;
import com.rpa.repository.ApplicationRepository;
import com.rpa.repository.FolderConfigurationDetailsRepository;
import com.rpa.repository.FolderConfigurationMasterRepository;
import com.rpa.repository.process.EmailConfigurationRepository;
import com.rpa.repository.process.ProcessRepository;
import com.rpa.service.UserService;

@Service
public class ProcessServiceImpl implements ProcessService {

	@Autowired
	private ProcessRepository processRepository;

	@Autowired
	private EmailConfigurationRepository emailConfigurationRepository;

	@Autowired
	private ApplicationConfigurationRepository applicationDetailRepository;

	@Autowired
	private ApplicationRepository applicationRepository;

	@Autowired
	private FolderConfigurationMasterRepository folderConfigurationMasterRepository;

	@Autowired
	private FolderConfigurationDetailsRepository folderConfigurationDetailsRepository;

	@Autowired
	private UserService userService;

	@Override
	public List<BusinessProcess> getAllBusinessProcesses() {
		return processRepository.findAll();
	}

	@Override
	public BusinessProcess findByProcessName(String processName) {
		return processRepository.findByProcessName(processName);
	}

	@Override
	public List<EmailConfiguration> checkProcessMailExist(Long id) {
		return emailConfigurationRepository.findByProcessId(id);
	}

	@Override
	public List<EmailConfiguration> saveAllProcessMail(List<EmailConfiguration> emailConfiguration) {
		return emailConfigurationRepository.save(emailConfiguration);
	}

	@Override
	public EmailConfiguration saveProcessMail(EmailConfiguration emailConfiguration) {
		return emailConfigurationRepository.save(emailConfiguration);
	}

	@Override
	public List<EmailConfiguration> findAll() {
		return emailConfigurationRepository.findEamilConfigsByProcessList(getCurrentUserProcessList());
	}

	@Override
	public BusinessProcess getOne(Long id) {
		return processRepository.getOne(id);
	}

	@Override
	public EmailConfiguration findById(String id) {
		return emailConfigurationRepository.getOne(Long.valueOf(id));
	}

	@Override
	public boolean mailProcessDelete(ArrayList<BigDecimal> Ids) {
		boolean flag = false;
		EmailConfiguration emailConfiguration = null;
		for (BigDecimal Id : Ids) {
			emailConfiguration = emailConfigurationRepository.findOne(Id.longValue());
			emailConfiguration.setStatus(RPAConstants.N);
			emailConfigurationRepository.save(emailConfiguration);
			flag = true;
		}
		return flag;
	}

	@Override
	public boolean buttonMailProcessActivate(ArrayList<BigDecimal> Ids) {
		boolean flag = false;
		EmailConfiguration emailConfiguration = null;
		for (BigDecimal Id : Ids) {
			emailConfiguration = emailConfigurationRepository.findOne(Id.longValue());
			emailConfiguration.setStatus(RPAConstants.Y);
			emailConfigurationRepository.save(emailConfiguration);
			flag = true;
		}
		return flag;
	}

	@Override
	public List<ApplicationConfiguration> checkAppProcessDetailsExist(Long id, String appId) {
		return applicationDetailRepository.findByProcessIdAndApp(id, Long.valueOf(appId));
	}

	@Override
	public ApplicationConfiguration save(ApplicationConfiguration appMasterDetails) {
		return applicationDetailRepository.save(appMasterDetails);
	}

	@Override
	public List<ApplicationConfiguration> getProcessApps() {
		return applicationDetailRepository.finAppConfigurationDetailsByUserProcessList(getCurrentUserProcessList());
	}

	@Override
	public ApplicationMaster findByAppId(Long appId) {
		return applicationRepository.getOne(appId);
	}

	@Override
	public ApplicationConfiguration findById(Long id) {
		return applicationDetailRepository.findById(id);
	}

	@Override
	public boolean appProcessDelete(ArrayList<BigDecimal> Ids) {
		boolean flag = false;
		ApplicationConfiguration applicationDetails = null;
		for (BigDecimal Id : Ids) {
			applicationDetails = applicationDetailRepository.findOne(Id.longValue());
			applicationDetails.setStatus(RPAConstants.N);
			applicationDetailRepository.save(applicationDetails);
			flag = true;
		}
		return flag;
	}

	@Override
	public boolean appProcessActivate(ArrayList<BigDecimal> Ids) {
		boolean flag = false;
		ApplicationConfiguration applicationDetails = null;
		for (BigDecimal Id : Ids) {
			applicationDetails = applicationDetailRepository.findOne(Id.longValue());
			applicationDetails.setStatus(RPAConstants.Y);
			applicationDetailRepository.save(applicationDetails);
			flag = true;
		}
		return flag;
	}

	@Override
	public List<ApplicationConfiguration> findByProcessId(Long processId) {
		return applicationDetailRepository.findByProcessId(processId);
	}

	@Override
	public List<FolderConfigurationMaster> findBanksByProcessName(String processName) {
		return folderConfigurationMasterRepository.findBanksByProcessName(processName);
	}

	@Override
	public List<String> findUniqueProcessList() {
		return folderConfigurationMasterRepository.findUniqueProcessList();
	}

	@Override
	public List<FolderConfigurationMaster> getFileTypesByBankAndProcessName(String processName, String custName) {
		return folderConfigurationMasterRepository.getFileTypesByBankAndProcessName(processName, custName);
	}

	@Override
	public List<FolderConfigurationDetails> getFolderConfigDetails() {
		return folderConfigurationDetailsRepository.findAll();
	}

	@Override
	public List<FolderConfigurationDetails> checkFolderConfigDetailsExist(String processName, String customerName,
			String fileType) {
		return folderConfigurationDetailsRepository.checkFolderConfigDetailsExist(processName, customerName, fileType);
	}

	@Override
	public FolderConfigurationDetails save(FolderConfigurationDetails folderConfigurationDetails) {
		return folderConfigurationDetailsRepository.save(folderConfigurationDetails);
	}

	@Override
	public FolderConfigurationDetails findByFolderConfigId(Long id) {
		return folderConfigurationDetailsRepository.findById(id);
	}

	@Override
	public boolean deleteFolderConfig(ArrayList<BigDecimal> Ids) {
		boolean flag = false;
		FolderConfigurationDetails folderConfigurationDetails = null;
		for (BigDecimal Id : Ids) {
			folderConfigurationDetails = folderConfigurationDetailsRepository.findOne(Id.longValue());
			folderConfigurationDetails.setStatus(RPAConstants.N);
			folderConfigurationDetailsRepository.save(folderConfigurationDetails);
			flag = true;
		}
		return flag;
	}

	@Override
	public boolean activateFolderConfig(ArrayList<BigDecimal> Ids) {
		boolean flag = false;
		FolderConfigurationDetails folderConfigurationDetails = null;
		for (BigDecimal Id : Ids) {
			folderConfigurationDetails = folderConfigurationDetailsRepository.findOne(Id.longValue());
			folderConfigurationDetails.setStatus(RPAConstants.Y);
			folderConfigurationDetailsRepository.save(folderConfigurationDetails);
			flag = true;
		}
		return flag;
	}

	public List<Long> getCurrentUserProcessList() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findByUsername(auth.getName());
		Set<BusinessProcess> processList = user.getBusinessProcesses();
		List<Long> userProcessList = new ArrayList<>();
		for (BusinessProcess process : processList) {
			userProcessList.add(process.getId());
		}
		return userProcessList;
	}

	@Override
	public List<FolderConfigurationMaster> getUniqueBankList() {
		return folderConfigurationMasterRepository.findUniqueBankList();
	}
}
