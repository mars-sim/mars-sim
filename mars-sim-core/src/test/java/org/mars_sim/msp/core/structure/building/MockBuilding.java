package org.mars_sim.msp.core.structure.building;

import java.util.ArrayList;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;

@SuppressWarnings("serial")
public class MockBuilding extends Building {

    public MockBuilding() {
        this(null);
    }
    public MockBuilding(BuildingManager manager)  {
		super(manager);
		buildingType = "Mock Building";
		this.manager = manager;
		malfunctionManager = new MalfunctionManager(this, 0D, 0D);
		functions = new ArrayList<Function>();
		//functions = new HashSet<Function>();
		functions.add(new LifeSupport(this, 10, 1));
	}
	public MockBuilding(BuildingTemplate template, BuildingManager manager)  {
		super(template, manager);
		buildingType = "Mock Building";
		this.manager = manager;
		malfunctionManager = new MalfunctionManager(this, 0D, 0D);
		functions = new ArrayList<Function>();
		//functions = new HashSet<Function>();
		functions.add(new LifeSupport(this, 10, 1));
	}

	public void setTemplateID(int id) {
		this.templateID = id;
	}

	public void setBuildingType(String type) {
	    this.buildingType = type;
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

//	public void removeFunction(Function function) {
//		if (functions.contains(function))
//	        functions.remove(function);
//	}

	@Override
	public Inventory getInventory() {
		return null;//manager.getSettlement().getInventory();
	}
}