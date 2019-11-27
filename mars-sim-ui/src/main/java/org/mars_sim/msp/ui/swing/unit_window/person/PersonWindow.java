/**
 * Mars Simulation Project
 * PersonWindow.java
 * @version 3.1.0 2017-03-19
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
	
//	private TabPanelActivity tabPanelActivity;
//	private TabPanelAttribute tabPanelAttribute;
//	private TabPanelCareer tabPanelCareer;
//	private TabPanelFavorite tabPanelFavorite;
//	private TabPanelGeneral tabPanelGeneral;
//	private TabPanelHealth tabPanelHealth;
//	private InventoryTabPanel inventoryTabPanel;
//	private LocationTabPanel locationTabPanel;
//	private TabPanelSchedule tabPanelSchedule;
//	private TabPanelScience tabPanelScience;
//	private TabPanelSkill tabPanelSkill;
//	private TabPanelSocial tabPanelSocial;
//	private TabPanelSponsorship tabPanelSponsorship;

	/**
	 * Constructor.
	 * 
	 * @param desktop the main desktop panel.
	 * @param person  the person for this window.
	 */
	public PersonWindow(MainDesktopPane desktop, Person person) {
		// Use UnitWindow constructor
		super(desktop, person, true);
		this.person = person;
	
		// Add tab panels
		
//		tabPanelActivity = new TabPanelActivity(person, desktop);
//		addTabPanel(tabPanelActivity);
//
//		tabPanelAttribute = new TabPanelAttribute(person, desktop);
//		addTabPanel(tabPanelAttribute);
		
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

		addTabPanel(new TabPanelGeneral(person, desktop));

		addTabPanel(new TabPanelHealth(person, desktop));

		addTabPanel(new NotesTabPanel(person, desktop));
		
		addTabPanel(new InventoryTabPanel(person, desktop));

		addTopPanel(new LocationTabPanel(person, desktop));

//		addTabPanel(new TabPanelPersonality(person, desktop));

		addTabPanel(new TabPanelSchedule(person, desktop));

		addTabPanel(new TabPanelScience(person, desktop));

		addTabPanel(new TabPanelSkill(person, desktop));

		addTabPanel(new TabPanelSocial(person, desktop));

		addTabPanel(new TabPanelSponsorship(person, desktop));

		// Add tab sorting
		sortTabPanels();
	}

//	public void initializeUI(TabPanel tabPanel) {
//		tabPanel.initializeUI();
//	}

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

//		TabPanel newTab = (TabPanel)e.getSource();//getSelected();
//		System.out.println("oldTab : " + oldTab + "    newTab : " + newTab);
//		
//		if (oldTab == null || newTab != oldTab) {
//			oldTab = newTab;
//			
//			if (!newTab.isUIDone());
//				newTab.initializeUI();
//				
////			if (newTab instanceof TabPanelActivity) {
////				if (tabPanelActivity.isUIDone());
////				 	tabPanelActivity.initializeUI();
////			} else if (newTab instanceof TabPanelAttribute) {
////				
////			}
//		}
	}
}
