/**
 * Mars Simulation Project
 * ConstructionManager.java
 * @version 3.1.0 2017-09-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.construction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Manager for construction sites at a settlement.
 */
public class ConstructionManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private Settlement settlement;
	/** The settlement's construction sites. */
	private List<ConstructionSite> sites;
	private ConstructionValues values;
	private SalvageValues salvageValues;
	private List<ConstructedBuildingLogEntry> constructedBuildingLog;

	/**
	 * Constructor.
	 * @param settlement the settlement.
	 */
	public ConstructionManager(Settlement settlement) {
		this.settlement = settlement;
		sites = new ArrayList<ConstructionSite>();
		values = new ConstructionValues(settlement);
		salvageValues = new SalvageValues(settlement);
		constructedBuildingLog = new ArrayList<ConstructedBuildingLogEntry>();
	}

	/**
	 * Gets all construction sites at the settlement.
	 * @return list of construction sites.
	 */
	public List<ConstructionSite> getConstructionSites() {
		return new ArrayList<ConstructionSite>(sites);
	}

	/**
	 * Returns the instance of all construction sites at the settlement.
	 * @return list of construction sites.
	 */
	public List<ConstructionSite> getSites() {
		return sites;
	}


	/**
	 * Gets construction sites needing a construction mission.
	 * @return list of construction sites.
	 */
	public List<ConstructionSite> getConstructionSitesNeedingConstructionMission() {
		List<ConstructionSite> result = new ArrayList<ConstructionSite>();
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
					    boolean hasConstructionMaterials = hasRemainingConstructionMaterials(currentStage);
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
	 * @param stage the construction stage.
	 * @return true if remaining materials available.
	 */
	public boolean hasRemainingConstructionMaterials(ConstructionStage stage) {

	    boolean result = false;

	    Iterator<Integer> i = stage.getRemainingResources().keySet().iterator();
	    while (i.hasNext() && !result) {
	    	Integer resource = i.next();
	        double amountRequired = stage.getRemainingResources().get(resource);
	        if (amountRequired > 0D) {
	            double amountStored = settlement.getInventory().getAmountResourceStored(resource, false);
	            if (amountStored > 0D) {
	                result = true;
	            }
	        }
	    }

	    Iterator<Integer> j = stage.getRemainingParts().keySet().iterator();
	    while (j.hasNext() && !result) {
	    	Integer part = j.next();
	        int numRequired = stage.getRemainingParts().get(part);
	        if (numRequired > 0) {
	            int numStored = settlement.getInventory().getItemResourceNum(part);
	            if (numStored > 0) {
	                result = true;
	            }
	        }
	    }

	    return result;
	}

	/**
	 * Gets construction sites needing a salvage mission.
	 * @return list of construction sites.
	 */
	public List<ConstructionSite> getConstructionSitesNeedingSalvageMission() {
		List<ConstructionSite> result = new ArrayList<ConstructionSite>();
		Iterator<ConstructionSite> i = sites.iterator();
		while (i.hasNext()) {
			ConstructionSite site = i.next();
			if (!site.isUndergoingConstruction() && !site.isUndergoingSalvage() &&
					!site.isAllConstructionComplete() && !site.isAllSalvageComplete()) {
				ConstructionStage currentStage = site.getCurrentConstructionStage();
				if (currentStage != null) {
					if (currentStage.isComplete()) result.add(site);
					else if (currentStage.isSalvaging()) result.add(site);
				}
			}
		}
		return result;
	}

	/**
	 * Creates a new construction site.
	 * @return newly created construction site.
	 */
	public ConstructionSite createNewConstructionSite() {
		ConstructionSite result = new ConstructionSite(settlement);//, this);
		sites.add(result);
		settlement.fireUnitUpdate(UnitEventType.START_CONSTRUCTION_SITE_EVENT, result);
		return result;
	}

	public Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Removes a construction site.
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
	 * @return construction values.
	 */
	public ConstructionValues getConstructionValues() {
		return values;
	}

	/**
	 * Gets the salvage values.
	 * @return salvage values.
	 */
	public SalvageValues getSalvageValues() {
		return salvageValues;
	}

	/**
	 * Adds a building log entry to the constructed buildings list.
	 * @param buildingName the building name to add.
	 * @param builtTime the time stamp that construction was finished.
	 */
	void addConstructedBuildingLogEntry(String buildingName, MarsClock builtTime) {
		if (buildingName == null) throw new IllegalArgumentException("buildingName is null");
		else if (builtTime == null) throw new IllegalArgumentException("builtTime is null");
		else {
			ConstructedBuildingLogEntry logEntry =
					new ConstructedBuildingLogEntry(buildingName, builtTime);
			constructedBuildingLog.add(logEntry);
		}
	}

	/**
	 * Gets a log of all constructed buildings at the settlement.
	 * @return list of ConstructedBuildingLogEntry
	 */
	public List<ConstructedBuildingLogEntry> getConstructedBuildingLog() {
		return new ArrayList<ConstructedBuildingLogEntry>(constructedBuildingLog);
	}

	/**
	 * Creates a new salvaging construction site to replace a building.
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
				BuildingManager.removePersonOrRobotFromBuilding(occupant, salvagedBuilding);
				BuildingManager.addToRandomBuilding(occupant, buildingManager.getSettlement().getIdentifier());
			}
		}

		// 2015-12-23 Added handling robots
		// Move any robot in building to somewhere else in the settlement.
		if (salvagedBuilding.hasFunction(FunctionType.ROBOTIC_STATION)) {
			RoboticStation station = salvagedBuilding.getRoboticStation();
			Iterator<Robot> i = station.getRobotOccupants().iterator();
			while (i.hasNext()) {
				Robot occupant = i.next();
				BuildingManager.removePersonOrRobotFromBuilding(occupant, salvagedBuilding);
				BuildingManager.addToRandomBuilding(occupant, buildingManager.getSettlement().getIdentifier());
			}
		}

		// Add construction site.
		ConstructionSite site = createNewConstructionSite();
		site.setXLocation(salvagedBuilding.getXLocation());
		site.setYLocation(salvagedBuilding.getYLocation());
		site.setFacing(salvagedBuilding.getFacing());
		ConstructionStageInfo buildingStageInfo = ConstructionUtil.getConstructionStageInfo(salvagedBuilding.getBuildingType());
		if (buildingStageInfo != null) {
			String frameName = buildingStageInfo.getPrerequisiteStage();
			ConstructionStageInfo frameStageInfo = ConstructionUtil.getConstructionStageInfo(frameName);
			if (frameStageInfo != null) {
				String foundationName = frameStageInfo.getPrerequisiteStage();
				ConstructionStageInfo foundationStageInfo = ConstructionUtil.getConstructionStageInfo(foundationName);
				if (foundationStageInfo != null) {
					// Add foundation stage.
					ConstructionStage foundationStage = new ConstructionStage(foundationStageInfo, site);
					foundationStage.setCompletedWorkTime(foundationStageInfo.getWorkTime());
					site.addNewStage(foundationStage);

					// Add frame stage.
					ConstructionStage frameStage = new ConstructionStage(frameStageInfo, site);
					frameStage.setCompletedWorkTime(frameStageInfo.getWorkTime());
					site.addNewStage(frameStage);

					// Add building stage and prepare for salvage.
					ConstructionStage buildingStage = new ConstructionStage(buildingStageInfo, site);
					buildingStage.setSalvaging(true);
					site.addNewStage(buildingStage);
				}
				else throw new IllegalStateException("Could not find foundation construction stage for building: " + salvagedBuilding.getBuildingType());
			}
			else throw new IllegalStateException("Could not find frame construction stage for building: " + salvagedBuilding.getBuildingType());
		}
		else throw new IllegalStateException("Could not find building construction stage for building: " + salvagedBuilding.getBuildingType());

		// Clear construction values cache.
		values.clearCache();

		return site;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		settlement = null;
		sites.clear();
		sites = null;
		values.destroy();
		values = null;
		salvageValues.destroy();
		salvageValues = null;
		constructedBuildingLog.clear();
		constructedBuildingLog = null;
	}
}