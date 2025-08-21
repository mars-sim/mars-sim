package com.mars_sim.core.building.construction;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.construction.ConstructionStageInfo.Stage;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

public class ConstructionConfigTest extends AbstractMarsSimUnitTest {
    public void testGetConstructionStageInfoList() {
        var cConfig = getConfig().getConstructionConfiguration();

        var bldgs = cConfig.getConstructionStageInfoList(Stage.BUILDING);
        assertFalse("Building stages is empty", bldgs.isEmpty());
     
        var found = cConfig.getConstructionStageInfoList(Stage.FOUNDATION);
        assertFalse("Foundation stages is empty", found.isEmpty());

        var frames = cConfig.getConstructionStageInfoList(Stage.FRAME);
        assertFalse("Frame stages is empty", frames.isEmpty());
    }

    public void testPotentialNextStages() {
        var cConfig = getConfig().getConstructionConfiguration();

        String selectedStage = "Round Hab Frame";
        var selected = cConfig.getConstructionStageInfoByName(selectedStage);
        var found = cConfig.getPotentialNextStages(selected);
        assertEquals("Number of later stages for " + selectedStage, 6, found.size());
    }

    public void testSteelFrameTower() {
        var cConfig = getConfig().getConstructionConfiguration();
        var selected = cConfig.getConstructionStageInfoByName("Steel Frame Tower");

        assertEquals("Name", "Steel Frame Tower", selected.getName());
        assertEquals("Type", Stage.FRAME, selected.getType());
        assertEquals("Width", 5D, selected.getWidth());
        assertEquals("Length", 5D, selected.getLength());
        assertEquals("Alignment", "length", selected.getAlignment());
        assertEquals("Base", 0, selected.getBaseLevel());
        assertTrue("Constructable", selected.isConstructable());
        assertTrue("Salvagable", selected.isSalvagable());
        assertEquals("Skill", 2, selected.getArchitectConstructionSkill());
        assertEquals("PreStage", "Surface Foundation 5x5", selected.getPrerequisiteStage().getName());

        var resources = selected.getResources();
        assertEquals("acetylene", 1D,
                    resources.get(ResourceUtil.findIDbyAmountResourceName("acetylene")));
        assertEquals("concrete", 25D,
                    resources.get(ResourceUtil.findIDbyAmountResourceName("concrete")));
        
        var parts = selected.getParts();
        assertEquals("steel truss", 20,
                    parts.get(ItemResourceUtil.findIDbyItemResourceName("steel truss")).intValue());
    }
}
