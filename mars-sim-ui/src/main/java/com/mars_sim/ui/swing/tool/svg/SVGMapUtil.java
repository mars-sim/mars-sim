/*
 * Mars Simulation Project
 * SVGMapUtil.java
 * @date 2025-08-25
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.svg;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.batik.gvt.GraphicsNode;

/**
 * Static utility class for mapping settlement map structures, such
 * as buildings and construction sites, with their SVG image views
 * on the settlement map.
 */
public final class SVGMapUtil {

    // Static members.
	private static final String fileName = SVGLoader.SVG_DIR + "svg_image_mapping.properties";
	private static final Logger logger = Logger.getLogger(SVGMapUtil.class.getName());
    
	private static Properties svgMapProperties;

    /**
     * Private constructor for utility class.
     */
    private SVGMapUtil() {}

    /**
     * Loads the SVG image mapping properties file.
     */
    private static void loadSVGImageMappingPropertiesFile() {

        svgMapProperties = new Properties();

        URL resource = SVGLoader.class.getResource(fileName);
        try (InputStream inputStream = resource.openStream();) {
            svgMapProperties.load(inputStream);
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Problem reading SVG properties file", e);
        }
    }

    /**
     * Gets an SVG graphics node for a given map item name.
     * 
     * @param prefix the property key prefix (ex: building)
     * @param name the name of the map item.
     * @param optional Finding an image is optional
     * @return the SVG graphics node.
     */
    private static GraphicsNode getSVGGraphicsNode(String prefix, String name, boolean optional) {
        GraphicsNode result = null;

        // Load svgMapProperties from file if necessary.
        if (svgMapProperties == null) {
            loadSVGImageMappingPropertiesFile();
        }

        // Append property prefix.
        StringBuffer propertyNameBuff = new StringBuffer("");
        if (prefix != null) {
            propertyNameBuff.append(prefix);
            propertyNameBuff.append(".");
        }

        // Append name.
        if (name != null) {
            String prepName = name.trim().toLowerCase().replace(" ", "_");
            propertyNameBuff.append(prepName);
        }

        String propertyName = propertyNameBuff.toString();

        String svgFileName = svgMapProperties.getProperty(propertyName);
        if (svgFileName != null) {
            result = SVGLoader.getSVGImage(prefix, svgFileName);
        }
        else if (!optional) {
            logger.warning("No SVGImage found for property " + propertyNameBuff.toString());
        }

        return result;
    }

    /**
     * Gets a SVG node for a building.
     * 
     * @param buildingName the building's name.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getBuildingSVG(String buildingName) {

        return getSVGGraphicsNode("building", buildingName, false);
    }

    /**
     * Gets a SVG node for a building overlay pattern.
     * 
     * @param buildingName the building's name.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getBuildingPatternSVG(String buildingName) {

        return getSVGGraphicsNode("building.pattern", buildingName, true);
    }

    /**
     * Gets a SVG node for a construction site.
     * 
     * @param constructionSiteStageName the construction site's current stage name.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getConstructionSiteSVG(String constructionSiteStageName) {

        return getSVGGraphicsNode("construction_stage", constructionSiteStageName, false);
    }

    /**
     * Gets a SVG node for a construction site overlay pattern.
     * 
     * @param constructionSiteStageName the construction site's current stage name.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getConstructionSitePatternSVG(String constructionSiteStageName) {

        return getSVGGraphicsNode("construction_stage.pattern", constructionSiteStageName, true);
    }


    /**
     * Gets a SVG node for a person.
     * 
     * @param type the unit's type.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getUnitSVG(String type) {

        return getSVGGraphicsNode("unit", type, true);
    }

    
    /**
     * Gets a SVG node for a vehicle.
     * 
     * @param vehicleType the vehicle type.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getVehicleSVG(String vehicleType) {

        return getSVGGraphicsNode("vehicle", vehicleType, false);
    }

    /**
     * Gets a SVG node for a vehicle maintenance/repair overlay.
     * 
     * @param vehicleType the vehicle type.
     * @return SVG node of null if none found.
     */
    public static GraphicsNode getMaintenanceOverlaySVG(String vehicleType) {

        return getSVGGraphicsNode("vehicle.maintenance", vehicleType, false);
    }

    /**
     * Gets a SVG node for a vehicle loading/unloading overlay.
     * 
     * @param vehicleType the vehicle type.
     * @return SVG node of null if none found.
     */
    public static GraphicsNode getLoadingOverlaySVG(String vehicleType) {

        return getSVGGraphicsNode("vehicle.loading", vehicleType, false);
    }

    /**
     * Gets a SVG node for an attachment part.
     * 
     * @param partType the part type.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getAttachmentPartSVG(String partType) {

        return getSVGGraphicsNode("vehicle.attachment_part", partType, false);
    }

    /**
     * Gets a SVG node for a building connector.
     * 
     * @param connectorType the connector type.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getBuildingConnectorSVG(String connectorType) {

        return getSVGGraphicsNode("building_connector", connectorType, false);
    }
}
