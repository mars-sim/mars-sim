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
	private static final String NAME = Msg.getString("Task.description.walk"); //$NON-NLS-1$

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Task constructInstance(Person person) {
//      Walk walk = person.getWalk();
//    	if (walk == null) {
//    		walk = new Walk(person);
//    		person.setWalk(walk);
//    	}
//    	else {
//    		walk.taskCompute();
//    		walk.compute();
//    	}
//    	
//    	return walk;

		return new Walk(person);
	}

	@Override
	public double getProbability(Person person) {

		double result = 0D;

		// If person is outside, give high probability to walk to emergency airlock
		// location.
		if (person.isOutside()) {
			result = 500D;
		} else if (person.isInVehicle()) {
			// If person is inside a rover, may walk to random location within rover.
			result = .05D;
		} else if (person.isInSettlement()) {
			// If person is inside a settlement building, may walk to a random location
			// within settlement.
			result = .5D;
		}

		if (result < 0)
			result = 0;

		return result;
	}

	@Override
	public Task constructInstance(Robot robot) {
//      Walk walk = robot.getWalk();
//    	if (walk == null) {
//    		walk = new Walk(robot);
//    		robot.setWalk(walk);
//    	}
//    	else {
//    		walk.botCompute();
//    	}   	
		return new Walk(robot);
	}

	@Override
	public double getProbability(Robot robot) {
		return 0;
	}
}