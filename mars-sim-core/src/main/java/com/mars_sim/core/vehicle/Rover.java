/*
 * Mars Simulation Project
 * Rover.java
 * @date 2024-07-12
 * @author Scott Davis
 */

package com.mars_sim.core.vehicle;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.LifeSupportInterface;
import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.air.AirComposition;
import com.mars_sim.core.building.function.SystemType;
import com.mars_sim.core.data.Range;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.RoverMission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Airlock;
import com.mars_sim.core.structure.Lab;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.SettlementConfig;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;

/**
 * The Rover class represents the rover type of ground vehicle. It contains
 * information about the rover.
 */
public class Rover extends GroundVehicle implements Crewable, 
	LifeSupportInterface, Airlockable, Medical, Towing {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(Rover.class.getName());

	/** The fuel range modifier. */
	private static final double FUEL_RANGE_FACTOR = 0.95;
	/** The reference small amount of resource. */
	private static final double SMALL_AMOUNT = 0.1;
	/** The amount of work time to perform maintenance (millisols) */
	private static final double MAINTENANCE_WORK_TIME = 125D;

	// Note: 34 kPa (5 psi) is chosen for the composition of oxygen inside a settlement at 58.8%.
	/** Rate of change of temperature in degree celsius. */
	private static final double RATE_OF_CHANGE_OF_C_PER_MILLISOL = 0.0005D;
	/** Rate of change of air pressure (kPa). */
	private static final double RATE_OF_CHANGE_OF_KPA_PER_MILLISOL = 0.0005D;
	/** The temperature flow [deg C per millisols] when connected to a settlement */
	private static final double TEMPERATURE_FLOW_PER_MILLISOL = 0.01D;
	/** The air pressure flow [kPa per millisols] when connected to a settlement */
	private static final double AIR_PRESSURE_FLOW_PER_MILLISOL = 0.01D;

	/** Normal air pressure (kPa). */
	private static final double NORMAL_AIR_PRESSURE = 17; 
	/** Normal temperature (celsius). */
	private static final double NORMAL_TEMP = 22.5D;
	
	/** The ratio of the amount of oxygen inhaled to the amount of carbon dioxide expelled. */
	private static double gasRatio;
	/** The minimum required O2 partial pressure. At 11.94 kPa (1.732 psi) */
	private static double minO2Pressure;

	private static Range tempRange;
	
	// Data members
	/** The rover's capacity for crew members. */
	private int crewCapacity = 0;
	/** The rover's capacity for robot crew members. */
	private int robotCrewCapacity = 0;

	/** The capacity of O2 in this rover (kg)  */
	private double oxygenCapacity;
	/** The rover's internal air pressure. */
	private double airPressure = 0; 
	/** The rover's internal temperature. */
	private double temperature = 0; //NORMAL_TE
	/** The rover's total crew internal volume. */
	private double cabinAirVolume;
	/** The full O2 partial pressure if at full tank. */
	private double fullO2PartialPressure;
	/** The nominal mass of O2 required to maintain the nominal partial pressure of 20.7 kPa (3.003 psi)  */
	private double massO2NominalLimit;
	/** The minimum mass of O2 required to maintain right above the safety limit of 11.94 kPa (1.732 psi)  */
	private double massO2MinimumLimit;

	/** The rover's lab activity spots. */
	private List<LocalPosition> labActivitySpots;
	/** The rover's sick bay activity spots. */
	private List<LocalPosition> sickBayActivitySpots;

	/** The rover's occupants. */
	private Set<Person> occupants;
	/** The rover's robot occupants. */
	private Set<Robot> robotOccupants;

	/** The rover's airlock. */
	private Airlock airlock;
	/** The rover's lab. */
	private Lab lab;
	/** The rover's sick bay. */
	private SickBay sickbay;
	/** The vehicle the rover is currently towing. */
	private Vehicle towedVehicle;
	/** The light utility vehicle currently docked at the rover. */
	private LightUtilityVehicle luv;

	/**
	 * Setup the various internal flags
	 * @param simulationConfig
	 */
	public static void initializeInstances(SimulationConfig simulationConfig) {
		double o2Consumed = simulationConfig.getPersonConfig().getHighO2ConsumptionRate();
		double cO2Expelled = simulationConfig.getPersonConfig().getCO2ExpelledRate();
		gasRatio = cO2Expelled/o2Consumed;
		
		minO2Pressure = simulationConfig.getPersonConfig().getMinSuitO2Pressure();
		tempRange = simulationConfig.getSettlementConfiguration().getLifeSupportRequirements(SettlementConfig.TEMPERATURE);
	
		Vehicle.initializeInstances(simulationConfig);
	}
	
	/**
	 * Constructs a Rover object at a given settlement.
	 *
	 * @param name        the name of the rover
	 * @param spec the configuration type of the vehicle.
	 * @param settlement  the settlement the rover is parked at
	 */
	public Rover(String name, VehicleSpec spec, Settlement settlement) {
		// Use GroundVehicle constructor
		super(name, spec, settlement, MAINTENANCE_WORK_TIME);
		
		occupants = new UnitSet<>();
		robotOccupants = new UnitSet<>();

		// Set crew capacity
		crewCapacity = spec.getCrewSize();
		robotCrewCapacity = crewCapacity;

		// Gets the estimated cabin compartment air volume.
		cabinAirVolume =  .8 * spec.getLength() * spec.getWidth() * 2D;
		oxygenCapacity = spec.getCargoCapacity(ResourceUtil.OXYGEN_ID);

		fullO2PartialPressure = Math.round(AirComposition.getOxygenPressure(oxygenCapacity, cabinAirVolume)*1_000.0)/1_000.0;
		massO2MinimumLimit = Math.round(minO2Pressure / fullO2PartialPressure * oxygenCapacity*10_000.0)/10_000.0;
		massO2NominalLimit =Math.round( NORMAL_AIR_PRESSURE / minO2Pressure * massO2MinimumLimit*10_000.0)/10_000.0;
		
		// Construct sick bay.
		if (spec.hasSickbay()) {
			sickbay = new SickBay(this, spec.getSickbayTechLevel(),
					spec.getSickbayBeds());

			// Initialize sick bay activity spots.
			sickBayActivitySpots = spec.getSickBayActivitySpots();
		}

		// Construct lab.
		if (spec.hasLab()) {
			lab = new MobileLaboratory(1, spec.getLabTechLevel(),
					spec.getLabTechSpecialties());

			// Initialize lab activity spots.
			labActivitySpots = spec.getLabActivitySpots();
		}

		// Create the rover's airlock.
		airlock = new VehicleAirlock(this, 2, spec.getAirlockLoc(), spec.getAirlockInteriorLoc(),
										 spec.getAirlockExteriorLoc());
	}

	/**
	 * Sets the scope string.
	 */
	@Override
	protected void setupScopeString() {
		super.setupScopeString();
		
		malfunctionManager.addScopeString(SystemType.ROVER.getName());
	}

	/**
	 * Sets the vehicle this rover is currently towing.
	 *
	 * @param towedVehicle the vehicle being towed by this rover.
	 */
	public void setTowedVehicle(Vehicle towedVehicle) {
		if (this == towedVehicle)
			throw new IllegalArgumentException("Rover cannot tow itself.");

		if (towedVehicle != null) {
			// if towedVehicle is not null, it means this rover has just hooked up for towing the towedVehicle
			addSecondaryStatus(StatusType.TOWING);
		}
		else {
			removeSecondaryStatus(StatusType.TOWING);
		}

		this.towedVehicle = towedVehicle;
	}

	/**
	 * Gets the vehicle this rover is currently towing.
	 *
	 * @return towed vehicle.
	 */
	public Vehicle getTowedVehicle() {
		return towedVehicle;
	}

	/**
	 * Is this rover towing another vehicle ?
	 *
	 * @return true or false
	 */
	public boolean isTowingAVehicle() {
        return towedVehicle != null;
	}

	/**
	 * Gets the number of crewmembers the vehicle can carry.
	 *
	 * @return capacity
	 */
	public int getCrewCapacity() {
		return crewCapacity;
	}

	/**
	 * Gets the number of robot crewmembers the vehicle can carry.
	 *
	 * @return capacity
	 */
	public int getRobotCrewCapacity() {
		return robotCrewCapacity;
	}

	/**
	 * Gets the current number of crewmembers.
	 *
	 * @return number of crewmembers
	 */
	public int getCrewNum() {
		if (!getCrew().isEmpty())
			return occupants.size();
		return 0;
	}

	/**
	 * Gets the current number of crewmembers.
	 *
	 * @return number of crewmembers
	 */
	public int getRobotCrewNum() {
		if (!getRobotCrew().isEmpty())
			return robotOccupants.size();
		return 0;
	}

	/**
	 * Gets a set of the robot crewmembers.
	 *
	 * @return robot crewmembers
	 */
	public Set<Person> getCrew() {
		if (occupants == null || occupants.isEmpty())
			return new UnitSet<>();
		return occupants;
	}

	/**
	 * Gets a set of the robot crewmembers.
	 *
	 * @return robot crewmembers
	 */
	public Set<Robot> getRobotCrew() {
		if (robotOccupants == null || robotOccupants.isEmpty())
			return new UnitSet<>();
		return robotOccupants;
	}

	/**
	 * Does the rover have no occupants ?
	 */
	public boolean hasNoCrew() {
		return robotOccupants.isEmpty() && occupants.isEmpty();
	}
	
	/**
	 * Checks if person is a crewmember.
	 *
	 * @param person the person to check
	 * @return true if person is a crewmember
	 */
	public boolean isCrewmember(Person person) {
		return getCrew().contains(person);
	}
	
	/**
	 * Checks if person is in the airlock.
	 *
	 * @param person the person to check
	 * @return
	 */
	public boolean isInAirlock(Person person) {
		return getAirlock().getOccupants123().contains(person.getIdentifier());
	}

	/**
	 * Checks if robot is a crewmember.
	 *
	 * @param robot the robot to check
	 * @return true if robot is a crewmember
	 */
	public boolean isRobotCrewmember(Robot robot) {
		return getRobotCrew().contains(robot);
	}

	/**
	 * Adds a person as crewmember.
	 *
	 * @param person
	 * @param true if the person can be added
	 */
	public boolean addPerson(Person person) {
		if (isCrewmember(person)) {
			return true;
		}
		if (occupants.add(person)) {
			person.setLocationStateType(LocationStateType.INSIDE_VEHICLE);
			// Fire the unit event type
			fireUnitUpdate(UnitEventType.INVENTORY_STORING_UNIT_EVENT, person);
			return true;
		}
		return false;
	}

	/**
	 * Removes a person as crewmember.
	 *
	 * @param person
	 * @param true if the person can be removed
	 */
	public boolean removePerson(Person person) {
		if (!isCrewmember(person)) {
			return true;
		}
		if (occupants.remove(person)) {
			fireUnitUpdate(UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT, person);
			return true;
		}
		return false;
	}

	/**
	 * Adds a robot as crewmember.
	 *
	 * @param robot
	 * @param true if the robot can be added
	 */
	public boolean addRobot(Robot robot) {
		if (isRobotCrewmember(robot)) {
			return true;
		}
		if (robotOccupants.add(robot)) {
			robot.setLocationStateType(LocationStateType.INSIDE_VEHICLE);
			fireUnitUpdate(UnitEventType.INVENTORY_STORING_UNIT_EVENT, robot);
			return true;
		}
		return false;
	}

	/**
	 * Removes a robot as crewmember.
	 *
	 * @param robot
	 * @param true if the robot can be removed
	 */
	public boolean removeRobot(Robot robot) {
		if (!isRobotCrewmember(robot)) {
			return true;
		}
		if (robotOccupants.remove(robot)) {
			fireUnitUpdate(UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT, robot);
			return robotOccupants.remove(robot);
		}
		return false;
	}

	/**
	 * Returns true if life support is working properly and is not out of oxygen or
	 * water.
	 *
	 * @return true if life support is OK
	 * @throws Exception if error checking life support.
	 */
	@Override
	public boolean lifeSupportCheck() {
		// Note: need to draw the the hose connecting between the vehicle and the settlement to supply resources
		if (isPluggedIn()) {
			if (haveStatusType(StatusType.TOWED) && !isInSettlement()) {

				double o2 = getTowingVehicle().getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
				if (o2 < SMALL_AMOUNT) {
					logger.log(this, Level.WARNING, 60_000,
						"No more oxygen.");
					return false;
				}

				else if (o2 <= massO2MinimumLimit) {
					logger.log(this, Level.WARNING, 60_000,
							"Remaining oxygen was below the safety threshold ("
									+ massO2MinimumLimit + " kg) ");
					return false;
				}

				if (getTowingVehicle().getSpecificAmountResourceStored(ResourceUtil.WATER_ID) <= 0D) {
					logger.log(this, Level.WARNING, 60_000,
							"Ran out of water.");
					return false;
				}
			}

			else if (getSettlement() != null)  {

				double o2 = getSettlement().getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
				if (o2 < SMALL_AMOUNT) {
					logger.log(this, Level.WARNING, 60_000,
						"No more oxygen.");
					return false;
				}

				else if (o2 <= massO2MinimumLimit) {
					logger.log(this, Level.WARNING, 60_000,
							"Remaining oxygen was below the safety threshold ("
									+ massO2MinimumLimit + " kg) ");
					return false;
				}

				if (getSettlement().getSpecificAmountResourceStored(ResourceUtil.WATER_ID) <= 0D) {
					logger.log(this, Level.WARNING, 60_000,
							"Ran out of water.");
					return false;
				}
			}

		}
		else {

			double o2 = getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
			if (o2 < SMALL_AMOUNT) {
				logger.log(this, Level.WARNING, 60_000,
					"No more oxygen.");
				return false;
			}

			else if (o2 <= massO2MinimumLimit) {
				logger.log(this, Level.WARNING, 60_000,
						"Remaining oxygen was below the safety threshold ("
								+ massO2MinimumLimit + " kg) ");
				return false;
			}

			if (getSpecificAmountResourceStored(ResourceUtil.WATER_ID) <= 0D) {
				logger.log(this, Level.WARNING, 60_000,
						"Ran out of water.");
				return false;
			}
		}

		double p = getAirPressure();
		if (p > PhysicalCondition.MAXIMUM_AIR_PRESSURE || p <= minO2Pressure) {
			logger.log(this, Level.WARNING, 60_000,
					"Out-of-range O2 pressure at " + Math.round(p * 100.0D) / 100.0D
					+ " kPa detected.");
			return false;
		}

		double t = getTemperature();
		if (t < tempRange.min() - Settlement.SAFE_TEMPERATURE_RANGE
				|| t > tempRange.max() + Settlement.SAFE_TEMPERATURE_RANGE) {
			logger.log(this, Level.WARNING, 10_000,
					"Out-of-range overall temperature at " + Math.round(t * 100.0D) / 100.0D
						+ " " + Msg.getString("temperature.sign.degreeCelsius") + " detected.");
			return false;
		}

		return true;
	}

	/**
	 * Gets the number of people the life support can provide for.
	 *
	 * @return the capacity of the life support system
	 */
	@Override
	public int getLifeSupportCapacity() {
		return crewCapacity;
	}

	/**
	 * Is the rover connected to the settlement through hoses ?
	 *
	 * @return true if yes
	 */
	public boolean isPluggedIn() {
		// Q: Should we consider a rover "plugged in" as soon as it parks in the settlement vicinity ?
		// Q: What distance is it supposed to be away from the host building ?
		if (isInSettlement())
			return true;

		if (getPrimaryStatus() == StatusType.GARAGED)
			return true;

        return haveStatusType(StatusType.TOWED);
    }

	/**
	 * Gets oxygen from system.
	 *
	 * @param oxygenTaken the amount of oxygen requested from system (kg)
	 * @return the amount of oxygen actually received from system (kg)
	 * @throws Exception if error providing oxygen.
	 */
	public double provideOxygen(double oxygenTaken) {
		// Future: Adopt calculateGasExchange() in CompositionOfAir 
		// for retrieving O2 here
		double lacking = 0;

		Vehicle v = null;

		// NOTE: need to draw the the hose connecting between the vehicle and the settlement to supply resources
		if (isPluggedIn()) {
			if (haveStatusType(StatusType.TOWED) && !isInSettlement()) {
				v = getTowingVehicle();

				lacking = v.retrieveAmountResource(ResourceUtil.OXYGEN_ID, oxygenTaken);
				v.storeAmountResource(ResourceUtil.CO2_ID, gasRatio * (oxygenTaken - lacking));
			}

			else if (isInSettlement()) {
				lacking = getSettlement().retrieveAmountResource(ResourceUtil.OXYGEN_ID, oxygenTaken);
				getSettlement().storeAmountResource(ResourceUtil.CO2_ID, gasRatio * (oxygenTaken - lacking));
			}
		}

		else {

			lacking = retrieveAmountResource(ResourceUtil.OXYGEN_ID, oxygenTaken);
			storeAmountResource(ResourceUtil.CO2_ID, gasRatio * (oxygenTaken - lacking));
		}

		return oxygenTaken - lacking;
	}

	/**
	 * Gets water from system.
	 *
	 * @param waterTaken the amount of water requested from system (kg)
	 * @return the amount of water actually received from system (kg)
	 * @throws Exception if error providing water.
	 */
	public double provideWater(double waterTaken) {
		double lacking = 0;

		Vehicle v = null;

		// Note: need to draw the the hose connecting between the vehicle and the settlement to supply resources
		if (isPluggedIn()) {
			if (haveStatusType(StatusType.TOWED) && !isInSettlement()) {
				v = getTowingVehicle();

				lacking = v.retrieveAmountResource(ResourceUtil.WATER_ID, waterTaken);
			}

			else if (isInSettlement()) {
				lacking = getSettlement().retrieveAmountResource(ResourceUtil.WATER_ID, waterTaken);
			}
		}
		else {

			lacking = retrieveAmountResource(ResourceUtil.WATER_ID, waterTaken);
		}

		return waterTaken - lacking;
	}

	/**
	 * Gets the air pressure of the life support system.
	 *
	 * @return air pressure (Pa)
	 */
	public double getAirPressure() {
		// Based on some pre-calculation,
		// To supply a partial oxygen pressure of 20.7 kPa, one needs at least 0.3107 kg O2

		// With the minimum required O2 partial pressure of 11.94 kPa (1.732 psi), the minimum mass of O2 is 0.1792 kg

		// Note : our target o2 partial pressure is now 17 kPa (not 20.7 kPa)
		// To supply 17 kPa O2, need 0.2552 kg O2

		double oxygenLeft = 0;

		if (!isInSettlement()) {
			if (getTowingVehicle() != null) {
				oxygenLeft = getTowingVehicle().getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
			}
			else
				oxygenLeft = getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
		}
		else {
			oxygenLeft = getSettlement().getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
		}

		if (oxygenLeft < SMALL_AMOUNT) {
			return 0;
		}

		else if (oxygenLeft < massO2NominalLimit) {
			// Assuming that we can maintain a constant oxygen partial pressure unless it falls below massO2NominalLimit
			double pp = AirComposition.getOxygenPressure(oxygenLeft, cabinAirVolume);
			logger.log(this, Level.WARNING, 60_000,
					Math.round(oxygenLeft*100.0)/100.0
						+ " kg O2 left at partial pressure of " + Math.round(pp*100.0)/100.0 + " kPa.");
			return pp;
		}

		//	Note: the outside ambient air pressure is weather.getAirPressure(getCoordinates())
		return NORMAL_AIR_PRESSURE;
	}

	/**
	 * Gets the temperature of the life support system.
	 *
	 * @return temperature (degrees C)
	 */
	public double getTemperature() {
		return temperature;
	}

	/**
	 * Plugs in the rover and adjust the temperature
	 *
	 * @param time
	 */
	public void plugInTemperature(double time) {
		// TODO: need to draw the the hose connecting between the vehicle and the settlement to supply resources
		if (isPluggedIn()) {
			if (temperature > NORMAL_TEMP * 1.15 || temperature < NORMAL_TEMP * 0.85) {
				// Internal air pumps of a rover maintains the air pressure
				// TODO: need to model the power usage

				double p = 0;
				if (getPrimaryStatus() == StatusType.GARAGED)
					p = getGarage().getCurrentTemperature();
				else
					p = getSettlement().getTemperature();
				double delta = temperature - p;
				if (delta > 5)
					delta = 5;
				else if (delta < -5)
					delta = -5;

				double result = temperature - delta * TEMPERATURE_FLOW_PER_MILLISOL * time;

				temperature = result;
			}
		}
	}


	/**
	 * Unplugs the rover and adjust the temperature
	 *
	 * @param time
	 */
	public void plugOffTemperature(double time) {
		if (temperature >= 0) {
			// if no one is occupying the rover, can power it off

			// TODO : will need to the internal air composition/pressure of a vehicle
			temperature -= RATE_OF_CHANGE_OF_C_PER_MILLISOL * time;
			if (temperature < 0)
				// but will use power to maintain the temperature at the minimum of zero deg C
				temperature = 0;
		}
	}

	/**
	 * Adjusts the air pressure of the rover.
	 *
	 * @param time
	 */
	public void plugInAirPressure(double time) {
		// TODO: need to draw the the hose connecting between the vehicle and the settlement to supply resources
		if (isPluggedIn()) {
			if (airPressure > NORMAL_AIR_PRESSURE * 1.15 || airPressure < NORMAL_AIR_PRESSURE * 0.85) {
				// Internal heat pump of a rover maintains the air pressure
				// TODO: need to model the power usage

				double p = 0;
				if (getPrimaryStatus() == StatusType.GARAGED)
					p = getGarage().getCurrentAirPressure();
				else
					p = getSettlement().getAirPressure();

				double delta = airPressure - p;
				if (delta > 5)
					delta = 5;
				else if (delta < -5)
					delta = -5;

				double result = airPressure - delta * AIR_PRESSURE_FLOW_PER_MILLISOL * time;
				airPressure = result;
			}
		}
	}

	public void plugOffAirPressure(double time) {
		if (airPressure >= 0) {
			// if no one is occupying the rover, can power it off
			double rate = RATE_OF_CHANGE_OF_KPA_PER_MILLISOL * time;

			// TODO : will need to the internal air composition/pressure of a vehicle
			airPressure -= rate;

			if (airPressure < 0)
				airPressure = 0;
		}
	}

	/**
	 * Gets the rover's airlock.
	 *
	 * @return rover's airlock
	 */
	public Airlock getAirlock() {
		return airlock;
	}

	
	
	/**
	 * Perform time-related processes
	 *
	 * @param pulse the amount of clock pulse passing (in millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		if (!super.timePassing(pulse)) {
			return false;
		}

		airlock.timePassing(pulse);

		boolean onAMission = isOutsideOnMarsMission();
		if (onAMission || isReservedForMission()) {

			Mission mission = getMission();
			if (mission != null) {
				// This code feel wrong
				Collection<Worker> members = mission.getMembers();
				for (Worker m: members) {
					if (m.getMission() == null) {
						// Defensively set the mission in the case that the delivery bot is registered as a mission member
						// but its mission is null
						// Question: why would the mission be null for this member in the first place after loading from a saved sim
						logger.info(this, m.getName() + " reregistered for " + mission + ".");
						m.setMission(mission);
					}
				}

				if (isInSettlement()) {
					plugInTemperature(pulse.getElapsed());
					plugInAirPressure(pulse.getElapsed());
				}
			}
		}

		else if (crewCapacity <= 0) {
			plugOffTemperature(pulse.getElapsed());
			plugOffAirPressure(pulse.getElapsed());
		}

		return true;
	}

	/**
	 * Gets a collection of people affected by this entity.
	 *
	 * @return people collection
	 */
	@Override
	public Collection<Person> getAffectedPeople() {
		Collection<Person> people = super.getAffectedPeople();

		Collection<Person> crew = getCrew();
		Iterator<Person> i = crew.iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (!people.contains(person))
				people.add(person);
		}

		return people;
	}

	/**
	 * Gets a collection of robots affected by this entity.
	 *
	 * @return robots collection
	 */
	@Override
	public Collection<Robot> getAffectedRobots() {
		Collection<Robot> robots = super.getAffectedRobots();

		Collection<Robot> crew = getRobotCrew();
		Iterator<Robot> i = crew.iterator();
		while (i.hasNext()) {
			Robot robot = i.next();
			if (!robots.contains(robot))
				robots.add(robot);
		}

		return robots;
	}

	/**
	 * Checks if the rover has a laboratory.
	 *
	 * @return true if lab.
	 */
	public boolean hasLab() {
		return lab != null;
	}

	/**
	 * Gets the rover's laboratory
	 *
	 * @return lab
	 */
	public Lab getLab() {
		return lab;
	}

	/**
	 * Gets a list of lab activity spots.
	 *
	 * @return list of activity spots as Point2D objects.
	 */
	public List<LocalPosition> getLabActivitySpots() {
		return labActivitySpots;
	}

	/**
	 * Checks if the rover has a sick bay.
	 *
	 * @return true if sick bay
	 */
	public boolean hasSickBay() {
		return sickbay != null;
	}

	/**
	 * Gets the rover's sick bay.
	 *
	 * @return sick bay
	 */
	public SickBay getSickBay() {
		return sickbay;
	}

	/**
	 * Gets a list of sick bay activity spots.
	 *
	 * @return list of activity spots as Point2D objects.
	 */
	public List<LocalPosition> getSickBayActivitySpots() {
		return sickBayActivitySpots;
	}

	/**
	 * Sets unit's location coordinates
	 *
	 * @param newLocation the new location of the unit
	 */
	@Override
	public void setCoordinates(Coordinates newLocation) {
		super.setCoordinates(newLocation);

		// Set towed vehicle (if any) to new location.
		if (towedVehicle != null)
			towedVehicle.setCoordinates(newLocation);
	}

	/**
	 * Gets the range of the vehicle.
	 *
	 * @return the range of the vehicle (in km)
	 * @throws Exception if error getting range.
	 */
	@Override
	public double getRange() {
		// Question: does it account for the return trip ?
		
		// Note: multiply by 0.95 would account for the extra distance travelled in between sites
		double fuelRange = super.getEstimatedRange() * FUEL_RANGE_FACTOR;

		// Battery also contributes to the range
		double cap = super.getBatteryCapacity();
		double percent = super.getBatteryPercent();
		double estFC = super.getEstimatedFuelConsumption();
		double batteryRange = cap * percent / 100 / estFC * 1000;
		
		// Estimate the distance traveled per sol
		double distancePerSol = getEstimatedTravelDistancePerSol();

		// Gets the life support resource margin
		double margin = getLifeSupportRangeErrorMargin();

		// Check food capacity as range limit.
		PersonConfig personConfig = SimulationConfig.instance().getPersonConfig();
		double foodConsumptionRate = personConfig.getFoodConsumptionRate();
		double foodCapacity = getSpecificCapacity(ResourceUtil.FOOD_ID);
		double foodSols = foodCapacity / (foodConsumptionRate * crewCapacity);
		double foodRange = distancePerSol * foodSols / margin;

		// Check water capacity as range limit.
		double waterConsumptionRate = personConfig.getWaterConsumptionRate();
		double waterCapacity = getSpecificCapacity(ResourceUtil.WATER_ID);
		double waterSols = waterCapacity / (waterConsumptionRate * crewCapacity);
		double waterRange = distancePerSol * waterSols / margin;

		// Check oxygen capacity as range limit.
		double oxygenConsumptionRate = personConfig.getNominalO2ConsumptionRate();
		double oxygenCapacity = getSpecificCapacity(ResourceUtil.OXYGEN_ID);
		double oxygenSols = oxygenCapacity / (oxygenConsumptionRate * crewCapacity);
		double oxygenRange = distancePerSol * oxygenSols / margin;

		return Math.min(oxygenRange, Math.min(foodRange, Math.min(waterRange, fuelRange + batteryRange)));
	}

	@Override
	public void setParkedLocation(LocalPosition position, double facing) {
		super.setParkedLocation(position, facing);

		// Update towed vehicle locations.
		updatedTowedVehicleSettlementLocation();
	}

	/**
	 * Updates the settlement location of any towed vehicles.
	 */
	private void updatedTowedVehicleSettlementLocation() {

		Vehicle towedVehicle = getTowedVehicle();
		if (towedVehicle != null) {
			if (towedVehicle instanceof Rover) {
				// Towed rovers should be located behind this rover with same facing.
				double distance = (getLength() + towedVehicle.getLength()) / 2D;
				LocalPosition towingPosition = new LocalPosition(0D, - distance);
				LocalPosition towedLoc = LocalAreaUtil.convert2SettlementPos(towingPosition, this);
				towedVehicle.setParkedLocation(towedLoc, getFacing());
			} else if (towedVehicle instanceof LightUtilityVehicle) {
				// Towed light utility vehicles should be attached to back of the rover
				// sideways and facing to the right.
				double distance = (getLength() + towedVehicle.getWidth()) / 2D;
				LocalPosition towingPosition = new LocalPosition(0D, - distance);
				LocalPosition towedLoc = LocalAreaUtil.convert2SettlementPos(towingPosition, this);
				towedVehicle.setParkedLocation(towedLoc, getFacing() + 90D);
			}
		}
	}

	/**
	 * Gets the time limit of the trip based on life support capacity.
	 *
	 * @param useBuffer use time buffer in estimation if true.
	 * @return time (millisols) limit.
	 */
	public double getTotalTripTimeLimit(boolean useBuffer) {
		return RoverMission.getTotalTripTimeLimit(this, getCrewCapacity(), useBuffer);
	}
	
	/**
	 * Gets the time limit of the trip based on life support capacity.
	 * Called by ExplorationSitePanel.
	 * 
	 * @param number of members
	 * @param useBuffer use time buffer in estimation if true.
	 * @return time (millisols) limit.
	 */
	public double getTotalTripTimeLimit(int member, boolean useBuffer) {
		return RoverMission.getTotalTripTimeLimit(this, member, useBuffer);
	}

	public boolean setLUV(LightUtilityVehicle luv) {
		if (!hasLUV()) {
			this.luv = luv;
			return true;
		}
		return false;
	}

	public LightUtilityVehicle getLUV() {
		return luv;
	}

	public boolean hasLUV() {
		return luv != null;
	}

	/**
	 * Does this rover have a set of clothing ?
	 */
	public boolean hasGarment() {
		return getItemResourceStored(ItemResourceUtil.GARMENT_ID) > 0;
	}

	@Override
	public void destroy() {
		super.destroy();

		towedVehicle = null;

		labActivitySpots = null;
		sickBayActivitySpots = null;

		if (airlock != null)
			airlock.destroy();
		airlock = null;

		if (lab != null)
			lab.destroy();
		lab = null;

		if (sickbay != null)
			sickbay.destroy();
		sickbay = null;
	}

}
