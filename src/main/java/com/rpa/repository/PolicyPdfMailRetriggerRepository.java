/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.PolicyPdfMailRetrigger;

public interface PolicyPdfMailRetriggerRepository extends JpaRepository<PolicyPdfMailRetrigger, Long>{

	@Query("SELECT s from PolicyPdfMailRetrigger s WHERE transactionRefNo=?1")
	PolicyPdfMailRetrigger getPdfMailTriggerStatus(long parseLong);

}
