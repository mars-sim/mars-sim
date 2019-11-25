/**
 * Mars Simulation Project
 * StructureMapLayer.java
 * @version 3.1.0 2017-04-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.batik.gvt.GraphicsNode;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnector;
import org.mars_sim.msp.core.structure.building.connection.Hatch;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;

/**
 * A settlement map layer for displaying buildings and construction sites.
 */
public class StructureMapLayer implements SettlementMapLayer {

    // Static members
    private static final Color BUILDING_COLOR = Color.GREEN;
    
    private static final Color SELECTED_BUILDING_BORDER_COLOR = Color.WHITE;//new Color(119, 85, 0); // dark orange
    
    private static final Color CONSTRUCTION_SITE_COLOR = new Color(119, 59, 0); // dark orange
    private static final Color SELECTED_CONSTRUCTION_SITE_COLOR = new Color(119, 85, 0); // dark orange
    
    private static final Color BUILDING_CONNECTOR_COLOR = Color.RED;
    private static final Color BUILDING_SPLIT_CONNECTOR_COLOR = Color.WHITE;

    private static final Color SITE_BORDER_COLOR = Color.BLACK;
    private final static float dash[] = { 1.0f };
    
    private final static BasicStroke dashed = new BasicStroke(0.2f,
    	      BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dash, 0.0f);
    
//    private final static BasicStroke THICK_DASHES = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
    
    private final static BasicStroke THICK_DASHES = new BasicStroke(2f,
  	      BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dash, 0.0f);
 
    
    // Data members
    private boolean selected = false;
    
    private double scale;
    private SettlementMapPanel mapPanel;
    private Map<Double, Map<BuildingKey, BufferedImage>> svgImageCache;

//	private Building building;

