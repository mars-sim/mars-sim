/**
 * Mars Simulation Project
 * MissionDaylightStep.java
 * @date 2026-11-14
 * @author Barry Evans
 */
package com.mars_sim.core.mission.steps;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.mission.MissionProject;
import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.TimeWindow;
import com.mars_sim.core.tool.RandomUtil;

/**
 * MissionDaylightStep represents a step in the mission where the step awaits daylight before completing.
 * It receives a future event when the daylight period starts.
 */
public class MissionDaylightStep extends MissionStep 
    implements ScheduledEventHandler {

    private static final int LOCAL_START = 300;
    private static final int LOCAL_END = 700;
    private static final int START_SPREAD = 10; // Spread of start time in sols

    private boolean timeValid = false;
    private TimeWindow timeRange;

    public MissionDaylightStep(MissionProject mission) {
        super(mission, Stage.PREPARATION, "Await daylight");
    }

    @Override 
    protected void start() {
        var s = getMission().getAssociatedSettlement();

        // Get the valid time range in MCT
        timeRange = new TimeWindow(LOCAL_START, LOCAL_END, s.getTimeZone());

        var currentTime = Simulation.instance().getMasterClock().getMarsTime();
        timeValid = timeRange.isWithinRange(currentTime.getMillisolInt());

        // If not valid now then set an alarm
        if (!timeValid) {
            // Set an alarm for when daylight starts. Add some random spread to avoid exact timing collisions
            var startMSol = timeRange.startMSol() + RandomUtil.getRandomInt(START_SPREAD);
            var readyToStart = currentTime.advanceToNextMSol(startMSol);
            s.getFutureManager().addEvent(readyToStart, this);
        }

        super.start();
    }

    @Override
    protected boolean execute(Worker worker) {

        if (timeValid) {
            complete();
        }
        return false;
    }

    /**
     * Checks if the current time is within the valid daylight period.
     * 
     * @return true if the current time is within the daylight period, false otherwise.
     */
    public boolean isTimeValid() {
        return timeValid;
    }

    @Override
    public String getEventDescription() {
        return "Awaiting daylight for mission " + getMission().getName();
    }

    @Override
    public int execute(MarsTime currentTime) {
        // double check the time
        timeValid = timeRange.isWithinRange(currentTime.getMillisolInt());
        
        // If not valid then repeat later; should never happen
        return timeValid ? 0 : 10;
    }

    /**
     * Returns the valid time range for the daylight step.
     * 
     * @return the TimeWindow representing the valid daylight period.
     */
    public TimeWindow getValidRange() {
        return timeRange;
    }
}
