package com.rpa.model.processors;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="GRID_FILES_COUNT_DETAILS")
public class GridFilesCountDetails  implements  Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Id
	@SequenceGenerator(name = "grid_fileDetails_seq", sequenceName = "grid_fileDetails_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="grid_fileDetails_seq")
	private long fileId;
	
	private long gridId;
	private long transactionInfoId;
	
	
	private String fileName;
	
	private String sheetsName;
	private String sheetsCount;
	private String fileTotalCount;
	public long getFileId() {
		return fileId;
	}
	public void setFileId(long fileId) {
		this.fileId = fileId;
	}
	public long getGridId() {
		return gridId;
	}
	public void setGridId(long gridId) {
		this.gridId = gridId;
	}
	public long getTransactionInfoId() {
		return transactionInfoId;
	}
	public void setTransactionInfoId(long transactionInfoId) {
		this.transactionInfoId = transactionInfoId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getSheetsName() {
		return sheetsName;
	}
	public void setSheetsName(String sheetsName) {
		this.sheetsName = sheetsName;
	}
	public String getSheetsCount() {
		return sheetsCount;
	}
	public void setSheetsCount(String sheetsCount) {
		this.sheetsCount = sheetsCount;
	}
	public String getFileTotalCount() {
		return fileTotalCount;
	}
	public void setFileTotalCount(String fileTotalCount) {
		this.fileTotalCount = fileTotalCount;
	}
	
	
}
