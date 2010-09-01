/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.people;

/**
 * Class Relationship.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Relationship implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _personName.
     */
    private java.lang.String _personName;

    /**
     * Field _opinion.
     */
    private long _opinion;

    /**
     * keeps track of state for field: _opinion
     */
    private boolean _has_opinion;


      //----------------/
     //- Constructors -/
    //----------------/

    public Relationship() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteOpinion(
    ) {
        this._has_opinion= false;
    }

    /**
     * Returns the value of field 'opinion'.
     * 
     * @return the value of field 'Opinion'.
     */
    public long getOpinion(
    ) {
        return this._opinion;
    }

    /**
     * Returns the value of field 'personName'.
     * 
     * @return the value of field 'PersonName'.
     */
    public java.lang.String getPersonName(
    ) {
        return this._personName;
    }

    /**
     * Method hasOpinion.
     * 
     * @return true if at least one Opinion has been added
     */
    public boolean hasOpinion(
    ) {
        return this._has_opinion;
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
     * Sets the value of field 'opinion'.
     * 
     * @param opinion the value of field 'opinion'.
     */
    public void setOpinion(
            final long opinion) {
        this._opinion = opinion;
        this._has_opinion = true;
    }

    /**
     * Sets the value of field 'personName'.
     * 
     * @param personName the value of field 'personName'.
     */
    public void setPersonName(
            final java.lang.String personName) {
        this._personName = personName;
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
     * org.mars_sim.msp.config.model.people.Relationship
     */
    public static org.mars_sim.msp.config.model.people.Relationship unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.people.Relationship) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.people.Relationship.class, reader);
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
