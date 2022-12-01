package com.rpa.service.processors;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rpa.model.processors.GridFilesCountDetails;
import com.rpa.model.processors.GridWithModelSheetAutomationModel;
import com.rpa.repository.processors.GridAutomationFilesDetailRepository;
import com.rpa.repository.processors.GridWithModelSheetAutomationRepository;
import com.rpa.response.GridModelRowStatusResponse;
import com.rpa.util.UtilityFile;

@Service
public class GridWithModelSheetAutomationServiceImpl implements GridWithModelSheetAutomationService{
	
	@Autowired
	GridWithModelSheetAutomationRepository gridAutomationRepository;
	
	@Autowired
	GridAutomationFilesDetailRepository gridAutomationFilesDetailRepository;
	
	@Override
	public
	List<GridWithModelSheetAutomationModel> FindUploadFilesList(Date date,String Status)
	{
		return gridAutomationRepository.FindUploadFilesList(date, Status);
	}
	@Override
	public
	List<GridWithModelSheetAutomationModel> FindValidateFilesList(String Status)
	{	
	return gridAutomationRepository.FindValidateFilesList(Status);
	}
	
	@Override
	public List<GridWithModelSheetAutomationModel> FindErrorFileList(Date transactionStartDate, String completed)
	{
		return gridAutomationRepository.FindErrorFileList( transactionStartDate, completed);
	}
	@Override
	public GridWithModelSheetAutomationModel save(GridWithModelSheetAutomationModel gridAutomationModel) {
		
		return gridAutomationRepository.save(gridAutomationModel);
	}
	@Override
	public List<GridWithModelSheetAutomationModel> findByGridId(long transactionId, long gridId) {
		// TODO Auto-generated method stub
		return gridAutomationRepository.findByGridId(transactionId,gridId);
	}
	@Override
	public List<GridWithModelSheetAutomationModel> findByTransactionId(long transactionId) {
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
	
	@Override
	public List<GridModelRowStatusResponse> getInsertedGridWithModelRowCount(String gridId,String flag) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {
		Connection connection = UtilityFile.getGridDbConnection();
		PreparedStatement statement = null;
		ResultSet rs = null;
		List<GridModelRowStatusResponse> list = new ArrayList<GridModelRowStatusResponse>();
		try{
			String sql ="";
		if(flag.equals("G")){
			 sql = "select grid_id,sheet_pair_no,grid_master_rows,remarks,UPDATE_RNE_ROWS,UPDATE_NON_RNE_ROWS from  GRID_MODEL_ROW_STATUS where GRID_ID='"+gridId+ "'";
		}else if(flag.equals("GWM")){
			 sql = "select grid_model_id,sheet_pair_no,grid_master_rows,remarks,UPDATE_RNE_ROWS,UPDATE_NON_RNE_ROWS from  GRID_MODEL_ROW_STATUS where GRID_MODEL_ID='"+gridId+ "'";
		}
		
		 statement = connection.prepareStatement(sql);
		 rs = statement.executeQuery();
		while (rs.next()) {
			GridModelRowStatusResponse gridModelRowStatusResponse = new GridModelRowStatusResponse();
			gridModelRowStatusResponse.setGridModelId(rs.getString(1));
			gridModelRowStatusResponse.setSheetPairNo(rs.getString(2));
			gridModelRowStatusResponse.setGridMasterRows(rs.getLong(3));
			gridModelRowStatusResponse.setRemarks(rs.getString(4));
			gridModelRowStatusResponse.setUpdateRneRows(rs.getLong(5));
			gridModelRowStatusResponse.setUpdateNonRneRows(rs.getLong(6));
			list.add(gridModelRowStatusResponse);
		}
		}finally{
		if(rs!=null)
			rs.close();
		if(statement!=null)
			statement.close();
		if(connection!=null)
			connection.close();
		}
		return list;
	}
	
}
