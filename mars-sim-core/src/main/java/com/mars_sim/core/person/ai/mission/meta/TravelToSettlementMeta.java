/**
 * Mars Simulation Project
 * TravelToSettlementMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.mission.AbstractMetaMission;
import com.mars_sim.core.mission.MissionCreationException;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.job.util.JobUtil;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.RoverMission;
import com.mars_sim.core.person.ai.mission.TravelToSettlement;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.core.vehicle.comparators.CrewRangeComparator;

/**
 * A meta mission for the TravelToSettlement mission.
 */
public class TravelToSettlementMeta extends AbstractMetaMission {
    
    private static final int EARLIEST_SOL_TRAVEL = 28;
    private static final Set<JobType> WORKER_JOBS = Collections.emptySet();
    private static final Set<JobType> LEADER_JOBS = Set.of(JobType.PILOT, JobType.POLITICIAN, JobType.REPORTER);

    private static final double BASE_MISSION_WEIGHT = 1D;
	private static final double RANGE_BUFFER = .8D;
	private static final double RELATIONSHIP_MODIFIER = 10D;
	private static final double JOB_MODIFIER = 1D;
	private static final double CROWDING_MODIFIER = 50D;
	private static final double SCIENCE_MODIFIER = 1D;

	public TravelToSettlementMeta() {
    	super(MissionType.TRAVEL_TO_SETTLEMENT, 8, LEADER_JOBS, WORKER_JOBS);

        setPreferredVehicle(Set.of(VehicleType.TRANSPORT_ROVER, VehicleType.EXPLORER_ROVER));
        setPopulationRatio(20);
        setPopulationThreshold(20);
        setSolThreshold(EARLIEST_SOL_TRAVEL);
    }
    
	/**
	 * Get the Vehicle comparator that is based on largest crew range
	 */
	@Override
	protected Comparator<Vehicle> getVehicleComparator() {
		return new CrewRangeComparator();
	}

    @Override
    public Mission constructInstance(Roster crew, boolean needsReview) throws MissionCreationException {
        // Find a random destination settlement for the mission.
        var destination = getRandomDestinationSettlement(crew);
        if (destination == null) {
            throw new MissionCreationException("mission.travelsettlement.dest");
        }
        return new TravelToSettlement(crew, destination, needsReview);
    }

    /**
	 * Determines a random destination settlement other than current one.
	 * 
	 * @param crew the crew of the mission
	 * @return randomly determined settlement
	 */
	private Settlement getRandomDestinationSettlement(Roster crew) {

		double range = crew.vehicle().getEstimatedRange();
		Settlement result = null;
        var startingSettlement = crew.leader().getSettlement();

		// Find all desirable destination settlements.
		Map<Settlement, Double> desirableSettlements = getDestinationSettlements(crew.leader(), startingSettlement, range);

		// Randomly select a desirable settlement.
		if (desirableSettlements.size() > 0) {
			result = RandomUtil.getWeightedRandomObject(desirableSettlements);
		}

		return result;
	}

	/**
	 * Get valid settlements to visit from a starting settlement.
	 * 
	 * @param startingSettlement the starting settlement.
	 */
	private static Collection<Settlement> getValidSettlements(Settlement startingSettlement) {

        // Get current travel destination for the starting settlement.
		var currentDestinations = startingSettlement.getMissionControl().getActiveMissions().stream()
                    .filter(TravelToSettlement.class::isInstance)
                    .map(m -> (TravelToSettlement) m)
                    .map(TravelToSettlement::getDestinationSettlement)
                    .collect(Collectors.toSet());
        
        var unitManager = Simulation.instance().getUnitManager();            
        Set<Settlement> potential = new HashSet<>(unitManager.getSettlements());
        potential.remove(startingSettlement);
        potential.removeAll(currentDestinations);
        
        return potential;
	}

