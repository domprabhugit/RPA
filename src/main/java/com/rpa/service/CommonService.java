/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.seleniumhq.jetty7.server.nio.SelectChannelConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainer;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.ApplicationConfiguration;
import com.rpa.model.BusinessProcess;
import com.rpa.model.FolderConfigurationDetails;
import com.rpa.model.TransactionInfo;
import com.rpa.model.User;
import com.rpa.model.processors.DailyFileTransferStatus;
import com.rpa.repository.ApplicationConfigurationRepository;
import com.rpa.repository.ApplicationRepository;
import com.rpa.repository.FolderConfigurationDetailsRepository;
import com.rpa.repository.process.ProcessRepository;
import com.rpa.repository.processors.DailyFileTransferStatusRepository;
import com.rpa.response.GridModelRowStatusResponse;
import com.rpa.response.ParamConfigResponse;
import com.rpa.response.UserProcessResponse;
import com.rpa.service.process.ProcessService;
import com.rpa.util.UtilityFile;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

@Service
public class CommonService {

	private static final Logger logger = LoggerFactory.getLogger(CommonService.class.getName());

	ApplicationContext applicationContext = SpringContext.getAppContext();

	@Autowired
	private ProcessRepository processRepository;

	@Autowired
	private ApplicationRepository applicationRepository;

	@Autowired
	private ApplicationConfigurationRepository applicationConfigurationRepository;

	@Autowired
	private UserService userService;

	@Inject
	private EmbeddedWebApplicationContext appContext;

	@Autowired
	private HttpServletRequest httpServletRequest;

	@Autowired
	private FolderConfigurationDetailsRepository folderConfigurationDetailsRepository;
	
	@Autowired
	private DailyFileTransferStatusRepository dailyFileTransferStatusRepository;
	
	@Autowired
	private ProcessService processService;

	public ModelMap getDashBoardDetails(ModelMap modelMap) {

		logger.info("BEGIN - getDashBoardDetails()");

		CamelContext camelContext = (CamelContext) applicationContext.getBean("camelContext");

		List<Route> routes = camelContext.getRoutes();

		int totalProcesses = routes.size();
		int runningProcesses = 0;
		int pausedProcesses = 0;
		int stoppedProcesses = 0;

		for (Route route : routes) {
			if (route.getRouteContext().getRoute().getStatus(camelContext).isStarted()) {
				runningProcesses++;
			} else if (route.getRouteContext().getRoute().getStatus(camelContext).isStopped()) {
				stoppedProcesses++;
			} else if (route.getRouteContext().getRoute().getStatus(camelContext).isSuspended()) {
				pausedProcesses++;
			}
		}

		modelMap.addAttribute("totalProcesses", totalProcesses);
		modelMap.addAttribute("runningProcesses", runningProcesses);
		modelMap.addAttribute("pausedProcesses", pausedProcesses);
		modelMap.addAttribute("stoppedProcesses", stoppedProcesses);

		logger.info("END - getDashBoardDetails()");

		return modelMap;
	}

