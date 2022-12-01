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
import com.rpa.model.processors.TafePolicy;
import com.rpa.response.CarUploadResponse;
import com.rpa.service.CommonService;
import com.rpa.service.processors.TafePolicyService;
import com.rpa.util.UtilityFile;

public class TafePolicyPdfUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(TafePolicyPdfUploader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	TafePolicyService tafePolicyService;

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
		logger.info("*********** inside Camel Process of tafePolicyPdfUploader Called ************");
		logger.info("BEGIN : tafePolicyPdfUploader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.UPLOAD);
		TafePolicyPdfUploader tafePolicyPdfUploader = new TafePolicyPdfUploader();
		tafePolicyService = applicationContext.getBean(TafePolicyService.class);
		jax2MarshallerGenerater = applicationContext.getBean(Jax2MarshallerGenerater.class);
		Jaxb2Marshaller jaxb2Marshaller = jax2MarshallerGenerater.getJax2MarshallerObject();
		omniDocClient = applicationContext.getBean(OmniDocClient.class);
		omniDocUploader = applicationContext.getBean(OmniDocsUploader.class);
		commonService = applicationContext.getBean(CommonService.class);
		environment = applicationContext.getBean(Environment.class);
		tafePolicyPdfUploader.doProcess(tafePolicyPdfUploader, exchange, transactionInfo, tafePolicyService,
				omniDocUploader, omniDocClient, jaxb2Marshaller, commonService, environment);
		logger.info("*********** inside Camel Process of tafePolicyPdfUploader Processor Ended ************");
	}

	public void doProcess(TafePolicyPdfUploader tafePolicyPdfUploader, Exchange exchange,
			TransactionInfo transactionInfo, TafePolicyService tafePolicyService, OmniDocsUploader omniDocUploader,
			OmniDocClient omniDocClient, Jaxb2Marshaller marshaller, CommonService commonService,
			Environment environment) throws Exception {
		logger.info("BEGIN : tafePolicyPdfUploader Processor - doProcess Method Called  ");
		
		getPolicyNumberListTobeUploadedFromLocalDb(transactionInfo, tafePolicyService, exchange, omniDocUploader,
				commonService, environment);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : tafePolicyPdfUploader Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeUploadedFromLocalDb(TransactionInfo transactionInfo,
			TafePolicyService tafePolicyService, Exchange exchange, OmniDocsUploader omniDocUploader,
			CommonService commonService, Environment environment) throws Exception {
		logger.info(
				"Processor - tafePolicyPdfUploader - BEGIN getPolicyNumberListTobeUploadedFromLocalDb()  called ");

		List<TafePolicy> tafePolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			tafePolicyNoList = tafePolicyService.findPdfUnUploadedPolicies(
					commonService.getThresholdFrequencyLevel(RPAConstants.TAFE_UPLOAD_THRESHOLD));
		} else {
			tafePolicyNoList = tafePolicyService.findPdfUnUploadedBackLogPolicies(
					UtilityFile.getCarPolicyProperty(RPAConstants.TAFE_BACKLOG_STARTDATE),
					UtilityFile.getCarPolicyProperty(RPAConstants.TAFE_BACKLOG_ENDDATE),
					commonService.getThresholdFrequencyLevel(RPAConstants.TAFE_UPLOAD_THRESHOLD));
		}
		logger.info("tafePolicyPdfUploader - UnUploaded Policies count ::" + tafePolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(tafePolicyNoList.size()));

		int uplodedPolicyPDFCount = 0, uplodedProposalPDFCount = 0;

		String inwardCode = "", policyNo = "", proposalNumber = "", filePath = "",
				inwardFolderIndex="",proposaFolderIndex="",userDbId="",paramValue="";
		CarUploadResponse carUploadResponse = null;
		
		String omniUser="",omniPassword="",omnicabinetName="",omnidataDefName="",omnidataDefId="",omniPolicyIndex="",omniProposalIndex="",omniInwardIndex="",
				omniAppServer="",omniDocSize="",omniOEMFolderIndex="";
		
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
			omniUser = UtilityFile.getOmniDocLiveProperty(RPAConstants.TAFE_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocLiveProperty(RPAConstants.TAFE_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_TAFE_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_TAFE_PREFIX_LIVE;
		}else{
			omniUser = UtilityFile.getOmniDocProperty(RPAConstants.TAFE_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocProperty(RPAConstants.TAFE_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_TAFE_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_TAFE_PREFIX;
		}
		
		userDbId = omniDocUploader.cabinetConnect(omniUser,
				omniPassword,
				omnicabinetName, omniAppServer);
		
		logger.info("tafePolicyPdfUploader - userDbId ::::::::::::::::::::::::::: "+userDbId);
		try{
		
			for (TafePolicy tafePolicyObj : tafePolicyNoList) {		
				
				policyNo = tafePolicyObj.getPolicyNo();
				proposalNumber = tafePolicyObj.getProposalNumber();
				
				/*if(tafePolicyObj.getInwardFolderIndex()==null || tafePolicyObj.getProposalFolderIndex()==null){
					
				}*/
				
				if (tafePolicyObj.getInwardCode() == null) {
					inwardCode = getNextInwardCode(paramValue);
					if(inwardCode==null)
						throw new Exception("Entry Not available in RPA_NUM_CTRL table for param_id :: "+paramValue);
				} else {
					inwardCode = tafePolicyObj.getInwardCode();
				}
				
				logger.info("tafePolicyPdfUploader - inwardCode ::::::::::::::::::::::::::: "+inwardCode);
				logger.info("tafePolicyPdfUploader - policyNo ::::::::::::::::::::::::::: "+policyNo);
				logger.info("tafePolicyPdfUploader - proposalNumber ::::::::::::::::::::::::::: "+proposalNumber);
				
				if(tafePolicyObj.getInwardFolderIndex()==null){
					inwardFolderIndex = omniDocUploader.addFolder(userDbId, omniOEMFolderIndex, inwardCode, omnicabinetName,omniAppServer);
					tafePolicyObj.setInwardFolderIndex(inwardFolderIndex);
				}else{
					inwardFolderIndex = tafePolicyObj.getInwardFolderIndex();
				}
				logger.info("tafePolicyPdfUploader - inwardFolderIndex ::::::::::::::::::::::::::: "+inwardFolderIndex);
				
				if(tafePolicyObj.getProposalFolderIndex()==null){
					proposaFolderIndex = omniDocUploader.addFolder(userDbId, inwardFolderIndex, proposalNumber, omnicabinetName,omniAppServer);
					tafePolicyObj.setProposalFolderIndex(proposaFolderIndex);
				}else{
					proposaFolderIndex = tafePolicyObj.getProposalFolderIndex();
				}
				logger.info("tafePolicyPdfUploader - proposaFolderIndex ::::::::::::::::::::::::::: "+proposaFolderIndex);
	
				if (tafePolicyObj.getIsPolicyUploaded() == null)
					tafePolicyObj.setIsPolicyUploaded(RPAConstants.N);
	
				filePath = "";
	
				/* policy Extraction */
				if (tafePolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
					logger.info("tafePolicyPdfUploader - policy not downloaded yet :: " + policyNo);
				} else if (tafePolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.R)) {
					logger.info("tafePolicyPdfUploader - No Record came while downloaded :: " + policyNo);
				} else if (tafePolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.N)
						|| tafePolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
					carUploadResponse = null;
					logger.info("tafePolicyPdfUploader - current policy number to be uploaded " + policyNo);
					
					filePath = tafePolicyObj.getPolicyPdfPath();
					if(filePath!=null){
					File policyFile = new File(filePath);
					if(policyFile!=null && policyFile.exists() && !policyFile.isDirectory()) { 
						logger.info("tafePolicyPdfUploader - Policy File available in : " + filePath);
						try{
							tafePolicyObj.setPolicyPdfUploadedTime(new Date());
						
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
							}
							catch(Exception e){
								if(carUploadResponse!=null){
								carUploadResponse.setStatusCode("E");
								}
								logger.error("tafePolicyPdfUploader - error while uploading policy doc :: "+e);
							}
						}else{
							logger.error("tafePolicyPdfUploader - policy  - "+policyNo+" file not available in " + filePath +"to upload");
							tafePolicyObj.setIsPolicyDownloaded(RPAConstants.N);
						}
					}else{
						logger.error("tafePolicyPdfUploader - file path is empty");
					}
					
					if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
						logger.info("tafePolicyPdfUploader - policy status code :: "+carUploadResponse.getStatusCode());
						uplodedPolicyPDFCount++;
						/* To Delete Policy pdf file */
						logger.info("tafePolicyPdfUploader - is " + policyNo + "'s pdf File Deleted ? "
								+ deleteFile(tafePolicyObj.getPolicyPdfPath()));
						tafePolicyObj.setIsPolicyUploaded(RPAConstants.Y);
					} else {
						if (tafePolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
							logger.info("tafePolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
									+ proposalNumber);
							tafePolicyObj.setIsPolicyUploaded(RPAConstants.E);
						} else {
							tafePolicyObj.setIsPolicyUploaded(RPAConstants.D);
						}
					}
					if(carUploadResponse!=null){
						tafePolicyObj.setPolicyRequest(carUploadResponse.getXmlRequest());
						tafePolicyObj.setPolicyResponse(carUploadResponse.getXmlResponse());
					}
				} else {
					logger.info("tafePolicyPdfUploader - Policy pdf already Uploaded for policy code :: " + policyNo);
				}
	
				if (tafePolicyObj.getIsProposalUploaded() == null)
					tafePolicyObj.setIsProposalUploaded(RPAConstants.N);
	
				/* proposal form extraction */
				if (tafePolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.N)) {
					logger.info("tafePolicyPdfUploader - proposal not downloaded yet :: " + proposalNumber);
				} else if (tafePolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.R)) {
					logger.info("tafePolicyPdfUploader - No Record came while downloaded :: " + proposalNumber);
				} else if (tafePolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.N)
						|| tafePolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
					carUploadResponse = null;
					filePath = tafePolicyObj.getProposalPdfPath();
					logger.info("tafePolicyPdfUploader - current proposal number to be uploaded " + proposalNumber);
	
					if(filePath!=null){
					File proposalFile = new File(filePath);
					if(proposalFile!=null && proposalFile.exists() && !proposalFile.isDirectory()) { 
						carUploadResponse = null;
						logger.info("tafePolicyPdfUploader - Proposal File available in : " + filePath);
							try{
							tafePolicyObj.setProposalPdfUploadedTime(new Date());
							
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
								logger.error("tafePolicyPdfUploader - error while uploading proposal doc :: "+e);
							}
						}else{
							logger.error("tafePolicyPdfUploader - proposal  - "+proposalNumber+" file not available in " + filePath +"to upload");
							tafePolicyObj.setIsProposalDownloaded(RPAConstants.N);
						}
					}else{
						logger.error("tafePolicyPdfUploader - file path is empty");
					}
					
					if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
						logger.info("tafePolicyPdfUploader - proposal status code :: "+carUploadResponse.getStatusCode());
						uplodedProposalPDFCount++;
						/* To Delete Proposal pdf file */
						logger.info("tafePolicyPdfUploader - is " + proposalNumber + "'s pdf File Deleted ? "
								+ deleteFile(tafePolicyObj.getProposalPdfPath()));
						tafePolicyObj.setIsProposalUploaded(RPAConstants.Y);
					} else {
						if (tafePolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
							logger.info("tafePolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
									+ proposalNumber);
							tafePolicyObj.setIsProposalUploaded(RPAConstants.E);
						} else {
							tafePolicyObj.setIsProposalUploaded(RPAConstants.D);
						}
					}
					if(carUploadResponse!=null){
						tafePolicyObj.setProposalRequest(carUploadResponse.getXmlRequest());
						tafePolicyObj.setProposalResponse(carUploadResponse.getXmlResponse());
					}
				} else {
					logger.info("tafePolicyPdfUploader - Proposal pdf already Uploaded for Proposal code :: "
							+ proposalNumber);
				}
				
				tafePolicyObj.setInwardCode(inwardCode);
				tafePolicyService.save(tafePolicyObj);
			
			}
		}finally{
			if(!userDbId.equals(""))
				logger.info("tafePolicyPdfUploader - cabinet disconeccted status ::"+omniDocUploader.cabinetDisconnect(omnicabinetName, userDbId, omniAppServer));
		}
		
		/* Total policy pdf uploaded */
		transactionInfo.setTotalSuccessRecords(String.valueOf(uplodedPolicyPDFCount));
		logger.info("tafePolicyPdfUploader - Uploaded policy count :: " + uplodedPolicyPDFCount);

		/* Total proposal pdf uploaded */
		transactionInfo.setTotalSuccessUploads((String.valueOf(uplodedProposalPDFCount)));
		logger.info("tafePolicyPdfUploader - Uploaded Proposal count :: " + uplodedProposalPDFCount);

		logger.info("tafePolicyPdfUploader - END getPolicyNumberListTobeUploadedFromLocalDb()");
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
		logger.info("tafePolicyPdfUploader - BEGIN getNextInwardCode() called");
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
		logger.info("tafePolicyPdfUploader - END downloadPdf() nextInwardCode ::" + nextInwardCode);
		return nextInwardCode;
	}
}
