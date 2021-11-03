/**
 * Mars Simulation Project
 * MissionPhase.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;

/**
 * A phase of a mission.
 */
public final class MissionPhase implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// The phase name.
	private String name;
	
	private String descriptionTemplate = null;

	/**
	 * Constructor
	 * 
	 * @param the phase name.
	 */
	public MissionPhase(String name) {
		// Hack for the transition phase
		if (name.startsWith("Mission.")) {
			// Assume it a key
			this.name = Msg.getString(name);
			this.descriptionTemplate = Msg.getString(name + ".description");
		}
		else {
			this.name = name;
		}
	}

	/**
	 * Gets the phase name.
	 * 
	 * @return phase name string.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the template for any description
	 * @return
	 */
	public String getDescriptionTemplate() {
		return descriptionTemplate;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj != null) && (obj instanceof MissionPhase)
				&& ((MissionPhase)obj).name.equals(name);
	}
	

	@Override
	public int hashCode() {
		return name.hashCode() % 32;
	}

}
