/*
 * Mars Simulation Project
 * ScheduleManager.java
 * @date 2023-07-03
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.task.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.DigLocalRegolith;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.time.ClockPulse;

public class ScheduleManager implements Serializable { //, Temporal {

	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ScheduleManager.class.getName());
	
	private static final int STANDARD_SLEEP_TIME = 75;
	private static final int TIME_GAP = 5;
	private static final int STANDARD_PREPARATION_TIME = 10;
	
	private String DIG_LOCAL_REOGOLITH = DigLocalRegolith.SIMPLE_NAME;
	
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
				
				if (ap.getMillisolInt() - STANDARD_SLEEP_TIME - TIME_GAP <= pulse.getMarsTime().getMillisolInt() ) {			
					if (ap.getTaskName().equalsIgnoreCase(DIG_LOCAL_REOGOLITH)) {
						// Execute the sleep task
						person.getTaskManager().replaceTask(new Sleep(person, STANDARD_SLEEP_TIME));
						// Add DigLocalReogth as a pending task
						person.getTaskManager().addAPendingTask(DIG_LOCAL_REOGOLITH, false, STANDARD_SLEEP_TIME + TIME_GAP, ap.getDuration());
						
						logger.info(person, "Getting some sleep before executing the appointed task '" + ap.getTaskName() + "'.");
						// Remove this appointment once execute
						i.remove();
					}	
				}
				else if (ap.getMillisolInt() - STANDARD_PREPARATION_TIME <= pulse.getMarsTime().getMillisolInt() ) {
					// Execute the sleep task
					person.getTaskManager().addAPendingTask(ap.getTaskName(), false, STANDARD_PREPARATION_TIME, ap.getDuration());
					
					logger.info(person, "Ready to show up for the appointed task '" + ap.getTaskName() + "'.");
					// Remove this appointment once execuite
					i.remove();
				}
			}
		}
		
		return true;
	}
	
	
}
