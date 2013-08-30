/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.building;

/**
 * Class EVA.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class EVA implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _airlockCapacity.
     */
    private long _airlockCapacity;

    /**
     * keeps track of state for field: _airlockCapacity
     */
    private boolean _has_airlockCapacity;


      //----------------/
     //- Constructors -/
    //----------------/

    public EVA() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteAirlockCapacity(
    ) {
        this._has_airlockCapacity= false;
    }

    /**
     * Returns the value of field 'airlockCapacity'.
     * 
     * @return the value of field 'AirlockCapacity'.
     */
    public long getAirlockCapacity(
    ) {
        return this._airlockCapacity;
    }

    /**
     * Method hasAirlockCapacity.
     * 
     * @return true if at least one AirlockCapacity has been added
     */
    public boolean hasAirlockCapacity(
    ) {
        return this._has_airlockCapacity;
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
     * Sets the value of field 'airlockCapacity'.
     * 
     * @param airlockCapacity the value of field 'airlockCapacity'.
     */
    public void setAirlockCapacity(
            final long airlockCapacity) {
        this._airlockCapacity = airlockCapacity;
        this._has_airlockCapacity = true;
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
     * org.mars_sim.msp.config.model.building.EVA
     */
    public static org.mars_sim.msp.config.model.building.EVA unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.building.EVA) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.building.EVA.class, reader);
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
