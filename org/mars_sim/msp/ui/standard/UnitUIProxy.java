/**
 * Mars Simulation Project
 * UnitUIProxy.java
 * @version 2.75 2003-07-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  
 
import java.awt.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.ui.standard.unit_window.UnitWindow;

/**
 * Abstract user proxy for a unit.  Individual
 * units should be mapped to a particular instance implementation
 * of this interface.
 */
public abstract class UnitUIProxy {

    // Data members
    protected Unit unit;
    protected UIProxyManager proxyManager;
    
    /** Constructs a UnitUIProxy object 
     *  @param unit the unit
     *  @param proxyManager the unit's proxy manager
     */
    public UnitUIProxy(Unit unit, UIProxyManager proxyManager) {
        this.unit = unit;
        this.proxyManager = proxyManager;
    }
    
    /** Returns true if unit is to be displayed on navigator map. 
     *  @return true if unit is to be displayed on navigator map
     */
    public abstract boolean isMapDisplayed();
    
    /** Returns image icon for surface navigator map. 
     *  @return image for surface navigator map
     */
    public abstract ImageIcon getSurfMapIcon();
    
    /** Returns image icon for topo navigator map. 
     *  @return image for topo navigator map
     */
    public abstract ImageIcon getTopoMapIcon();
    
    /** returns label color for surface navigator map. 
     *  @return label color for surface navigator map
     */
    public abstract Color getSurfMapLabelColor();
    
    /** returns label color for topo navigator map. 
     *  @return label color for topo navigator map
     */
    public abstract Color getTopoMapLabelColor();
    
    /** returns label font for navigator map. 
     *  @return label font for navigator map
     */
    public abstract Font getMapLabelFont();

    /** returns range (km) for clicking on unit on navigator map. 
     *  @return range (km) for clicking on unit on navigator map
     */
    public abstract double getMapClickRange();
    
    /** Returns true if unit is to be displayed on globe. 
     *  @return true if unit is to be displayed on globe
     */
    public abstract boolean isGlobeDisplayed();
    
    /** Returns label color for surface globe. 
     *  @return label color for surface globe
     */
    public abstract Color getSurfGlobeColor();
    
    /** Returns label color for topo globe. 
     *  @return label color for topo globe
     */
    public abstract Color getTopoGlobeColor();
    
    /** Returns image icon for unit button. 
     *  @return image for unit button
     */
    public abstract ImageIcon getButtonIcon();
    
    /** 
     * Gets a window for unit. 
     * 
     * @param desktop the desktop pane
     * @return unit window
     */
    public abstract UnitWindow getUnitWindow(MainDesktopPane desktop);
    
    /** Returns unit. 
     *  @return unit
     */
    public Unit getUnit() { return unit; }
    
    /**
     * Gets the UI proxy manager.
     */
    public UIProxyManager getUIProxyManager() {
        return proxyManager;
    }
    
    /**
     * String display
     */
    public String toString() {
        return unit.toString();
    }
}
