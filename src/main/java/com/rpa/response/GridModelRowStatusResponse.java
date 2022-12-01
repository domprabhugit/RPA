package com.rpa.response;

public class GridModelRowStatusResponse {

	String GridModelId;
	String SheetPairNo;
	String remarks;
	long GridMasterRows;
	long updateRneRows;
	long updateNonRneRows;
	
	
	public String getGridModelId() {
		return GridModelId;
	}
	public void setGridModelId(String gridModelId) {
		GridModelId = gridModelId;
	}
	public String getSheetPairNo() {
		return SheetPairNo;
	}
	public void setSheetPairNo(String sheetPairNo) {
		SheetPairNo = sheetPairNo;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public long getGridMasterRows() {
		return GridMasterRows;
	}
	public void setGridMasterRows(long gridMasterRows) {
		GridMasterRows = gridMasterRows;
	}
	public long getUpdateRneRows() {
		return updateRneRows;
	}
	public void setUpdateRneRows(long updateRneRows) {
		this.updateRneRows = updateRneRows;
	}
	public long getUpdateNonRneRows() {
		return updateNonRneRows;
	}
	public void setUpdateNonRneRows(long updateNonRneRows) {
		this.updateNonRneRows = updateNonRneRows;
	}
	
	
}
