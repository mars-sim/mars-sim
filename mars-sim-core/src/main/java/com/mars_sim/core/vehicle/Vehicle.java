/*
 * Mars Simulation Project
 * Vehicle.java
 * @date 2025-07-21
 * @author Scott Davis
 */
package com.mars_sim.core.vehicle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.function.SystemType;
import com.mars_sim.core.building.function.VehicleMaintenance;
import com.mars_sim.core.building.task.MaintainBuilding;
import com.mars_sim.core.data.History;
import com.mars_sim.core.data.MSolDataLogger;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentInventory;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.equipment.ItemHolder;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.malfunction.task.Repair;
import com.mars_sim.core.manufacture.Salvagable;
import com.mars_sim.core.manufacture.SalvageInfo;
import com.mars_sim.core.manufacture.SalvageProcessInfo;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.Converse;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.health.RadiationExposure;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.resource.SuppliesManifest;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.RadiationStatus;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.unit.AbstractMobileUnit;
import com.mars_sim.core.unit.MobileUnit;
import com.mars_sim.core.unit.UnitHolder;
import com.mars_sim.core.vehicle.task.LoadingController;

/**
 * The Vehicle class represents a generic vehicle. It keeps track of generic
 * information about the vehicle. This class needs to be subclassed to represent
 * a specific type of vehicle.
 */
