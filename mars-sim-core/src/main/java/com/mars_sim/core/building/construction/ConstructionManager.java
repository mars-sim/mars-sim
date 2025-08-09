/*
 * Mars Simulation Project
 * ConstructionManager.java
 * @date 2023-06-07
 * @author Scott Davis
 */
package com.mars_sim.core.building.construction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.BuildingSpec;
import com.mars_sim.core.building.construction.ConstructionStageInfo.Stage;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.LifeSupport;
import com.mars_sim.core.building.function.RoboticStation;
import com.mars_sim.core.data.History;
import com.mars_sim.core.data.History.HistoryItem;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.ObjectiveUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Manager for construction sites at a settlement.
 */
public class ConstructionManager implements Serializable {


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
		return Collections.unmodifiableList(sites);
	}

	/**
	 * Gets construction sites needing a mission.
	 * 
	 * @param construction Search for sites under construction not salvage
	 * @return list of construction sites.
	 */
	public List<ConstructionSite> getConstructionSitesNeedingMission(boolean construction) {
		List<ConstructionSite> result = new ArrayList<>();
		for (ConstructionSite site : sites) {
			if (!site.isWorkOnSite() &&
					!site.isAllConstructionComplete() && !site.isAllSalvageComplete()
					&& site.isConstruction() == construction) {
				ConstructionStage currentStage = site.getCurrentConstructionStage();
				if (currentStage != null) {
					boolean workNeeded = !currentStage.isComplete();
					
					// If not construction, i.e. salvage, or has materials then workable
					workNeeded &= (!site.isConstruction() || currentStage.hasMissingConstructionMaterials());
					if (workNeeded) {
					    result.add(site);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Creates a new construction site.
	 * @param placement 
	 * @param bestBuilding 
	 * 
	 * @return newly created construction site.
	 */
	private ConstructionSite createNewConstructionSite(String buildingType, LocalBoundedObject placement,
						boolean isConstruction, ConstructionStageInfo initStage) {
		
		ConstructionSite site = new ConstructionSite(settlement, buildingType, isConstruction, initStage, placement);
		sites.add(site);
    	unitManager.addUnit(site);

		settlement.fireUnitUpdate(UnitEventType.START_CONSTRUCTION_SITE_EVENT, site);
		logger.info(site, "Just created and registered in ConstructionManager.");
		logger.info(settlement, "New site created for a " + buildingType + " starting "
					+ initStage.getName() + " called " + site);
		
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
			for(Person occupant : new ArrayList<>(lifeSupport.getOccupants())) {
				BuildingManager.removePersonFromBuilding(occupant, salvagedBuilding);
				BuildingManager.addPersonToRandomBuilding(occupant, buildingManager.getSettlement());
			}
		}

		// Move any robot in building to somewhere else in the settlement.
		if (salvagedBuilding.hasFunction(FunctionType.ROBOTIC_STATION)) {
			RoboticStation station = salvagedBuilding.getRoboticStation();
			for (Robot occupant : new ArrayList<>(station.getRobotOccupants())) {
				BuildingManager.removeRobotFromBuilding(occupant, salvagedBuilding);
				BuildingManager.addRobotToRandomBuilding(occupant, buildingManager.getSettlement());
			}
		}

		var bldStage = getStageInfo(salvagedBuilding.getBuildingType(), Stage.BUILDING);

		// Add construction site.
		return createNewConstructionSite(salvagedBuilding.getBuildingType(), salvagedBuilding, false, bldStage);
	}

	/**
	 * Determines the construction site based upon profit.
	 * 
	 * @param skill
	 */
	public ConstructionSite getNextSite(int skill) {

		var potentials = sites.stream()
					.filter(s -> s.isConstruction() && !s.isWorkOnSite())
					.filter(s -> s.getCurrentConstructionStage().getInfo().getBaseLevel() <= skill)
					.toList();

		if (!potentials.isEmpty()) {
			var site = RandomUtil.getRandomElement(potentials);
			logger.info(settlement, "Found existing site to work on " + site);
			return site;
		}
		
		// Should select from Q once in place
		BuildingSpec bestBuilding = getNeededBuilding();

		// Place the new building
		var placement = BuildingPlacement.placeSite(settlement, bestBuilding);
		if (placement == null) {
			logger.warning(settlement, "Can not find a placement for " + bestBuilding.getName());
			return null;
		}

		// Find the first state
		ConstructionStageInfo initStage = getStageInfo(bestBuilding.getName(), Stage.FOUNDATION);
		return createNewConstructionSite(bestBuilding.getName(), placement, true, initStage);
	}

	/**
	 * Find the best building to create for this settlement
	 * @return
	 */
	private BuildingSpec getNeededBuilding() {
		// This implementation will be repalced with picking something off the queue
		String buildingType = ObjectiveUtil.getBuildingType(settlement.getObjective());

		return SimulationConfig.instance().getBuildingConfiguration().getBuildingSpec(buildingType);
	}

	/**
	 * Find the construction stage details in teh chain to create a certain building type
	 * @param buildingType Type to create
	 * @param target The type of stage to locate
	 * @return
	 */
	public static ConstructionStageInfo getStageInfo(String buildingType, Stage target) {
		var consConfig = SimulationConfig.instance().getConstructionConfiguration();

		// Get the top level Building stage and walkbacks
		var stageInfo = consConfig.getConstructionStageInfoByName(buildingType);
		while(stageInfo.getType() != target) {
			stageInfo = stageInfo.getPrerequisiteStage();
		}

		return stageInfo;
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
