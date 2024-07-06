/*
 * Mars Simulation Project
 * Heating.java
 * @date 2024-07-03
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.utility.heating;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.air.AirComposition;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.environment.Weather;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingConfig;
import com.mars_sim.core.structure.building.FunctionSpec;
import com.mars_sim.core.structure.building.function.BuildingAirlock;
import com.mars_sim.core.structure.building.function.farming.Crop;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.mapdata.location.Coordinates;

/**
 * The Heating class is a building function for regulating temperature in a settlement.
 */
public class Heating implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Heating.class.getName());
	/** The fine-tuneing param for a refresh cycle. */
	private static final int PER_UPDATE = 1;
	/** The assumed maximum indoor temperature. */
	private static final int MAX_INDOOR_TEMPERATURE = 45;
	/** The maximum error value. */
	private static final int MAX_ERROR_VALUE = 100;
	
	// Data members
	// KG_TO_LB = 2.204623;
	private static final double DEFAULT_ROOM_TEMPERATURE = 22.5;
	// kW_TO_kBTU_PER_HOUR = 3.41214; // 1 kW = 3412.14 BTU/hr
	private static final double C_TO_K = 273.15;
	private static final double TRANSMITTANCE_GREENHOUSE_HIGH_PRESSURE = .55;
	private static final double TRANSMITTANCE_WINDOW = 0.75;
	private static final double EMISSIVITY_DAY = 0.8 ;
	private static final double EMISSIVITY_NIGHT = 1.0 ;
	// EMISSIVITY_INSULATED = 0.05 ;
	private static final double STEFAN_BOLTZMANN_CONSTANT = 0.0000000567 ; // in W / (m^2 K^4)

	private static final double LARGE_INSULATION_CANOPY = .7; // [in kW]
	private static final double INSULATION_BLANKET = .3; // [in kW]
	private static final double HALLWAY_INSULATION = .5; // [in kW]
	
    // Thermostat's temperature allowance
    private static final double T_UPPER_SENSITIVITY = 1D;
    private static final double T_LOWER_SENSITIVITY = 1D;

    private static final double HEAT_DISSIPATED_PER_PERSON = .1; //[in kW]
    
    // MSOL_LIMIT = 1.5;
    // kPASCAL_PER_ATM = 1D/0.00986923267 ; // 1 kilopascal = 0.00986923267 atm
    // R_GAS_CONSTANT = 8.31441; //R = 8.31441 m3 Pa K−1 mol−1
	// 1 kilopascal = 0.00986923267 atm
	// 1 cubic ft = L * 0.035315
    // A full scale pressurized Mars rover prototype may have an airlock volume of 5.7 m^3
	
	private static final double HEIGHT = 2.5; // in meter
	
	/** The speed of the ventilation fan */
	private static final double CFM  = 50;
	
    /**  convert meters to feet  */
	// M_TO_FT = 3.2808399;//10.764;
	/**  Specific Heat Capacity = 4.0 for a typical U.S. house */
	//	SHC = 6.0; // [in BTU / sq ft / °F]
	/** Building Loss Coefficient (BLC) is 1.0 for a typical U.S. house  */
	//	BLC = 0.2;

	/** 
	 * Cooling Load Factor accounts for the fact that building thermal mass creates a time lag between 
	 * heat generation from internal sources and the corresponding cooling load. 
	 * It is equals to Sensible Cooling Load divided by Sensible Heat Gain
	 */
	private static final double CLF = 1.8D;
	
	/** 
	 * The U-value in [Watts/m^2/°K] is the thermal transmittance (reciprocal of R-value)
	 * Note that Heat Loss = (1/R-value)(surface area)(∆T) 
	 */
	private static final double uValue = 0.1;
 
    // Note : U_value will be converted to metric units in W/m²K. 
    // see at https://www.thenbs.com/knowledge/what-is-a-u-value-heat-loss-thermal-mass-and-online-calculators-explained
    
    private static final double Q_HF_FACTOR = 21.4D/10;///2.23694; // 1 m per sec = 2.23694 miles per hours
    
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
	private static final double ENERGY_FACTOR_EVA = SPECIFIC_HEAT_CAP_AIR_300K * BuildingAirlock.AIRLOCK_VOLUME_IN_CM * DRY_AIR_DENSITY /1000; 
	
	/**  R-value is a measure of thermal resistance, or ability of heat to transfer from hot to cold, through materials such as insulation */
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
	/** Is this building a storage bin? */
	private boolean isStorage = false;
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
	/** The heat required for heating. */
	private double heatRequired;
	/** The initial net heat gain/loss due to thermal system. */
	private double preNetHeat;
	/** The post net heat gain/loss due to thermal system. */
	private double postNetHeat;
	/** The base power down heat requirement from buildings.xml. */
	private double basePowerDownHeatRequirement = 0;
	/** The U value of the ceiling or floor. */
    private double uValueAreaCeilingFloor; 	// Thermal Transmittance 
	/** The U value of the wall. */
    private double uValueAreaWall;
	/** The heat generated by the thermal system. */
	private double heatGeneratedCache = 0; // the initial value is zero
	/** The excess heat pumped in from server room's equipment. */
	private double excessHeatComputation = 0;
	/** The incoming heat arriving at this building by the ventilation system. */
	private double ventInHeat;
	/**  The outgoing heat leaving this building by the ventilation system. */
	private double ventOutHeat;
	/** The current temperature of this building. */
	private double currentTemperature;
	/** The preset temperature of this building. */		
	private double tPreset;
	/** The air heat sink and the water heat sink. */
	private double[] heatSink = new double[2];
	/** The square root of the area of this building. */
	private double areaFactor;
	/** The slice of time [in seconds] per millisol. */
	private double timeSlice = MarsTime.SECONDS_PER_MILLISOL / PER_UPDATE;
	/** The delta temperature due to the heat transfer. */
	private double deltaTCache; 
	/** The deviation temperature (between the current temperature and the preset temperature). */
	private double devTCache; 

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
		
//		checkError("length", length);
		
		width = building.getWidth();
		
//		checkError("width", width);
		
		floorArea = length * width;
		
