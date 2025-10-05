package com.mars_sim.core.mission.objectives;

import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

public class TradeObjectiveTest extends AbstractMarsSimUnitTest {

    
    public void testAddEquipmentToLoad() {
        Good cement = GoodsUtil.getGood(ResourceUtil.CEMENT_ID);
        Good bag = GoodsUtil.getEquipmentGood(EquipmentType.BAG);
        Good barrel = GoodsUtil.getEquipmentGood(EquipmentType.BARREL);
        Good barrow = GoodsUtil.getEquipmentGood(EquipmentType.WHEELBARROW);
        Good box = GoodsUtil.getEquipmentGood(EquipmentType.SPECIMEN_BOX);

        var s = buildSettlement("test", true);

        // Add in Resource that will be ignored
        var buy = Map.of(barrel, 10, bag, 10);
        var sell = Map.of(box, 10, cement, 10, barrow, 10);
        var obj = new TradeObjective(s, buy, sell, 20D);

        assertEquals("Desired Buy load", buy, obj.getDesiredBuy());
        assertEquals("Sell load", sell, obj.getSell());
        assertTrue("Bought load", obj.getBought().isEmpty());

        // Check bought
        obj.updateBought(buy, 20);
        assertEquals("Bought load", buy, obj.getBought());
        assertEquals("Profit", 20D, obj.getProfit());

        Map<Integer,Integer> buyResources = new HashMap<>();
        obj.addEquipmentToLoad(buyResources, false);

        assertEquals("Buy resources", 2, buyResources.size());
        assertEquals("Bag amount", 10, buyResources.get(bag.getID()).intValue());
        assertEquals("Barrel amount", 10, buyResources.get(barrel.getID()).intValue());

        Map<Integer,Integer> sellResources = new HashMap<>();
        obj.addEquipmentToLoad(sellResources, true);

        assertEquals("Sell resources", 2, sellResources.size());
        assertEquals("Box amount", 10, sellResources.get(box.getID()).intValue());
        assertEquals("Barrow amount", 10, sellResources.get(barrow.getID()).intValue());
    }

     public void testAddEquipmentMerge() {
        Good bag = GoodsUtil.getEquipmentGood(EquipmentType.BAG);
        Good barrel = GoodsUtil.getEquipmentGood(EquipmentType.BARREL);
        Good barrow = GoodsUtil.getEquipmentGood(EquipmentType.WHEELBARROW);
        Good box = GoodsUtil.getEquipmentGood(EquipmentType.SPECIMEN_BOX);
        Good bottle = GoodsUtil.getEquipmentGood(EquipmentType.THERMAL_BOTTLE);


        var s = buildSettlement("test", true);

        // Add in Resource that will be ignored
        var buy = Map.of(barrel, 10, bag, 10);
        var sell = Map.of(box, 10, barrow, 10);
        var obj = new TradeObjective(s, buy, sell, 20D);

        assertEquals("Desired Buy load", buy, obj.getDesiredBuy());
        assertEquals("Sell load", sell, obj.getSell());
        assertTrue("Bought load", obj.getBought().isEmpty());

        // Check bought
        obj.updateBought(buy, 20);

        Map<Integer,Integer> buyResources = new HashMap<>(Map.of(bag.getID(),10, bottle.getID(), 5));
        obj.addEquipmentToLoad(buyResources, false);

        assertEquals("Buy resources", 3, buyResources.size());
        assertEquals("Bag amount", 20, buyResources.get(bag.getID()).intValue());
        assertEquals("Barrel amount", 10, buyResources.get(barrel.getID()).intValue());
        assertEquals("Bottle amount", 5, buyResources.get(bottle.getID()).intValue());

        Map<Integer,Integer> sellResources = new HashMap<>(Map.of(box.getID(),10, bottle.getID(), 5));
        obj.addEquipmentToLoad(sellResources, true);

        assertEquals("Sell resources", 3, sellResources.size());
        assertEquals("Box amount", 20, sellResources.get(box.getID()).intValue());
        assertEquals("Barrow amount", 10, sellResources.get(barrow.getID()).intValue());
        assertEquals("Bottle amount", 5, buyResources.get(bottle.getID()).intValue());
    }

