/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.vehicle;

/**
 * Class Cargo.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Cargo implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _totalCapacity.
     */
    private double _totalCapacity;

    /**
     * keeps track of state for field: _totalCapacity
     */
    private boolean _has_totalCapacity;

    /**
     * Field _capacityList.
     */
    private java.util.List<org.mars_sim.msp.config.model.vehicle.Capacity> _capacityList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Cargo() {
        super();
        this._capacityList = new java.util.ArrayList<org.mars_sim.msp.config.model.vehicle.Capacity>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vCapacity
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCapacity(
            final org.mars_sim.msp.config.model.vehicle.Capacity vCapacity)
    throws java.lang.IndexOutOfBoundsException {
        this._capacityList.add(vCapacity);
    }

    /**
     * 
     * 
     * @param index
     * @param vCapacity
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCapacity(
            final int index,
            final org.mars_sim.msp.config.model.vehicle.Capacity vCapacity)
    throws java.lang.IndexOutOfBoundsException {
        this._capacityList.add(index, vCapacity);
    }

    /**
     */
    public void deleteTotalCapacity(
    ) {
        this._has_totalCapacity= false;
    }

    /**
     * Method enumerateCapacity.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.vehicle.Capacity> enumerateCapacity(
    ) {
        return java.util.Collections.enumeration(this._capacityList);
    }

    /**
     * Method getCapacity.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.vehicle.Capacity at the given
     * index
     */
    public org.mars_sim.msp.config.model.vehicle.Capacity getCapacity(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._capacityList.size()) {
            throw new IndexOutOfBoundsException("getCapacity: Index value '" + index + "' not in range [0.." + (this._capacityList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.vehicle.Capacity) _capacityList.get(index);
    }

    /**
     * Method getCapacity.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.vehicle.Capacity[] getCapacity(
    ) {
        org.mars_sim.msp.config.model.vehicle.Capacity[] array = new org.mars_sim.msp.config.model.vehicle.Capacity[0];
        return (org.mars_sim.msp.config.model.vehicle.Capacity[]) this._capacityList.toArray(array);
    }

    /**
     * Method getCapacityCount.
     * 
     * @return the size of this collection
     */
    public int getCapacityCount(
    ) {
        return this._capacityList.size();
    }

    /**
     * Returns the value of field 'totalCapacity'.
     * 
     * @return the value of field 'TotalCapacity'.
     */
    public double getTotalCapacity(
    ) {
        return this._totalCapacity;
    }

    /**
     * Method hasTotalCapacity.
     * 
     * @return true if at least one TotalCapacity has been added
     */
    public boolean hasTotalCapacity(
    ) {
        return this._has_totalCapacity;
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
     * Method iterateCapacity.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.vehicle.Capacity> iterateCapacity(
    ) {
        return this._capacityList.iterator();
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
    public void removeAllCapacity(
    ) {
        this._capacityList.clear();
    }

    /**
     * Method removeCapacity.
     * 
     * @param vCapacity
     * @return true if the object was removed from the collection.
     */
    public boolean removeCapacity(
            final org.mars_sim.msp.config.model.vehicle.Capacity vCapacity) {
        boolean removed = _capacityList.remove(vCapacity);
        return removed;
    }

    /**
     * Method removeCapacityAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.vehicle.Capacity removeCapacityAt(
            final int index) {
        java.lang.Object obj = this._capacityList.remove(index);
        return (org.mars_sim.msp.config.model.vehicle.Capacity) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vCapacity
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setCapacity(
            final int index,
            final org.mars_sim.msp.config.model.vehicle.Capacity vCapacity)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._capacityList.size()) {
            throw new IndexOutOfBoundsException("setCapacity: Index value '" + index + "' not in range [0.." + (this._capacityList.size() - 1) + "]");
        }

        this._capacityList.set(index, vCapacity);
    }

    /**
     * 
     * 
     * @param vCapacityArray
     */
    public void setCapacity(
            final org.mars_sim.msp.config.model.vehicle.Capacity[] vCapacityArray) {
        //-- copy array
        _capacityList.clear();

        for (int i = 0; i < vCapacityArray.length; i++) {
                this._capacityList.add(vCapacityArray[i]);
        }
    }

    /**
     * Sets the value of field 'totalCapacity'.
     * 
     * @param totalCapacity the value of field 'totalCapacity'.
     */
    public void setTotalCapacity(
            final double totalCapacity) {
        this._totalCapacity = totalCapacity;
        this._has_totalCapacity = true;
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
     * org.mars_sim.msp.config.model.vehicle.Cargo
     */
    public static org.mars_sim.msp.config.model.vehicle.Cargo unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.vehicle.Cargo) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.vehicle.Cargo.class, reader);
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
