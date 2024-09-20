/**
 * Mars Simulation Project
 * SelfTreatHealthProblem.java
 * @date 2024-09-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.health.task;

import java.util.Comparator;
import java.util.Set;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.core.tool.Msg;

/**
 * A task for performing a medical self-treatment at a medical station.
 */
public class SelfTreatHealthProblem extends TreatHealthProblem {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static final SimLogger logger = SimLogger.getLogger(SelfTreatHealthProblem.class.getName());
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.selfTreatHealthProblem"); //$NON-NLS-1$

     /**
      * Factory method to create a self treating task for a problem the person has.
      *
      * @param p Person with a problem.
     */       
    static SelfTreatHealthProblem createTask(Person p) {
        // Get the problem that person can treat themselves
        var curable = MedicalHelper.getTreatableHealthProblems(p, p.getPhysicalCondition().getProblems(), true);
        if (curable.isEmpty()) {
            logger.warning(p, "Found no self-treatable health problem.");
            return null;
        }

        MedicalAid aid =  MedicalHelper.determineMedicalAid(p, curable);
        if (aid == null) {
            logger.warning(p, "Location does not allow self-treatment of health problem.");
            return null;
        }

        // Determine which health problem to treat.
        var healthProblem = determineHealthProblemToTreat(aid, curable);
        if (healthProblem == null) {
            logger.warning(p, "Found no self-treatable health problem.");
            return null;
        }

        return new SelfTreatHealthProblem(p, aid, healthProblem);
    }

    /**
     * Constructor.
     * 
     * @param healer the person to perform the task
     * @param problem Problem being treated
     * @param aid Where the treatment is taking place
     */
    private SelfTreatHealthProblem(Person healer, MedicalAid aid, HealthProblem problem) {
        super(NAME, healer, aid, problem);

        // Has the treatment been queued
        if (!aid.getProblemsAwaitingTreatment().contains(problem)) {
            logger.info(healer, problem + " requesting treatment treatment.");
            aid.requestTreatment(problem);
        }

        walkToMedicalAid(true);
    }

    /**
     * Determines the most serious health problem to self-treat.
     * @param curable Problems that are curable
     * @param aid Medical aid available
     * @return health problem or null if none found.
     */
    private static HealthProblem determineHealthProblemToTreat(MedicalAid aid, Set<HealthProblem> curable) {

        var found = curable.stream()
                        .filter(aid::canTreatProblem)
                        .max(Comparator.comparingInt(v -> v.getComplaint().getSeriousness()));
        if (found.isPresent()) {
            return found.get();
        }
        return null;
    }
}
