/**
 * Mars Simulation Project
 * SelfTreatHealthProblem.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package com.mars_sim.core.person.health.task;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.core.person.health.Treatment;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.MedicalCare;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.SickBay;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

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
    public static SelfTreatHealthProblem createTask(Person p) {
        var curable = getSelfTreatableHealthProblems(p);
        if (curable.isEmpty()) {
            logger.warning(p, "Has no self-treatable health problem.");
            return null;
        }

        MedicalAid aid = null;
        // Choose available medical aid for treatment.
        if (p.isInSettlement()) {
            aid = determineMedicalAidAtSettlement(p.getAssociatedSettlement(), curable);
        }
        else if (p.isInVehicle() && (p.getVehicle() instanceof Rover r)) {
            aid = determineMedicalAidInRover(r, curable);
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
     * Determine a medical aid at a settlement to use for self-treating a health problem.
     * 
     * @param settlement Place to search
     * @param curable 
     * @return medical aid or null if none found.
     */
    public static MedicalAid determineMedicalAidAtSettlement(Settlement settlement, Set<HealthProblem> curable) {

        Set<MedicalAid> goodMedicalAids = new HashSet<>();

        // Check all medical care buildings.
        for(Building building : settlement.getBuildingManager().getBuildingSet(
                                            FunctionType.MEDICAL_CARE)) {

            // Check if building currently has a malfunction.
            boolean malfunction = building.getMalfunctionManager().hasMalfunction();

            // Check if enough beds for patient.
            MedicalCare medicalCare = building.getMedical();

            if (!malfunction && canTreat(medicalCare, curable)) {
                goodMedicalAids.add(medicalCare);
            }
        }

        // Randomly select an valid medical care building.
        return RandomUtil.getARandSet(goodMedicalAids);
    }

    /**
     * Can a Medical Aid cure a specific healh problems
     * @param medicalCare Care center available
     * @param curable Set of problem to cure
     * @return
     */
    private static boolean canTreat(MedicalAid medicalCare, Set<HealthProblem> curable) {
        // Check if enough beds for patient.
        if (medicalCare.getPatientNum() >= medicalCare.getSickBedNum()) {
            return false;
        }

        // Check if any of person's self-treatable health problems can be treated in building.
        return curable.stream()
                        .anyMatch(medicalCare::canTreatProblem);
    }

    /**
     * Determine a medical aid on a vehicle to use for self-treating a health problem.
     * 
     * @param v Rover to check
     * @param curable Set of problem being cured
     * @return medical aid or null if none found.
     */
    public static MedicalAid determineMedicalAidInRover(Rover v, Set<HealthProblem> curable) {

        MedicalAid result = null;

        if (v.hasSickBay()) {
            SickBay sickBay = v.getSickBay();
            if (canTreat(sickBay, curable)) {
                return sickBay;
            }
        }

        return result;
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

    /**
     * Gets a list of health problems the person can self-treat.
     * 
     * @param person Perosn with the problem
     * @return list of health problems (may be empty).
     */
    public static Set<HealthProblem> getSelfTreatableHealthProblems(Person person) {

        Set<HealthProblem> result = new HashSet<>();
        int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);

        for(HealthProblem problem  : person.getPhysicalCondition().getProblems()) {
            if (problem.isDegrading()) {
                Treatment treatment = problem.getComplaint().getRecoveryTreatment();
                if (treatment != null) {
                    boolean selfTreatable = treatment.getSelfAdminister();
                    int requiredSkill = treatment.getSkill();
                    if (selfTreatable && (skill >= requiredSkill)) {
                        result.add(problem);
                    }
                }
            }
        }

        return result;
    }
}
