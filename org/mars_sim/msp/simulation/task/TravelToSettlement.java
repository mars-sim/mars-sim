/**
 * Mars Simulation Project
 * TravelToSettlement.java
 * @version 2.72 2001-07-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;

/** The TravelToSettlement class is a task to travel from one settlement to another randomly
 *  selected one within range of an available vehicle.  
 *
 *  May also be constructed with predetermined destination. 
 */
class TravelToSettlement extends Task {

    // Data members
    Settlement destination;

    /** Constructs a TravelToSettlement object with destination settlement
     *  randomly determined.
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public TravelToSettlement(Person person, VirtualMars mars) {
        super("Travel To Settlement", person, mars);

        destination = getRandomDestinationSettlement();
    }

    /** Constructs a TravelToSettlement object with given destination settlement.
     *  
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param destination the destination settlement
     */
    public TravelToSettlement(Person person, VirtualMars mars, Settlement destination) {
        super("Travel To " + destination.getName(), person, mars);

        this.destination = destination;
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, VirtualMars mars) {

        double result = 0D;

        if (person.getLocationSituation().equals("In Settlement")) {
            Settlement currentSettlement = person.getSettlement();
            int vehicleNum = currentSettlement.getVehicleNum();
            if (vehicleNum > 0) {
                for (int x=0; x < vehicleNum; x++) {
                    if (!currentSettlement.getVehicle(x).isReserved()) result = 5D;
                }
            }
        }

        return result;
    }

    /** Determines a random destination settlement other than current one.
     *  @return randomly determined settlement
     */
    private Settlement getRandomDestinationSettlement() {
        UnitManager unitManager = person.getUnitManager();
        Settlement embarkingSettlement = person.getSettlement();
        Settlement result = null;

        // Choose a random settlement other than current one.
        // 75% chance of selecting ove of the closest three settlements.
        if (RandomUtil.lessThanRandPercent(75)) 
            result = unitManager.getRandomOfThreeClosestSettlements(embarkingSettlement);
        else result = unitManager.getRandomSettlement(embarkingSettlement);
    
        return result;
    } 

    /** Performs this task for a given period of time 
     *  @param time amount of time to perform task (in millisols) 
     */
    double doTask(double time) {
        double timeLeft = super.doTask(time);
        if (subTask != null) return timeLeft;

        if (phase.equals("")) {
            phase = "Embarking";
            subTask = new EmbarkFromSettlement(person, mars, destination);
        }
        else if (phase.equals("Embarking")) {
            phase = "Driving";
            if (person.getVehicle() != null) {
                subTask = new DriveGroundVehicle(person, mars, (GroundVehicle) person.getVehicle(), destination.getCoordinates());
            }
            else isDone = true;
        }
        else if (phase.equals("Driving")) {
            phase = "Disembarking";
            subTask = new DisembarkAtSettlement(person, mars, destination);
        }
        else if (phase.equals("Disembarking")) {
            subTask = null;
            isDone = true;
        }

        return timeLeft; 
    }
}

