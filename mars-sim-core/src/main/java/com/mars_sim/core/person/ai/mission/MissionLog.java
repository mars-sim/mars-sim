/*
 * Mars Simulation Project
 * MissionLog.java
 * @date 2025-09-10
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;

/**
 * The class that holds all log details about a Mission.
 */
public class MissionLog implements Serializable  {

	private static final long serialVersionUID = 1L;

	/**
     * POJO class for a log entry.
     */
    public static class MissionLogEntry implements Serializable {
    	
        private static final long serialVersionUID = 1L;
        
        private MarsTime time;
        private String entry;
        private String enterBy;
        
        private MissionLogEntry(MarsTime time, String entry, String enterBy) {
            super();
            this.time = time;
            this.entry = entry;
            this.enterBy = enterBy;
        }
      
        public MarsTime getTime() {
            return time;
        }
    
        public String getEntry() {
            return entry;
        }
        
        public String getEnterBy() {
        	return enterBy;
        }
    
        @Override
        public String toString() {
            return "MissionLogEntry [time=" + time + ", entry=" + entry + "]";
        }		
    }
    
    private boolean done = false;
    
    private String lastEntry = "";
    
    private String lastEnterBy = "";
    
    private List<MissionLogEntry> log;
    
    private MarsTime timestampEmbarked;
    
    protected static MasterClock clock;

    public MissionLog() {
    	log = new ArrayList<>();
    }
    
    /**
     * Compares with the previous log entry. 
     * 
     * @param entry
     * @param enterBy
     * @param i
     */
    private void compareLog(String entry, String enterBy, int i) {
    	MissionLogEntry log0 = log.get(i);
    	String entry0 = log0.getEntry();
    	String enterBy0 = log0.getEnterBy();
    	
    	if (!entry0.equals(entry)
    		|| !enterBy0.equals(enterBy)) {
    		// Add new log entry
    		log.add(new MissionLogEntry(clock.getMarsTime(), entry, enterBy));
    	}
    	
		// if not meeting above criteria, do not add a new log entry
    }
    
    /**
     * Adds an entry.
     * 
     * @param entry
	 * @param enterBy the name of the person who logs this
     */
    public void addEntry(String entry, String enterBy) {
    	if (lastEntry.equals(entry) && lastEnterBy.equals(enterBy)) {
    		return;
    	}
    	
    	lastEntry = entry;
    	lastEnterBy = enterBy;
    	
    	int size = log.size();

    	if (size == 0) {
    		// Add new log entry
	        log.add(new MissionLogEntry(clock.getMarsTime(), entry, enterBy));
    	}
    	else if (size == 1) {
    		// Check on log1
    		compareLog(entry, enterBy, size - 1);
    	}
    	else if (size == 2) {
    		// Check on log2
    		compareLog(entry, enterBy, size - 1);
    		// Check on log1
    		compareLog(entry, enterBy, size - 2);
    		
		}
    	else if (size == 3) {
    		// Compare with the last 3 log entries
       		// Compare with the last 4 log entries
    		for (int i=1; i < 4; i++) {
    			// Check on log_
        		compareLog(entry, enterBy, size - i);
    		}
    	}
    	else {
    		// Compare with the last 4 log entries
    		for (int i=1; i < 5; i++) {
    			// Check on log_
        		compareLog(entry, enterBy, size - i);
    		}
    	}
    }

    /**
     * Adds an entry.
     * 
     * @param entry
	 * @param enterBy the name of the person who logs this
     */
    public void addEntry(String entry) {
    	addEntry(entry, "");
    }
    
    /**
	 * Gets the filed timestamp of the mission.
	 *
	 * @return
	 */
	public MarsTime getTimestampFiled() {
		if (!log.isEmpty()) {
			return log.get(0).getTime();
		}
		return null;
	}

    
	/**
	 * Gets the embarked timestamp when the vehicle departed. This is after any preparation steps.
	 *
	 * @return
	 */
	public MarsTime getTimestampEmbarked() {
		return timestampEmbarked;
	}


    /**
     * Gets the embarked timestamp.
     */
	public void generatedDateEmbarked() {
		if (timestampEmbarked == null) {
			timestampEmbarked = clock.getMarsTime();
		}
	}

	/**
	 * Gets the completed timestamp when the mission was done.
	 *
	 * @return
	 */
	public MarsTime getTimestampCompleted() {
		if (done && !log.isEmpty()) {
			return log.get(log.size()-1).getTime();
		}

		return null;
	}

    void setDone() {
        done = true;
    }

    public List<MissionLogEntry> getEntries() {
        return log;
    }

    public static void initialise(MasterClock mc) {
        clock = mc;
    }

    public MissionLogEntry getLastEntry() {
        if (log.isEmpty()) {
            return null;
        }
        return log.get(log.size()-1);
    }
}
