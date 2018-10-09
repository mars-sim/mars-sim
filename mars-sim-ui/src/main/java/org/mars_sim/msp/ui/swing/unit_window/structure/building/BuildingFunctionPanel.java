/**
 * Mars Simulation Project
 * BuildingFunctionPanel.java
 * @version 3.1.0 2017-09-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.Dimension;

import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

import com.alee.laf.panel.WebPanel;

/**
 * The BuildingFunctionPanel class is a panel representing a function for a
 * settlement building.
 */
public abstract class BuildingFunctionPanel extends WebPanel {

	/** The building this panel is for. */
	protected Building building;
	/** The main desktop. */
	protected MainDesktopPane desktop;

	/**
	 * Constructor.
	 * 
	 * @param building The building this panel is for.
	 * @param desktop  The main desktop.
	 */
	public BuildingFunctionPanel(Building building, MainDesktopPane desktop) {
		// User JPanel constructor
		super();

		// Initialize data members
		this.building = building;
		this.desktop = desktop;

		this.setMaximumSize(new Dimension(UnitWindow.WIDTH, UnitWindow.HEIGHT));

		setBorder(new MarsPanelBorder());
	}

	/**
	 * Update this panel.
	 */
	public abstract void update();
}
