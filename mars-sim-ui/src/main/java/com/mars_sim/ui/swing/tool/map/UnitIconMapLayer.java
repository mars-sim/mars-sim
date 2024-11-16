/*
 * Mars Simulation Project
 * UnitIconMapLayer.java
 * @date 2023-04-28
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Graphics2D;

import javax.swing.Icon;

import com.mars_sim.core.Unit;
import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfo;

/**
 * The UnitMapLayer is a graphics layer to display unit icons.
 */
public class UnitIconMapLayer extends UnitMapLayer {
	/**
	 * Is a clickable hotspot for a Unit on the surface
	 */
	private class UnitHotspot extends MapHotspot {

		private Unit target;

		protected UnitHotspot(IntPoint center, Unit target) {
			super(center, 5);
			this.target = target;
		}

		/**
		 * Delegate to the desktop to display the unit window
		 */
		@Override
		public void clicked() {
			displayComponent.getDesktop().showDetails(target);
		}
	}

	private MapPanel displayComponent;

	public UnitIconMapLayer(MapPanel displayComponent) {
		super(displayComponent);
		this.displayComponent = displayComponent;
	}

	/**
	 * Displays a unit on the map.
	 * 
 	 * @param unit      the unit to display.
	 * @param info		details how to render unit
	 * @param location  Lociation on the map of this unit
	 * @param baseMap   the type of map.
	 * @param g         the graphics context.
	 */
	protected MapHotspot displayUnit(Unit unit, UnitDisplayInfo displayInfo, IntPoint location,
							MapDisplay baseMap, Graphics2D g) {

		IntPoint imageLocation = getUnitDrawLocation(location, displayInfo.getMapIcon(unit, baseMap.getMapMetaData()));
		int locX = imageLocation.getiX();
		int locY = imageLocation.getiY();

		if (!(displayInfo.isMapBlink(unit) && getBlinkFlag())) {
			MapMetaData mapType = baseMap.getMapMetaData();
			Icon displayIcon = displayInfo.getMapIcon(unit, mapType);	
			displayIcon.paintIcon(displayComponent, g, locX, locY);

			return new UnitHotspot(location, unit);
		}

		return null;
	}

	/**
	 * Gets the unit image draw position on the map image.
	 *
	 * @param unitPosition absolute unit position
	 * @param unitIcon     unit's map image icon
	 * @return draw position for unit image
	 */
	private IntPoint getUnitDrawLocation(IntPoint unitPosition, Icon unitIcon) {

		int unitX = unitPosition.getiX();
		int unitY = unitPosition.getiY();
		int iconHeight = unitIcon.getIconHeight();
		int iconWidth = unitIcon.getIconWidth();

		return new IntPoint(unitX - (iconWidth / 2), unitY - (iconHeight / 2));
	}
}
