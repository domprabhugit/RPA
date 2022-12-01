package com.rpa.repository.processors;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.GridWithModelSheetAutomationModel;

public interface GridWithModelSheetAutomationRepository extends JpaRepository<GridWithModelSheetAutomationModel, Integer>  {
	
	@Query("SELECT s from GridWithModelSheetAutomationModel s WHERE s.startDate=?1 and s.fileStatus <>?2 and s.endDate is null and isValidated ='Y' and  isProcessed <> 'Y' and isProcessed <> 'S' ")
	List<GridWithModelSheetAutomationModel> FindUploadFilesList(Date date,String Status);
	
	
	
	@Query("SELECT s from GridWithModelSheetAutomationModel s WHERE  s.fileStatus <>?1 and s.endDate is null and isValidated <> 'Y' OR (isValidated ='Y' and isProcessed <> 'Y') ")
	List<GridWithModelSheetAutomationModel> FindValidateFilesList(String Status);


	@Query("SELECT s from GridWithModelSheetAutomationModel s WHERE s.startDate=?1 and s.fileStatus <>?2 and s.endDate is null and isValidated ='F' and  isProcessed <> 'Y' ")
	List<GridWithModelSheetAutomationModel> FindErrorFileList(Date transactionStartDate, String completed);


	@Query("SELECT s from GridWithModelSheetAutomationModel s WHERE s.transactionInfoId=?1 and s.id =?2")
    List<GridWithModelSheetAutomationModel> findByGridId(long transactionId, long gridId);


	@Query("SELECT s from GridWithModelSheetAutomationModel s WHERE s.transactionInfoId=?1")
	List<GridWithModelSheetAutomationModel> findByTransactionId(long transactionId);


	
}
