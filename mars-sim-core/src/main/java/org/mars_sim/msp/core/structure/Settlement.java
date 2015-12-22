/**
 * Mars Simulation Project
 * Settlement.java
 * @version 3.08 2015-12-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.mars.DailyWeather;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.RadiationExposure;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.job.Astronomer;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.structure.building.function.HeatMode;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * f The Settlement class represents a settlement unit on virtual Mars. It
 * contains information related to the state of the settlement.
 */
public class Settlement extends Structure implements Serializable, LifeSupportType {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static Logger logger = Logger.getLogger(Settlement.class.getName());
	/** Normal air pressure (Pa) */
	private static final double NORMAL_AIR_PRESSURE = 101325D;
	/** Normal temperature (celsius) */
	private static final double NORMAL_TEMP = 22.5D;
	// maximum & minimal acceptable temperature for living space (arbitrary)
	// TODO: where are these two values from people.xml saved into by
	// PersonConfig.java?
	private static final double MIN_TEMP = 0.0D;
	private static final double MAX_TEMP = 48.0D;

	public static final int SOL_PER_REFRESH = 5;

	private static final int RECORDING_FREQUENCY = 250; // in millisols

	private static final int RADIATION_CHECK_FREQ = 100; // in millisols

	public static final int NUM_CRITICAL_RESOURCES = 9;

	/*
	 * Amount of time (millisols) required for periodic maintenance. private
	 * static final double MAINTENANCE_TIME = 1000D;
	 */
	/** The settlement template name. */
	private String template;
	private String name;

	/** The initial population of the settlement. */
	private int initialPopulation;
	private int initialNumOfRobots;
	private int scenarioID;
	private int solCache = 1, counter30 = 1;
	private int numShift;
	private int numA; // number of people with work shift A
	private int numB; // number of people with work shift B
	private int numX; // number of people with work shift X
	private int numY; // number of people with work shift Y
	private int numZ; // number of people with work shift Z
	private int numOnCall;
	private int quotientCache;
	private int quotient1Cache;

	/**
	 * Amount of time (millisols) that the settlement has had zero population.
	 */
	private double zeroPopulationTime;
	public double mealsReplenishmentRate = 0.6;
	public double dessertsReplenishmentRate = 0.7;

	// 2014-11-23 Added foodProductionOverride
	private boolean foodProductionOverride = false;
	// private boolean reportSample = true;
	/** Override flag for mission creation at settlement. */
	private boolean missionCreationOverride = false;
	/** Override flag for manufacturing at settlement. */
	private boolean manufactureOverride = false;
	/** Override flag for resource process at settlement. */
	private boolean resourceProcessOverride = false;
	/**
	 * Override flag for construction/salvage mission creation at settlement.
	 */
	private boolean constructionOverride = false;

	private boolean[] exposed = {false, false, false};


	//private int[] resourceArray = new int[9];
	//private int[] solArray = new int[30];
	//private double[] samplePointArray = new double[(int)1000/RECORDING_FREQUENCY];
	//private Object[][][] resourceObject = new Object[][][]{resourceArray, samplePointArray, solArray};

	/** The settlement's building manager. */
	protected BuildingManager buildingManager;
	/** The settlement's building connector manager. */
	protected BuildingConnectorManager buildingConnectorManager;
	/** The settlement's goods manager. */
	protected GoodsManager goodsManager;
	/** The settlement's construction manager. */
	protected ConstructionManager constructionManager;
	/** The settlement's building power grid. */
	protected PowerGrid powerGrid;
	// 2014-10-17 Added heating system
	/** The settlement's heating system. */
	protected ThermalSystem thermalSystem;

	private Inventory inv;

	private ChainOfCommand chainOfCommand;

	private MarsClock clock;

	/** The settlement's achievement in scientific fields. */
	private Map<ScienceType, Double> scientificAchievement;

	//private Map<Integer, Double> resourceMapCache = new HashMap<>();
	private Map<Integer, Map<Integer, List<Double>>> resourceStat = new HashMap<>();

	/**
	 * Constructor for subclass extension.
	 *
	 * @param name
	 *            the settlement's name
	 * @param location
	 *            the settlement's location
	 */
	// constructor 1
	// TODO: pending for deletion (use constructor 2 instead)
	protected Settlement(String name, Coordinates location) {
		// Use Structure constructor.
		super(name, location);
		this.name = name;
		// count++;
		// logger.info("constructor 1 : count is " + count);
	}

	// constructor 2
	// 2014-10-28 Added settlement id
	protected Settlement(String name, int scenarioID, Coordinates location) {
		// Use Structure constructor.
		super(name, location);
		this.name = name;
		this.scenarioID = scenarioID;
		// count++;
		// logger.info("constructor 2 : count is " + count);
	}

	// constructor 3
	// 2014-10-29 Added settlement id
	// Called by UnitManager.java when users create the initial settlement
	// Called by ArrivingSettlement.java when users create a brand new
	// settlement
	public Settlement(String name, int id, String template, Coordinates location, int populationNumber,
			int initialNumOfRobots) {
		// Use Structure constructor
		super(name, location);
		this.name = name;
		this.template = template;
		this.scenarioID = id;
		this.initialNumOfRobots = initialNumOfRobots;
		this.initialPopulation = populationNumber;

		// count++;
		// logger.info("constructor 3 : count is " + count);
		this.inv = getInventory();

		//resourceStat = new HashMap<>();

		// Set inventory total mass capacity.
		inv.addGeneralCapacity(Double.MAX_VALUE);
		// Initialize building manager
		buildingManager = new BuildingManager(this);
		// Initialize building connector manager.
		buildingConnectorManager = new BuildingConnectorManager(this);
		// Initialize goods manager.
		goodsManager = new GoodsManager(this);
		// Initialize construction manager.
		constructionManager = new ConstructionManager(this);
		// Initialize power grid
		powerGrid = new PowerGrid(this);
		// 2014-10-17 Added thermal control system
		thermalSystem = new ThermalSystem(this);
		// Initialize scientific achievement.
		scientificAchievement = new HashMap<ScienceType, Double>(0);

		chainOfCommand = new ChainOfCommand(this);

		clock = Simulation.instance().getMasterClock().getMarsClock();


	}

	/**
	 * Gets the settlement's meals replenishment rate.
	 *
	 * @return mealsReplenishmentRate
	 */
	// 2015-01-12 Added getMealsReplenishmentRate
	public double getMealsReplenishmentRate() {
		return mealsReplenishmentRate;
	}

	/**
	 * Sets the settlement's meals replenishment rate.
	 *
	 * @param rate
	 */
	// 2015-01-12 Added setMealsReplenishmentRate
	public void setMealsReplenishmentRate(double rate) {
		mealsReplenishmentRate = rate;
	}

	/**
	 * Gets the settlement's desserts replenishment rate.
	 *
	 * @return DessertsReplenishmentRate
	 */
	// 2015-01-12 Added getDessertsReplenishmentRate
	public double getDessertsReplenishmentRate() {
		return dessertsReplenishmentRate;
	}

	/**
	 * Sets the settlement's desserts replenishment rate.
	 *
	 * @param rate
	 */
	// 2015-01-12 Added setDessertsReplenishmentRate
	public void setDessertsReplenishmentRate(double rate) {
		dessertsReplenishmentRate = rate;
	}

	/**
	 * Gets the settlement template's unique ID.
	 *
	 * @return ID number.
	 */
	// 2014-10-29 Added settlement id
	public int getID() {
		return scenarioID;
	}

	/**
	 * Gets the population capacity of the settlement
	 *
	 * @return the population capacity
	 */
	public int getPopulationCapacity() {
		int result = 0;
		Iterator<Building> i = buildingManager.getBuildings(BuildingFunction.LIVING_ACCOMODATIONS).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			LivingAccommodations livingAccommodations = (LivingAccommodations) building
					.getFunction(BuildingFunction.LIVING_ACCOMODATIONS);
			result += livingAccommodations.getBeds();
		}

