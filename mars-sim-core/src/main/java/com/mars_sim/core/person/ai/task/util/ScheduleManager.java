/*
 * Mars Simulation Project
 * ScheduleManager.java
 * @date 2023-07-03
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.task.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.DigLocalRegolith;
import com.mars_sim.core.person.ai.task.Sleep;
import com.mars_sim.core.time.ClockPulse;

public class ScheduleManager implements Serializable { //, Temporal {

	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ScheduleManager.class.getName());
	
	private static final int STANDARD_SLEEP_TIME = 75;
	private static final int TIME_GAP = 5;
	private static final int STANDARD_PREPARATION_TIME = 10;
	
	private final String REOGOLITH_SIMPLE_NAME = DigLocalRegolith.SIMPLE_NAME;
	private final String REOGOLITH_TASK_NAME = DigLocalRegolith.NAME;
	private final String SLEEP_SIMPLE_NAME = Sleep.SIMPLE_NAME;
	
	private Person person;
	
	private Set<Appointment> appointments = new HashSet<>();
	
	public ScheduleManager(Person person) {
		this.person = person;
	}
	
	public void setAppointment(Appointment ap) {
		appointments.add(ap);
	}
	
	/**
	 * Person can take action with time passing.
	 *
	 * @param pulse amount of time passing (in millisols).
	 */
//	@Override
	public boolean timePassing(ClockPulse pulse) {

		Iterator<Appointment> i = appointments.iterator();
		while (i.hasNext()) {
			Appointment ap = i.next();
			if (ap.getSol() == pulse.getMarsTime().getMissionSol()) {
				// TODO: need to account for a person's work shift
				if (ap.getMillisolInt() - STANDARD_SLEEP_TIME - TIME_GAP <= pulse.getMarsTime().getMillisolInt() ) {			
					if (person.getMission() == null && ap.getTaskName().equalsIgnoreCase(REOGOLITH_TASK_NAME)) {
						
						// Add the sleep task
						// Account for a person being outside
						if (person.isOutside()) {
							// Add sleep as a pending task
							person.getTaskManager().addPendingTask(SLEEP_SIMPLE_NAME, false, TIME_GAP * 2, STANDARD_SLEEP_TIME - TIME_GAP);
						}
						else {	
							// Add sleep as a pending task
							person.getTaskManager().addPendingTask(SLEEP_SIMPLE_NAME, false, TIME_GAP, STANDARD_SLEEP_TIME);
//							person.getTaskManager().replaceTask(new Sleep(person, STANDARD_SLEEP_TIME));
						}
						// Add DigLocalReogth as a pending task
						person.getTaskManager().addPendingTask(REOGOLITH_SIMPLE_NAME, false, STANDARD_SLEEP_TIME + TIME_GAP, ap.getDuration());
						
						logger.info(person, "Getting some sleep before executing the appointed task '" + ap.getTaskName() + "'.");
						// Remove this appointment once executed
						i.remove();
					}	
				}
				else if (ap.getMillisolInt() - STANDARD_PREPARATION_TIME <= pulse.getMarsTime().getMillisolInt() ) {
					// Add a pending task
					person.getTaskManager().addPendingTask(ap.getTaskName(), false, STANDARD_PREPARATION_TIME, ap.getDuration());
					
					logger.info(person, "Ready to show up for the appointed task '" + ap.getTaskName() + "'.");
					// Remove this appointment once executed
					i.remove();
				}
			}
		}
		
		return true;
	}
	
	
}
