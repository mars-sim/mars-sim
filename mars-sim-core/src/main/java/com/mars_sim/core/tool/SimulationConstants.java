/*
 * Mars Simulation Project
 * SimulationConstants.java
 * @date 2023-10-17
 * @author Barry Evans
 */

package com.mars_sim.core.tool;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.person.ai.mission.MissionManager;

public interface SimulationConstants {

  static Simulation sim = Simulation.instance();
  static MissionManager missionManager = sim.getMissionManager();
  static UnitManager unitManager = sim.getUnitManager();
  static SurfaceFeatures surfaceFeatures = sim.getSurfaceFeatures();

}
