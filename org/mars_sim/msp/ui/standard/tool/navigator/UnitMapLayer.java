/**
 * Mars Simulation Project
 * UnitMapLayer.java
 * @version 2.75 2003-09-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import java.awt.*;
import javax.swing.Icon;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.ui.standard.unit_display_info.*;

/**
 * The UnitMapLayer is a graphics layer to display units.
 */
class UnitMapLayer implements MapLayer {
    
    private static final int LABEL_HORIZONTAL_OFFSET = 2;
    
    // Domain data
    private UnitManager manager;
    private MapDisplay mapDisplay;

    /**
     * Constructor
     *
     * @param mars the mars instance.
     * @param mapDisplay the mapDisplay to use.
     */
    UnitMapLayer(Mars mars, MapDisplay mapDisplay) {
        
        manager = mars.getUnitManager();
        this.mapDisplay = mapDisplay;
    }
    
    /**
     * Displays the layer on the map image.
     *
     * @param g graphics context of the map display.
     */
    public void displayLayer(Graphics g) {
        
        UnitIterator i = manager.getUnits().iterator();
        while (i.hasNext()) {
            Unit unit = i.next();
            UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
            
            if (displayInfo.isMapDisplayed(unit)) {
                double angle = 0D;
                if (mapDisplay.isUsgs() && mapDisplay.isSurface()) 
                    angle = MapDisplay.HALF_MAP_ANGLE_USGS;
                else angle = MapDisplay.HALF_MAP_ANGLE_STANDARD;
                Coordinates unitCoords = unit.getCoordinates();
                if (mapDisplay.getMapCenter().getAngle(unitCoords) < angle) {
                    displayUnitIcon(unit, g);
                    if (mapDisplay.useUnitLabels()) displayUnitLabel(unit, g);
                }
            }
        }
    }
    
    /**
     * Displays the unit icon on the map image.
     *
     * @param unit the unit to display.
     * @param g the graphics context.
     */
    private void displayUnitIcon(Unit unit, Graphics g) {
        
        IntPoint location = mapDisplay.getRectPosition(unit.getCoordinates());
        UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
        
        IntPoint imageLocation = getUnitDrawLocation(location, displayInfo.getSurfMapIcon());
        int locX = imageLocation.getiX();
        int locY = imageLocation.getiY();
        
        if (mapDisplay.isTopo()) displayInfo.getTopoMapIcon().paintIcon(mapDisplay, g, locX, locY);
        else displayInfo.getSurfMapIcon().paintIcon(mapDisplay, g, locX, locY);
    }
    
    /**
     * Displays the unit label on the map image.
     *
     * @param unit the unit to display.
     * @param g the graphics context.
     */
    private void displayUnitLabel(Unit unit, Graphics g) {
        
        IntPoint location = mapDisplay.getRectPosition(unit.getCoordinates());
        UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
        
        IntPoint labelLocation = getLabelLocation(location, displayInfo.getSurfMapIcon());
        int locX = labelLocation.getiX();
        int locY = labelLocation.getiY();
        
        if (mapDisplay.isTopo()) g.setColor(displayInfo.getTopoMapLabelColor());
        else g.setColor(displayInfo.getSurfMapLabelColor());
        
        g.setFont(displayInfo.getMapLabelFont());
        
        g.drawString(unit.getName(), labelLocation.getiX(), labelLocation.getiY());
    }   
    
    /** 
     * Gets the unit image draw position on the map image.
     *
     * @param unitPosition absolute unit position
     * @param unitIcon unit's map image icon
     * @return draw position for unit image
     */
    private IntPoint getUnitDrawLocation(IntPoint unitPosition, Icon unitIcon) {
        
        int unitX = unitPosition.getiX();
        int unitY = unitPosition.getiY();
        int iconHeight = unitIcon.getIconHeight();
        int iconWidth = unitIcon.getIconWidth();
        
        return new IntPoint(unitX - (iconWidth / 2), unitY - (iconHeight / 2));
    }
    
    /** 
     * Gets the label draw postion on map panel.
     *
     * @param unitPosition the unit display position.
     * @param unitIcon unit's map image icon.
     * @return draw position for unit label
     */
    private IntPoint getLabelLocation(IntPoint unitPosition, Icon unitIcon) {
        
        int unitX = unitPosition.getiX();
        int unitY = unitPosition.getiY();
        int iconHeight = unitIcon.getIconHeight();
        int iconWidth = unitIcon.getIconWidth();
        
        return new IntPoint(unitX + (iconWidth / 2) + LABEL_HORIZONTAL_OFFSET, 
            unitY + (iconHeight / 2));
    }
}
