/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.people;

/**
 * Class PeopleConfiguration.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class PeopleConfiguration implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _oxygenConsumptionRate.
     */
    private org.mars_sim.msp.config.model.people.OxygenConsumptionRate _oxygenConsumptionRate;

    /**
     * Field _waterConsumptionRate.
     */
    private org.mars_sim.msp.config.model.people.WaterConsumptionRate _waterConsumptionRate;

    /**
     * Field _foodConsumptionRate.
     */
    private org.mars_sim.msp.config.model.people.FoodConsumptionRate _foodConsumptionRate;

    /**
     * Field _oxygenDeprivationTime.
     */
    private org.mars_sim.msp.config.model.people.OxygenDeprivationTime _oxygenDeprivationTime;

    /**
     * Field _waterDeprivationTime.
     */
    private org.mars_sim.msp.config.model.people.WaterDeprivationTime _waterDeprivationTime;

    /**
     * Field _foodDeprivationTime.
     */
    private org.mars_sim.msp.config.model.people.FoodDeprivationTime _foodDeprivationTime;

    /**
     * Field _starvationStartTime.
     */
    private org.mars_sim.msp.config.model.people.StarvationStartTime _starvationStartTime;

    /**
     * Field _minAirPressure.
     */
    private org.mars_sim.msp.config.model.people.MinAirPressure _minAirPressure;

    /**
     * Field _decompressionTime.
     */
    private org.mars_sim.msp.config.model.people.DecompressionTime _decompressionTime;

    /**
     * Field _minTemperature.
     */
    private org.mars_sim.msp.config.model.people.MinTemperature _minTemperature;

    /**
     * Field _maxTemperature.
     */
    private org.mars_sim.msp.config.model.people.MaxTemperature _maxTemperature;

    /**
     * Field _freezingTime.
     */
    private org.mars_sim.msp.config.model.people.FreezingTime _freezingTime;

    /**
     * Field _stressBreakdownChance.
     */
    private org.mars_sim.msp.config.model.people.StressBreakdownChance _stressBreakdownChance;

    /**
     * Field _genderMalePercentage.
     */
    private org.mars_sim.msp.config.model.people.GenderMalePercentage _genderMalePercentage;

    /**
     * Field _personalityTypes.
     */
    private org.mars_sim.msp.config.model.people.PersonalityTypes _personalityTypes;

    /**
     * Field _personNameList.
     */
    private org.mars_sim.msp.config.model.people.PersonNameList _personNameList;

    /**
     * Field _personList.
     */
    private org.mars_sim.msp.config.model.people.PersonList _personList;


      //----------------/
     //- Constructors -/
    //----------------/

    public PeopleConfiguration() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'decompressionTime'.
     * 
     * @return the value of field 'DecompressionTime'.
     */
    public org.mars_sim.msp.config.model.people.DecompressionTime getDecompressionTime(
    ) {
        return this._decompressionTime;
    }

    /**
     * Returns the value of field 'foodConsumptionRate'.
     * 
     * @return the value of field 'FoodConsumptionRate'.
     */
    public org.mars_sim.msp.config.model.people.FoodConsumptionRate getFoodConsumptionRate(
    ) {
        return this._foodConsumptionRate;
    }

    /**
     * Returns the value of field 'foodDeprivationTime'.
     * 
     * @return the value of field 'FoodDeprivationTime'.
     */
    public org.mars_sim.msp.config.model.people.FoodDeprivationTime getFoodDeprivationTime(
    ) {
        return this._foodDeprivationTime;
    }

    /**
     * Returns the value of field 'freezingTime'.
     * 
     * @return the value of field 'FreezingTime'.
     */
    public org.mars_sim.msp.config.model.people.FreezingTime getFreezingTime(
    ) {
        return this._freezingTime;
    }

    /**
     * Returns the value of field 'genderMalePercentage'.
     * 
     * @return the value of field 'GenderMalePercentage'.
     */
    public org.mars_sim.msp.config.model.people.GenderMalePercentage getGenderMalePercentage(
    ) {
        return this._genderMalePercentage;
    }

    /**
     * Returns the value of field 'maxTemperature'.
     * 
     * @return the value of field 'MaxTemperature'.
     */
    public org.mars_sim.msp.config.model.people.MaxTemperature getMaxTemperature(
    ) {
        return this._maxTemperature;
    }

    /**
     * Returns the value of field 'minAirPressure'.
     * 
     * @return the value of field 'MinAirPressure'.
     */
    public org.mars_sim.msp.config.model.people.MinAirPressure getMinAirPressure(
    ) {
        return this._minAirPressure;
    }

    /**
     * Returns the value of field 'minTemperature'.
     * 
     * @return the value of field 'MinTemperature'.
     */
    public org.mars_sim.msp.config.model.people.MinTemperature getMinTemperature(
    ) {
        return this._minTemperature;
    }

    /**
     * Returns the value of field 'oxygenConsumptionRate'.
     * 
     * @return the value of field 'OxygenConsumptionRate'.
     */
    public org.mars_sim.msp.config.model.people.OxygenConsumptionRate getOxygenConsumptionRate(
    ) {
        return this._oxygenConsumptionRate;
    }

    /**
     * Returns the value of field 'oxygenDeprivationTime'.
     * 
     * @return the value of field 'OxygenDeprivationTime'.
     */
    public org.mars_sim.msp.config.model.people.OxygenDeprivationTime getOxygenDeprivationTime(
    ) {
        return this._oxygenDeprivationTime;
    }

    /**
     * Returns the value of field 'personList'.
     * 
     * @return the value of field 'PersonList'.
     */
    public org.mars_sim.msp.config.model.people.PersonList getPersonList(
    ) {
        return this._personList;
    }

    /**
     * Returns the value of field 'personNameList'.
     * 
     * @return the value of field 'PersonNameList'.
     */
    public org.mars_sim.msp.config.model.people.PersonNameList getPersonNameList(
    ) {
        return this._personNameList;
    }

    /**
     * Returns the value of field 'personalityTypes'.
     * 
     * @return the value of field 'PersonalityTypes'.
     */
    public org.mars_sim.msp.config.model.people.PersonalityTypes getPersonalityTypes(
    ) {
        return this._personalityTypes;
    }

    /**
     * Returns the value of field 'starvationStartTime'.
     * 
     * @return the value of field 'StarvationStartTime'.
     */
    public org.mars_sim.msp.config.model.people.StarvationStartTime getStarvationStartTime(
    ) {
        return this._starvationStartTime;
    }

    /**
     * Returns the value of field 'stressBreakdownChance'.
     * 
     * @return the value of field 'StressBreakdownChance'.
     */
    public org.mars_sim.msp.config.model.people.StressBreakdownChance getStressBreakdownChance(
    ) {
        return this._stressBreakdownChance;
    }

    /**
     * Returns the value of field 'waterConsumptionRate'.
     * 
     * @return the value of field 'WaterConsumptionRate'.
     */
    public org.mars_sim.msp.config.model.people.WaterConsumptionRate getWaterConsumptionRate(
    ) {
        return this._waterConsumptionRate;
    }

    /**
     * Returns the value of field 'waterDeprivationTime'.
     * 
     * @return the value of field 'WaterDeprivationTime'.
     */
    public org.mars_sim.msp.config.model.people.WaterDeprivationTime getWaterDeprivationTime(
    ) {
        return this._waterDeprivationTime;
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
     * Sets the value of field 'decompressionTime'.
     * 
     * @param decompressionTime the value of field
     * 'decompressionTime'.
     */
    public void setDecompressionTime(
            final org.mars_sim.msp.config.model.people.DecompressionTime decompressionTime) {
        this._decompressionTime = decompressionTime;
    }

    /**
     * Sets the value of field 'foodConsumptionRate'.
     * 
     * @param foodConsumptionRate the value of field
     * 'foodConsumptionRate'.
     */
    public void setFoodConsumptionRate(
            final org.mars_sim.msp.config.model.people.FoodConsumptionRate foodConsumptionRate) {
        this._foodConsumptionRate = foodConsumptionRate;
    }

    /**
     * Sets the value of field 'foodDeprivationTime'.
     * 
     * @param foodDeprivationTime the value of field
     * 'foodDeprivationTime'.
     */
    public void setFoodDeprivationTime(
            final org.mars_sim.msp.config.model.people.FoodDeprivationTime foodDeprivationTime) {
        this._foodDeprivationTime = foodDeprivationTime;
    }

    /**
     * Sets the value of field 'freezingTime'.
     * 
     * @param freezingTime the value of field 'freezingTime'.
     */
    public void setFreezingTime(
            final org.mars_sim.msp.config.model.people.FreezingTime freezingTime) {
        this._freezingTime = freezingTime;
    }

    /**
     * Sets the value of field 'genderMalePercentage'.
     * 
     * @param genderMalePercentage the value of field
     * 'genderMalePercentage'.
     */
    public void setGenderMalePercentage(
            final org.mars_sim.msp.config.model.people.GenderMalePercentage genderMalePercentage) {
        this._genderMalePercentage = genderMalePercentage;
    }

    /**
     * Sets the value of field 'maxTemperature'.
     * 
     * @param maxTemperature the value of field 'maxTemperature'.
     */
    public void setMaxTemperature(
            final org.mars_sim.msp.config.model.people.MaxTemperature maxTemperature) {
        this._maxTemperature = maxTemperature;
    }

    /**
     * Sets the value of field 'minAirPressure'.
     * 
     * @param minAirPressure the value of field 'minAirPressure'.
     */
    public void setMinAirPressure(
            final org.mars_sim.msp.config.model.people.MinAirPressure minAirPressure) {
        this._minAirPressure = minAirPressure;
    }

    /**
     * Sets the value of field 'minTemperature'.
     * 
     * @param minTemperature the value of field 'minTemperature'.
     */
    public void setMinTemperature(
            final org.mars_sim.msp.config.model.people.MinTemperature minTemperature) {
        this._minTemperature = minTemperature;
    }

    /**
     * Sets the value of field 'oxygenConsumptionRate'.
     * 
     * @param oxygenConsumptionRate the value of field
     * 'oxygenConsumptionRate'.
     */
    public void setOxygenConsumptionRate(
            final org.mars_sim.msp.config.model.people.OxygenConsumptionRate oxygenConsumptionRate) {
        this._oxygenConsumptionRate = oxygenConsumptionRate;
    }

    /**
     * Sets the value of field 'oxygenDeprivationTime'.
     * 
     * @param oxygenDeprivationTime the value of field
     * 'oxygenDeprivationTime'.
     */
    public void setOxygenDeprivationTime(
            final org.mars_sim.msp.config.model.people.OxygenDeprivationTime oxygenDeprivationTime) {
        this._oxygenDeprivationTime = oxygenDeprivationTime;
    }

    /**
     * Sets the value of field 'personList'.
     * 
     * @param personList the value of field 'personList'.
     */
    public void setPersonList(
            final org.mars_sim.msp.config.model.people.PersonList personList) {
        this._personList = personList;
    }

    /**
     * Sets the value of field 'personNameList'.
     * 
     * @param personNameList the value of field 'personNameList'.
     */
    public void setPersonNameList(
            final org.mars_sim.msp.config.model.people.PersonNameList personNameList) {
        this._personNameList = personNameList;
    }

    /**
     * Sets the value of field 'personalityTypes'.
     * 
     * @param personalityTypes the value of field 'personalityTypes'
     */
    public void setPersonalityTypes(
            final org.mars_sim.msp.config.model.people.PersonalityTypes personalityTypes) {
        this._personalityTypes = personalityTypes;
    }

    /**
     * Sets the value of field 'starvationStartTime'.
     * 
     * @param starvationStartTime the value of field
     * 'starvationStartTime'.
     */
    public void setStarvationStartTime(
            final org.mars_sim.msp.config.model.people.StarvationStartTime starvationStartTime) {
        this._starvationStartTime = starvationStartTime;
    }

    /**
     * Sets the value of field 'stressBreakdownChance'.
     * 
     * @param stressBreakdownChance the value of field
     * 'stressBreakdownChance'.
     */
    public void setStressBreakdownChance(
            final org.mars_sim.msp.config.model.people.StressBreakdownChance stressBreakdownChance) {
        this._stressBreakdownChance = stressBreakdownChance;
    }

    /**
     * Sets the value of field 'waterConsumptionRate'.
     * 
     * @param waterConsumptionRate the value of field
     * 'waterConsumptionRate'.
     */
    public void setWaterConsumptionRate(
            final org.mars_sim.msp.config.model.people.WaterConsumptionRate waterConsumptionRate) {
        this._waterConsumptionRate = waterConsumptionRate;
    }

    /**
     * Sets the value of field 'waterDeprivationTime'.
     * 
     * @param waterDeprivationTime the value of field
     * 'waterDeprivationTime'.
     */
    public void setWaterDeprivationTime(
            final org.mars_sim.msp.config.model.people.WaterDeprivationTime waterDeprivationTime) {
        this._waterDeprivationTime = waterDeprivationTime;
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
     * org.mars_sim.msp.config.model.people.PeopleConfiguration
     */
    public static org.mars_sim.msp.config.model.people.PeopleConfiguration unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.people.PeopleConfiguration) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.people.PeopleConfiguration.class, reader);
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
