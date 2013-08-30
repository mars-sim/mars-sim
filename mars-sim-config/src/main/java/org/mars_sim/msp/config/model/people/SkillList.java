/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.people;

/**
 * Class SkillList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class SkillList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _skillList.
     */
    private java.util.List<org.mars_sim.msp.config.model.people.Skill> _skillList;


      //----------------/
     //- Constructors -/
    //----------------/

    public SkillList() {
        super();
        this._skillList = new java.util.ArrayList<org.mars_sim.msp.config.model.people.Skill>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vSkill
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSkill(
            final org.mars_sim.msp.config.model.people.Skill vSkill)
    throws java.lang.IndexOutOfBoundsException {
        this._skillList.add(vSkill);
    }

    /**
     * 
     * 
     * @param index
     * @param vSkill
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSkill(
            final int index,
            final org.mars_sim.msp.config.model.people.Skill vSkill)
    throws java.lang.IndexOutOfBoundsException {
        this._skillList.add(index, vSkill);
    }

    /**
     * Method enumerateSkill.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.people.Skill> enumerateSkill(
    ) {
        return java.util.Collections.enumeration(this._skillList);
    }

    /**
     * Method getSkill.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.people.Skill at the given index
     */
    public org.mars_sim.msp.config.model.people.Skill getSkill(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._skillList.size()) {
            throw new IndexOutOfBoundsException("getSkill: Index value '" + index + "' not in range [0.." + (this._skillList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.people.Skill) _skillList.get(index);
    }

    /**
     * Method getSkill.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.people.Skill[] getSkill(
    ) {
        org.mars_sim.msp.config.model.people.Skill[] array = new org.mars_sim.msp.config.model.people.Skill[0];
        return (org.mars_sim.msp.config.model.people.Skill[]) this._skillList.toArray(array);
    }

    /**
     * Method getSkillCount.
     * 
     * @return the size of this collection
     */
    public int getSkillCount(
    ) {
        return this._skillList.size();
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
     * Method iterateSkill.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.people.Skill> iterateSkill(
    ) {
        return this._skillList.iterator();
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
    public void removeAllSkill(
    ) {
        this._skillList.clear();
    }

    /**
     * Method removeSkill.
     * 
     * @param vSkill
     * @return true if the object was removed from the collection.
     */
    public boolean removeSkill(
            final org.mars_sim.msp.config.model.people.Skill vSkill) {
        boolean removed = _skillList.remove(vSkill);
        return removed;
    }

    /**
     * Method removeSkillAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.people.Skill removeSkillAt(
            final int index) {
        java.lang.Object obj = this._skillList.remove(index);
        return (org.mars_sim.msp.config.model.people.Skill) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vSkill
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setSkill(
            final int index,
            final org.mars_sim.msp.config.model.people.Skill vSkill)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._skillList.size()) {
            throw new IndexOutOfBoundsException("setSkill: Index value '" + index + "' not in range [0.." + (this._skillList.size() - 1) + "]");
        }

        this._skillList.set(index, vSkill);
    }

    /**
     * 
     * 
     * @param vSkillArray
     */
    public void setSkill(
            final org.mars_sim.msp.config.model.people.Skill[] vSkillArray) {
        //-- copy array
        _skillList.clear();

        for (int i = 0; i < vSkillArray.length; i++) {
                this._skillList.add(vSkillArray[i]);
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
     * org.mars_sim.msp.config.model.people.SkillList
     */
    public static org.mars_sim.msp.config.model.people.SkillList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.people.SkillList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.people.SkillList.class, reader);
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
