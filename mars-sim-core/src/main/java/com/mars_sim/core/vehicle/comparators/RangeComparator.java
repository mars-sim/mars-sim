/*
 * Mars Simulation Project
 * RangeComparator.java
 * @date 2025-07-24
 * @author Barry Evans
 */
package com.mars_sim.core.vehicle.comparators;

import java.util.Comparator;

import com.mars_sim.core.vehicle.Vehicle;

/**
 * This compares 2 Vehicles and orders them according to largest estimated distance
 */
public class RangeComparator implements Comparator<Vehicle> {
    /**
     * This compares 2 Vehicles based on the ordering criteria
     */
    @Override
    public int compare(Vehicle o1, Vehicle o2) {
        int result = 0;

        // Vehicle with superior range should be ranked higher.
        double firstRange = o1.getEstimatedRange();
        double secondRange = o2.getEstimatedRange();
        if (firstRange > secondRange) {
            result = 1;
        } else if (firstRange < secondRange) {
            result = -1;
        }

		return result;
	}
}
