//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.09.08 at 04:51:38 PM IST 
//


package com.rpa.wsdl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="accessedDateTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="annotationFlag" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="author" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="checkOutBy" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="checkOutStatus" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="createdByApp" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="createdByAppName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="createdDateTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="docOrderNo" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="docStatus" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="documentIndex" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="documentLock" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="documentName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="documentSize" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="documentType" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="documentVersionNo" type="{http://www.w3.org/2001/XMLSchema}float"/&gt;
 *         &lt;element name="enableLog" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="expiryDateTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="filedByUser" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="filedDateTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="finalizedBy" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="finalizedDateTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="finalizedFlag" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="ftsDocumentIndex" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="ftsFlag" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="isIndex" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="linkDocFlag" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="location" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="lockByUser" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="loginUserRights" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="noOfPages" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="odmaDocumentIndex" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="originalFolderIndex" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="originalFolderLocation" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="owner" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="ownerIndex" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="parentFolderIndex" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="pullPrintFlag" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="referenceFlag" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="revisedDateTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="textISIndex" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="thumbNailFlag" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="useFulInfo" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="versionFlag" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element ref="{http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/}NGODataDefinitionBDO" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "accessedDateTime",
    "annotationFlag",
    "author",
    "checkOutBy",
    "checkOutStatus",
    "comment",
    "createdByApp",
    "createdByAppName",
    "createdDateTime",
    "docOrderNo",
    "docStatus",
    "documentIndex",
    "documentLock",
    "documentName",
    "documentSize",
    "documentType",
    "documentVersionNo",
    "enableLog",
    "expiryDateTime",
    "filedByUser",
    "filedDateTime",
    "finalizedBy",
    "finalizedDateTime",
    "finalizedFlag",
    "ftsDocumentIndex",
    "ftsFlag",
    "isIndex",
    "linkDocFlag",
    "location",
    "lockByUser",
    "loginUserRights",
    "noOfPages",
    "odmaDocumentIndex",
    "originalFolderIndex",
    "originalFolderLocation",
    "owner",
    "ownerIndex",
    "parentFolderIndex",
    "pullPrintFlag",
    "referenceFlag",
    "revisedDateTime",
    "textISIndex",
    "thumbNailFlag",
    "useFulInfo",
    "versionFlag",
    "ngoDataDefinitionBDO"
})
public class NGOGetDocListDocDataBDO {

