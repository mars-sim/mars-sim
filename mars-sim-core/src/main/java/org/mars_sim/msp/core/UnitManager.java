/**
 * Mars Simulation Project
 * UnitManager.java
 * @version 3.04 2013-03-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleConfig;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/** The UnitManager class contains and manages all units in virtual
 *  Mars. It has methods for getting information about units. It is
 *  also responsible for creating all units on its construction.
 *  There should be only one instance of this class and it should be
 *  constructed and owned by the virtual Mars object.
 */
public class UnitManager implements Serializable {

    private static Logger logger = Logger.getLogger(UnitManager.class.getName());
    
    // Static Data Members
    public static final String SETTLEMENT = "settlement";
    public static final String PERSON = "person";
    public static final String VEHICLE = "vehicle";
    public static final String EQUIPMENT = "equipment";
    
    // Data members
    private Collection<Unit> units; // Collection of all units
    private List<String> settlementNames; // List of possible settlement names
    private List<String> vehicleNames; // List of possible vehicle names
    private List<String> personMaleNames; // List of possible male person names
    private List<String> personFemaleNames; // List of possible female person names
    private transient List<UnitManagerListener> listeners; // List of unit manager listeners.
    private Map<String, Integer> equipmentNumberMap; // Map of equipment types and their numbers.
    private Map<String, Integer> vehicleNumberMap; // Map of vehicle types and their numbers.

    /** 
     * Constructor
     */
    UnitManager() {

        // Initialize unit collection
        units = new ConcurrentLinkedQueue<Unit>();
        listeners = Collections.synchronizedList(new ArrayList<UnitManagerListener>());
        equipmentNumberMap = new HashMap<String, Integer>();
        vehicleNumberMap = new HashMap<String, Integer>();
    }

    /**
     * Constructs initial units.
     * @throws Exception in unable to load names.
     */
    void constructInitialUnits() {

        // Initialize name lists
        initializePersonNames();
        initializeSettlementNames();
        initializeVehicleNames();

        // Create initial units.
        createInitialSettlements();
        createInitialVehicles();
        createInitialEquipment();
        createInitialResources();
        createInitialParts();
        createInitialPeople();
    }

