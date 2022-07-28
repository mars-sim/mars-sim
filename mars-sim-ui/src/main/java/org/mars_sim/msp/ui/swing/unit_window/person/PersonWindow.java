/*
 * Mars Simulation Project
 * PersonWindow.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import javax.swing.event.ChangeEvent;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.NotesTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

/**
 * The PersonWindow is the window for displaying a person.
 */
@SuppressWarnings("serial")
public class PersonWindow extends UnitWindow {

	/** Is person dead? */
	private boolean deadCache = false;

	private Person person;

	/**
	 * Constructor.
	 * 
	 * @param desktop the main desktop panel.
	 * @param person  the person for this window.
	 */
	public PersonWindow(MainDesktopPane desktop, Person person) {
		// Use UnitWindow constructor
		super(desktop, person, person.getAssociatedSettlement().getName() + " - " + person.getNickName(), true);
		this.person = person;
	
		// Add tab panels	
		addTabPanel(new TabPanelGeneral(person, desktop));

		addTabPanel(new TabPanelActivity(person, desktop));
		
		addTabPanel(new TabPanelAttribute(person, desktop));

		addTabPanel(new TabPanelCareer(person, desktop));

		// Add death tab panel if person is dead.
		if (person.getPhysicalCondition().isDead()) {
			deadCache = true;
			addTabPanel(new TabPanelDeath(person, desktop));
		} else
			deadCache = false;

		addTabPanel(new TabPanelFavorite(person, desktop));

		addTabPanel(new TabPanelHealth(person, desktop));

		addTabPanel(new InventoryTabPanel(person, desktop));

		addTopPanel(new LocationTabPanel(person, desktop));

		addTabPanel(new NotesTabPanel(person, desktop));
		
		addTabPanel(new TabPanelPersonality(person, desktop));
		
		addTabPanel(new TabPanelSchedule(person, desktop));

		addTabPanel(new TabPanelScienceStudy(person, desktop));

		addTabPanel(new TabPanelSkill(person, desktop));

		addTabPanel(new TabPanelSocial(person, desktop));

		addTabPanel(new TabPanelSponsor(person, desktop));

		// Add to tab panels. 
		addTabIconPanels();
	}

	/**
	 * Updates this window.
	 */
	@Override
	public void update() {
		super.update();

		if (!deadCache) {
			if (person.isDeclaredDead()) {
				deadCache = true;
				addTabPanel(new TabPanelDeath(person, desktop));
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// nothing
	}
}
