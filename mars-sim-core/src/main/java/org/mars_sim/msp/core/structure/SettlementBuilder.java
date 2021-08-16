/**
 * Mars Simulation Project
 * SettlementConfig.java
 * @version 3.2.0 2021-07-10
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.apache.commons.lang3.time.StopWatch;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.configuration.Scenario;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Crew;
import org.mars_sim.msp.core.person.CrewConfig;
import org.mars_sim.msp.core.person.Favorite;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Member;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotConfig;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.goods.CreditManager;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * This class will create new complete Settlements from a template.
 * The creation includes all Persons, Vehicles & Robots.
 */
public final class SettlementBuilder {
	private static SimLogger logger = SimLogger.getLogger(SettlementBuilder.class.getName());
	
	public static final String EARTH = "Earth";
	
	// Change this to fore details time measurement on creation
	private static final boolean MEASURE_PHASES = false;
	
	private UnitManager unitManager;
	private RelationshipManager relationshipManager;
	private CreditManager creditManager;

	private SettlementConfig settlementConfig;
	private PersonConfig personConfig;
	private RobotConfig robotConfig;
	private CrewConfig crewConfig;

	public SettlementBuilder(Simulation sim, SimulationConfig simConfig) {
		super();
		this.unitManager = sim.getUnitManager();
		this.relationshipManager = sim.getRelationshipManager();
		this.creditManager = sim.getCreditManager();
		this.settlementConfig = simConfig.getSettlementConfiguration();
		this.personConfig = simConfig.getPersonConfig();
		this.robotConfig = simConfig.getRobotConfiguration();
	}

	/**
	 * Create all the initial Settlements
	 */
	public void createInitialSettlements(Scenario bootstrap) {
		logger.config("Scenario " + bootstrap.getName() + " loading");
		for (InitialSettlement spec : bootstrap.getSettlements()) {
			createFullSettlement(spec);
		}
		
		// If loading full default and game mode then place the Commander
		if (GameManager.mode == GameMode.COMMAND) {
			GameManager.placeInitialCommander(unitManager);
		}
	}
	
	/**
	 * This create a single fully populated Settlement according to the
	 * specification. This includes all sub-units, e.g. Vehicles & Persons
	 * along with any initila Parts & Resources.
	 * @param spec
	 * @return
	 */
	public Settlement createFullSettlement(InitialSettlement spec) {
		SettlementTemplate template = settlementConfig.getSettlementTemplate(spec.getSettlementTemplate());
		logger.config("Creating " + spec.getName() + " based on template " + spec.getSettlementTemplate());

		StopWatch watch = new StopWatch();
		watch.start();

		Settlement settlement = createSettlement(template, spec);
		outputTimecheck(settlement, watch, "Create Settlement");

		createVehicles(template, settlement);
		outputTimecheck(settlement, watch, "Create Vehicles");

		createEquipment(template, settlement);
		outputTimecheck(settlement, watch, "Create Equipment");

		createResources(template, settlement);
		outputTimecheck(settlement, watch, "Create Resources");

		createParts(template, settlement);
		outputTimecheck(settlement, watch, "Create Parts");

		// TOCO get off the Initial Settlement
		String crew = spec.getCrew();
		
		// Create pre-configured robots as stated in robots.xml
		if (crewConfig != null) {
			createPreconfiguredRobots(settlement);
			outputTimecheck(settlement, watch, "Create Preconfigured Robots");
		}
		
		// Create more robots to fill the settlement(s)
		createRobots(settlement);
		outputTimecheck(settlement, watch, "Create Robots");

		// Create settlers to fill the settlement(s)
		if ((crew != null) && (crewConfig != null)) {
			createPreconfiguredPeople(settlement, crew);
			outputTimecheck(settlement, watch, "Create Preconfigured People");
		}
		createPeople(settlement);
		outputTimecheck(settlement, watch, "Create People");

		// Manually add job positions
		settlement.tuneJobDeficit();
		outputTimecheck(settlement, watch, "Tune Job");

		// Add new settlement to credit manager.
		creditManager.addSettlement(settlement);
		
		watch.stop();
		if (MEASURE_PHASES) {
			logger.config(settlement, "Fully created in " + watch.getTime());
		}
		
		return settlement;
	}


