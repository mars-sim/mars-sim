/**
 * Mars Simulation Project
 * SimulationProperties.java
 * @version 2.73 2001-11-28
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
}
