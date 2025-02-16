package com.mars_sim.core.structure.building.function.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.resourceprocess.ResourceProcess;
import com.mars_sim.core.resourceprocess.task.ToggleResourceProcessMeta;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;

public class ToggleResourceProcessMetaTest extends AbstractMarsSimUnitTest {

    private Building buildProcessing(BuildingManager buildingManager, LocalPosition pos, double facing) {
        // ERV-1 only has 2 processes so simpler
		return buildFunction(buildingManager, "ERV-I", BuildingCategory.PROCESSING,
							FunctionType.RESOURCE_PROCESSING,  pos, facing, true);
	}


    public void testGetResourceProcessingTasks() {
        var s = buildSettlement("Resource", true);
        var b = buildProcessing(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var r = b.getResourceProcessing();
    
    
        var mt = new ToggleResourceProcessMeta();

        // No tasks as no resoruces
        var results = mt.getSettlementTasks(s);
        assertTrue("No tasks without resources", results.isEmpty());

        // Pick a prcoess and add resoruces
        var p = r.getProcesses().get(0);
        for(var i : p.getInputResources()) {
            s.storeAmountResource(i, 100D);
        }

        for(var o : p.getOutputResources()) {
            s.getGoodsManager().setSupplyValue(o, 100);  // Force a value output value
        }

        results = mt.getSettlementTasks(s);
        assertTrue("No tasks without toggle", results.isEmpty());

        // Reset toggle for now
        moveToToggle(p);
        results = mt.getSettlementTasks(s);
        assertEquals("Single available task", 1, results.size());

        // Start the process
        p.addToggleWorkTime(p.getRemainingToggleWorkTime() + 1);        
        results = mt.getSettlementTasks(s);
        assertTrue("No tasks as not running long enough", results.isEmpty());

        // Run the process to the next toggle phase
        moveToToggle(p);
        results = mt.getSettlementTasks(s);
        assertEquals("Single runing task", 1, results.size());
    }

    private void moveToToggle(ResourceProcess p) {
        var clock = getSim().getMasterClock();
        var newTime = p.getToggleDue().addTime(1);
        clock.setMarsTime(newTime);
        p.execute(newTime);
    }
}
