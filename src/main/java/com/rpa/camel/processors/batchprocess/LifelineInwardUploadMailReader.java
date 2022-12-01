/*
 * Robotic Process Automation
 * @originalAuthor Vijayananth.M
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.mail.BodyPart;
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
import org.springframework.core.env.Environment;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.util.UtilityFile;

public class LifelineInwardUploadMailReader implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(LifelineInwardUploadMailReader.class.getName());

	private static final int BUFFER_SIZE = 4096;
	
	DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
	
	@Autowired
	private Environment environment;
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Override
	public void process(Exchange exchange) {
			logger.info("*********** inside Camel Process of LifelineInwardMailReader Called ************");
			logger.info("BEGIN : LifelineInwardMailReader Processor - Started ");
			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
			applicationContext = SpringContext.getAppContext();
			environment = applicationContext.getBean(Environment.class);
			LifelineInwardUploadMailReader lifeLineUploadProcessorObj = new LifelineInwardUploadMailReader();
			lifeLineUploadProcessorObj.doProcess(lifeLineUploadProcessorObj,exchange,transactionInfo,environment);
			logger.info("*********** inside Camel Process of LifelineInwardMailReader Processor Ended ************");
	}

	public void doProcess(LifelineInwardUploadMailReader lifelineInwardMailReader, Exchange exchange, TransactionInfo transactionInfo,Environment environment) {
		logger.info("BEGIN : LifelineInwardMailReader Processor - doProcess Method Called  ");
		transactionInfo.setProcessPhase(RPAConstants.MAIL_READER);
		if (lifelineInwardMailReader.accessMailsFromOutlook(exchange,transactionInfo,environment)) {
			logger.info("LifelineInwardMailReader Processor - Mail Process done successfully  ");
		} else {
			transactionInfo.setProcessStatus(RPAConstants.MAIL_READ_ERROR);
			logger.error("Error in LifelineInwardMailReader Processor - Not able to do mail process properly   ");
		}
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		logger.info("BEGIN : LifelineInwardMailReader Processor - doProcess Method Ended  ");
	}

	public boolean accessMailsFromOutlook(Exchange exchange, TransactionInfo transactionInfo, Environment environment) {
		logger.info("BEGIN : LifelineInwardMailReader Processor - accessMailsFromOutlook Method Called  ");
		boolean isAtleastOneMailAvailable = false;
		try {
			String userName = UtilityFile.getIlongueProperty("Ilongue.Lifeline.Mail.User");
			String password = UtilityFile.getIlongueProperty("Ilongue.Lifeline.Mail.password");

			Properties props = new Properties();
				if(Arrays.stream(environment.getActiveProfiles()).anyMatch(
					   env -> (env.equalsIgnoreCase("uat") 
					   || env.equalsIgnoreCase("prod")) )) 
					{
						props.put("mail.host", "outlook.office365.com");
					}
					//Check if Active profiles contains "prod"
					else if(Arrays.stream(environment.getActiveProfiles()).anyMatch(
					   env -> (env.equalsIgnoreCase("dev")) )) 
					{
						props.put("mail.host", "imap.gmail.com");
					}
			//props.put("mail.starttls.enable", "true");
			

			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(userName, password);
				}
			});

			Store store = session.getStore("pop3s");
			logger.info("LifelineInwardMailReader Processor - Getting Pop3s Email Protocal");
			try {
				store.connect();
			} catch (Exception e) {
				logger.error("Error in LifelineInwardMailReader Processor - Not able connect with outlook ::"
						, e);
				return false;
			}

			Folder emailFolder = store.getFolder("INBOX");
			logger.info("LifelineInwardMailReader Processor - Getting Inbox folder");
			emailFolder.open(Folder.READ_WRITE);
			emailFolder.getMessages();
			
			Message[] messages = emailFolder.getMessages();

			logger.info(
					"LifelineInwardMailReader Processor - No of Messages currently available :: " + messages.length);
			logger.info("LifelineInwardMailReader Processor - Reading Mail.........");
			for (int i = 0, n = messages.length; i < n; i++) {
				Message message = messages[i];
				logger.info("LifelineInwardMailReader Processor - Subject :: " + message.getSubject());
				logger.info("LifelineInwardMailReader Processor - Mail Received From :: " + message.getFrom()[0]);

				if (message.getSubject().indexOf("Lifeline MIS -") != -1) {
					transactionInfo.setMigrationMailId(message.getFrom()[0]+"");
					isAtleastOneMailAvailable =true;
					Multipart multipart = (Multipart) message.getContent();
					if (downloadAttachment(multipart,exchange)) {
						exchange.getIn().setHeader("messageObject",message);
						exchange.getIn().setHeader("emailFolderObject",emailFolder);
						exchange.getIn().setHeader("storeObject",store);
					}
					break;
				}

			}
			if(!isAtleastOneMailAvailable){
				transactionInfo.setProcessStatus(RPAConstants.NO_MAIL);
				logger.info("LifelineInwardMailReader Processor- No Ilongue life line inward Mail Available this time.");
				exchange.getIn().setHeader(RPAConstants.NO_MAIL, RPAConstants.Y);
			}else{
				transactionInfo.setProcessStatus(RPAConstants.MAIL_READ);
				exchange.getIn().setHeader(RPAConstants.NO_MAIL, RPAConstants.N);
			}

			// close the store and folder objects
			/*emailFolder.close(true);
			store.close();*/
			logger.info("END : LifelineInwardMailReader Processor - accessMailsFromOutlook Method ");
			return true;
		} catch (Exception e) {
			logger.error(
					"Error in LifelineInwardMailReader Processor - Not able to Read Mail Properly ::" + e.getMessage());
			return false;
		}

	}

	private boolean downloadAttachment(Multipart multipart, Exchange exchange)
			throws FileNotFoundException, MessagingException, IOException, URISyntaxException {
		logger.info("BEGIN : LifelineInwardMailReader Processor - downloadAttachment Method Called  ");
		try {
			for (int k = 0; k < multipart.getCount(); k++) {
				BodyPart bodyPart = multipart.getBodyPart(k);
				if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())
						&& StringUtils.isBlank(bodyPart.getFileName())) {
					continue; // dealing with attachments only
				}
				String fileName ="";
				String downloadFolder = UtilityFile.getCodeBasePath()
						+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_DOWNLOAD);
				
				
				if(bodyPart.getFileName()!=null && !bodyPart.getFileName().toString().endsWith(".zip")){
					
					String extension = "." + FilenameUtils.getExtension(bodyPart.getFileName());

					InputStream is = bodyPart.getInputStream();

					fileName = UtilityFile.getFileNameWithoutExtension(new File(bodyPart.getFileName())).replaceAll("\\s+","") + RPAConstants.UNDERSCORE + df.format(new Date()) + extension;
					;
					FileOutputStream fos = new FileOutputStream(new File(downloadFolder + fileName));
					byte[] buf = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buf)) != -1) {
						fos.write(buf, 0, bytesRead);
					}
					fos.close();
					is.close();
					
				}else{
					 fileName = unzip(bodyPart, downloadFolder);
				}
				
				
				if (!fileName.equals("")) {
					File splitFolder = new File(UtilityFile.getCodeBasePath()
							+ UtilityFile.getIlongueProperty(RPAConstants.ILONGUE_LIFELINE_FOLDER_TO_B_SPLITTED)+UtilityFile.getFileNameWithoutExtension(new File(fileName)));
					if (!splitFolder.exists()) {
						splitFolder.mkdirs();
					}
					FileUtils.copyFileToDirectory(new File(downloadFolder + fileName), splitFolder);
					logger.info("LifelineInwardMailReader Processor - Attachment downloaded successfully ");
					exchange.setProperty("fileName", UtilityFile.getFileNameWithoutExtension(new File(fileName)));
				}

			}
			logger.info("END : LifelineInwardMailReader Processor - downloadAttachment Method ");
			return true;
		} catch (Exception e) {
			logger.error("Error while Saving attachment in LifelineInwardMailReader Processor ::" + e.getMessage());
			return false;
		}
	}

	public String unzip(BodyPart bodyPart, String destDirectory) throws IOException, MessagingException {
		logger.info("BEGIN : LifelineInwardMailReader Processor - unzip Method Called  ");
		String fileName = "",extension="",fileNameWithoutExtension="",dateTimeStamp="",filePath="";
		ZipInputStream zipIn = null;
		try {
			File destDir = new File(destDirectory);
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
			zipIn = new ZipInputStream(bodyPart.getInputStream());
			ZipEntry entry = zipIn.getNextEntry();
			// iterates over entries in the zip file
			while (entry != null) {
				fileName = entry.getName().replaceAll("\\s+","");
				extension = FilenameUtils.getExtension(fileName);
				fileNameWithoutExtension = UtilityFile.getFileNameWithoutExtension(new File(fileName)) ;
				dateTimeStamp = RPAConstants.UNDERSCORE + df.format(new Date());
				fileName = fileNameWithoutExtension+dateTimeStamp+"."+extension;
				filePath = destDirectory + File.separator + fileName;
				if (!entry.isDirectory()) {
					// if the entry is a file, extracts it
					extractFile(zipIn, filePath);
				} else {
					// if the entry is a directory, make the directory
					File dir = new File(filePath);
					dir.mkdirs();
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
			
			logger.info("LifelineInwardMailReader Processor - File Unzipped ");
			logger.info("END : LifelineInwardMailReader Processor - unzip Method ");

			return fileName;
		} catch (Exception e) {
			logger.error("Error while Unzipping attachment in LifelineInwardMailReader Processor ::" + e.getMessage());
			return "";
		}finally{
			if(zipIn!=null){
				zipIn.close();
			}
		}
		

	}

	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		BufferedOutputStream bos = null;
		try {
			logger.info("BEGIN : LifelineInwardMailReader Processor - extractFile Method Called  ");
			bos = new BufferedOutputStream(new FileOutputStream(filePath));
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
			
			logger.info("END : LifelineInwardMailReader Processor - extractFile Method ");
		} catch (Exception e) {
			logger.error("Error while extracting File in extractFile method of LifelineInwardMailReader Processor ::"
					+ e.getMessage());
		}finally{
			if(bos!=null){
				bos.close();
			}
		}
	}

}
