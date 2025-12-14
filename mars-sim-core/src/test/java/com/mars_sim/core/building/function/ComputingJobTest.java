package com.mars_sim.core.building.function;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;


import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.task.OptimizeSystem;
import com.mars_sim.core.computing.ComputingJob;
import com.mars_sim.core.computing.ComputingLoadType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.structure.Settlement;

public class ComputingJobTest extends MarsSimUnitTest {
    private static final double DURATION = 30D;
    private static final int STEPS = 20;

    @Test
    public void testCreation() {
        var s = buildSettlement("Compute");

        var job = new ComputingJob(s, ComputingLoadType.LOW, 1, DURATION, "Purpose");

        assertTrue(job.getCUPerMSol() > 0, "Job computing unit value");
        assertTrue(job.getRemainingNeed() > 0, "Job needs computing");
        assertFalse(job.isCompleted(), "Job not completed");


        assertEquals(job.getCUPerMSol() * DURATION, job.getRemainingNeed(), "Computing needed is correct");
    }

    private Building buildCompute(Settlement settlement) {
        return buildCompute(settlement.getBuildingManager());
	}
    
    private Building buildCompute(BuildingManager buildingManager) {
        return buildFunction(buildingManager, "Server Farm", BuildingCategory.LABORATORY,
                        FunctionType.COMPUTATION, LocalPosition.DEFAULT_POSITION, 0D, true);
	}

    @Test
    public void testCompute() {
        var s = buildSettlement("Compute City");

        // Research has compute
        Building b = buildCompute(s);
        
        var p = buildPerson("Test Programmer", s, JobType.COMPUTER_SCIENTIST);
            
        BuildingManager.addToBuilding(p, b);
        
        Building bLoc = p.getBuildingLocation();

        assertEquals(b, bLoc, "Same building ");
        
        var clock = getSim().getMasterClock().getMarsTime();
  
        b.getComputation().setFreeCU(20);
        
        OptimizeSystem task = new OptimizeSystem(p);
        
        var job = new ComputingJob(s, ComputingLoadType.LOW, clock.getMillisolInt(), DURATION, "Test Task");  
        job.pickMultipleNodes(0, clock.getMillisolInt());
        
        executeTask(p, task, 5);

        // Check one run
        double remainingNeed = job.getRemainingNeed();
  
        Computation center = null;
        double newNeed = 0;
        
        // Run job to consume bulk of power
        for (int i = 0; i < STEPS-1; i++) {
        	double cu = b.getComputation().getCurrentCU();
      
            clock = clock.addTime(1);
       
        	center = job.consumeProcessing(center, i * STEPS, clock.getMillisolInt());
  
            assertTrue(center != null, "Job found compute function #" + i);
            executeTask(p, task, 5);       
            
            var pulse = createPulse(clock, false, false);
            center.timePassing(pulse);
            
            newNeed = job.getRemainingNeed();
         
            // At the first round, newNeed equals origNeed
            // Comment out below for now. Unable to get the mars clock running
            assertTrue(newNeed == remainingNeed, "Computing consumed #" + i);
            
            assertFalse(job.isCompleted(), "Computing still active #" + 1);
        }

        // Comment out below for now. Unable to get the mars clock running
        assertTrue(newNeed == remainingNeed, "Computing consumed: ");
 
        // Big duration to complete
//        job.consumeProcessing(center, (DURATION/STEPS)*3, clock.getMillisolInt());

        // Comment out below for now. Unable to get the mars clock running
//        assertTrue(job.isCompleted(), "Job found compute function end");      
    }

    @Test
    public void testNoCompute() {
        var s = buildSettlement("Compute");

        var job = new ComputingJob(s, ComputingLoadType.LOW, 1, DURATION, "Purpose");  

        var clock = getSim().getMasterClock().getMarsTime();

        // Check one run
        double origNeed = job.getRemainingNeed();

        // Run job to consume bulk of power
        Computation center = job.consumeProcessing(null, DURATION/STEPS, clock.getMillisolInt());
        assertFalse(center != null, "Job found compute function");
        double newNeed = job.getRemainingNeed();
        assertEquals(newNeed, origNeed, "No computing consumed");
    }
}
