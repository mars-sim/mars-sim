/**
 * Mars Simulation Project
 * SettlementUIProxy.java
 * @version 2.74 2002-01-13
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;
import java.awt.*;
import javax.swing.*;

/** Standard user interface proxy for a settlement.
 */
public class SettlementUIProxy extends UnitUIProxy {

    // Data members
    private static ImageIcon surfMapIcon = ImageLoader.getIcon("SettlementSymbol");
    private static ImageIcon topoMapIcon = ImageLoader.getIcon("SettlementSymbolBlack");
    private static Font mapLabelFont = new Font("SansSerif", Font.PLAIN, 12);
    private static ImageIcon buttonIcon = ImageLoader.getIcon("SettlementIcon");
    private UnitDialog unitDialog;

    /** Constructs a SettlementUIProxy object
     *  @param settlement the settlement
     *  @proxyManager the settlement's UI proxy manager
     */
    public SettlementUIProxy(Settlement settlement, UIProxyManager proxyManager) {
        super(settlement, proxyManager);

        unitDialog = null;
    }

    /** Returns true if this settlement is to be displayed on navigator map.
     *  @return true if this settlement is to be displayed on navigator map
     */
    public boolean isMapDisplayed() { return true; }

    /** Returns image icon for surface navigator map.
     *  @return image for surface navigator map
     */
    public ImageIcon getSurfMapIcon() { return surfMapIcon; }

    /** Returns image icon for topo navigator map.
     *  @return image for topo navigator map
     */
    public ImageIcon getTopoMapIcon() { return topoMapIcon; }

    /** returns label color for surface navigator map.
     *  @return label color for surface navigator map
     */
    public Color getSurfMapLabelColor() { return Color.green; }

    /** returns label color for topo navigator map.
     *  @return label color for topo navigator map
     */
    public Color getTopoMapLabelColor() { return Color.black; }

    /** returns label font for navigator map.
     *  @return label font for navigator map
     */
    public Font getMapLabelFont() { return mapLabelFont; }

    /** returns range (km) for clicking on this settlement on navigator map.
     *  @return range (km) for clicking on this settlement on navigator map
     */
    public double getMapClickRange() { return 90D; }

    /** Returns true if settlement is to be displayed on globe.
     *  @return true if settlement is to be displayed on globe
     */
    public boolean isGlobeDisplayed() { return true; }

    /** Returns label color for surface globe.
     *  @return label color for surface globe
     */
    public Color getSurfGlobeColor() { return Color.green; }

    /** Returns label color for topo globe.
     *  @return label color for topo globe
     */
    public Color getTopoGlobeColor() { return Color.black; }

    /** Returns image icon for unit button.
     *  @return image for unit button
     */
    public ImageIcon getButtonIcon() { return buttonIcon; }

    /** Returns dialog window for person.
     *  @param desktop the desktop pane
     *  @return person dialog window
     */
    public UnitDialog getUnitDialog(MainDesktopPane desktop) {
        if (unitDialog == null)
            unitDialog = new SettlementDialog(desktop, this);
        return unitDialog;
    }
}
