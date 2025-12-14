package com.mars_sim.ui.swing;


import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.Icon;

import org.junit.jupiter.api.Test;

class ImageLoaderTest{
    @Test
    void testAnimatedWeatherIcons() {
        testLoadingIcon("weather/lowWind");
        testLoadingIcon("weather/highWind");
        testLoadingIcon("weather/spinningSun");
    }

    private void testLoadingIcon(String logicalName) {
        Icon loaded = ImageLoader.getIconByName(logicalName);
        assertNotNull(loaded, "Image called " + logicalName);
    }
}
