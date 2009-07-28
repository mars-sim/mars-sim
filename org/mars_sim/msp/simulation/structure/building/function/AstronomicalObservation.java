/**
 * Mars Simulation Project
 * AstronomicalObservation.java
 * @version 2.87 2009-07-26
 * @author Sebastien Venot
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.science.Science;
import org.mars_sim.msp.simulation.science.ScienceUtil;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingConfig;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.time.MarsClock;

/**
 * A building function for observing astronomical objects.
 */
public class AstronomicalObservation extends Function {
    
    private static String CLASS_NAME = 
        "org.mars_sim.msp.simulation.structure.building.function.AstronomicalObservation";
    private static Logger logger = Logger.getLogger(CLASS_NAME);
    public static String NAME = "Astronomical Observations";

    // Data members
    private double powerRequired;
    private int techLevel;
    private int observatoryCapacity;
    private int observerNum;

    /**
     * Constructor
     * 
     * @param building the building the function is for.
     * @throws BuildingException if error creating building function.
     */
    public AstronomicalObservation(Building building) throws BuildingException {
        // Use function constructor.
        super(NAME, building);

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

        try {
            powerRequired = config.getAstronomicalObservationPowerRequirement(building.getName());
            techLevel = config.getAstronomicalObservationTechLevel(building.getName());
            observatoryCapacity = config.getAstronomicalObservationCapacity(building.getName());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Constructor(): " + e.getMessage());
            throw new BuildingException("AstronomicalObservation.constructor: " + e.getMessage());
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

    /**
     * Time passing for the building.
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     */
    public void timePassing(double time) throws BuildingException {
        // Do nothing
    }

    /**
     * Adds a new observer to the observatory.
     * @throws Exception if observatory is already at capacity.
     */
    public void addObserver() throws Exception {
        observerNum++;
        if (observerNum > observatoryCapacity) {
            observerNum = observatoryCapacity;
            logger.log(Level.SEVERE, "addObserver(): " + "Observatory is already full of observers.");
            throw new Exception("Observatory is already full of observers.");
        }
    }
    
    /**
     * Removes an observer from the observatory.
     * @throws Exception if no observers currently in observatory.
     */
    public void removeObserver() throws Exception {
        observerNum--;
        if (getObserverNum() < 0) {
            observerNum = 0;
            logger.log(Level.SEVERE, "addObserver(): " + "Observatory is already empty of observers.");
            throw new Exception("Observatory is already empty of observers.");
        }
    }

    /**
     * Gets the current number of observers in the observatory.
     * @return number of observers.
     */
    public int getObserverNum() {
        return observerNum;
    }
    
    /**
     * Gets the capacity for observers in the observatory.
     * @return capacity.
     */
    public int getObservatoryCapacity() {
        return observatoryCapacity;
    }

    /**
     * Gets the technology level of the observatory.
     * @return technology level.
     */
    public int getTechnologyLevel() {
        return techLevel;
    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    public static final double getFunctionValue(String buildingName, boolean newBuilding, 
            Settlement settlement) throws Exception {

        double observatoryDemand = 0D;
        Science astronomyScience = ScienceUtil.getScience(Science.ASTRONOMY);
        
        // Determine settlement demand for astronomical observatories.
        String astronomySkill = ScienceUtil.getAssociatedSkill(astronomyScience);
        Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
        while (j.hasNext()) {
            observatoryDemand += j.next().getMind().getSkillManager().getSkillLevel(astronomySkill);
        }

        // Determine existing settlement supply of astronomical observatories.
        double observatorySupply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> k = settlement.getBuildingManager().getBuildings(NAME).iterator();
        while (k.hasNext()) {
            Building building = k.next();
            if (!newBuilding && building.getName().equalsIgnoreCase(buildingName) && !removedBuilding) {
                removedBuilding = true;
            } else {
                AstronomicalObservation astroFunction = (AstronomicalObservation) building.getFunction(NAME);
                int techLevel = astroFunction.getTechnologyLevel();
                int observatorySize = astroFunction.getObservatoryCapacity();
                observatorySupply += techLevel * observatorySize;
            }
        }

        // Determine existing settlement value for astronomical observatories.
        double existingObservatoryValue = observatoryDemand / (observatorySupply + 1D);

        // Determine settlement value for this building's astronomical observatory function.
        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        int techLevel = config.getAstronomicalObservationTechLevel(buildingName);
        int observatorySize = config.getAstronomicalObservationCapacity(buildingName);
        double buildingObservatorySupply = techLevel * observatorySize;
        
        double result = buildingObservatorySupply * existingObservatoryValue;
        
        // Subtract power usage cost per sol.
        double power = config.getAstronomicalObservationPowerRequirement(buildingName);
        double hoursInSol = MarsClock.convertMillisolsToSeconds(1000D) / 60D / 60D;
        double powerPerSol = power * hoursInSol;
        double powerValue = powerPerSol * settlement.getPowerGrid().getPowerValue();
        result -= powerValue;

        if (result < 0D) result = 0D;
        
        return result;
    }
}