package com.mars_sim.core.mission.objectives;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

public class TradeObjectiveTest extends MarsSimUnitTest {

    
    @Test
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

        assertEquals(buy, obj.getDesiredBuy(), "Desired Buy load");
        assertEquals(sell, obj.getSell(), "Sell load");
        assertTrue(obj.getBought().isEmpty(), "Bought load");

        // Check bought
        obj.updateBought(buy, 20);
        assertEquals(buy, obj.getBought(), "Bought load");
        assertEquals(20D, obj.getProfit(), "Profit");

        Map<Integer,Integer> buyResources = new HashMap<>();
        obj.addEquipmentToLoad(buyResources, false);

        assertEquals(2, buyResources.size(), "Buy resources");
        assertEquals(10, buyResources.get(bag.getID()).intValue(), "Bag amount");
        assertEquals(10, buyResources.get(barrel.getID()).intValue(), "Barrel amount");

        Map<Integer,Integer> sellResources = new HashMap<>();
        obj.addEquipmentToLoad(sellResources, true);

        assertEquals(2, sellResources.size(), "Sell resources");
        assertEquals(10, sellResources.get(box.getID()).intValue(), "Box amount");
        assertEquals(10, sellResources.get(barrow.getID()).intValue(), "Barrow amount");
    }

     @Test
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

        assertEquals(buy, obj.getDesiredBuy(), "Desired Buy load");
        assertEquals(sell, obj.getSell(), "Sell load");
        assertTrue(obj.getBought().isEmpty(), "Bought load");

        // Check bought
        obj.updateBought(buy, 20);

        Map<Integer,Integer> buyResources = new HashMap<>(Map.of(bag.getID(),10, bottle.getID(), 5));
        obj.addEquipmentToLoad(buyResources, false);

        assertEquals(3, buyResources.size(), "Buy resources");
        assertEquals(20, buyResources.get(bag.getID()).intValue(), "Bag amount");
        assertEquals(10, buyResources.get(barrel.getID()).intValue(), "Barrel amount");
        assertEquals(5, buyResources.get(bottle.getID()).intValue(), "Bottle amount");

        Map<Integer,Integer> sellResources = new HashMap<>(Map.of(box.getID(),10, bottle.getID(), 5));
        obj.addEquipmentToLoad(sellResources, true);

        assertEquals(3, sellResources.size(), "Sell resources");
        assertEquals(20, sellResources.get(box.getID()).intValue(), "Box amount");
        assertEquals(10, sellResources.get(barrow.getID()).intValue(), "Barrow amount");
        assertEquals(5, buyResources.get(bottle.getID()).intValue(), "Bottle amount");
    }

    @Test
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

        assertEquals(buy, obj.getDesiredBuy(), "Desired Buy load");
        assertEquals(sell, obj.getSell(), "Sell load");
        assertTrue(obj.getBought().isEmpty(), "Bought load");

        // Check bought
        obj.updateBought(buy, 20);
        assertEquals(buy, obj.getBought(), "Bought load");

        Map<Integer,Number> buyResources = new HashMap<>();
        obj.addResourcesToLoad(buyResources, false);

        assertEquals(2, buyResources.size(), "Buy resources");
        assertEquals(10, buyResources.get(ResourceUtil.OXYGEN_ID).intValue(), "Oxygen amount");
        assertEquals(10, buyResources.get(ItemResourceUtil.GARMENT_ID).intValue(), "Garment amount");

        Map<Integer,Number> sellResources = new HashMap<>();
        obj.addResourcesToLoad(sellResources, true);

        assertEquals(2, sellResources.size(), "Sell resources");
        assertEquals(10, sellResources.get(ResourceUtil.CEMENT_ID).intValue(), "Cemment amount");
        assertEquals(10, sellResources.get(ResourceUtil.WATER_ID).intValue(), "Water amount");
    }

    
    @Test
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

        assertEquals(3, buyResources.size(), "Buy resources");
        assertEquals(10, buyResources.get(ResourceUtil.OXYGEN_ID).intValue(), "Oxygen amount");
        assertEquals(20, buyResources.get(ResourceUtil.FOOD_ID).intValue(), "Food amount");
        assertEquals(10, buyResources.get(ResourceUtil.HYDROGEN_ID).intValue(), "Hydrogen amount");

        Map<Integer,Number> sellResources = new HashMap<>(Map.of(ResourceUtil.WATER_ID, 10, ResourceUtil.CONCRETE_ID, 10));
        obj.addResourcesToLoad(sellResources, true);

        assertEquals(3, sellResources.size(), "Sell resources");
        assertEquals(10, sellResources.get(ResourceUtil.CEMENT_ID).intValue(), "Cement amount");
        assertEquals(20, sellResources.get(ResourceUtil.WATER_ID).intValue(), "Water amount");
        assertEquals(10, sellResources.get(ResourceUtil.CONCRETE_ID).intValue(), "Concrete amount");
    }
}
