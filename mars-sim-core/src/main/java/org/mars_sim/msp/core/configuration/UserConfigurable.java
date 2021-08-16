/**
 * Mars Simulation Project
 * UserConfigurable.java
 * @author Barry Evans
 */
package org.mars_sim.msp.core.configuration;

/**
 * This represents a configuration ites that can be configured by the user.
 */
public interface UserConfigurable {

	/**
	 * Get the description.
	 * @return
	 */
	String getDescription();

	/**
	 * Update the description.
	 * @param description
	 */
	void setDescription(String description);

	/**
	 * Get the unique name of this configurable item.
	 * @return
	 */
	String getName();

	/**
	 * Is this crew bundled with the code base
	 * @return
	 */
	boolean isBundled();

}
