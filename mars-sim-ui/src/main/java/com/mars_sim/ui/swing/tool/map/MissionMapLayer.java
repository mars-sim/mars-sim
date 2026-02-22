/*
 * Mars Simulation Project
 * MissionMapLayer.java
 * @date 2026-01-31
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.map;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.mission.VehicleMission;

/** 
 * The MissionMapLayer is a map layer to display mission navpoints.
 * It extends NavpointMapLayer to reuse the navpoint display code, but it gets the navpoints from the mission manager
 * or single selected Mission.
 */
public class MissionMapLayer extends NavpointMapLayer{

    private static final String MISSION_PREFIX = "Mission :";
    private MissionManager missionManager;
    private Mission singleMission;

    public MissionMapLayer(MapPanel parent) {
        super(parent);

        missionManager = parent.getDesktop().getSimulation().getMissionManager();

    }

    /**
	 * Sets the single mission to display navpoints for. Set to null to display all
	 * mission navpoints.
	 * 
	 * @param singleMission the mission to display navpoints for.
	 */
	public void setSingleMission(Mission singleMission) {
		this.singleMission = singleMission;
	}

    /**
     * Get the paths to render on this layer. This is either all missions or
     * a single mission depending on the state of singleMission.
     * 
     * @return Map of mission name to list of navpoints for that mission.
     */
    protected Map<String,List<? extends SurfacePOI>> getPaths() {
        Map<String,List<? extends SurfacePOI>> paths = new HashMap<>();
		if (singleMission != null) {
			if (singleMission instanceof VehicleMission vm)
                paths.put(MISSION_PREFIX + singleMission.getName(), vm.getNavpoints());
		} else {
			for (Mission mission : missionManager.getMissions()) {
				if (mission instanceof VehicleMission vm && !vm.isDone())
                    paths.put(MISSION_PREFIX + mission.getName(), vm.getNavpoints());
			}
		}
        return paths;
    }
}
