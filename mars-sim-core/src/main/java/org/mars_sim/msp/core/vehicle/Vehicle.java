/**
 * Mars Simulation Project
 * Vehicle.java
 * @version 3.1.0 2017-08-08
 * @author Scott Davis
 */

package org.mars_sim.msp.core.vehicle;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.HaveConversation;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.taskUtil.Task;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.Indoor;
import org.mars_sim.msp.core.structure.building.function.SystemType;

/**
 * The Vehicle class represents a generic vehicle. It keeps track of generic
 * information about the vehicle. This class needs to be subclassed to represent
 * a specific type of vehicle.
 */
public abstract class Vehicle extends Unit
		implements Malfunctionable, Salvagable, Indoor, LocalBoundedObject, Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(Vehicle.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());

	/** The error margin for determining vehicle range. (Actual distance / Safe distance). */
	private static double fuel_range_error_margin = SimulationConfig.instance().getSettlementConfiguration()
			.loadMissionControl()[0];
	private static double life_support_range_error_margin = SimulationConfig.instance().getSettlementConfiguration()
			.loadMissionControl()[1];

	// For Methane : 
	// Specific energy is 55.5	MJ/kg, or 15,416 Wh/kg, or 15.416kWh / kg
	// Energy density is 0.0364 MJ/L, 36.4 kJ/L or 10 Wh/L
	// Note : 1 MJ = 0.277778 kWh; 1 kWh = 3.6 MJ
	// as comparison, 1 gallon (or 3.7854 L) of gasoline (which, for the record, it says is 33.7 kilowatt-hours) +> 8.9 kWh / L
	
	/** The specific energy of CH4 [kWh/kg] */
	private static final double METHANE_SPECIFIC_ENERGY = 15.416D;
	/** The Solid Oxide Fuel Cell Conversion Efficiency (dimension-less) */
	public static final double SOFC_CONVERSION_EFFICIENCY = .57;
	/** Lifetime Wear in millisols **/
	private static final double WEAR_LIFETIME = 668000D; // 668 Sols (1 orbit)
	/** The unit count for this person. */
	private static int uniqueCount = Unit.FIRST_VEHICLE_ID;
	
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
	
	/** Unique identifier for this vehicle. */
	private int identifier;
	/** Vehicle's associated Settlement. */
	private int associatedSettlementID;
	
	/** Current speed of vehicle in kph. */
	private double speed = 0; // 
	/** Base speed of vehicle in kph (can be set in child class). */
	private double baseSpeed = 0; // 
	/** The base range of the vehicle (with full tank of fuel and no cargo) (km). */
	private double baseRange = 0;
	/** Total distance traveled by vehicle (km). */
	private double distanceTraveled = 0; // 
	/** Distance traveled by vehicle since last maintenance (km) . */
	private double distanceMaint = 0; // 
	/** The efficiency of the vehicle's drivetrain. (kWh/km). */
	private double drivetrainEfficiency; // 
	/** The maximum fuel capacity of the vehicle [kg] */
	private double fuelCapacity;
	/** The total energy of the vehicle in full tank [kWh]. */
	private double totalEnergy = 100D;
	/** The base fuel consumption of the vehicle [km/kg]. */
	private double baseFuelConsumption;
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
	
	/** The vehicle type. */	
	private String vehicleType;
	/** The type of dessert loaded. */	
	private String typeOfDessertLoaded;
	
	/** A collection of locations that make up the vehicle's trail. */
	private ArrayList<Coordinates> trail;
	/** List of operator activity spots. */
	private List<Point2D> operatorActivitySpots;
	/** List of passenger activity spots. */
	private List<Point2D> passengerActivitySpots;
	
	/** The vehicle's status. */
	private StatusType statusType; 
	/** The malfunction manager for the vehicle. */
	protected MalfunctionManager malfunctionManager; 
	/** Direction vehicle is traveling */
	private Direction direction;
	/** The operator of the vehicle. */
	private VehicleOperator vehicleOperator;
	/** he vehicle that is currently towing this vehicle. */
	private Vehicle towingVehicle;
	/** The The vehicle's salvage info. */
	private SalvageInfo salvageInfo; 
	/** The vehicle's status log. */
	private Map<Integer, Map<Integer, List<StatusType>>> vehicleLog = new HashMap<>();
	
	// Static members
