package com.rpa.service;

import com.rpa.model.ErrorInfo;

public interface ErrorInfoService {

	void save(ErrorInfo errInfo);

	String getTransactionExceptionLog(String transactionId);

}
