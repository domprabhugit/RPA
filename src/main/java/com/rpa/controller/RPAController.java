/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.ApplicationConfiguration;
import com.rpa.model.ApplicationMaster;
import com.rpa.model.BusinessProcess;
import com.rpa.model.EmailConfiguration;
import com.rpa.model.FileUpload;
import com.rpa.model.FolderConfigurationDetails;
import com.rpa.model.FolderConfigurationMaster;
import com.rpa.model.TransactionInfo;
import com.rpa.model.UploadModel;
import com.rpa.model.User;
import com.rpa.model.processors.AbiblPolicy;
import com.rpa.model.processors.AgentAccessMaster;
import com.rpa.model.processors.AgentConfig;
import com.rpa.model.processors.AgentIncentiveModel;
import com.rpa.model.processors.AgentResponse;
import com.rpa.model.processors.AgentSlabMaster;
import com.rpa.model.processors.FirstgenPolicy;
import com.rpa.model.processors.FordPolicy;
import com.rpa.model.processors.GridAutomationModel;
import com.rpa.model.processors.GridFilesCountDetails;
import com.rpa.model.processors.GridWithModelSheetAutomationModel;
import com.rpa.model.processors.HondaPolicy;
import com.rpa.model.processors.InHouseLeads;
import com.rpa.model.processors.LeadTargetMaster;
import com.rpa.model.processors.LifeLineMigration;
import com.rpa.model.processors.MarutiPolicy;
import com.rpa.model.processors.MiblPolicy;
import com.rpa.model.processors.ModelCodeCreation;
import com.rpa.model.processors.PiaggioPolicy;
import com.rpa.model.processors.PlSlabMaster;
import com.rpa.model.processors.PolicyPdfMailRetrigger;
import com.rpa.model.processors.TafePolicy;
import com.rpa.model.processors.TataPolicy;
import com.rpa.model.processors.TlInHouseSlabMaster;
import com.rpa.model.processors.TlSlabMaster;
import com.rpa.model.processors.VolvoPolicy;
import com.rpa.repository.PolicyPdfMailRetriggerRepository;
import com.rpa.response.CarDailyStatusResponse;
import com.rpa.response.ChartMultiData;
import com.rpa.response.ClaimsDownloadResponse;
import com.rpa.response.GridModelRowStatusResponse;
import com.rpa.response.MarutiUploadReferenceResponse;
import com.rpa.response.MigrationResponse;
import com.rpa.response.ParamConfigResponse;
import com.rpa.response.UserProcessResponse;
import com.rpa.service.CommonService;
import com.rpa.service.DashboardService;
import com.rpa.service.ErrorInfoService;
import com.rpa.service.TransactionInfoService;
import com.rpa.service.UserService;
import com.rpa.service.process.ProcessService;
import com.rpa.service.processors.AbiblPolicyService;
import com.rpa.service.processors.AgentIncentiveService;
import com.rpa.service.processors.FirstGenDownloadPolicyService;
import com.rpa.service.processors.FordPolicyService;
import com.rpa.service.processors.GridAutomationService;
import com.rpa.service.processors.GridWithModelSheetAutomationService;
import com.rpa.service.processors.HondaPolicyService;
import com.rpa.service.processors.LifelineMigrationService;
import com.rpa.service.processors.MarutiPolicyService;
import com.rpa.service.processors.MiblPolicyService;
import com.rpa.service.processors.ModelCodeCreationService;
import com.rpa.service.processors.OmniDocsClaimsDocDownloaderService;
import com.rpa.service.processors.PiaggioPolicyService;
import com.rpa.service.processors.TafePolicyService;
import com.rpa.service.processors.TataPolicyService;
import com.rpa.service.processors.VolvoPolicyService;
import com.rpa.service.upload.UploadService;
import com.rpa.util.UtilityFile;

@Controller
public class RPAController {

	private static final Logger logger = LoggerFactory.getLogger(RPAController.class.getName());

	ApplicationContext applicationContext = SpringContext.getAppContext();

	@Autowired
	private UploadService uploadService;

	@Autowired
	private TransactionInfoService transactionInfoService;

	@Autowired
	private CommonService commonService;

	@Autowired
	private UserService userService;

	@Autowired
	private ProcessService processService;

	@Autowired
	private GridAutomationService gridAutomationService;

	@Autowired
	private LifelineMigrationService lifelineMigrationService;

	@Autowired
	private MarutiPolicyService marutiPolicyService;

	@Autowired
	private HondaPolicyService hondaPolicyService;

	@Autowired
	private FordPolicyService fordPolicyService;

	@Autowired
	private TataPolicyService tataPolicyService;

	@Autowired
	private AbiblPolicyService abiblPolicyService;

	@Autowired
	private MiblPolicyService miblPolicyService;

	@Autowired
	private VolvoPolicyService volvoPolicyService;

	@Autowired
	private TafePolicyService tafePolicyService;

	@Autowired
	private PiaggioPolicyService piaggioPolicyService;

	@Autowired
	private FirstGenDownloadPolicyService firstGenDownloadPolicyService;

	@Autowired
	private ErrorInfoService errorInfoService;

	@Autowired
	private OmniDocsClaimsDocDownloaderService omniDocsClaimsDocDownloaderService;

	@Autowired
	private Environment environment;

	@Autowired
	ModelCodeCreationService modelCodeCreationService;

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private GridWithModelSheetAutomationService gridWithModelSheetAutomationService;

	@Autowired
	private PolicyPdfMailRetriggerRepository policyPdfMailRetriggerRepository;

	@Autowired
	AgentIncentiveService agentIncentiveService;

	@GetMapping("/rpa")
	public String covernote(ModelMap model) {
		return "index";
	}

