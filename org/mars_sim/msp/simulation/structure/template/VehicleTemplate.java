/**
 * Mars Simulation Project
 * VehicleTemplate.java
 * @version 2.75 2004-03-24
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.template;

import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.*;

/** 
 * The VehicleTemplate class represents a template from 
 * which vehicles can be constructed from.
 */
public class VehicleTemplate {
    
    private static final String EXPLORER_ROVER = "Explorer Rover";
    private static final String TRANSPORT_ROVER = "Transport Rover";
    
    private String name;
    
    /**
     * Constructor
     *
     * @param name the vehicle's name.
     */
    public VehicleTemplate(String name) {
        this.name = name;
    }
    
    /**
     * Gets the vehicle template's name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Constructs an instance of the template's vehicle.
     * @param vehicleType the vehicle description
     * @param settlement the settlement
     * @param mars the Mars instance.
     * @return vehicle
     */
    public Vehicle constructVehicle(String name, String vehicleType, Settlement settlement, Mars mars) throws Exception {
        if (vehicleType.equals(EXPLORER_ROVER)) return new Rover(name, vehicleType, settlement, mars);
        else if (vehicleType.equals(TRANSPORT_ROVER)) return new Rover(name, vehicleType, settlement, mars);
        else throw new Exception("Vehicle of type " + vehicleType + " cannot be constructed");
    }
}