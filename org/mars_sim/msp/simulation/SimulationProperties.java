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

    /** Constructor */
    public SimulationProperties() {
   
        // Create a PropertiesXmlReader. 
        propertiesReader = new PropertiesXmlReader();
        propertiesReader.parse();
    }

    /** Gets the time ratio property. 
     *  Value must be > 0.
     *  Default value is 1000.
     *  @return the ration between simulation and real time 
     */
    public double getTimeRatio() {
        return propertiesReader.getTimeRatio();
    }
}
