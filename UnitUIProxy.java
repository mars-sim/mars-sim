/**
 * Mars Simulation Project
 * UnitUIProxy.java
 * @version 2.71 2000-09-17
 * @author Scott Davis
 */
 
import java.awt.*;
import javax.swing.*;

/**
 * Abstract user proxy for a unit.  Individual
 * units should be mapped to a particular instance implementation
 * of this interface.
 */
 
public abstract class UnitUIProxy {

    protected Unit unit;
    protected UIProxyManager proxyManager;
    
    public UnitUIProxy(Unit unit, UIProxyManager proxyManager) {
        this.unit = unit;
        this.proxyManager = proxyManager;
    }
    
    /** Returns true if unit is to be displayed on navigator map. */
    public abstract boolean isMapDisplayed();
    /** Returns image icon for surface navigator map. */
    public abstract ImageIcon getSurfMapIcon();
    /** Returns image icon for topo navigator map. */
    public abstract ImageIcon getTopoMapIcon();
    /** returns label color for surface navigator map. */
    public abstract Color getSurfMapLabelColor();
    /** returns label color for topo navigator map. */
    public abstract Color getTopoMapLabelColor();
    /** returns label font for navigator map. */
    public abstract Font getMapLabelFont();
    /** returns range (km) for clicking on unit on navigator map. */
    public abstract double getMapClickRange();
    /** Returns true if unit is to be displayed on globe. */
    public abstract boolean isGlobeDisplayed();
    /** Returns label color for surface globe. */
    public abstract Color getSurfGlobeColor();
    /** Returns label color for topo globe. */
    public abstract Color getTopoGlobeColor();
    /** Returns image icon for unit button. */
    public abstract ImageIcon getButtonIcon();
    /** Returns dialog window for unit. */
    public abstract UnitDialog getUnitDialog(MainDesktopPane desktop);
    /** Returns unit. */
    public Unit getUnit() { return unit; }
}
