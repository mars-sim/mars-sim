/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.people;

/**
 * Class PersonNameList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class PersonNameList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _personNameList.
     */
    private java.util.List<org.mars_sim.msp.config.model.people.PersonName> _personNameList;


      //----------------/
     //- Constructors -/
    //----------------/

    public PersonNameList() {
        super();
        this._personNameList = new java.util.ArrayList<org.mars_sim.msp.config.model.people.PersonName>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vPersonName
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPersonName(
            final org.mars_sim.msp.config.model.people.PersonName vPersonName)
    throws java.lang.IndexOutOfBoundsException {
        this._personNameList.add(vPersonName);
    }

    /**
     * 
     * 
     * @param index
     * @param vPersonName
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPersonName(
            final int index,
            final org.mars_sim.msp.config.model.people.PersonName vPersonName)
    throws java.lang.IndexOutOfBoundsException {
        this._personNameList.add(index, vPersonName);
    }

    /**
     * Method enumeratePersonName.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.people.PersonName> enumeratePersonName(
    ) {
        return java.util.Collections.enumeration(this._personNameList);
    }

    /**
     * Method getPersonName.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.people.PersonName at the given
     * index
     */
    public org.mars_sim.msp.config.model.people.PersonName getPersonName(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._personNameList.size()) {
            throw new IndexOutOfBoundsException("getPersonName: Index value '" + index + "' not in range [0.." + (this._personNameList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.people.PersonName) _personNameList.get(index);
    }

    /**
     * Method getPersonName.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.people.PersonName[] getPersonName(
    ) {
        org.mars_sim.msp.config.model.people.PersonName[] array = new org.mars_sim.msp.config.model.people.PersonName[0];
        return (org.mars_sim.msp.config.model.people.PersonName[]) this._personNameList.toArray(array);
    }

    /**
     * Method getPersonNameCount.
     * 
     * @return the size of this collection
     */
    public int getPersonNameCount(
    ) {
        return this._personNameList.size();
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
     * Method iteratePersonName.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.people.PersonName> iteratePersonName(
    ) {
        return this._personNameList.iterator();
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
    public void removeAllPersonName(
    ) {
        this._personNameList.clear();
    }

    /**
     * Method removePersonName.
     * 
     * @param vPersonName
     * @return true if the object was removed from the collection.
     */
    public boolean removePersonName(
            final org.mars_sim.msp.config.model.people.PersonName vPersonName) {
        boolean removed = _personNameList.remove(vPersonName);
        return removed;
    }

    /**
     * Method removePersonNameAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.people.PersonName removePersonNameAt(
            final int index) {
        java.lang.Object obj = this._personNameList.remove(index);
        return (org.mars_sim.msp.config.model.people.PersonName) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vPersonName
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setPersonName(
            final int index,
            final org.mars_sim.msp.config.model.people.PersonName vPersonName)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._personNameList.size()) {
            throw new IndexOutOfBoundsException("setPersonName: Index value '" + index + "' not in range [0.." + (this._personNameList.size() - 1) + "]");
        }

        this._personNameList.set(index, vPersonName);
    }

    /**
     * 
     * 
     * @param vPersonNameArray
     */
    public void setPersonName(
            final org.mars_sim.msp.config.model.people.PersonName[] vPersonNameArray) {
        //-- copy array
        _personNameList.clear();

        for (int i = 0; i < vPersonNameArray.length; i++) {
                this._personNameList.add(vPersonNameArray[i]);
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
     * org.mars_sim.msp.config.model.people.PersonNameList
     */
    public static org.mars_sim.msp.config.model.people.PersonNameList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.people.PersonNameList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.people.PersonNameList.class, reader);
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
