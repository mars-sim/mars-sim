/**
 * Mars Simulation Project
 * MedicalHelp.java
 * @version 2.74 2002-03-11
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.person.ai;

import java.io.Serializable;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.Infirmary;
import org.mars_sim.msp.simulation.structure.FacilityManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.medical.*;

/**
 * THis class represents a Task that requires a Person to provide Medical
 * help to someelse. It relies on looking for any Sick Bays in the
 * current location.
 */
public class MedicalAssistance extends Task implements Serializable {

    private final static String MEDICAL = "Medical";

    private double duration;            // How long for treatment
    private HealthProblem   problem;    // Problem being cured.

    /** Constructs a Medical help object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public MedicalAssistance(Person person, Mars mars) {
        super("Medical Assistance", person, true, mars);

        SickBay sickbay = getSickbay(person);
        problem = sickbay.getCurableProblem();

        int skill = person.getSkillManager().getSkillLevel(MEDICAL);

        Treatment treatment = problem.getIllness().getRecoveryTreatment();
	    description = "Apply " + treatment.getName();
        duration = treatment.getAdjustedDuration(10);

        // I fthe duration is negative, then the Treatment run in parallel to
        // the recovery and last the same time.
        if (duration > 0D) {
            problem.startTreatment(duration);
        }
        else {
            problem.startRecovery();
            duration = problem.getIllness().getRecoveryPeriod();
        }

        System.out.println(person.getName() + " starts " + description +
                            " for " + duration + " on " + problem.getSufferer());
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

	    return result;
    }

    static private SickBay getSickbay(Person person) {
        Settlement location = person.getSettlement();
        FacilityManager mgr = location.getFacilityManager();
        return ((Infirmary)mgr.getFacility(Infirmary.NAME)).getSickBay();
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

        timeCompleted += time;
        if (timeCompleted > duration) {
            done = true;
            // Add experience points for 'Medical' skill.
            // Add one point for every 100 millisols.
            double newPoints = duration / 100D;
            int experienceAptitude = person.getNaturalAttributeManager().getAttribute("Experience Aptitude");
            newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
            person.getSkillManager().addExperience(MEDICAL, newPoints);

            System.out.println(person.getName() + " done " + problem +
                               " skill " + newPoints);
            return timeCompleted - duration;
        }
        else {
            return 0;
        }
    }
}
