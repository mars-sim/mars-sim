/**
 * Mars Simulation Project
 * MissionManager.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/** 
 * The MissionManager class keeps track of ongoing missions
 * in the simulation. 
 *
 * The simulation has only one mission manager. 
 */
public class MissionManager implements Serializable {
    
    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.mission.MissionManager";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);
 

    // Data members
    private List<Mission> missions; // Current missions in the simulation.
    private transient List<MissionManagerListener> listeners; // Mission listeners.
    
    // Cache variables.
    private Person personCache;
    private MarsClock timeCache;
    private Map<Class<? extends Mission>, Double> missionProbCache;
    private double totalProbCache;

    // Array of potential new missions
    private Class<? extends Mission>[] potentialMissions = null;
    
    /** 
     * Constructor
     */
    public MissionManager() {
        // Initialize data members
        missions = new ArrayList<Mission>();
        listeners = Collections.synchronizedList(new ArrayList<MissionManagerListener>());
        
        // Initialize potential missions.
        potentialMissions = (Class<? extends Mission>[]) new Class[11];
        potentialMissions[0] = TravelToSettlement.class;
        potentialMissions[1] = Exploration.class;
        potentialMissions[2] = CollectIce.class;
        potentialMissions[3] = RescueSalvageVehicle.class;
        potentialMissions[4] = Trade.class;
        potentialMissions[5] = CollectRegolith.class;
        potentialMissions[6] = Mining.class;
        potentialMissions[7] = BuildingConstructionMission.class;
        potentialMissions[8] = AreologyStudyFieldMission.class;
        potentialMissions[9] = BiologyStudyFieldMission.class;
        potentialMissions[10] = BuildingSalvageMission.class;
        
        // Initialize cache values.
        personCache = null;
        timeCache = null;
        missionProbCache = new HashMap<Class<? extends Mission>, Double>(potentialMissions.length);
        totalProbCache = 0D;
    }
    
    /**
     * Add a listener
     * @param newListener The listener to add.
     */
    public void addListener(MissionManagerListener newListener) {
    	if (listeners == null) listeners = 
    		Collections.synchronizedList(new ArrayList<MissionManagerListener>());
        if (!listeners.contains(newListener)) listeners.add(newListener);
    }
    
