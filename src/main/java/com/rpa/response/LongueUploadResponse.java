package com.rpa.response;

public class LongueUploadResponse {

	private int rowCount;
	
	private StringBuffer csvBuffer ;

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public StringBuffer getCsvBuffer() {
		return csvBuffer;
	}

	public void setCsvBuffer(StringBuffer csvBuffer) {
		this.csvBuffer = csvBuffer;
	}
	
	
	
}
