package com.rpa.response;

import java.util.List;

import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.LifeLineMigration;

public class MigrationResponse {

	private List<LifeLineMigration> lifeLineMigrationList;
	
	private List<TransactionInfo> transactionInfoList;

	public List<LifeLineMigration> getLifeLineMigrationList() {
		return lifeLineMigrationList;
	}

	public void setLifeLineMigrationList(List<LifeLineMigration> lifeLineMigrationList) {
		this.lifeLineMigrationList = lifeLineMigrationList;
	}

	public List<TransactionInfo> getTransactionInfoList() {
		return transactionInfoList;
	}

	public void setTransactionInfoList(List<TransactionInfo> transactionInfoList) {
		this.transactionInfoList = transactionInfoList;
	}
	
	
}
