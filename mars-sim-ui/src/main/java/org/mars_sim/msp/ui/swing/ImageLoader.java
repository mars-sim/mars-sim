/**
 * Mars Simulation Project
 * ImageLoader.java
 * @version 3.1.0 2017-03-12
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.swing;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * This is a static class that acts as a helper to load Images for use in the
 * UI. It is based on loading the resource form the class path via the
 * ClassLoader assuming all the Images to load a PNG. However other alternative
 * strategies can be easily implemented within this class.
 */
public class ImageLoader {

	private static HashMap<String, ImageIcon> iconCache = new HashMap<String, ImageIcon>();
	private static HashMap<String, Image> imageCache = new HashMap<String, Image>();
	private static Toolkit usedToolkit = null;

	/**
	 * Sub-directory/package for the images
	 */
	/* [landrus, 26.11.09]: use classloader compatible paths */
	public final static String IMAGE_DIR = "/images/";

	public final static String ICON_DIR = "/icons/";

	public final static String TOOLBAR_ICON_DIR = "/icons/monitor_tool/toolbar/";
	
	/**
	 * Static singleton
	 */
	private ImageLoader() {
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
		String ext = "png";
		String fullImageName = imagename.endsWith(ext) ? imagename : imagename + "." + ext;
		ImageIcon found = iconCache.get(fullImageName);
		if (found == null) {
			String fileName = fullImageName.startsWith("/") ? fullImageName : "/" + fullImageName;

			/* [landrus, 26.11.09]: don't use the system classloader in a webstart env. */
			URL resource = ImageLoader.class.getResource(fileName);// ClassLoader.getSystemResource(fileName);
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
	 * @return ImageIcon containing image of specified name.
	 */
	public static ImageIcon getIcon(String imagename, String ext, String dir) {
		String fullImageName = imagename.endsWith(ext) ? imagename : imagename + "." + ext;
		ImageIcon found = iconCache.get(fullImageName);
		if (found == null) {
			String fileName = fullImageName.startsWith("/") ? fullImageName : dir + fullImageName;
			/* [landrus, 26.11.09]: don't use the system classloader in a webstart env. */
			URL resource = ImageLoader.class.getResource(fileName);// ClassLoader.getSystemResource(fileName);

			found = new ImageIcon(resource);

			iconCache.put(fullImageName, found);
		}

		return found;
	}

	/**
	 * Get an image with the specified name. The name should include the suffix
	 * identifying the format of the image.
	 *
	 * @param imagename
	 *            Name of image including suffix.
	 * @return Image found and loaded.
	 */
	public static Image getImage(String imagename) {
		Image newImage = imageCache.get(imagename);
		if (newImage == null) {

			if (usedToolkit == null) {
				usedToolkit = Toolkit.getDefaultToolkit();
			}
			/* [landrus, 26.11.09]: don't use the system classloader in a webstart env. */
			URL imageURL = ImageLoader.class.getResource(IMAGE_DIR + imagename);
			// ClassLoader.getSystemResource(IMAGE_DIR + imagename);

			newImage = usedToolkit.createImage(imageURL);
			imageCache.put(imagename, newImage);
		}
		return newImage;
	}
}