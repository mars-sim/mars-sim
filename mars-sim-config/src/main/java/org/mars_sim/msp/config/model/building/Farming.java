/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.building;

/**
 * Class Farming.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Farming implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _crops.
     */
    private long _crops;

    /**
     * keeps track of state for field: _crops
     */
    private boolean _has_crops;

    /**
     * Field _powerGrowingCrop.
     */
    private float _powerGrowingCrop;

    /**
     * keeps track of state for field: _powerGrowingCrop
     */
    private boolean _has_powerGrowingCrop;

    /**
     * Field _powerSustainingCrop.
     */
    private float _powerSustainingCrop;

    /**
     * keeps track of state for field: _powerSustainingCrop
     */
    private boolean _has_powerSustainingCrop;

    /**
     * Field _growingArea.
     */
    private float _growingArea;

    /**
     * keeps track of state for field: _growingArea
     */
    private boolean _has_growingArea;


      //----------------/
     //- Constructors -/
    //----------------/

    public Farming() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteCrops(
    ) {
        this._has_crops= false;
    }

    /**
     */
    public void deleteGrowingArea(
    ) {
        this._has_growingArea= false;
    }

    /**
     */
    public void deletePowerGrowingCrop(
    ) {
        this._has_powerGrowingCrop= false;
    }

    /**
     */
    public void deletePowerSustainingCrop(
    ) {
        this._has_powerSustainingCrop= false;
    }

    /**
     * Returns the value of field 'crops'.
     * 
     * @return the value of field 'Crops'.
     */
    public long getCrops(
    ) {
        return this._crops;
    }

    /**
     * Returns the value of field 'growingArea'.
     * 
     * @return the value of field 'GrowingArea'.
     */
    public float getGrowingArea(
    ) {
        return this._growingArea;
    }

    /**
     * Returns the value of field 'powerGrowingCrop'.
     * 
     * @return the value of field 'PowerGrowingCrop'.
     */
    public float getPowerGrowingCrop(
    ) {
        return this._powerGrowingCrop;
    }

    /**
     * Returns the value of field 'powerSustainingCrop'.
     * 
     * @return the value of field 'PowerSustainingCrop'.
     */
    public float getPowerSustainingCrop(
    ) {
        return this._powerSustainingCrop;
    }

    /**
     * Method hasCrops.
     * 
     * @return true if at least one Crops has been added
     */
    public boolean hasCrops(
    ) {
        return this._has_crops;
    }

    /**
     * Method hasGrowingArea.
     * 
     * @return true if at least one GrowingArea has been added
     */
    public boolean hasGrowingArea(
    ) {
        return this._has_growingArea;
    }

    /**
     * Method hasPowerGrowingCrop.
     * 
     * @return true if at least one PowerGrowingCrop has been added
     */
    public boolean hasPowerGrowingCrop(
    ) {
        return this._has_powerGrowingCrop;
    }

    /**
     * Method hasPowerSustainingCrop.
     * 
     * @return true if at least one PowerSustainingCrop has been
     * added
     */
    public boolean hasPowerSustainingCrop(
    ) {
        return this._has_powerSustainingCrop;
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
     * Sets the value of field 'crops'.
     * 
     * @param crops the value of field 'crops'.
     */
    public void setCrops(
            final long crops) {
        this._crops = crops;
        this._has_crops = true;
    }

    /**
     * Sets the value of field 'growingArea'.
     * 
     * @param growingArea the value of field 'growingArea'.
     */
    public void setGrowingArea(
            final float growingArea) {
        this._growingArea = growingArea;
        this._has_growingArea = true;
    }

    /**
     * Sets the value of field 'powerGrowingCrop'.
     * 
     * @param powerGrowingCrop the value of field 'powerGrowingCrop'
     */
    public void setPowerGrowingCrop(
            final float powerGrowingCrop) {
        this._powerGrowingCrop = powerGrowingCrop;
        this._has_powerGrowingCrop = true;
    }

    /**
     * Sets the value of field 'powerSustainingCrop'.
     * 
     * @param powerSustainingCrop the value of field
     * 'powerSustainingCrop'.
     */
    public void setPowerSustainingCrop(
            final float powerSustainingCrop) {
        this._powerSustainingCrop = powerSustainingCrop;
        this._has_powerSustainingCrop = true;
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
     * org.mars_sim.msp.config.model.building.Farming
     */
    public static org.mars_sim.msp.config.model.building.Farming unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.building.Farming) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.building.Farming.class, reader);
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
