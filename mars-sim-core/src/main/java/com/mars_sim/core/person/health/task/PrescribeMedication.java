/*
 * Mars Simulation Project
 * PrescribeMedication.java
 * @date 2023-09-17
 * @author Scott Davis
 */
package com.mars_sim.core.person.health.task;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.health.AnxietyMedication;
import com.mars_sim.core.person.health.Medication;
import com.mars_sim.core.person.health.RadiationExposure;
import com.mars_sim.core.person.health.RadioProtectiveAgent;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * A task in which a doctor prescribes (and provides) a medication to a patient.
 */
public class PrescribeMedication extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(PrescribeMedication.class.getName());
    
	/** SImple Task name */
	static final String SIMPLE_NAME = PrescribeMedication.class.getSimpleName();
	
	/** Task description name */
    private static final String NAME = Msg.getString(
            "Task.description.prescribeMedication"); //$NON-NLS-1$

	private static final double AVERAGE_MEDICAL_WASTE = .1 * RandomUtil.getRandomDouble(2);

    /** Task phases. */
    private static final TaskPhase MEDICATING = new TaskPhase(Msg.getString(
            "Task.phase.medicating")); //$NON-NLS-1$

    private static final ExperienceImpact IMPACT = new ExperienceImpact(10D, NaturalAttributeType.EXPERIENCE_APTITUDE,
                                                            false, 0.1D, SkillType.MEDICINE);

	// Data members.
	private Person patient = null;

	/**
	 * A Worker wants to to issue some medication a person
	 * 
	 * @param pharmacist the person performing the task.
	 */
	PrescribeMedication(Worker pharmacist) {
        // Use task constructor.
        super(NAME, pharmacist, false, IMPACT, 10D);

        // Determine patient needing medication
        patient = determinePatient(person);
        if (patient != null) {
        	
            if (person.isOutside())
            	endTask();
            // If in settlement, move doctor to building patient is in.
            else if (person.isInSettlement() && patient.getBuildingLocation() != null) {

                // Walk to patient's building.
            	walkToActivitySpotInBuilding(patient.getBuildingLocation(), FunctionType.MEDICAL_CARE, false);
            	
    			Task currentTask = patient.getMind().getTaskManager().getTask();
    			if (currentTask != null && !currentTask.getName().equalsIgnoreCase(RequestMedicalTreatment.NAME)) {
                	patient.getMind().getTaskManager().addPendingTask(RequestMedicalTreatment.SIMPLE_NAME);
    			}
            }
            else
            	endTask();

        }
        else {
            endTask();
        }

        // Initialize phase
        setPhase(MEDICATING);
    }

    /**
     * Who is being treated
     * @return
     */
    Person getPatient() {
        return patient;
    }

    /**
     * Determines source of patients
     * 
     * @param pharmacist the Worker prescribing the medication.
     * @return patient if one found, null otherwise.
     */
    static Collection<Person> determinePatients(Worker pharmacist) {

        // Get possible patient list.
        // Note: Doctor can also prescribe medication for himself.
        Collection<Person> patientList = null;
        if (pharmacist.isInSettlement()) {
            patientList = pharmacist.getSettlement().getIndoorPeople();
        }
        else {
            patientList = Collections.emptyList();
        }
        return patientList;
    }

    /**
     * Does this person need medication
     */
    static boolean needsMedication(Person patient) {
        PhysicalCondition condition = patient.getPhysicalCondition();
        RadiationExposure exposure = condition.getRadiationExposure();
        return (!condition.isDead()
            && ((condition.isStressedOut() && !condition.hasMedication(AnxietyMedication.NAME))
                || (exposure.isSick() && !condition.hasMedication(RadioProtectiveAgent.NAME))));
    }

    /**
     * Determines if there is a patient nearby needing medication.
     * 
     * @param pharmacist the Worker prescribing the medication.
     * @return patient if one found, null otherwise.
     */
    static Person determinePatient(Worker pharmacist) {

        // Get possible patient list.
        // Note: Doctor can also prescribe medication for himself.
        Collection<Person> patientList = determinePatients(pharmacist);

        // Determine patient.
        for(Person person : patientList) {
            if (needsMedication(person)) {
                return person;
            }
        }

        return null;
    }

    /**
     * Performs the medicating phase.
     * 
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double medicatingPhase(double time) {

        // Add experience.
        addExperience(time);

        // If duration, provide medication.
        if (getDuration() <= (getTimeCompleted() + time)) {
            var patientLocn = patient.getBuildingLocation();
            if (patientLocn != null) {
                PhysicalCondition condition = patient.getPhysicalCondition();

                Medication medication = null;
                if (condition.isRadiationPoisoned()) {
                	medication = new RadioProtectiveAgent(patient);                    
                }
                else if (condition.isStressedOut()) {
                	medication = new AnxietyMedication(patient); 
                }

                // Check if patient already has taken medication.
                if ((medication == null) || condition.hasMedication(medication.getName())) {
                    endTask();
                    return 0D;
                }

                // Medicate patient.
                condition.addMedication(medication);
 
                StringBuilder phrase = new StringBuilder();
                
                if (!worker.equals(patient)) {
                    phrase = phrase.append("Prescribing ").append(medication.getName())
                        .append(" to ").append(patient.getName()).append(" in ")
                        .append(patientLocn.getName())
                        .append("."); 
                }
                else {
                    phrase = phrase.append("Self-prescribing ").append(medication.getName()); 
                }
                logger.log(worker, Level.INFO, 5000, phrase.toString());
                
                produceMedicalWaste();
            }
            else 
            	logger.info(patient, "Is not in a proper place to receive medication.");
        }

        return 0D;
    }


	private void produceMedicalWaste() {
		if (!worker.isOutside()) {
            worker.storeAmountResource(ResourceUtil.toxicWasteID, AVERAGE_MEDICAL_WASTE);
        }
	}

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (MEDICATING.equals(getPhase())) {
            return medicatingPhase(time);
        }
        else {
            return time;
        }
    }
}
