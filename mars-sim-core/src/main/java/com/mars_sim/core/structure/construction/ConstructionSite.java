/**
 * Mars Simulation Project
 * ConstructionSite.java
 * @date 2023-06-07
 * @author Scott Davis
 */

package com.mars_sim.core.structure.construction;

import java.util.Collection;
import java.util.List;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.MissionPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingConfig;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.unit.FixedUnit;
import com.mars_sim.core.vehicle.GroundVehicle;

/**
 * A building construction site.
 */
public class ConstructionSite extends FixedUnit {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(ConstructionSite.class.getName());


    // Data members
    private boolean undergoingConstruction;
    private boolean undergoingSalvage;
    private boolean manual;
    
    /**
     * Has the site location been confirmed ?
     */
    private boolean isSiteLocConfirmed;
    
    private boolean isMousePickedUp;
    
	// Unique identifier
	private int identifier;
	
	/** construction skill for this site. */
    private int constructionSkill;

    private double width;
    private double length;
    private LocalPosition position;
    private double facing;

    private Collection<Worker> members;
    private List<GroundVehicle> vehicles;

    private ConstructionStage foundationStage;
    private ConstructionStage frameStage;
    private ConstructionStage buildingStage;
    private ConstructionManager constructionManager;
    private ConstructionStageInfo stageInfo;

    private MissionPhase phase;

    private static BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
    
    /**
     * Constructor.
     */
    public ConstructionSite(Settlement settlement) {
    	super("Site", settlement);
    	
    	this.constructionManager = settlement.getConstructionManager();

    	identifier = constructionManager.getUniqueID();
    	
    	createSiteName();

    	
    	width = 0D;
        length = 0D;
        position = LocalPosition.DEFAULT_POSITION;
        facing = 0D;
        foundationStage = null;
        frameStage = null;
        buildingStage = null;
        undergoingConstruction = false;
        undergoingSalvage = false;
    }

