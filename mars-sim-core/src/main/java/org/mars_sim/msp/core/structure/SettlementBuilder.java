/**
 * Mars Simulation Project
 * SettlementConfig.java
 * @version 3.2.0 2021-07-10
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
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
	
	private UnitManager unitManager;
	private RelationshipManager relationshipManager;

	private SettlementConfig settlementConfig;
	private PersonConfig personConfig;
	
	public SettlementBuilder(UnitManager unitManager, RelationshipManager relationshipManager,
			SimulationConfig simConfig) {
		super();
		this.unitManager = unitManager;
		this.relationshipManager = relationshipManager;
		this.settlementConfig = simConfig.getSettlementConfiguration();
		this.personConfig = simConfig.getPersonConfig();
	}

	/**
	 * Create all the initial Settlements
	 */
	public void createInitialSettlements() {
		for (InitialSettlement spec : settlementConfig.getInitialSettlements()) {
			createFullSettlement(spec);
		}
		
		// If loading full default and game mode then place the Commander
		if (GameManager.mode == GameMode.COMMAND) {
			GameManager.placeInitialCommander(unitManager);
		}
	}
	
	/**
	 * Theis create a single fully populated Settlement according to the
	 * specification. This includes all sub-units, e.g. Vehicles & Persons
	 * along with any initila Parts & Resources.
	 * @param spec
	 * @return
	 */
	public Settlement createFullSettlement(InitialSettlement spec) {
		Settlement settlement = createSettlement(spec);
		logger.config(settlement, "Populating based on template " + settlement.getTemplate());
		
		SettlementTemplate template = settlementConfig.getSettlementTemplate(settlement.getTemplate());

		createVehicles(template, settlement);
		
		createEquipment(template, settlement);

		createResources(template, settlement);

		createParts(template, settlement);
		
		// Create pre-configured robots as stated in robots.xml
//		if (useCrew)
//			createPreconfiguredRobots();
		
		// Create more robots to fill the settlement(s)
		createRobots(settlement);
		
		// Create pre-configured settlers as stated in people.xml
//		if (useCrew)
//			createPreconfiguredPeople();
		
		// Create more settlers to fill the settlement(s)
		createPeople(settlement);
		
		// Manually add job positions
		settlement.tuneJobDeficit();
		
		return settlement;
	}

	private Settlement createSettlement(InitialSettlement spec) {
		ReportingAuthorityType sponsor = spec.getSponsor();

		// Get settlement name
		String name = spec.getName();
		if (name == null) {
			name = Settlement.generateName(sponsor);
		}

		// Get settlement template
		String template = spec.getSettlementTemplate();

		// Get settlement longitude
		double longitude = 0D;
		String longitudeStr = spec.geLongitude();
		if (longitudeStr == null) {
			longitude = Coordinates.getRandomLongitude();
		} else {
			longitude = Coordinates.parseLongitude2Theta(longitudeStr);
		}

		// Get settlement latitude
		double latitude = 0D;
		String latitudeStr = spec.getLatitude();
		
		if (latitudeStr == null) {
			latitude = Coordinates.getRandomLatitude();
		} else {
			latitude = Coordinates.parseLatitude2Phi(latitudeStr);
		}

		Coordinates location = new Coordinates(latitude, longitude);

		int populationNumber = spec.getPopulationNumber();
		int initialNumOfRobots = spec.getNumOfRobots();

		// Add scenarioID
		//int scenarioID = settlementConfig.getInitialSettlementTemplateID(x);
		int scenarioID = 0;
		Settlement settlement = Settlement.createNewSettlement(name, scenarioID, template, sponsor, location, populationNumber,
				initialNumOfRobots);
		settlement.initialize();
		unitManager.addUnit(settlement);
	
		return settlement;
	}
	
	private void createVehicles(SettlementTemplate template, Settlement settlement) {
		Map<String, Integer> vehicleMap = template.getVehicles();
		Iterator<String> j = vehicleMap.keySet().iterator();
		ReportingAuthorityType sponsor = settlement.getSponsor();
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
				Equipment equipment = EquipmentFactory.createEquipment(type, settlement.getCoordinates(),
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
		ReportingAuthorityType sponsor = settlement.getSponsor();

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
			String fullname = Person.generateName(sponsor, country, gender);

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

			relationshipManager.addInitialSettler(person, settlement);

			// Set up preference
			person.getPreference().initializePreference();

			// Assign a job 
			person.getMind().getInitialJob(JobUtil.MISSION_CONTROL);
			
			unitManager.addUnit(person);
		}

		// Set up work shift
		unitManager.setupShift(settlement, initPop);
		
		// Establish a system of governance at a settlement.
		settlement.getChainOfCommand().establishSettlementGovernance();
	}	
}
