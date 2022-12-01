/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rpa.model.processors.AdhocSMS;
import com.rpa.repository.processors.AdhocSMSPolicyRepository;

@Service
public class AdhocSMSPolicyServiceImpl implements AdhocSMSPolicyService {

	@Autowired
	private AdhocSMSPolicyRepository adhocSMSPolicyRepository;
	
	@Override
	public List<AdhocSMS> findPoliciesTobeSentSms() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, URISyntaxException {
		
		return adhocSMSPolicyRepository.findPoliciesTobeSentSms();
	}

	@Override
	public AdhocSMS save(AdhocSMS obj) {
		return adhocSMSPolicyRepository.save(obj);
	}

	
	
}
