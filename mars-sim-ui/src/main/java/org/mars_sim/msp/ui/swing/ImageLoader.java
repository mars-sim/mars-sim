/*
 * Mars Simulation Project
 * ImageLoader.java
 * @date 2021-12-07
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * This is a static class that acts as a helper to load Images for use in the
 * UI. It is based on loading the resource form the class path via the
 * ClassLoader assuming all the Images to load a PNG. However other alternative
 * strategies can be easily implemented within this class.
 */
public class ImageLoader {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(ImageLoader.class.getName());

	private static HashMap<String, ImageIcon> iconCache = new HashMap<>();
	private static HashMap<String, Image> imageCache = new HashMap<>();
	private static Toolkit usedToolkit = null;

	/**
	 * Sub-directory/package for the images
	 */
	// Use classloader compatible paths
	public final static String IMAGE_DIR = "/images/";
	public final static String ICON_DIR = "/icons/";
	public final static String VEHICLE_ICON_DIR = "/icons/vehicle/";

	/**
	 * Static singleton
	 */
	private ImageLoader() {
	}

	/**
	 * Load the image icon with the specified name and a "png" image extension from
	 * IMAGE_DIR. This operation may either create a new Image Icon of returned
	 * a previously created one.
	 *
	 * @param imagename
	 *            Name of the image to load.
	 * @return ImageIcon containing image of specified name.
	 */
	public static ImageIcon getIcon(String imagename) {
		return getIcon(imagename, "png", IMAGE_DIR);
	}

	/**
	 * Load the image icon with the specified name and a "png" image extension. This
	 * operation may either create a new Image Icon of returned a previously created
	 * one.
	 *
	 * @param imagename
	 *            Name of the image to load.
	 * @return ImageIcon containing image of specified name.
	 */
	public static ImageIcon getIcon(String imagename, String dir) {
		return getIcon(imagename, "png", dir);
	}

	public static ImageIcon getNewIcon(String imagename) {
		ImageIcon found = null;

		if (imagename == null || imagename.equals("")) {
			return found;
		}

		String ext = "png";
		String fullImageName = imagename.endsWith(ext) ? imagename : imagename + "." + ext;
		found = iconCache.get(fullImageName);
		if (found == null) {
			String fileName = fullImageName.startsWith("/") ? fullImageName : "/" + fullImageName;

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
	 * Load the image icon with the specified name. This operation may either create
	 * a new Image Icon of returned a previously created one.
	 *
	 * @param imagename
	 *            Name of the image to load.
	 * @param ext
	 *            the file extension (ex. "png", "jpg").
	 * @param idr
	 *            the direcotyr of the file .
	 * @return ImageIcon containing image of specified name.
	 */
	public static ImageIcon getIcon(String imagename, String ext, String dir) {
		String fullImageName = imagename.endsWith(ext) ? imagename : imagename + "." + ext;
		ImageIcon found = iconCache.get(fullImageName);
		if (found == null) {
			String fileName = fullImageName.startsWith("/") ? fullImageName : dir + fullImageName;
//			logger.config("Filename : " + fileName + "   imagename : " + imagename + "    ext : "+ ext + "    dir : " + dir);
			found = new ImageIcon(ImageLoader.class.getResource(fileName));
			iconCache.put(fullImageName, found);
		}

		return found;
	}

	/**
	 * Get an image with the specified name. The name should include the suffix
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
//	   			logger.severe("'" + IMAGE_DIR + imagename + "' cannot be found");
				imageURL = ImageLoader.class.getResource(ICON_DIR + imageName);
				if (imageURL == null)
					logger.severe("'" + imageName + "' cannot be found");
    		}

			newImage = usedToolkit.createImage(imageURL);
			imageCache.put(imageName, newImage);
		}
		return newImage;
	}


	/**
	 * Convert from icon to image
	 *
	 * @param icon
	 * @return
	 */
	public static Image iconToImage(Icon icon) {
		// Note : use frame.setIconImage(iconToImage(icon));
		if (icon instanceof ImageIcon) {
			return ((ImageIcon)icon).getImage();
		}

		else {
			int w = icon.getIconWidth();
			int h = icon.getIconHeight();
			GraphicsEnvironment ge =
					GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			BufferedImage image = gc.createCompatibleImage(w, h);
			Graphics2D g = image.createGraphics();
			icon.paintIcon(null, g, 0, 0);
			g.dispose();
			return image;
	   }
	}
}
