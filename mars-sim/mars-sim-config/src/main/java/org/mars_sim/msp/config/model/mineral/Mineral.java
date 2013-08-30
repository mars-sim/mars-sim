/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.mineral;

/**
 * Class Mineral.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Mineral implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _frequency.
     */
    private org.mars_sim.msp.config.model.types.MineralFrequencyType _frequency;

    /**
     * Field _localeList.
     */
    private org.mars_sim.msp.config.model.mineral.LocaleList _localeList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Mineral() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'frequency'.
     * 
     * @return the value of field 'Frequency'.
     */
    public org.mars_sim.msp.config.model.types.MineralFrequencyType getFrequency(
    ) {
        return this._frequency;
    }

    /**
     * Returns the value of field 'localeList'.
     * 
     * @return the value of field 'LocaleList'.
     */
    public org.mars_sim.msp.config.model.mineral.LocaleList getLocaleList(
    ) {
        return this._localeList;
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
     * Sets the value of field 'frequency'.
     * 
     * @param frequency the value of field 'frequency'.
     */
    public void setFrequency(
            final org.mars_sim.msp.config.model.types.MineralFrequencyType frequency) {
        this._frequency = frequency;
    }

    /**
     * Sets the value of field 'localeList'.
     * 
     * @param localeList the value of field 'localeList'.
     */
    public void setLocaleList(
            final org.mars_sim.msp.config.model.mineral.LocaleList localeList) {
        this._localeList = localeList;
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
     * org.mars_sim.msp.config.model.mineral.Mineral
     */
    public static org.mars_sim.msp.config.model.mineral.Mineral unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.mineral.Mineral) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.mineral.Mineral.class, reader);
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
