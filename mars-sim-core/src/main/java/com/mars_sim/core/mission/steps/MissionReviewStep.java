/**
 * Mars Simulation Project
 * MissionReviewStep.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package com.mars_sim.core.mission.steps;

import com.mars_sim.core.mission.MissionProject;
import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.PlanType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;

/**
 * MissionReviewStep represents a step in the mission where the mission plan is reviewed.
 * It uses the MissionPlanning class to evaluate the mission based on the minimum passing score.
 */
public class MissionReviewStep extends MissionStep {

    private final MissionPlanning plan;

    public MissionReviewStep(MissionProject mission) {
        super(mission, Stage.PREPARATION, "Review Mission");

		var minScore = mission.getAssociatedSettlement().getMissionControl().getMinimumPassingScore();
		plan = new MissionPlanning(mission, 1, minScore);
        plan.setStatus(PlanType.PENDING);  // Old logic cannot start plan in PENDING state
    }

    @Override
    protected boolean execute(Worker worker) {
        switch (plan.getStatus()) {
            case APPROVED -> complete();  // Approved so complete the review step
            case NOT_APPROVED -> getMission().abort("not approved"); // Not approved so cannot complete the review step
            default -> {
                // Plan is still pending, cannot complete the review step
            }
        }
        return false;
    }

    /**
     * The plan associated to this review.
     * @return the mission planning associated with this review step.
     */
    public MissionPlanning getPlan() {
        return plan;
    }
}
