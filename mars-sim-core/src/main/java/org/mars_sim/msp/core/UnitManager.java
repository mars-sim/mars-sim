/**
 * Mars Simulation Project
 * UnitManager.java
 * @version 3.1.0 2017-09-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.Favorite;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PersonalityTraitType;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotConfig;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleConfig;

/**
 * The UnitManager class contains and manages all units in virtual Mars. It has
 * methods for getting information about units. It is also responsible for
 * creating all units on its construction. There should be only one instance of
 * this class and it should be constructed and owned by Simulation.
 */
public class UnitManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(UnitManager.class.getName());

	public static final int THREE_SHIFTS_MIN_POPULATION = 6;

	public static final String ONE_SPACE = "0";
	public static final String TWO_SPACES = "00";
	public static final String THREE_SPACES = "000";
	
	public static final String PERSON_NAME = "Person";
	public static final String VEHICLE_NAME = "Vehicle";
	public static final String SETTLEMENT_NAME = "Settlement";
	
	/** True if the simulation has just started. */
	private static boolean justStarting = true;
	/** The total numbers of Unit instances. */
	private static int totalNumUnits = 0;
	/** The instance of MarsSurface. */
	private static MarsSurface marsSurface;
	
	// Data members
	/** Is it running in Commander's Mode */
	public boolean isCommanderMode;	
	/** The commander's unique id . */
    public int commanderID;
	/** The cache of the mission sol. */    
	private int solCache = 0;
	/** The core engine's original build. */
	public String originalBuild;
	
	/** A map of all units with its unit identifier. */
	private volatile Map<Integer, Unit> lookupUnit = new HashMap<>();
	/** A map of settlements with its unit identifier. */
	private volatile Map<Integer, Settlement> lookupSettlement = new HashMap<>();
	/** A map of persons with its unit identifier. */
	private volatile Map<Integer, Person> lookupPerson = new HashMap<>();
	/** A map of robots with its unit identifier. */
	private volatile Map<Integer, Robot> lookupRobot = new HashMap<>();
	/** A map of vehicle with its unit identifier. */
	private volatile Map<Integer, Vehicle> lookupVehicle = new HashMap<>();
	/** A map of other equipment (excluding robots and vehicles) with its unit identifier. */
	private volatile Map<Integer, Equipment> lookupEquipment = new HashMap<>();
	
	// Transient members
	/** Flag true if the class has just been loaded */
	public static boolean justLoaded = true;
	/** Flag true if the class has just been reloaded/deserialized */
	public static boolean justReloaded = false;
	/** List of unit manager listeners. */
	private static List<UnitManagerListener> listeners;

	// Static members
	/** A list of all units. */
//	private static List<Unit> units;
	/** List of possible settlement names. */
	private static volatile List<String> settlementNames;
	/** List of possible vehicle names. */
	private static volatile List<String> vehicleNames;
	/** List of possible male person names. */
	private static volatile List<String> personMaleNames;
	/** List of possible female person names. */
	private static volatile List<String> personFemaleNames;
	/** List of possible robot names. */
	private static volatile List<String> robotNameList;

	/** Map of equipment types and their numbers. */
	private static volatile Map<String, Integer> equipmentNumberMap;
	/** Map of vehicle types and their numbers. */
	private static volatile Map<String, Integer> vehicleNumberMap;

	private static Map<Integer, List<String>> marsSociety = new HashMap<>();

	private static Map<Integer, List<String>> maleFirstNamesBySponsor = new HashMap<>();
	private static Map<Integer, List<String>> femaleFirstNamesBySponsor = new HashMap<>();

	private static Map<Integer, List<String>> maleFirstNamesByCountry = new HashMap<>();
	private static Map<Integer, List<String>> femaleFirstNamesByCountry = new HashMap<>();

	private static Map<Integer, List<String>> lastNamesBySponsor = new HashMap<>();
	private static Map<Integer, List<String>> lastNamesByCountry = new HashMap<>();

	private static List<String> countries;

	private static SimulationConfig simulationConfig = SimulationConfig.instance();
	private static Simulation sim = Simulation.instance();
	
	private static PersonConfig personConfig;
	private static SettlementConfig settlementConfig;
	private static VehicleConfig vehicleConfig;
	private static RobotConfig robotConfig;
