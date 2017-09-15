/**
 * Mars Simulation Project
 * Heating.java
 * @version 3.1.0 2017-09-14
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * The Heating class is a building function for regulating temperature in a settlement..
 */
public class Heating
extends Function
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
    /* default logger.*/
 	private static Logger logger = Logger.getLogger(Heating.class.getName());

	private static String sourceName = logger.getName();
    
	/** default logger. */
	//private static Logger logger = Logger.getLogger(Heating.class.getName());
	private static final FunctionType FUNCTION = FunctionType.LIFE_SUPPORT;

	// Heat gain and heat loss calculation
	// Source 1: Engineering concepts for Inflatable Mars Surface Greenhouses
	// http://ntrs.nasa.gov/search.jsp?R=20050193847
	// Full ver at http://www.marshome.org/files2/Hublitz2.pdf
	// Revised ver at https://www.researchgate.net/publication/7890528_Engineering_concepts_for_inflatable_Mars_surface_greenhouses

	// Data members
	private static final double DEFAULT_ROOM_TEMPERATURE = 22.5;
	private static final double kBTU_PER_HOUR_PER_kW = 3.41214; // 1 kW = 3412.14 BTU/hr
	private static final double C_TO_K = 273.15;
	private static final double TRANSMITTANCE_GREENHOUSE_HIGH_PRESSURE = .55 ;
	private static final double EMISSIVITY_DAY = 0.8 ;
	private static final double EMISSIVITY_NIGHT = 1.0 ;
	private static final double EMISSIVITY_INSULATED = 0.05 ;
	private static final double STEFAN_BOLTZMANN_CONSTANT = 0.0000000567 ; // in W / (m^2 K^4)

	private static final double INSULATION_BLANKET = 100D;
	private static final double INSULATION_CANOPY = 200D;
	private static final double HALLWAY_INSULATION = 50D;
	
    // Thermostat's temperature allowance
    private static final double T_UPPER_SENSITIVITY = 1D;
    private static final double T_LOWER_SENSITIVITY = 1D;

    private static final double kBTU_HEAT_DISSIPATED_PER_PERSON = .35D;
    
    private static final double MSOL_LIMIT = .9;
    
     //private static final double kPASCAL_PER_ATM = 1D/0.00986923267 ; // 1 kilopascal = 0.00986923267 atm
    //private static final double R_GAS_CONSTANT = 8.31441; //R = 8.31441 m3 Pa K−1 mol−1
	// 1 kilopascal = 0.00986923267 atm
	// 1 cubic ft = L * 0.035315
    // A full scale pressurized Mars rover prototype may have an airlock volume of 5.7 m^3
	
	private static final double HEIGHT = 2.5; // in meter
	
	/** The speed of the ventilation fan */
	private static final double CFM  = 50;
	
	/** The average volume of a airlock [m^3] */	
    private static double AIRLOCK_VOLUME_IN_CM = Building.AIRLOCK_VOLUME_IN_CM; // = 12 [in m^3]
    /**  convert meters to feet  */
	private static final double M_TO_FT = 3.2808399;//10.764;
	/**  Specific Heat Capacity = 4.0 for a typical U.S. house */
	private static final double SHC = 6.0; // [in BTU / sq ft / °F]

	// Building Loss Coefficient (BLC) is 1.0 for a typical U.S. house 
	//private static double BLC = 0.2;

	/** 
	 * Cooling Load Factor accounts for the fact that building thermal mass creates a time lag between 
	 * heat generation from internal sources and the corresponding cooling load. 
	 * It is equals to Sensible Cooling Load divided by Sensible Heat Gain
	 */
	private static final double CLF = 2D;
	
	/** 
	 * The U-value is the thermal transmittance (reciprocal of R-value)
	 * Note that Heat Loss = (1/R-value)(surface area)(∆T) 
	 * */
	private static double U_value = 0.07; // = 1/R_value; 
	// U values are in [Btu/°F/ft2/hr]
	// or = 0.3975 [Watts/m^2/°K] (by multiplying by 5.678)

	/**  R-value is a measure of thermal resistance, or ability of heat to transfer from hot to cold, through materials such as insulation */
	//private static final double R_value = 30;
	
    private static double U_value_area_crack_length, U_value_area_crack_length_for_airlock;
    
    // Note : U_value will be converted to metric units in W/m²K. 
    // see at https://www.thenbs.com/knowledge/what-is-a-u-value-heat-loss-thermal-mass-and-online-calculators-explained
    
    private static double q_H_factor = 21.4D/10;///2.23694; // 1 m per sec = 2.23694 miles per hours
    
    private static double airChangePerHr = .5;
    
    // Molar mass of CO2 = 44.0095 g/mol
    // average density of air : 0.020 kg/m3
	//double n = weather.getAirDensity(coordinates) * vol / 44D;
	//private double n_CO2 = .02D * VOLUME_OF_AIRLOCK / 44*1000;
	// 1 cubic feet of air has a total weight of 38.76 g
	//private double n_air = 1D;
	//private double n_sum = n_CO2 + n_air;
    
    //private static final int HEAT_CAP = 200;  
 	private static final int PER_UPDATE = 2 ; // must be a multiple of 2
 	
     
	private int counts;

    /** the heat gain from equipment in BTU per hour */
    private double heatGainEqiupment;// = 2000D;
	/** specific heat capacity of air at 300K [kJ/kg*K]*/	 
	private double C_p = 1.005;  
	/** Density of dry breathable air [kg/m3] */	
	private double dryAirDensity = 1.275D; //
	/** Factor for calculating airlock heat loss during EVA egress */
	private double energy_factor_EVA = C_p * AIRLOCK_VOLUME_IN_CM * dryAirDensity /1000; 

    private double width;
    
	private double length;
	
	private double floorArea;
	
	private double hullArea; // underbody and sidewall
	
	private double transmittance_window;

	private double transmittance_greenhouse;
	
	private double gain_factor_HPS = Crop.LOSS_FACTOR_HPS;

	private double powerRequired = 0;
	
	private double basePowerDownHeatRequirement = 0;

	private double SHC_area;

	// Thermal Transmittance 
    private double U_value_area_ceiling_or_floor;
    
    private double U_value_area_wall;

	private double heatGeneratedCache = 0; // the initial value is zero

	/** The heat extracted by the ventilation system */
	private double heatLossFromVent;
	/** The current temperature of this building */
	private double currentTemperature;

	private double elapsedTimeinHrs; // = ONE_TENTH_MILLISOLS_PER_UPDATE / 10D /1000D * 24D;
	
	private double t_initial;
	
	private double emissivity;
	
	private double conversion_factor;
	
	private double heat_sink = 0;
	
	private double area_factor;
	
	//private double timeCache;
	
	
	/** is this building a greenhouse */
	private boolean isGreenhouse = false;
	/** is this building a hallway or tunnel */
	private boolean isHallway = false;
	/** Is the airlock door open */
	private boolean hasHeatDumpViaAirlockOuterDoor = false;
	/** THe emissivity of the greenhouse canopy per millisol */
	private static Map<Integer, Double> emissivityMap;
	/** */
  	private String buildingType;

  	//private ThermalGeneration furnace;
 	//private ThermalSystem thermalSystem;
	private Building building;
	private Weather weather;
	private Coordinates location;
	private MasterClock masterClock;
	private MarsClock marsClock;
	private SurfaceFeatures surfaceFeatures;
	private Settlement settlement;
	private BuildingManager manager;
	private Farming farm;

	private List<Building> adjacentBuildings;
	
	/**
	 * Constructor.
	 * @param building the building this function is for.
	 */
	public Heating(Building building) {
		// Call Function constructor.
		super(FUNCTION, building);

        sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());
        
		this.building = building;
		this.manager = building.getBuildingManager();
		this.settlement = manager.getSettlement();

		buildingType =  building.getBuildingType();
		
		masterClock = Simulation.instance().getMasterClock();
		marsClock = masterClock.getMarsClock();
		weather = Simulation.instance().getMars().getWeather();
		//coordinates = building.getBuildingManager().getSettlement().getCoordinates();
		location = building.getLocation();
		//thermalSystem = settlement.getThermalSystem();
		if (surfaceFeatures == null)
			surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();

		length = building.getLength();
		width = building.getWidth() ;

		floorArea = length * width ;

		area_factor = Math.sqrt(Math.sqrt(floorArea));
		
		if (isHallway()) {
			isHallway = true;
			heatGainEqiupment = 40D;
		}
		else if (isGreenhouse()) {
			isGreenhouse = true;
			heatGainEqiupment = 400D;
		}
		else if (buildingType.toLowerCase().contains("lander")) {
			heatGainEqiupment = 2000D;
		}
		else if (isHab()) {
			heatGainEqiupment = 800D;
		}
		else if (buildingType.toLowerCase().contains("command")) {
			heatGainEqiupment = 1500D;
		}
		else if (buildingType.toLowerCase().contains("work")||buildingType.toLowerCase().contains("manu")) {
			heatGainEqiupment = 1500D;
		}
		else if (buildingType.toLowerCase().contains("lounge")) {
			heatGainEqiupment = 2400D;
		}
		else if (buildingType.toLowerCase().contains("outpost")) {
			heatGainEqiupment = 1500D;
		}
		else {
			heatGainEqiupment = 300D;			
		}
		
		if (isGreenhouse) { // greenhouse has a semi-transparent rooftop
			//farm = (Farming) building.getFunction(BuildingFunction.FARMING); // NullPointerException
			hullArea = (width + length) * HEIGHT * 2D ; // + floorArea // ceiling & floor not included since rooftop is transparent
			transmittance_greenhouse = TRANSMITTANCE_GREENHOUSE_HIGH_PRESSURE;
		}
		else {
			transmittance_window = 0.75;
			//hullArea = 2D * floorArea + (width + length) * HEIGHT * 2D ; // ceiling included
		}
	

		U_value_area_wall = U_value * 2D * (width + length) * M_TO_FT * (HEIGHT * M_TO_FT);
		U_value_area_ceiling_or_floor = U_value * (floorArea * M_TO_FT * M_TO_FT);
		

		//System.out.println(building.getNickName() + "'s U_value_area_ceiling_or_floor : "+ U_value_area_ceiling_or_floor);
		//System.out.println(building.getNickName() + "'s U_value_area_wall : "+ U_value_area_wall);

		// assuming airChangePerHr = .5 and q_H = 21.4;
		U_value_area_crack_length = 0.244 * .075 * airChangePerHr * q_H_factor * (4 * (.5 + .5) );
		// assuming four windows
		U_value_area_crack_length_for_airlock = 0.244 * .075 * airChangePerHr * q_H_factor * ( 2 * (2 + 6) + 4 * (.5 + .5) );
		//assuming two EVA airlock
		
		//System.out.println(building.getNickName() + "'s U_value_area_crack_length : "+ U_value_area_crack_length);
		//System.out.println(building.getNickName() + "'s U_value_area_crack_length_for_airlock : "+ U_value_area_crack_length_for_airlock);

		SHC_area = floorArea * M_TO_FT * M_TO_FT * SHC ;

		elapsedTimeinHrs = PER_UPDATE * 24D / 1000D;// [ hr/millisol] ONE_TENTH_MILLISOLS_PER_UPDATE / 10D /1000D * 24D;
		
		conversion_factor = elapsedTimeinHrs /  1.8  * 1000D  / SHC_area ; 
		
		t_initial = building.getInitialTemperature();
		currentTemperature = t_initial;

		//t_factor = vol / R_GAS_CONSTANT / n;

		emissivityMap = new ConcurrentHashMap<>();

		for (int i = 0; i <= 1000; i++) {
			// assuming the value of emissivity fluctuates as a cosine waveform between 0.8 (day) and 1.0ss (night)
			emissivity = .1D * Math.cos(i/500D* Math.PI) + (EMISSIVITY_NIGHT + EMISSIVITY_DAY)/2D;
			//System.out.println( i + " : " + emissivity);
			emissivityMap.put(i, emissivity);
		}

		
	}



	/**
     * Is this building a hallway or tunnel.
     * @return true or false
     */
    public boolean isHallway() {
    	return buildingType.toLowerCase().contains("hallway") || buildingType.toLowerCase().contains("tunnel");
    }

	/**
     * Is this building a large greenhouse.
     * @return true or false
     */
    public boolean isLargeGreenhouse() {
		return buildingType.toLowerCase().contains("large greenhouse");
    }
    
	/**
     * Is this building a greenhouse.
     * @return true or false
     */
    public boolean isGreenhouse() {
		return buildingType.toLowerCase().contains("greenhouse");
    }

	/**
     * Is this building a loading dock garage.
     * @return true or false
     */
    public boolean isLoadingDockGarage() {
		return buildingType.toLowerCase().equalsIgnoreCase("loading dock garage");
    }

	/**
     * Is this building a loading dock garage.
     * @return true or false
     */
    public boolean isGarage() {
		return buildingType.toLowerCase().equalsIgnoreCase("garage");
    }
    
	/**
     * Is this building a type of (retrofit) hab.
     * @return true or false
     */
    public boolean isHab() {
		return buildingType.toLowerCase().contains("hab");
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
	public void adjustHeatMode() {
		double t_now = currentTemperature;
		//if (building.getPowerMode() == PowerMode.FULL_POWER) {
			// ALLOWED_TEMP is thermostat's allowance temperature setting
		    // If T_NOW deg above INITIAL_TEMP, turn off furnace
			if (t_now > t_initial - 0.5 * T_LOWER_SENSITIVITY) {
				building.setHeatMode(HeatMode.HEAT_OFF);
			}
			//else if (t_now >= t_initial - .5 * T_LOWER_SENSITIVITY) {// + T_UPPER_SENSITIVITY) {
			//	building.setHeatMode(HeatMode.HEAT_OFF);
			//}
			else if (t_now >= t_initial - 1.0 * T_LOWER_SENSITIVITY) {
				building.setHeatMode(HeatMode.QUARTER_HEAT);
			}
			else if (t_now >= t_initial - 2.5 * T_LOWER_SENSITIVITY) {
				building.setHeatMode(HeatMode.HALF_HEAT);
			}
			else if (t_now >= t_initial - 5.0 * T_LOWER_SENSITIVITY) {
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
	 * @param millisols
	 * @return delta temperature
	 */
	public double determineDeltaTemperature(double t_in_C, double millisols) {

		// THIS IS A THREE-PART CALCULATION
		double t_out_C = settlement.getOutsideTemperature();
		// heatGain and heatLoss are to be converted from kJ to BTU below
		double d_t =  (t_in_C - t_out_C) * 1.8; //1.8 =  9D / 5D;
		double t_in_K = t_in_C + C_TO_K;
		double t_out_K = t_out_C + C_TO_K;
		
		//°C  x  9/5 + 32 = °F
		//(°F - 32)  x  5/9 = °C
		
		// (1) CALCULATE HEAT GAIN
		HeatMode mode = building.getHeatMode();
		
		// (1a) CALCULATE HEAT GENERATED BY DIRECTING HEATING FROM THE LAS INTERVAL OF TIME
		double heatPumpedIn = 0; //in kW
		// Note: 1 kW = 3412.14 BTU/hr

		if (mode != HeatMode.HEAT_OFF || mode != HeatMode.OFFLINE) {
			heatPumpedIn = heatGeneratedCache;
			//if (isGreenhouse) System.out.println(building.getNickName() + "'s heatPumpedIn : " + Math.round(heatPumpedIn*10_000D)/10_000D + " kW");
		}
		
		// (1b) CALCULATE HEAT GAIN BY PEOPLE
		// each person emits about 350 BTU/hr
		double heatGainOccupants = kBTU_HEAT_DISSIPATED_PER_PERSON * building.getInhabitants().size() ;
		// the energy required to heat up the in-rush of the new martian air
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s heatGainOccupants : " + Math.round(heatGainOccupants*10_000D)/10_000D + " kBTU/Hr");
		
		// (1c) CALCULATE HEAT GAIN BY EVA HEATER
		int num = building.numOfPeopleInAirLock(); // if num > 0, this building has an airlock
		
		double heatGainFromEVAHeater = 0;
		if (num > 0) heatGainFromEVAHeater = building.getTotalPowerForEVA()/2D; 
		// divide by 2 since half of the time a person is doing ingress 
		// Note : Assuming EVA heater requires .5kW of power for heating up the air for each person in an airlock during EVA ingress.

		// (1d) CALCULATE SOLAR HEAT GAIN
		if (surfaceFeatures == null)
			surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();

		double I = surfaceFeatures.getSolarIrradiance(location);
		double solarHeatGain =  0;

		if (isGreenhouse) {
			solarHeatGain +=  I * transmittance_greenhouse * floorArea /1000D;
			
			if (t_in_C <= 12 && I < 17)
				solarHeatGain += .5 * INSULATION_CANOPY; 

			else if (t_in_C <= 12 && I < 12)
				solarHeatGain += .75 * INSULATION_CANOPY; 
			
			else if (t_in_C <= 12 && I < 7)
				solarHeatGain += INSULATION_CANOPY; 		
				//System.out.println(building.getNickName() + " is using insulation canopy over the roof to regain " 
				//		+ Math.round(solarHeatGain*100D)/100D + " kW of heat.");
			
		}
		else if (isHallway) {
			solarHeatGain +=  I * transmittance_window * 4 * .5 * .5/1000D;
			
			if (I < 25 && t_in_C < 15) 
				solarHeatGain += .5 * HALLWAY_INSULATION ; 

			else if (I < 25 && t_in_C < 10)
				solarHeatGain += .75* HALLWAY_INSULATION ; 

			else if (I < 25 && t_in_C < 5)
				solarHeatGain += HALLWAY_INSULATION ; 
		}	
		else {
			solarHeatGain +=  I * transmittance_window * 4 * .5 * .5/1000D;
			
			if (I < 25 && t_in_C < 15) 
				solarHeatGain += .5 * INSULATION_BLANKET ; 

			else if (I < 25 && t_in_C < 10)
				solarHeatGain += .75* INSULATION_BLANKET ; 

			else if (I < 25 && t_in_C < 5)
				solarHeatGain += INSULATION_BLANKET ; 
		
			//System.out.println(building.getNickName() + " is using insulation blanket around windows to regain " 
			//		+ Math.round(solarHeatGain*100D)/100D + " kW of heat.");
			
		}		
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s solarHeatGain : " + Math.round(solarHeatGain*10_000D)/10_000D + " kW");
		
		// (1e) ADD HEAT GAIN BY EQUIPMENT
		// see heatGainEqiupment below
		
		// (1f) CALCULATE HEAT GAIN DUE TO ARTIFICIAL LIGHTING
		double lightingGain = 0;
		
		if (isGreenhouse) {
			if (farm == null) { // greenhouse has a semi-transparent rooftop
				farm = building.getFarming();//(Farming) building.getFunction(BuildingFunction.FARMING);
			}
	
	        lightingGain = farm.getTotalLightingPower() * gain_factor_HPS; // For high pressure sodium lamp, assuming 60% are nonvisible radiation (energy loss as heat)
			//if (isGreenhouse) System.out.println(building.getNickName() + "'s lightingGain : " + Math.round(lightingGain*10_000D)/10_000D + " kW");
		}	
		
		// (1e) CALCULATE TOTAL HEAT GAIN 
		double heatGain = kBTU_PER_HOUR_PER_kW * (heatGainFromEVAHeater + heatPumpedIn + solarHeatGain + lightingGain)  // in kBTU/hr
				+ heatGainOccupants // in kBTU/hr
				+ heatGainEqiupment/1000D; // in kBTU/hr		
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s heatGain : " + Math.round(heatGain*10_000D)/10_000D + " kBTU/Hr");
	
		// (2) CALCULATE HEAT LOSS
		
		// (2a) CALCULATE HEAT NEEDED FOR REHEATING AIRLOCK
		
		double heatAirlock = 0;
		// the energy loss due to gushing out the warm settlement air when airlock is open to the cold Martian air
		
		if (num > 0 && hasHeatDumpViaAirlockOuterDoor) {
			heatAirlock = energy_factor_EVA * (DEFAULT_ROOM_TEMPERATURE - t_out_C) * num ;
			hasHeatDumpViaAirlockOuterDoor = false;
		}

		// (2b) CALCULATE HEAT LOSS DUE TO STRUCTURE		
		double structuralLoss = 0;
	
		if (num > 0) {
			structuralLoss = CLF * d_t / 1000D // [in kBTU/hr]
					* (U_value_area_ceiling_or_floor * 2D
					+ U_value_area_wall
					+ U_value_area_crack_length_for_airlock * weather.getWindSpeed(location));// * M_TO_FT * 3600D);
					// Note : 1 m/s = 3.28084 ft/s = 2.23694 miles per hour
		}
		else {
			if (isGreenhouse) {
				structuralLoss = CLF *d_t / 1000D // [in kBTU/hr]
						* (U_value_area_ceiling_or_floor
						//+ U_value_area_wall
						+ U_value_area_crack_length * weather.getWindSpeed(location));// * M_TO_FT * 3600D);;			
			}
			else {
				structuralLoss = CLF * d_t / 1000D // [in kBTU/hr]
					* (U_value_area_ceiling_or_floor * 2D
					+ U_value_area_wall
					+ U_value_area_crack_length * weather.getWindSpeed(location));// * M_TO_FT * 3600D);
			}
		}	
		// Note : U_value in [Btu/°F/ft2/hr]
		
		//if (isGreenhouse) 
		//System.out.println(building.getNickName() + "'s structuralLoss/1000D : " + Math.round(structuralLoss/1000D*10_000D)/10_000D + " kBTU/Hr");

		// (2c) CALCULATE HEAT LOSS DUE TO VENTILATION
		double ventilationHeatLoss = heatLossFromVent - heatGainVentilation(t_in_C, millisols); 
		// reset heatExtracted to zero
		heatLossFromVent = 0;
		
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s ventilationHeatLoss : " + Math.round(ventilationHeatLoss*10_000D)/10_000D + " kBTU/Hr");
		
		// (2d) CALCULATE HEAT LOSS DUE TO HEAT RADIATED BACK TO OUTSIDE
		double solarHeatLoss =  0;

		if (isGreenhouse) {
			solarHeatLoss = emissivity * STEFAN_BOLTZMANN_CONSTANT
					* ( Math.pow(t_in_K, 4) - Math.pow(t_out_K, 4) ) * hullArea /1000D;
		}
		else {
			solarHeatLoss = emissivity * STEFAN_BOLTZMANN_CONSTANT
					* ( Math.pow(t_in_K, 4) - Math.pow(t_out_K, 4) ) * hullArea /1000D;
		}		
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s solarHeatLoss : " + Math.round(solarHeatLoss*10_000D)/10_000D + " kW");

		// (2e) At high RH, the air has close to the maximum water vapor that it can hold, 
		// so evaporation, and therefore heat loss, is decreased.
		
		// (2f) CALCULATE TOTAL HEAT LOSS	
		double heatLoss = structuralLoss // in kBTU/hr
						+ kBTU_PER_HOUR_PER_kW * (ventilationHeatLoss + heatAirlock + solarHeatLoss); // in kBTU/hr
		
		//if (isHallway) System.out.println(building.getNickName() + "'s heatLoss : " + Math.round(heatLoss*10_000D)/10_000D + " kBTU/Hr");

		// (3) CALCULATE THE INSTANTANEOUS CHANGE OF TEMPERATURE (DELTA T)
		//double d_t_F = time_interval * 1000D * (heatGain - heatLoss) / SHC_area ;
		
		// (3a) FIND THE DIFFERENCE between heat gain and heat loss
		double d_heat1 = heatGain - heatLoss;
			
		// (3b) FIND the CONVERSION FACTOR
		double c_factor = conversion_factor * millisols; // =  0.0047 at the start of the sim
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s t_factor : " + Math.round(t_factor*10000D)/10000D);

		// (3c) USE HEAT SINK to reduce the value of d_heat
		double d_heat2 = 0;
		
		if (t_in_C <= t_initial - 3.0 * T_LOWER_SENSITIVITY
				|| t_in_C >= t_initial + 3.0 * T_LOWER_SENSITIVITY)
			d_heat2 = computeHeatSink(d_heat1, c_factor);
		else
			d_heat2 = d_heat1;

		//if (buildingType.equalsIgnoreCase("lander hab")) 
		//	logger.info(building.getNickName() + "'s d_heat : " + Math.round(d_heat1*100D)/100D + " --> " + Math.round(d_heat2*100D)/100D);
	
		// (3d) FIND THE CHANGE OF TEMPERATURE (in degress celsius)  
		double d_t_C = c_factor * d_heat2 ; 
		// 5/9 = 1/convertCtoF ; // the difference between deg F and deg C . The term -32 got cancelled out
		//°C  x  9/5 + 32 = °F
		//(°F - 32)  x  5/9 = °C
		//applyHeatBuffer(changeOfTinC);
		//if (buildingType.equalsIgnoreCase("lander hab")) System.out.println( "   d_t : " + Math.round(d_t_C*1000D)/1000D);
		return d_t_C;
	}

	/***
	 * Computes the amount of heat absorbed or release by the heat sink 
	 * @param heat
	 * @param c_factor
	 * @return heat
	 */
	public double computeHeatSink(double heat, double c_factor) {
		double d_heat = heat; 
		// (3c0 FIND THE HEAT TO BE ABSORBED OR RELEASED BY THE HEAT SINK
		double limit = 1/c_factor; // = 210.9404 or 213.4221 at the start of the sim
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s limit : " + Math.round(limit*10000D)/10000D);
			
		double transfer = 0;

		if (d_heat > 0) {
			if (d_heat/limit > 2) {				// e.g. 7 > 5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [0] ");
				transfer = .75 * d_heat;		// d = 7 - 5 = 2				
			}
			else if (d_heat/limit > 1.5) {				// e.g. 7 > 5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [1] ");
				transfer = .5 * d_heat;		// d = 7 - 5 = 2				
			}

			else if (d_heat/limit > 1) {				// e.g. 7 > 5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [1] ");
				transfer = .375 * d_heat;		// d = 7 - 5 = 2				
			}
			else if (d_heat/limit > .5 ) {		// e.g. 4 > .5 * 5 
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [2] ");
				transfer = .5 * d_heat;	// d = 4 - .5 * 5 = 4 - 2.5 = 1.5			
			}
			else {
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [3] ");
				transfer = .75 * d_heat;	// d = 2 - .25 * 5 = 2 - 1.25 = 0.75			
			}
			
			if (heat_sink <= 100*floorArea) { // max out in absorbing x BTU/hr of heat
				// suck up heat
				heat_sink += transfer;						
				d_heat -= transfer;	
			}
			//else {
			//	transfer = 0;
			//}

		}
		
		else if (d_heat < 0) {
			double delta = Math.abs(d_heat);
			if (delta/limit > 2) { 			// e.g. -7 < -5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [4] ");
				transfer = .75 * d_heat;	// d = -7 + 5 = -2; d is negative
			}
			else if (delta/limit > 1.5) { 			// e.g. -7 < -5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [5] ");
				transfer = .5 * d_heat;	// d = -7 + 5 = -2; d is negative
			}
			else if (delta/limit > 1) { 			// e.g. -7 < -5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [5] ");
				transfer = .375 * d_heat;	// d = -7 + 5 = -2; d is negative
			}
			else if (delta/limit > .5) { 	// e.g. -4 < -.5 * 5
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [6] ");
				transfer = .5 * delta; // d = -4 + .5 * 5 = -4 + 2.5 = -1.5; d is -ve
			}
			else { 	
				//if (buildingType.equalsIgnoreCase("lander hab")) System.out.print(" [7] ");
				transfer = .75 * d_heat; // d = -2 + .25 * 5 = -2 + 1.25 = -0.75; d is -ve
			}
			
			if (heat_sink >= -100*floorArea) { // max out in releasing x BTU/hr of heat
				// release heat
				heat_sink += transfer;  
				d_heat -= transfer; 	
			}
			//else {
			//	transfer = 0;
			//}

		}
		
		return d_heat;
	}

	/**
	 * Computes heat gain from adjacent room(s) due to air ventilation. This helps the temperature equilibrium
	 * @param t temperature
	 * @return temperature
	 */
	public double heatGainVentilation(double t, double time) {
		double total_gain = 0; //heat_dump_1 = 0 , heat_dump_2 = 0;
		boolean tooLow = t < (t_initial - 2.0 * T_LOWER_SENSITIVITY );
		boolean tooHigh = t > (t_initial + 2.0 * T_UPPER_SENSITIVITY );
		double speed_factor = .01 * time * CFM;
		
		if (tooLow || tooHigh) { // this temperature range is arbitrary
			// TODO : determine if someone opens a hatch ??
			//LogConsolidated.log(logger, Level.WARNING, 2000, sourceName, 
			//		"The temperature for " + building + " in " + settlement 
			//	+ " is " + Math.round(t*100D)/100D + " C"
			//	, null);
			//LogConsolidated.log(logger, Level.WARNING, 5000, sourceName, "time : " + Math.round(time*1000D)/1000D, null);
			// Note : time = .121 at x128
			
			if (adjacentBuildings == null) {
				adjacentBuildings = settlement.getBuildingConnectors(building);
			}
			
			int size = adjacentBuildings.size();
			//area_factor = Math.sqrt(Math.sqrt(floorArea));
/*			
			if (isHallway)
				area_factor = .5;
			else if (isGreenhouse) 
				area_factor = 1.5;
			else if (isLargeGreenhouse()) 
				area_factor = 2.3;
			else if (isGarage())
				area_factor = 2;
			else if (isLoadingDockGarage())
				area_factor = 2.5;
*/			
			
			for (int i = 0; i < size; i++) {
				double t_next = adjacentBuildings.get(i).getCurrentTemperature();
				double t_i = adjacentBuildings.get(i).getInitialTemperature();
				//LogConsolidated.log(logger, Level.WARNING, 2000, sourceName, 
				//		"The temperature for the adj " + adjacentBuildings.get(i) + " in " + settlement 
				//	+ " is " + Math.round(t*100D)/100D + " C"
				//	, null);

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
/*			
			if (total_gain > 0)
				LogConsolidated.log(logger, Level.INFO, 20000, sourceName, 
						"Pumping in " + Math.round(total_gain*100D)/100D + " kW of heat by air vent to " 
				+ building + " in " + settlement + " from adjacent building(s).", null);
			else if (total_gain < 0)
				LogConsolidated.log(logger, Level.INFO, 20000, sourceName, 
						"Dumping off " + -Math.round(total_gain*100D)/100D + " kW of excess heat by air vent from " 
				+ building + " in " + settlement + " to adjacent building(s).", null);
*/	
		}
		
		return total_gain;
	}

	
	/**
	 * Applies a "mathematical" heat buffer to artificially stabilize temperature fluctuation due to rapid simulation time
	 * @return temperature (degree C)

	// 2014-10-17 Added applyHeatBuffer()
	public void applyHeatBuffer(double t) {
		// 2015-02-18 Added heat trap
		// This artificial heat trap or buffer serves to
		// 1. stabilize the temperature calculation
		// 2. smoothen out any abrupt temperature variation(*) in the settlement unit window
		// 3. reduce the frequency of the more intensive computation of heat gain and heat loss in determineDeltaTemperature()
		// Note*:  MSP is set to run at a much faster pace than the real time marsClock and the temperature change inside a room is time-dependent.
		//double factor = t;
		if (t > 2) { // && storedHeat >= -30  && storedHeat <= 30) {
			// Arbitrarily select to "trap" the amount heat so as to reduce "t" to half of its value
			storedHeat = storedHeat + 0.7 * t;
			t = t - 0.7 * t;
		}
		else if (t <= 2 && t >= 1) { // && storedHeat >= -30  && storedHeat <= 30) {
			// Arbitrarily select to "trap" the amount heat so as to reduce "t" to half of its value
			storedHeat = storedHeat + 0.4 * t;
			t = t - 0.4 *  t;
		}
		else if (t < -1 && t >= -2) {
			t = t - 0.5 * t;
			storedHeat = storedHeat + 0.4 * t;
		}
		else { //if (t < -2) {
			storedHeat = storedHeat + 0.8 * t;
			t = t - 0.8 * t;
		}

		if (storedHeat > HEAT_CAP) {
			t = t + 0.3;
			storedHeat = storedHeat - 0.3;
		}
		else if (storedHeat < -HEAT_CAP) {
			t = t - 0.3;
			storedHeat = storedHeat + 0.3;
		}

	    //System.out.println("storedHeat : "+ storedHeat);
		//System.out.println("t : "+ t);

	    deltaTemperature = t;

	}
*/


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

		double result = 0;
		return result;
	}


	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 */
	public void timePassing(double time) {
		counts++;
/*
		int time_factor = (int)(time *25D);
		if (time_factor < 1)
			time_factor = 1;
		time_factor *= PER_UPDATE;
*/		
		if (masterClock == null)
			masterClock = Simulation.instance().getMasterClock();
		if (marsClock == null)
			marsClock = masterClock.getMarsClock();
	
		double time_ratio = masterClock.getTimeRatio();
		double time_ratio_1 = Math.sqrt(time_ratio); // = 2.38 at x128
		double time_ratio_2 = Math.sqrt(Math.sqrt(time_ratio_1/4)); // = 2.38 at x128
		double update = Math.round(PER_UPDATE * time_ratio_2);
		if (update < 1)
			update = 1;
/*		
		LogConsolidated.log(logger, Level.INFO, 100, sourceName, 
		//		" msol : "
		//		+ _msol
				//Math.round(_msol*1000D)/1000D 
				 " c : " + counts
				, null);
*/		
		if (counts % (int)update == 0) {
			counts = 0;

			double limit = MSOL_LIMIT/Math.sqrt(time_ratio_2)*1.5;
			double new_time = update*time;
			int numCycles = (int)(new_time/limit);
			if (numCycles < 1)
				numCycles = 1;
			double msol = marsClock.getMillisol();
			int _msol =  (int) msol;
			
			if (isGreenhouse)
				emissivity = emissivityMap.get(_msol);
			else
				emissivity = EMISSIVITY_INSULATED;

			int countDown = numCycles;
			
			while (countDown != 0) {
				countDown--;
				cycleThermalControl(new_time/numCycles);
				//LogConsolidated.log(logger, Level.INFO, 1000, sourceName, 
				//	"  msol : " + _msol
				//	+ "   numCycles : " + numCycles
				//	+ "   t : " + Math.round(new_time/numCycles*1000.0)/1000.0 + " "
				//	, null);
			}
		}

		adjustHeatMode();

	}

	/**
	 * Notifies thermal control subsystem for the temperature change and power up and power down
	 * via 3 steps (this method houses the main thermal control codes)
	 * @param time in millisols
	 */
	public void cycleThermalControl(double time) {
		//System.out.println("Calling adjustThermalControl()");

		// Detect temperature change based on heat gain and heat loss
		double t = currentTemperature;
		double t_out = settlement.getOutsideTemperature();

		// STEP 1 : CALCULATE HEAT GAIN/LOSS AND RELATE IT TO THE TEMPERATURE CHANGE
		double dt = determineDeltaTemperature(t, time);
		
		// limit the abrupt change of temperature, for sanity sake
		if (dt < -20)
			dt = -20;
		else if (dt > 20)
			dt = 20;
		
		// STEP 2 : ADJUST THE CURRENT TEMPERATURE
		// Adjust the current temperature
		t += dt;
		// Safeguard against anomalous dt that would have crashed mars-sim
		if (t > 60)
			t = 60;
		else if (t < t_out)
			t = t_out;
		
		currentTemperature = t;
		
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
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {
		return 0;
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
	public double getMaintenanceTime() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void destroy() {
		super.destroy();

		building = null;
	 	//thermalSystem = null;
		building = null;
		weather = null;
		location = null;
		emissivityMap = null;
		masterClock = null;
		marsClock = null;
		surfaceFeatures = null;
		settlement = null;
		manager = null;
		farm = null;
		adjacentBuildings = null;
	}

}