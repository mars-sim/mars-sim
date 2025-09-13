/*
 * Mars Simulation Project
 * ConstructionManager.java
 * @date 2025-09-06
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
import com.mars_sim.core.building.config.BuildingSpec;
import com.mars_sim.core.building.construction.ConstructionSite.ConstructionPhase;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.LifeSupport;
import com.mars_sim.core.building.function.RoboticStation;
import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Manager for construction sites at a settlement.
 */
public class ConstructionManager implements Serializable {

	public static class BuildingSchedule implements Serializable {
		private static final long serialVersionUID = 1L;

		// Private handler to schedule the activiation
		private class Handler implements ScheduledEventHandler {
			private static final long serialVersionUID = 1L;

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
		 * Private handler for this queued item
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
	private List<BuildingSchedule> queue = new ArrayList<>();

	/**
	 * Constructor.
	 * 
	 * @param settlement the settlement.
	 */
	public ConstructionManager(Settlement settlement) {
		this.settlement = settlement;
		sites = new ArrayList<>();
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
	public List<ConstructionSite> getConstructionSitesNeedingMission() {
		List<ConstructionSite> result = new ArrayList<>();
		for (ConstructionSite site : sites) {
			if ((site.getWorkOnSite() == null) && !site.isComplete()) {
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
	 * 
	 * @param buildingType The final type of building
	 * @param placement Where the site is placed
	 * @param phases Phases required to create the construction.
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
		if (site.isProposed()) {
			sites.remove(site);
		}
	}


	/**
	 * Creates a new demolish construction site to replace a building.
	 * 
	 * @param demolist the building to be demolished.
	 * @throws Exception if error creating construction site.
	 */
	public void createNewSalvageConstructionSite(Building demolish) {
		// Remove building from settlement.
		BuildingManager buildingManager = demolish.getAssociatedSettlement().getBuildingManager();
		
		// Move any people in building to somewhere else in the settlement.
		List<Worker> occupants = new ArrayList<>();
		LifeSupport lifeSupport = demolish.getFunction(FunctionType.LIFE_SUPPORT);
		if (lifeSupport != null) {	
			occupants.addAll(lifeSupport.getOccupants());	
		}

		// Move any robot in building to somewhere else in the settlement.
		RoboticStation station= demolish.getFunction(FunctionType.ROBOTIC_STATION);
		if (station != null) {
			occupants.addAll(station.getRobotOccupants());
		}
		occupants.forEach(this::moveWorker);

		// What about people 
		buildingManager.removeBuilding(demolish);

		var bldStage = getConstructionStages(demolish.getBuildingType());
		if (bldStage.isEmpty()) {
			throw new IllegalStateException("No construction stages found for " + demolish.getBuildingType());
		}

		// Salvage so rotate the phases as demonlishing
		Collections.reverse(bldStage);
		var phases = bldStage.stream()
				.map(s -> new ConstructionPhase(s, false))
				.toList();

		// Add construction site.
		createNewConstructionSite(demolish.getBuildingType(), demolish, phases);
	}

	private void moveWorker(Worker w) {
		// Must be working in the affected Building
		w.getTaskManager().endCurrentTask();

		// Move them (should only be a single method
		if (w instanceof Person p)
			BuildingManager.addPersonToRandomBuilding(p, settlement);
		else if (w instanceof Robot r)
			BuildingManager.addRobotToRandomBuilding(r, settlement);
	}

	/**
	 * Determines the construction site based upon profit.
	 * 
	 * @param skill
	 */
	public ConstructionSite getNextConstructionSite(int skill) {

		var potentials = sites.stream()
					.filter(s -> s.getWorkOnSite() == null)
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
	 * Creates a new building site to build a building. This bypasses the queue.
	 * 
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
	 * Adds a building to the queue to be scheduled for construction.
	 * 
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
	 * Remove a previously added building from the queue
	 * @param item Schedule to be removed
	 * @return Was teh item found and removed
	 */
	public boolean removeBuildingFromQueue(BuildingSchedule item) {
		return queue.remove(item);
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
	 * @return list starting for first to last
	 */
	static List<ConstructionStageInfo> getConstructionStages(String buildingType) {
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
	}

	/**
	 * Can this building be demolished; it needs an associate ConstructionInfo
	 * @param b
	 * @return
	 */
	public boolean canDemolish(Building b) {
		return (!getConstructionStages(b.getBuildingType()).isEmpty());
	}

	/**
	 * Remove a site
	 * @param site
	 */
    public void removeSite(ConstructionSite site) {
        if (sites.contains(site) && site.isProposed()) {
			sites.remove(site);

			Simulation.instance().getUnitManager().removeUnit(site);
		}
    }
}