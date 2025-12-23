/*
 * Mars Simulation Project
 * PersonUnitWindow.java
 * @date 2023-06-04
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.jidesoft.plaf.windows.TMSchema.Prop;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.shift.ShiftSlot;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityContentPanel;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfo;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfoFactory;
import com.mars_sim.ui.swing.unit_window.InventoryTabPanel;
import com.mars_sim.ui.swing.unit_window.LocationTabPanel;
import com.mars_sim.ui.swing.unit_window.NotesTabPanel;
import com.mars_sim.ui.swing.unit_window.UnitWindow;


/**
 * The PersonWindow is the window for displaying a person.
 */
@SuppressWarnings("serial")
public class PersonUnitWindow extends EntityContentPanel<Person> {
		
	/** Is person dead? */
	private TabPanelDeath tabPanelDeath;
	
	/**
	 * Constructor.
	 * 
	 * @param person the Person to display.
	 * @param context the overall UI context.
	 */
	public PersonUnitWindow(Person person, UIContext context, Properties props) {
				
		super(person, context);
		
		// Add tab panels	
		addTabPanel(new TabPanelGeneral(person, context));
		addTabPanel(new TabPanelActivity(person, context));
		addTabPanel(new TabPanelAttribute(person, context));
		//addTabPanel(new TabPanelCareer(person, context));

		// Add death tab panel if person is dead.
		// if (person.isDeclaredDead()
		// 		|| person.getPhysicalCondition().isDead()) {			
		// 	tabPanelDeath = new TabPanelDeath(person, context);
		// 	addTabPanel(tabPanelDeath);
		// }
		addTabPanel(new TabPanelFavorite(person, context));
		//addTabPanel(new TabPanelHealth(person, context));
		addTabPanel(new InventoryTabPanel(person, context));
		addTabPanel(new LocationTabPanel(person, context));
		addTabPanel(new NotesTabPanel(person, context));
		addTabPanel(new TabPanelPersonality(person, context));
		addTabPanel(new TabPanelSchedule(person, context));
		//addTabPanel(new TabPanelScienceStudy(person, context));
		addTabPanel(new TabPanelSkill(person, context));
		//addTabPanel(new TabPanelSocial(person, context));		

		applyProps(props);
	}
	
	/**
	 * Updates this window.
	 */
	// @Override
	// public void update() {
	// 	super.update();
		
	// 	String title = person.getName() 
	// 			+ " of " + 
	// 			((person.getAssociatedSettlement() != null) ? person.getAssociatedSettlement() : person.getBuriedSettlement())
	// 			+ " (" + (person.getLocationStateType().getName()) + ")";
	// 	super.setTitle(title);
		
	// 	if (!deadCache 
	// 		&& (person.isDeclaredDead()
	// 		|| person.getPhysicalCondition().isDead())) {
	// 		deadCache = true;
	// 		addTabPanel(new TabPanelDeath(person, desktop));
	// 	}
		
	// 	if (deadCache && !person.getPhysicalCondition().isDead()) {
	// 		deadCache = false;
	// 		removeTabPanel(tabPanelDeath);
	// 	}		
	// }
}
