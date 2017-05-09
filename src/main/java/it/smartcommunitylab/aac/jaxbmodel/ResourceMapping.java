//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.05.09 at 06:07:09 PM CEST 
//


package it.smartcommunitylab.aac.jaxbmodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Resource mapping defines the service-specific
 * 				resources. Characterized by the mapping id, name, description,
 * 				(parametric) resource uri, authority role type, whether the explicit
 * 				approval is required to access to this resource, and whether
 * 				the resource is visible to other client apps
 * 
 * <p>Java class for resourceMapping complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="resourceMapping">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="uri" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="authority" type="{http://aac.smartcommunitylab.it/jaxbmodel}authority" />
 *       &lt;attribute name="approvalRequired" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="accessibleByOthers" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resourceMapping", propOrder = {
    "description"
})
public class ResourceMapping {

    @XmlElement(required = true)
    protected String description;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "uri")
    protected String uri;
    @XmlAttribute(name = "authority")
    protected Authority authority;
    @XmlAttribute(name = "approvalRequired")
    protected Boolean approvalRequired;
    @XmlAttribute(name = "accessibleByOthers")
    protected Boolean accessibleByOthers;

    /**
     * Default no-arg constructor
     * 
     */
    public ResourceMapping() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public ResourceMapping(final String description, final String id, final String name, final String uri, final Authority authority, final Boolean approvalRequired, final Boolean accessibleByOthers) {
        this.description = description;
        this.id = id;
        this.name = name;
        this.uri = uri;
        this.authority = authority;
        this.approvalRequired = approvalRequired;
        this.accessibleByOthers = accessibleByOthers;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the uri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the value of the uri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUri(String value) {
        this.uri = value;
    }

    /**
     * Gets the value of the authority property.
     * 
     * @return
     *     possible object is
     *     {@link Authority }
     *     
     */
    public Authority getAuthority() {
        return authority;
    }

    /**
     * Sets the value of the authority property.
     * 
     * @param value
     *     allowed object is
     *     {@link Authority }
     *     
     */
    public void setAuthority(Authority value) {
        this.authority = value;
    }

    /**
     * Gets the value of the approvalRequired property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isApprovalRequired() {
        if (approvalRequired == null) {
            return false;
        } else {
            return approvalRequired;
        }
    }

    /**
     * Sets the value of the approvalRequired property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApprovalRequired(Boolean value) {
        this.approvalRequired = value;
    }

    /**
     * Gets the value of the accessibleByOthers property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAccessibleByOthers() {
        if (accessibleByOthers == null) {
            return true;
        } else {
            return accessibleByOthers;
        }
    }

    /**
     * Sets the value of the accessibleByOthers property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAccessibleByOthers(Boolean value) {
        this.accessibleByOthers = value;
    }

}
