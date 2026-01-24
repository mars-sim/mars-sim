/*
 * Mars Simulation Project
 * SettlementDisplayInfo.java
 * @date 2023-04-28
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.displayinfo;

import com.mars_sim.core.Unit;
import com.mars_sim.ui.swing.sound.SoundConstants;

import java.awt.Color;
import java.awt.Font;

/**
 * Provides display information about a settlement.
 */
class SettlementDisplayInfoBean extends MapEntityDisplayInfo {

    /**
     * Constructor
     */
    SettlementDisplayInfoBean() {
        super("settlement", SoundConstants.SND_SETTLEMENT,
             new Font("SansSerif", Font.BOLD, 12), Color.green,
             "map/settlement", "map/settlement_black");
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
}
