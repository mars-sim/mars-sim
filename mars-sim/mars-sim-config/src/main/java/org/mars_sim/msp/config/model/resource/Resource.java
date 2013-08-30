/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.resource;

/**
 * Class Resource.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Resource implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _phase.
     */
    private org.mars_sim.msp.config.model.types.ResourcePhaseType _phase;

    /**
     * Field _lifeSupport.
     */
    private boolean _lifeSupport;

    /**
     * keeps track of state for field: _lifeSupport
     */
    private boolean _has_lifeSupport;


      //----------------/
     //- Constructors -/
    //----------------/

    public Resource() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteLifeSupport(
    ) {
        this._has_lifeSupport= false;
    }

    /**
     * Returns the value of field 'lifeSupport'.
     * 
     * @return the value of field 'LifeSupport'.
     */
    public boolean getLifeSupport(
    ) {
        return this._lifeSupport;
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
     * Returns the value of field 'phase'.
     * 
     * @return the value of field 'Phase'.
     */
    public org.mars_sim.msp.config.model.types.ResourcePhaseType getPhase(
    ) {
        return this._phase;
    }

    /**
     * Method hasLifeSupport.
     * 
     * @return true if at least one LifeSupport has been added
     */
    public boolean hasLifeSupport(
    ) {
        return this._has_lifeSupport;
    }

    /**
     * Returns the value of field 'lifeSupport'.
     * 
     * @return the value of field 'LifeSupport'.
     */
    public boolean isLifeSupport(
    ) {
        return this._lifeSupport;
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
     * Sets the value of field 'lifeSupport'.
     * 
     * @param lifeSupport the value of field 'lifeSupport'.
     */
    public void setLifeSupport(
            final boolean lifeSupport) {
        this._lifeSupport = lifeSupport;
        this._has_lifeSupport = true;
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
     * Sets the value of field 'phase'.
     * 
     * @param phase the value of field 'phase'.
     */
    public void setPhase(
            final org.mars_sim.msp.config.model.types.ResourcePhaseType phase) {
        this._phase = phase;
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
     * org.mars_sim.msp.config.model.resource.Resource
     */
    public static org.mars_sim.msp.config.model.resource.Resource unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.resource.Resource) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.resource.Resource.class, reader);
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
