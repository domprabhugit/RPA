package com.rpa;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.xml.dtm.ref.DTMNodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.rpa.response.CarUploadResponse;
@Component
public class OmniDocsUploader {

	private static final Logger logger = LoggerFactory.getLogger(OmniDocsUploader.class.getName());
	
	public CarUploadResponse uploadDoc(String username, String password, String cabinetName,String document, int volumeId, String documentSize,
			String docName, String folderIndex, String defName, String defIndex, String indexId, String indexValue,String inwardNumber,String inwardIndexId,String applicationServer) throws MalformedURLException,
	IOException, ParserConfigurationException, TransformerFactoryConfigurationError, SAXException, TransformerException, XPathExpressionException {
		
		CarUploadResponse marutiUploadResponse = new CarUploadResponse();
	 
	String responseString = "",statusCode ="";
	String outputString = "";
	/*String wsURL = "http://www.royalsundaram.net/OmniDocsWS/services/NGOAddDocumentService?wsdl";*/
	String wsURL = applicationServer+"/OmniDocsWS/services/NGOAddDocumentService?wsdl";
	URL url = new URL(wsURL);
	URLConnection connection = url.openConnection();
	HttpURLConnection httpConn = (HttpURLConnection)connection;
	ByteArrayOutputStream bout = new ByteArrayOutputStream();
	
	/*String xmlInput =" <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ngo=\"http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/\">\n"+
			//" <soapenv:Header/>\n" +
			" <soapenv:Body>\n" +
			" <ngo:NGOAddDocumentBDO>\n" +
			" <cabinetName>"+cabinetName+"</cabinetName>\n" +
			" <documentPath></documentPath>\n" +
			" <folderIndex>"+folderIndex+"</folderIndex>\n" +
			" <documentName>"+docName+"</documentName>\n" +
			" <userDBId></userDBId>\n" +
			" <volumeId>"+volumeId+"</volumeId>\n" +
			
			//" <creationDateTime></creationDateTime>\n" +
			
			" <versionFlag></versionFlag>\n" +
			" <accessType></accessType>\n" +
		
			//" <documentType></documentType>\n" +
			//" <createdByAppName></createdByAppName>\n" +
			//" <noOfPages></noOfPages>\n" +
			//" <DocumentSize>"+documentSize+"</DocumentSize>\n" +
			//" <FTSDocumentIndex></FTSDocumentIndex>\n" +
			//" <textISIndex></textISIndex>\n" +
			//" <ODMADocumentIndex></ODMADocumentIndex>\n" +
			
			" <enableLog></enableLog>\n" +
			" <comment></comment>\n" +
			" <author></author>\n" +
			
			//" <FTSFlag></FTSFlag>\n" +
			//" <groupIndex></groupIndex>\n" +
			
			" <ownerIndex></ownerIndex>\n" +
			" <nameLength></nameLength>\n" +
			" <versionComment></versionComment>\n" +
			
			//" <duplicateName></duplicateName>\n" +
			
			" <textAlsoFlag></textAlsoFlag>\n" +
			" <imageData></imageData>\n" +
			
			//" <transactionRequired></transactionRequired>\n" +
			
			" <validateDocumentImage></validateDocumentImage>\n" +
			" <ownerType></ownerType>\n" +
			
			//" <signFlag></signFlag>\n" +
			
			" <thumbNailFlag></thumbNailFlag>\n" +
			" <userName>"+username+"</userName>\n" +
			" <userPassword>"+password+"</userPassword>\n" +
			" <!--Optional:-->\n" +
			" <document>"+document+"</document>\n" +
			" <!--Optional:-->\n" +
			" <ngo:NGOAddDocDataDefCriterionBDO>\n" +
			" <dataDefIndex>"+defIndex+"</dataDefIndex>\n" +
			" <dataDefName>"+defName+"</dataDefName>\n" +
			" <!--1 or more repetitions:-->\n" +
			" <ngo:NGOAddDocDataDefCriteriaDataBDO>\n" +
			" <indexId>"+indexId+"</indexId>\n" +
			" <indexType>S</indexType>\n" +
			" <indexValue>"+indexValue+"</indexValue>\n" +
			" </ngo:NGOAddDocDataDefCriteriaDataBDO>\n" +
			" <ngo:NGOAddDocDataDefCriteriaDataBDO>\n" +
			" <indexId>"+inwardIndexId+"</indexId>\n" +
			" <indexType>S</indexType>\n" +
			" <indexValue>"+inwardNumber+"</indexValue>\n" +
			" </ngo:NGOAddDocDataDefCriteriaDataBDO>\n" +
			" </ngo:NGOAddDocDataDefCriterionBDO>\n" +
			" <!--Optional:-->\n" +
			" <ngo:NGOAddDocKeywordsCriterionBDO>\n" +
			" <!--Zero or more repetitions:-->\n" +
			" <keyword></keyword>\n" +
			" </ngo:NGOAddDocKeywordsCriterionBDO>\n" +
			" </ngo:NGOAddDocumentBDO>\n" +
			" </soapenv:Body>\n" +
			" </soapenv:Envelope>";
		*/
	
	String xmlInput =" <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ngo=\"http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/\">\n"+
			" <soapenv:Header/>\n" +
			" <soapenv:Body>\n" +
			" <ngo:NGOAddDocumentBDO>\n" +
			" <cabinetName>"+cabinetName+"</cabinetName>\n" +
			" <documentPath></documentPath>\n" +
			" <folderIndex>"+folderIndex+"</folderIndex>\n" +
			" <documentName>"+docName+"</documentName>\n" +
			" <userDBId></userDBId>\n" +
			" <volumeId>"+volumeId+"</volumeId>\n" +
			" <creationDateTime></creationDateTime>\n" +
			" <versionFlag></versionFlag>\n" +
			" <accessType></accessType>\n" +
			" <documentType></documentType>\n" +
			" <createdByAppName></createdByAppName>\n" +
			" <noOfPages></noOfPages>\n" +
			" <DocumentSize>"+documentSize+"</DocumentSize>\n" +
			" <FTSDocumentIndex></FTSDocumentIndex>\n" +
			" <textISIndex></textISIndex>\n" +
			" <ODMADocumentIndex></ODMADocumentIndex>\n" +
			" <enableLog></enableLog>\n" +
			" <comment></comment>\n" +
			" <author></author>\n" +
			" <FTSFlag></FTSFlag>\n" +
			" <groupIndex></groupIndex>\n" +
			" <ownerIndex></ownerIndex>\n" +
			" <nameLength></nameLength>\n" +
			" <versionComment></versionComment>\n" +
			" <duplicateName></duplicateName>\n" +
			" <textAlsoFlag></textAlsoFlag>\n" +
			" <imageData></imageData>\n" +
			" <transactionRequired></transactionRequired>\n" +
			" <validateDocumentImage></validateDocumentImage>\n" +
			" <ownerType></ownerType>\n" +
			" <signFlag></signFlag>\n" +
			" <thumbNailFlag></thumbNailFlag>\n" +
			" <userName>"+username+"</userName>\n" +
			" <userPassword>"+password+"</userPassword>\n" +
			" <!--Optional:-->\n" +
			" <document>"+document+"</document>\n" +
			" <!--Optional:-->\n" +
			" <ngo:NGOAddDocDataDefCriterionBDO>\n" +
			" <dataDefIndex>"+defIndex+"</dataDefIndex>\n" +
			" <dataDefName>"+defName+"</dataDefName>\n" +
			" <!--1 or more repetitions:-->\n" +
			" <ngo:NGOAddDocDataDefCriteriaDataBDO>\n" +
			" <indexId>"+indexId+"</indexId>\n" +
			" <indexType>S</indexType>\n" +
			" <indexValue>"+indexValue+"</indexValue>\n" +
			" </ngo:NGOAddDocDataDefCriteriaDataBDO>\n" +
			" <ngo:NGOAddDocDataDefCriteriaDataBDO>\n" +
			" <indexId>"+inwardIndexId+"</indexId>\n" +
			" <indexType>S</indexType>\n" +
			" <indexValue>"+inwardNumber+"</indexValue>\n" +
			" </ngo:NGOAddDocDataDefCriteriaDataBDO>\n" +
			" </ngo:NGOAddDocDataDefCriterionBDO>\n" +
			" <!--Optional:-->\n" +
			" <ngo:NGOAddDocKeywordsCriterionBDO>\n" +
			" <!--Zero or more repetitions:-->\n" +
			" <keyword></keyword>\n" +
			" </ngo:NGOAddDocKeywordsCriterionBDO>\n" +
			" </ngo:NGOAddDocumentBDO>\n" +
			" </soapenv:Body>\n" +
			" </soapenv:Envelope>";
	
	//logger.info(" OmniDocsUploader - add document xmlInput request :: "+ xmlInput); 
			
	byte[] buffer = new byte[xmlInput.length()];
	buffer = xmlInput.getBytes();
	bout.write(buffer);
	byte[] b = bout.toByteArray();
	marutiUploadResponse.setXmlRequest(b);
	//String SOAPAction ="http://www.royalsundaram.net:8080/OmniDocsWS/services/NGOAddDocumentService";
	//Set the appropriate HTTP parameters.
	httpConn.setRequestProperty("Content-Length",
	String.valueOf(b.length));
	httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
	httpConn.setRequestProperty("SOAPAction", "");
	httpConn.setRequestMethod("POST");
	httpConn.setDoOutput(true);
	httpConn.setDoInput(true);
	
	
	OutputStream out = httpConn.getOutputStream();
	//Write the content of the request to the outputstream of the HTTP Connection.
	out.write(b);
	out.close();
	//Ready with sending the request.
	 try{
	//Read the response.
	InputStreamReader isr =
	new InputStreamReader(httpConn.getInputStream());
	BufferedReader in = new BufferedReader(isr);
	
	 
	//Write the SOAP message response to a String.
	while ((responseString = in.readLine()) != null) {
		//System.out.println("responseString-->"+responseString);
	outputString = outputString + responseString;
	}
	 }catch(Exception e){
		 logger.error("OmniDocsUploader - error in uploading doc "+docName+" :: "+ e);
		 marutiUploadResponse.setStatusCode("E");
			return marutiUploadResponse;
	 }
	
	outputString = StringEscapeUtils.unescapeXml(outputString.toString());
	
	//logger.info(" OmniDocsUploader - add document xmloutput response :: "+ outputString); 
	
	marutiUploadResponse.setXmlResponse(outputString.getBytes());
	
	 statusCode = getElementValue("statusCode",outputString);
	logger.info(" OmniDocsUploader statusCode: " + statusCode +" for indexValue :: "+ indexValue);
	marutiUploadResponse.setStatusCode(statusCode);
		return marutiUploadResponse;
	}
	
	
	public String cabinetConnect(String username, String password, String cabinetName,String applicationServer) throws IOException, XPathExpressionException {

		String responseString = "",userDBId ="";
		String outputString = "";
		/*String wsURL = "www.royalsundaram.net/OmniDocsWS/services/NGOAddDocumentService?wsdl";*/
		String wsURL = applicationServer+"/OmniDocsWS/services/NGOExecuteAPIService?wsdl";
		URL url = new URL(wsURL);
		URLConnection connection = url.openConnection();
		HttpURLConnection httpConn = (HttpURLConnection)connection;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		String xmlInput =" <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ngo=\"http://bdo.ws.jts.omni.newgen.com/NGOExecuteAPIService/\">\n" +
		" <soapenv:Header/>\n" +
		" <soapenv:Body>\n" +
		" <ngo:NGOExecuteAPIBDO>\n" +
		" <inputXML><![CDATA[<?xml version='1.0'?>\n" +
		" <NGOConnectCabinet_Input>\n" +
		" <Option>NGOConnectCabinet</Option>\n" +
		" <CabinetName>"+cabinetName+"</CabinetName>\n" +    
		" <UserName>"+username+"</UserName>\n" +   
		" <UserPassword>"+password+"</UserPassword>\n" + 
		" <UserExist>N</UserExist>\n" +
		" </NGOConnectCabinet_Input>]]></inputXML>\n" +
		" <base64Encoded>N</base64Encoded>\n" +
		" </ngo:NGOExecuteAPIBDO>\n" +
		" </soapenv:Body>\n" +
		" </soapenv:Envelope>";
		
		//logger.info(" OmniDocsUploader - cabinetConnect - xmlInput request :: "+ xmlInput); 
		
		byte[] buffer = new byte[xmlInput.length()];
		buffer = xmlInput.getBytes();
		bout.write(buffer);
		byte[] b = bout.toByteArray();
		//Set the appropriate HTTP parameters.
		httpConn.setRequestProperty("Content-Length",
		String.valueOf(b.length));
		httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		httpConn.setRequestProperty("SOAPAction", "");
		httpConn.setRequestMethod("POST");
		httpConn.setDoOutput(true);
		httpConn.setDoInput(true);
		
		
		OutputStream out = httpConn.getOutputStream();
		//Write the content of the request to the outputstream of the HTTP Connection.
		out.write(b);
		out.close();
		//Ready with sending the request.
		 
		//Read the response.
		InputStreamReader isr =
		new InputStreamReader(httpConn.getInputStream());
		BufferedReader in = new BufferedReader(isr);
		 
		//Write the SOAP message response to a String.
		while ((responseString = in.readLine()) != null) {
			//System.out.println("responseString-->"+responseString);
		outputString = outputString + responseString;
		}
		
		outputString = StringEscapeUtils.unescapeXml(outputString.toString());
		
		outputString = outputString.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
		
		//outputString = outputString.replace("<outputXML>", "<outputXML><![CDATA[").replace("</outputXML>", "]]></outputXML>");
		
		//logger.info(" OmniDocsUploader - cabinet connect xmloutput response :: "+ outputString); 
		
		userDBId = getElementValueByXpath("UserDBId",outputString);
		logger.info(" OmniDocsUploader cabninet connect userDBId: " + userDBId);
		
		return userDBId;
	}
	
	
	public String cabinetDisconnect(String cabinetName,String userDBId,String applicationServer) throws IOException, XPathExpressionException {

		String responseString = "",statusCode ="";
		String outputString = "";
		String wsURL = applicationServer+"/OmniDocsWS/services/NGOExecuteAPIService?wsdl";
		URL url = new URL(wsURL);
		URLConnection connection = url.openConnection();
		HttpURLConnection httpConn = (HttpURLConnection)connection;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		String xmlInput =" <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ngo=\"http://bdo.ws.jts.omni.newgen.com/NGOExecuteAPIService/\">\n" +
		" <soapenv:Header/>\n" +
		" <soapenv:Body>\n" +
		" <ngo:NGOExecuteAPIBDO>\n" +
		" <inputXML><![CDATA[<?xml version='1.0'?>\n" +
		" <NGODisconnectCabinet_Input>\n" +
		" <Option>NGODisconnectCabinet</Option>\n" +
		" <CabinetName>"+cabinetName+"</CabinetName>\n" +    
		" <UserDBId>"+userDBId+"</UserDBId>\n" +   
		" </NGODisconnectCabinet_Input>]]></inputXML>\n" +
		" <base64Encoded>N</base64Encoded>\n" +
		" </ngo:NGOExecuteAPIBDO>\n" +
		" </soapenv:Body>\n" +
		" </soapenv:Envelope>";
		
		//logger.info(" OmniDocsUploader - cabinetDisconnect - xmlInput request :: "+ xmlInput); 
		
		byte[] buffer = new byte[xmlInput.length()];
		buffer = xmlInput.getBytes();
		bout.write(buffer);
		byte[] b = bout.toByteArray();
		//Set the appropriate HTTP parameters.
		httpConn.setRequestProperty("Content-Length",
		String.valueOf(b.length));
		httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		httpConn.setRequestProperty("SOAPAction", "");
		httpConn.setRequestMethod("POST");
		httpConn.setDoOutput(true);
		httpConn.setDoInput(true);
		
		
		OutputStream out = httpConn.getOutputStream();
		//Write the content of the request to the outputstream of the HTTP Connection.
		out.write(b);
		out.close();
		//Ready with sending the request.
		 
		//Read the response.
		InputStreamReader isr =
		new InputStreamReader(httpConn.getInputStream());
		BufferedReader in = new BufferedReader(isr);
		 
		//Write the SOAP message response to a String.
		while ((responseString = in.readLine()) != null) {
			//System.out.println("responseString-->"+responseString);
		outputString = outputString + responseString;
		}
		
		outputString = StringEscapeUtils.unescapeXml(outputString.toString());
		
		outputString = outputString.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
		//outputString = outputString.replace("<outputXML>", "<outputXML><![CDATA[").replace("</outputXML>", "]]></outputXML>");
		
		//logger.info(" OmniDocsUploader - cabinet disconnect xmloutput response :: "+ outputString);
		
		statusCode = getElementValueByXpath("Status",outputString);
		logger.info(" OmniDocsUploader cabinet disconnect statusCode: " + statusCode);
		
		return userDBId;
	}
	
