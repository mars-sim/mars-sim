/*
 * Mars Simulation Project
 * EVAMission.java
 * @date 2022-07-16
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.vehicle.Rover;

abstract class EVAMission extends RoverMission {

	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(EVAMission.class.getName());
	
	private static final MissionPhase WAIT_SUNLIGHT = new MissionPhase("Mission.phase.waitSunlight");
	private static final MissionStatus EVA_SUIT_CANNOT_BE_LOADED = new MissionStatus("Mission.status.noEVASuits");

	// Maximum time to wait for sunrise
	protected static final double MAX_WAIT_SUBLIGHT = 400D;

	private static final String NOT_ENOUGH_SUNLIGHT = "EVA - Not enough sunlight";

    private MissionPhase evaPhase;
    private boolean activeEVA = true;
	private int containerID;
	private int containerNum;

	// Does these EVA work in the dark?
	private boolean ignoreSunlight;

    protected EVAMission(MissionType missionType, 
            Worker startingPerson, Rover rover,
            MissionPhase evaPhase) {
        super(missionType, startingPerson, rover);
        
        this.evaPhase = evaPhase;

		// Check suit although these may be claimed before loading
		int suits = MissionUtil.getNumberAvailableEVASuitsAtSettlement(getStartingSettlement());
		if (suits < getMembers().size()) {
			endMission(EVA_SUIT_CANNOT_BE_LOADED);
		}
    }

	protected void setIgnoreSunlight(boolean newIgnore) {
		ignoreSunlight = newIgnore;
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
	 *
	 * @return Can the EVA phase be started
	 */
	private boolean canStartEVA() {
		boolean result = false;
		if (isEnoughSunlightForEVA()) {
			result = true;
		}
		else {
			// Decide what to do
			MarsTime sunrise = surfaceFeatures.getSunRise(getCurrentMissionLocation());
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
				setPhase(WAIT_SUNLIGHT, sunrise.getTruncatedDateTimeStamp());
			}
		}
		return result;
	}

		/**
	 * Check that if the sunlight is suitable to continue
	 * @param member
	 */
	private void performWaitForSunlight(Worker member) {
		if (isEnoughSunlightForEVA()) {
			logger.info(getRover(), "Stop wait as enough sunlight");
			setPhaseEnded(true);
		}
		else if (getPhaseDuration() > MAX_WAIT_SUBLIGHT) {
			logger.info(getRover(), "Waited long enough");
			setPhaseEnded(true);
			startTravellingPhase();
		}
	}


	/**
	 * Is there enough sunlight to leave the vehicle for an EVA
	 * @return
	 */
	protected boolean isEnoughSunlightForEVA() {
		return ignoreSunlight || EVAOperation.isEnoughSunlightForEVA(getCurrentMissionLocation());
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

			logger.info(getRover(), "EVA ended due to external trigger.");

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
		for(Worker member : getMembers()) {
			if (member instanceof Person) {
				Person person = (Person) member;
				Task task = person.getMind().getTaskManager().getTask();
				if (task instanceof EVAOperation) {
					((EVAOperation) task).endEVA();
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
				logger.info(getVehicle(), "Not enough sunlight.");
				addMissionLog(NOT_ENOUGH_SUNLIGHT);
				activeEVA = false;
			}

			// Anyone in the crew or a single person at the home settlement has a dangerous
			// illness, end phase.
			if (activeEVA && hasEmergency()) {
				logger.info(getVehicle(), "Had emergency.");
				activeEVA = false;
			}

			// Check if enough resources for remaining trip. false = not using margin.
			if (activeEVA && !hasEnoughResourcesForRemainingMission()) {
				activeEVA = false;
			}

			// All good so do the EVA
			if (activeEVA) {
				activeEVA = performEVA((Person) member);
				if (!activeEVA) {
					logger.info(getVehicle(), "EVA operation halted.");
					addMissionLog("EVA operation halted.");

				}
			}
		} 

		// EVA is not active so can th phase end?
		if (!activeEVA) {
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
     * Perform the specific EVA activities. This may cancel the EVA phase
     * @param person
     * @return
     */
	protected abstract boolean performEVA(Person person);

	/**
	 * Signak the start of an EVA phase to do any housekeeping
	 */
	protected void phaseEVAStarted() {
		activeEVA = true;
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
		double numberAccidents = evaTime * getMembers().size()* EVAOperation.BASE_ACCIDENT_CHANCE;

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
		double timeAtSites = getEstimatedTimeAtEVASite(useBuffer) * numSites;
		double tripTimeTravellingLimit = tripTimeLimit - timeAtSites;
		double averageSpeed = getAverageVehicleSpeedForOperators();
		double averageSpeedMillisol = averageSpeed / MarsTime.MILLISOLS_PER_HOUR;
		return tripTimeTravellingLimit * averageSpeedMillisol;
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
	public final int getNumEVASitesVisited() {
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
		
		double sunriseWaitMod = 1 + MAX_WAIT_SUBLIGHT/1000;
		
		// Add estimated EVA time at sites that haven't been visited yet.
		int remainingEVASites = getNumEVASites() - getNumEVASitesVisited();
		result += evaSiteTime * remainingEVASites * sunriseWaitMod;

		return result;
	}

	/**
	 * Define equipment needed for the EVAs
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

		List<Coordinates> unorderedSites2 = new ArrayList<>(unorderedSites);
		List<Coordinates> orderedSites = new ArrayList<>(unorderedSites2.size());
		Coordinates currentLocation = startingLocation;
		while (!unorderedSites2.isEmpty()) {
			Coordinates shortest = unorderedSites2.get(0);
			double shortestDistance = Coordinates.computeDistance(currentLocation, shortest);
			for(Coordinates site : unorderedSites2) {
				double distance = Coordinates.computeDistance(currentLocation, site);
				if (distance < shortestDistance) {
					shortest = site;
					shortestDistance = distance;
				}
			}

			unorderedSites2.remove(shortest);
			orderedSites.add(shortest);
			currentLocation = shortest;
		}

		return orderedSites;
	}
}