	public List<BusinessProcess> getProcessDetails() {

		logger.info("BEGIN - getProcessDetails()");

		CamelContext camelContext = (CamelContext) applicationContext.getBean("camelContext");

		List<Route> routes = camelContext.getRoutes();

		Set<BusinessProcess> runningProcessSet = new HashSet<BusinessProcess>();

		for (Route route : routes) {
			BusinessProcess process = new BusinessProcess();
			process.setProcessName(route.getId());
			process.setProcessDesc(route.getDescription());

			if (RPAConstants.Started.equals(route.getRouteContext().getRoute().getStatus(camelContext).name())) {
				process.setProcessStatus(RPAConstants.Running);
			} else if (RPAConstants.Stopped.equals(route.getRouteContext().getRoute().getStatus(camelContext).name())) {
				process.setProcessStatus(RPAConstants.Stopped);
			} else {
				process.setProcessStatus(RPAConstants.Unknown);
			}
			process.setUpTime(route.getUptime());

			if (process.getProcessName() != null) {
				BusinessProcess alreadyExists = processRepository.findByProcessName(process.getProcessName());
				if (alreadyExists == null) {
					process.setProcessStatus(RPAConstants.Unknown);
				} else {
					process.setId(alreadyExists.getId());
				}
			}
			runningProcessSet.add(process);
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Set<BusinessProcess> compareSet = new HashSet<BusinessProcess>();
		List<BusinessProcess> processList = new ArrayList<BusinessProcess>();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			String currentUserName = authentication.getName();
			User user = userService.findByUsername(currentUserName);
			Set<BusinessProcess> userProcessSet = user.getBusinessProcesses();
			for (BusinessProcess runningProcess : runningProcessSet) {
				for (BusinessProcess userProcess : userProcessSet) {
					if (runningProcess.getProcessName().equals(userProcess.getProcessName())) {
						compareSet.add(runningProcess);
					}
				}
			}
			for (BusinessProcess compareProcess : compareSet) {
				if (RPAConstants.Started.equals(camelContext.getRouteStatus(compareProcess.getProcessName()).name())) {
					compareProcess.setProcessState(true);
				} else if (RPAConstants.Stopped
						.equals(camelContext.getRouteStatus(compareProcess.getProcessName()).name())) {
					compareProcess.setProcessState(false);
				}
				logger.info("getProcessDetails()-" + compareProcess.toString());
				processList.add(compareProcess);
			}
		}

		logger.info("END - getProcessDetails()");

		return processList;
	}

