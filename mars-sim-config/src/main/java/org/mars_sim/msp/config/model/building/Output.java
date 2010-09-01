/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.building;

/**
 * Class Output.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Output implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _resource.
     */
    private java.lang.String _resource;

    /**
     * Field _rate.
     */
    private double _rate;

    /**
     * keeps track of state for field: _rate
     */
    private boolean _has_rate;

    /**
     * Field _ambient.
     */
    private boolean _ambient;

    /**
     * keeps track of state for field: _ambient
     */
    private boolean _has_ambient;


      //----------------/
     //- Constructors -/
    //----------------/

    public Output() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteAmbient(
    ) {
        this._has_ambient= false;
    }

    /**
     */
    public void deleteRate(
    ) {
        this._has_rate= false;
    }

    /**
     * Returns the value of field 'ambient'.
     * 
     * @return the value of field 'Ambient'.
     */
    public boolean getAmbient(
    ) {
        return this._ambient;
    }

    /**
     * Returns the value of field 'rate'.
     * 
     * @return the value of field 'Rate'.
     */
    public double getRate(
    ) {
        return this._rate;
    }

    /**
     * Returns the value of field 'resource'.
     * 
     * @return the value of field 'Resource'.
     */
    public java.lang.String getResource(
    ) {
        return this._resource;
    }

    /**
     * Method hasAmbient.
     * 
     * @return true if at least one Ambient has been added
     */
    public boolean hasAmbient(
    ) {
        return this._has_ambient;
    }

    /**
     * Method hasRate.
     * 
     * @return true if at least one Rate has been added
     */
    public boolean hasRate(
    ) {
        return this._has_rate;
    }

    /**
     * Returns the value of field 'ambient'.
     * 
     * @return the value of field 'Ambient'.
     */
    public boolean isAmbient(
    ) {
        return this._ambient;
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
     * Sets the value of field 'ambient'.
     * 
     * @param ambient the value of field 'ambient'.
     */
    public void setAmbient(
            final boolean ambient) {
        this._ambient = ambient;
        this._has_ambient = true;
    }

    /**
     * Sets the value of field 'rate'.
     * 
     * @param rate the value of field 'rate'.
     */
    public void setRate(
            final double rate) {
        this._rate = rate;
        this._has_rate = true;
    }

    /**
     * Sets the value of field 'resource'.
     * 
     * @param resource the value of field 'resource'.
     */
    public void setResource(
            final java.lang.String resource) {
        this._resource = resource;
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
     * org.mars_sim.msp.config.model.building.Output
     */
    public static org.mars_sim.msp.config.model.building.Output unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.building.Output) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.building.Output.class, reader);
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