	public synchronized String addFolder(String userDBId, String parentFolderIndex, String folderName,String cabinetName,String applicationServer) throws IOException, XPathExpressionException {

		String responseString = "",statusCode ="",newFolderIndex="";
		String outputString = "";
		/*String wsURL = "http://www.royalsundaram.net/OmniDocsWS/services/NGOAddDocumentService?wsdl";*/
		String wsURL = applicationServer+"/OmniDocsWS/services/NGOExecuteAPIService?wsdl";
		URL url = new URL(wsURL);
		URLConnection connection = url.openConnection();
		HttpURLConnection httpConn = (HttpURLConnection)connection;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		String xmlInput =" <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ngo=\"http://bdo.ws.jts.omni.newgen.com/NGOExecuteAPIService/\">\n" +
		" <soapenv:Header/>\n" +
		" <soapenv:Body>\n" +
		" <ngo:NGOExecuteAPIBDO>\n" +
		" <inputXML><![CDATA[<?xml version=\"1.0?\">\n" +
		" <NGOAddFolder_Input>\n" +
		" <Option>NGOAddFolder</Option>\n" +
		" <CabinetName>"+cabinetName+"</CabinetName>\n" +
		" <UserDBId>"+userDBId+"</UserDBId>\n" +
		" <Folder>\n" +
		" <ParentFolderIndex>"+parentFolderIndex+"</ParentFolderIndex>\n" +
		" <FolderName>"+folderName+"</FolderName>\n" +
		" <CreationDateTime></CreationDateTime>\n" +
		" <AccessType>I</AccessType>\n" +
		" <ImageVolumeIndex></ImageVolumeIndex>\n" +
		" <FolderType>G</FolderType>\n" +
		" <Location>G</Location>\n" +
		" <Comment>UserFolder</Comment>\n" +
		" <EnableFTSFlag>N</EnableFTSFlag>\n" +
		" <NoOfDocuments>0</NoOfDocuments>\n" +
		" <NoOfSubFolders>0</NoOfSubFolders>\n" +
		" <DataDefinition>\n" +
		" <DataDefName>OEM</DataDefName>\n" +
		" </DataDefinition>\n" +
		" </Folder>\n" +
		" </NGOAddFolder_Input>\n" +
		" ]]></inputXML>\n" +
		" <base64Encoded>N</base64Encoded>\n" +
		" </ngo:NGOExecuteAPIBDO>\n" +
		" </soapenv:Body>\n" +
		" </soapenv:Envelope>";
		
		//logger.info(" OmniDocsUploader - addFolder - xmlInput request :: "+ xmlInput); 
		
		byte[] buffer = new byte[xmlInput.length()];
		buffer = xmlInput.getBytes();
		bout.write(buffer);
		byte[] b = bout.toByteArray();
		//Set the appropriate HTTP parameters.
		httpConn.setRequestProperty("Content-Length",
		String.valueOf(b.length));
		httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		httpConn.setRequestProperty("SOAPAction", "");
		httpConn.setRequestMethod("POST");
		httpConn.setDoOutput(true);
		httpConn.setDoInput(true);
		
		
		OutputStream out = httpConn.getOutputStream();
		//Write the content of the request to the outputstream of the HTTP Connection.
		out.write(b);
		out.close();
		//Ready with sending the request.
		 
		//Read the response.
		InputStreamReader isr =
		new InputStreamReader(httpConn.getInputStream());
		BufferedReader in = new BufferedReader(isr);
		 
		//Write the SOAP message response to a String.
		while ((responseString = in.readLine()) != null) {
			//System.out.println("responseString-->"+responseString);
		outputString = outputString + responseString;
		}
		
		outputString = StringEscapeUtils.unescapeXml(outputString.toString());
		
		outputString = outputString.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
		
		//logger.info(" OmniDocsUploader - add folder xmloutput response :: "+ outputString);
		
		statusCode = getElementValueByXpath("Status",outputString);
		
		if(statusCode.equalsIgnoreCase("0")){
			newFolderIndex = getElementValueByXpath("FolderIndex",outputString);
		}
		
		logger.info(" OmniDocsUploader addFolder statuscode: " + statusCode);
		
		return newFolderIndex;
	}
	
/*	public static void main(String[] args) throws ParserConfigurationException, TransformerFactoryConfigurationError, SAXException, TransformerException {
		OmniDocsUploader omniDocsUploader =
		new OmniDocsUploader();
		try {
			omniDocsUploader.uploadDoc("");
		} catch (MalformedURLException e) {
		e.printStackTrace();
		} catch (IOException e) {
		e.printStackTrace();
		}
	}
	*/
	
