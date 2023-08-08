package org.mars.sim.mapdata;

import org.mars.sim.mapdata.megdr.MEGDRMapReader;
import org.mars.sim.tools.util.RandomUtil;

import junit.framework.TestCase;

public class MapDataFactoryTest extends TestCase {

    public void testMEGDRReader() {
        MEGDRMapReader memoryReader = MapDataFactory.createReader(MapDataFactory.MEMORY_READER
                                    + ", " + MEGDRMapReader.DEFAULT_MEGDR_FILE);
        MEGDRMapReader directReader = MapDataFactory.createReader(MapDataFactory.DIRECT_READER
                                    + ", " + MEGDRMapReader.DEFAULT_MEGDR_FILE);
        MEGDRMapReader arrayReader = MapDataFactory.createReader(MapDataFactory.ARRAY_READER
                                    + ", " + MEGDRMapReader.DEFAULT_MEGDR_FILE);
        for(int i = 0; i < 1000; i++) {
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
