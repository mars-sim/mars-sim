/*
 * Mars Simulation Project
 * Connection.java
 * @date 2022-07-13
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

public enum Connection {

	CHECKING_MESSAGES 		("Checking personal messages"),
	WATCHING_EARTH_NEWS		("Watching Earth news"),
	BROWSING_MARSNET		("Browsing MarsNet"),
	WATCHING_EARTH_TV		("Watching Earth TV"),
	WATCHING_A_MOVIE		("Watching a movie"),
	BROWSING_EARTH_INTERNET ("Browsing Earth internet"),
	;
		
	private String name;

	/** hidden constructor. */
	private Connection(String name) {
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}