/**
 * Mars Simulation Project
 * Settlement.java
 * @version 2.71 2000-11-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.Vector;

/** The Settlement class represents a settlement unit on virtual Mars.
 *  It contains information related to the state of the settlement.
 */
public class Settlement extends Structure {

    // Data members
    Vector people; // List of inhabitants
    Vector vehicles; // List of parked vehicles
    FacilityManager facilityManager; // The facility manager for the settlement.

    /** Constructs a Settlement object
     *  @param name the settlement's name
     *  @param location the settlement's location
     *  @param mars the virtual Mars
     *  @param manager the settlement's unit manager
     */
    Settlement(String name, Coordinates location, VirtualMars mars, UnitManager manager) {

        // Use Unit constructor
        super(name, location, mars, manager);

        // Initialize data members
        people = new Vector();
        vehicles = new Vector();
        facilityManager = new FacilityManager(this);
    }

    /** Returns the facility manager for the settlement 
     *  @return the settlement's facility manager
     */
    public FacilityManager getFacilityManager() {
        return facilityManager;
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

    /** Bring in a new inhabitant 
     *  @param newPerson the new person
     */
    void addPerson(Person newPerson) {
        people.addElement(newPerson);
    }

    /** Make a given inhabitant leave the settlement 
     *  @param person the person leaving
     */
    void personLeave(Person person) {
        if (people.contains(person)) {
            people.removeElement(person);
        }
    }

    /** Bring in a new vehicle to be parked 
     *  @param newVehicle the new vehicle
     */
    void addVehicle(Vehicle newVehicle) {
        vehicles.addElement(newVehicle);
    }

    /** Make a given vehicle leave the settlement 
     *  @param vehicle the vehicle leaving
     */
    void vehicleLeave(Vehicle vehicle) {
        if (vehicles.contains(vehicle)) {
            vehicles.removeElement(vehicle);
        }
    }

    /** Perform time-related processes 
     *  @param seconds the seconds passing
     */
    void timePasses(int seconds) {
        facilityManager.timePasses(seconds);
    }
}
