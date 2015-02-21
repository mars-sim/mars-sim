/**
 * Mars Simulation Project
 * Heating.java
 * @version 3.07 2015-02-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Iterator;

/**
 * The Heating class is a building function for life support and managing inhabitants.
 */
public class Heating
extends Function
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	//private static Logger logger = Logger.getLogger(Heating.class.getName());
	/** default logger. */

	DecimalFormat fmt = new DecimalFormat("#.#######"); 
	
	private static final BuildingFunction FUNCTION = BuildingFunction.LIFE_SUPPORT;

	// Data members
	//2015-02-19 Added MILLISOLS_PER_UPDATE
	private static final int ONE_TENTH_MILLISOLS_PER_UPDATE = 10 ;
	
    private static final double ROOM_TEMPERATURE = 22.5D;

    public static final double GREENHOUSE_TEMPERATURE = 24D;

    // Thermostat's temperature allowance 
    private static final double T_UPPER_SENSITIVITY = 1D; 
    private static final double T_LOWER_SENSITIVITY = 2.5D; 
    
    private static final int HEAT_CAP = 200;
    
	protected double width;
	protected double length;
	protected double floorArea;

	protected double baseHeatRequirement;
	protected double basePowerDownHeatRequirement;
	//private static int count;
	// Specific Heat Capacity = 4.0 for a typical U.S. house
	protected double SHC = 6.0; 
	// Building Loss Coefficient = 1.0 for a typical U.S. house
	protected double BLC = 0.2; 
	protected double currentTemperature;
	// 2014-11-02 Added heatGenerated
	private double heatGenerated = 0; // the initial value is zero 
	private double heatGeneratedCache = 0; // the initial value is zero 

	//private double powerRequired;
	private double heatRequired;

  	protected double deltaTemperature;
  	protected double storedHeat;

	private double meter2Feet = 10.764;
	//double interval = Simulation.instance().getMasterClock().getTimePulse() ;
	// 1 hour = 3600 sec , 1 sec = (1/3600) hrs
	// 1 sol on Mars has 88740 secs
	// 1 sol has 1000 milisol
	private double elapsedTimeinHrs; // = ONE_TENTH_MILLISOLS_PER_UPDATE / 10D /1000D * 24D;
	
	private double factor_heatLoss; // = BLC * floorArea * meter2Feet ;
	
	private double t_initial; 
	
	private double SHC_Area;
	
  	private String buildingType;
  	
 	protected ThermalGeneration furnace;
 	
	private Building building;
  	

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
	    
		deltaTemperature = 0;

		length = getBuilding().getLength();
		width = getBuilding().getWidth() ;
		buildingType =  getBuilding().getBuildingType();
		floorArea = length * width ;
		
		elapsedTimeinHrs = ONE_TENTH_MILLISOLS_PER_UPDATE / 10D /1000D * 24D;
		
		factor_heatLoss = BLC * floorArea * meter2Feet * elapsedTimeinHrs;
		
		SHC_Area = floorArea * SHC * floorArea;

		t_initial = getInitialTemperature();
		
		currentTemperature = t_initial;
	}


	/**
     * Gets the initial temperature of a building.
     * @return temperature (deg C)
     */
	//2014-10-23  Added getInitialTemperature()
    public double getInitialTemperature() {
    	//double result;
		//if (config.hasFarming(buildingType))
		if (buildingType == "Inflatable Greenhouse"
				|| buildingType == "Large Greenhouse"
				||	buildingType == "Inground Greenhouse" )
			return GREENHOUSE_TEMPERATURE;
		else
            return ROOM_TEMPERATURE;
    }
	

    /**
     * Gets the temperature of a building.
     * @return temperature (deg C)
    */
	//2014-10-17  Added getTemperature()
    public double getTemperature() {
            return currentTemperature;
    }
    
    /**
     * Sets the current temperature of a building due to heat gain
     * @return temperature (deg C)
     
    public void setTemperature(double t) {
        currentTemperature = t;
    }
    */
    
    /*
	//2014-10-17  Added getSHC() and getBLC()
    public double getSHC() {
    	return SHC;
    }
    public double getBLC() {
    	return BLC;
    }
    */
    
	/** Turn heat source off if reaching pre-setting temperature 
	 * @return none. set heatMode
	 */
	// 2014-11-02 Added checking if PowerMode.POWER_DOWN
	// TODO: also set up a time sensitivity value
	public void turnOnOffHeat() {
		double t_now = currentTemperature; //building.getTemperature();
		// if building has no power, power down the heating system
		if (building.getPowerMode() == PowerMode.POWER_DOWN)
			building.setHeatMode(HeatMode.POWER_DOWN);	
		else if (building.getPowerMode() == PowerMode.FULL_POWER) {			
			// ALLOWED_TEMP is thermostat's allowance temperature setting
		    // If T_NOW deg above INITIAL_TEMP, turn off furnace
			if (t_now > (t_initial + T_UPPER_SENSITIVITY )) {
			//if (T_NOW > T_INITIAL ) {				
				building.setHeatMode(HeatMode.POWER_DOWN);
			// If T_NOW is below INITIAL_TEMP - T_SENSITIVITY , turn on furnace 
			} else if (t_now < (t_initial - T_LOWER_SENSITIVITY)) { 
				building.setHeatMode(HeatMode.FULL_POWER);
			} //else ; // do nothing to change the HeatMode
		}
	}
	
	/**Adjust the current temperature in response to the delta temperature
	 * @return none. update currentTemperature
	 */
	public void updateTemperature() {
		currentTemperature += deltaTemperature;
		//building.setTemperature(building.getTemperature() + deltaTemperature);
	}

	
	/**
	 * Relate the change in heat to change in temperature 
	 * @return none. save result as deltaTemperature 
	 */
	//2015-02-19 Modified determineDeltaTemperature() to use MILLISOLS_PER_UPDATE
	public void determineDeltaTemperature() {
		//logger.info("determineDeltaTermperature() : In < " + building.getName() + " >");

		double outsideTemperature = Simulation.instance().getMars().getWeather().
        		getTemperature(building.getBuildingManager().getSettlement().getCoordinates());	
			//logger.info("determineDeltaTermperature() : outsideTemperature is " + outsideTemperature);
		// heatGain and heatLoss are to be converted from BTU to kJ below
		double heatGain = 0; // in BTU
		double heatGenerated; //in kJ/s
		if (building.getHeatMode() == HeatMode.FULL_POWER) {
			// HeatGenerated in kW 
			// Note: 1 kW = 3413 BTU/hr
			heatGenerated =  building.getBuildingManager().getSettlement().getThermalSystem().getGeneratedHeat();
			heatGain = elapsedTimeinHrs * heatGenerated * 3413; // in BTU/hr
		} // else if (building.getHeatMode() == HeatMode.POWER_DOWN) 
			 //heatGain = 0;
		// else heatGain = 0;
			//logger.info("determineDeltaTermperature() : heatMode is " + building.getHeatMode());
			//logger.info("determineDeltaTermperature() : heatGain is " + fmt.format(heatGain));	
		double diffTinF =  (currentTemperature - outsideTemperature) * 9D / 5D;  			
			//logger.info("determineDeltaTermperature() : BLC is " + building.getBLC());
			//logger.info("determineDeltaTermperature() : TinF is " + fmt.format(TinF));
			//logger.info("determineDeltaTermperature() : floorArea is " + floorArea);
			//logger.info("determineDeltaTermperature() : timefactor is " + fmt.format(marsSeconds * hrPerSec));
			//floorArea = this.length * this.width ;
			//logger.info("determineDeltaTermperature() : floorArea is " + floorArea);
		double heatLoss = factor_heatLoss * diffTinF;
			//logger.info("determineDeltaTermperature() : heatLoss is " + fmt.format(heatLoss));
		double changeOfTinF = ( heatGain - heatLoss) / SHC_Area ; 
		double changeOfTinC = (changeOfTinF) * 5D / 9D; // the difference between deg F and deg C (namely -32) got cancelled out 	
		setDeltaTemperature(changeOfTinC);
	}

	/**
	 * Gets the temperature change of a building due to heat gain/loss
	 * @return temperature (degree C)
	 */
	//public double getDeltaTemperature() {
	//    return deltaTemperature;
	//}
	/**
	 * Sets the change of temperature of a building due to heat gain/loss
	 * @return temperature (degree C)
	 */
	// 2014-10-17 Added setDeltaTemperature()

	public void setDeltaTemperature(double t) {
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
	// 2014-10-25 Currently skip calling for thermal control for Hallway 
	public void timePassing(double time) {
		
		//Inventory inv = getBuilding().getInventory();
		
		// Skip calling for thermal control for Hallway (coded as "virtual" building as of 3.07)
		//if (!building.getBuildingType().equals("Hallway")) 
			//System.out.println("ID: " + building.getID() + "\t" + building.getName()); 		
			adjustThermalControl();
	
	}
	
	/**
	 * Notify thermal control subsystem for the temperature change and power up and power down 
	 * via 3 steps (this method houses the main thermal control codes)
	 * @return power (kW)
	 */
	// 2014-10-25 Added adjustThermalControl()
	public void adjustThermalControl() {
		// Skip Hallway
		//if (!building.getBuildingType().equals("Hallway")) {
			
			MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();
		    int oneTenthmillisols =  (int) (clock.getMillisols() * 10);
			//System.out.println("millisols : " + millisols);
			
			if (oneTenthmillisols % ONE_TENTH_MILLISOLS_PER_UPDATE == 1) {	
				//logger.info("timePassing() : building is " + building.getName());
				

				// Detect temperature change based on heat gain and heat loss  
				// Step 2 of Thermal Control
				determineDeltaTemperature();
				
				// Adjust the current termperature 
				// Step 3 of Thermal Control
				updateTemperature();
				
				// Turn heat source off if reaching pre-setting temperature 
				// Step 1 of Thermal Control
				turnOnOffHeat();			
				

			}
		//}
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

		furnace = null;
		building = null;
	}
	/**
	 * Gets the heat this building currently requires for full-power mode.
	 * @return heat in kJ/s.
	 */
	//2014-11-02  Modified getFullHeatRequired()
	public double getFullHeatRequired()  {
		//double result = baseHeatRequirement;	
		if ( heatGeneratedCache != heatGenerated) {
			// if heatGeneratedCache is different from the its last value
			heatGeneratedCache = heatGenerated;
		//logger.info("getFullHeatRequired() : heatGenerated is updated to " + 
				//heatGenerated + " kW");	
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
		Iterator<Function> i = building.getFunctions().iterator();;
		while (i.hasNext()) result += i.next().getPoweredDownHeatRequired();

		return result;
	}


	@Override
	public double getMaintenanceTime() {
		// TODO Auto-generated method stub
		return 0;
	}
}