package org.mars_sim.msp.core.structure.building;

import java.util.ArrayList;
import java.util.Map;

import org.mars.sim.mapdata.location.LocalPosition;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;

@SuppressWarnings("serial")
public class MockBuilding extends Building {

	/* default logger. */
//	private static final Logger logger = Logger.getLogger(Building.class.getName());
	private static FunctionSpec lifeSupportSpec = null;
	
	private static FunctionSpec getLifeSupportSpec() {
		if (lifeSupportSpec == null) {
			
			lifeSupportSpec = new FunctionSpec(Map.of(BuildingConfig.POWER_REQUIRED, 1D,
													  FunctionSpec.CAPACITY, 10),
														null);
		}
		return lifeSupportSpec;
	}

    public MockBuilding() {
    	super();
    }
    
    public MockBuilding(BuildingManager manager, String name)  {
		super(manager, name);
		buildingType = "EVA Airlock";

		if (manager == null) {
			throw new IllegalArgumentException("Bulding manager can not be null");
		}
		manager.addMockBuilding(this);

		malfunctionManager = new MalfunctionManager(this, 0D, 0D);
		functions = new ArrayList<>();
		functions.add(new LifeSupport(this, getLifeSupportSpec()));
	}
    
	public MockBuilding(BuildingTemplate template, BuildingManager manager)  {
		super(template, manager);
		buildingType = "Mock Type";
		changeName("Mock Building");
		
		unitManager.addUnit(this);

		malfunctionManager = new MalfunctionManager(this, 0D, 0D);
		functions = new ArrayList<>();
		functions.add(new LifeSupport(this, getLifeSupportSpec()));
	}

	public void setTemplateID(int id) {
		this.templateID = id;
	}

	public void setBuildingType(String type) {
	    this.buildingType = type;
	}

	public void setLocation(LocalPosition loc) {
		this.loc = loc;
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
	
	public void setLocation(double x, double y) {
		loc = new LocalPosition(x, y);	
	}
}