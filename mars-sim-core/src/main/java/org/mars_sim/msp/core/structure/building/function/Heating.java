/*
 * Mars Simulation Project
 * Heating.java
 * @date 2022-06-25
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The Heating class is a building function for regulating temperature in a settlement..
 */
public class Heating
extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
    /* default logger.*/
 	private static final Logger logger = Logger.getLogger(Heating.class.getName());
    
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
	private static final double INSULATION_CANOPY =  0.125; // [in kW]
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
	private static final double CLF = 1.2D;
	
	/** 
	 * The U-value in [Watts/m^2/°K] is the thermal transmittance (reciprocal of R-value)
	 * Note that Heat Loss = (1/R-value)(surface area)(∆T) 
	 */
	private static double U_value = 0.1;

	/**  R-value is a measure of thermal resistance, or ability of heat to transfer from hot to cold, through materials such as insulation */
	// R_value = 30;
	
    private static double U_value_area_crack_length, U_value_area_crack_length_for_airlock;
    
    // Note : U_value will be converted to metric units in W/m²K. 
    // see at https://www.thenbs.com/knowledge/what-is-a-u-value-heat-loss-thermal-mass-and-online-calculators-explained
    
    private static double q_H_factor = 21.4D/10;///2.23694; // 1 m per sec = 2.23694 miles per hours
    
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
	/** specific heat capacity of air at 300K [kJ/kg*K] */	 
	private double C_p = 1.005;  
	/** Density of dry breathable air [kg/m3] */	
	private double dryAirDensity = 1.275D; //
	/** Factor for calculating airlock heat loss during EVA egress */
	private double energy_factor_EVA = C_p * BuildingAirlock.AIRLOCK_VOLUME_IN_CM * dryAirDensity /1000; 

    private double width;
    
	/** The floor area of the building. */	
	private double floorArea;
	/** The area spanning the side wall. */
	private double hullArea;
	
	private double transmittance_window;

	private double transmittance_greenhouse;
	
	private double gain_factor_HPS = Crop.LOSS_FACTOR_HPS;

	private double powerRequired = 0;
	
	private double basePowerDownHeatRequirement = 0;
	/** The specific heat capacity of the air with moisture. */	
	private double C_s; 
	/** The total mass of the air and moisture in this building. */	
	private double mass;
	/** The U value of the ceiling or floor. */
    private double U_value_area_ceiling_or_floor; 	// Thermal Transmittance 
	/** The U value of the wall. */
    private double U_value_area_wall;
	/** The heat generated by the heating system. */
	private double heatGeneratedCache = 0; // the initial value is zero
	/** The heat extracted by the ventilation system. */
	private double heatLossFromVent;
	/** The current temperature of this building. */
	private double currentTemperature;
	/** The previously recorded temperature of this building. */
	private double[] temperatureCache = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	
	private double timeSlice; 
	
	private double t_initial;

	private double conversion_factor;
	
	private double heat_sink = 0;
	
	private double area_factor;
	
	/** is this building a greenhouse */
	private boolean isGreenhouse = false;
	/** is this building a hallway or tunnel */
	private boolean isHallway = false;
	/** Is the airlock door open */
	private boolean hasHeatDumpViaAirlockOuterDoor = false;
	
	private Coordinates location;
	
	private List<Building> adjacentBuildings;

	private Farming farm;
	
	/**
	 * Constructor.
	 * @param building the building this function is for.
	 */
	public Heating(Building building, FunctionSpec spec) {
		// Call Function constructor.
		super(FUNCTION, spec, building);
        
		String buildingType = building.getBuildingType();
		
		location = building.getLocation();

		double length = building.getLength();
		width = building.getWidth() ;

		floorArea = length * width ;

		area_factor = Math.sqrt(Math.sqrt(floorArea));
		
		// TODO OOhhhh not nice using string values
		// Should be  part of BuildingSpec
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
			transmittance_greenhouse = TRANSMITTANCE_GREENHOUSE_HIGH_PRESSURE;
		}
		else {
			transmittance_window = 0.75;
			//hullArea = 2D * floorArea + (width + length) * HEIGHT * 2D ; // ceiling included
		}
	
		U_value_area_wall = U_value * 2D * (width + length) * HEIGHT;
		
		U_value_area_ceiling_or_floor = U_value * floorArea;
		// assuming airChangePerHr = .5 and q_H = 21.4;
		U_value_area_crack_length = 0.244 * .075 * airChangePerHr * q_H_factor * (4 * (.5 + .5) );
		// assuming four windows
		U_value_area_crack_length_for_airlock = 0.244 * .075 * airChangePerHr * q_H_factor * (2 * (2 + 6) + 4 * (.5 + .5) );
		//assuming two EVA airlock

		timeSlice = MarsClock.SECONDS_PER_MILLISOL / PER_UPDATE ;
	
		// References : 
		// (1) https://en.wiktionary.org/wiki/humid_heat
		// (2) https://physics.stackexchange.com/questions/45349/how-air-humidity-affects-how-much-time-is-needed-for-heating-the-air
		
		// C_s = 0.24 + 0.45H where 0.24 BTU/lb°F is the heat capacity of dry air, 0.45 BTU/lb°F is the heat capacity 
		// of water vapor, and SH is the specific humidity, the ratio of the mass of water vapor to that of dry air 
		// in the mixture.
		
		// In SI units, cs = 1.005 + 1.82H where 1.005 kJ/kg°C is the heat capacity of dry air, 1.82 kJ/kg°C the heat 
		// capacity of water vapor, and H is the specific humidity in kg water vapor per kg dry air in the mixture.

		double SH = 0.01; // assume 1% of air is moisture
		
		C_s = C_p + 1.82 * SH;
		
		t_initial = building.getInitialTemperature();
		
		currentTemperature = t_initial;
	
		for (int i=0; i<temperatureCache.length; i++) {
			temperatureCache[i] = t_initial;
		}
	}
    
    /**
     * Gets the temperature of a building.
     * @return temperature (deg C)
    */
    public double getCurrentTemperature() {
    	return currentTemperature;
    }

	/** Turn heat source off if reaching pre-setting temperature
	 * @return none. set heatMode
	 */
	// TODO: also set up a time sensitivity value
	private void adjustHeatMode() {
		double t_now = currentTemperature;
		//if (building.getPowerMode() == PowerMode.FULL_POWER) {
			// ALLOWED_TEMP is thermostat's allowance temperature setting
		    // If T_NOW deg above INITIAL_TEMP, turn off furnace
			if (t_now > t_initial) {
				building.setHeatMode(HeatMode.HEAT_OFF);
			}
			//else if (t_now >= t_initial - .5 * T_LOWER_SENSITIVITY) {// + T_UPPER_SENSITIVITY) {
			//	building.setHeatMode(HeatMode.HEAT_OFF);
			//}
			else if (t_now >= t_initial - 1.5 * T_LOWER_SENSITIVITY) {
				building.setHeatMode(HeatMode.QUARTER_HEAT);
			}
			else if (t_now >= t_initial - 3.5 * T_LOWER_SENSITIVITY) {
				building.setHeatMode(HeatMode.HALF_HEAT);
			}
			else if (t_now >= t_initial - 6.0 * T_LOWER_SENSITIVITY) {
				building.setHeatMode(HeatMode.THREE_QUARTER_HEAT);
			}
			else {//if (t_now < (t_initial - T_LOWER_SENSITIVITY)) {
				building.setHeatMode(HeatMode.FULL_HEAT);
			} //else ; // do nothing to change the HeatMode
		//}
		//else if (building.getPowerMode() == PowerMode.POWER_DOWN)
			// if building has no power, power down the heating system
		//	building.setHeatMode(HeatMode.HEAT_OFF);
		
		// Note: should NOT be OFFLINE since solar heat engine can still be turned ON
		
		// if building is under maintenance, use HeatMode.OFFLINE
		//else 
		if (building.getMalfunctionManager().hasMalfunction())
			building.setHeatMode(HeatMode.OFFLINE);

	}

	/**
	 * Determines the change in temperature 
	 * @param t_in_C
	 * @param delta_time in millisols
	 * @return delta temperature in C
	 */
	private double determineDeltaTemperature(double t_in_C, double delta_time) {

		// THIS IS A THREE-PART CALCULATION
		double t_out_C = building.getSettlement().getOutsideTemperature();
		// heatGain and heatLoss are to be converted from kJ to BTU below
		double d_t =  t_in_C - t_out_C; //1.8 =  9D / 5D;
		double t_in_K = t_in_C + C_TO_K;
		double t_out_K = t_out_C + C_TO_K;
		
		//°C  x  9/5 + 32 = °F
		//(°F - 32)  x  5/9 = °C
		
		// (1) CALCULATE HEAT GAIN
		HeatMode mode = building.getHeatMode();
		
		// (1a) CALCULATE HEAT GENERATED BY DIRECTING HEATING FROM THE LAS INTERVAL OF TIME
		double heatPumpedIn = 0; //in kW

		if (mode != HeatMode.HEAT_OFF || mode != HeatMode.OFFLINE) {
			heatPumpedIn = heatGeneratedCache;
			//if (isGreenhouse) System.out.println(building.getNickName() + "'s heatPumpedIn : " + Math.round(heatPumpedIn*10_000D)/10_000D + " kW");
		}
		
		// (1b) CALCULATE HEAT GAIN BY PEOPLE
		double heatGainOccupants = HEAT_DISSIPATED_PER_PERSON * building.getNumPeople();
		// the energy required to heat up the in-rush of the new martian air
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s heatGainOccupants : " + Math.round(heatGainOccupants*10_000D)/10_000D + " kBTU/Hr");
		
		// (1c) CALCULATE HEAT GAIN BY EVA HEATER
		int num = building.numOfPeopleInAirLock(); // if num > 0, this building has an airlock
		
		double heatGainFromEVAHeater = 0;
		if (num > 0) 
			heatGainFromEVAHeater = building.getTotalPowerForEVA()/2D; 
		// divide by 2 since half of the time a person is doing ingress 
		// Note : Assuming EVA heater requires .5kW of power for heating up the air for each person in an airlock during EVA ingress.

		// (1d) CALCULATE SOLAR HEAT GAIN
		double I = surface.getSolarIrradiance(location) / 1000 ; 
		// Convert from W to kW
		double solarHeatGain =  0;

		if (isGreenhouse) {
			solarHeatGain =  I * transmittance_greenhouse * floorArea;
		}
		
		else if (isHallway) {
			solarHeatGain =  I * transmittance_window * floorArea / 2 * .5 * .5;
		}
		
		else {
			solarHeatGain =  I * transmittance_window * 4 * .5 * .5;
		}		
		
		// if temperature inside is too high, will automatically close the "blind" or "curtain" partially to block the 
		// excessive sunlight from coming in as a way of cooling off the building.
		if (t_in_C < t_initial + 2.0 * T_UPPER_SENSITIVITY) {
			solarHeatGain = solarHeatGain/2;
		}
		else if (t_in_C < t_initial + 3.0 * T_UPPER_SENSITIVITY) {
			solarHeatGain = solarHeatGain/3;
		}			
		else if (t_in_C < t_initial + 4.0 * T_UPPER_SENSITIVITY) {
			solarHeatGain = solarHeatGain/4;
		}			
		else if (t_in_C < t_initial + 5.0 * T_UPPER_SENSITIVITY) {
			solarHeatGain = solarHeatGain/5;
		}				
		else if (t_in_C < t_initial + 6.0 * T_UPPER_SENSITIVITY) {
			solarHeatGain = solarHeatGain/6;
		}				
		else if (t_in_C < t_initial + 7.0 * T_UPPER_SENSITIVITY) {
			solarHeatGain = solarHeatGain/7;
		}				
		else if (t_in_C < t_initial + 8.0 * T_UPPER_SENSITIVITY) {
			solarHeatGain = solarHeatGain/8;
		}				
		else
			solarHeatGain = solarHeatGain/9;
		
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
				if (t_in_C <= 23.25)
					canopyHeatGain *= .1; 
				else if (t_in_C <= 22.25)
					canopyHeatGain *= .2; 				
			}

			if (t_in_C <= 21.25)
				canopyHeatGain *= .3; 				

			else if (t_in_C <= 20.25)
				canopyHeatGain *= .35; 

			else if (t_in_C <= 19.25)
				canopyHeatGain *= .4; 

			else if (t_in_C <= 18.25)
				canopyHeatGain *= .45; 

			else if (t_in_C <= 17.25)
				canopyHeatGain *= .5; 
			
			else if (t_in_C <= 16.25)
				canopyHeatGain *= .55; 

			else if (t_in_C <= 15.25)
				canopyHeatGain *= .6; 

			else if (t_in_C <= 14.25)
				canopyHeatGain *= .65; 
			
			else if (t_in_C <= 13.25)
				canopyHeatGain *= .7; 
			
			else if (t_in_C <= 12.25)
				canopyHeatGain *= .75; 		

			else if (t_in_C <= 11.25)
				canopyHeatGain *= .8; 		

			else if (t_in_C <= 10.25)
				canopyHeatGain *= .85; 		

			else if (t_in_C <= 9.25)
				canopyHeatGain *= .9; 		

			else if (t_in_C <= 8.25)
				canopyHeatGain *= .95; 		

			else if (t_in_C <= 7.25)
				canopyHeatGain *= 1; 		

		}
			
		//if (isGreenhouse && solarHeatGain > 0) 
		//	System.out.println(building.getNickName() + "'s solarHeatGain : " 
		//			+ Math.round(solarHeatGain*10_000D)/10_000D + " kW");
		

		// (1g) CALCULATE HEAT GAIN DUE TO ARTIFICIAL LIGHTING
		double lightingGain = 0;
		
		if (isGreenhouse) {
			if (farm == null) { // greenhouse has a semi-transparent rooftop
				farm = building.getFarming();
			}
	
	        lightingGain = farm.getTotalLightingPower() * gain_factor_HPS; 
	        // For high pressure sodium lamp, assuming 60% are nonvisible radiation (energy loss as heat)
			//if (isGreenhouse) System.out.println(building.getNickName() + "'s lightingGain : " + Math.round(lightingGain*10_000D)/10_000D + " kW");
		}	
		
		// (1f) ADD HEAT GAIN BY EQUIPMENT
		// see heatGainEqiupment below
		
		// (1g) CALCULATE HEAT GAIN DUE TO VENTILATION
		double ventilationHeatGain = heatGainVentilation(t_in_C, delta_time); 
		
		// (1h) CALCULATE TOTAL HEAT GAIN 
		double heatGain = heatPumpedIn + heatGainOccupants + heatGainFromEVAHeater + solarHeatGain 
				+ canopyHeatGain + lightingGain + ventilationHeatGain + heatGainEqiupment; 		
		
