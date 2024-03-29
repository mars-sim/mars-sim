/**
 * Mars Simulation Project
 * TreatMedicalPatientMeta.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.Iterator;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.TreatMedicalPatient;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.core.person.health.Treatment;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.MedicalCare;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.SickBay;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the TreatMedicalPatient task.
 */
public class TreatMedicalPatientMeta extends FactoryMetaTask {
    
	private static final int VALUE = 500;
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.treatMedicalPatient"); //$NON-NLS-1$

    public TreatMedicalPatientMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		
		setTrait(TaskTrait.MEDICAL);
		setPreferredJob(JobType.MEDICS);
	}
   

    @Override
    public Task constructInstance(Person person) {
        return new TreatMedicalPatient(person);
    }

    /**
     * Assess this person helping someone with treatment
     * @param person Being assessed
     * @return Potential suitable tasks
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
         
        if (!person.isInside() || !hasNeedyMedicalAids(person)) {
            return EMPTY_TASKLIST;
        }

        // Get the local medical aids to use.
        var result = new RatingScore(VALUE);	
        result = assessPersonSuitability(result, person);
        
        return createTaskJobs(result);
    }

    /**
     * Checks if there are local medical aids that have people waiting for treatment.
     * @param person the person.
     * @return true if needy medical aids.
     */
    private boolean hasNeedyMedicalAids(Person person) {

        boolean result = false;

		if (person.getLocationStateType() == LocationStateType.INSIDE_SETTLEMENT) {
            result = hasNeedyMedicalAidsAtSettlement(person, person.getSettlement());
        }
        else if (person.getLocationStateType() == LocationStateType.INSIDE_VEHICLE) {
            result = hasNeedyMedicalAidsInVehicle(person, person.getVehicle());
        }

        return result;
    }

    /**
     * Checks if there are medical aids at a settlement that have people waiting for treatment.
     * @param person the person.
     * @param settlement the settlement.
     * @return true if needy medical aids.
     */
    private boolean hasNeedyMedicalAidsAtSettlement(Person person, Settlement settlement) {

        boolean result = false;

        // Check all medical care buildings.
        Iterator<Building> i = settlement.getBuildingManager().getBuildingSet(
                FunctionType.MEDICAL_CARE).iterator();
        while (i.hasNext() && !result) {
            Building building = i.next();

            // Check if building currently has a malfunction.
            boolean malfunction = building.getMalfunctionManager().hasMalfunction();

            if (!malfunction) {

                // Check if there are any treatable medical problems at building.
                MedicalCare medicalCare = building.getMedical();
                if (hasTreatableHealthProblems(person, medicalCare)) {
                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * Checks if there are medical aids in a vehicle that have people waiting for treatment.
     * @param person the person.
     * @param vehicle the vehicle.
     * @return true if needy medical aids.
     */
    private boolean hasNeedyMedicalAidsInVehicle(Person person, Vehicle vehicle) {

        boolean result = false;

        if (VehicleType.isRover(vehicle.getVehicleType())) {
            Rover rover = (Rover) vehicle;
            if (rover.hasSickBay()) {
                SickBay sickBay = rover.getSickBay();
                if (hasTreatableHealthProblems(person, sickBay)) {
                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * Checks if a medical aid has waiting people with health problems that the person can treat.
     * @param person the person.
     * @param aid the medical aid.
     * @return true if treatable health problems.
     */
    private boolean hasTreatableHealthProblems(Person person, MedicalAid aid) {

        boolean result = false;

        // Get the person's medical skill.
        int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);

        // Check if there are any treatable health problems awaiting treatment.
        Iterator<HealthProblem> j = aid.getProblemsAwaitingTreatment().iterator();
        while (j.hasNext() && !result) {
            HealthProblem problem = j.next();
            Treatment treatment = problem.getComplaint().getRecoveryTreatment();
            if (treatment != null) {
                int requiredSkill = treatment.getSkill();
                if (skill >= requiredSkill) {
                    result = true;
                }
            }
        }

        return result;
    }
}
