package com.rpa.service.processors;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rpa.model.processors.GridAutomationModel;
import com.rpa.model.processors.GridFilesCountDetails;
import com.rpa.repository.processors.GridAutomationFilesDetailRepository;
import com.rpa.repository.processors.GridAutomationRepository;

@Service
public class GridAutomationServiceImpl implements GridAutomationService{
	
	@Autowired
	GridAutomationRepository gridAutomationRepository;
	
	@Autowired
	GridAutomationFilesDetailRepository gridAutomationFilesDetailRepository;
	
	@Override
	public
	List<GridAutomationModel> FindUploadFilesList(Date date,String Status)
	{
		return gridAutomationRepository.FindUploadFilesList(date, Status);
	}
	@Override
	public
	List<GridAutomationModel> FindValidateFilesList(String Status)
	{	
	return gridAutomationRepository.FindValidateFilesList(Status);
	}
	
	@Override
	public List<GridAutomationModel> FindErrorFileList(Date transactionStartDate, String completed)
	{
		return gridAutomationRepository.FindErrorFileList( transactionStartDate, completed);
	}
	@Override
	public GridAutomationModel save(GridAutomationModel gridAutomationModel) {
		
		return gridAutomationRepository.save(gridAutomationModel);
	}
	@Override
	public List<GridAutomationModel> findByGridId(long transactionId, long gridId) {
		// TODO Auto-generated method stub
		return gridAutomationRepository.findByGridId(transactionId,gridId);
	}
	@Override
	public List<GridAutomationModel> findByTransactionId(long transactionId) {
		// TODO Auto-generated method stub
		return gridAutomationRepository.findByTransactionId(transactionId);
	}
	
	@Override
	public List<GridFilesCountDetails> save(List<GridFilesCountDetails> gridFilesCountDetails) {
		return gridAutomationFilesDetailRepository.save(gridFilesCountDetails);
	}
	@Override
	public List<GridFilesCountDetails> getFilesCountDetails(long transactionId, long gridId) {
		// TODO Auto-generated method stub
		return gridAutomationFilesDetailRepository.getFilesCountDetails(transactionId,gridId);
	}
	
}