//	private static PartConfig partConfig;

	private static RelationshipManager relationshipManager;
	private static MalfunctionFactory factory;
	
	private static MarsClock marsClock;

	/**
	 * Constructor.
	 */
	public UnitManager() {
		marsClock = sim.getMasterClock().getMarsClock();

		// Initialize unit collection
		lookupUnit = new HashMap<>();
		lookupSettlement = new HashMap<>();
		lookupPerson = new HashMap<>();
		lookupRobot = new HashMap<>();
		lookupEquipment = new HashMap<>();
		lookupVehicle = new HashMap<>();
//		units = new CopyOnWriteArrayList<>();//ConcurrentLinkedQueue<Unit>();
		listeners = Collections.synchronizedList(new ArrayList<UnitManagerListener>());
		equipmentNumberMap = new HashMap<String, Integer>();
		vehicleNumberMap = new HashMap<String, Integer>();
		
//		partConfig = simulationConfig.getPartConfiguration();
		personConfig = simulationConfig.getPersonConfiguration();
		robotConfig = simulationConfig.getRobotConfiguration();
		settlementConfig = simulationConfig.getSettlementConfiguration();
		vehicleConfig = simulationConfig.getVehicleConfiguration();
		
		relationshipManager = sim.getRelationshipManager();
		factory = sim.getMalfunctionFactory();		

		// Add mars surface
		marsSurface = sim.getMars().getMarsSurface();

		marsClock = sim.getMasterClock().getMarsClock();

	}

	/**
	 * Constructs initial units.
	 *
	 * @throws Exception in unable to load names.
	 */
	synchronized void constructInitialUnits(boolean loadSaveSim) {
		// Add marsSurface as the very first unit
		addUnit(marsSurface);
		
		if (countries == null)
			countries = personConfig.createCountryList();

		// Initialize name lists
		initializeRobotNames();
		initializePersonNames();
		initializeLastNames();
		initializeFirstNames();
		
		// Initialize settlement and vehicle name lists
		initializeSettlementNames();
		initializeVehicleNames();

		if (!loadSaveSim) {
			// Create initial units.
			createInitialSettlements();
			createInitialVehicles();
			createInitialEquipment();
			createInitialResources();
			createInitialParts();
			// Find the settlement match for the user proposed commander's sponsor 
			if (GameManager.mode == GameMode.COMMAND)
				matchSettlement();
			// Create pre-configured robots as stated in robots.xml
			createPreconfiguredRobots();
			// Create more robots to fill the settlement(s)
			createInitialRobots();
			// Create pre-configured settlers as stated in people.xml
			createPreconfiguredPeople();
			// Create more settlers to fill the settlement(s)
			createInitialPeople();
			// Manually add job positions
			tuneJobDeficit();
		}

		justStarting = false;
	}

	/**
	 * Initializes the list of possible person names.
	 * 
	 * @throws Exception if unable to load name list.
	 */
	private void initializePersonNames() {
		try {
			List<String> personNames = personConfig.getPersonNameList();

			personMaleNames = new ArrayList<String>();
			personFemaleNames = new ArrayList<String>();

			Iterator<String> i = personNames.iterator();

			while (i.hasNext()) {

				String name = i.next();
				GenderType gender = personConfig.getPersonGender(name);
				if (gender == GenderType.MALE) {
					personMaleNames.add(name);
				} else if (gender == GenderType.FEMALE) {
					personFemaleNames.add(name);
				}

				marsSociety.put(0, personMaleNames);
				marsSociety.put(1, personFemaleNames);
			}

		} catch (Exception e) {
			throw new IllegalStateException("The person name list could not be loaded: " + e.getMessage(), e);
		}
	}

	/**
	 * Initializes a list of last names according for each space agency.
	 * 
	 * @throws Exception if unable to load the last name list.
	 */
	private void initializeLastNames() {
		try {
			List<Map<Integer, List<String>>> lastNames = personConfig.retrieveLastNameList();
			lastNamesBySponsor = lastNames.get(0);
			lastNamesByCountry = lastNames.get(1);

		} catch (Exception e) {
			throw new IllegalStateException("The last names list could not be loaded: " + e.getMessage(), e);
		}
	}

	/**
	 * Initializes a list of first names according for each space agency.
	 * 
	 * @throws Exception if unable to load the first name list.
	 */
	private void initializeFirstNames() {

		try {
			List<Map<Integer, List<String>>> firstNames = personConfig.retrieveFirstNameList();
			maleFirstNamesBySponsor = firstNames.get(0);
			femaleFirstNamesBySponsor = firstNames.get(1);
			maleFirstNamesByCountry = firstNames.get(2);
			femaleFirstNamesByCountry = firstNames.get(3);

		} catch (Exception e) {
			throw new IllegalStateException("The first names list could not be loaded: " + e.getMessage(), e);
		}
	}

	/**
	 * Initializes the list of possible robot names.
	 * 
	 * @throws Exception if unable to load name list.
	 */
	private void initializeRobotNames() {
		try {
			robotNameList = new ArrayList<String>();
			// robotNameList.add("ChefBot 001");
			// robotNameList.add("GardenBot 002");
			// robotNameList.add("RepairBot 003");

		} catch (Exception e) {
			throw new IllegalStateException("robot names could not be loaded: " + e.getMessage(), e);
		}
	}

	/**
	 * Initializes the list of possible vehicle names.
	 *
	 * @throws Exception if unable to load rover names.
	 */
	private void initializeVehicleNames() {
		try {
			vehicleNames = vehicleConfig.getRoverNameList();
//			System.out.println(vehicleNames);
		} catch (Exception e) {
			throw new IllegalStateException("rover names could not be loaded: " + e.getMessage(), e);
		}
	}

	/**
	 * Initializes the list of possible settlement names.
	 *
	 * @throws Exception if unable to load settlement names.
	 */
	private void initializeSettlementNames() {
		try {
			settlementNames = settlementConfig.getSettlementNameList();
		} catch (Exception e) {
			throw new IllegalStateException("settlement names could not be loaded: " + e.getMessage(), e);
		}
	}
	
	public Unit getUnitByID(int id) {
		return lookupUnit.get(id);
	}
	
	public void addUnitID(Unit unit) {
		if (unit != null && !lookupUnit.containsKey(unit.getIdentifier()))
			lookupUnit.put(unit.getIdentifier(), unit);
	}

	public void removeUnitID(Unit unit) {
		if (lookupUnit.containsKey(unit.getIdentifier()))
			lookupUnit.remove(unit.getIdentifier());
	}
	
	public Settlement getSettlementByID(int id) {
//		System.out.println("Getting " + lookupSettlement.get(id) + " (" + id + ")");
		return lookupSettlement.get(id);
	}
	
	public void addSettlementID(Settlement s) {
		if (lookupSettlement == null)
			lookupSettlement = new HashMap<>();
		if (s != null && !lookupSettlement.containsKey(s.getIdentifier())) {
//			System.out.println("Adding " + s + " (" + s.getIdentifier() + ") size : " + lookupSettlement.size());
			lookupSettlement.put(s.getIdentifier(), s);
		}
	}

	public void removeSettlementID(Settlement s) {
		if (!lookupSettlement.containsKey(s.getIdentifier()))
			lookupSettlement.remove(s.getIdentifier());
	}

	public Person getPersonByID(int id) {
		return lookupPerson.get(id);
	}

	public Settlement getCommanderSettlement() {
		return getPersonByID(commanderID).getAssociatedSettlement();
	}
	
	/**
	 * Gets the settlement list including the commander's associated settlement
	 * and the settlement that he's at or in the vicinity of
	 * 
	 * @return {@link List<Settlement>}
	 */
	public List<Settlement> getCommanderSettlements() {
		List<Settlement> settlements = new ArrayList<Settlement>();
		
		Person cc = getPersonByID(commanderID);
		// Add the commander's associated settlement
		Settlement as = cc.getAssociatedSettlement();
		settlements.add(as);
		
		// Find the settlement the commander is at
		Settlement s = cc.getSettlement();
		// If the commander is in the vicinity of a settlement
		if (s == null)
			s = CollectionUtils.findSettlement(cc.getCoordinates());
		if (s != null && as != s)
			settlements.add(s);
		
		return settlements;
	}
	
	public void addPersonID(Person p) {
		if (lookupPerson == null)
			lookupPerson = new HashMap<>();
		if (p != null && !lookupPerson.containsKey(p.getIdentifier()))
			lookupPerson.put(p.getIdentifier(), p);
	}

	public void removePersonID(Person p) {
		if (lookupPerson.containsKey(p.getIdentifier()))
			lookupPerson.remove(p.getIdentifier());
	}

	public Robot getRobotByID(int id) {
		return lookupRobot.get(id);
	}

	public void addRobotID(Robot r) {
		if (lookupRobot == null)
			lookupRobot = new HashMap<>();
		if (r != null && !lookupRobot.containsKey(r.getIdentifier()))
			lookupRobot.put(r.getIdentifier(), r);
	}

	public void removeRobotID(Robot r) {
		if (lookupRobot.containsKey(r.getIdentifier()))
			lookupRobot.remove(r.getIdentifier());
	}
	
	public Equipment getEquipmentByID(int id) {
		return lookupEquipment.get(id);
	}

	public void addEquipmentID(Equipment e) {
		if (lookupEquipment == null)
			lookupEquipment = new HashMap<>();
		if (e != null && !lookupEquipment.containsKey(e.getIdentifier()))
			lookupEquipment.put(e.getIdentifier(), e);
	}

	public void removeEquipmentID(Equipment e) {
		if (lookupEquipment.containsKey(e.getIdentifier()))
			lookupEquipment.remove(e.getIdentifier());
	}

	public Vehicle getVehicleByID(int id) {
		return lookupVehicle.get(id);
	}

	public void addVehicleID(Vehicle v) {
		if (lookupVehicle == null)
			lookupVehicle = new HashMap<>();
		if (v != null && !lookupVehicle.containsKey(v.getIdentifier()))
			lookupVehicle.put(v.getIdentifier(), v);
	}

	public void removeVehicleID(Vehicle v) {
		if (lookupVehicle.containsKey(v.getIdentifier()))
			lookupVehicle.remove(v.getIdentifier());
	}
	
	/**
	 * Adds a unit to the unit manager if it doesn't already have it.
	 *
	 * @param unit new unit to add.
	 */
	public void addUnit(Unit unit) {
//		if (!units.contains(unit)) {
//			units.add(unit);
			
			// Add the unit's id into its lookup maps
			if (unit instanceof Settlement)
				addSettlementID((Settlement)unit);
			else if (unit instanceof Person)
				addPersonID((Person)unit);
			else if (unit instanceof Robot)
				addRobotID((Robot)unit);
			else if (unit instanceof Vehicle)
				addVehicleID((Vehicle)unit);
			else if (unit instanceof Equipment)
				addEquipmentID((Equipment)unit);
			else if (unit instanceof MarsSurface)
				marsSurface = (MarsSurface) unit;
			else 
				addUnitID(unit);

			if (!justStarting) {
				computeUnitNum();
				computeUnits();
			}
			
			Iterator<Unit> i = unit.getInventory().getContainedUnits().iterator();
			while (i.hasNext()) {
				addUnit(i.next());
			}
			// Fire unit manager event.
			fireUnitManagerUpdate(UnitManagerEventType.ADD_UNIT, unit);
//		}
	}

	/**
	 * Removes a unit from the unit manager.
	 *
	 * @param unit the unit to remove.
	 */
	public void removeUnit(Unit unit) {
//		if (units.contains(unit)) {
//			units.remove(unit);

			// Add the unit's id into its lookup maps
			if (unit instanceof Settlement)
				removeSettlementID((Settlement)unit);
			else if (unit instanceof Person)
				removePersonID((Person)unit);
			else if (unit instanceof Robot)
				removeRobotID((Robot)unit);
			else if (unit instanceof Vehicle)
				removeVehicleID((Vehicle)unit);
			else if (unit instanceof Equipment)
				removeEquipmentID((Equipment)unit);
			else 
				removeUnitID(unit);
			
			if (!justStarting) {
				computeUnitNum();
				computeUnits();
			}

			// Fire unit manager event.
			fireUnitManagerUpdate(UnitManagerEventType.REMOVE_UNIT, unit);
//		}
	}

	/**
	 * Gets a new name for a unit.
	 * 
	 * @param unitType {@link UnitType} the type of unit.
	 * @param baseName the base name or null if none.
	 * @param gender   the gender of the person or null if not a person.
	 * @return new name
	 * @throws IllegalArgumentException if unitType is not valid.
	 */
	public String getNewName(UnitType unitType, String baseName, GenderType gender, RobotType robotType) {

		List<String> initialNameList = null;
		List<String> usedNames = new ArrayList<String>();
		String unitName = "";

		if (unitType == UnitType.SETTLEMENT) {
			initialNameList = settlementNames;
			Iterator<Settlement> si = getSettlements().iterator();
			while (si.hasNext()) {
				usedNames.add(si.next().getName());
			}
			unitName = SETTLEMENT_NAME;

		} else if (unitType == UnitType.VEHICLE) {
			if (baseName != null) {
				// for LUVs 
				String tagID = "";
				int number = 1;
				if (vehicleNumberMap.containsKey(baseName)) {
					number += vehicleNumberMap.get(baseName);
				}
				if (number < 10)
					tagID = TWO_SPACES + number;
				else if (number < 100)
					tagID = ONE_SPACE + number;
				else if (number < 1000)
					tagID = "" + number;
				else
					tagID = "" + number;
				vehicleNumberMap.put(baseName, number);
				return baseName + " " + tagID;

			} else {
				initialNameList = vehicleNames;
//				System.out.println(initialNameList);
				Iterator<Vehicle> vi = getVehicles().iterator();
				while (vi.hasNext()) {
					usedNames.add(vi.next().getName());
				}
				unitName = VEHICLE_NAME;
			}

		} else if (unitType == UnitType.PERSON) {
			if (GenderType.MALE == gender) {
				initialNameList = personMaleNames;
			} else if (GenderType.FEMALE == gender) {
				initialNameList = personFemaleNames;
			} else {
				throw new IllegalArgumentException("Improper gender for person unitType: " + gender);
			}
			Iterator<Person> pi = getPeople().iterator();
			while (pi.hasNext()) {
				usedNames.add(pi.next().getName());
			}
			unitName = PERSON_NAME;

		} else if (unitType == UnitType.ROBOT) {

			initialNameList = robotNameList;

			Iterator<Robot> ri = getRobots().iterator();
			while (ri.hasNext()) {
				usedNames.add(ri.next().getName());
			}

			unitName = robotType.getName();

		} else if (unitType == UnitType.EQUIPMENT) {
			if (baseName != null) {
				String tagID = "";
				int number = 1;
				if (equipmentNumberMap.containsKey(baseName)) {
					number += equipmentNumberMap.get(baseName);
				}
				if (number < 10)
					tagID = TWO_SPACES + number;
				else if (number < 100)
					tagID = ONE_SPACE + number;
				else if (number < 1000)
					tagID = "" + number;
				else
					tagID = "" + number;
				equipmentNumberMap.put(baseName, number);
				return baseName + " " + tagID;
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
			result = remainingNames.get(RandomUtil.getRandomInt(remainingNames.size() - 1));
		} else {
			int number = usedNames.size() + 1;
			String tagID = "";
			if (number < 10)
				tagID = TWO_SPACES + number;
			else if (number < 100)
				tagID = ONE_SPACE + number;
			else if (number < 1000)
				tagID = "" + number;
			else
				tagID = "" + number;
			result = unitName + " " + tagID;

		}

		return result;
	}

	/**
	 * Creates initial settlements
	 */
	private void createInitialSettlements() {
		int size = settlementConfig.getNumberOfInitialSettlements();
		try {
			for (int x = 0; x < size; x++) {
				// Get settlement name
				String name = settlementConfig.getInitialSettlementName(x);
				if (name.equals(SettlementConfig.RANDOM)) {
					name = getNewName(UnitType.SETTLEMENT, null, null, null);
				}

				// Get settlement template
				String template = settlementConfig.getInitialSettlementTemplate(x);
				String sponsor = settlementConfig.getInitialSettlementSponsor(x);

				// Get settlement longitude
				double longitude = 0D;
				String longitudeStr = settlementConfig.getInitialSettlementLongitude(x);
				if (longitudeStr.equals(SettlementConfig.RANDOM)) {
					longitude = Coordinates.getRandomLongitude();
				} else {
					longitude = Coordinates.parseLongitude(longitudeStr);
				}

				// Get settlement latitude
				double latitude = 0D;
				String latitudeStr = settlementConfig.getInitialSettlementLatitude(x);
				
				if (latitudeStr.equals(SettlementConfig.RANDOM)) {
					latitude = Coordinates.getRandomLatitude();
				} else {
					latitude = Coordinates.parseLatitude(latitudeStr);
				}

				Coordinates location = new Coordinates(latitude, longitude);

				int populationNumber = settlementConfig.getInitialSettlementPopulationNumber(x);
				int initialNumOfRobots = settlementConfig.getInitialSettlementNumOfRobots(x);
				// Add settlement's id called sid
				// Add scenarioID
				int scenarioID = settlementConfig.getInitialSettlementScenarioID(x);
				
				Settlement settlement = Settlement.createNewSettlement(name, scenarioID, template, sponsor, location, populationNumber,
						initialNumOfRobots);
				settlement.initialize();
				addUnit(settlement);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new IllegalStateException("Settlements could not be created: " + e.getMessage(), e);
		}

//		firstSettlement = getASettlement();
	}

	/**
	 * Creates initial vehicles based on settlement templates.
	 *
	 * @throws Exception if vehicles could not be constructed.
	 */
	private void createInitialVehicles() {

		try {
			for (Settlement settlement : getSettlements()) {
				SettlementTemplate template = settlementConfig.getSettlementTemplate(settlement.getTemplate());
				Map<String, Integer> vehicleMap = template.getVehicles();
				Iterator<String> j = vehicleMap.keySet().iterator();
				while (j.hasNext()) {
					String vehicleType = j.next();
					int number = vehicleMap.get(vehicleType);
					vehicleType = vehicleType.toLowerCase();
					for (int x = 0; x < number; x++) {
						if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType)) {
							String name = getNewName(UnitType.VEHICLE, "LUV", null, null);
							addUnit(new LightUtilityVehicle(name, vehicleType, settlement));
						} else {
							String name = getNewName(UnitType.VEHICLE, null, null, null);
							addUnit(new Rover(name, vehicleType, settlement));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// throw new IllegalStateException("Vehicles could not be created: " +
			// e.getMessage(), e);
		}
	}

	/**
	 * Creates the initial equipment at a settlement.
	 *
	 * @throws Exception if error constructing equipment.
	 */
	private void createInitialEquipment() {

		try {
			for (Settlement settlement : getSettlements()) {
				SettlementTemplate template = settlementConfig.getSettlementTemplate(settlement.getTemplate());
				Map<String, Integer> equipmentMap = template.getEquipment();
				for (String type : equipmentMap.keySet()) {
					int number = (Integer) equipmentMap.get(type);
					for (int x = 0; x < number; x++) {
						Equipment equipment = EquipmentFactory.createEquipment(type, settlement.getCoordinates(),
								false);
						equipment.setName(getNewName(UnitType.EQUIPMENT, type, null, null));
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
	 * Creates the initial resources at a settlement. Note: This is in addition to
	 * any initial resources set in buildings.
	 *
	 * @throws Exception if error storing resources.
	 */
	private void createInitialResources() {

		try {
			Iterator<Settlement> i = getSettlements().iterator();
			while (i.hasNext()) {
				Settlement settlement = i.next();
				SettlementTemplate template = settlementConfig.getSettlementTemplate(settlement.getTemplate());
				Map<AmountResource, Double> resourceMap = template.getResources();
				Iterator<AmountResource> j = resourceMap.keySet().iterator();
				while (j.hasNext()) {
					AmountResource resource = j.next();
					double amount = resourceMap.get(resource);
					Inventory inv = settlement.getInventory();
					double capacity = inv.getAmountResourceRemainingCapacity(resource, true, false);
					if (amount > capacity)
						amount = capacity;
					inv.storeAmountResource(resource, amount, true);
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("Resource could not be created: " + e.getMessage(), e);
		}
	}

	/**
	 * Create initial parts for a settlement.
	 *
	 * @throws Exception if error creating parts.
	 */
	private void createInitialParts() {

		try {
			Iterator<Settlement> i = getSettlements().iterator();
			while (i.hasNext()) {
				Settlement settlement = i.next();
				SettlementTemplate template = settlementConfig.getSettlementTemplate(settlement.getTemplate());
				Map<Part, Integer> partMap = template.getParts();
				Iterator<Part> j = partMap.keySet().iterator();
				while (j.hasNext()) {
					Part part = j.next();
					Integer number = partMap.get(part);
					Inventory inv = settlement.getInventory();
					inv.storeItemResources(part.getID(), number);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// throw new IllegalStateException("Parts could not be created: " +
			// e.getMessage(), e);
		}
	}

	/**
	 * Creates all pre-configured people as listed in people.xml.
	 * 
	 * @throws Exception if error parsing XML.
	 */
	private void createPreconfiguredPeople() {
		Settlement settlement = null;

		List<Person> personList = new ArrayList<>();
		
		if (personConfig == null) // FOR PASSING MAVEN TEST
			personConfig = SimulationConfig.instance().getPersonConfiguration();

		// TODO: will setting a limit on # crew to 7 be easier ?

		int size = personConfig.getNumberOfConfiguredPeople();

		// Create all configured people.
		for (int x = 0; x < size; x++) {

			// Get person's name (required)
			int crew_id = personConfig.getCrew(x);

			// Get person's name (required)
			String name = personConfig.getConfiguredPersonName(x, crew_id);

			// Get person's gender or randomly determine it if not configured.
			GenderType gender = personConfig.getConfiguredPersonGender(x, crew_id);
			if (gender == null) {
				gender = GenderType.FEMALE;
				if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) {
					gender = GenderType.MALE;
				}
			}
			
//			if (name == null
//				throw new IllegalStateException("Person name is null");
			
			boolean invalid = false;
			// Prevent mars-sim from using the user defined commander's name  
			if (name == "" || name == null) {
				logger.severe("A person's name is invalid in alpha crew list or in people.xml");
				invalid = true;
			}
				
			if (getFullname() != null && name.equals(getFullname())) {
				logger.severe("A person's name in people.xml collides with the user defined commander's name ");
				invalid = true;
			}
			
			if (invalid) {
				boolean isUnique = false;
				
				String oldName = name;
				
				while (!isUnique) {
					int num = 0;

					if (gender == GenderType.FEMALE) {
						num = 1;
					}
					
					List<String> list = marsSociety.get(num);
					name = list.get(RandomUtil.getRandomInt(list.size()-1));	
					
					if (name.equals(getFullname())) {
						isUnique = false;						
					}
					else {
						logger.config("'" + name + "' has been selected to replace '" + oldName + "' found in alpha crew list or in people.xml. ");
						isUnique = true;
					}

				}

			}

			// Get person's settlement or randomly determine it if not configured.
			String preConfigSettlementName = personConfig.getConfiguredPersonDestination(x, crew_id);
			if (preConfigSettlementName != null) {
				Collection<Settlement> col = getSettlements();//lookupSettlement.values();//CollectionUtils.getSettlement(units);
				settlement = CollectionUtils.getSettlement(col, preConfigSettlementName);
				if (settlement == null) {
//					System.out.println("settlement : " + settlement);
					// TODO: If settlement cannot be found that matches the settlement name,
					// should we put the person in a randomly selected settlement?
					settlement = CollectionUtils.getRandomSettlement(col);
					logger.log(Level.INFO, name + " is being sent to " + settlement + " since "
							+ preConfigSettlementName + " doesn't exist.");
				}

			} else {
				Collection<Settlement> col = lookupSettlement.values();//CollectionUtils.getSettlement(units);
				settlement = CollectionUtils.getRandomSettlement(col);
				logger.log(Level.INFO, name + " has no destination settlement specified and goes to "
						+ preConfigSettlementName + " by random.");
			}

			// If settlement is still null (no settlements available),
			// Don't create person.
			if (settlement == null) {
				return;
			}

			// If settlement does not have initial population capacity, try
			// another settlement.
			if (settlement.getInitialPopulation() <= settlement.getIndoorPeopleCount()) {
				Iterator<Settlement> i = getSettlements().iterator();
				Settlement newSettlement = null;
				while (i.hasNext() && (newSettlement == null)) {
					Settlement tempSettlement = i.next();
					if (tempSettlement.getInitialPopulation() > tempSettlement.getIndoorPeopleCount()) {
						newSettlement = tempSettlement;
					}
				}

				if (newSettlement != null) {
					settlement = newSettlement;
				} else {
					// If no settlement with room found, don't create person.
					return;
				}
			}

			// Retrieve country & sponsor designation from people.xml (may be edited in
			// CrewEditorFX)
			String sponsor = personConfig.getConfiguredPersonSponsor(x, crew_id);
			String country = personConfig.getConfiguredPersonCountry(x, crew_id);

			// Create person and add to the unit manager.
			// Use Builder Pattern for creating an instance of Person
			Person person = Person.create(name, settlement).setGender(gender).setCountry(country).setSponsor(sponsor)
					.build();
			person.initialize();

			personList.add(person);
			
			// TODO: read from file
			addUnit(person);

			relationshipManager.addInitialSettler(person, settlement);

			// Set person's job (if any).
			String jobName = personConfig.getConfiguredPersonJob(x, crew_id);
			if (jobName != null) {
				Job job = JobManager.getJob(jobName);
				if (job != null) {
					// Designate a specific job to a person
					person.getMind().setJob(job, true, JobManager.MISSION_CONTROL, JobAssignmentType.APPROVED,
							JobManager.MISSION_CONTROL);
					// Assign a job to a person based on settlement's need
				}
			}

			// Add Favorite class
			String mainDish = personConfig.getFavoriteMainDish(x, crew_id);
			String sideDish = personConfig.getFavoriteSideDish(x, crew_id);
			String dessert = personConfig.getFavoriteDessert(x, crew_id);
			String activity = personConfig.getFavoriteActivity(x, crew_id);

			person.getFavorite().setFavoriteMainDish(mainDish);
			person.getFavorite().setFavoriteSideDish(sideDish);
			person.getFavorite().setFavoriteDessert(dessert);
			person.getFavorite().setFavoriteActivity(FavoriteType.fromString(activity));

			// Set the person's configured Big Five Personality traits (if any).
			Map<String, Integer> bigFiveMap = personConfig.getBigFiveMap(x);
			if (bigFiveMap != null) {
				for (String type : bigFiveMap.keySet()) {
					int value = bigFiveMap.get(type);
					person.getMind().getTraitManager().setPersonalityTrait(PersonalityTraitType.fromString(type),
							value);
				}
			}

			// Override person's personality type based on people.xml, if any.
			String personalityType = personConfig.getConfiguredPersonPersonalityType(x, crew_id);
			if (personalityType != null) {
				person.getMind().getMBTI().setTypeString(personalityType);
			}

			// Call syncUpExtraversion() to sync up the extraversion score between the two
			// personality models
			person.getMind().getMBTI().syncUpExtraversion();

			// Set person's configured natural attributes (if any).
			Map<String, Integer> naturalAttributeMap = personConfig.getNaturalAttributeMap(x);
			if (naturalAttributeMap != null) {
				Iterator<String> i = naturalAttributeMap.keySet().iterator();
				while (i.hasNext()) {
					String attributeName = i.next();
					int value = (Integer) naturalAttributeMap.get(attributeName);
					person.getNaturalAttributeManager()
							.setAttribute(NaturalAttributeType.valueOfIgnoreCase(attributeName), value);
				}
			}

			// Set person's configured skills (if any).
			Map<String, Integer> skillMap = personConfig.getSkillMap(x);
			if (skillMap != null) {
				Iterator<String> i = skillMap.keySet().iterator();
				while (i.hasNext()) {
					String skillName = i.next();
					int level = (Integer) skillMap.get(skillName);
					person.getMind().getSkillManager()
							.addNewSkill(new Skill(SkillType.valueOfIgnoreCase(skillName), level));
				}
			}

			// Initialize Preference
			person.getPreference().initializePreference();

			// Initialize emotional states
			// person.setEmotionalStates(emotionJSONConfig.getEmotionalStates());
		}

		settlement.updateAllAssociatedPeople();
		settlement.updateAllAssociatedRobots();

		// Create all configured relationships.
		createConfiguredRelationships(personList);

	}

	/**
	 * Creates initial people based on available capacity at settlements.
	 * 
	 * @throws Exception if people can not be constructed.
	 */
	private void createInitialPeople() {
		if (relationshipManager == null)
			relationshipManager = Simulation.instance().getRelationshipManager();

		// Randomly create all remaining people to fill the settlements to capacity.
		try {
			Iterator<Settlement> i = getSettlements().iterator();
			while (i.hasNext()) {
				Settlement settlement = i.next();
				int initPop = settlement.getInitialPopulation();

				// Fill up the settlement by creating more people
				while (settlement.getIndoorPeopleCount() < initPop) {

					String sponsor = settlement.getSponsor();

					// Check for any duplicate full Name
					List<String> existingfullnames = new ArrayList<>();	
					Iterator<Person> j = getPeople().iterator();
					while (j.hasNext()) {
						String n = j.next().getName();
						existingfullnames.add(n);
					}
					
					// Prevent mars-sim from using the user defined commander's name  
					String userName = getFullname();
					if (userName != null && !existingfullnames.contains(userName))
						existingfullnames.add(userName);
					
					boolean isUniqueName = false;
					GenderType gender = null;
					Person person = null;
					String fullname = null;
					String country = getCountry(sponsor);
//					System.out.println("country : " + country);
					// Make sure settlement name isn't already being used.
					while (!isUniqueName) {

						int index = -1;

						isUniqueName = true;

						gender = GenderType.FEMALE;
						if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) {
							gender = GenderType.MALE;
						}

						String lastN = null;
						String firstN = null;

						boolean skip = false;

						List<String> last_list = new ArrayList<>();
						List<String> male_first_list = new ArrayList<>();
						List<String> female_first_list = new ArrayList<>();

						if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CNSA
								|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CNSA_L) {
							index = 0;

						} else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CSA
								|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CSA_L) {
							index = 1;

						} else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ISRO
								|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ISRO_L) {
							index = 2;

						} else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.JAXA
								|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.JAXA_L) {
							index = 3;

						} else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.NASA
								|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.NASA_L) {
							index = 4;

						} else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.RKA
								|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.RKA_L) {
							index = 5;

						} else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ESA
								|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ESA_L) {
							index = 6;

							int countryID = getCountryID(country);

							last_list = lastNamesByCountry.get(countryID);
							male_first_list = maleFirstNamesByCountry.get(countryID);
							female_first_list = femaleFirstNamesByCountry.get(countryID);

//						} else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.MARS_SOCIETY
//								 || ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.SPACE_X) {
//							index = 7;
//							skip = true;
//							fullname = getNewName(UnitType.PERSON, null, gender, null);

//						} else if (sponsor.contains("SpaceX")) {
//							index = 8;

						} else { // if belonging to the Mars Society
							index = 7;
							skip = true;
							fullname = getNewName(UnitType.PERSON, null, gender, null);
						}

						if (index != -1 && index != 2 && index != 7 && index != 8) {
							last_list = lastNamesBySponsor.get(index);
							male_first_list = maleFirstNamesBySponsor.get(index);
							female_first_list = femaleFirstNamesBySponsor.get(index);
						}

						if (!skip) {

							int rand0 = RandomUtil.getRandomInt(last_list.size() - 1);
							lastN = last_list.get(rand0);

							if (gender == GenderType.MALE) {
								int rand1 = RandomUtil.getRandomInt(male_first_list.size() - 1);
								firstN = male_first_list.get(rand1);
							} else {
								int rand1 = RandomUtil.getRandomInt(female_first_list.size() - 1);
								firstN = female_first_list.get(rand1);
							}

							fullname = firstN + " " + lastN;

						}

						// double checking if this name has already been in use
						Iterator<String> k = existingfullnames.iterator();
						while (k.hasNext()) {
							String n = k.next();

							if (n.equals(fullname)) {
								isUniqueName = false;
								logger.config(fullname + " is a duplicate name. Choose another one.");
								// break;
							}
						}

						// Prevent mars-sim from using the user defined commander's name  
						if (fullname.equals(getFullname()))
							isUniqueName = false;
					}

					// Use Builder Pattern for creating an instance of Person
					person = Person.create(fullname, settlement).setGender(gender).setCountry(country)
							.setSponsor(sponsor).build();
					person.initialize();

					Mind m = person.getMind();
					// Call syncUpExtraversion() to sync up the extraversion score between the two
					// personality models
					m.getMBTI().syncUpExtraversion();

					addUnit(person);

					relationshipManager.addInitialSettler(person, settlement);

					// Add Favorite class
					Favorite f = person.getFavorite();

					// Use getRandomDishes() to obtain main and side dishes
					String[] dishes = f.getRandomDishes();
					String mainDish = dishes[0];// f.getRandomMainDish();
					String sideDish = dishes[1];// f.getRandomSideDish();
					String dessert = f.getRandomDessert();
					FavoriteType activity = f.getRandomActivity();

					f.setFavoriteMainDish(mainDish);
					f.setFavoriteSideDish(sideDish);
					f.setFavoriteDessert(dessert);
					f.setFavoriteActivity(activity);

					// Set up preference
					person.getPreference().initializePreference();

//					System.out.println("UnitManager's createInitialPeople : settlement is " + settlement);
					// Assign a job 
					m.getInitialJob(JobManager.MISSION_CONTROL);

					// Add sponsor
					person.assignReportingAuthority();

					// Get a role
					person.getRole().obtainRole(settlement);

				}

				// Add calling updateAllAssociatedPeople(), not getAllAssociatedPeople()()
				settlement.updateAllAssociatedPeople();
				settlement.updateAllAssociatedRobots();

				// Set up work shift
				setupShift(settlement, initPop);

				// Establish a system of governance at settlement.
				settlement.getChainOfCommand().establishSettlementGovernance(settlement);
				
			}

		} catch (Exception e) {
			e.printStackTrace(System.err);
			// throw new IllegalStateException("People could not be created: " +
			// e.getMessage(), e);
		}
	}
	
	public void tuneJobDeficit() {
		Collection<Settlement> col = lookupSettlement.values();//CollectionUtils.getSettlement(units);
		for (Settlement settlement : col) {
			settlement.tuneJobDeficit();
		}
	}
	
	
	/**
	 * Update the commander's profile
	 * 
	 * @param cc the person instance
	 */
	public void updateCommander(Person cc) {
		String newCountry = getCountryStr();
		String newSponsor = getSponsor();		
		String newName = getFullname();
		String newGender = getGender();
		String newJob = getJobStr();

		// Replace the commander 
		cc.setName(newName);
		cc.setGender(newGender);
		cc.changeAge(getAge());
		cc.setJob(newJob, JobManager.MISSION_CONTROL);
//		logger.config(newName + " just picked the " + newJob + " job.");
		cc.setRole(RoleType.COMMANDER);
		logger.config(newName + " just picked the Commander role.");
		cc.setCountry(newCountry);
		cc.setSponsor(newSponsor);		
		
		commanderID = cc.getIdentifier();
		isCommanderMode = true;
		GameManager.setCommander(cc);
	}
	
	public int getCommanderID() {
		return commanderID;
	}
	
	/**
	 * Find the settlement match for the user proposed commander's sponsor 
	 */
	public void matchSettlement() {
		
		String country = getCountryStr();
		String sponsor = getSponsor();
		
		List<Settlement> list = new ArrayList<>(getSettlements());
		int size = list.size();
		for (int j = 0; j < size; j++) {
			Settlement s = list.get(j);		
			// If the sponsors are a match
			if (sponsor.equals(s.getSponsor()) ) {			
				s.setDesignatedCommander(true);
				logger.config("The country of '" + country + "' does have a settlement called '" + s + "'.");
				return;
			}
			
			// If this is the last settlement to examine
			else if ((j == size - 1)) {			
				s.setDesignatedCommander(true);
				logger.config("The country of '" + country + "' doesn't have any settlements.");
				return;
			}
		}			
	}
	
	
	public void setJob(Person p, int id) {
		// Designate a specific job to a person
		p.getMind().setJob(JobManager.getJob(JobType.getEditedJobString(id)), true, JobManager.MISSION_CONTROL, JobAssignmentType.APPROVED,
					JobManager.MISSION_CONTROL);
	}



	/**
	 * Determines the number of shifts for a settlement and assigns a work shift for
	 * each person
	 * 
	 * @param settlement
	 * @param pop population
	 */
	public void setupShift(Settlement settlement, int pop) {

		int numShift = 0;
		// ShiftType shiftType = ShiftType.OFF;

		if (pop == 1) {
			numShift = 1;
		} else if (pop < THREE_SHIFTS_MIN_POPULATION) {
			numShift = 2;
		} else {// if pop >= 6
			numShift = 3;
		}

		settlement.setNumShift(numShift);

		Collection<Person> people = settlement.getAllAssociatedPeople();

		for (Person p : people) {
			// keep pop as a param just
			// to speed up processing
			p.setShiftType(settlement.getAnEmptyWorkShift(pop));
		}

	}


	/**
	 * Creates all configured Robots.
	 *
	 * @throws Exception if error parsing XML.
	 */
	private void createPreconfiguredRobots() {
		int numBots = 0;
		int size = robotConfig.getNumberOfConfiguredRobots();
		// If players choose # of bots less than what's being configured
		// Create all configured robot.
		Collection<Settlement> col = new ArrayList<>(lookupSettlement.values());//CollectionUtils.getSettlement(units);
		for (int x = 0; x < size; x++) {
			boolean isDestinationChange = false;
			// Get robot's name (required)
			String name = robotConfig.getConfiguredRobotName(x);
			if (name == null) {
				throw new IllegalStateException("Robot name is null");
			}
			// Get robotType
			RobotType robotType = robotConfig.getConfiguredRobotType(x);
			// Get robot's settlement or randomly determine it if not
			// configured.
			String preConfigSettlementName = robotConfig.getConfiguredRobotSettlement(x);
			Settlement settlement = CollectionUtils.getSettlement(col, preConfigSettlementName);
			if (preConfigSettlementName != null) {
				// Find the settlement instance with that name
//				settlement = CollectionUtils.getSettlement(col, preConfigSettlementName);
				if (settlement == null) {
					// TODO: If settlement cannot be found that matches the settlement name,
					// should we put the robot in a randomly selected settlement?
					boolean done = false;
					while (!done) {
						if (col.size() > 0) {
							settlement = CollectionUtils.getRandomSettlement(col);
							settlement.updateAllAssociatedRobots();
							col.remove(settlement);
							
							boolean filled = (settlement.getNumBots() <= settlement.getProjectedNumOfRobots());
							if (filled) {
								isDestinationChange = true;
								done = true;
								logger.log(Level.CONFIG, "Robot " + name + " is being sent to " + settlement + " since "
										+ preConfigSettlementName + " doesn't exist.");
							}	

						}
						else {
							isDestinationChange = false;
							done = true;
							break;
						}
					}
				}
				
				else {
					// settlement != null
					boolean filled = (settlement.getNumBots() <= settlement.getProjectedNumOfRobots());
					if (filled) {
						isDestinationChange = true;
					}
					
					else {
						boolean done = false;
						while (!done) {
							if (col.size() > 0) {
								settlement = CollectionUtils.getRandomSettlement(col);
								settlement.updateAllAssociatedRobots();
								col.remove(settlement);
								
								if (filled) {
									isDestinationChange = true;
									done = true;
								}	
							}
							else {
								isDestinationChange = false;
								done = true;
								break;
							}
						}
					}
				}
			}
			
			else {
				// preConfigSettlementName = null
				boolean done = false;
				while (!done) {
					if (col.size() > 0) {
						settlement = CollectionUtils.getRandomSettlement(col);
						
						settlement.updateAllAssociatedRobots();
						col.remove(settlement);
						
						if (settlement.getNumBots() <= settlement.getProjectedNumOfRobots()) {
							isDestinationChange = true;
							done = true;
							logger.log(Level.CONFIG, "Robot " + name + " is being sent to " + settlement + " since "
									+ preConfigSettlementName + " doesn't exist.");
						}	
					}
					else {
						isDestinationChange = false;
						done = true;
						break;
					}
					
				}
				
			}
	
			// If settlement is still null (no settlements available), don't create robot.
			if (settlement == null) {
				return;
			}

			// update the num of bots
			settlement.updateAllAssociatedRobots();
			
//			System.out.println("settlement.getInitialNumOfRobots() : " + settlement.getInitialNumOfRobots());
//			System.out.println("settlement.getNumBots() : " + settlement.getNumBots());
			
			// If settlement does not have initial robot capacity, try another settlement.
			if (settlement.getProjectedNumOfRobots() <= numBots) { //settlement.getNumBots()) {
				return;
			}
	
			// Add "if (settlement != null)" to stop the last
			// instance of robot from getting overwritten
			if (settlement != null) {
				// Set robot's job (if any).
				String jobName = robotConfig.getConfiguredRobotJob(x);
				if (jobName != null) {
					String templateName = settlement.getTemplate();

					boolean proceed = true;

					if (jobName.equalsIgnoreCase("Gardener") && templateName.equals("Trading Outpost"))
						proceed = false;

					if (jobName.equalsIgnoreCase("Gardener") && templateName.equals("Mining Outpost"))
						proceed = false;

					if (proceed) {
						// Create robot and add to the unit manager.
			
						// Adopt Static Factory Method and Factory Builder Pattern
						Robot robot = Robot.create(name, settlement, robotType).setCountry("Earth").build();
						robot.initialize();
						addUnit(robot);
						numBots++;

						if (isDestinationChange) {

							RobotJob robotJob = JobManager.getRobotJob(robotType.getName());
							if (robotJob != null) {
								robot.getBotMind().setRobotJob(robotJob, true);
							}
	
							// Set robot's configured natural attributes (if any).
							Map<String, Integer> attributeMap = robotConfig.getRoboticAttributeMap(x);
							if (attributeMap != null) {
								Iterator<String> i = attributeMap.keySet().iterator();
								while (i.hasNext()) {
									String attributeName = i.next();
									int value = (Integer) attributeMap.get(attributeName);
									robot.getRoboticAttributeManager()
											.setAttribute(RoboticAttributeType.valueOfIgnoreCase(attributeName), value);
								}
							}
	
							// Set robot's configured skills (if any).
							Map<String, Integer> skillMap = robotConfig.getSkillMap(x);
							if (skillMap != null) {
								Iterator<String> i = skillMap.keySet().iterator();
								while (i.hasNext()) {
									String skillName = i.next();
									int level = (Integer) skillMap.get(skillName);
									robot.getBotMind().getSkillManager()
											.addNewSkill(new Skill(SkillType.valueOfIgnoreCase(skillName), level));
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Creates initial Robots based on available capacity at settlements.
	 * 
	 * @throws Exception if Robots can not be constructed.
	 */
	private void createInitialRobots() {
		// Randomly create all remaining robots to fill the settlements to capacity.
		try {
			Iterator<Settlement> i = getSettlements().iterator();
			while (i.hasNext()) {
				Settlement settlement = i.next();
				int initial = settlement.getProjectedNumOfRobots();
				// Note : need to call updateAllAssociatedRobots() first to compute numBots in Settlement
				while (settlement.getIndoorRobotsCount() < initial) {
					// Get a robotType randomly
					RobotType robotType = getABot(settlement, initial);
					// Adopt Static Factory Method and Factory Builder Pattern
					Robot robot = Robot.create(getNewName(UnitType.ROBOT, null, null, robotType), settlement, robotType)
							.setCountry("Earth").build();
					robot.initialize();

					addUnit(robot);

					String jobName = RobotJob.getName(robotType);
					if (jobName != null) {
						RobotJob robotJob = JobManager.getRobotJob(robotType.getName());
						if (robotJob != null) {
							robot.getBotMind().setRobotJob(robotJob, true);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new IllegalStateException("Robots could not be created: " + e.getMessage(), e);
		}
	}

	/**
	 * Obtains a robot type
	 * 
	 * @param s
	 * @param max
	 * @return
	 */
	public RobotType getABot(Settlement s, int max) {

		int[] numBots = new int[] { 0, 0, 0, 0, 0, 0, 0 };

		RobotType robotType = null;

		// find out how many in each robot type
		Iterator<Robot> i = s.getRobots().iterator();
		while (i.hasNext()) {
			Robot robot = i.next();
			if (robot.getRobotType() == RobotType.MAKERBOT)
				numBots[0]++;
			else if (robot.getRobotType() == RobotType.GARDENBOT)
				numBots[1]++;
			else if (robot.getRobotType() == RobotType.REPAIRBOT)
				numBots[2]++;
			else if (robot.getRobotType() == RobotType.CHEFBOT)
				numBots[3]++;
			else if (robot.getRobotType() == RobotType.MEDICBOT)
				numBots[4]++;
			else if (robot.getRobotType() == RobotType.DELIVERYBOT)
				numBots[5]++;
			else if (robot.getRobotType() == RobotType.CONSTRUCTIONBOT)
				numBots[6]++;
		}

		// determine the robotType
		if (max <= 4) {
			if (numBots[0] < 1)
				robotType = RobotType.MAKERBOT;
			else if (numBots[1] < 1)
				robotType = RobotType.GARDENBOT;
			else if (numBots[2] < 1)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[3] < 1)
				robotType = RobotType.CHEFBOT;
		}

		else if (max <= 6) {
			if (numBots[0] < 1)
				robotType = RobotType.MAKERBOT;
			else if (numBots[1] < 1)
				robotType = RobotType.GARDENBOT;
			else if (numBots[2] < 1)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[3] < 1)
				robotType = RobotType.CHEFBOT;
			else if (numBots[4] < 1)
				robotType = RobotType.MEDICBOT;
			else if (numBots[5] < 1)
				robotType = RobotType.DELIVERYBOT;
			// else if (numBots[6] < 1)
			// robotType = RobotType.CONSTRUCTIONBOT;
		}

		else if (max <= 9) {
			if (numBots[0] < 2)
				robotType = RobotType.MAKERBOT;
			else if (numBots[1] < 2)
				robotType = RobotType.GARDENBOT;
			else if (numBots[2] < 1)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[3] < 1)
				robotType = RobotType.CHEFBOT;
			else if (numBots[4] < 1)
				robotType = RobotType.MEDICBOT;
			else if (numBots[5] < 1)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[6] < 1)
				robotType = RobotType.CONSTRUCTIONBOT;
		}

		else if (max <= 12) {
			if (numBots[0] < 3)
				robotType = RobotType.MAKERBOT;
			else if (numBots[1] < 3)
				robotType = RobotType.GARDENBOT;
			else if (numBots[2] < 2)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[3] < 1)
				robotType = RobotType.CHEFBOT;
			else if (numBots[4] < 1)
				robotType = RobotType.MEDICBOT;
			else if (numBots[5] < 1)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[6] < 1)
				robotType = RobotType.CONSTRUCTIONBOT;
		}

		else if (max <= 18) {
			if (numBots[0] < 5)
				robotType = RobotType.MAKERBOT;
			else if (numBots[1] < 4)
				robotType = RobotType.GARDENBOT;
			else if (numBots[2] < 4)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[3] < 2)
				robotType = RobotType.CHEFBOT;
			else if (numBots[4] < 1)
				robotType = RobotType.MEDICBOT;
			else if (numBots[5] < 1)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[6] < 1)
				robotType = RobotType.CONSTRUCTIONBOT;
		}

		else if (max <= 24) {

			if (numBots[0] < 7)
				robotType = RobotType.MAKERBOT;
			else if (numBots[1] < 6)
				robotType = RobotType.GARDENBOT;
			else if (numBots[2] < 5)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[3] < 3)
				robotType = RobotType.CHEFBOT;
			else if (numBots[4] < 1)
				robotType = RobotType.MEDICBOT;
			else if (numBots[5] < 1)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[6] < 1)
				robotType = RobotType.CONSTRUCTIONBOT;
		}

		else if (max <= 36) {
			if (numBots[0] < 9)
				robotType = RobotType.MAKERBOT;
			else if (numBots[1] < 9)
				robotType = RobotType.GARDENBOT;
			else if (numBots[2] < 7)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[3] < 5)
				robotType = RobotType.CHEFBOT;
			else if (numBots[4] < 3)
				robotType = RobotType.MEDICBOT;
			else if (numBots[5] < 2)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[6] < 1)
				robotType = RobotType.CONSTRUCTIONBOT;
		}

		else if (max <= 48) {
			if (numBots[0] < 11)
				robotType = RobotType.MAKERBOT;
			else if (numBots[1] < 11)
				robotType = RobotType.GARDENBOT;
			else if (numBots[2] < 10)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[3] < 7)
				robotType = RobotType.CHEFBOT;
			else if (numBots[4] < 4)
				robotType = RobotType.MEDICBOT;
			else if (numBots[5] < 3)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[6] < 2)
				robotType = RobotType.CONSTRUCTIONBOT;
		}

		else {
			if (numBots[0] < 11)
				robotType = RobotType.MAKERBOT;
			else if (numBots[1] < 11)
				robotType = RobotType.GARDENBOT;
			else if (numBots[2] < 10)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[3] < 7)
				robotType = RobotType.CHEFBOT;
			else if (numBots[4] < 4)
				robotType = RobotType.MEDICBOT;
			else if (numBots[5] < 3)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[6] < 2)
				robotType = RobotType.CONSTRUCTIONBOT;
			else {
				int rand = RandomUtil.getRandomInt(20);
				if (rand <= 3)
					robotType = RobotType.MAKERBOT;
				else if (rand <= 7)
					robotType = RobotType.GARDENBOT;
				else if (rand <= 11)
					robotType = RobotType.REPAIRBOT;
				else if (rand <= 14)
					robotType = RobotType.CHEFBOT;
				else if (rand <= 16)
					robotType = RobotType.DELIVERYBOT;
				else if (rand <= 18)
					robotType = RobotType.CONSTRUCTIONBOT;
				else if (rand <= 20)
					robotType = RobotType.MEDICBOT;
				else
					robotType = RobotType.MAKERBOT;
			}
		}

		if (robotType == null) {
			System.out.println("UnitManager : robotType is null");
			robotType = RobotType.MAKERBOT;
		}
		return robotType;
	}

	/**
	 * Creates all configured people relationships.
	 *
	 * @throws Exception if error parsing XML.
	 */
	private void createConfiguredRelationships(List<Person> personList) {

		int size = personConfig.getNumberOfConfiguredPeople();
			
		// Create all configured people relationships.
		for (int x = 0; x < size; x++) {
			try {

				// Get person's name
//				String name = personConfig.getConfiguredPersonName(x, PersonConfig.ALPHA_CREW);
//				if (name == null || name.equals("")) {
//					throw new IllegalStateException("Person name is null");
//				}

				// Get the person
				Person person = personList.get(x);
//				Person person = null;
//				Iterator<Person> j = getPeople().iterator();
//				while (j.hasNext()) {
//					Person tempPerson = j.next();
//					if (tempPerson.getName().equals(name)) {
//						person = tempPerson;
//					}
//				}
//				if (person == null) {
//					throw new IllegalStateException("Person: " + name + " not found.");
//				}

				
				// Set person's configured relationships (if any).
				Map<String, Integer> relationshipMap = personConfig.getRelationshipMap(x);
				if (relationshipMap != null) {
					Iterator<String> i = relationshipMap.keySet().iterator();
					while (i.hasNext()) {
						String relationshipName = i.next();

						// Get the other people in the same settlement in the relationship.
						Person relationshipPerson = null;
						Iterator<Person> k = personList.iterator();
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
							relationshipManager.addRelationship(person, relationshipPerson,
									Relationship.EXISTING_RELATIONSHIP);
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
	 * Notify all the units that time has passed. Times they are a changing.
	 *
	 * @param time the amount time passing (in millisols)
	 * @throws Exception if error during time passing.
	 */
	void timePassing(double time) {
		// Note : resetting marsClock is needed after loading from a saved sim 
		// Cannot add "if (marsClock == null)"		
//		marsClock = Simulation.instance().getMasterClock().getMarsClock();
		
		int solElapsed = marsClock.getMissionSol();
		
		if (solCache != solElapsed) {
			solCache = solElapsed;
		
			if (solElapsed == 1)
				// Note that when loading from a saved sim...
				logger.info(" - - - - - - - - - - - - Sol " + solCache + " - - - - - - - - - - - - ");

			else //if (solElapsed != 1)
				// Note that when loading from a saved sim...
				logger.info(" - - - - - - - - - - - - Sol " + solCache + " - - - - - - - - - - - - ");
			
			// Compute reliability daily
			factory.computeReliability();
		}

		if (justLoaded) {
			// Only need to run all these below once at the start of the sim
			factory.computeReliability();

			Collection<Settlement> c = lookupSettlement.values();//CollectionUtils.getSettlement(units);
			for (Settlement s : c) {
				s.updateAllAssociatedPeople();
				s.updateAllAssociatedRobots();
				s.updateAllAssociatedVehicles();
			}
						
			justLoaded = false;
		}

//		if (units == null)
//			computeUnits();

//		for (Unit u : units) {
//			u.timePassing(time);
//		}
		
		for (Settlement s : lookupSettlement.values()) 
			s.timePassing(time);

		for (Person p : lookupPerson.values()) 
			p.timePassing(time);
		
		for (Robot r : lookupRobot.values()) 
			r.timePassing(time);

		for (Equipment e : lookupEquipment.values()) 
			e.timePassing(time);

		for (Vehicle v : lookupVehicle.values()) 
			v.timePassing(time);

	
		for (Unit u : lookupUnit.values()) {
			if (!(u instanceof Building)) {
				System.out.println(u);
				u.timePassing(time);
			}
		}
		
	}

	/**
	 * Get number of settlements
	 *
	 * @return the number of settlements
	 */
	public int getSettlementNum() {
		return lookupSettlement.size();//CollectionUtils.getSettlement(units).size();
	}

	/**
	 * Get settlements in virtual Mars
	 *
	 * @return Collection of settlements
	 */
	public Collection<Settlement> getSettlements() {
		if (lookupSettlement != null && !lookupSettlement.isEmpty())
			return lookupSettlement.values();//CollectionUtils.getSettlement(units);
		else 
			return new ArrayList<>();
	}

	/**
	 * Get number of vehicles
	 *
	 * @return the number of vehicles
	 */
	public int getVehicleNum() {
		return lookupVehicle.size();//CollectionUtils.getVehicle(units).size();
	}

	/**
	 * Get vehicles in virtual Mars
	 *
	 * @return Collection of vehicles
	 */
	public Collection<Vehicle> getVehicles() {
		return lookupVehicle.values();//CollectionUtils.getVehicle(units);
	}

	/**
	 * Get number of people
	 *
	 * @return the number of people
	 */
	public int getTotalNumPeople() {
		return lookupPerson.size();//CollectionUtils.getPerson(units).size();
	}

	/**
	 * Get all people in Mars
	 *
	 * @return Collection of people
	 */
	public Collection<Person> getPeople() {
		return lookupPerson.values();//CollectionUtils.getPerson(units);
	}

	/**
	 * Get all people in Mars
	 *
	 * @return Collection of people
	 */
	public Collection<Person> getOutsidePeople() {
		return //CollectionUtils.getPerson(units)
				lookupPerson.values()
				.stream()
				.filter(p -> p.getLocationStateType() == LocationStateType.OUTSIDE_SETTLEMENT_VICINITY
						|| p.getLocationStateType() == LocationStateType.OUTSIDE_ON_MARS)
				.collect(Collectors.toList());
	}

	/**
	 * Get number of Robots
	 *
	 * @return the number of Robots
	 */
	public int getRobotsNum() {
		return lookupRobot.size();//CollectionUtils.getRobot(units).size();
	}

	/**
	 * Get Robots in virtual Mars
	 *
	 * @return Collection of Robots
	 */
	public Collection<Robot> getRobots() {
		return lookupRobot.values();//CollectionUtils.getRobot(units);
	}

	/**
	 * Get the number of equipment.
	 * 
	 * @return number
	 */
	public int getEquipmentNum() {
		// TODO: should it include robots ? 
		return lookupEquipment.size();//CollectionUtils.getEquipment(units).size();
	}

	/**
	 * Get a collection of equipment.
	 * 
	 * @return collection
	 */
	public Collection<Equipment> getEquipment() {
		return lookupEquipment.values();//CollectionUtils.getEquipment(units);
	}

	/**
	 * The total number of units
	 * 
	 * @return the total number of units
	 */
	public int getUnitNum() {
		return totalNumUnits;
	}

	/**
	 * The total number of units
	 * 
	 * @return the total number of units
	 */
	public int computeUnitNum() {
		totalNumUnits = lookupUnit.size()
				+ lookupSettlement.size()
				+ lookupPerson.size()
				+ lookupRobot.size()
				+ lookupEquipment.size()
				+ lookupVehicle.size();
		return totalNumUnits;
	}
	
	/**
	 * Put together all units in virtual Mars
	 * @return
	 */
	public List<Unit> computeUnits() {
		return Stream.of(
				new ArrayList<>(lookupUnit.values()),
				new ArrayList<>(lookupSettlement.values()),
				new ArrayList<>(lookupPerson.values()),
				new ArrayList<>(lookupRobot.values()),
				new ArrayList<>(lookupEquipment.values()),
				new ArrayList<>(lookupVehicle.values())
				)
				.flatMap(Collection::stream).collect(Collectors.toList());		
//		List<Unit> list = new ArrayList<>();
//		list.addAll(lookupUnit.values());
//		list.addAll(lookupSettlement.values());
//		list.addAll(lookupPerson.values());
//		list.addAll(lookupRobot.values());
//		list.addAll(lookupEquipment.values());
//		list.addAll(lookupVehicle.values());	
//		return new ArrayList<>(list);
	}
	
//	public Collection<Unit>[] computeUnitArray() {
//		return new Collection<Unit>[] {
//				lookupUnit.values(),
//				lookupSettlement.values(),
//				lookupPerson.values(),
//				lookupRobot.values(),
//				lookupEquipment.values(),
//				lookupVehicle.values()
//		};
//	}
	
	
//	/**
//	 * Get all units in virtual Mars
//	 * 
//	 * @return Colleciton of units
//	 */
//	public Collection<Unit> getUnits() {
//		return units;
//	}

	/**
	 * Adds a unit manager listener
	 * 
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
	 * 
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
	 * 
	 * @param eventType the event type.
	 * @param unit      the unit causing the event.
	 */
	public final void fireUnitManagerUpdate(UnitManagerEventType eventType, Unit unit) {
		if (listeners == null) {
			listeners = Collections.synchronizedList(new ArrayList<UnitManagerListener>());
		}
		synchronized (listeners) {
			for (UnitManagerListener listener : listeners) {
				listener.unitManagerUpdate(new UnitManagerEvent(this, eventType, unit));
			}
		}
	}

	/**
	 * Finds a unit in the simulation that has the given name.
	 * 
	 * @param name the name to search for.
	 * @return unit or null if none.
	 */
	public Unit findUnit(String name) {
		Unit result = null;
		Iterator<Unit> i = computeUnits().iterator();
		while (i.hasNext() && (result == null)) {
			Unit unit = i.next();
			if (unit.getName().equalsIgnoreCase(name)) {
				result = unit;
			}
		}
		return result;
	}

	
	public static String getCountry(String sponsor) {

		if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CNSA
			|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CNSA_L)
			return "China";
		else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CSA
			||	ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CSA_L)
			return "Canada";
		else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ISRO
			||	ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ISRO_L)
			return "India";
		else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.JAXA
			||	ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.JAXA_L)
			return "Japan";
		else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.NASA
			||	ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.NASA_L)
			return "USA";
		else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.RKA
			||	ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.RKA_L)
			return "Russia";
		else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ESA
			||	ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ESA_L)
			return countries.get(RandomUtil.getRandomInt(6, 27));
		else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.MS
			||	ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.MARS_SOCIETY_L)
			return "USA";
		else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.SPACEX
			||	ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.SPACEX_L)
			return "USA";
		else
			return "USA";

	}

	public static String getCountryByID(int id) {
		if (countries == null)
			getCountryList();
		return countries.get(id);
	}
	
	public static String getSponsorByCountryID(int id) {
		if (id == -1)
			return "None";
		else if (id == 0)
			return ReportingAuthorityType.CNSA_L.getName();
		else if (id == 1)
			return ReportingAuthorityType.CSA_L.getName();
		else if (id == 2)
			return ReportingAuthorityType.ISRO_L.getName();
		else if (id == 3)
			return ReportingAuthorityType.JAXA_L.getName();
		else if (id == 4)
			return ReportingAuthorityType.NASA_L.getName(); // MS or SpaceX
		else if (id == 5)			
			return ReportingAuthorityType.RKA_L.getName();	
		else
			return ReportingAuthorityType.ESA_L.getName();
	}
	
	/**
	 * Maps the country to its sponsor
	 * 
	 * @param country
	 * @return sponsor
	 */
	public static String mapCountry2Sponsor(String country) {
		return getSponsorByCountryID(getCountryID(country));
	}
	
	/**
	 * Create the country list
	 * 
	 */
