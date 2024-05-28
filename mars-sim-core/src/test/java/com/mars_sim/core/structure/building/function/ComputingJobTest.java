package com.mars_sim.core.structure.building.function;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.computing.ComputingJob;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.mapdata.location.LocalPosition;

public class ComputingJobTest extends AbstractMarsSimUnitTest {
    private static final double DURATION = 150D;
    private static final int STEPS = 10;

    public void testCreation() {
        var s = buildSettlement("Compute");

        var job = new ComputingJob(s, DURATION, "Purpose");

        assertTrue("Job computing unit value", job.getCUPerMSol() > 0);
        assertTrue("Job needs computing", job.getNeeded() > 0);
        assertFalse("Job not completed", job.isCompleted());


        assertEquals("Computing needed is correct", job.getCUPerMSol() * DURATION, job.getNeeded());
    }

    private Building buildCompute(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
        return buildFunction(buildingManager, "Server Farm", BuildingCategory.LABORATORY,
                        FunctionType.COMPUTATION,  pos, facing, id);
	}

    public void testCompute() {
        var s = buildSettlement("Compute");

        // Research has compute
        buildCompute(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);

        var job = new ComputingJob(s, DURATION, "Purpose");  

        var clock = getSim().getMasterClock().getMarsTime();

        // Check one run
        double origNeed = job.getNeeded();

        // Run job to consume bulk of power
        for(int i = 0; i < STEPS-1; i++) {
            boolean consumed = job.consumeProcessing(DURATION/STEPS, clock);
            assertTrue("Job found compute function #" + i, consumed);
            double newNeed = job.getNeeded();
            assertGreaterThan("Computing consumed #" + i, newNeed, origNeed);
            assertFalse("Computign still active #" + 1, job.isCompleted());
            origNeed = newNeed;
        }

        // Big duratino to complete
        job.consumeProcessing((DURATION/STEPS)*3, clock);
        assertTrue("Job found compute function end", job.isCompleted());

    }

    public void testNoCompute() {
        var s = buildSettlement("Compute");

        var job = new ComputingJob(s, DURATION, "Purpose");  

        var clock = getSim().getMasterClock().getMarsTime();

        // Check one run
        double origNeed = job.getNeeded();

        // Run job to consume bulk of power
        boolean consumed = job.consumeProcessing(DURATION/STEPS, clock);
        assertFalse("Job found compute function", consumed);
        double newNeed = job.getNeeded();
        assertEquals("No computing consumed", newNeed, origNeed);
    }
}
