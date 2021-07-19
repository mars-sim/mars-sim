/**
 * Mars Simulation Project
 * MissionSubAgenda.java
 * @version 3.2.1 2021-07-19
 * @author Barry Evans
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.mars_sim.msp.core.person.ai.mission.MissionType;

/**
 * This class represents a sub-agenda of s Reporting 
 *
 */
public class MissionSubAgenda implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private String description;
	private Map<MissionType, Integer> modifiers;
	
	public MissionSubAgenda(String description, Map<MissionType, Integer> modifiers) {
		super();
		this.description = description;
		this.modifiers = Collections.unmodifiableMap(modifiers);
	}
	
	public String getDescription() {
		return description;
	}
	public Map<MissionType, Integer> getModifiers() {
		return modifiers;
	}
	
	
}
