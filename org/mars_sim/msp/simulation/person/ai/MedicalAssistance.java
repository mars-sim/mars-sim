/**
 * Mars Simulation Project
 * MedicalHelp.java
 * @version 2.74 2002-05-01
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.person.ai;

import java.io.Serializable;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.medical.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.simulation.malfunction.*;

/**
 * THis class represents a Task that requires a Person to provide Medical
 * help to someelse. It relies on looking for any Sick Bays in the
 * current location.
 */
public class MedicalAssistance extends Task implements Serializable {

    private final static String MEDICAL = "Medical";

    private double duration; // How long for treatment

    /** Constructs a Medical help object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public MedicalAssistance(Person person, Mars mars) {
        super("Medical Assistance", person, true, mars);

        SickBay sickbay = getSickbay(person);
        HealthProblem problem = sickbay.getCurableProblem();

        int skill = person.getSkillManager().getSkillLevel(MEDICAL);

        Treatment treatment = problem.getIllness().getRecoveryTreatment();
	    description = "Apply " + treatment.getName();
        duration = treatment.getAdjustedDuration(10);

        // Start the treatment and update sickBay
        problem.startTreatment(duration);
        sickbay.startTreatment(problem);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;

        SickBay infirmary = getSickbay(person);
        if ((infirmary != null) && infirmary.hasWaitingPatients()) {
            result = 50D * person.getPerformanceRating();
        }

        if (infirmary.getOwner().getMalfunctionManager().hasMalfunction())
	    result = 0D;
	
        return result;
    }

    /**
     * Gets the local sickbay.
     * Returns null if none.
     * @return sickbay
     */
    static private SickBay getSickbay(Person person) {
	String location = person.getLocationSituation();
	if (location.equals(person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
            FacilityManager mgr = settlement.getFacilityManager();
            return ((Infirmary)mgr.getFacility(Infirmary.NAME)).getSickBay();
	}
	if (location.equals(person.INVEHICLE)) {
	    Vehicle vehicle = person.getVehicle();
	    if (vehicle instanceof TransportRover) 
	        return (SickBay) ((TransportRover) vehicle).getMedicalFacility();
        }

	return null;
    }

    /** This task simply waits until the set duration of the task is complete, then ends the task.
     *  @param time the amount of time to perform this task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) done = true;

        // If sickbay owner has malfunction, end task.
        if (getSickbay(person).getOwner().getMalfunctionManager().hasMalfunction()) done = true;

	if (done) return timeLeft;
	
        // Check for accident in infirmary.
	checkForAccident(time);
	
        timeCompleted += time;
        if (timeCompleted > duration) {
            done = true;
            // Add experience points for 'Medical' skill.
            // Add one point for every 100 millisols.
            double newPoints = duration / 100D;
            int experienceAptitude = person.getNaturalAttributeManager().getAttribute("Experience Aptitude");
            newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
            person.getSkillManager().addExperience(MEDICAL, newPoints);

            return timeCompleted - duration;
        }
        else {
            return 0;
        }
    }

    /**
     * Check for accident in infirmary.
     * @param time the amount of time working (in millisols)
     */
    private void checkForAccident(double time) {

        Malfunctionable entity = getSickbay(person).getOwner();	
	
        double chance = .001D;
	    
        // Medical skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel("Medical");
        if (skill <= 3) chance *= (4 - skill);
	else chance /= (skill - 2);

	if (RandomUtil.lessThanRandPercent(chance * time)) {
	    System.out.println(person.getName() + " has accident during medical assistance.");
            entity.getMalfunctionManager().accident();
        }
    }
}
