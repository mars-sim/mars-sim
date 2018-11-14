/**
 * Mars Simulation Project
 * TravelToSettlement.java
 * @version 3.1.0 2017-08-08
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Driver;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.ai.job.Politician;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.robot.Robot;
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
public class TravelToSettlement extends RoverMission implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(TravelToSettlement.class.getName());

	/** Default description. */
	public static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.travelToSettlement"); //$NON-NLS-1$

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
	public TravelToSettlement(MissionMember startingMember) {
		// Use RoverMission constructor
		super(DEFAULT_DESCRIPTION, startingMember);

		Settlement s = startingMember.getSettlement();

		if (!isDone() && s != null) {

			// Initialize data members
			setStartingSettlement(s);

			// Set mission capacity.
			if (hasVehicle()) {
				setMissionCapacity(getRover().getCrewCapacity());
			}
			int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(startingMember.getSettlement());
			if (availableSuitNum < getMissionCapacity()) {
				setMissionCapacity(availableSuitNum);
			}

			// Choose destination settlement.
			setDestinationSettlement(getRandomDestinationSettlement(startingMember, getStartingSettlement()));
			if (destinationSettlement != null) {
				addNavpoint(new NavPoint(destinationSettlement.getCoordinates(), destinationSettlement,
						destinationSettlement.getName()));
				setDescription(Msg.getString("Mission.description.travelToSettlement.detail",
						destinationSettlement.getName())); // $NON-NLS-1$)
			} else {
				endMission("Destination is null.");
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
				recruitMembersForMission(startingMember);
			}

			// Check if vehicle can carry enough supplies for the mission.
			if (hasVehicle() && !isVehicleLoadable()) {
				endMission(VEHICLE_NOT_LOADABLE);// "Vehicle is not loadable. (TravelToSettlement)");
			}

			// Set initial phase
			setPhase(VehicleMission.APPROVAL);//.EMBARKING);
			setPhaseDescription(
					Msg.getString("Mission.phase.approval.description", getStartingSettlement().getName())); // $NON-NLS-1$
		}
		// logger.info("Travel to Settlement mission");
	}

	public TravelToSettlement(Collection<MissionMember> members, Settlement startingSettlement,
			Settlement destinationSettlement, Rover rover, String description) {
		// Use RoverMission constructor.
		super(description, (MissionMember) members.toArray()[0], RoverMission.MIN_GOING_MEMBERS, rover);

		// Initialize data members
		setStartingSettlement(startingSettlement);

		// Sets the mission capacity.
		setMissionCapacity(getRover().getCrewCapacity());
		int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(startingSettlement);
		if (availableSuitNum < getMissionCapacity())
			setMissionCapacity(availableSuitNum);

		// Set mission destination.
		setDestinationSettlement(destinationSettlement);
		addNavpoint(new NavPoint(this.destinationSettlement.getCoordinates(), this.destinationSettlement,
				this.destinationSettlement.getName()));

		// Add mission members.
		Iterator<MissionMember> i = members.iterator();
		while (i.hasNext()) {
			MissionMember member = i.next();
			// TODO Refactor.
			if (member instanceof Person) {
				Person person = (Person) member;
				person.getMind().setMission(this);
			} else if (member instanceof Robot) {
				Robot robot = (Robot) member;
				robot.getBotMind().setMission(this);
			}
		}

		// Set initial phase
		setPhase(VehicleMission.APPROVAL);//.EMBARKING);
		setPhaseDescription(Msg.getString("Mission.phase.approval.description", getStartingSettlement().getName())); // $NON-NLS-1$

		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			endMission(VEHICLE_NOT_LOADABLE);// "Vehicle is not loadable. (TravelToSettlement)");
		}
	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 * 
	 * @throws MissionException if problem setting a new phase.
	 */
	@Override
	protected void determineNewPhase() {
		if (APPROVAL.equals(getPhase())) {
			setPhase(VehicleMission.EMBARKING);
			setPhaseDescription(
					Msg.getString("Mission.phase.embarking.description", getCurrentNavpoint().getDescription()));//startingMember.getSettlement().toString())); // $NON-NLS-1$
		}
		
		else if (EMBARKING.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription(
					Msg.getString("Mission.phase.travelling.description", getNextNavpoint().getDescription())); // $NON-NLS-1$
			associateAllMembersWithSettlement(destinationSettlement);
		} 
		
		else if (TRAVELLING.equals(getPhase())) {
			if (getCurrentNavpoint().isSettlementAtNavpoint()) {
				setPhase(VehicleMission.DISEMBARKING);
				setPhaseDescription(
						Msg.getString("Mission.phase.disembarking.description", getCurrentNavpoint().getDescription())); // $NON-NLS-1$
			}
		} 
		
		else if (DISEMBARKING.equals(getPhase()))
			endMission(ALL_DISEMBARKED);
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
	private Settlement getRandomDestinationSettlement(MissionMember member, Settlement startingSettlement) {

		double range = getVehicle().getRange();
		Settlement result = null;

		// Find all desirable destination settlements.
		Map<Settlement, Double> desirableSettlements = getDestinationSettlements(member, startingSettlement, range);

		// Randomly select a desirable settlement.
		if (desirableSettlements.size() > 0) {
			result = RandomUtil.getWeightedRandomObject(desirableSettlements);
		}

		return result;
	}
//    private Settlement getRandomDestinationSettlement(Robot robot,
//            Settlement startingSettlement) {
//
//        double range = getVehicle().getRange();
//        Settlement result = null;
//
//        // Find all desirable destination settlements.
//        Map<Settlement, Double> desirableSettlements = getDestinationSettlements(
//                robot, startingSettlement, range);
//
//        // Randomly select a desirable settlement.
//        if (desirableSettlements.size() > 0) {
//            result = RandomUtil.getWeightedRandomObject(desirableSettlements);
//        }
//
//        return result;
//    }

	/**
	 * Gets all possible and desirable destination settlements.
	 * 
	 * @param member             the mission member searching for a settlement.
	 * @param startingSettlement the settlement the mission is starting at.
	 * @param range              the range (km) that can be travelled.
	 * @return map of destination settlements.
	 */
	public static Map<Settlement, Double> getDestinationSettlements(MissionMember member, Settlement startingSettlement,
			double range) {
		Map<Settlement, Double> result = new HashMap<Settlement, Double>();

		UnitManager unitManager = startingSettlement.getUnitManager();
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
//    public static Map<Settlement, Double> getDestinationSettlements(
//            Robot robot, Settlement startingSettlement, double range) {
//        Map<Settlement, Double> result = new HashMap<Settlement, Double>();
//
//        UnitManager unitManager = startingSettlement.getUnitManager();
//        Iterator<Settlement> i = unitManager.getSettlements().iterator();
//
//        while (i.hasNext()) {
//            Settlement settlement = i.next();
//            double distance = startingSettlement.getCoordinates().getDistance(
//                    settlement.getCoordinates());
//            boolean isTravelDestination = isCurrentTravelDestination(settlement);
//            if ((startingSettlement != settlement) && (distance <= (range * RANGE_BUFFER))
//                    && !isTravelDestination) {
//
//                double desirability = getDestinationSettlementDesirability(robot,
//                        startingSettlement, settlement);
//                if (desirability > 0D)
//                    result.put(settlement, desirability);
//            }
//        }
//
//        return result;
//    }

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
	private static double getDestinationSettlementDesirability(MissionMember member, Settlement startingSettlement,
			Settlement destinationSettlement) {

		// Determine relationship factor in destination settlement relative to
		// starting settlement.
		double relationshipFactor = 0D;
		RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		if (member instanceof Person) {
			Person person = (Person) member;
			double currentOpinion = relationshipManager.getAverageOpinionOfPeople(person,
					startingSettlement.getAllAssociatedPeople());
			double destinationOpinion = relationshipManager.getAverageOpinionOfPeople(person,
					destinationSettlement.getAllAssociatedPeople());
			relationshipFactor = (destinationOpinion - currentOpinion) / 100D;
		}

		// Determine job opportunities in destination settlement relative to
		// starting settlement.
		double jobFactor = 0D;
		if (member instanceof Person) {
			Person person = (Person) member;
			Job currentJob = person.getMind().getJob();
			double currentJobProspect = JobManager.getJobProspect(person, currentJob, startingSettlement, true);
			double destinationJobProspect = 0D;

			if (person.getMind().getJobLock()) {
				destinationJobProspect = JobManager.getJobProspect(person, currentJob, destinationSettlement, false);
			} else {
				destinationJobProspect = JobManager.getBestJobProspect(person, destinationSettlement, false);
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
		double crowdingFactor = destinationCrowding - startingCrowding;

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

//    private static double getDestinationSettlementDesirability(Robot robot,
//            Settlement startingSettlement, Settlement destinationSettlement) {
//
//        // Determine relationship factor in destination settlement relative to
//        // starting settlement.
///*        RelationshipManager relationshipManager = Simulation.instance()
//                .getRelationshipManager();
//        double currentOpinion = relationshipManager.getAverageOpinionOfPeople(
//                person, startingSettlement.getAllAssociatedPeople());
//        double destinationOpinion = relationshipManager
//                .getAverageOpinionOfPeople(person, destinationSettlement
//                        .getAllAssociatedPeople());
//        double relationshipFactor = (destinationOpinion - currentOpinion) / 100D;
//*/
//        // Determine job opportunities in destination settlement relative to
//        // starting settlement.
//        RobotJob currentJob = robot.getBotMind().getRobotJob();
//        double currentJobProspect = JobManager.getRobotJobProspect(robot,
//                currentJob, startingSettlement, true);
//        double destinationJobProspect = 0D;
//        if (robot.getBotMind().getJobLock())
//            destinationJobProspect = JobManager.getRobotJobProspect(robot,
//                    currentJob, destinationSettlement, false);
//        else
//            destinationJobProspect = JobManager.getBestRobotJobProspect(robot,
//                    destinationSettlement, false);
//        double jobFactor = 0D;
//        if (destinationJobProspect > currentJobProspect)
//            jobFactor = 1D;
//        else if (destinationJobProspect < currentJobProspect)
//            jobFactor = -1D;
//
//        // Determine available space in destination settlement relative to
//        // starting settlement.
//        int startingCrowding = startingSettlement.getRobotCapacity()
//                - startingSettlement.getAllAssociatedRobots().size() - 1;
//        int destinationCrowding = destinationSettlement.getRobotCapacity()
//                - destinationSettlement.getAllAssociatedRobots().size();
//        double crowdingFactor = destinationCrowding - startingCrowding;
//
//        // Determine science achievement factor for destination relative to starting settlement.
///*        double totalScienceAchievementFactor = (destinationSettlement
//                .getTotalScientificAchievement() - startingSettlement
//                .getTotalScientificAchievement()) / 10D;
//        double jobScienceAchievementFactor = 0D;
//        ScienceType jobScience = ScienceType.getJobScience(robot.getBotMind().getJob());
//        if (jobScience != null) {
//            double startingJobScienceAchievement = startingSettlement.getScientificAchievement(jobScience);
//            double destinationJobScienceAchievement = destinationSettlement.getScientificAchievement(jobScience);
//            jobScienceAchievementFactor = destinationJobScienceAchievement - startingJobScienceAchievement;
//        }
//        double scienceAchievementFactor = totalScienceAchievementFactor + jobScienceAchievementFactor;
//*/
//
//        if (destinationCrowding < RoverMission.MIN_PEOPLE) {
//            return 0;
//        }
//
//        // Return the sum of the factors with modifiers.
//        return //(relationshipFactor * RELATIONSHIP_MODIFIER)+
//                (jobFactor * JOB_MODIFIER)
//                + (crowdingFactor * CROWDING_MODIFIER);
//                // (scienceAchievementFactor * SCIENCE_MODIFIER)
//    }

	@Override
	protected boolean isCapableOfMission(MissionMember member) {
		if (super.isCapableOfMission(member)) {
			if (member.isInSettlement()) {
				if (member.getSettlement() == getStartingSettlement()) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public double getMissionQualification(MissionMember member) {
		double result = 0D;

//        if (isCapableOfMission(member)) {
		result = super.getMissionQualification(member);

//		if (member instanceof Person) {
			Person person = (Person) member;

			RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();

			// Add modifier for average relationship with inhabitants of
			// destination settlement.
			if (destinationSettlement != null) {
				Collection<Person> destinationInhabitants = destinationSettlement.getAllAssociatedPeople();
				double destinationSocialModifier = (relationshipManager.getAverageOpinionOfPeople(person,
						destinationInhabitants) - 50D) / 50D;
				result += destinationSocialModifier;
			}

			// Subtract modifier for average relationship with non-mission
			// inhabitants of starting settlement.
			if (getStartingSettlement() != null) {
				Collection<Person> startingInhabitants = getStartingSettlement().getAllAssociatedPeople();
				Iterator<Person> i = startingInhabitants.iterator();
				while (i.hasNext()) {
					if (hasMember(i.next())) {
						i.remove();
					}
				}
				double startingSocialModifier = (relationshipManager.getAverageOpinionOfPeople(person,
						startingInhabitants) - 50D) / 50D;
				result -= startingSocialModifier;
			}

			// If person has the "Driver" job, add 1 to their qualification.
			if (person.getMind().getJob() instanceof Driver) {
				result += 1D;
			}

			if (person.getMind().getJob() instanceof Politician) {
				result += 10D;
			}
//		} else if (member instanceof Robot) {
//			Robot robot = (Robot) member;
//
//			// If robot has the "Driver" job, add 1 to their qualification.
//			if (robot.getBotMind().getRobotJob() instanceof Deliverybot) {
//				result += 1D;
//			}
//		}
//        }

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
	 * Gets the number and types of equipment needed for the mission.
	 * 
	 * @param useBuffer use time buffer in estimation if true.
	 * @return map of equipment class and Integer number.
	 * @throws MissionException if error determining needed equipment.
	 */
	public Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		if (equipmentNeededCache != null)
			return equipmentNeededCache;
		else {
			Map<Integer, Integer> result = new HashMap<>();
			equipmentNeededCache = result;
			return result;
		}
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
				if (firstVehicle.getRange() > secondVehicle.getRange()) {
					result = 1;
				} else if (firstVehicle.getRange() < secondVehicle.getRange()) {
					result = -1;
				}
			}
		}

		return result;
	}

	@Override
	public void destroy() {
		super.destroy();
		destinationSettlement = null;
	}
}