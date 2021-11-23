package org.mars_sim.msp.core.malfunction;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.malfunction.MalfunctionMeta.EffortSpec;

import junit.framework.TestCase;

public class TestMalfunction extends TestCase {


	private static final String INSIDE_MALFUNCTION = "Class A Fire";
	
	private int counter = 0;
	private MalfunctionMeta insideMeta;
	
    @Override
    public void setUp() throws Exception {
        SimulationConfig.instance().loadConfig();
        MalfunctionConfig config = SimulationConfig.instance().getMalfunctionConfiguration();
        for (MalfunctionMeta m : config.getMalfunctionList()) {
        	if (m.getName().equals(INSIDE_MALFUNCTION)) {
        		insideMeta = m;
        	}
        }
    }
    
    private Malfunction createInsideMalfunction() {
		return new Malfunction(counter++, insideMeta, true);
	}
    
    public void testComplete() {
    	Malfunction mal = createInsideMalfunction();
    	assertFalse("Malfunction not completed", mal.isFixed());
    	double expected = mal.getWorkTime(MalfunctionRepairWork.INSIDE);
    	
    	// Add partial work
    	mal.addWorkTime(MalfunctionRepairWork.INSIDE, expected/2D, "Worker1");
    	assertFalse("Malfunction not partially completed", mal.isFixed());
    	assertFalse("Malfunction inside work not completed", mal.isWorkDone(MalfunctionRepairWork.INSIDE));

    	mal.addWorkTime(MalfunctionRepairWork.INSIDE, (expected/2D), "Worker1");
    	assertTrue("Malfunction completed", mal.isFixed());
    	assertTrue("Malfunction inside work completed", mal.isWorkDone(MalfunctionRepairWork.INSIDE));
    }
    
    
    public void testSlots() {
    	Malfunction mal = createInsideMalfunction();
    	EffortSpec e = insideMeta.getRepairEffort().get(MalfunctionRepairWork.INSIDE);
    	int desiredWorkers = e.getDesiredWorkers();
    	assertEquals("Malfunction initial slots", mal.numRepairerSlotsEmpty(MalfunctionRepairWork.INSIDE), desiredWorkers);

    	// Add worker
    	for(int i = 0; i < e.getDesiredWorkers(); i++) {
        	mal.addWorkTime(MalfunctionRepairWork.INSIDE, 0.001D, "Worker" + i);
        	int expectedSlots = desiredWorkers - (i + 1);
        	assertEquals("Available slots after worker #" + i, expectedSlots, mal.numRepairerSlotsEmpty(MalfunctionRepairWork.INSIDE));
        	
        	// Remove others
            mal.leaveWork(MalfunctionRepairWork.INSIDE, "Worker" + i);
            assertEquals("Available slots after removing worker #" + i,
            			expectedSlots + 1,
            			mal.numRepairerSlotsEmpty(MalfunctionRepairWork.INSIDE));	

        	// Add worker back in
        	mal.addWorkTime(MalfunctionRepairWork.INSIDE, 0.001D, "Worker" + i);
        	assertEquals("Available slots after re-adding worker #" + i, expectedSlots, mal.numRepairerSlotsEmpty(MalfunctionRepairWork.INSIDE));
    	}
    }
}