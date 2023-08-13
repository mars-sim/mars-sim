/**
 * Mars Simulation Project
 * RequestMedicalTreatmentMeta.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.RequestMedicalTreatment;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.MedicalAid;
import org.mars_sim.msp.core.person.health.Treatment;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.SickBay;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Meta task for the RequestMedicalTreatment task.
 */
public class RequestMedicalTreatmentMeta extends FactoryMetaTask {

	private static final int VALUE = 500;
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.requestMedicalTreatment"); //$NON-NLS-1$

    public RequestMedicalTreatmentMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		
		setTrait(TaskTrait.TREATMENT);
	}
    

    @Override
    public Task constructInstance(Person person) {
        return new RequestMedicalTreatment(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isOutside())
        	return 0;
        
        if (person.getPhysicalCondition().getProblems().isEmpty())
        	return 0;
        
        // Get person's medical skill level.
        int personMedicalSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);

        // Get the best medical skill level of local people.
        int bestMedicalSkill = getBestLocalMedicalSkill(person);

        // Determine all the person's health problems that need treatment.
        List<HealthProblem> problemsNeedingTreatment = new ArrayList<>();
        Iterator<HealthProblem> i = person.getPhysicalCondition().getProblems().iterator();
        while (i.hasNext()) {
            HealthProblem problem = i.next();
            if (problem.isDegrading()) {

                Treatment treatment = problem.getIllness().getRecoveryTreatment();
                if (treatment != null) {

                    // Can other person with best medical skill treat health problem.
                    boolean canTreat = false;
                    if (bestMedicalSkill >= treatment.getSkill()) {
                        result += VALUE;
                        canTreat = true;
                    }

                    // Check if person can treat the health problem himself/herself.
                    boolean selfTreat = false;
                    if (treatment.getSelfAdminister()
                        && personMedicalSkill >= treatment.getSkill()) {
                    	result += VALUE;
                    	selfTreat = true;
                    }

                    if (canTreat && !selfTreat) {
                        problemsNeedingTreatment.add(problem);
                    }
                }
            }
        }

        if (problemsNeedingTreatment.size() > 0) {

            // Determine if any available medical aids can be used to treat person's health problems.
            boolean usefulMedicalAids = false;
            Iterator<MedicalAid> j = getAvailableMedicalAids(person).iterator();
            while (j.hasNext() && !usefulMedicalAids) {
                MedicalAid aid = j.next();

                // Check if medical aid can treat any problems.
                boolean canTreatProblems = false;
                Iterator<HealthProblem> k = problemsNeedingTreatment.iterator();
                while (k.hasNext() && !canTreatProblems) {
                    HealthProblem problem = k.next();
                    if (aid.canTreatProblem(problem)) {
                        canTreatProblems = true;
                        result += VALUE;
                    }
                }

                if (canTreatProblems) {
                    usefulMedicalAids = true;
                }
            }

            // If any useful medical aids for treating person's health problems, return probability.
            if (usefulMedicalAids) {
                result += VALUE;
            }

            if (result > 0)
            	result = result + result * person.getPreference().getPreferenceScore(this)/5D;

	        if (result < 0) result = 0;

        }

        return result;
    }

    /**
     * Gets the local medical aids that are available for treatment.
     * 
     * @param person the person.
     * @return list of available medical aids.
     */
    private List<MedicalAid> getAvailableMedicalAids(Person person) {

        List<MedicalAid> result = new ArrayList<>();

        if (person.isInSettlement()) {
            result = getAvailableMedicalAidsAtSettlement(person);
        }
        else if (person.isInVehicle()) {
            result = getAvailableMedicalAidsInVehicle(person);
        }

        return result;
    }

    /**
     * Gets the medical aids at a settlement that are available for treatment.
     * 
     * @param person the person.
     * @return list of available medical aids.
     */
    private List<MedicalAid> getAvailableMedicalAidsAtSettlement(Person person) {

        List<MedicalAid> result = new ArrayList<>();

        // Check all medical care buildings.
        Iterator<Building> i = person.getSettlement().getBuildingManager().getBuildingSet(
                FunctionType.MEDICAL_CARE).iterator();
        while (i.hasNext()) {
            Building building = i.next();

            // Check if building currently has a malfunction.
            boolean malfunction = building.getMalfunctionManager().hasMalfunction();

            // Check if building has enough bed space.
            MedicalCare medicalCare = building.getMedical();
            int numPatients = medicalCare.getPatientNum();
            int numBeds = medicalCare.getSickBedNum();
            boolean enoughBedSpace = (numPatients < numBeds);

            if (!malfunction && enoughBedSpace) {
                result.add(medicalCare);
            }
        }

        return result;
    }

    /**
     * Get the medical aids in a rover that are available for treatment.
     * @param person the person.
     * @return list of available medical aids.
     */
    private List<MedicalAid> getAvailableMedicalAidsInVehicle(Person person) {

        List<MedicalAid> result = new ArrayList<>();

        if (VehicleType.isRover(person.getVehicle().getVehicleType())) {
            Rover rover = (Rover) person.getVehicle();
            if (rover.hasSickBay()) {
                SickBay sickBay = rover.getSickBay();
                int numPatients = sickBay.getPatientNum();
                int numBeds = sickBay.getSickBedNum();
                if (numPatients < numBeds) {
                    result.add(sickBay);
                }
            }
        }

        return result;
    }

    /**
     * Get the highest medical skill of all other local people.
     * @param person the person.
     * @return highest medical skill (-1 if no one else around).
     */
    private int getBestLocalMedicalSkill(Person person) {

        int result = -1;

        if (person.isInSettlement()) {

            result = getBestLocalMedicalSkillAtSettlement(person, person.getSettlement());
        }
        else if (person.isInVehicle()) {

            result = getBestLocalMedicalSkillInVehicle(person, person.getVehicle());
        }

        return result;
    }

    /**
     * Get the highest medical skill of all other people at a settlement.
     * @param person the person.
     * @param settlement the settlement.
     * @return highest medical skill (-1 if no one else around).
     */
    private int getBestLocalMedicalSkillAtSettlement(Person person, Settlement settlement) {

        int result = -1;

        Iterator<Person> i = settlement.getIndoorPeople().iterator();
        while (i.hasNext()) {
            Person inhabitant = i.next();
            if (person != inhabitant) {
                int medicalSkill = inhabitant.getSkillManager().getEffectiveSkillLevel(
                        SkillType.MEDICINE);
                if (medicalSkill > result) {
                    result = medicalSkill;
                }
            }
        }

        return result;
    }

    /**
     * Get the highest medical skill of all other people in a vehicle.
     * @param person the person.
     * @param vehicle the settlement.
     * @return highest medical skill (-1 if no one else around).
     */
    private int getBestLocalMedicalSkillInVehicle(Person person, Vehicle vehicle) {

        int result = -1;

        if (vehicle instanceof Crewable) {
            Crewable crewVehicle = (Crewable) person.getVehicle();
            Iterator<Person> i = crewVehicle.getCrew().iterator();
            while (i.hasNext()) {
                Person crewmember = i.next();
                if (person != crewmember) {
                    int medicalSkill = crewmember.getSkillManager().getEffectiveSkillLevel(
                            SkillType.MEDICINE);
                    if (medicalSkill > result) {
                        result = medicalSkill;
                    }
                }
            }
        }

        return result;
    }
}
