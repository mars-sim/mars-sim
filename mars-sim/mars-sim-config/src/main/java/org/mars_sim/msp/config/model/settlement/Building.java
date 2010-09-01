/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.settlement;

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
     * Field _type.
     */
    private java.lang.String _type;

    /**
     * Field _xLocation.
     */
    private double _xLocation;

    /**
     * keeps track of state for field: _xLocation
     */
    private boolean _has_xLocation;

    /**
     * Field _yLocation.
     */
    private double _yLocation;

    /**
     * keeps track of state for field: _yLocation
     */
    private boolean _has_yLocation;

    /**
     * Field _facing.
     */
    private double _facing;

    /**
     * keeps track of state for field: _facing
     */
    private boolean _has_facing;


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
    public void deleteFacing(
    ) {
        this._has_facing= false;
    }

    /**
     */
    public void deleteXLocation(
    ) {
        this._has_xLocation= false;
    }

    /**
     */
    public void deleteYLocation(
    ) {
        this._has_yLocation= false;
    }

    /**
     * Returns the value of field 'facing'.
     * 
     * @return the value of field 'Facing'.
     */
    public double getFacing(
    ) {
        return this._facing;
    }

    /**
     * Returns the value of field 'type'.
     * 
     * @return the value of field 'Type'.
     */
    public java.lang.String getType(
    ) {
        return this._type;
    }

    /**
     * Returns the value of field 'xLocation'.
     * 
     * @return the value of field 'XLocation'.
     */
    public double getXLocation(
    ) {
        return this._xLocation;
    }

    /**
     * Returns the value of field 'yLocation'.
     * 
     * @return the value of field 'YLocation'.
     */
    public double getYLocation(
    ) {
        return this._yLocation;
    }

    /**
     * Method hasFacing.
     * 
     * @return true if at least one Facing has been added
     */
    public boolean hasFacing(
    ) {
        return this._has_facing;
    }

    /**
     * Method hasXLocation.
     * 
     * @return true if at least one XLocation has been added
     */
    public boolean hasXLocation(
    ) {
        return this._has_xLocation;
    }

    /**
     * Method hasYLocation.
     * 
     * @return true if at least one YLocation has been added
     */
    public boolean hasYLocation(
    ) {
        return this._has_yLocation;
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
     * Sets the value of field 'facing'.
     * 
     * @param facing the value of field 'facing'.
     */
    public void setFacing(
            final double facing) {
        this._facing = facing;
        this._has_facing = true;
    }

    /**
     * Sets the value of field 'type'.
     * 
     * @param type the value of field 'type'.
     */
    public void setType(
            final java.lang.String type) {
        this._type = type;
    }

    /**
     * Sets the value of field 'xLocation'.
     * 
     * @param xLocation the value of field 'xLocation'.
     */
    public void setXLocation(
            final double xLocation) {
        this._xLocation = xLocation;
        this._has_xLocation = true;
    }

    /**
     * Sets the value of field 'yLocation'.
     * 
     * @param yLocation the value of field 'yLocation'.
     */
    public void setYLocation(
            final double yLocation) {
        this._yLocation = yLocation;
        this._has_yLocation = true;
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
     * org.mars_sim.msp.config.model.settlement.Building
     */
    public static org.mars_sim.msp.config.model.settlement.Building unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.settlement.Building) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.settlement.Building.class, reader);
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
