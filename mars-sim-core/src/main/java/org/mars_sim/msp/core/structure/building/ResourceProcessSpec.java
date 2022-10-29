/*
 * Mars Simulation Project
 * ResourceProcessSpec.java
 * @date 2021-08-20
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure.building;

import org.mars_sim.msp.core.process.ProcessSpec;

/**
 * The ResourceProcessSpec class represents the specification of a process of converting one set of
 * resources to another. This object is shared amongst ResourceProcess of the same type.
 */
public class ResourceProcessSpec extends ProcessSpec {

	private static final long serialVersionUID = 1L;


	private boolean defaultOn;

	public ResourceProcessSpec(String name, double powerRequired, int processTime, int workTime, boolean defaultOn) {
		super(name, powerRequired, processTime, workTime);

		this.defaultOn = defaultOn;
	}

	public boolean getDefaultOn() {
		return defaultOn;
	}
}
