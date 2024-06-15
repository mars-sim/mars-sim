/*
 * Mars Simulation Project
 * TreatMedicalPatient.java
 * @date 2023-11-16
 * @author Scott Davis
 */
package com.mars_sim.core.person.health.task;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalAid;
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
 * A task for performing a medical treatment on a patient at a medical station.
 * This task is not for performing a medical treatment on one's self.
 */
public class TreatMedicalPatient extends TreatHealthProblem {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(TreatMedicalPatient.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.treatMedicalPatient"); //$NON-NLS-1$

    /**
     * Create a task for a Doctor to provide Treatment to someone suffering from a Health Problem.
     * @param doctor
     * @return
     */
    static TreatMedicalPatient createTask(Person doctor) {
        Map<HealthProblem,MedicalAid> problems;
        if (doctor.isInSettlement()) {
            problems = findSettlementHealthProblems(doctor.getSettlement());
        }
        else if (doctor.isInVehicle()) {
            problems = findVehicleHealthProblems(doctor.getVehicle());
        }
        else {
            logger.warning(doctor, "Cannot determine the Medical station");
            return null;
        }

        // Filter problem to this that this doctor can handle
        var treatable = MedicalHelper.getTreatableHealthProblems(doctor, problems.keySet(), false);

        var healthProblem = RandomUtil.getARandSet(treatable);
        if (healthProblem == null) {
            logger.warning(doctor, "Cannot find a sutiable patient to treat");
            return null;
        }

        return new TreatMedicalPatient(doctor, problems.get(healthProblem), healthProblem);
    }

    /**
     * Find any problem that needs to be treated by a doctor
     * @param v Vehicle to check
     * @return
     */
    static Map<HealthProblem, MedicalAid> findVehicleHealthProblems(Vehicle v) {
        if (v instanceof Rover r) {
            SickBay sb = r.getSickBay();
            Map<HealthProblem,MedicalAid> results = new HashMap<>();

            getProblemsNeedingTreatment(sb).forEach(h -> results.put(h, sb));
            return results;

        }
        return Collections.emptyMap();
    }

    /**
     * Find any problem that needs to be treated by a doctor
     * @param settlement Settlement to check
     * @return
     */
    static Map<HealthProblem,MedicalAid>  findSettlementHealthProblems(Settlement settlement) {
        Map<HealthProblem,MedicalAid> results = new HashMap<>();
        // Check all medical care buildings.
        for(Building building : settlement.getBuildingManager().getBuildingSet(
                        FunctionType.MEDICAL_CARE)) {

            // Check if building currently has a malfunction.
            boolean malfunction = building.getMalfunctionManager().hasMalfunction();

            // Check if enough beds for patient.
            MedicalCare medicalCare = building.getMedical();

            if (!malfunction) {
                getProblemsNeedingTreatment(medicalCare).forEach(h -> results.put(h, medicalCare));
            }
        }

        return results;
    }

    /**
     * Get all the Healthproblem that are waiting treatment by a doctor
     * @param aid Statino to scan
     */
    private static List<HealthProblem> getProblemsNeedingTreatment(MedicalAid aid) {
        return aid.getProblemsAwaitingTreatment().stream()
                    .filter(hp -> !hp.getComplaint().getRecoveryTreatment().getSelfAdminister())
                    .toList();
    }

    /**
     * Constructor.
     * @param healer the person to perform the task
     * @param station Place traetment is waiting
     * @param problem Problem being treated
     */
    private TreatMedicalPatient(Person healer, MedicalAid station, HealthProblem problem) {
        super(NAME, healer, station, problem);

        var aid = station;

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

}
