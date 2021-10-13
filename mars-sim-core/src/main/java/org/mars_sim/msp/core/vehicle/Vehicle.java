/*
 * Mars Simulation Project
 * Vehicle.java
 * @date 2021-08-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.vehicle;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.data.MSolDataItem;
import org.mars_sim.msp.core.data.MSolDataLogger;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.Delivery;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.HaveConversation;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.Indoor;
import org.mars_sim.msp.core.structure.building.function.SystemType;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Vehicle class represents a generic vehicle. It keeps track of generic
 * information about the vehicle. This class needs to be subclassed to represent
 * a specific type of vehicle.
 */
public abstract class Vehicle extends Unit
		implements Malfunctionable, Salvagable, Temporal, Indoor, LocalBoundedObject, Serializable {

	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(Vehicle.class.getName());
	
	/** The error margin for determining vehicle range. (Actual distance / Safe distance). */
	private static double fuel_range_error_margin;
	private static double life_support_range_error_margin;

	// For Methane : 
	// Specific energy is 55.5	MJ/kg, or 15,416 Wh/kg, or 15.416kWh / kg
	// Energy density is 0.0364 MJ/L, 36.4 kJ/L or 10 Wh/L
	// Note : 1 MJ = 0.277778 kWh; 1 kWh = 3.6 MJ
	// as comparison, 1 gallon (or 3.7854 L) of gasoline (which, for the record, it says is 33.7 kilowatt-hours) +> 8.9 kWh / L
	
	/** The specific energy of CH4 [kWh/kg] */
	public static final double METHANE_SPECIFIC_ENERGY = 15.416D;
	/** The Solid Oxide Fuel Cell Conversion Efficiency (dimension-less) */
	public static final double SOFC_CONVERSION_EFFICIENCY = .65;
//	/** Lifetime Wear in millisols **/
//	private static final double WEAR_LIFETIME = 668_000; // 668 Sols (1 orbit)
	/** Estimated Number of hours traveled each day. **/
	private static final int ESTIMATED_NUM_HOURS = 16;

	// Name format for numbers units
	private static final String VEHICLE_TAG_NAME = "%s %03d";
	
	/** The types of status types that make a vehicle unavailable for us. */
	private static final List<StatusType> badStatus = Arrays.asList(
			StatusType.MAINTENANCE, 
			StatusType.TOWED, 
			StatusType.MOVING,
			StatusType.STUCK, 
			StatusType.MALFUNCTION);
	
	// 1989 NASA Mars Manned Transportation Vehicle - Shuttle Fuel Cell Power Plant (FCP)  7.6 kg/kW
	
	// DOE 2010 Targe : Specific power = 650 W_e/L; Power Density = 650 W_e/kg
	// Toyota Mirai Fuel cell - 90 kW
	
	// Data members
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
	
	/** The Base Lifetime Wear in msols **/
	private double baseWearLifetime;
	/** Current speed of vehicle in kph. */
	private double speed = 0; // 
	/** Previous speed of vehicle in kph. */
	private double previousSpeed = 0;
	/** Base speed of vehicle in kph (can be set in child class). */
	private double baseSpeed = 0; // 
	/** The base range of the vehicle (with full tank of fuel and no cargo) (km). */
	private double baseRange = 0;
	/** Total cumulative distance traveled by vehicle (km). */
	private double odometerMileage; // 
	/** Distance traveled by vehicle since last maintenance (km) . */
	private double distanceMaint; // 
	/** The efficiency of the vehicle's drivetrain. (kWh/km). */
	private double drivetrainEfficiency;
	/** The average power output of the vehicle. (kW). */
	private double averagePower = 0;
	/** The peak power output of the vehicle. (kW). */
	private double peakPower = 0;
	/** The total number of hours the vehicle is capable of operating. (hr). */
	private double totalHours;
	/** The maximum fuel capacity of the vehicle [kg] */
	private double fuelCapacity;
	/** The total energy of the vehicle in full tank [kWh]. */
	private double totalEnergy = 100D;
	/** The base fuel economy of the vehicle [km/kg]. */
	private double baseFuelEconomy;
	/** The base fuel consumption of the vehicle [km/kWh]. */
	private double baseFuelConsumption;
	/** The estimated average fuel economy of the vehicle for a trip [km/kg]. */
	private double estimatedAveFuelEconomy;
	/** The estimated combined total crew weight for a trip [km/kg]. */
	private double estimatedTotalCrewWeight;
	/** The cargo capacity of the vehicle for a trip [km/kg]. */
	private double cargoCapacity;
	/** The actual start mass of the vehicle (base mass + crew weight + full cargo weight) for a trip [km/kg]. */
	private double startMass;
	/** The estimated beginning mass of the vehicle (base mass + crew weight + full cargo weight) for a trip [km/kg]. */
	private double beginningMass;
	/** The base acceleration of the vehicle [m/s2]. */
	private double baseAccel = 0;
	/** The estimated end mass of the vehicle (base mass + crew weight + remaining cargo weight) for a trip [km/kg]. */
	private double endMass;
	/** Width of vehicle (meters). */
	private double width;
	/** Length of vehicle (meters). */
	private double length;
	/** Parked X location (meters) from center of settlement. */
	private double xLocParked;
	/** Parked Y location (meters) from center of settlement. */
	private double yLocParked;
	/** Parked facing (degrees clockwise from North). */
	private double facingParked;
	
	/** The vehicle type string. */	
	private String vehicleTypeString;
	/** The vehicle type. */	
	private VehicleType vehicleType;
	/** The type of dessert loaded. */	
	private String typeOfDessertLoaded;
	
	/** A collection of locations that make up the vehicle's trail. */
	private ArrayList<Coordinates> trail;
	/** List of operator activity spots. */
	private List<Point2D> operatorActivitySpots;
	/** List of passenger activity spots. */
	private List<Point2D> passengerActivitySpots;
	/** List of status types. */
	private Set<StatusType> statusTypes = new HashSet<>();
	/** The vehicle's status log. */
	private MSolDataLogger<Set<StatusType>> vehicleLog = new MSolDataLogger<>(5);
	
	/** The malfunction manager for the vehicle. */
	protected MalfunctionManager malfunctionManager; 
	/** Direction vehicle is traveling */
	private Direction direction;
	/** The operator of the vehicle. */
	private VehicleOperator vehicleOperator;
	/** The one currently towing this vehicle. */
	private Vehicle towingVehicle;
	/** The The vehicle's salvage info. */
	private SalvageInfo salvageInfo; 

	static {
		life_support_range_error_margin = simulationConfig.getSettlementConfiguration()
				.getRoverValues()[0];
		fuel_range_error_margin = simulationConfig.getSettlementConfiguration().getRoverValues()[1];
	}

	/**
	 * Constructor 1 : prepares a Vehicle object with a given settlement
	 * 
	 * @param name                the vehicle's name
	 * @param vehicleType         the configuration description of the vehicle.
	 * @param settlement          the settlement the vehicle is parked at.
	 * @param maintenanceWorkTime the work time required for maintenance (millisols)
	 */
	Vehicle(String name, String vehicleTypeString, Settlement settlement, double maintenanceWorkTime) {
		// Use Unit constructor
		super(name, settlement.getCoordinates());
		
		if (unitManager == null)
			unitManager = sim.getUnitManager();
		
		this.vehicleTypeString = vehicleTypeString;
		vehicleType = VehicleType.convertNameToVehicleType(vehicleTypeString);
		
		// Obtain the associated settlement ID 
		associatedSettlementID = settlement.getIdentifier();

		// Add this vehicle to be owned by the settlement
		settlement.addOwnedVehicle(this);

		// Store this vehicle in the settlement
		settlement.getInventory().storeUnit(this);
	
		// Initialize vehicle data
		vehicleTypeString = vehicleTypeString.toLowerCase();
	
		if (vehicleTypeString.equalsIgnoreCase(VehicleType.DELIVERY_DRONE.getName())) {
			baseWearLifetime = 668_000 * .75; // 668 Sols (1 orbit)
			// Note: Hard code the value of averagePower for the time being
			averagePower = 45;
		}
		else if (vehicleTypeString.equalsIgnoreCase(VehicleType.LUV.getName())) {
			baseWearLifetime = 668_000 * 2D; // 668 Sols (1 orbit)
			averagePower = 15;
		}	
		else if (vehicleTypeString.equalsIgnoreCase(VehicleType.EXPLORER_ROVER.getName())) {
			baseWearLifetime = 668_000; // 668 Sols (1 orbit)
			averagePower = 60;
		}
		else if (vehicleTypeString.equalsIgnoreCase(VehicleType.TRANSPORT_ROVER.getName())) {
			baseWearLifetime = 668_000 * 1.5; // 668 Sols (1 orbit)
			averagePower = 75;
		}
		else if (vehicleTypeString.equalsIgnoreCase(VehicleType.CARGO_ROVER.getName())) {
			baseWearLifetime = 668_000 * 1.25; // 668 Sols (1 orbit)
			averagePower = 90;
		}
			
		direction = new Direction(0);
		trail = new ArrayList<>();
		statusTypes = new HashSet<>();
		
		isReservedMission = false;
		distanceMark = false;
		reservedForMaintenance = false;
		emergencyBeacon = false;
		isSalvaged = false;

		// Initialize malfunction manager.
		malfunctionManager = new MalfunctionManager(this, baseWearLifetime, maintenanceWorkTime);
		
		// Add "vehicle" as scope
		malfunctionManager.addScopeString(SystemType.VEHICLE.getName());
		
		// Add its vehicle type as scope
		malfunctionManager.addScopeString(vehicleTypeString);
		
		// Add "rover" as scope
		if (vehicleTypeString.contains(SystemType.ROVER.getName())) {
			malfunctionManager.addScopeString(SystemType.ROVER.getName());
		}

		addStatus(StatusType.PARKED);
		
		// Set width and length of vehicle.
		VehicleConfig vehicleConfig = simulationConfig.getVehicleConfiguration();
		width = vehicleConfig.getWidth(vehicleTypeString);
		length = vehicleConfig.getLength(vehicleTypeString);

		// Set description
		setDescription(vehicleTypeString);
		// Set total distance traveled by vehicle (km)
		odometerMileage = 0;
		// Set distance traveled by vehicle since last maintenance (km)
		distanceMaint = 0;
		// Set base speed.
		baseSpeed = vehicleConfig.getBaseSpeed(vehicleTypeString);
//		setBaseSpeed(baseSpeed);

		// Set the empty mass of the vehicle.
		setBaseMass(vehicleConfig.getEmptyMass(vehicleTypeString));

		// Set the drivetrain efficiency [in kWh/km] of the vehicle.
		drivetrainEfficiency = vehicleConfig.getDrivetrainEfficiency(vehicleTypeString) ;
		
		// Gets the capacity [in kg] of vehicle's fuel tank 
		fuelCapacity = vehicleConfig.getCargoCapacity(vehicleTypeString, ResourceUtil.findAmountResourceName(getFuelType())); 

		// Gets the total energy [in kWh] on a full tank of methane
		totalEnergy = METHANE_SPECIFIC_ENERGY * fuelCapacity * SOFC_CONVERSION_EFFICIENCY * drivetrainEfficiency;

		// Assume Peak power as 3x average power.
		peakPower = averagePower * 3.0;
		
		// Gets the maximum total # of hours the vehicle is capable of operating
		totalHours = totalEnergy / averagePower * 3.0;
		
		// Gets the base range [in km] of the vehicle
		baseRange = baseSpeed * totalHours;

		// Gets the base fuel economy [in km/kg] of this vehicle 
		baseFuelEconomy = baseRange / fuelCapacity;
		
		// Gets the base fuel consumption [in km/kWh] of this vehicle 
		baseFuelConsumption = baseRange / totalEnergy;
		
		// Gets the crew capacity
		int numCrew = vehicleConfig.getCrewSize(vehicleTypeString);

		estimatedTotalCrewWeight = numCrew * Person.getAverageWeight();
		
		cargoCapacity = vehicleConfig.getTotalCapacity(vehicleTypeString);
		
		// Set the general capacity of the inventory to cargoCapacity
		getInventory().addGeneralCapacity(cargoCapacity);
		
		if (this instanceof Rover) {
		
			beginningMass = getBaseMass() + estimatedTotalCrewWeight + 500;	
			// Accounts for the rock sample, ice or regolith collected
			endMass = getBaseMass() + estimatedTotalCrewWeight + 1000;
		}
		
		else if (this instanceof Drone || this instanceof LightUtilityVehicle) {
			beginningMass = getBaseMass() + 300;
			// Accounts for the rock sample, ice or regolith collected
			endMass = getBaseMass()  + 300;
		}
		
		if (this instanceof Drone || this instanceof Rover) {
			// Gets the estimated average fuel economy for a trip [km/kg]
			estimatedAveFuelEconomy = baseFuelEconomy * (beginningMass / endMass * .75);
			// Gets the acceleration in m/s2
			baseAccel = averagePower / beginningMass / baseSpeed * 1000 * 3.6;
		}
		
		// Set initial parked location and facing at settlement.
		findNewParkingLoc();

		// Initialize operator activity spots.
		operatorActivitySpots = new ArrayList<>(vehicleConfig.getOperatorActivitySpots(vehicleTypeString));

		// Initialize passenger activity spots.
		passengerActivitySpots = new ArrayList<>(vehicleConfig.getPassengerActivitySpots(vehicleTypeString));
	}

	/**
	 * Constructor 2 : prepares a Vehicle object for testing (called by MockVehicle)
	 * 
	 * @param name                the vehicle's name
	 * @param vehicleType         the configuration description of the vehicle.
	 * @param settlement          the settlement the vehicle is parked at.
	 * @param baseSpeed           the base speed of the vehicle (kph)
	 * @param baseMass            the base mass of the vehicle (kg)
	 * @param fuelEconomy		  the fuel economy of the vehicle (km/kg)
	 * @param maintenanceWorkTime the work time required for maintenance (millisols)
	 */
	protected Vehicle(String name, String vehicleType, Settlement settlement, double baseSpeed, double baseMass,
			double fuelEconomy, double maintenanceWorkTime) {

		// Use Unit constructor
		super(name, settlement.getCoordinates());
		
		if (unitManager == null)
			unitManager = sim.getUnitManager();

		this.vehicleTypeString = vehicleType;

		associatedSettlementID = settlement.getIdentifier();
//		containerUnit = settlement;
		setContainerID(associatedSettlementID);
		settlement.getInventory().storeUnit(this);

		direction = new Direction(0);
		trail = new ArrayList<>();
		statusTypes = new HashSet<>();
		
		// Set description
		setDescription(vehicleType);
		// Set total distance traveled by vehicle [km]
		odometerMileage = 0;
		// Set distance traveled by vehicle since last maintenance [km]
		distanceMaint = 0;
		// Set the base fuel economy of the vehicle [km/kg]
		baseFuelEconomy = fuelEconomy;
		// Set base speed.
		this.baseSpeed = baseSpeed;
		// Set the empty mass of the vehicle.
		setBaseMass(baseMass);
		
		isReservedMission = false;
		distanceMark = false;
		reservedForMaintenance = false;
		emergencyBeacon = false;

		isSalvaged = false;
		salvageInfo = null;
		width = 0D;
		length = 0D;
		xLocParked = 0D;
		yLocParked = 0D;
		facingParked = 0D;

		// Initialize malfunction manager.
		malfunctionManager = new MalfunctionManager(this, getBaseWearLifetime(), maintenanceWorkTime);
		malfunctionManager.addScopeString(SystemType.VEHICLE.getName());
		
		addStatus(StatusType.PARKED);
	}

	public String getDescription(String vehicleType) {
		VehicleConfig vehicleConfig = simulationConfig.getVehicleConfiguration();
		return vehicleConfig.getDescription(vehicleType);
	}

	public String getVehicleTypeString() {
		return vehicleTypeString;
	}

	public VehicleType getVehicleType() {
		return vehicleType;
	}
	
	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public double getLength() {
		return length;
	}

	@Override
	public double getXLocation() {
		return xLocParked;
	}

	@Override
	public double getYLocation() {
		return yLocParked;
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
	public List<Point2D> getOperatorActivitySpots() {
		return operatorActivitySpots;
	}

	/**
	 * Gets a list of passenger activity spots.
	 * 
	 * @return list of activity spots as Point2D objects.
	 */
	public List<Point2D> getPassengerActivitySpots() {
		return passengerActivitySpots;
	}

	/**
	 * Sets the location and facing of the vehicle when parked at a settlement.
	 * 
	 * @param xLocation the x location (meters from settlement center - West:
	 *                  positive, East: negative).
	 * @param yLocation the y location (meters from settlement center - North:
	 *                  positive, South: negative).
	 * @param facing    (degrees from North clockwise).
	 */
	public void setParkedLocation(double xLocation, double yLocation, double facing) {

		// Get current human crew positions relative to the vehicle.
		Map<Person, Point2D> currentCrewPositions = getCurrentCrewPositions();

		// Get current robot crew positions relative to the vehicle.
		Map<Robot, Point2D> currentRobotCrewPositions = getCurrentRobotCrewPositions();

		// Set new parked location for the vehicle.
		this.xLocParked = xLocation;
		this.yLocParked = yLocation;
		this.facingParked = facing;

		// Set the human crew locations to the vehicle's new parked location.
		setCrewPositions(currentCrewPositions);

		// Set the robot crew locations to the vehicle's new parked location.
		setRobotCrewPositions(currentRobotCrewPositions);
	}

	/**
	 * Gets all human crew member positions relative to within the vehicle.
	 * 
	 * @return map of crew members and their relative vehicle positions.
	 */
	private Map<Person, Point2D> getCurrentCrewPositions() {

		Map<Person, Point2D> result = null;

		// Record current object-relative crew positions if vehicle is crewable.
		if (this instanceof Crewable) {
			Crewable crewable = (Crewable) this;
			result = new HashMap<>(crewable.getCrewNum());
			Iterator<Person> i = ((Crewable) this).getCrew().iterator();
			while (i.hasNext()) {
				Person crewmember = i.next();
				Point2D crewPos = LocalAreaUtil.getObjectRelativeLocation(crewmember.getXLocation(),
						crewmember.getYLocation(), this);
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
	private Map<Robot, Point2D> getCurrentRobotCrewPositions() {

		Map<Robot, Point2D> result = null;

		// Record current object-relative crew positions if vehicle is crewable.
		if (this instanceof Crewable) {
			Crewable crewable = (Crewable) this;
			result = new HashMap<>(crewable.getRobotCrewNum());
			Iterator<Robot> i = ((Crewable) this).getRobotCrew().iterator();
			while (i.hasNext()) {
				Robot robotCrewmember = i.next();
				Point2D crewPos = LocalAreaUtil.getObjectRelativeLocation(robotCrewmember.getXLocation(),
						robotCrewmember.getYLocation(), this);
				result.put(robotCrewmember, crewPos);
			}
		}

		return result;
	}

	/**
	 * Sets the positions of all human crew members (if any) to the vehicle's
	 * location.
	 */
	private void setCrewPositions(Map<Person, Point2D> currentCrewPositions) {

		// Only move crew if vehicle is Crewable.
		if (this instanceof Crewable) {
			Iterator<Person> i = ((Crewable) this).getCrew().iterator();
			while (i.hasNext()) {
				Person crewmember = i.next();

				Point2D currentCrewPos = currentCrewPositions.get(crewmember);
				Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(currentCrewPos.getX(),
						currentCrewPos.getY(), this);
				crewmember.setXLocation(settlementLoc.getX());
				crewmember.setYLocation(settlementLoc.getY());
			}
		}
	}

	/**
	 * Sets the positions of all robot crew members (if any) to the vehicle's
	 * location.
	 */
	private void setRobotCrewPositions(Map<Robot, Point2D> currentRobotCrewPositions) {

		// Only move crew if vehicle is Crewable.
		if (this instanceof Crewable) {
			Iterator<Robot> i = ((Crewable) this).getRobotCrew().iterator();
			while (i.hasNext()) {
				Robot robotCrewmember = i.next();

				Point2D currentCrewPos = currentRobotCrewPositions.get(robotCrewmember);
				Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(currentCrewPos.getX(),
						currentCrewPos.getY(), this);
				robotCrewmember.setXLocation(settlementLoc.getX());
				robotCrewmember.setYLocation(settlementLoc.getY());
			}
		}
	}

	/**
	 * Returns a list of vehicle's status types
	 * 
	 * @return the vehicle's status types
	 */
	public Set<StatusType> getStatusTypes() {
		return statusTypes;
	}

	/**
	 * Prints a string list of status types
	 * 
	 * @return
	 */
	public String printStatusTypes() {
		String s = statusTypes.toString();
		return Conversion.capitalize(s.substring(1 , s.length() - 1).toLowerCase());
	}
	
	/**
	 * Checks if this vehicle has already been tagged with a status type
	 * 
	 * @param status the status type of interest
	 * @return yes if it has it
	 */
	public boolean haveStatusType(StatusType status) {
        return statusTypes.contains(status);
    }
	
	/**
	 * Checks if this vehicle has no issues and is ready for mission
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
	
	/**
	 * Adds a status type for this vehicle
	 * 
	 * @param newStatus the status to be added
	 */
	public void addStatus(StatusType newStatus) {
		// Update status based on current situation.
		if (!statusTypes.contains(newStatus)) {
			statusTypes.add(newStatus);
			writeLog();
			fireUnitUpdate(UnitEventType.STATUS_EVENT, newStatus);
		}
	}
	
	/**
	 * Remove a status type for this vehicle
	 * 
	 * @param oldStatus the status to be removed
	 */
	public void removeStatus(StatusType oldStatus) {
		// Update status based on current situation.
		if (statusTypes.contains(oldStatus)) {
			statusTypes.remove(oldStatus);
			writeLog();
			fireUnitUpdate(UnitEventType.STATUS_EVENT, oldStatus);
		}
	}
	
	/**
	 * Checks the vehicle's status.
	 */
	private void checkStatus() {
		// Update status based on current situation.
		if (speed == 0) {
			if (getGarage() != null) {
				addStatus(StatusType.GARAGED);
				removeStatus(StatusType.PARKED);
			}
			else {
				addStatus(StatusType.PARKED);
				removeStatus(StatusType.GARAGED);
			}
			
			removeStatus(StatusType.MOVING);
//			removeStatus(StatusType.TOWED);
		}
		else {
			addStatus(StatusType.MOVING);
			removeStatus(StatusType.GARAGED);
			removeStatus(StatusType.PARKED);
		}
		
		if (isBeingTowed()) {
			addStatus(StatusType.TOWED);
//			removeStatus(StatusType.GARAGED);
//			removeStatus(StatusType.PARKED);
		}
		else if (this instanceof Rover && ((Rover)this).isTowingAVehicle()) {
			removeStatus(StatusType.TOWING);
		}
		
		if (reservedForMaintenance) {
			addStatus(StatusType.MAINTENANCE);
			removeStatus(StatusType.MOVING);
		}
		else {
			removeStatus(StatusType.MAINTENANCE);
		}
		
		if (malfunctionManager.hasMalfunction()) {
			addStatus(StatusType.MALFUNCTION);	
		}
	}
	
	/**
	 * Records the status in the vehicle log
	 */
	private void writeLog() {
		vehicleLog.addDataPoint(new HashSet<>(statusTypes));
	}

	/**
	 * Gets the vehicle log
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
			addStatus(StatusType.TOWED);
		}
		else {
			removeStatus(StatusType.TOWED);
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
	 * Check if this vehicle is being towed (by another vehicle).
	 * 
	 * @return true if it is being towed
	 */
	public boolean isBeingTowed() {
        return towingVehicle != null;
    }

	/**
	 * Gets the speed of vehicle
	 * 
	 * @return the vehicle's speed (in km/hr)
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * Gets the previous speed of vehicle
	 * 
	 * @return the vehicle's previous speed (in km/hr)
	 */
	public double getPreviousSpeed() {
		return previousSpeed;
	}
	
	/**
	 * Sets the vehicle's current speed
	 * 
	 * @param speed the vehicle's speed (in km/hr)
	 */
	public void setSpeed(double speed) {
		if (speed < 0D)
			throw new IllegalArgumentException("Vehicle speed cannot be less than 0 km/hr: " + speed);
		if (Double.isNaN(speed))
			throw new IllegalArgumentException("Vehicle speed is a NaN");
		this.previousSpeed = this.speed;
		this.speed = speed;
		fireUnitUpdate(UnitEventType.SPEED_EVENT);
	}

	/**
	 * Gets the base speed of vehicle
	 * 
	 * @return the vehicle's base speed (in km/hr)
	 */
	public double getBaseSpeed() {
		return baseSpeed;
	}

//	/**
//	 * Sets the base speed of vehicle
//	 * 
//	 * @param speed the vehicle's base speed (in km/hr)
//	 */
//	public void setBaseSpeed(double speed) {
//		if (speed < 0D)
//			throw new IllegalArgumentException("Vehicle base speed cannot be less than 0 km/hr");
//		baseSpeed = speed;
//	}

	/**
	 * Gets the current fuel range of the vehicle
	 * Note : this method will be overridden by Rover's getRange(). 
	 * 
	 * @param missionType the type of mission (needed in vehicle's getRange())
	 * @return the current fuel range of the vehicle (in km)
	 */
	public double getRange(MissionType missionType) {
		
		int radius = getAssociatedSettlement().getMissionRadius(missionType);	
		double range = 0;
		Mission mission = getMission();
		
        if (mission == null) {
        	// Before the mission is created, the range would be based on vehicle's capacity
        	range = estimatedAveFuelEconomy * fuelCapacity * getBaseMass() / getMass();// / fuel_range_error_margin;
        }
        else if (VehicleMission.REVIEWING.equals(mission.getPhase()) 
        	|| VehicleMission.EMBARKING.equals(mission.getPhase())) {
        	// Before loading/embarking phase, the amountOfFuel to be loaded is still zero.
        	// So the range would be based on vehicle's capacity
        	range = estimatedAveFuelEconomy * fuelCapacity * getBaseMass() / getMass();// / fuel_range_error_margin;
        }
        else {
            double amountOfFuel = getInventory().getAmountResourceStored(getFuelType(), false);
        	// During the journey, the range would be based on the amount of fuel in the vehicle
    		range = estimatedAveFuelEconomy * amountOfFuel * getBaseMass() / getMass();// / fuel_range_error_margin;	
        }
        
		range = Math.min(radius, (int)range);
		
		return range;
	}

	/**
	 * Gets the base range of the vehicle
	 * 
	 * @return the base range of the vehicle (in km)
	 * @throws Exception if error getting range.
	 */
	public double getBaseRange() {
		return baseRange;
	}

	/**
	 * Gets the fuel capaacity of the vehicle [kg].
	 * 
	 * @return
	 */
	public double getFuelCapacity() {
		return fuelCapacity;
	}
	

	/**
	 * Gets the energy available at the full tank [kWh].
	 * 
	 * @return
	 */
	public double getFullEnergy() {
		return totalEnergy;	
	}
	
	/**
	 * Gets the base fuel economy of the vehicle [km/kg].
	 * @return
	 */
	public double getBaseFuelEconomy() {
		return baseFuelEconomy;
	}

	/**
	 * Gets the instantaneous fuel economy of the vehicle [km/kg] 
	 * Note: assume that it is primarily dependent upon the current weight of the vehicle
	 * 
	 * @return
	 */ 
	public double getIFuelEconomy() {
//		if (speed > 0 && startMass != getMass())
//			logger.info(this 
//				+ "   current mass : " + Math.round(getMass()*10.0)/10.0
//				+ "   start mass : " + Math.round(startMass*10.0)/10.0 
//				+ "   driveTrain : " + drivetrainEfficiency 
//				+ "   IFC : " + Math.round(estimatedAveFuelEconomy * startMass / getMass()*10.0)/10.0);
		return estimatedAveFuelEconomy * startMass / getMass(); 
		
	}
	
	/**
	 * Records the beginning weight of the vehicle and its payload
	 */
	public void recordStartMass() {
		startMass = getMass();	
	}
	
	/**
	 * Gets the estimated average fuel consumption of the vehicle [km/kg] for a trip 
	 * Note: Assume that it is half of two fuel consumption values (between the beginning and the end of the trip)
	 * 
	 * @return
	 */
	public double getEstimatedAveFuelEconomy() {
		return estimatedAveFuelEconomy;
	}
	
	/**
	 * Gets the drivetrain efficiency of the vehicle.
	 * 
	 * @return drivetrain efficiency
	 */
	public double getDrivetrainEfficiency() {
		return drivetrainEfficiency;
	}

	/**
	 * Returns total distance traveled by vehicle (in km.)
	 * 
	 * @return the total distanced traveled by the vehicle (in km)
	 */
	public double getOdometerMileage() {
		return odometerMileage;
	}

	/**
	 * Adds a distance ]in km] to the vehicle's odometer (total distance traveled)
	 * 
	 * @param distance distance to add to total distance traveled (in km)
	 */
	public void addOdometerMileage(double distance) {
		odometerMileage += distance;
	}

	/**
	 * Returns distance traveled by vehicle since last maintenance (in km.)
	 * 
	 * @return distance traveled by vehicle since last maintenance (in km)
	 */
	public double getDistanceLastMaintenance() {
		return distanceMaint;
	}

	/**
	 * Adds a distance (in km.) to the vehicle's distance since last maintenance.
	 * Set distanceMark to true if vehicle is due for maintenance.
	 * 
	 * @param distance distance to add (in km)
	 */
	public void addDistanceLastMaintenance(double distance) {
		distanceMaint += distance;
		if ((distanceMaint > 5000D) && !distanceMark)
			distanceMark = true;
	}

	/** Sets vehicle's distance since last maintenance to zero */
	public void clearDistanceLastMaintenance() {
		distanceMaint = 0;
	}

	/**
	 * Returns direction of vehicle (0 = north, clockwise in radians)
	 * 
	 * @return the direction the vehicle is traveling (in radians)
	 */
	public Direction getDirection() {
		return (Direction) direction.clone();
	}

	/**
	 * Sets the vehicle's facing direction (0 = north, clockwise in radians)
	 * 
	 * @param direction the direction the vehicle is traveling (in radians)
	 */
	public void setDirection(Direction direction) {
		this.direction.setDirection(direction.getDirection());
	}


	/**
	 * Gets the instantaneous acceleration of the vehicle [m/s2]
	 * @return
	 */
	public double getAccel() {
		if (speed <= 1)
			return baseAccel;
		return (baseAccel + Math.min(baseAccel, averagePower / getMass() / speed * 3600)) / 2.0;
	}
	
	public abstract double getTerrainGrade();
	
	public abstract double getElevation();
	
	/**
	 * Gets the operator of the vehicle (person or AI)
	 * 
	 * @return the vehicle operator
	 */
	public VehicleOperator getOperator() {
//		if (vehicleOperator != null)
//			System.out.println(this.getNickName() + " operator: " + vehicleOperator.getOperatorName());
//		else
//			System.out.println(this.getNickName() + " vehicleOperator is null.");
		return vehicleOperator;
	}

	/**
	 * Sets the operator of the vehicle
	 * 
	 * @param vehicleOperator the vehicle operator
	 */
	public void setOperator(VehicleOperator vehicleOperator) {
		this.vehicleOperator = vehicleOperator;
//		if (vehicleOperator != null)
//			System.out.println(this.getNickName() + " new operator: " + vehicleOperator.getOperatorName());
//		else
//			System.out.println(this.getNickName() + " vehicleOperator is null.");
		fireUnitUpdate(UnitEventType.OPERATOR_EVENT, vehicleOperator);
	}

	/**
	 * Checks if a particular operator is appropriate for a vehicle.
	 * 
	 * @param operator the operator to check
	 * @return true if appropriate operator for this vehicle.
	 */
	public abstract boolean isAppropriateOperator(VehicleOperator operator);

	/**
	 * Returns the current settlement vehicle is parked at. Returns null if vehicle
	 * is not currently parked at a settlement.
	 * 
	 * @return the settlement the vehicle is parked at
	 */
	public Settlement getSettlement() {
		
		if (getContainerID() == 0)
			return null;
		
		Unit c = getContainerUnit();

		if (c instanceof Settlement)
			return (Settlement) c;
		else
			return null;
//		
//		 Unit topUnit = getTopContainerUnit();
//		 
//		 if ((topUnit != null) && (topUnit instanceof Settlement)) return (Settlement)
//		 topUnit; else return null;
//		 
	}

	/**
	 * Get the garage building that the vehicle is at
	 * 
	 * @return {@link Vehicle}
	 */
	public Building getGarage() {
		return BuildingManager.getBuilding(this, getSettlement());
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
		
		// Checks status.
		checkStatus();
		
		if (haveStatusType(StatusType.MOVING)) {
			// Assume the wear and tear factor is at 100% by being used in a mission
			malfunctionManager.activeTimePassing(pulse.getElapsed());
		}
		
		// If it's back at a settlement and is NOT in a garage
		else if (getSettlement() != null && !isVehicleInAGarage()) {
			if (!haveStatusType(StatusType.MAINTENANCE)) {
				// Assume the wear and tear factor is 75% less by being exposed outdoor
				malfunctionManager.activeTimePassing(pulse.getElapsed() * .25);
			}
		}
		
		// Make sure reservedForMaintenance is false if vehicle needs no maintenance.
		if (haveStatusType(StatusType.MAINTENANCE)) {
			if (malfunctionManager.getEffectiveTimeSinceLastMaintenance() <= 0D) {
				setReservedForMaintenance(false);
				removeStatus(StatusType.MAINTENANCE);
			}
		}
		else { // not under maintenance
			// Note: during maintenance, it doesn't need to be checking for malfunction.
			malfunctionManager.timePassing(pulse);
		}

		if (haveStatusType(StatusType.MALFUNCTION)) {
			if (malfunctionManager.getMalfunctions().size() == 0)
				removeStatus(StatusType.MALFUNCTION);
		}
	
		// Add the location to the trail if outside on a mission
		addToTrail(getCoordinates());

		correctVehicleReservation();

		return true;
	}
	
	/**
	 * Checks if the vehicle is currently in a garage or not.
	 * 
	 * @return true if vehicle is in a garage.
	 */
	public boolean isVehicleInAGarage() {
		return (BuildingManager.getBuilding(this) != null);
	}
	
	/**
	 * Resets the vehicle reservation status
	 */
	public void correctVehicleReservation() {
		if (isReservedMission) {
			// Set reserved for mission to false if the vehicle is not associated with a
			// mission.
			if (missionManager.getMissionForVehicle(this) == null) {
				logger.log(this, Level.FINE, 5000, 
						"Found reserved for an non-existing mission. Untagging it.");
				setReservedForMission(false);
			}
		} else {
			if (missionManager.getMissionForVehicle(this) != null) {
				logger.log(this, Level.FINE, 5000, 
						"On a mission but not registered as mission reserved. Correcting it.");
				setReservedForMission(true);
			}
		}
	}
	
	/**
	 * Gets a collection of people affected by this entity.
	 * 
	 * @return person collection
	 */
	public Collection<Person> getAffectedPeople() {
		Collection<Person> people = new ConcurrentLinkedQueue<>();

		// Check all people.
		Iterator<Person> i = unitManager.getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			Task task = person.getMind().getTaskManager().getTask();

			// Add all people maintaining this vehicle.
			if (task instanceof Maintenance) {
				if (((Maintenance) task).getEntity() == this) {
					if (!people.contains(person))
						people.add(person);
				}
			}

			// Add all people repairing this vehicle.
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
	 * Gets a collection of people who are available for social conversation in this
	 * vehicle
	 * 
	 * @return person collection
	 */
	public Collection<Person> getTalkingPeople() {
		Collection<Person> people = new ConcurrentLinkedQueue<>();

		// Check all people.
		Iterator<Person> i = unitManager.getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			Task task = person.getMind().getTaskManager().getTask();

			// Add all people having conversation from all places as the task
			if (task instanceof HaveConversation)
				if (!people.contains(person))
					people.add(person);

			// Add all people ready for switching to having conversation as task in this
			// vehicle.
//			 if (task instanceof Relax)
//			 if (!people.contains(person))
//			 people.add(person);
		}

		return people;
	}

	/**
	 * Gets a collection of robots affected by this entity.
	 * 
	 * @return robots collection
	 */
	public Collection<Robot> getAffectedRobots() {
		Collection<Robot> robots = new ConcurrentLinkedQueue<>();

		// Check all robots.
		Iterator<Robot> i = unitManager.getRobots().iterator();
		while (i.hasNext()) {
			Robot robot = i.next();
			Task task = robot.getBotMind().getBotTaskManager().getTask();

			// Add all robots maintaining this vehicle.
			if (task instanceof Maintenance) {
				if (((Maintenance) task).getEntity() == this) {
					if (!robots.contains(robot))
						robots.add(robot);
				}
			}

			// Add all robots repairing this vehicle.
			if (task instanceof Repair) {
				if (((Repair) task).getEntity() == this) {
					if (!robots.contains(robot))
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
				trail.add(new Coordinates(location));
		} else
			trail.add(new Coordinates(location));
	}

	/**
	 * Gets the resource type that this vehicle uses for fuel.
	 * 
	 * @return resource type
	 */
	public abstract int getFuelType();

	/**
	 * Gets the estimated distance traveled in one sol.
	 * 
	 * @return distance traveled (km)
	 */
	public double getEstimatedTravelDistancePerSol() {
		// Get estimated average speed (km / hr).
//    	double estSpeed = baseSpeed / 2D;
		// Return estimated average speed in km / sol.
		return baseSpeed * ESTIMATED_NUM_HOURS; // 60D / 60D / MarsClock.convertSecondsToMillisols(1D) * 1000D;
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
	 * Indicate the start of a salvage process on the item.
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
	 * Set initial parked location and facing at settlement.
	 */
	public abstract void findNewParkingLoc();

	public void relocateVehicle() {

		Building b = getGarage();
		if (b != null) {
			b.getGroundVehicleMaintenance().removeVehicle(this);
		}
		else // Call findNewParkingLoc() in GroundVehicle
			findNewParkingLoc();
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

	@Override
	public Settlement getAssociatedSettlement() {
		return unitManager.getSettlementByID(associatedSettlementID);
	}
	
	@Override
	public String getImmediateLocation() {
		return getLocationTag().getImmediateLocation();
	}

	@Override
	public String getLocale() {
		return getLocationTag().getLocale();
	}

	/**
	 * Is the vehicle outside of a settlement but within its vicinity
	 * 
	 * @return
	 */
	public boolean isRightOutsideSettlement() {
        return getLocationStateType() == LocationStateType.WITHIN_SETTLEMENT_VICINITY;
	}

	/**
	 * Is the vehicle parked
	 * 
	 * @return 
	 */
	public boolean isParked() {
		if (haveStatusType(StatusType.MOVING)) {
			return false;
		} else if (haveStatusType(StatusType.TOWED)) {
			Vehicle towingVehicle = getTowingVehicle();
            return towingVehicle == null
                    || (!towingVehicle.haveStatusType(StatusType.MOVING)
                    && !towingVehicle.haveStatusType(StatusType.TOWING));
		}
		
		return true;
	}
	
	@Override
	public Building getBuildingLocation() {
		return this.getGarage();
	}

	/**
	 * Checks if this vehicle is involved in a mission
	 * 
	 * @return true if yes
	 */
	public Mission getMission() {
		Iterator<Mission> i = missionManager.getMissions().iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (!mission.isDone()) {
				if (mission instanceof VehicleMission) {
	
					if (((VehicleMission) mission).getVehicle() == this) {
						return mission;
					}

					
				} else if (mission instanceof BuildingConstructionMission) {
					BuildingConstructionMission construction = (BuildingConstructionMission) mission;
					if (construction.getConstructionVehicles() != null) {
						if (construction.getConstructionVehicles().contains(this)) {
							return mission;
						}
					}

				} else if (mission instanceof BuildingSalvageMission) {
					BuildingSalvageMission salvage = (BuildingSalvageMission) mission;
					if (salvage.getConstructionVehicles().contains(this)) {
						return mission;
					}
				}
			}
		}

		return null;
	}
	
	/**
	 * Checks if this vehicle is involved in a mission
	 * 
	 * @return true if yes
	 */
	public double getMissionRange(MissionType missiontype) {
		return getSettlement().getMissionRadius(missiontype);
	}
	
	/**
	 * Checks if this vehicle is involved in a mission
	 * 
	 * @return true if yes
	 */
	public boolean isOnAMission() {
		Iterator<Mission> i = missionManager.getMissions().iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (!mission.isDone()) {
				if (mission instanceof VehicleMission) {
					if (((VehicleMission) mission).getVehicle() == this) {
						return true;
					}

					if (mission instanceof Mining) {
						if (((Mining) mission).getLightUtilityVehicle() == this) {
							return true;
						}
					}

					if (mission instanceof Trade) {
						Rover towingRover = (Rover) ((Trade) mission).getVehicle();
						if (towingRover != null) {
							if (towingRover.getTowedVehicle() == this) {
								return true;
							}
						}
					}
					
					if (mission instanceof Delivery) {
						if (((Delivery) mission).getVehicle() == this) {
								return true;
						}
					}
					
				} else if (mission instanceof BuildingConstructionMission) {
					BuildingConstructionMission construction = (BuildingConstructionMission) mission;
					if (construction.getConstructionVehicles() != null) {
						if (construction.getConstructionVehicles().contains(this)) {
							return true;
						}
					}

				} else if (mission instanceof BuildingSalvageMission) {
					BuildingSalvageMission salvage = (BuildingSalvageMission) mission;
					if (salvage.getConstructionVehicles().contains(this)) {
						return true;
					}
				}
			}
		}

		return false;
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
			if (vehicle.haveStatusType(StatusType.MOVING)) {
				result = true;
			} else if (vehicle.haveStatusType(StatusType.TOWED)) {
				Vehicle towingVehicle = vehicle.getTowingVehicle();
				if (towingVehicle != null 
						&& (towingVehicle.haveStatusType(StatusType.MOVING) 
						|| towingVehicle.haveStatusType(StatusType.TOWING))) {
					result = true;
				}
			}
		}

		return result;
	}
	
	/**
	 * Gets the specific base wear life time of this vehicle (in msols)
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
	
//	/**
//	 * Finds the string name of the amount resource
//	 * 
//	 * @param resource
//	 * @return resource string name
//	 */
//	@Override
//	public String findAmountResourceName(int resource) {
//		return ResourceUtil.findAmountResourceName(resource);
//	}
//	
//	/**
//	 * Finds the string name of the item resource
//	 * 
//	 * @param resource
//	 * @return resource string name
//	 */
//	@Override
//	public String findItemResourceName(int resource) {
//		return ItemResourceUtil.findItemResourceName(resource);
//	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Vehicle v = (Vehicle) obj;
		return this.getName().equals(v.getName())
				&& this.getIdentifier() == v.getIdentifier()
				&& this.vehicleTypeString.equals(v.getVehicleTypeString())
				&& this.associatedSettlementID == v.getAssociatedSettlementID();
	}
	
	/**
	 * Gets the hash code value.
	 * 
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		int hashCode = getName().hashCode();
		hashCode *= getIdentifier();
		hashCode *= associatedSettlementID;
		hashCode *= vehicleTypeString.hashCode();
		return hashCode;
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

	/**
	 * Generate a new name for the Vehcile; potentially this may be a preconfigured name
	 * or an auto-generated one.
	 * @param type
	 * @param sponsor Sponsor.
	 * @return
	 */
	public static String generateName(String type, ReportingAuthority sponsor) {
		String result = null;
		String baseName = type;
		
		if (type != null && type.equalsIgnoreCase(LightUtilityVehicle.NAME)) {
			baseName = "LUV";
		}
		else if (type != null && type.equalsIgnoreCase(VehicleType.DELIVERY_DRONE.getName())) {
			baseName = "Drone";
		}
		else {
			List<String> possibleNames = sponsor.getVehicleNames();
			if (!possibleNames.isEmpty()) {
				List<String> availableNames = new ArrayList<>(possibleNames);
				Collection<Vehicle> vehicles = unitManager.getVehicles();
				List<String> usedNames = vehicles.stream()
								.map(Vehicle::getName).collect(Collectors.toList());
				availableNames.removeAll(usedNames);
				
				if (!availableNames.isEmpty()) {
					result = availableNames.get(RandomUtil.getRandomInt(availableNames.size() - 1));
				} 			
			}
		}

		if (result == null) {
			int number = unitManager.incrementTypeCount(type);
			result = String.format(VEHICLE_TAG_NAME, baseName, number);
		}
		return result;
	}
}