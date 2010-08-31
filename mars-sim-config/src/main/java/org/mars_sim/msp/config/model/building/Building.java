/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.building;

/**
 * Class Building.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Building implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _width.
     */
    private float _width;

    /**
     * keeps track of state for field: _width
     */
    private boolean _has_width;

    /**
     * Field _length.
     */
    private float _length;

    /**
     * keeps track of state for field: _length
     */
    private boolean _has_length;

    /**
     * Field _enabled.
     */
    private boolean _enabled;

    /**
     * keeps track of state for field: _enabled
     */
    private boolean _has_enabled;

    /**
     * Field _editable.
     */
    private boolean _editable;

    /**
     * keeps track of state for field: _editable
     */
    private boolean _has_editable;

    /**
     * Field _powerRequired.
     */
    private org.mars_sim.msp.config.model.building.PowerRequired _powerRequired;

    /**
     * Field _functions.
     */
    private org.mars_sim.msp.config.model.building.Functions _functions;


      //----------------/
     //- Constructors -/
    //----------------/

    public Building() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteEditable(
    ) {
        this._has_editable= false;
    }

    /**
     */
    public void deleteEnabled(
    ) {
        this._has_enabled= false;
    }

    /**
     */
    public void deleteLength(
    ) {
        this._has_length= false;
    }

    /**
     */
    public void deleteWidth(
    ) {
        this._has_width= false;
    }

    /**
     * Returns the value of field 'editable'.
     * 
     * @return the value of field 'Editable'.
     */
    public boolean getEditable(
    ) {
        return this._editable;
    }

    /**
     * Returns the value of field 'enabled'.
     * 
     * @return the value of field 'Enabled'.
     */
    public boolean getEnabled(
    ) {
        return this._enabled;
    }

    /**
     * Returns the value of field 'functions'.
     * 
     * @return the value of field 'Functions'.
     */
    public org.mars_sim.msp.config.model.building.Functions getFunctions(
    ) {
        return this._functions;
    }

    /**
     * Returns the value of field 'length'.
     * 
     * @return the value of field 'Length'.
     */
    public float getLength(
    ) {
        return this._length;
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
     * Returns the value of field 'powerRequired'.
     * 
     * @return the value of field 'PowerRequired'.
     */
    public org.mars_sim.msp.config.model.building.PowerRequired getPowerRequired(
    ) {
        return this._powerRequired;
    }

    /**
     * Returns the value of field 'width'.
     * 
     * @return the value of field 'Width'.
     */
    public float getWidth(
    ) {
        return this._width;
    }

    /**
     * Method hasEditable.
     * 
     * @return true if at least one Editable has been added
     */
    public boolean hasEditable(
    ) {
        return this._has_editable;
    }

    /**
     * Method hasEnabled.
     * 
     * @return true if at least one Enabled has been added
     */
    public boolean hasEnabled(
    ) {
        return this._has_enabled;
    }

    /**
     * Method hasLength.
     * 
     * @return true if at least one Length has been added
     */
    public boolean hasLength(
    ) {
        return this._has_length;
    }

    /**
     * Method hasWidth.
     * 
     * @return true if at least one Width has been added
     */
    public boolean hasWidth(
    ) {
        return this._has_width;
    }

    /**
     * Returns the value of field 'editable'.
     * 
     * @return the value of field 'Editable'.
     */
    public boolean isEditable(
    ) {
        return this._editable;
    }

    /**
     * Returns the value of field 'enabled'.
     * 
     * @return the value of field 'Enabled'.
     */
    public boolean isEnabled(
    ) {
        return this._enabled;
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
     * Sets the value of field 'editable'.
     * 
     * @param editable the value of field 'editable'.
     */
    public void setEditable(
            final boolean editable) {
        this._editable = editable;
        this._has_editable = true;
    }

    /**
     * Sets the value of field 'enabled'.
     * 
     * @param enabled the value of field 'enabled'.
     */
    public void setEnabled(
            final boolean enabled) {
        this._enabled = enabled;
        this._has_enabled = true;
    }

    /**
     * Sets the value of field 'functions'.
     * 
     * @param functions the value of field 'functions'.
     */
    public void setFunctions(
            final org.mars_sim.msp.config.model.building.Functions functions) {
        this._functions = functions;
    }

    /**
     * Sets the value of field 'length'.
     * 
     * @param length the value of field 'length'.
     */
    public void setLength(
            final float length) {
        this._length = length;
        this._has_length = true;
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
     * Sets the value of field 'powerRequired'.
     * 
     * @param powerRequired the value of field 'powerRequired'.
     */
    public void setPowerRequired(
            final org.mars_sim.msp.config.model.building.PowerRequired powerRequired) {
        this._powerRequired = powerRequired;
    }

    /**
     * Sets the value of field 'width'.
     * 
     * @param width the value of field 'width'.
     */
    public void setWidth(
            final float width) {
        this._width = width;
        this._has_width = true;
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
     * org.mars_sim.msp.config.model.building.Building
     */
    public static org.mars_sim.msp.config.model.building.Building unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.building.Building) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.building.Building.class, reader);
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
