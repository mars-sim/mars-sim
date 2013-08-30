/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.construction;

/**
 * Class Construction.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Construction implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _foundationList.
     */
    private org.mars_sim.msp.config.model.construction.FoundationList _foundationList;

    /**
     * Field _frameList.
     */
    private org.mars_sim.msp.config.model.construction.FrameList _frameList;

    /**
     * Field _buildingList.
     */
    private org.mars_sim.msp.config.model.construction.BuildingList _buildingList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Construction() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'buildingList'.
     * 
     * @return the value of field 'BuildingList'.
     */
    public org.mars_sim.msp.config.model.construction.BuildingList getBuildingList(
    ) {
        return this._buildingList;
    }

    /**
     * Returns the value of field 'foundationList'.
     * 
     * @return the value of field 'FoundationList'.
     */
    public org.mars_sim.msp.config.model.construction.FoundationList getFoundationList(
    ) {
        return this._foundationList;
    }

    /**
     * Returns the value of field 'frameList'.
     * 
     * @return the value of field 'FrameList'.
     */
    public org.mars_sim.msp.config.model.construction.FrameList getFrameList(
    ) {
        return this._frameList;
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
     * Sets the value of field 'buildingList'.
     * 
     * @param buildingList the value of field 'buildingList'.
     */
    public void setBuildingList(
            final org.mars_sim.msp.config.model.construction.BuildingList buildingList) {
        this._buildingList = buildingList;
    }

    /**
     * Sets the value of field 'foundationList'.
     * 
     * @param foundationList the value of field 'foundationList'.
     */
    public void setFoundationList(
            final org.mars_sim.msp.config.model.construction.FoundationList foundationList) {
        this._foundationList = foundationList;
    }

    /**
     * Sets the value of field 'frameList'.
     * 
     * @param frameList the value of field 'frameList'.
     */
    public void setFrameList(
            final org.mars_sim.msp.config.model.construction.FrameList frameList) {
        this._frameList = frameList;
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
     * org.mars_sim.msp.config.model.construction.Construction
     */
    public static org.mars_sim.msp.config.model.construction.Construction unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.construction.Construction) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.construction.Construction.class, reader);
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
