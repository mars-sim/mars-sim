package org.mars_sim.msp.core.structure.goods;

import java.util.List;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.goods.Good;
import org.mars_sim.msp.core.goods.GoodType;
import org.mars_sim.msp.core.goods.GoodsUtil;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;

import junit.framework.TestCase;

public class TestGoods extends TestCase {

    private List<Good> goodsList;
    ItemResource hammer;
    
    public TestGoods() {
		super();
	}

    protected void setUp() throws Exception {
        SimulationConfig.instance().loadConfig();
        ResourceUtil.getInstance();        
        GoodType type = GoodType.TOOL;
        hammer = ItemResourceUtil.createItemResource("hammer", 1100, "a hand tool", type, 1.4D, 1);
        goodsList = GoodsUtil.getGoodsList();
    }

    public void testCreateItem() {
    	GoodType type = GoodType.INSTRUMENT;
    	Part microlens = ItemResourceUtil.createItemResource("microlens", 1102, "a test lense", type, 0.05D, 1);
    	assertNotNull(microlens);
    }

    public void testGoodsListNotNull() {
        assertNotNull(goodsList);
	}

	public void testGoodsListNotZero() {
		assertTrue(goodsList.size() > 0);
	}

	public void testGoodsListContainsWater() throws Exception {
		AmountResource water = ResourceUtil.findAmountResource(ResourceUtil.WATER);
		Good waterGood = GoodsUtil.getResourceGood(water);
		assertTrue(GoodsUtil.containsGood(waterGood));
	}

	public void testGoodsListContainsHammer() {
        Good hammerGood = GoodsUtil.createResourceGood(hammer);
        // hammer is not a standardized part and is NOT registered on the goodsMap
        assertFalse(GoodsUtil.containsGood(hammerGood));
	}

//	public void testGoodsListContainsBag() {
//		Good bagGood = GoodsUtil.getEquipmentGood(Bag.class);
//		assertTrue(GoodsUtil.containsGood(bagGood));
//	}
//
//	public void testGoodsListContainsExplorerRover() {
//		// "Explorer Rover" is a valid vehicle type
//		Good explorerRoverGood = GoodsUtil.getVehicleGood("Explorer Rover");
//		assertTrue(GoodsUtil.containsGood(explorerRoverGood));
//	}

//	public void testGoodsListDoesntContainFalseRover() {
//		// "False Rover" is not a valid vehicle type
//		Good falseRoverGood = GoodsUtil.getVehicleGood("False Rover");
////        if (falseRoverGood == null)
////        	System.out.println("falseRoverGood is null in TestGoods");
//		assertTrue(!GoodsUtil.containsGood(falseRoverGood));
//	}
}