//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.09.08 at 04:51:38 PM IST 
//


package com.rpa.wsdl;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
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
 *         &lt;element name="cabinetName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="documentPath" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="folderIndex" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="documentName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="userDBId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="volumeId" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="creationDateTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="versionFlag" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="accessType" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="documentType" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="createdByAppName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="noOfPages" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="DocumentSize" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="FTSDocumentIndex" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="textISIndex" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="ODMADocumentIndex" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="enableLog" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="author" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="FTSFlag" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="groupIndex" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="ownerIndex" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="nameLength" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="versionComment" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="duplicateName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="textAlsoFlag" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="imageData" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="transactionRequired" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="validateDocumentImage" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="ownerType" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="signFlag" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="thumbNailFlag" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="userName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="userPassword" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="document" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element ref="{http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/}NGOAddDocDataDefCriterionBDO" minOccurs="0"/&gt;
 *         &lt;element ref="{http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/}NGOAddDocKeywordsCriterionBDO" minOccurs="0"/&gt;
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
    "cabinetName",
    "documentPath",
    "folderIndex",
    "documentName",
    "userDBId",
    "volumeId",
    "creationDateTime",
    "versionFlag",
    "accessType",
    "documentType",
    "createdByAppName",
    "noOfPages",
    "documentSize",
    "ftsDocumentIndex",
    "textISIndex",
    "odmaDocumentIndex",
    "enableLog",
    "comment",
    "author",
    "ftsFlag",
    "groupIndex",
    "ownerIndex",
    "nameLength",
    "versionComment",
    "duplicateName",
    "textAlsoFlag",
    "imageData",
    "transactionRequired",
    "validateDocumentImage",
    "ownerType",
    "signFlag",
    "thumbNailFlag",
    "userName",
    "userPassword",
    "document",
    "ngoAddDocDataDefCriterionBDO",
    "ngoAddDocKeywordsCriterionBDO"
})
@XmlRootElement(name = "NGOAddDocumentBDO")
public class NGOAddDocumentBDO {

    @XmlElement(required = true, nillable = true)
    protected String cabinetName;
    @XmlElement(required = true, nillable = true)
    protected String documentPath;
    protected int folderIndex;
    @XmlElement(required = true)
    protected String documentName;
    @XmlElement(required = true, nillable = true)
    protected String userDBId;
    protected int volumeId;
    @XmlElement(required = true, nillable = true)
    protected String creationDateTime;
    @XmlElement(required = true, nillable = true)
    protected String versionFlag;
    @XmlElement(required = true, nillable = true)
    protected String accessType;
    @XmlElement(required = true, nillable = true)
    protected String documentType;
    @XmlElement(required = true, nillable = true)
    protected String createdByAppName;
    @XmlElement(required = true, nillable = true)
    protected String noOfPages;
    @XmlElement(name = "DocumentSize", required = true, nillable = true)
    protected String documentSize;
    @XmlElement(name = "FTSDocumentIndex", required = true, nillable = true)
    protected String ftsDocumentIndex;
    @XmlElement(required = true, nillable = true)
    protected String textISIndex;
    @XmlElement(name = "ODMADocumentIndex", required = true, nillable = true)
    protected String odmaDocumentIndex;
    @XmlElement(required = true, nillable = true)
    protected String enableLog;
    @XmlElement(required = true, nillable = true)
    protected String comment;
    @XmlElement(required = true, nillable = true)
    protected String author;
    @XmlElement(name = "FTSFlag", required = true, nillable = true)
    protected String ftsFlag;
    @XmlElement(required = true, nillable = true)
    protected String groupIndex;
    @XmlElement(required = true, nillable = true)
    protected String ownerIndex;
    @XmlElement(required = true, nillable = true)
    protected String nameLength;
    @XmlElement(required = true, nillable = true)
    protected String versionComment;
    @XmlElement(required = true, nillable = true)
    protected String duplicateName;
    @XmlElement(required = true, nillable = true)
    protected String textAlsoFlag;
    @XmlElement(required = true, nillable = true)
    protected String imageData;
    @XmlElement(required = true, nillable = true)
    protected String transactionRequired;
    @XmlElement(required = true, nillable = true)
    protected String validateDocumentImage;
    @XmlElement(required = true, nillable = true)
    protected String ownerType;
    @XmlElement(required = true, nillable = true)
    protected String signFlag;
    @XmlElement(required = true, nillable = true)
    protected String thumbNailFlag;
    @XmlElement(required = true, nillable = true)
    protected String userName;
    @XmlElement(required = true, nillable = true)
    protected String userPassword;
    @XmlElementRef(name = "document", type = JAXBElement.class, required = false)
    protected JAXBElement<String> document;
    @XmlElement(name = "NGOAddDocDataDefCriterionBDO", namespace = "http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", nillable = true)
    protected NGOAddDocDataDefCriterionBDO ngoAddDocDataDefCriterionBDO;
    @XmlElement(name = "NGOAddDocKeywordsCriterionBDO", namespace = "http://bdo.ws.jts.omni.newgen.com/NGOAddDocumentService/", nillable = true)
    protected NGOAddDocKeywordsCriterionBDO ngoAddDocKeywordsCriterionBDO;

