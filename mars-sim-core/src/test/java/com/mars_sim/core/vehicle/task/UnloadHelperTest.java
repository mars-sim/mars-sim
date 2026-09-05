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
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalManager;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

class UnloadHelperTest extends MarsSimUnitTest{
    @Test
    void testReleaseTowedVehicle() {
        var s = buildSettlement("towing");

        var towing = buildRover(s, "towing", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var towed = buildRover(s, "towed", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        towing.setTowedVehicle(towed);

        UnloadHelper.releaseTowedVehicle(towing, s);
        assertNull(towing.getTowedVehicle(), "Towed vehicle");
        assertNull(towed.getTowingVehicle(), "Towing vehicle");
        assertTrue(s.getParkedNGaragedVehicles().contains(towed), "Parked");
    }

    @Test
    void testUnloadDeceased() {
        var s = buildSettlement("towing");

        var v = buildRover(s, "towing", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var p1 = buildPerson("Alive", s);
        p1.transfer(v);
        var p2 = buildPerson("Dead", s);
        p2.transfer(v);
        var burns = getContext().getSim().getMedicalManager().getComplaintByName("BURNS");
        p2.getPhysicalCondition().recordDead(new HealthProblem(burns, p2), true, "Dead");
        assertEquals(2, v.getCrewNum(), "Inital size of crew");

        UnloadHelper.unloadDeceased(v, s);
        assertEquals(1, v.getCrewNum(), "Final size of crew");
        assertEquals(1, s.getIndoorPeopleCount(), "Settlement people");
    }

    @Test
    void testUnloadEVASuits() {
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

        var rh = s.getEquipmentInventory();
        UnloadHelper.unloadEVASuits(v, s, 1000D, 1);
        assertEquals(1, inv.getSuitSet().size(), "EVASuits left");
        assertEquals(suits - 1, rh.getSuitSet().size(), "EVASuits in settlement");
        assertGreaterThan("Settlement oxygen", 0D, rh.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID));
    }

    @Test
    void testUnloadInventory() {
        var s = buildSettlement("towing");
        var v = buildRover(s, "rover", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);

        var srh = s.getEquipmentInventory();
        srh.setCargoCapacity(50);

        var vehEO = v.getEquipmentInventory();
        vehEO.setCargoCapacity(60);
        
        // Load the vehicle
        int res1 = ResourceUtil.OXYGEN_ID;
        vehEO.storeAmountResource(res1, 10D);
        int res2 = ResourceUtil.FOOD_ID;
        vehEO.storeAmountResource(res2, 10D);
        int part1 = ItemResourceUtil.GARMENT_ID;
        vehEO.storeItemResource(part1, 10);

        double mass = vehEO.getStoredMass();
        
        assertGreaterThan("Initial stored mass", 0D, mass);

        double amountNotUsed = UnloadHelper.unloadInventory(v, s, mass);
        assertEquals(0D, amountNotUsed, "All efforts being used up");
        
        mass = vehEO.getStoredMass();
        
        assertEquals(0D, mass, "Final stored mass");
        
        assertEquals(10D, srh.getSpecificAmountResourceStored(res1), "Settlement res1");
        assertEquals(10D, srh.getSpecificAmountResourceStored(res2), "Settlement res2");
        assertEquals(10, srh.getItemResourceStored(part1), "Settlement part1");

    }
}
