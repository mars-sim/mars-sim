package com.mars_sim.core.manufacture;


import java.util.List;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.structure.Settlement;

public class WorkshopProcessTest extends AbstractMarsSimUnitTest {
    private static final String MAKE_NITRITES = "Make Nitrites";

    public void testManuProcessFailed() {
        ManufactureProcessInfo processInfo = getConfig().getManufactureConfiguration().getManufactureProcessList().stream()
                            .filter(p -> p.getName().equals(MAKE_NITRITES))
                            .findAny().get();
        assertTrue("Process has prcoess time", processInfo.getProcessTimeRequired() > 0);
        
        var s = buildSettlement("Test", true);
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var w = b.getManufacture();

        var p = new ManufactureProcess(processInfo, w);
        p.startProcess();
        assertTrue("Manufacture process started", p.isActive());
        assertTrue("Workshop has process", w.getProcesses().contains(p));

        assertTrue("Process active after Process", p.addProcessTime(processInfo.getProcessTimeRequired()));

        p.stopProcess(true);

        assertEquals("Tools at end", 0, w.getToolDetails().get(p.getProcessTool()).getInUse());
        assertFalse("Manufacture process stopped", p.isActive());
        assertFalse("Workshop has no process", w.getProcesses().contains(p));

        assertResources(s, processInfo.getInputList());
    }

    public void testManuProcess() {
        ManufactureProcessInfo processInfo = getConfig().getManufactureConfiguration().getManufactureProcessList().stream()
                            .filter(p -> p.getName().equals(MAKE_NITRITES))
                            .findAny().get();
        assertTrue("Process has prcoess time", processInfo.getProcessTimeRequired() > 0);
        
        var s = buildSettlement("Test", true);
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var w = b.getManufacture();


        var p = new ManufactureProcess(processInfo, w);
        var tool = w.getToolDetails().get(p.getProcessTool());
        assertEquals("Tools before start", 0, tool.getInUse());

        p.startProcess();
        assertTrue("Manufacture process started", p.isActive());
        assertTrue("Workshop has process", w.getProcesses().contains(p));
        assertEquals("Tools after start", 1, tool.getInUse());

        assertTrue("Process active after Process", p.addProcessTime(processInfo.getProcessTimeRequired()));
        assertFalse("Process active after Work", p.addWorkTime(processInfo.getWorkTimeRequired(), 1));

        assertFalse("Manufacture process stopped", p.isActive());
        assertFalse("Workshop has no process", w.getProcesses().contains(p));
        assertEquals("Tools at end", 0, tool.getInUse());


        assertResources(s, processInfo.getOutputList());
    }

    public void testSalvProcess() {
        SalvageProcessInfo processInfo = getConfig().getManufactureConfiguration().getSalvageInfoList().stream()
                            .filter(p -> p.getName().equals(SalvageProcessInfo.NAME_PREFIX + "gas canister"))
                            .findAny().get();
        assertEquals("Process has no process time", 0D, processInfo.getProcessTimeRequired());
        
        var s = buildSettlement("Test", true);
        var b = ManufacturingManagerTest.buildWorkshop(this, s.getBuildingManager());
        var w = b.getManufacture();

        var canister = EquipmentFactory.createEquipment(EquipmentType.GAS_CANISTER, s);


        var p = new SalvageProcess(processInfo, w, canister);
        p.startProcess();
        assertTrue("Salvage process started", p.isActive());
        assertTrue("Workshop has process", w.getProcesses().contains(p));
        assertFalse("Canister not registered", s.getEquipmentSet().contains(canister));

        assertFalse("Process active after Work", p.addWorkTime(processInfo.getWorkTimeRequired(), 1));

        assertFalse("Salvage process stopped", p.isActive());
        assertFalse("Workshop has no process", w.getProcesses().contains(p));

        for(var i: processInfo.getOutputList()) {
            if (i.getType() == ItemType.PART) {
                assertTrue("Settlement has output " + i.getName(), s.getItemResourceStored(i.getId()) > 0D);
            }
        }
    }

    private void assertResources(Settlement s, List<ProcessItem> processItems) {
        for(var i: processItems) {
            switch(i.getType()) {
                case AMOUNT_RESOURCE:
                    assertTrue("Settlement has output " + i.getName(), s.getAmountResourceStored(i.getId()) >= i.getAmount());
                    break;
                case PART:
                    assertTrue("Settlement has output " + i.getName(), s.getItemResourceStored(i.getId()) >= i.getAmount());
                    break;
                default:
            }
        }
        
    }
}
