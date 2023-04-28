/*
 * Mars Simulation Project
 * SettlementDisplayInfo.java
 * @date 2023-04-28
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

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

    private static Color lightSlateGray = new Color(255, 255, 255);
    
    // Data members
    private Icon surfMapIcon;
    private Icon topoMapIcon;
    private Icon geoMapIcon;
    private Icon regionMapIcon;
    private Icon vikingMapIcon;
    
    private Icon buttonIcon;
    private Font mapLabelFont;

    /**
     * Constructor
     */
    SettlementDisplayInfoBean() {
        surfMapIcon = ImageLoader.getIconByName("map/settlement");
        topoMapIcon = ImageLoader.getIconByName("map/settlement_black");
        geoMapIcon = ImageLoader.getIconByName("map/settlement_black");
        regionMapIcon = ImageLoader.getIconByName("map/settlement_black");
        vikingMapIcon = ImageLoader.getIconByName("map/settlement");
        buttonIcon = ImageLoader.getIconByName("settlement");
        mapLabelFont = new Font("SansSerif", Font.BOLD, 12);
    }

    /**
     * Checks if unit is to be displayed on the navigator tool map.
     * 
     * @param unit the unit to display
     * @return true if unit is to be displayed on navigator map.
     */
    public boolean isMapDisplayed(Unit unit) {
        return true;
    }

    /**
     * Gets display icon for the surface navigator map.
     * 
     * @param unit the unit to display
     * @return icon
     */
    public Icon getSurfMapIcon(Unit unit) {
        return surfMapIcon;
    }

    /**
     * Gets display icon for topo navigator map.
     * 
     * @param unit the unit to display
     * @return icon
     */
    public Icon getTopoMapIcon(Unit unit) {
        return topoMapIcon;
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
     * Gets the label color for surface navigator map.
     * 
     * @return color
     */
    public Color getSurfMapLabelColor() {
        return Color.green;
    }

    /**
     * Gets the label color for topo navigator map.
     * 
     * @return color
     */
    public Color getTopoMapLabelColor() {
        return Color.black;
    }

    /**
     * Gets display icon for geological navigator map.
     * 
     * @param unit the unit to display
     * @return icon
     */
	public Color getGeologyMapLabelColor() {
		return Color.black;
	}

    /**
     * Gets display icon for regional navigator map.
     * 
     * @param unit the unit to display
     * @return icon
     */
	public Color getRegionMapLabelColor() {
		return Color.black;
	}

    /**
     * Gets display icon for viking navigator map.
     * 
     * @param unit the unit to display
     * @return icon
     */
	public Color getVikingMapLabelColor() {
        return Color.green;
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
    public boolean isGlobeDisplayed(Unit unit) {
        return true;
    }

    /**
     * Gets display color for surface globe.
     * 
     * @return color
     */
    public Color getSurfGlobeColor() {
        return Color.green;
    }

    /**
     * Gets display color for topo globe.
     * 
     * @return color
     */
    public Color getTopoGlobeColor() {
        return Color.black;
    }

	/**
     * Gets display color for geological globe.
     * 
     * @return color
     */
	public Color getGeologyGlobeColor() {
		return lightSlateGray;
	}

    /**
     * Gets display color for regional globe.
     * 
     * @return color
     */
	public Color getRegionGlobeColor() {
		return lightSlateGray;
	}

    /**
     * Gets display color for viking globe.
     * 
     * @return color
     */
	public Color getVikingGlobeColor() {
        return Color.green;
	}

    /**
     * Gets icon for unit button.
     * 
     * @return icon
     */
    public Icon getButtonIcon(Unit unit) {
        return buttonIcon;
    }

    /**
     * Gets a sound appropriate for this unit.
     * 
     * @param unit the unit to display.
     * @returns sound filepath for unit or empty string if none.
     */
    public String getSound(Unit unit) {
    	return SoundConstants.SND_SETTLEMENT;
    }

    /**
     * Gets display icon for geological navigator map.
     * 
     * @param unit the unit to display
     * @return icon
     */
	public Icon getGeologyMapIcon(Unit unit) {
		return geoMapIcon;
	}

	/**
	 * Gets display icon for regional navigator map.
	 * 
	 * @param unit the unit to display
	 * @return icon
	 */
	public Icon getRegionMapIcon(Unit unit) {
		return regionMapIcon;
	}
	/**
	 * Gets display icon for viking navigator map.
	 * 
	 * @param unit the unit to display
	 * @return icon
	 */
	public Icon getVikingMapIcon(Unit unit) {
		return vikingMapIcon;
	}
	
}
