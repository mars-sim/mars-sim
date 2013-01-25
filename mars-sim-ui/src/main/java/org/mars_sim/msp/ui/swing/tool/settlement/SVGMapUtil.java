/**
 * Mars Simulation Project
 * SVGMapUtil.java
 * @version 3.04 2013-01-24
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
    private static Map<String, String> BUILDING_SVG_MAP;
    private static Map<String, String> CONSTRUCTION_SITE_MAP;
    private static Map<String, String> VEHICLE_SVG_MAP;
    private static Map<String, String> VEHICLE_MAINT_OVERLAY_SVG_MAP;
    private static Map<String, String> VEHICLE_LOADING_OVERLAY_SVG_MAP;
    private static Map<String, String> ATTACHMENT_PART_SVG_MAP;
    
    /**
     * Private constructor for utility class.
     */
    private SVGMapUtil() {}
    
    /**
     * Initializes the building SVG map.
     */
    private static void initializeBuildingSVGMap() {
        BUILDING_SVG_MAP = new HashMap<String, String>();
        
        // Add mapped building entries (do not include .svg suffix).
        BUILDING_SVG_MAP.put("lander hab", "lander_hab");
        BUILDING_SVG_MAP.put("inflatable greenhouse", "inflatable_greenhouse");
        BUILDING_SVG_MAP.put("md1 nuclear reactor", "md1_nuclear_reactor");
        BUILDING_SVG_MAP.put("starting erv base", "erv_base");
        BUILDING_SVG_MAP.put("erv base", "erv_base");
        BUILDING_SVG_MAP.put("regolith storage bin", "regolith_storage_bin");
        BUILDING_SVG_MAP.put("wind turbine", "wind_turbine");
        BUILDING_SVG_MAP.put("sand storage bin", "sand_storage_bin");
        BUILDING_SVG_MAP.put("lime storage bin", "lime_storage_bin");
        BUILDING_SVG_MAP.put("carbon storage bin", "carbon_storage_bin");
        BUILDING_SVG_MAP.put("cement storage bin", "cement_storage_bin"); // same image for concrete, cement and mortar
        BUILDING_SVG_MAP.put("mortar storage bin", "cement_storage_bin"); // same image for concrete, cement and mortar
        BUILDING_SVG_MAP.put("concrete storage bin", "cement_storage_bin"); // same image for concrete, cement and mortar
        BUILDING_SVG_MAP.put("atmospheric processor", "atmospheric_processor");
        BUILDING_SVG_MAP.put("residential quarters", "residential_quarters");
        BUILDING_SVG_MAP.put("lounge", "lounge");
        BUILDING_SVG_MAP.put("command and control", "command_and_control");
        BUILDING_SVG_MAP.put("infirmary", "infirmary");
        BUILDING_SVG_MAP.put("laboratory", "laboratory");
        BUILDING_SVG_MAP.put("workshop", "workshop");
        BUILDING_SVG_MAP.put("garage", "garage");
        BUILDING_SVG_MAP.put("large greenhouse", "large_greenhouse");
        BUILDING_SVG_MAP.put("astronomy observatory", "astronomy_observatory");
        BUILDING_SVG_MAP.put("md4 nuclear reactor", "md4_nuclear_reactor");
        BUILDING_SVG_MAP.put("storage hab", "storage_hab");
        BUILDING_SVG_MAP.put("residential hab", "residential_hab");
        BUILDING_SVG_MAP.put("medical hab", "medical_hab");
        BUILDING_SVG_MAP.put("research hab", "research_hab");
        BUILDING_SVG_MAP.put("machinery hab", "machinery_hab");
        BUILDING_SVG_MAP.put("solar photovoltaic array", "solar_photovoltaic_array");
        BUILDING_SVG_MAP.put("solar thermal array", "solar_thermal_array");
        BUILDING_SVG_MAP.put("small battery array", "small_battery_array");
        BUILDING_SVG_MAP.put("methane power generator", "methane_power_generator");
        BUILDING_SVG_MAP.put("small sabatier processor", "small_sabatier_processor");
        BUILDING_SVG_MAP.put("bunkhouse", "bunkhouse");
        BUILDING_SVG_MAP.put("manufacturing shed", "manufacturing_shed");
        BUILDING_SVG_MAP.put("small areothermal well", "small_areothermal_well");
        BUILDING_SVG_MAP.put("large areothermal well", "large_areothermal_well");
        BUILDING_SVG_MAP.put("storage shed", "storage_shed");
        BUILDING_SVG_MAP.put("outpost hub", "outpost_hub");
        BUILDING_SVG_MAP.put("loading dock garage", "loading_dock_garage");
        BUILDING_SVG_MAP.put("mining lab", "mining_lab");
        BUILDING_SVG_MAP.put("inground greenhouse", "inground_greenhouse");
    }
    
    /**
     * Initializes the construction site SVG map.
     */
    private static void initializeConstructionSiteSVGMap() {
        CONSTRUCTION_SITE_MAP = new HashMap<String, String>();
        
        // Add mapped construction site entries (do not include .svg suffix).
        CONSTRUCTION_SITE_MAP.put("residential quarters", "large_habitation_building_const");
        CONSTRUCTION_SITE_MAP.put("lounge", "large_habitation_building_const");
        CONSTRUCTION_SITE_MAP.put("command and control", "large_habitation_building_const");
        CONSTRUCTION_SITE_MAP.put("infirmary", "large_habitation_building_const");
        CONSTRUCTION_SITE_MAP.put("laboratory", "large_habitation_building_const");
        CONSTRUCTION_SITE_MAP.put("workshop", "large_habitation_building_const");
        CONSTRUCTION_SITE_MAP.put("large habitation frame", "large_habitation_frame_const");
        CONSTRUCTION_SITE_MAP.put("unprepared surface foundation", "unprepared_surface_foundation_const");
        CONSTRUCTION_SITE_MAP.put("garage", "garage_const");
        CONSTRUCTION_SITE_MAP.put("garage frame", "garage_frame_const");
        CONSTRUCTION_SITE_MAP.put("large greenhouse", "large_greenhouse_const");
        CONSTRUCTION_SITE_MAP.put("large greenhouse frame", "large_greenhouse_frame_const");
        CONSTRUCTION_SITE_MAP.put("astronomy observatory", "astronomy_observatory_const");
        CONSTRUCTION_SITE_MAP.put("astronomy observatory frame", "astronomy_observatory_frame_const");
        CONSTRUCTION_SITE_MAP.put("md4 nuclear reactor", "md4_nuclear_reactor_const");
        CONSTRUCTION_SITE_MAP.put("md4 nuclear reactor frame", "md4_nuclear_reactor_frame_const");
        CONSTRUCTION_SITE_MAP.put("md1 nuclear reactor", "md1_nuclear_reactor_const");
        CONSTRUCTION_SITE_MAP.put("md1 nuclear reactor frame", "md1_nuclear_reactor_frame_const");
        CONSTRUCTION_SITE_MAP.put("atmospheric processor", "atmospheric_processor_const");
        CONSTRUCTION_SITE_MAP.put("atmospheric processor frame", "atmospheric_processor_frame_const");
        CONSTRUCTION_SITE_MAP.put("erv base", "erv_base_const");
        CONSTRUCTION_SITE_MAP.put("starting erv base", "erv_base_const");
        CONSTRUCTION_SITE_MAP.put("erv base frame", "erv_base_frame_const");
        CONSTRUCTION_SITE_MAP.put("inflatable greenhouse", "inflatable_greenhouse_const");
        CONSTRUCTION_SITE_MAP.put("inflatable greenhouse frame", "inflatable_greenhouse_frame_const");
        CONSTRUCTION_SITE_MAP.put("lander hab", "hab_const");
        CONSTRUCTION_SITE_MAP.put("storage hab", "hab_const");
        CONSTRUCTION_SITE_MAP.put("research hab", "hab_const");
        CONSTRUCTION_SITE_MAP.put("residential hab", "hab_const");
        CONSTRUCTION_SITE_MAP.put("medical hab", "hab_const");
        CONSTRUCTION_SITE_MAP.put("machinery hab", "hab_const");
        CONSTRUCTION_SITE_MAP.put("lander hab frame", "hab_frame_const");
        CONSTRUCTION_SITE_MAP.put("solar photovoltaic array", "solar_photovoltaic_array_const");
        CONSTRUCTION_SITE_MAP.put("array frame", "array_frame_const");
        CONSTRUCTION_SITE_MAP.put("surface foundation 10m x 5m", "surface_foundation_10x5_const");
        CONSTRUCTION_SITE_MAP.put("solar thermal array", "solar_thermal_array_const");
        CONSTRUCTION_SITE_MAP.put("small battery array", "small_battery_array_const");
        CONSTRUCTION_SITE_MAP.put("methane power generator", "methane_power_generator_const");
        CONSTRUCTION_SITE_MAP.put("small steel frame", "small_steel_frame_const");
        CONSTRUCTION_SITE_MAP.put("surface foundation 5m x 5m", "surface_foundation_5x5_const");
        CONSTRUCTION_SITE_MAP.put("small sabatier processor", "small_sabatier_processor_const");
        CONSTRUCTION_SITE_MAP.put("wind turbine", "wind_turbine_const");
        CONSTRUCTION_SITE_MAP.put("steel frame tower", "steel_frame_tower_const");
        CONSTRUCTION_SITE_MAP.put("carbon storage bin", "storage_bin_const");
        CONSTRUCTION_SITE_MAP.put("cement storage bin", "storage_bin_const");
        CONSTRUCTION_SITE_MAP.put("sand storage bin", "storage_bin_const");
        CONSTRUCTION_SITE_MAP.put("lime storage bin", "storage_bin_const");
        CONSTRUCTION_SITE_MAP.put("mortar storage bin", "storage_bin_const");
        CONSTRUCTION_SITE_MAP.put("concrete storage bin", "storage_bin_const");
        CONSTRUCTION_SITE_MAP.put("regolith storage bin", "storage_bin_const");
        CONSTRUCTION_SITE_MAP.put("brick bin frame", "brick_bin_frame_const");
        CONSTRUCTION_SITE_MAP.put("surface foundation 3m x 2m", "surface_foundation_3x2_const");
        CONSTRUCTION_SITE_MAP.put("bunkhouse", "bunkhouse_const");
        CONSTRUCTION_SITE_MAP.put("manufacturing shed", "manufacturing_shed_const");
        CONSTRUCTION_SITE_MAP.put("small brick shed frame", "small_brick_shed_frame_const");
        CONSTRUCTION_SITE_MAP.put("subsurface foundation 5m x 5m x 3m", "subsurface_foundation_5x5x3_const");
        CONSTRUCTION_SITE_MAP.put("small areothermal well", "small_areothermal_well_const");
        CONSTRUCTION_SITE_MAP.put("small areothermal well frame", "small_areothermal_well_frame_const");
        CONSTRUCTION_SITE_MAP.put("shallow borehole drilling site", "shallow_borehole_drilling_site_const");
        CONSTRUCTION_SITE_MAP.put("large areothermal well", "large_areothermal_well_const");
        CONSTRUCTION_SITE_MAP.put("large areothermal well frame", "large_areothermal_well_frame_const");
        CONSTRUCTION_SITE_MAP.put("deep borehole drilling site", "deep_borehole_drilling_site_const");
        CONSTRUCTION_SITE_MAP.put("storage shed", "storage_shed_const");
        CONSTRUCTION_SITE_MAP.put("outpost hub", "outpost_hub_const");
        CONSTRUCTION_SITE_MAP.put("small vaulted brick frame", "small_vaulted_brick_frame_const");
        CONSTRUCTION_SITE_MAP.put("subsurface foundation 10m x 10m x 3m", "subsurface_foundation_10x10x3_const");
        CONSTRUCTION_SITE_MAP.put("loading dock garage", "loading_dock_garage_const");
        CONSTRUCTION_SITE_MAP.put("garage_brick_frame", "garage_brick_frame_const");
        CONSTRUCTION_SITE_MAP.put("ramped subsurface foundation 15m x 18m x 5m", "ramped_subsurface_foundation_15x18x5_const");
        CONSTRUCTION_SITE_MAP.put("mining log", "mining_lab_const");
        CONSTRUCTION_SITE_MAP.put("inground greenhouse", "inground_greenhouse_const");
        CONSTRUCTION_SITE_MAP.put("vaulted glass brick frame", "vaulted_glass_brick_frame_const");
        CONSTRUCTION_SITE_MAP.put("subsurface foundation 5m x 10m x 3m", "subsurface_foundation_5x10x3_const");
    }
    
    /**
     * Initializes the vehicle SVG map.
     */
    private static void initializeVehicleSVGMap() {
        VEHICLE_SVG_MAP = new HashMap<String, String>();
        
        // Add mapped vehicle entries (do not include .svg suffix).
        VEHICLE_SVG_MAP.put("explorer rover", "explorer_rover");
        VEHICLE_SVG_MAP.put("cargo rover", "cargo_rover");
        VEHICLE_SVG_MAP.put("transport rover", "transport_rover");
        VEHICLE_SVG_MAP.put("light utility vehicle", "light_utility_vehicle");
    }
    
    /**
     * Initializes the vehicle maintenance/repair overlay SVG map.
     */
    private static void initializeVehicleMaintOverlaySVGMap() {
        VEHICLE_MAINT_OVERLAY_SVG_MAP = new HashMap<String, String>();
        
        // Add mapped vehicle entries (do not include .svg suffix).
        VEHICLE_MAINT_OVERLAY_SVG_MAP.put("explorer rover", "explorer_rover_maint");
        VEHICLE_MAINT_OVERLAY_SVG_MAP.put("cargo rover", "cargo_rover_maint");
        VEHICLE_MAINT_OVERLAY_SVG_MAP.put("transport rover", "transport_rover_maint");
        VEHICLE_MAINT_OVERLAY_SVG_MAP.put("light utility vehicle", "light_utility_vehicle_maint");
    }
    
    /**
     * Initializes the vehicle loading/unloading overlay SVG map.
     */
    private static void initializeVehicleLoadingOverlaySVGMap() {
        VEHICLE_LOADING_OVERLAY_SVG_MAP = new HashMap<String, String>();
        
        // Add mapped vehicle entries (do not include .svg suffix).
        VEHICLE_LOADING_OVERLAY_SVG_MAP.put("explorer rover", "explorer_rover_load");
        VEHICLE_LOADING_OVERLAY_SVG_MAP.put("cargo rover", "cargo_rover_load");
        VEHICLE_LOADING_OVERLAY_SVG_MAP.put("transport rover", "transport_rover_load");
        // Note: not including a loading/unloading overlay for the light utility vehicle yet.  
        // May add it in the future.
    }
    
    /**
     * Initializes the attachment part SVG map.
     */
    private static void initializeAttachmentPartSVGMap() {
        ATTACHMENT_PART_SVG_MAP = new HashMap<String, String>();
        
        // Add mapped attachment part entries (do not include .svg suffix).
        ATTACHMENT_PART_SVG_MAP.put("bulldozer blade", "bulldozer_blade");
        ATTACHMENT_PART_SVG_MAP.put("soil compactor", "soil_compactor");
        ATTACHMENT_PART_SVG_MAP.put("backhoe", "backhoe");
        ATTACHMENT_PART_SVG_MAP.put("pneumatic drill", "pneumatic_drill");
        ATTACHMENT_PART_SVG_MAP.put("drilling rig", "drilling_rig");
        ATTACHMENT_PART_SVG_MAP.put("crane boom", "crane_boom");
    }
    
    /**
     * Gets a SVG node for a building.
     * @param buildingName the building's name.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getBuildingSVG(String buildingName) {
        if (BUILDING_SVG_MAP == null) initializeBuildingSVGMap();
        
        GraphicsNode result = null;
        
        String svgFileName = BUILDING_SVG_MAP.get(buildingName);
        if (svgFileName != null) result = SVGLoader.getSVGImage(svgFileName);
        
        return result;
    }
    
    /**
     * Gets a SVG node for a construction site.
     * @param constructionSiteStageName the construction site's current stage name.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getConstructionSiteSVG(String constructionSiteStageName) {
        if (CONSTRUCTION_SITE_MAP == null) initializeConstructionSiteSVGMap();
        
        GraphicsNode result = null;
        
        String svgFileName = CONSTRUCTION_SITE_MAP.get(constructionSiteStageName);
        if (svgFileName != null) result = SVGLoader.getSVGImage(svgFileName);
        
        return result;
    }
    
    /**
     * Gets a SVG node for a vehicle.
     * @param vehicleType the vehicle type.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getVehicleSVG(String vehicleType) {
        if (VEHICLE_SVG_MAP == null) initializeVehicleSVGMap();
        
        GraphicsNode result = null;
        
        String svgFileName = VEHICLE_SVG_MAP.get(vehicleType);
        if (svgFileName != null) result = SVGLoader.getSVGImage(svgFileName);
        
        return result;
    }
    
    /**
     * Gets a SVG node for a vehicle maintenance/repair overlay.
     * @param vehicleType the vehicle type.
     * @return SVG node of null if none found.
     */
    public static GraphicsNode getMaintenanceOverlaySVG(String vehicleType) {
        if (VEHICLE_MAINT_OVERLAY_SVG_MAP == null) initializeVehicleMaintOverlaySVGMap();
        
        GraphicsNode result = null;
        
        String svgFileName = VEHICLE_MAINT_OVERLAY_SVG_MAP.get(vehicleType);
        if (svgFileName != null) result = SVGLoader.getSVGImage(svgFileName);
        
        return result;
    }
    
    /**
     * Gets a SVG node for a vehicle loading/unloading overlay.
     * @param vehicleType the vehicle type.
     * @return SVG node of null if none found.
     */
    public static GraphicsNode getLoadingOverlaySVG(String vehicleType) {
        if (VEHICLE_LOADING_OVERLAY_SVG_MAP == null) initializeVehicleLoadingOverlaySVGMap();
        
        GraphicsNode result = null;
        
        String svgFileName = VEHICLE_LOADING_OVERLAY_SVG_MAP.get(vehicleType);
        if (svgFileName != null) result = SVGLoader.getSVGImage(svgFileName);
        
        return result;
    }
    
    /**
     * Gets a SVG node for an attachment part.
     * @param partType the part type.
     * @return SVG node or null if none found.
     */
    public static GraphicsNode getAttachmentPartSVG(String partType) {
        if (ATTACHMENT_PART_SVG_MAP == null) {
            initializeAttachmentPartSVGMap();
        }
        
        GraphicsNode result = null;
        
        String svgFileName = ATTACHMENT_PART_SVG_MAP.get(partType);
        if (svgFileName != null) result = SVGLoader.getSVGImage(svgFileName);
        
        return result;
    }
}