    /**
     * Gets the value of the cabinetName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCabinetName() {
        return cabinetName;
    }

    /**
     * Sets the value of the cabinetName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCabinetName(String value) {
        this.cabinetName = value;
    }

    /**
     * Gets the value of the documentPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentPath() {
        return documentPath;
    }

    /**
     * Sets the value of the documentPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentPath(String value) {
        this.documentPath = value;
    }

    /**
     * Gets the value of the folderIndex property.
     * 
     */
    public int getFolderIndex() {
        return folderIndex;
    }

    /**
     * Sets the value of the folderIndex property.
     * 
     */
    public void setFolderIndex(int value) {
        this.folderIndex = value;
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
     * Gets the value of the userDBId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserDBId() {
        return userDBId;
    }

    /**
     * Sets the value of the userDBId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserDBId(String value) {
        this.userDBId = value;
    }

    /**
     * Gets the value of the volumeId property.
     * 
     */
    public int getVolumeId() {
        return volumeId;
    }

    /**
     * Sets the value of the volumeId property.
     * 
     */
    public void setVolumeId(int value) {
        this.volumeId = value;
    }

    /**
     * Gets the value of the creationDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreationDateTime() {
        return creationDateTime;
    }

    /**
     * Sets the value of the creationDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreationDateTime(String value) {
        this.creationDateTime = value;
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
     * Gets the value of the accessType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccessType() {
        return accessType;
    }

    /**
     * Sets the value of the accessType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccessType(String value) {
        this.accessType = value;
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
     * Gets the value of the noOfPages property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNoOfPages() {
        return noOfPages;
    }

    /**
     * Sets the value of the noOfPages property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNoOfPages(String value) {
        this.noOfPages = value;
    }

    /**
     * Gets the value of the documentSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentSize() {
        return documentSize;
    }

    /**
     * Sets the value of the documentSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentSize(String value) {
        this.documentSize = value;
    }

    /**
     * Gets the value of the ftsDocumentIndex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFTSDocumentIndex() {
        return ftsDocumentIndex;
    }

    /**
     * Sets the value of the ftsDocumentIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFTSDocumentIndex(String value) {
        this.ftsDocumentIndex = value;
    }

    /**
     * Gets the value of the textISIndex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextISIndex() {
        return textISIndex;
    }

    /**
     * Sets the value of the textISIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextISIndex(String value) {
        this.textISIndex = value;
    }

    /**
     * Gets the value of the odmaDocumentIndex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getODMADocumentIndex() {
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
    public void setODMADocumentIndex(String value) {
        this.odmaDocumentIndex = value;
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
     * Gets the value of the ftsFlag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFTSFlag() {
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
    public void setFTSFlag(String value) {
        this.ftsFlag = value;
    }

    /**
     * Gets the value of the groupIndex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupIndex() {
        return groupIndex;
    }

    /**
     * Sets the value of the groupIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupIndex(String value) {
        this.groupIndex = value;
    }

    /**
     * Gets the value of the ownerIndex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerIndex() {
        return ownerIndex;
    }

    /**
     * Sets the value of the ownerIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerIndex(String value) {
        this.ownerIndex = value;
    }

    /**
     * Gets the value of the nameLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNameLength() {
        return nameLength;
    }

    /**
     * Sets the value of the nameLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNameLength(String value) {
        this.nameLength = value;
    }

    /**
     * Gets the value of the versionComment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersionComment() {
        return versionComment;
    }

    /**
     * Sets the value of the versionComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersionComment(String value) {
        this.versionComment = value;
    }

    /**
     * Gets the value of the duplicateName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDuplicateName() {
        return duplicateName;
    }

    /**
     * Sets the value of the duplicateName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDuplicateName(String value) {
        this.duplicateName = value;
    }

    /**
     * Gets the value of the textAlsoFlag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextAlsoFlag() {
        return textAlsoFlag;
    }

    /**
     * Sets the value of the textAlsoFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextAlsoFlag(String value) {
        this.textAlsoFlag = value;
    }

    /**
     * Gets the value of the imageData property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImageData() {
        return imageData;
    }

    /**
     * Sets the value of the imageData property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImageData(String value) {
        this.imageData = value;
    }

    /**
     * Gets the value of the transactionRequired property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactionRequired() {
        return transactionRequired;
    }

    /**
     * Sets the value of the transactionRequired property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionRequired(String value) {
        this.transactionRequired = value;
    }

    /**
     * Gets the value of the validateDocumentImage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValidateDocumentImage() {
        return validateDocumentImage;
    }

    /**
     * Sets the value of the validateDocumentImage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidateDocumentImage(String value) {
        this.validateDocumentImage = value;
    }

    /**
     * Gets the value of the ownerType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerType() {
        return ownerType;
    }

    /**
     * Sets the value of the ownerType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerType(String value) {
        this.ownerType = value;
    }

    /**
     * Gets the value of the signFlag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSignFlag() {
        return signFlag;
    }

    /**
     * Sets the value of the signFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSignFlag(String value) {
        this.signFlag = value;
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
     * Gets the value of the userName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the value of the userName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserName(String value) {
        this.userName = value;
    }

    /**
     * Gets the value of the userPassword property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * Sets the value of the userPassword property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserPassword(String value) {
        this.userPassword = value;
    }

    /**
     * Gets the value of the document property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDocument() {
        return document;
    }

    /**
     * Sets the value of the document property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDocument(JAXBElement<String> value) {
        this.document = value;
    }

    /**
     * Gets the value of the ngoAddDocDataDefCriterionBDO property.
     * 
     * @return
     *     possible object is
     *     {@link NGOAddDocDataDefCriterionBDO }
     *     
     */
    public NGOAddDocDataDefCriterionBDO getNGOAddDocDataDefCriterionBDO() {
        return ngoAddDocDataDefCriterionBDO;
    }

    /**
     * Sets the value of the ngoAddDocDataDefCriterionBDO property.
     * 
     * @param value
     *     allowed object is
     *     {@link NGOAddDocDataDefCriterionBDO }
     *     
     */
    public void setNGOAddDocDataDefCriterionBDO(NGOAddDocDataDefCriterionBDO value) {
        this.ngoAddDocDataDefCriterionBDO = value;
    }

    /**
     * Gets the value of the ngoAddDocKeywordsCriterionBDO property.
     * 
     * @return
     *     possible object is
     *     {@link NGOAddDocKeywordsCriterionBDO }
     *     
     */
    public NGOAddDocKeywordsCriterionBDO getNGOAddDocKeywordsCriterionBDO() {
        return ngoAddDocKeywordsCriterionBDO;
    }

    /**
     * Sets the value of the ngoAddDocKeywordsCriterionBDO property.
     * 
     * @param value
     *     allowed object is
     *     {@link NGOAddDocKeywordsCriterionBDO }
     *     
     */
    public void setNGOAddDocKeywordsCriterionBDO(NGOAddDocKeywordsCriterionBDO value) {
        this.ngoAddDocKeywordsCriterionBDO = value;
    }

}