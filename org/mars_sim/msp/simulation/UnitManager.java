/**
 * Mars Simulation Project
 * UnitManager.java
 * @version 2.73 2001-09-23
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.*;
import java.util.*;

/** The UnitManager class contains and manages all units in virtual
 *  Mars. It has methods for getting information about units. It is
 *  also responsible for creating all units on its construction.
 *  There should be only one instance of this class and it should be
 *  constructed and owned by the virtual Mars object.
 */
public class UnitManager {

    // Data members
    private VirtualMars mars; // Virtual Mars
    // should use collections here rather than Vector... (gregwhelan)
    private Vector unitVector; // Master list of all units
    private Vector settlementsVector; // List of settlement units
    private Vector vehiclesVector; // List of vehicle units
    private Vector peopleVector; // List of people units

    /** Constructs a UnitManager object
     *  @param mars the virtual Mars
     */
    UnitManager(VirtualMars mars) {
        // Initialize virtual mars to parameter
        this.mars = mars;

        // Initialize all unit vectors
        unitVector = new Vector();
        settlementsVector = new Vector();
        vehiclesVector = new Vector();
        peopleVector = new Vector();

        createEntities();
    }

    /** Create the units */
    private void createEntities() {
        createSettlements();
        createVehicles();
        createPeople();
    }

    /** Creates initial settlements with random locations */
    private void createSettlements() {
        // Get settlement names from "settlements.conf"
        String[] settlementNames = ConfFileProcessor.getSettlementNames();

        // Set base random value
        Random baseRand = new Random();

        // Create a settlement for each initial settlement name
        for (int x = 0; x < settlementNames.length; x++) {

            // Determine random location of settlement, adjust so it will be less likely to be near the poles
            double settlementPhi = (baseRand.nextGaussian() * (Math.PI / 7D)) + (Math.PI / 2D);
            if (settlementPhi > Math.PI) settlementPhi = Math.PI;
            if (settlementPhi < 0D) settlementPhi = 0D;
            double settlementTheta = (double)(Math.random() * (2D * Math.PI));
            Coordinates settlementCoords = new Coordinates(settlementPhi, settlementTheta);
            createSettlement(settlementNames[x], settlementCoords);
        }
    }

    /** Creates a settlement
     *  @param name the settlement's name
     *  @param coords the settlement's location
     */
    private void createSettlement(String name, Coordinates coords) {
        Settlement tempSettlement = new Settlement(name, coords, mars, this);
        // Add settlement to master unit and settlements vectors
        unitVector.addElement(tempSettlement);
        settlementsVector.addElement(tempSettlement);
    }


    /** Creates initial vehicles at random settlements */
    private void createVehicles() {

        // Get rover names from "rovers.conf"
        String[] roverNames = ConfFileProcessor.getRoverNames();

        for (int x = 0; x < roverNames.length; x++) {

            // Choose a settlement for rover.
            Vector minRovers = new Vector();
            int min = Integer.MAX_VALUE;
            for (int y=0; y < settlementsVector.size(); y++) {
                Settlement settlement = (Settlement) settlementsVector.elementAt(y);
                int roverNum = settlement.getVehicleNum();
                if (roverNum == min) minRovers.addElement(settlement);
                else if (roverNum < min) {
                    minRovers.removeAllElements();
                    minRovers.addElement(settlement);
                    min = roverNum;
                }
            }

            // Create the rover
            int randSettlement = RandomUtil.getRandomInt(minRovers.size() - 1);
            createVehicle(roverNames[x], new Coordinates(0D, 0D),
                    (Settlement) minRovers.elementAt(randSettlement));
        }
    }

    /** Creates a vehicle
     *  @param name the vehicle's name
     *  @param coords the vehicle's location
     *  @param seti the vehicle's settlement
     */
    void createVehicle(String name, Coordinates coords, Settlement seti) {
        Rover tempVehicle = new Rover(name, coords, mars, this);
        // Add rover to master unit and vehicles vectors
        unitVector.addElement(tempVehicle);
        vehiclesVector.addElement(tempVehicle);
        tempVehicle.setSettlement(seti);
    }

