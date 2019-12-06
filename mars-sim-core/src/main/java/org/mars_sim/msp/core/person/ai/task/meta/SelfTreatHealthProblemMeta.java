/**
 * Mars Simulation Project
 * SelfTreatMedicalProblemMeta.java
 * @version 3.1.0 2017-03-09
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.SelfTreatHealthProblem;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.Treatment;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.SickBay;

/**
 * Meta task for the SelfTreatHealthProblem task.
 */
public class SelfTreatHealthProblemMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	private static final int VALUE = 1000;
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.selfTreatHealthProblem"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new SelfTreatHealthProblem(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInside()) {
	        // Check if person has health problems that can be self-treated.
        	int size = getSelfTreatableHealthProblems(person).size();
        	
	        boolean hasSelfTreatableProblems = (size > 0);
	
	        // Check if person has available medical aids.
	        boolean hasAvailableMedicalAids = hasAvailableMedicalAids(person);
	
	
	        if (hasSelfTreatableProblems && hasAvailableMedicalAids) {
	            result = VALUE * size;
	        }
	
	        double pref = person.getPreference().getPreferenceScore(this);
	        
	        if (pref > 0)
	        	result = result * 3D;
        	
	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();
	
	        if (result < 0) result = 0;

        }
        
        return result;
    }

    /**
     * Gets a list of health problems the person can self-treat.
     * @param person the person.
     * @return list of health problems (may be empty).
     */
    private List<HealthProblem> getSelfTreatableHealthProblems(Person person) {

        List<HealthProblem> result = new ArrayList<HealthProblem>();

        Iterator<HealthProblem> i = person.getPhysicalCondition().getProblems().iterator();
        while (i.hasNext()) {
            HealthProblem problem = i.next();
            if (problem.isDegrading()) {
                Treatment treatment = problem.getIllness().getRecoveryTreatment();
                if (treatment != null) {
                    boolean selfTreatable = treatment.getSelfAdminister();
                    int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);
                    int requiredSkill = treatment.getSkill();
                    if (selfTreatable && (skill >= requiredSkill)) {
                        result.add(problem);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Checks if a person has any available local medical aids for self treating health problems.
     * @param person the person.
     * @return true if available medical aids.
     */
    private boolean hasAvailableMedicalAids(Person person) {

        boolean result = false;

        if (person.isInSettlement()) {
            result = hasAvailableMedicalAidsAtSettlement(person);
        }
        else if (person.isInVehicle()) {
            result = hasAvailableMedicalAidInVehicle(person);
        }

        return result;
    }

    /**
     * Checks if a person has any available medical aids at a settlement.
     * @param person the person.
     * @return true if available medical aids.
     */
    private boolean hasAvailableMedicalAidsAtSettlement(Person person) {

        boolean result = false;

        // Check all medical care buildings.
        Iterator<Building> i = person.getSettlement().getBuildingManager().getBuildings(
                FunctionType.MEDICAL_CARE).iterator();
        while (i.hasNext() && !result) {
            Building building = i.next();

            // Check if building currently has a malfunction.
            boolean malfunction = building.getMalfunctionManager().hasMalfunction();

            // Check if enough beds for patient.
            MedicalCare medicalCare = building.getMedical();
            int numPatients = medicalCare.getPatientNum();
            int numBeds = medicalCare.getSickBedNum();

            if ((numPatients < numBeds) && !malfunction) {

                // Check if any of person's self-treatable health problems can be treated in building.
                boolean canTreat = false;
                Iterator<HealthProblem> j = getSelfTreatableHealthProblems(person).iterator();
                while (j.hasNext() && !canTreat) {
                    HealthProblem problem = j.next();
                    if (medicalCare.canTreatProblem(problem)) {
                        canTreat = true;
                    }
                }

                if (canTreat) {
                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * Checks if a person has an available medical aid in a vehicle.
     * @param person the person.
     * @return true if available medical aids.
     */
    private boolean hasAvailableMedicalAidInVehicle(Person person) {

        boolean result = false;

        if (person.getVehicle() instanceof Rover) {
            Rover rover = (Rover) person.getVehicle();
            if (rover.hasSickBay()) {
                SickBay sickBay = rover.getSickBay();

                // Check if enough beds for patient.
                int numPatients = sickBay.getPatientNum();
                int numBeds = sickBay.getSickBedNum();

                if (numPatients < numBeds) {

                    // Check if any of person's self-treatable health problems can be treated in sick bay.
                    boolean canTreat = false;
                    Iterator<HealthProblem> j = getSelfTreatableHealthProblems(person).iterator();
                    while (j.hasNext() && !canTreat) {
                        HealthProblem problem = j.next();
                        if (sickBay.canTreatProblem(problem)) {
                            canTreat = true;
                        }
                    }

                    if (canTreat) {
                        result = true;
                    }
                }
            }
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}