	public void readAllFilesInsideAFolderWithExtension(String folderPath, String fileExtension, String fileAnotherExtension, Exchange exhange)
			throws IOException, URISyntaxException {

		String shareFolderPath = "";

		FolderConfigurationDetails folderConfigurationDetails = getFolderSharePath(RPAConstants.VB64COMPLIANCE,
				exhange.getProperty(RPAConstants.BANK_NAME).toString(), RPAConstants.FILE_TYPE_BANK);

		if (folderConfigurationDetails != null)
			shareFolderPath = folderConfigurationDetails.getFolderPath();

		logger.info("BEGIN - readAllFilesInsideAFolderWithExtension()---Share Folder Path::" + shareFolderPath);

		String filePath = UtilityFile.getCodeBasePath() + UtilityFile.getUploadProperty(folderPath);

		ArrayList<String> fileArrayList = new ArrayList<String>();
		if (!shareFolderPath.equals("")) {

			if (folderConfigurationDetails.getIsRestrictedFolder().equalsIgnoreCase(RPAConstants.N)) {
				logger.info("readAllFilesInsideAFolderWithExtension() isFolderRestrictive type :: " + RPAConstants.N);
				File sharedfiles = new File(shareFolderPath);
				File[] directoryListing = sharedfiles.listFiles();
				if (directoryListing != null) {
					for (File sharedFile : directoryListing) {
						logger.info(
								"readAllFilesInsideAFolderWithExtension() current file name to be copied to RPA bank folder is :: "
										+ sharedFile.getName());
						String currentFileName = sharedFile.getName();

						File sourceFile = new File(shareFolderPath + RPAConstants.SLASH + currentFileName);

						logger.info(" Shared File --> " + currentFileName + " copied to bank folder");

						if (sourceFile.getName().endsWith(".xls") || sourceFile.getName().endsWith(".XLS")) {
							FileUtils.copyFileToDirectory(sourceFile, new File(filePath));
							logger.info("xls file to be deleted from share folder :: " + currentFileName);
							if (sourceFile.delete()) {
								logger.info(currentFileName + " deleted from Shared Folder");
							}
						} else {
							logger.info(" Non xls file :: " + currentFileName + " available in the folder ");
						}
					}

				}
			} else {
				logger.info("readAllFilesInsideAFolderWithExtension() isFolderRestrictive type :: " + RPAConstants.Y);

				String smbPath = "smb:" + shareFolderPath.replace("\\\\", "//").replace("\\", "/");
				logger.info("readAllFilesInsideAFolderWithExtension() smb :: " + smbPath);
				NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null,
						folderConfigurationDetails.getUsername(),
						folderConfigurationDetails.getPassword());

				SmbFile sharedfiles = new SmbFile(smbPath, auth);
				SmbFile[] directoryListing = sharedfiles.listFiles();
				if (directoryListing != null) {
					for (SmbFile sharedFile : directoryListing) {
						logger.info(
								"readAllFilesInsideAFolderWithExtension() current file name to be copied to RPA bank folder is :: "
										+ sharedFile.getName());
						String currentFileName = sharedFile.getName();
						File sourceFile = new File(shareFolderPath + RPAConstants.SLASH + currentFileName);

						logger.info(" Shared File --> " + currentFileName + " copied to bank folder");

						if (sourceFile.getName().endsWith(".xls") || sourceFile.getName().endsWith(".XLS")) {
							FileUtils.copyFileToDirectory(sourceFile, new File(filePath));
							logger.info("xls file to be deleted from share folder :: " + currentFileName);
							if (sourceFile.delete()) {
								logger.info(currentFileName + " deleted from Shared Folder");
							}
						} else {
							logger.info(" Non xls file :: " + currentFileName + " available in the folder ");
						}
					}

				}
			}

			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					boolean accepted = false;
					if(name.endsWith(fileExtension) || name.endsWith(fileAnotherExtension)){
						accepted = true;
					}
					return accepted;
				}
			};

			File folder = new File(filePath);

			logger.info("readAllFilesInsideAFolderWithExtension()--- folder path::" + folder.getPath());

			File[] listOfFiles = folder.listFiles(filter);

			for (int i = 0; i < listOfFiles.length; i++) {
				File file = listOfFiles[i];
				FileChannel fileChannel = null;
				try {
					fileChannel = new RandomAccessFile(file, "rw").getChannel();
					logger.info("readAllFilesInsideAFolderWithExtension()---File Name::" + file.getPath());
					fileArrayList.add(file.getPath());
				} catch (FileNotFoundException e) {
				} catch (OverlappingFileLockException e) {
				} finally {
					if (fileChannel != null) {
						fileChannel.close();
					}
				}
			}
		} else {
			logger.error("readAllFilesInsideAFolderWithExtension() :: Share Folder not configured for "
					+ exhange.getProperty(RPAConstants.BANK_NAME));

		}
		exhange.getIn().setBody(fileArrayList);
		;

		logger.info("END - readAllFilesInsideAFolderWithExtension()---Folder Extension::" + fileExtension);

	}

	@SuppressWarnings("deprecation")
	public boolean isRowEmpty(HSSFRow row) {
		for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
			HSSFCell cell = row.getCell(c);
			if (cell != null && cell.getCellType() != HSSFCell.CELL_TYPE_BLANK)
				return false;
		}
		return true;
	}

	public Object getAppConfiguationList() {
		return applicationRepository.findAll();
	}

	public ApplicationConfiguration getApplicationDetails(Long processId, Long appId) {
		return applicationConfigurationRepository.findByProcessAndAppId(processId, appId);
	}

	public String getBaseUrl() throws UnknownHostException {
		SelectChannelConnector connector = (SelectChannelConnector) ((JettyEmbeddedServletContainer) appContext
				.getEmbeddedServletContainer()).getServer().getConnectors()[0];
		String scheme = "http";
		String url;
		// scheme = connector.getDefaultProtocol().toLowerCase().contains("ssl")
		// ? "https" : "http";
		String contextPath = appContext.getServletContext().getContextPath();
		if (httpServletRequest.getHeader("host") != null) {
			url = scheme + "://" + httpServletRequest.getHeader("host") + contextPath;
		} else {
			String ip = InetAddress.getLocalHost().getHostAddress();
			int port = connector.getLocalPort();
			url = scheme + "://" + ip + ":" + port + contextPath;
		}
		return url;
	}

	public FolderConfigurationDetails getFolderSharePath(String processName, String customerName, String fileType) {
		logger.info("Begin : getFolderSharePath() called with process -->" + processName + ", customer --> "
				+ customerName + ", file type -->" + fileType);
		FolderConfigurationDetails folderConfigDetails = null;
		List<FolderConfigurationDetails> listObj = folderConfigurationDetailsRepository
				.getActiveFolderConfigDetails(processName, customerName, fileType);
		for (FolderConfigurationDetails obj : listObj) {
			folderConfigDetails = obj;
		}
		if (folderConfigDetails == null) {
			logger.error("error in getFolderSharePath() :: Folder Configuration not done for process -->" + processName
					+ ", customer --> " + customerName + ", file type -->" + fileType);
		}
		return folderConfigDetails;
	}
	
	public Long getThresholdFrequencyLevel(String param) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException{
			logger.info("commonservice - BEGIN getThresholdFrequencyLevel() called -  param name :: "+param);
			Connection conn = UtilityFile.getLocalRPAConnection();
			long thresholdLevel = 0;
			CallableStatement callStmt = null;
			try {
				String sql = "select THRESHOLD_COUNT from CAR_POLICY_THRESHOLD where PARAM='"
						+ param + "'";
				PreparedStatement statement = conn.prepareStatement(sql);
				ResultSet rs = statement.executeQuery();
				while (rs.next()) {
					thresholdLevel = rs.getLong(1);
				}
				if(rs!=null)
					rs.close();
				if(statement!=null)
					statement.close();
			} finally {
				if (conn != null) {
					conn.close();
				}
				if (callStmt != null) {
					callStmt.close();
				}
			}
			logger.info("commonservice - END getThresholdFrequencyLevel() thresholdLevel ::" + thresholdLevel);
			return thresholdLevel;
		}
	
	public boolean transferFileBewtweenServers(TransactionInfo transactionInfo,String transferFileType, String extractedFilePrefix, String renewedFilePrefix, Exchange exchange, DailyFileTransferStatus dailyFileTransferStatus) throws Exception{
		String sourceFilePath = "",destinationFilePath="",appName="File Transfer";
		
		 String server = "52.172.1.178";
	        int port = 21;
	        String user = "Qlikviewusr";
	        String pass = "India@123";
		
		logger.info("called transferFileBewtweenServers() ");
		
		int successfulFileTransferCount = 0;
		
		FolderConfigurationDetails sourcefolderConfigurationDetails = getFolderSharePath(RPAConstants.SFTP_Files,
				transferFileType, RPAConstants.FROM_SERVER);
		
		FolderConfigurationDetails destinationfolderConfigurationDetails = getFolderSharePath(RPAConstants.SFTP_Files,
				transferFileType, RPAConstants.TO_SERVER);

		if (sourcefolderConfigurationDetails != null)
			sourceFilePath = sourcefolderConfigurationDetails.getFolderPath();
		
		if (destinationfolderConfigurationDetails != null)
			destinationFilePath = destinationfolderConfigurationDetails.getFolderPath();
		
		@SuppressWarnings("unused")
		boolean fileOneFound = false,fileTwoFound = false;
		
			String sourceSmbPath = "smb:" + sourceFilePath.replace("\\\\", "//").replace("\\", "/");
			logger.info("transferFileBewtweenServers() source smb path :: " + sourceSmbPath);
			String destinationSmbPath = "smb:" + destinationFilePath.replace("\\\\", "//").replace("\\", "/");
			logger.info("transferFileBewtweenServers() destination smb path :: " + destinationSmbPath);
			NtlmPasswordAuthentication sourceAuth = new NtlmPasswordAuthentication("",
					sourcefolderConfigurationDetails.getUsername(),
					sourcefolderConfigurationDetails.getPassword());
			
			//jcifs.Config.setProperty("jcifs.resolveOrder","BCAST,DNS");
			
			//jcifs.Config.setProperty("jcifs.resolveOrder","DNS");
			//jcifs.Config.setProperty("jcifs.smb.client.dfs.disabled","false");
			

			SmbFile sourcefiles = new SmbFile(sourceSmbPath, sourceAuth);
			
			SmbFile[] directoryListing = sourcefiles.listFiles();
			//SmbFile[] directoryListing = sourcefiles.listFiles(fileNamefilter);
			logger.info("directoryListing "+directoryListing);
			if (directoryListing != null) {
				logger.info("directoryListing length -->"+directoryListing.length);
				if(directoryListing.length>0){
					for (SmbFile sharedFile : directoryListing) {
						logger.info(
								"transferFileBewtweenServers() current file iterating in Destination server is :: "
										+ sharedFile.getName());
						String currentFileName = sharedFile.getName();
						if(dailyFileTransferStatus.getIsSbsExtractionTransferred().equalsIgnoreCase(RPAConstants.N) && currentFileName.equalsIgnoreCase(extractedFilePrefix)){
							//sharedFile.copyTo(destinationfiles);
							
						        FTPClient ftpClient = new FTPClient();
						        try {
						 
						            ftpClient.connect(server, port);
						            ftpClient.login(user, pass);
						            ftpClient.enterLocalPassiveMode();
						            
						            boolean folderCreated = ftpClient.makeDirectory("/Files/test/SBS_"+UtilityFile.createSpecifiedDateFormat(RPAConstants.yyyy_MM_dd));   
						            System.out.println("folderCreated-->"+folderCreated);
						            
						            
						            // APPROACH #1: uploads first file using an InputStream
						            //File firstLocalFile = new File("D:/Test/Projects.zip");
						 
						            String curName = sharedFile.getName();
									File sourceFile = new File(sourceFilePath + RPAConstants.SLASH + curName);
						            InputStream inputStream = new FileInputStream(sourceFile);
						 
						            System.out.println("Start uploading first file");
						            boolean done = ftpClient.storeFile(curName, inputStream);
						            inputStream.close();
						            if (done) {
						                System.out.println("The first file is uploaded successfully.");
						            }
						            
						        }catch(Exception e){
						        	
						        }
							
							
							successfulFileTransferCount++;
							logger.info(" file :: --> " + currentFileName + " copied to destination folder");
							fileOneFound = true;
							dailyFileTransferStatus.setIsSbsExtractionTransferred(RPAConstants.Y);
						}else if(dailyFileTransferStatus.getIsSbsRenewalTransferred().equalsIgnoreCase(RPAConstants.N) && currentFileName.equalsIgnoreCase(renewedFilePrefix)){
							//sharedFile.copyTo(destinationfiles);
							successfulFileTransferCount++;
							logger.info(" file :: --> " + currentFileName + " copied to destination folder");
							fileTwoFound = true;
							dailyFileTransferStatus.setIsSbsRenewalTransferred(RPAConstants.Y);
						}
						if(fileOneFound == true && fileTwoFound == true)
							break;
						//sourcefiles.copyTo(destinationfiles);
						
					}
					
					dailyFileTransferStatusRepository.save(dailyFileTransferStatus);
					
					transactionInfo.setTotalSuccessRecords(String.valueOf(successfulFileTransferCount));
					
					if(fileOneFound==false){
						EmailService emailService = (EmailService) applicationContext.getBean("emailService");
						emailService.carPolicyExtractionNotification((TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO),"File Not Found - Please check the "+extractedFilePrefix+" File availability in "+sourcefolderConfigurationDetails.getFolderPath()+"",appName);
						transactionInfo.setProcessFailureReason("File Not Found");
						throw new Exception("File Not Found - Please check proper File availability in "+sourcefolderConfigurationDetails.getFolderPath());
					}if(fileTwoFound==false){
						EmailService emailService = (EmailService) applicationContext.getBean("emailService");
						emailService.carPolicyExtractionNotification((TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO),"File Not Found - Please check the "+renewedFilePrefix+" File availability in "+sourcefolderConfigurationDetails.getFolderPath()+"",appName);
						transactionInfo.setProcessFailureReason("File Not Found");
						throw new Exception("File Not Found - Please check proper File availability in "+sourcefolderConfigurationDetails.getFolderPath());
					}
					
				}else{
					EmailService emailService = (EmailService) applicationContext.getBean("emailService");
					emailService.carPolicyExtractionNotification((TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO),"File Not Found - Please check the "+extractedFilePrefix+" & "+renewedFilePrefix+" File availability in "+sourcefolderConfigurationDetails.getFolderPath()+"",appName);
					transactionInfo.setProcessFailureReason("File Not Found");
					throw new Exception("File Not Found - Please check proper File availability in "+sourcefolderConfigurationDetails.getFolderPath());
				}

			}else{
				return false;
			}
			
		

	
		
		return true;
	}
	
	public DailyFileTransferStatus getDailyTransferStatus(String transferDate){
		return dailyFileTransferStatusRepository.getDailyTransferStatus(transferDate);
	}
	
	public void isCredentialsExpired(String flag, Exchange exchange){
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		BusinessProcess businessProcess = processService.findByProcessName(transactionInfo.getProcessName());
		Long appId = Long.valueOf(0);
		
		if(flag.equalsIgnoreCase("M")){
			appId = RPAConstants.APPID_MARUTI;
		}else if(flag.equalsIgnoreCase("H")){
			appId = RPAConstants.APPID_HONDA;
		}else if(flag.equalsIgnoreCase("F")){
			appId = RPAConstants.APPID_FORD;
		}else if(flag.equalsIgnoreCase("T")){
			appId = RPAConstants.APPID_TATA;
		}else if(flag.equalsIgnoreCase("A")){
			appId = RPAConstants.APPID_ABIBL;
		}else if(flag.equalsIgnoreCase("B")){
			appId = RPAConstants.APPID_MIBL;
		}else if(flag.equalsIgnoreCase("V")){
			appId = RPAConstants.APPID_VOLVO;
		}else if(flag.equalsIgnoreCase("E")){
			appId = RPAConstants.APPID_TAFE;
		}else if(flag.equalsIgnoreCase("P")){
			appId = RPAConstants.APPID_PIAGGIO;
		}
		
		ApplicationConfiguration applicationDetails = getApplicationDetails(businessProcess.getId(),
				appId);
		
		if(applicationDetails!=null){
			if(applicationDetails.getIsPasswordExpired().equalsIgnoreCase("Y")){
				exchange.getIn().setHeader(RPAConstants.PROCEED_FURTHER, RPAConstants.N);
				transactionInfo.setStatus("D");
				exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
			}else{
				exchange.getIn().setHeader(RPAConstants.PROCEED_FURTHER, RPAConstants.Y);
			}
		}else{
			logger.error("isCredentialsExpired - "+transactionInfo.getProcessName()+" application details not configured " );
			exchange.getIn().setHeader(RPAConstants.PROCEED_FURTHER, RPAConstants.Y);
		}
		
	}

	public List<BusinessProcess> getbussinessProcess() {
		return processRepository.findAll();
	}
	
	public BusinessProcess saveBusinessProcess(BusinessProcess businessProcess) {
		return processRepository.save(businessProcess);
	}

	public BusinessProcess findBussinessProcessById(Long id) {
		return processRepository.getOne(id);
	}

	public List<UserProcessResponse> getUserProcessConfigurationDetails() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		Connection conn = UtilityFile.getLocalRPAConnection();
		CallableStatement callStmt = null;
		List<UserProcessResponse> list = new ArrayList<UserProcessResponse>();
		try {
			String sql = "select USER_ID,PROCESS_ID from RPA_USER_PROCESS ORDER BY USER_ID,PROCESS_ID";
			PreparedStatement statement = conn.prepareStatement(sql);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				UserProcessResponse userProcessResponse = new UserProcessResponse(); 
				userProcessResponse.setUserId(rs.getLong(1));
				userProcessResponse.setProcessID(rs.getLong(2));
				list.add(userProcessResponse);
			}
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (callStmt != null) {
				callStmt.close();
			}
		}
		logger.info("commonservice - END getUserProcessConfigurationDetails() list size ::" + list.size());
		return list;
	}

	public boolean insertUserProcessConfig(String userId, String processId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		Connection conn = UtilityFile.getLocalRPAConnection();
		CallableStatement callStmt = null;
		try {
			String sql = "insert into RPA_USER_PROCESS values("+userId+","+processId+")";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.execute();
			
			if(statement!=null)
				statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (callStmt != null) {
				callStmt.close();
			}
		}
		return true;
	}

	public boolean deleteUserProcessConfig(String userId, String processId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		Connection conn = UtilityFile.getLocalRPAConnection();
		CallableStatement callStmt = null;
		try {
			String sql = "delete from RPA_USER_PROCESS where user_id="+userId+" and process_id="+processId;
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.executeUpdate();
			
			if(statement!=null)
				statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (callStmt != null) {
				callStmt.close();
			}
		}
		return true;
	}

	public boolean processConfigDelete(String processId) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		Connection conn = UtilityFile.getLocalRPAConnection();
		CallableStatement callStmt = null;
		try {
			String sql = "delete from BUSINESS_PROCESS where id="+processId;
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.executeUpdate();
			
			if(statement!=null)
				statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (callStmt != null) {
				callStmt.close();
			}
		}
		return true;
	}

	public List<ParamConfigResponse> getParamConfigDetails() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException {
		Connection conn = UtilityFile.getLocalRPAConnection();
		CallableStatement callStmt = null;
		List<ParamConfigResponse> list = new ArrayList<ParamConfigResponse>();
		try {
			String sql = "select PARAM,THRESHOLD_COUNT from CAR_POLICY_THRESHOLD";
			PreparedStatement statement = conn.prepareStatement(sql);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				ParamConfigResponse paramConfigResponse = new ParamConfigResponse(); 
				paramConfigResponse.setParamKey(rs.getString(1));
				paramConfigResponse.setParamValue(rs.getString(2));
				list.add(paramConfigResponse);
			}
			if(rs!=null)
				rs.close();
			if(statement!=null)
				statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (callStmt != null) {
				callStmt.close();
			}
		}
		logger.info("commonservice - END getUserProcessConfigurationDetails() list size ::" + list.size());
		return list;
	}

	public boolean insertParamConfig(String paramKey, String paramValue) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		Connection conn = UtilityFile.getLocalRPAConnection();
		CallableStatement callStmt = null;
		try {
			String sql = "insert into CAR_POLICY_THRESHOLD values ('"+paramKey+"','"+paramValue+"')";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.execute();
			
			if(statement!=null)
				statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (callStmt != null) {
				callStmt.close();
			}
		}
		return true;
	}

	public boolean deleteParamConfig(String paramKey, String paramValue) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		Connection conn = UtilityFile.getLocalRPAConnection();
		CallableStatement callStmt = null;
		try {
			String sql = "delete from CAR_POLICY_THRESHOLD where PARAM='"+paramKey+"' and THRESHOLD_COUNT='"+paramValue+"' ";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.executeUpdate();
			
			if(statement!=null)
				statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (callStmt != null) {
				callStmt.close();
			}
		}
		return true;
	}

	public boolean paramConfigUpdate(String paramKey, String paramValue) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		Connection conn = UtilityFile.getLocalRPAConnection();
		CallableStatement callStmt = null;
		try {
			String sql = "update CAR_POLICY_THRESHOLD set THRESHOLD_COUNT='"+paramValue+"' where PARAM='"+paramKey+"' " ;
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.executeUpdate();
			
			if(statement!=null)
				statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (callStmt != null) {
				callStmt.close();
			}
		}
		return true;
	}
	
}