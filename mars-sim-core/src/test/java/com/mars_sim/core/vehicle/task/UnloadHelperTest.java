package com.mars_sim.core.vehicle.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.mapdata.location.LocalPosition;

public class UnloadHelperTest extends AbstractMarsSimUnitTest{
    public void testReleaseTowedVehicle() {
        var s = buildSettlement("towing");

        var towing = buildRover(s, "towing", LocalPosition.DEFAULT_POSITION);
        var towed = buildRover(s, "towed", LocalPosition.DEFAULT_POSITION);
        towing.setTowedVehicle(towed);

        UnloadHelper.releaseTowedVehicle(towing, s);
        assertNull("Towed vehicle", towing.getTowedVehicle());
        assertNull("Towing vehicle", towed.getTowingVehicle());
        assertTrue("Parked", s.getParkedVehicles().contains(towed));
    }

    public void testUnloadDeceased() {
        var s = buildSettlement("towing");

        var v = buildRover(s, "towing", LocalPosition.DEFAULT_POSITION);
        var p1 = buildPerson("Alive", s);
        p1.transfer(v);
        var p2 = buildPerson("Dead", s);
        p2.transfer(v);
        p2.getPhysicalCondition().recordDead(new HealthProblem(ComplaintType.BURNS, p2), true, "Dead");
        assertEquals("Inital size of crew", 2, v.getCrewNum());

        UnloadHelper.unloadDeceased(v, s);
        assertEquals("Final size of crew", 1, v.getCrewNum());
        assertEquals("Settlement people", 1, s.getIndoorPeopleCount());
    }

    public void testUnloadEVASuits() {
        var s = buildSettlement("towing");

        var v = buildRover(s, "rover", LocalPosition.DEFAULT_POSITION);
        int suits = 3;
        for(int i = 0; i < suits; i++) {
            var e = EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, s);
            e.storeAmountResource(ResourceUtil.oxygenID, 10);
            e.transfer(v);
        }

        var inv = v.getEquipmentInventory();
        assertEquals("EVASuits loaded", suits, inv.getSuitSet().size());

        UnloadHelper.unloadEVASuits(v, s, 1000D, 1);
        assertEquals("EVASuits left", 1, inv.getSuitSet().size());
        assertEquals("EVASuits in settlement", suits - 1, s.getEquipmentInventory().getSuitSet().size());
        assertGreaterThan("Settlement oxygen", 0D, s.getAmountResourceStored(ResourceUtil.oxygenID));
    }

    public void testUnloadInventory() {
        var s = buildSettlement("towing");
        var v = buildRover(s, "rover", LocalPosition.DEFAULT_POSITION);

        // Load the vehicle
        int res1 = ResourceUtil.oxygenID;
        v.storeAmountResource(res1, 10D);
        int res2 = ResourceUtil.foodID;
        v.storeAmountResource(res2, 10D);
        int part1 = ItemResourceUtil.garmentID;
        v.storeItemResource(part1, 10);

        assertGreaterThan("Initial stored mass", 0D, v.getStoredMass());

        UnloadHelper.unloadInventory(v, s, 1000D);

        assertEquals("Final stored mass", 0D, v.getStoredMass());
        assertEquals("Settlement res1", 10D, s.getAmountResourceStored(res1));
        assertEquals("Settlement res2", 10D, s.getAmountResourceStored(res2));
        assertEquals("Settlement part1", 10, s.getItemResourceStored(part1));

    }
}
