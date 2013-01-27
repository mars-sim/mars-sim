/**
 * Mars Simulation Project
 * LabelMapLayer.java
 * @version 3.04 2013-01-27
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A settlement map layer for displaying labels for map objects.
 */
public class LabelMapLayer implements SettlementMapLayer {

    // Static members
    private static final Color BUILDING_LABEL_COLOR = new Color(0, 0, 255);
    private static final Color BUILDING_LABEL_OUTLINE_COLOR = new Color(255, 255, 255, 127);
    private static final Color CONSTRUCTION_SITE_LABEL_COLOR = new Color(0, 0, 255);
    private static final Color CONSTRUCTION_SITE_LABEL_OUTLINE_COLOR = new Color(255, 255, 255, 127);
    private static final Color VEHICLE_LABEL_COLOR = new Color(127, 0, 127);
    private static final Color VEHICLE_LABEL_OUTLINE_COLOR = new Color(255, 255, 255, 127);
    private static final Color PERSON_LABEL_COLOR = new Color(0, 255, 255);
    private static final Color PERSON_LABEL_OUTLINE_COLOR = new Color(0, 0, 0, 127);
    
    // Data members
    private SettlementMapPanel mapPanel;
    private Map<String, BufferedImage> labelImageCache;
    
    /**
     * Constructor
     * @param mapPanel the settlement map panel.
     */
    public LabelMapLayer(SettlementMapPanel mapPanel) {
        
        // Initialize data members.
        this.mapPanel = mapPanel;
        labelImageCache = new HashMap<String, BufferedImage>(30);
    }

