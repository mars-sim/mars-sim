/*
 * Mars Simulation Project
 * UserConfigurable.java
 * @date 2021-08-20
 * @author Barry Evans
 */
package com.mars_sim.core.configuration;

/**
 * This represents a configuration site that can be configured by the user.
 */
public interface UserConfigurable {

	/**
	 * Gets the description.
	 * 
	 * @return
	 */
	String getDescription();

	/**
	 * Gets the unique name of this configurable item.
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Is this crew bundled with the code base ?
	 * 
	 * @return
	 */
	boolean isBundled();

}
