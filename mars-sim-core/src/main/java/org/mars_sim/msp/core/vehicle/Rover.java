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

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.SystemType;

/**
 * The Rover class represents the rover type of ground vehicle. It contains
 * information about the rover.
 */
public class Rover extends GroundVehicle implements Crewable, LifeSupportType, Airlockable, Medical, Towing {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** The amount of work time to perform maintenance (millisols) */
	public static final double MAINTENANCE_WORK_TIME = 500D;

	/** Normal air pressure (Pa). */
	private double NORMAL_AIR_PRESSURE = 101325D;
	/** Normal temperature (celsius). */
	private double NORMAL_TEMP = 22.5D;

	// Data members
	/** The rover's capacity for crew members. */
	private int crewCapacity = 0;
	private int robotCrewCapacity = 0;

	/** The rover's inventory. */
	// private Inventory inv;
	/** The rover's airlock. */
	private Airlock airlock;
	/** The rover's lab. */
	private Lab lab;
	/** The rover's sick bay. */
	private SickBay sickbay;
	/** The vehicle the rover is currently towing. */
	private Vehicle towedVehicle;

	private List<Point2D> labActivitySpots;
	private List<Point2D> sickBayActivitySpots;

	// Static data members
	// private static VehicleConfig vehicleConfig;
	private static PersonConfig personConfig;
	private static Weather weather;

	/**
	 * Constructs a Rover object at a given settlement
	 * 
	 * @param name        the name of the rover
	 * @param description the configuration description of the vehicle.
	 * @param settlement  the settlement the rover is parked at
	 */
	public Rover(String name, String description, Settlement settlement) {
		// Use GroundVehicle constructor
		super(name, description, settlement, MAINTENANCE_WORK_TIME);

		// life_support_range_error_margin =
		// SimulationConfig.instance().getSettlementConfiguration().loadMissionControl()[0];

		// Get vehicle configuration.
		weather = Simulation.instance().getMars().getWeather();
		VehicleConfig vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();
		personConfig = SimulationConfig.instance().getPersonConfiguration();

		// Add scope to malfunction manager.
		malfunctionManager.addScopeString(SystemType.ROVER.toString());
		// malfunctionManager.addScopeString("Crewable");
		malfunctionManager.addScopeString(FunctionType.LIFE_SUPPORT.getName());
		// malfunctionManager.addScopeString(description);
		// if (config.hasLab(description))
		// malfunctionManager.addScopeString("Laboratory");
		// if (config.hasSickbay(description))
		// malfunctionManager.addScopeString("Sickbay");

		// Set crew capacity
		crewCapacity = vehicleConfig.getCrewSize(description);
		robotCrewCapacity = crewCapacity;

		Inventory inv = getInventory();

		// Set inventory total mass capacity.
		inv.addGeneralCapacity(vehicleConfig.getTotalCapacity(description));

		// Set inventory resource capacities.
		inv.addAmountResourceTypeCapacity(ResourceUtil.methaneID,
				vehicleConfig.getCargoCapacity(description, ResourceUtil.METHANE));
		inv.addARTypeCapacity(ResourceUtil.oxygenID,
				vehicleConfig.getCargoCapacity(description, LifeSupportType.OXYGEN));
		inv.addARTypeCapacity(ResourceUtil.waterID, vehicleConfig.getCargoCapacity(description, LifeSupportType.WATER));
		inv.addARTypeCapacity(ResourceUtil.foodID, vehicleConfig.getCargoCapacity(description, LifeSupportType.FOOD));
		inv.addAmountResourceTypeCapacity(ResourceUtil.rockSamplesID,
				vehicleConfig.getCargoCapacity(description, ResourceUtil.ROCK_SAMLES));
		inv.addARTypeCapacity(ResourceUtil.iceID, vehicleConfig.getCargoCapacity(description, ResourceUtil.ICE));
		inv.addAmountResourceTypeCapacity(ResourceUtil.foodWasteAR,
				vehicleConfig.getCargoCapacity(description, ResourceUtil.FOOD_WASTE));
		inv.addAmountResourceTypeCapacity(ResourceUtil.solidWasteAR,
				vehicleConfig.getCargoCapacity(description, ResourceUtil.SOLID_WASTE));
		inv.addAmountResourceTypeCapacity(ResourceUtil.toxicWasteAR,
				vehicleConfig.getCargoCapacity(description, ResourceUtil.TOXIC_WASTE));
		inv.addAmountResourceTypeCapacity(ResourceUtil.blackWaterID,
				vehicleConfig.getCargoCapacity(description, ResourceUtil.BLACK_WATER));
		inv.addAmountResourceTypeCapacity(ResourceUtil.greyWaterID,
				vehicleConfig.getCargoCapacity(description, ResourceUtil.GREY_WATER));

		// Construct sick bay.
		if (vehicleConfig.hasSickbay(description)) {
			sickbay = new SickBay(this, vehicleConfig.getSickbayTechLevel(description),
					vehicleConfig.getSickbayBeds(description));

			// Initialize sick bay activity spots.
			sickBayActivitySpots = new ArrayList<Point2D>(vehicleConfig.getSickBayActivitySpots(description));
		}

		// Construct lab.
		if (vehicleConfig.hasLab(description)) {
			lab = new MobileLaboratory(1, vehicleConfig.getLabTechLevel(description),
					vehicleConfig.getLabTechSpecialties(description));

			// Initialize lab activity spots.
			labActivitySpots = new ArrayList<Point2D>(vehicleConfig.getLabActivitySpots(description));
		}
		// Set rover terrain modifier
		setTerrainHandlingCapability(0D);

		// Create the rover's airlock.
		double airlockXLoc = vehicleConfig.getAirlockXLocation(description);
		double airlockYLoc = vehicleConfig.getAirlockYLocation(description);
		double airlockInteriorXLoc = vehicleConfig.getAirlockInteriorXLocation(description);
		double airlockInteriorYLoc = vehicleConfig.getAirlockInteriorYLocation(description);
		double airlockExteriorXLoc = vehicleConfig.getAirlockExteriorXLocation(description);
		double airlockExteriorYLoc = vehicleConfig.getAirlockExteriorYLocation(description);

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
		return getCrew().size();
	}

