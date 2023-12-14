/*
 * Mars Simulation Project
 * SkillOwner.java
 * @date 2023-12-14
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai;

import com.mars_sim.mapdata.location.Coordinates;

public interface SkillOwner {

	/**
	 * Returns a reference to the Person's skill manager
	 *
	 * @return the person's skill manager
	 */
	public SkillManager getSkillManager();

	/**
	 * Gets the workers name.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Where the the Worker positioned on the Mars Surface?
	 * 
	 * @return
	 */
	public Coordinates getCoordinates();

	/**
	 * How physically efficient is this Worker.
	 * 
	 * @return
	 */
	public double getPerformanceRating();
}
