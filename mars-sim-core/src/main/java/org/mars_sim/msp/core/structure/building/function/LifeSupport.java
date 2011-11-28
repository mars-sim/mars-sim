/**
 * Mars Simulation Project
 * LifeSupport.java
 * @version 3.02 2011-11-26
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The LifeSupport class is a building function for life support and managing inhabitants.
 */
public class LifeSupport extends Function implements Serializable {
    
	private static String CLASS_NAME = 
	    "org.mars_sim.msp.simulation.structure.building.function.LifeSupport";
	
	private static Logger logger = Logger.getLogger(CLASS_NAME);

	public static final String NAME = "Life Support";
	
	// Data members
	private int occupantCapacity;
	private double powerRequired;
	private Collection<Person> occupants;

	/**
	 * Constructor
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
	 */
	public LifeSupport(Building building) {
		// Call Function constructor.
		super(NAME, building);
		
		occupants = new ConcurrentLinkedQueue<Person>();
		
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		
//		try {
			// Set occupant capacity.
			occupantCapacity = config.getLifeSupportCapacity(building.getName());
		
			// Set life support power required.
			powerRequired = config.getLifeSupportPowerRequirement(building.getName());
//		}
//		catch (Exception e) {
//			throw new BuildingException("LifeSupport.constructor: " + e.getMessage());
//		}
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
		super(NAME, building);
		
		occupants = new ConcurrentLinkedQueue<Person>();
		this.occupantCapacity = occupantCapacity;
		this.powerRequired = powerRequired;
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
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(NAME).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding && building.getName().equalsIgnoreCase(buildingName) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                LifeSupport lsFunction = (LifeSupport) building.getFunction(NAME);
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
				if (building.hasFunction(NAME)) {
					LifeSupport lifeSupport = (LifeSupport) building.getFunction(NAME);
					if (lifeSupport.containsPerson(person)) lifeSupport.removePerson(person);
				}
			}

			// Add person to this building.            
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
		if (occupants.contains(occupant)) occupants.remove(occupant);
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
	public double getPowerDownPowerRequired() {
		return 0;
	}
	
	@Override
	public void destroy() {
	    super.destroy();
	    
	    occupants.clear();
	    occupants = null;
	}
}