    public void testAddResourcesToLoad() {
        Good cement = GoodsUtil.getGood(ResourceUtil.CEMENT_ID);
        Good water = GoodsUtil.getGood(ResourceUtil.WATER_ID);
        Good oxygen = GoodsUtil.getGood(ResourceUtil.OXYGEN_ID);
        Good garment = GoodsUtil.getGood(ItemResourceUtil.GARMENT_ID);
        Good bag = GoodsUtil.getEquipmentGood(EquipmentType.BAG);


        var s = buildSettlement("test", true);

        // Add in Parts that will be ignored
        var buy = Map.of(oxygen, 10, garment, 10);
        var sell = Map.of(water, 10, cement, 10, bag, 10);
        var obj = new TradeObjective(s, buy, sell, 20D);

        assertEquals("Desired Buy load", buy, obj.getDesiredBuy());
        assertEquals("Sell load", sell, obj.getSell());
        assertTrue("Bought load", obj.getBought().isEmpty());

        // Check bought
        obj.updateBought(buy, 20);
        assertEquals("Bought load", buy, obj.getBought());

        Map<Integer,Number> buyResources = new HashMap<>();
        obj.addResourcesToLoad(buyResources, false);

        assertEquals("Buy resources", 2, buyResources.size());
        assertEquals("Oxygen amount", 10, buyResources.get(ResourceUtil.OXYGEN_ID).intValue());
        assertEquals("Garment amount", 10, buyResources.get(ItemResourceUtil.GARMENT_ID).intValue());

        Map<Integer,Number> sellResources = new HashMap<>();
        obj.addResourcesToLoad(sellResources, true);

        assertEquals("Sell resources", 2, sellResources.size());
        assertEquals("Cemment amount", 10, sellResources.get(ResourceUtil.CEMENT_ID).intValue());
        assertEquals("Water amount", 10, sellResources.get(ResourceUtil.WATER_ID).intValue());
    }

    
    public void testLoadResourcesMerged() {
        Good cement = GoodsUtil.getGood(ResourceUtil.CEMENT_ID);
        Good water = GoodsUtil.getGood(ResourceUtil.WATER_ID);
        Good oxygen = GoodsUtil.getGood(ResourceUtil.OXYGEN_ID);
        Good food = GoodsUtil.getGood(ResourceUtil.FOOD_ID);

        var s = buildSettlement("test", true);

        var buy = Map.of(oxygen, 10, food, 10);
        var sell = Map.of(water, 10, cement, 10);
        var obj = new TradeObjective(s, buy, sell, 0D);
        obj.updateBought(buy, 20);

        Map<Integer,Number> buyResources = new HashMap<>(Map.of(ResourceUtil.HYDROGEN_ID, 10, ResourceUtil.FOOD_ID, 10));
        obj.addResourcesToLoad(buyResources, false);

        assertEquals("Buy resources", 3, buyResources.size());
        assertEquals("Oxygen amount", 10, buyResources.get(ResourceUtil.OXYGEN_ID).intValue());
        assertEquals("Food amount", 20, buyResources.get(ResourceUtil.FOOD_ID).intValue());
        assertEquals("Hydrogen amount", 10, buyResources.get(ResourceUtil.HYDROGEN_ID).intValue());

        Map<Integer,Number> sellResources = new HashMap<>(Map.of(ResourceUtil.WATER_ID, 10, ResourceUtil.CONCRETE_ID, 10));
        obj.addResourcesToLoad(sellResources, true);

        assertEquals("Sell resources", 3, sellResources.size());
        assertEquals("Cement amount", 10, sellResources.get(ResourceUtil.CEMENT_ID).intValue());
        assertEquals("Water amount", 20, sellResources.get(ResourceUtil.WATER_ID).intValue());
        assertEquals("Concrete amount", 10, sellResources.get(ResourceUtil.CONCRETE_ID).intValue());
    }
}
