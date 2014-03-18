/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.landmark;

/**
 * Class Landmark.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Landmark implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _approvaldate.
     */
    private java.lang.String _approvaldate;

    /**
     * Field _diameter.
     */
    private java.lang.String _diameter;

    /**
     * Field _latitude.
     */
    private java.lang.String _latitude;

    /**
     * Field _longitude.
     */
    private java.lang.String _longitude;

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _origin.
     */
    private java.lang.String _origin;


      //----------------/
     //- Constructors -/
    //----------------/

    public Landmark() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'approvaldate'.
     * 
     * @return the value of field 'Approvaldate'.
     */
    public java.lang.String getApprovaldate(
    ) {
        return this._approvaldate;
    }

    /**
     * Returns the value of field 'diameter'.
     * 
     * @return the value of field 'Diameter'.
     */
    public java.lang.String getDiameter(
    ) {
        return this._diameter;
    }

    /**
     * Returns the value of field 'latitude'.
     * 
     * @return the value of field 'Latitude'.
     */
    public java.lang.String getLatitude(
    ) {
        return this._latitude;
    }

    /**
     * Returns the value of field 'longitude'.
     * 
     * @return the value of field 'Longitude'.
     */
    public java.lang.String getLongitude(
    ) {
        return this._longitude;
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public java.lang.String getName(
    ) {
        return this._name;
    }

    /**
     * Returns the value of field 'origin'.
     * 
     * @return the value of field 'Origin'.
     */
    public java.lang.String getOrigin(
    ) {
        return this._origin;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid(
    ) {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(
            final java.io.Writer out)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Marshaller.marshal(this, handler);
    }

    /**
     * Sets the value of field 'approvaldate'.
     * 
     * @param approvaldate the value of field 'approvaldate'.
     */
    public void setApprovaldate(
            final java.lang.String approvaldate) {
        this._approvaldate = approvaldate;
    }

    /**
     * Sets the value of field 'diameter'.
     * 
     * @param diameter the value of field 'diameter'.
     */
    public void setDiameter(
            final java.lang.String diameter) {
        this._diameter = diameter;
    }

    /**
     * Sets the value of field 'latitude'.
     * 
     * @param latitude the value of field 'latitude'.
     */
    public void setLatitude(
            final java.lang.String latitude) {
        this._latitude = latitude;
    }

    /**
     * Sets the value of field 'longitude'.
     * 
     * @param longitude the value of field 'longitude'.
     */
    public void setLongitude(
            final java.lang.String longitude) {
        this._longitude = longitude;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(
            final java.lang.String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'origin'.
     * 
     * @param origin the value of field 'origin'.
     */
    public void setOrigin(
            final java.lang.String origin) {
        this._origin = origin;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.mars_sim.msp.config.model.landmark.Landmark
     */
    public static org.mars_sim.msp.config.model.landmark.Landmark unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.landmark.Landmark) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.landmark.Landmark.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate(
    )
    throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