	public int getRobotCrewNum() {
		return getRobotCrew().size();
	}

	/**
	 * Gets a collection of the crewmembers.
	 * 
	 * @return crewmembers as Collection
	 */
	public Collection<Person> getCrew() {
		return CollectionUtils.getPerson(getInventory().getContainedUnits());
	}

	public Collection<Robot> getRobotCrew() {
		return CollectionUtils.getRobot(getInventory().getContainedUnits());
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

		if (getInventory().getARStored(ResourceUtil.oxygenID, false) <= 0D)
			result = false;
		if (getInventory().getARStored(ResourceUtil.waterID, false) <= 0D)
			result = false;

		if (malfunctionManager.getOxygenFlowModifier() < 100D)
			result = false;
		if (malfunctionManager.getWaterFlowModifier() < 100D)
			result = false;

		if (getAirPressure() != NORMAL_AIR_PRESSURE)
			result = false;
		if (getTemperature() != NORMAL_TEMP)
			result = false;

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
	 * Gets oxygen from system.
	 * 
	 * @param amountRequested the amount of oxygen requested from system (kg)
	 * @return the amount of oxgyen actually received from system (kg)
	 * @throws Exception if error providing oxygen.
	 */
	public double provideOxygen(double amountRequested) {
		double oxygenTaken = amountRequested;
		double oxygenLeft = getInventory().getAmountResourceStored(ResourceUtil.oxygenID, false);

		if (oxygenTaken > oxygenLeft)
			oxygenTaken = oxygenLeft;

		getInventory().retrieveAmountResource(ResourceUtil.oxygenID, oxygenTaken);
		getInventory().addAmountDemandTotalRequest(ResourceUtil.oxygenID);
		getInventory().addAmountDemand(ResourceUtil.oxygenID, oxygenTaken);

		return oxygenTaken * (malfunctionManager.getOxygenFlowModifier() / 100D);
	}

	/**
	 * Gets water from system.
	 * 
	 * @param amountRequested the amount of water requested from system (kg)
	 * @return the amount of water actually received from system (kg)
	 * @throws Exception if error providing water.
	 */
	public double provideWater(double amountRequested) {
		double waterTaken = amountRequested;
		double waterLeft = getInventory().getAmountResourceStored(ResourceUtil.waterID, false);

		if (waterTaken > waterLeft)
			waterTaken = waterLeft;

		getInventory().retrieveAmountResource(ResourceUtil.waterID, waterTaken);
		getInventory().addAmountDemandTotalRequest(ResourceUtil.waterID);
		getInventory().addAmountDemand(ResourceUtil.waterID, waterTaken);

		return waterTaken * (malfunctionManager.getWaterFlowModifier() / 100D);
	}

	/**
	 * Gets the air pressure of the life support system.
	 * 
	 * @return air pressure (Pa)
	 */
	public double getAirPressure() {
		double result = NORMAL_AIR_PRESSURE * (malfunctionManager.getAirPressureModifier() / 100D);
		double ambient = weather.getAirPressure(getCoordinates());
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
		double result = NORMAL_TEMP * (malfunctionManager.getTemperatureModifier() / 100D);
		double ambient = weather.getTemperature(getCoordinates());
		if (result < ambient)
			return ambient;
		else
			return result;
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
	}

	/**
	 * Gets a collection of people affected by this entity.
	 * 
	 * @return person collection
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
	public double getRange() {
		double fuelRange = super.getRange();
		double maxRange = super.getSettlement().getMaxMssionRange();
		double distancePerSol = getEstimatedTravelDistancePerSol();

		// Check food capacity as range limit.
		double foodConsumptionRate = personConfig.getFoodConsumptionRate();
		double foodCapacity = getInventory().getARCapacity(ResourceUtil.foodID, false);
		double foodSols = foodCapacity / (foodConsumptionRate * crewCapacity);
		double foodRange = distancePerSol * foodSols / Vehicle.getLifeSupportRangeErrorMargin();

		// Check water capacity as range limit.
		double waterConsumptionRate = personConfig.getWaterConsumptionRate();
		double waterCapacity = getInventory().getARCapacity(ResourceUtil.waterID, false);
		double waterSols = waterCapacity / (waterConsumptionRate * crewCapacity);
		double waterRange = distancePerSol * waterSols / Vehicle.getLifeSupportRangeErrorMargin();
//    	if (waterRange < fuelRange) fuelRange = waterRange;

		// Check oxygen capacity as range limit.
		double oxygenConsumptionRate = personConfig.getNominalO2ConsumptionRate();
		double oxygenCapacity = getInventory().getARCapacity(ResourceUtil.oxygenID, false);
		double oxygenSols = oxygenCapacity / (oxygenConsumptionRate * crewCapacity);
		double oxygenRange = distancePerSol * oxygenSols / Vehicle.getLifeSupportRangeErrorMargin();
//    	if (oxygenRange < fuelRange) fuelRange = oxygenRange;

		return Math.min(oxygenRange, Math.min(foodRange, Math.min(waterRange, Math.min(maxRange, fuelRange))));

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

	@Override
	public void destroy() {
		super.destroy();

		// vehicleConfig = null;
		personConfig = null;
		// inv = null;
		weather = null;
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

	@Override
	public Settlement getBuriedSettlement() {
		// TODO Auto-generated method stub
		return null;
	}

}