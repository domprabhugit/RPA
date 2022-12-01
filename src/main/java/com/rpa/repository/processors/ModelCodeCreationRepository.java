package com.rpa.repository.processors;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.ModelCodeCreation;

public interface ModelCodeCreationRepository extends JpaRepository<ModelCodeCreation, Integer>  {
	
	@Query("SELECT s from ModelCodeCreation s WHERE s.startDate=?1 and s.fileStatus <>?2 and s.endDate is null and isModelValidated ='Y' and  isProcessed <> 'Y' ")
	List<ModelCodeCreation> FindUploadFilesList(Date date,String Status);

	@Query("SELECT s from ModelCodeCreation s WHERE  s.fileStatus <>?1 and s.endDate is null and isModelValidated <> 'Y' OR (isModelValidated ='Y' and isProcessed <> 'Y') ")
	List<ModelCodeCreation> FindValidateFilesList(String Status);

	@Query("SELECT s from ModelCodeCreation s WHERE s.startDate=?1 and s.fileStatus <>?2 and s.endDate is null and isModelValidated ='F' and  isProcessed <> 'Y' ")
	List<ModelCodeCreation> FindErrorFileList(Date transactionStartDate, String completed);

	@Query("SELECT s from ModelCodeCreation s WHERE s.transactionInfoId=?1")
	List<ModelCodeCreation> findByTransactionId(long parseLong);

	@Query("SELECT s from ModelCodeCreation s WHERE s.transactionInfoId=?1 and s.id =?2")
	List<ModelCodeCreation> findByModelCreationId(long transactionId, long modelCreationId);
	
}
