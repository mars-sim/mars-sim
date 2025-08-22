/*
 * Mars Simulation Project
 * UnitDisplayInfo.java
 * @date 2023-04-28
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_display_info;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;

import com.mars_sim.core.Unit;
import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.ui.swing.ImageLoader;

/**
 * Provides display information about a unit.
 */
public class UnitDisplayInfo {

    private Icon buttonIcon;
    private String defaultSound;
	
	UnitDisplayInfo(String buttonIconName) {
		// Needs changing
        if (buttonIconName != null) {
            this.buttonIcon = ImageLoader.getIconByName(buttonIconName);
        }
	}

    UnitDisplayInfo(String buttonIconName, String defaultSound) {
		this(buttonIconName);
        this.defaultSound = defaultSound;
	}

	/**
	 * Checks if unit is to be displayed on the navigator tool map.
	 * 
	 * @param unit the unit to display
	 * @return true if unit is to be displayed on navigator map.
	 */
    public boolean isMapDisplayed(Unit unit) {
        return false;
    }

	/**
	 * Gets display icon for the map
	 * 
	 * @param unit the unit to display
	 * @param type Meta data about base Map
	 * @return icon
	 */
    public Icon getMapIcon(Unit unit, MapMetaData type) {
        return null;
    }
	
	/**
	 * Checks if the map icon should blink on and off.
	 * 
	 * @param unit the unit to display
	 * @return true if blink
	 */
    public boolean isMapBlink(Unit unit) {
        return false;
    }

	/**
	 * Gets the label color for navigator map.
	 * @param type Meta data about base Map
	 * @return color
	 */
    public Color getMapLabelColor(MapMetaData type) {
        return null;
    }

	/**
	 * Gets the label font for navigator map.
	 * 
	 * @return font
	 */
    public Font getMapLabelFont() {
        return null;
    }

	/**
	 * Gets icon for unit button.
	 * @param unit the unit to display
	 * @return icon
	 */
	public Icon getButtonIcon(Unit unit) {
		return buttonIcon;
	}

	/**
	 * Gets a sound appropriate for this unit.
	 * 
	 * @param unit the unit to display.
	 * @return sound filepath for unit or empty string if none.
	 */
    public String getSound(Unit unit) {
        return defaultSound;
    }
}
