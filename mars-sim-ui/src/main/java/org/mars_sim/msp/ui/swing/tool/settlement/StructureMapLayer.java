/**
 * Mars Simulation Project
 * StructureMapLayer.java
 * @version 3.01 2011-06-17
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.batik.gvt.GraphicsNode;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;

/**
 * A settlement map layer for displaying buildings and construction sites.
 */
public class StructureMapLayer implements SettlementMapLayer {

    // Static members
    private static final Color BUILDING_COLOR = Color.GREEN;
    private static final Color CONSTRUCTION_SITE_COLOR = Color.BLACK;
    
    // Data members
    private SettlementMapPanel mapPanel;
    private Map<Double, Map<GraphicsNode, BufferedImage>> svgImageCache;
    private double scale;
    
    /**
     * Constructor
     * @param mapPanel the settlement map panel.
     */
    public StructureMapLayer(SettlementMapPanel mapPanel) {
        
        // Initialize data members.
        this.mapPanel = mapPanel;
        svgImageCache = new HashMap<Double, Map<GraphicsNode, BufferedImage>>(21);
        
        // Set Apache Batik library system property so that it doesn't output: 
        // "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" in system err.
        System.setProperty("org.apache.batik.warn_destination", "false");
    }
    
    @Override
    public void displayLayer(Graphics2D g2d, Settlement settlement, double xPos, 
            double yPos, int mapWidth, int mapHeight, double rotation, double scale) {
        
        this.scale = scale;
        
        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        
        // Get the map center point.
        double mapCenterX = mapWidth / 2D;
        double mapCenterY = mapHeight / 2D;
        
        // Translate map from settlement center point.
        g2d.translate(mapCenterX + (xPos * scale), mapCenterY + (yPos * scale));
        
        // Rotate map from North.
        g2d.rotate(rotation, 0D - (xPos * scale), 0D - (yPos * scale));
        
        // Draw all buildings.
        drawBuildings(g2d, settlement);
        
        // Draw all construction sites.
        drawConstructionSites(g2d, settlement);
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
    
    /**
     * Draw all of the buildings in the settlement.
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
    private void drawBuildings(Graphics2D g2d, Settlement settlement) {
        if (settlement != null) {
            Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
            while (i.hasNext()) drawBuilding(i.next(), g2d);
        }
    }
    
    /**
     * Draws a building on the map.
     * @param building the building.
     * @param g2d the graphics context.
     */
    private void drawBuilding(Building building, Graphics2D g2d) {
        
        // Use SVG image for building if available.
        GraphicsNode svg = SVGMapUtil.getBuildingSVG(building.getName().toLowerCase());
        if (svg != null) {
            drawSVGStructure(g2d, building.getXLocation(), building.getYLocation(), 
                    building.getWidth(), building.getLength(), building.getFacing(), svg);
        }
        else {
            // Otherwise draw colored rectangle for building.
            drawRectangleStructure(g2d, building.getXLocation(), building.getYLocation(), 
                    building.getWidth(), building.getLength(), building.getFacing(), 
                    BUILDING_COLOR);
        }
    }
    
    /**
     * Draw all of the construction sites in the settlement.
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
    private void drawConstructionSites(Graphics2D g2d, Settlement settlement) {
        if (settlement != null) {
            Iterator<ConstructionSite> i = settlement.getConstructionManager().
                    getConstructionSites().iterator();
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
    	if (stage != null) {
    		svg = SVGMapUtil.getConstructionSiteSVG(stage.getInfo().getName().toLowerCase());
    	}
        if (svg != null) {
            drawSVGStructure(g2d, site.getXLocation(), site.getYLocation(), 
                    site.getWidth(), site.getLength(), site.getFacing(), svg);
        }
        else {
            // Else draw colored rectangle for construction site.
            drawRectangleStructure(g2d, site.getXLocation(), site.getYLocation(), 
                    site.getWidth(), site.getLength(), site.getFacing(), 
                    CONSTRUCTION_SITE_COLOR);
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
     */
    private void drawSVGStructure(Graphics2D g2d, double xLoc, double yLoc,
            double width, double length, double facing, GraphicsNode svg) {
        
        drawStructure(true, g2d, xLoc, yLoc, width, length, facing, svg, null);
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
    private void drawRectangleStructure(Graphics2D g2d, double xLoc, double yLoc, 
            double width, double length, double facing, Color color) {
        
        drawStructure(false, g2d, xLoc, yLoc, width, length, facing, null, color);
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
     * @param color the color to display the rectangle if no SVG image.
     */
    private void drawStructure(boolean isSVG, Graphics2D g2d, double xLoc, double yLoc,
            double width, double length, double facing, GraphicsNode svg, Color color) {
        
        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        
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
        
        // Apply graphic transforms for structure.
        AffineTransform newTransform = new AffineTransform();
        newTransform.translate(translationX, translationY);
        newTransform.rotate(facingRadian, centerX + boundsPosX, centerY + boundsPosY);
        
        if (isSVG) {
            // Draw SVG image.
            /*
            newTransform.scale(scalingWidth, scalingLength);
            svg.setTransform(newTransform);
            svg.paint(g2d);
            */
            
            // Draw buffered image of structure.
            BufferedImage image = getBufferedImage(svg, width, length);
            if (image != null) {
                g2d.transform(newTransform);
                g2d.drawImage(image, 0, 0, mapPanel);
            }
        }
        else {
            // Draw filled rectangle.
            newTransform.scale(scalingWidth, scalingLength);
            g2d.transform(newTransform);
            g2d.setColor(color);
            g2d.fill(bounds);
        }
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
    
    /**
     * Gets a buffered image for a given graphics node.
     * @param svg the graphics node.
     * @param width the structure width.
     * @param length the structure length.
     * @return buffered image.
     */
    private BufferedImage getBufferedImage(GraphicsNode svg, double width, double length) {
        
        // Get image cache for current scale or create it if it doesn't exist.
        Map<GraphicsNode, BufferedImage> imageCache = null;
        if (svgImageCache.containsKey(scale)) {
            imageCache = svgImageCache.get(scale);
        }
        else {
            imageCache = new HashMap<GraphicsNode, BufferedImage>(100);
            svgImageCache.put(scale, imageCache);
        }
        
        // Get image from image cache or create it if it doesn't exist.
        BufferedImage image = null;
        if (imageCache.containsKey(svg)) image = imageCache.get(svg);
        else {
            image = createBufferedImage(svg, (int) width, (int) length);
            imageCache.put(svg, image);
        }
        
        return image;
    }
    
    /**
     * Creates a buffered image from a SVG graphics node.
     * @param svg the SVG graphics node.
     * @param width the width of the produced image.
     * @param length the length of the produced image.
     * @return the created buffered image.
     */
    private BufferedImage createBufferedImage(GraphicsNode svg, int width, int length) {
        
        BufferedImage bufferedImage = new BufferedImage((int) (width * scale), (int) (length * scale), 
                BufferedImage.TYPE_INT_ARGB);
        
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

        // Cleanup and return image
        g2d.dispose();
        
        return bufferedImage;
    }

    @Override
    public void destroy() {
        // Clear all buffered image caches.
        Iterator<Map<GraphicsNode, BufferedImage>> i = svgImageCache.values().iterator();
        while (i.hasNext()) {
            i.next().clear();
        }
        svgImageCache.clear();
    }
}