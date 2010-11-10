/**
 * Mars Simulation Project
 * SVGMapUtil.java
 * @version 3.00 2010-10-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.util.HashMap;
import java.util.Map;

import org.apache.batik.gvt.GraphicsNode;
import org.mars_sim.msp.ui.swing.SVGLoader;

/**
 * Static utility class for mapping settlement map structures, such
 * as buildings and construction sites, with their SVG image views 
 * on the settlement map.
 */
public final class SVGMapUtil {

    // Static members.
    private static Map<String, String> buildingSVGMap;
    private static Map<String, String> constructionSiteSVGMap;
    
    /**
     * Private constructor for utility class.
     */
    private SVGMapUtil() {}
    
    /**
     * Initializes the building SVG map.
     */
    private static void initializeBuildingSVGMap() {
        buildingSVGMap = new HashMap<String, String>();
        
        // Add mapped building entries (do not include .svg suffix).
        buildingSVGMap.put("Lander Hab", "lander_hab");
        buildingSVGMap.put("Inflatable Greenhouse", "inflatable_greenhouse");
        buildingSVGMap.put("MD1 Nuclear Reactor", "md1_nuclear_reactor");
        buildingSVGMap.put("Starting ERV Base", "erv_base");
        buildingSVGMap.put("ERV Base", "erv_base");
        buildingSVGMap.put("Regolith Storage Bin", "regolith_storage_bin");
        buildingSVGMap.put("Wind Turbine", "wind_turbine");
        buildingSVGMap.put("Sand Storage Bin", "sand_storage_bin");
        buildingSVGMap.put("Lime Storage Bin", "lime_storage_bin");
        buildingSVGMap.put("Carbon Storage Bin", "carbon_storage_bin");
        buildingSVGMap.put("Cement Storage Bin", "cement_storage_bin"); // same image for concrete, cement and mortar
        buildingSVGMap.put("Mortar Storage Bin", "cement_storage_bin"); // same image for concrete, cement and mortar
        buildingSVGMap.put("Concrete Storage Bin", "cement_storage_bin"); // same image for concrete, cement and mortar
        buildingSVGMap.put("Atmospheric Processor", "atmospheric_processor");
        buildingSVGMap.put("Residential Quarters", "residential_quarters");
        buildingSVGMap.put("Lounge", "lounge");
        buildingSVGMap.put("Command and Control", "command_and_control");
        buildingSVGMap.put("Infirmary", "infirmary");
        buildingSVGMap.put("Laboratory", "laboratory");
        // TODO: Add more mapped entities.
    }
    
    /**
     * Initializes the construction site SVG map.
     */
    private static void initializeConstructionSiteSVGMap() {
        constructionSiteSVGMap = new HashMap<String, String>();
        
        // Add mapped construction site entries (do not include .svg suffix).
        // TODO: add more mapped entities.
    }
    
    /**
     * Gets a SVG node for a building.
     * @param buildingName the building's name.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getBuildingSVG(String buildingName) {
        if (buildingSVGMap == null) initializeBuildingSVGMap();
        
        GraphicsNode result = null;
        
        String svgFileName = buildingSVGMap.get(buildingName);
        if (svgFileName != null) result = SVGLoader.getSVGImage(svgFileName);
        
        return result;
    }
    
    /**
     * Gets a SVG node for a construction site.
     * @param constructionSiteStageName the construction site's current stage name.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getConstructionSiteSVG(String constructionSiteStageName) {
        if (constructionSiteSVGMap == null) initializeConstructionSiteSVGMap();
        
        GraphicsNode result = null;
        
        String svgFileName = constructionSiteSVGMap.get(constructionSiteStageName);
        if (svgFileName != null) result = SVGLoader.getSVGImage(svgFileName);
        
        return result;
    }
}