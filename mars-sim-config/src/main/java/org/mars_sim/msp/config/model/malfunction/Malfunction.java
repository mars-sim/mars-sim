/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.malfunction;

/**
 * Class Malfunction.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Malfunction implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _severity.
     */
    private org.mars_sim.msp.config.model.malfunction.Severity _severity;

    /**
     * Field _probability.
     */
    private org.mars_sim.msp.config.model.malfunction.Probability _probability;

    /**
     * Field _repairTime.
     */
    private org.mars_sim.msp.config.model.malfunction.RepairTime _repairTime;

    /**
     * Field _emergencyRepairTime.
     */
    private org.mars_sim.msp.config.model.malfunction.EmergencyRepairTime _emergencyRepairTime;

    /**
     * Field _evaRepairTime.
     */
    private org.mars_sim.msp.config.model.malfunction.EvaRepairTime _evaRepairTime;

    /**
     * Field _entityList.
     */
    private org.mars_sim.msp.config.model.malfunction.EntityList _entityList;

    /**
     * Field _effectList.
     */
    private org.mars_sim.msp.config.model.malfunction.EffectList _effectList;

    /**
     * Field _medicalComplaintList.
     */
    private org.mars_sim.msp.config.model.malfunction.MedicalComplaintList _medicalComplaintList;

    /**
     * Field _repairPartsList.
     */
    private org.mars_sim.msp.config.model.malfunction.RepairPartsList _repairPartsList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Malfunction() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'effectList'.
     * 
     * @return the value of field 'EffectList'.
     */
    public org.mars_sim.msp.config.model.malfunction.EffectList getEffectList(
    ) {
        return this._effectList;
    }

    /**
     * Returns the value of field 'emergencyRepairTime'.
     * 
     * @return the value of field 'EmergencyRepairTime'.
     */
    public org.mars_sim.msp.config.model.malfunction.EmergencyRepairTime getEmergencyRepairTime(
    ) {
        return this._emergencyRepairTime;
    }

    /**
     * Returns the value of field 'entityList'.
     * 
     * @return the value of field 'EntityList'.
     */
    public org.mars_sim.msp.config.model.malfunction.EntityList getEntityList(
    ) {
        return this._entityList;
    }

    /**
     * Returns the value of field 'evaRepairTime'.
     * 
     * @return the value of field 'EvaRepairTime'.
     */
    public org.mars_sim.msp.config.model.malfunction.EvaRepairTime getEvaRepairTime(
    ) {
        return this._evaRepairTime;
    }

    /**
     * Returns the value of field 'medicalComplaintList'.
     * 
     * @return the value of field 'MedicalComplaintList'.
     */
    public org.mars_sim.msp.config.model.malfunction.MedicalComplaintList getMedicalComplaintList(
    ) {
        return this._medicalComplaintList;
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
     * Returns the value of field 'probability'.
     * 
     * @return the value of field 'Probability'.
     */
    public org.mars_sim.msp.config.model.malfunction.Probability getProbability(
    ) {
        return this._probability;
    }

    /**
     * Returns the value of field 'repairPartsList'.
     * 
     * @return the value of field 'RepairPartsList'.
     */
    public org.mars_sim.msp.config.model.malfunction.RepairPartsList getRepairPartsList(
    ) {
        return this._repairPartsList;
    }

    /**
     * Returns the value of field 'repairTime'.
     * 
     * @return the value of field 'RepairTime'.
     */
    public org.mars_sim.msp.config.model.malfunction.RepairTime getRepairTime(
    ) {
        return this._repairTime;
    }

    /**
     * Returns the value of field 'severity'.
     * 
     * @return the value of field 'Severity'.
     */
    public org.mars_sim.msp.config.model.malfunction.Severity getSeverity(
    ) {
        return this._severity;
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
     * Sets the value of field 'effectList'.
     * 
     * @param effectList the value of field 'effectList'.
     */
    public void setEffectList(
            final org.mars_sim.msp.config.model.malfunction.EffectList effectList) {
        this._effectList = effectList;
    }

    /**
     * Sets the value of field 'emergencyRepairTime'.
     * 
     * @param emergencyRepairTime the value of field
     * 'emergencyRepairTime'.
     */
    public void setEmergencyRepairTime(
            final org.mars_sim.msp.config.model.malfunction.EmergencyRepairTime emergencyRepairTime) {
        this._emergencyRepairTime = emergencyRepairTime;
    }

    /**
     * Sets the value of field 'entityList'.
     * 
     * @param entityList the value of field 'entityList'.
     */
    public void setEntityList(
            final org.mars_sim.msp.config.model.malfunction.EntityList entityList) {
        this._entityList = entityList;
    }

    /**
     * Sets the value of field 'evaRepairTime'.
     * 
     * @param evaRepairTime the value of field 'evaRepairTime'.
     */
    public void setEvaRepairTime(
            final org.mars_sim.msp.config.model.malfunction.EvaRepairTime evaRepairTime) {
        this._evaRepairTime = evaRepairTime;
    }

    /**
     * Sets the value of field 'medicalComplaintList'.
     * 
     * @param medicalComplaintList the value of field
     * 'medicalComplaintList'.
     */
    public void setMedicalComplaintList(
            final org.mars_sim.msp.config.model.malfunction.MedicalComplaintList medicalComplaintList) {
        this._medicalComplaintList = medicalComplaintList;
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
     * Sets the value of field 'probability'.
     * 
     * @param probability the value of field 'probability'.
     */
    public void setProbability(
            final org.mars_sim.msp.config.model.malfunction.Probability probability) {
        this._probability = probability;
    }

    /**
     * Sets the value of field 'repairPartsList'.
     * 
     * @param repairPartsList the value of field 'repairPartsList'.
     */
    public void setRepairPartsList(
            final org.mars_sim.msp.config.model.malfunction.RepairPartsList repairPartsList) {
        this._repairPartsList = repairPartsList;
    }

    /**
     * Sets the value of field 'repairTime'.
     * 
     * @param repairTime the value of field 'repairTime'.
     */
    public void setRepairTime(
            final org.mars_sim.msp.config.model.malfunction.RepairTime repairTime) {
        this._repairTime = repairTime;
    }

    /**
     * Sets the value of field 'severity'.
     * 
     * @param severity the value of field 'severity'.
     */
    public void setSeverity(
            final org.mars_sim.msp.config.model.malfunction.Severity severity) {
        this._severity = severity;
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
     * org.mars_sim.msp.config.model.malfunction.Malfunction
     */
    public static org.mars_sim.msp.config.model.malfunction.Malfunction unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.malfunction.Malfunction) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.malfunction.Malfunction.class, reader);
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
