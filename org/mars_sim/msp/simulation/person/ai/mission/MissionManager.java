/**
 * Mars Simulation Project
 * MissionManager.java
 * @version 2.78 2005-08-09
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/** 
 * The MissionManager class keeps track of ongoing missions
 * in the simulation. 
 *
 * The simulation has only one mission manager. 
 */
public class MissionManager implements Serializable {

    // Data members
    private List missions; // Current missions in the simulation. 
    
    // Cache variables.
    private Person personCache;
    private MarsClock timeCache;
    private Map missionProbCache;
    private double totalProbCache;

    // Array of potential new missions
    Class[] potentialMissions = { TravelToSettlement.class, Exploration.class, CollectIce.class };
    
    /** 
     * Constructor
     */
    public MissionManager() {
        // Initialize data members
        missions = new ArrayList();
        
        // Initialize cache values.
        personCache = null;
        timeCache = null;
        missionProbCache = new HashMap(potentialMissions.length);
        totalProbCache = 0D;
    }

    /** 
     * Gets the number of currently active missions.
     * @return number of active missions
     */
    public int getNumActiveMissions() {
        // Remove inactive missions.
        cleanMissions();

        return missions.size();
    }
    
    /**
     * Gets a list of current missions.
     * @return list of missions.
     */
    public List getMissions() {
    	// Remove inactive missions.
    	cleanMissions();
    	
    	return new ArrayList(missions);
    }

    /** 
     * Gets the mission a given person is a member of.
     * If person is a member of no mission, return null.
     * @param the given person
     * @return mission for that person
     */
    public Mission getMission(Person person) {
        Mission result = null;
        for (int x=0; x < missions.size(); x++) {
            Mission tempMission = (Mission) missions.get(x);
            if (tempMission.hasPerson(person)) result = tempMission;
        }
        return result;
    }

    /** 
     * Adds a new mission to the mission list.
     * @param newMission new mission to be added
     */
    public void addMission(Mission newMission) {
        if (!missions.contains(newMission)) {
            missions.add(newMission);
            // System.out.println("MissionManager: Added new mission - " + newMission.getName());
        }
    }

    /** 
     * Removes a mission from the mission list.
     * @param the mission to be removed
     */
    private void removeMission(Mission oldMission) {
        if (missions.contains(oldMission)) {
            missions.remove(oldMission);
            // System.out.println("MissionManager: Removed old mission - " + oldMission.getName());
        }
    } 

    /** 
     * Determines the total probability weight for available potential missions
     * for a given person.
     * @param person the given person
     * @return total probability weight
     */ 
    public double getTotalMissionProbability(Person person) {
    	
    	// If cache is not current, calculate the probabilities.
        if (!useCache(person)) calculateProbability(person);
        
        return totalProbCache;
    }

    /** 
     * Gets a new mission for a person based on potential missions available.
     * @param person person to find the mission for 
     * @return new mission
     */
    public Mission getNewMission(Person person) {
        
    	// If cache is not current, calculate the probabilities.
        if (!useCache(person)) calculateProbability(person);

        // Get a random number from 0 to the total probability weight.
        double r = RandomUtil.getRandomDouble(getTotalMissionProbability(person));

        // Determine which mission is selected.
        Class selectedMission = null;
        Iterator i = missionProbCache.keySet().iterator();
        while (i.hasNext()) {
        	Class mission = (Class) i.next();
        	double probWeight = ((Double) missionProbCache.get(mission)).doubleValue();
        	if (selectedMission == null) {
        		if (r < probWeight) selectedMission = mission;
        		else r -= probWeight;
        	}
        }

        // Initialize construction parameters
        Class[] parametersForFindingConstructor = { Person.class };
        Object[] parametersForInvokingConstructor = { person };

        // Construct the mission
        try {
            Constructor construct = (selectedMission.getConstructor(parametersForFindingConstructor));
            return (Mission) construct.newInstance(parametersForInvokingConstructor);
        }
        catch (Exception e) {
            System.err.println("MissionManager.getNewMission(): " + e.toString());
            e.printStackTrace(System.err);
            return null;
        } 
    }
    
    /**
     * Gets all the active missions associated with a given settlement.
     * @param settlement the settlement to find missions.
     * @return list of missions associated with the settlement.
     */
    public List getMissionsForSettlement(Settlement settlement) {
    	if (settlement == null) throw new IllegalArgumentException("settlement is null");
    	
    	List settlementMissions = new ArrayList();
    	Iterator i = getMissions().iterator();
    	while (i.hasNext()) {
    		Mission mission = (Mission) i.next();
    		if (!mission.isDone() && (settlement == mission.getAssociatedSettlement())) settlementMissions.add(mission);
    	}
    	
    	return settlementMissions;
    }
    
    /**
     * Gets a mission that the given vehicle is a part of.
     * @param vehicle the vehicle to check for.
     * @return mission or null if none.
     */
    public Mission getMissionForVehicle(Vehicle vehicle) {
    	if (vehicle == null) throw new IllegalArgumentException("vehicle is null");
    	
    	Mission result = null;
    	
    	Iterator i = getMissions().iterator();
    	while (i.hasNext()) {
    		Mission mission = (Mission) i.next();
    		if (!mission.isDone() && (mission instanceof VehicleMission)) {
    			if (((VehicleMission) mission).getVehicle() == vehicle) result = mission;
    		}
    	}
    	
    	return result;
    }

    /** 
     * Remove missions that are already completed.
     */
    private void cleanMissions() {
      
        int index = 0;
        while (index < missions.size()) {
            Mission tempMission = (Mission) missions.get(index);
            if ((tempMission == null) || tempMission.isDone()) removeMission(tempMission);
            else index++;
        }
    }
    
    /**
     * Calculates and caches the probabilities.
     * @param person the person to check for.
     */
    private void calculateProbability(Person person) {
    	// Initialize parameters.
    	Class[] parametersForFindingMethod = { Person.class };
        Object[] parametersForInvokingMethod = { person };
    	
    	// Clear total probabilities.
    	totalProbCache = 0D;
    	
    	// Determine probabilities.
        for (int x=0; x < potentialMissions.length; x++) {
            try {
            	Class probabilityClass = potentialMissions[x];
                Method probabilityMethod = probabilityClass.getMethod("getNewMissionProbability", parametersForFindingMethod);
                Double probability = (Double) probabilityMethod.invoke(null, parametersForInvokingMethod);
                missionProbCache.put(probabilityClass, probability);
    			totalProbCache += probability.doubleValue();
            } 
            catch (Exception e) { 
                System.err.println("MissionManager.getTotalMissionProbability(): " + e.toString());
            }
        }
    	
    	// Set the time cache to the current time.
    	timeCache = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
    	personCache = person;
    }
    
    /**
     * Checks if task probability cache should be used.
     * @param the person to check for.
     * @return true if cache should be used.
     */
    private boolean useCache(Person person) {
    	MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
    	if (currentTime.equals(timeCache) && (person == personCache)) return true;
    	return false;
    }
}