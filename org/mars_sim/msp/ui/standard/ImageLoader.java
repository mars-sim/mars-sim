/**
 * Mars Simulation Project
 * ImageLoader.java
 * @version 2.74 2002-04-09
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard;

import java.util.HashMap;
import javax.swing.ImageIcon;
import java.net.URL;

/**
 * This is a static class that acts as a helper to load Images for use in the UI.
 * It is based on loading the resource form the class path via the ClassLoader
 * assuming all the Images to load a GIF. However other alternative strategies
 * can be easily implemented within this class.
 */
public class ImageLoader {

    private static HashMap iconCache = new HashMap();

    /**
     * Static singleton
     */
    private ImageLoader() {
    }

    /**
     * Load the image icon with the specified name. This operation may either
     * create a new Image Icon of returned a previously created one.
     *
     * @param name Name of the image to load.
     * @return ImageIcon containing image of specified name.
     */
    public static ImageIcon getIcon(String name) {
        ImageIcon found = (ImageIcon)iconCache.get(name);
        if (found == null) {
            String fileName = "images/" + name + ".gif";
            URL resource = ClassLoader.getSystemResource(fileName);

            System.out.println("Found " + name + " @ " + resource);
            found = new ImageIcon(resource);

            iconCache.put(name, found);
        }

        return found;
    }
}