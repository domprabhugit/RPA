package com.rpa.service.processors;

import java.util.Date;
import java.util.List;

import com.rpa.model.processors.ModelCodeCreation;

public interface ModelCodeCreationService {

	List<ModelCodeCreation> FindValidateFilesList(String completed);

	List<ModelCodeCreation> FindUploadFilesList(Date date, String Status);

	ModelCodeCreation save(ModelCodeCreation modelCodeCreation);

	List<ModelCodeCreation> FindErrorFileList(Date transactionStartDate, String completed);

	List<ModelCodeCreation> findByTransactionId(long parseLong);

	List<ModelCodeCreation> findByModelCreationId(long parseLong, long parseLong2);

}
