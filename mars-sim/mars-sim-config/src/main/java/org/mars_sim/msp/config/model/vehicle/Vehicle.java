/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.vehicle;

/**
 * Class Vehicle.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Vehicle implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _type.
     */
    private java.lang.String _type;

    /**
     * Field _fuelEfficiency.
     */
    private org.mars_sim.msp.config.model.vehicle.FuelEfficiency _fuelEfficiency;

    /**
     * Field _baseSpeed.
     */
    private org.mars_sim.msp.config.model.vehicle.BaseSpeed _baseSpeed;

    /**
     * Field _emptyMass.
     */
    private org.mars_sim.msp.config.model.vehicle.EmptyMass _emptyMass;

    /**
     * Field _crewSize.
     */
    private org.mars_sim.msp.config.model.vehicle.CrewSize _crewSize;

    /**
     * Field _cargo.
     */
    private org.mars_sim.msp.config.model.vehicle.Cargo _cargo;

    /**
     * Field _partAttachment.
     */
    private org.mars_sim.msp.config.model.vehicle.PartAttachment _partAttachment;

    /**
     * Field _sickbay.
     */
    private org.mars_sim.msp.config.model.vehicle.Sickbay _sickbay;

    /**
     * Field _lab.
     */
    private org.mars_sim.msp.config.model.vehicle.Lab _lab;


      //----------------/
     //- Constructors -/
    //----------------/

    public Vehicle() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'baseSpeed'.
     * 
     * @return the value of field 'BaseSpeed'.
     */
    public org.mars_sim.msp.config.model.vehicle.BaseSpeed getBaseSpeed(
    ) {
        return this._baseSpeed;
    }

    /**
     * Returns the value of field 'cargo'.
     * 
     * @return the value of field 'Cargo'.
     */
    public org.mars_sim.msp.config.model.vehicle.Cargo getCargo(
    ) {
        return this._cargo;
    }

    /**
     * Returns the value of field 'crewSize'.
     * 
     * @return the value of field 'CrewSize'.
     */
    public org.mars_sim.msp.config.model.vehicle.CrewSize getCrewSize(
    ) {
        return this._crewSize;
    }

    /**
     * Returns the value of field 'emptyMass'.
     * 
     * @return the value of field 'EmptyMass'.
     */
    public org.mars_sim.msp.config.model.vehicle.EmptyMass getEmptyMass(
    ) {
        return this._emptyMass;
    }

    /**
     * Returns the value of field 'fuelEfficiency'.
     * 
     * @return the value of field 'FuelEfficiency'.
     */
    public org.mars_sim.msp.config.model.vehicle.FuelEfficiency getFuelEfficiency(
    ) {
        return this._fuelEfficiency;
    }

    /**
     * Returns the value of field 'lab'.
     * 
     * @return the value of field 'Lab'.
     */
    public org.mars_sim.msp.config.model.vehicle.Lab getLab(
    ) {
        return this._lab;
    }

    /**
     * Returns the value of field 'partAttachment'.
     * 
     * @return the value of field 'PartAttachment'.
     */
    public org.mars_sim.msp.config.model.vehicle.PartAttachment getPartAttachment(
    ) {
        return this._partAttachment;
    }

    /**
     * Returns the value of field 'sickbay'.
     * 
     * @return the value of field 'Sickbay'.
     */
    public org.mars_sim.msp.config.model.vehicle.Sickbay getSickbay(
    ) {
        return this._sickbay;
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
     * Sets the value of field 'baseSpeed'.
     * 
     * @param baseSpeed the value of field 'baseSpeed'.
     */
    public void setBaseSpeed(
            final org.mars_sim.msp.config.model.vehicle.BaseSpeed baseSpeed) {
        this._baseSpeed = baseSpeed;
    }

    /**
     * Sets the value of field 'cargo'.
     * 
     * @param cargo the value of field 'cargo'.
     */
    public void setCargo(
            final org.mars_sim.msp.config.model.vehicle.Cargo cargo) {
        this._cargo = cargo;
    }

    /**
     * Sets the value of field 'crewSize'.
     * 
     * @param crewSize the value of field 'crewSize'.
     */
    public void setCrewSize(
            final org.mars_sim.msp.config.model.vehicle.CrewSize crewSize) {
        this._crewSize = crewSize;
    }

    /**
     * Sets the value of field 'emptyMass'.
     * 
     * @param emptyMass the value of field 'emptyMass'.
     */
    public void setEmptyMass(
            final org.mars_sim.msp.config.model.vehicle.EmptyMass emptyMass) {
        this._emptyMass = emptyMass;
    }

    /**
     * Sets the value of field 'fuelEfficiency'.
     * 
     * @param fuelEfficiency the value of field 'fuelEfficiency'.
     */
    public void setFuelEfficiency(
            final org.mars_sim.msp.config.model.vehicle.FuelEfficiency fuelEfficiency) {
        this._fuelEfficiency = fuelEfficiency;
    }

    /**
     * Sets the value of field 'lab'.
     * 
     * @param lab the value of field 'lab'.
     */
    public void setLab(
            final org.mars_sim.msp.config.model.vehicle.Lab lab) {
        this._lab = lab;
    }

    /**
     * Sets the value of field 'partAttachment'.
     * 
     * @param partAttachment the value of field 'partAttachment'.
     */
    public void setPartAttachment(
            final org.mars_sim.msp.config.model.vehicle.PartAttachment partAttachment) {
        this._partAttachment = partAttachment;
    }

    /**
     * Sets the value of field 'sickbay'.
     * 
     * @param sickbay the value of field 'sickbay'.
     */
    public void setSickbay(
            final org.mars_sim.msp.config.model.vehicle.Sickbay sickbay) {
        this._sickbay = sickbay;
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
     * org.mars_sim.msp.config.model.vehicle.Vehicle
     */
    public static org.mars_sim.msp.config.model.vehicle.Vehicle unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.vehicle.Vehicle) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.vehicle.Vehicle.class, reader);
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