    /**
     * Constructor
     * @param mapPanel the settlement map panel.
     */
    public StructureMapLayer(SettlementMapPanel mapPanel) {

        // Initialize data members.
        this.mapPanel = mapPanel;
        svgImageCache = new HashMap<Double, Map<BuildingKey, BufferedImage>>(21);

        // Set Apache Batik library system property so that it doesn't output:
        // "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" in system err.
        System.setProperty("org.apache.batik.warn_destination", "false"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
	// Add this constructor for loading an svg image
	// for the selected building in unit window's building tab
    public StructureMapLayer(SettlementMapPanel mapPanel, Building building) {

//    	this.building = building;
//    	System.out.println("StructureMapLayer : building is "+ building);
        // Initialize data members.
        this.mapPanel = mapPanel;
        svgImageCache = new HashMap<Double, Map<BuildingKey, BufferedImage>>(21);

        // Set Apache Batik library system property so that it doesn't output:
        // "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" in system err.
        System.setProperty("org.apache.batik.warn_destination", "false"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public StructureMapLayer() {

        // Initialize data members.

        svgImageCache = new HashMap<Double, Map<BuildingKey, BufferedImage>>(21);

        // Set Apache Batik library system property so that it doesn't output:
        // "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" in system err.
        System.setProperty("org.apache.batik.warn_destination", "false"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override

 // 2014-11-04 Added new parameter building
    public void displayLayer(
            Graphics2D g2d, Settlement settlement, Building building, double xPos,
            double yPos, int mapWidth, int mapHeight, double rotation, double scale) {

    	Graphics2D g2d0 = g2d;
    	// value of scale came from paintComponent() in SettlementMapPanel.java
        this.scale = scale;
//        this.building = building;

        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();

        // Get the map center point.
        //double mapCenterX = mapWidth / 2D;
        //double mapCenterY = mapHeight / 2D;

        // Translate map from settlement center point.
        g2d.translate(mapWidth / 2D + (xPos * scale), mapHeight / 2D + (yPos * scale));

        // Rotate map from North.
        g2d.rotate(rotation, 0D - (xPos * scale), 0D - (yPos * scale));

        //2014-11-05 Added adjustScaleFactor()
        // discard the old scale value, compute a new value of scale.
    	//System.out.println("StructureMapLayer.java : displayLayer() : building is " + building);
        if (building != null) {
        	// Displaying a svg image for one single building
	        double width = building.getWidth();
	        double length = building.getLength();
	        scale = adjustScaleFactor(width, length);
        	drawOneBuilding(building, g2d0);
	    	//System.out.println("StructureMapLayer.java : displayLayer() : width is "+ width);
	      	//System.out.println("StructureMapLayer.java : displayLayer() : length is "+ length);
        }

        else {  // Displaying svg images of all buildings in the entire settlement
	        // Draw all buildings.
	        drawBuildings(g2d, settlement);

	        // Draw all construction sites.
	        drawConstructionSites(g2d, settlement);

	        // Draw all building connectors.
	        drawBuildingConnectors(g2d, settlement);

	        // Restore original graphic transforms.
	        g2d.setTransform(saveTransform);
        }
    }

	// 2014-11-04 Added drawOneBuilding() for displaying a building's svg image in unit window
    public void drawOneBuilding(Building building, Graphics2D g2d) {

        GraphicsNode svg = SVGMapUtil.getBuildingSVG(building.getBuildingType().toLowerCase());
        if (svg != null) {

            // Determine building pattern SVG image if available.
            GraphicsNode patternSVG = SVGMapUtil.getBuildingPatternSVG(building.getBuildingType().toLowerCase());

            drawSVGStructure(
                    g2d, 0.0, 0.0,
                    //g2d, building.getXLocation(), building.getYLocation(),
                    building.getWidth(), building.getLength(), building.getFacing(), svg, patternSVG
                    );
        }
    }

    /**
     * Draw all of the buildings in the settlement.
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
    private void drawBuildings(Graphics2D g2d, Settlement settlement) {
        if (settlement != null) {
            Iterator<Building> i = new ArrayList<>(settlement.getBuildingManager().getBuildings()).iterator();
            while (i.hasNext()) drawBuilding(i.next(), g2d);
        }
    }

    /**
     * Draws a building on the map.
     * @param building the building.
     * @param g2d the graphics context.
     */
    public void drawBuilding(Building building, Graphics2D g2d) {

    	// Check if it's drawing the mouse-picked building 
    	if (building.equals(mapPanel.getSelectedBuilding()))
   			selected = true;
    	else
    		selected = false;
    	
        // Use SVG image for building if available.
		// Need to STAY getName() or getBuildingType(), NOT changing to getNickName()
    	// or else svg for the building won't load up
        GraphicsNode svg = SVGMapUtil.getBuildingSVG(building.getBuildingType().toLowerCase());
        if (svg != null) {

            // Determine building pattern SVG image if available.
            GraphicsNode patternSVG = SVGMapUtil.getBuildingPatternSVG(building.getBuildingType().toLowerCase());

            drawSVGStructure(
                    g2d, building.getXLocation(), building.getYLocation(),
                    building.getWidth(), building.getLength(), building.getFacing(), svg, patternSVG
                    );
        }
        else {
            // Otherwise draw colored rectangle for building.
            drawRectangleStructure(
                    g2d, building.getXLocation(), building.getYLocation(),
                    building.getWidth(), building.getLength(), building.getFacing(),
                    BUILDING_COLOR
                    );
        }
    }

    /**
     * Draw all of the construction sites in the settlement.
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
    private void drawConstructionSites(Graphics2D g2d, Settlement settlement) {
        if (settlement != null) {
            Iterator<ConstructionSite> i = settlement
                    .getConstructionManager()
                    .getConstructionSites()
                    .iterator();
            while (i.hasNext()) drawConstructionSite(i.next(), g2d);
        }
    }

    /**
     * Draws a construction site on the map.
     * @param site the construction site.
     * @param g2d the graphics context.
     */
    private void drawConstructionSite(ConstructionSite site, Graphics2D g2d) {
        // Use SVG image for construction site if available.
        GraphicsNode svg = null;
        ConstructionStage stage = site.getCurrentConstructionStage();
        //System.out.println("stage is " + stage.toString());
        if (stage != null) {
            svg = SVGMapUtil.getConstructionSiteSVG(stage.getInfo().getName().toLowerCase());
        }
        
        if (svg != null) {
            // Determine construction site pattern SVG image if available.
            GraphicsNode patternSVG = SVGMapUtil
                    .getConstructionSitePatternSVG(
                            stage.getInfo().getName().toLowerCase()
                            );

            drawSVGStructure(
                    g2d, site.getXLocation(), site.getYLocation(),
                    site.getWidth(), site.getLength(), site.getFacing(), svg, patternSVG
                    );
        }
        else {
        	Color color = SELECTED_CONSTRUCTION_SITE_COLOR;
            // Else draw colored rectangle for construction site.
        	if (site.isMousePicked())
        		color = SELECTED_CONSTRUCTION_SITE_COLOR;
        	else
        		color = CONSTRUCTION_SITE_COLOR;
            drawRectangleStructure(
                    g2d, site.getXLocation(), site.getYLocation(),
                    site.getWidth(), site.getLength(), site.getFacing(),
                    color
                    );
        }
    }

    /**
     * Draws all of the building connectors at the settlement.
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
    private synchronized void drawBuildingConnectors(Graphics2D g2d, Settlement settlement) {

        if (settlement != null) {
            Iterator<BuildingConnector> i = settlement
                    .getBuildingConnectorManager()
                    .getAllBuildingConnections()
                    .iterator();
            while (i.hasNext()) {
                drawBuildingConnector(i.next(), g2d);
            }
        }
    }

    /**
     * Draws a building connector.
     * @param connector the building connector.
     * @param g2d the graphics context.
     */
    private void drawBuildingConnector(BuildingConnector connector, Graphics2D g2d) {

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
            drawPathShape(splitAreaPath, g2d, BUILDING_SPLIT_CONNECTOR_COLOR);

            // Use SVG image for hatch 1 if available.
            GraphicsNode hatch1SVG = SVGMapUtil.getBuildingConnectorSVG("hatch");
            if (hatch1SVG != null) {

                // Draw hatch 1.
                drawSVGStructure(g2d, hatch1.getXLocation(), hatch1.getYLocation(),
                        hatch1.getWidth(), hatch1.getLength(), hatch1.getFacing(), hatch1SVG, null);
            }
            else {
                // Otherwise draw colored rectangle for hatch 1.
                drawRectangleStructure(g2d, hatch1.getXLocation(), hatch1.getYLocation(),
                        hatch1.getWidth(), hatch1.getLength(), hatch1.getFacing(),
                        BUILDING_CONNECTOR_COLOR);
            }

            // Use SVG image for hatch 2 if available.
            GraphicsNode hatch2SVG = SVGMapUtil.getBuildingConnectorSVG("hatch");
            if (hatch2SVG != null) {

                // Draw hatch 2.
                drawSVGStructure(g2d, hatch2.getXLocation(), hatch2.getYLocation(),
                        hatch2.getWidth(), hatch2.getLength(), hatch2.getFacing(), hatch2SVG, null);
            }
            else {
                // Otherwise draw colored rectangle for hatch 2.
                drawRectangleStructure(g2d, hatch2.getXLocation(), hatch2.getYLocation(),
                        hatch2.getWidth(), hatch2.getLength(), hatch2.getFacing(),
                        BUILDING_CONNECTOR_COLOR);
            }
        }
        else {

            Hatch hatch = connector.getHatch1();

            // Use SVG image for hatch if available.
            GraphicsNode hatchSVG = SVGMapUtil.getBuildingConnectorSVG("hatch");
            if (hatchSVG != null) {

                // Draw hatch.
                drawSVGStructure(g2d, hatch.getXLocation(), hatch.getYLocation(),
                        hatch.getWidth(), hatch.getLength(), hatch.getFacing(), hatchSVG, null);
            }
            else {
                // Otherwise draw colored rectangle for hatch.
                drawRectangleStructure(g2d, hatch.getXLocation(), hatch.getYLocation(),
                        hatch.getWidth(), hatch.getLength(), hatch.getFacing(),
                        BUILDING_CONNECTOR_COLOR);
            }
        }
    }

    /**
     * Draws a structure as a SVG image on the map.
     * @param g2d the graphics2D context.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param facing the structure facing (degrees from North clockwise).
     * @param svg the SVG graphics node.
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
     */
    private void drawSVGStructure(
            Graphics2D g2d, double xLoc, double yLoc,
            double width, double length, double facing, GraphicsNode svg,
            GraphicsNode patternSVG) {
        drawStructure(true, g2d, xLoc, yLoc, width, length, facing, svg, patternSVG, null);
    }

    /**
     * Draws a structure as a rectangle on the map.
     * @param g2d the graphics2D context.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param facing the structure facing (degrees from North clockwise).
     * @param color the color to draw the rectangle.
     */
    private void drawRectangleStructure(
            Graphics2D g2d, double xLoc, double yLoc,
            double width, double length, double facing, Color color) {
        drawStructure(false, g2d, xLoc, yLoc, width, length, facing, null, null, color);
    }

	//2014-11-04 Added adjustScaleFactor() to maximize the display size of
    // the SVG Image of all buildings.
	public double adjustScaleFactor(double x, double y)  {
		double largerValue = 0;
		if (x >= y ) largerValue = x;
		else largerValue = y;
		scale = 100.0/largerValue * 0.9;

		return scale;
	}
    /**
     * Draws a structure on the map.
     * @param isSVG true if using a SVG image.
     * @param g2d the graphics2D context.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param facing the structure facing (degrees from North clockwise).
     * @param svg the SVG graphics node.
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
     * @param color the color to display the rectangle if no SVG image.
     */
    private void drawStructure(
            boolean isSVG, Graphics2D g2d, double xLoc, double yLoc,
            double width, double length, double facing, GraphicsNode svg,
            GraphicsNode patternSVG, Color color) {

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
        AffineTransform newTransform1 = new AffineTransform();
        
        // Apply graphic transforms for structure.		
        newTransform.translate(translationX, translationY);
        newTransform.rotate(facingRadian, centerX + boundsPosX, centerY + boundsPosY);
    
        if (isSVG) {
            // Draw buffered image of structure.
            BufferedImage image = getBufferedImage(svg, width, length, patternSVG);
            if (image != null) {
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
            
        	if (color.equals(SELECTED_CONSTRUCTION_SITE_COLOR)) {
                // Draw the dashed border
                g2d.setPaint(SITE_BORDER_COLOR);
                g2d.setStroke(dashed);
                g2d.draw(bounds);
                g2d.setStroke(oldStroke);
        	}
        }

        if (selected) {
	
        	newTransform1.scale(scalingWidth, scalingLength);
            g2d.transform(newTransform1);
         
			// Draw the dashed border over the selected building
			g2d.setPaint(SELECTED_BUILDING_BORDER_COLOR);
			g2d.setStroke(THICK_DASHES);                                           
			g2d.draw(bounds);
			// Restore the stroke
			g2d.setStroke(oldStroke);
        }
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }

    /**
     * Draws a path shape on the map.
     * @param g2d the graphics2D context.
     * @param color the color to display the path shape.
     */
    private void drawPathShape(Path2D pathShape, Graphics2D g2d, Color color) {

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
     * @param svg the SVG graphics node.
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
     * @return buffered image.
     */
    private BufferedImage getBufferedImage(
            GraphicsNode svg, double width, double length,
            GraphicsNode patternSVG) {

        // Get image cache for current scale or create it if it doesn't exist.
        Map<BuildingKey, BufferedImage> imageCache = null;
        if (svgImageCache.containsKey(scale)) {
            imageCache = svgImageCache.get(scale);
        }
        else {
            imageCache = new HashMap<BuildingKey, BufferedImage>(100);
            svgImageCache.put(scale, imageCache);
        }

        // Get image from image cache or create it if it doesn't exist.
        BufferedImage image = null;
        BuildingKey buildingKey = new BuildingKey(svg, width, length);
        if (imageCache.containsKey(buildingKey)) {
            image = imageCache.get(buildingKey);
        }
        else {
            image = createBufferedImage(svg, width, length, patternSVG);
            imageCache.put(buildingKey, image);
        }

        return image;
    }

    /**
     * Creates a buffered image from a SVG graphics node.
     * @param svg the SVG graphics node.
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
     * @return the created buffered image.
     */
    private BufferedImage createBufferedImage(GraphicsNode svg, double width, double length,
            GraphicsNode patternSVG) {

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
            double patternScaling = 0D;
            double patternWidth = 0D;
            double patternLength = 0D;

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

    public void setSelected(boolean value) {
    	selected = value;
    }
    
    @Override
    public void destroy() {
        // Clear all buffered image caches.
        Iterator<Map<BuildingKey, BufferedImage>> i = svgImageCache.values().iterator();
        while (i.hasNext()) {
            i.next().clear();
        }
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
                if (
                        svg.equals(buildingKeyObject.svg) &&
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