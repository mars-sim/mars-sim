
/**
 * Mars Simulation Project
 * MedicalHelper.java
 * @date 2024-06-09
 * @author Barry Evans
 */
package com.mars_sim.core.person.health.task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.HealthProblemState;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.core.person.health.Treatment;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.MedicalCare;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.SickBay;
import com.mars_sim.tools.util.RandomUtil;

/**
 * Helper class for identifying suitable HealtProblems
 */
public final class MedicalHelper {
    // Static helper class
    private MedicalHelper() {}

    /**
     * Determine a medical aid at a settlement to use for self-treating a health problem.
     * 
     * @param settlement Place to search
     * @param curable 
     * @return medical aid or null if none found.
     */
    static MedicalAid determineMedicalAidAtSettlement(Settlement settlement, Set<HealthProblem> curable) {
    
        Set<MedicalAid> goodMedicalAids = new HashSet<>();
    
        // Check all medical care buildings.
        for(Building building : settlement.getBuildingManager().getBuildingSet(
                                            FunctionType.MEDICAL_CARE)) {
    
            // Check if building currently has a malfunction.
            boolean malfunction = building.getMalfunctionManager().hasMalfunction();
    
            // Check if enough beds for patient.
            MedicalCare medicalCare = building.getMedical();
    
            if (!malfunction && MedicalHelper.canTreat(medicalCare, curable)) {
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
     * Gets a list of health problems the person can self-treat.
     * 
     * @param healer Perosn doing the healing
     * @param selfHeal Look only for Self heal problems
     * @return list of health problems (may be empty).
     */
    static Set<HealthProblem> getTreatableHealthProblems(Person healer, Collection<HealthProblem> problems,
                                                        boolean selfHeal) {
    
        Set<HealthProblem> result = new HashSet<>();
        int skill = healer.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);
    
        for(HealthProblem problem  : problems) {
            if (problem.getState() == HealthProblemState.DEGRADING) {
                Treatment treatment = problem.getComplaint().getRecoveryTreatment();
                if ((treatment != null) && (treatment.getSelfAdminister() == selfHeal)) {
                    int requiredSkill = treatment.getSkill();
                    if (skill >= requiredSkill) {
                        result.add(problem);
                    }
                }
            }
        }
    
        return result;
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

}
