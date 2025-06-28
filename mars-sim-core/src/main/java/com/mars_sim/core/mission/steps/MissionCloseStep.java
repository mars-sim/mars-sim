/**
 * Mars Simulation Project
 * MissionCloseStep.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package com.mars_sim.core.mission.steps;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mission.MissionProject;
import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;

/**
 * This is a step in a Mission that closes it down.
 */
public class MissionCloseStep extends MissionStep {

    private static final long serialVersionUID = 1L;
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
