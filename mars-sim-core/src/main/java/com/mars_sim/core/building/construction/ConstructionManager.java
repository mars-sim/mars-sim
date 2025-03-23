/*
 * Mars Simulation Project
 * ConstructionManager.java
 * @date 2023-06-07
 * @author Scott Davis
 */
package com.mars_sim.core.building.construction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.LifeSupport;
import com.mars_sim.core.building.function.RoboticStation;
import com.mars_sim.core.data.History;
import com.mars_sim.core.data.History.HistoryItem;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;

/**
 * Manager for construction sites at a settlement.
 */
public class ConstructionManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ConstructionManager.class.getName());

	// Data members.
	/** Counter of unit identifiers. */
	private int uniqueId = 0;
	
	private Settlement settlement;
	/** The settlement's construction sites. */
	private List<ConstructionSite> sites;
	private ConstructionValues values;
	private SalvageValues salvageValues;
	private History<String> constructedBuildingLog;

	private UnitManager unitManager = Simulation.instance().getUnitManager();
	
	/**
	 * Constructor.
	 * 
	 * @param settlement the settlement.
	 */
	public ConstructionManager(Settlement settlement) {
		this.settlement = settlement;
		sites = new ArrayList<>();
		values = new ConstructionValues(settlement);
		salvageValues = new SalvageValues(settlement);
		constructedBuildingLog = new History<>();
	}

	public int getUniqueID() {
		uniqueId++;
		return uniqueId;
	}
	
	/**
	 * Gets all construction sites at the settlement.
	 * 
	 * @return list of construction sites.
	 */
	public List<ConstructionSite> getConstructionSites() {
		return new ArrayList<>(sites);
	}

	/**
	 * Returns the instance of all construction sites at the settlement.
	 * 
	 * @return list of construction sites.
	 */
	public List<ConstructionSite> getSites() {
		return sites;
	}


	/**
	 * Gets construction sites needing a construction mission.
	 * 
	 * @return list of construction sites.
	 */
	public List<ConstructionSite> getConstructionSitesNeedingConstructionMission() {
		List<ConstructionSite> result = new ArrayList<>();
		Iterator<ConstructionSite> i = sites.iterator();
		while (i.hasNext()) {
			ConstructionSite site = i.next();
			if (!site.isUndergoingConstruction() && !site.isUndergoingSalvage() &&
					!site.isAllConstructionComplete() && !site.isAllSalvageComplete()) {
				ConstructionStage currentStage = site.getCurrentConstructionStage();
				if (currentStage != null) {
					if (currentStage.isComplete()) {
					    result.add(site);
					}
					else if (!currentStage.isSalvaging()) {
					    boolean workNeeded = currentStage.getCompletableWorkTime() >
					            currentStage.getCompletedWorkTime();
					    boolean hasConstructionMaterials = hasMissingConstructionMaterials(currentStage);
					    if (workNeeded || hasConstructionMaterials) {
					        result.add(site);
					    }
					}
				}
				else {
				    result.add(site);
				}
			}
		}
		return result;
	}

	/**
	 * Checks if the settlement has any construction materials needed for the stage.
	 * 
	 * @param stage the construction stage.
	 * @return true if missing materials available.
	 */
	public boolean hasMissingConstructionMaterials(ConstructionStage stage) {

	    boolean result = false;

	    Iterator<Integer> i = stage.getMissingResources().keySet().iterator();
	    while (i.hasNext() && !result) {
	    	Integer resource = i.next();
	        double amountRequired = stage.getMissingResources().get(resource);
	        if (amountRequired > 0D) {
	            double amountStored = settlement.getAmountResourceStored(resource);
	            if (amountStored > 0D) {
	                result = true;
	            }
	        }
	    }

	    Iterator<Integer> j = stage.getMissingParts().keySet().iterator();
	    while (j.hasNext() && !result) {
	    	Integer part = j.next();
	        int numRequired = stage.getMissingParts().get(part);
	        if (numRequired > 0) {
	            int numStored = settlement.getItemResourceStored(part);
	            if (numStored > 0) {
	                result = true;
	            }
	        }
	    }

	    return result;
	}

	/**
	 * Gets construction sites needing a salvage mission.
	 * 
	 * @return list of construction sites.
	 */
	public List<ConstructionSite> getConstructionSitesNeedingSalvageMission() {
		List<ConstructionSite> result = new ArrayList<>();
		for(ConstructionSite site : sites) {
			if (!site.isUndergoingConstruction() && !site.isUndergoingSalvage() &&
					!site.isAllConstructionComplete() && !site.isAllSalvageComplete()) {
				ConstructionStage currentStage = site.getCurrentConstructionStage();
				if ((currentStage != null)
					&& (currentStage.isComplete() || currentStage.isSalvaging())) {
					result.add(site);
				}
			}
		}
		return result;
	}

	/**
	 * Creates a new construction site.
	 * 
	 * @return newly created construction site.
	 */
	public ConstructionSite createNewConstructionSite() {
		
		ConstructionSite site = new ConstructionSite(settlement);
		sites.add(site);
    	unitManager.addUnit(site);

		settlement.fireUnitUpdate(UnitEventType.START_CONSTRUCTION_SITE_EVENT, site);
		logger.info(site, "Just created and registered in ConstructionManager.");
		
		return site;
	}

	public Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Removes a construction site.
	 * 
	 * @param site the construction site to remove.
	 * @throws Exception if site doesn't exist.
	 */
	public void removeConstructionSite(ConstructionSite site) {
		if (sites.contains(site)) {
			sites.remove(site);
		}
		else throw new IllegalStateException("Construction site doesn't exist.");
	}

	/**
	 * Gets the construction values.
	 * 
	 * @return construction values.
	 */
	public ConstructionValues getConstructionValues() {
		return values;
	}

	/**
	 * Gets the salvage values.
	 * 
	 * @return salvage values.
	 */
	public SalvageValues getSalvageValues() {
		return salvageValues;
	}

	/**
	 * Adds a building log entry to the constructed buildings list.
	 * 
	 * @param buildingName the building name to add.
	 */
	void addConstructedBuildingLogEntry(String buildingName) {
		if (buildingName == null) throw new IllegalArgumentException("buildingName is null");
		
		constructedBuildingLog.add(buildingName);
	}

	/**
	 * Gets a log of all constructed buildings at the settlement.
	 * 
	 * @return list of ConstructedBuildingLogEntry
	 */
	public List<HistoryItem<String>> getConstructedBuildingLog() {
		return constructedBuildingLog.getChanges();
	}

	/**
	 * Creates a new salvaging construction site to replace a building.
	 * 
	 * @param salvagedBuilding the building to be salvaged.
	 * @return the construction site.
	 * @throws Exception if error creating construction site.
	 */
	public ConstructionSite createNewSalvageConstructionSite(Building salvagedBuilding) {

		// Remove building from settlement.
		BuildingManager buildingManager = salvagedBuilding.getBuildingManager();
		buildingManager.removeBuilding(salvagedBuilding);

		// Move any people in building to somewhere else in the settlement.
		if (salvagedBuilding.hasFunction(FunctionType.LIFE_SUPPORT)) {
			LifeSupport lifeSupport = salvagedBuilding.getLifeSupport();
			Iterator<Person> i = lifeSupport.getOccupants().iterator();
			while (i.hasNext()) {
				Person occupant = i.next();
				BuildingManager.removePersonFromBuilding(occupant, salvagedBuilding);
				BuildingManager.addPersonToRandomBuilding(occupant, buildingManager.getSettlement());
			}
		}

		// Move any robot in building to somewhere else in the settlement.
		if (salvagedBuilding.hasFunction(FunctionType.ROBOTIC_STATION)) {
			RoboticStation station = salvagedBuilding.getRoboticStation();
			Iterator<Robot> i = station.getRobotOccupants().iterator();
			while (i.hasNext()) {
				Robot occupant = i.next();
				BuildingManager.removeRobotFromBuilding(occupant, salvagedBuilding);
				BuildingManager.addRobotToRandomBuilding(occupant, buildingManager.getSettlement());
			}
		}

		// Add construction site.
		ConstructionSite site = createNewConstructionSite();
		site.setPosition(salvagedBuilding.getPosition());
		site.setFacing(salvagedBuilding.getFacing());
		ConstructionStageInfo buildingStageInfo = ConstructionUtil.getConstructionStageInfo(salvagedBuilding.getBuildingType());
		if (buildingStageInfo != null) {
			ConstructionStageInfo frameStageInfo = buildingStageInfo.getPrerequisiteStage();
			if (frameStageInfo != null) {
				ConstructionStageInfo foundationStageInfo = frameStageInfo.getPrerequisiteStage();
				if (foundationStageInfo != null) {
					// Add foundation stage.
					ConstructionStage foundationStage = new ConstructionStage(foundationStageInfo, site);
					foundationStage.setCompletedWorkTime(foundationStageInfo.getWorkTime());
					site.addNewStage(foundationStage);
				}

				// Add frame stage.
				ConstructionStage frameStage = new ConstructionStage(frameStageInfo, site);
				frameStage.setCompletedWorkTime(frameStageInfo.getWorkTime());
				site.addNewStage(frameStage);
			}

			// Add building stage and prepare for salvage.
			ConstructionStage buildingStage = new ConstructionStage(buildingStageInfo, site);
			buildingStage.setSalvaging(true);
			site.addNewStage(buildingStage);
		}
	
		// Clear construction values cache.
		values.clearCache();

		return site;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		settlement = null;
		sites.clear();
		sites = null;
		values.destroy();
		values = null;
		salvageValues.destroy();
		salvageValues = null;
		constructedBuildingLog = null;
	}
}
