/**
 * Mars Simulation Project
 * Settlement.java
 * @version 2.74 2002-01-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.vehicle.*;
import java.util.*;

/** The Settlement class represents a settlement unit on virtual Mars.
 *  It contains information related to the state of the settlement.
 */
public class Settlement extends Structure {

    // Default population capacity for a settlement
    private static int DEFAULT_POPULATION_CAPACITY = 20;
    private static Random rand = new Random();
    
    // Data members
    Vector people; // List of inhabitants
    Vector vehicles; // List of parked vehicles
    int populationCapacity; // The population capacity of the settlement
    FacilityManager facilityManager; // The facility manager for the settlement

    /** Constructs a Settlement object at a given location
     *  @param name the settlement's name
     *  @param location the settlement's location
     *  @param populationCapacity the settlement's population capacity
     *  @param mars the virtual Mars
     */
    Settlement(String name, Coordinates location, int populationCapacity, VirtualMars mars) {
        // Use Unit constructor
        super(name, location, mars);

        // Initialize data members
        people = new Vector();
        vehicles = new Vector();
        if (populationCapacity == 0) this.populationCapacity = DEFAULT_POPULATION_CAPACITY;
        else this.populationCapacity = populationCapacity;
        facilityManager = new FacilityManager(this, mars);
    }
    
    /** Constructs a Settlement object at a random location
     *  @param name the settlement's name
     *  @param populationCapacity the settlement's population capacity
     *  @param mars the virtual Mars
     */
    Settlement(String name, int populationCapacity, VirtualMars mars) {
        
        // Use Unit constructor
        super(name, new Coordinates(0D, 0D), mars);
        
        // Determine random location of settlement, adjust so it will be less likely to be near the poles
        double settlementPhi = (rand.nextGaussian() * (Math.PI / 7D)) + (Math.PI / 2D);
        // double settlementPhi = (new Random().nextGaussian() * (Math.PI / 7D)) + (Math.PI / 2D);
        if (settlementPhi > Math.PI) settlementPhi = Math.PI;
        if (settlementPhi < 0D) settlementPhi = 0D;
        double settlementTheta = (double)(Math.random() * (2D * Math.PI));
        setCoordinates(new Coordinates(settlementPhi, settlementTheta));
      
        // Initialize data members
        people = new Vector();
        vehicles = new Vector();
        if (populationCapacity == 0) this.populationCapacity = DEFAULT_POPULATION_CAPACITY;
        else this.populationCapacity = populationCapacity;
        facilityManager = new FacilityManager(this, mars);
    }

    /** Returns the facility manager for the settlement 
     *  @return the settlement's facility manager
     */
    public FacilityManager getFacilityManager() {
        return facilityManager;
    }

    /** Gets the population capacity of the settlement
     *  @return the population capacity
     */
    public int getPopulationCapacity() {
        return populationCapacity;
    }

    /** Gets the current population of the settlement
     *  @return the number of inhabitants
     */
    public int getCurrentPopulation() {
        return people.size();
    }
    
    /** Gets the current available population capacity 
     *  of the settlement
     *  @return the available population capacity
     */
    public int getAvailablePopulationCapacity() {
        return populationCapacity - people.size();
    }

    /** Gets an array of current inhabitants of the settlement
     *  @return array of inhabitants
     */
    public Person[] getInhabitantArray() {
        Person[] personArray = new Person[people.size()];
        for (int x=0; x < people.size(); x++) 
            personArray[x] = (Person) people.elementAt(x); 
        return personArray;
    }

    /** Get number of inhabitants in settlement 
     *  @return the number of inhabitants
     */
    public int getPeopleNum() {
        return people.size();
    }

    /** Get number of parked vehicles in settlement 
     *  @return the number of parked vehicles
     */
    public int getVehicleNum() {
        return vehicles.size();
    }

    /** Get an inhabitant at a given vector index 
     *  @param index the inhabitant's index
     *  @return the inhabitant
     */
    public Person getPerson(int index) {
        if (index < people.size()) {
            return (Person) people.elementAt(index);
        } else {
            return null;
        }
    }

    /** Get a parked vehicle at a given vector index. 
     *  @param the vehicle's index
     *  @return the vehicle
     */
    public Vehicle getVehicle(int index) {
        if (index < vehicles.size()) {
            return (Vehicle) vehicles.elementAt(index);
        } else {
            return null;
        }
    }

    /** Determines if given person is an inhabitant of this settlement.
     *  @return true if person is an inhabitant of this settlement
     */
    public boolean isInhabitant(Person person) {
        boolean result = false;
        for (int x=0; x < people.size(); x++) {
            if (people.contains(person)) result = true;
        }
        return result;
    } 

    /** Bring in a new inhabitant 
     *  @param newPerson the new person
     */
    public void addPerson(Person newPerson) {
        if (!isInhabitant(newPerson)) people.addElement(newPerson);
    }

    /** Make a given inhabitant leave the settlement 
     *  @param person the person leaving
     */
    public void personLeave(Person person) {
        if (people.contains(person)) {
            people.removeElement(person);
        }
    }

    /** Bring in a new vehicle to be parked 
     *  @param newVehicle the new vehicle
     */
    public void addVehicle(Vehicle newVehicle) {
        if (!vehicles.contains(newVehicle)) vehicles.addElement(newVehicle);
    }

    /** Make a given vehicle leave the settlement 
     *  @param vehicle the vehicle leaving
     */
    public void vehicleLeave(Vehicle vehicle) {
        if (vehicles.contains(vehicle)) vehicles.removeElement(vehicle);
    }

    /** Perform time-related processes 
     *  @param time the amount of time passing (in millisols)
     */
    public void timePassing(double time) {
        facilityManager.timePassing(time);
    }
}
