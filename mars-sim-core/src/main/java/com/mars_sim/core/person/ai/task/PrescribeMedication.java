/**
 * Mars Simulation Project
 * PrescribeMedication.java
 * @date 2023-09-17
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.health.AnxietyMedication;
import com.mars_sim.core.person.health.Medication;
import com.mars_sim.core.person.health.RadiationExposure;
import com.mars_sim.core.person.health.RadioProtectiveAgent;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

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

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;

	// Data members.
	private Person patient = null;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 */
	public PrescribeMedication(Person person) {
        // Use task constructor.
        super(NAME, person, true, false, STRESS_MODIFIER, SkillType.MEDICINE, 100D, 10D);

        // Determine patient needing medication
        patient = determinePatient(person);
        if (patient != null) {
        	
            if (person.isOutside())
            	endTask();
            // If in settlement, move doctor to building patient is in.
            else if (person.isInSettlement() && patient.getBuildingLocation() != null
				&& person.isNominallyFit() && !person.getMind().getTaskManager().hasSameTask(RequestMedicalTreatment.NAME)) {

                // Walk to patient's building.
            	walkToActivitySpotInBuilding(person.getBuildingLocation(), FunctionType.MEDICAL_CARE, false);
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
        addPhase(MEDICATING);
        setPhase(MEDICATING);
    }

	public PrescribeMedication(Robot robot) {
        // Use task constructor.
        super(NAME, robot, true, false, STRESS_MODIFIER, SkillType.MEDICINE, 100D, 10D);

        // Determine patient needing medication
        patient = determinePatient(robot);
        if (patient != null) {
            // If in settlement, move doctor to building patient is in.
            if (robot.isInSettlement() && patient.getBuildingLocation() != null) {
                // Walk to patient's building.
            	
                // Walk to patient's building.
            	walkToActivitySpotInBuilding(robot.getBuildingLocation(), FunctionType.MEDICAL_CARE, false);
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
        addPhase(MEDICATING);
        setPhase(MEDICATING);
    }

    /**
     * Determines if there is a patient nearby needing medication.
     * 
     * @param doctor the doctor prescribing the medication.
     * @return patient if one found, null otherwise.
     */
    public Person determinePatient(Person doctor) {
        Person result = null;

        // Get possible patient list.
        // Note: Doctor can also prescribe medication for himself.
        Collection<Person> patientList = null;
        if (doctor.isInSettlement()) {
            patientList = doctor.getSettlement().getIndoorPeople();
        }
        else if (doctor.isInVehicle()) {
            Vehicle vehicle = doctor.getVehicle();
            if (vehicle instanceof Crewable) {
                Crewable crewVehicle = (Crewable) vehicle;
                patientList = crewVehicle.getCrew();
            }
        }

        // Determine patient.
        if (patientList != null) {
            Iterator<Person> i = patientList.iterator();
            while (i.hasNext() && (result == null)) {
                Person person = i.next();
                PhysicalCondition condition = person.getPhysicalCondition();
                RadiationExposure exposure = condition.getRadiationExposure();
                if (!condition.isDead()) {
                	if (condition.isStressedOut()) {
                        // Only prescribing anti-stress medication at the moment.
                        if (!condition.hasMedication(AnxietyMedication.NAME)) {
                            result = person;
                        }
                	}
                	else if (exposure.isSick()) {
                        if (!condition.hasMedication(RadioProtectiveAgent.NAME)) {
                            result = person;
                        }
                	}
                }
            }
        }

        return result;
    }

    public Person determinePatient(Robot doctor) {
        Person result = null;

        // Get possible patient list.
        // Note: Doctor can also prescribe medication for himself.
        Collection<Person> patientList = null;
        if (doctor.isInSettlement()) {
            patientList = doctor.getSettlement().getIndoorPeople();
        }

        // Determine patient.
        if (patientList != null) {
            Iterator<Person> i = patientList.iterator();
            while (i.hasNext() && (result == null)) {
                Person person = i.next();
                PhysicalCondition condition = person.getPhysicalCondition();
                RadiationExposure exposure = condition.getRadiationExposure();
                if (!condition.isDead()) {
                	if (condition.isStressedOut()) {
                        // Only prescribing anti-stress medication at the moment.
                        if (!condition.hasMedication(AnxietyMedication.NAME)) {
                            result = person;
                        }
                	}
                	else if (exposure.isSick()) {
                        if (!condition.hasMedication(RadioProtectiveAgent.NAME)) {
                            result = person;
                        }
                	}
                }
            }
        }

        return result;
    }

    /**
     * Performs the medicating phase.
     * 
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double medicatingPhase(double time) {

        // If duration, provide medication.
        if (getDuration() <= (getTimeCompleted() + time)) {
            if (patient != null && patient.getSettlement() != null && patient.getBuildingLocation() != null) {
                PhysicalCondition condition = patient.getPhysicalCondition();

                boolean needMeds = false;
                Medication medication = null;
                
                if (condition.isRadiationPoisoned()) {
                
                	medication = new RadioProtectiveAgent(patient);                    
                    // Check if patient already has taken medication.
                    if (!condition.hasMedication(medication.getName())) {
                        // Medicate patient.
                        condition.addMedication(medication);
                        needMeds = true;              
                    }
                }
                
                else if (condition.isStressedOut()) {
                	
                	medication = new AnxietyMedication(patient);                	
                    // Check if patient already has taken medication.
                    if (!condition.hasMedication(medication.getName())) {
                        // Medicate patient.
                        condition.addMedication(medication);
                        needMeds = true;            		
                    }
                }
                
                if (needMeds) {
                	StringBuilder phrase = new StringBuilder();
                    
                	if (!worker.equals(patient)) {
                		phrase = phrase.append("Prescribing ").append(medication.getName())
                			.append(" to ").append(patient.getName()).append(" in ").append(patient.getBuildingLocation().getNickName())
                			.append("."); 
                	}
                	else {
                		phrase = phrase.append("Is self-prescribing ").append(medication.getName())
                    			.append(" to onself in ").append(person.getBuildingLocation().getNickName())
                    			.append("."); 
                	}
            		logger.log(worker, Level.INFO, 5000,  phrase.toString());
                }
                
                produceMedicalWaste();

                Building b = patient.getBuildingLocation();
                if (b != null && b.hasFunction(FunctionType.MEDICAL_CARE))
                	walkToActivitySpotInBuilding(b, FunctionType.MEDICAL_CARE, false);
            }
            else 
            	logger.info(patient, "Is not in a proper place to receive medication.");
        }

        // Add experience.
        addExperience(time);

        return 0D;
    }


	public void produceMedicalWaste() {
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
