/*
 * Mars Simulation Project
 * Exploration.java
 * @date 2024-07-23
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.events.HistoricalEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mission.MetaMission;
import com.mars_sim.core.mission.objectives.ExplorationObjective;
import com.mars_sim.core.mission.task.ExploreSite;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;

/**
 * The Exploration class is a mission to travel in a rover to several random
 * locations around a settlement and collect rock samples.
 */
public class Exploration extends EVAMission
	implements SiteMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Exploration.class.getName());

	// Exploration mission event type
	public static final String SITE_EXPLORATION_EVENT = "explore site";

	/** Number of specimen containers required for the mission. */
	public static final int REQUIRED_SPECIMEN_CONTAINERS = 8;
	/** Amount of time to explore a site. */
	private static final double STANDARD_TIME_PER_SITE = 1000.0;
	
	/** Exploration Site */
	private static final String EXPLORATION_SITE = "Explore ";

	
	/** Mission Type enum. */
	public static final MissionType MISSION_TYPE = MissionType.EXPLORATION;

	/** Mission phase. */
	private static final MissionPhase EXPLORE_SITE = new MissionPhase("Mission.phase.exploreSite");
	private static final MissionStatus INVALID_EXPLORATION_SITE = new MissionStatus("Mission.status.invalidExplorationSite");
	
	private static final Set<ObjectiveType> OBJECTIVES = Set.of(ObjectiveType.TOURISM, ObjectiveType.TRANSPORTATION_HUB);

	private double currentSiteTime;
	
	private MineralSite currentSite;

	private ExplorationObjective objective;
	
	/** The set of sites to be claimed by this mission. */
	private List<MineralSite> claimedSites = new ArrayList<>();
	

	/**
	 * Constructor with explicit data.
	 *
	 * @param crew            collection of mission members.
	 * @param needsReview      whether the mission needs review.
	 * @param explorationSites   the sites to explore.
	 */
	public Exploration(MetaMission.Roster crew, boolean needsReview,
			List<MineralSite> explorationSites) {

		// Use RoverMission constructor.
		super(MISSION_TYPE, crew.leader(), (Rover)crew.vehicle(),
				EXPLORE_SITE, ExploreSite.LIGHT_LEVEL);
		
		this.objective = new ExplorationObjective();
		addObjective(objective);
				
		// Initialize explored sites.
		int buffer = (int)(getMembers().size() * 1.5);
		int newContainerNum = Math.max(buffer, REQUIRED_SPECIMEN_CONTAINERS);
		
		setEVAEquipment(EquipmentType.SPECIMEN_BOX, newContainerNum);
	
		// Set exploration navpoints.
		explorationSites.forEach(es -> addNavpoint(es.getCoordinates(), EXPLORATION_SITE + es.getName()));
		claimedSites.addAll(explorationSites);

		// Add home navpoint.
		Settlement s = getStartingSettlement();
		addNavpoint(s);

		// Check if vehicle can carry enough supplies for the mission. Must have NavPoints loaded
		if (!isVehicleLoadable()) {
			endMission(CANNOT_LOAD_RESOURCES);
		}

		// Add mission members.
		if (!isDone()) {
			addMembers(crew.members(), false);
			// Set initial mission phase.
			setInitialPhase(needsReview);
		}
	}

	/**
	 * Retrieves the current exploration site instance.
	 *
	 * @return
	 */
	private MineralSite retrieveASiteToClaim() {
		
		int idx = getCurrentNavpointIndex();
		idx--; // Decrement to allow for starting

		if (idx < 0 || idx >= claimedSites.size()) {
			logger.severe(this, "Cannot find Mineral site for navpoint index " + idx);
			return null;
		}
		return claimedSites.get(idx);
	}

	/**
	 * Updates the explored site and start an ExploreSite Task.
	 * 
	 * @param person
	 */
	@Override
	protected boolean performEVA(Person person) {
		
		boolean canAssign = false;
		
		// If person can explore the site, start that task.
		if (ExploreSite.canExploreSite(person)) {
			
			// Add new explored site if just starting exploring.
			if (currentSite == null) {
				currentSite = retrieveASiteToClaim();
				
				if (currentSite == null) {
					if (this instanceof AbstractVehicleMission avm) {
						// Calling AbstractVehicleMission's abortMission, not AbstractMission's abortMission
						avm.abortMission(INVALID_EXPLORATION_SITE, HistoricalEventType.MISSION_INVALID_SITE, person);
					}

					return false;
				}
			}
			canAssign = assignTask(person, new ExploreSite(person, currentSite, getRover(), this));
			
			if (canAssign) {
				logger.info(person, 20_000, "Ready to explore site and collect rocks.");
				
				// Update exploration site completion.
				double timePassed = getPhaseTimeElapsed();
				double completion = timePassed / STANDARD_TIME_PER_SITE;
				if (completion > 1D) {
					completion = 1D;
				}
				else if (completion < 0D) {
					completion = 0D;
				}		
				
				fireMissionUpdate(SITE_EXPLORATION_EVENT, getCurrentNavpointDescription());

				objective.updateSiteCompletion(getCurrentNavpointDescription(), completion);
			}
		}
		else {
			endEVATasks();
		}

		return true;
	}

	/**
	 * Ends the current EVA operations, i.e. getting everyone back to vehicle.
	 */
	@Override
	protected void endEVATasks() {
		super.endEVATasks();
		
		// Set the site to have been explored
		if (currentSite != null) {
			currentSite.setExplored(true);
		}
		
		currentSiteTime = 0D;
		currentSite = null;
	}

	/**
	 * Gets the estimated time spent at all exploration sites.
	 *
	 * @return time (millisols)
	 */
	protected double getEstimatedTimeOfAllEVAs() {
		return STANDARD_TIME_PER_SITE * getNumEVASites();
	}

	/**
	 * Gets a list of sites explored by the mission so far.
	 *
	 * @return list of explored sites.
	 */
	public List<MineralSite> getExploredSites() {
		return claimedSites;
	}

	/**
	 * Estimates the time needed at an EVA site.
	 * 
	 * @param buffer Add a buffer allowance
	 * @return Estimated time per EVA site
	 */
	protected double getEstimatedTimeAtEVASite(boolean buffer) {
		return STANDARD_TIME_PER_SITE;
	}

	/**
	 * Returns the average site score of all exploration sites.
	 */
	@Override
	public double getTotalSiteScore(Settlement reviewerSettlement) {
		if (claimedSites.isEmpty()) {
			return 0D;
		}

		int count = 0;
		double siteValue = 0D;
		for (MineralSite el : claimedSites) {
			count++;
			siteValue += Mining.getMiningSiteValue(el, reviewerSettlement);
		}

		if (count == 0)
			return 0;

		return siteValue / count;
	}

	/**
	 * Records the amount of resources collected.
	 * 
	 * @param resourceType
	 * @param samplesCollected
	 */
	public void recordResourceCollected(int resourceType, double samplesCollected) {
		objective.recordResourceCollected(resourceType, samplesCollected);
	}
	
	/**
	 * Adds the site time.
	 * 
	 * @param time
	 */
	public void addSiteTime(double time) {
		currentSiteTime += time;
	}
	
	/** 
	 * Gets amount of time to explore a site. 
	 */
	public double getCurrentSiteTime() {
		return currentSiteTime;
	}

	@Override
	public Set<ObjectiveType> getObjectiveSatisfied() {
		return OBJECTIVES;
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		claimedSites.clear();
		claimedSites = null;
	}
}
