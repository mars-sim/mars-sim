/**
 * Mars Simulation Project
 * UnitManager.java
 * @version 2.76 2004-06-01
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.equipment.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;

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
    private UnitCollection units; // Collection of all units
    private List settlementNames; // List of possible settlement names
    private List vehicleNames; // List of possible vehicle names
    private List personNames; // List of possible person names

    /** 
     * Constructor
     */
    UnitManager() {
   
        // Initialize unit collection
        units = new UnitCollection();
    }
    
    /**
     * Constructs initial units.
     * @throws Exception in unable to load names.
     */
    void constructInitialUnits() throws Exception {
        
        // Initialize name lists;
        initializePersonNames();
        initializeSettlementNames();
        initializeVehicleNames();
        
        // Create initial settlements
        createInitialSettlements();
        createInitialVehicles();
        createInitialPeople();
    }

    /**
     * Initializes the list of possible person names.
     * @throws Exception if unable to load name list.
     */
    private void initializePersonNames() throws Exception {
 		try {
			SimulationConfig simConfig = Simulation.instance().getSimConfig();
			PersonConfig personConfig = simConfig.getPersonConfiguration();
    		personNames = personConfig.getPersonNameList();
 		}
 		catch (Exception e) {
 			throw new Exception("person names could not be loaded: " + e.getMessage());
 		}
    }
    
    /**
     * Initializes the list of possible vehicle names.
     * @throws Exception if unable to load rover names.
     */
    private void initializeVehicleNames() throws Exception {
        try {
			SimulationConfig simConfig = Simulation.instance().getSimConfig();
			VehicleConfig vehicleConfig = simConfig.getVehicleConfiguration();
        	vehicleNames = vehicleConfig.getRoverNameList();
        }
        catch (Exception e) {
        	throw new Exception("rover names could not be loaded: " + e.getMessage());
        }
    }
    
    /**
     * Initializes the list of possible settlement names.
     * @throws Exception if unable to load settlement names.
     */
    private void initializeSettlementNames() throws Exception {
		try {
			SimulationConfig simConfig = Simulation.instance().getSimConfig();
			SettlementConfig settlementConfig = simConfig.getSettlementConfiguration();
			settlementNames = settlementConfig.getSettlementNameList();
		}
		catch (Exception e) {
			throw new Exception("settlement names could not be loaded: " + e.getMessage());
		}
    }
    
    /**
     * Adds a unit to the unit manager if it doesn't already have it.
     *
     * @param unit new unit to add.
     */
    public void addUnit(Unit unit) {
        if (!units.contains(unit)) {
            units.add(unit);
            units.mergeUnits(unit.getInventory().getContainedUnits());
        }
    }
    
    /**
     * Gets a new name for a unit.
     *
     * @param unitType the type of unit.
     * @return new name
     * @throws IllegalArgumentException if unitType is not valid.
     */
    public String getNewName(String unitType) {
        
        Collection initialNameList = null;
        ArrayList usedNames = new ArrayList();
        String unitName = "";
        
        if (unitType.equals(SETTLEMENT)) {
            initialNameList = settlementNames;
            SettlementIterator si = getSettlements().iterator();
            while (si.hasNext()) usedNames.add(si.next().getName());
            unitName = "Settlement";
        }
        else if (unitType.equals(VEHICLE)) {
            initialNameList = vehicleNames;
            VehicleIterator vi = getVehicles().iterator();
            while (vi.hasNext()) usedNames.add(vi.next().getName());
            unitName = "Vehicle";
        }
        else if (unitType.equals(PERSON)) {
            initialNameList = personNames;
            PersonIterator pi = getPeople().iterator();
            while (pi.hasNext()) usedNames.add(pi.next().getName());
            unitName = "Person";
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
        else result = unitName + " " + (usedNames.size() + 1);  
                
        return result;
    }       
    
    /** 
     * Creates initial settlements 
     */
    private void createInitialSettlements() throws Exception {
    	
		SimulationConfig simConfig = Simulation.instance().getSimConfig();
    	SettlementConfig config = simConfig.getSettlementConfiguration();
    	
		try {
			for (int x=0; x < config.getNumberOfInitialSettlements(); x++) {
				// Get settlement name
				String name = config.getInitialSettlementName(x);
				if (name.equals(SettlementConfig.RANDOM)) name = getNewName(SETTLEMENT);
				
				// Get settlement template
				String template = config.getInitialSettlementTemplate(x);
				
				// Get settlement longitude
				double longitude = 0D;
				String longitudeStr = config.getInitialSettlementLongitude(x);
				if (longitudeStr.equals(SettlementConfig.RANDOM)) longitude = Coordinates.getRandomLongitude();
				else longitude = Coordinates.parseLongitude(longitudeStr);
				
				// Get settlement latitude
				double latitude = 0D;
				String latitudeStr = config.getInitialSettlementLatitude(x);
				if (latitudeStr.equals(SettlementConfig.RANDOM)) latitude = Coordinates.getRandomLatitude();
				else latitude = Coordinates.parseLatitude(latitudeStr);
				
				Coordinates location = new Coordinates(latitude, longitude);
				
				addUnit(new Settlement(name, template, location));
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			throw new Exception("Settlements could not be created: " + e.getMessage());
		}
    }
    
    /**
     * Creates initial vehicles based on settlement templates.
     * @throws Exception if vehicles could not be constructed.
     */
    private void createInitialVehicles() throws Exception {
    	
		SimulationConfig simConfig = Simulation.instance().getSimConfig();
		SettlementConfig config = simConfig.getSettlementConfiguration();
    	
    	try {
    		SettlementIterator i = getSettlements().iterator();
    		while (i.hasNext()) {
    			Settlement settlement = i.next();
    			List vehicleTypes = config.getTemplateVehicleTypes(settlement.getTemplate());
    			Iterator j = vehicleTypes.iterator();
    			while (j.hasNext()) {
    				String vehicleType = (String) j.next();
    				addUnit(new Rover(getNewName(VEHICLE), vehicleType, settlement));
    			}
    		}
    	}
    	catch (Exception e) {
    		throw new Exception("Vehicles could not be created: " + e.getMessage());
    	}
    }
    
    /**
     * Creates initial people based on available capacity at settlements.
     * @throws Exception if people can not be constructed.
     */
    private void createInitialPeople() throws Exception {
    	
    	try {
    		SettlementIterator i = getSettlements().iterator();
    		while (i.hasNext()) {
    			Settlement settlement = i.next();
    			while (settlement.getAvailablePopulationCapacity() > 0) 
    				addUnit(new Person(getNewName(PERSON), settlement));
    		}
    	}
    	catch (Exception e) {
    		throw new Exception("People could not be created: " + e.getMessage());
    	}
    }

    /** 
     * Notify all the units that time has passed.
     * Times they are a changing.
     * @param time the amount time passing (in millisols)
     * @throws Exception if error during time passing.
     */
    void timePassing(double time) throws Exception {
        UnitIterator i = getUnits().iterator();
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
    
    /** 
     * Get number of equipment
     *
     * @return the number of equipment
     */
    public int getEquipmentNum() {
        return units.getEquipment().size();
    }

    /** 
     * Get all of the equipment.
     *
     * @return EquipmentCollection of equipment
     */
    public EquipmentCollection getEquipment() {
        return units.getEquipment();
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