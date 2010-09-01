/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.people;

/**
 * Class PersonList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class PersonList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _personList.
     */
    private java.util.List<org.mars_sim.msp.config.model.people.Person> _personList;


      //----------------/
     //- Constructors -/
    //----------------/

    public PersonList() {
        super();
        this._personList = new java.util.ArrayList<org.mars_sim.msp.config.model.people.Person>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vPerson
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPerson(
            final org.mars_sim.msp.config.model.people.Person vPerson)
    throws java.lang.IndexOutOfBoundsException {
        this._personList.add(vPerson);
    }

    /**
     * 
     * 
     * @param index
     * @param vPerson
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPerson(
            final int index,
            final org.mars_sim.msp.config.model.people.Person vPerson)
    throws java.lang.IndexOutOfBoundsException {
        this._personList.add(index, vPerson);
    }

    /**
     * Method enumeratePerson.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.people.Person> enumeratePerson(
    ) {
        return java.util.Collections.enumeration(this._personList);
    }

    /**
     * Method getPerson.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.people.Person at the given inde
     */
    public org.mars_sim.msp.config.model.people.Person getPerson(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._personList.size()) {
            throw new IndexOutOfBoundsException("getPerson: Index value '" + index + "' not in range [0.." + (this._personList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.people.Person) _personList.get(index);
    }

    /**
     * Method getPerson.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.people.Person[] getPerson(
    ) {
        org.mars_sim.msp.config.model.people.Person[] array = new org.mars_sim.msp.config.model.people.Person[0];
        return (org.mars_sim.msp.config.model.people.Person[]) this._personList.toArray(array);
    }

    /**
     * Method getPersonCount.
     * 
     * @return the size of this collection
     */
    public int getPersonCount(
    ) {
        return this._personList.size();
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
     * Method iteratePerson.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.people.Person> iteratePerson(
    ) {
        return this._personList.iterator();
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
    public void removeAllPerson(
    ) {
        this._personList.clear();
    }

    /**
     * Method removePerson.
     * 
     * @param vPerson
     * @return true if the object was removed from the collection.
     */
    public boolean removePerson(
            final org.mars_sim.msp.config.model.people.Person vPerson) {
        boolean removed = _personList.remove(vPerson);
        return removed;
    }

    /**
     * Method removePersonAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.people.Person removePersonAt(
            final int index) {
        java.lang.Object obj = this._personList.remove(index);
        return (org.mars_sim.msp.config.model.people.Person) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vPerson
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setPerson(
            final int index,
            final org.mars_sim.msp.config.model.people.Person vPerson)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._personList.size()) {
            throw new IndexOutOfBoundsException("setPerson: Index value '" + index + "' not in range [0.." + (this._personList.size() - 1) + "]");
        }

        this._personList.set(index, vPerson);
    }

    /**
     * 
     * 
     * @param vPersonArray
     */
    public void setPerson(
            final org.mars_sim.msp.config.model.people.Person[] vPersonArray) {
        //-- copy array
        _personList.clear();

        for (int i = 0; i < vPersonArray.length; i++) {
                this._personList.add(vPersonArray[i]);
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
     * org.mars_sim.msp.config.model.people.PersonList
     */
    public static org.mars_sim.msp.config.model.people.PersonList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.people.PersonList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.people.PersonList.class, reader);
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
