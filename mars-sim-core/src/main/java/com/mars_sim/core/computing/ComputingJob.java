/*
 * Mars Simulation Project
 * ComputingJob.java
 * @date 2024-05-04
 * @author Barry Evans
 */
package com.mars_sim.core.computing;

import java.io.Serializable;
import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.function.Computation;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.tools.util.RandomUtil;

/**
 * This class represents a job that uses computing resources to deliver an outcome.
 */
public class ComputingJob implements Serializable {
	
    private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(ComputingJob.class.getName());

    private Settlement host;
    private double initDemand;
    private double computingNeeded;
    private double cuPerMSol;
    private double duration;
    private String purpose;

    private boolean scheduled = false;

    /**
     * Constructor. Creates a computing job to run at a Settlement for a maximum duration.
     * 
     * @param host Where the computing job will run
     * @param duration Duration the computing job runs
     * @param purpose The purpose of the job
     */
    public ComputingJob(Settlement host, double duration, String purpose) {
        this.host = host;
        this.duration = duration;
        this.cuPerMSol = RandomUtil.getRandomDouble(.05, 0.15);
        this.initDemand = duration * cuPerMSol;
        this.computingNeeded = initDemand;
        this.purpose = purpose;
        		
		logger.log(host, Level.INFO, 10_000, "Requested computing resources: " 
		 		+ Math.round(computingNeeded * 100.0)/100.0 + " CUs for "
		 		+ purpose + ".");
    }

    /**
	 * Accesses the computing nodes. THis will reduce the computing needed by a factor
     * related to the time.
	 * 
	 * @param time The time for the processing node
	 * @param now Current time on mars
     * @return Computing process is running
	 */
	public boolean consumeProcessing(double time, MarsTime now) {
        // Submit request for computing resources; only once per job on first time through
        if (!scheduled) {
            int startMSol = now.getMillisolInt() + 1;
            int endMSol = (int) (startMSol + duration);
    	    Computation center = host.getBuildingManager()
    			    .getMostFreeComputingNode(initDemand, startMSol, endMSol);
            scheduled = (center != null) && center.scheduleTask(initDemand, startMSol, endMSol);
            if (!scheduled) {
    		    logger.info(host, 30_000L, "No computing resources available for " 
    		    	+ purpose + ".");
            }
        }

        // If scheduled then reduce computing
        if (scheduled) {
            var newNeed = computingNeeded - time * cuPerMSol * RandomUtil.getRandomDouble(.9, 1.1);
            computingNeeded = Math.max(0, newNeed);
        }

        return scheduled;
	}

    /**
     * Gets how much computing is needed.
     * 
     * @return
     */
    public double getNeeded() {
        return computingNeeded;
    }

    /**
     * Has all the computing demand been completed.
     * 
     * @return
     */
    public boolean isCompleted() {
        return computingNeeded <= 0;
    }

    /**
     * Gets how many CU consumed per mSol by this job.
     * 
     * @return
     */
    public double getCUPerMSol() {
        return cuPerMSol;
    }

    /**
     * Gets how much has been consumed so far.
     * 
     * @return
     */
    public double getConsumed() {
        return initDemand - computingNeeded;
    }
}
