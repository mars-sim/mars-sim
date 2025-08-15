/*
 * Mars Simulation Project
 * TaskEvent.java
 * @date 2022-07-24
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.util;

import com.mars_sim.core.Unit;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.person.EventType;

/**
 * This class represents the historical actions involving tasks.
 */
public class TaskEvent extends HistoricalEvent {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param person The person performing the task.
	 * @param task The task with the event.
	 * @param object The source of the event.
	 * @param eventType The type of event.
	 * @param description Further description of the event (may be empty string).
	 */
    // only 5 types of task events so far
	public TaskEvent(Unit unit, Task task, Object source, EventType eventType, String description) {
	
		// Use HistoricalEvent constructor.
		super(HistoricalEventCategory.TASK, 
				eventType, 
				source,
				"Tracking Task",
				description,
				unit.getName(),
				(Unit)source
		);
	}
}