public abstract class Vehicle extends AbstractMobileUnit
		implements Malfunctionable, Salvagable, Temporal,
		LocalBoundedObject, EquipmentOwner, ItemHolder, UnitHolder, Towed {

	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(Vehicle.class.getName());
	
	private static final int MAX_NUM_SOLS = 14;
	
	private static final double MAXIMUM_RANGE = 10_000;
	
    private static final double VEHICLE_CLEARANCE_0 = 1.4;
    private static final double VEHICLE_CLEARANCE_1 = 2.8;
    
	/** The error margin for determining vehicle range. (Actual distance / Safe distance). */
	private static double fuelRangeErrorMargin;
	private static double lifeSupportRangeErrorMargin;

	private static final String IMMINENT = " be imminent.";
	private static final String DETECTOR = "The radiation detector just forecasted a ";

	/** The types of status types that make a vehicle unavailable for us. */
	private static final List<StatusType> UNAVAILABLE_STATUS = Arrays.asList(
			StatusType.MAINTENANCE,
			StatusType.TOWED,
			StatusType.MOVING,
			StatusType.STUCK,
			StatusType.MALFUNCTION,
			StatusType.LOADING,
			StatusType.UNLOADING,
			StatusType.HOVERING,
			StatusType.OUT_OF_BATTERY_POWER,
			StatusType.OUT_OF_FUEL,
			StatusType.OUT_OF_OXIDIZER,	
			StatusType.TOWING
			);

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
	/** True if vehicle is charging. */
	private boolean isCharging;
	/** True if vehicle is ready to be drawn on the map. */
	private boolean isReady = false;
	
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
	private double cumFuelUsedKG;
	/** The cumulative energy usage of the vehicle [kWh] */
	private double cumEnergyUsedKWH;
	/** The instantaneous fuel economy of the vehicle [km/kg]. */
	private double iFuelEconomy;
	/** The instantaneous fuel consumption of the vehicle [Wh/km]. */
	private double iFuelConsumption;
	
	/** The vehicle specification */
	private String specName;
	/** The vehicle model */
	private String modelName;

	/** The radiation status instance that capture if the settlement has been exposed to a radiation event. */
	private RadiationStatus exposed = RadiationStatus.calculateChance(0D);

	/** The vehicle type. */
	protected VehicleType vehicleType;
	/** The primary status type. */
	private StatusType primaryStatus;

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
	/** The mission instance. */
	private Mission mission;

	/** A collection of locations that make up the vehicle's trail. */
	private List<Coordinates> trail;
	/** List of operator activity spots. */
	private List<LocalPosition> operatorActivitySpots;
	/** List of passenger activity spots. */
	private List<LocalPosition> passengerActivitySpots;
	/** List of status types. */
	private Set<StatusType> statusTypes = new HashSet<>();
	
	/** The vehicle's status log. */
	private History<Set<StatusType>> vehicleLog = new History<>(40);
	/** The vehicle's road speed history. */
	private MSolDataLogger<Integer> roadSpeedHistory = new MSolDataLogger<>(MAX_NUM_SOLS);
	/** The vehicle's road power history. */	
	private MSolDataLogger<Integer> roadPowerHistory = new MSolDataLogger<>(MAX_NUM_SOLS);

	private LoadingController loadingController;
	
	/**
	 * Set up the internal flags for the vehicle.
	 */
	public static void initializeInstances(SimulationConfig simulationConfig) {
		lifeSupportRangeErrorMargin = simulationConfig.getSettlementConfiguration()
				.getRoverValues()[0];
		fuelRangeErrorMargin = simulationConfig.getSettlementConfiguration().getRoverValues()[1];
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
		super(name, settlement);
		setLocationStateType(LocationStateType.SETTLEMENT_VICINITY);
		
		this.spec = spec;
		this.specName = spec.getName();
		this.modelName = spec.getModelName();
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
		
		baseWearLifetime = spec.getWearLifetime();

		// Initialize malfunction manager.
		malfunctionManager = new MalfunctionManager(this, baseWearLifetime, maintenanceWorkTime);

		setupScopeString();

		primaryStatus = StatusType.PARKED;
		
		writeLog();

		// Instantiate the motor controller
		vehicleController = new VehicleController(this);

		// Initialize operator activity spots.
		operatorActivitySpots = spec.getOperatorActivitySpots();

		// Initialize passenger activity spots.
		passengerActivitySpots = spec.getPassengerActivitySpots();
		
		isReady = true;
	}

	/**
	 * Is the vehicle ready to be drawn on the settlement map ?
	 */
	public boolean isReady() {
		return isReady;
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
	 * @return Name of base image for this vehicle
	 */
	public String getBaseImage() {
		return spec.getBaseImage();
	}
	
	/**
	 * Gets the spec name of the vehicle.
	 * 
	 * @return spec name
	 */
	public String getSpecName() {
		return specName;
	}
	
	/**
	 * Gets the model of the vehicle.
	 * 
	 * @return Name of the model
	 */
	public String getModelName() {
		return modelName;
	}

	/**
	 * Gets the vehicle type.
	 * 
	 * @return
	 */
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
	public double getFacing() {
		return facingParked;
	}

	/**
	 * Get the loading plan associated with this Vehicle
	 */
	public LoadingController getLoadingPlan() {
		return loadingController;
	}

	/**
	 * Changes the loading status of this loading.
	 * 
	 * @param manifest Supplies to load; if this is null then stop the loading
	 */
    public LoadingController setLoading(SuppliesManifest manifest) {
		if (manifest == null) {
			removeSecondaryStatus(StatusType.LOADING);
			loadingController = null;
		}
        else if (statusTypes.contains(StatusType.LOADING)) {
			logger.warning(this, "Already in loading status");
		}
		else {
			removeSecondaryStatus(StatusType.UNLOADING);
			loadingController = new LoadingController(getSettlement(), this, manifest);
			addSecondaryStatus(StatusType.LOADING);
		}
		return loadingController;
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
		setPosition(position);
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
	public void setParkedFlyerLocation(LocalPosition position, double facing) {
		// Set new parked location for the flyer.
		setPosition(position);
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
		if (this instanceof Crewable crewable) {
			result = new HashMap<>(crewable.getCrewNum());
            for (Person crewmember : crewable.getCrew()) {
                LocalPosition crewPos = LocalAreaUtil.convert2LocalPos(crewmember.getPosition(), this);
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
		if (this instanceof Crewable crewable) {
			result = new HashMap<>(crewable.getRobotCrewNum());
            for (Robot robotCrewmember : crewable.getRobotCrew()) {
                LocalPosition crewPos = LocalAreaUtil.convert2LocalPos(robotCrewmember.getPosition(), this);
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
		if (this instanceof Crewable crewable) {
            for (Person crewmember : crewable.getCrew()) {
                LocalPosition currentCrewPos = currentCrewPositions.get(crewmember);
                LocalPosition settlementLoc = LocalAreaUtil.convert2SettlementPos(currentCrewPos,
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
		if (this instanceof Crewable crewable) {
            for (Robot robotCrewmember : crewable.getRobotCrew()) {
                LocalPosition currentCrewPos = currentRobotCrewPositions.get(robotCrewmember);
                LocalPosition settlementLoc = LocalAreaUtil.convert2SettlementPos(currentCrewPos,
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
        return (primaryStatus == status) || statusTypes.contains(status);
    }

	/**
	 * Checks if this vehicle has no issues and is ready for mission.
	 *
	 * @return yes if it has anyone of the bad status types
	 */
	public boolean isVehicleReady() {
	    for (StatusType st : UNAVAILABLE_STATUS) {
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
	public boolean isInGarage() {
		Settlement settlement = getSettlement();
		return settlement != null && settlement.getBuildingManager().isInGarage(this);
	}

	/**
	 * Adds the vehicle to a garage.
	 *
	 * @return true if successful.
	 */
	public boolean addToAGarage() {
		return getSettlement().getBuildingManager().addToGarageBuilding(this) != null;
	}
	
	/**
	 * Records the status in the vehicle log.
	 */
	private void writeLog() {
		Set<StatusType> entry = new HashSet<>(statusTypes);
		entry.add(primaryStatus);
		vehicleLog.add(entry);
	}

	/**
	 * Gets the vehicle log.
	 *
	 * @return List of changes ot the status
	 */
	public History<Set<StatusType>> getVehicleLog() {
		return vehicleLog;
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
	 * Gets the average base power of the vehicle when operating [kW].
	 * 
	 * @return
	 */
	public double getBasePower() {
		return spec.getBasePower();
	}
	
	/**
	 * Gets the speed of vehicle.
	 *
	 * @return the vehicle's speed (in kph)
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * Sets the vehicle's current speed.
	 *
	 * @param speed the vehicle's speed (in kph)
	 */
	public void setSpeed(double speed) {
		if (speed < 0D)
			throw new IllegalArgumentException("Vehicle speed cannot be less than 0 km/hr: " + speed);
		if (Double.isNaN(speed))
			throw new IllegalArgumentException("Vehicle speed is a NaN");

		boolean hasSpeedChanged = speed != this.speed;
		if (hasSpeedChanged) {
			if (speed == 0D) {
				if (this instanceof Drone d) {
					if (d.getHoveringHeight() > 0) {
						setPrimaryStatus(StatusType.HOVERING);
					}
					else {
						setPrimaryStatus(StatusType.PARKED);
					}
				}
				else
					setPrimaryStatus(StatusType.PARKED);
			} 
			
			else if (this.speed == 0D || speed > 0) {
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
	 * @return the vehicle's base speed (in kph)
	 */
	public double getBaseSpeed() {
		return spec.getBaseSpeed();
	}

	/**
	 * Gets the estimated fuel range of the vehicle.
	 *
	 * @return the estimated fuel range of the vehicle (in km)
	 */
	public double getEstimatedRange() {
		double mass = getMass();
		double bMass = getBeginningMass();
		double massFactor = (mass + bMass) / 2 / bMass;

		// Before the mission is created, the range would be based on vehicle's fuel capacity
		return (getBaseRange() + getEstimatedFuelEconomy() * getFuelCapacity() * massFactor) / 2;
	}
	
	/**
	 * Gets the current fuel range of the vehicle.
	 *
	 * @return the current fuel range of the vehicle (in km)
	 */
	public double getRange() {
		// Question: does it account for the return trip ?
		double range;
		Mission mission = getMission();

        if ((mission == null) || (mission.getStage() == Stage.PREPARATION)) {
        	// Before the mission is created, the range would be based on vehicle's capacity
        	range = getEstimatedRange();
        }
        else {
        	
    		int fuelTypeID = getFuelTypeID();
    		if (fuelTypeID < 0) {
    			range = MAXIMUM_RANGE;
    		}
    		else {
                double amountOfFuel = getSpecificAmountResourceStored(fuelTypeID);
            	// During the journey, the range would be based on the amount of fuel in the vehicle
        		range = getEstimatedFuelEconomy() * amountOfFuel;
    		}
        }
        return (int) range;
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
		roadSpeedHistory.addDataPoint(value);
	}
	
	/**
	 * Sets the average road load power of the vehicle [kW].
	 * 
	 * @return
	 */
	public void setAverageRoadLoadPower(int value) {
		roadPowerHistory.addDataPoint(value);
	}
	
	/**
	 * Gets the average road load power of the vehicle [kph].
	 * 
	 * @return
	 */
	public double getRoadPowerHistoryAverage() {
		return roadPowerHistory.getAverageDouble();
	}
	
	/**
	 * Gets the average road speed power of the vehicle [kph].
	 * 
	 * @return
	 */
	public double getRoadSpeedHistoryAverage() {
		return roadSpeedHistory.getAverageDouble();
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
	 * Gets the cumulative fuel usage of the vehicle [kg].
	 * 
	 * @return
	 */
	public double getCumFuelUsage() {
		return cumFuelUsedKG;
	}
	
	/**
	 * Gets the total fuel energy available at the full tank [kWh].
	 *
	 * @return
	 */
	public double getFullTankFuelEnergyCapacity() {
		return spec.getFullTankFuelEnergyCapacity();
	}

	/**
	 * Gets the estimated energy available for the drivetrain [kWh].
	 *
	 * @return
	 */
	public double getDrivetrainEnergy() {
		return spec.getDrivetrainFuelEnergy();
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
		if (odometerMileage == 0 || (cumFuelUsedKG == 0 && cumEnergyUsedKWH == 0))
			return 0;
		// kg = kWh / (Wh / kg)
		// Note: This battery has 1 kWh/kg rating
		double batteryFuelKG = cumEnergyUsedKWH * 1;
		// [km] / [kg] 
		return odometerMileage / (cumFuelUsedKG + batteryFuelKG);
	}
	
	/**
	 * Gets the cumulative fuel consumption [Wh/km].
	 * 
	 * @return
	 */
	public double getCumFuelConsumption() {
		if (odometerMileage == 0 || (cumFuelUsedKG == 0 && cumEnergyUsedKWH == 0))
			return 0;
		// Wh = kg * Wh / kg
		double fuelWh = cumFuelUsedKG * getVehicleSpec().getFuel2DriveEnergy();
		// Wh  / km
		return (fuelWh + cumEnergyUsedKWH * 1000) / odometerMileage;
	}

	/**
	 * Gets the coefficient for converting estimated FC to estimated FE.
	 * 
	 * @return
	 */
	public double getCoeffEstFC2FE() {
		double estFE = getEstimatedFuelEconomy();
		double estFC = getEstimatedFuelConsumption();
		
		if (estFE > 0 && estFC > 0)
			// [km / kg]  / [Wh / km]  
			// km / kg / Wh * km 
			return estFE / estFC;

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
	 * Sets the instantaneous fuel consumption of the vehicle [Wh/km].
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
	 * Gets the stored mass plus the base mass.
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
		// Needs to be dropped when new Mission logic rolled out
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
			return (.4 * base + .6 * init) / VehicleController.FUEL_ECONOMY_FACTOR;
		else {
			return (.2 * base + .3 * init + .5 * cum);
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
			return (.4 * base + .6 * init) * VehicleController.FUEL_CONSUMPTION_FACTOR;
		else {
			return (.2 * base + .3 * init + .5 * cum);
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
	 * Gets the total battery capacity of the vehicle.
	 *
	 * @return
	 */
	public double getBatteryCapacity() {
		return spec.getBatteryCapacity();
	}
	
	/**
	 * Gets the percent of remaining battery energy of the vehicle.
	 *
	 * @return
	 */
	public double getBatteryPercent() {
		return getController().getBattery().getBatteryLevel();
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
	 * @param cumFuelUsedKG the fuel used [kg]
	 */
	public void addOdometerMileage(double distance, double cumEnergyUsed, double cumFuelUsedKG) {
		this.odometerMileage += distance;
		this.lastDistance = distance;
		this.cumEnergyUsedKWH += cumEnergyUsed/1000;
		this.cumFuelUsedKG += cumFuelUsedKG;
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

		var c = getContainerUnit();

		if (c instanceof Settlement s)
			return s;

		// If this unit is an LUV and it is within a rover
		if (c instanceof MobileUnit mu)
			return mu.getSettlement();

		return null;
	}

	/**
	 * Gets the garage building that the vehicle is at.
	 *
	 * @return {@link Building}
	 */
	public Building getGarage() {
		Settlement settlement = getSettlement();
		if (settlement == null)
			return null;
		
		for (Building garageBuilding : settlement.getBuildingManager().getGarages()) {
			VehicleMaintenance garage = garageBuilding.getVehicleMaintenance();
			if (garage != null) {
				
				if (this instanceof Rover rover && garage.containsRover(rover)) {
					return garageBuilding;
				}
				else if (this instanceof Flyer flyer && garage.containsFlyer(flyer)) {
					return garageBuilding;
				}
				else if (this instanceof LightUtilityVehicle luv && garage.containsUtilityVehicle(luv)) {
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

		if (isCharging && getContainerUnit() instanceof Settlement settlement) {
			chargeVehicle(pulse, settlement);
		}
		
		// If it's outside and moving
		else if (primaryStatus == StatusType.MOVING) {
			// Assume the wear and tear factor is at 100% by being used in a mission
			malfunctionManager.activeTimePassing(pulse);
			// Add the location to the trail if outside on a mission
			addToTrail(getCoordinates());
		}

		// Regardless being outside or inside settlement,
		// NOT under maintenance
		else {
			
			if (!haveStatusType(StatusType.GARAGED)) {
				int rand = RandomUtil.getRandomInt(3);
				// Assume the wear and tear factor is 75% less when not operating 
				if (rand == 3)
					malfunctionManager.activeTimePassing(pulse);
			}
			
			else {
				// Note: during maintenance, it doesn't need to be checking for malfunction.
				malfunctionManager.timePassing(pulse);
			}
			
		}

		// Background loading check
		if (haveStatusType(StatusType.LOADING) && isInSettlement()
				&& !loadingController.isCompleted()) {
			double time = pulse.getElapsed();
			double transferSpeed = 10; // Assume 10 kg per msol
			double amountLoading = time * transferSpeed;

			loadingController.backgroundLoad(amountLoading);
		}
		
		// Check once per msol (millisol integer)
		if (pulse.isNewIntMillisol()) {
					
			if (haveStatusType(StatusType.MALFUNCTION)
					&& malfunctionManager.getMalfunctions().isEmpty()) {
				// Regardless being outside or inside settlement,
				// if it's malfunction (outside or inside settlement) 
				// whether it's in a garage or not
				
				// Remove the malfunction status
				removeSecondaryStatus(StatusType.MALFUNCTION);
			}
			else if (haveStatusType(StatusType.UNLOADING)
					&& isEmpty() && !isReservedMission) {
				removeSecondaryStatus(StatusType.UNLOADING);	
			}
			
			// Sample a data point every SAMPLE_FREQ (in msols)
			int msol = pulse.getMarsTime().getMillisolInt();

			// Check every RADIATION_CHECK_FREQ (in millisols)
			// Compute whether a baseline, GCR, or SEP event has occurred
			int remainder = msol % RadiationExposure.RADIATION_CHECK_FREQ;
			if (remainder == RadiationExposure.RADIATION_CHECK_FREQ - 1) {
				RadiationStatus newExposed = RadiationStatus.calculateChance(pulse.getElapsed());
				setExposed(newExposed);
			}
		}
		
		return true;
	}

	/**
	 * Updates the status of Radiation exposure.
	 * 
	 * @param newExposed
	 */
	public void setExposed(RadiationStatus newExposed) {
		exposed = newExposed;
		
		if (exposed.isBaselineEvent()) {
			logger.log(this, Level.INFO, 1_000, DETECTOR + UnitEventType.BASELINE_EVENT + IMMINENT);
			this.fireUnitUpdate(UnitEventType.BASELINE_EVENT);
		}

		if (exposed.isGCREvent()) {
			logger.log(this, Level.INFO, 1_000, DETECTOR + UnitEventType.GCR_EVENT + IMMINENT);
			this.fireUnitUpdate(UnitEventType.GCR_EVENT);
		}

		if (exposed.isSEPEvent()) {
			logger.log(this, Level.INFO, 1_000, DETECTOR + UnitEventType.SEP_EVENT + IMMINENT);
			this.fireUnitUpdate(UnitEventType.SEP_EVENT);
		}
	}
	
	/**
	 * Gets the radiation status.
	 * 
	 * @return
	 */
	public RadiationStatus getExposed() {
		return exposed;
	}

	/**
	 * Gets a collection of people affected by this entity.
	 *
	 * @return person collection
	 */
	public Collection<Person> getAffectedPeople() {
		Collection<Person> people = new UnitSet<>();

		// Check all people.
        for (Person person : unitManager.getPeople()) {
            Task task = person.getMind().getTaskManager().getTask();

            // Add all people maintaining this vehicle.
            if (task instanceof MaintainBuilding mb
                    && this.equals(mb.getEntity())) {
                people.add(person);
            }

            // Add all people repairing this vehicle.
            if (task instanceof Repair r
                    && this.equals(r.getEntity())) {
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
		Collection<Person> people = new UnitSet<>();

		// Check all people.
        for (Person person : unitManager.getPeople()) {
            Task task = person.getMind().getTaskManager().getTask();

            // Add all people having conversation from all places as the task
            if (task instanceof Converse)
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
		Collection<Robot> robots = new UnitSet<>();

		// Check all robots.
        for (Robot robot : unitManager.getRobots()) {
            Task task = robot.getBotMind().getBotTaskManager().getTask();

            // Add all robots maintaining this vehicle.
            if (task instanceof MaintainBuilding mb) {
                if (mb.getEntity() == this) {
                    robots.add(robot);
                }
            }

            // Add all robots repairing this vehicle.
            if (task instanceof Repair r) {
                if (r.getEntity() == this) {
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
		if (!trail.isEmpty()) {
			Coordinates lastLocation = trail.get(trail.size() - 1);
			if (!lastLocation.equals(location) && !trail.contains(location)) {
				trail.add(location);
				}
		} else {
			trail.add(location);
		}
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
		salvageInfo = new SalvageInfo(this, info, settlement, masterClock.getMarsTime());
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
	 * Finds a new parking location and facing.
	 */
	public void findNewParkingLoc() {

		Settlement settlement = getSettlement();
		if (settlement == null) {
			logger.severe(this, "Not found to be parked in a settlement.");
			return;
		}

		LocalPosition centerLoc = LocalPosition.DEFAULT_POSITION;

		int weight = 2;

		List<Building> evas = settlement.getBuildingManager()
				.getBuildingsOfSameCategoryNZone0(BuildingCategory.EVA);
		int numGarages = settlement.getBuildingManager().getGarages().size();
		int total = (evas.size() + numGarages * weight - 1);
		if (total < 0)
			total = 0;
		int rand = RandomUtil.getRandomInt(total);

		if (rand != 0) {
			// Try parking near the eva for short walk
			if (rand < evas.size()) {
				Building eva = evas.get(rand);
				centerLoc = eva.getPosition();
			}

			else {
				// Try parking near a garage					
				Building garage = BuildingManager.getAGarage(getSettlement());
				if (garage != null) {
					centerLoc = garage.getPosition();
				}
			}
		}
		else {
			// Try parking near a garage
			// Get a nearby garage (but doesn't go in)
			Building garage = BuildingManager.getAGarage(getSettlement());
			if (garage != null) {
				centerLoc = garage.getPosition();
			}
		}

		// Place the vehicle starting from the settlement center (0,0).
		int oX = 10;
		int oY = 10;
		double newFacing = 0D;
		LocalPosition newLoc = null;
		int step = 2;
		boolean foundGoodLocation = false;

		boolean isSmallVehicle = VehicleType.isDrone(getVehicleType())
				|| getVehicleType() == VehicleType.LUV;

		double d = VEHICLE_CLEARANCE_0;
		if (isSmallVehicle)
			d = VEHICLE_CLEARANCE_1;
		
		// Note: enlarge (times 1.25) the dimension of a vehicle to avoid getting 
		// trapped within those enclosed space surrounded by buildings or hallways.
		
		double w = getWidth() * d * 1.25;
		double l = getLength() * d * 1.25;
		
		// Note: May need a more permanent solution by figuring out how to detect those enclosed space
		
		int count = 0;
		
		// Try iteratively outward from 10m to 500m distance range.
		for (int x = oX; (x < 500) && !foundGoodLocation; x+=step) {
			// Try random locations at each distance range.
			for (int y = oY; (y < 500) && !foundGoodLocation; y++) {
				double distance = Math.max(y, RandomUtil.getRandomDouble(-.5*x, .5*x) + .5*y);
				double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);
				
				newLoc = centerLoc.getPosition(distance, radianDirection);
				newFacing = RandomUtil.getRandomDouble(360D);

				// Check if new vehicle location collides with anything.
				
				// Note: excessive calling increase CPU Util
				foundGoodLocation = LocalAreaUtil.isObjectCollisionFree(this, w, l,
								newLoc.getX(), newLoc.getY(), 
								newFacing, getCoordinates());
				
				count++;
			}
		}

		if (foundGoodLocation) {
			setParkedLocation(newLoc, newFacing);
		}
	}

	/**
	 * Tags a vehicle for maintenance.
	 */
	public void maintainVehicle() {
        logger.info(this, "Triggering a vehicle maintenance task.");
	}
	
	
	/**
	 * Relocates a vehicle. 
	 */
	public void relocateVehicle() {
		if (isInGarage()) {
			BuildingManager.removeFromGarage(this);
			// Note: removeVehicle or removeFlyer will automatically call 
			// parkInVicinity which will in turns call findNewParkingLoc
			logger.info(this, "Left garage and parked outside as instructed.");
		}
		else {
			if (reservedForMaintenance || getPrimaryStatus() == StatusType.MAINTENANCE) {
				// If it's under maintenance, go to a garage if possible
				// else park outside
				logger.info(this, "Under maintenance. Looking for a garage.");
				boolean done = addToAGarage();
				if (!done) {
					logger.info(this, "Garage space not found. Parked outside.");
					findNewParkingLoc();
				}
			}
			else {
				logger.info(this, "Looking for another spot to park outside.");
				findNewParkingLoc();
			}
		}
	}

	public static double getFuelRangeErrorMargin() {
		return fuelRangeErrorMargin;
	}

	public static double getLifeSupportRangeErrorMargin() {
		return lifeSupportRangeErrorMargin;
	}

	/**
	 * Is the vehicle outside of a settlement but within its vicinity ?
	 *
	 * @return
	 */
	public boolean isRightOutsideSettlement() {
        return getLocationStateType() == LocationStateType.SETTLEMENT_VICINITY;
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
		return (mission != null);
	}

	/**
	 * Checks if the person is in a moving vehicle.
	 *
	 * @param person the person.
	 * @return true if person is in a moving vehicle.
	 */
	public static boolean inMovingRover(Person person) {
		return person.isInVehicle() && person.getVehicle().getPrimaryStatus() == StatusType.MOVING;
	}

	@Override
	public UnitType getUnitType() {
		return UnitType.VEHICLE;
	}

	/**
	 * Is this unit empty ?
	 *
	 * @return true if this unit doesn't carry any resources or equipment
	 */
	public boolean isEmpty() {
		return eqmInventory.isEmpty();
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
		return eqmInventory.getEquipmentSet();
	}

	/**
	 * Gets the container set.
	 *
	 * @return
	 */
	@Override
	public Set<Equipment> getContainerSet() {
		return eqmInventory.getContainerSet();
	}

	/**
	 * Gets the EVA suit set.
	 * 
	 * @return
	 */
	@Override
	public Set<Equipment> getSuitSet() {
		return eqmInventory.getSuitSet();
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
		return eqmInventory.addEquipment(e);
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
	public double getSpecificCapacity(int resource) {
		return eqmInventory.getSpecificCapacity(resource);
	}

	/**
	 * Obtains the remaining combined capacity of storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getRemainingCombinedCapacity(int resource) {
		return eqmInventory.getRemainingCombinedCapacity(resource);
	}

	/**
	 * Obtains the remaining specific capacity of storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getRemainingSpecificCapacity(int resource) {
		return eqmInventory.getRemainingSpecificCapacity(resource);
	}

	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasAmountResourceRemainingCapacity(int resource) {
		return eqmInventory.hasAmountResourceRemainingCapacity(resource);
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
	 * Gets the specific amount resources stored, NOT including those inside equipment.
	 *
	 * @param resource
	 * @return amount
	 */
	@Override
	public double getSpecificAmountResourceStored(int resource) {
		return eqmInventory.getSpecificAmountResourceStored(resource);
	}
	
	/**
	 * Gets all the specific amount resources stored, including those inside equipment.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAllSpecificAmountResourceStored(int resource) {
		return eqmInventory.getAllSpecificAmountResourceStored(resource);
	}

	/**
	 * Gets the quantity of all stock and specific amount resource stored.
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
	 * @param containerClass  the unit class
	 * @param brandNew  does it include brand new bag only
	 * @return number of empty containers
	 */
	@Override
	public int findNumEmptyContainersOfType(EquipmentType containerType, boolean brandNew) {
		return eqmInventory.findNumEmptyContainersOfType(containerType, brandNew);
	}

	/**
	 * Finds the number of containers of a particular type.
	 * 
	 * Note: will not count EVA suits.
	 *
	 * @param containerType the equipment type
	 * @return number of empty containers
	 */
	@Override
	public int findNumContainersOfType(EquipmentType containerType) {
		return eqmInventory.findNumContainersOfType(containerType);
	}

	/**
	 * Finds a container in storage.
	 *
	 * Note: will not count EVA suits.
	 * 
	 * @param containerType
	 * @param empty does it need to be empty ?
	 * @param resource If -1 then resource doesn't matter
	 * @return instance of container or null if none
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
		return getSuitSet().size();
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
	public Set<Integer> getSpecificResourceStoredIDs() {
		return eqmInventory.getSpecificResourceStoredIDs();
	}
	
	/**
	 * Gets all stored amount resources in eqmInventory, including inside equipment.
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAllAmountResourceStoredIDs() {
		return eqmInventory.getAllAmountResourceStoredIDs();
	}
	
	/**
	 * Obtains the remaining general storage space.
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
	protected boolean setContainerUnitAndID(UnitHolder newContainer) {
		if (newContainer != null) {
			var cu = getContainerUnit();
			
			if (newContainer.equals(cu)) {
				return true;
			}

			LocationStateType newState;
			// 2. Set new LocationStateType
			// Note: This is a special case for Vehicle
			//       A vehicle can have settlement as container unit while it's on Mars Surface
			// 2a. If the old cu is a settlement or building
			//     and the new cu is mars surface,
			//     then location state is within settlement vicinity
			if ((cu instanceof Settlement
						|| cu instanceof Building)
					&& newContainer instanceof MarsSurface) {
				newState = LocationStateType.SETTLEMENT_VICINITY;
			}	
			else {
				newState = getNewLocationState(newContainer);
			}
			
			// 3. Set containerID
			setContainer(newContainer, newState);
		}
		return true;
	}

	/**
	 * Gets the location state type based on the type of the new container unit.
	 *
	 * @param newContainer
	 * @return {@link LocationStateType}
	 */
	private LocationStateType getNewLocationState(UnitHolder newContainer) {

		return switch(newContainer) {
			case Settlement s -> (isInGarage() ? LocationStateType.INSIDE_SETTLEMENT
						: LocationStateType.SETTLEMENT_VICINITY);
			case Vehicle v -> LocationStateType.INSIDE_VEHICLE;
			case ConstructionSite cs -> LocationStateType.MARS_SURFACE;
			case Person p -> LocationStateType.ON_PERSON_OR_ROBOT;
			case MarsSurface ms -> LocationStateType.MARS_SURFACE;
			default -> null;
		};
	}

	/**
	 * Is this unit inside a settlement ?
	 *
	 * @return true if the unit is inside a settlement
	 */
	@Override
	public boolean isInSettlement() {
		if (getContainerUnit() instanceof MarsSurface) {
			return false;
		}

		var currentStateType = getLocationStateType();
		boolean isVehicleInGarage = LocationStateType.INSIDE_SETTLEMENT == currentStateType;
		boolean isVehicleInSettlementVicinity = LocationStateType.SETTLEMENT_VICINITY == currentStateType;
		boolean isUnitTypeSettlement = getContainerUnit() instanceof Settlement;

		return isVehicleInGarage || isVehicleInSettlementVicinity || isUnitTypeSettlement ;
	}

	/**
	 * Transfers the unit from one owner to another owner.
	 *
	 * @param origin {@link Unit} the original container unit
	 * @param destination {@link Unit} the destination container unit
	 */
	@Override
	public boolean transfer(UnitHolder destination) {
		boolean leaving = false;
		boolean transferred = false;
		var cu = getContainerUnit();
		// Note: at startup, a vehicle has Mars Surface as the container unit by default
		
		if (cu instanceof MarsSurface ms) {
			transferred = ms.removeVehicle(this);
		}
		
		else if (cu instanceof Settlement s) {
			transferred = s.removeVicinityParkedVehicle(this);
			leaving = true;
		}

		if (!transferred) {
			logger.warning(this, 60_000L, "Cannot be retrieved from " + cu + ".");
			// NOTE: need to revert back the retrieval action			
		}
		else {
			if (destination instanceof MarsSurface ms) {
				transferred = ms.addVehicle(this);
				leaving = true;
			}
			else if (destination instanceof Settlement s) {
    			// Add the vehicle to the settlement
				transferred = s.addVicinityVehicle(this);
			}

			if (!transferred) {
				logger.warning(this, 60_000L, "Cannot be stored into " + destination + ".");
				// NOTE: need to revert back the storage action
			}
			else {
				if (leaving && isInGarage()) {
					BuildingManager.removeFromGarage(this);
				}
				setContainerUnitAndID(destination);
			}
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
	
	/**
	 * Checks if battery charging is needed and charge the vehicle.
	 * 
	 * @param settlement
	 */ 
	public void chargeVehicle(ClockPulse pulse, Settlement settlement) {
		double timeInHours = pulse.getElapsed() * MarsTime.HOURS_PER_MILLISOL;
		double allowedEnergy = getController().getBattery().estimateChargeBattery(timeInHours);
		boolean isChargingNeeded = allowedEnergy > 0;

		if (isChargingNeeded) {
			double settlementPowerGridEnergy = settlement.getPowerGrid().retrieveStoredEnergy(allowedEnergy, timeInHours);
			double energyAccepted = getController().getBattery().chargeBattery(settlementPowerGridEnergy, timeInHours);
			
			if (energyAccepted > 0) {
				logger.info(this, 20_000L, "Charging. Budget: " + Math.round(allowedEnergy * 1000.0)/1000.0
						+ " kWh.  Accepted: " + Math.round(energyAccepted * 1000.0)/1000.0 + " kWh.");
			}
			else {
				setCharging(false);
			}
		}
		else {
			setCharging(false);
		}
	}
	
	public boolean isCharging() {
		return isCharging;
	}
	
	public void setCharging(boolean value) {
		isCharging = value;
	}
	
	public EquipmentInventory getEquipmentInventory() {
		return eqmInventory;
	}

	public VehicleController getController() {
		return vehicleController;
	}

	public VehicleSpec getVehicleSpec() {
		return spec;
	}
	
	/**
	 * Compares if an object is the same as this unit.
	 *
	 * @param obj
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Vehicle v = (Vehicle) obj;
		return this.getName() == v.getName()
				&& this.getVehicleType() == v.getVehicleType()
				&& this.getIdentifier() == v.getIdentifier();
	}

	/**
	 * Gets the hash code value.
	 *
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		return getIdentifier() % 64;
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

    public String getChildContext() {
        return getContext() + ENTITY_SEPERATOR + getName();
    }
}