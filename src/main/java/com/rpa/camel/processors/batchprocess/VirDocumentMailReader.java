/*
 * Robotic Process Automation
 * @originalAuthor S.Mohamed Ismaiel
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.APPVIR;
import com.rpa.service.CommonService;
import com.rpa.service.processors.AppVirService;
import com.rpa.util.UtilityFile;

public class VirDocumentMailReader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(VirDocumentMailReader.class.getName());

	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	AppVirService appVirService;
	
	@Autowired
	private CommonService commonService;
	
	@Override
	public void process(Exchange exchange) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
			logger.info("*********** inside Camel Process of VirDocumentMailReader Called ************");
			logger.info("BEGIN : VirDocumentMailReader Processor - Started ");
			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
			applicationContext = SpringContext.getAppContext();
			appVirService = applicationContext.getBean(AppVirService.class);
			commonService = applicationContext.getBean(CommonService.class);
			VirDocumentMailReader lifeLineUploadProcessorObj = new VirDocumentMailReader();
			lifeLineUploadProcessorObj.doProcess(lifeLineUploadProcessorObj,transactionInfo,exchange,appVirService,commonService);
			logger.info(
					"*********** inside Camel Process of VirDocumentMailReader Processor Ended ************");
	}

	public void doProcess(VirDocumentMailReader lifelineInwardMigrationMailReader, TransactionInfo transactionInfo, Exchange exchange, AppVirService appVirService, CommonService commonService) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("BEGIN : VirDocumentMailReader Processor - doProcess Method Called  ");
		transactionInfo.setProcessPhase(RPAConstants.MAIL_READER);
		if (lifelineInwardMigrationMailReader.accessMailsFromOutlook(transactionInfo,exchange,appVirService,commonService)) {
			logger.info("VirDocumentMailReader Processor - Mail Process done successfully  ");
		} else {
			transactionInfo.setProcessStatus(RPAConstants.MAIL_READ_ERROR);
			logger.error(
					"Error in VirDocumentMailReader Processor - Not able to do mail process properly   ");
		}
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		logger.info("BEGIN : VirDocumentMailReader Processor - doProcess Method Ended  ");
	}

	public boolean accessMailsFromOutlook(TransactionInfo transactionInfo, Exchange exchange, AppVirService appVirService, CommonService commonService) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		logger.info("BEGIN : VirDocumentMailReader Processor - accessMailsFromOutlook Method Called  ");

		List<APPVIR> autoInspektPolicyNoList = null;
		if (exchange.getProperty(RPAConstants.EXTRACTION_TYPE).equals(RPAConstants.NORMAL)) {
			autoInspektPolicyNoList = appVirService.findPdfToBeDownloaded(
					commonService.getThresholdFrequencyLevel(RPAConstants.VIRAPP_DOWNLOAD_THRESHOLD));
		} else {
			String startDate = UtilityFile.getVIRProperty(RPAConstants.VIRAPP_BACKLOG_STARTDATE);
			String endDate = UtilityFile.getVIRProperty(RPAConstants.VIRAPP_BACKLOG_ENDDATE);
			autoInspektPolicyNoList = appVirService.findPdfToBeDownloadedForBackLogPolicies(startDate, endDate,
					commonService.getThresholdFrequencyLevel(RPAConstants.VIRAPP_DOWNLOAD_THRESHOLD));
		}
		logger.info("virDocDocExtracter - Unextracted Policies count ::" + autoInspektPolicyNoList.size());
		/* Total policies taken for extraction */
		transactionInfo.setTotalRecords(String.valueOf(autoInspektPolicyNoList.size()));
		int downloadedDocCount = 0;
		try {
			String userName = UtilityFile.getVIRProperty("vir.mail.user");
			String password = UtilityFile.getVIRProperty("vir.mail.password");
			Properties props = new Properties();
			 //props.put("mail.host", "outlook.office365.com");
			props.put("mail.host", "pop.gmail.com");
			
			
			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(userName, password);
				}
			});
			session.setDebug(true);
			Store store = session.getStore("pop3s");
			logger.info("VirDocumentMailReader Processor - Getting Pop3s Email Protocal");
			try {
				store.connect();
			} catch (Exception e) {
				logger.error("Error in VirDocumentMailReader Processor - Not able connect with outlook ::"
						+ e.getMessage());
				return false;
			}

			Folder emailFolder = store.getFolder("INBOX");
			try{
			logger.info("VirDocumentMailReader Processor - Getting Inbox folder");
			emailFolder.open(Folder.READ_WRITE);
			emailFolder.getMessages();
			/*
			 * SearchTerm term = null; term = new SubjectTerm("Lifeline MIS -");
			 * logger.info(
			 * "VirDocumentMailReader Processor - Filtering Mail By Subject (Lifeline MIS -) "
			 * ); Message[] messages = emailFolder.search(term);
			 */
			Message[] messages = emailFolder.getMessages();
			/*
			List list = new ArrayList<>();
			list.add("C20191115000034");
			list.add("RS20191114000482");
			list.add("RS20191115000081");*/
			
			HashMap<String,APPVIR> map = new HashMap<>(); 
			List<String> virNumberListFromDB = new ArrayList<>();
			APPVIR curObj = null;
			for (APPVIR autoInspektPolicyObj : autoInspektPolicyNoList) {
				virNumberListFromDB.add(autoInspektPolicyObj.getVirNumber());
				map.put(autoInspektPolicyObj.getVirNumber(), autoInspektPolicyObj);
			}
			
			@SuppressWarnings("unchecked")
			Map<String,APPVIR> copyOfMap = (Map<String, APPVIR>) map.clone();

			
			logger.info("VirDocumentMailReader Processor - No of Messages currently available :: "
					+ messages.length);
			logger.info("VirDocumentMailReader Processor - Reading Mail.........");
			boolean isAtleastOneMigrationMailAvailable = false;
			for (int i = 0, n = messages.length; i < n; i++) {
				Message message = messages[i];
				logger.info("VirDocumentMailReader Processor - Subject :: " + message.getSubject());
				logger.info(
						"VirDocumentMailReader Processor - Mail Received From :: " + message.getFrom()[0]);

				

				if (message.getSubject().indexOf("Vehicle Inspection Approved for") != -1) {
					String virNumber = message.getSubject().substring(message.getSubject().lastIndexOf(" ") + 1).trim();
					curObj = map.get(virNumber);
					if(virNumberListFromDB.contains(virNumber)){
						copyOfMap.remove(virNumber);
						//availableVirInMaillist.add(virNumber);
					transactionInfo.setMigrationMailId(message.getFrom()[0]+"");
					isAtleastOneMigrationMailAvailable =true;
					Multipart multipart = (Multipart) message.getContent();
					
					if (downloadAttachment(multipart,transactionInfo,virNumber,curObj,appVirService)) {
						downloadedDocCount++;
						message.setFlag(Flags.Flag.DELETED, true);
						//message.setFlags(flag, set);
						//curObj.setIsReportDownloaded(RPAConstants.Y);
						logger.info(
								"VirDocumentMailReader Processor - Mail Deleted from Inbox which is having the Subject :: "
										+ message.getSubject());
					}else{
						curObj.setIsReportDownloaded(RPAConstants.E);
						appVirService.save(curObj);
					}
					
					}else{
						logger.info(
								"VirDocumentMailReader Processor - Read Mail not from db vir number");
					}

				}

			}
			
			for (Map.Entry<String,APPVIR> entry : copyOfMap.entrySet())  {
				APPVIR obj = entry.getValue();
				obj.setIsReportDownloaded(RPAConstants.R);
				appVirService.save(obj);
	    } 
			
			transactionInfo.setTotalSuccessRecords(String.valueOf(downloadedDocCount));
			if(!isAtleastOneMigrationMailAvailable){
				transactionInfo.setProcessStatus(RPAConstants.NO_MAIL);
				logger.info("VirDocumentMailReader Processor- No VIR Mail Available this time.");
				exchange.getIn().setHeader(RPAConstants.NO_MAIL, RPAConstants.Y);
			}else{
				transactionInfo.setProcessStatus(RPAConstants.MAIL_READ);
				exchange.getIn().setHeader(RPAConstants.NO_MAIL, RPAConstants.N);
			}

			}finally{
			// close the store and folder objects
				if (emailFolder.isOpen()) {
				//emailFolder.expunge();
				emailFolder.close(true);
				}
			}
			store.close();
			logger.info("END : VirDocumentMailReader Processor - accessMailsFromOutlook Method ");
			return true;
		} catch (Exception e) {
			logger.error("Error in VirDocumentMailReader Processor - Not able to Read Mail Properly ::"
					+ e.getMessage());
			return false;
		}

	}

	private boolean downloadAttachment(Multipart multipart, TransactionInfo transactionInfo, String virNumber, APPVIR curObj, AppVirService appVirService)
			throws FileNotFoundException, MessagingException, IOException {
		try {
			for (int k = 0; k < multipart.getCount(); k++) {
				BodyPart bodyPart = multipart.getBodyPart(k);
				if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())
						&& StringUtils.isBlank(bodyPart.getFileName())) {
					continue; // dealing with attachments only
				}
				logger.info("VirDocumentMailReader Processor- file to be downloaded -->"+bodyPart.getFileName());
				
				String extension = "."+FilenameUtils.getExtension(bodyPart.getFileName());
						
				InputStream is = bodyPart.getInputStream();
				String downloadFolder, uploadFolder;
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");

				downloadFolder = UtilityFile.getCodeBasePath()
						+ UtilityFile.getVIRProperty(RPAConstants.VIR_FOLDER_DOWNLOAD);
				uploadFolder = UtilityFile.getCodeBasePath()
						+ UtilityFile.getVIRProperty(RPAConstants.VIR_FOLDER_UPLOAD)+ UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY);;
				
				String fileName = "";
				fileName = virNumber + RPAConstants.UNDERSCORE + df.format(new Date())+extension;
				
				File f  = new File(downloadFolder);
				if(!f.exists()){
					f.mkdirs();
				}
				File processDirectory  = new File(uploadFolder);
				if(!processDirectory.exists()){
					processDirectory.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(new File(downloadFolder+fileName));
				byte[] buf = new byte[4096];
				int bytesRead;
				while ((bytesRead = is.read(buf)) != -1) {
					fos.write(buf, 0, bytesRead);
				}
				fos.close();
				is.close();
				logger.info("VirDocumentMailReader Processor- file saved in download folder :: "+fileName);
				FileUtils.copyFileToDirectory(new File(downloadFolder + fileName), new File(uploadFolder));
				curObj.setReportPdfPath(uploadFolder+"/"+fileName);
				curObj.setIsReportDownloaded(RPAConstants.Y);
					//transactionInfo.setFirstgenFileName(fileName);	
				
				
				logger.info("VirDocumentMailReader Processor- file saved in process folder :: "+fileName);
			}
			appVirService.save(curObj);
		} catch (URISyntaxException e) {
			logger.error("Error in VirDocumentMailReader Processor - Not able to download attachment ::"
					+ e.getMessage());
			return false;
		}

		return true;
	}

}
