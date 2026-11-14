package com.mars_sim.core.mission.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.mission.CreateParameters;
import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.mission.MissionTestHelper;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.PlanType;
import com.mars_sim.core.test.MarsSimUnitTest;

class MissionReviewStepTest extends MarsSimUnitTest {

    private MissionVehicleProject createReviewableProject() {
        var s = buildSettlement("Test 1");
        var r = MissionTestHelper.buildRoster(getContext(), s, 2, EXPLORER_ROVER, false);
		var project = new MissionVehicleProject(null, MissionType.VISIT_LANDMARK, 10, r);

        var testStep = new TestMissionStep(project, 10);
        List<MissionStep> steps = List.of(testStep);

        var param = new CreateParameters(true);
        project.setMissionSteps(steps, param);

        var plan = project.getPlan();
        assertNotNull(plan, "Plan should not be null");
        assertEquals(PlanType.PENDING, plan.getStatus(), "Plan should be pending");
        return project;
    }

    @Test
    void testApproved() {
        var project = createReviewableProject();

        var leader = project.getStartingPerson();

        // Leader start executing the mission step
        assertFalse(project.execute(leader), "Execution should not complete immediately");
        assertEquals(MissionReviewStep.class, project.getCurrentStep().getClass(), "Review active");

        // Mark plan as approved
        var plan = project.getPlan();
        plan.setStatus(PlanType.APPROVED);

        // Leader start executing the review and should complete it since the plan is approved
        project.execute(leader);
        assertNotEquals(MissionReviewStep.class, project.getCurrentStep().getClass(), "Next step active");
    }

    @Test
    void testNoReview() {
        var s = buildSettlement("Test 1");
        var r = MissionTestHelper.buildRoster(getContext(), s, 2, EXPLORER_ROVER, false);
		var project = new MissionVehicleProject(null, MissionType.VISIT_LANDMARK, 10, r);

        List<MissionStep> steps = List.of(new TestMissionStep(project, 10));

        var param = new CreateParameters(false);
        project.setMissionSteps(steps, param);

        var plan = project.getPlan();
        assertNull(plan, "Plan should not be null");

        var firstStep = project.getSteps().get(0);
        assertNotEquals(MissionReviewStep.class, firstStep.getClass(), "First step should not be review");
    }

    @Test
    void testNotApproved() {
        var project = createReviewableProject();

        var leader = project.getStartingPerson();

        // Leader start executing the mission step
        assertFalse(project.execute(leader), "Execution should not complete immediately");
        assertEquals(MissionReviewStep.class, project.getCurrentStep().getClass(), "Review active");

        // Mark plan as not approved
        var plan = project.getPlan();
        plan.setStatus(PlanType.NOT_APPROVED);

        // Leader start executing the review and should complete it since the plan is approved
        project.execute(leader);
        assertTrue(project.isDone(), "Project should be done");
    }
}
