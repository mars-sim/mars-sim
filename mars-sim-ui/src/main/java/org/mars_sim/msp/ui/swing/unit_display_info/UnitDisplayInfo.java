/*
 * Mars Simulation Project
 * UnitDisplayInfo.java
 * @date 2023-04-28
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;

import org.mars_sim.mapdata.MapMetaData;
import org.mars_sim.msp.core.Unit;

/**
 * Provides display information about a unit.
 */
public interface UnitDisplayInfo {

	/**
	 * Checks if unit is to be displayed on the navigator tool map.
	 * 
	 * @param unit the unit to display
	 * @return true if unit is to be displayed on navigator map.
	 */
	public boolean isMapDisplayed(Unit unit);

	/**
	 * Gets display icon for the map
	 * 
	 * @param unit the unit to display
	 * @param type Meta data about base Map
	 * @return icon
	 */
	public Icon getMapIcon(Unit unit, MapMetaData type);
	
	/**
	 * Checks if the map icon should blink on and off.
	 * 
	 * @param unit the unit to display
	 * @return true if blink
	 */
	public boolean isMapBlink(Unit unit);

	/**
	 * Gets the label color for navigator map.
	 * @param type Meta data about base Map
	 * @return color
	 */
	public Color getMapLabelColor(MapMetaData type);

	/**
	 * Gets the label font for navigator map.
	 * 
	 * @return font
	 */
	public Font getMapLabelFont();

	/**
	 * Gets the range (km) for clicking on unit on navigator map.
	 * 
	 * @return clicking range
	 */
	public double getMapClickRange();

	/**
	 * Checks if the unit is to be displayed on the navigator tool globe.
	 * 
	 * @param unit the unit to display.
	 * @return true if unit is to be displayed on globe
	 */
	public boolean isGlobeDisplayed(Unit unit);

	/**
	 * Gets display color for globe.
	 * @param type Meta data about base Map
	 * @return color
	 */
	public Color getGlobeColor(MapMetaData type);

	/**
	 * Gets icon for unit button.
	 * 
	 * @return icon
	 */
	public Icon getButtonIcon(Unit unit);

	/**
	 * Gets a sound appropriate for this unit.
	 * 
	 * @param unit the unit to display.
	 * @return sound filepath for unit or empty string if none.
	 */
	public String getSound(Unit unit);
}
