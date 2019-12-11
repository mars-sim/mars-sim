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
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.AreologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BiologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupply;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.MeteorologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.ExitAirlock;
import org.mars_sim.msp.core.person.ai.task.HaveConversation;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.RequestMedicalTreatment;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.Indoor;
import org.mars_sim.msp.core.structure.building.function.SystemType;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Vehicle class represents a generic vehicle. It keeps track of generic
 * information about the vehicle. This class needs to be subclassed to represent
 * a specific type of vehicle.
 */
public abstract class Vehicle extends Unit
		implements Malfunctionable, Salvagable, Indoor, LocalBoundedObject, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(Vehicle.class.getName());
	private static final String loggerName = logger.getName();
	private static final String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());

	/** The error margin for determining vehicle range. (Actual distance / Safe distance). */
	private static double fuel_range_error_margin;// = SimulationConfig.instance().getSettlementConfiguration().loadMissionControl()[0];
	private static double life_support_range_error_margin;// = SimulationConfig.instance().getSettlementConfiguration().loadMissionControl()[1];

	// For Methane : 
	// Specific energy is 55.5	MJ/kg, or 15,416 Wh/kg, or 15.416kWh / kg
	// Energy density is 0.0364 MJ/L, 36.4 kJ/L or 10 Wh/L
	// Note : 1 MJ = 0.277778 kWh; 1 kWh = 3.6 MJ
	// as comparison, 1 gallon (or 3.7854 L) of gasoline (which, for the record, it says is 33.7 kilowatt-hours) +> 8.9 kWh / L
	
	/** The specific energy of CH4 [kWh/kg] */
	private static final double METHANE_SPECIFIC_ENERGY = 15.416D;
	/** The Solid Oxide Fuel Cell Conversion Efficiency (dimension-less) */
	public static final double SOFC_CONVERSION_EFFICIENCY = .65;
	/** Lifetime Wear in millisols **/
	private static final double WEAR_LIFETIME = 668_000; // 668 Sols (1 orbit)
	/** Estimated Number of hours traveled each day. **/
	private static final int ESTIMATED_NUM_HOURS = 16;
	/** The scope name for Light Utility Vehicle.  **/
