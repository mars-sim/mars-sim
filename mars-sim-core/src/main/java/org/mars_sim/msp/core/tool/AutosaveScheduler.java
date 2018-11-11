/**
 * Mars Simulation Project
 * AutosaveScheduler.java
 * @version 3.1.0 2018-11-08
 * @author Manny Kung
 */

package org.mars_sim.msp.core.tool;

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

    static class MyTask implements Runnable {

        public void run() {
        	if (Simulation.instance().getAutosaveDefault()) {
        		Simulation.instance().getMasterClock().setSaveSim(Simulation.AUTOSAVE_AS_DEFAULT, null);
        	}
        	else {
        		Simulation.instance().getMasterClock().setAutosave(true);
        	}
        }
    }

    public static void cancel() {
    	if (t != null) {
    		t.cancel(true);
    		t = null;
    	}
    }
    
    public static void start() {
    	if (t == null)
    		t = autosaveService.scheduleAtFixedRate(new MyTask(), SimulationConfig.instance().getAutosaveInterval(),
				SimulationConfig.instance().getAutosaveInterval(), TimeUnit.MINUTES);
    }
}
