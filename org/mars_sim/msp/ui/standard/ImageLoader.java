/**
 * Mars Simulation Project
 * ImageLoader.java
 * @version 2.81 2007-08-27
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

/**
 * This is a static class that acts as a helper to load Images for use in the UI.
 * It is based on loading the resource form the class path via the ClassLoader
 * assuming all the Images to load a GIF. However other alternative strategies
 * can be easily implemented within this class.
 */
public class ImageLoader {
    
    private static String CLASS_NAME = 
	"org.mars_sim.msp.ui.standard.ImageLoader";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    private static HashMap<String, ImageIcon> iconCache = new HashMap<String, ImageIcon>();
    private static HashMap<String, Image> imageCache = new HashMap<String, Image>();
    private static Toolkit usedToolkit = null;

    /**
     * Sub-directory/package for the images
     */
    public final static String IMAGE_DIR = "images/";

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
        ImageIcon found = iconCache.get(name);
        if (found == null) {
            String fileName = IMAGE_DIR + name + ".gif";
            URL resource = ClassLoader.getSystemResource(fileName);

            found = new ImageIcon(resource);

            iconCache.put(name, found);
        }

        return found;
    }

    /**
     * Get an image with the specified name. The name should include the suffix
     * identifying the format of the image.
     *
     * @param imagename Name of image including suffix.
     * @return Image found and loaded.
     */
    public static Image getImage(String imagename) {
        Image newImage = imageCache.get(imagename);
        if (newImage == null) {

            if (usedToolkit == null) {
                usedToolkit = Toolkit.getDefaultToolkit();
            }
            URL imageURL = ClassLoader.getSystemResource(IMAGE_DIR + imagename);

            newImage = usedToolkit.createImage(imageURL);
            imageCache.put(imagename, newImage);
        }
        return newImage;
    }
}