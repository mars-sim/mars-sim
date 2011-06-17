/**
 * Mars Simulation Project
 * SettlementMapPanel.java
 * @version 3.01 2011-06-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.construction.ConstructionEvent;
import org.mars_sim.msp.core.structure.construction.ConstructionListener;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A panel for displaying the settlement map.
 */
public class SettlementMapPanel extends JPanel implements UnitListener, ConstructionListener {

    // Static members.
    public static final double DEFAULT_SCALE = 5D;
    public static final double MAX_SCALE = 55D;
    public static final double MIN_SCALE = 5D / 11D;
    private static final Color MAP_BACKGROUND = new Color(181, 95, 0);
    
    // Data members.
    private Settlement settlement;
    private double xPos;
    private double yPos;
    private double rotation;
    private double scale;
    private boolean showLabels;
    private List<SettlementMapLayer> mapLayers;
    
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
        
        // Create map layers.
        mapLayers = new ArrayList<SettlementMapLayer>(3);
        mapLayers.add(new BackgroundTileMapLayer(this));
        mapLayers.add(new StructureMapLayer(this));
        mapLayers.add(new LabelMapLayer(this));
        
        // Set preferred size.
        setPreferredSize(new Dimension(400, 400));
        
        // Set foreground and background colors.
        setOpaque(true);
        setBackground(MAP_BACKGROUND);
        setForeground(Color.WHITE);
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
        // Remove as unit and construction listener for old settlement.
        if (this.settlement != null) {
            this.settlement.removeUnitListener(this);
            Iterator<ConstructionSite> i = this.settlement.getConstructionManager()
                    .getConstructionSites().iterator();
            while (i.hasNext()) {
                i.next().removeConstructionListener(this);
            }
        }
        
        this.settlement = settlement;
        
        // Add as unit and construction listener for new settlement.
        if (settlement != null) {
            settlement.addUnitListener(this);
            Iterator<ConstructionSite> i = settlement.getConstructionManager()
                    .getConstructionSites().iterator();
            while (i.hasNext()) {
                i.next().addConstructionListener(this);
            }
        }
        
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
        
        //long startTime = System.nanoTime();
        
        Graphics2D g2d = (Graphics2D) g;
        
        // Set graphics rendering hints.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Display all map layers.
        Iterator<SettlementMapLayer> i = mapLayers.iterator();
        while (i.hasNext()) {
            i.next().displayLayer(g2d, settlement, xPos, yPos, getWidth(), getHeight(), rotation, scale);
        }
        
        //long endTime = System.nanoTime();
        //double timeDiff = (endTime - startTime) / 1000000D;
        //System.out.println("SMT paint time: " + (int) timeDiff + " ms");
    }

    @Override
    public void constructionUpdate(ConstructionEvent event) {
        // Draw map.
        repaint();
    }

    @Override
    public void unitUpdate(UnitEvent event) {
        // Add as listener for new construction sites.
        if (ConstructionManager.START_CONSTRUCTION_SITE_EVENT.equals(event.getType())) {
            ConstructionSite site = (ConstructionSite) event.getTarget();
            if (site != null) {
                site.addConstructionListener(this);
            }
        }
        
        // Redraw map for construction or building events.
        if (ConstructionManager.START_CONSTRUCTION_SITE_EVENT.equals(event.getType()) ||
                ConstructionManager.FINISH_BUILDING_EVENT.equals(event.getType()) ||
                ConstructionManager.FINISH_SALVAGE_EVENT.equals(event.getType()) ||
                BuildingManager.ADD_BUILDING_EVENT.equals(event.getType()) ||
                BuildingManager.REMOVE_BUILDING_EVENT.equals(event.getType())) {
            repaint();
        }
    }
    
    /**
     * Cleans up the map panel for disposal.
     */
    public void destroy() {
        // Remove as unit or construction listener.
        if (this.settlement != null) {
            this.settlement.removeUnitListener(this);
            Iterator<ConstructionSite> i = this.settlement.getConstructionManager()
                    .getConstructionSites().iterator();
            while (i.hasNext()) {
                i.next().removeConstructionListener(this);
            }
        }

        // Destroy all map layers.
        Iterator<SettlementMapLayer> i = mapLayers.iterator();
        while (i.hasNext()) {
            i.next().destroy();
        }
    }
}