/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.people;

/**
 * Class NaturalAttributeList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class NaturalAttributeList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _naturalAttributeList.
     */
    private java.util.List<org.mars_sim.msp.config.model.people.NaturalAttribute> _naturalAttributeList;


      //----------------/
     //- Constructors -/
    //----------------/

    public NaturalAttributeList() {
        super();
        this._naturalAttributeList = new java.util.ArrayList<org.mars_sim.msp.config.model.people.NaturalAttribute>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vNaturalAttribute
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addNaturalAttribute(
            final org.mars_sim.msp.config.model.people.NaturalAttribute vNaturalAttribute)
    throws java.lang.IndexOutOfBoundsException {
        this._naturalAttributeList.add(vNaturalAttribute);
    }

    /**
     * 
     * 
     * @param index
     * @param vNaturalAttribute
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addNaturalAttribute(
            final int index,
            final org.mars_sim.msp.config.model.people.NaturalAttribute vNaturalAttribute)
    throws java.lang.IndexOutOfBoundsException {
        this._naturalAttributeList.add(index, vNaturalAttribute);
    }

    /**
     * Method enumerateNaturalAttribute.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.people.NaturalAttribute> enumerateNaturalAttribute(
    ) {
        return java.util.Collections.enumeration(this._naturalAttributeList);
    }

    /**
     * Method getNaturalAttribute.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.people.NaturalAttribute at the
     * given index
     */
    public org.mars_sim.msp.config.model.people.NaturalAttribute getNaturalAttribute(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._naturalAttributeList.size()) {
            throw new IndexOutOfBoundsException("getNaturalAttribute: Index value '" + index + "' not in range [0.." + (this._naturalAttributeList.size() - 1) + "]");
        }

        return _naturalAttributeList.get(index);
    }

    /**
     * Method getNaturalAttribute.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.people.NaturalAttribute[] getNaturalAttribute(
    ) {
        org.mars_sim.msp.config.model.people.NaturalAttribute[] array = new org.mars_sim.msp.config.model.people.NaturalAttribute[0];
        return this._naturalAttributeList.toArray(array);
    }

    /**
     * Method getNaturalAttributeCount.
     * 
     * @return the size of this collection
     */
    public int getNaturalAttributeCount(
    ) {
        return this._naturalAttributeList.size();
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
     * Method iterateNaturalAttribute.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.people.NaturalAttribute> iterateNaturalAttribute(
    ) {
        return this._naturalAttributeList.iterator();
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
    public void removeAllNaturalAttribute(
    ) {
        this._naturalAttributeList.clear();
    }

    /**
     * Method removeNaturalAttribute.
     * 
     * @param vNaturalAttribute
     * @return true if the object was removed from the collection.
     */
    public boolean removeNaturalAttribute(
            final org.mars_sim.msp.config.model.people.NaturalAttribute vNaturalAttribute) {
        boolean removed = _naturalAttributeList.remove(vNaturalAttribute);
        return removed;
    }

    /**
     * Method removeNaturalAttributeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.people.NaturalAttribute removeNaturalAttributeAt(
            final int index) {
        java.lang.Object obj = this._naturalAttributeList.remove(index);
        return (org.mars_sim.msp.config.model.people.NaturalAttribute) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vNaturalAttribute
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setNaturalAttribute(
            final int index,
            final org.mars_sim.msp.config.model.people.NaturalAttribute vNaturalAttribute)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._naturalAttributeList.size()) {
            throw new IndexOutOfBoundsException("setNaturalAttribute: Index value '" + index + "' not in range [0.." + (this._naturalAttributeList.size() - 1) + "]");
        }

        this._naturalAttributeList.set(index, vNaturalAttribute);
    }

    /**
     * 
     * 
     * @param vNaturalAttributeArray
     */
    public void setNaturalAttribute(
            final org.mars_sim.msp.config.model.people.NaturalAttribute[] vNaturalAttributeArray) {
        //-- copy array
        _naturalAttributeList.clear();

        for (int i = 0; i < vNaturalAttributeArray.length; i++) {
                this._naturalAttributeList.add(vNaturalAttributeArray[i]);
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
     * org.mars_sim.msp.config.model.people.NaturalAttributeList
     */
    public static org.mars_sim.msp.config.model.people.NaturalAttributeList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.people.NaturalAttributeList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.people.NaturalAttributeList.class, reader);
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
