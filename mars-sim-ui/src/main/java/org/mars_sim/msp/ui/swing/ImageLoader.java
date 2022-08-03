/*
 * Mars Simulation Project
 * ImageLoader.java
 * @date 2022-08-03
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.mars.sim.console.MarsTerminal;

/**
 * This is a static class that acts as a helper to load Images for use in the
 * UI. It is based on loading the resource form the class path via the
 * ClassLoader. However other alternative strategies can be easily 
 * implemented within this class.
 */
public class ImageLoader {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(ImageLoader.class.getName());

	private static HashMap<String, ImageIcon> iconCache = new HashMap<>();
	private static HashMap<String, Image> imageCache = new HashMap<>();
	private static Toolkit usedToolkit = null;

	/**
	 * Sub-directory/package for the images.
	 */
	// Note: Switch to classloader compatible paths
	public static final String IMAGE_DIR = "/images/";
	public static final String MAPS_DIR = "/maps/";
	public static final String ICON_DIR = "/icons/";
	public static final String VEHICLE_ICON_DIR = "/icons/vehicle/";
	public static final String PNG = "png";
	public static final String SLASH = "/";
	
	/**
	 * Static singleton
	 */
	private ImageLoader() {
	}

	/**
	 * Loads the image icon with the specified name and a "png" image extension from
	 * IMAGE_DIR. This operation may either create a new Image Icon of returned
	 * a previously created one.
	 *
	 * @param imagename
	 *            Name of the image to load.
	 * @return ImageIcon containing image of specified name.
	 */
	public static ImageIcon getIcon(String imagename) {
		return getIcon(imagename, PNG, IMAGE_DIR);
	}

	/**
	 * Loads the image icon with the specified name and a "png" image extension. This
	 * operation may either create a new Image Icon of returned a previously created
	 * one.
	 *
	 * @param imagename
	 *            Name of the image to load.
	 * @return ImageIcon containing image of specified name.
	 */
	public static ImageIcon getIcon(String imagename, String dir) {
		return getIcon(imagename, PNG, dir);
	}

	public static ImageIcon getNewIcon(String imagename) {
		if (imagename == null || imagename.equals("")) {
			return null;
		}
		
		ImageIcon found = null;
		String ext = PNG;
		String fullImageName = imagename.endsWith(ext) ? imagename : imagename + "." + ext;
		found = iconCache.get(fullImageName);
		if (found == null) {
			String fileName = fullImageName.startsWith(SLASH) ? fullImageName : SLASH + fullImageName;
			// Don't use the system classloader in a webstart env
			URL resource = ImageLoader.class.getResource(fileName);
			// e.g. ClassLoader.getSystemResource(fileName)
			if (resource == null) {
    			logger.severe("'" + fileName + "' cannot be found");
    		}

			found = new ImageIcon(resource);

			iconCache.put(fullImageName, found);
		}

		return found;
	}

	/**
	 * Loads the image icon with the specified name. This operation may either create
	 * a new Image Icon of returned a previously created one.
	 *
	 * @param imagename
	 *            Name of the image to load.
	 * @param ext
	 *            the file extension (ex. "png", "jpg").
	 * @param idr
	 *            the directory of the file .
	 * @return ImageIcon containing image of specified name.
	 */
	public static ImageIcon getIcon(String imagename, String ext, String dir) {
		String fullImageName = imagename.endsWith(ext) ? imagename : imagename + "." + ext;
		ImageIcon found = iconCache.get(fullImageName);
		if (found == null) {
			String fileName = fullImageName.startsWith(SLASH) ? fullImageName : dir + fullImageName;
//			logger.config("Filename : " + fileName + "   imagename : " + imagename + "    ext : "+ ext + "    dir : " + dir);
			found = new ImageIcon(ImageLoader.class.getResource(fileName));
			iconCache.put(fullImageName, found);
		}

		return found;
	}

	/**
	 * Gets an image with the specified name. The name should include the suffix
	 * identifying the format of the image.
	 *
	 * @param imageName
	 *            Name of image including suffix.
	 * @return Image found and loaded.
	 */
	public static Image getImage(String imageName) {
		Image newImage = imageCache.get(imageName);
		if (newImage == null) {

			if (usedToolkit == null) {
				usedToolkit = Toolkit.getDefaultToolkit();
			}

			URL imageURL = ImageLoader.class.getResource(IMAGE_DIR + imageName);
			if (imageURL == null) {
				imageURL = ImageLoader.class.getResource(ICON_DIR + imageName);
				if (imageURL == null) {					
					imageURL = ImageLoader.class.getResource(MAPS_DIR + imageName);
					if (imageURL == null) {	
						logger.severe("'" + imageName + "' cannot be found");
					}
				}		
    		}

			newImage = usedToolkit.createImage(imageURL);
			imageCache.put(imageName, newImage);
		}
		return newImage;
	}

	/**
	 * Converts from icon to image.
	 *
	 * @param icon
	 * @return
	 */
	public static Image iconToImage(Icon icon) {
		return MarsTerminal.iconToImage(icon);
	}
}
