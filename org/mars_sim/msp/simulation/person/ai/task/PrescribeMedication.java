/**
 * Mars Simulation Project
 * PrescribeMedication.java
 * @version 2.86 2009-05-13
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PhysicalCondition;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.SkillManager;
import org.mars_sim.msp.simulation.person.ai.job.Doctor;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.person.medical.AntiStressMedication;
import org.mars_sim.msp.simulation.person.medical.Medication;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.vehicle.Crewable;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/**
 * A task in which a doctor prescribes (and provides) a medication to a patient.
 */
public class PrescribeMedication extends Task implements Serializable {

    private static String CLASS_NAME = 
        "org.mars_sim.msp.simulation.person.ai.task.PrescribeMedication";
    
    private static Logger logger = Logger.getLogger(CLASS_NAME);
    
    // Task phase
    private static final String MEDICATING = "Medicating";
    
    // The stress modified per millisol.
    private static final double STRESS_MODIFIER = 0D;
    
    // Data members.
    Person patient = null;
    Medication medication = null;
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @throws Exception if error creating task.
     */
    public PrescribeMedication(Person person) throws Exception {
        // Use task constructor.
        super("Prescribing Medication", person, true, false, STRESS_MODIFIER, true, 10D);
        
        // Determine patient needing medication.
        patient = determinePatient(person);
        if (patient != null) {
            // Determine medication to prescribe.
            medication = determineMedication(patient);
            
            // If in settlement, move doctor to building patient is in.
            if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
                try {
                    Building doctorBuilding = BuildingManager.getBuilding(person);
                    Building patientBuilding = BuildingManager.getBuilding(patient);
                    if (doctorBuilding != patientBuilding) 
                        BuildingManager.addPersonToBuilding(person, patientBuilding);
                }
                catch (BuildingException e) {
                    logger.log(Level.SEVERE,"PrescribeMedication.constructor(): " + e.getMessage());
                    endTask();
                }
            }
            
            logger.info(person.getName() + " prescribing " + medication.getName() + 
                    " to " + patient.getName());
        }
        else endTask();
        
        // Initialize phase
        addPhase(MEDICATING);
        setPhase(MEDICATING);
    }
    
    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given 
     *  the person and his/her situation.
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        // Only doctor job allowed to perform this task.
        Job job = person.getMind().getJob();
        if (job instanceof Doctor) {
            
            // Determine patient needing medication.
            Person patient = determinePatient(person);
            if (patient != null) {
                result = 100D;
            }
        }
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        return result;
    }
    
    /**
     * Determines if there is a patient nearby needing medication.
     * @param doctor the doctor prescribing the medication.
     * @return patient if one found, null otherwise.
     */
    private static Person determinePatient(Person doctor) {
        Person result = null;
        
        // Get possible patient list.
        // Note: Doctor can also prescribe medication for himself.
        Collection<Person> patientList = null;
        String loc = doctor.getLocationSituation();
        if (loc.equals(Person.INSETTLEMENT)) {
            patientList = doctor.getSettlement().getInhabitants();
        }
        else if (loc.equals(Person.INVEHICLE)) {
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
                if (condition.getStress() >= 100D) {
                    // Only prescribing anti-stress medication at the moment.
                    if (!condition.hasMedication(AntiStressMedication.NAME)) {
                        result = person;
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Determines a medication for the patient.
     * @param patient the patient to medicate.
     * @return medication.
     */
    private Medication determineMedication(Person patient) {
        // Only allow anti-stress medication for now.
        return new AntiStressMedication(patient);
    }
    
    /**
     * Performs the medicating phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double medicatingPhase(double time) throws Exception {
        
        // If duration, provide medication.
        if (getDuration() < (getTimeCompleted() + time)) {
            if (patient != null) {
                if (medication != null) {
                    PhysicalCondition condition = patient.getPhysicalCondition();
                    
                    // Check if patient already has taken medication.
                    if (!condition.hasMedication(medication.getName())) {
                        // Medicate patient.
                        condition.addMedication(medication);
                    }
                }
                else throw new Exception("medication is null");
            }
            else throw new Exception ("patient is null");
        }
        
        // Add experience.
        addExperience(time);
        
        return 0D;
    }
    
    @Override
    protected void addExperience(double time) {
        // Add experience to "Medical" skill
        // (1 base experience point per 10 millisols of work)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 10D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute(
            NaturalAttributeManager.EXPERIENCE_APTITUDE);
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(Skill.MEDICAL, newPoints);
    }

    @Override
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(1);
        results.add(Skill.MEDICAL);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        return manager.getEffectiveSkillLevel(Skill.MEDICAL);
    }

    @Override
    protected double performMappedPhase(double time) throws Exception {
        if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
        if (MEDICATING.equals(getPhase())) return medicatingPhase(time);
        else return time;
    }
}