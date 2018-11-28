/**
 * Mars Simulation Project
 * AutosaveScheduler.java
 * @version 3.1.0 2018-11-08
 * @author Manny Kung
 */

package org.mars_sim.msp.core.time;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;


// See https://stackoverflow.com/questions/14889143/how-to-stop-a-task-in-scheduledthreadpoolexecutor-once-i-think-its-completed
// regarding the issue of calling cancel()

public class AutosaveScheduler {
    static ScheduledExecutorService autosaveService = Executors.newSingleThreadScheduledExecutor();
    static ScheduledFuture<?> t;
    
    static long lastRemainingMinutes;
    
    static Simulation sim = Simulation.instance() ;
    static SimulationConfig simulationConfig = SimulationConfig.instance();
    static MasterClock masterClock = sim.getMasterClock();

    AutosaveScheduler() {
    	if (simulationConfig == null)
			simulationConfig = SimulationConfig.instance();
		
    	lastRemainingMinutes = simulationConfig.getAutosaveInterval();
    }
    
    static class MyTask implements Runnable {

        public void run() {
        	if (sim == null)
        		sim = Simulation.instance();
        	if (masterClock == null)
        		masterClock = sim.getMasterClock();
        	
        	if (sim.getAutosaveDefault()) {
        		// Choose the type of autosave based on the setting
        		masterClock.setSaveSim(Simulation.AUTOSAVE_AS_DEFAULT, null);
        	}
        	else {
        		masterClock.setAutosave(true);
        	}
        }
    }

    /**
     * Cancel the autosave service
     */
    public static void cancel() {
    	if (t != null) {
    		lastRemainingMinutes = getRemainingMinutes();
    		t.cancel(false);
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
    		
    		int m = simulationConfig.getAutosaveInterval();
    	    // see https://stackoverflow.com/questions/48216740/scheduledexecutorservice-end-after-a-timeout
    		t = autosaveService.scheduleAtFixedRate(new MyTask(), lastRemainingMinutes,
    				m, TimeUnit.MINUTES);
    	}
    }
    
    public static long getRemainingMinutes() {
    	return t.getDelay(TimeUnit.MINUTES);
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
    		
    		t = autosaveService.scheduleAtFixedRate(new MyTask(), minutes,
    				minutes, TimeUnit.MINUTES);
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
    		
    		int m = simulationConfig.getAutosaveInterval();
    		
    		t = autosaveService.scheduleAtFixedRate(new MyTask(), m,
    				m, TimeUnit.MINUTES);
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
