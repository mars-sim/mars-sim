/**
 * Mars Simulation Project
 * ArrivingSettlement.java
 * @version 3.1.0 2017-01-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.settlement;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.interplanetary.transport.TransitState;
import org.mars_sim.msp.core.interplanetary.transport.TransportEvent;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Favorite;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonBuilderImpl;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.ChainOfCommand;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A new arriving settlement from Earth.
 */
public class ArrivingSettlement
implements Transportable, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ArrivingSettlement.class.getName());

	// Data members.
	private String name;
	private String template;
	private TransitState transitState;
	private MarsClock launchDate;
	private MarsClock arrivalDate;
	private Coordinates landingLocation;
	private int populationNum;
	private int numOfRobots;
	private int scenarioID;

	/**
	 * Constructor.
	 * @param name the name of the arriving settlement.
	 * @param template the design template for the settlement.
	 * @param arrivalDate the arrival date.
	 * @param landingLocation the landing location.
	 * @param populationNum the population of new immigrants arriving with the settlement.
	 * @param numOfRobots the number of new robots.
	 */
	public ArrivingSettlement(
		String name, String template,
		MarsClock arrivalDate, Coordinates landingLocation,
		int populationNum, int numOfRobots) {
		this.name = name;
		this.template = template;
		this.arrivalDate = arrivalDate;
		this.landingLocation = landingLocation;
		this.populationNum = populationNum;
		this.numOfRobots = numOfRobots;
	}


	/**
	 * Gets the scenarioID of the arriving settlement.
	 * @return settlement scenarioID
	 */
	public int getScenarioID() {
		return scenarioID;
	}

	/**
	 * Sets the scenarioID of the arriving settlement.
	 * @param settlement id
	 */
	public void setScenarioID(int id) {
		scenarioID = id;
	}

	/**
	 * Gets the name of the arriving settlement.
	 * @return settlement name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the arriving settlement.
	 * @param name settlement name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the design template of the arriving settlement.
	 * @return the settlement template string.
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * Sets the design template of the arriving settlement.
	 * @param template the settlement template string.
	 */
	public void setTemplate(String template) {
		this.template = template;
	}

	/**
	 * Gets the transit state of the settlement.
	 * @return transit state string.
	 */
	public TransitState getTransitState() {
		return transitState;
	}

	/**
	 * Sets the transit state of the settlement.
	 * @param transitState {@link TransitState} the transit state
	 */
	public void setTransitState(TransitState transitState) {
		this.transitState = transitState;
	}

	/**
	 * Gets the launch date of the settlement.
	 * @return the launch date.
	 */
	public MarsClock getLaunchDate() {
		return launchDate;
	}

	/**
	 * Sets the launch date of the settlement.
	 * @param launchDate the launch date.
	 */
	public void setLaunchDate(MarsClock launchDate) {
		this.launchDate = launchDate;
	}

	/**
	 * Gets the arrival date of the settlement.
	 * @return the arrival date.
	 */
	public MarsClock getArrivalDate() {
		return arrivalDate;
	}

	/**
	 * Sets the arrival date of the settlement.
	 * @param arrivalDate the arrival date.
	 */
	public void setArrivalDate(MarsClock arrivalDate) {
		this.arrivalDate = arrivalDate;
	}

	/**
	 * Gets the landing location for the arriving settlement.
	 * @return landing location coordinates.
	 */
	public Coordinates getLandingLocation() {
		return landingLocation;
	}

	/**
	 * Sets the landing location for the arriving settlement.
	 * @param landingLocation the landing location coordinates.
	 */
	public void setLandingLocation(Coordinates landingLocation) {
		this.landingLocation = landingLocation;
	}

	/**
	 * Gets the population of the arriving settlement.
	 * @return population number.
	 */
	public int getPopulationNum() {
		return populationNum;
	}

	/**
	 * Sets the population of the arriving settlement.
	 * @param populationNum the population number.
	 */
	public void setPopulationNum(int populationNum) {
		this.populationNum = populationNum;
	}

	/**
	 * Gets the number of robots of the arriving settlement.
	 * @return numOfRobots.
	 */
	public int getNumOfRobots() {
		return numOfRobots;
	}

	/**
	 * Sets the number of robots of the arriving settlement.
	 * @param numOfRobots.
	 */
	public void setNumOfRobots(int numOfRobots) {
		this.numOfRobots = numOfRobots;
	}

	/**
	 * Commits a set of modifications for the arriving settlement.
	 */
	public void commitModification() {
		HistoricalEvent newEvent = new TransportEvent(
			this,
			EventType.TRANSPORT_ITEM_MODIFIED,
			"Arriving settlement modified"
		);
		Simulation.instance().getEventManager().registerNewEvent(newEvent);
	}

	@Override
	public void destroy() {
		name = null;
		template = null;
		transitState = null;
		launchDate = null;
		arrivalDate = null;
		landingLocation = null;
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(getName());
		buff.append(": ");
		buff.append(getArrivalDate().getDateString());
		return buff.toString();
	}

	@Override
	public int compareTo(Transportable o) {
		int result = 0;

		double arrivalTimeDiff = MarsClock.getTimeDiff(arrivalDate, o.getArrivalDate());
		if (arrivalTimeDiff < 0D) {
			result = -1;
		}
		else if (arrivalTimeDiff > 0D) {
			result = 1;
		}
		else {
			// If arrival time is the same, compare by settlement name alphabetically.
			result = name.compareTo(o.getName());
		}

		return result;
	}

	@Override
	public synchronized void performArrival() {
		// Create new settlement.
		Settlement newSettlement = createNewSettlement();
		//System.out.println("ArrivingSettlement.java : performArrival() : just done calling");

		// Create new immigrants with arriving settlement.
		createNewImmigrants(newSettlement);

		// Create new robots.
		createNewRobots(newSettlement);

		// Create new equipment.
		createNewEquipment(newSettlement);

		// Create new parts.
		createNewParts(newSettlement);

		// Create new resources.
		createNewResources(newSettlement);

		// Create new vehicles.
		createNewVehicles(newSettlement);
	}

	/**
	 * Create the new arriving settlement.
	 */
	private Settlement createNewSettlement() {

		// Create new settlement with unit manager.
		UnitManager unitManager = Simulation.instance().getUnitManager();
		// 2014-10-29 Computed sid
		//List<Settlement> settlements = new ArrayList<Settlement>(unitManager.getSettlements());
		scenarioID = 9; // NOTE: scenarioID will be updated later and NOT important here
		//System.out.println("ArrivingSettlement.java : createNewSettlement() : scenarioID is " + scenarioID);
		// TODO: add the option of choosing sponsor
		String sponsor = Msg.getString("ReportingAuthorityType.MarsSociety"); //$NON-NLS-1$ //"Mars Society (MS)";

		//Settlement newSettlement = new Settlement(name, scenarioID, template, sponsor, landingLocation, populationNum, numOfRobots);
		Settlement newSettlement = Settlement.createNewSettlement(name, scenarioID, template, sponsor, landingLocation, populationNum, numOfRobots);
		unitManager.addUnit(newSettlement);

		// Add new settlement to credit manager.
		Simulation.instance().getCreditManager().addSettlement(newSettlement);

		return newSettlement;
	}

	/**
	 * Create the new immigrants arriving with the settlement.
	 * @param newSettlement the new settlement.
	 */
	private void createNewImmigrants(Settlement newSettlement) {

		Collection<Person> immigrants = new ConcurrentLinkedQueue<Person>();
		UnitManager unitManager = Simulation.instance().getUnitManager();
		RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		for (int x = 0; x < populationNum; x++) {
			PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
			GenderType gender = GenderType.FEMALE;
			if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) gender = GenderType.MALE;
			String birthplace = "Earth"; //TODO: randomize from list of countries/federations
			String immigrantName = unitManager.getNewName(UnitType.PERSON, null, gender, null);
			String sponsor = newSettlement.getSponsor();
			String country = unitManager.getCountry(sponsor);
			//Person immigrant = new Person(immigrantName, gender, country, newSettlement, sponsor);
			// 2017-04-11 Use Builder Pattern for creating an instance of Person
			Person immigrant = Person.create(immigrantName, newSettlement)
									.setGender(gender)
									.setCountry(country)
									.setSponsor(sponsor)
									.build();
			immigrant.initialize();

			// Initialize favorites and preferences.
            Favorite favorites = immigrant.getFavorite();
            favorites.setFavoriteMainDish(favorites.getRandomMainDish());
            favorites.setFavoriteSideDish(favorites.getRandomSideDish());
            favorites.setFavoriteDessert(favorites.getRandomDessert());
            favorites.setFavoriteActivity(favorites.getRandomActivity());
            immigrant.getPreference().initializePreference();

            // Assign a job by calling getInitialJob
            immigrant.getMind().getInitialJob(JobManager.MISSION_CONTROL);

			unitManager.addUnit(immigrant);
			relationshipManager.addNewImmigrant(immigrant, immigrants);
			immigrants.add(immigrant);
			logger.info(immigrantName + " arrives on Mars at " + newSettlement.getName());
		}

		// Update command/governance and work shift schedules at settlement with new immigrants.
        if (immigrants.size() > 0) {

            int popSize = newSettlement.getAllAssociatedPeople().size();

            // Reset specialist positions at settlement.
            ChainOfCommand cc = newSettlement.getChainOfCommand();
            Iterator<Person> i = newSettlement.getAllAssociatedPeople().iterator();
            while (i.hasNext()) {
                Person person = i.next();
                if (popSize >= UnitManager.POPULATION_WITH_MAYOR) {
                    cc.set7Divisions(true);
                    cc.assignSpecialiststo7Divisions(person);
                }
                else {
                    cc.set3Divisions(true);
                    cc.assignSpecialiststo3Divisions(person);
                }
            }

			// 2016-12-21 Added calling updateAllAssociatedPeople(), not getAllAssociatedPeople()()
			newSettlement.updateAllAssociatedPeople();
			newSettlement.updateAllAssociatedRobots();

            // Reset work shift schedules at settlement.
            unitManager.setupShift(newSettlement, popSize);

            // Reset command/government system at settlement.
            unitManager.establishSettlementGovernance(newSettlement);
        }
	}

	/**
	 * Create the new settlement's robots.
	 * @param newSettlement the new settlement.
	 */
	private void createNewRobots(Settlement newSettlement) {

        UnitManager unitManager = Simulation.instance().getUnitManager();
	    for (int x = 0; x < numOfRobots; x++) {

	        // Get a robotType randomly
            RobotType robotType = unitManager.getABot(newSettlement, numOfRobots);

            // Create arriving robot.
            //Robot robot = new Robot(unitManager.getNewName(UnitType.ROBOT, null, null, robotType), robotType, "Earth",
            //        newSettlement, newSettlement.getCoordinates());
			// 2017-04-16 Adopt Static Factory Method and Factory Builder Pattern
			Robot robot = Robot.create(unitManager.getNewName(UnitType.ROBOT, null, null, robotType), newSettlement, robotType)
					.setCountry("Earth").build();
			robot.initialize();

            unitManager.addUnit(robot);

            // Initialize robot job.
            String jobName = RobotJob.getName(robotType);
            if (jobName != null) {
                RobotJob robotJob = JobManager.getRobotJob(robotType.getName());
                if (robotJob != null) {
                    robot.getBotMind().setRobotJob(robotJob, true);
                }
            }
	    }
	}

	/**
	 * Create the new settlement's equipment.
	 * @param newSettlement the new settlement.
	 */
	private void createNewEquipment(Settlement newSettlement) {

		SettlementTemplate template = SimulationConfig.instance().getSettlementConfiguration().
				getSettlementTemplate(getTemplate());
		UnitManager unitManager = Simulation.instance().getUnitManager();
		Iterator<String> equipmentI = template.getEquipment().keySet().iterator();
		while (equipmentI.hasNext()) {
			String equipmentType = equipmentI.next();
			int number = template.getEquipment().get(equipmentType);
			for (int x=0; x < number; x++) {
				Equipment equipment = EquipmentFactory.getEquipment(equipmentType,
						newSettlement.getCoordinates(), false);
				equipment.setName(unitManager.getNewName(UnitType.EQUIPMENT, equipmentType, null, null));
				unitManager.addUnit(equipment);
				newSettlement.getInventory().storeUnit(equipment);
			}
		}
	}

	/**
	 * Create the new settlement's parts.
	 * @param newSettlement the new settlement.
	 */
	private void createNewParts(Settlement newSettlement) {

		SettlementTemplate template = SimulationConfig.instance().getSettlementConfiguration().
				getSettlementTemplate(getTemplate());
		Iterator<Part> partsI = template.getParts().keySet().iterator();
		while (partsI.hasNext()) {
			Part part = partsI.next();
			int number = template.getParts().get(part);
			newSettlement.getInventory().storeItemResources(part, number);
		}
	}

	/**
	 * Create the new settlement's resources.
	 * @param newSettlement the new settlement.
	 */
	private void createNewResources(Settlement newSettlement) {

		SettlementTemplate template = SimulationConfig.instance().getSettlementConfiguration().
				getSettlementTemplate(getTemplate());
		Iterator<AmountResource> resourcesI = template.getResources().keySet().iterator();
		while (resourcesI.hasNext()) {
			AmountResource resource = resourcesI.next();
			double amount = template.getResources().get(resource);
			double capacity = newSettlement.getInventory().getAmountResourceRemainingCapacity(
					resource, true, false);
			if (amount > capacity) amount = capacity;
			newSettlement.getInventory().storeAmountResource(resource, amount, true);
			//newSettlement.getInventory().addAmountSupplyAmount(resource, amount);
		}
	}

	/**
	 * Create the new settlement's vehicles.
	 * @param newSettlement the new settlement.
	 */
	private void createNewVehicles(Settlement newSettlement) {

		SettlementTemplate template = SimulationConfig.instance().getSettlementConfiguration().
				getSettlementTemplate(getTemplate());
		UnitManager unitManager = Simulation.instance().getUnitManager();
		Iterator<String> vehicleI = template.getVehicles().keySet().iterator();
		while (vehicleI.hasNext()) {
			String vehicleType = vehicleI.next();
			int number = template.getVehicles().get(vehicleType);
			for (int x = 0; x < number; x++) {
				Vehicle vehicle = null;
				if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType)) {
					String name = unitManager.getNewName(UnitType.VEHICLE, "LUV", null, null);
					vehicle = new LightUtilityVehicle(name, vehicleType.toLowerCase(), newSettlement);
				} else {
					String name = unitManager.getNewName(UnitType.VEHICLE, null, null, null);
					vehicle = new Rover(name, vehicleType.toLowerCase(), newSettlement);
				}
				unitManager.addUnit(vehicle);
			}
		}
	}
}