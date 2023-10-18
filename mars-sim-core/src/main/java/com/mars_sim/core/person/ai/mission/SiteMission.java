/*
 * Mars Simulation Project
 * SiteMission.java
 * @date 2021-10-01
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission;

import com.mars_sim.core.structure.Settlement;

/**
 * A Mission that is associated with a Site(s)
 */
public interface SiteMission {

	/**
	 * Gets the total site score for this mission.
	 * 
	 * @param reviewerSettlement
	 * @return
	 */
	double getTotalSiteScore(Settlement reviewerSettlement);

}
