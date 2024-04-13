/*
 * Mars Simulation Project
 * GroupActivity.java
 * @date 2023-03-17
 * @author Barry Evans
 */
package com.mars_sim.core.activities;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.structure.GroupActivityType;
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
        SCHEDULED, PENDING, ACTIVE, DONE, UNSCHEDULED, CANCELLED;
    }

    private GroupActivityInfo definition;
    private ActivityState state = ActivityState.UNSCHEDULED;
    private Building meetingPlace;
    private Settlement owner;
    private MarsTime startTime;
    private Person instigator;
    private String name;

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
        this.name = definition.name();

        attachToSettlement(now, definition.firstSol());
    }

    /**
     * Create an activity for a specific person is the instigator.
     * @param name Special name of the activity
     * @param owner Owning Settlement
     * @param definition
     * @param instigator
     * @param now
     */
    private GroupActivity(String name, GroupActivityInfo definition, Settlement owner, Person instigator,
                            MarsTime now, int startSol) {
        this.instigator = instigator;
        this.definition = definition;
        this.owner = owner;
        this.name = name  +  (instigator != null ? " (" + instigator.getName() + ")"
                                                : "");

        attachToSettlement(now, startSol);
    }


    private void attachToSettlement(MarsTime now, int delayedSol) {    
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
        toEvent += delayedSol * 1000;
        
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
            case CANCELLED -> "cancelled";
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
                    // Cancel it
                    state = ActivityState.CANCELLED;
                    return -1;
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
     * Person who insitgates the meeting, could be null
     * @return Meeting Instigator
     */
    public Person getInstigator() {
        return instigator;
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
        return name;
    }

    /**
     * Get the total time this activity could take. Is the wait & actvity duration.
     * @return
     */
    public int getTotalDuration() {
        return definition.waitDuration() + definition.activityDuration();
    }

    /**
     * Get the definition of the impact on a Worker attending this activity
     */
    public ExperienceImpact getImpact() {
        return definition.impact();
    }

    /**
     * Start time of the next scheduled start
     * @return
     */
    public MarsTime getStartTime() {
        return startTime;
    }

    /**
     * THis helper method creates a person orientated activity in the future.
     * @param name Name of the activity
     * @param type Type of Person activity to request
     * @param host Settlement hosting activity
     * @param person Instigator of the activity; optional
     * @param now  Currnet mars time
     * @param dueInSols When is the event due in Sols
     */
    public static GroupActivity createPersonActivity(String name, GroupActivityType type,
                                    Settlement host, Person person, int dueInSol, MarsTime now) {
        GroupActivityInfo activityInfo = null;
        
        // First check that the request activity type is define for the settlement
        var template = SimulationConfig.instance().getSettlementConfiguration().getItem(host.getTemplate());
        var schedule = template.getActivitySchedule();
        if (schedule != null) {
            activityInfo = schedule.specials().get(type);
        }
        if (activityInfo == null) {
            // Not supported for this settlement 
            return null;
        }
        
        // Add event to the Settlment
        return new GroupActivity(name, activityInfo, host, person, now, dueInSol);
    }

}
