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
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.BuildingSpec;
import com.mars_sim.core.building.construction.ConstructionSite.ConstructionPhase;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.LifeSupport;
import com.mars_sim.core.building.function.RoboticStation;
import com.mars_sim.core.data.History;
import com.mars_sim.core.data.History.HistoryItem;
import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Manager for construction sites at a settlement.
 */
public class ConstructionManager implements Serializable {

	public static class BuildingSchedule implements Serializable {
		// Private handler to schedule the activiation
		private class Handler implements ScheduledEventHandler {
			@Override
			public String getEventDescription() {
				return "Queue new building " + buildingType;
			}

			@Override
			public int execute(MarsTime currentTime) {
				valid = null;
				return 0;
			}

		}

		private String buildingType;
		private MarsTime valid;

		private BuildingSchedule(String buildingType, MarsTime valid) {
			this.buildingType = buildingType;
			this.valid = valid;
		}

		public String getBuildingType() {
			return buildingType;
		}

		public MarsTime getStart() {
			return valid;
		}

		/**
		 * Privbate handler for this queued item
		 * @return
		 */
		public ScheduledEventHandler getHandler() {
			return new Handler();
		}

		/**
		 * Is this schedule ready to start building
		 * @return
		 */
		public boolean isReady() {
			return valid == null;
		}
	}

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ConstructionManager.class.getName());

	// Data members.
	/** Counter of unit identifiers. */
	private int uniqueId = 0;
	
	private Settlement settlement;
	private List<ConstructionSite> sites;
	private SalvageValues salvageValues;
	private History<String> constructedBuildingLog;
	private List<BuildingSchedule> queue = new ArrayList<>();

	/**
	 * Constructor.
	 * 
	 * @param settlement the settlement.
	 */
	public ConstructionManager(Settlement settlement) {
		this.settlement = settlement;
		sites = new ArrayList<>();
		salvageValues = new SalvageValues(settlement);
		constructedBuildingLog = new History<>();
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
			if ((site.getWorkOnSite() == null) &&
					!site.isComplete()&& site.isConstruction() == construction) {
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
	 * @param buildingType The final type of building
	 * @param placement Where the site is placed
	 * @param phases P{hases required to create the construction.
	 */
	private ConstructionSite createNewConstructionSite(String buildingType, LocalBoundedObject placement,
						 List<ConstructionPhase> phases) {
		String siteName = String.format("Site %s-%03d", settlement.getSettlementCode(), uniqueId++);

		ConstructionSite site = new ConstructionSite(settlement, siteName, buildingType, phases, placement);
		sites.add(site);
    	Simulation.instance().getUnitManager().addUnit(site);

		settlement.fireUnitUpdate(UnitEventType.START_CONSTRUCTION_SITE_EVENT, site);
		logger.info(site, "Just created for a " + buildingType);
		
		return site;
	}

	/**
	 * Removes a construction site.
	 * 
	 * @param site the construction site to remove.
	 * @throws Exception if site doesn't exist.
	 */
	public void removeConstructionSite(ConstructionSite site) {
		sites.remove(site);
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

		var bldStage = getConstructionStages(salvagedBuilding.getBuildingType());
		if (bldStage.isEmpty()) {
			throw new IllegalStateException("No construction stages found for " + salvagedBuilding.getBuildingType());
		}

		// Salvage so rotate the phases as demonlishing
		Collections.reverse(bldStage);
		var phases = bldStage.stream()
				.map(s -> new ConstructionPhase(s, false))
				.toList();

		// Add construction site.
		return createNewConstructionSite(salvagedBuilding.getBuildingType(), salvagedBuilding, phases);
	}

	/**
	 * Determines the construction site based upon profit.
	 * 
	 * @param skill
	 */
	public ConstructionSite getNextConstructionSite(int skill) {

		var potentials = sites.stream()
					.filter(s -> s.isConstruction() && (s.getWorkOnSite() == null))
					.filter(s -> s.getCurrentConstructionStage().getInfo().getBaseLevel() <= skill)
					.toList();

		if (!potentials.isEmpty()) {
			var site = RandomUtil.getRandomElement(potentials);
			logger.info(settlement, "Found existing site to work on " + site);
			return site;
		}

		// Should select from Q once in place
		if (!queue.isEmpty()) {
			var first = queue.stream()
					.filter(s -> s.isReady())
					.findFirst().orElse(null);
			if (first != null) {
				var bestBuilding = getBuildingSpec(first.getBuildingType());
				queue.remove(first);
				return createNewBuildingSite(bestBuilding);
			}
		}
		return null;
	}

	/**
	 * Create a new building site to build a building. This bypasses the queue
	 * @param building
	 * @return
	 */
	public ConstructionSite createNewBuildingSite(BuildingSpec building) {
		// Place the new building
		var placement = BuildingPlacement.placeSite(settlement, building);
		if (placement == null) {
			logger.warning(settlement, "Can not find a placement for " + building.getName());
			return null;
		}

		// Find the first state
		var bldStage = getConstructionStages(building.getName());
		var phases = bldStage.stream()
				.map(s -> new ConstructionPhase(s, true))
				.toList();

		return createNewConstructionSite(building.getName(), placement, phases);
	}

	/**
	 * Add a building to the queue to be scheduled for construction
	 * @param buildingType
	 * @param when This can be null if schedule now
	 */
	public void addBuildingToQueue(String buildingType, MarsTime when) {
		var bs = new BuildingSchedule(buildingType, when);
		queue.add(bs);
		if (when != null) {
			settlement.getFutureManager().addEvent(when, bs.getHandler());
		}
	}

	/**
	 * Get the building schedule
	 * @return
	 */
	public List<BuildingSchedule> getBuildingSchedule() {
		return queue;
	}

	private static BuildingSpec getBuildingSpec(String buildingType) {
		return SimulationConfig.instance().getBuildingConfiguration().getBuildingSpec(buildingType);
	}

	/**
	 * Find the construction stages needed to build a building.
	 * @param buildingType Type to create
	 * @return List starting for first to last
	 */
	public static List<ConstructionStageInfo> getConstructionStages(String buildingType) {
		var consConfig = SimulationConfig.instance().getConstructionConfiguration();

		List<ConstructionStageInfo> results = new ArrayList<>();
		// Get the top level Building stage and walkbacks
		var stageInfo = consConfig.getConstructionStageInfoByName(buildingType);
		while(stageInfo != null) {
			results.add(0, stageInfo);
			stageInfo = stageInfo.getPrerequisiteStage();
		}

		return results;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		sites.clear();
		salvageValues.destroy();
		salvageValues = null;
		constructedBuildingLog = null;
	}
}