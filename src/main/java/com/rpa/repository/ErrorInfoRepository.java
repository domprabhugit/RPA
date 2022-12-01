package com.rpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.ErrorInfo;

public interface ErrorInfoRepository extends JpaRepository<ErrorInfo, Long>{

	@Query("SELECT s.errorMessage from ErrorInfo s WHERE s.transactionId=?1 ")
	String getTransactionExceptionLog(Long transactionId);

	@Query("SELECT count(*) from ErrorInfo s WHERE s.transactionId=?1 ")
	int isTransactionExceptionExists(Long transactionId);

}
