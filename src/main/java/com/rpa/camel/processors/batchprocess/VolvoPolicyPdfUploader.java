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
import com.rpa.model.processors.VolvoPolicy;
import com.rpa.response.CarUploadResponse;
import com.rpa.service.CommonService;
import com.rpa.service.processors.VolvoPolicyService;
import com.rpa.util.UtilityFile;

public class VolvoPolicyPdfUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(VolvoPolicyPdfUploader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	VolvoPolicyService volvoPolicyService;

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
		logger.info("*********** inside Camel Process of volvoPolicyPdfUploader Called ************");
		logger.info("BEGIN : volvoPolicyPdfUploader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.UPLOAD);
		VolvoPolicyPdfUploader volvoPolicyPdfUploader = new VolvoPolicyPdfUploader();
		volvoPolicyService = applicationContext.getBean(VolvoPolicyService.class);
		jax2MarshallerGenerater = applicationContext.getBean(Jax2MarshallerGenerater.class);
		Jaxb2Marshaller jaxb2Marshaller = jax2MarshallerGenerater.getJax2MarshallerObject();
		omniDocClient = applicationContext.getBean(OmniDocClient.class);
		omniDocUploader = applicationContext.getBean(OmniDocsUploader.class);
		commonService = applicationContext.getBean(CommonService.class);
		environment = applicationContext.getBean(Environment.class);
		volvoPolicyPdfUploader.doProcess(volvoPolicyPdfUploader, exchange, transactionInfo, volvoPolicyService,
				omniDocUploader, omniDocClient, jaxb2Marshaller, commonService, environment);
		logger.info("*********** inside Camel Process of volvoPolicyPdfUploader Processor Ended ************");
	}

	public void doProcess(VolvoPolicyPdfUploader volvoPolicyPdfUploader, Exchange exchange,
			TransactionInfo transactionInfo, VolvoPolicyService volvoPolicyService, OmniDocsUploader omniDocUploader,
			OmniDocClient omniDocClient, Jaxb2Marshaller marshaller, CommonService commonService,
			Environment environment) throws Exception {
		logger.info("BEGIN : volvoPolicyPdfUploader Processor - doProcess Method Called  ");
		
		getPolicyNumberListTobeUploadedFromLocalDb(transactionInfo, volvoPolicyService, exchange, omniDocUploader,
				commonService, environment);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : volvoPolicyPdfUploader Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeUploadedFromLocalDb(TransactionInfo transactionInfo,
			VolvoPolicyService volvoPolicyService, Exchange exchange, OmniDocsUploader omniDocUploader,
			CommonService commonService, Environment environment) throws Exception {
		logger.info(
				"Processor - volvoPolicyPdfUploader - BEGIN getPolicyNumberListTobeUploadedFromLocalDb()  called ");

		List<VolvoPolicy> volvoPolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			volvoPolicyNoList = volvoPolicyService.findPdfUnUploadedPolicies(
					commonService.getThresholdFrequencyLevel(RPAConstants.VOLVO_UPLOAD_THRESHOLD));
		} else {
			volvoPolicyNoList = volvoPolicyService.findPdfUnUploadedBackLogPolicies(
					UtilityFile.getCarPolicyProperty(RPAConstants.VOLVO_BACKLOG_STARTDATE),
					UtilityFile.getCarPolicyProperty(RPAConstants.VOLVO_BACKLOG_ENDDATE),
					commonService.getThresholdFrequencyLevel(RPAConstants.VOLVO_UPLOAD_THRESHOLD));
		}
		logger.info("volvoPolicyPdfUploader - UnUploaded Policies count ::" + volvoPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(volvoPolicyNoList.size()));

		int uplodedPolicyPDFCount = 0, uplodedProposalPDFCount = 0;

		String inwardCode = "", policyNo = "", proposalNumber = "", filePath = "",
				inwardFolderIndex="",proposaFolderIndex="",userDbId="",paramValue="";
		CarUploadResponse carUploadResponse = null;
		
		String omniUser="",omniPassword="",omnicabinetName="",omnidataDefName="",omnidataDefId="",omniPolicyIndex="",omniProposalIndex="",omniInwardIndex="",
				omniAppServer="",omniDocSize="",omniOEMFolderIndex="";
		
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
			omniUser = UtilityFile.getOmniDocLiveProperty(RPAConstants.VOLVO_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocLiveProperty(RPAConstants.VOLVO_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_VOLVO_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_VOLVO_PREFIX_LIVE;
		}else{
			omniUser = UtilityFile.getOmniDocProperty(RPAConstants.VOLVO_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocProperty(RPAConstants.VOLVO_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_VOLVO_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_VOLVO_PREFIX;
		}
		
		userDbId = omniDocUploader.cabinetConnect(omniUser,
				omniPassword,
				omnicabinetName, omniAppServer);
		
		logger.info("volvoPolicyPdfUploader - userDbId ::::::::::::::::::::::::::: "+userDbId);
		
		try{
			
		for (VolvoPolicy volvoPolicyObj : volvoPolicyNoList) {	
			
			policyNo = volvoPolicyObj.getPolicyNo();
			proposalNumber = volvoPolicyObj.getProposalNumber();
			
			/*if(volvoPolicyObj.getInwardFolderIndex()==null || volvoPolicyObj.getProposalFolderIndex()==null){
				
			}*/

			if (volvoPolicyObj.getInwardCode() == null) {
				inwardCode = getNextInwardCode(paramValue);
				if(inwardCode==null)
					throw new Exception("Entry Not available in RPA_NUM_CTRL table for param_id :: "+paramValue);
			} else {
				inwardCode = volvoPolicyObj.getInwardCode();
			}
			
			logger.info("volvoPolicyPdfUploader - inwardCode ::::::::::::::::::::::::::: "+inwardCode);
			logger.info("volvoPolicyPdfUploader - policyNo ::::::::::::::::::::::::::: "+policyNo);
			logger.info("volvoPolicyPdfUploader - proposalNumber ::::::::::::::::::::::::::: "+proposalNumber);
			
			if(volvoPolicyObj.getInwardFolderIndex()==null){
				inwardFolderIndex = omniDocUploader.addFolder(userDbId, omniOEMFolderIndex, inwardCode, omnicabinetName,omniAppServer);
				volvoPolicyObj.setInwardFolderIndex(inwardFolderIndex);
			}else{
				inwardFolderIndex = volvoPolicyObj.getInwardFolderIndex();
			}
			logger.info("volvoPolicyPdfUploader - inwardFolderIndex ::::::::::::::::::::::::::: "+inwardFolderIndex);
			
			if(volvoPolicyObj.getProposalFolderIndex()==null){
				proposaFolderIndex = omniDocUploader.addFolder(userDbId, inwardFolderIndex, proposalNumber, omnicabinetName,omniAppServer);
				volvoPolicyObj.setProposalFolderIndex(proposaFolderIndex);
			}else{
				proposaFolderIndex = volvoPolicyObj.getProposalFolderIndex();
			}
			logger.info("volvoPolicyPdfUploader - proposaFolderIndex ::::::::::::::::::::::::::: "+proposaFolderIndex);

			if (volvoPolicyObj.getIsPolicyUploaded() == null)
				volvoPolicyObj.setIsPolicyUploaded(RPAConstants.N);

			filePath = "";

			/* policy Extraction */
			if (volvoPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
				logger.info("volvoPolicyPdfUploader - policy not downloaded yet :: " + policyNo);
			} else if (volvoPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.R)) {
				logger.info("volvoPolicyPdfUploader - No Record came while downloaded :: " + policyNo);
			} else if (volvoPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.N)
					|| volvoPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
				carUploadResponse = null;
				logger.info("volvoPolicyPdfUploader - current policy number to be uploaded " + policyNo);
				
				filePath = volvoPolicyObj.getPolicyPdfPath();
				if(filePath!=null){
				File policyFile = new File(filePath);
				if(policyFile!=null && policyFile.exists() && !policyFile.isDirectory()) { 
					logger.info("volvoPolicyPdfUploader - Policy File available in : " + filePath);
					try{
						volvoPolicyObj.setPolicyPdfUploadedTime(new Date());
					
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
							carUploadResponse.setStatusCode("E");
							logger.error("volvoPolicyPdfUploader - error while uploading policy doc :: "+e);
						}
					}else{
						logger.error("volvoPolicyPdfUploader - policy  - "+policyNo+" file not available in " + filePath +"to upload");
						volvoPolicyObj.setIsPolicyDownloaded(RPAConstants.N);
					}
				}else{
					logger.error("volvoPolicyPdfUploader - file path is empty");
				}
				
				if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
					uplodedPolicyPDFCount++;
					/* To Delete Policy pdf file */
					logger.info("volvoPolicyPdfUploader - is " + policyNo + "'s pdf File Deleted ? "
							+ deleteFile(volvoPolicyObj.getPolicyPdfPath()));
					volvoPolicyObj.setIsPolicyUploaded(RPAConstants.Y);
				} else {
					if (volvoPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
						logger.info("volvoPolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
								+ proposalNumber);
						volvoPolicyObj.setIsPolicyUploaded(RPAConstants.E);
					} else {
						volvoPolicyObj.setIsPolicyUploaded(RPAConstants.D);
					}
				}
				if(carUploadResponse!=null){
					volvoPolicyObj.setPolicyRequest(carUploadResponse.getXmlRequest());
					volvoPolicyObj.setPolicyResponse(carUploadResponse.getXmlResponse());
				}
			} else {
				logger.info("volvoPolicyPdfUploader - Policy pdf already Uploaded for policy code :: " + policyNo);
			}

			if (volvoPolicyObj.getIsProposalUploaded() == null)
				volvoPolicyObj.setIsProposalUploaded(RPAConstants.N);

			/* proposal form extraction */
			if (volvoPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.N)) {
				logger.info("volvoPolicyPdfUploader - proposal not downloaded yet :: " + proposalNumber);
			} else if (volvoPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.R)) {
				logger.info("volvoPolicyPdfUploader - No Record came while downloaded :: " + proposalNumber);
			} else if (volvoPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.N)
					|| volvoPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
				carUploadResponse = null;
				filePath = volvoPolicyObj.getProposalPdfPath();
				logger.info("volvoPolicyPdfUploader - current proposal number to be uploaded " + proposalNumber);

				if(filePath!=null){
				File proposalFile = new File(filePath);
				if(proposalFile!=null && proposalFile.exists() && !proposalFile.isDirectory()) { 
					logger.info("volvoPolicyPdfUploader - Proposal File available in : " + filePath);
						try{
						volvoPolicyObj.setProposalPdfUploadedTime(new Date());
						
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
							carUploadResponse.setStatusCode("E");
							logger.error("volvoPolicyPdfUploader - error while uploading proposal doc :: "+e);
						}
					}else{
						logger.error("volvoPolicyPdfUploader - proposal  - "+proposalNumber+" file not available in " + filePath +"to upload");
						volvoPolicyObj.setIsProposalDownloaded(RPAConstants.N);
					}
				}else{
					logger.error("volvoPolicyPdfUploader - file path is empty");
				}
				
				if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
					uplodedProposalPDFCount++;
					/* To Delete Proposal pdf file */
					logger.info("volvoPolicyPdfUploader - is " + proposalNumber + "'s pdf File Deleted ? "
							+ deleteFile(volvoPolicyObj.getProposalPdfPath()));
					volvoPolicyObj.setIsProposalUploaded(RPAConstants.Y);
				} else {
					if (volvoPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
						logger.info("volvoPolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
								+ proposalNumber);
						volvoPolicyObj.setIsProposalUploaded(RPAConstants.E);
					} else {
						volvoPolicyObj.setIsProposalUploaded(RPAConstants.D);
					}
				}
				if(carUploadResponse!=null){
					volvoPolicyObj.setProposalRequest(carUploadResponse.getXmlRequest());
					volvoPolicyObj.setProposalResponse(carUploadResponse.getXmlResponse());
				}
			} else {
				logger.info("volvoPolicyPdfUploader - Proposal pdf already Uploaded for Proposal code :: "
						+ proposalNumber);
			}
			
			volvoPolicyObj.setInwardCode(inwardCode);
			volvoPolicyService.save(volvoPolicyObj);
		
		}
		
		}finally{
			if(!userDbId.equals(""))
				logger.info("volvoPolicyPdfUploader - cabinet disconeccted status ::"+omniDocUploader.cabinetDisconnect(omnicabinetName, userDbId, omniAppServer)+" for policy :: "+ policyNo);
		}
		
		/* Total policy pdf uploaded */
		transactionInfo.setTotalSuccessRecords(String.valueOf(uplodedPolicyPDFCount));
		logger.info("volvoPolicyPdfUploader - Uploaded policy count :: " + uplodedPolicyPDFCount);

		/* Total proposal pdf uploaded */
		transactionInfo.setTotalSuccessUploads((String.valueOf(uplodedProposalPDFCount)));
		logger.info("volvoPolicyPdfUploader - Uploaded Proposal count :: " + uplodedProposalPDFCount);

		logger.info("volvoPolicyPdfUploader - END getPolicyNumberListTobeUploadedFromLocalDb()");
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
		logger.info("volvoPolicyPdfUploader - BEGIN getNextInwardCode() called");
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
		logger.info("volvoPolicyPdfUploader - END downloadPdf() nextInwardCode ::" + nextInwardCode);
		return nextInwardCode;
	}
}
