/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.partpackage;

/**
 * Class PartPackageList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class PartPackageList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _partPackageList.
     */
    private java.util.List<org.mars_sim.msp.config.model.partpackage.PartPackage> _partPackageList;


      //----------------/
     //- Constructors -/
    //----------------/

    public PartPackageList() {
        super();
        this._partPackageList = new java.util.ArrayList<org.mars_sim.msp.config.model.partpackage.PartPackage>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vPartPackage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPartPackage(
            final org.mars_sim.msp.config.model.partpackage.PartPackage vPartPackage)
    throws java.lang.IndexOutOfBoundsException {
        this._partPackageList.add(vPartPackage);
    }

    /**
     * 
     * 
     * @param index
     * @param vPartPackage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPartPackage(
            final int index,
            final org.mars_sim.msp.config.model.partpackage.PartPackage vPartPackage)
    throws java.lang.IndexOutOfBoundsException {
        this._partPackageList.add(index, vPartPackage);
    }

    /**
     * Method enumeratePartPackage.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.partpackage.PartPackage> enumeratePartPackage(
    ) {
        return java.util.Collections.enumeration(this._partPackageList);
    }

    /**
     * Method getPartPackage.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.partpackage.PartPackage at the
     * given index
     */
    public org.mars_sim.msp.config.model.partpackage.PartPackage getPartPackage(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._partPackageList.size()) {
            throw new IndexOutOfBoundsException("getPartPackage: Index value '" + index + "' not in range [0.." + (this._partPackageList.size() - 1) + "]");
        }

        return _partPackageList.get(index);
    }

    /**
     * Method getPartPackage.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.partpackage.PartPackage[] getPartPackage(
    ) {
        org.mars_sim.msp.config.model.partpackage.PartPackage[] array = new org.mars_sim.msp.config.model.partpackage.PartPackage[0];
        return this._partPackageList.toArray(array);
    }

    /**
     * Method getPartPackageCount.
     * 
     * @return the size of this collection
     */
    public int getPartPackageCount(
    ) {
        return this._partPackageList.size();
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
     * Method iteratePartPackage.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.partpackage.PartPackage> iteratePartPackage(
    ) {
        return this._partPackageList.iterator();
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
    public void removeAllPartPackage(
    ) {
        this._partPackageList.clear();
    }

    /**
     * Method removePartPackage.
     * 
     * @param vPartPackage
     * @return true if the object was removed from the collection.
     */
    public boolean removePartPackage(
            final org.mars_sim.msp.config.model.partpackage.PartPackage vPartPackage) {
        boolean removed = _partPackageList.remove(vPartPackage);
        return removed;
    }

    /**
     * Method removePartPackageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.partpackage.PartPackage removePartPackageAt(
            final int index) {
        java.lang.Object obj = this._partPackageList.remove(index);
        return (org.mars_sim.msp.config.model.partpackage.PartPackage) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vPartPackage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setPartPackage(
            final int index,
            final org.mars_sim.msp.config.model.partpackage.PartPackage vPartPackage)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._partPackageList.size()) {
            throw new IndexOutOfBoundsException("setPartPackage: Index value '" + index + "' not in range [0.." + (this._partPackageList.size() - 1) + "]");
        }

        this._partPackageList.set(index, vPartPackage);
    }

    /**
     * 
     * 
     * @param vPartPackageArray
     */
    public void setPartPackage(
            final org.mars_sim.msp.config.model.partpackage.PartPackage[] vPartPackageArray) {
        //-- copy array
        _partPackageList.clear();

        for (int i = 0; i < vPartPackageArray.length; i++) {
                this._partPackageList.add(vPartPackageArray[i]);
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
     * org.mars_sim.msp.config.model.partpackage.PartPackageList
     */
    public static org.mars_sim.msp.config.model.partpackage.PartPackageList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.partpackage.PartPackageList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.partpackage.PartPackageList.class, reader);
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
