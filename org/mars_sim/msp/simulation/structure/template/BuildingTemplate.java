/**
 * Mars Simulation Project
 * BuildingTemplate.java
 * @version 2.75 2003-01-05
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.template;

import org.mars_sim.msp.simulation.structure.building.*;

/** 
 * The BuildingTemplate class represents a template from 
 * which buildings can be constructed from.
 */
public class BuildingTemplate {
	
	private static String LANDER_HAB = "Lander Hab";
	private static String ERV_BASE = "ERV Base";
	private static String STARTING_ERV_BASE = "Starting ERV Base";
	private static String INFLATABLE_GREENHOUSE = "Inflatable Greenhouse";
	private static String NUCLEAR_REACTOR = "Nuclear Reactor";
    
    private String name;
    
    /**
     * Constructor
     *
     * @param name the building's name.
     */
    public BuildingTemplate(String name) {
        this.name = name;
    }
    
    /**
     * Gets the building template's name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Constructs an instance of the template's building.
     *
     * @param manager the building manager
     * @return building
     */
    public Building constructBuilding(BuildingManager manager) throws Exception {
        if (name.equals(LANDER_HAB)) return new LanderHab(manager);
    	else if (name.equals(STARTING_ERV_BASE)) return new ERVBase(manager, true);
        else if (name.equals(ERV_BASE)) return new ERVBase(manager, false);
        else if (name.equals(INFLATABLE_GREENHOUSE)) return new InflatableGreenhouse(manager);
        else if (name.equals(NUCLEAR_REACTOR)) return new NuclearReactor(manager);
        else throw new Exception("Building " + name + " cannot be constructed");
    }
}
