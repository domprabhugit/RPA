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
import com.rpa.model.processors.PiaggioPolicy;
import com.rpa.response.CarUploadResponse;
import com.rpa.service.CommonService;
import com.rpa.service.processors.PiaggioPolicyService;
import com.rpa.util.UtilityFile;

public class PiaggioPolicyPdfUploader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(PiaggioPolicyPdfUploader.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	PiaggioPolicyService piaggioPolicyService;

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
		logger.info("*********** inside Camel Process of piaggioPolicyPdfUploader Called ************");
		logger.info("BEGIN : piaggioPolicyPdfUploader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.UPLOAD);
		PiaggioPolicyPdfUploader piaggioPolicyPdfUploader = new PiaggioPolicyPdfUploader();
		piaggioPolicyService = applicationContext.getBean(PiaggioPolicyService.class);
		jax2MarshallerGenerater = applicationContext.getBean(Jax2MarshallerGenerater.class);
		Jaxb2Marshaller jaxb2Marshaller = jax2MarshallerGenerater.getJax2MarshallerObject();
		omniDocClient = applicationContext.getBean(OmniDocClient.class);
		omniDocUploader = applicationContext.getBean(OmniDocsUploader.class);
		commonService = applicationContext.getBean(CommonService.class);
		environment = applicationContext.getBean(Environment.class);
		piaggioPolicyPdfUploader.doProcess(piaggioPolicyPdfUploader, exchange, transactionInfo, piaggioPolicyService,
				omniDocUploader, omniDocClient, jaxb2Marshaller, commonService, environment);
		logger.info("*********** inside Camel Process of piaggioPolicyPdfUploader Processor Ended ************");
	}

	public void doProcess(PiaggioPolicyPdfUploader piaggioPolicyPdfUploader, Exchange exchange,
			TransactionInfo transactionInfo, PiaggioPolicyService piaggioPolicyService,
			OmniDocsUploader omniDocUploader, OmniDocClient omniDocClient, Jaxb2Marshaller marshaller,
			CommonService commonService, Environment environment) throws Exception {
		logger.info("BEGIN : piaggioPolicyPdfUploader Processor - doProcess Method Called  ");

		getPolicyNumberListTobeUploadedFromLocalDb(transactionInfo, piaggioPolicyService, exchange, omniDocUploader,
				commonService, environment);
		transactionInfo.setProcessStatus(RPAConstants.Success);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : piaggioPolicyPdfUploader Processor - doProcess Method Ended  ");
	}

	public boolean getPolicyNumberListTobeUploadedFromLocalDb(TransactionInfo transactionInfo,
			PiaggioPolicyService piaggioPolicyService, Exchange exchange, OmniDocsUploader omniDocUploader,
			CommonService commonService, Environment environment) throws Exception {
		logger.info(
				"Processor - piaggioPolicyPdfUploader - BEGIN getPolicyNumberListTobeUploadedFromLocalDb()  called ");

		List<PiaggioPolicy> piaggioPolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			piaggioPolicyNoList = piaggioPolicyService.findPdfUnUploadedPolicies(
					commonService.getThresholdFrequencyLevel(RPAConstants.PIAGGIO_UPLOAD_THRESHOLD));
		} else {
			piaggioPolicyNoList = piaggioPolicyService.findPdfUnUploadedBackLogPolicies(
					UtilityFile.getCarPolicyProperty(RPAConstants.PIAGGIO_BACKLOG_STARTDATE),
					UtilityFile.getCarPolicyProperty(RPAConstants.PIAGGIO_BACKLOG_ENDDATE),
					commonService.getThresholdFrequencyLevel(RPAConstants.PIAGGIO_UPLOAD_THRESHOLD));
		}
		logger.info("piaggioPolicyPdfUploader - UnUploaded Policies count ::" + piaggioPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(piaggioPolicyNoList.size()));

		int uplodedPolicyPDFCount = 0, uplodedProposalPDFCount = 0;

		String inwardCode = "", policyNo = "", proposalNumber = "", filePath = "", inwardFolderIndex = "",
				proposaFolderIndex = "", userDbId = "", paramValue = "";
		CarUploadResponse carUploadResponse = null;

		String omniUser = "", omniPassword = "", omnicabinetName = "", omnidataDefName = "", omnidataDefId = "",
				omniPolicyIndex = "", omniProposalIndex = "", omniInwardIndex = "", omniAppServer = "",
				omniDocSize = "", omniOEMFolderIndex = "";

		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
			omniUser = UtilityFile.getOmniDocLiveProperty(RPAConstants.PIAGGIO_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocLiveProperty(RPAConstants.PIAGGIO_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocLiveProperty(RPAConstants.OMNI_ABIBL_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_PIAGGIO_PREFIX_LIVE;
		} else {
			omniUser = UtilityFile.getOmniDocProperty(RPAConstants.PIAGGIO_OMNI_USERNAME);
			omniPassword = UtilityFile.getOmniDocProperty(RPAConstants.PIAGGIO_OMNI_PASSWORD);
			omnicabinetName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_CABINET_NAME);
			omnidataDefName = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_NAME);
			omnidataDefId = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DATA_DEF_ID);
			omniPolicyIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_POLICY_INDEX);
			omniProposalIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_PROPOSAL_INDEX);
			omniInwardIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_INWARD_INDEX);
			omniAppServer = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_APP_SERVER);
			omniDocSize = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_DOC_SIZE);
			omniOEMFolderIndex = UtilityFile.getOmniDocProperty(RPAConstants.OMNI_ABIBL_FOLDER_INDEX);
			paramValue = RPAConstants.OMNI_PIAGGIO_PREFIX;
		}

		userDbId = omniDocUploader.cabinetConnect(omniUser, omniPassword, omnicabinetName, omniAppServer);
		
		logger.info("piaggioPolicyPdfUploader - userDbId ::::::::::::::::::::::::::: " + userDbId);
		
	try {
		for (PiaggioPolicy piaggioPolicyObj : piaggioPolicyNoList) {

			policyNo = piaggioPolicyObj.getPolicyNo();
			proposalNumber = piaggioPolicyObj.getProposalNumber();

			/*if (piaggioPolicyObj.getInwardFolderIndex() == null || piaggioPolicyObj.getProposalFolderIndex() == null) {
				
			}*/

				if (piaggioPolicyObj.getInwardCode() == null) {
					inwardCode = getNextInwardCode(paramValue);
					if (inwardCode == null)
						throw new Exception("Entry Not available in RPA_NUM_CTRL table for param_id :: " + paramValue);
				} else {
					inwardCode = piaggioPolicyObj.getInwardCode();
				}

				logger.info("piaggioPolicyPdfUploader - inwardCode ::::::::::::::::::::::::::: " + inwardCode);
				logger.info("piaggioPolicyPdfUploader - policyNo ::::::::::::::::::::::::::: " + policyNo);
				logger.info("piaggioPolicyPdfUploader - proposalNumber ::::::::::::::::::::::::::: " + proposalNumber);

				if (piaggioPolicyObj.getInwardFolderIndex() == null) {
					inwardFolderIndex = omniDocUploader.addFolder(userDbId, omniOEMFolderIndex, inwardCode,
							omnicabinetName, omniAppServer);
					piaggioPolicyObj.setInwardFolderIndex(inwardFolderIndex);
				} else {
					inwardFolderIndex = piaggioPolicyObj.getInwardFolderIndex();
				}
				logger.info("piaggioPolicyPdfUploader - inwardFolderIndex ::::::::::::::::::::::::::: "
						+ inwardFolderIndex);

				if (piaggioPolicyObj.getProposalFolderIndex() == null) {
					proposaFolderIndex = omniDocUploader.addFolder(userDbId, inwardFolderIndex, proposalNumber,
							omnicabinetName, omniAppServer);
					piaggioPolicyObj.setProposalFolderIndex(proposaFolderIndex);
				} else {
					proposaFolderIndex = piaggioPolicyObj.getProposalFolderIndex();
				}
				logger.info("piaggioPolicyPdfUploader - proposaFolderIndex ::::::::::::::::::::::::::: "
						+ proposaFolderIndex);

				if (piaggioPolicyObj.getIsPolicyUploaded() == null)
					piaggioPolicyObj.setIsPolicyUploaded(RPAConstants.N);

				filePath = "";

				/* policy Extraction */
				if (piaggioPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.N)) {
					logger.info("piaggioPolicyPdfUploader - policy not downloaded yet :: " + policyNo);
				} else if (piaggioPolicyObj.getIsPolicyDownloaded().equalsIgnoreCase(RPAConstants.R)) {
					logger.info("piaggioPolicyPdfUploader - No Record came while downloaded :: " + policyNo);
				} else if (piaggioPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.N)
						|| piaggioPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
					carUploadResponse = null;
					logger.info("piaggioPolicyPdfUploader - current policy number to be uploaded " + policyNo);

					filePath = piaggioPolicyObj.getPolicyPdfPath();
					if (filePath != null) {
						File policyFile = new File(filePath);
						if (policyFile != null && policyFile.exists() && !policyFile.isDirectory()) {
							logger.info("piaggioPolicyPdfUploader - Policy File available in : " + filePath);
							try {
								piaggioPolicyObj.setPolicyPdfUploadedTime(new Date());

								carUploadResponse = omniDocUploader.uploadDoc(omniUser, omniPassword, omnicabinetName,
										getEncodedFileString(filePath, new File(filePath)), 1, omniDocSize,
										"policy_" + policyNo + ".pdf", proposaFolderIndex, omnidataDefName,
										omnidataDefId, omniPolicyIndex, policyNo, inwardCode, omniInwardIndex,
										omniAppServer);
							} catch (Exception e) {
								if (carUploadResponse != null) {
									carUploadResponse.setStatusCode("E");
								}
								logger.error("piaggioPolicyPdfUploader - error while uploading policy doc :: " + e);
							}
						} else {
							logger.error("piaggioPolicyPdfUploader - policy  - " + policyNo + " file not available in "
									+ filePath + "to upload");
							piaggioPolicyObj.setIsPolicyDownloaded(RPAConstants.N);
						}
					} else {
						logger.error("piaggioPolicyPdfUploader - file path is empty");
					}

					if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
						logger.info("piaggioPolicyPdfUploader - policy status code :: "
								+ carUploadResponse.getStatusCode());
						uplodedPolicyPDFCount++;
						/* To Delete Policy pdf file */
						logger.info("piaggioPolicyPdfUploader - is " + policyNo + "'s pdf File Deleted ? "
								+ deleteFile(piaggioPolicyObj.getPolicyPdfPath()));
						piaggioPolicyObj.setIsPolicyUploaded(RPAConstants.Y);
					} else {
						if (piaggioPolicyObj.getIsPolicyUploaded().equalsIgnoreCase(RPAConstants.D)) {
							logger.info("piaggioPolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
									+ proposalNumber);
							piaggioPolicyObj.setIsPolicyUploaded(RPAConstants.E);
						} else {
							piaggioPolicyObj.setIsPolicyUploaded(RPAConstants.D);
						}
					}
					if (carUploadResponse != null) {
						piaggioPolicyObj.setPolicyRequest(carUploadResponse.getXmlRequest());
						piaggioPolicyObj.setPolicyResponse(carUploadResponse.getXmlResponse());
					}
				} else {
					logger.info(
							"piaggioPolicyPdfUploader - Policy pdf already Uploaded for policy code :: " + policyNo);
				}

				if (piaggioPolicyObj.getIsProposalUploaded() == null)
					piaggioPolicyObj.setIsProposalUploaded(RPAConstants.N);

				/* proposal form extraction */
				if (piaggioPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.N)) {
					logger.info("piaggioPolicyPdfUploader - proposal not downloaded yet :: " + proposalNumber);
				} else if (piaggioPolicyObj.getIsProposalDownloaded().equalsIgnoreCase(RPAConstants.R)) {
					logger.info("piaggioPolicyPdfUploader - No Record came while downloaded :: " + proposalNumber);
				} else if (piaggioPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.N)
						|| piaggioPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
					carUploadResponse = null;
					filePath = piaggioPolicyObj.getProposalPdfPath();
					logger.info("piaggioPolicyPdfUploader - current proposal number to be uploaded " + proposalNumber);

					if (filePath != null) {
						File proposalFile = new File(filePath);
						if (proposalFile != null && proposalFile.exists() && !proposalFile.isDirectory()) {
							carUploadResponse = null;
							logger.info("piaggioPolicyPdfUploader - Proposal File available in : " + filePath);
							try {
								piaggioPolicyObj.setProposalPdfUploadedTime(new Date());

								carUploadResponse = omniDocUploader.uploadDoc(omniUser, omniPassword, omnicabinetName,
										getEncodedFileString(filePath, new File(filePath)), 1, omniDocSize,
										"proposal_" + proposalNumber + ".pdf", proposaFolderIndex, omnidataDefName,
										omnidataDefId, omniProposalIndex, proposalNumber, inwardCode, omniInwardIndex,
										omniAppServer);

							} catch (Exception e) {
								if (carUploadResponse != null) {
									carUploadResponse.setStatusCode("E");
								}
								logger.error("piaggioPolicyPdfUploader - error while uploading proposal doc :: " + e);
							}
						} else {
							logger.error("piaggioPolicyPdfUploader - proposal  - " + proposalNumber
									+ " file not available in " + filePath + "to upload");
							piaggioPolicyObj.setIsProposalDownloaded(RPAConstants.N);
						}
					} else {
						logger.error("tataPolicyPdfUploader - file path is empty");
					}

					if (carUploadResponse != null && carUploadResponse.getStatusCode().equals("0")) {
						logger.info("piaggioPolicyPdfUploader - proposal status code :: "
								+ carUploadResponse.getStatusCode());
						uplodedProposalPDFCount++;
						/* To Delete Proposal pdf file */
						logger.info("piaggioPolicyPdfUploader - is " + proposalNumber + "'s pdf File Deleted ? "
								+ deleteFile(piaggioPolicyObj.getProposalPdfPath()));
						piaggioPolicyObj.setIsProposalUploaded(RPAConstants.Y);
					} else {
						if (piaggioPolicyObj.getIsProposalUploaded().equalsIgnoreCase(RPAConstants.D)) {
							logger.info("piaggioPolicyPdfUploader - Unable to upload policy pdf for proposalNo :: "
									+ proposalNumber);
							piaggioPolicyObj.setIsProposalUploaded(RPAConstants.E);
						} else {
							piaggioPolicyObj.setIsProposalUploaded(RPAConstants.D);
						}
					}
					if (carUploadResponse != null) {
						piaggioPolicyObj.setProposalRequest(carUploadResponse.getXmlRequest());
						piaggioPolicyObj.setProposalResponse(carUploadResponse.getXmlResponse());
					}
				} else {
					logger.info("piaggioPolicyPdfUploader - Proposal pdf already Uploaded for Proposal code :: "
							+ proposalNumber);
				}

				piaggioPolicyObj.setInwardCode(inwardCode);
				piaggioPolicyService.save(piaggioPolicyObj);
			
		}
		
	} finally {
		if (!userDbId.equals(""))
			logger.info("piaggioPolicyPdfUploader - cabinet disconeccted status ::"
					+ omniDocUploader.cabinetDisconnect(omnicabinetName, userDbId, omniAppServer));
	}

		/* Total policy pdf uploaded */
		transactionInfo.setTotalSuccessRecords(String.valueOf(uplodedPolicyPDFCount));
		logger.info("piaggioPolicyPdfUploader - Uploaded policy count :: " + uplodedPolicyPDFCount);

		/* Total proposal pdf uploaded */
		transactionInfo.setTotalSuccessUploads((String.valueOf(uplodedProposalPDFCount)));
		logger.info("piaggioPolicyPdfUploader - Uploaded Proposal count :: " + uplodedProposalPDFCount);

		logger.info("piaggioPolicyPdfUploader - END getPolicyNumberListTobeUploadedFromLocalDb()");
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
		logger.info("piaggioPolicyPdfUploader - BEGIN getNextInwardCode() called");
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
		logger.info("piaggioPolicyPdfUploader - END downloadPdf() nextInwardCode ::" + nextInwardCode);
		return nextInwardCode;
	}
}