//	public static List<String> createCountryList() {
//
//		List<String> countries = new ArrayList<>();
//
//		countries.add("China"); //0
//		countries.add("Canada"); //1
//		countries.add("India"); //2
//		countries.add("Japan"); //3
//		countries.add("USA"); //4
//		countries.add("Russia"); //5
//
//		countries.add("Austria");
//		countries.add("Belgium");
//		countries.add("Czech Republic");
//		countries.add("Denmark");
//		countries.add("Estonia");
//		countries.add("Finland");
//		countries.add("France");
//		countries.add("Germany");
//		countries.add("Greece");
//		countries.add("Hungary");
//		countries.add("Ireland");
//		countries.add("Italy");
//		countries.add("Luxembourg");
//		countries.add("The Netherlands");
//		countries.add("Norway");
//		countries.add("Poland");
//		countries.add("Portugal");
//		countries.add("Romania");
//		countries.add("Spain");
//		countries.add("Sweden");
//		countries.add("Switzerland");
//		countries.add("UK");
//
//		return countries;
//	}

	/**
	 * Obtains the country id. If none, return -1.
	 * 
	 * @param country
	 * @return
	 */
	public static int getCountryID(String country) {
		if (personConfig == null)
			personConfig = SimulationConfig.instance().getPersonConfiguration();
		if (countries == null)
			countries = personConfig.createCountryList();
		return personConfig.computeCountryID(country);
	}

	public static List<String> getCountryList() {
		if (personConfig == null)
			personConfig = SimulationConfig.instance().getPersonConfiguration();
		if (countries == null)
			countries = personConfig.createCountryList();
		return countries;
	}
	
