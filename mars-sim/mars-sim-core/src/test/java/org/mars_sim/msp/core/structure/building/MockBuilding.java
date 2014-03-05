package org.mars_sim.msp.core.structure.building;

import java.util.ArrayList;

import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;

public class MockBuilding extends Building {

    public MockBuilding() {
        this(null);
    }
    
	public MockBuilding(BuildingManager manager)  {
		name = "Mock Building";
		this.manager = manager;
		malfunctionManager = new MalfunctionManager(this, 0D, 0D);
		functions = new ArrayList<Function>();
		functions.add(new LifeSupport(this, 10, 1));
	}
	
	public void setID(int id) {
	    this.id = id;
	}
	
	public void setName(String name) {
	    this.name = name;
	}
	
	public void setXLocation(double xLoc) {
	    this.xLoc = xLoc;
	}
	
	public void setYLocation(double yLoc) {
	    this.yLoc = yLoc;
	}
	
	public void setWidth(double width) {
	    this.width = width;
	}
	
	public void setLength(double length) {
	    this.length = length;
	}
	
	public void setFacing(double facing) {
	    this.facing = facing;
	}
	
	public void addFunction(Function function) {
	    functions.add(function);
	}
}