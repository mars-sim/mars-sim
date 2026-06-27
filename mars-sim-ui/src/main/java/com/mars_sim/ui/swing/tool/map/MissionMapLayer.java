/*
 * Mars Simulation Project
 * MissionMapLayer.java
 * @date 2026-01-31
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.map;

import java.util.List;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.EntityManagerListener;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.mission.MissionControl;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.structure.Settlement;

/** 
 * The MissionMapLayer is a map layer to display mission navpoints.
 * It extends RoutePathLayer to reuse the path display code, but it gets the navpoints from the missions
 * or single selected Mission.
 */
public class MissionMapLayer extends RoutePathLayer
        implements EntityListener, EntityManagerListener{

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
    private UnitManager unitMgr;

    /**
     * Display all active Missions on a layer, and listen for new missions to add to the display.
     * Completed missions will be removed automatically.
     * @param parent Map panel for display
     */
    public MissionMapLayer(MapPanel parent) {
        super(parent);

        // Listen for new Settlements
        unitMgr = parent.getDesktop().getSimulation().getUnitManager();
        unitMgr.addEntityManagerListener(UnitType.SETTLEMENT, this);

        for(var s : unitMgr.getSettlements()) {
            addSettlement(s);
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

        // Remove Entity manager listeners
        if (unitMgr != null) {
            unitMgr.removeEntityManagerListener(UnitType.SETTLEMENT, this);
            unitMgr.getSettlements().forEach(s -> s.removeEntityListener(this));
        }
    }

    /**
     * Mission has been updated. If it's done, remove it from the display.
     * @param event Mission updated.
     */
    @Override
    public void entityUpdate(EntityEvent event) {
        if (event.getSource() instanceof Mission m && m.isDone()) {
            removeMission(m);
        }
        else if (event.getSource() instanceof Settlement s) {
            if (event.getType().equals(MissionControl.MISSION_ADD)) {
                addMission((Mission) event.getTarget());
            }
            else if (event.getType().equals(MissionControl.MISSION_REMOVED)) {
                removeMission((Mission) event.getTarget());
            }
        }
    }

    /**
     * A new Settlement or Mission has been added.
     * @param newEntity Entity added.
     */
    @Override
    public void entityAdded(Entity newEntity) {
        if (newEntity instanceof Settlement s) {
            addSettlement(s);
        }
    }

    /**
     * Entity has been removed from the parent manager.
     * @param event Entity removed.
     */
    @Override
    public void entityRemoved(Entity event) {
        // Nothing to do
    }

    private void addSettlement(Settlement settlement) {
        // Listen for new missions in each settlement
        settlement.addEntityListener(this);

        // Add existing missions
        settlement.getMissionControl().getActiveMissions().forEach(this::addMission);
    }

    private void addMission(Mission mission) {
        if (mission instanceof VehicleMission vm && !vm.isDone()) {
            addPath(new MissionProxy(vm));
            vm.addEntityListener(this);
        }
    }

    private void removeMission(Mission m) {
        m.removeEntityListener(this);

        // Remove path if mission is done.
        for(RoutePath path : getPaths()) {
            if (path instanceof MissionProxy mp && mp.mission.equals(m)) {
                removePath(path);
                break;
            }
        }
    }
}