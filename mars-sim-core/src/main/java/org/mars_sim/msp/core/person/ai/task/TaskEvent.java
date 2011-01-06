/**
 * Mars Simulation Project
 * TaskEvent.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.person.Person;

/**
 * This class represents the historical actions involving tasks.
 */
public class TaskEvent extends HistoricalEvent {

	// Task event types.
	public final static String START = "Task Starting";
	public final static String FINISH = "Task Finished";
	public final static String DEVELOPMENT = "Task Development";

	/**
	 * Constructor
	 * @param person The person performing the task.
	 * @param task The task with the event.
	 * @param eventType The type of event.
	 * @param description Further description of the event (may be empty string).
	 */
	public TaskEvent(Person person, Task task, String eventType, String description) {
	
		// Use HistoricalEvent constructor.
		super(HistoricalEventManager.TASK, eventType, person, task.getDescription() + " " + description);
	}
}
