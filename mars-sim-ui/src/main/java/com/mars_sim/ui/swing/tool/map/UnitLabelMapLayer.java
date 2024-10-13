/*
 * Mars Simulation Project
 * UnitLabelMapLayer.java
 * @date 2023-04-28
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.map;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

import com.mars_sim.core.Unit;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfo;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The UnitMapLayer is a graphics layer to display unit labels.
 */
public class UnitLabelMapLayer extends UnitMapLayer {
	private static final int LABEL_HORIZONTAL_OFFSET = 2;

	public UnitLabelMapLayer(MapPanel panel) {
		super(panel);
	}

	/**
	 * Displays a unit on the map.
	 * 
	 * @param unit      the unit to display.
	 * @param mapCenter the location center of the map.
	 * @param baseMap   the type of map.
	 * @param g2d         the graphics context.
	 */
	protected MapHotspot displayUnit(Unit unit, Coordinates mapCenter, MapDisplay baseMap, Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		IntPoint location = MapUtils.getRectPosition(unit.getCoordinates(), mapCenter, baseMap);
		UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);

		IntPoint labelLocation = null;
		
		if (displayInfo != null) {
			labelLocation = getLabelLocation(location, displayInfo.getMapIcon(unit, baseMap.getMapMetaData()));
			g2d.setColor(displayInfo.getMapLabelColor(baseMap.getMapMetaData()));
			g2d.setFont(displayInfo.getMapLabelFont());

			if (labelLocation != null && unit != null
					&& !(displayInfo.isMapBlink(unit) 
					&& getBlinkFlag())) {
				g2d.drawString(unit.getName(), labelLocation.getiX(), labelLocation.getiY());
			}
		}

		return null;
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
