/*
 * Mars Simulation Project
 * Heating.java
 * @date 2024-07-03
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.air.AirComposition;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.environment.Weather;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingConfig;
import com.mars_sim.core.structure.building.FunctionSpec;
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
	// May add back private static SimLogger logger = SimLogger.getLogger(Heating.class.getName())

	// Heat gain and heat loss calculation
	// Source 1: Engineering concepts for Inflatable Mars Surface Greenhouses
	// http://ntrs.nasa.gov/search.jsp?R=20050193847
	// Full ver at http://www.marshome.org/files2/Hublitz2.pdf
	// Revised ver at https://www.researchgate.net/publication/7890528_Engineering_concepts_for_inflatable_Mars_surface_greenhouses

	// Data members
//	private static final double HEAT_MATCH_MOD = 1;
		
	// KG_TO_LB = 2.204623;
	private static final double DEFAULT_ROOM_TEMPERATURE = 22.5;
	// kW_TO_kBTU_PER_HOUR = 3.41214; // 1 kW = 3412.14 BTU/hr
	private static final double C_TO_K = 273.15;
	private static final double TRANSMITTANCE_GREENHOUSE_HIGH_PRESSURE = .55 ;
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
//	private static final double SPECIFIC_HEAT_CAP_WATER_300K = 4.184;
	/** Density of dry breathable air [kg/m3] */	
	private static final double DRY_AIR_DENSITY = 1.275D; //
	/** Factor for calculating airlock heat loss during EVA egress */
	private static final double ENERGY_FACTOR_EVA = SPECIFIC_HEAT_CAP_AIR_300K * BuildingAirlock.AIRLOCK_VOLUME_IN_CM * DRY_AIR_DENSITY /1000; 
	
 	private static final int PER_UPDATE = 1;
 	
	/**  R-value is a measure of thermal resistance, or ability of heat to transfer from hot to cold, through materials such as insulation */
	// R_value = 30;
	
    private static double uValueAreaCrackLength, uValueAreaCrackLengthAirlock;
   
	/** is this building a greenhouse */
	private boolean isGreenhouse = false;
	/** is this building a hallway or tunnel */
	private boolean isHallway = false;
	/** Is the airlock door open */
	private boolean hasHeatDumpViaAirlockOuterDoor = false;

    /** the heat gain from equipment in kW */
    private double heatGainEquipment;
	/** The width of the building. */	
    private double width;
	/** The floor area of the building. */	
	private double floorArea;
	/** The area spanning the side wall. */
	private double hullArea;
	
	private double transmittanceWindow;

	private double transmittanceGreenhouse;
	
	private double LAMP_GAIN_FACTOR = Crop.LOSS_FACTOR_HPS;
	/** The heat required for heating. */
	private double heatRequired;
	/** The heat gain/loss due to thermal system. */
	private double totalHeatGain;
	/** The base power down heat requirement from buildings.xml. */
	private double basePowerDownHeatRequirement = 0;
	/** The U value of the ceiling or floor. */
    private double uValueAreaCeilingFloor; 	// Thermal Transmittance 
	/** The U value of the wall. */
    private double uValueAreaWall;
	/** The heat generated by the thermal system. */
	private double heatGeneratedCache = 0; // the initial value is zero
	/** The heat pumped in from equipment. */
	private double excessHeat = 0;
	/** The heat gain or loss by the ventilation system from adjacent buildings. */
	private double heatLossFromVent;
	/** The current temperature of this building. */
	private double currentTemperature;
	/** The previously recorded temperature of this building. */
	private double[] temperatureCache = new double[] {
			0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0};
	
	private double tPreset;

	private double[] heatSink = new double[2];

	private double areaFactor;
	
	private double timeSlice = MarsTime.SECONDS_PER_MILLISOL / PER_UPDATE;
	
	/** The heat capacity (C_s * kg) of the air with moisture. */
	private double airHeatCap;
	
	/** The delta temperature due to the heat transfer. */
	private double deltaTemp; 
	
	/** The deviation temperature (between the current temperature and the preset temperature). */
	private double devTemp; 
	
	/** The total mass of the air and moisture in this building. */	
