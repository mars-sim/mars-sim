/*
 * Mars Simulation Project
 * Construction.java
 * @date 2023-05-12
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;

import java.util.List;

import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.vehicle.GroundVehicle;

/**
 * A mission with the behaviour of doing Construction that uses GroundVehicles
 */
public interface Construction extends Mission {

    /**
	 * Gets a list of all construction vehicles used by the mission.
	 *
	 * @return list of construction vehicles.
	 */
	List<GroundVehicle> getConstructionVehicles();
}
