/**
 * Mars Simulation Project
 * UnitMapLayer.java
 * @version 2.79 2006-06-28
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
    private boolean blinkFlag;

    /**
     * Constructor
     * @param mapDisplay the mapDisplay to use.
     */
    UnitMapLayer(MapDisplay mapDisplay) {
        
        manager = Simulation.instance().getUnitManager();
        this.mapDisplay = mapDisplay;
        blinkFlag = false;
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
        
        blinkFlag = !blinkFlag;
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
        
        IntPoint imageLocation = getUnitDrawLocation(location, displayInfo.getSurfMapIcon(unit));
        int locX = imageLocation.getiX();
        int locY = imageLocation.getiY();
        
        if (!displayInfo.isMapBlink(unit) || (displayInfo.isMapBlink(unit) && blinkFlag)) {
        	if (mapDisplay.isTopo()) displayInfo.getTopoMapIcon(unit).paintIcon(mapDisplay, g, locX, locY);
        	else displayInfo.getSurfMapIcon(unit).paintIcon(mapDisplay, g, locX, locY);
        }
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
        
        IntPoint labelLocation = getLabelLocation(location, displayInfo.getSurfMapIcon(unit));
        
        if (mapDisplay.isTopo()) g.setColor(displayInfo.getTopoMapLabelColor());
        else g.setColor(displayInfo.getSurfMapLabelColor());
        
        g.setFont(displayInfo.getMapLabelFont());
        
        if (!displayInfo.isMapBlink(unit) || (displayInfo.isMapBlink(unit) && blinkFlag)) {
        	g.drawString(unit.getName(), labelLocation.getiX(), labelLocation.getiY());
        }
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
