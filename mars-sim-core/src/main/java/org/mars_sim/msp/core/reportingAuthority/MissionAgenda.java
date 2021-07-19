/**
 * Mars Simulation Project
 * MissionAgenda.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import org.mars_sim.msp.core.person.ai.task.utils.Worker;

public interface MissionAgenda {

	public void reportFindings(Worker unit);

	public void gatherSamples(Worker unit);
		
	public String getObjectiveName();

	MissionSubAgenda[] getAgendas();

}
