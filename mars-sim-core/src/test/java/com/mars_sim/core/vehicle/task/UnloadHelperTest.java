package com.mars_sim.core.vehicle.task;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;


import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

public class UnloadHelperTest extends MarsSimUnitTest{
    @Test
    public void testReleaseTowedVehicle() {
        var s = buildSettlement("towing");

        var towing = buildRover(s, "towing", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var towed = buildRover(s, "towed", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        towing.setTowedVehicle(towed);

        UnloadHelper.releaseTowedVehicle(towing, s);
        assertNull(towing.getTowedVehicle(), "Towed vehicle");
        assertNull(towed.getTowingVehicle(), "Towing vehicle");
        assertTrue(s.getParkedGaragedVehicles().contains(towed), "Parked");
    }

    @Test
    public void testUnloadDeceased() {
        var s = buildSettlement("towing");

        var v = buildRover(s, "towing", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var p1 = buildPerson("Alive", s);
        p1.transfer(v);
        var p2 = buildPerson("Dead", s);
        p2.transfer(v);
        p2.getPhysicalCondition().recordDead(new HealthProblem(ComplaintType.BURNS, p2), true, "Dead");
        assertEquals(2, v.getCrewNum(), "Inital size of crew");

        UnloadHelper.unloadDeceased(v, s);
        assertEquals(1, v.getCrewNum(), "Final size of crew");
        assertEquals(1, s.getIndoorPeopleCount(), "Settlement people");
    }

    @Test
    public void testUnloadEVASuits() {
        var s = buildSettlement("towing");

        var v = buildRover(s, "rover", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        int suits = 3;
        for(int i = 0; i < suits; i++) {
            var e = EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, s);
            e.storeAmountResource(ResourceUtil.OXYGEN_ID, 10);
            e.transfer(v);
        }

        var inv = v.getEquipmentInventory();
        assertEquals(suits, inv.getSuitSet().size(), "EVASuits loaded");

        UnloadHelper.unloadEVASuits(v, s, 1000D, 1);
        assertEquals(1, inv.getSuitSet().size(), "EVASuits left");
        assertEquals(suits - 1, s.getEquipmentInventory().getSuitSet().size(), "EVASuits in settlement");
        assertGreaterThan("Settlement oxygen", 0D, s.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID));
    }

    @Test
    public void testUnloadInventory() {
        var s = buildSettlement("towing");
        var v = buildRover(s, "rover", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        s.getEquipmentInventory().setCargoCapacity(50);
        v.getEquipmentInventory().setCargoCapacity(60);
        
        // Load the vehicle
        int res1 = ResourceUtil.OXYGEN_ID;
        v.storeAmountResource(res1, 10D);
        int res2 = ResourceUtil.FOOD_ID;
        v.storeAmountResource(res2, 10D);
        int part1 = ItemResourceUtil.GARMENT_ID;
        v.storeItemResource(part1, 10);

        double mass = v.getStoredMass();
//        System.out.println("mass: " + mass);
        
        assertGreaterThan("Initial stored mass", 0D, mass);

        double amountNotUsed = UnloadHelper.unloadInventory(v, s, mass);
//        System.out.println("amountNotUsed: " + amountNotUsed);
        assertEquals(0D, amountNotUsed, "All efforts being used up");
        
        mass = v.getStoredMass();
//        System.out.println("mass: " + mass);
        v.getEquipmentInventory().printMicroInventoryStoredMass();
        
        assertEquals(0D, mass, "Final stored mass");
        
        assertEquals(10D, s.getSpecificAmountResourceStored(res1), "Settlement res1");
        assertEquals(10D, s.getSpecificAmountResourceStored(res2), "Settlement res2");
        assertEquals(10, s.getItemResourceStored(part1), "Settlement part1");

    }
}