    @XmlElement(required = true, nillable = true)
    protected String accessedDateTime;
    @XmlElement(required = true, nillable = true)
    protected String annotationFlag;
    @XmlElement(required = true, nillable = true)
    protected String author;
    @XmlElement(required = true, nillable = true)
    protected String checkOutBy;
    @XmlElement(required = true, nillable = true)
    protected String checkOutStatus;
    @XmlElement(required = true, nillable = true)
    protected String comment;
    @XmlElement(required = true, type = Integer.class, nillable = true)
    protected Integer createdByApp;
    @XmlElement(required = true, nillable = true)
    protected String createdByAppName;
    @XmlElement(required = true, nillable = true)
    protected String createdDateTime;
    @XmlElement(required = true, type = Integer.class, nillable = true)
    protected Integer docOrderNo;
    @XmlElement(required = true, nillable = true)
    protected String docStatus;
    protected int documentIndex;
    @XmlElement(required = true, nillable = true)
    protected String documentLock;
    @XmlElement(required = true, nillable = true)
    protected String documentName;
    protected int documentSize;
    @XmlElement(required = true, nillable = true)
    protected String documentType;
    @XmlElement(required = true, type = Float.class, nillable = true)
    protected Float documentVersionNo;
    @XmlElement(required = true, nillable = true)
    protected String enableLog;
    @XmlElement(required = true, nillable = true)
    protected String expiryDateTime;
    @XmlElement(required = true, nillable = true)
    protected String filedByUser;
    @XmlElement(required = true, nillable = true)
    protected String filedDateTime;
    @XmlElement(required = true, nillable = true)
    protected String finalizedBy;
    @XmlElement(required = true, nillable = true)
    protected String finalizedDateTime;
    @XmlElement(required = true, nillable = true)
    protected String finalizedFlag;
    @XmlElement(required = true, type = Integer.class, nillable = true)
    protected Integer ftsDocumentIndex;
    @XmlElement(required = true, nillable = true)
    protected String ftsFlag;
    @XmlElement(required = true, nillable = true)
    protected String isIndex;
    @XmlElement(required = true, nillable = true)
    protected String linkDocFlag;
    @XmlElement(required = true, nillable = true)
    protected String location;
    @XmlElement(required = true, nillable = true)
    protected String lockByUser;
    @XmlElement(required = true, nillable = true)
    protected String loginUserRights;
    @XmlElement(required = true, type = Integer.class, nillable = true)
    protected Integer noOfPages;
    @XmlElement(required = true, nillable = true)
    protected String odmaDocumentIndex;
    @XmlElement(required = true, nillable = true)
    protected String originalFolderIndex;
    @XmlElement(required = true, nillable = true)
    protected String originalFolderLocation;
    @XmlElement(required = true, nillable = true)
    protected String owner;
    @XmlElement(required = true, type = Integer.class, nillable = true)
    protected Integer ownerIndex;
    @XmlElement(required = true, type = Integer.class, nillable = true)
    protected Integer parentFolderIndex;
    @XmlElement(required = true, nillable = true)
    protected String pullPrintFlag;
    @XmlElement(required = true, nillable = true)
    protected String referenceFlag;
    @XmlElement(required = true, nillable = true)
    protected String revisedDateTime;
    @XmlElement(required = true, type = Integer.class, nillable = true)
    protected Integer textISIndex;
    @XmlElement(required = true, nillable = true)
    protected String thumbNailFlag;
    @XmlElement(required = true, nillable = true)
    protected String useFulInfo;
    @XmlElement(required = true, nillable = true)
    protected String versionFlag;
    @XmlElement(name = "NGODataDefinitionBDO", namespace = "http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", nillable = true)
    protected NGODataDefinitionBDO ngoDataDefinitionBDO;

    /**
     * Gets the value of the accessedDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccessedDateTime() {
        return accessedDateTime;
    }

    /**
     * Sets the value of the accessedDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccessedDateTime(String value) {
        this.accessedDateTime = value;
    }

    /**
     * Gets the value of the annotationFlag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnnotationFlag() {
        return annotationFlag;
    }

    /**
     * Sets the value of the annotationFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnnotationFlag(String value) {
        this.annotationFlag = value;
    }

    /**
     * Gets the value of the author property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthor(String value) {
        this.author = value;
    }

    /**
     * Gets the value of the checkOutBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCheckOutBy() {
        return checkOutBy;
    }

    /**
     * Sets the value of the checkOutBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCheckOutBy(String value) {
        this.checkOutBy = value;
    }

    /**
     * Gets the value of the checkOutStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCheckOutStatus() {
        return checkOutStatus;
    }

    /**
     * Sets the value of the checkOutStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCheckOutStatus(String value) {
        this.checkOutStatus = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the createdByApp property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCreatedByApp() {
        return createdByApp;
    }

    /**
     * Sets the value of the createdByApp property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCreatedByApp(Integer value) {
        this.createdByApp = value;
    }

    /**
     * Gets the value of the createdByAppName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreatedByAppName() {
        return createdByAppName;
    }

    /**
     * Sets the value of the createdByAppName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreatedByAppName(String value) {
        this.createdByAppName = value;
    }

    /**
     * Gets the value of the createdDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreatedDateTime() {
        return createdDateTime;
    }

    /**
     * Sets the value of the createdDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreatedDateTime(String value) {
        this.createdDateTime = value;
    }

    /**
     * Gets the value of the docOrderNo property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDocOrderNo() {
        return docOrderNo;
    }

    /**
     * Sets the value of the docOrderNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDocOrderNo(Integer value) {
        this.docOrderNo = value;
    }

    /**
     * Gets the value of the docStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocStatus() {
        return docStatus;
    }

    /**
     * Sets the value of the docStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocStatus(String value) {
        this.docStatus = value;
    }

    /**
     * Gets the value of the documentIndex property.
     * 
     */
    public int getDocumentIndex() {
        return documentIndex;
    }

