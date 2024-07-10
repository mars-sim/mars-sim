/*
 * Mars Simulation Project
 * MedicalAidTask.java
 * @date 2024-06-20
 * @author Barry Evans
 */
package com.mars_sim.core.person.health.task;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.MedicalCare;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.SickBay;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * A task that uses a medical station.
 */
public abstract class MedicalAidTask extends Task {

    private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(MedicalAidTask.class.getName());

    private MedicalAid medicalAid;

    protected MedicalAidTask(String name, Worker worker, MedicalAid hospital, ExperienceImpact impact,
                             double duration) {
        super(name, worker, true, impact, duration);
        
        medicalAid = hospital;
    }

    /**
     * Walk to the selected Medical aid and use it as a doctor or patient
     * @param needBed Use a bed in teh aid
     */
    protected void walkToMedicalAid(boolean needBed) {
        // Walk to medical aid.
        if (medicalAid instanceof MedicalCare medicalCare) {     
            // Walk to medical care building.
            walkToTaskSpecificActivitySpotInBuilding(medicalCare.getBuilding(), FunctionType.MEDICAL_CARE, false);
        }
        else if (medicalAid instanceof SickBay sb) {
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
     * Gets the malfunctionable associated with the medical aid.
     * @return the associated Malfunctionable
     */
    protected Malfunctionable getMalfunctionable() {
        Malfunctionable result = null;

        if (medicalAid instanceof SickBay bay) {
            result = bay.getVehicle();
        }
        else if (medicalAid instanceof MedicalCare care) {
            result = care.getBuilding();
        }
        else if (medicalAid instanceof Malfunctionable mal) {
            result = mal;
        }
        else {
            throw new IllegalArgumentException(medicalAid + " is not associated to a Malfunctionable");
        }
        return result;
    }

    /**
     * Where is this treatment taking place
     */
    public MedicalAid getMedicalAid() {
        return medicalAid;
    }
}
