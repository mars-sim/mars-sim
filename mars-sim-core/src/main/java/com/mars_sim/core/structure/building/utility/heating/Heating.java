/*
 * Mars Simulation Project
 * Heating.java
 * @date 2024-07-26
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.utility.heating;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.environment.Weather;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingConfig;
import com.mars_sim.core.structure.building.FunctionSpec;
import com.mars_sim.core.structure.building.function.ClassicAirlock;
import com.mars_sim.core.structure.building.function.LifeSupport;
import com.mars_sim.core.structure.building.function.farming.Crop;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The Heating class is a building function for regulating temperature in a
 * settlement.
 */
public class Heating implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Heating.class.getName());

	/** The heat loss limit for flagging errors. */
	private static final int LOSS_LIMIT = -40;
	/** The heat gain limit for flagging errors. */
	private static final int GAIN_LIMIT = 40;
	/** The assumed maximum indoor temperature. */
	private static final int MAX_INDOOR_TEMPERATURE = 40;
	/** The assumed minimum indoor temperature. */
	private static final int MIN_INDOOR_TEMPERATURE = 0;

	// Data members
	/** The gas constant is in the unit of J/K/mol. */
	private static final double GAS_CONSTANT = 8.31446261815324;
	/** The average molar mass of the air is 28.987 g/mol. */
//	private static final double MOLAR_MASS_OF_AIR = 28.97;
	// KG_TO_LB = 2.204623;
	private static final double DEFAULT_ROOM_TEMPERATURE = 22.5;
	// kW_TO_kBTU_PER_HOUR = 3.41214; // 1 kW = 3412.14 BTU/hr
	private static final double C_TO_K = 273.15;
	private static final double TRANSMITTANCE_GREENHOUSE_HIGH_PRESSURE = .55;
	private static final double TRANSMITTANCE_WINDOW = 0.75;
	private static final double EMISSIVITY_DAY = 0.8;
	private static final double EMISSIVITY_NIGHT = 1.0;
	// EMISSIVITY_INSULATED = 0.05 ;
	private static final double STEFAN_BOLTZMANN_CONSTANT = 0.0000000567; // in W / (m^2 K^4)

	private static final double LARGE_INSULATION_CANOPY = .7; // [in kW]
	private static final double INSULATION_BLANKET = .3; // [in kW]
	private static final double HALLWAY_INSULATION = .5; // [in kW]

	// Thermostat's temperature allowance
	private static final double T_UPPER_SENSITIVITY = 1D;
	private static final double T_LOWER_SENSITIVITY = 1D;

	private static final double HEAT_DISSIPATED_PER_PERSON = .1; // [in kW]

	private static final double HEAT_GAIN_PER_CHEF = .15; // [in kW]

	// MSOL_LIMIT = 1.5;
	// kPASCAL_PER_ATM = 1D/0.00986923267 ; // 1 kilopascal = 0.00986923267 atm
	// R_GAS_CONSTANT = 8.31441; //R = 8.31441 m3 Pa K−1 mol−1
	// 1 kilopascal = 0.00986923267 atm
	// 1 cubic ft = L * 0.035315
	// A full scale pressurized Mars rover prototype may have an airlock volume of
	// 5.7 m^3

	private static final double HEIGHT = 2.5; // in meter

	/** The speed of the ventilation fan */
	private static final double CFM = 50;

	/** convert meters to feet */
	// M_TO_FT = 3.2808399;//10.764;
	/** Specific Heat Capacity = 4.0 for a typical U.S. house */
	// SHC = 6.0; // [in BTU / sq ft / °F]
	/** Building Loss Coefficient (BLC) is 1.0 for a typical U.S. house */
	// BLC = 0.2;

	/**
	 * Cooling Load Factor accounts for the fact that building thermal mass creates
	 * a time lag between heat generation from internal sources and the
	 * corresponding cooling load. It is equals to Sensible Cooling Load divided by
	 * Sensible Heat Gain
	 */
	private static final double CLF = 1.8D;

	/**
	 * The U-value in [Watts/m^2/°K] is the thermal transmittance (reciprocal of
	 * R-value) Note that Heat Loss = (1/R-value)(surface area)(∆T)
	 */
	private static final double uValue = 0.1;

	// Note : U_value will be converted to metric units in W/m²K.
	// see at
	// https://www.thenbs.com/knowledge/what-is-a-u-value-heat-loss-thermal-mass-and-online-calculators-explained

	private static final double Q_HF_FACTOR = 21.4D / 10;/// 2.23694; // 1 m per sec = 2.23694 miles per hours

	private static final double AIR_CHANGE_PER_HR = .5;

	// Molar mass of CO2 = 44.0095 g/mol
	// average density of air : 0.020 kg/m3
	// double n = weather.getAirDensity(coordinates) * vol / 44D;
	// n_CO2 = .02D * VOLUME_OF_AIRLOCK / 44*1000;
	// 1 cubic feet of air has a total weight of 38.76 g
	// n_air = 1D;
	// n_sum = n_CO2 + n_air;

	/** Specific heat capacity (C_p) of air at 300K [kJ/kg/K] */
	private static final double SPECIFIC_HEAT_CAP_AIR_300K = 1.005;
	/** Specific heat capacity (C_p) of water at 20 deg cel or 300K [kJ/kg/K] */
	private static final double SPECIFIC_HEAT_CAP_WATER_300K = 4.184;
	/** Density of dry breathable air [kg/m3] */
	private static final double DRY_AIR_DENSITY = 1.275D; //
	/** Factor for calculating airlock heat loss during EVA egress */
	private static final double ENERGY_FACTOR_EVA = SPECIFIC_HEAT_CAP_AIR_300K * ClassicAirlock.AIRLOCK_VOLUME_IN_CM
			* DRY_AIR_DENSITY / 1000;

	/**
	 * R-value is a measure of thermal resistance, or ability of heat to transfer
	 * from hot to cold, through materials such as insulation
	 */
	// R_value = 30;

	private static double uValueAreaCrackLength, uValueAreaCrackLengthAirlock;

	/** Is this building a greenhouse? */
	private boolean isGreenhouse = false;
	/** Is this building a hallway or tunnel? */
	private boolean isConnector = false;
	/** Does this building have lodging? */
	private boolean isLodging = false;
	/** Does this building have medical beds? */
	private boolean isMedical = false;
	/** Does this building have vehicle maintenance? */
	private boolean isVehicle = false;
	/** Is this building a storage bin? */
//	private boolean isStorage = false;
	/** Is this building a EVA airlock? */
	private boolean isEVA = false;
	/** Is this building a lab? */
	private boolean isLab = false;
	/** Is this building a command post? */
	private boolean isCommand = false;
	/** Is the airlock door open */
	private boolean hasHeatDumpViaAirlockOuterDoor = false;
	/** Does it generate an error in the heat transfer calculation ? */
	private boolean error;

	/** the heat gain from equipment in kW */
	private double heatGainEquipment;
	/** The width of the building. */
	private double width;
	/** The length of the building. */
	private double length;
	/** The floor area of the building. */
	private double floorArea;
	/** The area spanning the side wall. */
	private double hullArea;
	/** The Solar Heat Gain Coefficient (SHGC) of a window. */
	private double transmittance;
	/** The factor for crop with High Pressure Sodium lamp. */
	private double LAMP_GAIN_FACTOR = Crop.LOSS_FACTOR_HPS;
	/** The cache heat required for heating. */
	private double reqHeatCache;
	/** The cache heat generated for heating. */
	private double heatGenCache;
	/** The initial net heat gain/loss due to thermal system. */
	private double preNetHeatCache;
	/** The heat gain of the thermal system. */
	private double heatGainCache;
	/** The heat loss of the thermal system. */
	private double heatLossCache;
	/** The post net heat gain/loss due to thermal system. */
	private double postNetHeatCache;
	/** The excess heat pumped in from server room's equipment. */
	private double excessHeatCache;
	/** The incoming heat arriving at this building by the ventilation system. */
	private double passiveVentHeatCache;
	/** The outgoing heat leaving this building by the ventilation system. */
	private double activeVentHeatCache;
	/** The base power down heat requirement from buildings.xml. */
	private double basePowerDownHeatRequirement = 0;
	/** The U value of the ceiling or floor. */
	private double uValueAreaCeilingFloor; // Thermal Transmittance
	/** The U value of the wall. */
	private double uValueAreaWall;
	/** The current temperature of this building. */
	private double currentTemperature;
	/** The preset temperature of this building. */
	private double tPreset;
	/** The air heat sink and the water heat sink. */
	private double[] heatSink = new double[2];
	/** The air heat sink limit and the water heat sink limit. */
	private double[] heatSinkLimit = new double[2];

	/** The square root of the area of this building. */
	private double areaFactor;
	/** The slice of time [in seconds] per millisol. */
//	private double timeSlice = MarsTime.SECONDS_PER_MILLISOL / PER_UPDATE;
	/** The delta temperature due to the heat transfer. */
	private double deltaTCache;
	/**
	 * The deviation temperature (between the current temperature and the preset
	 * temperature).
	 */
	private double devTCache;
	/**
	 * The conversion factor regarding the mass of the air moisture within this
	 * building.
	 */
