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
import com.rpa.model.processors.APPVIR;
import com.rpa.response.CarUploadResponse;
import com.rpa.service.CommonService;
import com.rpa.service.processors.AppVirService;
import com.rpa.util.UtilityFile;

public class VirDocUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(VirDocUploader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	AppVirService autoInspektVirService;

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
		logger.info("*********** inside Camel Process of autoInspektDocUploader Called ************");
		logger.info("BEGIN : autoInspektDocUploader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.UPLOAD);
		VirDocUploader autoInspektDocUploader = new VirDocUploader();
		autoInspektVirService = applicationContext.getBean(AppVirService.class);
		jax2MarshallerGenerater = applicationContext.getBean(Jax2MarshallerGenerater.class);
		Jaxb2Marshaller jaxb2Marshaller = jax2MarshallerGenerater.getJax2MarshallerObject();
		omniDocClient = applicationContext.getBean(OmniDocClient.class);
		omniDocUploader = applicationContext.getBean(OmniDocsUploader.class);
		commonService = applicationContext.getBean(CommonService.class);
		environment = applicationContext.getBean(Environment.class);
		autoInspektDocUploader.doProcess(autoInspektDocUploader, exchange, transactionInfo, autoInspektVirService,
				omniDocUploader, omniDocClient, jaxb2Marshaller, commonService, environment);
		logger.info("*********** inside Camel Process of autoInspektDocUploader Processor Ended ************");
	}

	public void doProcess(VirDocUploader autoInspektDocUploader, Exchange exchange,
			TransactionInfo transactionInfo, AppVirService autoInspektVirService, OmniDocsUploader omniDocUploader,
			OmniDocClient omniDocClient, Jaxb2Marshaller marshaller, CommonService commonService,
			Environment environment) throws Exception {
		logger.info("BEGIN : autoInspektDocUploader Processor - doProcess Method Called  ");
		
		getPolicyNumberListTobeUploadedFromLocalDb(transactionInfo, autoInspektVirService, exchange, omniDocUploader,
				commonService, environment);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : autoInspektDocUploader Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeUploadedFromLocalDb(TransactionInfo transactionInfo,
			AppVirService autoInspektVirService, Exchange exchange, OmniDocsUploader omniDocUploader,
			CommonService commonService, Environment environment) throws Exception {
		logger.info(
				"Processor - autoInspektDocUploader - BEGIN getPolicyNumberListTobeUploadedFromLocalDb()  called ");

		List<APPVIR> abiblPolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			abiblPolicyNoList = autoInspektVirService.findPdfUnUploadedPolicies(
					commonService.getThresholdFrequencyLevel(RPAConstants.VIRAPP_UPLOAD_THRESHOLD));
		} else {
			abiblPolicyNoList = autoInspektVirService.findPdfUnUploadedBackLogPolicies(
					UtilityFile.getCarPolicyProperty(RPAConstants.VIRAPP_BACKLOG_STARTDATE),
					UtilityFile.getCarPolicyProperty(RPAConstants.VIRAPP_BACKLOG_ENDDATE),
					commonService.getThresholdFrequencyLevel(RPAConstants.VIRAPP_UPLOAD_THRESHOLD));
		}
		logger.info("autoInspektDocUploader - UnUploaded Policies count ::" + abiblPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(abiblPolicyNoList.size()));

		int uplodedReportCount = 0, uplodedProposalPDFCount = 0;

		String inwardCode = "", policyNo = "", proposalNumber = "", filePath = "",
				inwardFolderIndex="",proposaFolderIndex="",userDbId="",paramValue="",virNumber="",regNo="";
		CarUploadResponse carUploadResponse = null;
		
		String omniUser="",omniPassword="",omnicabinetName="",omnidataDefName="",omnidataDefId="",omniPolicyIndex="",omniProposalIndex="",omniInwardIndex="",
				omniAppServer="",omniDocSize="",omniOEMFolderIndex="";
		
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
			omniUser = UtilityFile.getOmniDocLiveProperty(RPAConstants.VIRAPP_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocLiveProperty(RPAConstants.VIRAPP_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_VIRAPP_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_AUTOINSPECT_PREFIX_LIVE;
		}else{
			omniUser = UtilityFile.getOmniDocProperty(RPAConstants.VIRAPP_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocProperty(RPAConstants.VIRAPP_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_VIRAPP_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_AUTOINSPECT_PREFIX;
		}
		
		userDbId = omniDocUploader.cabinetConnect(omniUser,
				omniPassword,
				omnicabinetName, omniAppServer);

		logger.info("autoInspektDocUploader - userDbId ::::::::::::::::::::::::::: "+userDbId);
		
		try{
		
			for (APPVIR autoInspektVirObj : abiblPolicyNoList) {	
				
				policyNo = autoInspektVirObj.getPolicyNo();
				proposalNumber = autoInspektVirObj.getProposalNumber();
				virNumber = autoInspektVirObj.getVirNumber();
				regNo = autoInspektVirObj.getRegistrationNumber();
				
				/*if(autoInspektVirObj.getInwardFolderIndex()==null || autoInspektVirObj.getProposalFolderIndex()==null){
					
				}*/
				
					if (autoInspektVirObj.getInwardCode() == null) {
						autoInspektVirObj.setIsReportUploaded("X");
					} else {
						inwardCode = autoInspektVirObj.getInwardCode();
					}
					
					if (proposalNumber == null || proposalNumber.equals("") ) {
						autoInspektVirObj.setIsReportUploaded("X");
					}
					
					logger.info("autoInspektDocUploader - inwardCode ::::::::::::::::::::::::::: "+inwardCode);
					logger.info("autoInspektDocUploader - policyNo ::::::::::::::::::::::::::: "+policyNo);
					logger.info("autoInspektDocUploader - proposalNumber ::::::::::::::::::::::::::: "+proposalNumber);
					logger.info("autoInspektDocUploader - virNumber ::::::::::::::::::::::::::: "+virNumber);
					logger.info("autoInspektDocUploader - regNo ::::::::::::::::::::::::::: "+regNo);
					
					if(autoInspektVirObj.getInwardFolderIndex()==null){
						inwardFolderIndex = omniDocUploader.addFolder(userDbId, omniOEMFolderIndex, inwardCode, omnicabinetName,omniAppServer);
						autoInspektVirObj.setInwardFolderIndex(inwardFolderIndex);
					}else{
						inwardFolderIndex = autoInspektVirObj.getInwardFolderIndex();
					}
					logger.info("autoInspektDocUploader - inwardFolderIndex ::::::::::::::::::::::::::: "+inwardFolderIndex);
					
					if(autoInspektVirObj.getProposalFolderIndex()==null){
						proposaFolderIndex = omniDocUploader.addFolder(userDbId, inwardFolderIndex, proposalNumber, omnicabinetName,omniAppServer);
						autoInspektVirObj.setProposalFolderIndex(proposaFolderIndex);
					}else{
						proposaFolderIndex = autoInspektVirObj.getProposalFolderIndex();
					}
					logger.info("autoInspektDocUploader - proposaFolderIndex ::::::::::::::::::::::::::: "+proposaFolderIndex);
		
					if (autoInspektVirObj.getIsReportUploaded() == null)
						autoInspektVirObj.setIsReportUploaded(RPAConstants.N);
		
					filePath = "";
		
					/* policy Extraction */
					if (autoInspektVirObj.getIsReportDownloaded().equalsIgnoreCase(RPAConstants.N)) {
						logger.info("autoInspektDocUploader - vir doc  not downloaded yet for regNo :: " + regNo);
					} else if (autoInspektVirObj.getIsReportDownloaded().equalsIgnoreCase(RPAConstants.R)) {
						logger.info("autoInspektDocUploader - No Record came while downloaded vir doc for regNo :: " + regNo);
					} else if (autoInspektVirObj.getIsReportDownloaded().equalsIgnoreCase("X")) {
						logger.info("autoInspektDocUploader - Inward / Proposal No is not available to upload for regno :: " + regNo);
					} else if (autoInspektVirObj.getIsReportUploaded().equalsIgnoreCase(RPAConstants.N)
							|| autoInspektVirObj.getIsReportUploaded().equalsIgnoreCase(RPAConstants.D)) {
						carUploadResponse = null;
						logger.info("autoInspektDocUploader - current policy number to be uploaded " + policyNo);
						
						filePath = autoInspektVirObj.getReportPdfPath();
						if(filePath!=null){
						File policyFile = new File(filePath);
						if(policyFile!=null && policyFile.exists() && !policyFile.isDirectory()) { 
							logger.info("autoInspektDocUploader - Report File available in : " + filePath);
							try{
								autoInspektVirObj.setReportUploadedTime(new Date());
							
								carUploadResponse = omniDocUploader.uploadDoc(
										omniUser,
										omniPassword,
										omnicabinetName,
										getEncodedFileString(filePath, new File(filePath)), 1,
										omniDocSize,
										"Report_" + virNumber + ".pdf", proposaFolderIndex,
										omnidataDefName,
										omnidataDefId,
										omniPolicyIndex,
										policyNo, inwardCode,
										omniInwardIndex,
										omniAppServer);
								}
								catch(Exception e){
									if(carUploadResponse!=null){
									carUploadResponse.setStatusCode("E");
									}
									logger.error("autoInspektDocUploader - error while uploading policy doc :: "+e);
								}
							}else{
								logger.error("autoInspektDocUploader - policy  - "+policyNo+" file not available in " + filePath +"to upload");
								autoInspektVirObj.setIsReportDownloaded(RPAConstants.N);
							}
						}else{
							logger.error("autoInspektDocUploader - file path is empty");
						}
						
						if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
							logger.info("autoInspektDocUploader - policy status code :: "+carUploadResponse.getStatusCode());
							uplodedReportCount++;
							/* To Delete Report pdf file */
							logger.info("autoInspektDocUploader - is " + policyNo + "'s pdf File Deleted ? "
									+ deleteFile(autoInspektVirObj.getReportPdfPath()));
							autoInspektVirObj.setIsReportUploaded(RPAConstants.Y);
						} else {
							if (autoInspektVirObj.getIsReportUploaded().equalsIgnoreCase(RPAConstants.D)) {
								logger.info("autoInspektDocUploader - Unable to upload policy pdf for proposalNo :: "
										+ proposalNumber);
								autoInspektVirObj.setIsReportUploaded(RPAConstants.E);
							} else {
								autoInspektVirObj.setIsReportUploaded(RPAConstants.D);
							}
						}
						if(carUploadResponse!=null){
							autoInspektVirObj.setReportRequest(carUploadResponse.getXmlRequest());
							autoInspektVirObj.setReportResponse(carUploadResponse.getXmlResponse());
						}
					} else {
						logger.info("autoInspektDocUploader - Report pdf already Uploaded for policy code :: " + policyNo);
					}
		
					autoInspektVirObj.setInwardCode(inwardCode);
					autoInspektVirService.save(autoInspektVirObj);
					
				}
			}
		finally{
			if(!userDbId.equals(""))
				logger.info("autoInspektDocUploader - cabinet disconeccted status ::"+omniDocUploader.cabinetDisconnect(omnicabinetName, userDbId, omniAppServer));
		}
		
		/* Total policy pdf uploaded */
		transactionInfo.setTotalSuccessRecords(String.valueOf(uplodedReportCount));
		logger.info("autoInspektDocUploader - Uploaded policy count :: " + uplodedReportCount);

		/* Total proposal pdf uploaded */
		transactionInfo.setTotalSuccessUploads((String.valueOf(uplodedProposalPDFCount)));
		logger.info("autoInspektDocUploader - Uploaded Proposal count :: " + uplodedProposalPDFCount);

		logger.info("autoInspektDocUploader - END getPolicyNumberListTobeUploadedFromLocalDb()");
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
