/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.vehicle;

/**
 * Class VehicleConfiguration.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class VehicleConfiguration implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _vehicleList.
     */
    private java.util.List<org.mars_sim.msp.config.model.vehicle.Vehicle> _vehicleList;

    /**
     * Field _roverNameList.
     */
    private org.mars_sim.msp.config.model.vehicle.RoverNameList _roverNameList;


      //----------------/
     //- Constructors -/
    //----------------/

    public VehicleConfiguration() {
        super();
        this._vehicleList = new java.util.ArrayList<org.mars_sim.msp.config.model.vehicle.Vehicle>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vVehicle
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addVehicle(
            final org.mars_sim.msp.config.model.vehicle.Vehicle vVehicle)
    throws java.lang.IndexOutOfBoundsException {
        this._vehicleList.add(vVehicle);
    }

    /**
     * 
     * 
     * @param index
     * @param vVehicle
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addVehicle(
            final int index,
            final org.mars_sim.msp.config.model.vehicle.Vehicle vVehicle)
    throws java.lang.IndexOutOfBoundsException {
        this._vehicleList.add(index, vVehicle);
    }

    /**
     * Method enumerateVehicle.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.vehicle.Vehicle> enumerateVehicle(
    ) {
        return java.util.Collections.enumeration(this._vehicleList);
    }

    /**
     * Returns the value of field 'roverNameList'.
     * 
     * @return the value of field 'RoverNameList'.
     */
    public org.mars_sim.msp.config.model.vehicle.RoverNameList getRoverNameList(
    ) {
        return this._roverNameList;
    }

    /**
     * Method getVehicle.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.vehicle.Vehicle at the given
     * index
     */
    public org.mars_sim.msp.config.model.vehicle.Vehicle getVehicle(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vehicleList.size()) {
            throw new IndexOutOfBoundsException("getVehicle: Index value '" + index + "' not in range [0.." + (this._vehicleList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.vehicle.Vehicle) _vehicleList.get(index);
    }

    /**
     * Method getVehicle.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.vehicle.Vehicle[] getVehicle(
    ) {
        org.mars_sim.msp.config.model.vehicle.Vehicle[] array = new org.mars_sim.msp.config.model.vehicle.Vehicle[0];
        return (org.mars_sim.msp.config.model.vehicle.Vehicle[]) this._vehicleList.toArray(array);
    }

    /**
     * Method getVehicleCount.
     * 
     * @return the size of this collection
     */
    public int getVehicleCount(
    ) {
        return this._vehicleList.size();
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
     * Method iterateVehicle.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.vehicle.Vehicle> iterateVehicle(
    ) {
        return this._vehicleList.iterator();
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
    public void removeAllVehicle(
    ) {
        this._vehicleList.clear();
    }

    /**
     * Method removeVehicle.
     * 
     * @param vVehicle
     * @return true if the object was removed from the collection.
     */
    public boolean removeVehicle(
            final org.mars_sim.msp.config.model.vehicle.Vehicle vVehicle) {
        boolean removed = _vehicleList.remove(vVehicle);
        return removed;
    }

    /**
     * Method removeVehicleAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.vehicle.Vehicle removeVehicleAt(
            final int index) {
        java.lang.Object obj = this._vehicleList.remove(index);
        return (org.mars_sim.msp.config.model.vehicle.Vehicle) obj;
    }

    /**
     * Sets the value of field 'roverNameList'.
     * 
     * @param roverNameList the value of field 'roverNameList'.
     */
    public void setRoverNameList(
            final org.mars_sim.msp.config.model.vehicle.RoverNameList roverNameList) {
        this._roverNameList = roverNameList;
    }

    /**
     * 
     * 
     * @param index
     * @param vVehicle
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setVehicle(
            final int index,
            final org.mars_sim.msp.config.model.vehicle.Vehicle vVehicle)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vehicleList.size()) {
            throw new IndexOutOfBoundsException("setVehicle: Index value '" + index + "' not in range [0.." + (this._vehicleList.size() - 1) + "]");
        }

        this._vehicleList.set(index, vVehicle);
    }

    /**
     * 
     * 
     * @param vVehicleArray
     */
    public void setVehicle(
            final org.mars_sim.msp.config.model.vehicle.Vehicle[] vVehicleArray) {
        //-- copy array
        _vehicleList.clear();

        for (int i = 0; i < vVehicleArray.length; i++) {
                this._vehicleList.add(vVehicleArray[i]);
        }
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
     * org.mars_sim.msp.config.model.vehicle.VehicleConfiguration
     */
    public static org.mars_sim.msp.config.model.vehicle.VehicleConfiguration unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.vehicle.VehicleConfiguration) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.vehicle.VehicleConfiguration.class, reader);
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
