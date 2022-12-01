package com.rpa.response;

public class CarUploadResponse {

	private byte[] xmlRequest;
	
	private byte[] xmlResponse;
	
	private String statusCode;

	public byte[] getXmlRequest() {
		return xmlRequest;
	}

	public void setXmlRequest(byte[] xmlRequest) {
		this.xmlRequest = xmlRequest;
	}

	public byte[] getXmlResponse() {
		return xmlResponse;
	}

	public void setXmlResponse(byte[] xmlResponse) {
		this.xmlResponse = xmlResponse;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	
	
}
