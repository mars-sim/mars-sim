package com.mars_sim.core.structure.goods;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodCategory;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.resource.ItemResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.vehicle.VehicleType;

import junit.framework.TestCase;

public class TestGood extends TestCase {

    ItemResource hammer;
    
    public TestGood() {
		super();
	}

	@Override
    protected void setUp() throws Exception {
        SimulationConfig config = SimulationConfig.loadConfig();
  
        // Don't need a full GoodsManager initialisation
        GoodsManager.initializeInstances(config, null, null);
	}

	public void testPrinterGood() {
		Good printer = GoodsUtil.getGood(ItemResourceUtil.printerID);
		
		testGoodsBasics(printer, GoodCategory.ITEM_RESOURCE);
	}

	public void testRobotGood() {
		Good g = GoodsUtil.getGood(RobotType.getResourceID(RobotType.CHEFBOT));
		
		testGoodsBasics(g, GoodCategory.ROBOT);
	}

	public void testBarrelGood() {
		Good g = GoodsUtil.getGood(EquipmentType.getResourceID(EquipmentType.BARREL));
		
		testGoodsBasics(g, GoodCategory.CONTAINER);
	}

	public void testRoverGood() {
		Good g = GoodsUtil.getGood(VehicleType.getVehicleID(VehicleType.CARGO_ROVER));
		
		testGoodsBasics(g, GoodCategory.VEHICLE);
	}

	public void testEVAGood() {
		Good g = GoodsUtil.getGood(EquipmentType.getResourceID(EquipmentType.EVA_SUIT));
		
		testGoodsBasics(g, GoodCategory.EQUIPMENT);
	}

	public void testOxygenGood() {
		Good g = GoodsUtil.getGood(ResourceUtil.OXYGEN_ID);
		
		testGoodsBasics(g, GoodCategory.AMOUNT_RESOURCE);
	}

	private void testGoodsBasics(Good good, GoodCategory category) {
		var name = good.getName();

		assertEquals(name + " type", category, good.getCategory());
		assertTrue(name + " output cost", good.getCostOutput() > 0D);
		assertTrue(name + " output cost", good.getFlattenDemand() >0D);
	}

}