/**
 * Mars Simulation Project
 * LifeSupport.java
 * @version 3.07 2014-10-17
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
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
    //2014-10-17 mkung: Added the heating requirement
	//private double heatRequired;
	
	
	//2014-10-23 mkung: Added temperature setting */
    // How often to check on temperature change
    private static int tally; 
    // CAUTION: DO NOT SET TICKS_PER_UPDATE to a multiple of n if there are n building with life-support
    private static final double TICKS_PER_UPDATE = 4.0; // USE CAUTION: may skip update of certain buildings  
  	private static int count;
    // thermostat's allowance temperature setting
    // if T_SENSITIVITY is set to 2.0, 
  	// furnace ON when 2 deg below INITIAL_TEMP
    // furnace OFF when 2 deg above INITIAL_TEMP
    private static final double T_SENSITIVITY = 2D; 
  	protected HeatMode heatMode;
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

	/**
	 * Constructor.
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public LifeSupport(Building building) {
		// Call Function constructor.
		super(FUNCTION, building);

		occupants = new ConcurrentLinkedQueue<Person>();

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

		// Set occupant capacity.
		occupantCapacity = config.getLifeSupportCapacity(building.getName());

		// Set life support power required.
		powerRequired = config.getLifeSupportPowerRequirement(building.getName());

	    //2014-10-17 mkung: Added the heating requirement
		// Set life support heating required.
		//heatRequired = config.getLifeSupportHeatRequirement(building.getName());

		//2014-10-23 mkung: new initial values */
		count++;
		//logger.info("constructor : count is " + count);
		this.building = building;
		heatMode = HeatMode.NO_POWER;	
		deltaTemperature = 0;

		length = getBuilding().getLength();
		width = getBuilding().getWidth() ;
		name =  getBuilding().getName();
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
		this.occupantCapacity = occupantCapacity;
		this.powerRequired = powerRequired;
		
		//2014-10-23 mkung: new initial values */
		count++;
		//logger.info("constructor : count is " + count);
		this.building = building;
		heatMode = HeatMode.NO_POWER;	
		deltaTemperature = 0;
		length = getBuilding().getLength();
		width = getBuilding().getWidth() ;
		name =  getBuilding().getName();
		floorArea = length * width ;
		//logger.info("constructor : " + name + " is " + length + " x " + width);

	}


	// Turn heat source off if reaching pre-setting temperature 
	public void turnOnOffHeat() {
		double t = building.getInitialTemperature();
			//logger.info("t is " + t);
		// ALLOWED_TEMP is thermostat's allowance temperature setting
	    // If 3 deg above INITIAL_TEMP, turn off furnace
		if (building.getTemperature() > (t + T_SENSITIVITY )) {
			//logger.info("turnOnOffHeat() : TOO HOT!!! Temperature is "+ fmt.format(currentTemperature));
			setHeatMode(HeatMode.POWER_DOWN);
		// If 3 deg below INITIAL_TEMP, turn on furnace 
		} else if (building.getTemperature() < (t - T_SENSITIVITY)) { 
			setHeatMode(HeatMode.FULL_POWER);
			//logger.info("turnOnOffHeat() : TOO COLD!!! Temperature is "+ fmt.format(currentTemperature));
		}
	}
	
	/**Adjust the current temperature in response to the delta temperature
	 * @return none. update currentTemperature
	 */
	// 
	public void updateTemperature() {
		//currentTemperature += deltaTemperature;
		building.setTemperature(building.getTemperature()+deltaTemperature);
			//logger.info("timePassing() : updated currentTemp is "+ fmt.format(building.getTemperature()));
			//logger.info("timePassing() : updated deltaTemperature is "+ fmt.format(deltaTemperature));		
	}

	
	/**
	 * Relate the change in heat to change in temperature 
	 * @return none. save result as deltaTemperature 
	 */
	//2014-10-17 mkung: Added edetermineDeltaTemperature() 
	@SuppressWarnings("deprecation")
	public void determineDeltaTemperature() {
		//logger.info("determineDeltaTermperature() : In building < " + building.getName() + " >");
		//TODO: compute elapsedTime using MarsClock.getTimeDiff(clock1, clock2)
		//double heatLoss = 0;
		double meter2Feet = 10.764;
		double interval = Simulation.instance().getMasterClock().getTimePulse() ;
		// 1 hour = 3600 sec , 1 sec = (1/3600) hrs
		// 1 sol on Mars has 88740 secs
		// 1 sol has 1000 milisol
		double marsSeconds = 1000.0/88740.0*interval; 
		double secPerHr = 1.0/3600.0;
		//logger.info("interval : " + fmt.format(interval));
		//logger.info("marsSeconds : " + fmt.format(marsSeconds));
		//logger.info("secPerHr : " + fmt.format(secPerHr )); 

		// TODO: the outside Temperature varies from morning to evening
		double outsideTemperature = Simulation.instance().getMars().getWeather().
        		getTemperature(building.getBuildingManager().getSettlement().getCoordinates());	
			//logger.info("determineDeltaTermperature() : outsideTemperature is " + outsideTemperature);
		// heatGain and heatLoss are [in Joules]
		double heatGain;
		if (heatMode == HeatMode.FULL_POWER) {
			heatGain =  building.getBuildingManager().getSettlement().getThermalSystem().getGeneratedHeat();
			heatGain = heatGain * TICKS_PER_UPDATE;
		}
		else {
			heatGain = 0;
		}
		//logger.info("determineDeltaTermperature() : heatMode is " + heatMode);
		//logger.info("determineDeltaTermperature() : heatGain is " + fmt.format(heatGain));	

		double TinF =  (building.getTemperature() - outsideTemperature)*1.8; //-32 drops out			
			//logger.info("determineDeltaTermperature() : BLC is " + building.getBLC());
			//logger.info("determineDeltaTermperature() : TinF is " + fmt.format(TinF));
			//logger.info("determineDeltaTermperature() : floorArea is " + floorArea);
			//logger.info("determineDeltaTermperature() : timefactor is " + fmt.format(marsSeconds * secPerHr));
		//floorArea = this.length * this.width ;
			//logger.info("determineDeltaTermperature() : floorArea is " + floorArea);
		double heatLoss = TICKS_PER_UPDATE * building.getBLC() * floorArea * meter2Feet * marsSeconds * secPerHr * TinF;
			//logger.info("determineDeltaTermperature() : heatLoss is " + fmt.format(heatLoss));
		double deltaTinF = ( heatGain - heatLoss) / (building.getSHC() * floorArea); 
			//logger.info("determineDeltaTermperature() : deltaTinF is " + fmt.format(deltaTinF));
		double deltaTinC = (deltaTinF) *5.0/9.0; // -32 drops out
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
	//2014-10-17 mkung: Added setDeltaTemperature()
	public void setDeltaTemperature(double t) {
	    deltaTemperature = t;
	}


	/**
	 * Gets the building's power mode.
	 */
	//2014-10-17 mkung: Added heat mode
	public HeatMode getHeatMode() {
		return heatMode;
	}

	/**
	 * Sets the building's heat mode.
	 */
	//2014-10-17 mkung: Added heat mode
	public void setHeatMode(HeatMode heatMode) {
		this.heatMode = heatMode;
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
			if (!newBuilding && building.getName().equalsIgnoreCase(buildingName) && !removedBuilding) {
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
	 * Checks if the building contains a particular person.
	 * @return true if person is in building.
	 */
	public boolean containsPerson(Person person) {
		return occupants.contains(person);
	}

	/**
	 * Gets a collection of occupants in the building.
	 * @return collection of occupants
	 */
	public Collection<Person> getOccupants() {
		return new ConcurrentLinkedQueue<Person>(occupants);
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
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {

		//logger.info("timePassing() : building is " + building.getName());
		// Make sure all occupants are actually in settlement inventory.
		// If not, remove them as occupants.
		Inventory inv = getBuilding().getInventory();
		Iterator<Person> i = occupants.iterator();
		while (i.hasNext()) {
			if (!inv.containsUnit(i.next())) i.remove();
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

		double miliSolElapsed = Simulation.instance().getMasterClock().getTimePulse() ;
		//logger.info("timePassing() : TimePulse is " + miliSolElapsed);
		tally++;
		// TICKS_PER_UPDATE denote how frequent in updating the delta temperature
		if (tally == (int)TICKS_PER_UPDATE) {
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

	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return powerRequired;
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