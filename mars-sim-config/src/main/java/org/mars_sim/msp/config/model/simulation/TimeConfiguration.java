/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.simulation;

/**
 * Class TimeConfiguration.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class TimeConfiguration implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _timeRatio.
     */
    private org.mars_sim.msp.config.model.simulation.TimeRatio _timeRatio;

    /**
     * Field _earthStartDateTime.
     */
    private org.mars_sim.msp.config.model.simulation.EarthStartDateTime _earthStartDateTime;

    /**
     * Field _marsStartDateTime.
     */
    private org.mars_sim.msp.config.model.simulation.MarsStartDateTime _marsStartDateTime;


      //----------------/
     //- Constructors -/
    //----------------/

    public TimeConfiguration() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'earthStartDateTime'.
     * 
     * @return the value of field 'EarthStartDateTime'.
     */
    public org.mars_sim.msp.config.model.simulation.EarthStartDateTime getEarthStartDateTime(
    ) {
        return this._earthStartDateTime;
    }

    /**
     * Returns the value of field 'marsStartDateTime'.
     * 
     * @return the value of field 'MarsStartDateTime'.
     */
    public org.mars_sim.msp.config.model.simulation.MarsStartDateTime getMarsStartDateTime(
    ) {
        return this._marsStartDateTime;
    }

    /**
     * Returns the value of field 'timeRatio'.
     * 
     * @return the value of field 'TimeRatio'.
     */
    public org.mars_sim.msp.config.model.simulation.TimeRatio getTimeRatio(
    ) {
        return this._timeRatio;
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
     * Sets the value of field 'earthStartDateTime'.
     * 
     * @param earthStartDateTime the value of field
     * 'earthStartDateTime'.
     */
    public void setEarthStartDateTime(
            final org.mars_sim.msp.config.model.simulation.EarthStartDateTime earthStartDateTime) {
        this._earthStartDateTime = earthStartDateTime;
    }

    /**
     * Sets the value of field 'marsStartDateTime'.
     * 
     * @param marsStartDateTime the value of field
     * 'marsStartDateTime'.
     */
    public void setMarsStartDateTime(
            final org.mars_sim.msp.config.model.simulation.MarsStartDateTime marsStartDateTime) {
        this._marsStartDateTime = marsStartDateTime;
    }

    /**
     * Sets the value of field 'timeRatio'.
     * 
     * @param timeRatio the value of field 'timeRatio'.
     */
    public void setTimeRatio(
            final org.mars_sim.msp.config.model.simulation.TimeRatio timeRatio) {
        this._timeRatio = timeRatio;
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
     * org.mars_sim.msp.config.model.simulation.TimeConfiguration
     */
    public static org.mars_sim.msp.config.model.simulation.TimeConfiguration unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.simulation.TimeConfiguration) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.simulation.TimeConfiguration.class, reader);
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
