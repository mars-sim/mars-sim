/**
 * Mars Simulation Project
 * SimulationProperties.java
 * @version 2.73 2001-12-09
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

    /** Constructor */
    public SimulationProperties() {
   
        // Create a PropertiesXmlReader. 
        propertiesReader = new PropertiesXmlReader();
        propertiesReader.parse();
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

    /** Gets the person oxygen consumption property.
     *  Value must be >= 0.
     *  Default value is 1.0 kg/sol.
     *  @return the person oxygen consumption property
     */
    public double getPersonOxygenConsumption() {
        return propertiesReader.getPersonOxygenConsumption();
    }

    /** Gets the person water consumption property.
     *  Value must be >= 0.
     *  Default value is 4.0 kg/sol.
     *  @return the person water consumption property
     */
    public double getPersonWaterConsumption() {
        return propertiesReader.getPersonWaterConsumption();
    }

    /** Gets the person food consumption property.
     *  Value must be >= 0.
     *  Default value is 1.5 kg/sol.
     *  @return the person food consumption property
     */
    public double getPersonFoodConsumption() {
        return propertiesReader.getPersonFoodConsumption();
    }

    /** Gets the rover oxygen storage capacity property.
     *  Value must be >= 0.
     *  Default value is 350.0 kg.
     *  @return the rover oxygen storage capacity property
     */
    public double getRoverOxygenStorageCapacity() {
        return propertiesReader.getRoverOxygenStorageCapacity();
    }

    /** Gets the rover water storage capacity property.
     *  Value must be >= 0.
     *  Default value is 1400.0 kg.
     *  @return the rover water storage capacity property
     */
    public double getRoverWaterStorageCapacity() {
        return propertiesReader.getRoverWaterStorageCapacity();
    }

    /** Gets the rover food storage capacity property.
     *  Value must be >= 0.
     *  Default value is 525.0 kg.
     *  @return the rover food storage capacity property
     */
    public double getRoverFoodStorageCapacity() {
        return propertiesReader.getRoverFoodStorageCapacity();
    }

    /** Gets the rover fuel storage capacity property.
     *  Value must be >= 0.
     *  Default value is 2500.0 kg.
     *  @return the rover fuel storage capacity property
     */
    public double getRoverFuelStorageCapacity() {
        return propertiesReader.getRoverFuelStorageCapacity();
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
