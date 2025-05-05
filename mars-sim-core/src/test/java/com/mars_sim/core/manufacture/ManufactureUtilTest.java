package com.mars_sim.core.manufacture;

import java.util.Collections;
import java.util.Set;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.Settlement;

public class ManufactureUtilTest extends AbstractMarsSimUnitTest {

    private Building buildManufacture(Settlement s) {
        return buildFunction(s.getBuildingManager(), "Workshop", BuildingCategory.PROCESSING,
							FunctionType.MANUFACTURE,  LocalPosition.DEFAULT_POSITION, 0D, true);
    }
    
    public void testGetHighestManufacturingTechLevel() {
        var s = buildSettlement("tech", true);
        
        int highest = ManufactureUtil.getHighestManufacturingTechLevel(s);
        assertEquals("No manufacturing", -1, highest);

        var w = buildManufacture(s);
        s.getManuManager().updateTechLevel(); // Simulate a daily recheck after a new building
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
        Set<Tooling> tools = Set.of(selection.getTooling());
        var found = ManufactureUtil.getManufactureProcessesForTechSkillLevel(selection.getTechLevelRequired(),
                                    selection.getSkillLevelRequired()-1, tools);
        assertFalse("No process on low skill found", found.contains(selection));
        found = ManufactureUtil.getManufactureProcessesForTechSkillLevel(selection.getTechLevelRequired(),
                                    selection.getSkillLevelRequired(), Collections.emptySet());
        assertFalse("No process on High skill but no tools", found.contains(selection));
        
        found = ManufactureUtil.getManufactureProcessesForTechSkillLevel(selection.getTechLevelRequired(),
                                    selection.getSkillLevelRequired(), tools);
        assertTrue("Process on high skill found", found.contains(selection));
    }

    public void testGetManufactureProcessesWithGivenOutput() {
        var results = ManufactureUtil.getManufactureProcessesWithGivenOutput("barrel");

        // Should just be 1 on the current manufacturing,xml
        assertFalse("No barrel processes found", results.isEmpty());

        var selection = results.get(0);
        assertTrue("Output for barrel process", selection.isOutput("barrel"));
    }

    public void testGetSalvageProcessesForTechLevel() {

        var highList = ManufactureUtil.getSalvageProcessesForTechLevel(5);
        assertTrue("High tech salvage", !highList.isEmpty());
    
        var lowList = ManufactureUtil.getSalvageProcessesForTechLevel(1);
        assertTrue("Low tech salvage", !lowList.isEmpty());
    
        assertTrue("More high salvage than low", highList.size() > lowList.size());
        assertTrue("Low are in high", highList.containsAll(lowList));
    
        var selection = highList.get(0);
        var found = ManufactureUtil.getSalvageProcessesForTechSkillLevel(selection.getTechLevelRequired(),
                                selection.getSkillLevelRequired()-1, Collections.emptySet());
        assertFalse("No salvage on low skill found", found.contains(selection));
        found = ManufactureUtil.getSalvageProcessesForTechSkillLevel(selection.getTechLevelRequired(),
                                selection.getSkillLevelRequired(), Collections.emptySet());
        assertTrue("Salvage on low skill found", found.contains(selection));

        // Add Lifting to increase potential
        Set<Tooling> withLifting = Set.of(getConfig().getManufactureConfiguration().getTooling("Lifting"));
        var fullFound = ManufactureUtil.getSalvageProcessesForTechSkillLevel(selection.getTechLevelRequired(),
                    selection.getSkillLevelRequired(), withLifting);
        assertTrue("More Salvage with lifting", fullFound.size() > found.size());
    }
}