//		checkError("floorArea", floorArea);
		
		areaFactor = Math.sqrt(Math.sqrt(floorArea));

		switch(building.getCategory()) {
			case CONNECTION:
				isConnector = true;
				heatGainEquipment = 0.0879;
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
			case STORAGE:
				isStorage = true;
				heatGainEquipment = 0.0879;
				break;
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
		}
		else {
			// Include the 4 side walls and ceiling
			hullArea = (width + length) * HEIGHT * 2 + floorArea; 
			
			transmittance = TRANSMITTANCE_WINDOW;
		}
	
		uValueAreaWall = uValue * 2D * (width + length) * HEIGHT;
		
		uValueAreaCeilingFloor = uValue * floorArea;
		// assuming airChangePerHr = .5 and q_H = 21.4;
		uValueAreaCrackLength = 0.244 * .075 * AIR_CHANGE_PER_HR * Q_HF_FACTOR * (4 * (.5 + .5) );
		// assuming four windows
		uValueAreaCrackLengthAirlock = 0.244 * .075 * AIR_CHANGE_PER_HR * Q_HF_FACTOR * (2 * (2 + 6) + 4 * (.5 + .5) );
		//assuming two EVA airlock
		
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
	 * Determines the change in indoor temperature.
	 * 
	 * @param inTCelsius current indoor temperature in degree celsius
	 * @param millisols time in millisols
	 * @return delta temperature in degree celsius
	 */
	private double[] determineDeltaTemperature(double inTCelsius, double millisols) {
		// NOTE: THIS IS A 7-PART CALCULATION
		
		// (1) CALCULATE THE TEMPERATURES DIFFERENCE
		
		// The outside temperature in deg celsius
		double outTCelsius = building.getSettlement().getOutsideTemperature();
		
		error = checkError("outTCelsius", 
				+ Math.round(outTCelsius * 10.0)/10.0) || error;
		
//		if (error) {
//			logger.warning(building, "outTCelsius: " + Math.round(outTCelsius * 10.0)/10.0
//					+ "  inTCelsius: " + Math.round(inTCelsius * 10.0)/10.0);
//		}
			
		// Find the temperature difference between outside and inside 
		double deltaTinTout =  inTCelsius - outTCelsius; //1.8 =  9D / 5D;
		
		// °C  x  9/5 + 32 = °F
		// (°F - 32)  x  5/9 = °C
		
		// The indoor temperature in kelvin
		double inTKelvin = inTCelsius + C_TO_K;
		// The outside temperature in kelvin
		double outTKelvin = outTCelsius + C_TO_K;
		
		int numEVAgoers = building.numOfPeopleInAirLock(); // if num > 0, this building has an airlock
		
		// Convert from W to kW
		double irradiance = surface.getSolarIrradiance(location) / 1000.0 ; 
		// if sunlight = 25 W/m2, I = 25/1000 = 0.025 kW/m2
 
		error = checkError("I", irradiance) || error;
		
		// (2) CALCULATE HEAT GAIN 
		
		// Note: a new convention is used in that heat gain is positive and heat loss is negative
		
		// Reference : 
		// 1. Engineering concepts for Inflatable Mars Surface Greenhouses
		//    at http://ntrs.nasa.gov/search.jsp?R=20050193847
		//    Full ver at http://www.marshome.org/files2/Hublitz2.pdf
		//    Revised ver at https://www.researchgate.net/publication/7890528_Engineering_concepts_for_inflatable_Mars_surface_greenhouses
		
		// (2a) CALCULATE HEAT GENERATED BY DIRECTING HEATING FROM THE LAST INTERVAL OF TIME
		
		double[] gainValue = calculateHeatGain(numEVAgoers, irradiance);
		double gain = gainValue[0];
		double canopyHeatGain = gainValue[1];		
		
		// (3) CALCULATE HEAT LOSS
		
		double loss = calculateHeatLoss(canopyHeatGain, outTCelsius, deltaTinTout, 
	    		inTKelvin, outTKelvin, irradiance,
	    		numEVAgoers);
		
		// (4) CALCULATE THE NET HEAT

		// (4a) FIND THE DIFFERENCE between heat gain, heat loss 
		double diffHeatGainLoss = gain + loss;
		
		// Set the initial net heat transfer
		setPreNetHeat(diffHeatGainLoss);
		
		error = checkError("diffHeatGainLoss", diffHeatGainLoss) || error;
		
		if (diffHeatGainLoss > 20) {
			logger.warning(building, "diffHeatGainLoss: " + diffHeatGainLoss + " > 20.");
			error = true;
		}
		else if (diffHeatGainLoss < -20) {
			logger.warning(building, "diffHeatGainLoss: " + diffHeatGainLoss + " < -20.");
			error = true;
		}
		
		double seconds = timeSlice * millisols;
		
//		+ "  millisols: " + Math.round(millisols * 1000.0)/1000.0
//		+ "  seconds: " + Math.round(seconds * 1000.0)/1000.0
		
		// (5) APPLY THE HEAT SINKS - AIR MOISTURE AND WATER
		
		// (5a) Find the air moisture heat sink
		
		double[] value = computeAirHeatSink(diffHeatGainLoss, seconds);
		double dHeat1 = value[0];
		double convFactor1 = value[1];
		
		double convFactor = convFactor1;
		
		double dt = 0;

		// (5c) Find the body mass of water to act as water heat sink
			
		// e.g. Fish Farm and Algae Pond, etc.
		double waterMass = 0;
		
		if (building.getFishery() != null) {
			waterMass = building.getFishery().getTankSize();
		}
		else if (building.getAlgae() != null) {
			waterMass = building.getAlgae().getWaterMass();
		}
		
		// Future: account for the size of water tank in each building
		
		// Assume water in restroom, holding tank or pipes contribute to the 
		// mass of water as heat sink
		if (isConnector) { // 2m*length
			waterMass = floorArea * 2;
		}
		else if (isGreenhouse) { // 6m*9m, 5m*10m, 12m*18m 
			waterMass += floorArea / 3;
		}
		else if (isCommand) { // 7m*9m, 10m*10m
			waterMass = floorArea / 2.5 ;
		}
		else if (isEVA) { // 6m*4m
			waterMass = floorArea;
		}
		
		// Note that Lander Hab should have multiple functions but is not registering other yet.
		
		if (isLodging) { // 7m*9m, 10m*10m
			waterMass += floorArea / 3;
		}

		if (isMedical) { // 7m*9m, 10m*10m
			waterMass += floorArea / 3;
		}
		
		// Future: Compute the exact amount of the water mass for server farm
		// based on # of Computing Unit (CU).
		
		if (isLab) { // 7m*9m, 10m*10m
			waterMass += floorArea / 7;
		}		
//		else if (!isStorage) {
//			waterMass = floorArea * 5 / 4.5;			
//		}
		
		error = checkError("waterMass", waterMass) || error;
		
		// Future: pack water bladder into the inner wall of each building 
		
		double netHeat = dHeat1;
		
		// (5d) Does it have any body of water 
		
		// (5d1) No. Save the net heat after applying the air heat sink only to buffer the net heat
		
		if (waterMass > 0) {
			// (5d2) Yes. Calculate the water heat sink
			
			double[] value2 = computeWaterHeatSink(dHeat1, waterMass, seconds);
			double dHeat2 = value2[0];
			double convFactor2 = value2[1];
			convFactor = convFactor2;
			
			netHeat = dHeat2;
		}
		
		// e.g. EVA Airlock 
		// airMass: 24.956 
		// heatCapAirMoisture: 1.109
		// airHeatCap: 27.673
		// airHeatSink: 0.712 
		
		// (6) CALCULATE VENT OUT HEAT THROUGH VENTILATION
		
		double ventOutHeat = -calculateVentOutHeat(inTCelsius, millisols); 
		// Set the outgoing heat leaving this building
		setVentOutHeat(ventOutHeat);
		
		// The energy to be vent out.
		// if positive, dump hotter air to adjacent buildings, or colder air is coming in
		// if negative, suck hotter air from adjacent buildings, or hotter air is coming in
		
//		logger.info(building, "ventOutHeat: " + Math.round(ventOutHeat * 1000.0)/1000.0);
		
		if (ventOutHeat > 0) {
			// if the energy to be vent out is positive, dump hotter air to adjacent buildings
			netHeat = netHeat - ventOutHeat;  
		}
		
		else if (ventOutHeat < 0) {
			// if the energy to be vent out is negative, suck hotter air from adjacent buildings
			netHeat = netHeat - ventOutHeat; 
		}
	
		error = checkError("ventOutHeat", ventOutHeat) || error;
		
		// Set the post net heat transfer
		setPostNetHeat(netHeat);
		
		// (7)  CALCULATE THE INSTANTANEOUS CHANGE OF TEMPERATURE (DELTA T) (in degrees celsius)  

		dt = computeNewDeltaT(netHeat, convFactor);
		
		return new double[] {dt, convFactor};
	}

    /**
     * Calculates the heat gain.
     * 
     * @param numEVAgoers
     * @param error
     * @return
     */
    private double[] calculateHeatGain(int numEVAgoers, double irradiance) {
		// Add the heat generated in response to the rise or drop in temperature from last refresh cycle
		double heatGenLastCycle = getHeatGenerated();
		
		error = checkError("heatGenLastCycle", heatGenLastCycle) || error;
		
		// Add the excess heat from computation
		double excessComputation = getExcessHeatComputation();
		error = checkError("excessComputation", excessComputation) || error;
		
		double heatPumpedIn = heatGenLastCycle + excessComputation;

		// Manually reset the heat generated from last frame in back to zero
//		insertHeatGenerated(0);
		// Manually reset the heat pumped from last frame in back to zero
		insertExcessHeatComputation(0);
		
		// (2b) CALCULATE HEAT GAIN BY PEOPLE
		double heatGainOccupants = HEAT_DISSIPATED_PER_PERSON * building.getNumPeople();
		// the energy required to heat up the in-rush of the new martian air

		// (2c) CALCULATE HEAT GAIN BY EVA HEATER
	
		double heatGainFromEVAHeater = 0;
		if (numEVAgoers > 0) 
			heatGainFromEVAHeater = building.getTotalPowerForEVA()/2D; 
	
		error = checkError("heatGainFromEVAHeater", heatGainFromEVAHeater) || error;
		
		// divide by 2 since half of the time a person is doing ingress 
		// Note : Assuming EVA heater requires .5kW of power for heating up the air for each person in an airlock during EVA ingress.

		// (2d) CALCULATE SOLAR HEAT GAIN

		// Solar heat gain is the amount of solar radiation that enters a 
		// building through windows and other openings. 
		// The transmittance of a window is a measure of how much solar radiation 
		// it allows to pass through
		
		double solarHeatGain = 0;
		
		if (irradiance > 0) {
			if (isGreenhouse) {
				solarHeatGain =  irradiance * transmittance * hullArea * .12;
			}
			
			else if (isConnector) {
				solarHeatGain =  irradiance * transmittance * hullArea * .045;
			}
			
			else {
				solarHeatGain =  irradiance * transmittance * hullArea * .04;
			}
		}
		
//		logger.info(building, "I: " + Math.round(I * 100.0)/100.0
//				+ "  solarHeatGain: " + Math.round(solarHeatGain * 100.0)/100.0);
		
		error = checkError("solarHeatGain", solarHeatGain) || error;
		
		// (2e) CALCULATE INSULATION HEAT GAIN
		double canopyHeatGain = 0;
		double coeff = 0;
		
		// Note: Whenever the sun is about to go down, unfold the outer canopy over the 
		// structure to prevent heat loss
	
		if (irradiance < 0.05) {
			
			// If temperature inside is too low, will automatically close 
			// the window, blind or curtain partially to block the heat from radiating 
			// away to stop cool off the building.
			
			switch(building.getConstruction()) {
				case INFLATABLE:
					coeff = LARGE_INSULATION_CANOPY;
					break;
			
				case SEMI_SOLID:
					coeff = HALLWAY_INSULATION;
					break;
			
				default:
					coeff = INSULATION_BLANKET;
			}
				
			canopyHeatGain = 0.75 * coeff * (1 - irradiance);
		}
	
		error = checkError("canopyHeatGain", canopyHeatGain) || error;
		
		// (2f) CALCULATE HEAT GAIN DUE TO ARTIFICIAL LIGHTING
		double lightingGain = 0;
		
		if (isGreenhouse && building.getFarming() != null) {
			// greenhouse has a semi-transparent rooftop
			lightingGain = building.getFarming().getTotalLightingPower() * LAMP_GAIN_FACTOR;
	        // For high pressure sodium lamp, assuming 60% are invisible radiation (energy loss as heat)
		}
		else if (irradiance < 0.1) {
			// Based on the floor area and the sunlight intensity
			lightingGain = (1 - irradiance) * floorArea / 25;
		}

		error = checkError("lightingGain", lightingGain) || error;
		
		// (2g) ADD HEAT GAIN BY EQUIPMENT
		// see heatGainEqiupment below
			
		// Note: heatGain and heatLoss are to be converted from kJ to BTU below
		
		// (2h) CALCULATE TOTAL HEAT GAIN 
		double gain = heatPumpedIn 
				+ heatGainOccupants + heatGainFromEVAHeater 
				+ solarHeatGain + canopyHeatGain 
				+ lightingGain + heatGainEquipment;
		
		// (2i) Calculate the heat transfer due to ventilation

		double ventInHeat = getVentInHeat();	

		error = checkError("ventInHeat", ventInHeat) || error;
		
		if (ventInHeat > 0) {
			// Note: Ensure ventInHeat is positive when adding to the heat gain
			gain += ventInHeat;
			// Reset vent in heat back to zero
			setVentInHeat(0);
		}		

		error = checkError("gain", gain) || error;
		
		/**
		 * Do NOT delete. For future Debugging.
		 */
		if (error)
			logger.warning(building, "Gain: " + Math.round(gain * 1000.0)/1000.0
					+ "  ventInHeat: " + Math.round(ventInHeat * 1000.0)/1000.0
					+ "  heatPumped: " + Math.round(heatPumpedIn * 1000.0)/1000.0
					+ "  Occupants: " + Math.round(heatGainOccupants * 1000.0)/1000.0
					+ "  numEVAgoers: " + Math.round(numEVAgoers)/1000.0
					+ "  EVAHeater: " + Math.round(heatGainFromEVAHeater)/1000.0
					+ "  solarHeatGain: " + Math.round(solarHeatGain * 1000.0)/1000.0
					+ "  canopyHeatGain: " + Math.round(canopyHeatGain * 1000.0)/1000.0
					+ "  lighting: " + Math.round(lightingGain * 1000.0)/1000.0
					+ "  Equipment: " + Math.round(heatGainEquipment * 1000.0)/1000.0);
  
    	return new double[] {gain, canopyHeatGain};
    }
    
    /**
     * Calculates the heat loss.
     * 
     * @param error
     * @return
     */
    private double calculateHeatLoss(double canopyHeatGain, double outTCelsius, double deltaTinTout, 
    		double inTKelvin, double outTKelvin,
    		double irradiance, int numEVAgoers) {
		// (3a) CALCULATE HEAT NEEDED FOR REHEATING AIRLOCK
		
    	// Note that if the heat is negative, it means loss of heat
    	
		double heatAirlock = 0;
		// the energy loss due to gushing out the warm settlement air when airlock
		// is open to the cold Martian air
		
		if (numEVAgoers > 0 && hasHeatDumpViaAirlockOuterDoor) {
			heatAirlock = - ENERGY_FACTOR_EVA * (DEFAULT_ROOM_TEMPERATURE - outTCelsius) * numEVAgoers ;
			// flag that this calculation is done till the next time when 
			// the airlock is depressurized.
			hasHeatDumpViaAirlockOuterDoor = false;
		}
	
		error = checkError("heatAirlock", heatAirlock) || error;
		
		// (3b) CALCULATE HEAT LOSS DUE TO STRUCTURE		
		double structuralLoss = 0;
		// Note: deltaT is positive if indoor T is greater than outdoor T
		if (numEVAgoers > 0) {
			structuralLoss = - CLF * deltaTinTout
					* (uValueAreaCeilingFloor * 2D
					+ uValueAreaWall
					+ uValueAreaCrackLengthAirlock * weather.getWindSpeed(location)) / 1000 / 1.5;
					// Note : 1 m/s = 3.28084 ft/s = 2.23694 miles per hour
		}
		else {
			if (isGreenhouse) {
				structuralLoss = - CLF * deltaTinTout
						* (uValueAreaCeilingFloor
						+ uValueAreaWall
						+ uValueAreaCrackLength * weather.getWindSpeed(location)) / 1000 / 1.5;		
			}
			else {
				structuralLoss = - CLF * deltaTinTout
					* (uValueAreaCeilingFloor * 2D
					+ uValueAreaWall
					+ uValueAreaCrackLength * weather.getWindSpeed(location)) / 1000 / 1.5;
			}
		}	
		
		error = checkError("structuralLoss", structuralLoss) || error;
		
		// Note : U_value in kW/K/m2, not [Btu/°F/ft2/hr]

		// (3d) CALCULATE HEAT LOSS DUE TO HEAT RADIATED TO OUTSIDE
		double solarHeatLoss = 0;
		
		double canopyFactor = (1 + canopyHeatGain) * 2.5;
		// Note: canopyFactor represents the result of closing the canopy on the side wall
		// to avoid excessive heat loss for the greenhouse,
		// especially in the evening
		
		error = checkError("canopyFactor", canopyFactor) || error;
		
		double emissivity = 0;
		// Note: emissivity refers to the effectiveness of a surface to emit 
		// thermal radiation. It is a measure of how well a surface radiates 
		// heat, with a value ranging from 0 (perfect reflector) to 1 (perfect absorber).
		
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

		error = checkError("emissivity", emissivity) || error;
		
		if (isGreenhouse)  {
			emissivity = (EMISSIVITY_DAY * irradiance + EMISSIVITY_NIGHT * (0.7 - irradiance)) * 0.85;
			// e.g. I =  0,  e = .8 * 0  + 1.0 * (.7 -  0)   =  .7 * 1.1
			// e.g. I = .1,  e = .8 *.1  + 1.0 * (.7 - .1)  =  .68 * 1.1
			// e.g. I = .3,  e = .8 *.3  + 1.0 * (.7 - .3)  =  .64 * 1.1
			// e.g. I = .54, e = .8 *.54 + 1.0 * (.7 - .54) = .592 * 1.1
			if (emissivity > 1)
				emissivity = 1;
			else if (emissivity < .3)
				emissivity = .3;

			solarHeatLoss = - emissivity * STEFAN_BOLTZMANN_CONSTANT
					* ( Math.pow(inTKelvin, 4) - Math.pow(outTKelvin, 4) ) 
						* hullArea / canopyFactor / 1000D / 3;
		}
//		else if (isBrick)  {
//			For Outpost Hub, Bunkhouse, Tunnel, Inground Greenhouse, Loading Dock garage	
//			will need to model emissivity for these in-situ construction
//		}
		else {
			emissivity = EMISSIVITY_DAY * irradiance * .1 + EMISSIVITY_NIGHT * (0.7 - irradiance) * .35;
			// e.g. I =  0,  e = 
			// e.g. I = .1,  e = 
			// e.g. I = .3,  e = 
			// e.g. I = .54, e = 
			if (emissivity > 1)
				emissivity = 1;
			else if (emissivity < .15)
				emissivity = .15;
			
			solarHeatLoss = - emissivity * STEFAN_BOLTZMANN_CONSTANT
					* ( Math.pow(inTKelvin, 4) - Math.pow(outTKelvin, 4) ) 
						* hullArea / canopyFactor / 1000D / 2;
		}		
		
//		logger.info(building, "emissivity: " + Math.round(emissivity * 100.0)/100.0
//				+ "  solarHeatLoss: " + Math.round(solarHeatLoss * 100.0)/100.0);
		
		error = checkError("solarHeatLoss", solarHeatLoss) || error;
		
		/**
		 * Do NOT delete. For future Debugging.
		 */
		if (error)
			logger.warning(building, // "inTKelvin: " + inTKelvin
//					+ "   outTKelvin: " + outTKelvin
					"I: " + Math.round(irradiance * 1000.0)/1000.0 
					+ "  canopyFactor: " + Math.round(canopyFactor * 1000.0)/1000.0 
					+ "  canopyHeatGain: " + Math.round(canopyHeatGain * 1000.0)/1000.0 
					+ "  emissivity: " + Math.round(emissivity * 1000.0)/1000.0 
					+ "  solarHeatLoss: " + Math.round(solarHeatLoss * 1000.0)/1000.0);
		
		// (3e) At high RH, the air has close to the maximum water vapor that it can hold, 
		// so evaporation, and therefore heat loss, is decreased.
		
		// (3f) CALCULATE TOTAL HEAT LOSS	
		
		// Note: the new convention is that heat gain is positive and heat loss is negative
		
		double loss = heatAirlock + structuralLoss + solarHeatLoss;
		
		if (ventInHeat < 0) {
			// Note: Ensure ventInHeat is negative when adding to the heat loss
			loss += ventInHeat; 
			// Reset vent in heat back to zero
			setVentInHeat(0);	
		}
	
		error = checkError("loss", loss) || error;
		
		/**
		 * Do NOT delete. For future Debugging.
		 */
		if (error)
			logger.warning(building, "Loss: " + Math.round(loss * 1000.0)/1000.0
					+ "  heatAirlock: " + Math.round(heatAirlock * 1000.0)/1000.0
					+ "  structuralLoss: " + Math.round(structuralLoss * 1000.0)/1000.0
					+ "  solarHeatLoss: " + Math.round(solarHeatLoss * 1000.0)/1000.0
					+ "  ventInHeat: " + Math.round(ventInHeat * 1000.0)/1000.0);
	
		return loss;
    }
    
	/**
	 * Computes the heat transfer by applying the air heat sink.
	 * 
	 * @param dHeat1
	 * @param seconds
	 * @param error
	 * @return
	 */
	private double[] computeAirHeatSink(double dHeat0, double seconds) {
		
		double airMass = building.getLifeSupport().getAir().getTotalMass();
		
//		error = checkError("airMass", airMass) || error;
		
		// References : 
		// (1) https://en.wiktionary.org/wiki/humid_heat
		// (2) https://physics.stackexchange.com/questions/45349/how-air-humidity-affects-how-much-time-is-needed-for-heating-the-air
		
		// C_s = 0.24 + 0.45H where 0.24 BTU/lb°F is the heat capacity of dry air, 0.45 BTU/lb°F is the heat capacity 
		// of water vapor, and SH is the specific humidity, the ratio of the mass of water vapor to that of dry air 
		// in the mixture.
		
		// In SI units, cs = 1.005 + 1.82 * SH where 
		// 1.005 kJ/kg°C is the heat capacity of dry air, 
		// 1.82 kJ/kg°C the heat capacity of water vapor, 
		// and SH is the specific humidity in kg water vapor per kg dry air in the mixture.

		AirComposition.GasDetails gas = building.getLifeSupport().getAir().getGas(ResourceUtil.waterID);
	
		/** The percent of the air is moisture. Assume 1%. */
		double percentAirMoisture = gas.getPercent();
	
//		double airMoistureMass = gas.getMass();
		
		/** The specific heat capacity (C_s) of the air with moisture. */	
		double heatCapAirMoisture = SPECIFIC_HEAT_CAP_AIR_300K + 1.82 * percentAirMoisture / 100;
	
		// Q: should airMoistureMass (instead of airMass) be used instead ?
		// A: No. heatCapAirMoisture includes all elements in the air, not just water moisture
		
		// [kJ/°C] = [kJ/kg°C] * [kg]
		double airHeatCap = heatCapAirMoisture * airMass;

//		error = checkError("airHeatCap", airHeatCap) || error;
		
		double tRise = tPreset - currentTemperature;
		tRise = Math.abs(tRise);
		double factor = seconds/timeSlice * 2.5;
		if (tRise > factor)
			tRise = factor;
		
		// Q = mcΔT
		// kJ = kJ/(kg°C) * kg * °C
		// kJ/s = kJ/(kg°C) * kg * °C / s

		// kJ * 3600 = kWh
		// 1 kW = 1 kJ/s
		
		// [kW] = [kJ/°C] / [seconds] * [°C]
		// [kW/°C] =  [kJ/°C] / [seconds]
		
		double airHeatSink = airHeatCap / seconds * tRise;

		// e.g. EVA Airlock 
		// airMass: 24.956 [kg]
		// heatCapAirMoisture: 1.109 [kJ/kg°C]
		// airHeatCap: 27.673 [kJ/°C]
		// airHeatSink: 0.712 [kW/°C]
		
//		error = checkError("airHeatSink", airHeatSink) || error;
		
		// (5b) Use the air heat sink to buffer the net heat

		double dHeat1 = computeHeatSink(dHeat0, airHeatSink, 0, seconds);
	
		error = checkError("dHeat1", dHeat1) || error;
			
		double convFactor1 = seconds / airHeatCap; 
		
		/**
		 * Do NOT delete. For future Debugging.
		 */
		if (error || isLodging)
			logger.warning(building, "Air heat sink - "
				+ "dHeat0: " + Math.round(dHeat0 * 1000.0)/1000.0	
				+ "  dHeat1: " + Math.round(dHeat1 * 1000.0)/1000.0
				+ "  convFactor1: " + Math.round(convFactor1 * 1000.0)/1000.0
				+ "  airMass: " + Math.round(airMass * 1000.0)/1000.0
				+ "  airHeatCap: " + Math.round(airHeatCap * 1000.0)/1000.0
				+ "  heatCapAirMoisture: " + Math.round(heatCapAirMoisture * 1000.0)/1000.0
				+ "  %AirMoisture: " + Math.round(percentAirMoisture * 100.0)/100.0
				+ "  airHeatSink: " + Math.round(airHeatSink * 1000.0)/1000.0);
		
		return new double[] {dHeat1, convFactor1};
	}
	
	/**
	 * Computes the heat transfer by applying the water heat sink.
	 * 
	 * @param dHeat1
	 * @param waterMass
	 * @param seconds
	 * @param error
	 * @return
	 */
	private double[] computeWaterHeatSink(double dHeat1, double waterMass, 
			double seconds) {
		
		// c.f. with air heat sink
//		double airHeatCap = heatCapAirMoisture * airMass;			
//		double airHeatSink = airHeatCap / millisols / timeSlice;
		
		/** The specific heat capacity (C_s) of the air with moisture. */	
		double heatCapWater = SPECIFIC_HEAT_CAP_WATER_300K;
		
		double waterHeatCap = heatCapWater * waterMass;
		
		double tRise = tPreset - currentTemperature;
		tRise = Math.abs(tRise);
		double factor = seconds/timeSlice * 2.5;
		if (tRise > factor)
			tRise = factor;
		
		// Q = mcΔT
		// kJ = kJ/(kg°C) * kg * °C
		// kJ/s = kJ/(kg°C) * kg * °C / s

		// kJ * 3600 = kWh
		// 1 kW = 1 kJ/s
		
		// [kW] = [kJ/°C] / [seconds] * [°C]
		// [kW/°C] =  [kJ/°C] / [seconds]
		
		double waterHeatSink = waterHeatCap / seconds * tRise;

		error = checkError("waterHeatSink", waterHeatSink) || error;
		
		// 1 / [kJ/°C] * [seconds] =  [seconds] / [kJ/°C]
		// s / C_s / mass = [seconds] / [kJ/°C]
		double convFactor2 = seconds / waterHeatCap; 
		
//		Note: confirm if this is correct: double waterHeatSink = waterHeatCap / millisols / timeSlice ;

		// (5d3) Apply the water heat sink to buffer the net heat
			
		double dHeat2 = computeHeatSink(dHeat1, waterHeatSink, 1, seconds);
		
		error = checkError("dHeat2", dHeat2) || error;
				
//		/**
//		 * Do NOT delete. For Debugging
//		 */
		if (error || isLodging) 
			logger.warning(building, 
				  "Water heat sink - " 
				+ "dHeat1: " + Math.round(dHeat1 * 1000.0)/1000.0
				+ "  dHeat2: " + Math.round(dHeat2 * 1000.0)/1000.0
				+ "  convFactor2: " + Math.round(convFactor2 * 1000.0)/1000.0
				+ "  waterMass: " + Math.round(waterMass * 1000.0)/1000.0
				+ "  waterHeatCap: " + Math.round(waterHeatCap * 1000.0)/1000.0	
				+ "  waterHeatSink: " + Math.round(waterHeatSink * 1000.0)/1000.0);
		
		
		return new double[] {dHeat2, convFactor2};
	}

	
	/**
	 * Finds the new delta temperature.
	 * 
	 * @param netHeat
	 * @param convFactor
	 * @param error
	 * @return
	 */
	private double computeNewDeltaT(double netHeat, double convFactor) {

//		Energy = specific heat capacity * mass * delta temperature
//		[KJ] = [kJ / kg / degC] * kg * degC
//		delta kW * time = specific heat capacity * mass * delta temperature

		// 1 kW = 1 kJ/s
		// [kW] = [kJ/°C] / [seconds] * [°C]
		// [kW/°C] =  [kJ/°C] / [seconds]
		
		// [°C] = [kW] / * [seconds] / [kJ/°C]
//		t = heat * (millisols * timeSlice) / C_s / mass 
		
		double dt = netHeat * convFactor;
	
		if (dt > 20) {
			logger.warning(building, "dt: " 
					+ Math.round(dt * 10.0)/10.0 + " > 20.");
			error = true;
		}
		
		if (dt < -20) {
			logger.warning(building, "dt: " 
					+ Math.round(dt * 10.0)/10.0 + " < 20.");
			error = true;
		}
		
		error = checkError("dt", dt) || error;
		
		if (error || isLodging)
			logger.warning(building, 
				" dt: " + Math.round(dt * 10.0)/10.0
				+ "  last devT: " + Math.round(devTCache * 1000.0)/1000.0
				+ "  netHeat0: " + Math.round(preNetHeat * 1000.0)/1000.0
				+ "  netHeat1: " + Math.round(postNetHeat * 1000.0)/1000.0
				+ "  heatsink: [" + Math.round(heatSink[0] * 100.0)/100.0 
							+ ", " + Math.round(heatSink[1] * 100.0)/100.0 + "]");
		
		return dt;
	}
	
	/**
	 * Cycles through the thermal control system for temperature change.
	 * 
	 * @param deltaTime in millisols
	 */
	private void cycleThermalControl(double deltaTime) {
		// Detect temperatures
		double inT = currentTemperature;
		
		// Reset the error flag
		error = checkError("inT", inT);
		
		if (inT > MAX_INDOOR_TEMPERATURE || inT < 0) {
			logger.warning(building, "inTCelsius: " 
					+ Math.round(inT * 10.0)/10.0);
			error = true;
		}
		
		// STEP 1 : CALCULATE HEAT GAIN/LOSS AND RELATE IT TO THE TEMPERATURE CHANGE
		double output[] = determineDeltaTemperature(inT, deltaTime);
		
		double dt = output[0];
		double convFactor = output[1];
		
		error = checkError("dt", dt) || error ;
		
		// Set the delta temperature and call unitUpdate
		setDeltaTemp(dt);
		
		// STEP 2 : GET NEW CURRENT TEMPERATURE
		double newT = inT + dt;
		if (newT > MAX_INDOOR_TEMPERATURE)
			newT = MAX_INDOOR_TEMPERATURE;
		
		double outT = building.getSettlement().getOutsideTemperature();
		if (outT < 0 && newT < outT)
			newT = outT;
		
		error = checkError("newT", newT) || error ;
		
		// Set the temperature and call unitUpdate
		setTemperature(newT);
		
		// STEP 3 : FIND TEMPERATURE DEVIATION 
		
		// Insert the heat that needs to be gained in the next cycle
		double devT = tPreset - newT ;
		
		error = checkError("devT", devT) || error ;
		
		// if devT is positive, needs to raise temperature by increasing heat
		// if devT is negative, needs to low temperature by transferring heat away
		
		// Set the deviation temperature and call unitUpdate
		setDevTemp(devT);
		
		// STEP 4 : FIND HEAT REQUIRED TO FLATTEN TEMPERATURE DEVIATION 
		
//		Q = C * ΔT * m
//		[kJ] = [J/g°C] * [°C] * [kg]
//		[kW] = [J/g°C] * [°C] * [kg] / [s]
		
//		C is the specific heat capacity of the material (in J/g°C)
//		Q is the heat transfer, the energy required to raise the temperature (in J)
//		ΔT is the change in temperature (in °C)
//		m is the mass of the material (in grams)
		
//		delta kW = specific heat capacity * mass / time * delta temperature
		
		// Calculate the new heat kW required to raise the temperature back to the preset level
//		[kW] = [J/g°C] * [kg] / [s]  * [°C] 
		double reqkW = convFactor * devT;
	
		error = checkError("reqkW", reqkW) || error ;
		
		// Sets the heat required for this cycle
		setHeatRequired(reqkW);
	
		/**
		 * Do NOT delete. For future Debugging.
		 */
//		if (error)
//			logger.warning(building, 
//					" oldT: " + Math.round(inT * 100.0)/100.0
//					+ "  newT: " + Math.round(newT * 100.0)/100.0		
//					+ "  dt: " + Math.round(dt * 100.0)/100.0
//					+ "  devT: " + Math.round(devT * 100.0)/100.0
//					+ "  outT: " + Math.round(outT * 100.0)/100.0
//					+ "  reqkW: " + Math.round(reqkW * 100.0)/100.0);
	}
	
	/**
	 * Checks if a value is invalid.
	 * 
	 * @param type
	 * @param value
	 * @return 
	 */
	private boolean checkError(String type, double value) {
		if (Double.isInfinite(value)) {
			logger.severe(building, type + " is infinite.");
			return true;
		}
		else if (Double.isNaN(value)) {
			logger.severe(building, type + " is NaN.");
			return true;
		}	
		else if (value > MAX_ERROR_VALUE) {
			logger.warning(building, type + ": " 
					+ Math.round(value* 100.0)/100.0 + " > MAX_ERROR_VALUE.");
			return true;
		}
		else if (value < -MAX_ERROR_VALUE) {
			logger.warning(building, type + ": " 
					+ Math.round(value* 100.0)/100.0 + " < -MAX_ERROR_VALUE.");
			return true;
		}	
		return false;
	}	
	
	/**
	 * Computes the amount of heat absorbed or release by the moisture in the air as heat sink. 
	 * 
	 * @param heatNeedToAbsorb the amount of heat waiting to be taken in by the heat sink
	 * @param limit The latentPower in kW
	 * @param index of the heat sink double array
	 * @param millisols
	 * @return the excessive amount of heat that cannot be absorbed
	 */
	private double computeHeatSink(double heatNeedToAbsorb, double limit, 
			int index, double seconds) {
		// Return the excess heat after finding out the heat that can be 
		// absorbed or released by the heat sink of this building
		double excessH = heatNeedToAbsorb; 
	
		// For inflatable greenhouse, air heat sink ~ 3 kW
		// For large greenhouse, air heat sink ~ ? kW, water heat sink ~ 43 kW
		// For algae pond, air heat sink ~ 3 kW, water heat sink ~ 42 kW
		// For fish farm, air heat sink ~ 3 kW, water heat sink ~ 82 kW
		
		// millisols = seconds/timeSlice;
		
		double timeFactor = Math.max(seconds/timeSlice, 1);
		
		// How fast or efficient is the heat transfer ?
		// For air heat sink, assume 90%
		double efficiency = .9;
		if (index == 1) {
			// For water heat sink, assume 50%
			efficiency = .5;
		}
			
		// The fraction of the speed of a perfect conductor for the heat transfer
		double fraction = Math.min(1, timeFactor * efficiency);
		// Calculate the amount of heat that can be absorbed or released
		double transfer = excessH * fraction;
				
		double newSinkValue = heatSink[index];
		
		/**
		 * Do NOT delete. For debugging.
		 */
//		if (isGreenhouse)
//			logger.warning(building, 
//					"efficiency: " + Math.round(efficiency*100.0)/100.0
//					+ "  timeFactor: " + Math.round(timeFactor*100.0)/100.0 + ""
//					+ "  millisols: " + Math.round(millisols*100.0)/100.0 + ""
//					+ "  fraction: " + Math.round(fraction*100.0)/100.0 + ""
////					+ "  limit: " + Math.round(limit*100.0)/100.0 + ""
//					+ "  heatNeedAbsorbed: " + Math.round(heatNeedToAbsorb*100.0)/100.0 + " kW"
//					);
		
		if (excessH > 0) {
			// Need to suck up some heat
			if (newSinkValue + transfer > limit) {
				// Calculate new transfer amount
				transfer = limit - newSinkValue;
				// heatSink max out at limit
				newSinkValue = limit;
				// Reduce excessHeat
				excessH -= transfer;
			}
			else {
				// Soak up the transfer amount
				newSinkValue += transfer;	
				// Reduce excessHeat
				excessH -= transfer;
			}
			
			if (index == 0) {
				setAirHeatSink(newSinkValue);
			}
			else {
				setWaterHeatSink(newSinkValue);
			}
		}
		
		else if (excessH < 0) {
			// Note: both transfer and excessHeat are negative
			// Need to release heat
			if (newSinkValue + transfer < 0) {
				// Calculate new transfer amount
				transfer = - newSinkValue;
				// excessHeat will become less negative
				excessH -= transfer;
				// Take away all the heat in heatSink 
				newSinkValue = 0;
			}
			else {
				// Take away what is need in heatSink 
				newSinkValue += transfer;	
				// excessHeat will become less negative
				excessH -= transfer;
			}
			
			if (index == 0) {
				setAirHeatSink(newSinkValue);
			}
			else {
				setWaterHeatSink(newSinkValue);
			}
		}
		
		/**
		 * Do NOT delete. For debugging.
		 */	
		
//		if (isGreenhouse) logger.info(building, 
//				"index: " + index
//				+ "  heatSink[]: " + Math.round(heatSink[index]*100.0)/100.0 + " kW"
////				+ "  heatNeedToAbsorb: " + Math.round(heatNeedToAbsorb*100.0)/100.0 + " kW"
//				+ "  limit: " + Math.round(limit*100D)/100D + " kW"
//				+ "  transfer: " + Math.round(transfer*100.0)/100.0 + " kW"
//				+ "  excessHeat: " + Math.round(excessHeat*100.0)/100.0 + " kW"
////				+ "  millisols: " + Math.round(millisols*1000.0)/1000.0
//				);
	
		return excessH;
	}

	/**
	 * Computes the outgoing energy leaving this building by ventilation. 
	 * This helps reducing the delta temperature.
	 * 
	 * @param deg inTCelsius
	 * @param time
	 * @return energy to be vent out; if positive, hotter air is coming in;
	 * 			if negative, hotter air is leaving
	 */
	private double calculateVentOutHeat(double deg, double time) {
		double totalHeat = 0;
		boolean tooLow = deg < (tPreset - 2.5 * T_LOWER_SENSITIVITY);
		boolean tooHigh = deg > (tPreset + 2.5 * T_UPPER_SENSITIVITY);
		double speedFactor = .001 * time * CFM;
		
		if (tooLow || tooHigh) { // this temperature range is arbitrary
			// Note : time = .121 at x128

			adjacentBuildings = new ArrayList<>(building.getSettlement().getAdjacentBuildings(building));
			
			int size = adjacentBuildings.size();
//
			for (int i = 0; i < size; i++) {
				double tNow = adjacentBuildings.get(i).getCurrentTemperature();
				double tInit = adjacentBuildings.get(i).getPresetTemperature();

				boolean tooLowNext = tNow < (tInit - 2.5 * T_LOWER_SENSITIVITY);
				boolean tooHighNext = tNow > (tInit + 2.5 * T_UPPER_SENSITIVITY);
				
				double dt = Math.abs(deg - tNow);
				if (dt > 3)
					dt = 3;
				
				double deltaHeat = 0;
				
				if (tooLow) {
					// Heat is coming in
					// Need to suck hotter air from adjacent buildings
					if (tooHighNext) {
						
						if (tNow > deg) {
							// If this adjacent building has a higher T than this 
							// building of interest, then heat is venting in
							deltaHeat = speedFactor * dt;
							deltaHeat = Math.min(deltaHeat, CFM / size) * 2D * areaFactor;
						}
						else if (tNow < deg) {
							// heat is coming in in lesser magnitude
							deltaHeat = speedFactor * dt;
							deltaHeat = Math.min(deltaHeat, CFM / size) * areaFactor;
							// heat is leaving
						//	gain = - 2D * speed_factor * d_t;
						//	gain = Math.max(gain, -CFM/size*2D);
						}
					}
					else if (!tooLowNext) {
						if (tNow > deg) {
							// heat coming in
							deltaHeat = speedFactor * dt * areaFactor;
							deltaHeat = Math.min(deltaHeat, CFM / size * 2D);
						}
						else if (tNow < deg) {
							// heat coming in
							deltaHeat = -.5 *speedFactor * dt * areaFactor;
							deltaHeat = Math.min(deltaHeat, -CFM / size);
						}
					}
				}
				
				else if (tooHigh) {
					// Heat is leaving 
					// Need to dump hotter air to adjacent buildings
					if (tooLowNext) {
						if (deg > tNow) {
							// heat is leaving
							deltaHeat = -2D *speedFactor * dt * areaFactor;
							deltaHeat = Math.max(deltaHeat, -CFM / size * 2D);
						}
						else if (deg < tNow) {
							// heat is leaving
							deltaHeat = -speedFactor * dt * areaFactor;
							deltaHeat = Math.max(deltaHeat, -CFM / size);
						}
					}
					else if (!tooHighNext) {
						if (deg > tNow) {
							// heat is leaving
							deltaHeat = -speedFactor * dt * areaFactor;
							deltaHeat = Math.max(deltaHeat, -CFM / size * 2D);
						}
						else if (deg < tNow) {
							// heat is leaving
							deltaHeat = -.5 * speedFactor * dt * areaFactor;
							deltaHeat = Math.max(deltaHeat, -CFM / size);
						}
					}
				}
				
				if (deltaHeat > 0 && deltaHeat < 3) {
					adjacentBuildings.get(i).addVentInHeat(-deltaHeat);
					logger.info(building, "At " + Math.round(deg * 10.0)/10.0 
							+ " deg, venting in "
							+ Math.round(deltaHeat * 1000.0)/1000.0 + " kW from "
							+ adjacentBuildings.get(i).getName() 
							+ " at " + Math.round(tNow * 10.0)/10.0 + " deg.");
				}
				else if (deltaHeat < 0 && deltaHeat > -3) {
					
					// Inflatable Greenhouse 1 - At 23.5 deg, venting out 50.0 kW to Hallway 1 at 18.6 deg.
					// Q: How does this happen ?
					// Q: why could deltaHeat be added up to 50 kW ?
					// Q: 
					
					adjacentBuildings.get(i).addVentInHeat(-deltaHeat);
					logger.info(building, "At " + Math.round(deg * 10.0)/10.0 
							+ " deg, venting out "
							+ Math.round(-deltaHeat * 1000.0)/1000.0 + " kW to "
							+ adjacentBuildings.get(i).getName() 
							+ " at " + Math.round(tNow * 10.0)/10.0 + " deg.");
				}
				
				totalHeat += deltaHeat;
			}
		}
		
		return totalHeat;
	}


	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding true if adding a new building.
	 * @param settlement the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding,
			Settlement settlement) {
		return 0D;
	}

	/**
	 * Time passing for the building.
	 * 
	 * @param deltaTime amount of time passing (in millisols)
	 */
	public void timePassing(double time) {
		cycleThermalControl(time);
	}
	


	/**
	 * Gets the delta temperature for the heat transfer.
	 * 
	 * @return deg C.
	 */
	public double getDeltaTemp()  {
		return deltaTCache;
	}
	
	/**
	 * Gets the deviation temperature (between the indoor temperature and the preset).
	 * 
	 * @return deg C.
	 */
	public double getDevTemp()  {
		return devTCache;
	}
	
	/**
	 * Gets the heat this building currently required.
	 * 
	 * @return heat in kW.
	 */
	public double getHeatRequired()  {
		return heatRequired;
	}

	/**
	 * Gets the initial net heat gain of this building.
	 * 
	 * @return heat in kW.
	 */
	public double getPreNetHeat() {
		return preNetHeat;
	}
	
	/**
	 * Gets the post net heat gain of this building.
	 * 
	 * @return heat in kW.
	 */
	public double getPostNetHeat() {
		return postNetHeat;
	}

	/**
	 * Gets the heat to be generated for this building.
	 * 
	 * @return heat in kW.
	 */
	public double getHeatGenerated() {
		return heatGeneratedCache;
	}
	
	/**
	 * Gets the excess heat pumped due to server room's computational equipment.
	 * 
	 * @return heat in kW.
	 */
	public double getExcessHeatComputation() {
		return excessHeatComputation;
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
	 * Gets the air heat sink stored in this building.
	 * 
	 * @return heat in kW.
	 */
	public double getAirHeatSink() {
		return heatSink[0];
	}
	
	/**
	 * Sets the air heat sink of this building and call unitUpdate.
	 * 
	 * @param heat in kW.
	 */
	public void setAirHeatSink(double heat)  {
		heatSink[0] = heat;
		building.fireUnitUpdate(UnitEventType.AIR_HEAT_SINK_EVENT);
	}
	
	/**
	 * Sets the water heat sink of this building and call unitUpdate.
	 * 
	 * @param heat in kW.
	 */
	public void setWaterHeatSink(double heat)  {
		heatSink[1] = heat;
		building.fireUnitUpdate(UnitEventType.WATER_HEAT_SINK_EVENT);
	}
	
	/**
	 * Sets the heat this building currently required and call unitUpdate.
	 * 
	 * @param heat in kW.
	 */
	public void setHeatRequired(double heat)  {
		heatRequired = heat;
		building.fireUnitUpdate(UnitEventType.REQUIRED_HEAT_EVENT);
	}
	
	
	/**
	 * Sets the delta Temperature of this building and call unitUpdate.
	 * if positive, it needs to gain some heat
	 * if negative, it needs to lose some heat
	 * 
	 * @param heat in kW.
	 */
	public void setDeltaTemp(double heat)  {
		deltaTCache = heat;
		building.fireUnitUpdate(UnitEventType.DELTA_T_EVENT);
	}
	
	/**
	 * Sets the deviation temperature of this building and call unitUpdate.
	 * if positive, it needs to gain some heat
	 * if negative, it needs to lose some heat
	 * 
	 * @param heat in kW.
	 */
	public void setDevTemp(double heat)  {
		devTCache = heat;
		building.fireUnitUpdate(UnitEventType.DEV_T_EVENT);
	}
	
	/**
	 * Sets the initial net heat gain/loss of this building and call unitUpdate.
	 * 
	 * @param heat in kW.
	 */
	public void setPreNetHeat(double heat)  {
		preNetHeat = heat;
		building.fireUnitUpdate(UnitEventType.NET_HEAT_0_EVENT);
	}

	
	/**
	 * Sets the post net heat gain/loss of this building and call unitUpdate.
	 * 
	 * @param heat in kW.
	 */
	public void setPostNetHeat(double heat)  {
		postNetHeat = heat;
		building.fireUnitUpdate(UnitEventType.NET_HEAT_1_EVENT);
	}
	
	/**
	 * Sets the heat this building currently required and call unitUpdate.
	 * 
	 * @return heat in kW.
	 */
	public void setTemperature(double temp)  {
		currentTemperature = temp;
		building.fireUnitUpdate(UnitEventType.TEMPERATURE_EVENT);
	}
	
	/**
	 * Dumps the heat being generated to meet the heat gain/loss and call unitUpdate.
	 *
	 * @param heat
	 */
	public void insertHeatGenerated(double heat) {
		heatGeneratedCache = heat;
		building.fireUnitUpdate(UnitEventType.GENERATED_HEAT_EVENT);
	}
	
	/**
	 * Dumps the excess heat generated from server farm's equipment and call unitUpdate.
	 */
	public void insertExcessHeatComputation(double heat) {
		excessHeatComputation = heat; 
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
	 * Gets the outgoing heat leaving this building due to ventilation.
	 * 
	 * @return heat in kW.
	*/
	public double getVentOutHeat() {
		return ventOutHeat;
	}

	/**
	 * Gets the incoming heat arriving at this building due to ventilation.
	 * 
	 * @return heat in kW.
	*/
	public double getVentInHeat() {
		return ventInHeat;
	}
	
	/**
	 * Sets the outgoing heat leaving this building due to ventilation.
	 * Note: heat gain if positive; heat loss if negative.
	 * 
	 * @param heat removed or added
	 */
	public void setVentOutHeat(double heat) {
		ventOutHeat = heat;
		building.fireUnitUpdate(UnitEventType.VENT_OUT_EVENT);
	}
	
	/**
	 * Sets the incoming heat arriving at this building due to ventilation.
	 * Note: heat gain if positive; heat loss if negative.
	 * 
	 * @param heat removed or added
	 */
	public void addVentInHeat(double heat) {
		ventInHeat += heat;
		building.fireUnitUpdate(UnitEventType.VENT_IN_EVENT);
	}

	/**
	 * Sets the incoming heat arriving at this building due to ventilation.
	 * Note: heat gain if positive; heat loss if negative.
	 * 
	 * @param heat removed or added
	 */
	public void setVentInHeat(double heat) {
		ventInHeat = heat;
		building.fireUnitUpdate(UnitEventType.VENT_IN_EVENT);
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
