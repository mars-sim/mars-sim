package com.mars_sim.core.mission.predefined;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.mission.MetaMission;
import com.mars_sim.core.mission.MetaMission.Roster;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.mission.objectives.LandmarkObjective;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.test.MarsSimUnitTest;

class VisitLandmarkStepTest extends MarsSimUnitTest{
    @Test
    void testStart() throws CoordinatesException {
        var s = buildSettlement("Test");
        var r = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);
        
        var l = buildPerson("Leader", s);
        l.transfer(r);
        var w = buildPerson("Worker", s);
        w.transfer(r);

        var landmarks = getConfig().getLandmarkConfiguration().getLandmarks()
                    .getFeatures(CoordinatesFormat.fromString("0N 0E"), 1);
        assertFalse(landmarks.isEmpty(), "No landmarks found in test config");
        var landmark = landmarks.get(0);

        Roster roster = new MetaMission.Roster(l, List.of(w), r);
        
		var project = new MissionVehicleProject(null, MissionType.VISIT_LANDMARK, 10, roster);
        var st = new VisitLandmarkStep(project, landmark);
        project.addStep(st);
        project.execute(w);
        
        var obj = st.getObjective();
        assertTrue(obj instanceof LandmarkObjective, "Objective should not be complete at start");
        var landObj = (LandmarkObjective)obj;

        var times = landObj.getEVATimes();
        assertEquals(0D, times.get(l.getName()), "Leader no EVA");
        assertEquals(0D, times.get(w.getName()), "Worker no EVA");

        var task = w.getTaskManager().getTask();
        assertTrue(task instanceof LandmarkEVA, "Worker should be doing the visit task");   
        
        // Worker does EVA visit
        executeTask(w, task, 100);

        // Simulate time on site
        var clock = getSim().getMasterClock();
        clock.setMarsTime(clock.getMarsTime().addTime(landObj.getMSolViewing() + 1));
        executeTask(w, task, 1000);

        task.endTask();
        assertTrue(task.isDone(), "Worker should have completed EVA");

        assertTrue(times.get(w.getName()) > 0D, "Worker should have completed EVA");
        assertEquals(0D, times.get(l.getName()), "Leader still no EVA");

        // Simulate end of visit time
        clock.setMarsTime(clock.getMarsTime().addTime(landObj.getMSolAtSite() + 1));

        // Leader cannot execute because out of time
        assertFalse(project.execute(l), "Leader should have nothing to do after EVA complete");
        assertFalse(st.isCompleted(), "Step need everyone back on board");
        assertTrue(((EVAOperation)task).isRequestEndEVATrue(), "Worker recalled to rover after EVA");

        w.transfer(r);
        project.execute(l);

        assertTrue(st.isCompleted(), "Step should be complete after EVA");
    }
}
