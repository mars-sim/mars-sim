package com.mars_sim.core.mission.steps;

import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;

/**
 * Dummy mission step for testing purposes. It is executed a specified number of times before completing.
 */
public class TestMissionStep extends MissionStep {
    private int expectedCalls;

    /**
     * Constructs a TestMissionStep with the specified project and maximum number of calls before completion.
     * @param project the mission vehicle project associated with this step
     * @param maxCalls the maximum number of times this step should be executed before completion
     */
    public TestMissionStep(MissionVehicleProject project, int maxCalls) {
        super(project, Stage.ACTIVE, "Test Step");
        this.expectedCalls = maxCalls;
    }

    @Override
    protected boolean execute(Worker worker) {
        expectedCalls--;
        if (expectedCalls > 0) {
            complete(); // Mark the step as complete if maxCalls is still greater than 0
        }
        return false;
    }

}
