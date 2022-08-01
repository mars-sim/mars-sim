/*
 * Mars Simulation Project
 * Heating.java
 * @date 2022-07-11
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.List;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The Heating class is a building function for regulating temperature in a settlement..
 */
public class Heating
extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
//	private static SimLogger logger = SimLogger.getLogger(Heating.class.getName());
	
	private static final FunctionType FUNCTION = FunctionType.LIFE_SUPPORT;

	// Heat gain and heat loss calculation
	// Source 1: Engineering concepts for Inflatable Mars Surface Greenhouses
	// http://ntrs.nasa.gov/search.jsp?R=20050193847
	// Full ver at http://www.marshome.org/files2/Hublitz2.pdf
	// Revised ver at https://www.researchgate.net/publication/7890528_Engineering_concepts_for_inflatable_Mars_surface_greenhouses

	// Data members
	// KG_TO_LB = 2.204623;
	private static final double DEFAULT_ROOM_TEMPERATURE = 22.5;
	// kW_TO_kBTU_PER_HOUR = 3.41214; // 1 kW = 3412.14 BTU/hr
	private static final double C_TO_K = 273.15;
	private static final double TRANSMITTANCE_GREENHOUSE_HIGH_PRESSURE = .55 ;
	private static final double EMISSIVITY_DAY = 0.8 ;
	private static final double EMISSIVITY_NIGHT = 1.0 ;
	// EMISSIVITY_INSULATED = 0.05 ;
	private static final double STEFAN_BOLTZMANN_CONSTANT = 0.0000000567 ; // in W / (m^2 K^4)

	private static final double LARGE_INSULATION_CANOPY = 0.2; // [in kW]
	private static final double INSULATION_BLANKET = 0.1; // [in kW]
