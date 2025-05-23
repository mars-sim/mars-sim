package com.mars_sim.core.resource;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.equipment.EquipmentType;

public class SuppliesManifestTest extends AbstractMarsSimUnitTest{

    public void testAddResources() {
        final double OPT = 20D;
        final double MAND = 10D;
        var manifest = new SuppliesManifest();

        manifest.addAmount(ResourceUtil.ARGON_ID, MAND, true);
        manifest.addAmount(ResourceUtil.OXYGEN_ID, MAND, true);
        manifest.addAmount(ResourceUtil.NITROGEN_ID, OPT, false);

        var mand = manifest.getAmounts(true);
        assertEquals("Mandatory Resources", 2, mand.size());
        assertEquals("Argon amount", MAND, mand.get(ResourceUtil.ARGON_ID));
        assertEquals("Oxygen amount", MAND, mand.get(ResourceUtil.OXYGEN_ID));

        var opt = manifest.getAmounts(false);
        assertEquals("Optional Resources", 1, opt.size());
        assertEquals("Nitrogen amount", OPT, opt.get(ResourceUtil.NITROGEN_ID));
    }

    public void testAddItem() {
        final int OPT = 5;
        final int MAND = 4;
        var manifest = new SuppliesManifest();

        manifest.addItem(ItemResourceUtil.backhoeID, MAND, true);
        manifest.addItem(ItemResourceUtil.printerID, MAND, true);
        manifest.addItem(ItemResourceUtil.pneumaticDrillID, OPT, false);

        var mand = manifest.getItems(true);
        assertEquals("Mandatory Items", 2, mand.size());
        assertEquals("Backhoe amount", MAND, mand.get(ItemResourceUtil.backhoeID).intValue());
        assertEquals("Printer amount", MAND, mand.get(ItemResourceUtil.printerID).intValue());

        var opt = manifest.getItems(false);
        assertEquals("Optional Items", 1, opt.size());
        assertEquals("Drill amount", OPT, opt.get(ItemResourceUtil.pneumaticDrillID).intValue());
    }

    public void testAddEquipment() {
        final int OPT = 5;
        final int MAND = 4;
        var manifest = new SuppliesManifest();

        manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.BAG), MAND, true);
        manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.BARREL), MAND, true);
        manifest.addEquipment(EquipmentType.getResourceID(EquipmentType.EVA_SUIT), OPT, false);

        var mand = manifest.getEquipment(true);
        assertEquals("Mandatory Equipment", 2, mand.size());
        assertEquals("Argon amount", MAND, mand.get(EquipmentType.getResourceID(EquipmentType.BAG)).intValue());
        assertEquals("Oxygen amount", MAND, mand.get(EquipmentType.getResourceID(EquipmentType.BARREL)).intValue());

        var opt = manifest.getEquipment(false);
        assertEquals("Optional entries", 1, opt.size());
        assertEquals("Nitrogen amount", OPT, opt.get(EquipmentType.getResourceID(EquipmentType.EVA_SUIT)).intValue());
    }
}
