/*
 * Mars Simulation Project
 * ConstructionObjective.java
 * @date 2025-07-06
 * @author Scott Davis
 */
package com.mars_sim.core.mission.objectives;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionStage;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.vehicle.LightUtilityVehicle;

/**
 * This class holds the objectives and facilities of a Construction mission.
 */
public class ConstructionObjective implements MissionObjective {

    private static final long serialVersionUID = 1L;
	private ConstructionSite site;
	private ConstructionStage stage;

	private List<LightUtilityVehicle> constructionVehicles;
	private List<Integer> luvAttachmentParts;

	private Map<Integer, Double> workTimeMap;
	
    public ConstructionObjective(ConstructionSite site, ConstructionStage stage,
            List<LightUtilityVehicle> constructionVehicles, List<Integer> luvAttachmentParts) {
        this.site = site;
        this.stage = stage;
        this.constructionVehicles = constructionVehicles;
        this.luvAttachmentParts = luvAttachmentParts;
        
        workTimeMap = new HashMap<>();
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

    public List<LightUtilityVehicle> getConstructionVehicles() {
        return constructionVehicles;
    }

    public List<Integer> getLuvAttachmentParts() {
        return luvAttachmentParts;
    }

    /**
     * Records the work time.
     * 
     * @param personID
     * @param time
     */
    public void recordWorkTime(int personID, double time) {
    	double oldTime = 0;
    	
    	if (workTimeMap.containsKey(personID)) { 		
    		oldTime = workTimeMap.get(personID);
        	workTimeMap.put(personID, oldTime + time);
    	}
    	else {
    		workTimeMap.put(personID, time);
    	}
    }
    
    public double getWorkTime(int personID) {
    	if (workTimeMap.containsKey(personID)) { 
    		return workTimeMap.get(personID);
    	}
    	else
    		return 0;
    }
}
