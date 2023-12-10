/**
 * Mars Simulation Project
 * StructureMapLayer.java
 * @date 2023-11-06
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.gvt.GraphicsNode;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.structure.building.connection.BuildingConnector;
import com.mars_sim.core.structure.building.connection.Hatch;
import com.mars_sim.core.structure.construction.ConstructionSite;
import com.mars_sim.core.structure.construction.ConstructionStage;
import com.mars_sim.mapdata.location.BoundedObject;
import com.mars_sim.mapdata.location.LocalBoundedObject;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;

/**
 * A settlement map layer for displaying buildings and construction sites.
 */
public class StructureMapLayer extends AbstractMapLayer {


	private static final String HATCH = "hatch";
	
    // Static members
    private static final Color BLDG_COLOR = Color.GREEN;
    
    private static final Color BLDG_SELECTED_COLOR = Color.WHITE;//new Color(119, 85, 0); // dark orange
    
    private static final Color CONST_COLOR = new Color(119, 59, 0); // dark orange
    private static final Color CONST_SELECTED_COLOR = new Color(119, 85, 0); // dark orange
    private static final Color CONST_BORDER_COLOR = Color.BLACK;

    private static final Color CONN_COLOR = Color.RED;
    private static final Color SPLIT_CONN_COLOR = Color.WHITE;

    private static final Font LABEL_FONT = new Font(Font.SERIF, Font.PLAIN, 10); // Note size doesn;t matter

    private static final float[] DASHES = {50.0f, 20.0f, 10.0f, 20.0f};
    
    // See https://docstore.mik.ua/orelly/java-ent/jfc/ch04_05.htm for instructions on BasicStroke
    private static final BasicStroke thinDash = new BasicStroke(2.0f,
    	      BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, DASHES, 0.0f);
    
    private static final BasicStroke thickDash = new BasicStroke(10.0f,
  	      BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 50.0f, DASHES, 0.0f);

    private static final Color BLACK_OUTLINE = new Color(0, 0, 0, 190);
	private static final Color WHITE_OUTLINE = new Color(255, 255, 255, 190);
	private static final Color GREY_OUTLINE = new Color(192, 192, 192, 190);
 	private static final ColorChoice BUILDING_COLOR = new ColorChoice(Color.gray.darker(), WHITE_OUTLINE);
	private static final ColorChoice CONSTRUCTION_COLOR = new ColorChoice(new Color(237, 114, 38), new Color(0, 0, 0, 150));

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
    // Data members
    private boolean selected = false;
        
    private SettlementMapPanel mapPanel;
    
    private Map<Double, Map<BuildingKey, BufferedImage>> svgImageCache;

    /**
     * Constructor 1.
     * 
     * @param mapPanel the settlement map panel.
     */
    public StructureMapLayer(SettlementMapPanel mapPanel) {

        // Initialize data members.
        this.mapPanel = mapPanel;
        svgImageCache = new HashMap<>();
    }

    @Override
    public void displayLayer(
            Graphics2D g2d, Settlement settlement, Building building, double xPos,
            double yPos, int mapWidth, int mapHeight, double rotation, double scale) {

    	Graphics2D g2d0 = g2d;

        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();

        // Translate map from settlement center point.
        g2d.translate(mapWidth / 2D + (xPos * scale), mapHeight / 2D + (yPos * scale));

        // Rotate map from North.
        g2d.rotate(rotation, 0D - (xPos * scale), 0D - (yPos * scale));

        // Discard the old scale value, compute a new value of scale.
        if (building != null) {
        	// Display a svg image for one single building
        	drawOneBuilding(building, g2d0);
        }

        else {
            if (settlement != null) {  
                boolean bldgLabels = mapPanel.isShowBuildingLabels();

                // Display svg images of all buildings in the entire settlement
                // Draw all buildings.
                for(Building b: settlement.getBuildingManager().getBuildingSet()) {
          	        drawBuilding(b, g2d, rotation, scale, bldgLabels);
                }

                // Draw all construction sites.
                boolean constLabels = mapPanel.isShowConstructionLabels();
                for(ConstructionSite c : settlement.getConstructionManager()
                                        .getConstructionSites()) {
                    drawConstructionSite(c, g2d, rotation, scale, constLabels);
                }

                // Draw all building connectors.
                drawBuildingConnectors(g2d, scale, settlement);
            }
	        // Restore original graphic transforms.
	        g2d.setTransform(saveTransform);
        }
    }

