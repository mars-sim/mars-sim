/*
 * Mars Simulation Project
 * GroupActivity.java
 * @date 2023-03-17
 * @author Barry Evans
 */
package com.mars_sim.core.activities;

import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.time.MarsTime;

/**
 * This represents a scheduled GroupActivity based on a definition. This will cycle through a state flow
 * as time advances and uses the ScheduledEvent logic to advance it.
 * The activity can be reused or be a one off; defined bu the associated definition.
 */
public class GroupActivity implements ScheduledEventHandler {
    /**
     * The State that a Group Activity transitions
     */
    public enum ActivityState {
        SCHEDULED, PENDING, ACTIVE, DONE, UNSCHEDULED;
    }

    private GroupActivityInfo definition;
    private ActivityState state = ActivityState.UNSCHEDULED;
    private Building meetingPlace;
    private Settlement owner;
    private MarsTime startTime;

    /**
     * Create an activity for a specific Settlement. This will schedule the initial ScheduledEvent to
     * handle the first occurance of this Activity adjusted to the local time zone, e.g. if the Info
     * define midday, it will be midday local time.
     * 
     * @param definition Activity being created
     * @param owner Owning Settlement
     * @param now Current time for scheduling
     */
    public GroupActivity(GroupActivityInfo definition, Settlement owner, MarsTime now) {
        this.definition = definition;
        this.owner = owner;

        attachToSettlement(now);
    }

    private void attachToSettlement(MarsTime now) {    
        // Calculate the duration to the next scheduled start of this activity
        // But adjust to the local time zone
        int offset = MarsSurface.getTimeOffset(owner.getCoordinates());
        int standardStartTime = (definition.startTime() + offset) % 1000;

        // mSols to the schedued start
        int toEvent = standardStartTime - now.getMillisolInt();
        if (toEvent < 0) {
            // Passed today
            toEvent = 1000 + toEvent;
        } 

        // Add in the first sol
        toEvent += definition.firstSol() * 1000;
        
        state = ActivityState.SCHEDULED;
        startTime = now.addTime(toEvent);
        owner.getFutureManager().addEvent(startTime, this);
    }

    /**
     * Description of the future scheduled event for this activity.
     */
    @Override
    public String getEventDescription() {
        return getName() + " - " + switch(state) {
            case ACTIVE -> "end";
            case DONE -> "done";
            case PENDING -> "starting";
            case SCHEDULED -> "scheduled start";
            case UNSCHEDULED -> "unscheduled";
        };
    }

    /**
     * A timed changed has arrived for this activity. It will involve moving the state onwards.
     */
    @Override
    public int execute(MarsTime currentTime) {
        switch(state) {
            case SCHEDULED:
                if (selectMeetingPlace()) {
                    state = ActivityState.PENDING;
                    return definition.waitDuration();
                }
                else {
                    // Reschedule
                    return 1000;
                }
            case ACTIVE:
                // Activity has completed, so rescheduled in the future
                meetingPlace = null;
                int freq = definition.solFrequency();
                if (freq > 0) {
                    state = ActivityState.SCHEDULED;
                    int elapsed = 1000 * freq;
                    startTime = currentTime.addTime(elapsed);
                    return elapsed;
                }
                else {
                    state = ActivityState.DONE;
                    return -1; // A one off
                }
            
            case PENDING:
                // Waitng time is over, start
                state = ActivityState.ACTIVE;
                return definition.activityDuration();
            default:
                throw new IllegalStateException("Don;t know about state " + state);
        }
    }

    /**
     * Find a meeting place of the correct type
     */
    private boolean selectMeetingPlace() {
        // Select the largest
        var selected = owner.getBuildingManager().getBuildingsOfSameCategory(definition.place()).stream()
                            .filter(b -> b.getLifeSupport() != null)
                            // Sorted in reverse order so largest first
                            .sorted((a,b) -> Integer.compare(b.getLifeSupport().getOccupantCapacity(),
                                                            a.getLifeSupport().getOccupantCapacity()))
                            .findFirst();
        if (selected.isPresent()) {
            meetingPlace = selected.get();
            return true;
        }
        return false;
    }

    /**
     * The definition of this activity defining the behaviour.
     * @return
     */
    public GroupActivityInfo getDefinition() {
        return definition;
    }   
    
    /**
     * Is this activity active, i.e can Persons join it.
     * @return
     */
    public boolean isActive() {
        return (state == ActivityState.PENDING) || (state == ActivityState.ACTIVE);
    }

    /**
     * Curent state of the activity.
     * @return
     */
    public ActivityState getState() {
        return state;
    }

    /**
     * Allocated meeting place for this activity based on the type of category
     * @return
     */
    public Building getMeetingPlace() {
        return meetingPlace;
    }

    public String getName() {
        return definition.name();
    }

    /**
     * Get the total time this activity could take. Is the wait & actvity duration.
     * @return
     */
    public int getTotalDuration() {
        return definition.waitDuration() + definition.activityDuration();
    }

    /**
     * Start time of the next scheduled start
     * @return
     */
    public MarsTime getStartTime() {
        return startTime;
    }
}