		return result;
	}

	/**
	 * Gets the current population number of the settlement
	 *
	 * @return the number of inhabitants
	 */
	public int getCurrentPopulationNum() {
		return getInhabitants().size();
	}

	/**
	 * Gets a collection of the inhabitants of the settlement.
	 *
	 * @return Collection of inhabitants
	 */
	public Collection<Person> getInhabitants() {
		return CollectionUtils.getPerson(getInventory().getContainedUnits());
	}

	/**
	 * Gets the current available population capacity of the settlement
	 *
	 * @return the available population capacity
	 */
	public int getAvailablePopulationCapacity() {
		return getPopulationCapacity() - getCurrentPopulationNum();
	}

	/**
	 * Gets an array of current inhabitants of the settlement
	 *
	 * @return array of inhabitants
	 */
	public Person[] getInhabitantArray() {
		Collection<Person> people = getInhabitants();
		Person[] personArray = new Person[people.size()];
		Iterator<Person> i = people.iterator();
		int count = 0;
		while (i.hasNext()) {
			personArray[count] = i.next();
			count++;
		}
		return personArray;
	}

	/**
	 * Gets the robot capacity of the settlement
	 *
	 * @return the robot capacity
	 */
	public int getRobotCapacity() {
		int result = 0;
		int stations = 0;
		Iterator<Building> i = buildingManager.getBuildings().iterator();
		while (i.hasNext()) {
			Building building = i.next();
			result++;
		}
		Iterator<Building> j = buildingManager.getBuildings(BuildingFunction.ROBOTIC_STATION).iterator();
		while (j.hasNext()) {
			Building building = j.next();
			RoboticStation roboticStations = (RoboticStation) building.getFunction(BuildingFunction.ROBOTIC_STATION);
			stations += roboticStations.getSlots();
			// stations++;
		}
		// stations = stations * 2;

		result = result + stations;

		return result;
	}

	/**
	 * Gets the current number of robots in the settlement
	 *
	 * @return the number of robots
	 */
	public int getCurrentNumOfRobots() {
		return getRobots().size();
	}

	/**
	 * Gets a collection of the number of robots of the settlement.
	 *
	 * @return Collection of robots
	 */
	public Collection<Robot> getRobots() {
		return CollectionUtils.getRobot(getInventory().getContainedUnits());
	}

	/**
	 * Gets the current available robot capacity of the settlement
	 *
	 * @return the available robots capacity
	 */
	public int getAvailableRobotCapacity() {
		return getRobotCapacity() - getCurrentNumOfRobots();
	}

	/**
	 * Gets an array of current robots of the settlement
	 *
	 * @return array of robots
	 */
	public Robot[] getRobotArray() {
		Collection<Robot> robots = getRobots();
		Robot[] robotArray = new Robot[robots.size()];
		Iterator<Robot> i = robots.iterator();
		int count = 0;
		while (i.hasNext()) {
			robotArray[count] = i.next();
			count++;
		}
		return robotArray;
	}

	/**
	 * Gets a collection of vehicles parked at the settlement.
	 *
	 * @return Collection of parked vehicles
	 */
	public Collection<Vehicle> getParkedVehicles() {
		return CollectionUtils.getVehicle(getInventory().getContainedUnits());
	}

	/**
	 * Gets the number of vehicles parked at the settlement.
	 *
	 * @return parked vehicles number
	 */
	public int getParkedVehicleNum() {
		return getParkedVehicles().size();
	}

	/**
	 * Returns true if life support is working properly and is not out of oxygen
	 * or water.
	 *
	 * @return true if life support is OK
	 * @throws Exception
	 *             if error checking life support.
	 */
	public boolean lifeSupportCheck() {
		boolean result = true;

		AmountResource oxygen = AmountResource.findAmountResource(LifeSupportType.OXYGEN);
		if (getInventory().getAmountResourceStored(oxygen, false) <= 0D)
			result = false;
		AmountResource water = AmountResource.findAmountResource(LifeSupportType.WATER);
		if (getInventory().getAmountResourceStored(water, false) <= 0D)
			result = false;

		// TODO: check against indoor air pressure
		// if (getAirPressure() != NORMAL_AIR_PRESSURE)
		// result = false;
		// TODO: check if this is working
		// 2014-11-28 Added MAX_TEMP
		// if (getTemperature() < MIN_TEMP || getTemperature() > MAX_TEMP)
		// result = false;

		return result;
	}


	/**
	 * Gets the number of people the life support can provide for.
	 *
	 * @return the capacity of the life support system
	 */
	public int getLifeSupportCapacity() {
		return getPopulationCapacity();
	}

	/**
	 * Gets oxygen from system.
	 *
	 * @param amountRequested
	 *            the amount of oxygen requested from system (kg)
	 * @return the amount of oxygen actually received from system (kg)
	 * @throws Exception
	 *             if error providing oxygen.
	 */
	public double provideOxygen(double amountRequested) {
		AmountResource oxygen = AmountResource.findAmountResource(LifeSupportType.OXYGEN);
		double oxygenTaken = amountRequested;
		double oxygenLeft = getInventory().getAmountResourceStored(oxygen, false);
		if (oxygenTaken > oxygenLeft)
			oxygenTaken = oxygenLeft;
		getInventory().retrieveAmountResource(oxygen, oxygenTaken);
		// 2015-01-09 Added addDemandTotalRequest()
		inv.addAmountDemandTotalRequest(oxygen);
		// 2015-01-09 addDemandRealUsage()
		inv.addAmountDemand(oxygen, oxygenTaken);

		AmountResource carbonDioxide = AmountResource.findAmountResource("carbon dioxide");
		double carbonDioxideProvided = oxygenTaken;
		double carbonDioxideCapacity = getInventory().getAmountResourceRemainingCapacity(carbonDioxide, true, false);
		if (carbonDioxideProvided > carbonDioxideCapacity)
			carbonDioxideProvided = carbonDioxideCapacity;

		getInventory().storeAmountResource(carbonDioxide, carbonDioxideProvided, true);
		// 2015-01-15 Add addSupplyAmount()
		getInventory().addAmountSupplyAmount(carbonDioxide, carbonDioxideProvided);
		return oxygenTaken;
	}

	/**
	 * Gets water from system.
	 *
	 * @param amountRequested
	 *            the amount of water requested from system (kg)
	 * @return the amount of water actually received from system (kg)
	 * @throws Exception
	 *             if error providing water.
	 */
	public double provideWater(double amountRequested) {
		AmountResource water = AmountResource.findAmountResource(LifeSupportType.WATER);
		double waterTaken = amountRequested;
		double waterLeft = getInventory().getAmountResourceStored(water, false);
		if (waterTaken > waterLeft)
			waterTaken = waterLeft;
		getInventory().retrieveAmountResource(water, waterTaken);

		// 2015-01-09 Added addDemandTotalRequest()
		inv.addAmountDemandTotalRequest(water);
		// 2015-01-09 addDemandRealUsage()
		inv.addAmountDemand(water, waterTaken);

		return waterTaken;
	}

	/**
	 * Gets the air pressure of the life support system.
	 *
	 * @return air pressure (Pa)
	 */
	public double getAirPressure() {
		double result = NORMAL_AIR_PRESSURE;
		double ambient = Simulation.instance().getMars().getWeather().getAirPressure(getCoordinates());

		if (result < ambient)
			return ambient;
		else
			return result;
	}

	/**
	 * Gets the temperature of the life support system.
	 *
	 * @return temperature (degrees C)
	 */
	public double getTemperature() {
		double result = NORMAL_TEMP;
		// double result = getLifeSupport().getTemperature();
		double ambient = Simulation.instance().getMars().getWeather().getTemperature(getCoordinates());

		if (result < ambient)
			return ambient;
		else
			return result;

		// return result;
	}

	/**
	 * Perform time-related processes
	 *
	 * @param time
	 *            the amount of time passing (in millisols)
	 * @throws Exception
	 *             if error during time passing.
	 */
	public void timePassing(double time) {

		// If settlement is overcrowded, increase inhabitant's stress.
		// TODO: should the number of robots be accounted for here?
		int overCrowding = getCurrentPopulationNum() - getPopulationCapacity();
		if (overCrowding > 0) {
			double stressModifier = .1D * overCrowding * time;
			Iterator<Person> i = getInhabitants().iterator();
			while (i.hasNext()) {
				PhysicalCondition condition = i.next().getPhysicalCondition();
				condition.setStress(condition.getStress() + stressModifier);
			}
		}

		// TODO: what to take into consideration the presence of robots ?
		// If no current population at settlement for one sol, power down the
		// building and turn the heat off.
		if (getCurrentPopulationNum() == 0) {
			zeroPopulationTime += time;
			if (zeroPopulationTime > 1000D) {
				powerGrid.setPowerMode(PowerMode.POWER_DOWN);
				thermalSystem.setHeatMode(HeatMode.HEAT_OFF);
			}
		} else {
			zeroPopulationTime = 0D;
			powerGrid.setPowerMode(PowerMode.POWER_UP);
			// TODO: check if POWER_UP is necessary
			// Question: is POWER_UP a prerequisite of FULL_POWER ?
			// thermalSystem.setHeatMode(HeatMode.POWER_UP);
		}

		powerGrid.timePassing(time);

		thermalSystem.timePassing(time);

		buildingManager.timePassing(time);

		// 2015-01-09 Added makeDailyReport()
		performEndOfDayTasks(); // NOTE: also update solCache in makeDailyReport()

		// Sample a data point every RECORDING_FREQUENCY (in millisols)
	    int millisols =  (int) clock.getMillisol();
	    int quotient = millisols / RECORDING_FREQUENCY ;

	    if (quotientCache != quotient && quotient != 4) {
		    //System.out.println("Yes sampling at millisols : " + millisols + " quotient : " + quotient);
	    	// take a sample for each critical resource
	    	sampleAllResources();
	    	quotientCache = quotient;
	    }

		// Check every RECORDING_FREQUENCY (in millisols)
	    int quotient1 = millisols / RECORDING_FREQUENCY ;

	    if (quotient1Cache != quotient1 && quotient1 != 10) {
	    	// Compute whether a baseline, GCR, or SEP event has occurred
	    	checkRadiationProbability(time);
	    	quotient1Cache = quotient1;
	    }

	    // Updates the goodsManager once per sol at random time.
	    // Why random time?
	    updateGoodsManager(time);

		// 2015-04-18 Added updateRegistry();
		// updateRegistry();
	}

	public void sampleAllResources() {

        for (int i= 0; i < NUM_CRITICAL_RESOURCES; i++) {
        	sampleOneResource(i);
        }
	}

	public void sampleOneResource(int resourceType) {
	     String resource = null;

			if (resourceType == 0) {
				resource = LifeSupportType.OXYGEN;
			}
			else if (resourceType == 1) {
				resource = "hydrogen";
			}
			else if (resourceType == 2) {
				resource = "carbon dioxide";
				}
			else if (resourceType == 3) {
				resource = "methane";
				}
			else if (resourceType == 4) {
				resource = LifeSupportType.WATER;
			}
			else if (resourceType == 5) {
				resource = "grey water";
			}
			else if (resourceType == 6) {
				resource = "black water";
			}
			else if (resourceType == 7) {
				resource = "rock samples";
			}
			else if (resourceType == 8) {
				resource = "ice";
			}

			AmountResource ar = AmountResource.findAmountResource(resource);
			//double newAmount = inv.getAmountResourceStored(ar, false);
			//setOneResource(resourceType, newAmount);
	//}
	/*
	 * Saves the amount of a resource onto the resourceStat map
	 */
	//public void setOneResource(int resourceType, double newAmount) {

		//if (resourceStat.get(solCache) != null) {
			if (resourceStat.containsKey(solCache)) {
				Map<Integer, List<Double>> todayMap = resourceStat.get(solCache);
			//Map<Integer, List<Double>> todayMap = resourceStat.get(solCache);
			//if (todayMap != null) {
				//List<Double> list = todayMap.get(resourceType);
				//if (list != null) {
				if (todayMap.containsKey(resourceType)) {
					List<Double> list = todayMap.get(resourceType);
					double newAmount = inv.getAmountResourceStored(ar, false);
					list.add(newAmount);
					//todayMap.put(resourceType, list); // is it needed?
					//resourceStat.put(solCache, todayMap); // is it needed?
					//System.out.println(resourceType + " : " + list.get(list.size()-1) + " added");
				}

				else {
					List<Double> list = new ArrayList<>();
					double newAmount = inv.getAmountResourceStored(ar, false);
					list.add(newAmount);
					//System.out.println(resourceType + " : " + list.get(list.size()-1) + " added");
					todayMap.put(resourceType, list);
					//resourceStat.put(solCache, todayMap); // is it needed?
					//System.out.println("add a new amount and a new resource map");
				}

			} else {
				List<Double> list = new ArrayList<>();
				Map<Integer, List<Double>> todayMap = new HashMap<>();
				double newAmount = inv.getAmountResourceStored(ar, false);
				list.add(newAmount);
				//System.out.println(resourceType + " : " + list.get(list.size()-1) + " added");
				todayMap.put(resourceType, list);
				resourceStat.put(solCache, todayMap);
				//System.out.println("add a new amount, a new resource map and a new sol");
			}
		//}
	}


	/*
	 * Gets the average amount of a resource on a particular sol
	 */
	// Called by getOneResource() in MarqueeTicker.java
	public double getAverage(int solType, int resourceType) {
		int sol = 0;
		if (solType == 0) // today's average
			sol = solCache;
		else if (solType == -1) // yesterday's average
			sol = solCache - 1;

		int size = 0;
		double average = 0;

		if (resourceStat.containsKey(sol)) {
			Map<Integer, List<Double>> map = resourceStat.get(sol);
			//System.out.println("Sol " + solCache + " : yes to resourceStat.containsKey(" + sol + ")");
			//System.out.println("map.containsKey(resourceType) is " + map.containsKey(resourceType));
			if (map.containsKey(resourceType)) {
				List<Double> list = map.get(resourceType);
				//System.out.println("sol : " + solCache + "   solType : "  + solType + "   list is " + list);
				size = list.size();
				for (int i = 0; i < size; i++) {
					average += list.get(i);
					//System.out.println("list.get(i) is " + list.get(i));
				}

				//System.out.println("size is " + size + "   average is " + average);
				average = average/size;

			}
			else {
				average = 0; // how long will it be filled ? ?
			}

		}
		else
			average = 0;

		//if (size != 0)
		//	average = average/size;

		return average;
	}

	/*
	 * // 2015-04-18 updateRegistry() public void updateRegistry() {
	 *
	 * List<SettlementRegistry> settlementList =
	 * MultiplayerClient.getInstance().getSettlementRegistryList();
	 *
	 * int clientID = Integer.parseInt( st.nextToken().trim() );
	 *
	 * String template = st.nextToken().trim(); int pop = Integer.parseInt(
	 * st.nextToken().trim() ); int bots = Integer.parseInt(
	 * st.nextToken().trim() ); double lat = Double.parseDouble(
	 * st.nextToken().trim() ); double lo = Double.parseDouble(
	 * st.nextToken().trim() );
	 *
	 *
	 * settlementList.forEach( s -> { String pn = s.getPlayerName(); String sn =
	 * s.getName(); if (pn.equals(playerName) && sn.equals(name))
	 * s.updateRegistry(playerName, clientID, name, template, pop, bots, lat,
	 * lo); });
	 *
	 * }
	 */
	/**
	 * Provides the daily statistics on inhabitant's food energy intake
	 *
	 */
	// 2015-01-09 Added getFoodEnergyIntakeReport()
	public synchronized void getFoodEnergyIntakeReport() {
		// System.out.println("\n<<< Sol " + solCache + " End of Day Food Energy
		// Intake Report at " + this.getName() + " >>>");
		// System.out.println("** An settler on Mars is estimated to consume
		// about 10100 kJ per sol **");
		// Iterator<Person> i = getInhabitants().iterator();
		Iterator<Person> i = getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			Person p = i.next();
			PhysicalCondition condition = p.getPhysicalCondition();
			double energy = Math.round(condition.getEnergy() * 100.0) / 100.0;
			String name = p.getName();
			System.out.print(name + " : " + energy + " kJ" + "\t");
		}
	}

	/**
	 * Provides the daily reports for the settlement
	 */
	// 2015-01-09 Added makeDailyReport()
	public synchronized void performEndOfDayTasks() {

		if (clock == null)
			clock = Simulation.instance().getMasterClock().getMarsClock();

		// check for the passing of each day
		int solElapsed = MarsClock.getSolOfYear(clock);

		if (solElapsed != solCache) {

			// getFoodEnergyIntakeReport();

			//printWorkShift("Sol "+ solCache);
			reassignWorkShift();
			//printWorkShift("Sol "+ solElapsed);

			if (resourceStat.size() > 30)
				resourceStat.remove(0);

			//if (counter30 == 31) {
			//	resourceStat.remove(0);
				//resourceStat.clear();
				//resourceStat = new HashMap<>();
				//counter30--;
			//}
			//else
			//	counter30++;

			// 2015-12-05 Called inflateSleepHabit()
			Collection<Person> people = getInhabitants();
			for (Person p : people) {
				p.getPhysicalCondition().inflateSleepHabit();
			}

			solCache = solElapsed;

			// getSupplyDemandReport(solElapsed);
			refreshMapDaily(solElapsed);
		}
	}

	public void printWorkShift(String text) {
		logger.info(text + " " + getName() + " work shift status => A:" + numA + " B:" + numB
				+ " X:" + numX + " Y:" + numY + " Z:" + numZ + " OnCall:" + numOnCall);
				// + " Off:" + numOff);
	}

	/*
	 * Reassigns the work shift for all
	 */
	// 2015-11-04 Added reassignWorkShift()
	// TODO: should call this method at, say, 800 millisols, not right at 1000 millisols
	public void reassignWorkShift() {

		Collection<Person> people = getInhabitants();
		int pop = people.size();

		int numShift = 0;

		if (pop == 1) {
			numShift = 1;
		} else if (pop < UnitManager.THREE_SHIFTS_MIN_POPULATION) {
			numShift = 2;
		} else {// if pop => 6
			numShift = 3;
		}

		setNumShift(numShift);

		for (Person p : people) {

			if (p.getMind().getMission() == null
					&& p.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

				// 2015-12-05 Check if person is an astronomer.
	            boolean isAstronomer = (p.getMind().getJob() instanceof Astronomer);

	            if (isAstronomer) {
	            	// TODO: find the darkest time of the day
	            	// and set work shift to cover time period

	            	// For now, we may assume it will usually be X or Z, but Y
	            	// since Y is usually where midday is at unless a person is at polar region.
	            	ShiftType oldShift = p.getTaskSchedule().getShiftType();
	            	if (oldShift == ShiftType.Y) {

	            		boolean x_ok = isWorkShiftSaturated(ShiftType.X, false);
	            		boolean z_ok = isWorkShiftSaturated(ShiftType.Z, false);

		            	// TODO: Instead of throwing a dice,
	            		// take the shift that has less sunlight
	            		int rand;
	                	ShiftType newShift = null;

	            		if (x_ok && z_ok) {
	            			rand = RandomUtil.getRandomInt(1);
		            		if (rand == 0)
		            			newShift = ShiftType.X;
		            		else
		            			newShift = ShiftType.Z;
	            		}
	            		else if (x_ok)
	            			newShift = ShiftType.X;
	            		else if (z_ok)
	            			newShift = ShiftType.Z;

	            		p.setShiftType(newShift);
						//System.out.println(p + " old shift : " + oldShift + " new shift : " + newShift);

	            	}

	            } // end of if (isAstronomer)

	            else {

	            	// 2015-12-05 check if the current shift is still open and keep it if possible
					ShiftType oldShift = p.getTaskSchedule().getShiftType();

					if (oldShift == ShiftType.ON_CALL) {

						// TODO: check a person's sleep habit map and request changing his work shift
						// to avoid taking a work shift that overlaps his sleep hour
						ShiftType newShift = getAEmptyWorkShift(pop);
						if (newShift != oldShift) { // sanity check
							//System.out.println(this.getName() + "-- " + p + "'s old shift : " + oldShift + ",  new shift : " + newShift);
							p.setShiftType(newShift);
						}
					}

					else {
						// Note: if a person's shift is NOT saturated, he doesn't need to change shift
						boolean oldShift_ok = isWorkShiftSaturated(oldShift, true);

						// TODO: check a person's sleep habit map and request changing his work shift
						// to avoid taking a work shift that overlaps his sleep hour

						if (!oldShift_ok) {
							// if a person's shift is saturated, he will need to change shift
							ShiftType newShift = getAEmptyWorkShift(pop);
							if (newShift != oldShift) { // sanity check
								//System.out.println(this.getName() + "-- " + p + "'s old shift : " + oldShift + ",  new shift : " + newShift);
								p.setShiftType(newShift);
							}
						}
					}
	            } // end of if (isAstronomer)
			}
			// Just for sanity check for those on a vehicle mission
			// Note: shouldn't be needed this way but currently, when currently when starting a trade mission,
			// the code fails to change a person's work shift to On-call.
			else if (p.getMind().getMission() != null || p.getLocationSituation() == LocationSituation.IN_VEHICLE) {
				ShiftType oldShift = p.getTaskSchedule().getShiftType();
				if (oldShift != ShiftType.ON_CALL) {
					System.out.println(p + " old shift : " + oldShift + " new shift : " + ShiftType.ON_CALL);
					p.setShiftType(ShiftType.ON_CALL);
				}
			}
		}
	}

	/**
	 * Provides the daily demand statistics on sample amount resources
	 */
	// 2015-01-15 Added supply data
	public void getSupplyDemandReport(int solElapsed) {

		logger.info("<<< Sol " + solElapsed + " at " + this.getName()
				+ " End of Day Report of Amount Resource Supply and Demand Statistics >>>");

		String sample1 = "polyethylene";
		String sample2 = "concrete";

		// Sample supply and demand data on Potato and Water

		double supplyAmount1 = inv.getAmountSupplyAmount(sample1);
		double supplyAmount2 = inv.getAmountSupplyAmount(sample2);

		int supplyRequest1 = inv.getAmountSupplyRequest(sample1);
		int supplyRequest2 = inv.getAmountSupplyRequest(sample2);

		double demandAmount1 = inv.getAmountDemandAmount(sample1);
		double demandAmount2 = inv.getAmountDemandAmount(sample2);

		// int totalRequest1 = inv.getDemandTotalRequest(sample1);
		// int totalRequest2 = inv.getDemandTotalRequest(sample2);

		int demandSuccessfulRequest1 = inv.getAmountDemandMetRequest(sample1);
		int demandSuccessfulRequest2 = inv.getAmountDemandMetRequest(sample2);

		// int numOfGoodsInDemandAmountMap = inv.getDemandAmountMapSize();
		// int numOfGoodsInDemandTotalRequestMap =
		// inv.getDemandTotalRequestMapSize();
		// int numOfGoodsInDemandSuccessfulRequestMap =
		// inv.getDemandSuccessfulRequestMapSize();

		// logger.info(" numOfGoodsInDemandRequestMap : " +
		// numOfGoodsInDemandTotalRequestMap);
		// logger.info(" numOfGoodsInDemandSuccessfulRequestMap : " +
		// numOfGoodsInDemandSuccessfulRequestMap);
		// logger.info(" numOfGoodsInDemandAmountMap : " +
		// numOfGoodsInDemandAmountMap);

		logger.info(sample1 + " Supply Amount : " + Math.round(supplyAmount1 * 100.0) / 100.0);
		logger.info(sample1 + " Supply Request : " + supplyRequest1);

		logger.info(sample1 + " Demand Amount : " + Math.round(demandAmount1 * 100.0) / 100.0);
		// logger.info(sample1 + " Demand Total Request : " + totalRequest1);
		logger.info(sample1 + " Demand Successful Request : " + demandSuccessfulRequest1);

		logger.info(sample2 + " Supply Amount : " + Math.round(supplyAmount2 * 100.0) / 100.0);
		logger.info(sample2 + " Supply Request : " + supplyRequest2);

		logger.info(sample2 + " Demand Amount : " + Math.round(demandAmount2 * 100.0) / 100.0);
		// logger.info(sample2 + " Demand Total Request : " + totalRequest2);
		logger.info(sample2 + " Demand Successful Request : " + demandSuccessfulRequest2);

	}

	/**
	 * Refreshes the supply and demand data and weather data
	 */
	// 2015-02-13 Added refreshMapDaily()
	public void refreshMapDaily(int solElapsed) {

		boolean clearNow;

		// clearNow = true if solElapsed is an exact multiple of x
		// Clear maps once every x number of days
		if (solElapsed % SOL_PER_REFRESH == 0)
			clearNow = true;
		else
			clearNow = false;

		// Should clear only once and at the beginning of the day
		if (clearNow) {
			// carry out the daily average of the previous 5 days
			inv.compactAmountSupplyAmountMap(SOL_PER_REFRESH);
			inv.clearAmountSupplyRequestMap();

			inv.compactAmountDemandAmountMap(SOL_PER_REFRESH);
			inv.clearAmountDemandTotalRequestMap();
			inv.clearAmountDemandMetRequestMap();

			// 2015-03-06 Added clearing of weather data map
			Simulation.instance().getMars().getWeather().clearMap();

			logger.info("Just compacted supply/demand data map and cleared weather data maps at " + name);
		}

	}

	/**
	 * Updates the GoodsManager
	 *
	 * @param time
	 */
	private void updateGoodsManager(double time) {

		// Randomly update goods manager 1 time per Sol.
		if (!goodsManager.isInitialized() || (time >= RandomUtil.getRandomDouble(1000D))) {
			goodsManager.timePassing(time);
		}
	}

	/**
	 * Gets a collection of people affected by this entity.
	 *
	 * @return person collection
	 */
	// TODO: will this method be called by robots?
	public Collection<Person> getAffectedPeople() {
		Collection<Person> people = new ConcurrentLinkedQueue<Person>(getInhabitants());

		// Check all people.
		Iterator<Person> i = Simulation.instance().getUnitManager().getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			Task task = person.getMind().getTaskManager().getTask();

			// Add all people maintaining this settlement.
			if (task instanceof Maintenance) {
				if (((Maintenance) task).getEntity() == this) {
					if (!people.contains(person))
						people.add(person);
				}
			}

			// Add all people repairing this settlement.
			if (task instanceof Repair) {
				if (((Repair) task).getEntity() == this) {
					if (!people.contains(person))
						people.add(person);
				}
			}
		}

		return people;
	}

	/**
	 * Gets the settlement's building manager.
	 *
	 * @return building manager
	 */
	public BuildingManager getBuildingManager() {
		return buildingManager;
	}

	/**
	 * Gets the settlement's building connector manager.
	 *
	 * @return building connector manager.
	 */
	public BuildingConnectorManager getBuildingConnectorManager() {
		return buildingConnectorManager;
	}

	/**
	 * Gets the settlement's goods manager.
	 *
	 * @return goods manager
	 */
	public GoodsManager getGoodsManager() {
		return goodsManager;
	}

	/**
	 * Gets the closest available airlock to a person.
	 *
	 * @param person
	 *            the person.
	 * @return airlock or null if none available.
	 */
	public Airlock getClosestAvailableAirlock(Person person) {
		Airlock result = null;

		double leastDistance = Double.MAX_VALUE;
		BuildingManager manager = buildingManager;
		Iterator<Building> i = manager.getBuildings(BuildingFunction.EVA).iterator();
		while (i.hasNext()) {
			Building building = i.next();

			double distance = Point2D.distance(building.getXLocation(), building.getYLocation(), person.getXLocation(),
					person.getYLocation());
			if (distance < leastDistance) {
				EVA eva = (EVA) building.getFunction(BuildingFunction.EVA);
				result = eva.getAirlock();
				leastDistance = distance;
			}
		}

		return result;
	}

	public Airlock getClosestAvailableAirlock(Robot robot) {
		Airlock result = null;

		double leastDistance = Double.MAX_VALUE;
		BuildingManager manager = buildingManager;
		Iterator<Building> i = manager.getBuildings(BuildingFunction.EVA).iterator();
		while (i.hasNext()) {
			Building building = i.next();

			double distance = Point2D.distance(building.getXLocation(), building.getYLocation(), robot.getXLocation(),
					robot.getYLocation());
			if (distance < leastDistance) {
				EVA eva = (EVA) building.getFunction(BuildingFunction.EVA);
				result = eva.getAirlock();
				leastDistance = distance;
			}
		}

		return result;
	}

	/**
	 * Gets the closest available airlock at the settlement to the given
	 * location. The airlock must have a valid walkable interior path from the
	 * person's current location.
	 *
	 * @param person
	 *            the person.
	 * @param xLocation
	 *            the X location.
	 * @param yLocation
	 *            the Y location.
	 * @return airlock or null if none available.
	 */
	public Airlock getClosestWalkableAvailableAirlock(Person person, double xLocation, double yLocation) {
		Airlock result = null;
		Building currentBuilding = BuildingManager.getBuilding(person);

		if (currentBuilding == null) {
			//throw new IllegalStateException(person.getName() + " is not currently in a building.");	//throw new IllegalStateException(robot.getName() + " is not currently in a building.");
			// this major bug is due to getBuilding(robot) above in BuildingManager
			// what if a person is out there in ERV building for maintenance. ERV building has no LifeSupport function. currentBuilding will be null
			System.err.println(person.getName() + " is not currently in a building.");
			return null;
		}

		result = getAirlock(currentBuilding, xLocation, yLocation);

		return result;
	}

	public Airlock getClosestWalkableAvailableAirlock(Robot robot, double xLocation, double yLocation) {
		Airlock result = null;
		Building currentBuilding = BuildingManager.getBuilding(robot);

		if (currentBuilding == null) {
			//throw new IllegalStateException(robot.getName() + " is not currently in a building.");
			// this major bug is due to getBuilding(robot) above in BuildingManager
			// need to refine the concept of where a robot can go. They are thought to need RoboticStation function to "survive",
			// much like a person who needs LifeSupport function
			System.err.println(robot.getName() + " is not currently in a building.");
			return null;
		}

		result = getAirlock(currentBuilding, xLocation, yLocation);

		return result;
	}

	public Airlock getAirlock(Building currentBuilding, double xLocation, double yLocation) {
		Airlock result = null;

		double leastDistance = Double.MAX_VALUE;
		BuildingManager manager = buildingManager;
		Iterator<Building> i = manager.getBuildings(BuildingFunction.EVA).iterator();
		while (i.hasNext()) {
			Building building = i.next();

			if (buildingConnectorManager.hasValidPath(currentBuilding, building)) {

				double distance = Point2D.distance(building.getXLocation(), building.getYLocation(), xLocation,
						yLocation);
				if (distance < leastDistance) {
					EVA eva = (EVA) building.getFunction(BuildingFunction.EVA);
					result = eva.getAirlock();
					leastDistance = distance;
				}
			}
		}
		return result;
	}

	/**
	 * Gets the closest available airlock at the settlement to the given
	 * location. The airlock must have a valid walkable interior path from the
	 * given building's current location.
	 *
	 * @param building
	 *            the building in the walkable interior path.
	 * @param xLocation
	 *            the X location.
	 * @param yLocation
	 *            the Y location.
	 * @return airlock or null if none available.
	 */
	public Airlock getClosestWalkableAvailableAirlock(Building building, double xLocation, double yLocation) {
		Airlock result = null;

		double leastDistance = Double.MAX_VALUE;
		BuildingManager manager = buildingManager;
		Iterator<Building> i = manager.getBuildings(BuildingFunction.EVA).iterator();
		while (i.hasNext()) {
			Building nextBuilding = i.next();

			if (buildingConnectorManager.hasValidPath(building, nextBuilding)) {

				double distance = Point2D.distance(nextBuilding.getXLocation(), nextBuilding.getYLocation(), xLocation,
						yLocation);
				if (distance < leastDistance) {
					EVA eva = (EVA) nextBuilding.getFunction(BuildingFunction.EVA);
					result = eva.getAirlock();
					leastDistance = distance;
				}
			}
		}

		return result;
	}

	/**
	 * Checks if a building has a walkable path from it to an airlock.
	 *
	 * @param building
	 *            the building.
	 * @return true if an airlock is walkable from the building.
	 */
	public boolean hasWalkableAvailableAirlock(Building building) {
		return (getClosestWalkableAvailableAirlock(building, 0D, 0D) != null);
	}

	/**
	 * Gets the number of airlocks at the settlement.
	 *
	 * @return number of airlocks.
	 */
	public int getAirlockNum() {
		return buildingManager.getBuildings(BuildingFunction.EVA).size();
	}

	/**
	 * Gets the settlement's power grid.
	 *
	 * @return the power grid.
	 */
	public PowerGrid getPowerGrid() {
		return powerGrid;
	}

	/**
	 * Gets the settlement's heating system.
	 *
	 * @return thermalSystem.
	 */
	public ThermalSystem getThermalSystem() {
		return thermalSystem;
	}

	/**
	 * Gets the settlement template.
	 *
	 * @return template as string.
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * Gets all people associated with this settlement, even if they are out on
	 * missions.
	 *
	 * @return collection of associated people.
	 */
	public Collection<Person> getAllAssociatedPeople() {
		Collection<Person> result = new ConcurrentLinkedQueue<Person>();

		Iterator<Person> i = Simulation.instance().getUnitManager().getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (person.getAssociatedSettlement() == this)
				result.add(person);
		}

		return result;
	}


	/**
	 * Checks if the settlement has a particular person
	 * @param a person
	 * @return boolean
	 */
	//2015-12-01 Added hasPerson()
	public boolean hasPerson(Person aPerson) {
		boolean result = false;
		Collection<Person> list = getAllAssociatedPeople();

		Iterator<Person> i = list.iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (person.equals(aPerson))
				result = true;
		}

		return result;
	}

	/**
	 * Checks if the settlement contains a particular person with a given name (first or last)
	 * @param a person's first/last name
	 * @return 0 if none found, 1 if uniquely found, -1 if dead, 2 to n if not uniquely found

	//2015-12-01 Added hasPersonName()
	public int hasPersonName(String aName) {
		aName = aName.trim();
		String initial = null;
		boolean hasASpace = aName.contains(" ");
		int found = 0;
		int s_Index = 0;
		int dead = 0;

		int len = aName.length();
		boolean hasInitial = len > 3 && hasASpace;

		if (hasInitial) {
			for (int i = 0 ; i < len ; i++) {
		        if (aName.charAt(i) == ' ')
		        	s_Index = i;
			}

			if (s_Index == len-2) {
				// e.g. Cory_S
				initial = aName.substring(len-1, len);
				aName = aName.substring(0, len-2);
		    	//System.out.println("initial is " + initial);
		    	//System.out.println("aName is " + aName);
			}
			else if (s_Index == 1) {
				// e.g. S_Cory
				initial = aName.substring(0);
				aName = aName.substring(2, len);
		    	//System.out.println("initial is " + initial);
		    	//System.out.println("aName is " + aName);
			}
		}

		Collection<Person> list = getAllAssociatedPeople();

		Iterator<Person> i = list.iterator();
		while (i.hasNext()) {
			Person person = i.next();
			// Case 1: if aName is a full name
			if (hasASpace && person.getName().equalsIgnoreCase(aName)){
				found++;
				if (person.getPhysicalCondition().isDead())
					dead--;
			}
			else if (hasInitial) {
				// Case 2: if aName is a first name + space + last initial
				if (person.getName().toLowerCase().contains((aName + " " + initial).toLowerCase())) {
					found++;
				}
				// Case 3: if aName is a first initial + space + last name
				else if (person.getName().toLowerCase().contains((initial + " " + aName).toLowerCase())) {
					found++;
				}
			}
			else {
				String first = null;
				String last = null;
				String full = person.getName();
				int len1 = full.length();
				//int index1 = 0;

				for (int j = 0 ; j < len1 ; j++) {
			        if (full.charAt(j) == ' ') {
			        	//index1 = j;
				        first = full.substring(0, j);
				        last = full.substring(j+1, len1);
				        break;
			        }
				}


				// Case 4: if aName is a last name
				if (first.equalsIgnoreCase(aName)) {
					found++;
				}
				// Case 5: if aName is a first name
				else if (last.equalsIgnoreCase(aName)) {
					found++;
				}
			}
		}

		if (dead == -1)
			return -1;
		else
			return found;
	}
*/

	/**
	 * Returns a list of persons with a given name (first or last)
	 * @param a person's first/last name
	 * @return a list of persons
	 */
	//2015-12-21 Added returnPersonList()
	public List<Person> returnPersonList(String aName) {
		List<Person> personList = new ArrayList<>();
		aName = aName.trim();
		String initial = null;
		boolean hasASpace = aName.contains(" ");
		int found = 0;
		int s_Index = 0;
		int dead = 0;

		int len = aName.length();
		boolean hasInitial = len > 3 && hasASpace;

		if (hasInitial) {
			for (int i = 0 ; i < len ; i++) {
		        if (aName.charAt(i) == ' ')
		        	s_Index = i;
			}

			if (s_Index == len-2) {
				// e.g. Cory_S
				initial = aName.substring(len-1, len);
				aName = aName.substring(0, len-2);
		    	//System.out.println("initial is " + initial);
		    	//System.out.println("aName is " + aName);
			}
			else if (s_Index == 1) {
				// e.g. S_Cory
				initial = aName.substring(0);
				aName = aName.substring(2, len);
		    	//System.out.println("initial is " + initial);
		    	//System.out.println("aName is " + aName);
			}
		}

		Collection<Person> list = getAllAssociatedPeople();

		Iterator<Person> i = list.iterator();
		while (i.hasNext()) {
			Person person = i.next();
			// Case 1: if aName is a full name
			if (hasASpace && person.getName().equalsIgnoreCase(aName)){
				//found++;
				personList.add(person);
				//if (person.getPhysicalCondition().isDead())
					//dead--;
				//	personList.add(person);
			}
			else if (hasInitial) {
				// Case 2: if aName is a first name + space + last initial
				if (person.getName().toLowerCase().contains((aName + " " + initial).toLowerCase())) {
					//found++;
					personList.add(person);
				}
				// Case 3: if aName is a first initial + space + last name
				else if (person.getName().toLowerCase().contains((initial + " " + aName).toLowerCase())) {
					//found++;
					personList.add(person);
				}
			}
			else {
				String first = null;
				String last = null;
				String full = person.getName();
				int len1 = full.length();
				//int index1 = 0;

				for (int j = 0 ; j < len1 ; j++) {
			        if (full.charAt(j) == ' ') {
			        	//index1 = j;
				        first = full.substring(0, j);
				        last = full.substring(j+1, len1);
				        break;
			        }
				}


				// Case 4: if aName is a last name
				if (first.equalsIgnoreCase(aName)) {
					//found++;
					personList.add(person);
				}
				// Case 5: if aName is a first name
				else if (last.equalsIgnoreCase(aName)) {
					//found++;
					personList.add(person);
				}
			}
		}


		return personList;
	}

	/**
	 * Checks if the settlement contains a bot
	 * @param a bot's name
	 * @return 0 if none found, 1 if uniquely found, 2 if uniquely found but dead, -1...-n if not uniquely found
	 *

	//2015-12-01 Added hasRobotName()
	public int hasRobotName(String aName) {
		aName = aName.trim();
		//boolean isFullName = aName.trim().contains(" ");
		aName = aName.replace(" ", "");
		int found = 0;
		int dead = 0;
		int size = aName.length();

		Collection<Robot> list = getAllAssociatedRobots();

		Iterator<Robot> i = list.iterator();
		while (i.hasNext()) {
			Robot robot = i.next();
			// Case 1: exact match e.g. chefbot001
			if (robot.getName().replace(" ", "").equalsIgnoreCase(aName)){
				found++;
				if (robot.getPhysicalCondition().isDead())
					dead--;
			}
			// Case 2: some parts are matched
			else {
				// Case 2 e.g. chefbot, chefbot0_, chefbot0__, chefbot1_, chefbot00_, chefbot01_
				// need more information !
				if (robot.getName().replace(" ", "").contains(aName)) {
					found++;
				}

				else { // Case 3 e.g. filter out semi-good names such as chefbot01, chefbot1,  chefbot10 from bad/invalid name

					String last4digits = aName.substring(size-4, size-1);
					char first = last4digits.charAt(0);
					char second = last4digits.charAt(1);
					char third = last4digits.charAt(2);
					char fourth = last4digits.charAt(3);

					boolean firstIsDigit = (first >= '0' && first <= '9');
					boolean secondIsDigit = (second >= '0' && second <= '9');
					boolean thirdIsDigit = (third >= '0' && third <= '9');
					boolean fourthIsDigit = (fourth >= '0' && fourth <= '9');

					// Case 3A : e.g. chefbot0003 -- a typo having an extra zero
					if (size >= 11 && !firstIsDigit && secondIsDigit && thirdIsDigit && fourthIsDigit) {
						if (first == '0' && second == '0') {
							aName = aName.substring(0, size-4) + aName.substring(size-3, size-1);
							int result = checkRobotName(robot, aName);
							if (result < 0)
								dead = -1;
							else
								found = result;
						}
					}

					// Case 3B : e.g. chefbot01 -- a typo missing a zero
					else if (size >= 10 && !firstIsDigit && !secondIsDigit && thirdIsDigit && fourthIsDigit) {
						aName = aName.substring(0, size-2) + "0" + aName.substring(size-2, size-1);
						int result = checkRobotName(robot, aName);
						if (result < 0)
							dead = -1;
						else
							found = result;
					}

					// Case 3C : e.g. chefbot1 -- a typo missing two zeroes
					else if (size >= 9 && !firstIsDigit && !secondIsDigit && !thirdIsDigit && fourthIsDigit) {
						aName = aName.substring(0, size-2) + "0" + aName.substring(size-2, size-1);
						int result = checkRobotName(robot, aName);
						if (result < 0)
							dead = -1;
						else
							found = result;
					}
				}

			}
		}
		if (dead < 0)
			return -1;
		else
			return found;
	}
*/
	/**
	 * Returns a list of robots containing a particular name
	 * @param a bot's name
	 * @return a list of robots
	 *
	 */
	//2015-12-21 Added returnRobotList()
	public List<Robot> returnRobotList(String aName) {
		List<Robot> robotList = new ArrayList<>();
		aName = aName.trim();
		aName = aName.replace(" ", "");

		String last4digits = null;
		char first = '?';
		char second = '?';
		char third = '?';
		char fourth = '?';

		boolean firstIsDigit = false;
		boolean secondIsDigit = false;
		boolean thirdIsDigit = false;
		boolean fourthIsDigit = false;

		int size = aName.length();

		if (size >= 8) {
			last4digits = aName.substring(size-4, size);
			first = last4digits.charAt(0);
			second = last4digits.charAt(1);
			third = last4digits.charAt(2);
			fourth = last4digits.charAt(3);

			//System.out.println("The last 4 are : " + first + second + third + fourth);

			firstIsDigit = (first >= '0' && first <= '9');
			secondIsDigit = (second >= '0' && second <= '9');
			thirdIsDigit = (third >= '0' && third <= '9');
			fourthIsDigit = (fourth >= '0' && fourth <= '9');
		}

		Collection<Robot> list = getAllAssociatedRobots();

		Iterator<Robot> i = list.iterator();
		while (i.hasNext()) {
			Robot robot = i.next();
			// Case 1: exact match e.g. chefbot001
			if (robot.getName().replace(" ", "").equalsIgnoreCase(aName)){
				robotList.add(robot);
			}
			// Case 2: some parts are matched
			else {
				// Case 2 e.g. chefbot, chefbot0_, chefbot0__, chefbot1_, chefbot00_, chefbot01_
				// need more information !
				if (robot.getName().replace(" ", "").toLowerCase().contains(aName.toLowerCase())) {
					//System.out.println("aName is a part of " + robot.getName().replace(" ", ""));
					robotList.add(robot);
				}

				else if (size >= 8) { // Case 3 e.g. filter out semi-good names such as chefbot01, chefbot1,  chefbot10 from bad/invalid name

					// Case 3A : e.g. chefbot0003 -- a typo having an extra zero before the digit
					if (size >= 11 && !firstIsDigit && secondIsDigit && thirdIsDigit && fourthIsDigit) {
						if (first == '0' && second == '0') {
							String newName = aName.substring(0, size-4) + aName.substring(size-3, size);
							//System.out.println("Case 3A: newName is : " + newName);
							boolean result = checkRobotName(robot, newName);
							if (result)
								robotList.add(robot);
						}
					}

					// Case 3B : e.g. chefbot01 or chefbot11 -- a typo missing a zero before the digit
					else if (size >= 9 && !firstIsDigit && !secondIsDigit && thirdIsDigit && fourthIsDigit) {
						String newName = aName.substring(0, size-2) + "0" + aName.substring(size-2, size);
						//System.out.println("Case 3B: newName is : " + newName);
						boolean result = checkRobotName(robot, newName);
						if (result)
							robotList.add(robot);
					}

					// Case 3C : e.g. chefbot1 -- a typo missing two zeroes before the digit
					else if (size >= 8 && !firstIsDigit && !secondIsDigit && !thirdIsDigit && fourthIsDigit) {
						String newName = aName.substring(0, size-1) + "00" + aName.substring(size-1, size);
						//System.out.println("Case 3C: newName is : " + newName);
						boolean result = checkRobotName(robot, newName);
						if (result)
							robotList.add(robot);
					}
				}

			}
		}

		//System.out.println("robotList's size is " + robotList.size());
		return robotList;
	}

	/**
	 * Checks against the input name with the robot name
	 * @param robot
	 * @param aName
	 * @return
	 */
	// 2015-12-16 Added checkRobotName()
	public boolean checkRobotName(Robot robot, String aName) {

		//System.out.println("modified aName is " + aName);
		//aName = aName.substring(0, size-2) + "0" + aName.substring(size-2, size-1);
		if (robot.getName().replace(" ", "").equalsIgnoreCase(aName)){
			return true;
		}
		else
			return false;
	}

	/**
	 * Gets all Robots associated with this settlement, even if they are out on missions.
	 * @return collection of associated Robots.
	 */
	public Collection<Robot> getAllAssociatedRobots() {
		Collection<Robot> result = new ConcurrentLinkedQueue<Robot>();

		Iterator<Robot> i = Simulation.instance().getUnitManager().getRobots().iterator();
		while (i.hasNext()) {
			Robot robot = i.next();
			if (robot.getAssociatedSettlement() == this)
				result.add(robot);
		}

		return result;
	}

	/**
	 * Gets all vehicles associated with this settlement, even if they are out
	 * on missions.
	 *
	 * @return collection of associated vehicles.
	 */
	public Collection<Vehicle> getAllAssociatedVehicles() {
		Collection<Vehicle> result = getParkedVehicles();

		// Also add vehicle mission vehicles not parked at settlement.
		Iterator<Mission> i = Simulation.instance().getMissionManager().getMissionsForSettlement(this).iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !this.equals(vehicle.getSettlement()))
					result.add(vehicle);
			}
		}

		return result;
	}

	/**
	 * Sets the mission creation override flag.
	 *
	 * @param missionCreationOverride
	 *            override for settlement mission creation.
	 */
	public void setMissionCreationOverride(boolean missionCreationOverride) {
		this.missionCreationOverride = missionCreationOverride;
	}

	/**
	 * Gets the mission creation override flag.
	 *
	 * @return override for settlement mission creation.
	 */
	public boolean getMissionCreationOverride() {
		return missionCreationOverride;
	}

	/**
	 * Sets the construction override flag.
	 *
	 * @param constructionOverride
	 *            override for settlement construction/salvage mission creation.
	 */
	public void setConstructionOverride(boolean constructionOverride) {
		this.constructionOverride = constructionOverride;
	}

	/**
	 * Gets the construction override flag.
	 *
	 * @return override for settlement construction mission creation.
	 */
	public boolean getConstructionOverride() {
		return constructionOverride;
	}

	/**
	 * Sets the FoodProduction override flag.
	 *
	 * @param FoodProduction
	 *            override for FoodProduction.
	 */
	public void setFoodProductionOverride(boolean foodProductionOverride) {
		this.foodProductionOverride = foodProductionOverride;
	}

	/**
	 * Gets the FoodProduction override flag.
	 *
	 * @return override for settlement FoodProduction.
	 */
	public boolean getFoodProductionOverride() {
		return foodProductionOverride;
	}

	/**
	 * Sets the manufacture override flag.
	 *
	 * @param manufactureOverride
	 *            override for manufacture.
	 */
	public void setManufactureOverride(boolean manufactureOverride) {
		this.manufactureOverride = manufactureOverride;
	}

	/**
	 * Gets the manufacture override flag.
	 *
	 * @return override for settlement manufacture.
	 */
	public boolean getManufactureOverride() {
		return manufactureOverride;
	}

	/**
	 * Sets the resource process override flag.
	 *
	 * @param resourceProcessOverride
	 *            override for resource processes.
	 */
	public void setResourceProcessOverride(boolean resourceProcessOverride) {
		this.resourceProcessOverride = resourceProcessOverride;
	}

	/**
	 * Gets the resource process override flag.
	 *
	 * @return override for settlement resource processes.
	 */
	public boolean getResourceProcessOverride() {
		return resourceProcessOverride;
	}

	/**
	 * Gets the settlement's construction manager.
	 *
	 * @return construction manager.
	 */
	public ConstructionManager getConstructionManager() {
		return constructionManager;
	}

	/**
	 * Gets the settlement's achievement credit for a given scientific field.
	 *
	 * @param science
	 *            the scientific field.
	 * @return achievement credit.
	 */
	public double getScientificAchievement(ScienceType science) {
		double result = 0D;

		if (scientificAchievement.containsKey(science))
			result = scientificAchievement.get(science);

		return result;
	}

	/**
	 * Gets the settlement's total scientific achievement credit.
	 *
	 * @return achievement credit.
	 */
	public double getTotalScientificAchievement() {
		double result = 0D;

		Iterator<Double> i = scientificAchievement.values().iterator();
		while (i.hasNext())
			result += i.next();

		return result;
	}

	/**
	 * Add achievement credit to the settlement in a scientific field.
	 * @param achievementCredit  the achievement credit.
	 * @param science the scientific field.
	 */
	public void addScientificAchievement(double achievementCredit, ScienceType science) {
		if (scientificAchievement.containsKey(science))
			achievementCredit += scientificAchievement.get(science);

		scientificAchievement.put(science, achievementCredit);
	}

	/**
	 * Gets the initial population of the settlement.
	 * @return initial population number.
	 */
	public int getInitialPopulation() {
		return initialPopulation;
	}

	/**
	 * Gets the initial number of robots the settlement.
	 * @return initial number of robots.
	 */
	public int getInitialNumOfRobots() {
		return initialNumOfRobots;
	}


	/**
	 * Returns the chain of command
	 * @return chainOfCommand
	 */
	public ChainOfCommand getChainOfCommand() {
		return chainOfCommand;
	}

	/**
	 * Decrements a particular shift type
	 * @param shiftType
	 */
	public void decrementShiftType(ShiftType shiftType) {

		if (shiftType.equals(ShiftType.A)) {
			numA--;
		}

		else if (shiftType.equals(ShiftType.B)) {
			numB--;
		}

		else if (shiftType.equals(ShiftType.X)) {
			numX--;
		}

		else if (shiftType.equals(ShiftType.Y)) {
			numY--;
		}

		else if (shiftType.equals(ShiftType.Z)) {
			numZ--;
		}
		else if (shiftType.equals(ShiftType.ON_CALL)) {
			numOnCall--;
		}
	}


	/**
	 * Increments a particular shift type
	 * @param shiftType
	 */
	public void incrementShiftType(ShiftType shiftType) {

		if (shiftType.equals(ShiftType.A)) {
			numA++;
		}

		else if (shiftType.equals(ShiftType.B)) {
			numB++;
		}

		else if (shiftType.equals(ShiftType.X)) {
			numX++;
		}

		else if (shiftType.equals(ShiftType.Y)) {
			numY++;
		}

		else if (shiftType.equals(ShiftType.Z)) {
			numZ++;
		}
		else if (shiftType.equals(ShiftType.ON_CALL)) {
			numOnCall++;
		}
	}


	/**
	 * Checks if a particular work shift has been saturated
	 * @param ShiftType
	 * @param inclusiveChecking
	 * @return true/false
	 */
	// 2015-12-05 isWorkShiftSaturated
	public boolean isWorkShiftSaturated(ShiftType st, boolean inclusiveChecking) {
		boolean result = false;

		// Reduce the shiftType of interest to find out if it's saturated
		if (inclusiveChecking)
			decrementShiftType(st);

		int pop = getCurrentPopulationNum();
		int quotient = pop / numShift;
		int remainder = pop % numShift;

		switch (numShift) {
			case 1: // (numShift == 1)
				if (st != ShiftType.ON_CALL)
					result = false;
				break;
			case 2: // else if (numShift == 2) {

				switch (remainder) {
					case 0: // if (remainder == 0) {
						if (numA < quotient && numB < quotient) {
							result = true;
							break;
						}
						if (quotient == 1) {
							if (numA < 1) { // allow only 1 person with "A shift"
								if (st == ShiftType.A)
									result = true;
							} else {
								if (st == ShiftType.B)
									result = true;
							}
						}
						else if (quotient == 2) {
							if (numA < 2) { // allow 2 persons with "A shift"
								if (st == ShiftType.A)
									result = true;
							} else {
								if (st == ShiftType.B)
									result = true;
							}
						}
						break;
					case 1: // else { //if (remainder == 1) {
						if (numA < quotient && numB < quotient) {
							result = true;
							break;
						}
						if (quotient == 1) {
							if (numA < 2) { // allow 1 person with "A shift"
								if (st == ShiftType.A)
									result = true;
							} else {
								if (st == ShiftType.B)
									result = true;
							}
						}
						else if (quotient == 2) {
							if (numA < 3) { // allow 2 persons with "A shift"
								if (st == ShiftType.A)
									result = true;
							} else {
								if (st == ShiftType.B)
									result = true;
							}
						}
						break;
					} // end of switch (remainder)
					break;
			case 3: // else if (numShift == 3) {
				switch (remainder) {
					case 0: // if (remainder == 0) {
						if (numX < quotient && numY < quotient && numZ < quotient) {
							result = true;
							break;
						}
						if (numX < quotient + 1) { // allow up to q persons with "X shift"
							if (st == ShiftType.X)
								result = true;
						} else if (numY < quotient + 1) { // allow up to q persons with  "Y shift"
							if (st == ShiftType.Y)
								result = true;
						} else {
							if (st == ShiftType.Z)
								result = true;
						}
						break;
					case 1: // else if (remainder == 1) {
						if (numX < quotient && numY < quotient && numZ < quotient) {
							result = true;
							break;
						}
						if (numX < quotient + 1) { // allow up to q persons with "X shift"
							if (st == ShiftType.X)
								result = true;
						}
						else if (numY < quotient + 2) { // allow up to q + 1 persons  with "Y shift"
							if (st == ShiftType.Y)
								result = true;
						}
						else {
							if (st == ShiftType.Z)
								result = true;
						}
						break;
					case 2: // else {//if (remainder == 2) {
						if (numX < quotient && numY < quotient && numZ < quotient) {
							result = true;
							break;
						}
						if (numX < quotient + 2) { // allow up to q+1 persons with "X										// shift"
							if (st == ShiftType.X)
								result = true;
						}
						else if (numY < quotient + 2) { // allow up to q+1 persons with "Y shift"
							if (st == ShiftType.Y)
								result = true;
						}
						else {
							if (st == ShiftType.Z)
								result = true;
						}
						break;
					} // end of switch for case 3
				break;
			} // end of switch

		// fill back the shiftType of interest
		if (inclusiveChecking)
			incrementShiftType(st);

		return result;
	}

	/**
	 * Finds an empty work shift in a settlement
	 * @param population. If it wasn't known, use -1 to obtain the latest figure
	 * @return shiftype
	 */
	// 2015-11-01 Edited getAEmptyWorkShift
	public ShiftType getAEmptyWorkShift(int pop) {
		if (pop == -1)
			pop = getCurrentPopulationNum();

		int rand = -1;
		ShiftType shiftType = ShiftType.OFF;
		int quotient = pop / numShift;
		int remainder = pop % numShift;

		switch (numShift) {

		case 1: // (numShift == 1)
			shiftType = ShiftType.ON_CALL;
			break;
		case 2: // else if (numShift == 2) {
			switch (remainder) {
			case 0: // if (remainder == 0) {
				if (numA < quotient && numB < quotient) {
					rand = RandomUtil.getRandomInt(1);
					if (rand == 0) {
						shiftType = ShiftType.A;
					} else if (rand == 1) {
						shiftType = ShiftType.B;
					}
					break;
				}
				if (quotient == 1) {
					if (numA < 1) { // allow only 1 person with "A shift"
						shiftType = ShiftType.A;
					} else {
						shiftType = ShiftType.B;
					}
					break;
				}
				else if (quotient == 2) {
					if (numA < 2) { // allow 2 persons with "A shift"
						shiftType = ShiftType.A;
					} else {
						shiftType = ShiftType.B;
					}
					break;
				}
			case 1: // else { //if (remainder == 1) {
				if (numA < quotient && numB < quotient) {
					rand = RandomUtil.getRandomInt(1);
					if (rand == 0) {
						shiftType = ShiftType.A;
					} else if (rand == 1) {
						shiftType = ShiftType.B;
					}
					break;
				}
				if (quotient == 1) {
					if (numA < 2) { // allow 1 person with "A shift"
						shiftType = ShiftType.A;
					} else {
						shiftType = ShiftType.B;
					}
					break;
				}
				else if (quotient == 2) {
					if (numA < 3) { // allow 2 persons with "A shift"
						shiftType = ShiftType.A;
					} else {
						shiftType = ShiftType.B;
					}
					break;
				}
				break;
			} // end of switch (remainder)
			break;
		case 3: // else if (numShift == 3) {
			switch (remainder) {
			case 0: // if (remainder == 0) {
				if (numX < quotient && numY < quotient && numZ < quotient) {
					rand = RandomUtil.getRandomInt(2);
					if (rand == 0) {
						shiftType = ShiftType.X;
					} else if (rand == 1) {
						shiftType = ShiftType.Y;
					} else if (rand == 2) {
						shiftType = ShiftType.Z;
					}
					break;
				}
				if (numX < quotient + 1) { // allow up to q persons with "X shift"
					shiftType = ShiftType.X;
				} else if (numY < quotient + 1) { // allow up to q persons with  "Y shift"
					shiftType = ShiftType.Y;
				} else {
					shiftType = ShiftType.Z;
				}
				break;
			case 1: // else if (remainder == 1) {
				if (numX < quotient && numY < quotient && numZ < quotient) {
					rand = RandomUtil.getRandomInt(2);
					if (rand == 0) {
						shiftType = ShiftType.X;
					} else if (rand == 1) {
						shiftType = ShiftType.Y;
					} else if (rand == 2) {
						shiftType = ShiftType.Z;
					}
					break;
				}
				if (numX < quotient + 1) { // allow up to q persons with "X shift"
					shiftType = ShiftType.X;
				}
				else if (numY < quotient + 2) { // allow up to q + 1 persons  with "Y shift"
					shiftType = ShiftType.Y;
				}
				else {
					shiftType = ShiftType.Z;
				}
				break;
			case 2: // else {//if (remainder == 2) {
				if (numX < quotient && numY < quotient && numZ < quotient) {
					rand = RandomUtil.getRandomInt(2);
					if (rand == 0) {
						shiftType = ShiftType.X;
					} else if (rand == 1) {
						shiftType = ShiftType.Y;
					} else if (rand == 2) {
						shiftType = ShiftType.Z;
					}
					break;
				}
				if (numX < quotient + 2) { // allow up to q+1 persons with "X										// shift"
					shiftType = ShiftType.X;
				}
				else if (numY < quotient + 2) { // allow up to q+1 persons with "Y shift"
					shiftType = ShiftType.Y;
				}
				else {
					shiftType = ShiftType.Z;
				}
				break;
			} // end of switch for case 3
			break;
		} // end of switch

		return shiftType;
	}

	/**
	 * Sets the number of shift of a settlement
	 * @param numShift
	 */
	public void setNumShift(int numShift) {
		this.numShift = numShift;
	}

	/**
	 * Gets the current number of work shifts in a settlement
	 * @return a number, either 2 or 3
	 */
	public int getNumShift() {
		return numShift;
	}

	/*
	 * Restores the previous shift type
	 *
	 * public String reassignShiftType() { String shiftType = "None"; int rand =
	 * -1; int pop = getCurrentPopulationNum();
	 *
	 * if (numShift == 1) { ; // do nothing } else if (numShift == 2) { //
	 * examine numA and numB if (numA > ..pop.)
	 *
	 *
	 * rand = RandomUtil.getRandomInt(1); if (rand != -1) { if (rand == 0)
	 * shiftType = ShiftType.A; else if (rand == 1) shiftType = ShiftType.B;
	 *
	 * } } else if (numShift == 3) { // examine numX , numY and numZ
	 *
	 * rand = RandomUtil.getRandomInt(2); if (rand != -1) { if (rand == 0)
	 * shiftType = ShiftType.X; else if (rand == 1) shiftType = ShiftType.Y;
	 * else if (rand == 2) shiftType = ShiftType.Z;; } } return shiftType; }
	 */

	/*
	 * Increments the number of people in a particular work shift
	 *
	 * @param shiftType
	 */
	public void incrementAShift(ShiftType shiftType) {
		if (shiftType != null) {
			if (shiftType.equals(ShiftType.A))
				numA++;
			else if (shiftType.equals(ShiftType.B))
				numB++;
			else if (shiftType.equals(ShiftType.X))
				numX++;
			else if (shiftType.equals(ShiftType.Y))
				numY++;
			else if (shiftType.equals(ShiftType.Z))
				numZ++;
			else if (shiftType.equals(ShiftType.ON_CALL))
				numOnCall++;
		}
	}

	/*
	 * Decrements the number of people in a particular work shift
	 *
	 * @param shiftType
	 */
	public void decrementAShift(ShiftType shiftType) {
		if (shiftType != null) {
			if (shiftType.equals(ShiftType.A))
				numA--;
			else if (shiftType.equals(ShiftType.B))
				numB--;
			else if (shiftType.equals(ShiftType.X))
				numX--;
			else if (shiftType.equals(ShiftType.Y))
				numY--;
			else if (shiftType.equals(ShiftType.Z))
				numZ--;
			else if (shiftType.equals(ShiftType.ON_CALL))
				numOnCall--;
		}
	}

	/*
	public Map<Integer, Double> getResourceMapCache() {
		return resourceMapCache;
	}

	public void setOneResourceCache(int resourceType, double newAmount) {
		resourceMapCache.put(resourceType, newAmount);
		// System.out.println(" done with setOneResourceCache(). new amount is "
		// + newAmount);
	}
*/

	public Map<Integer, Map<Integer, List<Double>>> getResourceStat() {
		return resourceStat;
	}

	public int getSolCache() {
		return solCache;
	}

	public boolean[] getExposed() {
		return exposed;
	}

	/*
	 * Compute the probability of radiation exposure during EVA/outside walk
	 */
	public void checkRadiationProbability(double time) {
   	    //boolean[] exposed = {false, false, false};
   	    double exposure = 0;
 		// chance is in %
		double chance0 = Math.round(RadiationExposure.BASELINE_CHANCE_PER_100MSOL_DURING_EVA
	    		* time/100D * 100.0)/100.0;
   	    //double rand0 = Math.round(RandomUtil.getRandomDouble(100) * 100.0)/100.0;

	    //if (rand0 < chance0) {
	    if (RandomUtil.lessThanRandPercent(chance0)) {
	    	exposed[0] = true;
	    }
		//System.out.print(chance + " ");

	    // ~ 300 milli Sieverts for a 500-day mission
	    // Solar energetic particles (SEPs) event
		// chance is in %
		double chance1 = Math.round(RadiationExposure.SEP_CHANCE_PER_100MSOL_DURING_EVA
	    		* time/100D * 100.0)/100.0;

	    // rand is in %
	    //double rand1 = Math.round(RandomUtil.getRandomDouble(100) * 100.0)/100.0;

	    if (RandomUtil.lessThanRandPercent(chance1)) {
	    //if (rand1 < chance1) {
	    	exposed[1] = true;
	    	logger.info("An SEP event is detected by the sensor grid on " + getName());
	    }
	    // Galactic cosmic rays (GCRs) event
  		double chance2 = Math.round(RadiationExposure.SEP_CHANCE_PER_100MSOL_DURING_EVA
	    		* time/100D * 100.0)/100.0;

	    // rand is in %
	    //double rand2 = Math.round(RandomUtil.getRandomDouble(100) * 100.0)/100.0;

	    if (RandomUtil.lessThanRandPercent(chance2)) {
	    //if (rand2 < chance2) {
	    	exposed[2] = true;
	    	logger.info("An GCR event is detected by the sensor grid on " + getName());
	    }

	    //return exposed;
	}

	@Override
	public void destroy() {
		super.destroy();

		if (buildingManager != null) {
			buildingManager.destroy();
		}
		buildingManager = null;
		if (buildingConnectorManager != null) {
			buildingConnectorManager.destroy();
		}
		buildingConnectorManager = null;
		if (goodsManager != null) {
			goodsManager.destroy();
		}
		goodsManager = null;
		if (constructionManager != null) {
			constructionManager.destroy();
		}
		constructionManager = null;
		if (powerGrid != null) {
			powerGrid.destroy();
		}
		powerGrid = null;

		if (thermalSystem != null) {
			thermalSystem.destroy();
		}
		thermalSystem = null;

		template = null;
		if (scientificAchievement != null) {
			scientificAchievement.clear();
		}
		scientificAchievement = null;
	}

}