	public void createSiteName() {
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

    /**
     * Sets the width of the construction site.
     * 
     * @param width the width (meters).
     */
    public void setWidth(double width) {
        this.width = width;
    }

    @Override
    public double getLength() {
        return length;
    }

    /**
     * Sets the length of the construction site.
     * 
     * @param length the length (meters).
     */
    public void setLength(double length) {
        this.length = length;
    }

    @Override
    public LocalPosition getPosition() {
    	return position;
    }
    
	public void setPosition(LocalPosition position2) {
		this.position = position2;
	}
	
    @Override
    public double getFacing() {
        return facing;
    }

    /**
     * Sets the facing of the construction site.
     * 
     * @param facing
     */
    public void setFacing(double facing) {
        this.facing = facing;
    }

    /**
     * Checks if all construction is complete at the site.
     * 
     * @return true if construction is complete.
     */
    public boolean isAllConstructionComplete() {
        if ((buildingStage != null) && !undergoingSalvage) return buildingStage.isComplete();
        else return false;
    }

    /**
     * Checks if all salvage is complete at the site.
     * 
     * @return true if salvage is complete.
     */
    public boolean isAllSalvageComplete() {
        if (undergoingSalvage) {
            if (foundationStage == null) return true;
            else return foundationStage.isComplete();
        }
        else return false;
    }

    /**
     * Checks if site is currently undergoing construction.
     * 
     * @return true if undergoing construction.
     */
    public boolean isUndergoingConstruction() {
        return undergoingConstruction;
    }

    /**
     * Checks if site is currently undergoing salvage.
     * 
     * @return true if undergoing salvage.
     */
    public boolean isUndergoingSalvage() {
        return undergoingSalvage;
    }

    /**
     * Sets if site is currently undergoing construction.
     * 
     * @param undergoingConstruction true if undergoing construction.
     */
    public void setUndergoingConstruction(boolean undergoingConstruction) {
        this.undergoingConstruction = undergoingConstruction;

        UnitEventType eventType = (undergoingConstruction  ? UnitEventType.START_CONSTRUCTION_SITE_EVENT
                                        : UnitEventType.END_CONSTRUCTION_SITE_EVENT);

        fireUnitUpdate(eventType);
    }

    /**
     * Sets if site is currently undergoing salvage.
     * 
     * @param undergoingSalvage true if undergoing salvage.
     */
    public void setUndergoingSalvage(boolean undergoingSalvage) {
        this.undergoingSalvage = undergoingSalvage;
        UnitEventType eventType =  (undergoingSalvage ? UnitEventType.START_CONSTRUCTION_SALVAGE_EVENT
                                                    : UnitEventType.FINISH_CONSTRUCTION_SALVAGE_EVENT);
        
        fireUnitUpdate(eventType);
    }

    /**
     * Gets the current construction stage at the site.
     * 
     * @return construction stage.
     */
    public ConstructionStage getCurrentConstructionStage() {
        ConstructionStage result = null;

        if (buildingStage != null) result = buildingStage;
        else if (frameStage != null) result = frameStage;
        else if (foundationStage != null) result = foundationStage;

        return result;
    }

    /**
     * Adds a new construction stage to the site.
     * 
     * @param stage the new construction stage.
     * @throws Exception if error adding construction stage.
     */
    public void addNewStage(ConstructionStage stage) {
        if (ConstructionStageInfo.Stage.FOUNDATION.equals(stage.getInfo().getType())) {
            if (foundationStage != null) throw new IllegalStateException("Foundation stage already exists.");
            foundationStage = stage;
        }
        else if (ConstructionStageInfo.Stage.FRAME.equals(stage.getInfo().getType())) {
            if (frameStage != null) throw new IllegalStateException("Frame stage already exists");
            if (foundationStage == null) throw new IllegalStateException("Foundation stage hasn't been added yet.");
            frameStage = stage;
        }
        else if (ConstructionStageInfo.Stage.BUILDING.equals(stage.getInfo().getType())) {
            if (buildingStage != null) throw new IllegalStateException("Building stage already exists");
            if (frameStage == null) throw new IllegalStateException("Frame stage hasn't been added yet.");
            buildingStage = stage;
        }
        else throw new IllegalStateException("Stage type: " + stage.getInfo().getType() + " not valid");

        // Update construction site dimensions.
        updateDimensions(stage);

        // Fire construction event.
        fireUnitUpdate(UnitEventType.ADD_CONSTRUCTION_STAGE_EVENT, stage);
    }

    /**
     * Updates the width and length dimensions to a construction stage.
     * 
     * @param stage the construction stage.
     */
    private void updateDimensions(ConstructionStage stage) {

        double stageWidth = stage.getInfo().getWidth();
        double stageLength = stage.getInfo().getLength();

        if (!stage.getInfo().isUnsetDimensions()) {
            if (stageWidth != width) {
                width = stageWidth;
            }
            if (stageLength != length) {
                length = stageLength;
            }
        }
        else {
            if ((stageWidth > 0D) && (stageWidth != width)) {
                width = stageWidth;
            }
            else if (width <= 0D) {
                // Use default width (may be modified later).
                width = 10D;
            }
            if ((stageLength > 0D) && (stageLength != length)) {
                length = stageLength;
            }
            else if (length <= 0D) {
                // Use default length (may be modified later).
                length = 10D;
            }
        }
    }

    /**
     * Removes a salvaged stage from the construction site.
     * 
     * @param stage the salvaged construction stage.
     * @throws Exception if error removing the stage.
     */
    public void removeSalvagedStage(ConstructionStage stage) {
        if (ConstructionStageInfo.Stage.BUILDING.equals(stage.getInfo().getType())) {
            buildingStage = null;
        }
        else if (ConstructionStageInfo.Stage.FRAME.equals(stage.getInfo().getType())) {
            frameStage = null;
        }
        else if (ConstructionStageInfo.Stage.FOUNDATION.equals(stage.getInfo().getType())) {
            foundationStage = null;
        }
        else throw new IllegalStateException("Stage type: " + stage.getInfo().getType() + " not valid");

        // Fire construction event.
        fireUnitUpdate(UnitEventType.REMOVE_CONSTRUCTION_STAGE_EVENT, stage);
    }

    /**
     * Removes the current salvaged construction stage.
     * 
     * @throws Exception if error removing salvaged construction stage.
     */
    public void removeSalvagedStage() {
        if (undergoingSalvage) {
            if (buildingStage != null) buildingStage = null;
            else if (frameStage != null) frameStage = null;
            else if (foundationStage != null) foundationStage = null;
            else throw new IllegalStateException("Construction site has no stage to remove");
        }
        else throw new IllegalStateException("Construction site is not undergoing salvage");
    }

    /**
     * Creates a new building from the construction site.
     * 
     * @return newly constructed building.

     */
    public Building createBuilding() {
        if (buildingStage == null) throw new IllegalStateException("Building stage doesn't exist");

        var settlement = getAssociatedSettlement();

        BuildingManager manager = settlement.getBuildingManager();
        int id = manager.getNextTemplateID();
        String buildingType = buildingStage.getInfo().getName();
        String uniqueName = manager.getUniqueName(buildingType);
        
        int zone = 0;
        var spec = buildingConfig.getBuildingSpec(buildingType);

        Building newBuilding = new Building(settlement, Integer.toString(id), zone, uniqueName,
        		new BoundedObject(position, width, length, facing), spec);
        
        manager.addBuilding(newBuilding, true);

        // Record completed building name.
        constructionManager = settlement.getConstructionManager();
        constructionManager.addConstructedBuildingLogEntry(buildingStage.getInfo().getName());

        // Clear construction value cache.
        constructionManager.getConstructionValues().clearCache();

        // Fire construction event.
        fireUnitUpdate(UnitEventType.FINISH_CONSTRUCTION_BUILDING_EVENT, newBuilding);

        return newBuilding;
    }

    /**
     * Gets the building name the site will construct.
     * 
     * @return building name or null if undetermined.
     */
    public String getBuildingName() {
        if (buildingStage != null) return buildingStage.getInfo().getName();
        else return null;
    }

    /**
     * Checks if the site's current stage is unfinished.
     * 
     * @return true if stage unfinished.
     */
    public boolean hasUnfinishedStage() {
        ConstructionStage currentStage = getCurrentConstructionStage();
        return (currentStage != null) && !currentStage.isComplete();
    }

    /**
     * Checks if this site contains a given stage.
     * 
     * @param stage the stage info.
     * @return true if contains stage.
     */
    public boolean hasStage(ConstructionStageInfo stage) {
        if (stage == null) throw new IllegalArgumentException("stage cannot be null");

        boolean result = false;
        if ((foundationStage != null) && foundationStage.getInfo().equals(stage)) result = true;
        else if ((frameStage != null) && frameStage.getInfo().equals(stage)) result = true;
        else if ((buildingStage != null) && buildingStage.getInfo().equals(stage)) result = true;

        return result;
    }

    /**
     * Relocates the construction site by changing its coordinates.
     */
	public void relocateSite() {
        var existingPosn = getPosition();
		// Compute a new position for a site
		ConstructionMission.positionNewSite(this);
		
		logger.info(this, "Manually relocated by player from " 
				+ existingPosn + " to "
				+ getPosition());
	}

    public ConstructionManager getConstructionManager() {
    	return constructionManager;
    }

    public void setSkill(int constructionSkill) {
    	this.constructionSkill = constructionSkill;
    }

    public int getSkill() {
    	return constructionSkill;
    }

	public void setMembers(Collection<Worker> members) {
		this.members = members;
	}

	public void setVehicles(List<GroundVehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public Collection<Worker> getMembers() {
		return members;
	}

	public List<GroundVehicle> getVehicles() {
		return vehicles;
	}

	public ConstructionStageInfo getStageInfo() {
		return stageInfo;
	}

	public void setStageInfo(ConstructionStageInfo stageInfo) {
		this.stageInfo = stageInfo;
	}

	public boolean getManual() {
		return manual;
	}

	public void setManual(boolean manual) {
		this.manual = manual;
	}

	// for triggering the alertDialog()
	public boolean isSiteLocConfirmed() {
		return isSiteLocConfirmed;
	}

	public void setSiteLocConfirmed(boolean value) {
		this.isSiteLocConfirmed = value;
	}

	public boolean isMousePicked() {
		return isMousePickedUp;
	}

	public void setMousePicked(boolean value) {
		isMousePickedUp = value;
	}

	@Override
	public UnitType getUnitType() {
		return UnitType.CONSTRUCTION;
	}

	/**
	 * Is this unit inside a settlement ?
	 *
	 * @return true if the unit is inside a settlement
	 */
	@Override
	public boolean isInSettlement() {
		return false;
	}
	
    @Override
    public String getDescription() {
		StringBuilder result = new StringBuilder();

		ConstructionStage stage = getCurrentConstructionStage();
		if (stage != null) {
			result.append(stage.getInfo().getName());
		}

		return result.toString();
	}
	
	public void setPhase(MissionPhase phase) {
		this.phase = phase;
	}
	
	public MissionPhase getPhase() {
		return phase;
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
    @Override
	public void destroy() {
        super.destroy();
		position = null;
	    members = null;
	    vehicles = null;
	    foundationStage = null;
	    frameStage = null;
	    buildingStage = null;
	    constructionManager = null;
	    stageInfo = null;
	}
}
