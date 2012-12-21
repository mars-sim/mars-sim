/**
 * Mars Simulation Project
 * SVGMapUtil.java
 * @version 3.04 2012-12-18
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
    private static Map<String, String> vehicleSVGMap;
    
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
        buildingSVGMap.put("residential hab", "residential_hab");
        buildingSVGMap.put("medical hab", "medical_hab");
        buildingSVGMap.put("research hab", "research_hab");
        buildingSVGMap.put("machinery hab", "machinery_hab");
        buildingSVGMap.put("solar photovoltaic array", "solar_photovoltaic_array");
        buildingSVGMap.put("solar thermal array", "solar_thermal_array");
        buildingSVGMap.put("small battery array", "small_battery_array");
        buildingSVGMap.put("methane power generator", "methane_power_generator");
        buildingSVGMap.put("small sabatier processor", "small_sabatier_processor");
        buildingSVGMap.put("bunkhouse", "bunkhouse");
        buildingSVGMap.put("manufacturing shed", "manufacturing_shed");
        buildingSVGMap.put("small areothermal well", "small_areothermal_well");
        buildingSVGMap.put("large areothermal well", "large_areothermal_well");
        buildingSVGMap.put("storage shed", "storage_shed");
        buildingSVGMap.put("outpost hub", "outpost_hub");
        buildingSVGMap.put("loading dock garage", "loading_dock_garage");
        buildingSVGMap.put("mining lab", "mining_lab");
        buildingSVGMap.put("inground greenhouse", "inground_greenhouse");
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
        constructionSiteSVGMap.put("solar photovoltaic array", "solar_photovoltaic_array_const");
        constructionSiteSVGMap.put("array frame", "array_frame_const");
        constructionSiteSVGMap.put("surface foundation 10m x 5m", "surface_foundation_10x5_const");
        constructionSiteSVGMap.put("solar thermal array", "solar_thermal_array_const");
        constructionSiteSVGMap.put("small battery array", "small_battery_array_const");
        constructionSiteSVGMap.put("methane power generator", "methane_power_generator_const");
        constructionSiteSVGMap.put("small steel frame", "small_steel_frame_const");
        constructionSiteSVGMap.put("surface foundation 5m x 5m", "surface_foundation_5x5_const");
        constructionSiteSVGMap.put("small sabatier processor", "small_sabatier_processor_const");
        constructionSiteSVGMap.put("wind turbine", "wind_turbine_const");
        constructionSiteSVGMap.put("steel frame tower", "steel_frame_tower_const");
        constructionSiteSVGMap.put("carbon storage bin", "storage_bin_const");
        constructionSiteSVGMap.put("cement storage bin", "storage_bin_const");
        constructionSiteSVGMap.put("sand storage bin", "storage_bin_const");
        constructionSiteSVGMap.put("lime storage bin", "storage_bin_const");
        constructionSiteSVGMap.put("mortar storage bin", "storage_bin_const");
        constructionSiteSVGMap.put("concrete storage bin", "storage_bin_const");
        constructionSiteSVGMap.put("regolith storage bin", "storage_bin_const");
        constructionSiteSVGMap.put("brick bin frame", "brick_bin_frame_const");
        constructionSiteSVGMap.put("surface foundation 3m x 2m", "surface_foundation_3x2_const");
        constructionSiteSVGMap.put("bunkhouse", "bunkhouse_const");
        constructionSiteSVGMap.put("manufacturing shed", "manufacturing_shed_const");
        constructionSiteSVGMap.put("small brick shed frame", "small_brick_shed_frame_const");
        constructionSiteSVGMap.put("subsurface foundation 5m x 5m x 3m", "subsurface_foundation_5x5x3_const");
        constructionSiteSVGMap.put("small areothermal well", "small_areothermal_well_const");
        constructionSiteSVGMap.put("small areothermal well frame", "small_areothermal_well_frame_const");
        constructionSiteSVGMap.put("shallow borehole drilling site", "shallow_borehole_drilling_site_const");
        constructionSiteSVGMap.put("large areothermal well", "large_areothermal_well_const");
        constructionSiteSVGMap.put("large areothermal well frame", "large_areothermal_well_frame_const");
        constructionSiteSVGMap.put("deep borehole drilling site", "deep_borehole_drilling_site_const");
        constructionSiteSVGMap.put("storage shed", "storage_shed_const");
        constructionSiteSVGMap.put("outpost hub", "outpost_hub_const");
        constructionSiteSVGMap.put("small vaulted brick frame", "small_vaulted_brick_frame_const");
        constructionSiteSVGMap.put("subsurface foundation 10m x 10m x 3m", "subsurface_foundation_10x10x3_const");
        constructionSiteSVGMap.put("loading dock garage", "loading_dock_garage_const");
        constructionSiteSVGMap.put("garage_brick_frame", "garage_brick_frame_const");
        constructionSiteSVGMap.put("ramped subsurface foundation 15m x 18m x 5m", "ramped_subsurface_foundation_15x18x5_const");
        constructionSiteSVGMap.put("mining log", "mining_lab_const");
        constructionSiteSVGMap.put("inground greenhouse", "inground_greenhouse_const");
        constructionSiteSVGMap.put("vaulted glass brick frame", "vaulted_glass_brick_frame_const");
        constructionSiteSVGMap.put("subsurface foundation 5m x 10m x 3m", "subsurface_foundation_5x10x3_const");
    }
    
    /**
     * Initializes the vehicle SVG map.
     */
    private static void initializeVehicleSVGMap() {
        vehicleSVGMap = new HashMap<String, String>();
        
        // Add mapped vehicle entries (do not include .svg suffix).
        vehicleSVGMap.put("explorer rover", "explorer_rover");
        vehicleSVGMap.put("cargo rover", "cargo_rover");
        vehicleSVGMap.put("transport rover", "transport_rover");
        vehicleSVGMap.put("light utility vehicle", "light_utility_vehicle");
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
    
    /**
     * Gets a SVG node for a vehicle.
     * @param vehicleType the vehicle type.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getVehicleSVG(String vehicleType) {
        if (vehicleSVGMap == null) initializeVehicleSVGMap();
        
        GraphicsNode result = null;
        
        String svgFileName = vehicleSVGMap.get(vehicleType);
        if (svgFileName != null) result = SVGLoader.getSVGImage(svgFileName);
        
        return result;
    }
}