/**
 * Mars Simulation Project
 * PersonUIProxy.java
 * @version 2.74 2002-01-13
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import java.awt.*;
import javax.swing.*;

/**
 * Standard user interface proxy for a person.
 */
public class PersonUIProxy extends UnitUIProxy {

    // Data members
    private static ImageIcon buttonIcon = new ImageIcon("images/PersonIcon.gif");
    private UnitDialog unitDialog;

    /** Constructs a PersonUIProxy object 
     *  @param person the person
     *  @param proxyManager the UI proxy manager
     */
    public PersonUIProxy(Person person, UIProxyManager proxyManager) {
        super(person, proxyManager);

        unitDialog = null;
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

    /** Returns dialog window for person. 
     *  @return dialog window for person
     */
    public UnitDialog getUnitDialog(MainDesktopPane desktop) {
        if (unitDialog == null)
            unitDialog = new PersonDialog(desktop, this);
        return unitDialog;
    }
}
