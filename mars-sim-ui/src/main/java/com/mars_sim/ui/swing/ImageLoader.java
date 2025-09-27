/*
 * Mars Simulation Project
 * ImageLoader.java
 * @date 2023-04-13
 * @author Barry Evans
 */
package com.mars_sim.ui.swing;

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

import com.mars_sim.ui.swing.tool.svg.SVGIcon;

/**
 * This is a static class that acts as a helper to load Images for use in the
 * UI. It is based on loading the resource form the class path via the
 * ClassLoader. However other alternative strategies can be easily 
 * implemented within this class.
 */
public class ImageLoader {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(ImageLoader.class.getName());

	private static final String COMMA = ",";
	private static final String FORWARD_SLASH = "/";
	private static final String BACK_SLASH = "\\";
	private static final String PNG24 = "_24.png";
	
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
			logger.severe("Can't load icons.properties.");
		}

		defaultIcon = getIconByName("unknown");
	}

	/**
	 * Static singleton
	 */
	private ImageLoader() {
	}

	/**
	 * Gets icon by its logical name. The logical name is mapped to a file 
	 * by the icon.properties or if not present the icon naming convention
	 * is applied.
	 * 
	 * @param iconName Logical name of icon. 
	 */
	public static Icon getIconByName(String iconName) {
		Icon found = iconByName.get(iconName);
		if (found == null) {
			// Is there a path already defined 
			String imagePath = iconPaths.getProperty(iconName);
			if (imagePath == null) {
				// No path, assume this is PNG
				imagePath = ICON_DIR + iconName + PNG24;
			}
			
			if (imagePath.endsWith(SVG)) {
				found = loadSVGIcon(imagePath, 0, 0);
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
	 * Returns the image icon of the filename.
	 * 
	 * @param fileName
	 * @return
	 */
	private static ImageIcon loadImageIcon(String fileName) {
		URL imageSource = ImageLoader.class.getResource(fileName);
		if (imageSource == null) {
			return null;
		}
		return new ImageIcon(imageSource);
	}

	/**
	 * Returns the SVG icon from a spec.
	 * 
	 * @param spec The spec that defines the icon size & filepath
	 * @param xx
	 * @param yy
	 * @return
	 */
	private static Icon loadSVGIcon(String spec, int xx, int yy) {
        SVGIcon newIcon = null;
        String filename = "";
        int x = xx;
        int y = yy;
        
        try {
			// SVG Spec is defined as "{x},{y},{filepath.svg} in icons.properties"
			if (spec.contains(COMMA)) {
				
	        	String []items = spec.split(COMMA);
	        	
	        	if (items.length == 2) {
					filename = items[1];
					if (xx == 0)
						x = Integer.parseInt(items[0]);	
					if (yy == 0)
						y = x;
	        	}
	        	else if (items.length == 3) {
					filename = items[2];
					if (xx == 0)
						x = Integer.parseInt(items[0]);	 
					if (yy == 0)
						y = Integer.parseInt(items[1]);	 
	        	}
			}
			
			else {
				filename = spec;
			}
			
			if (filename.startsWith(FORWARD_SLASH) || filename.startsWith(BACK_SLASH))
				filename = filename.substring(1, filename.length());
			
			// Note: using ImageLoader.class.getClassLoader() below requires removing
			// the forward slash first at the start of the filename
            URL resource = ImageLoader.class.getClassLoader().getResource(filename);
            newIcon = new SVGIcon(resource.toString(), x, y);
            
        } catch (TranscoderException e) {
			logger.severe("Can't transcode the specs into svg icon.");
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
				logger.severe("Can't read image URL.");
			}
			imageCache.put(imageName, newImage);
		}
		return newImage;
	}

	public void destroy() {
		iconByName.clear();
		iconByName = null;
		iconPaths= null;
		imageCache.clear();
		imageCache = null;
		defaultIcon= null;
	}
}
