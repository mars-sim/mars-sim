/**
 * Mars Simulation Project
 * SimulationProperties.java
 * @version 2.74 2002-03-10
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The SimulationProperties class contains user-defined properties
 *  for the simulation.
 */
public class SimulationProperties {

    // Data members
    private PropertiesXmlReader propertiesReader; // The XML properties reader

    // Property values
    private double timeRatio = 0D; // Simulation time / real time
    private int initSettlements = 0;

    /** Constructor */
    public SimulationProperties() {

        // Create a PropertiesXmlReader.
        propertiesReader = new PropertiesXmlReader();
        propertiesReader.parse();
    }

    /** Number of settlements when starting
     */
    public int getInitSettlements() {
	if(initSettlements == 0) {
	    initSettlements = propertiesReader.getInitSettlements();
	}
	return initSettlements;
    }

    /** Number of settlements when starting
     */
    public void setInitSettlements(int newValue) {
	initSettlements = newValue;
    }

    /** Gets the time ratio property.
     *  Default value is 1000.0.
     *  @return the ration between simulation and real time
     */
    public double getTimeRatio() {
        if (timeRatio > 0D) return timeRatio;
        else return propertiesReader.getTimeRatio();
    }

    /** Sets the time ratio property.
     *  Value must be > 0.
     *  @param timeRatio simulation time / real time
     */
    public void setTimeRatio(double newTimeRatio) {
        if (newTimeRatio > 0) timeRatio = newTimeRatio;
    }

    /** 
     * Gets the airlock pressurization/depressurization time.
     * Value must be > 0.
     * Default value is 10 millisols.
     * @return airlock cycle time in millisols.
     */
    public double getAirlockCycleTime() {
        return propertiesReader.getAirlockCycleTime();
    }

    /** Gets the how long a Person can survive without oxygen in sol.
     *  Value must be >= 0.
     *
     *  @return the time period a person survies property
     */
    public double getPersonLackOfOxygenPeriod() {
        return propertiesReader.getPersonLackOfOxygenPeriod();
    }

    /** Gets the person oxygen consumption property.
     *  Value must be >= 0.
     *  Default value is 1.0 kg/sol.
     *  @return the person oxygen consumption property
     */
    public double getPersonOxygenConsumption() {
        return propertiesReader.getPersonOxygenConsumption();
    }

    /** Gets the  time a Person can survive without Water in sols.
     *  Value must be >= 0.
     *
     *  @return the persion a person can survive.
     */
    public double getPersonLackOfWaterPeriod() {
        return propertiesReader.getPersonLackOfWaterPeriod();
    }

    /** Gets the person water consumption property.
     *  Value must be >= 0.
     *  Default value is 4.0 kg/sol.
     *  @return the person water consumption property
     */
    public double getPersonWaterConsumption() {
        return propertiesReader.getPersonWaterConsumption();
    }

    /** Gets the period a person can survive without food in sols
     *  Value must be >= 0.
     *  @return the person food period property
     */
    public double getPersonLackOfFoodPeriod() {
        return propertiesReader.getPersonLackOfFoodPeriod();
    }

    /** Gets the person food consumption property.
     *  Value must be >= 0.
     *  Default value is 1.5 kg/sol.
     *  @return the person food consumption property
     */
    public double getPersonFoodConsumption() {
        return propertiesReader.getPersonFoodConsumption();
    }

    /**
     * Gets the person minimum air pressure property.
     * Default value is .25 atm.
     * @return the person minimum air pressure property
     */
    public double getPersonMinAirPressure() {
        return propertiesReader.getPersonMinAirPressure();
    }

    /**
     * Gets the person decompression time property.
     * Value must be >= 0.
     * Default value is 90.0 seconds.
     * @return person decompression time property
     */
    public double getPersonDecompressionTime() {
        return propertiesReader.getPersonDecompressionTime();
    }

    /**
     * Gets the person minimum temperature property.
     * Default value is 0 degrees Celsius.
     * @return person minimum temperature property
     */
    public double getPersonMinTemperature() {
        return propertiesReader.getPersonMinTemperature();
    }

    /**
     * Gets the person freezing time property.
     * Value must be >= 0.
     * Default value is 240.0 minutes.
     * @return person freezing time property
     */
    public double getPersonFreezingTime() {
        return propertiesReader.getPersonFreezingTime();
    }
    
    /** Gets the rover fuel efficiency property.
     *  Value must be > 0.
     *  Default value is 2.0 km/kg.
     *  @return the rover fuel efficiency property
     */
    public double getRoverFuelEfficiency() {
        return propertiesReader.getRoverFuelEfficiency();
    }

    /** Gets the rover range property.
     *  Value must be >= 0.
     *  Default value is 4000.0 km.
     *  @return the rover range property
     */
    public double getRoverRange() {
        return propertiesReader.getRoverRange();
    }

    /** Gets the settlement oxygen storage capacity property.
     *  Value must be >= 0.
     *  Default value is 10000.0 kg.
     *  @return the settlement oxygen storage capacity property
     */
    public double getSettlementOxygenStorageCapacity() {
        return propertiesReader.getSettlementOxygenStorageCapacity();
    }

    /** Gets the settlement water storage capacity property.
     *  Value must be >= 0.
     *  Default value is 10000.0 kg.
     *  @return the settlement water storage capacity property
     */
    public double getSettlementWaterStorageCapacity() {
        return propertiesReader.getSettlementWaterStorageCapacity();
    }

    /** Gets the settlement food storage capacity property.
     *  Value must be >= 0.
     *  Default value is 10000.0 kg.
     *  @return the settlement food storage capacity property
     */
    public double getSettlementFoodStorageCapacity() {
        return propertiesReader.getSettlementFoodStorageCapacity();
    }

    /** Gets the settlement fuel storage capacity property.
     *  Value must be >= 0.
     *  Default value is 10000.0 kg.
     *  @return the settlement fuel storage capacity property
     */
    public double getSettlementFuelStorageCapacity() {
        return propertiesReader.getSettlementFuelStorageCapacity();
    }

    /** Gets the greenhouse full harvest property.
     *  Value must be >= 0.
     *  Default value is 200.0 kg.
     *  @return the greenhouse full harvest property
     */
    public double getGreenhouseFullHarvest() {
        return propertiesReader.getGreenhouseFullHarvest();
    }

    /** Gets the greenhouse growing cycle property.
     *  Value must be >= 0.
     *  Default value is 10000.0 millisols.
     *  @return the greenhouse growing cycle property
     */
    public double getGreenhouseGrowingCycle() {
        return propertiesReader.getGreenhouseGrowingCycle();
    }
}
