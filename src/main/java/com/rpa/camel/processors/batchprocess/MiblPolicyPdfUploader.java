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
import com.rpa.model.processors.MiblPolicy;
import com.rpa.response.CarUploadResponse;
import com.rpa.service.CommonService;
import com.rpa.service.processors.MiblPolicyService;
import com.rpa.util.UtilityFile;

public class MiblPolicyPdfUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(MiblPolicyPdfUploader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	MiblPolicyService miblPolicyService;

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
		logger.info("*********** inside Camel Process of miblPolicyPdfUploader Called ************");
		logger.info("BEGIN : miblPolicyPdfUploader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.UPLOAD);
		MiblPolicyPdfUploader miblPolicyPdfUploader = new MiblPolicyPdfUploader();
		miblPolicyService = applicationContext.getBean(MiblPolicyService.class);
		jax2MarshallerGenerater = applicationContext.getBean(Jax2MarshallerGenerater.class);
		Jaxb2Marshaller jaxb2Marshaller = jax2MarshallerGenerater.getJax2MarshallerObject();
		omniDocClient = applicationContext.getBean(OmniDocClient.class);
		omniDocUploader = applicationContext.getBean(OmniDocsUploader.class);
		commonService = applicationContext.getBean(CommonService.class);
		environment = applicationContext.getBean(Environment.class);
		miblPolicyPdfUploader.doProcess(miblPolicyPdfUploader, exchange, transactionInfo, miblPolicyService,
				omniDocUploader, omniDocClient, jaxb2Marshaller, commonService, environment);
		logger.info("*********** inside Camel Process of miblPolicyPdfUploader Processor Ended ************");
	}

	public void doProcess(MiblPolicyPdfUploader miblPolicyPdfUploader, Exchange exchange,
			TransactionInfo transactionInfo, MiblPolicyService miblPolicyService, OmniDocsUploader omniDocUploader,
			OmniDocClient omniDocClient, Jaxb2Marshaller marshaller, CommonService commonService,
			Environment environment) throws Exception {
		logger.info("BEGIN : miblPolicyPdfUploader Processor - doProcess Method Called  ");
		
		getPolicyNumberListTobeUploadedFromLocalDb(transactionInfo, miblPolicyService, exchange, omniDocUploader,
				commonService, environment);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : miblPolicyPdfUploader Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeUploadedFromLocalDb(TransactionInfo transactionInfo,
			MiblPolicyService miblPolicyService, Exchange exchange, OmniDocsUploader omniDocUploader,
			CommonService commonService, Environment environment) throws Exception {
		logger.info(
				"Processor - miblPolicyPdfUploader - BEGIN getPolicyNumberListTobeUploadedFromLocalDb()  called ");

		List<MiblPolicy> miblPolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			miblPolicyNoList = miblPolicyService.findPdfUnUploadedPolicies(
					commonService.getThresholdFrequencyLevel(RPAConstants.MIBL_UPLOAD_THRESHOLD));
		} else {
			miblPolicyNoList = miblPolicyService.findPdfUnUploadedBackLogPolicies(
					UtilityFile.getCarPolicyProperty(RPAConstants.MIBL_BACKLOG_STARTDATE),
					UtilityFile.getCarPolicyProperty(RPAConstants.MIBL_BACKLOG_ENDDATE),
					commonService.getThresholdFrequencyLevel(RPAConstants.MIBL_UPLOAD_THRESHOLD));
		}
		logger.info("miblPolicyPdfUploader - UnUploaded Policies count ::" + miblPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(miblPolicyNoList.size()));

		int uplodedPolicyPDFCount = 0, uplodedProposalPDFCount = 0;

		String inwardCode = "", policyNo = "", proposalNumber = "", filePath = "",
				inwardFolderIndex="",proposaFolderIndex="",userDbId="",paramValue="";
		CarUploadResponse carUploadResponse = null;
		
		String omniUser="",omniPassword="",omnicabinetName="",omnidataDefName="",omnidataDefId="",omniPolicyIndex="",omniProposalIndex="",omniInwardIndex="",
				omniAppServer="",omniDocSize="",omniOEMFolderIndex="";
		
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
			omniUser = UtilityFile.getOmniDocLiveProperty(RPAConstants.MIBL_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocLiveProperty(RPAConstants.MIBL_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_MIBL_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_MIBL_PREFIX_LIVE;
		}else{
			omniUser = UtilityFile.getOmniDocProperty(RPAConstants.MIBL_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocProperty(RPAConstants.MIBL_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_MIBL_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_MIBL_PREFIX;
		}
		
		userDbId = omniDocUploader.cabinetConnect(omniUser,
				omniPassword,
				omnicabinetName, omniAppServer);
		
		logger.info("miblPolicyPdfUploader - userDbId ::::::::::::::::::::::::::: "+userDbId);
		try{
			for (MiblPolicy miblPolicyObj : miblPolicyNoList) {		
				
				policyNo = miblPolicyObj.getPolicyNo();
				proposalNumber = miblPolicyObj.getProposalNumber();
	
				/*if(miblPolicyObj.getInwardFolderIndex()==null || miblPolicyObj.getProposalFolderIndex()==null){
					
				}*/
				
				if (miblPolicyObj.getInwardCode() == null) {
					inwardCode = getNextInwardCode(paramValue);
					if(inwardCode==null)
						throw new Exception("Entry Not available in RPA_NUM_CTRL table for param_id :: "+paramValue);
				} else {
					inwardCode = miblPolicyObj.getInwardCode();
				}
				
				logger.info("miblPolicyPdfUploader - inwardCode ::::::::::::::::::::::::::: "+inwardCode);
				logger.info("miblPolicyPdfUploader - policyNo ::::::::::::::::::::::::::: "+policyNo);
				logger.info("miblPolicyPdfUploader - proposalNumber ::::::::::::::::::::::::::: "+proposalNumber);
				
				if(miblPolicyObj.getInwardFolderIndex()==null){
					inwardFolderIndex = omniDocUploader.addFolder(userDbId, omniOEMFolderIndex, inwardCode, omnicabinetName,omniAppServer);
					miblPolicyObj.setInwardFolderIndex(inwardFolderIndex);
				}else{
					inwardFolderIndex = miblPolicyObj.getInwardFolderIndex();
				}
				logger.info("miblPolicyPdfUploader - inwardFolderIndex ::::::::::::::::::::::::::: "+inwardFolderIndex);
				
				if(miblPolicyObj.getProposalFolderIndex()==null){
					proposaFolderIndex = omniDocUploader.addFolder(userDbId, inwardFolderIndex, proposalNumber, omnicabinetName,omniAppServer);
					miblPolicyObj.setProposalFolderIndex(proposaFolderIndex);
				}else{
					proposaFolderIndex = miblPolicyObj.getProposalFolderIndex();
				}
				logger.info("miblPolicyPdfUploader - proposaFolderIndex ::::::::::::::::::::::::::: "+proposaFolderIndex);
	
				if (miblPolicyObj.getIsPolicyUploaded() == null)
					miblPolicyObj.setIsPolicyUploaded(RPAConstants.N);
	
				filePath = "";
	
				/* policy Extraction */
				if (miblPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
					logger.info("miblPolicyPdfUploader - policy not downloaded yet :: " + policyNo);
				} else if (miblPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.R)) {
					logger.info("miblPolicyPdfUploader - No Record came while downloaded :: " + policyNo);
				} else if (miblPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.N)
						|| miblPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
					carUploadResponse = null;
					logger.info("miblPolicyPdfUploader - current policy number to be uploaded " + policyNo);
					filePath = miblPolicyObj.getPolicyPdfPath();
					if(filePath!=null){
					File policyFile = new File(filePath);
					if(policyFile!=null && policyFile.exists() && !policyFile.isDirectory()) { 
						logger.info("miblPolicyPdfUploader - Policy File available in : " + filePath);
						try{
					miblPolicyObj.setPolicyPdfUploadedTime(new Date());
					
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
							logger.error("miblPolicyPdfUploader - error while uploading policy doc :: "+e);
						}
					}else{
						logger.error("miblPolicyPdfUploader - policy  - "+policyNo+" file not available in " + filePath +"to upload");
						miblPolicyObj.setIsPolicyDownloaded(RPAConstants.N);
					}
				}else{
					logger.error("miblPolicyPdfUploader - file path is empty");
				}
				
					if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
						logger.info("miblPolicyPdfUploader - policy status code :: "+carUploadResponse.getStatusCode());
						uplodedPolicyPDFCount++;
						/* To Delete Policy pdf file */
						logger.info("miblPolicyPdfUploader - is " + policyNo + "'s pdf File Deleted ? "
								+ deleteFile(miblPolicyObj.getPolicyPdfPath()));
						miblPolicyObj.setIsPolicyUploaded(RPAConstants.Y);
					} else {
						if (miblPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
							logger.info("miblPolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
									+ proposalNumber);
							miblPolicyObj.setIsPolicyUploaded(RPAConstants.E);
						} else {
							miblPolicyObj.setIsPolicyUploaded(RPAConstants.D);
						}
					}
					if(carUploadResponse!=null){
						miblPolicyObj.setPolicyRequest(carUploadResponse.getXmlRequest());
						miblPolicyObj.setPolicyResponse(carUploadResponse.getXmlResponse());
					}
				} else {
					logger.info("miblPolicyPdfUploader - Policy pdf already Uploaded for policy code :: " + policyNo);
				}
	
				if (miblPolicyObj.getIsProposalUploaded() == null)
					miblPolicyObj.setIsProposalUploaded(RPAConstants.N);
	
				/* proposal form extraction */
				if (miblPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.N)) {
					logger.info("miblPolicyPdfUploader - proposal not downloaded yet :: " + proposalNumber);
				} else if (miblPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.R)) {
					logger.info("miblPolicyPdfUploader - No Record came while downloaded :: :: " + proposalNumber);
				} else if (miblPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.N)
						|| miblPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
					carUploadResponse = null;
					filePath = miblPolicyObj.getProposalPdfPath();
					logger.info("miblPolicyPdfUploader - current proposal number to be uploaded " + proposalNumber);
	
					if(filePath!=null){
						File proposalFile = new File(filePath);
						if(proposalFile!=null && proposalFile.exists() && !proposalFile.isDirectory()) { 
							carUploadResponse = null;
							logger.info("miblPolicyPdfUploader - Proposal File available in : " + filePath);
								try{
					miblPolicyObj.setProposalPdfUploadedTime(new Date());
						
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
									logger.error("miblPolicyPdfUploader - error while uploading proposal doc :: "+e);
								}
							}else{
								logger.error("miblPolicyPdfUploader - proposal  - "+proposalNumber+" file not available in " + filePath +"to upload");
								miblPolicyObj.setIsProposalDownloaded(RPAConstants.N);
							}
						}else{
							logger.error("miblPolicyPdfUploader - file path is empty");
						}
					
					if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
						logger.info("miblPolicyPdfUploader - proposal status code :: "+carUploadResponse.getStatusCode());
						uplodedProposalPDFCount++;
						/* To Delete Proposal pdf file */
						logger.info("miblPolicyPdfUploader - is " + proposalNumber + "'s pdf File Deleted ? "
								+ deleteFile(miblPolicyObj.getProposalPdfPath()));
						miblPolicyObj.setIsProposalUploaded(RPAConstants.Y);
					} else {
						if (miblPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
							logger.info("miblPolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
									+ proposalNumber);
							miblPolicyObj.setIsProposalUploaded(RPAConstants.E);
						} else {
							miblPolicyObj.setIsProposalUploaded(RPAConstants.D);
						}
					}
					if(carUploadResponse!=null){
						miblPolicyObj.setProposalRequest(carUploadResponse.getXmlRequest());
						miblPolicyObj.setProposalResponse(carUploadResponse.getXmlResponse());
					}
				} else {
					logger.info("miblPolicyPdfUploader - Proposal pdf already Uploaded for Proposal code :: "
							+ proposalNumber);
				}
				
				miblPolicyObj.setInwardCode(inwardCode);
				miblPolicyService.save(miblPolicyObj);
				
			}
		}finally{
			if(!userDbId.equals(""))
				logger.info("miblPolicyPdfUploader - cabinet disconeccted status ::"+omniDocUploader.cabinetDisconnect(omnicabinetName, userDbId, omniAppServer));
		}
		
		/* Total policy pdf uploaded */
		transactionInfo.setTotalSuccessRecords(String.valueOf(uplodedPolicyPDFCount));
		logger.info("miblPolicyPdfUploader - Uploaded policy count :: " + uplodedPolicyPDFCount);

		/* Total proposal pdf uploaded */
		transactionInfo.setTotalSuccessUploads((String.valueOf(uplodedProposalPDFCount)));
		logger.info("miblPolicyPdfUploader - Uploaded Proposal count :: " + uplodedProposalPDFCount);

		logger.info("miblPolicyPdfUploader - END getPolicyNumberListTobeUploadedFromLocalDb()");
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
		logger.info("miblPolicyPdfUploader - BEGIN getNextInwardCode() called");
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
		logger.info("miblPolicyPdfUploader - END downloadPdf() nextInwardCode ::" + nextInwardCode);
		return nextInwardCode;
	}
}
