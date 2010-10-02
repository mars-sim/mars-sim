/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.building;

/**
 * Class Research.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Research implements java.io.Serializable {


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
     * Field _capacity.
     */
    private long _capacity;

    /**
     * keeps track of state for field: _capacity
     */
    private boolean _has_capacity;

    /**
     * Field _researchSpecialityList.
     */
    private java.util.List<org.mars_sim.msp.config.model.building.ResearchSpeciality> _researchSpecialityList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Research() {
        super();
        this._researchSpecialityList = new java.util.ArrayList<org.mars_sim.msp.config.model.building.ResearchSpeciality>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vResearchSpeciality
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResearchSpeciality(
            final org.mars_sim.msp.config.model.building.ResearchSpeciality vResearchSpeciality)
    throws java.lang.IndexOutOfBoundsException {
        this._researchSpecialityList.add(vResearchSpeciality);
    }

    /**
     * 
     * 
     * @param index
     * @param vResearchSpeciality
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResearchSpeciality(
            final int index,
            final org.mars_sim.msp.config.model.building.ResearchSpeciality vResearchSpeciality)
    throws java.lang.IndexOutOfBoundsException {
        this._researchSpecialityList.add(index, vResearchSpeciality);
    }

    /**
     */
    public void deleteCapacity(
    ) {
        this._has_capacity= false;
    }

    /**
     */
    public void deleteTechLevel(
    ) {
        this._has_techLevel= false;
    }

    /**
     * Method enumerateResearchSpeciality.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.building.ResearchSpeciality> enumerateResearchSpeciality(
    ) {
        return java.util.Collections.enumeration(this._researchSpecialityList);
    }

    /**
     * Returns the value of field 'capacity'.
     * 
     * @return the value of field 'Capacity'.
     */
    public long getCapacity(
    ) {
        return this._capacity;
    }

    /**
     * Method getResearchSpeciality.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.building.ResearchSpeciality at
     * the given index
     */
    public org.mars_sim.msp.config.model.building.ResearchSpeciality getResearchSpeciality(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._researchSpecialityList.size()) {
            throw new IndexOutOfBoundsException("getResearchSpeciality: Index value '" + index + "' not in range [0.." + (this._researchSpecialityList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.building.ResearchSpeciality) _researchSpecialityList.get(index);
    }

    /**
     * Method getResearchSpeciality.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.building.ResearchSpeciality[] getResearchSpeciality(
    ) {
        org.mars_sim.msp.config.model.building.ResearchSpeciality[] array = new org.mars_sim.msp.config.model.building.ResearchSpeciality[0];
        return (org.mars_sim.msp.config.model.building.ResearchSpeciality[]) this._researchSpecialityList.toArray(array);
    }

    /**
     * Method getResearchSpecialityCount.
     * 
     * @return the size of this collection
     */
    public int getResearchSpecialityCount(
    ) {
        return this._researchSpecialityList.size();
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
     * Method hasCapacity.
     * 
     * @return true if at least one Capacity has been added
     */
    public boolean hasCapacity(
    ) {
        return this._has_capacity;
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
     * Method iterateResearchSpeciality.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.building.ResearchSpeciality> iterateResearchSpeciality(
    ) {
        return this._researchSpecialityList.iterator();
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
    public void removeAllResearchSpeciality(
    ) {
        this._researchSpecialityList.clear();
    }

    /**
     * Method removeResearchSpeciality.
     * 
     * @param vResearchSpeciality
     * @return true if the object was removed from the collection.
     */
    public boolean removeResearchSpeciality(
            final org.mars_sim.msp.config.model.building.ResearchSpeciality vResearchSpeciality) {
        boolean removed = _researchSpecialityList.remove(vResearchSpeciality);
        return removed;
    }

    /**
     * Method removeResearchSpecialityAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.building.ResearchSpeciality removeResearchSpecialityAt(
            final int index) {
        java.lang.Object obj = this._researchSpecialityList.remove(index);
        return (org.mars_sim.msp.config.model.building.ResearchSpeciality) obj;
    }

    /**
     * Sets the value of field 'capacity'.
     * 
     * @param capacity the value of field 'capacity'.
     */
    public void setCapacity(
            final long capacity) {
        this._capacity = capacity;
        this._has_capacity = true;
    }

    /**
     * 
     * 
     * @param index
     * @param vResearchSpeciality
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setResearchSpeciality(
            final int index,
            final org.mars_sim.msp.config.model.building.ResearchSpeciality vResearchSpeciality)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._researchSpecialityList.size()) {
            throw new IndexOutOfBoundsException("setResearchSpeciality: Index value '" + index + "' not in range [0.." + (this._researchSpecialityList.size() - 1) + "]");
        }

        this._researchSpecialityList.set(index, vResearchSpeciality);
    }

    /**
     * 
     * 
     * @param vResearchSpecialityArray
     */
    public void setResearchSpeciality(
            final org.mars_sim.msp.config.model.building.ResearchSpeciality[] vResearchSpecialityArray) {
        //-- copy array
        _researchSpecialityList.clear();

        for (int i = 0; i < vResearchSpecialityArray.length; i++) {
                this._researchSpecialityList.add(vResearchSpecialityArray[i]);
        }
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
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.mars_sim.msp.config.model.building.Research
     */
    public static org.mars_sim.msp.config.model.building.Research unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.building.Research) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.building.Research.class, reader);
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
