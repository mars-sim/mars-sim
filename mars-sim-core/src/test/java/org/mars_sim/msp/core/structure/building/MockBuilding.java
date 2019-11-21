package org.mars_sim.msp.core.structure.building;

import java.util.ArrayList;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;

@SuppressWarnings("serial")
public class MockBuilding extends Building {

	/** The unit count for this building. */
	private static int uniqueCount = Unit.FIRST_BUILDING_UNIT_ID;
	/** Unique identifier for this settlement. */
	private int identifier;
	
//	private BuildingManager manager;
	
	/**
	 * Must be synchronised to prevent duplicate ids being assigned via different
	 * threads.
	 * 
	 * @return
	 */
	private static synchronized int getNextIdentifier() {
		return uniqueCount++;
	}
	
	/**
	 * Get the unique identifier for this settlement
	 * 
	 * @return Identifier
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	public void incrementID() {
		// Gets the identifier
		this.identifier = getNextIdentifier();
	}
	
    public MockBuilding() {
    	super();
    }
    
    public MockBuilding(BuildingManager manager)  {
		super(manager);
		buildingType = "Mock Type";
		super.changeName("Mock Building");
		
		settlementID = (Integer) manager.getSettlement().getIdentifier();
		
//		sim.getUnitManager().addBuildingID(this);
		sim.getUnitManager().addUnit(this);
		
//		this.manager = manager;
		malfunctionManager = new MalfunctionManager(this, 0D, 0D);
		functions = new ArrayList<Function>();
		//functions = new HashSet<Function>();
		functions.add(new LifeSupport(this, 10, 1));
	}
    
	public MockBuilding(BuildingTemplate template, BuildingManager manager)  {
		super(template, manager);
		buildingType = "Mock Type";
		super.changeName("Mock Building");
		
		settlementID = (Integer) manager.getSettlement().getIdentifier();
		
//		sim.getUnitManager().addBuildingID(this);
		sim.getUnitManager().addUnit(this);


//		this.manager = manager;
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
		return super.getName();
	}
}