package com.rpa.repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.TransactionInfo;

public interface TransactionInfoRepository extends JpaRepository<TransactionInfo, Long>{

	/*@Query("SELECT t from TransactionInfo t WHERE t.transactionStartDate > ?1 AND t.transactionStartDate <?2 AND t.processName = ?3  ")*/
	@Query("SELECT t from TransactionInfo t WHERE t.transactionStartDate > ?1 AND t.transactionStartDate <?2 AND t.processName = ?3 and COALESCE(t.status,'Y') <> 'N' ")
	List<TransactionInfo> filterTransactionDetails(Timestamp startDate, Timestamp endDate, String processName);
	
	TransactionInfo findById(Long Id);
	
	@Query("SELECT t from TransactionInfo t WHERE t.runNo = ?1 ")
	List<TransactionInfo> findByRunNo(String RunNo);

	@Query("SELECT t from TransactionInfo t WHERE t.migrationDate = ?1 and status='Y' ")
	List<TransactionInfo> findByMigrationDate(Date countDate);
	
}