//		if (isGreenhouse && heatGain > 0) 
//			System.out.println(building.getNickName() + "'s heatGain : " 
//					+ Math.round(heatGain*10_000D)/10_000D + " kW");
	
		// (2) CALCULATE HEAT LOSS
		
		// (2a) CALCULATE HEAT NEEDED FOR REHEATING AIRLOCK
		
		double heatAirlock = 0;
		// the energy loss due to gushing out the warm settlement air when airlock is open to the cold Martian air
		
		if (num > 0 && hasHeatDumpViaAirlockOuterDoor) {
			heatAirlock = energy_factor_EVA * (DEFAULT_ROOM_TEMPERATURE - t_out_C) * num ;
			// flag that this calculation is done till the next time when the airlock is depressurized.
			hasHeatDumpViaAirlockOuterDoor = false;
		}
		
//		if (isGreenhouse) 
//			System.out.println(building.getNickName() + "'s heatAirlock : " 
//					+ Math.round(heatAirlock/1000D*10_000D)/10_000D + " kW");


		// (2b) CALCULATE HEAT LOSS DUE TO STRUCTURE		
		double structuralLoss = 0;
	
		if (num > 0) {
			structuralLoss = CLF * - d_t
					* (U_value_area_ceiling_or_floor * 2D
					+ U_value_area_wall
					+ U_value_area_crack_length_for_airlock * weather.getWindSpeed(location)) / 1000;
					// Note : 1 m/s = 3.28084 ft/s = 2.23694 miles per hour
		}
		else {
			if (isGreenhouse) {
				structuralLoss = CLF * - d_t
						* (U_value_area_ceiling_or_floor
						//+ U_value_area_wall
						+ U_value_area_crack_length * weather.getWindSpeed(location))/ 1000;		
			}
			else {
				structuralLoss = CLF * - d_t
					* (U_value_area_ceiling_or_floor * 2D
					+ U_value_area_wall
					+ U_value_area_crack_length * weather.getWindSpeed(location))/ 1000;
			}
		}	
		
		// Note : U_value in kW/K/m2, not [Btu/°F/ft2/hr]
		
