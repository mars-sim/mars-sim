/*
 * Mars Simulation Project
 * SettlementBuilder.java
 * @date 2023-07-30
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;

import com.mars_sim.core.Entity;
import com.mars_sim.core.GameManager;
import com.mars_sim.core.GameManager.GameMode;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.configuration.Scenario;
import com.mars_sim.core.configuration.UserConfigurableConfig;
import com.mars_sim.core.equipment.BinFactory;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Crew;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Member;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.NationSpec;
import com.mars_sim.core.person.NationSpecConfig;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.fav.Favorite;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.AssignmentType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.job.util.JobUtil;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.role.RoleUtil;
import com.mars_sim.core.person.ai.social.RelationshipType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotConfig;
import com.mars_sim.core.robot.RobotDemand;
import com.mars_sim.core.robot.RobotSpec;
import com.mars_sim.core.robot.RobotTemplate;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.robot.ai.job.RobotJob;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.VehicleFactory;

/**
 * This class will create new complete Settlements from a template.
 * The creation includes all Persons, Vehicles & Robots.
 */
public final class SettlementBuilder {
	
	private static SimLogger logger = SimLogger.getLogger(SettlementBuilder.class.getName());

	// Change this to fore details time measurement on creation
	private static final boolean MEASURE_PHASES = false;

	private UnitManager unitManager;
	private SettlementTemplateConfig settlementTemplateConfig;
	private RobotConfig robotConfig;
	private UserConfigurableConfig<Crew> crewConfig;
	private AuthorityFactory raFactory;
	private NationSpecConfig namingSpecs;

	private Consumer<String> statusConsumer;

	public SettlementBuilder(Simulation sim, SimulationConfig simConfig, Consumer<String> statusConsumer) {
		super();
		this.unitManager = sim.getUnitManager();
		this.settlementTemplateConfig = simConfig.getSettlementTemplateConfiguration();
		this.robotConfig = simConfig.getRobotConfiguration();
		this.raFactory = simConfig.getReportingAuthorityFactory();
		this.namingSpecs = new NationSpecConfig(simConfig);

		// Add an empty consumer to remove excessive if checks
		this.statusConsumer = (statusConsumer != null) ? statusConsumer : s -> {};
	}

	/**
	 * Creates all the initial Settlements.
	 */
	public void createInitialSettlements(Scenario bootstrap) {
		logger.config(bootstrap.getName() + " scenario loading...");
		for (InitialSettlement spec : bootstrap.getSettlements()) {
			createFullSettlement(spec);
		}

		statusConsumer.accept("Scenario " + bootstrap.getName() + " loaded.");
			
		// If loading full default and game mode then place the Commander
		if (GameManager.getGameMode() == GameMode.COMMAND) {
			GameManager.placeInitialCommander(unitManager);
		}
	}

	/**
	 * This creates a single fully populated Settlement according to the
	 * specification. 
	 * Note: it includes all sub-units, e.g. Vehicles & Persons
	 * along with any initial Parts & Resources.
	 * 
	 * @param spec
	 * @return
	 */
	public Settlement createFullSettlement(InitialSettlement spec) {
		SettlementTemplate template = settlementTemplateConfig.getItem(spec.getSettlementTemplate());
		logger.config("Creating '" + spec.getName() + "' based on template '" + spec.getSettlementTemplate() + "'...");
		statusConsumer.accept("Creating settlement " + spec.getName() + "...");

		StopWatch watch = new StopWatch();
		watch.start();

		Settlement settlement = createSettlement(template, spec);
		outputTimecheck(settlement, watch, "Create Settlement");

		// Deliver the supplies
		createSupplies(template.getSupplies(), settlement);
		outputTimecheck(settlement, watch, "Create Supplies");

		// TOCO get off the Initial Settlement
		String crew = spec.getCrew();

		// Create settlers to fill the settlement(s)
		if ((crew != null) && (crewConfig != null)) {
			createPreconfiguredPeople(settlement, crew);
			outputTimecheck(settlement, watch, "Create Preconfigured People");
		}
		createPeople(settlement, settlement.getInitialPopulation(), false);
		
		// Establish a system of governance at a settlement.
		settlement.getChainOfCommand().establishSettlementGovernance();
		outputTimecheck(settlement, watch, "Create People");
		
		// Manually add job positions
		JobUtil.tuneJobDeficit(settlement);
		outputTimecheck(settlement, watch, "Tune Job");

		// Create pre-configured robots as stated in Settlement template
		createPreconfiguredRobots(template, settlement);
		outputTimecheck(settlement, watch, "Create Preconfigured Robots");

		// Create more robots to fill the settlement(s)
		createRobots(settlement, settlement.getInitialNumOfRobots());
		outputTimecheck(settlement, watch, "Create Robots");

		watch.stop();
		if (MEASURE_PHASES) {
			logger.config(settlement, "Fully created in " + watch.getDuration());
		}

		return settlement;
	}

