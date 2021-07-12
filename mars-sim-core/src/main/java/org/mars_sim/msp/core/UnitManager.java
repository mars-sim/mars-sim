/**
 * Mars Simulation Project
 * UnitManager.java
 * @version 3.2.0 2021-06-20
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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.CrewConfig;
import org.mars_sim.msp.core.person.Favorite;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.role.RoleUtil;
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotConfig;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * The UnitManager class contains and manages all units in virtual Mars. It has
 * methods for getting information about units. It is also responsible for
 * creating all units on its construction. There should be only one instance of
 * this class and it should be constructed and owned by Simulation.
 */
public class UnitManager implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(UnitManager.class.getName());

	public static final int THREE_SHIFTS_MIN_POPULATION = 6;

	public static final String EARTH = "Earth";
	
	/** True if the simulation will start out with the default alpha crew members. */
	private static boolean useCrew = true;	
	/** Flag true if the class has just been loaded */
	public static boolean justLoaded = true;
	/** Flag true if the class has just been reloaded/deserialized */
	public static boolean justReloaded = false;
	
	/** List of unit manager listeners. */
	private static CopyOnWriteArrayList<UnitManagerListener> listeners;

	private static ExecutorService executor;
	
	private static List<SettlementTask> settlementTaskList = new ArrayList<>();

	// Static members
	/** List of possible male person names. */
	private static volatile List<String> personMaleNames;
	/** List of possible female person names. */
	private static volatile List<String> personFemaleNames;

	/** Map of equipment types and their numbers. */
	private Map<String, Integer> unitCounts = new HashMap<>();
	
	private static Map<Integer, List<String>> marsSociety = new ConcurrentHashMap<>();

	private static List<String> ESACountries;
	private static List<String> allCountries;
	
	// Data members
	/** The commander's unique id . */
    public int commanderID = -1;
	/** The core engine's original build. */
	public String originalBuild;
	
	/** A map of all map display units (settlements and vehicles). */
	private volatile List<Unit> displayUnits;
	/** A map of all units with its unit identifier. */
	//private volatile Map<Integer, Unit> lookupUnit;// = new HashMap<>();
	/** A map of settlements with its unit identifier. */
	private volatile Map<Integer, Settlement> lookupSettlement;// = new HashMap<>();
	/** A map of sites with its unit identifier. */
	private volatile Map<Integer, ConstructionSite> lookupSite;
	/** A map of persons with its unit identifier. */
	private volatile Map<Integer, Person> lookupPerson;// = new HashMap<>();
	/** A map of robots with its unit identifier. */
	private volatile Map<Integer, Robot> lookupRobot;// = new HashMap<>();
	/** A map of vehicle with its unit identifier. */
	private volatile Map<Integer, Vehicle> lookupVehicle;// = new HashMap<>();
	/** A map of equipment (excluding robots and vehicles) with its unit identifier. */
	private volatile Map<Integer, Equipment> lookupEquipment;// = new HashMap<>();
	/** A map of building with its unit identifier. */
	private volatile Map<Integer, Building> lookupBuilding;// = new HashMap<>();
	
	private static SimulationConfig simulationConfig = SimulationConfig.instance();
	private static Simulation sim = Simulation.instance();
	
	private static PersonConfig personConfig;
	private static CrewConfig crewConfig;
	private static RobotConfig robotConfig;

	private static RelationshipManager relationshipManager;
	private static MalfunctionFactory factory;
	
	/** The instance of MarsSurface. */
	private MarsSurface marsSurface;

	/**
	 * Counter of unit identifiers
	 */
	private int uniqueId = 0;

	/**
	 * Constructor.
	 */
	public UnitManager() {
		// Initialize unit collection
		lookupSite       = new ConcurrentHashMap<>();
		lookupSettlement = new ConcurrentHashMap<>();
		lookupPerson     = new ConcurrentHashMap<>();
		lookupRobot      = new ConcurrentHashMap<>();
		lookupEquipment  = new ConcurrentHashMap<>();
		lookupVehicle    = new ConcurrentHashMap<>();
		lookupBuilding   = new ConcurrentHashMap<>();
		
		listeners = new CopyOnWriteArrayList<>();//Collections.synchronizedList(new ArrayList<UnitManagerListener>());
	
		personConfig = simulationConfig.getPersonConfig();	
		robotConfig = simulationConfig.getRobotConfiguration();
		crewConfig = simulationConfig.getCrewConfig();

		relationshipManager = sim.getRelationshipManager();
		factory = sim.getMalfunctionFactory();		
	}

	/**
	 * Constructs initial units.
	 *
	 * @throws Exception in unable to load names.
	 */
	synchronized void constructInitialUnits() {
		// Add marsSurface as the very first unit
		//marsSurface = new MarsSurface();
		//addUnit(marsSurface);
		
		if (ESACountries == null)
			ESACountries = personConfig.createESACountryList();

		if (allCountries == null)
			allCountries = personConfig.createAllCountryList();
		
		// Initialize name lists
		initializePersonNames();

		// Initialize the role prospect array
		RoleUtil.initialize();
	}

	/**
	 * Initializes the list of possible person names.
	 * 
	 * @throws Exception if unable to load name list.
	 */
	private void initializePersonNames() {
		try {
			List<String> personNames = personConfig.getPersonNameList();

			personMaleNames = new CopyOnWriteArrayList<String>();
			personFemaleNames = new CopyOnWriteArrayList<String>();

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
	 * Get the apporpirate Unit Map for a Unit identifier
	 * @param id
	 * @return
	 */
	private Map<Integer, ? extends Unit> getUnitMap(Integer id) {
		UnitType type = getTypeFromIdentifier(id);
		Map<Integer,? extends Unit> map = null;

		switch (type) {
		case PERSON:
			map = lookupPerson;
			break;
		case VEHICLE:
			map = lookupVehicle;
			break;
		case SETTLEMENT:
			map = lookupSettlement;
			break;
		case BUILDING:
			map = lookupBuilding;
			break;
		case EQUIPMENT:
			map = lookupEquipment;
			break;
		case ROBOT:
			map = lookupRobot;
			break;
		case CONSTRUCTION:
			map = lookupSite;
		default:
			throw new IllegalArgumentException("No Unit map for type " + type);
		}
		
		return map;
	}
	
	/**
	 * Gets the unit with a particular indentifier (unit id)
	 * 
	 * @param id indentifier
	 * @return
	 */
	public Unit getUnitByID(Integer id) {
		if (id.intValue() == Unit.MARS_SURFACE_UNIT_ID)
			return marsSurface;
		else if (id.intValue() == Unit.UNKNOWN_UNIT_ID) {
			return null;
		}
		
		Unit found = getUnitMap(id).get(id);
		if (found == null) {
			logger.warning("Unit not found " + id + " type:" + getTypeFromIdentifier(id));
		}
		return found;
	}

	public Settlement getSettlementByID(Integer id) {
		return lookupSettlement.get(id);
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
	
	public Person getPersonByID(Integer id) {
		return lookupPerson.get(id);
	}

	public Robot getRobotByID(Integer id) {
		return lookupRobot.get(id);
	}

	public Equipment getEquipmentByID(Integer id) {
		return lookupEquipment.get(id);
	}

	public Building getBuildingByID(Integer id) {
		return lookupBuilding.get(id);
	}

	public Vehicle getVehicleByID(Integer id) {
		return lookupVehicle.get(id);
	}
	
//	public Drone getDroneByID(Integer id) {
//		return lookupDrone.get(id);
//	}

	/**
	 * Adds a unit to the unit manager if it doesn't already have it.
	 *
	 * @param unit new unit to add.
	 */
	public void addUnit(Unit unit) {
//		boolean computeDisplay = false;

		if (unit != null) {
			switch(unit.getUnitType()) {
			case SETTLEMENT:
				lookupSettlement.put(unit.getIdentifier(),
			   			(Settlement) unit);
//				computeDisplay = true;
				addDisplayUnit(unit);
				break;
			case PERSON:
				lookupPerson.put(unit.getIdentifier(),
			   			(Person) unit);
				break;
			case ROBOT:
				lookupRobot.put(unit.getIdentifier(),
			   			(Robot) unit);
				break;
			case VEHICLE:
				lookupVehicle.put(unit.getIdentifier(),
			   			(Vehicle) unit);
//				computeDisplay = true;
				addDisplayUnit(unit);
				break;
			case EQUIPMENT:
				lookupEquipment.put(unit.getIdentifier(),
			   			(Equipment) unit);
				break;
			case BUILDING:
				lookupBuilding.put(unit.getIdentifier(),
						   			(Building) unit);
				break;
			case CONSTRUCTION:
				lookupSite.put(unit.getIdentifier(),
							   (ConstructionSite) unit);
				break;
			case PLANET:
				// Bit of a hack at the moment.
				// Need to revisit once extra Planets added.
				marsSurface = (MarsSurface) unit;
				break;
			default:
				throw new IllegalArgumentException("Cannot store unit type:" + unit.getUnitType());
			}

//			if (computeDisplay) {
//				// Recompute the map display units
//				computeDisplayUnits();
//			}
			
			// Fire unit manager event.
			fireUnitManagerUpdate(UnitManagerEventType.ADD_UNIT, unit);

			// If this Unit has just been built it can not contain Units.
			// Unit methods cannot be trusted during the object initiation phase
//			if (unit.getInventory() != null) {
//				Iterator<Unit> i = unit.getInventory().getContainedUnits().iterator();
//				while (i.hasNext()) {
//					addUnit(i.next());
//				}
//			}
		}
	}

	/**
	 * Removes a unit from the unit manager.
	 *
	 * @param unit the unit to remove.
	 */
	public void removeUnit(Unit unit) {
		Map<Integer,? extends Unit> map = getUnitMap(unit.getIdentifier());

		map.remove(unit.getIdentifier());

		// Fire unit manager event.
		fireUnitManagerUpdate(UnitManagerEventType.REMOVE_UNIT, unit);
	}

	/**
	 * Increment the count of the number of new unit requested.
	 * This count is independent of the actual Units held in the manager.
	 * @param name
	 * @return
	 */
	public int incrementTypeCount(String name) {
		synchronized (unitCounts) {
			return unitCounts.merge(name, 1, (a, b) -> a + b);
		}
	}	


	/**
	 * Creates all pre-configured people as listed in people.xml.
	 * 
	 * @throws Exception if error parsing XML.
	 */
	private void createPreconfiguredPeople() {
		Settlement settlement = null;

		List<Person> personList = new CopyOnWriteArrayList<>();
		
		if (personConfig == null) // FOR PASSING MAVEN TEST
			personConfig = SimulationConfig.instance().getPersonConfig();

		if (crewConfig == null) // FOR PASSING MAVEN TEST
			crewConfig = SimulationConfig.instance().getCrewConfig();
		
		// TODO: will setting a limit on # crew to 7 be easier ?

		// Get crew ID
		int crewID = crewConfig.getSelectedCrew();
		
		int size = crewConfig.getNumberOfConfiguredPeople(crewID);

		// Create all configured people.
		for (int x = 0; x < size; x++) {
	
//			String crewName = crewConfig.getConfiguredPersonCrew(x, crew_id, false);
			
			// Get person's name (required)
			String name = crewConfig.getConfiguredPersonName(x, crewID, false);

			// Get person's gender or randomly determine it if not configured.
			GenderType gender = crewConfig.getConfiguredPersonGender(x, crewID, false);
			if (gender == null) {
				gender = GenderType.FEMALE;
				if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) {
					gender = GenderType.MALE;
				}
			}
			
//			if (name == null
//				throw new IllegalStateException("Person name is null");
			

			// Get person's settlement or randomly determine it if not configured.
			String preConfigSettlementName = crewConfig.getConfiguredPersonDestination(x, crewID, false);
			if (preConfigSettlementName != null) {
				settlement = CollectionUtils.getSettlement(lookupSettlement.values(), preConfigSettlementName);
				if (settlement == null) {
					// Note: if settlement cannot be found that matches the settlement name,
					// do NOT use this member
//					settlement = CollectionUtils.getRandomSettlement(col);
					logger.log(Level.CONFIG, "Alpha crew member '" + name + "' has the designated settlement called '" 
						+ preConfigSettlementName + "' but it doesn't exist.");
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
				Iterator<Settlement> i = lookupSettlement.values().iterator();
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
			
			// Get person's age
			int age = 0;
			String ageStr = crewConfig.getConfiguredPersonAge(x, crewID, false);
			if (ageStr == null)
				age = RandomUtil.getRandomInt(21, 65);
			else
				age = Integer.parseInt(ageStr);	

			// Retrieve country & sponsor designation from people.xml (may be edited in
			// CrewEditorFX)
			ReportingAuthorityType sponsor = crewConfig.getConfiguredPersonSponsor(x, crewID, false);
			String country = crewConfig.getConfiguredPersonCountry(x, crewID, false);

			// Loads the person's preconfigured skills (if any).
			Map<String, Integer> skillMap = crewConfig.getSkillMap(x, crewID);
		
			// Set the person's configured Big Five Personality traits (if any).
			Map<String, Integer> bigFiveMap = crewConfig.getBigFiveMap(x);

			// Override person's personality type based on people.xml, if any.
			String mbti = crewConfig.getConfiguredPersonPersonalityType(x, crewID, false);
			
			// Set person's configured natural attributes (if any).
			Map<String, Integer> attributeMap = crewConfig.getNaturalAttributeMap(x);
			
			// Create person and add to the unit manager.
			// Use Builder Pattern for creating an instance of Person
			Person person = Person.create(name, settlement)
					.setGender(gender)
					.setAge(age)
					.setCountry(country)
					.setSponsor(sponsor)
					.setSkill(skillMap)
					.setPersonality(bigFiveMap, mbti)
					.setAttribute(attributeMap)
					.build();
			
			person.initialize();
	
			// Set the person as a preconfigured crew member
			person.setPreConfigured(true);

			personList.add(person);

			relationshipManager.addInitialSettler(person, settlement);

			// Set person's job (if any).
			String jobName = crewConfig.getConfiguredPersonJob(x, crewID, false);
			if (jobName != null) {
				JobType job = JobType.getJobTypeByName(jobName);
				if (job != null) {
					// Designate a specific job to a person
					person.getMind().assignJob(job, true, JobUtil.MISSION_CONTROL, JobAssignmentType.APPROVED,
							JobUtil.MISSION_CONTROL);
					// Assign a job to a person based on settlement's need
				}
			}

			// Add Favorite class
			String mainDish = crewConfig.getFavoriteMainDish(x, crewID);
			String sideDish = crewConfig.getFavoriteSideDish(x, crewID);
			String dessert = crewConfig.getFavoriteDessert(x, crewID);
			String activity = crewConfig.getFavoriteActivity(x, crewID);

			// Add Favorite class
			Favorite f = person.getFavorite();
			
			if (mainDish != null) {
				f.setFavoriteMainDish(mainDish);
			}
			
			if (sideDish != null) {
				f.setFavoriteSideDish(sideDish);
			}
			
			if (dessert != null) {
				f.setFavoriteDessert(dessert);
			}	

			if (activity != null) {
				f.setFavoriteActivity(activity);
			}	

			// Initialize Preference
			person.getPreference().initializePreference();

			// Initialize emotional states
			// person.setEmotionalStates(emotionJSONConfig.getEmotionalStates());
			
			addUnit(person);
		}

		// Create all configured relationships.
		createConfiguredRelationships(personList);
	}
	
	public void setCommanderId(int commanderID) {
		this.commanderID = commanderID;
	}
	public int getCommanderID() {
		return commanderID;
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
		Collection<Settlement> col = new CopyOnWriteArrayList<>(lookupSettlement.values());//CollectionUtils.getSettlement(units);
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
//					String templateName = settlement.getTemplate();

					boolean proceed = true;

//					if (jobName.equalsIgnoreCase("Gardener") && templateName.equals("Trading Outpost"))
//						proceed = false;
//
//					if (jobName.equalsIgnoreCase("Gardener") && templateName.equals("Mining Outpost"))
//						proceed = false;

					if (proceed) {
						// Create robot and add to the unit manager.
			
						// Set robot's configured skills (if any).
						Map<String, Integer> skillMap = robotConfig.getSkillMap(x);
						
						// Set robot's configured natural attributes (if any).
						Map<String, Integer> attributeMap = robotConfig.getRoboticAttributeMap(x);
						
						// Adopt Static Factory Method and Factory Builder Pattern
						Robot robot = Robot.create(name, settlement, robotType)
								.setCountry(EARTH)
								.setSkill(skillMap, robotType)
								.setAttribute(attributeMap)
								.build();
						robot.initialize();

						numBots++;

						if (isDestinationChange) {

							RobotJob robotJob = JobUtil.getRobotJob(robotType.getName());
							if (robotJob != null) {
								robot.getBotMind().setRobotJob(robotJob, true);
							}
						}
						
						addUnit(robot);
					}
				}
			}
		}
	}

	/**
	 * Creates all configured people relationships.
	 *
	 * @throws Exception if error parsing XML.
	 */
	private void createConfiguredRelationships(List<Person> personList) {

		if (crewConfig == null) // FOR PASSING MAVEN TEST
			crewConfig = SimulationConfig.instance().getCrewConfig();
		
		// Get crew ID
		int crewID = crewConfig.getSelectedCrew();
		
		int size = crewConfig.getNumberOfConfiguredPeople(crewID);
			
		// Create all configured people relationships.
		for (int x = 0; x < size; x++) {
			try {

				// Get the person
				Person person = personList.get(x);
				
				// Set person's configured relationships (if any).
				Map<String, Integer> relationshipMap = crewConfig.getRelationshipMap(x, crewID);
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
							throw new IllegalStateException("'" + relationshipName + "' not found.");
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
	 * @param pulse the amount time passing (in millisols)
	 * @throws Exception if error during time passing.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {	
		if (pulse.isNewSol() || justLoaded) {			
			// Compute reliability daily
			factory.computeReliability();
			justLoaded = false;
		}

		if (pulse.getElapsed() > 0) {
			runExecutor(pulse);
		}
		else {
			logger.warning("Zero elapsed pulse #" + pulse.getId());
		}
		
		return true;
	}
	
	/**
	 * Sets up executive service
	 */
	private void setupExecutor() {
		if (executor == null) {
			int size = (int)(getSettlementNum()/2D);
			int num = Math.min(size, Simulation.NUM_THREADS - simulationConfig.getUnusedCores());
			if (num <= 0) num = 1;
			logger.config("Setting up " + num + " thread(s) for running the settlement update.");
			executor = Executors.newFixedThreadPool(num,
					new ThreadFactoryBuilder().setNameFormat("unitmanager-thread-%d").build());
		}
	}
	
	/**
	 * Sets up settlement tasks for executive service
	 */
	private void setupTasks() {
		if (settlementTaskList == null || settlementTaskList.isEmpty()) {
			settlementTaskList = new CopyOnWriteArrayList<>();
			lookupSettlement.values().forEach(s -> activateSettlement(s));
		}
	}
	
	public void activateSettlement(Settlement s) {
		if (!lookupSettlement.containsKey(s.getIdentifier())) {
			throw new IllegalStateException("Do not know new Settlement "
						+ s.getName());
		}
		
		SettlementTask st = new SettlementTask(s);
		settlementTaskList.add(st);
	}
	
	/**
	 * Fires the clock pulse to each clock listener
	 * 
	 * @param pulse
	 */
	private void runExecutor(ClockPulse pulse) {
		setupExecutor();
		setupTasks();
		settlementTaskList.forEach(s -> {
			s.setCurrentPulse(pulse);
		});

		// Execute all listener concurrently and wait for all to complete before advancing
		// Ensure that Settlements stay synch'ed and some don't get ahead of others as tasks queue
		try {
			List<Future<String>> results = executor.invokeAll(settlementTaskList);
			for (Future<String> future : results) {
				future.get();
			};
		} 
		catch (ExecutionException ee) {
			// Problem running the pulse
			ee.printStackTrace();
		}
		catch (InterruptedException ie) {
			// Program probably exiting
			if (executor.isShutdown()) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	/**
	 * Ends the current executor
	 */
	public void endSimulation() {
		if (executor != null)
			executor.shutdownNow();
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
		if (lookupSettlement != null && !lookupSettlement.isEmpty()) {
			return Collections.unmodifiableCollection(lookupSettlement.values());//CollectionUtils.getSettlement(units); 
		}
		else {
//			logger.severe("lookupSettlement is null.");
			return new ArrayList<>();
		}
	}

	/**
	 * Get vehicles in virtual Mars
	 *
	 * @return Collection of vehicles
	 */
	public Collection<Vehicle> getVehicles() {
		return Collections.unmodifiableCollection(lookupVehicle.values());//CollectionUtils.getVehicle(units);
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
		return Collections.unmodifiableCollection(lookupPerson.values());//CollectionUtils.getPerson(units);
	}

	/**
	 * Get Robots in virtual Mars
	 *
	 * @return Collection of Robots
	 */
	public Collection<Robot> getRobots() {
		return Collections.unmodifiableCollection(lookupRobot.values());//CollectionUtils.getRobot(units);
	}

	public List<Unit> findDisplayUnits() {
		List<Unit> units = new ArrayList<>();
		Collection<Settlement> settlements = lookupSettlement.values();
		units.addAll(settlements);
		for (Settlement s: settlements) {
			units.addAll(s.getMissionVehicles());
		}
		displayUnits = units;
		return units;	
	}
	
	private void addDisplayUnit(Unit unit) {
		if (displayUnits == null)
			displayUnits = new ArrayList<>();
		
		displayUnits.add(unit);
	}
	
	/**
	 * Obtains the settlement and vehicle units for map display
	 * @return
	 */
	public List<Unit> getDisplayUnits() {
		return displayUnits; //findDisplayUnits();	
	}

	/**
	 * Adds a unit manager listener
	 * 
	 * @param newListener the listener to add.
	 */
	public final void addUnitManagerListener(UnitManagerListener newListener) {
		if (listeners == null) {
			listeners = new CopyOnWriteArrayList<>();//Collections.synchronizedList(new ArrayList<UnitManagerListener>());
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
			listeners = new CopyOnWriteArrayList<>();//Collections.synchronizedList(new ArrayList<UnitManagerListener>());
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
			listeners = new CopyOnWriteArrayList<>();//Collections.synchronizedList(new ArrayList<UnitManagerListener>());
		}
		synchronized (listeners) {
			for (UnitManagerListener listener : listeners) {
				listener.unitManagerUpdate(new UnitManagerEvent(this, eventType, unit));
			}
		}
	}


	/**
	 * Obtains the country id. If none, return -1.
	 * 
	 * @param country
	 * @return
	 */
	public static List<String> getAllCountryList() {
		if (personConfig == null)
			personConfig = SimulationConfig.instance().getPersonConfig();
		if (allCountries == null)
			allCountries = personConfig.createAllCountryList();
		return allCountries;
	}
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 */
	public void reinit(MarsClock clock) {
		
		for (Person p: lookupPerson.values()) {
			p.reinit();
		}
		for (Robot r: lookupRobot.values()) {
			r.reinit();
		}
		for (Building b: lookupBuilding.values()) {
			b.reinit();
		}
		for (Settlement s: lookupSettlement.values()) {
			s.reinit();
		}
		
		// Sets up the executor
		setupExecutor();
		// Sets up the concurrent tasks
		setupTasks();
	}
	
	/**
	 * Returns Mars surface instance
	 * 
	 * @return {@Link MarsSurface}
	 */
	public MarsSurface getMarsSurface() {
		return marsSurface;
	}
	
	public static void setCrew(boolean value) {
		useCrew = value;
	}

	public static boolean getCrew() {
		return useCrew;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		Iterator<Settlement> i1 = lookupSettlement.values().iterator();
		while (i1.hasNext()) {
			i1.next().destroy();
		}
		Iterator<ConstructionSite> i0 = lookupSite.values().iterator();
		while (i0.hasNext()) {
			i0.next().destroy();
		}
		Iterator<Vehicle> i2 = lookupVehicle.values().iterator();
		while (i2.hasNext()) {
			i2.next().destroy();
		}
		Iterator<Building> i3 = lookupBuilding.values().iterator();
		while (i3.hasNext()) {
			i3.next().destroy();
		}
		Iterator<Person> i4 = lookupPerson.values().iterator();
		while (i4.hasNext()) {
			i4.next().destroy();
		}
		Iterator<Robot> i5 = lookupRobot.values().iterator();
		while (i5.hasNext()) {
			i5.next().destroy();
		}
		Iterator<Equipment> i6 = lookupEquipment.values().iterator();
		while (i6.hasNext()) {
			i6.next().destroy();
		}
	
		lookupSite.clear();
		lookupSettlement.clear();
		lookupVehicle.clear();
		lookupBuilding.clear();
		lookupPerson.clear();
		lookupRobot.clear();
		lookupEquipment.clear();
		
		lookupSite = null;
		lookupSettlement = null;
		lookupVehicle = null;
		lookupBuilding = null;
		lookupPerson = null;
		lookupRobot = null;
		lookupEquipment = null;

		sim = null;
		simulationConfig = SimulationConfig.instance();
		marsSurface = null;
		
		personMaleNames = null;
		personFemaleNames = null;
		listeners.clear();
		listeners = null;
		
		personConfig = null;
		crewConfig = null;
		robotConfig = null;
		
		relationshipManager = null;
		factory = null;
	}
	
	/**
	 * Prepares the Settlement task for setting up its own thread.
	 */
	class SettlementTask implements Callable<String> {
		private Settlement settlement;
		private ClockPulse currentPulse;
		
		protected Settlement getSettlement() {
			return settlement;
		}
		
		public void setCurrentPulse(ClockPulse pulse) {
			this.currentPulse = pulse;
		}

		private SettlementTask(Settlement settlement) {
			this.settlement = settlement;
		}

		@Override
		public String call() throws Exception {
			settlement.timePassing(currentPulse);	
			return settlement.getName() + " completed pulse #" + currentPulse.getId();
		}
	}

	/**
	 * Extracts the UnitType from an identifier
	 * @param id
	 * @return
	 */
	public static UnitType getTypeFromIdentifier(int id) {
		// Extract the bottom 8 bit
		int typeId = (id & 255);
		
		return UnitType.values()[typeId];
		}
	
	/**
	 * Generate a new unique UnitId for a certain type. This will be used later
	 * for lookups.
	 * The lowest 8 bits contain the ordinal of the UnitType. Top remaining bits 
	 * are a unique increasing number.
	 * This guarantees 
	 * uniqueness PLUS a quick means to identify the UnitType from only the 
	 * identifier.
	 * @param unitType
	 * @return
	 */
	public synchronized int generateNewId(UnitType unitType) {
		int baseId = uniqueId++;
		int typeId = unitType.ordinal();
		
		return (baseId << 8) + typeId;
	}

}
