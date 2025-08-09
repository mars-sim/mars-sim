/**
 * Mars Simulation Project
 * ConstructionSite.java
 * @date 2023-06-07
 * @author Scott Davis
 */

package com.mars_sim.core.building.construction;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.BuildingSpec;
import com.mars_sim.core.building.construction.ConstructionStageInfo.Stage;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.unit.FixedUnit;

/**
 * A building construction site.
 */
public class ConstructionSite extends FixedUnit {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(ConstructionSite.class.getName());


    private boolean isConstruction;
    private boolean isWorkOnsite;
    private boolean unstarted = true;
    
	// Unique identifier
	private int identifier;

    private double width;
    private double length;
    private LocalPosition position;
    private double facing;

    private String targetBuilding;
    private ConstructionStage currentStage;

    
    /**
     * Constructor.
     */
    public ConstructionSite(Settlement settlement, String target, boolean isConstruction,
                            ConstructionStageInfo initStage,
                            LocalBoundedObject placement) {
    	super("Site", settlement);
    	
    	identifier = settlement.getConstructionManager().getUniqueID();
    	
    	createSiteName();

        this.isConstruction = isConstruction;
        this.targetBuilding = target;
    	this.width = placement.getWidth();
        this.length = placement.getLength();
        this.facing = placement.getFacing();
        this.position = placement.getPosition();
        this.currentStage = new ConstructionStage(initStage, this, isConstruction);

    }

	private void createSiteName() {
	    if (identifier < 10) {
			setName(getName() + "00" + identifier);
		}
		else if (identifier < 100) {
			setName(getName() + "0" + identifier);
		}
		else {
			setName(getName() + identifier);
		}
	}

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public LocalPosition getPosition() {
    	return position;
    }
	
    @Override
    public double getFacing() {
        return facing;
    }

    /**
     * Checks if all construction is complete at the site.
     * 
     * @return true if construction is complete.
     */
    public boolean isAllConstructionComplete() {
        if (currentStage.getInfo().getType() == Stage.BUILDING && isConstruction) return currentStage.isComplete();
        else return false;
    }

    /**
     * Checks if all salvage is complete at the site.
     * 
     * @return true if salvage is complete.
     */
    public boolean isAllSalvageComplete() {
        if (!isConstruction) {
            if (currentStage.getInfo().getType() != Stage.FOUNDATION) return false;
            else return currentStage.isComplete();
        }
        else return false;
    }

    /**
     * Checks if site is currently undergoing construction.
     * 
     * @return true if undergoing construction.
     */
    public boolean isConstruction() {
        return isConstruction;
    }

    /**
     * Checks if site has work on site
     */
    public boolean isWorkOnSite() {
        return isWorkOnsite;
    }

    /**
     * Sets if site has work on site.
     */
    public void setWorkOnSite(boolean active) {
        this.unstarted = false;
        this.isWorkOnsite = active;

        UnitEventType eventType;
        if (isConstruction) {
            eventType = (isWorkOnsite  ? UnitEventType.START_CONSTRUCTION_SITE_EVENT
                                        : UnitEventType.END_CONSTRUCTION_SITE_EVENT);
        }
        else {
            eventType =  (isWorkOnsite ? UnitEventType.START_CONSTRUCTION_SALVAGE_EVENT
                                        : UnitEventType.FINISH_CONSTRUCTION_SALVAGE_EVENT);
        }

        fireUnitUpdate(eventType);
    }

    /**
     * Gets the current construction stage at the site.
     * 
     * @return construction stage.
     */
    public ConstructionStage getCurrentConstructionStage() {
        return currentStage;
    }

    /**
     * Adds a new construction stage to the site.
     * 
     * @param newStage the new construction stage.
     * @throws Exception if error adding construction stage.
     */
    public void addNewStage(ConstructionStageInfo newStage) {
        var stageType = currentStage.getInfo().getType();
        var newType = newStage.getType();

        // Stage type must move forward
        if (newType.ordinal() > stageType.ordinal()) {
            // Fire construction event.
            currentStage = new ConstructionStage(newStage, this, isConstruction);
            fireUnitUpdate(UnitEventType.ADD_CONSTRUCTION_STAGE_EVENT, currentStage);
            return;
        }

        logger.severe(this, "Invalid stage construction change from " + currentStage.getInfo().getName()
                            + " to " + newStage.getName());
    }

