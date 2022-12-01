/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.binary.Base64;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.rpa.OmniDocClient;
import com.rpa.OmniDocsUploader;
import com.rpa.camel.config.SpringContext;
import com.rpa.camel.processors.common.Jax2MarshallerGenerater;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.FirstgenPolicy;
import com.rpa.response.CarUploadResponse;
import com.rpa.service.CommonService;
import com.rpa.service.processors.FirstGenDownloadPolicyService;
import com.rpa.util.UtilityFile;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class FirstgenPdfUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(FirstgenPdfUploader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	FirstGenDownloadPolicyService firstGenDownloadPolicyService;

	@Autowired
	Jax2MarshallerGenerater jax2MarshallerGenerater;

	@Autowired
	OmniDocClient omniDocClient;

	@Autowired
	OmniDocsUploader omniDocUploader;

	@Autowired
	private CommonService commonService;

	@Autowired
	private Environment environment;

	@SuppressWarnings("static-access")
	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("*********** inside Camel Process of firstgenPdfUploader Called ************");
		logger.info("BEGIN : firstgenPdfUploader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.UPLOAD);
		FirstgenPdfUploader firstgenPdfUploader = new FirstgenPdfUploader();
		firstGenDownloadPolicyService = applicationContext.getBean(FirstGenDownloadPolicyService.class);
		jax2MarshallerGenerater = applicationContext.getBean(Jax2MarshallerGenerater.class);
		Jaxb2Marshaller jaxb2Marshaller = jax2MarshallerGenerater.getJax2MarshallerObject();
		omniDocClient = applicationContext.getBean(OmniDocClient.class);
		omniDocUploader = applicationContext.getBean(OmniDocsUploader.class);
		commonService = applicationContext.getBean(CommonService.class);
		environment = applicationContext.getBean(Environment.class);
		firstgenPdfUploader.doProcess(firstgenPdfUploader, exchange, transactionInfo, firstGenDownloadPolicyService,
				omniDocUploader, omniDocClient, jaxb2Marshaller, commonService, environment);
		logger.info("*********** inside Camel Process of firstgenPdfUploader Processor Ended ************");
	}

	public void doProcess(FirstgenPdfUploader firstgenPdfUploader, Exchange exchange,
			TransactionInfo transactionInfo, FirstGenDownloadPolicyService firstGenDownloadPolicyService, OmniDocsUploader omniDocUploader,
			OmniDocClient omniDocClient, Jaxb2Marshaller marshaller, CommonService commonService,
			Environment environment) throws Exception {
		logger.info("BEGIN : firstgenPdfUploader Processor - doProcess Method Called  ");
		
		getPolicyNumberListTobeUploadedFromLocalDb(transactionInfo, firstGenDownloadPolicyService, exchange, omniDocUploader,
				commonService, environment);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : firstgenPdfUploader Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeUploadedFromLocalDb(TransactionInfo transactionInfo,
			FirstGenDownloadPolicyService firstGenDownloadPolicyService, Exchange exchange, OmniDocsUploader omniDocUploader,
			CommonService commonService, Environment environment) throws Exception {
		logger.info(
				"Processor - firstgenPdfUploader - BEGIN getPolicyNumberListTobeUploadedFromLocalDb()  called ");

		List<FirstgenPolicy> fordPolicyNoList = null;
			fordPolicyNoList = firstGenDownloadPolicyService.findPdfUnUploadedPolicies(
					commonService.getThresholdFrequencyLevel(RPAConstants.FIRSTGEN_UPLOAD_THRESHOLD));
		
		logger.info("firstgenPdfUploader - UnUploaded Policies count ::" + fordPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(fordPolicyNoList.size()));

		int uplodedPolicyPDFCount = 0, uplodedProposalPDFCount = 0;

		String /*inwardCode = "",*/ policyNo = "",shareFolderPath="";
		//String proposaFolderIndex="";
		CarUploadResponse carUploadResponse = null;
		
		String omniUser="",omniPassword="",omnicabinetName="",omnidataDefName="",omnidataDefId="",omniPolicyIndex="",
				omniAppServer="",omniDocSize="",omniDTCFolderIndex="",omniDocTypeIndex="";
		
			omniUser = UtilityFile.getDTCDocLiveProperty(RPAConstants.OMNI_USERNAME);
			omniPassword = UtilityFile.getDTCDocLiveProperty(RPAConstants.OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getDTCDocLiveProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getDTCDocLiveProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getDTCDocLiveProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getDTCDocLiveProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniAppServer = UtilityFile.getDTCDocLiveProperty(RPAConstants.OMNI_APP_SERVER_IP);
			omniDocSize = UtilityFile.getDTCDocLiveProperty(RPAConstants.OMNI_DOC_SIZE);
			if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
				omniDTCFolderIndex = UtilityFile.getDTCDocLiveProperty(RPAConstants.OMNI_DTC_LIVE_FOLDER_INDEX);
			}else{
				omniDTCFolderIndex = UtilityFile.getDTCDocLiveProperty(RPAConstants.OMNI_DTC_UAT_FOLDER_INDEX);
			}
			omniDocTypeIndex = UtilityFile.getDTCDocLiveProperty(RPAConstants.OMNI_DOC_TYPE_INDEX);
		
		String uploadFilePath = "";
		//uploadFilePath = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty("firstgen.download.policy.upload.location");
		/*File uploadFolder = new File(uploadFilePath);
		if (!uploadFolder.exists());
			uploadFolder.mkdirs();*/
		
		String fileName = "";
		
		String firstGenDBSPDFPath = UtilityFile.getCodeBasePath()
				+ UtilityFile.getCarPolicyProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DOCUMENT_FOLDER);
		
		File firstGenDBSPDFFolder = new File(firstGenDBSPDFPath);
		if (!firstGenDBSPDFFolder.exists());
			firstGenDBSPDFFolder.mkdirs();
		
		for (FirstgenPolicy firstgenPolicyObj : fordPolicyNoList) {		
			policyNo = firstgenPolicyObj.getPolicyNo().replaceAll("(\\r\\n|\\n|\\r)", "");
			
			if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
				/*fileName = "policy_" + policyNo + ".pdf";*/
				fileName =  policyNo + "_PolicyDocument.pdf";
			}else{
				/*fileName = "UAT_policy_" + policyNo + ".pdf";*/
				fileName = "UAT_" + policyNo + "_PolicyDocument.pdf";
			}
			
			logger.info("firstgenPdfUploader - policyNo ::::::::::::::::::::::::::: "+policyNo);
			
			if (firstgenPolicyObj.getIsPolicyUploaded() == null)
				firstgenPolicyObj.setIsPolicyUploaded(RPAConstants.N);

			/* policy Extraction */
			if (firstgenPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
				logger.info("firstgenPdfUploader - policy not downloaded yet :: " + policyNo);
			} else if (firstgenPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.N)
					|| firstgenPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {

				logger.info("firstgenPdfUploader - current policy number to be uploaded " + policyNo);
				
				File currentAvailableFoder = new File("\\\\inmchn242\\documents\\DBS_PDF");
				logger.info("firstgenPdfUploader - is currentAvailableFoder is writtable ?? :: "+currentAvailableFoder.canWrite());
				
				logger.info("firstgenPdfUploader - is currentAvailableFoder is readable ?? :: "+currentAvailableFoder.canRead());
				
				try{
					displayDirectoryContents(currentAvailableFoder);
				}catch(Exception e){
					logger.error("error in displayDirectoryContents ::"+e);
				}
				
				try{
					listFilesAndFolderWithSmb();
				}catch(Exception e){
					logger.error("error in listFilesAndFolderWithSmb ::"+e);
				}
				
				try{
					getSmbFilesWithoutCredentials();
				}catch(Exception e){
					logger.error("error in getSmbFilesWithoutCredentials ::"+e);
				}
				
				if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
					if(policyNo.startsWith("PDS")){
						
						/*if(currentAvailableFoder.canWrite()){
							shareFolderPath = "\\\\inmchn242\\documents\\DBS_PDF\\DS06 _PDF\\"+policyNo+"\\";	
						}else{*/
							shareFolderPath = UtilityFile.getCodeBasePath()
									+ UtilityFile.getCarPolicyProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DOCUMENT06_FOLDER)+"\\"+policyNo+"\\";
							File dso6Folder = new File(shareFolderPath);
							if (!dso6Folder.exists());
								dso6Folder.mkdirs();
						/*}*/
						
					}else if(policyNo.startsWith("DBS")){
						/*if(currentAvailableFoder.canWrite()){
							shareFolderPath = "\\\\inmchn242\\documents\\DBS_PDF\\DS07_PDF\\"+policyNo+"\\";
						}else{*/
							shareFolderPath = UtilityFile.getCodeBasePath()
									+ UtilityFile.getCarPolicyProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DOCUMENT07_FOLDER)+"\\"+policyNo+"\\";
							File dso7Folder = new File(shareFolderPath);
							if (!dso7Folder.exists());
							dso7Folder.mkdirs();
						/*}*/
					}
				}else{
					if(policyNo.startsWith("PDS")){
						/*if(currentAvailableFoder.canWrite()){
							shareFolderPath = "\\\\inmchn242\\documents\\DBS_PDF\\DS06 _PDF\\TST_"+policyNo+"\\";
						}else{*/
							shareFolderPath = UtilityFile.getCodeBasePath()
									+ UtilityFile.getCarPolicyProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DOCUMENT06_FOLDER)+"\\TST_"+policyNo+"\\";
							File dso6Folder = new File(shareFolderPath);
							if (!dso6Folder.exists());
								dso6Folder.mkdirs();
						/*}*/
					}else if(policyNo.startsWith("DBS")){
						/*if(currentAvailableFoder.canWrite()){
							shareFolderPath = "\\\\inmchn242\\documents\\DBS_PDF\\DS07_PDF\\TST_"+policyNo+"\\";
						}else{*/
							shareFolderPath = UtilityFile.getCodeBasePath()
									+ UtilityFile.getCarPolicyProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DOCUMENT07_FOLDER)+"\\TST_"+policyNo+"\\";
							File dso7Folder = new File(shareFolderPath);
							if (!dso7Folder.exists());
								dso7Folder.mkdirs();
						/*}*/
					}
				}
				
				
				
				
				
				/*if(currentAvailableFoder.canWrite()){	
					File uploadFolder = new File("\\\\inmchn242\\documents\\DBS_PDF\\DS07_PDF\\TST_"+policyNo);
					if (!uploadFolder.exists());
					boolean isDirectoryCreated  = uploadFolder.mkdirs();
					logger.info("firstgenPdfUploader - is directory created ?? :: "+uploadFolder.mkdirs());
					if(!isDirectoryCreated){
						try{
						Files.createDirectory(uploadFolder.toPath());
						}catch(Exception e){
							logger.error("Exception in createDirectory method :: "+ e);
						}
					}
						logger.info("firstgenPdfUploader - is directory is writtable ?? :: "+uploadFolder.canWrite());
						
						logger.info("firstgenPdfUploader - is directory is readable ?? :: "+uploadFolder.canRead());
						
						
				
				}*/
				if(firstgenPolicyObj.getIsPdfMerged().equalsIgnoreCase("N")){
					File f = new File(shareFolderPath + policyNo + "_PolicyDocument.pdf");
					if(f!=null){
						logger.info("firstgenPdfUploader -- file path :: "+f.getPath());
						logger.info("firstgenPdfUploader -- file absolute path :: "+f.getAbsolutePath());
						logger.info("firstgenPdfUploader -- file name :: "+f.getName());
					}
					if(f.exists() && !f.isDirectory()) { 
						logger.info("firstgenPdfUploader - file already merged as :: " + shareFolderPath+fileName);
					}else{
						if(mergePDf(firstgenPolicyObj.getPolicyPdfPath(),firstgenPolicyObj.getInvoicePdfPath(),shareFolderPath+fileName)){
							/*To Delete policy & invoice file after merge*/
							logger.info("firstgenPdfUploader - is policy - " + policyNo + "'s pdf File Deleted ? "
									+ deleteFile(firstgenPolicyObj.getPolicyPdfPath()));
							logger.info("firstgenPdfUploader - is invoice of " + policyNo + "'s pdf File Deleted ? "
									+ deleteFile(firstgenPolicyObj.getInvoicePdfPath()));
							firstgenPolicyObj.setIsPdfMerged(RPAConstants.Y);
							firstgenPolicyObj.setFileMergedPath(shareFolderPath+fileName);
						}
					}

				}
				
				File f = new File(shareFolderPath+fileName);
				if(f!=null && f.exists() && !f.isDirectory()) { 
					firstgenPolicyObj.setPolicyPdfUploadedTime(new Date());
					carUploadResponse = omniDocUploader.uploadDocWithOutInwardCode(
							omniUser,
							omniPassword,
							omnicabinetName,
							getEncodedFileString(shareFolderPath+fileName, new File(shareFolderPath+fileName)), 1,
							omniDocSize,
							fileName, omniDTCFolderIndex,
							omnidataDefName,
							omnidataDefId,
							omniPolicyIndex,
							policyNo,omniDocTypeIndex,
							omniAppServer);
				}else{
					logger.error("firstgenPdfUploader - File Not Found - File not available under :: "+shareFolderPath +" for policy "+policyNo);
				}
				
				
				//uploadOmniDocs( policyNo, new File(uploadFilePath+"policy_" + policyNo + ".pdf"));

				if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
					uplodedPolicyPDFCount++;
					/* To Delete uploaded Policy pdf file */
					/*logger.info("firstgenPdfUploader - is upload file of " + policyNo + "'s pdf File Deleted ? "
							+ deleteFile(uploadFilePath+fileName));*/
					firstgenPolicyObj.setIsPolicyUploaded(RPAConstants.Y);
				} else {
					if (firstgenPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
						logger.info("firstgenPdfUploader - Unable to upload policy pdf for policyNo :: "
								+ policyNo);
						firstgenPolicyObj.setIsPolicyUploaded(RPAConstants.E);
					} else {
						firstgenPolicyObj.setIsPolicyUploaded(RPAConstants.D);
					}
				}
				if(carUploadResponse!=null){
					firstgenPolicyObj.setPolicyRequest(carUploadResponse.getXmlRequest());
					firstgenPolicyObj.setPolicyResponse(carUploadResponse.getXmlResponse());
				}
			} else {
				logger.info("firstgenPdfUploader - Policy pdf already Uploaded for policy code :: " + policyNo);
			}

			
			//logger.info("firstgenPdfUploader - cabinet disconeccted status ::"+omniDocUploader.cabinetDisconnect(omnicabinetName, userDbId, omniAppServer));

			//firstgenPolicyObj.setInwardCode(inwardCode);
			firstGenDownloadPolicyService.save(firstgenPolicyObj);

		}
		/* Total policy pdf uploaded */
		transactionInfo.setTotalSuccessRecords(String.valueOf(uplodedPolicyPDFCount));
		logger.info("firstgenPdfUploader - Uploaded policy count :: " + uplodedPolicyPDFCount);

		/* Total proposal pdf uploaded */
		transactionInfo.setTotalSuccessUploads((String.valueOf(uplodedProposalPDFCount)));
		logger.info("firstgenPdfUploader - Uploaded Proposal count :: " + uplodedProposalPDFCount);

		logger.info("firstgenPdfUploader - END getPolicyNumberListTobeUploadedFromLocalDb()");
		return true;

	}

	public boolean deleteFile(String filePath) {
		boolean isFileDeleted = false;
		File file = null;
		file = new File(filePath);
		isFileDeleted = file.delete();
		file = null;
		return isFileDeleted;
	}

	public String getEncodedFileString(String filePath, File file) throws IOException {
		InputStream finput = new FileInputStream(filePath);
		byte[] imageBytes = new byte[(int) file.length()];
		finput.read(imageBytes, 0, imageBytes.length);
		finput.close();
		return Base64.encodeBase64String(imageBytes);
	}

	
	@SuppressWarnings("deprecation")
	private boolean mergePDf(String pdfOne,String pdfTwo,String destinationPdfPath) throws IOException{
		try {
		PDFMergerUtility ut = new PDFMergerUtility();
		ut.addSource(new File(pdfOne));
		ut.addSource(new File(pdfTwo));
		ut.setDestinationFileName(destinationPdfPath);
		ut.mergeDocuments();
		return true;
		}catch(Exception e){
			logger.error("firstgenPdfUploader - Error in mergePDf() :: "+e);
		}
		return false;
		
	}
	
	 public static void displayDirectoryContents(File dir) {
	      try { 
	         File[] files = dir.listFiles();
	         if(files!=null && files.length>0){
		         for (File file : files) {
		            if (file.isDirectory()) {
		            	logger.info("directory:" + file.getCanonicalPath());
		               displayDirectoryContents(file);
		            } else {
		            	logger.info("     file:" + file.getCanonicalPath());
		            } 
		         } 
	         }else{
	        	 logger.info("displayDirectoryContents no File Available in folder");
	         }
	      } catch (IOException e) {
	    	  logger.error("error in displayDirectoryContents :" +e);
	      } 
	   }
	 
	 public static void listFilesAndFolderWithSmb() throws MalformedURLException, SmbException{
		 SmbFile smb = new SmbFile("smb:\\\\inmchn242\\documents\\DBS_PDF", "smb://inmchn242/documents/DBS_PDF/");
		 SmbFile[] directoryListing = smb.listFiles(); // OK
		 if (directoryListing != null) {
				for (SmbFile sharedFile : directoryListing) {
					logger.info(
							"listFilesAndFolderWithSmb() current file name :: "
									+ sharedFile.getName());
				}
		 }
	 }
	 
	 public static void getSmbFilesWithoutCredentials() throws MalformedURLException, SmbException{
		 String path="smb://inmchn242/documents/DBS_PDF/";

		 SmbFile smbFile = new SmbFile(path);
		 String a[]=smbFile.list();
		 for(int i=0;i<a.length;i++)
		 {
		     logger.info(
						"getSmbFilesWithoutCredentials() current file name :: "
								+ a[i]);
		 }
	 }
	
}