//	private double airMass;

	// References : 
	// (1) https://en.wiktionary.org/wiki/humid_heat
	// (2) https://physics.stackexchange.com/questions/45349/how-air-humidity-affects-how-much-time-is-needed-for-heating-the-air
	
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
		double length = building.getLength();
		width = building.getWidth();
		floorArea = length * width;
		areaFactor = Math.sqrt(Math.sqrt(floorArea));

		switch(building.getCategory()) {
			case HALLWAY:
				isHallway = true;
				heatGainEquipment = 0.117;
				break;
			case FARMING:
				isGreenhouse = true;
				heatGainEquipment = 0.117;
				break;
			case ERV:
				heatGainEquipment = 0.586;
				break;
			case COMMAND:
				heatGainEquipment = 0.4396;
				break;
			case WORKSHOP:
				heatGainEquipment = 0.4396;
				break;
			case LABORATORY:
				heatGainEquipment = 0.4396;
				break;
			case LIVING:
				heatGainEquipment = 0.7034;
				break;
			default:
				heatGainEquipment = 0.0879;
				break;	
		}
		
		if (isGreenhouse) { // Note that greenhouse has a semi-transparent rooftop
			hullArea = length * HEIGHT ; 
			// take only half of the height of the 2 side walls
			// do not include the front and back wall 
			// ceiling & floor not included
			transmittanceGreenhouse = TRANSMITTANCE_GREENHOUSE_HIGH_PRESSURE;
		}
		else {
			transmittanceWindow = 0.75;
			//hullArea = 2D * floorArea + (width + length) * HEIGHT * 2D ; // ceiling included
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
	
		for (int i=0; i<temperatureCache.length; i++) {
			temperatureCache[i] = tPreset;
		}
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
	private double determineDeltaTemperature(double inTCelsius, double millisols) {
		// NOTE: THIS IS A THREE-PART CALCULATION
		// The outside temperature in celsius
		double outTCelsius = building.getSettlement().getOutsideTemperature();
		// Find the temperature difference between outside and inside 
		double deltaTinTout =  inTCelsius - outTCelsius; //1.8 =  9D / 5D;
		// The indoor temperature in kelvin
		double inTKelvin = inTCelsius + C_TO_K;
		// The outside temperature in kelvin
		double outTKelvin = outTCelsius + C_TO_K;
	
		//°C  x  9/5 + 32 = °F
		//(°F - 32)  x  5/9 = °C
		
		// heatGain and heatLoss are to be converted from kJ to BTU below
		// (1g) CALCULATE HEAT GAIN DUE TO VENTILATION
		
		// Note: run heatGainVentilation once only here.
		// Do not run it again when calculating heat loss
		double ventHeatGain = heatGainVentilation(inTCelsius, millisols); 
		
//		if (isGreenhouse) logger.info(building, "ventilationHeatGain: " + ventilationHeatGain)
		
		// (1) CALCULATE HEAT GAIN
		
		// (1a) CALCULATE HEAT GENERATED BY DIRECTING HEATING FROM THE LAST INTERVAL OF TIME
		
		// Note: this includes the actual heat generated from last refresh
		// Plus the excess heat from the surrounding rooms
		double heatPumpedIn = getHeatGenerated() + excessHeat; //in kW

		// Manually reset the heat generated from last frame in back to zero
//		insertHeatGenerated(0);
		// Manually reset the heat pumped from last frame in back to zero
//		insertExcessHeat(0);
	
		// (1b) CALCULATE HEAT GAIN BY PEOPLE
		double heatGainOccupants = HEAT_DISSIPATED_PER_PERSON * building.getNumPeople();
		// the energy required to heat up the in-rush of the new martian air

		// (1c) CALCULATE HEAT GAIN BY EVA HEATER
		int numEVAgoers = building.numOfPeopleInAirLock(); // if num > 0, this building has an airlock
		
		double heatGainFromEVAHeater = 0;
		if (numEVAgoers > 0) 
			heatGainFromEVAHeater = building.getTotalPowerForEVA()/2D; 
	
		// divide by 2 since half of the time a person is doing ingress 
		// Note : Assuming EVA heater requires .5kW of power for heating up the air for each person in an airlock during EVA ingress.

		// (1d) CALCULATE SOLAR HEAT GAIN
		// Convert from W to kW
		double I = surface.getSolarIrradiance(location) / 1000.0 ; 
		// if sunlight = 25 W/m2, I = 25/1000 = 0.025 kW/m2
 
		double solarHeatGain =  0;
		
		// If temperature inside is too low, will automatically close 
		// the window, blind or curtain partially to block the heat from radiating 
		// away to stop cool off the building.
		
		if (I < 0.05) {
			if (isGreenhouse) {
				solarHeatGain =  I * transmittanceGreenhouse * floorArea * .125;
			}
			
			else if (isHallway) {
				solarHeatGain =  I * transmittanceWindow * building.getLength() * .05;
			}
			
			else {
				solarHeatGain =  I * transmittanceWindow * floorArea * .05;
			}		
	
			solarHeatGain = 0.5 * solarHeatGain * (1 - I);
		}
		
		// (1e) CALCULATE INSULATION HEAT GAIN
		double canopyHeatGain = 0;

		// Note: Whenever the sun is about to go down, unfold the outer canopy over the 
		// structure to prevent heat loss
	
		if (I < 0.05) {
			
			switch(building.getConstruction()) {
				case INFLATABLE:
					canopyHeatGain = LARGE_INSULATION_CANOPY;
					break;
			
				case SEMI_SOLID:
					canopyHeatGain = HALLWAY_INSULATION;
					break;
			
				default:
					canopyHeatGain = INSULATION_BLANKET;
			}
				
			canopyHeatGain = 0.5 * canopyHeatGain * (1 - I);
		}
	
		// (1g) CALCULATE HEAT GAIN DUE TO ARTIFICIAL LIGHTING
		double lightingGain = 0;
		
		if (isGreenhouse && building.getFarming() != null) {
			// greenhouse has a semi-transparent rooftop
			lightingGain = building.getFarming().getTotalLightingPower() * LAMP_GAIN_FACTOR;
	        // For high pressure sodium lamp, assuming 60% are invisible radiation (energy loss as heat)
		}	

		// (1f) ADD HEAT GAIN BY EQUIPMENT
		// see heatGainEqiupment below
		
		// (1g) add the heat in the rise or drop in temperature from last cycle
		// namely heatToMatchTemp;
	
		// (1h) CALCULATE TOTAL HEAT GAIN 
		double gain = heatPumpedIn 
				+ heatGainOccupants + heatGainFromEVAHeater 
				+ solarHeatGain + canopyHeatGain 
				+ lightingGain + heatGainEquipment;
		
		if (ventHeatGain > 0) {
			gain += ventHeatGain;
		}
		
		/**
		 * Do NOT delete. For future Debugging.
		 */
//		if (building.getBuildingType().contains("EVA")) 
//			logger.info(building, "Gain: " + Math.round(gain * 1000.0)/1000.0
//					+ "  vent: " + Math.round(ventHeatGain * 1000.0)/1000.0
//					+ "  heatPumped: " + Math.round(heatPumpedIn * 1000.0)/1000.0
//					+ "  Occupants: " + Math.round(heatGainOccupants * 1000.0)/1000.0
//					+ "  numEVAgoers: " + Math.round(numEVAgoers)/1000.0
//					+ "  EVAHeater: " + Math.round(heatGainFromEVAHeater)/1000.0
//					+ "  solarHeatGain: " + Math.round(solarHeatGain * 1000.0)/1000.0
//					+ "  canopyHeat: " + Math.round(canopyHeatGain * 1000.0)/1000.0
//					+ "  lighting: " + Math.round(lightingGain * 1000.0)/1000.0
//					+ "  Equipment: " + Math.round(heatGainEquipment * 1000.0)/1000.0
//					);
		
		
		// (2) CALCULATE HEAT LOSS
		
		// (2a) CALCULATE HEAT NEEDED FOR REHEATING AIRLOCK
		
		double heatAirlock = 0;
		// the energy loss due to gushing out the warm settlement air when airlock
		// is open to the cold Martian air
		
		if (numEVAgoers > 0 && hasHeatDumpViaAirlockOuterDoor) {
			heatAirlock = ENERGY_FACTOR_EVA * (DEFAULT_ROOM_TEMPERATURE - outTCelsius) * numEVAgoers ;
			// flag that this calculation is done till the next time when 
			// the airlock is depressurized.
			hasHeatDumpViaAirlockOuterDoor = false;
		}
	
		// (2b) CALCULATE HEAT LOSS DUE TO STRUCTURE		
		double structuralLoss = 0;
		// Note: deltaT is positive if indoor T is greater than outdoor T
		if (numEVAgoers > 0) {
			structuralLoss = CLF * deltaTinTout
					* (uValueAreaCeilingFloor * 2D
					+ uValueAreaWall
					+ uValueAreaCrackLengthAirlock * weather.getWindSpeed(location)) / 1000;
					// Note : 1 m/s = 3.28084 ft/s = 2.23694 miles per hour
		}
		else {
			if (isGreenhouse) {
				structuralLoss = CLF * deltaTinTout
						* (uValueAreaCeilingFloor
						+ uValueAreaWall
						+ uValueAreaCrackLength * weather.getWindSpeed(location))/ 1000;		
			}
			else {
				structuralLoss = CLF * deltaTinTout
					* (uValueAreaCeilingFloor * 2D
					+ uValueAreaWall
					+ uValueAreaCrackLength * weather.getWindSpeed(location))/ 1000;
			}
		}	
		
		// Note : U_value in kW/K/m2, not [Btu/°F/ft2/hr]

		// (2c) CALCULATE HEAT LOSS DUE TO VENTILATION

		double ventHeatLoss = getHeatVent();	

		// Reset heat vent from loss back to zero
		setHeatVent(0);	

		// (2d) CALCULATE HEAT LOSS DUE TO HEAT RADIATED BACK TO OUTSIDE
		double solarHeatLoss =  0;
		
		double canopyFactor = (1 + canopyHeatGain) * 1.5;
		// Note: canopyFactor represents the result of closing the canopy on the side wall
		// to avoid excessive heat loss for the greenhouse,
		// especially in the evening
		
		double emissivity = 0;
		// Note: emissivity refers to the effectiveness of a surface to emit 
		// thermal radiation. It is a measure of how well a surface radiates 
		// heat, with a value ranging from 0 (perfect reflector) to 1 (perfect absorber).
		
		// In a greenhouse, emissivity plays a significant role in heat loss due to 
		// radiation. When a surface has a high emissivity, it radiates heat more 
		// efficiently, which can lead to increased heat loss. 
		
		// Conversely, a surface with a low emissivity reflects more heat back into 
		// the greenhouse, reducing heat loss.

		if (isGreenhouse)  {
			emissivity = EMISSIVITY_DAY + EMISSIVITY_NIGHT * (1 - I);
			if (emissivity > 1)
				emissivity = 1;
			else if (emissivity < .2)
				emissivity = .2;

			solarHeatLoss = (1 - emissivity) * STEFAN_BOLTZMANN_CONSTANT
					* ( Math.pow(inTKelvin, 4) - Math.pow(outTKelvin, 4) ) * (hullArea + floorArea) / canopyFactor / 1000D;
		}
		else {
			solarHeatLoss = (1 - emissivity) * STEFAN_BOLTZMANN_CONSTANT
					* ( Math.pow(inTKelvin, 4) - Math.pow(outTKelvin, 4) ) * floorArea / canopyFactor / 1000D;
		}		
		
		/**
		 * Do NOT delete. For future Debugging.
		 */
//		if (building.getBuildingType().contains("EVA"))  
//			logger.info(building, // "inTKelvin: " + inTKelvin
////					+ "   outTKelvin: " + outTKelvin
//					"Loss  I: " + Math.round(I * 1000.0)/1000.0 
//					+ "  canopyFactor: " + Math.round(canopyFactor * 1000.0)/1000.0 
//					+ "  canopyHeatGain: " + Math.round(canopyHeatGain * 1000.0)/1000.0 
//					+ "  emissivity: " + Math.round(emissivity * 1000.0)/1000.0 
//					+ "  solarHeatLoss: " + Math.round(solarHeatLoss * 1000.0)/1000.0 );
		
		// (2e) At high RH, the air has close to the maximum water vapor that it can hold, 
		// so evaporation, and therefore heat loss, is decreased.
		
		// (2f) CALCULATE TOTAL HEAT LOSS	
		double loss = heatAirlock + structuralLoss + ventHeatLoss + solarHeatLoss;
		
		if (ventHeatGain < 0) {
			// Note: Ensure ventilationHeatGain becomes positive when adding to the heat loss
			loss -= ventHeatGain; 	
		}
		
		/**
		 * Do NOT delete. For future Debugging.
		 */
//		if (building.getBuildingType().contains("EVA"))
//			logger.info(building, "Loss: " + Math.round(loss * 1000.0)/1000.0
//					+ "  heatAirlock: " + Math.round(heatAirlock * 1000.0)/1000.0
//					+ "  structuralLoss: " + Math.round(structuralLoss * 1000.0)/1000.0
//					+ "  ventHeatLoss: " + Math.round(ventHeatLoss * 1000.0)/1000.0
//					+ "  solarHeatLoss: " + Math.round(solarHeatLoss * 1000.0)/1000.0
//					+ "  ventHeatGain: " + Math.round(ventHeatGain * 1000.0)/1000.0);
		
		// (3) CALCULATE THE INSTANTANEOUS CHANGE OF TEMPERATURE (DELTA T)
		// delta t = (heatGain - heatLoss) / (time_interval * C_s * mass) ;
		
		// (3a) FIND THE DIFFERENCE between heat gain, heat loss and heat match
		double diffHeatGainLoss = gain - loss;
		
		// Set the heat gain
		setTotalHeatGain(diffHeatGainLoss);
			
		// (3b) FIND AIR MOISTURE RELATED FACTORS
		double airMass = building.getLifeSupport().getAir().getTotalMass();
		
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
//		logger.info(building, "Air moisture percent: " + percentAirMoisture);
		
//		double mass = gas.getMass();
//		logger.info(building, "Air moisture mass: " + mass);
		
		/** The specific heat capacity (C_s) of the air with moisture. */	
		double heatCapAirMoisture = SPECIFIC_HEAT_CAP_AIR_300K + 1.82 * percentAirMoisture / 100;
//		logger.info(building, "heatCapAirMoisture: " + heatCapAirMoisture);
		
		airHeatCap = heatCapAirMoisture * airMass;

		double airHeatSinkPerDegC = airHeatCap / millisols / timeSlice;

		// (3c) USE AIR AS HEAT SINK to buffer the difference in heat
		double dHeat1 = computeHeatSink(diffHeatGainLoss, airHeatSinkPerDegC, 0, millisols);
	
//		Using the equation: 
//		Energy = specific heat capacity * mass * delta temperature
//		[KJ] = [kJ / kg / C] * kg * C
//		delta kW * time = specific heat capacity * mass * delta temperature
//		delta temperature = delta kW * time / specific heat capacity / mass
//		d_t = deltaHeat  * millisols * timeSlice * / C_s / mass 
			 
		double dTCelcius = dHeat1 * millisols * timeSlice / airHeatCap;
	
		/**
		 * Do NOT delete. For future Debugging.
		 */
//		if (building.getBuildingType().contains("EVA"))
//			logger.info(building, 
//				"  oldT: " + Math.round(inTCelsius * 100.0)/100.0
//				+ "  dt: " + Math.round(dTCelcius * 1000.0)/1000.0
//				+ "  devT: " + Math.round(devTemp * 1000.0)/1000.0
////				+ "  gain: " + Math.round(gain * 1000.0)/1000.0
////				+ "  loss: " + Math.round(loss * 1000.0)/1000.0
//				+ "  dHeat0: " + Math.round(diffHeatGainLoss * 1000.0)/1000.0
//				+ "  dHeat1: " + Math.round(dHeat1 * 1000.0)/1000.0
//				+ "  heatsink: [" + Math.round(heatSink[0] * 100.0)/100.0 
//							+ ", " + Math.round(heatSink[1] * 100.0)/100.0 + "]"
//				+ "  airHeatCap: " + Math.round(airHeatCap * 1000.0)/1000.0
//				+ "  airMass: " + Math.round(airMass * 1000.0)/1000.0
//				+ "  airkW/degC: " + Math.round(airHeatSinkPerDegC * 1000.0)/1000.0
//				+ "  heatCap: " + Math.round(heatCapAirMoisture * 1000.0)/1000.0
//				+ "  % moist: " + Math.round(percentAirMoisture * 100.0)/100.0
//				+ "  millisols: " + Math.round(millisols * 1000.0)/1000.0
//				+ "  f: " + Math.round(millisols * timeSlice * 1000.0)/1000.0);
	
	
//		// (3d) ACCOUNT FOR BODY OF WATER AS HEAT SINK to buffer the difference in heat
//		// e.g. Fish Farm and Algae Pond, etc.
////		double waterMass = 0;
////		
////		if (building.getFishery() != null) {
////			waterMass = building.getFishery().getTankSize();
////		}
////		else if (building.getAlgae() != null) {
////			waterMass = building.getAlgae().getWaterMass();
////		}
////		
////		if (waterMass > 0) {
//			/** The specific heat capacity (C_s) of the air with moisture. */	
//			double heatCapWater = SPECIFIC_HEAT_CAP_WATER_300K;
//		
//			double waterHeatCap = heatCapWater * waterMass;
//	
//			double convFactor2 = timeSlice / waterHeatCap; 
//			
//			// 3600 kJ = 1 kWh
//			// q = 1 kg * 4.18 J / g / C * delta temperature 
//			double waterkW = waterHeatCap * millisols * timeSlice / 3600 ;
//			// Assuming the floor area affects the energy that the water can hold 
//
//			double deltaWaterHeat = computeHeatSink(deltaAirHeat, waterkW, 1, millisols);
//			
//			// (3e) FIND THE CHANGE OF TEMPERATURE (in degrees celsius)  
//			// Using the equation: 
//			// energy  = specific heat capacity * mass * delta temperature
//			// [KJ] = [kJ / kg / C] * kg * C
//			// delta kW * time = specific heat capacity * mass * delta temperature
//			// delta temperature = delta kW * time / specific heat capacity / mass
//			// d_t = deltaHeat  * millisols * timeSlice * / C_s / mass 
//			dTCelcius = deltaWaterHeat * millisols * convFactor2 ; 
//			
//			/**
//			 * Do NOT delete. For Debugging
//			 */
////			if (isGreenhouse) logger.info(building, 
////					"convFactor2: " + Math.round(convFactor2*100.0)/100.0
////					+ "  airMass: " + Math.round(airMass*100.0)/100.0
////					+ "  waterMass: " + Math.round(waterMass*100.0)/100.0				
////					+ "  diffHeatGainLoss: " + Math.round(diffHeatGainLoss*1000.0)/1000.0
////					+ "  airkW: " + Math.round(airkW*1000.0)/1000.0
////					+ "  waterkW: " + Math.round(waterkW*1000.0)/1000.0
////					+ "  deltaAirHeat: " + Math.round(deltaAirHeat*100.0)/100.0
////					+ "  deltaWaterHeat: " + Math.round(deltaWaterHeat*100.0)/100.0
////					+ "  dTCelcius: " + Math.round(dTCelcius*100.0)/100.0);
//		}
//		else {
//			double convFactor = timeSlice / airHeatCap; 
//			
//			// (3e) FIND THE CHANGE OF TEMPERATURE (in degrees celsius)  
//			// Using the equation: 
//			// energy  = specific heat capacity * mass * delta temperature
//			// [KJ] = [kJ / kg / C] * kg * C
//			// delta kW * time = specific heat capacity * mass * delta temperature
//			// delta temperature = delta kW * time / specific heat capacity / mass
//			// d_t = deltaHeat  * millisols * timeSlice * / C_s / mass 
//			dTCelcius = deltaAirHeat * millisols * convFactor ; 
//			
//			/**
//			 * Do NOT delete. For Debugging
//			 */
//			if (isGreenhouse) logger.info(building, 
//					"convFactor: " + Math.round(convFactor*100.0)/100.0
//					+ "  airMass: " + Math.round(airMass*100.0)/100.0
//					+ "  diffHeatGainLoss: " + Math.round(diffHeatGainLoss*1000.0)/1000.0
//					+ "  airkW: " + Math.round(airkW*1000.0)/1000.0
//					+ "  deltaAirHeat: " + Math.round(deltaAirHeat*100.0)/100.0
//					+ "  dTCelcius: " + Math.round(dTCelcius*100.0)/100.0);
//		}
		
		return dTCelcius;
	}

	/**
	 * Computes the amount of heat absorbed or release by the moisture in the air as heat sink. 
	 * 
	 * @param heatNeedToAbsorb
	 * @param limit The latentPower in kW
	 * @param index of the heat sink double array
	 * @param millisols
	 * @return the excessive amount of heat that cannot be absorbed
	 */
	private double computeHeatSink(double heatNeedToAbsorb, double limit, 
			int index, double millisols) {
		// Return the excess heat after finding out the heat that can be 
		// absorbed or released by the heat sink of this building
		double excessH = heatNeedToAbsorb; 

		// For inflatable greenhouse,
		// limit = 70.33 kW
		// conversionFactor = 0.64
		
		double timeFactor = Math.max(millisols, 1);
		
		// How efficient is the heat transfer
		// If it's air heat sink, assume 100%
		double efficiency = .7;
		if (index == 1) {
			// If it's water heat sink, it's 30%
			efficiency = .3;
		}
			
		// The fraction of the speed of a perfect conductor for the heat transfer
		double fraction = Math.min(1, timeFactor * efficiency);
		// Calculate the amount of heat that can be absorbed or released
		double transfer = excessH * fraction;
				
		/**
		 * Do NOT delete. For debugging.
		 */
//		if (isGreenhouse)
//			logger.info(building, 
//					"efficiency: " + Math.round(efficiency*100.0)/100.0
//					+ "  timeFactor: " + Math.round(timeFactor*100.0)/100.0 + ""
//					+ "  millisols: " + Math.round(millisols*100.0)/100.0 + ""
//					+ "  fraction: " + Math.round(fraction*100.0)/100.0 + ""
////					+ "  limit: " + Math.round(limit*100.0)/100.0 + ""
//					+ "  heatNeedAbsorbed: " + Math.round(heatNeedToAbsorb*100.0)/100.0 + " kW"
//					);
		

		if (excessH > 0) {
			// Need to suck up some heat
			if (heatSink[index] + transfer > limit) {
				// Calculate new transfer amount
				transfer = limit - heatSink[index];
				// heatSink max out at limit
				heatSink[index] = limit;
				// Reduce excessHeat
				excessH -= transfer;
			}
			else {
				// Soak up the transfer amount
				heatSink[index] += transfer;	
				// Reduce excessHeat
				excessH -= transfer;
			}
		}
		
		else if (excessH < 0) {
			// Note: both transfer and excessHeat are negative
			// Need to release heat
			if (heatSink[index] + transfer < 0) {
				// Calculate new transfer amount
				transfer = - heatSink[index];
				// Take away all the heat in heatSink 
				heatSink[index] = 0;
				// excessHeat will become less negative
				excessH += heatSink[index];
			}
			else {
				// Take away what is need in heatSink 
				heatSink[index] += transfer;	
				// excessHeat will become less negative
				excessH -= transfer;
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
	 * Computes heat gain from adjacent room(s) due to air ventilation. 
	 * This helps the temperature equilibrium.
	 * 
	 * @param t inTCelsius
	 * @param time
	 * @return temperature
	 */
	private double heatGainVentilation(double t, double time) {
		double totalGain = 0; //heat_dump_1 = 0 , heat_dump_2 = 0;
		boolean tooLow = t < (tPreset - 2 * T_LOWER_SENSITIVITY);
		boolean tooHigh = t > (tPreset + 2 * T_UPPER_SENSITIVITY);
		double speedFactor = .001 * time * CFM;
		
		if (tooLow || tooHigh) { // this temperature range is arbitrary
			// Note : time = .121 at x128

			adjacentBuildings = new ArrayList<>(building.getSettlement().getAdjacentBuildings(building));
			
			int size = adjacentBuildings.size();
//			area_factor = Math.sqrt(Math.sqrt(floorArea));
			
//			if (isHallway)
//				area_factor = .5;
//			else if (isGreenhouse) 
//				area_factor = 1.5;
//			else if (isLargeGreenhouse()) 
//				area_factor = 2.3;
//			else if (isGarage())
//				area_factor = 2;
//			else if (isLoadingDockGarage())
//				area_factor = 2.5;
		
			for (int i = 0; i < size; i++) {
				double tNext = adjacentBuildings.get(i).getCurrentTemperature();
				double tInit = adjacentBuildings.get(i).getPresetTemperature();

				boolean tooLowNext = tNext < (tInit - 2.5 * T_LOWER_SENSITIVITY);
				boolean tooHighNext = tNext > (tInit + 2.5 * T_UPPER_SENSITIVITY);
				
				double dt = Math.abs(t - tNext);

				double gain = 0;
				if (tooLow) {
					if (tooHighNext) {
						if (tNext > t) {
							// heat coming in
							gain = 2D * speedFactor * dt * areaFactor;
							gain = Math.min(gain, CFM / size * 2D * areaFactor);
						}
						else {
							// heat coming in
							gain = speedFactor * dt * areaFactor;
							gain = Math.min(gain, CFM / size * areaFactor);
							// heat is leaving
						//	gain = - 2D * speed_factor * d_t;
						//	gain = Math.max(gain, -CFM/size*2D);
						}
					}
					else if (!tooLowNext) {
						if (tNext > t) {
							// heat coming in
							gain = speedFactor * dt * areaFactor;
							gain = Math.min(gain, CFM / size * 2D);
						}
						else {
							// heat coming in
							gain = .5 *speedFactor * dt * areaFactor;
							gain = Math.min(gain, CFM / size);
						}
					}
				}
				
				else if (tooHigh) {
					if (tooLowNext) {
						if (t > tNext) {
							// heat is leaving
							gain = -2D *speedFactor * dt * areaFactor;
							gain = Math.max(gain, -CFM / size * 2D * areaFactor);
						}
						else {
							// heat is leaving
							gain = -speedFactor * dt * areaFactor;
							gain = Math.max(gain, -CFM / size * areaFactor);
						}
					}
					else if (!tooHighNext) {
						if (t > tNext) {
							// heat is leaving
							gain = -speedFactor * dt * areaFactor;
							gain = Math.max(gain, -CFM / size * 2D);
						}
						else {
							// heat is leaving
							gain = -.5 * speedFactor * dt * areaFactor;
							gain = Math.max(gain, -CFM / size);
						}
					}
				}
				
				adjacentBuildings.get(i).extractHeat(gain);
				
				totalGain += gain;
			}
		}
		
		return totalGain;
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

//	/**
//	 * Moderate the time for heating.
//	 * 
//	 * @param time time in millisols
//	 * @throws Exception if error during action.
//	 */
//	private void moderateTime(double time) {
////		// Call cycleThermalControl and consume the pulse time.
////		cycleThermalControl(time);
//		double remainingTime = time;
//		double deltaTime = Task.getStandardPulseTime();
//		while (remainingTime > 0 && deltaTime > 0) {
//			if (remainingTime > deltaTime) {
//				// Call cycleThermalControl and consume the pulse time.
//				cycleThermalControl(deltaTime);
//				// Reduce the total time by the pulse time
//				remainingTime -= deltaTime;
//			}
//			else {
//				// Call cycleThermalControl and consume the pulse time.
//				cycleThermalControl(remainingTime);
//				// Reduce the total time by the pulse time
//				remainingTime = 0;
//			}
//		}
//	}
	
	/**
	 * Notifies thermal control subsystem for the temperature change and power up and power down
	 * via 3 steps (this method houses the main thermal control codes).
	 * 
	 * @param deltaTime in millisols
	 */
	private void cycleThermalControl(double deltaTime) {
		// Detect temperatures
		double inT = currentTemperature;
//		double outT = building.getSettlement().getOutsideTemperature();
			
		// STEP 1 : CALCULATE HEAT GAIN/LOSS AND RELATE IT TO THE TEMPERATURE CHANGE
		double dt = determineDeltaTemperature(inT, deltaTime);
		
		// Set the delta temperature and call unitUpdate
		setDeltaTemp(dt);
		
		// STEP 2 : LIMIT THE TEMPERATURE CHANGE
		// Limit any spurious change of temperature for the sake of stability 
//		if (oldT < tPreset - 5.0 * T_LOWER_SENSITIVITY) {
//			if (dt < - 5.0 * T_LOWER_SENSITIVITY * 2)
//				dt = - 5.0 * T_LOWER_SENSITIVITY * 2;
//		}
//		else if (oldT > tPreset + 5.0 * T_UPPER_SENSITIVITY) {
//			if (dt > 5.0 * T_UPPER_SENSITIVITY * 2)
//				dt = 5.0 * T_UPPER_SENSITIVITY * 2;			
//		}		

//		if (isGreenhouse) logger.info(building, " dt = " + Math.round(dt*10.0)/10.0);

//		currentTemperature += dt;
		
//		if (isGreenhouse) logger.info(building, "currentTemperature: " + Math.round(currentTemperature*10.0)/10.0);
		
		// STEP 3 : Limit the current temperature
		double newT = inT + dt;
		
		// Set the temperature and call unitUpdate
		setTemperature(newT);
		
//		// STEP 4 : Stabilize the temperature by delaying the change
//		double t = 0;
//		int size = temperatureCache.length;
//		for (int i=0; i<size; i++) {
//			t += temperatureCache[i];
//		}
//
//		// Modify currentTemperature with values in temperatureCache
//		double latestT = (t + newT) / (size + 1);
//
//		currentTemperature = latestT;
//		
//		// Push index out
//		for (int i=1; i<size-1; i++) {
//			temperatureCache[i] = temperatureCache[i-1];
//		}
//		
//		// Insert the latest temperature to the first of the list
//		temperatureCache[0] = newT;
		
		// Insert the heat that needs to be gained in the next cycle
		double devT = tPreset - newT ;
		
		// if devT is positive, needs to raise temperature by increasing heat
		// if devT is negative, needs to low temperature by transferring heat away
		
		// Set the deviation temperature and call unitUpdate
		setDevTemp(devT);
		
//		Q = C * ΔT * m
//		[kJ] = [J/g°C] * [°C] * [kg]
//		[kW] = [J/g°C] * [°C] * [kg] / [s]
		
//		C is the specific heat capacity of the material (in J/g°C)
//		Q is the heat transfer, the energy required to raise the temperature (in J)
//		ΔT is the change in temperature (in °C)
//		m is the mass of the material (in grams)
		
//		delta kW = specific heat capacity / time * mass * delta temperature
		
		// Calculate and cache the heat kW needed to raise the temperature back to the preset
		double req = airHeatCap / (deltaTime * timeSlice) * devT;
		
		// Sets the heat required for this cycle
		setHeatRequired(req);
	
		/**
		 * Do NOT delete. For future Debugging.
		 */
//		if (building.getBuildingType().contains("EVA"))
//			logger.info(building, 
//					"  newT: " + Math.round(newT * 100.0)/100.0
//					+ "  dt: " + Math.round(dt * 100.0)/100.0
//					+ "  devT: " + Math.round(devT * 100.0)/100.0
//					+ "  req: " + Math.round(req * 100.0)/100.0);
	}

	/**
	 * Gets the delta temperature for the heat transfer.
	 * 
	 * @return deg C.
	 */
	public double getDeltaTemp()  {
		return deltaTemp;
	}
	
	/**
	 * Gets the deviation temperature (between the indoor temperature and the preset).
	 * 
	 * @return deg C.
	 */
	public double getDevTemp()  {
		return devTemp;
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
	 * Gets the heat gain of this building.
	 * 
	 * @return heat in kW.
	 */
	public double getHeatGain() {
		return totalHeatGain;
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
	 * Gets the excess heat of this building.
	 * 
	 * @return heat in kW.
	 */
	public double getExcessHeat() {
		return excessHeat;
	}
	
	/**
	 * Sets the delta Temperature of this building and call unitUpdate.
	 * if positive, it needs to gain some heat
	 * if negative, it needs to lose some heat
	 * 
	 * @param heat in kW.
	 */
	public void setDeltaTemp(double heat)  {
		deltaTemp = heat;
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
		devTemp = heat;
		building.fireUnitUpdate(UnitEventType.DEV_T_EVENT);
	}
	
	/**
	 * Sets the total heat gain/loss of this building and call unitUpdate.
	 * 
	 * @param heat in kW.
	 */
	public void setTotalHeatGain(double heat)  {
		totalHeatGain = heat;
		building.fireUnitUpdate(UnitEventType.TOTAL_HEAT_GAIN_EVENT);
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
	 * Sets the heat this building currently required and call unitUpdate.
	 * 
	 * @return heat in kW.
	 */
	public void setTemperature(double temp)  {
		currentTemperature = temp;
		building.fireUnitUpdate(UnitEventType.TEMPERATURE_EVENT);
	}
		
	/**
	 * Sets the heat ventilation and call unitUpdate.
	 * 
	 * @return heat in kW.
	 */
	public void setHeatVent(double heat)  {
		heatLossFromVent = heat;
		building.fireUnitUpdate(UnitEventType.HEAT_VENT_EVENT);
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
	 * Dumps the excess heat to the building and call unitUpdate.
	 */
	public void insertExcessHeat(double heat) {
		excessHeat = heat; 
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
	 * Gets the heat gain/loss due to ventilation from adjacent buildings.
	 * 
	 * @return heat in kW.
	*/
	public double getHeatVent() {
		return heatLossFromVent;
	}
	
	/**
	 * Extracts the amount of heat from ventilation for this building.
	 * Note : heat loss if positive; heat gain if negative.
	 * 
	 * @param heatLoss removed or added
	 */
	public void extractHeat(double heatLoss) {
		heatLossFromVent += heatLoss;
		building.fireUnitUpdate(UnitEventType.HEAT_VENT_EVENT);
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
	 * Gets the water heat sink.
	 * 
	 * @return
	 */
	public double getWaterHeatSink() {
		return heatSink[1];
	}
	
	/**
	 * Gets the air heat sink.
	 * 
	 * @return
	 */
	public double getAirHeatSink() {
		return heatSink[0];
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
		temperatureCache = null;
		heatSink = null;
	}

}
