package com.mars_sim.core.structure.goods;

import java.util.List;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.equipment.BinType;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodType;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.resource.ItemResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;

import junit.framework.TestCase;

public class TestGoodsUtil extends TestCase {

    ItemResource hammer;
    
    public TestGoodsUtil() {
		super();
	}

	@Override
    protected void setUp() throws Exception {
        SimulationConfig config = SimulationConfig.loadConfig();
  
        // Don't need a full GoodsManager initialisation
        GoodsManager.initializeInstances(config, null, null, null);

        GoodType type = GoodType.TOOL;
        hammer = ItemResourceUtil.createItemResource("hammer", 1100, "a hand tool", type, 1.4D, 1);
    }

    public void testCreateItem() {
    	GoodType type = GoodType.INSTRUMENT;
    	Part microlens = ItemResourceUtil.createItemResource("microlens", 1102, "a test lense", type, 0.05D, 1);
    	assertNotNull(microlens);
    }

    public void testGoodsListNotNull() {
        List<Good> goodsList = GoodsUtil.getGoodsList();
        assertNotNull(goodsList);
	}

	public void testGoodsListNotZero() {
        List<Good> goodsList = GoodsUtil.getGoodsList();
		assertTrue(goodsList.size() > 0);
	}

	public void testGoodsListContainsWater() {
		Good waterGood = GoodsUtil.getGood(ResourceUtil.WATER_ID);
		assertNotNull("Found water Good", waterGood);
	}

	public void testGoodsListContainsHammer() {
        Good hammerGood = GoodsUtil.getGood(hammer.getID());
        // hammer is not a standardized part and is NOT registered on the goodsMap
        assertNull(hammerGood);
	}

	public void testGoodsListContainsBag() {
		Good bagGood = GoodsUtil.getEquipmentGood(EquipmentType.BAG);
		assertNotNull("Found Bag Good", bagGood);
	}

	public void testGoodsListContainsPot() {
		Good potGood = GoodsUtil.getBinGood(BinType.POT);
		assertNotNull("Found Pot Good", potGood);
	}
	
	public void testGoodsListContainsExplorerRover() {
		// "Explorer Rover" is a valid vehicle type
        String typeName = "Explorer Rover";
		Good explorerRoverGood = GoodsUtil.getVehicleGood(typeName);
		assertNotNull("Found good vehicleType " +  typeName, explorerRoverGood);
	}

	public void testGoodsListDoesntContainFalseRover() {
		// "False Rover" is not a valid vehicle type
		Good falseRoverGood = GoodsUtil.getVehicleGood("False Rover");
		assertNull("Non-Existent Vehicle Good not found", falseRoverGood);
	}
}