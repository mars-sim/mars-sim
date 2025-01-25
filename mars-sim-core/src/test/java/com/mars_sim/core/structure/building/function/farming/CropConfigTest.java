package com.mars_sim.core.structure.building.function.farming;

import com.mars_sim.core.AbstractMarsSimUnitTest;

public class CropConfigTest extends AbstractMarsSimUnitTest {

    public void testGetStats() {
        var config = getConfig().getCropConfiguration();

        assertTrue("Average growing sols", config.getAverageCropGrowingTime() > 0);
        assertTrue("Farming area", config.getFarmingAreaNeededPerPerson() > 0);

    }

    public void testGetCropTypes() {
        var config = getConfig().getCropConfiguration();

        assertTrue("Crop types", config.getCropTypes().size() > 0);
    }

    public void testGarlic() {
        testCropSpec("Garlic", CropCategory.BULBS, 9, 85);
    }

    public void testRice() {
        testCropSpec("Rice", CropCategory.GRAINS, 11, 100);
    }


    private void testCropSpec(String name, CropCategory category, int numPhases, int growingSols) {
                        
        var config = getConfig().getCropConfiguration();

        var spec = config.getCropTypeByName(name);
        assertNotNull(name + " found", spec);
        assertEquals("Name", name, spec.getName());
        assertEquals(name + " category", category, spec.getCropCategory());
        assertEquals(name + " growing sols", growingSols, spec.getGrowingSols());
        assertEquals(name + " phases", numPhases, spec.getPhases().size());


    }
}
