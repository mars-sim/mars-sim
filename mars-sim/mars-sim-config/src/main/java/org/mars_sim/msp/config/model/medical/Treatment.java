/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.medical;

/**
 * Class Treatment.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Treatment implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _skill.
     */
    private org.mars_sim.msp.config.model.medical.Skill _skill;

    /**
     * Field _medicalTechLevel.
     */
    private org.mars_sim.msp.config.model.medical.MedicalTechLevel _medicalTechLevel;

    /**
     * Field _treatmentTime.
     */
    private org.mars_sim.msp.config.model.medical.TreatmentTime _treatmentTime;

    /**
     * Field _retainaid.
     */
    private org.mars_sim.msp.config.model.medical.Retainaid _retainaid;


      //----------------/
     //- Constructors -/
    //----------------/

    public Treatment() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'medicalTechLevel'.
     * 
     * @return the value of field 'MedicalTechLevel'.
     */
    public org.mars_sim.msp.config.model.medical.MedicalTechLevel getMedicalTechLevel(
    ) {
        return this._medicalTechLevel;
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
     * Returns the value of field 'retainaid'.
     * 
     * @return the value of field 'Retainaid'.
     */
    public org.mars_sim.msp.config.model.medical.Retainaid getRetainaid(
    ) {
        return this._retainaid;
    }

    /**
     * Returns the value of field 'skill'.
     * 
     * @return the value of field 'Skill'.
     */
    public org.mars_sim.msp.config.model.medical.Skill getSkill(
    ) {
        return this._skill;
    }

    /**
     * Returns the value of field 'treatmentTime'.
     * 
     * @return the value of field 'TreatmentTime'.
     */
    public org.mars_sim.msp.config.model.medical.TreatmentTime getTreatmentTime(
    ) {
        return this._treatmentTime;
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
     * Sets the value of field 'medicalTechLevel'.
     * 
     * @param medicalTechLevel the value of field 'medicalTechLevel'
     */
    public void setMedicalTechLevel(
            final org.mars_sim.msp.config.model.medical.MedicalTechLevel medicalTechLevel) {
        this._medicalTechLevel = medicalTechLevel;
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
     * Sets the value of field 'retainaid'.
     * 
     * @param retainaid the value of field 'retainaid'.
     */
    public void setRetainaid(
            final org.mars_sim.msp.config.model.medical.Retainaid retainaid) {
        this._retainaid = retainaid;
    }

    /**
     * Sets the value of field 'skill'.
     * 
     * @param skill the value of field 'skill'.
     */
    public void setSkill(
            final org.mars_sim.msp.config.model.medical.Skill skill) {
        this._skill = skill;
    }

    /**
     * Sets the value of field 'treatmentTime'.
     * 
     * @param treatmentTime the value of field 'treatmentTime'.
     */
    public void setTreatmentTime(
            final org.mars_sim.msp.config.model.medical.TreatmentTime treatmentTime) {
        this._treatmentTime = treatmentTime;
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
     * org.mars_sim.msp.config.model.medical.Treatment
     */
    public static org.mars_sim.msp.config.model.medical.Treatment unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.medical.Treatment) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.medical.Treatment.class, reader);
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
