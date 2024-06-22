/**
 * Mars Simulation Project
 * BuildingMapLayer.java
 * @date 2023-12-13
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.apache.batik.gvt.GraphicsNode;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.structure.building.connection.BuildingConnector;
import com.mars_sim.core.structure.building.connection.Hatch;
import com.mars_sim.core.structure.building.function.ActivitySpot;
import com.mars_sim.core.structure.building.function.Function;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;

/**
 * A settlement map layer for displaying buildings and construction sites.
 */
public class BuildingMapLayer extends AbstractMapLayer {

	private static final String HATCH = "hatch";
	
    // Static members
    private static final Font SPOT_FONT = new Font("Arial", Font.PLAIN, 6); 
    private static final Color BLDG_COLOR = Color.GREEN;
    
    private static final Color BLDG_SELECTED_COLOR = Color.WHITE;//new Color(119, 85, 0); // dark orange

    private static final Color CONN_COLOR = Color.RED;
    private static final Color SPLIT_CONN_COLOR = Color.WHITE;

    private static final Font LABEL_FONT = new Font(Font.SERIF, Font.PLAIN, 10); // Note size doesn;t matter

    private static final Color BLACK_OUTLINE = new Color(0, 0, 0, 190);
	private static final Color WHITE_OUTLINE = new Color(255, 255, 255, 190);
	private static final Color GREY_OUTLINE = new Color(192, 192, 192, 190);
 	private static final ColorChoice BUILDING_COLOR = new ColorChoice(Color.gray.darker(), WHITE_OUTLINE);
    private static final ColorChoice SPOT_COLOR = new ColorChoice(Color.BLACK, WHITE_OUTLINE);

	private static final Map<BuildingCategory,ColorChoice> BUILDING_COLORS = new EnumMap<>(BuildingCategory.class);

	static {
		BUILDING_COLORS.put(BuildingCategory.WORKSHOP, new ColorChoice(new Color(195, 176, 145), BLACK_OUTLINE));
		BUILDING_COLORS.put(BuildingCategory.LABORATORY, new ColorChoice(new Color(51, 102, 153), WHITE_OUTLINE));
		BUILDING_COLORS.put(BuildingCategory.LIVING, new ColorChoice(new Color (236, 121, 154).darker(), WHITE_OUTLINE));
		BUILDING_COLORS.put(BuildingCategory.MEDICAL, new ColorChoice(new Color (51, 204, 255), WHITE_OUTLINE));
		BUILDING_COLORS.put(BuildingCategory.COMMAND, new ColorChoice(new Color(255, 102, 102).darker(), WHITE_OUTLINE));
		BUILDING_COLORS.put(BuildingCategory.VEHICLE, new ColorChoice(Color.yellow, GREY_OUTLINE));
		BUILDING_COLORS.put(BuildingCategory.HALLWAY, new ColorChoice(Color.gray, WHITE_OUTLINE));
		BUILDING_COLORS.put(BuildingCategory.FARMING, new ColorChoice(new Color (133, 187, 101), WHITE_OUTLINE));
		BUILDING_COLORS.put(BuildingCategory.PROCESSING, new ColorChoice(new Color (182, 201, 255), BLACK_OUTLINE));
		BUILDING_COLORS.put(BuildingCategory.POWER, new ColorChoice(new Color(174, 198, 207), BLACK_OUTLINE));
		BUILDING_COLORS.put(BuildingCategory.EVA_AIRLOCK, new ColorChoice(new Color (184, 134, 11), WHITE_OUTLINE));
		BUILDING_COLORS.put(BuildingCategory.ERV, new ColorChoice(new Color (83, 83, 83), WHITE_OUTLINE));
	}
        
    private SettlementMapPanel mapPanel;
    
    /**
     * Constructor 1.
     * 
     * @param mapPanel the settlement map panel.
     */
    public BuildingMapLayer(SettlementMapPanel mapPanel) {

        // Initialize data members.
        this.mapPanel = mapPanel;
    }

    @Override
    public void displayLayer(Settlement settlement, MapViewPoint viewpoint) {

        // Save original graphics transforms.
        AffineTransform saveTransform = viewpoint.prepareGraphics();

        if (settlement != null) {  
            boolean bldgLabels = mapPanel.isShowBuildingLabels();
            Set<FunctionType> spotLabels = mapPanel.getShowSpotLabels();

            // Display svg images of all buildings in the entire settlement
            // Draw all buildings.
            var buildings = settlement.getBuildingManager().getBuildingSet();
            for(Building b: buildings) {
                drawBuilding(b, bldgLabels, viewpoint);
            }

            // Draw all building connectors.
            drawBuildingConnectors(settlement, viewpoint);

            // Must draw spots last so they are on top of hatches
            if (!spotLabels.isEmpty()) {
                for(Building b: buildings) {
                    drawSpots(b, spotLabels, viewpoint);
                }
            }
        }
        // Restore original graphic transforms.
        viewpoint.graphics().setTransform(saveTransform);
    }

