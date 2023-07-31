/*
 * Mars Simulation Project
 * UnitEntity.java
 * @date 2023-07-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function.farming;

/**
 * Represents an unit entity that is simulated by the system. These could be an active element
 * that is proactively managed or a passive object such as a Mission.
 */
public interface UnitEntity {
	
    /**
     * Gets the name of the entity.
     * 
     * @return
     */
    String getName();
}