//		if (isGreenhouse) 
//			System.out.println(building.getNickName() + "'s structuralLoss : " 
//					+ Math.round(structuralLoss*10_000D)/10_000D + " kW");

		// (2c) CALCULATE HEAT LOSS DUE TO VENTILATION
		// heatLossFromVent can be smaller than zero
		double ventilationHeatLoss = heatLossFromVent;
		// reset heatExtracted to zero
		heatLossFromVent = 0;
		
//		if (isGreenhouse) 
//			System.out.println(building.getNickName() + "'s ventilationHeatLoss : " 
//					+ Math.round(ventilationHeatLoss*10_000D)/10_000D + " kW");
		
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
					* ( Math.pow(t_in_K, 4) - Math.pow(t_out_K, 4) ) * hullArea / 1000D;
		}
		else {
			solarHeatLoss = 0;
		}		
		
		
//		if (isGreenhouse) 
//			System.out.println(building.getNickName() + "'s solarHeatLoss : " 
//				+ Math.round(solarHeatLoss*100D)/100D + " kW");

		// (2e) At high RH, the air has close to the maximum water vapor that it can hold, 
		// so evaporation, and therefore heat loss, is decreased.
		
		// (2f) CALCULATE TOTAL HEAT LOSS	
		double heatLoss = heatAirlock + structuralLoss + ventilationHeatLoss + solarHeatLoss;
		
