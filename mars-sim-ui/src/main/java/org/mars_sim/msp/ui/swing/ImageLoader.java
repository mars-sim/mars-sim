/*
 * Mars Simulation Project
 * ImageLoader.java
 * @date 2022-08-03
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.batik.transcoder.TranscoderException;
import org.mars.sim.console.MarsTerminal;
import org.mars_sim.msp.ui.swing.tool.svg.SVGIcon;

/**
 * This is a static class that acts as a helper to load Images for use in the
 * UI. It is based on loading the resource form the class path via the
 * ClassLoader. However other alternative strategies can be easily 
 * implemented within this class.
 */
public class ImageLoader {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(ImageLoader.class.getName());

	private static Map<String, Icon> iconByName = new HashMap<>();
	private static Properties iconPaths;

	private static HashMap<String, ImageIcon> iconCache = new HashMap<>();
	private static HashMap<String, Image> imageCache = new HashMap<>();
	private static Toolkit usedToolkit = null;

	private static Icon defaultIcon;

	/**
	 * Sub-directory/package for the images.
	 */
	// Note: Switch to classloader compatible paths
	public static final String IMAGE_DIR = "/images/";
	public static final String MAPS_DIR = "/maps/";
	public static final String ICON_DIR = "/icons/";
	public static final String VEHICLE_ICON_DIR = "/icons/vehicle/";
	public static final String PNG = "png";
	public static final String SVG = "svg";
	public static final String SLASH = "/";
	
	static {
		iconPaths = new Properties();
		try (InputStream input = ImageLoader.class.getResourceAsStream("/icons.properties")) {
			iconPaths.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		defaultIcon = getIconByName("unknown");
	}

	/**
	 * Static singleton
	 */
	private ImageLoader() {
	}

	/**
	 * Get icon by it's logical name. The logicla name is mapped to a file 
	 * by the icon.properties or if not present the icon naming convention
	 * is applied.
	 * @param iconName Logical name of icon. 
	 */
	public static Icon getIconByName(String iconName) {
		Icon found = iconByName.get(iconName);
		if (found == null) {
			// Is there a path already defined 
			String imagePath = iconPaths.getProperty(iconName);
			if (imagePath == null) {
				// No path, assume this is PNG
				imagePath = ICON_DIR + iconName + "_24.png";
			}
			
			if (imagePath.endsWith(SVG)) {
				found = loadSVGIcon(imagePath);
			}
			else {
				found = loadImageIcon(imagePath);
			}

			// Display a default icon
			if(found == null) {
				found = defaultIcon;
			}
			iconByName.put(iconName, found);
		}

		return found;
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
			found = loadImageIcon(fileName);
			iconCache.put(fullImageName, found);
		}

		return found;
	}

	private static ImageIcon loadImageIcon(String fileName) {
		URL imageSource = ImageLoader.class.getResource(fileName);
		if (imageSource == null) {
			return null;
		}
		return new ImageIcon(imageSource);
	}

	/**
	 * Load an SVG icon from a spec.
	 * @param spec The spec defines the icon size & filepath
	 * @return
	 */
	private static Icon loadSVGIcon(String spec) {
        SVGIcon newIcon = null;
        try {
			// SVG Spec is defined as "<size>,<filepath .svg>"
			String []items = spec.split(",");
			String filename = items[1];
			int size = Integer.parseInt(items[0]);
            URL resource = ImageLoader.class.getClassLoader().getResource(filename);

            newIcon = new SVGIcon(resource.toString(), size, size);
        } catch (TranscoderException e) {
            e.printStackTrace();
        }

		return newIcon;
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
