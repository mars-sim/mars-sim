/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.building;

/**
 * Class Storage.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Storage implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _resourceStorageList.
     */
    private java.util.List<org.mars_sim.msp.config.model.building.ResourceStorage> _resourceStorageList;

    /**
     * Field _resourceInitialList.
     */
    private java.util.List<org.mars_sim.msp.config.model.building.ResourceInitial> _resourceInitialList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Storage() {
        super();
        this._resourceStorageList = new java.util.ArrayList<org.mars_sim.msp.config.model.building.ResourceStorage>();
        this._resourceInitialList = new java.util.ArrayList<org.mars_sim.msp.config.model.building.ResourceInitial>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vResourceInitial
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResourceInitial(
            final org.mars_sim.msp.config.model.building.ResourceInitial vResourceInitial)
    throws java.lang.IndexOutOfBoundsException {
        this._resourceInitialList.add(vResourceInitial);
    }

    /**
     * 
     * 
     * @param index
     * @param vResourceInitial
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResourceInitial(
            final int index,
            final org.mars_sim.msp.config.model.building.ResourceInitial vResourceInitial)
    throws java.lang.IndexOutOfBoundsException {
        this._resourceInitialList.add(index, vResourceInitial);
    }

    /**
     * 
     * 
     * @param vResourceStorage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResourceStorage(
            final org.mars_sim.msp.config.model.building.ResourceStorage vResourceStorage)
    throws java.lang.IndexOutOfBoundsException {
        this._resourceStorageList.add(vResourceStorage);
    }

    /**
     * 
     * 
     * @param index
     * @param vResourceStorage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResourceStorage(
            final int index,
            final org.mars_sim.msp.config.model.building.ResourceStorage vResourceStorage)
    throws java.lang.IndexOutOfBoundsException {
        this._resourceStorageList.add(index, vResourceStorage);
    }

    /**
     * Method enumerateResourceInitial.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.building.ResourceInitial> enumerateResourceInitial(
    ) {
        return java.util.Collections.enumeration(this._resourceInitialList);
    }

    /**
     * Method enumerateResourceStorage.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.building.ResourceStorage> enumerateResourceStorage(
    ) {
        return java.util.Collections.enumeration(this._resourceStorageList);
    }

    /**
     * Method getResourceInitial.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.building.ResourceInitial at
     * the given index
     */
    public org.mars_sim.msp.config.model.building.ResourceInitial getResourceInitial(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._resourceInitialList.size()) {
            throw new IndexOutOfBoundsException("getResourceInitial: Index value '" + index + "' not in range [0.." + (this._resourceInitialList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.building.ResourceInitial) _resourceInitialList.get(index);
    }

    /**
     * Method getResourceInitial.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.building.ResourceInitial[] getResourceInitial(
    ) {
        org.mars_sim.msp.config.model.building.ResourceInitial[] array = new org.mars_sim.msp.config.model.building.ResourceInitial[0];
        return (org.mars_sim.msp.config.model.building.ResourceInitial[]) this._resourceInitialList.toArray(array);
    }

    /**
     * Method getResourceInitialCount.
     * 
     * @return the size of this collection
     */
    public int getResourceInitialCount(
    ) {
        return this._resourceInitialList.size();
    }

    /**
     * Method getResourceStorage.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.building.ResourceStorage at
     * the given index
     */
    public org.mars_sim.msp.config.model.building.ResourceStorage getResourceStorage(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._resourceStorageList.size()) {
            throw new IndexOutOfBoundsException("getResourceStorage: Index value '" + index + "' not in range [0.." + (this._resourceStorageList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.building.ResourceStorage) _resourceStorageList.get(index);
    }

    /**
     * Method getResourceStorage.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.building.ResourceStorage[] getResourceStorage(
    ) {
        org.mars_sim.msp.config.model.building.ResourceStorage[] array = new org.mars_sim.msp.config.model.building.ResourceStorage[0];
        return (org.mars_sim.msp.config.model.building.ResourceStorage[]) this._resourceStorageList.toArray(array);
    }

    /**
     * Method getResourceStorageCount.
     * 
     * @return the size of this collection
     */
    public int getResourceStorageCount(
    ) {
        return this._resourceStorageList.size();
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
     * Method iterateResourceInitial.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.building.ResourceInitial> iterateResourceInitial(
    ) {
        return this._resourceInitialList.iterator();
    }

    /**
     * Method iterateResourceStorage.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.building.ResourceStorage> iterateResourceStorage(
    ) {
        return this._resourceStorageList.iterator();
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
    public void removeAllResourceInitial(
    ) {
        this._resourceInitialList.clear();
    }

    /**
     */
    public void removeAllResourceStorage(
    ) {
        this._resourceStorageList.clear();
    }

    /**
     * Method removeResourceInitial.
     * 
     * @param vResourceInitial
     * @return true if the object was removed from the collection.
     */
    public boolean removeResourceInitial(
            final org.mars_sim.msp.config.model.building.ResourceInitial vResourceInitial) {
        boolean removed = _resourceInitialList.remove(vResourceInitial);
        return removed;
    }

    /**
     * Method removeResourceInitialAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.building.ResourceInitial removeResourceInitialAt(
            final int index) {
        java.lang.Object obj = this._resourceInitialList.remove(index);
        return (org.mars_sim.msp.config.model.building.ResourceInitial) obj;
    }

    /**
     * Method removeResourceStorage.
     * 
     * @param vResourceStorage
     * @return true if the object was removed from the collection.
     */
    public boolean removeResourceStorage(
            final org.mars_sim.msp.config.model.building.ResourceStorage vResourceStorage) {
        boolean removed = _resourceStorageList.remove(vResourceStorage);
        return removed;
    }

    /**
     * Method removeResourceStorageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.building.ResourceStorage removeResourceStorageAt(
            final int index) {
        java.lang.Object obj = this._resourceStorageList.remove(index);
        return (org.mars_sim.msp.config.model.building.ResourceStorage) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vResourceInitial
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setResourceInitial(
            final int index,
            final org.mars_sim.msp.config.model.building.ResourceInitial vResourceInitial)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._resourceInitialList.size()) {
            throw new IndexOutOfBoundsException("setResourceInitial: Index value '" + index + "' not in range [0.." + (this._resourceInitialList.size() - 1) + "]");
        }

        this._resourceInitialList.set(index, vResourceInitial);
    }

    /**
     * 
     * 
     * @param vResourceInitialArray
     */
    public void setResourceInitial(
            final org.mars_sim.msp.config.model.building.ResourceInitial[] vResourceInitialArray) {
        //-- copy array
        _resourceInitialList.clear();

        for (int i = 0; i < vResourceInitialArray.length; i++) {
                this._resourceInitialList.add(vResourceInitialArray[i]);
        }
    }

    /**
     * 
     * 
     * @param index
     * @param vResourceStorage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setResourceStorage(
            final int index,
            final org.mars_sim.msp.config.model.building.ResourceStorage vResourceStorage)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._resourceStorageList.size()) {
            throw new IndexOutOfBoundsException("setResourceStorage: Index value '" + index + "' not in range [0.." + (this._resourceStorageList.size() - 1) + "]");
        }

        this._resourceStorageList.set(index, vResourceStorage);
    }

    /**
     * 
     * 
     * @param vResourceStorageArray
     */
    public void setResourceStorage(
            final org.mars_sim.msp.config.model.building.ResourceStorage[] vResourceStorageArray) {
        //-- copy array
        _resourceStorageList.clear();

        for (int i = 0; i < vResourceStorageArray.length; i++) {
                this._resourceStorageList.add(vResourceStorageArray[i]);
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
     * org.mars_sim.msp.config.model.building.Storage
     */
    public static org.mars_sim.msp.config.model.building.Storage unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.building.Storage) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.building.Storage.class, reader);
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
