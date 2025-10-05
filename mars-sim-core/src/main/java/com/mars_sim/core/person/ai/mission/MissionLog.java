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

    private List<MissionLogEntry> log = new ArrayList<>();
    private MarsTime timestampEmbarked;
    private boolean done = false;
    protected static MasterClock clock;

    /**
     * Compares with the previous log entry. 
     * 
     * @param entry
     * @param enterBy
     * @param size
     * @param i
     */
    private void compareLog(String entry, String enterBy, int size, int i) {
    	MissionLogEntry log0 = log.get(size - i);
    	String entry0 = log0.getEntry();
    	String enterBy0 = log0.getEnterBy();
    	if (entry0.equals(entry) && enterBy0.equals(enterBy)
        	) {
        	// Do not need to add a new log entry
        }
    	else if (entry0.equals(entry) && !enterBy0.equals(enterBy)
	        ) {
    		// Add new log entry
	        log.add(new MissionLogEntry(clock.getMarsTime(), entry, enterBy));
	       }
    	else {
    		// Add new log entry
    		log.add(new MissionLogEntry(clock.getMarsTime(), entry, enterBy));
    	}
    }
    
    /**
     * Adds an entry.
     * 
     * @param entry
	 * @param enterBy the name of the person who logs this
     */
    public void addEntry(String entry, String enterBy) {
    	int size = log.size();

    	if (size == 0) {
    		// Add new log entry
	        log.add(new MissionLogEntry(clock.getMarsTime(), entry, enterBy));
    	}
    	else if (size == 1) {
    		// Check on log1
    		compareLog(entry, enterBy, size, 1);
    	}
    	else if (size == 2) {
    		// Check on log2
    		compareLog(entry, enterBy, size, 2);
    		// Check on log1
    		compareLog(entry, enterBy, size, 1);
    		
		}
    	else {
    		// Compare with the last 3 log entries
    		
    		// Check on log3
    		compareLog(entry, enterBy, 3, 3);
    		// Check on log2
    		compareLog(entry, enterBy, 3, 2);
    		// Check on log1
    		compareLog(entry, enterBy, 3, 1);
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
