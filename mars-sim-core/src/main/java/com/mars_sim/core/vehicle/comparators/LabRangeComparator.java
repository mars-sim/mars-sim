/*
 * Mars Simulation Project
 * LabComparator.java
 * @date 2025-07-24
 * @author Barry Evans
 */package com.mars_sim.core.vehicle.comparators;

import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * This comparator compares Vehicles based on 2 criteria:
 * 1. Vehicle with lab
 * 2. Largest range is preferred
 */
public class LabRangeComparator extends RangeComparator {

	@Override
	public int compare(Vehicle o1, Vehicle o2) {
		// Check of one rover has a research lab and the other one doesn't.
		int firstLab = ((o1 instanceof Rover r1) && r1.hasLab()) ?  r1.getLab().getTechnologyLevel() : -1;
		int secondLab = ((o2 instanceof Rover r2) && r2.hasLab()) ?  r2.getLab().getTechnologyLevel() : -1;
		if (firstLab > secondLab)
			return 1;
		else if (firstLab < secondLab)
			return -1;
		return super.compare(o1, o2);
	}
}