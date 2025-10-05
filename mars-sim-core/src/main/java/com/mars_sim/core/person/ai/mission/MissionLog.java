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
	 * The maximum number of log entries to compare for duplicates.
	 */
	static final int MAX_COMPARE = 2;

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
    
    private List<MissionLogEntry> log;
    
    private MarsTime timestampEmbarked;
    
    protected static MasterClock clock;

    public MissionLog() {
    	log = new ArrayList<>();
    }
    
    /**
     * Adds an entry.
     * 
     * @param entry
	 * @param enterBy the name of the person who logs this
     */
    public void addEntry(String entry, String enterBy) {
		if (!log.isEmpty()) {
			int compareSize = Math.min(log.size(), MAX_COMPARE);
			for(int i=1; i <= compareSize; i++) {
				MissionLogEntry log0 = log.get(log.size() - i);
				String entry0 = log0.getEntry();
				
				if (entry0.equals(entry)) {
					// Same as one of the chosen ones, do not add
					return;
				}
			}
		}

		// Add entry
		log.add(new MissionLogEntry(clock.getMarsTime(), entry, enterBy));
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