    public void drawOneBuilding(Building building, Graphics2D g2d) {

        GraphicsNode svg = SVGMapUtil.getBuildingSVG(building.getBuildingType().toLowerCase());
        if (svg != null) {
            // Determine building pattern SVG image if available.
            GraphicsNode patternSVG = SVGMapUtil.getBuildingPatternSVG(building.getBuildingType().toLowerCase());
            LocalBoundedObject newPosition = new BoundedObject(0, 0, building.getWidth(),
                                                                building.getLength(), building.getFacing());
            drawSVGStructure(g2d, 0, newPosition, svg, patternSVG);
        }
    }

    /**
     * Draws a building on the map.
     * 
     * @param building the building.
     * @param g2d the graphics context.
     */
    private void drawBuilding(Building building, Graphics2D g2d, double rotation, double scale, boolean showLabel) {

    	// Check if it's drawing the mouse-picked building 
        selected = building.equals(mapPanel.getSelectedBuilding());
    	
        // Use SVG image for building if available  		
        GraphicsNode svg = SVGMapUtil.getBuildingSVG(building.getBuildingType().toLowerCase());
        if (svg != null) {
            // Determine building pattern SVG image if available.
            GraphicsNode patternSVG = SVGMapUtil.getBuildingPatternSVG(building.getBuildingType().toLowerCase());

            drawSVGStructure(g2d, scale, building, svg, patternSVG);
        }
        else {
            // Otherwise draw colored rectangle for building.
            drawRectangleStructure(g2d, scale, building, BLDG_COLOR);
        }

        if (showLabel) {
            String[] words = building.getName().split(" ");
            ColorChoice frontColor = BUILDING_COLORS.getOrDefault(building.getCategory(), BUILDING_COLOR);

            drawCenteredMultiLabel(g2d, words, LABEL_FONT, building.getPosition(),
                frontColor, rotation, scale);
        }
    }

    /**
     * Draws a construction site on the map.
     * 
     * @param site the construction site.
     * @param g2d the graphics context.
     */
    private void drawConstructionSite(ConstructionSite site, Graphics2D g2d, double rotation, double scale,
                    boolean showLabel) {
        // Use SVG image for construction site if available.
        GraphicsNode svg = null;
        ConstructionStage stage = site.getCurrentConstructionStage();
        String stageName = null;
        if (stage != null) {
            stageName = stage.getInfo().getName().toLowerCase();
            svg = SVGMapUtil.getConstructionSiteSVG(stageName);
        }
        
        if (svg != null) {
            // Determine construction site pattern SVG image if available.
            GraphicsNode patternSVG = SVGMapUtil
                    .getConstructionSitePatternSVG(stageName);

            drawSVGStructure(g2d, scale, site, svg, patternSVG);
        }
        else {
        	Color color = (site.isMousePicked() ? CONST_SELECTED_COLOR : CONST_COLOR);
            drawRectangleStructure(g2d, scale, site, color);
        }

        
        if (showLabel) {
            String[] words = getConstructionLabel(site).split(" ");

            drawCenteredMultiLabel(g2d, words, LABEL_FONT, site.getPosition(),
                CONSTRUCTION_COLOR, rotation, scale);
        }
    }

    /**
	 * Gets the label for a construction site.
	 * 
	 * @param site the construction site.
	 * @return the construction label.
	 */
	public static String getConstructionLabel(ConstructionSite site) {
		String label = ""; //$NON-NLS-1$
		ConstructionStage stage = site.getCurrentConstructionStage();
		if (stage != null) {
			if (site.isUndergoingConstruction()) {
				label = Msg.getString("LabelMapLayer.constructing", stage.getInfo().getName()); //$NON-NLS-1$
			} else if (site.isUndergoingSalvage()) {
				label = Msg.getString("LabelMapLayer.salvaging", stage.getInfo().getName()); //$NON-NLS-1$
			} else if (site.hasUnfinishedStage()) {
				if (stage.isSalvaging()) {
					label = Msg.getString("LabelMapLayer.salvagingUnfinished", stage.getInfo().getName()); //$NON-NLS-1$
				} else {
					label = Msg.getString("LabelMapLayer.constructingUnfinished", stage.getInfo().getName()); //$NON-NLS-1$
				}
			} else {
				label = Msg.getString("LabelMapLayer.completed", stage.getInfo().getName()); //$NON-NLS-1$
			}
		} else {
			label = Msg.getString("LabelMapLayer.noConstruction"); //$NON-NLS-1$
		}
		return label;
	}

