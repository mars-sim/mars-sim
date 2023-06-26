package org.mars_sim.msp.core.tool;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;

public interface SimulationConstants {

  static Simulation sim = Simulation.instance();
  static MissionManager missionManager = sim.getMissionManager();
  static UnitManager unitManager = sim.getUnitManager();
  static SurfaceFeatures surfaceFeatures = sim.getSurfaceFeatures();

}
