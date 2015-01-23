/**
 * Mars Simulation Project
 * LifeSupport.java
 * @version 3.07 2015-01-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The LifeSupport class is a building function for life support and managing inhabitants.
 */
public class LifeSupport
extends Function
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(LifeSupport.class.getName());
	/** default logger. */

	DecimalFormat fmt = new DecimalFormat("#.#######"); 
	
	private static final BuildingFunction FUNCTION = BuildingFunction.LIFE_SUPPORT;

	// Data members
	private int occupantCapacity;
	private double powerRequired;
    //2014-10-17 Added the heating requirement
	private double heatRequired;
	
	
	//2014-10-23 Added temperature setting */
    // How often to check on temperature change
    private static int tally; 
    // CAUTION: DO NOT SET TICKS_PER_UPDATE to a multiple of N if there are N buildings with life-support function
    // set TICKS_PER_UPDATE to a default of 5 for a MD1 base. 
    // The bigger the number, the more erratic (and the less frequent) the temperature update
    private static final int TICKS_PER_UPDATE = 9; 
  	//private static int count;
    // thermostat's allowance temperature setting
    // if T_SENSITIVITY is set to 2.0, 
  	// furnace ON when 2 deg below INITIAL_TEMP
    // furnace OFF when 2 deg above INITIAL_TEMP
    private static final double T_SENSITIVITY = 1D; 
  	protected double baseHeatRequirement;
  	protected double basePowerDownHeatRequirement;
  	private double length;
  	private double width ;
  	private String name;
  	protected double floorArea;
  	protected double currentTemperature;
  	protected double deltaTemperature;
  	protected ThermalGeneration furnace;
	private Building building;

	private Collection<Person> occupants;
	private Collection<Robot> robots;

	/**
	 * Constructor.
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public LifeSupport(Building building) {
		// Call Function constructor.
		super(FUNCTION, building);

		occupants = new ConcurrentLinkedQueue<Person>();
		robots = new ConcurrentLinkedQueue<Robot>();

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

		// Set occupant capacity.
		occupantCapacity = config.getLifeSupportCapacity(building.getBuildingType());

		//2014-10-17 Added the heating requirement
		// Set life support heating required.
		//heatRequired = config.getLifeSupportHeatRequirement(building.getName());

		// Set life support power required.
		powerRequired = config.getLifeSupportPowerRequirement(building.getBuildingType());
	    
		//2014-10-23 new initial values */
		//count++;
		//logger.info("constructor : count is " + count);
		this.building = building;
		deltaTemperature = 0;

		length = getBuilding().getLength();
		width = getBuilding().getWidth() ;
		name =  getBuilding().getBuildingType();
		floorArea = length * width ;
		//logger.info("constructor : " + name + " is " + length + " x " + width);

	}

	/**
	 * Alternate constructor with given occupant capacity and power required.
	 * @param building the building this function is for.
	 * @param occupantCapacity the number of occupants this building can hold.
	 * @param powerRequired the power required (kW)
	 * @throws BuildingException if error constructing function.
	 */
	public LifeSupport(Building building, int occupantCapacity, double powerRequired) {
		// Use Function constructor
		super(FUNCTION, building);

		occupants = new ConcurrentLinkedQueue<Person>();
		robots = new ConcurrentLinkedQueue<Robot>();

		this.occupantCapacity = occupantCapacity;
		this.powerRequired = powerRequired;
		
		//2014-10-23 new initial values */
		//count++;
		//logger.info("constructor : count is " + count);
		this.building = building;
		deltaTemperature = 0;
		length = getBuilding().getLength();
		width = getBuilding().getWidth() ;
		name =  getBuilding().getBuildingType();
		floorArea = length * width ;
		//logger.info("constructor : " + name + " is " + length + " x " + width);

	}

	/** Turn heat source off if reaching pre-setting temperature 
	 * @return none. set heatMode
	 */
	// 2014-11-02 Added checking if PowerMode.POWER_DOWN
	// TODO: also set up a time sensitivity value
	public void turnOnOffHeat() {
		double T_INITIAL = building.getInitialTemperature();
		double T_NOW = building.getTemperature();
		// if building has no power, power down the heating system
		if (building.getPowerMode() == PowerMode.POWER_DOWN)
			building.setHeatMode(HeatMode.POWER_DOWN);	
		else if (building.getPowerMode() == PowerMode.FULL_POWER) {			
			// ALLOWED_TEMP is thermostat's allowance temperature setting
		    // If T_SENSITIVITY deg above INITIAL_TEMP, turn off furnace
			if (T_NOW > (T_INITIAL + T_SENSITIVITY )) {
				building.setHeatMode(HeatMode.POWER_DOWN);
			// If T_SENSITIVITY deg below INITIAL_TEMP, turn on furnace 
			} else if (T_NOW < (T_INITIAL - T_SENSITIVITY)) { 
				building.setHeatMode(HeatMode.FULL_POWER);
			} else ; // do nothing to change the HeatMode
		}
	}
	
	/**Adjust the current temperature in response to the delta temperature
	 * @return none. update currentTemperature
	 */
	public void updateTemperature() {
		//currentTemperature += deltaTemperature;
		building.setTemperature(building.getTemperature() + deltaTemperature);
	}

	
	/**
	 * Relate the change in heat to change in temperature 
	 * @return none. save result as deltaTemperature 
	 */
	//2014-10-17 Added determineDeltaTemperature() 

	public void determineDeltaTemperature() {
		//logger.info("determineDeltaTermperature() : In < " + building.getName() + " >");
		double meter2Feet = 10.764;
		double interval = Simulation.instance().getMasterClock().getTimePulse() ;
		// 1 hour = 3600 sec , 1 sec = (1/3600) hrs
		// 1 sol on Mars has 88740 secs
		// 1 sol has 1000 milisol
		double marsSeconds = 1000.0/88740.0 * interval; 
		double hrPerSec = 1.0/3600.0;
		//logger.info("interval : " + fmt.format(interval));
		//logger.info("marsSeconds : " + fmt.format(marsSeconds));
		//logger.info("hrPerSec : " + fmt.format(hrPerSec)); 

		// TODO: the outside Temperature varies from morning to evening
		double outsideTemperature = Simulation.instance().getMars().getWeather().
        		getTemperature(building.getBuildingManager().getSettlement().getCoordinates());	
			//logger.info("determineDeltaTermperature() : outsideTemperature is " + outsideTemperature);
		// heatGain and heatLoss are kJ
		double heatGain; // in BTU
		double heatGenerated; //in kJ/s
		if (building.getHeatMode() == HeatMode.FULL_POWER) {
			// HeatGenerated in kW 
			// Note: 1 kW = 3413 BTU/hr
			heatGenerated =  building.getBuildingManager().getSettlement().getThermalSystem().getGeneratedHeat();
			heatGain = heatGenerated * 3413; // in BTU/hr
			heatGain = heatGain * (double)TICKS_PER_UPDATE * marsSeconds * hrPerSec;		
		} else if (building.getHeatMode() == HeatMode.POWER_DOWN) 
			heatGain = 0;
		else 
			heatGain = 0;
			//logger.info("determineDeltaTermperature() : heatMode is " + building.getHeatMode());
			//logger.info("determineDeltaTermperature() : heatGain is " + fmt.format(heatGain));	
		double TinF =  (building.getTemperature() - outsideTemperature)*1.8; //-32 drops out			
			//logger.info("determineDeltaTermperature() : BLC is " + building.getBLC());
			//logger.info("determineDeltaTermperature() : TinF is " + fmt.format(TinF));
			//logger.info("determineDeltaTermperature() : floorArea is " + floorArea);
			//logger.info("determineDeltaTermperature() : timefactor is " + fmt.format(marsSeconds * hrPerSec));
			//floorArea = this.length * this.width ;
			//logger.info("determineDeltaTermperature() : floorArea is " + floorArea);
		double heatLoss = (double)TICKS_PER_UPDATE * building.getBLC() * floorArea * meter2Feet * marsSeconds * hrPerSec * TinF;
			//logger.info("determineDeltaTermperature() : heatLoss is " + fmt.format(heatLoss));
		double deltaTinF = ( heatGain - heatLoss) / (building.getSHC() * floorArea); 
			//logger.info("determineDeltaTermperature() : deltaTinF is " + fmt.format(deltaTinF));
		double deltaTinC = (deltaTinF) *5.0/9.0; // the difference between deg F and deg C (namely -32) got cancelled out 
			//logger.info("determineDeltaTermperature() : deltaTinC is " + fmt.format(deltaTinC));		
		setDeltaTemperature(deltaTinC);
	}

	/**
	 * Gets the temperature change of a building due to heat gain
	 * @return temperature (deg C)
	 */
	public double getDeltaTemperature() {
	    return deltaTemperature;
	}
	/**
	 * Sets the chage of temperature of a building due to heat gain
	 * @return temperature (degrees C)
	 */
	//2014-10-17 Added setDeltaTemperature()
	public void setDeltaTemperature(double t) {
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

		// Demand is 2 occupant capacity for every inhabitant. 
		double demand = settlement.getAllAssociatedPeople().size() * 2D;

		double supply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			}
			else {
				LifeSupport lsFunction = (LifeSupport) building.getFunction(FUNCTION);
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += lsFunction.occupantCapacity * wearModifier;
			}
		}

		double occupantCapacityValue = demand / (supply + 1D);

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		double occupantCapacity = config.getLifeSupportCapacity(buildingName);

		double result = occupantCapacity * occupantCapacityValue;

		// Subtract power usage cost per sol.
		double power = config.getLifeSupportPowerRequirement(buildingName);
		double hoursInSol = MarsClock.convertMillisolsToSeconds(1000D) / 60D / 60D;
		double powerPerSol = power * hoursInSol;
		double powerValue = powerPerSol * settlement.getPowerGrid().getPowerValue() / 1000D;
		result -= powerValue;

		if (result < 0D) result = 0D;

		return result;
	}

	/**
	 * Gets the building's capacity for supporting occupants.
	 * @return number of inhabitants.
	 */
	public int getOccupantCapacity() {
		return occupantCapacity;
	}

	/**
	 * Gets the current number of occupants in the building.
	 * @return occupant number
	 */
	public int getOccupantNumber() {
		return occupants.size();
	}

	/**
	 * Gets the available occupancy room.
	 * @return occupancy room
	 */
	public int getAvailableOccupancy() {
		int available = occupantCapacity - getOccupantNumber();
		if (available > 0) return available;
		else return 0;
	}

	/**
	 * Checks if the building contains a particular unit.
	 * @return true if unit is in building.
	 */
	public boolean containsOccupant(Unit unit) {
		boolean result = false;
        Person person = null;
        Robot robot = null;     
        if (unit instanceof Person) {
         	person = (Person) unit;
         	result = occupants.contains(person);
        }
        else if (unit instanceof Robot) {
        	robot = (Robot) unit;
         	result = occupants.contains(robot);
        }
		return result;
	}

	/**
	 * Gets a collection of occupants in the building.
	 * @return collection of occupants
	 */
	public Collection<Person> getOccupants() {
		return new ConcurrentLinkedQueue<Person>(occupants);
	}
	
	/**
	 * Gets a collection of occupants in the building.
	 * @return collection of occupants
	 */
	public Collection<Robot> getRobots() {
		return new ConcurrentLinkedQueue<Robot>(robots);
	}

	
	/**
	 * Adds a person to the building.
	 * Note: building occupant capacity can be exceeded but stress levels
	 * in the building will increase. 
	 * (todo: add stress later)
	 * @param person new person to add to building.
	 * @throws BuildingException if person is already building occupant.
	 */
	public void addPerson(Person person) {
		if (!occupants.contains(person)) {
			// Remove person from any other inhabitable building in the settlement.
			Iterator<Building> i = getBuilding().getBuildingManager().getBuildings().iterator();
			while (i.hasNext()) {
				Building building = i.next();
				if (building.hasFunction(FUNCTION)) {
					BuildingManager.removePersonFromBuilding(person, building);
				}
			}

			// Add person to this building.
			logger.finest("Adding " + person + " to " + getBuilding() + " life support.");
			occupants.add(person);
		}
		else {
			throw new IllegalStateException("Person already occupying building.");
		} 
	}

	/**
	 * Removes a person from the building.
	 * @param occupant the person to remove from building.
	 * @throws BuildingException if person is not building occupant.
	 */
	public void removePerson(Person occupant) {
		if (occupants.contains(occupant)) {
		    occupants.remove(occupant);
		    logger.finest("Removing " + occupant + " from " + getBuilding() + " life support.");
		}
		else {
			throw new IllegalStateException("Person does not occupy building.");
		} 
	}
	/**
	 * Adds a robot to the building.
	 * Note: robot capacity can be exceeded 
	 * @param robot new robot to add to building.
	 * @throws BuildingException if robot is already building occupant.
	 */
	public void addRobot(Robot robot) {
		if (!robots.contains(robot)) {
			// Remove robot from any other inhabitable building in the settlement.
			Iterator<Building> i = getBuilding().getBuildingManager().getBuildings().iterator();
			while (i.hasNext()) {
				Building building = i.next();
				if (building.hasFunction(FUNCTION)) {
					BuildingManager.removePersonFromBuilding(robot, building);
				}
			}

			// Add robot to this building.
			logger.finest("Adding " + robot + " to " + getBuilding() + " life support.");
			robots.add(robot);
		}
		else {
			throw new IllegalStateException("This robot is already in this building.");
		} 
	}

	/**
	 * Removes a robot from the building.
	 * @param occupant the robot to remove from building.
	 * @throws BuildingException if robot is not building occupant.
	 */
	public void removeRobot(Robot robot) {
		if (robots.contains(robot)) {
			robots.remove(robot);
		    logger.finest("Removing " + robot + " from " + getBuilding() + " life support.");
		}
		else {
			throw new IllegalStateException("The robot is not in this building.");
		} 
	}

	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	// 2014-10-25 Currently skip calling for thermal control for Hallway 
	public void timePassing(double time) {
		//logger.info("timePassing() : building is " + building.getName());
		// Make sure all occupants are actually in settlement inventory.
		// If not, remove them as occupants.
		Inventory inv = getBuilding().getInventory();

		Iterator<Person> i = occupants.iterator();
		while (i.hasNext()) {
			if (!inv.containsUnit(i.next())) i.remove();
		}

		Iterator<Robot> jj = robots.iterator();
		while (jj.hasNext()) {
			if (!inv.containsUnit(jj.next())) jj.remove();
		}

		// Add stress if building is overcrowded.
		int overcrowding = getOccupantNumber() - occupantCapacity;
		if (overcrowding > 0) {

			if(logger.isLoggable(Level.FINEST)){
				logger.finest("Overcrowding at " + getBuilding());
			}
			double stressModifier = .1D * overcrowding * time;
			Iterator<Person> j = getOccupants().iterator();
			while (j.hasNext()) {
				PhysicalCondition condition = j.next().getPhysicalCondition();
				condition.setStress(condition.getStress() + stressModifier);
			}
		}	

		
		// skip calling for thermal control for Hallway (coded as "virtual" building as of 3.07)
		// make sure it calls out buildingType, NOT calling out getNickName()
		if (!building.getBuildingType().equals("Hallway")) 
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
		if (!building.getBuildingType().equals("Hallway")) {
			double miliSolElapsed = Simulation.instance().getMasterClock().getTimePulse() ;
			//logger.info("timePassing() : TimePulse is " + miliSolElapsed);
			tally++;
			// TICKS_PER_UPDATE denote how frequent in updating the delta temperature
			if (tally == TICKS_PER_UPDATE) {
				//logger.info("timePassing() : building is " + building.getName());
				
				// Turn heat source off if reaching pre-setting temperature 
				// Step 1 of Thermal Control
				turnOnOffHeat();			
				// Detect temperature change based on heat gain and heat loss  
				// Step 2 of Thermal Control
				determineDeltaTemperature();
				// Adjust the current termperature 
				// Step 3 of Thermal Control
				updateTemperature();

				tally = 0;
			}
		}
	}
	

	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return (powerRequired + heatRequired);
	}
	
	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {
		return 0;
	}

	@Override
	public double getMaintenanceTime() {
		return occupantCapacity * 10D;
	}

	@Override
	public void destroy() {
		super.destroy();

		occupants.clear();
		occupants = null;
	}

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}
}