/**
 * Mars Simulation Project
 * EquipmentUIProxy.java
 * @version 2.75 2003-07-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import java.awt.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.equipment.Equipment;

/**
 * Standard user interface proxy for equipment.
 */
public class EquipmentUIProxy extends UnitUIProxy {

    // Data members
    private static ImageIcon buttonIcon = ImageLoader.getIcon("EquipmentIcon");
    private UnitDialog unitDialog;

    /** Constructs a EquipmentUIProxy object
     *  @param equipment the equipment
     *  @param proxyManager the UI proxy manager
     */
    public EquipmentUIProxy(Equipment equipment, UIProxyManager proxyManager) {
        super(equipment, proxyManager);

        unitDialog = null;
    }

    /** Returns true if this equipment is to be displayed on navigator map.
     *  @return true if this equipment is to be displayed on navigator map
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

    /** returns range (km) for clicking on this equipment on navigator map.
     *  @return range (km) for clicking on this equipment on navigator map
     */
    public double getMapClickRange() { return 0D; }

    /** Returns true if equipment is to be displayed on globe.
     *  @return true if equipment is to be displayed on globe
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

    /** Returns dialog window for equipment.
     *  @return dialog window for equipment
     */
    public UnitDialog getUnitDialog(MainDesktopPane desktop) {
        return null;
    }
}
