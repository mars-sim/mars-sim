/**
 * Mars Simulation Project
 * Rover.java
 * @version 3.1.0 2017-09-07
 * @author Scott Davis
 */

package org.mars_sim.msp.core.vehicle;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.CompositionOfAir;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The Rover class represents the rover type of ground vehicle. It contains
 * information about the rover.
 */
public class Rover extends GroundVehicle implements Crewable, LifeSupportInterface, Airlockable, Medical, Towing {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(Rover.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());

	/** The fuel range modifier. */
	public static final double FUEL_RANGE_FACTOR = 0.95;
	/** The mission range modifier. */  
	public static final double MISSION_RANGE_FACTOR = 1.9;
	/** The reference small amount of resource. */
	public static final double SMALL_AMOUNT = 0.1;
	/** The amount of work time to perform maintenance (millisols) */
	public static final double MAINTENANCE_WORK_TIME = 500D;

	// Note: 34 kPa (5 psi) is chosen for the composition of oxygen inside a settlement at 58.8%.
	/** Rate of change of temperature in degree celsius. */
	private static final double RATE_OF_CHANGE_OF_C_PER_MILLISOL = 0.0005D; 
	/** Rate of change of air pressure (kPa). */
	private static final double RATE_OF_CHANGE_OF_kPa_PER_MILLISOL = 0.0005D; 
	/** The factitious temperature flow [deg C per millisols] when connected to a settlement */
	private static final double TEMPERATURE_FLOW_PER_MILLISOL = 0.01D; 
	/** The factitious air pressure flow [kPa per millisols] when connected to a settlement */
	private static final double AIR_PRESSURE_FLOW_PER_MILLISOL = 0.01D; 
	
	/** Normal air pressure (kPa). */
	private static final double NORMAL_AIR_PRESSURE = 17; //20.7; //34.7D; 
	/** Normal temperature (celsius). */
	private static final double NORMAL_TEMP = 22.5D;
	
	// Data members
	/** The rover's capacity for crew members. */
	private int crewCapacity = 0;
	/** The rover's capacity for robot crew members. */
	private int robotCrewCapacity = 0;
	
	/** The minimum required O2 partial pressure. At 11.94 kPa (1.732 psi)  */
	private double min_o2_pressure;
	/** The full O2 partial pressure if at full tank. */
	private double fullO2PartialPressure;
	/** The nominal mass of O2 required to maintain the nominal partial pressure of 20.7 kPa (3.003 psi)  */
	private double massO2NominalLimit;
	/** The minimum mass of O2 required to maintain right above the safety limit of 11.94 kPa (1.732 psi)  */
	private double massO2MinimumLimit;
	/** The capacity of O2 in this rover (kg)  */
	private double oxygenCapacity;
	/** The rover's internal air pressure. */
	private double airPressure = 0; //NORMAL_AIR_PRESSURE;
	/** The rover's internal temperature. */
	private double temperature = 0; //NORMAL_TEMP;
	/** The rover's cargo capacity */
	private double cargoCapacity = 0;
	/** The rover's total crew internal volume. */
	private double cabinAirVolume;

	/** The rover's lab activity spots. */
	private List<Point2D> labActivitySpots;
	/** The rover's sick bay activity spots. */
	private List<Point2D> sickBayActivitySpots;
	
	/** The rover's airlock. */
	private Airlock airlock;
	/** The rover's lab. */
	private Lab lab;
	/** The rover's sick bay. */
	private SickBay sickbay;
	/** The vehicle the rover is currently towing. */
	private Vehicle towedVehicle;

