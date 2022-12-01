package com.rpa.repository.processors;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.AdhocSMS;


public interface AdhocSMSPolicyRepository  extends JpaRepository<AdhocSMS, Integer>{


	@Query("SELECT s from AdhocSMS s where isSmsSent = 'N' ")
	List<AdhocSMS> findPoliciesTobeSentSms();
	
}
