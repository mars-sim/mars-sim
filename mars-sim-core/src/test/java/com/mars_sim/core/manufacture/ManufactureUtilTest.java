package com.mars_sim.core.manufacture;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.structure.building.function.FunctionType;

public class ManufactureUtilTest extends AbstractMarsSimUnitTest {

    private Building buildManufacture(Settlement s) {
        return buildFunction(s.getBuildingManager(), "Workshop", BuildingCategory.PROCESSING,
							FunctionType.MANUFACTURE,  LocalPosition.DEFAULT_POSITION, 0D, true);
    }
    
    public void testGetHighestManufacturingTechLevel() {
        var s = buildSettlement();
        
        int highest = ManufactureUtil.getHighestManufacturingTechLevel(s);
        assertEquals("No manufacturing", -1, highest);

        var w = buildManufacture(s);
        highest = ManufactureUtil.getHighestManufacturingTechLevel(s);

        assertEquals("Highest tech level found", w.getManufacture().getTechLevel(), highest);
    }

    public void testGetManufactureProcessesForTechLevel() {

        var highList = ManufactureUtil.getManufactureProcessesForTechLevel(5);
        assertTrue("High tech processes", !highList.isEmpty());

        var lowList = ManufactureUtil.getManufactureProcessesForTechLevel(1);
        assertTrue("Low tech processes", !lowList.isEmpty());

        assertTrue("More high processes than low", highList.size() > lowList.size());
        assertTrue("Low are in high", highList.containsAll(lowList));

        var selection = highList.get(0);
        var found = ManufactureUtil.getManufactureProcessesForTechSkillLevel(selection.getTechLevelRequired(), selection.getSkillLevelRequired()-1);
        assertFalse("No process on low skill found", found.contains(selection));
        found = ManufactureUtil.getManufactureProcessesForTechSkillLevel(selection.getTechLevelRequired(), selection.getSkillLevelRequired());
        assertTrue("Process on low skill found", found.contains(selection));
    }

    public void testGetManufactureProcessesWithGivenOutput() {
        var results = ManufactureUtil.getManufactureProcessesWithGivenOutput("barrel");

        // Should just be 1 on the current manufacturing,xml
        assertFalse("No barrel processes found", results.isEmpty());

        var selection = results.get(0);
        assertTrue("Output for barrel process", selection.getOutputNames().contains("barrel"));
    }

    public void testGetSalvageProcessesForTechLevel() {

        var highList = ManufactureUtil.getSalvageProcessesForTechLevel(5);
        assertTrue("High tech salvage", !highList.isEmpty());
    
        var lowList = ManufactureUtil.getSalvageProcessesForTechLevel(1);
        assertTrue("Low tech salvage", !lowList.isEmpty());
    
        assertTrue("More high salvage than low", highList.size() > lowList.size());
        assertTrue("Low are in high", highList.containsAll(lowList));
    
        var selection = highList.get(0);
        var found = ManufactureUtil.getSalvageProcessesForTechSkillLevel(selection.getTechLevelRequired(), selection.getSkillLevelRequired()-1);
        assertFalse("No salvage on low skill found", found.contains(selection));
        found = ManufactureUtil.getSalvageProcessesForTechSkillLevel(selection.getTechLevelRequired(), selection.getSkillLevelRequired());
        assertTrue("Salvage on low skill found", found.contains(selection));
    }

    public void testCanSalvageBarrel() {
        var s = buildSettlement();
        var w = buildManufacture(s);
        var manu = w.getManufacture();

        SalvageProcessInfo selected = null;
        var found = ManufactureUtil.getSalvageProcessesForTechLevel(manu.getTechLevel());
        for(var p : found) {
            if (p.getItemName().equals("barrel")) {
                selected = p;
                break;
            }
        }

        assertNotNull("Found barrel salvage", selected);
        var canstart = ManufactureUtil.canSalvageProcessBeStarted(selected, manu);
        assertFalse("Cannot start barrel salvage witout barrels", canstart);

        EquipmentFactory.createEquipment(EquipmentType.BARREL, s);
        canstart = ManufactureUtil.canSalvageProcessBeStarted(selected, manu);
        assertTrue("Can start barrel salvage with barrel", canstart);
    }
}
