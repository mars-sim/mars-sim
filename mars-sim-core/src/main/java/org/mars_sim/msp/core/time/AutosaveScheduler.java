/**
 * Mars Simulation Project
 * AutosaveScheduler.java
 * @version 3.1.0 2018-11-08
 * @author Manny Kung
 */

package org.mars_sim.msp.core.time;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Simulation.SaveType;
import org.mars_sim.msp.core.SimulationConfig;


// See https://stackoverflow.com/questions/14889143/how-to-stop-a-task-in-scheduledthreadpoolexecutor-once-i-think-its-completed
// regarding the issue of calling cancel()

public class AutosaveScheduler {
	
	private static Logger logger = Logger.getLogger(AutosaveScheduler.class.getName());
	
//    static ScheduledExecutorService autosaveService = Executors.newSingleThreadScheduledExecutor();
    static ScheduledThreadPoolExecutor autosaveService = new ScheduledThreadPoolExecutor(1);
    static ScheduledFuture<?> t;
    
    static long lastRemainingSeconds;
    
    static Simulation sim = Simulation.instance() ;
    static SimulationConfig simulationConfig = SimulationConfig.instance();
    static MasterClock masterClock = sim.getMasterClock();

    AutosaveScheduler() {
    	// see https://stackoverflow.com/questions/36747987/how-to-setremoveoncancelpolicy-for-executors-newscheduledthreadpool5/36748183#36748183
    	autosaveService.setRemoveOnCancelPolicy(true);
    	
    	if (simulationConfig == null)
			simulationConfig = SimulationConfig.instance();
		
    	lastRemainingSeconds = simulationConfig.getAutosaveInterval() * 60;
    }
    
    static class MyTask implements Runnable {

        public void run() {
        	if (sim == null)
        		sim = Simulation.instance();
        	if (masterClock == null)
        		masterClock = sim.getMasterClock();
        	
        	if (sim.getAutosaveDefault()) {
        		// Autosave as default
        		masterClock.setSaveSim(SaveType.AUTOSAVE_AS_DEFAULT, null);
        	}
        	else {
        		// Autosave with build info and timestamp
        		masterClock.setSaveSim(SaveType.AUTOSAVE, null);
        	}
        }
    }

    /**
     * Cancel the autosave service
     */
    public static void cancel() {
    	if (t != null) {
    		lastRemainingSeconds = getRemainingSeconds();
//        	logger.config("Autosave's remaining seconds : " + lastRemainingSeconds);
    		t.cancel(true);
//    		if (t.isCancelled())
//    			logger.config("the autosave timer was cancelled.");
    		t = null;
    	}
    }
    
    /**
     * Starts the autosave service
     */
    public static void start() {
    	if (t == null) {
    		if (simulationConfig == null)
    			simulationConfig = SimulationConfig.instance();
    		
    		int s = simulationConfig.getAutosaveInterval() * 60;
    	    // see https://stackoverflow.com/questions/48216740/scheduledexecutorservice-end-after-a-timeout
    		t = autosaveService.scheduleAtFixedRate(new MyTask(), lastRemainingSeconds,
    				s, TimeUnit.SECONDS);
    	}
    }
    
    public static long getRemainingSeconds() {
    	return t.getDelay(TimeUnit.SECONDS);
    }
    
    /**
     * Starts the autosave service with a designated interval in minutes
     * 
     * @param minutes
     */
    public static void start(int minutes) {
    	if (t == null) {
    		if (simulationConfig == null)
    			simulationConfig = SimulationConfig.instance();
    		
    		simulationConfig.setAutosaveInterval(minutes);
    		
    		int secs = minutes * 60;
    		
    		// Resets the remaining seconds to the new input value
    		if (lastRemainingSeconds > secs)
    			lastRemainingSeconds = secs;
    		
    		t = autosaveService.scheduleAtFixedRate(new MyTask(), lastRemainingSeconds,
    				secs, TimeUnit.SECONDS);
    	}
    }
    
    /**
     * Starts the autosave service with a designated interval in minutes
     * 
     * @param minutes
     */
    public static void defaultStart() {
    	if (t == null) {	
    		if (simulationConfig == null)
    			simulationConfig = SimulationConfig.instance();
    		
    		int s = simulationConfig.getAutosaveInterval() * 60;
    		
    		t = autosaveService.scheduleAtFixedRate(new MyTask(), s,
    				s, TimeUnit.SECONDS);
    	}
    }
    
    public void destroy() {
    	autosaveService = null;
    	t = null;
        sim = null;
        simulationConfig = null;
        masterClock = null;
    }
}
