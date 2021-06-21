/**
 * Mars Simulation Project
 * DayDreamMeta.java
 * @version 3.2.0 2021-06-20
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.DayDream;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;

/**
 * Meta task for the Day Dream task. It can always be done.
 */
public class DayDreamMeta extends MetaTask {

    public DayDreamMeta() {
		super(DayDream.NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
	}

    @Override
    public Task constructInstance(Person person) {
    	if (person.isInside())
    		return new DayDream(person);
    	else // A day-dreaming person outside should walk back to the settlement
    		return new Walk(person);
    }

    /**
     * Always returns a potential probability.
     */
    @Override
    public double getProbability(Person person) {
    	return 0.0001D;
    }
}