//	private static VehicleConfig vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();

	static {
		life_support_range_error_margin = SimulationConfig.instance().getSettlementConfiguration()
				.loadMissionControl()[0];
		fuel_range_error_margin = SimulationConfig.instance().getSettlementConfiguration().loadMissionControl()[1];
	}
	
	/**
	 * Must be synchronised to prevent duplicate ids being assigned via different
	 * threads.
	 * 
	 * @return
	 */
	private static synchronized int getNextIdentifier() {
		return uniqueCount++;
	}
	
	/**
	 * Get the unique identifier for this person
	 * 
	 * @return Identifier
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	/**
	 * Constructor 1 : prepares a Vehicle object with a given settlement
	 * 
	 * @param name                the vehicle's name
	 * @param vehicleType         the configuration description of the vehicle.
	 * @param settlement          the settlement the vehicle is parked at.
	 * @param maintenanceWorkTime the work time required for maintenance (millisols)
	 */
	Vehicle(String name, String vehicleType, Settlement settlement, double maintenanceWorkTime) {
		// Use Unit constructor
		super(name, settlement.getCoordinates());
		
		if (unitManager == null)
			unitManager = sim.getUnitManager();
		
		this.identifier = getNextIdentifier();

		unitManager.addVehicleID(this);
		
		// Place this person within a settlement
//		enter(LocationCodeType.SETTLEMENT);
			
		this.vehicleType = vehicleType;
		associatedSettlementID = settlement.getIdentifier();
//		containerUnit = settlement;
		setContainerID(associatedSettlementID);
		settlement.getInventory().storeUnit(this);

//		missionManager = Simulation.instance().getMissionManager();
//		vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();

	
		// Initialize vehicle data
		vehicleType = vehicleType.toLowerCase();

		direction = new Direction(0);
		trail = new ArrayList<Coordinates>();

		isReservedMission = false;
		distanceMark = false;
		reservedForMaintenance = false;
		emergencyBeacon = false;
		isSalvaged = false;

		// Initialize malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, maintenanceWorkTime);
		malfunctionManager.addScopeString(SystemType.VEHICLE.getName());// "Vehicle");

		setStatus(StatusType.PARKED);
		
		// Set width and length of vehicle.
		width = vehicleConfig.getWidth(vehicleType);
		length = vehicleConfig.getLength(vehicleType);

		setDescription(vehicleType);
//		setDescription(vehicleConfig.getDescription(vehicleType));
		
		// Set base speed.
		setBaseSpeed(vehicleConfig.getBaseSpeed(vehicleType));

		// Set the empty mass of the vehicle.
		setBaseMass(vehicleConfig.getEmptyMass(vehicleType));

		// Set the drivetrain efficiency of the vehicle.
		drivetrainEfficiency = vehicleConfig.getDrivetrainEfficiency(vehicleType) ;
		
		// Gets the capacity of vehicle's fuel tank 
		fuelCapacity = vehicleConfig.getCargoCapacity(vehicleType, ResourceUtil.findAmountResourceName(getFuelType())); 
//		System.out.println("fuelCapacity: " + fuelCapacity);
		// Gets the total energy on a full tank of methane
		totalEnergy = METHANE_SPECIFIC_ENERGY * fuelCapacity * SOFC_CONVERSION_EFFICIENCY;// * drivetrainEfficiency;	

		// Gets the base range of the vehicle
		baseRange = totalEnergy / drivetrainEfficiency;
		
		// Gets the fuel consumption of this vehicle [km/kg]
		baseFuelConsumption = baseRange / fuelCapacity; 
		
		// Set initial parked location and facing at settlement.
		determinedSettlementParkedLocationAndFacing();

		// Initialize operator activity spots.
		operatorActivitySpots = new ArrayList<Point2D>(vehicleConfig.getOperatorActivitySpots(vehicleType));

		// Initialize passenger activity spots.
		passengerActivitySpots = new ArrayList<Point2D>(vehicleConfig.getPassengerActivitySpots(vehicleType));
	}

	/**
	 * Constructor 2 : prepares a Vehicle object for testing (called by MockVehicle)
	 * 
	 * @param name                the vehicle's name
	 * @param vehicleType         the configuration description of the vehicle.
	 * @param settlement          the settlement the vehicle is parked at.
	 * @param baseSpeed           the base speed of the vehicle (kph)
	 * @param baseMass            the base mass of the vehicle (kg)
	 * @param fuelEfficiency      the fuel efficiency of the vehicle (km/kg)
	 * @param maintenanceWorkTime the work time required for maintenance (millisols)
	 */
	protected Vehicle(String name, String vehicleType, Settlement settlement, double baseSpeed, double baseMass,
			double fuelEfficiency, double maintenanceWorkTime) {

		// Use Unit constructor
		super(name, settlement.getCoordinates());
		
		if (unitManager == null)
			unitManager = sim.getUnitManager();
		
		this.identifier = getNextIdentifier();
		
		if (unitManager != null) // for passing maven test
			unitManager.addVehicleID(this);
		
		// Place this person within a settlement
//		enter(LocationCodeType.SETTLEMENT);
		
		this.vehicleType = vehicleType;

		associatedSettlementID = settlement.getIdentifier();
//		containerUnit = settlement;
		setContainerID(associatedSettlementID);
		settlement.getInventory().storeUnit(this);

		// Initialize vehicle data
		setDescription(vehicleType);
		direction = new Direction(0);
		trail = new ArrayList<Coordinates>();
		setBaseSpeed(baseSpeed);
		setBaseMass(baseMass);
		this.drivetrainEfficiency = fuelEfficiency / 100.0;
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
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, maintenanceWorkTime);
		malfunctionManager.addScopeString(SystemType.VEHICLE.getName());// "Vehicle");
		
		setStatus(StatusType.PARKED);
	}

	public String getDescription(String vehicleType) {
		return vehicleConfig.getDescription(vehicleType);
	}

	public String getVehicleType() {
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
			result = new HashMap<Person, Point2D>(crewable.getCrewNum());
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
			result = new HashMap<Robot, Point2D>(crewable.getRobotCrewNum());
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
	 * Returns vehicle's current status
	 * 
	 * @return the vehicle's current status
	 */
	public StatusType getStatus() {
		return statusType;
	}

	/**
	 * Sets the status type of a vehicle
	 * 
	 * @param type
	 */
	public void setStatus(StatusType newStatus) {
		// Update status based on current situation.		
		if (statusType != newStatus) {
			statusType = newStatus;
			writeLog(newStatus);
			fireUnitUpdate(UnitEventType.STATUS_EVENT, newStatus);
		}
	}
	
	/**
	 * Checks the vehicle's status.
	 */
	private void checkStatus() {
		// Update status based on current situation.
		StatusType newStatus = null;
		if (getGarage() != null)
			newStatus = StatusType.GARAGED;
		else
			newStatus = StatusType.PARKED;
		
		if (speed > 0D)
			newStatus = StatusType.MOVING;
		else if (towingVehicle != null)
			newStatus = StatusType.TOWED;
		else if (reservedForMaintenance)
			newStatus = StatusType.MAINTENANCE;
		else if (malfunctionManager.hasMalfunction())
			newStatus = StatusType.MALFUNCTION;

		setStatus(newStatus);
	}
	
	/**
	 * Records the status in the vehicle log 
	 * 
	 * @param type
	 */
	public void writeLog(StatusType type) {
		int today = marsClock.getMissionSol();
		int millisols = marsClock.getMillisolInt();
		
		Map<Integer, List<StatusType>> eachSol = null;
		List<StatusType> list = null;
		
		if (vehicleLog.containsKey(today)) {
			eachSol = vehicleLog.get(today);
					
			if (eachSol.containsKey(millisols)) {
				list = eachSol.get(millisols);
			}
			else {
				list = new ArrayList<>();
			}
		}
		
		else {
			eachSol = new HashMap<>();
			list = new ArrayList<>();
		}
		
		list.add(type);
		eachSol.put(millisols, list);
		vehicleLog.put(today, eachSol);
//		System.out.println(getName() + " log's size : " + vehicleLog.size());
	}

	public Map<Integer, Map<Integer, List<StatusType>>> getVehicleLog() {
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
		if (towingVehicle == null)
			return false;
		else
			return true;
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
	 * Sets the vehicle's current speed
	 * 
	 * @param speed the vehicle's speed (in km/hr)
	 */
	public void setSpeed(double speed) {
		if (speed < 0D)
			throw new IllegalArgumentException("Vehicle speed cannot be less than 0 km/hr: " + speed);
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

	/**
	 * Sets the base speed of vehicle
	 * 
	 * @param speed the vehicle's base speed (in km/hr)
	 */
	public void setBaseSpeed(double speed) {
		if (speed < 0D)
			throw new IllegalArgumentException("Vehicle base speed cannot be less than 0 km/hr");
		baseSpeed = speed;
	}

	/**
	 * Gets the current range of the vehicle
	 * 
	 * @return the current range of the vehicle (in km)
	 * @throws Exception if error getting range.
	 */
	public double getRange() {
		return totalEnergy * (getBaseMass() + fuelCapacity) / (getMass() + fuelCapacity);// / fuel_range_error_margin;
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
	 * Gets the base fuel consumption of the vehicle [km/kg].
	 * @return
	 */
	public double getBaseFuelConsumption() {
		return baseFuelConsumption;
	}

	/**
	 * Gets the instantaneous fuel consumption of the vehicle [km/kg] 
	 * (Depending on the current weight of the vehicle)
	 * @return
	 */
	public double getIFuelConsumption() {
		return baseFuelConsumption * (getMass() + fuelCapacity) / (getBaseMass() + fuelCapacity) ;
	}
	
	/**
	 * Gets the drivetrain efficiency of the vehicle.
	 * 
	 * @return drivetrain efficiency [km/kg]
	 */
	public double getDrivetrainEfficiency() {
		return drivetrainEfficiency;
	}

	/**
	 * Returns total distance traveled by vehicle (in km.)
	 * 
	 * @return the total distanced traveled by the vehicle (in km)
	 */
	public double getTotalDistanceTraveled() {
		return distanceTraveled;
	}

	/**
	 * Adds a distance (in km.) to the vehicle's total distance traveled
	 * 
	 * @param distance distance to add to total distance traveled (in km)
	 */
	public void addTotalDistanceTraveled(double distance) {
		distanceTraveled += distance;
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
	 * Gets the operator of the vehicle (person or AI)
	 * 
	 * @return the vehicle operator
	 */
	public VehicleOperator getOperator() {
		return vehicleOperator;
	}

	/**
	 * Sets the operator of the vehicle
	 * 
	 * @param vehicleOperator the vehicle operator
	 */
	public void setOperator(VehicleOperator vehicleOperator) {
		this.vehicleOperator = vehicleOperator;
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
	 * @param time the amount of time passing (millisols)
	 * @throws Exception if error during time.
	 */
	public void timePassing(double time) {

		// Checks status.
		checkStatus();
		
		if (statusType == StatusType.MOVING)
			malfunctionManager.activeTimePassing(time);
		// Make sure reservedForMaintenance is false if vehicle needs no maintenance.
		else if (statusType == StatusType.MAINTENANCE) {
			if (malfunctionManager.getEffectiveTimeSinceLastMaintenance() <= 0D)
				setReservedForMaintenance(false);
		} else {
			malfunctionManager.timePassing(time);
		}

		addToTrail(getCoordinates());

		correctVehicleReservation();

		// If operator is dead, remove operator and stop vehicle.
		VehicleOperator operator = vehicleOperator;
		if ((operator != null) && (operator instanceof Person)) {
			Person personOperator = (Person) operator;
			if (personOperator.getPhysicalCondition().isDead()) {
				setOperator(null);
				setSpeed(0);
				setParkedLocation(0D, 0D, getDirection().getDirection());
			}
			// TODO : will another person take his place as the driver
		}

	}

	/**
	 * Resets the vehicle reservation status
	 */
	public void correctVehicleReservation() {
		if (isReservedMission) {
			// Set reserved for mission to false if the vehicle is not associated with a
			// mission.
			if (missionManager.getMissionForVehicle(this) == null) {
				LogConsolidated.log(Level.FINE, 500, sourceName,
						"[" + getLocationTag().getLocale() + "] " + getName() 
						+ " was found reserved for an non-existing mission. Untagging it.");
				setReservedForMission(false);
			}
		} else {
			if (missionManager.getMissionForVehicle(this) != null) {
				LogConsolidated.log(Level.FINE, 500, sourceName,
						"[" + getLocationTag().getLocale() + "] " + getName()
						+ " is on a mission but is not registered as mission reserved. Correcting it.");
			}
		}
	}
	
	/**
	 * Gets a collection of people affected by this entity.
	 * 
	 * @return person collection
	 */
	public Collection<Person> getAffectedPeople() {
		Collection<Person> people = new ConcurrentLinkedQueue<Person>();

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
		Collection<Person> people = new ConcurrentLinkedQueue<Person>();

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
		Collection<Robot> robots = new ConcurrentLinkedQueue<Robot>();

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
		return baseSpeed * 16; // 60D / 60D / MarsClock.convertSecondsToMillisols(1D) * 1000D;
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
	public abstract void determinedSettlementParkedLocationAndFacing();

	public String getTypeOfDessertLoaded() {
		return typeOfDessertLoaded;
	}

	public void setTypeOfDessertLoaded(String dessertName) {
		typeOfDessertLoaded = dessertName;
	}

	public static double getErrorMargin() {
		return fuel_range_error_margin;
	}

	public static double getLifeSupportRangeErrorMargin() {
		return life_support_range_error_margin;
	}

	public int getAssociatedSettlementID() {
		return associatedSettlementID;
	}

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
		if (getLocationStateType() == LocationStateType.OUTSIDE_SETTLEMENT_VICINITY)
			return true;
		else
			return false;
	}

	/**
	 * Is the vehicle parked inside or right outside of a settlement but within its vicinity
	 * 
	 * @return 
	 */
	public boolean isParked() {
		if (getLocationStateType() == LocationStateType.OUTSIDE_SETTLEMENT_VICINITY
				|| getLocationStateType() == LocationStateType.INSIDE_SETTLEMENT)
			return true;
		else
			return false;
	}
	
	@Override
	public Building getBuildingLocation() {
		return this.getGarage();
	}

	@Override
	public Unit getUnit() {
		return this;
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
				} else if (mission instanceof BuildingConstructionMission) {
					BuildingConstructionMission construction = (BuildingConstructionMission) mission;
					if (construction.getConstructionVehicles() != null) {
						if (construction.getConstructionVehicles().contains(this)) {
							return true;
						}
					}
					// else {
					// result = null;
					// }
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
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Vehicle v = (Vehicle) obj;
		return this.getName().equals(v.getName())
				&& this.identifier == v.getIdentifier()
				&& this.vehicleType.equals(v.getVehicleType())
				&& this.associatedSettlementID == v.getAssociatedSettlementID();
	}
	
	/**
	 * Gets the hash code value.
	 * 
	 * @return hash code
	 */
	public int hashCode() {
		int hashCode = getName().hashCode();
		hashCode *= identifier;
		hashCode *= associatedSettlementID;
		hashCode *= vehicleType.hashCode();
		return hashCode;
	}
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param mgr
	 */
	public static void initializeInstances() {
		vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();
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
		statusType = null;
		if (salvageInfo != null)
			salvageInfo.destroy();
		salvageInfo = null;
	}
}