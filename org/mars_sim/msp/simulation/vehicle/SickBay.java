/**
 * Mars Simulation Project
 * SickBay.java
 * @version 2.76 2004-06-01
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.person.medical.MedicalStation;
import java.io.Serializable;

/**
 * The SickBay class is a medical station for a vehicle.
 */
public class SickBay extends MedicalStation implements Serializable {
    
    private Vehicle vehicle; // The vehicle this sickbay is in.
    
    /**
     * Constructor
     *
     * @param vehicle The vehicle the sickbay is in.
     * @param treatmentLevel The treatment level of the medical station.
     * @param sickBedNum Number of sickbeds. 
     */
    public SickBay(Vehicle vehicle, int treatmentLevel, int sickBedNum) {
        // Use MedicalStation constructor
        super(treatmentLevel, sickBedNum);
        
        this.vehicle = vehicle;
    }
    
    /**
     * Gets the vehicle this sickbay is in.
     * 
     * @return vehicle
     */
    public Vehicle getVehicle() {
        return vehicle;
    }
}