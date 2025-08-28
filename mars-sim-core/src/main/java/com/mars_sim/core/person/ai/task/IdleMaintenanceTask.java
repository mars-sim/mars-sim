package com.mars_sim.core.person.ai.task;

import java.util.Random;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsClock;

/**
 * IdleMaintenanceTask represents a low-priority maintenance/inspection task 
 * a person performs when idle. It consumes a small amount of time and 
 * slightly improves the condition of random equipment in the settlement.
 */
public class IdleMaintenanceTask extends Task {

    private static final int DURATION_MILLISOLS = 30; // task lasts 30 millisols (example duration)

    private Person person;
    private Settlement settlement;
    private boolean completed = false;

    public IdleMaintenanceTask(Person person) {
        super(person);
        this.person = person;
        this.settlement = person.getAssociatedSettlement();
        setName("Idle Maintenance");
        setDescription("Performing routine maintenance on equipment");
        // If needed, assign a low priority or mark as background task
    }

    @Override
    public void perform() {
        // Simulate work being done; after duration, mark complete
        MarsClock clock = person.getSimulation().getMasterClock().getMarsClock();
        // (Pseudo-code: wait or tick for DURATION_MILLISOLS)
        // For simplicity in this implementation, we immediately mark as completed.
        completed = true;
        // Improve a random equipment's reliability slightly
        if (settlement != null) {
            MalfunctionManager mm = settlement.getMalfunctionManager();
            if (mm != null) {
                mm.improveRandomEquipmentReliability(0.01); 
                // improves reliability by 1% as a result of maintenance
            }
        }
    }

    @Override
    public boolean isFinished() {
        return completed;
    }
}
