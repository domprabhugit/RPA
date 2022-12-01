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
import org.zeroturnaround.zip.ZipUtil;

import com.rpa.constants.RPAConstants;
import com.rpa.model.processors.LifeLineMigration;
import com.rpa.util.UtilityFile;

@Service("mailService")
public class MailServiceImpl implements MailService {

	@Autowired
	JavaMailSender mailSender;

	@Autowired
	EmailService emailService;

	private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class.getName());

	@Override
	public void sendEmailForGlBatchProcess(String filePath) {

		try {
			int success = 0;
			int fail = 0;
			int total = 0;
			File file = new File(filePath);

			if (file != null) {
				File[] fileList = file.listFiles();

				for (File f : fileList) {
					if (f.isDirectory()) {
						if (f.getName().equalsIgnoreCase("Success")) {
							success = new File(f.getAbsolutePath()).list().length;
						} else if (f.getName().equalsIgnoreCase("Error")) {
							fail = new File(f.getAbsolutePath()).list().length;
						}
					} else {
						f.delete();
					}

				}

			}
			total = success + fail;

			String content = "<html><body>Dear Team" + ",<br/><br/>Please find the status of GL batch post processing. "
					+ "<br/><br/>No of process:&nbsp;&nbsp;&nbsp;<b>" + total + "</b><br/>"
					+ " Success:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>" + success
					+ "</b><br/>Error:&nbsp;&nbsp;&nbsp;:&nbsp;&nbsp;&nbsp;<b>" + fail + "</b><br/><br/><br/><br/>"
					+ "Please find the attached files for your reference.<br/><br/>Regards,<br/>RPA Admin.</body></html>";

			ZipUtil.pack(new File(filePath), new File(filePath + ".zip")); 

			// ZipUtil.unexplode( new File(filePath+".zip")); //TO zip the File
			// Send it For Mail

			MimeMessagePreparator preparator = new MimeMessagePreparator() {

				@Override
				public void prepare(MimeMessage mimeMessage) throws Exception {
					// TODO Auto-generated method stub

					String from = UtilityFile.getBatchProperty("Batch.Process.SendMailFrom");
					MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
					helper.setSubject("GL BATCH Post Process Status_"
							+ UtilityFile.dateToSting(UtilityFile.yesterday(), RPAConstants.dd_slash_MM_slash_yyyy));
					helper.setFrom(from);
					
					String[] tomailds = emailService.getToMailIds("FirstGenBatchProcess");
					if(tomailds!=null){
						helper.setTo(tomailds);
					}else{
						logger.error("Firstgen Batch process - TO EMAIL ID's NOT FOUND.");	
					}
					String[] ccmailds = emailService.getCcMailIds("FirstGenBatchProcess");
					if(ccmailds!=null){
						helper.setCc(ccmailds);
					}
					helper.setText(content, true);
					File file = new File(filePath + ".zip");
					InputStream inputStream = new FileInputStream(file);
					helper.addAttachment(file.getName(), new ByteArrayResource(IOUtils.toByteArray(inputStream)));
					if(inputStream!=null){
						inputStream.close();
					}
				}
			};

			mailSender.send(preparator);

			logger.info(" Batch Process mail send successfully");
		} catch (MailException ex) {
			logger.error(" error in Batch process mail sending" + ex.getMessage(), ex);
		}

		catch (Exception e) {
			logger.error(" error in Batch process mail sending" + e.getMessage(), e);
		}

	}

	@Override
	public void sendMigrationStatus(boolean isMismatchAvailable, List<LifeLineMigration> list, Date date,String toMailid) {
		MimeMessagePreparator preparator = getContentWtihAttachementMessagePreparatorForMigrationStatus(
				isMismatchAvailable, list, date,toMailid);

		try {
			mailSender.send(preparator);
			logger.info("Ilongue Migration Status mail sent successfully");
		} catch (MailException ex) {
			logger.error(" error in sending Ilongue Migration Status mail" + ex.getMessage(), ex);
		}
	}

	private MimeMessagePreparator getContentWtihAttachementMessagePreparatorForMigrationStatus(
			boolean isMismatchAvailable, List<LifeLineMigration> list, Date date, String toMailid) {
		MimeMessagePreparator preparator = new MimeMessagePreparator() {

			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

				String statusString = "";
				if (isMismatchAvailable) {
					statusString = "There is some mismatch between Firstgen & I-Longue Migration Data Dated on " + date
							+ ", PFB for more details";
				} else {
					statusString = "There is no mismatch between Firstgen & I-Longue Migration Data Dated on " + date
							+ ", PFB for more details";
				}

				StringBuffer sb1 = new StringBuffer();
				sb1.append("<html><body><br/>Dear Team,<br/><br/>" + statusString
						+ "<br/><br/><table style='border:0px solid #e8d5d5' border=1 cellpadding='0' cellspacing='0'><tr><td style='width:200px;text-align:left;'><b>Stage Type</b></td><td style='width:75px;text-align:center;' ><b>Firstgen</b></td><td style='width:75px;text-align:center;' ><b>I-longue</b></td></tr>");
				for (int i = 0; i < list.size(); i++) {
					if (i == 0) {
						sb1.append("<tr><td style='width:200px;text-align:left;' >Approved count</td><td style='width:75px;text-align:center;' >").append(list.get(i).getApprovedCount())
						.append("</td><td style='width:75px;text-align:center;'>").append(list.get(i + 1).getApprovedCount())
						.append("</td>").append("</tr>");
						sb1.append("<tr><td style='width:200px;text-align:left;' >Cancelled count</td><td style='width:75px;text-align:center;' >").append(list.get(i).getCancelledCount())
						.append("</td><td style='width:75px;text-align:center;' >").append(list.get(i + 1).getCancelledCount())
						.append("</td>").append("</tr>");
						sb1.append("<tr><td style='width:200px;text-align:left;' >Healthclaims count</td><td style='width:75px;text-align:center;' >").append(list.get(i).getHealthclaims())
						.append("</td><td style='width:75px;text-align:center;' >").append(list.get(i + 1).getHealthclaims())
						.append("</td>").append("</tr>");
						sb1.append("<tr><td style='width:200px;text-align:left;' >Healthinward count</td><td style='width:75px;text-align:center;' >")
						.append(list.get(i).getHealthInwardCount()).append("</td><td style='width:75px;text-align:center;' >")
						.append(list.get(i + 1).getHealthInwardCount()).append("</td>")
						.append("</tr>");
						sb1.append("<tr><td style='width:200px;text-align:left;' >Licenseagent count</td><td style='width:75px;text-align:center;' >").append(list.get(i).getLicenseAgent())
						.append("</td><td style='width:75px;text-align:center;' >").append(list.get(i + 1).getLicenseAgent())
						.append("</td>").append("</tr>");
						sb1.append("<tr><td style='width:200px;text-align:left;' >Lifeline count</td><td style='width:75px;text-align:center;' >").append(list.get(i).getLifelineInward())
						.append("</td><td style='width:75px;text-align:center;' >").append(list.get(i + 1).getLifelineInward())
						.append("</td>").append("</tr>");
						sb1.append("<tr><td style='width:200px;text-align:left;' >Mobileinward count</td><td style='width:75px;text-align:center;' >")
						.append(list.get(i).getMobileInwardCount()).append("</td><td style='width:75px;text-align:center;' >")
						.append(list.get(i + 1).getMobileInwardCount()).append("</td>")
						.append("</tr>");
						sb1.append("<tr><td style='width:200px;text-align:left;' >Motor claims</td><td style='width:75px;text-align:center;' >").append(list.get(i).getMotorClaims())
						.append("</td><td style='width:75px;text-align:center;' >").append(list.get(i + 1).getMotorClaims())
						.append("</td>").append("</tr>");
						sb1.append("<tr><td style='width:200px;text-align:left;' >Pipeline count</td><td style='width:75px;text-align:center;' >").append(list.get(i).getPipelineCount())
						.append("</td><td style='width:75px;text-align:center;' >").append(list.get(i + 1).getPipelineCount())
						.append("</td>").append("</tr>");
						sb1.append("<tr><td style='width:200px;text-align:left;'>Renewalpolicy count</td><td style='width:75px;text-align:center;' >")
						.append(list.get(i).getRenewalPolicyCount()).append("</td><td style='width:75px;text-align:center;' >")
						.append(list.get(i + 1).getRenewalPolicyCount()).append("</td>")
						.append("</tr>");
						sb1.append("<tr><td style='width:200px;text-align:left;'>Xgenhealthclaims count</td><td style='width:75px;text-align:center;' >")
						.append(list.get(i).getXgenHealthclaims()).append("</td><td style='width:75px;text-align:center;' >")
						.append(list.get(i + 1).getXgenHealthclaims()).append("</td>").append("</tr>");
						sb1.append("<tr><td style='width:200px;text-align:left;'>Receipt count</td><td style='width:75px;text-align:center;' >").append(list.get(i).getReceiptCount())
						.append("</td><td style='width:75px;text-align:center;' >").append(list.get(i + 1).getReceiptCount())
						.append("</td>").append("</tr>");

					}
				}
				sb1.append("</table></br><br/><br/>RPA Admin.</body></html>");
				helper.setTo(toMailid);
				/*String[] tomailds = emailService.getToMailIds("LifelineMigrationProcess");
				if(tomailds!=null){
					helper.setTo(tomailds);
				}else{
					logger.error("ToMaild is not mapped for LifelineMigrationProcess processor, Not able to send Migration status mail");	
				}*/
				/*String[] ccmailds = emailService.getCcMailIds("LifelineMigrationProcess");
				if(ccmailds!=null){
					helper.setCc(ccmailds);
				}*/
				helper.setSubject("Firstgen to I-longue Migration Status");
				helper.setFrom("auto-reply@rpa.com");

				helper.setText(sb1.toString(), true);
			}
		};
		return preparator;
	}

	@Override
	public void getPolicyPDFMailPendingstatus(String filePath,int quoteCount) {
		try {
			
			String content = "<html><body>Dear Team" + ",<br/><br/>Please find the Previous day pending Quote status of Policy Retrigger for Failed case RPA Process . "
					+ "<br/><br/>No of Pending Quotes After RPA Retrigger Process :&nbsp;&nbsp;&nbsp;<b>" + quoteCount + "<br/><br/><br/><br/>"
					+ "Please find the attached files for your reference.<br/><br/>Regards,<br/>RPA Admin.</body></html>";

			if(quoteCount>0){
				ZipUtil.pack(new File(filePath), new File(filePath + ".zip")); 
			}
			

			// ZipUtil.unexplode( new File(filePath+".zip")); //TO zip the File
			// Send it For Mail

			MimeMessagePreparator preparator = new MimeMessagePreparator() {

				@Override
				public void prepare(MimeMessage mimeMessage) throws Exception {
					// TODO Auto-generated method stub

					String from = UtilityFile.getBatchProperty("Batch.Process.SendMailFrom");
					MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
					helper.setSubject("RPA Policy PDF Mail Retrigger Pending Status_"
							+ UtilityFile.dateToSting(UtilityFile.yesterday(), RPAConstants.dd_slash_MM_slash_yyyy));
					helper.setFrom(from);
					
					String[] tomailds = emailService.getToMailIds("policyPDFMailRetrigger");
					if(tomailds!=null){
						helper.setTo(tomailds);
					}else{
						logger.error("Policy Retrigger for Failed case - TO EMAIL ID's NOT FOUND.");	
					}
					String[] ccmailds = emailService.getCcMailIds("policyPDFMailRetrigger");
					if(ccmailds!=null){
						helper.setCc(ccmailds);
					}
					helper.setText(content, true);
					
					if(quoteCount>0){
					File file = new File(filePath + ".zip");
					InputStream inputStream = new FileInputStream(file);
					helper.addAttachment(file.getName(), new ByteArrayResource(IOUtils.toByteArray(inputStream)));
					if(inputStream!=null){
						inputStream.close();
					}
					}
				}
			};

			mailSender.send(preparator);

			logger.info(" Policy Retrigger for Failed case - status mail send successfully");
		} catch (MailException ex) {
			logger.error(" Policy Retrigger for Failed case - error in Batch process mail sending" + ex.getMessage(), ex);
		}

		catch (Exception e) {
			logger.error(" Policy Retrigger for Failed case - error in Batch process mail sending" + e.getMessage(), e);
		}
		
	}
}