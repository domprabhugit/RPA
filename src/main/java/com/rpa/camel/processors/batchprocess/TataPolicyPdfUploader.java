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
import com.rpa.model.processors.TataPolicy;
import com.rpa.response.CarUploadResponse;
import com.rpa.service.CommonService;
import com.rpa.service.processors.TataPolicyService;
import com.rpa.util.UtilityFile;

public class TataPolicyPdfUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(TataPolicyPdfUploader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	TataPolicyService tataPolicyService;

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
		logger.info("*********** inside Camel Process of tataPolicyPdfUploader Called ************");
		logger.info("BEGIN : tataPolicyPdfUploader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.UPLOAD);
		TataPolicyPdfUploader tataPolicyPdfUploader = new TataPolicyPdfUploader();
		tataPolicyService = applicationContext.getBean(TataPolicyService.class);
		jax2MarshallerGenerater = applicationContext.getBean(Jax2MarshallerGenerater.class);
		Jaxb2Marshaller jaxb2Marshaller = jax2MarshallerGenerater.getJax2MarshallerObject();
		omniDocClient = applicationContext.getBean(OmniDocClient.class);
		omniDocUploader = applicationContext.getBean(OmniDocsUploader.class);
		commonService = applicationContext.getBean(CommonService.class);
		environment = applicationContext.getBean(Environment.class);
		tataPolicyPdfUploader.doProcess(tataPolicyPdfUploader, exchange, transactionInfo, tataPolicyService,
				omniDocUploader, omniDocClient, jaxb2Marshaller, commonService, environment);
		logger.info("*********** inside Camel Process of tataPolicyPdfUploader Processor Ended ************");
	}

	public void doProcess(TataPolicyPdfUploader tataPolicyPdfUploader, Exchange exchange,
			TransactionInfo transactionInfo, TataPolicyService tataPolicyService, OmniDocsUploader omniDocUploader,
			OmniDocClient omniDocClient, Jaxb2Marshaller marshaller, CommonService commonService,
			Environment environment) throws Exception {
		logger.info("BEGIN : tataPolicyPdfUploader Processor - doProcess Method Called  ");
		
		getPolicyNumberListTobeUploadedFromLocalDb(transactionInfo, tataPolicyService, exchange, omniDocUploader,
				commonService, environment);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : tataPolicyPdfUploader Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeUploadedFromLocalDb(TransactionInfo transactionInfo,
			TataPolicyService tataPolicyService, Exchange exchange, OmniDocsUploader omniDocUploader,
			CommonService commonService, Environment environment) throws Exception {
		logger.info(
				"Processor - tataPolicyPdfUploader - BEGIN getPolicyNumberListTobeUploadedFromLocalDb()  called ");

		List<TataPolicy> tataPolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			tataPolicyNoList = tataPolicyService.findPdfUnUploadedPolicies(
					commonService.getThresholdFrequencyLevel(RPAConstants.TATA_UPLOAD_THRESHOLD));
		} else {
			tataPolicyNoList = tataPolicyService.findPdfUnUploadedBackLogPolicies(
					UtilityFile.getCarPolicyProperty(RPAConstants.TATA_BACKLOG_STARTDATE),
					UtilityFile.getCarPolicyProperty(RPAConstants.TATA_BACKLOG_ENDDATE),
					commonService.getThresholdFrequencyLevel(RPAConstants.TATA_UPLOAD_THRESHOLD));
		}
		logger.info("tataPolicyPdfUploader - UnUploaded Policies count ::" + tataPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(tataPolicyNoList.size()));

		int uplodedPolicyPDFCount = 0, uplodedProposalPDFCount = 0;

		String inwardCode = "", policyNo = "", proposalNumber = "", filePath = "",
				paramValue = "",inwardFolderIndex="",proposaFolderIndex="",userDbId="";
		CarUploadResponse carUploadResponse = null;
		
		String omniUser="",omniPassword="",omnicabinetName="",omnidataDefName="",omnidataDefId="",omniPolicyIndex="",omniProposalIndex="",omniInwardIndex="",
				omniAppServer="",omniDocSize="",omniOEMFolderIndex="";
		
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
			omniUser = UtilityFile.getOmniDocLiveProperty(RPAConstants.TATA_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocLiveProperty(RPAConstants.TATA_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_TATA_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_TATA_PREFIX_LIVE;
		}else{
			omniUser = UtilityFile.getOmniDocProperty(RPAConstants.TATA_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocProperty(RPAConstants.TATA_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_TATA_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_TATA_PREFIX;
		}
		
		userDbId = omniDocUploader.cabinetConnect(omniUser,
				omniPassword,
				omnicabinetName, omniAppServer);
		
		logger.info("tataPolicyPdfUploader - userDbId ::::::::::::::::::::::::::: "+userDbId);
		try{
			for (TataPolicy tataPolicyObj : tataPolicyNoList) {		
				
				policyNo = tataPolicyObj.getPolicyNo();
				proposalNumber = tataPolicyObj.getProposalNumber();
				
				/*if(tataPolicyObj.getInwardFolderIndex()==null || tataPolicyObj.getProposalFolderIndex()==null){
					
				}*/
				
				
					
		
					if (tataPolicyObj.getInwardCode() == null) {
						inwardCode = getNextInwardCode(paramValue);
					} else {
						inwardCode = tataPolicyObj.getInwardCode();
					}
					
					logger.info("tataPolicyPdfUploader - inwardCode ::::::::::::::::::::::::::: "+inwardCode);
					logger.info("tataPolicyPdfUploader - policyNo ::::::::::::::::::::::::::: "+policyNo);
					logger.info("tataPolicyPdfUploader - proposalNumber ::::::::::::::::::::::::::: "+proposalNumber);
					
					
					if(tataPolicyObj.getInwardFolderIndex()==null){
						inwardFolderIndex = omniDocUploader.addFolder(userDbId, omniOEMFolderIndex, inwardCode, omnicabinetName,omniAppServer);
						tataPolicyObj.setInwardFolderIndex(inwardFolderIndex);
					}else{
						inwardFolderIndex = tataPolicyObj.getInwardFolderIndex();
					}
					logger.info("tataPolicyPdfUploader - inwardFolderIndex ::::::::::::::::::::::::::: "+inwardFolderIndex);
					
					if(tataPolicyObj.getProposalFolderIndex()==null){
						proposaFolderIndex = omniDocUploader.addFolder(userDbId, inwardFolderIndex, proposalNumber, omnicabinetName,omniAppServer);
						tataPolicyObj.setProposalFolderIndex(proposaFolderIndex);
					}else{
						proposaFolderIndex = tataPolicyObj.getProposalFolderIndex();
					}
					logger.info("tataPolicyPdfUploader - proposaFolderIndex ::::::::::::::::::::::::::: "+proposaFolderIndex);
		
					if (tataPolicyObj.getIsPolicyUploaded() == null)
						tataPolicyObj.setIsPolicyUploaded(RPAConstants.N);
		
					filePath = "";
		
					/* policy Extraction */
					if (tataPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
						logger.info("tataPolicyPdfUploader - policy not downloaded yet :: " + policyNo);
					}else if (tataPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.R)) {
						logger.info("tataPolicyPdfUploader - No Record came while downloaded :: " + policyNo);
					} else if (tataPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.N)
							|| tataPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
						carUploadResponse = null;
						logger.info("tataPolicyPdfUploader - current policy number to be uploaded " + policyNo);
						filePath = tataPolicyObj.getPolicyPdfPath();
						if(filePath!=null){
							File policyFile = new File(filePath);
							if(policyFile!=null && policyFile.exists() && !policyFile.isDirectory()) { 
								logger.info("tataPolicyPdfUploader - Policy File available in : " + filePath);
								
								tataPolicyObj.setPolicyPdfUploadedTime(new Date());
								try{
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
									logger.error("tataPolicyPdfUploader - error while uploading policy doc :: "+e);
								}
							}else{
								logger.error("tataPolicyPdfUploader - policy  - "+policyNo+" file not available in " + filePath +"to upload");
								tataPolicyObj.setIsPolicyDownloaded(RPAConstants.N);
							}
						}else{
							logger.error("tataPolicyPdfUploader - file path is empty");
						}
						
						
						if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
							logger.info("tataPolicyPdfUploader - policy status code :: "+carUploadResponse.getStatusCode());
							uplodedPolicyPDFCount++;
							/* To Delete Policy pdf file */
							logger.info("tataPolicyPdfUploader - is " + policyNo + "'s pdf File Deleted ? "
									+ deleteFile(tataPolicyObj.getPolicyPdfPath()));
							tataPolicyObj.setIsPolicyUploaded(RPAConstants.Y);
						} else {
							if (tataPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
								logger.info("tataPolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
										+ proposalNumber);
								tataPolicyObj.setIsPolicyUploaded(RPAConstants.E);
							} else {
								tataPolicyObj.setIsPolicyUploaded(RPAConstants.D);
							}
						}
						if(carUploadResponse!=null){
							tataPolicyObj.setPolicyRequest(carUploadResponse.getXmlRequest());
							tataPolicyObj.setPolicyResponse(carUploadResponse.getXmlResponse());
						}
					} else {
						logger.info("tataPolicyPdfUploader - Policy pdf already Uploaded for policy code :: " + policyNo);
					}
		
					if (tataPolicyObj.getIsProposalUploaded() == null)
						tataPolicyObj.setIsProposalUploaded(RPAConstants.N);
		
					/* proposal form extraction */
					if (tataPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.N)) {
						logger.info("tataPolicyPdfUploader - proposal not downloaded yet :: " + proposalNumber);
					}else if (tataPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.R)) {
						logger.info("tataPolicyPdfUploader - No record came while downloaded :: " + proposalNumber);
					}else if (tataPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.N)
							|| tataPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
						carUploadResponse = null;
						filePath = tataPolicyObj.getProposalPdfPath();
						logger.info("tataPolicyPdfUploader - current proposal number to be uploaded " + proposalNumber);
		
						if(filePath!=null){
							File proposalFile = new File(filePath);
							if(proposalFile!=null && proposalFile.exists() && !proposalFile.isDirectory()) { 
								carUploadResponse = null;
								logger.info("tataPolicyPdfUploader - Proposal File available in : " + filePath);
								try{
							tataPolicyObj.setProposalPdfUploadedTime(new Date());
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
									logger.error("tataPolicyPdfUploader - error while uploading proposal doc :: "+e);
								}
							}else{
								logger.error("tataPolicyPdfUploader - proposal  - "+proposalNumber+" file not available in " + filePath +"to upload");
								tataPolicyObj.setIsProposalDownloaded(RPAConstants.N);
							}
						}else{
							logger.error("tataPolicyPdfUploader - file path is empty");
						}
						
						if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
							logger.info("tataPolicyPdfUploader - proposal status code :: "+carUploadResponse.getStatusCode());
							uplodedProposalPDFCount++;
							/* To Delete Proposal pdf file */
							logger.info("tataPolicyPdfUploader - is " + proposalNumber + "'s pdf File Deleted ? "
									+ deleteFile(tataPolicyObj.getProposalPdfPath()));
							tataPolicyObj.setIsProposalUploaded(RPAConstants.Y);
						} else {
							if (tataPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
								logger.info("tataPolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
										+ proposalNumber);
								tataPolicyObj.setIsProposalUploaded(RPAConstants.E);
							} else {
								tataPolicyObj.setIsProposalUploaded(RPAConstants.D);
							}
						}
						if(carUploadResponse!=null){
							tataPolicyObj.setProposalRequest(carUploadResponse.getXmlRequest());
							tataPolicyObj.setProposalResponse(carUploadResponse.getXmlResponse());
						}
				} else {
					logger.info("tataPolicyPdfUploader - Proposal pdf already Uploaded for Proposal code :: "
							+ proposalNumber);
				}
				
				tataPolicyObj.setInwardCode(inwardCode);
				tataPolicyService.save(tataPolicyObj);
				
		}
		}finally{
			if(!userDbId.equals(""))
				logger.info("tataPolicyPdfUploader - cabinet disconeccted status ::"+omniDocUploader.cabinetDisconnect(omnicabinetName, userDbId, omniAppServer));
		}
		/* Total policy pdf uploaded */
		transactionInfo.setTotalSuccessRecords(String.valueOf(uplodedPolicyPDFCount));
		logger.info("tataPolicyPdfUploader - Uploaded policy count :: " + uplodedPolicyPDFCount);

		/* Total proposal pdf uploaded */
		transactionInfo.setTotalSuccessUploads((String.valueOf(uplodedProposalPDFCount)));
		logger.info("tataPolicyPdfUploader - Uploaded Proposal count :: " + uplodedProposalPDFCount);

		logger.info("tataPolicyPdfUploader - END getPolicyNumberListTobeUploadedFromLocalDb()");
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
		logger.info("tataPolicyPdfUploader - BEGIN getNextInwardCode() called");
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
		logger.info("tataPolicyPdfUploader - END downloadPdf() nextInwardCode ::" + nextInwardCode);
		return nextInwardCode;
	}
}