	/**
	 * Gets all possible and desirable destination settlements.
	 * 
	 * @param member             the mission member searching for a settlement.
	 * @param startingSettlement the settlement the mission is starting at.
	 * @param range              the range (km) that can be travelled.
	 * @return map of destination settlements.
	 */
	private static Map<Settlement, Double> getDestinationSettlements(Worker member, Settlement startingSettlement,
			double range) {
		Map<Settlement, Double> result = new HashMap<>();

		// Find potential destination settlements within range and not current travel destination.
		for(var potentialDestination : getValidSettlements(startingSettlement)) {
			double distance = startingSettlement.getCoordinates().getDistance(potentialDestination.getCoordinates());
			if (distance <= (range * RANGE_BUFFER)) {

				double desirability = getDestinationSettlementDesirability(member, startingSettlement, potentialDestination);
				if (desirability > 0D)
					result.put(potentialDestination, desirability);
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

		if (member instanceof Person person) {
			double currentOpinion = RelationshipUtil.getAverageOpinionOfPeople(person,
					startingSettlement.getAllAssociatedPeople());
			double destinationOpinion = RelationshipUtil.getAverageOpinionOfPeople(person,
					destinationSettlement.getAllAssociatedPeople());
			relationshipFactor = (destinationOpinion - currentOpinion) / 100D;
		}

		// Determine job opportunities in destination settlement relative to
		// starting settlement.
		double jobFactor = 0D;
		if (member instanceof Person person) {
			JobType currentJob = person.getMind().getJobType();
			double currentJobProspect = JobUtil.getJobProspect(person, currentJob, startingSettlement, true);
			double destinationJobProspect;

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

		if (member instanceof Person person) {
			ScienceType jobScience = ScienceType.getJobScience(person.getMind().getJobType());
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
    public RatingScore getProbability(Person person) {

        RatingScore missionProbability = RatingScore.ZERO_RATING;
        if (person.isInSettlement()) {
            // Check if mission is possible for person based on their
            // circumstance.
            Settlement settlement = person.getSettlement();

            missionProbability = getMissionProbability(settlement, person);

    		if (missionProbability.getScore() <= 0) {
    			return RatingScore.ZERO_RATING;
    		}
    		
	        // Job modifier.
    		missionProbability.addModifier(LEADER, getLeaderSuitability(person));
            missionProbability = MetaTask.applyCommerceFactor(missionProbability, settlement, CommerceType.TOURISM);
			
			// if introvert, score  0 to  50 --> -2 to 0
			// if extrovert, score 50 to 100 -->  0 to 2
			// Reduce probability if introvert
			int extrovert = person.getExtrovertmodifier();
			missionProbability.addModifier(PERSON_EXTROVERT, (1 + extrovert/2.0));
			
			missionProbability.applyRange(0, LIMIT);
        }
		 
        return missionProbability;
    }

    private RatingScore getMissionProbability(Settlement settlement, Worker member) {
        
        // Check if there are any desirable settlements within range.
        double topSettlementDesirability = 0D;
        var vehicle = selectVehicle(settlement);
        if (vehicle == null) {
            return RatingScore.ZERO_RATING;
        }

        Map<Settlement, Double> desirableSettlements = getDestinationSettlements(
                member, settlement, vehicle.getEstimatedRange());

        if ((desirableSettlements == null) || desirableSettlements.isEmpty()) {
            return RatingScore.ZERO_RATING;
        }

        Iterator<Settlement> i = desirableSettlements.keySet().iterator();
        while (i.hasNext()) {
            Settlement desirableSettlement = i.next();
            double desirability = desirableSettlements.get(desirableSettlement);
            if (desirability > topSettlementDesirability) {
                topSettlementDesirability = desirability;
            }
        }

        // Determine mission probability.
        RatingScore missionProbability = new RatingScore(BASE_MISSION_WEIGHT
                                                + (topSettlementDesirability / 100D));
		
        // Crowding modifier.
        int crowding = settlement.getIndoorPeopleCount()
                - settlement.getPopulationCapacity();
        if (crowding > 0) {
            missionProbability.addModifier(OVER_CROWDING, (crowding + 1));
        }

        return missionProbability;
    }

    @Override
	public double getWorkerSuitability(Worker member) {
		double result = super.getWorkerSuitability(member);

		if (member instanceof Person person) {
			// If person has the "Driver" job, add 1 to their qualification.
			if (person.getMind().getJobType() == JobType.PILOT) {
				result += 1D;
			}

			if (person.getMind().getJobType() == JobType.POLITICIAN) {
				result += 10D;
			}
        }

		return result;
	}
}
