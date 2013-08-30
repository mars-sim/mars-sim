/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
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

    /**
     * Field _parkingLocationList.
     */
    private java.util.List<org.mars_sim.msp.config.model.building.ParkingLocation> _parkingLocationList;


      //----------------/
     //- Constructors -/
    //----------------/

    public GroundVehicleMaintenance() {
        super();
        this._parkingLocationList = new java.util.ArrayList<org.mars_sim.msp.config.model.building.ParkingLocation>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vParkingLocation
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addParkingLocation(
            final org.mars_sim.msp.config.model.building.ParkingLocation vParkingLocation)
    throws java.lang.IndexOutOfBoundsException {
        this._parkingLocationList.add(vParkingLocation);
    }

    /**
     * 
     * 
     * @param index
     * @param vParkingLocation
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addParkingLocation(
            final int index,
            final org.mars_sim.msp.config.model.building.ParkingLocation vParkingLocation)
    throws java.lang.IndexOutOfBoundsException {
        this._parkingLocationList.add(index, vParkingLocation);
    }

    /**
     */
    public void deleteVehicleCapacity(
    ) {
        this._has_vehicleCapacity= false;
    }

    /**
     * Method enumerateParkingLocation.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.building.ParkingLocation> enumerateParkingLocation(
    ) {
        return java.util.Collections.enumeration(this._parkingLocationList);
    }

    /**
     * Method getParkingLocation.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.building.ParkingLocation at
     * the given index
     */
    public org.mars_sim.msp.config.model.building.ParkingLocation getParkingLocation(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._parkingLocationList.size()) {
            throw new IndexOutOfBoundsException("getParkingLocation: Index value '" + index + "' not in range [0.." + (this._parkingLocationList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.building.ParkingLocation) _parkingLocationList.get(index);
    }

    /**
     * Method getParkingLocation.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.building.ParkingLocation[] getParkingLocation(
    ) {
        org.mars_sim.msp.config.model.building.ParkingLocation[] array = new org.mars_sim.msp.config.model.building.ParkingLocation[0];
        return (org.mars_sim.msp.config.model.building.ParkingLocation[]) this._parkingLocationList.toArray(array);
    }

    /**
     * Method getParkingLocationCount.
     * 
     * @return the size of this collection
     */
    public int getParkingLocationCount(
    ) {
        return this._parkingLocationList.size();
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
     * Method iterateParkingLocation.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.building.ParkingLocation> iterateParkingLocation(
    ) {
        return this._parkingLocationList.iterator();
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
     */
    public void removeAllParkingLocation(
    ) {
        this._parkingLocationList.clear();
    }

    /**
     * Method removeParkingLocation.
     * 
     * @param vParkingLocation
     * @return true if the object was removed from the collection.
     */
    public boolean removeParkingLocation(
            final org.mars_sim.msp.config.model.building.ParkingLocation vParkingLocation) {
        boolean removed = _parkingLocationList.remove(vParkingLocation);
        return removed;
    }

    /**
     * Method removeParkingLocationAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.building.ParkingLocation removeParkingLocationAt(
            final int index) {
        java.lang.Object obj = this._parkingLocationList.remove(index);
        return (org.mars_sim.msp.config.model.building.ParkingLocation) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vParkingLocation
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setParkingLocation(
            final int index,
            final org.mars_sim.msp.config.model.building.ParkingLocation vParkingLocation)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._parkingLocationList.size()) {
            throw new IndexOutOfBoundsException("setParkingLocation: Index value '" + index + "' not in range [0.." + (this._parkingLocationList.size() - 1) + "]");
        }

        this._parkingLocationList.set(index, vParkingLocation);
    }

    /**
     * 
     * 
     * @param vParkingLocationArray
     */
    public void setParkingLocation(
            final org.mars_sim.msp.config.model.building.ParkingLocation[] vParkingLocationArray) {
        //-- copy array
        _parkingLocationList.clear();

        for (int i = 0; i < vParkingLocationArray.length; i++) {
                this._parkingLocationList.add(vParkingLocationArray[i]);
        }
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
