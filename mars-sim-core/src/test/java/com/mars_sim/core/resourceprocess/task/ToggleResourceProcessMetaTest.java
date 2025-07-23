package com.mars_sim.core.resourceprocess.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.resourceprocess.ResourceProcess;
import com.mars_sim.core.science.task.MarsSimContext;

public class ToggleResourceProcessMetaTest extends AbstractMarsSimUnitTest {

    static Building buildProcessing(MarsSimContext context, BuildingManager buildingManager, LocalPosition pos, double facing) {
        // ERV-1 only has 2 processes so simpler
		return context.buildFunction(buildingManager, "ERV-I", BuildingCategory.PROCESSING,
							FunctionType.RESOURCE_PROCESSING,  pos, facing, true);
	}

    static void moveToToggle(MarsSimContext context, ResourceProcess p) {
        var clock = context.getSim().getMasterClock();
        var newTime = p.getToggleDue().addTime(1);
        clock.setMarsTime(newTime);
        p.execute(newTime);
    }

    public void testGetResourceProcessingTasks() {
        var s = buildSettlement("Resource", true);
        var b = buildProcessing(this, s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var r = b.getResourceProcessing();

        // Create a second processor to test multiple
        buildProcessing(this, s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
    
        var mt = new ToggleResourceProcessMeta();

        // No tasks as no resources
        var results = mt.getSettlementTasks(s);
        assertTrue("No tasks without resources", results.isEmpty());

        // Set zero cargo capacity
//        s.getEquipmentInventory().setCargoCapacity(0);
        
        // Pick a process and add resources
        var p = r.getProcesses().get(0);
        for (var i : p.getInputResources()) {
            s.storeAmountResource(i, 100D);
        }

        double stored = s.getStoredMass();
//        System.out.println("store: " + stored);
        assertEquals("Stored mass", 100.0, stored);
        
        for (var o : p.getOutputResources()) {
            s.getGoodsManager().setSupplyScore(o, 100);  // Force a value output value
        }

        results = mt.getSettlementTasks(s);
//        System.out.println(results);
        assertTrue("No tasks without toggle", results.isEmpty());

        // Reset toggle for now
        moveToToggle(this, p);
        results = mt.getSettlementTasks(s);
//        System.out.println(results);
        // Note: how does resetting the toggle affect the amount of resources ? 
//        assertEquals("Single available task", 1, results.size());

        // Start the process
        p.addToggleWorkTime(p.getRemainingToggleWorkTime() + 1);        
        results = mt.getSettlementTasks(s);
        assertTrue("No tasks as not running long enough", results.isEmpty());

        // Run the process to the next toggle phase
        moveToToggle(this, p);
        results = mt.getSettlementTasks(s);
        assertEquals("Single runing task", 1, results.size());
    }
}
