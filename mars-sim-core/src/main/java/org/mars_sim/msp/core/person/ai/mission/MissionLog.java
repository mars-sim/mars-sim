/*
 * Mars Simulation Project
 * MissionLog.java
 * @date 2022-09-03
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.time.MarsClock;

/**
 * Holds all log details about a Missiion
 */
public class MissionLog implements Serializable  {

	private static final long serialVersionUID = 1L;

	/**
     * POJO class for a log entry
     */
    public static class MissionLogEntry implements Serializable {
    	
        private static final long serialVersionUID = 1L;
        
        private MarsClock time;
        private String entry;
        
        private MissionLogEntry(MarsClock time, String entry) {
            super();
            this.time = time;
            this.entry = entry;
        }
    
        public MarsClock getTime() {
            return time;
        }
    
        public String getEntry() {
            return entry;
        }
    
        @Override
        public String toString() {
            return "MissionLogEntry [time=" + time + ", entry=" + entry + "]";
        }		
    }

    private List<MissionLogEntry> log = new ArrayList<>();
    private MarsClock startDate;
    private boolean done = false;
    protected static MarsClock marsClock;

    public void addEntry(String entry) {
		log.add(new MissionLogEntry(new MarsClock(marsClock), entry));
    }

    /**
	 * Gets the date filed timestamp of the mission.
	 *
	 * @return
	 */
	public MarsClock getDateCreated() {
		if (!log.isEmpty()) {
			return log.get(0).getTime();
		}
		return null;
	}

    
	/**
	 * Gets the date work started. This is after any preparation steps.
	 *
	 * @return
	 */
	public MarsClock getDateStarted() {
		return startDate;
	}

	/**
	 * Gets the date missions was finsihed
	 *
	 * @return
	 */
	public MarsClock getDateFinished() {
		if (done && !log.isEmpty()) {
            // TODO SHould this be when teh mission returned to the Settlement? 
			return log.get(log.size()-1).getTime();
		}

		return null;
	}

    void setDone() {
        done = true;
    }

    void setStarted() {
        if (startDate == null) {
            startDate = new MarsClock(marsClock);
        }
    }

    public List<MissionLogEntry> getEntries() {
        return log;
    }

    public static void initialise(MarsClock mc) {
        marsClock = mc;
    }

    public MissionLogEntry getLastEntry() {
        if (log.isEmpty()) {
            return null;
        }
        return log.get(log.size()-1);
    }
}