    /** Creates initial people at random settlements */
    private void createPeople() {
        // Get people names from "people.conf"
        String[] peopleNames = ConfFileProcessor.getPersonNames();

        // Create a Person object for each name
        // Choose a random settlement to put the person.
        for (int x = 0; x < peopleNames.length; x++) {

            Vector minPeople = new Vector();
            int min = Integer.MAX_VALUE;
            for (int y=0; y < settlementsVector.size(); y++) {
                Settlement settlement = (Settlement) settlementsVector.elementAt(y);
                int inhabitantNum = settlement.getPeopleNum();
                FacilityManager facilityManager = settlement.getFacilityManager();
                LivingQuartersFacility livingQuarters = (LivingQuartersFacility) facilityManager.getFacility("Living Quarters");
                int maxCapacity = livingQuarters.getMaximumCapacity();
                if (inhabitantNum < maxCapacity) {
                    if (inhabitantNum == min) minPeople.addElement(settlement);
                    else if (inhabitantNum < min) {
                        minPeople.removeAllElements();
                        minPeople.addElement(settlement);
                        min = inhabitantNum;
                    }
                }
            }

            // Create the person 
            int randSettlement = RandomUtil.getRandomInt(minPeople.size() - 1);
            createPerson(peopleNames[x], new Coordinates(0D, 0D),
                    (Settlement) minPeople.elementAt(randSettlement));
        }
    }

    /** Creates a person
     *  @param name the person's name
     *  @param coord the person's location
     *  @param seti the person's settlement
     */
    private void createPerson(String name, Coordinates coord, Settlement seti) {
        // Create a person with that name
        Person tempPerson = new Person(name, coord, mars, this);
        // Add person to master unit and people vectors
        unitVector.addElement(tempPerson);
        peopleVector.addElement(tempPerson);
        tempPerson.setSettlement(seti);
    }


    /** Notify all the units that time has passed. Times they are a
     *  changing.
     *  @param time the amount time passing (in millisols)  
     */
    void timePassing(double time) {
        for (int x = 0; x < unitVector.size(); x++) {
            ((Unit) unitVector.elementAt(x)).timePassing(time);
        }
    }

    /** Get number of settlements 
     *  @return the number of settlements
     */
    public int getSettlementNum() {
        return settlementsVector.size();
    }

    /** Get number of vehicles 
     *  @return the number of vehicles
     */
    public int getVehicleNum() {
        return vehiclesVector.size();
    }

    /** Get population 
     *  @return the number of people
     */
    public int getPeopleNum() {
        return peopleVector.size();
    }

    /** Get a random settlement 
     *  @return a random settlement
     */
    public Settlement getRandomSettlement() {
        int r = RandomUtil.getRandomInt(settlementsVector.size() - 1);
        return (Settlement) settlementsVector.elementAt(r);
    }

    /** Get a random settlement other than given current one. 
     *  @return a random settlement
     */
    public Settlement getRandomSettlement(Settlement current) {
        Settlement newSettlement;
        do {
            newSettlement = getRandomSettlement();
        } while (newSettlement == current)
            ;

        return newSettlement;
    }

    /** Get a random settlement among the closest three settlements to
     *  the given location.
     *  @return a random settlement  
     */
    public Settlement getRandomOfThreeClosestSettlements(Coordinates location) {
        Vector tempVector = new Vector();
        Vector resultVector = new Vector();

        for (int x = 0; x < settlementsVector.size(); x++) {
            Settlement tempSettlement = (Settlement) settlementsVector.elementAt(x);
            if (!tempSettlement.getCoordinates().equals(location))
                tempVector.addElement(tempSettlement);
        }

        for (int x = 0; x < 3; x++) {
            Settlement nearestSettlement = null;
            double smallestDistance = 100000D;
            for (int y = 0; y < tempVector.size(); y++) {
                Settlement tempSettlement = (Settlement) tempVector.elementAt(y);
                double tempDistance = location.getDistance(tempSettlement.getCoordinates());
                if ((tempDistance < smallestDistance) && (tempDistance != 0D)) {
                    smallestDistance = tempDistance;
                    nearestSettlement = tempSettlement;
                }
            }
            resultVector.addElement(nearestSettlement);
            tempVector.removeElement(nearestSettlement);
        }

        int r = RandomUtil.getRandomInt(2);
        return (Settlement) resultVector.elementAt(r);
    }

    /** Get a random settlement among the closest three settlements to
     *  the given settlement
     *  @return a random settlement  
     */
    public Settlement getRandomOfThreeClosestSettlements(Settlement current) {
        return getRandomOfThreeClosestSettlements(current.getCoordinates());
    }

    /** The total number of units 
     *  @return the total number of units
     */
    public int getUnitCount() {
        return unitVector.size();
    }

    /** Returns an array of all the units. 
     *  @return an array of all the units
     */
    public Unit[] getUnits() {
        Unit[] units = new Unit[unitVector.size()];
        for (int x = 0; x < units.length; x++)
            units[x] = (Unit) unitVector.elementAt(x);
        return units;
    }
    
    public Vehicle[] getVehicles() {
        Vehicle[] vehicles = new Vehicle[vehiclesVector.size()];
        for (int x=0; x < vehicles.length; x++)
            vehicles[x] = (Vehicle) vehiclesVector.elementAt(x);
        return vehicles;
    }
}
