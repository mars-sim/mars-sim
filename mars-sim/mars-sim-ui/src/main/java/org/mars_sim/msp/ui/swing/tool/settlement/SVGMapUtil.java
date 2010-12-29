/**
 * Mars Simulation Project
 * SVGMapUtil.java
 * @version 3.00 2010-11-13
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
        buildingSVGMap.put("lander hab", "lander_hab");
        buildingSVGMap.put("inflatable greenhouse", "inflatable_greenhouse");
        buildingSVGMap.put("md1 nuclear reactor", "md1_nuclear_reactor");
        buildingSVGMap.put("starting erv base", "erv_base");
        buildingSVGMap.put("erv base", "erv_base");
        buildingSVGMap.put("regolith storage bin", "regolith_storage_bin");
        buildingSVGMap.put("wind turbine", "wind_turbine");
        buildingSVGMap.put("sand storage bin", "sand_storage_bin");
        buildingSVGMap.put("lime storage bin", "lime_storage_bin");
        buildingSVGMap.put("carbon storage bin", "carbon_storage_bin");
        buildingSVGMap.put("cement storage bin", "cement_storage_bin"); // same image for concrete, cement and mortar
        buildingSVGMap.put("mortar storage bin", "cement_storage_bin"); // same image for concrete, cement and mortar
        buildingSVGMap.put("concrete storage bin", "cement_storage_bin"); // same image for concrete, cement and mortar
        buildingSVGMap.put("atmospheric processor", "atmospheric_processor");
        buildingSVGMap.put("residential quarters", "residential_quarters");
        buildingSVGMap.put("lounge", "lounge");
        buildingSVGMap.put("command and control", "command_and_control");
        buildingSVGMap.put("infirmary", "infirmary");
        buildingSVGMap.put("laboratory", "laboratory");
        buildingSVGMap.put("workshop", "workshop");
        buildingSVGMap.put("garage", "garage");
        buildingSVGMap.put("large greenhouse", "large_greenhouse");
        buildingSVGMap.put("astronomy observatory", "astronomy_observatory");
        buildingSVGMap.put("md4 nuclear reactor", "md4_nuclear_reactor");
        buildingSVGMap.put("storage hab", "storage_hab");
        // TODO: Add more mapped entities.
    }
    
    /**
     * Initializes the construction site SVG map.
     */
    private static void initializeConstructionSiteSVGMap() {
        constructionSiteSVGMap = new HashMap<String, String>();
        
        // Add mapped construction site entries (do not include .svg suffix).
        constructionSiteSVGMap.put("residential quarters", "large_habitation_building_const");
        constructionSiteSVGMap.put("lounge", "large_habitation_building_const");
        constructionSiteSVGMap.put("command and control", "large_habitation_building_const");
        constructionSiteSVGMap.put("infirmary", "large_habitation_building_const");
        constructionSiteSVGMap.put("laboratory", "large_habitation_building_const");
        constructionSiteSVGMap.put("workshop", "large_habitation_building_const");
        constructionSiteSVGMap.put("large habitation frame", "large_habitation_frame_const");
        constructionSiteSVGMap.put("unprepared surface foundation", "unprepared_surface_foundation_const");
        constructionSiteSVGMap.put("garage", "garage_const");
        constructionSiteSVGMap.put("garage frame", "garage_frame_const");
        constructionSiteSVGMap.put("large greenhouse", "large_greenhouse_const");
        constructionSiteSVGMap.put("large greenhouse frame", "large_greenhouse_frame_const");
        constructionSiteSVGMap.put("astronomy observatory", "astronomy_observatory_const");
        constructionSiteSVGMap.put("astronomy observatory frame", "astronomy_observatory_frame_const");
        constructionSiteSVGMap.put("md4 nuclear reactor", "md4_nuclear_reactor_const");
        constructionSiteSVGMap.put("md4 nuclear reactor frame", "md4_nuclear_reactor_frame_const");
        constructionSiteSVGMap.put("md1 nuclear reactor", "md1_nuclear_reactor_const");
        constructionSiteSVGMap.put("md1 nuclear reactor frame", "md1_nuclear_reactor_frame_const");
        constructionSiteSVGMap.put("atmospheric processor", "atmospheric_processor_const");
        constructionSiteSVGMap.put("atmospheric processor frame", "atmospheric_processor_frame_const");
        constructionSiteSVGMap.put("erv base", "erv_base_const");
        constructionSiteSVGMap.put("starting erv base", "erv_base_const");
        constructionSiteSVGMap.put("erv base frame", "erv_base_frame_const");
        constructionSiteSVGMap.put("inflatable greenhouse", "inflatable_greenhouse_const");
        constructionSiteSVGMap.put("inflatable greenhouse frame", "inflatable_greenhouse_frame_const");
        constructionSiteSVGMap.put("lander hab", "hab_const");
        constructionSiteSVGMap.put("storage hab", "hab_const");
        constructionSiteSVGMap.put("research hab", "hab_const");
        constructionSiteSVGMap.put("residential hab", "hab_const");
        constructionSiteSVGMap.put("medical hab", "hab_const");
        constructionSiteSVGMap.put("machinery hab", "hab_const");
        constructionSiteSVGMap.put("lander hab frame", "hab_frame_const");
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