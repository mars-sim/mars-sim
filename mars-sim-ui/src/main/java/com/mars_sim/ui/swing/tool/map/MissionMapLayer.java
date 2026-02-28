/*
 * Mars Simulation Project
 * MissionMapLayer.java
 * @date 2026-01-31
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.map;

import java.util.List;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.mission.MissionManagerListener;
import com.mars_sim.core.person.ai.mission.VehicleMission;

/** 
 * The MissionMapLayer is a map layer to display mission navpoints.
 * It extends RoutePathLayer to reuse the path display code, but it gets the navpoints from the mission manager
 * or single selected Mission.
 */
public class MissionMapLayer extends RoutePathLayer implements EntityListener, MissionManagerListener{

    private static class MissionProxy implements RoutePath {
        private Mission mission;

        public MissionProxy(Mission mission) {
            this.mission = mission;
        }

        @Override
        public String getContext() {
            return MISSION_PREFIX + mission.getName();
        }

        @Override
        public List<? extends SurfacePOI> getNavpoints() {
            if (mission instanceof VehicleMission vm)
                return vm.getNavpoints();
            else
                return List.of();
        }

        @Override
        public Coordinates getStart() {
            return mission.getAssociatedSettlement().getCoordinates();
        }
    }
    
    private static final String MISSION_PREFIX = "Mission :";
    private MissionManager missionManager;

    /**
     * Display all active Missions on a layer, and listen for new missions to add to the display.
     * Completed missions will be removed automatically.
     * @param parent Map panel for display
     */
    public MissionMapLayer(MapPanel parent) {
        super(parent);

        missionManager = parent.getDesktop().getSimulation().getMissionManager();
        missionManager.addListener(this);
        for (Mission mission : missionManager.getMissions()) {
			if (mission instanceof VehicleMission vm && !vm.isDone()) {
                addPath(new MissionProxy(vm));
                vm.addEntityListener(this);
            }
        }
    }

    /**
     * Display the route of a single mission on a layer.
     * @param parent Map panel for display
     * @param singleMission Mission to display.
     */
    public MissionMapLayer(MapPanel parent, Mission singleMission) {
        super(parent);

        addPath(new MissionProxy(singleMission));
    }

    /**
     * Release the listeners.
     */
    @Override
    public void release() {
        for(RoutePath path : getPaths()) {
            if (path instanceof MissionProxy mp) {
                mp.mission.removeEntityListener(this);
            }
        }

        if (missionManager != null) {
            missionManager.removeListener(this);
        }
    }

    @Override
    public void entityUpdate(EntityEvent event) {
        if (event.getSource() instanceof Mission m && m.isDone()) {
            removeMission(m);
        }
    }

    @Override
    public void addMission(Mission mission) {
        if (mission instanceof VehicleMission vm && !vm.isDone()) {
            addPath(new MissionProxy(vm));
            vm.addEntityListener(this);
        }
    }

    @Override
    public void removeMission(Mission m) {
        m.removeEntityListener(this);

        // Remove path if mission is done.
        for(RoutePath path : getPaths()) {
            if (path instanceof MissionProxy mp && mp.mission == m) {
                removePath(path);
                break;
            }
        }
    }
}