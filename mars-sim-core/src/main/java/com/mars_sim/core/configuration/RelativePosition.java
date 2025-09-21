/*
 * Mars Simulation Project
 * RelativePosition.java
 * @date 2025-09-06
 * @author Barry Evans
 */
package com.mars_sim.core.configuration;

import java.io.Serializable;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;

/**
 * This is a position that is relative to an entity defined in a configuration.
 * It is expected that this is eventually converted into an absolute LocalPosition.
 */
public record RelativePosition(double x, double y) implements Serializable {

    /**
     * Creates an absolute LocalPosition for this relative position in a specific context.
     * 
     * @param context
     * @return
     */
    public LocalPosition toPosition(LocalBoundedObject context) {
		double[] translate = LocalAreaUtil.translateLocation(x, y, context);
		double translateX = translate[0];
		double translateY = translate[1];

		return new LocalPosition(translateX, translateY);
    }

    /**
	 * Gets the Relative position by applying a delta.
	 * 
	 * @param xDelta Adjustment on x axis
	 * @param yDelta Adjustment on y axis
	 */
	public RelativePosition move(double xDelta, double yDelta) {
		return new RelativePosition(x + xDelta, y + yDelta);
	}

	/**
	 * Is this position within the boundaries of an X & Y ?
	 * 
	 * @param maxX
	 * @param maxY
	 * @return
	 */
	public boolean isWithin(double maxX, double maxY) {
		return (x <= maxX && y <= maxY);
	}
}
