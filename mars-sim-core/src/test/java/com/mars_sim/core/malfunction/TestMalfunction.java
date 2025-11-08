package com.mars_sim.core.malfunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.malfunction.MalfunctionMeta.EffortSpec;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;

public class TestMalfunction extends MarsSimUnitTest {


	private static final String AIR_LEAK ="Air Leak";
	
	private int counter = 0;
	private MalfunctionMeta insideMeta;

	private MalfunctionManager mgr;

	
        @BeforeEach
    public void setUpMalfunction() {
		

        MalfunctionConfig config = getConfig().getMalfunctionConfiguration();
        for (MalfunctionMeta m : config.getMalfunctionList()) {
        	if (m.getName().equals(AIR_LEAK)) {
        		insideMeta = m;
        	}
        }

		Settlement s = buildSettlement("mock");

		// Allows rover to go directly into Garage
		buildGarage(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);

		Rover r = buildRover(s, "Test", null, EXPLORER_ROVER);
		mgr = r.getMalfunctionManager();
		mgr.initScopes();
    }
    
    private Malfunction createInsideMalfunction() {
		return new Malfunction(mgr, counter++, insideMeta, true);
	}
    
    @Test
    public void testComplete() {
    	Malfunction mal = createInsideMalfunction();
    	assertFalse(mal.isFixed(), "Malfunction not completed");
    	double expected = mal.getWorkTime(MalfunctionRepairWork.INSIDE);
    	
    	// Add partial work
    	mal.addWorkTime(MalfunctionRepairWork.INSIDE, expected/2D, "Worker1");
    	assertFalse(mal.isFixed(), "Malfunction not partially completed");
    	assertFalse(mal.isWorkDone(MalfunctionRepairWork.INSIDE), "Malfunction inside work not completed");

    	mal.addWorkTime(MalfunctionRepairWork.INSIDE, (expected/2D), "Worker1");
    	assertTrue(mal.isFixed(), "Malfunction completed");
    	assertTrue(mal.isWorkDone(MalfunctionRepairWork.INSIDE), "Malfunction inside work completed");
    }
    
    
    @Test
    public void testSlots() {
    	Malfunction mal = createInsideMalfunction();
    	EffortSpec e = insideMeta.getRepairEffort().get(MalfunctionRepairWork.INSIDE);
    	int desiredWorkers = e.getDesiredWorkers();
    	assertEquals(desiredWorkers, mal.numRepairerSlotsEmpty(MalfunctionRepairWork.INSIDE), "Malfunction initial slots");

    	// Add worker
    	for (int i = 0; i < e.getDesiredWorkers(); i++) {
        	mal.addWorkTime(MalfunctionRepairWork.INSIDE, 0.001D, "Worker" + i);
        	int expectedSlots = desiredWorkers - (i + 1);
        	assertEquals(expectedSlots, mal.numRepairerSlotsEmpty(MalfunctionRepairWork.INSIDE), "Available slots after worker #" + i);
        	
        	// Remove others
            mal.leaveWork(MalfunctionRepairWork.INSIDE, "Worker" + i);
            assertEquals(expectedSlots + 1,
            			mal.numRepairerSlotsEmpty(MalfunctionRepairWork.INSIDE),
            			"Available slots after removing worker #" + i);	

        	// Add worker back in
        	mal.addWorkTime(MalfunctionRepairWork.INSIDE, 0.001D, "Worker" + i);
        	assertEquals(expectedSlots, mal.numRepairerSlotsEmpty(MalfunctionRepairWork.INSIDE), "Available slots after re-adding worker #" + i);
    	}
    }
}