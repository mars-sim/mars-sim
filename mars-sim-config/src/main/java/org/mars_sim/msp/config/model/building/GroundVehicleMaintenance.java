/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.building;

/**
 * Class GroundVehicleMaintenance.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class GroundVehicleMaintenance implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _vehicleCapacity.
     */
    private long _vehicleCapacity;

    /**
     * keeps track of state for field: _vehicleCapacity
     */
    private boolean _has_vehicleCapacity;


      //----------------/
     //- Constructors -/
    //----------------/

    public GroundVehicleMaintenance() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteVehicleCapacity(
    ) {
        this._has_vehicleCapacity= false;
    }

    /**
     * Returns the value of field 'vehicleCapacity'.
     * 
     * @return the value of field 'VehicleCapacity'.
     */
    public long getVehicleCapacity(
    ) {
        return this._vehicleCapacity;
    }

    /**
     * Method hasVehicleCapacity.
     * 
     * @return true if at least one VehicleCapacity has been added
     */
    public boolean hasVehicleCapacity(
    ) {
        return this._has_vehicleCapacity;
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
     * Sets the value of field 'vehicleCapacity'.
     * 
     * @param vehicleCapacity the value of field 'vehicleCapacity'.
     */
    public void setVehicleCapacity(
            final long vehicleCapacity) {
        this._vehicleCapacity = vehicleCapacity;
        this._has_vehicleCapacity = true;
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
     * org.mars_sim.msp.config.model.building.GroundVehicleMaintenance
     */
    public static org.mars_sim.msp.config.model.building.GroundVehicleMaintenance unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.building.GroundVehicleMaintenance) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.building.GroundVehicleMaintenance.class, reader);
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
