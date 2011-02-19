/**
 * Mars Simulation Project
 * SettlementMapPanel.java
 * @version 3.00 2011-02-19
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import org.apache.batik.gvt.GraphicsNode;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.ui.swing.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A panel for displaying the settlement map.
 */
public class SettlementMapPanel extends JPanel {

    // Static members.
    public static final double DEFAULT_SCALE = 5D;
    public static final double MAX_SCALE = 55D;
    public static final double MIN_SCALE = 5D / 11D;
    private static final Color BUILDING_COLOR = Color.BLUE;
    private static final Color CONSTRUCTION_SITE_COLOR = Color.BLACK;
    private static final Color LABEL_COLOR = Color.BLUE;
    private static final Color MAP_BACKGROUND = new Color(181, 95, 0);
    private static final int MAX_BACKGROUND_IMAGE_NUM = 20;
    
    // Data members.
    private Settlement settlement;
    private double xPos;
    private double yPos;
    private double rotation;
    private double scale;
    private boolean showLabels;
    private Map<Settlement, String> settlementBackgroundMap;
    
    /**
     * A panel for displaying a settlement map.
     */
    public SettlementMapPanel() {
        // Use JPanel constructor.
        super();
        
        // Initialize data members.
        xPos = 0D;
        yPos = 0D;
        rotation = 0D;
        scale = DEFAULT_SCALE;
        settlement = null;
        showLabels = false;
        settlementBackgroundMap = new HashMap<Settlement, String>();
        
        // Set preferred size.
        setPreferredSize(new Dimension(400, 400));
        
        // Set foreground and background colors.
        setOpaque(true);
        setBackground(MAP_BACKGROUND);
        setForeground(Color.WHITE);
        
        // Set Apache Batik library system property so that it doesn't output: 
        // "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" in system err.
        System.setProperty("org.apache.batik.warn_destination", "false");
    }
    
    /**
     * Gets the settlement currently displayed.
     * @return settlement or null if none.
     */
    public Settlement getSettlement() {
        return settlement;
    }
    
    /**
     * Sets the settlement to display.
     * @param settlement the settlement.
     */
    public void setSettlement(Settlement settlement) {
        this.settlement = settlement;
        repaint();
    }
    
    /**
     * Gets the map scale.
     * @return scale (pixels per meter).
     */
    public double getScale() {
        return scale;
    }
    
    /**
     * Sets the map scale.
     * @param scale (pixels per meter).
     */
    public void setScale(double scale) {
        this.scale = scale;
        repaint();
    }
    
    /**
     * Gets the map rotation.
     * @return rotation (radians).
     */
    public double getRotation() {
        return rotation;
    }
    
    /**
     * Sets the map rotation.
     * @param rotation (radians).
     */
    public void setRotation(double rotation) {
        this.rotation = rotation;
        repaint();
    }
    /**
     * Resets the position, scale and rotation of the map.
     * Separate function that only uses one repaint.
     */
    public void reCenter() {        
        xPos = 0D;
        yPos = 0D;
        setRotation(0D);
        scale = DEFAULT_SCALE;
        repaint();
    }
    
    /**
     * Moves the center of the map by a given number of pixels.
     * @param xDiff the X axis pixels.
     * @param yDiff the Y axis pixels.
     */
    public void moveCenter(double xDiff, double yDiff) {
        xDiff /= scale;
        yDiff /= scale;
        
        // Correct due to rotation of map.
        double realXDiff = (Math.cos(rotation) * xDiff) + (Math.sin(rotation) * yDiff);
        double realYDiff = (Math.cos(rotation) * yDiff) - (Math.sin(rotation) * xDiff);
        
        xPos += realXDiff;
        yPos += realYDiff;
        repaint();
    }
    
    /**
     * Checks if labels should be displayed.
     * @return true if labels should be displayed.
     */
    public boolean isShowLabels() {
        return showLabels;
    }
    
    /**
     * Sets if labels should be displayed.
     * @param showLabels true if labels should be displayed.
     */
    public void setShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw background image tiles.
        drawBackgroundImageTiles(g2d);
        
        double mapCenterX = getWidth() / 2D;
        double mapCenterY = getHeight() / 2D;
        
        // Translate map from settlement center point.
        g2d.translate(xPos * scale, yPos * scale);
        
        // Rotate map from North.
        g2d.rotate(rotation, mapCenterX - (xPos * scale), mapCenterY - (yPos * scale));
        
        // Draw each building.
        drawBuildings(g2d);
        
