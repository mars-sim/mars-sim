package com.mars_sim.core.manufacture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;


import java.util.List;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.structure.Settlement;

public class WorkshopProcessTest extends MarsSimUnitTest {
    private static final String FURNACE_PROCESS = "Cast aluminum ingot";

    @Test
    public void testManuProcessFailed() {
        ManufactureProcessInfo processInfo = getConfig().getManufactureConfiguration().getManufactureProcessList().stream()
                            .filter(p -> p.getName().equals(FURNACE_PROCESS))
                            .findAny().get();
        assertTrue(processInfo.getProcessTimeRequired() > 0, "Process has prcoess time");
        
        var s = buildSettlement("Test", true);
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var w = b.getManufacture();

        var p = new ManufactureProcess(processInfo, w);
        p.startProcess();
        assertTrue(p.isActive(), "Manufacture process started");
        assertTrue(w.getProcesses().contains(p), "Workshop has process");

        assertTrue(p.addProcessTime(processInfo.getProcessTimeRequired()), "Process active after Process");

        p.stopProcess(true);

        assertEquals(0, w.getToolDetails().get(p.getTooling()).getInUse(), "Tools at end");
        assertFalse(p.isActive(), "Manufacture process stopped");
        assertFalse(w.getProcesses().contains(p), "Workshop has no process");

        assertResources(s, processInfo.getInputList());
    }

    @Test
    public void testManuProcess() {
        ManufactureProcessInfo processInfo = getConfig().getManufactureConfiguration().getManufactureProcessList().stream()
                            .filter(p -> p.getName().equals(FURNACE_PROCESS))
                            .findAny().get();
        assertTrue(processInfo.getProcessTimeRequired() > 0, "Process has prcoess time");
        
        var s = buildSettlement("Test", true);
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var w = b.getManufacture();


        var p = new ManufactureProcess(processInfo, w);
        var tool = w.getToolDetails().get(p.getTooling());
        assertEquals(0, tool.getInUse(), "Tools before start");

        p.startProcess();
        assertTrue(p.isActive(), "Manufacture process started");
        assertTrue(w.getProcesses().contains(p), "Workshop has process");
        assertEquals(1, tool.getInUse(), "Tools after start");

        assertTrue(p.addProcessTime(processInfo.getProcessTimeRequired()), "Process active after Process");
        assertFalse(p.addWorkTime(processInfo.getWorkTimeRequired(), 1), "Process active after Work");

        assertFalse(p.isActive(), "Manufacture process stopped");
        assertFalse(w.getProcesses().contains(p), "Workshop has no process");
        assertEquals(0, tool.getInUse(), "Tools at end");


        assertResources(s, processInfo.getOutputList());
    }

    @Test
    public void testSalvProcess() {
        SalvageProcessInfo processInfo = getConfig().getManufactureConfiguration().getSalvageInfoList().stream()
                            .filter(p -> p.getName().equals(SalvageProcessInfo.NAME_PREFIX + "gas canister"))
                            .findAny().get();
        assertEquals(0D, processInfo.getProcessTimeRequired(), "Process has no process time");
        
        var s = buildSettlement("Test", true);
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var w = b.getManufacture();

        var canister = EquipmentFactory.createEquipment(EquipmentType.GAS_CANISTER, s);


        var p = new SalvageProcess(processInfo, w, canister);
        p.startProcess();
        assertTrue(p.isActive(), "Salvage process started");
        assertTrue(w.getProcesses().contains(p), "Workshop has process");
        assertFalse(s.getEquipmentSet().contains(canister), "Canister not registered");

        assertFalse(p.addWorkTime(processInfo.getWorkTimeRequired(), 1), "Process active after Work");

        assertFalse(p.isActive(), "Salvage process stopped");
        assertFalse(w.getProcesses().contains(p), "Workshop has no process");

        for(var i: processInfo.getOutputList()) {
            if (i.getType() == ItemType.PART) {
                assertTrue(s.getItemResourceStored(i.getId()) > 0D, "Settlement has output " + i.getName());
            }
        }
    }

    private void assertResources(Settlement s, List<ProcessItem> processItems) {
        for(var i: processItems) {
            switch(i.getType()) {
                case AMOUNT_RESOURCE:
                    assertTrue(s.getSpecificAmountResourceStored(i.getId()) >= i.getAmount(), "Settlement has output " + i.getName());
                    break;
                case PART:
                    assertTrue(s.getItemResourceStored(i.getId()) >= i.getAmount(), "Settlement has output " + i.getName());
                    break;
                default:
            }
        }
        
    }
}
