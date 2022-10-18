/*
 * Mars Simulation Project
 * Vehicle.java
 * @date 2022-06-27
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
import java.util.logging.Level;
import java.util.stream.Collectors;

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
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.mission.MissionPhase.Stage;
import org.mars_sim.msp.core.person.ai.task.Conversation;
import org.mars_sim.msp.core.person.ai.task.MaintainBuilding;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.resource.ResourceUtil;
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
	
	/** The error margin for determining vehicle range. (Actual distance / Safe distance). */
	private static double fuel_range_error_margin;
	private static double life_support_range_error_margin;

	// Future : may move fuel specs to a separate config file
	/**
	 * <p> Methane's Specific Energy is 55.5 MJ/kg, or 15,416 Wh/kg, or 15.416kWh/kg
	 * <p> Energy density is 0.0364 MJ/L, 36.4 kJ/L, or 10 Wh/L
	 * <p> Note : 1 MJ = 0.277778 kWh; 1 kWh = 3.6 MJ
	 * <p> As comparison, 1 gallon (or 3.7854 L) of gasoline has 33.7 kWh of energy. Energy Density is 8.9 kWh/L
	 */
	public static final double METHANE_SPECIFIC_ENERGY = 15.416; // [in kWh/kg]

	/**
	 * The Solid Oxide Fuel Cell (SOFC) Conversion Efficiency for using methane is dimension-less.
	 * Light hydrocarbon fuels, such as methane can be internally reformed within the anode of the fuel cells.
	 */
	public static final double SOFC_CONVERSION_EFFICIENCY = .65;
	
	public static final double KG_PER_KWH = 1.0 / SOFC_CONVERSION_EFFICIENCY / METHANE_SPECIFIC_ENERGY; 
	
	public static final double WH_PER_KG = 1000.0 * SOFC_CONVERSION_EFFICIENCY * METHANE_SPECIFIC_ENERGY;

	//	/** Lifetime Wear in millisols **/
//	private static final double WEAR_LIFETIME = 668_000; // 668 Sols (1 orbit)
	/** Estimated Number of hours traveled each day. **/
	private static final int ESTIMATED_TRAVEL_HOURS_PER_SOL = 16;

	// Format for unit
