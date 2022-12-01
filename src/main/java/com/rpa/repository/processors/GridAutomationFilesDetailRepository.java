package com.rpa.repository.processors;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.GridAutomationModel;
import com.rpa.model.processors.GridFilesCountDetails;

public interface GridAutomationFilesDetailRepository extends JpaRepository<GridFilesCountDetails, Integer>  {
	
	
  @Query("SELECT s from GridFilesCountDetails s WHERE s.transactionInfoId =?1 and s.gridId =?2   order by s.fileId ")
	List<GridFilesCountDetails> getFilesCountDetails(long transactionId, long gridId);
	
	
	
	



}
