/*
 * Mars Simulation Project
 * SourceSpec.java
 * @date 2023-05-31
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

	private Properties attributes;
	
	private int numModules;
	private double conversionEfficiency;
	private double percentLoadCapacity;
	private double capacity;
	
	public SourceSpec(String type, Properties attributes, int numModules, double unitCapacity, double stirlingConversion, double percentLoadCapacity) {
		super();
		this.type = type;
		this.capacity = unitCapacity;
		this.attributes = attributes;
		this.numModules = numModules;
		this.conversionEfficiency = stirlingConversion;
		this.percentLoadCapacity = percentLoadCapacity;
	}

	public String getType() {
		return type;
	}

	public double getCapacity() {
		return capacity;
	}
	
	public int getNumModules() {
		return numModules;
	}

	public double getConversionEfficiency() {
		return conversionEfficiency;
	}
	
	public double getpercentLoadCapacity() {
		return percentLoadCapacity;
	}

	
	public String getAttribute(String prop) {
		return attributes.getProperty(prop);
	}
	
	
}
