/*
 * Robotic Process Automation
 * @originalAuthor Vijayananth.M
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.batchprocess;

import java.util.Date;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.LifeLineMigration;
import com.rpa.repository.TransactionInfoRepository;
import com.rpa.service.EmailService;
import com.rpa.service.processors.LifelineMigrationService;

public class LifelineInwardMigrationComparator implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(LifelineInwardMigrationComparator.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	LifelineMigrationService lifelineMigrationService;

	@Autowired
	TransactionInfoRepository transactionInfoRepository;

	@Autowired
	private EmailService emailService;

	private void AutoWiringBeanPropertiesSetMethod() {

		logger.info("AutoWiringBeanPropertiesSetMethod is Called in LifelineInwardMigrationComparator Class");
		applicationContext = SpringContext.getAppContext();
		lifelineMigrationService = applicationContext.getBean(LifelineMigrationService.class);
		emailService = applicationContext.getBean(EmailService.class);
		transactionInfoRepository = applicationContext.getBean(TransactionInfoRepository.class);
		logger.info("AutoWiringBeanPropertiesSetMethod is Ended in LifelineInwardMigrationComparator Class");

	}

	@Override
	public void process(Exchange exchange) {
		if (exchange.getIn().getHeader(RPAConstants.NO_FILE) != null
				&& !exchange.getIn().getHeader(RPAConstants.NO_FILE).equals(RPAConstants.Y)) {
			AutoWiringBeanPropertiesSetMethod();
			logger.info("*********** inside Camel Process of LifelineInwardMigrationComparator Called ************");
			logger.info("BEGIN : LifelineInwardMigrationComparator Processor - Started ");
			TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
			LifelineInwardMigrationComparator lifeLineUploadProcessorObj = new LifelineInwardMigrationComparator();
			lifeLineUploadProcessorObj.doProcess(lifeLineUploadProcessorObj, lifelineMigrationService, emailService,
					transactionInfoRepository, transactionInfo, exchange);
			logger.info(
					"*********** inside Camel Process of LifelineInwardMigrationComparator Processor Ended ************");
		}
	}

	public void doProcess(LifelineInwardMigrationComparator lifelineInwardMailReader,
			LifelineMigrationService lifelineMigrationService, EmailService emailService,
			TransactionInfoRepository transactionInfoRepository, TransactionInfo transactionInfo, Exchange exchange) {

		logger.info("BEGIN : LifelineInwardMigrationComparator Processor - doProcess Method Called  ");
		transactionInfo.setProcessPhase(RPAConstants.DATA_COMPARE);
		if (comparePolicies(lifelineMigrationService, emailService, transactionInfoRepository, transactionInfo)) {
			
		} else {
			transactionInfo.setProcessStatus(RPAConstants.DATA_COMPARE_ERROR);
		}
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);
		logger.info("BEGIN : LifelineInwardMigrationComparator Processor - doProcess Method Ended  ");
	}

	private boolean comparePolicies(LifelineMigrationService lifelineMigrationService, EmailService emailService,
			TransactionInfoRepository transactionInfoRepository, TransactionInfo transactionInfo) {
		logger.info("BEGIN : LifelineInwardMigrationComparator Processor - comparePolicies Method Called  ");
		try {
			boolean isMismatchAvailable = false, isAnyDateRecordPendingToCompare = false;
			List<Date> umcomparedDates = lifelineMigrationService
					.getUncomparedDates(transactionInfo.getId());
			for (Date date : umcomparedDates) {
				isAnyDateRecordPendingToCompare = true;
				logger.info(
						"LifelineInwardMigrationComparator Processor - Comparation started for migration data dated on --> "
								+ date);
				List<LifeLineMigration> list = lifelineMigrationService.getUncomparedListByDate(date);
				if (list.size() > 1) {
					for (int i = 0; i < list.size(); i++) {
						if (i == 0) {
							if (list.get(i).getApprovedCount() != (list.get(i + 1).getApprovedCount())) {
								isMismatchAvailable = true;
							} else if (list.get(i).getCancelledCount() != (list.get(i + 1).getCancelledCount())) {
								isMismatchAvailable = true;
							} else if (list.get(i).getHealthclaims() != (list.get(i + 1).getHealthclaims())) {
								isMismatchAvailable = true;
							} else if (list.get(i).getHealthInwardCount() != (list.get(i + 1).getHealthInwardCount())) {
								isMismatchAvailable = true;
							} else if (list.get(i).getLicenseAgent() != (list.get(i + 1).getLicenseAgent())) {
								isMismatchAvailable = true;
							} else if (list.get(i).getLifelineInward() != (list.get(i + 1).getLifelineInward())) {
								isMismatchAvailable = true;
							} else if (list.get(i).getMobileInwardCount() != (list.get(i + 1).getMobileInwardCount())) {
								isMismatchAvailable = true;
							} else if (list.get(i).getMotorClaims() != (list.get(i + 1).getMotorClaims())) {
								isMismatchAvailable = true;
							} else if (list.get(i).getPipelineCount() != (list.get(i + 1).getPipelineCount())) {
								isMismatchAvailable = true;
							} else if (list.get(i).getReceiptCount() != (list.get(i + 1).getReceiptCount())) {
								isMismatchAvailable = true;
							} else if (list.get(i)
									.getRenewalPolicyCount() != (list.get(i + 1).getRenewalPolicyCount())) {
								isMismatchAvailable = true;
							} else if (list.get(i).getXgenHealthclaims() != (list.get(i + 1).getXgenHealthclaims())) {
								isMismatchAvailable = true;
							}
							if (isMismatchAvailable) {
								logger.info(
										"LifelineInwardMigrationComparator Processor - Mismatch b/w Fisrtgen & Ilongue data for the date --> "
												+ date);
							} else {
								logger.info(
										"LifelineInwardMigrationComparator Processor - No Mismatch b/w Fisrtgen & Ilongue data for the date --> "
												+ date);
							}
							/*List<TransactionInfo> transactionInfoList = transactionInfoRepository
									.findByMigrationDate(date);*/
							/*for (TransactionInfo transactionObj : transactionInfoList) {*/
								if (isMismatchAvailable) {
									transactionInfo.setMigrationStatus(RPAConstants.P);
								} else {
									transactionInfo.setMigrationStatus(RPAConstants.C);
								}
								/*transactionInfoRepository.save(transactionInfo);*/
						/*	}*/
							emailService.sendMigrationStatus(isMismatchAvailable, list, date,transactionInfo.getMigrationMailId());
							List<LifeLineMigration> objList = lifelineMigrationService.findByCountDate(date);
							for (LifeLineMigration obj : objList) {
								obj.setIsCompared(RPAConstants.Y);
								lifelineMigrationService.save(obj);
							}
							transactionInfo.setProcessStatus(RPAConstants.DATA_COMPARED);
							logger.info(
									"LifelineInwardMigrationComparator Processor - Comparison done for the date --> "
											+ date);
						}
					}

					transactionInfo.setProcessPhase(RPAConstants.Completed);
				} else {
					transactionInfo.setProcessStatus(RPAConstants.NO_DATA);
					logger.error("Migration Data was not properly inserted for the date -->" + date);
				}

			}

			if (!isAnyDateRecordPendingToCompare) {
				transactionInfo.setProcessStatus(RPAConstants.NO_DATA);
				throw new Exception(RPAConstants.NO_DATA);
			}

			return true;
		} catch (Exception e) {
			logger.info("Error in comparePolicies of LifelineInwardMigrationComparator Processor :: " + e.getMessage());
			return false;
		}
	}

}
