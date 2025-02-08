package com.mars_sim.core.structure;


import com.mars_sim.core.AbstractMarsSimUnitTest;

public class SettlementTemplateConfigTest extends AbstractMarsSimUnitTest {
    public void testGetAll() {
        var st = simConfig.getSettlementTemplateConfiguration();

        var known = st.getKnownItems();
        assertFalse("Settlement templates defined", known.isEmpty());

        var name = st.getItemNames();
        assertEquals("Names and template counts", name.size(), known.size());
    }

    public void testHubBase() {
        var st = simConfig.getSettlementTemplateConfiguration();

        var hubBase = st.getItem("Hub Base");
        assertNotNull("Hub Base template found", hubBase);
        assertEquals("Name of template", "Hub Base", hubBase.getName());

        assertEquals("Shift pattern", "Standard 4 Shift", hubBase.getShiftDefinition().getName());
    
        var supplies = hubBase.getSupplies();
        assertFalse("Equipment is empty", supplies.getEquipment().isEmpty());
        assertFalse("Parts is empty", supplies.getParts().isEmpty());
        assertFalse("Bins is empty", supplies.getBins().isEmpty());
        assertFalse("Buildings is empty", supplies.getBuildings().isEmpty());
        assertFalse("Resources is empty", supplies.getResources().isEmpty());
        assertFalse("Vehciles is empty", supplies.getVehicles().isEmpty());

        assertFalse("Robots is empty", hubBase.getPredefinedRobots().isEmpty());

        assertEquals("Supply mission", 1, hubBase.getResupplyMissionTemplates().size());

        assertEquals("Sponsor", "MS", hubBase.getSponsor().getName());

    }
}
