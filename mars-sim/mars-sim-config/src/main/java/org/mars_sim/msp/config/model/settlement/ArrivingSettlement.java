/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.settlement;

/**
 * Class ArrivingSettlement.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class ArrivingSettlement implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _template.
     */
    private java.lang.String _template;

    /**
     * Field _arrivalTime.
     */
    private double _arrivalTime;

    /**
     * keeps track of state for field: _arrivalTime
     */
    private boolean _has_arrivalTime;

    /**
     * Field _location.
     */
    private org.mars_sim.msp.config.model.settlement.Location _location;

    /**
     * Field _population.
     */
    private org.mars_sim.msp.config.model.settlement.Population _population;


      //----------------/
     //- Constructors -/
    //----------------/

    public ArrivingSettlement() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteArrivalTime(
    ) {
        this._has_arrivalTime= false;
    }

    /**
     * Returns the value of field 'arrivalTime'.
     * 
     * @return the value of field 'ArrivalTime'.
     */
    public double getArrivalTime(
    ) {
        return this._arrivalTime;
    }

    /**
     * Returns the value of field 'location'.
     * 
     * @return the value of field 'Location'.
     */
    public org.mars_sim.msp.config.model.settlement.Location getLocation(
    ) {
        return this._location;
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
     * Returns the value of field 'population'.
     * 
     * @return the value of field 'Population'.
     */
    public org.mars_sim.msp.config.model.settlement.Population getPopulation(
    ) {
        return this._population;
    }

    /**
     * Returns the value of field 'template'.
     * 
     * @return the value of field 'Template'.
     */
    public java.lang.String getTemplate(
    ) {
        return this._template;
    }

    /**
     * Method hasArrivalTime.
     * 
     * @return true if at least one ArrivalTime has been added
     */
    public boolean hasArrivalTime(
    ) {
        return this._has_arrivalTime;
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
     * Sets the value of field 'arrivalTime'.
     * 
     * @param arrivalTime the value of field 'arrivalTime'.
     */
    public void setArrivalTime(
            final double arrivalTime) {
        this._arrivalTime = arrivalTime;
        this._has_arrivalTime = true;
    }

    /**
     * Sets the value of field 'location'.
     * 
     * @param location the value of field 'location'.
     */
    public void setLocation(
            final org.mars_sim.msp.config.model.settlement.Location location) {
        this._location = location;
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
     * Sets the value of field 'population'.
     * 
     * @param population the value of field 'population'.
     */
    public void setPopulation(
            final org.mars_sim.msp.config.model.settlement.Population population) {
        this._population = population;
    }

    /**
     * Sets the value of field 'template'.
     * 
     * @param template the value of field 'template'.
     */
    public void setTemplate(
            final java.lang.String template) {
        this._template = template;
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
     * org.mars_sim.msp.config.model.settlement.ArrivingSettlement
     */
    public static org.mars_sim.msp.config.model.settlement.ArrivingSettlement unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.settlement.ArrivingSettlement) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.settlement.ArrivingSettlement.class, reader);
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
