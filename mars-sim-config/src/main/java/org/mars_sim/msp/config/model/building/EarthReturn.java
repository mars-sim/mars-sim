/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.building;

/**
 * Class EarthReturn.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class EarthReturn implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _crewCapacity.
     */
    private long _crewCapacity;

    /**
     * keeps track of state for field: _crewCapacity
     */
    private boolean _has_crewCapacity;


      //----------------/
     //- Constructors -/
    //----------------/

    public EarthReturn() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteCrewCapacity(
    ) {
        this._has_crewCapacity= false;
    }

    /**
     * Returns the value of field 'crewCapacity'.
     * 
     * @return the value of field 'CrewCapacity'.
     */
    public long getCrewCapacity(
    ) {
        return this._crewCapacity;
    }

    /**
     * Method hasCrewCapacity.
     * 
     * @return true if at least one CrewCapacity has been added
     */
    public boolean hasCrewCapacity(
    ) {
        return this._has_crewCapacity;
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
     * Sets the value of field 'crewCapacity'.
     * 
     * @param crewCapacity the value of field 'crewCapacity'.
     */
    public void setCrewCapacity(
            final long crewCapacity) {
        this._crewCapacity = crewCapacity;
        this._has_crewCapacity = true;
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
     * org.mars_sim.msp.config.model.building.EarthReturn
     */
    public static org.mars_sim.msp.config.model.building.EarthReturn unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.building.EarthReturn) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.building.EarthReturn.class, reader);
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
