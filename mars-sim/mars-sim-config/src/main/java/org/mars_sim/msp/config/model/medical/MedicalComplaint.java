/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.medical;

/**
 * Class MedicalComplaint.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class MedicalComplaint implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _seriousness.
     */
    private org.mars_sim.msp.config.model.medical.Seriousness _seriousness;

    /**
     * Field _degradeTime.
     */
    private org.mars_sim.msp.config.model.medical.DegradeTime _degradeTime;

    /**
     * Field _recoveryTime.
     */
    private org.mars_sim.msp.config.model.medical.RecoveryTime _recoveryTime;

    /**
     * Field _probability.
     */
    private org.mars_sim.msp.config.model.medical.Probability _probability;

    /**
     * Field _performancePercent.
     */
    private org.mars_sim.msp.config.model.medical.PerformancePercent _performancePercent;

    /**
     * Field _treatmentType.
     */
    private org.mars_sim.msp.config.model.medical.TreatmentType _treatmentType;

    /**
     * Field _degradeComplaint.
     */
    private org.mars_sim.msp.config.model.medical.DegradeComplaint _degradeComplaint;


      //----------------/
     //- Constructors -/
    //----------------/

    public MedicalComplaint() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'degradeComplaint'.
     * 
     * @return the value of field 'DegradeComplaint'.
     */
    public org.mars_sim.msp.config.model.medical.DegradeComplaint getDegradeComplaint(
    ) {
        return this._degradeComplaint;
    }

    /**
     * Returns the value of field 'degradeTime'.
     * 
     * @return the value of field 'DegradeTime'.
     */
    public org.mars_sim.msp.config.model.medical.DegradeTime getDegradeTime(
    ) {
        return this._degradeTime;
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
     * Returns the value of field 'performancePercent'.
     * 
     * @return the value of field 'PerformancePercent'.
     */
    public org.mars_sim.msp.config.model.medical.PerformancePercent getPerformancePercent(
    ) {
        return this._performancePercent;
    }

    /**
     * Returns the value of field 'probability'.
     * 
     * @return the value of field 'Probability'.
     */
    public org.mars_sim.msp.config.model.medical.Probability getProbability(
    ) {
        return this._probability;
    }

    /**
     * Returns the value of field 'recoveryTime'.
     * 
     * @return the value of field 'RecoveryTime'.
     */
    public org.mars_sim.msp.config.model.medical.RecoveryTime getRecoveryTime(
    ) {
        return this._recoveryTime;
    }

    /**
     * Returns the value of field 'seriousness'.
     * 
     * @return the value of field 'Seriousness'.
     */
    public org.mars_sim.msp.config.model.medical.Seriousness getSeriousness(
    ) {
        return this._seriousness;
    }

    /**
     * Returns the value of field 'treatmentType'.
     * 
     * @return the value of field 'TreatmentType'.
     */
    public org.mars_sim.msp.config.model.medical.TreatmentType getTreatmentType(
    ) {
        return this._treatmentType;
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
     * Sets the value of field 'degradeComplaint'.
     * 
     * @param degradeComplaint the value of field 'degradeComplaint'
     */
    public void setDegradeComplaint(
            final org.mars_sim.msp.config.model.medical.DegradeComplaint degradeComplaint) {
        this._degradeComplaint = degradeComplaint;
    }

    /**
     * Sets the value of field 'degradeTime'.
     * 
     * @param degradeTime the value of field 'degradeTime'.
     */
    public void setDegradeTime(
            final org.mars_sim.msp.config.model.medical.DegradeTime degradeTime) {
        this._degradeTime = degradeTime;
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
     * Sets the value of field 'performancePercent'.
     * 
     * @param performancePercent the value of field
     * 'performancePercent'.
     */
    public void setPerformancePercent(
            final org.mars_sim.msp.config.model.medical.PerformancePercent performancePercent) {
        this._performancePercent = performancePercent;
    }

    /**
     * Sets the value of field 'probability'.
     * 
     * @param probability the value of field 'probability'.
     */
    public void setProbability(
            final org.mars_sim.msp.config.model.medical.Probability probability) {
        this._probability = probability;
    }

    /**
     * Sets the value of field 'recoveryTime'.
     * 
     * @param recoveryTime the value of field 'recoveryTime'.
     */
    public void setRecoveryTime(
            final org.mars_sim.msp.config.model.medical.RecoveryTime recoveryTime) {
        this._recoveryTime = recoveryTime;
    }

    /**
     * Sets the value of field 'seriousness'.
     * 
     * @param seriousness the value of field 'seriousness'.
     */
    public void setSeriousness(
            final org.mars_sim.msp.config.model.medical.Seriousness seriousness) {
        this._seriousness = seriousness;
    }

    /**
     * Sets the value of field 'treatmentType'.
     * 
     * @param treatmentType the value of field 'treatmentType'.
     */
    public void setTreatmentType(
            final org.mars_sim.msp.config.model.medical.TreatmentType treatmentType) {
        this._treatmentType = treatmentType;
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
     * org.mars_sim.msp.config.model.medical.MedicalComplaint
     */
    public static org.mars_sim.msp.config.model.medical.MedicalComplaint unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.medical.MedicalComplaint) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.medical.MedicalComplaint.class, reader);
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
