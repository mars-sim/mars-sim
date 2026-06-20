package com.mars_sim.core.person.ai.task;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.LifeSupportInterface;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ThirstLevel;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ResourceUtil;

class EatDrinkTest extends MarsSimUnitTest {
    
    private static final double INITIAL_RESOURCE = 100D;

    protected Building buildDining(BuildingManager buildingManager, LocalPosition pos, double facing) {
        return buildFunction(buildingManager, "Lander Hab", BuildingCategory.LIVING,
                                FunctionType.DINING,  pos, facing, true);
    }

    @Test
    void testSettlementWater() {
        var s = buildSettlement("mock");
        var d = buildDining(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var p = buildPerson("eater", s, JobType.ENGINEER, d, FunctionType.DINING);
        s.storeAmountResource(ResourceUtil.WATER_ID, INITIAL_RESOURCE);

        testWater(p, s);
        assertTrue(s.getSpecificAmountResourceStored(ResourceUtil.WATER_ID) < INITIAL_RESOURCE, "Water consumed");
    }

    @Test
    void testBottleWater() {
        var s = buildSettlement("mock");
        var d = buildDining(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var p = buildPerson("eater", s, JobType.ENGINEER, d, FunctionType.DINING);

        // Create bottle and assign to person
        var b = EquipmentFactory.createEquipment(EquipmentType.THERMAL_BOTTLE, s);
        b.storeAmountResource(ResourceUtil.WATER_ID, INITIAL_RESOURCE);
        p.assignThermalBottle();

        testWater(p, s);
        assertTrue(b.getSpecificAmountResourceStored(ResourceUtil.WATER_ID) < INITIAL_RESOURCE, "Water consumed");
    }

    @Test
    void testVehicleWater() {
        var s = buildSettlement("mock");
        var p = buildPerson("eater", s);
        var v = buildRover(s, "R1", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        v.storeAmountResource(ResourceUtil.WATER_ID, INITIAL_RESOURCE);

        p.transfer(v);
        assertTrue(p.isInVehicle(), "In vehicle");

        testWater(p, v);
        assertTrue(v.getSpecificAmountResourceStored(ResourceUtil.WATER_ID) < INITIAL_RESOURCE, "Water consumed");

    }

    private void testWater(Person p, LifeSupportInterface support) {
        var pc = p.getPhysicalCondition();
        pc.setThirst(ThirstLevel.DRY.getMaxValue());
        var initialThirst = pc.getThirst();
        assertTrue(initialThirst > 0, "Person is thirty");

        // Move advance to update thirst level and ensure that the person is thirsty
        pc.timePassing(createPulse(10), support);

        var t = new EatDrink(p);
        assertFalse(t.isDone(), "EatDrink task not complete");
      
        executeTask(p, t, 150);
        assertTrue(t.isDone(), "Eatdrnk completed");
        assertTrue(pc.getThirst() < initialThirst, "Person is less thirty");
    }

    @Test
    void testSettlementFood() {
        var s = buildSettlement("mock");
        var d = buildDining(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var p = buildPerson("eater", s, JobType.ENGINEER, d, FunctionType.DINING);

        testHunger(p, s);
    }

    @Test
    void testSettlementFoodWater() {
        var s = buildSettlement("mock");
        var d = buildDining(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var p = buildPerson("eater", s, JobType.ENGINEER, d, FunctionType.DINING);

        var pc = p.getPhysicalCondition();
        pc.setThirst(PhysicalCondition.MAX_THIRST);
        var initialThirst = pc.getThirst();

        testHunger(p, s);

        assertTrue(pc.getThirst() < initialThirst, "Person is less thirsty");

    }

    @Test
    void testVehicleFood() {
        var s = buildSettlement("mock");
        var p = buildPerson("eater", s);
        var v = buildRover(s, "R1", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);

        p.transfer(v);
        assertTrue(p.isInVehicle(), "In vehicle");

        testHunger(p, v);
    }

    private void testHunger(Person p, ResourceHolder rh) {
        rh.storeAmountResource(ResourceUtil.FOOD_ID, INITIAL_RESOURCE);

        var pc = p.getPhysicalCondition();
        pc.setHunger(PhysicalCondition.HUNGER_THRESHOLD + 1);
        pc.setThirst(0D);
        assertTrue(pc.isHungry(), "Person is hungry");

        var t = new EatDrink(p);
        assertFalse(t.isDone(), "EatDrink task not complete");

        executeTask(p, t, 1000);
        assertTrue(t.isDone(), "Eatdrnk completed");
        assertFalse(pc.isHungry(), "Person is not hungry");
        assertTrue(rh.getSpecificAmountResourceStored(ResourceUtil.FOOD_ID) < INITIAL_RESOURCE, "Food consumed");
    }
}