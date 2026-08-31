/*
 * Mars Simulation Project
 * ExploreSiteStep.java
 * @date 2026-08-31
 * @author Barry Evans
 */
package com.mars_sim.core.mission.predefined;

import com.mars_sim.core.mission.steps.EVAMissionStep;
import com.mars_sim.core.mission.task.ExploreSite;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Exploration;
import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.mission.objectives.ExplorationObjective;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.resource.SuppliesManifest;
import com.mars_sim.core.vehicle.Rover;

/**
 * A predefined mission step for exploring a mineral site.
 */
public class ExploreSiteStep extends EVAMissionStep {

    /** Number of specimen containers required for the mission. */
	public static final int REQUIRED_SPECIMEN_CONTAINERS = 8;

    private MineralSite site;
    private ExplorationObjective objective;

    public ExploreSiteStep(MissionVehicleProject parent, ExplorationObjective objective,MineralSite site) {
        super(parent, Stage.ACTIVE, "Explore " + site.getName(),
                // Give extra 50% on site to cover EVA ingress/engress
                (int)(objective.getTargetExploreTime() * 1.5D));

        this.site = site;
        this.objective = objective;
    }

    /**
     * Calculates containers needed for exploration.
     * 
     * @param includeOptionals Add optional resources to the manifest
     * @param resources Place to hold the order
     */
    @Override
    protected void getRequiredResources(SuppliesManifest resources, boolean includeOptionals) {
        super.getRequiredResources(resources, includeOptionals);
      
    	int buffer = (int)(getMission().getMembers().size() * 1.5);
		int newContainerNum = Math.max(buffer, REQUIRED_SPECIMEN_CONTAINERS);
        resources.setMinEquipment(EquipmentType.SPECIMEN_BOX.getResourceID(), newContainerNum, true);
    }

    /**
     * Executes the EVA task for exploring the site.
     */
    @Override
    protected boolean executeEVA(Person worker) {
        // Check how much exploration has been done
        var explored = objective.getSiteTime().getOrDefault(site.getName(), 0D);
        if (explored >= objective.getTargetExploreTime()) {
            // Site already fully explored
            endAllEVAs();
            return false; 
        }

        var rover = (Rover)((MissionVehicleProject)getMission()).getVehicle();
        var newTask = new ExploreSite(worker, site, rover, objective);

        getMission().fireMissionUpdate(Exploration.SITE_EXPLORATION_EVENT, site.getName());
        return assignTask(worker, newTask);
    }

    /**
     * Notify the mission that the exploration step is complete.
     */
    @Override
    protected void complete() {
        getMission().fireMissionUpdate(Exploration.SITE_EXPLORATION_EVENT, site.getName());
        super.complete();
    }

    /**
     * Step has stopped so mark the site as explored
     */
    @Override
    protected void start() {
        site.setExplored(true);
        super.start();
    }

    /**
     * Get the exploration objective associated with this step.
     */
    @Override
    public MissionObjective getObjective() {
        return objective;
    }
}
