/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.people;

/**
 * Class PersonalityTypes.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class PersonalityTypes implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _mbtiList.
     */
    private java.util.List<org.mars_sim.msp.config.model.people.Mbti> _mbtiList;


      //----------------/
     //- Constructors -/
    //----------------/

    public PersonalityTypes() {
        super();
        this._mbtiList = new java.util.ArrayList<org.mars_sim.msp.config.model.people.Mbti>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vMbti
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMbti(
            final org.mars_sim.msp.config.model.people.Mbti vMbti)
    throws java.lang.IndexOutOfBoundsException {
        this._mbtiList.add(vMbti);
    }

    /**
     * 
     * 
     * @param index
     * @param vMbti
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addMbti(
            final int index,
            final org.mars_sim.msp.config.model.people.Mbti vMbti)
    throws java.lang.IndexOutOfBoundsException {
        this._mbtiList.add(index, vMbti);
    }

    /**
     * Method enumerateMbti.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.people.Mbti> enumerateMbti(
    ) {
        return java.util.Collections.enumeration(this._mbtiList);
    }

    /**
     * Method getMbti.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.people.Mbti at the given index
     */
    public org.mars_sim.msp.config.model.people.Mbti getMbti(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._mbtiList.size()) {
            throw new IndexOutOfBoundsException("getMbti: Index value '" + index + "' not in range [0.." + (this._mbtiList.size() - 1) + "]");
        }

        return _mbtiList.get(index);
    }

    /**
     * Method getMbti.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.people.Mbti[] getMbti(
    ) {
        org.mars_sim.msp.config.model.people.Mbti[] array = new org.mars_sim.msp.config.model.people.Mbti[0];
        return this._mbtiList.toArray(array);
    }

    /**
     * Method getMbtiCount.
     * 
     * @return the size of this collection
     */
    public int getMbtiCount(
    ) {
        return this._mbtiList.size();
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
     * Method iterateMbti.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.people.Mbti> iterateMbti(
    ) {
        return this._mbtiList.iterator();
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
    public void removeAllMbti(
    ) {
        this._mbtiList.clear();
    }

    /**
     * Method removeMbti.
     * 
     * @param vMbti
     * @return true if the object was removed from the collection.
     */
    public boolean removeMbti(
            final org.mars_sim.msp.config.model.people.Mbti vMbti) {
        boolean removed = _mbtiList.remove(vMbti);
        return removed;
    }

    /**
     * Method removeMbtiAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.people.Mbti removeMbtiAt(
            final int index) {
        java.lang.Object obj = this._mbtiList.remove(index);
        return (org.mars_sim.msp.config.model.people.Mbti) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vMbti
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setMbti(
            final int index,
            final org.mars_sim.msp.config.model.people.Mbti vMbti)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._mbtiList.size()) {
            throw new IndexOutOfBoundsException("setMbti: Index value '" + index + "' not in range [0.." + (this._mbtiList.size() - 1) + "]");
        }

        this._mbtiList.set(index, vMbti);
    }

    /**
     * 
     * 
     * @param vMbtiArray
     */
    public void setMbti(
            final org.mars_sim.msp.config.model.people.Mbti[] vMbtiArray) {
        //-- copy array
        _mbtiList.clear();

        for (int i = 0; i < vMbtiArray.length; i++) {
                this._mbtiList.add(vMbtiArray[i]);
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
     * org.mars_sim.msp.config.model.people.PersonalityTypes
     */
    public static org.mars_sim.msp.config.model.people.PersonalityTypes unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.people.PersonalityTypes) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.people.PersonalityTypes.class, reader);
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
