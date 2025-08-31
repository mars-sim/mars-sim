/**
 * Mars Simulation Project
 * ConstructionSite.java
 * @date 2023-06-07
 * @author Scott Davis
 */

package com.mars_sim.core.building.construction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.unit.FixedUnit;

/**
 * A building construction site.
 */
public class ConstructionSite extends FixedUnit {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /**
     * A phase of construction that might be construct or salvage.
     */
    public record ConstructionPhase(ConstructionStageInfo stage, boolean construct) implements Serializable {}

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(ConstructionSite.class.getName());

    private boolean isConstruction;
    private Mission activeWork;
    private boolean unstarted = true;

    private double width;
    private double length;
    private LocalPosition position;
    private double facing;

    private String targetBuilding;
    private ConstructionStage currentStage;

    private List<ConstructionPhase> phases;
    
    /**
     * Constructor.
     */
    public ConstructionSite(Settlement settlement, String siteName, String target, List<ConstructionPhase> phases,
                            LocalBoundedObject placement) {
    	super(siteName, settlement);
    	
        this.phases = new ArrayList<>(phases);

        // Pop off the first phase
        var initPhase = this.phases.remove(0);

        this.isConstruction = initPhase.construct();
        this.currentStage = new ConstructionStage(initPhase.stage(), this, isConstruction);

        this.targetBuilding = target;
    	this.width = placement.getWidth();
        this.length = placement.getLength();
        this.facing = placement.getFacing();
        this.position = placement.getPosition();
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
     * Get the remaining construction phases
     * @return
     */
    public List<ConstructionPhase> getRemainingPhases() {
        return phases;
    }

    /**
     * Checks if all construction is complete at the site.
     * 
     * @return true if construction is complete.
     */
    public boolean isComplete() {
        if (phases.isEmpty()) return currentStage.isComplete();
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
     * Sets if site has work on site.
     */
    public void setWorkOnSite(Mission activeWork) {
        this.unstarted = false;
        this.activeWork = activeWork;

        UnitEventType eventType = ((activeWork != null)  ? UnitEventType.START_CONSTRUCTION_SITE_EVENT
                                        : UnitEventType.END_CONSTRUCTION_SITE_EVENT);
        fireUnitUpdate(eventType);
    }

    /**
     * Get the mission that is doing active work on site
     * @return
     */
    public Mission getWorkOnSite() {
        return activeWork;
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
     * Advance to the next phase
     * @return All completed
     */
    public boolean advanceToNextPhase() {
        if (phases.isEmpty()) return true;

        var nextPhase = phases.remove(0);
        this.isConstruction = nextPhase.construct();
        this.currentStage = new ConstructionStage(nextPhase.stage(), this, isConstruction);

        logger.info(this, "Advanced to next phase '" + nextPhase.stage().getName() + "' "
                + (isConstruction ? "construction" : "salvage"));

        fireUnitUpdate(UnitEventType.ADD_CONSTRUCTION_STAGE_EVENT, currentStage);
        return phases.isEmpty();
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

        // Fire construction event.
        fireUnitUpdate(UnitEventType.FINISH_CONSTRUCTION_BUILDING_EVENT, newBuilding);

        return newBuilding;
    }

    
	/**
	 * Salvage construction parts from the stage.
	 * @param site 
	 * @param stage 
	 * 
	 */
	public void reclaimParts(double skill) {
		logger.info(this, "Reclaimed parts");

		// Modify salvage chance based on building wear condition.
		// Note: if non-building construction stage, wear condition should be 100%.
		double salvageChance = (50D * .25D) + 25D; // Needs to be aware ot the source Building


		// Modify salvage chance based on average construction skill.
		salvageChance += skill * 5D;

		// Salvage construction parts.
		for(var e : currentStage.getInfo().getParts().entrySet()) {
			int part = e.getKey();
			int number = e.getValue();

			int salvagedNumber = 0;
			for (int x = 0; x < number; x++) {
				if (RandomUtil.lessThanRandPercent(salvageChance))
					salvagedNumber++;
			}

			var settlement = getAssociatedSettlement();
			if (salvagedNumber > 0) {
				Part p = ItemResourceUtil.findItemResource(part);
				double mass = salvagedNumber * p.getMassPerItem();
				double capacity = settlement.getCargoCapacity();
				if (mass <= capacity) {
					settlement.storeItemResource(part, salvagedNumber);
				}

			}
		}
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

    public boolean isRelocatable() {
        return unstarted && isConstruction;
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
        if (activeWork != null) {
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
