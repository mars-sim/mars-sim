/**
 * Mars Simulation Project
 * PersonWindow.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

/**
 * The PersonWindow is the window for displaying a person.
 */
public class PersonWindow extends UnitWindow {
    
    private boolean dead = false;  // Is person dead?
    
    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param person the person for this window.
     */
    public PersonWindow(MainDesktopPane desktop, Person person) {
        // Use UnitWindow constructor
        super(desktop, person, false);
        
        // Add tab panels
        addTopPanel(new LocationPanel(person, desktop));
        addTabPanel(new InventoryTabPanel(person, desktop));
        addTabPanel(new AttributeTabPanel(person, desktop));
        addTabPanel(new SkillTabPanel(person, desktop));
        addTabPanel(new ActivityTabPanel(person, desktop));
        addTabPanel(new HealthTabPanel(person, desktop));
        addTabPanel(new GeneralTabPanel(person, desktop));
        addTabPanel(new SocialTabPanel(person, desktop));
        addTabPanel(new ScienceTabPanel(person, desktop));
        
        // Add death tab panel if person is dead.
        if (person.getPhysicalCondition().isDead()) {
            dead = true;
            addTabPanel(new DeathTabPanel(person, desktop));
        }
        else dead = false;
    }
    
    /**
     * Updates this window.
     */
    public void update() {
        super.update();
        
        Person person = (Person) unit;
        
        if (!dead) {
            if (person.getPhysicalCondition().isDead()) {
                dead = true;
                addTabPanel(new DeathTabPanel(person, desktop));
            }
        }
    }
}
