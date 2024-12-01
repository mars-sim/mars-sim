/**
 * Mars Simulation Project
 * ConstructionStage.java
 * @date 2023-06-07
 * @author Scott Davis
 */
package com.mars_sim.core.structure.construction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.tool.Conversion;

/**
 * A construction stage of a construction site.
 */
public class ConstructionStage implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Work time modifier for salvaging a construction stage. */
    private static final double SALVAGE_WORK_TIME_MODIFIER = .25D;

    // Data members
    private boolean isSalvaging;
    
    private double completedWorkTime;
    private double completableWorkTime;

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
    public ConstructionStage(ConstructionStageInfo info, ConstructionSite site) {
        this.info = info;
        this.site = site;
        
        isSalvaging = false;
        
        completedWorkTime = 0D;
        completableWorkTime = 0D;
        
        originalReqParts = new HashMap<>(info.getParts());
        originalReqResources = new HashMap<>(info.getResources());

        availableResources = new HashMap<>();
        availableParts = new HashMap<>();
        
        missingParts = new HashMap<>(info.getParts());
        missingResources = new HashMap<>(info.getResources());
        
        // Update the missing completable work time.
        updateCompletableWorkTime();
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
     * Gets the amount work time that can be completed for this stage.
     * 
     * @return completable work time (millisols).
     */
    public double getCompletableWorkTime() {
        return completableWorkTime;
    }

    /**
     * Gets the required work time for the stage.
     * 
     * @return work time (in millisols).
     */
    public double getRequiredWorkTime() {
        double requiredWorkTime = info.getWorkTime();
        if (isSalvaging) {
            requiredWorkTime *= SALVAGE_WORK_TIME_MODIFIER;
        }
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
     * Checks if the stage is salvaging.
     * 
     * @return true if stage is salvaging.
     */
    public boolean isSalvaging() {
        return isSalvaging;
    }

    /**
     * Sets if the stage is salvaging.
     * 
     * @param isSalvaging true if staging is salvaging.
     */
    public void setSalvaging(boolean isSalvaging) {
        this.isSalvaging = isSalvaging;
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
     * Adds parts to the construction stage.
     * 
     * @param part the part to add.
     * @param number the number of parts to add.
     */
    public void addParts(Integer part, int number) {

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
                
                // Update the missing completable work time.
                updateCompletableWorkTime();
                
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
    public void addResource(Integer resource, double amount) {

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
                
                // Update the missing completable work time.
                updateCompletableWorkTime();
                
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
     * Updates the completable work time available.
     */
    private void updateCompletableWorkTime() {

        double totalRequiredConstructionMaterial = getConstructionMaterialMass(
                info.getResources(), info.getParts());

        double totalMissingConstructionMaterial = getConstructionMaterialMass(
                missingResources, missingParts);

        double proportion = 1D;
        if (totalRequiredConstructionMaterial > 0D) {
            proportion = (totalRequiredConstructionMaterial - totalMissingConstructionMaterial) / 
                    totalRequiredConstructionMaterial;
        }

        completableWorkTime = proportion * info.getWorkTime();
    }

    /**
     * Gets the total mass of construction materials.
     * 
     * @param resources map of resources and their amounts (kg).
     * @param parts map of parts and their numbers.
     * @return total mass.
     */
    private double getConstructionMaterialMass(Map<Integer, Double> resources, 
    		Map<Integer, Integer> parts) {

        double result = 0D;

        // Add total mass of resources.
        Iterator<Integer> i = resources.keySet().iterator();
        while (i.hasNext()) {
        	Integer resource = i.next();
            double amount = resources.get(resource);
            result += amount;
        }

        // Add total mass of parts.
        Iterator<Integer> j = parts.keySet().iterator();
        while (j.hasNext()) {
        	Integer part = j.next();
            int number = parts.get(part);
            double mass = ItemResourceUtil.findItemResource(part).getMassPerItem();
            result += number * mass;
        }

        return result;
    }

    @Override
    public String toString() {
        String result = "";
        if (isSalvaging) result = Conversion.capitalize("Salvaging " + info.getName());
        else result = Conversion.capitalize("Constructing " + info.getName());
        return result;
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
