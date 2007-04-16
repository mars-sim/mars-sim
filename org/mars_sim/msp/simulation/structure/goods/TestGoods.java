package org.mars_sim.msp.simulation.structure.goods;

import java.util.Set;

import junit.framework.TestCase;
import org.mars_sim.msp.simulation.equipment.Bag;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;

public class TestGoods extends TestCase {

	public TestGoods() {
		super();
	}
	
	public void testGoodsListNotNull() {
		Set goodsList = GoodsUtil.getGoodsSet();
		assertTrue("Goods list not null", (goodsList != null));
	}
	
	public void testGoodsListNotZero() {
		Set goodsList = GoodsUtil.getGoodsSet();
		assertTrue("Goods list not zero", (goodsList.size() > 0));
	}
	
	public void testGoodsListContainsWater() {
		Set goodsList = GoodsUtil.getGoodsSet();
		Good waterGood = GoodsUtil.getResourceGood(AmountResource.WATER);
		assertTrue("Goods list contains water", goodsList.contains(waterGood));
	}
	
	public void testGoodsListContainsHammer() {
		Set goodsList = GoodsUtil.getGoodsSet();
		Good hammerGood = GoodsUtil.getResourceGood(ItemResource.HAMMER);
		assertTrue("Goods list contains hammer", goodsList.contains(hammerGood));
	}
	
	public void testGoodsListContainsBag() {
		Set goodsList = GoodsUtil.getGoodsSet();
		Good bagGood = GoodsUtil.getEquipmentGood(Bag.class);
		assertTrue("Goods list contains bag", goodsList.contains(bagGood));
	}
	
	public void testGoodsListContainsExplorerRover() {
		Set goodsList = GoodsUtil.getGoodsSet();
		Good explorerRoverGood = GoodsUtil.getVehicleGood("Transport Rover");
		assertTrue("Goods list contains explorer rover", goodsList.contains(explorerRoverGood));
	}
}