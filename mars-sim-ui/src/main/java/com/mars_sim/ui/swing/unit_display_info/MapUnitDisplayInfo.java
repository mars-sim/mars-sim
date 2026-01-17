/*
 * Mars Simulation Project
 * MapUnitDisplayInfo.java
 * @date 2025-08-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_display_info;

import javax.swing.Icon;

import java.awt.Color;
import java.awt.Font;

import com.mars_sim.core.Unit;
import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.ui.swing.ImageLoader;

/**
 * This holds the display info for a unit type that can be displayed on the map.
 * It extends the UnitDisplayInfo class to provide additional functionality specific to map display.
 */
public class MapUnitDisplayInfo extends UnitDisplayInfo {

    private Font mapLabelFont;
    private Color mapLabelColor;

    private Icon blackMapIcon;
    private Icon normalMapIcon;


    /**
     * Constructor. Use the entityKey as the button icon.
     */
    MapUnitDisplayInfo(String entityKey, String defaultSound,
                    Font mapLabel, Color mapLabelColor,
                    String normalMapIcon, String blackMapIcon) {
        this(entityKey, entityKey, defaultSound,
             mapLabel, mapLabelColor,
             normalMapIcon, blackMapIcon);
    }

    /**
     * Constructor. Defines all parameters.
     */
    MapUnitDisplayInfo(String buttonIcon, String entityKey, String defaultSound,
                    Font mapLabel, Color mapLabelColor,
                    String normalMapIcon, String blackMapIcon) {

        super(buttonIcon, entityKey, defaultSound);
        this.mapLabelFont = mapLabel;
        this.mapLabelColor = mapLabelColor;
        this.blackMapIcon = ImageLoader.getIconByName(blackMapIcon);
        this.normalMapIcon =ImageLoader.getIconByName(normalMapIcon);
    }

    /**
     * Checks if unit is to be displayed on the navigator tool map. This allows a level of filtering
     * @param unit the unit to display
     * @return Display on a map or not
     */
    public boolean isMapDisplayed(Unit unit) {
        return true; // All units are displayed on the map
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
     * Gets display icon for the surface navigator map.
     * 
     * @param unit the unit to display
     * @param type Type of map
     * @return icon
     */
    public Icon getMapIcon(Unit unit, MapMetaData type) {
        return (type.isColourful() ? blackMapIcon : normalMapIcon);
    }

    /**
     * Gets the label font for navigator map.
     * 
     * @return font
     */
    public Font getMapLabelFont() {
        return mapLabelFont;
    }

    /**
     * Gets the label color for surface navigator map.
     * 
     * @param type Type of map
     * @return color
     */
    public Color getMapLabelColor(MapMetaData type) {
        return (type.isColourful() ? Color.black : mapLabelColor);
    }
}
