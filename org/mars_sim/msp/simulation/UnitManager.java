/**
 * Mars Simulation Project
 * UnitManager.java
 * @version 2.74 2002-03-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.simulation.equipment.*;
import java.io.*;
import java.util.*;

/** The UnitManager class contains and manages all units in virtual
 *  Mars. It has methods for getting information about units. It is
 *  also responsible for creating all units on its construction.
 *  There should be only one instance of this class and it should be
 *  constructed and owned by the virtual Mars object.
 */
public class UnitManager implements Serializable {

    // Data members
    private Mars mars; // Virtual Mars
    private UnitCollection units; // Collection of all units

    /** Constructs a UnitManager object
     *  @param mars the virtual Mars
     */
    UnitManager(Mars mars) {
        // Initialize virtual mars to parameter
        this.mars = mars;

        // Initialize all unit vectors
        units = new UnitCollection();

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
        units.mergeSettlements(settlementsReader.getSettlements());
    }

    /** Creats initial vehicles from XML config file */
    private void createVehicles() {
        VehiclesXmlReader vehiclesReader = new VehiclesXmlReader(this, mars);
        vehiclesReader.parse();
        units.mergeVehicles(vehiclesReader.getVehicles());
    }

    /** Creates initial people from XML config file */
    private void createPeople() {
        PeopleXmlReader peopleReader = new PeopleXmlReader(this, mars);
        peopleReader.parse();
        units.mergePeople(peopleReader.getPeople());
    }

    /** Notify all the units that time has passed. 
     *  Times they are a changing.
     *  @param time the amount time passing (in millisols)  
     */
    void timePassing(double time) {
        UnitIterator i = units.iterator();
        while (i.hasNext()) i.next().timePassing(time);
    }

    /** Get number of settlements 
     *  @return the number of settlements
     */
    public int getSettlementNum() {
        return units.getSettlements().size();
    }
    
    /** Get settlements in vitual Mars
     *  @return SettlementCollection of settlements
     */
    public SettlementCollection getSettlements() {
        return units.getSettlements();
    }

    /** Get number of vehicles 
     *  @return the number of vehicles
     */
    public int getVehicleNum() {
        return units.getVehicles().size();
    }
    
    /** Get vehicles in virtual Mars
     *  @return VehicleCollection of vehicles
     */
    public VehicleCollection getVehicles() {
        return units.getVehicles();
    }

    /** Get number of people
     *  @return the number of people
     */
    public int getPeopleNum() {
        return units.getPeople().size();
    }
    
    /** Get people in virtual Mars
     *  @return PersonCollection of people
     */
    public PersonCollection getPeople() {
        return units.getPeople();
    }

    /** The total number of units 
     *  @return the total number of units
     */
    public int getUnitNum() {
        return units.size();
    }
    
    /** Get all units in virtual Mars
     *  @return UnitColleciton of units
     */
    public UnitCollection getUnits() {
        return new UnitCollection(units);
    }
}
