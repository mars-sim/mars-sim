/*
 * Mars Simulation Project
 * SettlementDisplayInfo.java
 * @date 2023-04-28
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import org.mars.sim.mapdata.MapMetaData;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.sound.SoundConstants;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Font;

/**
 * Provides display information about a settlement.
 */
class SettlementDisplayInfoBean implements UnitDisplayInfo {

    // Navigator click range in km.
    private static double SETTLEMENT_CLICK_RANGE = 90D;
        
    private Icon buttonIcon;
    private Font mapLabelFont;

    private Icon blackMapIcon;
    private Icon normalMapIcon;

    /**
     * Constructor
     */
    SettlementDisplayInfoBean() {
        normalMapIcon = ImageLoader.getIconByName("map/settlement");
        blackMapIcon = ImageLoader.getIconByName("map/settlement_black");
        buttonIcon = ImageLoader.getIconByName("settlement");
        mapLabelFont = new Font("SansSerif", Font.BOLD, 12);
    }

    /**
     * Checks if unit is to be displayed on the navigator tool map.
     * 
     * @param unit the unit to display
     * @return true if unit is to be displayed on navigator map.
     */
    @Override
    public boolean isMapDisplayed(Unit unit) {
        return true;
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
     * Checks if the map icon should blink on and off.
     * 
     * @param unit the unit to display
     * @return true if blink
     */
    @Override
    public boolean isMapBlink(Unit unit) {
    	return false;
    }

    /**
     * Gets the label color for surface navigator map.
     * @param type Type of map
     * @return color
     */
    @Override
    public Color getMapLabelColor(MapMetaData type) {
        return (type.isColourful() ? Color.black : Color.green);
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
     * Gets the range (km) for clicking on unit on navigator map.
     * 
     * @return clicking range
     */
    public double getMapClickRange() {
        return SETTLEMENT_CLICK_RANGE;
    }

    /**
     * Checks if the unit is to be displayed on the navigator tool globe.
     * 
     * @param unit the unit to display.
     * @return true if unit is to be displayed on globe
     */
    @Override
    public boolean isGlobeDisplayed(Unit unit) {
        return true;
    }

    /**
     * Gets display color for surface globe.
     * 
     * @return color
     */
    @Override
    public Color getGlobeColor(MapMetaData type) {
        return (type.isColourful() ? Color.black : Color.green);

    }

  

    /**
     * Gets icon for unit button.
     * 
     * @return icon
     */
    @Override
    public Icon getButtonIcon(Unit unit) {
        return buttonIcon;
    }

    /**
     * Gets a sound appropriate for this unit.
     * 
     * @param unit the unit to display.
     * @returns sound filepath for unit or empty string if none.
     */
    @Override
    public String getSound(Unit unit) {
    	return SoundConstants.SND_SETTLEMENT;
    }
}
