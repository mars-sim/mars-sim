package org.mars_sim.msp.core.person.ai.mission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;

public abstract class EVAMission extends RoverMission {

    /** default logger. */
	private static SimLogger logger = SimLogger.getLogger(EVAMission.class.getName());

    private MissionPhase evaPhase;
    private boolean activeEVA = true;

	private int containerID;

	private int containerNum;

    protected EVAMission(String description, MissionType missionType, 
            MissionMember startingPerson, Rover rover,
            MissionPhase evaPhase) {
        super(description, missionType, startingPerson, rover);
        
        this.evaPhase = evaPhase;
    }

    
	@Override
	protected boolean determineNewPhase() {
		boolean handled = true;
		if (!super.determineNewPhase()) {
			if (TRAVELLING.equals(getPhase())) {
				if (getCurrentNavpoint().isSettlementAtNavpoint()) {
					startDisembarkingPhase();
				}
				else if (canStartEVA()) {
					activeEVA = true;
					setPhase(evaPhase, getCurrentNavpointDescription());
				}
			}
			else if (WAIT_SUNLIGHT.equals(getPhase())) {
				activeEVA = true;
				setPhase(evaPhase, getCurrentNavpointDescription());
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

	@Override
	protected void performPhase(MissionMember member) {
		super.performPhase(member);
		if (evaPhase.equals(getPhase())) {
			evaPhase(member);
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
		for(MissionMember member : getMembers()) {
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
	private void evaPhase(MissionMember member) {

		if (activeEVA) {
			// Check if crew has been at site for more than one sol.
			double timeDiff = getPhaseDuration();
			activeEVA = (timeDiff < getEstimatedTimeAtEVASite(false));

			// If no one can explore the site and this is not due to it just being
			// night time, end the exploring phase.
			activeEVA = activeEVA && isEnoughSunlightForEVA();

			// Anyone in the crew or a single person at the home settlement has a dangerous
			// illness, end phase.
			activeEVA = activeEVA && !hasEmergency();

			// Check if enough resources for remaining trip. false = not using margin.
			if (!hasEnoughResourcesForRemainingMission(false)) {
				// If not, determine an emergency destination.
				determineEmergencyDestination(member);
				activeEVA = false;
			}

			// All good so do the EVA
			if (activeEVA) {
				activeEVA = performEVA((Person) member);
			}
		} 

		// EVA is not active so can th phase end?
		if (!activeEVA) {
			if (isEveryoneInRover()) {
				// End phase
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
     * Calculate the spare parts needed for the trip
     */
    @Override
	protected Map<Integer, Number> getSparePartsForTrip(double distance) {
		// Load the standard parts from VehicleMission.
		Map<Integer, Number> result = super.getSparePartsForTrip(distance);

		// Determine repair parts for EVA Suits.
		double evaTime = getEstimatedRemainingEVATime(true);
		double numberAccidents = evaTime * getPeopleNumber() * EVAOperation.BASE_ACCIDENT_CHANCE;

		// Assume the average number malfunctions per accident is 1.5.
		double numberMalfunctions = numberAccidents * VehicleMission.AVERAGE_EVA_MALFUNCTION;

		result.putAll(getEVASparePartsForTrip(numberMalfunctions));

		return result;
	}

    /**
     * Get the remaining mission tiem based on the travel and the remaining EVA time.
     * @return Mission time left.
     */
    @Override
	protected double getEstimatedRemainingMissionTime(boolean useBuffer) {
		double result = super.getEstimatedRemainingMissionTime(useBuffer);
		result += getEstimatedRemainingEVATime(useBuffer);
		return result;
	}

    
	/**
	 * Gets the total number of EVA sites for this mission.
	 *
	 * @return number of sites.
	 */
	public final int getNumEVASites() {
		return getNumberOfNavpoints() - 2;
	}

	/**
	 * Gets the number of EVA sites that have been currently visited by the
	 * mission.
	 *
	 * @return number of sites.
	 */
	public final int getNumEVASitesVisited() {
		int result = getCurrentNavpointIndex();
		if (result == (getNumberOfNavpoints() - 1))
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

		// Add estimated EVA time at sites that haven't been visited yet.
		int remainingEVASites = getNumEVASites() - getNumEVASitesVisited();
		result += evaSiteTime * remainingEVASites;

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

		int crewNum = getPeopleNumber();

		// Add the maount for the site visits
		addLifeSupportResources(result, crewNum, timeSols, useBuffer);

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
