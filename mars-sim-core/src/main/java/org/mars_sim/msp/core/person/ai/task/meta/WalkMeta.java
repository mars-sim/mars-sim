/**
 * Mars Simulation Project
 * WalkMeta.java
 * @version 3.1.0 2017-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.robot.Robot;

/**
 * Meta task for the Walk task.
 */
public class WalkMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.walk"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new Walk(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
     		
        // If person is outside, give high probability to walk to emergency airlock location.
        if (person.isOutside()) {
            result = 500D;
        }
        else if (person.isInSettlement()) {
            // If person is inside a settlement building, may walk to a random location within settlement.
            result = .5D;
        }
        else if (person.isInVehicle()) {
            // If person is inside a rover, may walk to random location within rover.
            result = .5D;
        }

        if (result < 0) result = 0;

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new Walk(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        //double result = 0D;

        // If robot is outside, give high probability to walk to emergency airlock location.
        //if (LocationSituation.OUTSIDE == robot.getLocationSituation()) {
        //    result = 2000D;
        //}

        return 0;
	}
}