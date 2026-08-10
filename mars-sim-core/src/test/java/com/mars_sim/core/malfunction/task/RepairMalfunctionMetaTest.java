package com.mars_sim.core.malfunction.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.test.MarsSimUnitTest;

class RepairMalfunctionMetaTest extends MarsSimUnitTest {
    private static final String FIRE = "Class A Combustible Fire";

    // This unit test needs failures
    @BeforeEach
    void setup() {
        MalfunctionManager.setNoFailures(false);
    }

    @AfterEach
    void teardown() {
        MalfunctionManager.setNoFailures(true);
    }

    private void buldMalfunction(String fault, Malfunctionable b, EquipmentOwner eqmInv) {
        
        var mm = getConfig().getMalfunctionConfiguration().getMalfunctionList().stream()
                .filter(m -> m.getName().equals(fault))
                .findFirst();
        assertTrue(mm.isPresent(), fault + " malfunction should be defined in the configuration");
        var fire = mm.get();

        var man = b.getMalfunctionManager();
        var m = man.triggerMalfunction(fire, false, null);
        assertTrue(man.hasMalfunction(), "Malfunction should be present after triggering");

        m.getRepairParts().entrySet().forEach(e -> eqmInv.storeItemResource(e.getKey().getPart().getID(),
                                                                    e.getValue()));                                                            
    }

    @Test
    void testGetSettlementTasks() {
        var s = buildSettlement("test");
        var b = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);

        var mt = new RepairMalfunctionMeta();
        var tasks = mt.getSettlementTasks(s);
        assertTrue(tasks.isEmpty(), "No tasks should be available when there are no malfunctions");

        buldMalfunction(FIRE, b, s.getEquipmentInventory());

        tasks = mt.getSettlementTasks(s);
        assertEquals(1, tasks.size(), "Tasks should be available when resources are available");
    }

    @Test
    void testVehicleFault() {
        var s = buildSettlement("test");
        var v = buildRover(s, CARGO_ROVER, LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);
        var p = buildPerson("Fixed", s);
        p.transfer(v);

        var mt = new RepairMalfunctionMeta();
        var tasks = mt.getTaskJobs(p);
        assertTrue(tasks.isEmpty(), "No tasks should be available when there are no malfunctions");

        buldMalfunction(FIRE, v, s.getEquipmentInventory());

        tasks = mt.getTaskJobs(p);
        assertEquals(1, tasks.size(), "Tasks should be available when resources are available");
    }
}
