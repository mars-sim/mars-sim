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
        
        private String time;
        private String entry;
        
        private MissionLogEntry(String time, String entry) {
            super();
            this.time = time;
            this.entry = entry;
        }
    
        public String getTime() {
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
    private String startDate;
    private boolean done = false;
    protected static MarsClock marsClock;

    void addEntry(String entry) {
		String time = marsClock.getTrucatedDateTimeStamp();
		log.add(new MissionLogEntry(time, entry));
    }

    /**
	 * Gets the date filed timestamp of the mission.
	 *
	 * @return
	 */
	public String getDateCreated() {
		if (!log.isEmpty()) {
			return log.get(0).getTime();
		}
		return "";
	}

    
	/**
	 * Gets the date work started. This is after any preparation steps.
	 *
	 * @return
	 */
	public String getDateStarted() {
		return startDate;
	}

	/**
	 * Gets the date missions was finsihed
	 *
	 * @return
	 */
	public String getDateFinished() {
		if (done && !log.isEmpty()) {
            // TODO SHould this be when teh mission returned to the Settlement? 
			return log.get(log.size()-1).getTime();
		}

		return "";
	}

    void setDone() {
        done = true;
    }

    void setStarted() {
        if (startDate == null) {
            startDate = marsClock.getTrucatedDateTimeStamp();
        }
    }

    public List<MissionLogEntry> getEntries() {
        return log;
    }

    public static void initialise(MarsClock mc) {
        marsClock = mc;
    }
}
