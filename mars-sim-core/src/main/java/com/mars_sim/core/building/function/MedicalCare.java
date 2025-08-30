/*
 * Mars Simulation Project
 * MedicalCare.java
 * @date 2025-08-14
 * @author Scott Davis
 */
package com.mars_sim.core.building.function;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.FunctionSpec;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.core.person.health.MedicalStation;
import com.mars_sim.core.person.health.Treatment;
import com.mars_sim.core.person.health.task.RequestMedicalTreatment;
import com.mars_sim.core.person.health.task.TreatMedicalPatient;
import com.mars_sim.core.structure.Settlement;

/**
 * The MedicalCare class represents a building function for providing medical
 * care.
 */
public class MedicalCare extends Function implements MedicalAid {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(MedicalCare.class.getName());

	private MedicalStation medicalStation;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @param spec Specification of Function
	 * @throws BuildingException if function could not be constructed.
	 */
	public MedicalCare(Building building, FunctionSpec spec) {
		// Use Function constructor.
		super(FunctionType.MEDICAL_CARE, spec, building);

		int techLevel = spec.getTechLevel();
		
		// THis is not good. all details should be in the FunctionSpec
		Set<LocalPosition> bedSet = buildingConfig.getBuildingSpec(building.getBuildingType()).getBeds();

		// NOTE: distinguish between activity spots and bed locations
		medicalStation = new MedicalStation(building.getName(), techLevel, bedSet.size());
		
		medicalStation.setMedicalBeds(bedSet);
	}
	
	/**
	 * Gets the value of the function for a named building type.
	 * 
	 * @param type the building type.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String type, boolean newBuilding, Settlement settlement) {

		// Demand is 5 medical points per inhabitant.
		double demand = settlement.getNumCitizens() * 5D;

		double supply = 0D;
		boolean removedBuilding = false;
		for(Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.MEDICAL_CARE)) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(type) && !removedBuilding) {
				removedBuilding = true;
			} else {
				MedicalCare medFunction = building.getMedical();
				double tech = medFunction.getTechLevel();
				double beds = medFunction.getSickBedNum();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += (tech * tech) * beds * wearModifier;
			}
		}

		double value = demand / (supply + 1D) / 10D;

		double tech = buildingConfig.getFunctionSpec(type, FunctionType.MEDICAL_CARE).getTechLevel();
		return tech * value;
	}

    
    /**
     * Dispatch a worker to serve a medical need by occupying an activity spot.
     * 
     * @param worker
     * @return
     */
    public static boolean dispatchToMedical(Worker worker) {
    	
    	boolean success = false;
    	
    	Building building = worker.getSettlement().getBuildingManager()
				.getABuilding(FunctionType.MEDICAL_CARE, FunctionType.LIFE_SUPPORT);

		if (building != null) {

			// Send this doctor to occupy a physical spot
			success = BuildingManager.addToActivitySpot(worker, building, FunctionType.MEDICAL_CARE);
				
			if (success)
				logger.info(worker, 20_000, "Arrived at " + building.getName() + ".");
			else
				logger.info(worker, 0, "Unable to be go to a medical building.");
		}
		else
			logger.info(worker, 0, "Unable to find a medical building.");
		
	    return success;
    }
    
	/**
	 * Adds a patient to a medical bed.
	 * 
	 * @return
	 */
	public boolean addPatientToBed(Person person) {
		return medicalStation.addPatientToBed(person);
	}
	
	/**
	 * Gets the number of sick beds.
	 * 
	 * @return Sick bed count.
	 */
	public int getSickBedNum() {
		return medicalStation.getSickBedNum();
	}

	/**
	 * Gets the current number of people being treated here.
	 * 
	 * @return Patient count.
	 */
	public int getPatientNum() {
		return medicalStation.getPatientNum();
	}
	
	/**
	 * Are there any patients ?
	 * 
	 * @return 
	 */
	public boolean hasPatients() {
		return medicalStation.hasPatients();
	}
	
	
	/**
	 * Gets the patients at this medical station.
	 * 
	 * @return Collection of People.
	 */
	public Collection<Person> getPatients() {
		return medicalStation.getPatients();
	}

	/**
	 * Gets the number of people using this medical aid to treat sick people.
	 * 
	 * @return number of people
	 */
	public int getPhysicianNum() {
		int result = 0;

		LifeSupport lifeSupport = getBuilding().getFunction(FunctionType.LIFE_SUPPORT);
		if (lifeSupport != null) {
			Iterator<Person> i = lifeSupport.getOccupants().iterator();
			while (i.hasNext()) {
				Task task = i.next().getMind().getTaskManager().getTask();
				if (task instanceof TreatMedicalPatient tmp) {
					MedicalAid aid = tmp.getMedicalAid();						
					if ((aid != null) && (aid == this))
						result++;
				}
				else if (task instanceof RequestMedicalTreatment rmt) {
					MedicalAid aid = rmt.getMedicalAid();						
					if ((aid != null) && (aid == this))
						result++;
				}	
			}
		}

		return result;
	}

	@Override
	public List<HealthProblem> getProblemsAwaitingTreatment() {
		return medicalStation.getProblemsAwaitingTreatment();
	}

	@Override
	public List<HealthProblem> getProblemsBeingTreated() {
		return medicalStation.getProblemsBeingTreated();
	}

	@Override
	public List<Treatment> getSupportedTreatments() {
		return medicalStation.getSupportedTreatments();
	}

	@Override
	public boolean canTreatProblem(HealthProblem problem) {
		return medicalStation.canTreatProblem(problem);
	}

	@Override
	public void requestTreatment(HealthProblem problem) {
		medicalStation.requestTreatment(problem);
	}

	@Override
	public void cancelRequestTreatment(HealthProblem problem) {
		medicalStation.cancelRequestTreatment(problem);
	}

	@Override
	public void startTreatment(HealthProblem problem, double treatmentDuration) {
		medicalStation.startTreatment(problem, treatmentDuration);
	}

	@Override
	public void stopTreatment(HealthProblem problem) {
		medicalStation.stopTreatment(problem);
	}

	@Override
	public List<Person> getRestingRecoveryPeople() {
		return medicalStation.getRestingRecoveryPeople();
	}

	@Override
	public void startRestingRecovery(Person person) {
		medicalStation.startRestingRecovery(person);
	}

	@Override
	public void stopRestingRecovery(Person person) {
		medicalStation.stopRestingRecovery(person);
	}

	/**
	 * Gets the treatment level.
	 * 
	 * @return treatment level
	 */
	public int getTechLevel() {
		return medicalStation.getTreatmentLevel();
	}

	@Override
	public double getMaintenanceTime() {
		double result = medicalStation.getTreatmentLevel() * .5;
		// Add maintenance for number of sick beds.
		result *= medicalStation.getSickBedNum() * .5;

		return result;
	}

	@Override
	public void destroy() {
		super.destroy();

		medicalStation = null;
	}
}
