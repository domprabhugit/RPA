/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.apache.camel.Exchange;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import com.rpa.constants.RPAConstants;
import com.rpa.model.BusinessProcess;
import com.rpa.model.EmailConfiguration;
import com.rpa.model.TransactionInfo;
import com.rpa.model.User;
import com.rpa.model.processors.LifeLineMigration;
import com.rpa.repository.process.EmailConfigurationRepository;
import com.rpa.repository.process.ProcessRepository;

@Service("emailService")
public class EmailServiceImpl implements EmailService{

	private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class.getName());

	@Autowired
    private CommonService commonService;
	
	@Autowired
	JavaMailSender mailSender;

	@Autowired
	MailService mailService;
	
	@Autowired
	private EmailConfigurationRepository emailConfigurationRepository;

	@Autowired
	private ProcessRepository processRepository;

	@Override
	public void vb64ComplianceNotification(String bankName, String filePath, TransactionInfo transactionInfo) {

		MimeMessagePreparator preparator = prepareMessageForvb64Compliance(bankName, filePath, transactionInfo);

		try {
			mailSender.send(preparator);
			logger.info("E-mail sent successfully.");
		} catch (MailException ex) {
			logger.error("Error in VB64 E-mail sending::" + ex.getMessage(), ex);
		}
	}

	private MimeMessagePreparator prepareMessageForvb64Compliance(final String bankName, final String filePath, final TransactionInfo transactionInfo) {

		MimeMessagePreparator preparator = new MimeMessagePreparator() {

			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

				String[] tomailds = getToMailIds(transactionInfo.getProcessName());
				if(tomailds!=null){
					helper.setTo(tomailds);
				}else{
					logger.error("64VB Compliance - TO EMAIL ID's NOT FOUND.");	
				}
				String[] ccmailds = getCcMailIds(transactionInfo.getProcessName());
				if(ccmailds!=null){
					helper.setCc(ccmailds);
				}
				helper.setSubject("64VB Upload - Error Records Generated");
				helper.setFrom(RPAConstants.AUTO_REPLY_FROM_EMAIL_ID);

				String content = "<html><body>Dear User,"
						+ "<br/><br/>This is to notify that error records are generated during 64VB Upload for "
						+ bankName + ".<br/><br/>"
						+ "Please find attached the error report.<br/><br/>Regards,<br/>RPA Admin.</body></html>";

				helper.setText(content, true);

				// Add a resource as an attachment
				File file = new File(filePath);
				InputStream inputStream = new FileInputStream(file);
				helper.addAttachment(file.getName(), new ByteArrayResource(IOUtils.toByteArray(inputStream)));
				if(inputStream!=null){
					inputStream.close();
				}
			}
		};
		return preparator;
	}

	@Override
	public void sendEmailForUserCreation(User user) {

		MimeMessagePreparator preparator = getEmailForUserCreation(user);

		try {
			mailSender.send(preparator);
		} catch (MailException ex) {
			System.err.println(ex.getMessage());
		}
	}

	private MimeMessagePreparator getEmailForUserCreation(final User user) {

		MimeMessagePreparator preparator = new MimeMessagePreparator() {

			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

				helper.setSubject("RPA Application - Credentials");
				helper.setFrom("auto-reply@rpa.com");
				helper.setTo(user.getEmailId());
				String content = "<html><body>Dear " + user.getUsername()
				+ ",<br/><br/>Please find below the URL & User Credentials for accessing the RPA Application. "
				+"<br/><br/>"
				+ "URL : " + commonService.getBaseUrl() + RPAConstants.APP_URL_LOGIN
				+"<br/><br/>"
				+ "User Name: " + user.getUsername()
				+"<br/><br/>"
				+ "Password: " + user.getPassword()
				+"<br/><br/>"
				+ "Regards,<br/>RPA Admin.</body></html>";
				helper.setText(content, true);

				// Add a resource as an attachment
				/*File file = new File(filePath);	
                InputStream inputStream = new FileInputStream(file);
                helper.addAttachment(file.getName(),  new ByteArrayResource(IOUtils.toByteArray(inputStream)));*/
			}
		};
		return preparator;
	}

	@Override
	public void sendMigrationStatus(boolean isMismatchAvailable, List<LifeLineMigration> list,Date date,String toMailid) {
		mailService.sendMigrationStatus(isMismatchAvailable, list, date,toMailid);

	}

	public String[] getToMailIds(String processName) {
		String[] toMaildArr = null;
		logger.info("BEGIN - getToMailIds()");
		String toMailIds = "";
		List<EmailConfiguration> EmailConfigurationList = getEmailConfigurationMapping(processName);
		for(EmailConfiguration obj : EmailConfigurationList){
			toMailIds = obj.getToEmailIds();
			if(toMailIds.equals("")){
				logger.error("ToMail is empty for processid -->"+processName);	
			}else{
				toMaildArr = toMailIds.split(",");
			}
		}
		logger.info("END - getToMailIds()");
		return toMaildArr;
	}

	public String[] getCcMailIds(String processName) {
		String[] ccMaildArr = null;
		logger.info("BEGIN - getCcMailIds()");
		String ccMailIds = "";
		List<EmailConfiguration> EmailConfigurationList = getEmailConfigurationMapping(processName);
		for(EmailConfiguration obj : EmailConfigurationList){
			ccMailIds = obj.getCcEmailIds();
			if(ccMailIds!=null && !ccMailIds.equals("")){
				ccMaildArr = ccMailIds.split(",");
			}
		}
		logger.info("END - getCcMailIds()");
		return ccMaildArr;
	}
	
	public List<EmailConfiguration> getEmailConfigurationMapping(String processName) {
		logger.info("BEGIN - getEmailConfigurationMapping()");
		List<EmailConfiguration> list = null;
		BusinessProcess pusinessProcess =processRepository.findByProcessName(processName);
		if(pusinessProcess!=null){
			list = emailConfigurationRepository.findByActiveProcessId(pusinessProcess.getId());	
			if(list.isEmpty()){
				logger.error("Email will not be sent, No Email mapping found for processid -->"+processName);
			}
		}
		return list;
	}
	
	@Override
	public List<EmailConfiguration> findByProcessId(Long processId) {
		return emailConfigurationRepository.findByProcessId(processId);
	}

	@Override
	public void lifeLineInwardUplaodNotification(String errorFilePath, Exchange exchange,TransactionInfo transactionInfo) {
		MimeMessagePreparator preparator = prepareMessageForlifeLineInwardUpload( errorFilePath, exchange,transactionInfo);

		try {
			mailSender.send(preparator);
			logger.info("E-mail sent successfully.");
		} catch (MailException ex) {
			logger.error("Error in lifeLineInwardUplaodNotification E-mail sending::" + ex.getMessage(), ex);
		}
	}

	private MimeMessagePreparator prepareMessageForlifeLineInwardUpload(String errorFilePath,
			Exchange exchange, TransactionInfo transactionInfo) {
		MimeMessagePreparator preparator = new MimeMessagePreparator() {

			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

				String[] tomailds = getToMailIds("IlongueLifelineInwardUpload");
				if(tomailds!=null){
					helper.setTo(tomailds);
				}else{
					logger.error("ToMaild is not mapped for IlongueLifelineInwardUpload processor, Not able to send Error status mail");	
				}
				
				helper.setCc(transactionInfo.getMigrationMailId());
				helper.setSubject("LifeLine Inward upload - Error Records Generated");
				helper.setFrom(RPAConstants.AUTO_REPLY_FROM_EMAIL_ID);

				String content = "<html><body>Dear User,"
						+ "<br/><br/>This is to notify that error records are generated during LifeLine Inward upload for the file "
						+ exchange.getProperty("fileName") + ".<br/><br/>"
						+ "Please find attached the error report.<br/><br/>Regards,<br/>RPA Admin.</body></html>";

				helper.setText(content, true);

				// Add a resource as an attachment
				File file = new File(errorFilePath);
				InputStream inputStream = new FileInputStream(file);
				helper.addAttachment(file.getName(), new ByteArrayResource(IOUtils.toByteArray(inputStream)));
				if(inputStream!=null){
					inputStream.close();
				}

			}
		};
		return preparator;
	}
	
	
	@Override
	public void carPolicyExtractionNotification(TransactionInfo transactionInfo,String errorMsg,String appName) {

		MimeMessagePreparator preparator = prepareMessageForcarPolicyExtraction(transactionInfo,errorMsg,appName);

		try {
			mailSender.send(preparator);
			logger.info("E-mail sent successfully.");
		} catch (MailException ex) {
			logger.error("Error in carPolicyExtractionNotification E-mail sending::" + ex.getMessage(), ex);
		}
	}

	private MimeMessagePreparator prepareMessageForcarPolicyExtraction(TransactionInfo transactionInfo, String errorMsg, String appName) {

		MimeMessagePreparator preparator = new MimeMessagePreparator() {

			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

				String[] tomailds = getToMailIds(transactionInfo.getProcessName());
				if(tomailds!=null){
					helper.setTo(tomailds);
				}else{
					logger.error(appName+" - TO EMAIL ID's NOT FOUND.");	
				}
				String[] ccmailds = getCcMailIds(transactionInfo.getProcessName());
				if(ccmailds!=null){
					helper.setCc(ccmailds);
				}
				helper.setSubject(appName+" Policy Extraction - Error While Accessing Application");
				helper.setFrom(RPAConstants.AUTO_REPLY_FROM_EMAIL_ID);

				String content = "<html><body>Dear User,"
						+ "<br/><br/>"+errorMsg+"<br/><br/>"
						+ "<br/><br/>Regards,<br/>RPA Admin.</body></html>";

				helper.setText(content, true);

			}
		};
		return preparator;
	}
	
	@Override
	public void fileTransferNotification(TransactionInfo transactionInfo,String errorMsg) {

		MimeMessagePreparator preparator = prepareMessageForFileTransferNotification(transactionInfo,errorMsg);

		try {
			mailSender.send(preparator);
			logger.info("E-mail sent successfully.");
		} catch (MailException ex) {
			logger.error("Error in fileTransferNotification E-mail sending::" + ex.getMessage(), ex);
		}
	}
	
	private MimeMessagePreparator prepareMessageForFileTransferNotification(TransactionInfo transactionInfo, String errorMsg) {

		MimeMessagePreparator preparator = new MimeMessagePreparator() {

			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

				String[] tomailds = getToMailIds(transactionInfo.getProcessName());
				if(tomailds!=null){
					helper.setTo(tomailds);
				}else{
					logger.error("SFTP - TO EMAIL ID's NOT FOUND.");	
				}
				String[] ccmailds = getCcMailIds(transactionInfo.getProcessName());
				if(ccmailds!=null){
					helper.setCc(ccmailds);
				}
				helper.setSubject("SFTP - Error While Accessing Application");
				helper.setFrom(RPAConstants.AUTO_REPLY_FROM_EMAIL_ID);

				String content = "<html><body>Dear User,"
						+ "<br/><br/>"+errorMsg+"<br/><br/>"
						+ "<br/><br/>Regards,<br/>RPA Admin.</body></html>";

				helper.setText(content, true);

			}
		};
		return preparator;
	}
	
	@Override
	public void carPolicyMarutiSrcNotification(String errorMsg,String appName) {

		MimeMessagePreparator preparator = prepareMessageForMarutiSrcNotification(errorMsg,appName);

		try {
			mailSender.send(preparator);
			logger.info("E-mail sent successfully.");
		} catch (MailException ex) {
			logger.error("Error in carPolicyExtractionNotification E-mail sending::" + ex.getMessage(), ex);
		}
	}
	
	
	private MimeMessagePreparator prepareMessageForMarutiSrcNotification(String errorMsg, String appName) {

		MimeMessagePreparator preparator = new MimeMessagePreparator() {

			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

					helper.setTo("mohamedismaiel.s@prodian.co.in");
					
				helper.setSubject(appName+" Policy Extraction - src image mismatch may be cancelled policy too");
				helper.setFrom(RPAConstants.AUTO_REPLY_FROM_EMAIL_ID);

				String content = "<html><body>Dear User,"
						+ "<br/><br/>"+errorMsg+"<br/><br/>"
						+ "<br/><br/>Regards,<br/>RPA Admin.</body></html>";

				helper.setText(content, true);

			}
		};
		return preparator;
	}
}