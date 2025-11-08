package com.mars_sim.core.manufacture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.Settlement;

public class ManufactureUtilTest extends MarsSimUnitTest {

    private Building buildManufacture(Settlement s) {
        return buildFunction(s.getBuildingManager(), "Workshop", BuildingCategory.PROCESSING,
							FunctionType.MANUFACTURE,  LocalPosition.DEFAULT_POSITION, 0D, true);
    }
    
    @Test
    public void testGetHighestManufacturingTechLevel() {
        var s = buildSettlement("tech", true);
        
        int highest = ManufactureUtil.getHighestManufacturingTechLevel(s);
        assertEquals(-1, highest, "No manufacturing");

        var w = buildManufacture(s);
        s.getManuManager().updateTechLevel(); // Simulate a daily recheck after a new building
        highest = ManufactureUtil.getHighestManufacturingTechLevel(s);

        assertEquals(w.getManufacture().getTechLevel(), highest, "Highest tech level found");
    }

    @Test
    public void testGetManufactureProcessesForTechLevel() {

        var highList = ManufactureUtil.getManufactureProcessesForTechLevel(5);
        assertTrue(!highList.isEmpty(), "High tech processes");

        var lowList = ManufactureUtil.getManufactureProcessesForTechLevel(1);
        assertTrue(!lowList.isEmpty(), "Low tech processes");

        assertTrue(highList.size() > lowList.size(), "More high processes than low");
        assertTrue(highList.containsAll(lowList), "Low are in high");

        var selection = highList.get(0);
        Set<Tooling> tools = Set.of(selection.getTooling());
        var found = ManufactureUtil.getManufactureProcessesForTechSkillLevel(selection.getTechLevelRequired(),
                                    selection.getSkillLevelRequired()-1, tools);
        assertFalse(found.contains(selection), "No process on low skill found");
        found = ManufactureUtil.getManufactureProcessesForTechSkillLevel(selection.getTechLevelRequired(),
                                    selection.getSkillLevelRequired(), Collections.emptySet());
        assertFalse(found.contains(selection), "No process on High skill but no tools");
        
        found = ManufactureUtil.getManufactureProcessesForTechSkillLevel(selection.getTechLevelRequired(),
                                    selection.getSkillLevelRequired(), tools);
        assertTrue(found.contains(selection), "Process on high skill found");
    }

    @Test
    public void testGetManufactureProcessesWithGivenOutput() {
        var results = ManufactureUtil.getManufactureProcessesWithGivenOutput("barrel");

        // Should just be 1 on the current manufacturing,xml
        assertFalse(results.isEmpty(), "No barrel processes found");

        var selection = results.get(0);
        assertTrue(selection.isOutput("barrel"), "Output for barrel process");
    }

    @Test
    public void testGetSalvageProcessesForTechLevel() {

        var highList = ManufactureUtil.getSalvageProcessesForTechLevel(5);
        assertTrue(!highList.isEmpty(), "High tech salvage");
    
        var lowList = ManufactureUtil.getSalvageProcessesForTechLevel(1);
        assertTrue(!lowList.isEmpty(), "Low tech salvage");
    
        assertTrue(highList.size() > lowList.size(), "More high salvage than low");
        assertTrue(highList.containsAll(lowList), "Low are in high");
    
        var selection = highList.get(0);
        var found = ManufactureUtil.getSalvageProcessesForTechSkillLevel(selection.getTechLevelRequired(),
                                selection.getSkillLevelRequired()-1, Collections.emptySet());
        assertFalse(found.contains(selection), "No salvage on low skill found");
        found = ManufactureUtil.getSalvageProcessesForTechSkillLevel(selection.getTechLevelRequired(),
                                selection.getSkillLevelRequired(), Collections.emptySet());
        assertTrue(found.contains(selection), "Salvage on low skill found");

        // Add Lifting to increase potential
        Set<Tooling> withLifting = Set.of(getConfig().getManufactureConfiguration().getTooling(ManufactureConfig.LIFTING));
        var fullFound = ManufactureUtil.getSalvageProcessesForTechSkillLevel(selection.getTechLevelRequired(),
                    selection.getSkillLevelRequired(), withLifting);
        assertTrue(fullFound.size() > found.size(), "More Salvage with lifting");
    }
}
