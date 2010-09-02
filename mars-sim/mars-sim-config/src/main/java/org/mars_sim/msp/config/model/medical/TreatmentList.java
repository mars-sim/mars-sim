/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.medical;

/**
 * Class TreatmentList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class TreatmentList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _treatmentList.
     */
    private java.util.List<org.mars_sim.msp.config.model.medical.Treatment> _treatmentList;


      //----------------/
     //- Constructors -/
    //----------------/

    public TreatmentList() {
        super();
        this._treatmentList = new java.util.ArrayList<org.mars_sim.msp.config.model.medical.Treatment>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vTreatment
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addTreatment(
            final org.mars_sim.msp.config.model.medical.Treatment vTreatment)
    throws java.lang.IndexOutOfBoundsException {
        this._treatmentList.add(vTreatment);
    }

    /**
     * 
     * 
     * @param index
     * @param vTreatment
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addTreatment(
            final int index,
            final org.mars_sim.msp.config.model.medical.Treatment vTreatment)
    throws java.lang.IndexOutOfBoundsException {
        this._treatmentList.add(index, vTreatment);
    }

    /**
     * Method enumerateTreatment.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.medical.Treatment> enumerateTreatment(
    ) {
        return java.util.Collections.enumeration(this._treatmentList);
    }

    /**
     * Method getTreatment.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.medical.Treatment at the given
     * index
     */
    public org.mars_sim.msp.config.model.medical.Treatment getTreatment(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._treatmentList.size()) {
            throw new IndexOutOfBoundsException("getTreatment: Index value '" + index + "' not in range [0.." + (this._treatmentList.size() - 1) + "]");
        }

        return _treatmentList.get(index);
    }

    /**
     * Method getTreatment.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.medical.Treatment[] getTreatment(
    ) {
        org.mars_sim.msp.config.model.medical.Treatment[] array = new org.mars_sim.msp.config.model.medical.Treatment[0];
        return this._treatmentList.toArray(array);
    }

    /**
     * Method getTreatmentCount.
     * 
     * @return the size of this collection
     */
    public int getTreatmentCount(
    ) {
        return this._treatmentList.size();
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
     * Method iterateTreatment.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.medical.Treatment> iterateTreatment(
    ) {
        return this._treatmentList.iterator();
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
    public void removeAllTreatment(
    ) {
        this._treatmentList.clear();
    }

    /**
     * Method removeTreatment.
     * 
     * @param vTreatment
     * @return true if the object was removed from the collection.
     */
    public boolean removeTreatment(
            final org.mars_sim.msp.config.model.medical.Treatment vTreatment) {
        boolean removed = _treatmentList.remove(vTreatment);
        return removed;
    }

    /**
     * Method removeTreatmentAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.medical.Treatment removeTreatmentAt(
            final int index) {
        java.lang.Object obj = this._treatmentList.remove(index);
        return (org.mars_sim.msp.config.model.medical.Treatment) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vTreatment
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setTreatment(
            final int index,
            final org.mars_sim.msp.config.model.medical.Treatment vTreatment)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._treatmentList.size()) {
            throw new IndexOutOfBoundsException("setTreatment: Index value '" + index + "' not in range [0.." + (this._treatmentList.size() - 1) + "]");
        }

        this._treatmentList.set(index, vTreatment);
    }

    /**
     * 
     * 
     * @param vTreatmentArray
     */
    public void setTreatment(
            final org.mars_sim.msp.config.model.medical.Treatment[] vTreatmentArray) {
        //-- copy array
        _treatmentList.clear();

        for (int i = 0; i < vTreatmentArray.length; i++) {
                this._treatmentList.add(vTreatmentArray[i]);
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
     * org.mars_sim.msp.config.model.medical.TreatmentList
     */
    public static org.mars_sim.msp.config.model.medical.TreatmentList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.medical.TreatmentList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.medical.TreatmentList.class, reader);
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
