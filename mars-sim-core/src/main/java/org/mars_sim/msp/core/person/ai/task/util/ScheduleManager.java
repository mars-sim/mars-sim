/*
 * Mars Simulation Project
 * ScheduleManager.java
 * @date 2023-07-03
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.task.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

public class ScheduleManager implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;
	
	private Person person;
	
	private Set<Appointment> appointments = new HashSet<>();
	
	public ScheduleManager(Person person) {
		this.person = person;	
	}
	
	/**
	 * Person can take action with time passing.
	 *
	 * @param pulse amount of time passing (in millisols).
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {

		return true;
	}
	
	
}
