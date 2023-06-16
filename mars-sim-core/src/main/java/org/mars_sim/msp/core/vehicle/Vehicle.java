/*
 * Mars Simulation Project
 * Vehicle.java
 * @date 2023-06-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.vehicle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.data.MSolDataItem;
import org.mars_sim.msp.core.data.MSolDataLogger;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentInventory;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.equipment.ItemHolder;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.Conversation;
import org.mars_sim.msp.core.person.ai.task.MaintainBuilding;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.project.Stage;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.Indoor;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.SystemType;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Vehicle class represents a generic vehicle. It keeps track of generic
 * information about the vehicle. This class needs to be subclassed to represent
 * a specific type of vehicle.
 */
public abstract class Vehicle extends Unit
		implements Malfunctionable, Salvagable, Temporal, Indoor,
		LocalBoundedObject, EquipmentOwner, ItemHolder {

	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(Vehicle.class.getName());
	
	private static final double RANGE_FACTOR = 1.2;
	private static final double MAXIMUM_RANGE = 10_000;
	
	private static final int MAX_NUM_SOLS = 14;
	
	/** The error margin for determining vehicle range. (Actual distance / Safe distance). */
	private static double fuel_range_error_margin;
	private static double life_support_range_error_margin;

	/** The types of status types that make a vehicle unavailable for us. */
	private static final List<StatusType> badStatus = Arrays.asList(
			StatusType.MAINTENANCE,
			StatusType.TOWED,
			StatusType.MOVING,
			StatusType.STUCK,
			StatusType.MALFUNCTION);

	/** True if vehicle is currently reserved for a mission. */
	protected boolean isReservedMission;
	/** True if vehicle is due for maintenance. */
	private boolean distanceMark;
	/** True if vehicle is currently reserved for periodic maintenance. */
	private boolean reservedForMaintenance;
	/** The emergency beacon for the vehicle. True if beacon is turned on. */
	private boolean emergencyBeacon;
	/** True if vehicle is salvaged. */
	private boolean isSalvaged;
	
	/** Vehicle's associated Settlement. */
	private int associatedSettlementID;
	
	/** The average road load power of the vehicle [kph]. */
	private double averageRoadLoadSpeed;

	/** The average road load power of the vehicle [kW]. */
	private double averageRoadLoadPower;
			
	/** Parked facing (degrees clockwise from North). */
	private double facingParked;
	/** The Base Lifetime Wear in msols **/
	private double baseWearLifetime;
	/** Current accel of vehicle in m/s2. */
	private double accel = 0;
	/** Current speed of vehicle in kph. */
	private double speed = 0; //
	/** Total cumulative distance traveled by vehicle (km). */
	private double odometerMileage; //
	/** The last distance travelled by vehicle (km). */
	private double lastDistance;
	/** Distance traveled by vehicle since last maintenance (km) . */
	private double distanceMaint; //
	/** The cumulative fuel usage of the vehicle [kg] */
//	private double fuelCumUsed;
	/** The cumulative energy usage of the vehicle [kWh] */
	private double cumEnergyUsedKWH;
	/** The instantaneous fuel economy of the vehicle [km/kg]. */
	private double iFuelEconomy;
	/** The instantaneous fuel consumption of the vehicle [Wh/km]. */
	private double iFuelConsumption;
	/** The actual start mass of the vehicle (base mass + crew weight + full cargo weight) for a trip [km/kg]. */
	private double startMass = 0;
	
	/** The vehicle specification */
	private String specName;
	
	/** Parked position (meters) from center of settlement. */
	private LocalPosition posParked;
	
	/** The vehicle type. */
	protected VehicleType vehicleType;

	/** A collection of locations that make up the vehicle's trail. */
	private List<Coordinates> trail;
	/** List of operator activity spots. */
	private List<LocalPosition> operatorActivitySpots;
	/** List of passenger activity spots. */
	private List<LocalPosition> passengerActivitySpots;
	/** List of status types. */
	private Set<StatusType> statusTypes = new HashSet<>();
	
	/** The primary status type. */
	private StatusType primaryStatus;

	/** The vehicle's status log. */
	private MSolDataLogger<Set<StatusType>> vehicleLog = new MSolDataLogger<>(MAX_NUM_SOLS);
	/** The vehicle's road speed history. */
	private MSolDataLogger<Integer> roadSpeedHistory = new MSolDataLogger<>(MAX_NUM_SOLS);
	/** The vehicle's road power history. */	
	private MSolDataLogger<Integer> roadPowerHistory = new MSolDataLogger<>(MAX_NUM_SOLS);
	
	/** The malfunction manager for the vehicle. */
	protected MalfunctionManager malfunctionManager;
	/** Direction vehicle is traveling */
	private Direction direction;
	/** The operator of the vehicle. */
	private Worker vehicleOperator;
	/** The one currently towing this vehicle. */
	private Vehicle towingVehicle;
	/** The vehicle's salvage info. */
	private SalvageInfo salvageInfo;
	/** The EquipmentInventory instance. */
	private EquipmentInventory eqmInventory;
	/** The VehicleController instance. */
	private VehicleController vehicleController;
	/** The VehicleSpec instance. */
	private VehicleSpec spec;

	private Mission mission;
	
	static {
		life_support_range_error_margin = simulationConfig.getSettlementConfiguration()
				.getRoverValues()[0];
		fuel_range_error_margin = simulationConfig.getSettlementConfiguration().getRoverValues()[1];
	}

	/**
	 * Constructor 1 : prepares a Vehicle object with a given settlement.
	 *
	 * @param name                the vehicle's name
	 * @param vehicleType         the configuration description of the vehicle.
	 * @param settlement          the settlement the vehicle is parked at.
	 * @param maintenanceWorkTime the work time required for maintenance (millisols)
	 */
	Vehicle(String name, VehicleSpec spec, Settlement settlement, double maintenanceWorkTime) {
		// Use Unit constructor
		super(name, settlement.getCoordinates());
		
		this.spec = spec;
		this.specName = spec.getName();
		this.vehicleType = spec.getType();
		setBaseMass(spec.getEmptyMass());
		
		// Get the description
		String description = spec.getDescription();
		// Set the description
		setDescription(description);
		// Get cargo capacity
		double cargoCapacity = spec.getTotalCapacity();
		// Create microInventory instance
		eqmInventory = new EquipmentInventory(this, cargoCapacity);

		// Set the capacities for each supported resource
		Map<Integer, Double> capacities = spec.getCargoCapacityMap();
		if (capacities != null) {
			eqmInventory.setResourceCapacityMap(capacities);
		}

		// Set total distance traveled by vehicle (km)
		odometerMileage = 0;
		// Set distance traveled by vehicle since last maintenance (km)
		distanceMaint = 0;
		// Obtain the associated settlement ID
		associatedSettlementID = settlement.getIdentifier();

		direction = new Direction(0);
		trail = new ArrayList<>();
		statusTypes = new HashSet<>();

		isReservedMission = false;
		distanceMark = false;
		reservedForMaintenance = false;
		emergencyBeacon = false;
		isSalvaged = false;
		
		// Make this vehicle to be owned by the settlement
		settlement.addOwnedVehicle(this);

		// Set the initial coordinates to be that of the settlement
		setCoordinates(settlement.getCoordinates());
		baseWearLifetime = 668_000 * spec.getWearModifier(); // 668 Sols (1 orbit)


		// Initialize malfunction manager.
		malfunctionManager = new MalfunctionManager(this, baseWearLifetime, maintenanceWorkTime);

		setupScopeString();

		primaryStatus = StatusType.PARKED;
		
		writeLog();

		// Instantiate the motor controller
		vehicleController = new VehicleController(this);
		
		// Set initial parked location and facing at settlement.
		findNewParkingLoc();

		// Initialize operator activity spots.
		operatorActivitySpots = spec.getOperatorActivitySpots();

		// Initialize passenger activity spots.
		passengerActivitySpots = spec.getPassengerActivitySpots();
	}

	/**
	 * Sets the scope string.
	 */
	protected void setupScopeString() {
		// Add "vehicle" as scope
		malfunctionManager.addScopeString(SystemType.VEHICLE.getName());
	
		// Add its vehicle type as scope
		malfunctionManager.addScopeString(vehicleType.name());
	}
	
	/**
	 * Gets the base image for this Vehicle.
	 * 
	 * @todo This needs refactoring to avoid copying out VehicleSpec properties
	 * @return Name of base image for this vehicle
	 */
	public String getBaseImage() {
		return spec.getBaseImage();
	}
	/**
	 * Gets the name of the vehicle specification.
	 * 
	 * @see VehicleConfig#getVehicleSpec(String)
	 * @return Name of the VehicleSpec
	 */
	public String getSpecName() {
		return specName;
	}

	public VehicleType getVehicleType() {
		return vehicleType;
	}

	@Override
	public double getWidth() {
		return spec.getWidth();
	}

	@Override
	public double getLength() {
		return spec.getLength();
	}

	@Override
	public LocalPosition getPosition() {
		return posParked;
	}

	@Override
	public double getFacing() {
		return facingParked;
	}

	/**
	 * Gets a list of operator activity spots.
	 *
	 * @return list of activity spots as Point2D objects.
	 */
	public List<LocalPosition> getOperatorActivitySpots() {
		return operatorActivitySpots;
	}

	/**
	 * Gets a list of passenger activity spots.
	 *
	 * @return list of activity spots as Point2D objects.
	 */
	public List<LocalPosition> getPassengerActivitySpots() {
		return passengerActivitySpots;
	}

	/**
	 * Sets the location and facing of the vehicle when parked at a settlement.
	 *
	 * @param position  Position of the parking relative to the Settlement
	 * @param facing    (degrees from North clockwise).
	 */
	public void setParkedLocation(LocalPosition position, double facing) {
		// Set new parked location for the vehicle.
		this.posParked = position;
		this.facingParked = facing;
		
		// Get current human crew positions relative to the vehicle.
		Map<Person, LocalPosition> currentCrewPositions = getCurrentCrewPositions();
		// Set the human crew locations to the vehicle's new parked location.
		if (currentCrewPositions != null)
			setCrewPositions(currentCrewPositions);
		
		// Get current robot crew positions relative to the vehicle.
		Map<Robot, LocalPosition> currentRobotCrewPositions = getCurrentRobotCrewPositions();

		// Set the robot crew locations to the vehicle's new parked location.
		if (currentRobotCrewPositions != null)
			setRobotCrewPositions(currentRobotCrewPositions);
	}

	/**
	 * Sets the location and facing of the drone when parked at a settlement.
	 *
	 * @param position  Position of the parking relative to the Settlement
	 * @param facing    (degrees from North clockwise).
	 */
	public void setFlyerLocation(LocalPosition position, double facing) {
		// Set new parked location for the flyer.
		this.posParked = position;
		this.facingParked = facing;
	}
	
	/**
	 * Gets all human crew member positions relative to within the vehicle.
	 *
	 * @return map of crew members and their relative vehicle positions.
	 */
	private Map<Person, LocalPosition> getCurrentCrewPositions() {

		Map<Person, LocalPosition> result = null;

		// Record current object-relative crew positions if vehicle is crewable.
		if (this instanceof Crewable) {
			Crewable crewable = (Crewable) this;
			result = new HashMap<>(crewable.getCrewNum());
			Iterator<Person> i = crewable.getCrew().iterator();
			while (i.hasNext()) {
				Person crewmember = i.next();
				LocalPosition crewPos = LocalAreaUtil.getObjectRelativePosition(crewmember.getPosition(), this);
				result.put(crewmember, crewPos);
			}
		}

		return result;
	}

	/**
	 * Gets all robot crew member positions relative to within the vehicle.
	 *
	 * @return map of crew members and their relative vehicle positions.
	 */
	private Map<Robot, LocalPosition> getCurrentRobotCrewPositions() {

		Map<Robot, LocalPosition> result = null;

		// Record current object-relative crew positions if vehicle is crewable.
		if (this instanceof Crewable) {
			Crewable crewable = (Crewable) this;
			result = new HashMap<>(crewable.getRobotCrewNum());
			Iterator<Robot> i = ((Crewable) this).getRobotCrew().iterator();
			while (i.hasNext()) {
				Robot robotCrewmember = i.next();
				LocalPosition crewPos = LocalAreaUtil.getObjectRelativePosition(robotCrewmember.getPosition(), this);
				result.put(robotCrewmember, crewPos);
			}
		}

		return result;
	}

	/**
	 * Sets the positions of all human crew members (if any) to the vehicle's
	 * location.
	 * 
	 * @param currentCrewPositions
	 */
	private void setCrewPositions(Map<Person, LocalPosition> currentCrewPositions) {

		// Only move crew if vehicle is Crewable.
		if (this instanceof Crewable) {
			Iterator<Person> i = ((Crewable) this).getCrew().iterator();
			while (i.hasNext()) {
				Person crewmember = i.next();

				LocalPosition currentCrewPos = currentCrewPositions.get(crewmember);
				LocalPosition settlementLoc = LocalAreaUtil.getLocalRelativePosition(currentCrewPos,
																		this);
				crewmember.setPosition(settlementLoc);
			}
		}
	}

	/**
	 * Sets the positions of all robot crew members (if any) to the vehicle's
	 * location.
	 * 
	 * @param currentRobotCrewPositions
	 */
	private void setRobotCrewPositions(Map<Robot, LocalPosition> currentRobotCrewPositions) {

		// Only move crew if vehicle is Crewable.
		if (this instanceof Crewable) {
			Iterator<Robot> i = ((Crewable) this).getRobotCrew().iterator();
			while (i.hasNext()) {
				Robot robotCrewmember = i.next();

				LocalPosition currentCrewPos = currentRobotCrewPositions.get(robotCrewmember);
				LocalPosition settlementLoc = LocalAreaUtil.getLocalRelativePosition(currentCrewPos,
														this);
				robotCrewmember.setPosition(settlementLoc);
			}
		}
	}

	/**
	 * Prints a string list of status types.
	 *
	 * @return
	 */
	public String printStatusTypes() {
		StringBuilder builder = new StringBuilder();
		builder.append(primaryStatus.getName());

		for (StatusType st : statusTypes) {
			builder.append(", ").append(st.getName());
		}
		return builder.toString();
	}

	/**
	 * Checks if this vehicle has already been tagged with a status type.
	 *
	 * @param status the status type of interest
	 * @return yes if it has it
	 */
	public boolean haveStatusType(StatusType status) {
        return statusTypes.contains(status);
    }

	/**
	 * Checks if this vehicle has no issues and is ready for mission.
	 *
	 * @return yes if it has anyone of the bad status types
	 */
	public boolean isVehicleReady() {
	    for (StatusType st : badStatus) {
			if (statusTypes.contains(st))
				return false;
	    }

	    return true;
	}

	public StatusType getPrimaryStatus() {
		return primaryStatus;
	}

	/**
	 * Sets the Primary status of a Vehicle that represents it's situation.
	 * 
	 * @param newStatus Must be a primary status value
	 */
	public void setPrimaryStatus(StatusType newStatus) {
		setPrimaryStatus(newStatus, null);
	}

	/**
	 * Sets the Primary status of a Vehicle that represents it's situation. Also there is 
	 * a Secondary status on why the primary has changed.
	 * 
	 * @param newStatus Must be a primary status value
	 * @param secondary Reason for the change; can be null be none given
	 */
	public void setPrimaryStatus(StatusType newStatus, StatusType secondary) {
		if (!newStatus.isPrimary()) {
			throw new IllegalArgumentException("Status is not Primary " + newStatus.getName());
		}

		boolean doEvent = false;
		if (primaryStatus != newStatus) {
			primaryStatus = newStatus;
			doEvent = true;
		}

		// Secondary is optional
		if ((secondary != null) && !statusTypes.contains(secondary)) {
			statusTypes.add(secondary);
			doEvent = true;
		}

		if (doEvent) {
			writeLog();
			fireUnitUpdate(UnitEventType.STATUS_EVENT, newStatus);
		}
	}

	/**
	 * Adds a Secondary status type for this vehicle.
	 *
	 * @param newStatus the status to be added
	 */
	public void addSecondaryStatus(StatusType newStatus) {
		if (newStatus.isPrimary()) {
			throw new IllegalArgumentException("Status is not Secondary " + newStatus.getName());
		}

		// Update status based on current situation.
		if (!statusTypes.contains(newStatus)) {
			statusTypes.add(newStatus);
			writeLog();
			fireUnitUpdate(UnitEventType.STATUS_EVENT, newStatus);
		}
	}

	/**
	 * Removes a Secondary status type for this vehicle.
	 *
	 * @param oldStatus the status to be removed
	 */
	public void removeSecondaryStatus(StatusType oldStatus) {
		// Update status based on current situation.
		if (statusTypes.contains(oldStatus)) {
			statusTypes.remove(oldStatus);
			writeLog();
			fireUnitUpdate(UnitEventType.STATUS_EVENT, oldStatus);
		}
	}

	/**
	 * Checks if the vehicle is currently in a garage or not.
	 *
	 * @return true if vehicle is in a garage.
	 */
	public boolean isInAGarage() {

		Settlement settlement = getSettlement();
		if (settlement != null) {
			List<Building> list = settlement.getBuildingManager().getBuildings(FunctionType.VEHICLE_MAINTENANCE);
			for (Building garageBuilding : list) {
				VehicleMaintenance garage = garageBuilding.getVehicleMaintenance();
				if (garage != null) {
					if (garage.containsVehicle(this)
						|| (getVehicleType() == VehicleType.DELIVERY_DRONE
						&& garage.containsFlyer((Flyer)this))) {
							return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Records the status in the vehicle log.
	 */
	private void writeLog() {
		Set<StatusType> entry = new HashSet<>(statusTypes);
		entry.add(primaryStatus);
		vehicleLog.addDataPoint(entry);
	}

	/**
	 * Gets the vehicle log.
	 *
	 * @return a map of vehicle status by sol
	 */
	public Map<Integer, List<MSolDataItem<Set<StatusType>>>> getVehicleLog() {
		return vehicleLog.getHistory();
	}

	/**
	 * Checks if the vehicle is reserved for any reason.
	 *
	 * @return true if vehicle is currently reserved
	 */
	public boolean isReserved() {
		return isReservedMission || reservedForMaintenance;
	}

	/**
	 * Checks if the vehicle is reserved for a mission.
	 *
	 * @return true if vehicle is reserved for a mission.
	 */
	public boolean isReservedForMission() {
		return isReservedMission;
	}

	/**
	 * Sets if the vehicle is reserved for a mission or not.
	 *
	 * @param reserved the vehicle's reserved for mission status
	 */
	public void setReservedForMission(boolean reserved) {
		if (isReservedMission != reserved) {
			isReservedMission = reserved;
			fireUnitUpdate(UnitEventType.RESERVED_EVENT);
		}
	}

	/**
	 * Checks if the vehicle is reserved for maintenance.
	 *
	 * @return true if reserved for maintenance.
	 */
	public boolean isReservedForMaintenance() {
		return reservedForMaintenance;
	}

	/**
	 * Sets if the vehicle is reserved for maintenance or not.
	 *
	 * @param reserved true if reserved for maintenance
	 */
	public void setReservedForMaintenance(boolean reserved) {
		if (reservedForMaintenance != reserved) {
			reservedForMaintenance = reserved;
			fireUnitUpdate(UnitEventType.RESERVED_EVENT);
		}
	}

	/**
	 * Sets the vehicle that is currently towing this vehicle.
	 *
	 * @param towingVehicle the vehicle
	 */
	public void setTowingVehicle(Vehicle towingVehicle) {
		if (this == towingVehicle)
			throw new IllegalArgumentException("Vehicle cannot tow itself.");

		if (towingVehicle != null) {
			// if towedVehicle is not null, it means this rover has just hooked up for towing the towedVehicle
			addSecondaryStatus(StatusType.TOWED);
		}
		else {
			removeSecondaryStatus(StatusType.TOWED);
		}

		this.towingVehicle = towingVehicle;
	}

	/**
	 * Gets the vehicle that is currently towing this vehicle.
	 *
	 * @return towing vehicle
	 */
	public Vehicle getTowingVehicle() {
		return towingVehicle;
	}

	/**
	 * Checks if this vehicle is being towed (by another vehicle).
	 *
	 * @return true if it is being towed
	 */
	public boolean isBeingTowed() {
        return towingVehicle != null;
    }

	/**
	 * Gets the average power of the vehicle when operating [kW].
	 * 
	 * @return
	 */
	public double getAveragePower() {
		return spec.getBasePower();
	}
	
	/**
	 * Gets the speed of vehicle.
	 *
	 * @return the vehicle's speed (in km/hr)
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * Sets the vehicle's current speed.
	 *
	 * @param speed the vehicle's speed (in km/hr)
	 */
	public void setSpeed(double speed) {
		if (speed < 0D)
			throw new IllegalArgumentException("Vehicle speed cannot be less than 0 km/hr: " + speed);
		if (Double.isNaN(speed))
			throw new IllegalArgumentException("Vehicle speed is a NaN");

		if (speed != this.speed) {
			if (speed == 0D) {
				setPrimaryStatus(StatusType.PARKED);
			} 
			else if (this.speed == 0D) {
				// Was zero so now must be moving
				setPrimaryStatus(StatusType.MOVING);
			}
			this.speed = speed;
			fireUnitUpdate(UnitEventType.SPEED_EVENT);
		}
	}

	/**
	 * Gets the base speed of vehicle
	 *
	 * @return the vehicle's base speed (in kph or km/hr)
	 */
	public double getBaseSpeed() {
		return spec.getBaseSpeed();
	}


	/**
	 * Gets the current fuel range of the vehicle.
	 *
	 * @return the current fuel range of the vehicle (in km)
	 */
	public double getRange() {

		double range = 0;
		Mission mission = getMission();

        if ((mission == null) || (mission.getStage() == Stage.PREPARATION)) {
        	// Before the mission is created, the range would be based on vehicle's capacity
        	range = Math.min(getBaseRange() * RANGE_FACTOR, getEstimatedFuelEconomy() * getFuelCapacity()) * getMass() / getBeginningMass();// * fuel_range_error_margin
        }
        else {
        	
    		int fuelTypeID = getFuelTypeID();
    		if (fuelTypeID < 0) {
    			range = MAXIMUM_RANGE;
    		}
    		else {
                double amountOfFuel = getAmountResourceStored(fuelTypeID);
            	// During the journey, the range would be based on the amount of fuel in the vehicle
        		range = Math.min(getBaseRange() * RANGE_FACTOR, getEstimatedFuelEconomy() * amountOfFuel) * getMass() / getBeginningMass();
    		}
        }

        return (int)range;
	}

	/**
	 * Gets the base range of the vehicle.
	 *
	 * @return the base range of the vehicle [km]
	 * @throws Exception if error getting range.
	 */
	public double getBaseRange() {
		return spec.getBaseRange();
	}

	/**
	 * Gets the fuel capacity of the vehicle [kg].
	 *
	 * @return
	 */
	public double getFuelCapacity() {
		return spec.getFuelCapacity();
	}

	/**
	 * Sets the average road load speed of the vehicle [kph].
	 * 
	 * @return
	 */
	public void setAverageRoadLoadSpeed(int value) {
		logger.info(this, 10_000L, " AverageRoadLoadSpeed: " + value);
		roadSpeedHistory.addDataPoint(value);
	}
	
	/**
	 * Sets the average road load power of the vehicle [kW].
	 * 
	 * @return
	 */
	public void setAverageRoadLoadPower(int value) {
		logger.info(this, 10_000L, " AverageRoadLoadPower: " + value);
		roadPowerHistory.addDataPoint(value);
	}
	
	/**
	 * Gets the average road load power of the vehicle [kph].
	 * 
	 * @return
	 */
	public double getAverageRoadLoadSpeed() {
		return averageRoadLoadSpeed;
	}
	
	/**
	 * Gets the average road load power of the vehicle [kW].
	 * 
	 * @return
	 */
	public double getAverageRoadLoadPower() {
		return averageRoadLoadPower;
	}
	
	
	/**
	 * Gets the cumulative energy usage of the vehicle [kWh].
	 * 
	 * @return
	 */
	public double getCumEnergyUsage() {
		return cumEnergyUsedKWH;
	}
	
	/**
	 * Gets the energy available at the full tank [kWh].
	 *
	 * @return
	 */
	public double getEnergyCapacity() {
		return spec.getEnergyCapacity();
	}

	/**
	 * Gets the estimated energy available for the drivetrain [kWh].
	 *
	 * @return
	 */
	public double getDrivetrainEnergy() {
		return spec.getDrivetrainEnergy();
	}
	
	/**
	 * Gets the fuel to energy conversion factor [Wh/kg].
	 * 
	 * @return
	 */
	public double getFuelConv() {
		return spec.getFuel2DriveEnergy();
	}
	
	/**
	 * Gets the cumulative fuel economy [km/kg].
	 * 
	 * @return
	 */
	public double getCumFuelEconomy() {
//		return getFuelConv() / getCumFuelConsumption();
		if (odometerMileage == 0 || cumEnergyUsedKWH == 0)
			return 0;
		return odometerMileage / cumEnergyUsedKWH / 1000 * getFuelConv();
	}
	
	/**
	 * Gets the cumulative fuel consumption [Wh/km].
	 * 
	 * @return
	 */
	public double getCumFuelConsumption() {
		if (odometerMileage == 0 || cumEnergyUsedKWH == 0)
			return 0;
		return 1000 * cumEnergyUsedKWH / odometerMileage;
	}
	
	/**
	 * Gets the coefficient for converting cumulative FC to cumulative FE.
	 * 
	 * @return
	 */
	public double getCoeffCumFC2FE() {
		double cumFE = getCumFuelEconomy();
		double cumFC = getCumFuelConsumption();
		
		if (cumFE > 0 && cumFC > 0 && averageRoadLoadPower > 0 && averageRoadLoadSpeed >0)
			return cumFE / cumFC * averageRoadLoadPower / averageRoadLoadSpeed ;
		
		return 0;
	}
	
	
	/**
	 * Gets the base fuel economy of the vehicle [km/kg].
	 * 
	 * @return
	 */
	public double getBaseFuelEconomy() {
		return spec.getBaseFuelEconomy();
	}

	/**
	 * Gets the base fuel consumption of the vehicle [Wh/km].
	 * 
	 * @return
	 */
	public double getBaseFuelConsumption() {
		return spec.getBaseFuelConsumption();
	}
	
	/**
	 * Gets the instantaneous fuel consumption of the vehicle [Wh/km].
	 * 
	 * @return
	 */
	public double getIFuelConsumption() {
		return iFuelConsumption;
	}
	
	/**
	 * Sets the instantaneous fuel consumption of the vehicle [kWh/km].
	 * 
	 * @param iFuelC
	 */
	public void setIFuelConsumption(double iFuelC) {
		this.iFuelConsumption = iFuelC;
	}
	
	/**
	 * Sets the instantaneous fuel economy of the vehicle [km/kg].
	 * 
	 * @param iFuelEconomy
	 */
	public void setIFuelEconomy(double iFuelEconomy) {
		this.iFuelEconomy = iFuelEconomy;
	}
	
	/**
	 * Gets the instantaneous fuel economy of the vehicle [km/kg].
	 * 
	 * @return
	 */
	public double getIFuelEconomy() {
		return iFuelEconomy;
	}
	
	/**
	 * Mass of Equipment is the stored mass plus the base mass.
	 */
	@Override
	public double getMass() {
		return eqmInventory.getStoredMass() + getBaseMass();
	}
	
	/**
	 * Gets the estimated beginning mass [kg].
	 */
	public double getBeginningMass() {
		return spec.getBeginningMass();
	}
	
	/**
	 * Records the beginning weight of the vehicle and its payload [kg].
	 */
	public void recordStartMass() {
		startMass = getMass();
	}

	/**
	 * Records the beginning weight of the vehicle and its payload [kg].
	 */
	public double getStartMass() {
		return startMass;
	}
	
	/**
	 * Gets the initial fuel economy of the vehicle [km/kg] for a trip.
	 *
	 * @return
	 */
	public double getInitialFuelEconomy() {
		return spec.getInitialFuelEconomy();
	}

	/**
	 * Gets the estimated fuel economy of the vehicle [km/kg] for a trip.
	 *
	 * @return
	 */
	public double getEstimatedFuelEconomy() {
		double base = getBaseFuelEconomy();
		double cum = getCumFuelEconomy();
		double init = getInitialFuelEconomy();
		// Note: init < base always
		// Note: if cum < base, then trip is less economical more than expected
		// Note: if cum > base, then trip is more economical than expected
		if (cum == 0)
			return (.5 * base + .5 * init) * VehicleController.FUEL_ECONOMY_FACTOR;
		else {
			return (.3 * base + .3 * init + .4 * cum);
		}
	}

	/**
	 * Gets the initial fuel consumption of the vehicle [Wh/km] for a trip.
	 *
	 * @return
	 */
	public double getInitialFuelConsumption() {
		return spec.getInitialFuelConsumption();
	}
	
	/**
	 * Gets the estimated fuel consumption of the vehicle [Wh/km] for a trip.
	 *
	 * @return
	 */
	public double getEstimatedFuelConsumption() {
		double base = getBaseFuelConsumption();
		double cum = getCumFuelConsumption();
		double init = getInitialFuelConsumption();
		// Note: init > base always
		// Note: if cum > base, then vehicle consumes more than expected
		// Note: if cum < base, then vehicle consumes less than expected		
		if (cum == 0)
			return (.5 * base + .5 * init) / VehicleController.FUEL_ECONOMY_FACTOR;
		else {
			return (.3 * base + .3 * init + .4 * cum);
		}
	}
	
	/**
	 * Gets the number of battery modules of the vehicle.
	 *
	 * @return
	 */
	public int getBatteryModule() {
		return spec.getBatteryModule();
	}
	
	/**
	 * Gets the number of fuel cell stacks of the vehicle.
	 *
	 * @return
	 */
	public int getFuellCellStack() {
		return spec.getFuelCellStack();
	}
			
	/**
	 * Gets the drivetrain efficiency of the vehicle.
	 *
	 * @return drivetrain efficiency
	 */
	public double getDrivetrainEfficiency() {
		return spec.getDrivetrainEfficiency();
	}

	/**
	 * Returns total distance traveled by vehicle [km].
	 *
	 * @return the total distanced traveled by the vehicle [km]
	 */
	public double getOdometerMileage() {
		return odometerMileage;
	}

	/**
	 * Adds the distance traveled to vehicle's odometer (total distance traveled)
	 * and record the fuel used.
	 *
	 * @param distance the distance traveled traveled [km]
	 * @param cumEnergyUsed the energy used [Wh]
	 */
	public void addOdometerMileage(double distance, double cumEnergyUsed) {
		this.odometerMileage += distance;
		this.lastDistance = distance;
		this.cumEnergyUsedKWH += cumEnergyUsed/1000;
	}

	public double getLastDistanceTravelled() {
		return lastDistance;
	}
	
	/**
	 * Returns distance traveled by vehicle since last maintenance [km].
	 *
	 * @return distance traveled by vehicle since last maintenance [km]
	 */
	public double getDistanceLastMaintenance() {
		return distanceMaint;
	}

	/**
	 * Adds a distance to the vehicle's distance since last maintenance.
	 * Sets distanceMark to true if vehicle is due for maintenance.
	 *
	 * @param distance distance to add ([km]
	 */
	public void addDistanceLastMaintenance(double distance) {
		distanceMaint += distance;
		if ((distanceMaint > 5000D) && !distanceMark)
			distanceMark = true;
	}

	/** 
	 * Sets vehicle's distance since last maintenance to zero. 
	 */
	public void clearDistanceLastMaintenance() {
		distanceMaint = 0;
	}

	/**
	 * Returns direction of vehicle (0 = north, clockwise in radians).
	 *
	 * @return the direction the vehicle is traveling (in radians)
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * Sets the vehicle's facing direction (0 = north, clockwise in radians).
	 *
	 * @param direction the direction the vehicle is traveling (in radians)
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	/**
	 * Gets the instantaneous acceleration of the vehicle [m/s2].
	 * 
	 * @return
	 */
	public double getAccel() {
		return accel;
	}

	/**
	 * Sets the acceleration in [m/s2].
	 * 
	 * @param accel
	 */
	public void setAccel(double accel) {
		this.accel = accel;
	}
	
	/**
	 * Gets the allowable acceleration of the vehicle [m/s2].
	 * 
	 * @return
	 */
	public double getAllowedAccel() {
		if (speed <= 1)
			return getBaseAccel();
		return getBaseAccel() * getBeginningMass() / getMass();
//		return (baseAccel + Math.min(baseAccel, averagePower / getMass() / speed * 3600)) / 2.0;
	}
	
	/**
	 * Gets the base acceleration of the vehicle [m/s2].
	 * 
	 * @return
	 */
	public double getBaseAccel() {
		return spec.getBaseAccel();
	}
	
	public abstract double getTerrainGrade();

	public abstract double getElevation();

	/**
	 * Gets the operator of the vehicle (person or AI).
	 *
	 * @return the vehicle operator
	 */
	public Worker getOperator() {
		return vehicleOperator;
	}

	/**
	 * Sets the operator of the vehicle.
	 *
	 * @param vehicleOperator the vehicle operator
	 */
	public void setOperator(Worker vehicleOperator) {
		this.vehicleOperator = vehicleOperator;
		fireUnitUpdate(UnitEventType.OPERATOR_EVENT, vehicleOperator);
	}

	/**
	 * Returns the current settlement vehicle is parked at. Returns null if vehicle
	 * is not currently parked at a settlement.
	 *
	 * @return the settlement the vehicle is parked at
	 */
	@Override
	public Settlement getSettlement() {

		if (getContainerID() <= Unit.MARS_SURFACE_UNIT_ID)
			return null;

		Unit c = getContainerUnit();

		if (c.getUnitType() == UnitType.SETTLEMENT)
			return (Settlement) c;

		// If this unit is an LUV and it is within a rover
		if (c.getUnitType() == UnitType.VEHICLE)
			return ((Vehicle)c).getSettlement();

		return null;
	}

	/**
	 * Gets the garage building that the vehicle is at.
	 *
	 * @return {@link Vehicle}
	 */
	public Building getGarage() {
		Settlement settlement = getSettlement();
		if (settlement != null) {
			for (Building garageBuilding : settlement.getBuildingManager().getGarages()) {
				VehicleMaintenance garage = garageBuilding.getVehicleMaintenance();
				if (garage != null && garage.containsVehicle(this)) {
					return garageBuilding;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the unit's malfunction manager.
	 *
	 * @return malfunction manager
	 */
	public MalfunctionManager getMalfunctionManager() {
		return malfunctionManager;
	}

	/**
	 * Time passing for vehicle.
	 *
	 * @param pulse the amount of clock pulse passing (in millisols)
	 * @throws Exception if error during time.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		if (!isValid(pulse)) {
			return false;
		}

		if (primaryStatus == StatusType.MOVING) {
			// Assume the wear and tear factor is at 100% by being used in a mission
			malfunctionManager.activeTimePassing(pulse);
		}

		// If it's back at a settlement and is NOT in a garage
		else if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == getLocationStateType()
			&& !haveStatusType(StatusType.MAINTENANCE)) {
			int rand = RandomUtil.getRandomInt(3);
			// Assume the wear and tear factor is 75% less when not operating 
			if (rand == 3)
				malfunctionManager.activeTimePassing(pulse);
		}

		// Make sure reservedForMaintenance is false if vehicle needs no maintenance.
		if (haveStatusType(StatusType.MAINTENANCE) 
			&& malfunctionManager.getEffectiveTimeSinceLastMaintenance() <= 0D) {
			setReservedForMaintenance(false);
			removeSecondaryStatus(StatusType.MAINTENANCE);
		}
		else { // not under maintenance
			// Note: during maintenance, it doesn't need to be checking for malfunction.
			malfunctionManager.timePassing(pulse);
		}

		if (haveStatusType(StatusType.MALFUNCTION)
			&& malfunctionManager.getMalfunctions().size() == 0) {
				removeSecondaryStatus(StatusType.MALFUNCTION);
		}

		// Add the location to the trail if outside on a mission
		addToTrail(getCoordinates());
		// Check once per msol (millisol integer)
		if (pulse.isNewMSol()) {
			int count = 0;
			int sum = 0;

			for (int sol: roadSpeedHistory.getHistory().keySet()) {
				List<MSolDataItem<Integer>> speeds = roadSpeedHistory.getHistory().get(sol);
				for (MSolDataItem<Integer> s: speeds) {
					count++;
					sum += s.getData();
				}
			}
			
			if (count > 0 && sum > 0)
				averageRoadLoadSpeed = sum / count;
			
			count = 0;
			sum = 0;
			for (int sol: roadPowerHistory.getHistory().keySet()) {
				List<MSolDataItem<Integer>> speeds = roadPowerHistory.getHistory().get(sol);
				for (MSolDataItem<Integer> s: speeds) {
					count++;
					sum += s.getData();
				}
			}
			
			if (count > 0 && sum > 0)
				averageRoadLoadPower = sum / count;
		}
		
		return true;
	}

	/**
	 * Resets the vehicle reservation status.
	 */
	// public void correctVehicleReservation() {
	// 	if (isReservedMission
	// 		// Set reserved for mission to false if the vehicle is not associated with a
	// 		// mission.
	// 		&& missionManager.getMissionForVehicle(this) == null) {
	// 			logger.log(this, Level.FINE, 5000,
	// 					"Found reserved for an non-existing mission. Untagging it.");
	// 			setReservedForMission(false);
	// 	} else if (missionManager.getMissionForVehicle(this) != null) {
	// 			logger.log(this, Level.FINE, 5000,
	// 					"On a mission but not registered as mission reserved. Correcting it.");
	// 			setReservedForMission(true);
	// 	}
	// }

	/**
	 * Gets a collection of people affected by this entity.
	 *
	 * @return person collection
	 */
	public Collection<Person> getAffectedPeople() {
		Collection<Person> people = new HashSet<>();

		// Check all people.
		Iterator<Person> i = unitManager.getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			Task task = person.getMind().getTaskManager().getTask();

			// Add all people maintaining this vehicle.
			if ((task instanceof MaintainBuilding)
				&& this.equals(((MaintainBuilding) task).getEntity())) {
				people.add(person);
			}

			// Add all people repairing this vehicle.
			if ((task instanceof Repair)
				&& this.equals(((Repair) task).getEntity())) {
				people.add(person);
			}
		}

		return people;
	}

	/**
	 * Gets a collection of people who are available for social conversation in this
	 * vehicle.
	 *
	 * @return person collection
	 */
	public Collection<Person> getTalkingPeople() {
		Collection<Person> people = new HashSet<>();

		// Check all people.
		Iterator<Person> i = unitManager.getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			Task task = person.getMind().getTaskManager().getTask();

			// Add all people having conversation from all places as the task
			if (task instanceof Conversation)
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
		Collection<Robot> robots = new HashSet<>();

		// Check all robots.
		Iterator<Robot> i = unitManager.getRobots().iterator();
		while (i.hasNext()) {
			Robot robot = i.next();
			Task task = robot.getBotMind().getBotTaskManager().getTask();

			// Add all robots maintaining this vehicle.
			if (task instanceof MaintainBuilding) {
				if (((MaintainBuilding) task).getEntity() == this) {
					robots.add(robot);
				}
			}

			// Add all robots repairing this vehicle.
			if (task instanceof Repair) {
				if (((Repair) task).getEntity() == this) {
					robots.add(robot);
				}
			}
		}

		return robots;
	}

	/**
	 * Gets the vehicle's trail as a collection of coordinate locations.
	 *
	 * @return trail collection
	 */
	public Collection<Coordinates> getTrail() {
		return trail;
	}

	/**
	 * Adds a location to the vehicle's trail if appropriate.
	 *
	 * @param location location to be added to trail
	 */
	public void addToTrail(Coordinates location) {

		if (getSettlement() != null) {
			if (trail.size() > 0)
				trail.clear();
		} else if (trail.size() > 0) {
			Coordinates lastLocation = trail.get(trail.size() - 1);
			if (!lastLocation.equals(location) && (lastLocation.getDistance(location) >= 2D))
				trail.add(location);
		} else
			trail.add(location);
	}

	/**
	 * Gets the resource type id that this vehicle uses for fuel.
	 *
	 * @return resource type id
	 */
	public int getFuelTypeID() {
		return spec.getFuelType();
	}
	
	/**
	 * Gets the fuel type of this vehicle.
	 *
	 * @return fuel type string
	 */
	public String getFuelTypeStr() {
		return spec.getFuelTypeStr();
	}
	
	/**
	 * Gets the estimated distance traveled in one sol.
	 *
	 * @return distance traveled (km)
	 */
	public double getEstimatedTravelDistancePerSol() {
		// Return estimated average speed in km / sol.
		return getBaseSpeed() * VehicleSpec.ESTIMATED_TRAVEL_HOURS_PER_SOL;
	}

	/**
	 * Checks if the vehicle's emergency beacon is turned on.
	 *
	 * @return true if beacon is on.
	 */
	public boolean isBeaconOn() {
		return emergencyBeacon;
	}

	/**
	 * Sets the vehicle's emergency beacon on or off.
	 *
	 * @param state true if beacon is to be on.
	 */
	public void setEmergencyBeacon(boolean state) {
		if (emergencyBeacon != state) {
			emergencyBeacon = state;
			fireUnitUpdate(UnitEventType.EMERGENCY_BEACON_EVENT);
		}
	}

	/**
	 * Checks if the item is salvaged.
	 *
	 * @return true if salvaged.
	 */
	public boolean isSalvaged() {
		return isSalvaged;
	}

	/**
	 * Indicates the start of a salvage process on the item.
	 *
	 * @param info       the salvage process info.
	 * @param settlement the settlement where the salvage is taking place.
	 */
	public void startSalvage(SalvageProcessInfo info, int settlement) {
		salvageInfo = new SalvageInfo(this, info, settlement);
		isSalvaged = true;
	}

	/**
	 * Gets the salvage info.
	 *
	 * @return salvage info or null if item not salvaged.
	 */
	public SalvageInfo getSalvageInfo() {
		return salvageInfo;
	}

	/**
	 * Sets initial parked location and facing at settlement.
	 */
	public abstract void findNewParkingLoc();

	public void relocateVehicle() {
		if (isInAGarage()) {
			BuildingManager.removeFromGarage(this);
		}
		else {
			findNewParkingLoc();
		}
	}

	public static double getFuelRangeErrorMargin() {
		return fuel_range_error_margin;
	}

	public static double getLifeSupportRangeErrorMargin() {
		return life_support_range_error_margin;
	}

	public int getAssociatedSettlementID() {
		return associatedSettlementID;
	}

	/**
	 * Gets the settlement the person is currently associated with.
	 *
	 * @return associated settlement or null if none.
	 */
	@Override
	public Settlement getAssociatedSettlement() {
		return unitManager.getSettlementByID(associatedSettlementID);
	}

	/**
	 * Is the vehicle outside of a settlement but within its vicinity ?
	 *
	 * @return
	 */
	public boolean isRightOutsideSettlement() {
        return getLocationStateType() == LocationStateType.WITHIN_SETTLEMENT_VICINITY;
	}


	@Override
	public Building getBuildingLocation() {
		return getGarage();
	}

	/**
	 * Checks if this vehicle is involved in a mission.
	 */
	public Mission getMission() {
		return mission;
	}

	public void setMission(Mission newMission) {
		this.mission = newMission;
	}

	/**
	 * Checks if this vehicle not in a settlement and is outside on a mission on the surface of Mars.
	 *
	 * @return true if yes
	 */
	public boolean isOutsideOnMarsMission() {
		return LocationStateType.MARS_SURFACE == currentStateType;
	}

	/**
	 * Checks if the person is in a moving vehicle.
	 *
	 * @param person the person.
	 * @return true if person is in a moving vehicle.
	 */
	public static boolean inMovingRover(Person person) {

		boolean result = false;

		if (person.isInVehicle()) {
			Vehicle vehicle = person.getVehicle();
			result = vehicle.getPrimaryStatus() == StatusType.MOVING;
		}

		return result;
	}

	/**
	 * Gets the specific base wear life time of this vehicle (in msols).
	 *
	 * @return
	 */
	public double getBaseWearLifetime() {
		return baseWearLifetime;
	}

	@Override
	public UnitType getUnitType() {
		return UnitType.VEHICLE;
	}

	/**
	 * Gets the holder's unit instance.
	 *
	 * @return the holder's unit instance
	 */
	@Override
	public Unit getHolder() {
		return this;
	}

	/**
	 * Is this unit empty ?
	 *
	 * @return true if this unit doesn't carry any resources or equipment
	 */
	public boolean isEmpty() {
		return (eqmInventory.getStoredMass() == 0D);
	}

	/**
	 * Gets the total mass on this vehicle (not including vehicle's weight).
	 *
	 * @return
	 */
	@Override
	public double getStoredMass() {
		return eqmInventory.getStoredMass();
	}

	/**
	 * Gets the equipment list.
	 *
	 * @return
	 */
	@Override
	public Set<Equipment> getEquipmentSet() {
		if (eqmInventory == null)
			return new HashSet<>();
		return eqmInventory.getEquipmentSet();
	}

	/**
	 * Finds all of the containers (excluding EVA suit).
	 *
	 * @return collection of containers or empty collection if none.
	 */
	@Override
	public Collection<Container> findAllContainers() {
		return eqmInventory.findAllContainers();
	}

	/**
	 * Finds all of the containers of a particular type (excluding EVA suit).
	 *
	 * @return collection of containers or empty collection if none.
	 */
	@Override
	public Collection<Container> findContainersOfType(EquipmentType type){
		return eqmInventory.findContainersOfType(type);
	}

	/**
	 * Does this unit possess an equipment of this type ?
	 *
	 * @param typeID
	 * @return
	 */
	@Override
	public boolean containsEquipment(EquipmentType type) {
		return eqmInventory.containsEquipment(type);
	}

	/**
	 * Adds an equipment to this unit.
	 *
	 * @param equipment
	 * @return true if it can be carried
	 */
	@Override
	public boolean addEquipment(Equipment e) {
		if (eqmInventory.addEquipment(e)) {
			e.setContainerUnit(this);
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_EQUIPMENT_EVENT, this);
			return true;
		}
		return false;
	}

	/**
	 * Removes an equipment.
	 *
	 * @param equipment
	 */
	@Override
	public boolean removeEquipment(Equipment equipment) {
		return eqmInventory.removeEquipment(equipment);
	}

	/**
	 * Stores the item resource.
	 *
	 * @param resource the item resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public int storeItemResource(int resource, int quantity) {
		return eqmInventory.storeItemResource(resource, quantity);
	}

	/**
	 * Retrieves the item resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	@Override
	public int retrieveItemResource(int resource, int quantity) {
		return eqmInventory.retrieveItemResource(resource, quantity);
	}

	/**
	 * Retrieves the resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	@Override
	public double retrieveAmountResource(int resource, double quantity) {
		return eqmInventory.retrieveAmountResource(resource, quantity);
	}

	/**
	 * Stores the resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public double storeAmountResource(int resource, double quantity) {
		return eqmInventory.storeAmountResource(resource, quantity);
	}

	/**
	 * Gets the item resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceStored(int resource) {
		return eqmInventory.getItemResourceStored(resource);
	}

	/**
	 * Gets the capacity of a particular amount resource.
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getAmountResourceCapacity(int resource) {
		return eqmInventory.getAmountResourceCapacity(resource);
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceRemainingCapacity(int resource) {
		return eqmInventory.getAmountResourceRemainingCapacity(resource);
	}

	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasAmountResourceRemainingCapacity(int resource) {
		return eqmInventory. hasAmountResourceRemainingCapacity(resource);
	}
	
	/**
     * Gets the total capacity that it can hold.
     *
     * @return total capacity (kg).
     */
	@Override
	public double getCargoCapacity() {
		return eqmInventory.getCargoCapacity();
	}

	/**
	 * Gets the amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceStored(int resource) {
		return eqmInventory.getAmountResourceStored(resource);
	}
	/**
	 * Gets all the amount resource resource stored, including inside equipment.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAllAmountResourceStored(int resource) {
		return eqmInventory.getAllAmountResourceStored(resource);
	}
	
	/**
	 * Finds the number of empty containers of a class that are contained in storage and have
	 * an empty inventory.
	 *
	 * @param containerClass  the unit class.
	 * @param brandNew  does it include brand new bag only
	 * @return number of empty containers.
	 */
	@Override
	public int findNumEmptyContainersOfType(EquipmentType containerType, boolean brandNew) {
		return eqmInventory.findNumEmptyContainersOfType(containerType, brandNew);
	}

	/**
	 * Finds the number of containers of a particular type
	 *
	 * @param containerType the equipment type.
	 * @return number of empty containers.
	 */
	@Override
	public int findNumContainersOfType(EquipmentType containerType) {
		return eqmInventory.findNumContainersOfType(containerType);
	}

	/**
	 * Finds a container in storage.
	 *
	 * @param containerType
	 * @param empty does it need to be empty ?
	 * @param resource If -1 then resource doesn't matter
	 * @return instance of container or null if none.
	 */
	@Override
	public Container findContainer(EquipmentType containerType, boolean empty, int resource) {
		return eqmInventory.findContainer(containerType, empty, resource);
	}


	/**
	 * Finds the number of EVA suits (may or may not have resources inside) that are contained in storage.
	 *
	 * @return number of EVA suits
	 */
	public int findNumEVASuits() {
		int result = 0;
		for (Equipment e : eqmInventory.getEquipmentSet()) {
			if (e.getEquipmentType() == EquipmentType.EVA_SUIT) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Gets a set of item resources in storage.
	 * 
	 * @return  a set of resources
	 */
	@Override
	public Set<Integer> getItemResourceIDs() {
		return eqmInventory.getItemResourceIDs();
	}

	/**
	 * Gets a set of resources in storage.
	 * 
	 * @return  a set of resources
	 */
	@Override
	public Set<Integer> getAmountResourceIDs() {
		return eqmInventory.getAmountResourceIDs();
	}
	/**
	 * Gets all stored amount resources in eqmInventory, including inside equipment
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAllAmountResourceIDs() {
		return eqmInventory.getAllAmountResourceIDs();
	}
	
	/**
	 * Obtains the remaining general storage space
	 *
	 * @return quantity
	 */
	@Override
	public double getRemainingCargoCapacity() {
		return eqmInventory.getRemainingCargoCapacity();
	}

	/**
	 * Does it have this item resource ?
	 *
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasItemResource(int resource) {
		return eqmInventory.hasItemResource(resource);
	}

	/**
	 * Gets the remaining quantity of an item resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceRemainingQuantity(int resource) {
		return eqmInventory.getItemResourceRemainingQuantity(resource);
	}

	/**
	 * Sets the unit's container unit.
	 *
	 * @param newContainer the unit to contain this unit.
	 */
	@Override
	public void setContainerUnit(Unit newContainer) {
		if (newContainer != null) {
			if (newContainer.equals(getContainerUnit())) {
				return;
			}
			// 1. Set Coordinates
			if (newContainer.getUnitType() == UnitType.MARS) {
				// Since it's on the surface of Mars,
				// First set its initial location to its old parent's location as it's leaving its parent.
				// Later it may move around and updates its coordinates by itself
				setCoordinates(getContainerUnit().getCoordinates());
			}
			else {
				// Null its coordinates since it's now slaved after its parent
				setNullCoordinates();
			}
			// 2. Set new LocationStateType
			updateVehicleState(newContainer);
			// 3. Set containerID
			setContainerID(newContainer.getIdentifier());
			// 4. Fire the container unit event
			fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
		}
	}

	/**
	 * Updates the location state type of a vehicle.
	 *
	 * @apiNote (1) : WITHIN_SETTLEMENT_VICINITY is the intermediate state between being INSIDE_SETTLEMENT (in a garage) and being OUTSIDE_ON_MARS.
	 *
	 * @apiNote (2) : WITHIN_SETTLEMENT_VICINITY can be used by a person or a vehicle.
	 *
	 * @apiNote (3) : If a vehicle may be in a garage inside a building, this vehicle is INSIDE_SETTLEMENT.
	 *                If a vehicle is parked right outside a settlement, this vehicle is WITHIN_SETTLEMENT_VICINITY.
	 *
	 * @param newContainer
	 */
	public void updateVehicleState(Unit newContainer) {
		if (newContainer == null) {
			currentStateType = LocationStateType.UNKNOWN;
			return;
		}

		currentStateType = getNewLocationState(newContainer);
	}

	/**
	 * Updates the location state type directly.
	 *
	 * @param type
	 */
	public void updateLocationStateType(LocationStateType type) {
		currentStateType = type;
	}

	/**
	 * Gets the location state type based on the type of the new container unit.
	 *
	 * @param newContainer
	 * @return {@link LocationStateType}
	 */
	@Override
	public LocationStateType getNewLocationState(Unit newContainer) {

		if (newContainer.getUnitType() == UnitType.SETTLEMENT) {
			if (isInAGarage()) {
				return LocationStateType.INSIDE_SETTLEMENT;
			}
			else
				return LocationStateType.WITHIN_SETTLEMENT_VICINITY;
		}

//		if (newContainer.getUnitType() == UnitType.BUILDING)
//			return LocationStateType.INSIDE_SETTLEMENT;

		if (newContainer.getUnitType() == UnitType.VEHICLE)
			return LocationStateType.INSIDE_VEHICLE;

		if (newContainer.getUnitType() == UnitType.CONSTRUCTION)
			return LocationStateType.MARS_SURFACE;

		if (newContainer.getUnitType() == UnitType.PERSON)
			return LocationStateType.ON_PERSON_OR_ROBOT;

		if (newContainer.getUnitType() == UnitType.MARS)
			return LocationStateType.MARS_SURFACE;

		return null;
	}

	/**
	 * Is this unit inside a settlement ?
	 *
	 * @return true if the unit is inside a settlement
	 */
	@Override
	public boolean isInSettlement() {

		if (containerID <= MARS_SURFACE_UNIT_ID)
			return false;

		// if the vehicle is parked in a garage
		if (LocationStateType.INSIDE_SETTLEMENT == currentStateType)
			return true;

		// Note: in future, WITHIN_SETTLEMENT_VICINITY will
		// mean that the vehicle is NOT in the settlement.
		// But for now, it loosely means the vehicle is still in the settlement.

		// if the vehicle is parked in the vicinity of a settlement
		if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == currentStateType)
			return true;

		if (getContainerUnit().getUnitType() == UnitType.SETTLEMENT
				&& ((Settlement)(getContainerUnit())).containsParkedVehicle((Vehicle)this)) {
			return true;
		}

		return false;
	}

	/**
	 * Transfers the unit from one owner to another owner.
	 *
	 * @param origin {@link Unit} the original container unit
	 * @param destination {@link Unit} the destination container unit
	 */
	public boolean transfer(Unit destination) {
		boolean transferred = false;
		// Set the old container unit
		Unit cu = getContainerUnit();

		if (cu.getUnitType() == UnitType.MARS) {
			transferred = ((MarsSurface)cu).removeVehicle(this);
		}
		else if (cu.getUnitType() == UnitType.SETTLEMENT) {
			Settlement currentBase = (Settlement)cu;
			transferred = currentBase.removeParkedVehicle(this);
			this.setCoordinates(currentBase.getCoordinates());
		}

		if (transferred) {
			if (destination.getUnitType() == UnitType.MARS) {
				transferred = ((MarsSurface)destination).addVehicle(this);
			}
			else if (cu.getUnitType() == UnitType.SETTLEMENT) {
				transferred = ((Settlement)destination).addParkedVehicle(this);
			}

			if (!transferred) {
				logger.warning(this + " cannot be stored into " + destination + ".");
				// NOTE: need to revert back the storage action
			}
			else {
				// Set the new container unit (which will internally set the container unit id)
				setContainerUnit(destination);
				// Fire the unit event type
				getContainerUnit().fireUnitUpdate(UnitEventType.INVENTORY_STORING_UNIT_EVENT, this);
				// Fire the unit event type
				getContainerUnit().fireUnitUpdate(UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT, this);
			}
		}
		else {
			logger.warning(this + " cannot be retrieved from " + cu + ".");
			// NOTE: need to revert back the retrieval action
		}

		return transferred;
	}

    /**
	 * Gets the amount of fuel (kg) needed for a trip of a given distance (km).
	 *
	 * @param tripDistance   the distance (km) of the trip.
	 * @param useMargin      Apply safety margin when loading resources before embarking if true.
	 * @return amount of fuel needed for trip (kg)
	 */
	public double getFuelNeededForTrip(double tripDistance, boolean useMargin) {
		return vehicleController.getFuelNeededForTrip(this, tripDistance, 
				getEstimatedFuelEconomy(), useMargin);
	}
	
	public EquipmentInventory getEquipmentInventory() {
		return eqmInventory;
	}

	public VehicleController getController() {
		return vehicleController;
	}
	
	/** 
	 * Gets the VehicleSpec instance. 
	 */
	public VehicleSpec getVehicleSpec() {
		return spec;
	}
	
	
	/**
	 * Compares if an object is the same as this unit
	 *
	 * @param obj
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Vehicle v = (Vehicle) obj;
		return this.getIdentifier() == v.getIdentifier();
	}

	/**
	 * Gets the hash code value.
	 *
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		return getIdentifier() % 32;
	}

	@Override
	public void destroy() {
		super.destroy();

		malfunctionManager.destroy();
		malfunctionManager = null;
		direction = null;
		vehicleOperator = null;
		trail.clear();
		trail = null;
		towingVehicle = null;
		statusTypes.clear();
		statusTypes = null;
		if (salvageInfo != null)
			salvageInfo.destroy();
		salvageInfo = null;
	}
}