    /**
     * Draws all of the building connectors at the settlement.
     * 
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
    private synchronized void drawBuildingConnectors(Graphics2D g2d, double scale, Settlement settlement) {

        for(BuildingConnector c : settlement
                    .getBuildingConnectorManager()
                    .getAllBuildingConnections()) {
            drawBuildingConnector(c, g2d, scale);
        }
    }

    /**
     * Draws a building connector.
     * 
     * @param connector the building connector.
     * @param g2d the graphics context.
     */
    private void drawBuildingConnector(BuildingConnector connector, Graphics2D g2d, double scale) {

        if (connector.isSplitConnection()) {
            Hatch hatch1 = connector.getHatch1();
            Hatch hatch2 = connector.getHatch2();

            // Draw building connector area between two hatches.
            Path2D.Double splitAreaPath = new Path2D.Double();
            Point2D.Double leftHatch1Loc = LocalAreaUtil.getLocalRelativeLocation(hatch1.getWidth() / 2D, 0D, hatch1);
            splitAreaPath.moveTo(leftHatch1Loc.getX(), leftHatch1Loc.getY());
            Point2D.Double rightHatch2Loc = LocalAreaUtil.getLocalRelativeLocation(hatch2.getWidth() / -2D, 0D,  hatch2);
            splitAreaPath.lineTo(rightHatch2Loc.getX(), rightHatch2Loc.getY());
            Point2D.Double leftHatch2Loc = LocalAreaUtil.getLocalRelativeLocation(hatch2.getWidth() / 2D, 0D,  hatch2);
            splitAreaPath.lineTo(leftHatch2Loc.getX(), leftHatch2Loc.getY());
            Point2D.Double rightHatch1Loc = LocalAreaUtil.getLocalRelativeLocation(hatch1.getWidth() / -2D, 0D, hatch1);
            splitAreaPath.lineTo(rightHatch1Loc.getX(), rightHatch1Loc.getY());
            splitAreaPath.closePath();
            drawPathShape(splitAreaPath, g2d, scale, SPLIT_CONN_COLOR);

            // Use SVG image for hatch 1 if available.
            GraphicsNode hatch1SVG = SVGMapUtil.getBuildingConnectorSVG(HATCH);
            if (hatch1SVG != null) {

                // Draw hatch 1.
                drawSVGStructure(g2d, scale, hatch1, hatch1SVG, null);
            }
            else {
                // Otherwise draw colored rectangle for hatch 1.
                drawRectangleStructure(g2d, scale, hatch1, CONN_COLOR);
            }

            // Use SVG image for hatch 2 if available.
            GraphicsNode hatch2SVG = SVGMapUtil.getBuildingConnectorSVG(HATCH);
            if (hatch2SVG != null) {

                // Draw hatch 2.
                drawSVGStructure(g2d, scale, hatch2, hatch2SVG, null);
            }
            else {
                // Otherwise draw colored rectangle for hatch 2.
                drawRectangleStructure(g2d, scale, hatch2, CONN_COLOR);
            }
        }
        else {

            Hatch hatch = connector.getHatch1();

            // Use SVG image for hatch if available.
            GraphicsNode hatchSVG = SVGMapUtil.getBuildingConnectorSVG(HATCH);
            if (hatchSVG != null) {

                // Draw hatch.
                drawSVGStructure(g2d, scale, hatch, hatchSVG, null);
            }
            else {
                // Otherwise draw colored rectangle for hatch.
                drawRectangleStructure(g2d, scale, hatch, CONN_COLOR);
            }
        }
    }

    /**
     * Draws a structure as a SVG image on the map.
     * 
     * @param g2d the graphics2D context.
     * @param scale
     * @param placement Placement of structure
     * @param svg the SVG graphics node.
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
     */
    private void drawSVGStructure(
            Graphics2D g2d, double scale, LocalBoundedObject placement, GraphicsNode svg,
            GraphicsNode patternSVG) {
        drawStructure(true, g2d, scale, placement, svg, patternSVG, null);
    }