	/**
	 * Creates the supplies in a settlement.
	 * 
	 * @param settlement Target settlement
	 * @param supplies The definition of the Supplies
	 */
	public void createSupplies(SettlementSupplies supplies, Settlement settlement) {
		createVehicles(supplies, settlement);

		createEquipment(supplies, settlement);

		createBins(supplies, settlement);
		
		createResources(supplies, settlement);

		createParts(supplies, settlement);
	}

	/**
	 * Outputs the time it takes to check on a phase.
	 * 
	 * @param settlement
	 * @param watch
	 * @param phase
	 */
	private static void outputTimecheck(Settlement settlement, StopWatch watch, String phase) {
		if (MEASURE_PHASES) {
			watch.split();
			logger.config(settlement, phase + " took " + watch.getDuration() + " ms");
			watch.unsplit();
		}
	}

	/**
	 * Generates a unique name for the Settlement.
	 * 
	 * @param sponsor
	 * @return
	 */
	private String generateName(Authority sponsor) {
		List<String> remainingNames = new ArrayList<>(sponsor.getSettlementNames());

		List<String> usedNames = unitManager.getSettlements().stream()
							.map(Entity::getName).toList();

		remainingNames.removeAll(usedNames);
		int idx = RandomUtil.getRandomInt(remainingNames.size());

		return remainingNames.get(idx);
	}

	/**
	 * Creates a settlement.
	 * 
	 * @param template
	 * @param spec
	 * @return
	 */
	private Settlement createSettlement(SettlementTemplate template, InitialSettlement spec) {
		Authority ra;
		String sponsor = spec.getSponsor();
		// If the sponsor has not be defined; then use the template
		if (sponsor == null) {
			ra = template.getSponsor();
		}
		else {
			ra = raFactory.getItem(sponsor);
		}
		
		// Get settlement name
		String name = spec.getName();
		if (name == null) {
			name = generateName(ra);
		}

		// Get settlement longitude
		Coordinates location = spec.getLocation();
		if (location == null) {
			location = Coordinates.getRandomLocation();
		}

		int populationNumber = spec.getPopulationNumber();
		int initialNumOfRobots = spec.getNumOfRobots();

		// Add scenarioID
		Settlement settlement = Settlement.createNewSettlement(name,
									spec.getSettlementTemplate(), ra,
									location, populationNumber,
									initialNumOfRobots);
		
		Settlement.initializeStatics();
		
		settlement.initializeData();
		
		unitManager.addUnit(settlement);

		return settlement;
	}

	/**
	 * Creates the initial vehicles at a settlement.
	 * 
	 * @param template
	 * @param settlement
	 */
	private void createVehicles(SettlementSupplies template, Settlement settlement) {
		for(Entry<String, Integer> v : template.getVehicles().entrySet()) {
			String vehicleType = v.getKey();
			int number = v.getValue();
			for (int x = 0; x < number; x++) {
				VehicleFactory.createVehicle(unitManager, settlement, vehicleType);
			}
		}
	}

