/*
 * Robotic Process Automation
 * @originalAuthor Mohamed ismaiel.s
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.model.processors;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "DAILY_FILE_TRANSFER_STATUS")
public class DailyFileTransferStatus  implements Serializable   {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	private String transferDate;
	
	private String isSbsExtractionTransferred;
	
	private String isSbsRenewalTransferred;

	public String getTransferDate() {
		return transferDate;
	}

	public void setTransferDate(String transferDate) {
		this.transferDate = transferDate;
	}

	public String getIsSbsExtractionTransferred() {
		return isSbsExtractionTransferred;
	}

	public void setIsSbsExtractionTransferred(String isSbsExtractionTransferred) {
		this.isSbsExtractionTransferred = isSbsExtractionTransferred;
	}

	public String getIsSbsRenewalTransferred() {
		return isSbsRenewalTransferred;
	}

	public void setIsSbsRenewalTransferred(String isSbsRenewalTransferred) {
		this.isSbsRenewalTransferred = isSbsRenewalTransferred;
	}
	
	
	
}
