package com.mars_sim.core.building.function.farming;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;

class CropConfigTest {

    private SimulationConfig simConfig;

    @BeforeEach
    void setUp() {
        simConfig = SimulationConfig.loadConfig();
    }

    @Test
    void testPhaseType() {
        // Check all the special preconfigured PhaseTypes can be resolved
        assertEquals(PhaseType.GERMINATION, Phase.getAssociatedPhaseType("Germination"), "Germination");
        assertEquals(PhaseType.PLANTING, Phase.getAssociatedPhaseType("Planting"), "Planting");
        assertEquals(PhaseType.HARVESTING, Phase.getAssociatedPhaseType("Harvesting"), "Harvest");
        assertEquals(PhaseType.FINISHED, Phase.getAssociatedPhaseType("Finished"), "Finished");

        assertEquals(PhaseType.OTHER, Phase.getAssociatedPhaseType("Sprouting"), "Other 1");
        assertEquals(PhaseType.OTHER, Phase.getAssociatedPhaseType("Leafing"), "Other 2");
    }

    @Test
    void testGetStats() {
        var config = simConfig.getCropConfiguration();

        assertTrue(config.getAverageCropGrowingTime() > 0, "Average growing sols");
        assertTrue(config.getFarmingAreaNeededPerPerson() > 0, "Farming area");

    }

    @Test
    void testFungi() {
        testCropCategory("Fungi",7,  false);
    }

    @Test
    void testBulbs() {
        testCropCategory("Bulbs",9,  true);
    }

    private void testCropCategory(String name, int numPhases, boolean needsLight) {
                        
        var config = simConfig.getCropConfiguration();
        var cat = config.getCropCategories().stream().filter(v -> v.getName().equals(name)).findFirst().orElse(null);

        assertNotNull(cat, name + " found");
        assertEquals(name, cat.getName(), "Name");
        assertEquals(needsLight, cat.needsLight(), name + " needs light");
        assertEquals(numPhases, cat.getPhases().size(), name + " phsaes");
        var inGround = cat.getInGroundPercentage();
        assertTrue((1D < inGround) && (inGround <= 95D), name + " inground %age");

        double total = 0;
        var phases = cat.getPhases();
        for(int i = 0; i < phases.size(); i++) {
            var phase = phases.get(i);
            total += phase.getPercentGrowth();
            assertEquals(total, phase.getCumulativePercentGrowth(), 0.0001, phase.getPhaseType() + " cummulatiive growth");

            var nextPhase = cat.getNextPhase(phase);
            if (i == (phases.size() - 1)) {
                // Last phase so next phase should be null
                assertNull(nextPhase, "Next phase at last phase");
            }
            else {
                assertEquals(phases.get(i+1), nextPhase, "Next phase");
            }

        }
    }

    @Test
    void testGetCropTypes() {
        var config = simConfig.getCropConfiguration();

        assertTrue(config.getCropTypes().size() > 0, "Crop types");
    }

    @Test
    void testGarlic() {
        testCropSpec("Garlic", "Bulbs", 85);
    }

    @Test
    void testRice() {
        testCropSpec("Rice", "Grains", 100);
    }


    private void testCropSpec(String name, String category, int growingSols) {
                        
        var config = simConfig.getCropConfiguration();
        var cat = config.getCropCategories().stream().filter(v -> v.getName().equals(category)).findFirst().orElse(null);

        var spec = config.getCropTypeByName(name);
        assertNotNull(spec, name + " found");
        assertEquals(name, spec.getName(), "Name");
        assertEquals(cat, spec.getCropCategory(), name + " category");
        assertEquals(growingSols, spec.getGrowingSols(), name + " growing sols");
    }
}
