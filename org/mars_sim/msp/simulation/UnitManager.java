/**
 * Mars Simulation Project
 * UnitManager.java
 * @version 2.73 2001-11-18
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

        // Create settlements
        createSettlements();

        // Create vehicles
        createVehicles();
 
        // Create people
        createPeople();
    }

    /** Creates initial settlements from XML config file */
    private void createSettlements() {
        SettlementsXmlReader settlementsReader = new SettlementsXmlReader(mars);
        settlementsReader.parse();
        settlementsVector = settlementsReader.getSettlements();
        for (int x=0; x < settlementsVector.size(); x++) 
            unitVector.addElement(settlementsVector.elementAt(x));
    }

    /** Creats initial vehicles from XML config file */
    private void createVehicles() {

        VehiclesXmlReader vehiclesReader = new VehiclesXmlReader(this, mars);
        vehiclesReader.parse();
        vehiclesVector = vehiclesReader.getVehicles();
        for (int x=0; x < vehiclesVector.size(); x++)
            unitVector.addElement(vehiclesVector.elementAt(x));
    }

    /** Creates initial people from XML config file */
    private void createPeople() {

        PeopleXmlReader peopleReader = new PeopleXmlReader(this, mars);
        peopleReader.parse();
        peopleVector = peopleReader.getPeople();
        for (int x=0; x < peopleVector.size(); x++) 
            unitVector.addElement(peopleVector.elementAt(x));
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

    public Vector getSettlements() {
        return new Vector(settlementsVector);
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

    public Settlement getSettlement(String settlementName) {
        Settlement result = null;

        for (int x=0; x < settlementsVector.size(); x++) {
            Settlement settlement = (Settlement) settlementsVector.elementAt(x);
            if (settlement.getName().equals(settlementName)) result = settlement;
        }

        return result;
    }
}
