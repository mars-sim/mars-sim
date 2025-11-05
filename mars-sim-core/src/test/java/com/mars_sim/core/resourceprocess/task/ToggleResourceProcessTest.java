package com.mars_sim.core.resourceprocess.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;

public class ToggleResourceProcessTest extends MarsSimUnitTest {


    @Test
    public void testStartToggleOn() {
        var s = buildSettlement("Resource", true);
        var b = ToggleResourceProcessMetaTest.buildProcessing(this, s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var r = b.getResourceProcessing();

        // Pick a process and add resources
        var p = r.getProcesses().get(0);

        // Reset toggle for now
        ToggleResourceProcessMetaTest.moveToToggle(this, p);

        var w = buildPerson("worker", s);
        var t = new ToggleResourceProcess(w, false, p.getSpec());

        assertFalse(t.isDone(), "Started task");
        assertTrue(p.isWorkerAssigned(), "Worker assigned");
        assertEquals(p, t.getResourceProcess(), "Select process");
        assertEquals(b, t.getBuilding(), "Selected Building");
        assertFalse(p.isProcessRunning(), "Process not running");

        var completed = p.addToggleWorkTime(p.getRemainingToggleWorkTime() + 1);
        assertTrue(completed, "Toggle completed");
        assertTrue(p.isProcessRunning(), "Process running");

        t.endTask();
        assertFalse(p.isWorkerAssigned(), "Worker relased");
    }

    @Test
    public void testStartToggleOnDuplicate() {
        var s = buildSettlement("Resource", true);
        var b = ToggleResourceProcessMetaTest.buildProcessing(this, s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var r = b.getResourceProcessing();

        // Pick a process and add resources
        var p = r.getProcesses().get(0);

        // Reset toggle for now
        ToggleResourceProcessMetaTest.moveToToggle(this, p);

        var w = buildPerson("worker", s);
        var t = new ToggleResourceProcess(w, false, p.getSpec());
        assertFalse(t.isDone(), "Started task");

        var w1 = buildPerson("worker 2", s);
        var t1 = new ToggleResourceProcess(w1, false, p.getSpec());
        assertTrue(t1.isDone(), "Failed to start");
    }

    @Test
    public void testStartNoToggle() {
        var s = buildSettlement("Resource", true);
        var b = ToggleResourceProcessMetaTest.buildProcessing(this, s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var r = b.getResourceProcessing();

        // Pick a process and add resources
        var p = r.getProcesses().get(0);

        var w = buildPerson("worker", s);
        var t = new ToggleResourceProcess(w, false, p.getSpec());

        assertTrue(t.isDone(), "Task not started");
    }
}
