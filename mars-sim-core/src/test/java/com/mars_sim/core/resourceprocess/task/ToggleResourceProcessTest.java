package com.mars_sim.core.resourceprocess.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.MarsSimContext;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.test.MarsSimUnitTest;

public class ToggleResourceProcessTest extends MarsSimUnitTest {

    static Building buildProcessing(MarsSimContext context, BuildingManager buildingManager, LocalPosition pos, double facing) {
        // Note: "ERV-I" only has 2 processes so simpler
		return context.buildFunction(buildingManager, "Lander Hab", BuildingCategory.PROCESSING,
							FunctionType.RESOURCE_PROCESSING,  pos, facing, true);
	}
    
    @Test
    public void testStartToggleOn() {
        var s = buildSettlement("Resource Settlement", true);
        var b = buildProcessing(getContext(), s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var r = b.getResourceProcessing();

        // Pick the first process
        var p = r.getProcesses().get(1);

        // Reset toggle for now
        ToggleResourceProcessMetaTest.moveToToggle(getContext(), p);

        var w = buildPerson("process worker 0", s);
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
        var s = buildSettlement("Resource Settlement", true);
        var b = buildProcessing(getContext(), s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var r = b.getResourceProcessing();

        // Pick the second process, namely, "Sabatier RWGS Reactor"
        var p = r.getProcesses().get(1);
        
        assertTrue(22 == r.getProcesses().size(), "# of Resource process");
        assertTrue(p.getProcessName().equals("Sabatier RWGS Reactor"), "Name of the Resource proess");
        
        // Reset toggle for now
        ToggleResourceProcessMetaTest.moveToToggle(getContext(), p);

        var w = buildPerson("process worker 1", s);
        var t = new ToggleResourceProcess(w, false, p.getSpec());
        
        assertTrue(t.getPhase().getName().equals("Toggling Resource Process"), "The Phase Name");
        
        assertFalse(t.isDone(), "Started task");

        var w1 = buildPerson("process worker 2", s);
        var t1 = new ToggleResourceProcess(w1, false, p.getSpec());
        
        assertTrue(t1.isDone(), "Failed to start");
    }

    @Test
    public void testStartNoToggle() {
        var s = buildSettlement("Resource Settlement", true);
        var b = ToggleResourceProcessMetaTest.buildProcessing(getContext(), s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var r = b.getResourceProcessing();

        // Pick a process and add resources
        var p = r.getProcesses().get(0);

        var w = buildPerson("process worker 3", s);
        var t = new ToggleResourceProcess(w, false, p.getSpec());

        assertTrue(t.isDone(), "Task not started");
    }
}
