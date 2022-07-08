/*
 * Mars Simulation Project
 * SourceSpec.java
 * @date 2022-07-07
 * @author Barry Evans
 */

package org.mars_sim.msp.core.structure.building;

import java.util.Properties;

/**
 * Spec of either a power or heat source.
 */
public class SourceSpec {
	public static final String TOGGLE = "toggle";

	public static final String FUEL_TYPE = "fuel-type";

	public static final String CONSUMPTION_RATE = "consumption-rate";
	
	private String type;
	private double capacity;
	private Properties attributes;
	
	public SourceSpec(String type, double capacity, Properties attributes) {
		super();
		this.type = type;
		this.capacity = capacity;
		this.attributes = attributes;
	}

	public String getType() {
		return type;
	}

	public double getCapacity() {
		return capacity;
	}

	public String getAttribute(String prop) {
		return attributes.getProperty(prop);
	}
	
	
}
