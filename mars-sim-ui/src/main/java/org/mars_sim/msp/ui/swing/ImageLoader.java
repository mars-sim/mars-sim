/*
 * Mars Simulation Project
 * ImageLoader.java
 * @date 2022-08-03
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
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

	private static HashMap<String, Image> imageCache = new HashMap<>();

	private static Icon defaultIcon;

	/**
	 * Sub-directory/package for the images.
	 */
	// Note: Switch to classloader compatible paths
	private static final String IMAGE_DIR = "/images/";
	private static final String MAPS_DIR = "/maps/";
	private static final String ICON_DIR = "/icons/";
	private static final String SVG = "svg";
	
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
			InputStream imageURL = null;
			String imagePath = iconPaths.getProperty(imageName);
			if (imagePath != null) {
				imageURL = ImageLoader.class.getResourceAsStream(imagePath);
			}
			else {
				// Search for image
				imageURL = ImageLoader.class.getResourceAsStream(IMAGE_DIR + imageName);
				if (imageURL == null) {
					imageURL = ImageLoader.class.getResourceAsStream(ICON_DIR + imageName);
					if (imageURL == null) {					
						imageURL = ImageLoader.class.getResourceAsStream(MAPS_DIR + imageName);
					}		
				}
			}

			if (imageURL == null) {	
				logger.severe("'" + imageName + "' cannot be found");
			}

			// Read and load image
			try {
				newImage = ImageIO.read(imageURL);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
