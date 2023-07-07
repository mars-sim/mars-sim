/*
 * Mars Simulation Project
 * LifeSupport.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.air.AirComposition;
import org.mars_sim.msp.core.data.UnitSet;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsTime;

/**
 * The LifeSupport class is a building function for life support and managing
 * inhabitants.
 */
public class LifeSupport extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final Logger logger = Logger.getLogger(LifeSupport.class.getName());

	// Data members
	private int occupantCapacity;

	private double powerRequired;
	private double length;
	private double width;
	protected double floorArea;

	private Collection<Person> occupants;

	private AirComposition air;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @param spec Defiens the Life support capability
	 */
	public LifeSupport(Building building, FunctionSpec spec) {
		super(FunctionType.LIFE_SUPPORT, spec, building);

		occupants = new UnitSet<>();

		this.occupantCapacity = spec.getCapacity();
		this.powerRequired = spec.getDoubleProperty(BuildingConfig.POWER_REQUIRED);

		length = building.getLength();
		width = building.getWidth();
		floorArea = length * width;

		double t = AirComposition.C_TO_K + building.getCurrentTemperature();
		double vol = building.getVolumeInLiter(); // 1 Cubic Meter = 1,000 Liters
		air = new AirComposition(t, vol);
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingType the building type.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingType, boolean newBuilding, Settlement settlement) {

		// Demand is 2 occupant capacity for every inhabitant.
		double demand = settlement.getNumCitizens() * 2D;

		double supply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.LIFE_SUPPORT).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingType) && !removedBuilding) {
				removedBuilding = true;
			} else {
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += building.getLifeSupport().occupantCapacity * wearModifier;
			}
		}

		double occupantCapacityValue = demand / (supply + 1D);

		FunctionSpec spec = buildingConfig.getFunctionSpec(buildingType, FunctionType.LIFE_SUPPORT);
		int occupantCapacity = spec.getCapacity();

		double result = occupantCapacity * occupantCapacityValue;

		// Subtract power usage cost per sol.
		double power = spec.getDoubleProperty(BuildingConfig.POWER_REQUIRED);
		double powerPerSol = power * MarsTime.HOURS_PER_MILLISOL * 1000D;
		double powerValue = powerPerSol * settlement.getPowerGrid().getPowerValue() / 1000D;
		result -= powerValue;

		if (result < 0D)
			result = 0D;

		return result;
	}

	/**
	 * Gets the building's capacity for supporting occupants.
	 * 
	 * @return number of inhabitants.
	 */
	public int getOccupantCapacity() {
		return occupantCapacity;
	}

	/**
	 * Gets the current number of occupants in the building.
	 * 
	 * @return occupant number
	 */
	public int getOccupantNumber() {
		return occupants.size();
	}

	/**
	 * Gets the available occupancy room.
	 * 
	 * @return occupancy room
	 */
	public int getAvailableOccupancy() {
		int available = occupantCapacity - getOccupantNumber();
        return Math.max(available, 0);
	}

	/**
	 * Checks if the building contains a particular unit.
	 * 
	 * @return true if unit is in building.
	 */
	public boolean containsOccupant(Person person) {
		return occupants.contains(person);
	}

	/**
	 * Gets a collection of occupants in the building.
	 * 
	 * @return collection of occupants
	 */
	public Collection<Person> getOccupants() {
		return occupants;
	}

	/**
	 * Adds a person to the building. Note: building occupant capacity can be
	 * exceeded but stress levels in the building will increase. (todo: add stress
	 * later)
	 * 
	 * @param person new person to add to building.
	 */
	public void addPerson(Person person) {
		if (!occupants.contains(person)) {
			// Remove person from any other inhabitable building in the settlement.
			Iterator<Building> i = building.getBuildingManager().getBuildingSet().iterator(); 
			while (i.hasNext()) {
				Building building = i.next();
				if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
					// remove this person from the old building first
					BuildingManager.removePersonFromBuilding(person, building);
				}
			}
			
			// Add person to this building.
			occupants.add(person);
			logger.finest("Adding " + person + " to " + building + " life support.");
		} else {
			throw new IllegalStateException("Person already occupying building.");
		}
	}

	/**
	 * Removes a person from the building.
	 * 
	 * @param occupant the person to remove from building.
	 */
	public void removePerson(Person occupant) {
		if (occupants.contains(occupant)) {
			occupants.remove(occupant);
			logger.finest("Removing " + occupant + " from " + building + " life support.");
		} else {
			throw new IllegalStateException("Person does not occupy building.");
		}
	}

	/**
	 * Time passing for the building.
	 * 
	 * @param time amount of time passing (in millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			if (occupants != null && !occupants.isEmpty()) {
				// Make sure all occupants are actually in settlement inventory.
				// If not, remove them as occupants.
				Iterator<Person> i = occupants.iterator();
				while (i.hasNext()) {
					if (!building.getSettlement().containsPerson(i.next()))
						i.remove();
				}
			}
	
			// Add stress if building is overcrowded.
			int overcrowding = getOccupantNumber() - occupantCapacity;
			if (overcrowding > 0) {
	
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest("Overcrowding at " + building);
				}
	
				double stressModifier = .1D * overcrowding * pulse.getElapsed();
	
				if (occupants != null) {
					Iterator<Person> j = getOccupants().iterator();
					while (j.hasNext()) {
						j.next().getPhysicalCondition().addStress(stressModifier);
					}
				}
			}

			// Update Air
			air.timePassing(building, pulse);
		}
		return valid;
	}

	/**
	 * Get details about the composition of the air
	 * @return
	 */
	public AirComposition getAir() {
		return air;
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	@Override
	public double getFullPowerRequired() {
		return powerRequired;
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

}
