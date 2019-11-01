/**
 * Mars Simulation Project
 * UnitLabelMapLayer.java
 * @version 3.1.0 2017-09-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

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
	 * 
	 * @param unit      the unit to display.
	 * @param mapCenter the location center of the map.
	 * @param mapType   the type of map.
	 * @param g         the graphics context.
	 */
	protected void displayUnit(Unit unit, Coordinates mapCenter, String mapType, Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		IntPoint location = MapUtils.getRectPosition(unit.getCoordinates(), mapCenter, mapType);
		UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);

		IntPoint labelLocation = null;
		
		if (displayInfo != null) {
			if (SurfMarsMap.TYPE.equals(mapType)) {
				labelLocation = getLabelLocation(location, displayInfo.getSurfMapIcon(unit));
				g2d.setColor(displayInfo.getSurfMapLabelColor());
			}
			else if (TopoMarsMap.TYPE.equals(mapType)) {
				labelLocation = getLabelLocation(location, displayInfo.getTopoMapIcon(unit));
				g2d.setColor(displayInfo.getTopoMapLabelColor());
			}
			else if (GeologyMarsMap.TYPE.equals(mapType)) {
				labelLocation = getLabelLocation(location, displayInfo.getGeologyMapIcon(unit));
				g2d.setColor(displayInfo.getGeologyMapLabelColor());
			}

			g2d.setFont(displayInfo.getMapLabelFont());
	
			if (!(displayInfo.isMapBlink(unit) && getBlinkFlag() && unit != null)) {
				g2d.drawString(unit.getName(), labelLocation.getiX(), labelLocation.getiY());
			}
		}
	}

	/**
	 * Gets the label draw position on map panel.
	 * 
	 * @param unitPosition the unit display position.
	 * @param unitIcon     unit's map image icon.
	 * @return draw position for unit label.
	 */
	private IntPoint getLabelLocation(IntPoint unitPosition, Icon unitIcon) {

		int unitX = unitPosition.getiX();
		int unitY = unitPosition.getiY();
		int iconHeight = unitIcon.getIconHeight();
		int iconWidth = unitIcon.getIconWidth();

		return new IntPoint(unitX + (iconWidth / 2) + LABEL_HORIZONTAL_OFFSET, unitY + (iconHeight / 2));
	}
}