/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.building;

/**
 * Class PowerRequired.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class PowerRequired implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _basePower.
     */
    private float _basePower;

    /**
     * keeps track of state for field: _basePower
     */
    private boolean _has_basePower;

    /**
     * Field _basePowerDownPower.
     */
    private float _basePowerDownPower;

    /**
     * keeps track of state for field: _basePowerDownPower
     */
    private boolean _has_basePowerDownPower;


      //----------------/
     //- Constructors -/
    //----------------/

    public PowerRequired() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteBasePower(
    ) {
        this._has_basePower= false;
    }

    /**
     */
    public void deleteBasePowerDownPower(
    ) {
        this._has_basePowerDownPower= false;
    }

    /**
     * Returns the value of field 'basePower'.
     * 
     * @return the value of field 'BasePower'.
     */
    public float getBasePower(
    ) {
        return this._basePower;
    }

    /**
     * Returns the value of field 'basePowerDownPower'.
     * 
     * @return the value of field 'BasePowerDownPower'.
     */
    public float getBasePowerDownPower(
    ) {
        return this._basePowerDownPower;
    }

    /**
     * Method hasBasePower.
     * 
     * @return true if at least one BasePower has been added
     */
    public boolean hasBasePower(
    ) {
        return this._has_basePower;
    }

    /**
     * Method hasBasePowerDownPower.
     * 
     * @return true if at least one BasePowerDownPower has been adde
     */
    public boolean hasBasePowerDownPower(
    ) {
        return this._has_basePowerDownPower;
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
     * Sets the value of field 'basePower'.
     * 
     * @param basePower the value of field 'basePower'.
     */
    public void setBasePower(
            final float basePower) {
        this._basePower = basePower;
        this._has_basePower = true;
    }

    /**
     * Sets the value of field 'basePowerDownPower'.
     * 
     * @param basePowerDownPower the value of field
     * 'basePowerDownPower'.
     */
    public void setBasePowerDownPower(
            final float basePowerDownPower) {
        this._basePowerDownPower = basePowerDownPower;
        this._has_basePowerDownPower = true;
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
     * org.mars_sim.msp.config.model.building.PowerRequired
     */
    public static org.mars_sim.msp.config.model.building.PowerRequired unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.building.PowerRequired) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.building.PowerRequired.class, reader);
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