	/**
	 * Creates the initial equipment at a settlement.
	 *
	 * @param template
	 * @param settlement
	 * @throws Exception if error making equipment.
	 */
	private void createEquipment(SettlementSupplies template, Settlement settlement) {
		for(Entry<String, Integer> e : template.getEquipment().entrySet()) {
			String type = e.getKey();
			int number = e.getValue();
			for (int x = 0; x < number; x++) {
				EquipmentFactory.createEquipment(type, settlement);
			}
		}
	}

	/**
	 * Creates the initial bins at a settlement.
	 *
	 * @param template
	 * @param settlement
	 * @throws Exception if error making bins
	 */
	private void createBins(SettlementSupplies template, Settlement settlement) {
		for(Entry<String, Integer> e : template.getBins().entrySet()) {
			String type = e.getKey();
			int number = e.getValue();
			for (int x = 0; x < number; x++) {
				BinFactory.createBins(type, settlement);
			}
		}
	}

	/**
	 * Creates initial Robots based on available capacity at settlements.
	 * 
	 * @param settlement
	 * @param target
	 * @throws Exception if Robots can not be constructed
	 */
	public void createRobots(Settlement settlement, int target) {
		// Randomly create all remaining robots to fill the settlements to capacity.
		RobotDemand demand = new RobotDemand(settlement);

		// Note : need to call updateAllAssociatedRobots() first to compute numBots in Settlement
		while (settlement.getNumBots() < target) {
			// Get a robotType randomly
			RobotType robotType = demand.getBestNewRobot();

			// Adopt Static Factory Method and Factory Builder Pattern
			String newName = Robot.generateName(robotType);

			// Find the spec for this robot, take any model
			RobotSpec spec = robotConfig.getRobotSpec(robotType, null);

			buildRobot(settlement, spec, newName);
		}
	}

	/**
	 * Builds a single Robot in a settlement according to a spec.
	 * 
	 * @param settlement Home of the Robot
	 * @param spec Specification of what to build
	 * @param name New name
	 */
	private void buildRobot(Settlement settlement, RobotSpec spec, String name) {
		Robot robot = new Robot(name, settlement, spec);
		robot.initialize();

		RobotJob robotJob = JobUtil.getRobotJob(spec.getRobotType());
		robot.getBotMind().setRobotJob(robotJob, true);
		
        // Call addOwnedRobot prior to addUnit
		settlement.addOwnedRobot(robot);

		unitManager.addUnit(robot);
	}

	/**
	 * Creates the initial resources at a settlement. Note: This is in addition to
	 * any initial resources set in buildings.
	 * 
	 * @param template
	 * @param settlement
	 * @throws Exception if error storing resources
	 */
	private void createResources(SettlementSupplies template, Settlement settlement) {

		Map<AmountResource, Double> resourceMap = template.getResources();
		for (Entry<AmountResource, Double> value : resourceMap.entrySet()) {
			AmountResource resource = value.getKey();
			double amount = value.getValue();
			double capacity = settlement.getRemainingCombinedCapacity(resource.getID());
			if (amount > capacity)
				amount = capacity;
			settlement.storeAmountResource(resource.getID(), amount);
		}
	}

	/**
	 * Creates initial parts for a settlement.
	 * 
	 * @param template
	 * @param settlement
	 * @throws Exception if error creating parts
	 */
	private void createParts(SettlementSupplies template, Settlement settlement) {

		Map<Part, Integer> partMap = template.getParts();
		for (Entry<Part, Integer> item : partMap.entrySet()) {
			Part part = item.getKey();
			Integer number = item.getValue();
			settlement.storeItemResource(part.getID(), number);
		}
	}

