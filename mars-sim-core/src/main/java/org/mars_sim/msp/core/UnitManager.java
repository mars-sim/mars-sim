/**
 * Mars Simulation Project
 * UnitManager.java
 * @version 3.1.0 2016-10-31
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.person.Crew;
import org.mars_sim.msp.core.person.Favorite;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PersonGender;
import org.mars_sim.msp.core.person.PersonalityTraitType;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.EmotionJSONConfig;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.reportingAuthority.CNSAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.FindingLife;
import org.mars_sim.msp.core.reportingAuthority.FindingMineral;
import org.mars_sim.msp.core.reportingAuthority.MarsSocietyMissionControl;
import org.mars_sim.msp.core.reportingAuthority.NASAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.reportingAuthority.SettlingMars;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotConfig;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.robot.RoboticAttribute;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.ChainOfCommand;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleConfig;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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

	public static final int POPULATION_WITH_SUB_COMMANDER = 12;
	public static final int POPULATION_WITH_MAYOR = 48;
	public static final int POPULATION_WITH_COMMANDER = 4;
	public static final int THREE_SHIFTS_MIN_POPULATION = 6;

	public static ReportingAuthorityType[] SPONSORS = ReportingAuthorityType.SPONSORS;
	public static int NUM_SPONSORS = SPONSORS.length;

	// Data members
	//private int solCache;
	/** Flag true if the class has just been deserialized */
	public transient boolean justReloaded = true;
	/** Collection of all units. */
	private Collection<Unit> units;
	/** List of possible settlement names. */
	private List<String> settlementNames;
	/** List of possible vehicle names. */
	private List<String> vehicleNames;
	/** List of possible male person names. */
	private List<String> personMaleNames;
	/** List of possible female person names. */
	private List<String> personFemaleNames;
	/** List of possible robot names. */
	private List<String> robotNameList;

	/** List of unit manager listeners. */
	private transient List<UnitManagerListener> listeners;
	/** Map of equipment types and their numbers. */
	private Map<String, Integer> equipmentNumberMap;
	/** Map of vehicle types and their numbers. */
	private Map<String, Integer> vehicleNumberMap;

	private Map<Integer, List<String>> marsSociety = new HashMap<>();

	private Map<Integer, List<String>> maleFirstNamesBySponsor = new HashMap<>();
	private Map<Integer, List<String>> femaleFirstNamesBySponsor = new HashMap<>();

	private Map<Integer, List<String>> maleFirstNamesByCountry = new HashMap<>();
	private Map<Integer, List<String>> femaleFirstNamesByCountry = new HashMap<>();

	private Map<Integer, List<String>> lastNamesBySponsor = new HashMap<>();
	private Map<Integer, List<String>> lastNamesByCountry = new HashMap<>();

	private List<String> countries;

	private Settlement firstSettlement;
	private PersonConfig personConfig;
	private SettlementConfig settlementConfig;
	private RelationshipManager relationshipManager;
	private VehicleConfig vehicleConfig;
	private RobotConfig robotConfig;



	//private EmotionJSONConfig emotionJSONConfig;// = new EmotionJSONConfig();

    //private transient ExecutorService settlementExecutor;
	//private transient ThreadPoolExecutor personExecutor, settlementExecutor;

	/**
	 * Constructor.
	 */
	public UnitManager() {
		//logger.info("UnitManager's constructor is in " + Thread.currentThread().getName() + " Thread");
		// Initialize unit collection
		units = new ConcurrentLinkedQueue<Unit>();
		listeners = Collections.synchronizedList(new ArrayList<UnitManagerListener>());
		//settlementNamesMap = new HashMap<>();
		equipmentNumberMap = new HashMap<String, Integer>();
		vehicleNumberMap = new HashMap<String, Integer>();
		personConfig = SimulationConfig.instance().getPersonConfiguration();
		robotConfig = SimulationConfig.instance().getRobotConfiguration();
		settlementConfig = SimulationConfig.instance().getSettlementConfiguration();
		vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();
		//emotionJSONConfig = new EmotionJSONConfig();
		relationshipManager = Simulation.instance().getRelationshipManager();

		//settlementExecutor = Executors.newSingleThreadExecutor();
		//personExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
		//settlementExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);


	}

	/**
	 * Constructs initial units.
	 *
	 * @throws Exception
	 *             in unable to load names.
	 */
	void constructInitialUnits() {

		countries = personConfig.createCountryList();

		// Initialize name lists
		initializeRobotNames();
		initializePersonNames();
		initializeLastNames();
		initializeFirstNames();

		// Initialize settlement and vehicle name lists
		initializeSettlementNames();
		initializeVehicleNames();

		// Create initial units.
		createInitialSettlements();
		createInitialVehicles();
		createInitialEquipment();
		createInitialResources();
		createInitialParts();

		// Create pre-configured robots as stated in robots.xml
		createPreconfiguredRobots();
		// Create more robots to fill the settlement(s)
		createInitialRobots();
		// Create pre-configured settlers as stated in people.xml
		createPreconfiguredPeople();
		// Create more settlers to fill the settlement(s)
		createInitialPeople();
	}

	/**
	 * Initializes the list of possible person names.
	 * @throws Exception if unable to load name list.
	 */
	private void initializePersonNames() {
		try {
			//PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
			List<String> personNames = personConfig.getPersonNameList();

			personMaleNames = new ArrayList<String>();
			personFemaleNames = new ArrayList<String>();

			Iterator<String> i = personNames.iterator();
			while (i.hasNext()) {

				String name = i.next();

				//ReportingAuthorityType sponsorType = personConfig.getPersonSponsor(name);

				//if (sponsorType.equals(ReportingAuthorityType.MARS_SOCIETY)) {

					PersonGender gender = personConfig.getPersonGender(name);
					if (gender == PersonGender.MALE) {
						personMaleNames.add(name);
					} else if (gender == PersonGender.FEMALE) {
						personFemaleNames.add(name);
					}

				//}

				marsSociety.put(0, personMaleNames);
				marsSociety.put(1, personFemaleNames);
			}

		} catch (Exception e) {
			throw new IllegalStateException("The person name list could not be loaded: " + e.getMessage(), e);
		}
	}


	/**
	 * Initializes a list of last names according for each space agency.
	 * @throws Exception if unable to load the last name list.
	 */
    // 2016-04-06 Added initializeLastNames()
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
	 * @throws Exception if unable to load the first name list.
	 */
    // 2016-04-06 Added initializeFirstNames()
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
		//System.out.println("done with initializeFirstNames()");
	}


	/**
	 * Initializes the list of possible robot names.
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
	 * @throws Exception
	 *             if unable to load rover names.
	 */
	private void initializeVehicleNames() {
		try {
			//VehicleConfig vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();
			vehicleNames = vehicleConfig.getRoverNameList();
		} catch (Exception e) {
			throw new IllegalStateException("rover names could not be loaded: " + e.getMessage(), e);
		}
	}

	/**
	 * Initializes the list of possible settlement names.
	 *
	 * @throws Exception
	 *             if unable to load settlement names.
	 */
	private void initializeSettlementNames() {
		try {
			settlementNames = settlementConfig.getSettlementNameList();
		} catch (Exception e) {
			throw new IllegalStateException("settlement names could not be loaded: " + e.getMessage(), e);
		}
	}

	/**
	 * Adds a unit to the unit manager if it doesn't already have it.
	 *
	 * @param unit
	 *            new unit to add.
	 */
	public void addUnit(Unit unit) {
		if (!units.contains(unit)) {
			units.add(unit);
			Iterator<Unit> i = unit.getInventory().getContainedUnits().iterator();
			while (i.hasNext()) {
				addUnit(i.next());
			}

			// Fire unit manager event.
			fireUnitManagerUpdate(UnitManagerEventType.ADD_UNIT, unit);
		}
	}

	/**
	 * Removes a unit from the unit manager.
	 *
	 * @param unit
	 *            the unit to remove.
	 */
	public void removeUnit(Unit unit) {
		if (units.contains(unit)) {
			units.remove(unit);

			// Fire unit manager event.
			fireUnitManagerUpdate(UnitManagerEventType.REMOVE_UNIT, unit);
		}
	}

	/**
	 * Gets a new name for a unit.
	 * @param unitType {@link UnitType} the type of unit.
	 * @param baseName the base name or null if none.
	 * @param gender the gender of the person or null if not a person.
	 * @return new name
	 * @throws IllegalArgumentException  if unitType is not valid.
	 */
	public String getNewName(UnitType unitType, String baseName, PersonGender gender, RobotType robotType) {

		List<String> initialNameList = null;
		List<String> usedNames = new ArrayList<String>();
		String unitName = "";

		if (unitType == UnitType.SETTLEMENT) {
			initialNameList = settlementNames;
			Iterator<Settlement> si = getSettlements().iterator();
			while (si.hasNext()) {
				usedNames.add(si.next().getName());
			}
			unitName = "Settlement";

		} else if (unitType == UnitType.VEHICLE) {
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

		} else if (unitType == UnitType.PERSON) {
			if (PersonGender.MALE == gender) {
				initialNameList = personMaleNames;
			} else if (PersonGender.FEMALE == gender) {
				initialNameList = personFemaleNames;
			} else {
				throw new IllegalArgumentException("Improper gender for person unitType: " + gender);
			}
			Iterator<Person> pi = getPeople().iterator();
			while (pi.hasNext()) {
				usedNames.add(pi.next().getName());
			}
			unitName = "Person";

		} else if (unitType == UnitType.ROBOT) {

			initialNameList = robotNameList;

			Iterator<Robot> ri = getRobots().iterator();
			while (ri.hasNext()) {
				usedNames.add(ri.next().getName());
			}

			//System.out.println("robotType is " + robotType);
			unitName = robotType.getName();

		} else if (unitType == UnitType.EQUIPMENT) {
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
			result = remainingNames.get(RandomUtil.getRandomInt(remainingNames.size() - 1));
		} else {
			int num = usedNames.size() + 1;
			String numStr = "";
			if (num < 10)
				numStr = "00" + num;
			else if (num < 100)
				numStr = "0" + num;
			else if (num < 1000)
				numStr = "" + num;
			result = unitName + " " + numStr;

			// System.out.println("Name : " +result + " Type : " +
			// robotType.getDisplayName());
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
				// 2014-10-29 Added settlement's id called sid
				// 2015-01-16 Added scenarioID
				int scenarioID = settlementConfig.getInitialSettlementScenarioID(x);
				// System.out.println("in unitManager, scenarioID is " +
				// scenarioID);
				addUnit(new Settlement(name, scenarioID, template, sponsor, location, populationNumber, initialNumOfRobots));

			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new IllegalStateException("Settlements could not be created: " + e.getMessage(), e);
		}

		firstSettlement = getASettlement();
	}

	/**
	 * Creates initial vehicles based on settlement templates.
	 *
	 * @throws Exception
	 *             if vehicles could not be constructed.
	 */
	private void createInitialVehicles() {

		try {
			Iterator<Settlement> i = getSettlements().iterator();
			while (i.hasNext()) {
				Settlement settlement = i.next();
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
			throw new IllegalStateException("Vehicles could not be created: " + e.getMessage(), e);
		}
	}

	/**
	 * Creates the initial equipment at a settlement.
	 *
	 * @throws Exception
	 *             if error constructing equipment.
	 */
	private void createInitialEquipment() {

		try {
			Iterator<Settlement> i = getSettlements().iterator();
			while (i.hasNext()) {
				Settlement settlement = i.next();
				SettlementTemplate template = settlementConfig.getSettlementTemplate(settlement.getTemplate());
				Map<String, Integer> equipmentMap = template.getEquipment();
				Iterator<String> j = equipmentMap.keySet().iterator();
				while (j.hasNext()) {
					String type = j.next();
					int number = (Integer) equipmentMap.get(type);
					for (int x = 0; x < number; x++) {
						Equipment equipment = EquipmentFactory.getEquipment(type, settlement.getCoordinates(), false);
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
	 * Creates the initial resources at a settlement. Note: This is in addition
	 * to any initial resources set in buildings.
	 *
	 * @throws Exception
	 *             if error storing resources.
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
					// System.out.println("createInitialResources() : resource :
					// " + resource.getName());
					double amount = resourceMap.get(resource);
					// System.out.println("createInitialResources() : amount : "
					// + amount);
					Inventory inv = settlement.getInventory();
					double capacity = inv.getAmountResourceRemainingCapacity(resource, true, false);
					// System.out.println("createInitialResources() : capacity
					// is "+capacity);
					if (amount > capacity) {
						amount = capacity;
					}
					inv.storeAmountResource(resource, amount, true);

					//System.out.println(settlement.getName() + " -\t" + resource.getName() + " -\t" + amount + " / " + capacity);
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("Resource could not be created: " + e.getMessage(), e);
		}
	}

	/**
	 * Create initial parts for a settlement.
	 *
	 * @throws Exception
	 *             if error creating parts.
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
					inv.storeItemResources(part, number);
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("Part could not be created: " + e.getMessage(), e);
		}
	}

	/**
	 * Creates all pre-configured people as listed in people.xml.
	 * @throws Exception if error parsing XML.
	 */
	private void createPreconfiguredPeople() {
		Settlement settlement = null;

		if (personConfig == null) // FOR PASSING MAVEN TEST
			personConfig = SimulationConfig.instance().getPersonConfiguration();
		//RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		//EmotionJSONConfig emotionJSONConfig = new EmotionJSONConfig();

		//Crew crew = new Crew("Alpha");
		// TODO: will setting a limit on # crew to 7 be easier ?

		int size = personConfig.getNumberOfConfiguredPeople();

		//int crew_id = -1;

		// Create all configured people.
		for (int x = 0; x < size; x++) {

			// Get person's name (required)
			int crew_id = personConfig.getCrew(x);
			//System.out.println("crew_id : " + crew_id);

			// Get person's name (required)
			String name = personConfig.getConfiguredPersonName(x, crew_id);
			if (name == null) {
				throw new IllegalStateException("Person name is null");
			}

			// Get person's gender or randomly determine it if not configured.
			PersonGender gender = personConfig.getConfiguredPersonGender(x, crew_id);
			if (gender == null) {
				gender = PersonGender.FEMALE;
				if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) {
					gender = PersonGender.MALE;
				}
			}

			// Get person's settlement or randomly determine it if not configured.
			String settlementName = personConfig.getConfiguredPersonDestination(x, crew_id);
			//Settlement settlement = null;
			if (settlementName != null) {
				Collection<Settlement> col = CollectionUtils.getSettlement(units);
				settlement = CollectionUtils.getSettlement(col, settlementName);
				if (settlement == null) {
					// TODO: If settlement cannot be found that matches the settlement name,
					// should we put the person in a randomly selected settlement?
					settlement = CollectionUtils.getRandomSettlement(col);
					logger.log(Level.INFO, name + " ends up going to " + settlement
							+ " since " + settlementName + " doesn't exist.");
				}
			} else {
				Collection<Settlement> col = CollectionUtils.getSettlement(units);
				settlement = CollectionUtils.getRandomSettlement(col);
				logger.log(Level.INFO, name + " has no destination settlement specified and goes to "
						+ settlementName + " by random.");
			}

			// If settlement is still null (no settlements available),
			// Don't create person.
			if (settlement == null) {
				return;
			}

			// If settlement does not have initial population capacity, try
			// another settlement.
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
				} else {
					// If no settlement with room found, don't create person.
					return;
				}
			}

			// 2017-01-24 retrieve country & sponsor designation from people.xml (may be edited in CrewEditorFX)
			String sponsor = personConfig.getConfiguredPersonSponsor(x, crew_id);
			String country = personConfig.getConfiguredPersonCountry(x, crew_id);
			//String sponsor = settlement.getSponsor();
			//System.out.println("sponsor is " + sponsor);
			//String country = getCountry(sponsor);

			// Create person and add to the unit manager.
			Person person = new Person(name, gender, country, settlement, sponsor);

			// TODO: read from file
			addUnit(person);

			//System.out.println("done with addUnit() in createConfiguredPeople() in UnitManager");

			relationshipManager.addInitialSettler(person, settlement);

			// Set person's job (if any).
			String jobName = personConfig.getConfiguredPersonJob(x, crew_id);
			if (jobName != null) {
				Job job = JobManager.getJob(jobName);
				if (job != null) {
					// 2016-04-16 Designate a specific job to a person
					person.getMind().setJob(job, true, JobManager.MISSION_CONTROL, JobAssignmentType.APPROVED ,JobManager.MISSION_CONTROL);
					// Assign a job to a person based on settlement's need
					//person.getMind().getInitialJob(JobManager.MISSION_CONTROL);
				}
			}

			// 2015-02-27 and 2015-03-24 Added Favorite class
			String mainDish = personConfig.getFavoriteMainDish(x, crew_id);
			String sideDish = personConfig.getFavoriteSideDish(x, crew_id);
			String dessert = personConfig.getFavoriteDessert(x, crew_id);
			String activity = personConfig.getFavoriteActivity(x, crew_id);

			person.getFavorite().setFavoriteMainDish(mainDish);
			person.getFavorite().setFavoriteSideDish(sideDish);
			person.getFavorite().setFavoriteDessert(dessert);
			person.getFavorite().setFavoriteActivity(activity);
			//System.out.println("done with setFavorite_() in createConfiguredPeople() in UnitManager");


			// 2015-11-23 Set the person's configured Big Five Personality traits (if any).
			Map<String, Integer> bigFiveMap = personConfig.getBigFiveMap(x);
			if (bigFiveMap != null) {
				for (String type : bigFiveMap.keySet()) {
					int value = bigFiveMap.get(type);
					//System.out.println(type + " : " + value);
					person.getMind().getPersonalityTraitManager()
					.setPersonalityTrait(PersonalityTraitType.fromString(type), value);
				}
			}

			// Override person's personality type based on people.xml, if any.
			String personalityType = personConfig.getConfiguredPersonPersonalityType(x, crew_id);
			if (personalityType != null) {
				person.getMind().getMBTI().setTypeString(personalityType);
			}

			// 2016-11-05 Call syncUpExtraversion() to sync up the extraversion score between the two personality models
			person.getMind().getMBTI().syncUpExtraversion();

			// Set person's configured natural attributes (if any).
			Map<String, Integer> naturalAttributeMap = personConfig.getNaturalAttributeMap(x);
			if (naturalAttributeMap != null) {
				Iterator<String> i = naturalAttributeMap.keySet().iterator();
				while (i.hasNext()) {
					String attributeName = i.next();
					int value = (Integer) naturalAttributeMap.get(attributeName);
					person.getNaturalAttributeManager().setAttribute(NaturalAttribute
							.valueOfIgnoreCase(attributeName), value);
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

			// 2015-06-07 Added Preference
			person.getPreference().initializePreference();

			// 2015-12-12 Added setEmotionalStates()
			//person.setEmotionalStates(emotionJSONConfig.getEmotionalStates());

		}

		// 2016-12-21 Call updateAllAssociatedPeople()
		settlement.updateAllAssociatedPeople();
		settlement.updateAllAssociatedRobots();

		//System.out.println("b4 calling createConfiguredRelationships() in UnitManager");
		// Create all configured relationships.
		createConfiguredRelationships();

	}

	/**
	 * Creates initial people based on available capacity at settlements.
	 * @throws Exception if people can not be constructed.
	 */
	private void createInitialPeople() {

		//PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
		if (relationshipManager == null)
			relationshipManager = Simulation.instance().getRelationshipManager();

		// Randomly create all remaining people to fill the settlements to capacity.
		try {
			Iterator<Settlement> i = getSettlements().iterator();
			while (i.hasNext()) {
				Settlement settlement = i.next();
				int initPop = settlement.getInitialPopulation();

				// Fill up the settlement by creating more people
				while (settlement.getCurrentPopulationNum() < initPop) {

					String sponsor = settlement.getSponsor();
			    	//System.out.println("sponsor is " + sponsor);

					//2016-08-30 Check for any duplicate full Name
					List<String> existingfullnames = new ArrayList<>();
					Iterator<Person> j = getPeople().iterator();
					while (j.hasNext()) {
						String n = j.next().getName();
						existingfullnames.add(n);
					}

					//System.out.println("done creating a namelist.");

					boolean isUniqueName = false;
					PersonGender gender = null;
					Person person = null;
					String fullname = null;
					String country = getCountry(sponsor);
					//System.out.println("country is " + country);
		    		//ReportingAuthorityType type = ReportingAuthorityType.fromString(sponsor);

					// Make sure settlement name isn't already being used.
					while (!isUniqueName) {

						isUniqueName = true;

						gender = PersonGender.FEMALE;
						if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) {
							gender = PersonGender.MALE;
						}

						String lastN = null;
						String firstN = null;

						boolean skip = false;

						List<String> last_list = new ArrayList<>();
						List<String> male_first_list = new ArrayList<>();
						List<String> female_first_list = new ArrayList<>();


						if (sponsor.contains("CNSA")) { //if (type == ReportingAuthorityType.CNSA) {
							last_list = lastNamesBySponsor.get(0);
							male_first_list = maleFirstNamesBySponsor.get(0);
							female_first_list = femaleFirstNamesBySponsor.get(0);

						} else if (sponsor.contains("CSA")) {//if (type == ReportingAuthorityType.CSA) {
							last_list = lastNamesBySponsor.get(1);
							male_first_list = maleFirstNamesBySponsor.get(1);
							female_first_list = femaleFirstNamesBySponsor.get(1);

						} else if (sponsor.contains("ESA")) {//if (type == ReportingAuthorityType.ESA) {
							//System.out.println("country is " + country);
							int countryID = getCountryID(country);
							//System.out.println("countryID is " + countryID);
							last_list = lastNamesByCountry.get(countryID);
							male_first_list = maleFirstNamesByCountry.get(countryID);
							female_first_list = femaleFirstNamesByCountry.get(countryID);

						} else if (sponsor.contains("ISRO")) {//if (type == ReportingAuthorityType.ISRO) {
							last_list = lastNamesBySponsor.get(3);
							male_first_list = maleFirstNamesBySponsor.get(3);
							female_first_list = femaleFirstNamesBySponsor.get(3);

						} else if (sponsor.contains("JAXA")) {//if (type == ReportingAuthorityType.JAXA) {
							last_list = lastNamesBySponsor.get(4);
							male_first_list = maleFirstNamesBySponsor.get(4);
							female_first_list = femaleFirstNamesBySponsor.get(4);

			    		} else if (sponsor.contains("NASA")) {//if (type == ReportingAuthorityType.NASA) {
							last_list = lastNamesBySponsor.get(5);
							male_first_list = maleFirstNamesBySponsor.get(5);
							female_first_list = femaleFirstNamesBySponsor.get(5);

						} else if (sponsor.contains("RKA")) { //if (type == ReportingAuthorityType.RKA) {
							last_list = lastNamesBySponsor.get(6);
							male_first_list = maleFirstNamesBySponsor.get(6);
							female_first_list = femaleFirstNamesBySponsor.get(6);

			    		} else { // if belonging to the Mars Society
			    			skip = true;
				    		fullname = getNewName(UnitType.PERSON, null, gender, null);
			    		}

						if (!skip) {

			    			int rand0 = RandomUtil.getRandomInt(last_list.size()-1);
			    			lastN = last_list.get(rand0);

			    			if (gender == PersonGender.MALE) {
				    			int rand1 = RandomUtil.getRandomInt(male_first_list.size()-1);
			    				firstN = male_first_list.get(rand1);
			    			}
			    			else {
				    			int rand1 = RandomUtil.getRandomInt(female_first_list.size()-1);
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
								System.out.println("UnitManager : " + fullname + " is a duplicate name. Selecting another one...");
								//break;
							}
						}

					}

					person = new Person(fullname, gender, country, settlement, sponsor); // TODO: read from file

					Mind m = person.getMind();
					// 2016-11-05 Call syncUpExtraversion() to sync up the extraversion score between the two personality models
					m.getMBTI().syncUpExtraversion();

					addUnit(person);

					relationshipManager.addInitialSettler(person, settlement);

					Favorite f = person.getFavorite();
					// 2015-02-27 and 2015-03-24 Added Favorite class
					String mainDish = f.getRandomMainDish();
					String sideDish = f.getRandomSideDish();
					String dessert = f.getRandomDessert();
					String activity = f.getRandomActivity();

					f.setFavoriteMainDish(mainDish);
					f.setFavoriteSideDish(sideDish);
					f.setFavoriteDessert(dessert);
					f.setFavoriteActivity(activity);

					// 2015-06-07 Added Preference
					person.getPreference().initializePreference();

					// 2015-06-18 Assign a job by calling getInitialJob
					m.getInitialJob(JobManager.MISSION_CONTROL);

				    // 2015-10-05 added setupReportingAuthority()
				    person.assignReportingAuthority();

					ChainOfCommand cc = settlement.getChainOfCommand();

					// 2015-04-30 Assign a role to everyone
					if (initPop >= POPULATION_WITH_MAYOR) {
						cc.set7Divisions(true);
						cc.assignSpecialiststo7Divisions(person);
					} else {
						cc.set3Divisions(true);
						cc.assignSpecialiststo3Divisions(person);
					}


				}

				// 2016-12-21 Added calling updateAllAssociatedPeople(), not getAllAssociatedPeople()()
				settlement.updateAllAssociatedPeople();
				settlement.updateAllAssociatedRobots();

				// 2015-07-02 Added setupShift()
				setupShift(settlement, initPop);

				// Establish a system of governance at settlement.
				establishSettlementGovernance(settlement);

			}

		} catch (Exception e) {
			e.printStackTrace(System.err);
			//throw new IllegalStateException("People could not be created: " + e.getMessage(), e);
		}
	}

	// 2015-04-28 Added establishCommand()
	private void establishCommand(Settlement settlement, int pop) {

		electCommanders(settlement, RoleType.COMMANDER, pop);
		// pop < POPULATION_WITH_MAYOR
		if (pop >= POPULATION_WITH_SUB_COMMANDER) {
			// electCommanders(settlement, RoleType.SUB_COMMANDER, pop);
			electChief(settlement, RoleType.CHIEF_OF_SUPPLY_N_RESOURCES);
			electChief(settlement, RoleType.CHIEF_OF_ENGINEERING);
			electChief(settlement, RoleType.CHIEF_OF_SAFETY_N_HEALTH);
		}

	}

	// 2015-05-11 Added electCommanders()
	public void electCommanders(Settlement settlement, RoleType role, int pop) {
		Collection<Person> people = settlement.getAllAssociatedPeople();
		Person cc = null;
		int cc_leadership = 0;
		int cc_combined = 0;

		Person cv = null;
		int cv_leadership = 0;
		int cv_combined = 0;
		// compare their leadership scores
		for (Person p : people) {
			NaturalAttributeManager mgr = p.getNaturalAttributeManager();
			int p_leadership = mgr.getAttribute(NaturalAttribute.LEADERSHIP);
			int p_combined = 3 * mgr.getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE)
					+ 2 * mgr.getAttribute(NaturalAttribute.EMOTIONAL_STABILITY)
					+ mgr.getAttribute(NaturalAttribute.ATTRACTIVENESS)
					+ mgr.getAttribute(NaturalAttribute.CONVERSATION);
			// if this person p has a higher leadership score than the previous
			// cc
			if (p_leadership > cc_leadership) {
				if (pop >= POPULATION_WITH_SUB_COMMANDER) {
					cv_leadership = cc_leadership;
					cv = cc;
					cv_combined = cc_combined;
				}
				cc = p;
				cc_leadership = p_leadership;
				cc_combined = p_combined;
			}
			// if this person p has the same leadership score as the previous cc
			else if (p_leadership == cc_leadership) {
				// if this person p has a higher combined score than the
				// previous cc
				if (p_combined > cc_combined) {
					// this person becomes the cc
					if (pop >= POPULATION_WITH_SUB_COMMANDER) {
						cv = cc;
						cv_leadership = cc_leadership;
						cv_combined = cc_combined;
					}
					cc = p;
					cc_leadership = p_leadership;
					cc_combined = p_combined;
				}
				/*
				 * else { // if this person p has a lower combined score than
				 * previous cc // but have a higher leadership score than the
				 * previous cv if (pop >= POPULATION_WITH_SUB_COMMANDER) { if (
				 * p_leadership > cv_leadership) { // this person p becomes the
				 * sub-commander cv = p; cv_leadership = p_leadership;
				 * cv_combined = p_combined; } else if ( p_leadership ==
				 * cv_leadership) { if ( p_combined > cv_combined) { cv = p;
				 * cv_leadership = p_leadership; cv_combined = p_combined; } } }
				 * }
				 */
			} else if (pop >= POPULATION_WITH_SUB_COMMANDER) {

				if (p_leadership > cv_leadership) {
					// this person p becomes the sub-commander
					cv = p;
					cv_leadership = p_leadership;
					cv_combined = p_combined;
				} else if (p_leadership == cv_leadership) {
					// compare person p's combined score with the cv's combined
					// score
					if (p_combined > cv_combined) {
						cv = p;
						cv_leadership = p_leadership;
						cv_combined = p_combined;
					}
				}

			}
		}
		// TODO: look at other attributes and/or skills when comparing
		// individuals
		cc.setRole(RoleType.COMMANDER);

		if (pop >= POPULATION_WITH_SUB_COMMANDER)
			cv.setRole(RoleType.SUB_COMMANDER);
	}

	/**
	 * Establish or reset the system of governance at a settlement.
	 * @param settlement the settlement.
	 */
	public void establishSettlementGovernance(Settlement settlement) {

	    int popSize = settlement.getAllAssociatedPeople().size();
	    if (popSize >= POPULATION_WITH_MAYOR) {
            establishGovernment(settlement);
        }
        else {
            establishCommand(settlement, popSize);
        }
	}


	/*
	 * Determines the number of shifts for a settlement and assigns a work shift for each person
	 * @param settlement
	 * @param pop population
	 */
	// 2015-07-02 Added setupShift()
	public void setupShift(Settlement settlement, int pop) {

		int numShift = 0;
		ShiftType shiftType = ShiftType.OFF;

		if (pop == 1) {
			numShift = 1;
		}
		else if (pop < THREE_SHIFTS_MIN_POPULATION) {
			numShift = 2;
		}
		else {//if pop >= 6
			numShift = 3;
		}

		settlement.setNumShift(numShift);
		//System.out.println(" # shift is " + numShift);

		Collection<Person> people = settlement.getAllAssociatedPeople();

		for (Person p : people) {
			//System.out.println(p.getName());
			shiftType = settlement.getAEmptyWorkShift(pop); // keep pop as a param just to speed up processing
			p.setShiftType(shiftType);
		}

		//settlement.printWorkShift("Sol 1");
	}


	// 2015-04-30 Added establishGovernment()
	private void establishGovernment(Settlement settlement) {
		electMayor(settlement, RoleType.MAYOR);

		electChief(settlement, RoleType.CHIEF_OF_AGRICULTURE);
		electChief(settlement, RoleType.CHIEF_OF_ENGINEERING);
		electChief(settlement, RoleType.CHIEF_OF_MISSION_PLANNING);
		electChief(settlement, RoleType.CHIEF_OF_SAFETY_N_HEALTH);
		electChief(settlement, RoleType.CHIEF_OF_SCIENCE);
		electChief(settlement, RoleType.CHIEF_OF_SUPPLY_N_RESOURCES);
		electChief(settlement, RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS);

	}

	// 2015-05-11 Added electMayor()
	public void electMayor(Settlement settlement, RoleType role) {
		Collection<Person> people = settlement.getAllAssociatedPeople();
		Person mayorCandidate = null;
		int m_leadership = 0;
		int m_combined = 0;
		// compare their leadership scores
		for (Person p : people) {
			NaturalAttributeManager mgr = p.getNaturalAttributeManager();
			int p_leadership = mgr.getAttribute(NaturalAttribute.LEADERSHIP);
			int p_tradeSkill = 5 * p.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.TRADING);
			p_leadership = p_leadership + p_tradeSkill;
			int p_combined = mgr.getAttribute(NaturalAttribute.ATTRACTIVENESS)
					+ 3 * mgr.getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE)
					+ mgr.getAttribute(NaturalAttribute.CONVERSATION);
			// if this person p has a higher leadership score than the previous
			// cc
			if (p_leadership > m_leadership) {
				m_leadership = p_leadership;
				mayorCandidate = p;
				m_combined = p_combined;
			}
			// if this person p has the same leadership score as the previous cc
			else if (p_leadership == m_leadership) {
				// if this person p has a higher combined score in those 4
				// categories than the previous cc
				if (p_combined > m_combined) {
					// this person becomes the cc
					m_leadership = p_leadership;
					mayorCandidate = p;
					m_combined = p_combined;
				}
			}
		}

		if (mayorCandidate != null) {
		    mayorCandidate.setRole(RoleType.MAYOR);
		}
	}

	// 2015-04-30 Added electChief()
	public void electChief(Settlement settlement, RoleType role) {
		// System.out.println("role is "+ role);
		Collection<Person> people = settlement.getAllAssociatedPeople();

		RoleType specialty = null;
		Person chief = null;
		int c_skills = 0;
		int c_combined = 0;

		SkillType skill_1 = null;
		SkillType skill_2 = null;
		SkillType skill_3 = null;
		SkillType skill_4 = null;
		if (role == RoleType.CHIEF_OF_ENGINEERING) {
			skill_1 = SkillType.MATERIALS_SCIENCE;
			skill_2 = SkillType.CONSTRUCTION;
			skill_3 = SkillType.PHYSICS;
			skill_4 = SkillType.MECHANICS;
			specialty = RoleType.ENGINEERING_SPECIALIST;
		} else if (role == RoleType.CHIEF_OF_AGRICULTURE) {
			skill_1 = SkillType.BOTANY;
			skill_2 = SkillType.BIOLOGY;
			skill_3 = SkillType.CHEMISTRY;
			skill_4 = SkillType.TRADING;
			specialty = RoleType.AGRICULTURE_SPECIALIST;
		} else if (role == RoleType.CHIEF_OF_SAFETY_N_HEALTH) {
			skill_1 = SkillType.EVA_OPERATIONS;
			skill_2 = SkillType.MEDICINE;
			skill_3 = SkillType.COOKING;
			skill_4 = SkillType.CONSTRUCTION;
			specialty = RoleType.SAFETY_SPECIALIST;
		} else if (role == RoleType.CHIEF_OF_SCIENCE) {
			skill_1 = SkillType.AREOLOGY;
			skill_2 = SkillType.CHEMISTRY;
			skill_3 = SkillType.PHYSICS;
			skill_4 = SkillType.MATHEMATICS;
			specialty = RoleType.SCIENCE_SPECIALIST;
		} else if (role == RoleType.CHIEF_OF_MISSION_PLANNING) {
			skill_1 = SkillType.MATHEMATICS;
			skill_2 = SkillType.DRIVING;
			skill_3 = SkillType.CONSTRUCTION;
			skill_4 = SkillType.EVA_OPERATIONS;
			specialty = RoleType.MISSION_SPECIALIST;
		} else if (role == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) {
			skill_1 = SkillType.TRADING;
			skill_2 = SkillType.MATHEMATICS;
			skill_3 = SkillType.BOTANY;
			skill_4 = SkillType.COOKING;
			specialty = RoleType.RESOURCE_SPECIALIST;
		} else if (role == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS) {
			skill_1 = SkillType.DRIVING;
			skill_2 = SkillType.METEOROLOGY;
			skill_3 = SkillType.AREOLOGY;
			skill_4 = SkillType.MATHEMATICS;
			specialty = RoleType.LOGISTIC_SPECIALIST;
		}

		// compare their scores
		for (Person p : people) {
			SkillManager skillMgr = p.getMind().getSkillManager();
			NaturalAttributeManager mgr = p.getNaturalAttributeManager();
			if (p.getRole().getType() == specialty) {
				// && (p.getRole().getType() != RoleType.COMMANDER)
				// && (p.getRole().getType() != RoleType.SUB_COMMANDER)) {

				int p_skills = 6 * skillMgr.getEffectiveSkillLevel(skill_1)
						+ 5 * skillMgr.getEffectiveSkillLevel(skill_2)
						+ 4 * skillMgr.getEffectiveSkillLevel(skill_3)
						+ 3 * skillMgr.getEffectiveSkillLevel(skill_4);

				int p_combined = mgr.getAttribute(NaturalAttribute.LEADERSHIP)
						+ mgr.getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE)
						+ skillMgr.getEffectiveSkillLevel(SkillType.MANAGEMENT);
				// if this person p has a higher experience score than the
				// previous cc
				if (p_skills > c_skills) {
					c_skills = p_skills;
					chief = p;
					c_combined = p_combined;
				}
				// if this person p has the same experience score as the
				// previous chief
				else if (p_skills == c_skills) {
					// if this person p has a higher combined score in those 4
					// categories than the previous chief
					if (p_combined > c_combined) {
						// this person becomes the chief
						c_skills = p_skills;
						chief = p;
						c_combined = p_combined;
					}
				}
			}
		}
		if (chief != null) {
		    chief.setRole(role);
		    // System.out.println("Chief is "+ chief.getName());
		}
	}

	/**
	 * Creates all configured Robots.
	 *
	 * @throws Exception
	 *             if error parsing XML.
	 */
	private void createPreconfiguredRobots() {
		int size = robotConfig.getNumberOfConfiguredRobots();
		// Create all configured robot.
		for (int x = 0; x < size; x++) {
			// System.out.println("x is "+ x);
			// Get robot's name (required)
			String name = robotConfig.getConfiguredRobotName(x);
			if (name == null) {
				throw new IllegalStateException("Robot name is null");
			}
			// System.out.println("name is "+ name);

			// Get robotType
			//RobotType robotType = getABot(size);
			RobotType robotType = robotConfig.getConfiguredRobotType(x);
			// System.out.println("robotType is "+ robotType.getName());

			// Get robot's settlement or randomly determine it if not
			// configured.
			String settlementName = robotConfig.getConfiguredRobotSettlement(x);
			// System.out.println("settlementName is " + settlementName);
			Settlement settlement = null;
			if (settlementName != null) {
				Collection<Settlement> col = CollectionUtils.getSettlement(units);
				// Find the settlement instance with that name
				settlement = CollectionUtils.getSettlement(col, settlementName);
				if (settlement == null) {
					// TODO: If settlement cannot be found that matches the settlement name,
					// should we put the robot in a randomly selected settlement?
					settlement = CollectionUtils.getRandomSettlement(col);
					logger.log(Level.INFO, name + " ends up going to " + settlement
							+ " since " + settlementName + " doesn't exist.");
				}
			} else {
				Collection<Settlement> col = CollectionUtils.getSettlement(units);
				settlement = CollectionUtils.getRandomSettlement(col);
				logger.log(Level.INFO, name + " has no destination settlement specified and goes to "
						+ settlementName + " by random.");
			}

			// If settlement is still null (no settlements available),
			// Don't create robot.
			if (settlement == null) {
				return;
			}


			// If settlement does not have initial robot capacity, try
			// another settlement.
			if (settlement.getInitialNumOfRobots() <= settlement.getCurrentNumOfRobots()) {
				//Iterator<Settlement> i = getSettlements().iterator();
				//Settlement newSettlement = null;
				//while (i.hasNext() && (newSettlement == null)) {
					//Settlement tempSettlement = i.next();
					//if (tempSettlement.getInitialNumOfRobots() > tempSettlement.getCurrentNumOfRobots()) {
					//	newSettlement = tempSettlement;
					//}
				//}
				//if (newSettlement != null) {
				//	settlement = newSettlement;
				//} else {
					// If no settlement with room found, don't create robot.
					return;
				//}
			}

			// 2015-03-02 Added "if (settlement != null)" to stop the last
			// instance of robot from getting overwritten
			if (settlement != null) {
				// Create robot and add to the unit manager.
				Robot robot = new Robot(name, robotType, "Mars", settlement, settlement.getCoordinates());
				addUnit(robot);
				// System.out.println("UnitManager : createConfiguredRobots() :
				// a robot is added !");
				// System.out.println("robotType is "+robotType.toString());

				// Set robot's job (if any).
				String jobName = robotConfig.getConfiguredRobotJob(x);
				// System.out.println("jobName is "+jobName);
				if (jobName != null) {
					RobotJob robotJob = JobManager.getRobotJob(robotType.getName());
					if (robotJob != null) {
						robot.getBotMind().setRobotJob(robotJob, true);
					}
				}

				// Set robot's configured natural attributes (if any).
				Map<String, Integer> attributeMap = robotConfig.getRoboticAttributeMap(x);
				if (attributeMap != null) {
					Iterator<String> i = attributeMap.keySet().iterator();
					while (i.hasNext()) {
						String attributeName = i.next();
						int value = (Integer) attributeMap.get(attributeName);
						robot.getRoboticAttributeManager()
								.setAttribute(RoboticAttribute.valueOfIgnoreCase(attributeName), value);
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
								.addNewSkill(new Skill(SkillType.valueOfIgnoreCase(skillName), // due
																								// to
																								// i18n,
																								// the
																								// keys
																								// from
																								// xml
																								// must
																								// equal
																								// the
																								// enum
																								// values,
																								// which
																								// are
																								// all
																								// upper
																								// case
										level));
					}
				}
			}
		}
	}

	/**
	 * Creates initial Robots based on available capacity at settlements.
	 * @throws Exception if Robots can not be constructed.
	 */
	private void createInitialRobots() {
		// Randomly create all remaining robots to fill the settlements to capacity.
		try {
			Iterator<Settlement> i = getSettlements().iterator();
			while (i.hasNext()) {
				Settlement settlement = i.next();
				int initial = settlement.getInitialNumOfRobots();
				while (settlement.getCurrentNumOfRobots() < initial) {
					//System.out.println("Settlement : " + settlement.getName());
					//System.out.println("getCurrentNumOfRobots() : " + settlement.getCurrentNumOfRobots());
					//System.out.println("initial : " + initial);
					// System.out.println(" getInitialNumOfRobots() : " +
					// settlement.getInitialNumOfRobots());

					// Get a robotType randomly
					RobotType robotType = getABot(settlement, initial);

					//System.out.println("robotType is "+robotType.toString());
					Robot robot = new Robot(getNewName(UnitType.ROBOT, null, null, robotType), robotType, "Mars",
							settlement, settlement.getCoordinates());

					addUnit(robot);
					// System.out.println("UnitManager : createInitialRobots() :
					// a robot is added in " + settlement);

					String jobName = RobotJob.getName(robotType);
					if (jobName != null) {
						// RobotJob robotJob = JobManager.getRobotJob(jobName);
						RobotJob robotJob = JobManager.getRobotJob(robotType.getName());
						// System.out.println("jobName is "+jobName);
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

	public RobotType getABot(Settlement s, int max) {

		int[] numBots = new int[]{0,0,0,0,0,0,0};
/*
		int numChefbot = 0;
		int numConstructionbot = 0;
		int numDeliverybot = 0;
		int numGardenbot = 0;
		int numMakerbot = 0;
		int numMedicbot = 0;
		int numRepairbot = 0;
*/

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
			//else if (numBots[6] < 1)
			//	robotType = RobotType.CONSTRUCTIONBOT;
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
/*
		int rand = RandomUtil.getRandomInt(15); // 0 to 15

		if (numChefbot < 4 && rand < 2) // 0, 1
			robotType = RobotType.CHEFBOT;
		//else if (numConstructionbot < 3 && num < 4) // 2, 3
		//	robotType = RobotType.CONSTRUCTIONBOT;
		//else if (numDeliverybot < 1 && num < 5) // 4
		//	robotType = RobotType.DELIVERYBOT;
		else if (numGardenbot < 5 && rand < 8) // 5, 6, 7
			robotType = RobotType.GARDENBOT;
		else if (numMakerbot < 6 && rand < 11) // 8, 9, 10
			robotType = RobotType.MAKERBOT;
		else if (numMedicbot < 1 && rand < 12) // 11,
			robotType = RobotType.MEDICBOT;
		else if (numRepairbot < 5 && rand < 15) // 12, 13, 14,
			robotType = RobotType.REPAIRBOT;
		else {// if a particular robottype already exceeded the limit
			RandomUtil.getRandomInt(15);
			robotType = RobotType.MAKERBOT;
		}
*/

		if (robotType == null) {
			System.out.println("robotType : null");
			robotType = RobotType.MAKERBOT;
		}
		return robotType;
	}

	/**
	 * Creates all configured people relationships.
	 *
	 * @throws Exception
	 *             if error parsing XML.
	 */
	private void createConfiguredRelationships() {
		//PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
		//RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		int size = personConfig.getNumberOfConfiguredPeople();
		// Create all configured people relationships.
		for (int x = 0; x < size; x++) {
			try {

				// Get person's name
				String name = personConfig.getConfiguredPersonName(x, PersonConfig.ALPHA_CREW);
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
	 * @param time
	 *            the amount time passing (in millisols)
	 * @throws Exception
	 *             if error during time passing.
	 */
	void timePassing(double time) {
		//logger.info("UnitManager's timePassing() is in " + Thread.currentThread().getName());
		// it's in pool-5-thread-1

		if (justReloaded) {
			Collection<Settlement> c = CollectionUtils.getSettlement(units);
			for (Settlement s : c) {
				s.updateAllAssociatedPeople();
				s.updateAllAssociatedRobots();
			}
			justReloaded = false;
		}

		Iterator<Unit> i = units.iterator();
		while (i.hasNext()) {

			Unit unit = i.next();
			if (unit instanceof Building) {
				//Building b = (Building) unit;
				//final long time0 = System.nanoTime();
				//settlementExecutor.execute(new SettlementTask(s, time));
				//b.timePassing(time);
				//final long time1 = System.nanoTime();
				//System.out.println("It takes " + (time1-time0)/1.0e3 + " milliseconds to process " + p.getName());
			}
			//else if (unit instanceof Settlement) {
			//	Settlement s = (Settlement) unit;
				//final long time0 = System.nanoTime();
			//	settlementExecutor.execute(new SettlementTask(s, time));
			//	s.timePassing(time);
				//final long time1 = System.nanoTime();
				//System.out.println("It takes " + (time1-time0)/1.0e3 + " milliseconds to process " + s.getName());
			//}
			//else if (unit instanceof Person) {
			//	Person p = (Person) unit;
				//final long time0 = System.nanoTime();
				//personExecutor.execute(new PersonTask(p, time));
			//	p.timePassing(time);
				//final long time1 = System.nanoTime();
				//System.out.println("It takes " + (time1-time0)/1.0e3 + " milliseconds to process " + p.getName());
			//}
			//else if (unit instanceof Robot) {
			//	Robot r = (Robot) unit;
				//final long time0 = System.nanoTime();
				//personExecutor.execute(new PersonTask(p, time));
			//	r.timePassing(time);
				//final long time1 = System.nanoTime();
				//System.out.println("It takes " + (time1-time0)/1.0e3 + " milliseconds to process " + p.getName());
			//}
			else
				unit.timePassing(time);

		}
/*
		if (masterClock == null)
			masterClock = Simulation.instance().getMasterClock();

		MarsClock clock = masterClock.getMarsClock();
		// check for the passing of each day
		int solElapsed = MarsClock.getSolOfYear(clock);
		if (solElapsed != solCache) {
			// reportSample = true;
			solCache = solElapsed;
			logger.info("<Benchmarking> Current Tick Per Second (TPS) : "
					+ Simulation.instance().getMasterClock().getPulsesPerSecond());
		}
*/
	}


	public class SettlementTask implements Runnable {
		Settlement s;
		double time;
		private SettlementTask(Settlement s, double time) {
			this.s = s;
			this.time = time;
		}
		@Override
		public void run() {
			try {
				s.timePassing(time);
			} catch (ConcurrentModificationException e) {
                logger.severe(e.getMessage());
			} //Exception e) {}
		}
	}

/*
	public class PersonTask implements Runnable {
		Person p;
		double time;
		private PersonTask(Person p, double time) {
			this.p = p;
			this.time = time;
		}
		@Override
		public void run() {
			try {
				p.timePassing(time);
			} catch (ConcurrentModificationException e) {} //Exception e) {}
		}
	}
*/

	/**
	 * Get number of settlements
	 *
	 * @return the number of settlements
	 */
	public int getSettlementNum() {
		return CollectionUtils.getSettlement(units).size();
	}

	/**
	 * Get settlements in virtual Mars
	 *
	 * @return Collection of settlements
	 */
	public Collection<Settlement> getSettlements() {
		return CollectionUtils.getSettlement(units);
	}

	public Settlement getASettlement() {
		List<Settlement> list = new ArrayList<>();
		list.addAll(getSettlements());
		return list.get(0);
	}

	public Settlement getFirstSettlement() {
		return firstSettlement;
	}

	/**
	 * Get number of vehicles
	 *
	 * @return the number of vehicles
	 */
	public int getVehicleNum() {
		return CollectionUtils.getVehicle(units).size();
	}

	/**
	 * Get vehicles in virtual Mars
	 *
	 * @return Collection of vehicles
	 */
	public Collection<Vehicle> getVehicles() {
		return CollectionUtils.getVehicle(units);
	}

	/**
	 * Get number of people
	 *
	 * @return the number of people
	 */
	public int getPeopleNum() {
		return CollectionUtils.getPerson(units).size();
	}

	/**
	 * Get people in virtual Mars
	 *
	 * @return Collection of people
	 */
	public Collection<Person> getPeople() {
		return CollectionUtils.getPerson(units);
	}

	/**
	 * Get number of Robots
	 *
	 * @return the number of Robots
	 */
	public int getRobotsNum() {
		return CollectionUtils.getRobot(units).size();
	}

	/**
	 * Get Robots in virtual Mars
	 *
	 * @return Collection of Robots
	 */
	public Collection<Robot> getRobots() {
		return CollectionUtils.getRobot(units);
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

	/**
	 * The total number of units
	 * @return the total number of units
	 */
	public int getUnitNum() {
		return units.size();
	}

	/**
	 * Get all units in virtual Mars
	 * @return Colleciton of units
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
	 * @param name the name to search for.
	 * @return unit or null if none.
	 */
	public Unit findUnit(String name) {
		Unit result = null;
		Iterator<Unit> i = units.iterator();
		while (i.hasNext() && (result == null)) {
			Unit unit = i.next();
			if (unit.getName().equalsIgnoreCase(name)) {
				result = unit;
			}
		}
		return result;
	}


	public ReportingAuthorityType[] getSponsors() {
		return SPONSORS;
	}

	@SuppressWarnings("restriction")
	public ObservableList<Settlement> getSettlementOList() {
		return 	FXCollections.observableArrayList(getSettlements());
	}

	public String getCountry(String sponsor) {

		if (sponsor.contains("CNSA"))//.equals(Msg.getString("ReportingAuthorityType.CNSA")))
			return "China";
		else if (sponsor.contains("CSA"))//.equals(Msg.getString("ReportingAuthorityType.CSA")))
			return "Canada";
		else if (sponsor.contains("ESA"))//.equals(Msg.getString("ReportingAuthorityType.ESA")))
			return countries.get(RandomUtil.getRandomInt(6, 27));
		else if (sponsor.contains("ISRO"))//.equals(Msg.getString("ReportingAuthorityType.ISRO")))
			return "India";
		else if (sponsor.contains("JAXA"))//.equals(Msg.getString("ReportingAuthorityType.JAXA")))
			return "Japan";
		else if (sponsor.contains("NASA"))//.equals(Msg.getString("ReportingAuthorityType.NASA")))
			return "US";
		else if (sponsor.contains("RKA"))//.equals(Msg.getString("ReportingAuthorityType.RKA")))
			return "Russia";
		else if (sponsor.contains("MS"))
			return "US";
		else
			return "US";

	}

/*
	// 2017-01-21 Add createCountryList();
	public void createCountryList() {

		countries = new ArrayList<>();

		countries.add("China"); //0
		countries.add("Canada"); //1
		countries.add("India"); //2
		countries.add("Japan"); //3
		countries.add("US"); //4
		countries.add("Russia"); //5

		countries.add("Austria");
		countries.add("Belgium");
		countries.add("Czech Republic");
		countries.add("Denmark");
		countries.add("Estonia");
		countries.add("Finland");
		countries.add("France");
		countries.add("Germany");
		countries.add("Greece");
		countries.add("Hungary");
		countries.add("Ireland");
		countries.add("Italy");
		countries.add("Luxembourg");
		countries.add("The Netherlands");
		countries.add("Norway");
		countries.add("Poland");
		countries.add("Portugal");
		countries.add("Romania");
		countries.add("Spain");
		countries.add("Sweden");
		countries.add("Switzerland");
		countries.add("UK");

	}
	*/

	public int getCountryID(String country) {
		return countries.indexOf(country);
	}


	//public ThreadPoolExecutor getPersonExecutor() {;
	//	return personExecutor;
	//}

	//public String getBuild() {
	//	return build;
	//}

	//public static void setBuild(String value) {
	//	build = value;
	//}

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
		//personExecutor = null;
		//settlementExecutor = null;
		equipmentNumberMap.clear();
		equipmentNumberMap = null;
		vehicleNumberMap.clear();
		vehicleNumberMap = null;
		//masterClock = null;
		firstSettlement = null;
		personConfig = null;
		settlementConfig = null;
		relationshipManager = null;
		//emotionJSONConfig = null;
		vehicleConfig = null;
	}
}