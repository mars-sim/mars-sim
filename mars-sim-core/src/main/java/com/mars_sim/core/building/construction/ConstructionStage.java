/**
 * Mars Simulation Project
 * ConstructionStage.java
 * @date 2023-06-07
 * @author Scott Davis
 */
package com.mars_sim.core.building.construction;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.SettlementParameters;
import com.mars_sim.core.tool.RandomUtil;

/**
 * A construction stage of a construction site.
 */
public class ConstructionStage implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Work time modifier for salvaging a construction stage. */
    private static final double SALVAGE_WORK_TIME_MODIFIER = .25D;

    // Data members
    private boolean isConstruction;
    
    private double completedWorkTime;
    private double requiredWorkTime;

    private ConstructionStageInfo info;
    private ConstructionSite site;
    
    /**
     * Represents the quantities of a Material needed for construction
     */
    public static class Material implements Serializable {
        private double required;
        private double available;

        private Material(double required) {
            this.required = required;
        }

        public double getRequired() {
            return required;
        }

        public double getAvailable() {
            return available;
        }

        public double getMissing() {
            return required - available;
        }

        private void addAmount(double delta) {
            available += delta;
            available = Math.min(required, available);  // Available cannot be more than required
        }
    }

    private Map<Integer, Material> parts;
    private Map<Integer, Material> resources;
    
    /**
     * Constructor.
     * 
     * @param info the stage information.
     */
    ConstructionStage(ConstructionStageInfo info, ConstructionSite site, boolean isConstruction) {
        this.info = info;
        this.site = site;
        this.isConstruction = isConstruction;
        
        var quickConstruction = site.getAssociatedSettlement().getPreferences().getBooleanValue(
                                                                        SettlementParameters.INSTANCE,
                                                                        SettlementParameters.QUICK_CONST,
                                                           false);
        completedWorkTime = 0D;
        var scaling = (quickConstruction ? 10 : 1);
        requiredWorkTime = info.getWorkTime()/scaling;
        if (isConstruction) {
            resources = info.getResources().entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, v -> new Material(v.getValue()/scaling)));
            requiredWorkTime *= SALVAGE_WORK_TIME_MODIFIER;
        }
        else {
            resources = Collections.emptyMap();
        }

        parts = info.getParts().entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, v -> new Material(v.getValue()/scaling)));
    }

    /**
     * Get the construction stage information.
     * 
     * @return stage information.
     */
    public ConstructionStageInfo getInfo() {
        return info;
    }

    /**
     * Is this stage doing Constructiopn or Salvage
     * @return
     */
    public boolean isConstruction() {
        return isConstruction;
    }

    /**
     * Gets the completed work time on the stage.
     * 
     * @return work time (in millisols).
     */
    public double getCompletedWorkTime() {
        return completedWorkTime;
    }

    /**
     * Sets the completed work time on the stage.
     * 
     * @param completedWorkTime work time (in millisols).
     */
    public void setCompletedWorkTime(double completedWorkTime) {
        this.completedWorkTime = completedWorkTime;
    }

    /**
     * Gets the required work time for the stage.
     * 
     * @return work time (in millisols).
     */
    public double getRequiredWorkTime() {
        return requiredWorkTime;
    }

    /**
     * Adds work time to the construction stage.
     * 
     * @param workTime the work time (in millisols) to add.
     */
    public void addWorkTime(double workTime) {
        completedWorkTime += workTime;

        if (completedWorkTime > getRequiredWorkTime()) {
            completedWorkTime = getRequiredWorkTime();
        }

        // Fire construction event
        site.fireUnitUpdate(UnitEventType.ADD_CONSTRUCTION_WORK_EVENT, this);
    }

    /**
     * Checks if the stage is complete.
     * 
     * @return true if stage is complete.
     */
    public boolean isComplete() {
        return (completedWorkTime >= getRequiredWorkTime());
    }

    /**
     * Gets the parts needed for construction.
     * 
     * @return map of parts and their numbers.
     */
    public Map<Integer, Material> getParts() {
        return parts;
    }

    /**
     * Gets the resources needed for construction.
     * 
     * @return map of resources and their amounts (kg).
     */
    public Map<Integer, Material> getResources() {
        return resources;
    }

    /**
     * Get how much of a material is still needed
     * @param id
     * @return
     */
    public double getResourceNeeded(int id) {
        var m = resources.get(id);
        return (m == null ? 0D : m.getMissing());
    }
    
    /**
	 * Loads remaining required construction materials into site that are available
	 * at settlement inventory.
	 * @param settlement 
	 * 
	 * @return true if all resources are available
	 */
	public boolean loadAvailableConstructionMaterials(Settlement settlement) {
		// Account for the situation when all the input materials are ready 
		// but since some processes take time to produce the output materials (the construction materials)
		// It should simply wait for it to finish, without having to compute if resources are 
		// missing over and over again.
		if (!isConstruction) {
            return true;
        }

        boolean allLoaded = true;
        boolean createEvent = false;

		// Load amount resources.
		for(var r : resources.entrySet()) {
			int resource = r.getKey();
			var material = r.getValue();
            var amountNeeded = material.getMissing();
            if (amountNeeded > 0) {
                double amountAvailable = settlement.getSpecificAmountResourceStored(resource);
                // Load as much of the remaining resource as possible into the construction site
                // stage.
                if (amountNeeded > amountAvailable) {
                    amountNeeded = amountAvailable;
                    allLoaded = false;
                }
                if (amountNeeded > 0) {
                    // Retrieve this materials now
                    settlement.retrieveAmountResource(resource, amountNeeded);
                    // Store the materials at this site
                    material.addAmount(amountNeeded);
                    createEvent = true;
                }
            }
		}
	
        // Load parts
		for(var r : parts.entrySet()) {
			int part = r.getKey();
			var material = r.getValue();
            var amountNeeded = (int)material.getMissing();
            if (amountNeeded > 0) {
                int amountAvailable = settlement.getItemResourceStored(part);
                // Load as much of the remaining parts as possible into the construction site
                // stage.
                if (amountNeeded > amountAvailable) {
                    amountNeeded = amountAvailable;
                    allLoaded = false;
                }
                if (amountNeeded > 0) {
                    // Retrieve this materials now
                    settlement.retrieveItemResource(part, amountNeeded);
                    // Store the materials at this site
                    material.addAmount(amountNeeded);
                    createEvent = true;
                }
            }
		}

        if (createEvent) {
            // Generate an event if at least one has been changed
            site.fireUnitUpdate(UnitEventType.ADD_CONSTRUCTION_MATERIALS_EVENT, this);
        }
		return allLoaded;
	}

    /**
	 * Checks if the settlement has any construction materials needed for the stage.
	 * 
	 * @return true if missing materials available.
	 */
	public boolean hasMissingConstructionMaterials() {
        var  settlement = site.getAssociatedSettlement();
        if (resources.entrySet().stream()
            .anyMatch(e -> (e.getValue().getMissing() > 0)
                    && (e.getValue().getMissing() > settlement.getSpecificAmountResourceStored(e.getKey())))) {
            return true;
        }

        return parts.entrySet().stream()
            .anyMatch(e -> (e.getValue().getMissing() > 0)
                    && (e.getValue().getMissing() > settlement.getItemResourceStored(e.getKey())));
    }

    @Override
    public String toString() {
        if (isConstruction) return "Constructing " + info.getName();
        else return "Salvaging " + info.getName();
    }

    /**
     * Reclaim parts
     * @param reclaimChance
     */
    void reclaimParts(double reclaimChance) {
        var settlement = site.getAssociatedSettlement();

		// Salvage construction parts.
		for(var e : parts.entrySet()) {
			int part = e.getKey();
			var material = e.getValue();

			int salvagedNumber = 0;
			for (int x = 0; x < material.getRequired(); x++) {
				if (RandomUtil.lessThanRandPercent(reclaimChance))
					salvagedNumber++;
			}

			if (salvagedNumber > 0) {
				material.addAmount(salvagedNumber);
				settlement.storeItemResource(part, salvagedNumber);
			}
		}
    }
        
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		info = null;
	    site = null;
	    parts.clear();
        resources.clear();
	}

}
