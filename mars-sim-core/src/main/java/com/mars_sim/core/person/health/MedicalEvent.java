/*
 * Mars Simulation Project
 * MedicalEvent.java
 * @date 2025-08-14
 * @author Scott Davis
 */
package com.mars_sim.core.person.health;

import com.mars_sim.core.Unit;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Conversion;

/**
 * This class represents the historical action of a medical problem occurring or
 * being resolved.  Death is also recorded with medical events.
 */
public class MedicalEvent extends HistoricalEvent {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor.
	 * 
	 * @param worker the worker with the medical problem.
	 * @param illness the medical problem.
	 * @param eventType the medical event type.
	 */
	public MedicalEvent(Worker worker, HealthProblem illness, EventType eventType) {
		// Call HistoricalEvent constructor.
		super(HistoricalEventCategory.MEDICAL,  
				eventType, 
				illness, 
				Conversion.capitalize0(illness.getComplaint().getName()),
				worker.getTaskDescription(),
				worker.getName(),
				(Unit)worker,
				worker.getAssociatedSettlement().getName(),
				worker.getCoordinates());
	}
}
