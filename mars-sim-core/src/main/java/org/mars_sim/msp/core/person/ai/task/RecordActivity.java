/**
 * Mars Simulation Project
 * RecordActivity.java
 * @version 3.1.0 2018-06-09
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The RecordActivity class is a task for recording events/activities 
 */
public class RecordActivity
extends Task
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.recordActivity"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase RECORDING = new TaskPhase(Msg.getString(
            "Task.phase.recordingActivity")); //$NON-NLS-1$

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -1D;

    /**
     * Constructor. This is an effort-driven task.
     * @param person the person performing the task.
     */
    public RecordActivity(Person person) {
        // Use Task constructor.
        super(NAME, person, true, false, STRESS_MODIFIER, true,
                RandomUtil.getRandomDouble(10D));

        if (person.isInSettlement()) {
        	this.walkToRandomLocation(false);
        }
        
        else if (person.isInVehicle()) {

            if (person.getVehicle() instanceof Rover) {
                walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
            }
        }
        
        else {
            endTask();
        }

        // Initialize phase
        addPhase(RECORDING);
        setPhase(RECORDING);
    }

    @Override
    protected FunctionType getLivingFunction() {
        return FunctionType.ADMINISTRATION;
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (RECORDING.equals(getPhase())) {
            return recordingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the recording phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double recordingPhase(double time) {
        // Do nothing
        return 0D;
    }

    @Override
    protected void addExperience(double time) {
        // This task adds no experience.
    }

    @Override
    public void endTask() {
        super.endTask();
    }


    @Override
    public int getEffectiveSkillLevel() {
        return 0;
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(0);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

    }
}