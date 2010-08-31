/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.construction;

/**
 * Class FoundationList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class FoundationList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _foundationList.
     */
    private java.util.List<org.mars_sim.msp.config.model.construction.Foundation> _foundationList;


      //----------------/
     //- Constructors -/
    //----------------/

    public FoundationList() {
        super();
        this._foundationList = new java.util.ArrayList<org.mars_sim.msp.config.model.construction.Foundation>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vFoundation
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addFoundation(
            final org.mars_sim.msp.config.model.construction.Foundation vFoundation)
    throws java.lang.IndexOutOfBoundsException {
        this._foundationList.add(vFoundation);
    }

    /**
     * 
     * 
     * @param index
     * @param vFoundation
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addFoundation(
            final int index,
            final org.mars_sim.msp.config.model.construction.Foundation vFoundation)
    throws java.lang.IndexOutOfBoundsException {
        this._foundationList.add(index, vFoundation);
    }

    /**
     * Method enumerateFoundation.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.construction.Foundation> enumerateFoundation(
    ) {
        return java.util.Collections.enumeration(this._foundationList);
    }

    /**
     * Method getFoundation.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.construction.Foundation at the
     * given index
     */
    public org.mars_sim.msp.config.model.construction.Foundation getFoundation(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._foundationList.size()) {
            throw new IndexOutOfBoundsException("getFoundation: Index value '" + index + "' not in range [0.." + (this._foundationList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.construction.Foundation) _foundationList.get(index);
    }

    /**
     * Method getFoundation.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.construction.Foundation[] getFoundation(
    ) {
        org.mars_sim.msp.config.model.construction.Foundation[] array = new org.mars_sim.msp.config.model.construction.Foundation[0];
        return (org.mars_sim.msp.config.model.construction.Foundation[]) this._foundationList.toArray(array);
    }

    /**
     * Method getFoundationCount.
     * 
     * @return the size of this collection
     */
    public int getFoundationCount(
    ) {
        return this._foundationList.size();
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
     * Method iterateFoundation.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.construction.Foundation> iterateFoundation(
    ) {
        return this._foundationList.iterator();
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
    public void removeAllFoundation(
    ) {
        this._foundationList.clear();
    }

    /**
     * Method removeFoundation.
     * 
     * @param vFoundation
     * @return true if the object was removed from the collection.
     */
    public boolean removeFoundation(
            final org.mars_sim.msp.config.model.construction.Foundation vFoundation) {
        boolean removed = _foundationList.remove(vFoundation);
        return removed;
    }

    /**
     * Method removeFoundationAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.construction.Foundation removeFoundationAt(
            final int index) {
        java.lang.Object obj = this._foundationList.remove(index);
        return (org.mars_sim.msp.config.model.construction.Foundation) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vFoundation
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setFoundation(
            final int index,
            final org.mars_sim.msp.config.model.construction.Foundation vFoundation)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._foundationList.size()) {
            throw new IndexOutOfBoundsException("setFoundation: Index value '" + index + "' not in range [0.." + (this._foundationList.size() - 1) + "]");
        }

        this._foundationList.set(index, vFoundation);
    }

    /**
     * 
     * 
     * @param vFoundationArray
     */
    public void setFoundation(
            final org.mars_sim.msp.config.model.construction.Foundation[] vFoundationArray) {
        //-- copy array
        _foundationList.clear();

        for (int i = 0; i < vFoundationArray.length; i++) {
                this._foundationList.add(vFoundationArray[i]);
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
     * org.mars_sim.msp.config.model.construction.FoundationList
     */
    public static org.mars_sim.msp.config.model.construction.FoundationList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.construction.FoundationList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.construction.FoundationList.class, reader);
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