    /**
     * Sets the value of the documentIndex property.
     * 
     */
    public void setDocumentIndex(int value) {
        this.documentIndex = value;
    }

    /**
     * Gets the value of the documentLock property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentLock() {
        return documentLock;
    }

    /**
     * Sets the value of the documentLock property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentLock(String value) {
        this.documentLock = value;
    }

    /**
     * Gets the value of the documentName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentName() {
        return documentName;
    }

    /**
     * Sets the value of the documentName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentName(String value) {
        this.documentName = value;
    }

    /**
     * Gets the value of the documentSize property.
     * 
     */
    public int getDocumentSize() {
        return documentSize;
    }

    /**
     * Sets the value of the documentSize property.
     * 
     */
    public void setDocumentSize(int value) {
        this.documentSize = value;
    }

    /**
     * Gets the value of the documentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentType() {
        return documentType;
    }

    /**
     * Sets the value of the documentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentType(String value) {
        this.documentType = value;
    }

    /**
     * Gets the value of the documentVersionNo property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getDocumentVersionNo() {
        return documentVersionNo;
    }

    /**
     * Sets the value of the documentVersionNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setDocumentVersionNo(Float value) {
        this.documentVersionNo = value;
    }

    /**
     * Gets the value of the enableLog property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnableLog() {
        return enableLog;
    }

    /**
     * Sets the value of the enableLog property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnableLog(String value) {
        this.enableLog = value;
    }

    /**
     * Gets the value of the expiryDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExpiryDateTime() {
        return expiryDateTime;
    }

    /**
     * Sets the value of the expiryDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExpiryDateTime(String value) {
        this.expiryDateTime = value;
    }

    /**
     * Gets the value of the filedByUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFiledByUser() {
        return filedByUser;
    }

    /**
     * Sets the value of the filedByUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFiledByUser(String value) {
        this.filedByUser = value;
    }

    /**
     * Gets the value of the filedDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFiledDateTime() {
        return filedDateTime;
    }

    /**
     * Sets the value of the filedDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFiledDateTime(String value) {
        this.filedDateTime = value;
    }

    /**
     * Gets the value of the finalizedBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinalizedBy() {
        return finalizedBy;
    }

    /**
     * Sets the value of the finalizedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinalizedBy(String value) {
        this.finalizedBy = value;
    }

    /**
     * Gets the value of the finalizedDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinalizedDateTime() {
        return finalizedDateTime;
    }

    /**
     * Sets the value of the finalizedDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinalizedDateTime(String value) {
        this.finalizedDateTime = value;
    }

    /**
     * Gets the value of the finalizedFlag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinalizedFlag() {
        return finalizedFlag;
    }

    /**
     * Sets the value of the finalizedFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinalizedFlag(String value) {
        this.finalizedFlag = value;
    }

    /**
     * Gets the value of the ftsDocumentIndex property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFtsDocumentIndex() {
        return ftsDocumentIndex;
    }

    /**
     * Sets the value of the ftsDocumentIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFtsDocumentIndex(Integer value) {
        this.ftsDocumentIndex = value;
    }

    /**
     * Gets the value of the ftsFlag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFtsFlag() {
        return ftsFlag;
    }

    /**
     * Sets the value of the ftsFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFtsFlag(String value) {
        this.ftsFlag = value;
    }

    /**
     * Gets the value of the isIndex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsIndex() {
        return isIndex;
    }

    /**
     * Sets the value of the isIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsIndex(String value) {
        this.isIndex = value;
    }

    /**
     * Gets the value of the linkDocFlag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkDocFlag() {
        return linkDocFlag;
    }

    /**
     * Sets the value of the linkDocFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkDocFlag(String value) {
        this.linkDocFlag = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocation(String value) {
        this.location = value;
    }

    /**
     * Gets the value of the lockByUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLockByUser() {
        return lockByUser;
    }

    /**
     * Sets the value of the lockByUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLockByUser(String value) {
        this.lockByUser = value;
    }

    /**
     * Gets the value of the loginUserRights property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLoginUserRights() {
        return loginUserRights;
    }

    /**
     * Sets the value of the loginUserRights property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLoginUserRights(String value) {
        this.loginUserRights = value;
    }

    /**
     * Gets the value of the noOfPages property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNoOfPages() {
        return noOfPages;
    }

    /**
     * Sets the value of the noOfPages property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNoOfPages(Integer value) {
        this.noOfPages = value;
    }

    /**
     * Gets the value of the odmaDocumentIndex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOdmaDocumentIndex() {
        return odmaDocumentIndex;
    }

    /**
     * Sets the value of the odmaDocumentIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOdmaDocumentIndex(String value) {
        this.odmaDocumentIndex = value;
    }

    /**
     * Gets the value of the originalFolderIndex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginalFolderIndex() {
        return originalFolderIndex;
    }

    /**
     * Sets the value of the originalFolderIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginalFolderIndex(String value) {
        this.originalFolderIndex = value;
    }

    /**
     * Gets the value of the originalFolderLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginalFolderLocation() {
        return originalFolderLocation;
    }

    /**
     * Sets the value of the originalFolderLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginalFolderLocation(String value) {
        this.originalFolderLocation = value;
    }

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwner(String value) {
        this.owner = value;
    }

    /**
     * Gets the value of the ownerIndex property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getOwnerIndex() {
        return ownerIndex;
    }

    /**
     * Sets the value of the ownerIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setOwnerIndex(Integer value) {
        this.ownerIndex = value;
    }

    /**
     * Gets the value of the parentFolderIndex property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getParentFolderIndex() {
        return parentFolderIndex;
    }

    /**
     * Sets the value of the parentFolderIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setParentFolderIndex(Integer value) {
        this.parentFolderIndex = value;
    }

    /**
     * Gets the value of the pullPrintFlag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPullPrintFlag() {
        return pullPrintFlag;
    }

    /**
     * Sets the value of the pullPrintFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPullPrintFlag(String value) {
        this.pullPrintFlag = value;
    }

    /**
     * Gets the value of the referenceFlag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReferenceFlag() {
        return referenceFlag;
    }

    /**
     * Sets the value of the referenceFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReferenceFlag(String value) {
        this.referenceFlag = value;
    }

    /**
     * Gets the value of the revisedDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRevisedDateTime() {
        return revisedDateTime;
    }

    /**
     * Sets the value of the revisedDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRevisedDateTime(String value) {
        this.revisedDateTime = value;
    }

    /**
     * Gets the value of the textISIndex property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTextISIndex() {
        return textISIndex;
    }

    /**
     * Sets the value of the textISIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTextISIndex(Integer value) {
        this.textISIndex = value;
    }

    /**
     * Gets the value of the thumbNailFlag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThumbNailFlag() {
        return thumbNailFlag;
    }

    /**
     * Sets the value of the thumbNailFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThumbNailFlag(String value) {
        this.thumbNailFlag = value;
    }

    /**
     * Gets the value of the useFulInfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUseFulInfo() {
        return useFulInfo;
    }

    /**
     * Sets the value of the useFulInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUseFulInfo(String value) {
        this.useFulInfo = value;
    }

    /**
     * Gets the value of the versionFlag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersionFlag() {
        return versionFlag;
    }

    /**
     * Sets the value of the versionFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersionFlag(String value) {
        this.versionFlag = value;
    }

    /**
     * Gets the value of the ngoDataDefinitionBDO property.
     * 
     * @return
     *     possible object is
     *     {@link NGODataDefinitionBDO }
     *     
     */
    public NGODataDefinitionBDO getNGODataDefinitionBDO() {
        return ngoDataDefinitionBDO;
    }

    /**
     * Sets the value of the ngoDataDefinitionBDO property.
     * 
     * @param value
     *     allowed object is
     *     {@link NGODataDefinitionBDO }
     *     
     */
    public void setNGODataDefinitionBDO(NGODataDefinitionBDO value) {
        this.ngoDataDefinitionBDO = value;
    }

}