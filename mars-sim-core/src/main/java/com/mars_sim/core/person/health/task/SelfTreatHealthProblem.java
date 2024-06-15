/**
 * Mars Simulation Project
 * SelfTreatHealthProblem.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package com.mars_sim.core.person.health.task;

import java.util.Comparator;
import java.util.Set;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.MedicalCare;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.SickBay;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.Msg;

/**
 * A task for performing a medical self-treatment at a medical station.
 */
public class SelfTreatHealthProblem extends TreatHealthProblem {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(SelfTreatHealthProblem.class.getName());
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.selfTreatHealthProblem"); //$NON-NLS-1$


     /**
      * Factory method to create a self treating task for a problem the person has.
      *
      * @param p Person with a problem.
     */       
    static SelfTreatHealthProblem createTask(Person p) {
        // Get the problem that perosn can treat themselves
        var curable = MedicalHelper.getTreatableHealthProblems(p, p.getPhysicalCondition().getProblems(), true);
        if (curable.isEmpty()) {
            logger.warning(p, "Has no self-treatable health problem.");
            return null;
        }

        MedicalAid aid = null;
        // Choose available medical aid for treatment.
        if (p.isInSettlement()) {
            aid = MedicalHelper.determineMedicalAidAtSettlement(p.getAssociatedSettlement(), curable);
        }
        else if (p.isInVehicle() && (p.getVehicle() instanceof Rover r)) {
            aid = MedicalHelper.determineMedicalAidInRover(r, curable);
        }
        
        if (aid == null) {
            logger.warning(p, "Location does not allow self-treatment of health problem.");
            return null;
        }

        // Determine which health problem to treat.
        var healthProblem = determineHealthProblemToTreat(aid, curable);
        if (healthProblem == null) {
            logger.warning(p, "Has no self-treatable health problem.");
            return null;
        }

        return new SelfTreatHealthProblem(p, aid, healthProblem);
    }

    /**
     * Constructor.
     * @param healer the person to perform the task
     * @param problem Problem being treated
     * @param aid Where teh treatment is taking place
     */
    private SelfTreatHealthProblem(Person healer, MedicalAid aid, HealthProblem problem) {
        super(NAME, healer, aid, problem);

        // Has the treatment been queued
        if (!aid.getProblemsAwaitingTreatment().contains(problem)) {
            logger.info(healer, problem + " requesting treatment treatment.");
            aid.requestTreatment(problem);
        }

        // Walk to medical aid.
        if (aid instanceof MedicalCare medicalCare) {     
            // Walk to medical care building.
            walkToTaskSpecificActivitySpotInBuilding(medicalCare.getBuilding(), FunctionType.MEDICAL_CARE, false);
        }
        else if (aid instanceof SickBay sb) {
            // Walk to medical activity spot in rover.
            Vehicle vehicle = sb.getVehicle();
            if (vehicle instanceof Rover r) {

                // Walk to rover sick bay activity spot.
                walkToSickBayActivitySpotInRover(r, false);
            }
        }
        else {
            logger.severe(person, "Medical aid could not be determined.");
            endTask();
        }
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
