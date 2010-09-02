/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.resupply;

/**
 * Class Resupply.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Resupply implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _buildingList.
     */
    private java.util.List<org.mars_sim.msp.config.model.resupply.Building> _buildingList;

    /**
     * Field _vehicleList.
     */
    private java.util.List<org.mars_sim.msp.config.model.resupply.Vehicle> _vehicleList;

    /**
     * Field _equipmentList.
     */
    private java.util.List<org.mars_sim.msp.config.model.resupply.Equipment> _equipmentList;

    /**
     * Field _personList.
     */
    private java.util.List<org.mars_sim.msp.config.model.resupply.Person> _personList;

    /**
     * Field _resourceList.
     */
    private java.util.List<org.mars_sim.msp.config.model.resupply.Resource> _resourceList;

    /**
     * Field _partList.
     */
    private java.util.List<org.mars_sim.msp.config.model.resupply.Part> _partList;

    /**
     * Field _partPackageList.
     */
    private java.util.List<org.mars_sim.msp.config.model.resupply.PartPackage> _partPackageList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Resupply() {
        super();
        this._buildingList = new java.util.ArrayList<org.mars_sim.msp.config.model.resupply.Building>();
        this._vehicleList = new java.util.ArrayList<org.mars_sim.msp.config.model.resupply.Vehicle>();
        this._equipmentList = new java.util.ArrayList<org.mars_sim.msp.config.model.resupply.Equipment>();
        this._personList = new java.util.ArrayList<org.mars_sim.msp.config.model.resupply.Person>();
        this._resourceList = new java.util.ArrayList<org.mars_sim.msp.config.model.resupply.Resource>();
        this._partList = new java.util.ArrayList<org.mars_sim.msp.config.model.resupply.Part>();
        this._partPackageList = new java.util.ArrayList<org.mars_sim.msp.config.model.resupply.PartPackage>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vBuilding
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addBuilding(
            final org.mars_sim.msp.config.model.resupply.Building vBuilding)
    throws java.lang.IndexOutOfBoundsException {
        this._buildingList.add(vBuilding);
    }

    /**
     * 
     * 
     * @param index
     * @param vBuilding
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addBuilding(
            final int index,
            final org.mars_sim.msp.config.model.resupply.Building vBuilding)
    throws java.lang.IndexOutOfBoundsException {
        this._buildingList.add(index, vBuilding);
    }

    /**
     * 
     * 
     * @param vEquipment
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEquipment(
            final org.mars_sim.msp.config.model.resupply.Equipment vEquipment)
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
            final org.mars_sim.msp.config.model.resupply.Equipment vEquipment)
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
            final org.mars_sim.msp.config.model.resupply.Part vPart)
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
            final org.mars_sim.msp.config.model.resupply.Part vPart)
    throws java.lang.IndexOutOfBoundsException {
        this._partList.add(index, vPart);
    }

    /**
     * 
     * 
     * @param vPartPackage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPartPackage(
            final org.mars_sim.msp.config.model.resupply.PartPackage vPartPackage)
    throws java.lang.IndexOutOfBoundsException {
        this._partPackageList.add(vPartPackage);
    }

    /**
     * 
     * 
     * @param index
     * @param vPartPackage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPartPackage(
            final int index,
            final org.mars_sim.msp.config.model.resupply.PartPackage vPartPackage)
    throws java.lang.IndexOutOfBoundsException {
        this._partPackageList.add(index, vPartPackage);
    }

    /**
     * 
     * 
     * @param vPerson
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPerson(
            final org.mars_sim.msp.config.model.resupply.Person vPerson)
    throws java.lang.IndexOutOfBoundsException {
        this._personList.add(vPerson);
    }

    /**
     * 
     * 
     * @param index
     * @param vPerson
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPerson(
            final int index,
            final org.mars_sim.msp.config.model.resupply.Person vPerson)
    throws java.lang.IndexOutOfBoundsException {
        this._personList.add(index, vPerson);
    }

    /**
     * 
     * 
     * @param vResource
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResource(
            final org.mars_sim.msp.config.model.resupply.Resource vResource)
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
            final org.mars_sim.msp.config.model.resupply.Resource vResource)
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
            final org.mars_sim.msp.config.model.resupply.Vehicle vVehicle)
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
            final org.mars_sim.msp.config.model.resupply.Vehicle vVehicle)
    throws java.lang.IndexOutOfBoundsException {
        this._vehicleList.add(index, vVehicle);
    }

    /**
     * Method enumerateBuilding.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.resupply.Building> enumerateBuilding(
    ) {
        return java.util.Collections.enumeration(this._buildingList);
    }

    /**
     * Method enumerateEquipment.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.resupply.Equipment> enumerateEquipment(
    ) {
        return java.util.Collections.enumeration(this._equipmentList);
    }

    /**
     * Method enumeratePart.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.resupply.Part> enumeratePart(
    ) {
        return java.util.Collections.enumeration(this._partList);
    }

    /**
     * Method enumeratePartPackage.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.resupply.PartPackage> enumeratePartPackage(
    ) {
        return java.util.Collections.enumeration(this._partPackageList);
    }

    /**
     * Method enumeratePerson.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.resupply.Person> enumeratePerson(
    ) {
        return java.util.Collections.enumeration(this._personList);
    }

    /**
     * Method enumerateResource.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.resupply.Resource> enumerateResource(
    ) {
        return java.util.Collections.enumeration(this._resourceList);
    }

    /**
     * Method enumerateVehicle.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.resupply.Vehicle> enumerateVehicle(
    ) {
        return java.util.Collections.enumeration(this._vehicleList);
    }

    /**
     * Method getBuilding.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.resupply.Building at the given
     * index
     */
    public org.mars_sim.msp.config.model.resupply.Building getBuilding(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._buildingList.size()) {
            throw new IndexOutOfBoundsException("getBuilding: Index value '" + index + "' not in range [0.." + (this._buildingList.size() - 1) + "]");
        }

        return _buildingList.get(index);
    }

    /**
     * Method getBuilding.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.resupply.Building[] getBuilding(
    ) {
        org.mars_sim.msp.config.model.resupply.Building[] array = new org.mars_sim.msp.config.model.resupply.Building[0];
        return this._buildingList.toArray(array);
    }

    /**
     * Method getBuildingCount.
     * 
     * @return the size of this collection
     */
    public int getBuildingCount(
    ) {
        return this._buildingList.size();
    }

    /**
     * Method getEquipment.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.resupply.Equipment at the
     * given index
     */
    public org.mars_sim.msp.config.model.resupply.Equipment getEquipment(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._equipmentList.size()) {
            throw new IndexOutOfBoundsException("getEquipment: Index value '" + index + "' not in range [0.." + (this._equipmentList.size() - 1) + "]");
        }

        return _equipmentList.get(index);
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
    public org.mars_sim.msp.config.model.resupply.Equipment[] getEquipment(
    ) {
        org.mars_sim.msp.config.model.resupply.Equipment[] array = new org.mars_sim.msp.config.model.resupply.Equipment[0];
        return this._equipmentList.toArray(array);
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
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public java.lang.String getName(
    ) {
        return this._name;
    }

    /**
     * Method getPart.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.resupply.Part at the given inde
     */
    public org.mars_sim.msp.config.model.resupply.Part getPart(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._partList.size()) {
            throw new IndexOutOfBoundsException("getPart: Index value '" + index + "' not in range [0.." + (this._partList.size() - 1) + "]");
        }

        return _partList.get(index);
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
    public org.mars_sim.msp.config.model.resupply.Part[] getPart(
    ) {
        org.mars_sim.msp.config.model.resupply.Part[] array = new org.mars_sim.msp.config.model.resupply.Part[0];
        return this._partList.toArray(array);
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
     * Method getPartPackage.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.resupply.PartPackage at the
     * given index
     */
    public org.mars_sim.msp.config.model.resupply.PartPackage getPartPackage(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._partPackageList.size()) {
            throw new IndexOutOfBoundsException("getPartPackage: Index value '" + index + "' not in range [0.." + (this._partPackageList.size() - 1) + "]");
        }

        return _partPackageList.get(index);
    }

    /**
     * Method getPartPackage.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.resupply.PartPackage[] getPartPackage(
    ) {
        org.mars_sim.msp.config.model.resupply.PartPackage[] array = new org.mars_sim.msp.config.model.resupply.PartPackage[0];
        return this._partPackageList.toArray(array);
    }

    /**
     * Method getPartPackageCount.
     * 
     * @return the size of this collection
     */
    public int getPartPackageCount(
    ) {
        return this._partPackageList.size();
    }

    /**
     * Method getPerson.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.resupply.Person at the given
     * index
     */
    public org.mars_sim.msp.config.model.resupply.Person getPerson(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._personList.size()) {
            throw new IndexOutOfBoundsException("getPerson: Index value '" + index + "' not in range [0.." + (this._personList.size() - 1) + "]");
        }

        return _personList.get(index);
    }

    /**
     * Method getPerson.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.resupply.Person[] getPerson(
    ) {
        org.mars_sim.msp.config.model.resupply.Person[] array = new org.mars_sim.msp.config.model.resupply.Person[0];
        return this._personList.toArray(array);
    }

    /**
     * Method getPersonCount.
     * 
     * @return the size of this collection
     */
    public int getPersonCount(
    ) {
        return this._personList.size();
    }

    /**
     * Method getResource.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.resupply.Resource at the given
     * index
     */
    public org.mars_sim.msp.config.model.resupply.Resource getResource(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._resourceList.size()) {
            throw new IndexOutOfBoundsException("getResource: Index value '" + index + "' not in range [0.." + (this._resourceList.size() - 1) + "]");
        }

        return _resourceList.get(index);
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
    public org.mars_sim.msp.config.model.resupply.Resource[] getResource(
    ) {
        org.mars_sim.msp.config.model.resupply.Resource[] array = new org.mars_sim.msp.config.model.resupply.Resource[0];
        return this._resourceList.toArray(array);
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
     * org.mars_sim.msp.config.model.resupply.Vehicle at the given
     * index
     */
    public org.mars_sim.msp.config.model.resupply.Vehicle getVehicle(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._vehicleList.size()) {
            throw new IndexOutOfBoundsException("getVehicle: Index value '" + index + "' not in range [0.." + (this._vehicleList.size() - 1) + "]");
        }

        return _vehicleList.get(index);
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
    public org.mars_sim.msp.config.model.resupply.Vehicle[] getVehicle(
    ) {
        org.mars_sim.msp.config.model.resupply.Vehicle[] array = new org.mars_sim.msp.config.model.resupply.Vehicle[0];
        return this._vehicleList.toArray(array);
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
     * Method iterateBuilding.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.resupply.Building> iterateBuilding(
    ) {
        return this._buildingList.iterator();
    }

    /**
     * Method iterateEquipment.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.resupply.Equipment> iterateEquipment(
    ) {
        return this._equipmentList.iterator();
    }

    /**
     * Method iteratePart.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.resupply.Part> iteratePart(
    ) {
        return this._partList.iterator();
    }

    /**
     * Method iteratePartPackage.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.resupply.PartPackage> iteratePartPackage(
    ) {
        return this._partPackageList.iterator();
    }

    /**
     * Method iteratePerson.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.resupply.Person> iteratePerson(
    ) {
        return this._personList.iterator();
    }

    /**
     * Method iterateResource.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.resupply.Resource> iterateResource(
    ) {
        return this._resourceList.iterator();
    }

    /**
     * Method iterateVehicle.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.resupply.Vehicle> iterateVehicle(
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
    public void removeAllBuilding(
    ) {
        this._buildingList.clear();
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
    public void removeAllPartPackage(
    ) {
        this._partPackageList.clear();
    }

    /**
     */
    public void removeAllPerson(
    ) {
        this._personList.clear();
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
     * Method removeBuilding.
     * 
     * @param vBuilding
     * @return true if the object was removed from the collection.
     */
    public boolean removeBuilding(
            final org.mars_sim.msp.config.model.resupply.Building vBuilding) {
        boolean removed = _buildingList.remove(vBuilding);
        return removed;
    }

    /**
     * Method removeBuildingAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.resupply.Building removeBuildingAt(
            final int index) {
        java.lang.Object obj = this._buildingList.remove(index);
        return (org.mars_sim.msp.config.model.resupply.Building) obj;
    }

    /**
     * Method removeEquipment.
     * 
     * @param vEquipment
     * @return true if the object was removed from the collection.
     */
    public boolean removeEquipment(
            final org.mars_sim.msp.config.model.resupply.Equipment vEquipment) {
        boolean removed = _equipmentList.remove(vEquipment);
        return removed;
    }

    /**
     * Method removeEquipmentAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.resupply.Equipment removeEquipmentAt(
            final int index) {
        java.lang.Object obj = this._equipmentList.remove(index);
        return (org.mars_sim.msp.config.model.resupply.Equipment) obj;
    }

    /**
     * Method removePart.
     * 
     * @param vPart
     * @return true if the object was removed from the collection.
     */
    public boolean removePart(
            final org.mars_sim.msp.config.model.resupply.Part vPart) {
        boolean removed = _partList.remove(vPart);
        return removed;
    }

    /**
     * Method removePartAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.resupply.Part removePartAt(
            final int index) {
        java.lang.Object obj = this._partList.remove(index);
        return (org.mars_sim.msp.config.model.resupply.Part) obj;
    }

    /**
     * Method removePartPackage.
     * 
     * @param vPartPackage
     * @return true if the object was removed from the collection.
     */
    public boolean removePartPackage(
            final org.mars_sim.msp.config.model.resupply.PartPackage vPartPackage) {
        boolean removed = _partPackageList.remove(vPartPackage);
        return removed;
    }

    /**
     * Method removePartPackageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.resupply.PartPackage removePartPackageAt(
            final int index) {
        java.lang.Object obj = this._partPackageList.remove(index);
        return (org.mars_sim.msp.config.model.resupply.PartPackage) obj;
    }

    /**
     * Method removePerson.
     * 
     * @param vPerson
     * @return true if the object was removed from the collection.
     */
    public boolean removePerson(
            final org.mars_sim.msp.config.model.resupply.Person vPerson) {
        boolean removed = _personList.remove(vPerson);
        return removed;
    }

    /**
     * Method removePersonAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.resupply.Person removePersonAt(
            final int index) {
        java.lang.Object obj = this._personList.remove(index);
        return (org.mars_sim.msp.config.model.resupply.Person) obj;
    }

    /**
     * Method removeResource.
     * 
     * @param vResource
     * @return true if the object was removed from the collection.
     */
    public boolean removeResource(
            final org.mars_sim.msp.config.model.resupply.Resource vResource) {
        boolean removed = _resourceList.remove(vResource);
        return removed;
    }

    /**
     * Method removeResourceAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.resupply.Resource removeResourceAt(
            final int index) {
        java.lang.Object obj = this._resourceList.remove(index);
        return (org.mars_sim.msp.config.model.resupply.Resource) obj;
    }

    /**
     * Method removeVehicle.
     * 
     * @param vVehicle
     * @return true if the object was removed from the collection.
     */
    public boolean removeVehicle(
            final org.mars_sim.msp.config.model.resupply.Vehicle vVehicle) {
        boolean removed = _vehicleList.remove(vVehicle);
        return removed;
    }

    /**
     * Method removeVehicleAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.resupply.Vehicle removeVehicleAt(
            final int index) {
        java.lang.Object obj = this._vehicleList.remove(index);
        return (org.mars_sim.msp.config.model.resupply.Vehicle) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vBuilding
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setBuilding(
            final int index,
            final org.mars_sim.msp.config.model.resupply.Building vBuilding)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._buildingList.size()) {
            throw new IndexOutOfBoundsException("setBuilding: Index value '" + index + "' not in range [0.." + (this._buildingList.size() - 1) + "]");
        }

        this._buildingList.set(index, vBuilding);
    }

    /**
     * 
     * 
     * @param vBuildingArray
     */
    public void setBuilding(
            final org.mars_sim.msp.config.model.resupply.Building[] vBuildingArray) {
        //-- copy array
        _buildingList.clear();

        for (int i = 0; i < vBuildingArray.length; i++) {
                this._buildingList.add(vBuildingArray[i]);
        }
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
            final org.mars_sim.msp.config.model.resupply.Equipment vEquipment)
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
            final org.mars_sim.msp.config.model.resupply.Equipment[] vEquipmentArray) {
        //-- copy array
        _equipmentList.clear();

        for (int i = 0; i < vEquipmentArray.length; i++) {
                this._equipmentList.add(vEquipmentArray[i]);
        }
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
     * 
     * 
     * @param index
     * @param vPart
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setPart(
            final int index,
            final org.mars_sim.msp.config.model.resupply.Part vPart)
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
            final org.mars_sim.msp.config.model.resupply.Part[] vPartArray) {
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
     * @param vPartPackage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setPartPackage(
            final int index,
            final org.mars_sim.msp.config.model.resupply.PartPackage vPartPackage)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._partPackageList.size()) {
            throw new IndexOutOfBoundsException("setPartPackage: Index value '" + index + "' not in range [0.." + (this._partPackageList.size() - 1) + "]");
        }

        this._partPackageList.set(index, vPartPackage);
    }

    /**
     * 
     * 
     * @param vPartPackageArray
     */
    public void setPartPackage(
            final org.mars_sim.msp.config.model.resupply.PartPackage[] vPartPackageArray) {
        //-- copy array
        _partPackageList.clear();

        for (int i = 0; i < vPartPackageArray.length; i++) {
                this._partPackageList.add(vPartPackageArray[i]);
        }
    }

    /**
     * 
     * 
     * @param index
     * @param vPerson
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setPerson(
            final int index,
            final org.mars_sim.msp.config.model.resupply.Person vPerson)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._personList.size()) {
            throw new IndexOutOfBoundsException("setPerson: Index value '" + index + "' not in range [0.." + (this._personList.size() - 1) + "]");
        }

        this._personList.set(index, vPerson);
    }

    /**
     * 
     * 
     * @param vPersonArray
     */
    public void setPerson(
            final org.mars_sim.msp.config.model.resupply.Person[] vPersonArray) {
        //-- copy array
        _personList.clear();

        for (int i = 0; i < vPersonArray.length; i++) {
                this._personList.add(vPersonArray[i]);
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
            final org.mars_sim.msp.config.model.resupply.Resource vResource)
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
            final org.mars_sim.msp.config.model.resupply.Resource[] vResourceArray) {
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
            final org.mars_sim.msp.config.model.resupply.Vehicle vVehicle)
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
            final org.mars_sim.msp.config.model.resupply.Vehicle[] vVehicleArray) {
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
     * org.mars_sim.msp.config.model.resupply.Resupply
     */
    public static org.mars_sim.msp.config.model.resupply.Resupply unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.resupply.Resupply) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.resupply.Resupply.class, reader);
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