	@GetMapping("/dashboard")
	public String dashboard(ModelMap model) {

		String firstTimePassword = "", id = "";
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			User user = userService.findByUsername(auth.getName());
			if (user.getActive().equals("N")) {
				return "error/403";
			}

			firstTimePassword = user.getFirstTimePassword();
			id = String.valueOf(user.getId());
		} catch (Exception e) {
			logger.error("error in getting session user -->" + e.getMessage());
		}
		if (firstTimePassword.equals(RPAConstants.Y)) {

			model.addAttribute("firstTimePassword", firstTimePassword);
			model.addAttribute("id", id);
			return "login";
		} else {
			logger.info("Inside dashboard()");
			model.addAttribute("processlist", commonService.getProcessDetails());
			commonService.getDashBoardDetails(model);
			return "index";
		}
	}

	@GetMapping("/businessprocess")
	public String businessprocess(ModelMap model) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Set<BusinessProcess> processSet = null;
		List<BusinessProcess> processlist = new ArrayList<BusinessProcess>();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			String currentUserName = authentication.getName();
			User user = userService.findByUsername(currentUserName);
			processSet = user.getBusinessProcesses();
			for (BusinessProcess process : processSet) {
				processlist.add(process);
			}
		}
		model.addAttribute("vb64modal", "vb64modal");
		model.addAttribute("glBatchModal", "glBatchModal");
		model.addAttribute("lifeLineMigrationModal", "lifeLineMigrationModal");
		model.addAttribute("lifeLineUploadModal", "lifeLineUploadModal");
		model.addAttribute("marutiPolicyExtractionModal", "marutiPolicyExtractionModal");
		model.addAttribute("marutiPolicyUploadModal", "marutiPolicyUploadModal");
		model.addAttribute("gridUploadModal", "gridUploadModal");
		model.addAttribute("claimsDownloadModal", "claimsDownloadModal");
		model.addAttribute("processlist", processlist);
		model.addAttribute("dailyFileTransferModal", "dailyFileTransferModal");
		model.addAttribute("policyPdfMailTriggerTranModal", "policyPdfMailTriggerTranModal");
		model.addAttribute("AgentIncentiveCalculatorProcessTranModal", "AgentIncentiveCalculatorProcessTranModal");
		return "businessprocess";
	}

	@GetMapping("/fileupload")
	public String fileupload(ModelMap model) {

		UploadModel form = new UploadModel();
		model.addAttribute("fileuploadForm", form);
		model.addAttribute("banklist", uploadService.getUniqueRecords());

		return "fileupload";
	}

	@GetMapping("/getProcessesByBankNameForFileUpload")
	public @ResponseBody List<FileUpload> getProcessesByBankNameForFileUpload(
			@RequestParam(value = "bankName", required = true) String bankName) {
		List<FileUpload> list = uploadService.findByBankName(bankName);
		return list;
	}

	@GetMapping("/getFoldersByBankNameForFileUpload")
	public @ResponseBody List<FileUpload> getFoldersByBankNameForFileUpload(
			@RequestParam(value = "bankName", required = true) String bankName,
			@RequestParam(value = "processName", required = true) String processName) {
		List<FileUpload> list = uploadService.findByBankNameAndProcessName(bankName, processName);
		return list;
	}

	@PostMapping("/filterTransactionDetails")
	public @ResponseBody List<TransactionInfo> filterTransactionDetails(HttpServletRequest req,
			HttpServletResponse resp, ModelMap model) throws ParseException {

		String startDate = req.getParameter("startDate") == null ? "" : req.getParameter("startDate");
		String endDate = req.getParameter("endDate") == null ? "" : req.getParameter("endDate");
		String processName = req.getParameter("processName") == null ? "" : req.getParameter("processName");

		List<TransactionInfo> list = transactionInfoService.filterTransactionDetails(startDate, endDate, processName);

		return list;
	}

	@PostMapping("/viewTransaction")
	public @ResponseBody TransactionInfo viewTransaction(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) {
		String id = req.getParameter("id") == null ? "" : req.getParameter("id");
		TransactionInfo transactionInfo = transactionInfoService.findById(Long.parseLong(id));

		return transactionInfo;
	}

	@GetMapping(value = "/downloadXLS")
	public void downloadXLS(HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws ParseException {

		String downloadFilePath = request.getParameter("downloadFilePath") == null ? ""
				: request.getParameter("downloadFilePath");

		try {

			File file = new File(downloadFilePath);

			if (!file.exists()) {
				throw new Exception("No Files Exist");
			} else {

				FileInputStream inputStream = new FileInputStream(file);

				response.setHeader("Content-Disposition", "inline; filename=" + file.getName());
				response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

				ServletOutputStream outputStream = response.getOutputStream();
				IOUtils.copy(inputStream, outputStream);

				outputStream.close();
				inputStream.close();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@GetMapping("/emailconfig")
	public String getMailPage(ModelMap model) {
		model.addAttribute("processlist", commonService.getProcessDetails());
		return "emailconfig";
	}

	@PostMapping("/insertEmailConfig")
	public @ResponseBody String insertEmailConfig(HttpServletRequest req, HttpServletResponse resp, ModelMap model)
			throws ParseException {

		logger.info("BEGIN - insertEmailConfig()" + req.getQueryString());

		String mailIds = req.getParameter("toEmailIds") == null ? "" : req.getParameter("toEmailIds");
		if (mailIds.length() > 0) {
			mailIds = mailIds.endsWith(",") ? mailIds.substring(0, mailIds.length() - 1) : mailIds;
		}

		String ccMailIds = req.getParameter("ccEmailIds") == null ? "" : req.getParameter("ccEmailIds");
		if (ccMailIds.length() > 0) {
			ccMailIds = ccMailIds.endsWith(",") ? ccMailIds.substring(0, ccMailIds.length() - 1) : ccMailIds;
		}

		String processes = req.getParameter("processName") == null ? "" : req.getParameter("processName");

		List<EmailConfiguration> businessProcessList = new ArrayList<EmailConfiguration>();

		if (processes.length() > 0) {
			String[] processList = processes.split(",");
			for (String processName : processList) {
				BusinessProcess businessProcess = processService.findByProcessName(processName);
				List<EmailConfiguration> duplicateProcessMail = processService
						.checkProcessMailExist(businessProcess.getId());

				if (duplicateProcessMail.size() > 0) {
					logger.info(
							"END - insertEmailConfig() - Duplicate Email Configuration exists for::-" + processName);
					return RPAConstants.EMAIL_DUPLICATE_CONFIGURATION + processName;
				} else {
					EmailConfiguration emailConfiguration = new EmailConfiguration();
					emailConfiguration.setToEmailIds(mailIds);
					emailConfiguration.setCcEmailIds(ccMailIds);
					emailConfiguration.setProcessId(businessProcess.getId());
					emailConfiguration.setStatus(RPAConstants.Y);
					businessProcessList.add(emailConfiguration);
				}
			}
		}

		if (businessProcessList.size() > 0) {
			for(EmailConfiguration emailConfig : businessProcessList){
				processService.saveProcessMail(emailConfig);
			}

			logger.info("END - insertEmailConfig() - Email Configured.");
			return RPAConstants.EMAIL_ADDED;
		}
		logger.error("END - insertEmailConfig() - Unable to process.");
		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/getEmailDetails")
	public @ResponseBody List<EmailConfiguration> getEmailDetails() {
		List<EmailConfiguration> list = processService.findAll();
		for(EmailConfiguration obj :  list){
			BusinessProcess bp =  processService.getOne(obj.getProcessId());
			obj.setProcessName(bp.getProcessName()); 
			if(obj.getStatus()!=null && obj.getStatus().equals(RPAConstants.Y)){
				obj.setStatus(RPAConstants.Active);
			} else {
				obj.setStatus(RPAConstants.Inactive);
			}
		}
		return list;
	}

	@PostMapping("/viewEmailConfigModal")
	public @ResponseBody EmailConfiguration viewEmailConfigModal(HttpServletRequest req, HttpServletResponse resp) {
		String id = req.getParameter("id") == null ? "" : req.getParameter("id");
		EmailConfiguration emailConfiguration = processService.findById(id);
		emailConfiguration.setProcessName(processService.getOne(emailConfiguration.getProcessId()).getProcessName());
		return emailConfiguration;
	}

	@PostMapping("/updateEmailConfig")
	public @ResponseBody String updateEmailConfig(HttpServletRequest req, HttpServletResponse resp, ModelMap model)
			throws ParseException {

		logger.info("BEGIN - updateEmailConfig()" + req.getQueryString());

		String id = req.getParameter("id") == null ? "" : req.getParameter("id");

		String mailIds = req.getParameter("toEmailIds") == null ? "" : req.getParameter("toEmailIds");
		if (mailIds.length() > 0) {
			mailIds = mailIds.endsWith(",") ? mailIds.substring(0, mailIds.length() - 1) : mailIds;
		}

		String ccMailIds = req.getParameter("ccEmailIds") == null ? "" : req.getParameter("ccEmailIds");
		if (ccMailIds.length() > 0) {
			ccMailIds = ccMailIds.endsWith(",") ? ccMailIds.substring(0, ccMailIds.length() - 1) : ccMailIds;
		}

		EmailConfiguration emailConfiguration = processService.findById(id);
		emailConfiguration.setToEmailIds(mailIds);
		emailConfiguration.setCcEmailIds(ccMailIds);

		if(processService.saveProcessMail(emailConfiguration)!=null){
			logger.info("END - updateEmailConfig() - Email Configured.");
			return RPAConstants.EMAIL_UPDATED;
		}

		logger.error("END - updateEmailConfig() - Unable to process.");
		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/deleteEmailConfig")
	public @ResponseBody String deleteEmailConfig(HttpServletRequest req, HttpServletResponse resp)
			throws ParseException {

		logger.info("BEGIN - deleteEmailConfig()");

		String checked = req.getParameter("checked") == null ? "" : req.getParameter("checked");

		ArrayList<BigDecimal> decArray = new ArrayList<BigDecimal>();

		for (String s : checked.split(",")) {
			decArray.add(new BigDecimal(s));
			logger.info("deleteUser() - Mail-Process to be Deleted::" + s);
		}

		boolean flag = processService.mailProcessDelete(decArray);

		if (flag) {
			logger.info("END - deleteEmailConfig() - Email Deleted.");
			return RPAConstants.EMAIL_DELETED;
		}
		logger.error("END - deleteEmailConfig() - Unable to process.");

		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/activateEmailConfig")
	public @ResponseBody String activateEmailConfig(HttpServletRequest req, HttpServletResponse resp)
			throws ParseException {

		logger.info("BEGIN - activateEmailConfig()");

		String checked = req.getParameter("checked") == null ? "" : req.getParameter("checked");

		ArrayList<BigDecimal> decArray = new ArrayList<BigDecimal>();

		for( String s : checked.split(",") ){
			decArray.add( new BigDecimal(s) );
			logger.info("activateEmailConfig() - ID's to be Activated::"+s);
		}

		boolean flag = processService.buttonMailProcessActivate(decArray);

		if(flag){
			logger.info("END - activateEmailConfig() - Email Activated.");
			return RPAConstants.EMAIL_ACTIVATED;
		}
		logger.error("END - activateEmailConfig() - Unable to process.");

		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/getMigrationStatusByTranId")
	public @ResponseBody MigrationResponse getMigrationStatusByDate(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException {
		MigrationResponse migrationResponse = new MigrationResponse();
		logger.info("BEGIN - getMigrationStatusByDate()");
		String id = req.getParameter("id") == null ? "" : req.getParameter("id");
		TransactionInfo infObj = transactionInfoService.findById(Long.valueOf(id));
		List<TransactionInfo> infoList = new ArrayList<>();
		infoList.add(infObj);
		List<LifeLineMigration> migrationList = lifelineMigrationService.findByCountDate(infObj.getMigrationDate());
		migrationResponse.setLifeLineMigrationList(migrationList); 
		migrationResponse.setTransactionInfoList(infoList);
		logger.info("END - getMigrationStatusByDate() - Fetched Migration status list ");

		return migrationResponse;
	}

	/*@PostMapping("/geTransactionInfoObj")
	public @ResponseBody TransactionInfo getMigrationStatusByDate(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException {
		List<LifeLineMigration> list = null;
		logger.info("BEGIN - getMigrationStatusByDate()");
		String id = req.getParameter("id") == null ? "" : req.getParameter("id");
		TransactionInfo infObj = transactionInfoService.findById(Long.valueOf(id));
		list = lifelineMigrationService.findByCountDate(infObj.getMigrationDate());
		DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		if(!date.equals("")){
		Date countDate = sdf.parse(date);
		list = lifelineMigrationService.findByCountDate(countDate);
		}
		logger.info("END - getMigrationStatusByDate() - Fetched Migration status list ");

		return list;
	}*/



	@GetMapping("/applicationConfig")
	public String getAppDetailsPage(ModelMap model) {
		model.addAttribute("processlist", commonService.getProcessDetails());
		model.addAttribute("applist", commonService.getAppConfiguationList());
		return "applicationconfig";
	}

	@PostMapping("/insertApplicationConfig")
	public @ResponseBody String insertApplicationConfig(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException {

		logger.info("BEGIN - insertApplicationConfig()" + req.getQueryString());

		String processName = req.getParameter("processName") == null ? "" : req.getParameter("processName");
		String appId = req.getParameter("appName") == null ? "" : req.getParameter("appName");
		String url = req.getParameter("url") == null ? "" : req.getParameter("url");
		String username = req.getParameter("username") == null ? "" : req.getParameter("username");
		String password = req.getParameter("password") == null ? "" : req.getParameter("password");

		BusinessProcess businessProcess = processService.findByProcessName(processName);
		List<ApplicationConfiguration> duplicateProcessMail = processService
				.checkAppProcessDetailsExist(businessProcess.getId(), appId);
		if (duplicateProcessMail.size() > 0) {
			logger.info("END - insertApplicationConfig() - Duplicate Application configuration for Application id --> "
					+ appId + " , Processor name -->" + processName);
			return RPAConstants.APPLICATION_DUPLICATE_CONFIGURATION;
		}
		ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
		applicationConfiguration.setAppId(Long.valueOf(appId));
		applicationConfiguration.setProcessId(businessProcess.getId());
		applicationConfiguration.setUrl(url);
		applicationConfiguration.setUsername(username);
		applicationConfiguration.setPassword(password);
		applicationConfiguration.setStatus(RPAConstants.Y);
		applicationConfiguration.setIsPasswordExpired(RPAConstants.N);

		ApplicationConfiguration savedObj = processService.save(applicationConfiguration);
		if (savedObj != null) {
			logger.info(
					"END - insertApplicationConfig() - Application configuration successfuly added for the processor --> "
							+ processName);
			return RPAConstants.APPLICATION_ADDED;
		}
		logger.error("END - insertApplicationConfig() - unable to add Application configuration for the processor -->"
				+ processName);
		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/getApplicationConfigurationDetails")
	public @ResponseBody List<ApplicationConfiguration> getApplicationConfigurationDetails() {
		try {
			List<ApplicationConfiguration> list = processService.getProcessApps();
			for (ApplicationConfiguration obj : list) {
				BusinessProcess bp = processService.getOne(obj.getProcessId());
				obj.setProcessName(bp.getProcessName());
				if (obj.getStatus() != null && obj.getStatus().equals(RPAConstants.Y)) {
					obj.setStatus(RPAConstants.Active);
				} else {
					obj.setStatus(RPAConstants.Inactive);
				}
				ApplicationMaster application = processService.findByAppId(obj.getAppId());
				obj.setAppName(application.getApplicationName());
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@PostMapping("/viewApplicationConfigModal")
	public @ResponseBody ApplicationConfiguration viewProcessApp(HttpServletRequest req, HttpServletResponse resp) {
		String id = req.getParameter("id") == null ? "" : req.getParameter("id");
		ApplicationConfiguration appMasterDetail = processService.findById(Long.valueOf(id));
		appMasterDetail.setProcessName(processService.getOne(appMasterDetail.getProcessId()).getProcessName());
		return appMasterDetail;
	}

	@PostMapping("/updateApplicationConfig")
	public @ResponseBody String updateApplicationConfig(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException {

		logger.info("BEGIN - updateApplicationConfig()" + req.getQueryString());

		String id = req.getParameter("id") == null ? "" : req.getParameter("id");
		String processName = req.getParameter("processName") == null ? "" : req.getParameter("processName");
		String url = req.getParameter("url") == null ? "" : req.getParameter("url");
		String username = req.getParameter("username") == null ? "" : req.getParameter("username");
		String password = req.getParameter("password") == null ? "" : req.getParameter("password");
		String isPasswordExpired = req.getParameter("isPasswordExpired") == null ? "" : req.getParameter("isPasswordExpired");

		ApplicationConfiguration appMasterDetails = processService.findById(Long.valueOf(id));
		appMasterDetails.setUrl(url);
		appMasterDetails.setUsername(username);
		appMasterDetails.setPassword(password);
		appMasterDetails.setStatus(RPAConstants.Y);
		appMasterDetails.setIsPasswordExpired(isPasswordExpired);

		ApplicationConfiguration savedObj = processService.save(appMasterDetails);
		if (savedObj != null) {
			logger.info(
					"END - updateApplicationConfig() - Application configuration successfuly updated for the processor --> "
							+ processName);
			return RPAConstants.APPLICATION_UPDATED;
		}
		logger.error(
				"END - updateApplicationConfig() - unable to update Application configuration for the processor -->"
						+ processName);
		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/deleteApplicationConfig")
	public @ResponseBody String deleteApplicationConfiguration(HttpServletRequest req, HttpServletResponse resp)
			throws ParseException {

		logger.info("BEGIN - deleteApplicationConfiguration()");

		String checked = req.getParameter("checked") == null ? "" : req.getParameter("checked");

		ArrayList<BigDecimal> decArray = new ArrayList<BigDecimal>();

		for (String s : checked.split(",")) {
			decArray.add(new BigDecimal(s));
			logger.info("deleteApplicationConfiguration() - Application configuration to be Deleted::" + s);
		}

		boolean flag = processService.appProcessDelete(decArray);

		if (flag) {
			logger.info("END - deleteApplicationConfiguration() - Application configuration Deleted");
			return RPAConstants.APPLICATION_DELETED;
		}
		logger.error("END - appProcessDelete() - Unable to process");

		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/activateApplicationConfig")
	public @ResponseBody String activateApplicationConfiguration(HttpServletRequest req, HttpServletResponse resp)
			throws ParseException {

		logger.info("BEGIN - activateApplicationConfiguration()");

		String checked = req.getParameter("checked") == null ? "" : req.getParameter("checked");

		ArrayList<BigDecimal> decArray = new ArrayList<BigDecimal>();

		for (String s : checked.split(",")) {
			decArray.add(new BigDecimal(s));
			logger.info("activateApplicationConfiguration() - Application configuration to be Activated::" + s);
		}

		boolean flag = processService.appProcessActivate(decArray);

		if (flag) {
			logger.info("END - activateApplicationConfiguration() - Application configuration Activated");
			return RPAConstants.APPLICATION_ACTIVATED;
		}
		logger.error("END - activateApplicationConfiguration() - Unable to process");

		return RPAConstants.INTERNAL_ERROR;
	}

	@GetMapping("/folderConfig")
	public String folderConfig(ModelMap model) {

		List<String> folderConfigurationMasterList = processService.findUniqueProcessList();
		model.addAttribute("processlist", folderConfigurationMasterList);
		model.addAttribute("banklist", processService.getUniqueBankList());

		return "folderConfig";
	}

	@PostMapping("/getBanksByProcessName")
	public @ResponseBody List<FolderConfigurationMaster> getBanksByProcessName(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException {
		List<FolderConfigurationMaster> folderConfigurationMasterList = null;
		logger.info("BEGIN - getBanksByProcessName()");
		String processName = req.getParameter("processName") == null ? "" : req.getParameter("processName");
		folderConfigurationMasterList = processService.findBanksByProcessName(processName);
		logger.info("END - getBanksByProcessName()");
		return folderConfigurationMasterList;
	}

	@PostMapping("/getFileTypesByBankAndProcessName")
	public @ResponseBody List<FolderConfigurationMaster> getFileTypesByBankAndProcessName(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException {
		List<FolderConfigurationMaster> folderConfigurationMasterList = null;
		logger.info("BEGIN - getFileTypesByBankAndProcessName()");
		String processName = req.getParameter("processName") == null ? "" : req.getParameter("processName");
		String custName = req.getParameter("custName") == null ? "" : req.getParameter("custName");
		folderConfigurationMasterList = processService.getFileTypesByBankAndProcessName(processName,custName);
		logger.info("END - getFileTypesByBankAndProcessName()");
		return folderConfigurationMasterList;
	}

	@PostMapping("/getFolderConfigDetails")
	public @ResponseBody List<FolderConfigurationDetails> getFolderConfigDetails() {
		try {
			List<FolderConfigurationDetails> list = processService.getFolderConfigDetails();
			for (FolderConfigurationDetails obj : list) {
				if (obj.getStatus() != null && obj.getStatus().equals(RPAConstants.Y)) {
					obj.setStatus(RPAConstants.Active);
				} else {
					obj.setStatus(RPAConstants.Inactive);
				}
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@PostMapping("/insertFolderConfig")
	public @ResponseBody String insertFolderConfig(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException {

		logger.info("BEGIN - insertFolderConfig()" + req.getQueryString());

		String processName = req.getParameter("processName") == null ? "" : req.getParameter("processName");
		String customerName = req.getParameter("customerName") == null ? "" : req.getParameter("customerName");
		String fileType = req.getParameter("fileType") == null ? "" : req.getParameter("fileType");
		String folderPath = req.getParameter("folderPath") == null ? "" : req.getParameter("folderPath");
		String isRestrcictedFolder = req.getParameter("isRestrcictedFolder") == null ? "" : req.getParameter("isRestrcictedFolder");
		String folderUsername = req.getParameter("folderUsername") == null ? "" : req.getParameter("folderUsername");
		String folderPassword = req.getParameter("folderPassword") == null ? "" : req.getParameter("folderPassword");

		List<FolderConfigurationDetails> duplicateFolderConfig = processService
				.checkFolderConfigDetailsExist(processName,customerName,fileType);
		if (duplicateFolderConfig.size() > 0) {
			logger.info("END - insertFolderConfig() - Duplicate Folder configuration Found :: process name --> "
					+ processName + " , customer name -->" + customerName + " , File Type -->" + fileType);
			return RPAConstants.FOLDER_DUPLICATE_CONFIGURATION;
		}
		FolderConfigurationDetails folderConfigurationDetails = new FolderConfigurationDetails();
		folderConfigurationDetails.setProcessName(processName);
		folderConfigurationDetails.setCustomerName(customerName);
		folderConfigurationDetails.setFileType(fileType);
		folderConfigurationDetails.setFolderPath(folderPath);
		folderConfigurationDetails.setStatus(RPAConstants.Y);
		if(isRestrcictedFolder.equalsIgnoreCase(RPAConstants.Y)){
			folderConfigurationDetails.setUsername(folderUsername);
			folderConfigurationDetails.setPassword(folderPassword);
		}else{
			folderConfigurationDetails.setUsername("");
			folderConfigurationDetails.setPassword("");
		}
		folderConfigurationDetails.setIsRestrictedFolder(isRestrcictedFolder);

		FolderConfigurationDetails savedObj = processService.save(folderConfigurationDetails);
		if (savedObj != null) {
			logger.info(
					"END - insertFolderConfig() - Folder configuration successfuly added for the processor --> "
							+ processName);
			return RPAConstants.FOLDER_ADDED;
		}
		logger.error("END - insertFolderConfig() - unable to add Folder configuration for the processor -->"
				+ processName);
		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/getFolderConfigById")
	public @ResponseBody FolderConfigurationDetails getFolderConfigById(HttpServletRequest req, HttpServletResponse resp) {
		String id = req.getParameter("id") == null ? "" : req.getParameter("id");
		FolderConfigurationDetails folderConfigurationDetails = processService.findByFolderConfigId(Long.valueOf(id));
		return folderConfigurationDetails;
	}

	@PostMapping("/updateFolderConfig")
	public @ResponseBody String updateFolderConfig(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException {

		logger.info("BEGIN - updateFolderConfig()" + req.getQueryString());

		String id = req.getParameter("id") == null ? "" : req.getParameter("id");
		String folderPath = req.getParameter("folderPath") == null ? "" : req.getParameter("folderPath");
		String isRestrcictedFolder = req.getParameter("isRestrcictedFolder") == null ? "" : req.getParameter("isRestrcictedFolder");
		String folderUsername = req.getParameter("folderUsername") == null ? "" : req.getParameter("folderUsername");
		String folderPassword = req.getParameter("folderPassword") == null ? "" : req.getParameter("folderPassword");

		FolderConfigurationDetails folderConfigurationDetails = processService.findByFolderConfigId(Long.valueOf(id));
		folderConfigurationDetails.setFolderPath(folderPath);

		if(isRestrcictedFolder.equalsIgnoreCase(RPAConstants.Y)){
			folderConfigurationDetails.setUsername(folderUsername);
			folderConfigurationDetails.setPassword(folderPassword);
		}else{
			folderConfigurationDetails.setUsername("");
			folderConfigurationDetails.setPassword("");
		}
		folderConfigurationDetails.setIsRestrictedFolder(isRestrcictedFolder);

		FolderConfigurationDetails savedObj = processService.save(folderConfigurationDetails);
		if (savedObj != null) {
			logger.info(
					"END - updateFolderConfig() - Folder configuration successfuly updated for the id --> "
							+ id);
			return RPAConstants.FOLDER_UPDATED;
		}
		logger.error("END - updateFolderConfig() - unable to update Folder configuration for the processor -->"
				+ id);
		return RPAConstants.INTERNAL_ERROR;
	}


	@PostMapping("/deleteFolderConfig")
	public @ResponseBody String deleteFolderConfig(HttpServletRequest req, HttpServletResponse resp)
			throws ParseException {

		logger.info("BEGIN - deleteFolderConfig()");

		String checked = req.getParameter("checked") == null ? "" : req.getParameter("checked");

		ArrayList<BigDecimal> decArray = new ArrayList<BigDecimal>();

		for (String s : checked.split(",")) {
			decArray.add(new BigDecimal(s));
			logger.info("deleteFolderConfig() - Folder Configuration to be Deleted::" + s);
		}

		boolean flag = processService.deleteFolderConfig(decArray);

		if (flag) {
			logger.info("END - deleteFolderConfig() - Folder Configuration Deleted.");
			return RPAConstants.FOLDER_DELETED;
		}
		logger.error("END - deleteFolderConfig() - Unable to process.");

		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/activateFolderConfig")
	public @ResponseBody String activateFolderConfig(HttpServletRequest req, HttpServletResponse resp)
			throws ParseException {

		logger.info("BEGIN - activateFolderConfig()");

		String checked = req.getParameter("checked") == null ? "" : req.getParameter("checked");

		ArrayList<BigDecimal> decArray = new ArrayList<BigDecimal>();

		for( String s : checked.split(",") ){
			decArray.add( new BigDecimal(s) );
			logger.info("activateFolderConfig() - Folder Configuration ID's to be Activated::"+s);
		}

		boolean flag = processService.activateFolderConfig(decArray);

		if(flag){
			logger.info("END - activateFolderConfig() - Folder Configuration Activated.");
			return RPAConstants.FOLDER_ACTIVATED;
		}
		logger.error("END - activateFolderConfig() - Unable to process.");

		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/getMarutiPolicyDateList")
	public @ResponseBody List<String> getMarutiPolicyDateList(HttpServletRequest req,
			HttpServletResponse resp) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {

		String statusFlag = req.getParameter("statusFlag") == null ? "" : req.getParameter("statusFlag");
		String monthYear = req.getParameter("monthYear") == null ? "" : req.getParameter("monthYear");

		List<String> dateList = marutiPolicyService.getMarutiPolicyDateList(monthYear,statusFlag);


		return dateList;
	}

	@PostMapping("/getMarutiDailyStatus")
	public @ResponseBody CarDailyStatusResponse getMarutiDailyStatus(HttpServletRequest req,
			HttpServletResponse resp) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		CarDailyStatusResponse carDailyStatusResponse = null;
		String policyDate = req.getParameter("policyDate") == null ? "" : req.getParameter("policyDate");
		String carType = req.getParameter("carType") == null ? "" : req.getParameter("carType");

		if(carType.equalsIgnoreCase("MAR")){
			carDailyStatusResponse = marutiPolicyService.getMarutiDailyStatus(policyDate);
		}else if(carType.equalsIgnoreCase("HON")){
			carDailyStatusResponse = hondaPolicyService.getMarutiDailyStatus(policyDate);
		}else if(carType.equalsIgnoreCase("FRD")){
			carDailyStatusResponse = fordPolicyService.getMarutiDailyStatus(policyDate);
		}else if(carType.equalsIgnoreCase("TAT")){
			carDailyStatusResponse = fordPolicyService.getMarutiDailyStatus(policyDate);
		}else if(carType.equalsIgnoreCase("ABL")){
			carDailyStatusResponse = fordPolicyService.getMarutiDailyStatus(policyDate);
		}else if(carType.equalsIgnoreCase("MBL")){
			carDailyStatusResponse = miblPolicyService.getMarutiDailyStatus(policyDate);
		}else if(carType.equalsIgnoreCase("VOL")){
			carDailyStatusResponse = volvoPolicyService.getMarutiDailyStatus(policyDate);
		}else if(carType.equalsIgnoreCase("TAF")){
			carDailyStatusResponse = tafePolicyService.getMarutiDailyStatus(policyDate);
		}else if(carType.equalsIgnoreCase("PIA")){
			carDailyStatusResponse = piaggioPolicyService.getMarutiDailyStatus(policyDate);
		}else if(carType.equalsIgnoreCase("FGN")){
			carDailyStatusResponse = firstGenDownloadPolicyService.getMarutiDailyStatus(policyDate);
		}
		return carDailyStatusResponse;
	}

	@PostMapping("/getMarutiMonthlyStatus")
	public @ResponseBody CarDailyStatusResponse getMarutiMonthlyStatus(HttpServletRequest req,
			HttpServletResponse resp) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		CarDailyStatusResponse carDailyStatusResponse = null;
		String policyMonth = req.getParameter("policyMonth") == null ? "" : req.getParameter("policyMonth");
		String policyYear = req.getParameter("policyYear") == null ? "" : req.getParameter("policyYear");
		String carType = req.getParameter("carType") == null ? "" : req.getParameter("carType");

		if(carType.equalsIgnoreCase("MAR")){
			carDailyStatusResponse = marutiPolicyService.getMarutiMonthlyStatus(policyMonth,policyYear);
		}else if(carType.equalsIgnoreCase("HON")){
			carDailyStatusResponse = hondaPolicyService.getMarutiMonthlyStatus(policyMonth,policyYear);
		}else if(carType.equalsIgnoreCase("FRD")){
			carDailyStatusResponse = fordPolicyService.getMarutiMonthlyStatus(policyMonth,policyYear);
		}else if(carType.equalsIgnoreCase("TAT")){
			carDailyStatusResponse = tataPolicyService.getMarutiMonthlyStatus(policyMonth,policyYear);
		}else if(carType.equalsIgnoreCase("ABL")){
			carDailyStatusResponse = abiblPolicyService.getMarutiMonthlyStatus(policyMonth,policyYear);
		}else if(carType.equalsIgnoreCase("MBL")){
			carDailyStatusResponse = miblPolicyService.getMarutiMonthlyStatus(policyMonth,policyYear);
		}else if(carType.equalsIgnoreCase("VOL")){
			carDailyStatusResponse = volvoPolicyService.getMarutiMonthlyStatus(policyMonth,policyYear);
		}else if(carType.equalsIgnoreCase("TAF")){
			carDailyStatusResponse = tafePolicyService.getMarutiMonthlyStatus(policyMonth,policyYear);
		}else if(carType.equalsIgnoreCase("PIA")){
			carDailyStatusResponse = piaggioPolicyService.getMarutiMonthlyStatus(policyMonth,policyYear);
		}else if(carType.equalsIgnoreCase("FGN")){
			carDailyStatusResponse = firstGenDownloadPolicyService.getMarutiMonthlyStatus(policyMonth,policyYear);
		}

		return carDailyStatusResponse;
	}

	@PostMapping("/getMarutiYearlyStatus")
	public @ResponseBody CarDailyStatusResponse getMarutiYearlyStatus(HttpServletRequest req,
			HttpServletResponse resp) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		CarDailyStatusResponse carDailyStatusResponse = null;
		String policyYear = req.getParameter("policyYear") == null ? "" : req.getParameter("policyYear");
		String carType = req.getParameter("carType") == null ? "" : req.getParameter("carType");

		if(carType.equalsIgnoreCase("MAR")){
			carDailyStatusResponse = marutiPolicyService.getMarutiYearlyStatus(policyYear);
		}else if(carType.equalsIgnoreCase("HON")){
			carDailyStatusResponse = hondaPolicyService.getMarutiYearlyStatus(policyYear);
		}else if(carType.equalsIgnoreCase("FRD")){
			carDailyStatusResponse = fordPolicyService.getMarutiYearlyStatus(policyYear);
		}else if(carType.equalsIgnoreCase("TAT")){
			carDailyStatusResponse = tataPolicyService.getMarutiYearlyStatus(policyYear);
		}else if(carType.equalsIgnoreCase("ABL")){
			carDailyStatusResponse = abiblPolicyService.getMarutiYearlyStatus(policyYear);
		}else if(carType.equalsIgnoreCase("MBL")){
			carDailyStatusResponse =miblPolicyService.getMarutiYearlyStatus(policyYear);
		}else if(carType.equalsIgnoreCase("VOL")){
			carDailyStatusResponse =volvoPolicyService.getMarutiYearlyStatus(policyYear);
		}else if(carType.equalsIgnoreCase("TAF")){
			carDailyStatusResponse =tafePolicyService.getMarutiYearlyStatus(policyYear);
		}else if(carType.equalsIgnoreCase("PIA")){
			carDailyStatusResponse =piaggioPolicyService.getMarutiYearlyStatus(policyYear);
		}else if(carType.equalsIgnoreCase("FGN")){
			carDailyStatusResponse = firstGenDownloadPolicyService.getMarutiYearlyStatus(policyYear);
		}

		return carDailyStatusResponse;
	}

	@GetMapping("/policyExtraction")
	public String policyExtraction(ModelMap model) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		model.addAttribute("carTypeList", marutiPolicyService.getCarTypeList());
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
			model.addAttribute("marutiDmsCheckHostDetails",UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_POLICY_DMSCHECK_LIVE_HOST_DETAILS));
		}else{
			model.addAttribute("marutiDmsCheckHostDetails",UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_POLICY_DMSCHECK_HOST_DETAILS));
		}

		return "policyExtraction";
	}

	@PostMapping("/getCarPolicyExtractionDetails")
	public @ResponseBody List<MarutiPolicy> getCarPolicyExtractionDetails(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		List<MarutiPolicy> carPolicyDetailsList = null;
		logger.info("BEGIN - getCarPolicyExtractionDetails()");
		String startDate = req.getParameter("startDate") == null ? "" : req.getParameter("startDate");
		String endDate = req.getParameter("endDate") == null ? "" : req.getParameter("endDate");
		String carType = req.getParameter("carType") == null ? "" : req.getParameter("carType");
		carPolicyDetailsList = marutiPolicyService.getCarPolicyExtractionDetails(startDate,endDate,carType);
		logger.info("END - getCarPolicyExtractionDetails()");
		return carPolicyDetailsList;
	}

	@PostMapping("/getTransactionExceptionLog")
	public @ResponseBody String getTransactionExceptionLog(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException {
		String transactionId = req.getParameter("id") == null ? "" : req.getParameter("id");
		return errorInfoService.getTransactionExceptionLog(transactionId);
	}
	@GetMapping("/getGridFilesList")
	public @ResponseBody List<GridAutomationModel> getGridFilesList(HttpServletRequest req, HttpServletResponse resp)
	{
		String transactionId = req.getParameter("transactionId") == null ? "" : req.getParameter("transactionId");
		String gridId = req.getParameter("gridId") == null ? "" : req.getParameter("gridId");
		List<GridAutomationModel> gridList = req.getParameter("gridId") ==  ""?gridAutomationService.findByTransactionId(Long.parseLong(transactionId)):
			gridAutomationService.findByGridId(Long.parseLong(transactionId),Long.parseLong(gridId));


		return gridList;
	}

	@GetMapping(value = "/commonDownloadMethod")
	public void commonDownloadMethod(HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws ParseException {

		String downloadFilePath = request.getParameter("downloadFilePath") == null ? ""
				: request.getParameter("downloadFilePath");
		downloadFilePath = downloadFilePath.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
		String contentType = request.getParameter("contentType") == null ? "" : request.getParameter("contentType");

		try {
			downloadFilePath = URLDecoder.decode(downloadFilePath, "UTF-8");
			File file = new File(downloadFilePath);

			if (!file.exists()) {
				throw new Exception("No Files Exist");
			} else {

				FileInputStream inputStream = new FileInputStream(file);
				String ext = FilenameUtils.getExtension(file.getName());
				String fileName=file.getName().indexOf("__")==-1?file.getName():file.getName().split("__")[0].concat("." + ext);

				response.setHeader("Content-Disposition", "inline; filename=\"" +fileName+ "\"");
				response.setContentType(contentType);

				ServletOutputStream outputStream = response.getOutputStream();
				IOUtils.copy(inputStream, outputStream);

				outputStream.close();
				inputStream.close();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@GetMapping("/getGridSheetCountList")
	public @ResponseBody void getGridSheetCountList(HttpServletRequest req, HttpServletResponse response) throws IOException
	{
		String transactionId = req.getParameter("transactionId") == null ? "" : req.getParameter("transactionId");
		String gridId = req.getParameter("gridId") == null ? "" : req.getParameter("gridId");
		String fileName = req.getParameter("fileName") == null ? "" : req.getParameter("fileName");
		String contentType = req.getParameter("contentType") == null ? "" : req.getParameter("contentType");
		List<GridFilesCountDetails> gridFilesCountList =gridAutomationService.getFilesCountDetails(Long.parseLong(transactionId),Long.parseLong(gridId));


		if(!gridFilesCountList.isEmpty())
		{
			int i=0;
			Workbook xlsWb = new HSSFWorkbook();
			Sheet xlsSheet = xlsWb.createSheet("CountSheet");
			Row xlsRow = xlsSheet.createRow(i);
			Cell xlsCell = xlsRow.createCell(i); 	
			xlsCell.setCellValue("SheetName");
			xlsCell = xlsRow.createCell(i+1); 
			xlsCell.setCellValue("SheetCount");
			xlsCell = xlsRow.createCell(i+2);
			xlsCell.setCellValue("TotalCount");
			i=1;	
			Integer totalValue = 0;
			for(GridFilesCountDetails  grid    :gridFilesCountList)
			{


				xlsRow = xlsSheet.createRow(i++);
				for(int j=0;j<1;j++)
				{
					xlsCell = xlsRow.createCell(j); 	
					xlsCell.setCellValue(grid.getSheetsName());
					xlsCell = xlsRow.createCell(j+1); 
					xlsCell.setCellValue(grid.getSheetsCount().split("=")[0]);
					xlsCell = xlsRow.createCell(j+2); 
					xlsCell.setCellValue(Integer.valueOf(grid.getSheetsCount().split("=")[1]));
					totalValue += Integer.valueOf(grid.getSheetsCount().split("=")[1]);
				}


			}
			xlsRow = xlsSheet.createRow(xlsSheet.getLastRowNum()+1);
			xlsCell = xlsRow.createCell(i); 	
			xlsCell.setCellValue("TotalRecord");
			xlsCell = xlsRow.createCell(i+1); 
			/*xlsCell.setCellValue("SheetCount");*/
			xlsCell.setCellValue(totalValue);



			fileName = URLDecoder.decode(fileName, "UTF-8");	
			String ext = FilenameUtils.getExtension(fileName);
			fileName=fileName.replace("." + ext, "").trim().concat("_Count." + RPAConstants.FILE_XLS_EXTENSION);
			response.setHeader("Content-Disposition", "inline; filename=\"" +fileName+ "\"");
			response.setContentType(contentType);

			ServletOutputStream outputStream = response.getOutputStream();
			xlsWb=UtilityFile.autoSizeColumns(xlsWb);

			xlsWb.write(outputStream);
			xlsWb.close();

		}
		else
		{


		}

	}
	@PostMapping("/getUploadReference")
	public @ResponseBody MarutiUploadReferenceResponse getUploadReference(HttpServletRequest req,
			HttpServletResponse resp) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {

		String policyNo = req.getParameter("id") == null ? "" : req.getParameter("id");

		String flag = req.getParameter("flag") == null ? "" : req.getParameter("flag");

		MarutiUploadReferenceResponse carDailyStatusResponse = marutiPolicyService.getUploadReference(policyNo,flag);

		return carDailyStatusResponse;
	}

	@PostMapping("/getclaimsCurrentStatus")
	public @ResponseBody ClaimsDownloadResponse getclaimsCurrentStatus(HttpServletRequest req,
			HttpServletResponse resp) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {

		ClaimsDownloadResponse claimsDownloadResponse = omniDocsClaimsDocDownloaderService.getMarutiDailyStatus();

		return claimsDownloadResponse;
	}

	@GetMapping("/getModelGridFilesList")
	public @ResponseBody List<ModelCodeCreation> getModelGridFilesList(HttpServletRequest req, HttpServletResponse resp)
	{
		String transactionId = req.getParameter("transactionId") == null ? "" : req.getParameter("transactionId");
		String gridId = req.getParameter("gridId") == null ? "" : req.getParameter("gridId");
		List<ModelCodeCreation> gridList = req.getParameter("gridId") ==  ""?modelCodeCreationService.findByTransactionId(Long.parseLong(transactionId)):
			modelCodeCreationService.findByModelCreationId(Long.parseLong(transactionId),Long.parseLong(gridId));
		return gridList;
	}

	@PostMapping("/getHondaPolicyExtractionDetails")
	public @ResponseBody List<HondaPolicy> getHondaPolicyExtractionDetails(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException {
		List<HondaPolicy> carPolicyDetailsList = null;
		logger.info("BEGIN - getCarPolicyExtractionDetails()");
		String startDate = req.getParameter("startDate") == null ? "" : req.getParameter("startDate");
		String endDate = req.getParameter("endDate") == null ? "" : req.getParameter("endDate");
		String carType = req.getParameter("carType") == null ? "" : req.getParameter("carType");
		carPolicyDetailsList = hondaPolicyService.getCarPolicyExtractionDetails(startDate,endDate,carType);
		logger.info("END - getCarPolicyExtractionDetails()");
		return carPolicyDetailsList;
	}

	@PostMapping("/getFordPolicyExtractionDetails")
	public @ResponseBody List<FordPolicy> getFordPolicyExtractionDetails(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException {
		List<FordPolicy> carPolicyDetailsList = null;
		logger.info("BEGIN - getCarPolicyExtractionDetails()");
		String startDate = req.getParameter("startDate") == null ? "" : req.getParameter("startDate");
		String endDate = req.getParameter("endDate") == null ? "" : req.getParameter("endDate");
		String carType = req.getParameter("carType") == null ? "" : req.getParameter("carType");
		carPolicyDetailsList = fordPolicyService.getCarPolicyExtractionDetails(startDate,endDate,carType);
		logger.info("END - getCarPolicyExtractionDetails()");
		return carPolicyDetailsList;
	}

	@PostMapping("/getTATAPolicyExtractionDetails")
	public @ResponseBody List<TataPolicy> getTATAPolicyExtractionDetails(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException {
		List<TataPolicy> carPolicyDetailsList = null;
		logger.info("BEGIN - getTATAPolicyExtractionDetails()");
		String startDate = req.getParameter("startDate") == null ? "" : req.getParameter("startDate");
		String endDate = req.getParameter("endDate") == null ? "" : req.getParameter("endDate");
		String carType = req.getParameter("carType") == null ? "" : req.getParameter("carType");
		carPolicyDetailsList = tataPolicyService.getCarPolicyExtractionDetails(startDate,endDate,carType);
		logger.info("END - getTATAPolicyExtractionDetails()");
		return carPolicyDetailsList;
	}

	@PostMapping("/getABIBLPolicyExtractionDetails")
	public @ResponseBody List<AbiblPolicy> getABIBLPolicyExtractionDetails(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException {
		List<AbiblPolicy> carPolicyDetailsList = null;
		logger.info("BEGIN - getABIBLPolicyExtractionDetails()");
		String startDate = req.getParameter("startDate") == null ? "" : req.getParameter("startDate");
		String endDate = req.getParameter("endDate") == null ? "" : req.getParameter("endDate");
		String carType = req.getParameter("carType") == null ? "" : req.getParameter("carType");
		carPolicyDetailsList = abiblPolicyService.getCarPolicyExtractionDetails(startDate,endDate,carType);
		logger.info("END - getCarPolicyExtractionDetails()");
		return carPolicyDetailsList;
	}

	@PostMapping("/getMIBLPolicyExtractionDetails")
	public @ResponseBody List<MiblPolicy> getMIBLPolicyExtractionDetails(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException {
		List<MiblPolicy> carPolicyDetailsList = null;
		logger.info("BEGIN - getMIBLPolicyExtractionDetails()");
		String startDate = req.getParameter("startDate") == null ? "" : req.getParameter("startDate");
		String endDate = req.getParameter("endDate") == null ? "" : req.getParameter("endDate");
		String carType = req.getParameter("carType") == null ? "" : req.getParameter("carType");
		carPolicyDetailsList = miblPolicyService.getCarPolicyExtractionDetails(startDate,endDate,carType);
		logger.info("END - getCarPolicyExtractionDetails()");
		return carPolicyDetailsList;
	}

	@PostMapping("/getVOLVOPolicyExtractionDetails")
	public @ResponseBody List<VolvoPolicy> getVOLVOPolicyExtractionDetails(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException {
		List<VolvoPolicy> carPolicyDetailsList = null;
		logger.info("BEGIN - getVOLVOPolicyExtractionDetails()");
		String startDate = req.getParameter("startDate") == null ? "" : req.getParameter("startDate");
		String endDate = req.getParameter("endDate") == null ? "" : req.getParameter("endDate");
		String carType = req.getParameter("carType") == null ? "" : req.getParameter("carType");
		carPolicyDetailsList = volvoPolicyService.getCarPolicyExtractionDetails(startDate,endDate,carType);
		logger.info("END - getVOLVOPolicyExtractionDetails()");
		return carPolicyDetailsList;
	}

	@PostMapping("/getTAFEPolicyExtractionDetails")
	public @ResponseBody List<TafePolicy> getTAFEPolicyExtractionDetails(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException {
		List<TafePolicy> carPolicyDetailsList = null;
		logger.info("BEGIN - getVOLVOPolicyExtractionDetails()");
		String startDate = req.getParameter("startDate") == null ? "" : req.getParameter("startDate");
		String endDate = req.getParameter("endDate") == null ? "" : req.getParameter("endDate");
		String carType = req.getParameter("carType") == null ? "" : req.getParameter("carType");
		carPolicyDetailsList = tafePolicyService.getCarPolicyExtractionDetails(startDate,endDate,carType);
		logger.info("END - getVOLVOPolicyExtractionDetails()");
		return carPolicyDetailsList;
	}

	@PostMapping("/getPIAGGIOPolicyExtractionDetails")
	public @ResponseBody List<PiaggioPolicy> getPIAGGIOPolicyExtractionDetails(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException {
		List<PiaggioPolicy> carPolicyDetailsList = null;
		logger.info("BEGIN - getVOLVOPolicyExtractionDetails()");
		String startDate = req.getParameter("startDate") == null ? "" : req.getParameter("startDate");
		String endDate = req.getParameter("endDate") == null ? "" : req.getParameter("endDate");
		String carType = req.getParameter("carType") == null ? "" : req.getParameter("carType");
		carPolicyDetailsList = piaggioPolicyService.getCarPolicyExtractionDetails(startDate,endDate,carType);
		logger.info("END - getVOLVOPolicyExtractionDetails()");
		return carPolicyDetailsList;
	}

	@PostMapping("/getFirstgenPolicyExtractionDetails")
	public @ResponseBody List<FirstgenPolicy> getFirstgenPolicyExtractionDetails(HttpServletRequest req,
			HttpServletResponse resp) throws ParseException {
		List<FirstgenPolicy> carPolicyDetailsList = null;
		logger.info("BEGIN - getFirstgenPolicyExtractionDetails()");
		String startDate = req.getParameter("startDate") == null ? "" : req.getParameter("startDate");
		String endDate = req.getParameter("endDate") == null ? "" : req.getParameter("endDate");
		String carType = req.getParameter("carType") == null ? "" : req.getParameter("carType");
		carPolicyDetailsList = firstGenDownloadPolicyService.getCarPolicyExtractionDetails(startDate,endDate,carType);
		logger.info("END - getFirstgenPolicyExtractionDetails()");
		return carPolicyDetailsList;
	}

	@GetMapping("/firstgenPolicyExtraction")
	public String firstgenPolicyExtraction(ModelMap model) {
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
			model.addAttribute("marutiDmsCheckHostDetails",UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_POLICY_DMSCHECK_LIVE_HOST_DETAILS));
		}else{
			model.addAttribute("marutiDmsCheckHostDetails",UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_POLICY_DMSCHECK_HOST_DETAILS));
		}

		return "firstgenPolicyExtraction";
	}

	@PostMapping(value = "/getCarStatusChartDetails")
	public @ResponseBody ChartMultiData getCarStatusChartDetails(HttpServletRequest req,
			HttpServletResponse resp) {
		String year = req.getParameter("year") == null ? "" : req.getParameter("year");
		ChartMultiData chartData = dashboardService.carPolicyChartStatus(year);
		return chartData;
	}

	@PostMapping(value = "/getxgenStatusChartDetails")
	public @ResponseBody ChartMultiData getxgenStatusChartDetails(HttpServletRequest req,
			HttpServletResponse resp) {
		String year = req.getParameter("year") == null ? "" : req.getParameter("year");
		ChartMultiData chartData = dashboardService.xgenPolicyChartStatus(year);
		return chartData;
	}


	@PostMapping(value = "/getxgenGLStatusChartDetails")
	public @ResponseBody ChartMultiData getxgenGLStatusChartDetails(HttpServletRequest req,
			HttpServletResponse resp) {
		String year = req.getParameter("year") == null ? "" : req.getParameter("year");
		ChartMultiData chartData = dashboardService.getxgenGLStatusChartDetails(year);
		return chartData;
	}

	@PostMapping(value = "/getPolicyMailRetriggerStatusChartDetails")
	public @ResponseBody ChartMultiData getPolicyMailRetriggerStatusChartDetails(HttpServletRequest req,
			HttpServletResponse resp) {
		String year = req.getParameter("year") == null ? "" : req.getParameter("year");
		ChartMultiData chartData = dashboardService.getPolicyMailRetriggerStatusChartDetails(year);
		return chartData;
	}

	@PostMapping(value = "/getGridAutomationStatusChartDetails")
	public @ResponseBody ChartMultiData getGridAutomationStatusChartDetails(HttpServletRequest req,
			HttpServletResponse resp) {
		String year = req.getParameter("year") == null ? "" : req.getParameter("year");
		ChartMultiData chartData = dashboardService.gridAutomationChartStatus(year);
		return chartData;
	}


	@GetMapping("/getGridModelFilesList")
	public @ResponseBody List<GridWithModelSheetAutomationModel> getGridModelFilesList(HttpServletRequest req, HttpServletResponse resp)
	{
		String transactionId = req.getParameter("transactionId") == null ? "" : req.getParameter("transactionId");
		String gridId = req.getParameter("gridId") == null ? "" : req.getParameter("gridId");
		List<GridWithModelSheetAutomationModel> gridList = req.getParameter("gridId") ==  ""?gridWithModelSheetAutomationService.findByTransactionId(Long.parseLong(transactionId)):
			gridWithModelSheetAutomationService.findByGridId(Long.parseLong(transactionId),Long.parseLong(gridId));


		return gridList;
	}



	@GetMapping("/getInsertedGridWithModelRowCount")
	public @ResponseBody List<GridModelRowStatusResponse> getInsertedGridWithModelRowCount(HttpServletRequest req, HttpServletResponse resp) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException
	{
		String gridId = req.getParameter("gridId") == null ? "" : req.getParameter("gridId");
		String flag = req.getParameter("flag") == null ? "" : req.getParameter("flag");
		List<GridModelRowStatusResponse> insertedRowLists = gridWithModelSheetAutomationService.getInsertedGridWithModelRowCount(gridId,flag);



		return insertedRowLists;
	}

	@GetMapping("/processConfig")
	public String getProcessDetailsPage(ModelMap model) {
		//model.addAttribute("processlist", commonService.getbussinessProcess());
		return "processConfig";
	}

	@PostMapping("/getProcessConfigurationDetails")
	public @ResponseBody List<BusinessProcess> getProcessConfigurationDetails() {
		try {
			List<BusinessProcess> list = commonService.getbussinessProcess();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}


	@PostMapping("/insertProcessConfig")
	public @ResponseBody String insertProcessConfig(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException {

		logger.info("BEGIN - insertProcessConfig()" + req.getQueryString());

		//String processId = req.getParameter("processId") == null ? "" : req.getParameter("processId");
		String processName = req.getParameter("processName") == null ? "" : req.getParameter("processName");
		String processDesc = req.getParameter("processDesc") == null ? "" : req.getParameter("processDesc");

		BusinessProcess businessProcess = new BusinessProcess();

		/*businessProcess.setId(Long.valueOf(processId));*/
		businessProcess.setProcessDesc(processDesc);
		businessProcess.setProcessName(processName);
		businessProcess.setProcessState(false);

		BusinessProcess savedObj = commonService.saveBusinessProcess(businessProcess);
		if (savedObj != null) {
			logger.info(
					"END - insertProcessConfig() - Process configuration successfuly added for the processor --> "
							+ processName);
			return RPAConstants.PROCESS_ADDED;
		}
		logger.error("END - insertProcessConfig() - unable to add PROCESS for the processor -->"
				+ processName);
		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/updateProcessConfig")
	public @ResponseBody String updateProcessConfig(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException {

		logger.info("BEGIN - updateProcessConfig()" + req.getQueryString());

		String processId = req.getParameter("processId") == null ? "" : req.getParameter("processId");
		String processName = req.getParameter("processName") == null ? "" : req.getParameter("processName");
		String processDesc = req.getParameter("processDesc") == null ? "" : req.getParameter("processDesc");

		BusinessProcess businessProcess = commonService.findBussinessProcessById(Long.valueOf(processId));
		businessProcess.setProcessDesc(processDesc);
		businessProcess.setProcessName(processName);

		BusinessProcess savedObj = commonService.saveBusinessProcess(businessProcess);
		if (savedObj != null) {
			logger.info(
					"END - updateProcessConfig() - Process successfuly updated for the processor --> "
							+ processName);
			return RPAConstants.PROCESS_UPDATED;
		}
		logger.error(
				"END - updateProcessConfig() - unable to update Process for the processor -->"
						+ processName);
		return RPAConstants.INTERNAL_ERROR;
	}


	@PostMapping("/processConfigDelete")
	public @ResponseBody String processConfigDelete(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {

		logger.info("BEGIN - processConfigDelete()" + req.getQueryString());

		String processId = req.getParameter("processId") == null ? "" : req.getParameter("processId");

		if(commonService.processConfigDelete(processId)){
			logger.info(
					"END - processConfigDelete() -  Process configuration successfuly deleted for the process --> "
							+ processId);
			return RPAConstants.PROCESS_DELETED;
		}else{
			logger.error("END - processConfigDelete() - unable to delete user proces for the user -->"
					+ processId);
			return RPAConstants.INTERNAL_ERROR;
		}
	}

	@GetMapping("/paramConfig")
	public String getParamConfigDetailsPage(ModelMap model) {
		return "paramConfig";
	}

	@PostMapping("/getParamConfigDetails")
	public @ResponseBody List<ParamConfigResponse> getParamConfigDetails() {
		try {
			List<ParamConfigResponse> list = commonService.getParamConfigDetails();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}


	@PostMapping("/insertParamConfig")
	public @ResponseBody String insertParamConfig(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {

		logger.info("BEGIN - insertParamConfig()" + req.getQueryString());

		String paramKey = req.getParameter("paramKey") == null ? "" : req.getParameter("paramKey");
		String paramValue = req.getParameter("paramValue") == null ? "" : req.getParameter("paramValue");

		if(commonService.insertParamConfig(paramKey,paramValue)){
			logger.info(
					"END - insertParamConfig() - param configuration successfuly added for the param key --> "
							+ paramKey);
			return RPAConstants.PARAM_ADDED;
		}else{
			logger.error("END - insertParamConfig() - unable to add param for the param key -->"
					+ paramKey);
			return RPAConstants.INTERNAL_ERROR;
		}

	}

	@PostMapping("/paramConfigDelete")
	public @ResponseBody String deleteParamConfig(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {

		logger.info("BEGIN - deleteParamConfig()" + req.getQueryString());

		String paramKey = req.getParameter("paramKey") == null ? "" : req.getParameter("paramKey");
		String paramValue = req.getParameter("paramValue") == null ? "" : req.getParameter("paramValue");

		if(commonService.deleteParamConfig(paramKey,paramValue)){
			logger.info(
					"END - deleteParamConfig() - param configuration successfuly deleted for the param key --> "
							+ paramKey);
			return RPAConstants.PARAM_DELETED;
		}else{
			logger.error("END - deleteParamConfig() - unable to delete param for the user param key -->"
					+ paramKey);
			return RPAConstants.INTERNAL_ERROR;
		}
	}




	@GetMapping("/userProcess")
	public String getUserProcessDetailsPage(ModelMap model) {
		//model.addAttribute("processlist", commonService.getbussinessProcess());
		return "userProcess";
	}

	@PostMapping("/getUserProcessDetails")
	public @ResponseBody List<UserProcessResponse> getUserProcessDetails() {
		try {
			List<UserProcessResponse> list = commonService.getUserProcessConfigurationDetails();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}


	@PostMapping("/insertUserProcessConfig")
	public @ResponseBody String insertUserProcessConfig(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {

		logger.info("BEGIN - insertUserProcessConfig()" + req.getQueryString());

		String userId = req.getParameter("userId") == null ? "" : req.getParameter("userId");
		String processId = req.getParameter("processId") == null ? "" : req.getParameter("processId");

		if(commonService.insertUserProcessConfig(userId,processId)){
			logger.info(
					"END - insertUserProcessConfig() - user Process configuration successfuly added for the user --> "
							+ userId);
			return RPAConstants.USERPROCESS_ADDED;
		}else{
			logger.error("END - insertUserProcessConfig() - unable to add user proces for the user -->"
					+ userId);
			return RPAConstants.INTERNAL_ERROR;
		}

	}

	@PostMapping("/userProcessConfigDelete")
	public @ResponseBody String deleteUserProcessConfig(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {

		logger.info("BEGIN - deleteUserProcessConfig()" + req.getQueryString());

		String userId = req.getParameter("userId") == null ? "" : req.getParameter("userId");
		String processId = req.getParameter("processId") == null ? "" : req.getParameter("processId");

		if(commonService.deleteUserProcessConfig(userId,processId)){
			logger.info(
					"END - deleteUserProcessConfig() - user Process configuration successfuly deleted for the user --> "
							+ userId);
			return RPAConstants.USERPROCESS_DELETED;
		}else{
			logger.error("END - deleteUserProcessConfig() - unable to delete user proces for the user -->"
					+ userId);
			return RPAConstants.INTERNAL_ERROR;
		}
	}

	@PostMapping("/paramConfigUpdate")
	public @ResponseBody String paramConfigUpdate(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {

		logger.info("BEGIN - paramConfigUpdate()" + req.getQueryString());

		String paramKey = req.getParameter("paramKey") == null ? "" : req.getParameter("paramKey");
		String paramValue = req.getParameter("paramValue") == null ? "" : req.getParameter("paramValue");

		if(commonService.paramConfigUpdate(paramKey,paramValue)){
			logger.info(
					"END - paramConfigUpdate() - param configuration successfuly deleted for the param key --> "
							+ paramKey);
			return RPAConstants.PARAM_UPDATED;
		}else{
			logger.error("END - paramConfigUpdate() - unable to delete param for the user param key -->"
					+ paramKey);
			return RPAConstants.INTERNAL_ERROR;
		}
	}


	@PostMapping("/getPdfMailTriggerStatus")
	public @ResponseBody PolicyPdfMailRetrigger getPdfMailTriggerStatus(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) {
		String id = req.getParameter("id") == null ? "" : req.getParameter("id");
		PolicyPdfMailRetrigger policyPdfMailRetrigger = policyPdfMailRetriggerRepository.getPdfMailTriggerStatus(Long.parseLong(id));

		return policyPdfMailRetrigger;
	}

	@PostMapping("/getIncentiveReportDetails")
	public @ResponseBody List<AgentResponse> getIncentiveReportDetails(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) {
		String year = req.getParameter("year") == null ? "" : req.getParameter("year");
		String month = req.getParameter("month") == null ? "" : req.getParameter("month");
		String type = req.getParameter("type") == null ? "" : req.getParameter("type");
		List<AgentResponse> agentResponse = agentIncentiveService.getIncentiveReportDetails(year,month,type);

		return agentResponse;
	}

	@GetMapping("/agentIncentiveCalculation")
	public String getAgentIncentiveCalculationPage(ModelMap model) {
		return "AgentIncentiveCalculation";
	}

	@PostMapping("/getCalcalculationCompletionStatus")
	public @ResponseBody List<String> getCalcalculationCompletionStatus(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) {
		String year = req.getParameter("year") == null ? "" : req.getParameter("year");
		List<String> monthYear = agentIncentiveService.getCalcalculationCompletionStatus(year);
		return monthYear;
	}

	@PostMapping("/getAgentIncentiveStatus")
	public @ResponseBody AgentIncentiveModel getAgentIncentiveStatus(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) {
		String id = req.getParameter("id") == null ? "" : req.getParameter("id");
		AgentIncentiveModel agentIncentiveModel = agentIncentiveService.getAgentIncentiveStatus(Long.parseLong(id));

		return agentIncentiveModel;
	}


	@GetMapping("/AgentMasters")
	public String getAgentMasters(ModelMap model) {
		return "AgentMasters";
	}

	@PostMapping("/getPerPolicyDetails")
	public @ResponseBody List<AgentConfig> getPerPolicyDetails(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) {
		List<AgentConfig> agentConfig = agentIncentiveService.getPerPolicyDetails();

		return agentConfig;
	}

	@PostMapping("/getInHouseLeads")
	public @ResponseBody List<InHouseLeads> getInHouseLeads(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) {
		List<InHouseLeads> inHouseLeads = agentIncentiveService.getInHouseLeads();

		return inHouseLeads;
	}

	@PostMapping("/getAgentSlab")
	public @ResponseBody List<AgentSlabMaster> getAgentSlab(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) {
		List<AgentSlabMaster> agentSlabMaster = agentIncentiveService.getAgentSlab();

		return agentSlabMaster;
	}

	@PostMapping("/getTlSlab")
	public @ResponseBody List<TlSlabMaster> getTlSlab(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) {
		List<TlSlabMaster> tlSlabMaster = agentIncentiveService.getTlSlab();

		return tlSlabMaster;
	}

	@PostMapping("/getPlSlab")
	public @ResponseBody List<PlSlabMaster> getPlSlab(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) {
		List<PlSlabMaster> plSlabMaster = agentIncentiveService.getPlSlab();

		return plSlabMaster;
	}

	@PostMapping("/getTlInHouseSlab")
	public @ResponseBody List<TlInHouseSlabMaster> getTlInHouseSlab(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) {
		List<TlInHouseSlabMaster> plSlabMaster = agentIncentiveService.getTlInHouseSlab();

		return plSlabMaster;
	}

	@PostMapping("/getAgentAccessLate")
	public @ResponseBody List<AgentAccessMaster> getAgentAccessLate(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) {
		List<AgentAccessMaster> plSlabMaster = agentIncentiveService.getAgentAccessLate();

		return plSlabMaster;
	}

	@PostMapping("/getLeadTarget")
	public @ResponseBody List<LeadTargetMaster> getLeadTarget(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) {
		List<LeadTargetMaster> leadTarget = agentIncentiveService.getLeadTarget();

		return leadTarget;
	}

	@PostMapping("/insertPerPolicy")
	public @ResponseBody String insertPerPolicy(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException {

		logger.info("BEGIN - insertApplicationConfig()" + req.getQueryString());

		String key = req.getParameter("key") == null ? "" : req.getParameter("key");
		String value = req.getParameter("value") == null ? "" : req.getParameter("value");

		AgentConfig agentConfig = agentIncentiveService.findByKey(key);
		if (agentConfig!=null) {
			logger.info("END - insertPerPolicy() - Duplicate key -->" + key);
			return "Key Already Exists";
		}
		AgentConfig newAgentConfig = new AgentConfig();
		newAgentConfig.setKey(key);
		newAgentConfig.setValue(value);

		AgentConfig savedObj = agentIncentiveService.save(newAgentConfig);
		if (savedObj != null) {
			logger.info(
					"END - insertPerPolicy() - Agent config successfuly added for the processor --> "
							+ key);
			return "Agent Config added successfully";
		}
		logger.error("END - insertPerPolicy() - unable to add Agent config for the key -->"
				+ key);
		return RPAConstants.INTERNAL_ERROR;
	}


	@PostMapping("/updateAgentConfig")
	public @ResponseBody String updateAgentConfig(HttpServletRequest req, HttpServletResponse resp, ModelMap model)
			throws ParseException {

		logger.info("BEGIN - updateAgentConfig()" + req.getQueryString());

		String key_edit = req.getParameter("key_edit") == null ? "" : req.getParameter("key_edit");

		String value_edit = req.getParameter("value_edit") == null ? "" : req.getParameter("value_edit");

		AgentConfig agentConfig = agentIncentiveService.findByKey(key_edit);
		agentConfig.setValue(value_edit);

		if(agentIncentiveService.save(agentConfig)!=null){
			logger.info("END - updateAgentConfig() - Agent config updated.");
			return "Agent Config updated successfully";
		}

		logger.error("END - updateAgentConfig() - Unable to process.");
		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/insertAgentSlab")
	public @ResponseBody String insertAgentSlab(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException {

		logger.info("BEGIN - insertAgentSlab()" + req.getQueryString());

		String odMinSlab = req.getParameter("odMinSlab") == null ? "" : req.getParameter("odMinSlab");
		String odMaxSlab = req.getParameter("odMaxSlab") == null ? "" : req.getParameter("odMaxSlab");
		String pqgPercentage = req.getParameter("pgqPercentage") == null ? "" : req.getParameter("pgqPercentage");
		String wsiPercentage = req.getParameter("wsiPercentage") == null ? "" : req.getParameter("wsiPercentage");
		String wpiPercentage = req.getParameter("wpiPercentage") == null ? "" : req.getParameter("wpiPercentage");
		String inbPercentage = req.getParameter("inbPercentage") == null ? "" : req.getParameter("inbPercentage");

		AgentSlabMaster agentSlabMaster = agentIncentiveService.findBySlab(odMinSlab,odMaxSlab);
		if (agentSlabMaster!=null) {
			logger.info("END - insertAgentSlab() - Duplicate slab  -->" + odMinSlab +" & "+odMaxSlab);
			return "Slab Already Exists";
		}
		AgentSlabMaster newAgentSlabMaster = new AgentSlabMaster();
		newAgentSlabMaster.setOdMinSlab(odMinSlab);
		newAgentSlabMaster.setOdMaxSlab(odMaxSlab);
		newAgentSlabMaster.setPgqPercentage(pqgPercentage);
		newAgentSlabMaster.setWsiPercentage(wsiPercentage);
		newAgentSlabMaster.setWpiPercentage(wpiPercentage);
		newAgentSlabMaster.setInbPercentage(inbPercentage);

		AgentSlabMaster savedObj = agentIncentiveService.save(newAgentSlabMaster);
		if (savedObj != null) {
			logger.info(
					"END - insertAgentSlab() - Agent Slab successfuly added for the processor --> "
							+ odMinSlab +" & "+odMaxSlab);
			return "Agent Config added successfully";
		}
		logger.error("END - insertAgentSlab() - unable to add Agent Slab for the key -->"
				+ odMinSlab +" & "+odMaxSlab);
		return RPAConstants.INTERNAL_ERROR;
	}


	@PostMapping("/updateAgentSlab")
	public @ResponseBody String updateAgentSlab(HttpServletRequest req, HttpServletResponse resp, ModelMap model)
			throws ParseException {

		logger.info("BEGIN - updateAgentSlab()" + req.getQueryString());

		String agentSlabOdMin_edit = req.getParameter("agentSlabOdMin_edit") == null ? "" : req.getParameter("agentSlabOdMin_edit");
		String agentSlabOdMax_edit = req.getParameter("agentSlabOdMax_edit") == null ? "" : req.getParameter("agentSlabOdMax_edit");
		String agentSlabPQG_edit = req.getParameter("agentSlabPQG_edit") == null ? "" : req.getParameter("agentSlabPQG_edit");
		String agentSlabWPI_edit = req.getParameter("agentSlabWPI_edit") == null ? "" : req.getParameter("agentSlabWPI_edit");
		String agentSlabWSI_edit = req.getParameter("agentSlabWSI_edit") == null ? "" : req.getParameter("agentSlabWSI_edit");
		String agentSlabINB_edit = req.getParameter("agentSlabINB_edit") == null ? "" : req.getParameter("agentSlabINB_edit");

		AgentSlabMaster agentSlabMaster = agentIncentiveService.findBySlab(agentSlabOdMin_edit,agentSlabOdMax_edit);
		agentSlabMaster.setPgqPercentage(agentSlabPQG_edit);
		agentSlabMaster.setWsiPercentage(agentSlabWPI_edit);
		agentSlabMaster.setWpiPercentage(agentSlabWSI_edit);
		agentSlabMaster.setInbPercentage(agentSlabINB_edit);

		if(agentIncentiveService.save(agentSlabMaster)!=null){
			logger.info("END - updateAgentSlab() - Agent Slab updated.");
			return "Agent Slab updated successfully";
		}

		logger.error("END - updateAgentSlab() - Unable to process.");
		return RPAConstants.INTERNAL_ERROR;
	}


	@PostMapping("/insertTlSlab")
	public @ResponseBody String insertTlSlab(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException {

		logger.info("BEGIN - insertTlSlab()" + req.getQueryString());

		String perc_70To85 = req.getParameter("perc_70To85") == null ? "" : req.getParameter("perc_70To85");
		String perc_85To90 = req.getParameter("perc_85To90") == null ? "" : req.getParameter("perc_85To90");
		String perc_90To95 = req.getParameter("perc_90To95") == null ? "" : req.getParameter("perc_90To95");
		String perc_95To100 = req.getParameter("perc_95To100") == null ? "" : req.getParameter("perc_95To100");
		String perc_100AndAbove = req.getParameter("perc_100AndAbove") == null ? "" : req.getParameter("perc_100AndAbove");

		TlSlabMaster tlSlabMaster = new TlSlabMaster();
		tlSlabMaster.setPercentage_70_To_85(perc_70To85);
		tlSlabMaster.setPercenatge_85_To_90(perc_85To90);
		tlSlabMaster.setPercenatge_90_To_95(perc_90To95);
		tlSlabMaster.setPercenatge_95_To_100(perc_95To100);
		tlSlabMaster.setPercenatge_100_Above(perc_100AndAbove);

		TlSlabMaster savedObj = agentIncentiveService.save(tlSlabMaster);
		if (savedObj != null) {
			logger.info(
					"END - insertTlSlab() - Tl Slab successfuly added  ");
			return "TL Slab added successfully";
		}
		logger.error("END - insertTlSlab() - unable to add TL Slab");
		return RPAConstants.INTERNAL_ERROR;
	}


	@PostMapping("/updateTlSlab")
	public @ResponseBody String updateTlSlab(HttpServletRequest req, HttpServletResponse resp, ModelMap model)
			throws ParseException {

		logger.info("BEGIN - updateTlSlab()" + req.getQueryString());

		String perc_70To85 = req.getParameter("perc_70To85") == null ? "" : req.getParameter("perc_70To85");
		String perc_85To90 = req.getParameter("perc_85To90") == null ? "" : req.getParameter("perc_85To90");
		String perc_90To95 = req.getParameter("perc_90To95") == null ? "" : req.getParameter("perc_90To95");
		String perc_95To100 = req.getParameter("perc_95To100") == null ? "" : req.getParameter("perc_95To100");
		String perc_100AndAbove = req.getParameter("perc_100AndAbove") == null ? "" : req.getParameter("perc_100AndAbove");
		String tlSlabId = req.getParameter("tlSlabId") == null ? "" : req.getParameter("tlSlabId");

		TlSlabMaster tlSlabMaster = agentIncentiveService.findById(tlSlabId);
		tlSlabMaster.setPercentage_70_To_85(perc_70To85);
		tlSlabMaster.setPercenatge_85_To_90(perc_85To90);
		tlSlabMaster.setPercenatge_90_To_95(perc_90To95);
		tlSlabMaster.setPercenatge_95_To_100(perc_95To100);
		tlSlabMaster.setPercenatge_100_Above(perc_100AndAbove);

		if(agentIncentiveService.save(tlSlabMaster)!=null){
			logger.info("END - updateTlSlab() - TL Slab updated.");
			return "TL Slab updated successfully";
		}

		logger.error("END - updateTlSlab() - Unable to process.");
		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/updatePlSlab")
	public @ResponseBody String updatePlSlab(HttpServletRequest req, HttpServletResponse resp, ModelMap model)
			throws ParseException {

		logger.info("BEGIN - updatePlSlab()" + req.getQueryString());

		String perc_70To85 = req.getParameter("perc_70To85") == null ? "" : req.getParameter("perc_70To85");
		String perc_85To90 = req.getParameter("perc_85To90") == null ? "" : req.getParameter("perc_85To90");
		String perc_90To95 = req.getParameter("perc_90To95") == null ? "" : req.getParameter("perc_90To95");
		String perc_95To100 = req.getParameter("perc_95To100") == null ? "" : req.getParameter("perc_95To100");
		String perc_100AndAbove = req.getParameter("perc_100AndAbove") == null ? "" : req.getParameter("perc_100AndAbove");
		String tlSlabId = req.getParameter("tlSlabId") == null ? "" : req.getParameter("tlSlabId");

		PlSlabMaster plSlabMaster = agentIncentiveService.findByPlId(tlSlabId);
		plSlabMaster.setPercentage_70_To_85(perc_70To85);
		plSlabMaster.setPercenatge_85_To_90(perc_85To90);
		plSlabMaster.setPercenatge_90_To_95(perc_90To95);
		plSlabMaster.setPercenatge_95_To_100(perc_95To100);
		plSlabMaster.setPercenatge_100_Above(perc_100AndAbove);

		if(agentIncentiveService.save(plSlabMaster)!=null){
			logger.info("END - updateTlSlab() - TL Slab updated.");
			return "TL Slab updated successfully";
		}

		logger.error("END - updatePlSlab() - Unable to process.");
		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/insertInHouseTlSlab")
	public @ResponseBody String insertInHouseTlSlab(HttpServletRequest req, HttpServletResponse resp,
			ModelMap model) throws ParseException {

		logger.info("BEGIN - insertInHouseTlSlab()" + req.getQueryString());

		String odMinSlab = req.getParameter("odMinSlab") == null ? "" : req.getParameter("odMinSlab");
		String odMaxSlab = req.getParameter("odMaxSlab") == null ? "" : req.getParameter("odMaxSlab");
		String target = req.getParameter("target") == null ? "" : req.getParameter("target");
		String incentivePercentage = req.getParameter("incentivePercentage") == null ? "" : req.getParameter("incentivePercentage");

		TlInHouseSlabMaster tlInHouseSlabMaster = agentIncentiveService.findByTlHouseSlab(odMinSlab,odMaxSlab);
		if (tlInHouseSlabMaster!=null) {
			logger.info("END - insertInHouseTlSlab() - Duplicate slab  -->" + odMinSlab +" & "+odMaxSlab);
			return "Slab Already Exists";
		}
		TlInHouseSlabMaster newTlInHouseSlabMaster = new TlInHouseSlabMaster();
		newTlInHouseSlabMaster.setOdMinSlab(odMinSlab);
		newTlInHouseSlabMaster.setOdMaxSlab(odMaxSlab);
		newTlInHouseSlabMaster.setTarget(target);
		newTlInHouseSlabMaster.setIncentivePercentage(incentivePercentage);

		TlInHouseSlabMaster savedObj = agentIncentiveService.save(newTlInHouseSlabMaster);
		if (savedObj != null) {
			logger.info(
					"END - insertInHouseTlSlab() - TL Slab successfuly added for the processor --> "
							+ odMinSlab +" & "+odMaxSlab);
			return "TL slab added successfully";
		}
		logger.error("END - insertInHouseTlSlab() - unable to add Tl Slab for the key -->"
				+ odMinSlab +" & "+odMaxSlab);
		return RPAConstants.INTERNAL_ERROR;
	}


	@PostMapping("/updateInHouseTlSlab")
	public @ResponseBody String updateInHouseTlSlab(HttpServletRequest req, HttpServletResponse resp, ModelMap model)
			throws ParseException {

		logger.info("BEGIN - updateInHouseTlSlab()" + req.getQueryString());

		String target = req.getParameter("target") == null ? "" : req.getParameter("target");
		String incentivePercentage = req.getParameter("incentivePercentage") == null ? "" : req.getParameter("incentivePercentage");
		String inHouseTlSlabId = req.getParameter("inHouseTlSlabId") == null ? "" : req.getParameter("inHouseTlSlabId");

		TlInHouseSlabMaster agentSlabMaster = agentIncentiveService.findByTlHouseSlabId(inHouseTlSlabId);
		agentSlabMaster.setTarget(target);
		agentSlabMaster.setIncentivePercentage(incentivePercentage);

		if(agentIncentiveService.save(agentSlabMaster)!=null){
			logger.info("END - updateInHouseTlSlab() - TL Slab updated.");
			return "TL Slab updated successfully";
		}

		logger.error("END - updateInHouseTlSlab() - Unable to process.");
		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping(value = "/getVirStatusChartDetails")
	public @ResponseBody ChartMultiData getVirStatusChartDetails(HttpServletRequest req,
			HttpServletResponse resp) {
		String year = req.getParameter("year") == null ? "" : req.getParameter("year");
		ChartMultiData chartData = dashboardService.virChartStatus(year);
		return chartData;
	}

}