//		if (isGreenhouse) 
//			System.out.println(building.getNickName() + "'s heatLoss : " 
//					+ Math.round(heatLoss*10_000D)/10_000D + " kW");

		// (3) CALCULATE THE INSTANTANEOUS CHANGE OF TEMPERATURE (DELTA T)
		// delta t = (heatGain - heatLoss) / (time_interval * C_s * mass) ;
		
		// (3a) FIND THE DIFFERENCE between heat gain and heat loss
		double d_heat1 = heatGain - heatLoss;
			
		// (3b) FIND the CONVERSION FACTOR
		double c_factor = 1 / (conversion_factor * delta_time); 
		//if (isGreenhouse) 
		//	System.out.println(building.getNickName() + "'s c_factor : " 
		//			+ Math.round(c_factor*10000D)/10000D);

		// (3c) USE HEAT SINK to reduce the value of d_heat
		double d_heat2 = 0;//d_heat1;
		
		if (t_in_C <= t_initial - 2 * T_LOWER_SENSITIVITY
				|| t_in_C >= t_initial + 2 * T_UPPER_SENSITIVITY)	
			d_heat2 = computeHeatSink(d_heat1, c_factor);
		else
			d_heat2 = d_heat1;

		//if (isGreenhouse) 
		//	System.out.println(building.getNickName() + "'s d_heat : " 
		//			+ Math.round(d_heat1*100D)/100D + " --> " + Math.round(d_heat2*100D)/100D);
	
		// (3d) FIND THE CHANGE OF TEMPERATURE (in degress celsius)  
		// Using the equation : heat = mass * specific heat capacity * delta temperature
		// d_t = d_heat / mass / C_s
		double d_t_C = c_factor * d_heat2 ; 
		//applyHeatBuffer(changeOfTinC);
		//if (isGreenhouse) 
		//	System.out.println(building.getNickName() + "'s d_t_C : " 
		//			+ Math.round(d_t_C*100D)/100D);
		return d_t_C;
	}

	/***
	 * Computes the amount of heat absorbed or release by the heat sink 
	 * @param delta_heat
	 * @param c_factor
	 * @return heat
	 */
	private double computeHeatSink(double delta_heat, double c_factor) {
		double d_heat = delta_heat; 
		// (3c0 FIND THE HEAT TO BE ABSORBED OR RELEASED BY THE HEAT SINK
		double limit = 1/c_factor; // = 210.9404 or 213.4221 at the start of the sim
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s limit : " + Math.round(limit*10000D)/10000D);
			
		double efficiency = 1;
		double transfer = 0;

		if (heat_sink <= 5*floorArea) { // max out in absorbing heat
			efficiency = 1;
		}
		else if (heat_sink <= 10*floorArea) { // max out in absorbing heat
			efficiency = .9;
		}
		else if (heat_sink <= 15*floorArea) { // max out in absorbing heat
			efficiency = .8;
		}			
		else if (heat_sink <= 20*floorArea) { // max out in absorbing heat
			efficiency = .7;
		}	
		else if (heat_sink <= 25*floorArea) { // max out in absorbing heat
			efficiency = .6;
		}	
		else if (heat_sink <= 30*floorArea) { // max out in absorbing heat
			efficiency = .5;
		}	
		else if (heat_sink <= 35*floorArea) { // max out in absorbing heat
			efficiency = .4;
		}	
		else if (heat_sink <= 40*floorArea) { // max out in absorbing heat
			efficiency = .3;
		}	
		else if (heat_sink <= 45*floorArea) { // max out in absorbing heat
			efficiency = .2;
		}	
		else if (heat_sink <= 50*floorArea) { // max out in absorbing heat
			efficiency = .1;
		}	
		else {
			efficiency = 0;
		}
		
		if (d_heat > 0) {
					
			if (d_heat/limit > 3) {				// e.g. 7 > 5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [0] ");
				transfer = .75 * d_heat * efficiency;		// d = 7 - 5 = 2				
			}
			else if (d_heat/limit > 2.5) {				// e.g. 7 > 5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [1] ");
				transfer = .6 * d_heat * efficiency;		// d = 7 - 5 = 2				
			}

			else if (d_heat/limit > 2) {				// e.g. 7 > 5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [1] ");
				transfer = .45 * d_heat * efficiency;		// d = 7 - 5 = 2				
			}
			else if (d_heat/limit > 1.5 ) {		// e.g. 4 > .5 * 5 
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [2] ");
				transfer = .3 * d_heat * efficiency;	// d = 4 - .5 * 5 = 4 - 2.5 = 1.5			
			}
			else if (d_heat/limit > 1 ) {		// e.g. 4 > .5 * 5 
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [2] ");
				transfer = .15 * d_heat * efficiency;	// d = 4 - .5 * 5 = 4 - 2.5 = 1.5			
			}
			else {
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [3] ");
				transfer = .1 * d_heat * efficiency;	// d = 2 - .25 * 5 = 2 - 1.25 = 0.75			
			}
			
			// suck up heat
			heat_sink += transfer;						
			d_heat -= transfer;	

		}
		
		else if (d_heat < 0) {
			double delta = Math.abs(d_heat);
			if (delta/limit > 3) { 			// e.g. -7 < -5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [4] ");
				transfer = .75 * d_heat * efficiency;	// d = -7 + 5 = -2; d is negative
			}
			else if (delta/limit > 2.5) { 			// e.g. -7 < -5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [5] ");
				transfer = .6 * d_heat * efficiency;	// d = -7 + 5 = -2; d is negative
			}
			else if (delta/limit > 2) { 			// e.g. -7 < -5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [5] ");
				transfer = .5 * d_heat * efficiency;	// d = -7 + 5 = -2; d is negative
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
				transfer = .15 * d_heat * efficiency; // d = -2 + .25 * 5 = -2 + 1.25 = -0.75; d is -ve
			}
			
			//if (heat_sink >= -50*floorArea) { // max out in releasing heat
				// release heat
			//	heat_sink += transfer;  
			//	d_heat -= transfer; 	
			//}

		}
		
		//if (isGreenhouse)
		//	System.out.println(building.getNickName() +"'s Heat Sink has " + Math.round(heat_sink*10.0)/10.0 + " kW"
		//			+ "    Transferred : " + Math.round(transfer*10.0)/10.0 + " kW");
		
		return d_heat;
	}

	/**
	 * Computes heat gain from adjacent room(s) due to air ventilation. This helps the temperature equilibrium
	 * @param t temperature
	 * @return temperature
	 */
	private double heatGainVentilation(double t, double time) {
		double total_gain = 0; //heat_dump_1 = 0 , heat_dump_2 = 0;
		boolean tooLow = t < (t_initial - 3.0 * T_LOWER_SENSITIVITY);
		boolean tooHigh = t > (t_initial + 3.0 * T_UPPER_SENSITIVITY);
		double speed_factor = .01 * time * CFM;
		
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
				double t_next = adjacentBuildings.get(i).getCurrentTemperature();
				double t_i = adjacentBuildings.get(i).getInitialTemperature();

				boolean too_low_next = t_next < (t_i - 2.5 * T_LOWER_SENSITIVITY);
				boolean too_high_next = t_next > (t_i + 2.5 * T_UPPER_SENSITIVITY);
				
				double d_t = Math.abs(t - t_next);

				double gain = 0;
				if (tooLow) {
					if (too_high_next) {
						if (t_next > t) {
							// heat coming in
							gain = 2D * speed_factor * d_t*area_factor;
							gain = Math.min(gain, CFM/size*2D*area_factor);
						}
						else {
							// heat coming in
							gain = speed_factor * d_t*area_factor;
							gain = Math.min(gain, CFM/size*area_factor);
							// heat is leaving
						//	gain = - 2D * speed_factor * d_t;
						//	gain = Math.max(gain, -CFM/size*2D);
						}
					}
					else if (!too_low_next) {
						if (t_next > t) {
							// heat coming in
							gain = speed_factor * d_t*area_factor;
							gain = Math.min(gain, CFM/size*2D);
						}
						else {
							// heat coming in
							gain = .5 *speed_factor * d_t*area_factor;
							gain = Math.min(gain, CFM/size);
						}
						//else if (t_next - T_LOWER_SENSITIVITY < t) {
							// heat is leaving
						//	gain = -speed_factor * d_t;
						//	gain = Math.max(gain, -CFM/size*2D);
						//}
					}
				}
				
				else if (tooHigh) {
					if (too_low_next) {
						if (t > t_next) {
							// heat is leaving
							gain = -2D *speed_factor * d_t*area_factor;
							gain = Math.max(gain, -CFM/size*2D*area_factor);
						}
						else {
							// heat is leaving
							gain = -speed_factor * d_t*area_factor;
							gain = Math.max(gain, -CFM/size*area_factor);
						}
						//else if (t < t_next + T_LOWER_SENSITIVITY) {
							// heat coming in
						//	gain = 2D *speed_factor * d_t;
						//	gain = Math.min(gain, CFM/size*2D);
						//}
					}
					else if (!too_high_next) {
						if (t > t_next) {
							// heat is leaving
							gain = -speed_factor * d_t*area_factor;
							gain = Math.max(gain, -CFM/size*2D);
						}
						else {
							// heat is leaving
							gain = -.5 * speed_factor * d_t*area_factor;
							gain = Math.max(gain, -CFM/size);
						}
						//else if (t < t_next + T_LOWER_SENSITIVITY) {
							// heat coming in
						//	gain = speed_factor * d_t;
						//	gain = Math.min(gain, CFM/size*2D);
						//}
					}
					
				}
				
				adjacentBuildings.get(i).extractHeat(gain);
				
				total_gain += gain;

			}
		}
		
		return total_gain;
	}


	/**
	 * Gets the value of the function for a named building.
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
	 * via 3 steps (this method houses the main thermal control codes)
	 * @param delta_time in millisols
	 */
	private void cycleThermalControl(double delta_time) {
		// Detect temperatures
		double old_t = currentTemperature;
		double new_t = 0;
		double t_out = building.getSettlement().getOutsideTemperature();

		if (mass == 0) {
			mass = building.getLifeSupport().getAir().getTotalMass();
			conversion_factor = C_s * mass / timeSlice; 
		}
		
		// STEP 1 : CALCULATE HEAT GAIN/LOSS AND RELATE IT TO THE TEMPERATURE CHANGE
		double dt = 0;
		if (conversion_factor != 0)
			dt = determineDeltaTemperature(old_t, delta_time);
			
		// STEP 2 : LIMIT THE TEMPERATURE CHANGE
		// Limit any spurious change of temperature for the sake of stability 
		if (old_t < t_initial + 5.0 * T_LOWER_SENSITIVITY) {
			if (dt < -5)
				dt = -5;
		}
		else if (old_t > t_initial + 5.0 * T_UPPER_SENSITIVITY) {
			if (dt > 5)
				dt = 5;			
		}		

		//System.out.println(building.getNickName() + "'s dt = " + Math.round(dt*10.0)/10.0);
		// Limit the current temperature
		new_t = old_t + dt;
		// Safeguard against anomalous dt that would have crashed mars-sim
		if (new_t > 45)
			new_t = 45;
		else if (new_t < t_out)
			new_t = t_out;
		
		// Stabilize the temperature by delaying the change
		double t = 0;
		int size = temperatureCache.length;
		for (int i=0; i<size; i++) {
			t += temperatureCache[i];
		}

		currentTemperature = (t + old_t + new_t) / (size + 2);
		
		for (int i=1; i<size-1; i++) {
			temperatureCache[i] = temperatureCache[i-1];
		}
		temperatureCache[0] = old_t;
		

		
		// STEP 3 : CHANGE THE HEAT MODE
		// Turn heat source off if reaching certain temperature thresholds
		adjustHeatMode();
		
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return powerRequired;
	}

	/**
	 * Sets the power required for heating
	 * @param power
	 */
	public void setPowerRequired(double power) {
		powerRequired = power;
	}

	/**
	 * Gets the heat this building currently required.
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
	 * Sets the amount of heat loss from ventilation for this building
	 * Note : heat loss if negative. heat gain if positive
	 * @param heatLoss removed or added
	 */
	public void setHeatLoss(double heatLoss) {
		heatLossFromVent = heatLossFromVent + heatLoss;
	}

	/**
	 * Flags the presence of the heat loss due to opening an airlock outer door
	 * @param value
	 */
	public void flagHeatLostViaAirlockOuterDoor(boolean value) {
		hasHeatDumpViaAirlockOuterDoor = value;
	}

	
	@Override
	public void destroy() {
		super.destroy();
		location = null;
		farm = null;
		adjacentBuildings = null;
	}

}
