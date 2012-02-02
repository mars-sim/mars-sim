/**
 * Mars Simulation Project
 * SettlementMapPanel.java
 * @version 3.02 2012-01-31
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockListener;

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
public class SettlementMapPanel extends JPanel implements ClockListener {

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
    private boolean showBuildingLabels;
    private boolean showConstructionLabels;
    private boolean showPersonLabels;
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
        showBuildingLabels = true;
        showConstructionLabels = true;
        showPersonLabels = true;
        
        // Create map layers.
        mapLayers = new ArrayList<SettlementMapLayer>(3);
        mapLayers.add(new BackgroundTileMapLayer(this));
        mapLayers.add(new StructureMapLayer(this));
        mapLayers.add(new LabelMapLayer(this));
        mapLayers.add(new PersonMapLayer(this));
        
        // Set preferred size.
        setPreferredSize(new Dimension(400, 400));
        
        // Set foreground and background colors.
        setOpaque(true);
        setBackground(MAP_BACKGROUND);
        setForeground(Color.WHITE);
        
        Simulation.instance().getMasterClock().addClockListener(this);
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
     * Checks if building labels should be displayed.
     * @return true if building labels should be displayed.
     */
    public boolean isShowBuildingLabels() {
        return showBuildingLabels;
    }
    
    /**
     * Sets if building labels should be displayed.
     * @param showLabels true if building labels should be displayed.
     */
    public void setShowBuildingLabels(boolean showLabels) {
        this.showBuildingLabels = showLabels;
        repaint();
    }
    
    /**
     * Checks if construction site labels should be displayed.
     * @return true if construction site labels should be displayed.
     */
    public boolean isShowConstructionLabels() {
        return showConstructionLabels;
    }
    
    /**
     * Sets if construction site labels should be displayed.
     * @param showLabels true if construction site labels should be displayed.
     */
    public void setShowConstructionLabels(boolean showLabels) {
        this.showConstructionLabels = showLabels;
        repaint();
    }
    
    /**
     * Checks if person labels should be displayed.
     * @return true if person labels should be displayed.
     */
    public boolean isShowPersonLabels() {
        return showPersonLabels;
    }
    
    /**
     * Sets if person labels should be displayed.
     * @param showLabels true if person labels should be displayed.
     */
    public void setShowPersonLabels(boolean showLabels) {
        this.showPersonLabels = showLabels;
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
    
    /**
     * Cleans up the map panel for disposal.
     */
    public void destroy() {
        // Remove clock listener.
        Simulation.instance().getMasterClock().removeClockListener(this);

        // Destroy all map layers.
        Iterator<SettlementMapLayer> i = mapLayers.iterator();
        while (i.hasNext()) {
            i.next().destroy();
        }
    }

    @Override
    public void clockPulse(double time) {
        // Repaint map panel with each clock pulse.
        repaint();
    }
}