    @Override
    public void displayLayer(Graphics2D g2d, Settlement settlement,
            double xPos, double yPos, int mapWidth, int mapHeight,
            double rotation, double scale) {
            
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
        if (mapPanel.isShowBuildingLabels()) {
            drawBuildingLabels(g2d, settlement);
        }
            
        // Draw all construction site labels.
        if (mapPanel.isShowConstructionLabels()) {
            drawConstructionSiteLabels(g2d, settlement);
        }
        
        // Draw all vehicle labels.
        if (mapPanel.isShowVehicleLabels()) {
        	drawVehicleLabels(g2d, settlement);
        }
        
        // Draw all people labels.
        if (mapPanel.isShowPersonLabels()) {
            drawPersonLabels(g2d, settlement);
        }
            
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
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
                drawLabel(g2d, building.getName(), building.getXLocation(), building.getYLocation(), 
                		BUILDING_LABEL_COLOR, BUILDING_LABEL_OUTLINE_COLOR);
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
                drawLabel(g2d, siteLabel, site.getXLocation(), site.getYLocation(), 
                		CONSTRUCTION_SITE_LABEL_COLOR, CONSTRUCTION_SITE_LABEL_OUTLINE_COLOR);
            }
        }
    }
    
    /**
     * Gets the label for a construction site.
     * @param site the construction site.
     * @return the construction label.
     */
    public static String getConstructionLabel(ConstructionSite site) {
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
     * Draw labels for all of the vehicles parked at the settlement.
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
    private void drawVehicleLabels(Graphics2D g2d, Settlement settlement) {
        if (settlement != null) {
            
            // Draw all parked vehicles.
            Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
            while (i.hasNext()) {
                Vehicle vehicle = i.next();
                drawLabel(g2d, vehicle.getName(), vehicle.getXLocation(), vehicle.getYLocation(), 
                		VEHICLE_LABEL_COLOR, VEHICLE_LABEL_OUTLINE_COLOR);
                
                // Draw any towed vehicles.
                if (vehicle instanceof Rover) {
                    Rover rover = (Rover) vehicle;
                    Vehicle towedVehicle = rover.getTowedVehicle();
                    if (towedVehicle != null) {
                        drawLabel(g2d, towedVehicle.getName(), towedVehicle.getXLocation(), towedVehicle.getYLocation(), 
                                VEHICLE_LABEL_COLOR, VEHICLE_LABEL_OUTLINE_COLOR);
                    }
                }
            }
            
            // Draw all vehicles used in construction sites at the settlement.
            Iterator<Mission> j = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement).iterator();
            while (j.hasNext()) {
                Mission mission = j.next();
                if (mission instanceof BuildingConstructionMission) {
                    Iterator<GroundVehicle> k = ((BuildingConstructionMission) mission).getConstructionVehicles().iterator();
                    while (k.hasNext()) {
                        Vehicle vehicle = k.next();
                        drawLabel(g2d, vehicle.getName(), vehicle.getXLocation(), vehicle.getYLocation(), 
                                VEHICLE_LABEL_COLOR, VEHICLE_LABEL_OUTLINE_COLOR);
                    }
                }
                else if (mission instanceof BuildingSalvageMission) {
                    Iterator<GroundVehicle> k = ((BuildingSalvageMission) mission).getConstructionVehicles().iterator();
                    while (k.hasNext()) {
                        Vehicle vehicle = k.next();
                        drawLabel(g2d, vehicle.getName(), vehicle.getXLocation(), vehicle.getYLocation(), 
                                VEHICLE_LABEL_COLOR, VEHICLE_LABEL_OUTLINE_COLOR);
                    }
                }
            }
        }
    }
    
    /**
     * Draw labels for all people at the settlement.
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
    private void drawPersonLabels(Graphics2D g2d, Settlement settlement) {
        
        if (settlement != null) {
            
            // Draw all people that are at the settlement or outside near by.
            Iterator<Person> i = Simulation.instance().getUnitManager().getPeople().iterator();
            while (i.hasNext()) {
                Person person = i.next();
                
                // Only draw living people.
                if (!person.getPhysicalCondition().isDead()) {

                    // Draw people that at settlement location.
                    Coordinates settlementLoc = settlement.getCoordinates();
                    Coordinates personLoc = person.getCoordinates();
                    if (personLoc.equals(settlementLoc)) {
                        int offset = 8;
                        drawLabelRight(g2d, person.getName(), person.getXLocation(), person.getYLocation(), 
                                PERSON_LABEL_COLOR, PERSON_LABEL_OUTLINE_COLOR, offset);
                    }
                }
            }
        }
    }
    
    /**
     * Draws a label centered at the X, Y location.
     * @param g2d the graphics 2D context.
     * @param label the label string.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param labelColor the color of the label.
     * @param labelOutlineColor the color of the outline of the label.
     */
    private void drawLabel(Graphics2D g2d, String label, double xLoc, double yLoc, 
            Color labelColor, Color labelOutlineColor) {
        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        
        // Get the label image.
        BufferedImage labelImage = getLabelImage(label, g2d.getFont(), g2d.getFontRenderContext(), 
                labelColor, labelOutlineColor);
        
        // Determine transform information.
        double centerX = labelImage.getWidth() / 2D;
        double centerY = labelImage.getHeight() / 2D;
        double translationX = (-1D * xLoc * mapPanel.getScale()) - centerX;
        double translationY = (-1D * yLoc * mapPanel.getScale()) - centerY;
        
        // Apply graphic transforms for label.
        AffineTransform newTransform = new AffineTransform(saveTransform);
        newTransform.translate(translationX, translationY);
        newTransform.rotate(mapPanel.getRotation() * -1D, centerX, centerY);
        g2d.setTransform(newTransform);
        
        // Draw image label.
        g2d.drawImage(labelImage, 0, 0, mapPanel);
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
    
    /**
     * Draws a label to the right of an X, Y location.
     * @param g2d the graphics 2D context.
     * @param label the label string.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param labelColor the color of the label.
     * @param labelOutlineColor the color of the outline of the label.
     * @param offset the X pixel offset from the center point.
     */
    private void drawLabelRight(Graphics2D g2d, String label, double xLoc, double yLoc, 
            Color labelColor, Color labelOutlineColor, int offset) {
        
        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        
        // Get the label image.
        BufferedImage labelImage = getLabelImage(label, g2d.getFont(), g2d.getFontRenderContext(), 
                labelColor, labelOutlineColor);
        
        // Determine transform information.
        double centerX = labelImage.getWidth() / 2D;
        double centerY = labelImage.getHeight() / 2D;
        double translationX = (-1D * xLoc * mapPanel.getScale()) - centerX;
        double translationY = (-1D * yLoc * mapPanel.getScale()) - centerY;
        
        // Apply graphic transforms for label.
        AffineTransform newTransform = new AffineTransform(saveTransform);
        newTransform.translate(translationX, translationY);
        newTransform.rotate(mapPanel.getRotation() * -1D, centerX, centerY);
        g2d.setTransform(newTransform);
        
        // Draw image label.
        int totalRightOffset = (labelImage.getWidth() / 2) + offset;
        g2d.drawImage(labelImage, totalRightOffset, 0, mapPanel);
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
    
    /**
     * Gets an image of the label from cache or creates one if it doesn't exist.
     * @param label the label string.
     * @param font the font to use.
     * @param fontRenderContext the font render context to use.
     * @param labelColor the color of the label.
     * @param labelOutlineColor the color of the outline of the label.
     * @return buffered image of label.
     */
    private BufferedImage getLabelImage(String label, Font font, FontRenderContext fontRenderContext, Color labelColor, 
    		Color labelOutlineColor) {
        
        BufferedImage labelImage = null;
        if (labelImageCache.containsKey(label)) {
            labelImage = labelImageCache.get(label);
        }
        else {
            labelImage = createLabelImage(label, font, fontRenderContext, labelColor, labelOutlineColor);
            labelImageCache.put(label, labelImage);
        }
        
        return labelImage;
    }
    
    /**
     * Creates a label image.
     * @param label the label string.
     * @param font the font to use.
     * @param fontRenderContext the font render context to use.
     * @param labelColor the color of the label.
     * @param labelOutlineColor the color of the outline of the label.
     * @return buffered image of label.
     */
    private BufferedImage createLabelImage(String label, Font font, FontRenderContext fontRenderContext, Color labelColor, 
    		Color labelOutlineColor) {
        
        // Determine bounds.
        TextLayout textLayout1 = new TextLayout(label, font, fontRenderContext);
        Rectangle2D bounds1 = textLayout1.getBounds();
        
        // Get label shape.
        Shape labelShape = textLayout1.getOutline(null);
        
        // Create buffered image for label.
        int width = (int) (bounds1.getWidth() + bounds1.getX()) + 4;
        int height = (int) (bounds1.getHeight()) + 4;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        // Get graphics context from buffered image.
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(2D - bounds1.getX(), 2D - bounds1.getY());
        
        // Draw label outline.
        Stroke saveStroke = g2d.getStroke();
        g2d.setColor(labelOutlineColor);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(labelShape);
        g2d.setStroke(saveStroke);
        
        // Fill label
        g2d.setColor(labelColor);
        g2d.fill(labelShape);
        
        // Dispose of image graphics context.
        g2d.dispose();
        
        return bufferedImage;
    }
    
    @Override
    public void destroy() {
        // Clear label image cache.
        labelImageCache.clear();
    }
}