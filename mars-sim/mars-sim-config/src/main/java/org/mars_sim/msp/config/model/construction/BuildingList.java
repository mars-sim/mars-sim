/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.construction;

/**
 * Class BuildingList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class BuildingList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _buildingList.
     */
    private java.util.List<org.mars_sim.msp.config.model.construction.Building> _buildingList;


      //----------------/
     //- Constructors -/
    //----------------/

    public BuildingList() {
        super();
        this._buildingList = new java.util.ArrayList<org.mars_sim.msp.config.model.construction.Building>();
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
            final org.mars_sim.msp.config.model.construction.Building vBuilding)
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
            final org.mars_sim.msp.config.model.construction.Building vBuilding)
    throws java.lang.IndexOutOfBoundsException {
        this._buildingList.add(index, vBuilding);
    }

    /**
     * Method enumerateBuilding.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.construction.Building> enumerateBuilding(
    ) {
        return java.util.Collections.enumeration(this._buildingList);
    }

    /**
     * Method getBuilding.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.construction.Building at the
     * given index
     */
    public org.mars_sim.msp.config.model.construction.Building getBuilding(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._buildingList.size()) {
            throw new IndexOutOfBoundsException("getBuilding: Index value '" + index + "' not in range [0.." + (this._buildingList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.construction.Building) _buildingList.get(index);
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
    public org.mars_sim.msp.config.model.construction.Building[] getBuilding(
    ) {
        org.mars_sim.msp.config.model.construction.Building[] array = new org.mars_sim.msp.config.model.construction.Building[0];
        return (org.mars_sim.msp.config.model.construction.Building[]) this._buildingList.toArray(array);
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
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.construction.Building> iterateBuilding(
    ) {
        return this._buildingList.iterator();
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
     * Method removeBuilding.
     * 
     * @param vBuilding
     * @return true if the object was removed from the collection.
     */
    public boolean removeBuilding(
            final org.mars_sim.msp.config.model.construction.Building vBuilding) {
        boolean removed = _buildingList.remove(vBuilding);
        return removed;
    }

    /**
     * Method removeBuildingAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.construction.Building removeBuildingAt(
            final int index) {
        java.lang.Object obj = this._buildingList.remove(index);
        return (org.mars_sim.msp.config.model.construction.Building) obj;
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
            final org.mars_sim.msp.config.model.construction.Building vBuilding)
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
            final org.mars_sim.msp.config.model.construction.Building[] vBuildingArray) {
        //-- copy array
        _buildingList.clear();

        for (int i = 0; i < vBuildingArray.length; i++) {
                this._buildingList.add(vBuildingArray[i]);
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
     * org.mars_sim.msp.config.model.construction.BuildingList
     */
    public static org.mars_sim.msp.config.model.construction.BuildingList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.construction.BuildingList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.construction.BuildingList.class, reader);
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
