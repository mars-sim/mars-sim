/**
 * Mars Simulation Project
 * AbstractUnitDisplayInfo.java
 * @date 19-02-2023
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.unit_display_info;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;

import org.mars_sim.msp.core.Unit;

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
    public Icon getSurfMapIcon(Unit unit) {
        return null;
    }

    @Override
    public Icon getTopoMapIcon(Unit unit) {
        return null;
    }

    @Override
    public Icon getGeologyMapIcon(Unit unit) {
        return null;
    }

    @Override
    public boolean isMapBlink(Unit unit) {
        return false;
    }

    @Override
    public Color getSurfMapLabelColor() {
        return null;
    }

    @Override
    public Color getTopoMapLabelColor() {
        return null;
    }

    @Override
    public Color getGeologyMapLabelColor() {
        return null;
    }

    @Override
    public Font getMapLabelFont() {
        return null;
    }

    @Override
    public double getMapClickRange() {
        return 0;
    }

    @Override
    public boolean isGlobeDisplayed(Unit unit) {
        return false;
    }

    @Override
    public Color getSurfGlobeColor() {
        return null;
    }

    @Override
    public Color getTopoGlobeColor() {
        return null;
    }

    @Override
    public Color getGeologyGlobeColor() {
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
