/*
 * Mars Simulation Project
 * EVAMission.java
 * @date 2025-08-17
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.EVAOperation.LightLevel;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.vehicle.Rover;

abstract class EVAMission extends RoverMission {

	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(EVAMission.class.getName());
	
	private static final MissionPhase WAIT_SUNLIGHT = new MissionPhase("Mission.phase.waitSunlight");
	private static final MissionStatus EVA_SUIT_CANNOT_BE_LOADED = new MissionStatus("Mission.status.noEVASuits");

	// Maximum time to wait for sunrise
	protected static final double MAX_WAIT_SUBLIGHT = 400D;
	// Require sunlight to be stable for at least this long (in millisols) before resuming EVA
	private static final double SUNLIGHT_STABLE_MIN = 10D;
	// Treat two EVA sites closer than this as "the same place" to avoid micro-hops (km)
	private static final double MIN_SITE_SEPARATION_KM = 0.10D; // ~100 meters

	private static final String NOT_ENOUGH_SUNLIGHT = "EVA - Not enough sunlight";

    private MissionPhase evaPhase;
    private boolean activeEVA = true;
	private int containerID;
	private int containerNum;
	private LightLevel minSunlight;
	// Tracks when sunlight first became "good enough" during WAIT_SUNLIGHT (phase-relative millisols)
	private double sunlightOkSinceInWait = Double.NaN;

    protected EVAMission(MissionType missionType, 
            Worker startingPerson, Rover rover,
            MissionPhase evaPhase, LightLevel minSunlight) {
        super(missionType, startingPerson, rover);
        
        this.evaPhase = evaPhase;
		this.minSunlight = minSunlight;

		// Check suit although these may be claimed before loading
		int suits = MissionUtil.getNumberAvailableEVASuitsAtSettlement(getStartingSettlement());
		if (suits < getMembers().size()) {
			endMission(EVA_SUIT_CANNOT_BE_LOADED);
		}
    }
    
	@Override
	protected boolean determineNewPhase() {
		boolean handled = true;
		if (!super.determineNewPhase()) {
			if (TRAVELLING.equals(getPhase())) {
				if (isCurrentNavpointSettlement()) {
					startDisembarkingPhase();
				}
				else if (canStartEVA()) {
					setPhase(evaPhase, getCurrentNavpointDescription());
					phaseEVAStarted();
				}
			}
			else if (WAIT_SUNLIGHT.equals(getPhase())) {
				setPhase(evaPhase, getCurrentNavpointDescription());
				phaseEVAStarted();
			}
			else if (evaPhase.equals(getPhase())) {
				startTravellingPhase();
			}
			else {
				handled = false;
			}
		}
		return handled;
	}

	/**
	 * Can the EVA phase be started ?
	 * 
	 * @return 
	 */
	private boolean canStartEVA() {
		boolean result = false;
		if (isEnoughSunlightForEVA()) {
			result = true;
		}
		else {
			// Decide what to do
			MarsTime sunrise = surfaceFeatures.getOrbitInfo().getSunrise(getCurrentMissionLocation());
			if (surfaceFeatures.inDarkPolarRegion(getCurrentMissionLocation())
					|| (sunrise.getTimeDiff(getMarsTime()) > MAX_WAIT_SUBLIGHT)) {
				// No point waiting, move to next site
				logger.info(getVehicle(), "Continue travel, sunrise too late " + sunrise.getTruncatedDateTimeStamp());
				addMissionLog(NOT_ENOUGH_SUNLIGHT);
				startTravellingPhase();
			}
			else {
				// Wait for sunrise
				logger.info(getVehicle(), "Waiting for sunrise @ " + sunrise.getTruncatedDateTimeStamp());
				// reset stability window whenever we enter WAIT_SUNLIGHT
				sunlightOkSinceInWait = Double.NaN;
				setPhase(WAIT_SUNLIGHT, sunrise.getTruncatedDateTimeStamp());
			}
		}
		return result;
	}

	/**
	 * Check that if the sunlight is suitable to continue
	 * @param member Member triggering the waiting
	 */
	private void performWaitForSunlight(Worker member) {
		if (isEnoughSunlightForEVA()) {
			// Start (or continue) counting a stability window
			if (Double.isNaN(sunlightOkSinceInWait)) {
				sunlightOkSinceInWait = getPhaseDuration();
			}
			double stableFor = getPhaseDuration() - sunlightOkSinceInWait;
			if (stableFor >= SUNLIGHT_STABLE_MIN) {
				logger.info(getVehicle(), "Stop wait as enough sunlight");
				setPhaseEnded(true);
				sunlightOkSinceInWait = Double.NaN; // reset for next time
			}
		}
		else {
			// Sunlight dropped below the threshold; reset the stability window
			sunlightOkSinceInWait = Double.NaN;
			if (getPhaseDuration() > MAX_WAIT_SUBLIGHT) {
				logger.info(getVehicle(), "Waited long enough");
				setPhaseEnded(true);
				startTravellingPhase();
			}
		}
	}


	/**
	 * Is there enough sunlight to leave the vehicle for an EVA
	 * @return
	 */
	protected boolean isEnoughSunlightForEVA() {
		Coordinates locn = getCurrentMissionLocation();

		if (minSunlight == LightLevel.NONE) {
			// Don't bother calculating sunlight; EVA valid in whatever conditions
			return true;
		}

		// This is equivalent of a 1% sun ratio as below
		return (EVAOperation.isSunlightAboveLevel(locn, minSunlight)
					&& !surfaceFeatures.inDarkPolarRegion(locn));
	}

	/**
	 * Perform the current phase
	 */
	@Override
	protected void performPhase(Worker member) {
		super.performPhase(member);
		if (evaPhase.equals(getPhase())) {
			evaPhase(member);
		}
		else if (WAIT_SUNLIGHT.equals(getPhase())) {
			performWaitForSunlight(member);
		}
	}

	/**
	 * Ends the exploration at a site.
	 */
	@Override
	public void abortPhase() {
		if (evaPhase.equals(getPhase())) {

			logger.info(getVehicle(), "EVA ended due to external trigger.");

			endEVATasks();
		}
		else
			super.abortPhase();
	}

    /**
	 * End all EVA Operations
	 */
	protected void endEVATasks() {
		// End each member's EVA task.
		for (Worker member : getMembers()) {
			if (member instanceof Person person) {
				Task task = person.getMind().getTaskManager().getTask();
				if (task instanceof EVAOperation eo) {
					eo.requestEndEVA();
				}
			}
		}
	}
    
	/**
	 * Performs the explore site phase of the mission.
	 *
	 * @param member the mission member currently performing the mission
	 * @throws MissionException if problem performing phase.
	 */
	private void evaPhase(Worker member) {

		if (activeEVA) {
			// Check if crew has been at site for more than one sol.
			double timeDiff = getPhaseDuration();
			if (timeDiff > getEstimatedTimeAtEVASite(false)) {
				logger.info(getVehicle(), "Ran out of EVA site time.");
				activeEVA = false;
			}

			// If no one can explore the site and this is not due to it just being
			// night time, end the exploring phase.
			if (activeEVA && !isEnoughSunlightForEVA()) {
				logger.info(getVehicle(), "Not enough sunlight during the EVA phase of the mission.");
				addMissionLog(NOT_ENOUGH_SUNLIGHT);
				activeEVA = false;
			}

			// Anyone in the crew or a single person at the home settlement has a dangerous
			// illness, end phase.
			if (activeEVA && hasEmergency()) {
				logger.info(getVehicle(), "A medical emergency was reported during the EVA phase of the mission.");
				activeEVA = false;
			}

			// Check if enough resources for remaining trip. false = not using margin.
			if (activeEVA && !hasEnoughResourcesForRemainingMission()) {
				logger.info(getVehicle(), "Not enough resources was reported during the EVA phase of the mission.");
				activeEVA = false;
			}
			
			// All good so far, perform the EVA
			if (activeEVA) {
				// performEVA will check if rover capacity is full
				activeEVA = performEVA((Person) member);
				if (!activeEVA) {
					logger.info(member, "EVA operation Terminated.");
					addMissionLog("EVA operation Terminated.");
				}
			}
		} 

		// An EVA-ending event was triggered. End EVA phase.
		if (!activeEVA) {
			
			// First, clean up any "teleported" members to avoid stale state
			checkTeleported();

			if (isEveryoneInRover()) {
				// End phase
				phaseEVAEnded();
				setPhaseEnded(true);
			} 
			else {
				// Call everyone back inside
				endEVATasks();
			}
		}
	}

	/**
	 * Ensures no "teleported" person is still a member of this mission.
	 * Note: still investigating the cause and how to handle this.
	 */
	void checkTeleported() {

		// Collect first to avoid modifying underlying collection during iteration
		List<Worker> toRemove = new ArrayList<>();

		for (Iterator<Worker> i = getMembers().iterator(); i.hasNext();) {    
			Worker member = i.next();

			if (member instanceof Person p
				&& (p.isInSettlement() 
				|| p.isInSettlementVicinity()
				|| p.isRightOutsideSettlement())) {

				logger.severe(p, 10_000, "Invalid 'teleportation' detected. Current location: " 
						+ p.getLocationTag().getExtendedLocation() + ".");
				
				toRemove.add(member);
			}
		}

		// Perform proper removal with bookkeeping after iteration
		for (Worker member : toRemove) {
			removeMember(member);
		}
	}
	
    /**
     * Perform the specific EVA activities. This may cancel the EVA phase
     * @param person
     * @return
     */
	protected abstract boolean performEVA(Person person);

	/**
	 * Signal the start of an EVA phase to do any housekeeping
	 */
	protected void phaseEVAStarted() {
		activeEVA = true;
		// Clear any lingering WAIT_SUNLIGHT state
		sunlightOkSinceInWait = Double.NaN;
	}

	/**
	 * Notifies the sub-classes that the current EVA has ended. Trigger any housekeeping.
	 */
	protected void phaseEVAEnded() {
	}

    /**
     * Calculate the spare parts needed for the trip
     */
    @Override
	protected Map<Integer, Number> getSparePartsForTrip(double distance) {
		// Load the standard parts from VehicleMission.
		Map<Integer, Number> result = super.getSparePartsForTrip(distance);

		// Determine repair parts for EVA Suits.
		double evaTime = getEstimatedRemainingEVATime(true);
		double numberAccidents = evaTime * getMembers().size() * EVAOperation.BASE_ACCIDENT_CHANCE;

		// Assume the average number malfunctions per accident is 1.5.
		double numberMalfunctions = numberAccidents * MalfunctionManager.AVERAGE_EVA_MALFUNCTION;

		result.putAll(getEVASparePartsForTrip(numberMalfunctions));

		return result;
	}

    /**
     * Get the remaining mission time based on the travel and the remaining EVA time.
     * @return Mission time left.
     */
    @Override
	protected double getEstimatedRemainingMissionTime(boolean useBuffer) {
		double result = super.getEstimatedRemainingMissionTime(useBuffer);
		result += getEstimatedRemainingEVATime(useBuffer);
		return result;
	}

    /**
	 * Gets the range of a trip based on its time limit and collection sites.
	 *
	 * @param tripTimeLimit time (millisols) limit of trip.
	 * @param numSites      the number of collection sites.
	 * @param useBuffer     Use time buffer in estimations if true.
	 * @return range (km) limit.
	 */
	protected double getTripTimeRange(double tripTimeLimit, int numSites, boolean useBuffer) {
		// Include a small sunlight-wait allowance so planning matches runtime behavior.
		// (Consistent with getEstimatedRemainingEVATime(...) which already uses this mod.)
		double timeAtSites = getEstimatedTimeAtEVASite(useBuffer) * numSites * getSunriseWaitMod();
		double tripTimeTravellingLimit = tripTimeLimit - timeAtSites;
		if (tripTimeTravellingLimit <= 0D) {
			return 0D; // no travel time left once site time + wait are accounted for
		}
		double averageSpeed = getAverageVehicleSpeedForOperators();
		double averageSpeedMillisol = averageSpeed / MarsTime.MILLISOLS_PER_HOUR;
		return tripTimeTravellingLimit * averageSpeedMillisol;
	}

	/**
	 * Small planning allowance for waiting on sunlight during EVA.
	 * If EVA can occur in any light (NONE), no wait is assumed.
	 */
	private double getSunriseWaitMod() {
		// MAX_WAIT_SUBLIGHT is in millisols; convert to a multiplier like in getEstimatedRemainingEVATime()
		return (minSunlight == LightLevel.NONE) ? 1.0 : 1.0 + (MAX_WAIT_SUBLIGHT / 1000.0);
	}

	/**
	 * Gets the total number of EVA sites for this mission.
	 *
	 * @return number of sites.
	 */
	public final int getNumEVASites() {
		return getNavpoints().size() - 2;
	}

	/**
	 * Gets the number of EVA sites that have been currently visited by the
	 * mission.
	 *
	 * @return number of sites.
	 */
	private final int getNumEVASitesVisited() {
		int result = getCurrentNavpointIndex();
		if (result == (getNavpoints().size() - 1))
			result -= 1;
		return result;
	}

	/**
	 * Gets the estimated time remaining for exploration sites in the mission.
	 *
	 * @return time (millisols)
	 * @throws MissionException if error estimating time.
	 */
	private double getEstimatedRemainingEVATime(boolean buffer) {
		double result = 0D;
		double evaSiteTime = getEstimatedTimeAtEVASite(buffer);

		// Add estimated remaining exploration time at current site if still there.
		if (evaPhase.equals(getPhase())) {
			double remainingTime = evaSiteTime - getPhaseDuration();
			if (remainingTime > 0D)
				result += remainingTime;
		}
		
		double sunriseWaitMod = 1 + MAX_WAIT_SUBLIGHT / 1000D;
		
		// Add estimated EVA time at sites that haven't been visited yet.
		int remainingEVASites = getNumEVASites() - getNumEVASitesVisited();
		result += evaSiteTime * remainingEVASites * sunriseWaitMod;

		return result;
	}

	/**
	 * Defines equipment needed for the EVAs.
	 * 
	 * @param eqmType Equipment needed
	 * @param eqmNum Number of equipment
	 */
	protected void setEVAEquipment(EquipmentType eqmType, int eqmNum) {
		this.containerID = EquipmentType.getResourceID(eqmType);
		this.containerNum = eqmNum;
	}

	@Override
	protected Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		Map<Integer, Integer> result = super.getEquipmentNeededForRemainingMission(useBuffer);

		// Include required number of containers.
		if (containerID > 0) {
			result.put(containerID, containerNum);
		}
		return result;
	}

	/**
	 * Estimate the time needed at an EVA site.
	 * @param buffer Add a buffer allowance
	 * @return Estimated time per EVA site
	 */
	protected abstract double getEstimatedTimeAtEVASite(boolean buffer);

	@Override
	protected Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useBuffer) {
		Map<Integer, Number> result = super.getResourcesNeededForRemainingMission(useBuffer);

		double explorationSitesTime = getEstimatedRemainingEVATime(useBuffer);
		double timeSols = explorationSitesTime / 1000D;

		// Add the amount for the site visits
		result = addLifeSupportResources(result, getMembers().size(), timeSols, useBuffer);

		return result;
	}


	/**
	 * Order a list of Coordinates starting from a point to minimise
	 * the travel time.
	 * @param unorderedSites
	 * @param startingLocation
	 * @return
	 */
	public static List<Coordinates> getMinimalPath(Coordinates startingLocation, List<Coordinates> unorderedSites) {

		// 1) Greedy nearest-neighbor initial route (existing behavior)
		List<Coordinates> unvisited = new ArrayList<>(unorderedSites);
		List<Coordinates> orderedSites = new ArrayList<>(unvisited.size());
		Coordinates currentLocation = startingLocation;
		while (!unvisited.isEmpty()) {
			Coordinates shortest = unvisited.get(0);
			double shortestDistance = currentLocation.getDistance(shortest);
			for (int i = 1; i < unvisited.size(); i++) {
				Coordinates site = unvisited.get(i);
				double distance = currentLocation.getDistance(site);
				if (distance < shortestDistance) {
					shortest = site;
					shortestDistance = distance;
				}
			}
			unvisited.remove(shortest);
			orderedSites.add(shortest);
			currentLocation = shortest;
		}

		// 2) Tiny 2-opt improvement pass for an open path (start -> p0 -> ... -> pN)
		twoOptImprove(startingLocation, orderedSites);

		// 3) Prune micro-sites that are effectively the same location (avoid repeated EVA churn)
		return pruneCloseSites(orderedSites, MIN_SITE_SEPARATION_KM);
	}

	/**
	 * In-place 2-opt improvement for an open route:
	 * considers reversing segments [i..k] and keeps changes that reduce total distance.
	 * O(n^2), very fast for the usual small number of EVA sites.
	 */
	private static void twoOptImprove(Coordinates start, List<Coordinates> route) {
		final int n = route.size();
		if (n < 4) return; // nothing meaningful to optimize

		boolean improved = true;
		while (improved) {
			improved = false;
			for (int i = 0; i < n - 2; i++) {
				Coordinates prev = (i == 0) ? start : route.get(i - 1);
				Coordinates A = route.get(i);
				for (int k = i + 1; k < n - 1; k++) { // require k+1 exists
					Coordinates B = route.get(k);
					Coordinates next = route.get(k + 1);

					double oldCost = prev.getDistance(A) + B.getDistance(next);
					double newCost = prev.getDistance(B) + A.getDistance(next);

					if (newCost + 1e-9 < oldCost) {
						// Reverse the segment [i..k] to reduce total travel
						Collections.reverse(route.subList(i, k + 1));
						improved = true;
						// After reverse, A is now route.get(i); refresh for subsequent comparisons
						A = route.get(i);
					}
				}
			}
		}
	}

	/**
	 * Remove consecutive sites that are closer than a minimum separation.
	 * This prevents "stop, sample, move 50m, stop again" loops that waste time/supplies.
	 */
	private static List<Coordinates> pruneCloseSites(List<Coordinates> route, double minKm) {
		if (route.size() < 2) return route;
		List<Coordinates> pruned = new ArrayList<>(route.size());
		Coordinates lastKept = route.get(0);
		pruned.add(lastKept);
		for (int i = 1; i < route.size(); i++) {
			Coordinates c = route.get(i);
			if (lastKept.getDistance(c) >= minKm) {
				pruned.add(c);
				lastKept = c;
			}
		}
		return pruned;
	}
}
