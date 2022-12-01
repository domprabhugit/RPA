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
import java.net.URISyntaxException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
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
import com.rpa.model.processors.FordPolicy;
import com.rpa.response.CarUploadResponse;
import com.rpa.service.CommonService;
import com.rpa.service.processors.FordPolicyService;
import com.rpa.util.UtilityFile;

public class FordPolicyPdfUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(FordPolicyPdfUploader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	FordPolicyService fordPolicyService;

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
		logger.info("*********** inside Camel Process of fordPolicyPdfUploader Called ************");
		logger.info("BEGIN : fordPolicyPdfUploader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.UPLOAD);
		FordPolicyPdfUploader fordPolicyPdfUploader = new FordPolicyPdfUploader();
		fordPolicyService = applicationContext.getBean(FordPolicyService.class);
		jax2MarshallerGenerater = applicationContext.getBean(Jax2MarshallerGenerater.class);
		Jaxb2Marshaller jaxb2Marshaller = jax2MarshallerGenerater.getJax2MarshallerObject();
		omniDocClient = applicationContext.getBean(OmniDocClient.class);
		omniDocUploader = applicationContext.getBean(OmniDocsUploader.class);
		commonService = applicationContext.getBean(CommonService.class);
		environment = applicationContext.getBean(Environment.class);
		fordPolicyPdfUploader.doProcess(fordPolicyPdfUploader, exchange, transactionInfo, fordPolicyService,
				omniDocUploader, omniDocClient, jaxb2Marshaller, commonService, environment);
		logger.info("*********** inside Camel Process of fordPolicyPdfUploader Processor Ended ************");
	}

	public void doProcess(FordPolicyPdfUploader fordPolicyPdfUploader, Exchange exchange,
			TransactionInfo transactionInfo, FordPolicyService fordPolicyService, OmniDocsUploader omniDocUploader,
			OmniDocClient omniDocClient, Jaxb2Marshaller marshaller, CommonService commonService,
			Environment environment) throws Exception {
		logger.info("BEGIN : fordPolicyPdfUploader Processor - doProcess Method Called  ");
		
		getPolicyNumberListTobeUploadedFromLocalDb(transactionInfo, fordPolicyService, exchange, omniDocUploader,
				commonService, environment);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : fordPolicyPdfUploader Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeUploadedFromLocalDb(TransactionInfo transactionInfo,
			FordPolicyService fordPolicyService, Exchange exchange, OmniDocsUploader omniDocUploader,
			CommonService commonService, Environment environment) throws Exception {
		logger.info(
				"Processor - fordPolicyPdfUploader - BEGIN getPolicyNumberListTobeUploadedFromLocalDb()  called ");

		List<FordPolicy> fordPolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			fordPolicyNoList = fordPolicyService.findPdfUnUploadedPolicies(
					commonService.getThresholdFrequencyLevel(RPAConstants.FORD_UPLOAD_THRESHOLD));
		} else {
			fordPolicyNoList = fordPolicyService.findPdfUnUploadedBackLogPolicies(
					UtilityFile.getCarPolicyProperty(RPAConstants.FORD_BACKLOG_STARTDATE),
					UtilityFile.getCarPolicyProperty(RPAConstants.FORD_BACKLOG_ENDDATE),
					commonService.getThresholdFrequencyLevel(RPAConstants.FORD_UPLOAD_THRESHOLD));
		}
		logger.info("fordPolicyPdfUploader - UnUploaded Policies count ::" + fordPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(fordPolicyNoList.size()));

		int uplodedPolicyPDFCount = 0, uplodedProposalPDFCount = 0;

		String inwardCode = "", policyNo = "", proposalNumber = "", filePath = "",
				inwardFolderIndex="",proposaFolderIndex="",userDbId="",paramValue="";
		CarUploadResponse carUploadResponse = null;
		
		String omniUser="",omniPassword="",omnicabinetName="",omnidataDefName="",omnidataDefId="",omniPolicyIndex="",omniProposalIndex="",omniInwardIndex="",
				omniAppServer="",omniDocSize="",omniOEMFolderIndex="";
		
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
			omniUser = UtilityFile.getOmniDocLiveProperty(RPAConstants.FORD_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocLiveProperty(RPAConstants.FORD_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_FORD_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_FORD_PREFIX_LIVE;
		}else{
			omniUser = UtilityFile.getOmniDocProperty(RPAConstants.FORD_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocProperty(RPAConstants.FORD_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_FORD_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_FORD_PREFIX;
		}
		
		userDbId = omniDocUploader.cabinetConnect(omniUser,
				omniPassword,
				omnicabinetName, omniAppServer);
		
		logger.info("fordPolicyPdfUploader - userDbId ::::::::::::::::::::::::::: "+userDbId);
		try{
			for (FordPolicy fordPolicyObj : fordPolicyNoList) {		
				
				policyNo = fordPolicyObj.getPolicyNo();
				proposalNumber = fordPolicyObj.getProposalNumber();
				
				
			
				
			
				if (fordPolicyObj.getInwardCode() == null) {
					inwardCode = getNextInwardCode(paramValue);
					if(inwardCode==null)
						throw new Exception("Entry Not available in RPA_NUM_CTRL table for param_id :: "+paramValue);
				} else {
					inwardCode = fordPolicyObj.getInwardCode();
				}
				
				logger.info("fordPolicyPdfUploader - inwardCode ::::::::::::::::::::::::::: "+inwardCode);
				logger.info("fordPolicyPdfUploader - policyNo ::::::::::::::::::::::::::: "+policyNo);
				logger.info("fordPolicyPdfUploader - proposalNumber ::::::::::::::::::::::::::: "+proposalNumber);
				
				
				if(fordPolicyObj.getInwardFolderIndex()==null){
					inwardFolderIndex = omniDocUploader.addFolder(userDbId, omniOEMFolderIndex, inwardCode, omnicabinetName,omniAppServer);
					fordPolicyObj.setInwardFolderIndex(inwardFolderIndex);
				}else{
					inwardFolderIndex = fordPolicyObj.getInwardFolderIndex();
				}
				logger.info("fordPolicyPdfUploader - inwardFolderIndex ::::::::::::::::::::::::::: "+inwardFolderIndex);
				
				if(fordPolicyObj.getProposalFolderIndex()==null){
					proposaFolderIndex = omniDocUploader.addFolder(userDbId, inwardFolderIndex, proposalNumber, omnicabinetName,omniAppServer);
					fordPolicyObj.setProposalFolderIndex(proposaFolderIndex);
				}else{
					proposaFolderIndex = fordPolicyObj.getProposalFolderIndex();
				}
				logger.info("fordPolicyPdfUploader - proposaFolderIndex ::::::::::::::::::::::::::: "+proposaFolderIndex);
	
				if (fordPolicyObj.getIsPolicyUploaded() == null)
					fordPolicyObj.setIsPolicyUploaded(RPAConstants.N);
	
				filePath = "";
	
				/* policy Extraction */
				if (fordPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
					logger.info("fordPolicyPdfUploader - policy not downloaded yet :: " + policyNo);
				} else if (fordPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.R)) {
					logger.info("fordPolicyPdfUploader - No Record came while downloaded :: " + policyNo);
				} else if (fordPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.N)
						|| fordPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
					carUploadResponse = null;
					logger.info("fordPolicyPdfUploader - current policy number to be uploaded " + policyNo);
					filePath = fordPolicyObj.getPolicyPdfPath();
					if(filePath!=null){
						File policyFile = new File(filePath);
						if(policyFile!=null && policyFile.exists() && !policyFile.isDirectory()) { 
							logger.info("fordPolicyPdfUploader - Policy File available in : " + filePath);
							try{
								fordPolicyObj.setPolicyPdfUploadedTime(new Date());
							carUploadResponse = omniDocUploader.uploadDoc(
									omniUser,
									omniPassword,
									omnicabinetName,
									getEncodedFileString(filePath, new File(filePath)), 1,
									omniDocSize,
									"policy_" + policyNo + ".pdf", proposaFolderIndex,
									omnidataDefName,
									omnidataDefId,
									omniPolicyIndex,
									policyNo, inwardCode,
									omniInwardIndex,
									omniAppServer);
							}catch(Exception e){
								if(carUploadResponse!=null){
								carUploadResponse.setStatusCode("E");
								}
								logger.error("fordPolicyPdfUploader - error while uploading policy doc :: "+e);
							}
						}else{
							logger.error("fordPolicyPdfUploader - policy  - "+policyNo+" file not available in " + filePath +"to upload");
							fordPolicyObj.setIsPolicyDownloaded(RPAConstants.N);
						}
					}else{
						logger.error("fordPolicyPdfUploader - file path is empty");
					}
					
					if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
						logger.info("fordPolicyPdfUploader - policy status code :: "+carUploadResponse.getStatusCode());
						uplodedPolicyPDFCount++;
						/* To Delete Policy pdf file */
						logger.info("fordPolicyPdfUploader - is " + policyNo + "'s pdf File Deleted ? "
								+ deleteFile(fordPolicyObj.getPolicyPdfPath()));
						fordPolicyObj.setIsPolicyUploaded(RPAConstants.Y);
					} else {
						if (fordPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
							logger.info("fordPolicyPdfUploader - Unable to upload policy pdf for proposalNo retry 2 :: "
									+ proposalNumber);
							fordPolicyObj.setIsPolicyUploaded(RPAConstants.E);
						} else {
							logger.info("fordPolicyPdfUploader - Unable to upload policy pdf for proposalNo retry 1 :: "
									+ proposalNumber);
							fordPolicyObj.setIsPolicyUploaded(RPAConstants.D);
						}
					}
					if(carUploadResponse!=null){
						fordPolicyObj.setPolicyRequest(carUploadResponse.getXmlRequest());
						fordPolicyObj.setPolicyResponse(carUploadResponse.getXmlResponse());
					}
				} else {
					logger.info("fordPolicyPdfUploader - Policy pdf already Uploaded for policy code :: " + policyNo);
				}
	
				if (fordPolicyObj.getIsProposalUploaded() == null)
					fordPolicyObj.setIsProposalUploaded(RPAConstants.N);
	
				/* proposal form extraction */
				if (fordPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.N)) {
					logger.info("fordPolicyPdfUploader - proposal not downloaded yet :: " + proposalNumber);
				} else if (fordPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.R)) {
					logger.info("fordPolicyPdfUploader - No Record came while downloaded :: " + proposalNumber);
				} else if (fordPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.N)
						|| fordPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
					carUploadResponse = null;
					filePath = fordPolicyObj.getProposalPdfPath();
					logger.info("fordPolicyPdfUploader - current proposal number to be uploaded " + proposalNumber);
	
					if(filePath!=null){
						File proposalFile = new File(filePath);
						if(proposalFile!=null && proposalFile.exists() && !proposalFile.isDirectory()) { 
							carUploadResponse = null;
							logger.info("fordPolicyPdfUploader - Proposal File available in : " + filePath);
							try{
								fordPolicyObj.setProposalPdfUploadedTime(new Date());
							carUploadResponse = omniDocUploader.uploadDoc(
									omniUser,
									omniPassword,
									omnicabinetName,
									getEncodedFileString(filePath, new File(filePath)), 1,
									omniDocSize,
									"proposal_" + proposalNumber + ".pdf", proposaFolderIndex,
									omnidataDefName,
									omnidataDefId,
									omniProposalIndex,
									proposalNumber, inwardCode,
									omniInwardIndex,
									omniAppServer);
							}catch(Exception e){
								if(carUploadResponse!=null){
								carUploadResponse.setStatusCode("E");
								}
								logger.error("fordPolicyPdfUploader - error while uploading proposal doc :: "+e);
							}
						}else{
							logger.error("fordPolicyPdfUploader - proposal  - "+proposalNumber+" file not available in " + filePath +"to upload");
							fordPolicyObj.setIsProposalDownloaded(RPAConstants.N);
						}
					}else{
						logger.error("fordPolicyPdfUploader - file path is empty");
					}
					
					if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
						logger.info("fordPolicyPdfUploader - proposal status code :: "+carUploadResponse.getStatusCode());
						uplodedProposalPDFCount++;
						/* To Delete Proposal pdf file */
						logger.info("fordPolicyPdfUploader - is " + proposalNumber + "'s pdf File Deleted ? "
								+ deleteFile(fordPolicyObj.getProposalPdfPath()));
						fordPolicyObj.setIsProposalUploaded(RPAConstants.Y);
					} else {
						if (fordPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
							logger.info("fordPolicyPdfUploader - Unable to upload policy pdf for proposalNo in retry 2 :: "
									+ proposalNumber);
							fordPolicyObj.setIsProposalUploaded(RPAConstants.E);
						} else {
							logger.info("fordPolicyPdfUploader - Unable to upload policy pdf for proposalNo in retry 1 :: "
									+ proposalNumber);
							fordPolicyObj.setIsProposalUploaded(RPAConstants.D);
						}
					}
					if(carUploadResponse!=null){
						fordPolicyObj.setProposalRequest(carUploadResponse.getXmlRequest());
						fordPolicyObj.setProposalResponse(carUploadResponse.getXmlResponse());
					}
				} else {
					logger.info("fordPolicyPdfUploader - Proposal pdf already Uploaded for Proposal code :: "
							+ proposalNumber);
				}
	
				fordPolicyObj.setInwardCode(inwardCode);
				fordPolicyService.save(fordPolicyObj);
	
			}
			}finally{
				if(!userDbId.equals(""))
					logger.info("fordPolicyPdfUploader - cabinet disconeccted status ::"+omniDocUploader.cabinetDisconnect(omnicabinetName, userDbId, omniAppServer));
			}
		
		
		
		
		/* Total policy pdf uploaded */
		transactionInfo.setTotalSuccessRecords(String.valueOf(uplodedPolicyPDFCount));
		logger.info("fordPolicyPdfUploader - Uploaded policy count :: " + uplodedPolicyPDFCount);

		/* Total proposal pdf uploaded */
		transactionInfo.setTotalSuccessUploads((String.valueOf(uplodedProposalPDFCount)));
		logger.info("fordPolicyPdfUploader - Uploaded Proposal count :: " + uplodedProposalPDFCount);

		logger.info("fordPolicyPdfUploader - END getPolicyNumberListTobeUploadedFromLocalDb()");
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

	private String getNextInwardCode(String paramValue) throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("fordPolicyPdfUploader - BEGIN getNextInwardCode() called");
		Connection conn = UtilityFile.getLocalRPAConnection();
		String nextInwardCode = "";
		CallableStatement callStmt = null;
		try {
			callStmt = conn.prepareCall("CALL UPDATE_MAX_ID(?,?)");
			callStmt.setString(1, paramValue);
			callStmt.registerOutParameter(2, Types.VARCHAR);
			callStmt.execute();
			nextInwardCode = callStmt.getString(2);
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (callStmt != null) {
				callStmt.close();
			}
		}
		logger.info("fordPolicyPdfUploader - END downloadPdf() nextInwardCode ::" + nextInwardCode);
		return nextInwardCode;
	}
}