//	/**
//	 * Sets the commander mode
//	 */
//	public void setCommanderMode(boolean value) {
//		isCommanderMode = value;
//	}
	
	/**
	 * is the simulation running in commander mode ? 
	 */
	public boolean getCommanderMode() {
		return isCommanderMode;
	}
	
	/** Gets the commander's fullname */
	public String getFullname() {
		// During maven test, CommanderProfile/Contact instance doesn't exist
		if (personConfig == null)
			personConfig = SimulationConfig.instance().getPersonConfiguration();
		if (personConfig != null)
			return personConfig.getCommander().getFullName();
		else
			return null;
	}
	
	/** Gets the commander's gender */
	public String getGender() {
		return personConfig.getCommander().getGender();
	}
	
	/** Gets the commander's age */
	public int getAge() {
		return personConfig.getCommander().getAge();
	}

	/** Gets the commander's job */
	public int getJob() {
		return personConfig.getCommander().getJob();
	}
	
	public String getJobStr() {
		return personConfig.getCommander().getJobStr();
	}
	
//	/** Gets the commander's country */
//	public int getCountry() {
//		return personConfig.getCommander().getCountry();
//	}

	/** Gets the commander's country */
	public String getCountryStr() {
		return personConfig.getCommander().getCountryStr();
	}
	
	/** Gets the commander's sponsor */
	public String getSponsor() {
		return personConfig.getCommander().getSponsor();
	}
	
	/** Gets the settlement's phase */
	public int getPhase() {
		return personConfig.getCommander().getPhase();
	}
	