//	private static final String LUV = "LUV";
	
	/** The unit count for this person. */
	private static int uniqueCount = Unit.FIRST_VEHICLE_UNIT_ID;
	
	/** The types of status types that make a vehicle unavailable for us. */
	private static final List<StatusType> badStatus = Arrays.asList(StatusType.MAINTENANCE, StatusType.TOWED, StatusType.MOVING,
			StatusType.STUCK, StatusType.MALFUNCTION);
	
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
	private double distanceTraveled; // 
	/** Distance traveled by vehicle since last maintenance (km) . */
	private double distanceMaint; // 
	/** The efficiency of the vehicle's drivetrain. (kWh/km). */
	private double drivetrainEfficiency;
	/** The continuous motor power output of the vehicle. (kW). */
	private double continuousPower = 8;
	/** The total number of hours the vehicle is capable of operating. (kW). */
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
	/** List of status types. */
	private List<StatusType> statusTypes;
	/** The vehicle's status log. */
	private Map<Integer, Map<Integer, List<StatusType>>> vehicleLog = new HashMap<>();
	
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
				.loadMissionControl()[0];
		fuel_range_error_margin = simulationConfig.getSettlementConfiguration().loadMissionControl()[1];
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
	
	public void incrementID() {
		// Gets the identifier
		this.identifier = getNextIdentifier();
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
		
		// Add this vehicle to the lookup map
		unitManager.addVehicleID(this);
		
		this.vehicleType = vehicleType;
		
		// Obtain the associated settlement ID 
		associatedSettlementID = settlement.getIdentifier();

		// Add this vehicle to be owned by the settlement
		settlement.addOwnedVehicle(this);

		// Store this vehicle in the settlement
		settlement.getInventory().storeUnit(this);
	
		// Initialize vehicle data
		vehicleType = vehicleType.toLowerCase();

		direction = new Direction(0);
		trail = new ArrayList<Coordinates>();
		statusTypes = new ArrayList<>();
		
		isReservedMission = false;
		distanceMark = false;
		reservedForMaintenance = false;
		emergencyBeacon = false;
		isSalvaged = false;

		// Initialize malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, maintenanceWorkTime);
		
		// Add "vehicle" as scope
		malfunctionManager.addScopeString(SystemType.VEHICLE.getName());
		
		// Add its vehicle type as scope
		malfunctionManager.addScopeString(vehicleType);
		
		if (!vehicleType.equals(LightUtilityVehicle.class.getSimpleName())) {
			// Add "rover" as scope
			malfunctionManager.addScopeString(SystemType.ROVER.getName());
		}
		
		addStatus(StatusType.PARKED);
		
		// Set width and length of vehicle.
		width = vehicleConfig.getWidth(vehicleType);
		length = vehicleConfig.getLength(vehicleType);

		// Set description
		setDescription(vehicleType);
		// Set total distance traveled by vehicle (km)
		distanceTraveled = 0;
		// Set distance traveled by vehicle since last maintenance (km)
		distanceMaint = 0;
		// Set base speed.
		baseSpeed = vehicleConfig.getBaseSpeed(vehicleType);
//		setBaseSpeed(baseSpeed);

		// Set the empty mass of the vehicle.
		setBaseMass(vehicleConfig.getEmptyMass(vehicleType));

		// Set the drivetrain efficiency [in kWh/km] of the vehicle.
		drivetrainEfficiency = vehicleConfig.getDrivetrainEfficiency(vehicleType) ;
		
		// Gets the capacity [in kg] of vehicle's fuel tank 
		fuelCapacity = vehicleConfig.getCargoCapacity(vehicleType, ResourceUtil.findAmountResourceName(getFuelType())); 

		// Gets the total energy [in kWh] on a full tank of methane
		totalEnergy = METHANE_SPECIFIC_ENERGY * fuelCapacity * SOFC_CONVERSION_EFFICIENCY;

		// Gets the maximum total # of hours the vehicle is capable of operating
		totalHours = totalEnergy / continuousPower;
		
		// Gets the average number of sols the vehicle is capable of operating
//		double sols = totalHours / ESTIMATED_NUM_HOURS;
		
		// Gets the base range [in km] of the vehicle
		baseRange = baseSpeed * totalHours;
//		baseRange = totalEnergy / drivetrainEfficiency;
		
		// Gets the base fuel economy [in km/kg] of this vehicle 
		baseFuelEconomy = baseRange / fuelCapacity;
		
		// Gets the base fuel consumption [in km/kWh] of this vehicle 
		baseFuelConsumption = baseRange / totalEnergy;
		
		// Gets the crew capacity
		int numCrew = 0; 
//		if (this instanceof Rover)
			numCrew = vehicleConfig.getCrewSize(vehicleType);//((Rover)this).getCrewCapacity();
//		else if (this instanceof LightUtilityVehicle)
//			numCrew = vehicleConfig.getCrewSize(type);//Vehicle.((LightUtilityVehicle)this).getCrewCapacity();
		
		estimatedTotalCrewWeight = numCrew * Person.getAverageWeight();
		
		cargoCapacity = vehicleConfig.getTotalCapacity(vehicleType);
		
		if (this instanceof Rover) {
			beginningMass = getBaseMass() + estimatedTotalCrewWeight + 500;	//cargoCapacity/3;
			
			// Accounts for the rock sample, ice or regolith collected
			endMass = getBaseMass() + estimatedTotalCrewWeight + 1000;	//cargoCapacity/15;
			
			// Gets the estimated average fuel economy for a trip [km/kg]
			estimatedAveFuelEconomy = baseFuelEconomy * (beginningMass / endMass * .75);
			
//			logger.config(Conversion.capitalize(vehicleType) 
//					+ " -          total energy : " + Math.round(totalEnergy*100.0)/100.0 + " kWh");
//			logger.config(Conversion.capitalize(vehicleType) 
//					+ " -           total hours : " + Math.round(totalHours*100.0)/100.0 + " hrs");
//			logger.config(Conversion.capitalize(vehicleType) 
//					+ " -             # of sols : " + Math.round(sols*100.0)/100.0);
//			logger.config(Conversion.capitalize(vehicleType) 
//					+ " -            base range : " + Math.round(baseRange*100.0)/100.0 + " km");
//			logger.config(Conversion.capitalize(vehicleType) 
//					+ " - drivetrain efficiency : " + Math.round(drivetrainEfficiency*100.0)/100.0 + " kWh/km");
//			logger.config(Conversion.capitalize(vehicleType) 
//					+ " -        beginning mass : " + Math.round(beginningMass*100.0)/100.0 + " kg");
//			logger.config(Conversion.capitalize(vehicleType) 
//					+ " -              end mass : " + Math.round(endMass*100.0)/100.0 + " kg");		
//			logger.config(Conversion.capitalize(vehicleType) 
//					+ " -          current Mass : " + Math.round(getMass()*100.0)/100.0 + " kg");	
//			logger.config(Conversion.capitalize(vehicleType) 
//					+ " -     base fuel economy : " + Math.round(baseFuelEconomy*100.0)/100.0 + " km/kg");
//			logger.config(Conversion.capitalize(vehicleType) 
//					+ " -  average fuel economy : " + Math.round(estimatedAveFuelEconomy*100.0)/100.0 + " km/kg");
//			logger.config(Conversion.capitalize(vehicleType) 
//					+ " -  base fuel cosumption : " + Math.round(baseFuelConsumption*100.0)/100.0 + " km/kWh");
			
		}
		
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
	 * @param fuelEconomy		  the fuel economy of the vehicle (km/kg)
	 * @param maintenanceWorkTime the work time required for maintenance (millisols)
	 */
	protected Vehicle(String name, String vehicleType, Settlement settlement, double baseSpeed, double baseMass,
			double fuelEconomy, double maintenanceWorkTime) {

		// Use Unit constructor
		super(name, settlement.getCoordinates());
		
		if (unitManager == null)
			unitManager = sim.getUnitManager();
		
//		this.identifier = getNextIdentifier();
		
		if (unitManager != null) // for passing maven test
			unitManager.addVehicleID(this);
		
		// Place this person within a settlement
//		enter(LocationCodeType.SETTLEMENT);
		
		this.vehicleType = vehicleType;

		associatedSettlementID = settlement.getIdentifier();
//		containerUnit = settlement;
		setContainerID(associatedSettlementID);
		settlement.getInventory().storeUnit(this);

		direction = new Direction(0);
		trail = new ArrayList<Coordinates>();
		statusTypes = new ArrayList<>();
		
		// Set description
		setDescription(vehicleType);
		// Set total distance traveled by vehicle (km)
		distanceTraveled = 0;
		// Set distance traveled by vehicle since last maintenance (km)
		distanceMaint = 0;
		// Set base speed.
		this.baseSpeed = baseSpeed;
//		setBaseSpeed(baseSpeed);

		// Set the empty mass of the vehicle.
		setBaseMass(baseMass);
		
		this.drivetrainEfficiency = .15;
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
		
		addStatus(StatusType.PARKED);
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
	 * Returns a list of vehicle's status types
	 * 
	 * @return the vehicle's status types
	 */
	public List<StatusType> getStatusTypes() {
		return statusTypes;
	}

	public boolean sameStatusTypes(List<StatusType> st1, List<StatusType> st2) {
		Set<StatusType> s1 = new HashSet<>(st1);
		Set<StatusType> s2 = new HashSet<>(st2);
		if (s1.equals(s2))
			return true;
		
		return false;
	}
	
	/**
	 * Prints a string list of status types
	 * 
	 * @return
	 */
	public String printStatusTypes() {
		String s = "";
		int size = statusTypes.size();
		if (size == 0)
			return s;
		else if (size == 1) {
			s = statusTypes.get(0).getName();
		}
		else if (size > 1) {
			for (int i=0; i<size; i++) {
				s += statusTypes.get(i).getName();
				if (i != size - 1)
					s += ", ";
			}
		}
		
		return s.trim();
	}
	
	/**
	 * Checks if this vehicle has already been tagged with a status type
	 * 
	 * @param status the status type of interest
	 * @return yes if it has it
	 */
	public boolean haveStatusType(StatusType status) {
		if (statusTypes.contains(status))
			return true;
		
		return false;
	}
	
	/**
	 * Checks if this vehicle has already been tagged with anyone of the provided status types
	 * 
	 * @param status a variable number of the status type of interest
	 * @return yes if it has anyone of them
	 */
	public boolean haveStatusTypes(StatusType... statuses) {
	    for (StatusType st : statuses) {
			if (statusTypes.contains(st))
				return true;
	    }
	    
	    return false;
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
	 * @param type
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
	 * @param type
	 */
	public void removeStatus(StatusType newStatus) {
		// Update status based on current situation.
		if (statusTypes.contains(newStatus)) {
			statusTypes.remove(newStatus);
			writeLog();
			fireUnitUpdate(UnitEventType.STATUS_EVENT, newStatus);
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
				removeStatus(StatusType.MOVING);
//				removeStatus(StatusType.TOWED);
			}
			else {
				addStatus(StatusType.PARKED);
				removeStatus(StatusType.GARAGED);
				removeStatus(StatusType.MOVING);
//				removeStatus(StatusType.TOWED);
			}
		}
		
		if (speed > 0D) {
			addStatus(StatusType.MOVING);
			removeStatus(StatusType.GARAGED);
			removeStatus(StatusType.PARKED);
		}
		
		if (towingVehicle != null) {
			addStatus(StatusType.TOWED);
//			removeStatus(StatusType.GARAGED);
//			removeStatus(StatusType.PARKED);
		}
		else {
			removeStatus(StatusType.TOWED);
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
	 * 
	 * @param type
	 */
	public void writeLog() {
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
		
		list.addAll(statusTypes);
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
	 * @param missionType the type of mission (needed in Rover's getRange())
	 * @return the current fuel range of the vehicle (in km)
	 */
	public double getRange(MissionType missionType) {
		Inventory vInv = getInventory();
        int fuelType = getFuelType();
        double amountOfFuel = vInv.getAmountResourceStored(fuelType, false);
		if (amountOfFuel > 0)
			return estimatedAveFuelEconomy * amountOfFuel * getBaseMass() / getMass();// / fuel_range_error_margin;
		else
			return estimatedAveFuelEconomy * fuelCapacity * getBaseMass() / getMass();// / fuel_range_error_margin;
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
	public double getEstimatedAveFuelConsumption() {
		return estimatedAveFuelEconomy;
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
	 * Adds a distance ]in km] to the vehicle's odometer (total distance traveled)
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
		
		if (haveStatusType(StatusType.MOVING)) {
			// Assume the wear and tear factor is at 100% by being used in a mission
			malfunctionManager.activeTimePassing(time);
		}
		
		// If it's back at a settlement and is NOT in a garage
		if (getSettlement() != null && !isRoverInAGarage()) {
			// Assume the wear and tear factor is 75% less by being exposed outdoor
			malfunctionManager.activeTimePassing(time * .25);
		}
		
		// Make sure reservedForMaintenance is false if vehicle needs no maintenance.
		if (haveStatusType(StatusType.MAINTENANCE)) {
			if (malfunctionManager.getEffectiveTimeSinceLastMaintenance() <= 0D) {
				setReservedForMaintenance(false);
				removeStatus(StatusType.MAINTENANCE);
			}
		}
		else {
			// Note: during maintenance, it doesn't need to be checking for malfunction.
			malfunctionManager.timePassing(time);
		}

		if (haveStatusType(StatusType.MALFUNCTION)) {
			if (malfunctionManager.getMalfunctions().size() == 0)
				removeStatus(StatusType.MALFUNCTION);
		}
				
//		if (haveStatusType(StatusType.OUT_OF_FUEL)
//			|| haveStatusType(StatusType.PARKED)
//			|| haveStatusType(StatusType.GARAGED)) {
//			setOperator(null);
//			setSpeed(0);
//		} 
		
		addToTrail(getCoordinates());

		correctVehicleReservation();

//		// If operator is dead, remove operator and stop vehicle.
//		VehicleOperator operator = vehicleOperator;
//		if ((operator != null) && (operator instanceof Person)) {
//			Person personOperator = (Person) operator;
//			if (personOperator.getPhysicalCondition().isDead()) {
//				setOperator(null);
//				setSpeed(0);
//				setParkedLocation(0D, 0D, getDirection().getDirection());
//			}
//			// TODO : will another person take his place as the driver
//		}
	}

	/**
	 * Checks on a person's status to see if he can walk home or be rescued
	 * 
	 * @param rover
	 * @param p
	 * @param disembarkSettlement
	 */
	private void checkPersonStatus(Rover rover, Person p, Settlement disembarkSettlement) {
		if (p.isInVehicle() || p.isOutside()) {
			// Get random inhabitable building at emergency settlement.
			Building destinationBuilding = disembarkSettlement.getBuildingManager().getRandomAirlockBuilding();
			if (destinationBuilding != null) {
				Point2D destinationLoc = LocalAreaUtil.getRandomInteriorLocation(destinationBuilding);
				Point2D adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(destinationLoc.getX(),
						destinationLoc.getY(), destinationBuilding);

				double fatigue = p.getFatigue(); // 0 to infinity
				double perf = p.getPerformanceRating(); // 0 to 1
				double stress = p.getStress(); // 0 to 100
				double energy = p.getEnergy(); // 100 to infinity
				double hunger = p.getHunger(); // 0 to infinity

				boolean hasStrength = fatigue < 1000 && perf > .4 && stress < 60 && energy > 750 && hunger < 1000;
				
				if (p.isInVehicle()) {// && p.getInventory().findNumUnitsOfClass(EVASuit.class) == 0) {
					// Checks to see if the person has an EVA suit	
					if (!ExitAirlock.goodEVASuitAvailable(rover.getInventory(), p)) {

						LogConsolidated.log(Level.WARNING, 0, sourceName, "[" + p.getLocationTag().getLocale() + "] "
										+ p + " could not find a working EVA suit and needed to wait.");
					
						// If the person does not have an EVA suit	
						int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(disembarkSettlement);
//						int suitVehicle = rover.getInventory().findNumUnitsOfClass(EVASuit.class);
						
						if (availableSuitNum > 0) {
							// Deliver an EVA suit from the settlement to the rover
							// TODO: Need to generate a task for a person to hand deliver an extra suit
							EVASuit suit = disembarkSettlement.getInventory().findAnEVAsuit(); //(EVASuit) disembarkSettlement.getInventory().findUnitOfClass(EVASuit.class);
							if (suit != null && rover.getInventory().canStoreUnit(suit, false)) {
								
								suit.transfer(disembarkSettlement, rover);
//								disembarkSettlement.getInventory().retrieveUnit(suit);
//								rover.getInventory().storeUnit(suit);
								
								LogConsolidated.log(Level.WARNING, 0, sourceName, "[" + p.getLocationTag().getLocale() + "] "
										+ p + " received a spare EVA suit from the settlement.");
							}
						}
					}
				}
				
				if (Walk.canWalkAllSteps(p, adjustedLoc.getX(), adjustedLoc.getY(), 0, destinationBuilding)) {
			
					if (hasStrength) {
						LogConsolidated.log(Level.INFO, 20_000, sourceName, 
								"[" + disembarkSettlement.getName() + "] "
								+ p.getName() + " still had strength left and would help unload cargo.");
						// help unload the cargo
						unloadCargo(p, rover);
					}	
					else {
						LogConsolidated.log(Level.INFO, 20_000, sourceName, 
								"[" + disembarkSettlement.getName() + "] "
								+ p.getName() + " had no more strength and walked back to the settlement.");
						// walk back home
						assignTask(p, new Walk(p, adjustedLoc.getX(), adjustedLoc.getY(), 0, destinationBuilding));
					}
					
				} 
				
				else if (!hasStrength) {

					// Help this person put on an EVA suit
					// TODO: consider inflatable medical tent for emergency transport of incapacitated personnel
					
					// This person needs to be rescued.
					LogConsolidated.log(Level.INFO, 0, sourceName, 
							"[" + disembarkSettlement.getName() + "] "
							+ Msg.getString("RoverMission.log.emergencyEnterSettlement", p.getName(), 
									disembarkSettlement.getNickName())); //$NON-NLS-1$
					
					// Initiate an rescue operation
					// TODO: Gets a lead person to perform it and give him a rescue badge
					rescueOperation(rover, p, disembarkSettlement);
					
					LogConsolidated.log(Level.INFO, 0, sourceName, 
							"[" + disembarkSettlement.getName() + "] "
							+ p.getName() 
							+ " was transported to ("
							+ Math.round(p.getXLocation()*10.0)/10.0 + ", " 
							+ Math.round(p.getYLocation()*10.0)/10.0 + ") in "
							+ p.getBuildingLocation().getNickName()); //$NON-NLS-1$
					
					// TODO: how to force the person to receive some form of medical treatment ?
					p.getMind().getTaskManager().clearAllTasks();
					p.getMind().getTaskManager().addTask(new RequestMedicalTreatment(p), false);
					
				}

			}
			
			else {
				logger.severe("No inhabitable buildings at " + disembarkSettlement);
			}
		}
	}
	
	/**
	 * Rescue the person from the rover
	 * 
	 * @param r the rover
	 * @param p the person
	 * @param s the settlement
	 */
	private void rescueOperation(Rover r, Person p, Settlement s) {
		
		if (p.isDeclaredDead()) {
			Unit cu = p.getPhysicalCondition().getDeathDetails().getContainerUnit();
//			cu.getInventory().retrieveUnit(p);
			p.transfer(cu, s);
		}
		// Retrieve the person from the rover
		else if (r != null) {
//			r.getInventory().retrieveUnit(p);
			p.transfer(r, s);
		}
		else if (p.isOutside()) {
//			unitManager.getMarsSurface().getInventory().retrieveUnit(p);
			p.transfer(unitManager.getMarsSurface(), s);
		}
		
		// Store the person into the settlement
//		s.getInventory().storeUnit(p);
		
		// Gets the settlement id
		int id = s.getIdentifier();
		// Store the person into a medical building
		BuildingManager.addToMedicalBuilding(p, id);

		// Register the historical event
//		HistoricalEvent rescueEvent = new MissionHistoricalEvent(EventType.MISSION_RESCUE_PERSON, 
//				this,
//				p.getPhysicalCondition().getHealthSituation(), 
//				p.getTaskDescription(), 
//				p.getName(),
//				r.getNickName(), 
//				p.getLocationTag().getLocale(),
//				p.getAssociatedSettlement().getName()
//				);
//		eventManager.registerNewEvent(rescueEvent);
	}
	
	/**
	 * Give a person the task from unloading the vehicle
	 * 
	 * @param p
	 * @param rover
	 */
	private void unloadCargo(Person p, Rover rover) {
		if (RandomUtil.lessThanRandPercent(50)) {
			if (isRoverInAGarage()) {
				assignTask(p, new UnloadVehicleGarage(p, rover));
			} 
			
			else {
				// Check if it is day time.
				if (!EVAOperation.isGettingDark(p)) {
					assignTask(p, new UnloadVehicleEVA(p, rover));
				}
			}
			
//			return;	
		}	
	}
	
	/**
	 * Adds a new task for a person in the mission. Task may be not assigned if it
	 * is effort-driven and person is too ill to perform it.
	 * 
	 * @param person the person to assign to the task
	 * @param task   the new task to be assigned
	 * @return true if task can be performed.
	 */
	protected boolean assignTask(Person person, Task task) {
		boolean canPerformTask = true;

		// If task is effort-driven and person too ill, do not assign task.
		if (task.isEffortDriven() && (person.getPerformanceRating() == 0D)) {
			canPerformTask = false;
		}

		if (canPerformTask) {
			person.getMind().getTaskManager().addTask(task, false);
		}

		return canPerformTask;
	}
	
	/**
	 * Checks if the rover is currently in a garage or not.
	 * 
	 * @return true if rover is in a garage.
	 */
	public boolean isRoverInAGarage() {
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
	public abstract void determinedSettlementParkedLocationAndFacing();

	public String getTypeOfDessertLoaded() {
		return typeOfDessertLoaded;
	}

	public void setTypeOfDessertLoaded(String dessertName) {
		typeOfDessertLoaded = dessertName;
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
		if (getLocationStateType() == LocationStateType.WITHIN_SETTLEMENT_VICINITY)
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
		if (getLocationStateType() == LocationStateType.WITHIN_SETTLEMENT_VICINITY
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
	public Mission getMission() {
		Iterator<Mission> i = missionManager.getMissions().iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (!mission.isDone()) {
				if (mission instanceof VehicleMission) {
	
					if (((VehicleMission) mission).getVehicle() == this) {
						return mission;
					}

//					if (mission instanceof Mining) {
//						if (((Mining) mission).getLightUtilityVehicle() == this) {
//							return mission;
//						}
//					}

//					if (mission instanceof Trade) {
//						Rover towingRover = (Rover) ((Trade) mission).getVehicle();
//						if (towingRover != null) {
//							if (towingRover.getTowedVehicle() == this) {
//								return mission;
//							}
//						}
//					}
//				} else if (mission instanceof BuildingConstructionMission) {
//					BuildingConstructionMission construction = (BuildingConstructionMission) mission;
//					if (construction.getConstructionVehicles() != null) {
//						if (construction.getConstructionVehicles().contains(this)) {
//							return mission;
//						}
//					}
//					// else {
//					// result = null;
//					// }
//				} else if (mission instanceof BuildingSalvageMission) {
//					BuildingSalvageMission salvage = (BuildingSalvageMission) mission;
//					if (salvage.getConstructionVehicles().contains(this)) {
//						return mission;
//					}
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
	public double getMissionRange(String missionName) {
			
		if (missionName.equalsIgnoreCase(AreologyFieldStudy.DEFAULT_DESCRIPTION)) {
			return getSettlement().getMissionRadius(0);
		}
		
		if (missionName.equalsIgnoreCase(BiologyFieldStudy.DEFAULT_DESCRIPTION)) {
			return getSettlement().getMissionRadius(1);
		}
		
		if (missionName.equalsIgnoreCase(CollectIce.DEFAULT_DESCRIPTION)) {
			return getSettlement().getMissionRadius(2);
		}
		
		if (missionName.equalsIgnoreCase(CollectRegolith.DEFAULT_DESCRIPTION)) {
			return getSettlement().getMissionRadius(3);
		}
		
		if (missionName.equalsIgnoreCase(EmergencySupply.DEFAULT_DESCRIPTION)) {
			return getSettlement().getMissionRadius(4);
		}
		
		if (missionName.equalsIgnoreCase(Exploration.DEFAULT_DESCRIPTION)) {
			return getSettlement().getMissionRadius(5);
		}
		
		if (missionName.equalsIgnoreCase(MeteorologyFieldStudy.DEFAULT_DESCRIPTION)) {
			return getSettlement().getMissionRadius(6);
		}
		
		if (missionName.equalsIgnoreCase(Mining.DEFAULT_DESCRIPTION)) {
			return getSettlement().getMissionRadius(7);
		}

		if (missionName.equalsIgnoreCase(RescueSalvageVehicle.DEFAULT_DESCRIPTION)) {
			return getSettlement().getMissionRadius(8);
		}
		
		if (missionName.equalsIgnoreCase(Trade.DEFAULT_DESCRIPTION)) {
			return getSettlement().getMissionRadius(9);
		}
		
		if (missionName.equalsIgnoreCase(TravelToSettlement.DEFAULT_DESCRIPTION)) {
			return getSettlement().getMissionRadius(10);
		}
		
		
		return getSettlement().getMaxMssionRange();
	}
	
	/**
	 * Checks if this vehicle is involved in a mission
	 * 
	 * @return true if yes
	 */
	public double getMissionRange(Mission mission) {
		if (!mission.isDone()) {
			if (mission instanceof VehicleMission) {

				if (mission instanceof AreologyFieldStudy) {
					if (((AreologyFieldStudy) mission).getVehicle() == this) {
						return getSettlement().getMissionRadius(0);
					}
				}
				
				if (mission instanceof BiologyFieldStudy) {
					if (((BiologyFieldStudy) mission).getVehicle() == this) {
						return getSettlement().getMissionRadius(1);
					}
				}
				
				if (mission instanceof CollectIce) {
					if (((CollectIce) mission).getVehicle() == this) {
						return getSettlement().getMissionRadius(2);
					}
				}
				
				if (mission instanceof CollectRegolith) {
					if (((CollectRegolith) mission).getVehicle() == this) {
						return getSettlement().getMissionRadius(3);
					}
				}
				
				if (mission instanceof EmergencySupply) {
					if (((EmergencySupply) mission).getVehicle() == this) {
						return getSettlement().getMissionRadius(4);
					}
				}
				
				if (mission instanceof Exploration) {
					if (((Exploration) mission).getVehicle() == this) {
						return getSettlement().getMissionRadius(5);
					}
				}
				
				if (mission instanceof MeteorologyFieldStudy) {
					if (((MeteorologyFieldStudy) mission).getVehicle() == this) {
						return getSettlement().getMissionRadius(6);
					}
				}
				
				if (mission instanceof Mining) {
					if (((Mining) mission).getVehicle() == this) {
						return getSettlement().getMissionRadius(7);
					}
				}
	
				
				if (mission instanceof RescueSalvageVehicle) {
					if (((RescueSalvageVehicle) mission).getVehicle() == this) {
						return getSettlement().getMissionRadius(8);
					}
				}
				
				if (mission instanceof Trade) {
					Rover towingRover = (Rover) ((Trade) mission).getVehicle();
					if (towingRover != null) {
						if (towingRover.getTowedVehicle() == this) {
							return getSettlement().getMissionRadius(9);
						}
					}
				}
				
				if (mission instanceof TravelToSettlement) {
					if (((TravelToSettlement) mission).getVehicle() == this) {
						return getSettlement().getMissionRadius(10);
					}
				}
				
//				if (mission instanceof BuildingConstructionMission) {
//					BuildingConstructionMission construction = (BuildingConstructionMission) mission;
//					if (construction.getConstructionVehicles() != null) {
//						if (construction.getConstructionVehicles().contains(this)) {
//							return true;
//						}
//					}
//				}
//				
//				if (mission instanceof BuildingSalvageMission) {
//					BuildingSalvageMission salvage = (BuildingSalvageMission) mission;
//					if (salvage.getConstructionVehicles().contains(this)) {
//						return true;
//					}
//				}
			}
		}
			
		return getSettlement().getMaxMssionRange();
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
	 * Reset uniqueCount to the current number of vehicles
	 */
	public static void reinitializeIdentifierCount() {
		uniqueCount = unitManager.getVehiclesNum() + Unit.FIRST_VEHICLE_UNIT_ID;
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