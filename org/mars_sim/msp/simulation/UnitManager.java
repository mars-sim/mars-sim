/**
 * Mars Simulation Project
 * UnitManager.java
 * @version 2.78 2005-10-07
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.equipment.Equipment;
import org.mars_sim.msp.simulation.equipment.EquipmentCollection;
import org.mars_sim.msp.simulation.equipment.EquipmentFactory;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.person.ai.social.Relationship;
import org.mars_sim.msp.simulation.person.ai.social.RelationshipManager;
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
    private List personMaleNames; // List of possible male person names
    private List personFemaleNames; // List of possible female person names

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
        
        // Initialize name lists
        initializePersonNames();
        initializeSettlementNames();
        initializeVehicleNames();
        
        // Create initial units.
        createInitialSettlements();
        createInitialVehicles();
        createInitialEquipment();
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
    		List personNames = personConfig.getPersonNameList();
    		personMaleNames = new ArrayList();
    		personFemaleNames = new ArrayList();
    		Iterator i = personNames.iterator();
    		while (i.hasNext()) {
    			String name = (String) i.next();
    			String gender = personConfig.getPersonGender(name);
    			if (gender.equals(Person.MALE)) personMaleNames.add(name);
    			else if (gender.equals(Person.FEMALE)) personFemaleNames.add(name);
    		}
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
     * @param gender the gender of the person or null if not a person.
     * @return new name
     * @throws IllegalArgumentException if unitType is not valid.
     */
    public String getNewName(String unitType, String gender) {
        
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
        	if (Person.MALE.equals(gender)) initialNameList = personMaleNames;
        	else if (Person.FEMALE.equals(gender)) initialNameList = personFemaleNames;
        	else throw new IllegalArgumentException("Improper gender for person unitType: " + gender);
            PersonIterator pi = getPeople().iterator();
            while (pi.hasNext()) usedNames.add(pi.next().getName());
            unitName = "Person";
        }
        else throw new IllegalArgumentException("Improper unitType");
 
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
				if (name.equals(SettlementConfig.RANDOM)) name = getNewName(SETTLEMENT, null);
				
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
    				addUnit(new Rover(getNewName(VEHICLE, null), vehicleType, settlement));
    			}
    		}
    	}
    	catch (Exception e) {
    		throw new Exception("Vehicles could not be created: " + e.getMessage());
    	}
    }
    
    /**
     * Creates the initial equipment at a settlement.
     * @throws Exception if error constructing equipment.
     */
    private void createInitialEquipment() throws Exception {
    	
		SimulationConfig simConfig = Simulation.instance().getSimConfig();
		SettlementConfig config = simConfig.getSettlementConfiguration();
    	
    	try {
    		SettlementIterator i = getSettlements().iterator();
    		while (i.hasNext()) {
    			Settlement settlement = i.next();
    			Map equipmentMap = config.getTemplateEquipment(settlement.getTemplate());
    			Iterator j = equipmentMap.keySet().iterator();
    			while (j.hasNext()) {
    				String type = (String) j.next();
    				int number = ((Integer) equipmentMap.get(type)).intValue();
    				for (int x = 0; x < number; x++) {
    					Equipment equipment = EquipmentFactory.getEquipment(type, settlement.getCoordinates());
    					settlement.getInventory().storeUnit(equipment);
    					addUnit(equipment);
    				}
    			}
    		}
    	}
    	catch (Exception e) {
    		throw new Exception("Equipment could not be created: " + e.getMessage());
    	}
    }
    
    /**
     * Creates initial people based on available capacity at settlements.
     * @throws Exception if people can not be constructed.
     */
    private void createInitialPeople() throws Exception {
    	
    	// Create configured people.
    	createConfiguredPeople();
    	
    	PersonConfig personConfig = Simulation.instance().getSimConfig().getPersonConfiguration();
		RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		
		// Randomly create all remaining people to fill the settlements to capacity.
    	try {
    		SettlementIterator i = getSettlements().iterator();
    		while (i.hasNext()) {
    			Settlement settlement = i.next();
    			
    			while (settlement.getAvailablePopulationCapacity() > 0) {
    				String gender = Person.FEMALE;
    				if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) gender = Person.MALE;
    				Person person = new Person(getNewName(PERSON, gender), gender, settlement);
    				addUnit(person);
    				relationshipManager.addInitialSettler(person, settlement);
    			}
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace(System.err);
    		throw new Exception("People could not be created: " + e.getMessage());
    	}
    }
    
    /**
     * Creates all configured people.
     * @throws Exception if error parsing XML.
     */
    private void createConfiguredPeople() throws Exception {
    	PersonConfig personConfig = Simulation.instance().getSimConfig().getPersonConfiguration();
		RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
    	
		// Create all configured people.
		for (int x = 0; x < personConfig.getNumberOfConfiguredPeople(); x++) {
			try {
				// Get person's name (required)
				String name = personConfig.getConfiguredPersonName(x);
				if (name == null) throw new Exception("Person name is null");
				
				// Get person's gender or randomly determine it if not configured.
				String gender = personConfig.getConfiguredPersonGender(x);
				if (gender == null) {
					gender = Person.FEMALE;
					if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) gender = Person.MALE;
				}
				
				// Get person's settlement or randomly determine it if not configured.
				String settlementName = personConfig.getConfiguredPersonSettlement(x);
				Settlement settlement = null;
				if (settlementName != null) settlement = getSettlements().getSettlement(settlementName);
				else settlement = getSettlements().getRandomSettlement();

				// Create person and add to the unit manager.
				Person person = new Person(name, gender, settlement);
				addUnit(person);
				relationshipManager.addInitialSettler(person, settlement);
				
				// Set person's configured personality type (if any).
				String personalityType = personConfig.getConfiguredPersonPersonalityType(x);
				if (personalityType != null) person.getMind().getPersonalityType().setTypeString(personalityType);
				
				// Set person's job (if any).
				String jobName = personConfig.getConfiguredPersonJob(x);
				if (jobName != null) {
					Job job = Simulation.instance().getJobManager().getJob(jobName);
					person.getMind().setJob(job, true);
				}
				
				// Set person's configured natural attributes (if any).
				Map naturalAttributeMap = personConfig.getNaturalAttributeMap(x);
				if (naturalAttributeMap != null) {
					Iterator i = naturalAttributeMap.keySet().iterator();
					while (i.hasNext()) {
						String attributeName = (String) i.next();
						int value = ((Integer) naturalAttributeMap.get(attributeName)).intValue();
						person.getNaturalAttributeManager().setAttribute(attributeName, value);
					}
				}
				
				// Set person's configured skills (if any).
				Map skillMap = personConfig.getSkillMap(x);
				if (skillMap != null) {
					Iterator i = skillMap.keySet().iterator();
					while (i.hasNext()) {
						String skillName = (String) i.next();
						int level = ((Integer) skillMap.get(skillName)).intValue();
						person.getMind().getSkillManager().addNewSkill(new Skill(skillName, level));
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
				System.err.println("Configured person could not be created: " + e.getMessage());
			}
		}
		
		// Create all configured relationships.
		createConfiguredRelationships();
    }
    
    /**
     * Creates all configured people relationships.
     * @throws Exception if error parsing XML.
     */
    private void createConfiguredRelationships() throws Exception{
    	PersonConfig personConfig = Simulation.instance().getSimConfig().getPersonConfiguration();
		RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		
		// Create all configured people relationships.
		for (int x = 0; x < personConfig.getNumberOfConfiguredPeople(); x++) {
			try {
				// Get person's name
				String name = personConfig.getConfiguredPersonName(x);
				if (name == null) throw new Exception("Person name is null");
				
				// Get the person
				Person person = null;
				PersonIterator j = getPeople().iterator();
				while (j.hasNext()) {
					Person tempPerson = j.next();
					if (tempPerson.getName().equals(name)) person = tempPerson;
				}
				if (person == null) throw new Exception("Person: " + name + " not found.");
				
				// Set person's configured relationships (if any).
				Map relationshipMap = personConfig.getRelationshipMap(x);
				if (relationshipMap != null) {
					Iterator i = relationshipMap.keySet().iterator();
					while (i.hasNext()) {
						String relationshipName = (String) i.next();
						
						// Get the other person in the relationship.
						Person relationshipPerson = null;
						PersonIterator k = getPeople().iterator();
						while (k.hasNext()) {
							Person tempPerson = k.next();
							if (tempPerson.getName().equals(relationshipName)) relationshipPerson = tempPerson;
						}
						if (relationshipPerson == null) throw new Exception("Person: " + relationshipName + " not found.");
						
						int opinion = ((Integer) relationshipMap.get(relationshipName)).intValue();
						
						// Set the relationship opinion.
						Relationship relationship = relationshipManager.getRelationship(person, relationshipPerson);
						if (relationship != null) relationship.setPersonOpinion(person, opinion);
						else {
							relationshipManager.addRelationship(person, relationshipPerson, Relationship.EXISTING_RELATIONSHIP);
							relationship = relationshipManager.getRelationship(person, relationshipPerson);
							relationship.setPersonOpinion(person, opinion);
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
				System.err.println("Configured relationship could not be created: " + e.getMessage());
			}
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