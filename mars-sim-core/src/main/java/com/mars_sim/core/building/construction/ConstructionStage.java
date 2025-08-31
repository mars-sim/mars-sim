/**
 * Mars Simulation Project
 * ConstructionStage.java
 * @date 2023-06-07
 * @author Scott Davis
 */
package com.mars_sim.core.building.construction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.SettlementParameters;

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
    
    private Map<Integer, Integer> missingParts;
    private Map<Integer, Double> missingResources;
    
    private Map<Integer, Double> availableResources;
    private Map<Integer, Integer> availableParts;
    
    private Map<Integer, Integer> originalReqParts;
    private Map<Integer, Double> originalReqResources;
    
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
        requiredWorkTime =  (quickConstruction ? 0.1D : 1D) * info.getWorkTime();
        if (!isConstruction) {
            requiredWorkTime *= SALVAGE_WORK_TIME_MODIFIER;
        }
        
        // Quick construction also reduces resources needed
        if (quickConstruction && isConstruction) {
            originalReqParts = new HashMap<>();
            info.getParts().forEach((k,v) -> originalReqParts.put(k, (int)(v * 0.1D)));

            originalReqResources = new HashMap<>();
            info.getResources().forEach((k,v) -> originalReqResources.put(k, (v * 0.1D)));
        }
        else {
            originalReqParts = info.getParts();
            originalReqResources = info.getResources();
        }

        missingParts = new HashMap<>(originalReqParts);
        availableParts = new HashMap<>();

        missingResources = new HashMap<>(originalReqResources);
        availableResources = new HashMap<>();

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
     * Gets the original parts needed for construction.
     * 
     * @return map of parts and their numbers.
     */
    public Map<Integer, Integer> getOriginalParts() {
        return new HashMap<>(originalReqParts);
    }

    /**
     * Gets the original resources needed for construction.
     * 
     * @return map of resources and their amounts (kg).
     */
    public Map<Integer, Double> getOriginalResources() {
        return new HashMap<>(originalReqResources);
    }

    /**
     * Gets the missing parts needed for construction.
     * 
     * @return map of parts and their numbers.
     */
    public Map<Integer, Integer> getMissingParts() {
        return new HashMap<>(missingParts);
    }

    /**
     * Gets the missing resources needed for construction.
     * 
     * @return map of resources and their amounts (kg).
     */
    public Map<Integer, Double> getMissingResources() {
        return new HashMap<>(missingResources);
    }
    
    /**
     * Gets the available resources on site for construction.
     * 
     * @return map of resources and their amounts (kg).
     */
    public Map<Integer, Double> getAvailableResources() {
        return new HashMap<>(availableResources);
    }
    
    /**
     * Gets the available parts on site for construction.
     * 
     * @return map of parts and their amounts (kg).
     */
    public Map<Integer, Integer> getAvailableParts() {
        return new HashMap<>(availableParts);
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

		// Load amount resources.
		for(var r : missingResources.entrySet()) {
			int resource = r.getKey();
			double amountNeeded = r.getValue();
			double amountAvailable = settlement.getSpecificAmountResourceStored(resource);
			// Load as much of the remaining resource as possible into the construction site
			// stage.
			double amountLoading = Math.min(amountAvailable, amountNeeded);

			if (amountLoading > 0) {
				// Retrieve this materials now
				settlement.retrieveAmountResource(resource, amountLoading);
				// Store the materials at this site
				addResource(resource, amountLoading);
				amountNeeded -= amountLoading;
			}

			// Use a 10% buffer just in case other tasks will consume this materials at the same time
			if (amountNeeded > 0) {
				return false;
			}
		}
		
		return true;
	}
		
		
	/**
	 * Loads remaining required construction materials into site that are available
	 * at settlement inventory.
	 * 
	 * @return true if all parts are available
	 */
	public boolean loadAvailableConstructionParts(Settlement settlement) {
        if (!isConstruction) {
            return true;
        }

		boolean enough = true;
		// Load parts.
		for(var e : missingParts.entrySet()) {
			int part = e.getKey();
			int numberNeeded = e.getValue();
			int numberAvailable = settlement.getItemResourceStored(part);
			// Load as many remaining parts as possible into the construction site stage.
			int numberLoading = Math.min(numberAvailable, numberNeeded);

			if (numberLoading > 0) {
				// Retrieve this item now
				settlement.retrieveItemResource(part, numberLoading);
				// Store this item at this site
				addParts(part, numberLoading);
			}
			else
				enough = false;
		}
		
		return enough;
	}
    
    /**
     * Adds parts to the construction stage.
     * 
     * @param part the part to add.
     * @param number the number of parts to add.
     */
    private void addParts(Integer part, int number) {

        if (missingParts.containsKey(part)) {
            int missingRequiredNum = missingParts.get(part);
            if (number <= missingRequiredNum) {
                missingRequiredNum -= number;
                if (missingRequiredNum >= 0) {
                    missingParts.put(part, missingRequiredNum);
                }
          
                int availableNum = 0;
                if (availableParts.containsKey(part)) {
                	availableNum = availableParts.get(part);
                }
                availableNum += number;
                availableParts.put(part, availableNum);
                
                // Fire construction event
                site.fireUnitUpdate(UnitEventType.ADD_CONSTRUCTION_MATERIALS_EVENT, this);
            }
            else {
                throw new IllegalStateException("Trying to add " + number + " " + part + 
                        " to " + info.getName() + " when only " + missingRequiredNum + 
                        " are needed.");
            }
        }
        else {
            throw new IllegalStateException("Construction stage " + info.getName() + 
                    " does not require part " + part);
        }
    }

    /**
     * Adds resource to the construction stage.
     * 
     * @param resource the resource to add.
     * @param amount the amount (kg) of resource to add.
     */
    private void addResource(Integer resource, double amount) {

        if (missingResources.containsKey(resource)) {
            double missingRequiredAmount = missingResources.get(resource);
            if (amount <= missingRequiredAmount) {
                missingRequiredAmount -= amount;
                missingResources.put(resource, missingRequiredAmount);

                double availableAmount = 0;
                if (availableResources.containsKey(resource)) {
                	availableAmount = availableResources.get(resource);
                }
                availableAmount += amount;
                availableResources.put(resource, availableAmount);
                
                // Fire construction event
                site.fireUnitUpdate(UnitEventType.ADD_CONSTRUCTION_MATERIALS_EVENT, this);
            }
            else {
                throw new IllegalStateException("Trying to add " + amount + " " + resource + 
                        " to " + info.getName() + " when only " + missingRequiredAmount + 
                        " are needed.");
            }
        }
        else {
            throw new IllegalStateException("Construction stage " + info.getName() + 
                    " does not require resource " + resource);
        }
    }

    /**
	 * Checks if the settlement has any construction materials needed for the stage.
	 * 
	 * @return true if missing materials available.
	 */
	public boolean hasMissingConstructionMaterials() {
        var  settlement = site.getAssociatedSettlement();
        if (missingResources.entrySet().stream()
            .anyMatch(e -> (e.getValue() > 0)
                    && (e.getValue() > settlement.getSpecificAmountResourceStored(e.getKey())))) {
            return true;
        }

        return missingParts.entrySet().stream()
            .anyMatch(e -> (e.getValue() > 0)
                    && (e.getValue() > settlement.getItemResourceStored(e.getKey())));
    }

    @Override
    public String toString() {
        if (isConstruction) return "Constructing " + info.getName();
        else return "Salvaging " + info.getName();
    }
    
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		info = null;
	    site = null;
	    missingParts.clear();
	    missingResources.clear();
	    originalReqParts.clear();
	    originalReqResources.clear();
	    
	    missingParts = null;
	    missingResources = null;
	    originalReqParts = null;
	    originalReqResources = null;
	}
}