//	private static final double INSULATION_CANOPY =  0.125; // [in kW]
	private static final double HALLWAY_INSULATION = 0.075; // [in kW]
	
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
	private static double uValue = 0.1;

	/**  R-value is a measure of thermal resistance, or ability of heat to transfer from hot to cold, through materials such as insulation */
	// R_value = 30;
	
    private static double uValueAreaCrackLength, uValueAreaCrackLengthAirlock;
    
    // Note : U_value will be converted to metric units in W/m²K. 
    // see at https://www.thenbs.com/knowledge/what-is-a-u-value-heat-loss-thermal-mass-and-online-calculators-explained
    
    private static double qHFactor = 21.4D/10;///2.23694; // 1 m per sec = 2.23694 miles per hours
    
    private static double airChangePerHr = .5;
    
    // Molar mass of CO2 = 44.0095 g/mol
    // average density of air : 0.020 kg/m3
	// double n = weather.getAirDensity(coordinates) * vol / 44D;
    // n_CO2 = .02D * VOLUME_OF_AIRLOCK / 44*1000;
	// 1 cubic feet of air has a total weight of 38.76 g
    // n_air = 1D;
    // n_sum = n_CO2 + n_air;
    
 	private static final int PER_UPDATE = 2 ; // must be a multiple of 2
 	
    /** the heat gain from equipment in kW */
    private double heatGainEqiupment;
	/** specific heat capacity (C_p) of air at 300K [kJ/kg*K] */	 
	private double SPECIFIC_HEAT_CAP_AIR_300K = 1.005;  
	/** Density of dry breathable air [kg/m3] */	
	private double dryAirDensity = 1.275D; //
	/** Factor for calculating airlock heat loss during EVA egress */
	private double energyFactorEVA = SPECIFIC_HEAT_CAP_AIR_300K * BuildingAirlock.AIRLOCK_VOLUME_IN_CM * dryAirDensity /1000; 

    private double width;
    
	/** The floor area of the building. */	
	private double floorArea;
	/** The area spanning the side wall. */
	private double hullArea;
	
	private double transmittanceWindow;

	private double transmittanceGreenhouse;
	
	private double LAMP_GAIN_FACTOR = Crop.LOSS_FACTOR_HPS;

	private double powerRequired = 0;
	
	private double basePowerDownHeatRequirement = 0;
	/** The specific heat capacity (C_s) of the air with moisture. */	
	private double heatCapAirMoisture; 
	/** The total mass of the air and moisture in this building. */	
	private double airMass;
	/** The U value of the ceiling or floor. */
    private double uValueAreaCeilingFloor; 	// Thermal Transmittance 
	/** The U value of the wall. */
    private double uValueAreaWall;
	/** The heat generated by the heating system. */
	private double heatGeneratedCache = 0; // the initial value is zero
	/** The heat extracted by the ventilation system. */
	private double heatLossFromVent;
	/** The current temperature of this building. */
	private double currentTemperature;
	/** The previously recorded temperature of this building. */
	private double[] temperatureCache = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	
	private double timeSlice; 
	
	private double tInitial;

	private double conversionFactor;
	
	private double heatSink = 0;
	
	private double areaFactor;
	
	/** is this building a greenhouse */
	private boolean isGreenhouse = false;
	/** is this building a hallway or tunnel */
	private boolean isHallway = false;
	/** Is the airlock door open */
	private boolean hasHeatDumpViaAirlockOuterDoor = false;
	
	private Coordinates location;
	
	private List<Building> adjacentBuildings;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 */
	public Heating(Building building, FunctionSpec spec) {
		// Call Function constructor.
		super(FUNCTION, spec, building);

		location = building.getLocation();
		double length = building.getLength();
		width = building.getWidth() ;
		floorArea = length * width ;
		areaFactor = Math.sqrt(Math.sqrt(floorArea));

		switch(building.getCategory()) {
			case HALLWAY:
				isHallway = true;
				heatGainEqiupment = 0.0117;
				break;
			case FARMING:
				isGreenhouse = true;
				heatGainEqiupment = 0.117;
				break;
			case ERV:
				heatGainEqiupment = 0.586;
				break;
			case HABITAT:
				heatGainEqiupment = 0.4396;
				break;
			case WORKSHOP:
				heatGainEqiupment = 0.4396;
				break;
			case LIVING:
				heatGainEqiupment = 0.7034;
				break;
			default:
				heatGainEqiupment = 0.0879;
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
		uValueAreaCrackLength = 0.244 * .075 * airChangePerHr * qHFactor * (4 * (.5 + .5) );
		// assuming four windows
		uValueAreaCrackLengthAirlock = 0.244 * .075 * airChangePerHr * qHFactor * (2 * (2 + 6) + 4 * (.5 + .5) );
		//assuming two EVA airlock

		timeSlice = MarsClock.SECONDS_PER_MILLISOL / PER_UPDATE;
	
		// References : 
		// (1) https://en.wiktionary.org/wiki/humid_heat
		// (2) https://physics.stackexchange.com/questions/45349/how-air-humidity-affects-how-much-time-is-needed-for-heating-the-air
		
		// C_s = 0.24 + 0.45H where 0.24 BTU/lb°F is the heat capacity of dry air, 0.45 BTU/lb°F is the heat capacity 
		// of water vapor, and SH is the specific humidity, the ratio of the mass of water vapor to that of dry air 
		// in the mixture.
		
		// In SI units, cs = 1.005 + 1.82H where 1.005 kJ/kg°C is the heat capacity of dry air, 1.82 kJ/kg°C the heat 
		// capacity of water vapor, and H is the specific humidity in kg water vapor per kg dry air in the mixture.

		double SH = 0.01; // assume 1% of air is moisture
		
		heatCapAirMoisture = SPECIFIC_HEAT_CAP_AIR_300K + 1.82 * SH;
		
		tInitial = building.getInitialTemperature();
		
		currentTemperature = tInitial;
	
		for (int i=0; i<temperatureCache.length; i++) {
			temperatureCache[i] = tInitial;
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

	/** Turn heat source off if reaching pre-setting temperature
	 * @return none. set heatMode
	 */
	private void adjustHeatMode(double time) {
		
		if (building.getMalfunctionManager().hasMalfunction()) {
			building.setHeatMode(HeatMode.OFFLINE);
			return;
		}
		
		double tNow = currentTemperature;
			
	    // If T_NOW deg above INITIAL_TEMP, turn off furnace
		if (tNow > tInitial + 1) {
			building.setHeatMode(HeatMode.HEAT_OFF);
		}
		else if (tNow >= tInitial - .5 * T_LOWER_SENSITIVITY / time) {
			building.setHeatMode(HeatMode.ONE_EIGHTH_HEAT);
		}
		else if (tNow >= tInitial - 1.5 * T_LOWER_SENSITIVITY / time) {
			building.setHeatMode(HeatMode.QUARTER_HEAT);
		}
		else if (tNow >= tInitial - 3.5 * T_LOWER_SENSITIVITY / time) {
			building.setHeatMode(HeatMode.HALF_HEAT);
		}
		else if (tNow >= tInitial - 6.5 * T_LOWER_SENSITIVITY / time) {
			building.setHeatMode(HeatMode.THREE_QUARTER_HEAT);
		}
		else {
			building.setHeatMode(HeatMode.FULL_HEAT);
		}
		
		// if building has no power, power down the heating system: building.setHeatMode(HeatMode.HEAT_OFF);
		// Note: should NOT be OFFLINE since solar heat engine can still be turned ON

	}

	/**
	 * Determines the change in temperature.
	 * 
	 * @param tCelsius
	 * @param deltaMillisols in millisols
	 * @return delta temperature in C
	 */
	private double determineDeltaTemperature(double tCelsius, double deltaMillisols) {

		// THIS IS A THREE-PART CALCULATION
		double tOutCelsius = building.getSettlement().getOutsideTemperature();
		// heatGain and heatLoss are to be converted from kJ to BTU below
		double deltaTemp =  tCelsius - tOutCelsius; //1.8 =  9D / 5D;
		double tKelvin = tCelsius + C_TO_K;
		double tOutKelvin = tOutCelsius + C_TO_K;
		
//		if (isGreenhouse)
//			logger.info(building, "diff inside and outside: " + deltaTemp);
		
		//°C  x  9/5 + 32 = °F
		//(°F - 32)  x  5/9 = °C
		
		// (1) CALCULATE HEAT GAIN
		HeatMode mode = building.getHeatMode();
		
		// (1a) CALCULATE HEAT GENERATED BY DIRECTING HEATING FROM THE LAS INTERVAL OF TIME
		double heatPumpedIn = 0; //in kW

		if (mode != HeatMode.HEAT_OFF || mode != HeatMode.OFFLINE) {
			heatPumpedIn = heatGeneratedCache;
			// Manually reset the heat pumped in back to zero 
			// (not necessary since it will be set in every frame)
			// heatGeneratedCache = 0;
			//if (isGreenhouse) System.out.println(building.getNickName() + "'s heatPumpedIn : " + Math.round(heatPumpedIn*10_000D)/10_000D + " kW");
		}
		
//		if (isGreenhouse)
//			logger.info(building, "heatPumpedIn: " + heatPumpedIn);
		
		// (1b) CALCULATE HEAT GAIN BY PEOPLE
		double heatGainOccupants = HEAT_DISSIPATED_PER_PERSON * building.getNumPeople();
		// the energy required to heat up the in-rush of the new martian air
	
//		if (isGreenhouse)
//			logger.info(building, "heatGainOccupants: " + heatGainOccupants);
		
		// (1c) CALCULATE HEAT GAIN BY EVA HEATER
		int num = building.numOfPeopleInAirLock(); // if num > 0, this building has an airlock
		
		double heatGainFromEVAHeater = 0;
		if (num > 0) 
			heatGainFromEVAHeater = building.getTotalPowerForEVA()/2D; 
		
//		if (isGreenhouse)
//			logger.info(building, "heatGainFromEVAHeater: " + heatGainFromEVAHeater);
		
		// divide by 2 since half of the time a person is doing ingress 
		// Note : Assuming EVA heater requires .5kW of power for heating up the air for each person in an airlock during EVA ingress.

		// (1d) CALCULATE SOLAR HEAT GAIN
		// Convert from W to kW
		double I = surface.getSolarIrradiance(location) / 1000.0 ; 
		double solarHeatGain =  0;

		if (isGreenhouse) {
			solarHeatGain =  I * transmittanceGreenhouse * floorArea;
		}
		
		else if (isHallway) {
			solarHeatGain =  I * transmittanceWindow * floorArea / 2 * .5 * .5;
		}
		
		else {
			solarHeatGain =  I * transmittanceWindow * 4 * .5 * .5;
		}		
		
		// if temperature inside is too high, will automatically close the "blind" or "curtain" partially to block the 
		// excessive sunlight from coming in as a way of cooling off the building.
		if (tCelsius < tInitial + 2.0 * T_UPPER_SENSITIVITY) {
			solarHeatGain = solarHeatGain/2;
		}
		else if (tCelsius < tInitial + 3.0 * T_UPPER_SENSITIVITY) {
			solarHeatGain = solarHeatGain/3;
		}			
		else if (tCelsius < tInitial + 4.0 * T_UPPER_SENSITIVITY) {
			solarHeatGain = solarHeatGain/4;
		}			
		else if (tCelsius < tInitial + 5.0 * T_UPPER_SENSITIVITY) {
			solarHeatGain = solarHeatGain/5;
		}				
		else if (tCelsius < tInitial + 6.0 * T_UPPER_SENSITIVITY) {
			solarHeatGain = solarHeatGain/6;
		}				
		else if (tCelsius < tInitial + 7.0 * T_UPPER_SENSITIVITY) {
			solarHeatGain = solarHeatGain/7;
		}				
		else if (tCelsius < tInitial + 8.0 * T_UPPER_SENSITIVITY) {
			solarHeatGain = solarHeatGain/8;
		}				
		else
			solarHeatGain = solarHeatGain/9;
		
//		if (isGreenhouse)
//			logger.info(building, "solarHeatGain: " + solarHeatGain);

		
		// (1e) CALCULATE INSULATION HEAT GAIN
		double canopyHeatGain = 0;
	
		if (I < 25) {
			switch(building.getConstruction()){
				case INFLATABLE:
					canopyHeatGain = LARGE_INSULATION_CANOPY;
					break;
			
				case SEMI_SOLID:
					canopyHeatGain = HALLWAY_INSULATION;
					break;
			
				default:
					canopyHeatGain = INSULATION_BLANKET;
			}
		
			// whenever the sun goes down, put on the canopy over the inflatable ceiling to prevent heat loss
			if (isGreenhouse) {	
				if (tCelsius <= 23.25)
					canopyHeatGain *= .1; 
				else if (tCelsius <= 22.25)
					canopyHeatGain *= .2; 				
			}

			if (tCelsius <= 21.25)
				canopyHeatGain *= .3; 				

			else if (tCelsius <= 20.25)
				canopyHeatGain *= .35; 

			else if (tCelsius <= 19.25)
				canopyHeatGain *= .4; 

			else if (tCelsius <= 18.25)
				canopyHeatGain *= .45; 

			else if (tCelsius <= 17.25)
				canopyHeatGain *= .5; 
			
			else if (tCelsius <= 16.25)
				canopyHeatGain *= .55; 

			else if (tCelsius <= 15.25)
				canopyHeatGain *= .6; 

			else if (tCelsius <= 14.25)
				canopyHeatGain *= .65; 
			
			else if (tCelsius <= 13.25)
				canopyHeatGain *= .7; 
			
			else if (tCelsius <= 12.25)
				canopyHeatGain *= .75; 		

			else if (tCelsius <= 11.25)
				canopyHeatGain *= .8; 		

			else if (tCelsius <= 10.25)
				canopyHeatGain *= .85; 		

			else if (tCelsius <= 9.25)
				canopyHeatGain *= .9; 		

			else if (tCelsius <= 8.25)
				canopyHeatGain *= .95; 		

			else if (tCelsius <= 7.25)
				canopyHeatGain *= 1;
		}
			
//		if (isGreenhouse)
//			logger.info(building, "canopyHeatGain: " + canopyHeatGain);

	
		// (1g) CALCULATE HEAT GAIN DUE TO ARTIFICIAL LIGHTING
		double lightingGain = 0;
		
		if (isGreenhouse && building.getFarming() != null) {
			// greenhouse has a semi-transparent rooftop
			lightingGain = building.getFarming().getTotalLightingPower() * LAMP_GAIN_FACTOR;
	        // For high pressure sodium lamp, assuming 60% are nonvisible radiation (energy loss as heat)
		}	
		
//		if (isGreenhouse)
//			logger.info(building, "lightingGain: " + lightingGain);

		
		// (1f) ADD HEAT GAIN BY EQUIPMENT
		// see heatGainEqiupment below
		
		// (1g) CALCULATE HEAT GAIN DUE TO VENTILATION
		double ventilationHeatGain = heatGainVentilation(tCelsius, deltaMillisols); 
		
//		if (isGreenhouse)
//			logger.info(building, "ventilationHeatGain: " + ventilationHeatGain);
//		
//		if (isGreenhouse)
//			logger.info(building, "heatGainEqiupment: " + heatGainEqiupment);
		
		// (1h) CALCULATE TOTAL HEAT GAIN 
		double heatGain = heatPumpedIn + heatGainOccupants + heatGainFromEVAHeater + solarHeatGain 
				+ canopyHeatGain + lightingGain + ventilationHeatGain + heatGainEqiupment; 		
		
//		if (isGreenhouse)
//			logger.info(building, "heat gain kW: " + heatGain);
		
		// (2) CALCULATE HEAT LOSS
		
		// (2a) CALCULATE HEAT NEEDED FOR REHEATING AIRLOCK
		
		double heatAirlock = 0;
		// the energy loss due to gushing out the warm settlement air when airlock is open to the cold Martian air
		
		if (num > 0 && hasHeatDumpViaAirlockOuterDoor) {
			heatAirlock = energyFactorEVA * (DEFAULT_ROOM_TEMPERATURE - tOutCelsius) * num ;
			// flag that this calculation is done till the next time when the airlock is depressurized.
			hasHeatDumpViaAirlockOuterDoor = false;
		}

//		if (isGreenhouse)
//			logger.info(building, "heatAirlock: " + heatAirlock);
		
		// (2b) CALCULATE HEAT LOSS DUE TO STRUCTURE		
		double structuralLoss = 0;
	
		if (num > 0) {
			structuralLoss = CLF * deltaTemp
					* (uValueAreaCeilingFloor * 2D
					+ uValueAreaWall
					+ uValueAreaCrackLengthAirlock * weather.getWindSpeed(location)) / 1000;
					// Note : 1 m/s = 3.28084 ft/s = 2.23694 miles per hour
		}
		else {
			if (isGreenhouse) {
				structuralLoss = CLF * deltaTemp
						* (uValueAreaCeilingFloor
						+ uValueAreaWall
						+ uValueAreaCrackLength * weather.getWindSpeed(location))/ 1000;		
			}
			else {
				structuralLoss = CLF * deltaTemp
					* (uValueAreaCeilingFloor * 2D
					+ uValueAreaWall
					+ uValueAreaCrackLength * weather.getWindSpeed(location))/ 1000;
			}
		}	
		
//		if (isGreenhouse)
//			logger.info(building, "structuralLoss: " + structuralLoss);
		
		// Note : U_value in kW/K/m2, not [Btu/°F/ft2/hr]

		// (2c) CALCULATE HEAT LOSS DUE TO VENTILATION
		// heatLossFromVent can be smaller than zero
		double ventilationHeatLoss = heatLossFromVent;
		
//		if (isGreenhouse)
//			logger.info(building, "ventilationHeatLoss: " + ventilationHeatLoss);
		
		// reset heatExtracted to zero
		heatLossFromVent = 0;

		// (2d) CALCULATE HEAT LOSS DUE TO HEAT RADIATED BACK TO OUTSIDE
		double solarHeatLoss =  0;
		
		double canopyFactor = 1;
		// canopyFactor represents the result of closing the canopy on the side wall
		// to avoid excessive heat loss for the greenhouse,
		// especially in the evening
		if (I < 25)
			canopyFactor = canopyHeatGain * 5;
		
		if (isGreenhouse) {
			double emissivity = EMISSIVITY_DAY + EMISSIVITY_NIGHT * (1 - I);
			if (emissivity > 1)
				emissivity = 1;
			else if (emissivity < .2)
				emissivity = .2;

			solarHeatLoss = canopyFactor * emissivity * STEFAN_BOLTZMANN_CONSTANT
					* ( Math.pow(tKelvin, 4) - Math.pow(tOutKelvin, 4) ) * hullArea / 1000D;
		}
		else {
			solarHeatLoss = 0;
		}		
		
//		if (isGreenhouse)
//			logger.info(building, "solarHeatLoss: " + solarHeatLoss);
		
		// (2e) At high RH, the air has close to the maximum water vapor that it can hold, 
		// so evaporation, and therefore heat loss, is decreased.
		
		// (2f) CALCULATE TOTAL HEAT LOSS	
		double heatLoss = heatAirlock + structuralLoss + ventilationHeatLoss + solarHeatLoss;
		
//		if (isGreenhouse)
//			logger.info(building, "heat loss kW: " + heatLoss);
		
		// (3) CALCULATE THE INSTANTANEOUS CHANGE OF TEMPERATURE (DELTA T)
		// delta t = (heatGain - heatLoss) / (time_interval * C_s * mass) ;
		
		// (3a) FIND THE DIFFERENCE between heat gain and heat loss
		double diffHeatGainLoss = heatGain - heatLoss;
			
		// (3b) FIND the CONVERSION FACTOR
		double convFactor = 1 / (conversionFactor * deltaMillisols); 

		// (3c) USE HEAT SINK to buffer the difference in heat
		double deltaHeat = 0;
		
		if (tCelsius <= tInitial - 2 * T_LOWER_SENSITIVITY
				|| tCelsius >= tInitial + 2 * T_UPPER_SENSITIVITY)	
			deltaHeat = computeHeatSink(diffHeatGainLoss, convFactor);
		else
			deltaHeat = diffHeatGainLoss;

		// (3d) FIND THE CHANGE OF TEMPERATURE (in degrees celsius)  
		// Using the equation : heat = mass * specific heat capacity * delta temperature
		// d_t = d_heat / mass / C_s
		double dTCelcius = convFactor * deltaHeat ; 
		//applyHeatBuffer(changeOfTinC);

//		if (isGreenhouse)
//			logger.info(building, "dTCelcius: " + dTCelcius);
		
		return dTCelcius;
	}

	/**
	 * Computes the amount of heat absorbed or release by the heat sink. 
	 * This represents the buffer heat owned by the collective structure of this building.
	 * 
	 * @param amountHeat
	 * @param cFactor
	 * @return heat
	 */
	private double computeHeatSink(double amountHeat, double cFactor) {
		double deltaHeat = amountHeat; 
		// (3c0 FIND THE HEAT TO BE ABSORBED OR RELEASED BY THE HEAT SINK
		double limit = floorArea * 1/cFactor; 
		// e.g. For inflatable greenhouse, limit is 59.27 under a situation

		double efficiency = 1;
		double transfer = 0;

		if (heatSink <= 5*floorArea) { 
			efficiency = 1;
		}
		else if (heatSink <= 10*floorArea) { 
			efficiency = .9;
		}
		else if (heatSink <= 15*floorArea) { 
			efficiency = .8;
		}			
		else if (heatSink <= 20*floorArea) { 
			efficiency = .7;
		}	
		else if (heatSink <= 25*floorArea) { 
			efficiency = .6;
		}	
		else if (heatSink <= 30*floorArea) { 
			efficiency = .5;
		}	
		else if (heatSink <= 35*floorArea) { 
			efficiency = .4;
		}	
		else if (heatSink <= 40*floorArea) { 
			efficiency = .3;
		}	
		else if (heatSink <= 45*floorArea) { 
			efficiency = .2;
		}	
		else if (heatSink <= 50*floorArea) { 
			efficiency = .1;
		}	
		else {
			efficiency = 0;
		}
		
		if (deltaHeat > 0) {
					
			if (deltaHeat/limit > 3) {				// e.g. 7 > 5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [0] ");
				transfer = .75 * deltaHeat * efficiency;		// d = 7 - 5 = 2				
			}
			else if (deltaHeat/limit > 2.5) {				// e.g. 7 > 5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [1] ");
				transfer = .6 * deltaHeat * efficiency;		// d = 7 - 5 = 2				
			}

			else if (deltaHeat/limit > 2) {				// e.g. 7 > 5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [1] ");
				transfer = .45 * deltaHeat * efficiency;		// d = 7 - 5 = 2				
			}
			else if (deltaHeat/limit > 1.5 ) {		// e.g. 4 > .5 * 5 
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [2] ");
				transfer = .3 * deltaHeat * efficiency;	// d = 4 - .5 * 5 = 4 - 2.5 = 1.5			
			}
			else if (deltaHeat/limit > 1 ) {		// e.g. 4 > .5 * 5 
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [2] ");
				transfer = .15 * deltaHeat * efficiency;	// d = 4 - .5 * 5 = 4 - 2.5 = 1.5			
			}
			else {
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [3] ");
				transfer = .1 * deltaHeat * efficiency;	// d = 2 - .25 * 5 = 2 - 1.25 = 0.75			
			}
			
			// Suck up some heat
			if (heatSink + transfer > limit) {
				transfer = limit - heatSink;
				// heatSink cannot go higher than this upper limit
				heatSink = limit;
				deltaHeat -= transfer;
			}
			else {
				heatSink += transfer;	
				deltaHeat -= transfer;
			}
		}
		
		else if (deltaHeat < 0) {
			double delta = Math.abs(deltaHeat);
			if (delta/limit > 3) { 			// e.g. -7 < -5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [4] ");
				transfer = .75 * deltaHeat * efficiency;	// d = -7 + 5 = -2; d is negative
			}
			else if (delta/limit > 2.5) { 			// e.g. -7 < -5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [5] ");
				transfer = .6 * deltaHeat * efficiency;	// d = -7 + 5 = -2; d is negative
			}
			else if (delta/limit > 2) { 			// e.g. -7 < -5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [5] ");
				transfer = .5 * deltaHeat * efficiency;	// d = -7 + 5 = -2; d is negative
			}
			else if (delta/limit > 1.5) { 	// e.g. -4 < -.5 * 5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [6] ");
				transfer = .35 * delta * efficiency; // d = -4 + .5 * 5 = -4 + 2.5 = -1.5; d is -ve
			}
			else if (delta/limit > 1) { 	// e.g. -4 < -.5 * 5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [6] ");
				transfer = .25 * delta * efficiency; // d = -4 + .5 * 5 = -4 + 2.5 = -1.5; d is -ve
			}
			else { 	
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [7] ");
				transfer = .15 * deltaHeat * efficiency; // d = -2 + .25 * 5 = -2 + 1.25 = -0.75; d is -ve
			}
			
			// Release heat
			if (heatSink - transfer < 0) {
				deltaHeat = transfer - heatSink;
				heatSink = 0;
			}
			else {
				heatSink -= transfer;	
				deltaHeat += transfer;
			}
		}
		
//		if (isGreenhouse)
//			logger.info(building, "heatSink: " + Math.round(heatSink*100.0)/100.0 + " kW"
//					+ "   transfer: " + Math.round(transfer*100.0)/100.0 + " kW"
//					+ "   deltaHeat: " + Math.round(deltaHeat*100.0)/100.0 + " kW"
//					+ "   limit: " + Math.round(limit*100D)/100D + " kW"
//					);
		
		return deltaHeat;
	}

	/**
	 * Computes heat gain from adjacent room(s) due to air ventilation. 
	 * This helps the temperature equilibrium.
	 * 
	 * @param t temperature
	 * @return temperature
	 */
	private double heatGainVentilation(double t, double time) {
		double totalGain = 0; //heat_dump_1 = 0 , heat_dump_2 = 0;
		boolean tooLow = t < (tInitial - 3.0 * T_LOWER_SENSITIVITY);
		boolean tooHigh = t > (tInitial + 3.0 * T_UPPER_SENSITIVITY);
		double speedFactor = .01 * time * CFM;
		
		if (tooLow || tooHigh) { // this temperature range is arbitrary
			// Note : time = .121 at x128
			
			if (adjacentBuildings == null) {
				adjacentBuildings = building.getSettlement().getBuildingConnectors(building);
			}
			
			int size = adjacentBuildings.size();
			//area_factor = Math.sqrt(Math.sqrt(floorArea));
			
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
				double tInit = adjacentBuildings.get(i).getInitialTemperature();

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
						//else if (t_next - T_LOWER_SENSITIVITY < t) {
							// heat is leaving
						//	gain = -speed_factor * d_t;
						//	gain = Math.max(gain, -CFM/size*2D);
						//}
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
						//else if (t < t_next + T_LOWER_SENSITIVITY) {
							// heat coming in
						//	gain = 2D *speed_factor * d_t;
						//	gain = Math.min(gain, CFM/size*2D);
						//}
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
						//else if (t < t_next + T_LOWER_SENSITIVITY) {
							// heat coming in
						//	gain = speed_factor * d_t;
						//	gain = Math.min(gain, CFM/size*2D);
						//}
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
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			cycleThermalControl(pulse.getElapsed());
		}
		return valid;
	}

	/**
	 * Notifies thermal control subsystem for the temperature change and power up and power down
	 * via 3 steps (this method houses the main thermal control codes).
	 * 
	 * @param deltaTime in millisols
	 */
	private void cycleThermalControl(double deltaTime) {
		// Detect temperatures
		double oldT = currentTemperature;
		double newT = 0;
		double outT = building.getSettlement().getOutsideTemperature();

		airMass = building.getLifeSupport().getAir().getTotalMass();
		conversionFactor = heatCapAirMoisture * airMass / timeSlice; 
		if (conversionFactor <= 0.0)
			return;
			
		// STEP 1 : CALCULATE HEAT GAIN/LOSS AND RELATE IT TO THE TEMPERATURE CHANGE
		double dt = determineDeltaTemperature(oldT, deltaTime);
		
//		if (isGreenhouse)	
//			logger.info(building, " delta temp = " + Math.round(dt*10.0)/10.0);
		
		// STEP 2 : LIMIT THE TEMPERATURE CHANGE
		// Limit any spurious change of temperature for the sake of stability 
//		if (oldT < tInitial + 10.0 * T_LOWER_SENSITIVITY) {
//			if (dt < -10)
//				dt = -10;
//		}
//		else if (oldT > tInitial + 10.0 * T_UPPER_SENSITIVITY) {
//			if (dt > 10)
//				dt = 10;			
//		}		

//		if (isGreenhouse)
//			logger.info(building, " dt = " + Math.round(dt*10.0)/10.0);

		// STEP 3 : Limit the current temperature
		newT = oldT + dt;
		// Safeguard against anomalous dt that would have crashed mars-sim
		if (newT > 45)
			newT = 45;
		else if (newT < outT)
			newT = outT;
		
		// STEP 4 : Stabilize the temperature by delaying the change
		double t = 0;
		int size = temperatureCache.length;
		for (int i=0; i<size; i++) {
			t += temperatureCache[i];
		}

		currentTemperature = (t + oldT + newT) / (size + 2);
		
		for (int i=1; i<size-1; i++) {
			temperatureCache[i] = temperatureCache[i-1];
		}
		temperatureCache[0] = oldT;

		// STEP 5 : CHANGE THE HEAT MODE
		// Turn heat source off if reaching certain temperature thresholds
		adjustHeatMode(deltaTime);
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return powerRequired;
	}

	/**
	 * Sets the power required for heating.
	 * 
	 * @param power
	 */
	public void setPowerRequired(double power) {
		powerRequired = power;
	}

	/**
	 * Gets the heat this building currently required.
	 * 
	 * @return heat in kW.
	 */
	public double getFullHeatRequired()  {
		return heatGeneratedCache;
	}

	/**
	 * Sets the heat generated by this building.
	 */
	public void setHeatGenerated(double heat) {
		heatGeneratedCache = heat; 
	}

	/**
	 * Gets the heat the building requires for power-down mode.
	 * @return heat in kW.
	*/
	public double getPoweredDownHeatRequired() {
		return basePowerDownHeatRequirement;

	}

	/**
	 * Sets the amount of heat loss from ventilation for this building.
	 * Note : heat loss if negative. heat gain if positive.
	 * 
	 * @param heatLoss removed or added
	 */
	public void setHeatLoss(double heatLoss) {
		heatLossFromVent = heatLossFromVent + heatLoss;
	}

	/**
	 * Flags the presence of the heat loss due to opening an airlock outer door.
	 * 
	 * @param value
	 */
	public void flagHeatLostViaAirlockOuterDoor(boolean value) {
		hasHeatDumpViaAirlockOuterDoor = value;
	}

	
	@Override
	public void destroy() {
		super.destroy();
		location = null;
		adjacentBuildings = null;
	}

}
