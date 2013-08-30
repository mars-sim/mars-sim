/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.settlement;

/**
 * Class SettlementConfiguration.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class SettlementConfiguration implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _settlementTemplateList.
     */
    private org.mars_sim.msp.config.model.settlement.SettlementTemplateList _settlementTemplateList;

    /**
     * Field _initialSettlementList.
     */
    private org.mars_sim.msp.config.model.settlement.InitialSettlementList _initialSettlementList;

    /**
     * Field _newArrivingSettlementList.
     */
    private org.mars_sim.msp.config.model.settlement.NewArrivingSettlementList _newArrivingSettlementList;

    /**
     * Field _settlementNameList.
     */
    private org.mars_sim.msp.config.model.settlement.SettlementNameList _settlementNameList;


      //----------------/
     //- Constructors -/
    //----------------/

    public SettlementConfiguration() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'initialSettlementList'.
     * 
     * @return the value of field 'InitialSettlementList'.
     */
    public org.mars_sim.msp.config.model.settlement.InitialSettlementList getInitialSettlementList(
    ) {
        return this._initialSettlementList;
    }

    /**
     * Returns the value of field 'newArrivingSettlementList'.
     * 
     * @return the value of field 'NewArrivingSettlementList'.
     */
    public org.mars_sim.msp.config.model.settlement.NewArrivingSettlementList getNewArrivingSettlementList(
    ) {
        return this._newArrivingSettlementList;
    }

    /**
     * Returns the value of field 'settlementNameList'.
     * 
     * @return the value of field 'SettlementNameList'.
     */
    public org.mars_sim.msp.config.model.settlement.SettlementNameList getSettlementNameList(
    ) {
        return this._settlementNameList;
    }

    /**
     * Returns the value of field 'settlementTemplateList'.
     * 
     * @return the value of field 'SettlementTemplateList'.
     */
    public org.mars_sim.msp.config.model.settlement.SettlementTemplateList getSettlementTemplateList(
    ) {
        return this._settlementTemplateList;
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
     * Sets the value of field 'initialSettlementList'.
     * 
     * @param initialSettlementList the value of field
     * 'initialSettlementList'.
     */
    public void setInitialSettlementList(
            final org.mars_sim.msp.config.model.settlement.InitialSettlementList initialSettlementList) {
        this._initialSettlementList = initialSettlementList;
    }

    /**
     * Sets the value of field 'newArrivingSettlementList'.
     * 
     * @param newArrivingSettlementList the value of field
     * 'newArrivingSettlementList'.
     */
    public void setNewArrivingSettlementList(
            final org.mars_sim.msp.config.model.settlement.NewArrivingSettlementList newArrivingSettlementList) {
        this._newArrivingSettlementList = newArrivingSettlementList;
    }

    /**
     * Sets the value of field 'settlementNameList'.
     * 
     * @param settlementNameList the value of field
     * 'settlementNameList'.
     */
    public void setSettlementNameList(
            final org.mars_sim.msp.config.model.settlement.SettlementNameList settlementNameList) {
        this._settlementNameList = settlementNameList;
    }

    /**
     * Sets the value of field 'settlementTemplateList'.
     * 
     * @param settlementTemplateList the value of field
     * 'settlementTemplateList'.
     */
    public void setSettlementTemplateList(
            final org.mars_sim.msp.config.model.settlement.SettlementTemplateList settlementTemplateList) {
        this._settlementTemplateList = settlementTemplateList;
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
     * org.mars_sim.msp.config.model.settlement.SettlementConfiguration
     */
    public static org.mars_sim.msp.config.model.settlement.SettlementConfiguration unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.settlement.SettlementConfiguration) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.settlement.SettlementConfiguration.class, reader);
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
