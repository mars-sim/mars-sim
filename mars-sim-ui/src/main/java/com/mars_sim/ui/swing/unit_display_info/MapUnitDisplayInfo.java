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
     * Constructor.
     */
    public MapUnitDisplayInfo(String buttonIcon, String defaultSound,
                    Font mapLabel, Color mapLabelColor,
                    String normalMapIcon, String blackMapIcon) {
        super(buttonIcon, defaultSound);
        this.mapLabelFont = mapLabel;
        this.mapLabelColor = mapLabelColor;
        this.blackMapIcon = ImageLoader.getIconByName(blackMapIcon);
        this.normalMapIcon =ImageLoader.getIconByName(normalMapIcon);
    }

    @Override
    public boolean isMapDisplayed(Unit unit) {
        return true; // All units are displayed on the map
    }

    /**
     * Gets display icon for the surface navigator map.
     * 
     * @param unit the unit to display
     * @param type Type of map
     * @return icon
     */
    @Override
    public Icon getMapIcon(Unit unit, MapMetaData type) {
        return (type.isColourful() ? blackMapIcon : normalMapIcon);
    }

    /**
     * Gets the label font for navigator map.
     * 
     * @return font
     */
    @Override
    public Font getMapLabelFont() {
        return mapLabelFont;
    }

    /**
     * Gets the label color for surface navigator map.
     * 
     * @param type Type of map
     * @return color
     */
    @Override
    public Color getMapLabelColor(MapMetaData type) {
        return (type.isColourful() ? Color.black : mapLabelColor);
    }
}
