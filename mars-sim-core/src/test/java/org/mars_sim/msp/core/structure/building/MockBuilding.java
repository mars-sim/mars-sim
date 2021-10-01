package org.mars_sim.msp.core.structure.building;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;

@SuppressWarnings("serial")
public class MockBuilding extends Building {

	/* default logger. */
	private static final Logger logger = Logger.getLogger(Building.class.getName());
	
    public MockBuilding() {
    	super();
    }
    
    public MockBuilding(BuildingManager manager, String name)  {
		super(manager, name);
		buildingType = "EVA Airlock";
		setNickName(name);
		changeName(name);

		if (manager == null) {
			throw new IllegalArgumentException("Bulding manager can not be null");
		}
		manager.addMockBuilding(this);

		malfunctionManager = new MalfunctionManager(this, 0D, 0D);
		functions = new ArrayList<Function>();
		functions.add(new LifeSupport(this, 10, 1));
	}
    
	public MockBuilding(BuildingTemplate template, BuildingManager manager)  {
		super(template, manager);
		buildingType = "Mock Type";
		setNickName("Mock Building");
		changeName("Mock Building");
		
		unitManager.addUnit(this);

		malfunctionManager = new MalfunctionManager(this, 0D, 0D);
		functions = new ArrayList<Function>();
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

	public void setZLocation(double zLoc) {
	    this.zLoc = zLoc;
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

	@Override
	public Inventory getInventory() {
		return null;
	}
	
	@Override
	public String toString() {
		return super.getNickName();
	}
}