    /**
     * Draws a building on the map.
     * 
     * @param building the building.
     */
    private void drawBuilding(Building building, boolean showLabel, MapViewPoint viewpoint) {

    	// Check if it's drawing the mouse-picked building 
        Color selectedColor = (building.equals(mapPanel.getSelectedBuilding()) ? BLDG_SELECTED_COLOR : null);
    	
        // Use SVG image for building if available  		
        GraphicsNode svg = SVGMapUtil.getBuildingSVG(building.getBuildingType().toLowerCase());
        if (svg != null) {
            // Determine building pattern SVG image if available.
            GraphicsNode patternSVG = SVGMapUtil.getBuildingPatternSVG(building.getBuildingType().toLowerCase());
            drawStructure(building, svg, patternSVG, selectedColor, viewpoint);
        }
        else {
            // Otherwise draw colored rectangle for building.
            drawRectangle(building, BLDG_COLOR, selectedColor, viewpoint);
        }

        if (showLabel) {
            String[] words = building.getName().split(" ");
            ColorChoice frontColor = BUILDING_COLORS.getOrDefault(building.getCategory(), BUILDING_COLOR);

            drawCenteredMultiLabel(words, LABEL_FONT, building.getPosition(),
                                    frontColor,  viewpoint);
        }
    }

    /**
     * Draws the activity spots of a building.
     * 
     * @param building
     * @param showSpots
     */
    private void drawSpots(Building building, Set<FunctionType> showSpots, MapViewPoint viewpoint) {
        for(Function f : building.getFunctions()) {
            if (showSpots.contains(f.getFunctionType())) {
                for(ActivitySpot spot : f.getActivitySpots()) {
                    drawOval(spot.getPos(), SPOT_COLOR, viewpoint);
                    
                    drawRightLabel(false, spot.getName(), spot.getPos(), SPOT_COLOR,
                            SPOT_FONT, 1.5f, 0f, viewpoint);
                }
            }
        }
    }

    /**
     * Draws all of the building connectors at the settlement.
     * 
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
    private synchronized void drawBuildingConnectors(Settlement settlement, MapViewPoint viewpoint) {

        for(BuildingConnector c : settlement
                    .getBuildingConnectorManager()
                    .getAllBuildingConnections()) {
            drawBuildingConnector(c, viewpoint);
        }
    }

    /**
     * Draws a building connector.
     * 
     * @param connector the building connector.
     * @param g2d the graphics context.
     */
    private void drawBuildingConnector(BuildingConnector connector, MapViewPoint viewpoint) {

        if (connector.isSplitConnection()) {
            Hatch hatch1 = connector.getHatch1();
            Hatch hatch2 = connector.getHatch2();

            // Draw building connector area between two hatches.
            Path2D.Double splitAreaPath = new Path2D.Double();
            Point2D.Double leftHatch1Loc = LocalAreaUtil.convert2SettlementPos(hatch1.getWidth() / 2D, 0D, hatch1);
            splitAreaPath.moveTo(leftHatch1Loc.getX(), leftHatch1Loc.getY());
            Point2D.Double rightHatch2Loc = LocalAreaUtil.convert2SettlementPos(hatch2.getWidth() / -2D, 0D,  hatch2);
            splitAreaPath.lineTo(rightHatch2Loc.getX(), rightHatch2Loc.getY());
            Point2D.Double leftHatch2Loc = LocalAreaUtil.convert2SettlementPos(hatch2.getWidth() / 2D, 0D,  hatch2);
            splitAreaPath.lineTo(leftHatch2Loc.getX(), leftHatch2Loc.getY());
            Point2D.Double rightHatch1Loc = LocalAreaUtil.convert2SettlementPos(hatch1.getWidth() / -2D, 0D, hatch1);
            splitAreaPath.lineTo(rightHatch1Loc.getX(), rightHatch1Loc.getY());
            splitAreaPath.closePath();
            drawPathShape(splitAreaPath, SPLIT_CONN_COLOR, viewpoint);

            drawHatch(hatch1, viewpoint);
            drawHatch(hatch2, viewpoint);
        }
        else {
            Hatch hatch = connector.getHatch1();
            drawHatch(hatch, viewpoint);
        }
    }

    /**
     * Draws a Hatch on the map.
     * 
     * @param hatch The Hatch to draw
     */
    private void drawHatch(Hatch hatch, MapViewPoint viewpoint) {
        // Use SVG image for hatch if available.
        GraphicsNode hatchSVG = SVGMapUtil.getBuildingConnectorSVG(HATCH);
        if (hatchSVG != null) {
            // Draw hatch.
            drawStructure(hatch, hatchSVG, null, null, viewpoint);
        }
        else {
            // Otherwise draw colored rectangle for hatch.
            drawRectangle(hatch, CONN_COLOR, null, viewpoint);
        }
    }

    /**
     * Draws a path shape on the map.
     * 
     * @param pathShape The shape to draw
     * @param color the color to display the path shape.
     */
    private void drawPathShape(Path2D pathShape, Color color, MapViewPoint viewpoint) {

        var g2d = viewpoint.graphics();
        double scale = viewpoint.scale();

        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();

        // Determine bounds.
        Rectangle2D bounds = pathShape.getBounds2D();

        // Determine transform information.
        double boundsPosX = bounds.getX() * scale;
        double boundsPosY = bounds.getY() * scale;
        double centerX = bounds.getWidth() * scale / 2D;
        double centerY = bounds.getHeight() * scale / 2D;
        double translationX = (-1D * bounds.getCenterX() * scale) - centerX - boundsPosX;
        double translationY = (-1D * bounds.getCenterY() * scale) - centerY - boundsPosY;
        double facingRadian = Math.PI;

        // Apply graphic transforms for path shape.
        AffineTransform newTransform = new AffineTransform();
        newTransform.translate(translationX, translationY);
        newTransform.rotate(facingRadian, centerX + boundsPosX, centerY + boundsPosY);

        // Draw filled path shape.
        newTransform.scale(scale, scale);
        g2d.transform(newTransform);
        g2d.setColor(color);
        g2d.fill(pathShape);

        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
}