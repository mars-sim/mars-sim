package com.mars_sim.ui.swing;


import javax.swing.Icon;

import com.mars_sim.ui.swing.ImageLoader;

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
