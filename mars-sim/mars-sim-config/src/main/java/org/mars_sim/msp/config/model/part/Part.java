/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.part;

/**
 * Class Part.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Part implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _mass.
     */
    private double _mass;

    /**
     * keeps track of state for field: _mass
     */
    private boolean _has_mass;

    /**
     * Field _maintenanceEntityList.
     */
    private org.mars_sim.msp.config.model.part.MaintenanceEntityList _maintenanceEntityList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Part() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteMass(
    ) {
        this._has_mass= false;
    }

    /**
     * Returns the value of field 'maintenanceEntityList'.
     * 
     * @return the value of field 'MaintenanceEntityList'.
     */
    public org.mars_sim.msp.config.model.part.MaintenanceEntityList getMaintenanceEntityList(
    ) {
        return this._maintenanceEntityList;
    }

    /**
     * Returns the value of field 'mass'.
     * 
     * @return the value of field 'Mass'.
     */
    public double getMass(
    ) {
        return this._mass;
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
     * Method hasMass.
     * 
     * @return true if at least one Mass has been added
     */
    public boolean hasMass(
    ) {
        return this._has_mass;
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
     * Sets the value of field 'maintenanceEntityList'.
     * 
     * @param maintenanceEntityList the value of field
     * 'maintenanceEntityList'.
     */
    public void setMaintenanceEntityList(
            final org.mars_sim.msp.config.model.part.MaintenanceEntityList maintenanceEntityList) {
        this._maintenanceEntityList = maintenanceEntityList;
    }

    /**
     * Sets the value of field 'mass'.
     * 
     * @param mass the value of field 'mass'.
     */
    public void setMass(
            final double mass) {
        this._mass = mass;
        this._has_mass = true;
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
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.mars_sim.msp.config.model.part.Part
     */
    public static org.mars_sim.msp.config.model.part.Part unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.part.Part) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.part.Part.class, reader);
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
