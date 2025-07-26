/*
 * Mars Simulation Project
 * CrewRangeComparator.java
 * @date 2025-07-24
 * @author Barry Evans
 */
package com.mars_sim.core.vehicle.comparators;

import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * This comparator compares Vehicles based on 2 criteria:
 * 1. Size of crew
 * 2. Largest range is preferred
 */
public class CrewRangeComparator extends RangeComparator {

	@Override
	public int compare(Vehicle o1, Vehicle o2) {
		// Check of one rover has a research lab and the other one doesn't.
		int crew1 = (o1 instanceof Crewable c1 ? c1.getCrewCapacity() : 0);
		int crew2 = (o2 instanceof Crewable c2 ? c2.getCrewCapacity() : 0);
		if (crew1 > crew2)
			return 1;
		else if (crew1 < crew2)
			return -1;
		return super.compare(o1, o2);
	}
}