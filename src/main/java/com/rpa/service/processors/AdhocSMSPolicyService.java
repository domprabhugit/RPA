/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import com.rpa.model.processors.AdhocSMS;

public interface AdhocSMSPolicyService {
	
	List<AdhocSMS> findPoliciesTobeSentSms() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, URISyntaxException;

	AdhocSMS save(AdhocSMS obj);


}
