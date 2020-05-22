
package de.unijena.bioinf.babelms.cef;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="ce" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="fv" use="required" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="is" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="p" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="scanType" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "MSDetails")
public class MSDetails {

    @XmlAttribute(name = "ce")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String ce;
    @XmlAttribute(name = "fv", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String fv;
    @XmlAttribute(name = "is", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String is;
    @XmlAttribute(name = "p", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String p;
    @XmlAttribute(name = "scanType", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String scanType;

    /**
     * Gets the value of the ce property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCe() {
        return ce;
    }

    /**
     * Sets the value of the ce property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCe(String value) {
        this.ce = value;
    }

    /**
     * Gets the value of the fv property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFv() {
        return fv;
    }

    /**
     * Sets the value of the fv property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFv(String value) {
        this.fv = value;
    }

    /**
     * Gets the value of the is property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIs() {
        return is;
    }

    /**
     * Sets the value of the is property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIs(String value) {
        this.is = value;
    }

    /**
     * Gets the value of the p property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getP() {
        return p;
    }

    /**
     * Sets the value of the p property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setP(String value) {
        this.p = value;
    }

    /**
     * Gets the value of the scanType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScanType() {
        return scanType;
    }

    /**
     * Sets the value of the scanType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScanType(String value) {
        this.scanType = value;
    }

}