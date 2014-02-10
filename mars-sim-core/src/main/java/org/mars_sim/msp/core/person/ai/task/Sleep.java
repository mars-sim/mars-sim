/**
 * Mars Simulation Project
 * Sleep.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;

/** 
 * The Sleep class is a task for sleeping.
 * The duration of the task is by default chosen randomly, between 250 - 350 millisols.
 * Note: Sleeping reduces fatigue and stress.
 */
class Sleep extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

//	private static Logger logger = Logger.getLogger(Sleep.class.getName());

	/** Task phase. */
	private static final String SLEEPING = "Sleeping";

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.3D;
	/** The base alarm time (millisols) at 0 degrees longitude. */
	private static final double BASE_ALARM_TIME = 300D;

    // Data members
    /** The living accommodations if any. */
	private LivingAccommodations accommodations;
    /** The previous time (millisols). */
	private double previousTime;

    /** 
     * Constructor
     * @param person the person to perform the task
     * @throws Exception if error constructing task.
     */
    public Sleep(Person person) {
        super("Sleeping", person, false, false, STRESS_MODIFIER, true, (250D + RandomUtil.getRandomDouble(80D)));

        // If person is in a settlement, try to find a living accommodations building.
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {

            Building quarters = getAvailableLivingQuartersBuilding(person);
            if (quarters != null) {
                // Walk to quarters.
                walkToQuartersBuilding(quarters);
                accommodations = (LivingAccommodations) quarters.getFunction(LivingAccommodations.NAME);
                accommodations.addSleeper();
            }
        }
        
        previousTime = Simulation.instance().getMasterClock().getMarsClock().getMillisol();
        
        // Initialize phase
        addPhase(SLEEPING);
        setPhase(SLEEPING);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  Returns 25 if person's fatigue is over 750, more if fatigue is much higher.
     *  Returns an additional 50 if it is night time.
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

		// Fatigue modifier.
		double fatigue = person.getPhysicalCondition().getFatigue();
        if (fatigue > 500D) result = (fatigue - 500D) / 4D;
        
        // Dark outside modifier.
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
		if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) result *= 2D;
        
        // Crowding modifier.
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {

		    Building building = getAvailableLivingQuartersBuilding(person);
		    if (building != null) {
		        result *= Task.getCrowdingProbabilityModifier(person, building);
		        result *= Task.getRelationshipModifier(person, building);
		    }
		}
        
        // No sleeping outside.
        if (person.getLocationSituation().equals(Person.OUTSIDE)) {
            result = 0D;
        }

        return result;
    }
    
    /**
     * Walk to sleeping quarters building.
     * @param quartersBuilding the quarters building.
     */
    private void walkToQuartersBuilding(Building quartersBuilding) {
        
        // Determine location within sleeping quarters building.
        // TODO: Use action point rather than random internal location.
        Point2D.Double buildingLoc = LocalAreaUtil.getRandomInteriorLocation(quartersBuilding);
        Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(buildingLoc.getX(), 
                buildingLoc.getY(), quartersBuilding);
        
        // Check if there is a valid interior walking path between buildings.
        BuildingConnectorManager connectorManager = person.getSettlement().getBuildingConnectorManager();
        Building currentBuilding = BuildingManager.getBuilding(person);
        
        if (connectorManager.hasValidPath(currentBuilding, quartersBuilding)) {
            Task walkingTask = new WalkInterior(person, quartersBuilding, settlementLoc.getX(), 
                    settlementLoc.getY());
            addSubTask(walkingTask);
        }
        else {
            // TODO: Add task for EVA walking to get to sleeping quarters building.
            BuildingManager.addPersonToBuilding(person, quartersBuilding, settlementLoc.getX(), 
                    settlementLoc.getY());
        }
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (SLEEPING.equals(getPhase())) return sleepingPhase(time);
    	else return time;
    }

    /**
     * Performs the sleeping phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double sleepingPhase(double time) {
    	
		// Reduce person's fatigue
		double newFatigue = person.getPhysicalCondition().getFatigue() - (5D * time);
		if (newFatigue < 0D) newFatigue = 0D;
        person.getPhysicalCondition().setFatigue(newFatigue);
        
        // Check if alarm went off.
        double newTime = Simulation.instance().getMasterClock().getMarsClock().getMillisol();
        double alarmTime = getAlarmTime();
        if ((previousTime <= alarmTime) && (newTime >= alarmTime)) {
        	endTask();
        	// logger.info(person.getName() + " woke up from alarm.");
        }
        else previousTime = newTime;
        
        return 0D;
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// This task adds no experience.
	}
    
	/**
	 * Ends the task and performs any final actions.
	 */
	public void endTask() {
		super.endTask();
		
		// Remove person from living accommodations bed so others can use it.
		if (accommodations != null && accommodations.getSleepers() > 0) {
		    accommodations.removeSleeper();
		}

	}
    
	/**
	 * Gets an available living accommodations building that the person can use.
	 * Returns null if no living accommodations building is currently available.
	 *
	 * @param person the person
	 * @return available living accommodations building
	 * @throws BuildingException if error finding living accommodations building.
	 */
	private static Building getAvailableLivingQuartersBuilding(Person person) {
     
		Building result = null;
        
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List<Building> quartersBuildings = manager.getBuildings(LivingAccommodations.NAME);
			quartersBuildings = BuildingManager.getNonMalfunctioningBuildings(quartersBuildings);
			quartersBuildings = getQuartersWithEmptyBeds(quartersBuildings);
			quartersBuildings = BuildingManager.getLeastCrowdedBuildings(quartersBuildings);
			
			if (quartersBuildings.size() > 0) {
                Map<Building, Double> quartersBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                        person, quartersBuildings);
                result = RandomUtil.getWeightedRandomObject(quartersBuildingProbs);
            }
		}
        
		return result;
	}
	
	/**
	 * Gets living accommodations with empty beds from a list of buildings with the living accommodations function.
	 * @param buildingList list of buildings with the living accommodations function.
	 * @return list of buildings with empty beds.
	 * @throws BuildingException if any buildings in list don't have the living accommodations function.
	 */
	private static List<Building> getQuartersWithEmptyBeds(List<Building> buildingList) {
		List<Building> result = new ArrayList<Building>();
		
		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			LivingAccommodations quarters = (LivingAccommodations) building.getFunction(LivingAccommodations.NAME);
			if (quarters.getSleepers() < quarters.getBeds()) result.add(building);
		}
		
		return result;
	}
	
	/**
	 * Gets the wakeup alarm time for the person's longitude.
	 * @return alarm time in millisols.
	 */
	private double getAlarmTime() {
		double timeDiff = 1000D * (person.getCoordinates().getTheta() / (2D * Math.PI));
		double modifiedAlarmTime = BASE_ALARM_TIME - timeDiff;
		if (modifiedAlarmTime < 0D) modifiedAlarmTime += 1000D;
		return modifiedAlarmTime;
	}
	
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		return 0;	
	}
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(0);
		return results;
	}
	
	@Override
	public void destroy() {
	    super.destroy();
	    
	    accommodations = null;
	}
}