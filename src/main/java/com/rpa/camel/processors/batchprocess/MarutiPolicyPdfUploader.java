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
import com.rpa.model.processors.MarutiPolicy;
import com.rpa.response.CarUploadResponse;
import com.rpa.service.CommonService;
import com.rpa.service.processors.MarutiPolicyService;
import com.rpa.util.UtilityFile;

public class MarutiPolicyPdfUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(MarutiPolicyPdfUploader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	MarutiPolicyService marutiPolicyService;

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
		logger.info("*********** inside Camel Process of marutiPolicyPdfUploader Called ************");
		logger.info("BEGIN : marutiPolicyPdfUploader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.UPLOAD);
		MarutiPolicyPdfUploader marutiPolicyPdfUploader = new MarutiPolicyPdfUploader();
		marutiPolicyService = applicationContext.getBean(MarutiPolicyService.class);
		jax2MarshallerGenerater = applicationContext.getBean(Jax2MarshallerGenerater.class);
		Jaxb2Marshaller jaxb2Marshaller = jax2MarshallerGenerater.getJax2MarshallerObject();
		omniDocClient = applicationContext.getBean(OmniDocClient.class);
		omniDocUploader = applicationContext.getBean(OmniDocsUploader.class);
		commonService = applicationContext.getBean(CommonService.class);
		environment = applicationContext.getBean(Environment.class);
		marutiPolicyPdfUploader.doProcess(marutiPolicyPdfUploader, exchange, transactionInfo, marutiPolicyService,
				omniDocUploader, omniDocClient, jaxb2Marshaller, commonService, environment);
		logger.info("*********** inside Camel Process of marutiPolicyPdfUploader Processor Ended ************");
	}

	public void doProcess(MarutiPolicyPdfUploader marutiPolicyPdfUploader, Exchange exchange,
			TransactionInfo transactionInfo, MarutiPolicyService marutiPolicyService, OmniDocsUploader omniDocUploader,
			OmniDocClient omniDocClient, Jaxb2Marshaller marshaller, CommonService commonService,
			Environment environment) throws Exception {
		logger.info("BEGIN : marutiPolicyPdfUploader Processor - doProcess Method Called  ");
		
		getPolicyNumberListTobeUploadedFromLocalDb(transactionInfo, marutiPolicyService, exchange, omniDocUploader,
				commonService, environment);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : marutiPolicyPdfUploader Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeUploadedFromLocalDb(TransactionInfo transactionInfo,
			MarutiPolicyService marutiPolicyService, Exchange exchange, OmniDocsUploader omniDocUploader,
			CommonService commonService, Environment environment) throws Exception {
		logger.info(
				"Processor - marutiPolicyPdfUploader - BEGIN getPolicyNumberListTobeUploadedFromLocalDb()  called ");

		List<MarutiPolicy> marutiPolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			marutiPolicyNoList = marutiPolicyService.findPdfUnUploadedPolicies(
					commonService.getThresholdFrequencyLevel(RPAConstants.MARUTI_UPLOAD_THRESHOLD));
		} else {
			marutiPolicyNoList = marutiPolicyService.findPdfUnUploadedBackLogPolicies(
					UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_BACKLOG_STARTDATE),
					UtilityFile.getCarPolicyProperty(RPAConstants.MARUTI_BACKLOG_ENDDATE),
					commonService.getThresholdFrequencyLevel(RPAConstants.MARUTI_UPLOAD_THRESHOLD));
		}
		logger.info("marutiPolicyPdfUploader - UnUploaded Policies count ::" + marutiPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(marutiPolicyNoList.size()));

		int uplodedPolicyPDFCount = 0, uplodedProposalPDFCount = 0;

		String inwardCode = "", policyNo = "", proposalNumber = "", filePath = "",
				paramValue = "",inwardFolderIndex="",proposaFolderIndex="",userDbId="";
		CarUploadResponse marutiUploadResponse = null;
		
		String omniUser="",omniPassword="",omnicabinetName="",omnidataDefName="",omnidataDefId="",omniPolicyIndex="",omniProposalIndex="",omniInwardIndex="",
				omniAppServer="",omniDocSize="",omniOEMFolderIndex="";
		
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
			omniUser = UtilityFile.getOmniDocLiveProperty(RPAConstants.MARUTI_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocLiveProperty(RPAConstants.MARUTI_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_MARUTI_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_MARUTI_PREFIX_LIVE;
		}else{
			omniUser = UtilityFile.getOmniDocProperty(RPAConstants.MARUTI_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocProperty(RPAConstants.MARUTI_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_MARUTI_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_MARUTI_PREFIX;
		}

		userDbId = omniDocUploader.cabinetConnect(omniUser,
				omniPassword,
				omnicabinetName, omniAppServer);
		logger.info("marutiPolicyPdfUploader - userDbId ::::::::::::::::::::::::::: "+userDbId);
		
		try{
			for (MarutiPolicy marutiPolicyObj : marutiPolicyNoList) {	
				
				policyNo = marutiPolicyObj.getPolicyNo();
				proposalNumber = marutiPolicyObj.getProposalNumber();
				
				/*if(marutiPolicyObj.getInwardFolderIndex()==null || marutiPolicyObj.getProposalFolderIndex()==null){
				
				}*/
				
				
				
					
					if (marutiPolicyObj.getInwardCode() == null) {
						inwardCode = getNextInwardCode(paramValue);
					} else {
						inwardCode = marutiPolicyObj.getInwardCode();
					}
					
					logger.info("marutiPolicyPdfUploader - inwardCode ::::::::::::::::::::::::::: "+inwardCode);
					logger.info("marutiPolicyPdfUploader - policyNo ::::::::::::::::::::::::::: "+policyNo);
					logger.info("marutiPolicyPdfUploader - proposalNumber ::::::::::::::::::::::::::: "+proposalNumber);
					
					if(marutiPolicyObj.getInwardFolderIndex()==null){
						inwardFolderIndex = omniDocUploader.addFolder(userDbId, omniOEMFolderIndex, inwardCode, omnicabinetName,omniAppServer);
						marutiPolicyObj.setInwardFolderIndex(inwardFolderIndex);
					}else{
						inwardFolderIndex = marutiPolicyObj.getInwardFolderIndex();
					}
					logger.info("marutiPolicyPdfUploader - inwardFolderIndex ::::::::::::::::::::::::::: "+inwardFolderIndex);
					
					if(marutiPolicyObj.getProposalFolderIndex()==null){
						proposaFolderIndex = omniDocUploader.addFolder(userDbId, inwardFolderIndex, proposalNumber, omnicabinetName,omniAppServer);
						marutiPolicyObj.setProposalFolderIndex(proposaFolderIndex);
					}else{
						proposaFolderIndex = marutiPolicyObj.getProposalFolderIndex();
					}
					logger.info("marutiPolicyPdfUploader - proposaFolderIndex ::::::::::::::::::::::::::: "+proposaFolderIndex);
		
					if (marutiPolicyObj.getIsPolicyUploaded() == null)
						marutiPolicyObj.setIsPolicyUploaded(RPAConstants.N);
		
					filePath = "";
		
					/* policy Extraction */
					if (marutiPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
						logger.info("marutiPolicyPdfUploader - policy not downloaded yet :: " + policyNo);
					} else if (marutiPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.N)
							|| marutiPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
						marutiUploadResponse = null;
						logger.info("marutiPolicyPdfUploader - current policy number to be uploaded " + policyNo);
						filePath = marutiPolicyObj.getPolicyPdfPath();
						
						if(filePath!=null){
							marutiPolicyObj.setPolicyPdfUploadedTime(new Date());
							File policyFile = new File(filePath);
							
							if(policyFile!=null && policyFile.exists() && !policyFile.isDirectory()) { 
								logger.info("marutiPolicyPdfUploader - Policy File available in : " + filePath);
								if(isFileSizeIsValid(policyFile,Double.valueOf("35"))){
								try{
								marutiUploadResponse = omniDocUploader.uploadDoc(
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
									if(marutiUploadResponse!=null){
									marutiUploadResponse.setStatusCode("E");
									}
									logger.error("marutiPolicyPdfUploader - error while uploading policy doc :: "+e);
								}
								}else{
									logger.error("marutiPolicyPdfUploader - policy  - "+policyNo+" file size is smaller than 35 kb need to download again");
									logger.info("marutiPolicyPdfUploader - is " + policyNo + "'s pdf File Deleted ? "
											+ deleteFile(marutiPolicyObj.getPolicyPdfPath()));
									marutiPolicyObj.setIsPolicyDownloaded(RPAConstants.N);
								}
							}else{
								logger.error("marutiPolicyPdfUploader - policy  - "+policyNo+" file not available in " + filePath +"to upload");
								marutiPolicyObj.setIsPolicyDownloaded(RPAConstants.N);
							}
						}else{
							logger.error("marutiPolicyPdfUploader - file path is empty");
						}
		
						if (marutiUploadResponse != null && marutiUploadResponse.getStatusCode().equals("0")) {
							uplodedPolicyPDFCount++;
							logger.info("marutiPolicyPdfUploader - policy status code :: "+marutiUploadResponse.getStatusCode());
							/* To Delete Policy pdf file */
							logger.info("marutiPolicyPdfUploader - is " + policyNo + "'s pdf File Deleted ? "
									+ deleteFile(marutiPolicyObj.getPolicyPdfPath()));
							marutiPolicyObj.setIsPolicyUploaded(RPAConstants.Y);
						} else {
							if (marutiPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
								logger.info("marutiPolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
										+ proposalNumber);
								marutiPolicyObj.setIsPolicyUploaded(RPAConstants.E);
							} else {
								marutiPolicyObj.setIsPolicyUploaded(RPAConstants.D);
							}
						}
						if(marutiUploadResponse!=null){
							marutiPolicyObj.setPolicyRequest(marutiUploadResponse.getXmlRequest());
							marutiPolicyObj.setPolicyResponse(marutiUploadResponse.getXmlResponse());
						}
					} else {
						logger.info("marutiPolicyPdfUploader - Policy pdf already Uploaded for policy code :: " + policyNo);
					}
		
					if (marutiPolicyObj.getIsProposalUploaded() == null)
						marutiPolicyObj.setIsProposalUploaded(RPAConstants.N);
		
					/* proposal form extraction */
					if (marutiPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.N)) {
						logger.info("marutiPolicyPdfUploader - proposal not downloaded yet :: " + proposalNumber);
					} else if (marutiPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.N)
							|| marutiPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
						marutiUploadResponse = null;
						filePath = marutiPolicyObj.getProposalPdfPath();
						logger.info("marutiPolicyPdfUploader - current proposal number to be uploaded " + proposalNumber);
		
						if(filePath!=null){
							marutiPolicyObj.setProposalPdfUploadedTime(new Date());
							File proposalFile = new File(filePath);
							if(proposalFile!=null && proposalFile.exists() && !proposalFile.isDirectory()) { 
								logger.info("marutiPolicyPdfUploader - Proposal File available in : " + filePath);
								marutiUploadResponse = null;
								if(isFileSizeIsValid(proposalFile,Double.valueOf("35"))){
								try{
								marutiUploadResponse = omniDocUploader.uploadDoc(
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
									if(marutiUploadResponse!=null){
									marutiUploadResponse.setStatusCode("E");
									}
									logger.error("marutiPolicyPdfUploader - error while uploading proposal doc :: "+e);
								}
								}else{
									logger.error("marutiPolicyPdfUploader - proposal  - "+proposalNumber+" file size is smaller than 35 kb need to download again");
									logger.info("marutiPolicyPdfUploader - is " + proposalNumber + "'s pdf File Deleted ? "
											+ deleteFile(marutiPolicyObj.getProposalPdfPath()));
									marutiPolicyObj.setIsProposalDownloaded(RPAConstants.N);
								}
							}else{
								logger.error("marutiPolicyPdfUploader - proposal  - "+proposalNumber+" file not available in " + filePath +"to upload");
								marutiPolicyObj.setIsProposalDownloaded(RPAConstants.N);
							}
						}else{
							logger.error("marutiPolicyPdfUploader - file path is empty");
						}
						
						if (marutiUploadResponse != null && marutiUploadResponse.getStatusCode().equals("0")) {
							logger.info("marutiPolicyPdfUploader - propsal status code :: "+marutiUploadResponse.getStatusCode());
							uplodedProposalPDFCount++;
							/* To Delete Proposal pdf file */
							logger.info("marutiPolicyPdfUploader - is " + proposalNumber + "'s pdf File Deleted ? "
									+ deleteFile(marutiPolicyObj.getProposalPdfPath()));
							marutiPolicyObj.setIsProposalUploaded(RPAConstants.Y);
						} else {
							if (marutiPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
								logger.info("marutiPolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
										+ proposalNumber);
								marutiPolicyObj.setIsProposalUploaded(RPAConstants.E);
							} else {
								marutiPolicyObj.setIsProposalUploaded(RPAConstants.D);
							}
						}
						if(marutiUploadResponse!=null){
							marutiPolicyObj.setProposalRequest(marutiUploadResponse.getXmlRequest());
							marutiPolicyObj.setProposalResponse(marutiUploadResponse.getXmlResponse());
						}
					} else {
						logger.info("marutiPolicyPdfUploader - Proposal pdf already Uploaded for Proposal code :: "
								+ proposalNumber);
					}
					
					marutiPolicyObj.setInwardCode(inwardCode);
					marutiPolicyService.updateWithByteMarutiData(marutiPolicyObj);
	
				}
		}finally{
			
			if(!userDbId.equals(""))
				logger.info("marutiPolicyPdfUploader - cabinet disconeccted status ::"+omniDocUploader.cabinetDisconnect(omnicabinetName, userDbId, omniAppServer));
		
		}
		
		/* Total policy pdf uploaded */
		transactionInfo.setTotalSuccessRecords(String.valueOf(uplodedPolicyPDFCount));
		logger.info("marutiPolicyPdfUploader - Uploaded policy count :: " + uplodedPolicyPDFCount);

		/* Total proposal pdf uploaded */
		transactionInfo.setTotalSuccessUploads((String.valueOf(uplodedProposalPDFCount)));
		logger.info("marutiPolicyPdfUploader - Uploaded Proposal count :: " + uplodedProposalPDFCount);

		logger.info("marutiPolicyPdfUploader - END getPolicyNumberListTobeUploadedFromLocalDb()");
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
		logger.info("marutiPolicyPdfUploader - BEGIN getNextInwardCode() called");
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
		logger.info("marutiPolicyPdfUploader - END downloadPdf() nextInwardCode ::" + nextInwardCode);
		return nextInwardCode;
	}
	
	private boolean isFileSizeIsValid(File file,double thresholdSize) {
		if(((double) file.length() / (1024))>thresholdSize){
			return true;
		}else{
			return false;
		}
	}
}
