package com.rpa.repository.processors;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.GridAutomationModel;

public interface GridAutomationRepository extends JpaRepository<GridAutomationModel, Integer>  {
	
	@Query("SELECT s from GridAutomationModel s WHERE s.startDate=?1 and s.fileStatus <>?2 and s.endDate is null and isValidated ='Y' and  isProcessed <> 'Y' and isProcessed <> 'S' ")
	List<GridAutomationModel> FindUploadFilesList(Date date,String Status);
	
	
	
	@Query("SELECT s from GridAutomationModel s WHERE  s.fileStatus <>?1 and s.endDate is null and isValidated <> 'Y' OR (isValidated ='Y' and isProcessed <> 'Y') ")
	List<GridAutomationModel> FindValidateFilesList(String Status);


	@Query("SELECT s from GridAutomationModel s WHERE s.startDate=?1 and s.fileStatus <>?2 and s.endDate is null and isValidated ='F' and  isProcessed <> 'Y' ")
	List<GridAutomationModel> FindErrorFileList(Date transactionStartDate, String completed);


	@Query("SELECT s from GridAutomationModel s WHERE s.transactionInfoId=?1 and s.id =?2")
    List<GridAutomationModel> findByGridId(long transactionId, long gridId);


	@Query("SELECT s from GridAutomationModel s WHERE s.transactionInfoId=?1")
	List<GridAutomationModel> findByTransactionId(long transactionId);


	
}