	/**
	 * Creates initial people based on available capacity at settlements.
	 *
	 * @param settlement Hosting settlement
	 * @param targetPopulation Population goal
	 * @param noDefaultRole True if there's no default role for this person and need to look for one for him
	 */
	public void createPeople(Settlement settlement, int targetPopulation, boolean noDefaultRole) {

		Authority sponsor = settlement.getReportingAuthority();
		
		long males = settlement.getAllAssociatedPeople().stream()
												.filter(p -> p.getGender() == GenderType.MALE).count();
		int targetMales = (int) (sponsor.getGenderRatio() * targetPopulation);
		
		// Who exists already
		Set<String> existingfullnames = new HashSet<>(unitManager.getPeople().stream()
								.map(Person::getName).collect(Collectors.toSet()));

		// Fill up the settlement by creating more people
		while (settlement.getNumCitizens() < targetPopulation) {
			
			// Choose the next gender based on the current ratio of M/F
			GenderType gender;
			if (males < targetMales) {
				gender = GenderType.MALE;
				males++;
			}
			else {
				gender = GenderType.FEMALE;
			}
			
			// This is random and may change on each call
			String country = sponsor.getRandomCountry();
			NationSpec spec = namingSpecs.getItem(country);

			// Make sure settlement name isn't already being used.
			String fullname = spec.generateName(gender, existingfullnames);
			existingfullnames.add(fullname);

			// Use Builder Pattern for creating an instance of Person
			Person person = Person.create(fullname, settlement, gender)
					.setSponsor(sponsor)
					.setCountry(spec)
					.build();

	        // Call addACitizen prior to addUnit
			settlement.addACitizen(person);

			unitManager.addUnit(person);
			
			// Set up preference
			person.getPreference().initializePreference();
			// Assign a job
			person.getMind().getAJob(true, JobUtil.MISSION_CONTROL);
		
			JobType jobType = person.getMind().getJobType();
			
			if (jobType == JobType.TOURIST) {
				person.setRole(RoleType.GUEST);
			}		
			else if (noDefaultRole) {
				
				if (jobType == JobType.TOURIST) {
					person.setRole(RoleType.GUEST);
				}
				else {
					RoleType choosen = RoleUtil.findBestRole(person);
					person.setRole(choosen);
				}
			}
		}
	}

	/**
	 * Creates all pre-configured people as listed in people.xml.
	 * 
	 * @param settlement
	 * @param crewName
	 */
	private void createPreconfiguredPeople(Settlement settlement, String crewName) {

		Crew crew = crewConfig.getItem(crewName);
		if (crew == null) {
			throw new IllegalArgumentException("No crew defined called " + crewName);
		}
		logger.config("Crew '" + crew.getName() + "' assigned to " + settlement.getName() + ".");

		// Check for any duplicate full Name
		Collection<Person> people = unitManager.getPeople();
		
		Set<String> existingfullnames = people.stream()
				.map(Person::getName).collect(Collectors.toSet());

		Map<Person, Map<String, Integer>> addedCrew = new HashMap<>();

		// Get person's settlement or same sponsor
		Authority defaultSponsor = settlement.getReportingAuthority();

		// Create all configured people.
		for (Member m : crew.getTeam()) {
			if (settlement.getInitialPopulation() > settlement.getNumCitizens()) {
				Authority sponsor = defaultSponsor;
				if (m.getSponsorCode() != null) {
					sponsor = raFactory.getItem(m.getSponsorCode());
				}
	
				// Get person's gender or randomly determine it if not configured.
				GenderType gender = m.getGender();

				if (gender == null) {
					gender = GenderType.FEMALE;
					if (RandomUtil.getRandomDouble(1.0D) <= sponsor.getGenderRatio()) {
						gender = GenderType.MALE;
					}
				}
				
				// Check name
				String name = m.getName();
				if (existingfullnames.contains(name)) {
					// Should not happen so a cheap fix in place
					logger.warning("Person already called " + name + ".");

					// This is random and may change on each call
					String country = sponsor.getRandomCountry();
					NationSpec spec = namingSpecs.getItem(country);
					name = spec.generateName(gender, existingfullnames);
					
				}
				existingfullnames.add(name);

				// Get person's age
				int age = 0;
				String ageStr = m.getAge();
				if (ageStr != null)
					age = Integer.parseInt(ageStr);
				else
					age = RandomUtil.getRandomInt(21, 65);

				// Retrieve country & sponsor designation from people.xml (may be edited in
				// CrewEditorFX)
				var country = namingSpecs.getItem(m.getCountry());

				// Loads the person's preconfigured skills (if any).
				Map<SkillType, Integer> skillMap = m.getSkillMap();

				// Set the person's configured Big Five Personality traits (if any).
				Map<String, Integer> bigFiveMap = new HashMap<>(); //TOOO

				// Override person's personality type based on people.xml, if any.
				String mbti = m.getMBTI();

				// Create person and add to the unit manager.
				// Use Builder Pattern for creating an instance of Person
				Person person = Person.create(name, settlement, gender)
						.setAge(age)
						.setSponsor(sponsor)
						.setCountry(country)
						.setSkill(skillMap)
						.setPersonality(bigFiveMap, mbti)
						.build();

		        // Call addACitizen prior to addUnit
				settlement.addACitizen(person);

				unitManager.addUnit(person);

				// Set the person as a preconfigured crew member
				Map<String, Integer> relMap = m.getRelationshipMap();
				if (relMap != null) {
					addedCrew.put(person, relMap);
				}

				// Set person's job (if any).
				String jobName = m.getJob();
				if (jobName != null) {
					JobType job = JobType.getJobTypeByName(jobName);
					if (job != null) {
						// Designate a specific job to a person
						person.getMind().assignJob(job, true, JobUtil.MISSION_CONTROL, AssignmentType.APPROVED,
								JobUtil.MISSION_CONTROL);
					}
				}

				// Set Favourites
				setFavorites(person, m);
								
				// Initialize Preference
				person.getPreference().initializePreference();
			}
		}

		createConfiguredRelationships(addedCrew);
	}


