package com.mars_sim.core.map.megdr;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.tool.RandomUtil;

class MEGDRFactoryTest {
    @Test
    void testMEGDRReader() {
        // Use different space combinations
        MEGDRMapReader memoryReader = MEGDRFactory.createReader(MEGDRFactory.MEMORY_READER
                                    + MEGDRFactory.SEPARATOR + MEGDRMapReader.DEFAULT_MEGDR_FILE);
        MEGDRMapReader directReader = MEGDRFactory.createReader(MEGDRFactory.DIRECT_READER
                                    + MEGDRFactory.SEPARATOR + " " + MEGDRMapReader.DEFAULT_MEGDR_FILE);
        MEGDRMapReader arrayReader = MEGDRFactory.createReader(MEGDRFactory.ARRAY_READER
        						+ " " + MEGDRFactory.SEPARATOR  + MEGDRMapReader.DEFAULT_MEGDR_FILE + " ");
        
        for (int i = 0; i < 1000; i++) {
            double phi = RandomUtil.getRandomDouble(Math.PI);
            double theta = RandomUtil.getRandomDouble(Math.PI * 2);
            short memoryElevation = memoryReader.getElevation(phi, theta);
            short directElevation = directReader.getElevation(phi, theta);
            short arrayElevation = arrayReader.getElevation(phi, theta);

            assertEquals("Array & Direct elevation", arrayElevation, directElevation);
            assertEquals("Array & Memory elevation", arrayElevation, memoryElevation);
        }
    }
}
