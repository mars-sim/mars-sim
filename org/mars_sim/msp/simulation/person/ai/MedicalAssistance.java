/**
 * Mars Simulation Project
 * MedicalHelp.java
 * @version 2.75 2003-06-19
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.person.ai;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.function.MedicalCare;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.medical.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.simulation.malfunction.Malfunctionable;

/**
 * This class represents a task that requires a person to provide medical
 * help to someone else. 
 */
public class MedicalAssistance extends Task implements Serializable {

    private final static String MEDICAL = "Medical";

    private MedicalAid medical;    // The medical station the person is at.
    private double duration;       // How long for treatment
    private HealthProblem problem; // Health problem to treat.

    /** 
     * Constructor
     *
     * @param person the person to perform the task
     * @param mars the virtual Mars
     */
    public MedicalAssistance(Person person, Mars mars) {
        super("Medical Assistance", person, true, mars);
        
        // Sets this task to create historical events.
        setCreateEvents(true);

        // Get a local medical aid that needs work.
        List localAids = getNeedyMedicalAids(person);
        if (localAids.size() > 0) {
            int rand = RandomUtil.getRandomInt(localAids.size() - 1);
            medical = (MedicalAid) localAids.get(rand);
        
            // Get a curable medical problem waiting for treatment at the medical aid.
            problem = (HealthProblem) medical.getProblemsAwaitingTreatment().get(0);

            // Get the person's medical skill.
            int skill = person.getSkillManager().getEffectiveSkillLevel(MEDICAL);
            
            // Treat medical problem.
            Treatment treatment = problem.getIllness().getRecoveryTreatment();
	        description = "Apply " + treatment.getName();
            duration = treatment.getAdjustedDuration(skill);
            
            // Start the treatment
            try {
                medical.startTreatment(problem, duration);
            }
            catch (Exception e) {
                System.out.println("MedicalAssistance: " + e.getMessage());
                endTask();
            }
        }
        else endTask();
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;

        // Get the local medical aids to use.
        if (getNeedyMedicalAids(person).size() > 0) result = 50D;
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        return result;
    }

    /**
     * Gets the local medical aids that have patients waiting.
     * 
     * @return List of medical aids
     */
    private static List getNeedyMedicalAids(Person person) {
        List result = new ArrayList();
        
        String location = person.getLocationSituation();
        if (location.equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
            List infirmaries = settlement.getBuildingManager().getBuildings(MedicalCare.class);
            Iterator i = infirmaries.iterator();
            while (i.hasNext()) {
                MedicalAid aid = (MedicalAid) i.next();
                if (isNeedyMedicalAid(aid)) result.add(aid);
            }
        }
        if (location.equals(Person.INVEHICLE)) {
            Vehicle vehicle = person.getVehicle();
            if (vehicle instanceof TransportRover) {
                MedicalAid aid = ((TransportRover) vehicle).getMedicalFacility();
                if (isNeedyMedicalAid(aid)) result.add(aid);
            }
        }

        return result;
    }
    
    /**
     * Checks if a medical aid needs work.
     *
     * @return true if medical aid has patients waiting and is not malfunctioning.
     */
    private static boolean isNeedyMedicalAid(MedicalAid aid) {
        boolean waitingProblems = (aid.getProblemsAwaitingTreatment().size() > 0);
        boolean malfunction = getMalfunctionable(aid).getMalfunctionManager().hasMalfunction();
        if (waitingProblems && !malfunction) return true;
        else return false;
    }
    
    /**
     * Gets the malfunctionable associated with the medical aid.
     *
     * @param aid The medical aid
     * @return the associated Malfunctionable
     */
    private static Malfunctionable getMalfunctionable(MedicalAid aid) {
        Malfunctionable result = null;
        
        if (aid instanceof SickBay) result = ((SickBay) aid).getVehicle();
        else if (aid instanceof Building) result = (Building) aid;
        else result = (Malfunctionable) aid;
        
        return result;
    }

    /** This task simply waits until the set duration of the task is complete, then ends the task.
     *  @param time the amount of time to perform this task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();

        // If sickbay owner has malfunction, end task.
        if (getMalfunctionable(medical).getMalfunctionManager().hasMalfunction()) endTask();

        if (isDone()) return timeLeft;

        // Check for accident in infirmary.
        checkForAccident(time);

        timeCompleted += time;
        if (timeCompleted > duration) {
            // Add experience points for 'Medical' skill.
            // Add one point for every 100 millisols.
            double newPoints = duration / 100D;
            int experienceAptitude = person.getNaturalAttributeManager().getAttribute("Experience Aptitude");
            newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
            person.getSkillManager().addExperience(MEDICAL, newPoints);

            problem.startRecovery();
            endTask();
            return timeCompleted - duration;
        }
        else return 0;
    }

    /**
     * Check for accident in infirmary.
     * @param time the amount of time working (in millisols)
     */
    private void checkForAccident(double time) {

        Malfunctionable entity = getMalfunctionable(medical);

        double chance = .001D;

        // Medical skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel("Medical");
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has accident during medical assistance.");
            entity.getMalfunctionManager().accident();
        }
    }
    
    /**
     * Ends the task and performs any final actions.
     */
    public void endTask() {
        super.endTask();
        
        // Stop treatment.
        try {
            if (!problem.getRecovering()) medical.stopTreatment(problem);
        }
        catch (Exception e) {
            System.out.println("MedicalAssistance.endTask(): " + e.getMessage());
        }
    }
    
    /**
     * Gets the medical aid the person is using for this task.
     *
     * @return medical aid or null.
     */
    public MedicalAid getMedicalAid() {
        return medical;
    }
}
