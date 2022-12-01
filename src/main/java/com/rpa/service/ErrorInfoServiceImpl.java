package com.rpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rpa.model.ErrorInfo;
import com.rpa.repository.ErrorInfoRepository;

@Service
public class ErrorInfoServiceImpl implements ErrorInfoService {

	@Autowired
	private ErrorInfoRepository errorInfoRepository;

	@Override
	public void save(ErrorInfo errInfo) {
		errorInfoRepository.save(errInfo);
	}

	@Override
	public String getTransactionExceptionLog(String transactionId) {
		
		int count = errorInfoRepository.isTransactionExceptionExists(Long.valueOf(transactionId));
		if(count>0)
			return errorInfoRepository.getOne(Long.valueOf(transactionId)).getErrorMessage();
		else
			return "";
		
	}

}
