/**
 * Mars Simulation Project
 * MissionManager.java
 * @version 2.74 2002-03-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import java.util.Vector;
import java.lang.reflect.*;
import java.io.Serializable;

/** The MissionManager class keeps track of ongoing missions
 *  in the simulation. 
 *
 *  The simulation has only one mission manager. 
 */
public class MissionManager implements Serializable {

    // Data members
    private Vector missions; // Current missions in the simulation. 
    private Mars mars; // Virtual Mars 

    // Array of potential new missions
    Class[] potentialMissions = { TravelToSettlement.class, CollectRockSamplesMission.class };

    /** Constructs a MissioniManager object
     *  @param mars the virtual Mars
     */
    public MissionManager(Mars mars) {

        // Initialize data members
        this.mars = mars;
        missions = new Vector();
    }

    /** Returns the virtual Mars in the simulation
     *  @return virtual Mars
     */
    public Mars getMars() {
        return mars;
    }

    /** Returns the number of currently active missions.
     *  @return number of active missions
     */
    public int getNumActiveMissions() {

        // Remove inactive missions.
        cleanMissions();

        return missions.size();
    }

    /** Returns the mission a given person is a member of.
     *  If person is a member of no mission, return null.
     *  @param the given person
     *  @return mission for that person
     */
    public Mission getMission(Person person) {
        Mission result = null;
        for (int x=0; x < missions.size(); x++) {
            Mission tempMission = (Mission) missions.elementAt(x);
            if (tempMission.hasPerson(person)) result = tempMission;
        }
        return result;
    }

    /** Adds a new mission to the mission list.
     *  @param newMission new mission to be added
     */
    void addMission(Mission newMission) {
        if (!missions.contains(newMission)) {
            missions.addElement(newMission);
            // System.out.println("MissionManager: Added new mission - " + newMission.getName());
        }
    }

    /** Removes a mission from the mission list.
     *  @param the mission to be removed
     */
    void removeMission(Mission oldMission) {
        if (missions.contains(oldMission)) {
            missions.removeElement(oldMission);
            // System.out.println("MissionManager: Removed old mission - " + oldMission.getName());
        }
    } 

    /** Determines the total probability weight for available potential missions
     *  for a given person.
     *  @person the given person
     *  @return total probability weight
     */ 
    double getTotalMissionProbability(Person person) {
        double result = 0D;
     
        // Initialize parameters
        Class[] parametersForFindingMethod = { Person.class, Mars.class };
        Object[] parametersForInvokingMethod = { person, mars };

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

    /** Gets a new mission for a person based on potential missions available.
     *  @param person person to find the mission for
     *  @param totalProbabilityWeight total probability weight of all available missions 
     *  @return new mission
     */
    Mission getNewMission(Person person, double totalProbabilityWeight) {
        
        // Initialize parameters
        Class[] parametersForFindingMethod = { Person.class, Mars.class };
        Object[] parametersForInvokingMethod = { person, mars };

        // Get a random number from 0 to the total probability weight.
        double r = RandomUtil.getRandomDouble(totalProbabilityWeight);

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
                // System.out.println("MissionManager.getNewMission() (1): " + e.toString());
            }
        }

        // Initialize construction parameters
        Class[] parametersForFindingConstructor = { MissionManager.class, Person.class };
        Object[] parametersForInvokingConstructor = { this, person };

        // Construct the mission
        try {
            Constructor construct = (mission.getConstructor(parametersForFindingConstructor));
            return (Mission) construct.newInstance(parametersForInvokingConstructor);
        }
        catch (Exception e) {
            // System.out.println("MissionManager.getNewMission() (2): " + e.toString());
            return null;
        } 
    }

    /** Gets the total weighted probability of a given person joining a active mission
     *  in the simulation.
     *  @param person the given person
     *  @return the total weighted probability
     */
    double getTotalActiveMissionProbability(Person person) {
        double result = 0D;
       
        // Remove missions that are already completed.
	cleanMissions();
	
        for (int x=0; x < missions.size(); x++) {
            Mission tempMission = (Mission) missions.elementAt(x);
            result += tempMission.getJoiningProbability(person);
        }
    
        return result;
    }

    /** Gets an active mission for a person to join.
     *  @param person the given person
     *  @param totalProbabilityWeight the total probability weight for active missions.
     */
    Mission getActiveMission(Person person, double totalProbabilityWeight) {
        Mission result = null;

        // Remove missions that are already completed.
        cleanMissions();

        // Get a random number from 0 to the total probability weight.
        double r = RandomUtil.getRandomDouble(totalProbabilityWeight);

        for (int x=0; x < missions.size(); x++) {
            Mission tempMission = (Mission) missions.elementAt(x);            

            if (result == null) {
                double weight = tempMission.getJoiningProbability(person);
                if (r < weight) result = tempMission;
                else r -= weight;
            }
        }

        // if (result == null) System.out.println("MissionManager.getActiveMission(): Returned null");

        return result;
    }

    /** Remove missions that are already completed.
     */
    private void cleanMissions() {
      
        int index = 0;
        while (index < missions.size()) {
            Mission tempMission = (Mission) missions.elementAt(index);
            if ((tempMission == null) || tempMission.isDone()) removeMission(tempMission);
            else index++;
        }
    }
}
