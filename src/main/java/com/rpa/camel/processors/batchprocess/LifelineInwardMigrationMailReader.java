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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.util.UtilityFile;

public class LifelineInwardMigrationMailReader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(LifelineInwardMigrationMailReader.class.getName());

	@Override
	public void process(Exchange exchange) {
			logger.info("*********** inside Camel Process of LifelineInwardMigrationMailReader Called ************");
			logger.info("BEGIN : LifelineInwardMigrationMailReader Processor - Started ");
			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
			LifelineInwardMigrationMailReader lifeLineUploadProcessorObj = new LifelineInwardMigrationMailReader();
			lifeLineUploadProcessorObj.doProcess(lifeLineUploadProcessorObj,transactionInfo,exchange);
			logger.info(
					"*********** inside Camel Process of LifelineInwardMigrationMailReader Processor Ended ************");
	}

	public void doProcess(LifelineInwardMigrationMailReader lifelineInwardMigrationMailReader, TransactionInfo transactionInfo, Exchange exchange) {
		logger.info("BEGIN : LifelineInwardMigrationMailReader Processor - doProcess Method Called  ");
		transactionInfo.setProcessPhase(RPAConstants.MAIL_READER);
		if (lifelineInwardMigrationMailReader.accessMailsFromOutlook(transactionInfo,exchange)) {
			logger.info("LifelineInwardMigrationMailReader Processor - Mail Process done successfully  ");
		} else {
			transactionInfo.setProcessStatus(RPAConstants.MAIL_READ_ERROR);
			logger.error(
					"Error in LifelineInwardMigrationMailReader Processor - Not able to do mail process properly   ");
		}
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		logger.info("BEGIN : LifelineInwardMigrationMailReader Processor - doProcess Method Ended  ");
	}

	public boolean accessMailsFromOutlook(TransactionInfo transactionInfo, Exchange exchange) {
		logger.info("BEGIN : LifelineInwardMigrationMailReader Processor - accessMailsFromOutlook Method Called  ");

		try {
			String userName = UtilityFile.getIlongueProperty("Ilongue.Lifeline.Mail.User");
			String password = UtilityFile.getIlongueProperty("Ilongue.Lifeline.Mail.password");

			Properties props = new Properties();
			// props.put("mail.host", "outlook.office365.com");
			props.put("mail.host", "imap.gmail.com");

			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(userName, password);
				}
			});

			Store store = session.getStore("pop3s");
			logger.info("LifelineInwardMigrationMailReader Processor - Getting Pop3s Email Protocal");
			try {
				store.connect();
			} catch (Exception e) {
				logger.error("Error in LifelineInwardMigrationMailReader Processor - Not able connect with outlook ::"
						+ e.getMessage());
				return false;
			}

			Folder emailFolder = store.getFolder("INBOX");
			logger.info("LifelineInwardMigrationMailReader Processor - Getting Inbox folder");
			emailFolder.open(Folder.READ_WRITE);
			emailFolder.getMessages();
			/*
			 * SearchTerm term = null; term = new SubjectTerm("Lifeline MIS -");
			 * logger.info(
			 * "LifelineInwardMigrationMailReader Processor - Filtering Mail By Subject (Lifeline MIS -) "
			 * ); Message[] messages = emailFolder.search(term);
			 */
			Message[] messages = emailFolder.getMessages();

			logger.info("LifelineInwardMigrationMailReader Processor - No of Messages currently available :: "
					+ messages.length);
			logger.info("LifelineInwardMigrationMailReader Processor - Reading Mail.........");
			boolean isAtleastOneMigrationMailAvailable = false;
			for (int i = 0, n = messages.length; i < n; i++) {
				Message message = messages[i];
				logger.info("LifelineInwardMigrationMailReader Processor - Subject :: " + message.getSubject());
				logger.info(
						"LifelineInwardMigrationMailReader Processor - Mail Received From :: " + message.getFrom()[0]);

				String appFlag = "0";
				if (message.getSubject().indexOf("ILINK_MIG_TRANSACTION") != -1) {
					transactionInfo.setMigrationMailId(message.getFrom()[0]+"");
					isAtleastOneMigrationMailAvailable =true;
					if (message.getSubject().indexOf("ILINK_MIG_TRANSACTION_COUNT_REPORT")!=-1) {
						appFlag = "F";
					} else if (message.getSubject().indexOf("ILINK_MIG_TRANSACTION & SESSION DETAILS Report")!=-1) {
						appFlag = "I";
					}
					Multipart multipart = (Multipart) message.getContent();
					if (downloadAttachment(multipart, appFlag,transactionInfo)) {
						message.setFlag(Flags.Flag.DELETED, true);
						logger.info(
								"LifelineInwardMigrationMailReader Processor - Mail Deleted from Inbox which is having the Subject :: "
										+ message.getSubject());
					}

				}

			}
			if(!isAtleastOneMigrationMailAvailable){
				transactionInfo.setProcessStatus(RPAConstants.NO_MAIL);
				logger.info("LifelineInwardMigrationMailReader Processor- No Ilongue Migration Mail Available this time.");
				exchange.getIn().setHeader(RPAConstants.NO_MAIL, RPAConstants.Y);
			}else{
				transactionInfo.setProcessStatus(RPAConstants.MAIL_READ);
				exchange.getIn().setHeader(RPAConstants.NO_MAIL, RPAConstants.N);
			}

			// close the store and folder objects
			emailFolder.close(true);
			store.close();
			logger.info("END : LifelineInwardMigrationMailReader Processor - accessMailsFromOutlook Method ");
			return true;
		} catch (Exception e) {
			logger.error("Error in LifelineInwardMigrationMailReader Processor - Not able to Read Mail Properly ::"
					+ e.getMessage());
			return false;
		}

	}

	private boolean downloadAttachment(Multipart multipart, String appFlag, TransactionInfo transactionInfo)
			throws FileNotFoundException, MessagingException, IOException {
		try {
			for (int k = 0; k < multipart.getCount(); k++) {
				BodyPart bodyPart = multipart.getBodyPart(k);
				if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())
						&& StringUtils.isBlank(bodyPart.getFileName())) {
					continue; // dealing with attachments only
				}
				logger.info("LifelineInwardMigrationMailReader Processor- file to be downloaded -->"+bodyPart.getFileName());
				
				String extension = "."+FilenameUtils.getExtension(bodyPart.getFileName());
						
				InputStream is = bodyPart.getInputStream();
				String downloadFolder, processFolder;
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");

				downloadFolder = UtilityFile.getCodeBasePath()
						+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_MIGRRATION_FOLDER_DOWNLOAD);
				processFolder = UtilityFile.getCodeBasePath()
						+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_MIGRRATION_FOLDER_PROCESS);
				String fileName = "";
				if (appFlag.equals(RPAConstants.F)) {
					fileName = RPAConstants.FIRSTGEN + RPAConstants.UNDERSCORE + df.format(new Date())+extension;
				} else {
					fileName = RPAConstants.ILONGUE + RPAConstants.UNDERSCORE + df.format(new Date())+extension;
				}
				File f  = new File(downloadFolder);
				if(!f.exists()){
					f.mkdirs();
				}
				File processDirectory  = new File(processFolder);
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
				logger.info("LifelineInwardMigrationMailReader Processor- file saved in download folder :: "+fileName);
				FileUtils.copyFileToDirectory(new File(downloadFolder + fileName), new File(processFolder));
				if (appFlag.equals(RPAConstants.F)) {
					transactionInfo.setFirstgenFileName(fileName);	
				}else{
					transactionInfo.setIlongueFileName(fileName);
				}
				
				logger.info("LifelineInwardMigrationMailReader Processor- file saved in process folder :: "+fileName);
			}
		} catch (URISyntaxException e) {
			logger.error("Error in LifelineInwardMigrationMailReader Processor - Not able to download attachment ::"
					+ e.getMessage());
			return false;
		}

		return true;
	}

}
