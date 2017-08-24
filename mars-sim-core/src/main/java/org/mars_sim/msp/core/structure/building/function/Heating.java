/**
 * Mars Simulation Project
 * Heating.java
 * @version 3.1.0 2018-08-16
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.ThermalSystem;
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
import java.util.logging.Level;
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
 	private static Logger logger = Logger.getLogger(LivingAccommodations.class.getName());

	private static String sourceName = logger.getName();
    
	/** default logger. */
	//private static Logger logger = Logger.getLogger(Heating.class.getName());
	private static final BuildingFunction FUNCTION = BuildingFunction.LIFE_SUPPORT;

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

	private static final double INSULATION_BLANKET = 30000D;
	
    // Thermostat's temperature allowance
    private static final double T_UPPER_SENSITIVITY = 1D;
    private static final double T_LOWER_SENSITIVITY = 1D;

    private static final double kBTU_HEAT_DISSIPATED_PER_PERSON = .35D;
    //private static final int HEAT_CAP = 200;  
	private static final int ONE_TENTH_MILLISOLS_PER_UPDATE = 10 ;
	
    //private static final double kPASCAL_PER_ATM = 1D/0.00986923267 ; // 1 kilopascal = 0.00986923267 atm
    //private static final double R_GAS_CONSTANT = 8.31441; //R = 8.31441 m3 Pa K−1 mol−1
	// 1 kilopascal = 0.00986923267 atm
	// 1 cubic ft = L * 0.035315
    // A full scale pressurized Mars rover prototype may have an airlock volume of 5.7 m^3
	
	private static final double HEIGHT = 2.5; // in meter
	
	/** The average volume of a airlock [m^3] */	
    private static double AIRLOCK_VOLUME_IN_CM = Building.AIRLOCK_VOLUME_IN_CM; // = 12 [in m^3]
    /**  convert meters to feet  */
	private static final double M_TO_FT = 3.2808399;//10.764;
	/**  Specific Heat Capacity = 4.0 for a typical U.S. house */
	private static final double SHC = 6.0; //in BTU/ sq ft / F
	/**  R-value is a measure of thermal resistance, or ability of heat to transfer from hot to cold, through materials such as insulation */
	private static final double R_value = 30;
	/** Building Loss Coefficient (BLC) is 1.0 for a typical U.S. house */
	//private static double BLC = 0.2;

	// Heat Loss = (1/R-value)(surface area)(∆T)
	
	private static double U_value = 1/R_value; // U values are in [Btu/°F/ft2/hr]
	
    private static double U_value_area_crack_length, U_value_area_crack_length_for_airlock;
    
    private static double q_H_factor = 21.4D/10/2.23694; // 1 m per sec = 2.23694 miles per hours
    
    private static double airChangePerHr = .5;
    
    // Molar mass of CO2 = 44.0095 g/mol
    // average density of air : 0.020 kg/m3
	//double n = weather.getAirDensity(coordinates) * vol / 44D;
	//private double n_CO2 = .02D * VOLUME_OF_AIRLOCK / 44*1000;
	// 1 cubic feet of air has a total weight of 38.76 g
	//private double n_air = 1D;
	//private double n_sum = n_CO2 + n_air;
    
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

	private double basePowerDownHeatRequirement = 0;

	private double SHC_area;

    private double U_value_area_ceiling_or_floor;
    
    private double U_value_area_wall;

	private double heatGenerated = 0; // the initial value is zero
	
	private double heatGeneratedCache = 0; // the initial value is zero
	//private double powerRequired;
	private double heatRequired;
	/** The heat extracted by the ventilation system */
	private double heatExtractedVentilation;
	/** The current temperature of this building */
	private double currentTemperature;


	private double elapsedTimeinHrs; // = ONE_TENTH_MILLISOLS_PER_UPDATE / 10D /1000D * 24D;
	private double t_initial;
	private double emissivity;
	
	/** */
	private boolean isGreenhouse = false;
	/** */
	private boolean isHallway = false;
	/** Is the airlock door open */
	private boolean hasHeatDumpViaAirlockOuterDoor = false;
	/** */
	private Map<Integer, Double> emissivityMap;
	/** */
  	private String buildingType;

  	//private ThermalGeneration furnace;
 	private ThermalSystem thermalSystem;
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
		thermalSystem = settlement.getThermalSystem();
		if (surfaceFeatures == null)
			surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();

		length = building.getLength();
		width = building.getWidth() ;

		floorArea = length * width ;

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
	
		elapsedTimeinHrs = 24D / 1000D;// [ hr/millisol] ONE_TENTH_MILLISOLS_PER_UPDATE / 10D /1000D * 24D;


		U_value_area_ceiling_or_floor = U_value * (floorArea * M_TO_FT * M_TO_FT);
		U_value_area_wall = U_value * 2D * (width + length) * M_TO_FT * (HEIGHT * M_TO_FT);

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

		t_initial = building.getInitialTemperature();
		currentTemperature = t_initial;

		//t_factor = vol / R_GAS_CONSTANT / n;

		emissivityMap = new ConcurrentHashMap<>();

		for (int i = 0; i < 1000; i++) {
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
     * Is this building a greenhouse.
     * @return true or false
     */
    public boolean isGreenhouse() {
		return buildingType.toLowerCase().contains("greenhouse");
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
	public void setNewHeatMode(double t) {
		double t_now = t;
		if (building.getPowerMode() == PowerMode.FULL_POWER) {
			// ALLOWED_TEMP is thermostat's allowance temperature setting
		    // If T_NOW deg above INITIAL_TEMP, turn off furnace
			if (t_now > (t_initial + T_UPPER_SENSITIVITY )) {
				building.setHeatMode(HeatMode.HEAT_OFF);
			}
			else if (t_now > t_initial) {
				building.setHeatMode(HeatMode.HALF_HEAT);
			}
			else {//if (t_now < (t_initial - T_LOWER_SENSITIVITY)) {
				building.setHeatMode(HeatMode.ONLINE);
			} //else ; // do nothing to change the HeatMode
		}
		//else if (building.getPowerMode() == PowerMode.POWER_DOWN)
			// if building has no power, power down the heating system
		//	building.setHeatMode(HeatMode.HEAT_OFF);
		// Note: should NOT be OFFLINE since solar heat engine can still be turned ON
		// if building is under maintenance, use HeatMode.OFFLINE
		else if (building.getMalfunctionManager().hasMalfunction())
			building.setHeatMode(HeatMode.OFFLINE);

	}

	/**
	 * Determines the change in temperature 
	 * @return deltaTemperature
	 */
	public double determineDeltaTemperature(double t_in_C, double millisols) {
		HeatMode mode = building.getHeatMode();
		double time_interval = elapsedTimeinHrs * millisols;
		// THREE-PART CALCULATION
		double t_out_C = settlement.getOutsideTemperature();
		// heatGain and heatLoss are to be converted from kJ to BTU below
		double convertCtoF =  (t_in_C - t_out_C) * 1.8; //1.8 =  9D / 5D;
		double t_in_K = t_in_C + C_TO_K;
		double t_out_K = t_out_C + C_TO_K;
		
		// (1) CALCULATE HEAT GAIN

		// (1a) CALCULATE HEAT PUMPED IN BY DIRECTING HEATING
		double heatPumpedIn = 0; //in kW
		if (mode == HeatMode.ONLINE || mode == HeatMode.HALF_HEAT) {
			// Note: 1 kW = 3412.14 BTU/hr
			if (thermalSystem == null)
				thermalSystem = settlement.getThermalSystem();
			heatPumpedIn =  thermalSystem.getGeneratedHeat();
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
			solarHeatGain =  I * transmittance_greenhouse * floorArea /1000D;
					
			//if (I < 40)
			//	solarHeatGain += INSULATION_BLANKET; 
		}
		else {
			solarHeatGain =  I * transmittance_window * 4 * .5 * .5/1000D;
		}
		
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s solarHeatGain : " + Math.round(solarHeatGain*10_000D)/10_000D + " kW");
		
		// (1e) ADD HEAT GAIN BY EQUIPMENT
		// see heatGainEqiupment below
		
		// (1f) CALCULATE HEAT GAIN DUE TO ARTIFICIAL LIGHTING
		double lightingGain = 0;
		
		if (isGreenhouse) {
			if (farm == null) { // greenhouse has a semi-transparent rooftop
				farm = (Farming) building.getFunction(BuildingFunction.FARMING);
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
			structuralLoss = t_in_C * convertCtoF / 1000D * (
					U_value_area_ceiling_or_floor * 2D
					+ U_value_area_wall
					+ U_value_area_crack_length_for_airlock * weather.getWindSpeed(location) * M_TO_FT);
					// Note : 1 m/s = 3.28084 ft/s
		}
		else {
			structuralLoss = t_in_C * convertCtoF / 1000D * (
						U_value_area_ceiling_or_floor * 2D
						+ U_value_area_wall
						+ U_value_area_crack_length * weather.getWindSpeed(location) * M_TO_FT);
		}
		
		// U_value in [Btu/°F/ft2/hr]
		
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s structuralLoss/1000D : " + Math.round(structuralLoss/1000D*10_000D)/10_000D + " kBTU/Hr");

		// (2c) CALCULATE HEAT LOSS DUE TO VENTILATION
		double ventilationHeatLoss = heatExtractedVentilation + heatGainVentilation(t_in_C); 
		// reset heatExtracted to zero
		heatExtractedVentilation = 0;
		
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s ventilationHeatLoss : " + Math.round(ventilationHeatLoss*10_000D)/10_000D + " kBTU/Hr");
		
		// (2d) CALCULATE HEAT LOSS DUE TO SOLAR RADIATION
		double solarHeatLoss =  0;

		if (isGreenhouse) {
			solarHeatLoss = emissivity * STEFAN_BOLTZMANN_CONSTANT
					* ( Math.pow(t_in_K, 4) - Math.pow(t_out_K, 4) ) * hullArea /1000D;
		}
		
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s solarHeatLoss : " + Math.round(solarHeatLoss*10_000D)/10_000D + " kW");

		// (1e) CALCULATE TOTAL HEAT LOSS
		
		double heatLoss = structuralLoss // in kBTU/hr
						+ kBTU_PER_HOUR_PER_kW * (ventilationHeatLoss + heatAirlock + solarHeatLoss); // in kBTU/hr
		
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s heatLoss : " + Math.round(heatLoss*10_000D)/10_000D + " kBTU/Hr");


		// (3) CALCULATE THE INSTANTANEOUS CHANGE OF TEMPERATURE (DELTA T)
		double d_t_F = time_interval * 1000D * (heatGain - heatLoss) / SHC_area ;

		double d_t_C = d_t_F / convertCtoF ; // 5/9 = 1/convertCtoF ; // the difference between deg F and deg C . The term -32 got cancelled out
		//applyHeatBuffer(changeOfTinC);
		//if (isGreenhouse) System.out.println(building.getNickName() + "'s d_t_inC : " + Math.round(d_t_C*10000D)/10000D);
		return d_t_C;
	}


	/**
	 * Computes heat gain from adjacent room(s) due to air ventilation. This helps the temperature equilibrium
	 * @param t temperature
	 * @return temperature
	 */
	public double heatGainVentilation(double t) {
		double total_dump = 0; //heat_dump_1 = 0 , heat_dump_2 = 0;
		
		if (t < (t_initial - 4 * T_LOWER_SENSITIVITY ) || t > (t_initial + 4 * T_UPPER_SENSITIVITY )) { // this temperature range is arbitrary
			// TODO : determine if someone opens a hatch ??
			
			//LogConsolidated.log(logger, Level.WARNING, 3000, sourceName, "Temperature is below 10 C at " + building + " in " + settlement, null);

			if (adjacentBuildings == null) {
				adjacentBuildings = settlement.getBuildingConnectors(building);
			}
			
			int size = adjacentBuildings.size();
			
			for (int i = 0; i < size; i++) {
				double t_0 = adjacentBuildings.get(i).getCurrentTemperature();
				double ratio = t_0 / t;
				double heat_dump_0 = 0;
				if (t_0 > t)
					// heat coming in
					heat_dump_0 = .02 * ratio;
				else
					// heat is leaving
					heat_dump_0 = -.02 / ratio;
				
				total_dump += heat_dump_0;
			}
/*			
			double t_1 = 0, t_2 = 0;
			
			//boolean t1 = false, t2 = false;
			int size = adjacentBuildings.size();
			if (size == 1) {
				//t1 = true;
				t_1 = adjacentBuildings.get(0).getCurrentTemperature();
				
				double ratio_1 = t_1 / t;
				if (t_1 > t)
					// heat coming in
					heat_dump_1 = .1 * ratio_1;
				else
					// heat is leaving
					heat_dump_1 = -.1 / ratio_1;	
				
				adjacentBuildings.get(0).extractHeat(heat_dump_1);
				
				//heat_dump_2 = 0;
			}
			
			else if (size == 2) {
				//t2 = true;
				t_1 = adjacentBuildings.get(0).getCurrentTemperature();
				t_2 = adjacentBuildings.get(1).getCurrentTemperature();
				
				double ratio_1 = t_1 / t;
				double ratio_2 = t_2 / t;
				if (t_1 > t)
					// heat coming in
					heat_dump_1 = .1 * ratio_1;
				else
					// heat is leaving
					heat_dump_1 = -.1 / ratio_1;			
					
				if (t_2 > t)
					// heat coming in
					heat_dump_2 = .1 * ratio_2;
				else
					// heat is leaving
					heat_dump_2 = -.1 / ratio_2;
				
				adjacentBuildings.get(0).extractHeat(heat_dump_1);
				adjacentBuildings.get(1).extractHeat(heat_dump_2);
			}
*/			
		}
		
		return total_dump;
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

		//if ((int)time >= 1) {
			// if time >= 1 millisols, need to call adjustThermalControl() right away to update the temperature
		//	adjustThermalControl(time);
		//}

		//else {
			// if time < 1 millisols, may skip calling adjustThermalControl() for several cycle to reduce CPU utilization.
			if (masterClock == null)
				masterClock = Simulation.instance().getMasterClock();
			if (marsClock == null)
				marsClock = masterClock.getMarsClock();

			//int checkTime = (int) Math.round(marsClock.getMillisol());
			// Note : sometimes getMillisol() skips, does Math.round() help ?
			//if (checkTime % 10 == 0) {
			//}
			
			
			// Below is an attempt to ensure that in case getMillisol() skip a millisol, it would still catch it 
			int oneTenthmillisols =  (int) (marsClock.getMillisol() * 10);
			//System.out.println(" oneTenthmillisols : " + oneTenthmillisols);
			if (oneTenthmillisols % ONE_TENTH_MILLISOLS_PER_UPDATE == 0) {
				//System.out.println(" oneTenthmillisols % 10 == 0 ");
				// Skip calling for thermal control for Hallway (coded as "virtual" building as of 3.07)
				//if (!building.getBuildingType().equals("Hallway"))
				if (isGreenhouse())
					emissivity = emissivityMap.get( (int) (oneTenthmillisols/10D) );
				else
					emissivity = EMISSIVITY_INSULATED;
				adjustThermalControl(time);
			}
		//}
	}

	/**
	 * Notifies thermal control subsystem for the temperature change and power up and power down
	 * via 3 steps (this method houses the main thermal control codes)
	 * @param time in millisols
	 */
	// 2014-10-25 Added adjustThermalControl()
	public void adjustThermalControl(double time) {
		// Step 1 of Thermal Control
		// Detect temperature change based on heat gain and heat loss
		double t = currentTemperature;
		double dt = determineDeltaTemperature(t, time);

		// Step 2 of Thermal Control
		// Adjust the current temperature
		//t = updateTemperature(dt);
		t += dt;

		//t = ventAirToAdjacentRoom(t);
		
		currentTemperature = t;
		
		// Step 3 of Thermal Control
		// Turn heat source off if reaching pre-setting temperature
		setNewHeatMode(t);

	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return heatRequired;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {
		return 0;
	}

	/**
	 * Gets the heat this building currently requires for full-power mode.
	 * @return heat in kJ/s.
	 */
	//2014-11-02  Modified getFullHeatRequired()
	public double getFullHeatRequired()  {
		if (heatGeneratedCache != heatGenerated) {
			heatGeneratedCache = heatGenerated;
		}

		// Determine heat required for each function.
		//TODO: should I add power requirement inside
		// thermal generation function instead?
		//Iterator<Function> i = functions.iterator();
		//while (i.hasNext()) result += i.next().getFullHeatRequired();
		return heatGenerated;
	}
	//2014-11-02 Added setHeatGenerated()
	public void setHeatGenerated(double heatGenerated) {
		this.heatGenerated = heatGenerated;
	}

	/**
	 * Gets the heat the building requires for power-down mode.
	 * @return heat in kJ/s.
	*/
	//2014-10-17  Added heat mode
	public double getPoweredDownHeatRequired() {
		double result = basePowerDownHeatRequirement;

		// Determine heat required for each function.
		//Iterator<Function> i = building.getFunctions().iterator();;
		//while (i.hasNext()) result += i.next().getPoweredDownHeatRequired();

		return result;
	}

	/**
	 * Sets the heat that has been added or removed from this building
	 * Note : heat removed if negative. heat added if positive
	 * @param heat removed or added
	 */
	public void extractHeat(double heat) {
		heatExtractedVentilation = heat;
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
	 	thermalSystem = null;
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