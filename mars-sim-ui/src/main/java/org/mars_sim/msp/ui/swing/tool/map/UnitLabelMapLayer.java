/**
 * Mars Simulation Project
 * UnitLabelMapLayer.java
 * @version 3.07 2014-10-13
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Graphics;

import javax.swing.Icon;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The UnitMapLayer is a graphics layer to display unit labels.
 */
public class UnitLabelMapLayer extends UnitMapLayer {

	private static final int LABEL_HORIZONTAL_OFFSET = 2;
	
	/**
	 * Displays a unit on the map.
	 * @param unit the unit to display.
	 * @param mapCenter the location center of the map.
	 * @param mapType the type of map.
	 * @param g the graphics context.
	 */
	protected void displayUnit(Unit unit, Coordinates mapCenter, String mapType, Graphics g) {
		
        IntPoint location = MapUtils.getRectPosition(unit.getCoordinates(), mapCenter, mapType);
        UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
        
        IntPoint labelLocation = getLabelLocation(location, displayInfo.getSurfMapIcon(unit));
        
        if (TopoMarsMap.TYPE.equals(mapType)) g.setColor(displayInfo.getTopoMapLabelColor());
        else if (displayInfo != null)
        	g.setColor(displayInfo.getSurfMapLabelColor());
        	
        g.setFont(displayInfo.getMapLabelFont());
        
        if (!(displayInfo.isMapBlink(unit) && getBlinkFlag())) {
        	g.drawString(unit.getName(), 
        	labelLocation.getiX(), labelLocation.getiY());
        }
	}
	
    /** 
     * Gets the label draw position on map panel.
     * @param unitPosition the unit display position.
     * @param unitIcon unit's map image icon.
     * @return draw position for unit label.
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