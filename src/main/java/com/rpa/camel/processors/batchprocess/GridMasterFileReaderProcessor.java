package com.rpa.camel.processors.batchprocess;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.FolderConfigurationDetails;
import com.rpa.model.TransactionInfo;
import com.rpa.repository.FolderConfigurationDetailsRepository;
import com.rpa.repository.TransactionInfoRepository;
import com.rpa.service.CommonService;
import com.rpa.util.UtilityFile;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class GridMasterFileReaderProcessor implements Processor

{

	private static final Logger logger = LoggerFactory.getLogger(GridMasterFileReaderProcessor.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	TransactionInfoRepository transactionInfoRepository;

	@Autowired
	private CommonService commonService;

	@SuppressWarnings("unused")
	private void AutoWiringBeanPropertiesSetMethod() {

		logger.info("AutoWiringBeanPropertiesSetMethod is Called in GridMasterFileReaderProcessor Class");
		applicationContext = SpringContext.getAppContext();
		transactionInfoRepository = applicationContext.getBean(TransactionInfoRepository.class);
		applicationContext.getBean(FolderConfigurationDetailsRepository.class);
		commonService = applicationContext.getBean(CommonService.class);
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in GridMasterFileReaderProcessor Class");

	}

	@Override
	public void process(Exchange exchange) throws Exception {
		GridMasterFileReaderProcessor fileReaderProcessor = new GridMasterFileReaderProcessor();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty("TRANSACTION_INFO_REQ");
		fileReaderProcessor.doProcess(transactionInfo, exchange);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

	};

	private void doProcess(TransactionInfo transactionInfo, Exchange exchange) throws URISyntaxException, IOException {
		AutoWiringBeanPropertiesSetMethod();
		logger.info("BEGIN - GridMasterFileReaderProcessor---doProcess() Method Called Here" );
		transactionInfo.setProcessStatus(RPAConstants.GRID_READER_PHASE);
		readAllFilesInsideAFolderForGridProcess(transactionInfo);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		logger.info("BEGIN - GridMasterFileReaderProcessor---doProcess() Method Ended Here" );
	}

	
	private void readAllFilesInsideAFolderForGridProcess(TransactionInfo transactionInfo)
			throws URISyntaxException, IOException {
		String shareFolderPath = "";
FolderConfigurationDetails folderConfigurationDetails = commonService.getFolderSharePath(transactionInfo.getProcessName(), RPAConstants.gridCustomerName, RPAConstants.GridFileType);
if (folderConfigurationDetails != null)
shareFolderPath = folderConfigurationDetails.getFolderPath();
logger.info("BEGIN - readAllFilesInsideAFolderForGridProcess()---Share Folder Path::" + shareFolderPath);
String filePath = UtilityFile.getCodeBasePath()+ UtilityFile.getGridUploadProperty("Grid.File.Location.BasePath");

		if (!shareFolderPath.equals("")) {
			if (folderConfigurationDetails.getIsRestrictedFolder().equalsIgnoreCase(RPAConstants.Y)) {
				logger.info("readAllFilesInsideAFolderForGridProcess() isFolderRestrictive type :: " + RPAConstants.Y);
				String smbPath = "smb:" + shareFolderPath.replace("\\\\", "//").replace("\\", "/");
				//NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("tekplay.com",
					//	folderConfigurationDetails.getUsername(), folderConfigurationDetails.getPassword());
				NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null,
						folderConfigurationDetails.getUsername(), folderConfigurationDetails.getPassword());


				SmbFile sharedfiles = new SmbFile(smbPath, auth);
				
				SmbFile[] directoryListing = sharedfiles.listFiles();

				if (directoryListing != null) {
					for (SmbFile sharedFile : directoryListing) {
						logger.info(
								"readAllFilesInsideAFolderForGridProcess() current file name to be copied to Grid folder is :: "
										+ sharedFile.getName());
						String currentFileName = sharedFile.getName();
						String ext = FilenameUtils.getExtension(currentFileName);
						if (Arrays.asList(
								UtilityFile.getGridUploadProperty("Grid.File.Extension").split(RPAConstants.COMMA))
								.contains(ext)) {
						/*	File sourceFile = new File(shareFolderPath + RPAConstants.SLASH + currentFileName);
							FileUtils.copyFileToDirectory(sourceFile, new File(filePath));
							
							//SmbFile sourceFile1 = new SmbFile(sharedFile,auth);
							NtlmPasswordAuthentication auth1 = new NtlmPasswordAuthentication(null,
									"localhost\\User", "welcome@12");
							sharedFile.copyTo(new SmbFile( "smb://localhost" + filePath.replace(":", "$"),auth1));
							sharedFile.renameTo(new SmbFile( "smb://localhost" +filePath.replace(":", "$"),auth1));
						//	sourceFile1.
						//	FileUtils.copyFileToDirectory(sourceFile1, new File(filePath));
							sharedFile.delete();
							logger.info(" Shared File --> " + currentFileName + " copied to Grid folder");
							if (sourceFile1.delete()) {
								logger.info(currentFileName + " deleted from Shared Folder");

							} else {
								logger.info(" Non Excel file :: " + currentFileName + " available in the folder ");
							}*/
						
							InputStream in = null;
				               OutputStream out = null;
							
							   in = new BufferedInputStream(new SmbFileInputStream(sharedFile));
			                   out = new BufferedOutputStream(new FileOutputStream(filePath+ RPAConstants.SLASH +currentFileName));
			 
			                   byte[] buffer = new byte[4096];
			                   int len = 0; //Read length
			                   while ((len = in.read(buffer, 0, buffer.length)) != -1) {
			                             out.write(buffer, 0, len);
			                   }
			                   out.flush(); //The refresh buffer output stream
			                   out.close();
			                   in.close();
			                   sharedFile.delete();
			           		logger.info(currentFileName + " deleted from Shared Folder");
							
						} else {
							logger.info(" File::> " + currentFileName + "  is  not  excel File");

						}
					}

				}

			} else {
				File sharedfiles = new File(shareFolderPath);
				File[] directoryListing = sharedfiles.listFiles();
				if (directoryListing != null) { 
					for (File sharedFile : directoryListing) {
						logger.info(
								"readAllFilesInsideAFolderForGridProcess() current file name to be copied to Grid folder is :: "
										+ sharedFile.getName());
						String currentFileName = sharedFile.getName();
						String ext = FilenameUtils.getExtension(currentFileName);
						if (Arrays.asList(
								UtilityFile.getGridUploadProperty("Grid.File.Extension").split(RPAConstants.COMMA))
								.contains(ext)) {
							File sourceFile = new File(shareFolderPath + RPAConstants.SLASH + currentFileName);
							FileUtils.copyFileToDirectory(sourceFile, new File(filePath));
							logger.info(" Shared File --> " + currentFileName + " copied to bank folder");
							if (sourceFile.delete()) {
								logger.info(currentFileName + " deleted from Shared Folder");

							} else {
								logger.info(" Non xls file :: " + currentFileName + " available in the folder ");
							}
						} else {
							logger.info(" File::> " + currentFileName + " not  excel File");

						}
					}

				}

			}

		}

	}

}
