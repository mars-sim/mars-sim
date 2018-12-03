/**
 * Mars Simulation Project
 * PersonWindow.java
 * @version 3.1.0 2017-03-19
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

/**
 * The PersonWindow is the window for displaying a person.
 */
public class PersonWindow
extends UnitWindow {

	/** Is person dead? */
	private boolean deadCache = false;

	private Person person;

	/**
	 * Constructor.
	 * @param desktop the main desktop panel.
	 * @param person the person for this window.
	 */
	public PersonWindow(MainDesktopPane desktop, Person person) {
		// Use UnitWindow constructor
		super(desktop, person, true);
		this.person = person;

		// Add tab panels
		addTabPanel(new TabPanelActivity(person, desktop));

		addTabPanel(new TabPanelAttribute(person, desktop));
		// 2015-03-31  Added TabPanelCareer
		addTabPanel(new TabPanelCareer(person, desktop));

		// Add death tab panel if person is dead.
		if (person.getPhysicalCondition().isDead()) {
			deadCache = true;
			addTabPanel(new TabPanelDeath(person, desktop));
		}
		else
			deadCache = false;

		// 2015-02-27  Added TabPanelFavorite
		addTabPanel(new TabPanelFavorite(person, desktop));

		addTabPanel(new TabPanelGeneral(person, desktop));

		addTabPanel(new TabPanelHealth(person, desktop));

		addTabPanel(new InventoryTabPanel(person, desktop));

		addTopPanel(new LocationTabPanel(person, desktop));
		// 2016-11-06 Added TabPanelPersonality
		addTabPanel(new TabPanelPersonality(person, desktop));
		// 2015-03-20  Added TabPanelSchedule
		addTabPanel(new TabPanelSchedule(person, desktop));

		addTabPanel(new TabPanelScience(person, desktop));

		addTabPanel(new TabPanelSkill(person, desktop));

		addTabPanel(new TabPanelSocial(person, desktop));

		addTabPanel(new TabPanelSponsorship(person, desktop));

	    // Add tab sorting
		sortTabPanels();
	}

	/**
	 * Updates this window.
	 */
	@Override
	public void update() {
		super.update();
		//Person person = (Person) unit;
		if (!deadCache) {
			if (person.isDeclaredDead()) {
				deadCache = true;
				addTabPanel(new TabPanelDeath(person, desktop));
			}
		}
	}
}
