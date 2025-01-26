package com.mars_sim.core.structure.building.function.farming;

import com.mars_sim.core.AbstractMarsSimUnitTest;

public class CropConfigTest extends AbstractMarsSimUnitTest {

    public void testPhaseType() {
        // Check all the special preconfigured PhaseTypes can be resolved
        assertEquals("Germination", PhaseType.GERMINATION, Phase.getAssociatedPhaseType("Germination"));
        assertEquals("Planting", PhaseType.PLANTING, Phase.getAssociatedPhaseType("Planting"));
        assertEquals("Harvest", PhaseType.HARVESTING, Phase.getAssociatedPhaseType("Harvesting"));
        assertEquals("Finished", PhaseType.FINISHED, Phase.getAssociatedPhaseType("Finished"));

        assertEquals("Other 1", PhaseType.OTHER, Phase.getAssociatedPhaseType("Sprouting"));
        assertEquals("Other 2", PhaseType.OTHER, Phase.getAssociatedPhaseType("Leafing"));
    }

    public void testGetStats() {
        var config = getConfig().getCropConfiguration();

        assertTrue("Average growing sols", config.getAverageCropGrowingTime() > 0);
        assertTrue("Farming area", config.getFarmingAreaNeededPerPerson() > 0);

    }

    public void testFungi() {
        testCropCategory("Fungi",7,  false);
    }

    public void testBulbs() {
        testCropCategory("Bulbs",9,  true);
    }

    private void testCropCategory(String name, int numPhases, boolean needsLight) {
                        
        var config = getConfig().getCropConfiguration();
        var cat = config.getCropCategories().stream().filter(v -> v.getName().equals(name)).findFirst().orElse(null);

        assertNotNull(name + " found", cat);
        assertEquals("Name", name, cat.getName());
        assertEquals(name + " needs light", needsLight, cat.needsLight());
        assertEquals(name + " phsaes", numPhases, cat.getPhases().size());
        var inGround = cat.getInGroundPercentage();
        assertTrue(name + " inground %age", (1D < inGround) && (inGround <= 95D));

        double total = 0;
        var phases = cat.getPhases();
        for(int i = 0; i < phases.size(); i++) {
            var phase = phases.get(i);
            total += phase.getPercentGrowth();
            assertEquals(phase.getPhaseType() + " cummulatiive growth", total,
                    phase.getCumulativePercentGrowth(), 0.0001);

            var nextPhase = cat.getNextPhase(phase);
            if (i == (phases.size() - 1)) {
                // Last phase so next phase should be null
                assertNull("Next phase at last phase", nextPhase);
            }
            else {
                assertEquals("Next phase", phases.get(i+1), nextPhase);
            }

        }
    }

    public void testGetCropTypes() {
        var config = getConfig().getCropConfiguration();

        assertTrue("Crop types", config.getCropTypes().size() > 0);
    }

    public void testGarlic() {
        testCropSpec("Garlic", "Bulbs", 85);
    }

    public void testRice() {
        testCropSpec("Rice", "Grains", 100);
    }


    private void testCropSpec(String name, String category, int growingSols) {
                        
        var config = getConfig().getCropConfiguration();
        var cat = config.getCropCategories().stream().filter(v -> v.getName().equals(category)).findFirst().orElse(null);

        var spec = config.getCropTypeByName(name);
        assertNotNull(name + " found", spec);
        assertEquals("Name", name, spec.getName());
        assertEquals(name + " category", cat, spec.getCropCategory());
        assertEquals(name + " growing sols", growingSols, spec.getGrowingSols());
    }
}
