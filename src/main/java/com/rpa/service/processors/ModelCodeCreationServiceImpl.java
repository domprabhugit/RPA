package com.rpa.service.processors;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rpa.model.processors.ModelCodeCreation;
import com.rpa.repository.processors.ModelCodeCreationRepository;

@Service
public class ModelCodeCreationServiceImpl implements ModelCodeCreationService{
	
	@Autowired
	ModelCodeCreationRepository modelCodeCreationRepository;

	@Override
	public List<ModelCodeCreation> FindValidateFilesList(String Status) {
		return modelCodeCreationRepository.FindValidateFilesList(Status);
	}
	
	@Override
	public
	List<ModelCodeCreation> FindUploadFilesList(Date date,String Status)
	{
		return modelCodeCreationRepository.FindUploadFilesList(date, Status);
	}
	
	@Override
	public ModelCodeCreation save(ModelCodeCreation modelCodeCreation) {
		
		return modelCodeCreationRepository.save(modelCodeCreation);
	}
	
	@Override
	public List<ModelCodeCreation> FindErrorFileList(Date transactionStartDate, String completed)
	{
		return modelCodeCreationRepository.FindErrorFileList( transactionStartDate, completed);
	}

	@Override
	public List<ModelCodeCreation> findByTransactionId(long parseLong) {
		return modelCodeCreationRepository.findByTransactionId(parseLong);
	}

	@Override
	public List<ModelCodeCreation> findByModelCreationId(long transactionId, long modelCreationId) {
		return modelCodeCreationRepository.findByModelCreationId(transactionId,modelCreationId);
	}
	
}
