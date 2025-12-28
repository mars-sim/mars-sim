/*
 * Mars Simulation Project
 * PersonUnitWindow.java
 * @date 2023-06-04
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.util.Properties;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityContentPanel;
import com.mars_sim.ui.swing.entitywindow.worker.TabPanelActivity;
import com.mars_sim.ui.swing.entitywindow.worker.TabPanelAttribute;
import com.mars_sim.ui.swing.entitywindow.worker.TabPanelSchedule;
import com.mars_sim.ui.swing.entitywindow.worker.TabPanelSkill;
import com.mars_sim.ui.swing.unit_window.InventoryTabPanel;
import com.mars_sim.ui.swing.unit_window.LocationTabPanel;
import com.mars_sim.ui.swing.unit_window.NotesTabPanel;


/**
 * The PersonWindow is the window for displaying a person.
 */
@SuppressWarnings("serial")
public class PersonUnitWindow extends EntityContentPanel<Person> 
{
		
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
		addTabPanel(new TabPanelCareer(person, context));

		// Add death tab panel if person is dead.
		if (person.isDeclaredDead()
			|| person.getPhysicalCondition().isDead()) {			
			tabPanelDeath = new TabPanelDeath(person, context);
			addTabPanel(tabPanelDeath);
		}
		addTabPanel(new TabPanelFavorite(person, context));
		addTabPanel(new TabPanelHealth(person, context));
		addTabPanel(new InventoryTabPanel(person, context));
		addTabPanel(new LocationTabPanel(person, context));
		addTabPanel(new NotesTabPanel(person, context));
		addTabPanel(new TabPanelPersonality(person, context));
		addTabPanel(new TabPanelSchedule(person, context));
		addTabPanel(new TabPanelScienceStudy(person, context));
		addTabPanel(new TabPanelSkill(person, context));
		addTabPanel(new TabPanelSocial(person, context));		

		applyProps(props);
	}
	
	@Override
	public void entityUpdate(EntityEvent event) {
		// Trap death event to add/remove death tab panel
		if (EntityEventType.DEATH_EVENT.equals(event.getType())
				&& (tabPanelDeath == null)) {
			tabPanelDeath = new TabPanelDeath(getEntity(), getContext());
			addTabPanel(tabPanelDeath);
		}
		super.entityUpdate(event);
	}
}
