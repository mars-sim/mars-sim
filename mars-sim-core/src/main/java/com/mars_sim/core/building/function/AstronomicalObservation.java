/*
 * Mars Simulation Project
 * AstronomicalObservation.java
 * @date 2025-07-29
 * @author Sebastien Venot
 */
package com.mars_sim.core.building.function;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.config.BuildingConfig;
import com.mars_sim.core.building.config.FunctionSpec;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.structure.Settlement;

/**
 * A building function for observing astronomical objects.
 */
public class AstronomicalObservation extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final Logger logger = Logger.getLogger(AstronomicalObservation.class.getName());

	// Data members
	private double fullPowerLoad;
	private int techLevel;
	private int observatoryCapacity;
	private int observerNum;

	/**
	 * Constructor.
	 * 
	 * @param building the building the function is for.
	 * @param spec Function details
	 * @throws BuildingException if error creating building function.
	 */
	public AstronomicalObservation(Building building, FunctionSpec spec) {
		// Use function constructor.
		super(FunctionType.ASTRONOMICAL_OBSERVATION, spec, building);

		fullPowerLoad = spec.getDoubleProperty(BuildingConfig.POWER);
		techLevel = spec.getTechLevel();
		observatoryCapacity = spec.getCapacity();
	}

	/**
	 * Gets the value of the function for a named building type.
	 * 
	 * @param type  the building type.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String type, boolean newBuilding, Settlement settlement) {
		// Note: do use getNumCitizens() since observatoryCapacity below will be used
		double demand =  settlement.getNumCitizens();
		
		double supply = settlement.getAllAssociatedPeople().stream()
				.mapToDouble(p -> p.getSkillManager().getSkillLevel(SkillType.ASTRONOMY))
				.sum();
		
		boolean removedBuilding = false;
		for (Building building : settlement.getBuildingManager().getObservatories()) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(type) && !removedBuilding) {
				removedBuilding = true;
			} else {
				AstronomicalObservation astroFunction = building.getAstronomicalObservation();
				int techLevel = astroFunction.getTechnologyLevel();
				int observatoryCapacity = astroFunction.getObservatoryCapacity();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += techLevel * observatoryCapacity * wearModifier;
			}
		}
		
		return demand / (supply + 1);
	}

	/**
	 * Gets the value of this function.
	 * 
	 * @return value (VP) of building function.
	 */
	public double getFunctionValue() {
		
		Settlement s = getSettlement();
		// Note: do use getNumCitizens() since observatoryCapacity below will be used
		double demand =  s.getNumCitizens();
		
		double supply = getSettlement().getAllAssociatedPeople().stream()
				.mapToDouble(p -> p.getSkillManager().getSkillLevel(SkillType.ASTRONOMY))
				.sum();	
		
		supply += techLevel * observatoryCapacity;
		
		return demand / (supply + 1);
	}
	
	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	@Override
	public double getFullPowerLoad() {
		return fullPowerLoad;
	}

	/**
	 * Adds a new observer to the observatory.
	 * 
	 * @return If there is space
	 */
	public boolean addObserver() {
		observerNum++;
		if (observerNum > observatoryCapacity) {
			observerNum = observatoryCapacity;
			logger.log(Level.SEVERE, "addObserver(): " + "Observatory is already full of observers.");
			return false;
		}
		return true;
	}

	/**
	 * Removes an observer from the observatory.
	 * 
	 * @throws Exception if no observers currently in observatory.
	 */
	public void removeObserver() {
		observerNum--;
		if (observerNum < 0) {
			observerNum = 0;
			logger.log(Level.SEVERE, "removeObserver(): " + "Observatory is already empty of observers.");
			throw new IllegalStateException("Observatory is already empty of observers.");
		}
	}

	/**
	 * Gets the current number of observers in the observatory.
	 * 
	 * @return number of observers.
	 */
	public int getObserverNum() {
		return observerNum;
	}

	/**
	 * Gets the capacity for observers in the observatory.
	 * 
	 * @return capacity.
	 */
	public int getObservatoryCapacity() {
		return observatoryCapacity;
	}

	/**
	 * Gets the technology level of the observatory.
	 * 
	 * @return technology level.
	 */
	public int getTechnologyLevel() {
		return techLevel;
	}

	@Override
	public double getMaintenanceTime() {

		double result = fullPowerLoad;

		// Add maintenance for tech level.
		result *= techLevel * .5;

		// Add maintenance for observer capacity.
		result *= observatoryCapacity * .25;

		return result;
	}
}
