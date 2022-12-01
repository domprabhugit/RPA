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

import com.rpa.OmniDocClient;
import com.rpa.OmniDocsUploader;
import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.FirstgenPolicy;
import com.rpa.response.CarUploadResponse;
import com.rpa.service.CommonService;
import com.rpa.service.processors.FirstGenDownloadPolicyService;
import com.rpa.util.UtilityFile;

public class FirstgenErrorPdfUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(FirstgenErrorPdfUploader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	FirstGenDownloadPolicyService firstGenDownloadPolicyService;

	@Autowired
	OmniDocClient omniDocClient;

	@Autowired
	OmniDocsUploader omniDocUploader;

	@Autowired
	private CommonService commonService;

	@Autowired
	private Environment environment;

	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("*********** inside Camel Process of firstgenErrorPdfUploader Called ************");
		logger.info("BEGIN : firstgenErrorPdfUploader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.UPLOAD);
		FirstgenErrorPdfUploader firstgenErrorPdfUploader = new FirstgenErrorPdfUploader();
		firstGenDownloadPolicyService = applicationContext.getBean(FirstGenDownloadPolicyService.class);
		omniDocClient = applicationContext.getBean(OmniDocClient.class);
		omniDocUploader = applicationContext.getBean(OmniDocsUploader.class);
		commonService = applicationContext.getBean(CommonService.class);
		environment = applicationContext.getBean(Environment.class);
		firstgenErrorPdfUploader.doProcess(firstgenErrorPdfUploader, exchange, transactionInfo, firstGenDownloadPolicyService,
				omniDocUploader, omniDocClient, commonService, environment);
		logger.info("*********** inside Camel Process of firstgenErrorPdfUploader Processor Ended ************");
	}

	public void doProcess(FirstgenErrorPdfUploader firstgenErrorPdfUploader, Exchange exchange,
			TransactionInfo transactionInfo, FirstGenDownloadPolicyService firstGenDownloadPolicyService, OmniDocsUploader omniDocUploader,
			OmniDocClient omniDocClient, CommonService commonService,
			Environment environment) throws Exception {
		logger.info("BEGIN : firstgenErrorPdfUploader Processor - doProcess Method Called  ");
		
		getPolicyNumberListTobeUploadedFromLocalDb(transactionInfo, firstGenDownloadPolicyService, exchange, omniDocUploader,
				commonService, environment);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : firstgenErrorPdfUploader Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeUploadedFromLocalDb(TransactionInfo transactionInfo,
			FirstGenDownloadPolicyService firstGenDownloadPolicyService, Exchange exchange, OmniDocsUploader omniDocUploader,
			CommonService commonService, Environment environment) throws Exception {
		logger.info(
				"Processor - firstgenErrorPdfUploader - BEGIN getPolicyNumberListTobeUploadedFromLocalDb()  called ");

		List<FirstgenPolicy> fordPolicyNoList = null;
			fordPolicyNoList = firstGenDownloadPolicyService.findErrorPdfUploadedPolicies(
					commonService.getThresholdFrequencyLevel(RPAConstants.FIRSTGEN_UPLOAD_THRESHOLD));
		
		logger.info("firstgenErrorPdfUploader - error Policies count ::" + fordPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(fordPolicyNoList.size()));

		int uplodedPolicyPDFCount = 0,uplodedProposalPDFCount=0;

		String /*inwardCode = "",*/ policyNo = "";
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
		
		String fileName = "";
			
		for (FirstgenPolicy firstgenPolicyObj : fordPolicyNoList) {		
			policyNo = firstgenPolicyObj.getPolicyNo().replaceAll("(\\r\\n|\\n|\\r)", "");
			
			if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
				fileName = "policy_" + policyNo + ".pdf";
			}else{
				fileName = "UAT_policy_" + policyNo + ".pdf";
			}
			
			logger.info("firstgenErrorPdfUploader - policyNo ::::::::::::::::::::::::::: "+policyNo);
			
			if (firstgenPolicyObj.getIsPolicyUploaded() == null)
				firstgenPolicyObj.setIsPolicyUploaded(RPAConstants.N);


			/* policy Extraction */
			if (firstgenPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
				logger.info("firstgenErrorPdfUploader - policy not downloaded yet :: " + policyNo);
			} else if (firstgenPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.E)) {

				logger.info("firstgenErrorPdfUploader - current policy number to be uploaded " + policyNo);
				
				
				File f = new File(firstgenPolicyObj.getFileMergedPath());
				if(f!=null && f.exists() && !f.isDirectory()) { 
					firstgenPolicyObj.setPolicyPdfUploadedTime(new Date());
					carUploadResponse = omniDocUploader.uploadDocWithOutInwardCode(
							omniUser,
							omniPassword,
							omnicabinetName,
							getEncodedFileString(firstgenPolicyObj.getFileMergedPath(), new File(firstgenPolicyObj.getFileMergedPath())), 1,
							omniDocSize,
							fileName, omniDTCFolderIndex,
							omnidataDefName,
							omnidataDefId,
							omniPolicyIndex,
							policyNo,omniDocTypeIndex,
							omniAppServer);
				}else{
					logger.error("firstgenErrorPdfUploader - File Not Found - File not available:: "+firstgenPolicyObj.getFileMergedPath());
				}
				
				
				//uploadOmniDocs( policyNo, new File(uploadFilePath+"policy_" + policyNo + ".pdf"));

				if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
					uplodedPolicyPDFCount++;
					/* To Delete uploaded Policy pdf file */
					logger.info("firstgenErrorPdfUploader - is upload file of " + policyNo + "'s pdf File Deleted ? "
							+ deleteFile(firstgenPolicyObj.getFileMergedPath()));
					firstgenPolicyObj.setIsPolicyUploaded(RPAConstants.Y);
				} else {
					if (firstgenPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.E)) {
						logger.info("firstgenErrorPdfUploader - Unable to upload policy pdf through errpdf uploader too - policyNo :: "
								+ policyNo);
						firstgenPolicyObj.setIsPolicyUploaded("X");
					} 
				}
				if(carUploadResponse!=null){
					firstgenPolicyObj.setPolicyRequest(carUploadResponse.getXmlRequest());
					firstgenPolicyObj.setPolicyResponse(carUploadResponse.getXmlResponse());
				}
			} else {
				logger.info("firstgenErrorPdfUploader - Policy pdf already Uploaded for policy code :: " + policyNo);
			}

			
			//logger.info("firstgenErrorPdfUploader - cabinet disconeccted status ::"+omniDocUploader.cabinetDisconnect(omnicabinetName, userDbId, omniAppServer));

			//firstgenPolicyObj.setInwardCode(inwardCode);
			firstGenDownloadPolicyService.save(firstgenPolicyObj);

		}
		/* Total policy pdf uploaded */
		transactionInfo.setTotalSuccessRecords(String.valueOf(uplodedPolicyPDFCount));
		logger.info("firstgenErrorPdfUploader - Uploaded policy count :: " + uplodedPolicyPDFCount);

		/* Total proposal pdf uploaded */
		transactionInfo.setTotalSuccessUploads((String.valueOf(uplodedProposalPDFCount)));
		logger.info("firstgenErrorPdfUploader - Uploaded Proposal count :: " + uplodedProposalPDFCount);

		logger.info("firstgenErrorPdfUploader - END getPolicyNumberListTobeUploadedFromLocalDb()");
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

	
}
