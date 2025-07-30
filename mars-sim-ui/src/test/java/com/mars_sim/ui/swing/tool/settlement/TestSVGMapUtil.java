package com.mars_sim.ui.swing.tool.settlement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.batik.gvt.GraphicsNode;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.construction.ConstructionConfig;
import com.mars_sim.core.building.construction.ConstructionStageInfo;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.vehicle.VehicleSpec;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;

import junit.framework.TestCase;

/**
 * Unit test suite for the SVGMapUtil class.
 */
public class TestSVGMapUtil extends TestCase {

    private SimulationConfig config;

    @Override
    public void setUp() {
    	config = SimulationConfig.loadConfig();
    }

    /**
     * Test the getBuildingSVG method.
     */
    public void testGetBuildingSVG() {
        
        // Check that all configured building names are mapped to a SVG image.
        for(var bs : config.getBuildingConfiguration().getBuildingSpecs()) {
            GraphicsNode svg = SVGMapUtil.getBuildingSVG(bs.getName());
            assertNotNull(bs.getName() + " is not mapped to a SVG image.", svg);
        }
    }
    
    /**
     * Test the getConstructionSiteSVG method.
     */
    public void testGetConstructionSiteSVG() {
        
        // Check that all construction stage names are mapped to a SVG image.
        ConstructionConfig cConfig = config.getConstructionConfiguration();
        List<ConstructionStageInfo> constructionStages = new ArrayList<ConstructionStageInfo>();
        constructionStages.addAll(cConfig.getConstructionStageInfoList(ConstructionStageInfo.Stage.FOUNDATION));
        constructionStages.addAll(cConfig.getConstructionStageInfoList(ConstructionStageInfo.Stage.FRAME));
        constructionStages.addAll(cConfig.getConstructionStageInfoList(ConstructionStageInfo.Stage.BUILDING));
        
        Iterator<ConstructionStageInfo> i = constructionStages.iterator();
        while (i.hasNext()) {
            ConstructionStageInfo constStageInfo = i.next();
            String constName = constStageInfo.getName();
            GraphicsNode svg = SVGMapUtil.getConstructionSiteSVG(constName);
            assertNotNull(constName + " is not mapped to a SVG image.", svg);
        }
    }
    
    /**
     * Test the getVehicleSVG method.
     */
    public void testGetVehicleSVG() {
        
        // Check that all vehicle types are mapped to a SVG image.
        for(VehicleSpec vs :  config.getVehicleConfiguration().getVehicleSpecs()) {
            GraphicsNode svg = SVGMapUtil.getVehicleSVG(vs.getBaseImage());
            assertNotNull(vs.getType().getName() + " is not mapped to a SVG image.", svg);
        }
    }
    
    /**
     * Test the getMaintenanceOverlaySVG method.
     */
    public void testGetMaintenanceOverlaySVG() {
        
        // Check that all vehicle types have a maintenance overlay mapped to a SVG image.
        for(VehicleSpec vs :  config.getVehicleConfiguration().getVehicleSpecs()) {
            VehicleType type = vs.getType();
            GraphicsNode svg = SVGMapUtil.getMaintenanceOverlaySVG(type.name());
            assertNotNull(type + " does not have a maintenance overlay mapped to a SVG image.", svg);
        }
    }
    
    /**
     * Test the getLoadingOverlaySVG method.
     */
    public void testGetLoadingOverlaySVG() {
        
        // Check that all vehicle types have a loading overlay mapped to a SVG image.
        for(VehicleSpec vs :  config.getVehicleConfiguration().getVehicleSpecs()) {
            VehicleType type = vs.getType();
            if (type != VehicleType.LUV) {
                GraphicsNode svg = SVGMapUtil.getLoadingOverlaySVG(type.name());
                assertNotNull(type + " does not have a loading overlay mapped to a SVG image.", svg);
            }
        }
    }
    
    /**
     * Test the getAttachmentPartSVG method.
     */
    public void testGetAttachmentPartSVG() {
        
        // Check that all vehicle attachment parts are mapped to a SVG image.
        Iterator<Part> i = config.getVehicleConfiguration().
                getVehicleSpec("Light Utility Vehicle").getAttachableParts().iterator();
        while (i.hasNext()) {
            Part attachmentPart = i.next();
            GraphicsNode svg = SVGMapUtil.getAttachmentPartSVG(attachmentPart.getName());
            assertNotNull(attachmentPart.getName() + " is not mapped to a SVG image.", svg);
        }
    }
}