package com.rpa.service.processors;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.rpa.model.processors.GridWithModelSheetAutomationModel;
import com.rpa.response.GridModelRowStatusResponse;
import com.rpa.model.processors.GridFilesCountDetails;

public interface GridWithModelSheetAutomationService {

	List<GridWithModelSheetAutomationModel> FindUploadFilesList(Date date, String Status);

	List<GridWithModelSheetAutomationModel> FindValidateFilesList(String Status);

	List<GridWithModelSheetAutomationModel> FindErrorFileList(Date transactionStartDate, String completed);
	
	GridWithModelSheetAutomationModel  save(GridWithModelSheetAutomationModel gridAutomationModel);

	List<GridWithModelSheetAutomationModel> findByGridId(long transactionId, long gridId);

	List<GridWithModelSheetAutomationModel> findByTransactionId(long transactionId);
	
	List<GridFilesCountDetails>  save(List<GridFilesCountDetails> gridFilesCountDetails);
	
	List<GridFilesCountDetails>  getFilesCountDetails(long transactionId, long gridId);

	List<GridModelRowStatusResponse> getInsertedGridWithModelRowCount(String gridId, String flag) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException;
	

	

}
