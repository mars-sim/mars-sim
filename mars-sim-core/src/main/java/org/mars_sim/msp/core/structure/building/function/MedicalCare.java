/**
 * Mars Simulation Project
 * MedicalCare.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;
 
import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.core.person.*;
import org.mars_sim.msp.core.person.ai.task.*;
import org.mars_sim.msp.core.person.medical.*;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.*;
 
/**
 * The MedicalCare class represents a building function for providing medical care.
 */
public class MedicalCare extends Function implements MedicalAid, Serializable {

	public static final String NAME = "Medical Care";
	
	private MedicalStation medicalStation;
	
	/**
	 * Constructor
	 * @param building the building this function is for.
	 * @throws BuildingException if function could not be constructed.
	 */
	public MedicalCare(Building building) throws BuildingException {
		// Use Function constructor.
		super(NAME, building);
		
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		
		try {
			int techLevel = config.getMedicalCareTechLevel(building.getName());
			int beds = config.getMedicalCareBeds(building.getName());
			medicalStation = new MedicalStation(techLevel, beds);
		}
		catch (Exception e) {
			throw new BuildingException("MedicalCare.constructor: " + e.getMessage());
		}
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
        
        // Demand is 5 medical points per inhabitant.
        double demand = settlement.getAllAssociatedPeople().size() * 5D;
        
        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(NAME).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding && building.getName().equalsIgnoreCase(buildingName) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                MedicalCare medFunction = (MedicalCare) building.getFunction(NAME);
                double tech = medFunction.getTechLevel();
                double beds = medFunction.getSickBedNum();
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                supply += (tech * tech) * beds * wearModifier;
            }
        }
        
        double medicalPointValue = demand / (supply + 1D);
        
        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        double tech = config.getMedicalCareTechLevel(buildingName);
        double beds = config.getMedicalCareBeds(buildingName);
        double medicalPoints = (tech * tech) * beds;
        
        return medicalPoints * medicalPointValue;
    }

    /**
     * Gets the number of sick beds.
     * @return Sick bed count.
     */
    public int getSickBedNum() {
    	return medicalStation.getSickBedNum();
    }
    
    /**
     * Gets the current number of people being treated here.
     * @return Patient count.
     */
    public int getPatientNum() {
    	return medicalStation.getPatientNum();
    }
    
    /**
     * Gets the patients at this medical station.
     * @return Collection of People.
     */
    public Collection<Person> getPatients() {
    	return medicalStation.getPatients();
    }
    
	/**
	 * Gets the number of people using this medical aid to treat sick people.
	 * @return number of people
	 */
	public int getPhysicianNum() {
		int result = 0;
        
        if (getBuilding().hasFunction(LifeSupport.NAME)) {
        	try {
        		LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(LifeSupport.NAME);
        		Iterator<Person> i = lifeSupport.getOccupants().iterator();
        		while (i.hasNext()) {
					Task task = i.next().getMind().getTaskManager().getTask();
					if (task instanceof MedicalAssistance) {
						MedicalAid aid = ((MedicalAssistance) task).getMedicalAid();
						if ((aid != null) && (aid == this)) result++;
					}
        		}
        	}
        	catch (Exception e) {}
        }
        
		return result;
	}
	
	/**
	 * Gets the health problems awaiting treatment at the medical station.
	 *
	 * @return list of health problems
	 */
	public List<HealthProblem> getProblemsAwaitingTreatment() {
		return medicalStation.getProblemsAwaitingTreatment();
	}
	
	/**
	 * Gets the health problems currently being treated at the medical station.
	 *
	 * @return list of health problems
	 */
	public List<HealthProblem> getProblemsBeingTreated() {
		return medicalStation.getProblemsBeingTreated();
	}
	
	/**
	 * Get a list of supported Treatments at this medical aid.
	 *
	 * @return List of treatments.
	 */
	public List<Treatment> getSupportedTreatments() {
		return medicalStation.getSupportedTreatments();
	}
	
	/**
	 * Checks if a health problem can be treated at this medical aid.
	 *
	 * @param problem The health problem to check treatment.
	 * @return true if problem can be treated.
	 */
	public boolean canTreatProblem(HealthProblem problem) {
		return medicalStation.canTreatProblem(problem);	
	}
	
	/**
	 * Add a health problem to the queue of problems awaiting treatment at this
	 * medical aid.
	 *
	 * @param problem The health problem to await treatment.
	 * @throws Exception if health problem cannot be treated here.
	 */
	public void requestTreatment(HealthProblem problem) throws Exception {
		medicalStation.requestTreatment(problem);
		
		// Add person to building if possible.
		try {
			if (getBuilding().hasFunction(LifeSupport.NAME)) {
				LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(LifeSupport.NAME);
				lifeSupport.addPerson(problem.getSufferer());
			}
		}
		catch (BuildingException e) {}
	}
	
	/**
	 * Starts the treatment of a health problem in the waiting queue.
	 *
	 * @param problem the health problem to start treating.
	 * @param treatmentDuration the time required to perform the treatment.
	 * @throws Exception if treatment cannot be started.
	 */
	public void startTreatment(HealthProblem problem, double treatmentDuration) throws Exception {
		medicalStation.startTreatment(problem, treatmentDuration);
        
		// Add person to building if possible.
		try {
			if (getBuilding().hasFunction(LifeSupport.NAME)) {
				LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(LifeSupport.NAME);
				lifeSupport.addPerson(problem.getSufferer());
			}
		}
		catch (BuildingException e) {}
	}
	
	/**
	 * Stop a previously started treatment.
	 *
	 * @param problem Health problem stopping treatment on.
	 * @throws Exception if health problem is not being treated.
	 */
	public void stopTreatment(HealthProblem problem) throws Exception {
		medicalStation.stopTreatment(problem);
	}
	
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) throws BuildingException {
	
	    /*
		String name = getBuilding().getBuildingManager().getSettlement().getName();
		if (getProblemsBeingTreated().size() > 0) {
			Iterator i = getProblemsBeingTreated().iterator();
			while (i.hasNext()) {
				HealthProblem problem = (HealthProblem) i.next();
				logger.info(name + ": " + problem.toString() + " - cured: " + problem.getCured());
			}
		}
		*/
	}
	
	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return 0D;
	}
	
	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPowerDownPowerRequired() {
		return 0D;
	}
	
	/**
	 * Gets the treatment level.
	 * @return treatment level
	 */
	public int getTechLevel() {
		return medicalStation.getTreatmentLevel();
	}
}