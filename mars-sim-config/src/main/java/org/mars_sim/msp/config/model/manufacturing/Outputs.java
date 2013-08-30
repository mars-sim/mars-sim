/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.manufacturing;

/**
 * Class Outputs.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Outputs implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _resourceList.
     */
    private java.util.List<org.mars_sim.msp.config.model.manufacturing.Resource> _resourceList;

    /**
     * Field _partList.
     */
    private java.util.List<org.mars_sim.msp.config.model.manufacturing.Part> _partList;

    /**
     * Field _equipmentList.
     */
    private java.util.List<org.mars_sim.msp.config.model.manufacturing.Equipment> _equipmentList;

    /**
     * Field _vehicleList.
     */
    private java.util.List<org.mars_sim.msp.config.model.manufacturing.Vehicle> _vehicleList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Outputs() {
        super();
        this._resourceList = new java.util.ArrayList<org.mars_sim.msp.config.model.manufacturing.Resource>();
        this._partList = new java.util.ArrayList<org.mars_sim.msp.config.model.manufacturing.Part>();
        this._equipmentList = new java.util.ArrayList<org.mars_sim.msp.config.model.manufacturing.Equipment>();
        this._vehicleList = new java.util.ArrayList<org.mars_sim.msp.config.model.manufacturing.Vehicle>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vEquipment
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEquipment(
            final org.mars_sim.msp.config.model.manufacturing.Equipment vEquipment)
    throws java.lang.IndexOutOfBoundsException {
        this._equipmentList.add(vEquipment);
    }

    /**
     * 
     * 
     * @param index
     * @param vEquipment
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEquipment(
            final int index,
            final org.mars_sim.msp.config.model.manufacturing.Equipment vEquipment)
    throws java.lang.IndexOutOfBoundsException {
        this._equipmentList.add(index, vEquipment);
    }

    /**
     * 
     * 
     * @param vPart
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPart(
            final org.mars_sim.msp.config.model.manufacturing.Part vPart)
    throws java.lang.IndexOutOfBoundsException {
        this._partList.add(vPart);
    }

    /**
     * 
     * 
     * @param index
     * @param vPart
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPart(
            final int index,
            final org.mars_sim.msp.config.model.manufacturing.Part vPart)
    throws java.lang.IndexOutOfBoundsException {
        this._partList.add(index, vPart);
    }

    /**
     * 
     * 
     * @param vResource
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResource(
            final org.mars_sim.msp.config.model.manufacturing.Resource vResource)
    throws java.lang.IndexOutOfBoundsException {
        this._resourceList.add(vResource);
    }

    /**
     * 
     * 
     * @param index
     * @param vResource
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResource(
            final int index,
            final org.mars_sim.msp.config.model.manufacturing.Resource vResource)
    throws java.lang.IndexOutOfBoundsException {
        this._resourceList.add(index, vResource);
    }

    /**
     * 
     * 
     * @param vVehicle
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addVehicle(
            final org.mars_sim.msp.config.model.manufacturing.Vehicle vVehicle)
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
            final org.mars_sim.msp.config.model.manufacturing.Vehicle vVehicle)
    throws java.lang.IndexOutOfBoundsException {
        this._vehicleList.add(index, vVehicle);
    }

    /**
     * Method enumerateEquipment.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.manufacturing.Equipment> enumerateEquipment(
    ) {
        return java.util.Collections.enumeration(this._equipmentList);
    }

    /**
     * Method enumeratePart.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.manufacturing.Part> enumeratePart(
    ) {
        return java.util.Collections.enumeration(this._partList);
    }

    /**
     * Method enumerateResource.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.manufacturing.Resource> enumerateResource(
    ) {
        return java.util.Collections.enumeration(this._resourceList);
    }

    /**
     * Method enumerateVehicle.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.manufacturing.Vehicle> enumerateVehicle(
    ) {
        return java.util.Collections.enumeration(this._vehicleList);
    }

    /**
     * Method getEquipment.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.manufacturing.Equipment at the
     * given index
     */
    public org.mars_sim.msp.config.model.manufacturing.Equipment getEquipment(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._equipmentList.size()) {
            throw new IndexOutOfBoundsException("getEquipment: Index value '" + index + "' not in range [0.." + (this._equipmentList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.manufacturing.Equipment) _equipmentList.get(index);
    }

    /**
     * Method getEquipment.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.manufacturing.Equipment[] getEquipment(
    ) {
        org.mars_sim.msp.config.model.manufacturing.Equipment[] array = new org.mars_sim.msp.config.model.manufacturing.Equipment[0];
        return (org.mars_sim.msp.config.model.manufacturing.Equipment[]) this._equipmentList.toArray(array);
    }

    /**
     * Method getEquipmentCount.
     * 
     * @return the size of this collection
     */
    public int getEquipmentCount(
    ) {
        return this._equipmentList.size();
    }

    /**
     * Method getPart.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.manufacturing.Part at the
     * given index
     */
    public org.mars_sim.msp.config.model.manufacturing.Part getPart(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._partList.size()) {
            throw new IndexOutOfBoundsException("getPart: Index value '" + index + "' not in range [0.." + (this._partList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.manufacturing.Part) _partList.get(index);
    }

    /**
     * Method getPart.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.manufacturing.Part[] getPart(
    ) {
        org.mars_sim.msp.config.model.manufacturing.Part[] array = new org.mars_sim.msp.config.model.manufacturing.Part[0];
        return (org.mars_sim.msp.config.model.manufacturing.Part[]) this._partList.toArray(array);
    }

    /**
     * Method getPartCount.
     * 
     * @return the size of this collection
     */
    public int getPartCount(
    ) {
        return this._partList.size();
    }

    /**
     * Method getResource.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.manufacturing.Resource at the
     * given index
     */
    public org.mars_sim.msp.config.model.manufacturing.Resource getResource(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._resourceList.size()) {
            throw new IndexOutOfBoundsException("getResource: Index value '" + index + "' not in range [0.." + (this._resourceList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.manufacturing.Resource) _resourceList.get(index);
    }

    /**
     * Method getResource.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.manufacturing.Resource[] getResource(
    ) {
        org.mars_sim.msp.config.model.manufacturing.Resource[] array = new org.mars_sim.msp.config.model.manufacturing.Resource[0];
        return (org.mars_sim.msp.config.model.manufacturing.Resource[]) this._resourceList.toArray(array);
    }

    /**
     * Method getResourceCount.
     * 
     * @return the size of this collection
     */
    public int getResourceCount(
    ) {
        return this._resourceList.size();
    }

    /**
     * Method getVehicle.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.manufacturing.Vehicle at the
     * given index
     */
    public org.mars_sim.msp.config.model.manufacturing.Vehicle getVehicle(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vehicleList.size()) {
            throw new IndexOutOfBoundsException("getVehicle: Index value '" + index + "' not in range [0.." + (this._vehicleList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.manufacturing.Vehicle) _vehicleList.get(index);
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
    public org.mars_sim.msp.config.model.manufacturing.Vehicle[] getVehicle(
    ) {
        org.mars_sim.msp.config.model.manufacturing.Vehicle[] array = new org.mars_sim.msp.config.model.manufacturing.Vehicle[0];
        return (org.mars_sim.msp.config.model.manufacturing.Vehicle[]) this._vehicleList.toArray(array);
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
     * Method iterateEquipment.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.manufacturing.Equipment> iterateEquipment(
    ) {
        return this._equipmentList.iterator();
    }

    /**
     * Method iteratePart.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.manufacturing.Part> iteratePart(
    ) {
        return this._partList.iterator();
    }

    /**
     * Method iterateResource.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.manufacturing.Resource> iterateResource(
    ) {
        return this._resourceList.iterator();
    }

    /**
     * Method iterateVehicle.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.manufacturing.Vehicle> iterateVehicle(
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
    public void removeAllEquipment(
    ) {
        this._equipmentList.clear();
    }

    /**
     */
    public void removeAllPart(
    ) {
        this._partList.clear();
    }

    /**
     */
    public void removeAllResource(
    ) {
        this._resourceList.clear();
    }

    /**
     */
    public void removeAllVehicle(
    ) {
        this._vehicleList.clear();
    }

    /**
     * Method removeEquipment.
     * 
     * @param vEquipment
     * @return true if the object was removed from the collection.
     */
    public boolean removeEquipment(
            final org.mars_sim.msp.config.model.manufacturing.Equipment vEquipment) {
        boolean removed = _equipmentList.remove(vEquipment);
        return removed;
    }

    /**
     * Method removeEquipmentAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.manufacturing.Equipment removeEquipmentAt(
            final int index) {
        java.lang.Object obj = this._equipmentList.remove(index);
        return (org.mars_sim.msp.config.model.manufacturing.Equipment) obj;
    }

    /**
     * Method removePart.
     * 
     * @param vPart
     * @return true if the object was removed from the collection.
     */
    public boolean removePart(
            final org.mars_sim.msp.config.model.manufacturing.Part vPart) {
        boolean removed = _partList.remove(vPart);
        return removed;
    }

    /**
     * Method removePartAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.manufacturing.Part removePartAt(
            final int index) {
        java.lang.Object obj = this._partList.remove(index);
        return (org.mars_sim.msp.config.model.manufacturing.Part) obj;
    }

    /**
     * Method removeResource.
     * 
     * @param vResource
     * @return true if the object was removed from the collection.
     */
    public boolean removeResource(
            final org.mars_sim.msp.config.model.manufacturing.Resource vResource) {
        boolean removed = _resourceList.remove(vResource);
        return removed;
    }

    /**
     * Method removeResourceAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.manufacturing.Resource removeResourceAt(
            final int index) {
        java.lang.Object obj = this._resourceList.remove(index);
        return (org.mars_sim.msp.config.model.manufacturing.Resource) obj;
    }

    /**
     * Method removeVehicle.
     * 
     * @param vVehicle
     * @return true if the object was removed from the collection.
     */
    public boolean removeVehicle(
            final org.mars_sim.msp.config.model.manufacturing.Vehicle vVehicle) {
        boolean removed = _vehicleList.remove(vVehicle);
        return removed;
    }

    /**
     * Method removeVehicleAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.manufacturing.Vehicle removeVehicleAt(
            final int index) {
        java.lang.Object obj = this._vehicleList.remove(index);
        return (org.mars_sim.msp.config.model.manufacturing.Vehicle) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vEquipment
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setEquipment(
            final int index,
            final org.mars_sim.msp.config.model.manufacturing.Equipment vEquipment)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._equipmentList.size()) {
            throw new IndexOutOfBoundsException("setEquipment: Index value '" + index + "' not in range [0.." + (this._equipmentList.size() - 1) + "]");
        }

        this._equipmentList.set(index, vEquipment);
    }

    /**
     * 
     * 
     * @param vEquipmentArray
     */
    public void setEquipment(
            final org.mars_sim.msp.config.model.manufacturing.Equipment[] vEquipmentArray) {
        //-- copy array
        _equipmentList.clear();

        for (int i = 0; i < vEquipmentArray.length; i++) {
                this._equipmentList.add(vEquipmentArray[i]);
        }
    }

    /**
     * 
     * 
     * @param index
     * @param vPart
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setPart(
            final int index,
            final org.mars_sim.msp.config.model.manufacturing.Part vPart)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._partList.size()) {
            throw new IndexOutOfBoundsException("setPart: Index value '" + index + "' not in range [0.." + (this._partList.size() - 1) + "]");
        }

        this._partList.set(index, vPart);
    }

    /**
     * 
     * 
     * @param vPartArray
     */
    public void setPart(
            final org.mars_sim.msp.config.model.manufacturing.Part[] vPartArray) {
        //-- copy array
        _partList.clear();

        for (int i = 0; i < vPartArray.length; i++) {
                this._partList.add(vPartArray[i]);
        }
    }

    /**
     * 
     * 
     * @param index
     * @param vResource
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setResource(
            final int index,
            final org.mars_sim.msp.config.model.manufacturing.Resource vResource)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._resourceList.size()) {
            throw new IndexOutOfBoundsException("setResource: Index value '" + index + "' not in range [0.." + (this._resourceList.size() - 1) + "]");
        }

        this._resourceList.set(index, vResource);
    }

    /**
     * 
     * 
     * @param vResourceArray
     */
    public void setResource(
            final org.mars_sim.msp.config.model.manufacturing.Resource[] vResourceArray) {
        //-- copy array
        _resourceList.clear();

        for (int i = 0; i < vResourceArray.length; i++) {
                this._resourceList.add(vResourceArray[i]);
        }
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
            final org.mars_sim.msp.config.model.manufacturing.Vehicle vVehicle)
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
            final org.mars_sim.msp.config.model.manufacturing.Vehicle[] vVehicleArray) {
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
     * org.mars_sim.msp.config.model.manufacturing.Outputs
     */
    public static org.mars_sim.msp.config.model.manufacturing.Outputs unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.manufacturing.Outputs) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.manufacturing.Outputs.class, reader);
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
