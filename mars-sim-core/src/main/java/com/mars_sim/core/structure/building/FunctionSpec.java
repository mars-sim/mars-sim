/*
 * Mars Simulation Project
 * FunctionSpec.java
 * @date 2023-11-24
 * @author Barry Evans
 */
package com.mars_sim.core.structure.building;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.mapdata.location.LocalPosition;

/**
 * Represents a specification of a Building Function in a generic fashion.
 */
public class FunctionSpec {
	// Name of the standard capacity property
	public static final String CAPACITY = "capacity";
	private static final String AREA = "area";
	private static final String DEPTH = "depth";
	public static final String BED = "bed";

	// Name of the standard tech level property
	private static final String TECH_LEVEL = "tech-level";

	private FunctionType type;
	private Map<String, Object> props;
	private Set<NamedPosition> spots;

	/**
	 * Constructor.
	 * 
	 * @param props
	 * @param spots
	 */
	public FunctionSpec(FunctionType type, Map<String, Object> props, Set<NamedPosition> spots) {
		this.props = props;
		this.type = type;
		if (spots == null) {
			this.spots = Collections.emptySet();
		} else {
			this.spots = Collections.unmodifiableSet(spots);
		}
	}

	public FunctionType getType() {
		return type;
	}
	
	public Set<NamedPosition> getActivitySpots() {
		return spots;
	}

	/**
	 * Gets the custom Function property.
	 * 
	 * @param name
	 * @return
	 */
	public Object getProperty(String name) {
		return props.get(name);
	}

	/**
	 * Gets the value of the standard capacity property.
	 */
	public int getCapacity() {
		return getIntegerProperty(CAPACITY);
	}

	/**
	 * Gets the value of the standard area property.
	 */
	public double getArea() {
		return getDoubleProperty(AREA);
	}
	
	/**
	 * Gets the value of the standard depth property.
	 */
	public double getDepth() {
		return getDoubleProperty(DEPTH);
	}

	/**
	 * Gets the value of the standard TechLevel property.
	 */
	public int getTechLevel() {
		return getIntegerProperty(TECH_LEVEL);
	}

	/**
	 * Gets a Function property as a Position object.
	 * 
	 * @param propName
	 * @return
	 */
	public LocalPosition getPositionProperty(String propName) {
		return (LocalPosition) props.get(propName);
	}

	/**
	 * Gets a Function property as a double object.
	 * 
	 * @param propName
	 * @return
	 */
	public double getDoubleProperty(String propName) {
		Object value = props.get(propName);
		if (value instanceof Double v) {
			return v;
		}
		return Double.parseDouble((String) value);
	}

	/**
	 * Gets a Function property as a integer object.
	 * 
	 * @param propName
	 * @return
	 */
	public int getIntegerProperty(String propName) {
		Object value = props.get(propName);
		if (value instanceof Integer v) {
			return v;
		}
		return Integer.parseInt((String) value);
	}

	/**
	 * Gets a Function property as a boolean object.
	 * 
	 * @param propName
	 * @return
	 */
    public boolean getBoolProperty(String propName, boolean defaultValue) {
		Object value = props.get(propName);
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Boolean v) {
			return v;
		}
		return Boolean.parseBoolean((String) value);
    }
}