	private String getElementValue(String tagName, String xmlString) throws XPathExpressionException {

	        try {

	            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder builder = factory.newDocumentBuilder();
	            Document document = builder.parse(new InputSource(new StringReader(xmlString)));
	            Element rootElement = document.getDocumentElement();
	            
	            NodeList list = rootElement.getElementsByTagName(tagName);

	            if (list != null && list.getLength() > 0) {
	                NodeList subList = list.item(0).getChildNodes();

	                if (subList != null && subList.getLength() > 0) {
	                    return subList.item(0).getNodeValue();
	                }
	            }
	        } catch (ParserConfigurationException e) {
	            e.printStackTrace();
	        } catch (SAXException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }


	        return null;
	    }
	
	
	private String getElementValueByXpath(String tagName, String xmlString) throws XPathExpressionException {

        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlString)));
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("//"+tagName);
            NodeList nl = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            return ((DTMNodeList) nl).getDTMIterator().toString();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }
	
	public CarUploadResponse uploadDocWithOutInwardCode(String username, String password, String cabinetName,String document, int volumeId, String documentSize,
			String docName, String folderIndex, String defName, String defIndex, String policyIndexId, String policyNo,String documentTypeIndexId,String ApplicationServerIp) throws MalformedURLException,
	IOException, ParserConfigurationException, TransformerFactoryConfigurationError, SAXException, TransformerException, XPathExpressionException {
		
		CarUploadResponse marutiUploadResponse = new CarUploadResponse();
	 
	String responseString = "",statusCode ="";
	String outputString = "";
	/*String wsURL = "http://www.royalsundaram.net/OmniDocsWS/services/NGOAddDocumentService?wsdl";*/
	String wsURL = ApplicationServerIp+"/OmniDocsWS/services/NGOAddDocumentService?wsdl";
	URL url = new URL(wsURL);
	URLConnection connection = url.openConnection();
	HttpURLConnection httpConn = (HttpURLConnection)connection;
	ByteArrayOutputStream bout = new ByteArrayOutputStream();
	
	String xmlInput =" <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ngo=\"http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/\">\n"+
			" <soapenv:Header/>\n" +
			" <soapenv:Body>\n" +
			" <ngo:NGOAddDocumentBDO>\n" +
			" <cabinetName>"+cabinetName+"</cabinetName>\n" +
			" <documentPath></documentPath>\n" +
			" <folderIndex>"+folderIndex+"</folderIndex>\n" +
			" <documentName>"+docName+"</documentName>\n" +
			" <userDBId></userDBId>\n" +
			" <volumeId>"+volumeId+"</volumeId>\n" +
			" <creationDateTime></creationDateTime>\n" +
			" <versionFlag></versionFlag>\n" +
			" <accessType></accessType>\n" +
			" <documentType></documentType>\n" +
			" <createdByAppName></createdByAppName>\n" +
			" <noOfPages></noOfPages>\n" +
			" <DocumentSize>"+documentSize+"</DocumentSize>\n" +
			" <FTSDocumentIndex></FTSDocumentIndex>\n" +
			" <textISIndex></textISIndex>\n" +
			" <ODMADocumentIndex></ODMADocumentIndex>\n" +
			" <enableLog></enableLog>\n" +
			" <comment></comment>\n" +
			" <author></author>\n" +
			" <FTSFlag></FTSFlag>\n" +
			" <groupIndex></groupIndex>\n" +
			" <ownerIndex></ownerIndex>\n" +
			" <nameLength></nameLength>\n" +
			" <versionComment></versionComment>\n" +
			" <duplicateName></duplicateName>\n" +
			" <textAlsoFlag></textAlsoFlag>\n" +
			" <imageData></imageData>\n" +
			" <transactionRequired></transactionRequired>\n" +
			" <validateDocumentImage></validateDocumentImage>\n" +
			" <ownerType></ownerType>\n" +
			" <signFlag></signFlag>\n" +
			" <thumbNailFlag></thumbNailFlag>\n" +
			" <userName>"+username+"</userName>\n" +
			" <userPassword>"+password+"</userPassword>\n" +
			" <!--Optional:-->\n" +
			" <document>"+document+"</document>\n" +
			" <!--Optional:-->\n" +
			" <ngo:NGOAddDocDataDefCriterionBDO>\n" +
			" <dataDefIndex>"+defIndex+"</dataDefIndex>\n" +
			" <dataDefName>"+defName+"</dataDefName>\n" +
			" <!--1 or more repetitions:-->\n" +
			" <ngo:NGOAddDocDataDefCriteriaDataBDO>\n" +
			/*" <indexId>"+documentTypeIndexId+"</indexId>\n" +*/
			" <indexId>86</indexId>\n" +
			" <indexType>S</indexType>\n" +
			" <indexValue>"+docName+"</indexValue>\n" +
			" </ngo:NGOAddDocDataDefCriteriaDataBDO>\n" +
			" <ngo:NGOAddDocDataDefCriteriaDataBDO>\n" +
			/*" <indexId>"+policyIndexId+"</indexId>\n" +*/
			" <indexId>87</indexId>\n" +
			" <indexType>S</indexType>\n" +
			" <indexValue>"+policyNo+"</indexValue>\n" +
			" </ngo:NGOAddDocDataDefCriteriaDataBDO>\n" +
			" </ngo:NGOAddDocDataDefCriterionBDO>\n" +
			" <!--Optional:-->\n" +
			" <ngo:NGOAddDocKeywordsCriterionBDO>\n" +
			" <!--Zero or more repetitions:-->\n" +
			" <keyword></keyword>\n" +
			" </ngo:NGOAddDocKeywordsCriterionBDO>\n" +
			" </ngo:NGOAddDocumentBDO>\n" +
			" </soapenv:Body>\n" +
			" </soapenv:Envelope>";
	
	byte[] buffer = new byte[xmlInput.length()];
	buffer = xmlInput.getBytes();
	bout.write(buffer);
	byte[] b = bout.toByteArray();
	marutiUploadResponse.setXmlRequest(b);
	//String SOAPAction ="http://www.royalsundaram.net:8080/OmniDocsWS/services/NGOAddDocumentService";
	//Set the appropriate HTTP parameters.
	httpConn.setRequestProperty("Content-Length",
	String.valueOf(b.length));
	httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
	httpConn.setRequestProperty("SOAPAction", "");
	httpConn.setRequestMethod("POST");
	httpConn.setDoOutput(true);
	httpConn.setDoInput(true);
	
	
	OutputStream out = httpConn.getOutputStream();
	//Write the content of the request to the outputstream of the HTTP Connection.
	out.write(b);
	out.close();
	//Ready with sending the request.
	 
	//Read the response.
	InputStreamReader isr =
	new InputStreamReader(httpConn.getInputStream());
	BufferedReader in = new BufferedReader(isr);
	 
	//Write the SOAP message response to a String.
	while ((responseString = in.readLine()) != null) {
		//System.out.println("responseString-->"+responseString);
	outputString = outputString + responseString;
	}
	
	outputString = StringEscapeUtils.unescapeXml(outputString.toString());
	
	marutiUploadResponse.setXmlResponse(outputString.getBytes());
	
	 statusCode = getElementValue("statusCode",outputString);
	logger.info(" OmniDocsUploader statusCode: " + statusCode +" for policyNo :: "+ policyNo);
	marutiUploadResponse.setStatusCode(statusCode);
		return marutiUploadResponse;
	}
	
	
	public String getFolderIndexByName(String cabinetName,String applicationServer,String userdbid,String folderName,String parentFolderIndex) throws IOException, XPathExpressionException {

		String responseString = "",folderIndex ="";
		String outputString = "";
		/*String wsURL = "www.royalsundaram.net/OmniDocsWS/services/NGOAddDocumentService?wsdl";*/
		String wsURL = applicationServer+"/OmniDocsWS/services/NGOExecuteAPIService?wsdl";
		URL url = new URL(wsURL);
		URLConnection connection = url.openConnection();
		HttpURLConnection httpConn = (HttpURLConnection)connection;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		String xmlInput =" <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ngo=\"http://bdo.ws.jts.omni.newgen.com/NGOExecuteAPIService/\">\n" +
		" <soapenv:Header/>\n" +
		" <soapenv:Body>\n" +
		" <ngo:NGOExecuteAPIBDO>\n" +
		" <inputXML><![CDATA[<?xml version='1.0'?>\n" +
		" <NGOGetFolderIdForName_Input>\n" +
		" <Option>NGOGetFolderIdForName</Option>\n" +
		" <CabinetName>"+cabinetName+"</CabinetName>\n" +    
		" <UserDBId>"+userdbid+"</UserDBId>\n" +   
		" <FolderName>"+folderName+"</FolderName>\n" + 
		" <ParentFolderIndex>"+parentFolderIndex+"</ParentFolderIndex>\n" +
		" </NGOGetFolderIdForName_Input]]></inputXML>\n" +
		" <base64Encoded>N</base64Encoded>\n" +
		" </ngo:NGOExecuteAPIBDO>\n" +
		" </soapenv:Body>\n" +
		" </soapenv:Envelope>";
		
		//logger.info(" OmniDocsUploader - cabinetConnect - xmlInput request :: "+ xmlInput); 
		
		byte[] buffer = new byte[xmlInput.length()];
		buffer = xmlInput.getBytes();
		bout.write(buffer);
		byte[] b = bout.toByteArray();
		//Set the appropriate HTTP parameters.
		httpConn.setRequestProperty("Content-Length",
		String.valueOf(b.length));
		httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		httpConn.setRequestProperty("SOAPAction", "");
		httpConn.setRequestMethod("POST");
		httpConn.setDoOutput(true);
		httpConn.setDoInput(true);
		
		
		OutputStream out = httpConn.getOutputStream();
		//Write the content of the request to the outputstream of the HTTP Connection.
		out.write(b);
		out.close();
		//Ready with sending the request.
		 
		//Read the response.
		InputStreamReader isr =
		new InputStreamReader(httpConn.getInputStream());
		BufferedReader in = new BufferedReader(isr);
		 
		//Write the SOAP message response to a String.
		while ((responseString = in.readLine()) != null) {
			//System.out.println("responseString-->"+responseString);
		outputString = outputString + responseString;
		}
		
		outputString = StringEscapeUtils.unescapeXml(outputString.toString());
		
		outputString = outputString.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
		
		//outputString = outputString.replace("<outputXML>", "<outputXML><![CDATA[").replace("</outputXML>", "]]></outputXML>");
		
		//logger.info(" OmniDocsUploader - cabinet connect xmloutput response :: "+ outputString); 
		
		folderIndex = getElementValueByXpath("FolderIndex",outputString);
		logger.info(" OmniDocsUploader  FolderIndex: " + folderIndex);
		
		return folderIndex;
	}
}
