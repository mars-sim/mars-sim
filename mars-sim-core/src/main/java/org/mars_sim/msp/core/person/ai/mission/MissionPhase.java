/**
 * Mars Simulation Project
 * MissionPhase.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.project.Stage;

/**
 * A phase of a mission.
 */
public final class MissionPhase implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String MSG_KEY_PREFIX = "Mission.phase.";

	// The phase name.
	private String name;
	
	private String descriptionTemplate = null;

	private Stage stage = Stage.ACTIVE;

	/**
	 * Constructor for an ACTIVE phase
	 * 
	 * @param the phase name.
	 */
	public MissionPhase(String name) {
		this(name, Stage.ACTIVE);
	}

	/**
	 * Constructor
	 * 
	 * @param key The key for the phase name.
	 */
	public MissionPhase(String key, Stage stage) {
		// Hack for the transition phase
		if (!key.startsWith(MSG_KEY_PREFIX)) {
			key = MSG_KEY_PREFIX + key;
		}

		this.name = Msg.getString(key);
		this.descriptionTemplate = Msg.getString(key + ".description");

		this.stage = stage;
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
	
	/**
	 * Get teh Stage associated wth this phase.
	 */
	public Stage getStage() {
		return stage;
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
