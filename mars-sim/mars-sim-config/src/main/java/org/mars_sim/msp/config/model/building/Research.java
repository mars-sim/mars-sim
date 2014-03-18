/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
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
     * Field _researchSpecialtyList.
     */
    private java.util.List<org.mars_sim.msp.config.model.building.ResearchSpecialty> _researchSpecialtyList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Research() {
        super();
        this._researchSpecialtyList = new java.util.ArrayList<org.mars_sim.msp.config.model.building.ResearchSpecialty>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vResearchSpecialty
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResearchSpecialty(
            final org.mars_sim.msp.config.model.building.ResearchSpecialty vResearchSpecialty)
    throws java.lang.IndexOutOfBoundsException {
        this._researchSpecialtyList.add(vResearchSpecialty);
    }

    /**
     * 
     * 
     * @param index
     * @param vResearchSpecialty
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResearchSpecialty(
            final int index,
            final org.mars_sim.msp.config.model.building.ResearchSpecialty vResearchSpecialty)
    throws java.lang.IndexOutOfBoundsException {
        this._researchSpecialtyList.add(index, vResearchSpecialty);
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
     * Method enumerateResearchSpecialty.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.building.ResearchSpecialty> enumerateResearchSpecialty(
    ) {
        return java.util.Collections.enumeration(this._researchSpecialtyList);
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
     * Method getResearchSpecialty.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.building.ResearchSpecialty at
     * the given index
     */
    public org.mars_sim.msp.config.model.building.ResearchSpecialty getResearchSpecialty(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._researchSpecialtyList.size()) {
            throw new IndexOutOfBoundsException("getResearchSpecialty: Index value '" + index + "' not in range [0.." + (this._researchSpecialtyList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.building.ResearchSpecialty) _researchSpecialtyList.get(index);
    }

    /**
     * Method getResearchSpecialty.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.building.ResearchSpecialty[] getResearchSpecialty(
    ) {
        org.mars_sim.msp.config.model.building.ResearchSpecialty[] array = new org.mars_sim.msp.config.model.building.ResearchSpecialty[0];
        return (org.mars_sim.msp.config.model.building.ResearchSpecialty[]) this._researchSpecialtyList.toArray(array);
    }

    /**
     * Method getResearchSpecialtyCount.
     * 
     * @return the size of this collection
     */
    public int getResearchSpecialtyCount(
    ) {
        return this._researchSpecialtyList.size();
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
     * Method iterateResearchSpecialty.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.building.ResearchSpecialty> iterateResearchSpecialty(
    ) {
        return this._researchSpecialtyList.iterator();
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
    public void removeAllResearchSpecialty(
    ) {
        this._researchSpecialtyList.clear();
    }

    /**
     * Method removeResearchSpecialty.
     * 
     * @param vResearchSpecialty
     * @return true if the object was removed from the collection.
     */
    public boolean removeResearchSpecialty(
            final org.mars_sim.msp.config.model.building.ResearchSpecialty vResearchSpecialty) {
        boolean removed = _researchSpecialtyList.remove(vResearchSpecialty);
        return removed;
    }

    /**
     * Method removeResearchSpecialtyAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.building.ResearchSpecialty removeResearchSpecialtyAt(
            final int index) {
        java.lang.Object obj = this._researchSpecialtyList.remove(index);
        return (org.mars_sim.msp.config.model.building.ResearchSpecialty) obj;
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
     * @param vResearchSpecialty
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setResearchSpecialty(
            final int index,
            final org.mars_sim.msp.config.model.building.ResearchSpecialty vResearchSpecialty)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._researchSpecialtyList.size()) {
            throw new IndexOutOfBoundsException("setResearchSpecialty: Index value '" + index + "' not in range [0.." + (this._researchSpecialtyList.size() - 1) + "]");
        }

        this._researchSpecialtyList.set(index, vResearchSpecialty);
    }

    /**
     * 
     * 
     * @param vResearchSpecialtyArray
     */
    public void setResearchSpecialty(
            final org.mars_sim.msp.config.model.building.ResearchSpecialty[] vResearchSpecialtyArray) {
        //-- copy array
        _researchSpecialtyList.clear();

        for (int i = 0; i < vResearchSpecialtyArray.length; i++) {
                this._researchSpecialtyList.add(vResearchSpecialtyArray[i]);
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
