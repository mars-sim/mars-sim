/**
 * Mars Simulation Project
 * MedicalCare.java
 * @version 2.76 2004-06-10
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;
 
import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.task.*;
import org.mars_sim.msp.simulation.person.medical.*;
import org.mars_sim.msp.simulation.structure.building.*;
 
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
		
		SimulationConfig simConfig = Simulation.instance().getSimConfig();
		BuildingConfig config = simConfig.getBuildingConfiguration();
		
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
    public PersonCollection getPatients() {
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
        		PersonIterator i = lifeSupport.getOccupants().iterator();
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
	public List getProblemsAwaitingTreatment() {
		return medicalStation.getProblemsAwaitingTreatment();
	}
	
	/**
	 * Gets the health problems currently being treated at the medical station.
	 *
	 * @return list of health problems
	 */
	public List getProblemsBeingTreated() {
		return medicalStation.getProblemsBeingTreated();
	}
	
	/**
	 * Get a list of supported Treatments at this medical aid.
	 *
	 * @return List of treatments.
	 */
	public List getSupportedTreatments() {
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
				System.out.println(name + ": " + problem.toString() + " - cured: " + problem.getCured());
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