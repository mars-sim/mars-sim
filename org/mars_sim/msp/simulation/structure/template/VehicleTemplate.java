/**
 * Mars Simulation Project
 * VehicleTemplate.java
 * @version 2.75 2003-01-05
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.template;

import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.simulation.structure.*;

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
     *
     * @param manager the vehicle manager
     * @return vehicle
     */
    public Vehicle constructVehicle(String nameOfVehicle, Settlement settlement, Mars mars) throws Exception {
        if (name.equals(EXPLORER_ROVER)) return new ExplorerRover(nameOfVehicle, settlement, mars);
        else if (name.equals(TRANSPORT_ROVER)) return new TransportRover(nameOfVehicle, settlement, mars);
        else throw new Exception("Vehicle of type " + name + " cannot be constructed");
    }
}
