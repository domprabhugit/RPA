/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.model;

public class VB64EmailPojo {
     
    private String bankName;

    private String fileName;
     
    private CustomerInfo customerInfo;
    
    public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

	public void setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

	@Override
	public String toString() {
		return "VB64EmailPojo [bankName=" + bankName + ", fileName=" + fileName + ", customerInfo=" + customerInfo
				+ "]";
	}
}