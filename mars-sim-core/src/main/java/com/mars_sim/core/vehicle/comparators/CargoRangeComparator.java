/*
 * Mars Simulation Project
 * CargoRangeComparator.java
 * @date 2025-07-24
 * @author Barry Evans
 */
package com.mars_sim.core.vehicle.comparators;

import com.mars_sim.core.vehicle.Vehicle;

/**
 * This comparator compares Vehicles based on 2 criteria:
 * 1. Vehicle with largest cargo capacity
 * 2. If cargos are the same then largest range is preferred
 */
public class CargoRangeComparator extends RangeComparator {

    /**
     * This compares 2 Vehicles based on the ordering criteria
     */
    @Override
    public int compare(Vehicle o1, Vehicle o2) {
        int result = 0;

        // Check if one has more general cargo capacity than the other.
        double firstCapacity = o1.getCargoCapacity();
        double secondCapacity = o2.getCargoCapacity();
        if (firstCapacity > secondCapacity) {
            result = 1;
        } else if (secondCapacity > firstCapacity) {
            result = -1;
        }

        // Vehicle with superior range should be ranked higher.
        if (result == 0) {
            result = super.compare(o1, o2);
        }

		return result;
	}
}
