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

class MissionDaylightStepTest extends MarsSimUnitTest {

    private MissionVehicleProject createDaylightProject() {
        var s = buildSettlement("Test 1");
        var r = MissionTestHelper.buildRoster(getContext(), s, 2, EXPLORER_ROVER, false);
		var project = new MissionVehicleProject(null, MissionType.VISIT_LANDMARK, 10, r);

        project.addStep(new MissionDaylightStep(project));
        project.addStep(new TestMissionStep(project, 10));

        return project;
    }

    @Test
    void testStartInDarkness() {
        var project = createDaylightProject();

        var leader = project.getStartingPerson();

        var clock = getSim().getMasterClock();
        clock.setMarsTime(clock.getMarsTime().advanceToNextMSol(5));

        // Leader start executing the mission step
        assertFalse(project.execute(leader), "Execution should not complete immediately");

        var step = project.getCurrentStep();
        assertEquals(MissionDaylightStep.class, step.getClass(), "Daylight step active");
        MissionDaylightStep daylightStep = (MissionDaylightStep) step;
        assertFalse(daylightStep.isTimeValid(), "Time is not valid");

        var fm = project.getAssociatedSettlement().getFutureManager();
        var daylightEvent = fm.getEvents().stream().filter(e -> e.getHandler() instanceof MissionDaylightStep).findFirst().orElse(null);
        assertNotNull(daylightEvent, "Event queued for daylight step");

        var expected = daylightStep.getValidRange();
        assertTrue(expected.isWithinRange(daylightEvent.getWhen().getMillisolInt()), "Event corresponds to the daylight step");

        var pulse = createPulse(daylightEvent.getWhen(), false, false, false);
        fm.timePassing(pulse);
        assertTrue(daylightStep.isTimeValid(), "Time is not valid");

        project.execute(leader);
        assertNotEquals(daylightStep, project.getCurrentStep(), "Next step active");
    }

    @Test
    void testStartInDaylight() {
        var project = createDaylightProject();

        var leader = project.getStartingPerson();

        var clock = getSim().getMasterClock();
        clock.setMarsTime(clock.getMarsTime().advanceToNextMSol(500));

        var step = project.getSteps().get(0);
        assertEquals(MissionDaylightStep.class, step.getClass(), "Daylight step active");
        MissionDaylightStep daylightStep = (MissionDaylightStep) step;

        project.execute(leader);
        assertTrue(daylightStep.isTimeValid(), "Time is valid");

        // No event should be queued for the current daylight step since it's already valid
        var fm = project.getAssociatedSettlement().getFutureManager();
        var daylightEvent = fm.getEvents().stream().filter(e -> e.getHandler() instanceof MissionDaylightStep).findFirst().orElse(null);
        assertNull(daylightEvent, "No event should be queued for daylight step");

        // Execute the mission step directly since the time is already valid
        assertNotEquals(daylightStep, project.getCurrentStep(), "Next step active");
    }

    @Test
    void testMissionDaylight() {
        var s = buildSettlement("Test 1");
        var r = MissionTestHelper.buildRoster(getContext(), s, 2, EXPLORER_ROVER, false);
		var project = new MissionVehicleProject(null, MissionType.VISIT_LANDMARK, 10, r);

        var testStep = new TestMissionStep(project, 10);
        List<MissionStep> steps = List.of(testStep);

        var param = new CreateParameters(false, true);
        project.setMissionSteps(steps, param);

        var finalSteps = project.getSteps();

        // Load, Daylight, Board, Test, Disembark, Closedown
        assertEquals(6, finalSteps.size(), "There should be size final steps");
        assertEquals(MissionLoadVehicleStep.class, finalSteps.get(0).getClass(), "First step should be load vehicle step");
        assertEquals(MissionDaylightStep.class, finalSteps.get(1).getClass(), "Second step should be daylight step");
        assertEquals(MissionBoardVehicleStep.class, finalSteps.get(2).getClass(), "Third step should be board vehicle step");
    }
}
