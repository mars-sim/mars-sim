/**
 * Mars Simulation Project
 * PersonMapLayer.java
 * @version 3.04 2013-02-02
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Iterator;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A settlement map layer for displaying people.
 */
public class PersonMapLayer implements SettlementMapLayer {

    // Static members
    private static final Color PERSON_COLOR = new Color(0, 255, 255);
    private static final Color PERSON_OUTLINE_COLOR = new Color(0, 0, 0, 190);
    
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
            
        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();

        // Get the map center point.
        double mapCenterX = mapWidth / 2D;
        double mapCenterY = mapHeight / 2D;

        // Translate map from settlement center point.
        g2d.translate(mapCenterX + (xPos * scale), mapCenterY + (yPos * scale));

        // Rotate map from North.
        g2d.rotate(rotation, 0D - (xPos * scale), 0D - (yPos * scale));

        // Draw all people.
        drawPeople(g2d, settlement);

        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
    
    /**
     * Draw people at a settlement.
     * @param g2d the graphics context.
     * @param settlement the settlement to draw people at.
     */
    private void drawPeople(Graphics2D g2d, Settlement settlement) {
        
        if (settlement != null) {
            
            // Draw all people that are at the settlement or outside near by.
            Iterator<Person> i = Simulation.instance().getUnitManager().getPeople().iterator();
            while (i.hasNext()) {
                Person person = i.next();
                
                // Only draw living people.
                if (!person.getPhysicalCondition().isDead()) {

                    // Draw people that are at the settlement location.
                    Coordinates settlementLoc = settlement.getCoordinates();
                    Coordinates personLoc = person.getCoordinates();
                    if (personLoc.equals(settlementLoc)) {
                        drawPerson(g2d, person);
                    }
                }
            }
        }
    }
    
    /**
     * Draw a person at a settlement.
     * @param g2d the graphics context.
     * @param person the person to draw.
     */
    private void drawPerson(Graphics2D g2d, Person person) {
        
        if (person != null) {
            
            // Save original graphics transforms.
            AffineTransform saveTransform = g2d.getTransform();
            
            double circleDiameter = 10D;
            double centerX = circleDiameter / 2D;
            double centerY = circleDiameter / 2D;
            
            double translationX = (-1D * person.getXLocation() * mapPanel.getScale() - centerX);
            double translationY = (-1D * person.getYLocation() * mapPanel.getScale() - centerY);
            
            // Apply graphic transforms for label.
            AffineTransform newTransform = new AffineTransform(saveTransform);
            newTransform.translate(translationX, translationY);
            newTransform.rotate(mapPanel.getRotation() * -1D, centerX, centerY);
            g2d.setTransform(newTransform);
            
            // Set color outline color.
            g2d.setColor(PERSON_OUTLINE_COLOR);
            
            // Draw outline circle.
            g2d.fillOval(0,  0, 11, 11);
            
            // Set circle color.
            g2d.setColor(PERSON_COLOR);
            
            // Draw circle.
            g2d.fillOval(0, 0, 10, 10);
            
            // Restore original graphic transforms.
            g2d.setTransform(saveTransform);
        }
    }
    
    @Override
    public void destroy() {
        // Do nothing
    }
}