        // Draw each construction site.
        drawConstructionSites(g2d);
    }
    
    /**
     * Draws the background image tiles on the map.
     */
    private void drawBackgroundImageTiles(Graphics2D g2d) {
        ImageIcon backgroundTileIcon = getBackgroundImage(settlement);
        if (backgroundTileIcon != null) {
            for (int x = 0; x < getWidth(); x+= backgroundTileIcon.getIconWidth()) {
                for (int y = 0; y < getHeight(); y+= backgroundTileIcon.getIconHeight()) {
                    g2d.drawImage(backgroundTileIcon.getImage(), x, y, this);
                }
            }
        }
    }
    
    /**
     * Gets the background tile image icon for a settlement.
     * @param settlement the settlement to display.
     * @return the background tile image icon or null if none found.
     */
    private ImageIcon getBackgroundImage(Settlement settlement) {
        ImageIcon result = null;
        
        if (settlementBackgroundMap.containsKey(settlement)) {
            String backgroundImageName = settlementBackgroundMap.get(settlement);
            result = ImageLoader.getIcon(backgroundImageName, "jpg");
        }
        else {
            int count = 1;
            Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
            while (i.hasNext()) {
                if (i.next().equals(settlement)) {
                    String backgroundImageName = "settlement_map_tile" + count;
                    settlementBackgroundMap.put(settlement, backgroundImageName);
                    result = ImageLoader.getIcon(backgroundImageName, "jpg");
                }
                count++;
                if (count > MAX_BACKGROUND_IMAGE_NUM) {
                    count = 1;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Draw all of the buildings in the settlement.
     * @param g2d the graphics context.
     */
    private void drawBuildings(Graphics2D g2d) {
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
        
        // Draw building label if displaying labels.
        if (showLabels) {
            drawLabel(g2d, building.getName(), building.getXLocation(), building.getYLocation());
        }
    }
    
    /**
     * Draw all of the construction sites in the settlement.
     * @param g2d the graphics context.
     */
    private void drawConstructionSites(Graphics2D g2d) {
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
        String stageName = site.getCurrentConstructionStage().getInfo().getName().toLowerCase();
        GraphicsNode svg = SVGMapUtil.getConstructionSiteSVG(stageName);
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
        
        // Draw construction site label if displaying labels.
        if (showLabels) {
            drawLabel(g2d, getConstructionLabel(site), site.getXLocation(), site.getYLocation());
        }
    }
    
    /**
     * Gets the label for a construction site.
     * @param site the construction site.
     * @return the construction label.
     */
    private String getConstructionLabel(ConstructionSite site) {
        String label = "";
        ConstructionStage stage = site.getCurrentConstructionStage();
        if (stage != null) {
            if (site.isUndergoingConstruction()) {
                label = "Constructing " + stage.getInfo().getName();
            }
            else if (site.isUndergoingSalvage()) {
                label = "Salvaging " + stage.getInfo().getName();
            }
            else if (site.hasUnfinishedStage()) {
                if (stage.isSalvaging()) {
                    label = "Salvaging " + stage.getInfo().getName() + " unfinished";
                }
                else {
                    label = "Constructing " + stage.getInfo().getName() + " unfinished";
                }
            }
            else {
                label = stage.getInfo().getName() + " completed";
            }
        }
        else {
            label = "No construction";
        }
        
        return label;
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
        double centerMapX = getWidth() / 2D;
        double centerMapY = getHeight() / 2D;
        double translationX = (-1D * xLoc * scale) - centerX - boundsPosX + centerMapX;
        double translationY = (-1D * yLoc * scale) - centerY - boundsPosY + centerMapY;
        double facingRadian = facing / 180D * Math.PI;
        
        // Apply graphic transforms for structure.
        AffineTransform newTransform = new AffineTransform();
        newTransform.translate(translationX, translationY);
        newTransform.rotate(facingRadian, centerX, centerY);
        newTransform.scale(scalingWidth, scalingLength);
        g2d.transform(newTransform);
        
        if (isSVG) {
            // Draw SVG image.
            svg.paint(g2d);
        }
        else {
            // Draw filled rectangle.
            g2d.setColor(color);
            g2d.fill(bounds);
        }
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
    
    /**
     * Draws a building or construction site label.
     * @param g2d the graphics 2D context.
     * @param label the label string.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     */
    private void drawLabel(Graphics2D g2d, String label, double xLoc, double yLoc) {
        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        
        // Determine bounds.
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD));
        FontMetrics metrics = g2d.getFontMetrics();
        double height = metrics.getLeading();
        double width = metrics.stringWidth(label);
        Rectangle2D bounds = new Rectangle2D.Double(width / -2D, height / -2D, width, height);
        
        // Determine transform information.
        double boundsPosX = bounds.getX() * scale;
        double boundsPosY = bounds.getY() * scale;
        double centerX = bounds.getWidth() * scale / 2D;
        double centerY = bounds.getHeight() * scale / 2D;
        double centerMapX = getWidth() / 2D;
        double centerMapY = getHeight() / 2D;
        double translationX = (-1D * xLoc * scale) - centerX - boundsPosX + centerMapX;
        double translationY = (-1D * yLoc * scale) - centerY - boundsPosY + centerMapY;
        
        // Apply graphic transforms for structure.
        AffineTransform newTransform = new AffineTransform();
        newTransform.translate(translationX, translationY);
        newTransform.rotate(rotation * -1D);
        g2d.transform(newTransform);
        
        // Draw label.
        g2d.setColor(LABEL_COLOR);
        g2d.drawString(label, (int) bounds.getX(), (int) bounds.getY());
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
}