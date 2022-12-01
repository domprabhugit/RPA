//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.09.08 at 04:51:38 PM IST 
//


package com.rpa.wsdl;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.rpa.wsdl package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _NGOAddDocDataDefCriterionBDO_QNAME = new QName("http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", "NGOAddDocDataDefCriterionBDO");
    private final static QName _NGOAddDocKeywordsCriterionBDO_QNAME = new QName("http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", "NGOAddDocKeywordsCriterionBDO");
    private final static QName _NGOAddDocDataDefCriteriaDataBDO_QNAME = new QName("http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", "NGOAddDocDataDefCriteriaDataBDO");
    private final static QName _NGOGetDocListDocDataBDO_QNAME = new QName("http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", "NGOGetDocListDocDataBDO");
    private final static QName _NGODataDefinitionBDO_QNAME = new QName("http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", "NGODataDefinitionBDO");
    private final static QName _NGODataDefFieldBDO_QNAME = new QName("http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", "NGODataDefFieldBDO");
    private final static QName _NGOAddDocumentBDODocument_QNAME = new QName("", "document");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.rpa.wsdl
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link NGOExecuteAPIBDO }
     * 
     */
    public NGOExecuteAPIBDO createNGOExecuteAPIBDO() {
        return new NGOExecuteAPIBDO();
    }

    /**
     * Create an instance of {@link NGOExecuteAPIResponseBDO }
     * 
     */
    public NGOExecuteAPIResponseBDO createNGOExecuteAPIResponseBDO() {
        return new NGOExecuteAPIResponseBDO();
    }

    /**
     * Create an instance of {@link NGOAddDocumentBDO }
     * 
     */
    public NGOAddDocumentBDO createNGOAddDocumentBDO() {
        return new NGOAddDocumentBDO();
    }

    /**
     * Create an instance of {@link NGOAddDocDataDefCriterionBDO }
     * 
     */
    public NGOAddDocDataDefCriterionBDO createNGOAddDocDataDefCriterionBDO() {
        return new NGOAddDocDataDefCriterionBDO();
    }

    /**
     * Create an instance of {@link NGOAddDocKeywordsCriterionBDO }
     * 
     */
    public NGOAddDocKeywordsCriterionBDO createNGOAddDocKeywordsCriterionBDO() {
        return new NGOAddDocKeywordsCriterionBDO();
    }

    /**
     * Create an instance of {@link NGOAddDocDataDefCriteriaDataBDO }
     * 
     */
    public NGOAddDocDataDefCriteriaDataBDO createNGOAddDocDataDefCriteriaDataBDO() {
        return new NGOAddDocDataDefCriteriaDataBDO();
    }

    /**
     * Create an instance of {@link NGOAddDocumentResponseBDO }
     * 
     */
    public NGOAddDocumentResponseBDO createNGOAddDocumentResponseBDO() {
        return new NGOAddDocumentResponseBDO();
    }

    /**
     * Create an instance of {@link NGOGetDocListDocDataBDO }
     * 
     */
    public NGOGetDocListDocDataBDO createNGOGetDocListDocDataBDO() {
        return new NGOGetDocListDocDataBDO();
    }

    /**
     * Create an instance of {@link NGODataDefinitionBDO }
     * 
     */
    public NGODataDefinitionBDO createNGODataDefinitionBDO() {
        return new NGODataDefinitionBDO();
    }

    /**
     * Create an instance of {@link NGODataDefFieldBDO }
     * 
     */
    public NGODataDefFieldBDO createNGODataDefFieldBDO() {
        return new NGODataDefFieldBDO();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NGOAddDocDataDefCriterionBDO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", name = "NGOAddDocDataDefCriterionBDO")
    public JAXBElement<NGOAddDocDataDefCriterionBDO> createNGOAddDocDataDefCriterionBDO(NGOAddDocDataDefCriterionBDO value) {
        return new JAXBElement<NGOAddDocDataDefCriterionBDO>(_NGOAddDocDataDefCriterionBDO_QNAME, NGOAddDocDataDefCriterionBDO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NGOAddDocKeywordsCriterionBDO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", name = "NGOAddDocKeywordsCriterionBDO")
    public JAXBElement<NGOAddDocKeywordsCriterionBDO> createNGOAddDocKeywordsCriterionBDO(NGOAddDocKeywordsCriterionBDO value) {
        return new JAXBElement<NGOAddDocKeywordsCriterionBDO>(_NGOAddDocKeywordsCriterionBDO_QNAME, NGOAddDocKeywordsCriterionBDO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NGOAddDocDataDefCriteriaDataBDO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", name = "NGOAddDocDataDefCriteriaDataBDO")
    public JAXBElement<NGOAddDocDataDefCriteriaDataBDO> createNGOAddDocDataDefCriteriaDataBDO(NGOAddDocDataDefCriteriaDataBDO value) {
        return new JAXBElement<NGOAddDocDataDefCriteriaDataBDO>(_NGOAddDocDataDefCriteriaDataBDO_QNAME, NGOAddDocDataDefCriteriaDataBDO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NGOGetDocListDocDataBDO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", name = "NGOGetDocListDocDataBDO")
    public JAXBElement<NGOGetDocListDocDataBDO> createNGOGetDocListDocDataBDO(NGOGetDocListDocDataBDO value) {
        return new JAXBElement<NGOGetDocListDocDataBDO>(_NGOGetDocListDocDataBDO_QNAME, NGOGetDocListDocDataBDO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NGODataDefinitionBDO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", name = "NGODataDefinitionBDO")
    public JAXBElement<NGODataDefinitionBDO> createNGODataDefinitionBDO(NGODataDefinitionBDO value) {
        return new JAXBElement<NGODataDefinitionBDO>(_NGODataDefinitionBDO_QNAME, NGODataDefinitionBDO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NGODataDefFieldBDO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", name = "NGODataDefFieldBDO")
    public JAXBElement<NGODataDefFieldBDO> createNGODataDefFieldBDO(NGODataDefFieldBDO value) {
        return new JAXBElement<NGODataDefFieldBDO>(_NGODataDefFieldBDO_QNAME, NGODataDefFieldBDO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "document", scope = NGOAddDocumentBDO.class)
    public JAXBElement<String> createNGOAddDocumentBDODocument(String value) {
        return new JAXBElement<String>(_NGOAddDocumentBDODocument_QNAME, String.class, NGOAddDocumentBDO.class, value);
    }

}
