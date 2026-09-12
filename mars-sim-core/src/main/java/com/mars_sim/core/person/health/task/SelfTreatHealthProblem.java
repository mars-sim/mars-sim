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

//       	if (healer.isSuperUnfit()) {
//    		logger.info(worker, "Super Unfit.");
//    		endTask();
//    		return;
//    	}
       	
        // Check queuing the treatment
        if (!aid.getProblemsAwaitingTreatment().contains(problem)) {
            logger.info(healer, "Queuing self-treatment of a health problem, namely, " + problem + ".");
            aid.requestTreatment(problem);
        }
        
        // Note: For now, no need of checking for the location state of the doctor
        
        // Future: Simulate offering telemedicine via mission control if a person is on a mission and in a vehicle

        if (healer.isInVehicleInGarage()) {
        	logger.info(healer, 10_000, "Starting in-garaged-vehicle self-treatment of health problem " + problem + ".");
        }
        
        else if (healer.isInSettlement()) {
        	logger.info(healer, 10_000, "Starting in-settlement self-treatment of health problem " + problem + ".");
        	
//	       	// Send the person as a patient to a medical bed
//            else if (BuildingManager.addPatientToMedicalBed(healer, healer.getSettlement())) {
//    			logger.info(healer, 10_000, "Successfully being added to a medical bed during self-treatment.");
//    		}
//    		else {
//    			logger.info(healer, 10_000, "Unsuccessfully being added to a medical bed during self-treatment.");
//    		}
        }
        else if (healer.isInVehicle() ) {
        	logger.info(healer, 10_000, "Starting in-vehicle self-treatment of health problem " + problem + ".");
        }
        else {
        	logger.info(healer, 10_000, "Being outside and unable to start self-treatment of health problem " + problem + ".");
        	endTask();
        }
    }

    /**
     * Determines the most serious health problem to self-treat.
     * 
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
