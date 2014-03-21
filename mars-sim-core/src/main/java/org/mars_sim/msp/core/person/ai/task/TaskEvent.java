/**
 * Mars Simulation Project
 * TaskEvent.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;

/**
 * This class represents the historical actions involving tasks.
 */
public class TaskEvent
extends HistoricalEvent {

	/**
	 * Constructor.
	 * @param person The person performing the task.
	 * @param task The task with the event.
	 * @param eventType The type of event.
	 * @param description Further description of the event (may be empty string).
	 */
	public TaskEvent(Person person, Task task, EventType eventType, String description) {
	
		// Use HistoricalEvent constructor.
		super(HistoricalEventCategory.TASK, eventType, person, task.getDescription() + " " + description);
	}
}
