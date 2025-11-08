package com.mars_sim.core.structure.goods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

public class TestGood {

    ItemResource hammer;
    
    public TestGood() {
		super();
	}

	@BeforeEach
    protected void setUp() throws Exception {
        SimulationConfig config = SimulationConfig.loadConfig();
  
        // Don't need a full GoodsManager initialisation
        GoodsManager.initializeInstances(config, null, null, null);
	}

	@Test
	void testPrinterGood() {
		Good printer = GoodsUtil.getGood(ItemResourceUtil.SLS_3D_PRINTER_ID);
		
		testGoodsBasics(printer, GoodCategory.ITEM_RESOURCE);
	}

	@Test
	void testRobotGood() {
		Good g = GoodsUtil.getGood(RobotType.getResourceID(RobotType.CHEFBOT));
		
		testGoodsBasics(g, GoodCategory.ROBOT);
	}

	@Test
	void testBarrelGood() {
		Good g = GoodsUtil.getGood(EquipmentType.getResourceID(EquipmentType.BARREL));
		
		testGoodsBasics(g, GoodCategory.CONTAINER);
	}

	@Test
	void testRoverGood() {
		Good g = GoodsUtil.getGood(VehicleType.getVehicleID(VehicleType.CARGO_ROVER));
		
		testGoodsBasics(g, GoodCategory.VEHICLE);
	}

	@Test
	void testEVAGood() {
		Good g = GoodsUtil.getGood(EquipmentType.getResourceID(EquipmentType.EVA_SUIT));
		
		testGoodsBasics(g, GoodCategory.EQUIPMENT);
	}

	@Test
	void testOxygenGood() {
		Good g = GoodsUtil.getGood(ResourceUtil.OXYGEN_ID);
		
		testGoodsBasics(g, GoodCategory.AMOUNT_RESOURCE);
	}

	private void testGoodsBasics(Good good, GoodCategory category) {
		var name = good.getName();

		assertEquals(category, good.getCategory(), name + " type");
		assertTrue(good.getCostOutput() > 0D, name + " output cost");
		assertTrue(good.getFlattenDemand() > 0D, name + " output cost");
	}

}