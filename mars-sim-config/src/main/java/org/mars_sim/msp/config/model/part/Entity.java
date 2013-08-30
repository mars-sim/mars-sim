/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.part;

/**
 * Class Entity.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Entity implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _probability.
     */
    private long _probability;

    /**
     * keeps track of state for field: _probability
     */
    private boolean _has_probability;

    /**
     * Field _maxNumber.
     */
    private long _maxNumber;

    /**
     * keeps track of state for field: _maxNumber
     */
    private boolean _has_maxNumber;


      //----------------/
     //- Constructors -/
    //----------------/

    public Entity() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteMaxNumber(
    ) {
        this._has_maxNumber= false;
    }

    /**
     */
    public void deleteProbability(
    ) {
        this._has_probability= false;
    }

    /**
     * Returns the value of field 'maxNumber'.
     * 
     * @return the value of field 'MaxNumber'.
     */
    public long getMaxNumber(
    ) {
        return this._maxNumber;
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public java.lang.String getName(
    ) {
        return this._name;
    }

    /**
     * Returns the value of field 'probability'.
     * 
     * @return the value of field 'Probability'.
     */
    public long getProbability(
    ) {
        return this._probability;
    }

    /**
     * Method hasMaxNumber.
     * 
     * @return true if at least one MaxNumber has been added
     */
    public boolean hasMaxNumber(
    ) {
        return this._has_maxNumber;
    }

    /**
     * Method hasProbability.
     * 
     * @return true if at least one Probability has been added
     */
    public boolean hasProbability(
    ) {
        return this._has_probability;
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
     * Sets the value of field 'maxNumber'.
     * 
     * @param maxNumber the value of field 'maxNumber'.
     */
    public void setMaxNumber(
            final long maxNumber) {
        this._maxNumber = maxNumber;
        this._has_maxNumber = true;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(
            final java.lang.String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'probability'.
     * 
     * @param probability the value of field 'probability'.
     */
    public void setProbability(
            final long probability) {
        this._probability = probability;
        this._has_probability = true;
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
     * org.mars_sim.msp.config.model.part.Entity
     */
    public static org.mars_sim.msp.config.model.part.Entity unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.part.Entity) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.part.Entity.class, reader);
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
