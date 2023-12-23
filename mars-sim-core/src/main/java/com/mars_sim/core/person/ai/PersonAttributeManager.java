/*
 * Mars Simulation Project
 * PersonAttributeManager.java
 * @date 2023-11-30
 * @author Barry Evans
 */

package com.mars_sim.core.person.ai;

import java.util.Map;

public class PersonAttributeManager extends NaturalAttributeManager {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Construction an attribute manager with an initial set
	 * @param initialAttr Initial attribute to load; is optional
	 */
	public PersonAttributeManager(Map<NaturalAttributeType, Integer> initialAttr) {
		if (initialAttr != null) {
			for(var e : initialAttr.entrySet()) {
				setAttribute(e.getKey(), e.getValue());
			}	
		}
	}
}
