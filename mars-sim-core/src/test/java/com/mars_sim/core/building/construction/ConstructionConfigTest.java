package com.mars_sim.core.building.construction;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.construction.ConstructionStageInfo.Stage;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

class ConstructionConfigTest {

    private ConstructionConfig cConfig;

    @BeforeEach
    void setUp() {
        cConfig = SimulationConfig.loadConfig().getConstructionConfiguration();
    }

    @Test
    void testGetConstructionStageInfoList() {

        var bldgs = cConfig.getConstructionStageInfoList(Stage.BUILDING);
        assertFalse(bldgs.isEmpty(), "Building stages is empty");
     
        var found = cConfig.getConstructionStageInfoList(Stage.FOUNDATION);
        assertFalse(found.isEmpty(), "Foundation stages is empty");

        var frames = cConfig.getConstructionStageInfoList(Stage.FRAME);
        assertFalse(frames.isEmpty(), "Frame stages is empty");
    }

    @Test
    void testSteelFrameTower() {
        var selected = cConfig.getConstructionStageInfoByName("Steel Frame Tower");

        assertEquals(selected.getName(), "Steel Frame Tower", "Name");
        assertEquals(Stage.FRAME, selected.getType(), "Type");
        assertEquals(5D, selected.getWidth(), "Width");
        assertEquals(5D, selected.getLength(), "Length");
        assertEquals(selected.getAlignment(), "length", "Alignment");
        assertEquals(0, selected.getBaseLevel(), "Base");
        assertTrue(selected.isConstructable(), "Constructable");
        assertTrue(selected.isSalvagable(), "Salvagable");
        assertEquals(2, selected.getArchitectConstructionSkill(), "Skill");
        assertEquals(selected.getPrerequisiteStage().getName(), "Surface Foundation 5x5", "PreStage");

        var resources = selected.getResources();
        assertEquals(1D, resources.get(ResourceUtil.findIDbyAmountResourceName("acetylene")), "acetylene");
        assertEquals(25D, resources.get(ResourceUtil.findIDbyAmountResourceName("concrete")), "concrete");
        
        var parts = selected.getParts();
        assertEquals(20, parts.get(ItemResourceUtil.findIDbyItemResourceName("steel truss")).intValue(), "steel truss");
    }
}
