package org.mars_sim.msp.simulation.structure.goods;

import java.util.List;

import junit.framework.TestCase;
import org.mars_sim.msp.simulation.equipment.Bag;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;

public class TestGoods extends TestCase {

	public TestGoods() {
		super();
	}
	
	public void testGoodsListNotNull() {
		List goodsList = GoodsUtil.getGoodsList();
		assertTrue("Goods list not null", (goodsList != null));
	}
	
	public void testGoodsListNotZero() {
		List goodsList = GoodsUtil.getGoodsList();
		assertTrue("Goods list not zero", (goodsList.size() > 0));
	}
	
	public void testGoodsListContainsWater() throws Exception {
		AmountResource water = AmountResource.findAmountResource("water");
		Good waterGood = GoodsUtil.getResourceGood(water);
		assertTrue("Goods list contains water", GoodsUtil.containsGood(waterGood));
	}
	
	public void testGoodsListContainsHammer() {
		ItemResource hammer = ItemResource.getTestResourceHammer();
		Good hammerGood = GoodsUtil.getResourceGood(hammer);
		assertTrue("Goods list contains hammer", GoodsUtil.containsGood(hammerGood));
	}
	
	public void testGoodsListContainsBag() {
		Good bagGood = GoodsUtil.getEquipmentGood(Bag.class);
		assertTrue("Goods list contains bag", GoodsUtil.containsGood(bagGood));
	}
	
	public void testGoodsListContainsExplorerRover() {
		Good explorerRoverGood = GoodsUtil.getVehicleGood("Transport Rover");
		assertTrue("Goods list contains explorer rover", GoodsUtil.containsGood(explorerRoverGood));
	}
	
	public void testGoodsListDoesntContainFalseRover() {
		Good falseRoverGood = GoodsUtil.getVehicleGood("False Rover");
		assertTrue("Goods list doesn't contain false rover", !GoodsUtil.containsGood(falseRoverGood));
	}
}