/**
 * Mars Simulation Project
 * PersonWindow.java
 * @version 2.75 2003-07-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.person;

import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.unit_window.*;

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
        addTabPanel(new LocationTabPanel(person, desktop));
        addTabPanel(new InventoryTabPanel(person, desktop));
        addTabPanel(new AttributeTabPanel(person, desktop));
        addTabPanel(new SkillTabPanel(person, desktop));
        addTabPanel(new ActivityTabPanel(person, desktop));
        addTabPanel(new HealthTabPanel(person, desktop));
        
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
    protected void update() {
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