    /**
     * Removes a salvaged stage from the construction site.
     * 
     * @param stage the salvaged construction stage.
     * @throws Exception if error removing the stage.
     */
    public void removeSalvagedStage(ConstructionStage stage) {
        var stageType = currentStage.getInfo().getType();
        var newType = stage.getInfo().getType();

        // Stage type must move backward
        if (newType.ordinal() < stageType.ordinal()) {
            // Fire construction event.
            currentStage = stage;
            fireUnitUpdate(UnitEventType.REMOVE_CONSTRUCTION_STAGE_EVENT, currentStage);
            return;
        }

        logger.severe(this, "Invalid stage savlage change from " + currentStage.getInfo().getName()
                            + " to " + stage.getInfo().getName());
    }

    /**
     * Creates a new building from the construction site.
     * 
     * @return newly constructed building.

     */
    public Building createBuilding() {
        if (currentStage.getInfo().getType() != Stage.BUILDING) {
            throw new IllegalStateException("Building stage doesn't exist");
        }

        var settlement = getAssociatedSettlement();

        BuildingManager manager = settlement.getBuildingManager();
        int id = manager.getNextTemplateID();
        String buildingType = currentStage.getInfo().getName();
        String uniqueName = manager.getUniqueName(buildingType);
        
        int zone = 0;
        var spec = getBuildingSpec(buildingType);

        Building newBuilding = new Building(settlement, Integer.toString(id), zone, uniqueName,
        		new BoundedObject(position, width, length, facing), spec);
        
        manager.addBuilding(newBuilding, true);

        // Record completed building name.
        var constructionManager = settlement.getConstructionManager();
        constructionManager.addConstructedBuildingLogEntry(buildingType);

        // Clear construction value cache.
        constructionManager.getConstructionValues().clearCache();

        // Fire construction event.
        fireUnitUpdate(UnitEventType.FINISH_CONSTRUCTION_BUILDING_EVENT, newBuilding);

        return newBuilding;
    }

    private static BuildingSpec getBuildingSpec(String buildingType) {
        return SimulationConfig.instance().getBuildingConfiguration().getBuildingSpec(buildingType);
    }

    /**
     * Gets the building name the site will construct.
     * 
     * @return building name or null if undetermined.
     */
    public String getBuildingName() {
        return targetBuilding;
    }

    /**
     * Checks if the site's current stage is unfinished.
     * 
     * @return true if stage unfinished.
     */
    public boolean hasUnfinishedStage() {
        return !currentStage.isComplete();
    }

    public boolean isUnstarted() {
        return unstarted;
    }

    /**
     * Relocates the construction site by changing its coordinates.
     */
	public void relocateSite() {
        var existingPosn = getPosition();

		// Compute a new position for a site
        var spec = getBuildingSpec(targetBuilding);

		var newPlacement = BuildingPlacement.placeSite(this.getAssociatedSettlement(), spec);
        position = newPlacement.getPosition();
		
		logger.info(this, "Manually relocated by player from " 
				+ existingPosn + " to "
				+ position);
        

	}

	@Override
	public UnitType getUnitType() {
		return UnitType.CONSTRUCTION;
	}
	
    /**
     * Get the dynamic description based on the current stage.
     * @return Description of site
     */
    @Override
    public String getDescription() {
		StringBuilder result = new StringBuilder();

		ConstructionStage stage = getCurrentConstructionStage();
		if (stage != null) {
			result.append(stage.getInfo().getName());
		}

		return result.toString();
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
    @Override
	public void destroy() {
        super.destroy();
	    currentStage = null;
	}

    /**
     * Get a status description of the site.
     * @return
     */
    public String getStatusDescription() {
        String result = currentStage.getInfo().getName();
        if (isWorkOnsite) {
            if (isConstruction) {
                result += " - Active Construction";
            } else  {
                result += " - Active Salvage";
            }
        } else if (hasUnfinishedStage()) {
            if (isConstruction)
                result += " - Construction Unfinished";
            else
                result += " - Salvage Unfinished";
        }
        return result;
    }
}
