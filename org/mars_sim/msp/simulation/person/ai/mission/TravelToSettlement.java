/**
 * Mars Simulation Project
 * TravelToSettlement.java
 * @version 2.78 2005-08-18
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.UnitManager;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.job.*;
import org.mars_sim.msp.simulation.person.ai.social.RelationshipManager;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleIterator;

/** 
 * The TravelToSettlement class is a mission to travel from one settlement 
 * to another randomly selected one within range of an available rover.   
 */
public class TravelToSettlement extends RoverMission implements Serializable {
	
	// Static members
	private static final double BASE_MISSION_WEIGHT = 5D;
	private static final double RELATIONSHIP_MODIFIER = 10D;
	private static final double JOB_MODIFIER = 1D;
	private static final double CROWDING_MODIFIER = 5D;
	private static final double RANGE_BUFFER = .8D;
	
    // Data members
    private Settlement startingSettlement;
    private Settlement destinationSettlement;

    /** 
     * Constructs a TravelToSettlement object with destination settlement
     * randomly determined.
     * @param startingPerson the person starting the mission.
     * @throws MissionException if error constructing mission.
     */
    public TravelToSettlement(Person startingPerson) throws MissionException {
    	// Use RoverMission constructor
        super("Travel To Settlement", startingPerson);
   
        if (!isDone()) {
        	
        	// Initialize data members
        	startingSettlement = startingPerson.getSettlement();

        	// Set mission capacity.
        	if (hasVehicle()) setMissionCapacity(getRover().getCrewCapacity());
        	
        	// Choose destination settlement.
        	destinationSettlement = getRandomDestinationSettlement(startingPerson, startingSettlement);
        	if (destinationSettlement != null) 
        		addNavpoint(new NavPoint(destinationSettlement.getCoordinates(), destinationSettlement));
        	else endMission();
        	
        	// Recruit additional people to mission.
        	recruitPeopleForMission(startingPerson);
        }
        
        // Set initial phase
        setPhase(VehicleMission.EMBARKING);
        
        // System.out.println("Travel to Settlement mission");
    }

    /** 
     * Gets the weighted probability that a given person would start this mission.
     * @param person the given person
     * @return the weighted probability
     */
    public static double getNewMissionProbability(Person person) {

        // Check if mission is possible for person based on their circumstance.
    	boolean missionPossible = true;
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
	    
	    	// Check if available rover.
	    	if (!areVehiclesAvailable(settlement)) missionPossible = false;
            
			// At least one person left to hold down the fort.
	    	if (!atLeastOnePersonRemainingAtSettlement(settlement)) missionPossible = false;
        }
        
        // Determine mission probability.
        double missionProbability = 0D;
        if (missionPossible) {
        	missionProbability = BASE_MISSION_WEIGHT;
            
            // Crowding modifier.
        	if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
                Settlement settlement = person.getSettlement();
                int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
                if (crowding > 0) missionProbability *= (crowding + 1);
        	}
        	
