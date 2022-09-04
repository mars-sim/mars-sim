/**
 * Mars Simulation Project
 * TravelToSettlement.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The TravelToSettlement class is a mission to travel from one settlement to
 * another randomly selected one within range of an available rover. TODO
 * externalize strings
 */
public class TravelToSettlement extends RoverMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final Logger logger = Logger.getLogger(TravelToSettlement.class.getName());
	
	// Static members
	public static final double BASE_MISSION_WEIGHT = 1D;

	private static final double RELATIONSHIP_MODIFIER = 10D;

	private static final double JOB_MODIFIER = 1D;

	private static final double CROWDING_MODIFIER = 50D;

	private static final double SCIENCE_MODIFIER = 1D;

	private static final double RANGE_BUFFER = .8D;

	// Data members
	private Settlement destinationSettlement;

	/**
	 * Constructor with destination settlement randomly determined.
	 * 
	 * @param startingMember the mission member starting the mission.
	 */
	public TravelToSettlement(Worker startingMember, boolean needsReview) {
		// Use RoverMission constructor
		super(MissionType.TRAVEL_TO_SETTLEMENT, startingMember, null);

		Settlement s = getStartingSettlement();

		if (!isDone() && s != null) {

			// Choose destination settlement.
			setDestinationSettlement(getRandomDestinationSettlement(startingMember, s));
			if (destinationSettlement != null) {
				addNavpoint(destinationSettlement);
				setName(Msg.getString("Mission.description.travelToSettlement.detail",
						destinationSettlement.getName())); // $NON-NLS-1$)
			}
			else {
				endMissionProblem(startingMember, "No destination");
			}

			// Check mission available space
			if (!isDone()) {
				int availableSpace = destinationSettlement.getPopulationCapacity()
						- destinationSettlement.getNumCitizens();

				if (availableSpace < getMissionCapacity()) {
					setMissionCapacity(availableSpace);
				}
			}

			// Recruit additional members to mission.
			if (!isDone()) {
				if (!recruitMembersForMission(startingMember, 2))
					return;
			}

			// Check if vehicle can carry enough supplies for the mission.
			if (hasVehicle() && !isVehicleLoadable()) {
				endMission(CANNOT_LOAD_RESOURCES);
				return;
			}

			// Set initial phase
			setInitialPhase(needsReview);
		}
	}

	public TravelToSettlement(Collection<Worker> members, 
			Settlement destinationSettlement, Rover rover) {
		// Use RoverMission constructor.
		super(MissionType.TRAVEL_TO_SETTLEMENT, (Worker) members.toArray()[0], rover);

		// Set mission destination.
		setDestinationSettlement(destinationSettlement);
		addNavpoint(this.destinationSettlement);

		// Add mission members.
		addMembers(members, false);

		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			endMission(CANNOT_LOAD_RESOURCES);
			return;
		}

		setInitialPhase(false);
	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 * 
	 * @throws MissionException if problem setting a new phase.
	 */
	@Override
	protected boolean determineNewPhase() {
		boolean handled = true;
	
		if (!super.determineNewPhase()) {
			if (TRAVELLING.equals(getPhase())) {
				if (isCurrentNavpointSettlement()) {
					startDisembarkingPhase();
				}
			} 
			else {
				handled = false;
			}
		}
		return handled;
	}

	/**
	 * Sets the destination settlement.
	 * 
	 * @param destinationSettlement the new destination settlement.
	 */
	private void setDestinationSettlement(Settlement destinationSettlement) {
		this.destinationSettlement = destinationSettlement;
		fireMissionUpdate(MissionEventType.DESTINATION_SETTLEMENT);
	}

	/**
	 * Gets the destination settlement.
	 * 
	 * @return destination settlement
	 */
	public final Settlement getDestinationSettlement() {
		return destinationSettlement;
	}

	/**
	 * Determines a random destination settlement other than current one.
	 * 
	 * @param member             the mission member searching for a settlement.
	 * @param startingSettlement the settlement the mission is starting at.
	 * @return randomly determined settlement
	 */
	private Settlement getRandomDestinationSettlement(Worker member, Settlement startingSettlement) {

		double range = getVehicle().getRange(MissionType.TRAVEL_TO_SETTLEMENT);
		Settlement result = null;

		// Find all desirable destination settlements.
		Map<Settlement, Double> desirableSettlements = getDestinationSettlements(member, startingSettlement, range);

		// Randomly select a desirable settlement.
		if (desirableSettlements.size() > 0) {
			result = RandomUtil.getWeightedRandomObject(desirableSettlements);
		}

		return result;
	}


	/**
	 * Gets all possible and desirable destination settlements.
	 * 
	 * @param member             the mission member searching for a settlement.
	 * @param startingSettlement the settlement the mission is starting at.
	 * @param range              the range (km) that can be travelled.
	 * @return map of destination settlements.
	 */
	public static Map<Settlement, Double> getDestinationSettlements(Worker member, Settlement startingSettlement,
			double range) {
		Map<Settlement, Double> result = new HashMap<>();

		Iterator<Settlement> i = unitManager.getSettlements().iterator();

		while (i.hasNext()) {
			Settlement settlement = i.next();
			double distance = startingSettlement.getCoordinates().getDistance(settlement.getCoordinates());
			boolean isTravelDestination = isCurrentTravelDestination(settlement);
			if ((startingSettlement != settlement) && (distance <= (range * RANGE_BUFFER)) && !isTravelDestination) {

				double desirability = getDestinationSettlementDesirability(member, startingSettlement, settlement);
				if (desirability > 0D)
					result.put(settlement, desirability);
			}
		}

		return result;
	}

	/**
	 * Checks if a settlement is the destination of a current travel to settlement
	 * mission.
	 * 
	 * @param settlement the settlement.
	 * @return true if settlement is a travel destination.
	 */
	private static boolean isCurrentTravelDestination(Settlement settlement) {

		boolean result = false;

		Iterator<Mission> i = Simulation.instance().getMissionManager().getMissions().iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof TravelToSettlement) {
				Settlement destination = ((TravelToSettlement) mission).getDestinationSettlement();
				if (settlement.equals(destination)) {
					result = true;
				}
			}
		}

		return result;
	}

	/**
	 * Gets the desirability of the destination settlement.
	 * 
	 * @param member                the mission member looking at the settlement.
	 * @param startingSettlement    the settlement the member is already at.
	 * @param destinationSettlement the new settlement.
	 * @return negative or positive desirability weight value.
	 */
	private static double getDestinationSettlementDesirability(Worker member, Settlement startingSettlement,
			Settlement destinationSettlement) {

		// Determine relationship factor in destination settlement relative to
		// starting settlement.
		double relationshipFactor = 0D;

		if (member instanceof Person) {
			Person person = (Person) member;
			double currentOpinion = RelationshipUtil.getAverageOpinionOfPeople(person,
					startingSettlement.getAllAssociatedPeople());
			double destinationOpinion = RelationshipUtil.getAverageOpinionOfPeople(person,
					destinationSettlement.getAllAssociatedPeople());
			relationshipFactor = (destinationOpinion - currentOpinion) / 100D;
		}

		// Determine job opportunities in destination settlement relative to
		// starting settlement.
		double jobFactor = 0D;
		if (member instanceof Person) {
			Person person = (Person) member;
			JobType currentJob = person.getMind().getJob();
			double currentJobProspect = JobUtil.getJobProspect(person, currentJob, startingSettlement, true);
			double destinationJobProspect = 0D;

			if (person.getMind().getJobLock()) {
				destinationJobProspect = JobUtil.getJobProspect(person, currentJob, destinationSettlement, false);
			} else {
				destinationJobProspect = JobUtil.getBestJobProspect(person, destinationSettlement, false);
			}

			if (destinationJobProspect > currentJobProspect) {
				jobFactor = 1D;
			} else if (destinationJobProspect < currentJobProspect) {
				jobFactor = -1D;
			}
		}

		// Determine available space in destination settlement relative to
		// starting settlement.
		int startingCrowding = startingSettlement.getPopulationCapacity()
				- startingSettlement.getNumCitizens() - 1;
		int destinationCrowding = destinationSettlement.getPopulationCapacity()
				- destinationSettlement.getNumCitizens();
		int crowdingFactor = destinationCrowding - startingCrowding;

		// Determine science achievement factor for destination relative to starting
		// settlement.
		double totalScienceAchievementFactor = (destinationSettlement.getTotalScientificAchievement()
				- startingSettlement.getTotalScientificAchievement()) / 10D;
		double jobScienceAchievementFactor = 0D;

		if (member instanceof Person) {
			Person person = (Person) member;
			ScienceType jobScience = ScienceType.getJobScience(person.getMind().getJob());
			if (jobScience != null) {
				double startingJobScienceAchievement = startingSettlement.getScientificAchievement(jobScience);
				double destinationJobScienceAchievement = destinationSettlement.getScientificAchievement(jobScience);
				jobScienceAchievementFactor = destinationJobScienceAchievement - startingJobScienceAchievement;
			}
		}

		double scienceAchievementFactor = totalScienceAchievementFactor + jobScienceAchievementFactor;

		if (destinationCrowding < RoverMission.MIN_GOING_MEMBERS) {
			return 0;
		}

		// Return the sum of the factors with modifiers.
		return (relationshipFactor * RELATIONSHIP_MODIFIER) + (jobFactor * JOB_MODIFIER)
				+ (crowdingFactor * CROWDING_MODIFIER) + (scienceAchievementFactor * SCIENCE_MODIFIER);
	}

	@Override
	public double getMissionQualification(Worker member) {
		double result = super.getMissionQualification(member);

		if (member instanceof Person) {
			Person person = (Person) member;

			// Add modifier for average relationship with inhabitants of
			// destination settlement.
			if (destinationSettlement != null) {
				Collection<Person> destinationInhabitants = destinationSettlement.getAllAssociatedPeople();
				double destinationSocialModifier = (RelationshipUtil.getAverageOpinionOfPeople(person,
						destinationInhabitants) - 50D) / 50D;
				result += destinationSocialModifier;
			}

			// Subtract modifier for average relationship with non-mission
			// inhabitants of starting settlement.
			if (getStartingSettlement() != null) {
				Collection<Person> startingInhabitants = new ArrayList<>(getStartingSettlement().getAllAssociatedPeople());
				startingInhabitants.removeAll(getMembers());
				double startingSocialModifier = (RelationshipUtil.getAverageOpinionOfPeople(person,
						startingInhabitants) - 50D) / 50D;
				result -= startingSocialModifier;
			}

			// If person has the "Driver" job, add 1 to their qualification.
			if (person.getMind().getJob() == JobType.PILOT) {
				result += 1D;
			}

			if (person.getMind().getJob() == JobType.POLITICIAN) {
				result += 10D;
			}
        }

		return result;
	}

	/**
	 * Gets the settlement associated with the mission.
	 * 
	 * @return settlement or null if none.
	 */
	@Override
	public Settlement getAssociatedSettlement() {
		return destinationSettlement;
	}

	/**
	 * Compares the quality of two vehicles for use in this mission. (This method
	 * should be added to by children)
	 * 
	 * @param firstVehicle  the first vehicle to compare
	 * @param secondVehicle the second vehicle to compare
	 * @return -1 if the second vehicle is better than the first vehicle, 0 if
	 *         vehicle are equal in quality, and 1 if the first vehicle is better
	 *         than the second vehicle.
	 * @throws IllegalArgumentException if firstVehicle or secondVehicle is null.
	 * @throws MissionException         if error comparing vehicles.
	 */
	@Override
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) {
		int result = super.compareVehicles(firstVehicle, secondVehicle);

		if ((result == 0) && isUsableVehicle(firstVehicle) && isUsableVehicle(secondVehicle)) {
			// Check if one can hold more crew than the other.
			if (((Rover) firstVehicle).getCrewCapacity() > ((Rover) secondVehicle).getCrewCapacity()) {
				result = 1;
			} else if (((Rover) firstVehicle).getCrewCapacity() < ((Rover) secondVehicle).getCrewCapacity()) {
				result = -1;
			}

			// Vehicle with superior range should be ranked higher.
			if (result == 0) {
				if (firstVehicle.getRange(MissionType.TRAVEL_TO_SETTLEMENT) > secondVehicle.getRange(MissionType.TRAVEL_TO_SETTLEMENT)) {
					result = 1;
				} else if (firstVehicle.getRange(MissionType.TRAVEL_TO_SETTLEMENT) < secondVehicle.getRange(MissionType.TRAVEL_TO_SETTLEMENT)) {
					result = -1;
				}
			}
		}

		return result;
	}
}