//	/**
//	 * Returns Mars surface instance
//	 * 
//	 * @return {@Link MarsSurface}
//	 */
//	public MarsSurface getMarsSurface() {
//		return marsSurface;
//	}
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 */
	public void initializeInstances(MarsClock clock) {
		marsClock = clock;
	}
	
	public void setMarsSurface() {
		sim.getMars().setMarsSurface(marsSurface);//(MarsSurface)units.get(0));
	}
	
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
//		Iterator<Unit> i = units.iterator();
//		while (i.hasNext()) {
//			i.next().destroy();
//		}
//		units.clear();
//		units = null;
		
		lookupUnit = null;
		lookupSettlement = null;

//		settlementNames.clear();
		settlementNames = null;
//		vehicleNames.clear();
		vehicleNames = null;
//		personMaleNames.clear();
		personMaleNames = null;
//		personFemaleNames.clear();
		personFemaleNames = null;
		listeners.clear();
		listeners = null;
		// personExecutor = null;
		// settlementExecutor = null;
//		equipmentNumberMap.clear();
		equipmentNumberMap = null;
//		vehicleNumberMap.clear();
		vehicleNumberMap = null;
		// masterClock = null;
//		firstSettlement = null;
		personConfig = null;
		settlementConfig = null;
		relationshipManager = null;
		// emotionJSONConfig = null;
		vehicleConfig = null;
		
		factory = null;
		marsClock = null;
	}
}