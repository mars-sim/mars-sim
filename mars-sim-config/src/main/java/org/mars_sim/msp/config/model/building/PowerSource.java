/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.building;

/**
 * Class PowerSource.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class PowerSource implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _type.
     */
    private java.lang.String _type;

    /**
     * Field _toggle.
     */
    private boolean _toggle;

    /**
     * keeps track of state for field: _toggle
     */
    private boolean _has_toggle;

    /**
     * Field _power.
     */
    private double _power;

    /**
     * keeps track of state for field: _power
     */
    private boolean _has_power;

    /**
     * Field _fuelType.
     */
    private java.lang.String _fuelType;

    /**
     * Field _consumptionRate.
     */
    private double _consumptionRate;

    /**
     * keeps track of state for field: _consumptionRate
     */
    private boolean _has_consumptionRate;


      //----------------/
     //- Constructors -/
    //----------------/

    public PowerSource() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteConsumptionRate(
    ) {
        this._has_consumptionRate= false;
    }

    /**
     */
    public void deletePower(
    ) {
        this._has_power= false;
    }

    /**
     */
    public void deleteToggle(
    ) {
        this._has_toggle= false;
    }

    /**
     * Returns the value of field 'consumptionRate'.
     * 
     * @return the value of field 'ConsumptionRate'.
     */
    public double getConsumptionRate(
    ) {
        return this._consumptionRate;
    }

    /**
     * Returns the value of field 'fuelType'.
     * 
     * @return the value of field 'FuelType'.
     */
    public java.lang.String getFuelType(
    ) {
        return this._fuelType;
    }

    /**
     * Returns the value of field 'power'.
     * 
     * @return the value of field 'Power'.
     */
    public double getPower(
    ) {
        return this._power;
    }

    /**
     * Returns the value of field 'toggle'.
     * 
     * @return the value of field 'Toggle'.
     */
    public boolean getToggle(
    ) {
        return this._toggle;
    }

    /**
     * Returns the value of field 'type'.
     * 
     * @return the value of field 'Type'.
     */
    public java.lang.String getType(
    ) {
        return this._type;
    }

    /**
     * Method hasConsumptionRate.
     * 
     * @return true if at least one ConsumptionRate has been added
     */
    public boolean hasConsumptionRate(
    ) {
        return this._has_consumptionRate;
    }

    /**
     * Method hasPower.
     * 
     * @return true if at least one Power has been added
     */
    public boolean hasPower(
    ) {
        return this._has_power;
    }

    /**
     * Method hasToggle.
     * 
     * @return true if at least one Toggle has been added
     */
    public boolean hasToggle(
    ) {
        return this._has_toggle;
    }

    /**
     * Returns the value of field 'toggle'.
     * 
     * @return the value of field 'Toggle'.
     */
    public boolean isToggle(
    ) {
        return this._toggle;
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
     * Sets the value of field 'consumptionRate'.
     * 
     * @param consumptionRate the value of field 'consumptionRate'.
     */
    public void setConsumptionRate(
            final double consumptionRate) {
        this._consumptionRate = consumptionRate;
        this._has_consumptionRate = true;
    }

    /**
     * Sets the value of field 'fuelType'.
     * 
     * @param fuelType the value of field 'fuelType'.
     */
    public void setFuelType(
            final java.lang.String fuelType) {
        this._fuelType = fuelType;
    }

    /**
     * Sets the value of field 'power'.
     * 
     * @param power the value of field 'power'.
     */
    public void setPower(
            final double power) {
        this._power = power;
        this._has_power = true;
    }

    /**
     * Sets the value of field 'toggle'.
     * 
     * @param toggle the value of field 'toggle'.
     */
    public void setToggle(
            final boolean toggle) {
        this._toggle = toggle;
        this._has_toggle = true;
    }

    /**
     * Sets the value of field 'type'.
     * 
     * @param type the value of field 'type'.
     */
    public void setType(
            final java.lang.String type) {
        this._type = type;
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
     * org.mars_sim.msp.config.model.building.PowerSource
     */
    public static org.mars_sim.msp.config.model.building.PowerSource unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.building.PowerSource) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.building.PowerSource.class, reader);
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
