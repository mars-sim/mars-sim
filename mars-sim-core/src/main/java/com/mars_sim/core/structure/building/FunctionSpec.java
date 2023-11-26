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

import com.mars_sim.mapdata.location.LocalPosition;

/**
 * Represents a specification of a Building Function in a generic fashion
 */
public class FunctionSpec {
	// Name of the standard capacity property
	public static final String CAPACITY = "capacity";
	public static final String GUEST_BED = "guest";
	public static final String BED = "bed";

	// Name of the standard tech level property
	private static final String TECH_LEVEL = "tech-level";

	private BuildingSpec buildingSpec;
	private Map<String, Object> props;
	private Set<LocalPosition> spots;

	public FunctionSpec(Map<String, Object> props, Set<LocalPosition> spots) {
		this.props = props;
		if (spots == null) {
			this.spots = Collections.emptySet();
		} else {
			this.spots = Collections.unmodifiableSet(spots);
		}
	}

	public Set<LocalPosition> getActivitySpots() {
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
	 * Gets the value of the standard bed property.
	 */
	public int getGuestBeds() {
		return getIntegerProperty(GUEST_BED);
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
	 * Sets the building specs instance.
	 * 
	 * @param spec
	 */
	public void setBuildingSpec(BuildingSpec spec) {
		buildingSpec = spec;
	}

	/**
	 * Gets the building specs instance.
	 * 
	 * @param spec
	 */
	public BuildingSpec getBuildingSpec() {
		return buildingSpec;
	}
}