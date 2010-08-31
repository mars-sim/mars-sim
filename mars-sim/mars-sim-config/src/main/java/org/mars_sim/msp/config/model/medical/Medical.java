/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.medical;

/**
 * Class Medical.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Medical implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _medicalComplaintList.
     */
    private org.mars_sim.msp.config.model.medical.MedicalComplaintList _medicalComplaintList;

    /**
     * Field _treatmentList.
     */
    private org.mars_sim.msp.config.model.medical.TreatmentList _treatmentList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Medical() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'medicalComplaintList'.
     * 
     * @return the value of field 'MedicalComplaintList'.
     */
    public org.mars_sim.msp.config.model.medical.MedicalComplaintList getMedicalComplaintList(
    ) {
        return this._medicalComplaintList;
    }

    /**
     * Returns the value of field 'treatmentList'.
     * 
     * @return the value of field 'TreatmentList'.
     */
    public org.mars_sim.msp.config.model.medical.TreatmentList getTreatmentList(
    ) {
        return this._treatmentList;
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
     * Sets the value of field 'medicalComplaintList'.
     * 
     * @param medicalComplaintList the value of field
     * 'medicalComplaintList'.
     */
    public void setMedicalComplaintList(
            final org.mars_sim.msp.config.model.medical.MedicalComplaintList medicalComplaintList) {
        this._medicalComplaintList = medicalComplaintList;
    }

    /**
     * Sets the value of field 'treatmentList'.
     * 
     * @param treatmentList the value of field 'treatmentList'.
     */
    public void setTreatmentList(
            final org.mars_sim.msp.config.model.medical.TreatmentList treatmentList) {
        this._treatmentList = treatmentList;
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
     * org.mars_sim.msp.config.model.medical.Medical
     */
    public static org.mars_sim.msp.config.model.medical.Medical unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.medical.Medical) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.medical.Medical.class, reader);
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