	private void setFavorites(Person person, Member m) {
		
		// Add Favorite class
		String mainDish = m.getMainDish();
		String sideDish = m.getSideDish();
		String dessert = m.getDessert();
		String activity = m.getActivity();

		// Add Favorite class
		Favorite f = person.getFavorite();

		Set<String> favoriteDishes = new HashSet<>();
		if (mainDish != null) {
			favoriteDishes.add(mainDish);
		}
		if (sideDish != null) {
			favoriteDishes.add(sideDish);
		}
		if (dessert != null) {
			favoriteDishes.add(dessert);
		}
		if (!favoriteDishes.isEmpty()) {
			// If no favorite dishes then generate some random ones
			f.setFavoriteDishes(favoriteDishes);
		}

		if (activity != null) {
			var favoriteType = FavoriteType.fromString(activity);
			f.setFavoriteActivityType(favoriteType);
		}
	}

	/**
	 * Creates all configured pre-configured crew relationships.
	 * 
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
			            RelationshipUtil.changeOpinion(person, potentialFriend, RelationshipType.FACE_TO_FACE_COMMUNICATION, opinion);
					}
				}
			}
		}
	}

	/**
	 * Creates all configured Robots.
	 * 
	 * @param template
	 * @param settlement
	 * @throws Exception if error parsing XML.
	 */
	private void createPreconfiguredRobots(SettlementTemplate template, Settlement settlement) {
		for(RobotTemplate rt : template.getPredefinedRobots()) {
			String newName = rt.getName();

			if (newName != null) {
				// Check predefined name does not exist already
				int idx = 1;
				Collection<Robot> robots = unitManager.getRobots();
				boolean goodName = false;
				while(!goodName) {
					final String nextName = newName;
					Optional<Robot> found = robots.stream().filter(r -> r.getName().equals(nextName)).findAny();
					goodName = found.isEmpty();
					if (!goodName) {
						newName = rt.getName() + " #" + idx++;
					}
				}
			}
			else {
				// Generate a name
				newName = Robot.generateName(rt.getType());
			}

			// Find the spec for this robot, take any model
			RobotSpec spec = robotConfig.getRobotSpec(rt.getType(), rt.getModel());

			buildRobot(settlement, spec, newName);
		}
	}

	/**
	 * Enable the use of a predefined crews.
	 * 
	 * @param crewConfig
	 */
	public void setCrew(UserConfigurableConfig<Crew> crewConfig) {
		this.crewConfig = crewConfig;
	}
}
