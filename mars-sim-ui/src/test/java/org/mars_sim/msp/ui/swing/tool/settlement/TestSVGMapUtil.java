package org.mars_sim.msp.ui.swing.tool.settlement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.batik.gvt.GraphicsNode;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.construction.ConstructionConfig;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;

import junit.framework.TestCase;

/**
 * Unit test suite for the SVGMapUtil class.
 */
public class TestSVGMapUtil extends TestCase {

    @Override
    public void setUp() throws Exception {
        SimulationConfig.instance().loadConfig();
    }

    /**
     * Test the getBuildingSVG method.
     */
    public void testGetBuildingSVG() {
        
        // Check that all configured building names are mapped to a SVG image.
        Iterator<String> i = SimulationConfig.instance().getBuildingConfiguration().
                getBuildingTypes().iterator();
        while (i.hasNext()) {
            String buildingName = i.next();
            GraphicsNode svg = SVGMapUtil.getBuildingSVG(buildingName);
            assertNotNull(buildingName + " is not mapped to a SVG image.", svg);
        }
    }
    
    /**
     * Test the getConstructionSiteSVG method.
     */
    public void testGetConstructionSiteSVG() {
        
        // Check that all construction stage names are mapped to a SVG image.
        ConstructionConfig config = SimulationConfig.instance().getConstructionConfiguration();
        List<ConstructionStageInfo> constructionStages = new ArrayList<ConstructionStageInfo>();
        constructionStages.addAll(config.getConstructionStageInfoList(ConstructionStageInfo.FOUNDATION));
        constructionStages.addAll(config.getConstructionStageInfoList(ConstructionStageInfo.FRAME));
        constructionStages.addAll(config.getConstructionStageInfoList(ConstructionStageInfo.BUILDING));
        
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
        Iterator<String> i = SimulationConfig.instance().getVehicleConfiguration().
                getVehicleTypes().iterator();
        while (i.hasNext()) {
            String vehicleType = i.next();
            GraphicsNode svg = SVGMapUtil.getVehicleSVG(vehicleType);
            assertNotNull(vehicleType + " is not mapped to a SVG image.", svg);
        }
    }
    
    /**
     * Test the getMaintenanceOverlaySVG method.
     */
    public void testGetMaintenanceOverlaySVG() {
        
        // Check that all vehicle types have a maintenance overlay mapped to a SVG image.
        Iterator<String> i = SimulationConfig.instance().getVehicleConfiguration().
                getVehicleTypes().iterator();
        while (i.hasNext()) {
            String vehicleType = i.next();
            GraphicsNode svg = SVGMapUtil.getMaintenanceOverlaySVG(vehicleType);
            assertNotNull(vehicleType + " does not have a maintenance overlay mapped to a SVG image.", svg);
        }
    }
    
    /**
     * Test the getLoadingOverlaySVG method.
     */
    public void testGetLoadingOverlaySVG() {
        
        // Check that all vehicle types have a loading overlay mapped to a SVG image.
        Iterator<String> i = SimulationConfig.instance().getVehicleConfiguration().
                getVehicleTypes().iterator();
        while (i.hasNext()) {
            String vehicleType = i.next();
            if (!vehicleType.equalsIgnoreCase("Light Utility Vehicle")) {
                GraphicsNode svg = SVGMapUtil.getLoadingOverlaySVG(vehicleType);
                assertNotNull(vehicleType + " does not have a loading overlay mapped to a SVG image.", svg);
            }
        }
    }
    
    /**
     * Test the getAttachmentPartSVG method.
     */
    public void testGetAttachmentPartSVG() {
        
        // Check that all vehicle attachment parts are mapped to a SVG image.
        Iterator<Part> i = SimulationConfig.instance().getVehicleConfiguration().
                getAttachableParts("Light Utility Vehicle").iterator();
        while (i.hasNext()) {
            Part attachmentPart = i.next();
            GraphicsNode svg = SVGMapUtil.getAttachmentPartSVG(attachmentPart.getName());
            assertNotNull(attachmentPart.getName() + " is not mapped to a SVG image.", svg);
        }
    }
}