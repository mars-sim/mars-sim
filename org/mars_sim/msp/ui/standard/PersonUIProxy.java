/**
 * Mars Simulation Project
 * PersonUIProxy.java
 * @version 2.75 2003-07-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import java.awt.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.ui.standard.unit_window.UnitWindow;
import org.mars_sim.msp.ui.standard.unit_window.person.PersonWindow;

/**
 * Standard user interface proxy for a person.
 */
public class PersonUIProxy extends UnitUIProxy {

    // Data members
    private static ImageIcon buttonIcon = ImageLoader.getIcon("PersonIcon");
    private UnitWindow unitWindow;

    /** Constructs a PersonUIProxy object
     *  @param person the person
     *  @param proxyManager the UI proxy manager
     */
    public PersonUIProxy(Person person, UIProxyManager proxyManager) {
        super(person, proxyManager);
    }

    /** Returns true if this person is to be displayed on navigator map.
     *  @return true if this person is to be displayed on navigator map
     */
    public boolean isMapDisplayed() { return false; }

    /** Returns image icon for surface navigator map.
     *  @return image for surface navigator map
     */
    public ImageIcon getSurfMapIcon() { return null; }

    /** Returns image icon for topo navigator map.
     *  @return image for topo navigator map
     */
    public ImageIcon getTopoMapIcon() { return null; }

    /** returns label color for surface navigator map.
     *  @return label color for surface navigator map
     */
    public Color getSurfMapLabelColor() { return null; }

    /** returns label color for topo navigator map.
     *  @return label color for topo navigator map
     */
    public Color getTopoMapLabelColor() { return null; }

    /** returns label font for navigator map.
     *  @return label font for navigator map
     */
    public Font getMapLabelFont() { return null; }

    /** returns range (km) for clicking on this person on navigator map.
     *  @return range (km) for clicking on this person on navigator map
     */
    public double getMapClickRange() { return 0D; }

    /** Returns true if person is to be displayed on globe.
     *  @return true if person is to be displayed on globe
     */
    public boolean isGlobeDisplayed() { return false; }

    /** Returns label color for surface globe.
     *  @return label color for surface globe
     */
    public Color getSurfGlobeColor() { return null; }

    /** Returns label color for topo globe.
     *  @return label color for topo globe
     */
    public Color getTopoGlobeColor() { return null; }

    /** Returns image icon for unit button.
     *  @return image for unit button
     */
    public ImageIcon getButtonIcon() { return buttonIcon; }

    /** 
     * Gets a window for unit. 
     * 
     * @param desktop the desktop pane
     * @return unit window
     */
    public UnitWindow getUnitWindow(MainDesktopPane desktop) {
        if (unitWindow == null) unitWindow = new PersonWindow(desktop, this);
        return unitWindow;
    }
}
