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
import com.rpa.model.processors.HondaPolicy;
import com.rpa.response.CarUploadResponse;
import com.rpa.service.CommonService;
import com.rpa.service.processors.HondaPolicyService;
import com.rpa.util.UtilityFile;

public class HondaPolicyPdfUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(HondaPolicyPdfUploader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	HondaPolicyService hondaPolicyService;

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
		logger.info("*********** inside Camel Process of hondaPolicyPdfUploader Called ************");
		logger.info("BEGIN : hondaPolicyPdfUploader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.UPLOAD);
		HondaPolicyPdfUploader hondaPolicyPdfUploader = new HondaPolicyPdfUploader();
		hondaPolicyService = applicationContext.getBean(HondaPolicyService.class);
		jax2MarshallerGenerater = applicationContext.getBean(Jax2MarshallerGenerater.class);
		Jaxb2Marshaller jaxb2Marshaller = jax2MarshallerGenerater.getJax2MarshallerObject();
		omniDocClient = applicationContext.getBean(OmniDocClient.class);
		omniDocUploader = applicationContext.getBean(OmniDocsUploader.class);
		commonService = applicationContext.getBean(CommonService.class);
		environment = applicationContext.getBean(Environment.class);
		hondaPolicyPdfUploader.doProcess(hondaPolicyPdfUploader, exchange, transactionInfo, hondaPolicyService,
				omniDocUploader, omniDocClient, jaxb2Marshaller, commonService, environment);
		logger.info("*********** inside Camel Process of hondaPolicyPdfUploader Processor Ended ************");
	}

	public void doProcess(HondaPolicyPdfUploader hondaPolicyPdfUploader, Exchange exchange,
			TransactionInfo transactionInfo, HondaPolicyService hondaPolicyService, OmniDocsUploader omniDocUploader,
			OmniDocClient omniDocClient, Jaxb2Marshaller marshaller, CommonService commonService,
			Environment environment) throws Exception {
		logger.info("BEGIN : hondaPolicyPdfUploader Processor - doProcess Method Called  ");
		/*
		 * marshaller.setContextPath("com.rpa.wsdl");
		 * marshaller.afterPropertiesSet(); omniDocClient.setDefaultUri(
		 * "https://www.royalsundaram.net/OmniDocsWS/services");
		 * omniDocClient.setMarshaller(marshaller);
		 * omniDocClient.setUnmarshaller(marshaller);
		 */
		getPolicyNumberListTobeUploadedFromLocalDb(transactionInfo, hondaPolicyService, exchange, omniDocUploader,
				commonService, environment);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : hondaPolicyPdfUploader Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeUploadedFromLocalDb(TransactionInfo transactionInfo,
			HondaPolicyService hondaPolicyService, Exchange exchange, OmniDocsUploader omniDocUploader,
			CommonService commonService, Environment environment) throws Exception {
		logger.info(
				"Processor - hondaPolicyPdfUploader - BEGIN getPolicyNumberListTobeUploadedFromLocalDb()  called ");

		List<HondaPolicy> hondaPolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			hondaPolicyNoList = hondaPolicyService.findPdfUnUploadedPolicies(
					commonService.getThresholdFrequencyLevel(RPAConstants.HONDA_UPLOAD_THRESHOLD));
		} else {
			hondaPolicyNoList = hondaPolicyService.findPdfUnUploadedBackLogPolicies(
					UtilityFile.getCarPolicyProperty(RPAConstants.HONDA_BACKLOG_STARTDATE),
					UtilityFile.getCarPolicyProperty(RPAConstants.HONDA_BACKLOG_ENDDATE),
					commonService.getThresholdFrequencyLevel(RPAConstants.HONDA_UPLOAD_THRESHOLD));
		}
		logger.info("hondaPolicyPdfUploader - UnUploaded Policies count ::" + hondaPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(hondaPolicyNoList.size()));

		int uplodedPolicyPDFCount = 0, uplodedProposalPDFCount = 0;

		String inwardCode = "", policyNo = "", proposalNumber = "", filePath = "",
				paramValue = "",inwardFolderIndex="",proposaFolderIndex="",userDbId="";
		CarUploadResponse carUploadResponse = null;
		
		String omniUser="",omniPassword="",omnicabinetName="",omnidataDefName="",omnidataDefId="",omniPolicyIndex="",omniProposalIndex="",omniInwardIndex="",
				omniAppServer="",omniDocSize="",omniOEMFolderIndex="";
		
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
			omniUser = UtilityFile.getOmniDocLiveProperty(RPAConstants.HONDA_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocLiveProperty(RPAConstants.HONDA_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_HONDA_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_HONDA_PREFIX_LIVE;
		}else{
			omniUser = UtilityFile.getOmniDocProperty(RPAConstants.HONDA_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocProperty(RPAConstants.HONDA_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_HONDA_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_HONDA_PREFIX;
		}
		
			userDbId = omniDocUploader.cabinetConnect(omniUser,
				omniPassword,
				omnicabinetName, omniAppServer);
			logger.info("hondaPolicyPdfUploader - userDbId ::::::::::::::::::::::::::: "+userDbId);
		
			try{
				for (HondaPolicy hondaPolicyObj : hondaPolicyNoList) {	
					
					policyNo = hondaPolicyObj.getPolicyNo();
					proposalNumber = hondaPolicyObj.getProposalNumber();
					
					/*if(hondaPolicyObj.getInwardFolderIndex()==null || hondaPolicyObj.getProposalFolderIndex()==null){
						
					}*/
					
				
					if (hondaPolicyObj.getInwardCode() == null) {
						inwardCode = getNextInwardCode(paramValue);
					} else {
						inwardCode = hondaPolicyObj.getInwardCode();
					}
					
					logger.info("hondaPolicyPdfUploader - inwardCode ::::::::::::::::::::::::::: "+inwardCode);
					logger.info("hondaPolicyPdfUploader - policyNo ::::::::::::::::::::::::::: "+policyNo);
					logger.info("hondaPolicyPdfUploader - proposalNumber ::::::::::::::::::::::::::: "+proposalNumber);
					
					if(hondaPolicyObj.getInwardFolderIndex()==null){
						inwardFolderIndex = omniDocUploader.addFolder(userDbId, omniOEMFolderIndex, inwardCode, omnicabinetName,omniAppServer);
						hondaPolicyObj.setInwardFolderIndex(inwardFolderIndex);
					}else{
						inwardFolderIndex = hondaPolicyObj.getInwardFolderIndex();
					}
					logger.info("hondaPolicyPdfUploader - inwardFolderIndex ::::::::::::::::::::::::::: "+inwardFolderIndex);
					
					if(hondaPolicyObj.getProposalFolderIndex()==null){
						proposaFolderIndex = omniDocUploader.addFolder(userDbId, inwardFolderIndex, proposalNumber, omnicabinetName,omniAppServer);
						hondaPolicyObj.setProposalFolderIndex(proposaFolderIndex);
					}else{
						proposaFolderIndex = hondaPolicyObj.getProposalFolderIndex();
					}
					logger.info("hondaPolicyPdfUploader - proposaFolderIndex ::::::::::::::::::::::::::: "+proposaFolderIndex);
		
					if (hondaPolicyObj.getIsPolicyUploaded() == null)
						hondaPolicyObj.setIsPolicyUploaded(RPAConstants.N);
		
					filePath = "";
		
					/* policy Extraction */
					if (hondaPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
						logger.info("hondaPolicyPdfUploader - policy not downloaded yet :: " + policyNo);
					} else if (hondaPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.R)) {
						logger.info("hondaPolicyPdfUploader - No record came when downloaded :: " + policyNo);
					} else if (hondaPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.N)
							|| hondaPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
						carUploadResponse = null;
						logger.info("hondaPolicyPdfUploader - current policy number to be uploaded " + policyNo);
						filePath = hondaPolicyObj.getPolicyPdfPath();
		
						hondaPolicyObj.setPolicyPdfUploadedTime(new Date());
						
						File policyFile = new File(filePath);
						if(policyFile!=null && policyFile.exists() && !policyFile.isDirectory()) { 
							logger.info("hondaPolicyPdfUploader - Policy File available in : " + filePath);
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
								logger.error("hondaPolicyPdfUploader - error while uploading policy doc :: "+e);
							}
						}else{
							logger.error("hondaPolicyPdfUploader - policy "+policyNo+" file not available in " + filePath +"to upload");
							hondaPolicyObj.setIsPolicyDownloaded("N");
						}
		
						if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
							logger.info("hondaPolicyPdfUploader - policy status code :: "+carUploadResponse.getStatusCode());
							uplodedPolicyPDFCount++;
							/* To Delete Policy pdf file */
							logger.info("hondaPolicyPdfUploader - is " + policyNo + "'s pdf File Deleted ? "
									+ deleteFile(hondaPolicyObj.getPolicyPdfPath()));
							hondaPolicyObj.setIsPolicyUploaded(RPAConstants.Y);
						} else {
							if (hondaPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
								logger.info("hondaPolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
										+ proposalNumber);
								hondaPolicyObj.setIsPolicyUploaded(RPAConstants.E);
							} else {
								hondaPolicyObj.setIsPolicyUploaded(RPAConstants.D);
							}
						}
						if(carUploadResponse!=null){
							hondaPolicyObj.setPolicyRequest(carUploadResponse.getXmlRequest());
							hondaPolicyObj.setPolicyResponse(carUploadResponse.getXmlResponse());
						}
					} else {
						logger.info("hondaPolicyPdfUploader - Policy pdf already Uploaded for policy code :: " + policyNo);
					}
		
					if (hondaPolicyObj.getIsProposalUploaded() == null)
						hondaPolicyObj.setIsProposalUploaded(RPAConstants.N);
		
					/* proposal form extraction */
					if (hondaPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.N)) {
						logger.info("hondaPolicyPdfUploader - proposal not downloaded yet :: " + proposalNumber);
					} else if (hondaPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.R)) {
						logger.info("hondaPolicyPdfUploader - No record came when downloaded :: " + proposalNumber);
					} else if (hondaPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.N)
							|| hondaPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
						carUploadResponse = null;
						filePath = hondaPolicyObj.getProposalPdfPath();
						logger.info("hondaPolicyPdfUploader - current proposal number to be uploaded " + proposalNumber);
		
						hondaPolicyObj.setProposalPdfUploadedTime(new Date());
							
						File proposalFile = new File(filePath);
						if(proposalFile!=null && proposalFile.exists() && !proposalFile.isDirectory()) {
							carUploadResponse = null;
							logger.info("hondaPolicyPdfUploader - Proposal File available in : " + filePath);
							try{
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
								logger.error("hondaPolicyPdfUploader - error while uploading proposal doc :: "+e);
							}
						}else{
							logger.error("hondaPolicyPdfUploader - proposal  - "+proposalNumber+" file not available in " + filePath +"to upload");
							hondaPolicyObj.setIsProposalDownloaded("N");
						}
							
						if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
							logger.info("hondaPolicyPdfUploader - proposal status code :: "+carUploadResponse.getStatusCode());
							uplodedProposalPDFCount++;
							/* To Delete Proposal pdf file */
							logger.info("hondaPolicyPdfUploader - is " + proposalNumber + "'s pdf File Deleted ? "
									+ deleteFile(hondaPolicyObj.getProposalPdfPath()));
							hondaPolicyObj.setIsProposalUploaded(RPAConstants.Y);
						} else {
							if (hondaPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
								logger.info("hondaPolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
										+ proposalNumber);
								hondaPolicyObj.setIsProposalUploaded(RPAConstants.E);
							} else {
								hondaPolicyObj.setIsProposalUploaded(RPAConstants.D);
							}
						}
						if(carUploadResponse!=null){
							hondaPolicyObj.setProposalRequest(carUploadResponse.getXmlRequest());
							hondaPolicyObj.setProposalResponse(carUploadResponse.getXmlResponse());
						}
					} else {
						logger.info("hondaPolicyPdfUploader - Proposal pdf already Uploaded for Proposal code :: "
								+ proposalNumber);
					}
		
					hondaPolicyObj.setInwardCode(inwardCode);
					hondaPolicyService.save(hondaPolicyObj);
					
					}
		
				}finally{
					if(!userDbId.equals(""))
						logger.info("hondaPolicyPdfUploader - cabinet disconeccted status ::"+omniDocUploader.cabinetDisconnect(omnicabinetName, userDbId, omniAppServer));
				}
			
			
		/* Total policy pdf uploaded */
		transactionInfo.setTotalSuccessRecords(String.valueOf(uplodedPolicyPDFCount));
		logger.info("hondaPolicyPdfUploader - Uploaded policy count :: " + uplodedPolicyPDFCount);

		/* Total proposal pdf uploaded */
		transactionInfo.setTotalSuccessUploads((String.valueOf(uplodedProposalPDFCount)));
		logger.info("hondaPolicyPdfUploader - Uploaded Proposal count :: " + uplodedProposalPDFCount);

		logger.info("hondaPolicyPdfUploader - END getPolicyNumberListTobeUploadedFromLocalDb()");
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
		logger.info("hondaPolicyPdfUploader - BEGIN getNextInwardCode() called");
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
		logger.info("hondaPolicyPdfUploader - END downloadPdf() nextInwardCode ::" + nextInwardCode);
		return nextInwardCode;
	}
}