    /**
     * Initializes the list of possible person names.
     * @throws Exception if unable to load name list.
     */
    private void initializePersonNames() {
        try {
            PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
            List<String> personNames = personConfig.getPersonNameList();
            personMaleNames = new ArrayList<String>();
            personFemaleNames = new ArrayList<String>();
            Iterator<String> i = personNames.iterator();
            while (i.hasNext()) {
                String name = i.next();
                String gender = personConfig.getPersonGender(name);
                if (gender.equals(Person.MALE)) {
                    personMaleNames.add(name);
                } else if (gender.equals(Person.FEMALE)) {
                    personFemaleNames.add(name);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("person names could not be loaded: " + e.getMessage(), e);
        }
    }

    /**
     * Initializes the list of possible vehicle names.
     * @throws Exception if unable to load rover names.
     */
    private void initializeVehicleNames() {
        try {
            VehicleConfig vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();
            vehicleNames = vehicleConfig.getRoverNameList();
        } catch (Exception e) {
            throw new IllegalStateException("rover names could not be loaded: " + e.getMessage(), e);
        }
    }

    /**
     * Initializes the list of possible settlement names.
     * @throws Exception if unable to load settlement names.
     */
    private void initializeSettlementNames() {
        try {
            SettlementConfig settlementConfig = SimulationConfig.instance().getSettlementConfiguration();
            settlementNames = settlementConfig.getSettlementNameList();
        } catch (Exception e) {
            throw new IllegalStateException("settlement names could not be loaded: " + e.getMessage(), e);
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
            Iterator<Unit> i = unit.getInventory().getContainedUnits().iterator();
            while (i.hasNext()) {
                addUnit(i.next());
            }

            // Fire unit manager event.
            fireUnitManagerUpdate(UnitManagerEvent.ADD_UNIT, unit);
        }
    }

    /**
     * Removes a unit from the unit manager.
     * @param unit the unit to remove.
     */
    public void removeUnit(Unit unit) {
        if (units.contains(unit)) {
            units.remove(unit);

            // Fire unit manager event.
            fireUnitManagerUpdate(UnitManagerEvent.REMOVE_UNIT, unit);
        }
    }

    /**
     * Gets a new name for a unit.
     * @param unitType the type of unit.
     * @param baseName the base name or null if none.
     * @param gender the gender of the person or null if not a person.
     * @return new name
     * @throws IllegalArgumentException if unitType is not valid.
     */
    public String getNewName(String unitType, String baseName, String gender) {

        List<String> initialNameList = null;
        List<String> usedNames = new ArrayList<String>();
        String unitName = "";

        if (unitType.equals(SETTLEMENT)) {
            initialNameList = settlementNames;
            Iterator<Settlement> si = getSettlements().iterator();
            while (si.hasNext()) {
                usedNames.add(si.next().getName());
            }
            unitName = "Settlement";
        } else if (unitType.equals(VEHICLE)) {
            if (baseName != null) {
                int number = 1;
                if (vehicleNumberMap.containsKey(baseName)) {
                    number += vehicleNumberMap.get(baseName);
                }
                vehicleNumberMap.put(baseName, number);
                return baseName + " " + number;
            } else {
                initialNameList = vehicleNames;
                Iterator<Vehicle> vi = getVehicles().iterator();
                while (vi.hasNext()) {
                    usedNames.add(vi.next().getName());
                }
                unitName = "Vehicle";
            }
        } else if (unitType.equals(PERSON)) {
            if (Person.MALE.equals(gender)) {
                initialNameList = personMaleNames;
            } else if (Person.FEMALE.equals(gender)) {
                initialNameList = personFemaleNames;
            } else {
                throw new IllegalArgumentException("Improper gender for person unitType: " + gender);
            }
            Iterator<Person> pi = getPeople().iterator();
            while (pi.hasNext()) {
                usedNames.add(pi.next().getName());
            }
            unitName = "Person";
        } else if (unitType.equals(EQUIPMENT)) {
            if (baseName != null) {
                int number = 1;
                if (equipmentNumberMap.containsKey(baseName)) {
                    number += equipmentNumberMap.get(baseName);
                }
                equipmentNumberMap.put(baseName, number);
                return baseName + " " + number;
            }
        } else {
            throw new IllegalArgumentException("Improper unitType");
        }

        List<String> remainingNames = new ArrayList<String>();
        Iterator<String> i = initialNameList.iterator();
        while (i.hasNext()) {
            String name = i.next();
            if (!usedNames.contains(name)) {
                remainingNames.add(name);
            }
        }

        String result = "";
        if (remainingNames.size() > 0) {
            result = remainingNames.get(
                    RandomUtil.getRandomInt(remainingNames.size() - 1));
        } else {
            result = unitName + " " + (usedNames.size() + 1);
        }

        return result;
    }

    /** 
     * Creates initial settlements 
     */
    private void createInitialSettlements() {

        SettlementConfig config = SimulationConfig.instance().getSettlementConfiguration();

        try {
            for (int x = 0; x < config.getNumberOfInitialSettlements(); x++) {
                // Get settlement name
                String name = config.getInitialSettlementName(x);
                if (name.equals(SettlementConfig.RANDOM)) {
                    name = getNewName(SETTLEMENT, null, null);
                }

                // Get settlement template
                String template = config.getInitialSettlementTemplate(x);

                // Get settlement longitude
                double longitude = 0D;
                String longitudeStr = config.getInitialSettlementLongitude(x);
                if (longitudeStr.equals(SettlementConfig.RANDOM)) {
                    longitude = Coordinates.getRandomLongitude();
                } else {
                    longitude = Coordinates.parseLongitude(longitudeStr);
                }

                // Get settlement latitude
                double latitude = 0D;
                String latitudeStr = config.getInitialSettlementLatitude(x);
                if (latitudeStr.equals(SettlementConfig.RANDOM)) {
                    latitude = Coordinates.getRandomLatitude();
                } else {
                    latitude = Coordinates.parseLatitude(latitudeStr);
                }

                Coordinates location = new Coordinates(latitude, longitude);
                
                int populationNumber = config.getInitialSettlementPopulationNumber(x);

                addUnit(new Settlement(name, template, location, populationNumber));
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new IllegalStateException("Settlements could not be created: " + e.getMessage(), e);
        }
    }

    /**
     * Creates initial vehicles based on settlement templates.
     * @throws Exception if vehicles could not be constructed.
     */
    private void createInitialVehicles() {

        SettlementConfig config = SimulationConfig.instance().getSettlementConfiguration();

        try {
            Iterator<Settlement> i = getSettlements().iterator();
            while (i.hasNext()) {
                Settlement settlement = i.next();
                SettlementTemplate template = config.getSettlementTemplate(settlement.getTemplate());
                Map<String, Integer> vehicleMap = template.getVehicles();
                Iterator<String> j = vehicleMap.keySet().iterator();
                while (j.hasNext()) {
                    String vehicleType = j.next();
                    int number = vehicleMap.get(vehicleType);

                    for (int x = 0; x < number; x++) {
                        if (LightUtilityVehicle.NAME.equals(vehicleType)) {
                            String name = getNewName(VEHICLE, "LUV", null);
                            addUnit(new LightUtilityVehicle(name, vehicleType, settlement));
                        } else {
                            String name = getNewName(VEHICLE, null, null);
                            addUnit(new Rover(name, vehicleType, settlement));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Vehicles could not be created: " + e.getMessage(), e);
        }
    }

    /**
     * Creates the initial equipment at a settlement.
     * @throws Exception if error constructing equipment.
     */
    private void createInitialEquipment() {

        SettlementConfig config = SimulationConfig.instance().getSettlementConfiguration();

        try {
            Iterator<Settlement> i = getSettlements().iterator();
            while (i.hasNext()) {
                Settlement settlement = i.next();
                SettlementTemplate template = config.getSettlementTemplate(settlement.getTemplate());
                Map<String, Integer> equipmentMap = template.getEquipment();
                Iterator<String> j = equipmentMap.keySet().iterator();
                while (j.hasNext()) {
                    String type = j.next();
                    int number = (Integer) equipmentMap.get(type);
                    for (int x = 0; x < number; x++) {
                        Equipment equipment = EquipmentFactory.getEquipment(type, settlement.getCoordinates(), false);
                        equipment.setName(getNewName(EQUIPMENT, type, null));
                        settlement.getInventory().storeUnit(equipment);
                        addUnit(equipment);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Equipment could not be created: " + e.getMessage(), e);
        }
    }

    /**
     * Creates the initial resources at a settlement.
     * Note: This is in addition to any initial resources set in buildings.
     * @throws Exception if error storing resources.
     */
    private void createInitialResources() {

        SettlementConfig config = SimulationConfig.instance().getSettlementConfiguration();

        try {
            Iterator<Settlement> i = getSettlements().iterator();
            while (i.hasNext()) {
                Settlement settlement = i.next();
                SettlementTemplate template = config.getSettlementTemplate(settlement.getTemplate());
                Map<AmountResource, Double> resourceMap = template.getResources();
                Iterator<AmountResource> j = resourceMap.keySet().iterator();
                while (j.hasNext()) {
                    AmountResource resource = j.next();
                    double amount = resourceMap.get(resource);
                    Inventory inv = settlement.getInventory();
                    double capacity = inv.getAmountResourceRemainingCapacity(resource, true, false);
                    if (amount > capacity) {
                        amount = capacity;
                    }                        
                    inv.storeAmountResource(resource, amount, true);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Equipment could not be created: " + e.getMessage(), e);
        }
    }

    /**
     * Create initial parts for a settlement.
     * @throws Exception if error creating parts.
     */
    private void createInitialParts() {
        SettlementConfig config = SimulationConfig.instance().getSettlementConfiguration();

        try {
            Iterator<Settlement> i = getSettlements().iterator();
            while (i.hasNext()) {
                Settlement settlement = i.next();
                SettlementTemplate template = config.getSettlementTemplate(settlement.getTemplate());
                Map<Part, Integer> partMap = template.getParts();
                Iterator<Part> j = partMap.keySet().iterator();
                while (j.hasNext()) {
                    Part part = j.next();
                    Integer number = partMap.get(part);
                    Inventory inv = settlement.getInventory();
                    inv.storeItemResources(part, number);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Part could not be created: " + e.getMessage(), e);
        }
    }

    /**
     * Creates initial people based on available capacity at settlements.
     * @throws Exception if people can not be constructed.
     */
    private void createInitialPeople() {

        // Create configured people.
        createConfiguredPeople();

        PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
        RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();

        // Randomly create all remaining people to fill the settlements to capacity.
        try {
            Iterator<Settlement> i = getSettlements().iterator();
            while (i.hasNext()) {
                Settlement settlement = i.next();

                while (settlement.getCurrentPopulationNum() < settlement.getInitialPopulation()) {
                	String gender = Person.FEMALE;
                    if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) {
                        gender = Person.MALE;
                    }
                    Person person = new Person(getNewName(PERSON, null, gender), gender, settlement);
                    addUnit(person);
                    relationshipManager.addInitialSettler(person, settlement);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new IllegalStateException("People could not be created: " + e.getMessage(), e);
        }
    }

    /**
     * Creates all configured people.
     * @throws Exception if error parsing XML.
     */
    private void createConfiguredPeople() {
        PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
        RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();

        // Create all configured people.
        for (int x = 0; x < personConfig.getNumberOfConfiguredPeople(); x++) {
        	// Get person's name (required)
        	String name = personConfig.getConfiguredPersonName(x);
        	if (name == null) {
        		throw new IllegalStateException("Person name is null");
        	}

        	// Get person's gender or randomly determine it if not configured.
        	String gender = personConfig.getConfiguredPersonGender(x);
        	if (gender == null) {
        		gender = Person.FEMALE;
        		if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) {
        			gender = Person.MALE;
        		}
        	}

        	// Get person's settlement or randomly determine it if not configured.
        	String settlementName = personConfig.getConfiguredPersonSettlement(x);
        	Settlement settlement = null;
        	if (settlementName != null) {
        		Collection<Settlement> col = CollectionUtils.getSettlement(units);
        		settlement = CollectionUtils.getSettlement(col, settlementName);
        		if (settlement == null) {
        			// If settlement cannot be found that matches the settlement name,
        			// put person in a randomly selected settlement.
        			logger.log(Level.WARNING, "Person " + name + " could not be located" +
        					" at " + settlementName + " because the settlement doesn't exist.");
        			settlement = CollectionUtils.getRandomSettlement(col);
        		}
        	} else {
        		Collection<Settlement> col = CollectionUtils.getSettlement(units);
        		settlement = CollectionUtils.getRandomSettlement(col);
        	}

        	// If settlement is still null (no settlements available),
        	// Don't create person.
        	if (settlement == null) {
        		return;
        	}

        	// If settlement does not have initial population capacity, try another settlement.
        	if (settlement.getInitialPopulation() <= settlement.getCurrentPopulationNum()) {
        		Iterator<Settlement> i = getSettlements().iterator();
        		Settlement newSettlement = null;
        		while (i.hasNext() && (newSettlement == null)) {
        			Settlement tempSettlement = i.next();
        			if (tempSettlement.getInitialPopulation() > tempSettlement.getCurrentPopulationNum()) {
        				newSettlement = tempSettlement;
        			}
        		}
        		if (newSettlement != null) {
        			settlement = newSettlement;
        		}
        		else {
        			// If no settlement with room found, don't create person.
        			return;
        		}
        	}

        	// Create person and add to the unit manager.
        	Person person = new Person(name, gender, settlement);
        	addUnit(person);
        	relationshipManager.addInitialSettler(person, settlement);

        	// Set person's configured personality type (if any).
        	String personalityType = personConfig.getConfiguredPersonPersonalityType(x);
        	if (personalityType != null) {
        		person.getMind().getPersonalityType().setTypeString(personalityType);
        	}

        	// Set person's job (if any).
        	String jobName = personConfig.getConfiguredPersonJob(x);
        	if (jobName != null) {
        		Job job = JobManager.getJob(jobName);
        		person.getMind().setJob(job, true);
        	}

        	// Set person's configured natural attributes (if any).
        	Map<String, Integer> naturalAttributeMap = personConfig.getNaturalAttributeMap(x);
        	if (naturalAttributeMap != null) {
        		Iterator<String> i = naturalAttributeMap.keySet().iterator();
        		while (i.hasNext()) {
        			String attributeName = i.next();
        			int value = (Integer) naturalAttributeMap.get(attributeName);
        			person.getNaturalAttributeManager().setAttribute(attributeName, value);
        		}
        	}

        	// Set person's configured skills (if any).
        	Map<String, Integer> skillMap = personConfig.getSkillMap(x);
        	if (skillMap != null) {
        		Iterator<String> i = skillMap.keySet().iterator();
        		while (i.hasNext()) {
        			String skillName = i.next();
        			int level = (Integer) skillMap.get(skillName);
        			person.getMind().getSkillManager().addNewSkill(new Skill(skillName, level));
        		}
        	}
        }

        // Create all configured relationships.
        createConfiguredRelationships();
    }

    /**
     * Creates all configured people relationships.
     * @throws Exception if error parsing XML.
     */
    private void createConfiguredRelationships() {
        PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
        RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();

        // Create all configured people relationships.
        for (int x = 0; x < personConfig.getNumberOfConfiguredPeople(); x++) {
            try {
                // Get person's name
                String name = personConfig.getConfiguredPersonName(x);
                if (name == null) {
                    throw new IllegalStateException("Person name is null");
                }

                // Get the person
                Person person = null;
                Iterator<Person> j = getPeople().iterator();
                while (j.hasNext()) {
                    Person tempPerson = j.next();
                    if (tempPerson.getName().equals(name)) {
                        person = tempPerson;
                    }
                }
                if (person == null) {
                    throw new IllegalStateException("Person: " + name + " not found.");
                }

                // Set person's configured relationships (if any).
                Map<String, Integer> relationshipMap = personConfig.getRelationshipMap(x);
                if (relationshipMap != null) {
                    Iterator<String> i = relationshipMap.keySet().iterator();
                    while (i.hasNext()) {
                        String relationshipName = i.next();

                        // Get the other person in the relationship.
                        Person relationshipPerson = null;
                        Iterator<Person> k = getPeople().iterator();
                        while (k.hasNext()) {
                            Person tempPerson = k.next();
                            if (tempPerson.getName().equals(relationshipName)) {
                                relationshipPerson = tempPerson;
                            }
                        }
                        if (relationshipPerson == null) {
                            throw new IllegalStateException("Person: " + relationshipName + " not found.");
                        }

                        int opinion = (Integer) relationshipMap.get(relationshipName);

                        // Set the relationship opinion.
                        Relationship relationship = relationshipManager.getRelationship(person, relationshipPerson);
                        if (relationship != null) {
                            relationship.setPersonOpinion(person, opinion);
                        } else {
                            relationshipManager.addRelationship(person, relationshipPerson, Relationship.EXISTING_RELATIONSHIP);
                            relationship = relationshipManager.getRelationship(person, relationshipPerson);
                            relationship.setPersonOpinion(person, opinion);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
                logger.log(Level.SEVERE, "Configured relationship could not be created: " + e.getMessage());
            }
        }
    }

    /** 
     * Notify all the units that time has passed.
     * Times they are a changing.
     * @param time the amount time passing (in millisols)
     * @throws Exception if error during time passing.
     */
    void timePassing(double time) {
        Iterator<Unit> i = units.iterator();
        while (i.hasNext()) {
            i.next().timePassing(time);
        }
    }

    /** Get number of settlements
     *  @return the number of settlements
     */
    public int getSettlementNum() {
        return CollectionUtils.getSettlement(units).size();
    }

    /** Get settlements in vitual Mars
     *  @return Collection of settlements
     */
    public Collection<Settlement> getSettlements() {
        return CollectionUtils.getSettlement(units);
    }

    /** Get number of vehicles
     *  @return the number of vehicles
     */
    public int getVehicleNum() {
        return CollectionUtils.getVehicle(units).size();
    }

    /** Get vehicles in virtual Mars
     *  @return Collection of vehicles
     */
    public Collection<Vehicle> getVehicles() {
        return CollectionUtils.getVehicle(units);
    }

    /** Get number of people
     *  @return the number of people
     */
    public int getPeopleNum() {
        return CollectionUtils.getPerson(units).size();
    }

    /** Get people in virtual Mars
     *  @return Collection of people
     */
    public Collection<Person> getPeople() {
        return CollectionUtils.getPerson(units);
    }

    /**
     * Get the number of equipment.
     * @return number
     */
    public int getEquipmentNum() {
        return CollectionUtils.getEquipment(units).size();
    }

    /**
     * Get a collection of equipment.
     * @return collection
     */
    public Collection<Equipment> getEquipment() {
        return CollectionUtils.getEquipment(units);
    }

    /** The total number of units
     *  @return the total number of units
     */
    public int getUnitNum() {
        return units.size();
    }

    /** Get all units in virtual Mars
     *  @return Colleciton of units
     */
    public Collection<Unit> getUnits() {
        return units;
    }

    /**
     * Adds a unit manager listener
     * @param newListener the listener to add.
     */
    public final void addUnitManagerListener(UnitManagerListener newListener) {
        if (listeners == null) {
            listeners = Collections.synchronizedList(new ArrayList<UnitManagerListener>());
        }
        if (!listeners.contains(newListener)) {
            listeners.add(newListener);
        }
    }

    /**
     * Removes a unit manager listener
     * @param oldListener the listener to remove.
     */
    public final void removeUnitManagerListener(UnitManagerListener oldListener) {
        if (listeners == null) {
            listeners = Collections.synchronizedList(new ArrayList<UnitManagerListener>());
        }
        if (listeners.contains(oldListener)) {
            listeners.remove(oldListener);
        }
    }

    /**
     * Fire a unit update event.
     * @param eventType the event type.
     * @param unit the unit causing the event.
     */
    public final void fireUnitManagerUpdate(String eventType, Unit unit) {
        if (listeners == null) {
            listeners = Collections.synchronizedList(new ArrayList<UnitManagerListener>());
        }
        synchronized (listeners) {
            Iterator<UnitManagerListener> i = listeners.iterator();
            while (i.hasNext()) {
                i.next().unitManagerUpdate(new UnitManagerEvent(this, eventType, unit));
            }
        }
    }

    /**
     * Finds a unit in the simulation that has the given name.
     * @param name the name to search for.
     * @return unit or null if none.
     */
    public Unit findUnit(String name) {
        Unit result = null;
        Iterator<Unit> i = units.iterator();
        while (i.hasNext() && (result == null)) {
            Unit unit = i.next();
            if (unit.getName().equals(name)) {
                result = unit;
            }
        }
        return result;
    }
    
    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        Iterator<Unit> i = units.iterator();
        while (i.hasNext()) {
            i.next().destroy();
        }
        units.clear();
        units = null;
        
        settlementNames.clear();
        settlementNames = null;
        vehicleNames.clear();
        vehicleNames = null;
        personMaleNames.clear();
        personMaleNames = null;
        personFemaleNames.clear();
        personFemaleNames = null;
        listeners.clear();
        listeners = null;
        equipmentNumberMap.clear();
        equipmentNumberMap = null;
        vehicleNumberMap.clear();
        vehicleNumberMap = null;
    }
}