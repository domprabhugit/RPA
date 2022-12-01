package com.rpa.service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.camel.Exchange;

import com.rpa.model.TransactionInfo;

public interface TransactionInfoService {
	List<TransactionInfo> filterTransactionDetails(String startDate, String endDate, String processName) throws ParseException;
	TransactionInfo findById(Long Id);
	boolean inactiveOldTransactions(Exchange exchange, Date dateObj);
}