//	private static final String KWH = " kWh   ";
//	private static final String KG = " kg   ";
//	private static final String KM_KG = " km/kg   ";
	
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
	/** Base speed of vehicle in kph (can be set in child class). */
	private double baseSpeed = 0; //
	/** The base range of the vehicle (with full tank of fuel and no cargo) (km). */
	private double baseRange = 0;
	/** Total cumulative distance traveled by vehicle (km). */
	private double odometerMileage; //
	/** The last distance travelled by vehicle (km). */
	private double lastDistance;
	/** Distance traveled by vehicle since last maintenance (km) . */
	private double distanceMaint; //
	/** The efficiency of the vehicle's drivetrain. [dimension-less] */
	private double drivetrainEfficiency;
	/** The conversion fuel-to-drive energy factor for a specific vehicle type [Wh/kg] */
	private double conversionFuel2DriveEnergy;
	/** The average power output of the vehicle. (kW). */
	private double averagePower = 0;
	/** The total number of hours the vehicle is capable of operating. (hr). */
	private double totalHours;
	/** The cumulative fuel usage of the vehicle [kg] */
	private double fuelCumUsed;
	/** The maximum fuel capacity of the vehicle [kg] */
	private double fuelCapacity;
	/** The total energy of the vehicle in full tank [kWh]. */
	private double energyCapacity;
	/** The estimated energy available for the drivetrain [kWh]. */
	private double drivetrainEnergy;
	/** The base fuel economy of the vehicle [km/kg]. */
	private double baseFuelEconomy;
	/** The estimated average fuel economy of the vehicle for a trip [km/kg]. */
	private double estimatedFuelEconomy;
	/** The instantaneous fuel economy of the vehicle [km/kg]. */
	private double iFuelEconomy;
	/** The base fuel consumption of the vehicle [Wh/km]. See https://ev-database.org/cheatsheet/energy-consumption-electric-car */
	private double baseFuelConsumption;
	/** The instantaneous fuel consumption of the vehicle [Wh/km]. */
	private double iFuelConsumption;
	/** The actual start mass of the vehicle (base mass + crew weight + full cargo weight) for a trip [km/kg]. */
	private double startMass = 0;
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
	/** Parked position (meters) from center of settlement. */
	private LocalPosition posParked;
	/** Parked facing (degrees clockwise from North). */
	private double facingParked;

	/** The vehicle type string. */
	private String vehicleTypeString;
	/** The vehicle type. */
	private VehicleType vehicleType;

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
	private MSolDataLogger<Set<StatusType>> vehicleLog = new MSolDataLogger<>(5);
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
	Vehicle(String name, String vehicleTypeString, Settlement settlement, double maintenanceWorkTime) {
		// Use Unit constructor
		super(name, settlement.getCoordinates());

		if (unitManager == null)
			unitManager = sim.getUnitManager();
		
		this.vehicleTypeString = vehicleTypeString;
		
		// Set description
		setDescription(vehicleTypeString);
		
		vehicleType = VehicleType.convertNameToVehicleType(vehicleTypeString);

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
		// Manually add this vehicle to the settlement
		settlement.addOwnedVehicle(this);
		// Set the initial coordinates to be that of the settlement
		setCoordinates(settlement.getCoordinates());
		
		setupBaseWear();

		// Initialize malfunction manager.
		malfunctionManager = new MalfunctionManager(this, baseWearLifetime, maintenanceWorkTime);

		setupScopeString();

		primaryStatus = StatusType.PARKED;
		
		writeLog();

		VehicleSpec spec = simulationConfig.getVehicleConfiguration().getVehicleSpec(vehicleTypeString);
		// Set width and length of vehicle.
		width = spec.getWidth();
		length = spec.getLength();

		setupSpecs(spec);

		// Set initial parked location and facing at settlement.
		findNewParkingLoc();

		// Initialize operator activity spots.
		operatorActivitySpots = spec.getOperatorActivitySpots();

		// Initialize passenger activity spots.
		passengerActivitySpots = spec.getPassengerActivitySpots();
	}

	/**
	 * Sets the base wear life time.
	 */
	private void setupBaseWear() {
	
		if (vehicleType == VehicleType.DELIVERY_DRONE) {
			baseWearLifetime = 668_000 * .75; // 668 Sols (1 orbit)
		}
		else if (vehicleType == VehicleType.LUV) {
			baseWearLifetime = 668_000 * 2D; // 668 Sols (1 orbit)
		}
		else if (vehicleType == VehicleType.EXPLORER_ROVER) {
			baseWearLifetime = 668_000; // 668 Sols (1 orbit)
		}
		else if (vehicleType == VehicleType.TRANSPORT_ROVER) {
			baseWearLifetime = 668_000 * 1.5; // 668 Sols (1 orbit)
		}
		else if (vehicleType == VehicleType.CARGO_ROVER) {
			baseWearLifetime = 668_000 * 1.25; // 668 Sols (1 orbit)
		}
	}
	
	/**
	 * Sets the scope string.
	 */
	private void setupScopeString() {
		// Add "vehicle" as scope
		malfunctionManager.addScopeString(SystemType.VEHICLE.getName());
	
		// Add its vehicle type as scope
		malfunctionManager.addScopeString(vehicleTypeString);
	
		// Add "rover" as scope
		if (vehicleTypeString.contains(SystemType.ROVER.getName())) {
			malfunctionManager.addScopeString(SystemType.ROVER.getName());
		}
	}
	
	/**
	 * Sets up the vehicle specs.
	 * 
	 * @param spec
	 */
	private void setupSpecs(VehicleSpec spec) {
		// Gets the crew capacity
		int numCrew = spec.getCrewSize();
		// Gets estimated total crew weight
		double estimatedTotalCrewWeight = numCrew * Person.getAverageWeight();
		// Gets cargo capacity
		double cargoCapacity = spec.getTotalCapacity();
		// Create microInventory instance
		eqmInventory = new EquipmentInventory(this, cargoCapacity);

		// Set the capacities for each supported resource
		Map<String, Double> capacities = spec.getCargoCapacityMap();
		if (capacities != null) {
			eqmInventory.setResourceCapacityMap(capacities);
		}

		// Set total distance traveled by vehicle (km)
		odometerMileage = 0;
		// Set distance traveled by vehicle since last maintenance (km)
		distanceMaint = 0;
		// Set base speed.
		baseSpeed = spec.getBaseSpeed();
		// Set average power when operating the vehicle at base/average speed.
		averagePower = spec.getAveragePower();
		// Set the empty mass of the vehicle.
		setBaseMass(spec.getEmptyMass());
		// Set the drivetrain efficiency [dimension-less] of the vehicle.
		drivetrainEfficiency = spec.getDriveTrainEff();
		// Gets the capacity [in kg] of vehicle's fuel tank
		fuelCapacity = spec.getCargoCapacity(ResourceUtil.findAmountResourceName(getFuelType()));
		// Gets the energy capacity [kWh] based on a full tank of methane
		energyCapacity = fuelCapacity / KG_PER_KWH;
		// Gets the conversion factor for a specific vehicle
		conversionFuel2DriveEnergy = KG_PER_KWH * drivetrainEfficiency;
		// Define percent of other energy usage (other than for drivetrain)
		double otherEnergyUsage = 0;
		// Assume the peak power is 4x the average power.
		double peakPower = averagePower * 4.0;
		
		if (vehicleType == VehicleType.DELIVERY_DRONE) {
			// Hard-code percent energy usage for this vehicle.
			otherEnergyUsage = 5.0;
			// Gets the estimated energy available for drivetrain [in kWh]
			drivetrainEnergy = energyCapacity * (1.0 - otherEnergyUsage / 100.0) * drivetrainEfficiency;
			// Gets the maximum total # of hours the vehicle is capable of operating
			totalHours = drivetrainEnergy / averagePower;

			// Gets the base range [in km] of the vehicle
			baseRange = baseSpeed * totalHours;
			// Gets the base fuel economy [in km/kg] of this vehicle
			baseFuelEconomy = baseRange / fuelCapacity;
			// Gets the base fuel consumption [in Wh/km] of this vehicle
			baseFuelConsumption =  energyCapacity * 1000.0 / baseRange;

			// Accounts for the fuel (methane and oxygen) and the traded goods
			beginningMass = getBaseMass() + 500;
			// Accounts for water and the traded goods
			endMass = getBaseMass() + 450;			
		}
		
		else if (vehicleType == VehicleType.LUV) {
			// Hard-code percent energy usage for this vehicle.
			otherEnergyUsage = 30.0;
			// Gets the estimated energy available for drivetrain [in kWh]
			drivetrainEnergy = energyCapacity * (1.0 - otherEnergyUsage / 100.0) * drivetrainEfficiency;
			// Gets the maximum total # of hours the vehicle is capable of operating
			totalHours = drivetrainEnergy / averagePower;
			
			// Gets the base range [in km] of the vehicle
			baseRange = baseSpeed * totalHours;
			// Gets the base fuel economy [in km/kg] of this vehicle
			baseFuelEconomy = baseRange / fuelCapacity;
			// Gets the base fuel consumption [in Wh/km] of this vehicle
			baseFuelConsumption =  energyCapacity * 1000.0 / baseRange;

			// Accounts for the occupant weight
			beginningMass = getBaseMass() + estimatedTotalCrewWeight;
			// Accounts for the occupant weight
			endMass = getBaseMass() + estimatedTotalCrewWeight;			
		}
		
		else if (vehicleType == VehicleType.EXPLORER_ROVER) {
			// Hard-code percent energy usage for this vehicle.
			otherEnergyUsage = 15.0;
			// Gets the estimated energy available for drivetrain [in kWh]
			drivetrainEnergy = energyCapacity * (1.0 - otherEnergyUsage / 100.0) * drivetrainEfficiency;
			// Gets the maximum total # of hours the vehicle is capable of operating
			totalHours = drivetrainEnergy / averagePower;
			
			// Gets the base range [in km] of the vehicle
			baseRange = baseSpeed * totalHours;
			// Gets the base fuel economy [in km/kg] of this vehicle
			baseFuelEconomy = baseRange / fuelCapacity;
			// Gets the base fuel consumption [in Wh/km] of this vehicle
			baseFuelConsumption =  energyCapacity * 1000.0 / baseRange;
			
			// Accounts for the occupant consumables
			beginningMass = getBaseMass() + estimatedTotalCrewWeight + 4 * 50;
			// Accounts for the rock sample, ice or regolith collected
			endMass = getBaseMass() + estimatedTotalCrewWeight + 800;	
			
		}
		
		else if (vehicleType == VehicleType.CARGO_ROVER) {
			// Hard-code percent energy usage for this vehicle.
			otherEnergyUsage = 10.0;
			// Gets the estimated energy available for drivetrain [in kWh]
			drivetrainEnergy = energyCapacity * (1.0 - otherEnergyUsage / 100.0) * drivetrainEfficiency;		
			// Gets the maximum total # of hours the vehicle is capable of operating
			totalHours = drivetrainEnergy / averagePower;
			
			// Gets the base range [in km] of the vehicle
			baseRange = baseSpeed * totalHours;
			// Gets the base fuel economy [in km/kg] of this vehicle
			baseFuelEconomy = baseRange / fuelCapacity;
			// Gets the base fuel consumption [in Wh/km] of this vehicle
			baseFuelConsumption =  energyCapacity * 1000.0 / baseRange;

			// Accounts for the occupant consumables and traded goods 
			beginningMass = getBaseMass() + estimatedTotalCrewWeight + 2 * 50 + 1500;
			// Accounts for the occupant consumables and traded goods
			endMass = getBaseMass() + estimatedTotalCrewWeight + 1500;				
		}
		
		else if (vehicleType == VehicleType.TRANSPORT_ROVER) {
			// Hard-code percent energy usage for this vehicle.
			otherEnergyUsage = 20.0;
			// Gets the estimated energy available for drivetrain [in kWh]
			drivetrainEnergy = energyCapacity * (1.0 - otherEnergyUsage / 100.0) * drivetrainEfficiency;
			// Gets the maximum total # of hours the vehicle is capable of operating
			totalHours = drivetrainEnergy / averagePower;
			
			// Gets the base range [in km] of the vehicle
			baseRange = baseSpeed * totalHours;
			// Gets the base fuel economy [in km/kg] of this vehicle
			baseFuelEconomy = baseRange / fuelCapacity;
			// Gets the base fuel consumption [in Wh/km] of this vehicle
			baseFuelConsumption =  energyCapacity * 1000.0 / baseRange;

			// // Accounts for the occupant consumables and personal possession
			beginningMass = getBaseMass() + estimatedTotalCrewWeight + 8 * (50 + 100);
			// Accounts for the reduced occupant consumables
			endMass = getBaseMass() + estimatedTotalCrewWeight + 8 * 100;				
		}

		// Gets the estimated average fuel economy for a trip [km/kg]
		estimatedFuelEconomy = baseFuelEconomy * beginningMass / endMass * .75;
		// Gets the base acceleration [m/s2]
		baseAccel = peakPower / beginningMass / baseSpeed * 1000 * 3.6;

//		logger.log(this, Level.INFO, 0, 
//				vehicleType.getName() + "   "
//				+ "drivetrainEfficiency: " + Math.round(drivetrainEfficiency * 100.0)/100.0 + "   " 
//	   		 	+ "baseSpeed: " + Math.round(baseSpeed * 100.0)/100.0 + " kW/hr   " 
//    		 	+ "averagePower: " + Math.round(averagePower * 100.0)/100.0 + " kW   "
//    	    	+ "baseAccel: " + Math.round(baseAccel * 100.0)/100.0 + " m/s2  "      		 	
//    	    	+ "energyCapacity: " + Math.round(energyCapacity * 100.0)/100.0 + KWH 
//    	    	+ "drivetrainEnergy: " + Math.round(drivetrainEnergy * 100.0)/100.0 + KWH);  
//
//    	logger.log(this, Level.INFO, 0, 	     	    	
//    		 	"totalHours: " + Math.round(totalHours * 100.0)/100.0 + " hr   "
//    		 	+ "baseRange: " + Math.round(baseRange * 100.0)/100.0 + " km   "
//    		 	+ "baseFuelEconomy: " + Math.round(baseFuelEconomy * 100.0)/100.0 + KM_KG
//    		 	+ "estimatedAveFuelEconomy: " + Math.round(estimatedFuelEconomy * 100.0)/100.0 + KM_KG
//    	    	+ "initial FuelEconomy: " + Math.round(getInitialFuelEconomy() * 100.0)/100.0 + KM_KG     		 	
//	 			+ "baseFuelConsumption: " + Math.round(baseFuelConsumption * 100.0)/100.0 + " Wh/km   ");
//    		 	
//    	logger.log(this, Level.INFO, 0, 	 	
//				"fuelCapacity: " + Math.round(fuelCapacity * 100.0)/100.0 + KG 			
//    		 	+ "cargoCapacity: " + Math.round(cargoCapacity * 100.0)/100.0 + KG
//    	       	+ "baseMass: " + Math.round(getBaseMass() * 100.0)/100.0 + KG
//       		 	+ "beginningMass: " + Math.round(beginningMass * 100.0)/100.0 + KG
//    		 	+ "endMass: " + Math.round(endMass * 100.0)/100.0 + KG);  	
	}
	
	/**
	 * Constructor 2 : prepares a Vehicle object for testing (called by MockVehicle).
	 *
	 * @param name                the vehicle's name
	 * @param vehicleType         the configuration description of the vehicle.
	 * @param settlement          the settlement the vehicle is parked at.
	 * @param baseSpeed           the base speed of the vehicle [kph]
	 * @param baseMass            the base mass of the vehicle [kg]
	 * @param fuelEconomy		  the fuel economy of the vehicle [km/kg]
	 * @param maintenanceWorkTime the work time required for maintenance [millisols]
	 */
	protected Vehicle(String name, String vehicleType, Settlement settlement, double baseSpeed, double baseMass,
			double fuelEconomy, double maintenanceWorkTime) {

		// Use Unit constructor
		super(name, settlement.getCoordinates());

		if (unitManager == null)
			unitManager = sim.getUnitManager();

		this.vehicleTypeString = vehicleType;

		associatedSettlementID = settlement.getIdentifier();

		setContainerID(associatedSettlementID);

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
		posParked = LocalPosition.DEFAULT_POSITION;
		facingParked = 0D;

		// Create microInventory instance for testing. 
		// Set up a 2000 kg cargo cap
		eqmInventory = new EquipmentInventory(this, 2_000D);

		// Initialize malfunction manager.
		malfunctionManager = new MalfunctionManager(this, getBaseWearLifetime(), maintenanceWorkTime);
		malfunctionManager.addScopeString(SystemType.VEHICLE.getName());

		primaryStatus = StatusType.PARKED;
		writeLog();

		// Add to the settlement
		settlement.addOwnedVehicle(this);
	}

	/**
	 * Gets the vehicle description.
	 * 
	 * @param vehicleType
	 * @return
	 */
	public String getDescription(String vehicleType) {
		VehicleConfig vehicleConfig = simulationConfig.getVehicleConfiguration();
		return vehicleConfig.getVehicleSpec(vehicleType).getDescription();
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
		return averagePower;
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
	 * @return the vehicle's base speed (in km/hr)
	 */
	public double getBaseSpeed() {
		return baseSpeed;
	}


	/**
	 * Gets the current fuel range of the vehicle.
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
        	range = estimatedFuelEconomy * fuelCapacity * getBaseMass() / getMass();// * fuel_range_error_margin
        }
		// TODO Fix this; is this clause every triggered
        else if (mission.getPhase().getStage() == Stage.PREPARATION) {
        	// Before loading/embarking phase, the amountOfFuel to be loaded is still zero.
        	// So the range would be based on vehicle's capacity
        	range = estimatedFuelEconomy * fuelCapacity * getBaseMass() / getMass();
        }
        else {
            double amountOfFuel = getAmountResourceStored(getFuelType());
        	// During the journey, the range would be based on the amount of fuel in the vehicle
    		range = estimatedFuelEconomy * amountOfFuel * getBaseMass() / getMass();
        }

        return Math.min(radius, (int)range);
	}

	/**
	 * Gets the base range of the vehicle.
	 *
	 * @return the base range of the vehicle [km]
	 * @throws Exception if error getting range.
	 */
	public double getBaseRange() {
		return baseRange;
	}

	/**
	 * Gets the fuel capacity of the vehicle [kg].
	 *
	 * @return
	 */
	public double getFuelCapacity() {
		return fuelCapacity;
	}

	/**
	 * Gets the cumulative fuel usage of the vehicle [kg].
	 *
	 * @return
	 */
	public double getFuelCumulativeUsage() {
		return fuelCumUsed;
	}
	
	/**
	 * Gets the energy available at the full tank [kWh].
	 *
	 * @return
	 */
	public double getEnergyCapacity() {
		return energyCapacity;
	}

	/**
	 * Gets the estimated energy available for the drivetrain [kWh].
	 *
	 * @return
	 */
	public double getDrivetrainEnergy() {
		return drivetrainEnergy;
	}
	
	/**
	 * Gets the fuel to energy conversion factor.
	 * 
	 * @return
	 */
	public double getFuelConv() {
		return conversionFuel2DriveEnergy;
	}
	
	/**
	 * Gets the cumulative fuel economy [km/kg].
	 * 
	 * @return
	 */
	public double getCumFuelEconomy() {
		if (fuelCumUsed == 0.0d)
			return 0;
		return odometerMileage / fuelCumUsed;
	}
	
	/**
	 * Gets the cumulative fuel consumption [Wh/km].
	 * 
	 * @return
	 */
	public double getCumFuelConsumption() {
		if (odometerMileage == 0.0d)
			return 0;
		return WH_PER_KG * fuelCumUsed / odometerMileage;
	}

	/**
	 * Gets the base fuel economy of the vehicle [km/kg].
	 * 
	 * @return
	 */
	public double getBaseFuelEconomy() {
		return baseFuelEconomy;
	}

	/**
	 * Gets the base fuel consumption of the vehicle [Wh/km].
	 * 
	 * @return
	 */
	public double getBaseFuelConsumption() {
		return baseFuelConsumption;
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
	 * Gets the initial fuel economy of the vehicle [km/kg].
	 * Note: assume that it is primarily dependent upon the current weight of the vehicle
	 *
	 * @return
	 */
	public double getInitialFuelEconomy() {
		return estimatedFuelEconomy * (startMass + beginningMass) / 2.0 / getMass();
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
	 * Gets the estimated average fuel consumption of the vehicle [km/kg] for a trip.
	 * Note: Assume that it is half of two fuel consumption values (between the beginning and the end of the trip)
	 *
	 * @return
	 */
	public double getEstimatedFuelEconomy() {
		return estimatedFuelEconomy;
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
	 * @param fuelUsed the fuel used [kg]
	 */
	public void addOdometerMileage(double distance, double fuelUsed) {
		odometerMileage += distance;
		lastDistance = distance;
		fuelCumUsed += fuelUsed;
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
		if (speed <= 1)
			return baseAccel;
		return (baseAccel + Math.min(baseAccel, averagePower / getMass() / speed * 3600)) / 2.0;
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

		if (getContainerID() == Unit.MARS_SURFACE_UNIT_ID)
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
			List<Building> list = settlement.getBuildingManager().getGarages();
			for (Building garageBuilding : list) {
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

		correctVehicleReservation();

		return true;
	}

	/**
	 * Resets the vehicle reservation status.
	 */
	public void correctVehicleReservation() {
		if (isReservedMission
			// Set reserved for mission to false if the vehicle is not associated with a
			// mission.
			&& missionManager.getMissionForVehicle(this) == null) {
				logger.log(this, Level.FINE, 5000,
						"Found reserved for an non-existing mission. Untagging it.");
				setReservedForMission(false);
		} else if (missionManager.getMissionForVehicle(this) != null) {
				logger.log(this, Level.FINE, 5000,
						"On a mission but not registered as mission reserved. Correcting it.");
				setReservedForMission(true);
		}
	}

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
		// Return estimated average speed in km / sol.
		return baseSpeed * ESTIMATED_TRAVEL_HOURS_PER_SOL;
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
					
				} else if (mission.getMissionType() == MissionType.BUILDING_CONSTRUCTION) {
					BuildingConstructionMission construction = (BuildingConstructionMission) mission;
					if (!construction.getConstructionVehicles().isEmpty() 
						&& construction.getConstructionVehicles().contains(this)) {
						return mission;
					}

				} else if (mission.getMissionType() == MissionType.BUILDING_SALVAGE) {
					BuildingSalvageMission salvage = (BuildingSalvageMission) mission;
					if (!salvage.getConstructionVehicles().isEmpty() 
						&& salvage.getConstructionVehicles().contains(this)) {
						return mission;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Returns the mission range prescribed by its home settlement.
	 *
	 * @return true if yes
	 */
	public double getMissionRange(MissionType missiontype) {
		return getAssociatedSettlement().getMissionRadius(missiontype);
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

	/**
	 * Gets the holder's unit instance
	 *
	 * @return the holder's unit instance
	 */
	@Override
	public Unit getHolder() {
		return this;
	}

	/**
	 * Generate a new name for the Vehicle; potentially this may be a preconfigured name
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

	/**
	 * Mass of Equipment is the stored mass plus the base mass.
	 */
	@Override
	public double getMass() {
		return eqmInventory.getStoredMass() + getBaseMass();
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
	 * Gets the total mass on this vehicle (not including vehicle's weight)
	 *
	 * @return
	 */
	@Override
	public double getStoredMass() {
		return eqmInventory.getStoredMass();
	}

	/**
	 * Get the equipment list
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
	 * Does this unit possess an equipment of this equipment type
	 *
	 * @param typeID
	 * @return
	 */
	@Override
	public boolean containsEquipment(EquipmentType type) {
		return eqmInventory.containsEquipment(type);
	}

	/**
	 * Adds an equipment to this unit
	 *
	 * @param equipment
	 * @return true if it can be carried
	 */
	@Override
	public boolean addEquipment(Equipment e) {
		if (eqmInventory.addEquipment(e)) {
			e.setCoordinates(getCoordinates());
//			e.setContainerUnit(this);
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_EQUIPMENT_EVENT, this);
			return true;
		}
		return false;
	}

	/**
	 * Remove an equipment
	 *
	 * @param equipment
	 */
	@Override
	public boolean removeEquipment(Equipment equipment) {
		return eqmInventory.removeEquipment(equipment);
	}

	/**
	 * Stores the item resource
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
	 * Retrieves the item resource
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
	 * Retrieves the resource
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
	 * Stores the resource
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
	 * Gets the item resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceStored(int resource) {
		return eqmInventory.getItemResourceStored(resource);
	}

	/**
	 * Gets the capacity of a particular amount resource
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getAmountResourceCapacity(int resource) {
		return eqmInventory.getAmountResourceCapacity(resource);
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource
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
	 * Gets the amount resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceStored(int resource) {
		return eqmInventory.getAmountResourceStored(resource);
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
	 * @return  a set of resources
	 */
	@Override
	public Set<Integer> getItemResourceIDs() {
		return eqmInventory.getItemResourceIDs();
	}

	/**
	 * Gets a set of resources in storage.
	 * @return  a set of resources
	 */
	@Override
	public Set<Integer> getAmountResourceIDs() {
		return eqmInventory.getAmountResourceIDs();
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
	 * Gets the remaining quantity of an item resource
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
			if (newContainer.getUnitType() != UnitType.PLANET) {
				// Do not inherit the location of a Planet.
				setCoordinates(newContainer.getCoordinates());
			}
			// 2. Set LocationStateType
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
	 * Updates the location state type directly
	 *
	 * @param type
	 */
	public void updateLocationStateType(LocationStateType type) {
		currentStateType = type;
	}

	/**
	 * Gets the location state type based on the type of the new container unit
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
			return LocationStateType.WITHIN_SETTLEMENT_VICINITY;

		if (newContainer.getUnitType() == UnitType.PERSON)
			return LocationStateType.ON_PERSON_OR_ROBOT;

		if (newContainer.getUnitType() == UnitType.PLANET)
			return LocationStateType.MARS_SURFACE;

		return null;
	}

	/**
	 * Is this unit inside a settlement
	 *
	 * @return true if the unit is inside a settlement
	 */
	@Override
	public boolean isInSettlement() {

		if (containerID == MARS_SURFACE_UNIT_ID)
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
	 * Transfer the unit from one owner to another owner
	 *
	 * @param origin {@link Unit} the original container unit
	 * @param destination {@link Unit} the destination container unit
	 */
	public boolean transfer(Unit destination) {
		boolean transferred = false;
		Unit cu = getContainerUnit();

		if (cu.getUnitType() == UnitType.PLANET) {
			transferred = ((MarsSurface)cu).removeVehicle(this);
		}
		else if (cu.getUnitType() == UnitType.SETTLEMENT) {
			transferred = ((Settlement)cu).removeParkedVehicle(this);
		}

		if (transferred) {
			if (destination.getUnitType() == UnitType.PLANET) {
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

	public EquipmentInventory getEquipmentInventory() {
		return eqmInventory;
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