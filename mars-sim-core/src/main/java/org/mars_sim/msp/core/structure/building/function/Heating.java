/**
 * Mars Simulation Project
 * Heating.java
 * @version 3.07 2015-02-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.ThermalSystem;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * The Heating class is a building function for regulating temperature in a settlement..
 */
public class Heating
extends Function
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	//private static Logger logger = Logger.getLogger(Heating.class.getName());

	DecimalFormat fmt = new DecimalFormat("#.#######");

	private static final BuildingFunction FUNCTION = BuildingFunction.LIFE_SUPPORT;
	// Data members
	//2015-02-19 Added MILLISOLS_PER_UPDATE
	private static final int ONE_TENTH_MILLISOLS_PER_UPDATE = 10 ;
	private static final double TRANSMITTANCE_GREENHOUSE_HIGH_PRESSURE = .55 ;
	private static final double EMISSIVITY_DAY = .8 ;
	private static final double EMISSIVITY_NIGHT = 1.0 ;
	private static final double STEFAN_BOLTZMANN_CONSTANT = 0.0000000567 ; // in W / (m^2 K^4)

	//private static final double ROOM_TEMPERATURE = 22.5D;
    //public static final double GREENHOUSE_TEMPERATURE = 24D;
    // Thermostat's temperature allowance
    private static final double T_UPPER_SENSITIVITY = 1D;
    private static final double T_LOWER_SENSITIVITY = 1D;
    private static final int HEAT_CAP = 200;
    private static final double HEAT_GAIN_FROM_EQUIPMENT = 3000;
    private static final double HEAT_DISSIPATED_PER_PERSON = 350D;
    //private static final double kPASCAL_PER_ATM = 1D/0.00986923267 ; // 1 kilopascal = 0.00986923267 atm
    //private static final double R_GAS_CONSTANT = 8.31441; //R = 8.31441 m3 Pa K−1 mol−1
	// 1 kilopascal = 0.00986923267 atm
	// 1 cubic ft = L * 0.035315
    // A full scale pressurized Mars rover prototype may have an airlock volume of 5.7 m^3
    private double VOLUME_OF_AIRLOCK =  3 * 3 * 2; //in m^3
	// Molar mass of CO2 = 44.0095 g/mol
    // average density of air : 0.020 kg/m3
	//double n = weather.getAirDensity(coordinates) * vol / 44D;
	//private double n_CO2 = .02D * VOLUME_OF_AIRLOCK / 44*1000;
	// 1 cubic feet of air has a total weight of 38.76 g
	//private double n_air = 1D;
	//private double n_sum = n_CO2 + n_air;
	private double C_p = 1.0015; // specific heat capacity of air at 250K
	private double dryAirDensity = 1.275D; // breath-able air in [kg/m3]
	private double energy_factor = C_p * VOLUME_OF_AIRLOCK * dryAirDensity /1000; // for airlock heat loss
	//private double t_factor =  vol / n;
    private double width;
	private double length;
	private double floorArea;
	//private double baseHeatRequirement;
	private double basePowerDownHeatRequirement = 0;
	private double meter2Feet = 10.764;
	//private static int count;
	// Specific Heat Capacity = 4.0 for a typical U.S. house
	private double SHC = 6.0; //in BTU/ sq ft / F
	private double SHC_Area;
	// Building Loss Coefficient = 1.0 for a typical U.S. house
	//private double BLC = 0.2;
	private double R_value = 30;
	private double U_value = 1/R_value;
    private double factor_heatLossFromRoof;
    private double factor_heatLossFromCeiling;
    private double factor_heatLossFromWall;
    private double factor_heatLossFromCrackLength;
    private double q_H_factor = 21.4D/10/2.23694; // 1 m per sec = 2.23694 miles per hours
    private double airChangePerHr = .5;

	//private double factor_heatLoss; // = U_value * floorArea * meter2Feet ;

	// 2014-11-02 Added heatGenerated
	private double heatGenerated = 0; // the initial value is zero
	private double heatGeneratedCache = 0; // the initial value is zero
	//private double powerRequired;
	private double heatRequired;
	private double deltaTemperature;
	private double currentTemperature;
    //private double heatLossEachEVA;

	private double storedHeat;
	//double interval = Simulation.instance().getMasterClock().getTimePulse() ;
	// 1 hour = 3600 sec , 1 sec = (1/3600) hrs
	// 1 sol on Mars has 88740 secs
	// 1 sol has 1000 milisol
	private double elapsedTimeinHrs; // = ONE_TENTH_MILLISOLS_PER_UPDATE / 10D /1000D * 24D;
	private double t_initial;

  	//private String buildingType;

  	//private ThermalGeneration furnace;
 	private ThermalSystem thermalSystem;
	private Building building;
	private Weather weather;
	private Coordinates coordinates;
	private MasterClock masterClock;
	private MarsClock clock;

	/**
	 * Constructor.
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public Heating(Building building) {
		// Call Function constructor.
		super(FUNCTION, building);

		this.building = building;

		//BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		//powerRequired = config.getLifeSupportPowerRequirement(building.getBuildingType());

		length = getBuilding().getLength();
		width = getBuilding().getWidth() ;
		//buildingType =  getBuilding().getBuildingType();
		floorArea = length * width ;

		elapsedTimeinHrs = ONE_TENTH_MILLISOLS_PER_UPDATE / 10D /1000D * 24D;
		//elapsedTimeinHrs = ONE_TENTH_MILLISOLS_PER_UPDATE / 10D *1000D / 24D;

		//factor_heatLoss = U_value * floorArea * meter2Feet * elapsedTimeinHrs;
		factor_heatLossFromRoof = U_value * floorArea * meter2Feet;
		factor_heatLossFromCeiling = U_value * floorArea * meter2Feet;
		factor_heatLossFromWall = U_value * 2 * (length * 9D + width * 9D) * meter2Feet;
		//assume airChangePerHr = .5, q_H = 21.4;
		// CrackLength from two EVA airlock and 4 windows
		factor_heatLossFromCrackLength = 0.244 * .075 * airChangePerHr * q_H_factor * ( 2 * (2 + 7) + 4 * (2 + 3) );
		//SHC_Area = floorArea * SHC * floorArea;
		SHC_Area = floorArea * meter2Feet * meter2Feet * SHC ;

		t_initial = building.getInitialTemperature();
		currentTemperature = t_initial;
		deltaTemperature = 0;
		//t_factor = vol / R_GAS_CONSTANT / n;

		masterClock = Simulation.instance().getMasterClock();
		clock = masterClock.getMarsClock();
		weather = Simulation.instance().getMars().getWeather();
		coordinates = building.getBuildingManager().getSettlement().getCoordinates();
		thermalSystem = building.getBuildingManager().getSettlement().getThermalSystem();
	}



	/**
     * Gets the initial temperature of a building.
     * @return temperature (deg C)
	//2014-10-23  Added getInitialTemperature()
    public double getInitialTemperature() {
    	//double result;
		//if (config.hasFarming(buildingType))
		//if (buildingType == "Inflatable Greenhouse"
		//		|| buildingType == "Large Greenhouse"
		//		||	buildingType == "Inground Greenhouse" )
		//	return GREENHOUSE_TEMPERATURE;
		//else
        //    return ROOM_TEMPERATURE;
		return building.getInitialTemperature();
    }
	*/

    /**
     * Gets the temperature of a building.
     * @return temperature (deg C)
    */
	//2014-10-17  Added getCurrentTemperature()
    public double getCurrentTemperature() {
            return currentTemperature;
    }


	/** Turn heat source off if reaching pre-setting temperature
	 * @return none. set heatMode
	 */
	// 2014-11-02 Added checking if PowerMode.POWER_DOWN
	// TODO: also set up a time sensitivity value
	public void setNewHeatMode(double t) {
		//double t_now = currentTemperature; //building.getTemperature();
		double t_now = t;
		// if building has no power, power down the heating system
		if (building.getPowerMode() == PowerMode.POWER_DOWN)
			building.setHeatMode(HeatMode.HEAT_OFF);
		// Note: should NOT be OFFLINE since solar heat engine can still be turned ON
		// if building is under maintenance, use HeatMode.OFFLINE
		else if (building.getMalfunctionManager().hasMalfunction())
			building.setHeatMode(HeatMode.OFFLINE);
		else if (building.getPowerMode() == PowerMode.FULL_POWER) {
			// ALLOWED_TEMP is thermostat's allowance temperature setting
		    // If T_NOW deg above INITIAL_TEMP, turn off furnace
			if (t_now > (t_initial + T_UPPER_SENSITIVITY )) {
			//if (T_NOW > T_INITIAL ) {
				building.setHeatMode(HeatMode.HEAT_OFF);
			// If T_NOW is below INITIAL_TEMP - T_SENSITIVITY , turn on furnace
			} else if (t_now < (t_initial - T_LOWER_SENSITIVITY)) {
				building.setHeatMode(HeatMode.ONLINE);
			} //else ; // do nothing to change the HeatMode
		}

	}

	/**Adjust the current temperature in response to the delta temperature
	 * @return none. update currentTemperature
	 */
	//public void updateTemperature(double dt) {
	//	currentTemperature += dt;
	//}


	/**
	 * Relate the change in heat to change in temperature
	 * @return none. save result as deltaTemperature
	 */
	//2015-02-19 Modified determineDeltaTemperature() to use MILLISOLS_PER_UPDATE
	public double determineDeltaTemperature(double t, double millisols) {
		// IT HAS THREE PARTS
		double outsideTemperature = 0;
		outsideTemperature = weather.getTemperature(coordinates);
		// heatGain and heatLoss are to be converted from BTU to kJ below
		//
		// (1) CALCULATE HEAT GAIN
		double heatGain = 0; // in BTU
		double heatGenerated = 0; //in kJ/s
		if (building.getHeatMode() == HeatMode.ONLINE) {
			// HeatGenerated in kW
			// Note: 1 kW = 3412.14 BTU/hr
			if (thermalSystem == null)
				thermalSystem = building.getBuildingManager().getSettlement().getThermalSystem();
			heatGenerated =  thermalSystem.getGeneratedHeat();
		}
		// each person emits about 350 BTU/hr
		double heatGainFromOccupants = HEAT_DISSIPATED_PER_PERSON * building.getInhabitants().size() ;
		heatGain = 3412.14 * heatGenerated
				+ heatGainFromOccupants
				+ HEAT_GAIN_FROM_EQUIPMENT ; // in BTU/hr

		//
		// (2) CALCULATE HEAT LOSS
		double energyExpendedToHeatAirlockMartianAir = 0;
		if (building.getFunction(BuildingFunction.EVA) != null) {
			if (building.numOfPeopleInAirLock() > 0) {
				energyExpendedToHeatAirlockMartianAir = energy_factor * (22.5D - outsideTemperature) * 2;
				// Multiplied by two in order to account for
				//(1) the energy loss due to the original room-temperature air gushing out
				//(2) the energy required to heat up the in-rush of the new martian air
				//System.out.println("energyExpendedToHeatAirlockMartianAir in kW : " + energyExpendedToHeatAirlockMartianAir);
				//System.out.println("energyExpendedToHeatAirlockMartianAir in BTU : " + 3412.14 * energyExpendedToHeatAirlockMartianAir);
				// e.g. kW : 0.323
				// e.g. BTU : 1101.4
			}
		}
		double diffTinF =  (t - outsideTemperature) * 9D / 5D;
		double heatLoss = diffTinF
						* (factor_heatLossFromRoof
						+ factor_heatLossFromCeiling
						+ factor_heatLossFromWall
						+ factor_heatLossFromCrackLength * weather.getWindSpeed(coordinates)
						+ 3412.14 * energyExpendedToHeatAirlockMartianAir);


		//
		// (3) CALCULATE THE INSTANTANEOUS CHANGE OF TEMPERATURE (DELTA T)
		double changeOfTinF = elapsedTimeinHrs * millisols *( heatGain - heatLoss) / SHC_Area ;
		double changeOfTinC = changeOfTinF * 5D / 9D ; // the difference between deg F and deg C (namely -32) got cancelled out
		//applyHeatBuffer(changeOfTinC);
		deltaTemperature = changeOfTinC;
		return changeOfTinC;
	}

	/**
	 * Gets the temperature change of a building due to heat gain/loss
	 * @return temperature (degree C)
	 */
	//public double getDeltaTemperature() {
	//    return deltaTemperature;
	//}

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
		// Note*:  MSP is set to run at a much faster pace than the real time clock and the temperature change inside a room is time-dependent.
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
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {

		if ((int)time >= 1) {
			// if time >= 1 millisols, need to call adjustThermalControl() right away to update the temperature
			adjustThermalControl(time);
		}

		else {
			// if time < 1 millisols, may skip calling adjustThermalControl() for several cycle to reduce CPU utilization.
			if (masterClock == null)
				masterClock = Simulation.instance().getMasterClock();

			if (clock == null)
				clock = masterClock.getMarsClock();

			int oneTenthmillisols =  (int) (clock.getMillisol() * 10);
			//System.out.println(" oneTenthmillisols : " + oneTenthmillisols);
			if (oneTenthmillisols % ONE_TENTH_MILLISOLS_PER_UPDATE == 0)
				//System.out.println(" oneTenthmillisols % 10 == 0 ");
				// Skip calling for thermal control for Hallway (coded as "virtual" building as of 3.07)
				//if (!building.getBuildingType().equals("Hallway"))
				adjustThermalControl(time);
		}
	}

	/**
	 * Notify thermal control subsystem for the temperature change and power up and power down
	 * via 3 steps (this method houses the main thermal control codes)
	 * @return power (kW)
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

	@Override
	public void destroy() {
		super.destroy();

		building = null;
	 	thermalSystem = null;
		building = null;
		weather = null;
		coordinates = null;
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


	@Override
	public double getMaintenanceTime() {
		// TODO Auto-generated method stub
		return 0;
	}
}