/*
 * Mars Simulation Project
 * MissionCapability.java
 * @date 2023-05-31
 * @author Barry Evans
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;
import java.util.Map;

/**
 * This class represents a demonstration capability to develop.
 */
public class MissionCapability implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String description;
	private Map<PreferenceKey, Double> preferences;

	public MissionCapability(String description, Map<PreferenceKey, Double> preferences) {
		super();
		this.description = description;
		this.preferences = preferences;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String toString() {
		return description;
	}

	/**
	 * What preferenes are defined for this capabilty
	 * @return
	 */
    public Map<PreferenceKey, Double> getPreferences() {
        return preferences;
    }
}
