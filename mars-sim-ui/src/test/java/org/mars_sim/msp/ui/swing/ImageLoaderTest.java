package org.mars_sim.msp.ui.swing;


import javax.swing.Icon;

import junit.framework.TestCase;

public class ImageLoaderTest extends TestCase {
    public void testAnimatedWeatherIcons() {
        testLoadingIcon("weather/lowWind");
        testLoadingIcon("weather/highWind");
        testLoadingIcon("weather/spinningSun");
    }

    private void testLoadingIcon(String logicalName) {
        Icon loaded = ImageLoader.getIconByName(logicalName);
        assertNotNull("Image called " + logicalName, loaded);
    }
}