    		// Job modifier.
        	Job job = person.getMind().getJob();
        	if (job != null) missionProbability *= job.getStartMissionProbabilityModifier(TravelToSettlement.class);	
        }

        return missionProbability;
    }
    
	/**
	 * Checks to see if any vehicles are available at a settlement.
	 * @param settlement the settlement to check.
	 * @return true if vehicles are available.
	 */
	private static boolean areVehiclesAvailable(Settlement settlement) {
		
		boolean result = false;
		
		VehicleIterator i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			
			boolean usable = true;
			if (vehicle.isReserved()) usable = false;
			if (!vehicle.getStatus().equals(Vehicle.PARKED)) usable = false;
			if (!(vehicle instanceof Rover)) usable = false;
			
			if (usable) result = true;    
		}
		
		return result;
	}

    /** 
     * Determines a random destination settlement other than current one.
     * @param person the person searching for a settlement.
     * @param startingSettlement the settlement the mission is starting at.
     * @return randomly determined settlement
     */
    private Settlement getRandomDestinationSettlement(Person person, Settlement startingSettlement) {
    	double range = getVehicle().getRange();
        UnitManager unitManager = startingSettlement.getUnitManager();
        Settlement result = null;

        SettlementCollection settlements = new SettlementCollection(unitManager.getSettlements());
        
        // Find all desirable destination settlements.
        Map desirableSettlements = new HashMap();
        SettlementIterator i = new SettlementCollection(unitManager.getSettlements()).iterator();
        while (i.hasNext()) {
        	Settlement settlement = i.next();
        	double distance = startingSettlement.getCoordinates().getDistance(settlement.getCoordinates());
        	if ((startingSettlement != settlement) && (distance <= (range * RANGE_BUFFER))) {
        		double desirability = getDestinationSettlementDesirability(person, startingSettlement, settlement);
        		if (desirability > 0D) desirableSettlements.put(settlement, new Double(desirability));
        	}
        }
        
        // Randomly select a desirable settlement.
        if (desirableSettlements.size() > 0) result = (Settlement) RandomUtil.getWeightedRandomObject(desirableSettlements);
    
        return result;
    }
	
	/**
	 * Gets the desirability of the destination settlement.
	 * @param person the person looking at the settlement.
	 * @param startingSettlement the settlement the person is already at.
	 * @param destinationSettlement the new settlement.
	 * @return negative or positive desirability weight value.
	 */
	private static double getDestinationSettlementDesirability(Person person, Settlement startingSettlement, 
			Settlement destinationSettlement) {
			
		// Determine relationship factor in destination settlement relative to starting settlement.
		RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		double currentOpinion = relationshipManager.getAverageOpinionOfPeople(person, 
				startingSettlement.getAllAssociatedPeople());
		double destinationOpinion = relationshipManager.getAverageOpinionOfPeople(person, 
				destinationSettlement.getAllAssociatedPeople());
		double relationshipFactor = (destinationOpinion - currentOpinion) / 100D;
			
		// Determine job opportunities in destination settlement relative to starting settlement.
		JobManager jobManager = Simulation.instance().getJobManager();
		Job currentJob = person.getMind().getJob();
		double currentJobProspect = jobManager.getJobProspect(person, currentJob, startingSettlement, true);
		double destinationJobProspect = 0D;
		if (person.getMind().getJobLock()) 
			destinationJobProspect = jobManager.getJobProspect(person, currentJob, destinationSettlement, false);
		else destinationJobProspect = jobManager.getBestJobProspect(person, destinationSettlement, false);
		boolean betterJobProspect = (destinationJobProspect > currentJobProspect);
		double jobFactor = 0D;
		if (destinationJobProspect > currentJobProspect) jobFactor = 1D;
		else if (destinationJobProspect < currentJobProspect) jobFactor = -1D;
			
		// Determine available space in destination settlement relative to starting settlement.
		int startingCrowding = startingSettlement.getPopulationCapacity() - 
				startingSettlement.getAllAssociatedPeople().size() - 1;
		int destinationCrowding = destinationSettlement.getPopulationCapacity() - 
				destinationSettlement.getAllAssociatedPeople().size();
		double crowdingFactor = destinationCrowding - startingCrowding;
		
		// Return the sum of the factors with modifiers.
		return (relationshipFactor * RELATIONSHIP_MODIFIER) + (jobFactor * JOB_MODIFIER) + (crowdingFactor * CROWDING_MODIFIER);
	}
	
	/**
	 * Checks to see if a person is capable of joining a mission.
	 * @param person the person to check.
	 * @return true if person could join mission.
	 */
	protected boolean isCapableOfMission(Person person) {
		if (super.isCapableOfMission(person)) {
			if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
				if (person.getSettlement() == startingSettlement) return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the mission qualification value for the person.
	 * Person is qualified and interested in joining the mission if the value is larger than 0.
	 * The larger the qualification value, the more likely the person will be picked for the mission.
	 * Qualification values of zero or negative will not join missions.
	 * @param person the person to check.
	 * @return mission qualification value.
	 * @throws MissionException if problem finding mission qualification.
	 */
	protected double getMissionQualification(Person person) throws MissionException {
		double result = 0D;
		
		if (isCapableOfMission(person)) {
			result = super.getMissionQualification(person);
			RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
			
			// Add modifier for average relationship with inhabitants of destination settlement.
			if (destinationSettlement != null) {
				PersonCollection destinationInhabitants = destinationSettlement.getAllAssociatedPeople();
				double destinationSocialModifier = (relationshipManager.getAverageOpinionOfPeople(person, 
						destinationInhabitants) - 50D) / 50D;
				result += destinationSocialModifier;
			}
			
			// Subtract modifier for average relationship with non-mission inhabitants of starting settlement.
			if (startingSettlement != null) {
				PersonCollection startingInhabitants = startingSettlement.getAllAssociatedPeople();
				PersonIterator i = startingInhabitants.iterator();
				while (i.hasNext()) {
					if (hasPerson(i.next())) i.remove();
				}
				double startingSocialModifier = (relationshipManager.getAverageOpinionOfPeople(person, 
						startingInhabitants) - 50D) / 50D;
				result -= startingSocialModifier;
			}
			
			// If person has the "Driver" job, add 1 to their qualification.
			if (person.getMind().getJob() instanceof Driver) result += 1D;
		}
		
		return result;
	}
	
	/**
	 * Recruits new people into the mission.
	 * @param startingPerson the person starting the mission.
	 */
	protected void recruitPeopleForMission(Person startingPerson) {
		super.recruitPeopleForMission(startingPerson);
		
		// Make sure there is at least one person left at the starting settlement.
		if (!atLeastOnePersonRemainingAtSettlement(startingSettlement)) {
			// Remove last person added to the mission.
			Person lastPerson = (Person) getPeople().get(getPeopleNumber() - 1);
			if (lastPerson != null) {
				getPeople().remove(lastPerson);
				if (getPeopleNumber() < getMinPeople()) endMission();
			}
		}
	}
	
	/**
	 * Gets the settlement associated with the mission.
	 * @return settlement or null if none.
	 */
	public Settlement getAssociatedSettlement() {
		return destinationSettlement;
	}
}