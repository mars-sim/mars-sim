package com.mars_sim.core.resource;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.equipment.EquipmentType;

class SuppliesManifestTest {

    @BeforeEach
    void setUp() {
        SimulationConfig.loadConfig();
    }
    
    @Test
    void testAddResources() {
        final double OPT = 20D;
        final double MAND = 10D;
        var manifest = new SuppliesManifest();

        manifest.addAmount(ResourceUtil.ARGON_ID, MAND, true);
        manifest.addAmount(ResourceUtil.OXYGEN_ID, MAND, true);
        manifest.addAmount(ResourceUtil.NITROGEN_ID, OPT, false);

        var mand = manifest.getAmounts(true);
        assertEquals(2, mand.size(), "Mandatory Resources");
        assertEquals(MAND, mand.get(ResourceUtil.ARGON_ID), "Argon amount");
        assertEquals(MAND, mand.get(ResourceUtil.OXYGEN_ID), "Oxygen amount");

        var opt = manifest.getAmounts(false);
        assertEquals(1, opt.size(), "Optional Resources");
        assertEquals(OPT, opt.get(ResourceUtil.NITROGEN_ID), "Nitrogen amount");
    }

    @Test
    void testAddItem() {
        final int OPT = 5;
        final int MAND = 4;
        var manifest = new SuppliesManifest();

        manifest.addItem(ItemResourceUtil.BACKHOE_ID, MAND, true);
        manifest.addItem(ItemResourceUtil.SLS_3D_PRINTER_ID, MAND, true);
        manifest.addItem(ItemResourceUtil.PNEUMATIC_DRILL_ID, OPT, false);

        var mand = manifest.getItems(true);
        assertEquals(2, mand.size(), "Mandatory Items");
        assertEquals(MAND, mand.get(ItemResourceUtil.BACKHOE_ID).intValue(), "Backhoe amount");
        assertEquals(MAND, mand.get(ItemResourceUtil.SLS_3D_PRINTER_ID).intValue(), "Printer amount");

        var opt = manifest.getItems(false);
        assertEquals(1, opt.size(), "Optional Items");
        assertEquals(OPT, opt.get(ItemResourceUtil.PNEUMATIC_DRILL_ID).intValue(), "Drill amount");
    }

    @Test
    void testAddEquipment() {
        final int OPT = 5;
        final int MAND = 4;
        var manifest = new SuppliesManifest();

        manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.BAG), MAND, true);
        manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.BARREL), MAND, true);
        manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.EVA_SUIT), OPT, false);

        var mand = manifest.getEquipment(true);
        assertEquals(2, mand.size(), "Mandatory Equipment");
        assertEquals(MAND, mand.get(EquipmentType.getResourceID(EquipmentType.BAG)).intValue(), "Argon amount");
        assertEquals(MAND, mand.get(EquipmentType.getResourceID(EquipmentType.BARREL)).intValue(), "Oxygen amount");

        var opt = manifest.getEquipment(false);
        assertEquals(1, opt.size(), "Optional entries");
        assertEquals(OPT, opt.get(EquipmentType.getResourceID(EquipmentType.EVA_SUIT)).intValue(), "Nitrogen amount");
    }
}
