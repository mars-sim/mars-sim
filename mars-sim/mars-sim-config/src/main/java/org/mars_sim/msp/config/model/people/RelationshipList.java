/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.people;

/**
 * Class RelationshipList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class RelationshipList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _relationshipList.
     */
    private java.util.List<org.mars_sim.msp.config.model.people.Relationship> _relationshipList;


      //----------------/
     //- Constructors -/
    //----------------/

    public RelationshipList() {
        super();
        this._relationshipList = new java.util.ArrayList<org.mars_sim.msp.config.model.people.Relationship>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vRelationship
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRelationship(
            final org.mars_sim.msp.config.model.people.Relationship vRelationship)
    throws java.lang.IndexOutOfBoundsException {
        this._relationshipList.add(vRelationship);
    }

    /**
     * 
     * 
     * @param index
     * @param vRelationship
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRelationship(
            final int index,
            final org.mars_sim.msp.config.model.people.Relationship vRelationship)
    throws java.lang.IndexOutOfBoundsException {
        this._relationshipList.add(index, vRelationship);
    }

    /**
     * Method enumerateRelationship.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.people.Relationship> enumerateRelationship(
    ) {
        return java.util.Collections.enumeration(this._relationshipList);
    }

    /**
     * Method getRelationship.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.people.Relationship at the
     * given index
     */
    public org.mars_sim.msp.config.model.people.Relationship getRelationship(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._relationshipList.size()) {
            throw new IndexOutOfBoundsException("getRelationship: Index value '" + index + "' not in range [0.." + (this._relationshipList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.people.Relationship) _relationshipList.get(index);
    }

    /**
     * Method getRelationship.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.people.Relationship[] getRelationship(
    ) {
        org.mars_sim.msp.config.model.people.Relationship[] array = new org.mars_sim.msp.config.model.people.Relationship[0];
        return (org.mars_sim.msp.config.model.people.Relationship[]) this._relationshipList.toArray(array);
    }

    /**
     * Method getRelationshipCount.
     * 
     * @return the size of this collection
     */
    public int getRelationshipCount(
    ) {
        return this._relationshipList.size();
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
     * Method iterateRelationship.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.people.Relationship> iterateRelationship(
    ) {
        return this._relationshipList.iterator();
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
    public void removeAllRelationship(
    ) {
        this._relationshipList.clear();
    }

    /**
     * Method removeRelationship.
     * 
     * @param vRelationship
     * @return true if the object was removed from the collection.
     */
    public boolean removeRelationship(
            final org.mars_sim.msp.config.model.people.Relationship vRelationship) {
        boolean removed = _relationshipList.remove(vRelationship);
        return removed;
    }

    /**
     * Method removeRelationshipAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.people.Relationship removeRelationshipAt(
            final int index) {
        java.lang.Object obj = this._relationshipList.remove(index);
        return (org.mars_sim.msp.config.model.people.Relationship) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vRelationship
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setRelationship(
            final int index,
            final org.mars_sim.msp.config.model.people.Relationship vRelationship)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._relationshipList.size()) {
            throw new IndexOutOfBoundsException("setRelationship: Index value '" + index + "' not in range [0.." + (this._relationshipList.size() - 1) + "]");
        }

        this._relationshipList.set(index, vRelationship);
    }

    /**
     * 
     * 
     * @param vRelationshipArray
     */
    public void setRelationship(
            final org.mars_sim.msp.config.model.people.Relationship[] vRelationshipArray) {
        //-- copy array
        _relationshipList.clear();

        for (int i = 0; i < vRelationshipArray.length; i++) {
                this._relationshipList.add(vRelationshipArray[i]);
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
     * org.mars_sim.msp.config.model.people.RelationshipList
     */
    public static org.mars_sim.msp.config.model.people.RelationshipList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.people.RelationshipList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.people.RelationshipList.class, reader);
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
