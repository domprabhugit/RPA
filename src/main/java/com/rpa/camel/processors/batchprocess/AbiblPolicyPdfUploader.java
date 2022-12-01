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
import com.rpa.model.processors.AbiblPolicy;
import com.rpa.response.CarUploadResponse;
import com.rpa.service.CommonService;
import com.rpa.service.processors.AbiblPolicyService;
import com.rpa.util.UtilityFile;

public class AbiblPolicyPdfUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(AbiblPolicyPdfUploader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	AbiblPolicyService abiblPolicyService;

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
		logger.info("*********** inside Camel Process of abiblPolicyPdfUploader Called ************");
		logger.info("BEGIN : abiblPolicyPdfUploader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.UPLOAD);
		AbiblPolicyPdfUploader abiblPolicyPdfUploader = new AbiblPolicyPdfUploader();
		abiblPolicyService = applicationContext.getBean(AbiblPolicyService.class);
		jax2MarshallerGenerater = applicationContext.getBean(Jax2MarshallerGenerater.class);
		Jaxb2Marshaller jaxb2Marshaller = jax2MarshallerGenerater.getJax2MarshallerObject();
		omniDocClient = applicationContext.getBean(OmniDocClient.class);
		omniDocUploader = applicationContext.getBean(OmniDocsUploader.class);
		commonService = applicationContext.getBean(CommonService.class);
		environment = applicationContext.getBean(Environment.class);
		abiblPolicyPdfUploader.doProcess(abiblPolicyPdfUploader, exchange, transactionInfo, abiblPolicyService,
				omniDocUploader, omniDocClient, jaxb2Marshaller, commonService, environment);
		logger.info("*********** inside Camel Process of abiblPolicyPdfUploader Processor Ended ************");
	}

	public void doProcess(AbiblPolicyPdfUploader abiblPolicyPdfUploader, Exchange exchange,
			TransactionInfo transactionInfo, AbiblPolicyService abiblPolicyService, OmniDocsUploader omniDocUploader,
			OmniDocClient omniDocClient, Jaxb2Marshaller marshaller, CommonService commonService,
			Environment environment) throws Exception {
		logger.info("BEGIN : abiblPolicyPdfUploader Processor - doProcess Method Called  ");
		
		getPolicyNumberListTobeUploadedFromLocalDb(transactionInfo, abiblPolicyService, exchange, omniDocUploader,
				commonService, environment);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : abiblPolicyPdfUploader Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeUploadedFromLocalDb(TransactionInfo transactionInfo,
			AbiblPolicyService abiblPolicyService, Exchange exchange, OmniDocsUploader omniDocUploader,
			CommonService commonService, Environment environment) throws Exception {
		logger.info(
				"Processor - abiblPolicyPdfUploader - BEGIN getPolicyNumberListTobeUploadedFromLocalDb()  called ");

		List<AbiblPolicy> abiblPolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			abiblPolicyNoList = abiblPolicyService.findPdfUnUploadedPolicies(
					commonService.getThresholdFrequencyLevel(RPAConstants.ABIBL_UPLOAD_THRESHOLD));
		} else {
			abiblPolicyNoList = abiblPolicyService.findPdfUnUploadedBackLogPolicies(
					UtilityFile.getCarPolicyProperty(RPAConstants.ABIBL_BACKLOG_STARTDATE),
					UtilityFile.getCarPolicyProperty(RPAConstants.ABIBL_BACKLOG_ENDDATE),
					commonService.getThresholdFrequencyLevel(RPAConstants.ABIBL_UPLOAD_THRESHOLD));
		}
		logger.info("abiblPolicyPdfUploader - UnUploaded Policies count ::" + abiblPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(abiblPolicyNoList.size()));

		int uplodedPolicyPDFCount = 0, uplodedProposalPDFCount = 0;

		String inwardCode = "", policyNo = "", proposalNumber = "", filePath = "",
				inwardFolderIndex="",proposaFolderIndex="",userDbId="",paramValue="";
		CarUploadResponse carUploadResponse = null;
		
		String omniUser="",omniPassword="",omnicabinetName="",omnidataDefName="",omnidataDefId="",omniPolicyIndex="",omniProposalIndex="",omniInwardIndex="",
				omniAppServer="",omniDocSize="",omniOEMFolderIndex="";
		
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
			omniUser = UtilityFile.getOmniDocLiveProperty(RPAConstants.ABIBL_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocLiveProperty(RPAConstants.ABIBL_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_ABIBL_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_ABIBL_PREFIX_LIVE;
		}else{
			omniUser = UtilityFile.getOmniDocProperty(RPAConstants.ABIBL_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocProperty(RPAConstants.ABIBL_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_ABIBL_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_ABIBL_PREFIX;
		}
		
		userDbId = omniDocUploader.cabinetConnect(omniUser,
				omniPassword,
				omnicabinetName, omniAppServer);

		logger.info("abiblPolicyPdfUploader - userDbId ::::::::::::::::::::::::::: "+userDbId);
		
		try{
		
			for (AbiblPolicy abiblPolicyObj : abiblPolicyNoList) {	
				
				policyNo = abiblPolicyObj.getPolicyNo();
				proposalNumber = abiblPolicyObj.getProposalNumber();
				
				/*if(abiblPolicyObj.getInwardFolderIndex()==null || abiblPolicyObj.getProposalFolderIndex()==null){
					
				}*/
				
			
					
		
					if (abiblPolicyObj.getInwardCode() == null) {
						inwardCode = getNextInwardCode(paramValue);
						if(inwardCode==null)
							throw new Exception("Entry Not available in RPA_NUM_CTRL table for param_id :: "+paramValue);
					} else {
						inwardCode = abiblPolicyObj.getInwardCode();
					}
					
					logger.info("abiblPolicyPdfUploader - inwardCode ::::::::::::::::::::::::::: "+inwardCode);
					logger.info("abiblPolicyPdfUploader - policyNo ::::::::::::::::::::::::::: "+policyNo);
					logger.info("abiblPolicyPdfUploader - proposalNumber ::::::::::::::::::::::::::: "+proposalNumber);
					
					if(abiblPolicyObj.getInwardFolderIndex()==null){
						inwardFolderIndex = omniDocUploader.addFolder(userDbId, omniOEMFolderIndex, inwardCode, omnicabinetName,omniAppServer);
						abiblPolicyObj.setInwardFolderIndex(inwardFolderIndex);
					}else{
						inwardFolderIndex = abiblPolicyObj.getInwardFolderIndex();
					}
					logger.info("abiblPolicyPdfUploader - inwardFolderIndex ::::::::::::::::::::::::::: "+inwardFolderIndex);
					
					if(abiblPolicyObj.getProposalFolderIndex()==null){
						proposaFolderIndex = omniDocUploader.addFolder(userDbId, inwardFolderIndex, proposalNumber, omnicabinetName,omniAppServer);
						abiblPolicyObj.setProposalFolderIndex(proposaFolderIndex);
					}else{
						proposaFolderIndex = abiblPolicyObj.getProposalFolderIndex();
					}
					logger.info("abiblPolicyPdfUploader - proposaFolderIndex ::::::::::::::::::::::::::: "+proposaFolderIndex);
		
					if (abiblPolicyObj.getIsPolicyUploaded() == null)
						abiblPolicyObj.setIsPolicyUploaded(RPAConstants.N);
		
					filePath = "";
		
					/* policy Extraction */
					if (abiblPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
						logger.info("abiblPolicyPdfUploader - policy not downloaded yet :: " + policyNo);
					} else if (abiblPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.R)) {
						logger.info("abiblPolicyPdfUploader - No Record came while downloaded :: " + policyNo);
					} else if (abiblPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.N)
							|| abiblPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
						carUploadResponse = null;
						logger.info("abiblPolicyPdfUploader - current policy number to be uploaded " + policyNo);
						
						filePath = abiblPolicyObj.getPolicyPdfPath();
						if(filePath!=null){
						File policyFile = new File(filePath);
						if(policyFile!=null && policyFile.exists() && !policyFile.isDirectory()) { 
							logger.info("abiblPolicyPdfUploader - Policy File available in : " + filePath);
							try{
								abiblPolicyObj.setPolicyPdfUploadedTime(new Date());
							
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
									logger.error("abiblPolicyPdfUploader - error while uploading policy doc :: "+e);
								}
							}else{
								logger.error("abiblPolicyPdfUploader - policy  - "+policyNo+" file not available in " + filePath +"to upload");
								abiblPolicyObj.setIsPolicyDownloaded(RPAConstants.N);
							}
						}else{
							logger.error("abiblPolicyPdfUploader - file path is empty");
						}
						
						if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
							logger.info("abiblPolicyPdfUploader - policy status code :: "+carUploadResponse.getStatusCode());
							uplodedPolicyPDFCount++;
							/* To Delete Policy pdf file */
							logger.info("abiblPolicyPdfUploader - is " + policyNo + "'s pdf File Deleted ? "
									+ deleteFile(abiblPolicyObj.getPolicyPdfPath()));
							abiblPolicyObj.setIsPolicyUploaded(RPAConstants.Y);
						} else {
							if (abiblPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
								logger.info("abiblPolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
										+ proposalNumber);
								abiblPolicyObj.setIsPolicyUploaded(RPAConstants.E);
							} else {
								abiblPolicyObj.setIsPolicyUploaded(RPAConstants.D);
							}
						}
						if(carUploadResponse!=null){
							abiblPolicyObj.setPolicyRequest(carUploadResponse.getXmlRequest());
							abiblPolicyObj.setPolicyResponse(carUploadResponse.getXmlResponse());
						}
					} else {
						logger.info("abiblPolicyPdfUploader - Policy pdf already Uploaded for policy code :: " + policyNo);
					}
		
					if (abiblPolicyObj.getIsProposalUploaded() == null)
						abiblPolicyObj.setIsProposalUploaded(RPAConstants.N);
		
					/* proposal form extraction */
					if (abiblPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.N)) {
						logger.info("abiblPolicyPdfUploader - proposal not downloaded yet :: " + proposalNumber);
					} else if (abiblPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.R)) {
						logger.info("abiblPolicyPdfUploader - No Record came while downloaded :: " + proposalNumber);
					} else if (abiblPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.N)
							|| abiblPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
						carUploadResponse = null;
						filePath = abiblPolicyObj.getProposalPdfPath();
						logger.info("abiblPolicyPdfUploader - current proposal number to be uploaded " + proposalNumber);
		
						if(filePath!=null){
						File proposalFile = new File(filePath);
						if(proposalFile!=null && proposalFile.exists() && !proposalFile.isDirectory()) { 
							carUploadResponse = null;
							logger.info("abiblPolicyPdfUploader - Proposal File available in : " + filePath);
								try{
								abiblPolicyObj.setProposalPdfUploadedTime(new Date());
								
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
									logger.error("abiblPolicyPdfUploader - error while uploading proposal doc :: "+e);
								}
							}else{
								logger.error("abiblPolicyPdfUploader - proposal  - "+proposalNumber+" file not available in " + filePath +"to upload");
								abiblPolicyObj.setIsProposalDownloaded(RPAConstants.N);
							}
						}else{
							logger.error("abiblPolicyPdfUploader - file path is empty");
						}
						
						if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
							logger.info("abiblPolicyPdfUploader - proposal status code :: "+carUploadResponse.getStatusCode());
							uplodedProposalPDFCount++;
							/* To Delete Proposal pdf file */
							logger.info("abiblPolicyPdfUploader - is " + proposalNumber + "'s pdf File Deleted ? "
									+ deleteFile(abiblPolicyObj.getProposalPdfPath()));
							abiblPolicyObj.setIsProposalUploaded(RPAConstants.Y);
						} else {
							if (abiblPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
								logger.info("abiblPolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
										+ proposalNumber);
								abiblPolicyObj.setIsProposalUploaded(RPAConstants.E);
							} else {
								abiblPolicyObj.setIsProposalUploaded(RPAConstants.D);
							}
						}
						if(carUploadResponse!=null){
							abiblPolicyObj.setProposalRequest(carUploadResponse.getXmlRequest());
							abiblPolicyObj.setProposalResponse(carUploadResponse.getXmlResponse());
						}
					} else {
						logger.info("abiblPolicyPdfUploader - Proposal pdf already Uploaded for Proposal code :: "
								+ proposalNumber);
					}
					
					abiblPolicyObj.setInwardCode(inwardCode);
					abiblPolicyService.save(abiblPolicyObj);
					
				}
			}
		finally{
			if(!userDbId.equals(""))
				logger.info("abiblPolicyPdfUploader - cabinet disconeccted status ::"+omniDocUploader.cabinetDisconnect(omnicabinetName, userDbId, omniAppServer));
		}
		
		/* Total policy pdf uploaded */
		transactionInfo.setTotalSuccessRecords(String.valueOf(uplodedPolicyPDFCount));
		logger.info("abiblPolicyPdfUploader - Uploaded policy count :: " + uplodedPolicyPDFCount);

		/* Total proposal pdf uploaded */
		transactionInfo.setTotalSuccessUploads((String.valueOf(uplodedProposalPDFCount)));
		logger.info("abiblPolicyPdfUploader - Uploaded Proposal count :: " + uplodedProposalPDFCount);

		logger.info("abiblPolicyPdfUploader - END getPolicyNumberListTobeUploadedFromLocalDb()");
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
		logger.info("abiblPolicyPdfUploader - BEGIN getNextInwardCode() called");
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
		logger.info("abiblPolicyPdfUploader - END downloadPdf() nextInwardCode ::" + nextInwardCode);
		return nextInwardCode;
	}
}
