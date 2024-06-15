/**
 * Mars Simulation Project
 * RequestMedicalTreatment.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package com.mars_sim.core.person.health.task;

import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.MedicalCare;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.SickBay;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.tools.Msg;

/**
 * A task for requesting and awaiting medical treatment at a medical station.
 */
public class RequestMedicalTreatment extends Task {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** Simple Task name */
	public static final String SIMPLE_NAME = RequestMedicalTreatment.class.getSimpleName();
	
    /** Task name */
    public static final String NAME = Msg.getString(
            "Task.description.requestMedicalTreatment"); //$NON-NLS-1$

    /** Task phases. */
    static final TaskPhase WAITING_FOR_TREATMENT = new TaskPhase(Msg.getString(
            "Task.phase.waitingForMedicalTreatment")); //$NON-NLS-1$
    static final TaskPhase TREATMENT = new TaskPhase(Msg.getString(
            "Task.phase.receivingMedicalTreatment")); //$NON-NLS-1$

    /** Maximum waiting duration in millisols. */
    private static final double MAX_WAITING_DURATION = 200D;

    private static final ExperienceImpact IMPACT = new ExperienceImpact(10D, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                                false, 0.3);

    // Data members.
    private double waitingDuration;
    private MedicalAid medicalAid;
    private boolean requested;

    /**
     * Constructor.
     * 
     * @param person the person to perform the task
     */
    public RequestMedicalTreatment(Person person) {
        super(NAME, person, false, IMPACT, 0);
	     
        // Get problems that need help
        var curable = getRequestableTreatment(person);
        if (curable.isEmpty()) {
        	endTask();
            return;
        }
        		
        // Choose available medical aid for treatment.
        if (person.isInSettlement()) {
            medicalAid = MedicalHelper.determineMedicalAidAtSettlement(person.getAssociatedSettlement(), curable);
        }
        else if (person.isInVehicle() && (person.getVehicle() instanceof Rover r)) {
            medicalAid = MedicalHelper.determineMedicalAidInRover(r, curable);
        }

        if (medicalAid != null) {
            if (medicalAid instanceof MedicalCare medicalCare) {
                // Walk to medical care building.
                Building b = medicalCare.getBuilding();
                if (b != null)
                	walkToActivitySpotInBuilding(b, FunctionType.MEDICAL_CARE, false);
            }
            else if (medicalAid instanceof SickBay sb) {
                // Walk to medical activity spot in rover.
                Vehicle vehicle = sb.getVehicle();
                if (VehicleType.isRover(vehicle.getVehicleType())) {
                    // Walk to rover sick bay activity spot.
                    walkToSickBayActivitySpotInRover((Rover) vehicle, false);
                }
            }
        }
        
        else {
            endTask();
        }

        // Initialize phase.
        setPhase(WAITING_FOR_TREATMENT);
    }

    /**
     * Find any health problems that this patient can ask for help.
     * @param person
     * @return
     */
    static Set<HealthProblem> getRequestableTreatment(Person person) {
        return person.getPhysicalCondition().getProblems().stream()
                                .filter(p -> (p.getComplaint().getRecoveryTreatment() != null) 
                                        && !p.getComplaint().getRecoveryTreatment().getSelfAdminister())
                               .collect(Collectors.toSet());
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
     * Performs the waiting for treatment phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left over after performing the phase.
     */
    private double waitingForTreatmentPhase(double time) {

        double remainingTime = 0D;

        // Check if any health problems are currently being treated.
        if (underTreatment()) {
            setPhase(TREATMENT);
            remainingTime = time;
        }
        else {
            // Request treatment on first waiting call
            if (!requested) {

                requested = true;

                // Check if health problems are awaiting treatment.
                for(HealthProblem problem : person.getPhysicalCondition().getProblems()) {
                    if (!medicalAid.getProblemsAwaitingTreatment().contains(problem)
                            && medicalAid.canTreatProblem(problem)) {
                        // Request treatment for health problem.
                        medicalAid.requestTreatment(problem);
                    }
                }
            }

            // Check have not waited too long
            waitingDuration += time;
            if (waitingDuration >= MAX_WAITING_DURATION) {
                // End task if longer than maximum waiting duration.
                remainingTime = waitingDuration - MAX_WAITING_DURATION;
                endTask();
            }
        }

        return remainingTime;
    }

    private boolean underTreatment() {
        for(HealthProblem problem : person.getPhysicalCondition().getProblems()) {
            if (medicalAid.getProblemsBeingTreated().contains(problem)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Performs the treatment phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left over after performing the phase.
     */
    private double treatmentPhase(double time) {

        double remainingTime = 0D;

        // Check if any health problems are currently being treated.
        if (!underTreatment()) {

            // Check if person still has health problems needing treatment.
            boolean treatableProblems = person.getPhysicalCondition().getProblems().stream()
                                    .anyMatch(hp -> medicalAid.getProblemsAwaitingTreatment().contains(hp));
            if (treatableProblems) {
                // If any remaining treatable problems, wait for treatment.
                setPhase(WAITING_FOR_TREATMENT);
                waitingDuration = 0D;
                remainingTime = time;
            }
            else {
                // Nothing being treated so done
                endTask();
            }
        }

        return remainingTime;
    }

	/**
	 * Gets the medical aid the person is using for this task.
	 * 
	 * @return medical aid or null.
	 */
	public MedicalAid getMedicalAid() {
		return medicalAid;
	}
}