	private static void outputTimecheck(Settlement settlement, StopWatch watch, String phase) {
		if (MEASURE_PHASES) {
			watch.split();
			logger.config(settlement, phase + " took " + watch.getTime() + " ms");
			watch.unsplit();
		}
	}

	private Settlement createSettlement(SettlementTemplate template, InitialSettlement spec) {
		String sponsor = spec.getSponsor();
		// Fi the sponsor has not be defined; then use the template
		if (sponsor == null) {
			sponsor = template.getSponsor();
		}
		ReportingAuthority ra = ReportingAuthorityFactory.getAuthority(sponsor);
		
		// Get settlement name
		String name = spec.getName();
		if (name == null) {
			name = Settlement.generateName(ra);
		}

		// Get settlement longitude
		Coordinates location = spec.getLocation();
		if (location == null) {
			double longitude = Coordinates.getRandomLongitude();
			double latitude = Coordinates.getRandomLatitude();
			location = new Coordinates(latitude, longitude);
		}

		int populationNumber = spec.getPopulationNumber();
		int initialNumOfRobots = spec.getNumOfRobots();

		// Add scenarioID
		int scenarioID = template.getID();
		Settlement settlement = Settlement.createNewSettlement(name, scenarioID,
									spec.getSettlementTemplate(), ra,
									location, populationNumber,
									initialNumOfRobots);
		settlement.initialize();
		unitManager.addUnit(settlement);
	
		return settlement;
	}
	
