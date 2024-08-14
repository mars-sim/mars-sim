/*
 * Mars Simulation Project
 * AbstractUnitDisplayInfo.java
 * @date 2023-04-28
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_display_info;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;

import com.mars_sim.core.Unit;
import com.mars_sim.core.map.MapMetaData;

/**
 * Abstract noop implementation of the UnitDisplayInfo. Used as a 
 * seed for subclasses.
 */
abstract class AbstractUnitDisplayInfo implements UnitDisplayInfo {

    @Override
    public boolean isMapDisplayed(Unit unit) {
        return false;
    }

    @Override
    public Icon getMapIcon(Unit unit, MapMetaData type) {
        return null;
    }
    
    @Override
    public boolean isMapBlink(Unit unit) {
        return false;
    }

    @Override
    public Color getMapLabelColor(MapMetaData type) {
        return null;
    }

    
    @Override
    public Font getMapLabelFont() {
        return null;
    }

    @Override
    public double getMapClickRange() {
        return 15;
    }

    @Override
    public boolean isGlobeDisplayed(Unit unit) {
        return false;
    }

    @Override
    public Color getGlobeColor(MapMetaData type) {
        return null;
    }
    
    @Override
    public Icon getButtonIcon(Unit unit) {
        return null;
    }

    @Override
    public String getSound(Unit unit) {
        return null;
    }
    
}
