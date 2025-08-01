/*
 * Mars Simulation Project
 * ConstructionObjective.java
 * @date 2025-07-06
 * @author Scott Davis
 */
package com.mars_sim.core.mission.objectives;

import java.util.List;

import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionStage;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.vehicle.GroundVehicle;

/**
 * This class holds the objectives and facilities of a Construction mission.
 */
public class ConstructionObjective implements MissionObjective {

    private ConstructionSite site;
	private ConstructionStage stage;

	private List<GroundVehicle> constructionVehicles;
	private List<Integer> luvAttachmentParts;

    public ConstructionObjective(ConstructionSite site, ConstructionStage stage,
            List<GroundVehicle> constructionVehicles, List<Integer> luvAttachmentParts) {
        this.site = site;
        this.stage = stage;
        this.constructionVehicles = constructionVehicles;
        this.luvAttachmentParts = luvAttachmentParts;
    }

    @Override
    public String getName() {
        return site.getName();
    }

    public ConstructionSite getSite() {
        return site;
    }

    public ConstructionStage getStage() {
        return stage;
    }

    public List<GroundVehicle> getConstructionVehicles() {
        return constructionVehicles;
    }

    public List<Integer> getLuvAttachmentParts() {
        return luvAttachmentParts;
    }

    
}
