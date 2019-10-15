/**
 * Mars Simulation Project
 * AstronomicalObservation.java
 * @version 3.1.0 2018-10-26
 * @author Sebastien Venot
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * A building function for observing astronomical objects.
 */
public class AstronomicalObservation extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(AstronomicalObservation.class.getName());

	private static final FunctionType FUNCTION = FunctionType.ASTRONOMICAL_OBSERVATIONS;

	// Data members
	private double powerRequired;
	private int techLevel;
	private int observatoryCapacity;
	private int observerNum;

	/**
	 * Constructor.
	 * 
	 * @param building the building the function is for.
	 * @throws BuildingException if error creating building function.
	 */
	public AstronomicalObservation(Building building) {
		// Use function constructor.
		super(FUNCTION, building);

		powerRequired = buildingConfig.getAstronomicalObservationPowerRequirement(building.getBuildingType());
		techLevel = buildingConfig.getAstronomicalObservationTechLevel(building.getBuildingType());
		observatoryCapacity = buildingConfig.getAstronomicalObservationCapacity(building.getBuildingType());

		// Load activity spots
		loadActivitySpots(buildingConfig.getAstronomicalObservationActivitySpots(building.getBuildingType()));
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return powerRequired;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * 
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {
		return 0;
	}

	/**
	 * Time passing for the building.
	 * 
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {
		// Do nothing
	}

	/**
	 * Adds a new observer to the observatory.
	 * 
	 * @throws Exception if observatory is already at capacity.
	 */
	public void addObserver() {
		observerNum++;
		if (observerNum > observatoryCapacity) {
			observerNum = observatoryCapacity;
			logger.log(Level.SEVERE, "addObserver(): " + "Observatory is already full of observers.");
			throw new IllegalStateException("Observatory is already full of observers.");
		}
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

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {

		double observatoryDemand = 0D;
		ScienceType astronomyScience = ScienceType.ASTRONOMY;

		// Determine settlement demand for astronomical observatories.
		SkillType astronomySkill = astronomyScience.getSkill();
		Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
		while (j.hasNext()) {
			observatoryDemand += j.next().getSkillManager().getSkillLevel(astronomySkill);
		}

		// Determine existing settlement supply of astronomical observatories.
		double observatorySupply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> k = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
		while (k.hasNext()) {
			Building building = k.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				AstronomicalObservation astroFunction = building.getAstronomicalObservation();
				int techLevel = astroFunction.techLevel;
				int observatorySize = astroFunction.observatoryCapacity;
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				observatorySupply += techLevel * observatorySize * wearModifier;
			}
		}

		// Determine existing settlement value for astronomical observatories.
		double existingObservatoryValue = observatoryDemand / (observatorySupply + 1D);

		// Determine settlement value for this building's astronomical observatory
		// function.

		int techLevel = buildingConfig.getAstronomicalObservationTechLevel(buildingName);
		int observatorySize = buildingConfig.getAstronomicalObservationCapacity(buildingName);
		double buildingObservatorySupply = techLevel * observatorySize;

		double result = buildingObservatorySupply * existingObservatoryValue;

		// Subtract power usage cost per sol.
		double power = buildingConfig.getAstronomicalObservationPowerRequirement(buildingName);
//		double hoursInSol = MarsClock.convertMillisolsToSeconds(1000D) / 60D / 60D;
		double powerPerSol = power * MarsClock.HOURS_PER_MILLISOL * 1000D;
		double powerValue = powerPerSol * settlement.getPowerGrid().getPowerValue();
		result -= powerValue;

		if (result < 0D)
			result = 0D;

		return result;
	}

	@Override
	public double getMaintenanceTime() {

		double result = 0D;

		// Add maintenance for tech level.
		result += techLevel * 10D;

		// Add maintenance for observer capacity.
		result += observatoryCapacity * 10D;

		return result;
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