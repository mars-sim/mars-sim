/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.building;

/**
 * Class AstronomicalObservation.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class AstronomicalObservation implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _techLevel.
     */
    private long _techLevel;

    /**
     * keeps track of state for field: _techLevel
     */
    private boolean _has_techLevel;

    /**
     * Field _capacity.
     */
    private long _capacity;

    /**
     * keeps track of state for field: _capacity
     */
    private boolean _has_capacity;

    /**
     * Field _powerRequired.
     */
    private float _powerRequired;

    /**
     * keeps track of state for field: _powerRequired
     */
    private boolean _has_powerRequired;


      //----------------/
     //- Constructors -/
    //----------------/

    public AstronomicalObservation() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteCapacity(
    ) {
        this._has_capacity= false;
    }

    /**
     */
    public void deletePowerRequired(
    ) {
        this._has_powerRequired= false;
    }

    /**
     */
    public void deleteTechLevel(
    ) {
        this._has_techLevel= false;
    }

    /**
     * Returns the value of field 'capacity'.
     * 
     * @return the value of field 'Capacity'.
     */
    public long getCapacity(
    ) {
        return this._capacity;
    }

    /**
     * Returns the value of field 'powerRequired'.
     * 
     * @return the value of field 'PowerRequired'.
     */
    public float getPowerRequired(
    ) {
        return this._powerRequired;
    }

    /**
     * Returns the value of field 'techLevel'.
     * 
     * @return the value of field 'TechLevel'.
     */
    public long getTechLevel(
    ) {
        return this._techLevel;
    }

    /**
     * Method hasCapacity.
     * 
     * @return true if at least one Capacity has been added
     */
    public boolean hasCapacity(
    ) {
        return this._has_capacity;
    }

    /**
     * Method hasPowerRequired.
     * 
     * @return true if at least one PowerRequired has been added
     */
    public boolean hasPowerRequired(
    ) {
        return this._has_powerRequired;
    }

    /**
     * Method hasTechLevel.
     * 
     * @return true if at least one TechLevel has been added
     */
    public boolean hasTechLevel(
    ) {
        return this._has_techLevel;
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
     * Sets the value of field 'capacity'.
     * 
     * @param capacity the value of field 'capacity'.
     */
    public void setCapacity(
            final long capacity) {
        this._capacity = capacity;
        this._has_capacity = true;
    }

    /**
     * Sets the value of field 'powerRequired'.
     * 
     * @param powerRequired the value of field 'powerRequired'.
     */
    public void setPowerRequired(
            final float powerRequired) {
        this._powerRequired = powerRequired;
        this._has_powerRequired = true;
    }

    /**
     * Sets the value of field 'techLevel'.
     * 
     * @param techLevel the value of field 'techLevel'.
     */
    public void setTechLevel(
            final long techLevel) {
        this._techLevel = techLevel;
        this._has_techLevel = true;
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
     * org.mars_sim.msp.config.model.building.AstronomicalObservatio
     */
    public static org.mars_sim.msp.config.model.building.AstronomicalObservation unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.building.AstronomicalObservation) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.building.AstronomicalObservation.class, reader);
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
