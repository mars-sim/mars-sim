/**
 * Mars Simulation Project
 * TreatMedicalPatient.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskEvent;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.MedicalAid;
import org.mars_sim.msp.core.person.health.Treatment;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.SickBay;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A task for performing a medical treatment on a patient at a medical station.
 * This task is not for performing a medical treatment on one's self.
 */
public class TreatMedicalPatient extends Task {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(TreatMedicalPatient.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.treatMedicalPatient"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase TREATMENT = new TaskPhase(Msg.getString(
            "Task.phase.treatingMedicalPatient")); //$NON-NLS-1$

    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = 1D;

    // Data members.
    private double duration;
    private double treatmentTime;
    
    private MedicalAid medicalAid;
    private Person patient;
    private HealthProblem healthProblem;

    /**
     * Constructor.
     * @param person the person to perform the task
     */
    public TreatMedicalPatient(Person person) {
        super(NAME, person, true, true, STRESS_MODIFIER, SkillType.MEDICINE, 25D);

        treatmentTime = 0D;

        // Choose available medical aid for treatment.
        medicalAid = determineMedicalAid(person);

        if (medicalAid != null) {

            // Determine patient and health problem to treat.
            healthProblem = determineHealthProblemToTreat(medicalAid);
            if (healthProblem != null) {

                patient = healthProblem.getSufferer();

                // Get the person's medical skill.
                int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);

                // Determine medical treatment.
                Treatment treatment = healthProblem.getIllness().getRecoveryTreatment();
                if (treatment != null) {
                    duration = treatment.getAdjustedDuration(skill);
                    setStressModifier(STRESS_MODIFIER * treatment.getSkill());
                }
                else {
                    logger.severe(person, healthProblem + " does not have treatment.");
                    endTask();
                }
            }
            else {
                logger.severe(person, "Could not find a treatable health problem at " + medicalAid);
                endTask();
            }

            // Walk to medical aid.
            if (medicalAid instanceof MedicalCare) {
                // Walk to medical care building.
                MedicalCare medicalCare = (MedicalCare) medicalAid;

                // Walk to medical care building.
                walkToTaskSpecificActivitySpotInBuilding(medicalCare.getBuilding(), FunctionType.MEDICAL_CARE, false);
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
            logger.severe(person, "Medical aid could not be determined.");
            endTask();
        }

        // Initialize phase.
        addPhase(TREATMENT);
        setPhase(TREATMENT);
    }

    /**
     * Determines a local medical aid to use.
     * @return medical aid or null if none found.
     */
    private MedicalAid determineMedicalAid(Person doctor) {

        MedicalAid result = null;

        if (doctor.isInSettlement()) {
            result = determineMedicalAidAtSettlement();
        }
        else if (doctor.isInVehicle()) {
            result = determineMedicalAidInVehicle();
        }

        return result;
    }

    /**
     * Determines a medical aid at a settlement to use.
     * 
     * @return medical aid or null if none found.
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

            if (!malfunction) {

                // Check if there are any treatable medical problems at building.
                MedicalCare medicalCare = building.getMedical();
                if (hasTreatableHealthProblems(medicalCare)) {
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

    /**
     * Determines a medical aid in a vehicle to use.
     * @return medical aid or null if none found.
     */
    private MedicalAid determineMedicalAidInVehicle() {

        MedicalAid result = null;

        if (person.getVehicle() instanceof Rover) {
            Rover rover = (Rover) person.getVehicle();
            if (rover.hasSickBay()) {
                SickBay sickBay = rover.getSickBay();
                if (hasTreatableHealthProblems(sickBay)) {
                    result = sickBay;
                }
            }
        }

        return result;
    }

    /**
     * Checks if a medical has health problems that the person can treat.
     * @param aid the medical aid.
     * @return true if treatable medical problems.
     */
    private boolean hasTreatableHealthProblems(MedicalAid aid) {

        boolean result = false;

        // Get the person's medical skill.
        int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);

        // Check if there are any treatable health problems awaiting treatment.
        Iterator<HealthProblem> j = aid.getProblemsAwaitingTreatment().iterator();
        while (j.hasNext() && !result) {
            HealthProblem problem = j.next();
            Treatment treatment = problem.getIllness().getRecoveryTreatment();
            if (treatment != null) {
                int requiredSkill = treatment.getSkill();
                if (skill >= requiredSkill) {
                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * Determines a health problem to treat.
     * @param medicalAid the medical aid.
     * @return health problem or null if none found.
     */
    private HealthProblem determineHealthProblemToTreat(MedicalAid medicalAid) {

        HealthProblem result = null;

        // Get the person's medical skill.
        int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);

        // Determine health problems that person can treat.
        List<HealthProblem> treatableHealthProblems = new ArrayList<HealthProblem>();
        Iterator<HealthProblem> i = medicalAid.getProblemsAwaitingTreatment().iterator();
        while (i.hasNext()) {
            HealthProblem problem = i.next();
            Treatment treatment = problem.getIllness().getRecoveryTreatment();
            if (treatment != null) {
                int requiredSkill = treatment.getSkill();
                if (skill >= requiredSkill) {
                    treatableHealthProblems.add(problem);
                }
            }
        }

        if (treatableHealthProblems.size() > 0) {

            // Get random health problem from treatable list.
            int index = RandomUtil.getRandomInt(treatableHealthProblems.size() - 1);
            result = treatableHealthProblems.get(index);
        }

        return result;
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (TREATMENT.equals(getPhase())) {
            return treatmentPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the treatment phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left over after performing the phase.
     */
    private double treatmentPhase(double time) {

        double timeLeft = 0D;
        Malfunctionable entity = getMalfunctionable();
        
        // If medical aid has malfunction, end task.
        if (entity.getMalfunctionManager().hasMalfunction()) {
            endTask();
        }

        if (isDone()) {
			endTask();
            return time;
        }

        // Start treatment if not already started.
        if (healthProblem.getAwaitingTreatment()) {

            medicalAid.startTreatment(healthProblem, duration);
    		logger.log(worker, Level.INFO, 0, "Treating " + patient + " for " + healthProblem.getIllness().getType().getName() + ".");
    		
            // Create starting task event if needed.
            if (getCreateEvents()) {
                TaskEvent startingEvent = new TaskEvent(person, // unit
                		this,  // task
                		healthProblem.getSufferer(), // source
                		EventType.TASK_START,  // event type
                		NAME // description
                ); 
                registerNewEvent(startingEvent);
            }
        }

        // Check for accident in medical aid.
        checkForAccident(entity, 0.005D, time);

        treatmentTime += time;
        if (treatmentTime >= duration) {
            healthProblem.startRecovery();
            timeLeft = treatmentTime - duration;
            endTask();
        }

        // Add experience.
        addExperience(time);

        return timeLeft;
    }

    /**
     * Gets the malfunctionable associated with the medical aid.
     * @return the associated Malfunctionable
     */
    private Malfunctionable getMalfunctionable() {
        Malfunctionable result = null;

        if (medicalAid instanceof SickBay) {
            result = ((SickBay) medicalAid).getVehicle();
        }
        else if (medicalAid instanceof MedicalCare) {
            result = ((MedicalCare) medicalAid).getBuilding();
        }
        else {
            result = (Malfunctionable) medicalAid;
        }

        return result;
    }

 
	/**
	 * Gets the medical aid the person is using for this task.
	 * 
	 * @return medical aid or null.
	 */
	public MedicalAid getMedicalAid() {
		return medicalAid;
	}
	
	/**
	 * Stop the treatment
	 */
    @Override
    protected void clearDown() {
        // Stop treatment.
        if (medicalAid != null && medicalAid.getProblemsBeingTreated().contains(healthProblem)) {
            medicalAid.stopTreatment(healthProblem);
        }
    }
}