	private void createVehicles(SettlementTemplate template, Settlement settlement) {
		Map<String, Integer> vehicleMap = template.getVehicles();
		Iterator<String> j = vehicleMap.keySet().iterator();
		ReportingAuthority sponsor = settlement.getSponsor();
		while (j.hasNext()) {
			String vehicleType = j.next();
			int number = vehicleMap.get(vehicleType);
			vehicleType = vehicleType.toLowerCase();
			for (int x = 0; x < number; x++) {
				String name = Vehicle.generateName(vehicleType, sponsor);
				if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType)) {
					LightUtilityVehicle luv = new LightUtilityVehicle(name, vehicleType, settlement);
					unitManager.addUnit(luv);	
				} 
				else if (VehicleType.DELIVERY_DRONE.getName().equalsIgnoreCase(vehicleType)) {
					Drone drone = new Drone(name, vehicleType, settlement);
					unitManager.addUnit(drone);
				}
				else {
					Rover rover = new Rover(name, vehicleType, settlement);
					unitManager.addUnit(rover);
				}
			}
		}
	}
	

	/**
	 * Creates the initial equipment at a settlement.
	 *
	 * @throws Exception if error constructing equipment.
	 */
	private void createEquipment(SettlementTemplate template, Settlement settlement) {
		Map<String, Integer> equipmentMap = template.getEquipment();
		for (String type : equipmentMap.keySet()) {
			int number = equipmentMap.get(type);
			for (int x = 0; x < number; x++) {
				Equipment equipment = EquipmentFactory.createEquipment(type, settlement,
						false);
				settlement.addOwnedEquipment(equipment);
				unitManager.addUnit(equipment);
			}
		}
	}

	/**
	 * Creates initial Robots based on available capacity at settlements.
	 * 
	 * @throws Exception if Robots can not be constructed.
	 */
	private void createRobots(Settlement settlement) {
		// Randomly create all remaining robots to fill the settlements to capacity.
		int initial = settlement.getProjectedNumOfRobots();
		// Note : need to call updateAllAssociatedRobots() first to compute numBots in Settlement
		while (settlement.getIndoorRobotsCount() < initial) {
			// Get a robotType randomly
			RobotType robotType = Robot.selectNewRobotType(settlement);
			// Adopt Static Factory Method and Factory Builder Pattern
			String newName = Robot.generateName(robotType);
			Robot robot = Robot.create(newName, settlement, robotType)
					.setCountry(EARTH)
					.setSkill(null, robotType)
					.setAttribute(null)
					.build();
			robot.initialize();
			// Set name at its parent class "Unit"
			robot.setName(newName);
			
			String jobName = RobotJob.getName(robotType);
			if (jobName != null) {
				RobotJob robotJob = JobUtil.getRobotJob(robotType.getName());
				if (robotJob != null) {
					robot.getBotMind().setRobotJob(robotJob, true);
				}
			}
			
			unitManager.addUnit(robot);
		}
	}

	/**
	 * Creates the initial resources at a settlement. Note: This is in addition to
	 * any initial resources set in buildings.
	 *
	 * @throws Exception if error storing resources.
	 */
	private void createResources(SettlementTemplate template, Settlement settlement) {
		Inventory inv = settlement.getInventory();

		Map<AmountResource, Double> resourceMap = template.getResources();
		for (Entry<AmountResource, Double> value : resourceMap.entrySet()) {
			AmountResource resource = value.getKey();
			double amount = value.getValue();
			double capacity = inv.getAmountResourceRemainingCapacity(resource, true, false);
			if (amount > capacity)
				amount = capacity;
			inv.storeAmountResource(resource, amount, true);
		}
	}

	/**
	 * Create initial parts for a settlement.
	 *
	 * @throws Exception if error creating parts.
	 */
	private void createParts(SettlementTemplate template, Settlement settlement) {
		Inventory inv = settlement.getInventory();

		Map<Part, Integer> partMap = template.getParts();
		for (Entry<Part, Integer> item : partMap.entrySet()) {
			Part part = item.getKey();
			Integer number = item.getValue();
			inv.storeItemResources(part.getID(), number);
		}
	}

	/**
	 * Creates initial people based on available capacity at settlements.
	 * 
	 * @throws Exception if people can not be constructed.
	 */
	private void createPeople(Settlement settlement) {

		int initPop = settlement.getInitialPopulation();
		ReportingAuthority sponsor = settlement.getSponsor();

		// Fill up the settlement by creating more people
		while (settlement.getIndoorPeopleCount() < initPop) {
			
			GenderType gender = GenderType.FEMALE;
			if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) {
				gender = GenderType.MALE;
			}
			Person person = null;
			
			// This is random and may change on each call
			String country = ReportingAuthorityFactory.getDefaultCountry(sponsor);

			// Make sure settlement name isn't already being used.
			String fullname = Person.generateName(country, gender);

			// Use Builder Pattern for creating an instance of Person
			person = Person.create(fullname, settlement)
					.setGender(gender)
					.setCountry(country)
					.setSponsor(sponsor)
					.setSkill(null)
					.setPersonality(null, null)
					.setAttribute(null)
					.build();
			
			person.initialize();
			unitManager.addUnit(person);

			relationshipManager.addInitialSettler(person, settlement);

			// Set up preference
			person.getPreference().initializePreference();

			// Assign a job 
			person.getMind().getInitialJob(JobUtil.MISSION_CONTROL);
		}

		// Set up work shift
		unitManager.setupShift(settlement, initPop);
		
		// Establish a system of governance at a settlement.
		settlement.getChainOfCommand().establishSettlementGovernance();
	}

	/**
	 * Creates all pre-configured people as listed in people.xml.
	 */
	private void createPreconfiguredPeople(Settlement settlement, String crewName) {

		Crew crew = crewConfig.getItem(crewName);
		if (crew == null) {
			throw new IllegalArgumentException("No crew defined called " + crewName);
		}
		
		Map<Person, Map<String, Integer>> addedCrew = new HashMap<>();
		
		// Create all configured people.
		for (Member m : crew.getTeam()) {
			if (settlement.getInitialPopulation() > settlement.getNumCitizens()) {
	
				// Get person's settlement or same sponsor
				ReportingAuthority sponsor = settlement.getSponsor();
				if (m.getSponsorCode() != null) {
					 sponsor = ReportingAuthorityFactory.getAuthority(
											m.getSponsorCode());
				}
	
				String name = m.getName();
				logger.log(Level.INFO, name + " from crew '" + crew.getName() + "' assigned to Settlement " + settlement.getName());
					
				// Get person's gender or randomly determine it if not configured.
				GenderType gender = m.getGender();
				if (gender == null) {
					gender = GenderType.FEMALE;
					if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) {
						gender = GenderType.MALE;
					}
				}
		
				// Get person's age
				int age = 0;
				String ageStr = m.getAge();
				if (ageStr != null)
					age = Integer.parseInt(ageStr);	
				else
					age = RandomUtil.getRandomInt(21, 65);
	
				// Retrieve country & sponsor designation from people.xml (may be edited in
				// CrewEditorFX)
				String country = m.getCountry();
	
				// Loads the person's preconfigured skills (if any).
				Map<String, Integer> skillMap = m.getSkillMap();
				
				// Set the person's configured Big Five Personality traits (if any).
				Map<String, Integer> bigFiveMap = new HashMap<>(); //TOOO
		
				// Override person's personality type based on people.xml, if any.
				String mbti = m.getMBTI();
				
				// Set person's configured natural attributes (if any).
				Map<String, Integer> attributeMap = new HashMap<>();
				
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
				unitManager.addUnit(person);
		
				// Set the person as a preconfigured crew member
				person.setPreConfigured(true);
				Map<String, Integer> relMap = m.getRelationshipMap();
				if (relMap != null) {
					addedCrew.put(person, relMap);
				}
				relationshipManager.addInitialSettler(person, settlement);
		
				// Set person's job (if any).
				String jobName = m.getJob();
				if (jobName != null) {
					JobType job = JobType.getJobTypeByName(jobName);
					if (job != null) {
						// Designate a specific job to a person
						person.getMind().assignJob(job, true, JobUtil.MISSION_CONTROL, JobAssignmentType.APPROVED,
								JobUtil.MISSION_CONTROL);
					}
				}
	
				// Add Favorite class
				String mainDish = m.getMainDish();
				String sideDish = m.getSideDish();
				String dessert = m.getDessert();
				String activity = m.getActivity();
		
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
			}
		}
		
		createConfiguredRelationships(addedCrew);
	}
	

	/**
	 * Creates all configured pre-configured crew relationships
	 * @param addedCrew 
	 */
	private void createConfiguredRelationships(Map<Person, Map<String, Integer>> addedCrew) {
	
		// Create all configured people relationships.
		for(Entry<Person, Map<String, Integer>> p : addedCrew.entrySet()) {
			Person person = p.getKey();
				
			// Set person's configured relationships (if any).
			Map<String, Integer> relationshipMap = p.getValue();
			for (Entry<String, Integer> friend : relationshipMap.entrySet()) {
				String friendName = friend.getKey();

				// Get the other people in the same settlement in the relationship.
				for (Person potentialFriend : addedCrew.keySet()) {
					if (potentialFriend.getName().equals(friendName)) {
						int opinion = friend.getValue();

						// Set the relationship opinion.
						Relationship relationship = relationshipManager.getRelationship(person, potentialFriend);
						if (relationship != null) {
							relationship.setPersonOpinion(person, opinion);
						} else {
							relationshipManager.addRelationship(person, potentialFriend,
									Relationship.EXISTING_RELATIONSHIP);
							relationship = relationshipManager.getRelationship(person, potentialFriend);
							relationship.setPersonOpinion(person, opinion);
						}
					}
				}
			}
		}
	}

	/**
	 * Creates all configured Robots.
	 *
	 * @throws Exception if error parsing XML.
	 */
	private void createPreconfiguredRobots(Settlement settlement) {
		int size = robotConfig.getNumberOfConfiguredRobots();

		for (int x = 0; x < size; x++) {
			String preConfigSettlementName = robotConfig.getConfiguredRobotSettlement(x);
			if (settlement.getName().equals(preConfigSettlementName)
					&& (settlement.getNumBots() <= settlement.getProjectedNumOfRobots())) {
				// Get robot's name (required)
				String name = robotConfig.getConfiguredRobotName(x);

				// Get robotType
				RobotType robotType = robotConfig.getConfiguredRobotType(x);

				// Set robot's job (if any).
				String jobName = robotConfig.getConfiguredRobotJob(x);
				if (jobName != null) {
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

					unitManager.addUnit(robot);
				}
			}
		}
	}
	
	/**
	 * Enable the use of a predefined crews
	 */
	public void setCrew(CrewConfig crewConfig) {
		this.crewConfig = crewConfig;
	}
}
