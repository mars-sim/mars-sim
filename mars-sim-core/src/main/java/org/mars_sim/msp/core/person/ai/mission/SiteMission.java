/*
 * Mars Simulation Project
 * SiteMission.java
 * @date 2021-10-01
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.mission;

import org.mars_sim.msp.core.structure.Settlement;

/**
 * A Mission that is associated with a Site(s)
 */
public interface SiteMission {

	/**
	 * What is the total site score ofr this mission from the prespective
	 * if a reviewer's Settlement
	 * @param reviewerSettlement
	 * @return
	 */
	double getTotalSiteScore(Settlement reviewerSettlement);

}
