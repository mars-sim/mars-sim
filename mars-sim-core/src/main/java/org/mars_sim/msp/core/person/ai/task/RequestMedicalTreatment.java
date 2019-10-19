/**
 * Mars Simulation Project
 * RequestMedicalTreatment.java
 * @version 3.1.0 2017-03-09
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.MedicalAid;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.SickBay;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A task for requesting and awaiting medical treatment at a medical station.
 */
public class RequestMedicalTreatment extends Task implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = .3D;

    /** default logger. */
    private static Logger logger = Logger.getLogger(RequestMedicalTreatment.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.requestMedicalTreatment"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase WAITING_FOR_TREATMENT = new TaskPhase(Msg.getString(
            "Task.phase.waitingForMedicalTreatment")); //$NON-NLS-1$
    private static final TaskPhase TREATMENT = new TaskPhase(Msg.getString(
            "Task.phase.receivingMedicalTreatment")); //$NON-NLS-1$

    /** Maximum waiting duration in millisols. */
    private static final double MAX_WAITING_DURATION = 200D;

    // Data members.
    private MedicalAid medicalAid;
    private double waitingDuration;

    /**
     * Constructor.
     * 
     * @param person the person to perform the task
     */
    public RequestMedicalTreatment(Person person) {
        super(NAME, person, false, false, STRESS_MODIFIER, false, 10D);
	     
        if (person.getPhysicalCondition().getProblems().size() == 0)
        	endTask();
        		
        // Choose available medical aid for treatment.
        medicalAid = determineMedicalAid();

        if (medicalAid != null) {

            if (medicalAid instanceof MedicalCare) {
                // Walk to medical care building.
                MedicalCare medicalCare = (MedicalCare) medicalAid;

                // Walk to medical care building.
                //walkToActivitySpotInBuilding(medicalCare.getBuilding(), false);
                Building b = medicalCare.getBuilding();
                if (b != null)
                	walkToActivitySpotInBuilding(b, FunctionType.MEDICAL_CARE, false);
                //else
                //	endTask();
            }
            else if (medicalAid instanceof SickBay) {
                // Walk to medical activity spot in rover.
                Vehicle vehicle = ((SickBay) medicalAid).getVehicle();
                if (vehicle instanceof Rover) {

                    // Walk to rover sick bay activity spot.
                    walkToSickBayActivitySpotInRover((Rover) vehicle, false);
                }
            }
        }
        
        else {
            //logger.severe("Medical aid could not be determined.");
            endTask();
        }

        // Initialize phase.
        addPhase(WAITING_FOR_TREATMENT);
        addPhase(TREATMENT);
        setPhase(WAITING_FOR_TREATMENT);
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (WAITING_FOR_TREATMENT.equals(getPhase())) {
            return waitingForTreatmentPhase(time);
        }
        else if (TREATMENT.equals(getPhase())) {
            return treatmentPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Determines a medical aid to wait for treatment at.
     * @return medical aid.
     */
    private MedicalAid determineMedicalAid() {

        MedicalAid result = null;

        if (person.isInSettlement()) {
            result = determineMedicalAidAtSettlement();
        }
        else if (person.isInVehicle()) {
            result = determineMedicalAidInVehicle();
        }

        return result;
    }

    /**
     * Determines a medical aid at a settlement.
     * @return medical aid.
     */
    private MedicalAid determineMedicalAidAtSettlement() {

        MedicalAid result = null;

        List<MedicalAid> goodMedicalAids = new ArrayList<MedicalAid>();

        // Check all medical care buildings.
        Iterator<Building> i = person.getSettlement().getBuildingManager().getBuildings(
                FunctionType.MEDICAL_CARE).iterator();
        while (i.hasNext()) {
            Building building = i.next();

            // Check if building currently has a malfunction.
            boolean malfunction = building.getMalfunctionManager().hasMalfunction();

            // Check if enough beds for patient.
            MedicalCare medicalCare = building.getMedical();
            int numPatients = medicalCare.getPatientNum();
            int numBeds = medicalCare.getSickBedNum();
            if ((numPatients < numBeds) && !malfunction) {
                // Check if medical aid can treat any health problems.
                boolean canTreatProblems = false;
                Iterator<HealthProblem> j = person.getPhysicalCondition().getProblems().iterator();
                while (j.hasNext()) {
                    HealthProblem problem = j.next();
                    if (problem.isDegrading() && medicalCare.canTreatProblem(problem)) {
                        canTreatProblems = true;
                    }
                }

                if (canTreatProblems) {
                    goodMedicalAids.add(medicalCare);
                }
            }
        }

        // Randomly select an valid medical care building.
        if (goodMedicalAids.size() > 0) {
            int index = RandomUtil.getRandomInt(goodMedicalAids.size() - 1);
            result = goodMedicalAids.get(index);
        }

        return result;
    }

    public int getNumHealthProblem() {
        int numProblem = 0;
        Iterator<HealthProblem> i = person.getPhysicalCondition().getProblems().iterator();
        while (i.hasNext()) {
            HealthProblem problem = i.next();
            if (problem.getRecovering() && problem.requiresBedRest()) {
            	numProblem++;
            }
        }
        return numProblem;
    }
    
    /**
     * Determines a medical aid in a vehicle.
     * @return medical aid.
     */
    private MedicalAid determineMedicalAidInVehicle() {

        MedicalAid result = null;

        if (person.getVehicle() instanceof Rover) {
            Rover rover = (Rover) person.getVehicle();
            if (rover.hasSickBay()) {
                SickBay sickBay = rover.getSickBay();
                int numPatients = sickBay.getPatientNum();
                int numBeds = sickBay.getSickBedNum();
                if (numPatients < numBeds) {
                    boolean canTreatProblems = false;
                    Iterator<HealthProblem> j = person.getPhysicalCondition().getProblems().iterator();
                    while (j.hasNext()) {
                        HealthProblem problem = j.next();
                        if (problem.isDegrading() && sickBay.canTreatProblem(problem)) {
                            canTreatProblems = true;
                        }
                    }

                    if (canTreatProblems) {
                        result = sickBay;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Performs the waiting for treatment phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left over after performing the phase.
     */
    private double waitingForTreatmentPhase(double time) {

        double remainingTime = 0D;

        // Check if any health problems are currently being treated.
        boolean underTreatment = false;
        Iterator<HealthProblem> i = person.getPhysicalCondition().getProblems().iterator();
        while (i.hasNext()) {
            HealthProblem problem = i.next();
            if (medicalAid.getProblemsBeingTreated().contains(problem)) {
                underTreatment = true;
            }
        }

        if (underTreatment) {
            setPhase(TREATMENT);
            remainingTime = time;
        }
        else {

            // Check if health problems are awaiting treatment.
            Iterator<HealthProblem> j = person.getPhysicalCondition().getProblems().iterator();
            while (j.hasNext()) {
                HealthProblem problem = j.next();
                if (!medicalAid.getProblemsAwaitingTreatment().contains(problem)) {
                    if (medicalAid.canTreatProblem(problem)) {
                        // Request treatment for health problem.
                        medicalAid.requestTreatment(problem);
                    }
                }
            }

            waitingDuration += time;
            if (waitingDuration >= MAX_WAITING_DURATION) {
                // End task if longer than maximum waiting duration.
                remainingTime = waitingDuration - MAX_WAITING_DURATION;
                endTask();
            }
        }

        return remainingTime;
    }

    /**
     * Performs the treatment phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left over after performing the phase.
     */
    private double treatmentPhase(double time) {

        double remainingTime = 0D;

        // Check if any health problems are currently being treated.
        boolean underTreatment = false;
        Iterator<HealthProblem> i = person.getPhysicalCondition().getProblems().iterator();
        while (i.hasNext()) {
            HealthProblem problem = i.next();
            if (medicalAid.getProblemsBeingTreated().contains(problem)) {
                underTreatment = true;
            }
        }

        if (!underTreatment) {

            // Check if person still has health problems needing treatment.
            boolean treatableProblems = false;
            Iterator<HealthProblem> j = person.getPhysicalCondition().getProblems().iterator();
            while (j.hasNext()) {
                HealthProblem problem = j.next();
                if (medicalAid.getProblemsAwaitingTreatment().contains(problem)) {
                    treatableProblems = true;
                }
            }

            if (treatableProblems) {
                // If any remaining treatable problems, wait for treatment.
                setPhase(WAITING_FOR_TREATMENT);
                waitingDuration = 0D;
                remainingTime = time;
            }
            else {
                endTask();
            }
        }

        return remainingTime;
    }

    @Override
    public FunctionType getLivingFunction() {
        return FunctionType.MEDICAL_CARE;
    }

    @Override
    public int getEffectiveSkillLevel() {
        // No effective skill level.
        return 0;
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        return new ArrayList<SkillType>(0);
    }

    @Override
    protected void addExperience(double time) {
        // Do nothing
    }

    @Override
    public void endTask() {
        super.endTask();

        // Remove person from medical aid.
        if (medicalAid != null) {

            // Cancel any treatment requests of the person.
            Iterator<HealthProblem> i = medicalAid.getProblemsAwaitingTreatment().iterator();
            while (i.hasNext()) {
                HealthProblem problem = i.next();
                if (problem.getSufferer().equals(person)) {
                    medicalAid.cancelRequestTreatment(problem);
                }
            }

            // Stop any treatment of health problems of the person.
            Iterator<HealthProblem> j = medicalAid.getProblemsBeingTreated().iterator();
            while (j.hasNext()) {
                HealthProblem problem = j.next();
                if (problem.getSufferer().equals(person)) {
                    medicalAid.stopTreatment(problem);
                }
            }
        }
    }
}