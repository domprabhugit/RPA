package com.rpa.service.processors;

import java.util.Date;
import java.util.List;

import com.rpa.model.processors.GridAutomationModel;
import com.rpa.model.processors.GridFilesCountDetails;

public interface GridAutomationService {

	List<GridAutomationModel> FindUploadFilesList(Date date, String Status);

	List<GridAutomationModel> FindValidateFilesList(String Status);

	List<GridAutomationModel> FindErrorFileList(Date transactionStartDate, String completed);
	
	GridAutomationModel  save(GridAutomationModel gridAutomationModel);

	List<GridAutomationModel> findByGridId(long transactionId, long gridId);

	List<GridAutomationModel> findByTransactionId(long transactionId);
	
	List<GridFilesCountDetails>  save(List<GridFilesCountDetails> gridFilesCountDetails);
	
	List<GridFilesCountDetails>  getFilesCountDetails(long transactionId, long gridId);
	

	

}
