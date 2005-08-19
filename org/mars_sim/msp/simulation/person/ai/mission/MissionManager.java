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
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
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

    // Array of potential new missions
    // Class[] potentialMissions = { TravelToSettlement.class, Exploration.class, CollectIce.class };
    Class[] potentialMissions = { Exploration.class, CollectIce.class };

    /** 
     * Constructor
     */
    public MissionManager() {
        // Initialize data members
        missions = new ArrayList();
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
    	
    	return missions;
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
    void removeMission(Mission oldMission) {
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
        double result = 0D;
     
        // Initialize parameters
        Class[] parametersForFindingMethod = { Person.class };
        Object[] parametersForInvokingMethod = { person };

        // Sum the probable weights for each available potential mission.
        for (int x=0; x < potentialMissions.length; x++) {
            try {
                Method probability = potentialMissions[x].getMethod("getNewMissionProbability", parametersForFindingMethod);
                result += ((Double) probability.invoke(null, parametersForInvokingMethod)).doubleValue();
            } 
            catch (Exception e) { 
                // System.out.println("MissionManager.getTotalMissionProbability(): " + e.toString());
            }
        }

        return result; 
    }

    /** 
     * Gets a new mission for a person based on potential missions available.
     * @param person person to find the mission for 
     * @return new mission
     */
    public Mission getNewMission(Person person) {
        
        // Initialize parameters
        Class[] parametersForFindingMethod = { Person.class };
        Object[] parametersForInvokingMethod = { person };

        // Get a random number from 0 to the total probability weight.
        double r = RandomUtil.getRandomDouble(getTotalMissionProbability(person));

        // Determine which mission is selected.
        Class mission = null;
        for (int x=0; x < potentialMissions.length; x++) {
            try {
                Method probability = potentialMissions[x].getMethod("getNewMissionProbability", parametersForFindingMethod);
                double weight = ((Double) probability.invoke(null, parametersForInvokingMethod)).doubleValue();
                
                if (mission == null) {
                    if (r < weight) mission = potentialMissions[x];
                    else r -= weight;
                }
            } 
            catch (Exception e) { 
                System.out.println("MissionManager.getNewMission() (1): " + e.toString());
            }
        }

        // Initialize construction parameters
        Class[] parametersForFindingConstructor = { Person.class };
        Object[] parametersForInvokingConstructor = { person };

        // Construct the mission
        try {
            Constructor construct = (mission.getConstructor(parametersForFindingConstructor));
            return (Mission) construct.newInstance(parametersForInvokingConstructor);
        }
        catch (Exception e) {
            System.out.println("MissionManager.getNewMission() (2): " + e.toString());
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
    	Iterator i = missions.iterator();
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
    	
    	Iterator i = missions.iterator();
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
}