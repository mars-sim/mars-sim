/**
 * Mars Simulation Project
 * MissionCloseStep.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.project.Stage;

/**
 * This is a step in a Mission that closes it down.
 */
class MissionCloseStep extends MissionStep {

    private static final SimLogger logger = SimLogger.getLogger(MissionCloseStep.class.getName());

    public MissionCloseStep(MissionProject project) {
        super(project, Stage.CLOSEDOWN, "Closedown");
    }

    /**
     * Execute the close down that involves experiecne update and leaving the Mission.
     * LEader closes the Mission down.
     */
    @Override
    protected boolean execute(Worker worker) {
        MissionProject m = getMission();
        if (getLeader().equals(worker)) {
            // Leader is last to leave and closes the mission
            if (m.getMembers().size() == 1) {
                logger.info(worker, "Leader closing mission");
                worker.setMission(null);

                complete(); // Will call the cleardown method to release anything

                // Should the leader write a report ?
            }
        }
        else {
            worker.setMission(null);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Mission " + getMission().getName() + " close";
    }
}