//	private double convFactorAirMoisture;

	private Coordinates location;

	private Building building;

	private List<Building> adjacentBuildings;

	protected static SurfaceFeatures surface;
	protected static Weather weather;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 */
	public Heating(Building building, FunctionSpec spec) {
		// Call Function constructor.
//		super(FUNCTION, spec, building);
		this.building = building;

		location = building.getSettlement().getCoordinates();
		length = building.getLength();

		if (length < 0) {
			logger.severe(building, "length: " + length);
		}

		width = building.getWidth();

		floorArea = building.getFloorArea();

		areaFactor = building.getAreaFactor();

		switch (building.getCategory()) {
		case CONNECTION:
			isConnector = true;
			heatGainEquipment = 0.0879;
			break;
		case VEHICLE:
			isVehicle = true;
			heatGainEquipment = 1.0;
			break;
		case FARMING:
			isGreenhouse = true;
			heatGainEquipment = 0.25;
			break;
		case EVA:
			isEVA = true;
			heatGainEquipment = 0.586;
			break;
		case COMMAND:
			isCommand = true;
			heatGainEquipment = 0.4;
			break;
		case COMMUNICATION:
			heatGainEquipment = 0.586;
			break;
		case WORKSHOP:
			heatGainEquipment = 0.7034;
			break;
		case LABORATORY:
			isLab = true;
			heatGainEquipment = 0.4396;
			break;
		case LIVING:
			isLodging = true;
			heatGainEquipment = 0.7034;
			break;
		case MEDICAL:
			isMedical = true;
			heatGainEquipment = 0.586;
			break;
//			case STORAGE:
//				isStorage = true;
//				heatGainEquipment = 0.0879;
//				break;
		case ERV:
			heatGainEquipment = 0.586;
			break;
		default:
			heatGainEquipment = 0.117;
			break;
		}
		// transmittance or SHGC range from 0 (completely blocking solar radiation) to
		// 1 (completely transmitting solar radiation).

		if (isGreenhouse) {
			// Note that greenhouse has a semi-transparent rooftop
			// Include the 4 side walls
			// Take only half of the height of the 2 side walls
			// Take the full front and back side walls
			hullArea = width * HEIGHT * 2 + length * HEIGHT + floorArea;
			// SHGC values range from 0 (completely blocking solar radiation) to
			// 1 (completely transmitting solar radiation).
			transmittance = TRANSMITTANCE_GREENHOUSE_HIGH_PRESSURE;
		} else {
			// Include the 4 side walls and ceiling
			hullArea = (width + length) * HEIGHT * 2 + floorArea;

			transmittance = TRANSMITTANCE_WINDOW;
		}

		uValueAreaWall = uValue * 2D * (width + length) * HEIGHT;

		uValueAreaCeilingFloor = uValue * floorArea;
		// assuming airChangePerHr = .5 and q_H = 21.4;
		uValueAreaCrackLength = 0.244 * .075 * AIR_CHANGE_PER_HR * Q_HF_FACTOR * (4 * (.5 + .5));
		// assuming four windows
		uValueAreaCrackLengthAirlock = 0.244 * .075 * AIR_CHANGE_PER_HR * Q_HF_FACTOR * (2 * (2 + 6) + 4 * (.5 + .5));
		// assuming two EVA airlock

		tPreset = building.getPresetTemperature();

		currentTemperature = tPreset;
	}

	/**
	 * Gets the temperature of a building.
	 * 
	 * @return temperature (deg C)
	 */
	public double getCurrentTemperature() {
		return currentTemperature;
	}

	/**
	 * Determines the heat gain or loss and the new indoor temperature.
	 * 
	 * @param millisols time in millisols
	 * @return the new temperature in degree celsius
	 */
	private double[] determineHeatTemperature(double millisols) {
		// NOTE: THIS IS A 7-PART CALCULATION

		// (1) CALCULATE TEMPERATURES
		// The indoor temperature in deg celsius
		double inTCelsius = getCurrentTemperature();

		// The outside temperature in deg celsius
		double outTCelsius = building.getSettlement().getOutsideTemperature();

		error = checkError("outTCelsius", +Math.round(outTCelsius * 10.0) / 10.0, 100) || error;

		if (error || outTCelsius < -100) {
			logger.warning(building, 20_000, "outTCelsius: " + Math.round(outTCelsius * 10.0) / 10.0 + "  inTCelsius: "
					+ Math.round(inTCelsius * 10.0) / 10.0);
		}

		// Find the temperature difference between outside and inside
		double deltaTinTout = inTCelsius - outTCelsius; // 1.8 = 9D / 5D;

		// °C x 9/5 + 32 = °F
		// (°F - 32) x 5/9 = °C

		// The indoor temperature in kelvin
		double inTKelvin = inTCelsius + C_TO_K;
		// The outside temperature in kelvin
		double outTKelvin = outTCelsius + C_TO_K;

		int numEVAgoers = building.numOfPeopleInAirLock(); // if num > 0, this building has an airlock

		// Convert from W to kW
		double irradiance = surface.getSolarIrradiance(location) / SurfaceFeatures.MAX_SOLAR_IRRADIANCE;
		// if sunlight = 25 W/m2, I = 25/717 = 0.035 kW/m2

//		error = checkError("I", irradiance, 1) || error;

		// (2) CALCULATE HEAT GAIN

		// Note: a new convention is used in that heat gain is positive and heat loss is
		// negative

		// Reference :
		// 1. Engineering concepts for Inflatable Mars Surface Greenhouses
		// at http://ntrs.nasa.gov/search.jsp?R=20050193847
		// Full ver at http://www.marshome.org/files2/Hublitz2.pdf
		// Revised ver at
		// https://www.researchgate.net/publication/7890528_Engineering_concepts_for_inflatable_Mars_surface_greenhouses

		// (2a) CALCULATE HEAT GENERATED BY DIRECTING HEATING FROM THE LAST INTERVAL OF
		// TIME

		double[] gainValue = calculateHeatGain(numEVAgoers, irradiance);
		double gain = gainValue[0];
		double canopyHeatGain = gainValue[1];
//		logger.info(building, "gain: " + Math.round(gain * 100.0)/100.0 + " kW");
		// Set heat gain
		setHeatGain(gain);

		// (3) CALCULATE HEAT LOSS

		double loss = calculateHeatLoss(canopyHeatGain, outTCelsius, deltaTinTout, inTKelvin, outTKelvin, irradiance,
				numEVAgoers);
//		logger.info(building, "loss: " + Math.round(loss * 100.0)/100.0 + " kW");
		// Set heat gain
		setHeatLost(loss);

		// (4) CALCULATE THE NET HEAT

		// (4a) FIND THE DIFFERENCE between heat gain, heat loss
		double diffHeatGainLoss = gain + loss;
//		logger.info(building, "diffHeatGainLoss: " + Math.round(diffHeatGainLoss * 100.0)/100.0 + " kW");
		// Set the initial net heat transfer
		setPreNetHeat(diffHeatGainLoss);
//		logger.info(building, 0, "setPreNetHeat - diffHeatGainLoss: " + Math.round(diffHeatGainLoss * 100.0)/100.0);
		error = checkError("diffHeatGainLoss", diffHeatGainLoss, 30) || error;

		if (diffHeatGainLoss > 30) {
			logger.warning(building, "diffHeatGainLoss: " + Math.round(diffHeatGainLoss * 10.0) / 10.0 + " kW");
			error = true;
		} else if (diffHeatGainLoss < -30) {
			logger.warning(building, "diffHeatGainLoss: " + Math.round(diffHeatGainLoss * 10.0) / 10.0 + " kW");
			error = true;
		}

		double seconds = Math.max(0.011, Math.min(2, MarsTime.SECONDS_PER_MILLISOL * millisols));

		// Step 4b : USE VENT HEAT VIA ACTIVE VENTILATION TO BRING IN HEAT FROM
		// NEIGBORING BUILDINGS

		// 4b1: Find activeVentHeat in kW
		double activeVentHeat = calculateActiveVentHeat(diffHeatGainLoss, inTCelsius, millisols);

		// if activeVentHeat is positive, suck hotter air from adjacent buildings, thus
		// having hotter air
		// to come in and raise this building temperature
		if (activeVentHeat > 0) {
			// Bringing in extra heat for this building reduces the required heat needed to
			// be generated by this building
			if (activeVentHeat > 10) {
				logger.warning(building, 20_000,
						"activeVentHeat: " + Math.round(activeVentHeat * 100.0) / 100.0 + ".");
				error = true;
			}
		}
		// if activeVentHeat is negative, dump hotter air to adjacent buildings, thus
		// allowing colder air
		// to come in and lower the building temperature
		else if (activeVentHeat < 0) {
			// Pushing out extra heat from this building
			if (activeVentHeat < -10) {
				logger.warning(building, 20_000,
						"activeVentHeat: " + Math.round(activeVentHeat * 100.0) / 100.0 + ".");
				error = true;
			}
		}
		
		diffHeatGainLoss += activeVentHeat;

		error = checkError("activeVentHeat", activeVentHeat, 30) || error;
		// Set the active heat initiated by this building
		setActiveVentHeat(activeVentHeat);
		
//		if (activeVentHeat > 0 || activeVentHeat < 0) {
//			// Note: Even though activeVentHeat can reduce the delta heat,
//			// it needs to recompute the temperature
//			double activeT = computeNewT(newT, activeVentHeat, millisols * MarsTime.SECONDS_PER_MILLISOL);	
//			// Set the temperature and call unitUpdate
//			setTemperature(activeT);
//			if (activeT > 0.1 || activeT < -0.1)
//				logger.info(building, 20_000, 
//					"T: " + Math.round(newT * 1000.0) / 1000.0 
//					+ " -> " + Math.round(activeT * 1000.0) / 1000.0
//					+ "  activeVentHeat: " + Math.round(activeVentHeat * 1000.0) / 1000.0);
//			newT = activeT;
//		}
		
		
//		logger.info(building, 20_000, 
//				"millisols: " + Math.round(millisols * 1000.0)/1000.0
//				+ "  upperBound: " + Math.round(upperBound * 10.0)/10.0
//				+ "  lowerBound: " + Math.round(lowerBound * 10.0)/10.0
//				+ "  seconds: " + Math.round(seconds * 1000.0)/1000.0);

		// Note: This nowT is a reference for calculating how much air can sink the heat
		// only
		// and will not be used for adjusting indoor temperature
//?		double activeT = computeNewT(inTCelsius, diffHeatGainLoss, seconds);

		double activeT = inTCelsius;
				
		// (5) APPLY THE HEAT SINKS - AIR MOISTURE AND WATER

		// (5a) Find the air moisture heat sink

		double convFactor = 0;

		// e.g. Fish Farm and Algae Pond, etc.
		double waterMass = 0;

		if (building.getFishery() != null) {
			waterMass = building.getFishery().getTankSize() / 4;
		} else if (building.getAlgae() != null) {
			waterMass = building.getAlgae().getWaterMass() / 4;
		}

		// Future: account for the size of water tank in each building

		// Assume water in restroom, holding tank or pipes contribute to the
		// mass of water as heat sink
		if (isConnector) { // 2m*length
			waterMass = floorArea;
		} else if (isGreenhouse) { // 6m*9m, 5m*10m, 12m*18m
			waterMass += floorArea / 2;
		} else if (isCommand) { // 7m*9m, 10m*10m
			waterMass = floorArea / 4;
		} else if (isEVA) { // 6m*4m
			waterMass = floorArea;
		} else if (isVehicle) { // 12x16, 15x18
			waterMass = floorArea / 3;
		}

		// Note that Lander Hab should have multiple functions but is not registering
		// other yet.

		else if (isLodging) { // 7m*9m, 10m*10m
			waterMass += floorArea / 5;
		}

		else if (isMedical) { // 7m*9m, 10m*10m
			waterMass += floorArea / 6;
		}

		// Future: Compute the exact amount of the water mass for server farm
		// based on # of Computing Unit (CU).

		else if (isLab) { // 7m*9m, 10m*10m
			waterMass += floorArea / 7;
		} else {
			waterMass = floorArea / 10;
		}

		// Future: pack water bladder into the inner wall of each building

		// (5b) Does it have any body of water

		double nowT = 0;
		
		if (waterMass > 0) {
			// (5b1) Yes. Calculate the water heat sink

			// Note: This nowT is a reference for calculating how much water can sink the
			// heat only
			// and will not be used for adjusting indoor temperature
//			nowT = computeNewT((activeT + inTCelsius) / 2, diffHeatGainLoss, seconds);

			double[] value2 = computeWaterHeatSink((activeT + inTCelsius) / 2, diffHeatGainLoss, waterMass, seconds);

			// dHeat2 is in kW, not kJ
			double dHeatWater = value2[0];
//			logger.info(building, 0, "waterMass - dHeatWater: " + Math.round(dHeatWater * 100.0)/100.0);
			// Update diffHeatGainLoss
			diffHeatGainLoss += dHeatWater;
//			logger.info(building, 0, "waterMass - diffHeatGainLoss: " + Math.round(diffHeatGainLoss * 100.0)/100.0);

			double convFactorWater = value2[1];

			// Set convFactor as convFactorWater
			convFactor = convFactorWater;
		} else {
			// (5b2) No. Save the net heat after applying the air heat sink only to buffer
			// the net heat
		}

//		logger.info(building, 0, "setPostNetHeat: " + Math.round(getPostNetHeat() * 100.0)/100.0);

		// (5b) Find the air moisture heat sink

		double[] value1 = computeAirHeatSink((nowT + inTCelsius) / 2, diffHeatGainLoss, seconds);
		double dHeatAir = value1[0];
		double convFactorAir = value1[1];

		// Update diffHeatGainLoss
		diffHeatGainLoss += dHeatAir;
//		logger.info(building, 0, "airMass - diffHeatGainLoss: " + Math.round(diffHeatGainLoss * 100.0)/100.0);

		// First assume using convFactorAir
		convFactor = convFactorAir;

		// (6) CALCULATE THE INSTANTANEOUS CHANGE OF TEMPERATURE (DELTA T) (in degrees
		// celsius)

		// post net heat is in kW, not kJ

		// Note: multiply getPostNetHeat() with seconds cause temperature instability

		error = checkError("diffHeatGainLoss", diffHeatGainLoss, 30) || error;

		setPostNetHeat(diffHeatGainLoss);

		// use the last good inTCelsius to derive the real nowT
		nowT = computeNewT(inTCelsius, diffHeatGainLoss, seconds);

//		logger.info(building, 0, "nowT: " + Math.round(nowT * 100.0)/100.0);
		return new double[] { nowT, convFactor };
	}

	/**
	 * Computes the new temperature based on the entropy and the ideal gas law.
	 * 
	 * @param oldTC in deg celsius
	 * @param deltaHeatkW heat change in kW
	 * @return
	 */
	private double computeNewT(double oldTC, double deltaHeatkW, double seconds) {
		double k = C_TO_K + oldTC;
		double numMoles = 0;
		LifeSupport ls = building.getLifeSupport();
		if (ls != null)
			numMoles = ls.getAir().getTotalNumMoles();
		else
			return 0;

		// ΔS = Q / T = [J] / [K] = [J/K]
		// delta entropy in [J/K] or [kJ / 1000 / K] or [kW * seconds / 1000 / K]
		// Since J = Ws

		double entropyChange = deltaHeatkW * seconds * 1000 / k;

		double ratio = entropyChange / GAS_CONSTANT / numMoles;

		// newT in [C]
		double newTC = k * Math.exp(ratio) - C_TO_K;
	
		// T2 = T1 * exp(ΔS / (nR))
		// n = 0.0821 L·atm/mol·K
		// R = 0.0289 kg/mol

		if (newTC > 40) {
			logger.warning(building, 20_000, "newT: " + Math.round(newTC * 100.0) / 100.0 + " > 40.");
			error = true;
		}

		else if (newTC < -40) {
			logger.warning(building, 20_000, "newT: " + Math.round(newTC * 100.0) / 100.0 + " < -40.");
			error = true;
		}

		if (error)
			logger.info(building, 20_000,
				"computeNewT - "
				+ "T: " + Math.round(oldTC * 1000.0) / 1000.0 
				+ " -> " + Math.round(newTC * 1000.0) / 1000.0
				+ "  deltaHeatkW: " + Math.round(deltaHeatkW * 100.0) / 100.0 + " kW" 
				+ "  Math.exp(ratio): " + Math.round(Math.exp(ratio) * 1000.0) / 1000.0
				+ "  ratio: " + Math.round(ratio * 1000.0) / 1000.0
				+ "  entropyChange: " + Math.round(entropyChange * 1_000.0) / 1_000.0 + " kJ/K" 
				+ "  numMoles: " + Math.round(numMoles * 10.0) / 10.0 
				+ "  seconds: " + Math.round(seconds * 100.0) / 100.0);
		return newTC;
	}
	
	/**
	 * Estimates the required heat to produce. See
	 * http://electron6.phys.utk.edu/PhysicsProblems/Mechanics/9-Gereral%20Physics/Entropy.html
	 * 
	 * @param nowT
	 * @param millisols
	 * @return
	 */
	private double estimateRequiredHeat(double nowT, double millisols) {
		// if devT is positive, needs to raise temperature by increasing heat
		// if devT is negative, needs to low temperature by transferring heat away

		double seconds = millisols * MarsTime.SECONDS_PER_MILLISOL;

		double k = C_TO_K + nowT;
		double tPresetK = C_TO_K + tPreset;
		double logTs = Math.log(tPresetK/k);
		double numMoles = building.getLifeSupport().getAir().getTotalNumMoles();
		double nR = GAS_CONSTANT * numMoles;

		// ΔS = Q / T = [J] / [K] = [J/K]
		// delta entropy in [J/K] or [kJ / 1000 / K] or [kW * seconds / 1000 / K]
		// Since J = W * s
		// [J/K] = [kW * seconds / 1000 / K]

		// 1 Wh = 3.6 kJ
		// 1 kWh = 3.6 MJ
		// 1 W = J / s
		// 1 kW = kJ / s

		double entropyChange = logTs * nR;
		
		// if devT is positive, needs to raise temperature by increasing heat
		// if devT is negative, needs to low temperature by transferring heat away
		double deltaHeatJ = entropyChange * k;

//		// Find the estimate required heat in kW (not kWh)
		// Note: the advantage of using kW, instead of kWh is that kW can be compared
		// across the board
		double estReqHeatkW = deltaHeatJ / 1000.0 / seconds;

		if (estReqHeatkW > 40) {
			logger.warning(building, 20_000,
					"estReqHeatkW: " + Math.round(estReqHeatkW * 100.0) / 100.0 + " > 40.");
			error = true;
		}

		else if (estReqHeatkW < -40) {
			logger.warning(building, 20_000, "estReqHeat: " + Math.round(estReqHeatkW * 10000.0) / 10000.0 + " < -40.");
			error = true;
		}

		if (error)
			logger.info(building, 20_000,
					"estimateRequiredHeat - " + "estReqHeat: " + Math.round(estReqHeatkW * 100.0) / 100.0 + " kW"
					+ "  nowT: " + Math.round(nowT * 10.0) / 10.0
					+ "  deltaHeatJ: " + Math.round(deltaHeatJ * 10.0) / 10.0 + " J" 
					+ "  millisols: " + Math.round(millisols * 1000.0) / 1000.0 
					+ "  seconds: " + Math.round(seconds * 1000.0) / 1000.0 
					+ "  logTs: " + Math.round(logTs * 1000.0) / 1000.0 
					+ "  entropyChange: " + Math.round(entropyChange * 100.0) / 100.0 + " J/K" 
					+ "  nR: " + Math.round(nR * 10.0) / 10.0 
					+ "  numMoles: " + Math.round(numMoles * 10.0) / 10.0);

//		estReqHeatkW = Math.max(-40, Math.min(40, estReqHeatkW));

		return estReqHeatkW;
	}

	/**
	 * Calculates the heat gain.
	 * 
	 * @param numEVAgoers
	 * @param error
	 * @return
	 */
	private double[] calculateHeatGain(int numEVAgoers, double irradiance) {
		// Add the heat generated in response to the rise or drop in temperature from
		// last refresh cycle
		double heatGenCache = getHeatGenerated();

		error = checkError("heatGenCache", heatGenCache, 30) || error;

		// Add the excess heat from computation
		double excessHeat = getExcessHeat();

		error = checkError("excessHeat", excessHeat, 10) || error;

		// (2a) CALCULATE HEAT GAIN BY HEAT GEN FROM LAST CYCLE AND EXCESS HEAT FROM
		// COMPUTING

		double heatPumpedIn = heatGenCache + excessHeat;

		// (2b) CALCULATE HEAT GAIN BY KITCHEN FOOD PREPARATION
		double heatGainChief = 0;
		if (building.getFoodProduction() != null) {
			heatGainChief = HEAT_GAIN_PER_CHEF * building.getFoodProduction().getNumOccupiedActivitySpots();
		}
		if (building.getCooking() != null) {
			heatGainChief += HEAT_GAIN_PER_CHEF * building.getCooking().getNumOccupiedActivitySpots();
		}

		// (2c) CALCULATE HEAT GAIN BY PEOPLE
		double heatGainOccupants = HEAT_DISSIPATED_PER_PERSON * building.getNumPeople();
		// the energy required to heat up the in-rush of the new martian air

		// (2d) CALCULATE HEAT GAIN BY EVA HEATER

		double heatGainFromEVAHeater = 0;
		if (numEVAgoers > 0)
			heatGainFromEVAHeater = building.getTotalPowerForEVA() / 2D;

		error = checkError("heatGainFromEVAHeater", heatGainFromEVAHeater, 10) || error;

		// divide by 2 since half of the time a person is doing ingress
		// Note : Assuming EVA heater requires .5kW of power for heating up the air for
		// each person in an airlock during EVA ingress.

		// (2e) CALCULATE SOLAR HEAT GAIN

		// Solar heat gain is the amount of solar radiation that enters a
		// building through windows and other openings.
		// The transmittance of a window is a measure of how much solar radiation
		// it allows to pass through

		double solarHeatGain = 0;

		if (irradiance > 0) {
			if (isGreenhouse) {
				solarHeatGain = irradiance * transmittance * hullArea * .055;
			}

			else if (isConnector) {
				solarHeatGain = irradiance * transmittance * hullArea * .025;
			}

			else {
				solarHeatGain = irradiance * transmittance * hullArea * .01;
			}
		}

//		logger.info(building, "I: " + Math.round(I * 100.0)/100.0
//				+ "  solarHeatGain: " + Math.round(solarHeatGain * 100.0)/100.0);

		error = checkError("solarHeatGain", solarHeatGain, 20) || error;

		// (2f) CALCULATE INSULATION HEAT GAIN
		double canopyHeatGain = 0;
		double coeff = 0;

		// Note: Whenever the sun is about to go down, unfold the outer canopy over the
		// structure to prevent heat loss

		if (irradiance < 0.05) {

			// If temperature inside is too low, will automatically close
			// the window, blind or curtain partially to block the heat from radiating
			// away to stop cool off the building.

			switch (building.getConstruction()) {
			case INFLATABLE:
				coeff = LARGE_INSULATION_CANOPY;
				break;

			case SEMI_ENGINEERED:
				coeff = HALLWAY_INSULATION;
				break;

			default:
				coeff = INSULATION_BLANKET;
			}

			// Assume high indoor temperature would lower canopyHeatGain
			double ratioT = C_TO_K / (C_TO_K + getCurrentTemperature());
			canopyHeatGain = 0.7 * coeff * (.718 - irradiance) * ratioT * ratioT;
		}

		error = checkError("canopyHeatGain", canopyHeatGain, 15) || error;

		// (2g) CALCULATE HEAT GAIN DUE TO ARTIFICIAL LIGHTING
		double lightingGain = 0;

		// Case 1: Specialized Lighting for Crop
		if (isGreenhouse && building.getFarming() != null) {
			// Currently, greenhouses uses high pressure sodium lamps with the assumption
			// of having 60% invisible radiation (energy loss as heat)
			lightingGain = building.getFarming().getTotalLightingPower() * LAMP_GAIN_FACTOR;

//			logger.info(building, 60_000, "lightingGain: " + Math.round(lightingGain * 10.0)/10.0);
		}

		// Case 2: General Lighting
		if (irradiance < 0.075) {
			// The general lighting requirement is based upon the floor area and the
			// sunlight intensity
			lightingGain += (.718 - irradiance) * floorArea / 100;

//			logger.info(building, 60_000, "I: " + Math.round(irradiance * 10.0)/10.0
//					+ "  lightingGain: " + Math.round(lightingGain * 10.0)/10.0);
		}

		error = checkError("lightingGain", lightingGain, 15) || error;

		// (2h) ADD HEAT GAIN BY EQUIPMENT
		// see heatGainEqiupment below

		// (2i) CALCULATE TOTAL HEAT GAIN
		double gain = heatPumpedIn + heatGainChief + heatGainOccupants + heatGainFromEVAHeater + solarHeatGain
				+ canopyHeatGain + lightingGain + heatGainEquipment;

		// (2j) Calculate the passive vent heat due to ventilation by adjacent buildings

		double passiveVentHeat = getPassiveVentHeat();

		error = checkError("ventInHeat", passiveVentHeat, 30) || error;

		if (passiveVentHeat > 0) {
			// Note: Ensure ventInHeat is positive when adding to the heat gain
			gain += passiveVentHeat;
			// Reset vent in heat back to zero
			setPassiveVentHeat(0);
		}

		error = checkError("gain", gain, GAIN_LIMIT) || error;

		/**
		 * Do NOT delete. For future Debugging.
		 */
		if (error)
			logger.warning(building, 20_000,
					"Gain: " + Math.round(gain * 100.0) / 100.0 + "  passiveVentHeat: "
							+ Math.round(passiveVentHeat * 100.0) / 100.0 + "  heatGenCache: "
							+ Math.round(heatGenCache * 100.0) / 100.0 + "  excessHeat: "
							+ Math.round(excessHeat * 100.0) / 100.0 + "  Occupants: "
							+ Math.round(heatGainOccupants * 100.0) / 100.0 + "  numEVAgoers: " + numEVAgoers
							+ "  EVAHeater: " + Math.round(heatGainFromEVAHeater * 100.0) / 100.0 + "  solarHeatGain: "
							+ Math.round(solarHeatGain * 100.0) / 100.0 + "  canopyHeatGain: "
							+ Math.round(canopyHeatGain * 100.0) / 100.0 + "  lighting: "
							+ Math.round(lightingGain * 100.0) / 100.0 + "  Equipment: "
							+ Math.round(heatGainEquipment * 100.0) / 100.0);

		return new double[] { gain, canopyHeatGain };
	}

	/**
	 * Calculates the heat loss.
	 * 
	 * @param error
	 * @return
	 */
	private double calculateHeatLoss(double canopyHeatGain, double outTCelsius, double deltaTinTout, double inTKelvin,
			double outTKelvin, double irradiance, int numEVAgoers) {
		// (3a) CALCULATE HEAT NEEDED FOR REHEATING AIRLOCK

		// Note that if the heat is negative, it means loss of heat

		double heatAirlock = 0;
		// the energy loss due to gushing out the warm settlement air when airlock
		// is open to the cold Martian air

		if (numEVAgoers > 0 && hasHeatDumpViaAirlockOuterDoor) {
			heatAirlock = -ENERGY_FACTOR_EVA * (DEFAULT_ROOM_TEMPERATURE - outTCelsius) * numEVAgoers;
			// flag that this calculation is done till the next time when
			// the airlock is depressurized.
			hasHeatDumpViaAirlockOuterDoor = false;
		}

		error = checkError("heatAirlock", heatAirlock, -10) || error;

		// (3b) CALCULATE HEAT LOSS DUE TO STRUCTURE
		double structuralLoss = 0;
		// Note: deltaT is positive if indoor T is greater than outdoor T
		if (numEVAgoers > 0) {
			structuralLoss = -CLF * deltaTinTout * (uValueAreaCeilingFloor * 2D + uValueAreaWall
					+ uValueAreaCrackLengthAirlock * weather.getWindSpeed(location)) / 1000 / 1.1;
			// Note : 1 m/s = 3.28084 ft/s = 2.23694 miles per hour
		} else {
			if (isGreenhouse) {
				structuralLoss = -CLF * deltaTinTout * (uValueAreaCeilingFloor + uValueAreaWall
						+ uValueAreaCrackLength * weather.getWindSpeed(location)) / 1000 / 1.1;
			} else {
				structuralLoss = -CLF * deltaTinTout * (uValueAreaCeilingFloor * 2D + uValueAreaWall
						+ uValueAreaCrackLength * weather.getWindSpeed(location)) / 1000 / 1.1;
			}
		}

		error = checkError("structuralLoss", structuralLoss, 10) || error;

		// Note : U_value in kW/K/m2, not [Btu/°F/ft2/hr]

		// (3d) CALCULATE HEAT LOSS DUE TO HEAT RADIATED TO OUTSIDE
		double solarHeatLoss = 0;

		double canopyFactor = (1 + canopyHeatGain) * 2;
		// Note: canopyFactor represents the result of closing the canopy on the side
		// wall
		// to avoid excessive heat loss for the greenhouse,
		// especially in the evening

		error = checkError("canopyFactor", canopyFactor, 15) || error;

		double emissivity = 0;
		// Note: emissivity refers to the effectiveness of a surface to emit
		// thermal radiation. It is a measure of how well a surface radiates
		// heat, with a value ranging from 0 (perfect reflector) to 1 (perfect
		// absorber).

		// In a greenhouse, emissivity plays a significant role in heat loss due to
		// radiation. When a surface has a high emissivity, it radiates heat more
		// efficiently, which can lead to increased heat loss.

		// Conversely, a surface with a low emissivity reflects more heat back into
		// the greenhouse, reducing heat loss.

//		Glass: 0.88 to 0.92 (depending on the type of glass and its surface finish)
//		Marble: 0.89 to 0.92 (depending on the type of marble and its surface finish)
//		Aluminum: 0.03 (low emissivity, often used in low-e coatings)
//		Steel: 0.5 to 0.7 (depending on the type of steel and its surface finish)
//		Concrete: 0.8 to 0.9 (depending on the type of concrete and its surface finish)

		if (isGreenhouse) {
			emissivity = (EMISSIVITY_DAY * irradiance + EMISSIVITY_NIGHT * (0.718 - irradiance)) * 0.85;
			// e.g. I = 0, e = .8 * 0 + 1.0 * (.7 - 0) = .7 * 1.1
			// e.g. I = .1, e = .8 *.1 + 1.0 * (.7 - .1) = .68 * 1.1
			// e.g. I = .3, e = .8 *.3 + 1.0 * (.7 - .3) = .64 * 1.1
			// e.g. I = .54, e = .8 *.54 + 1.0 * (.7 - .54) = .592 * 1.1
			if (emissivity > 1)
				emissivity = 1;
			else if (emissivity < .3)
				emissivity = .3;

			solarHeatLoss = -emissivity * STEFAN_BOLTZMANN_CONSTANT * (Math.pow(inTKelvin, 4) - Math.pow(outTKelvin, 4))
					* hullArea / canopyFactor / 1000D / 2;
		}
//		else if (isBrick)  {
//			For Outpost Hub, Bunkhouse, Tunnel, Inground Greenhouse, Loading Dock garage	
//			will need to model emissivity for these in-situ construction
//		}
		else {
			emissivity = EMISSIVITY_DAY * irradiance * .1 + EMISSIVITY_NIGHT * (0.718 - irradiance) * .35;
			// e.g. I = 0, e =
			// e.g. I = .1, e =
			// e.g. I = .3, e =
			// e.g. I = .54, e =
			if (emissivity > 1)
				emissivity = 1;
			else if (emissivity < .15)
				emissivity = .15;

			solarHeatLoss = -emissivity * STEFAN_BOLTZMANN_CONSTANT * (Math.pow(inTKelvin, 4) - Math.pow(outTKelvin, 4))
					* hullArea / canopyFactor / 1000D;
		}

//		logger.info(building, "emissivity: " + Math.round(emissivity * 100.0)/100.0
//				+ "  solarHeatLoss: " + Math.round(solarHeatLoss * 100.0)/100.0);

		error = checkError("emissivity", emissivity, 15) || error;

		error = checkError("solarHeatLoss", solarHeatLoss, -20) || error;

		/**
		 * Do NOT delete. For future Debugging.
		 */
		if (error || solarHeatLoss < -20)
			logger.warning(building, 20_000, // "inTKelvin: " + inTKelvin
//					+ "   outTKelvin: " + outTKelvin
					"I: " + Math.round(irradiance * 1000.0) / 1000.0 + "  canopyFactor: "
							+ Math.round(canopyFactor * 1000.0) / 1000.0 + "  canopyHeatGain: "
							+ Math.round(canopyHeatGain * 1000.0) / 1000.0 + "  emissivity: "
							+ Math.round(emissivity * 1000.0) / 1000.0 + "  solarHeatLoss: "
							+ Math.round(solarHeatLoss * 1000.0) / 1000.0);

		// (3e) At high RH, the air has close to the maximum water vapor that it can
		// hold,
		// so evaporation, and therefore heat loss, is decreased.

		// (3f) CALCULATE TOTAL HEAT LOSS

		// Note: the new convention is that heat gain is positive and heat loss is
		// negative

		double loss = heatAirlock + structuralLoss + solarHeatLoss;
		// loss is negative by definition

		if (passiveVentHeatCache < 0) {
			// Note: Ensure ventInHeat is negative when adding to the heat loss
			loss += passiveVentHeatCache;
			// Reset vent in heat back to zero
			setPassiveVentHeat(0);
		}

		error = checkError("loss", loss, LOSS_LIMIT) || error;

		/**
		 * Do NOT delete. For future Debugging.
		 */
		if (error)
			logger.warning(building, 20_000,
					"Loss: " + Math.round(loss * 1000.0) / 1000.0 + " kW" + "  heatAirlock: "
							+ Math.round(heatAirlock * 1000.0) / 1000.0 + "  structuralLoss: "
							+ Math.round(structuralLoss * 1000.0) / 1000.0 + "  solarHeatLoss: "
							+ Math.round(solarHeatLoss * 1000.0) / 1000.0 + "  ventInHeat: "
							+ Math.round(passiveVentHeatCache * 1000.0) / 1000.0);

		return loss;
	}

	/**
	 * Computes the heat transfer by applying the air heat sink.
	 * 
	 * @param nowT     in deg celsius
	 * @param diffHeat
	 * @param seconds
	 * @param error
	 * @return
	 */
	private double[] computeAirHeatSink(double nowT, double diffHeat, double seconds) {

		double airMass = building.getLifeSupport().getAir().getTotalMass();

//		error = checkError("airMass", airMass) || error;

		// References :
		// (1) https://en.wiktionary.org/wiki/humid_heat
		// (2)
		// https://physics.stackexchange.com/questions/45349/how-air-humidity-affects-how-much-time-is-needed-for-heating-the-air

		// C_s = 0.24 + 0.45H where 0.24 BTU/lb°F is the heat capacity of dry air, 0.45
		// BTU/lb°F is the heat capacity
		// of water vapor, and SH is the specific humidity, the ratio of the mass of
		// water vapor to that of dry air
		// in the mixture.

		// In SI units, cs = 1.005 + 1.82 * SH where
		// 1.005 kJ/kg°C is the heat capacity of dry air,
		// 1.82 kJ/kg°C the heat capacity of water vapor,
		// and SH is the specific humidity in kg water vapor per kg dry air in the
		// mixture.

		double percentAirMoisture = 0;

		LifeSupport ls = building.getLifeSupport();
		if (ls != null)
			/** The percent of the air is moisture. Assume 1%. */
			percentAirMoisture = ls.getAir().getGas(ResourceUtil.waterID).getPercent();

//		double airMoistureMass = gas.getMass();

		/** The specific heat capacity (C_s) of the air with moisture. */
		double heatCapAirMoisture = SPECIFIC_HEAT_CAP_AIR_300K + 1.82 * percentAirMoisture / 100;

		// Q: should airMoistureMass (instead of airMass) be used instead ?
		// A: No. heatCapAirMoisture includes all elements in the air, not just water
		// moisture

		// [kJ/°C] = [kJ/kg°C] * [kg]
		double airHeatCap = heatCapAirMoisture * airMass;

//		error = checkError("airHeatCap", airHeatCap) || error;

		double tRise = 0;
		double efficiency = .9;
		if (nowT > 0)
			// The smaller the nowT, the bigger the tRise-+
			tRise = 1 + tPreset / nowT * efficiency;

		// Q = mcΔT
		// kJ = kJ/(kg°C) * kg * °C
		// kJ/s = kJ/(kg°C) * kg * °C / s

		// kJ * 3600 = kWh
		// 1 kW = 1 kJ/s

		// [kW] = [kJ/°C] / [seconds] * [°C]
		// [kW/°C] = [kJ/°C] / [seconds]

		double airHeatSink = airHeatCap / seconds * tRise;

		// e.g. EVA Airlock
		// airMass: 24.956 [kg]
		// heatCapAirMoisture: 1.109 [kJ/kg°C]
		// airHeatCap: 27.673 [kJ/°C]
		// airHeatSink: 0.712 [kW/°C]

//		error = checkError("airHeatSink", airHeatSink) || error;

		// (5b) Use the air heat sink to buffer the net heat

		double convFactorAir = seconds / airHeatCap;

		double dHeat1 = computeHeatSink(nowT, diffHeat, airHeatSink, 0, seconds / MarsTime.SECONDS_PER_MILLISOL);

		error = checkError("dHeat1", dHeat1, 30) || error;

		/**
		 * Do NOT delete. For future Debugging.
		 */
		if (error)
			logger.warning(building, 20_000,
					"Air heat sink - " + "nowT: " + Math.round(nowT * 100.0) / 100.0 + "  dHeat1: "
							+ Math.round(dHeat1 * 1000.0) / 1000.0 + "  convFactorAir: "
							+ Math.round(convFactorAir * 1000.0) / 1000.0 + "  airMass: "
							+ Math.round(airMass * 1000.0) / 1000.0 + "  airHeatCap: "
							+ Math.round(airHeatCap * 1000.0) / 1000.0 + "  heatCapAirMoisture: "
							+ Math.round(heatCapAirMoisture * 1000.0) / 1000.0 + "  %AirMoisture: "
							+ Math.round(percentAirMoisture * 100.0) / 100.0 + "  airHeatSink: "
							+ Math.round(airHeatSink * 1000.0) / 1000.0);

		return new double[] { dHeat1, convFactorAir };
	}

	/**
	 * Computes the heat transfer by applying the water heat sink.
	 * 
	 * @param nowT
	 * @param diffHeat
	 * @param waterMass
	 * @param seconds
	 * @param error
	 * @return
	 */
	private double[] computeWaterHeatSink(double nowT, double diffHeat, double waterMass, double seconds) {

		// Use the specific heat capacity (C_s) of the air with moisture.
		double waterHeatCap = SPECIFIC_HEAT_CAP_WATER_300K * waterMass;

		double tRise = 0;
		double efficiency = .8;
		if (nowT > 0)
			// The smaller the nowT, the bigger the tRise-+
			tRise = 1 + tPreset / nowT * efficiency;

		// Q = mcΔT
		// kJ = kJ/(kg°C) * kg * °C
		// kJ/s = kJ/(kg°C) * kg * °C / s

		// kJ * 3600 = kWh
		// 1 kW = 1 kJ/s

		// [kW] = [kJ/°C] / [seconds] * [°C]
		// [kW/°C] = [kJ/°C] / [seconds]

		double waterHeatSink = waterHeatCap / seconds * tRise;

//		error = checkError("waterHeatSink", waterHeatSink) || error;

		// 1 / [kJ/°C] * [seconds] = [seconds] / [kJ/°C]
		// s / C_s / mass = [seconds] / [kJ/°C]
		double convFactorWater = seconds / waterHeatCap;

//		Note: confirm if this is correct: double waterHeatSink = waterHeatCap / millisols / timeSlice ;

		// (5d3) Apply the water heat sink to buffer the net heat

		double dHeat2 = computeHeatSink(nowT, diffHeat, waterHeatSink, 1, seconds / MarsTime.SECONDS_PER_MILLISOL);

		error = checkError("dHeat2", dHeat2, 30) || error;

//		/**
//		 * Do NOT delete. For Debugging
//		 */
		if (error
//				|| (dHeat1 > 0 && dHeat1 < dHeat2)
//				|| (dHeat1 < 0 && dHeat1 > dHeat2)
		) {
			logger.warning(building, 20_000,
					"Water heat sink - " + "T: " + Math.round(currentTemperature * 100.0) / 100.0 + "  nowT: "
							+ Math.round(nowT * 100.0) / 100.0 + "  dHeat2: " + Math.round(dHeat2 * 1000.0) / 1000.0
							+ "  convFactorWater: " + Math.round(convFactorWater * 1000.0) / 1000.0 + "  waterMass: "
							+ Math.round(waterMass * 1000.0) / 1000.0 + "  waterHeatCap: "
							+ Math.round(waterHeatCap * 1000.0) / 1000.0 + "  waterHeatSink: "
							+ Math.round(waterHeatSink * 1000.0) / 1000.0);
		}

		return new double[] { dHeat2, convFactorWater };
	}

	/**
	 * Cycles through the thermal control system for temperature change.
	 * 
	 * @param millisols time in millisols
	 */
	private void cycleThermalControl(double millisols) {

		double oldT = getCurrentTemperature();

		// Reset the error flag
		error = checkError("oldT", oldT, MAX_INDOOR_TEMPERATURE);

		if (oldT > MAX_INDOOR_TEMPERATURE) {
			logger.warning(building, 20_000, "inT: " + Math.round(oldT * 10.0) / 10.0);
		}

		// STEP 1 : CALCULATE HEAT GAIN/LOSS AND RELATE IT TO THE TEMPERATURE CHANGE
		double output[] = determineHeatTemperature(millisols);

		double newT = output[0];

		double convFactor = output[1];

		error = checkError("newT", newT, MAX_INDOOR_TEMPERATURE) || error;

		if (newT > MAX_INDOOR_TEMPERATURE) {
			logger.warning(building, 20_000,
					"newT: " + Math.round(newT * 10.0) / 10.0 + " > " + MAX_INDOOR_TEMPERATURE);
			newT = MAX_INDOOR_TEMPERATURE;
			error = true;
		}

		else if (newT < MIN_INDOOR_TEMPERATURE) {
			logger.warning(building, 20_000,
					"newT: " + Math.round(newT * 10.0) / 10.0 + " < " + MIN_INDOOR_TEMPERATURE);
			newT = MIN_INDOOR_TEMPERATURE;
			error = true;
		}

//		double outT = building.getSettlement().getOutsideTemperature();
//		if (outT < 0 && newT < outT)
//			newT = outT;

		// Set the temperature and call unitUpdate
		setTemperature(newT);

		// STEP 2 : GET THE DELTA TEMPERATURE
		double dt = oldT - newT;

		error = checkError("dt", dt, MAX_INDOOR_TEMPERATURE) || error;

		// Set the delta temperature and call unitUpdate
		setDeltaTemp(dt);

		// STEP 3 : FIND TEMPERATURE DEVIATION

		// Insert the heat that needs to be gained in the next cycle
		double devT = tPreset - newT;

		error = checkError("devT", devT, MAX_INDOOR_TEMPERATURE) || error;

		// if devT is positive, needs to raise temperature by increasing heat
		// if devT is negative, needs to low temperature by transferring heat away

		// Set the deviation temperature and call unitUpdate
		setDevTemp(devT);

		// STEP 4 : FIND HEAT REQUIRED TO FLATTEN TEMPERATURE DEVIATION

		double bound = Math.max(-3, Math.min(3, millisols / convFactor));

		// Calculate the new heat kW required to raise the temperature back to the
		// preset level
		double reqkW = devT * bound;
		// Note that multiplying by deltaTime is not mathematically sound but results in
		// better reqkW

		double estReqHeat = 0;
		double selReqHeat = 0;
		
		if (devT > 0.2 || devT < -0.2)
			estReqHeat = estimateRequiredHeat(newT, millisols);

		if (estReqHeat > 0) {
			if (estReqHeat > reqkW) {
				selReqHeat = reqkW;
			} else {
				selReqHeat = estReqHeat;
			}
		} else if (estReqHeat < 0) {
			if (estReqHeat < reqkW) {
				selReqHeat = reqkW;
			} else {
				selReqHeat = estReqHeat;
			}
		}
		
		// 5b : USE HEAT SINK TO LOWER REQUIRED HEAT

		double extraHeat = 0;
		double newHeat = 0;
		double limit0 = -areaFactor * 3;
		double limit1 = -3;

		if (selReqHeat < limit0 || devT < limit1) {
			// A little too much heat. Temperature is higher than preset

			// Further convert a half quarter of the selectedReqHeat to avoid overheating
			extraHeat = Math.abs(selReqHeat / limit0) + Math.abs(devT / limit1);
			newHeat = extraHeat;
			// Dump a half quarter of selectedReqHeat. Reduce selectedReqHeat by a quarter
			selReqHeat = selReqHeat + extraHeat;

//			logger.info("selReqHeat: " + selReqHeat
//					+ "  areaFactor: " + areaFactor
//					+ "  devT: " + devT
//					+ "  extraHeat: " + extraHeat
//					+ "  newHeat: " + newHeat
//					);

			// Pick the first heat sink (air or water) randomly
			int rand = RandomUtil.getRandomInt(1);

			// Note: Use heat sink to trap this heat, rather than dumping it outside
			double oldSink0 = heatSinkLimit[rand];
			double transfer = 0;
			double newSink0 = oldSink0 + transfer;
			if (newSink0 > heatSinkLimit[rand]) {
				newSink0 = heatSinkLimit[rand];
				transfer = newSink0 - oldSink0;
			} else {
				transfer = .5 * extraHeat;
			}
			extraHeat = extraHeat - transfer;
			newHeat = transfer;
			heatSinkLimit[rand] = newSink0;

//			logger.info("oldSink0: " + oldSink0
//					+ "  newSink0: " + newSink0
//					+ "  extraHeat: " + extraHeat
//					+ "  newHeat: " + newHeat
//					+ "  transfer: " + transfer
//					);

			// Switch over to the other heat sink
			if (rand == 0)
				rand = 1;
			else
				rand = 0;

			double oldSink1 = heatSinkLimit[rand];
			double newSink1 = oldSink1 + extraHeat;

			if (newSink1 > heatSinkLimit[rand]) {
				newSink1 = heatSinkLimit[rand];
				transfer = newSink1 - oldSink1;
			} else {
				transfer = extraHeat;
			}
			newHeat += transfer;
			heatSinkLimit[rand] = newSink1;
//			logger.info("oldSink1: " + oldSink1
//					+ "  newSink1: " + newSink1
//					+ "  extraHeat: " + extraHeat
//					+ "  newHeat: " + newHeat
//					+ "  transfer: " + transfer
//					);

			if (newHeat > 3 || newT > 40 || newT <= 10) {
				error = true;
				logger.info(building, 20_000, "At T: " + Math.round(newT * 100.0) / 100.0 
					+ ", soak up " + Math.round(newHeat * 1000.0) / 1000.0
					+ " kW with Phase Change Material (PCM) to avoid overheating. areaFactor: " + Math.round(areaFactor * 10.0) / 10.0 
					+ ".");
			}
		}

		error = checkError("selReqHeat", selReqHeat, 30) || error;
		// Sets the heat required for this cycle
		setHeatRequired(selReqHeat);

		/**
		 * Do NOT delete. For future Debugging.
		 */
		if (error || selReqHeat > 30 || selReqHeat < -30)
			logger.warning(building, 20_000, "cycleThermalControl - " + "oldT: " + Math.round(oldT * 100.0) / 100.0
					+ "  newT: " + Math.round(newT * 100.0) / 100.0
					+ "  devT: " + Math.round(devT * 100.0) / 100.0

					+ "  extraHeat: " + Math.round(extraHeat * 1000.0) / 1000.0 
					+ "  newHeat: " + Math.round(newHeat * 1000.0) / 1000.0 
					+ "  selectedReqHeat: " + Math.round(selReqHeat * 1000.0) / 1000.0 
					+ "  estReqHeat: " + Math.round(estReqHeat * 1000.0) / 1000.0 
					+ "  reqkW: " + Math.round(reqkW * 1000.0) / 1000.0
					
					+ "  convFactor: " + Math.round(convFactor * 1000.0) / 1000.0 
					+ "  millisols: " + Math.round(millisols * 1000.0) / 1000.0 
					+ "  bound: " + Math.round(bound * 100.0) / 100.0
					+ "  preNetHeat: " + Math.round(preNetHeatCache * 1000.0) / 1000.0 
					+ "  postNetHeat: " + Math.round(postNetHeatCache * 100.0) / 100.0);
	}

	/**
	 * Checks if a value is invalid.
	 * 
	 * @param type
	 * @param value
	 * @param limit
	 * @return
	 */
	private boolean checkError(String type, double value, int limit) {
		if (Double.isInfinite(value)) {
			logger.severe(building, 20_000, type + " is infinite.");
			return true;
		} else if (Double.isNaN(value)) {
			logger.severe(building, 20_000, type + " is NaN.");
			return true;
		} else if (value == 0.0) {
			return false;
		} else if (limit > 0 && value > limit) {
			logger.warning(building, 20_000, type + ": " + Math.round(value * 100.0) / 100.0 + " > " + limit + ".");
			return true;
		} else if (limit < 0 && value < limit) {
			logger.warning(building, 20_000, type + ": " + Math.round(value * 100.0) / 100.0 + " < " + limit + ".");
			return true;
		}

		return false;
	}

	/**
	 * Computes a new amount of heat upon after interacting with air moisture or
	 * liquid water as heat sink.
	 * 
	 * @Note this serves to minimize oldHeat by storing some in heatsink[]. When
	 *       oldHeat is +ve, heat sink will take in some amount, thus making excessH
	 *       (still +ve) less than oldHeat in value. When oldHeat is -ve, heat sink
	 *       will release stored heat, thus making excessH (still -ve) less than
	 *       oldHeat in value.
	 * @param nowT       in deg celsius
	 * @param diffHeat
	 * @param limit      The latentPower in kW
	 * @param convFactor
	 * @param index      of the heat sink double array (0 = air moisture; 1 = liquid
	 *                   water)
	 * @param millisols
	 * @return the excessive amount of heat that cannot be absorbed
	 */
	private double computeHeatSink(double nowT, double diffHeat, double limit, int index, double millisols) {
		// THe purpose is to minimize diffHeat
		double heat = 0;

		double dt = (nowT - tPreset);
		if (dt > 20)
			dt = 20;
		else if (dt < -20)
			dt = -20;

		// For inflatable greenhouse, air heat sink ~ 3 kW
		// For large greenhouse, air heat sink ~ ? kW, water heat sink ~ 43 kW
		// For algae pond, air heat sink ~ 3 kW, water heat sink ~ 42 kW
		// For fish farm, air heat sink ~ 3 kW, water heat sink ~ 82 kW

		// How fast or efficient is the heat transfer ?
		// For air heat sink, assume 30%
		double efficiency = .3;
		if (index == 1) {
			// For water heat sink, assume 50%
			efficiency = .5;
		}

		// The fraction of the speed of a perfect conductor for the heat transfer
		double upperBound = Math.min(1, millisols * efficiency * areaFactor * 3);

		// Note: if transfer is +ve, it's the amount to be absorbed into the heat sink
		// if transfer is -ve, it's the amount to be released from the heat sink

		// Calculate the amount of heat that can be absorbed or released
		final double transfer = dt * upperBound;
		double newTransfer = transfer;

		double storedSink = heatSink[index];

		// Case 1 : too hot, the sink absorbs the heat
		if (dt > 0) {
			// Note: transfer is positive
			// Heat sink will suck up some heat

			// Case 1a : can't absorb more heat this time
			if (storedSink >= limit)
				return 0;

			// Case 1b : If the stored heat plus the newTransfer exceed the new stored limit
			if (storedSink + newTransfer > limit) {
				// Reduce transfer amount
				newTransfer = limit - storedSink;
				// Store the heak
				storedSink += newTransfer;
				// Reduce newHeat
				heat -= newTransfer;
			} else {
				// Case 1c
				// Soak up the transfer amount
				storedSink += newTransfer;
				// Reduce newHeat
				heat -= newTransfer;
			}

			if (heatSinkLimit == null) {
				heatSinkLimit = new double[2];
			}

			if (index == 0) {
				setAirHeatSink(storedSink);
				heatSinkLimit[0] = limit;
			} else {
				setWaterHeatSink(storedSink);
				heatSinkLimit[1] = limit;
			}
		}

		// Case 2 : too cold, the sink releases the heat
		else if (dt < 0) {
			// Note: transfer is negative
			// Need to release heat from heat sink

			// Case 2a : can't absorb more heat this time
			if (storedSink <= 0)
				return 0;

			if (storedSink + newTransfer < 0) {
				// Case 2b
				// Calculate new transfer amount
				newTransfer = -storedSink;
				// Take away all the heat in heatSink
				storedSink = 0;
				// newHeat will become less negative
				heat -= newTransfer;
			} else {
				// Case 2c
				// Take away what is needed in heatSink
				storedSink += newTransfer;
				// newHeat will become less negative
				heat -= newTransfer;
			}

			if (index == 0) {
				setAirHeatSink(storedSink);
			} else {
				setWaterHeatSink(storedSink);
			}
		}

		/**
		 * Do NOT delete. For debugging.
		 */
		if (error)
			logger.warning(building, 3_000, "index:" + index + "  nowT: " + Math.round(nowT * 10.0) / 10.0 + "  dh: "
					+ Math.round(dt * 1000.0) / 1000.0 + "  heat: " + Math.round(heat * 1000.0) / 1000.0
					+ "  millisols: " + Math.round(millisols * 10.0) / 10.0 + "  upperBound: "
					+ Math.round(upperBound * 10.0) / 10.0 + "  heatSink[" + Math.round(heatSink[0] * 1000.0) / 1000.0
					+ ", " + Math.round(heatSink[1] * 1000.0) / 1000.0 + "]" + "  limit: "
					+ Math.round(limit * 10000.0) / 10000.0 + "  transfer: " + Math.round(transfer * 1000.0) / 1000.0
					+ "  newTransfer: " + Math.round(newTransfer * 1000.0) / 1000.0 + "  efficiency: "
					+ Math.round(efficiency * 100.0) / 100.0);

		return heat;
	}

	/**
	 * Computes the heat in kW to be moved by active ventilation.
	 * 
	 * @param heat
	 * @param degNow    the current temperature of the building in Deg Celsius
	 * @param millisols
	 * @return energy to be moved; if positive, hotter air is coming in; if
	 *         negative, hotter air is leaving
	 */
	private double calculateActiveVentHeat(double heat, double degNow, double millisols) {
		final double STEP = 1.25;

		// Reference : time = .121 at x128
		// e.g. Lander Hab: At 26.8 deg, dt: 0.48 speedFactor: 0.234 areaFactor: 3.0

		// if reqheat is -ve, heat is +ve
		// if reqheat is +ve, heat is -ve
		double dt0 = Math.abs(degNow - tPreset) / 3;
		if (dt0 > 6)
			dt0 = 6;

		// Even if there's no net heat, the temperature variance would still trigger the
		// vent flow
		double modHeat = Math.abs(heat) / 3;
		if (modHeat > 5)
			modHeat = 5;

		double totalHeat = 0;
		// Note: this temperature range is arbitrary
		boolean tooLow = degNow < (tPreset - STEP * T_LOWER_SENSITIVITY);
		boolean tooHigh = degNow > (tPreset + STEP * T_UPPER_SENSITIVITY);

		double lowerBound = Math.max(0, millisols);
		double upperBound = Math.min(1, lowerBound);

		double speedFactor = .06 * upperBound * CFM * areaFactor / 2;

		if (!tooLow && !tooHigh) {
			return 0;
		}

		adjacentBuildings = new ArrayList<>(building.getSettlement().getAdjacentBuildings(building));

		int size = adjacentBuildings.size();

		for (int i = 0; i < size; i++) {
			double tNow = adjacentBuildings.get(i).getCurrentTemperature();
			double tInit = adjacentBuildings.get(i).getPresetTemperature();

			boolean tooLowAdj = tNow < (tInit - STEP * T_LOWER_SENSITIVITY);
			boolean tooHighAdj = tNow > (tInit + STEP * T_UPPER_SENSITIVITY);

			double dt1 = Math.abs(degNow - tNow) / 4;
			if (dt1 > 5)
				dt1 = 5;

			// The larger the area of a room, the more spread out the heat, the harder for
			// the heat to be vented out
			double maxHeat = modHeat * dt0 * dt1 * speedFactor;
			if (maxHeat > 5)
				maxHeat = 5;
			else if (maxHeat < -5)
				maxHeat = -5;

			double deltaHeat = 0;

			if (tooLow && (tNow - degNow) > 2) {
				// Heat is coming in
				// Need to suck hotter air from adjacent buildings
				if (tooHighAdj) {

					if (tNow > degNow) {
						// If this adjacent building has a higher T than this
						// building of interest, then heat is venting in
						deltaHeat = maxHeat * .9;
					} else if (tNow < degNow) {
						// heat is coming in in lesser magnitude
						deltaHeat = maxHeat * .4;
					}
				} else if (!tooLowAdj) {
					if (tNow > degNow) {
						// heat coming in
						deltaHeat = maxHeat * .7;
					} else if (tNow < degNow) {
						// heat coming in
						deltaHeat = maxHeat * .3;
					}
				}
			}

			else if (tooHigh && (degNow - tNow) > 2) {
				// Heat is leaving
				// Need to dump hotter air to adjacent buildings
				if (tooLowAdj) {
					if (degNow > tNow) {
						// heat is leaving
						deltaHeat = -maxHeat * .9;
					} else if (degNow < tNow) {
						// heat is leaving
						deltaHeat = -maxHeat * .4;
					}
				} else if (!tooHighAdj) {
					if (degNow > tNow) {
						// heat is leaving
						deltaHeat = -maxHeat * .7;
					} else if (degNow < tNow) {
						// heat is leaving
						deltaHeat = -maxHeat * .3;
					}
				}
			}

			// +ve deltaHeat means this building is gaining heat
			if (deltaHeat > 0) {

				// -ve deltaHeat means the adjacent building is losing heat
				adjacentBuildings.get(i).addVentInHeat(-deltaHeat);
				/**
				 * Do NOT delete. For future Debugging.
				 */
//				logger.info(building, 20_000, "At " + Math.round(degNow * 10.0)/10.0 
//							+ " deg, venting in "
//							+ Math.round(deltaHeat * 1000.0)/1000.0 + " kW (max: "
//							+ Math.round(maxHeat * 1000.0)/1000.0	+ ") from "
//							+ adjacentBuildings.get(i).getName() 
//							+ " at " + Math.round(tNow * 10.0)/10.0 + " deg.");
			}
			// -ve deltaHeat means this building is losing heat
			else if (deltaHeat < 0) {

				// +ve deltaHeat means the adjacent building is gaining heat
				adjacentBuildings.get(i).addVentInHeat(-deltaHeat);
				/**
				 * Do NOT delete. For future Debugging.
				 */
//				logger.info(building, 20_000, "At " + Math.round(degNow * 10.0)/10.0 
//							+ " deg, venting out "
//							+ Math.round(-deltaHeat * 1000.0)/1000.0 + " kW (max: "
//							+ Math.round(maxHeat * 1000.0)/1000.0	+ ") to "
//							+ adjacentBuildings.get(i).getName() 
//							+ " at " + Math.round(tNow * 10.0)/10.0 + " deg.");
			}

			totalHeat += deltaHeat;
			/**
			 * Do NOT delete. For future Debugging.
			 */
//			if (totalHeat > 0 || totalHeat < 0) {
//				logger.info(building, "1. At " + Math.round(degNow * 10.0)/10.0 
//						+ " deg, modHeat: "+ Math.round(modHeat * 1000.0)/1000.0
//						+ "  heat: "+ Math.round(heat * 1000.0)/1000.0
//						+ "  deltaHeat: "+ Math.round(deltaHeat * 1000.0)/1000.0
//						+ "  totalHeat: " + Math.round(totalHeat * 1000.0)/1000.0
//						+ "  MAX_HEAT: " + Math.round(MAX_HEAT * 1000.0)/1000.0	
//						+ "  dt1: " + Math.round(dt1 * 100.0)/100.0
//						+ "  convFactor: " + Math.round(convFactor * 100.0)/100.0 
//						+ "  speedFactor: "+ Math.round(speedFactor * 1000.0)/1000.0
//						+ "  areaFactor: " + Math.round(areaFactor * 1000.0)/1000.0);
//			}
		}
		/**
		 * Do NOT delete. For future Debugging.
		 */
//		if (totalHeat > 20 || totalHeat < -20) {
//			// speedFactor: 0.022  areaFactor: 2.711
//			logger.info(building, "2. At " + Math.round(deg * 10.0)/10.0 
//					+ " deg, totalHeat: " + Math.round(totalHeat * 1000.0)/1000.0
//					+ "  speedFactor: "+ Math.round(speedFactor * 1000.0)/1000.0
//					+ "  areaFactor: " + Math.round(areaFactor * 1000.0)/1000.0);
//		}

		return totalHeat;
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {
		return 0D;
	}

	/**
	 * Time passing for the building.
	 * 
	 * @param deltaTime amount of time passing (in millisols)
	 */
	public void timePassing(double time) {
		if (time > 0)
			cycleThermalControl(time);
	}

	/**
	 * Gets the delta temperature for the heat transfer.
	 * 
	 * @return deg C.
	 */
	public double getDeltaTemp() {
		return deltaTCache;
	}

	/**
	 * Gets the deviation temperature (between the indoor temperature and the
	 * preset).
	 * 
	 * @return deg C.
	 */
	public double getDevTemp() {
		return devTCache;
	}

	/**
	 * Gets the heat this building currently required.
	 * 
	 * @return heat in kW.
	 */
	public double getHeatRequired() {
		return reqHeatCache;
	}

	/**
	 * Gets the initial net heat gain of this building.
	 * 
	 * @return heat in kW.
	 */
	public double getPreNetHeat() {
		return preNetHeatCache;
	}

	/**
	 * Gets the heat gain of this building.
	 * 
	 * @return heat in kW.
	 */
	public double getHeatGain() {
		return heatGainCache;
	}

	/**
	 * Gets the heat loss of this building.
	 * 
	 * @return heat in kW.
	 */
	public double getHeatLoss() {
		return heatLossCache;
	}

	/**
	 * Gets the post net heat gain of this building.
	 * 
	 * @return heat in kW.
	 */
	public double getPostNetHeat() {
		return postNetHeatCache;
	}

	/**
	 * Gets the heat to be generated for this building.
	 * 
	 * @return heat in kW.
	 */
	public double getHeatGenerated() {
		return heatGenCache;
	}

	/**
	 * Gets the excess heat pumped due to server room's computational equipment.
	 * 
	 * @return heat in kW.
	 */
	public double getExcessHeat() {
		return excessHeatCache;
	}

	/**
	 * Gets the water heat sink stored in this building.
	 * 
	 * @return heat in kW.
	 */
	public double getWaterHeatSink() {
		return heatSink[1];
	}

	/**
	 * Gets the water heat sink limit for this building.
	 * 
	 * @return heat in kW.
	 */
	public double getWaterHeatSinkLimit() {
		return heatSinkLimit[1];
	}

	/**
	 * Gets the air heat sink stored in this building.
	 * 
	 * @return heat in kW.
	 */
	public double getAirHeatSink() {
		return heatSink[0];
	}

	/**
	 * Gets the air heat sink limit for this building.
	 * 
	 * @return heat in kW.
	 */
	public double getAirHeatSinkLimit() {
		return heatSinkLimit[0];
	}

	/**
	 * Sets the air heat sink of this building and call unitUpdate.
	 * 
	 * @param heat in kW.
	 */
	public void setAirHeatSink(double heat) {
		heatSink[0] = heat;
		building.fireUnitUpdate(UnitEventType.AIR_HEAT_SINK_EVENT);
	}

	/**
	 * Sets the water heat sink of this building and call unitUpdate.
	 * 
	 * @param heat in kW.
	 */
	public void setWaterHeatSink(double heat) {
		heatSink[1] = heat;
		building.fireUnitUpdate(UnitEventType.WATER_HEAT_SINK_EVENT);
	}

	/**
	 * Sets the heat this building currently required and call unitUpdate.
	 * 
	 * @param heat in kW.
	 */
	public void setHeatRequired(double heat) {
		reqHeatCache = heat;
		building.fireUnitUpdate(UnitEventType.REQUIRED_HEAT_EVENT);
	}

	/**
	 * Sets the delta Temperature of this building and call unitUpdate. if positive,
	 * it needs to gain some heat if negative, it needs to lose some heat
	 * 
	 * @param heat in kW.
	 */
	public void setDeltaTemp(double heat) {
		deltaTCache = heat;
		building.fireUnitUpdate(UnitEventType.DELTA_T_EVENT);
	}

	/**
	 * Sets the deviation temperature of this building and call unitUpdate. if
	 * positive, it needs to gain some heat if negative, it needs to lose some heat
	 * 
	 * @param heat in kW.
	 */
	public void setDevTemp(double heat) {
		devTCache = heat;
		building.fireUnitUpdate(UnitEventType.DEV_T_EVENT);
	}

	/**
	 * Sets the initial net heat gain/loss of this building and call unitUpdate.
	 * 
	 * @param heat in kW.
	 */
	public void setPreNetHeat(double heat) {
		preNetHeatCache = heat;
		building.fireUnitUpdate(UnitEventType.NET_HEAT_0_EVENT);
	}

	/**
	 * Sets the heat gain of this building and call unitUpdate.
	 * 
	 * @param heat in kW.
	 */
	public void setHeatGain(double heat) {
		heatGainCache = heat;
		building.fireUnitUpdate(UnitEventType.HEAT_GAIN_EVENT);
	}

	/**
	 * Sets the heat loss of this building and call unitUpdate.
	 * 
	 * @param heat in kW.
	 */
	public void setHeatLost(double heat) {
		heatLossCache = heat;
		building.fireUnitUpdate(UnitEventType.HEAT_LOSS_EVENT);
	}

	/**
	 * Sets the post net heat gain/loss of this building and call unitUpdate.
	 * 
	 * @param heat in kW.
	 */
	public void setPostNetHeat(double heat) {
		postNetHeatCache = heat;
		building.fireUnitUpdate(UnitEventType.NET_HEAT_1_EVENT);
	}

	/**
	 * Sets the heat this building currently required and call unitUpdate.
	 * 
	 * @return heat in kW.
	 */
	public void setTemperature(double temp) {
		currentTemperature = temp;
		building.fireUnitUpdate(UnitEventType.TEMPERATURE_EVENT);
	}

	/**
	 * Dumps the heat being generated to meet the heat gain/loss and call
	 * unitUpdate.
	 *
	 * @param heat
	 */
	public void insertHeatGenerated(double heat) {
		heatGenCache = heat;
		building.fireUnitUpdate(UnitEventType.GENERATED_HEAT_EVENT);
	}

	/**
	 * Dumps the excess heat generated from server farm's equipment and call
	 * unitUpdate.
	 */
	public void insertExcessHeatComputation(double heat) {
		excessHeatCache = heat;
		building.fireUnitUpdate(UnitEventType.EXCESS_HEAT_EVENT);
	}

	/**
	 * Gets the heat the building requires for power-down mode.
	 * 
	 * @return heat in kW.
	 */
	public double getPoweredDownHeatRequired() {
		return basePowerDownHeatRequirement;

	}

	/**
	 * Gets the vent heat actively managed by this building via ventilation.
	 * 
	 * @return heat in kW.
	 */
	public double getActiveVentHeat() {
		return activeVentHeatCache;
	}

	/**
	 * Gets the vent heat passively initiated by other building toward this
	 * building.
	 * 
	 * @return heat in kW.
	 */
	public double getPassiveVentHeat() {
		return passiveVentHeatCache;
	}

	/**
	 * Sets the vent heat actively managed by this building toward adjacent
	 * buildings via ventilation. Note: heat gain if positive; heat loss if
	 * negative.
	 * 
	 * @param heat removed or added
	 */
	public void setActiveVentHeat(double heat) {
		activeVentHeatCache = heat;
		building.fireUnitUpdate(UnitEventType.ACTIVE_VENT_EVENT);
	}

	/**
	 * Adds the passive vent heat initiated by adjacent buildings toward this
	 * building via ventilation. Note: heat gain if positive; heat loss if negative.
	 * 
	 * @param heat removed or added
	 */
	public void addVentInHeat(double heat) {
		passiveVentHeatCache += heat;
		building.fireUnitUpdate(UnitEventType.PASSIVE_VENT_EVENT);
	}

	/**
	 * Sets the passive vent heat initiated by adjacent buildings toward this
	 * building via ventilation. Note: heat gain if positive; heat loss if negative.
	 * 
	 * @param heat removed or added
	 */
	public void setPassiveVentHeat(double heat) {
		passiveVentHeatCache = heat;
		building.fireUnitUpdate(UnitEventType.PASSIVE_VENT_EVENT);
	}

	/**
	 * Flags the presence of the heat loss due to opening an airlock outer door.
	 * 
	 * @param value
	 */
	public void flagHeatLostViaAirlockOuterDoor(boolean value) {
		hasHeatDumpViaAirlockOuterDoor = value;
	}

	/**
	 * Reloads instances after loading from a saved sim.
	 *
	 * @param bc {@link BuildingConfig}
	 * @param c0 {@link MasterClock}
	 * @param pc {@link PersonConfig}
	 */
	public static void initializeInstances(SurfaceFeatures sf, Weather w) {
		weather = w;
		surface = sf;
	}

	public void destroy() {
		building = null;
		location = null;
		adjacentBuildings = null;
		heatSink = null;
	}

}
