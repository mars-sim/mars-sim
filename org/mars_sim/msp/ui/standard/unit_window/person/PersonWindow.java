/**
 * Mars Simulation Project
 * PersonWindow.java
 * @version 2.75 2003-06-12
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.person;

import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.unit_window.*;

/**
 * The PersonWindow is the window for displaying a person.
 */
public class PersonWindow extends UnitWindow {
    
    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param proxy the unit UI proxy for this window.
     */
    public PersonWindow(MainDesktopPane desktop, UnitUIProxy proxy) {
        // Use UnitWindow constructor
        super(desktop, proxy);
        
        // Add tab panels
        PersonUIProxy personProxy = (PersonUIProxy) proxy;
        addTabPanel(new LocationTabPanel(proxy, desktop));
        addTabPanel(new InventoryTabPanel(proxy, desktop));
        addTabPanel(new AttributeTabPanel(proxy, desktop));
        addTabPanel(new SkillTabPanel(proxy, desktop));
        addTabPanel(new ActivityTabPanel(proxy, desktop));
    }
}