    /**
     * Remove a listener
     * @param oldListener the listener to remove.
     */
    public void removeListener(MissionManagerListener oldListener) {
    	if (listeners == null) listeners = 
    		Collections.synchronizedList(new ArrayList<MissionManagerListener>());
    	if (listeners.contains(oldListener)) listeners.remove(oldListener);
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
    public List<Mission> getMissions() {
    	// Remove inactive missions.
    	cleanMissions();
    	
    	return new ArrayList<Mission>(missions);
    }

    /** 
     * Gets the mission a given person is a member of.
     * If person is a member of no mission, return null.
     * @param person the given person
     * @return mission for that person
     */
    public Mission getMission(Person person) {
        Mission result = null;
        for (int x=0; x < missions.size(); x++) {
            Mission tempMission = missions.get(x);
            if (tempMission.hasPerson(person)) result = tempMission;
        }
        return result;
    }

    /** 
     * Adds a new mission to the mission list.
     * @param newMission new mission to be added
     */
    public void addMission(Mission newMission) {
    	if (newMission == null) throw new IllegalArgumentException("newMission is null");
        if (!missions.contains(newMission)) {
            missions.add(newMission);
            
            // Update listeners.
            if (listeners == null) listeners = 
            	Collections.synchronizedList(new ArrayList<MissionManagerListener>());
            synchronized(listeners) {
            	Iterator<MissionManagerListener> i = listeners.iterator();
            	while (i.hasNext()) i.next().addMission(newMission);
            }
            
            if(logger.isLoggable(Level.FINER)) {
             logger.finer("MissionManager: Added new mission - " + newMission.getName());
            }
        }
    }

    /** 
     * Removes a mission from the mission list.
     * @param the mission to be removed
     */
    private void removeMission(Mission oldMission) {
        if (missions.contains(oldMission)) {
            missions.remove(oldMission);
            
            // Update listeners.
            if (listeners == null) listeners = 
            	Collections.synchronizedList(new ArrayList<MissionManagerListener>());
            synchronized(listeners) {
            	Iterator i = listeners.iterator();
            	while (i.hasNext()) ((MissionManagerListener) i.next()).removeMission(oldMission);
            }
            
            if(logger.isLoggable(Level.FINER)) {
             logger.finer("MissionManager: Removed old mission - " + oldMission.getName());
            }
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
        Class<? extends Mission> selectedMission = null;
        Iterator<Class<? extends Mission>> i = missionProbCache.keySet().iterator();
        while (i.hasNext()) {
        	Class<? extends Mission> mission = i.next();
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
        if (selectedMission != null) {
        	try {
        		Constructor construct = selectedMission.getConstructor(parametersForFindingConstructor);
        		return (Mission) construct.newInstance(parametersForInvokingConstructor);
        	}
        	catch (Exception e) {
        		logger.log(Level.SEVERE, "MissionManager.getNewMission()", e);
        		return null;
        	}
        }
        else {
        	logger.log(Level.SEVERE, "MissionManager.getNewMission() - selected mission is null");
        	return null;
        }
    }
    
    /**
     * Gets all the active missions associated with a given settlement.
     * @param settlement the settlement to find missions.
     * @return list of missions associated with the settlement.
     */
    public List<Mission> getMissionsForSettlement(Settlement settlement) {
    	if (settlement == null) throw new IllegalArgumentException("settlement is null");
    	
    	List<Mission> settlementMissions = new ArrayList<Mission>();
    	Iterator<Mission> i = getMissions().iterator();
    	while (i.hasNext()) {
    		Mission mission = i.next();
    		if (!mission.isDone() && (settlement == mission.getAssociatedSettlement())) 
    			settlementMissions.add(mission);
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
    	
    	Iterator<Mission> i = getMissions().iterator();
    	while (i.hasNext()) {
    		Mission mission = i.next();
    		if (!mission.isDone()) {
                if (mission instanceof VehicleMission) {
                    if (((VehicleMission) mission).getVehicle() == vehicle) result = mission;
                    if (mission instanceof Mining) {
                        if (((Mining)mission).getLightUtilityVehicle() == vehicle) result = mission;
                    }
                    if (mission instanceof Trade) {
                        Rover towingRover = (Rover) ((Trade) mission).getVehicle();
                        if (towingRover != null) {
                            if (towingRover.getTowedVehicle() == vehicle) result = mission;
                        }
                    }
                }
                else if (mission instanceof BuildingConstructionMission) {
                    BuildingConstructionMission construction = (BuildingConstructionMission) mission;
                    if (construction.getConstructionVehicles().contains(vehicle)) result = mission;
                }
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
            Mission tempMission = missions.get(index);
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
            	Class<? extends Mission> probabilityClass = potentialMissions[x];
                Method probabilityMethod = probabilityClass.getMethod("getNewMissionProbability", parametersForFindingMethod);
                Double probability = (Double) probabilityMethod.invoke(null, parametersForInvokingMethod);
                missionProbCache.put(probabilityClass, probability);
    			totalProbCache += probability.doubleValue();
            } 
            catch (Exception e) { 
        	logger.log(Level.SEVERE, "MissionManager.getTotalMissionProbability()", e);
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
    
    /**
     * Updates mission based on passing time.
     * @param time amount of time passing (millisols)
     * @throws Exception if error in updating missions
     */
    public void timePassing(double time) throws Exception {
    	Iterator<Mission> i = missions.iterator();
    	while (i.hasNext()) i.next().timePassing(time);
    }
}