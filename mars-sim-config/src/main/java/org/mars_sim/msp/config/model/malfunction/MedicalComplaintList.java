/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.malfunction;

/**
 * Class MedicalComplaintList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class MedicalComplaintList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _medicalComplaintList.
     */
    private java.util.List<org.mars_sim.msp.config.model.malfunction.MedicalComplaint> _medicalComplaintList;


      //----------------/
     //- Constructors -/
    //----------------/

    public MedicalComplaintList() {
        super();
        this._medicalComplaintList = new java.util.ArrayList<org.mars_sim.msp.config.model.malfunction.MedicalComplaint>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vMedicalComplaint
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMedicalComplaint(
            final org.mars_sim.msp.config.model.malfunction.MedicalComplaint vMedicalComplaint)
    throws java.lang.IndexOutOfBoundsException {
        this._medicalComplaintList.add(vMedicalComplaint);
    }

    /**
     * 
     * 
     * @param index
     * @param vMedicalComplaint
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMedicalComplaint(
            final int index,
            final org.mars_sim.msp.config.model.malfunction.MedicalComplaint vMedicalComplaint)
    throws java.lang.IndexOutOfBoundsException {
        this._medicalComplaintList.add(index, vMedicalComplaint);
    }

    /**
     * Method enumerateMedicalComplaint.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.malfunction.MedicalComplaint> enumerateMedicalComplaint(
    ) {
        return java.util.Collections.enumeration(this._medicalComplaintList);
    }

    /**
     * Method getMedicalComplaint.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.malfunction.MedicalComplaint
     * at the given index
     */
    public org.mars_sim.msp.config.model.malfunction.MedicalComplaint getMedicalComplaint(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._medicalComplaintList.size()) {
            throw new IndexOutOfBoundsException("getMedicalComplaint: Index value '" + index + "' not in range [0.." + (this._medicalComplaintList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.malfunction.MedicalComplaint) _medicalComplaintList.get(index);
    }

    /**
     * Method getMedicalComplaint.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.malfunction.MedicalComplaint[] getMedicalComplaint(
    ) {
        org.mars_sim.msp.config.model.malfunction.MedicalComplaint[] array = new org.mars_sim.msp.config.model.malfunction.MedicalComplaint[0];
        return (org.mars_sim.msp.config.model.malfunction.MedicalComplaint[]) this._medicalComplaintList.toArray(array);
    }

    /**
     * Method getMedicalComplaintCount.
     * 
     * @return the size of this collection
     */
    public int getMedicalComplaintCount(
    ) {
        return this._medicalComplaintList.size();
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
     * Method iterateMedicalComplaint.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.malfunction.MedicalComplaint> iterateMedicalComplaint(
    ) {
        return this._medicalComplaintList.iterator();
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
    public void removeAllMedicalComplaint(
    ) {
        this._medicalComplaintList.clear();
    }

    /**
     * Method removeMedicalComplaint.
     * 
     * @param vMedicalComplaint
     * @return true if the object was removed from the collection.
     */
    public boolean removeMedicalComplaint(
            final org.mars_sim.msp.config.model.malfunction.MedicalComplaint vMedicalComplaint) {
        boolean removed = _medicalComplaintList.remove(vMedicalComplaint);
        return removed;
    }

    /**
     * Method removeMedicalComplaintAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.malfunction.MedicalComplaint removeMedicalComplaintAt(
            final int index) {
        java.lang.Object obj = this._medicalComplaintList.remove(index);
        return (org.mars_sim.msp.config.model.malfunction.MedicalComplaint) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vMedicalComplaint
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setMedicalComplaint(
            final int index,
            final org.mars_sim.msp.config.model.malfunction.MedicalComplaint vMedicalComplaint)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._medicalComplaintList.size()) {
            throw new IndexOutOfBoundsException("setMedicalComplaint: Index value '" + index + "' not in range [0.." + (this._medicalComplaintList.size() - 1) + "]");
        }

        this._medicalComplaintList.set(index, vMedicalComplaint);
    }

    /**
     * 
     * 
     * @param vMedicalComplaintArray
     */
    public void setMedicalComplaint(
            final org.mars_sim.msp.config.model.malfunction.MedicalComplaint[] vMedicalComplaintArray) {
        //-- copy array
        _medicalComplaintList.clear();

        for (int i = 0; i < vMedicalComplaintArray.length; i++) {
                this._medicalComplaintList.add(vMedicalComplaintArray[i]);
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
     * org.mars_sim.msp.config.model.malfunction.MedicalComplaintLis
     */
    public static org.mars_sim.msp.config.model.malfunction.MedicalComplaintList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.malfunction.MedicalComplaintList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.malfunction.MedicalComplaintList.class, reader);
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
