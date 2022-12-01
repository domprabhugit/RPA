package com.rpa.repository.processors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.DailyFileTransferStatus;

public interface DailyFileTransferStatusRepository extends JpaRepository<DailyFileTransferStatus, Integer> {

	@Query("select s from DailyFileTransferStatus s where s.transferDate=?1")
	DailyFileTransferStatus getDailyTransferStatus(String transferDate);

}
