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
        if (name.equals("Lander Hab")) return new LanderHab(manager);
        else if (name.equals("ERV Base")) return new ERVBase(manager);
        else if (name.equals("Inflatable Greenhouse")) return new InflatableGreenhouse(manager);
        else if (name.equals("Nuclear Reactor")) return new NuclearReactor(manager);
        else throw new Exception("Building " + name + " cannot be constructed");
    }
}
