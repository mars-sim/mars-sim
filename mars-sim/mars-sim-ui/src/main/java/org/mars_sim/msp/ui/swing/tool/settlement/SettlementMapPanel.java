/**
 * Mars Simulation Project
 * SettlementMapPanel.java
 * @version 3.00 2010-09-30
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.JPanel;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;


public class SettlementMapPanel extends JPanel {

    // Static members.
    private static final double DEFAULT_SCALE = 5D;
    
    // Data members.
    private Settlement settlement;
    private double xPos;
    private double yPos;
    private double rotation;
    private double scale;
    
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
        
        // Set preferred size.
        setPreferredSize(new Dimension(400, 400));
        
        // Set foreground and background colors.
        setOpaque(true);
        setBackground(new Color(54, 13, 0));
        setForeground(Color.WHITE);
    }
    
    /**
     * Sets the settlement to display.
     * @param settlement the settlement.
     */
    public void setSettlement(Settlement settlement) {
        this.settlement = settlement;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        //g2d.drawString("test", getWidth() / 2, getHeight() / 2);
        
        // Translate map from settlement center point.
        g2d.translate(xPos * scale, yPos * scale);
        
        // Rotate map from North.
        g2d.rotate(rotation, xPos * scale, yPos * scale);
        
        // Draw each building.
        drawBuildings(g2d);
        
        // Draw each construction site.
        drawConstructionSites(g2d);
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
        
        // TODO: Use SVG image for building if available.
        
        drawRectangleStructure(g2d, building.getXLocation(), building.getYLocation(), 
                building.getWidth(), building.getLength(), building.getFacing(), Color.BLUE);
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
        // TODO: Use SVG image for construction site if available.
        
        drawRectangleStructure(g2d, site.getXLocation(), site.getYLocation(), 
                site.getWidth(), site.getLength(), site.getFacing(), Color.BLACK);
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
        
        AffineTransform saveTransform = g2d.getTransform();
        
        Rectangle2D bounds = new Rectangle2D.Double(0, 0, width, length);
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
        
        AffineTransform newTransform = new AffineTransform();
        newTransform.translate(translationX, translationY);
        newTransform.rotate(facingRadian, centerX, centerY);
        newTransform.scale(scalingWidth, scalingLength);
        g2d.transform(newTransform);
        
        g2d.setColor(color);
        g2d.fill(bounds);
        
        g2d.setTransform(saveTransform);
    }
}