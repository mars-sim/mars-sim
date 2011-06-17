/**
 * Mars Simulation Project
 * LabelMapLayer.java
 * @version 3.01 2011-06-16
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;

/**
 * A settlement map layer for displaying labels for buildings and construction sites.
 */
public class LabelMapLayer implements SettlementMapLayer {

    // Static members
    private static final Color LABEL_COLOR = Color.BLUE;
    private static final Color LABEL_OUTLINE_COLOR = new Color(255, 255, 255, 127);
    
    // Data members
    private SettlementMapPanel mapPanel;
    
    public LabelMapLayer(SettlementMapPanel mapPanel) {
        
        // Initialize data members.
        this.mapPanel = mapPanel;
    }

    @Override
    public void displayLayer(Graphics2D g2d, Settlement settlement,
            double xPos, double yPos, int mapWidth, int mapHeight,
            double rotation, double scale) {
        
        if (mapPanel.isShowLabels()) {
            
            // Save original graphics transforms.
            AffineTransform saveTransform = g2d.getTransform();
            
            // Get the map center point.
            double mapCenterX = mapWidth / 2D;
            double mapCenterY = mapHeight / 2D;
            
            // Translate map from settlement center point.
            g2d.translate(mapCenterX + (xPos * scale), mapCenterY + (yPos * scale));
            
            // Rotate map from North.
            g2d.rotate(rotation, 0D - (xPos * scale), 0D - (yPos * scale));
            
            // Draw all building labels.
            drawBuildingLabels(g2d, settlement);
            
            // Draw all construction site labels.
            drawConstructionSiteLabels(g2d, settlement);
            
            // Restore original graphic transforms.
            g2d.setTransform(saveTransform);
        }
    }
    
    /**
     * Draw labels for all of the buildings in the settlement.
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
    private void drawBuildingLabels(Graphics2D g2d, Settlement settlement) {
        if (settlement != null) {
            Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
            while (i.hasNext()) {
                Building building = i.next();
                drawLabel(g2d, building.getName(), building.getXLocation(), building.getYLocation());
            }
        }
    }
    
    /**
     * Draw labels for all of the construction sites in the settlement.
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
    private void drawConstructionSiteLabels(Graphics2D g2d, Settlement settlement) {
        if (settlement != null) {
            Iterator<ConstructionSite> i = settlement.getConstructionManager().
                    getConstructionSites().iterator();
            while (i.hasNext()) {
                ConstructionSite site = i.next();
                String siteLabel = getConstructionLabel(site);
                drawLabel(g2d, siteLabel, site.getXLocation(), site.getYLocation());
            }
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
        TextLayout textLayout = new TextLayout(label, g2d.getFont(), g2d.getFontRenderContext());
        Rectangle2D bounds = textLayout.getBounds();
        
        // Determine transform information.
        double boundsPosX = bounds.getX();
        double boundsPosY = bounds.getY();
        double centerX = bounds.getWidth() / 2D;
        double centerY = bounds.getHeight() / 2D;
        double translationX = (-1D * xLoc * mapPanel.getScale()) - centerX - boundsPosX;
        double translationY = (-1D * yLoc * mapPanel.getScale()) - centerY - boundsPosY;
        
        // Apply graphic transforms for label.
        AffineTransform newTransform = new AffineTransform();
        newTransform.translate(translationX, translationY);
        newTransform.rotate(mapPanel.getRotation() * -1D, centerX + boundsPosX, centerY + boundsPosY);
        Shape labelShape = textLayout.getOutline(newTransform);
        
        // Draw label outline.
        Stroke saveStroke = g2d.getStroke();
        g2d.setColor(LABEL_OUTLINE_COLOR);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(labelShape);
        g2d.setStroke(saveStroke);
        
        // Fill label
        g2d.setColor(LABEL_COLOR);
        g2d.fill(labelShape);
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
    
    @Override
    public void destroy() {
        // Do nothing.
    }
}