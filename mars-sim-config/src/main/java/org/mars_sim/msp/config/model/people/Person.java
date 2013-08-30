/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.people;

/**
 * Class Person.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Person implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _gender.
     */
    private org.mars_sim.msp.config.model.configuration.types.Gender _gender;

    /**
     * Field _personalityType.
     */
    private java.lang.String _personalityType;

    /**
     * Field _settlement.
     */
    private java.lang.String _settlement;

    /**
     * Field _job.
     */
    private java.lang.String _job;

    /**
     * Field _naturalAttributeList.
     */
    private org.mars_sim.msp.config.model.people.NaturalAttributeList _naturalAttributeList;

    /**
     * Field _skillList.
     */
    private org.mars_sim.msp.config.model.people.SkillList _skillList;

    /**
     * Field _relationshipList.
     */
    private org.mars_sim.msp.config.model.people.RelationshipList _relationshipList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Person() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'gender'.
     * 
     * @return the value of field 'Gender'.
     */
    public org.mars_sim.msp.config.model.configuration.types.Gender getGender(
    ) {
        return this._gender;
    }

    /**
     * Returns the value of field 'job'.
     * 
     * @return the value of field 'Job'.
     */
    public java.lang.String getJob(
    ) {
        return this._job;
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
     * Returns the value of field 'naturalAttributeList'.
     * 
     * @return the value of field 'NaturalAttributeList'.
     */
    public org.mars_sim.msp.config.model.people.NaturalAttributeList getNaturalAttributeList(
    ) {
        return this._naturalAttributeList;
    }

    /**
     * Returns the value of field 'personalityType'.
     * 
     * @return the value of field 'PersonalityType'.
     */
    public java.lang.String getPersonalityType(
    ) {
        return this._personalityType;
    }

    /**
     * Returns the value of field 'relationshipList'.
     * 
     * @return the value of field 'RelationshipList'.
     */
    public org.mars_sim.msp.config.model.people.RelationshipList getRelationshipList(
    ) {
        return this._relationshipList;
    }

    /**
     * Returns the value of field 'settlement'.
     * 
     * @return the value of field 'Settlement'.
     */
    public java.lang.String getSettlement(
    ) {
        return this._settlement;
    }

    /**
     * Returns the value of field 'skillList'.
     * 
     * @return the value of field 'SkillList'.
     */
    public org.mars_sim.msp.config.model.people.SkillList getSkillList(
    ) {
        return this._skillList;
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
     * Sets the value of field 'gender'.
     * 
     * @param gender the value of field 'gender'.
     */
    public void setGender(
            final org.mars_sim.msp.config.model.configuration.types.Gender gender) {
        this._gender = gender;
    }

    /**
     * Sets the value of field 'job'.
     * 
     * @param job the value of field 'job'.
     */
    public void setJob(
            final java.lang.String job) {
        this._job = job;
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
     * Sets the value of field 'naturalAttributeList'.
     * 
     * @param naturalAttributeList the value of field
     * 'naturalAttributeList'.
     */
    public void setNaturalAttributeList(
            final org.mars_sim.msp.config.model.people.NaturalAttributeList naturalAttributeList) {
        this._naturalAttributeList = naturalAttributeList;
    }

    /**
     * Sets the value of field 'personalityType'.
     * 
     * @param personalityType the value of field 'personalityType'.
     */
    public void setPersonalityType(
            final java.lang.String personalityType) {
        this._personalityType = personalityType;
    }

    /**
     * Sets the value of field 'relationshipList'.
     * 
     * @param relationshipList the value of field 'relationshipList'
     */
    public void setRelationshipList(
            final org.mars_sim.msp.config.model.people.RelationshipList relationshipList) {
        this._relationshipList = relationshipList;
    }

    /**
     * Sets the value of field 'settlement'.
     * 
     * @param settlement the value of field 'settlement'.
     */
    public void setSettlement(
            final java.lang.String settlement) {
        this._settlement = settlement;
    }

    /**
     * Sets the value of field 'skillList'.
     * 
     * @param skillList the value of field 'skillList'.
     */
    public void setSkillList(
            final org.mars_sim.msp.config.model.people.SkillList skillList) {
        this._skillList = skillList;
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
     * org.mars_sim.msp.config.model.people.Person
     */
    public static org.mars_sim.msp.config.model.people.Person unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.people.Person) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.people.Person.class, reader);
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
