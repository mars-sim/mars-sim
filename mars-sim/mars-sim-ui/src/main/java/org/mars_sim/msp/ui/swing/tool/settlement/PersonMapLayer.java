/**
 * Mars Simulation Project
 * PersonMapLayer.java
 * @version 3.02 2012-01-30
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.ConstructBuilding;
import org.mars_sim.msp.core.person.ai.task.SalvageBuilding;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;

public class PersonMapLayer implements SettlementMapLayer {

    // Static members
    private static final Color LABEL_COLOR = new Color(0, 255, 255);
    private static final Color LABEL_OUTLINE_COLOR = new Color(0, 0, 0, 127);
    private static final int OFFSET_PIXELS = 12;
    
    // Data members
    private SettlementMapPanel mapPanel;
    
    /**
     * Constructor
     * @param mapPanel the settlement map panel.
     */
    public PersonMapLayer(SettlementMapPanel mapPanel) {
        
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
            
            // Draw all person labels.
            drawPersonLabels(g2d, settlement);
            
            // Restore original graphic transforms.
            g2d.setTransform(saveTransform);
            
            // Draw the labels for all people out on EVA's.
            drawEVAPersonLabels(g2d, settlement);
        }
    }
    
    /**
     * Draw people located at the settlement on the map.
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
    private void drawPersonLabels(Graphics2D g2d, Settlement settlement) {
        
        if (settlement != null) {
            
            // Draw labels of people in buildings.
            Iterator<Building> i = settlement.getBuildingManager().getBuildings(LifeSupport.NAME).iterator();
            while (i.hasNext()) {
                Building building = i.next();
                LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
                Iterator<Person> j = lifeSupport.getOccupants().iterator();
                int offset = 1;
                while (j.hasNext()) {
                    drawPersonLabel(g2d, j.next(), building.getName(), building.getXLocation(), 
                            building.getYLocation(), offset);
                    offset++;
                }
            }
        
            // Draw labels of people working on construction sites.
            Iterator<ConstructionSite> j = settlement.getConstructionManager().getConstructionSites().iterator();
            while (j.hasNext()) {
                ConstructionSite site = j.next();
                String siteLabel = LabelMapLayer.getConstructionLabel(site);
                int offset = 1;
                Iterator<Person> k = getConstructionSalvageWorkers(site, settlement).iterator();
                while (k.hasNext()) {
                    Person person = k.next();
                    drawPersonLabel(g2d, person, siteLabel, site.getXLocation(), site.getYLocation(), offset);
                    offset++;
                }
            }
        }
    }
    
    /**
     * Gets a list of all people actively working on a construction or salvage site.
     * @param site the construction site.
     * @param settlement the settlement.
     * @return list of construction workers.
     */
    private List<Person> getConstructionSalvageWorkers(ConstructionSite site, Settlement settlement) {
        
        List<Person> result = new ArrayList<Person>();
        
        Iterator<Person> k = settlement.getAllAssociatedPeople().iterator();
        while (k.hasNext()) {
            Person person = k.next();
            if (!person.getLocationSituation().equals(Person.INSETTLEMENT)) {
                Task task = person.getMind().getTaskManager().getTask();
                if ((task != null)) {
                    if (task instanceof ConstructBuilding) {
                        ConstructBuilding constTask = (ConstructBuilding) task;
                        if (constTask.getConstructionStage().equals(site.getCurrentConstructionStage())) {
                            result.add(person);
                        }
                    }
                    else if (task instanceof SalvageBuilding) {
                        SalvageBuilding salvTask = (SalvageBuilding) task;
                        if (salvTask.getConstructionStage().equals(site.getCurrentConstructionStage())) {
                            result.add(person);
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Draws a person's label on the map.
     * @param g2d the graphics context.
     * @param person the person.
     * @param alignText text that needs to be left-aligned with.
     * @param xLoc the xLocation to draw.
     * @param yLoc the yLocation to draw.
     * @param offset the vertical offset for the label.
     */
    private void drawPersonLabel(Graphics2D g2d, Person person, String alignText, double xLoc, double yLoc, 
            int offset) {
        
        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        
        // Save original font.
        Font saveFont = g2d.getFont();
        
        // Set label font.
        Font labelFont = new Font(saveFont.getName(), saveFont.getStyle(), saveFont.getSize() - 3);
        g2d.setFont(labelFont);
        
        // Determine bounds.
        TextLayout textLayout = new TextLayout("* " + person.getName(), g2d.getFont(), g2d.getFontRenderContext());
        Rectangle2D bounds = textLayout.getBounds();
        int width = (int) (bounds.getWidth() + bounds.getX()) + 4;
        int height = (int) (bounds.getHeight() + bounds.getY()) + 4;
        
        // Get label shape.
        Shape labelShape = textLayout.getOutline(null);
        
        // Determine text alignment offset.
        TextLayout alignTextLayout = new TextLayout(alignText, saveFont, g2d.getFontRenderContext());
        Rectangle2D alignBounds = alignTextLayout.getBounds();
        int alignWidth = (int) (alignBounds.getWidth() + alignBounds.getX()) + 4;
        int alignOffset = (alignWidth / 2) - (width / 2);
        
        // Determine transform information.
        double centerX = width / 2D;
        double centerY = height / 2D;
        double translationX = (-1D * xLoc * mapPanel.getScale()) - centerX;
        double translationY = (-1D * yLoc * mapPanel.getScale()) + centerY + 2D;
        
        // Apply graphic transforms for label.
        AffineTransform newTransform = new AffineTransform(saveTransform);
        newTransform.translate(translationX, translationY);
        newTransform.rotate(mapPanel.getRotation() * -1D, centerX, centerY - 6D);
        newTransform.translate((-1D * alignOffset) + 2, (offset * OFFSET_PIXELS));
        g2d.setTransform(newTransform);
        
        // Draw label outline.
        Stroke saveStroke = g2d.getStroke();
        g2d.setColor(LABEL_OUTLINE_COLOR);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(labelShape);
        g2d.setStroke(saveStroke);
        
        // Fill label
        g2d.setColor(LABEL_COLOR);
        g2d.fill(labelShape);
        
        // Restore original font and graphic transforms.
        g2d.setFont(saveFont);
        g2d.setTransform(saveTransform);
    }
    
    /**
     * Draw labels for people out on EVA's (not construction/salvage workers).
     * @param g2d the graphics 2D context.
     * @param settlement the settlement.
     */
    private void drawEVAPersonLabels(Graphics2D g2d, Settlement settlement) {
        // Save original font and transform.
        Font saveFont = g2d.getFont();
        AffineTransform saveTransform = g2d.getTransform();
        
        int x = 20;
        int y = 20;
        
        g2d.translate(x, y);
        
        TextLayout headingLayout = new TextLayout("Outside Settlement (EVA)", g2d.getFont(), g2d.getFontRenderContext());
        Shape headingShape = headingLayout.getOutline(null);
        Stroke saveStroke = g2d.getStroke();
        g2d.setColor(LABEL_OUTLINE_COLOR);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(headingShape);
        g2d.setStroke(saveStroke);
        g2d.setColor(LABEL_COLOR);
        g2d.fill(headingShape);
        
        Font personFont = new Font(saveFont.getName(), saveFont.getStyle(), saveFont.getSize() - 3);
        g2d.setFont(personFont);
        
        int offset = 1;
        Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
        while (i.hasNext()) {
            Person person = i.next();
            boolean isEVAatSettlement = true;
            if (!person.getLocationSituation().equals(Person.OUTSIDE)) {
                isEVAatSettlement = false;
            }
            
            if (!person.getCoordinates().equals(settlement.getCoordinates())) {
                isEVAatSettlement = false;
            }
            
            // Don't display construction/salvage workers as they are already displayed at those construction sites.
            if (isConstructingSalvaging(person)) {
                isEVAatSettlement = false;
            }
            
            if (isEVAatSettlement) {
                g2d.translate(0, 15);
                
                TextLayout personLayout = new TextLayout("* " + person.getName(), g2d.getFont(), g2d.getFontRenderContext());
                Shape personShape = personLayout.getOutline(null);
                g2d.setColor(LABEL_OUTLINE_COLOR);
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.draw(personShape);
                g2d.setStroke(saveStroke);
                g2d.setColor(LABEL_COLOR);
                g2d.fill(personShape);
                
                offset++;
            }
        }
        
        // Restore original font and transform.
        g2d.setTransform(saveTransform);
        g2d.setFont(saveFont);
    }
    
    /**
     * Checks if a person is actively doing construction/salvage work at a construction site.
     * @param person the person.
     * @return true if doing construction/salvage work.
     */
    private boolean isConstructingSalvaging(Person person) {
        
        boolean result = false;
        if (!person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Task task = person.getMind().getTaskManager().getTask();
            if ((task != null)) {
                if ((task instanceof ConstructBuilding) || (task instanceof SalvageBuilding)) {
                    result = true;
                }
            }
        }
        
        return result;
    }
    
    @Override
    public void destroy() {
        // Do nothing
    }
}