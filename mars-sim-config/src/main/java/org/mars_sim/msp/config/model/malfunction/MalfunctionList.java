/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.malfunction;

/**
 * Class MalfunctionList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class MalfunctionList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _malfunctionList.
     */
    private java.util.List<org.mars_sim.msp.config.model.malfunction.Malfunction> _malfunctionList;


      //----------------/
     //- Constructors -/
    //----------------/

    public MalfunctionList() {
        super();
        this._malfunctionList = new java.util.ArrayList<org.mars_sim.msp.config.model.malfunction.Malfunction>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vMalfunction
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMalfunction(
            final org.mars_sim.msp.config.model.malfunction.Malfunction vMalfunction)
    throws java.lang.IndexOutOfBoundsException {
        this._malfunctionList.add(vMalfunction);
    }

    /**
     * 
     * 
     * @param index
     * @param vMalfunction
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMalfunction(
            final int index,
            final org.mars_sim.msp.config.model.malfunction.Malfunction vMalfunction)
    throws java.lang.IndexOutOfBoundsException {
        this._malfunctionList.add(index, vMalfunction);
    }

    /**
     * Method enumerateMalfunction.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.malfunction.Malfunction> enumerateMalfunction(
    ) {
        return java.util.Collections.enumeration(this._malfunctionList);
    }

    /**
     * Method getMalfunction.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.malfunction.Malfunction at the
     * given index
     */
    public org.mars_sim.msp.config.model.malfunction.Malfunction getMalfunction(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._malfunctionList.size()) {
            throw new IndexOutOfBoundsException("getMalfunction: Index value '" + index + "' not in range [0.." + (this._malfunctionList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.malfunction.Malfunction) _malfunctionList.get(index);
    }

    /**
     * Method getMalfunction.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.malfunction.Malfunction[] getMalfunction(
    ) {
        org.mars_sim.msp.config.model.malfunction.Malfunction[] array = new org.mars_sim.msp.config.model.malfunction.Malfunction[0];
        return (org.mars_sim.msp.config.model.malfunction.Malfunction[]) this._malfunctionList.toArray(array);
    }

    /**
     * Method getMalfunctionCount.
     * 
     * @return the size of this collection
     */
    public int getMalfunctionCount(
    ) {
        return this._malfunctionList.size();
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
     * Method iterateMalfunction.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.malfunction.Malfunction> iterateMalfunction(
    ) {
        return this._malfunctionList.iterator();
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
    public void removeAllMalfunction(
    ) {
        this._malfunctionList.clear();
    }

    /**
     * Method removeMalfunction.
     * 
     * @param vMalfunction
     * @return true if the object was removed from the collection.
     */
    public boolean removeMalfunction(
            final org.mars_sim.msp.config.model.malfunction.Malfunction vMalfunction) {
        boolean removed = _malfunctionList.remove(vMalfunction);
        return removed;
    }

    /**
     * Method removeMalfunctionAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.malfunction.Malfunction removeMalfunctionAt(
            final int index) {
        java.lang.Object obj = this._malfunctionList.remove(index);
        return (org.mars_sim.msp.config.model.malfunction.Malfunction) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vMalfunction
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setMalfunction(
            final int index,
            final org.mars_sim.msp.config.model.malfunction.Malfunction vMalfunction)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._malfunctionList.size()) {
            throw new IndexOutOfBoundsException("setMalfunction: Index value '" + index + "' not in range [0.." + (this._malfunctionList.size() - 1) + "]");
        }

        this._malfunctionList.set(index, vMalfunction);
    }

    /**
     * 
     * 
     * @param vMalfunctionArray
     */
    public void setMalfunction(
            final org.mars_sim.msp.config.model.malfunction.Malfunction[] vMalfunctionArray) {
        //-- copy array
        _malfunctionList.clear();

        for (int i = 0; i < vMalfunctionArray.length; i++) {
                this._malfunctionList.add(vMalfunctionArray[i]);
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
     * org.mars_sim.msp.config.model.malfunction.MalfunctionList
     */
    public static org.mars_sim.msp.config.model.malfunction.MalfunctionList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.malfunction.MalfunctionList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.malfunction.MalfunctionList.class, reader);
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
