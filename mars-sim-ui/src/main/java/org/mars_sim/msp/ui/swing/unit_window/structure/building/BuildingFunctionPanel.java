/*
 * Mars Simulation Project
 * BuildingFunctionPanel.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import javax.swing.Icon;

import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.SalvageTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The BuildingFunctionPanel class is a panel representing a function for a
 * settlement building.
 */
@SuppressWarnings("serial")
public abstract class BuildingFunctionPanel extends TabPanel {

	/** The building this panel is for. */
	protected Building building;
	
	/**
	 * Constructor 1.
	 * 
	 * @param title Shown as the tab title
	 * @param description Shown as the long title/description at the top of the displayed panel
	 * @param building The building this panel is for.
	 * @param desktop  The main desktop.
	 */
	protected BuildingFunctionPanel(String title, Building building, MainDesktopPane desktop) {
		this(title, title, building, desktop);
	}
	
	/**
	 * Constructor 2.
	 * 
	 * @param title
	 * @param tabIcon
	 * @param building
	 * @param desktop
	 */
	protected BuildingFunctionPanel(String title, Icon tabIcon, Building building, MainDesktopPane desktop) {
		// User TabPanel constructor
		super (title, title, tabIcon, title, building, desktop);

		this.building = building;
	}
	
	/**
	 * Constructor 3.
	 * 
	 * @param title Shown as the tab title
	 * @param description Shown as the long title/description at the top of the displayed panel
	 * @param building The building this panel is for.
	 * @param desktop  The main desktop.
	 */
	protected BuildingFunctionPanel(String title, String description, Building building, MainDesktopPane desktop) {
		// User TabPanel constructor
		super(title, description, null, description, building, desktop);

		this.building = building;
	}
}
