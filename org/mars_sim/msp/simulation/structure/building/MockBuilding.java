package org.mars_sim.msp.simulation.structure.building;

import java.util.ArrayList;

import org.mars_sim.msp.simulation.structure.building.function.Function;
import org.mars_sim.msp.simulation.structure.building.function.LifeSupport;

public class MockBuilding extends Building {

	public MockBuilding(BuildingManager manager) throws BuildingException {
		name = "Mock Building";
		this.manager = manager;
		functions = new ArrayList<Function>();
		functions.add(new LifeSupport(this, 10, 1));
	}
}