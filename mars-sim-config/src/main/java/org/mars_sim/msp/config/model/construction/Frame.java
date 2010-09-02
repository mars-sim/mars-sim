/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.construction;

/**
 * Class Frame.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Frame implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _constructable.
     */
    private boolean _constructable;

    /**
     * keeps track of state for field: _constructable
     */
    private boolean _has_constructable;

    /**
     * Field _salvagable.
     */
    private boolean _salvagable;

    /**
     * keeps track of state for field: _salvagable
     */
    private boolean _has_salvagable;

    /**
     * Field _workTime.
     */
    private double _workTime;

    /**
     * keeps track of state for field: _workTime
     */
    private boolean _has_workTime;

    /**
     * Field _skillRequired.
     */
    private long _skillRequired;

    /**
     * keeps track of state for field: _skillRequired
     */
    private boolean _has_skillRequired;

    /**
     * Field _foundation.
     */
    private java.lang.String _foundation;

    /**
     * Field _width.
     */
    private double _width;

    /**
     * keeps track of state for field: _width
     */
    private boolean _has_width;

    /**
     * Field _length.
     */
    private double _length;

    /**
     * keeps track of state for field: _length
     */
    private boolean _has_length;

    /**
     * Field _resourceList.
     */
    private java.util.List<org.mars_sim.msp.config.model.construction.Resource> _resourceList;

    /**
     * Field _partList.
     */
    private java.util.List<org.mars_sim.msp.config.model.construction.Part> _partList;

    /**
     * Field _vehicleList.
     */
    private java.util.List<org.mars_sim.msp.config.model.construction.Vehicle> _vehicleList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Frame() {
        super();
        this._resourceList = new java.util.ArrayList<org.mars_sim.msp.config.model.construction.Resource>();
        this._partList = new java.util.ArrayList<org.mars_sim.msp.config.model.construction.Part>();
        this._vehicleList = new java.util.ArrayList<org.mars_sim.msp.config.model.construction.Vehicle>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vPart
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPart(
            final org.mars_sim.msp.config.model.construction.Part vPart)
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
            final org.mars_sim.msp.config.model.construction.Part vPart)
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
            final org.mars_sim.msp.config.model.construction.Resource vResource)
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
            final org.mars_sim.msp.config.model.construction.Resource vResource)
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
            final org.mars_sim.msp.config.model.construction.Vehicle vVehicle)
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
            final org.mars_sim.msp.config.model.construction.Vehicle vVehicle)
    throws java.lang.IndexOutOfBoundsException {
        this._vehicleList.add(index, vVehicle);
    }

    /**
     */
    public void deleteConstructable(
    ) {
        this._has_constructable= false;
    }

    /**
     */
    public void deleteLength(
    ) {
        this._has_length= false;
    }

    /**
     */
    public void deleteSalvagable(
    ) {
        this._has_salvagable= false;
    }

    /**
     */
    public void deleteSkillRequired(
    ) {
        this._has_skillRequired= false;
    }

    /**
     */
    public void deleteWidth(
    ) {
        this._has_width= false;
    }

    /**
     */
    public void deleteWorkTime(
    ) {
        this._has_workTime= false;
    }

    /**
     * Method enumeratePart.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.construction.Part> enumeratePart(
    ) {
        return java.util.Collections.enumeration(this._partList);
    }

    /**
     * Method enumerateResource.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.construction.Resource> enumerateResource(
    ) {
        return java.util.Collections.enumeration(this._resourceList);
    }

    /**
     * Method enumerateVehicle.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.construction.Vehicle> enumerateVehicle(
    ) {
        return java.util.Collections.enumeration(this._vehicleList);
    }

    /**
     * Returns the value of field 'constructable'.
     * 
     * @return the value of field 'Constructable'.
     */
    public boolean getConstructable(
    ) {
        return this._constructable;
    }

    /**
     * Returns the value of field 'foundation'.
     * 
     * @return the value of field 'Foundation'.
     */
    public java.lang.String getFoundation(
    ) {
        return this._foundation;
    }

    /**
     * Returns the value of field 'length'.
     * 
     * @return the value of field 'Length'.
     */
    public double getLength(
    ) {
        return this._length;
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
     * org.mars_sim.msp.config.model.construction.Part at the given
     * index
     */
    public org.mars_sim.msp.config.model.construction.Part getPart(
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
    public org.mars_sim.msp.config.model.construction.Part[] getPart(
    ) {
        org.mars_sim.msp.config.model.construction.Part[] array = new org.mars_sim.msp.config.model.construction.Part[0];
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
     * Method getResource.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.construction.Resource at the
     * given index
     */
    public org.mars_sim.msp.config.model.construction.Resource getResource(
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
    public org.mars_sim.msp.config.model.construction.Resource[] getResource(
    ) {
        org.mars_sim.msp.config.model.construction.Resource[] array = new org.mars_sim.msp.config.model.construction.Resource[0];
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
     * Returns the value of field 'salvagable'.
     * 
     * @return the value of field 'Salvagable'.
     */
    public boolean getSalvagable(
    ) {
        return this._salvagable;
    }

    /**
     * Returns the value of field 'skillRequired'.
     * 
     * @return the value of field 'SkillRequired'.
     */
    public long getSkillRequired(
    ) {
        return this._skillRequired;
    }

    /**
     * Method getVehicle.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.construction.Vehicle at the
     * given index
     */
    public org.mars_sim.msp.config.model.construction.Vehicle getVehicle(
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
    public org.mars_sim.msp.config.model.construction.Vehicle[] getVehicle(
    ) {
        org.mars_sim.msp.config.model.construction.Vehicle[] array = new org.mars_sim.msp.config.model.construction.Vehicle[0];
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
     * Returns the value of field 'width'.
     * 
     * @return the value of field 'Width'.
     */
    public double getWidth(
    ) {
        return this._width;
    }

    /**
     * Returns the value of field 'workTime'.
     * 
     * @return the value of field 'WorkTime'.
     */
    public double getWorkTime(
    ) {
        return this._workTime;
    }

    /**
     * Method hasConstructable.
     * 
     * @return true if at least one Constructable has been added
     */
    public boolean hasConstructable(
    ) {
        return this._has_constructable;
    }

    /**
     * Method hasLength.
     * 
     * @return true if at least one Length has been added
     */
    public boolean hasLength(
    ) {
        return this._has_length;
    }

    /**
     * Method hasSalvagable.
     * 
     * @return true if at least one Salvagable has been added
     */
    public boolean hasSalvagable(
    ) {
        return this._has_salvagable;
    }

    /**
     * Method hasSkillRequired.
     * 
     * @return true if at least one SkillRequired has been added
     */
    public boolean hasSkillRequired(
    ) {
        return this._has_skillRequired;
    }

    /**
     * Method hasWidth.
     * 
     * @return true if at least one Width has been added
     */
    public boolean hasWidth(
    ) {
        return this._has_width;
    }

    /**
     * Method hasWorkTime.
     * 
     * @return true if at least one WorkTime has been added
     */
    public boolean hasWorkTime(
    ) {
        return this._has_workTime;
    }

    /**
     * Returns the value of field 'constructable'.
     * 
     * @return the value of field 'Constructable'.
     */
    public boolean isConstructable(
    ) {
        return this._constructable;
    }

    /**
     * Returns the value of field 'salvagable'.
     * 
     * @return the value of field 'Salvagable'.
     */
    public boolean isSalvagable(
    ) {
        return this._salvagable;
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
     * Method iteratePart.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.construction.Part> iteratePart(
    ) {
        return this._partList.iterator();
    }

    /**
     * Method iterateResource.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.construction.Resource> iterateResource(
    ) {
        return this._resourceList.iterator();
    }

    /**
     * Method iterateVehicle.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.construction.Vehicle> iterateVehicle(
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
     * Method removePart.
     * 
     * @param vPart
     * @return true if the object was removed from the collection.
     */
    public boolean removePart(
            final org.mars_sim.msp.config.model.construction.Part vPart) {
        boolean removed = _partList.remove(vPart);
        return removed;
    }

    /**
     * Method removePartAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.construction.Part removePartAt(
            final int index) {
        java.lang.Object obj = this._partList.remove(index);
        return (org.mars_sim.msp.config.model.construction.Part) obj;
    }

    /**
     * Method removeResource.
     * 
     * @param vResource
     * @return true if the object was removed from the collection.
     */
    public boolean removeResource(
            final org.mars_sim.msp.config.model.construction.Resource vResource) {
        boolean removed = _resourceList.remove(vResource);
        return removed;
    }

    /**
     * Method removeResourceAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.construction.Resource removeResourceAt(
            final int index) {
        java.lang.Object obj = this._resourceList.remove(index);
        return (org.mars_sim.msp.config.model.construction.Resource) obj;
    }

    /**
     * Method removeVehicle.
     * 
     * @param vVehicle
     * @return true if the object was removed from the collection.
     */
    public boolean removeVehicle(
            final org.mars_sim.msp.config.model.construction.Vehicle vVehicle) {
        boolean removed = _vehicleList.remove(vVehicle);
        return removed;
    }

    /**
     * Method removeVehicleAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.construction.Vehicle removeVehicleAt(
            final int index) {
        java.lang.Object obj = this._vehicleList.remove(index);
        return (org.mars_sim.msp.config.model.construction.Vehicle) obj;
    }

    /**
     * Sets the value of field 'constructable'.
     * 
     * @param constructable the value of field 'constructable'.
     */
    public void setConstructable(
            final boolean constructable) {
        this._constructable = constructable;
        this._has_constructable = true;
    }

    /**
     * Sets the value of field 'foundation'.
     * 
     * @param foundation the value of field 'foundation'.
     */
    public void setFoundation(
            final java.lang.String foundation) {
        this._foundation = foundation;
    }

    /**
     * Sets the value of field 'length'.
     * 
     * @param length the value of field 'length'.
     */
    public void setLength(
            final double length) {
        this._length = length;
        this._has_length = true;
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
            final org.mars_sim.msp.config.model.construction.Part vPart)
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
            final org.mars_sim.msp.config.model.construction.Part[] vPartArray) {
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
            final org.mars_sim.msp.config.model.construction.Resource vResource)
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
            final org.mars_sim.msp.config.model.construction.Resource[] vResourceArray) {
        //-- copy array
        _resourceList.clear();

        for (int i = 0; i < vResourceArray.length; i++) {
                this._resourceList.add(vResourceArray[i]);
        }
    }

    /**
     * Sets the value of field 'salvagable'.
     * 
     * @param salvagable the value of field 'salvagable'.
     */
    public void setSalvagable(
            final boolean salvagable) {
        this._salvagable = salvagable;
        this._has_salvagable = true;
    }

    /**
     * Sets the value of field 'skillRequired'.
     * 
     * @param skillRequired the value of field 'skillRequired'.
     */
    public void setSkillRequired(
            final long skillRequired) {
        this._skillRequired = skillRequired;
        this._has_skillRequired = true;
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
            final org.mars_sim.msp.config.model.construction.Vehicle vVehicle)
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
            final org.mars_sim.msp.config.model.construction.Vehicle[] vVehicleArray) {
        //-- copy array
        _vehicleList.clear();

        for (int i = 0; i < vVehicleArray.length; i++) {
                this._vehicleList.add(vVehicleArray[i]);
        }
    }

    /**
     * Sets the value of field 'width'.
     * 
     * @param width the value of field 'width'.
     */
    public void setWidth(
            final double width) {
        this._width = width;
        this._has_width = true;
    }

    /**
     * Sets the value of field 'workTime'.
     * 
     * @param workTime the value of field 'workTime'.
     */
    public void setWorkTime(
            final double workTime) {
        this._workTime = workTime;
        this._has_workTime = true;
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
     * org.mars_sim.msp.config.model.construction.Frame
     */
    public static org.mars_sim.msp.config.model.construction.Frame unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.construction.Frame) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.construction.Frame.class, reader);
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
