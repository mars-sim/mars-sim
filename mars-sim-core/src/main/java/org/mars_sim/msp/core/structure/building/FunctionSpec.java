/*
 * Mars Simulation Project
 * FunctionSpec.java
 * @date 2022-07-06
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure.building;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.LocalPosition;

/**
 * Represents a specification of a Building Function in a generic fashion
 */
public class FunctionSpec {
    // Name  of the standard capacity property
    public static final String CAPACITY = "capacity";

    // Name  of the standard tech level property
    private static final String TECH_LEVEL = "tech-level";

	private Map<String, Object> props;
	private List<LocalPosition> spots;

	public FunctionSpec(Map<String, Object> props, List<LocalPosition> spots) {
		this.props = props;
		this.spots = Collections.unmodifiableList(spots);
	}

	public List<LocalPosition> getActivitySpots() {
		return spots;
	}
	
	/**
	 * Get the custom Function property
	 * @param name
	 * @return
	 */
	public Object getProperty(String name) {
		return props.get(name);
	}

    /**
     * Get the value of the standard Capacity property
     */
    public int getCapacity() {
		return getIntegerProperty(CAPACITY);
    }

    /**
     * Get the value of the standard TechLevel property
     */
    public int getTechLevel() {
		return getIntegerProperty(TECH_LEVEL);
    }

    /**
     * Get a Function property as a Position object.
     * @param propName
     * @return
     */
    public LocalPosition getPositionProperty(String propName) {
        return (LocalPosition) props.get(propName);
    }

    /**
     * Get a Function property as a double object.
     * @param propName
     * @return
     */
    public double getDoubleProperty(String propName) {
		return Double.parseDouble((String) props.get(propName));
    }

    /**
     * Get a Function property as a integer object.
     * @param propName
     * @return
     */
    public int getIntegerProperty(String propName) {
		return Integer.parseInt((String) props.get(propName));
    }
}