	/**
	 * Constructs a Rover object at a given settlement
	 * 
	 * @param name        the name of the rover
	 * @param type the configuration type of the vehicle.
	 * @param settlement  the settlement the rover is parked at
	 */
	public Rover(String name, String type, Settlement settlement) {
		// Use GroundVehicle constructor
		super(name, type, settlement, MAINTENANCE_WORK_TIME);

		// Add scope to malfunction manager.

		// if (config.hasLab(description))
		// malfunctionManager.addScopeString("Laboratory");
		// if (config.hasSickbay(description))
		// malfunctionManager.addScopeString("Sickbay");

		// Set crew capacity
		crewCapacity = vehicleConfig.getCrewSize(type);
		robotCrewCapacity = crewCapacity;
		// Get inventory object
		Inventory inv = getInventory();

		// Set total cargo capacity
		cargoCapacity = vehicleConfig.getTotalCapacity(type);
		// Set inventory total mass capacity.
		inv.addGeneralCapacity(cargoCapacity);
		
		// Gets the estimated cabin compartment air volume.
		cabinAirVolume = vehicleConfig.getEstimatedAirVolume(type);

		oxygenCapacity = vehicleConfig.getCargoCapacity(type, LifeSupportInterface.OXYGEN);
		min_o2_pressure = personConfig.getMinSuitO2Pressure();
		fullO2PartialPressure = Math.round(CompositionOfAir.KPA_PER_ATM * oxygenCapacity / CompositionOfAir.O2_MOLAR_MASS 
				* CompositionOfAir.R_GAS_CONSTANT / cabinAirVolume*1_000.0)/1_000.0;
		massO2MinimumLimit = Math.round(min_o2_pressure / fullO2PartialPressure * oxygenCapacity*10_000.0)/10_000.0;
		massO2NominalLimit =Math.round( NORMAL_AIR_PRESSURE / min_o2_pressure * massO2MinimumLimit*10_000.0)/10_000.0;
		 
//		logger.config(type + " : full tank O2 partial pressure is " + fullO2PartialPressure + " kPa");
//		logger.config(type + " : minimum mass limit of O2 (above the safety limit) is " + massO2MinimumLimit  + " kg");
//		logger.config(type + " : nomimal mass limit of O2 is " + massO2NominalLimit  + " kg");
		
		// Set inventory resource capacities.
		inv.addAmountResourceTypeCapacity(ResourceUtil.methaneID, vehicleConfig.getCargoCapacity(type, ResourceUtil.METHANE));
		inv.addAmountResourceTypeCapacity(ResourceUtil.oxygenID, oxygenCapacity);
		inv.addAmountResourceTypeCapacity(ResourceUtil.waterID, vehicleConfig.getCargoCapacity(type, LifeSupportInterface.WATER));
		inv.addAmountResourceTypeCapacity(ResourceUtil.foodID, vehicleConfig.getCargoCapacity(type, LifeSupportInterface.FOOD));
		inv.addAmountResourceTypeCapacity(ResourceUtil.rockSamplesID, vehicleConfig.getCargoCapacity(type, ResourceUtil.ROCK_SAMLES));
		inv.addAmountResourceTypeCapacity(ResourceUtil.iceID, vehicleConfig.getCargoCapacity(type, ResourceUtil.ICE));
		
		inv.addAmountResourceTypeCapacity(ResourceUtil.foodWasteID,
				vehicleConfig.getCargoCapacity(type, ResourceUtil.FOOD_WASTE));
		inv.addAmountResourceTypeCapacity(ResourceUtil.solidWasteID,
				vehicleConfig.getCargoCapacity(type, ResourceUtil.SOLID_WASTE));
		inv.addAmountResourceTypeCapacity(ResourceUtil.toxicWasteID,
				vehicleConfig.getCargoCapacity(type, ResourceUtil.TOXIC_WASTE));
		inv.addAmountResourceTypeCapacity(ResourceUtil.blackWaterID,
				vehicleConfig.getCargoCapacity(type, ResourceUtil.BLACK_WATER));
		inv.addAmountResourceTypeCapacity(ResourceUtil.greyWaterID,
				vehicleConfig.getCargoCapacity(type, ResourceUtil.GREY_WATER));

		// Construct sick bay.
		if (vehicleConfig.hasSickbay(type)) {
			sickbay = new SickBay(this, vehicleConfig.getSickbayTechLevel(type),
					vehicleConfig.getSickbayBeds(type));

			// Initialize sick bay activity spots.
			sickBayActivitySpots = new ArrayList<Point2D>(vehicleConfig.getSickBayActivitySpots(type));
		}

		// Construct lab.
		if (vehicleConfig.hasLab(type)) {
			lab = new MobileLaboratory(1, vehicleConfig.getLabTechLevel(type),
					vehicleConfig.getLabTechSpecialties(type));

			// Initialize lab activity spots.
			labActivitySpots = new ArrayList<Point2D>(vehicleConfig.getLabActivitySpots(type));
		}
		// Set rover terrain modifier
		setTerrainHandlingCapability(0D);

		// Create the rover's airlock.
		double airlockXLoc = vehicleConfig.getAirlockXLocation(type);
		double airlockYLoc = vehicleConfig.getAirlockYLocation(type);
		double airlockInteriorXLoc = vehicleConfig.getAirlockInteriorXLocation(type);
		double airlockInteriorYLoc = vehicleConfig.getAirlockInteriorYLocation(type);
		double airlockExteriorXLoc = vehicleConfig.getAirlockExteriorXLocation(type);
		double airlockExteriorYLoc = vehicleConfig.getAirlockExteriorYLocation(type);

		try {
			airlock = new VehicleAirlock(this, 2, airlockXLoc, airlockYLoc, airlockInteriorXLoc, airlockInteriorYLoc,
					airlockExteriorXLoc, airlockExteriorYLoc);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Sets the vehicle this rover is currently towing.
	 * 
	 * @param towedVehicle the vehicle being towed.
	 */
	public void setTowedVehicle(Vehicle towedVehicle) {
		if (this == towedVehicle)
			throw new IllegalArgumentException("Rover cannot tow itself.");
		if (towedVehicle != null) {
			// if towedVehicle is not null, it means this rover has just hooked up for towing the towedVehicle
			addStatus(StatusType.TOWING);
		}
		this.towedVehicle = towedVehicle;
		//updatedTowedVehicleSettlementLocation();
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
	 * Is this rover towing another vehicle.
	 * 
	 * @return true or false
	 */
	public boolean isTowingAVehicle() {
		if (towedVehicle != null)
			return true;
		else
			return false;
	}

	/**
	 * Gets the number of crewmembers the vehicle can carry.
	 * 
	 * @return capacity
	 */
	public int getCrewCapacity() {
		return crewCapacity;
	}

	public int getRobotCrewCapacity() {
		return robotCrewCapacity;
	}

	/**
	 * Gets the current number of crewmembers.
	 * 
	 * @return number of crewmembers
	 */
	public int getCrewNum() {
		return getInventory().getNumContainedPeople(); //getCrew().size();
	}

	public int getRobotCrewNum() {
		return getInventory().getNumContainedRobots(); //return getRobotCrew().size();
	}

	/**
	 * Gets a collection of the crewmembers.
	 * 
	 * @return crewmembers as Collection
	 */
	public Collection<Person> getCrew() {
		return getInventory().getContainedPeople();
//		return CollectionUtils.getPerson(getInventory().getContainedUnits());
	}

	public Collection<Robot> getRobotCrew() {
		return getInventory().getContainedRobots();
//		return CollectionUtils.getRobot(getInventory().getContainedUnits());
	}

	/**
	 * Checks if person is a crewmember.
	 * 
	 * @param person the person to check
	 * @return true if person is a crewmember
	 */
	public boolean isCrewmember(Person person) {
		return getInventory().containsUnit(person);
	}

	/**
	 * Checks if robot is a crewmember.
	 * 
	 * @param robot the robot to check
	 * @return true if robot is a crewmember
	 */
	public boolean isRobotCrewmember(Robot robot) {
		return getInventory().containsUnit(robot);
	}

	/**
	 * Returns true if life support is working properly and is not out of oxygen or
	 * water.
	 * 
	 * @return true if life support is OK
	 * @throws Exception if error checking life support.
	 */
	public boolean lifeSupportCheck() {
		boolean result = true;
		
		Inventory inv = null;
		
		// TODO: need to draw the the hose connecting between the vehicle and the settlement to supply resources
		
		if (isPluggedIn()) {
			if (haveStatusType(StatusType.TOWED) && !isInSettlement()) {
				inv = getTowingVehicle().getInventory();
			}
//			if (getSettlement() == null) {
//				LogConsolidated.log(Level.SEVERE, 1_000, sourceName, "[" + this.getName() + "] " 
//						+ getNickName() + "'s settlement is null");
//			
//				inv = getInventory();
//			}
			else
				// Use the settlement's inventory
				inv = getSettlement().getInventory();
		}
		else 
			inv = getInventory();
		
		double o2 = inv.getAmountResourceStored(ResourceUtil.oxygenID, false);
		if (o2 < SMALL_AMOUNT) {
			LogConsolidated.log(Level.WARNING, 30_000, sourceName,
					"[" + this.getLocationTag().getLocale() + "] " 
							+ this.getName() + " had no more oxygen.");
			result = false;
		}
		
		else if (o2 <= massO2MinimumLimit) {
			LogConsolidated.log(Level.WARNING, 10_000, sourceName,
					"[" + this.getLocationTag().getLocale() + "] " 
							+ this.getName() + "'s remaining oxygen was below the safety threshold (" 
							+ massO2MinimumLimit + " kg) ");
			result = false;
		}
		
		if (inv.getAmountResourceStored(ResourceUtil.waterID, false) <= 0D) {
			LogConsolidated.log(Level.WARNING, 10_000, sourceName,
					"[" + this.getLocationTag().getLocale() + "] " 
							+ this.getName() + " ran out of water.");
			result = false;
		}
		
//		if (malfunctionManager.getOxygenFlowModifier() < 100D)
//			result = false;
//		if (malfunctionManager.getWaterFlowModifier() < 100D)
//			result = false;

		double p = getAirPressure();
		if (p > PhysicalCondition.MAXIMUM_AIR_PRESSURE || p <= min_o2_pressure) {
			LogConsolidated.log(Level.SEVERE, 10_000, sourceName,
					"[" + this.getName() + "] out-of-range O2 pressure at " + Math.round(p * 100.0D) / 100.0D 
					+ " kPa detected.");
			result = false;
		}
		
		double t = getTemperature();
		if (t < Settlement.life_support_value[0][4] - Settlement.SAFE_TEMPERATURE_RANGE
				|| t > Settlement.life_support_value[1][4] + Settlement.SAFE_TEMPERATURE_RANGE) {
				LogConsolidated.log(Level.SEVERE, 10_000, sourceName,
					"[" + this.getName() + "] out-of-range overall temperature at " + Math.round(t * 100.0D) / 100.0D 
						+ " " + Msg.getString("temperature.sign.degreeCelsius") + " detected.");		
			result = false;
		}

		return result;
	}

	/**
	 * Gets the number of people the life support can provide for.
	 * 
	 * @return the capacity of the life support system
	 */
	public int getLifeSupportCapacity() {
		return crewCapacity;
	}

	/**
	 * Is the rover connected to the settlement through hoses
	 * 
	 * @return true if yes
	 */
	public boolean isPluggedIn() {
		if (isInSettlement())
			return true;
		
		if (haveStatusType(StatusType.GARAGED))
			return true;
		
		if (haveStatusType(StatusType.TOWED))
			return true;
		
		return false;
	}
	
	/**
	 * Gets oxygen from system.
	 * 
	 * @param amountRequested the amount of oxygen requested from system (kg)
	 * @return the amount of oxgyen actually received from system (kg)
	 * @throws Exception if error providing oxygen.
	 */
	public double provideOxygen(double amountRequested) {
		Inventory inv = null;
		
		// TODO: need to draw the the hose connecting between the vehicle and the settlement to supply resources
		
		if (isPluggedIn()) {
			if (haveStatusType(StatusType.TOWED) && !isInSettlement()) {
				inv = getTowingVehicle().getInventory();
			}
				
//			if (getSettlement() == null) {
//				LogConsolidated.log(Level.SEVERE, 1_000, sourceName, "[" + this.getName() + "] " 
//					+ getNickName() + "'s settlement is null");
//		
//				
//				inv = getInventory();
//			}
			else
				// Use the settlement's inventory
				inv = getSettlement().getInventory();
		}
		else
			inv = getInventory();
		
		double oxygenTaken = amountRequested;
		double oxygenLeft = inv.getAmountResourceStored(ResourceUtil.oxygenID, false);

		if (oxygenTaken > oxygenLeft)
			oxygenTaken = oxygenLeft;

		getInventory().retrieveAmountResource(ResourceUtil.oxygenID, oxygenTaken);
//		getInventory().addAmountDemandTotalRequest(ResourceUtil.oxygenID);
//		getInventory().addAmountDemand(ResourceUtil.oxygenID, oxygenTaken);

//		return oxygenTaken * (malfunctionManager.getOxygenFlowModifier() / 100D);
		return oxygenTaken;
	}

	/**
	 * Gets water from system.
	 * 
	 * @param amountRequested the amount of water requested from system (kg)
	 * @return the amount of water actually received from system (kg)
	 * @throws Exception if error providing water.
	 */
	public double provideWater(double amountRequested) {
		Inventory inv = null;
		// TODO: need to draw the the hose connecting between the vehicle and the settlement to supply resources
		
		if (isPluggedIn()) {
			if (haveStatusType(StatusType.TOWED) && !isInSettlement()) {
				inv = getTowingVehicle().getInventory();
			}
//			if (getSettlement() == null) {
//				LogConsolidated.log(Level.SEVERE, 1_000, sourceName, "[" + this.getName() + "] " 
//					+ getNickName() + "'s settlement is null");
//		
//				inv = getInventory();
//			}
			else
				// Use the settlement's inventory
				inv = getSettlement().getInventory();
		}
		else
			inv = getInventory();
		
		double waterTaken = amountRequested;
		double waterLeft = inv.getAmountResourceStored(ResourceUtil.waterID, false);

		if (waterTaken > waterLeft)
			waterTaken = waterLeft;

		getInventory().retrieveAmountResource(ResourceUtil.waterID, waterTaken);
//		getInventory().addAmountDemandTotalRequest(ResourceUtil.waterID);
//		getInventory().addAmountDemand(ResourceUtil.waterID, waterTaken);

//		return waterTaken * (malfunctionManager.getWaterFlowModifier() / 100D);
		return waterTaken;
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

		double oxygenLeft = getInventory().getAmountResourceStored(ResourceUtil.oxygenID, false);
		// Assuming that we can maintain a constant oxygen partial pressure unless it falls below massO2NominalLimit 
		if (oxygenLeft < massO2NominalLimit) {
			double remainingMass = oxygenLeft;
			double pp = CompositionOfAir.KPA_PER_ATM * remainingMass / CompositionOfAir.O2_MOLAR_MASS * CompositionOfAir.R_GAS_CONSTANT / cabinAirVolume;
			LogConsolidated.log(Level.WARNING, 10_000, sourceName,
					"[" + this.getLocationTag().getLocale() + "] " 
						+ this.getName() + " has " + Math.round(oxygenLeft*100.0)/100.0
						+ " kg O2 left at partial pressure of " + Math.round(pp*100.0)/100.0);
			return pp;
		}

//		Note: the outside ambient air pressure is weather.getAirPressure(getCoordinates());

		return NORMAL_AIR_PRESSURE;// * (malfunctionManager.getAirPressureModifier() / 100D);	
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
				if (haveStatusType(StatusType.GARAGED))
					p = getGarage().getCurrentTemperature();
				else 
					p = getSettlement().getTemperature();// * (malfunctionManager.getTemperatureModifier() / 100D);
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
	 * Adjust the air pressure of the rover
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
				if (haveStatusType(StatusType.GARAGED))
					p = getGarage().getCurrentAirPressure();
				else 
					p = getSettlement().getAirPressure();// * (malfunctionManager.getAirPressureModifier() / 100D);
				
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
	
//			double nitrogenLeft = getInventory().getAmountResourceStored(ResourceUtil.nitrogenID, false);
//			double oxygenLeft = getInventory().getAmountResourceStored(ResourceUtil.oxygenID, false);
//			double co2Left = getInventory().getAmountResourceStored(ResourceUtil.oxygenID, false);
//			double waterLeft = getInventory().getAmountResourceStored(ResourceUtil.waterID, false);
//			double sum = nitrogenLeft + oxygenLeft + co2Left + waterLeft;
			
			double rate = RATE_OF_CHANGE_OF_kPa_PER_MILLISOL * time;
			
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
	 * @param time the amount of time passing (in millisols)
	 * @throws exception if error during time.
	 */
	public void timePassing(double time) {
		super.timePassing(time);

		airlock.timePassing(time);
		
		boolean onAMission = isOnAMission();
		if (onAMission || isReservedForMission()) {
			if (isInSettlement()) {
				plugInTemperature(time);
				plugInAirPressure(time);	
			}
			
			if (getInventory().getAmountResourceStored(getFuelType(), false) > GroundVehicle.LEAST_AMOUNT)
				if (super.haveStatusType(StatusType.OUT_OF_FUEL))
					super.removeStatus(StatusType.OUT_OF_FUEL);
			
//			String s = this + " is plugged in.  " +  + airPressure + " kPa  " + temperature + " C";
//			if (!sCache.equals(s)) {
//				sCache = s;
//				logger.info(sCache);
//			}
		}
		
		else if (crewCapacity <= 0) {
			plugOffTemperature(time);
			plugOffAirPressure(time);
		}
	
			
		
	}

	/**
	 * Gets a collection of people affected by this entity.
	 * 
	 * @return people collection
	 */
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
	public List<Point2D> getLabActivitySpots() {
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
	public List<Point2D> getSickBayActivitySpots() {
		return sickBayActivitySpots;
	}

	/**
	 * Checks if a particular operator is appropriate for a vehicle.
	 * 
	 * @param operator the operator to check
	 * @return true if appropriate operator for this vehicle.
	 */
	public boolean isAppropriateOperator(VehicleOperator operator) {
		return (operator instanceof Person) && (getInventory().containsUnit((Unit) operator));
	}

	/**
	 * Gets the resource type that this vehicle uses for fuel.
	 * 
	 * @return resource type as a string
	 */
	public int getFuelType() {
//    	try {
		return ResourceUtil.methaneID;
//    	}
//    	catch (Exception e) {
//    		return null;
//    	}
	}

	public AmountResource getFuelTypeAR() {
		try {
			return ResourceUtil.methaneAR;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Sets unit's location coordinates
	 * 
	 * @param newLocation the new location of the unit
	 */
	public void setCoordinates(Coordinates newLocation) {
		super.setCoordinates(newLocation);

		// Set towed vehicle (if any) to new location.
		if (towedVehicle != null)
			towedVehicle.setCoordinates(newLocation);
	}

	/**
	 * Gets the range of the vehicle
	 * 
	 * @return the range of the vehicle (in km)
	 * @throws Exception if error getting range.
	 */
	public double getRange(MissionType missionType) {
		// Note: multiply by 0.9 would account for the extra distance travelled in between sites 
		double fuelRange = super.getRange(missionType) * FUEL_RANGE_FACTOR;
		// Obtains the max mission range [in km] based on the type of mission
		// Note: total route ~= mission radius * 2   
		double missionRange = super.getMissionRange(missionType) * MISSION_RANGE_FACTOR;
		
		// Estimate the distance traveled per sol
		double distancePerSol = getEstimatedTravelDistancePerSol();
		
		// Gets the life support resource margin
		double margin = Vehicle.getLifeSupportRangeErrorMargin();
		
		// Check food capacity as range limit.
		double foodConsumptionRate = personConfig.getFoodConsumptionRate();
		double foodCapacity = getInventory().getAmountResourceCapacity(ResourceUtil.foodID, false);
		double foodSols = foodCapacity / (foodConsumptionRate * crewCapacity);
		double foodRange = distancePerSol * foodSols / margin;

		// Check water capacity as range limit.
		double waterConsumptionRate = personConfig.getWaterConsumptionRate();
		double waterCapacity = getInventory().getAmountResourceCapacity(ResourceUtil.waterID, false);
		double waterSols = waterCapacity / (waterConsumptionRate * crewCapacity);
		double waterRange = distancePerSol * waterSols / margin;
//    	if (waterRange < fuelRange) fuelRange = waterRange;

		// Check oxygen capacity as range limit.
		double oxygenConsumptionRate = personConfig.getNominalO2ConsumptionRate();
		double oxygenCapacity = getInventory().getAmountResourceCapacity(ResourceUtil.oxygenID, false);
		double oxygenSols = oxygenCapacity / (oxygenConsumptionRate * crewCapacity);
		double oxygenRange = distancePerSol * oxygenSols / margin;
//    	if (oxygenRange < fuelRange) fuelRange = oxygenRange;

		double max = Math.min(oxygenRange, Math.min(foodRange, Math.min(waterRange, Math.min(missionRange, fuelRange))));

//		String s0 = this + " - " + missionName + " \n";
//		String s1 = String.format(" Radius : %5.0f km   Fuel : %5.0f km   Dist/sol : %5.0f km   Max : %5.0f km", 
//				missionRange, fuelRange, distancePerSol, max);
//		System.out.print(s0);
//		System.out.println(s1);
		
		return max;
	}

	@Override
	public void setParkedLocation(double xLocation, double yLocation, double facing) {
		super.setParkedLocation(xLocation, yLocation, facing);

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
				double towedX = 0D;
				double towedY = 0D - distance;
				Point2D.Double towedLoc = LocalAreaUtil.getLocalRelativeLocation(towedX, towedY, this);
				towedVehicle.setParkedLocation(towedLoc.getX(), towedLoc.getY(), getFacing());
			} else if (towedVehicle instanceof LightUtilityVehicle) {
				// Towed light utility vehicles should be attached to back of the rover
				// sideways and facing to the right.
				double distance = (getLength() + towedVehicle.getWidth()) / 2D;
				double towedX = 0D;
				double towedY = 0D - distance;
				Point2D.Double towedLoc = LocalAreaUtil.getLocalRelativeLocation(towedX, towedY, this);
				towedVehicle.setParkedLocation(towedLoc.getX(), towedLoc.getY(), getFacing() + 90D);
			}
		}

	}

	@Override
	public Collection<Unit> getUnitCrew() {
		// TODO Auto-generated method stub
		return null;
	}

	// public static double getLifeSupportRangeErrorMargin() {
	// return life_support_range_error_margin;
	// }

	@Override
	public String getNickName() {
		return getName();
	}
	
	
	public double getCargoCapacity() {
		return cargoCapacity;
	}
	
	@Override
	public void destroy() {
		super.destroy();

		// vehicleConfig = null;
		personConfig = null;
		// inv = null;
//		weather = null;
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