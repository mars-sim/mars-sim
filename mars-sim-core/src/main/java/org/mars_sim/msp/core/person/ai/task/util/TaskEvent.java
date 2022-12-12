/*
 * Mars Simulation Project
 * TaskEvent.java
 * @date 2022-07-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.util;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.person.EventType;

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
	public TaskEvent(Unit person, Task task, Object source, EventType eventType, String description) {
	
		// Use HistoricalEvent constructor.
		super(HistoricalEventCategory.TASK, 
				eventType, 
				source,
				"Tracking Task",
				description,
				person.getName(),
				((Unit)source).getLocationTag().getImmediateLocation(),
				((Unit)source).getAssociatedSettlement().getName(),
				((Unit)source).getCoordinates().getCoordinateString()
		);
	}
}
