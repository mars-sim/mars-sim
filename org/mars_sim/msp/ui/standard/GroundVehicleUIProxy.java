/**
 * Mars Simulation Project
 * GroundVehicleUIProxy.java
 * @version 2.74 2002-03-15
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.vehicle.*;
import java.awt.*;
import javax.swing.*;

/**
 * Standard user interface proxy for a ground vehicle.
 */
public abstract class GroundVehicleUIProxy extends VehicleUIProxy {

    // Data members
    private GroundVehicle groundVehicle;
    protected static ImageIcon surfMapIcon = ImageLoader.getIcon("VehicleSymbol");
    protected static ImageIcon topoMapIcon = ImageLoader.getIcon("VehicleSymbolBlack");
    protected static Font mapLabelFont = new Font("SansSerif", Font.PLAIN, 9);
    protected ImageIcon buttonIcon;
    protected UnitDialog unitDialog;

    /** Constructs a GroundVehicleUIProxy object
     *  @param groundVehicle the ground vehicle
     *  @param proxyManager the unit UI proxy manager
     *  @param buttonIcon the unit button image
     */
    public GroundVehicleUIProxy(GroundVehicle groundVehicle,
            UIProxyManager proxyManager) {
        super(groundVehicle, proxyManager);

        this.groundVehicle = groundVehicle;
    }

    /** Returns true if this ground vehicle is to be displayed on navigator map.
     *  @return true if displayed on navigator map
     */
    public boolean isMapDisplayed() {
        if (groundVehicle.getSettlement() == null) return true;
        else return false;
    }

    /** Returns image icon for surface navigator map.
     *  @return image icon for surface navigator map
     */
    public ImageIcon getSurfMapIcon() {
        return surfMapIcon;
    }

    /** Returns image icon for topo navigator map.
     *  @return image icon for topo navigator map
     */
    public ImageIcon getTopoMapIcon() {
        return topoMapIcon;
    }

    /** returns label color for surface navigator map.
     *  @return label color for surface navigator map
     */
    public Color getSurfMapLabelColor() {
        return Color.white;
    }

    /** returns label color for topo navigator map.
     *  @return label color for topo navigator map
     */
    public Color getTopoMapLabelColor() {
        return Color.black;
    }

    /** returns label font for navigator map.
     *  @return label font for navigator map
     */
    public Font getMapLabelFont() {
        return mapLabelFont;
    }

    /** returns range (km) for clicking on this ground vehicle on navigator map.
     *  @return range (km) for clicking on this ground vehicle on navigator map
     */
    public double getMapClickRange() {
        return 40D;
    }

    /** Returns true if ground vehicle is to be displayed on globe.
     *  @return true if ground vehicle is to be displayed on globe
     */
    public boolean isGlobeDisplayed() {
        if (groundVehicle.getSettlement() == null) return true;
        else return false;
    }

    /** Returns label color for surface globe.
     *  @return label color for surface globe
     */
    public Color getSurfGlobeColor() {
        return Color.white;
    }

    /** Returns label color for topo globe.
     *  @return label color for topo globe
     */
    public Color getTopoGlobeColor() {
        return Color.black;
    }

    /** Returns image icon for unit button.
     *  @return image icon for unit button
     */
    public ImageIcon getButtonIcon() {
        return buttonIcon;
    }
}
