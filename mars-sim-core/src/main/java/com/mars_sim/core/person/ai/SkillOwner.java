/*
 * Mars Simulation Project
 * SkillOwner.java
 * @date 2023-12-14
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai;

import com.mars_sim.core.Entity;

public interface SkillOwner extends Entity {

	/**
	 * Returns a reference to the Person's skill manager
	 *
	 * @return the person's skill manager
	 */
	public SkillManager getSkillManager();

	/**
	 * Gets the performance rating (0..1) of the owner.
	 * 
	 * @return
	 */
	public double getPerformanceRating();
}
