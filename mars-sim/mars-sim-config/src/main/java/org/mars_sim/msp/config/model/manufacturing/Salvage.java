/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.manufacturing;

/**
 * Class Salvage.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Salvage implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _itemName.
     */
    private java.lang.String _itemName;

    /**
     * Field _type.
     */
    private java.lang.String _type;

    /**
     * Field _tech.
     */
    private long _tech;

    /**
     * keeps track of state for field: _tech
     */
    private boolean _has_tech;

    /**
     * Field _skill.
     */
    private long _skill;

    /**
     * keeps track of state for field: _skill
     */
    private boolean _has_skill;

    /**
     * Field _workTime.
     */
    private long _workTime;

    /**
     * keeps track of state for field: _workTime
     */
    private boolean _has_workTime;

    /**
     * Field _partSalvageList.
     */
    private java.util.List<org.mars_sim.msp.config.model.manufacturing.PartSalvage> _partSalvageList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Salvage() {
        super();
        this._partSalvageList = new java.util.ArrayList<org.mars_sim.msp.config.model.manufacturing.PartSalvage>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vPartSalvage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPartSalvage(
            final org.mars_sim.msp.config.model.manufacturing.PartSalvage vPartSalvage)
    throws java.lang.IndexOutOfBoundsException {
        this._partSalvageList.add(vPartSalvage);
    }

    /**
     * 
     * 
     * @param index
     * @param vPartSalvage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPartSalvage(
            final int index,
            final org.mars_sim.msp.config.model.manufacturing.PartSalvage vPartSalvage)
    throws java.lang.IndexOutOfBoundsException {
        this._partSalvageList.add(index, vPartSalvage);
    }

    /**
     */
    public void deleteSkill(
    ) {
        this._has_skill= false;
    }

    /**
     */
    public void deleteTech(
    ) {
        this._has_tech= false;
    }

    /**
     */
    public void deleteWorkTime(
    ) {
        this._has_workTime= false;
    }

    /**
     * Method enumeratePartSalvage.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.manufacturing.PartSalvage> enumeratePartSalvage(
    ) {
        return java.util.Collections.enumeration(this._partSalvageList);
    }

    /**
     * Returns the value of field 'itemName'.
     * 
     * @return the value of field 'ItemName'.
     */
    public java.lang.String getItemName(
    ) {
        return this._itemName;
    }

    /**
     * Method getPartSalvage.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.manufacturing.PartSalvage at
     * the given index
     */
    public org.mars_sim.msp.config.model.manufacturing.PartSalvage getPartSalvage(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._partSalvageList.size()) {
            throw new IndexOutOfBoundsException("getPartSalvage: Index value '" + index + "' not in range [0.." + (this._partSalvageList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.manufacturing.PartSalvage) _partSalvageList.get(index);
    }

    /**
     * Method getPartSalvage.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.manufacturing.PartSalvage[] getPartSalvage(
    ) {
        org.mars_sim.msp.config.model.manufacturing.PartSalvage[] array = new org.mars_sim.msp.config.model.manufacturing.PartSalvage[0];
        return (org.mars_sim.msp.config.model.manufacturing.PartSalvage[]) this._partSalvageList.toArray(array);
    }

    /**
     * Method getPartSalvageCount.
     * 
     * @return the size of this collection
     */
    public int getPartSalvageCount(
    ) {
        return this._partSalvageList.size();
    }

    /**
     * Returns the value of field 'skill'.
     * 
     * @return the value of field 'Skill'.
     */
    public long getSkill(
    ) {
        return this._skill;
    }

    /**
     * Returns the value of field 'tech'.
     * 
     * @return the value of field 'Tech'.
     */
    public long getTech(
    ) {
        return this._tech;
    }

    /**
     * Returns the value of field 'type'.
     * 
     * @return the value of field 'Type'.
     */
    public java.lang.String getType(
    ) {
        return this._type;
    }

    /**
     * Returns the value of field 'workTime'.
     * 
     * @return the value of field 'WorkTime'.
     */
    public long getWorkTime(
    ) {
        return this._workTime;
    }

    /**
     * Method hasSkill.
     * 
     * @return true if at least one Skill has been added
     */
    public boolean hasSkill(
    ) {
        return this._has_skill;
    }

    /**
     * Method hasTech.
     * 
     * @return true if at least one Tech has been added
     */
    public boolean hasTech(
    ) {
        return this._has_tech;
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
     * Method iteratePartSalvage.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.manufacturing.PartSalvage> iteratePartSalvage(
    ) {
        return this._partSalvageList.iterator();
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
    public void removeAllPartSalvage(
    ) {
        this._partSalvageList.clear();
    }

    /**
     * Method removePartSalvage.
     * 
     * @param vPartSalvage
     * @return true if the object was removed from the collection.
     */
    public boolean removePartSalvage(
            final org.mars_sim.msp.config.model.manufacturing.PartSalvage vPartSalvage) {
        boolean removed = _partSalvageList.remove(vPartSalvage);
        return removed;
    }

    /**
     * Method removePartSalvageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.manufacturing.PartSalvage removePartSalvageAt(
            final int index) {
        java.lang.Object obj = this._partSalvageList.remove(index);
        return (org.mars_sim.msp.config.model.manufacturing.PartSalvage) obj;
    }

    /**
     * Sets the value of field 'itemName'.
     * 
     * @param itemName the value of field 'itemName'.
     */
    public void setItemName(
            final java.lang.String itemName) {
        this._itemName = itemName;
    }

    /**
     * 
     * 
     * @param index
     * @param vPartSalvage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setPartSalvage(
            final int index,
            final org.mars_sim.msp.config.model.manufacturing.PartSalvage vPartSalvage)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._partSalvageList.size()) {
            throw new IndexOutOfBoundsException("setPartSalvage: Index value '" + index + "' not in range [0.." + (this._partSalvageList.size() - 1) + "]");
        }

        this._partSalvageList.set(index, vPartSalvage);
    }

    /**
     * 
     * 
     * @param vPartSalvageArray
     */
    public void setPartSalvage(
            final org.mars_sim.msp.config.model.manufacturing.PartSalvage[] vPartSalvageArray) {
        //-- copy array
        _partSalvageList.clear();

        for (int i = 0; i < vPartSalvageArray.length; i++) {
                this._partSalvageList.add(vPartSalvageArray[i]);
        }
    }

    /**
     * Sets the value of field 'skill'.
     * 
     * @param skill the value of field 'skill'.
     */
    public void setSkill(
            final long skill) {
        this._skill = skill;
        this._has_skill = true;
    }

    /**
     * Sets the value of field 'tech'.
     * 
     * @param tech the value of field 'tech'.
     */
    public void setTech(
            final long tech) {
        this._tech = tech;
        this._has_tech = true;
    }

    /**
     * Sets the value of field 'type'.
     * 
     * @param type the value of field 'type'.
     */
    public void setType(
            final java.lang.String type) {
        this._type = type;
    }

    /**
     * Sets the value of field 'workTime'.
     * 
     * @param workTime the value of field 'workTime'.
     */
    public void setWorkTime(
            final long workTime) {
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
     * org.mars_sim.msp.config.model.manufacturing.Salvage
     */
    public static org.mars_sim.msp.config.model.manufacturing.Salvage unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.manufacturing.Salvage) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.manufacturing.Salvage.class, reader);
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
