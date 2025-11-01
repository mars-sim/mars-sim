package com.mars_sim.core.structure;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.resource.ItemResourceUtil;

public class SettlementTemplateConfigTest extends AbstractMarsSimUnitTest {
    public void testGetAll() {
        var st = getConfig().getSettlementTemplateConfiguration();

        var known = st.getKnownItems();
        assertFalse("Settlement templates defined", known.isEmpty());

        var name = st.getItemNames();
        assertEquals("Names and template counts", name.size(), known.size());
    }

    public void testHubBase() {
        var st = getConfig().getSettlementTemplateConfiguration();

        var hubBase = st.getItem("Hub Base");
        assertNotNull("Hub Base template found", hubBase);
        assertEquals("Name of template", "Hub Base", hubBase.getName());

        assertEquals("Shift pattern", "Standard 4 Shift", hubBase.getShiftDefinition().getName());
    
        var supplies = hubBase.getSupplies();
        assertFalse("Equipment is empty", supplies.getEquipment().isEmpty());

        assertFalse("Parts is empty", supplies.getParts().isEmpty());
        int num = supplies.getParts().get(
                            ItemResourceUtil.findItemResource("biosensor"));
        assertEquals("Biosensor", 52, num); // biosensors are now in certain packages. Total of 52 for Hub Base
        assertTrue("Has printers", supplies.getParts().get(
                            ItemResourceUtil.findItemResource(ItemResourceUtil.SLS_3D_PRINTER_ID)) > 0);

        assertFalse("Bins is empty", supplies.getBins().isEmpty());
        assertFalse("Buildings is empty", supplies.getBuildings().isEmpty());
        assertFalse("Resources is empty", supplies.getResources().isEmpty());
        assertFalse("Vehciles is empty", supplies.getVehicles().isEmpty());

        assertFalse("Robots is empty", hubBase.getPredefinedRobots().isEmpty());

        assertEquals("Supply mission", 1, hubBase.getResupplyMissionTemplates().size());

        assertEquals("Sponsor", "MS", hubBase.getSponsor().getName());

    }
}
