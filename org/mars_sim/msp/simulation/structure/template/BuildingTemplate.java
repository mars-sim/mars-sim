/**
 * Mars Simulation Project
 * BuildingTemplate.java
 * @version 2.75 2004-04-05
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
     * @throws BuildingException if building can not be constructed.
     */
    public Building constructBuilding(BuildingManager manager) throws BuildingException {
		return new Building(name, manager);
    }
}