    /**
     * Draws a structure as a rectangle on the map.
     * 
     * @param g2d the graphics2D context.
     * @param scale
     * @param placement Placement of structure
     * @param color the color to draw the rectangle.
     */
    private void drawRectangleStructure(
            Graphics2D g2d, double scale, LocalBoundedObject placement, Color color) {
        drawStructure(false, g2d, scale, placement, null, null, color);
    }
    
    /**
     * Draws a structure on the map.
     * 
     * @param isSVG true if using a SVG image.
     * @param scale Scale
     * @param placement Placement of structure
     * @param svg the SVG graphics node.
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
     * @param color the color to display the rectangle if no SVG image.
     */
    private void drawStructure(
            boolean isSVG, Graphics2D g2d, double scale, LocalBoundedObject placement,
            GraphicsNode svg, GraphicsNode patternSVG, Color color) {

        double xLoc = placement.getXLocation();
        double yLoc = placement.getYLocation();
        double width = placement.getWidth();
        double length = placement.getLength();
        double facing = placement.getFacing();

        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        // Save original stroke
        Stroke oldStroke = g2d.getStroke();
   
        // Determine bounds.
        Rectangle2D bounds = null;
        if (isSVG) bounds = svg.getBounds();
        else bounds = new Rectangle2D.Double(0, 0, width, length);

        // Determine transform information.
        double scalingWidth = width / bounds.getWidth() * scale;
        double scalingLength = length / bounds.getHeight() * scale;
        double boundsPosX = bounds.getX() * scalingWidth;
        double boundsPosY = bounds.getY() * scalingLength;
        double centerX = width * scale / 2D;
        double centerY = length * scale / 2D;
        double translationX = (-1D * xLoc * scale) - centerX - boundsPosX;
        double translationY = (-1D * yLoc * scale) - centerY - boundsPosY;
        double facingRadian = facing / 180D * Math.PI;
  
        AffineTransform newTransform = new AffineTransform();
        
        if (isSVG) {
            // Draw buffered image of structure.
            BufferedImage image = getBufferedImage(svg, width, length, patternSVG, scale);
            if (image != null) {
                
                // Apply graphic transforms for structure.		
                newTransform.translate(translationX, translationY);
                newTransform.rotate(facingRadian, centerX + boundsPosX, centerY + boundsPosY);
            	
                g2d.transform(newTransform);
                
                if (mapPanel != null) {    
                	g2d.drawImage(image, 0, 0, mapPanel);
                }
            }
        }
        
        
        else {
            // Else draw colored rectangle for construction site.

            // Draw filled rectangle.
            newTransform.scale(scalingWidth, scalingLength);
            g2d.transform(newTransform);
            
            g2d.setColor(color);
            g2d.fill(bounds);
            
        	if (color.equals(CONST_SELECTED_COLOR)) {
                // Draw the dashed border
                g2d.setPaint(CONST_BORDER_COLOR);
                g2d.setStroke(thinDash);
                g2d.draw(bounds);
                g2d.setStroke(oldStroke);
        	}
        }

        if (selected) {   
            AffineTransform newTransform1 = new AffineTransform();
        	newTransform1.scale(scalingWidth, scalingLength);
            g2d.transform(newTransform1);
            
			// Draw the dashed border over the selected building
			g2d.setPaint(BLDG_SELECTED_COLOR);
			g2d.setStroke(thickDash);                                           
			g2d.draw(bounds);
			
			// Restore the stroke
			g2d.setStroke(oldStroke);
        }
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }

    /**
     * Draws a path shape on the map.
     * 
     * @param g2d the graphics2D context.
     * @param color the color to display the path shape.
     */
    private void drawPathShape(Path2D pathShape, Graphics2D g2d, double scale, Color color) {

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

    /**
     * Gets a buffered image for a given graphics node.
     * 
     * @param svg the SVG graphics node.
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
     * @return buffered image.
     */
    private BufferedImage getBufferedImage(
            GraphicsNode svg, double width, double length,
            GraphicsNode patternSVG, double scale) {

        // Get image cache for current scale or create it if it doesn't exist.
        Map<BuildingKey, BufferedImage> imageCache = null;
        if (svgImageCache.containsKey(scale)) {
            imageCache = svgImageCache.get(scale);
        }
        else {
            imageCache = new HashMap<>(100);
            svgImageCache.put(scale, imageCache);
        }

        // Get image from image cache or create it if it doesn't exist.
        BufferedImage image = null;
        BuildingKey buildingKey = new BuildingKey(svg, width, length);
        if (imageCache.containsKey(buildingKey)) {
            image = imageCache.get(buildingKey);
        }
        else {
            image = createBufferedImage(svg, width, length, patternSVG, scale);
            imageCache.put(buildingKey, image);
        }

        return image;
    }

    /**
     * Creates a buffered image from a SVG graphics node.
     * 
     * @param svg the SVG graphics node.
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
     * @return the created buffered image.
     */
    private BufferedImage createBufferedImage(GraphicsNode svg, double width, double length,
            GraphicsNode patternSVG, double scale) {

        int imageWidth = (int) (width * scale);
        if (imageWidth <= 0) {
            imageWidth = 1;
        }
        int imageLength = (int) (length * scale);
        if (imageLength <= 0) {
            imageLength = 1;
        }
        BufferedImage bufferedImage = new BufferedImage(
                imageWidth, imageLength,
                BufferedImage.TYPE_INT_ARGB
                );

        // Determine bounds.
        Rectangle2D bounds = svg.getBounds();

        // Determine transform information.
        double scalingWidth = width / bounds.getWidth() * scale;
        double scalingLength = length / bounds.getHeight() * scale;

        // Draw the SVG image on the buffered image.
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        svg.setTransform(AffineTransform.getScaleInstance(scalingWidth, scalingLength));
        svg.paint(g2d);

        // Draw repeating pattern SVG image on the buffered image.
        if (patternSVG != null) {
            double patternScaling;
            double patternWidth;
            double patternLength;

            double originalProportions = bounds.getWidth() / bounds.getHeight();
            double finalProportions = width / length;
            Rectangle2D patternBounds = patternSVG.getBounds();
            if ((finalProportions / originalProportions) >= 1D) {
                patternScaling = scalingLength;
                patternLength = length * (patternBounds.getHeight() / bounds.getHeight());
                patternWidth = patternLength * (patternBounds.getWidth() / patternBounds.getHeight());
            }
            else {
                patternScaling = scalingWidth;
                patternWidth = width * (patternBounds.getWidth() / bounds.getWidth());
                patternLength = patternWidth * (patternBounds.getHeight() / patternBounds.getWidth());
            }

            AffineTransform patternTransform = new AffineTransform();
            patternTransform.scale(patternScaling, patternScaling);
            for (double x = 0D; x < length; x += patternLength) {
                patternTransform.translate(0D, x * bounds.getHeight());
                double y = 0D;
                for (; y < width; y += patternWidth) {
                    patternTransform.translate(y * bounds.getWidth(), 0D);
                    patternSVG.setTransform(patternTransform);
                    patternSVG.paint(g2d);
                    patternTransform.translate(y * bounds.getWidth() * -1D, 0D);
                }
                patternTransform.translate(0D, x * bounds.getHeight() * -1D);
            }
        }

        // Cleanup and return image
        g2d.dispose();

        return bufferedImage;
    }
    
    @Override
    public void destroy() {
        svgImageCache.clear();
    }

    /**
     * Inner class to serve as map key for building images.
     */
    private class BuildingKey {

        private GraphicsNode svg;
        private double width;
        private double length;

        BuildingKey(GraphicsNode svg, double width, double length) {
            this.svg = svg;
            this.width = width;
            this.length = length;
        }

        @Override
        public boolean equals(Object object) {

            boolean result = false;
            if (object instanceof BuildingKey) {
                BuildingKey buildingKeyObject = (BuildingKey) object;
                if (svg.equals(buildingKeyObject.svg) &&
                        (width == buildingKeyObject.width) &&
                        (length == buildingKeyObject.length)) {
                    result = true;
                }
            }

            return result;
        }

        @Override
        public int hashCode() {
            return svg.hashCode() + (int) ((width + length) * 10D);
        }
    }
}
