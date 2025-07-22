package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
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
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ResourceUtil;

public class EatDrinkTest extends AbstractMarsSimUnitTest {
    
    private static final double INITIAL_RESOURCE = 100D;

    protected Building buildDining(BuildingManager buildingManager, LocalPosition pos, double facing) {
        return buildFunction(buildingManager, "Lander Hab", BuildingCategory.LIVING,
                                FunctionType.DINING,  pos, facing, true);
    }

    public void testSettlementWater() {
        var s = buildSettlement();
        var d = buildDining(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var p = buildPerson("eater", s, JobType.ENGINEER, d, FunctionType.DINING);
        s.storeAmountResource(ResourceUtil.WATER_ID, INITIAL_RESOURCE);

        testWater(p);
        assertTrue("Water consumed", s.getSpecificAmountResourceStored(ResourceUtil.WATER_ID) < INITIAL_RESOURCE);
    }

    public void testBottleWater() {
        var s = buildSettlement();
        var d = buildDining(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var p = buildPerson("eater", s, JobType.ENGINEER, d, FunctionType.DINING);

        // Create bottle and assign to person
        var b = EquipmentFactory.createEquipment(EquipmentType.THERMAL_BOTTLE, s);
        b.storeAmountResource(ResourceUtil.WATER_ID, INITIAL_RESOURCE);
        p.assignThermalBottle();

        testWater(p);
        assertTrue("Water consumed", b.getSpecificAmountResourceStored(ResourceUtil.WATER_ID) < INITIAL_RESOURCE);
    }

    public void testVehicleWater() {
        var s = buildSettlement();
        var p = buildPerson("eater", s);
        var v = buildRover(s, "R1", LocalPosition.DEFAULT_POSITION);
        v.storeAmountResource(ResourceUtil.WATER_ID, INITIAL_RESOURCE);

        p.transfer(v);
        assertTrue("In vehicle", p.isInVehicle());

        testWater(p);
        assertTrue("Water consumed", v.getSpecificAmountResourceStored(ResourceUtil.WATER_ID) < INITIAL_RESOURCE);

    }

    private void testWater(Person p) {
        var pc = p.getPhysicalCondition();
        pc.setThirst(PhysicalCondition.MAX_THIRST);
        var initialThirst = pc.getThirst();
        assertTrue("Person is thirty", initialThirst > 0);

        var t = new EatDrink(p);
        assertFalse("EatDrink task not complete", t.isDone());

        executeTask(p, t, 100);
        assertTrue("Eatdrnk completed", t.isDone());
        assertTrue("Person is less thirty", pc.getThirst() < initialThirst);
    }

    public void testSettlementFood() {
        var s = buildSettlement();
        var d = buildDining(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var p = buildPerson("eater", s, JobType.ENGINEER, d, FunctionType.DINING);

        testHunger(p, s);
    }

    public void testSettlementFoodWater() {
        var s = buildSettlement();
        var d = buildDining(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var p = buildPerson("eater", s, JobType.ENGINEER, d, FunctionType.DINING);

        var pc = p.getPhysicalCondition();
        pc.setThirst(PhysicalCondition.MAX_THIRST);
        var initialThirst = pc.getThirst();

        testHunger(p, s);

        assertTrue("Person is less thirsty", pc.getThirst() < initialThirst);

    }

    public void testVehicleFood() {
        var s = buildSettlement();
        var p = buildPerson("eater", s);
        var v = buildRover(s, "R1", LocalPosition.DEFAULT_POSITION);

        p.transfer(v);
        assertTrue("In vehicle", p.isInVehicle());

        testHunger(p, v);
    }

    private void testHunger(Person p, ResourceHolder rh) {
        rh.storeAmountResource(ResourceUtil.FOOD_ID, INITIAL_RESOURCE);

        var pc = p.getPhysicalCondition();
        pc.setHunger(PhysicalCondition.HUNGER_THRESHOLD + 1);
        pc.setThirst(0D);
        assertTrue("Person is hungry", pc.isHungry());

        var t = new EatDrink(p);
        assertFalse("EatDrink task not complete", t.isDone());

        executeTask(p, t, 1000);
        assertTrue("Eatdrnk completed", t.isDone());
        assertFalse("Person is not hungry", pc.isHungry());
        assertTrue("Food consumed", rh.getSpecificAmountResourceStored(ResourceUtil.FOOD_ID) < INITIAL_RESOURCE);
    }
}
