/**
 * Mars Simulation Project
 * UnitManager.java
 * @version 2.75 2003-01-19
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

    // Static Data Members
    public static String SETTLEMENT = "settlement";
    public static String PERSON = "person";
    public static String VEHICLE = "vehicle";
    public static String EQUIPMENT = "equipment";
    
    // Data members
    private Mars mars; // Virtual Mars
    private UnitCollection units; // Collection of all units
    private ArrayList settlementNames; // List of possible settlement names
    private ArrayList vehicleNames; // List of possible vehicle names
    private ArrayList personNames; // List of possible person names

    /** Constructs a UnitManager object
     *  @param mars the virtual Mars
     */
    UnitManager(SimulationProperties p, Mars mars) {
        // Initialize virtual mars to parameter
        this.mars = mars;
   
        // Initialize unit collection
        units = new UnitCollection();
    }
    
    /**
     * Constructs initial units.
     */
    void constructInitialUnits() {
        
        // Initialize name lists;
        initializePersonNames();
        initializeSettlementNames();
        initializeVehicleNames();
        
        // Create initial settlements
        createInitialSettlements();

	    // Add any inventoried units.
	    addInventoryUnits();
    }

    /**
     * Initializes the list of possible person names.
     */
    private void initializePersonNames() {
        PersonNamesXmlReader personNamesReader = new PersonNamesXmlReader();
        personNamesReader.parse();
        personNames = personNamesReader.getPersonNames();
    }
    
    /**
     * Initializes the list of possible vehicle names.
     */
    private void initializeVehicleNames() {
        VehicleNamesXmlReader vehicleNamesReader = new VehicleNamesXmlReader();
        vehicleNamesReader.parse();
        vehicleNames = vehicleNamesReader.getVehicleNames();
    }
    
    /**
     * Initializes the list of possible settlement names.
     */
    private void initializeSettlementNames() {
        SettlementNamesXmlReader settlementNamesReader = new SettlementNamesXmlReader();
        settlementNamesReader.parse();
        settlementNames = settlementNamesReader.getSettlementNames();
    }
    
    /**
     * Adds a unit to the unit manager if it doesn't already have it.
     *
     * @param unit new unit to add.
     */
    public void addUnit(Unit unit) {
        if (!units.contains(unit)) units.add(unit);
    }
    
    /**
     * Gets a new name for a unit.
     *
     * @param unitType the type of unit.
     * @return new name
     * @throws IllegalArgumentException if unitType is not valid.
     */
    public String getNewName(String unitType) throws IllegalArgumentException {
        Collection initialNameList = null;
        ArrayList usedNames = new ArrayList();
        
        if (unitType.equals(SETTLEMENT)) {
            initialNameList = settlementNames;
            SettlementIterator si = getSettlements().iterator();
            while (si.hasNext()) usedNames.add(si.next().getName());
        }
        else if (unitType.equals(VEHICLE)) {
            initialNameList = vehicleNames;
            VehicleIterator vi = getVehicles().iterator();
            while (vi.hasNext()) usedNames.add(vi.next().getName());
        }
        else if (unitType.equals(PERSON)) {
            initialNameList = personNames;
            PersonIterator pi = getPeople().iterator();
            while (pi.hasNext()) usedNames.add(pi.next().getName());
        }
        else throw new IllegalArgumentException("Inproper unitType");
 
        ArrayList remainingNames = new ArrayList();
        Iterator i = initialNameList.iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            if (!usedNames.contains(name)) remainingNames.add(name);
        }
            
        String result = "";
        if (remainingNames.size() > 0) result = (String) remainingNames.get(
                RandomUtil.getRandomInt(remainingNames.size() - 1));
        else if (usedNames.size() > 0) result = (String) usedNames.get(
                RandomUtil.getRandomInt(usedNames.size() - 1));      
                
        return result;
    }       
    
    /** Creates initial settlements */
    private void createInitialSettlements() {
        SettlementsXmlReader settlementsReader = new SettlementsXmlReader(mars);
        settlementsReader.parse();
        units.mergeSettlements(settlementsReader.getInitialSettlements());
    }

    /** Adds all units in inventories. */
    private void addInventoryUnits() {
	    UnitCollection contained = new UnitCollection();
	    UnitIterator i = units.iterator();
	    while (i.hasNext())
	        contained.mergeUnits(i.next().getInventory().getAllContainedUnits());
	    units.mergeUnits(contained);
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
        //return new UnitCollection(units);
        return units;
    }
}
