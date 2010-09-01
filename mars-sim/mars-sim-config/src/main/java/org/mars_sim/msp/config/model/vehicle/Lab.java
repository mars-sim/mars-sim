/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.vehicle;

/**
 * Class Lab.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Lab implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _techLevel.
     */
    private long _techLevel;

    /**
     * keeps track of state for field: _techLevel
     */
    private boolean _has_techLevel;

    /**
     * Field _techSpecialityList.
     */
    private java.util.List<org.mars_sim.msp.config.model.vehicle.TechSpeciality> _techSpecialityList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Lab() {
        super();
        this._techSpecialityList = new java.util.ArrayList<org.mars_sim.msp.config.model.vehicle.TechSpeciality>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vTechSpeciality
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addTechSpeciality(
            final org.mars_sim.msp.config.model.vehicle.TechSpeciality vTechSpeciality)
    throws java.lang.IndexOutOfBoundsException {
        this._techSpecialityList.add(vTechSpeciality);
    }

    /**
     * 
     * 
     * @param index
     * @param vTechSpeciality
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addTechSpeciality(
            final int index,
            final org.mars_sim.msp.config.model.vehicle.TechSpeciality vTechSpeciality)
    throws java.lang.IndexOutOfBoundsException {
        this._techSpecialityList.add(index, vTechSpeciality);
    }

    /**
     */
    public void deleteTechLevel(
    ) {
        this._has_techLevel= false;
    }

    /**
     * Method enumerateTechSpeciality.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.vehicle.TechSpeciality> enumerateTechSpeciality(
    ) {
        return java.util.Collections.enumeration(this._techSpecialityList);
    }

    /**
     * Returns the value of field 'techLevel'.
     * 
     * @return the value of field 'TechLevel'.
     */
    public long getTechLevel(
    ) {
        return this._techLevel;
    }

    /**
     * Method getTechSpeciality.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.vehicle.TechSpeciality at the
     * given index
     */
    public org.mars_sim.msp.config.model.vehicle.TechSpeciality getTechSpeciality(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._techSpecialityList.size()) {
            throw new IndexOutOfBoundsException("getTechSpeciality: Index value '" + index + "' not in range [0.." + (this._techSpecialityList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.vehicle.TechSpeciality) _techSpecialityList.get(index);
    }

    /**
     * Method getTechSpeciality.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.vehicle.TechSpeciality[] getTechSpeciality(
    ) {
        org.mars_sim.msp.config.model.vehicle.TechSpeciality[] array = new org.mars_sim.msp.config.model.vehicle.TechSpeciality[0];
        return (org.mars_sim.msp.config.model.vehicle.TechSpeciality[]) this._techSpecialityList.toArray(array);
    }

    /**
     * Method getTechSpecialityCount.
     * 
     * @return the size of this collection
     */
    public int getTechSpecialityCount(
    ) {
        return this._techSpecialityList.size();
    }

    /**
     * Method hasTechLevel.
     * 
     * @return true if at least one TechLevel has been added
     */
    public boolean hasTechLevel(
    ) {
        return this._has_techLevel;
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
     * Method iterateTechSpeciality.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.vehicle.TechSpeciality> iterateTechSpeciality(
    ) {
        return this._techSpecialityList.iterator();
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
    public void removeAllTechSpeciality(
    ) {
        this._techSpecialityList.clear();
    }

    /**
     * Method removeTechSpeciality.
     * 
     * @param vTechSpeciality
     * @return true if the object was removed from the collection.
     */
    public boolean removeTechSpeciality(
            final org.mars_sim.msp.config.model.vehicle.TechSpeciality vTechSpeciality) {
        boolean removed = _techSpecialityList.remove(vTechSpeciality);
        return removed;
    }

    /**
     * Method removeTechSpecialityAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.vehicle.TechSpeciality removeTechSpecialityAt(
            final int index) {
        java.lang.Object obj = this._techSpecialityList.remove(index);
        return (org.mars_sim.msp.config.model.vehicle.TechSpeciality) obj;
    }

    /**
     * Sets the value of field 'techLevel'.
     * 
     * @param techLevel the value of field 'techLevel'.
     */
    public void setTechLevel(
            final long techLevel) {
        this._techLevel = techLevel;
        this._has_techLevel = true;
    }

    /**
     * 
     * 
     * @param index
     * @param vTechSpeciality
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setTechSpeciality(
            final int index,
            final org.mars_sim.msp.config.model.vehicle.TechSpeciality vTechSpeciality)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._techSpecialityList.size()) {
            throw new IndexOutOfBoundsException("setTechSpeciality: Index value '" + index + "' not in range [0.." + (this._techSpecialityList.size() - 1) + "]");
        }

        this._techSpecialityList.set(index, vTechSpeciality);
    }

    /**
     * 
     * 
     * @param vTechSpecialityArray
     */
    public void setTechSpeciality(
            final org.mars_sim.msp.config.model.vehicle.TechSpeciality[] vTechSpecialityArray) {
        //-- copy array
        _techSpecialityList.clear();

        for (int i = 0; i < vTechSpecialityArray.length; i++) {
                this._techSpecialityList.add(vTechSpecialityArray[i]);
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
     * org.mars_sim.msp.config.model.vehicle.Lab
     */
    public static org.mars_sim.msp.config.model.vehicle.Lab unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.vehicle.Lab) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.vehicle.Lab.class, reader);
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
