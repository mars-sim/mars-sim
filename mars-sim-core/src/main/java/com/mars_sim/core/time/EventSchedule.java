/*
 * Mars Simulation Project
 * EventSchedule.java
 * @date 2023-03-17
 * @author Barry Evans
 */
package com.mars_sim.core.time;

import java.io.Serializable;

/**
 * This represents the details of events that happen in the future on a schedule.
 * It can cover one off events or repeating events.
 */
public class EventSchedule implements Serializable {
    private static final long serialVersionUID = 1L;
	private int firstSol;
    private int frequency;
    private int timeOfDay;

    /**
     * Create a repeating schedule
     * @param firstSol The sol of the first occurance of the event
     * @param frequency Frequency in Sols of when this repeats
     * @param timeOfDay Time of day the event occurs
     */
    public EventSchedule(int firstSol, int frequency, int timeOfDay) {
        this.firstSol = firstSol;
        this.frequency = frequency;
        this.timeOfDay = timeOfDay;
    }

    public int getFirstSol() {
        return firstSol;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getTimeOfDay() {
        return timeOfDay;
    }

    /**
     * Calaculate the time when the first event will occur from the current time.
     * The schedule is defined in terms of Central Mars Time.
     * @param now Time on Mars now
     * @param zone Zone where the event is to be applied
     * @return
     */
    public MarsTime getFirstEvent(MarsTime now, MarsZone zone) {
        // Calculate the duration to the next scheduled start of this calendar
        // But adjust to the local time zone
        int standardStartTime = (timeOfDay + zone.getMSolOffset()) % 1000;

        // mSols to the schedued start
        int toEvent = standardStartTime - now.getMillisolInt();
        if (toEvent < 0) {
            // Passed today
            toEvent = 1000 + toEvent;
        } 

        // Add in the first sol
        toEvent += firstSol * 1000;
        return now.addTime(toEvent);
    }

    /**
     * Get a new Schedule when the sol to the first event is changed.
     * @param startSol New starting sol value.
     * @return
     */
    public EventSchedule adjustStartSol(int startSol) {
        return new EventSchedule(startSol, frequency, timeOfDay);
    }
}
