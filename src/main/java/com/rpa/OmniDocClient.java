package com.rpa;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.rpa.constants.RPAConstants;
import com.rpa.wsdl.NGOAddDocDataDefCriteriaDataBDO;
import com.rpa.wsdl.NGOAddDocDataDefCriterionBDO;
import com.rpa.wsdl.NGOAddDocKeywordsCriterionBDO;
import com.rpa.wsdl.NGOAddDocumentBDO;
import com.rpa.wsdl.NGOAddDocumentResponseBDO;
import com.rpa.wsdl.NGOExecuteAPIBDO;
import com.rpa.wsdl.NGOExecuteAPIResponseBDO;

@Component
public class OmniDocClient extends WebServiceGatewaySupport {

	private static final Logger log = LoggerFactory.getLogger(OmniDocClient.class);

	NGOExecuteAPIBDO ngoExecuteApiRequest = new NGOExecuteAPIBDO();
	NGOExecuteAPIResponseBDO ngoExecuteApiResponse = new NGOExecuteAPIResponseBDO();

	NGOAddDocumentBDO ngoAddDocumentRequest = new NGOAddDocumentBDO();
	NGOAddDocumentResponseBDO ngoAddDocumentResponse = new NGOAddDocumentResponseBDO();

	public String getCabinetConnection() throws Exception {

		String cabinetOpenRequest = "<![CDATA[<?xml version='1.0'?><NGOConnectCabinet_Input><Option>NGOConnectCabinet</Option><CabinetName>tstomnidoc</CabinetName><UserName>ditscan</UserName><UserPassword>India@123</UserPassword><UserExist>N</UserExist></NGOConnectCabinet_Input>]]>";
		ngoExecuteApiRequest.setInputXML(cabinetOpenRequest);
		ngoExecuteApiRequest.setBase64Encoded(RPAConstants.N);

		ngoExecuteApiResponse = callSoapService(ngoExecuteApiRequest);

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource src = new InputSource();
		src.setCharacterStream(new StringReader(ngoExecuteApiResponse.getOutputXML()));

		Document doc = builder.parse(src);
		String userDbId = doc.getElementsByTagName("UserDBId").item(0).getTextContent();
		System.out.println("userDbId-->" + userDbId);

		return userDbId;
	}

	public String closeCabinetConnection(String userDbId) throws Exception {

		String cabinetCloseXml = "<![CDATA[<?xml version='1.0'?><NGODisconnectCabinet_Input><Option>NGODisconnectCabinet</Option><CabinetName>tstomnidoc</CabinetName><UserDBId>"
				+ userDbId + "</UserDBId></NGODisconnectCabinet_Input>]]>";
		ngoExecuteApiRequest.setInputXML(cabinetCloseXml);
		ngoExecuteApiRequest.setBase64Encoded(RPAConstants.N);
		ngoExecuteApiResponse = callSoapService(ngoExecuteApiRequest);

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource src = new InputSource();
		src.setCharacterStream(new StringReader(ngoExecuteApiResponse.getOutputXML()));

		Document doc = builder.parse(src);
		String disconnectStatus = doc.getElementsByTagName("Status").item(0).getTextContent();
		System.out.println("disconnectStatus-->" + disconnectStatus);

		return disconnectStatus;
	}

