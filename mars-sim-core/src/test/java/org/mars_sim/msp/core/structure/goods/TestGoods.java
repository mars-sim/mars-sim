package org.mars_sim.msp.core.structure.goods;

import java.util.List;

import junit.framework.TestCase;

import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;

public class TestGoods extends TestCase {

    private List<Good> goodsList;
    private ItemResource hammer;

    public TestGoods() {
		super();
	}

    protected void setUp() throws Exception {
        SimulationConfig.loadConfig();
        hammer = ItemResource.createItemResource("hammer", 1, "a tool", 1.4D);
        goodsList = GoodsUtil.getGoodsList();
    }

    public void testGoodsListNotNull() {
        assertNotNull(goodsList);
	}
	
	public void testGoodsListNotZero() {
		assertTrue(goodsList.size() > 0);
	}
	
	public void testGoodsListContainsWater() throws Exception {
		AmountResource water = AmountResource.findAmountResource(LifeSupportType.WATER);
		Good waterGood = GoodsUtil.getResourceGood(water);
		assertTrue( GoodsUtil.containsGood(waterGood));
	}
	
	public void testGoodsListContainsHammer() {
        Good hammerGood = GoodsUtil.getResourceGood(hammer);
		assertFalse(GoodsUtil.containsGood(hammerGood));
	}
	
	public void testGoodsListContainsBag() {
		Good bagGood = GoodsUtil.getEquipmentGood(Bag.class);
		assertTrue(GoodsUtil.containsGood(bagGood));
	}
	
	public void testGoodsListContainsExplorerRover() {
		Good explorerRoverGood = GoodsUtil.getVehicleGood("Transport Rover");
		assertTrue( GoodsUtil.containsGood(explorerRoverGood));
	}
	
	public void testGoodsListDoesntContainFalseRover() {
		Good falseRoverGood = GoodsUtil.getVehicleGood("False Rover");
		assertTrue( !GoodsUtil.containsGood(falseRoverGood));
	}
}