	public String addDocument( String username, String password, String cabinetName,JAXBElement<String> document, int volumeId, String documentSize,
			String docName, int folderIndex, String defName, int defIndex, int indexId, String indexValue,String inwardNumber) throws Exception {

		ngoAddDocumentRequest.setCabinetName(cabinetName);
		ngoAddDocumentRequest.setDocumentName(docName);
		ngoAddDocumentRequest.setFolderIndex(folderIndex);
		ngoAddDocumentRequest.setUserName(username);
		ngoAddDocumentRequest.setUserPassword(password);
		ngoAddDocumentRequest.setDocument(document);
		ngoAddDocumentRequest.setVolumeId(volumeId);
		ngoAddDocumentRequest.setDocumentSize(documentSize);
		
		NGOAddDocDataDefCriterionBDO ngoAddDocDataDefCriterionBDO = new NGOAddDocDataDefCriterionBDO();
		ngoAddDocDataDefCriterionBDO.setDataDefIndex(defIndex);
		ngoAddDocDataDefCriterionBDO.setDataDefName(defName);
		
		NGOAddDocDataDefCriteriaDataBDO ngoAddDocDataDefCriteriaDataBDODoc = new NGOAddDocDataDefCriteriaDataBDO();
		ngoAddDocDataDefCriteriaDataBDODoc.setIndexId(indexId);
		ngoAddDocDataDefCriteriaDataBDODoc.setIndexType("S");
		ngoAddDocDataDefCriteriaDataBDODoc.setIndexValue(indexValue);
		ngoAddDocDataDefCriterionBDO.getNGOAddDocDataDefCriteriaDataBDO().add(ngoAddDocDataDefCriteriaDataBDODoc);
		
		NGOAddDocDataDefCriteriaDataBDO ngoAddDocDataDefCriteriaDataBDOInward = new NGOAddDocDataDefCriteriaDataBDO();
		ngoAddDocDataDefCriteriaDataBDOInward.setIndexId(171);
		ngoAddDocDataDefCriteriaDataBDOInward.setIndexType("S");
		ngoAddDocDataDefCriteriaDataBDOInward.setIndexValue(inwardNumber);
		ngoAddDocDataDefCriterionBDO.getNGOAddDocDataDefCriteriaDataBDO().add(ngoAddDocDataDefCriteriaDataBDOInward);
		
		/*List<NGOAddDocDataDefCriteriaDataBDO> ngoAddDocDataDefCriteriaDataBDOList = new ArrayList<>();

		NGOAddDocDataDefCriteriaDataBDO ngoAddDocDataDefCriteriaDataBDODoc = new NGOAddDocDataDefCriteriaDataBDO();
		ngoAddDocDataDefCriteriaDataBDODoc.setIndexId(indexId);
		ngoAddDocDataDefCriteriaDataBDODoc.setIndexType("S");
		ngoAddDocDataDefCriteriaDataBDODoc.setIndexValue(indexValue);
		ngoAddDocDataDefCriteriaDataBDOList.add(ngoAddDocDataDefCriteriaDataBDODoc);

		NGOAddDocDataDefCriteriaDataBDO ngoAddDocDataDefCriteriaDataBDOInward = new NGOAddDocDataDefCriteriaDataBDO();
		ngoAddDocDataDefCriteriaDataBDOInward.setIndexId(171);
		ngoAddDocDataDefCriteriaDataBDOInward.setIndexType("S");
		ngoAddDocDataDefCriteriaDataBDOInward.setIndexValue(inwardNumber);
		ngoAddDocDataDefCriteriaDataBDOList.add(ngoAddDocDataDefCriteriaDataBDOInward);

		ngoAddDocDataDefCriterionBDO.setNgoAddDocDataDefCriteriaDataBDO(ngoAddDocDataDefCriteriaDataBDOList);*/

		ngoAddDocumentRequest.setNGOAddDocDataDefCriterionBDO(ngoAddDocDataDefCriterionBDO);
		
		NGOAddDocKeywordsCriterionBDO ngoAddDocKeyword =  new NGOAddDocKeywordsCriterionBDO();
		ngoAddDocKeyword.getKeyword();
		ngoAddDocumentRequest.setNGOAddDocKeywordsCriterionBDO(ngoAddDocKeyword);

		ngoAddDocumentResponse = callAddDocumentSoapService(ngoAddDocumentRequest);

		System.out.println("statusCode-->" + ngoAddDocumentResponse.getStatusCode());

		return String.valueOf(ngoAddDocumentResponse.getStatusCode());
	}

	public NGOExecuteAPIResponseBDO callSoapService(NGOExecuteAPIBDO ngoExecuteApiRequest) {
		NGOExecuteAPIResponseBDO ngoExecuteApiResponse = (NGOExecuteAPIResponseBDO) getWebServiceTemplate().marshalSendAndReceive(
				"https://www.royalsundaram.net/OmniDocsWS/services/NGOExecuteAPIService", ngoExecuteApiRequest,
				new SoapActionCallback("http://spring.io/guides/gs-producing-web-service/NGOExecuteAPIBDO"));

		return ngoExecuteApiResponse;
	}

	public NGOAddDocumentResponseBDO callAddDocumentSoapService(NGOAddDocumentBDO request) {
		
		NGOAddDocumentResponseBDO response = (NGOAddDocumentResponseBDO) getWebServiceTemplate().marshalSendAndReceive(
				"https://www.royalsundaram.net/OmniDocsWS/services/NGOAddDocumentService", request,
				new SoapActionCallback("http://spring.io/guides/gs-producing-web-service/NGOAddDocumentBDO"));

		return response;
	}

}